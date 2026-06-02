package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class BadgeV2 extends JLabel {

    public BadgeV2(String text) {
        this(text, AppV2Theme.INFO, Color.WHITE);
    }

    public BadgeV2(String text, Color background, Color foreground) {
        super(text);
        setOpaque(true);
        setBackground(background);
        setForeground(foreground);
        setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        setBorder(BorderFactory.createEmptyBorder(5, 9, 5, 9));
    }

    public static BadgeV2 etapa(String text) {
        return new BadgeV2(text, AppV2Theme.PRIMARY, Color.WHITE);
    }

    public static BadgeV2 estado(String text) {
        return new BadgeV2(text, AppV2Theme.INFO, Color.WHITE);
    }

    public static BadgeV2 pendiente(String text) {
        return new BadgeV2(text, new Color(233, 236, 239), AppV2Theme.TEXT_PRIMARY);
    }

    public static BadgeV2 alerta(String text) {
        return new BadgeV2(text, AppV2Theme.WARNING, Color.WHITE);
    }
}
