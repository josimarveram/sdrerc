package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.JTextField;

public class AppV2SearchField extends JTextField {

    private String placeholder;

    public AppV2SearchField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder == null ? "" : placeholder;
        setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        setToolTipText(this.placeholder);
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
        setToolTipText(this.placeholder);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText() != null && !getText().isEmpty()) {
            return;
        }
        if (isFocusOwner() || placeholder.isEmpty()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(AppV2Theme.MUTED);
            g2.setFont(getFont());
            Insets insets = getInsets();
            int y = (getHeight() - getFontMetrics(getFont()).getHeight()) / 2
                    + getFontMetrics(getFont()).getAscent();
            g2.drawString(placeholder, insets.left + 2, y);
        } finally {
            g2.dispose();
        }
    }
}
