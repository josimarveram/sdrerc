package com.sdrerc.v3.domain;

/**
 * Info de impacto para el modal de confirmación de eliminación (baja lógica) de un registro desde
 * Registro/Recepción — no existe en V2 (ahí no había un aviso previo de impacto), requisito nuevo
 * explícito de V3 (13/08/2026). No indica por sí sola qué se va a limpiar en cascada (eso lo decide
 * la cantidad real de solicitudes relacionadas en el momento de la baja, ver
 * {@code ExpedienteEdicionManualDAO#eliminar}); es solo una advertencia informativa basada en el
 * estado actual del propio expediente.
 *
 * <p>20/08/2026, pedido explícito del usuario: gana {@link #tamanoPosibleGrupoFamiliar} y
 * {@link #tamanoGrupoFamiliarConfirmado} (0 cuando el flag booleano correspondiente es falso),
 * calculados dinámicamente en cada consulta — mismo criterio/mismos métodos de conteo que usa
 * {@code GrupoFamiliarDAO} al momento real de eliminar, para que el aviso previo muestre el alcance
 * real de la regla de tamaño mínimo (2 o menos → se disuelve todo el vínculo; 3 o más → solo se
 * retira la solicitud eliminada).</p>
 */
public class ImpactoEliminacionRegistroDTO {

    private final boolean potencialDuplicado;
    private final boolean posibleGrupoFamiliar;
    private final boolean grupoFamiliarConfirmado;
    private final int tamanoPosibleGrupoFamiliar;
    private final int tamanoGrupoFamiliarConfirmado;

    public ImpactoEliminacionRegistroDTO(
            boolean potencialDuplicado,
            boolean posibleGrupoFamiliar,
            boolean grupoFamiliarConfirmado,
            int tamanoPosibleGrupoFamiliar,
            int tamanoGrupoFamiliarConfirmado) {
        this.potencialDuplicado = potencialDuplicado;
        this.posibleGrupoFamiliar = posibleGrupoFamiliar;
        this.grupoFamiliarConfirmado = grupoFamiliarConfirmado;
        this.tamanoPosibleGrupoFamiliar = tamanoPosibleGrupoFamiliar;
        this.tamanoGrupoFamiliarConfirmado = tamanoGrupoFamiliarConfirmado;
    }

    public boolean isPotencialDuplicado() {
        return potencialDuplicado;
    }

    public boolean isPosibleGrupoFamiliar() {
        return posibleGrupoFamiliar;
    }

    public boolean isGrupoFamiliarConfirmado() {
        return grupoFamiliarConfirmado;
    }

    public int getTamanoPosibleGrupoFamiliar() {
        return tamanoPosibleGrupoFamiliar;
    }

    public int getTamanoGrupoFamiliarConfirmado() {
        return tamanoGrupoFamiliarConfirmado;
    }

    public boolean isSinImpacto() {
        return !potencialDuplicado && !posibleGrupoFamiliar && !grupoFamiliarConfirmado;
    }
}
