package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.domain.CatalogoItemDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Subconjunto de com.sdrerc.infrastructure.sdrercapp.dao.CatalogoLookupDAO (V2): solo los
 * lookups que necesita el registro manual (etapa/estado/tipo de movimiento/canal de
 * recepción/tipo de acta por código, más los 4 catálogos para combos: canal de recepción,
 * procedimiento registral, tipo de documento, tipo de acta). El resto de catálogos de V2 (tipo de
 * notificación, estado de notificación, etc.) se agregan cuando el modulo correspondiente se
 * porte. Mismo SQL.
 */
@Repository
public class CatalogoLookupDAO {

    private final DataSource dataSource;

    public CatalogoLookupDAO(DataSource dataSource) {
        this.dataSource = dataSource;
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

    private List<CatalogoItemDTO> listarCatalogo(String tabla) throws SQLException {
        String sql = "SELECT codigo, nombre FROM " + tabla + " WHERE activo = 1 ORDER BY nombre";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<CatalogoItemDTO> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new CatalogoItemDTO(rs.getString("codigo"), rs.getString("nombre")));
            }
            return items;
        }
    }

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

    /**
     * Carga diaria trae "Tipo de acta" como texto libre de la plantilla Excel (puede venir como
     * código o como nombre visible); a diferencia de {@link #obtenerTipoActaId}, que solo acepta
     * código exacto.
     */
    public Long obtenerTipoActaIdPorCodigoONombre(Connection conn, String value) throws SQLException {
        return obtenerIdPorCodigoONombre(conn, "tipo_acta", "id_tipo_acta", value);
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

    private Long obtenerIdPorCodigoONombre(Connection conn, String tabla, String columnaId, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT " + columnaId + " FROM " + tabla
                + " WHERE activo = 1 AND (UPPER(codigo) = ? OR UPPER(nombre) = ?) AND ROWNUM = 1";
        String normalized = value.trim().toUpperCase();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalized);
            ps.setString(2, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                long id = rs.getLong(1);
                return rs.wasNull() ? null : id;
            }
        }
    }
}
