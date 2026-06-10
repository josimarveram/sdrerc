package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class StageProgressPanelV2 extends JPanel {

    private static final String[] MACRO_ETAPAS = {
        "REGISTRO",
        "ASIGNACION",
        "ANALISIS",
        "VERIFICACION",
        "FIRMA_EMISION",
        "EJECUCION",
        "NOTIFICACION",
        "PUBLICACION_CONDICIONAL",
        "EXPEDIENTE_DIGITAL",
        "CIERRE_ARCHIVO"
    };

    public StageProgressPanelV2(String etapaActual) {
        this(etapaActual, null);
    }

    public StageProgressPanelV2(String etapaActual, String estadoActual) {
        super(new FlowLayout(FlowLayout.LEFT, 3, 2));
        setOpaque(false);
        construir(etapaActual, estadoActual);
    }

    private void construir(String etapaActual, String estadoActual) {
        int activeIndex = indexOf(etapaActual);
        boolean observado = DisplayNameMapperV2.estadoObservado(estadoActual);
        for (int i = 0; i < MACRO_ETAPAS.length; i++) {
            Color background;
            Color foreground;
            String label = DisplayNameMapperV2.etapa(MACRO_ETAPAS[i]);
            if (activeIndex >= 0 && i < activeIndex) {
                background = AppV2Theme.SOFT_GREEN;
                foreground = AppV2Theme.SUCCESS;
                label = "✓ " + label;
            } else if (i == activeIndex) {
                background = observado ? AppV2Theme.SOFT_ORANGE : AppV2Theme.PRIMARY;
                foreground = observado ? AppV2Theme.WARNING : Color.WHITE;
            } else {
                background = AppV2Theme.SOFT_GRAY;
                foreground = AppV2Theme.TEXT_SECONDARY;
            }
            BadgeV2 badge = new BadgeV2(label, background, foreground);
            badge.setFont(AppV2Theme.fontBold(10));
            badge.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
            add(badge);
        }
    }

    private int indexOf(String etapa) {
        if (etapa == null) {
            return -1;
        }
        for (int i = 0; i < MACRO_ETAPAS.length; i++) {
            if (MACRO_ETAPAS[i].equalsIgnoreCase(etapa)) {
                return i;
            }
        }
        return -1;
    }
}
