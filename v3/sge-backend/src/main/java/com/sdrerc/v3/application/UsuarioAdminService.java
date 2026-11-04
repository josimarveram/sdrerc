package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.RolResumenDTO;
import com.sdrerc.v3.domain.UsuarioAdminDTO;
import com.sdrerc.v3.infrastructure.dao.EquipoAsignacionDAO;
import com.sdrerc.v3.infrastructure.dao.RolAdminDAO;
import com.sdrerc.v3.infrastructure.dao.UsuarioAdminDAO;
import com.sdrerc.v3.security.PasswordEncoder;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.sql.SQLException;
import org.springframework.stereotype.Service;

/**
 * Port de {@code com.sdrerc.application.sdrercapp.UsuarioService} +
 * {@code UsuarioValidacionService} (V2), Administración &gt; Usuarios. Reglas de negocio
 * portadas literales: no perder el último {@code ADMIN_SISTEMA} activo, no inactivar el usuario
 * de la sesión actual, al menos 1 rol obligatorio. Único cambio real: el usuario actual se
 * resuelve desde {@link CurrentUser} (JWT del request), no de {@code SessionContext} estático —
 * mismo patrón ya aplicado en el resto de V3.
 */
@Service
public class UsuarioAdminService {

    private static final String CODIGO_ROL_ADMIN = "ADMIN_SISTEMA";
    private static final int MIN_LONGITUD_PASSWORD_TEMPORAL = 8;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private final UsuarioAdminDAO usuarioAdminDAO;
    private final RolAdminDAO rolAdminDAO;
    private final EquipoAsignacionDAO equipoAsignacionDAO;

    public UsuarioAdminService(UsuarioAdminDAO usuarioAdminDAO, RolAdminDAO rolAdminDAO,
            EquipoAsignacionDAO equipoAsignacionDAO) {
        this.usuarioAdminDAO = usuarioAdminDAO;
        this.rolAdminDAO = rolAdminDAO;
        this.equipoAsignacionDAO = equipoAsignacionDAO;
    }

    public List<UsuarioAdminDTO> buscar(String texto, Boolean activo, Long idRol, Long idEquipo, Integer limite)
            throws SQLException {
        return usuarioAdminDAO.buscar(texto, activo, idRol, idEquipo, limite);
    }

