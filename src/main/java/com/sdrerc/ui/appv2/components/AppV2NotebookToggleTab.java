package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import javax.swing.JComponent;

public class AppV2NotebookToggleTab extends JComponent {

    public static final int DEFAULT_WIDTH = 34;
    public static final int DEFAULT_HEIGHT = 112;

    private Color accent = AppV2Theme.TEAL;
    private Color background = AppV2Theme.SOFT_BLUE;
    private boolean expanded;
    private boolean hover;
    private boolean pressed;

    public AppV2NotebookToggleTab() {
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        setMinimumSize(new Dimension(28, 90));
        setMaximumSize(new Dimension(40, 130));
        setName("Pestaña del panel de asignación");
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!isEnabled()) {
                    return;
                }
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!isEnabled()) {
                    return;
                }
                boolean fire = pressed && contains(e.getPoint());
                pressed = false;
                repaint();
                if (fire) {
                    fireActionPerformed();
                }
            }
        };
        addMouseListener(mouse);
    }

    public void addActionListener(ActionListener listener) {
        listenerList.add(ActionListener.class, listener);
    }

    public void removeActionListener(ActionListener listener) {
        listenerList.remove(ActionListener.class, listener);
    }

    public void setAccent(Color accent, Color background) {
        this.accent = accent == null ? AppV2Theme.TEAL : accent;
        this.background = background == null ? AppV2Theme.SOFT_BLUE : background;
        repaint();
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        repaint();
    }

    public boolean isExpanded() {
        return expanded;
    }

    private void fireActionPerformed() {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "toggle");
        for (ActionListener listener : listenerList.getListeners(ActionListener.class)) {
            listener.actionPerformed(event);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int offsetX = pressed ? 1 : (hover ? -1 : 0);
            int x = 4 + offsetX;
            int y = 4;
            int w = getWidth() - 8;
            int h = getHeight() - 8;

            Path2D shadow = tabPath(x + 2, y + 3, w, h);
            g2.setColor(new Color(30, 41, 59, 32));
            g2.fill(shadow);

            Path2D tab = tabPath(x, y, w, h);
            Color fill = hover ? blend(background, accent, 0.10d) : background;
            g2.setPaint(new GradientPaint(x, y, blend(fill, Color.WHITE, 0.24d), x + w, y, fill));
            g2.fill(tab);

            g2.setColor(blend(accent, AppV2Theme.TEXT_PRIMARY, hover ? 0.08d : 0.16d));
            g2.setStroke(new BasicStroke(1.3f));
            g2.draw(tab);

            g2.setColor(blend(accent, Color.WHITE, 0.12d));
            g2.fillRoundRect(x + w - 6, y + 8, 4, h - 16, 5, 5);
        } finally {
            g2.dispose();
        }
    }

    private Path2D tabPath(int x, int y, int width, int height) {
        int right = x + width - 1;
        int bottom = y + height - 1;
        Path2D path = new Path2D.Double();
        path.moveTo(x + 12, y);
        path.lineTo(right - 3, y);
        path.quadTo(right, y, right, y + 4);
        path.lineTo(right, bottom - 4);
        path.quadTo(right, bottom, right - 3, bottom);
        path.lineTo(x + 12, bottom);
        path.quadTo(x, bottom, x, bottom - 12);
        path.lineTo(x, y + 12);
        path.quadTo(x, y, x + 12, y);
        path.closePath();
        return path;
    }

    private static Color blend(Color base, Color overlay, double ratio) {
        double keep = 1.0d - ratio;
        int red = (int) Math.round(base.getRed() * keep + overlay.getRed() * ratio);
        int green = (int) Math.round(base.getGreen() * keep + overlay.getGreen() * ratio);
        int blue = (int) Math.round(base.getBlue() * keep + overlay.getBlue() * ratio);
        return new Color(red, green, blue);
    }
}
