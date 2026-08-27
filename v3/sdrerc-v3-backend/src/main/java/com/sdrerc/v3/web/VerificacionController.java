package com.sdrerc.v3.web;

import com.sdrerc.v3.application.VerificacionExpedienteService;
import com.sdrerc.v3.domain.DocumentoAnalizadoDTO;
import com.sdrerc.v3.domain.EquipoAsignacionDTO;
import com.sdrerc.v3.domain.UsuarioAsignableDTO;
import com.sdrerc.v3.domain.VerificacionExpedienteDTO;
import com.sdrerc.v3.domain.VerificacionRegistroDTO;
import com.sdrerc.v3.domain.VerificacionResultadoDTO;
import com.sdrerc.v3.web.dto.RecibirExpedienteRequest;
import com.sdrerc.v3.web.dto.VerificacionAprobarDestinoRequest;
import com.sdrerc.v3.web.dto.VerificacionObservarRequest;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Equivalente web de la Bandeja Verificación (JPanelVerificacionV2) — mismo alcance que
 * {@code VerificacionExpedienteService}/{@code VerificacionExpedienteDAO}, ver Javadoc del DAO
 * para el detalle completo de las 3 ramas de "Registrar Verificación" y qué código huérfano de V2
 * NO se portó.
 *
 * <p>Los endpoints de documentos ({@code /documentos*}) son passthroughs delgados hacia
 * {@code DocumentoAnalisisDAO} (el mismo ya construido para Análisis, misma tabla
 * {@code EXPEDIENTE_DOCUMENTO_ANALIZADO}) — se exponen bajo {@code /api/verificacion} en vez de
 * reutilizar literalmente {@code /api/analisis/documentos} por higiene de API (un desarrollador que
 * lea este controller debe encontrar aquí todo lo que la pantalla de Verificación necesita), no
 * porque sean datos distintos.</p>
 */
@RestController
@RequestMapping("/api/verificacion")
public class VerificacionController {

    private final VerificacionExpedienteService verificacionExpedienteService;

    public VerificacionController(VerificacionExpedienteService verificacionExpedienteService) {
        this.verificacionExpedienteService = verificacionExpedienteService;
    }

    @GetMapping("/bandeja")
    public List<VerificacionExpedienteDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer limite) throws SQLException {
        return verificacionExpedienteService.buscarExpedientes(texto, estado, desde, hasta, limite);
    }

    @PostMapping("/resultado/validar")
    public List<String> validar(@RequestBody VerificacionRegistroDTO registro) {
        return verificacionExpedienteService.validarRegistroVerificacion(registro);
    }

    /** "Registrar Verificación" (Observado) — solo guarda, no mueve el expediente de etapa. */
    @PostMapping("/observar")
    public VerificacionResultadoDTO observar(@RequestBody VerificacionObservarRequest request) throws SQLException {
        return verificacionExpedienteService.registrarObservado(request.idExpediente(), request.comentario(), request.descripcionObservacion());
    }

    /** "Registrar Verificación" (Aprobado) — solo guarda, no mueve el expediente de etapa (antes "aprobar-directo", enviaba directo a Ejecución). */
    @PostMapping("/registrar-aprobado")
    public VerificacionResultadoDTO registrarAprobado(@RequestBody RecibirExpedienteRequest request) throws SQLException {
        return verificacionExpedienteService.registrarAprobado(request.idExpediente(), request.comentario());
    }

    /** "Enviar a ..." (Destino operativo) — mueve el expediente ya registrado (Observado o Aprobado) al equipo elegido. */
    @PostMapping("/aprobar-destino")
    public VerificacionResultadoDTO aprobarDestino(@RequestBody VerificacionAprobarDestinoRequest request) throws SQLException {
        return verificacionExpedienteService.aprobarVerificacionConDestino(
                request.idExpediente(), request.comentario(), request.idEquipoDestino(), request.idUsuarioDestino());
    }

    @GetMapping("/equipos")
    public List<EquipoAsignacionDTO> listarEquiposDestino() throws SQLException {
        return verificacionExpedienteService.listarEquiposDestino();
    }

    @GetMapping("/usuarios")
    public List<UsuarioAsignableDTO> listarUsuariosDestino(@RequestParam Long idEquipo) throws SQLException {
        return verificacionExpedienteService.listarUsuariosDestino(idEquipo);
    }

    @GetMapping("/{idExpediente}/documentos")
    public List<DocumentoAnalizadoDTO> listarDocumentos(@PathVariable Long idExpediente) throws SQLException {
        return verificacionExpedienteService.listarDocumentosVerificados(idExpediente);
    }

    /** Ícono "Guardar" por fila — devuelve el id real (documento ya existente en Verificación, nunca uno nuevo). */
    @PostMapping("/documentos")
    public Long guardarDocumento(@RequestBody DocumentoAnalizadoDTO documento) throws SQLException {
        return verificacionExpedienteService.guardarDocumentoVerificado(documento.getIdExpediente(), documento);
    }
}
