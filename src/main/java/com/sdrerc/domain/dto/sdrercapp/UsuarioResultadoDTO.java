package com.sdrerc.domain.dto.sdrercapp;

public class UsuarioResultadoDTO {

    private final boolean exitoso;
    private final String mensaje;
    private final UsuarioDTO usuario;

    private UsuarioResultadoDTO(boolean exitoso, String mensaje, UsuarioDTO usuario) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.usuario = usuario;
    }

    public static UsuarioResultadoDTO exito(String mensaje, UsuarioDTO usuario) {
        return new UsuarioResultadoDTO(true, mensaje, usuario);
    }

    public static UsuarioResultadoDTO fallo(String mensaje) {
        return new UsuarioResultadoDTO(false, mensaje, null);
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public String getMensaje() {
        return mensaje;
    }

    public UsuarioDTO getUsuario() {
        return usuario;
    }
}
