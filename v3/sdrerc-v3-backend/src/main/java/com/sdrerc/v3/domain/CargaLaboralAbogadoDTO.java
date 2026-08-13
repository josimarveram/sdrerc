package com.sdrerc.v3.domain;

/**
 * Port literal de com.sdrerc.domain.dto.sdrercapp.CargaLaboralAbogadoDTO (V2), usado por el
 * Dashboard para el grafico "Carga por abogado (top 10)" (mismo calculo que la bandeja Carga
 * Abogados de Asignacion, sin filtro de equipo).
 */
public record CargaLaboralAbogadoDTO(
        Long idUsuario,
        String abogado,
        String supervisor,
        int analisisPorRecibir,
        int analisisEnProceso,
        int analisisObservado,
        int analisisCartaIntermedia,
        int enVerificacion,
        int enEjecucion,
        int porVencer,
        int vencidos) {

    /** Total de la carga de Analisis (suma de las 4 subcolumnas), incluye lo asignado aun no recibido. */
    public int enAnalisis() {
        return analisisPorRecibir + analisisEnProceso + analisisObservado + analisisCartaIntermedia;
    }

    public int cargaTotal() {
        return enAnalisis() + enVerificacion + enEjecucion;
    }
}
