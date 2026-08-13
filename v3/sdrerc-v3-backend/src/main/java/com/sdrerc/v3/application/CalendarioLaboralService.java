package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.PlazoConfiguracionDTO;
import com.sdrerc.v3.domain.rules.ProcedimientoRegistralRules;
import com.sdrerc.v3.infrastructure.dao.FeriadoNacionalDAO;
import com.sdrerc.v3.infrastructure.dao.PlazoConfiguracionDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Subconjunto de com.sdrerc.application.sdrercapp.CalendarioLaboralService (V2): cálculo de fecha
 * de vencimiento al crear una solicitud nueva, más el cálculo de "Días" restantes (con
 * congelamiento por carta intermedia pendiente) que necesitan las bandejas de listado. Mismo
 * SQL/lógica que V2 para lo portado.
 */
@Service
public class CalendarioLaboralService {

    public static final int PLAZO_SOLICITUD_DIAS_HABILES_DEFAULT = 30;

    private final FeriadoNacionalDAO feriadoNacionalDAO;
    private final PlazoConfiguracionDAO plazoConfiguracionDAO;

    public CalendarioLaboralService(FeriadoNacionalDAO feriadoNacionalDAO, PlazoConfiguracionDAO plazoConfiguracionDAO) {
        this.feriadoNacionalDAO = feriadoNacionalDAO;
        this.plazoConfiguracionDAO = plazoConfiguracionDAO;
    }

    public LocalDate calcularFechaVencimientoSolicitud(Connection conn, LocalDate fechaBase, String procedimientoRegistral)
            throws java.sql.SQLException {
        PlazoConfiguracionDTO plazo = resolverPlazoSolicitud(conn, procedimientoRegistral);
        int diasPlazo = diasPlazoValido(plazo, ProcedimientoRegistralRules.resolverDiasHabilesFallback(procedimientoRegistral));
        if (PlazoConfiguracionDTO.UNIDAD_CALENDARIO.equals(plazo.getUnidadPlazo())) {
            return calcularFechaVencimientoCalendario(fechaBase, diasPlazo);
        }
        return calcularFechaVencimientoHabil(conn, fechaBase, diasPlazo);
    }

