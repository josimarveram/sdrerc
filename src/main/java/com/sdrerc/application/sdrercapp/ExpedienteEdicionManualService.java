package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.ExpedienteEdicionManualDTO;
import com.sdrerc.domain.dto.sdrercapp.RegistroManualResultadoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteEdicionManualDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteRegistroDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExpedienteEdicionManualService {

    private final RegistroManualValidacionService validacionService;
    private final ExpedienteEdicionManualDAO edicionManualDAO;
    private final ExpedienteRegistroDAO expedienteRegistroDAO;
    private final UsuarioAsignacionService usuarioAsignacionService = new UsuarioAsignacionService();

    public ExpedienteEdicionManualService() {
        this(new RegistroManualValidacionService(), new ExpedienteEdicionManualDAO(), new ExpedienteRegistroDAO());
    }

    public ExpedienteEdicionManualService(
            RegistroManualValidacionService validacionService,
            ExpedienteEdicionManualDAO edicionManualDAO,
            ExpedienteRegistroDAO expedienteRegistroDAO) {
        this.validacionService = validacionService;
        this.edicionManualDAO = edicionManualDAO;
        this.expedienteRegistroDAO = expedienteRegistroDAO;
    }

    public ExpedienteEdicionManualDTO obtenerParaEdicion(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente para editar.");
        }
        return edicionManualDAO.obtenerParaEdicion(idExpediente);
    }

    public ExpedienteEdicionManualDTO obtenerParaEdicionDesdeAnalisis(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente para editar.");
        }
        return edicionManualDAO.obtenerParaEdicionDesdeAnalisis(idExpediente);
    }

    public List<String> validar(ExpedienteEdicionManualDTO dto) throws SQLException {
        List<String> errores = new ArrayList<String>();
        if (dto == null || dto.getIdExpediente() == null) {
            errores.add("Seleccione un expediente para editar.");
            return errores;
        }
        errores.addAll(validacionService.validar(dto));

        String numeroExpedienteSgd = dto.getSolicitud().getNumeroExpedienteSgd();
        if (hasText(numeroExpedienteSgd)) {
            String duplicado = expedienteRegistroDAO.detectarDuplicadoPorNumeroExpedienteSgd(
                    numeroExpedienteSgd, dto.getIdExpediente());
            if (hasText(duplicado)) {
                errores.add("N° expediente SGD ya está registrado en " + duplicado + ". Ingrese un número distinto.");
            }
        }
        return errores;
    }

    public RegistroManualResultadoDTO guardar(ExpedienteEdicionManualDTO dto) throws SQLException {
        List<String> errores = validar(dto);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(" | ", errores));
        }
        return edicionManualDAO.guardar(dto);
    }

    public RegistroManualResultadoDTO guardarDesdeAnalisis(ExpedienteEdicionManualDTO dto) throws SQLException {
        List<String> errores = validar(dto);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(" | ", errores));
        }
        return edicionManualDAO.guardarDesdeAnalisis(dto);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public RegistroManualResultadoDTO eliminar(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente para eliminar.");
        }
        return edicionManualDAO.eliminar(idExpediente, resolverUsuarioActualSdrercApp());
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
