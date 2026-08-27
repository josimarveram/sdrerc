package com.sdrerc.v3.domain;

/** Resultado de confirmar la importación de "Personal por supervisor" (fila por fila, sin todo-o-nada). */
public record EquipoSupervisionImportResultadoDTO(int fila, String usuarioAbogado, String usuarioSupervisor, String resultado, String mensaje) {
}
