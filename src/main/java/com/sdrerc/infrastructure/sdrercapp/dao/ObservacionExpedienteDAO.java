package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.ObservacionAnalisisDTO;
import com.sdrerc.domain.dto.sdrercapp.ObservacionVerificacionDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class ObservacionExpedienteDAO {

    private static final String ORIGEN_ANALISIS = "ANALISIS";
    private static final String ORIGEN_VERIFICACION = "VERIFICACION";

    private final CatalogoLookupDAO catalogoLookupDAO;

    public ObservacionExpedienteDAO() {
        this(new CatalogoLookupDAO());
    }

    public ObservacionExpedienteDAO(CatalogoLookupDAO catalogoLookupDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    public List<CatalogoItemDTO> listarTiposObservacion() throws SQLException {
        return catalogoLookupDAO.listarTiposObservacion();
    }

    public void insertarObservacion(
            Connection conn,
            Long idExpediente,
            ObservacionAnalisisDTO observacion,
            Long idUsuarioCreador) throws SQLException {
        if (observacion == null || !observacion.hasDescripcion()) {
            return;
        }
        Long idTipoObservacion = catalogoLookupDAO.obtenerTipoObservacionId(conn, observacion.getTipoObservacionCodigo());
        if (idTipoObservacion == null && observacion.getTipoObservacionCodigo() != null
                && !observacion.getTipoObservacionCodigo().trim().isEmpty()) {
            throw new SQLException("No se encontró el tipo de observación: " + observacion.getTipoObservacionCodigo() + ".");
        }
        String sql = "INSERT INTO expediente_observacion ("
                + "id_expediente, id_tipo_observacion, origen_observacion, descripcion, "
                + "subsanada, fecha_observacion, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, 0, SYSTIMESTAMP, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            if (idTipoObservacion == null) {
                ps.setNull(2, Types.NUMERIC);
            } else {
                ps.setLong(2, idTipoObservacion);
            }
            ps.setString(3, ORIGEN_ANALISIS);
            ps.setString(4, limitar(observacion.getDescripcion(), 1500));
            if (idUsuarioCreador == null) {
                ps.setNull(5, Types.NUMERIC);
            } else {
                ps.setLong(5, idUsuarioCreador);
            }
            ps.executeUpdate();
        }
    }

    public void insertarObservacionVerificacion(
            Connection conn,
            Long idExpediente,
            ObservacionVerificacionDTO observacion,
            Long idUsuarioCreador) throws SQLException {
        if (observacion == null || !observacion.hasDescripcion()) {
            return;
        }
        Long idTipoObservacion = catalogoLookupDAO.obtenerTipoObservacionId(conn, observacion.getTipoObservacionCodigo());
        if (idTipoObservacion == null && hasText(observacion.getTipoObservacionCodigo())) {
            throw new SQLException("No se encontró el tipo de observación: " + observacion.getTipoObservacionCodigo() + ".");
        }
        Long idMotivoCorreccion = catalogoLookupDAO.obtenerMotivoCorreccionId(conn, observacion.getMotivoCorreccionCodigo());
        if (idMotivoCorreccion == null && hasText(observacion.getMotivoCorreccionCodigo())) {
            throw new SQLException("No se encontró el motivo de corrección: " + observacion.getMotivoCorreccionCodigo() + ".");
        }
        String sql = "INSERT INTO expediente_observacion ("
                + "id_expediente, id_tipo_observacion, id_motivo_correccion, origen_observacion, descripcion, "
                + "subsanada, fecha_observacion, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, 0, SYSTIMESTAMP, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            if (idTipoObservacion == null) {
                ps.setNull(2, Types.NUMERIC);
            } else {
                ps.setLong(2, idTipoObservacion);
            }
            if (idMotivoCorreccion == null) {
                ps.setNull(3, Types.NUMERIC);
            } else {
                ps.setLong(3, idMotivoCorreccion);
            }
            ps.setString(4, ORIGEN_VERIFICACION);
            ps.setString(5, limitar(observacion.getDescripcion(), 1500));
            if (idUsuarioCreador == null) {
                ps.setNull(6, Types.NUMERIC);
            } else {
                ps.setLong(6, idUsuarioCreador);
            }
            ps.executeUpdate();
        }
    }

    private static String limitar(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
