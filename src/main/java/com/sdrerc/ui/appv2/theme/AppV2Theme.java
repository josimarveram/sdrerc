package com.sdrerc.ui.appv2.theme;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

public final class AppV2Theme {

    public static final Color PRIMARY = new Color(21, 71, 117);
    public static final Color PRIMARY_DARK = new Color(12, 47, 82);
    public static final Color PRIMARY_HOVER = new Color(31, 91, 145);
    public static final Color SIDEBAR = new Color(17, 49, 78);
    public static final Color SIDEBAR_HOVER = new Color(22, 60, 94);
    public static final Color SIDEBAR_ACTIVE = new Color(10, 34, 57);
    public static final Color SIDEBAR_DARK = new Color(10, 34, 57);
    public static final Color BACKGROUND = new Color(244, 246, 249);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SURFACE_ALT = new Color(250, 251, 253);
    public static final Color BORDER = new Color(218, 225, 233);
    public static final Color BORDER_STRONG = new Color(196, 205, 216);
    public static final Color INPUT_BACKGROUND = Color.WHITE;
    public static final Color INPUT_BORDER = new Color(214, 223, 232);
    public static final Color INPUT_BORDER_HOVER = new Color(188, 201, 214);
    public static final Color INPUT_BORDER_FOCUS = PRIMARY;
    public static final Color INPUT_ICON = new Color(96, 110, 126);
    public static final Color TEXT_PRIMARY = new Color(28, 36, 46);
    public static final Color TEXT_SECONDARY = new Color(82, 95, 110);
    public static final Color SUCCESS = new Color(38, 129, 80);
    public static final Color WARNING = new Color(184, 105, 16);
    public static final Color ERROR = new Color(178, 52, 52);
    public static final Color INFO = new Color(35, 112, 168);
    public static final Color TEAL = new Color(31, 137, 141);
    public static final Color INDIGO = new Color(82, 93, 163);
    public static final Color MUTED = new Color(108, 117, 125);
    public static final Color SOFT_BLUE = new Color(229, 241, 252);
    public static final Color SOFT_GREEN = new Color(229, 244, 236);
    public static final Color SOFT_ORANGE = new Color(255, 243, 224);
    public static final Color SOFT_RED = new Color(253, 232, 232);
    public static final Color SOFT_GRAY = new Color(239, 242, 246);

    public static final String FONT_FAMILY = "Segoe UI";
    public static final int FONT_SIZE_SMALL = 12;
    public static final int FONT_SIZE_BASE = 13;
    public static final int FONT_SIZE_MEDIUM = 15;
    public static final int FONT_SIZE_TITLE = 20;
    public static final int FONT_SIZE_HERO = 30;

    public static final int SPACE_SMALL = 8;
    public static final int SPACE = 12;
    public static final int SPACE_LARGE = 20;
    public static final int SPACE_XL = 28;

    private AppV2Theme() {
    }

    public static Font fontPlain(int size) {
        return new Font(FONT_FAMILY, Font.PLAIN, size);
    }

    public static Font fontBold(int size) {
        return new Font(FONT_FAMILY, Font.BOLD, size);
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(SPACE, SPACE, SPACE, SPACE));
    }

    public static Border compactCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(SPACE_SMALL, SPACE, SPACE_SMALL, SPACE));
    }

    public static Border sectionBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(SPACE_LARGE, SPACE_LARGE, SPACE_LARGE, SPACE_LARGE));
    }

    public static Border pageBorder() {
        return BorderFactory.createEmptyBorder(SPACE_XL, SPACE_XL, SPACE_XL, SPACE_XL);
    }

    public static Border toolbarBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(SPACE, SPACE_LARGE, SPACE, SPACE_LARGE));
    }
}
