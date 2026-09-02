package com.sdrerc.v3.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdrerc.v3.domain.AuthenticatedUser;
import com.sdrerc.v3.infrastructure.dao.PlantillaDocumentoAdminDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import com.sdrerc.v3.security.jwt.OnlyOfficeJwtService;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Edición en línea de plantillas de documento vía OnlyOffice Document Server (contenedor Docker
 * local, ver `v3/CLAUDE.md` — mejora explícita sobre V2 pedida por el usuario, alternativa a la
 * "vista previa" ya construida cuando además se quiere editar el `.docx` sin bajarlo a Word de
 * escritorio). Protocolo estándar de OnlyOffice (no inventado acá): documentado en
 * api.onlyoffice.com/docs/docs-api.
 *
 * <p><b>Detalle de red no evidente</b>: la URL de guardado que Document Server envía en su propio
 * callback ({@code status=2/6}) depende de configuración interna del contenedor que no controlamos
 * con precisión (puede reportar un host distinto al que el backend puede alcanzar). En vez de
 * asumir que esa URL es directamente descargable, {@link #descargarDesdeOnlyOffice} la reescribe
 * siempre a {@code localhost:8082} (el mapeo de puerto local ya conocido y probado), conservando
 * únicamente la ruta y los parámetros — determinístico, no depende de adivinar esa configuración.</p>
 */
@Service
public class PlantillaOnlyOfficeService {

    private final PlantillaDocumentoAdminDAO plantillaDocumentoAdminDAO;
    private final PlantillaDocumentoAdminService plantillaDocumentoAdminService;
    private final OnlyOfficeJwtService onlyOfficeJwtService;
    private final String backendCallbackBaseUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PlantillaOnlyOfficeService(
            PlantillaDocumentoAdminDAO plantillaDocumentoAdminDAO,
            PlantillaDocumentoAdminService plantillaDocumentoAdminService,
            OnlyOfficeJwtService onlyOfficeJwtService,
            @Value("${sdrerc.v3.onlyoffice.backend-callback-base-url}") String backendCallbackBaseUrl) {
        this.plantillaDocumentoAdminDAO = plantillaDocumentoAdminDAO;
        this.plantillaDocumentoAdminService = plantillaDocumentoAdminService;
        this.onlyOfficeJwtService = onlyOfficeJwtService;
        this.backendCallbackBaseUrl = backendCallbackBaseUrl;
    }

    /** Config firmada que el frontend le pasa tal cual a {@code new DocsAPI.DocEditor(id, config)}. */
    public Map<String, Object> construirConfigEditor(Long idPlantillaDocumento) throws SQLException {
        PlantillaDocumentoAdminDAO.ContenidoPlantilla contenido = plantillaDocumentoAdminDAO.obtenerContenido(idPlantillaDocumento);
        String tokenDescarga = onlyOfficeJwtService.emitirTokenDescarga(idPlantillaDocumento);
        String documentUrl = backendCallbackBaseUrl + "/api/admin/plantillas/onlyoffice/documento/" + tokenDescarga;
        String callbackUrl = backendCallbackBaseUrl + "/api/admin/plantillas/onlyoffice/callback?idPlantillaDocumento=" + idPlantillaDocumento;
        // Clave única por sesión de edición (no por plantilla): Document Server cachea por "key",
        // reutilizar la misma entre aperturas distintas podría servir una versión vieja cacheada.
        String key = "plantilla" + idPlantillaDocumento + "_" + System.currentTimeMillis();
        AuthenticatedUser usuarioActual = CurrentUser.get();

        Map<String, Object> permissions = new LinkedHashMap<>();
        permissions.put("edit", true);
        permissions.put("download", true);

        Map<String, Object> document = new LinkedHashMap<>();
        document.put("fileType", "docx");
        document.put("key", key);
        document.put("title", contenido.nombreArchivo());
        document.put("url", documentUrl);
        document.put("permissions", permissions);

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", String.valueOf(usuarioActual.idUsuario()));
        user.put("name", usuarioActual.nombreCompleto());

        Map<String, Object> editorConfig = new LinkedHashMap<>();
        editorConfig.put("callbackUrl", callbackUrl);
        editorConfig.put("lang", "es");
        editorConfig.put("user", user);

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("document", document);
        config.put("documentType", "word");
        config.put("editorConfig", editorConfig);
        config.put("token", onlyOfficeJwtService.firmarConfigEditor(config));
        return config;
    }

    /** Llamado por el CONTENEDOR de OnlyOffice (nunca por el navegador) para leer el `.docx` a editar. */
    public byte[] obtenerContenidoParaDescarga(String tokenDescarga) throws SQLException {
        Long idPlantillaDocumento = onlyOfficeJwtService.validarTokenDescarga(tokenDescarga);
        return plantillaDocumentoAdminDAO.obtenerContenido(idPlantillaDocumento).contenido();
    }

    /**
     * Callback real de guardado. Devuelve siempre {@code true}/{@code false} (nunca lanza) — el
     * controller traduce eso al formato {@code {"error": 0|1}} que exige el protocolo de OnlyOffice,
     * para no dejar a Document Server reintentando indefinidamente ni mostrando un error genérico
     * al usuario mientras sigue con el editor abierto.
     */
    public boolean procesarCallback(Long idPlantillaDocumento, String bodyJson, String headerAuthorization) {
        try {
            JsonNode raiz = objectMapper.readTree(bodyJson);
            String token = extraerToken(raiz, headerAuthorization);
            if (token == null) {
                throw new IllegalStateException("Callback de OnlyOffice sin token de firma.");
            }
            onlyOfficeJwtService.validarCallback(token);

            int status = raiz.path("status").asInt(-1);
            // 2 = guardado final (editor cerrado); 6 = guardado forzado (aún en edición). El resto
            // (0=en edición sin guardar, 1=listo para editar, 3/4/7=error/sin cambios) no trae
            // documento nuevo que persistir.
            if (status != 2 && status != 6) {
                return true;
            }
            String urlGuardado = raiz.path("url").asText(null);
            if (urlGuardado == null || urlGuardado.isBlank()) {
                throw new IllegalStateException("Callback de OnlyOffice sin URL de documento guardado.");
            }
            byte[] contenidoNuevo = descargarDesdeOnlyOffice(urlGuardado);

            Long idTipoDocumentoAdjunto = plantillaDocumentoAdminDAO.obtenerIdTipoDocumento(idPlantillaDocumento);
            if (idTipoDocumentoAdjunto == null) {
                throw new IllegalStateException("No se encontró la plantilla que se estaba editando.");
            }
            String nombreArchivo = plantillaDocumentoAdminDAO.obtenerContenido(idPlantillaDocumento).nombreArchivo();
            plantillaDocumentoAdminService.cargarNuevaVersion(
                    idTipoDocumentoAdjunto, nombreArchivo, contenidoNuevo, "Editado en línea (OnlyOffice)");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String extraerToken(JsonNode raiz, String headerAuthorization) {
        if (headerAuthorization != null && headerAuthorization.startsWith("Bearer ")) {
            return headerAuthorization.substring("Bearer ".length());
        }
        if (raiz.hasNonNull("token")) {
            return raiz.get("token").asText();
        }
        return null;
    }

    private byte[] descargarDesdeOnlyOffice(String urlOriginal) throws IOException, InterruptedException {
        URI original = URI.create(urlOriginal);
        URI reescrita;
        try {
            reescrita = new URI("http", null, "localhost", 8082, original.getRawPath(), original.getRawQuery(), null);
        } catch (URISyntaxException ex) {
            throw new IOException("URL de documento guardado inválida.", ex);
        }
        HttpRequest request = HttpRequest.newBuilder(reescrita).GET().build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new IOException("No se pudo descargar el documento guardado desde OnlyOffice (HTTP " + response.statusCode() + ").");
        }
        return response.body();
    }
}
