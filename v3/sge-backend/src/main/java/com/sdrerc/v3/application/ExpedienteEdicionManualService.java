package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.ExpedienteEdicionManualDTO;
import com.sdrerc.v3.domain.ImpactoEliminacionRegistroDTO;
import com.sdrerc.v3.domain.RegistroManualResultadoDTO;
import com.sdrerc.v3.infrastructure.dao.ExpedienteEdicionManualDAO;
import com.sdrerc.v3.infrastructure.dao.ExpedienteRegistroDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Port literal de com.sdrerc.application.sdrercapp.ExpedienteEdicionManualService (V2), con el
 * camino de edición/baja desde Registro/Recepción MÁS el overload {@code desdeAnalisis} agregado
 * 21/08/2026 (pedido explícito del usuario) — ver Javadoc de {@link ExpedienteEdicionManualDAO}.
 * Única diferencia real: {@code guardar()}/{@code eliminar()} resuelven el usuario actual desde
 * {@link CurrentUser} (JWT del request) en vez de {@code SessionContext} estático, mismo patrón ya
 * aplicado en Registro manual/Carga diaria.
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
        return obtenerParaEdicion(idExpediente, false);
    }

    /**
     * {@code desdeAnalisis=true} omite la exigencia de etapa REGISTRO/estado REGISTRADO/sin
     * asignación (21/08/2026, pedido explícito del usuario: habilitar el lápiz de edición del panel
     * "Datos" dentro del módulo Análisis) — ver Javadoc de {@link ExpedienteEdicionManualDAO}.
     */
    public ExpedienteEdicionManualDTO obtenerParaEdicion(Long idExpediente, boolean desdeAnalisis) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente para editar.");
        }
        return desdeAnalisis
                ? edicionManualDAO.obtenerParaEdicionDesdeAnalisis(idExpediente)
                : edicionManualDAO.obtenerParaEdicion(idExpediente);
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
        dto.setNumeroExpedienteSgdDuplicado(false);
        dto.setMotivoNumeroExpedienteSgdDuplicado(null);

        String numeroExpedienteSgd = dto.getSolicitud().getNumeroExpedienteSgd();
        if (hasText(numeroExpedienteSgd)) {
            String duplicado = expedienteRegistroDAO.detectarDuplicadoPorNumeroExpedienteSgd(
                    numeroExpedienteSgd, dto.getIdExpediente());
            if (hasText(duplicado)) {
                // Bug corregido (13/08/2026, pedido explícito del usuario): antes solo se agregaba
                // el mensaje a `errores` (texto genérico en el Alert superior), sin marcar
                // `numeroExpedienteSgdDuplicado`/`motivoNumeroExpedienteSgdDuplicado` en el DTO —
                // a diferencia de `RegistroManualExpedienteService.validarConDuplicados`, que sí
                // marca esos 2 campos. Sin esto, `RegistroExpedienteForm` (compartido con
                // Registro manual) nunca resaltaba el campo "N° expediente SGD" en rojo con su
                // `helperText` para Edición manual/el panel de datos inline, aunque el backend ya
                // detectaba el duplicado correctamente.
                String motivo = "N° expediente SGD ya está registrado en " + duplicado + ".";
                dto.setNumeroExpedienteSgdDuplicado(true);
                dto.setMotivoNumeroExpedienteSgdDuplicado(motivo);
                errores.add(motivo + " Ingrese un número distinto.");
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
        return guardar(dto, false);
    }

    /** {@code desdeAnalisis=true} — ver Javadoc de {@link #obtenerParaEdicion(Long, boolean)}. */
    public RegistroManualResultadoDTO guardar(ExpedienteEdicionManualDTO dto, boolean desdeAnalisis) throws SQLException {
        List<String> errores = validar(dto);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(" | ", errores));
        }
        return desdeAnalisis
                ? edicionManualDAO.guardarDesdeAnalisis(dto, CurrentUser.get().idUsuario())
                : edicionManualDAO.guardar(dto, CurrentUser.get().idUsuario());
    }

    public RegistroManualResultadoDTO eliminar(Long idExpediente) throws SQLException {
        return edicionManualDAO.eliminar(idExpediente, CurrentUser.get().idUsuario());
    }

    /** Info de impacto para el modal de confirmación de eliminación — ver Javadoc del DAO. */
    public ImpactoEliminacionRegistroDTO obtenerImpactoEliminacion(Long idExpediente) throws SQLException {
        return edicionManualDAO.obtenerImpactoEliminacion(idExpediente);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
