package com.sdrerc.ui.common.icon;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;

public final class IconUtils {

    private static final String RESOURCE_ROOT = "/com/sdrerc/ui/iconos/";
    private static final String FLAT_RESOURCE_ROOT = "com/sdrerc/ui/iconos/";
    private static final int DEFAULT_SIZE = 16;

    private IconUtils() {
    }

    public static Icon load(String iconName, int size) {
        String normalized = normalize(iconName);
        if (normalized == null) {
            return null;
        }

        String path = FLAT_RESOURCE_ROOT + normalized;
        java.net.URL resource = IconUtils.class.getClassLoader().getResource(path);
        System.out.println("[IconUtils] iconName=" + iconName);
        System.out.println("[IconUtils] path=" + path);
        System.out.println("[IconUtils] resource=" + resource);
        logToFile("iconName=" + iconName);
        logToFile("path=" + path);
        logToFile("resource=" + resource);

        if (resource == null) {
            System.err.println("No se pudo cargar icono: " + iconName + " - recurso no encontrado");
            logToFile("icon=null recurso no encontrado");
            return null;
        }

        try {
            Icon icon = new FlatSVGIcon(path, size, size);
            System.out.println("[IconUtils] icon=" + icon);
            logToFile("icon=" + icon);
            return icon;
        } catch (Throwable ex) {
            System.out.println("[IconUtils] icon=null");
            System.err.println("No se pudo cargar icono: " + iconName + " - " + ex.getMessage());
            logToFile("icon=null exception=" + ex.getClass().getName() + ": " + ex.getMessage());
            return null;
        }
    }

    public static JButton createPrimaryButton(String text, String iconName) {
        JButton button = new JButton(text);
        applyIcon(button, iconName);
        return button;
    }

    public static JButton createSecondaryButton(String text, String iconName) {
        JButton button = new JButton(text);
        applyIcon(button, iconName);
        return button;
    }

    public static JButton createIconButton(String tooltip, String iconName) {
        JButton button = new JButton();
        button.setToolTipText(tooltip);
        applyIcon(button, iconName);
        return button;
    }

    public static JMenuItem createMenuItem(String text, String iconName) {
        JMenuItem item = new JMenuItem(text);
        Icon icon = load(iconName, DEFAULT_SIZE);
        if (icon != null) {
            item.setIcon(icon);
        }
        return item;
    }

    public static void applyIcon(JButton button, String iconName) {
        Icon icon = load(iconName, DEFAULT_SIZE);
        if (icon != null) {
            button.setIcon(icon);
            button.setIconTextGap(8);
        }
    }

    private static String normalize(String iconName) {
        if (iconName == null || iconName.trim().isEmpty()) {
            return null;
        }
        String value = iconName.trim();
        return value.toLowerCase().endsWith(".svg") ? value : value + ".svg";
    }

    private static void logToFile(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter("icon-debug.log", true))) {
            out.println("[IconUtils] " + message);
        } catch (IOException ex) {
            // Diagnostico temporal: no debe afectar la carga de pantallas.
        }
    }
}
