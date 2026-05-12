package com.sdrerc.ui.common.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

public final class MouseWheelScrollHelper {
    private static final String LISTENER_KEY = MouseWheelScrollHelper.class.getName() + ".listener.";
    private static final int UNIT_INCREMENT = 24;

    private MouseWheelScrollHelper() {
    }

    public static void enableMouseWheelScrollInsideForm(JScrollPane scrollPane, JComponent rootPanel) {
        if (scrollPane == null || rootPanel == null) {
            return;
        }
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(UNIT_INCREMENT);
        scrollPane.getVerticalScrollBar().setBlockIncrement(Math.max(UNIT_INCREMENT * 4, 96));
        install(scrollPane, rootPanel);
    }

    private static void install(JScrollPane scrollPane, Component component) {
        if (component == null || shouldSkipComponentTree(scrollPane, component)) {
            return;
        }

        addListenerIfNeeded(scrollPane, component);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                install(scrollPane, child);
            }
        }
    }

    private static boolean shouldSkipComponentTree(JScrollPane targetScrollPane, Component component) {
        if (component instanceof JTable || component instanceof JScrollBar) {
            return true;
        }
        if (component instanceof JScrollPane && component != targetScrollPane) {
            configureNestedScrollPane((JScrollPane) component);
            return true;
        }
        if (component instanceof JViewport) {
            Component view = ((JViewport) component).getView();
            return view instanceof JTable;
        }
        return false;
    }

    private static void configureNestedScrollPane(JScrollPane scrollPane) {
        scrollPane.setWheelScrollingEnabled(true);
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        if (verticalBar != null) {
            verticalBar.setUnitIncrement(UNIT_INCREMENT);
            verticalBar.setBlockIncrement(Math.max(UNIT_INCREMENT * 4, 96));
        }
    }

    private static void addListenerIfNeeded(JScrollPane scrollPane, Component component) {
        String key = LISTENER_KEY + System.identityHashCode(scrollPane);
        if (component instanceof JComponent) {
            JComponent jComponent = (JComponent) component;
            if (Boolean.TRUE.equals(jComponent.getClientProperty(key))) {
                return;
            }
            jComponent.putClientProperty(key, Boolean.TRUE);
        }

        component.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (shouldIgnoreEvent(e)) {
                    return;
                }
                if (scrollVertically(scrollPane, e)) {
                    e.consume();
                }
            }
        });
    }

    private static boolean shouldIgnoreEvent(MouseWheelEvent e) {
        Component source = e.getComponent();
        if (source instanceof JComboBox && ((JComboBox<?>) source).isPopupVisible()) {
            return true;
        }
        JComboBox<?> combo = (JComboBox<?>) SwingUtilities.getAncestorOfClass(JComboBox.class, source);
        return combo != null && combo.isPopupVisible();
    }

    private static boolean scrollVertically(JScrollPane scrollPane, MouseWheelEvent e) {
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        if (verticalBar == null || !verticalBar.isVisible()) {
            return false;
        }

        int oldValue = verticalBar.getValue();
        int unitIncrement = Math.max(verticalBar.getUnitIncrement(e.getWheelRotation()), UNIT_INCREMENT);
        int delta = e.getUnitsToScroll() * unitIncrement;
        if (delta == 0) {
            delta = e.getWheelRotation() * unitIncrement;
        }

        int maxValue = verticalBar.getMaximum() - verticalBar.getVisibleAmount();
        int newValue = Math.max(verticalBar.getMinimum(), Math.min(maxValue, oldValue + delta));
        if (newValue == oldValue) {
            return false;
        }
        verticalBar.setValue(newValue);
        return true;
    }
}
