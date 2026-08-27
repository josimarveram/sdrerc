package com.sdrerc.v3.web;

import com.sdrerc.v3.application.PlantillaBloqueAdminService;
import com.sdrerc.v3.application.PlantillaDocumentoAdminService;
import com.sdrerc.v3.domain.PlantillaBloqueAdminDTO;
import com.sdrerc.v3.domain.PlantillaDocumentoAdminDTO;
import com.sdrerc.v3.domain.PlantillaVariableDTO;
import com.sdrerc.v3.infrastructure.dao.PlantillaDocumentoAdminDAO;
import com.sdrerc.v3.web.dto.GuardarBloqueRequest;
import com.sdrerc.v3.web.dto.GuardarOrdenBloquesRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Equivalente web de Administración &gt; Plantillas de documento (V2: {@code JPanelPlantillasDocumentoV2}
 * + {@code DlgBloquesPlantillaV2}). Ver Javadoc de {@code PlantillaDocumentoAdminService}/
 * {@code AnalisisPlantillaDocumentoService} para las mejoras funcionales sobre V2 (catálogo de
 * variables estructurado, generación de documento de prueba).
 */
@RestController
@RequestMapping("/api/admin/plantillas")
public class PlantillaAdminController {

    private static final MediaType DOCX = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final PlantillaDocumentoAdminService plantillaDocumentoAdminService;
    private final PlantillaBloqueAdminService plantillaBloqueAdminService;

    public PlantillaAdminController(PlantillaDocumentoAdminService plantillaDocumentoAdminService,
            PlantillaBloqueAdminService plantillaBloqueAdminService) {
        this.plantillaDocumentoAdminService = plantillaDocumentoAdminService;
        this.plantillaBloqueAdminService = plantillaBloqueAdminService;
    }

    @GetMapping
    public List<PlantillaDocumentoAdminDTO> listarTipos() throws SQLException {
        return plantillaDocumentoAdminService.listarTiposConPlantilla();
    }

    @GetMapping("/{idTipoDocumentoAdjunto}/historial")
    public List<PlantillaDocumentoAdminDTO> listarHistorial(@PathVariable Long idTipoDocumentoAdjunto) throws SQLException {
        return plantillaDocumentoAdminService.listarHistorial(idTipoDocumentoAdjunto);
    }

    @GetMapping("/version/{idPlantillaDocumento}/descargar")
    public ResponseEntity<byte[]> descargar(@PathVariable Long idPlantillaDocumento) throws SQLException {
        PlantillaDocumentoAdminDAO.ContenidoPlantilla contenido = plantillaDocumentoAdminService.descargarContenido(idPlantillaDocumento);
        return ResponseEntity.ok()
                .contentType(DOCX)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(contenido.nombreArchivo()).build().toString())
                .body(contenido.contenido());
    }

    @PostMapping("/{idTipoDocumentoAdjunto}/version")
    public IdResponse cargarNuevaVersion(
            @PathVariable Long idTipoDocumentoAdjunto,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(required = false) String comentario) throws SQLException, IOException {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Seleccione un archivo .docx para cargar.");
        }
        Long id = plantillaDocumentoAdminService.cargarNuevaVersion(
                idTipoDocumentoAdjunto, archivo.getOriginalFilename(), archivo.getBytes(), comentario);
        return new IdResponse(id);
    }

    @PutMapping("/version/{idPlantillaDocumento}/activar")
    public void activarVersion(@PathVariable Long idPlantillaDocumento) throws SQLException {
        plantillaDocumentoAdminService.activarVersion(idPlantillaDocumento);
    }

    @GetMapping("/{idTipoDocumentoAdjunto}/generar-prueba")
    public ResponseEntity<byte[]> generarPrueba(@PathVariable Long idTipoDocumentoAdjunto) throws SQLException, IOException {
        byte[] contenido = plantillaDocumentoAdminService.generarDocumentoPrueba(idTipoDocumentoAdjunto);
        return ResponseEntity.ok()
                .contentType(DOCX)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("documento_prueba.docx").build().toString())
                .body(contenido);
    }

    @GetMapping("/variables")
    public List<PlantillaVariableDTO> listarVariables() {
        return plantillaDocumentoAdminService.listarVariablesDisponibles();
    }

    @GetMapping("/{idTipoDocumentoAdjunto}/bloques")
    public List<PlantillaBloqueAdminDTO> listarBloques(@PathVariable Long idTipoDocumentoAdjunto) throws SQLException {
        return plantillaBloqueAdminService.listarPorTipo(idTipoDocumentoAdjunto);
    }

    @PostMapping("/bloques")
    public IdResponse crearBloque(@RequestBody GuardarBloqueRequest request) throws SQLException {
        return new IdResponse(plantillaBloqueAdminService.guardar(aDto(request, null)));
    }

    @PutMapping("/bloques/{idPlantillaBloque}")
    public IdResponse editarBloque(@PathVariable Long idPlantillaBloque, @RequestBody GuardarBloqueRequest request) throws SQLException {
        return new IdResponse(plantillaBloqueAdminService.guardar(aDto(request, idPlantillaBloque)));
    }

    @DeleteMapping("/bloques/{idPlantillaBloque}")
    public void eliminarBloque(@PathVariable Long idPlantillaBloque) throws SQLException {
        plantillaBloqueAdminService.eliminar(idPlantillaBloque);
    }

    @PutMapping("/{idTipoDocumentoAdjunto}/bloques/orden")
    public void guardarOrdenBloques(@PathVariable Long idTipoDocumentoAdjunto, @RequestBody GuardarOrdenBloquesRequest request)
            throws SQLException {
        plantillaBloqueAdminService.guardarOrden(request.idsOrdenados());
    }

    public record IdResponse(Long id) {
    }

    private static PlantillaBloqueAdminDTO aDto(GuardarBloqueRequest request, Long idPlantillaBloque) {
        PlantillaBloqueAdminDTO dto = new PlantillaBloqueAdminDTO();
        dto.setIdPlantillaBloque(idPlantillaBloque != null ? idPlantillaBloque : request.idPlantillaBloque());
        dto.setIdTipoDocumentoAdjunto(request.idTipoDocumentoAdjunto());
        dto.setSeccion(request.seccion());
        dto.setTitulo(request.titulo());
        dto.setContenido(request.contenido());
        dto.setVariableCondicion(request.variableCondicion());
        dto.setOperadorCondicion(request.operadorCondicion());
        dto.setValoresCondicion(request.valoresCondicion());
        return dto;
    }
}
