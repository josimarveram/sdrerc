package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class AppV2ExpandCollapseGlyph extends JPanel {

    public static final int NONE = 0;
    public static final int EXPAND = 1;
    public static final int COLLAPSE = 2;
    public static final int LOADING = 3;

    private int state = NONE;
    private Color accent = AppV2Theme.PRIMARY;

    public AppV2ExpandCollapseGlyph() {
        setOpaque(false);
        setPreferredSize(new Dimension(30, 28));
    }

    public void configure(int state, Color accent, Color background) {
        this.state = state;
        this.accent = accent == null ? AppV2Theme.PRIMARY : accent;
        setBackground(background);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (state == NONE) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            if (state == LOADING) {
                paintLoading(g2, cx, cy);
                return;
            }
            int size = 18;
            int x = cx - size / 2;
            int y = cy - size / 2;
            Color fill = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 28);
            g2.setColor(fill);
            g2.fillOval(x, y, size, size);
            g2.setColor(accent);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x, y, size, size);
            int pad = 5;
            g2.drawLine(x + pad, cy, x + size - pad, cy);
            if (state == EXPAND) {
                g2.drawLine(cx, y + pad, cx, y + size - pad);
            }
        } finally {
            g2.dispose();
        }
    }

    private void paintLoading(Graphics2D g2, int cx, int cy) {
        g2.setColor(accent);
        int start = cx - 8;
        for (int i = 0; i < 3; i++) {
            g2.fillOval(start + i * 7, cy - 2, 4, 4);
        }
    }
}
