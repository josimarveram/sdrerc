/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author David
 */
public class User {
    /*
    private int id;
    private String username;
    private String fullname;
    private String role;

    public User(int id, String username, String fullname, String role) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.role = role;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getFullname() { return fullname; }
    public String getRole() { return role; }
    
    */
    
    private Long userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String status;
    private Long idTecnico;
    private List<String> roles = new ArrayList<>();

    // 🔹 Constructor vacío
    public User() {
    }

    // 🔹 Constructor útil (opcional)
    public User(Long userId, String username,String fullName, String status) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.status = status;
    }
    
    

    // ===== GETTERS =====

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    /**
     * ✔ Necesario para autenticación
     * ✔ Usado por LoginUseCase y PasswordEncoder
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getStatus() {
        return status;
    }

    public Long getIdTecnico() {
        return idTecnico;
    }

    public List<String> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    // ===== SETTERS =====

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * ⚠️ Nunca guardar password plano
     * Siempre asignar el hash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setIdTecnico(Long idTecnico) {
        this.idTecnico = idTecnico;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles == null ? new ArrayList<>() : new ArrayList<>(roles);
    }

    // ===== MÉTODOS DE APOYO (OPCIONAL) =====

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    public boolean hasRole(String roleName) {
        if (roleName == null) {
            return false;
        }
        for (String role : roles) {
            if (roleName.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }
    
    public String toString() {
        return fullName;
    }
}
