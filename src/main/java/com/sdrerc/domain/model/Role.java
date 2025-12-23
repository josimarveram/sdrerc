/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model;

/**
 *
 * @author David
 */
public class Role {
    private Long roleId;
    private String roleCode;
    private String roleName;
    private String description;
    private String status;

    // 🔹 Constructor vacío
    public Role() {
    }

    // 🔹 Constructor completo (opcional)
    public Role(Long roleId, String roleCode, String roleName,
                String description, String status) {
        this.roleId = roleId;
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.description = description;
        this.status = status;
    }

    // ===== GETTERS =====

    public Long getRoleId() {
        return roleId;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    // ===== SETTERS =====

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ===== MÉTODO DE APOYO =====

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
