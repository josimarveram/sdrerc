package com.sdrerc.v3.web;

import com.sdrerc.v3.application.UbigeoAppService;
import com.sdrerc.v3.domain.UbigeoItemDTO;
import java.sql.SQLException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Combos en cascada Departamento -> Provincia -> Distrito, usados en "Datos de notificación y ubicación". */
@RestController
@RequestMapping("/api/ubigeo")
public class UbigeoController {

    private final UbigeoAppService ubigeoAppService;

    public UbigeoController(UbigeoAppService ubigeoAppService) {
        this.ubigeoAppService = ubigeoAppService;
    }

    @GetMapping("/departamentos")
    public List<UbigeoItemDTO> departamentos() throws SQLException {
        return ubigeoAppService.listarDepartamentos();
    }

    @GetMapping("/provincias")
    public List<UbigeoItemDTO> provincias(@RequestParam Long idDepartamento) throws SQLException {
        return ubigeoAppService.listarProvincias(idDepartamento);
    }

    @GetMapping("/distritos")
    public List<UbigeoItemDTO> distritos(@RequestParam Long idProvincia) throws SQLException {
        return ubigeoAppService.listarDistritos(idProvincia);
    }
}
