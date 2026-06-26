package com.sdrerc.ui.common;

import com.formdev.flatlaf.FlatLightLaf;
import com.sdrerc.ui.appv2.util.AppV2DisplayScale;
import java.awt.Font;
import javax.swing.UIManager;

public final class AppUiConfig {
    private static final String UI_FONT = "Segoe UI";
    private static final int UI_FONT_SIZE = 13;

    private AppUiConfig() {
    }

    public static void install()
    {
        configurarPropiedadesSistema();
        configurarFlatLaf();
    }

    private static void configurarPropiedadesSistema()
    {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.dpiaware", "true");
    }

    private static void configurarFlatLaf()
    {
        Font defaultFont = new Font(UI_FONT, Font.PLAIN, AppV2DisplayScale.scaleFont(UI_FONT_SIZE));
        UIManager.put("defaultFont", defaultFont);

        FlatLightLaf.setup();

        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", true);
    }
}
