package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.CargaDiariaPreviewDTO;
import com.sdrerc.v3.domain.rules.ProcedimientoRegistralRules;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

/**
 * Port de la exportación a Excel de la previsualización de Carga diaria
 * ({@code JPanelCargaDiariaRecepcionV2.exportarPrevisualizacion}/{@code escribirExcelPrevisualizacion},
 * V2). V2 exporta directamente los valores ya formateados del {@code JTable} (mismas 20 columnas
 * visibles, mismo orden); acá se reconstruyen esos mismos valores formateados a partir del DTO,
 * porque V3 no tiene un modelo de tabla Swing del que leer. Mismas 20 columnas/encabezados, mismo
 * criterio de formato (fecha dd/MM/yyyy, documento "SIN DNI" vacío, canal con nombre visible,
 * duplicidad Sí/No, número de expediente con los mismos 3 textos de respaldo, observación
 * consolidada en frases "Dato incompleto: X").
 */
@Service
public class CargaDiariaExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] COLUMNAS = {
        "TIPO DE SOLICITUD",
        "FECHA DE SOLICITUD",
        "SOLICITADO POR",
        "TIPO DOCUMENTO IDENTIDAD SOLICITANTE",
        "N° DOCUMENTO IDENTIDAD SOLICITANTE",
        "N° TRÁMITE WEB",
        "CANAL RECEPCIÓN",
        "N° EXPEDIENTE SGD",
        "TIPO DOCUMENTO",
        "N° DOCUMENTO",
        "PROCEDIMIENTO REGISTRAL",
        "TIPO DE ACTA",
        "N° ACTA",
        "TITULAR",
        "TIPO DOCUMENTO IDENTIDAD TITULAR",
        "N° DOCUMENTO IDENTIDAD TITULAR",
        "RESULTADO DEL SISTEMA",
        "DUPLICIDAD",
        "NÚMERO EXPEDIENTE",
        "OBSERVACIÓN"
    };

    public byte[] exportarExcel(List<CargaDiariaPreviewDTO> registros) throws IOException {
        List<CargaDiariaPreviewDTO> filas = registros == null ? new ArrayList<>() : registros;
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Previsualización");
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle textStyle = crearEstiloTexto(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);

            Row header = sheet.createRow(0);
            for (int col = 0; col < COLUMNAS.length; col++) {
                Cell cell = header.createCell(col);
                cell.setCellValue(COLUMNAS[col]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < filas.size(); i++) {
                CargaDiariaPreviewDTO item = filas.get(i);
                Row row = sheet.createRow(i + 1);
                Object[] valores = crearFila(item);
                for (int col = 0; col < valores.length; col++) {
                    Cell cell = row.createCell(col);
                    if (col == 1 && item.getFechaRecepcion() != null) {
                        cell.setCellValue(java.sql.Date.valueOf(item.getFechaRecepcion()));
                        cell.setCellStyle(dateStyle);
                    } else {
                        cell.setCellValue(valores[col] == null ? "" : valores[col].toString());
                        cell.setCellStyle(textStyle);
                    }
                }
            }

            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new CellRangeAddress(0, filas.size(), 0, COLUMNAS.length - 1));
            for (int col = 0; col < COLUMNAS.length; col++) {
                sheet.autoSizeColumn(col);
                int width = sheet.getColumnWidth(col);
                sheet.setColumnWidth(col, Math.min(Math.max(width + 512, 2800), 18000));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private Object[] crearFila(CargaDiariaPreviewDTO item) {
        return new Object[]{
            safe(item.getTipoSolicitud()),
            item.getFechaRecepcion() == null ? safe(item.getFechaRecepcionTexto()) : DATE_FORMAT.format(item.getFechaRecepcion()),
            safe(item.getRemitente()),
            safe(item.getTipoDocumentoIdentidadSolicitante()),
            documentoVisual(item.getNumeroDocumentoIdentidadSolicitante()),
            safe(item.getNumeroTramite()),
            canalVisual(item.getCanalRecepcion()),
            safe(item.getNumeroExpedienteSgd()),
            safe(item.getTipoDocumento()),
            safe(item.getNumeroDocumento()),
            safe(item.getTipoProcedimiento()),
            safe(item.getTipoActa()),
            safe(item.getNumeroActa()),
            safe(item.getTitular()),
            safe(item.getTipoDocumentoIdentidadTitular()),
            documentoVisual(item.getNumeroDocumentoIdentidadTitular()),
            safe(item.getEstadoValidacion()),
            item.isPosibleDuplicado() ? "Sí" : "No",
            numeroExpedientePreview(item),
            observacionValidacionTabla(item)
        };
    }

    private CellStyle crearEstiloCabecera(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        aplicarBordes(style);
        return style;
    }

    private CellStyle crearEstiloTexto(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        aplicarBordes(style);
        return style;
    }

    private CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = crearEstiloTexto(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/MM/yyyy"));
        return style;
    }

    private void aplicarBordes(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String documentoVisual(String value) {
        return value == null || value.trim().isEmpty() || "SIN DNI".equalsIgnoreCase(value.trim()) ? "" : value.trim();
    }

    private static String canalVisual(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT)
                .replace('Á', 'A')
                .replace('É', 'E')
                .replace('Í', 'I')
                .replace('Ó', 'O')
                .replace('Ú', 'U')
                .replaceAll("\\s+", "_");
        if ("INTERNO".equals(normalized)) {
            return "Interno";
        }
        if ("MESA_PARTES_PRESENCIAL".equals(normalized) || "MESA_DE_PARTES_PRESENCIAL".equals(normalized)) {
            return "Mesa de partes presencial";
        }
        if ("MESA_PARTES_VIRTUAL".equals(normalized) || "MESA_DE_PARTES_VIRTUAL".equals(normalized) || "MPV".equals(normalized)) {
            return "Mesa de partes virtual";
        }
        if ("OR".equals(normalized) || "OR_PRESENCIAL".equals(normalized)) {
            return "OR Presencial";
        }
        if ("OR_PASIVO".equals(normalized) || "PASIVO_OR".equals(normalized)) {
            return "OR Pasivo";
        }
        return value.trim();
    }

    static String numeroExpedientePreview(CargaDiariaPreviewDTO item) {
        if (item == null) {
            return "Pendiente";
        }
        if (hasText(item.getNumeroExpedienteGenerado())) {
            return item.getNumeroExpedienteGenerado();
        }
        if (item.isPosibleDuplicado()) {
            return "Sin número por duplicado";
        }
        if (ProcedimientoRegistralRules.requiereDecisionAsignacionParaNumero(item.getTipoProcedimiento())) {
            return "Sin número por procedimiento";
        }
        return "Pendiente";
    }

    static String observacionValidacionTabla(CargaDiariaPreviewDTO item) {
        List<String> observaciones = new ArrayList<>();
        if (item == null) {
            return "Sin observación";
        }
        if (item.isPosibleDuplicado()) {
            observaciones.add("Potencial duplicado");
        }
        if (item.isGrupoFamiliar() || item.isPosibleGrupoFamiliar()) {
            observaciones.add("Posible Grupo Familiar");
        }
        observaciones.addAll(observacionesIncompletas(item.getMensajeValidacion()));
        if (observaciones.isEmpty()) {
            return "Sin observación";
        }
        return String.join(" | ", observaciones);
    }

    private static List<String> observacionesIncompletas(String mensajeValidacion) {
        List<String> observaciones = new ArrayList<>();
        if (!hasText(mensajeValidacion)) {
            return observaciones;
        }
        String[] partes = mensajeValidacion.split("\\s*\\|\\s*");
        for (String parte : partes) {
            String observacion = convertirADatoIncompleto(parte);
            if (hasText(observacion) && !observaciones.contains(observacion)) {
                observaciones.add(observacion);
            }
        }
        return observaciones;
    }

    private static String convertirADatoIncompleto(String mensaje) {
        if (!hasText(mensaje)) {
            return null;
        }
        String lower = mensaje.trim().toLowerCase(Locale.ROOT);
        if (!lower.contains("obligatorio") && !lower.contains("inválida") && !lower.contains("invalida") && !lower.contains("determinar")) {
            return null;
        }
        if (lower.contains("número de trámite")) {
            return "Dato incompleto: Número de trámite";
        }
        if (lower.contains("número de documento")) {
            return "Dato incompleto: N° Documento";
        }
        if (lower.contains("tipo de procedimiento")) {
            return "Dato incompleto: Procedimiento registral";
        }
        if (lower.contains("tipo de solicitud")) {
            return "Dato incompleto: Tipo de solicitud";
        }
        if (lower.contains("tipo de documento de identidad del solicitante")) {
            return "Dato incompleto: Tipo documento identidad solicitante";
        }
        if (lower.contains("número de documento de identidad del solicitante")) {
            return "Dato incompleto: N° documento identidad solicitante";
        }
        if (lower.contains("tipo de documento de identidad del titular")) {
            return "Dato incompleto: Tipo documento identidad titular";
        }
        if (lower.contains("número de documento de identidad del titular")) {
            return "Dato incompleto: N° documento identidad titular";
        }
        if (lower.contains("tipo de documento")) {
            return "Dato incompleto: Tipo documento";
        }
        if (lower.contains("tipo de acta")) {
            return "Dato incompleto: Tipo de acta";
        }
        if (lower.contains("número de acta")) {
            return "Dato incompleto: N° Acta";
        }
        if (lower.contains("titular")) {
            return "Dato incompleto: Titular";
        }
        if (lower.contains("fecha de solicitud")) {
            return "Dato incompleto: Fecha de solicitud";
        }
        if (lower.contains("canal de recepción") || lower.contains("canal de recepcion")) {
            return "Dato incompleto: Canal recepción";
        }
        return null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
