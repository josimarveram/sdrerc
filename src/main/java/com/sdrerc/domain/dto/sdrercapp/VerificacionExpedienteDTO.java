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
    private final String numeroDocumento;
    private final String tipoDocumento;
    private final String numeroDocumentoTitular;
    private final String tipoDocumentoTitular;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final String solicitante;
    private final String tipoDocumentoSolicitante;
    private final String numeroDocumentoSolicitante;
    private final String correoSolicitante;
    private final String telefonoSolicitante;
    private final String departamentoSolicitante;
    private final String provinciaSolicitante;
    private final String distritoSolicitante;
    private final String direccionSolicitante;
    private final String canalIngreso;
    private final String observacionSolicitud;
    private final boolean grupoFamiliar;
    private final String criterioGrupoFamiliar;
    private final String observacionGrupoFamiliar;
    private final LocalDate fechaRecepcion;
    private final LocalDate fechaVencimiento;
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
    private final Long idDocumentoPendiente;
    private final String tipoDocumentoPendiente;

    public VerificacionExpedienteDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroExpedienteSgd,
            String numeroTramiteDocumentario,
            String procedimiento,
            String numeroDocumento,
            String tipoDocumento,
            String numeroDocumentoTitular,
            String tipoDocumentoTitular,
            String tipoActa,
            String numeroActa,
            String titular,
            String solicitante,
            String tipoDocumentoSolicitante,
            String numeroDocumentoSolicitante,
            String correoSolicitante,
            String telefonoSolicitante,
            String departamentoSolicitante,
            String provinciaSolicitante,
            String distritoSolicitante,
            String direccionSolicitante,
            String canalIngreso,
            String observacionSolicitud,
            boolean grupoFamiliar,
            String criterioGrupoFamiliar,
            String observacionGrupoFamiliar,
            LocalDate fechaRecepcion,
            LocalDate fechaVencimiento,
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
            boolean puedeDerivarNotificacion,
            Long idDocumentoPendiente,
            String tipoDocumentoPendiente) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroExpedienteSgd = safe(numeroExpedienteSgd);
        this.numeroTramiteDocumentario = safe(numeroTramiteDocumentario);
        this.procedimiento = safeProcedimiento(procedimiento);
        this.numeroDocumento = safe(numeroDocumento);
        this.tipoDocumento = safe(tipoDocumento);
        this.numeroDocumentoTitular = safe(numeroDocumentoTitular);
        this.tipoDocumentoTitular = safe(tipoDocumentoTitular);
        this.tipoActa = safe(tipoActa);
        this.numeroActa = safe(numeroActa);
        this.titular = safe(titular);
        this.solicitante = safe(solicitante);
        this.tipoDocumentoSolicitante = safe(tipoDocumentoSolicitante);
        this.numeroDocumentoSolicitante = safe(numeroDocumentoSolicitante);
        this.correoSolicitante = safe(correoSolicitante);
        this.telefonoSolicitante = safe(telefonoSolicitante);
        this.departamentoSolicitante = safe(departamentoSolicitante);
        this.provinciaSolicitante = safe(provinciaSolicitante);
        this.distritoSolicitante = safe(distritoSolicitante);
        this.direccionSolicitante = safe(direccionSolicitante);
        this.canalIngreso = safe(canalIngreso);
        this.observacionSolicitud = safe(observacionSolicitud);
        this.grupoFamiliar = grupoFamiliar;
        this.criterioGrupoFamiliar = safe(criterioGrupoFamiliar);
        this.observacionGrupoFamiliar = safe(observacionGrupoFamiliar);
        this.fechaRecepcion = fechaRecepcion;
        this.fechaVencimiento = fechaVencimiento;
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
        this.idDocumentoPendiente = idDocumentoPendiente;
        this.tipoDocumentoPendiente = safe(tipoDocumentoPendiente);
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

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getNumeroDocumentoTitular() {
        return numeroDocumentoTitular;
    }

    public String getTipoDocumentoTitular() {
        return tipoDocumentoTitular;
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

    public String getSolicitante() {
        return solicitante;
    }

    public String getTipoDocumentoSolicitante() {
        return tipoDocumentoSolicitante;
    }

    public String getNumeroDocumentoSolicitante() {
        return numeroDocumentoSolicitante;
    }

    public String getCorreoSolicitante() {
        return correoSolicitante;
    }

    public String getTelefonoSolicitante() {
        return telefonoSolicitante;
    }

    public String getDepartamentoSolicitante() {
        return departamentoSolicitante;
    }

    public String getProvinciaSolicitante() {
        return provinciaSolicitante;
    }

    public String getDistritoSolicitante() {
        return distritoSolicitante;
    }

    public String getDireccionSolicitante() {
        return direccionSolicitante;
    }

    public String getCanalIngreso() {
        return canalIngreso;
    }

    public String getObservacionSolicitud() {
        return observacionSolicitud;
    }

    public boolean isGrupoFamiliar() {
        return grupoFamiliar;
    }

    public String getCriterioGrupoFamiliar() {
        return criterioGrupoFamiliar;
    }

    public String getObservacionGrupoFamiliar() {
        return observacionGrupoFamiliar;
    }

    public String getGrupoFamiliarEstado() {
        return grupoFamiliar ? "Si" : "No";
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
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

    public Long getIdDocumentoPendiente() {
        return idDocumentoPendiente;
    }

    public String getTipoDocumentoPendiente() {
        return tipoDocumentoPendiente;
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
