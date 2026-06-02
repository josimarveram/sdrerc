package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SectionPanelV2 extends JPanel {

    private final JPanel content = new JPanel(new BorderLayout());

    public SectionPanelV2(String title) {
        super(new BorderLayout(0, 12));
        setBackground(AppV2Theme.SURFACE);
        setBorder(AppV2Theme.sectionBorder());

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppV2Theme.fontBold(17));
        lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);
        content.setOpaque(false);

        add(lblTitle, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    public void setContent(Component component) {
        content.removeAll();
        content.add(component, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }
}
