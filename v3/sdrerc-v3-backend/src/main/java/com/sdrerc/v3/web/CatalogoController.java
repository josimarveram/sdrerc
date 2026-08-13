package com.sdrerc.v3.web;

import com.sdrerc.v3.application.CatalogoLookupService;
import com.sdrerc.v3.domain.CatalogoItemDTO;
import java.sql.SQLException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Catálogos para combos del formulario de Registro manual (canal, procedimiento, tipo de documento, tipo de acta). */
@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    private final CatalogoLookupService catalogoLookupService;

    public CatalogoController(CatalogoLookupService catalogoLookupService) {
        this.catalogoLookupService = catalogoLookupService;
    }

    @GetMapping("/canales-recepcion")
    public List<CatalogoItemDTO> canalesRecepcion() throws SQLException {
        return catalogoLookupService.listarCanalesRecepcion();
    }

    @GetMapping("/procedimientos-registrales")
    public List<CatalogoItemDTO> procedimientosRegistrales() throws SQLException {
        return catalogoLookupService.listarProcedimientosRegistrales();
    }

    @GetMapping("/tipos-documento")
    public List<CatalogoItemDTO> tiposDocumento() throws SQLException {
        return catalogoLookupService.listarTiposDocumento();
    }

    @GetMapping("/tipos-acta")
    public List<CatalogoItemDTO> tiposActa() throws SQLException {
        return catalogoLookupService.listarTiposActa();
    }
}
