package com.sdrerc.domain.dto.sdrercapp;

public class UbigeoItemDTO {

    private final Long id;
    private final String codigo;
    private final String nombre;
    private final Long idPadre;

    public UbigeoItemDTO(Long id, String codigo, String nombre, Long idPadre) {
        this.id = id;
        this.codigo = trimToEmpty(codigo);
        this.nombre = trimToEmpty(nombre);
        this.idPadre = idPadre;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public Long getIdPadre() {
        return idPadre;
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
