package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JLabel;

public class AppV2SearchToolbar extends AppV2FilterPanel {

    private int filterColumn;

    public AppV2SearchToolbar() {
        super();
    }

    public void addSearchRow(String label, Component searchField, Component actions) {
        GridBagConstraints gbc = baseConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(label(label), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(searchField, gbc);

        gbc.gridx = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(actions, gbc);
    }

    public void addFilter(String label, Component component) {
        GridBagConstraints gbc = baseConstraints();
        gbc.gridy = 1;
        gbc.gridx = filterColumn++;
        add(label(label), gbc);

        gbc.gridx = filterColumn++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(component, gbc);
    }

    public void addCompactFilter(Component component) {
        GridBagConstraints gbc = baseConstraints();
        gbc.gridy = 1;
        gbc.gridx = filterColumn;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(component, gbc);
        filterColumn += 2;
    }

    private GridBagConstraints baseConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lbl.setForeground(AppV2Theme.TEXT_SECONDARY);
        return lbl;
    }
}
