package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.PermisoAdminDTO;
import com.sdrerc.v3.domain.RolAdminDTO;
import com.sdrerc.v3.infrastructure.dao.PermisoAdminDAO;
import com.sdrerc.v3.infrastructure.dao.RolAdminDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Port de {@code com.sdrerc.application.sdrercapp.RolService} +
 * {@code RolValidacionService} (V2), Administración &gt; Roles. Reglas de negocio portadas
 * literales: código único con formato {@code [A-Z0-9_]+}, no inactivar un rol con usuarios activos
 * asociados. Único cambio real: el usuario actual se resuelve desde {@link CurrentUser}.
 */
@Service
public class RolAdminService {

    private final RolAdminDAO rolAdminDAO;
    private final PermisoAdminDAO permisoAdminDAO;

    public RolAdminService(RolAdminDAO rolAdminDAO, PermisoAdminDAO permisoAdminDAO) {
        this.rolAdminDAO = rolAdminDAO;
        this.permisoAdminDAO = permisoAdminDAO;
    }

    public List<RolAdminDTO> buscar(String texto, Boolean activo, Integer limite) throws SQLException {
        return rolAdminDAO.buscar(texto, activo, limite);
    }

    public RolAdminDTO obtenerPorId(Long idRol) throws SQLException {
        RolAdminDTO rol = rolAdminDAO.obtenerPorId(idRol);
        if (rol == null) {
            throw new IllegalArgumentException("El rol seleccionado no existe.");
        }
        return rol;
    }

    public List<PermisoAdminDTO> listarPermisosPorRol(Long idRol) throws SQLException {
        return permisoAdminDAO.listarPermisosPorRol(idRol);
    }

    public RolAdminDTO guardar(RolAdminDTO rol) throws SQLException {
        prepararRol(rol);
        validarGuardar(rol);
        if (rolAdminDAO.existeCodigo(rol.getCodigo(), rol.getIdRol())) {
            throw new IllegalArgumentException("Ya existe un rol con el código indicado.");
        }

        boolean esNuevo = rol.getIdRol() == null;
        if (!esNuevo) {
            RolAdminDTO actual = obtenerPorId(rol.getIdRol());
            validarInactivacion(actual, rol.isActivo());
        }

        return esNuevo
                ? rolAdminDAO.insertar(rol, resolverIdUsuarioActual())
                : rolAdminDAO.actualizar(rol, resolverIdUsuarioActual());
    }

    public RolAdminDTO cambiarActivo(Long idRol, boolean activo) throws SQLException {
        if (idRol == null) {
            throw new IllegalArgumentException("Seleccione un rol.");
        }
        RolAdminDTO actual = obtenerPorId(idRol);
        validarInactivacion(actual, activo);
        return rolAdminDAO.cambiarActivo(idRol, activo, resolverIdUsuarioActual());
    }

    public void guardarPermisos(Long idRol, List<Long> idsPermiso) throws SQLException {
        permisoAdminDAO.sincronizarPermisos(idRol, idsPermiso, resolverIdUsuarioActual());
    }

    private void prepararRol(RolAdminDTO rol) {
        rol.setCodigo(normalizarCodigo(rol.getCodigo()));
        rol.setNombre(trim(rol.getNombre()));
        rol.setDescripcion(trim(rol.getDescripcion()));
    }

    private void validarGuardar(RolAdminDTO rol) {
        List<String> errores = new ArrayList<>();
        if (!hasText(rol.getCodigo())) {
            errores.add("Ingrese el código del rol.");
        } else if (!rol.getCodigo().matches("[A-Z0-9_]+")) {
            errores.add("El código debe usar mayúsculas, números y guion bajo, sin espacios.");
        } else if (rol.getCodigo().length() > 40) {
            errores.add("El código del rol no debe superar 40 caracteres.");
        }
        if (!hasText(rol.getNombre())) {
            errores.add("Ingrese el nombre del rol.");
        } else if (rol.getNombre().length() > 150) {
            errores.add("El nombre del rol no debe superar 150 caracteres.");
        }
        if (rol.getDescripcion() != null && rol.getDescripcion().length() > 500) {
            errores.add("La descripción no debe superar 500 caracteres.");
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
    }

    private void validarInactivacion(RolAdminDTO actual, boolean activoDestino) throws SQLException {
        if (activoDestino || actual == null || !actual.isActivo()) {
            return;
        }
        int usuariosActivos = rolAdminDAO.contarUsuariosActivos(actual.getIdRol());
        if (usuariosActivos > 0) {
            throw new IllegalArgumentException("No se puede inactivar el rol porque tiene usuarios asociados.");
        }
    }

    private Long resolverIdUsuarioActual() {
        return CurrentUser.get().idUsuario();
    }

    private static String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "_");
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
