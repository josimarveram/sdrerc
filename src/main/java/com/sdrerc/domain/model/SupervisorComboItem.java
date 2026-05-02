package com.sdrerc.domain.model;

public class SupervisorComboItem {

    public static final String TIPO_TODOS = "TODOS";
    public static final String TIPO_SIN_SUPERVISOR = "SIN_SUPERVISOR";
    public static final String TIPO_SUPERVISOR = "SUPERVISOR";

    private Long userId;
    private String nombreVisible;
    private String username;
    private String tipo;

    public SupervisorComboItem(Long userId, String nombreVisible, String username, String tipo) {
        this.userId = userId;
        this.nombreVisible = nombreVisible;
        this.username = username;
        this.tipo = tipo;
    }

    public static SupervisorComboItem todos() {
        return new SupervisorComboItem(null, "TODOS", null, TIPO_TODOS);
    }

    public static SupervisorComboItem sinSupervisor() {
        return new SupervisorComboItem(null, "SIN SUPERVISOR", null, TIPO_SIN_SUPERVISOR);
    }

    public Long getUserId() {
        return userId;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    public String getUsername() {
        return username;
    }

    public String getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        if (TIPO_TODOS.equals(tipo) || TIPO_SIN_SUPERVISOR.equals(tipo)) {
            return nombreVisible;
        }
        return nombreVisible + (username == null || username.trim().isEmpty() ? "" : " (" + username + ")");
    }
}
