package com.sdrerc.v3.web;

import com.sdrerc.v3.application.ExpedienteEdicionManualService;
import com.sdrerc.v3.domain.ExpedienteEdicionManualDTO;
import com.sdrerc.v3.domain.RegistroManualResultadoDTO;
import com.sdrerc.v3.web.dto.ExpedienteEdicionManualValidacionResponse;
import java.sql.SQLException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Equivalente web de "Edición manual" (V2: JPanelRegistroManualRecepcionV2 en modo edición, botón
 * "Editar" de la Bandeja Registro). Alcance: solo edición desde Registro/Recepción (expediente en
 * etapa REGISTRO/estado REGISTRADO, sin asignación activa) — los puntos de entrada desde Análisis y
 * Asignación de V2 (con reglas de validación distintas) quedan fuera porque esos módulos no existen
 * todavía en V3. Tampoco incluye eliminar (baja lógica) — ver v3/CLAUDE.md.
 */
@RestController
@RequestMapping("/api/registro/edicion")
public class RegistroEdicionManualController {

    private final ExpedienteEdicionManualService expedienteEdicionManualService;

    public RegistroEdicionManualController(ExpedienteEdicionManualService expedienteEdicionManualService) {
        this.expedienteEdicionManualService = expedienteEdicionManualService;
    }

    @GetMapping("/{idExpediente}")
    public ExpedienteEdicionManualDTO obtenerParaEdicion(@PathVariable Long idExpediente) throws SQLException {
        return expedienteEdicionManualService.obtenerParaEdicion(idExpediente);
    }

    @PostMapping("/validar")
    public ExpedienteEdicionManualValidacionResponse validar(@RequestBody ExpedienteEdicionManualDTO registro) throws SQLException {
        List<String> mensajes = expedienteEdicionManualService.validar(registro);
        return new ExpedienteEdicionManualValidacionResponse(mensajes, registro);
    }

    @PutMapping
    public RegistroManualResultadoDTO guardar(@RequestBody ExpedienteEdicionManualDTO registro) throws SQLException {
        return expedienteEdicionManualService.guardar(registro);
    }
}
