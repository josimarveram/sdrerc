package com.sdrerc.application.sdrercapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CargaDiariaPlantillaService {

    public static final String NOMBRE_ARCHIVO = "plantilla_carga_diaria_sdrerc.xlsx";

    private static final String VERSION_PLANTILLA = "PLANTILLA_SDRERC_CARGA_DIARIA_V2";
    private static final String HOJA_CARGA = "CARGA_DIARIA";
    private static final String HOJA_INSTRUCCIONES = "INSTRUCCIONES";
    private static final String HOJA_CATALOGOS = "CATALOGOS";
    private static final int ULTIMA_FILA_VALIDACION = 1000;

    private static final int COL_TIPO_SOLICITUD = 0;
    private static final int COL_FECHA_SOLICITUD = 1;
    private static final int COL_SOLICITADO_POR = 2;
    private static final int COL_TIPO_DOC_SOLICITANTE = 3;
    private static final int COL_NUM_DOC_SOLICITANTE = 4;
    private static final int COL_TRAMITE_WEB = 5;
    private static final int COL_TIPO_DOCUMENTO = 6;
    private static final int COL_NUM_DOCUMENTO = 7;
    private static final int COL_PROCEDIMIENTO = 8;
    private static final int COL_TIPO_ACTA = 9;
    private static final int COL_NUM_ACTA = 10;
    private static final int COL_TITULAR = 11;
    private static final int COL_TIPO_DOC_TITULAR = 12;
    private static final int COL_NUM_DOC_TITULAR = 13;
    private static final int COL_OBSERVACION = 14;

    private static final String[] COLUMNAS = {
        "TIPO DE SOLICITUD",
        "FECHA DE SOLICITUD",
        "SOLICITADO POR",
        "TIPO DOCUMENTO IDENTIDAD SOLICITANTE",
        "N° DOCUMENTO IDENTIDAD SOLICITANTE",
        "N° TRÁMITE WEB",
        "TIPO DOCUMENTO",
        "N° DOCUMENTO",
        "PROCEDIMIENTO REGISTRAL",
        "TIPO DE ACTA",
        "N° ACTA",
        "TITULAR",
        "TIPO DOCUMENTO IDENTIDAD TITULAR",
        "N° DOCUMENTO IDENTIDAD TITULAR",
        "OBSERVACIÓN INICIAL"
    };

    private static final int[] ANCHOS = {
        22, 20, 36, 30, 32, 28, 22, 28, 34, 22, 20, 38, 28, 30, 44
    };

    private static final String[] CATALOGO_TIPO_SOLICITUD = {"PARTE", "OFICIO"};
    private static final String[] CATALOGO_IDENTIDAD_SOLICITANTE = {"DNI", "RUC", "CE", "PASAPORTE"};
    private static final String[] CATALOGO_IDENTIDAD_TITULAR = {"DNI", "CE", "PASAPORTE"};
    private static final String[] CATALOGO_PROCEDIMIENTO = {
        "Reconsideración",
        "Rectificación administrativa",
        "Título de Nacionalidad",
        "Cancelación",
        "Reconstitución",
        "Apelación",
        "Actualización de datos"
    };
    private static final String[] CATALOGO_TIPO_ACTA = {"Nacimiento", "Matrimonio", "Defunción"};
    private static final String[] CATALOGO_TIPO_DOCUMENTO = {
        "Solicitud",
        "Carta",
        "Informe",
        "Oficio",
        "Resolución",
        "Hoja de envío",
        "Memorando",
        "Hoja de elevación",
        "Proveído",
        "Pedido",
        "Formato",
        "Expediente",
        "Ayuda memoria",
        "Informe técnico"
    };

    public void generarPlantilla(File destino) throws IOException {
        if (destino == null) {
            throw new IllegalArgumentException("Seleccione una ruta de destino.");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle textStyle = crearEstiloTexto(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);
            CellStyle titleStyle = crearEstiloTitulo(workbook);

            crearHojaCatalogos(workbook);
            crearHojaCarga(workbook, headerStyle, textStyle, dateStyle);
            crearHojaInstrucciones(workbook, titleStyle, textStyle);
            workbook.setSheetHidden(workbook.getSheetIndex(HOJA_CATALOGOS), true);
            workbook.setActiveSheet(workbook.getSheetIndex(HOJA_CARGA));

            try (FileOutputStream out = new FileOutputStream(destino)) {
                workbook.write(out);
            }
        }
    }

    private void crearHojaCarga(Workbook workbook, CellStyle headerStyle, CellStyle textStyle, CellStyle dateStyle) {
        Sheet sheet = workbook.createSheet(HOJA_CARGA);
        Row header = sheet.createRow(0);
        header.setHeightInPoints(26);

        for (int i = 0; i < COLUMNAS.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(COLUMNAS[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, ANCHOS[i] * 256);
            sheet.setDefaultColumnStyle(i, i == 1 ? dateStyle : textStyle);
        }

        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, COLUMNAS.length - 1));
        aplicarValidaciones(sheet);
    }

    private void crearHojaCatalogos(Workbook workbook) {
        Sheet sheet = workbook.createSheet(HOJA_CATALOGOS);
        crearCatalogo(workbook, sheet, 0, "CAT_TIPO_SOLICITUD", "TIPO DE SOLICITUD", CATALOGO_TIPO_SOLICITUD);
        crearCatalogo(workbook, sheet, 1, "CAT_IDENTIDAD_SOLICITANTE", "TIPO DOC. SOLICITANTE", CATALOGO_IDENTIDAD_SOLICITANTE);
        crearCatalogo(workbook, sheet, 2, "CAT_IDENTIDAD_TITULAR", "TIPO DOC. TITULAR", CATALOGO_IDENTIDAD_TITULAR);
        crearCatalogo(workbook, sheet, 3, "CAT_PROCEDIMIENTO", "PROCEDIMIENTO REGISTRAL", CATALOGO_PROCEDIMIENTO);
        crearCatalogo(workbook, sheet, 4, "CAT_TIPO_ACTA", "TIPO DE ACTA", CATALOGO_TIPO_ACTA);
        crearCatalogo(workbook, sheet, 5, "CAT_TIPO_DOCUMENTO", "TIPO DOCUMENTO", CATALOGO_TIPO_DOCUMENTO);
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void crearCatalogo(Workbook workbook, Sheet sheet, int column, String nombreRango, String titulo, String[] valores) {
        Row header = obtenerOCrearFila(sheet, 0);
        header.createCell(column).setCellValue(titulo);
        for (int i = 0; i < valores.length; i++) {
            Row row = obtenerOCrearFila(sheet, i + 1);
            row.createCell(column).setCellValue(valores[i]);
        }

        Name name = workbook.createName();
        name.setNameName(nombreRango);
        String letra = CellReference.convertNumToColString(column);
        name.setRefersToFormula("'" + HOJA_CATALOGOS + "'!$" + letra + "$2:$" + letra + "$" + (valores.length + 1));
    }

    private Row obtenerOCrearFila(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        return row == null ? sheet.createRow(rowIndex) : row;
    }

    private void aplicarValidaciones(Sheet sheet) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        aplicarLista(sheet, helper, COL_TIPO_SOLICITUD, "CAT_TIPO_SOLICITUD");
        aplicarLista(sheet, helper, COL_TIPO_DOC_SOLICITANTE, "CAT_IDENTIDAD_SOLICITANTE");
        aplicarLista(sheet, helper, COL_TIPO_DOCUMENTO, "CAT_TIPO_DOCUMENTO");
        aplicarLista(sheet, helper, COL_PROCEDIMIENTO, "CAT_PROCEDIMIENTO");
        aplicarLista(sheet, helper, COL_TIPO_ACTA, "CAT_TIPO_ACTA");
        aplicarLista(sheet, helper, COL_TIPO_DOC_TITULAR, "CAT_IDENTIDAD_TITULAR");
    }

    private void aplicarLista(Sheet sheet, DataValidationHelper helper, int column, String nombreRango) {
        DataValidationConstraint constraint = helper.createFormulaListConstraint(nombreRango);
        CellRangeAddressList rango = new CellRangeAddressList(1, ULTIMA_FILA_VALIDACION, column, column);
        DataValidation validation = helper.createValidation(constraint, rango);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.createErrorBox("Valor no permitido", "Seleccione una opción válida de la lista.");
        sheet.addValidationData(validation);
    }

    private void crearHojaInstrucciones(Workbook workbook, CellStyle titleStyle, CellStyle textStyle) {
        Sheet sheet = workbook.createSheet(HOJA_INSTRUCCIONES);
        int rowIndex = 0;

        Row title = sheet.createRow(rowIndex++);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue("Plantilla oficial SDRERC - Carga diaria");
        titleCell.setCellStyle(titleStyle);

        Row version = sheet.createRow(rowIndex++);
        version.createCell(0).setCellValue("Version");
        version.createCell(1).setCellValue(VERSION_PLANTILLA);

        rowIndex++;
        String[] instrucciones = {
            "Complete la informacion en la hoja CARGA_DIARIA desde la fila 2.",
            "No cambie los nombres de las columnas.",
            "No elimine columnas. Puede dejar OBSERVACION INICIAL vacia si no aplica.",
            "FECHA DE SOLICITUD debe ingresarse en formato dd/MM/yyyy.",
            "TIPO DOCUMENTO IDENTIDAD SOLICITANTE permite DNI, RUC, CE o PASAPORTE.",
            "N° DOCUMENTO IDENTIDAD SOLICITANTE reemplaza al campo DNI SOLICITANTE. Si no existe DNI, puede usar SIN DNI y el importador lo guardara vacio.",
            "TIPO DOCUMENTO IDENTIDAD TITULAR permite DNI, CE o PASAPORTE.",
            "N° DOCUMENTO IDENTIDAD TITULAR debe completarse si se conoce. Si TITULAR es igual a SOLICITADO POR, el importador copia el documento del solicitante cuando falte.",
            "Reglas de identidad: DNI = 8 numeros, RUC = 11 numeros, CE = hasta 12 alfanumericos, PASAPORTE = hasta 12 alfanumericos.",
            "N° TRAMITE WEB puede quedar como SIN TRAMITE si no existe referencia web.",
            "Si N° TRAMITE WEB contiene numeros, el canal se deriva como MPV.",
            "Si N° TRAMITE WEB es SIN TRAMITE y el documento del solicitante contiene numeros, el canal se deriva como MP PRESENCIAL.",
            "Si N° TRAMITE WEB es SIN TRAMITE y el documento del solicitante esta vacio, SOLICITADO POR permite derivar OR o INTERNO segun el origen RENIEC informado.",
            "N° DOCUMENTO corresponde al numero del documento recibido y se guarda como metadata documental.",
            "TIPO DE SOLICITUD debe corresponder a Parte u Oficio segun el documento recibido.",
            "PROCEDIMIENTO REGISTRAL, TIPO DE ACTA, TIPO DOCUMENTO y TIPO DE SOLICITUD tienen lista desplegable en la plantilla.",
            "TIPO DE ACTA y N° ACTA se usan para identificar el acta registral.",
            "TITULAR y N° ACTA se usan para detectar posibles duplicados.",
            "Los duplicados se registran para trazabilidad y no generan nuevo numero de expediente hasta su asociacion en Asignacion.",
            "Despues de completar el archivo, use Seleccionar archivo y Previsualizar en Registro / Recepcion."
        };

        for (String instruccion : instrucciones) {
            Row row = sheet.createRow(rowIndex++);
            Cell cell = row.createCell(0);
            cell.setCellValue(instruccion);
            cell.setCellStyle(textStyle);
        }

        sheet.setColumnWidth(0, 95 * 256);
        sheet.setColumnWidth(1, 38 * 256);
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
        style.setWrapText(true);

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

    private CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = crearEstiloTexto(workbook);
        CreationHelper helper = workbook.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("dd/mm/yyyy"));
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
