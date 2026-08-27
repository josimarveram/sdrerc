package com.sdrerc.v3.domain;

import java.time.LocalDate;

/**
 * Port ACOTADO de com.sdrerc.domain.dto.sdrercapp.EjecucionExpedienteDTO (V2, ~50 campos) — fila de
 * la Bandeja Ejecución. Mismo criterio de recorte que Análisis/Verificación: sin los ~15 campos de
 * contacto del solicitante (el "Panel de datos" reutiliza el endpoint genérico de Registro).
 *
 * <p><b>Campos de {@code expediente_resolucion} descartados por completo</b> (mismo criterio que
 * FIRMA_EMISION en Verificación): verificado con {@code SELECT COUNT(*) FROM expediente_resolucion
 * WHERE activo=1} (20/08/2026) — 0 filas reales, pese a que SÍ existen 3 expedientes reales en
 * etapa EJECUCION. La validación real de "¿tiene resolución lista?" en V2
 * ({@code tieneDocumentoResolucionConNumero}) nunca depende de esa tabla vacía — cae siempre al
 * fallback: existe un {@code EXPEDIENTE_DOCUMENTO_ANALIZADO} nivel 0 activo cuyo
 * {@code tipo_documento_adjunto.codigo} contiene "RESOLUCION" y tiene {@code numero_documento}. V3
 * usa ÚNICAMENTE ese camino real (columna {@code documentoResolucionConNumero}, más
 * {@code tipoDocumentoResolucion} para la columna "Tipo documento" de la grilla, resuelta con el
 * mismo criterio) — sin campos idResolucion/tipoResolucion/numeroResolucion/fechaResolucion/
 * fechaFirmaResolucion ligados a la tabla vacía.</p>
 */
public class EjecucionExpedienteDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroExpedienteSgd;
    private final String procedimiento;
    private final String tipoDocumentoResolucion;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final String responsable;
    private final Long idResponsable;
    private final Long idEquipoResponsable;
    private final LocalDate fechaRecepcion;
    private final LocalDate fechaVencimiento;
    private final Long diasRestantes;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final String resultadoAnalisis;
    private final String responsableAnalisis;
    private final int totalDocumentos;
    private final int totalRelacionados;
    private final boolean documentoResolucionConNumero;
    private final boolean grupoFamiliar;

    public EjecucionExpedienteDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroExpedienteSgd,
            String procedimiento,
            String tipoDocumentoResolucion,
            String tipoActa,
            String numeroActa,
            String titular,
            String responsable,
            Long idResponsable,
            Long idEquipoResponsable,
            LocalDate fechaRecepcion,
            LocalDate fechaVencimiento,
            Long diasRestantes,
            String etapaCodigo,
            String estadoCodigo,
            String resultadoAnalisis,
            String responsableAnalisis,
            int totalDocumentos,
            int totalRelacionados,
            boolean documentoResolucionConNumero,
            boolean grupoFamiliar) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroExpedienteSgd = safe(numeroExpedienteSgd);
        this.procedimiento = safe(procedimiento);
        this.tipoDocumentoResolucion = hasText(tipoDocumentoResolucion) ? tipoDocumentoResolucion.trim() : "Resolución";
        this.tipoActa = safe(tipoActa);
        this.numeroActa = safe(numeroActa);
        this.titular = safe(titular);
        this.responsable = safe(responsable);
        this.idResponsable = idResponsable;
        this.idEquipoResponsable = idEquipoResponsable;
        this.fechaRecepcion = fechaRecepcion;
        this.fechaVencimiento = fechaVencimiento;
        this.diasRestantes = diasRestantes;
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.resultadoAnalisis = safe(resultadoAnalisis);
        this.responsableAnalisis = safe(responsableAnalisis);
        this.totalDocumentos = totalDocumentos;
        this.totalRelacionados = totalRelacionados;
        this.documentoResolucionConNumero = documentoResolucionConNumero;
        this.grupoFamiliar = grupoFamiliar;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public String getNumeroExpedienteSgd() {
        return numeroExpedienteSgd;
    }

    public String getProcedimiento() {
        return procedimiento;
    }

    public String getTipoDocumentoResolucion() {
        return tipoDocumentoResolucion;
    }

    public String getTipoActa() {
        return tipoActa;
    }

    public String getNumeroActa() {
        return numeroActa;
    }

    public String getTitular() {
        return titular;
    }

    public String getResponsable() {
        return responsable;
    }

    public Long getIdResponsable() {
        return idResponsable;
    }

    public Long getIdEquipoResponsable() {
        return idEquipoResponsable;
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public Long getDiasRestantes() {
        return diasRestantes;
    }

    public String getEtapaCodigo() {
        return etapaCodigo;
    }

    public String getEstadoCodigo() {
        return estadoCodigo;
    }

    public String getResultadoAnalisis() {
        return resultadoAnalisis;
    }

    public String getResponsableAnalisis() {
        return responsableAnalisis;
    }

    public int getTotalDocumentos() {
        return totalDocumentos;
    }

    public int getTotalRelacionados() {
        return totalRelacionados;
    }

    public boolean isDocumentoResolucionConNumero() {
        return documentoResolucionConNumero;
    }

    /**
     * Flag {@code EXPEDIENTE_SOLICITUD.grupo_familiar} (grupo familiar CONFIRMADO, no la alerta
     * "Posible Grupo Familiar") — mismo campo ya expuesto en Análisis/Verificación, agregado acá
     * 21/08/2026 (pedido explícito del usuario) para gatear la subpestaña "Grupo Familiar" de solo
     * lectura en el workspace de Ejecución.
     */
    public boolean isGrupoFamiliar() {
        return grupoFamiliar;
    }

    public boolean isEnEjecucion() {
        return "EJECUCION".equalsIgnoreCase(etapaCodigo) && "EN_EJECUCION".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isEjecutado() {
        return "EJECUCION".equalsIgnoreCase(etapaCodigo) && "EJECUTADO".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isCorregible() {
        return "EJECUCION".equalsIgnoreCase(etapaCodigo)
                && ("DOCUMENTO_INCONSISTENTE".equalsIgnoreCase(estadoCodigo) || "REQUIERE_CORRECCION".equalsIgnoreCase(estadoCodigo));
    }

    /** KPI "Pendientes" (mismo criterio que V2: EN_EJECUCION o INDAGATORIO). */
    public boolean isPendiente() {
        return "EN_EJECUCION".equalsIgnoreCase(estadoCodigo) || "INDAGATORIO".equalsIgnoreCase(estadoCodigo);
    }

    /**
     * KPI "Listos para notificación" de V2 = {@code isEjecutado() && hasAccion(DERIVACION_A_NOTIFICACION)}.
     * La transición DERIVACION_A_NOTIFICACION está SIEMPRE activa desde EJECUTADO (verificado en
     * Oracle, única fila, sin condición adicional) — bajo el modelo simplificado de V3 (sin el
     * mecanismo "acciones_permitidas" de una vista, ver Javadoc del DAO) esto es equivalente a
     * {@code isEjecutado()}. V2 ya mostraba estos 2 KPIs con el mismo conteo en la práctica; V3
     * conserva ambos rótulos por fidelidad 1:1 con V2, documentando la redundancia heredada.
     */
    public boolean isListoParaNotificacion() {
        return isEjecutado();
    }

    /** "Por vencer / vencidos" (mismo umbral ≤3 días que Análisis/Verificación). */
    public boolean isPorVencerOVencido() {
        return diasRestantes != null && diasRestantes <= 3;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
