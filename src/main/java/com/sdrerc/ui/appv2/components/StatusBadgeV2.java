package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import java.awt.Color;

public final class StatusBadgeV2 {

    private StatusBadgeV2() {
    }

    public static BadgeV2 forEtapa(String etapa) {
        BadgeV2 badge = new BadgeV2(DisplayNameMapperV2.etapa(safe(etapa)), AppV2Theme.SOFT_BLUE, AppV2Theme.PRIMARY);
        badge.setFont(AppV2Theme.fontBold(11));
        return badge;
    }

    public static BadgeV2 forEstado(String estado) {
        String normalized = safe(estado).toUpperCase();
        String display = DisplayNameMapperV2.estado(safe(estado));
        if (normalized.contains("OBSERV") || normalized.contains("CORRECCION") || normalized.contains("INCONSISTENTE")) {
            return compact(display, AppV2Theme.SOFT_ORANGE, AppV2Theme.WARNING);
        }
        if (normalized.contains("CERRADO") || normalized.contains("ARCHIVADO") || normalized.contains("FIRMADO") || normalized.contains("VERIFICADO")) {
            return compact(display, AppV2Theme.SOFT_GREEN, AppV2Theme.SUCCESS);
        }
        if (normalized.contains("VENC") || normalized.contains("ERROR")) {
            return compact(display, AppV2Theme.SOFT_RED, AppV2Theme.ERROR);
        }
        return compact(display, AppV2Theme.SOFT_GRAY, AppV2Theme.TEXT_SECONDARY);
    }

    public static BadgeV2 forDias(Object value) {
        if (value == null || value.toString().isEmpty()) {
                return compact("Sin plazo", AppV2Theme.SOFT_GRAY, AppV2Theme.MUTED);
        }
        try {
            long dias = Long.parseLong(value.toString());
            if (dias < 0) {
                return compact(dias + " vencido", AppV2Theme.SOFT_RED, AppV2Theme.ERROR);
            }
            if (dias <= 3) {
                return compact(dias + " días", AppV2Theme.SOFT_ORANGE, AppV2Theme.WARNING);
            }
            return compact(dias + " días", AppV2Theme.SOFT_GREEN, AppV2Theme.SUCCESS);
        } catch (NumberFormatException ex) {
            return compact(value.toString(), AppV2Theme.SOFT_GRAY, AppV2Theme.MUTED);
        }
    }

    public static BadgeV2 small(String text, Color background, Color foreground) {
        return new BadgeV2(text, background, foreground);
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private static BadgeV2 compact(String text, Color background, Color foreground) {
        BadgeV2 badge = new BadgeV2(text, background, foreground);
        badge.setFont(AppV2Theme.fontBold(11));
        return badge;
    }
}
