package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.NotificacionAsignacionDocumentoDTO;
import com.sdrerc.v3.domain.NotificacionIntentoDTO;
import com.sdrerc.v3.infrastructure.dao.CatalogoLookupDAO;
import com.sdrerc.v3.infrastructure.dao.DocumentoAnalisisDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Port acotado de la Bandeja Notificación de Notificación (3ra pestaña, 22/08/2026, pedido
 * explícito del usuario vía prompt estructurado, investigación previa obligatoria contra V2 real —
 * {@code JPanelNotificacionV2.crearBandejaNotifV2}/{@code reconstruirFilasNotifBandeja}/
 * {@code crearBorradoresIntento}/{@code guardarFilaIntento} y
 * {@code DocumentoAnalisisDAO.listarDocumentosNotificacion}/{@code registrarIntentoNotificacion}/
 * {@code actualizarIntentoNotificacion}/{@code confirmarRecepcionIntentoNotificacion}/
 * {@code darBajaIntentoNotificacion}). Ver Javadoc del DAO (V3) para el detalle SQL completo.
 *
 * <p><b>Hallazgo clave de la investigación, con implicancia directa en el diseño de V3</b>: en V2
 * real, el buscador (texto/fechas/estado/combos), los 6 KPIs y el panel lateral de doble clic de
 * esta pestaña dependen todos de una lista/tabla LEGACY a nivel de expediente
 * ({@code NotificacionExpedienteDTO}/{@code expedientes}/{@code expedientesVisibles}/{@code table})
 * que está desconectada de los datos reales que alimenta la grilla árbol visible
 * (`tablaNotifBandeja`/`documentosNotifBandeja`, basada en `NotificacionAsignacionDocumentoDTO`) —
 * confirmado leyendo el código real: la tabla legacy nunca se agrega a ningún contenedor visible,
 * "Buscar" solo refresca esos 6 KPIs y no filtra la grilla real, y el panel de doble clic solo
 * muestra datos si por casualidad ese expediente estaba en una búsqueda previa exitosa. V3 NO
 * replica ese bug: el buscador, los KPIs y el "Panel de datos" de esta pestaña se construyen
 * directamente sobre {@code NotificacionAsignacionDocumentoDTO} (el mismo DTO real de la grilla),
 * igual que ya se hizo para Bandeja Asignación/Validación.</p>
 */
@Service
public class NotificacionNotificacionService {

    private final DocumentoAnalisisDAO documentoAnalisisDAO;
    private final CatalogoLookupDAO catalogoLookupDAO;

    public NotificacionNotificacionService(DocumentoAnalisisDAO documentoAnalisisDAO, CatalogoLookupDAO catalogoLookupDAO) {
        this.documentoAnalisisDAO = documentoAnalisisDAO;
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    /** Bandeja Notificación — SÍ filtra por visibilidad de fila (mismo criterio que Bandeja Validación). */
    public List<NotificacionAsignacionDocumentoDTO> listarBandeja() throws SQLException {
        var usuario = CurrentUser.get();
        boolean esAdmin = usuario.hasRole("ADMIN_SISTEMA");
        List<Long> idsEquipoActual = esAdmin ? List.of() : catalogoLookupDAO.listarIdsEquipoDeUsuario(usuario.idUsuario());
        return documentoAnalisisDAO.listarDocumentosNotificacion(esAdmin, usuario.idUsuario(), idsEquipoActual);
    }

    /** Intentos directos (1/2) de un documento — alimenta la expansión "+" de una fila padre. */
    public List<NotificacionIntentoDTO> listarIntentos(Long idDocumentoAnalizado) throws SQLException {
        return documentoAnalisisDAO.listarIntentosNotificacion(idDocumentoAnalizado);
    }

    /** "+ Agregar intento", fila borrador — ver validaciones de máximo 2 intentos en el frontend (mismo criterio que V2). */
    public void agregarIntento(
            Long idExpediente, Long idDocumentoAnalizado, String tipoNotificacionCodigo,
            String codigoNotificacion, LocalDate fechaEnvio) throws SQLException {
        documentoAnalisisDAO.registrarIntentoNotificacion(
                idExpediente, idDocumentoAnalizado, tipoNotificacionCodigo, codigoNotificacion, fechaEnvio,
                CurrentUser.get().idUsuario());
    }

    /** Guardar una fila de intento existente sin marcarla "Ubicado" (Estado final resuelto por el cliente). */
    public void actualizarIntento(
            Long idExpedienteNotificacion, String tipoNotificacionCodigo, String estadoNotificacionCodigo,
            String codigoNotificacion, LocalDate fechaEnvio) throws SQLException {
        documentoAnalisisDAO.actualizarIntentoNotificacion(
                idExpedienteNotificacion, tipoNotificacionCodigo, estadoNotificacionCodigo, codigoNotificacion,
                fechaEnvio, CurrentUser.get().idUsuario());
    }

    /** Guardar una fila de intento marcándola "Ubicado" — confirma recepción (Fecha Acuse). */
    public void confirmarRecepcion(
            Long idExpediente, Long idExpedienteNotificacion, Long idDocumentoAnalizado,
            String codigoORecibidoPor, LocalDate fechaEnvio, LocalDate fechaRecepcion) throws SQLException {
        documentoAnalisisDAO.confirmarRecepcionIntentoNotificacion(
                idExpediente, idExpedienteNotificacion, idDocumentoAnalizado, codigoORecibidoPor,
                fechaEnvio, fechaRecepcion, CurrentUser.get().idUsuario());
    }

    /** Baja lógica de un intento ya registrado (ícono Eliminar). */
    public void eliminarIntento(Long idExpedienteNotificacion) throws SQLException {
        documentoAnalisisDAO.darBajaIntentoNotificacion(idExpedienteNotificacion, CurrentUser.get().idUsuario());
    }
}
