/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.User;
import com.sdrerc.infrastructure.repository.SupervisionRepository;
import com.sdrerc.infrastructure.repository.UserRepository;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author David
 */
public class SupervisionService {
    private final UserRepository userRepo;
    private final SupervisionRepository supervisionRepo;
    
    public SupervisionService() {
        this.userRepo = new UserRepository();
        this.supervisionRepo = new SupervisionRepository();
    }
    
    
    public void asignarAbogados(Long supervisorId,
                                List<Long> abogados)
            throws SQLException {
        if (abogados == null) {
            abogados = Collections.emptyList();
        }

        if (supervisorId == null || supervisorId <= 0) {
            throw new IllegalArgumentException("Seleccione un supervisor válido.");
        }

        if (!userRepo.tieneRol(supervisorId, "SUPERVISION")) {
            throw new IllegalStateException("Esta opción solo aplica a usuarios con rol SUPERVISION.");
        }

        for (Long abogadoId : abogados) {
            if (abogadoId == null || abogadoId <= 0) {
                throw new IllegalArgumentException("Seleccione un abogado válido.");
            }
            if (!userRepo.tieneRol(abogadoId, "ABOGADO")) {
                throw new IllegalStateException("El usuario seleccionado no tiene rol ABOGADO.");
            }
            if (supervisionRepo.abogadoAsignadoAOtroSupervisor(supervisorId, abogadoId)) {
                throw new IllegalStateException("El abogado ya está asignado a otro supervisor.");
            }
        }

        try {
            supervisionRepo.reemplazarAbogados(supervisorId, abogados);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1) {
                throw new IllegalStateException("El abogado ya está asignado a otro supervisor.", ex);
            }
            throw ex;
        }
    }

    public void quitarAbogado(Long supervisorId, Long abogadoId)
            throws SQLException {
        supervisionRepo.delete(supervisorId, abogadoId);
    }

    public List<Long> obtenerAbogados(Long supervisorId)
            throws SQLException {
        return supervisionRepo.findAbogadosBySupervisor(supervisorId);
    }

    public List<User> listarAbogadosDisponiblesParaSupervisor(Long supervisorId)
            throws SQLException {
        return supervisionRepo.findAbogadosDisponiblesParaSupervisor(supervisorId);
    }
}
