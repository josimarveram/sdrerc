package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO;
import com.sdrerc.domain.dto.sdrercapp.RolDTO;
import com.sdrerc.domain.dto.sdrercapp.RolFiltroDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioFiltroDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioResultadoDTO;
import com.sdrerc.infrastructure.security.PasswordEncoder;
import com.sdrerc.infrastructure.sdrercapp.dao.EquipoAsignacionDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.RolDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.UsuarioDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.List;

public class UsuarioService {

    private static final String CODIGO_ROL_ADMIN = "ADMIN_SISTEMA";
    private static final int MIN_LONGITUD_PASSWORD_TEMPORAL = 8;

    private final UsuarioDAO usuarioDAO;
    private final RolDAO rolDAO;
    private final EquipoAsignacionDAO equipoAsignacionDAO;
    private final UsuarioAsignacionService usuarioAsignacionService;
    private final UsuarioValidacionService validacionService;

    public UsuarioService() {
        this(
                new UsuarioDAO(),
                new RolDAO(),
                new EquipoAsignacionDAO(),
                new UsuarioAsignacionService(),
                new UsuarioValidacionService());
    }

    public UsuarioService(
            UsuarioDAO usuarioDAO,
            RolDAO rolDAO,
            EquipoAsignacionDAO equipoAsignacionDAO,
            UsuarioAsignacionService usuarioAsignacionService,
            UsuarioValidacionService validacionService) {
        this.usuarioDAO = usuarioDAO;
        this.rolDAO = rolDAO;
        this.equipoAsignacionDAO = equipoAsignacionDAO;
        this.usuarioAsignacionService = usuarioAsignacionService;
        this.validacionService = validacionService;
    }

    public List<UsuarioDTO> buscar(UsuarioFiltroDTO filtro) throws SQLException {
        return usuarioDAO.buscar(filtro);
    }

    public UsuarioDTO obtenerPorId(Long idUsuario) throws SQLException {
        return usuarioDAO.obtenerPorId(idUsuario);
    }

    public List<RolDTO> listarRolesActivos() throws SQLException {
        return rolDAO.buscar(new RolFiltroDTO(null, Boolean.TRUE, 1000));
    }

    public List<EquipoAsignacionDTO> listarEquiposActivos() throws SQLException {
        return equipoAsignacionDAO.listarEquiposActivos();
    }

    public UsuarioResultadoDTO guardar(UsuarioDTO usuario, List<Long> idsRoles, Long idEquipo) throws SQLException {
        validacionService.prepararUsuario(usuario);
        List<String> errores = validacionService.validarGuardar(usuario, idsRoles);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
        if (usuarioDAO.existeUsername(usuario.getUsername(), usuario.getIdUsuario())) {
            throw new IllegalArgumentException("Ya existe un usuario con el login indicado.");
        }

        boolean esNuevo = usuario.getIdUsuario() == null;
        if (!esNuevo) {
            UsuarioDTO actual = usuarioDAO.obtenerPorId(usuario.getIdUsuario());
            if (actual == null) {
                throw new IllegalArgumentException("El usuario seleccionado no existe.");
            }
            validarNoPierdeAdministracion(actual, usuario.isActivo(), idsRoles);
            validarInactivacionUsuarioActual(actual, usuario.isActivo());
        }

        UsuarioDTO guardado = usuarioDAO.guardarUsuarioCompleto(
                usuario,
                idsRoles,
                idEquipo,
                resolverUsuarioActualSdrercApp());
        String mensaje = esNuevo
                ? "El usuario fue registrado correctamente."
                : "El usuario fue actualizado correctamente.";
        return UsuarioResultadoDTO.exito(mensaje, guardado);
    }

    public UsuarioResultadoDTO cambiarActivo(Long idUsuario, boolean activo) throws SQLException {
        if (idUsuario == null) {
            throw new IllegalArgumentException("Seleccione un usuario.");
        }
        UsuarioDTO actual = usuarioDAO.obtenerPorId(idUsuario);
        if (actual == null) {
            throw new IllegalArgumentException("El usuario seleccionado no existe.");
        }
        validarNoPierdeAdministracion(actual, activo, actual.getIdsRoles());
        validarInactivacionUsuarioActual(actual, activo);
        UsuarioDTO actualizado = usuarioDAO.cambiarActivo(idUsuario, activo, resolverUsuarioActualSdrercApp());
        String mensaje = activo
                ? "El usuario fue activado correctamente."
                : "El usuario fue inactivado correctamente.";
        return UsuarioResultadoDTO.exito(mensaje, actualizado);
    }

    /**
     * Usado por Administración &gt; Usuarios &gt; "Restablecer clave": el administrador fija una
     * contraseña temporal para el usuario seleccionado, forzando el cambio en su próximo login.
     * Opcionalmente reinicia también la verificación en dos pasos (cuando el usuario perdió su
     * dispositivo autenticador), lo que obliga a un nuevo enrolamiento TOTP.
     */
    public UsuarioResultadoDTO restablecerClave(Long idUsuario, String passwordTemporal, boolean tambienReiniciarTotp)
            throws SQLException {
        if (idUsuario == null) {
            throw new IllegalArgumentException("Seleccione un usuario.");
        }
        if (passwordTemporal == null || passwordTemporal.trim().length() < MIN_LONGITUD_PASSWORD_TEMPORAL) {
            throw new IllegalArgumentException(
                    "La contraseña temporal debe tener al menos " + MIN_LONGITUD_PASSWORD_TEMPORAL + " caracteres.");
        }
        UsuarioDTO actual = usuarioDAO.obtenerPorId(idUsuario);
        if (actual == null) {
            throw new IllegalArgumentException("El usuario seleccionado no existe.");
        }

        String hash = PasswordEncoder.hash(passwordTemporal.trim());
        usuarioDAO.actualizarPasswordHash(idUsuario, hash, true, resolverUsuarioActualSdrercApp());
        if (tambienReiniciarTotp) {
            usuarioDAO.actualizarTotp(idUsuario, null, false);
        }
        return UsuarioResultadoDTO.exito(
                "Se generó una contraseña temporal. El usuario deberá cambiarla en su próximo ingreso.", actual);
    }

    private void validarNoPierdeAdministracion(UsuarioDTO actual, boolean activoDestino, List<Long> idsRolesDestino) throws SQLException {
        boolean eraAdmin = usuarioDAO.usuarioTieneRolActivo(actual.getIdUsuario(), CODIGO_ROL_ADMIN);
        if (!eraAdmin) {
            return;
        }
        boolean seraAdmin = usuarioDAO.seleccionIncluyeRolCodigo(idsRolesDestino, CODIGO_ROL_ADMIN);
        if (activoDestino && seraAdmin) {
            return;
        }
        int administradoresActivos = usuarioDAO.contarUsuariosActivosConRol(CODIGO_ROL_ADMIN);
        if (administradoresActivos <= 1) {
            throw new IllegalArgumentException("No se puede dejar al sistema sin un administrador activo.");
        }
    }

    private void validarInactivacionUsuarioActual(UsuarioDTO actual, boolean activoDestino) {
        if (activoDestino) {
            return;
        }
        Long idActual = resolverUsuarioActualSdrercApp();
        if (idActual != null && idActual.equals(actual.getIdUsuario())) {
            throw new IllegalArgumentException("No se puede inactivar el usuario actual.");
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
