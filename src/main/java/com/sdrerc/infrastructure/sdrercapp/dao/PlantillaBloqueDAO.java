package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.PlantillaBloqueDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PlantillaBloqueDAO {

    public List<PlantillaBloqueDTO> listarPorTipo(Long idTipoDocumentoAdjunto) throws SQLException {
        String sql = "SELECT id_plantilla_bloque, id_tipo_documento_adjunto, orden, seccion, titulo, contenido, "
                + "variable_condicion, operador_condicion, valores_condicion, activo "
                + "FROM plantilla_bloque WHERE id_tipo_documento_adjunto = ? AND activo = 1 ORDER BY orden";
        try (Connection conn = SdrercAppConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTipoDocumentoAdjunto);
            try (ResultSet rs = ps.executeQuery()) {
                List<PlantillaBloqueDTO> items = new ArrayList<PlantillaBloqueDTO>();
                while (rs.next()) {
                    items.add(map(rs));
                }
                return items;
            }
        }
    }

    public List<PlantillaBloqueDTO> listarPorCodigoTipo(String codigoTipoDocumento) throws SQLException {
        if (codigoTipoDocumento == null || codigoTipoDocumento.trim().isEmpty()) {
            return new ArrayList<PlantillaBloqueDTO>();
        }
        String sql = "SELECT pb.id_plantilla_bloque, pb.id_tipo_documento_adjunto, pb.orden, pb.seccion, pb.titulo, pb.contenido, "
                + "pb.variable_condicion, pb.operador_condicion, pb.valores_condicion, pb.activo "
                + "FROM plantilla_bloque pb "
                + "JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = pb.id_tipo_documento_adjunto "
                + "WHERE pb.activo = 1 AND UPPER(tda.codigo) = UPPER(?) ORDER BY pb.orden";
        try (Connection conn = SdrercAppConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoTipoDocumento.trim());
            try (ResultSet rs = ps.executeQuery()) {
                List<PlantillaBloqueDTO> items = new ArrayList<PlantillaBloqueDTO>();
                while (rs.next()) {
                    items.add(map(rs));
                }
                return items;
            }
        }
    }

    public Long insertar(PlantillaBloqueDTO bloque, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            int siguienteOrden = bloque.getOrden() > 0 ? bloque.getOrden() : obtenerSiguienteOrden(conn, bloque.getIdTipoDocumentoAdjunto());
            String sql = "INSERT INTO plantilla_bloque ("
                    + "id_tipo_documento_adjunto, orden, seccion, titulo, contenido, "
                    + "variable_condicion, operador_condicion, valores_condicion, activo, creado_por, creado_en"
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
            try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"id_plantilla_bloque"})) {
                ps.setLong(1, bloque.getIdTipoDocumentoAdjunto());
                ps.setInt(2, siguienteOrden);
                ps.setString(3, bloque.getSeccion());
                ps.setString(4, bloque.getTitulo());
                ps.setString(5, bloque.getContenido());
                ps.setString(6, bloque.getVariableCondicion());
                ps.setString(7, bloque.getOperadorCondicion());
                ps.setString(8, bloque.getValoresCondicion());
                setLongOrNull(ps, 9, idUsuario);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    return keys.next() ? Long.valueOf(keys.getLong(1)) : null;
                }
            }
        }
    }

    public void actualizar(PlantillaBloqueDTO bloque, Long idUsuario) throws SQLException {
        String sql = "UPDATE plantilla_bloque SET seccion = ?, titulo = ?, contenido = ?, "
                + "variable_condicion = ?, operador_condicion = ?, valores_condicion = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_plantilla_bloque = ?";
        try (Connection conn = SdrercAppConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bloque.getSeccion());
            ps.setString(2, bloque.getTitulo());
            ps.setString(3, bloque.getContenido());
            ps.setString(4, bloque.getVariableCondicion());
            ps.setString(5, bloque.getOperadorCondicion());
            ps.setString(6, bloque.getValoresCondicion());
            setLongOrNull(ps, 7, idUsuario);
            ps.setLong(8, bloque.getIdPlantillaBloque());
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se encontró el bloque de plantilla seleccionado.");
            }
        }
    }

    public void eliminar(Long idPlantillaBloque, Long idUsuario) throws SQLException {
        String sql = "UPDATE plantilla_bloque SET activo = 0, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_plantilla_bloque = ?";
        try (Connection conn = SdrercAppConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            setLongOrNull(ps, 1, idUsuario);
            ps.setLong(2, idPlantillaBloque);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se encontró el bloque de plantilla seleccionado.");
            }
        }
    }

    public void guardarOrden(List<Long> idsOrdenados, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                String sql = "UPDATE plantilla_bloque SET orden = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                        + "WHERE id_plantilla_bloque = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    for (int i = 0; i < idsOrdenados.size(); i++) {
                        ps.setInt(1, i + 1);
                        setLongOrNull(ps, 2, idUsuario);
                        ps.setLong(3, idsOrdenados.get(i));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                conn.commit();
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private int obtenerSiguienteOrden(Connection conn, Long idTipoDocumentoAdjunto) throws SQLException {
        String sql = "SELECT NVL(MAX(orden), 0) + 1 FROM plantilla_bloque WHERE id_tipo_documento_adjunto = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTipoDocumentoAdjunto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 1;
            }
        }
    }

    private PlantillaBloqueDTO map(ResultSet rs) throws SQLException {
        return new PlantillaBloqueDTO(
                Long.valueOf(rs.getLong("id_plantilla_bloque")),
                Long.valueOf(rs.getLong("id_tipo_documento_adjunto")),
                rs.getInt("orden"),
                rs.getString("seccion"),
                rs.getString("titulo"),
                leerClob(rs.getClob("contenido")),
                rs.getString("variable_condicion"),
                rs.getString("operador_condicion"),
                rs.getString("valores_condicion"),
                rs.getInt("activo") == 1);
    }

    private static String leerClob(Clob clob) throws SQLException {
        if (clob == null) {
            return "";
        }
        try {
            return clob.getSubString(1, (int) clob.length());
        } finally {
            clob.free();
        }
    }

    private static void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // El error original se reporta al usuario; el rollback fallido no debe ocultarlo.
        }
    }
}
