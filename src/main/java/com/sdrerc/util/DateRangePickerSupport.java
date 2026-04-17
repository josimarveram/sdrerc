package com.sdrerc.util;

import com.toedter.calendar.JDateChooser;
import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import javax.swing.SwingUtilities;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

public final class DateRangePickerSupport {

    private DateRangePickerSupport() {
    }

    public static final class Range {
        private final JDateChooser fromPicker;
        private final JDateChooser toPicker;
        private final JLabel feedbackLabel;

        private Range(JDateChooser fromPicker, JDateChooser toPicker, JLabel feedbackLabel) {
            this.fromPicker = fromPicker;
            this.toPicker = toPicker;
            this.feedbackLabel = feedbackLabel;
        }

        public JDateChooser getFromPicker() {
            return fromPicker;
        }

        public JDateChooser getToPicker() {
            return toPicker;
        }

        public Date getFromDate() {
            return fromPicker.getDate();
        }

        public Date getToDate() {
            return toPicker.getDate();
        }

        public void clear() {
            fromPicker.setDate(null);
            toPicker.setDate(null);
            markValid(fromPicker);
            markValid(toPicker);
            if (feedbackLabel != null) {
                feedbackLabel.setText(" ");
            }
        }

        public boolean isValidRange() {
            Date from = startOfDay(getFromDate());
            Date to = startOfDay(getToDate());

            boolean invalid = from != null && to != null && from.after(to);
            if (invalid) {
                markInvalid(fromPicker);
                markInvalid(toPicker);
                if (feedbackLabel != null) {
                    feedbackLabel.setForeground(new Color(198, 40, 40));
                    feedbackLabel.setText("Fecha Desde no puede ser mayor que Fecha Hasta.");
                }
                return false;
            }

            markValid(fromPicker);
            markValid(toPicker);
            if (feedbackLabel != null) {
                feedbackLabel.setText(" ");
            }
            return true;
        }
    }

    public static Range replaceTextFields(
        JTextField fromField,
        JTextField toField,
        JPanel parent,
        JLabel feedbackLabel
    ) {
        Objects.requireNonNull(fromField);
        Objects.requireNonNull(toField);
        Objects.requireNonNull(parent);

        parent.doLayout();

        Rectangle fromBounds = resolveBounds(fromField);
        Rectangle toBounds = resolveBounds(toField);

        JDateChooser fromPicker = createPicker();
        JDateChooser toPicker = createPicker();

        replaceComponent(parent, fromField, fromPicker, fromBounds);
        replaceComponent(parent, toField, toPicker, toBounds);

        Range range = new Range(fromPicker, toPicker, feedbackLabel);
        fromPicker.addPropertyChangeListener("date", evt -> range.isValidRange());
        toPicker.addPropertyChangeListener("date", evt -> range.isValidRange());

        parent.revalidate();
        parent.repaint();
        return range;
    }

    public static Date startOfDay(Date date) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date endOfDay(Date date) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    public static JDateChooser replaceSpinnerDeferred(
        final JSpinner spinner,
        final JPanel parent,
        final Date initialDate
    ) {
        final JDateChooser[] pickerHolder = new JDateChooser[1];
        SwingUtilities.invokeLater(() -> {
            JDateChooser picker = replaceSpinner(spinner, parent);
            if (initialDate != null) {
                picker.setDate(initialDate);
            }
            pickerHolder[0] = picker;
        });
        return pickerHolder[0];
    }

    public static void replaceSpinnerDeferred(
        final JSpinner spinner,
        final JPanel parent,
        final Date initialDate,
        final SingleDateConsumer consumer
    ) {
        SwingUtilities.invokeLater(() -> {
            JDateChooser picker = replaceSpinner(spinner, parent);
            if (initialDate != null) {
                picker.setDate(initialDate);
            }
            if (consumer != null) {
                consumer.accept(picker);
            }
        });
    }

    private static JDateChooser createPicker() {
        JDateChooser picker = new JDateChooser();
        configurePicker(picker);
        return picker;
    }

    public static void configurePicker(JDateChooser picker) {
        picker.setDateFormatString("dd/MM/yyyy");
        picker.setOpaque(false);
        picker.putClientProperty("JComponent.roundRect", Boolean.TRUE);

        JComponent editor = picker.getDateEditor().getUiComponent();
        editor.putClientProperty("JComponent.roundRect", Boolean.TRUE);
        editor.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");

        if (picker.getCalendarButton() != null) {
            picker.getCalendarButton().setToolTipText("Abrir calendario");
            picker.getCalendarButton().putClientProperty("JButton.buttonType", "borderless");
        }
    }

    private static void replaceComponent(Container parent, JComponent oldComponent, JComponent newComponent, Rectangle bounds) {
        parent.remove(oldComponent);

        if (parent.getLayout() instanceof AbsoluteLayout) {
            parent.add(newComponent, new AbsoluteConstraints(bounds.x, bounds.y, bounds.width, bounds.height));
        } else {
            newComponent.setBounds(bounds);
            parent.add(newComponent);
        }
    }

    private static Rectangle resolveBounds(JComponent component) {
        Rectangle bounds = component.getBounds();
        if (bounds.width > 0 && bounds.height > 0) {
            return bounds;
        }

        return new Rectangle(
            component.getX(),
            component.getY(),
            Math.max(component.getPreferredSize().width, 140),
            Math.max(component.getPreferredSize().height, 32)
        );
    }

    public static void replaceTextFieldsDeferred(
        final JTextField fromField,
        final JTextField toField,
        final JPanel parent,
        final JLabel feedbackLabel,
        final RangeConsumer consumer
    ) {
        SwingUtilities.invokeLater(() -> {
            Range range = replaceTextFields(fromField, toField, parent, feedbackLabel);
            if (consumer != null) {
                consumer.accept(range);
            }
        });
    }

    public interface RangeConsumer {
        void accept(Range range);
    }

    public interface SingleDateConsumer {
        void accept(JDateChooser picker);
    }

    private static JDateChooser replaceSpinner(JSpinner spinner, JPanel parent) {
        Objects.requireNonNull(spinner);
        Objects.requireNonNull(parent);

        parent.doLayout();

        Rectangle bounds = resolveBounds(spinner);
        JDateChooser picker = createPicker();

        Object value = spinner.getValue();
        if (value instanceof Date) {
            picker.setDate((Date) value);
        }

        replaceComponent(parent, spinner, picker, bounds);
        parent.revalidate();
        parent.repaint();
        return picker;
    }

    private static void markInvalid(JDateChooser picker) {
        picker.putClientProperty("JComponent.outline", "error");
        JComponent editor = picker.getDateEditor().getUiComponent();
        editor.putClientProperty("JComponent.outline", "error");
    }

    private static void markValid(JDateChooser picker) {
        picker.putClientProperty("JComponent.outline", null);
        JComponent editor = picker.getDateEditor().getUiComponent();
        editor.putClientProperty("JComponent.outline", null);
    }
}
