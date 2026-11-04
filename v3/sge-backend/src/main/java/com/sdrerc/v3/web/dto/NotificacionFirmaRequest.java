package com.sdrerc.v3.web.dto;

import java.time.LocalDate;

/** Ícono "Guardar" de la grilla "Documentos a firmar" (mini-panel "① Emisión"). */
public record NotificacionFirmaRequest(
        Long idDocumentoAnalizado,
        String numeroDocumento,
        LocalDate fechaEmision,
        String estadoDocumentoCodigo) {
}
