package com.sdrerc.ui.appv2.components;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

public final class AppV2CopyTextSupport {

    private static final Set<Window> ENABLED_WINDOWS =
            Collections.newSetFromMap(new WeakHashMap<Window, Boolean>());
    private static boolean listenerInstalled;

    private AppV2CopyTextSupport() {
    }

    public static void installForWindow(Window window) {
        if (window == null) {
            return;
        }
        synchronized (AppV2CopyTextSupport.class) {
            ENABLED_WINDOWS.add(window);
            if (!listenerInstalled) {
                Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
                    @Override
                    public void eventDispatched(AWTEvent event) {
                        if (event instanceof MouseEvent) {
                            handleMouseEvent((MouseEvent) event);
                        }
                    }
                }, AWTEvent.MOUSE_EVENT_MASK);
                listenerInstalled = true;
            }
        }
    }

    private static void handleMouseEvent(MouseEvent event) {
        if (!event.isPopupTrigger()) {
            return;
        }
        if (!(event.getSource() instanceof Component)) {
            return;
        }

        Component component = (Component) event.getSource();
        if (!belongsToEnabledWindow(component)) {
            return;
        }
        if (!isSupported(component)) {
            return;
        }
        if (hasCustomPopup(component)) {
            return;
        }

        String text = resolveText(component);
        if (!hasText(text)) {
            return;
        }

        JPopupMenu popup = new JPopupMenu();
        JMenuItem copyItem = new JMenuItem("Copiar");
        copyItem.addActionListener(e -> copyToClipboard(text));
        popup.add(copyItem);
        if (component instanceof JLabel) {
            JMenuItem selectItem = new JMenuItem("Seleccionar texto");
            selectItem.addActionListener(e -> showSelectableTextPopup(component, text));
            popup.add(selectItem);
        }
        popup.show(component, event.getX(), event.getY());
        event.consume();
    }

    private static boolean belongsToEnabledWindow(Component component) {
        Window window = SwingUtilities.getWindowAncestor(component);
        while (window != null) {
            synchronized (AppV2CopyTextSupport.class) {
                if (ENABLED_WINDOWS.contains(window)) {
                    return true;
                }
            }
            window = window.getOwner();
        }
        return false;
    }

    private static boolean isSupported(Component component) {
        return component instanceof JLabel || component instanceof JTextComponent;
    }

    private static boolean hasCustomPopup(Component component) {
        return component instanceof JComponent
                && ((JComponent) component).getComponentPopupMenu() != null;
    }

    private static String resolveText(Component component) {
        if (component instanceof JPasswordField) {
            return null;
        }
        if (component instanceof JTextComponent) {
            JTextComponent textComponent = (JTextComponent) component;
            String selectedText = textComponent.getSelectedText();
            if (hasText(selectedText)) {
                return selectedText.trim();
            }
            String text = textComponent.getText();
            return text == null ? null : text.trim();
        }
        if (component instanceof JLabel) {
            return normalizeLabelText(((JLabel) component).getText());
        }
        return null;
    }

    private static String normalizeLabelText(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text;
        if (normalized.toLowerCase(Locale.ROOT).contains("<html")) {
            normalized = normalized.replaceAll("(?i)<br\\s*/?>", "\n");
            normalized = normalized.replaceAll("<[^>]*>", "");
        }
        normalized = normalized
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"");
        return normalized.trim();
    }

    private static boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }

    private static void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(text), null);
    }

    private static void showSelectableTextPopup(Component owner, String text) {
        if (!hasText(text)) {
            return;
        }

        JPopupMenu popup = new JPopupMenu();
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(owner.getFont());
        area.setRows(Math.min(8, Math.max(2, countLines(text))));
        area.setColumns(Math.min(64, Math.max(28, longestLine(text))));

        JScrollPane scroll = new JScrollPane(area);
        int width = Math.min(560, Math.max(300, owner.getWidth() + 120));
        int height = Math.min(240, Math.max(90, area.getPreferredSize().height + 28));
        scroll.setPreferredSize(new Dimension(width, height));
        popup.add(scroll);

        JMenuItem copySelection = new JMenuItem("Copiar selección");
        copySelection.addActionListener(e -> {
            String selected = area.getSelectedText();
            copyToClipboard(hasText(selected) ? selected : area.getText());
            popup.setVisible(false);
        });
        popup.add(copySelection);

        popup.show(owner, 0, owner.getHeight());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                area.requestFocusInWindow();
                area.selectAll();
            }
        });
    }

    private static int countLines(String text) {
        return text.split("\\R", -1).length;
    }

    private static int longestLine(String text) {
        String[] lines = text.split("\\R", -1);
        int max = 0;
        for (String line : lines) {
            max = Math.max(max, line.length());
        }
        return max;
    }
}
