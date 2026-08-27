package com.sdrerc.v3.web;

import com.sdrerc.v3.application.ExpedienteEdicionManualService;
import com.sdrerc.v3.domain.ExpedienteEdicionManualDTO;
import com.sdrerc.v3.domain.ImpactoEliminacionRegistroDTO;
import com.sdrerc.v3.domain.RegistroManualResultadoDTO;
import com.sdrerc.v3.web.dto.ExpedienteEdicionManualValidacionResponse;
import java.sql.SQLException;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Equivalente web de "Edición manual" (V2: JPanelRegistroManualRecepcionV2 en modo edición, botón
 * "Editar" de la Bandeja Registro). Alcance: edición/eliminación desde Registro/Recepción
 * (expediente en etapa REGISTRO/estado REGISTRADO, sin asignación activa) MÁS el camino de edición
 * desde Análisis ({@code desdeAnalisis=true} en {@code GET}/{@code PUT}, agregado 21/08/2026, pedido
 * explícito del usuario, ver Javadoc de {@code ExpedienteEdicionManualDAO}) — el punto de entrada
 * equivalente desde Asignación (con sus propias reglas distintas en V2) sigue sin portar, ese módulo
 * no expone hoy un botón "Editar" en V3. {@code DELETE} (baja lógica) replica la limpieza en cascada
 * de alertas de solicitudes relacionadas que hace V2 — ver Javadoc de
 * {@code ExpedienteEdicionManualDAO.eliminar} para el detalle exacto.
 */
@RestController
@RequestMapping("/api/registro/edicion")
public class RegistroEdicionManualController {

    private final ExpedienteEdicionManualService expedienteEdicionManualService;

    public RegistroEdicionManualController(ExpedienteEdicionManualService expedienteEdicionManualService) {
        this.expedienteEdicionManualService = expedienteEdicionManualService;
    }

    @GetMapping("/{idExpediente}")
    public ExpedienteEdicionManualDTO obtenerParaEdicion(
            @PathVariable Long idExpediente,
            @RequestParam(required = false, defaultValue = "false") boolean desdeAnalisis) throws SQLException {
        return expedienteEdicionManualService.obtenerParaEdicion(idExpediente, desdeAnalisis);
    }

    @PostMapping("/validar")
    public ExpedienteEdicionManualValidacionResponse validar(@RequestBody ExpedienteEdicionManualDTO registro) throws SQLException {
        List<String> mensajes = expedienteEdicionManualService.validar(registro);
        return new ExpedienteEdicionManualValidacionResponse(mensajes, registro);
    }

    @PutMapping
    public RegistroManualResultadoDTO guardar(
            @RequestBody ExpedienteEdicionManualDTO registro,
            @RequestParam(required = false, defaultValue = "false") boolean desdeAnalisis) throws SQLException {
        return expedienteEdicionManualService.guardar(registro, desdeAnalisis);
    }

    @DeleteMapping("/{idExpediente}")
    public RegistroManualResultadoDTO eliminar(@PathVariable Long idExpediente) throws SQLException {
        return expedienteEdicionManualService.eliminar(idExpediente);
    }

    @GetMapping("/{idExpediente}/impacto-eliminacion")
    public ImpactoEliminacionRegistroDTO obtenerImpactoEliminacion(@PathVariable Long idExpediente) throws SQLException {
        return expedienteEdicionManualService.obtenerImpactoEliminacion(idExpediente);
    }
}
