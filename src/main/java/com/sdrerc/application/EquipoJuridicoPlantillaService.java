package com.sdrerc.application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class EquipoJuridicoPlantillaService {

    private static final String VERSION_PLANTILLA = "PLANTILLA_SDRERC_EQUIPO_JURIDICO_V1";
    private static final String[] COLUMNAS = {
        "ITEM",
        "TIPO_PERSONAL",
        "ROL_OPERATIVO",
        "APELLIDO_PATERNO",
        "APELLIDO_MATERNO",
        "NOMBRES",
        "NOMBRE_COMPLETO",
        "TIPO_DOCUMENTO",
        "NUMERO_DOCUMENTO",
        "USERNAME",
        "PASSWORD_TEMPORAL",
        "SUPERVISOR",
        "ESTADO",
        "OBSERVACION"
    };

    private static final String[] ROLES_OPERATIVOS = {
        "ABOGADO",
        "SUPERVISION",
        "ABOGADO_SUPERVISION"
    };

    private static final String[] ESTADOS = {
        "ACTIVO",
        "INACTIVO"
    };

    private static final String[] TIPOS_DOCUMENTO = {
        "DNI",
        "CE",
        "PASAPORTE",
        "OTRO"
    };

    public void generarPlantilla(File destino) throws IOException {
        if (destino == null) {
            throw new IllegalArgumentException("Seleccione una ruta de destino.");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle textStyle = crearEstiloTexto(workbook);
            CellStyle instructionTitleStyle = crearEstiloTitulo(workbook);

            crearHojaEquipoJuridico(workbook, headerStyle, textStyle);
            crearHojaInstrucciones(workbook, instructionTitleStyle, textStyle);
            crearHojaCatalogos(workbook, headerStyle, textStyle);

            int catalogosIndex = workbook.getSheetIndex("CATALOGOS");
            if (catalogosIndex >= 0) {
                workbook.setSheetHidden(catalogosIndex, true);
            }

            try (FileOutputStream out = new FileOutputStream(destino)) {
                workbook.write(out);
            }
        }
    }

    private void crearHojaEquipoJuridico(Workbook workbook, CellStyle headerStyle, CellStyle textStyle) {
        Sheet sheet = workbook.createSheet("EQUIPO_JURIDICO");
        Row header = sheet.createRow(0);
        header.setHeightInPoints(24);

        for (int i = 0; i < COLUMNAS.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(COLUMNAS[i]);
            cell.setCellStyle(headerStyle);
        }

        Object[][] ejemplos = {
            {1, "ABOGADO", "ABOGADO", "PEREZ", "SOTO", "JUAN CARLOS", "PEREZ SOTO JUAN CARLOS", "DNI", "12345678", "jperez", "", "MARIA LOPEZ RAMOS", "ACTIVO", "Fila de ejemplo. Reemplazar antes de importar."},
            {2, "SUPERVISOR", "SUPERVISION", "LOPEZ", "RAMOS", "MARIA", "LOPEZ RAMOS MARIA", "DNI", "87654321", "mlopez", "", "", "ACTIVO", "Fila de ejemplo. Reemplazar antes de importar."},
            {3, "ABOGADO/SUPERVISOR", "ABOGADO_SUPERVISION", "GARCIA", "NUNEZ", "ANA", "GARCIA NUNEZ ANA", "", "", "", "", "", "ACTIVO", "USERNAME y PASSWORD_TEMPORAL pueden quedar vacios."}
        };

        for (int r = 0; r < ejemplos.length; r++) {
            Row row = sheet.createRow(r + 1);
            for (int c = 0; c < ejemplos[r].length; c++) {
                Cell cell = row.createCell(c);
                Object value = ejemplos[r][c];
                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else {
                    cell.setCellValue(value == null ? "" : value.toString());
                }
                cell.setCellStyle(textStyle);
            }
        }

        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, COLUMNAS.length - 1));
        configurarAnchos(sheet);
        agregarValidaciones(sheet);
    }

    private void crearHojaInstrucciones(Workbook workbook, CellStyle titleStyle, CellStyle textStyle) {
        Sheet sheet = workbook.createSheet("INSTRUCCIONES");
        int rowIndex = 0;

        Row title = sheet.createRow(rowIndex++);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue("Plantilla oficial SDRERC - Equipo Juridico");
        titleCell.setCellStyle(titleStyle);

        Row version = sheet.createRow(rowIndex++);
        version.createCell(0).setCellValue("Version");
        version.createCell(1).setCellValue(VERSION_PLANTILLA);

        rowIndex++;
        String[] instrucciones = {
            "No modificar nombres de columnas.",
            "No agregar ni eliminar columnas.",
            "No llenar ID_TECNICO, USER_ID ni ROLE_ID.",
            "El sistema generara los identificadores internos.",
            "ROL_OPERATIVO acepta: ABOGADO, SUPERVISION, ABOGADO_SUPERVISION.",
            "Si USERNAME queda vacio, se sugerira automaticamente en la fase de importacion.",
            "Si PASSWORD_TEMPORAL queda vacio, se usara una contrasena temporal definida.",
            "SUPERVISOR debe coincidir con un supervisor existente o incluido en la plantilla.",
            "NUMERO_DOCUMENTO no es obligatorio en esta fase.",
            "OBSERVACION es para resultados de validacion/importacion posterior."
        };

        for (String instruccion : instrucciones) {
            Row row = sheet.createRow(rowIndex++);
            Cell cell = row.createCell(0);
            cell.setCellValue(instruccion);
            cell.setCellStyle(textStyle);
        }

        sheet.setColumnWidth(0, 86 * 256);
        sheet.setColumnWidth(1, 44 * 256);
    }

    private void crearHojaCatalogos(Workbook workbook, CellStyle headerStyle, CellStyle textStyle) {
        Sheet sheet = workbook.createSheet("CATALOGOS");
        Row header = sheet.createRow(0);
        String[] headers = {"ROL_OPERATIVO", "ESTADO", "TIPO_DOCUMENTO"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int maxRows = Math.max(ROLES_OPERATIVOS.length, Math.max(ESTADOS.length, TIPOS_DOCUMENTO.length));
        for (int i = 0; i < maxRows; i++) {
            Row row = sheet.createRow(i + 1);
            crearCeldaCatalogo(row, 0, i < ROLES_OPERATIVOS.length ? ROLES_OPERATIVOS[i] : "", textStyle);
            crearCeldaCatalogo(row, 1, i < ESTADOS.length ? ESTADOS[i] : "", textStyle);
            crearCeldaCatalogo(row, 2, i < TIPOS_DOCUMENTO.length ? TIPOS_DOCUMENTO[i] : "", textStyle);
        }

        sheet.setColumnWidth(0, 28 * 256);
        sheet.setColumnWidth(1, 18 * 256);
        sheet.setColumnWidth(2, 20 * 256);
    }

    private void crearCeldaCatalogo(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void agregarValidaciones(Sheet sheet) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        agregarLista(sheet, helper, 2, ROLES_OPERATIVOS);
        agregarLista(sheet, helper, 7, TIPOS_DOCUMENTO);
        agregarLista(sheet, helper, 12, ESTADOS);
    }

    private void agregarLista(Sheet sheet, DataValidationHelper helper, int column, String[] values) {
        CellRangeAddressList addressList = new CellRangeAddressList(1, 1000, column, column);
        DataValidationConstraint constraint = helper.createExplicitListConstraint(values);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    private void configurarAnchos(Sheet sheet) {
        int[] widths = {8, 22, 26, 24, 24, 28, 38, 20, 22, 24, 26, 38, 16, 54};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
    }

    private CellStyle crearEstiloCabecera(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle crearEstiloTexto(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        return style;
    }
}
