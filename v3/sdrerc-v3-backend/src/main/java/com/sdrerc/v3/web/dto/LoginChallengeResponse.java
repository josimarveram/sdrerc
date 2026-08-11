package com.sdrerc.v3.web.dto;

/**
 * Mensajes de error de login deliberadamente genericos (mismo criterio que V2: nunca revelar si
 * un username existe o no). Este DTO solo se emite DESPUES de validar usuario+password con
 * exito; por eso ya puede incluir datos del usuario sin violar esa regla.
 */
public record LoginChallengeResponse(
        String challengeToken,
        NextLoginStep nextStep,
        String username,
        String nombreCompleto,
        boolean tieneCorreo,
        String correoEnmascarado,
        boolean totpHabilitado) {
}
