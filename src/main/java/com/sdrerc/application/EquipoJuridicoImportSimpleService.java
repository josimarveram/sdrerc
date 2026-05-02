package com.sdrerc.application;

import com.sdrerc.domain.model.EquipoJuridicoExcelRow;
import com.sdrerc.domain.model.EquipoJuridicoImportResult;
import com.sdrerc.domain.model.EquipoJuridicoImportItem;
import com.sdrerc.domain.model.EquipoJuridicoImportPreview;
import com.sdrerc.domain.model.EquipoJuridicoImportRowResult;
import com.sdrerc.domain.model.EquipoJuridicoRegistro;
import com.sdrerc.infrastructure.database.OracleConnection;
import java.io.File;
import java.io.InputStream;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EquipoJuridicoImportSimpleService {

    private static final String VERSION_PLANTILLA = "PLANTILLA_SDRERC_EQUIPO_JURIDICO_V2";
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
    private static final String PASSWORD_TEMPORAL_IMPORTACION = "Reniec@2026";

    private final EquipoJuridicoService equipoJuridicoService = new EquipoJuridicoService();

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

    public EquipoJuridicoImportResult confirmarImportacion(
            EquipoJuridicoImportPreview preview,
            boolean incluirAdvertencias) {

        EquipoJuridicoImportResult result = new EquipoJuridicoImportResult();
        Set<String> usernamesReservados = new HashSet<>();
        Map<String, String> usernamesPorPersona = new HashMap<>();

        for (EquipoJuridicoImportItem item : preview.getItems()) {
            EquipoJuridicoImportRowResult rowResult = new EquipoJuridicoImportRowResult();
            rowResult.setItem(item.getItem());
            rowResult.setAbogado(item.getAbogado());
            rowResult.setSupervisor(item.getSupervisor());

            if (item.tieneErrores()) {
                rowResult.setEstado("OMITIDO");
                rowResult.setMensaje("Fila omitida por errores de validación.");
                result.getResultadosPorFila().add(rowResult);
                continue;
            }
            if (item.tieneAdvertencias()) {
                result.setConAdvertencias(result.getConAdvertencias() + 1);
            }
            if (item.tieneAdvertencias() && !incluirAdvertencias) {
                rowResult.setEstado("OMITIDO");
                rowResult.setMensaje("Fila omitida por advertencias no confirmadas.");
                result.getResultadosPorFila().add(rowResult);
                continue;
            }

            try (Connection conn = OracleConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    ParsedName abogado = parsearNombreInstitucional(item.getAbogado(), "ABOGADO", null);
                    ParsedName supervisor = isBlank(item.getSupervisor())
                            ? null
                            : parsearNombreInstitucional(item.getSupervisor(), "SUPERVISOR", null);

                    EquipoJuridicoRegistro abogadoRegistro = crearRegistro(abogado);
                    abogadoRegistro.setUsername(generarUsernameSugerido(abogado, usernamesReservados, usernamesPorPersona));
                    abogadoRegistro.setAbogado(true);

                    EquipoJuridicoRegistro supervisorRegistro = null;
                    if (supervisor != null) {
                        supervisorRegistro = crearRegistro(supervisor);
                        supervisorRegistro.setUsername(generarUsernameSugerido(supervisor, usernamesReservados, usernamesPorPersona));
                        supervisorRegistro.setSupervision(true);
                    }

                    EquipoJuridicoImportRowResult imported = equipoJuridicoService.importarFila(
                            conn,
                            item.getItem(),
                            abogadoRegistro,
                            supervisorRegistro
                    );
                    conn.commit();
                    result.getResultadosPorFila().add(imported);
                } catch (Exception ex) {
                    conn.rollback();
                    rowResult.setEstado("ERROR");
                    rowResult.setMensaje(ex.getMessage());
                    result.getResultadosPorFila().add(rowResult);
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (Exception ex) {
                rowResult.setEstado("ERROR");
                rowResult.setMensaje(ex.getMessage());
                result.getResultadosPorFila().add(rowResult);
            }
        }

        result.recalcularTotales();
        return result;
    }

    public List<EquipoJuridicoExcelRow> leerExcel(File archivo) throws Exception {
        validarArchivo(archivo);
        try (ZipFile zip = new ZipFile(archivo)) {
            List<String> sharedStrings = leerSharedStrings(zip);
            validarVersionSiExiste(zip, sharedStrings);
            List<Map<Integer, String>> rows = leerSheet(zip, "xl/worksheets/sheet1.xml", sharedStrings);
            validarEstructura(rows);

            List<EquipoJuridicoExcelRow> result = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                Map<Integer, String> data = rows.get(i);
                if (filaVacia(data)) {
                    continue;
                }
                EquipoJuridicoExcelRow row = new EquipoJuridicoExcelRow();
                row.setRowNumber(i + 1);
                row.setItem(value(data, 0));
                row.setAbogado(value(data, 1));
                row.setSupervisor(value(data, 2));
                row.setPersonal(value(data, 3));
                row.setEstado(value(data, 4));
                result.add(row);
            }
            return result;
        }
    }

    private List<Map<Integer, String>> leerSheet(ZipFile zip, String path, List<String> sharedStrings) throws Exception {
        ZipEntry entry = zip.getEntry(path);
        if (entry == null) {
            throw new IllegalArgumentException("No se pudo leer la hoja EQUIPO_JURIDICO del archivo.");
        }

        List<Map<Integer, String>> rows = new ArrayList<>();
        Document doc = parse(zip, entry);
        NodeList rowNodes = doc.getElementsByTagName("row");
        for (int i = 0; i < rowNodes.getLength(); i++) {
            Element rowElement = (Element) rowNodes.item(i);
            int rowIndex = parseInt(rowElement.getAttribute("r"), i + 1) - 1;
            while (rows.size() <= rowIndex) {
                rows.add(new HashMap<>());
            }
            Map<Integer, String> row = rows.get(rowIndex);
            NodeList cellNodes = rowElement.getElementsByTagName("c");
            for (int c = 0; c < cellNodes.getLength(); c++) {
                Element cell = (Element) cellNodes.item(c);
                int col = columnIndex(cell.getAttribute("r"));
                row.put(col, leerCelda(cell, sharedStrings));
            }
        }
        return rows;
    }

    private String leerCelda(Element cell, List<String> sharedStrings) {
        String type = cell.getAttribute("t");
        if ("inlineStr".equals(type)) {
            return textFromFirst(cell, "t");
        }

        String value = textFromFirst(cell, "v");
        if ("s".equals(type) && !isBlank(value)) {
            int index = parseInt(value, -1);
            if (index >= 0 && index < sharedStrings.size()) {
                return sharedStrings.get(index);
            }
        }
        return value == null ? "" : value.trim();
    }

    private List<String> leerSharedStrings(ZipFile zip) throws Exception {
        List<String> values = new ArrayList<>();
        ZipEntry entry = zip.getEntry("xl/sharedStrings.xml");
        if (entry == null) {
            return values;
        }

        Document doc = parse(zip, entry);
        NodeList siNodes = doc.getElementsByTagName("si");
        for (int i = 0; i < siNodes.getLength(); i++) {
            Element si = (Element) siNodes.item(i);
            NodeList textNodes = si.getElementsByTagName("t");
            StringBuilder text = new StringBuilder();
            for (int t = 0; t < textNodes.getLength(); t++) {
                text.append(textNodes.item(t).getTextContent());
            }
            values.add(text.toString());
        }
        return values;
    }

    private Document parse(ZipFile zip, ZipEntry entry) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        try (InputStream in = zip.getInputStream(entry)) {
            return factory.newDocumentBuilder().parse(in);
        }
    }

    private void validarVersionSiExiste(ZipFile zip, List<String> sharedStrings) throws Exception {
        ZipEntry entry = zip.getEntry("xl/worksheets/sheet2.xml");
        if (entry == null) {
            return;
        }
        List<Map<Integer, String>> rows = leerSheet(zip, "xl/worksheets/sheet2.xml", sharedStrings);
        boolean encontroVersion = false;
        for (Map<Integer, String> row : rows) {
            for (String value : row.values()) {
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

    private void validarEstructura(List<Map<Integer, String>> rows) {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("La hoja EQUIPO_JURIDICO no tiene cabecera.");
        }
        Map<Integer, String> header = rows.get(0);
        for (int i = 0; i < COLUMNAS_REQUERIDAS.size(); i++) {
            String actual = normalizarNombre(value(header, i));
            String esperado = COLUMNAS_REQUERIDAS.get(i);
            if (!esperado.equals(actual)) {
                throw new IllegalArgumentException("Columna invalida en posicion " + (i + 1)
                        + ". Se esperaba " + esperado + " y se encontro " + actual + ".");
            }
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

        String estado = normalizarNombre(row.getEstado());
        if (isBlank(estado)) {
            estado = "ACTIVO";
            item.agregarAdvertencia("ESTADO vacio; se asumira ACTIVO en la futura importacion.");
        } else if (!ESTADOS_VALIDOS.contains(estado)) {
            item.agregarError("ESTADO no reconocido. Use ACTIVO o INACTIVO.");
        }
        item.setEstado(estado);

        String personal = normalizarNombre(row.getPersonal());
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
                agregarError(item, "ABOGADO es obligatorio.");
            }
            return null;
        }
        String[] tokens = normalizarNombre(nombre).split(" ");
        if (tokens.length == 1) {
            agregarError(item, campo + " no tiene formato valido. Debe usar APELLIDO_PATERNO APELLIDO_MATERNO NOMBRES.");
            return null;
        }
        if (tokens.length == 2) {
            agregarAdvertencia(item, campo + " tiene solo 2 palabras; falta apellido materno o nombres completos.");
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

    private void agregarError(EquipoJuridicoImportItem item, String mensaje) {
        if (item == null) {
            throw new IllegalArgumentException(mensaje);
        }
        item.agregarError(mensaje);
    }

    private void agregarAdvertencia(EquipoJuridicoImportItem item, String mensaje) {
        if (item != null) {
            item.agregarAdvertencia(mensaje);
        }
    }

    private EquipoJuridicoRegistro crearRegistro(ParsedName name) {
        EquipoJuridicoRegistro registro = new EquipoJuridicoRegistro();
        registro.setApellidoPaterno(name.apellidoPaterno);
        registro.setApellidoMaterno(name.apellidoMaterno);
        registro.setNombres(name.nombres);
        registro.setPasswordTemporal(PASSWORD_TEMPORAL_IMPORTACION);
        return registro;
    }

    public String normalizarNombre(String nombre) {
        if (nombre == null) {
            return "";
        }
        return Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
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

    private boolean filaVacia(Map<Integer, String> row) {
        for (int i = 0; i < COLUMNAS_REQUERIDAS.size(); i++) {
            if (!isBlank(value(row, i))) {
                return false;
            }
        }
        return true;
    }

    private String value(Map<Integer, String> row, int index) {
        String value = row.get(index);
        return value == null ? "" : value.trim();
    }

    private String textFromFirst(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return "";
        }
        Node node = nodes.item(0);
        return node == null ? "" : node.getTextContent().trim();
    }

    private int columnIndex(String cellRef) {
        int result = 0;
        for (int i = 0; i < cellRef.length(); i++) {
            char ch = cellRef.charAt(i);
            if (ch < 'A' || ch > 'Z') {
                break;
            }
            result = result * 26 + (ch - 'A' + 1);
        }
        return Math.max(0, result - 1);
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return defaultValue;
        }
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
