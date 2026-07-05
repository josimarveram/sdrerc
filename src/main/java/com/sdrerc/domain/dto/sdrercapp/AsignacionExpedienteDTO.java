package com.sdrerc.domain.dto.sdrercapp;

import com.sdrerc.domain.rules.ProcedimientoRegistralRules;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AsignacionExpedienteDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroExpedienteSgd;
    private final String numeroHojaEnvioAsignacion;
    private final String numeroTramiteDocumentario;
    private final String canalIngreso;
    private final String tipoSolicitud;
    private final String numeroDocumento;
    private final String tipoDocumento;
    private final String procedimiento;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final String solicitante;
    private final String tipoDocumentoSolicitante;
    private final String numeroDocumentoSolicitante;
    private final String correoSolicitante;
    private final String telefonoSolicitante;
    private final String direccionSolicitante;
    private final String departamentoSolicitante;
    private final String provinciaSolicitante;
    private final String distritoSolicitante;
    private final String equipoAsignado;
    private final Long idEquipoResponsable;
    private final String abogadoAsignado;
    private final Long idAbogadoResponsable;
    private final String tipoDocumentoTitular;
    private final String numeroDocumentoTitular;
    private final LocalDate fechaRecepcion;
    private final Long diasRestantes;
    private final LocalDateTime fechaRegistro;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final boolean asignacionActiva;
    private final int posiblesRelacionados;
    private final int asociadosConfirmados;
    private final boolean potencialDuplicado;
    private final String observacionSolicitud;
    private final boolean grupoFamiliar;
    private final String criterioGrupoFamiliar;
    private final String observacionGrupoFamiliar;

    public AsignacionExpedienteDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroExpedienteSgd,
            String numeroHojaEnvioAsignacion,
            String numeroTramiteDocumentario,
            String canalIngreso,
            String tipoSolicitud,
            String numeroDocumento,
            String tipoDocumento,
            String procedimiento,
            String tipoActa,
            String numeroActa,
            String titular,
            String solicitante,
            String tipoDocumentoSolicitante,
            String numeroDocumentoSolicitante,
            String correoSolicitante,
            String telefonoSolicitante,
            String direccionSolicitante,
            String departamentoSolicitante,
            String provinciaSolicitante,
            String distritoSolicitante,
            String equipoAsignado,
            Long idEquipoResponsable,
            String abogadoAsignado,
            Long idAbogadoResponsable,
            String tipoDocumentoTitular,
            String numeroDocumentoTitular,
            LocalDate fechaRecepcion,
            Long diasRestantes,
            LocalDateTime fechaRegistro,
            String etapaCodigo,
            String estadoCodigo,
            boolean asignacionActiva,
            int posiblesRelacionados,
            int asociadosConfirmados,
            boolean potencialDuplicado,
            String observacionSolicitud,
            boolean grupoFamiliar,
            String criterioGrupoFamiliar,
            String observacionGrupoFamiliar) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroExpedienteSgd = safe(numeroExpedienteSgd);
        this.numeroHojaEnvioAsignacion = safe(numeroHojaEnvioAsignacion);
        this.numeroTramiteDocumentario = safe(numeroTramiteDocumentario);
        this.canalIngreso = safe(canalIngreso);
        this.tipoSolicitud = safe(tipoSolicitud);
        this.numeroDocumento = safe(numeroDocumento);
        this.tipoDocumento = safe(tipoDocumento);
        this.procedimiento = safeProcedimiento(procedimiento);
        this.tipoActa = safe(tipoActa);
        this.numeroActa = safe(numeroActa);
        this.titular = safe(titular);
        this.solicitante = safe(solicitante);
        this.tipoDocumentoSolicitante = safe(tipoDocumentoSolicitante);
        this.numeroDocumentoSolicitante = safe(numeroDocumentoSolicitante);
        this.correoSolicitante = safe(correoSolicitante);
        this.telefonoSolicitante = safe(telefonoSolicitante);
        this.direccionSolicitante = safe(direccionSolicitante);
        this.departamentoSolicitante = safe(departamentoSolicitante);
        this.provinciaSolicitante = safe(provinciaSolicitante);
        this.distritoSolicitante = safe(distritoSolicitante);
        this.equipoAsignado = safe(equipoAsignado);
        this.idEquipoResponsable = idEquipoResponsable;
        this.abogadoAsignado = safe(abogadoAsignado);
        this.idAbogadoResponsable = idAbogadoResponsable;
        this.tipoDocumentoTitular = safe(tipoDocumentoTitular);
        this.numeroDocumentoTitular = safe(numeroDocumentoTitular);
        this.fechaRecepcion = fechaRecepcion;
        this.diasRestantes = diasRestantes;
        this.fechaRegistro = fechaRegistro;
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.asignacionActiva = asignacionActiva;
        this.posiblesRelacionados = posiblesRelacionados;
        this.asociadosConfirmados = asociadosConfirmados;
        this.potencialDuplicado = potencialDuplicado;
        this.observacionSolicitud = safe(observacionSolicitud);
        this.grupoFamiliar = grupoFamiliar;
        this.criterioGrupoFamiliar = safe(criterioGrupoFamiliar);
        this.observacionGrupoFamiliar = safe(observacionGrupoFamiliar);
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

    public String getNumeroHojaEnvioAsignacion() {
        return numeroHojaEnvioAsignacion;
    }

    public String getNumeroTramiteDocumentario() {
        return numeroTramiteDocumentario;
    }

    public String getCanalIngreso() {
        return canalIngreso;
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getProcedimiento() {
        return procedimiento;
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

    public String getEquipoAsignado() {
        return equipoAsignado;
    }

    public Long getIdEquipoResponsable() {
        return idEquipoResponsable;
    }

    public String getAbogadoAsignado() {
        return abogadoAsignado;
    }

    public Long getIdAbogadoResponsable() {
        return idAbogadoResponsable;
    }

    public String getNumeroDocumentoTitular() {
        return numeroDocumentoTitular;
    }

    public String getTipoDocumentoTitular() {
        return tipoDocumentoTitular;
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
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

    public boolean isAsignacionActiva() {
        return asignacionActiva;
    }

    public int getPosiblesRelacionados() {
        return posiblesRelacionados;
    }

    public int getAsociadosConfirmados() {
        return asociadosConfirmados;
    }

    public boolean isPotencialDuplicado() {
        return potencialDuplicado;
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

    public boolean isPosibleGrupoFamiliar() {
        return grupoFamiliar
                || !criterioGrupoFamiliar.isEmpty()
                || !observacionGrupoFamiliar.isEmpty()
                || contienePosibleGrupoFamiliar(observacionSolicitud);
    }

    public String getGrupoFamiliarEstado() {
        if (grupoFamiliar) {
            return "Sí";
        }
        return isPosibleGrupoFamiliar() ? "Posible" : "No";
    }

    public boolean tieneObservacionRegistro() {
        String observacion = observacionSolicitud.toUpperCase();
        return observacion.contains("ADVERTENCIAS DE VALIDACIÓN")
                || observacion.contains("MOTIVO DUPLICADO");
    }

    public String getAlertaIngreso() {
        if (potencialDuplicado) {
            return "Potencial duplicado";
        }
        if (isPosibleGrupoFamiliar()) {
            return "Posible Grupo Familiar";
        }
        if (tieneObservacionRegistro()) {
            return "Con observaciones";
        }
        return "Normal";
    }

    public Long getDiasDesdeRegistro() {
        return diasRestantes;
    }

    public boolean isAsignable() {
        return "REGISTRO".equalsIgnoreCase(etapaCodigo)
                && "REGISTRADO".equalsIgnoreCase(estadoCodigo)
                && !asignacionActiva;
    }

    public boolean tieneNumeroExpediente() {
        return !numeroExpediente.trim().isEmpty();
    }

    public boolean requiereDecisionNumeroAsignacion() {
        return !tieneNumeroExpediente()
                && isAsignable()
                && ProcedimientoRegistralRules.requiereDecisionAsignacionParaNumero(procedimiento);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String safeProcedimiento(String value) {
        String canonico = ProcedimientoRegistralRules.nombreCanonico(value);
        return canonico == null ? "" : canonico;
    }

    private static boolean contienePosibleGrupoFamiliar(String value) {
        if (value == null) {
            return false;
        }
        return value.trim().toLowerCase(java.util.Locale.ROOT).contains("posible grupo familiar");
    }
}
