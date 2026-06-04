package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.CargoAcuseDTO;
import com.sdrerc.domain.dto.sdrercapp.CierreNotificacionDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.PublicacionRequeridaDTO;
import java.util.ArrayList;
import java.util.List;

public class NotificacionValidacionService {

    public List<String> validarRegistro(NotificacionRegistroDTO registro) {
        List<String> errores = new ArrayList<String>();
        if (registro == null) {
            errores.add("Complete los datos de notificación.");
            return errores;
        }
        if (registro.getIdExpediente() == null) {
            errores.add("Seleccione un expediente.");
        }
        if (!hasText(registro.getAccionCodigo())) {
            errores.add("Seleccione la acción de notificación.");
        }
        if (!hasText(registro.getTipoNotificacionCodigo())) {
            errores.add("Seleccione el tipo de notificación.");
        }
        return errores;
    }

    public List<String> validarCargo(CargoAcuseDTO cargo) {
        List<String> errores = new ArrayList<String>();
        if (cargo == null) {
            errores.add("Complete los datos del cargo de acuse.");
            return errores;
        }
        if (cargo.getIdExpediente() == null) {
            errores.add("Seleccione un expediente.");
        }
        if (!hasText(cargo.getAccionCodigo())) {
            errores.add("Seleccione la acción de cargo.");
        }
        if (!hasText(cargo.getEstadoCargoCodigo())) {
            errores.add("Seleccione el estado del cargo.");
        }
        if (!hasText(cargo.getRecibidoPor())) {
            errores.add("Ingrese la persona que recibió el cargo.");
        }
        return errores;
    }

    public List<String> validarPublicacion(PublicacionRequeridaDTO publicacion) {
        List<String> errores = new ArrayList<String>();
        if (publicacion == null) {
            errores.add("Complete los datos de publicación.");
            return errores;
        }
        if (publicacion.getIdExpediente() == null) {
            errores.add("Seleccione un expediente.");
        }
        if (!hasText(publicacion.getAccionCodigo())) {
            errores.add("Seleccione la acción de publicación.");
        }
        if (!hasText(publicacion.getMotivo())) {
            errores.add("Ingrese el motivo de publicación.");
        }
        if (!hasText(publicacion.getComentario())) {
            errores.add("Ingrese el sustento de publicación.");
        }
        return errores;
    }

    public List<String> validarCierre(CierreNotificacionDTO cierre) {
        List<String> errores = new ArrayList<String>();
        if (cierre == null) {
            errores.add("Complete los datos de cierre.");
            return errores;
        }
        if (cierre.getIdExpediente() == null) {
            errores.add("Seleccione un expediente.");
        }
        if (!hasText(cierre.getAccionCodigo())) {
            errores.add("Seleccione la acción de cierre.");
        }
        return errores;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
