package com.sdrerc.domain.dto.sdrercapp;

import com.sdrerc.domain.rules.ProcedimientoRegistralRules;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

public class VerificacionExpedienteDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroExpedienteSgd;
    private final String numeroTramiteDocumentario;
    private final String procedimiento;
    private final String tipoDocumento;
    private final String numeroDocumentoTitular;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final LocalDate fechaRecepcion;
    private final Long diasRestantes;
    private final LocalDateTime fechaEnvioVerificacion;
    private final LocalDateTime fechaUltimoMovimiento;
    private final String responsable;
    private final String equipo;
    private final String responsableAnalisis;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final boolean tieneObservacionPendiente;
    private final int totalRelacionados;
    private final int totalDocumentosAnalizados;
    private final String ultimoResultadoAnalisis;
    private final String fundamentoAnalisis;
    private final String ultimaObservacionVerificacion;
    private final boolean requierePublicacion;
    private final LocalDate fechaPublicacion;
    private final String tipoDocumentoEmitido;
    private final String numeroDocumentoEmitido;
    private final LocalDate fechaDocumentoEmitido;
    private final LocalDateTime fechaFirmaDocumento;
    private final boolean tieneCartaEdicto;
    private final boolean puedeDerivarNotificacion;

    public VerificacionExpedienteDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroExpedienteSgd,
            String numeroTramiteDocumentario,
            String procedimiento,
            String tipoDocumento,
            String numeroDocumentoTitular,
            String tipoActa,
            String numeroActa,
            String titular,
            LocalDate fechaRecepcion,
            Long diasRestantes,
            LocalDateTime fechaEnvioVerificacion,
            LocalDateTime fechaUltimoMovimiento,
            String responsable,
            String equipo,
            String responsableAnalisis,
            String etapaCodigo,
            String estadoCodigo,
            boolean tieneObservacionPendiente,
            int totalRelacionados,
            int totalDocumentosAnalizados,
            String ultimoResultadoAnalisis,
            String fundamentoAnalisis,
            String ultimaObservacionVerificacion,
            boolean requierePublicacion,
            LocalDate fechaPublicacion,
            String tipoDocumentoEmitido,
            String numeroDocumentoEmitido,
            LocalDate fechaDocumentoEmitido,
            LocalDateTime fechaFirmaDocumento,
            boolean tieneCartaEdicto,
            boolean puedeDerivarNotificacion) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroExpedienteSgd = safe(numeroExpedienteSgd);
        this.numeroTramiteDocumentario = safe(numeroTramiteDocumentario);
        this.procedimiento = safeProcedimiento(procedimiento);
        this.tipoDocumento = safe(tipoDocumento);
        this.numeroDocumentoTitular = safe(numeroDocumentoTitular);
        this.tipoActa = safe(tipoActa);
        this.numeroActa = safe(numeroActa);
        this.titular = safe(titular);
        this.fechaRecepcion = fechaRecepcion;
        this.diasRestantes = diasRestantes;
        this.fechaEnvioVerificacion = fechaEnvioVerificacion;
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
        this.responsable = safe(responsable);
        this.equipo = safe(equipo);
        this.responsableAnalisis = safe(responsableAnalisis);
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.tieneObservacionPendiente = tieneObservacionPendiente;
        this.totalRelacionados = totalRelacionados;
        this.totalDocumentosAnalizados = totalDocumentosAnalizados;
        this.ultimoResultadoAnalisis = safe(ultimoResultadoAnalisis);
        this.fundamentoAnalisis = safe(fundamentoAnalisis);
        this.ultimaObservacionVerificacion = safe(ultimaObservacionVerificacion);
        this.requierePublicacion = requierePublicacion;
        this.fechaPublicacion = fechaPublicacion;
        this.tipoDocumentoEmitido = safe(tipoDocumentoEmitido);
        this.numeroDocumentoEmitido = safe(numeroDocumentoEmitido);
        this.fechaDocumentoEmitido = fechaDocumentoEmitido;
        this.fechaFirmaDocumento = fechaFirmaDocumento;
        this.tieneCartaEdicto = tieneCartaEdicto;
        this.puedeDerivarNotificacion = puedeDerivarNotificacion;
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

    public String getNumeroTramiteDocumentario() {
        return numeroTramiteDocumentario;
    }

    public String getProcedimiento() {
        return procedimiento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumentoTitular() {
        return numeroDocumentoTitular;
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

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public LocalDateTime getFechaEnvioVerificacion() {
        return fechaEnvioVerificacion;
    }

    public LocalDateTime getFechaUltimoMovimiento() {
        return fechaUltimoMovimiento;
    }

    public String getResponsable() {
        return responsable;
    }

    public String getEquipo() {
        return equipo;
    }

    public String getResponsableAnalisis() {
        return responsableAnalisis;
    }

    public String getEtapaCodigo() {
        return etapaCodigo;
    }

    public String getEstadoCodigo() {
        return estadoCodigo;
    }

    public boolean isTieneObservacionPendiente() {
        return tieneObservacionPendiente;
    }

    public int getTotalRelacionados() {
        return totalRelacionados;
    }

    public int getTotalDocumentosAnalizados() {
        return totalDocumentosAnalizados;
    }

    public String getUltimoResultadoAnalisis() {
        return ultimoResultadoAnalisis;
    }

    public String getFundamentoAnalisis() {
        return fundamentoAnalisis;
    }

    public String getUltimaObservacionVerificacion() {
        return ultimaObservacionVerificacion;
    }

    public boolean isRequierePublicacion() {
        return requierePublicacion;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    public String getTipoDocumentoEmitido() {
        return tipoDocumentoEmitido;
    }

    public String getNumeroDocumentoEmitido() {
        return numeroDocumentoEmitido;
    }

    public LocalDate getFechaDocumentoEmitido() {
        return fechaDocumentoEmitido;
    }

    public LocalDateTime getFechaFirmaDocumento() {
        return fechaFirmaDocumento;
    }

    public boolean isTieneCartaEdicto() {
        return tieneCartaEdicto;
    }

    public boolean isPuedeDerivarNotificacion() {
        return puedeDerivarNotificacion;
    }

    public Long getDiasEnEtapa() {
        return diasRestantes;
    }

    public boolean isRegistrableVerificacion() {
        return "VERIFICACION".equalsIgnoreCase(etapaCodigo) && "EN_VERIFICACION".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isEnviableFirma() {
        return "VERIFICACION".equalsIgnoreCase(etapaCodigo) && "VERIFICADO".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isDevolvibleAnalisis() {
        return "VERIFICACION".equalsIgnoreCase(etapaCodigo)
                && ("REQUIERE_CORRECCION".equalsIgnoreCase(estadoCodigo)
                || "DOCUMENTO_INCONSISTENTE".equalsIgnoreCase(estadoCodigo));
    }

    public boolean isDocumentoEmitido() {
        return "FIRMA_EMISION".equalsIgnoreCase(etapaCodigo)
                && ("EMITIDO".equalsIgnoreCase(estadoCodigo)
                || "RESOLUCION_NUMERADA".equalsIgnoreCase(estadoCodigo));
    }

    public boolean isResultadoResolutivo() {
        String resultado = normalize(ultimoResultadoAnalisis);
        return "PROCEDENTE".equals(resultado)
                || "PROCEDENTE EN PARTE".equals(resultado)
                || "IMPROCEDENTE".equals(resultado);
    }

    public boolean isEnviableNotificacion() {
        return "FIRMA_EMISION".equalsIgnoreCase(etapaCodigo)
                && !isResultadoResolutivo()
                && puedeDerivarNotificacion;
    }

    public String getDestinoSiguiente() {
        if (isResultadoResolutivo()) {
            return "Ejecución";
        }
        if (puedeDerivarNotificacion) {
            return "Notificación";
        }
        return "Notificación (transición no configurada)";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String safeProcedimiento(String value) {
        String canonico = ProcedimientoRegistralRules.nombreCanonico(value);
        return canonico == null ? "" : canonico;
    }

    private static String normalize(String value) {
        String normalized = Normalizer.normalize(safe(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }
}
