package com.sdrerc.v3.web.dto;

/**
 * Replica el enrutamiento de segundo factor de {@code LoginFrameV2.enrutarSegundoFactor()} (V2):
 * cambio de password obligatorio primero si aplica; luego correo si el usuario tiene
 * {@code USUARIO.correo} cargado (primera opción); si no, TOTP (verificación si ya está
 * habilitado, enrolamiento la primera vez).
 */
public enum NextLoginStep {
    CAMBIO_PASSWORD,
    EMAIL,
    TOTP,
    TOTP_ENROLL
}
