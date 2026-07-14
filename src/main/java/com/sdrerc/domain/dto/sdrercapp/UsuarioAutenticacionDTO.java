package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDateTime;

/**
 * Datos de {@code usuario} necesarios exclusivamente para el flujo de login
 * (credenciales + 2FA). Separado de {@link UsuarioDTO} porque este ultimo se
 * usa en pantallas de administracion y no debe exponer nunca el hash de
 * contraseña ni el secreto TOTP cifrado.
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
}
