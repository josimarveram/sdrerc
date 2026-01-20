/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.infrastructure.repository.SupervisionRepository;
import com.sdrerc.infrastructure.repository.UserRepository;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author David
 */
public class SupervisionService {
    //private final UserRepository userRepo;
    //private final SupervisionRepository supervisionRepo;

    private final SupervisionRepository supervisionRepo;
    
    public SupervisionService() {
        
        this.supervisionRepo = new SupervisionRepository();
    }
    
    
    public void asignarAbogados(Long supervisorId,
                                List<Long> abogados)
            throws SQLException {
        /*
        if (!userService.tieneRol(supervisorId, "SUPERVISOR")) {
            throw new BusinessException("El usuario no es Supervisor");
        }
        */
        
        List<Long> actuales =
        supervisionRepo.findAbogadosBySupervisor(supervisorId);

        // INSERTAR NUEVOS
        for (Long abogadoId : abogados) {
            /*
            if (!userService.tieneRol(abogadoId, "ABOGADO")) {
                throw new BusinessException(
                    "El usuario " + abogadoId + " no es Abogado"
                );
            }
            */

            if (!supervisionRepo.exists(supervisorId, abogadoId)) {
                supervisionRepo.insert(supervisorId, abogadoId);
            }
        }
        
        // ELIMINAR LOS QUITADOS
        for (Long abogadoId : actuales) {

            if (!abogados.contains(abogadoId)) {
                supervisionRepo.delete(supervisorId, abogadoId);
            }
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
}
