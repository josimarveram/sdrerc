package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;

public class AppV2ContextChip extends JButton {

    private Color accent = AppV2Theme.TEAL;
    private Color background = AppV2Theme.SOFT_BLUE;

    public AppV2ContextChip(String text) {
        super(text);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        setForeground(AppV2Theme.TEXT_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(6, 24, 6, 12));
        setPreferredSize(new Dimension(112, 32));
        setMinimumSize(new Dimension(92, 30));
    }

    public void setAccent(Color accent, Color background) {
        this.accent = accent == null ? AppV2Theme.TEAL : accent;
        this.background = background == null ? AppV2Theme.SOFT_BLUE : background;
        repaint();
    }

    public void setExpanded(boolean expanded) {
        setText(expanded ? "Restaurar" : "Expandir");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = getModel().isRollover() ? blend(background, Color.WHITE, 0.18d) : background;
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
        g2.setColor(accent);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
        g2.fillOval(10, (getHeight() / 2) - 4, 8, 8);
        g2.dispose();
        super.paintComponent(g);
    }

    private static Color blend(Color base, Color overlay, double ratio) {
        double keep = 1.0d - ratio;
        int red = (int) Math.round(base.getRed() * keep + overlay.getRed() * ratio);
        int green = (int) Math.round(base.getGreen() * keep + overlay.getGreen() * ratio);
        int blue = (int) Math.round(base.getBlue() * keep + overlay.getBlue() * ratio);
        return new Color(red, green, blue);
    }
}
