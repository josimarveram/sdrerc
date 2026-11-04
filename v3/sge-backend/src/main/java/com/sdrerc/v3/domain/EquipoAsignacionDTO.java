package com.sdrerc.v3.domain;

/** Port literal de com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO (V2). */
public class EquipoAsignacionDTO {

    private final Long idEquipo;
    private final String codigo;
    private final String nombre;
    private final String area;

    public EquipoAsignacionDTO(Long idEquipo, String codigo, String nombre, String area) {
        this.idEquipo = idEquipo;
        this.codigo = safe(codigo);
        this.nombre = safe(nombre);
        this.area = safe(area);
    }

    public Long getIdEquipo() {
        return idEquipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getArea() {
        return area;
    }

    public String getDisplayName() {
        if (area.isEmpty()) {
            return nombre;
        }
        return nombre + " - " + area;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
