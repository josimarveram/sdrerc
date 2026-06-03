package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.RolDTO;
import com.sdrerc.domain.dto.sdrercapp.RolFiltroDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.RolDAO;
import java.sql.SQLException;
import java.util.List;

public class UsuarioRolService {

    private final RolDAO rolDAO;

    public UsuarioRolService() {
        this(new RolDAO());
    }

    public UsuarioRolService(RolDAO rolDAO) {
        this.rolDAO = rolDAO;
    }

    public List<RolDTO> listarRolesActivos() throws SQLException {
        return rolDAO.buscar(new RolFiltroDTO(null, Boolean.TRUE, 1000));
    }
}
