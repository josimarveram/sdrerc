package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

/**
 * Stable remove action icon for compact operational tables.
 */
public class AppV2RemoveActionButton extends JButton {

    private boolean active;

    public AppV2RemoveActionButton() {
        setText("");
        setFocusable(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
    }

    public void configure(boolean active, boolean actionEnabled) {
        this.active = active;
        setEnabled(actionEnabled);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(22, Math.min(getWidth() - 4, getHeight() - 4));
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            Color accent = isEnabled() || active ? AppV2Theme.ERROR : AppV2Theme.TEXT_SECONDARY;
            Color fill = active ? AppV2Theme.SOFT_RED
                    : isEnabled() ? new Color(251, 236, 236) : AppV2Theme.SURFACE_ALT;

            g2.setColor(fill);
            g2.fillRoundRect(x, y, size, size, 6, 6);
            g2.setColor(accent);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawRoundRect(x, y, size, size, 6, 6);

            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 6, y + 6, x + size - 6, y + size - 6);
            g2.drawLine(x + size - 6, y + 6, x + 6, y + size - 6);
        } finally {
            g2.dispose();
        }
    }
}
