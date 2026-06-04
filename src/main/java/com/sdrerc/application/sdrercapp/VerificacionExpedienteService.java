package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.VerificacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.VerificacionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.VerificacionResultadoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.CatalogoLookupDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.ObservacionExpedienteDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.VerificacionExpedienteDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VerificacionExpedienteService {

    private static final String ACCION_APROBACION = "APROBACION_VERIFICACION";
    private static final String ACCION_OBSERVACION = "REGISTRO_OBSERVACION_VERIFICACION";
    private static final String ACCION_DOCUMENTO_INCONSISTENTE = "REVERSION_ESTADO_DOCUMENTO";
    private static final String ACCION_DEVOLUCION_ANALISIS = "DEVOLUCION_A_ANALISIS";
    private static final String ACCION_ENVIO_FIRMA = "ENVIO_FIRMA";

    private final VerificacionExpedienteDAO verificacionExpedienteDAO;
    private final VerificacionValidacionService validacionService;
    private final UsuarioAsignacionService usuarioAsignacionService;
    private final CatalogoLookupDAO catalogoLookupDAO;
    private final ObservacionExpedienteDAO observacionExpedienteDAO;

    public VerificacionExpedienteService() {
        this(
                new VerificacionExpedienteDAO(),
                new VerificacionValidacionService(),
                new UsuarioAsignacionService(),
                new CatalogoLookupDAO(),
                new ObservacionExpedienteDAO());
    }

    public VerificacionExpedienteService(
            VerificacionExpedienteDAO verificacionExpedienteDAO,
            VerificacionValidacionService validacionService,
            UsuarioAsignacionService usuarioAsignacionService,
            CatalogoLookupDAO catalogoLookupDAO,
            ObservacionExpedienteDAO observacionExpedienteDAO) {
        this.verificacionExpedienteDAO = verificacionExpedienteDAO;
        this.validacionService = validacionService;
        this.usuarioAsignacionService = usuarioAsignacionService;
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.observacionExpedienteDAO = observacionExpedienteDAO;
    }

    public List<VerificacionExpedienteDTO> buscarExpedientes(String textoLibre, String estadoCodigo, int limite) throws SQLException {
        return verificacionExpedienteDAO.buscarExpedientes(textoLibre, estadoCodigo, limite);
    }

    public List<CatalogoItemDTO> listarResultadosVerificacion() {
        List<CatalogoItemDTO> items = new ArrayList<CatalogoItemDTO>();
        items.add(new CatalogoItemDTO(ACCION_APROBACION, "Aprobar verificación"));
        items.add(new CatalogoItemDTO(ACCION_OBSERVACION, "Requiere corrección"));
        items.add(new CatalogoItemDTO(ACCION_DOCUMENTO_INCONSISTENTE, "Documento inconsistente"));
        return items;
    }

    public List<CatalogoItemDTO> listarTiposObservacion() throws SQLException {
        return observacionExpedienteDAO.listarTiposObservacion();
    }

    public List<CatalogoItemDTO> listarMotivosCorreccion() throws SQLException {
        return catalogoLookupDAO.listarMotivosCorreccion();
    }

    public VerificacionResultadoDTO registrarVerificacion(VerificacionRegistroDTO registro) throws SQLException {
        List<String> errores = validacionService.validarRegistroVerificacion(registro);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
        return verificacionExpedienteDAO.registrarVerificacion(registro, resolverUsuarioActualSdrercApp());
    }

    public VerificacionResultadoDTO aprobarVerificacion(Long idExpediente, String comentario) throws SQLException {
        validacionService.validarExpedienteSeleccionado(idExpediente);
        return verificacionExpedienteDAO.aprobarVerificacion(idExpediente, comentario, resolverUsuarioActualSdrercApp());
    }

    public VerificacionResultadoDTO enviarFirma(Long idExpediente, String comentario) throws SQLException {
        validacionService.validarExpedienteSeleccionado(idExpediente);
        return verificacionExpedienteDAO.enviarFirma(idExpediente, comentario, resolverUsuarioActualSdrercApp());
    }

    public VerificacionResultadoDTO devolverAnalisis(VerificacionRegistroDTO registro) throws SQLException {
        List<String> errores = validacionService.validarRegistroVerificacion(registro);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
        return verificacionExpedienteDAO.devolverAnalisis(registro, resolverUsuarioActualSdrercApp());
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
