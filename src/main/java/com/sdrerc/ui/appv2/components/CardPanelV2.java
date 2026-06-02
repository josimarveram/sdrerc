package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CardPanelV2 extends JPanel {

    public CardPanelV2(String title, String description) {
        super(new BorderLayout(8, 8));
        setBackground(AppV2Theme.SURFACE);
        setBorder(AppV2Theme.cardBorder());

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppV2Theme.fontBold(16));
        lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);

        JLabel lblDescription = new JLabel("<html><body style='width:210px'>" + escape(description) + "</body></html>");
        lblDescription.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        lblDescription.setForeground(AppV2Theme.TEXT_SECONDARY);

        add(lblTitle, BorderLayout.NORTH);
        add(lblDescription, BorderLayout.CENTER);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
