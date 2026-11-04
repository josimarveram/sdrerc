package com.sdrerc.v3.web;

import com.sdrerc.v3.application.PlantillaOnlyOfficeService;
import java.sql.SQLException;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Edición en línea de plantillas vía OnlyOffice Document Server — ver Javadoc de
 * {@link PlantillaOnlyOfficeService}. Solo 2 de los 3 endpoints son públicos a propósito
 * ({@code /documento/**} y {@code /callback}, ver {@code SecurityConfig}): los llama directo el
 * contenedor Document Server, que no tiene (ni debe tener) el JWT de sesión de la app — su propia
 * seguridad es un token/JWT distinto, verificado dentro del Service. {@code /{id}/config} sigue
 * protegido por el filtro normal porque lo llama el navegador con sesión activa.
 */
@RestController
@RequestMapping("/api/admin/plantillas/onlyoffice")
public class PlantillaOnlyOfficeController {

    private static final MediaType DOCX = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final PlantillaOnlyOfficeService plantillaOnlyOfficeService;

    public PlantillaOnlyOfficeController(PlantillaOnlyOfficeService plantillaOnlyOfficeService) {
        this.plantillaOnlyOfficeService = plantillaOnlyOfficeService;
    }

    @GetMapping("/{idPlantillaDocumento}/config")
    public Map<String, Object> config(@PathVariable Long idPlantillaDocumento) throws SQLException {
        return plantillaOnlyOfficeService.construirConfigEditor(idPlantillaDocumento);
    }

    @GetMapping("/documento/{token}")
    public ResponseEntity<byte[]> documento(@PathVariable String token) throws SQLException {
        byte[] contenido = plantillaOnlyOfficeService.obtenerContenidoParaDescarga(token);
        return ResponseEntity.ok().contentType(DOCX).body(contenido);
    }

    @PostMapping("/callback")
    public Map<String, Object> callback(
            @RequestParam Long idPlantillaDocumento,
            @RequestBody String body,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        boolean exito = plantillaOnlyOfficeService.procesarCallback(idPlantillaDocumento, body, authorization);
        // Protocolo de OnlyOffice: siempre HTTP 200 con {"error": 0} en éxito, cualquier otro valor
        // hace que Document Server reintente el callback o muestre un error al usuario editando.
        return Map.of("error", exito ? 0 : 1);
    }
}
