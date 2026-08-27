package com.sdrerc.v3.domain;

import java.time.LocalDate;

/**
 * Una fila de una de las 2 hojas "UNIFICADA" del Excel de carga inicial (migración histórica, ver
 * v3/CLAUDE.md). Alcance del Incremento 1: solo los campos de Registro + Asignación — el bloque
 * {@code EJECUCIONES.*} (Análisis/Verificación/Ejecución/Notificación) se agrega en incrementos
 * siguientes, no se lee todavía.
 *
 * <p>Regla de confidencialidad: esta clase nunca se loguea/imprime completa — solo campos
 * individuales no sensibles (fila, hoja) cuando hace falta reportar un error.</p>
 */
public class CargaInicialFilaDTO {

    public static final String HOJA_DIARIA = "DATA DIARIA UNIFICADA";
    public static final String HOJA_PASIVO_OR = "DATA PASIVO OR UNIFICADA";

    private String hojaOrigen;
    private int filaExcel;

    private LocalDate fechaSolicitud;
    private String tipoTramite;
    private String canalRecepcion;
    private String derivadoPor;
    private String dniSolicitante;
    private String numeroTramiteWeb;
    private String tipoDocumento;
    private String numeroDocumento;
    private String tipoSolicitud;
    private String tipoActa;
    private String numeroActa;
    private String titular;
    private String responsablePreevaluacion;
    private LocalDate fechaAsignado;
    private String numeroHojaEnvioAsignacion;
    private String asignadoAAbogado;

    /** Solo DATA PASIVO OR UNIFICADA — número de expediente legado, preservado tal cual (decisión confirmada). */
    private String numeroExpedienteLegado;

    /**
     * Columna "N EXPEDIENTE DIGITAL" — pese al nombre, no es un dato del módulo Expediente Digital:
     * marca que esta fila es un duplicado de otro expediente ya numerado (ej. "DUPLICADO_EXP 018",
     * "DUPLICIDAD_EXP 0329", "SDRERC_EXP_2026_000 018" — los 3 formatos reales encontrados en el
     * Excel, ver v3/CLAUDE.md 26/08/2026). El número se extrae y se usa para corregir
     * {@code numero_expediente} en un paso aparte, después de que la fila ya tiene expediente creado.
     */
    private String numeroExpedienteDigital;

    /**
     * Bloque EJECUCIONES.* (Incremento 2, ver v3/CLAUDE.md 26/08/2026) — cubre Análisis, el paso
     * técnico por Verificación y Ejecución. Notificación (cartas 1/2, oficio, memorando) queda
     * diferida a propósito (requeriría sintetizar un documento analizado, fuera de alcance).
     */
    private LocalDate fechaEmisionAnalisis;
    private String resultadoAnalisisTexto;
    private LocalDate fechaEjecucion;
    private String estadoEjecucionTexto;

    /**
     * Bloque Notificación (Incremento 3, ver v3/CLAUDE.md 26/08/2026) — número de resolución (para
     * sintetizar el documento Resolución), procedimiento (para determinar SU tipo real vía
     * "EJECUCIONES.PROCEDIMIENTO", pedido explícito del usuario) y los datos reales de las cartas
     * 1°/2° al ciudadano (medio de notificación, fecha/número de cada una) para reconstruir los
     * intentos de notificación.
     */
    private String numeroResolucion;
    private String resolucionLimpia;
    private String procedimientoEjecuciones;
    private String asignadoNotificacion;
    private String medioNotificacion;
    private LocalDate fechaEmision1Carta;
    private String numero1Carta;
    private LocalDate fechaElaboracion2Carta;
    private String numero2Carta;

    public boolean esPasivoOr() {
        return HOJA_PASIVO_OR.equals(hojaOrigen);
    }

    public String getHojaOrigen() {
        return hojaOrigen;
    }

    public void setHojaOrigen(String hojaOrigen) {
        this.hojaOrigen = hojaOrigen;
    }

    public int getFilaExcel() {
        return filaExcel;
    }

