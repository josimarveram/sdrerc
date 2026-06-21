package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.application.sdrercapp.CalendarioLaboralService;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteBandejaDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExpedienteBandejaDAO {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private final CalendarioLaboralService calendarioLaboralService = new CalendarioLaboralService();

    public List<ExpedienteBandejaDTO> listarTodos() throws SQLException {
        return buscar(null, null, null, DEFAULT_LIMIT);
    }

    public List<ExpedienteBandejaDTO> buscarPorTexto(String textoLibre) throws SQLException {
        return buscar(textoLibre, null, null, DEFAULT_LIMIT);
    }

    public List<ExpedienteBandejaDTO> filtrarPorEtapa(String etapaCodigo) throws SQLException {
        return buscar(null, etapaCodigo, null, DEFAULT_LIMIT);
    }

    public List<ExpedienteBandejaDTO> filtrarPorEstado(String estadoCodigo) throws SQLException {
        return buscar(null, null, estadoCodigo, DEFAULT_LIMIT);
    }

    public List<ExpedienteBandejaDTO> buscar(String textoLibre, String etapaCodigo, String estadoCodigo, int limite) throws SQLException {
        return buscar(textoLibre, etapaCodigo, estadoCodigo, null, null, limite);
    }

    public List<ExpedienteBandejaDTO> buscar(
            String textoLibre,
            String etapaCodigo,
            String estadoCodigo,
            LocalDate fechaSolicitudDesde,
            LocalDate fechaSolicitudHasta,
            int limite) throws SQLException {
        List<Object> params = new ArrayList<>();
        boolean soportaGrupoFamiliar;
        try (Connection conn = SdrercAppConnection.getConnection()) {
            soportaGrupoFamiliar = soportaGrupoFamiliar(conn);
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("SELECT id_expediente, numero_expediente, numero_tramite_documentario, ");
        sql.append("etapa_codigo, estado_codigo, abogado_inicial, responsable_actual, equipo_actual, ");
        sql.append("(SELECT fecha_recepcion FROM (SELECT s.fecha_recepcion FROM expediente_solicitud s ");
        sql.append("WHERE s.id_expediente = b.id_expediente AND s.activo = 1 ORDER BY s.creado_en DESC) WHERE ROWNUM = 1) AS fecha_recepcion, ");
        sql.append("(SELECT canal FROM (SELECT cr.nombre AS canal FROM expediente_solicitud s ");
        sql.append("LEFT JOIN canal_recepcion cr ON cr.id_canal_recepcion = s.id_canal_recepcion ");
        sql.append("WHERE s.id_expediente = b.id_expediente AND s.activo = 1 ORDER BY s.creado_en DESC) WHERE ROWNUM = 1) AS canal, ");
        sql.append("(SELECT procedimiento FROM (SELECT s.asunto AS procedimiento FROM expediente_solicitud s ");
        sql.append("WHERE s.id_expediente = b.id_expediente AND s.activo = 1 ORDER BY s.creado_en DESC) WHERE ROWNUM = 1) AS procedimiento, ");
        sql.append("(SELECT tipo_acta FROM (SELECT ta.nombre AS tipo_acta FROM expediente_acta a ");
        sql.append("LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = a.id_tipo_acta ");
        sql.append("WHERE a.id_expediente = b.id_expediente AND a.activo = 1 ORDER BY a.creado_en DESC) WHERE ROWNUM = 1) AS tipo_acta, ");
        sql.append("(SELECT numero_acta FROM (SELECT a.numero_acta FROM expediente_acta a ");
        sql.append("WHERE a.id_expediente = b.id_expediente AND a.activo = 1 ORDER BY a.creado_en DESC) WHERE ROWNUM = 1) AS numero_acta, ");
        sql.append("(SELECT titular FROM (SELECT ").append(nombrePersona("p")).append(" AS titular ");
        sql.append("FROM expediente_persona ep JOIN persona p ON p.id_persona = ep.id_persona ");
        sql.append("WHERE ep.id_expediente = b.id_expediente AND ep.tipo_relacion_persona = 'TITULAR' AND ep.activo = 1 ");
        sql.append("ORDER BY ep.creado_en DESC) WHERE ROWNUM = 1) AS titular, ");
        sql.append("(SELECT COUNT(1) FROM expediente_relacion er WHERE er.activo = 1 ");
        sql.append("AND (er.id_expediente_principal = b.id_expediente OR er.id_expediente_relacionado = b.id_expediente)) AS cantidad_relaciones, ");
        if (soportaGrupoFamiliar) {
            sql.append("(SELECT NVL(sg.grupo_familiar, 0) FROM (SELECT s.grupo_familiar FROM expediente_solicitud s ");
            sql.append("WHERE s.id_expediente = b.id_expediente AND s.activo = 1 ORDER BY s.creado_en DESC, s.id_expediente_solicitud DESC) sg WHERE ROWNUM = 1) AS grupo_familiar_marca, ");
            sql.append("(SELECT sg.criterio_grupo_familiar FROM (SELECT s.criterio_grupo_familiar FROM expediente_solicitud s ");
            sql.append("WHERE s.id_expediente = b.id_expediente AND s.activo = 1 ORDER BY s.creado_en DESC, s.id_expediente_solicitud DESC) sg WHERE ROWNUM = 1) AS criterio_grupo_familiar, ");
            sql.append("(SELECT sg.observacion_grupo_familiar FROM (SELECT s.observacion_grupo_familiar FROM expediente_solicitud s ");
            sql.append("WHERE s.id_expediente = b.id_expediente AND s.activo = 1 ORDER BY s.creado_en DESC, s.id_expediente_solicitud DESC) sg WHERE ROWNUM = 1) AS observacion_grupo_familiar, ");
        } else {
            sql.append("0 AS grupo_familiar_marca, CAST(NULL AS VARCHAR2(80)) AS criterio_grupo_familiar, ");
            sql.append("CAST(NULL AS VARCHAR2(500)) AS observacion_grupo_familiar, ");
        }
        sql.append("fecha_registro, fecha_ultimo_movimiento, fecha_vencimiento, ");
        sql.append("requiere_publicacion, expediente_digital_completo ");
        sql.append("FROM vw_expediente_bandeja b WHERE 1 = 1 ");

        if (hasText(textoLibre)) {
            sql.append("AND (");
            sql.append("UPPER(numero_expediente) LIKE ? ");
            sql.append("OR UPPER(numero_tramite_documentario) LIKE ? ");
            sql.append("OR UPPER(etapa_codigo) LIKE ? ");
            sql.append("OR UPPER(estado_codigo) LIKE ? ");
            sql.append("OR UPPER(NVL(abogado_inicial, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(responsable_actual, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(equipo_actual, '')) LIKE ? ");
            sql.append("OR EXISTS (SELECT 1 FROM expediente_solicitud ss ");
            sql.append("WHERE ss.id_expediente = b.id_expediente AND ss.activo = 1 ");
            sql.append("AND UPPER(NVL(ss.numero_expediente_sgd, '')) LIKE ?) ");
            sql.append("OR EXISTS (SELECT 1 FROM expediente_acta aa ");
            sql.append("WHERE aa.id_expediente = b.id_expediente AND aa.activo = 1 ");
            sql.append("AND UPPER(NVL(aa.numero_acta, '')) LIKE ?) ");
            sql.append("OR EXISTS (SELECT 1 FROM expediente_persona ept JOIN persona pt ON pt.id_persona = ept.id_persona ");
            sql.append("WHERE ept.id_expediente = b.id_expediente AND ept.activo = 1 ");
            sql.append("AND UPPER(ept.tipo_relacion_persona) = 'TITULAR' ");
            sql.append("AND (UPPER(NVL(").append(nombrePersona("pt")).append(", '')) LIKE ? ");
            sql.append("OR UPPER(NVL(pt.numero_documento, '')) LIKE ?)) ");
            sql.append(") ");
            String pattern = "%" + textoLibre.trim().toUpperCase() + "%";
            for (int i = 0; i < 11; i++) {
                params.add(pattern);
            }
        }

        if (hasText(etapaCodigo)) {
            sql.append("AND UPPER(etapa_codigo) = ? ");
            params.add(etapaCodigo.trim().toUpperCase());
        }

        if (hasText(estadoCodigo)) {
            sql.append("AND UPPER(estado_codigo) = ? ");
            params.add(estadoCodigo.trim().toUpperCase());
        }

        if (fechaSolicitudDesde != null) {
            sql.append("AND EXISTS (SELECT 1 FROM expediente_solicitud sf ");
            sql.append("WHERE sf.id_expediente = b.id_expediente AND sf.activo = 1 ");
            sql.append("AND TRUNC(sf.fecha_recepcion) >= ?) ");
            params.add(Date.valueOf(fechaSolicitudDesde));
        }

        if (fechaSolicitudHasta != null) {
            sql.append("AND EXISTS (SELECT 1 FROM expediente_solicitud sf ");
            sql.append("WHERE sf.id_expediente = b.id_expediente AND sf.activo = 1 ");
            sql.append("AND TRUNC(sf.fecha_recepcion) <= ?) ");
            params.add(Date.valueOf(fechaSolicitudHasta));
        }

        sql.append("ORDER BY fecha_vencimiento ASC NULLS LAST, titular ASC NULLS LAST, id_expediente ASC");
        sql.append(") WHERE ROWNUM <= ?");
        params.add(normalizarLimite(limite));

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<ExpedienteBandejaDTO> expedientes = new ArrayList<>();
                while (rs.next()) {
                    expedientes.add(map(conn, rs));
                }
                return expedientes;
            }
        }
    }

    private ExpedienteBandejaDTO map(Connection conn, ResultSet rs) throws SQLException {
        Date fechaVencimiento = rs.getDate("fecha_vencimiento");
        return new ExpedienteBandejaDTO(
                getLongOrNull(rs, "id_expediente"),
                rs.getString("numero_expediente"),
                rs.getString("numero_tramite_documentario"),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                rs.getString("abogado_inicial"),
                rs.getString("responsable_actual"),
                rs.getString("equipo_actual"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                toLocalDateTime(rs.getTimestamp("fecha_registro")),
                toLocalDateTime(rs.getTimestamp("fecha_ultimo_movimiento")),
                toLocalDate(fechaVencimiento),
                getBooleanFromNumber(rs, "requiere_publicacion"),
                getBooleanFromNumber(rs, "expediente_digital_completo"),
                rs.getString("canal"),
                rs.getString("procedimiento"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                grupoFamiliar(
                        rs.getInt("cantidad_relaciones"),
                        rs.getInt("grupo_familiar_marca") == 1,
                        rs.getString("criterio_grupo_familiar"),
                        rs.getString("observacion_grupo_familiar")),
                rs.getString("titular"),
                calendarioLaboralService.calcularDiasHabilesRestantes(conn, fechaVencimiento)
        );
    }

    private static String grupoFamiliar(int cantidadRelaciones, boolean grupoFamiliar, String criterio, String observacion) {
        List<String> alertas = new ArrayList<>();
        if (cantidadRelaciones > 0) {
            alertas.add(cantidadRelaciones == 1 ? "1 asociado" : cantidadRelaciones + " asociados");
        }
        if (grupoFamiliar) {
            alertas.add("Grupo familiar");
        } else if (hasText(criterio) || hasText(observacion)) {
            alertas.add("Posible grupo familiar");
        }
        return alertas.isEmpty() ? "Sin alerta" : String.join(" / ", alertas);
    }

    private boolean soportaGrupoFamiliar(Connection conn) throws SQLException {
        String sql = "SELECT 1 FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_SOLICITUD' "
                + "AND column_name = 'GRUPO_FAMILIAR'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }

    private static String nombrePersona(String alias) {
        return "TRIM(NVL(" + alias + ".razon_social, TRIM(NVL(" + alias + ".nombres, '') || ' ' || NVL(" + alias + ".apellidos, ''))))";
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static int normalizarLimite(int limite) {
        if (limite <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limite, MAX_LIMIT);
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static boolean getBooleanFromNumber(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return !rs.wasNull() && value == 1;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
