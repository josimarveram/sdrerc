package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;

public class AppV2ContextChip extends JButton {

    private Color accent = AppV2Theme.TEAL;
    private Color background = AppV2Theme.SOFT_BLUE;
    private boolean expanded;

    public AppV2ContextChip(String text) {
        super("");
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        setForeground(AppV2Theme.TEXT_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setPreferredSize(new Dimension(44, 34));
        setMinimumSize(new Dimension(40, 32));
        setMaximumSize(new Dimension(48, 36));
        setText("");
    }

    public void setAccent(Color accent, Color background) {
        this.accent = accent == null ? AppV2Theme.TEAL : accent;
        this.background = background == null ? AppV2Theme.SOFT_BLUE : background;
        repaint();
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        setText("");
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int offset = getModel().isPressed() ? 1 : 0;
        Color fill = getModel().isRollover() ? blend(background, Color.WHITE, 0.22d) : background;
        Color shadow = new Color(30, 41, 59, 32);
        int w = getWidth() - 7;
        int h = getHeight() - 5;
        int x = 2 + offset;
        int y = 2 + offset;

        g2.setColor(shadow);
        g2.fillRoundRect(x + 2, y + 3, w, h, 12, 12);
        g2.setColor(fill);
        g2.fillRoundRect(x, y, w, h, 12, 12);
        g2.setColor(accent);
        g2.fillRoundRect(x, y, 9, h, 10, 10);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(x, y, w, h, 12, 12);

        Polygon fold = new Polygon();
        fold.addPoint(x + w - 9, y + 1);
        fold.addPoint(x + w - 1, y + 1);
        fold.addPoint(x + w - 1, y + 9);
        g2.setColor(blend(fill, accent, 0.20d));
        g2.fillPolygon(fold);

        g2.setColor(accent);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int centerY = y + h / 2;
        if (expanded) {
            g2.drawLine(x + 19, centerY - 5, x + 14, centerY);
            g2.drawLine(x + 14, centerY, x + 19, centerY + 5);
            g2.drawLine(x + 25, centerY - 5, x + 30, centerY);
            g2.drawLine(x + 30, centerY, x + 25, centerY + 5);
        } else {
            g2.drawLine(x + 15, centerY - 5, x + 20, centerY);
            g2.drawLine(x + 20, centerY, x + 15, centerY + 5);
            g2.drawLine(x + 29, centerY - 5, x + 24, centerY);
            g2.drawLine(x + 24, centerY, x + 29, centerY + 5);
        }
        g2.dispose();
    }

    private static Color blend(Color base, Color overlay, double ratio) {
        double keep = 1.0d - ratio;
        int red = (int) Math.round(base.getRed() * keep + overlay.getRed() * ratio);
        int green = (int) Math.round(base.getGreen() * keep + overlay.getGreen() * ratio);
        int blue = (int) Math.round(base.getBlue() * keep + overlay.getBlue() * ratio);
        return new Color(red, green, blue);
    }
}
