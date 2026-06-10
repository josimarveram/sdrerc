package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AppV2SideSectionPanel extends JPanel {

    private final JPanel form = new JPanel(new GridBagLayout());
    private int row;

    public AppV2SideSectionPanel(String title) {
        super(new java.awt.BorderLayout(0, 10));
        setOpaque(false);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppV2Theme.BORDER));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);

        form.setOpaque(false);
        add(lblTitle, java.awt.BorderLayout.NORTH);
        add(form, java.awt.BorderLayout.CENTER);
    }

    public void addRow(String label, Component component) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.NORTHWEST;
        gbcLabel.insets = new Insets(6, 0, 6, 14);

        GridBagConstraints gbcValue = new GridBagConstraints();
        gbcValue.gridx = 1;
        gbcValue.gridy = row;
        gbcValue.weightx = 1;
        gbcValue.fill = GridBagConstraints.HORIZONTAL;
        gbcValue.insets = new Insets(6, 0, 6, 0);

        form.add(label(label), gbcLabel);
        form.add(component, gbcValue);
        row++;
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lbl.setForeground(AppV2Theme.TEXT_SECONDARY);
        return lbl;
    }
}
