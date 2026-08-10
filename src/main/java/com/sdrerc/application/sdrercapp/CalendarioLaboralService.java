package com.sdrerc.application.sdrercapp;

import com.sdrerc.infrastructure.database.SdrercAppConnection;
import com.sdrerc.domain.dto.sdrercapp.PlazoConfiguracionDTO;
import com.sdrerc.domain.rules.ProcedimientoRegistralRules;
import com.sdrerc.infrastructure.sdrercapp.dao.FeriadoNacionalDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.PlazoConfiguracionDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    /**
     * Sin idExpediente: comportamiento historico, cuenta siempre desde "hoy" (sin congelamiento).
     * Se mantiene solo para el caso en que no se disponga del id del expediente en la consulta;
     * los llamadores que ya lo tienen deben preferir el overload con idExpediente.
     */
    public Long calcularDiasHabilesRestantes(Connection conn, Date fechaVencimiento) throws SQLException {
        return calcularDiasHabilesRestantes(conn, null, fechaVencimiento);
    }

    public Long calcularDiasHabilesRestantes(Date fechaVencimiento) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return calcularDiasHabilesRestantes(conn, fechaVencimiento);
        }
    }

    /**
     * Congelamiento del conteo de "Dias" mientras el expediente tiene una carta intermedia ya
     * emitida y esperando la respuesta del ciudadano (09/08/2026, pedido explicito del usuario:
     * "cuando se tiene una carta intermedia y ya esta emitido deberia detener los dias desde la
     * fecha emision de la carta intermedia y se deberia reactivar el conteo desde la fecha de
     * asignacion al abogado de analisis (derivacion)"). En vez de "hoy", la referencia para contar
     * dias habiles restantes se congela en la fecha de emision de la carta intermedia mientras esta
     * pendiente; en cuanto se registra la confirmacion de respuesta (mismo campo
     * EXPEDIENTE_DOCUMENTO_ANALIZADO.CONFIRMACION_RESPUESTA que ya usa
     * UsuarioAsignacionDAO.CONDICION_CARTA_INTERMEDIA_RESPONDIDA para saber que la carta "ya fue
     * derivada de vuelta a Analisis"), el conteo vuelve a correr desde "hoy" normalmente. No se
     * modifica EXPEDIENTE.FECHA_VENCIMIENTO en ningun momento: es un ajuste de la fecha de
     * referencia usada solo para calcular/mostrar "Dias", no del plazo legal en si.
     */
    public Long calcularDiasHabilesRestantes(Connection conn, Long idExpediente, Date fechaVencimiento) throws SQLException {
        LocalDate desde = resolverFechaReferenciaPlazo(conn, idExpediente);
        PlazoConfiguracionDTO plazo = resolverPlazoSolicitud(conn);
        if (plazo != null && PlazoConfiguracionDTO.UNIDAD_CALENDARIO.equals(plazo.getUnidadPlazo())) {
            return fechaVencimiento == null
                    ? null
                    : Long.valueOf(ChronoUnit.DAYS.between(desde, fechaVencimiento.toLocalDate()));
        }
        return fechaVencimiento == null
                ? null
                : Long.valueOf(calcularDiasHabilesRestantes(conn, desde, fechaVencimiento.toLocalDate()));
    }

    public Long calcularDiasHabilesRestantes(Long idExpediente, Date fechaVencimiento) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return calcularDiasHabilesRestantes(conn, idExpediente, fechaVencimiento);
        }
    }

    /**
     * true si el conteo de "Dias" de este expediente esta actualmente congelado (ver
     * {@link #resolverFechaEmisionCartaIntermediaPendiente}); usado por la UI para mostrar el
     * icono de pausa junto al pill "Días" en el "Panel de datos" (09/08/2026, pedido explicito del
     * usuario).
     */
    public boolean tienePlazoCongelado(Connection conn, Long idExpediente) throws SQLException {
        return idExpediente != null && resolverFechaEmisionCartaIntermediaPendiente(conn, idExpediente) != null;
    }

    public boolean tienePlazoCongelado(Long idExpediente) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return tienePlazoCongelado(conn, idExpediente);
        }
    }

    private LocalDate resolverFechaReferenciaPlazo(Connection conn, Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return LocalDate.now();
        }
        LocalDate fechaCongelada = resolverFechaEmisionCartaIntermediaPendiente(conn, idExpediente);
        return fechaCongelada != null ? fechaCongelada : LocalDate.now();
    }

    /**
     * Ultima fecha de emision (MAX fecha_documento) entre las cartas intermedias activas del
     * expediente que ya fueron emitidas pero cuya respuesta del ciudadano todavia no se confirma;
     * null si no hay ninguna carta en ese estado (conteo normal).
     *
     * OJO (09/08/2026, diagnosticado con SELECT de solo lectura autorizado por el usuario sobre
     * SDRERC-EXP-2026-000005): `confirmacion_respuesta` NUNCA queda en NULL real una vez que el
     * documento se guarda con `requiere_respuesta=1` -- `DocumentoAnalisisDAO.normalizarConfirmacionRespuesta`
     * normaliza cualquier valor vacio/no reconocido al literal `'PENDIENTE'` desde el primer guardado
     * en Analisis, mucho antes de que el ciudadano responda. El primer intento de esta bandera
     * comparaba contra `IS NULL`, que en la practica nunca se cumplia (se verifico con el
     * expediente de ejemplo: `confirmacion_respuesta='PENDIENTE'`), asi que el congelamiento nunca
     * se activaba. Fix: tratar tanto `NULL` como el literal `'PENDIENTE'` como "todavia sin
     * confirmar" (sigue congelado); solo `'SI'`/`'NO'` (los otros 2 valores validos de
     * `normalizarConfirmacionRespuesta`) cuentan como respuesta ya confirmada y reactivan el conteo.
     */
    private LocalDate resolverFechaEmisionCartaIntermediaPendiente(Connection conn, Long idExpediente) throws SQLException {
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
