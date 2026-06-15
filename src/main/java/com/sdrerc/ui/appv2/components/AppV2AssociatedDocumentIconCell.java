package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class AppV2AssociatedDocumentIconCell extends JPanel {

    private Color accent = AppV2Theme.PRIMARY;

    public AppV2AssociatedDocumentIconCell() {
        setOpaque(true);
        setPreferredSize(new Dimension(30, 28));
    }

    public void configure(Color accent, Color background, javax.swing.border.Border border) {
        this.accent = accent == null ? AppV2Theme.PRIMARY : accent;
        setBackground(background == null ? AppV2Theme.SURFACE_ALT : background);
        setBorder(border);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = 15;
            int h = 18;
            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;
            Color fill = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 22);
            Color line = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 150);
            g2.setColor(fill);
            g2.fillRoundRect(x, y, w, h, 4, 4);
            g2.setColor(line);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(x, y, w, h, 4, 4);
            int fold = 5;
            g2.drawLine(x + w - fold, y, x + w - 1, y + fold);
            g2.drawLine(x + w - fold, y, x + w - fold, y + fold);
            g2.drawLine(x + 4, y + 8, x + w - 4, y + 8);
            g2.drawLine(x + 4, y + 12, x + w - 5, y + 12);
        } finally {
            g2.dispose();
        }
    }
}
