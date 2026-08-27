package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.CargaInicialFilaDTO;
import com.sdrerc.v3.domain.CargaInicialResultadoDTO;
import com.sdrerc.v3.infrastructure.dao.CargaInicialDAO;
import com.sdrerc.v3.infrastructure.dao.CargaInicialDAO.ResultadoFila;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Orquestador del batch de carga inicial (migración histórica, ver v3/CLAUDE.md) — Incremento 1
 * (Registro + Asignación). Procesa las filas de forma estrictamente SECUENCIAL (nunca en paralelo,
 * ver Javadoc de {@link CargaInicialDAO}) y registra 1 fila de auditoría por cada fila de Excel en
 * {@code CARGA_INICIAL_LOG}, exista o no el expediente resultante.
 *
 * <p>Regla de confidencialidad: este Service solo maneja conteos agregados a nivel de log de
 * aplicación (ver {@link CargaInicialResultadoDTO}) — nunca imprime/loguea un valor de fila.</p>
 */
@Service
public class CargaInicialImportService {

    private final CargaInicialArchivoParserService parserService;
    private final CargaInicialDAO cargaInicialDAO;

    public CargaInicialImportService(CargaInicialArchivoParserService parserService, CargaInicialDAO cargaInicialDAO) {
        this.parserService = parserService;
        this.cargaInicialDAO = cargaInicialDAO;
    }

    public CargaInicialResultadoDTO ejecutar(InputStream archivo, Long idUsuarioActual) throws IOException {
        List<CargaInicialFilaDTO> filas = parserService.parsear(archivo);
        int importados = 0;
        int incompletos = 0;
        int errores = 0;
        for (CargaInicialFilaDTO fila : filas) {
            ResultadoFila resultado = cargaInicialDAO.procesarFila(fila, idUsuarioActual);
            cargaInicialDAO.registrarLog(fila.getHojaOrigen(), fila.getFilaExcel(), resultado, idUsuarioActual);
            switch (resultado.estado()) {
                case CargaInicialDAO.ESTADO_IMPORTADO -> importados++;
                case CargaInicialDAO.ESTADO_INCOMPLETO -> incompletos++;
                default -> errores++;
            }
        }
        return new CargaInicialResultadoDTO(filas.size(), importados, incompletos, errores);
    }

    /**
     * Segunda pasada, exclusiva de Asignación — reprocesa el MISMO Excel de la 1ra pasada, pero
     * solo reintenta la resolución de abogado (matching mejorado: tildes/coma/espacios, alias de
     * typos, incluye USUARIO inactivos) sobre expedientes YA CREADOS por {@link #ejecutar}. Nunca
     * vuelve a crear Registro (persona/solicitud/acta/documento) — busca el expediente ya existente
     * de cada fila en {@code CARGA_INICIAL_LOG} (por hoja+fila de Excel) y, si esa fila nunca llegó a
     * crear expediente (quedó en ERROR en la 1ra pasada), la omite sin contarla en el resultado.
     */
    public CargaInicialResultadoDTO ejecutarReintentoAsignacion(InputStream archivo, Long idUsuarioActual) throws IOException {
        List<CargaInicialFilaDTO> filas = parserService.parsear(archivo);
        int consideradas = 0;
        int importados = 0;
        int incompletos = 0;
        int errores = 0;
        for (CargaInicialFilaDTO fila : filas) {
            Long idExpediente = cargaInicialDAO.buscarExpedienteResultante(fila.getHojaOrigen(), fila.getFilaExcel());
            if (idExpediente == null) {
                continue;
            }
            consideradas++;
            ResultadoFila resultado = cargaInicialDAO.reintentarAsignacion(fila, idExpediente, idUsuarioActual);
            cargaInicialDAO.registrarLog(fila.getHojaOrigen(), fila.getFilaExcel(), resultado, idUsuarioActual);
            switch (resultado.estado()) {
                case CargaInicialDAO.ESTADO_IMPORTADO -> importados++;
                case CargaInicialDAO.ESTADO_INCOMPLETO -> incompletos++;
                default -> errores++;
            }
        }
        return new CargaInicialResultadoDTO(consideradas, importados, incompletos, errores);
    }

    /**
     * Reprocesa EXCLUSIVAMENTE las filas que en la 1ra pasada quedaron en {@code ERROR} sin llegar a
     * crear expediente (ej. "sin número de acta"/"hoja de envío duplicada", ambas reglas ya
     * relajadas en {@link CargaInicialDAO#procesarFila} — ver v3/CLAUDE.md 26/08/2026). Nunca
     * duplica una fila que ya tiene expediente: consulta {@link CargaInicialDAO#buscarExpedienteResultante}
     * primero y la omite si ya existe, sin importar el motivo (evita crear un 2do expediente para
     * una fila que la 1ra pasada ya procesó con éxito).
     */
    public CargaInicialResultadoDTO ejecutarPendientes(InputStream archivo, Long idUsuarioActual) throws IOException {
        List<CargaInicialFilaDTO> filas = parserService.parsear(archivo);
        int consideradas = 0;
        int importados = 0;
        int incompletos = 0;
        int errores = 0;
        for (CargaInicialFilaDTO fila : filas) {
            Long idExpedienteExistente = cargaInicialDAO.buscarExpedienteResultante(fila.getHojaOrigen(), fila.getFilaExcel());
            if (idExpedienteExistente != null) {
                continue;
            }
            consideradas++;
            ResultadoFila resultado = cargaInicialDAO.procesarFila(fila, idUsuarioActual);
            cargaInicialDAO.registrarLog(fila.getHojaOrigen(), fila.getFilaExcel(), resultado, idUsuarioActual);
            switch (resultado.estado()) {
                case CargaInicialDAO.ESTADO_IMPORTADO -> importados++;
                case CargaInicialDAO.ESTADO_INCOMPLETO -> incompletos++;
                default -> errores++;
            }
        }
        return new CargaInicialResultadoDTO(consideradas, importados, incompletos, errores);
    }

