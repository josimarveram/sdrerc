package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.AbogadoSupervisadoDTO;
import com.sdrerc.v3.domain.AreaAdminDTO;
import com.sdrerc.v3.domain.EquipoJuridicoAdminDTO;
import com.sdrerc.v3.domain.EquipoMiembroAdminDTO;
import com.sdrerc.v3.domain.UsuarioAsignableEquipoDTO;
import com.sdrerc.v3.infrastructure.dao.AreaAdminDAO;
import com.sdrerc.v3.infrastructure.dao.EquipoJuridicoAdminDAO;
import com.sdrerc.v3.infrastructure.dao.EquipoMiembroAdminDAO;
import com.sdrerc.v3.infrastructure.dao.UsuarioSupervisionAdminDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Port de {@code com.sdrerc.application.sdrercapp.EquipoJuridicoService} +
 * {@code EquipoJuridicoValidacionService} (V2), Administración &gt; Equipo Jurídico. Reglas de
 * negocio portadas literales: código único `[A-Z0-9_]+`, no inactivar un equipo con expedientes
 * activos, no retirar un miembro con expedientes activos, responsable/supervisor de equipo debe
 * tener rol SUPERVISION o ADMIN_SISTEMA.
 */
@Service
public class EquipoJuridicoAdminService {

    private final EquipoJuridicoAdminDAO equipoJuridicoAdminDAO;
    private final EquipoMiembroAdminDAO equipoMiembroAdminDAO;
    private final AreaAdminDAO areaAdminDAO;
    private final UsuarioSupervisionAdminDAO usuarioSupervisionAdminDAO;

    public EquipoJuridicoAdminService(
            EquipoJuridicoAdminDAO equipoJuridicoAdminDAO,
            EquipoMiembroAdminDAO equipoMiembroAdminDAO,
            AreaAdminDAO areaAdminDAO,
            UsuarioSupervisionAdminDAO usuarioSupervisionAdminDAO) {
        this.equipoJuridicoAdminDAO = equipoJuridicoAdminDAO;
        this.equipoMiembroAdminDAO = equipoMiembroAdminDAO;
        this.areaAdminDAO = areaAdminDAO;
        this.usuarioSupervisionAdminDAO = usuarioSupervisionAdminDAO;
    }

    public List<EquipoJuridicoAdminDTO> buscar(String texto, Boolean activo, Long idArea, Long idSupervisor, Integer limite)
            throws SQLException {
        return equipoJuridicoAdminDAO.buscar(texto, activo, idArea, idSupervisor, limite);
    }

    public EquipoJuridicoAdminDTO obtenerPorId(Long idEquipo) throws SQLException {
        EquipoJuridicoAdminDTO equipo = equipoJuridicoAdminDAO.obtenerPorId(idEquipo);
        if (equipo == null) {
            throw new IllegalArgumentException("El equipo seleccionado no existe.");
        }
        return equipo;
    }

    public List<AreaAdminDTO> listarAreasActivas() throws SQLException {
        return areaAdminDAO.listar(Boolean.TRUE);
    }

    public List<UsuarioAsignableEquipoDTO> listarUsuariosAsignables() throws SQLException {
        return equipoMiembroAdminDAO.listarUsuariosAsignables(null, false);
    }

    public List<UsuarioAsignableEquipoDTO> listarSupervisoresAsignables() throws SQLException {
        return equipoMiembroAdminDAO.listarUsuariosAsignables(null, true);
    }

    public List<EquipoMiembroAdminDTO> listarMiembros(Long idEquipo) throws SQLException {
        return equipoMiembroAdminDAO.listarMiembros(idEquipo);
    }

    public List<UsuarioAsignableEquipoDTO> listarSupervisoresConAbogados() throws SQLException {
        return usuarioSupervisionAdminDAO.listarSupervisoresConAbogados();
    }

    public List<AbogadoSupervisadoDTO> listarAbogadosPorSupervisor(Long idSupervisor) throws SQLException {
        return usuarioSupervisionAdminDAO.listarAbogadosPorSupervisor(idSupervisor);
    }

    public void quitarAbogadoDeSupervisor(Long idSupervisor, Long idAbogado) throws SQLException {
        if (idSupervisor == null) {
            throw new IllegalArgumentException("Seleccione un supervisor.");
        }
        if (idAbogado == null) {
            throw new IllegalArgumentException("Seleccione un abogado supervisado.");
        }
        usuarioSupervisionAdminDAO.quitarAbogado(idSupervisor, idAbogado, resolverIdUsuarioActual());
    }

    public void agregarAbogadoASupervisor(Long idSupervisor, Long idAbogado) throws SQLException {
        if (idSupervisor == null) {
            throw new IllegalArgumentException("Seleccione un supervisor.");
        }
        if (idAbogado == null) {
            throw new IllegalArgumentException("Seleccione un abogado.");
        }
        usuarioSupervisionAdminDAO.agregarAbogado(idSupervisor, idAbogado, resolverIdUsuarioActual());
    }

