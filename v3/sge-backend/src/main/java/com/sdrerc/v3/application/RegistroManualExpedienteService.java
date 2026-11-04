package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.RegistroManualExpedienteDTO;
import com.sdrerc.v3.domain.RegistroManualResultadoDTO;
import com.sdrerc.v3.domain.rules.ProcedimientoRegistralRules;
import com.sdrerc.v3.infrastructure.dao.ExpedienteRegistroDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Port casi literal de com.sdrerc.application.sdrercapp.RegistroManualExpedienteService (V2).
 * Única diferencia real: {@code registrar()} resuelve el usuario actual desde
 * {@link CurrentUser} (JWT del request) en vez de que el DAO lo lea de un {@code SessionContext}
 * estático — mismo patrón ya aplicado en Fase 0/1.
 */
@Service
public class RegistroManualExpedienteService {

    private final RegistroManualValidacionService validacionService;
    private final ExpedienteRegistroDAO expedienteRegistroDAO;
    private final CorrelativoExpedienteService correlativoExpedienteService;

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
        List<String> mensajes = new ArrayList<>(validar(registro));
        if (registro == null) {
            return mensajes;
        }
        registro.setPosibleDuplicado(false);
        registro.setMotivoDuplicado(null);
        registro.setNumeroExpedienteSgdDuplicado(false);
        registro.setMotivoNumeroExpedienteSgdDuplicado(null);
        registro.getSolicitud().limpiarDeteccionGrupoFamiliar();
        if (registro.getSolicitud().isGrupoFamiliar() && !hasText(registro.getSolicitud().getCriterioGrupoFamiliar())) {
            registro.getSolicitud().setCriterioGrupoFamiliar("MANUAL");
        }

        String numeroExpedienteSgd = registro.getSolicitud().getNumeroExpedienteSgd();
        if (hasText(numeroExpedienteSgd)) {
            String duplicadoSgd = expedienteRegistroDAO.detectarDuplicadoPorNumeroExpedienteSgd(numeroExpedienteSgd, null);
            if (hasText(duplicadoSgd)) {
                String motivo = "N° expediente SGD ya está registrado en " + duplicadoSgd + ".";
                registro.setNumeroExpedienteSgdDuplicado(true);
                registro.setMotivoNumeroExpedienteSgdDuplicado(motivo);
                mensajes.add(motivo + " Ingrese un número distinto.");
            }
        }

        String numeroActa = registro.getActa().getNumeroActa();
        String titular = registro.getTitular().getNombreCompleto();
        if (!hasText(titular)) {
            return mensajes;
        }

        if (hasText(numeroActa)) {
            String duplicado = expedienteRegistroDAO.detectarDuplicadoPorActaYTitular(numeroActa, titular);
            if (hasText(duplicado)) {
                registro.setPosibleDuplicado(true);
                registro.setMotivoDuplicado(duplicado);
                mensajes.add("Documento duplicado: " + duplicado
                        + " Se guardará sin número de expediente y quedará marcado para Asignación.");
            }
        }
        if (ProcedimientoRegistralRules.requiereDecisionAsignacionParaNumero(
                registro.getSolicitud().getTipoProcedimientoNombre())) {
            mensajes.add(ProcedimientoRegistralRules.mensajeSinNumeroRecepcion());
        }

        if (!registro.isPosibleDuplicado()) {
            String grupoFamiliar = expedienteRegistroDAO.detectarPosibleGrupoFamiliarPorTitular(titular, null);
            if (hasText(grupoFamiliar)) {
                registro.getSolicitud().agregarObservacionGrupoFamiliar("COINCIDENCIA_APELLIDOS_BD", grupoFamiliar);
                mensajes.add(grupoFamiliar);
            }
        }
        return mensajes;
    }

    public RegistroManualResultadoDTO registrar(RegistroManualExpedienteDTO registro) throws SQLException {
        if (registro == null) {
            throw new IllegalArgumentException("Complete los datos del formulario antes de registrar.");
        }
        List<String> errores = validarConDuplicados(registro);
        if (registro.isNumeroExpedienteSgdDuplicado()) {
            throw new IllegalArgumentException(registro.getMotivoNumeroExpedienteSgdDuplicado());
        }
        if (!errores.isEmpty()) {
            registro.setObservacionesGenerales(unirObservaciones(
                    registro.getObservacionesGenerales(),
                    "Advertencias de validación: " + String.join(" | ", errores)));
        }
        return expedienteRegistroDAO.registrarManual(registro, correlativoExpedienteService, CurrentUser.get().idUsuario());
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
