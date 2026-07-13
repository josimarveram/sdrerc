package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    public Long obtenerTipoActaIdPorCodigoONombre(Connection conn, String value) throws SQLException {
        return obtenerIdPorCodigoONombre(conn, "tipo_acta", "id_tipo_acta", value);
    }

    public Long obtenerTipoNotificacionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_notificacion", "id_tipo_notificacion", codigo);
    }

    public Long obtenerEstadoNotificacionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "estado_notificacion", "id_estado_notificacion", codigo);
    }

    public Long obtenerEstadoCargoAcuseId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "estado_cargo_acuse", "id_estado_cargo_acuse", codigo);
    }

    public Long obtenerTipoResultadoEvaluacionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_resultado_evaluacion", "id_tipo_resultado_evaluacion", codigo);
    }

    public Long obtenerTipoDocumentoAdjuntoId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_documento_adjunto", "id_tipo_documento_adjunto", codigo);
    }

    public Long obtenerEstadoDocumentoId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "estado_documento", "id_estado_documento", codigo);
    }

    public Long obtenerTipoObservacionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_observacion", "id_tipo_observacion", codigo);
    }

    public Long obtenerMotivoNoCorrespondeId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "motivo_no_corresponde", "id_motivo_no_corresponde", codigo);
    }

    public Long obtenerMotivoCorreccionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "motivo_correccion", "id_motivo_correccion", codigo);
    }

    public Long obtenerTipoResolucionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_resolucion", "id_tipo_resolucion", codigo);
    }

    public Long obtenerTipoResultadoEjecucionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_resultado_ejecucion", "id_tipo_resultado_ejecucion", codigo);
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

    public List<CatalogoItemDTO> listarResultadosEvaluacion() throws SQLException {
        return listarCatalogo("tipo_resultado_evaluacion");
    }

    public List<CatalogoItemDTO> listarTiposDocumentoAdjunto() throws SQLException {
        return listarCatalogo("tipo_documento_adjunto");
    }

    public List<CatalogoItemDTO> listarTiposDocumentoAdjuntoAnalisis() throws SQLException {
        String sql = "SELECT codigo, nombre FROM tipo_documento_adjunto "
                + "WHERE activo = 1 AND codigo LIKE 'ANALISIS_%' ORDER BY codigo";
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

    public List<CatalogoItemDTO> listarEstadosDocumento() throws SQLException {
        return listarCatalogo("estado_documento");
    }

    public List<CatalogoItemDTO> listarEstadosExpediente() throws SQLException {
        return listarCatalogo("estado_expediente");
    }

    public List<CatalogoItemDTO> listarEstadosExpedientePorEtapa(String etapaCodigo) throws SQLException {
        String sql = "SELECT est.codigo, est.nombre "
                + "FROM estado_expediente est "
                + "JOIN etapa_expediente et ON et.id_etapa = est.id_etapa "
                + "WHERE est.activo = 1 AND et.activo = 1 AND UPPER(et.codigo) = ? "
                + "ORDER BY est.id_estado";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, etapaCodigo == null ? "" : etapaCodigo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                List<CatalogoItemDTO> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(new CatalogoItemDTO(rs.getString("codigo"), rs.getString("nombre")));
                }
                return items;
            }
        }
    }

    public List<CatalogoItemDTO> listarEstadosExpedientePorCodigos(List<String> codigos) throws SQLException {
        List<CatalogoItemDTO> items = new ArrayList<>();
        if (codigos == null || codigos.isEmpty()) {
            return items;
        }
        String sql = "SELECT codigo, nombre "
                + "FROM estado_expediente "
                + "WHERE activo = 1 AND UPPER(codigo) = ? "
                + "ORDER BY id_estado";
        Set<String> agregados = new LinkedHashSet<>();
        try (Connection conn = SdrercAppConnection.getConnection()) {
            for (String codigo : codigos) {
                String normalized = codigo == null ? "" : codigo.trim().toUpperCase();
                if (normalized.isEmpty() || agregados.contains(normalized)) {
                    continue;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, normalized);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            items.add(new CatalogoItemDTO(rs.getString("codigo"), rs.getString("nombre")));
                            agregados.add(normalized);
                        }
                    }
                }
            }
        }
        return items;
    }

    public List<CatalogoItemDTO> listarTiposObservacion() throws SQLException {
        return listarCatalogo("tipo_observacion");
    }

    public List<CatalogoItemDTO> listarMotivosNoCorresponde() throws SQLException {
        return listarCatalogo("motivo_no_corresponde");
    }

    public List<CatalogoItemDTO> listarMotivosCorreccion() throws SQLException {
        return listarCatalogo("motivo_correccion");
    }

    public List<CatalogoItemDTO> listarTiposResolucion() throws SQLException {
        return listarCatalogo("tipo_resolucion");
    }

    public List<CatalogoItemDTO> listarResultadosEjecucion() throws SQLException {
        return listarCatalogo("tipo_resultado_ejecucion");
    }

    public List<CatalogoItemDTO> listarResultadosValidacion() throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            String sql = "SELECT COUNT(1) FROM user_tables WHERE table_name = 'TIPO_RESULTADO_VALIDACION'";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt(1) == 0) {
                    return new ArrayList<>();
                }
            }
        }
        return listarCatalogo("tipo_resultado_validacion");
    }

    public List<CatalogoItemDTO> listarTiposNotificacion() throws SQLException {
        return listarCatalogo("tipo_notificacion");
    }

    public List<CatalogoItemDTO> listarEstadosNotificacion() throws SQLException {
        return listarCatalogo("estado_notificacion");
    }

    public List<CatalogoItemDTO> listarEstadosCargoAcuse() throws SQLException {
        return listarCatalogo("estado_cargo_acuse");
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
