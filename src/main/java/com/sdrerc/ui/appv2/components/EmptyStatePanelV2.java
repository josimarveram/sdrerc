package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class EmptyStatePanelV2 extends JPanel {

    public EmptyStatePanelV2(String title, String detail) {
        super(new BorderLayout(0, 8));
        setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);

        JLabel lblDetail = new JLabel(detail);
        lblDetail.setHorizontalAlignment(SwingConstants.CENTER);
        lblDetail.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        lblDetail.setForeground(AppV2Theme.TEXT_SECONDARY);

        add(lblTitle, BorderLayout.CENTER);
        add(lblDetail, BorderLayout.SOUTH);
    }
}
