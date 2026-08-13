package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.UbigeoItemDTO;
import com.sdrerc.v3.infrastructure.dao.UbigeoAppDAO;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

/** Port literal de com.sdrerc.application.sdrercapp.UbigeoAppService (V2). */
@Service
public class UbigeoAppService {

    private final UbigeoAppDAO dao;

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
