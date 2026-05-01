/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.Role;
import com.sdrerc.domain.model.User;
import com.sdrerc.infrastructure.repository.UserRepository;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author David
 */
public class UserService {
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
        repo.asignarRoles(userId, roles);
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
