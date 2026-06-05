package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.CierrePublicacionDTO;
import com.sdrerc.domain.dto.sdrercapp.PublicacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.PublicacionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.PublicacionResultadoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.PublicacionExpedienteDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PublicacionExpedienteService {

    public static final String ACCION_REGISTRO_PUBLICACION = "REGISTRO_PUBLICACION";
    public static final String ACCION_CIERRE = "CIERRE";

    private final PublicacionExpedienteDAO publicacionExpedienteDAO;
    private final PublicacionValidacionService validacionService;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public PublicacionExpedienteService() {
        this(new PublicacionExpedienteDAO(), new PublicacionValidacionService(), new UsuarioAsignacionService());
    }

    public PublicacionExpedienteService(
            PublicacionExpedienteDAO publicacionExpedienteDAO,
            PublicacionValidacionService validacionService,
            UsuarioAsignacionService usuarioAsignacionService) {
        this.publicacionExpedienteDAO = publicacionExpedienteDAO;
        this.validacionService = validacionService;
        this.usuarioAsignacionService = usuarioAsignacionService;
    }

    public List<PublicacionExpedienteDTO> buscarExpedientes(String textoLibre, String estadoCodigo, int limite) throws SQLException {
        return publicacionExpedienteDAO.buscarExpedientes(textoLibre, estadoCodigo, limite);
    }

    public PublicacionResultadoDTO registrarPublicacion(PublicacionRegistroDTO registro) throws SQLException {
        PublicacionRegistroDTO normalizado = normalizarRegistro(registro);
        validarRegistro(normalizado);
        return publicacionExpedienteDAO.registrarPublicacion(normalizado, resolverUsuarioActualSdrercApp());
    }

    public PublicacionResultadoDTO cerrarExpediente(CierrePublicacionDTO cierre) throws SQLException {
        validarCierre(cierre);
        return publicacionExpedienteDAO.cerrarExpediente(cierre, resolverUsuarioActualSdrercApp());
    }

    private PublicacionRegistroDTO normalizarRegistro(PublicacionRegistroDTO registro) {
        LocalDate fecha = registro == null || registro.getFechaPublicacion() == null
                ? LocalDate.now()
                : registro.getFechaPublicacion();
        return new PublicacionRegistroDTO(
                registro == null ? null : registro.getIdExpediente(),
                registro == null ? "" : registro.getAccionCodigo(),
                registro == null || registro.getTipoPublicacion().trim().isEmpty()
                        ? "NOTIFICACION_FALLIDA"
                        : registro.getTipoPublicacion(),
                fecha,
                registro == null ? "" : registro.getMedioPublicacion(),
                registro == null ? "" : registro.getNumeroPublicacion(),
                registro == null ? "" : registro.getResultadoPublicacion(),
                registro == null ? "" : registro.getComentario());
    }

    private void validarRegistro(PublicacionRegistroDTO registro) {
        List<String> errores = validacionService.validarRegistro(registro);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
    }

    private void validarCierre(CierrePublicacionDTO cierre) {
        List<String> errores = validacionService.validarCierre(cierre);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
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
