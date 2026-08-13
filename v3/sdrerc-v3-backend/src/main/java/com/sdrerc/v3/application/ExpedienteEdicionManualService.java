package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.ExpedienteEdicionManualDTO;
import com.sdrerc.v3.domain.RegistroManualResultadoDTO;
import com.sdrerc.v3.infrastructure.dao.ExpedienteEdicionManualDAO;
import com.sdrerc.v3.infrastructure.dao.ExpedienteRegistroDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Port literal de com.sdrerc.application.sdrercapp.ExpedienteEdicionManualService (V2), acotado al
 * camino de edición desde Registro/Recepción (sin {@code obtenerParaEdicionDesdeAnalisis}/
 * {@code guardarDesdeAnalisis}/{@code eliminar} — ver Javadoc de {@link ExpedienteEdicionManualDAO}).
 * Única diferencia real: {@code guardar()} resuelve el usuario actual desde {@link CurrentUser}
 * (JWT del request) en vez de {@code SessionContext} estático, mismo patrón ya aplicado en
 * Registro manual/Carga diaria.
 */
@Service
public class ExpedienteEdicionManualService {

    private final RegistroManualValidacionService validacionService;
    private final ExpedienteEdicionManualDAO edicionManualDAO;
    private final ExpedienteRegistroDAO expedienteRegistroDAO;

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

    /** Solo lectura, sin exigir editable — usado por el panel "Datos" (ver Javadoc del DAO). */
    public ExpedienteEdicionManualDTO obtenerDatos(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        return edicionManualDAO.obtenerDatos(idExpediente);
    }

    public List<String> validar(ExpedienteEdicionManualDTO dto) throws SQLException {
        List<String> errores = new ArrayList<>();
        if (dto == null || dto.getIdExpediente() == null) {
            errores.add("Seleccione un expediente para editar.");
            return errores;
        }
        errores.addAll(validacionService.validar(dto));
        dto.setPosibleDuplicado(false);
        dto.setMotivoDuplicado(null);

        String numeroExpedienteSgd = dto.getSolicitud().getNumeroExpedienteSgd();
        if (hasText(numeroExpedienteSgd)) {
            String duplicado = expedienteRegistroDAO.detectarDuplicadoPorNumeroExpedienteSgd(
                    numeroExpedienteSgd, dto.getIdExpediente());
            if (hasText(duplicado)) {
                errores.add("N° expediente SGD ya está registrado en " + duplicado + ". Ingrese un número distinto.");
            }
        }

        String numeroActa = dto.getActa().getNumeroActa();
        String titular = dto.getTitular().getNombreCompleto();
        if (hasText(numeroActa) && hasText(titular)) {
            String duplicadoActaTitular = expedienteRegistroDAO.detectarDuplicadoPorActaYTitular(
                    numeroActa, titular, dto.getIdExpediente());
            if (hasText(duplicadoActaTitular)) {
                dto.setPosibleDuplicado(true);
                dto.setMotivoDuplicado(duplicadoActaTitular);
            }
        }
        return errores;
    }

    public RegistroManualResultadoDTO guardar(ExpedienteEdicionManualDTO dto) throws SQLException {
        List<String> errores = validar(dto);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(" | ", errores));
        }
        return edicionManualDAO.guardar(dto, CurrentUser.get().idUsuario());
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
