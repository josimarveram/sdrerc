package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.CargaInicialFilaDTO;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

/**
 * Parser del Excel de carga inicial (migración histórica, ver v3/CLAUDE.md) — únicamente las 2
 * hojas ya unificadas ("DATA DIARIA UNIFICADA"/"DATA PASIVO OR UNIFICADA"; el cruce contra
 * EJECUCIONES ya viene hecho en el propio archivo). Alcance del Incremento 1: solo las columnas de
 * Registro + Asignación (ver {@link CargaInicialFilaDTO}).
 *
 * <p><b>Regla de confidencialidad (sin excepción)</b>: este parser NUNCA loguea/imprime un valor de
 * celda. Lee por POSICIÓN de columna (índice 0-based confirmado contra los encabezados reales de
 * cada hoja, fila 1 únicamente) en vez de por nombre de encabezado exacto — evita depender de la
 * codificación exacta de caracteres especiales (ej. "N°") del archivo, y es más robusto que un
 * matching de texto.</p>
 */
@Service
public class CargaInicialArchivoParserService {

    // Índices de columna (0-based) de "DATA DIARIA UNIFICADA", confirmados contra la fila 1 real.
    private static final int D_FECHA_SOLICITUD = 0;
    private static final int D_TIPO_TRAMITE = 1;
    private static final int D_CANAL_RECEPCION = 2;
    private static final int D_DERIVADO_POR = 3;
    private static final int D_DNI_SOLICITANTE = 4;
    private static final int D_N_TRAMITE_WEB = 5;
    private static final int D_TIPO_DOCUMENTO = 6;
    private static final int D_N_DOCUMENTO = 7;
    private static final int D_TIPO_SOLICITUD = 8;
    private static final int D_TIPO_ACTA = 9;
    private static final int D_N_ACTA = 10;
    private static final int D_TITULAR = 11;
    private static final int D_RESPONSABLE_PREEVALUACION = 12;
    private static final int D_FECHA_ASIGNADO = 15;
    private static final int D_N_DOCUMENTO_HOJA_ENVIO = 17;
    private static final int D_N_EXPEDIENTE_DIGITAL = 18;
    private static final int D_ASIGNADO_A_ABOGADO = 20;
    private static final int D_N_RESOLUCION = 21;
    private static final int D_RESOLUCION_LIMPIA = 22;
    private static final int D_EJEC_FECHA_EMISION = 26;
    private static final int D_EJEC_PROCEDIMIENTO = 28;
    private static final int D_EJEC_TIPO_CALIFICACION = 30;
    private static final int D_EJEC_FECHA_EJECUCION = 40;
    private static final int D_EJEC_ESTADO = 41;
    private static final int D_EJEC_ASIGNADO_NOTIF = 45;
    private static final int D_EJEC_MEDIO_NOTIFICACION = 46;
    private static final int D_EJEC_FECHA_EMISION_1_CARTA = 47;
    private static final int D_EJEC_NUMERO_1_CARTA = 48;
    private static final int D_EJEC_FECHA_ELABORACION_2_CARTA = 49;
    private static final int D_EJEC_NUMERO_2_CARTA = 50;

    // Índices de columna (0-based) de "DATA PASIVO OR UNIFICADA", confirmados contra la fila 1 real.
    private static final int P_FECHA_RECEPCION_SDRERC = 1;
    private static final int P_TIPO_TRAMITE = 2;
    private static final int P_DERIVADO_POR = 3;
    private static final int P_N_EXPEDIENTE_LEGADO = 4;
    private static final int P_TIPO_DOCUMENTO = 7;
    private static final int P_N_DOCUMENTO = 8;
    private static final int P_TIPO_ACTA = 9;
    private static final int P_N_ACTA = 10;
    private static final int P_TITULAR = 11;
    private static final int P_FECHA_ASIGNADO = 13;
    private static final int P_N_DOCUMENTO_HOJA_ENVIO = 15;
    private static final int P_N_EXPEDIENTE_DIGITAL = 16;
    private static final int P_ASIGNADO_A_ABOGADO = 18;
    private static final int P_N_RESOLUCION = 19;
    private static final int P_RESOLUCION_LIMPIA = 21;
    private static final int P_EJEC_FECHA_EMISION = 23;
    private static final int P_EJEC_PROCEDIMIENTO = 25;
    private static final int P_EJEC_TIPO_CALIFICACION = 29;
    private static final int P_EJEC_FECHA_EJECUCION = 39;
    private static final int P_EJEC_ESTADO = 40;
    private static final int P_EJEC_ASIGNADO_NOTIF = 44;
    private static final int P_EJEC_MEDIO_NOTIFICACION = 45;
    private static final int P_EJEC_FECHA_EMISION_1_CARTA = 46;
    private static final int P_EJEC_NUMERO_1_CARTA = 47;
    private static final int P_EJEC_FECHA_ELABORACION_2_CARTA = 48;
    private static final int P_EJEC_NUMERO_2_CARTA = 49;

