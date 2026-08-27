package com.sdrerc.v3.web.dto;

/** "Restablecer clave" de Administración &gt; Usuarios (mismo diálogo que V2, {@code DlgRestablecerClaveV2}). */
public record RestablecerClaveRequest(String passwordTemporal, boolean reiniciarVerificacionDosPasos) {
}
