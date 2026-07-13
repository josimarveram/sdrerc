package com.sdrerc.domain.dto.sdrercapp;

import com.sdrerc.domain.rules.ProcedimientoRegistralRules;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class EjecucionExpedienteDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroExpedienteSgd;
    private final String numeroTramiteDocumentario;
    private final String procedimiento;
    private final String tipoDocumento;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final LocalDate fechaRecepcion;
    private final Long diasRestantes;
    private final LocalDateTime fechaIngresoEjecucion;
    private final LocalDateTime fechaUltimoMovimiento;
    private final String responsable;
    private final String equipo;
    private final String responsableAnalisis;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final String resultadoAnalisis;
    private final String fundamentoAnalisis;
    private final String resultadoVerificacion;
    private final String ultimaObservacion;
    private final int totalDocumentos;
    private final int totalRelacionados;
    private final Long idResolucion;
    private final String tipoResolucion;
    private final String numeroResolucion;
    private final LocalDate fechaResolucion;
    private final LocalDateTime fechaFirmaResolucion;
    private final boolean requierePublicacion;
    private final LocalDate fechaPublicacion;
    private final String accionesPermitidas;
    private final String numeroDocumento;
    private final String tipoDocumentoTitular;
    private final String numeroDocumentoTitular;
    private final String solicitante;
    private final String tipoDocumentoSolicitante;
    private final String numeroDocumentoSolicitante;
    private final String correoSolicitante;
    private final String telefonoSolicitante;
    private final String direccionSolicitante;
    private final String departamentoSolicitante;
    private final String provinciaSolicitante;
    private final String distritoSolicitante;
    private final String canalIngreso;
    private final String observacionSolicitud;
    private final boolean grupoFamiliar;
    private final String criterioGrupoFamiliar;
    private final String observacionGrupoFamiliar;
    private final LocalDate fechaVencimiento;

    public EjecucionExpedienteDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroExpedienteSgd,
            String numeroTramiteDocumentario,
            String procedimiento,
            String tipoDocumento,
            String tipoActa,
            String numeroActa,
            String titular,
            LocalDate fechaRecepcion,
            Long diasRestantes,
            LocalDateTime fechaIngresoEjecucion,
            LocalDateTime fechaUltimoMovimiento,
            String responsable,
            String equipo,
            String responsableAnalisis,
            String etapaCodigo,
            String estadoCodigo,
            String resultadoAnalisis,
            String fundamentoAnalisis,
            String resultadoVerificacion,
            String ultimaObservacion,
            int totalDocumentos,
            int totalRelacionados,
            Long idResolucion,
            String tipoResolucion,
            String numeroResolucion,
            LocalDate fechaResolucion,
            LocalDateTime fechaFirmaResolucion,
            boolean requierePublicacion,
            LocalDate fechaPublicacion,
            String accionesPermitidas,
            String numeroDocumento,
            String tipoDocumentoTitular,
            String numeroDocumentoTitular,
            String solicitante,
            String tipoDocumentoSolicitante,
            String numeroDocumentoSolicitante,
            String correoSolicitante,
            String telefonoSolicitante,
            String direccionSolicitante,
            String departamentoSolicitante,
            String provinciaSolicitante,
            String distritoSolicitante,
            String canalIngreso,
            String observacionSolicitud,
            boolean grupoFamiliar,
            String criterioGrupoFamiliar,
            String observacionGrupoFamiliar,
            LocalDate fechaVencimiento) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroExpedienteSgd = safe(numeroExpedienteSgd);
        this.numeroTramiteDocumentario = safe(numeroTramiteDocumentario);
        this.procedimiento = safeProcedimiento(procedimiento);
        this.tipoDocumento = safe(tipoDocumento);
        this.tipoActa = safe(tipoActa);
        this.numeroActa = safe(numeroActa);
        this.titular = safe(titular);
        this.fechaRecepcion = fechaRecepcion;
        this.diasRestantes = diasRestantes;
        this.fechaIngresoEjecucion = fechaIngresoEjecucion;
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
        this.responsable = safe(responsable);
        this.equipo = safe(equipo);
        this.responsableAnalisis = safe(responsableAnalisis);
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.resultadoAnalisis = safe(resultadoAnalisis);
        this.fundamentoAnalisis = safe(fundamentoAnalisis);
        this.resultadoVerificacion = safe(resultadoVerificacion);
        this.ultimaObservacion = safe(ultimaObservacion);
        this.totalDocumentos = totalDocumentos;
        this.totalRelacionados = totalRelacionados;
        this.idResolucion = idResolucion;
        this.tipoResolucion = safe(tipoResolucion);
        this.numeroResolucion = safe(numeroResolucion);
        this.fechaResolucion = fechaResolucion;
        this.fechaFirmaResolucion = fechaFirmaResolucion;
        this.requierePublicacion = requierePublicacion;
        this.fechaPublicacion = fechaPublicacion;
        this.accionesPermitidas = safe(accionesPermitidas);
        this.numeroDocumento = safe(numeroDocumento);
        this.tipoDocumentoTitular = safe(tipoDocumentoTitular);
        this.numeroDocumentoTitular = safe(numeroDocumentoTitular);
        this.solicitante = safe(solicitante);
        this.tipoDocumentoSolicitante = safe(tipoDocumentoSolicitante);
        this.numeroDocumentoSolicitante = safe(numeroDocumentoSolicitante);
        this.correoSolicitante = safe(correoSolicitante);
        this.telefonoSolicitante = safe(telefonoSolicitante);
        this.direccionSolicitante = safe(direccionSolicitante);
        this.departamentoSolicitante = safe(departamentoSolicitante);
        this.provinciaSolicitante = safe(provinciaSolicitante);
        this.distritoSolicitante = safe(distritoSolicitante);
        this.canalIngreso = safe(canalIngreso);
        this.observacionSolicitud = safe(observacionSolicitud);
        this.grupoFamiliar = grupoFamiliar;
        this.criterioGrupoFamiliar = safe(criterioGrupoFamiliar);
        this.observacionGrupoFamiliar = safe(observacionGrupoFamiliar);
        this.fechaVencimiento = fechaVencimiento;
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

    public LocalDateTime getFechaIngresoEjecucion() {
        return fechaIngresoEjecucion;
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

    public String getResultadoAnalisis() {
        return resultadoAnalisis;
    }

    public String getFundamentoAnalisis() {
        return fundamentoAnalisis;
    }

    public String getResultadoVerificacion() {
        return resultadoVerificacion;
    }

    public String getUltimaObservacion() {
        return ultimaObservacion;
    }

    public int getTotalDocumentos() {
        return totalDocumentos;
    }

    public int getTotalRelacionados() {
        return totalRelacionados;
    }

    public Long getIdResolucion() {
        return idResolucion;
    }

    public String getTipoResolucion() {
        return tipoResolucion;
    }

    public String getNumeroResolucion() {
        return numeroResolucion;
    }

    public LocalDate getFechaResolucion() {
        return fechaResolucion;
    }

    public LocalDateTime getFechaFirmaResolucion() {
        return fechaFirmaResolucion;
    }

    public boolean isRequierePublicacion() {
        return requierePublicacion;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    public String getAccionesPermitidas() {
        return accionesPermitidas;
    }

    public Long getDiasEnEtapa() {
        return diasRestantes;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getTipoDocumentoTitular() {
        return tipoDocumentoTitular;
    }

    public String getNumeroDocumentoTitular() {
        return numeroDocumentoTitular;
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

    public String getDireccionSolicitante() {
        return direccionSolicitante;
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

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public boolean hasAccion(String codigoAccion) {
        if (codigoAccion == null || codigoAccion.trim().isEmpty() || accionesPermitidas.trim().isEmpty()) {
            return false;
        }
        Set<String> acciones = new HashSet<String>(Arrays.asList(accionesPermitidas.toUpperCase(Locale.ROOT).split(",")));
        return acciones.contains(codigoAccion.trim().toUpperCase(Locale.ROOT));
    }

    public boolean isEnEjecucion() {
        return "EJECUCION".equalsIgnoreCase(etapaCodigo) && "EN_EJECUCION".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isEjecutado() {
        return "EJECUCION".equalsIgnoreCase(etapaCodigo) && "EJECUTADO".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isCorregible() {
        return "EJECUCION".equalsIgnoreCase(etapaCodigo)
                && ("DOCUMENTO_INCONSISTENTE".equalsIgnoreCase(estadoCodigo)
                || "REQUIERE_CORRECCION".equalsIgnoreCase(estadoCodigo));
    }

    public boolean isDocumentoEmitido() {
        return hasText(numeroResolucion) || idResolucion != null;
    }

    public boolean isListoParaNotificacion() {
        return isEjecutado() && hasAccion("DERIVACION_A_NOTIFICACION");
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String safeProcedimiento(String value) {
        String canonico = ProcedimientoRegistralRules.nombreCanonico(value);
        return canonico == null ? "" : canonico;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
