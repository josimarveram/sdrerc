package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.CatalogoItemDTO;
import com.sdrerc.v3.domain.DocumentoAnalizadoDTO;
import com.sdrerc.v3.domain.EjecucionExpedienteDTO;
import com.sdrerc.v3.domain.EjecucionResultadoDTO;
import com.sdrerc.v3.domain.EjecucionUltimoResultadoDTO;
import com.sdrerc.v3.domain.ExpedienteEdicionManualDTO;
import com.sdrerc.v3.domain.UsuarioAsignableDTO;
import com.sdrerc.v3.infrastructure.dao.AnalisisExpedienteDAO;
import com.sdrerc.v3.infrastructure.dao.CatalogoLookupDAO;
import com.sdrerc.v3.infrastructure.dao.DocumentoAnalisisDAO;
import com.sdrerc.v3.infrastructure.dao.EjecucionExpedienteDAO;
import com.sdrerc.v3.infrastructure.dao.EquipoAsignacionDAO;
import com.sdrerc.v3.infrastructure.dao.UsuarioAsignacionDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import com.sdrerc.v3.web.dto.DocumentoWordDTO;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Port acotado de com.sdrerc.application.sdrercapp.EjecucionExpedienteService (V2) — mismo alcance
 * que {@link EjecucionExpedienteDAO} (ver su Javadoc para el detalle completo de las 4 acciones
 * reales: registrar resultado, enviar a supervisión, observación/error material, devolver a
 * análisis). Visibilidad por asignación SIN abogados supervisados (a diferencia de Verificación).
 */
@Service
public class EjecucionExpedienteService {

    /** Único equipo real que V2 acepta como destino de "Enviar a Supervisión" (esEquipoDestinoEjecucionValido). */
    private static final String EQUIPO_SUPERVISION = "EQ_SUPERVISION";

    /**
     * Las cartas finales de notificación (mismos códigos que {@code TIPOS_CARTA_NOTIFICACION} en
     * `EjecucionContent.tsx`) — únicos tipos que pueden eliminarse desde Ejecución (21/08/2026,
     * pedido explícito del usuario: "no me debería permitir eliminar documentos que vienen de
     * análisis o verificación o de otras áreas"). Validado acá (no solo en el frontend) porque la
     * baja lógica es una escritura real contra la BD compartida con V2.
     *
     * <p>28/08/2026: ampliado de 3 a las 6 cartas finales reales del catálogo {@code EJECUCION_DOC_*}
     * (todas con {@code clasificacion=FINAL}) — antes solo Improcedente/Procedente/Procedente en
     * Parte; faltaban Abandono/Desistimiento/Infundado. Mismos 6 códigos que
     * {@code CatalogoLookupDAO#listarTiposDocumentoAdjuntoEjecucion} ya devuelve.</p>
     */
    private static final Set<String> TIPOS_CARTA_NOTIFICACION = Set.of(
            "EJECUCION_DOC_01_CARTA_ABANDONO",
            "EJECUCION_DOC_17_CARTA_IMPROCEDENTE",
            "EJECUCION_DOC_18_CARTA_PROCEDENTE",
            "EJECUCION_DOC_19_CARTA_PROCEDENTE_EN_PARTE",
            "EJECUCION_DOC_22_CARTA_DESISTIMIENTO",
            "EJECUCION_DOC_23_CARTA_INFUNDADO");

    private final EjecucionExpedienteDAO ejecucionExpedienteDAO;
    private final CatalogoLookupDAO catalogoLookupDAO;
    private final DocumentoAnalisisDAO documentoAnalisisDAO;
    private final EquipoAsignacionDAO equipoAsignacionDAO;
    private final UsuarioAsignacionDAO usuarioAsignacionDAO;
    private final AnalisisExpedienteDAO analisisExpedienteDAO;
    private final ExpedienteEdicionManualService expedienteEdicionManualService;
    private final AnalisisPlantillaDocumentoService analisisPlantillaDocumentoService;

    public EjecucionExpedienteService(
            EjecucionExpedienteDAO ejecucionExpedienteDAO,
            CatalogoLookupDAO catalogoLookupDAO,
            DocumentoAnalisisDAO documentoAnalisisDAO,
            EquipoAsignacionDAO equipoAsignacionDAO,
            UsuarioAsignacionDAO usuarioAsignacionDAO,
            AnalisisExpedienteDAO analisisExpedienteDAO,
            ExpedienteEdicionManualService expedienteEdicionManualService,
            AnalisisPlantillaDocumentoService analisisPlantillaDocumentoService) {
        this.ejecucionExpedienteDAO = ejecucionExpedienteDAO;
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.documentoAnalisisDAO = documentoAnalisisDAO;
        this.equipoAsignacionDAO = equipoAsignacionDAO;
        this.usuarioAsignacionDAO = usuarioAsignacionDAO;
        this.analisisExpedienteDAO = analisisExpedienteDAO;
        this.expedienteEdicionManualService = expedienteEdicionManualService;
        this.analisisPlantillaDocumentoService = analisisPlantillaDocumentoService;
    }

