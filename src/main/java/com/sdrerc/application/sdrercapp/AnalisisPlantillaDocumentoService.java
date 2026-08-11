package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.AnalisisExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.domain.dto.sdrercapp.PlantillaBloqueDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.PlantillaBloqueDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.PlantillaDocumentoDAO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import org.apache.xmlbeans.XmlCursor;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
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

public class AnalisisPlantillaDocumentoService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String DOCX_EXTENSION = ".docx";
    private static final Pattern PATRON_SI = Pattern.compile(
            "\\[\\[SI_([A-Za-z0-9_]+):([^\\]]*)\\]\\]", Pattern.CASE_INSENSITIVE);
    private static final String MARCADOR_FIN_SI = "[[FIN_SI]]";
    private static final Pattern PATRON_MARCADOR_CONTENIDO = Pattern.compile(
            "\\[\\[CONTENIDO(?::([A-Za-z0-9_]+))?\\]\\]", Pattern.CASE_INSENSITIVE);

    private final PlantillaDocumentoDAO plantillaDocumentoDAO;
    private final PlantillaBloqueDAO plantillaBloqueDAO;

    public AnalisisPlantillaDocumentoService() {
        this(new PlantillaDocumentoDAO(), new PlantillaBloqueDAO());
    }

    public AnalisisPlantillaDocumentoService(PlantillaDocumentoDAO plantillaDocumentoDAO, PlantillaBloqueDAO plantillaBloqueDAO) {
        this.plantillaDocumentoDAO = plantillaDocumentoDAO;
        this.plantillaBloqueDAO = plantillaBloqueDAO;
    }

    public Path generarDocumento(
            AnalisisExpedienteDTO expediente,
            DocumentoAnalizadoDTO documento,
            List<DocumentoAnalizadoDTO> documentosExpediente,
            Path destino) throws IOException {
        Files.createDirectories(destino.toAbsolutePath().getParent());
        Map<String, String> valores = valores(expediente, documento, informeMasReciente(documentosExpediente));
        List<PlantillaBloqueDTO> bloques = obtenerBloques(documento);
        PlantillaDocumentoDAO.ContenidoPlantilla plantillaDb = obtenerPlantillaDb(documento);
        if (plantillaDb != null) {
            generarDocxDesdeBytes(plantillaDb.getContenido(), destino, valores, bloques);
            return destino;
        }
        Path plantilla = resolverPlantilla(documento);
        if (plantilla.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(DOCX_EXTENSION)) {
            generarDocxDesdeArchivo(plantilla, destino, valores, bloques);
        } else {
            Files.copy(plantilla, destino, StandardCopyOption.REPLACE_EXISTING);
        }
        return destino;
    }

    /**
     * Bloques de contenido configurados por el administrador (Administracion > Plantillas
     * de documento > bloques) para el tipo de documento. Si la consulta falla por cualquier
     * motivo (p.ej. tabla no aplicada todavia), se degrada a "sin bloques" sin romper la
     * generacion del documento.
     */
    private List<PlantillaBloqueDTO> obtenerBloques(DocumentoAnalizadoDTO documento) {
        try {
            return plantillaBloqueDAO.listarPorCodigoTipo(documento.getTipoDocumentoCodigo());
        } catch (SQLException ex) {
            return new ArrayList<PlantillaBloqueDTO>();
        }
    }

    /**
     * Prioridad de resolucion: si el tipo de documento tiene una version activa cargada
     * desde Administracion > Plantillas de documento (PLANTILLA_DOCUMENTO), esa BLOB gana
     * sobre el archivo en docs/plantillas. Si no hay fila activa (adopcion gradual) o la
     * consulta falla por cualquier motivo, se degrada al mecanismo de archivo existente.
     */
    private PlantillaDocumentoDAO.ContenidoPlantilla obtenerPlantillaDb(DocumentoAnalizadoDTO documento) {
        try {
            return plantillaDocumentoDAO.obtenerActivaPorCodigoTipo(documento.getTipoDocumentoCodigo());
        } catch (SQLException ex) {
            return null;
        }
    }

    public Path resolverPlantilla(DocumentoAnalizadoDTO documento) throws IOException {
        Path base = rutaBasePlantillas();
        if (!Files.isDirectory(base)) {
            throw new IOException("No se encontró la carpeta de plantillas: " + base.toAbsolutePath());
        }

        final String objetivoNombre = normalizarClave(documento.getTipoDocumentoNombre());
        final String objetivoCodigo = normalizarClave(limpiarCodigo(documento.getTipoDocumentoCodigo()));
        final Path[] mejor = new Path[1];
        final int[] mejorPuntaje = new int[]{-1};

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
            throw new IOException("No se encontró plantilla Word para el tipo de documento: "
                    + valor(documento.getTipoDocumentoNombre()));
        }
        return mejor[0];
    }

    public String nombreSugerido(AnalisisExpedienteDTO expediente, DocumentoAnalizadoDTO documento) {
        String tipo = normalizarArchivo(documento.getTipoDocumentoNombre());
        String numero = normalizarArchivo(expediente.getNumeroExpediente());
        String extension = ".docx";
        try {
            if (obtenerPlantillaDb(documento) == null) {
                extension = resolverPlantilla(documento).getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".doc")
                        ? ".doc"
                        : ".docx";
            }
        } catch (IOException ignored) {
            // Si no se puede resolver todavía, se sugiere docx y se mostrará el error al generar.
        }
        return tipo + "_" + numero + "_" + LocalDate.now().toString() + extension;
    }

    private void generarDocxDesdeArchivo(
            Path plantilla, Path destino, Map<String, String> valores, List<PlantillaBloqueDTO> bloques) throws IOException {
        try (InputStream in = Files.newInputStream(plantilla)) {
            generarDocxDesdeStream(in, destino, valores, bloques);
        }
    }

    private void generarDocxDesdeBytes(
            byte[] contenido, Path destino, Map<String, String> valores, List<PlantillaBloqueDTO> bloques) throws IOException {
        try (InputStream in = new ByteArrayInputStream(contenido)) {
            generarDocxDesdeStream(in, destino, valores, bloques);
        }
    }

    private void generarDocxDesdeStream(
            InputStream in, Path destino, Map<String, String> valores, List<PlantillaBloqueDTO> bloques) throws IOException {
        byte[] intermedio;
        try (XWPFDocument document = new XWPFDocument(in)) {
            aplicarCondicionales(document, valores);
            insertarBloques(document, bloques, valores);
            ByteArrayOutputStream bufferIntermedio = new ByteArrayOutputStream();
            document.write(bufferIntermedio);
            intermedio = bufferIntermedio.toByteArray();
        }
        // insertarBloques reubica parrafos con XmlCursor.moveXml (operacion cruda de XMLBeans);
        // la lista de parrafos que cachea POI en memoria (document.getParagraphs()) no queda
        // sincronizada con ese movimiento y produce XmlValueDisconnectedException si se sigue
        // usando el mismo objeto XWPFDocument. Se serializa y se vuelve a abrir para que POI
        // reconstruya su modelo en memoria a partir del XML ya reordenado, antes de continuar.
        try (InputStream inIntermedio = new ByteArrayInputStream(intermedio);
                XWPFDocument document = new XWPFDocument(inIntermedio)) {
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
            try (OutputStream out = Files.newOutputStream(destino)) {
                document.write(out);
            }
        }
    }

    /**
     * Permite condicionar parrafos dentro de una misma plantilla segun el valor de
     * CUALQUIER variable ya disponible en el mapa de sustitucion (el mismo que resuelve
     * los #variable#), usando marcadores de texto dentro del propio Word:
     * [[SI_canalRecepcion:MPV]] ... [[FIN_SI]]
     * [[SI_resAnalisis:PROCEDENTE|PROCEDENTE_EN_PARTE]] ... [[FIN_SI]]
     * El nombre de variable se compara sin distinguir mayusculas/minusculas ni guiones
     * bajos (tipoActa y TIPO_ACTA son el mismo criterio). Los parrafos marcador siempre
     * se eliminan; el contenido entre ellos se conserva solo si el valor actual de la
     * variable coincide con alguno de los valores listados (separados por "|").
     */
    private void aplicarCondicionales(XWPFDocument document, Map<String, String> valores) {
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        List<Integer> aEliminar = new ArrayList<Integer>();
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
     * Inserta, en orden, los bloques de contenido configurados desde Administracion >
     * Plantillas de documento > bloques cuya condicion se cumple, en el punto de la
     * plantilla base marcado con el parrafo literal [[CONTENIDO]] o, si el bloque tiene
     * seccion asignada, en el marcador nombrado [[CONTENIDO:seccion]] correspondiente.
     * Una misma plantilla puede tener varios marcadores (uno por seccion); cada uno recibe
     * solo los bloques de su propia seccion. Si la plantilla no tiene el marcador de una
     * seccion, esos bloques simplemente no se insertan (no rompe la generacion).
     * El titulo se inserta en negrita como parrafo aparte; ambos (titulo y contenido)
     * conservan sus #variable# intactas, que se resuelven despues en reemplazarParrafos
     * junto con el resto del documento.
     */
    private void insertarBloques(XWPFDocument document, List<PlantillaBloqueDTO> bloques, Map<String, String> valores) {
        if (bloques == null || bloques.isEmpty()) {
            return;
        }
        Map<String, List<PlantillaBloqueDTO>> bloquesPorSeccion = new LinkedHashMap<String, List<PlantillaBloqueDTO>>();
        for (PlantillaBloqueDTO bloque : bloques) {
            String clave = bloque.tieneSeccion() ? normalizarNombreVariable(bloque.getSeccion()) : "";
            List<PlantillaBloqueDTO> lista = bloquesPorSeccion.get(clave);
            if (lista == null) {
                lista = new ArrayList<PlantillaBloqueDTO>();
                bloquesPorSeccion.put(clave, lista);
            }
            lista.add(bloque);
        }
        // XWPFDocument.insertNewParagraph(cursor) tiene un ClassCastException conocido de
        // Apache POI 5.2.5 (XmlAnyTypeImpl -> CTP) al insertar en documentos reales de Word.
        // Se evita por completo: los parrafos nuevos se crean al final del documento
        // (createParagraph, via probado y sin problemas) y luego se reubican uno a uno justo
        // antes del marcador correspondiente con XmlCursor.moveXml (operacion generica de
        // XMLBeans sobre XML crudo, sin el casting problematico). Mover en orden de creacion,
        // siempre "justo antes del marcador", preserva el orden final correcto.
        for (XWPFParagraph marcador : buscarParrafosMarcador(document)) {
            String seccion = extraerSeccionMarcador(marcador.getText());
            List<PlantillaBloqueDTO> bloquesSeccion = bloquesPorSeccion.get(seccion);
            if (bloquesSeccion == null || bloquesSeccion.isEmpty()) {
                continue;
            }
            List<XWPFParagraph> nuevosParrafos = new ArrayList<XWPFParagraph>();
            for (PlantillaBloqueDTO bloque : bloquesSeccion) {
                if (!coincideCondicionBloque(bloque, valores)) {
                    continue;
                }
                if (hasText(bloque.getTitulo())) {
                    XWPFParagraph pTitulo = document.createParagraph();
                    XWPFRun runTitulo = pTitulo.createRun();
                    runTitulo.setBold(true);
                    runTitulo.setText(bloque.getTitulo());
                    nuevosParrafos.add(pTitulo);
                }
                if (hasText(bloque.getContenido())) {
                    XWPFParagraph pContenido = document.createParagraph();
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
        // Los parrafos marcador [[CONTENIDO...]] NO se eliminan aqui: tras moveXml, la lista
        // de parrafos que cachea POI en memoria queda desincronizada del XML real y
        // document.getPosOfParagraph/removeBodyElement ya no ubican los nodos correctos. La
        // eliminacion se hace en generarDocxDesdeStream, sobre el documento recien reabierto
        // tras el round-trip de serializacion.
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
        List<XWPFParagraph> marcadores = new ArrayList<XWPFParagraph>();
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            if (extraerSeccionMarcador(paragraph.getText()) != null) {
                marcadores.add(paragraph);
            }
        }
        return marcadores;
    }

    /**
     * Si el texto es exactamente [[CONTENIDO]] o [[CONTENIDO:seccion]], retorna la seccion
     * (cadena vacia para el marcador sin nombre). Si no coincide con el patron, retorna null.
     */
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

    private boolean coincideCondicionBloque(PlantillaBloqueDTO bloque, Map<String, String> valores) {
        if (!bloque.tieneCondicion()) {
            return true;
        }
        boolean coincide = coincideCriterio(bloque.getVariableCondicion(), bloque.getValoresCondicion(), valores);
        return PlantillaBloqueDTO.OPERADOR_NO_COINCIDE.equals(bloque.getOperadorCondicion()) ? !coincide : coincide;
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
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
            result = result.replace("[" + entry.getKey() + "]", entry.getValue());
            result = result.replace("#" + entry.getKey() + "#", entry.getValue());
        }
        return result;
    }

    private Map<String, String> valores(
            AnalisisExpedienteDTO expediente,
            DocumentoAnalizadoDTO documento,
            DocumentoAnalizadoDTO informeReferencia) {
        Map<String, String> values = new LinkedHashMap<String, String>();
        values.put("NUMERO_EXPEDIENTE", valor(expediente.getNumeroExpediente()));
        values.put("NUMERO_EXPEDIENTE_SGD", valor(expediente.getNumeroExpedienteSgd()));
        values.put("TRAMITE_WEB", valor(expediente.getNumeroTramiteDocumentario()));
        values.put("TITULAR", valor(expediente.getTitular()));
        values.put("DOCUMENTO_IDENTIDAD_TITULAR", valor(expediente.getNumeroDocumentoTitular()));
        values.put("SOLICITANTE", valor(expediente.getSolicitante()));
        values.put("DOCUMENTO_IDENTIDAD_SOLICITANTE", valor(expediente.getNumeroDocumentoSolicitante()));
        values.put("TIPO_ACTA", valor(expediente.getTipoActa()));
        values.put("NUMERO_ACTA", valor(expediente.getNumeroActa()));
        values.put("PROCEDIMIENTO_REGISTRAL", valor(expediente.getProcedimiento()));
        values.put("ABOGADO_RESPONSABLE", valor(expediente.getResponsable()));
        values.put("EQUIPO", valor(expediente.getEquipo()));
        values.put("FECHA_SOLICITUD", fecha(expediente.getFechaRecepcion()));
        values.put("TIPO_DOCUMENTO", valor(documento.getTipoDocumentoNombre()));
        values.put("ESTADO_DOCUMENTO", valor(documento.getEstadoDocumentoNombre()));
        values.put("FECHA_DOCUMENTO", fecha(documento.getFechaDocumento()));
        values.put("NUMERO_DOCUMENTO", valor(documento.getNumeroDocumento()));
        values.put("DESCRIPCION_DOCUMENTO", valor(documento.getDescripcion()));
        values.put("REQUIERE_RESPUESTA", documento.isRequiereRespuesta() ? "Sí" : "No");
        values.put("FECHA_ACUSE", fecha(documento.getFechaAcuse()));
        values.put("CONFIRMACION_RESPUESTA", valor(documento.getConfirmacionRespuesta()));
        values.put("FECHA_RESPUESTA", fecha(documento.getFechaRespuesta()));
        values.put("HOJA_ENVIO", valor(documento.getNumeroHojaEnvioRespuesta()));
        values.put("NOTIFICADO", documento.isNotificado() ? "Sí" : "No");
        values.put("nomTitular", valor(expediente.getTitular()));
        values.put("dniTitular", valor(expediente.getNumeroDocumentoTitular()));
        values.put("nomSolicitante", valor(expediente.getSolicitante()));
        values.put("dniSolicitante", valor(expediente.getNumeroDocumentoSolicitante()));
        values.put("tipoActa", valor(expediente.getTipoActa()));
        values.put("nroActa", valor(expediente.getNumeroActa()));
        values.put("direccion", valor(expediente.getDireccionSolicitante()));
        values.put("correo", valor(expediente.getCorreoSolicitante()));
        values.put("tipoProcedimiento", valor(expediente.getProcedimiento()));
        values.put("fechaSolicitud", fecha(expediente.getFechaRecepcion()));
        values.put("nroTramiteWeb", valor(expediente.getNumeroTramiteDocumentario()));
        values.put("canalRecepcion", valor(expediente.getCanalIngreso()));
        values.put("fechaActual", fecha(LocalDate.now()));
        values.put("numDoc", valor(expediente.getNumeroDocumento()));
        values.put("tipoDoc", valor(expediente.getTipoDocumento()));
        values.put("numDocInforme", informeReferencia == null ? "" : valor(informeReferencia.getNumeroDocumento()));
        values.put("fechaDocInforme", informeReferencia == null ? "" : fecha(informeReferencia.getFechaDocumento()));
        values.put("resAnalisis", valor(expediente.getUltimoResultadoAnalisis()));
        return values;
    }

    private static DocumentoAnalizadoDTO informeMasReciente(List<DocumentoAnalizadoDTO> documentos) {
        if (documentos == null) {
            return null;
        }
        DocumentoAnalizadoDTO mejor = null;
        for (DocumentoAnalizadoDTO candidato : documentos) {
            if (candidato == null || !candidato.isActivo() || !esTipoInforme(candidato)) {
                continue;
            }
            if (mejor == null || esMasReciente(candidato, mejor)) {
                mejor = candidato;
            }
        }
        return mejor;
    }

    private static boolean esTipoInforme(DocumentoAnalizadoDTO documento) {
        String codigo = limpiarCodigo(documento.getTipoDocumentoCodigo()).toUpperCase(Locale.ROOT);
        if (codigo.startsWith("INFORME")) {
            return true;
        }
        String nombre = valor(documento.getTipoDocumentoNombre()).toUpperCase(Locale.ROOT);
        return nombre.startsWith("INFORME");
    }

    private static boolean esMasReciente(DocumentoAnalizadoDTO candidato, DocumentoAnalizadoDTO actual) {
        LocalDate fechaCandidato = candidato.getFechaDocumento();
        LocalDate fechaActualMejor = actual.getFechaDocumento();
        if (fechaCandidato != null && fechaActualMejor != null) {
            return fechaCandidato.isAfter(fechaActualMejor);
        }
        if (fechaCandidato != null) {
            return true;
        }
        if (fechaActualMejor != null) {
            return false;
        }
        return candidato.getFechaRegistro() != null
                && (actual.getFechaRegistro() == null || candidato.getFechaRegistro().isAfter(actual.getFechaRegistro()));
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
        String normalized = Normalizer.normalize(valor(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return normalized;
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
