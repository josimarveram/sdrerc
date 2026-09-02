package com.sdrerc.v3.web;

import com.sdrerc.v3.application.EjecucionExpedienteService;
import com.sdrerc.v3.domain.CatalogoItemDTO;
import com.sdrerc.v3.domain.DocumentoAnalizadoDTO;
import com.sdrerc.v3.domain.EjecucionExpedienteDTO;
import com.sdrerc.v3.domain.EjecucionResultadoDTO;
import com.sdrerc.v3.domain.EjecucionUltimoResultadoDTO;
import com.sdrerc.v3.domain.UsuarioAsignableDTO;
import com.sdrerc.v3.web.dto.DocumentoWordDTO;
import com.sdrerc.v3.web.dto.EjecucionEnviarSupervisionRequest;
import com.sdrerc.v3.web.dto.EjecucionObservacionRequest;
import com.sdrerc.v3.web.dto.EjecucionResultadoRequest;
import com.sdrerc.v3.web.dto.EjecucionReversionRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Equivalente web de la Bandeja Ejecución (JPanelEjecucionV2) — mismo alcance que
 * {@code EjecucionExpedienteService}/{@code EjecucionExpedienteDAO}, ver Javadoc del DAO para el
 * detalle completo de las 4 acciones reales y qué código huérfano de V2 SÍ se decidió exponer en
 * este incremento (a diferencia de Análisis/Verificación).
 *
 * <p>Los endpoints de documentos ({@code /documentos*}) son passthroughs delgados hacia
 * {@code DocumentoAnalisisDAO} (mismo patrón ya establecido en Análisis/Verificación).</p>
 */
@RestController
@RequestMapping("/api/ejecucion")
public class EjecucionController {

    private final EjecucionExpedienteService ejecucionExpedienteService;

    public EjecucionController(EjecucionExpedienteService ejecucionExpedienteService) {
        this.ejecucionExpedienteService = ejecucionExpedienteService;
    }

    @GetMapping("/bandeja")
    public List<EjecucionExpedienteDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer limite) throws SQLException {
        return ejecucionExpedienteService.buscarExpedientes(texto, estado, desde, hasta, limite);
    }

    /** "Registrar resultado de ejecución" — ejecuta SOLO INICIO_EJECUCION. */
    @PostMapping("/resultado")
    public EjecucionResultadoDTO registrarResultado(@RequestBody EjecucionResultadoRequest request) throws SQLException {
        return ejecucionExpedienteService.registrarResultado(request.idExpediente(), request.resultadoNombre(), request.comentario());
    }

    /**
     * Lectura de solo consulta del último resultado de ejecución ya registrado (01/09/2026, pedido
     * explícito del usuario) — permite que el panel lo muestre de forma persistente sin depender del
     * mensaje efímero de la última acción, que se pierde al remontar la subpestaña.
     */
    @GetMapping("/{idExpediente}/ultimo-resultado")
    public EjecucionUltimoResultadoDTO obtenerUltimoResultadoRegistrado(@PathVariable Long idExpediente) throws SQLException {
        return ejecucionExpedienteService.obtenerUltimoResultadoRegistrado(idExpediente);
    }

    /** "Enviar a Supervisión" — ejecuta SOLO DERIVACION_A_NOTIFICACION, validando carta final en despacho. */
    @PostMapping("/enviar-supervision")
    public EjecucionResultadoDTO enviarSupervision(@RequestBody EjecucionEnviarSupervisionRequest request) throws SQLException {
        return ejecucionExpedienteService.enviarSupervision(request.idExpediente(), request.comentario(), request.idUsuarioDestino());
    }

    @PostMapping("/observacion")
    public EjecucionResultadoDTO registrarObservacion(@RequestBody EjecucionObservacionRequest request) throws SQLException {
        return ejecucionExpedienteService.registrarObservacion(
                request.idExpediente(), request.tipoObservacionCodigo(), request.motivoCorreccionCodigo(), request.descripcion());
    }

    @PostMapping("/error-material")
    public EjecucionResultadoDTO registrarDocumentoInconsistente(@RequestBody EjecucionObservacionRequest request) throws SQLException {
        return ejecucionExpedienteService.registrarDocumentoInconsistente(
                request.idExpediente(), request.tipoObservacionCodigo(), request.motivoCorreccionCodigo(), request.descripcion());
    }

    @PostMapping("/devolver-analisis")
    public EjecucionResultadoDTO revertirAnalisis(@RequestBody EjecucionReversionRequest request) throws SQLException {
        return ejecucionExpedienteService.revertirAnalisis(
                request.idExpediente(), request.tipoObservacionCodigo(), request.motivoCorreccionCodigo(), request.motivoReversion(), request.comentario());
    }

    @GetMapping("/usuarios-supervision")
    public List<UsuarioAsignableDTO> listarUsuariosSupervision() throws SQLException {
        return ejecucionExpedienteService.listarUsuariosSupervision();
    }

    @GetMapping("/catalogos/resultados")
    public List<CatalogoItemDTO> listarResultados() throws SQLException {
        return ejecucionExpedienteService.listarResultadosEjecucion();
    }

    @GetMapping("/catalogos/tipos-observacion")
    public List<CatalogoItemDTO> listarTiposObservacion() throws SQLException {
        return ejecucionExpedienteService.listarTiposObservacion();
    }

    @GetMapping("/catalogos/motivos-correccion")
    public List<CatalogoItemDTO> listarMotivosCorreccion() throws SQLException {
        return ejecucionExpedienteService.listarMotivosCorreccion();
    }

    @GetMapping("/{idExpediente}/documentos")
    public List<DocumentoAnalizadoDTO> listarDocumentos(@PathVariable Long idExpediente) throws SQLException {
        return ejecucionExpedienteService.listarDocumentosEjecucion(idExpediente);
    }

    /** Ícono "Guardar" por fila — devuelve el id real. */
    @PostMapping("/documentos")
    public Long guardarDocumento(@RequestBody DocumentoAnalizadoDTO documento) throws SQLException {
        return ejecucionExpedienteService.guardarDocumentoEjecucion(documento.getIdExpediente(), documento);
    }

    /**
     * Ícono "Eliminar" por fila (21/08/2026, pedido explícito del usuario: "3 primeros iconos...
     * similar como en la grilla de documentos analizados") — baja lógica, mismo endpoint que Análisis.
     */
    @DeleteMapping("/documentos/{idExpediente}/{idDocumentoAnalizado}")
    public void eliminarDocumento(@PathVariable Long idExpediente, @PathVariable Long idDocumentoAnalizado) throws SQLException {
        ejecucionExpedienteService.eliminarDocumentoEjecucion(idExpediente, idDocumentoAnalizado);
    }

    /** Descarga Word por fila — mismo mecanismo que Análisis. */
    @GetMapping("/{idExpediente}/documentos/{idDocumentoAnalizado}/word")
    public ResponseEntity<byte[]> descargarWord(
            @PathVariable Long idExpediente, @PathVariable Long idDocumentoAnalizado) throws SQLException, IOException {
        DocumentoWordDTO documento = ejecucionExpedienteService.generarDocumentoWordEjecucion(idExpediente, idDocumentoAnalizado);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(documento.nombreArchivo()).build().toString())
                .body(documento.contenido());
    }
}
