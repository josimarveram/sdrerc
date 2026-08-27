package com.sdrerc.v3.web;

import com.sdrerc.v3.application.ExpedienteConsultaService;
import com.sdrerc.v3.application.ExpedienteTrazabilidadService;
import com.sdrerc.v3.domain.ExpedienteBandejaDTO;
import com.sdrerc.v3.domain.ExpedienteTimelineDTO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Equivalente web del módulo "Bandeja de Expedientes" (V2: {@code JPanelBandejaExpedientesNueva}
 * instanciada con {@code etapaInicial=null}, "perfil general" — no confundir con la instancia
 * "perfilRegistroRecepcion=true" que respalda `/api/registro/bandeja`, esa etapa fija a REGISTRO no
 * se toca). 24/08/2026, pedido explícito del usuario, reconstrucción completa (no port literal): V2
 * solo reutiliza el mismo panel de Registro sin bloquear etapa — acá se investigó además
 * {@code DlgConsolaExpedienteV2} (la "consola de expediente" real de V2, 1008 líneas, tabs
 * Detalles/Documentos/Documentos asociados/Timeline/Acciones disponibles) para diseñar una ficha de
 * expediente mucho más rica ("Expediente 360°"). Ver Javadoc de {@link ExpedienteTrazabilidadDAO}
 * para la subpestaña "Trazabilidad".
 *
 * <p>{@code /bandeja} reutiliza literalmente {@link ExpedienteConsultaService#buscarBandeja}, el
 * mismo método ya usado por {@code RegistroBandejaController} — único cambio real: {@code etapa} es
 * un parámetro real recibido del cliente (combo visible en la UI), no fijo a "REGISTRO". Solo
 * lectura, sin filtro de visibilidad por asignación (mismo criterio que Registro/Bandeja Asignación,
 * ver `CLAUDE.md` raíz sección Permisos: "pantallas de coordinación... deben ver toda la cola").</p>
 */
@RestController
@RequestMapping("/api/expedientes")
public class ExpedienteConsolaController {

    private final ExpedienteConsultaService expedienteConsultaService;
    private final ExpedienteTrazabilidadService expedienteTrazabilidadService;

    public ExpedienteConsolaController(
            ExpedienteConsultaService expedienteConsultaService,
            ExpedienteTrazabilidadService expedienteTrazabilidadService) {
        this.expedienteConsultaService = expedienteConsultaService;
        this.expedienteTrazabilidadService = expedienteTrazabilidadService;
    }

    @GetMapping("/bandeja")
    public List<ExpedienteBandejaDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String etapa,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer limite) throws SQLException {
        return expedienteConsultaService.buscarBandeja(texto, etapa, estado, desde, hasta, limite);
    }

    @GetMapping("/{idExpediente}/trazabilidad")
    public List<ExpedienteTimelineDTO> trazabilidad(@PathVariable Long idExpediente) throws SQLException {
        return expedienteTrazabilidadService.listarTrazabilidad(idExpediente);
    }
}
