package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.ExpedienteDigitalDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteDigitalFiltroDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteDigitalRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteDigitalResultadoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteDigitalDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.List;

public class ExpedienteDigitalService {

    public static final String ACCION_CREACION_CARPETA = "CREACION_CARPETA_EXPEDIENTE_DIGITAL";
    public static final String ACCION_REGISTRO_LINK = "REGISTRO_LINK_EXPEDIENTE_DIGITAL";
    public static final String ACCION_CARGA_DOCUMENTOS = "CARGA_DOCUMENTOS_EXPEDIENTE_DIGITAL";

    private final ExpedienteDigitalDAO expedienteDigitalDAO;
    private final ExpedienteDigitalValidacionService validacionService;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public ExpedienteDigitalService() {
        this(new ExpedienteDigitalDAO(), new ExpedienteDigitalValidacionService(), new UsuarioAsignacionService());
    }

    public ExpedienteDigitalService(
            ExpedienteDigitalDAO expedienteDigitalDAO,
            ExpedienteDigitalValidacionService validacionService,
            UsuarioAsignacionService usuarioAsignacionService) {
        this.expedienteDigitalDAO = expedienteDigitalDAO;
        this.validacionService = validacionService;
        this.usuarioAsignacionService = usuarioAsignacionService;
    }

    public List<ExpedienteDigitalDTO> buscarExpedientes(String textoLibre, String estadoCodigo, int limite) throws SQLException {
        return expedienteDigitalDAO.buscarExpedientes(new ExpedienteDigitalFiltroDTO(textoLibre, estadoCodigo, limite));
    }

    public ExpedienteDigitalResultadoDTO registrarCarpeta(ExpedienteDigitalRegistroDTO registro) throws SQLException {
        ExpedienteDigitalRegistroDTO normalizado = normalizar(registro, ACCION_CREACION_CARPETA);
        validarCarpeta(normalizado);
        return expedienteDigitalDAO.registrarCarpeta(normalizado, resolverUsuarioActualSdrercApp());
    }

    public ExpedienteDigitalResultadoDTO registrarEnlace(ExpedienteDigitalRegistroDTO registro) throws SQLException {
        ExpedienteDigitalRegistroDTO normalizado = normalizar(registro, ACCION_CREACION_CARPETA);
        validarEnlace(normalizado);
        return expedienteDigitalDAO.registrarEnlace(normalizado, resolverUsuarioActualSdrercApp());
    }

    public ExpedienteDigitalResultadoDTO marcarCompleto(ExpedienteDigitalRegistroDTO registro) throws SQLException {
        ExpedienteDigitalRegistroDTO normalizado = normalizar(registro, ACCION_CARGA_DOCUMENTOS);
        validarMarcadoCompleto(normalizado);
        return expedienteDigitalDAO.marcarCompleto(normalizado, resolverUsuarioActualSdrercApp());
    }

    private ExpedienteDigitalRegistroDTO normalizar(ExpedienteDigitalRegistroDTO registro, String accionDefecto) {
        return new ExpedienteDigitalRegistroDTO(
                registro == null ? null : registro.getIdExpediente(),
                accionDefecto,
                registro == null ? "" : registro.getCodigoExpedienteDigital(),
                registro == null ? "" : registro.getRutaCarpeta(),
                registro == null ? "" : registro.getEnlaceCarpeta(),
                registro == null ? "" : registro.getComentario());
    }

    private void validarCarpeta(ExpedienteDigitalRegistroDTO registro) {
        List<String> errores = validacionService.validarCarpeta(registro);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
    }

    private void validarEnlace(ExpedienteDigitalRegistroDTO registro) {
        List<String> errores = validacionService.validarEnlace(registro);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
    }

    private void validarMarcadoCompleto(ExpedienteDigitalRegistroDTO registro) {
        List<String> errores = validacionService.validarMarcadoCompleto(registro);
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
