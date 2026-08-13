package com.sdrerc.v3.web;

import com.sdrerc.v3.application.ExpedienteRelacionadoService;
import com.sdrerc.v3.domain.ExpedienteRelacionResultadoDTO;
import com.sdrerc.v3.domain.ExpedienteRelacionadoDTO;
import com.sdrerc.v3.web.dto.AsociarRelacionadosRequest;
import java.sql.SQLException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Equivalente web del panel "Asociar" (Registro: lengueta "Asociar duplicados"; Asignación:
 * lengueta "Asociar"). Controller único y compartido por ambos módulos — ver CLAUDE.md raíz,
 * sección Registro: en V2 ambos ya reutilizan el mismo {@code ExpedienteRelacionadoService}, así
 * que en V3 se expone un solo recurso REST en vez de duplicar el endpoint por módulo. El
 * componente React que lo consume ({@code AsociarDuplicadosPanel.tsx}) también es uno solo,
 * importado tanto por la Bandeja Registro como por la Bandeja Asignación.
 *
 * <p>Alcance de este incremento: listar candidatos + expediente principal
 * ({@link #listarSolicitudesDelGrupo}), confirmar asociación ({@link #asociar}, cubre tanto
 * "Asociación rápida" como el flujo manual con checkboxes — misma llamada, el frontend decide qué
 * ids envía) y listar ya confirmados ({@link #listarConfirmados}). Queda fuera: desasociar (quitar
 * una asociación ya confirmada) y "Decisión de número" (generar número de expediente), ambos
 * diferidos a un incremento posterior.</p>
 */
@RestController
@RequestMapping("/api/expedientes-relacionados")
public class ExpedienteRelacionadoController {

    private final ExpedienteRelacionadoService expedienteRelacionadoService;

    public ExpedienteRelacionadoController(ExpedienteRelacionadoService expedienteRelacionadoService) {
        this.expedienteRelacionadoService = expedienteRelacionadoService;
    }

    @GetMapping("/{idExpediente}/solicitudes-grupo")
    public List<ExpedienteRelacionadoDTO> listarSolicitudesDelGrupo(@PathVariable Long idExpediente) throws SQLException {
        return expedienteRelacionadoService.listarSolicitudesDelGrupo(idExpediente);
    }

    @GetMapping("/{idExpediente}/confirmados")
    public List<ExpedienteRelacionadoDTO> listarConfirmados(@PathVariable Long idExpediente) throws SQLException {
        return expedienteRelacionadoService.listarAsociadosConfirmados(idExpediente);
    }

    @PostMapping("/asociar")
    public ExpedienteRelacionResultadoDTO asociar(@RequestBody AsociarRelacionadosRequest request) throws SQLException {
        return expedienteRelacionadoService.asociarRelacionados(
                request.idExpedientePrincipal(),
                request.idsRelacionados(),
                request.descripcion());
    }
}
