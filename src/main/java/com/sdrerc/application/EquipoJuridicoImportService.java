package com.sdrerc.application;

import com.sdrerc.domain.model.EquipoJuridicoExcelRow;
import com.sdrerc.domain.model.EquipoJuridicoImportItem;
import com.sdrerc.domain.model.EquipoJuridicoImportPreview;
import com.sdrerc.infrastructure.database.OracleConnection;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class EquipoJuridicoImportService {

    private static final String VERSION_PLANTILLA = "PLANTILLA_SDRERC_EQUIPO_JURIDICO_V2";
    private static final String HOJA_EQUIPO_JURIDICO = "EQUIPO_JURIDICO";
    private static final List<String> COLUMNAS_REQUERIDAS = Arrays.asList(
            "ITEM",
            "ABOGADO",
            "SUPERVISOR",
            "PERSONAL",
            "ESTADO"
    );
    private static final Set<String> ESTADOS_VALIDOS = new HashSet<>(Arrays.asList("ACTIVO", "INACTIVO"));
    private static final Set<String> PERSONAL_VALIDO = new HashSet<>(Arrays.asList(
            "PERSONAL PLANTA",
            "PERSONAL OR",
            "CAS ELECTORAL",
            "OTRO"
    ));

    public EquipoJuridicoImportPreview previsualizar(File archivo) throws Exception {
        List<EquipoJuridicoExcelRow> rows = leerExcel(archivo);
        EquipoJuridicoImportPreview preview = new EquipoJuridicoImportPreview();
        Map<String, EquipoJuridicoImportItem> abogadosPorNombre = new HashMap<>();
        Map<String, String> supervisorPorAbogado = new HashMap<>();
        Set<String> usernamesReservados = new HashSet<>();
        Map<String, String> usernamesPorPersona = new HashMap<>();

        for (EquipoJuridicoExcelRow row : rows) {
            EquipoJuridicoImportItem item = validarFila(row, usernamesReservados, usernamesPorPersona);
            String abogadoNormalizado = normalizarNombre(row.getAbogado());
            String supervisorNormalizado = normalizarNombre(row.getSupervisor());

            if (!isBlank(abogadoNormalizado)) {
                if (abogadosPorNombre.containsKey(abogadoNormalizado)) {
                    item.agregarError("El abogado aparece duplicado dentro del Excel.");
                    item.setAccionPrevista("ERROR");
                } else {
                    abogadosPorNombre.put(abogadoNormalizado, item);
                }

                String supervisorPrevio = supervisorPorAbogado.get(abogadoNormalizado);
                if (supervisorPrevio != null && !supervisorPrevio.equals(supervisorNormalizado)) {
                    item.agregarError("El abogado tiene dos supervisores distintos dentro del Excel.");
                    item.setAccionPrevista("ERROR");
                } else if (!isBlank(supervisorNormalizado)) {
                    supervisorPorAbogado.put(abogadoNormalizado, supervisorNormalizado);
                }
            }

            if (!item.tieneErrores() && isBlank(item.getAccionPrevista())) {
                item.setAccionPrevista(isBlank(item.getSupervisor())
                        ? "CREAR_ABOGADO"
                        : "CREAR_ABOGADO; CREAR_SUPERVISOR; ASIGNAR_SUPERVISOR");
            }

            preview.getItems().add(item);
        }

        preview.recalcularTotales();
        return preview;
    }

    public List<EquipoJuridicoExcelRow> leerExcel(File archivo) throws Exception {
        validarArchivo(archivo);

        try (FileInputStream input = new FileInputStream(archivo);
             Workbook workbook = new XSSFWorkbook(input)) {

            validarVersion(workbook);
            Sheet sheet = workbook.getSheet(HOJA_EQUIPO_JURIDICO);
            if (sheet == null) {
                throw new IllegalArgumentException("No existe la hoja requerida EQUIPO_JURIDICO.");
            }
            validarEstructura(sheet);

            DataFormatter formatter = new DataFormatter();
            List<EquipoJuridicoExcelRow> rows = new ArrayList<>();
            int lastRow = sheet.getLastRowNum();
            for (int i = 1; i <= lastRow; i++) {
                Row excelRow = sheet.getRow(i);
                if (excelRow == null || filaVacia(excelRow, formatter)) {
                    continue;
                }

                EquipoJuridicoExcelRow row = new EquipoJuridicoExcelRow();
                row.setRowNumber(i + 1);
                row.setItem(getCellValue(excelRow, 0, formatter));
                row.setAbogado(getCellValue(excelRow, 1, formatter));
                row.setSupervisor(getCellValue(excelRow, 2, formatter));
                row.setPersonal(getCellValue(excelRow, 3, formatter));
                row.setEstado(getCellValue(excelRow, 4, formatter));
                rows.add(row);
            }
            return rows;
        }
    }

    private EquipoJuridicoImportItem validarFila(
            EquipoJuridicoExcelRow row,
            Set<String> usernamesReservados,
            Map<String, String> usernamesPorPersona) throws Exception {
        EquipoJuridicoImportItem item = new EquipoJuridicoImportItem();
        item.setItem(row.getItem());
        item.setAbogado(trimToEmpty(row.getAbogado()));
        item.setSupervisor(trimToEmpty(row.getSupervisor()));
        item.setPersonal(trimToEmpty(row.getPersonal()));

        String estado = normalizarSimple(row.getEstado());
        if (isBlank(estado)) {
            estado = "ACTIVO";
            item.agregarAdvertencia("ESTADO vacio; se asumira ACTIVO en la futura importacion.");
        } else if (!ESTADOS_VALIDOS.contains(estado)) {
            item.agregarError("ESTADO no reconocido. Use ACTIVO o INACTIVO.");
        }
        item.setEstado(estado);

        String personal = normalizarSimple(row.getPersonal());
        if (isBlank(personal)) {
            item.agregarAdvertencia("PERSONAL vacio; se tratara como dato informativo no registrado.");
        } else if (!PERSONAL_VALIDO.contains(personal)) {
            item.agregarAdvertencia("PERSONAL no reconocido; se mantendra solo como informacion de previsualizacion.");
        }

        ParsedName abogado = parsearNombreInstitucional(row.getAbogado(), "ABOGADO", item);
        ParsedName supervisor = null;
        if (isBlank(row.getSupervisor())) {
            item.agregarAdvertencia("SUPERVISOR vacio; el abogado quedara sin supervisor en la futura importacion.");
        } else {
            supervisor = parsearNombreInstitucional(row.getSupervisor(), "SUPERVISOR", item);
        }

        if (abogado != null) {
            item.setUsernameSugerido(generarUsernameSugerido(abogado, usernamesReservados, usernamesPorPersona));
        }
        if (supervisor != null) {
            generarUsernameSugerido(supervisor, usernamesReservados, usernamesPorPersona);
        }
        if (item.tieneErrores()) {
            item.setAccionPrevista("ERROR");
        }
        return item;
    }

    private ParsedName parsearNombreInstitucional(String nombre, String campo, EquipoJuridicoImportItem item) {
        if (isBlank(nombre)) {
            if ("ABOGADO".equals(campo)) {
                item.agregarError("ABOGADO es obligatorio.");
            }
            return null;
        }

        String normalizado = normalizarNombre(nombre);
        String[] tokens = normalizado.split(" ");
        if (tokens.length == 1) {
            item.agregarError(campo + " no tiene formato valido. Debe usar APELLIDO_PATERNO APELLIDO_MATERNO NOMBRES.");
            return null;
        }
        if (tokens.length == 2) {
            item.agregarAdvertencia(campo + " tiene solo 2 palabras; falta apellido materno o nombres completos.");
            return new ParsedName(tokens[0], "", tokens[1]);
        }

        StringBuilder nombres = new StringBuilder();
        for (int i = 2; i < tokens.length; i++) {
            if (nombres.length() > 0) {
                nombres.append(' ');
            }
            nombres.append(tokens[i]);
        }
        return new ParsedName(tokens[0], tokens[1], nombres.toString());
    }

    public String normalizarNombre(String nombre) {
        if (nombre == null) {
            return "";
        }
        String value = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return value;
    }

    private String normalizarSimple(String value) {
        return normalizarNombre(value);
    }

    private String generarUsernameSugerido(
            ParsedName name,
            Set<String> usernamesReservados,
            Map<String, String> usernamesPorPersona) throws Exception {
        String nombreNormalizado = name.apellidoPaterno + " " + name.apellidoMaterno + " " + name.nombres;
        if (usernamesPorPersona.containsKey(nombreNormalizado)) {
            return usernamesPorPersona.get(nombreNormalizado);
        }

        String[] nombres = name.nombres.split(" ");
        String primerNombre = nombres.length == 0 ? "" : nombres[0];
        String base = (primerNombre.isEmpty() ? "" : primerNombre.substring(0, 1))
                + name.apellidoPaterno
                + (name.apellidoMaterno.isEmpty() ? "" : name.apellidoMaterno.substring(0, 1));
        base = base.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        if (base.isEmpty()) {
            base = "usuario";
        }

        String candidato = base;
        int correlativo = 2;
        while (usernamesReservados.contains(candidato) || existeUsernameEnBd(candidato)) {
            candidato = base + correlativo;
            correlativo++;
        }
        usernamesReservados.add(candidato);
        usernamesPorPersona.put(nombreNormalizado, candidato);
        return candidato;
    }

    private boolean existeUsernameEnBd(String username) throws Exception {
        String sql = "SELECT COUNT(1) FROM APP_USERS WHERE UPPER(TRIM(USERNAME)) = UPPER(TRIM(?))";
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizarUsername(username));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private String normalizarUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private void validarArchivo(File archivo) {
        if (archivo == null) {
            throw new IllegalArgumentException("Seleccione un archivo.");
        }
        if (!archivo.exists()) {
            throw new IllegalArgumentException("El archivo seleccionado no existe.");
        }
        if (!archivo.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new IllegalArgumentException("El archivo debe tener extension .xlsx.");
        }
    }

    private void validarVersion(Workbook workbook) {
        Sheet sheet = workbook.getSheet("INSTRUCCIONES");
        if (sheet == null) {
            return;
        }
        DataFormatter formatter = new DataFormatter();
        boolean encontroVersion = false;
        for (Row row : sheet) {
            for (Cell cell : row) {
                String value = formatter.formatCellValue(cell);
                if (value != null && value.contains("PLANTILLA_SDRERC_EQUIPO_JURIDICO")) {
                    encontroVersion = true;
                    if (!value.contains(VERSION_PLANTILLA)) {
                        throw new IllegalArgumentException("La plantilla no corresponde a la version V2 oficial.");
                    }
                }
            }
        }
        if (!encontroVersion) {
            return;
        }
    }

    private void validarEstructura(Sheet sheet) {
        Row header = sheet.getRow(0);
        if (header == null) {
            throw new IllegalArgumentException("La hoja EQUIPO_JURIDICO no tiene cabecera.");
        }

        DataFormatter formatter = new DataFormatter();
        for (int i = 0; i < COLUMNAS_REQUERIDAS.size(); i++) {
            String actual = normalizarSimple(getCellValue(header, i, formatter));
            String esperado = COLUMNAS_REQUERIDAS.get(i);
            if (!esperado.equals(actual)) {
                throw new IllegalArgumentException("Columna invalida en posicion " + (i + 1)
                        + ". Se esperaba " + esperado + " y se encontro " + actual + ".");
            }
        }
    }

    private boolean filaVacia(Row row, DataFormatter formatter) {
        for (int i = 0; i < COLUMNAS_REQUERIDAS.size(); i++) {
            if (!isBlank(getCellValue(row, i, formatter))) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(Row row, int column, DataFormatter formatter) {
        Cell cell = row.getCell(column);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static class ParsedName {
        private final String apellidoPaterno;
        private final String apellidoMaterno;
        private final String nombres;

        private ParsedName(String apellidoPaterno, String apellidoMaterno, String nombres) {
            this.apellidoPaterno = apellidoPaterno;
            this.apellidoMaterno = apellidoMaterno;
            this.nombres = nombres;
        }
    }
}
