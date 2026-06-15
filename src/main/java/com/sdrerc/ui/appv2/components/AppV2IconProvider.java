package com.sdrerc.ui.appv2.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Icon;

public final class AppV2IconProvider {

    public static final String HOME = "HOME";
    public static final String BANDEJA = "BANDEJA";
    public static final String REGISTRO = "REGISTRO";
    public static final String ASIGNACION = "ASIGNACION";
    public static final String ANALISIS = "ANALISIS";
    public static final String VERIFICACION = "VERIFICACION";
    public static final String FIRMA_EMISION = "FIRMA_EMISION";
    public static final String EJECUCION = "EJECUCION";
    public static final String NOTIFICACION = "NOTIFICACION";
    public static final String PUBLICACION = "PUBLICACION";
    public static final String EXPEDIENTE_DIGITAL = "EXPEDIENTE_DIGITAL";
    public static final String CIERRE_ARCHIVO = "CIERRE_ARCHIVO";
    public static final String USUARIOS = "USUARIOS";
    public static final String EQUIPO_JURIDICO = "EQUIPO_JURIDICO";
    public static final String ROLES = "ROLES";
    public static final String SALIR = "SALIR";
    public static final String COLLAPSE = "COLLAPSE";
    public static final String EXPAND = "EXPAND";
    public static final String PENCIL = "PENCIL";

    private static final String ROOT = "icons/appv2/";
    private static final Map<String, String> ICONS = new LinkedHashMap<String, String>();

    static {
        ICONS.put(HOME, "home.svg");
        ICONS.put(BANDEJA, "inbox.svg");
        ICONS.put(REGISTRO, "file-plus.svg");
        ICONS.put(ASIGNACION, "user-check.svg");
        ICONS.put(ANALISIS, "file-search.svg");
        ICONS.put(VERIFICACION, "check-circle.svg");
        ICONS.put(FIRMA_EMISION, "file-signature.svg");
        ICONS.put(EJECUCION, "play-circle.svg");
        ICONS.put(NOTIFICACION, "bell.svg");
        ICONS.put(PUBLICACION, "megaphone.svg");
        ICONS.put(EXPEDIENTE_DIGITAL, "folder.svg");
        ICONS.put(CIERRE_ARCHIVO, "archive.svg");
        ICONS.put(USUARIOS, "users.svg");
        ICONS.put(EQUIPO_JURIDICO, "briefcase.svg");
        ICONS.put(ROLES, "shield.svg");
        ICONS.put(SALIR, "log-out.svg");
        ICONS.put(COLLAPSE, "panel-left-close.svg");
        ICONS.put(EXPAND, "panel-left-open.svg");
        ICONS.put(PENCIL, "pencil.svg");
    }

    private AppV2IconProvider() {
    }

    public static Icon menu(String code) {
        return load(code, 18);
    }

    public static Icon menuCollapsed(String code) {
        return load(code, 21);
    }

    public static Icon action(String code) {
        return load(code, 16);
    }

    public static Icon load(String code, int size) {
        String fileName = ICONS.get(code);
        if (fileName == null) {
            return new FallbackIcon(code, size);
        }
        String path = ROOT + fileName;
        if (AppV2IconProvider.class.getClassLoader().getResource(path) == null) {
            return new FallbackIcon(code, size);
        }
        try {
            return new FlatSVGIcon(path, size, size);
        } catch (Throwable ex) {
            return new FallbackIcon(code, size);
        }
    }

    private static final class FallbackIcon implements Icon {

        private final String code;
        private final int size;

        private FallbackIcon(String code, int size) {
            this.code = code == null ? "?" : code;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(216, 231, 242));
                g2.drawOval(x + 1, y + 1, size - 2, size - 2);
                g2.setFont(AppV2Theme.fontBold(Math.max(10, size / 2)));
                String letter = code.trim().isEmpty() ? "?" : code.substring(0, 1);
                FontMetrics metrics = g2.getFontMetrics();
                int textX = x + (size - metrics.stringWidth(letter)) / 2;
                int textY = y + ((size - metrics.getHeight()) / 2) + metrics.getAscent();
                g2.drawString(letter, textX, textY);
            } finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
