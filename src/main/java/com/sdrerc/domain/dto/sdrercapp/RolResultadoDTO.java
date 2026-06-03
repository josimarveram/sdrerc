package com.sdrerc.domain.dto.sdrercapp;

public class RolResultadoDTO {

    private final boolean exitoso;
    private final String mensaje;
    private final RolDTO rol;

    private RolResultadoDTO(boolean exitoso, String mensaje, RolDTO rol) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.rol = rol;
    }

    public static RolResultadoDTO exito(String mensaje, RolDTO rol) {
        return new RolResultadoDTO(true, mensaje, rol);
    }

    public static RolResultadoDTO fallo(String mensaje) {
        return new RolResultadoDTO(false, mensaje, null);
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public String getMensaje() {
        return mensaje;
    }

    public RolDTO getRol() {
        return rol;
    }
}
