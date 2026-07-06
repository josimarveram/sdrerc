package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.GridBagLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class AppV2FilterPanel extends JPanel {

    public AppV2FilterPanel() {
        super(new GridBagLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
    }
}
