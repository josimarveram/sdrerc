package com.sdrerc.v3.domain;

import java.time.LocalDateTime;

/**
 * Lectura de solo consulta del último "Registrar resultado de ejecución" (INICIO_EJECUCION) ya
 * aplicado a un expediente — permite que el panel muestre de forma persistente (sobrevive al
 * remount de la subpestaña al cambiar de tab, ver EjecucionExpedienteDAO#obtenerUltimoResultadoRegistrado)
 * qué resultado quedó registrado, sin depender de un mensaje efímero de la última acción.
 * {@code resultadoNombre} viene del campo {@code motivo} de EXPEDIENTE_HISTORIAL; {@code null} si el
 * expediente nunca registró un resultado de ejecución.
 */
public record EjecucionUltimoResultadoDTO(String resultadoNombre, LocalDateTime fechaRegistro) {
}