    public List<EjecucionExpedienteDTO> buscarExpedientes(
            String textoLibre, String estadoCodigo, LocalDate fechaSolicitudDesde, LocalDate fechaSolicitudHasta, Integer limite) throws SQLException {
        var usuario = CurrentUser.get();
        boolean esAdmin = usuario.hasRole("ADMIN_SISTEMA");
        List<Long> idsEquipoActual = esAdmin ? List.of() : catalogoLookupDAO.listarIdsEquipoDeUsuario(usuario.idUsuario());
        return ejecucionExpedienteDAO.buscarExpedientes(
                textoLibre, estadoCodigo, fechaSolicitudDesde, fechaSolicitudHasta, esAdmin, usuario.idUsuario(), idsEquipoActual, limite);
    }

    /** "Registrar resultado de ejecución" — ejecuta SOLO INICIO_EJECUCION. */
    public EjecucionResultadoDTO registrarResultado(Long idExpediente, String resultadoNombre, String comentario) throws SQLException {
        return ejecucionExpedienteDAO.registrarResultado(idExpediente, resultadoNombre, comentario, CurrentUser.get().idUsuario());
    }

    /**
     * Lectura de solo consulta del último resultado de ejecución ya registrado (01/09/2026, pedido
     * explícito del usuario: que el panel muestre de forma persistente qué resultado quedó
     * registrado, aunque la subpestaña se haya desmontado/remontado por un cambio de tab).
     */
    public EjecucionUltimoResultadoDTO obtenerUltimoResultadoRegistrado(Long idExpediente) throws SQLException {
        return ejecucionExpedienteDAO.obtenerUltimoResultadoRegistrado(idExpediente);
    }

    /** "Enviar a Supervisión" — resuelve internamente el id de EQ_SUPERVISION, sin combo de equipo en el frontend. */
    public EjecucionResultadoDTO enviarSupervision(Long idExpediente, String comentario, Long idUsuarioDestino) throws SQLException {
        Long idEquipoSupervision = resolverIdEquipoSupervision();
        return ejecucionExpedienteDAO.enviarSupervision(idExpediente, comentario, idEquipoSupervision, idUsuarioDestino, CurrentUser.get().idUsuario());
    }

    public EjecucionResultadoDTO registrarObservacion(Long idExpediente, String tipoObservacionCodigo, String motivoCorreccionCodigo, String descripcion)
            throws SQLException {
        return ejecucionExpedienteDAO.registrarObservacion(idExpediente, tipoObservacionCodigo, motivoCorreccionCodigo, descripcion, CurrentUser.get().idUsuario());
    }

    public EjecucionResultadoDTO registrarDocumentoInconsistente(
            Long idExpediente, String tipoObservacionCodigo, String motivoCorreccionCodigo, String descripcion) throws SQLException {
        return ejecucionExpedienteDAO.registrarDocumentoInconsistente(
                idExpediente, tipoObservacionCodigo, motivoCorreccionCodigo, descripcion, CurrentUser.get().idUsuario());
    }

    public EjecucionResultadoDTO revertirAnalisis(
            Long idExpediente, String tipoObservacionCodigo, String motivoCorreccionCodigo, String motivoReversion, String comentario) throws SQLException {
        return ejecucionExpedienteDAO.revertirAnalisis(
                idExpediente, tipoObservacionCodigo, motivoCorreccionCodigo, motivoReversion, comentario, CurrentUser.get().idUsuario());
    }

    /** Combo "Usuario destino" de "Enviar a Supervisión" — mismos usuarios activos de EQ_SUPERVISION, sin restricción de rol. */
    public List<UsuarioAsignableDTO> listarUsuariosSupervision() throws SQLException {
        Long idEquipoSupervision = resolverIdEquipoSupervision();
        if (idEquipoSupervision == null) {
            return List.of();
        }
        return usuarioAsignacionDAO.listarUsuariosAsignablesPorEquipo(idEquipoSupervision);
    }

    public List<CatalogoItemDTO> listarResultadosEjecucion() throws SQLException {
        return catalogoLookupDAO.listarResultadosEjecucion();
    }

