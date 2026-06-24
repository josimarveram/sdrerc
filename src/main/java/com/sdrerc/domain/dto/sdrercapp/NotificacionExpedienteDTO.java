package com.sdrerc.domain.dto.sdrercapp;

import com.sdrerc.domain.rules.ProcedimientoRegistralRules;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class NotificacionExpedienteDTO {

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
    private final LocalDateTime fechaIngresoNotificacion;
    private final LocalDateTime fechaUltimoMovimiento;
    private final String responsable;
    private final String equipo;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final String resultadoAnalisis;
    private final String resultadoVerificacion;
    private final String resultadoEjecucion;
    private final String ultimaObservacion;
    private final int totalDocumentos;
    private final int totalRelacionados;
    private final Long idResolucion;
    private final String tipoResolucion;
    private final String numeroResolucion;
    private final LocalDate fechaResolucion;
    private final Long idNotificacion;
    private final String tipoNotificacionCodigo;
    private final String tipoNotificacion;
    private final String estadoNotificacionCodigo;
    private final String estadoNotificacion;
    private final Integer numeroIntento;
    private final LocalDateTime fechaEnvio;
    private final String resultadoNotificacion;
    private final boolean requierePublicacion;
    private final LocalDate fechaPublicacion;
    private final Long idCargoAcuse;
    private final String estadoCargoCodigo;
    private final String estadoCargo;
    private final LocalDateTime fechaCargo;
    private final String recibidoPor;
    private final String accionesPermitidas;

    public NotificacionExpedienteDTO(
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
            LocalDateTime fechaIngresoNotificacion,
            LocalDateTime fechaUltimoMovimiento,
            String responsable,
            String equipo,
            String etapaCodigo,
            String estadoCodigo,
            String resultadoAnalisis,
            String resultadoVerificacion,
            String resultadoEjecucion,
            String ultimaObservacion,
            int totalDocumentos,
            int totalRelacionados,
            Long idResolucion,
            String tipoResolucion,
            String numeroResolucion,
            LocalDate fechaResolucion,
            Long idNotificacion,
            String tipoNotificacionCodigo,
            String tipoNotificacion,
            String estadoNotificacionCodigo,
            String estadoNotificacion,
            Integer numeroIntento,
            LocalDateTime fechaEnvio,
            String resultadoNotificacion,
            boolean requierePublicacion,
            LocalDate fechaPublicacion,
            Long idCargoAcuse,
            String estadoCargoCodigo,
            String estadoCargo,
            LocalDateTime fechaCargo,
            String recibidoPor,
            String accionesPermitidas) {
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
        this.fechaIngresoNotificacion = fechaIngresoNotificacion;
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
        this.responsable = safe(responsable);
        this.equipo = safe(equipo);
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.resultadoAnalisis = safe(resultadoAnalisis);
        this.resultadoVerificacion = safe(resultadoVerificacion);
        this.resultadoEjecucion = safe(resultadoEjecucion);
        this.ultimaObservacion = safe(ultimaObservacion);
        this.totalDocumentos = totalDocumentos;
        this.totalRelacionados = totalRelacionados;
        this.idResolucion = idResolucion;
        this.tipoResolucion = safe(tipoResolucion);
        this.numeroResolucion = safe(numeroResolucion);
        this.fechaResolucion = fechaResolucion;
        this.idNotificacion = idNotificacion;
        this.tipoNotificacionCodigo = safe(tipoNotificacionCodigo);
        this.tipoNotificacion = safe(tipoNotificacion);
        this.estadoNotificacionCodigo = safe(estadoNotificacionCodigo);
        this.estadoNotificacion = safe(estadoNotificacion);
        this.numeroIntento = numeroIntento;
        this.fechaEnvio = fechaEnvio;
        this.resultadoNotificacion = safe(resultadoNotificacion);
        this.requierePublicacion = requierePublicacion;
        this.fechaPublicacion = fechaPublicacion;
        this.idCargoAcuse = idCargoAcuse;
        this.estadoCargoCodigo = safe(estadoCargoCodigo);
        this.estadoCargo = safe(estadoCargo);
        this.fechaCargo = fechaCargo;
        this.recibidoPor = safe(recibidoPor);
        this.accionesPermitidas = safe(accionesPermitidas);
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

    public LocalDateTime getFechaIngresoNotificacion() {
        return fechaIngresoNotificacion;
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

    public String getEtapaCodigo() {
        return etapaCodigo;
    }

    public String getEstadoCodigo() {
        return estadoCodigo;
    }

    public String getResultadoAnalisis() {
        return resultadoAnalisis;
    }

    public String getResultadoVerificacion() {
        return resultadoVerificacion;
    }

    public String getResultadoEjecucion() {
        return resultadoEjecucion;
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

    public Long getIdNotificacion() {
        return idNotificacion;
    }

    public String getTipoNotificacionCodigo() {
        return tipoNotificacionCodigo;
    }

    public String getTipoNotificacion() {
        return tipoNotificacion;
    }

    public String getEstadoNotificacionCodigo() {
        return estadoNotificacionCodigo;
    }

    public String getEstadoNotificacion() {
        return estadoNotificacion;
    }

    public Integer getNumeroIntento() {
        return numeroIntento;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public String getResultadoNotificacion() {
        return resultadoNotificacion;
    }

    public boolean isRequierePublicacion() {
        return requierePublicacion;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    public Long getIdCargoAcuse() {
        return idCargoAcuse;
    }

    public String getEstadoCargoCodigo() {
        return estadoCargoCodigo;
    }

    public String getEstadoCargo() {
        return estadoCargo;
    }

    public LocalDateTime getFechaCargo() {
        return fechaCargo;
    }

    public String getRecibidoPor() {
        return recibidoPor;
    }

    public String getAccionesPermitidas() {
        return accionesPermitidas;
    }

    public Long getDiasEnEtapa() {
        return diasRestantes;
    }

    public boolean hasAccion(String codigoAccion) {
        if (codigoAccion == null || codigoAccion.trim().isEmpty() || accionesPermitidas.trim().isEmpty()) {
            return false;
        }
        Set<String> acciones = new HashSet<String>(Arrays.asList(accionesPermitidas.toUpperCase(Locale.ROOT).split(",")));
        return acciones.contains(codigoAccion.trim().toUpperCase(Locale.ROOT));
    }

    public boolean isEnNotificacion() {
        return "NOTIFICACION".equalsIgnoreCase(etapaCodigo) && "EN_NOTIFICACION".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isCargoPendiente() {
        return "NOTIFICACION".equalsIgnoreCase(etapaCodigo) && "CARGO_PENDIENTE".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isCargoRecibido() {
        return "NOTIFICACION".equalsIgnoreCase(etapaCodigo) && "CARGO_RECIBIDO".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isNotificado() {
        return "NOTIFICACION".equalsIgnoreCase(etapaCodigo) && "NOTIFICADO".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isRequierePublicacionEstado() {
        return "NOTIFICACION".equalsIgnoreCase(etapaCodigo) && "REQUIERE_PUBLICACION".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isFallida() {
        return "FALLIDA".equalsIgnoreCase(estadoNotificacionCodigo)
                || "FALLIDA".equalsIgnoreCase(estadoNotificacion)
                || isRequierePublicacionEstado();
    }

    public boolean isAcuseRegistrado() {
        return idCargoAcuse != null;
    }

    public int getSiguienteIntento() {
        if (numeroIntento == null || numeroIntento < 1) {
            return 1;
        }
        return Math.min(numeroIntento + 1, 4);
    }

    public boolean isIntentosAgotados() {
        return numeroIntento != null && numeroIntento >= 3;
    }

    public String getDocumentoNotificarResumen() {
        if (!tipoResolucion.trim().isEmpty() && !numeroResolucion.trim().isEmpty()) {
            return tipoResolucion + " " + numeroResolucion;
        }
        if (!numeroResolucion.trim().isEmpty()) {
            return numeroResolucion;
        }
        if (!tipoResolucion.trim().isEmpty()) {
            return tipoResolucion;
        }
        if (!tipoDocumento.trim().isEmpty()) {
            return tipoDocumento;
        }
        return "";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String safeProcedimiento(String value) {
        String canonico = ProcedimientoRegistralRules.nombreCanonico(value);
        return canonico == null ? "" : canonico;
    }
}
