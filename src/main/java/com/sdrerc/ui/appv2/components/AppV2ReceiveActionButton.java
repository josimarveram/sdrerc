package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

/**
 * Stable receive action icon for compact operational tables.
 */
public class AppV2ReceiveActionButton extends JButton {

    private boolean received;

    public AppV2ReceiveActionButton() {
        setText("");
        setFocusable(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
    }

    public void configure(boolean received, boolean actionEnabled) {
        this.received = received;
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
            Color accent = isEnabled() || received ? AppV2Theme.PRIMARY : AppV2Theme.TEXT_SECONDARY;
            Color fill = received ? AppV2Theme.SOFT_GREEN
                    : isEnabled() ? AppV2Theme.SOFT_BLUE : AppV2Theme.SURFACE_ALT;

            g2.setColor(fill);
            g2.fillRoundRect(x, y, size, size, 6, 6);
            g2.setColor(accent);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawRoundRect(x, y, size, size, 6, 6);

            if (received) {
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 5, y + 11, x + 9, y + 15);
                g2.drawLine(x + 9, y + 15, x + 17, y + 7);
                return;
            }

            int center = x + size / 2;
            g2.drawLine(center, y + 5, center, y + 13);
            g2.drawLine(center, y + 13, center - 4, y + 9);
            g2.drawLine(center, y + 13, center + 4, y + 9);
            g2.drawLine(x + 5, y + 16, x + 5, y + 18);
            g2.drawLine(x + 5, y + 18, x + size - 5, y + 18);
            g2.drawLine(x + size - 5, y + 18, x + size - 5, y + 16);
        } finally {
            g2.dispose();
        }
    }
}