    public LocalDate calcularFechaVencimientoHabil(Connection conn, LocalDate fechaBase, int diasHabiles) throws java.sql.SQLException {
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

    /**
     * "Días" restantes (con congelamiento). Port literal de la lógica de V2: mientras el
     * expediente tiene una carta intermedia ya emitida y esperando la respuesta del ciudadano, la
     * referencia para contar deja de ser "hoy" y se congela en la fecha de emisión de esa carta;
     * en cuanto se confirma la respuesta, el conteo vuelve a correr desde "hoy". Nunca modifica
     * EXPEDIENTE.FECHA_VENCIMIENTO, solo la fecha de referencia usada para mostrar "Días".
     */
    public Long calcularDiasHabilesRestantes(Connection conn, Long idExpediente, Date fechaVencimiento) throws java.sql.SQLException {
        LocalDate desde = resolverFechaReferenciaPlazo(conn, idExpediente);
        PlazoConfiguracionDTO plazo = resolverPlazoSolicitud(conn);
        if (plazo != null && PlazoConfiguracionDTO.UNIDAD_CALENDARIO.equals(plazo.getUnidadPlazo())) {
            return fechaVencimiento == null
                    ? null
                    : ChronoUnit.DAYS.between(desde, fechaVencimiento.toLocalDate());
        }
        return fechaVencimiento == null
                ? null
                : (long) calcularDiasHabilesRestantes(conn, desde, fechaVencimiento.toLocalDate());
    }

    private LocalDate resolverFechaReferenciaPlazo(Connection conn, Long idExpediente) throws java.sql.SQLException {
        if (idExpediente == null) {
            return LocalDate.now();
        }
        LocalDate fechaCongelada = resolverFechaEmisionCartaIntermediaPendiente(conn, idExpediente);
        return fechaCongelada != null ? fechaCongelada : LocalDate.now();
    }

    private LocalDate resolverFechaEmisionCartaIntermediaPendiente(Connection conn, Long idExpediente) throws java.sql.SQLException {
        String sql = "SELECT MAX(da.fecha_documento) AS fecha_congelada "
                + "FROM expediente_documento_analizado da "
                + "JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                + "WHERE da.id_expediente = ? AND da.activo = 1 "
                + "AND UPPER(NVL(tda.clasificacion, '')) = 'INTERMEDIO' "
                + "AND NVL(da.requiere_respuesta, 0) = 1 "
                + "AND da.fecha_documento IS NOT NULL "
                + "AND UPPER(NVL(da.confirmacion_respuesta, 'PENDIENTE')) = 'PENDIENTE'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date fecha = rs.getDate("fecha_congelada");
                    return fecha == null ? null : fecha.toLocalDate();
                }
            }
        }
        return null;
    }

    public int calcularDiasHabilesRestantes(Connection conn, LocalDate desde, LocalDate fechaVencimiento) throws java.sql.SQLException {
        if (desde == null || fechaVencimiento == null || desde.equals(fechaVencimiento)) {
            return 0;
        }
        LocalDate inicio = desde.isBefore(fechaVencimiento) ? desde.plusDays(1) : fechaVencimiento.plusDays(1);
        LocalDate fin = desde.isBefore(fechaVencimiento) ? fechaVencimiento : desde;
        Set<LocalDate> feriados = cargarFeriadosActivos(conn, inicio, fin);
        int dias = contarDiasHabiles(inicio, fin, feriados);
        return desde.isBefore(fechaVencimiento) ? dias : -dias;
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

    public PlazoConfiguracionDTO resolverPlazoSolicitud(Connection conn) throws java.sql.SQLException {
        try {
            PlazoConfiguracionDTO plazo = plazoConfiguracionDAO.obtenerPlazoSolicitud(conn);
            return plazo != null ? plazo : new PlazoConfiguracionDTO();
        } catch (java.sql.SQLException ex) {
            if (esObjetoNoExiste(ex) || esColumnaNoExiste(ex)) {
                return new PlazoConfiguracionDTO();
            }
            throw ex;
        }
    }

    public PlazoConfiguracionDTO resolverPlazoSolicitud(Connection conn, String procedimientoRegistral) throws java.sql.SQLException {
        String codigoPlazo = ProcedimientoRegistralRules.resolverCodigoPlazoSolicitud(procedimientoRegistral);
        if (PlazoConfiguracionDTO.CODIGO_SOLICITUD_SDRERC.equals(codigoPlazo)) {
            return resolverPlazoSolicitud(conn);
        }
        try {
            PlazoConfiguracionDTO plazo = plazoConfiguracionDAO.obtenerPlazoPorCodigo(conn, codigoPlazo);
            return plazo == null ? crearFallbackProcedimiento(codigoPlazo, procedimientoRegistral) : plazo;
        } catch (java.sql.SQLException ex) {
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
        fallback.setDiasPlazo(ProcedimientoRegistralRules.resolverDiasHabilesFallback(procedimientoRegistral));
        fallback.setUnidadPlazo(PlazoConfiguracionDTO.UNIDAD_HABILES);
        fallback.setActivo(true);
        return fallback;
    }

    private int diasPlazoValido(PlazoConfiguracionDTO plazo, int fallback) {
        return plazo == null || plazo.getDiasPlazo() == null || plazo.getDiasPlazo() <= 0
                ? fallback
                : plazo.getDiasPlazo();
    }

    private Set<LocalDate> cargarFeriadosActivos(Connection conn, LocalDate desde, LocalDate hasta) throws java.sql.SQLException {
        if (conn == null || desde == null || hasta == null || hasta.isBefore(desde)) {
            return Collections.emptySet();
        }
        try {
            return feriadoNacionalDAO.listarFechasActivas(conn, desde, hasta);
        } catch (java.sql.SQLException ex) {
            if (esObjetoNoExiste(ex)) {
                return Collections.emptySet();
            }
            throw ex;
        }
    }

    private boolean esObjetoNoExiste(java.sql.SQLException ex) {
        return ex != null && (ex.getErrorCode() == 942
                || (ex.getMessage() != null && ex.getMessage().contains("ORA-00942")));
    }

    private boolean esColumnaNoExiste(java.sql.SQLException ex) {
        return ex != null && (ex.getErrorCode() == 904
                || (ex.getMessage() != null && ex.getMessage().contains("ORA-00904")));
    }
}
