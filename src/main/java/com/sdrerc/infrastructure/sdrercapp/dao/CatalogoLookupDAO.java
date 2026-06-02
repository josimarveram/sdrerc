package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CatalogoLookupDAO {

    public Long obtenerEtapaId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "etapa_expediente", "id_etapa", codigo);
    }

    public Long obtenerEstadoId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "estado_expediente", "id_estado", codigo);
    }

    public Long obtenerTipoMovimientoId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_movimiento", "id_tipo_movimiento", codigo);
    }

    public Long obtenerCanalRecepcionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "canal_recepcion", "id_canal_recepcion", codigo);
    }

    public Long obtenerTipoActaId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_acta", "id_tipo_acta", codigo);
    }

    public Long obtenerTipoNotificacionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_notificacion", "id_tipo_notificacion", codigo);
    }

    public Long obtenerEstadoNotificacionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "estado_notificacion", "id_estado_notificacion", codigo);
    }

    public List<CatalogoItemDTO> listarCanalesRecepcion() throws SQLException {
        return listarCatalogo("canal_recepcion");
    }

    public List<CatalogoItemDTO> listarProcedimientosRegistrales() throws SQLException {
        return listarCatalogo("procedimiento_registral");
    }

    public List<CatalogoItemDTO> listarTiposDocumento() throws SQLException {
        return listarCatalogo("tipo_documento");
    }

    public List<CatalogoItemDTO> listarTiposActa() throws SQLException {
        return listarCatalogo("tipo_acta");
    }

    private Long obtenerIdPorCodigo(Connection conn, String tabla, String columnaId, String codigo) throws SQLException {
        String sql = "SELECT " + columnaId + " FROM " + tabla + " WHERE UPPER(codigo) = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo == null ? "" : codigo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                long value = rs.getLong(1);
                return rs.wasNull() ? null : value;
            }
        }
    }

    private List<CatalogoItemDTO> listarCatalogo(String tabla) throws SQLException {
        String sql = "SELECT codigo, nombre FROM " + tabla + " WHERE activo = 1 ORDER BY nombre";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<CatalogoItemDTO> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new CatalogoItemDTO(rs.getString("codigo"), rs.getString("nombre")));
            }
            return items;
        }
    }
}
