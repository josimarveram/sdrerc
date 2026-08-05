package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.application.sdrercapp.CalendarioLaboralService;
import com.sdrerc.domain.dto.sdrercapp.CargaLaboralAbogadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CargaLaboralDocumentoDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UsuarioAsignacionDAO {

    private final CalendarioLaboralService calendarioLaboralService;

    public UsuarioAsignacionDAO() {
        this(new CalendarioLaboralService());
    }

    public UsuarioAsignacionDAO(CalendarioLaboralService calendarioLaboralService) {
        this.calendarioLaboralService = calendarioLaboralService;
    }

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
        sql.append(subconsultaAnalisisPorRecibir());
        sql.append(subconsultaAnalisisEnProceso());
        sql.append(subconsultaAnalisisObservado());
        sql.append(subconsultaAnalisisCartaIntermedia());
        sql.append(subconsultaConteoPorEtapa("VERIFICACION", "en_verificacion"));
        sql.append(subconsultaConteoPorEtapa("EJECUCION", "en_ejecucion"));
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
        sql.append("ORDER BY (analisis_por_recibir + analisis_en_proceso + analisis_observado + analisis_carta_intermedia ");
        sql.append("+ en_verificacion + en_ejecucion) ASC, vencidos ASC, u.nombre_completo ASC");

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
                            rs.getInt("analisis_por_recibir"),
                            rs.getInt("analisis_en_proceso"),
                            rs.getInt("analisis_observado"),
                            rs.getInt("analisis_carta_intermedia"),
                            rs.getInt("en_verificacion"),
                            rs.getInt("en_ejecucion"),
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

    /**
     * Condicion (para EXISTS/NOT EXISTS) de "tiene una carta intermedia ya respondida y derivada de
     * vuelta al equipo de Analisis": documento activo de clasificacion INTERMEDIO, que exigia
     * respuesta, ya fue notificado, y ya tiene confirmacion de respuesta registrada (mismo criterio
     * de "ya derivado a Analisis" que usa {@code DocumentoAnalisisDAO.listarCartasRespuestaPendientes},
     * salvo que aqui exige ademas que la respuesta ya este confirmada).
     */
    private static final String CONDICION_CARTA_INTERMEDIA_RESPONDIDA =
            "EXISTS (SELECT 1 FROM expediente_documento_analizado da "
                    + "JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                    + "WHERE da.id_expediente = e.id_expediente AND da.activo = 1 "
                    + "AND UPPER(NVL(tda.clasificacion, '')) = 'INTERMEDIO' "
                    + "AND NVL(da.requiere_respuesta, 0) = 1 AND NVL(da.notificado, 0) = 1 "
                    + "AND da.confirmacion_respuesta IS NOT NULL)";

    /**
     * "Por recibir": asignado desde Asignacion (EXPEDIENTE_ASIGNACION activa) pero el abogado
     * todavia no hizo clic en "Recibir expediente" (sigue en etapa ASIGNACION/estado ASIGNADO,
     * no ha pasado a etapa ANALISIS). Antes de esta subcolumna, estos expedientes no se contaban
     * como carga de Analisis en absoluto (solo se contaba et.codigo = 'ANALISIS').
     */
    private static String subconsultaAnalisisPorRecibir() {
        return "(SELECT COUNT(*) FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = u.id_usuario "
                + "AND et.codigo = 'ASIGNACION' AND es.codigo = 'ASIGNADO') AS analisis_por_recibir, ";
    }

    /** "Observado": el expediente esta en Analisis con estado OBSERVADO (regreso de Verificacion/Notificacion). */
    private static String subconsultaAnalisisObservado() {
        return "(SELECT COUNT(*) FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = u.id_usuario "
                + "AND et.codigo = 'ANALISIS' AND es.codigo = 'OBSERVADO') AS analisis_observado, ";
    }

    /** "Carta intermedia": ya recibido en Analisis, no OBSERVADO, con una carta intermedia ya respondida. */
    private static String subconsultaAnalisisCartaIntermedia() {
        return "(SELECT COUNT(*) FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = u.id_usuario "
                + "AND et.codigo = 'ANALISIS' AND es.codigo <> 'OBSERVADO' "
                + "AND " + CONDICION_CARTA_INTERMEDIA_RESPONDIDA + ") AS analisis_carta_intermedia, ";
    }

    /** "En analisis": recibido, no OBSERVADO y sin una carta intermedia pendiente de atender (resto). */
    private static String subconsultaAnalisisEnProceso() {
        return "(SELECT COUNT(*) FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = u.id_usuario "
                + "AND et.codigo = 'ANALISIS' AND es.codigo <> 'OBSERVADO' "
                + "AND NOT " + CONDICION_CARTA_INTERMEDIA_RESPONDIDA + ") AS analisis_en_proceso, ";
    }

    /**
     * Listado fila-por-fila (no agregado) de los expedientes que forman la carga de Analisis de
     * un abogado (los mismos 4 buckets que las subcolumnas Por recibir/En analisis/Observado/
     * Carta intermedia de {@link #listarCargaLaboralAbogados(Long)}), con estado y dias habiles
     * restantes ya calculados, para el panel lateral "Detalle de carga" de la bandeja Carga
     * Abogados (doble clic sobre una fila). Deliberadamente NO incluye Verificacion/Ejecucion:
     * ese detalle solo debe reflejar lo que las 4 columnas visibles de la grilla ya muestran.
     */
    public List<CargaLaboralDocumentoDTO> listarDocumentosPorAbogado(Long idUsuario) throws SQLException {
        List<CargaLaboralDocumentoDTO> documentos = new ArrayList<>();
        if (idUsuario == null) {
            return documentos;
        }
        String sql = "SELECT e.id_expediente, e.numero_expediente, et.nombre AS etapa, "
                + "es.nombre AS estado, e.fecha_vencimiento "
                + "FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = ? "
                + "AND ((et.codigo = 'ASIGNACION' AND es.codigo = 'ASIGNADO') "
                + "     OR et.codigo = 'ANALISIS') "
                + "ORDER BY e.fecha_vencimiento ASC NULLS LAST, e.numero_expediente ASC";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date fechaVencimiento = rs.getDate("fecha_vencimiento");
                    documentos.add(new CargaLaboralDocumentoDTO(
                            getLongOrNull(rs, "id_expediente"),
                            rs.getString("numero_expediente"),
                            rs.getString("etapa"),
                            rs.getString("estado"),
                            fechaVencimiento == null ? null : fechaVencimiento.toLocalDate(),
                            calendarioLaboralService.calcularDiasHabilesRestantes(conn, fechaVencimiento)));
                }
            }
        }
        return documentos;
    }

    /**
     * IDs de abogado (EXPEDIENTE_ASIGNACION.id_usuario_asignado) con al menos un expediente de
     * carga de Analisis (mismo alcance que {@link #listarDocumentosPorAbogado(Long)}: Por recibir
     * o etapa Analisis, sin Verificacion/Ejecucion) cuya fecha de vencimiento cae en [desde, hasta].
     * Cualquiera de los dos limites puede ser nulo (rango abierto de ese lado). Usado por el
     * filtro de fechas del panel de busqueda de Carga Abogados; el filtrado real de la lista
     * ocurre en memoria (Java) sobre estos ids, sin tocar la consulta agregada principal.
     */
    public Set<Long> listarIdsUsuarioConVencimientoEnRango(LocalDate desde, LocalDate hasta) throws SQLException {
        Set<Long> ids = new HashSet<>();
        if (desde == null && hasta == null) {
            return ids;
        }
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sql.append("SELECT DISTINCT ea.id_usuario_asignado AS id_usuario FROM expediente e ");
        sql.append("JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 ");
        sql.append("JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual ");
        sql.append("JOIN estado_expediente es ON es.id_estado = e.id_estado_actual ");
        sql.append("WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 ");
        sql.append("AND ((et.codigo = 'ASIGNACION' AND es.codigo = 'ASIGNADO') ");
        sql.append("     OR et.codigo = 'ANALISIS') ");
        sql.append("AND e.fecha_vencimiento IS NOT NULL ");
        if (desde != null) {
            sql.append("AND TRUNC(e.fecha_vencimiento) >= ? ");
            params.add(Date.valueOf(desde));
        }
        if (hasta != null) {
            sql.append("AND TRUNC(e.fecha_vencimiento) <= ? ");
            params.add(Date.valueOf(hasta));
        }
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long idUsuario = getLongOrNull(rs, "id_usuario");
                    if (idUsuario != null) {
                        ids.add(idUsuario);
                    }
                }
            }
        }
        return ids;
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
