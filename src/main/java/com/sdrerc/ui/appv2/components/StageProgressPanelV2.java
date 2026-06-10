package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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

    private static final Color[] ACCENTS = {
        AppV2Theme.INFO,
        AppV2Theme.TEAL,
        AppV2Theme.INDIGO,
        AppV2Theme.SUCCESS,
        new Color(116, 78, 145),
        AppV2Theme.WARNING,
        new Color(178, 72, 86),
        new Color(150, 91, 33),
        new Color(54, 104, 126),
        new Color(88, 98, 110)
    };

    public StageProgressPanelV2(String etapaActual) {
        this(etapaActual, null);
    }

    public StageProgressPanelV2(String etapaActual, String estadoActual) {
        super(new BorderLayout());
        setOpaque(false);
        construir(etapaActual, estadoActual);
    }

    private void construir(String etapaActual, String estadoActual) {
        int activeIndex = indexOf(etapaActual);
        boolean observado = DisplayNameMapperV2.estadoObservado(estadoActual);
        AppV2WrapPanel wrap = new AppV2WrapPanel(5, 6);
        for (int i = 0; i < MACRO_ETAPAS.length; i++) {
            wrap.add(new StageStep(i, DisplayNameMapperV2.etapa(MACRO_ETAPAS[i]),
                    statusText(i, activeIndex, observado),
                    ACCENTS[i % ACCENTS.length],
                    activeIndex, observado));
            if (i < MACRO_ETAPAS.length - 1) {
                JLabel arrow = new JLabel(">");
                arrow.setFont(AppV2Theme.fontBold(15));
                arrow.setForeground(AppV2Theme.TEXT_SECONDARY);
                arrow.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
                wrap.add(arrow);
            }
        }
        add(wrap, BorderLayout.CENTER);
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

    private static String statusText(int index, int activeIndex, boolean observado) {
        if (activeIndex < 0) {
            return "Ruta BPMN";
        }
        if (index < activeIndex) {
            return "Completado";
        }
        if (index == activeIndex) {
            return observado ? "En observacion" : "Actual";
        }
        return "Pendiente";
    }

    private static final class StageStep extends JPanel {

        private final Color accent;
        private final boolean active;
        private final boolean completed;
        private final boolean observed;

        private StageStep(int index, String label, String status, Color accent,
                int activeIndex, boolean observado) {
            super(new BorderLayout(8, 0));
            this.accent = accent;
            this.active = index == activeIndex;
            this.completed = activeIndex >= 0 && index < activeIndex;
            this.observed = active && observado;
            setOpaque(false);
            setPreferredSize(new Dimension(154, 48));
            setMinimumSize(new Dimension(136, 46));
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 6));

            JLabel number = new StageNumber(String.valueOf(index + 1), accent,
                    completed, active, observed);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

            JLabel title = new JLabel(label);
            title.setFont(AppV2Theme.fontBold(11));
            title.setForeground(active ? AppV2Theme.PRIMARY : AppV2Theme.TEXT_PRIMARY);
            title.setToolTipText(label);

            JLabel detail = new JLabel(status);
            detail.setFont(AppV2Theme.fontPlain(10));
            detail.setForeground(observed ? AppV2Theme.WARNING : AppV2Theme.TEXT_SECONDARY);

            text.add(Box.createVerticalGlue());
            text.add(title);
            text.add(detail);
            text.add(Box.createVerticalGlue());

            add(number, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color background = active
                        ? new Color(230, 242, 247)
                        : completed ? AppV2Theme.SOFT_GREEN : AppV2Theme.SOFT_GRAY;
                if (observed) {
                    background = AppV2Theme.SOFT_ORANGE;
                }
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(active ? accent : AppV2Theme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    private static final class StageNumber extends JLabel {

        private final Color accent;
        private final boolean completed;
        private final boolean active;
        private final boolean observed;

        private StageNumber(String value, Color accent, boolean completed,
                boolean active, boolean observed) {
            super(value);
            this.accent = accent;
            this.completed = completed;
            this.active = active;
            this.observed = observed;
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setFont(AppV2Theme.fontBold(10));
            setForeground(Color.WHITE);
            setPreferredSize(new Dimension(28, 28));
            setMinimumSize(new Dimension(28, 28));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = active ? accent : completed ? AppV2Theme.SUCCESS : AppV2Theme.MUTED;
                if (observed) {
                    fill = AppV2Theme.WARNING;
                }
                g2.setColor(fill);
                int size = Math.min(getWidth(), getHeight()) - 2;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                g2.fillOval(x, y, size, size);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
