package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.infrastructure.database.SdrercAppConnection;
import com.sdrerc.infrastructure.security.PasswordEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Códigos de respaldo TOTP de un solo uso (para cuando el usuario pierde su dispositivo
 * autenticador). Se guardan hasheados con BCrypt, igual que {@code usuario.password_hash}.
 */
public class UsuarioTotpBackupCodeDAO {

    public void generarYGuardarLote(Long idUsuario, List<String> codigosEnClaro) throws SQLException {
        String deleteSql = "DELETE FROM usuario_totp_backup_code WHERE id_usuario = ?";
        String insertSql = "INSERT INTO usuario_totp_backup_code (id_usuario, codigo_hash, usado, creado_en) "
                + "VALUES (?, ?, 0, SYSTIMESTAMP)";
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                    psDelete.setLong(1, idUsuario);
                    psDelete.executeUpdate();
                }
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    for (String codigo : codigosEnClaro) {
                        psInsert.setLong(1, idUsuario);
                        psInsert.setString(2, PasswordEncoder.hash(codigo));
                        psInsert.addBatch();
                    }
                    psInsert.executeBatch();
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
    }

    /**
     * Busca entre los códigos de respaldo no usados del usuario uno que coincida (BCrypt). Si
     * encuentra coincidencia, lo marca como usado (de un solo uso) y retorna true.
     */
    public boolean consumirSiValido(Long idUsuario, String codigoIngresado) throws SQLException {
        if (idUsuario == null || codigoIngresado == null || codigoIngresado.trim().isEmpty()) {
            return false;
        }
        String selectSql = "SELECT id_backup_code, codigo_hash FROM usuario_totp_backup_code "
                + "WHERE id_usuario = ? AND usado = 0";
        String updateSql = "UPDATE usuario_totp_backup_code SET usado = 1, usado_en = SYSTIMESTAMP "
                + "WHERE id_backup_code = ?";
        try (Connection conn = SdrercAppConnection.getConnection()) {
            Long idBackupCodeCoincidente = null;
            try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
                psSelect.setLong(1, idUsuario);
                try (ResultSet rs = psSelect.executeQuery()) {
                    while (rs.next()) {
                        String hash = rs.getString("codigo_hash");
                        if (hash != null && PasswordEncoder.matches(codigoIngresado.trim(), hash)) {
                            idBackupCodeCoincidente = rs.getLong("id_backup_code");
                            break;
                        }
                    }
                }
            }
            if (idBackupCodeCoincidente == null) {
                return false;
            }
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setLong(1, idBackupCodeCoincidente);
                psUpdate.executeUpdate();
            }
            return true;
        }
    }
}
