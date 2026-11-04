package com.sdrerc.v3.web;

import com.sdrerc.v3.application.FeriadoAdminService;
import com.sdrerc.v3.domain.FeriadoAdminDTO;
import com.sdrerc.v3.web.dto.GuardarFeriadoRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Equivalente web de Administración &gt; Feriados (V2: {@code JPanelFeriadosV2}). */
@RestController
@RequestMapping("/api/admin/feriados")
public class FeriadoAdminController {

    private final FeriadoAdminService feriadoAdminService;

    public FeriadoAdminController(FeriadoAdminService feriadoAdminService) {
        this.feriadoAdminService = feriadoAdminService;
    }

    @GetMapping
    public List<FeriadoAdminDTO> buscar(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false, defaultValue = "0") int limite) throws SQLException {
        return feriadoAdminService.buscar(anio, activo, limite);
    }

    @PostMapping
    public FeriadoAdminDTO crear(@RequestBody GuardarFeriadoRequest request) throws SQLException {
        return feriadoAdminService.guardar(aDto(request, null));
    }

    @PutMapping("/{idFeriado}")
    public FeriadoAdminDTO editar(@PathVariable Long idFeriado, @RequestBody GuardarFeriadoRequest request) throws SQLException {
        return feriadoAdminService.guardar(aDto(request, idFeriado));
    }

    @PutMapping("/{idFeriado}/activo")
    public FeriadoAdminDTO cambiarActivo(@PathVariable Long idFeriado, @RequestParam boolean activo) throws SQLException {
        return feriadoAdminService.cambiarActivo(idFeriado, activo);
    }

    @PostMapping("/importar-xml")
    public ImportarResponse importarXml(@RequestParam("archivo") MultipartFile archivo, @RequestParam int anio)
            throws SQLException, IOException {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Seleccione un archivo .xml para importar.");
        }
        int importados = feriadoAdminService.importarXmlPorAnio(archivo.getInputStream(), anio);
        return new ImportarResponse(importados);
    }

    public record ImportarResponse(int importados) {
    }

    private static FeriadoAdminDTO aDto(GuardarFeriadoRequest request, Long idFeriado) {
        FeriadoAdminDTO dto = new FeriadoAdminDTO();
        dto.setIdFeriado(idFeriado != null ? idFeriado : request.idFeriado());
        dto.setFecha(request.fecha());
        dto.setNombre(request.nombre());
        dto.setTipo(request.tipo());
        dto.setActivo(request.activo());
        dto.setObservacion(request.observacion());
        return dto;
    }
}
