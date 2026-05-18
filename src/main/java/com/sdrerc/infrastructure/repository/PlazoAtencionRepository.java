package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.PlazoAtencionConfig;
import com.sdrerc.infrastructure.database.OracleConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PlazoAtencionRepository {

    public Map<Integer, PlazoAtencionConfig> listarConfiguracionesActivas() throws SQLException
    {
        Map<Integer, PlazoAtencionConfig> configs = new HashMap<>();
        String sql = "SELECT ID_TIPO_DOCUMENTO, DIAS_PLAZO, PORCENTAJE_VERDE_DESDE, "
                + "PORCENTAJE_AMARILLO_DESDE, PORCENTAJE_ROJO_DESDE "
                + "FROM PLAZO_ATENCION_DOCUMENTO "
                + "WHERE ACTIVO = 1";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idTipoDocumento = rs.getInt("ID_TIPO_DOCUMENTO");
                configs.put(idTipoDocumento, new PlazoAtencionConfig(
                        idTipoDocumento,
                        rs.getInt("DIAS_PLAZO"),
                        rs.getInt("PORCENTAJE_VERDE_DESDE"),
                        rs.getInt("PORCENTAJE_AMARILLO_DESDE"),
                        rs.getInt("PORCENTAJE_ROJO_DESDE")
                ));
            }
        }
        return configs;
    }
}
