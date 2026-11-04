package com.sdrerc.v3.domain;

/**
 * Port de com.sdrerc.domain.dto.sdrercapp.GrupoFamiliarEstadoAlertaDTO (V2).
 *
 * <p>20/08/2026, pedido explícito del usuario: gana {@link #tamanoGrupo}, calculado dinámicamente
 * en cada consulta (no cacheado) — cantidad de solicitudes/integrantes que tiene HOY el vínculo del
 * expediente consultado, sea el grupo familiar confirmado ({@code PERSONA.id_grupo_familiar}) o la
 * alerta "Posible Grupo Familiar" (heurística por apellidos), según cuál de los 2 aplique. Usado por
 * el frontend para armar el texto de confirmación antes de eliminar (regla de tamaño mínimo: 2
 * integrantes o menos disuelve el vínculo por completo; 3 o más solo retira al que se está
 * eliminando). {@code 0} cuando no hay ni grupo confirmado ni alerta activa.</p>
 */
public class GrupoFamiliarEstadoAlertaDTO {

    private final boolean grupoFamiliarConfirmado;
    private final String mensajeAlertaPosibleGrupoFamiliar;
    private final int tamanoGrupo;

    public GrupoFamiliarEstadoAlertaDTO(
            boolean grupoFamiliarConfirmado, String mensajeAlertaPosibleGrupoFamiliar, int tamanoGrupo) {
        this.grupoFamiliarConfirmado = grupoFamiliarConfirmado;
        this.mensajeAlertaPosibleGrupoFamiliar = mensajeAlertaPosibleGrupoFamiliar;
        this.tamanoGrupo = tamanoGrupo;
    }

    public boolean isGrupoFamiliarConfirmado() {
        return grupoFamiliarConfirmado;
    }

    public String getMensajeAlertaPosibleGrupoFamiliar() {
        return mensajeAlertaPosibleGrupoFamiliar;
    }

    public int getTamanoGrupo() {
        return tamanoGrupo;
    }

    public boolean isTieneAlerta() {
        return mensajeAlertaPosibleGrupoFamiliar != null && !mensajeAlertaPosibleGrupoFamiliar.trim().isEmpty();
    }
}
