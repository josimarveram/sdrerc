package com.sdrerc.v3.web.dto;

import java.util.List;

/** Reordenar bloques de contenido (Administración &gt; Plantillas de documento). */
public record GuardarOrdenBloquesRequest(List<Long> idsOrdenados) {
}
