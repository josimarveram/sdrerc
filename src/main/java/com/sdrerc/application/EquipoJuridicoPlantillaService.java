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

    private static final String VERSION_PLANTILLA = "PLANTILLA_SDRERC_EQUIPO_JURIDICO_V2";
    private static final String[] COLUMNAS = {
        "ITEM",
        "ABOGADO",
        "SUPERVISOR",
        "PERSONAL",
        "ESTADO"
    };

    private static final String[] ESTADOS = {
        "ACTIVO",
        "INACTIVO"
    };

    private static final String[] TIPOS_PERSONAL = {
        "PERSONAL PLANTA",
        "PERSONAL OR",
        "CAS ELECTORAL",
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
            {1, "ALVARADO CAZORLA JOSE PAUL", "SANTIAGO RAMIREZ JULIO", "PERSONAL PLANTA", "ACTIVO"},
            {2, "ALVAREZ GARCIA ANA CAROLINA", "SANTIAGO RAMIREZ JULIO", "CAS ELECTORAL", "ACTIVO"},
            {3, "VERA MIRANDA JOSIMAR", "LOPEZ RAMOS MARIA", "PERSONAL OR", "ACTIVO"}
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
            "ABOGADO debe ingresarse en formato: APELLIDO_PATERNO APELLIDO_MATERNO NOMBRES.",
            "SUPERVISOR debe ingresarse en formato: APELLIDO_PATERNO APELLIDO_MATERNO NOMBRES.",
            "Ejemplos validos: ALVARADO CAZORLA JOSE PAUL, VERA MIRANDA JOSIMAR, PEREZ SOTO JUAN CARLOS.",
            "Parser futuro: con 3 o mas palabras, la primera es apellido paterno, la segunda apellido materno y la tercera en adelante nombres.",
            "Si el nombre tiene 2 palabras, se marcara advertencia en la futura previsualizacion.",
            "Si el nombre tiene 1 palabra, se marcara error.",
            "USERNAME no se llena en esta plantilla; el sistema lo generara automaticamente en la futura importacion.",
            "Regla futura de USERNAME: primera letra del primer nombre real + apellido paterno + inicial del apellido materno.",
            "Ejemplos de USERNAME: ALVARADO CAZORLA JOSE PAUL -> jalvaradoc; VERA MIRANDA JOSIMAR -> jveram; PEREZ SOTO JUAN CARLOS -> jperezs.",
            "Si el USERNAME generado ya existe, el sistema agregara correlativo.",
            "SUPERVISOR es recomendado; si no existe, el sistema lo creara o lo detectara en la futura importacion.",
            "Si SUPERVISOR queda vacio, el abogado se cargara sin supervisor y quedara como advertencia en la futura previsualizacion.",
            "PERSONAL es informativo en esta fase. Valores sugeridos: PERSONAL PLANTA, PERSONAL OR, CAS ELECTORAL, OTRO.",
            "ESTADO acepta ACTIVO o INACTIVO. Si queda vacio, se asumira ACTIVO en la futura importacion.",
            "Las observaciones, errores y advertencias se mostraran en la futura pantalla de previsualizacion/reporte."
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
        String[] headers = {"ESTADO", "PERSONAL"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int maxRows = Math.max(ESTADOS.length, TIPOS_PERSONAL.length);
        for (int i = 0; i < maxRows; i++) {
            Row row = sheet.createRow(i + 1);
            crearCeldaCatalogo(row, 0, i < ESTADOS.length ? ESTADOS[i] : "", textStyle);
            crearCeldaCatalogo(row, 1, i < TIPOS_PERSONAL.length ? TIPOS_PERSONAL[i] : "", textStyle);
        }

        sheet.setColumnWidth(0, 18 * 256);
        sheet.setColumnWidth(1, 24 * 256);
    }

    private void crearCeldaCatalogo(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void agregarValidaciones(Sheet sheet) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        agregarLista(sheet, helper, 3, TIPOS_PERSONAL);
        agregarLista(sheet, helper, 4, ESTADOS);
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
        int[] widths = {8, 38, 38, 24, 16};
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
