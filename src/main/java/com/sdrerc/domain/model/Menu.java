/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model;

/**
 *
 * @author David
 */
public class Menu {
    private Long menuId;
    private String menuCode;
    private String menuName;
    private String url;
    private Long parentId;
    private Integer orderNumber;
    private String icon;
    private String status;

    // 🔹 Constructor vacío
    public Menu() {
    }

    // 🔹 Constructor completo (opcional)
    public Menu(Long menuId, String menuCode, String menuName,
                String url, Long parentId, Integer orderNumber,
                String icon, String status) {
        this.menuId = menuId;
        this.menuCode = menuCode;
        this.menuName = menuName;
        this.url = url;
        this.parentId = parentId;
        this.orderNumber = orderNumber;
        this.icon = icon;
        this.status = status;
    }

    // ===== GETTERS =====

    public Long getMenuId() {
        return menuId;
    }

    public String getMenuCode() {
        return menuCode;
    }

    public String getMenuName() {
        return menuName;
    }

    public String getUrl() {
        return url;
    }

    public Long getParentId() {
        return parentId;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public String getIcon() {
        return icon;
    }

    public String getStatus() {
        return status;
    }

    // ===== SETTERS =====

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public void setMenuCode(String menuCode) {
        this.menuCode = menuCode;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ===== MÉTODO DE APOYO =====

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
