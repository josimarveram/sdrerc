package com.sdrerc.v3.domain;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Port ACOTADO de com.sdrerc.domain.dto.sdrercapp.VerificacionExpedienteDTO (V2, 63 campos) — fila
 * de la Bandeja Verificación. Mismo criterio de recorte que {@code AnalisisExpedienteDTO}: sin los
 * ~15 campos de contacto del solicitante (el "Panel de datos" reutiliza el endpoint genérico de
 * Registro, no un SELECT propio).
 *
 * <p>Se omiten TODOS los campos relacionados a la etapa {@code FIRMA_EMISION}
 * (tipoDocumentoEmitido/numeroDocumentoEmitido/fechaDocumentoEmitido/fechaFirmaDocumento,
 * tieneCartaEdicto, puedeDerivarNotificacion, y los métodos {@code isEnviableFirma()}/
 * {@code isDocumentoEmitido()}/{@code isEnviableNotificacion()}/{@code getDestinoSiguiente()}) —
 * verificado con {@code SELECT COUNT(*) FROM expediente WHERE activo=1 AND etapa=FIRMA_EMISION} (20/08/2026):
 * 0 filas reales. Ningún camino vivo de "Registrar Verificación" produce ese estado (era territorio
 * exclusivo del código huérfano {@code FirmaEmisionExpedienteService}, nunca conectado a un botón
 * visible — ver Javadoc de {@code VerificacionExpedienteDAO}). El KPI "Emitidos" de V2 tampoco se
 * porta, por el mismo motivo.</p>
 */
public class VerificacionExpedienteDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroExpedienteSgd;
    private final String procedimiento;
    private final String tipoDocumentoPendiente;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final String responsable;
    private final Long idResponsable;
    private final Long idEquipoResponsable;
    private final LocalDate fechaRecepcion;
    private final LocalDate fechaVencimiento;
    private final Long diasRestantes;
    private final LocalDateTime fechaRegistro;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final boolean grupoFamiliar;
    private final int asociadosConfirmados;
    private final int documentosAnalizados;
    private final String ultimoResultadoAnalisis;
    private final String responsableAnalisis;
    private final String equipoAnalisis;

    public VerificacionExpedienteDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroExpedienteSgd,
            String procedimiento,
            String tipoDocumentoPendiente,
            String tipoActa,
            String numeroActa,
            String titular,
            String responsable,
            Long idResponsable,
            Long idEquipoResponsable,
            LocalDate fechaRecepcion,
            LocalDate fechaVencimiento,
            Long diasRestantes,
            LocalDateTime fechaRegistro,
            String etapaCodigo,
            String estadoCodigo,
            boolean grupoFamiliar,
            int asociadosConfirmados,
            int documentosAnalizados,
            String ultimoResultadoAnalisis,
            String responsableAnalisis,
            String equipoAnalisis) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroExpedienteSgd = safe(numeroExpedienteSgd);
        this.procedimiento = safe(procedimiento);
        this.tipoDocumentoPendiente = safe(tipoDocumentoPendiente);
        this.tipoActa = safe(tipoActa);
        this.numeroActa = safe(numeroActa);
        this.titular = safe(titular);
        this.responsable = safe(responsable);
        this.idResponsable = idResponsable;
        this.idEquipoResponsable = idEquipoResponsable;
        this.fechaRecepcion = fechaRecepcion;
        this.fechaVencimiento = fechaVencimiento;
        this.diasRestantes = diasRestantes;
        this.fechaRegistro = fechaRegistro;
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.grupoFamiliar = grupoFamiliar;
        this.asociadosConfirmados = asociadosConfirmados;
        this.documentosAnalizados = documentosAnalizados;
        this.ultimoResultadoAnalisis = safe(ultimoResultadoAnalisis);
        this.responsableAnalisis = safe(responsableAnalisis);
        this.equipoAnalisis = safe(equipoAnalisis);
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

    public String getTipoDocumentoPendiente() {
        return tipoDocumentoPendiente;
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public String getEtapaCodigo() {
        return etapaCodigo;
    }

    public String getEstadoCodigo() {
        return estadoCodigo;
    }

    public boolean isGrupoFamiliar() {
        return grupoFamiliar;
    }

    public int getAsociadosConfirmados() {
        return asociadosConfirmados;
    }

    public int getDocumentosAnalizados() {
        return documentosAnalizados;
    }

    public String getUltimoResultadoAnalisis() {
        return ultimoResultadoAnalisis;
    }

    public String getResponsableAnalisis() {
        return responsableAnalisis;
    }

    public String getEquipoAnalisis() {
        return equipoAnalisis;
    }

    public boolean isRegistrableVerificacion() {
        return "VERIFICACION".equalsIgnoreCase(etapaCodigo) && "EN_VERIFICACION".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isObservado() {
        return "REQUIERE_CORRECCION".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isInconsistente() {
        return "DOCUMENTO_INCONSISTENTE".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isAprobado() {
        return "VERIFICADO".equalsIgnoreCase(estadoCodigo);
    }

    /** Mismo criterio que V2 (comparación normalizada sin tildes) — decide la rama (b) de "Registrar Verificación". */
    public boolean isResultadoResolutivo() {
        String resultado = normalize(ultimoResultadoAnalisis);
        return "PROCEDENTE".equals(resultado) || "PROCEDENTE EN PARTE".equals(resultado) || "IMPROCEDENTE".equals(resultado);
    }

    /** "Por vencer / vencidos" (mismo umbral ≤3 días que Análisis). */
    public boolean isPorVencerOVencido() {
        return diasRestantes != null && diasRestantes <= 3;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String normalize(String value) {
        String normalized = Normalizer.normalize(safe(value), Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return normalized.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }
}
