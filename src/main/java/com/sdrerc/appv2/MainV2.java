package com.sdrerc.appv2;

import com.sdrerc.ui.appv2.MenuPrincipalV2;
import com.sdrerc.ui.common.AppUiConfig;
import javax.swing.SwingUtilities;

public class MainV2 {

    public static void main(String[] args) {
        AppUiConfig.install();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MenuPrincipalV2 menu = new MenuPrincipalV2();
                menu.setLocationRelativeTo(null);
                menu.setVisible(true);
            }
        });
    }
}
