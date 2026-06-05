package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;

public class AppV2SidebarCollapseButton extends JButton {

    public AppV2SidebarCollapseButton() {
        super("<");
        setFocusPainted(false);
        setOpaque(true);
        setContentAreaFilled(true);
        setBorderPainted(false);
        setBackground(AppV2Theme.SIDEBAR_HOVER);
        setForeground(java.awt.Color.WHITE);
        setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(34, 30));
        setMinimumSize(new Dimension(34, 30));
        setToolTipText("Colapsar menú");
    }

    public void setCollapsed(boolean collapsed) {
        setText(collapsed ? ">" : "<");
        setToolTipText(collapsed ? "Expandir menú" : "Colapsar menú");
    }
}
