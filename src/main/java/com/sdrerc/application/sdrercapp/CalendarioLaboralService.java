package com.sdrerc.application.sdrercapp;

import com.sdrerc.infrastructure.database.SdrercAppConnection;
import com.sdrerc.domain.dto.sdrercapp.PlazoConfiguracionDTO;
import com.sdrerc.domain.rules.ProcedimientoRegistralRules;
import com.sdrerc.infrastructure.sdrercapp.dao.FeriadoNacionalDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.PlazoConfiguracionDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
        return calcularFechaVencimientoSolicitud(conn, fechaBase, null);
    }

    public LocalDate calcularFechaVencimientoSolicitud(
            Connection conn,
            LocalDate fechaBase,
            String procedimientoRegistral) throws SQLException {
        PlazoConfiguracionDTO plazo = resolverPlazoSolicitud(conn, procedimientoRegistral);
        if (plazo == null) {
            return calcularFechaVencimientoHabil(conn, fechaBase, PLAZO_SOLICITUD_DIAS_HABILES_DEFAULT);
        }
        int diasPlazo = diasPlazoValido(
                plazo,
                ProcedimientoRegistralRules.resolverDiasHabilesFallback(procedimientoRegistral));
        if (PlazoConfiguracionDTO.UNIDAD_CALENDARIO.equals(plazo.getUnidadPlazo())) {
            return calcularFechaVencimientoCalendario(fechaBase, diasPlazo);
        }
        return calcularFechaVencimientoHabil(conn, fechaBase, diasPlazo);
    }

    public LocalDate calcularFechaVencimientoSolicitud(LocalDate fechaBase) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return calcularFechaVencimientoSolicitud(conn, fechaBase);
        }
    }

    public LocalDate calcularFechaVencimientoSolicitud(LocalDate fechaBase, String procedimientoRegistral) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return calcularFechaVencimientoSolicitud(conn, fechaBase, procedimientoRegistral);
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

    public LocalDate calcularFechaVencimientoCalendario(LocalDate fechaBase, int diasCalendario) {
        if (fechaBase == null) {
            return null;
        }
        if (diasCalendario <= 0) {
            return fechaBase;
        }
        return fechaBase.plusDays(diasCalendario);
    }

    public Long calcularDiasHabilesRestantes(Connection conn, Date fechaVencimiento) throws SQLException {
        PlazoConfiguracionDTO plazo = resolverPlazoSolicitud(conn);
        if (plazo != null && PlazoConfiguracionDTO.UNIDAD_CALENDARIO.equals(plazo.getUnidadPlazo())) {
            return fechaVencimiento == null
                    ? null
                    : Long.valueOf(ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento.toLocalDate()));
        }
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
        PlazoConfiguracionDTO plazo = resolverPlazoSolicitud(conn);
        return diasPlazoValido(plazo, PLAZO_SOLICITUD_DIAS_HABILES_DEFAULT);
    }

    public int resolverDiasPlazoSolicitud(Connection conn, String procedimientoRegistral) throws SQLException {
        PlazoConfiguracionDTO plazo = resolverPlazoSolicitud(conn, procedimientoRegistral);
        return diasPlazoValido(plazo, ProcedimientoRegistralRules.resolverDiasHabilesFallback(procedimientoRegistral));
    }

    public PlazoConfiguracionDTO resolverPlazoSolicitud(Connection conn) throws SQLException {
        try {
            return plazoConfiguracionDAO.obtenerPlazoSolicitud(conn);
        } catch (SQLException ex) {
            if (esObjetoNoExiste(ex) || esColumnaNoExiste(ex)) {
                PlazoConfiguracionDTO fallback = new PlazoConfiguracionDTO();
                fallback.setDiasPlazo(Integer.valueOf(PLAZO_SOLICITUD_DIAS_HABILES_DEFAULT));
                fallback.setUnidadPlazo(PlazoConfiguracionDTO.UNIDAD_HABILES);
                fallback.setActivo(true);
                return fallback;
            }
            throw ex;
        }
    }

    public PlazoConfiguracionDTO resolverPlazoSolicitud(Connection conn, String procedimientoRegistral) throws SQLException {
        String codigoPlazo = ProcedimientoRegistralRules.resolverCodigoPlazoSolicitud(procedimientoRegistral);
        if (PlazoConfiguracionDTO.CODIGO_SOLICITUD_SDRERC.equals(codigoPlazo)) {
            return resolverPlazoSolicitud(conn);
        }
        try {
            PlazoConfiguracionDTO plazo = plazoConfiguracionDAO.obtenerPlazoPorCodigo(conn, codigoPlazo);
            return plazo == null ? crearFallbackProcedimiento(codigoPlazo, procedimientoRegistral) : plazo;
        } catch (SQLException ex) {
            if (esObjetoNoExiste(ex) || esColumnaNoExiste(ex)) {
                return crearFallbackProcedimiento(codigoPlazo, procedimientoRegistral);
            }
            throw ex;
        }
    }

    private PlazoConfiguracionDTO crearFallbackProcedimiento(String codigoPlazo, String procedimientoRegistral) {
        PlazoConfiguracionDTO fallback = new PlazoConfiguracionDTO();
        fallback.setCodigo(codigoPlazo);
        fallback.setAmbito(codigoPlazo);
        fallback.setNombre("Plazo técnico de contingencia para " + codigoPlazo);
        fallback.setDiasPlazo(Integer.valueOf(ProcedimientoRegistralRules.resolverDiasHabilesFallback(procedimientoRegistral)));
        fallback.setUnidadPlazo(PlazoConfiguracionDTO.UNIDAD_HABILES);
        fallback.setActivo(true);
        return fallback;
    }

    private int diasPlazoValido(PlazoConfiguracionDTO plazo, int fallback) {
        return plazo == null || plazo.getDiasPlazo() == null || plazo.getDiasPlazo().intValue() <= 0
                ? fallback
                : plazo.getDiasPlazo().intValue();
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