    public EquipoJuridicoAdminDTO guardar(EquipoJuridicoAdminDTO equipo, Long idResponsable) throws SQLException {
        prepararEquipo(equipo);
        validarGuardar(equipo);
        if (equipoJuridicoAdminDAO.existeCodigo(equipo.getCodigo(), equipo.getIdEquipo())) {
            throw new IllegalArgumentException("Ya existe un equipo con el código indicado.");
        }
        boolean esNuevo = equipo.getIdEquipo() == null;
        if (!esNuevo) {
            obtenerPorId(equipo.getIdEquipo());
            if (!equipo.isActivo()) {
                validarEquipoSinExpedientesActivos(equipo.getIdEquipo());
            }
        }
        if (idResponsable != null && !equipoMiembroAdminDAO.usuarioSupervisorCompatible(idResponsable)) {
            throw new IllegalArgumentException("Seleccione un usuario con rol de supervisión para responsable del equipo.");
        }
        return equipoJuridicoAdminDAO.guardarConResponsable(equipo, idResponsable, resolverIdUsuarioActual());
    }

    public EquipoJuridicoAdminDTO cambiarActivo(Long idEquipo, boolean activo) throws SQLException {
        if (idEquipo == null) {
            throw new IllegalArgumentException("Seleccione un equipo.");
        }
        obtenerPorId(idEquipo);
        if (!activo) {
            validarEquipoSinExpedientesActivos(idEquipo);
        }
        return equipoJuridicoAdminDAO.cambiarActivo(idEquipo, activo, resolverIdUsuarioActual());
    }

    public void agregarMiembro(Long idEquipo, Long idUsuario) throws SQLException {
        obtenerPorId(idEquipo);
        if (idUsuario == null) {
            throw new IllegalArgumentException("Seleccione un usuario para agregar.");
        }
        equipoMiembroAdminDAO.agregarMiembro(idEquipo, idUsuario, resolverIdUsuarioActual());
    }

    public void quitarMiembro(Long idEquipo, Long idUsuario) throws SQLException {
        obtenerPorId(idEquipo);
        if (idUsuario == null) {
            throw new IllegalArgumentException("Seleccione un miembro del equipo.");
        }
        int expedientesActivos = equipoMiembroAdminDAO.contarExpedientesActivosMiembro(idEquipo, idUsuario);
        if (expedientesActivos > 0) {
            throw new IllegalArgumentException("No se puede retirar el miembro porque tiene expedientes activos.");
        }
        equipoMiembroAdminDAO.quitarMiembro(idEquipo, idUsuario, resolverIdUsuarioActual());
    }

    public void marcarResponsable(Long idEquipo, Long idUsuario) throws SQLException {
        obtenerPorId(idEquipo);
        if (idUsuario == null) {
            throw new IllegalArgumentException("Seleccione un miembro del equipo.");
        }
        if (!equipoMiembroAdminDAO.usuarioSupervisorCompatible(idUsuario)) {
            throw new IllegalArgumentException("Seleccione un usuario con rol de supervisión para responsable del equipo.");
        }
        equipoMiembroAdminDAO.marcarResponsable(idEquipo, idUsuario, resolverIdUsuarioActual());
    }

    private void validarEquipoSinExpedientesActivos(Long idEquipo) throws SQLException {
        int expedientesActivos = equipoJuridicoAdminDAO.contarExpedientesActivosEquipo(idEquipo);
        if (expedientesActivos > 0) {
            throw new IllegalArgumentException("No se puede inactivar el equipo porque tiene expedientes activos.");
        }
    }

    private void prepararEquipo(EquipoJuridicoAdminDTO equipo) {
        equipo.setCodigo(equipo.getCodigo() == null ? null
                : equipo.getCodigo().trim().replace(' ', '_').toUpperCase(Locale.ROOT));
        equipo.setNombre(trimToNull(equipo.getNombre()));
        equipo.setDescripcion(trimToNull(equipo.getDescripcion()));
    }

    private void validarGuardar(EquipoJuridicoAdminDTO equipo) {
        List<String> errores = new ArrayList<>();
        if (!hasText(equipo.getCodigo())) {
            errores.add("Ingrese el código del equipo.");
        } else if (!equipo.getCodigo().matches("[A-Z0-9_]+")) {
            errores.add("El código del equipo debe usar mayúsculas, números y guion bajo, sin espacios.");
        } else if (equipo.getCodigo().length() > 40) {
            errores.add("El código del equipo no debe superar 40 caracteres.");
        }
        if (!hasText(equipo.getNombre())) {
            errores.add("Ingrese el nombre del equipo.");
        } else if (equipo.getNombre().length() > 150) {
            errores.add("El nombre del equipo no debe superar 150 caracteres.");
        }
        if (hasText(equipo.getDescripcion()) && equipo.getDescripcion().length() > 500) {
            errores.add("La descripción del equipo no debe superar 500 caracteres.");
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
    }

    private Long resolverIdUsuarioActual() {
        return CurrentUser.get().idUsuario();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
