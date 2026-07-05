package com.sdrerc.ui.appv2.components;

import com.sdrerc.application.sdrercapp.PlazoConfiguracionService;
import com.sdrerc.domain.dto.sdrercapp.PlazoConfiguracionDTO;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.Color;
import java.sql.SQLException;

public final class PlazoVisualSupportV2 {

    public enum Nivel {
        VERDE,
        AMARILLO,
        ROJO,
        VENCIDO,
        SIN_CONFIG
    }

    private static final Object LOCK = new Object();
    private static volatile PlazoConfiguracionDTO plazoSolicitud;
    private static volatile boolean cargaIntentada;

    private PlazoVisualSupportV2() {
    }

    public static Nivel clasificarDias(Long diasRestantes) {
        if (diasRestantes == null) {
            return Nivel.SIN_CONFIG;
        }
        if (diasRestantes.longValue() < 0L) {
            return Nivel.VENCIDO;
        }
        PlazoConfiguracionDTO config = obtenerPlazoSolicitud();
        int diasPlazo = config == null || config.getDiasPlazo() == null || config.getDiasPlazo().intValue() <= 0
                ? 30
                : config.getDiasPlazo().intValue();
        int porcentajeRestante = Math.round((diasRestantes.longValue() * 100f) / diasPlazo);
        int verde = porcentaje(config == null ? null : config.getPorcentajeVerdeDesde(), 51);
        int amarillo = porcentaje(config == null ? null : config.getPorcentajeAmarilloDesde(), 21);
        int rojo = porcentaje(config == null ? null : config.getPorcentajeRojoDesde(), 0);
        if (porcentajeRestante >= verde) {
            return Nivel.VERDE;
        }
        if (porcentajeRestante >= amarillo) {
            return Nivel.AMARILLO;
        }
        if (porcentajeRestante >= rojo) {
            return Nivel.ROJO;
        }
        return Nivel.ROJO;
    }

    public static Color foregroundFor(Nivel nivel) {
        if (nivel == null) {
            return AppV2Theme.MUTED;
        }
        switch (nivel) {
            case VERDE:
                return AppV2Theme.SUCCESS;
            case AMARILLO:
                return AppV2Theme.WARNING;
            case ROJO:
            case VENCIDO:
                return AppV2Theme.ERROR;
            case SIN_CONFIG:
            default:
                return AppV2Theme.MUTED;
        }
    }

    public static Color backgroundFor(Nivel nivel) {
        if (nivel == null) {
            return AppV2Theme.SOFT_GRAY;
        }
        switch (nivel) {
            case VERDE:
                return AppV2Theme.SOFT_GREEN;
            case AMARILLO:
                return AppV2Theme.SOFT_ORANGE;
            case ROJO:
            case VENCIDO:
                return AppV2Theme.SOFT_RED;
            case SIN_CONFIG:
            default:
                return AppV2Theme.SOFT_GRAY;
        }
    }

    public static void invalidateCache() {
        synchronized (LOCK) {
            plazoSolicitud = null;
            cargaIntentada = false;
        }
    }

    private static PlazoConfiguracionDTO obtenerPlazoSolicitud() {
        if (cargaIntentada) {
            return plazoSolicitud;
        }
        synchronized (LOCK) {
            if (!cargaIntentada) {
                try {
                    plazoSolicitud = new PlazoConfiguracionService().obtenerPlazoSolicitud();
                } catch (SQLException ex) {
                    plazoSolicitud = null;
                } finally {
                    cargaIntentada = true;
                }
            }
        }
        return plazoSolicitud != null ? plazoSolicitud : new PlazoConfiguracionDTO();
    }

    private static int porcentaje(Integer valor, int defecto) {
        return valor == null ? defecto : valor.intValue();
    }
}
