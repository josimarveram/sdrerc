package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.domain.PlantillaDocumentoAdminDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Port literal de {@code com.sdrerc.infrastructure.sdrercapp.dao.PlantillaDocumentoDAO} (V2):
 * versiones de plantilla Word (BLOB) por {@code TIPO_DOCUMENTO_ADJUNTO}, solo 1 activa a la vez
 * por tipo. Mismo alcance que V2: acotado a los tipos {@code ANALISIS_%} (los únicos con
 * generación de Word real hoy en V3, vía Análisis/Ejecución).
 */
@Repository
public class PlantillaDocumentoAdminDAO {

    public record ContenidoPlantilla(String nombreArchivo, byte[] contenido) {
    }

    private final DataSource dataSource;

    public PlantillaDocumentoAdminDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<PlantillaDocumentoAdminDTO> listarTiposConPlantilla() throws SQLException {
        String sql = "SELECT tda.id_tipo_documento_adjunto, tda.codigo AS tipo_codigo, tda.nombre AS tipo_nombre, "
                + "pd.id_plantilla_documento, pd.version, pd.nombre_archivo, pd.tamano_bytes, pd.comentario, "
                + "pd.activo, u.nombre_completo AS cargado_por, pd.creado_en "
                + "FROM tipo_documento_adjunto tda "
                + "LEFT JOIN plantilla_documento pd ON pd.id_tipo_documento_adjunto = tda.id_tipo_documento_adjunto AND pd.activo = 1 "
                + "LEFT JOIN usuario u ON u.id_usuario = pd.creado_por "
                + "WHERE tda.activo = 1 AND tda.codigo LIKE 'ANALISIS\\_%' ESCAPE '\\' "
                + "ORDER BY tda.codigo";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<PlantillaDocumentoAdminDTO> items = new ArrayList<>();
            while (rs.next()) {
                items.add(map(rs));
            }
            return items;
        }
    }

    public List<PlantillaDocumentoAdminDTO> listarHistorial(Long idTipoDocumentoAdjunto) throws SQLException {
        String sql = "SELECT tda.id_tipo_documento_adjunto, tda.codigo AS tipo_codigo, tda.nombre AS tipo_nombre, "
                + "pd.id_plantilla_documento, pd.version, pd.nombre_archivo, pd.tamano_bytes, pd.comentario, "
                + "pd.activo, u.nombre_completo AS cargado_por, pd.creado_en "
                + "FROM plantilla_documento pd "
                + "JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = pd.id_tipo_documento_adjunto "
                + "LEFT JOIN usuario u ON u.id_usuario = pd.creado_por "
                + "WHERE pd.id_tipo_documento_adjunto = ? "
                + "ORDER BY pd.version DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTipoDocumentoAdjunto);
            try (ResultSet rs = ps.executeQuery()) {
                List<PlantillaDocumentoAdminDTO> items = new ArrayList<>();
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
        try (Connection conn = dataSource.getConnection();
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
        try (Connection conn = dataSource.getConnection();
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

    public Long insertarVersion(Long idTipoDocumentoAdjunto, String nombreArchivo, byte[] contenido, String comentario,
            Long idUsuario) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
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
                if (ex instanceof SQLException sqlEx) {
                    throw sqlEx;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public void activarVersion(Long idPlantillaDocumento, Long idUsuario) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
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
                if (ex instanceof SQLException sqlEx) {
                    throw sqlEx;
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
                return rs.next() ? rs.getLong(1) : null;
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

    private PlantillaDocumentoAdminDTO map(ResultSet rs) throws SQLException {
        PlantillaDocumentoAdminDTO dto = new PlantillaDocumentoAdminDTO();
        dto.setIdPlantillaDocumento(getLongOrNull(rs, "id_plantilla_documento"));
        dto.setIdTipoDocumentoAdjunto(rs.getLong("id_tipo_documento_adjunto"));
        dto.setTipoDocumentoCodigo(rs.getString("tipo_codigo"));
        dto.setTipoDocumentoNombre(rs.getString("tipo_nombre"));
        dto.setVersion(rs.getLong("version"));
        dto.setNombreArchivo(rs.getString("nombre_archivo"));
        dto.setTamanoBytes(rs.getLong("tamano_bytes"));
        dto.setComentario(rs.getString("comentario"));
        dto.setActivo(rs.getInt("activo") == 1);
        dto.setCargadoPor(rs.getString("cargado_por"));
        dto.setCreadoEn(toLocalDateTime(rs.getTimestamp("creado_en")));
        return dto;
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

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // El error original se reporta al usuario; el rollback fallido no debe ocultarlo.
        }
    }
}
