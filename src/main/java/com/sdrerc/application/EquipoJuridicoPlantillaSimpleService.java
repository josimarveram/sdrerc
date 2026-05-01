package com.sdrerc.application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EquipoJuridicoPlantillaSimpleService {

    private static final String VERSION_PLANTILLA = "PLANTILLA_SDRERC_EQUIPO_JURIDICO_V2";
    private static final String[] COLUMNAS = {
        "ITEM",
        "ABOGADO",
        "SUPERVISOR",
        "PERSONAL",
        "ESTADO"
    };

    public void generarPlantilla(File destino) throws IOException {
        if (destino == null) {
            throw new IllegalArgumentException("Seleccione una ruta de destino.");
        }

        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(destino))) {
            agregar(zip, "[Content_Types].xml", contentTypes());
            agregar(zip, "_rels/.rels", rootRels());
            agregar(zip, "xl/workbook.xml", workbook());
            agregar(zip, "xl/_rels/workbook.xml.rels", workbookRels());
            agregar(zip, "xl/styles.xml", styles());
            agregar(zip, "xl/worksheets/sheet1.xml", equipoJuridicoSheet());
            agregar(zip, "xl/worksheets/sheet2.xml", instruccionesSheet());
            agregar(zip, "xl/worksheets/sheet3.xml", catalogosSheet());
        }
    }

    private void agregar(ZipOutputStream zip, String path, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
            + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
            + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
            + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
            + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
            + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
            + "<Override PartName=\"/xl/worksheets/sheet2.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
            + "<Override PartName=\"/xl/worksheets/sheet3.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
            + "</Types>";
    }

    private String rootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
            + "</Relationships>";
    }

    private String workbook() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
            + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
            + "<sheets>"
            + "<sheet name=\"EQUIPO_JURIDICO\" sheetId=\"1\" r:id=\"rId1\"/>"
            + "<sheet name=\"INSTRUCCIONES\" sheetId=\"2\" r:id=\"rId2\"/>"
            + "<sheet name=\"CATALOGOS\" sheetId=\"3\" state=\"hidden\" r:id=\"rId3\"/>"
            + "</sheets>"
            + "</workbook>";
    }

    private String workbookRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
            + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet2.xml\"/>"
            + "<Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet3.xml\"/>"
            + "<Relationship Id=\"rId4\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
            + "</Relationships>";
    }

    private String styles() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
            + "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font><font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font></fonts>"
            + "<fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill><fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF1F4E78\"/><bgColor indexed=\"64\"/></patternFill></fill></fills>"
            + "<borders count=\"2\"><border/><border><left style=\"thin\"/><right style=\"thin\"/><top style=\"thin\"/><bottom style=\"thin\"/></border></borders>"
            + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
            + "<cellXfs count=\"3\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
            + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>"
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment vertical=\"center\" wrapText=\"1\"/></xf></cellXfs>"
            + "</styleSheet>";
    }

    private String equipoJuridicoSheet() {
        StringBuilder xml = new StringBuilder();
        xml.append(sheetStart());
        xml.append("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>");
        xml.append(columns(new int[]{8, 38, 38, 24, 16}));
        xml.append("<sheetData>");
        xml.append(row(1, COLUMNAS, 1));
        xml.append(row(2, new String[]{"1", "ALVARADO CAZORLA JOSE PAUL", "SANTIAGO RAMIREZ JULIO", "PERSONAL PLANTA", "ACTIVO"}, 2));
        xml.append(row(3, new String[]{"2", "ALVAREZ GARCIA ANA CAROLINA", "SANTIAGO RAMIREZ JULIO", "CAS ELECTORAL", "ACTIVO"}, 2));
        xml.append(row(4, new String[]{"3", "VERA MIRANDA JOSIMAR", "LOPEZ RAMOS MARIA", "PERSONAL OR", "ACTIVO"}, 2));
        xml.append("</sheetData>");
        xml.append("<autoFilter ref=\"A1:E1\"/>");
        xml.append(validations());
        xml.append("</worksheet>");
        return xml.toString();
    }

    private String instruccionesSheet() {
        String[] instrucciones = {
            "Plantilla oficial SDRERC - Equipo Juridico",
            "Version: " + VERSION_PLANTILLA,
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
        StringBuilder xml = new StringBuilder();
        xml.append(sheetStart());
        xml.append(columns(new int[]{90}));
        xml.append("<sheetData>");
        for (int i = 0; i < instrucciones.length; i++) {
            xml.append(row(i + 1, new String[]{instrucciones[i]}, i == 0 ? 1 : 2));
        }
        xml.append("</sheetData></worksheet>");
        return xml.toString();
    }

    private String catalogosSheet() {
        String[][] rows = {
            {"ESTADO", "PERSONAL"},
            {"ACTIVO", "PERSONAL PLANTA"},
            {"INACTIVO", "PERSONAL OR"},
            {"", "CAS ELECTORAL"},
            {"", "OTRO"}
        };
        StringBuilder xml = new StringBuilder();
        xml.append(sheetStart());
        xml.append(columns(new int[]{18, 24}));
        xml.append("<sheetData>");
        for (int i = 0; i < rows.length; i++) {
            xml.append(row(i + 1, rows[i], i == 0 ? 1 : 2));
        }
        xml.append("</sheetData></worksheet>");
        return xml.toString();
    }

    private String sheetStart() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">";
    }

    private String columns(int[] widths) {
        StringBuilder xml = new StringBuilder("<cols>");
        for (int i = 0; i < widths.length; i++) {
            int col = i + 1;
            xml.append("<col min=\"").append(col).append("\" max=\"").append(col)
                .append("\" width=\"").append(widths[i]).append("\" customWidth=\"1\"/>");
        }
        xml.append("</cols>");
        return xml.toString();
    }

    private String row(int index, String[] values, int style) {
        StringBuilder xml = new StringBuilder();
        xml.append("<row r=\"").append(index).append("\">");
        for (int i = 0; i < values.length; i++) {
            xml.append(cell(colName(i + 1) + index, values[i], style));
        }
        xml.append("</row>");
        return xml.toString();
    }

    private String cell(String ref, String value, int style) {
        return "<c r=\"" + ref + "\" t=\"inlineStr\" s=\"" + style + "\"><is><t>"
            + escape(value)
            + "</t></is></c>";
    }

    private String validations() {
        return "<dataValidations count=\"2\">"
            + validation("D2:D1000", "\"PERSONAL PLANTA,PERSONAL OR,CAS ELECTORAL,OTRO\"")
            + validation("E2:E1000", "\"ACTIVO,INACTIVO\"")
            + "</dataValidations>";
    }

    private String validation(String range, String formula) {
        return "<dataValidation type=\"list\" allowBlank=\"1\" showErrorMessage=\"1\" sqref=\"" + range + "\">"
            + "<formula1>" + formula + "</formula1>"
            + "</dataValidation>";
    }

    private String colName(int index) {
        StringBuilder name = new StringBuilder();
        while (index > 0) {
            int rem = (index - 1) % 26;
            name.insert(0, (char) ('A' + rem));
            index = (index - 1) / 26;
        }
        return name.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
