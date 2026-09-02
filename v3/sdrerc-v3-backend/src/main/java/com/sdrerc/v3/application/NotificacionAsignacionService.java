package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.AsignacionHistorialDTO;
import com.sdrerc.v3.domain.CatalogoItemDTO;
import com.sdrerc.v3.domain.EquipoAsignacionDTO;
import com.sdrerc.v3.domain.NotificacionAsignacionDocumentoDTO;
import com.sdrerc.v3.domain.UsuarioAsignableDTO;
import com.sdrerc.v3.infrastructure.dao.AsignacionExpedienteDAO;
import com.sdrerc.v3.infrastructure.dao.CatalogoLookupDAO;
import com.sdrerc.v3.infrastructure.dao.DocumentoAnalisisDAO;
import com.sdrerc.v3.infrastructure.dao.EquipoAsignacionDAO;
import com.sdrerc.v3.infrastructure.dao.UsuarioAsignacionDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Port acotado de com.sdrerc.application.sdrercapp.NotificacionExpedienteService (V2) — primera
 * fase de Notificación (21/08/2026, pedido explícito del usuario): solo la Bandeja Asignación
 * (documento-nivel), ver Javadoc de {@link DocumentoAnalisisDAO}.
 *
 * <p>"Destino operativo" del mini-panel "② Asignación" (V2) se implementa como una sola acción
 * ({@link #generarAsignacion}) que dispatcha según el código del equipo elegido — {@code
 * EQ_VALIDACION}/{@code EQ_NOTIFICACION} asignan (o reasignan) el lote completo vía {@link
 * DocumentoAnalisisDAO#asignarNotificacionMultiple}; {@code EQ_EJECUCION} deriva UN SOLO documento
 * de vuelta a Ejecución vía {@link DocumentoAnalisisDAO#derivarDocumentoNotificacionAEjecucion} (no
 * reasigna responsable, mismo criterio que Verificación→Ejecución). {@code EQ_ANALISIS} queda
 * DIFERIDO — ver Javadoc de {@link DocumentoAnalisisDAO}.</p>
 */
@Service
public class NotificacionAsignacionService {

    /** Equipos válidos para "Destino operativo" en este incremento (EQ_ANALISIS diferido). */
    private static final Set<String> EQUIPOS_DESTINO_VALIDOS = Set.of("EQ_VALIDACION", "EQ_NOTIFICACION", "EQ_EJECUCION");
    private static final String EQUIPO_EJECUCION = "EQ_EJECUCION";
    private static final String EQUIPO_ANALISIS = "EQ_ANALISIS";
    /**
     * Equipos válidos para el "Destino operativo" del resultado Observado del panel de Supervisión
     * (02/09/2026, pedido explícito del usuario — restaura el flujo real de V2/diseño previo al
     * 28/08/2026: al observar, el documento se deriva a Análisis o Ejecución para su corrección,
     * distinto del "Destino operativo" de {@link #EQUIPOS_DESTINO_VALIDOS} usado por "Asignar", que
     * envía a Validación/Notificación). {@code EQ_ANALISIS} exige la transición
     * {@code DEVOLUCION_A_ANALISIS: NOTIFICACION/POR_ASIGNAR → ANALISIS/OBSERVADO} — script
     * {@code 122_transicion_notificacion_supervision_observado_analisis.sql}, creado, no ejecutado.
     */
    private static final Set<String> EQUIPOS_DESTINO_OBSERVADO = Set.of(EQUIPO_ANALISIS, EQUIPO_EJECUCION);

    private final DocumentoAnalisisDAO documentoAnalisisDAO;
    private final EquipoAsignacionDAO equipoAsignacionDAO;
    private final UsuarioAsignacionDAO usuarioAsignacionDAO;
    private final CatalogoLookupDAO catalogoLookupDAO;
    private final AsignacionExpedienteDAO asignacionExpedienteDAO;

    public NotificacionAsignacionService(
            DocumentoAnalisisDAO documentoAnalisisDAO,
            EquipoAsignacionDAO equipoAsignacionDAO,
            UsuarioAsignacionDAO usuarioAsignacionDAO,
            CatalogoLookupDAO catalogoLookupDAO,
            AsignacionExpedienteDAO asignacionExpedienteDAO) {
        this.documentoAnalisisDAO = documentoAnalisisDAO;
        this.equipoAsignacionDAO = equipoAsignacionDAO;
        this.usuarioAsignacionDAO = usuarioAsignacionDAO;
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.asignacionExpedienteDAO = asignacionExpedienteDAO;
    }

    /** Bandeja Asignación — sin filtro de visibilidad por fila (ver Javadoc del DAO). */
    public List<NotificacionAsignacionDocumentoDTO> listarBandeja() throws SQLException {
        return documentoAnalisisDAO.listarDocumentosAsignacionNotificacion();
    }

    /** Mini-panel "① Emisión" — todos los documentos del expediente enfocado. */
    public List<NotificacionAsignacionDocumentoDTO> listarDocumentosPorExpediente(Long idExpediente) throws SQLException {
        return documentoAnalisisDAO.listarDocumentosNotificacionPorExpediente(idExpediente);
    }

    /** Ícono "Guardar" de "Documentos a firmar". */
    public void guardarFirma(Long idDocumentoAnalizado, String numeroDocumento, LocalDate fechaEmision, String estadoDocumentoCodigo) throws SQLException {
        documentoAnalisisDAO.registrarFirmaDocumentoNotificacion(
                idDocumentoAnalizado, numeroDocumento, fechaEmision, estadoDocumentoCodigo, CurrentUser.get().idUsuario());
    }

    /**
     * "Destino operativo" — dispatch único según el código del equipo elegido. Ver Javadoc de la
     * clase para el alcance (EQ_VALIDACION/EQ_NOTIFICACION asignan el lote, EQ_EJECUCION deriva un
     * único documento).
     */
    public void generarAsignacion(
            List<Long> idsDocumentoAnalizado,
            Long idEquipoDestino,
            Long idUsuarioDestino,
            Map<Long, String> hojasEnvioPorDocumento,
            String comentario,
            boolean reasignacion) throws SQLException {
        var actor = CurrentUser.get();
        String codigoEquipo = resolverCodigoEquipo(idEquipoDestino);
        if (EQUIPO_EJECUCION.equalsIgnoreCase(codigoEquipo)) {
            if (idsDocumentoAnalizado == null || idsDocumentoAnalizado.size() != 1) {
                throw new IllegalArgumentException("Para derivar a Ejecución, seleccione exactamente un documento.");
            }
            documentoAnalisisDAO.derivarDocumentoNotificacionAEjecucion(
                    idsDocumentoAnalizado.get(0), idEquipoDestino, idUsuarioDestino, comentario,
                    actor.idUsuario(), actor.hasRole("ADMIN_SISTEMA"));
            return;
        }
        documentoAnalisisDAO.asignarNotificacionMultiple(
                idsDocumentoAnalizado, idEquipoDestino, idUsuarioDestino, hojasEnvioPorDocumento,
                actor.idUsuario(), actor.hasRole("ADMIN_SISTEMA"), reasignacion);
    }

    private String resolverCodigoEquipo(Long idEquipoDestino) throws SQLException {
        if (idEquipoDestino == null) {
            return null;
        }
        return equipoAsignacionDAO.listarEquiposActivos().stream()
                .filter(eq -> idEquipoDestino.equals(eq.getIdEquipo()))
                .map(EquipoAsignacionDTO::getCodigo)
                .findFirst()
                .orElse(null);
    }

    /** "Registrar Supervisión" (resultado Aprobado) — solo auditoría, ver Javadoc del DAO. */
    public void registrarSupervisionAprobada(Long idExpediente, Long idDocumentoAnalizado) throws SQLException {
        documentoAnalisisDAO.registrarSupervisionEmisionAprobada(idExpediente, idDocumentoAnalizado, CurrentUser.get().idUsuario());
    }

    /**
     * "Registrar Supervisión" (resultado Observado) — 02/09/2026, pedido explícito del usuario.
     * Deriva el documento/expediente a {@code EQ_ANALISIS} (vía {@link
     * AsignacionExpedienteDAO#reasignarDesdeCartaRespuesta}, mismo método ya usado por "Cartas de
     * respuesta" con destino "DERIVADO", acá con destino "OBSERVADO" — el propio Javadoc de ese
     * método ya anticipaba este reuso) o {@code EQ_EJECUCION} (vía {@link
     * DocumentoAnalisisDAO#derivarDocumentoNotificacionAEjecucion}, ya existente). El nombre del
     * motivo de corrección se antepone al comentario libre del usuario (sin columna nueva —
     * `motivo_correccion` no tiene una columna dedicada en `EXPEDIENTE_HISTORIAL` para esta acción,
     * a diferencia del fix de Ejecución que sí usa la columna `motivo`).
     */
    public void registrarSupervisionObservada(
            Long idExpediente, Long idDocumentoAnalizado, Long idEquipoDestino, Long idUsuarioDestino,
            String motivoCorreccionNombre, String comentario) throws SQLException {
        String codigoEquipo = resolverCodigoEquipo(idEquipoDestino);
        if (codigoEquipo == null || !EQUIPOS_DESTINO_OBSERVADO.contains(codigoEquipo)) {
            throw new IllegalArgumentException("Seleccione Equipo Análisis o Equipo Ejecución como destino de la observación.");
        }
        var actor = CurrentUser.get();
        String comentarioCompleto = (motivoCorreccionNombre != null && !motivoCorreccionNombre.isBlank() ? "Motivo: " + motivoCorreccionNombre + ". " : "")
                + (comentario != null ? comentario : "");
        if (EQUIPO_EJECUCION.equalsIgnoreCase(codigoEquipo)) {
            documentoAnalisisDAO.derivarDocumentoNotificacionAEjecucion(
                    idDocumentoAnalizado, idEquipoDestino, idUsuarioDestino, comentarioCompleto,
                    actor.idUsuario(), actor.hasRole("ADMIN_SISTEMA"));
            return;
        }
        asignacionExpedienteDAO.reasignarDesdeCartaRespuesta(
                idExpediente, idEquipoDestino, idUsuarioDestino, comentarioCompleto,
                actor.idUsuario(), actor.hasRole("ADMIN_SISTEMA"), "OBSERVADO");
    }

    /** Combo "Equipo destino" exclusivo del resultado Observado del panel de Supervisión (Análisis/Ejecución). */
    public List<EquipoAsignacionDTO> listarEquiposDestinoObservado() throws SQLException {
        return equipoAsignacionDAO.listarEquiposActivos().stream()
                .filter(eq -> EQUIPOS_DESTINO_OBSERVADO.contains(eq.getCodigo()))
                .toList();
    }

    /** Combo "Motivo de corrección" del resultado Observado (mismo catálogo `motivo_correccion` que ya usa Ejecución). */
    public List<CatalogoItemDTO> listarMotivosCorreccion() throws SQLException {
        return catalogoLookupDAO.listarMotivosCorreccion();
    }

    public boolean existeSupervisionAprobada(Long idDocumentoAnalizado) throws SQLException {
        return documentoAnalisisDAO.existeSupervisionEmisionAprobada(idDocumentoAnalizado);
    }

    public List<AsignacionHistorialDTO> listarHistorial(Long idDocumentoAnalizado) throws SQLException {
        return documentoAnalisisDAO.listarHistorialAsignacionesNotificacion(idDocumentoAnalizado);
    }

    /** Combo "Equipo destino" — filtrado a EQ_VALIDACION/EQ_NOTIFICACION/EQ_EJECUCION (EQ_ANALISIS diferido). */
    public List<EquipoAsignacionDTO> listarEquiposDestino() throws SQLException {
        return equipoAsignacionDAO.listarEquiposActivos().stream()
                .filter(eq -> EQUIPOS_DESTINO_VALIDOS.contains(eq.getCodigo()))
                .toList();
    }

    /** Combo "Usuario destino" — solo pertenencia activa al equipo, sin restricción de rol (mismo criterio que Verificación). */
    public List<UsuarioAsignableDTO> listarUsuariosDestino(Long idEquipo) throws SQLException {
        return usuarioAsignacionDAO.listarUsuariosAsignablesPorEquipo(idEquipo);
    }
}
