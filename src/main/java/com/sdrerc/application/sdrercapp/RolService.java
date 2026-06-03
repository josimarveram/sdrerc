package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.RolDTO;
import com.sdrerc.domain.dto.sdrercapp.RolFiltroDTO;
import com.sdrerc.domain.dto.sdrercapp.RolResultadoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.RolDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.List;

public class RolService {

    private final RolDAO rolDAO;
    private final RolValidacionService validacionService;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public RolService() {
        this(new RolDAO(), new RolValidacionService(), new UsuarioAsignacionService());
    }

    public RolService(RolDAO rolDAO, RolValidacionService validacionService, UsuarioAsignacionService usuarioAsignacionService) {
        this.rolDAO = rolDAO;
        this.validacionService = validacionService;
        this.usuarioAsignacionService = usuarioAsignacionService;
    }

    public List<RolDTO> buscar(RolFiltroDTO filtro) throws SQLException {
        return rolDAO.buscar(filtro);
    }

    public RolDTO obtenerPorId(Long idRol) throws SQLException {
        return rolDAO.obtenerPorId(idRol);
    }

    public RolResultadoDTO guardar(RolDTO rol) throws SQLException {
        prepararRol(rol);
        List<String> errores = validacionService.validarGuardar(rol);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
        if (rolDAO.existeCodigo(rol.getCodigo(), rol.getIdRol())) {
            throw new IllegalArgumentException("Ya existe un rol con el código indicado.");
        }

        boolean esNuevo = rol.getIdRol() == null;
        if (!esNuevo) {
            RolDTO actual = rolDAO.obtenerPorId(rol.getIdRol());
            if (actual == null) {
                throw new IllegalArgumentException("El rol seleccionado no existe.");
            }
            validarInactivacion(actual, rol.isActivo());
        }

        RolDTO guardado = esNuevo
                ? rolDAO.insertar(rol, resolverUsuarioActualSdrercApp())
                : rolDAO.actualizar(rol, resolverUsuarioActualSdrercApp());
        String mensaje = esNuevo
                ? "El rol fue registrado correctamente."
                : "El rol fue actualizado correctamente.";
        return RolResultadoDTO.exito(mensaje, guardado);
    }

    public RolResultadoDTO cambiarActivo(Long idRol, boolean activo) throws SQLException {
        if (idRol == null) {
            throw new IllegalArgumentException("Seleccione un rol.");
        }
        RolDTO actual = rolDAO.obtenerPorId(idRol);
        if (actual == null) {
            throw new IllegalArgumentException("El rol seleccionado no existe.");
        }
        validarInactivacion(actual, activo);
        RolDTO actualizado = rolDAO.cambiarActivo(idRol, activo, resolverUsuarioActualSdrercApp());
        String mensaje = activo
                ? "El rol fue activado correctamente."
                : "El rol fue inactivado correctamente.";
        return RolResultadoDTO.exito(mensaje, actualizado);
    }

    private void prepararRol(RolDTO rol) {
        if (rol == null) {
            return;
        }
        rol.setCodigo(validacionService.normalizarCodigo(rol.getCodigo()));
        rol.setNombre(validacionService.normalizarTexto(rol.getNombre()));
        rol.setDescripcion(validacionService.normalizarTexto(rol.getDescripcion()));
    }

    private void validarInactivacion(RolDTO actual, boolean activoDestino) throws SQLException {
        if (activoDestino || actual == null || !actual.isActivo()) {
            return;
        }
        int usuariosActivos = rolDAO.contarUsuariosActivos(actual.getIdRol());
        if (usuariosActivos > 0) {
            throw new IllegalArgumentException("No se puede inactivar el rol porque tiene usuarios asociados.");
        }
    }

    private Long resolverUsuarioActualSdrercApp() {
        try {
            String username = SessionContext.getUsername();
            return usuarioAsignacionService.obtenerIdUsuarioActivoPorUsername(username);
        } catch (Exception ex) {
            return null;
        }
    }
}
