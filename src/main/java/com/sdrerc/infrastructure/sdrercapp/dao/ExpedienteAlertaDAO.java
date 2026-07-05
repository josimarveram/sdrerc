package com.sdrerc.infrastructure.sdrercapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ExpedienteAlertaDAO {

    public void registrarAlertas(
            Connection conn,
            Long idExpediente,
            String tipoAlerta,
            String nivel,
            List<String> mensajes,
            Long idUsuario) throws SQLException {
        if (conn == null || idExpediente == null || mensajes == null || mensajes.isEmpty()) {
            return;
        }
        String tipo = normalizarTexto(tipoAlerta, "OPERATIVA");
        String nivelNormalizado = normalizarTexto(nivel, "ALERTA");
        for (String mensaje : normalizarMensajes(mensajes)) {
            if (existeAlertaActiva(conn, idExpediente, tipo, mensaje)) {
                continue;
            }
            insertarAlerta(conn, idExpediente, tipo, nivelNormalizado, mensaje, idUsuario);
        }
    }

    public int marcarAtendidas(
            Connection conn,
            Long idExpediente,
            List<String> mensajes,
            Long idUsuario) throws SQLException {
        if (conn == null || idExpediente == null || mensajes == null || mensajes.isEmpty()) {
            return 0;
        }
        int total = 0;
        String sql = "UPDATE expediente_alerta "
                + "SET atendida = 1, activo = 0 "
                + "WHERE id_expediente = ? AND activo = 1 AND atendida = 0 AND UPPER(mensaje) = ?";
        for (String mensaje : normalizarMensajes(mensajes)) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idExpediente);
                ps.setString(2, mensaje.toUpperCase(Locale.ROOT));
                total += ps.executeUpdate();
            }
        }
        return total;
    }

    public String obtenerResumenAlertas(Connection conn, Long idExpediente) throws SQLException {
        if (conn == null || idExpediente == null) {
            return null;
        }
        String sql = "SELECT LISTAGG(mensaje, ' / ') WITHIN GROUP (ORDER BY creado_en, id_expediente_alerta) AS resumen "
                + "FROM expediente_alerta "
                + "WHERE id_expediente = ? AND activo = 1 AND atendida = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return normalizarTexto(rs.getString("resumen"), null);
            }
        }
    }

    private void insertarAlerta(
            Connection conn,
            Long idExpediente,
            String tipoAlerta,
            String nivel,
            String mensaje,
            Long idUsuario) throws SQLException {
        String sql = "INSERT INTO expediente_alerta ("
                + "id_expediente, tipo_alerta, mensaje, nivel, atendida, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, 0, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, limitar(tipoAlerta, 60));
            ps.setString(3, limitar(mensaje, 500));
            ps.setString(4, limitar(nivel, 20));
            if (idUsuario == null) {
                ps.setNull(5, java.sql.Types.NUMERIC);
            } else {
                ps.setLong(5, idUsuario);
            }
            ps.executeUpdate();
        }
    }

    private boolean existeAlertaActiva(Connection conn, Long idExpediente, String tipoAlerta, String mensaje) throws SQLException {
        String sql = "SELECT 1 FROM expediente_alerta "
                + "WHERE id_expediente = ? AND activo = 1 AND atendida = 0 "
                + "AND UPPER(tipo_alerta) = ? AND UPPER(mensaje) = ? AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, tipoAlerta.toUpperCase(Locale.ROOT));
            ps.setString(3, mensaje.toUpperCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private List<String> normalizarMensajes(List<String> mensajes) {
        Set<String> unicos = new LinkedHashSet<>();
        for (String mensaje : mensajes) {
            String limpio = normalizarTexto(mensaje, null);
            if (limpio != null) {
                unicos.add(limpio);
            }
        }
        return new ArrayList<>(unicos);
    }

    private String normalizarTexto(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private String limitar(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        int low = 0;
        int high = Math.min(value.length(), maxLength);
        while (low < high) {
            int mid = (low + high + 1) >>> 1;
            if (value.substring(0, mid).getBytes(StandardCharsets.UTF_8).length <= maxLength) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return value.substring(0, low);
    }
}
