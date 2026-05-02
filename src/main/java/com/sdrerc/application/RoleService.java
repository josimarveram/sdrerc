/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.Role;
import com.sdrerc.domain.model.PaginatedResult;
import com.sdrerc.infrastructure.repository.RoleRepository;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author David
 */
public class RoleService {
    
    private final RoleRepository repository = new RoleRepository();

    public void registrar(Role role) throws SQLException {
        if (role.getRoleName() == null || role.getRoleName().isEmpty()) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio");
        }
        repository.save(role);
    }

    public void actualizar(Role role) throws SQLException {
        repository.update(role);
    }

    public List<Role> listar() throws SQLException {
        return repository.findAll();
    }
    
    public List<Role> buscar(String nombre, String estado) throws SQLException {
        return repository.buscar(nombre, estado);
    }

    public PaginatedResult<Role> buscarPaginado(
            String filtro, String estado, int page, int pageSize) throws SQLException {
        return repository.buscarPaginado(filtro, estado, page, pageSize);
    }
    
    public void cambiarEstado(Long id, String estado) throws SQLException {
        repository.cambiarEstado(id, estado);
    }
}
