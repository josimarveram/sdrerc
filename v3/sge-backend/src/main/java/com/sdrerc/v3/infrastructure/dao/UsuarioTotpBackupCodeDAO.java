package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.security.PasswordEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Port literal de com.sdrerc.infrastructure.sdrercapp.dao.UsuarioTotpBackupCodeDAO (V2). Mismo
 * SQL, mismo hasheo BCrypt de un solo uso; unico cambio es el origen de la conexion (DataSource
 * de Spring en vez de SdrercAppConnection estatico).
 */
@Repository
public class UsuarioTotpBackupCodeDAO {

    private final DataSource dataSource;

    public UsuarioTotpBackupCodeDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void generarYGuardarLote(Long idUsuario, List<String> codigosEnClaro) throws SQLException {
        String deleteSql = "DELETE FROM usuario_totp_backup_code WHERE id_usuario = ?";
        String insertSql = "INSERT INTO usuario_totp_backup_code (id_usuario, codigo_hash, usado, creado_en) "
                + "VALUES (?, ?, 0, SYSTIMESTAMP)";
        try (Connection conn = dataSource.getConnection()) {
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

    public boolean consumirSiValido(Long idUsuario, String codigoIngresado) throws SQLException {
        if (idUsuario == null || codigoIngresado == null || codigoIngresado.trim().isEmpty()) {
            return false;
        }
        String selectSql = "SELECT id_backup_code, codigo_hash FROM usuario_totp_backup_code "
                + "WHERE id_usuario = ? AND usado = 0";
        String updateSql = "UPDATE usuario_totp_backup_code SET usado = 1, usado_en = SYSTIMESTAMP "
                + "WHERE id_backup_code = ?";
        try (Connection conn = dataSource.getConnection()) {
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
