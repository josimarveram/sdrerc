package com.sdrerc.v3.web;

import com.sdrerc.v3.application.AnalisisExpedienteService;
import com.sdrerc.v3.domain.AnalisisDetalleDTO;
import com.sdrerc.v3.domain.AnalisisExpedienteDTO;
import com.sdrerc.v3.domain.AnalisisRegistroDTO;
import com.sdrerc.v3.domain.AnalisisResultadoDTO;
import com.sdrerc.v3.domain.DocumentoAnalizadoDTO;
import com.sdrerc.v3.web.dto.DocumentoWordDTO;
import com.sdrerc.v3.web.dto.RecibirAsociadoRequest;
import com.sdrerc.v3.web.dto.RecibirExpedienteRequest;
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
 * Equivalente web de la pestaña "Bandeja Análisis" (JPanelAnalisisV2, lengueta principal) — mismo
 * alcance que {@code AnalisisExpedienteService}/{@code AnalisisExpedienteDAO} (20/08/2026, primer
 * incremento del módulo, ver Javadoc del DAO para qué queda deliberadamente fuera de este
 * incremento: registrar resultado final, enviar a verificación, documentos analizados, Word).
 */
@RestController
@RequestMapping("/api/analisis")
public class AnalisisController {

    private final AnalisisExpedienteService analisisExpedienteService;

    public AnalisisController(AnalisisExpedienteService analisisExpedienteService) {
        this.analisisExpedienteService = analisisExpedienteService;
    }

    @GetMapping("/bandeja")
    public List<AnalisisExpedienteDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer limite) throws SQLException {
        return analisisExpedienteService.buscarExpedientes(texto, estado, desde, hasta, limite);
    }

    @PostMapping("/recibir")
    public AnalisisResultadoDTO recibir(@RequestBody RecibirExpedienteRequest request) throws SQLException {
        return analisisExpedienteService.recibirExpediente(request.idExpediente(), request.comentario());
    }

    /** "Recibir" en la mini-grilla de "Solicitudes asociadas" de la subpestaña Análisis. */
    @PostMapping("/recibir-asociado")
    public AnalisisResultadoDTO recibirAsociado(@RequestBody RecibirAsociadoRequest request) throws SQLException {
        return analisisExpedienteService.recibirDocumentoAsociado(
                request.idExpedientePrincipal(), request.idExpedienteAsociado(), request.comentario());
    }

    @GetMapping("/{idExpediente}/resultado")
    public AnalisisDetalleDTO obtenerResultado(@PathVariable Long idExpediente) throws SQLException {
        return analisisExpedienteService.obtenerAnalisisRegistrado(idExpediente);
    }

    @PostMapping("/resultado/validar")
    public List<String> validarResultado(@RequestBody AnalisisRegistroDTO registro) {
        return analisisExpedienteService.validarRegistroAnalisis(registro);
    }

    /** "Registrar resultado final" — validación previa vía {@code /resultado/validar} antes de confirmar. */
    @PostMapping("/resultado")
    public AnalisisResultadoDTO registrarResultado(@RequestBody AnalisisRegistroDTO registro) throws SQLException {
        return analisisExpedienteService.registrarAnalisis(registro);
    }

    @PostMapping("/verificacion")
    public AnalisisResultadoDTO enviarVerificacion(@RequestBody RecibirExpedienteRequest request) throws SQLException {
        return analisisExpedienteService.enviarVerificacion(request.idExpediente(), request.comentario());
    }

    /** Grilla "Documentos analizados" (maestro-detalle) de la subpestaña Análisis. */
    @GetMapping("/{idExpediente}/documentos")
    public List<DocumentoAnalizadoDTO> listarDocumentos(@PathVariable Long idExpediente) throws SQLException {
        return analisisExpedienteService.listarDocumentosAnalizados(idExpediente);
    }

    /**
     * Ícono "Guardar" por fila — {@code documento.idExpediente} ya viene en el cuerpo. Devuelve el
     * id real (resuelve el id temporal negativo de una fila nueva, ver
     * {@code DocumentoAnalisisDAO#guardarDocumentoAnalizado}).
     */
    @PostMapping("/documentos")
    public Long guardarDocumento(@RequestBody DocumentoAnalizadoDTO documento) throws SQLException {
        return analisisExpedienteService.guardarDocumentoAnalizado(documento.getIdExpediente(), documento);
    }

    /** Ícono "Eliminar" por fila — baja lógica, ver {@code CLAUDE.md} raíz: "No eliminar físicamente". */
    @DeleteMapping("/documentos/{idExpediente}/{idDocumentoAnalizado}")
    public void eliminarDocumento(@PathVariable Long idExpediente, @PathVariable Long idDocumentoAnalizado) throws SQLException {
        analisisExpedienteService.darBajaDocumentoAnalizado(idExpediente, idDocumentoAnalizado);
    }

    /** Descarga Word por fila — alcance simplificado, ver Javadoc de {@code AnalisisPlantillaDocumentoService}. */
    @GetMapping("/{idExpediente}/documentos/{idDocumentoAnalizado}/word")
    public ResponseEntity<byte[]> descargarWord(
            @PathVariable Long idExpediente, @PathVariable Long idDocumentoAnalizado) throws SQLException, IOException {
        DocumentoWordDTO documento = analisisExpedienteService.generarDocumentoWord(idExpediente, idDocumentoAnalizado);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(documento.nombreArchivo()).build().toString())
                .body(documento.contenido());
    }
}
