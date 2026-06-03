package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.EquipoMiembroDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableEquipoDTO;
import java.sql.SQLException;
import java.util.List;

public class EquipoMiembroService {

    private final EquipoJuridicoService equipoJuridicoService;

    public EquipoMiembroService() {
        this(new EquipoJuridicoService());
    }

    public EquipoMiembroService(EquipoJuridicoService equipoJuridicoService) {
        this.equipoJuridicoService = equipoJuridicoService;
    }

    public List<EquipoMiembroDTO> listarMiembros(Long idEquipo) throws SQLException {
        return equipoJuridicoService.listarMiembros(idEquipo);
    }

    public List<UsuarioAsignableEquipoDTO> listarUsuariosAsignables() throws SQLException {
        return equipoJuridicoService.listarUsuariosAsignables();
    }

    public void agregarMiembro(Long idEquipo, Long idUsuario) throws SQLException {
        equipoJuridicoService.agregarMiembro(idEquipo, idUsuario);
    }

    public void quitarMiembro(Long idEquipo, Long idUsuario) throws SQLException {
        equipoJuridicoService.quitarMiembro(idEquipo, idUsuario);
    }

    public void marcarResponsable(Long idEquipo, Long idUsuario) throws SQLException {
        equipoJuridicoService.marcarResponsable(idEquipo, idUsuario);
    }
}
