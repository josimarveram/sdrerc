package com.sdrerc.domain.dto.sdrercapp;

public class EquipoJuridicoResultadoDTO {

    private final boolean exito;
    private final String mensaje;
    private final EquipoJuridicoDTO equipo;

    private EquipoJuridicoResultadoDTO(boolean exito, String mensaje, EquipoJuridicoDTO equipo) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.equipo = equipo;
    }

    public static EquipoJuridicoResultadoDTO exito(String mensaje, EquipoJuridicoDTO equipo) {
        return new EquipoJuridicoResultadoDTO(true, mensaje, equipo);
    }

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public EquipoJuridicoDTO getEquipo() {
        return equipo;
    }
}
