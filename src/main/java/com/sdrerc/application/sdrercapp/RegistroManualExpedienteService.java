package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.RegistroManualExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.RegistroManualResultadoDTO;
import com.sdrerc.domain.rules.ProcedimientoRegistralRules;
import com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteRegistroDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegistroManualExpedienteService {

    private final RegistroManualValidacionService validacionService;
    private final ExpedienteRegistroDAO expedienteRegistroDAO;
    private final CorrelativoExpedienteService correlativoExpedienteService;

    public RegistroManualExpedienteService() {
        this(new RegistroManualValidacionService(), new ExpedienteRegistroDAO(), new CorrelativoExpedienteService());
    }

    public RegistroManualExpedienteService(
            RegistroManualValidacionService validacionService,
            ExpedienteRegistroDAO expedienteRegistroDAO,
            CorrelativoExpedienteService correlativoExpedienteService) {
        this.validacionService = validacionService;
        this.expedienteRegistroDAO = expedienteRegistroDAO;
        this.correlativoExpedienteService = correlativoExpedienteService;
    }

    public List<String> validar(RegistroManualExpedienteDTO registro) {
        return validacionService.validar(registro);
    }

    public List<String> validarConDuplicados(RegistroManualExpedienteDTO registro) throws SQLException {
        List<String> mensajes = new ArrayList<String>(validar(registro));
        if (registro == null) {
            return mensajes;
        }
        registro.setPosibleDuplicado(false);
        registro.setMotivoDuplicado(null);
        registro.getSolicitud().limpiarDeteccionGrupoFamiliar();
        if (registro.getSolicitud().isGrupoFamiliar() && !hasText(registro.getSolicitud().getCriterioGrupoFamiliar())) {
            registro.getSolicitud().setCriterioGrupoFamiliar("MANUAL");
        }

        String numeroActa = registro.getActa().getNumeroActa();
        String titular = registro.getTitular().getNombreCompleto();
        if (!hasText(titular)) {
            return mensajes;
        }

        if (hasText(numeroActa)) {
            String duplicado = expedienteRegistroDAO.detectarDuplicadoPorActaYTitular(numeroActa, titular);
            if (hasText(duplicado)) {
                String motivo = "Acta y titular ya existen en " + duplicado;
                registro.setPosibleDuplicado(true);
                registro.setMotivoDuplicado(motivo);
                mensajes.add("Documento duplicado: " + motivo
                        + ". Se guardará sin número de expediente y quedará marcado para Asignación.");
            }
        }
        if (ProcedimientoRegistralRules.requiereDecisionAsignacionParaNumero(
                registro.getSolicitud().getTipoProcedimientoNombre())) {
            mensajes.add(ProcedimientoRegistralRules.mensajeSinNumeroRecepcion());
        }

        String grupoFamiliar = expedienteRegistroDAO.detectarPosibleGrupoFamiliarPorTitular(titular, null);
        if (hasText(grupoFamiliar)) {
            registro.getSolicitud().agregarObservacionGrupoFamiliar("COINCIDENCIA_APELLIDOS_BD", grupoFamiliar);
            mensajes.add(grupoFamiliar);
        }
        return mensajes;
    }

    public RegistroManualResultadoDTO registrar(RegistroManualExpedienteDTO registro) throws SQLException {
        if (registro == null) {
            throw new IllegalArgumentException("Complete los datos del formulario antes de registrar.");
        }
        List<String> errores = validarConDuplicados(registro);
        if (!errores.isEmpty()) {
            registro.setObservacionesGenerales(unirObservaciones(
                    registro.getObservacionesGenerales(),
                    "Advertencias de validación: " + String.join(" | ", errores)));
        }
        return expedienteRegistroDAO.registrarManual(registro, correlativoExpedienteService);
    }

    private String unirObservaciones(String actual, String nueva) {
        if (actual == null || actual.trim().isEmpty()) {
            return nueva;
        }
        if (nueva == null || nueva.trim().isEmpty()) {
            return actual;
        }
        if (actual.contains(nueva)) {
            return actual;
        }
        return actual.trim() + " | " + nueva.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
