package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AppV2SideSectionPanel extends JPanel {

    private static final int LABEL_COLUMN_WIDTH = 160;
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
        gbcLabel.weightx = 0;
        gbcLabel.fill = GridBagConstraints.NONE;
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

    public void addContent(Component component) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(6, 0, 6, 0);
        form.add(component, gbc);
        row++;
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lbl.setForeground(AppV2Theme.TEXT_SECONDARY);
        Dimension pref = lbl.getPreferredSize();
        lbl.setPreferredSize(new Dimension(LABEL_COLUMN_WIDTH, pref.height));
        lbl.setMinimumSize(new Dimension(LABEL_COLUMN_WIDTH, pref.height));
        return lbl;
    }
}
