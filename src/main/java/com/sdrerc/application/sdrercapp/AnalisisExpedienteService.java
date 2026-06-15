package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.AnalisisExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.AnalisisRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.AnalisisResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.AnalisisExpedienteDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.CatalogoLookupDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.ObservacionExpedienteDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnalisisExpedienteService {

    private final AnalisisExpedienteDAO analisisExpedienteDAO;
    private final AnalisisValidacionService validacionService;
    private final UsuarioAsignacionService usuarioAsignacionService;
    private final CatalogoLookupDAO catalogoLookupDAO;
    private final ObservacionExpedienteDAO observacionExpedienteDAO;
    private boolean usuarioActualResuelto;
    private Long idUsuarioActualSdrercApp;

    public AnalisisExpedienteService() {
        this(
                new AnalisisExpedienteDAO(),
                new AnalisisValidacionService(),
                new UsuarioAsignacionService(),
                new CatalogoLookupDAO(),
                new ObservacionExpedienteDAO());
    }

    public AnalisisExpedienteService(
            AnalisisExpedienteDAO analisisExpedienteDAO,
            AnalisisValidacionService validacionService,
            UsuarioAsignacionService usuarioAsignacionService,
            CatalogoLookupDAO catalogoLookupDAO,
            ObservacionExpedienteDAO observacionExpedienteDAO) {
        this.analisisExpedienteDAO = analisisExpedienteDAO;
        this.validacionService = validacionService;
        this.usuarioAsignacionService = usuarioAsignacionService;
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.observacionExpedienteDAO = observacionExpedienteDAO;
    }

    public List<AnalisisExpedienteDTO> buscarExpedientes(String textoLibre, String estadoCodigo, int limite) throws SQLException {
        return buscarExpedientes(textoLibre, estadoCodigo, null, null, limite);
    }

    public List<AnalisisExpedienteDTO> buscarExpedientes(
            String textoLibre,
            String estadoCodigo,
            LocalDate fechaSolicitudDesde,
            LocalDate fechaSolicitudHasta,
            int limite) throws SQLException {
        return analisisExpedienteDAO.buscarExpedientes(
                textoLibre,
                estadoCodigo,
                fechaSolicitudDesde,
                fechaSolicitudHasta,
                limite);
    }

    public List<CatalogoItemDTO> listarResultadosAnalisis() throws SQLException {
        List<CatalogoItemDTO> items = new ArrayList<CatalogoItemDTO>();
        items.addAll(catalogoLookupDAO.listarResultadosEvaluacion());
        items.add(new CatalogoItemDTO("OBSERVADO", "Observado / requiere subsanación"));
        items.add(new CatalogoItemDTO("NO_CORRESPONDE", "No corresponde a SDRERC"));
        return items;
    }

    public List<CatalogoItemDTO> listarTiposObservacion() throws SQLException {
        return observacionExpedienteDAO.listarTiposObservacion();
    }

    public List<CatalogoItemDTO> listarMotivosNoCorresponde() throws SQLException {
        return catalogoLookupDAO.listarMotivosNoCorresponde();
    }

    public AnalisisResultadoDTO recibirExpediente(Long idExpediente, String comentario) throws SQLException {
        validacionService.validarExpedienteSeleccionado(idExpediente);
        return analisisExpedienteDAO.recibirExpediente(idExpediente, comentario, resolverUsuarioActualSdrercApp());
    }

    public AnalisisResultadoDTO recibirDocumentoAsociado(
            Long idExpedientePrincipal,
            Long idExpedienteAsociado,
            String comentario) throws SQLException {
        validacionService.validarExpedienteSeleccionado(idExpedientePrincipal);
        validacionService.validarExpedienteSeleccionado(idExpedienteAsociado);
        return analisisExpedienteDAO.recibirDocumentoAsociado(
                idExpedientePrincipal,
                idExpedienteAsociado,
                comentario,
                resolverUsuarioActualSdrercApp());
    }

    public boolean usuarioActualEsResponsable(Long idUsuarioResponsable) {
        if (idUsuarioResponsable == null) {
            return false;
        }
        Long idUsuarioActual = resolverUsuarioActualSdrercApp();
        return idUsuarioActual != null && idUsuarioActual.equals(idUsuarioResponsable);
    }

    public AnalisisResultadoDTO registrarAnalisis(AnalisisRegistroDTO registro) throws SQLException {
        List<String> errores = validacionService.validarRegistroAnalisis(registro);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
        return analisisExpedienteDAO.registrarAnalisis(registro, resolverUsuarioActualSdrercApp());
    }

    public AnalisisResultadoDTO enviarVerificacion(Long idExpediente, String comentario) throws SQLException {
        validacionService.validarExpedienteSeleccionado(idExpediente);
        return analisisExpedienteDAO.enviarVerificacion(idExpediente, comentario, resolverUsuarioActualSdrercApp());
    }

    public AnalisisResultadoDTO derivarNotificacionEspecial(Long idExpediente, String comentario) throws SQLException {
        validacionService.validarExpedienteSeleccionado(idExpediente);
        return analisisExpedienteDAO.derivarNotificacionEspecial(idExpediente, comentario, resolverUsuarioActualSdrercApp());
    }

    public AnalisisResultadoDTO archivarNoCorresponde(Long idExpediente, String comentario) throws SQLException {
        validacionService.validarExpedienteSeleccionado(idExpediente);
        if (comentario == null || comentario.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese el sustento para archivar por no corresponder.");
        }
        return analisisExpedienteDAO.archivarNoCorresponde(idExpediente, comentario, resolverUsuarioActualSdrercApp());
    }

    private Long resolverUsuarioActualSdrercApp() {
        if (usuarioActualResuelto) {
            return idUsuarioActualSdrercApp;
        }
        usuarioActualResuelto = true;
        try {
            String username = SessionContext.getUsername();
            idUsuarioActualSdrercApp = usuarioAsignacionService.obtenerIdUsuarioActivoPorUsername(username);
        } catch (Exception ex) {
            idUsuarioActualSdrercApp = null;
        }
        return idUsuarioActualSdrercApp;
    }
}
