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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

public final class FormValidationUI
{
    private static final Color ERROR_BACKGROUND = new Color(255, 246, 246);
    private static final Color ERROR_BORDER = new Color(211, 84, 84);
    private static final Color SUCCESS_BACKGROUND = new Color(244, 252, 247);
    private static final Color SUCCESS_BORDER = new Color(74, 154, 104);
    private static final Map<JComponent, ComponentState> ORIGINAL_STATE = new WeakHashMap<>();

    private FormValidationUI()
    {
    }

    public static void markInvalid(JComponent component, String message)
    {
        if (component == null) return;

        applyState(component, "error", ERROR_BORDER, ERROR_BACKGROUND, message);
        if (component instanceof JDateChooser) {
            Component editor = ((JDateChooser) component).getDateEditor().getUiComponent();
            if (editor instanceof JComponent) {
                applyState((JComponent) editor, "error", ERROR_BORDER, ERROR_BACKGROUND, message);
            }
        }
    }

    public static void markValid(JComponent component)
    {
        if (component == null) return;

        applyState(component, "success", SUCCESS_BORDER, SUCCESS_BACKGROUND, null);
        if (component instanceof JDateChooser) {
            Component editor = ((JDateChooser) component).getDateEditor().getUiComponent();
            if (editor instanceof JComponent) {
                applyState((JComponent) editor, "success", SUCCESS_BORDER, SUCCESS_BACKGROUND, null);
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

    public static void clearState(JComponent component)
    {
        clear(component);
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

    public static void onTextChanged(JTextField field, Runnable validation)
    {
        onTextChanged((JTextComponent) field, validation);
    }

    public static void onTextChanged(JTextArea area, Runnable validation)
    {
        onTextChanged((JTextComponent) area, validation);
    }

    public static void onSelectionChanged(JComboBox<?> combo, Runnable validation)
    {
        if (combo == null || validation == null) return;
        combo.addActionListener(e -> validation.run());
    }

    public static void onDateChanged(JDateChooser dateChooser, Runnable validation)
    {
        if (dateChooser == null || validation == null) return;
        dateChooser.addPropertyChangeListener("date", e -> validation.run());
    }

    private static void onTextChanged(JTextComponent textComponent, Runnable validation)
    {
        if (textComponent == null || validation == null) return;
        textComponent.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                validation.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                validation.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                validation.run();
            }
        });
    }

    private static void applyState(JComponent component, String outline, Color border, Color background, String message)
    {
        ORIGINAL_STATE.computeIfAbsent(component, ComponentState::new);
        component.putClientProperty("JComponent.outline", outline);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1, true),
                BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        component.setBackground(background);
        component.setToolTipText(message);
    }

    private static void restore(JComponent component)
    {
        ComponentState state = ORIGINAL_STATE.remove(component);
        if (state == null) return;

        component.setBorder(state.border);
        component.setBackground(state.background);
        component.setToolTipText(state.toolTipText);
        component.putClientProperty("JComponent.outline", state.outline);
    }

    private static final class ComponentState
    {
        private final Border border;
        private final Color background;
        private final String toolTipText;
        private final Object outline;

        private ComponentState(JComponent component)
        {
            this.border = component.getBorder();
            this.background = component.getBackground();
            this.toolTipText = component.getToolTipText();
            this.outline = component.getClientProperty("JComponent.outline");
        }
    }
}
