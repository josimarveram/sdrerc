package com.sdrerc.application.sdrercapp;

import com.sdrerc.infrastructure.database.SdrercAppConnection;
import com.sdrerc.infrastructure.sdrercapp.dao.FeriadoNacionalDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.PlazoConfiguracionDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

public class CalendarioLaboralService {

    public static final int PLAZO_SOLICITUD_DIAS_HABILES_DEFAULT = 30;

    private final FeriadoNacionalDAO feriadoNacionalDAO;
    private final PlazoConfiguracionDAO plazoConfiguracionDAO;

    public CalendarioLaboralService() {
        this(new FeriadoNacionalDAO(), new PlazoConfiguracionDAO());
    }

    public CalendarioLaboralService(FeriadoNacionalDAO feriadoNacionalDAO, PlazoConfiguracionDAO plazoConfiguracionDAO) {
        this.feriadoNacionalDAO = feriadoNacionalDAO;
        this.plazoConfiguracionDAO = plazoConfiguracionDAO;
    }

    public LocalDate calcularFechaVencimientoSolicitud(Connection conn, LocalDate fechaBase) throws SQLException {
        return calcularFechaVencimientoHabil(conn, fechaBase, resolverDiasPlazoSolicitud(conn));
    }

    public LocalDate calcularFechaVencimientoSolicitud(LocalDate fechaBase) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return calcularFechaVencimientoSolicitud(conn, fechaBase);
        }
    }

    public LocalDate calcularFechaVencimientoHabil(Connection conn, LocalDate fechaBase, int diasHabiles) throws SQLException {
        if (fechaBase == null) {
            return null;
        }
        if (diasHabiles <= 0) {
            return fechaBase;
        }

        LocalDate limiteConsulta = fechaBase.plusYears(2);
        Set<LocalDate> feriados = cargarFeriadosActivos(conn, fechaBase.plusDays(1), limiteConsulta);
        LocalDate cursor = fechaBase;
        int acumulado = 0;
        while (acumulado < diasHabiles) {
            cursor = cursor.plusDays(1);
            if (esDiaHabil(cursor, feriados)) {
                acumulado++;
            }
        }
        return cursor;
    }

    public Long calcularDiasHabilesRestantes(Connection conn, Date fechaVencimiento) throws SQLException {
        return fechaVencimiento == null
                ? null
                : Long.valueOf(calcularDiasHabilesRestantes(conn, LocalDate.now(), fechaVencimiento.toLocalDate()));
    }

    public Long calcularDiasHabilesRestantes(Date fechaVencimiento) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return calcularDiasHabilesRestantes(conn, fechaVencimiento);
        }
    }

    public int calcularDiasHabilesRestantes(Connection conn, LocalDate desde, LocalDate fechaVencimiento) throws SQLException {
        if (desde == null || fechaVencimiento == null || desde.equals(fechaVencimiento)) {
            return 0;
        }
        LocalDate inicio = desde.isBefore(fechaVencimiento) ? desde.plusDays(1) : fechaVencimiento.plusDays(1);
        LocalDate fin = desde.isBefore(fechaVencimiento) ? fechaVencimiento : desde;
        Set<LocalDate> feriados = cargarFeriadosActivos(conn, inicio, fin);
        int dias = contarDiasHabiles(inicio, fin, feriados);
        return desde.isBefore(fechaVencimiento) ? dias : -dias;
    }

    public boolean esDiaHabil(LocalDate fecha) throws SQLException {
        if (fecha == null) {
            return false;
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            Set<LocalDate> feriados = cargarFeriadosActivos(conn, fecha, fecha);
            return esDiaHabil(fecha, feriados);
        }
    }

    public boolean esDiaHabil(LocalDate fecha, Set<LocalDate> feriados) {
        if (fecha == null) {
            return false;
        }
        DayOfWeek day = fecha.getDayOfWeek();
        if (DayOfWeek.SATURDAY.equals(day) || DayOfWeek.SUNDAY.equals(day)) {
            return false;
        }
        return feriados == null || !feriados.contains(fecha);
    }

    public int resolverDiasPlazoSolicitud(Connection conn) throws SQLException {
        try {
            Integer dias = plazoConfiguracionDAO.obtenerDiasPlazoSolicitud(conn);
            return dias == null || dias.intValue() <= 0 ? PLAZO_SOLICITUD_DIAS_HABILES_DEFAULT : dias.intValue();
        } catch (SQLException ex) {
            if (esObjetoNoExiste(ex) || esColumnaNoExiste(ex)) {
                return PLAZO_SOLICITUD_DIAS_HABILES_DEFAULT;
            }
            throw ex;
        }
    }

    private int contarDiasHabiles(LocalDate inicio, LocalDate fin, Set<LocalDate> feriados) {
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            return 0;
        }
        int total = 0;
        LocalDate cursor = inicio;
        while (!cursor.isAfter(fin)) {
            if (esDiaHabil(cursor, feriados)) {
                total++;
            }
            cursor = cursor.plusDays(1);
        }
        return total;
    }

    private Set<LocalDate> cargarFeriadosActivos(Connection conn, LocalDate desde, LocalDate hasta) throws SQLException {
        if (conn == null || desde == null || hasta == null || hasta.isBefore(desde)) {
            return Collections.emptySet();
        }
        try {
            return feriadoNacionalDAO.listarFechasActivas(conn, desde, hasta);
        } catch (SQLException ex) {
            if (esObjetoNoExiste(ex)) {
                return Collections.emptySet();
            }
            throw ex;
        }
    }

    private boolean esObjetoNoExiste(SQLException ex) {
        return ex != null && (ex.getErrorCode() == 942
                || (ex.getMessage() != null && ex.getMessage().contains("ORA-00942")));
    }

    private boolean esColumnaNoExiste(SQLException ex) {
        return ex != null && (ex.getErrorCode() == 904
                || (ex.getMessage() != null && ex.getMessage().contains("ORA-00904")));
    }
}
