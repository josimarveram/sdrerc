package com.sdrerc.v3.domain;

import java.time.LocalDateTime;

/**
 * Port literal de com.sdrerc.domain.dto.sdrercapp.UsuarioAutenticacionDTO (V2). Datos de
 * {@code usuario} necesarios exclusivamente para el flujo de login (credenciales + 2FA);
 * separado del DTO administrativo de usuario para no exponer nunca el hash de contraseña ni
 * el secreto TOTP cifrado fuera de este flujo.
 */
public class UsuarioAutenticacionDTO {

    private Long idUsuario;
    private String username;
    private String nombreCompleto;
    private String passwordHash;
    private boolean activo;
    private String estado;
    private boolean debeCambiarPassword;
    private String totpSecretCifrado;
    private boolean totpHabilitado;
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;
    private String correo;

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isDebeCambiarPassword() {
        return debeCambiarPassword;
    }

    public void setDebeCambiarPassword(boolean debeCambiarPassword) {
        this.debeCambiarPassword = debeCambiarPassword;
    }

    public String getTotpSecretCifrado() {
        return totpSecretCifrado;
    }

    public void setTotpSecretCifrado(String totpSecretCifrado) {
        this.totpSecretCifrado = totpSecretCifrado;
    }

    public boolean isTotpHabilitado() {
        return totpHabilitado;
    }

    public void setTotpHabilitado(boolean totpHabilitado) {
        this.totpHabilitado = totpHabilitado;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
