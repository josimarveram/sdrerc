package com.sdrerc.v3.web;

import com.sdrerc.v3.application.RegistroManualExpedienteService;
import com.sdrerc.v3.domain.RegistroManualExpedienteDTO;
import com.sdrerc.v3.domain.RegistroManualResultadoDTO;
import com.sdrerc.v3.web.dto.RegistroManualValidacionResponse;
import java.sql.SQLException;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Equivalente web del panel "Registro manual" de V2 (primer módulo de escritura de la Fase 2 del
 * plan de migración). Alcance de este incremento: solo el camino de alta de un expediente nuevo
 * (con su detección de duplicado por acta+titular/N° SGD y posible grupo familiar). La bandeja de
 * listado (Bandeja Registro), la carga diaria por Excel y la asociación de duplicados quedan para
 * incrementos siguientes de esta misma fase.
 */
@RestController
@RequestMapping("/api/registro")
public class RegistroManualController {

    private final RegistroManualExpedienteService registroManualExpedienteService;

    public RegistroManualController(RegistroManualExpedienteService registroManualExpedienteService) {
        this.registroManualExpedienteService = registroManualExpedienteService;
    }

    @PostMapping("/manual/validar")
    public RegistroManualValidacionResponse validar(@RequestBody RegistroManualExpedienteDTO registro) throws SQLException {
        List<String> mensajes = registroManualExpedienteService.validarConDuplicados(registro);
        return new RegistroManualValidacionResponse(mensajes, registro);
    }

    @PostMapping("/manual")
    public RegistroManualResultadoDTO registrar(@RequestBody RegistroManualExpedienteDTO registro) throws SQLException {
        return registroManualExpedienteService.registrar(registro);
    }
}
