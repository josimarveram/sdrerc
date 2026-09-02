package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.DatosActaDTO;
import com.sdrerc.v3.domain.DatosPersonaRegistroDTO;
import com.sdrerc.v3.domain.DatosSolicitudDTO;
import com.sdrerc.v3.domain.DocumentoAnalizadoDTO;
import com.sdrerc.v3.domain.ExpedienteEdicionManualDTO;
import com.sdrerc.v3.domain.PlantillaBloqueAdminDTO;
import com.sdrerc.v3.infrastructure.dao.PlantillaBloqueAdminDAO;
import com.sdrerc.v3.infrastructure.dao.PlantillaDocumentoAdminDAO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;
import org.springframework.stereotype.Service;

/**
 * Port de {@code com.sdrerc.application.sdrercapp.AnalisisPlantillaDocumentoService} (V2) —
 * generación de Word desde plantilla para un documento analizado. Completa el alcance
 * deliberadamente diferido en el primer incremento (20/08/2026): ahora SÍ prioriza la BLOB de
 * {@code PLANTILLA_DOCUMENTO} (Administración &gt; Plantillas de documento) sobre el archivo de
 * {@code docs/plantillas}, y aplica condicionales {@code [[SI_variable:valor]]...[[FIN_SI]]} y
 * bloques de contenido {@code [[CONTENIDO]]}/{@code [[CONTENIDO:seccion]]} — mismo workaround real
 * de V2 para el {@code ClassCastException} de {@code XWPFDocument.insertNewParagraph} en POI 5.2.5
 * (crear párrafos al final con {@code createParagraph()} + reubicar con {@code XmlCursor.moveXml} +
 * round-trip de serializar/reabrir, ver comentarios en {@link #generarDesdeStream}).
 *
 * <p>Firma pública de {@link #generarDocumento} sin cambios respecto al primer incremento — los
 * llamadores (Análisis, Ejecución) no necesitan tocarse; ambos empiezan a beneficiarse de las
 * plantillas BLOB/bloques/condicionales de forma transparente.</p>
 *
 * <p>Sigue sin portar los otros 3 formatos legado de V2 ({@code {{}}}/{@code ${}}/{@code []}) —
 * decisión ya tomada en el primer incremento, sin uso real en las plantillas vigentes.</p>
 */
