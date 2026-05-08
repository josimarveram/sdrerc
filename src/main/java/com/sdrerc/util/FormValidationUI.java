package com.sdrerc.util;

import com.toedter.calendar.JDateChooser;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

public final class FormValidationUI
{
    private static final Color ERROR_BACKGROUND = new Color(255, 244, 244);
    private static final Color ERROR_BORDER = new Color(220, 88, 88);
    private static final Map<JComponent, ComponentState> ORIGINAL_STATE = new WeakHashMap<>();

    private FormValidationUI()
    {
    }

    public static void markInvalid(JComponent component, String message)
    {
        if (component == null) return;

        applyInvalidStyle(component, message);
        if (component instanceof JDateChooser) {
            Component editor = ((JDateChooser) component).getDateEditor().getUiComponent();
            if (editor instanceof JComponent) {
                applyInvalidStyle((JComponent) editor, message);
            }
        }
    }

    public static void clear(JComponent component)
    {
        if (component == null) return;

        restore(component);
        if (component instanceof JDateChooser) {
            Component editor = ((JDateChooser) component).getDateEditor().getUiComponent();
            if (editor instanceof JComponent) {
                restore((JComponent) editor);
            }
        }
    }

    public static void clearAll(Container container)
    {
        if (container == null) return;

        for (Component child : container.getComponents()) {
            if (child instanceof JComponent) {
                clear((JComponent) child);
            }
            if (child instanceof Container) {
                clearAll((Container) child);
            }
        }
    }

    public static void onFocusLost(JComponent component, Runnable validation)
    {
        if (component == null || validation == null) return;

        component.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e)
            {
                validation.run();
            }
        });

        if (component instanceof JDateChooser) {
            Component editor = ((JDateChooser) component).getDateEditor().getUiComponent();
            if (editor instanceof JComponent) {
                ((JComponent) editor).addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e)
                    {
                        validation.run();
                    }
                });
            }
        }
    }

    private static void applyInvalidStyle(JComponent component, String message)
    {
        ORIGINAL_STATE.computeIfAbsent(component, ComponentState::new);
        component.setBorder(BorderFactory.createLineBorder(ERROR_BORDER, 1, true));
        component.setBackground(ERROR_BACKGROUND);
        component.setToolTipText(message);
    }

    private static void restore(JComponent component)
    {
        ComponentState state = ORIGINAL_STATE.remove(component);
        if (state == null) return;

        component.setBorder(state.border);
        component.setBackground(state.background);
        component.setToolTipText(state.toolTipText);
    }

    private static final class ComponentState
    {
        private final Border border;
        private final Color background;
        private final String toolTipText;

        private ComponentState(JComponent component)
        {
            this.border = component.getBorder();
            this.background = component.getBackground();
            this.toolTipText = component.getToolTipText();
        }
    }
}
