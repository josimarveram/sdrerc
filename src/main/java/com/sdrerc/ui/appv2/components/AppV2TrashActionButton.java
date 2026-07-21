package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

/**
 * Icono premium de "tacho de basura" con fondo solido azul institucional, para acciones de
 * eliminacion secundarias (ej. quitar una alerta) que deben resaltar sin usar el rojo reservado
 * a las eliminaciones destructivas de {@link AppV2RemoveActionButton}.
 */
public class AppV2TrashActionButton extends JButton {

    private boolean hover;

    public AppV2TrashActionButton() {
        setText("");
        setFocusable(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(30, 30));
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(28, Math.min(getWidth() - 2, getHeight() - 2));
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            Color fill = isEnabled() ? (hover ? AppV2Theme.PRIMARY_HOVER : AppV2Theme.PRIMARY) : AppV2Theme.MUTED;

            g2.setColor(fill);
            g2.fillRoundRect(x, y, size, size, 8, 8);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int left = x + (int) Math.round(size * 0.26);
            int right = x + size - (int) Math.round(size * 0.26);
            int lidY = y + (int) Math.round(size * 0.28);
            int bottomY = y + size - (int) Math.round(size * 0.2);

            g2.drawLine(left - 2, lidY, right + 2, lidY);
            int lidHandleLeft = x + (int) Math.round(size * 0.38);
            int lidHandleRight = x + size - (int) Math.round(size * 0.38);
            g2.drawLine(lidHandleLeft, lidY, lidHandleLeft + 2, lidY - 4);
            g2.drawLine(lidHandleRight, lidY, lidHandleRight - 2, lidY - 4);
            g2.drawLine(lidHandleLeft + 2, lidY - 4, lidHandleRight - 2, lidY - 4);

            g2.drawLine(left, lidY + 2, left + 1, bottomY);
            g2.drawLine(right, lidY + 2, right - 1, bottomY);
            g2.drawLine(left + 1, bottomY, right - 1, bottomY);

            int slatTop = lidY + 5;
            int slatBottom = bottomY - 3;
            int mid = x + size / 2;
            g2.drawLine(mid, slatTop, mid, slatBottom);
        } finally {
            g2.dispose();
        }
    }
}
