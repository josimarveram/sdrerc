package com.sdrerc.v3.domain;

import java.time.LocalDate;

/** Port literal de com.sdrerc.domain.dto.sdrercapp.CargaLaboralDocumentoDTO (V2). */
public class CargaLaboralDocumentoDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String etapa;
    private final String estado;
    private final LocalDate fechaVencimiento;
    private final Long diasRestantes;

    public CargaLaboralDocumentoDTO(
            Long idExpediente,
            String numeroExpediente,
            String etapa,
            String estado,
            LocalDate fechaVencimiento,
            Long diasRestantes) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.etapa = safe(etapa);
        this.estado = safe(estado);
        this.fechaVencimiento = fechaVencimiento;
        this.diasRestantes = diasRestantes;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public String getEtapa() {
        return etapa;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public Long getDiasRestantes() {
        return diasRestantes;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
