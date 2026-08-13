package com.sdrerc.v3.infrastructure.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Repository;

/**
 * Subconjunto de com.sdrerc.infrastructure.sdrercapp.dao.FeriadoNacionalDAO (V2): solo
 * {@code listarFechasActivas}, usado por CalendarioLaboralService para el cálculo de fecha de
 * vencimiento. Mismo SQL.
 */
@Repository
public class FeriadoNacionalDAO {

    public Set<LocalDate> listarFechasActivas(Connection conn, LocalDate desde, LocalDate hasta) throws SQLException {
        Set<LocalDate> fechas = new LinkedHashSet<>();
        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            return fechas;
        }
        String sql = "SELECT fecha FROM feriado_nacional "
                + "WHERE activo = 1 AND fecha >= ? AND fecha <= ? "
                + "ORDER BY fecha";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date fecha = rs.getDate("fecha");
                    if (fecha != null) {
                        fechas.add(fecha.toLocalDate());
                    }
                }
            }
        }
        return fechas;
    }
}
