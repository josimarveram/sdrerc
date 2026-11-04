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
 * Port acotado de la Bandeja Publicación de Notificación (4ta pestaña, 24/08/2026, pedido explícito
 * del usuario: "sería casi lo mismo que la bandeja de notificación, solo que sería 1 sola
 * notificación de tipo publicación"). Investigación previa contra la sección "Bandeja Publicacion"
 * ya documentada en el `CLAUDE.md` raíz (implementada en V2 el 05/08/2026, con varias correcciones
 * posteriores documentadas ahí mismo — bandera {@code AGOTO_INTENTOS_DIRECTOS_SQL}, columna fusionada
 * "Estado Notif./Public.", "Fecha Publicación" reutilizando {@code fecha_envio}).
 *
 * <p>Mismo universo de documentos que la Bandeja Notificación ({@code CONDICION_BANDEJA_NOTIFICACION}
 * + misma visibilidad por fila), acotado a los que YA agotaron sus 2 intentos directos
 * ({@code AGOTO_INTENTOS_DIRECTOS_SQL = 1}) — ver Javadoc de {@link DocumentoAnalisisDAO#listarDocumentosBandejaPublicacion}.
 * El "3er intento" (Publicación) reutiliza literalmente el mismo mecanismo de intento
 * ({@code EXPEDIENTE_NOTIFICACION} con {@code tipo_notificacion='PUBLICACION'}, catálogo ya sembrado),
 * no una tabla nueva — los intentos 1/2 se muestran de solo lectura (sin acción alguna, confirmado
 * contra V2 real), solo la fila de Publicación es editable: "Fecha Publicación" reutiliza
 * {@code fecha_envio} (mismo campo que ya usa cualquier intento), y "Estado Notif./Public." mapea a
 * {@code estado_notificacion} códigos {@code PENDIENTE}/{@code EXITOSA} (Pendiente/Publicado) — sin
 * paso de "confirmar recepción" (a diferencia de un intento directo ubicado, la Publicación no toca
 * {@code expediente_cargo_acuse} ni {@code expediente_documento_analizado.fecha_acuse}).</p>
 */
@Service
public class NotificacionPublicacionService {

    private final DocumentoAnalisisDAO documentoAnalisisDAO;
    private final CatalogoLookupDAO catalogoLookupDAO;

    public NotificacionPublicacionService(DocumentoAnalisisDAO documentoAnalisisDAO, CatalogoLookupDAO catalogoLookupDAO) {
        this.documentoAnalisisDAO = documentoAnalisisDAO;
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    /** Bandeja Publicación — SÍ filtra por visibilidad de fila (mismo criterio que Bandeja Notificación). */
    public List<NotificacionAsignacionDocumentoDTO> listarBandeja() throws SQLException {
        var usuario = CurrentUser.get();
        boolean esAdmin = usuario.hasRole("ADMIN_SISTEMA");
        List<Long> idsEquipoActual = esAdmin ? List.of() : catalogoLookupDAO.listarIdsEquipoDeUsuario(usuario.idUsuario());
        return documentoAnalisisDAO.listarDocumentosBandejaPublicacion(esAdmin, usuario.idUsuario(), idsEquipoActual);
    }

    /** Intentos 1/2 (solo lectura) + Publicación (editable) de un documento. */
    public List<NotificacionIntentoDTO> listarIntentos(Long idDocumentoAnalizado) throws SQLException {
        return documentoAnalisisDAO.listarIntentosPublicacion(idDocumentoAnalizado);
    }

    /** "+ Agregar publicación", fila borrador — reutiliza {@code registrarIntentoNotificacion} con tipo PUBLICACION. */
    public void agregarPublicacion(Long idExpediente, Long idDocumentoAnalizado, LocalDate fechaPublicacion) throws SQLException {
        documentoAnalisisDAO.registrarIntentoNotificacion(
                idExpediente, idDocumentoAnalizado, "PUBLICACION", "", fechaPublicacion, CurrentUser.get().idUsuario());
    }

    /** Guardar la fila de Publicación ya persistida — reutiliza {@code actualizarIntentoNotificacion}. */
    public void actualizarPublicacion(Long idExpedienteNotificacion, String estadoNotificacionCodigo, LocalDate fechaPublicacion) throws SQLException {
        documentoAnalisisDAO.actualizarIntentoNotificacion(
                idExpedienteNotificacion, "PUBLICACION", estadoNotificacionCodigo, "", fechaPublicacion, CurrentUser.get().idUsuario());
    }

    /** Baja lógica de la fila de Publicación — reutiliza {@code darBajaIntentoNotificacion}. */
    public void eliminarPublicacion(Long idExpedienteNotificacion) throws SQLException {
        documentoAnalisisDAO.darBajaIntentoNotificacion(idExpedienteNotificacion, CurrentUser.get().idUsuario());
    }
}
