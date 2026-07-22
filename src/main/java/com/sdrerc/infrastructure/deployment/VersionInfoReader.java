package com.sdrerc.infrastructure.deployment;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.CodeSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Lee {@code version-local.json}, el archivo que {@code scripts/client/sdrerc-launcher.ps1} escribe
 * en {@code C:\SDRERC_CLIENTE\app} cada vez que aplica una actualizacion descargada desde
 * {@code D:\SDRERC_RELEASES\latest}. Permite mostrar en el login la version del paquete que el
 * cliente tiene instalado localmente en este momento, sin depender de una libreria JSON externa
 * (el formato es plano y lo controla el propio launcher/publish-sdrerc-release.ps1).
 *
 * <p>Orden de resolucion de {@link #leerEtiquetaVersion()}:</p>
 * <ol>
 *   <li>{@code version-local.json} de la copia que esta corriendo ahora mismo (directorio de
 *   trabajo actual o junto al JAR en ejecucion) - caso normal cuando la app se abre via el
 *   launcher real.</li>
 *   <li>{@code version-local.json} del cliente instalado en {@code C:\SDRERC_CLIENTE\app} en esta
 *   misma maquina, si existe - cubre desarrollo local (ej. {@code run-v2.ps1}) en una maquina que
 *   tambien tiene el cliente LAN instalado, mostrando la version realmente desplegada como
 *   referencia aunque se este corriendo la app desde el codigo fuente.</li>
 *   <li>Version declarada en {@code pom.xml} del directorio de trabajo actual (en desarrollo, la
 *   raiz del repositorio), etiquetada "(desarrollo local)" - ultimo respaldo cuando no hay ninguna
 *   instalacion de cliente detectable en la maquina.</li>
 * </ol>
 */
public final class VersionInfoReader {

    private static final String ARCHIVO = "version-local.json";
    private static final String ARCHIVO_POM = "pom.xml";
    private static final String RUTA_CLIENTE_INSTALADO = "C:\\SDRERC_CLIENTE\\app\\version-local.json";
    private static final Pattern CAMPO_VERSION = Pattern.compile("\"version\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern CAMPO_FECHA = Pattern.compile("\"releaseDate\"\\s*:\\s*\"([^\"]*)\"");

    private VersionInfoReader() {
    }

    public static String leerEtiquetaVersion() {
        String etiquetaInstalada = leerEtiquetaVersionEnEjecucion();
        if (etiquetaInstalada != null) {
            return etiquetaInstalada;
        }
        String etiquetaClienteInstalado = leerEtiquetaVersionClienteInstalado();
        if (etiquetaClienteInstalado != null) {
            return etiquetaClienteInstalado;
        }
        return leerEtiquetaVersionDesarrollo();
    }

    private static String leerEtiquetaVersionEnEjecucion() {
        return formatearEtiquetaDesdeArchivo(resolverArchivo(), "Versión ");
    }

    private static String leerEtiquetaVersionClienteInstalado() {
        return formatearEtiquetaDesdeArchivo(new File(RUTA_CLIENTE_INSTALADO), "Versión cliente instalada ");
    }

    private static String formatearEtiquetaDesdeArchivo(File archivo, String prefijo) {
        if (archivo == null || !archivo.isFile()) {
            return null;
        }
        try {
            String contenido = new String(Files.readAllBytes(archivo.toPath()), StandardCharsets.UTF_8);
            String version = extraer(CAMPO_VERSION, contenido);
            if (version == null || version.trim().isEmpty()) {
                return null;
            }
            String fecha = formatearFecha(extraer(CAMPO_FECHA, contenido));
            return fecha != null
                    ? (prefijo + version.trim() + " · " + fecha)
                    : (prefijo + version.trim());
        } catch (IOException ex) {
            return null;
        }
    }

    private static String leerEtiquetaVersionDesarrollo() {
        String version = leerVersionPomLocal();
        return version == null ? null : ("Versión " + version + " (desarrollo local)");
    }

    private static String leerVersionPomLocal() {
        File pom = new File(ARCHIVO_POM);
        if (!pom.isFile()) {
            return null;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Element raiz = builder.parse(pom).getDocumentElement();
            NodeList hijos = raiz.getChildNodes();
            for (int i = 0; i < hijos.getLength(); i++) {
                Node nodo = hijos.item(i);
                if (nodo.getNodeType() == Node.ELEMENT_NODE && "version".equals(nodo.getNodeName())) {
                    String texto = nodo.getTextContent();
                    return texto == null || texto.trim().isEmpty() ? null : texto.trim();
                }
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private static File resolverArchivo() {
        File enDirectorioActual = new File(ARCHIVO);
        if (enDirectorioActual.isFile()) {
            return enDirectorioActual;
        }
        File juntoAlJar = resolverJuntoAlJar();
        return juntoAlJar != null && juntoAlJar.isFile() ? juntoAlJar : enDirectorioActual;
    }

    private static File resolverJuntoAlJar() {
        try {
            CodeSource origen = VersionInfoReader.class.getProtectionDomain().getCodeSource();
            if (origen == null || origen.getLocation() == null) {
                return null;
            }
            File jar = new File(origen.getLocation().toURI());
            File directorio = jar.isFile() ? jar.getParentFile() : jar;
            return directorio == null ? null : new File(directorio, ARCHIVO);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String extraer(Pattern patron, String contenido) {
        Matcher matcher = patron.matcher(contenido);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String formatearFecha(String isoFecha) {
        if (isoFecha == null || isoFecha.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDate fecha = LocalDate.parse(isoFecha.trim());
            return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception ex) {
            return isoFecha.trim();
        }
    }
}
