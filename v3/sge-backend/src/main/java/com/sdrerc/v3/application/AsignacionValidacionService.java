package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.EquipoAsignacionDTO;
import com.sdrerc.v3.domain.UsuarioAsignableDTO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/** Port literal de com.sdrerc.application.sdrercapp.AsignacionValidacionService (V2), sin BD. */
@Service
public class AsignacionValidacionService {

    public List<String> validarAsignacion(List<Long> idsExpediente, EquipoAsignacionDTO equipo, UsuarioAsignableDTO abogado) {
        List<String> errores = new ArrayList<>();
        if (idsExpediente == null || idsExpediente.isEmpty()) {
            errores.add("Seleccione al menos un expediente para asignar.");
        }
        if (equipo == null || equipo.getIdEquipo() == null) {
            errores.add("Seleccione el equipo destino.");
        }
        if (abogado == null || abogado.getIdUsuario() == null) {
            errores.add("Seleccione el abogado responsable.");
        }
        if (equipo != null && abogado != null
                && equipo.getIdEquipo() != null
                && abogado.getIdEquipo() != null
                && !equipo.getIdEquipo().equals(abogado.getIdEquipo())) {
            errores.add("El abogado responsable debe pertenecer al equipo destino.");
        }
        return errores;
    }
}
