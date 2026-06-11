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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CargaDiariaPlantillaService {

    public static final String NOMBRE_ARCHIVO = "plantilla_carga_diaria_sdrerc.xlsx";

    private static final String VERSION_PLANTILLA = "PLANTILLA_SDRERC_CARGA_DIARIA_V2";
    private static final String HOJA_CARGA = "CARGA_DIARIA";
    private static final String HOJA_INSTRUCCIONES = "INSTRUCCIONES";

    private static final String[] COLUMNAS = {
        "TIPO DE SOLICITUD",
        "FECHA DE SOLICITUD",
        "SOLICITADO POR",
        "N° TRÁMITE WEB",
        "TIPO DOCUMENTO",
        "N° DOCUMENTO",
        "PROCEDIMIENTO REGISTRAL",
        "TIPO DE ACTA",
        "N° ACTA",
        "TITULAR",
        "OBSERVACIÓN INICIAL"
    };

    private static final int[] ANCHOS = {
        22, 20, 36, 28, 22, 28, 34, 22, 20, 38, 44
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

            crearHojaCarga(workbook, headerStyle, textStyle, dateStyle);
            crearHojaInstrucciones(workbook, titleStyle, textStyle);
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
            "N° TRAMITE WEB puede quedar como SIN TRAMITE si no existe referencia web.",
            "N° DOCUMENTO corresponde al numero del documento recibido y se guarda como metadata documental.",
            "TIPO DE SOLICITUD debe corresponder a Parte u Oficio segun el documento recibido.",
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
