package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;

public class AppV2TableSectionPanel extends JPanel {

    private final JPanel header = new JPanel(new BorderLayout(8, 8));

    public AppV2TableSectionPanel(Component tablePanel) {
        super(new BorderLayout(8, 8));
        setOpaque(false);
        header.setOpaque(false);
        add(header, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    public void setActions(Component actions) {
        header.add(actions, BorderLayout.WEST);
    }

    public void setStatus(Component status) {
        if (status != null) {
            status.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
            status.setForeground(AppV2Theme.TEXT_SECONDARY);
            header.add(status, BorderLayout.SOUTH);
        }
    }
}
