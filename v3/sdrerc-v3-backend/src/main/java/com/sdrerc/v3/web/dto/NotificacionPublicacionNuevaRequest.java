package com.sdrerc.v3.web.dto;

import java.time.LocalDate;

/** "+ Agregar publicación", fila borrador guardada (Bandeja Publicación, 4ta pestaña). */
public record NotificacionPublicacionNuevaRequest(Long idExpediente, Long idDocumentoAnalizado, LocalDate fechaPublicacion) {
}
