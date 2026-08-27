package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.PlantillaDocumentoAdminDTO;
import com.sdrerc.v3.domain.PlantillaVariableDTO;
import com.sdrerc.v3.infrastructure.dao.PlantillaDocumentoAdminDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Port de {@code com.sdrerc.application.sdrercapp.PlantillaDocumentoService} (V2), Administración
 * &gt; Plantillas de documento. Reglas portadas literales: solo {@code .docx}, tamaño máximo 15 MB.
 */
@Service
public class PlantillaDocumentoAdminService {

    private static final long TAMANO_MAXIMO_BYTES = 15L * 1024 * 1024;

    private final PlantillaDocumentoAdminDAO plantillaDocumentoAdminDAO;
    private final AnalisisPlantillaDocumentoService analisisPlantillaDocumentoService;

    public PlantillaDocumentoAdminService(PlantillaDocumentoAdminDAO plantillaDocumentoAdminDAO,
            AnalisisPlantillaDocumentoService analisisPlantillaDocumentoService) {
        this.plantillaDocumentoAdminDAO = plantillaDocumentoAdminDAO;
        this.analisisPlantillaDocumentoService = analisisPlantillaDocumentoService;
    }

    public List<PlantillaDocumentoAdminDTO> listarTiposConPlantilla() throws SQLException {
        return plantillaDocumentoAdminDAO.listarTiposConPlantilla();
    }

    public List<PlantillaDocumentoAdminDTO> listarHistorial(Long idTipoDocumentoAdjunto) throws SQLException {
        if (idTipoDocumentoAdjunto == null) {
            throw new IllegalArgumentException("Seleccione un tipo de documento.");
        }
        return plantillaDocumentoAdminDAO.listarHistorial(idTipoDocumentoAdjunto);
    }

    public PlantillaDocumentoAdminDAO.ContenidoPlantilla descargarContenido(Long idPlantillaDocumento) throws SQLException {
        if (idPlantillaDocumento == null) {
            throw new IllegalArgumentException("Seleccione una versión de plantilla.");
        }
        return plantillaDocumentoAdminDAO.obtenerContenido(idPlantillaDocumento);
    }

    public Long cargarNuevaVersion(Long idTipoDocumentoAdjunto, String nombreArchivo, byte[] contenido, String comentario)
            throws SQLException {
        if (idTipoDocumentoAdjunto == null) {
            throw new IllegalArgumentException("Seleccione el tipo de documento para el que se carga la plantilla.");
        }
        validarArchivo(nombreArchivo, contenido);
        return plantillaDocumentoAdminDAO.insertarVersion(
                idTipoDocumentoAdjunto, nombreArchivo, contenido, comentario, resolverIdUsuarioActual());
    }

    public void activarVersion(Long idPlantillaDocumento) throws SQLException {
        if (idPlantillaDocumento == null) {
            throw new IllegalArgumentException("Seleccione la versión de plantilla a activar.");
        }
        plantillaDocumentoAdminDAO.activarVersion(idPlantillaDocumento, resolverIdUsuarioActual());
    }

