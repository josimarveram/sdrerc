package com.sdrerc.v3.web.dto;

/** Alta/edición de un bloque de contenido (Administración &gt; Plantillas de documento). */
public record GuardarBloqueRequest(
        Long idPlantillaBloque,
        Long idTipoDocumentoAdjunto,
        String seccion,
        String titulo,
        String contenido,
        String variableCondicion,
        String operadorCondicion,
        String valoresCondicion) {
}
