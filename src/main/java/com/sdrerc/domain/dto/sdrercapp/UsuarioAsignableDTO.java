package com.sdrerc.domain.dto.sdrercapp;

public class UsuarioAsignableDTO {

    private final Long idUsuario;
    private final String username;
    private final String nombreCompleto;
    private final Long idEquipo;
    private final String equipoNombre;
    private final String rolCodigo;
    private final String supervisorNombre;

    public UsuarioAsignableDTO(
            Long idUsuario,
            String username,
            String nombreCompleto,
            Long idEquipo,
            String equipoNombre,
            String rolCodigo,
            String supervisorNombre) {
        this.idUsuario = idUsuario;
        this.username = safe(username);
        this.nombreCompleto = safe(nombreCompleto);
        this.idEquipo = idEquipo;
        this.equipoNombre = safe(equipoNombre);
        this.rolCodigo = safe(rolCodigo);
        this.supervisorNombre = safe(supervisorNombre);
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public Long getIdEquipo() {
        return idEquipo;
    }

    public String getEquipoNombre() {
        return equipoNombre;
    }

    public String getRolCodigo() {
        return rolCodigo;
    }

    public String getSupervisorNombre() {
        return supervisorNombre;
    }

    public String getDisplayName() {
        return nombreCompleto.isEmpty() ? username : nombreCompleto;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
