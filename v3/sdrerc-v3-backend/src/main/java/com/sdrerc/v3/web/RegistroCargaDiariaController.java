package com.sdrerc.v3.web;

import com.sdrerc.v3.application.CargaDiariaExpedienteService;
import com.sdrerc.v3.application.CargaDiariaPlantillaService;
import com.sdrerc.v3.domain.CargaDiariaPreviewDTO;
import com.sdrerc.v3.domain.CargaDiariaResultadoDTO;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Equivalente web de la pestaña "Carga diaria" de Recepción (V2: JPanelCargaDiariaRecepcionV2).
 * Subir Excel/CSV → parsear → validar (mismas reglas que Registro manual) → previsualización
 * editable → (re)Validar tras editar → Confirmar y registrar. Incluye además exportar la
 * previsualización a Excel y descargar la plantilla oficial — mismo alcance funcional que V2.
 */
@RestController
@RequestMapping("/api/registro/carga-diaria")
public class RegistroCargaDiariaController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final CargaDiariaExpedienteService cargaDiariaExpedienteService;

    public RegistroCargaDiariaController(CargaDiariaExpedienteService cargaDiariaExpedienteService) {
        this.cargaDiariaExpedienteService = cargaDiariaExpedienteService;
    }

    @PostMapping("/previsualizar")
    public List<CargaDiariaPreviewDTO> previsualizar(@RequestParam("archivo") MultipartFile archivo) throws IOException, SQLException {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Seleccione un archivo .xlsx o .csv para previsualizar.");
        }
        return cargaDiariaExpedienteService.previsualizar(archivo.getInputStream(), archivo.getOriginalFilename());
    }

    @PostMapping("/validar")
    public List<CargaDiariaPreviewDTO> validar(@RequestBody List<CargaDiariaPreviewDTO> registros) throws SQLException {
        return cargaDiariaExpedienteService.validar(registros);
    }

    @PostMapping("/confirmar")
    public CargaDiariaResultadoDTO confirmar(@RequestBody List<CargaDiariaPreviewDTO> registros) throws SQLException {
        return cargaDiariaExpedienteService.confirmar(registros);
    }

    @PostMapping("/exportar")
    public ResponseEntity<byte[]> exportar(@RequestBody List<CargaDiariaPreviewDTO> registros) throws IOException {
        byte[] contenido = cargaDiariaExpedienteService.exportarExcel(registros);
        String nombreArchivo = "previsualizacion_carga_diaria_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        return archivoAdjunto(contenido, nombreArchivo);
    }

    @GetMapping("/plantilla")
    public ResponseEntity<byte[]> plantilla() throws IOException {
        byte[] contenido = cargaDiariaExpedienteService.generarPlantilla();
        return archivoAdjunto(contenido, CargaDiariaPlantillaService.NOMBRE_ARCHIVO);
    }

    private ResponseEntity<byte[]> archivoAdjunto(byte[] contenido, String nombreArchivo) {
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombreArchivo).build().toString())
                .body(contenido);
    }
}
