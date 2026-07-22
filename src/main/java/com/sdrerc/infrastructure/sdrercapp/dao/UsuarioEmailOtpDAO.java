package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.infrastructure.database.SdrercAppConnection;
import com.sdrerc.infrastructure.security.PasswordEncoder;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Código de verificación de un solo uso enviado por correo (2FA login V2), guardado en
 * {@code usuario_email_otp}. Mismo patrón que {@link UsuarioTotpBackupCodeDAO}: se guarda
 * hasheado con BCrypt (nunca en claro) y es de un solo uso; a diferencia de los backup codes,
 * además vence a los pocos minutos.
 */
public class UsuarioEmailOtpDAO {

    private static final int LONGITUD_CODIGO = 6;
    private static final int MINUTOS_EXPIRACION = 10;

    /**
     * Genera un código nuevo de {@value #LONGITUD_CODIGO} dígitos, invalida cualquier código
     * previo no usado del mismo usuario, y lo guarda hasheado con expiración a
     * {@value #MINUTOS_EXPIRACION} minutos. Retorna el código en claro para enviarlo por correo
     * (nunca se persiste ni se loguea en claro).
     */
    public String generarYGuardar(Long idUsuario) throws SQLException {
        String codigo = generarCodigoAleatorio();
        String deleteSql = "DELETE FROM usuario_email_otp WHERE id_usuario = ? AND usado = 0";
        String insertSql = "INSERT INTO usuario_email_otp (id_usuario, codigo_hash, expira_en, usado, creado_en) "
                + "VALUES (?, ?, ?, 0, SYSTIMESTAMP)";
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                    psDelete.setLong(1, idUsuario);
                    psDelete.executeUpdate();
                }
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setLong(1, idUsuario);
                    psInsert.setString(2, PasswordEncoder.hash(codigo));
                    psInsert.setTimestamp(3, Timestamp.valueOf(
                            java.time.LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION)));
                    psInsert.executeUpdate();
                }
                conn.commit();
            } catch (Exception ex) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                    // No ocultar el error original.
                }
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
        return codigo;
    }

    /**
     * Busca entre los códigos no usados y no vencidos del usuario uno que coincida (BCrypt). Si
     * encuentra coincidencia, lo marca como usado (de un solo uso) y retorna true.
     */
    public boolean consumirSiValido(Long idUsuario, String codigoIngresado) throws SQLException {
        if (idUsuario == null || codigoIngresado == null || codigoIngresado.trim().isEmpty()) {
            return false;
        }
        String selectSql = "SELECT id_email_otp, codigo_hash FROM usuario_email_otp "
                + "WHERE id_usuario = ? AND usado = 0 AND expira_en > SYSTIMESTAMP";
        String updateSql = "UPDATE usuario_email_otp SET usado = 1, usado_en = SYSTIMESTAMP "
                + "WHERE id_email_otp = ?";
        try (Connection conn = SdrercAppConnection.getConnection()) {
            Long idCoincidente = null;
            try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
                psSelect.setLong(1, idUsuario);
                try (ResultSet rs = psSelect.executeQuery()) {
                    while (rs.next()) {
                        String hash = rs.getString("codigo_hash");
                        if (hash != null && PasswordEncoder.matches(codigoIngresado.trim(), hash)) {
                            idCoincidente = rs.getLong("id_email_otp");
                            break;
                        }
                    }
                }
            }
            if (idCoincidente == null) {
                return false;
            }
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setLong(1, idCoincidente);
                psUpdate.executeUpdate();
            }
            return true;
        }
    }

    private static String generarCodigoAleatorio() {
        SecureRandom random = new SecureRandom();
        int max = (int) Math.pow(10, LONGITUD_CODIGO);
        int valor = random.nextInt(max);
        return String.format("%0" + LONGITUD_CODIGO + "d", valor);
    }
}