    /**
     * Corrige {@code numero_expediente} de expedientes YA CREADOS cuando su fila trae un marcador
     * reconocible en "N EXPEDIENTE DIGITAL" (ver Javadoc de {@link CargaInicialDAO#corregirNumeroExpedienteDigital}
     * — decisión confirmada explícitamente por el usuario 26/08/2026: esta fila es un duplicado de
     * otro expediente ya numerado y debe compartir su mismo número, no uno nuevo). Omite filas sin
     * valor en esa columna y filas sin expediente creado (no debería haber ninguna a esta altura, ya
     * que {@link #ejecutarPendientes} ya cerró todos los `ERROR`).
     */
    public CargaInicialResultadoDTO corregirNumeroExpedienteDigital(InputStream archivo, Long idUsuarioActual) throws IOException {
        List<CargaInicialFilaDTO> filas = parserService.parsear(archivo);
        int consideradas = 0;
        int corregidos = 0;
        int sinPatron = 0;
        int errores = 0;
        for (CargaInicialFilaDTO fila : filas) {
            if (fila.getNumeroExpedienteDigital() == null || fila.getNumeroExpedienteDigital().isBlank()) {
                continue;
            }
            Long idExpediente = cargaInicialDAO.buscarExpedienteResultante(fila.getHojaOrigen(), fila.getFilaExcel());
            if (idExpediente == null) {
                continue;
            }
            consideradas++;
            ResultadoFila resultado = cargaInicialDAO.corregirNumeroExpedienteDigital(fila, idExpediente);
            cargaInicialDAO.registrarLog(fila.getHojaOrigen(), fila.getFilaExcel(), resultado, idUsuarioActual);
            switch (resultado.estado()) {
                case CargaInicialDAO.ESTADO_IMPORTADO -> corregidos++;
                case CargaInicialDAO.ESTADO_INCOMPLETO -> sinPatron++;
                default -> errores++;
            }
        }
        return new CargaInicialResultadoDTO(consideradas, corregidos, sinPatron, errores);
    }

    /**
     * Incremento 2 (Análisis + paso técnico por Verificación + Ejecución, ver v3/CLAUDE.md) — reprocesa
     * el mismo Excel, buscando el expediente ya creado por las pasadas anteriores (nunca crea uno
     * nuevo) y avanzándolo con {@link CargaInicialDAO#avanzarAnalisisVerificacionEjecucion}. Omite
     * filas sin expediente resultante (no debería haber ninguna a esta altura).
     */
    public CargaInicialResultadoDTO ejecutarAnalisisVerificacionEjecucion(InputStream archivo, Long idUsuarioActual) throws IOException {
        List<CargaInicialFilaDTO> filas = parserService.parsear(archivo);
        int consideradas = 0;
        int importados = 0;
        int incompletos = 0;
        int errores = 0;
        for (CargaInicialFilaDTO fila : filas) {
            Long idExpediente = cargaInicialDAO.buscarExpedienteResultante(fila.getHojaOrigen(), fila.getFilaExcel());
            if (idExpediente == null) {
                continue;
            }
            consideradas++;
            ResultadoFila resultado = cargaInicialDAO.avanzarAnalisisVerificacionEjecucion(fila, idExpediente, idUsuarioActual);
            cargaInicialDAO.registrarLog(fila.getHojaOrigen(), fila.getFilaExcel(), resultado, idUsuarioActual);
            switch (resultado.estado()) {
                case CargaInicialDAO.ESTADO_IMPORTADO -> importados++;
                case CargaInicialDAO.ESTADO_INCOMPLETO -> incompletos++;
                default -> errores++;
            }
        }
        return new CargaInicialResultadoDTO(consideradas, importados, incompletos, errores);
    }

    /**
     * Incremento 3 (Notificación, ver v3/CLAUDE.md) — reprocesa el mismo Excel, buscando el
     * expediente ya creado por las pasadas anteriores (nunca crea uno nuevo) y sintetizando sus
     * documentos (Resolución + Carta) y, si corresponde, la Notificación real vía
     * {@link CargaInicialDAO#avanzarNotificacion}. Omite filas sin expediente resultante.
     */
    public CargaInicialResultadoDTO ejecutarNotificacion(InputStream archivo, Long idUsuarioActual) throws IOException {
        List<CargaInicialFilaDTO> filas = parserService.parsear(archivo);
        int consideradas = 0;
        int importados = 0;
        int incompletos = 0;
        int errores = 0;
        for (CargaInicialFilaDTO fila : filas) {
            Long idExpediente = cargaInicialDAO.buscarExpedienteResultante(fila.getHojaOrigen(), fila.getFilaExcel());
            if (idExpediente == null) {
                continue;
            }
            consideradas++;
            ResultadoFila resultado = cargaInicialDAO.avanzarNotificacion(fila, idExpediente, idUsuarioActual);
            cargaInicialDAO.registrarLog(fila.getHojaOrigen(), fila.getFilaExcel(), resultado, idUsuarioActual);
            switch (resultado.estado()) {
                case CargaInicialDAO.ESTADO_IMPORTADO -> importados++;
                case CargaInicialDAO.ESTADO_INCOMPLETO -> incompletos++;
                default -> errores++;
            }
        }
        return new CargaInicialResultadoDTO(consideradas, importados, incompletos, errores);
    }
}
