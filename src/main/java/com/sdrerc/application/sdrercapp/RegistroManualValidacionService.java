package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.DatosActaDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosNotificacionDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosPersonaRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosSolicitudDTO;
import com.sdrerc.domain.dto.sdrercapp.RegistroManualExpedienteDTO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegistroManualValidacionService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+()\\-\\s]{6,30}$");
    private static final Pattern DOCUMENT_PATTERN = Pattern.compile("^[A-Za-z0-9\\-]{4,30}$");

    public List<String> validar(RegistroManualExpedienteDTO registro) {
        List<String> errores = new ArrayList<>();
        if (registro == null) {
            errores.add("Complete los datos del formulario antes de registrar.");
            return errores;
        }

        validarSolicitud(registro.getSolicitud(), errores);
        validarActa(registro.getActa(), errores);
        validarPersona("Titular", registro.getTitular(), true, errores);
        validarPersona("Remitente", registro.getRemitente(), true, errores);
        validarNotificacion(registro.getNotificacion(), errores);
        return errores;
    }

    private void validarSolicitud(DatosSolicitudDTO solicitud, List<String> errores) {
        if (!hasText(solicitud.getNumeroTramite())) {
            errores.add("Número de trámite obligatorio.");
        }
        if (solicitud.getFechaRecepcion() == null) {
            errores.add("Fecha de recepción obligatoria o inválida.");
        } else if (solicitud.getFechaRecepcion().isAfter(LocalDate.now())) {
            errores.add("La fecha de recepción no puede ser futura.");
        }
        if (!hasText(solicitud.getTipoProcedimientoNombre())) {
            errores.add("Tipo de procedimiento obligatorio.");
        }
        if (!hasText(solicitud.getTipoDocumentoNombre())) {
            errores.add("Tipo de documento obligatorio.");
        }
    }

    private void validarActa(DatosActaDTO acta, List<String> errores) {
        if (!hasText(acta.getNumeroActa())) {
            errores.add("Número o referencia de acta obligatorio.");
        }
        if (acta.getAnioActa() != null && (acta.getAnioActa() < 1900 || acta.getAnioActa() > LocalDate.now().getYear() + 1)) {
            errores.add("Año del acta fuera de rango.");
        }
    }

    private void validarPersona(String etiqueta, DatosPersonaRegistroDTO persona, boolean obligatorio, List<String> errores) {
        if (obligatorio && !hasText(persona.getNombreCompleto())) {
            errores.add(etiqueta + " obligatorio.");
        }
        if (hasText(persona.getNumeroDocumento()) && !DOCUMENT_PATTERN.matcher(persona.getNumeroDocumento()).matches()) {
            errores.add("Documento de " + etiqueta.toLowerCase() + " inválido.");
        }
        if (hasText(persona.getCorreo()) && !EMAIL_PATTERN.matcher(persona.getCorreo()).matches()) {
            errores.add("Correo de " + etiqueta.toLowerCase() + " inválido.");
        }
        if (hasText(persona.getTelefono()) && !PHONE_PATTERN.matcher(persona.getTelefono()).matches()) {
            errores.add("Teléfono de " + etiqueta.toLowerCase() + " inválido.");
        }
    }

    private void validarNotificacion(DatosNotificacionDTO notificacion, List<String> errores) {
        if (notificacion == null || !notificacion.requiereRegistroNotificacion()) {
            return;
        }
        if (notificacion.requiereVirtual()) {
            if (!hasText(notificacion.getCorreo())) {
                errores.add("Correo de notificación obligatorio para notificación virtual.");
            } else if (!EMAIL_PATTERN.matcher(notificacion.getCorreo()).matches()) {
                errores.add("Correo de notificación inválido.");
            }
        }
        if (notificacion.requiereFisica() && !hasText(notificacion.getDireccion())) {
            errores.add("Dirección de notificación obligatoria para notificación física/presencial.");
        }
        if (hasText(notificacion.getTelefono()) && !PHONE_PATTERN.matcher(notificacion.getTelefono()).matches()) {
            errores.add("Teléfono de notificación inválido.");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
