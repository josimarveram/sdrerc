package com.sdrerc.v3.web;

import com.sdrerc.v3.application.UsuarioAdminService;
import com.sdrerc.v3.domain.EquipoAsignacionDTO;
import com.sdrerc.v3.domain.RolResumenDTO;
import com.sdrerc.v3.domain.UsuarioAdminDTO;
import com.sdrerc.v3.web.dto.GuardarUsuarioRequest;
import com.sdrerc.v3.web.dto.RestablecerClaveRequest;
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
 * Equivalente web de Administración &gt; Usuarios (V2: {@code JPanelUsuariosV2} +
 * {@code DlgRestablecerClaveV2}). Alcance de este incremento: listar/filtrar, obtener, crear,
 * editar, activar/inactivar, restablecer clave, y los catálogos de roles/equipos para el
 * multi-select "Roles y equipos" del workspace.
 */
@RestController
@RequestMapping("/api/admin/usuarios")
public class UsuarioAdminController {

    private final UsuarioAdminService usuarioAdminService;

    public UsuarioAdminController(UsuarioAdminService usuarioAdminService) {
        this.usuarioAdminService = usuarioAdminService;
    }

    @GetMapping
    public List<UsuarioAdminDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Long idRol,
            @RequestParam(required = false) Long idEquipo,
            @RequestParam(required = false) Integer limite) throws SQLException {
        return usuarioAdminService.buscar(texto, activo, idRol, idEquipo, limite);
    }

    @GetMapping("/{idUsuario}")
    public UsuarioAdminDTO obtener(@PathVariable Long idUsuario) throws SQLException {
        return usuarioAdminService.obtenerPorId(idUsuario);
    }

    @GetMapping("/roles")
    public List<RolResumenDTO> listarRoles() throws SQLException {
        return usuarioAdminService.listarRolesActivos();
    }

    @GetMapping("/equipos")
    public List<EquipoAsignacionDTO> listarEquipos() throws SQLException {
        return usuarioAdminService.listarEquiposActivos();
    }

    @PostMapping
    public UsuarioAdminDTO crear(@RequestBody GuardarUsuarioRequest request) throws SQLException {
        return usuarioAdminService.guardar(aDto(request, null), request.idsRoles(), request.idsEquipos());
    }

    @PutMapping("/{idUsuario}")
    public UsuarioAdminDTO editar(@PathVariable Long idUsuario, @RequestBody GuardarUsuarioRequest request)
            throws SQLException {
        return usuarioAdminService.guardar(aDto(request, idUsuario), request.idsRoles(), request.idsEquipos());
    }

    @PutMapping("/{idUsuario}/activo")
    public UsuarioAdminDTO cambiarActivo(@PathVariable Long idUsuario, @RequestParam boolean activo)
            throws SQLException {
        return usuarioAdminService.cambiarActivo(idUsuario, activo);
    }

    @PostMapping("/{idUsuario}/restablecer-clave")
    public void restablecerClave(@PathVariable Long idUsuario, @RequestBody RestablecerClaveRequest request)
            throws SQLException {
        usuarioAdminService.restablecerClave(idUsuario, request.passwordTemporal(), request.reiniciarVerificacionDosPasos());
    }

    private static UsuarioAdminDTO aDto(GuardarUsuarioRequest request, Long idUsuario) {
        UsuarioAdminDTO dto = new UsuarioAdminDTO();
        dto.setIdUsuario(idUsuario != null ? idUsuario : request.idUsuario());
        dto.setUsername(request.username());
        dto.setNombreCompleto(request.nombreCompleto());
        dto.setTipoDocumento(request.tipoDocumento());
        dto.setNumeroDocumento(request.numeroDocumento());
        dto.setCorreo(request.correo());
        dto.setTelefono(request.telefono());
        dto.setActivo(request.activo());
        return dto;
    }
}
