/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.Role;
import com.sdrerc.domain.model.PaginatedResult;
import com.sdrerc.domain.model.User;
import com.sdrerc.domain.model.UsuarioListadoItem;
import com.sdrerc.infrastructure.repository.UserRepository;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author David
 */
public class UserService {
    private static final String MENSAJE_USUARIO_OPERATIVO_SIN_TECNICO =
            "Los usuarios con rol ABOGADO o SUPERVISION deben registrarse desde Equipo Jurídico "
            + "para crear automáticamente la persona operativa y su vínculo técnico.";

    private final UserRepository repo;

    
    public UserService() {
        this.repo = new UserRepository();
    }

    public void registrar(User role) throws SQLException {
        if (role.getUsername()== null || role.getUsername().isEmpty()) {
            throw new IllegalArgumentException("El nombre del usuario es obligatorio");
        }
        repo.save(role);
    }

    public void actualizar(User role) throws SQLException {
        repo.update(role);
    }

    public List<User> listar() throws SQLException {
        return repo.findAll();
    }
    
    public List<User> buscar(String nombre, String estado) throws SQLException{
        return repo.buscar(nombre, estado);
    }

    public PaginatedResult<UsuarioListadoItem> buscarPaginado(
            String filtro, String estado, int page, int pageSize) throws SQLException {
        return repo.buscarPaginado(filtro, estado, page, pageSize);
    }

    public void cambiarEstado(Long id, String estado) throws SQLException {
        repo.cambiarEstado(id, estado);
    }

    public void vincularTecnico(Long userId, Integer idTecnico) throws SQLException {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Seleccione un usuario.");
        }
        if (idTecnico == null || idTecnico <= 0) {
            throw new IllegalArgumentException("Seleccione un técnico válido.");
        }

        Long idTecnicoActual = repo.obtenerIdTecnicoUsuario(userId);
        if (idTecnicoActual != null && idTecnicoActual.intValue() == idTecnico.intValue()) {
            throw new IllegalStateException("El usuario ya tiene vinculado este técnico.");
        }

        if (repo.existeTecnicoVinculadoAOtroUsuario(userId, idTecnico)) {
            throw new IllegalStateException("Este técnico ya está vinculado a otro usuario.");
        }

        try {
            repo.vincularTecnico(userId, idTecnico);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1) {
                throw new IllegalStateException("Este técnico ya está vinculado a otro usuario.", ex);
            }
            throw ex;
        }
    }

    public void cambiarPassword(Long userId, String hash)
            throws SQLException {

        repo.actualizarPassword(userId, hash);
    }
    
    public List<Role> obtenerRolesUsuario(Long userId) throws SQLException {
        return repo.listarRolesPorUsuario(userId);
    }
    
    public void asignarRoles(Long userId, List<Long> roles)
            throws SQLException {
        validarRolesOperativosConTecnico(userId, roles);
        repo.asignarRoles(userId, roles);
    }

    private void validarRolesOperativosConTecnico(Long userId, List<Long> roleIds)
            throws SQLException {
        if (userId == null || userId <= 0 || roleIds == null || roleIds.isEmpty()) {
            return;
        }

        if (!incluyeRolOperativoJuridico(roleIds)) {
            return;
        }

        Long idTecnico = repo.obtenerIdTecnicoUsuario(userId);
        if (idTecnico == null) {
            throw new IllegalStateException(MENSAJE_USUARIO_OPERATIVO_SIN_TECNICO);
        }
    }

    private boolean incluyeRolOperativoJuridico(List<Long> roleIds)
            throws SQLException {
        List<String> roleNames = repo.obtenerRoleNamesPorIds(roleIds);
        for (String roleName : roleNames) {
            String normalized = roleName == null ? "" : roleName.trim().toUpperCase();
            if ("ABOGADO".equals(normalized) || "SUPERVISION".equals(normalized)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean tieneRol(Long userId, String roleName)
            throws SQLException {
        return repo.tieneRol(userId, roleName);
    }
    
    public List<User> listarPorRol(String roleName)
            throws SQLException {
        return repo.listarPorRol(roleName);
    }
}
