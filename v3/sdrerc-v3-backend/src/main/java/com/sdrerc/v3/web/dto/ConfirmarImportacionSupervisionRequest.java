package com.sdrerc.v3.web.dto;

import com.sdrerc.v3.domain.EquipoSupervisionImportItemDTO;
import java.util.List;

/** "Confirmar importación" de la plantilla Excel de "Personal por supervisor". */
public record ConfirmarImportacionSupervisionRequest(List<EquipoSupervisionImportItemDTO> filas, boolean incluirAdvertencias) {
}
