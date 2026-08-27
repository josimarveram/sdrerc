package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.DocumentoAnalizadoDTO;
import com.sdrerc.v3.domain.EquipoAsignacionDTO;
import com.sdrerc.v3.domain.ObservacionVerificacionDTO;
import com.sdrerc.v3.domain.UsuarioAsignableDTO;
import com.sdrerc.v3.domain.VerificacionExpedienteDTO;
import com.sdrerc.v3.domain.VerificacionRegistroDTO;
import com.sdrerc.v3.domain.VerificacionResultadoDTO;
import com.sdrerc.v3.infrastructure.dao.CatalogoLookupDAO;
import com.sdrerc.v3.infrastructure.dao.DocumentoAnalisisDAO;
import com.sdrerc.v3.infrastructure.dao.EquipoAsignacionDAO;
import com.sdrerc.v3.infrastructure.dao.UsuarioAsignacionDAO;
import com.sdrerc.v3.infrastructure.dao.VerificacionExpedienteDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Port acotado de com.sdrerc.application.sdrercapp.VerificacionExpedienteService (V2) — mismo
 * alcance que {@link VerificacionExpedienteDAO} (solo el camino real de "Registrar Verificación",
 * ver su Javadoc). Segundo consumidor real de {@link com.sdrerc.v3.infrastructure.dao.VisibilidadBandejaSql}
 * (con {@code incluirAbogadosSupervisados=true}, a diferencia de Análisis).
 */
@Service
public class VerificacionExpedienteService {

    /** Mismo whitelist que V2 ({@code esEquipoDestinoVerificacionValido}), verificado contra el código real. */
    private static final Set<String> EQUIPOS_DESTINO_VALIDOS = Set.of("EQ_ANALISIS", "EQ_EJECUCION", "EQ_SUPERVISION");

    private final VerificacionExpedienteDAO verificacionExpedienteDAO;
    private final VerificacionValidacionService verificacionValidacionService;
    private final CatalogoLookupDAO catalogoLookupDAO;
    private final DocumentoAnalisisDAO documentoAnalisisDAO;
    private final EquipoAsignacionDAO equipoAsignacionDAO;
    private final UsuarioAsignacionDAO usuarioAsignacionDAO;

    public VerificacionExpedienteService(
            VerificacionExpedienteDAO verificacionExpedienteDAO,
            VerificacionValidacionService verificacionValidacionService,
            CatalogoLookupDAO catalogoLookupDAO,
            DocumentoAnalisisDAO documentoAnalisisDAO,
            EquipoAsignacionDAO equipoAsignacionDAO,
            UsuarioAsignacionDAO usuarioAsignacionDAO) {
        this.verificacionExpedienteDAO = verificacionExpedienteDAO;
        this.verificacionValidacionService = verificacionValidacionService;
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.documentoAnalisisDAO = documentoAnalisisDAO;
        this.equipoAsignacionDAO = equipoAsignacionDAO;
        this.usuarioAsignacionDAO = usuarioAsignacionDAO;
    }

    public List<VerificacionExpedienteDTO> buscarExpedientes(
            String textoLibre, String estadoCodigo, LocalDate fechaSolicitudDesde, LocalDate fechaSolicitudHasta, Integer limite) throws SQLException {
        var usuario = CurrentUser.get();
        boolean esAdmin = usuario.hasRole("ADMIN_SISTEMA");
        List<Long> idsEquipoActual = esAdmin ? List.of() : catalogoLookupDAO.listarIdsEquipoDeUsuario(usuario.idUsuario());
        return verificacionExpedienteDAO.buscarExpedientes(
                textoLibre, estadoCodigo, fechaSolicitudDesde, fechaSolicitudHasta, esAdmin, usuario.idUsuario(), idsEquipoActual, limite);
    }

    public List<String> validarRegistroVerificacion(VerificacionRegistroDTO registro) {
        return verificacionValidacionService.validarRegistroVerificacion(registro);
    }

    /** "Registrar Verificación" — rama Observado: solo guarda, no mueve el expediente de etapa. */
    public VerificacionResultadoDTO registrarObservado(Long idExpediente, String comentario, String descripcionObservacion) throws SQLException {
        ObservacionVerificacionDTO observacion = new ObservacionVerificacionDTO("", "", "", "", descripcionObservacion);
        return verificacionExpedienteDAO.registrarObservado(idExpediente, comentario, observacion, CurrentUser.get().idUsuario());
    }

    /** "Registrar Verificación" — rama Aprobado: solo guarda, no mueve el expediente de etapa (antes {@code aprobarVerificacionDirecta}). */
    public VerificacionResultadoDTO registrarAprobado(Long idExpediente, String comentario) throws SQLException {
        return verificacionExpedienteDAO.registrarAprobado(idExpediente, comentario, CurrentUser.get().idUsuario());
    }

    /** "Enviar a ..." — mueve el expediente ya registrado (Observado o Aprobado) al equipo elegido en Destino operativo. */
    public VerificacionResultadoDTO aprobarVerificacionConDestino(
            Long idExpediente, String comentario, Long idEquipoDestino, Long idUsuarioDestino) throws SQLException {
        return verificacionExpedienteDAO.aprobarVerificacionConDestino(idExpediente, comentario, idEquipoDestino, idUsuarioDestino, CurrentUser.get().idUsuario());
    }

    /** Combo "Equipo destino" — filtrado server-side a EQ_ANALISIS/EQ_EJECUCION/EQ_SUPERVISION (mismo whitelist que V2). */
    public List<EquipoAsignacionDTO> listarEquiposDestino() throws SQLException {
        return equipoAsignacionDAO.listarEquiposActivos().stream()
                .filter(eq -> EQUIPOS_DESTINO_VALIDOS.contains(eq.getCodigo()))
                .toList();
    }

    /**
     * Combo "Usuario destino" — {@code listarUsuariosAsignablesPorEquipo} (solo pertenencia activa
     * al equipo, sin restricción de rol), NO {@code listarAbogadosAsignables} (restringido a
     * ABOGADO/ANALISTA) — mismo criterio que V2, ya que EQ_SUPERVISION tiene miembros con otros roles.
     */
    public List<UsuarioAsignableDTO> listarUsuariosDestino(Long idEquipo) throws SQLException {
        return usuarioAsignacionDAO.listarUsuariosAsignablesPorEquipo(idEquipo);
    }

    /** Grilla "Documentos verificados" — reutiliza literalmente DocumentoAnalisisDAO (misma tabla que Análisis). */
    public List<DocumentoAnalizadoDTO> listarDocumentosVerificados(Long idExpediente) throws SQLException {
        return documentoAnalisisDAO.listarPorExpediente(idExpediente);
    }

    /** Ícono "Guardar" de la grilla — solo Estado/Detalle Obs. (columna "Comentario", mapea a `descripcion`)/Fecha Emisión/N° Documento son editables en el frontend. */
    public Long guardarDocumentoVerificado(Long idExpediente, DocumentoAnalizadoDTO documento) throws SQLException {
        return documentoAnalisisDAO.guardarDocumentoAnalizado(idExpediente, documento, CurrentUser.get().idUsuario());
    }
}
