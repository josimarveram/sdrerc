package com.sdrerc.domain.dto.sdrercapp;

import java.util.List;

public class CargaDiariaResumenDTO {

    private final int totalLeidos;
    private final int validos;
    private final int conErrores;
    private final int posiblesDuplicados;
    private final int conGrupoFamiliar;
    private final int pendientesNumero;
    private final int listosParaRegistrar;
    private final int registrados;

    public CargaDiariaResumenDTO(
            int totalLeidos,
            int validos,
            int conErrores,
            int posiblesDuplicados,
            int conGrupoFamiliar,
            int pendientesNumero,
            int listosParaRegistrar,
            int registrados) {
        this.totalLeidos = totalLeidos;
        this.validos = validos;
        this.conErrores = conErrores;
        this.posiblesDuplicados = posiblesDuplicados;
        this.conGrupoFamiliar = conGrupoFamiliar;
        this.pendientesNumero = pendientesNumero;
        this.listosParaRegistrar = listosParaRegistrar;
        this.registrados = registrados;
    }

    public static CargaDiariaResumenDTO desde(List<CargaDiariaPreviewDTO> items) {
        if (items == null || items.isEmpty()) {
            return new CargaDiariaResumenDTO(0, 0, 0, 0, 0, 0, 0, 0);
        }

        int validos = 0;
        int conErrores = 0;
        int duplicados = 0;
        int grupoFamiliar = 0;
        int pendientesNumero = 0;
        int listos = 0;
        int registrados = 0;

        for (CargaDiariaPreviewDTO item : items) {
            if (item.isPosibleDuplicado()) {
                duplicados++;
            }
            if (item.isGrupoFamiliar() || hasText(item.getObservacionGrupoFamiliar())) {
                grupoFamiliar++;
            }
            if (!item.isRegistrado()
                    && item.isListoParaRegistrar()
                    && !hasText(item.getNumeroExpedienteGenerado())) {
                pendientesNumero++;
            }
            if (item.isListoParaRegistrar()) {
                listos++;
            }
            if (item.isRegistrado()) {
                registrados++;
            }
            String estado = item.getEstadoValidacion();
            if ("Error".equalsIgnoreCase(estado)
                    || "Duplicado".equalsIgnoreCase(estado)
                    || "Con observaciones".equalsIgnoreCase(estado)) {
                conErrores++;
            } else if ("Valido".equalsIgnoreCase(normalizarEstado(estado))
                    || "Advertencia".equalsIgnoreCase(estado)
                    || item.isListoParaRegistrar()) {
                validos++;
            }
        }

        return new CargaDiariaResumenDTO(
                items.size(),
                validos,
                conErrores,
                duplicados,
                grupoFamiliar,
                pendientesNumero,
                listos,
                registrados);
    }

    public int getTotalLeidos() {
        return totalLeidos;
    }

    public int getValidos() {
        return validos;
    }

    public int getConErrores() {
        return conErrores;
    }

    public int getPosiblesDuplicados() {
        return posiblesDuplicados;
    }

    public int getConGrupoFamiliar() {
        return conGrupoFamiliar;
    }

    public int getPendientesNumero() {
        return pendientesNumero;
    }

    public int getListosParaRegistrar() {
        return listosParaRegistrar;
    }

    public int getRegistrados() {
        return registrados;
    }

    private static String normalizarEstado(String estado) {
        return estado == null ? "" : estado.replace("á", "a").replace("Á", "A");
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