@Service
public class AnalisisPlantillaDocumentoService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String DOCX_EXTENSION = ".docx";
    private static final Pattern PATRON_SI = Pattern.compile("\\[\\[SI_([A-Za-z0-9_]+):([^\\]]*)\\]\\]", Pattern.CASE_INSENSITIVE);
    private static final String MARCADOR_FIN_SI = "[[FIN_SI]]";
    private static final Pattern PATRON_MARCADOR_CONTENIDO =
            Pattern.compile("\\[\\[CONTENIDO(?::([A-Za-z0-9_]+))?\\]\\]", Pattern.CASE_INSENSITIVE);

    private final PlantillaDocumentoAdminDAO plantillaDocumentoAdminDAO;
    private final PlantillaBloqueAdminDAO plantillaBloqueAdminDAO;

    public AnalisisPlantillaDocumentoService(PlantillaDocumentoAdminDAO plantillaDocumentoAdminDAO,
            PlantillaBloqueAdminDAO plantillaBloqueAdminDAO) {
        this.plantillaDocumentoAdminDAO = plantillaDocumentoAdminDAO;
        this.plantillaBloqueAdminDAO = plantillaBloqueAdminDAO;
    }

    public byte[] generarDocumento(String tipoDocumentoCodigo, String tipoDocumentoNombre, Map<String, String> valores)
            throws IOException {
        List<PlantillaBloqueAdminDTO> bloques = obtenerBloques(tipoDocumentoCodigo);
        PlantillaDocumentoAdminDAO.ContenidoPlantilla plantillaDb = obtenerPlantillaDb(tipoDocumentoCodigo);
        try (InputStream in = plantillaDb != null
                ? new ByteArrayInputStream(plantillaDb.contenido())
                : Files.newInputStream(resolverPlantilla(tipoDocumentoCodigo, tipoDocumentoNombre))) {
            return generarDesdeStream(in, valores, bloques);
        }
    }

    /**
     * "Generar documento de prueba" (Administración &gt; Plantillas de documento, mejora
     * explícita de V3 sin equivalente en V2): rellena la plantilla con datos ficticios
     * representativos, para que el administrador vea de inmediato si el Word se ve bien —
     * variables mal escritas, marcador {@code [[CONTENIDO]]} faltante, condicionales rotos — sin
     * necesitar un expediente real ni pasar por Análisis/Ejecución.
     */
    public byte[] generarDocumentoPrueba(String tipoDocumentoCodigo, String tipoDocumentoNombre) throws IOException {
        return generarDocumento(tipoDocumentoCodigo, tipoDocumentoNombre, valoresDePrueba());
    }

    public String nombreSugerido(String numeroExpediente, String tipoDocumentoNombre) {
        return normalizarArchivo(tipoDocumentoNombre) + "_" + normalizarArchivo(numeroExpediente) + "_" + LocalDate.now() + DOCX_EXTENSION;
    }

    /** Mismo set de variables {@code #VARIABLE#} (mayúsculas, nombres de V2) y sus alias camelCase ya usados en las plantillas reales. */
    public Map<String, String> valores(ExpedienteEdicionManualDTO expediente, DocumentoAnalizadoDTO documento, String resultadoAnalisisNombre) {
        DatosSolicitudDTO solicitud = expediente == null ? null : expediente.getSolicitud();
        DatosActaDTO acta = expediente == null ? null : expediente.getActa();
        DatosPersonaRegistroDTO titular = expediente == null ? null : expediente.getTitular();
        DatosPersonaRegistroDTO remitente = expediente == null ? null : expediente.getRemitente();

        Map<String, String> values = new LinkedHashMap<>();
        values.put("NUMERO_EXPEDIENTE", valor(expediente == null ? null : expediente.getNumeroExpediente()));
        values.put("NUMERO_EXPEDIENTE_SGD", valor(solicitud == null ? null : solicitud.getNumeroExpedienteSgd()));
        values.put("TRAMITE_WEB", valor(solicitud == null ? null : solicitud.getNumeroTramite()));
        values.put("TITULAR", valor(titular == null ? null : titular.getNombreCompleto()));
        values.put("DOCUMENTO_IDENTIDAD_TITULAR", valor(titular == null ? null : titular.getNumeroDocumento()));
        values.put("SOLICITANTE", valor(remitente == null ? null : remitente.getNombreCompleto()));
        values.put("DOCUMENTO_IDENTIDAD_SOLICITANTE", valor(remitente == null ? null : remitente.getNumeroDocumento()));
        values.put("TIPO_ACTA", valor(acta == null ? null : acta.getTipoActaNombre()));
        values.put("NUMERO_ACTA", valor(acta == null ? null : acta.getNumeroActa()));
        values.put("PROCEDIMIENTO_REGISTRAL", valor(solicitud == null ? null : solicitud.getTipoProcedimientoNombre()));
        values.put("FECHA_SOLICITUD", fecha(solicitud == null ? null : solicitud.getFechaRecepcion()));
        values.put("TIPO_DOCUMENTO_SOLICITUD", valor(solicitud == null ? null : solicitud.getTipoDocumentoNombre()));
        values.put("NUMERO_DOCUMENTO_SOLICITUD", valor(solicitud == null ? null : solicitud.getNumeroDocumento()));
        values.put("TIPO_DOCUMENTO", valor(documento == null ? null : documento.getTipoDocumentoNombre()));
        values.put("ESTADO_DOCUMENTO", valor(documento == null ? null : documento.getEstadoDocumentoNombre()));
        values.put("FECHA_DOCUMENTO", fecha(documento == null ? null : documento.getFechaDocumento()));
        values.put("NUMERO_DOCUMENTO", valor(documento == null ? null : documento.getNumeroDocumento()));
        values.put("DESCRIPCION_DOCUMENTO", valor(documento == null ? null : documento.getDescripcion()));
        values.put("REQUIERE_RESPUESTA", documento != null && documento.isRequiereRespuesta() ? "Sí" : "No");
        values.put("FECHA_ACUSE", fecha(documento == null ? null : documento.getFechaAcuse()));
        values.put("CONFIRMACION_RESPUESTA", valor(documento == null ? null : documento.getConfirmacionRespuesta()));
        values.put("FECHA_RESPUESTA", fecha(documento == null ? null : documento.getFechaRespuesta()));
        values.put("HOJA_ENVIO", valor(documento == null ? null : documento.getNumeroHojaEnvioRespuesta()));
        values.put("NOTIFICADO", documento != null && documento.isNotificado() ? "Sí" : "No");
        values.put("nomTitular", values.get("TITULAR"));
        values.put("dniTitular", values.get("DOCUMENTO_IDENTIDAD_TITULAR"));
        values.put("nomSolicitante", values.get("SOLICITANTE"));
        values.put("dniSolicitante", values.get("DOCUMENTO_IDENTIDAD_SOLICITANTE"));
        values.put("tipoActa", values.get("TIPO_ACTA"));
        values.put("nroActa", values.get("NUMERO_ACTA"));
        values.put("direccion", valor(remitente == null ? null : remitente.getDireccion()));
        values.put("correo", valor(remitente == null ? null : remitente.getCorreo()));
        values.put("tipoProcedimiento", values.get("PROCEDIMIENTO_REGISTRAL"));
        values.put("fechaSolicitud", values.get("FECHA_SOLICITUD"));
        values.put("nroTramiteWeb", values.get("TRAMITE_WEB"));
        values.put("tipoDocumentoSolicitud", values.get("TIPO_DOCUMENTO_SOLICITUD"));
        values.put("numeroDocumentoSolicitud", values.get("NUMERO_DOCUMENTO_SOLICITUD"));
        values.put("canalRecepcion", valor(solicitud == null ? null : solicitud.getCanalNombre()));
        values.put("fechaActual", fecha(LocalDate.now()));
        values.put("resAnalisis", valor(resultadoAnalisisNombre));
        return values;
    }

    /** Datos ficticios representativos para "Generar documento de prueba" — mismas claves que {@link #valores}. */
    private Map<String, String> valoresDePrueba() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("NUMERO_EXPEDIENTE", "SDRERC-EXP-2026-000000");
        values.put("NUMERO_EXPEDIENTE_SGD", "SGD-SOL-2026-00000");
        values.put("TRAMITE_WEB", "TW-000000");
        values.put("TITULAR", "JUAN CARLOS PÉREZ GÓMEZ");
        values.put("DOCUMENTO_IDENTIDAD_TITULAR", "12345678");
        values.put("SOLICITANTE", "MARÍA LUISA GÓMEZ TORRES");
        values.put("DOCUMENTO_IDENTIDAD_SOLICITANTE", "87654321");
        values.put("TIPO_ACTA", "ACTA DE NACIMIENTO");
        values.put("NUMERO_ACTA", "1234-2020");
        values.put("PROCEDIMIENTO_REGISTRAL", "RECTIFICACIÓN ADMINISTRATIVA");
        values.put("FECHA_SOLICITUD", fecha(LocalDate.now().minusDays(20)));
        values.put("TIPO_DOCUMENTO_SOLICITUD", "COPIA DE DNI");
        values.put("NUMERO_DOCUMENTO_SOLICITUD", "12345678");
        values.put("TIPO_DOCUMENTO", "INFORME");
        values.put("ESTADO_DOCUMENTO", "EN PROYECTO");
        values.put("FECHA_DOCUMENTO", fecha(LocalDate.now()));
        values.put("NUMERO_DOCUMENTO", "0001-2026");
        values.put("DESCRIPCION_DOCUMENTO", "Documento de prueba generado desde Administración de plantillas.");
        values.put("REQUIERE_RESPUESTA", "No");
        values.put("FECHA_ACUSE", "");
        values.put("CONFIRMACION_RESPUESTA", "");
        values.put("FECHA_RESPUESTA", "");
        values.put("HOJA_ENVIO", "HE-000000");
        values.put("NOTIFICADO", "No");
        values.put("nomTitular", values.get("TITULAR"));
        values.put("dniTitular", values.get("DOCUMENTO_IDENTIDAD_TITULAR"));
        values.put("nomSolicitante", values.get("SOLICITANTE"));
        values.put("dniSolicitante", values.get("DOCUMENTO_IDENTIDAD_SOLICITANTE"));
        values.put("tipoActa", values.get("TIPO_ACTA"));
        values.put("nroActa", values.get("NUMERO_ACTA"));
        values.put("direccion", "AV. EJEMPLO 123, LIMA");
        values.put("correo", "correo.prueba@ejemplo.com");
        values.put("tipoProcedimiento", values.get("PROCEDIMIENTO_REGISTRAL"));
        values.put("fechaSolicitud", values.get("FECHA_SOLICITUD"));
        values.put("nroTramiteWeb", values.get("TRAMITE_WEB"));
        values.put("tipoDocumentoSolicitud", values.get("TIPO_DOCUMENTO_SOLICITUD"));
        values.put("numeroDocumentoSolicitud", values.get("NUMERO_DOCUMENTO_SOLICITUD"));
        values.put("canalRecepcion", "MESA DE PARTES VIRTUAL");
        values.put("fechaActual", fecha(LocalDate.now()));
        values.put("resAnalisis", "PROCEDENTE");
        return values;
    }

    private List<PlantillaBloqueAdminDTO> obtenerBloques(String tipoDocumentoCodigo) {
        try {
            return plantillaBloqueAdminDAO.listarPorCodigoTipo(tipoDocumentoCodigo);
        } catch (SQLException ex) {
            return new ArrayList<>();
        }
    }

    private PlantillaDocumentoAdminDAO.ContenidoPlantilla obtenerPlantillaDb(String tipoDocumentoCodigo) {
        try {
            return plantillaDocumentoAdminDAO.obtenerActivaPorCodigoTipo(tipoDocumentoCodigo);
        } catch (SQLException ex) {
            return null;
        }
    }

    public Path resolverPlantilla(String tipoDocumentoCodigo, String tipoDocumentoNombre) throws IOException {
        Path base = rutaBasePlantillas();
        if (!Files.isDirectory(base)) {
            throw new IOException("No se encontró la carpeta de plantillas: " + base.toAbsolutePath());
        }
        String objetivoNombre = normalizarClave(tipoDocumentoNombre);
        String objetivoCodigo = normalizarClave(limpiarCodigo(tipoDocumentoCodigo));
        Path[] mejor = new Path[1];
        int[] mejorPuntaje = {-1};

        Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String fileName = file.getFileName().toString();
                String lower = fileName.toLowerCase(Locale.ROOT);
                if (!lower.endsWith(".docx") && !lower.endsWith(".doc")) {
                    return FileVisitResult.CONTINUE;
                }
                String baseName = normalizarClave(removerExtension(fileName));
                int puntaje = puntajeCoincidencia(baseName, objetivoNombre, objetivoCodigo, lower.endsWith(DOCX_EXTENSION));
                if (puntaje > mejorPuntaje[0]) {
                    mejorPuntaje[0] = puntaje;
                    mejor[0] = file;
                }
                return FileVisitResult.CONTINUE;
            }
        });

        if (mejor[0] == null || mejorPuntaje[0] <= 0) {
            throw new IOException("No se encontró plantilla Word para el tipo de documento: " + valor(tipoDocumentoNombre));
        }
        return mejor[0];
    }

    /**
     * Round-trip obligatorio: {@link #insertarBloques} reubica párrafos con
     * {@code XmlCursor.moveXml} (operación cruda de XMLBeans); la lista de párrafos que POI cachea
     * en memoria ({@code document.getParagraphs()}) no queda sincronizada con ese movimiento y
     * produce {@code XmlValueDisconnectedException} si se sigue usando el mismo objeto
     * {@code XWPFDocument}. Se serializa a un buffer intermedio y se vuelve a abrir para que POI
     * reconstruya su modelo en memoria a partir del XML ya reordenado, antes de continuar con la
     * sustitución de variables y la eliminación de los marcadores.
     */
    private byte[] generarDesdeStream(InputStream in, Map<String, String> valores, List<PlantillaBloqueAdminDTO> bloques)
            throws IOException {
        byte[] intermedio;
        try (XWPFDocument document = new XWPFDocument(in)) {
            aplicarCondicionales(document, valores);
            insertarBloques(document, bloques, valores);
            ByteArrayOutputStream bufferIntermedio = new ByteArrayOutputStream();
            document.write(bufferIntermedio);
            intermedio = bufferIntermedio.toByteArray();
        }
        try (InputStream inIntermedio = new ByteArrayInputStream(intermedio);
                XWPFDocument document = new XWPFDocument(inIntermedio);
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            eliminarMarcadorContenido(document);
            reemplazarParrafos(document.getParagraphs(), valores);
            for (XWPFTable table : document.getTables()) {
                reemplazarTabla(table, valores);
            }
            for (XWPFHeader header : document.getHeaderList()) {
                reemplazarParrafos(header.getParagraphs(), valores);
                for (XWPFTable table : header.getTables()) {
                    reemplazarTabla(table, valores);
                }
            }
            for (XWPFFooter footer : document.getFooterList()) {
                reemplazarParrafos(footer.getParagraphs(), valores);
                for (XWPFTable table : footer.getTables()) {
                    reemplazarTabla(table, valores);
                }
            }
            document.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Condicionales dentro de la propia plantilla: {@code [[SI_canalRecepcion:MPV]] ... [[FIN_SI]]}.
     * El nombre de variable se compara sin distinguir mayúsculas/guiones bajos; los valores
     * esperados van separados por {@code |}. Los párrafos marcador siempre se eliminan; el
     * contenido intermedio se conserva solo si el valor actual de la variable coincide.
     */
    private void aplicarCondicionales(XWPFDocument document, Map<String, String> valores) {
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        List<Integer> aEliminar = new ArrayList<>();
        int i = 0;
        while (i < paragraphs.size()) {
            String texto = paragraphs.get(i).getText() == null ? "" : paragraphs.get(i).getText().trim();
            Matcher matcher = PATRON_SI.matcher(texto);
            if (matcher.matches()) {
                int fin = buscarFinSi(paragraphs, i + 1);
                if (fin < 0) {
                    i++;
                    continue;
                }
                boolean coincide = coincideCriterio(matcher.group(1), matcher.group(2), valores);
                aEliminar.add(i);
                if (!coincide) {
                    for (int j = i + 1; j < fin; j++) {
                        aEliminar.add(j);
                    }
                }
                aEliminar.add(fin);
                i = fin + 1;
                continue;
            }
            i++;
        }
        for (int idx = aEliminar.size() - 1; idx >= 0; idx--) {
            int pos = document.getPosOfParagraph(paragraphs.get(aEliminar.get(idx)));
            if (pos >= 0) {
                document.removeBodyElement(pos);
            }
        }
    }

    private int buscarFinSi(List<XWPFParagraph> paragraphs, int desde) {
        for (int k = desde; k < paragraphs.size(); k++) {
            String texto = paragraphs.get(k).getText() == null ? "" : paragraphs.get(k).getText().trim();
            if (MARCADOR_FIN_SI.equalsIgnoreCase(texto)) {
                return k;
            }
        }
        return -1;
    }

    /**
     * Inserta, en orden, los bloques cuya condición se cumple en el punto de la plantilla base
     * marcado con {@code [[CONTENIDO]]} o, si el bloque tiene sección, en el marcador nombrado
     * {@code [[CONTENIDO:seccion]]} correspondiente — ver Javadoc de {@link #generarDesdeStream}
     * para el porqué del workaround {@code createParagraph()}+{@code XmlCursor.moveXml} en vez de
     * {@code insertNewParagraph} (bug real de POI 5.2.5).
     */
    private void insertarBloques(XWPFDocument document, List<PlantillaBloqueAdminDTO> bloques, Map<String, String> valores) {
        if (bloques == null || bloques.isEmpty()) {
            return;
        }
        Map<String, List<PlantillaBloqueAdminDTO>> bloquesPorSeccion = new LinkedHashMap<>();
        for (PlantillaBloqueAdminDTO bloque : bloques) {
            String clave = bloque.tieneSeccion() ? normalizarNombreVariable(bloque.getSeccion()) : "";
            bloquesPorSeccion.computeIfAbsent(clave, k -> new ArrayList<>()).add(bloque);
        }
        for (XWPFParagraph marcador : buscarParrafosMarcador(document)) {
            String seccion = extraerSeccionMarcador(marcador.getText());
            List<PlantillaBloqueAdminDTO> bloquesSeccion = bloquesPorSeccion.get(seccion);
            if (bloquesSeccion == null || bloquesSeccion.isEmpty()) {
                continue;
            }
            // Mismo estilo de párrafo que el propio marcador (normalmente el estilo de cuerpo de
            // texto de la plantilla, ej. "Default") — así el título/contenido insertados usan la
            // misma tipografía/tamaño que el resto del Word en vez del default de Word ("Normal",
            // Calibri) que resultaría de un párrafo sin estilo asignado. `getStyle()` puede devolver
            // `null` si el marcador tampoco tenía estilo explícito; en ese caso se deja sin asignar,
            // mismo comportamiento que antes de este ajuste.
            String estiloReferencia = marcador.getStyle();
            List<XWPFParagraph> nuevosParrafos = new ArrayList<>();
            for (PlantillaBloqueAdminDTO bloque : bloquesSeccion) {
                if (!coincideCondicionBloque(bloque, valores)) {
                    continue;
                }
                if (hasText(bloque.getTitulo())) {
                    XWPFParagraph pTitulo = document.createParagraph();
                    if (estiloReferencia != null) {
                        pTitulo.setStyle(estiloReferencia);
                    }
                    XWPFRun runTitulo = pTitulo.createRun();
                    runTitulo.setBold(true);
                    runTitulo.setText(bloque.getTitulo());
                    nuevosParrafos.add(pTitulo);
                }
                if (hasText(bloque.getContenido())) {
                    XWPFParagraph pContenido = document.createParagraph();
                    if (estiloReferencia != null) {
                        pContenido.setStyle(estiloReferencia);
                    }
                    pContenido.createRun().setText(bloque.getContenido());
                    nuevosParrafos.add(pContenido);
                }
            }
            for (XWPFParagraph nuevo : nuevosParrafos) {
                XmlCursor cursorNuevo = nuevo.getCTP().newCursor();
                XmlCursor cursorMarcador = marcador.getCTP().newCursor();
                cursorNuevo.moveXml(cursorMarcador);
                cursorNuevo.dispose();
                cursorMarcador.dispose();
            }
        }
        // Los párrafos marcador NO se eliminan acá — ver Javadoc de generarDesdeStream: tras
        // moveXml la lista de párrafos en memoria queda desincronizada del XML real, la eliminación
        // se hace sobre el documento recién reabierto tras el round-trip.
    }

    private void eliminarMarcadorContenido(XWPFDocument document) {
        List<XWPFParagraph> marcadores = buscarParrafosMarcador(document);
        for (int i = marcadores.size() - 1; i >= 0; i--) {
            int pos = document.getPosOfParagraph(marcadores.get(i));
            if (pos >= 0) {
                document.removeBodyElement(pos);
            }
        }
    }

    private List<XWPFParagraph> buscarParrafosMarcador(XWPFDocument document) {
        List<XWPFParagraph> marcadores = new ArrayList<>();
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            if (extraerSeccionMarcador(paragraph.getText()) != null) {
                marcadores.add(paragraph);
            }
        }
        return marcadores;
    }

    private static String extraerSeccionMarcador(String texto) {
        if (texto == null) {
            return null;
        }
        Matcher matcher = PATRON_MARCADOR_CONTENIDO.matcher(texto.trim());
        if (!matcher.matches()) {
            return null;
        }
        String seccion = matcher.group(1);
        return seccion == null ? "" : normalizarNombreVariable(seccion);
    }

    private boolean coincideCondicionBloque(PlantillaBloqueAdminDTO bloque, Map<String, String> valores) {
        if (!bloque.tieneCondicion()) {
            return true;
        }
        boolean coincide = coincideCriterio(bloque.getVariableCondicion(), bloque.getValoresCondicion(), valores);
        return PlantillaBloqueAdminDTO.OPERADOR_NO_COINCIDE.equals(bloque.getOperadorCondicion()) ? !coincide : coincide;
    }

    private boolean coincideCriterio(String variable, String valoresCriterio, Map<String, String> valores) {
        String valorActual = normalizarClave(buscarValorPorVariable(valores, variable));
        if (valorActual.isEmpty()) {
            return false;
        }
        for (String valorEsperado : valoresCriterio.split("\\|")) {
            if (normalizarClave(valorEsperado).equals(valorActual)) {
                return true;
            }
        }
        return false;
    }

    private static String buscarValorPorVariable(Map<String, String> valores, String variable) {
        String objetivo = normalizarNombreVariable(variable);
        for (Map.Entry<String, String> entry : valores.entrySet()) {
            if (normalizarNombreVariable(entry.getKey()).equals(objetivo)) {
                return entry.getValue();
            }
        }
        return "";
    }

    private static String normalizarNombreVariable(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private void reemplazarTabla(XWPFTable table, Map<String, String> valores) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                reemplazarParrafos(cell.getParagraphs(), valores);
                for (XWPFTable nested : cell.getTables()) {
                    reemplazarTabla(nested, valores);
                }
            }
        }
    }

    private void reemplazarParrafos(Iterable<XWPFParagraph> paragraphs, Map<String, String> valores) {
        for (XWPFParagraph paragraph : paragraphs) {
            String texto = paragraph.getText();
            if (texto == null || texto.isEmpty()) {
                continue;
            }
            String reemplazado = reemplazar(texto, valores);
            if (texto.equals(reemplazado)) {
                continue;
            }
            XWPFRun first = null;
            for (XWPFRun run : paragraph.getRuns()) {
                if (first == null) {
                    first = run;
                    run.setText(reemplazado, 0);
                } else {
                    run.setText("", 0);
                }
            }
        }
    }

    private String reemplazar(String texto, Map<String, String> valores) {
        String result = texto;
        for (Map.Entry<String, String> entry : valores.entrySet()) {
            result = result.replace("#" + entry.getKey() + "#", entry.getValue() == null ? "" : entry.getValue());
        }
        return result;
    }

    private Path rutaBasePlantillas() {
        return Paths.get(System.getProperty("user.dir"), "docs", "plantillas");
    }

    private static int puntajeCoincidencia(String fileBase, String nombre, String codigo, boolean docx) {
        int score = 0;
        if (!nombre.isEmpty() && fileBase.equals(nombre)) {
            score += 100;
        } else if (!nombre.isEmpty() && (fileBase.contains(nombre) || nombre.contains(fileBase))) {
            score += 50;
        }
        if (!codigo.isEmpty() && fileBase.equals(codigo)) {
            score += 90;
        } else if (!codigo.isEmpty() && (fileBase.contains(codigo) || codigo.contains(fileBase))) {
            score += 40;
        }
        if (docx) {
            score += 5;
        }
        return score;
    }

    private static String limpiarCodigo(String codigo) {
        String value = valor(codigo);
        value = value.replaceFirst("(?i)^ANALISIS_DOC_[0-9]+_", "");
        value = value.replaceFirst("(?i)^ANALISIS_", "");
        return value;
    }

    private static String removerExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index <= 0 ? fileName : fileName.substring(0, index);
    }

    private static String normalizarClave(String value) {
        return Normalizer.normalize(valor(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private static String normalizarArchivo(String value) {
        String normalized = normalizarClave(value);
        return normalized.isEmpty() ? "documento" : normalized;
    }

    private static String fecha(LocalDate value) {
        return value == null ? "" : DATE_FORMAT.format(value);
    }

    private static String valor(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
