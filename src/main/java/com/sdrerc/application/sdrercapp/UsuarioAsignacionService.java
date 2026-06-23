package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO;
import com.sdrerc.domain.dto.sdrercapp.CargaLaboralAbogadoDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.EquipoAsignacionDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.UsuarioAsignacionDAO;
import java.sql.SQLException;
import java.util.List;

public class UsuarioAsignacionService {

    private final EquipoAsignacionDAO equipoAsignacionDAO;
    private final UsuarioAsignacionDAO usuarioAsignacionDAO;

    public UsuarioAsignacionService() {
        this(new EquipoAsignacionDAO(), new UsuarioAsignacionDAO());
    }

    public UsuarioAsignacionService(EquipoAsignacionDAO equipoAsignacionDAO, UsuarioAsignacionDAO usuarioAsignacionDAO) {
        this.equipoAsignacionDAO = equipoAsignacionDAO;
        this.usuarioAsignacionDAO = usuarioAsignacionDAO;
    }

    public List<EquipoAsignacionDTO> listarEquiposActivos() throws SQLException {
        return equipoAsignacionDAO.listarEquiposActivos();
    }

    public List<UsuarioAsignableDTO> listarAbogadosAsignables(Long idEquipo) throws SQLException {
        return usuarioAsignacionDAO.listarAbogadosAsignables(idEquipo);
    }

    public List<CargaLaboralAbogadoDTO> listarCargaLaboralAbogados(Long idEquipo) throws SQLException {
        return usuarioAsignacionDAO.listarCargaLaboralAbogados(idEquipo);
    }

    public Long obtenerIdUsuarioActivoPorUsername(String username) throws SQLException {
        return usuarioAsignacionDAO.obtenerIdUsuarioActivoPorUsername(username);
    }
}
