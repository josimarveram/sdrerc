package com.sdrerc.v3.web;

import com.sdrerc.v3.application.RolAdminService;
import com.sdrerc.v3.domain.PermisoAdminDTO;
import com.sdrerc.v3.domain.RolAdminDTO;
import com.sdrerc.v3.web.dto.GuardarPermisosRolRequest;
import com.sdrerc.v3.web.dto.GuardarRolRequest;
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

/**
 * Equivalente web de Administración &gt; Roles (V2: {@code JPanelRolesV2}). Mejora sobre V2 en el
 * frontend (Accordion por módulo en vez de tabla plana sin agrupar) — este controller expone la
 * misma lista plana de permisos que V2, la agrupación es responsabilidad del cliente.
 */
@RestController
@RequestMapping("/api/admin/roles")
public class RolAdminController {

    private final RolAdminService rolAdminService;

    public RolAdminController(RolAdminService rolAdminService) {
        this.rolAdminService = rolAdminService;
    }

    @GetMapping
    public List<RolAdminDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Integer limite) throws SQLException {
        return rolAdminService.buscar(texto, activo, limite);
    }

    @GetMapping("/{idRol}")
    public RolAdminDTO obtener(@PathVariable Long idRol) throws SQLException {
        return rolAdminService.obtenerPorId(idRol);
    }

    @GetMapping("/{idRol}/permisos")
    public List<PermisoAdminDTO> listarPermisos(@PathVariable Long idRol) throws SQLException {
        return rolAdminService.listarPermisosPorRol(idRol);
    }

    @PostMapping
    public RolAdminDTO crear(@RequestBody GuardarRolRequest request) throws SQLException {
        return rolAdminService.guardar(aDto(request, null));
    }

    @PutMapping("/{idRol}")
    public RolAdminDTO editar(@PathVariable Long idRol, @RequestBody GuardarRolRequest request) throws SQLException {
        return rolAdminService.guardar(aDto(request, idRol));
    }

    @PutMapping("/{idRol}/activo")
    public RolAdminDTO cambiarActivo(@PathVariable Long idRol, @RequestParam boolean activo) throws SQLException {
        return rolAdminService.cambiarActivo(idRol, activo);
    }

    @PutMapping("/{idRol}/permisos")
    public void guardarPermisos(@PathVariable Long idRol, @RequestBody GuardarPermisosRolRequest request) throws SQLException {
        rolAdminService.guardarPermisos(idRol, request.idsPermiso());
    }

    private static RolAdminDTO aDto(GuardarRolRequest request, Long idRol) {
        RolAdminDTO dto = new RolAdminDTO();
        dto.setIdRol(idRol != null ? idRol : request.idRol());
        dto.setCodigo(request.codigo());
        dto.setNombre(request.nombre());
        dto.setDescripcion(request.descripcion());
        dto.setActivo(request.activo());
        return dto;
    }
}
