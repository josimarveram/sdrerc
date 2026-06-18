package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.FeriadoNacionalDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import com.sdrerc.infrastructure.sdrercapp.dao.FeriadoNacionalDAO;
import com.sdrerc.shared.session.SessionContext;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FeriadoNacionalService {

    private static final DateTimeFormatter DATE_FORMAT_SLASH = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FORMAT_DASH = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final FeriadoNacionalDAO feriadoNacionalDAO;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public FeriadoNacionalService() {
        this(new FeriadoNacionalDAO(), new UsuarioAsignacionService());
    }

    public FeriadoNacionalService(FeriadoNacionalDAO feriadoNacionalDAO, UsuarioAsignacionService usuarioAsignacionService) {
        this.feriadoNacionalDAO = feriadoNacionalDAO;
        this.usuarioAsignacionService = usuarioAsignacionService;
    }

    public List<FeriadoNacionalDTO> buscar(Integer anio, Boolean activo, int limite) throws SQLException {
        return feriadoNacionalDAO.buscar(anio, activo, limite);
    }

    public FeriadoNacionalDTO guardar(FeriadoNacionalDTO feriado) throws SQLException {
        validarFeriado(feriado);
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idUsuario = resolverUsuarioActualSdrercApp();
                if (feriadoNacionalDAO.existeFecha(conn, feriado.getFecha(), feriado.getTipo(), feriado.getIdFeriado())) {
                    throw new IllegalArgumentException("Ya existe un feriado registrado para la fecha y tipo indicados.");
                }
                FeriadoNacionalDTO resultado = feriado.getIdFeriado() == null
                        ? feriadoNacionalDAO.insertar(conn, feriado, idUsuario)
                        : feriadoNacionalDAO.actualizar(conn, feriado, idUsuario);
                conn.commit();
                return resultado;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public FeriadoNacionalDTO cambiarActivo(Long idFeriado, boolean activo) throws SQLException {
        if (idFeriado == null) {
            throw new IllegalArgumentException("Seleccione un feriado.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                FeriadoNacionalDTO resultado = feriadoNacionalDAO.cambiarActivo(
                        conn,
                        idFeriado,
                        activo,
                        resolverUsuarioActualSdrercApp());
                conn.commit();
                return resultado;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public int importarXmlPorAnio(File archivoXml, int anio) throws SQLException {
        List<FeriadoNacionalDTO> items = leerFeriadosXml(archivoXml, anio);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("El archivo XML no contiene feriados para importar.");
        }

        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idUsuario = resolverUsuarioActualSdrercApp();
                for (FeriadoNacionalDTO feriado : items) {
                    if (feriadoNacionalDAO.existeFecha(conn, feriado.getFecha(), feriado.getTipo(), null)) {
                        throw new IllegalArgumentException("Ya existe un feriado registrado para "
                                + DATE_FORMAT_SLASH.format(feriado.getFecha()) + " (" + feriado.getTipo() + ").");
                    }
                }
                int importados = 0;
                for (FeriadoNacionalDTO feriado : items) {
                    feriadoNacionalDAO.insertar(conn, feriado, idUsuario);
                    importados++;
                }
                conn.commit();
                return importados;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private List<FeriadoNacionalDTO> leerFeriadosXml(File archivoXml, int anio) {
        validarArchivoXml(archivoXml);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);

            Document document = factory.newDocumentBuilder().parse(archivoXml);
            Element root = document.getDocumentElement();
            if (root == null) {
                throw new IllegalArgumentException("El XML no contiene datos.");
            }
            validarAnioRaiz(root, anio);

            NodeList nodes = root.getElementsByTagName("feriado");
            if (nodes.getLength() == 0) {
                nodes = root.getElementsByTagName("holiday");
            }
            List<FeriadoNacionalDTO> result = new ArrayList<FeriadoNacionalDTO>();
            Set<String> fechasArchivo = new HashSet<String>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (!(node instanceof Element)) {
                    continue;
                }
                FeriadoNacionalDTO feriado = leerFeriadoXml((Element) node, i + 1, anio);
                validarFeriado(feriado);
                String key = feriado.getFecha().toString() + "|" + feriado.getTipo().trim().toUpperCase(Locale.ROOT);
                if (!fechasArchivo.add(key)) {
                    throw new IllegalArgumentException("El XML contiene feriados duplicados para "
                            + DATE_FORMAT_SLASH.format(feriado.getFecha()) + " (" + feriado.getTipo() + ").");
                }
                result.add(feriado);
            }
            return result;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("No se pudo leer el XML de feriados: " + ex.getMessage(), ex);
        }
    }

    private void validarArchivoXml(File archivoXml) {
        if (archivoXml == null || !archivoXml.isFile()) {
            throw new IllegalArgumentException("Seleccione un archivo XML válido.");
        }
        if (!archivoXml.getName().toLowerCase(Locale.ROOT).endsWith(".xml")) {
            throw new IllegalArgumentException("El archivo debe tener extensión .xml.");
        }
    }

    private void validarAnioRaiz(Element root, int anio) {
        String anioXml = firstNonBlank(
                root.getAttribute("anio"),
                root.getAttribute("año"),
                root.getAttribute("year"),
                textFromFirst(root, "anio"),
                textFromFirst(root, "año"),
                textFromFirst(root, "year"));
        if (!isBlank(anioXml)) {
            int parsed = parseInt(anioXml.trim(), "El año del XML no es válido.");
            if (parsed != anio) {
                throw new IllegalArgumentException("El XML corresponde al año " + parsed
                        + ", pero el filtro seleccionado es " + anio + ".");
            }
        }
    }

    private FeriadoNacionalDTO leerFeriadoXml(Element element, int index, int anio) {
        FeriadoNacionalDTO feriado = new FeriadoNacionalDTO();
        LocalDate fecha = parseFecha(firstNonBlank(
                element.getAttribute("fecha"),
                element.getAttribute("date"),
                textFromFirst(element, "fecha"),
                textFromFirst(element, "date")), index);
        if (fecha.getYear() != anio) {
            throw new IllegalArgumentException("El feriado de la fila " + index
                    + " tiene fecha fuera del año seleccionado: " + DATE_FORMAT_SLASH.format(fecha) + ".");
        }

        feriado.setFecha(fecha);
        feriado.setNombre(firstNonBlank(
                element.getAttribute("nombre"),
                element.getAttribute("name"),
                element.getAttribute("motivo"),
                element.getAttribute("reason"),
                textFromFirst(element, "nombre"),
                textFromFirst(element, "name"),
                textFromFirst(element, "motivo"),
                textFromFirst(element, "reason")));
        feriado.setTipo(firstNonBlank(
                element.getAttribute("tipo"),
                element.getAttribute("type"),
                textFromFirst(element, "tipo"),
                textFromFirst(element, "type"),
                "NACIONAL"));
        feriado.setActivo(parseActivo(firstNonBlank(
                element.getAttribute("activo"),
                element.getAttribute("active"),
                textFromFirst(element, "activo"),
                textFromFirst(element, "active")), true));
        feriado.setObservacion(firstNonBlank(
                element.getAttribute("observacion"),
                element.getAttribute("observación"),
                element.getAttribute("observation"),
                textFromFirst(element, "observacion"),
                textFromFirst(element, "observación"),
                textFromFirst(element, "observation")));
        return feriado;
    }

    private LocalDate parseFecha(String value, int index) {
        if (isBlank(value)) {
            throw new IllegalArgumentException("El feriado de la fila " + index + " no tiene fecha.");
        }
        String fecha = value.trim();
        try {
            return fecha.contains("/") ? LocalDate.parse(fecha, DATE_FORMAT_SLASH) : LocalDate.parse(fecha, DATE_FORMAT_DASH);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Fecha inválida en feriado " + index + ": " + fecha
                    + ". Use dd/MM/yyyy o yyyy-MM-dd.");
        }
    }

    private boolean parseActivo(String value, boolean defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return "1".equals(normalized)
                || "SI".equals(normalized)
                || "SÍ".equals(normalized)
                || "TRUE".equals(normalized)
                || "ACTIVO".equals(normalized);
    }

    private static String textFromFirst(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes == null || nodes.getLength() == 0) {
            return "";
        }
        Node node = nodes.item(0);
        return node == null ? "" : node.getTextContent();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private static int parseInt(String value, String message) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validarFeriado(FeriadoNacionalDTO feriado) {
        if (feriado == null) {
            throw new IllegalArgumentException("Complete los datos del feriado.");
        }
        if (feriado.getFecha() == null) {
            throw new IllegalArgumentException("Ingrese la fecha del feriado.");
        }
        if (feriado.getFecha().isBefore(LocalDate.of(2000, 1, 1))) {
            throw new IllegalArgumentException("Ingrese una fecha válida para el feriado.");
        }
        if (feriado.getNombre() == null || feriado.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese el nombre del feriado.");
        }
        if (feriado.getNombre().trim().length() > 150) {
            throw new IllegalArgumentException("El nombre del feriado no debe exceder 150 caracteres.");
        }
        if (feriado.getObservacion() != null && feriado.getObservacion().trim().length() > 300) {
            throw new IllegalArgumentException("La observación no debe exceder 300 caracteres.");
        }
        feriado.setNombre(feriado.getNombre());
        feriado.setTipo(feriado.getTipo());
        feriado.setObservacion(feriado.getObservacion());
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Long resolverUsuarioActualSdrercApp() {
        try {
            String username = SessionContext.getUsername();
            return usuarioAsignacionService.obtenerIdUsuarioActivoPorUsername(username);
        } catch (Exception ex) {
            return null;
        }
    }
}
