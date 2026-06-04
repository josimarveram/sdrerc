package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.CargoAcuseDTO;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.CierreNotificacionDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.PublicacionRequeridaDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.CatalogoLookupDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.NotificacionExpedienteDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class NotificacionExpedienteService {

    public static final String ACCION_NOTIFICACION_VIRTUAL = "NOTIFICACION_VIRTUAL";
    public static final String ACCION_NOTIFICACION_PRESENCIAL_1 = "NOTIFICACION_PRESENCIAL_1";
    public static final String ACCION_NOTIFICACION_PRESENCIAL_2 = "NOTIFICACION_PRESENCIAL_2";
    public static final String ACCION_RECEPCION_CARGO = "RECEPCION_CARGO_ACUSE";
    public static final String ACCION_CONFIRMACION = "CONFIRMACION_NOTIFICACION";
    public static final String ACCION_NOTIFICACION_FALLIDA = "REGISTRO_NOTIFICACION_FALLIDA";
    public static final String ACCION_GENERACION_PUBLICACION = "GENERACION_PUBLICACION";
    public static final String ACCION_CIERRE = "CIERRE";

    private final NotificacionExpedienteDAO notificacionExpedienteDAO;
    private final NotificacionValidacionService validacionService;
    private final UsuarioAsignacionService usuarioAsignacionService;
    private final CatalogoLookupDAO catalogoLookupDAO;

    public NotificacionExpedienteService() {
        this(
                new NotificacionExpedienteDAO(),
                new NotificacionValidacionService(),
                new UsuarioAsignacionService(),
                new CatalogoLookupDAO());
    }

    public NotificacionExpedienteService(
            NotificacionExpedienteDAO notificacionExpedienteDAO,
            NotificacionValidacionService validacionService,
            UsuarioAsignacionService usuarioAsignacionService,
            CatalogoLookupDAO catalogoLookupDAO) {
        this.notificacionExpedienteDAO = notificacionExpedienteDAO;
        this.validacionService = validacionService;
        this.usuarioAsignacionService = usuarioAsignacionService;
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    public List<NotificacionExpedienteDTO> buscarExpedientes(String textoLibre, String estadoCodigo, int limite) throws SQLException {
        return notificacionExpedienteDAO.buscarExpedientes(textoLibre, estadoCodigo, limite);
    }

    public List<CatalogoItemDTO> listarTiposNotificacion() throws SQLException {
        return catalogoLookupDAO.listarTiposNotificacion();
    }

    public List<CatalogoItemDTO> listarEstadosCargoAcuse() throws SQLException {
        return catalogoLookupDAO.listarEstadosCargoAcuse();
    }

    public NotificacionResultadoDTO registrarNotificacion(NotificacionRegistroDTO registro) throws SQLException {
        NotificacionRegistroDTO normalizado = normalizarRegistro(registro);
        validarRegistro(normalizado);
        return notificacionExpedienteDAO.registrarNotificacion(normalizado, resolverUsuarioActualSdrercApp());
    }

    public NotificacionResultadoDTO registrarCargo(CargoAcuseDTO cargo) throws SQLException {
        CargoAcuseDTO normalizado = normalizarCargo(cargo);
        validarCargo(normalizado);
        return notificacionExpedienteDAO.registrarCargo(normalizado, resolverUsuarioActualSdrercApp());
    }

    public NotificacionResultadoDTO marcarNotificado(NotificacionRegistroDTO registro) throws SQLException {
        NotificacionRegistroDTO normalizado = normalizarRegistro(registro);
        return notificacionExpedienteDAO.marcarNotificado(normalizado, resolverUsuarioActualSdrercApp());
    }

    public NotificacionResultadoDTO registrarPublicacion(PublicacionRequeridaDTO publicacion) throws SQLException {
        validarPublicacion(publicacion);
        return notificacionExpedienteDAO.registrarPublicacion(publicacion, resolverUsuarioActualSdrercApp());
    }

    public NotificacionResultadoDTO cerrarExpediente(CierreNotificacionDTO cierre) throws SQLException {
        validarCierre(cierre);
        return notificacionExpedienteDAO.cerrarExpediente(cierre, resolverUsuarioActualSdrercApp());
    }

    private void validarRegistro(NotificacionRegistroDTO registro) {
        List<String> errores = validacionService.validarRegistro(registro);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
    }

    private void validarCargo(CargoAcuseDTO cargo) {
        List<String> errores = validacionService.validarCargo(cargo);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
    }

    private void validarPublicacion(PublicacionRequeridaDTO publicacion) {
        List<String> errores = validacionService.validarPublicacion(publicacion);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
    }

    private void validarCierre(CierreNotificacionDTO cierre) {
        List<String> errores = validacionService.validarCierre(cierre);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
    }

    private NotificacionRegistroDTO normalizarRegistro(NotificacionRegistroDTO registro) {
        LocalDate fecha = registro == null || registro.getFechaNotificacion() == null
                ? LocalDate.now()
                : registro.getFechaNotificacion();
        return new NotificacionRegistroDTO(
                registro == null ? null : registro.getIdExpediente(),
                registro == null ? "" : registro.getAccionCodigo(),
                registro == null ? "" : registro.getTipoNotificacionCodigo(),
                fecha,
                registro == null ? "" : registro.getResultado(),
                registro == null ? "" : registro.getDestinatario(),
                registro == null ? "" : registro.getComentario());
    }

    private CargoAcuseDTO normalizarCargo(CargoAcuseDTO cargo) {
        LocalDate fecha = cargo == null || cargo.getFechaCargo() == null ? LocalDate.now() : cargo.getFechaCargo();
        return new CargoAcuseDTO(
                cargo == null ? null : cargo.getIdExpediente(),
                cargo == null ? "" : cargo.getAccionCodigo(),
                cargo == null || cargo.getEstadoCargoCodigo().trim().isEmpty() ? "CARGO_RECIBIDO" : cargo.getEstadoCargoCodigo(),
                fecha,
                cargo == null ? "" : cargo.getRecibidoPor(),
                cargo == null ? "" : cargo.getComentario());
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
