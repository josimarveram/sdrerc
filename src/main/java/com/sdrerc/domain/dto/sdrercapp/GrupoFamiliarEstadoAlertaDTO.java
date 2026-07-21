package com.sdrerc.domain.dto.sdrercapp;

public class GrupoFamiliarEstadoAlertaDTO {

    private final boolean grupoFamiliarConfirmado;
    private final String mensajeAlertaPosibleGrupoFamiliar;

    public GrupoFamiliarEstadoAlertaDTO(boolean grupoFamiliarConfirmado, String mensajeAlertaPosibleGrupoFamiliar) {
        this.grupoFamiliarConfirmado = grupoFamiliarConfirmado;
        this.mensajeAlertaPosibleGrupoFamiliar = mensajeAlertaPosibleGrupoFamiliar;
    }

    public boolean isGrupoFamiliarConfirmado() {
        return grupoFamiliarConfirmado;
    }

    public String getMensajeAlertaPosibleGrupoFamiliar() {
        return mensajeAlertaPosibleGrupoFamiliar;
    }

    public boolean tieneAlertaPosibleGrupoFamiliar() {
        return mensajeAlertaPosibleGrupoFamiliar != null && !mensajeAlertaPosibleGrupoFamiliar.trim().isEmpty();
    }
}
