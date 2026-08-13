package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.PlantillaDocumentoDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlantillaDocumentoDAO {

    public static class ContenidoPlantilla {
        private final String nombreArchivo;
        private final byte[] contenido;

        public ContenidoPlantilla(String nombreArchivo, byte[] contenido) {
            this.nombreArchivo = nombreArchivo;
            this.contenido = contenido;
        }

        public String getNombreArchivo() {
            return nombreArchivo;
        }

        public byte[] getContenido() {
            return contenido;
        }
    }

    public List<PlantillaDocumentoDTO> listarTiposConPlantillaAnalisis() throws SQLException {
        String sql = "SELECT tda.id_tipo_documento_adjunto, tda.codigo AS tipo_codigo, tda.nombre AS tipo_nombre, "
                + "pd.id_plantilla_documento, pd.version, pd.nombre_archivo, pd.tamano_bytes, pd.comentario, "
                + "pd.activo, u.nombre_completo AS cargado_por, pd.creado_en "
                + "FROM tipo_documento_adjunto tda "
                + "LEFT JOIN plantilla_documento pd ON pd.id_tipo_documento_adjunto = tda.id_tipo_documento_adjunto AND pd.activo = 1 "
                + "LEFT JOIN usuario u ON u.id_usuario = pd.creado_por "
                + "WHERE tda.activo = 1 AND tda.codigo LIKE 'ANALISIS\\_%' ESCAPE '\\' "
                + "ORDER BY tda.codigo";
        try (Connection conn = SdrercAppConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<PlantillaDocumentoDTO> items = new ArrayList<PlantillaDocumentoDTO>();
            while (rs.next()) {
                items.add(map(rs));
            }
            return items;
        }
    }

    public List<PlantillaDocumentoDTO> listarHistorial(Long idTipoDocumentoAdjunto) throws SQLException {
        String sql = "SELECT tda.id_tipo_documento_adjunto, tda.codigo AS tipo_codigo, tda.nombre AS tipo_nombre, "
                + "pd.id_plantilla_documento, pd.version, pd.nombre_archivo, pd.tamano_bytes, pd.comentario, "
                + "pd.activo, u.nombre_completo AS cargado_por, pd.creado_en "
                + "FROM plantilla_documento pd "
                + "JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = pd.id_tipo_documento_adjunto "
                + "LEFT JOIN usuario u ON u.id_usuario = pd.creado_por "
                + "WHERE pd.id_tipo_documento_adjunto = ? "
                + "ORDER BY pd.version DESC";
        try (Connection conn = SdrercAppConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTipoDocumentoAdjunto);
            try (ResultSet rs = ps.executeQuery()) {
                List<PlantillaDocumentoDTO> items = new ArrayList<PlantillaDocumentoDTO>();
                while (rs.next()) {
                    items.add(map(rs));
                }
                return items;
            }
        }
    }

    public ContenidoPlantilla obtenerActivaPorCodigoTipo(String codigoTipoDocumento) throws SQLException {
        if (codigoTipoDocumento == null || codigoTipoDocumento.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT pd.nombre_archivo, pd.contenido "
                + "FROM plantilla_documento pd "
                + "JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = pd.id_tipo_documento_adjunto "
                + "WHERE pd.activo = 1 AND UPPER(tda.codigo) = UPPER(?)";
        try (Connection conn = SdrercAppConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoTipoDocumento.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new ContenidoPlantilla(rs.getString("nombre_archivo"), rs.getBytes("contenido"));
            }
        }
    }

    public ContenidoPlantilla obtenerContenido(Long idPlantillaDocumento) throws SQLException {
        String sql = "SELECT nombre_archivo, contenido FROM plantilla_documento WHERE id_plantilla_documento = ?";
        try (Connection conn = SdrercAppConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idPlantillaDocumento);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No se encontró la versión de plantilla seleccionada.");
                }
                return new ContenidoPlantilla(rs.getString("nombre_archivo"), rs.getBytes("contenido"));
            }
        }
    }

    public Long insertarVersion(
            Long idTipoDocumentoAdjunto,
            String nombreArchivo,
            byte[] contenido,
            String comentario,
            Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                desactivarVersionesDeTipo(conn, idTipoDocumentoAdjunto);
                long siguienteVersion = obtenerSiguienteVersion(conn, idTipoDocumentoAdjunto);
                String sql = "INSERT INTO plantilla_documento ("
                        + "id_tipo_documento_adjunto, version, nombre_archivo, contenido, tamano_bytes, "
                        + "comentario, activo, creado_por, creado_en"
                        + ") VALUES (?, ?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
                Long idGenerado;
                try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"id_plantilla_documento"})) {
                    ps.setLong(1, idTipoDocumentoAdjunto);
                    ps.setLong(2, siguienteVersion);
                    ps.setString(3, nombreArchivo);
                    ps.setBytes(4, contenido);
                    ps.setLong(5, contenido == null ? 0L : contenido.length);
                    ps.setString(6, comentario);
                    setLongOrNull(ps, 7, idUsuario);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        idGenerado = keys.next() ? keys.getLong(1) : null;
                    }
                }
                conn.commit();
                return idGenerado;
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

    public void activarVersion(Long idPlantillaDocumento, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idTipoDocumentoAdjunto = obtenerIdTipoDocumento(conn, idPlantillaDocumento);
                if (idTipoDocumentoAdjunto == null) {
                    throw new SQLException("No se encontró la versión de plantilla seleccionada.");
                }
                desactivarVersionesDeTipo(conn, idTipoDocumentoAdjunto);
                String sql = "UPDATE plantilla_documento SET activo = 1, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                        + "WHERE id_plantilla_documento = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    setLongOrNull(ps, 1, idUsuario);
                    ps.setLong(2, idPlantillaDocumento);
                    int updated = ps.executeUpdate();
                    if (updated != 1) {
                        throw new SQLException("No se pudo activar la versión de plantilla seleccionada.");
                    }
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

    private Long obtenerIdTipoDocumento(Connection conn, Long idPlantillaDocumento) throws SQLException {
        String sql = "SELECT id_tipo_documento_adjunto FROM plantilla_documento WHERE id_plantilla_documento = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idPlantillaDocumento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Long.valueOf(rs.getLong(1)) : null;
            }
        }
    }

    private void desactivarVersionesDeTipo(Connection conn, Long idTipoDocumentoAdjunto) throws SQLException {
        String sql = "UPDATE plantilla_documento SET activo = 0, modificado_en = SYSTIMESTAMP "
                + "WHERE id_tipo_documento_adjunto = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTipoDocumentoAdjunto);
            ps.executeUpdate();
        }
    }

    private long obtenerSiguienteVersion(Connection conn, Long idTipoDocumentoAdjunto) throws SQLException {
        String sql = "SELECT NVL(MAX(version), 0) + 1 FROM plantilla_documento WHERE id_tipo_documento_adjunto = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTipoDocumentoAdjunto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 1L;
            }
        }
    }

    private PlantillaDocumentoDTO map(ResultSet rs) throws SQLException {
        long idPlantilla = rs.getLong("id_plantilla_documento");
        return new PlantillaDocumentoDTO(
                rs.wasNull() ? null : Long.valueOf(idPlantilla),
                Long.valueOf(rs.getLong("id_tipo_documento_adjunto")),
                rs.getString("tipo_codigo"),
                rs.getString("tipo_nombre"),
                rs.getLong("version"),
                rs.getString("nombre_archivo"),
                rs.getLong("tamano_bytes"),
                rs.getString("comentario"),
                rs.getInt("activo") == 1,
                rs.getString("cargado_por"),
                toLocalDateTime(rs.getTimestamp("creado_en")));
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
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
