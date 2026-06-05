package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

public class AppV2FilterPanel extends JPanel {

    public AppV2FilterPanel() {
        super(new GridBagLayout());
        setBackground(AppV2Theme.SURFACE);
        setBorder(AppV2Theme.toolbarBorder());
    }
}
