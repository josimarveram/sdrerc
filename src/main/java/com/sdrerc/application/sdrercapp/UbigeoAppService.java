package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.UbigeoItemDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.UbigeoAppDAO;
import java.sql.SQLException;
import java.util.List;

public class UbigeoAppService {

    private final UbigeoAppDAO dao;

    public UbigeoAppService() {
        this(new UbigeoAppDAO());
    }

    public UbigeoAppService(UbigeoAppDAO dao) {
        this.dao = dao;
    }

    public List<UbigeoItemDTO> listarDepartamentos() throws SQLException {
        return dao.listarDepartamentos();
    }

    public List<UbigeoItemDTO> listarProvincias(Long idDepartamento) throws SQLException {
        return dao.listarProvincias(idDepartamento);
    }

    public List<UbigeoItemDTO> listarDistritos(Long idProvincia) throws SQLException {
        return dao.listarDistritos(idProvincia);
    }
}