    public void setFilaExcel(int filaExcel) {
        this.filaExcel = filaExcel;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getTipoTramite() {
        return tipoTramite;
    }

    public void setTipoTramite(String tipoTramite) {
        this.tipoTramite = tipoTramite;
    }

    public String getCanalRecepcion() {
        return canalRecepcion;
    }

    public void setCanalRecepcion(String canalRecepcion) {
        this.canalRecepcion = canalRecepcion;
    }

    public String getDerivadoPor() {
        return derivadoPor;
    }

    public void setDerivadoPor(String derivadoPor) {
        this.derivadoPor = derivadoPor;
    }

    public String getDniSolicitante() {
        return dniSolicitante;
    }

    public void setDniSolicitante(String dniSolicitante) {
        this.dniSolicitante = dniSolicitante;
    }

    public String getNumeroTramiteWeb() {
        return numeroTramiteWeb;
    }

    public void setNumeroTramiteWeb(String numeroTramiteWeb) {
        this.numeroTramiteWeb = numeroTramiteWeb;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(String tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public String getTipoActa() {
        return tipoActa;
    }

    public void setTipoActa(String tipoActa) {
        this.tipoActa = tipoActa;
    }

    public String getNumeroActa() {
        return numeroActa;
    }

    public void setNumeroActa(String numeroActa) {
        this.numeroActa = numeroActa;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getResponsablePreevaluacion() {
        return responsablePreevaluacion;
    }

    public void setResponsablePreevaluacion(String responsablePreevaluacion) {
        this.responsablePreevaluacion = responsablePreevaluacion;
    }

    public LocalDate getFechaAsignado() {
        return fechaAsignado;
    }

    public void setFechaAsignado(LocalDate fechaAsignado) {
        this.fechaAsignado = fechaAsignado;
    }

    public String getNumeroHojaEnvioAsignacion() {
        return numeroHojaEnvioAsignacion;
    }

    public void setNumeroHojaEnvioAsignacion(String numeroHojaEnvioAsignacion) {
        this.numeroHojaEnvioAsignacion = numeroHojaEnvioAsignacion;
    }

    public String getAsignadoAAbogado() {
        return asignadoAAbogado;
    }

    public void setAsignadoAAbogado(String asignadoAAbogado) {
        this.asignadoAAbogado = asignadoAAbogado;
    }

    public String getNumeroExpedienteLegado() {
        return numeroExpedienteLegado;
    }

    public void setNumeroExpedienteLegado(String numeroExpedienteLegado) {
        this.numeroExpedienteLegado = numeroExpedienteLegado;
    }

    public String getNumeroExpedienteDigital() {
        return numeroExpedienteDigital;
    }

    public void setNumeroExpedienteDigital(String numeroExpedienteDigital) {
        this.numeroExpedienteDigital = numeroExpedienteDigital;
    }

    public LocalDate getFechaEmisionAnalisis() {
        return fechaEmisionAnalisis;
    }

    public void setFechaEmisionAnalisis(LocalDate fechaEmisionAnalisis) {
        this.fechaEmisionAnalisis = fechaEmisionAnalisis;
    }

    public String getResultadoAnalisisTexto() {
        return resultadoAnalisisTexto;
    }

    public void setResultadoAnalisisTexto(String resultadoAnalisisTexto) {
        this.resultadoAnalisisTexto = resultadoAnalisisTexto;
    }

    public LocalDate getFechaEjecucion() {
        return fechaEjecucion;
    }

    public void setFechaEjecucion(LocalDate fechaEjecucion) {
        this.fechaEjecucion = fechaEjecucion;
    }

    public String getEstadoEjecucionTexto() {
        return estadoEjecucionTexto;
    }

    public void setEstadoEjecucionTexto(String estadoEjecucionTexto) {
        this.estadoEjecucionTexto = estadoEjecucionTexto;
    }

    public String getNumeroResolucion() {
        return numeroResolucion;
    }

    public void setNumeroResolucion(String numeroResolucion) {
        this.numeroResolucion = numeroResolucion;
    }

    public String getResolucionLimpia() {
        return resolucionLimpia;
    }

    public void setResolucionLimpia(String resolucionLimpia) {
        this.resolucionLimpia = resolucionLimpia;
    }

    public String getProcedimientoEjecuciones() {
        return procedimientoEjecuciones;
    }

    public void setProcedimientoEjecuciones(String procedimientoEjecuciones) {
        this.procedimientoEjecuciones = procedimientoEjecuciones;
    }

    public String getAsignadoNotificacion() {
        return asignadoNotificacion;
    }

    public void setAsignadoNotificacion(String asignadoNotificacion) {
        this.asignadoNotificacion = asignadoNotificacion;
    }

    public String getMedioNotificacion() {
        return medioNotificacion;
    }

    public void setMedioNotificacion(String medioNotificacion) {
        this.medioNotificacion = medioNotificacion;
    }

    public LocalDate getFechaEmision1Carta() {
        return fechaEmision1Carta;
    }

    public void setFechaEmision1Carta(LocalDate fechaEmision1Carta) {
        this.fechaEmision1Carta = fechaEmision1Carta;
    }

    public String getNumero1Carta() {
        return numero1Carta;
    }

    public void setNumero1Carta(String numero1Carta) {
        this.numero1Carta = numero1Carta;
    }

    public LocalDate getFechaElaboracion2Carta() {
        return fechaElaboracion2Carta;
    }

    public void setFechaElaboracion2Carta(LocalDate fechaElaboracion2Carta) {
        this.fechaElaboracion2Carta = fechaElaboracion2Carta;
    }

    public String getNumero2Carta() {
        return numero2Carta;
    }

    public void setNumero2Carta(String numero2Carta) {
        this.numero2Carta = numero2Carta;
    }
}