    public UsuarioAdminDTO obtenerPorId(Long idUsuario) throws SQLException {
        UsuarioAdminDTO usuario = usuarioAdminDAO.obtenerPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario seleccionado no existe.");
        }
        return usuario;
    }

    public List<RolResumenDTO> listarRolesActivos() throws SQLException {
        return rolAdminDAO.listarActivos();
    }

    public List<com.sdrerc.v3.domain.EquipoAsignacionDTO> listarEquiposActivos() throws SQLException {
        return equipoAsignacionDAO.listarEquiposActivos();
    }

    public UsuarioAdminDTO guardar(UsuarioAdminDTO usuario, List<Long> idsRoles, List<Long> idsEquipos)
            throws SQLException {
        prepararUsuario(usuario);
        validarGuardar(usuario, idsRoles);
        if (usuarioAdminDAO.existeUsername(usuario.getUsername(), usuario.getIdUsuario())) {
            throw new IllegalArgumentException("Ya existe un usuario con el login indicado.");
        }

        boolean esNuevo = usuario.getIdUsuario() == null;
        if (!esNuevo) {
            UsuarioAdminDTO actual = obtenerPorId(usuario.getIdUsuario());
            validarNoPierdeAdministracion(actual, usuario.isActivo(), idsRoles);
            validarInactivacionUsuarioActual(actual, usuario.isActivo());
        }

        return usuarioAdminDAO.guardarUsuarioCompleto(usuario, idsRoles, idsEquipos, resolverIdUsuarioActual());
    }

    public UsuarioAdminDTO cambiarActivo(Long idUsuario, boolean activo) throws SQLException {
        if (idUsuario == null) {
            throw new IllegalArgumentException("Seleccione un usuario.");
        }
        UsuarioAdminDTO actual = obtenerPorId(idUsuario);
        validarNoPierdeAdministracion(actual, activo, actual.getIdsRoles());
        validarInactivacionUsuarioActual(actual, activo);
        return usuarioAdminDAO.cambiarActivo(idUsuario, activo, resolverIdUsuarioActual());
    }

    /**
     * "Restablecer clave": fija una contraseña temporal, fuerza el cambio en el próximo login y,
     * opcionalmente, reinicia la verificación en dos pasos (usuario perdió su dispositivo).
     */
    public void restablecerClave(Long idUsuario, String passwordTemporal, boolean reiniciarVerificacionDosPasos)
            throws SQLException {
        if (idUsuario == null) {
            throw new IllegalArgumentException("Seleccione un usuario.");
        }
        if (passwordTemporal == null || passwordTemporal.trim().length() < MIN_LONGITUD_PASSWORD_TEMPORAL) {
            throw new IllegalArgumentException(
                    "La contraseña temporal debe tener al menos " + MIN_LONGITUD_PASSWORD_TEMPORAL + " caracteres.");
        }
        obtenerPorId(idUsuario);

        String hash = PasswordEncoder.hash(passwordTemporal.trim());
        usuarioAdminDAO.actualizarPasswordHash(idUsuario, hash, true, resolverIdUsuarioActual());
        if (reiniciarVerificacionDosPasos) {
            usuarioAdminDAO.actualizarTotp(idUsuario, null, false);
        }
    }

    private void prepararUsuario(UsuarioAdminDTO usuario) {
        usuario.setUsername(normalizarLogin(usuario.getUsername()));
        usuario.setNombreCompleto(trim(usuario.getNombreCompleto()));
        usuario.setTipoDocumento(usuario.getTipoDocumento() == null ? null
                : usuario.getTipoDocumento().trim().toUpperCase(Locale.ROOT));
        usuario.setNumeroDocumento(trim(usuario.getNumeroDocumento()));
        usuario.setCorreo(usuario.getCorreo() == null ? null : usuario.getCorreo().trim().toLowerCase(Locale.ROOT));
        usuario.setTelefono(trim(usuario.getTelefono()));
        usuario.setEstado(usuario.isActivo() ? "ACTIVO" : "INACTIVO");
    }

    private void validarGuardar(UsuarioAdminDTO usuario, List<Long> idsRoles) {
        List<String> errores = new ArrayList<>();
        if (!hasText(usuario.getUsername())) {
            errores.add("Ingrese el usuario.");
        } else if (!usuario.getUsername().matches("[A-Z0-9._-]+")) {
            errores.add("El usuario solo debe usar letras, números, punto, guion o guion bajo.");
        } else if (usuario.getUsername().length() > 60) {
            errores.add("El usuario no debe superar 60 caracteres.");
        }
        if (!hasText(usuario.getNombreCompleto())) {
            errores.add("Ingrese el nombre completo del usuario.");
        } else if (usuario.getNombreCompleto().length() > 250) {
            errores.add("El nombre completo no debe superar 250 caracteres.");
        }
        if (hasText(usuario.getCorreo())) {
            if (usuario.getCorreo().length() > 250) {
                errores.add("El correo no debe superar 250 caracteres.");
            } else if (!EMAIL_PATTERN.matcher(usuario.getCorreo()).matches()) {
                errores.add("Ingrese un correo válido.");
            }
        }
        if (hasText(usuario.getTipoDocumento()) && usuario.getTipoDocumento().length() > 20) {
            errores.add("El tipo de documento no debe superar 20 caracteres.");
        }
        if (hasText(usuario.getNumeroDocumento()) && usuario.getNumeroDocumento().length() > 20) {
            errores.add("El número de documento no debe superar 20 caracteres.");
        }
        if (idsRoles == null || idsRoles.isEmpty()) {
            errores.add("Seleccione al menos un rol.");
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
    }

    private void validarNoPierdeAdministracion(UsuarioAdminDTO actual, boolean activoDestino, List<Long> idsRolesDestino)
            throws SQLException {
        boolean eraAdmin = usuarioAdminDAO.usuarioTieneRolActivo(actual.getIdUsuario(), CODIGO_ROL_ADMIN);
        if (!eraAdmin) {
            return;
        }
        boolean seraAdmin = usuarioAdminDAO.seleccionIncluyeRolCodigo(idsRolesDestino, CODIGO_ROL_ADMIN);
        if (activoDestino && seraAdmin) {
            return;
        }
        int administradoresActivos = usuarioAdminDAO.contarUsuariosActivosConRol(CODIGO_ROL_ADMIN);
        if (administradoresActivos <= 1) {
            throw new IllegalArgumentException("No se puede dejar al sistema sin un administrador activo.");
        }
    }

    private void validarInactivacionUsuarioActual(UsuarioAdminDTO actual, boolean activoDestino) {
        if (activoDestino) {
            return;
        }
        Long idActual = resolverIdUsuarioActual();
        if (idActual != null && idActual.equals(actual.getIdUsuario())) {
            throw new IllegalArgumentException("No se puede inactivar el usuario actual.");
        }
    }

    private Long resolverIdUsuarioActual() {
        return CurrentUser.get().idUsuario();
    }

    private static String normalizarLogin(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
