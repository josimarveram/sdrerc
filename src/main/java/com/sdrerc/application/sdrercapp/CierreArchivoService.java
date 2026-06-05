package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.ArchivoExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.CierreArchivoExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.CierreArchivoResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CierreExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteTimelineDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.CierreArchivoDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.List;

public class CierreArchivoService {

    public static final String ACCION_CIERRE = "CIERRE";
    public static final String ACCION_ARCHIVO = "ARCHIVO";

    private final CierreArchivoDAO cierreArchivoDAO;
    private final CierreArchivoValidacionService validacionService;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public CierreArchivoService() {
        this(new CierreArchivoDAO(), new CierreArchivoValidacionService(), new UsuarioAsignacionService());
    }

    public CierreArchivoService(
            CierreArchivoDAO cierreArchivoDAO,
            CierreArchivoValidacionService validacionService,
            UsuarioAsignacionService usuarioAsignacionService) {
        this.cierreArchivoDAO = cierreArchivoDAO;
        this.validacionService = validacionService;
        this.usuarioAsignacionService = usuarioAsignacionService;
    }

    public List<CierreArchivoExpedienteDTO> buscarExpedientes(String textoLibre, String estadoCodigo, int limite) throws SQLException {
        return cierreArchivoDAO.buscarExpedientes(textoLibre, estadoCodigo, limite);
    }

    public CierreArchivoResultadoDTO registrarCierre(CierreExpedienteDTO cierre) throws SQLException {
        CierreExpedienteDTO normalizado = new CierreExpedienteDTO(
                cierre == null ? null : cierre.getIdExpediente(),
                ACCION_CIERRE,
                cierre == null ? "" : cierre.getMotivo(),
                cierre == null ? "" : cierre.getComentario());
        validarCierre(normalizado);
        return cierreArchivoDAO.registrarCierre(normalizado, resolverUsuarioActualSdrercApp());
    }

    public CierreArchivoResultadoDTO registrarArchivo(ArchivoExpedienteDTO archivo) throws SQLException {
        ArchivoExpedienteDTO normalizado = new ArchivoExpedienteDTO(
                archivo == null ? null : archivo.getIdExpediente(),
                ACCION_ARCHIVO,
                archivo == null ? "" : archivo.getMotivo(),
                archivo == null ? "" : archivo.getComentario());
        validarArchivo(normalizado);
        return cierreArchivoDAO.registrarArchivo(normalizado, resolverUsuarioActualSdrercApp());
    }

    public List<ExpedienteTimelineDTO> listarHistorial(Long idExpediente) throws SQLException {
        return cierreArchivoDAO.listarHistorial(idExpediente);
    }

    private void validarCierre(CierreExpedienteDTO cierre) {
        List<String> errores = validacionService.validarCierre(cierre);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
    }

    private void validarArchivo(ArchivoExpedienteDTO archivo) {
        List<String> errores = validacionService.validarArchivo(archivo);
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
