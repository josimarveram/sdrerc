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
 * Icono premium de "buscar/agregar" (lupa) con fondo solido azul institucional, para abrir
 * un buscador de forma compacta dentro de un panel lateral.
 */
public class AppV2SearchActionButton extends JButton {

    private boolean hover;

    public AppV2SearchActionButton() {
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
            Color fill = hover ? AppV2Theme.PRIMARY_HOVER : AppV2Theme.PRIMARY;

            g2.setColor(fill);
            g2.fillRoundRect(x, y, size, size, 8, 8);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int lensSize = (int) Math.round(size * 0.42);
            int lensX = x + (int) Math.round(size * 0.28);
            int lensY = y + (int) Math.round(size * 0.28);
            g2.drawOval(lensX, lensY, lensSize, lensSize);
            int handleStartX = lensX + lensSize - 2;
            int handleStartY = lensY + lensSize - 2;
            g2.drawLine(handleStartX, handleStartY, x + size - 5, y + size - 5);
        } finally {
            g2.dispose();
        }
    }
}
