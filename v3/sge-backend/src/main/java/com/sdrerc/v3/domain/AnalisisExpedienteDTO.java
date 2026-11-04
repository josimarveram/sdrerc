package com.sdrerc.v3.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Port acotado de com.sdrerc.domain.dto.sdrercapp.AnalisisExpedienteDTO (V2, 41 campos) — fila de
 * la Bandeja Análisis. V3 solo porta los campos que la bandeja (13 columnas del
 * {@code AnalisisTableModel} real: Días/Expediente/N° SGD/Fecha solicitud/Procedimiento/Tipo
 * acta/Nro. acta/Titular/Abogado designado/Estado/Asociados, más id/etapa) y la lógica de negocio
 * embebida realmente necesitan. Los campos de contacto del solicitante (correo/teléfono/
 * dirección/ubigeo) NO se portan acá: el "Panel de datos" de esta bandeja reutiliza el mismo
 * endpoint genérico de Registro (ver Javadoc de {@code AnalisisExpedienteDAO}), no un SELECT
 * propio — evita duplicar esos ~15 campos en 2 DTOs distintos.
 *
 * <p>Métodos de negocio embebidos, calculados (no persistidos) — mismos nombres/criterios exactos
 * que el DTO real de V2, verificados contra {@code JPanelAnalisisV2}/{@code AnalisisExpedienteDAO}
 * (20/08/2026): {@link #isRecibible()}, {@link #isRegistrable()}, {@link #isEnviableVerificacion()},
 * {@link #isDerivado()}, {@link #isObservado()}, {@link #isPorVencerOVencido()}.</p>
 */
public class AnalisisExpedienteDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroExpedienteSgd;
    private final String procedimiento;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final String responsable;
    private final Long idResponsable;
    private final Long idEquipoResponsable;
    private final LocalDate fechaRecepcion;
    private final LocalDate fechaVencimiento;
    private final LocalDateTime fechaRegistro;
    private final Long diasRestantes;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final boolean grupoFamiliar;
    private final int asociadosConfirmados;
    private final int documentosAnalizados;

    public AnalisisExpedienteDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroExpedienteSgd,
            String procedimiento,
            String tipoActa,
            String numeroActa,
            String titular,
            String responsable,
            Long idResponsable,
            Long idEquipoResponsable,
            LocalDate fechaRecepcion,
            LocalDate fechaVencimiento,
            LocalDateTime fechaRegistro,
            Long diasRestantes,
            String etapaCodigo,
            String estadoCodigo,
            boolean grupoFamiliar,
            int asociadosConfirmados,
            int documentosAnalizados) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroExpedienteSgd = safe(numeroExpedienteSgd);
        this.procedimiento = safe(procedimiento);
        this.tipoActa = safe(tipoActa);
        this.numeroActa = safe(numeroActa);
        this.titular = safe(titular);
        this.responsable = safe(responsable);
        this.idResponsable = idResponsable;
        this.idEquipoResponsable = idEquipoResponsable;
        this.fechaRecepcion = fechaRecepcion;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaRegistro = fechaRegistro;
        this.diasRestantes = diasRestantes;
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.grupoFamiliar = grupoFamiliar;
        this.asociadosConfirmados = asociadosConfirmados;
        this.documentosAnalizados = documentosAnalizados;
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
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

    public boolean isGrupoFamiliar() {
        return grupoFamiliar;
    }

    public int getAsociadosConfirmados() {
        return asociadosConfirmados;
    }

    public int getDocumentosAnalizados() {
        return documentosAnalizados;
    }

    public boolean isRecibible() {
        return "ASIGNACION".equalsIgnoreCase(etapaCodigo) && "ASIGNADO".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isRegistrable() {
        return "ANALISIS".equalsIgnoreCase(etapaCodigo)
                && ("RECIBIDO_POR_ABOGADO".equalsIgnoreCase(estadoCodigo)
                || "OBSERVADO".equalsIgnoreCase(estadoCodigo)
                || "DERIVADO".equalsIgnoreCase(estadoCodigo)
                || "SUBSANADO".equalsIgnoreCase(estadoCodigo));
    }

    public boolean isEnviableVerificacion() {
        return isRegistrable() || ("ANALISIS".equalsIgnoreCase(etapaCodigo) && "ATENDIDO".equalsIgnoreCase(estadoCodigo));
    }

    public boolean isDerivado() {
        return "ANALISIS".equalsIgnoreCase(etapaCodigo) && "DERIVADO".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isObservado() {
        return "OBSERVADO".equalsIgnoreCase(estadoCodigo);
    }

    /** "Por vencer / vencidos" (KPI único de esta bandeja, umbral &lt;=3 días — distinto del 0-5 que usan Registro/Asignación). */
    public boolean isPorVencerOVencido() {
        return diasRestantes != null && diasRestantes <= 3;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
