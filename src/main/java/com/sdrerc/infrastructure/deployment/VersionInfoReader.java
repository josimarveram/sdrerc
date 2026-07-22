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

/**
 * Lee {@code version-local.json}, el archivo que {@code scripts/client/sdrerc-launcher.ps1} escribe
 * en {@code C:\SDRERC_CLIENTE\app} cada vez que aplica una actualizacion descargada desde
 * {@code D:\SDRERC_RELEASES\latest}. Permite mostrar en el login la version del paquete que el
 * cliente tiene instalado localmente en este momento, sin depender de una libreria JSON externa
 * (el formato es plano y lo controla el propio launcher/publish-sdrerc-release.ps1).
 *
 * <p>Si la app se ejecuta fuera del flujo del launcher (por ejemplo con {@code run-v2.ps1} en
 * desarrollo), el archivo no existe y {@link #leerEtiquetaVersion()} retorna {@code null}.</p>
 */
public final class VersionInfoReader {

    private static final String ARCHIVO = "version-local.json";
    private static final Pattern CAMPO_VERSION = Pattern.compile("\"version\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern CAMPO_FECHA = Pattern.compile("\"releaseDate\"\\s*:\\s*\"([^\"]*)\"");

    private VersionInfoReader() {
    }

    public static String leerEtiquetaVersion() {
        File archivo = resolverArchivo();
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
            return fecha != null ? ("Versión " + version.trim() + " · " + fecha) : ("Versión " + version.trim());
        } catch (IOException ex) {
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
