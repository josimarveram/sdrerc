package com.sdrerc.v3.web;

import com.sdrerc.v3.application.GrupoFamiliarService;
import com.sdrerc.v3.domain.GrupoFamiliarCandidatoDTO;
import com.sdrerc.v3.domain.GrupoFamiliarIntegranteDTO;
import com.sdrerc.v3.domain.GrupoFamiliarResultadoDTO;
import com.sdrerc.v3.web.dto.AsociarGrupoFamiliarRequest;
import java.sql.SQLException;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Equivalente web del panel "Grupo Familiar" (Registro: {@code JPanelRegistrarGrupoFamiliarV2};
 * Asignación: lengueta homónima, mismo patrón). Controller único y compartido por ambos módulos —
 * mismo criterio que {@code ExpedienteRelacionadoController}/"Asociar": un solo recurso REST, un
 * solo componente React ({@code GrupoFamiliarPanel.tsx}) importado desde ambas bandejas.
 *
 * <p>Alcance de este incremento: candidatos por heurística de apellidos
 * ({@link #listarPosiblesIntegrantes}) + búsqueda manual de respaldo
 * ({@link #buscarPosiblesIntegrantesManual}), confirmar asociación ({@link #asociar}), listar el
 * grupo ya confirmado ({@link #listarIntegrantes}) y retirar un integrante ({@link #eliminar},
 * botón "x" en V2). Queda fuera: todo lo atado a la baja lógica de un registro (limpieza de
 * alertas remanentes al eliminar un expediente), funcionalidad todavía no construida en V3.</p>
 */
@RestController
@RequestMapping("/api/grupo-familiar")
public class GrupoFamiliarController {

    private final GrupoFamiliarService grupoFamiliarService;

    public GrupoFamiliarController(GrupoFamiliarService grupoFamiliarService) {
        this.grupoFamiliarService = grupoFamiliarService;
    }

    @GetMapping("/{idExpediente}/candidatos")
    public List<GrupoFamiliarCandidatoDTO> listarPosiblesIntegrantes(@PathVariable Long idExpediente) throws SQLException {
        return grupoFamiliarService.listarPosiblesIntegrantes(idExpediente);
    }

    @GetMapping("/{idExpediente}/candidatos/buscar")
    public List<GrupoFamiliarCandidatoDTO> buscarPosiblesIntegrantesManual(
            @PathVariable Long idExpediente,
            @RequestParam String texto) throws SQLException {
        return grupoFamiliarService.buscarPosiblesIntegrantesManual(idExpediente, texto);
    }

    @GetMapping("/{idExpediente}/integrantes")
    public List<GrupoFamiliarIntegranteDTO> listarIntegrantes(@PathVariable Long idExpediente) throws SQLException {
        return grupoFamiliarService.listarIntegrantesGrupoFamiliar(idExpediente);
    }

    @PostMapping("/asociar")
    public GrupoFamiliarResultadoDTO asociar(@RequestBody AsociarGrupoFamiliarRequest request) throws SQLException {
        return grupoFamiliarService.asociarGrupoFamiliar(request.idExpedientePrincipal(), request.idsExpedientesCandidatos());
    }

    @DeleteMapping("/{idExpediente}")
    public GrupoFamiliarResultadoDTO eliminar(@PathVariable Long idExpediente) throws SQLException {
        return grupoFamiliarService.eliminarDeGrupoFamiliar(idExpediente);
    }
}
