package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.AnalisisExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
            "\\[\\[SI_(ACTA|PROCEDIMIENTO):([^\\]]*)\\]\\]", Pattern.CASE_INSENSITIVE);
    private static final String MARCADOR_FIN_SI = "[[FIN_SI]]";

    public Path generarDocumento(
            AnalisisExpedienteDTO expediente,
            DocumentoAnalizadoDTO documento,
            List<DocumentoAnalizadoDTO> documentosExpediente,
            Path destino) throws IOException {
        Path plantilla = resolverPlantilla(documento);
        Files.createDirectories(destino.toAbsolutePath().getParent());
        if (plantilla.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(DOCX_EXTENSION)) {
            DocumentoAnalizadoDTO informeReferencia = informeMasReciente(documentosExpediente);
            generarDocx(plantilla, destino, expediente, valores(expediente, documento, informeReferencia));
        } else {
            Files.copy(plantilla, destino, StandardCopyOption.REPLACE_EXISTING);
        }
        return destino;
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
            extension = resolverPlantilla(documento).getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".doc")
                    ? ".doc"
                    : ".docx";
        } catch (IOException ignored) {
            // Si no se puede resolver todavía, se sugiere docx y se mostrará el error al generar.
        }
        return tipo + "_" + numero + "_" + LocalDate.now().toString() + extension;
    }

    private void generarDocx(
            Path plantilla,
            Path destino,
            AnalisisExpedienteDTO expediente,
            Map<String, String> valores) throws IOException {
        try (InputStream in = Files.newInputStream(plantilla);
                XWPFDocument document = new XWPFDocument(in)) {
            aplicarCondicionales(document, expediente);
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
     * Permite condicionar parrafos dentro de una misma plantilla segun el tipo de
     * acta o el procedimiento registral del expediente, usando marcadores de texto:
     * [[SI_ACTA:Valor1|Valor2]] ... [[FIN_SI]]
     * [[SI_PROCEDIMIENTO:Valor1|Valor2]] ... [[FIN_SI]]
     * Los parrafos marcador siempre se eliminan; el contenido entre ellos se
     * conserva solo si el expediente coincide con alguno de los valores listados.
     */
    private void aplicarCondicionales(XWPFDocument document, AnalisisExpedienteDTO expediente) {
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        String tipoActaActual = normalizarClave(expediente.getTipoActa());
        String procedimientoActual = normalizarClave(expediente.getProcedimiento());
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
                boolean coincide = coincideCriterio(
                        matcher.group(1).toUpperCase(Locale.ROOT),
                        matcher.group(2),
                        tipoActaActual,
                        procedimientoActual);
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

    private boolean coincideCriterio(
            String criterio, String valoresCriterio, String tipoActaActual, String procedimientoActual) {
        String objetivo = "ACTA".equals(criterio) ? tipoActaActual : procedimientoActual;
        if (objetivo.isEmpty()) {
            return false;
        }
        for (String valorEsperado : valoresCriterio.split("\\|")) {
            if (normalizarClave(valorEsperado).equals(objetivo)) {
                return true;
            }
        }
        return false;
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
}
