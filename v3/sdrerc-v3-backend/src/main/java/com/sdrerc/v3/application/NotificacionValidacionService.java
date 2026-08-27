package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.EquipoAsignacionDTO;
import com.sdrerc.v3.domain.NotificacionAsignacionDocumentoDTO;
import com.sdrerc.v3.domain.UsuarioAsignableDTO;
import com.sdrerc.v3.infrastructure.dao.CatalogoLookupDAO;
import com.sdrerc.v3.infrastructure.dao.DocumentoAnalisisDAO;
import com.sdrerc.v3.infrastructure.dao.EquipoAsignacionDAO;
import com.sdrerc.v3.infrastructure.dao.UsuarioAsignacionDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Port acotado de la Bandeja Validación de Notificación (21/08/2026, pedido explícito del usuario,
 * investigación previa contra el código real de V2 — {@code JPanelNotificacionV2.crearBandejaValidacion}
 * y {@code DocumentoAnalisisDAO.registrarResultadoValidacion}). Ver Javadoc de {@link
 * DocumentoAnalisisDAO#listarDocumentosValidacion}/{@link DocumentoAnalisisDAO#registrarResultadoValidacion}
 * para el detalle completo de la lógica de negocio portada.
 *
 * <p><b>"Enviar a supervisión" (22/08/2026, mismo día, pedido explícito del usuario — reemplaza la
 * decisión anterior de no construir ningún destino operativo)</b>: V2 real, en efecto, no tiene este
 * bloque — el regreso al Supervisor ahí es 100% automático dentro de {@code registrarResultadoValidacion}
 * (Aprobado revierte el expediente a {@code POR_ASIGNAR}; Observado solo limpia el responsable, sin
 * asignar a nadie en particular). El usuario pidió explícitamente agregar, como mejora de V3 sobre
 * V2 (no como port literal), un paso manual adicional que permita al validador elegir a qué
 * usuario/equipo de supervisión se deriva el documento — sin reemplazar el registro del resultado
 * (`registrarResultadoValidacion` sigue siendo la acción que mueve el estado del expediente), sino
 * como acción independiente posterior. Reutiliza el mismo mecanismo ya construido para "Destino
 * operativo" de Bandeja Asignación ({@link DocumentoAnalisisDAO#asignarNotificacionMultiple}) con
 * destino fijo al equipo real `EQ_SUPERVISION` (ya sembrado y en uso por el combo "Equipo destino" de
 * Verificación, "Eq. Supervision") — al no ser `EQ_VALIDACION`/`EQ_NOTIFICACION`,
 * {@code resolverEstadoExpedienteDestinoNotificacion} no toca `estado_expediente` (sigue lo que ya
 * dejó `registrarResultadoValidacion`), solo actualiza responsable (`id_equipo_responsable_actual`/
 * `id_usuario_responsable_actual` del expediente y `id_equipo_notificacion`/`id_usuario_notificacion`
 * del documento) + historial. Sin script SQL nuevo: `EQ_SUPERVISION` y sus transiciones de
 * responsable ya existen.
 */
@Service
public class NotificacionValidacionService {

    private static final String EQUIPO_SUPERVISION = "EQ_SUPERVISION";

    private final DocumentoAnalisisDAO documentoAnalisisDAO;
    private final CatalogoLookupDAO catalogoLookupDAO;
    private final EquipoAsignacionDAO equipoAsignacionDAO;
    private final UsuarioAsignacionDAO usuarioAsignacionDAO;

    public NotificacionValidacionService(
            DocumentoAnalisisDAO documentoAnalisisDAO,
            CatalogoLookupDAO catalogoLookupDAO,
            EquipoAsignacionDAO equipoAsignacionDAO,
            UsuarioAsignacionDAO usuarioAsignacionDAO) {
        this.documentoAnalisisDAO = documentoAnalisisDAO;
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.equipoAsignacionDAO = equipoAsignacionDAO;
        this.usuarioAsignacionDAO = usuarioAsignacionDAO;
    }

    /** Bandeja Validación — SÍ filtra por visibilidad de fila (a diferencia de Bandeja Asignación). */
    public List<NotificacionAsignacionDocumentoDTO> listarBandeja() throws SQLException {
        var usuario = CurrentUser.get();
        boolean esAdmin = usuario.hasRole("ADMIN_SISTEMA");
        List<Long> idsEquipoActual = esAdmin ? List.of() : catalogoLookupDAO.listarIdsEquipoDeUsuario(usuario.idUsuario());
        return documentoAnalisisDAO.listarDocumentosValidacion(esAdmin, usuario.idUsuario(), idsEquipoActual);
    }

    /** Grilla "Documentos a validar" del panel — todos los documentos del expediente enfocado. */
    public List<NotificacionAsignacionDocumentoDTO> listarDocumentosPorExpediente(Long idExpediente) throws SQLException {
        return documentoAnalisisDAO.listarDocumentosNotificacionPorExpediente(idExpediente);
    }

    /** "Registrar Validación" — Aprobado/Observado, ver Javadoc del DAO para el detalle de cada caso. */
    public void registrarResultadoValidacion(Long idDocumentoAnalizado, String resultadoCodigo, String comentario) throws SQLException {
        documentoAnalisisDAO.registrarResultadoValidacion(idDocumentoAnalizado, resultadoCodigo, comentario, CurrentUser.get().idUsuario());
    }

    /** Combo "Equipo destino" del bloque "Enviar a supervisión" — fijo a `EQ_SUPERVISION`. */
    public List<EquipoAsignacionDTO> listarEquipoSupervision() throws SQLException {
        return equipoAsignacionDAO.listarEquiposActivos().stream()
                .filter(eq -> EQUIPO_SUPERVISION.equals(eq.getCodigo()))
                .toList();
    }

    /** Combo "Usuario destino" del bloque "Enviar a supervisión" — solo pertenencia activa al equipo. */
    public List<UsuarioAsignableDTO> listarUsuariosSupervision(Long idEquipo) throws SQLException {
        return usuarioAsignacionDAO.listarUsuariosAsignablesPorEquipo(idEquipo);
    }

    /**
     * "Enviar a supervisión" — deriva el responsable del documento/expediente al usuario/equipo de
     * supervisión elegido, sin tocar `estado_expediente` (ver Javadoc de la clase). Reutiliza
     * {@code asignarNotificacionMultiple} con {@code reasignacion=true} (el documento ya tuvo
     * responsable previo, el Validador).
     */
    public void enviarASupervision(Long idDocumentoAnalizado, Long idEquipoDestino, Long idUsuarioDestino) throws SQLException {
        var actor = CurrentUser.get();
        documentoAnalisisDAO.asignarNotificacionMultiple(
                List.of(idDocumentoAnalizado), idEquipoDestino, idUsuarioDestino, null,
                actor.idUsuario(), actor.hasRole("ADMIN_SISTEMA"), true);
    }
}
