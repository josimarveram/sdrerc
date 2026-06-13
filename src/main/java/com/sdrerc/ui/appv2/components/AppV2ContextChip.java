package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;

public class AppV2ContextChip extends JButton {

    private Color accent = AppV2Theme.TEAL;
    private Color background = AppV2Theme.SOFT_BLUE;
    private final String normalLabel;
    private boolean expanded;

    public AppV2ContextChip(String text) {
        super("");
        this.normalLabel = text == null || text.trim().isEmpty() ? "Ampliar" : text.trim();
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        setForeground(AppV2Theme.TEXT_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setPreferredSize(new Dimension(116, 40));
        setMinimumSize(new Dimension(108, 38));
        setMaximumSize(new Dimension(132, 42));
        getAccessibleContext().setAccessibleName(normalLabel + " panel");
        setText("");
    }

    public void setAccent(Color accent, Color background) {
        this.accent = accent == null ? AppV2Theme.TEAL : accent;
        this.background = background == null ? AppV2Theme.SOFT_BLUE : background;
        repaint();
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        getAccessibleContext().setAccessibleName((expanded ? "Restaurar" : normalLabel) + " panel");
        setText("");
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int offsetY = getModel().isPressed() ? 1 : (getModel().isRollover() ? -1 : 0);
            int w = getWidth() - 2;
            int h = getHeight() - 3;
            Path2D tab = tabPath(1, 2 + offsetY, w, h);
            Path2D shadow = tabPath(2, 5, w, h);

            g2.setColor(new Color(30, 41, 59, 30));
            g2.fill(shadow);

            Color fill = blend(background, Color.WHITE, getModel().isRollover() ? 0.34d : 0.18d);
            g2.setColor(fill);
            g2.fill(tab);

            g2.setClip(tab);
            g2.setColor(blend(accent, Color.WHITE, 0.10d));
            g2.fillRect(0, 2 + offsetY, getWidth(), 5);
            g2.setClip(null);

            g2.setColor(blend(accent, AppV2Theme.TEXT_PRIMARY, 0.10d));
            g2.setStroke(new BasicStroke(1.3f));
            g2.draw(tab);

            int centerY = (getHeight() / 2) + offsetY;
            paintToggleGlyph(g2, 17, centerY);

            String label = expanded ? "Restaurar" : normalLabel;
            g2.setFont(getFont());
            g2.setColor(AppV2Theme.TEXT_PRIMARY);
            FontMetrics metrics = g2.getFontMetrics();
            int textY = centerY + (metrics.getAscent() - metrics.getDescent()) / 2;
            g2.drawString(label, 36, textY);
        } finally {
            g2.dispose();
        }
    }

    private Path2D tabPath(int x, int y, int width, int height) {
        int right = x + width - 1;
        int bottom = y + height - 1;
        Path2D path = new Path2D.Double();
        path.moveTo(x + 7, y);
        path.lineTo(right - 19, y);
        path.quadTo(right - 12, y, right - 9, y + 8);
        path.lineTo(right, bottom);
        path.lineTo(x + 5, bottom);
        path.quadTo(x, bottom, x, bottom - 6);
        path.lineTo(x, y + 7);
        path.quadTo(x, y, x + 7, y);
        path.closePath();
        return path;
    }

    private void paintToggleGlyph(Graphics2D g2, int centerX, int centerY) {
        g2.setColor(accent);
        g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (expanded) {
            drawChevron(g2, centerX - 4, centerY, true);
            drawChevron(g2, centerX + 6, centerY, false);
        } else {
            drawChevron(g2, centerX - 4, centerY, false);
            drawChevron(g2, centerX + 6, centerY, true);
        }
    }

    private void drawChevron(Graphics2D g2, int x, int y, boolean pointsRight) {
        int direction = pointsRight ? 1 : -1;
        g2.drawLine(x - 3 * direction, y - 5, x + 2 * direction, y);
        g2.drawLine(x + 2 * direction, y, x - 3 * direction, y + 5);
    }

    private static Color blend(Color base, Color overlay, double ratio) {
        double keep = 1.0d - ratio;
        int red = (int) Math.round(base.getRed() * keep + overlay.getRed() * ratio);
        int green = (int) Math.round(base.getGreen() * keep + overlay.getGreen() * ratio);
        int blue = (int) Math.round(base.getBlue() * keep + overlay.getBlue() * ratio);
        return new Color(red, green, blue);
    }
}
