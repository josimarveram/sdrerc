package com.sdrerc.ui.appv2.components;

import java.awt.AWTEvent;
import java.awt.Component;
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
import javax.swing.SwingUtilities;
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
}
