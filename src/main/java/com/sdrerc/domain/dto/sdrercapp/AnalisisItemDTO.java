package com.sdrerc.domain.dto.sdrercapp;

public class AnalisisItemDTO {

    private final Long idExpedienteAnalisis;
    private final int numeroAnalisis;
    private final String titulo;
    private final String estadoAnalisis;
    private final AnalisisDetalleDTO detalle;

    public AnalisisItemDTO(
            Long idExpedienteAnalisis,
            int numeroAnalisis,
            String titulo,
            String estadoAnalisis,
            AnalisisDetalleDTO detalle) {
        this.idExpedienteAnalisis = idExpedienteAnalisis;
        this.numeroAnalisis = numeroAnalisis <= 0 ? 1 : numeroAnalisis;
        this.titulo = safe(titulo);
        this.estadoAnalisis = safe(estadoAnalisis);
        this.detalle = detalle;
    }

    public Long getIdExpedienteAnalisis() {
        return idExpedienteAnalisis;
    }

    public int getNumeroAnalisis() {
        return numeroAnalisis;
    }

    public String getTitulo() {
        if (!titulo.isEmpty()) {
            return titulo;
        }
        return "Análisis " + numeroAnalisis;
    }

    public String getEstadoAnalisis() {
        return estadoAnalisis;
    }

    public AnalisisDetalleDTO getDetalle() {
        return detalle;
    }

    @Override
    public String toString() {
        return getTitulo();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
