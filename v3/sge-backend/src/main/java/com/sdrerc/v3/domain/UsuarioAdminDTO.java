package com.sdrerc.v3.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Port de {@code com.sdrerc.domain.dto.sdrercapp.UsuarioDTO} (V2), con 2 diferencias deliberadas
 * de diseño (Administración &gt; Usuarios V3, mejor forma acordada con el usuario, no un port
 * literal — ver "Principio rector" en v3/CLAUDE.md: paridad de datos, libertad de diseño):
 *
 * <p><b>1. Un solo campo {@code nombreCompleto}, sin {@code nombres}/{@code apellidos}
 * separados</b>: {@code USUARIO} solo tiene la columna {@code nombre_completo} (verificado contra
 * {@code user_tab_columns} real) — V2 simulaba 2 campos en el formulario Swing
 * ({@code txtNombres}/{@code txtApellidos}) pero al releer un usuario para editar, "Apellidos"
 * siempre volvía vacío y "Nombres" traía el nombre completo entero (bug de diseño real de V2,
 * exactamente el "medio desordenado" que el usuario pidió mejorar). V3 usa un único campo de
 * texto libre, consistente con lo que la BD realmente guarda.</p>
 *
 * <p><b>2. Multi-equipo</b>: {@code idsEquipos}/{@code equiposResumen} (plural), no
 * {@code idEquipo}/{@code equipoNombre} (singular) como V2. En V2, el formulario de Usuarios solo
 * permite 1 equipo activo a la vez ({@code UsuarioEquipoDAO.sincronizarEquipo} desactiva
 * cualquier otro), pero el módulo Equipo Jurídico sí permite que un usuario pertenezca a varios
 * equipos simultáneamente — inconsistencia real de V2, corregida en V3 permitiendo multi-equipo
 * desde ambos lados (decisión ya confirmada explícitamente por el usuario).</p>
 */
public class UsuarioAdminDTO {

    private Long idUsuario;
    private String username;
    private String nombreCompleto;
    private String tipoDocumento;
    private String numeroDocumento;
    private String correo;
    private String telefono;
    private String estado;
    private boolean activo;
    private String rolesResumen;
    private List<Long> idsRoles = new ArrayList<>();
    private String equiposResumen;
    private List<Long> idsEquipos = new ArrayList<>();
    private LocalDateTime creadoEn;
    private LocalDateTime modificadoEn;

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getRolesResumen() {
        return rolesResumen;
    }

    public void setRolesResumen(String rolesResumen) {
        this.rolesResumen = rolesResumen;
    }

    public List<Long> getIdsRoles() {
        return idsRoles;
    }

    public void setIdsRoles(List<Long> idsRoles) {
        this.idsRoles = idsRoles == null ? new ArrayList<>() : idsRoles;
    }

    public String getEquiposResumen() {
        return equiposResumen;
    }

    public void setEquiposResumen(String equiposResumen) {
        this.equiposResumen = equiposResumen;
    }

    public List<Long> getIdsEquipos() {
        return idsEquipos;
    }

    public void setIdsEquipos(List<Long> idsEquipos) {
        this.idsEquipos = idsEquipos == null ? new ArrayList<>() : idsEquipos;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getModificadoEn() {
        return modificadoEn;
    }

    public void setModificadoEn(LocalDateTime modificadoEn) {
        this.modificadoEn = modificadoEn;
    }
}
