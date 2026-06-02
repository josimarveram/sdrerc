package com.sdrerc.domain.dto.sdrercapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CargaDiariaResultadoDTO {

    private final int registrados;
    private final int omitidos;
    private final String mensaje;
    private final List<CargaDiariaPreviewDTO> registros;

    public CargaDiariaResultadoDTO(
            int registrados,
            int omitidos,
            String mensaje,
            List<CargaDiariaPreviewDTO> registros) {
        this.registrados = registrados;
        this.omitidos = omitidos;
        this.mensaje = mensaje == null ? "" : mensaje;
        this.registros = registros == null
                ? Collections.<CargaDiariaPreviewDTO>emptyList()
                : Collections.unmodifiableList(new ArrayList<CargaDiariaPreviewDTO>(registros));
    }

    public int getRegistrados() {
        return registrados;
    }

    public int getOmitidos() {
        return omitidos;
    }

    public String getMensaje() {
        return mensaje;
    }

    public List<CargaDiariaPreviewDTO> getRegistros() {
        return registros;
    }
}
