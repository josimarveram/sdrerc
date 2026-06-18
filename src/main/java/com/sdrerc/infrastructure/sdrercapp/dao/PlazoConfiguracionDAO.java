package com.sdrerc.infrastructure.sdrercapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlazoConfiguracionDAO {

    public Integer obtenerDiasPlazoSolicitud(Connection conn) throws SQLException {
        String sql = "SELECT dias_plazo FROM ("
                + "SELECT pc.dias_plazo "
                + "FROM plazo_configuracion pc "
                + "LEFT JOIN etapa_expediente et ON et.id_etapa = pc.id_etapa "
                + "WHERE pc.activo = 1 "
                + "AND (pc.id_etapa IS NULL OR UPPER(et.codigo) = 'REGISTRO') "
                + "ORDER BY CASE WHEN UPPER(et.codigo) = 'REGISTRO' THEN 0 ELSE 1 END, "
                + "CASE WHEN pc.id_tipo_documento IS NULL THEN 0 ELSE 1 END, "
                + "pc.id_plazo_configuracion"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            int dias = rs.getInt("dias_plazo");
            return rs.wasNull() ? null : dias;
        }
    }
}
