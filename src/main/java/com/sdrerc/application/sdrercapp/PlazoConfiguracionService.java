package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.PlazoConfiguracionDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import com.sdrerc.infrastructure.sdrercapp.dao.PlazoConfiguracionDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PlazoConfiguracionService {

    private final PlazoConfiguracionDAO plazoConfiguracionDAO;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public PlazoConfiguracionService() {
        this(new PlazoConfiguracionDAO(), new UsuarioAsignacionService());
    }

    public PlazoConfiguracionService(
            PlazoConfiguracionDAO plazoConfiguracionDAO,
            UsuarioAsignacionService usuarioAsignacionService) {
        this.plazoConfiguracionDAO = plazoConfiguracionDAO;
        this.usuarioAsignacionService = usuarioAsignacionService;
    }

    public List<PlazoConfiguracionDTO> buscar(String textoLibre, Boolean activo, int limite) throws SQLException {
        return plazoConfiguracionDAO.buscar(textoLibre, activo, limite);
    }

    public PlazoConfiguracionDTO obtenerPlazoSolicitud() throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return plazoConfiguracionDAO.obtenerPlazoSolicitud(conn);
        }
    }

    public PlazoConfiguracionDTO obtenerPlazoPorCodigo(String codigo) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return plazoConfiguracionDAO.obtenerPlazoPorCodigo(conn, codigo);
        }
    }

    public PlazoConfiguracionDTO guardar(PlazoConfiguracionDTO plazo) throws SQLException {
        validar(plazo);
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                if (plazo.isActivo()
                        && plazoConfiguracionDAO.existeCodigoActivo(conn, plazo.getCodigo(), plazo.getIdPlazoConfiguracion())) {
                    throw new IllegalArgumentException("Ya existe una configuración activa para el código indicado.");
                }
                Long idUsuario = resolverUsuarioActualSdrercApp();
                PlazoConfiguracionDTO resultado = plazo.getIdPlazoConfiguracion() == null
                        ? plazoConfiguracionDAO.insertar(conn, plazo, idUsuario)
                        : plazoConfiguracionDAO.actualizar(conn, plazo, idUsuario);
                conn.commit();
                return resultado;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public PlazoConfiguracionDTO cambiarActivo(Long idPlazoConfiguracion, boolean activo) throws SQLException {
        if (idPlazoConfiguracion == null) {
            throw new IllegalArgumentException("Seleccione una configuración de plazo.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                PlazoConfiguracionDTO actual = plazoConfiguracionDAO.obtenerPorId(conn, idPlazoConfiguracion);
                if (actual == null) {
                    throw new IllegalArgumentException("No se encontró la configuración seleccionada.");
                }
                if (activo && plazoConfiguracionDAO.existeCodigoActivo(conn, actual.getCodigo(), idPlazoConfiguracion)) {
                    throw new IllegalArgumentException("Ya existe una configuración activa para el código indicado.");
                }
                PlazoConfiguracionDTO resultado = plazoConfiguracionDAO.cambiarActivo(
                        conn,
                        idPlazoConfiguracion,
                        activo,
                        resolverUsuarioActualSdrercApp());
                conn.commit();
                return resultado;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private void validar(PlazoConfiguracionDTO plazo) {
        if (plazo == null) {
            throw new IllegalArgumentException("Complete los datos de la configuración.");
        }
        if (!hasText(plazo.getCodigo())) {
            throw new IllegalArgumentException("Ingrese el código del plazo.");
        }
        if (plazo.getCodigo().length() > 80) {
            throw new IllegalArgumentException("El código no debe exceder 80 caracteres.");
        }
        if (!hasText(plazo.getNombre())) {
            throw new IllegalArgumentException("Ingrese el nombre de la configuración.");
        }
        if (plazo.getNombre().length() > 180) {
            throw new IllegalArgumentException("El nombre no debe exceder 180 caracteres.");
        }
        if (!hasText(plazo.getAmbito())) {
            plazo.setAmbito(plazo.getCodigo());
        }
        if (plazo.getDiasPlazo() == null || plazo.getDiasPlazo().intValue() <= 0) {
            throw new IllegalArgumentException("Los días del plazo deben ser mayores a cero.");
        }
        if (!PlazoConfiguracionDTO.UNIDAD_HABILES.equals(plazo.getUnidadPlazo())
                && !PlazoConfiguracionDTO.UNIDAD_CALENDARIO.equals(plazo.getUnidadPlazo())) {
            throw new IllegalArgumentException("Seleccione una unidad de plazo válida.");
        }
        LocalDate desde = plazo.getFechaVigenciaDesde();
        LocalDate hasta = plazo.getFechaVigenciaHasta();
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha de vigencia hasta no puede ser anterior a la fecha desde.");
        }
        if (plazo.getObservacion() != null && plazo.getObservacion().trim().length() > 300) {
            throw new IllegalArgumentException("La observación no debe exceder 300 caracteres.");
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

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
