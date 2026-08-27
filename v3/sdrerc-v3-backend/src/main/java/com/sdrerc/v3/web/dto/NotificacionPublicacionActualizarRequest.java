package com.sdrerc.v3.web.dto;

import java.time.LocalDate;

/** Guardar la fila de Publicación ya persistida (Bandeja Publicación, 4ta pestaña). */
public record NotificacionPublicacionActualizarRequest(Long idExpedienteNotificacion, String estadoNotificacionCodigo, LocalDate fechaPublicacion) {
}
