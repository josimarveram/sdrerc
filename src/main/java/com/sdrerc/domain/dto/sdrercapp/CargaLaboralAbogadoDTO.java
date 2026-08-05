package com.sdrerc.domain.dto.sdrercapp;

public class CargaLaboralAbogadoDTO {

    private final Long idUsuario;
    private final String abogado;
    private final String equipo;
    private final String supervisor;
    private final int enAnalisis;
    private final String analisisDetalle;
    private final int enEjecucion;
    private final String ejecucionDetalle;
    private final int documentosPendientes;
    private final String documentosDetalle;
    private final int porVencer;
    private final int vencidos;

    public CargaLaboralAbogadoDTO(
            Long idUsuario,
            String abogado,
            String equipo,
            String supervisor,
            int enAnalisis,
            String analisisDetalle,
            int enEjecucion,
            String ejecucionDetalle,
            int documentosPendientes,
            String documentosDetalle,
            int porVencer,
            int vencidos) {
        this.idUsuario = idUsuario;
        this.abogado = safe(abogado);
        this.equipo = safe(equipo);
        this.supervisor = safe(supervisor);
        this.enAnalisis = enAnalisis;
        this.analisisDetalle = safe(analisisDetalle);
        this.enEjecucion = enEjecucion;
        this.ejecucionDetalle = safe(ejecucionDetalle);
        this.documentosPendientes = documentosPendientes;
        this.documentosDetalle = safe(documentosDetalle);
        this.porVencer = porVencer;
        this.vencidos = vencidos;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getAbogado() {
        return abogado;
    }

    public String getEquipo() {
        return equipo;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public int getEnAnalisis() {
        return enAnalisis;
    }

    public String getAnalisisDetalle() {
        return analisisDetalle;
    }

    public int getEnEjecucion() {
        return enEjecucion;
    }

    public String getEjecucionDetalle() {
        return ejecucionDetalle;
    }

    public int getDocumentosPendientes() {
        return documentosPendientes;
    }

    public String getDocumentosDetalle() {
        return documentosDetalle;
    }

    public int getPorVencer() {
        return porVencer;
    }

    public int getVencidos() {
        return vencidos;
    }

    public int getCargaTotal() {
        return enAnalisis + enEjecucion + documentosPendientes;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
