package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.CargaLaboralAbogadoDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioAsignacionDAO {

    public List<UsuarioAsignableDTO> listarAbogadosAsignables(Long idEquipo) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT u.id_usuario, u.username, u.nombre_completo, ");
        sql.append("eu.id_equipo, eq.nombre AS equipo_nombre, ");
        sql.append("(SELECT LISTAGG(r.codigo, ', ') WITHIN GROUP (ORDER BY r.codigo) ");
        sql.append("   FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol AND r.activo = 1 ");
        sql.append("  WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1) AS rol_codigo, ");
        sql.append("(SELECT MAX(sup.nombre_completo) FROM usuario_supervision us ");
        sql.append("   JOIN usuario sup ON sup.id_usuario = us.id_supervisor AND sup.activo = 1 ");
        sql.append("  WHERE us.id_abogado = u.id_usuario AND us.activo = 1) AS supervisor_nombre ");
        sql.append("FROM usuario u ");
        sql.append("LEFT JOIN equipo_usuario eu ON eu.id_usuario = u.id_usuario AND eu.activo = 1 ");
        sql.append("LEFT JOIN equipo eq ON eq.id_equipo = eu.id_equipo AND eq.activo = 1 ");
        sql.append("WHERE u.activo = 1 ");
        sql.append("AND UPPER(u.estado) = 'ACTIVO' ");
        sql.append("AND EXISTS (SELECT 1 FROM usuario_rol ur2 ");
        sql.append("  JOIN rol r2 ON r2.id_rol = ur2.id_rol AND r2.activo = 1 ");
        sql.append(" WHERE ur2.id_usuario = u.id_usuario AND ur2.activo = 1 ");
        sql.append("   AND UPPER(r2.codigo) IN ('ABOGADO', 'ANALISTA')) ");
        if (idEquipo != null) {
            sql.append("AND eu.id_equipo = ? ");
            params.add(idEquipo);
        }
        sql.append("ORDER BY u.nombre_completo");

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<UsuarioAsignableDTO> usuarios = new ArrayList<>();
                while (rs.next()) {
                    usuarios.add(new UsuarioAsignableDTO(
                            getLongOrNull(rs, "id_usuario"),
                            rs.getString("username"),
                            rs.getString("nombre_completo"),
                            getLongOrNull(rs, "id_equipo"),
                            rs.getString("equipo_nombre"),
                            rs.getString("rol_codigo"),
                            rs.getString("supervisor_nombre")));
                }
                return usuarios;
            }
        }
    }

    /**
     * Igual que {@link #listarAbogadosAsignables(Long)} pero sin restringir por rol
     * (ABOGADO/ANALISTA): sirve para combos de "usuario destino" hacia equipos que no son de
     * Analisis, como EQ_NOTIFICACION o EQ_VALIDACION, cuyos miembros tienen rol NOTIFICACION o
     * VALIDACION, no ABOGADO. La pertenencia al equipo (idEquipo) ya es el filtro relevante.
     */
    public List<UsuarioAsignableDTO> listarUsuariosAsignablesPorEquipo(Long idEquipo) throws SQLException {
        if (idEquipo == null) {
            return new ArrayList<>();
        }
        String sql = "SELECT DISTINCT u.id_usuario, u.username, u.nombre_completo, "
                + "eu.id_equipo, eq.nombre AS equipo_nombre, "
                + "(SELECT LISTAGG(r.codigo, ', ') WITHIN GROUP (ORDER BY r.codigo) "
                + "   FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol AND r.activo = 1 "
                + "  WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1) AS rol_codigo, "
                + "(SELECT MAX(sup.nombre_completo) FROM usuario_supervision us "
                + "   JOIN usuario sup ON sup.id_usuario = us.id_supervisor AND sup.activo = 1 "
                + "  WHERE us.id_abogado = u.id_usuario AND us.activo = 1) AS supervisor_nombre "
                + "FROM usuario u "
                + "JOIN equipo_usuario eu ON eu.id_usuario = u.id_usuario AND eu.activo = 1 "
                + "JOIN equipo eq ON eq.id_equipo = eu.id_equipo AND eq.activo = 1 "
                + "WHERE u.activo = 1 "
                + "AND UPPER(u.estado) = 'ACTIVO' "
                + "AND eu.id_equipo = ? "
                + "ORDER BY u.nombre_completo";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEquipo);
            try (ResultSet rs = ps.executeQuery()) {
                List<UsuarioAsignableDTO> usuarios = new ArrayList<>();
                while (rs.next()) {
                    usuarios.add(new UsuarioAsignableDTO(
                            getLongOrNull(rs, "id_usuario"),
                            rs.getString("username"),
                            rs.getString("nombre_completo"),
                            getLongOrNull(rs, "id_equipo"),
                            rs.getString("equipo_nombre"),
                            rs.getString("rol_codigo"),
                            rs.getString("supervisor_nombre")));
                }
                return usuarios;
            }
        }
    }

    public Long obtenerIdUsuarioActivoPorUsername(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT id_usuario FROM usuario "
                + "WHERE UPPER(username) = ? AND activo = 1 AND UPPER(estado) = 'ACTIVO'";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                long value = rs.getLong("id_usuario");
                return rs.wasNull() ? null : value;
            }
        }
    }

    /**
     * Carga laboral por abogado (no por equipo): un abogado que pertenece a 2+ equipos activos
     * antes generaba una fila duplicada por cada membresia (LEFT JOIN equipo_usuario en la tabla
     * conductora); ahora la tabla conductora es solo `usuario` (filtrada por rol via EXISTS) y el
     * supervisor se resuelve como subconsulta escalar (MAX), garantizando 1 fila por abogado. El
     * conteo de carga es por etapa (Analisis, Verificacion, Ejecucion: el abogado sigue el
     * expediente hasta que se ejecuta, no solo mientras esta en Analisis; en Verificacion el
     * expediente sigue ligado a el via EXPEDIENTE_ASIGNACION aunque quien actua ahi sea el
     * supervisor). Los campos *_detalle traen el desglose por estado ya formateado en SQL, para
     * mostrarlo como tooltip sin una segunda consulta.
     */
    public List<CargaLaboralAbogadoDTO> listarCargaLaboralAbogados(Long idEquipo) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT u.id_usuario, u.nombre_completo AS abogado, ");
        sql.append("(SELECT MAX(sup2.nombre_completo) FROM usuario_supervision us2 ");
        sql.append("   JOIN usuario sup2 ON sup2.id_usuario = us2.id_supervisor AND sup2.activo = 1 ");
        sql.append("  WHERE us2.id_abogado = u.id_usuario AND us2.activo = 1) AS supervisor, ");
        sql.append(subconsultaConteoPorEtapa("ANALISIS", "en_analisis"));
        sql.append(subconsultaDetallePorEtapa("ANALISIS", "analisis_detalle"));
        sql.append(subconsultaConteoPorEtapa("VERIFICACION", "en_verificacion"));
        sql.append(subconsultaDetallePorEtapa("VERIFICACION", "verificacion_detalle"));
        sql.append(subconsultaConteoPorEtapa("EJECUCION", "en_ejecucion"));
        sql.append(subconsultaDetallePorEtapa("EJECUCION", "ejecucion_detalle"));
        sql.append("(SELECT COUNT(*) FROM expediente e ");
        sql.append(" JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 ");
        sql.append(" WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 ");
        sql.append(" AND ea.id_usuario_asignado = u.id_usuario ");
        sql.append(" AND e.fecha_vencimiento IS NOT NULL ");
        sql.append(" AND TRUNC(e.fecha_vencimiento) BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + 5) AS por_vencer, ");
        sql.append("(SELECT COUNT(*) FROM expediente e ");
        sql.append(" JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 ");
        sql.append(" WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 ");
        sql.append(" AND ea.id_usuario_asignado = u.id_usuario ");
        sql.append(" AND e.fecha_vencimiento IS NOT NULL AND TRUNC(e.fecha_vencimiento) < TRUNC(SYSDATE)) AS vencidos ");
        sql.append("FROM usuario u ");
        sql.append("WHERE u.activo = 1 ");
        sql.append("AND UPPER(u.estado) = 'ACTIVO' ");
        sql.append("AND EXISTS (SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol AND r.activo = 1 ");
        sql.append("  WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1 ");
        sql.append("    AND UPPER(r.codigo) IN ('ABOGADO', 'ANALISTA')) ");
        if (idEquipo != null) {
            sql.append("AND EXISTS (SELECT 1 FROM equipo_usuario eu3 ");
            sql.append("  WHERE eu3.id_usuario = u.id_usuario AND eu3.activo = 1 AND eu3.id_equipo = ?) ");
            params.add(idEquipo);
        }
        sql.append("ORDER BY (en_analisis + en_verificacion + en_ejecucion) ASC, vencidos ASC, u.nombre_completo ASC");

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<CargaLaboralAbogadoDTO> cargas = new ArrayList<>();
                while (rs.next()) {
                    cargas.add(new CargaLaboralAbogadoDTO(
                            getLongOrNull(rs, "id_usuario"),
                            rs.getString("abogado"),
                            rs.getString("supervisor"),
                            rs.getInt("en_analisis"),
                            rs.getString("analisis_detalle"),
                            rs.getInt("en_verificacion"),
                            rs.getString("verificacion_detalle"),
                            rs.getInt("en_ejecucion"),
                            rs.getString("ejecucion_detalle"),
                            rs.getInt("por_vencer"),
                            rs.getInt("vencidos")));
                }
                return cargas;
            }
        }
    }

    private static String subconsultaConteoPorEtapa(String codigoEtapa, String alias) {
        return "(SELECT COUNT(*) FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = u.id_usuario AND et.codigo = '" + codigoEtapa + "') AS " + alias + ", ";
    }

    private static String subconsultaDetallePorEtapa(String codigoEtapa, String alias) {
        return "(SELECT LISTAGG(es.nombre || ': ' || g.cnt, ', ') WITHIN GROUP (ORDER BY es.nombre) "
                + "FROM (SELECT e.id_estado_actual, COUNT(*) AS cnt FROM expediente e "
                + "  JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "  JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "  WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "  AND ea.id_usuario_asignado = u.id_usuario AND et.codigo = '" + codigoEtapa + "' "
                + "  GROUP BY e.id_estado_actual) g "
                + "JOIN estado_expediente es ON es.id_estado = g.id_estado_actual) AS " + alias + ", ";
    }

    public List<Long> listarIdsEquipoDeUsuario(Long idUsuario) throws SQLException {
        List<Long> ids = new ArrayList<>();
        if (idUsuario == null) {
            return ids;
        }
        String sql = "SELECT id_equipo FROM equipo_usuario WHERE id_usuario = ? AND activo = 1";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long idEquipo = getLongOrNull(rs, "id_equipo");
                    if (idEquipo != null) {
                        ids.add(idEquipo);
                    }
                }
            }
        }
        return ids;
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
