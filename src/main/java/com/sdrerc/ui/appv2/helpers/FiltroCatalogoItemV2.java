package com.sdrerc.ui.appv2.helpers;

public class FiltroCatalogoItemV2 {

    private final String codigo;
    private final String nombreVisible;

    public FiltroCatalogoItemV2(String codigo, String nombreVisible) {
        this.codigo = codigo;
        this.nombreVisible = nombreVisible;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    public boolean hasCodigo() {
        return codigo != null && !codigo.trim().isEmpty();
    }

    @Override
    public String toString() {
        return nombreVisible == null || nombreVisible.trim().isEmpty() ? "-" : nombreVisible;
    }
}
