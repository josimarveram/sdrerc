package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;

public class PillBadgeV2 extends BadgeV2 {

    private Color cellBackground;

    public PillBadgeV2(String text, Color background, Color foreground) {
        this(text, background, foreground, null);
    }

    public PillBadgeV2(String text, Color background, Color foreground, Color cellBackground) {
        super(text, background, foreground);
        this.cellBackground = cellBackground;
        setOpaque(false);
        setHorizontalAlignment(CENTER);
        setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
    }

    public void setCellBackground(Color cellBackground) {
        this.cellBackground = cellBackground;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (cellBackground != null) {
                g2.setColor(cellBackground);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            String text = getText() == null ? "" : getText();
            Font font = getFont() == null ? AppV2Theme.fontBold(11) : getFont();
            g2.setFont(font);
            FontMetrics metrics = g2.getFontMetrics(font);
            int textWidth = metrics.stringWidth(text);
            int pillWidth = Math.min(Math.max(34, textWidth + 22), Math.max(34, getWidth() - 10));
            int pillHeight = Math.min(24, Math.max(20, getHeight() - 8));
            int x = Math.max(4, (getWidth() - pillWidth) / 2);
            int y = Math.max(3, (getHeight() - pillHeight) / 2);

            g2.setColor(getBackground());
            g2.fillRoundRect(x, y, pillWidth, pillHeight, pillHeight, pillHeight);
            g2.setColor(getForeground());
            int textX = x + (pillWidth - textWidth) / 2;
            int textY = y + (pillHeight - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(text, textX, textY);
        } finally {
            g2.dispose();
        }
    }
}
