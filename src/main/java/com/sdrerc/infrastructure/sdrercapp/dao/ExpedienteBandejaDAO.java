package com.sdrerc.infrastructure.sdrercapp.dao;

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
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("SELECT id_expediente, numero_expediente, numero_tramite_documentario, ");
        sql.append("etapa_codigo, estado_codigo, abogado_inicial, responsable_actual, equipo_actual, ");
        sql.append("fecha_registro, fecha_ultimo_movimiento, fecha_vencimiento, ");
        sql.append("requiere_publicacion, expediente_digital_completo ");
        sql.append("FROM vw_expediente_bandeja WHERE 1 = 1 ");

        if (hasText(textoLibre)) {
            sql.append("AND (");
            sql.append("UPPER(numero_expediente) LIKE ? ");
            sql.append("OR UPPER(numero_tramite_documentario) LIKE ? ");
            sql.append("OR UPPER(etapa_codigo) LIKE ? ");
            sql.append("OR UPPER(estado_codigo) LIKE ? ");
            sql.append("OR UPPER(NVL(abogado_inicial, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(responsable_actual, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(equipo_actual, '')) LIKE ? ");
            sql.append(") ");
            String pattern = "%" + textoLibre.trim().toUpperCase() + "%";
            for (int i = 0; i < 7; i++) {
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

        sql.append("ORDER BY fecha_ultimo_movimiento DESC NULLS LAST, id_expediente DESC");
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
                    expedientes.add(map(rs));
                }
                return expedientes;
            }
        }
    }

    private ExpedienteBandejaDTO map(ResultSet rs) throws SQLException {
        return new ExpedienteBandejaDTO(
                getLongOrNull(rs, "id_expediente"),
                rs.getString("numero_expediente"),
                rs.getString("numero_tramite_documentario"),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                rs.getString("abogado_inicial"),
                rs.getString("responsable_actual"),
                rs.getString("equipo_actual"),
                toLocalDateTime(rs.getTimestamp("fecha_registro")),
                toLocalDateTime(rs.getTimestamp("fecha_ultimo_movimiento")),
                toLocalDate(rs.getDate("fecha_vencimiento")),
                getBooleanFromNumber(rs, "requiere_publicacion"),
                getBooleanFromNumber(rs, "expediente_digital_completo")
        );
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
