package com.sdrerc.v3.web;

import com.sdrerc.v3.application.PlazoAdminService;
import com.sdrerc.v3.domain.PlazoConfiguracionDTO;
import com.sdrerc.v3.web.dto.GuardarPlazoRequest;
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

/** Equivalente web de Administración &gt; Plazos (V2: {@code JPanelPlazosV2}). */
@RestController
@RequestMapping("/api/admin/plazos")
public class PlazoAdminController {

    private final PlazoAdminService plazoAdminService;

    public PlazoAdminController(PlazoAdminService plazoAdminService) {
        this.plazoAdminService = plazoAdminService;
    }

    @GetMapping
    public List<PlazoConfiguracionDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false, defaultValue = "0") int limite) throws SQLException {
        return plazoAdminService.buscar(texto, activo, limite);
    }

    @GetMapping("/solicitud")
    public PlazoConfiguracionDTO obtenerPlazoSolicitud() throws SQLException {
        return plazoAdminService.obtenerPlazoSolicitud();
    }

    @PostMapping
    public PlazoConfiguracionDTO crear(@RequestBody GuardarPlazoRequest request) throws SQLException {
        return plazoAdminService.guardar(aDto(request, null));
    }

    @PutMapping("/{idPlazoConfiguracion}")
    public PlazoConfiguracionDTO editar(@PathVariable Long idPlazoConfiguracion, @RequestBody GuardarPlazoRequest request)
            throws SQLException {
        return plazoAdminService.guardar(aDto(request, idPlazoConfiguracion));
    }

    @PutMapping("/{idPlazoConfiguracion}/activo")
    public PlazoConfiguracionDTO cambiarActivo(@PathVariable Long idPlazoConfiguracion, @RequestParam boolean activo)
            throws SQLException {
        return plazoAdminService.cambiarActivo(idPlazoConfiguracion, activo);
    }

    private static PlazoConfiguracionDTO aDto(GuardarPlazoRequest request, Long idPlazoConfiguracion) {
        PlazoConfiguracionDTO dto = new PlazoConfiguracionDTO();
        dto.setIdPlazoConfiguracion(idPlazoConfiguracion != null ? idPlazoConfiguracion : request.idPlazoConfiguracion());
        dto.setCodigo(request.codigo());
        dto.setNombre(request.nombre());
        dto.setAmbito(request.ambito());
        dto.setDiasPlazo(request.diasPlazo());
        dto.setUnidadPlazo(request.unidadPlazo());
        dto.setPorcentajeVerdeDesde(request.porcentajeVerdeDesde());
        dto.setPorcentajeAmarilloDesde(request.porcentajeAmarilloDesde());
        dto.setPorcentajeRojoDesde(request.porcentajeRojoDesde());
        dto.setFechaVigenciaDesde(request.fechaVigenciaDesde());
        dto.setFechaVigenciaHasta(request.fechaVigenciaHasta());
        dto.setActivo(request.activo());
        dto.setObservacion(request.observacion());
        return dto;
    }
}