    public List<CatalogoItemDTO> listarTiposObservacion() throws SQLException {
        return catalogoLookupDAO.listarTiposObservacion();
    }

    public List<CatalogoItemDTO> listarMotivosCorreccion() throws SQLException {
        return catalogoLookupDAO.listarMotivosCorreccion();
    }

    /** Grilla "Documentos ejecutados" — reutiliza literalmente DocumentoAnalisisDAO (misma tabla que Análisis/Verificación). */
    public List<DocumentoAnalizadoDTO> listarDocumentosEjecucion(Long idExpediente) throws SQLException {
        return documentoAnalisisDAO.listarPorExpediente(idExpediente);
    }

    public Long guardarDocumentoEjecucion(Long idExpediente, DocumentoAnalizadoDTO documento) throws SQLException {
        return documentoAnalisisDAO.guardarDocumentoAnalizado(idExpediente, documento, CurrentUser.get().idUsuario());
    }

    /**
     * Ícono "Eliminar" por fila — baja lógica, mismo patrón que Análisis (ver {@code CLAUDE.md} raíz:
     * "No eliminar físicamente"). Solo permite eliminar cartas finales de notificación
     * ({@link #TIPOS_CARTA_NOTIFICACION}) — un documento que viene de Análisis/Verificación/otra área
     * (resolución, informe, oficio, etc.) se rechaza, aunque el frontend ya deshabilita ese ícono
     * para esos casos (esta validación es la defensa real, no solo la de UI).
     */
    public void eliminarDocumentoEjecucion(Long idExpediente, Long idDocumentoAnalizado) throws SQLException {
        DocumentoAnalizadoDTO documento = documentoAnalisisDAO.listarPorExpediente(idExpediente).stream()
                .filter(d -> idDocumentoAnalizado.equals(d.getIdDocumentoAnalizado()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El documento seleccionado ya no existe."));
        if (!TIPOS_CARTA_NOTIFICACION.contains(documento.getTipoDocumentoCodigo())) {
            throw new IllegalStateException(
                    "Solo pueden eliminarse desde Ejecución las cartas finales de notificación agregadas en esta bandeja.");
        }
        documentoAnalisisDAO.darBajaDocumentoAnalizado(idExpediente, idDocumentoAnalizado, CurrentUser.get().idUsuario());
    }

    /**
     * Descarga Word por fila — mismo mecanismo que Análisis (21/08/2026, pedido explícito del
     * usuario: "3 primeros iconos... similar como en la grilla de documentos analizados", agrega el
     * ícono Word que Ejecución no tenía). Reutiliza el resultado de Análisis ya registrado
     * (`AnalisisExpedienteDAO#obtenerAnalisisRegistrado`, sin restricción de etapa — lee
     * `expediente_evaluacion` por id de expediente, válido sin importar que el expediente ya haya
     * avanzado a Ejecución) para la misma variable `#resAnalisis#` que usa Análisis.
     */
    public DocumentoWordDTO generarDocumentoWordEjecucion(Long idExpediente, Long idDocumentoAnalizado) throws SQLException, IOException {
        DocumentoAnalizadoDTO documento = documentoAnalisisDAO.listarPorExpediente(idExpediente).stream()
                .filter(d -> idDocumentoAnalizado.equals(d.getIdDocumentoAnalizado()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El documento seleccionado ya no existe."));
        ExpedienteEdicionManualDTO expediente = expedienteEdicionManualService.obtenerDatos(idExpediente);
        String resultadoNombre = analisisExpedienteDAO.obtenerAnalisisRegistrado(idExpediente).getResultadoNombre();
        Map<String, String> valores = analisisPlantillaDocumentoService.valores(expediente, documento, resultadoNombre);
        byte[] contenido = analisisPlantillaDocumentoService.generarDocumento(
                documento.getTipoDocumentoCodigo(), documento.getTipoDocumentoNombre(), valores);
        String nombreArchivo = analisisPlantillaDocumentoService.nombreSugerido(
                expediente.getNumeroExpediente(), documento.getTipoDocumentoNombre());
        return new DocumentoWordDTO(contenido, nombreArchivo);
    }

    private Long resolverIdEquipoSupervision() throws SQLException {
        return equipoAsignacionDAO.listarEquiposActivos().stream()
                .filter(eq -> EQUIPO_SUPERVISION.equalsIgnoreCase(eq.getCodigo()))
                .map(eq -> eq.getIdEquipo())
                .findFirst()
                .orElse(null);
    }
}