    /**
     * @param archivo ruta fija en disco del Excel unificado (el batch no recibe upload — ver
     *                {@code CargaInicialRunner}).
     */
    public List<CargaInicialFilaDTO> parsear(InputStream archivo) throws IOException {
        List<CargaInicialFilaDTO> filas = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(archivo)) {
            DataFormatter formatter = new DataFormatter();
            Sheet diaria = workbook.getSheet(CargaInicialFilaDTO.HOJA_DIARIA);
            if (diaria != null) {
                parsearHojaDiaria(diaria, formatter, filas);
            }
            Sheet pasivoOr = workbook.getSheet(CargaInicialFilaDTO.HOJA_PASIVO_OR);
            if (pasivoOr != null) {
                parsearHojaPasivoOr(pasivoOr, formatter, filas);
            }
        }
        return filas;
    }

    private void parsearHojaDiaria(Sheet sheet, DataFormatter formatter, List<CargaInicialFilaDTO> filas) {
        for (int r = 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null || esFilaVacia(row, D_TITULAR)) {
                continue;
            }
            CargaInicialFilaDTO dto = new CargaInicialFilaDTO();
            dto.setHojaOrigen(CargaInicialFilaDTO.HOJA_DIARIA);
            dto.setFilaExcel(r + 1);
            dto.setFechaSolicitud(leerFecha(row, D_FECHA_SOLICITUD));
            dto.setTipoTramite(texto(row, D_TIPO_TRAMITE, formatter));
            dto.setCanalRecepcion(texto(row, D_CANAL_RECEPCION, formatter));
            dto.setDerivadoPor(texto(row, D_DERIVADO_POR, formatter));
            dto.setDniSolicitante(texto(row, D_DNI_SOLICITANTE, formatter));
            dto.setNumeroTramiteWeb(texto(row, D_N_TRAMITE_WEB, formatter));
            dto.setTipoDocumento(texto(row, D_TIPO_DOCUMENTO, formatter));
            dto.setNumeroDocumento(texto(row, D_N_DOCUMENTO, formatter));
            dto.setTipoSolicitud(texto(row, D_TIPO_SOLICITUD, formatter));
            dto.setTipoActa(texto(row, D_TIPO_ACTA, formatter));
            dto.setNumeroActa(texto(row, D_N_ACTA, formatter));
            dto.setTitular(texto(row, D_TITULAR, formatter));
            dto.setResponsablePreevaluacion(texto(row, D_RESPONSABLE_PREEVALUACION, formatter));
            dto.setFechaAsignado(leerFecha(row, D_FECHA_ASIGNADO));
            dto.setNumeroHojaEnvioAsignacion(texto(row, D_N_DOCUMENTO_HOJA_ENVIO, formatter));
            dto.setNumeroExpedienteDigital(texto(row, D_N_EXPEDIENTE_DIGITAL, formatter));
            dto.setAsignadoAAbogado(texto(row, D_ASIGNADO_A_ABOGADO, formatter));
            dto.setFechaEmisionAnalisis(leerFecha(row, D_EJEC_FECHA_EMISION));
            dto.setResultadoAnalisisTexto(texto(row, D_EJEC_TIPO_CALIFICACION, formatter));
            dto.setFechaEjecucion(leerFecha(row, D_EJEC_FECHA_EJECUCION));
            dto.setEstadoEjecucionTexto(texto(row, D_EJEC_ESTADO, formatter));
            dto.setNumeroResolucion(texto(row, D_N_RESOLUCION, formatter));
            dto.setResolucionLimpia(texto(row, D_RESOLUCION_LIMPIA, formatter));
            dto.setProcedimientoEjecuciones(texto(row, D_EJEC_PROCEDIMIENTO, formatter));
            dto.setAsignadoNotificacion(texto(row, D_EJEC_ASIGNADO_NOTIF, formatter));
            dto.setMedioNotificacion(texto(row, D_EJEC_MEDIO_NOTIFICACION, formatter));
            dto.setFechaEmision1Carta(leerFecha(row, D_EJEC_FECHA_EMISION_1_CARTA));
            dto.setNumero1Carta(texto(row, D_EJEC_NUMERO_1_CARTA, formatter));
            dto.setFechaElaboracion2Carta(leerFecha(row, D_EJEC_FECHA_ELABORACION_2_CARTA));
            dto.setNumero2Carta(texto(row, D_EJEC_NUMERO_2_CARTA, formatter));
            filas.add(dto);
        }
    }

    private void parsearHojaPasivoOr(Sheet sheet, DataFormatter formatter, List<CargaInicialFilaDTO> filas) {
        for (int r = 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null || esFilaVacia(row, P_TITULAR)) {
                continue;
            }
            CargaInicialFilaDTO dto = new CargaInicialFilaDTO();
            dto.setHojaOrigen(CargaInicialFilaDTO.HOJA_PASIVO_OR);
            dto.setFilaExcel(r + 1);
            dto.setFechaSolicitud(leerFecha(row, P_FECHA_RECEPCION_SDRERC));
            dto.setTipoTramite(texto(row, P_TIPO_TRAMITE, formatter));
            dto.setDerivadoPor(texto(row, P_DERIVADO_POR, formatter));
            dto.setNumeroExpedienteLegado(texto(row, P_N_EXPEDIENTE_LEGADO, formatter));
            dto.setTipoDocumento(texto(row, P_TIPO_DOCUMENTO, formatter));
            dto.setNumeroDocumento(texto(row, P_N_DOCUMENTO, formatter));
            dto.setTipoActa(texto(row, P_TIPO_ACTA, formatter));
            dto.setNumeroActa(texto(row, P_N_ACTA, formatter));
            dto.setTitular(texto(row, P_TITULAR, formatter));
            dto.setFechaAsignado(leerFecha(row, P_FECHA_ASIGNADO));
            dto.setNumeroHojaEnvioAsignacion(texto(row, P_N_DOCUMENTO_HOJA_ENVIO, formatter));
            dto.setNumeroExpedienteDigital(texto(row, P_N_EXPEDIENTE_DIGITAL, formatter));
            dto.setAsignadoAAbogado(texto(row, P_ASIGNADO_A_ABOGADO, formatter));
            dto.setFechaEmisionAnalisis(leerFecha(row, P_EJEC_FECHA_EMISION));
            dto.setResultadoAnalisisTexto(texto(row, P_EJEC_TIPO_CALIFICACION, formatter));
            dto.setFechaEjecucion(leerFecha(row, P_EJEC_FECHA_EJECUCION));
            dto.setEstadoEjecucionTexto(texto(row, P_EJEC_ESTADO, formatter));
            dto.setNumeroResolucion(texto(row, P_N_RESOLUCION, formatter));
            dto.setResolucionLimpia(texto(row, P_RESOLUCION_LIMPIA, formatter));
            dto.setProcedimientoEjecuciones(texto(row, P_EJEC_PROCEDIMIENTO, formatter));
            dto.setAsignadoNotificacion(texto(row, P_EJEC_ASIGNADO_NOTIF, formatter));
            dto.setMedioNotificacion(texto(row, P_EJEC_MEDIO_NOTIFICACION, formatter));
            dto.setFechaEmision1Carta(leerFecha(row, P_EJEC_FECHA_EMISION_1_CARTA));
            dto.setNumero1Carta(texto(row, P_EJEC_NUMERO_1_CARTA, formatter));
            dto.setFechaElaboracion2Carta(leerFecha(row, P_EJEC_FECHA_ELABORACION_2_CARTA));
            dto.setNumero2Carta(texto(row, P_EJEC_NUMERO_2_CARTA, formatter));
            filas.add(dto);
        }
    }

    private boolean esFilaVacia(Row row, int columnaClave) {
        Cell cell = row.getCell(columnaClave);
        return cell == null || cell.getCellType() == CellType.BLANK;
    }

    private String texto(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }

    private LocalDate leerFecha(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null || cell.getCellType() != CellType.NUMERIC) {
            return null;
        }
        try {
            return DateUtil.getJavaDate(cell.getNumericCellValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
