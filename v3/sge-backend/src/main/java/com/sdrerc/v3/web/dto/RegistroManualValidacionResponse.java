package com.sdrerc.v3.web.dto;

import com.sdrerc.v3.domain.RegistroManualExpedienteDTO;
import java.util.List;

/**
 * {@code validarConDuplicados} (V2) muta el propio {@code registro} en memoria (marca
 * posibleDuplicado/motivoDuplicado/detección de grupo familiar) además de devolver mensajes; se
 * devuelven ambos para que el frontend pueda reflejar esos flags en el formulario antes de
 * confirmar el registro, igual que hace el panel de Registro manual en V2.
 */
public record RegistroManualValidacionResponse(List<String> mensajes, RegistroManualExpedienteDTO registro) {
}
