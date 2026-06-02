package com.sdrerc.domain.dto.sdrercapp;

public class CatalogoItemDTO {

    private final String codigo;
    private final String nombre;

    public CatalogoItemDTO(String codigo, String nombre) {
        this.codigo = trimToNull(codigo);
        this.nombre = trimToNull(nombre);
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean hasCodigo() {
        return codigo != null && !codigo.trim().isEmpty();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
