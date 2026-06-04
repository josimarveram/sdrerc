package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionReversionDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.CatalogoLookupDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.EjecucionExpedienteDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.ObservacionExpedienteDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EjecucionExpedienteService {

    public static final String ACCION_INICIO_EJECUCION = "INICIO_EJECUCION";
    public static final String ACCION_OBSERVACION_EJECUCION = "OBSERVACION_EJECUCION";
    public static final String ACCION_DOCUMENTO_INCONSISTENTE = "REVERSION_ESTADO_DOCUMENTO_EJECUCION";
    public static final String ACCION_DEVOLUCION_ANALISIS = "DEVOLUCION_A_ANALISIS";
    public static final String ACCION_DERIVACION_NOTIFICACION = "DERIVACION_A_NOTIFICACION";

    private final EjecucionExpedienteDAO ejecucionExpedienteDAO;
    private final EjecucionValidacionService validacionService;
    private final UsuarioAsignacionService usuarioAsignacionService;
    private final CatalogoLookupDAO catalogoLookupDAO;
    private final ObservacionExpedienteDAO observacionExpedienteDAO;

    public EjecucionExpedienteService() {
        this(
                new EjecucionExpedienteDAO(),
                new EjecucionValidacionService(),
                new UsuarioAsignacionService(),
                new CatalogoLookupDAO(),
                new ObservacionExpedienteDAO());
    }

    public EjecucionExpedienteService(
            EjecucionExpedienteDAO ejecucionExpedienteDAO,
            EjecucionValidacionService validacionService,
            UsuarioAsignacionService usuarioAsignacionService,
            CatalogoLookupDAO catalogoLookupDAO,
            ObservacionExpedienteDAO observacionExpedienteDAO) {
        this.ejecucionExpedienteDAO = ejecucionExpedienteDAO;
        this.validacionService = validacionService;
        this.usuarioAsignacionService = usuarioAsignacionService;
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.observacionExpedienteDAO = observacionExpedienteDAO;
    }

    public List<EjecucionExpedienteDTO> buscarExpedientes(String textoLibre, String estadoCodigo, int limite) throws SQLException {
        return ejecucionExpedienteDAO.buscarExpedientes(textoLibre, estadoCodigo, limite);
    }

    public List<CatalogoItemDTO> listarResultadosEjecucion() throws SQLException {
        return catalogoLookupDAO.listarResultadosEjecucion();
    }

    public List<CatalogoItemDTO> listarTiposObservacion() throws SQLException {
        return observacionExpedienteDAO.listarTiposObservacion();
    }

    public List<CatalogoItemDTO> listarMotivosCorreccion() throws SQLException {
        return catalogoLookupDAO.listarMotivosCorreccion();
    }

    public EjecucionResultadoDTO registrarEjecucion(EjecucionRegistroDTO registro) throws SQLException {
        validar(registro, false);
        return ejecucionExpedienteDAO.registrarEjecucion(normalizarRegistro(registro), resolverUsuarioActualSdrercApp());
    }

    public EjecucionResultadoDTO marcarEjecutado(EjecucionRegistroDTO registro) throws SQLException {
        validar(registro, false);
        EjecucionRegistroDTO normalizado = normalizarRegistro(registro);
        if (normalizado.getResultadoCodigo().trim().isEmpty()) {
            normalizado = new EjecucionRegistroDTO(
                    normalizado.getIdExpediente(),
                    normalizado.getAccionCodigo(),
                    "EJECUTADO",
                    "Ejecutado",
                    normalizado.getFechaEjecucion(),
                    normalizado.getTipoObservacionCodigo(),
                    normalizado.getMotivoCorreccionCodigo(),
                    normalizado.getComentario());
        }
        return ejecucionExpedienteDAO.registrarEjecucion(normalizado, resolverUsuarioActualSdrercApp());
    }

    public EjecucionResultadoDTO registrarObservacion(EjecucionRegistroDTO registro) throws SQLException {
        validar(registro, true);
        return ejecucionExpedienteDAO.registrarObservacion(normalizarRegistro(registro), resolverUsuarioActualSdrercApp());
    }

    public EjecucionResultadoDTO registrarDocumentoInconsistente(EjecucionRegistroDTO registro) throws SQLException {
        validar(registro, true);
        return ejecucionExpedienteDAO.registrarDocumentoInconsistente(normalizarRegistro(registro), resolverUsuarioActualSdrercApp());
    }

    public EjecucionResultadoDTO derivarNotificacion(EjecucionRegistroDTO registro) throws SQLException {
        validar(registro, false);
        return ejecucionExpedienteDAO.derivarNotificacion(normalizarRegistro(registro), resolverUsuarioActualSdrercApp());
    }

    public EjecucionResultadoDTO revertirAnalisis(EjecucionReversionDTO reversion) throws SQLException {
        List<String> errores = validacionService.validarReversion(reversion);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
        return ejecucionExpedienteDAO.revertirAnalisis(reversion, resolverUsuarioActualSdrercApp());
    }

    private void validar(EjecucionRegistroDTO registro, boolean requiereComentario) {
        List<String> errores = validacionService.validarRegistro(registro, requiereComentario);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
    }

    private EjecucionRegistroDTO normalizarRegistro(EjecucionRegistroDTO registro) {
        LocalDate fecha = registro.getFechaEjecucion() == null ? LocalDate.now() : registro.getFechaEjecucion();
        return new EjecucionRegistroDTO(
                registro.getIdExpediente(),
                registro.getAccionCodigo(),
                registro.getResultadoCodigo(),
                registro.getResultadoNombre(),
                fecha,
                registro.getTipoObservacionCodigo(),
                registro.getMotivoCorreccionCodigo(),
                registro.getComentario());
    }

    private Long resolverUsuarioActualSdrercApp() {
        try {
            String username = SessionContext.getUsername();
            return usuarioAsignacionService.obtenerIdUsuarioActivoPorUsername(username);
        } catch (Exception ex) {
            return null;
        }
    }
}
