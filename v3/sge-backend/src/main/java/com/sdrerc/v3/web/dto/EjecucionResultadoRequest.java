package com.sdrerc.v3.web.dto;

import java.time.LocalDate;

/** Sección "Resultado de ejecución" — ejecuta únicamente INICIO_EJECUCION (ver EjecucionExpedienteDAO). */
public record EjecucionResultadoRequest(
        Long idExpediente, String resultadoCodigo, String resultadoNombre, LocalDate fechaEjecucion, String comentario) {
}
