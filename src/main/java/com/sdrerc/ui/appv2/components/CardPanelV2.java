package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class CardPanelV2 extends JPanel {

    public CardPanelV2(String title, String description) {
        super(new BorderLayout(8, 8));
        setBackground(AppV2Theme.SURFACE);
        setBorder(AppV2Theme.cardBorder());

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppV2Theme.fontBold(16));
        lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);

        JTextArea lblDescription = new JTextArea(description == null ? "" : description);
        lblDescription.setEditable(false);
        lblDescription.setFocusable(false);
        lblDescription.setOpaque(false);
        lblDescription.setLineWrap(true);
        lblDescription.setWrapStyleWord(true);
        lblDescription.setMargin(new Insets(0, 0, 0, 0));
        lblDescription.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        lblDescription.setForeground(AppV2Theme.TEXT_SECONDARY);

        add(lblTitle, BorderLayout.NORTH);
        add(lblDescription, BorderLayout.CENTER);
    }
}