    /** "Generar documento de prueba" — ver Javadoc de {@link AnalisisPlantillaDocumentoService#generarDocumentoPrueba}. */
    public byte[] generarDocumentoPrueba(Long idTipoDocumentoAdjunto) throws SQLException, IOException {
        if (idTipoDocumentoAdjunto == null) {
            throw new IllegalArgumentException("Seleccione un tipo de documento.");
        }
        PlantillaDocumentoAdminDTO tipo = listarTiposConPlantilla().stream()
                .filter(t -> idTipoDocumentoAdjunto.equals(t.getIdTipoDocumentoAdjunto()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El tipo de documento seleccionado no existe."));
        return analisisPlantillaDocumentoService.generarDocumentoPrueba(tipo.getTipoDocumentoCodigo(), tipo.getTipoDocumentoNombre());
    }

    /**
     * Catálogo de referencia de variables — mejora explícita sobre V2 (bloque de texto fijo en un
     * diálogo), ver Javadoc de {@link PlantillaVariableDTO}. Mismas claves exactas que
     * {@code AnalisisPlantillaDocumentoService#valores(...)} resuelve hoy.
     */
    public List<PlantillaVariableDTO> listarVariablesDisponibles() {
        return List.of(
                new PlantillaVariableDTO("NUMERO_EXPEDIENTE", "Número de expediente SDRERC (ej. SDRERC-EXP-2026-000123)", "Expediente"),
                new PlantillaVariableDTO("NUMERO_EXPEDIENTE_SGD", "Número de expediente SGD", "Expediente"),
                new PlantillaVariableDTO("TRAMITE_WEB", "Número de trámite web (Mesa de partes virtual)", "Expediente"),
                new PlantillaVariableDTO("nroTramiteWeb", "Alias camelCase de TRAMITE_WEB", "Expediente"),
                new PlantillaVariableDTO("PROCEDIMIENTO_REGISTRAL", "Nombre del procedimiento registral", "Expediente"),
                new PlantillaVariableDTO("tipoProcedimiento", "Alias camelCase de PROCEDIMIENTO_REGISTRAL", "Expediente"),
                new PlantillaVariableDTO("FECHA_SOLICITUD", "Fecha de recepción de la solicitud (dd/MM/yyyy)", "Expediente"),
                new PlantillaVariableDTO("fechaSolicitud", "Alias camelCase de FECHA_SOLICITUD", "Expediente"),
                new PlantillaVariableDTO("canalRecepcion", "Canal de ingreso (Mesa de partes virtual, presencial, etc.)", "Expediente"),
                new PlantillaVariableDTO("fechaActual", "Fecha de hoy (dd/MM/yyyy), al momento de generar el documento", "Expediente"),
                new PlantillaVariableDTO("TITULAR", "Nombre completo del titular del acta", "Titular"),
                new PlantillaVariableDTO("nomTitular", "Alias camelCase de TITULAR", "Titular"),
                new PlantillaVariableDTO("DOCUMENTO_IDENTIDAD_TITULAR", "N° de documento de identidad del titular", "Titular"),
                new PlantillaVariableDTO("dniTitular", "Alias camelCase de DOCUMENTO_IDENTIDAD_TITULAR", "Titular"),
                new PlantillaVariableDTO("TIPO_ACTA", "Nombre del tipo de acta (nacimiento, matrimonio, defunción)", "Titular"),
                new PlantillaVariableDTO("tipoActa", "Alias camelCase de TIPO_ACTA", "Titular"),
                new PlantillaVariableDTO("NUMERO_ACTA", "Número de acta registral", "Titular"),
                new PlantillaVariableDTO("nroActa", "Alias camelCase de NUMERO_ACTA", "Titular"),
                new PlantillaVariableDTO("SOLICITANTE", "Nombre completo del solicitante", "Solicitante"),
                new PlantillaVariableDTO("nomSolicitante", "Alias camelCase de SOLICITANTE", "Solicitante"),
                new PlantillaVariableDTO("DOCUMENTO_IDENTIDAD_SOLICITANTE", "N° de documento de identidad del solicitante", "Solicitante"),
                new PlantillaVariableDTO("dniSolicitante", "Alias camelCase de DOCUMENTO_IDENTIDAD_SOLICITANTE", "Solicitante"),
                new PlantillaVariableDTO("direccion", "Dirección del solicitante", "Solicitante"),
                new PlantillaVariableDTO("correo", "Correo electrónico del solicitante", "Solicitante"),
                new PlantillaVariableDTO("TIPO_DOCUMENTO", "Tipo del documento que se está generando (carta, informe, resolución)", "Documento"),
                new PlantillaVariableDTO("ESTADO_DOCUMENTO", "Estado del documento (En proyecto, En despacho, Emitido...)", "Documento"),
                new PlantillaVariableDTO("FECHA_DOCUMENTO", "Fecha de emisión del documento (dd/MM/yyyy)", "Documento"),
                new PlantillaVariableDTO("NUMERO_DOCUMENTO", "Número del documento emitido", "Documento"),
                new PlantillaVariableDTO("DESCRIPCION_DOCUMENTO", "Comentario/descripción del documento", "Documento"),
                new PlantillaVariableDTO("REQUIERE_RESPUESTA", "\"Sí\"/\"No\" — si el documento requiere respuesta del ciudadano", "Documento"),
                new PlantillaVariableDTO("FECHA_ACUSE", "Fecha de acuse de recepción (dd/MM/yyyy)", "Documento"),
                new PlantillaVariableDTO("CONFIRMACION_RESPUESTA", "Confirmación de respuesta del ciudadano (SI/NO/PENDIENTE)", "Documento"),
                new PlantillaVariableDTO("FECHA_RESPUESTA", "Fecha de respuesta del ciudadano (dd/MM/yyyy)", "Documento"),
                new PlantillaVariableDTO("HOJA_ENVIO", "Número de hoja de envío de la respuesta", "Documento"),
                new PlantillaVariableDTO("NOTIFICADO", "\"Sí\"/\"No\" — si el documento ya fue notificado", "Documento"),
                new PlantillaVariableDTO("resAnalisis", "Resultado del análisis (Procedente, Improcedente, ...)", "Análisis"));
    }

    private void validarArchivo(String nombreArchivo, byte[] contenido) {
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El archivo de plantilla debe tener un nombre.");
        }
        if (!nombreArchivo.toLowerCase(Locale.ROOT).endsWith(".docx")) {
            throw new IllegalArgumentException("Solo se admiten plantillas en formato .docx.");
        }
        if (contenido == null || contenido.length == 0) {
            throw new IllegalArgumentException("El archivo de plantilla está vacío.");
        }
        if (contenido.length > TAMANO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("El archivo de plantilla supera el tamaño máximo permitido (15 MB).");
        }
    }

    private Long resolverIdUsuarioActual() {
        return CurrentUser.get().idUsuario();
    }
}
