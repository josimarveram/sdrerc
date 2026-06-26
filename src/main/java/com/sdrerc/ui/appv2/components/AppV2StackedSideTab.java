package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class AppV2StackedSideTab extends JPanel {

    private final String label;
    private final int preferredWidth;
    private final int preferredHeight;
    private final Color idleColor;
    private final Color selectedColor;
    private final Color expandedColor;
    private boolean selected;
    private boolean expanded;

    public AppV2StackedSideTab(
            String label,
            int preferredWidth,
            int preferredHeight,
            Color idleColor,
            Color selectedColor,
            Color expandedColor) {
        this.label = label == null ? "" : label;
        this.preferredWidth = preferredWidth;
        this.preferredHeight = preferredHeight;
        this.idleColor = idleColor == null ? new Color(230, 241, 245) : idleColor;
        this.selectedColor = selectedColor == null ? AppV2Theme.PRIMARY : selectedColor;
        this.expandedColor = expandedColor == null ? this.selectedColor.darker() : expandedColor;
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Dimension size = new Dimension(preferredWidth, preferredHeight);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
    }

    public void setState(boolean selected, boolean expanded) {
        this.selected = selected;
        this.expanded = expanded;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color background = expanded ? expandedColor : (selected ? selectedColor : idleColor);
            Color border = expanded ? expandedColor.darker()
                    : (selected ? selectedColor.darker() : AppV2Theme.BORDER);
            Color text = selected ? Color.WHITE : AppV2Theme.PRIMARY_DARK;

            g2.setColor(background);
            g2.fillRoundRect(2, 2, getWidth() - 3, getHeight() - 4, 18, 18);
            g2.setColor(border);
            g2.drawRoundRect(2, 2, getWidth() - 3, getHeight() - 4, 18, 18);

            g2.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
            g2.setColor(text);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(label);
            int x = -((getHeight() + textWidth) / 2);
            int y = (getWidth() + fm.getAscent() - fm.getDescent()) / 2;
            g2.rotate(-Math.PI / 2);
            g2.drawString(label, x, y);
        } finally {
            g2.dispose();
        }
    }
}
