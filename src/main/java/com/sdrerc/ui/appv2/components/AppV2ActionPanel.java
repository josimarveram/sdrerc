package com.sdrerc.ui.appv2.components;

import java.awt.FlowLayout;
import javax.swing.JPanel;

public class AppV2ActionPanel extends JPanel {

    public AppV2ActionPanel(int alignment) {
        super(new FlowLayout(alignment, 8, 0));
        setOpaque(false);
    }

    public static AppV2ActionPanel left() {
        return new AppV2ActionPanel(FlowLayout.LEFT);
    }

    public static AppV2ActionPanel right() {
        return new AppV2ActionPanel(FlowLayout.RIGHT);
    }
}
