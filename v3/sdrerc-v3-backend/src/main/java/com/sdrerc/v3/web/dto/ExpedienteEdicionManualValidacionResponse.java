package com.sdrerc.v3.web.dto;

import com.sdrerc.v3.domain.ExpedienteEdicionManualDTO;
import java.util.List;

/** Mismo patrón que {@link RegistroManualValidacionResponse}, para el formulario de Edición manual. */
public record ExpedienteEdicionManualValidacionResponse(List<String> mensajes, ExpedienteEdicionManualDTO registro) {
}
