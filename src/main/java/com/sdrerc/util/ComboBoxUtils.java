package com.sdrerc.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboPopup;

public final class ComboBoxUtils {
    private static final String ELLIPSIS = "...";
    private static final int EXTRA_POPUP_WIDTH = 36;
    private static final int MAX_POPUP_WIDTH = 720;
    private static final int DEFAULT_VISIBLE_ROWS = 8;

    private ComboBoxUtils() {
    }

    public static void applySmartRenderer(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JComboBox<?>) {
                installRenderer((JComboBox<?>) component);
            }

            if (component instanceof Container) {
                applySmartRenderer((Container) component);
            }
        }
    }

    public static void installRenderer(JComboBox<?> comboBox) {
        if (comboBox.getRenderer() instanceof SmartComboRenderer) {
            return;
        }

        comboBox.setRenderer(new SmartComboRenderer(comboBox));
        comboBox.setMaximumRowCount(DEFAULT_VISIBLE_ROWS);
        updateSelectedTooltip(comboBox);
        comboBox.addActionListener(e -> updateSelectedTooltip(comboBox));
        installPopupWidthHandler(comboBox);
    }

    private static void updateSelectedTooltip(JComboBox<?> comboBox) {
        Object selected = comboBox.getSelectedItem();
        comboBox.setToolTipText(selected == null ? null : selected.toString());
    }

    private static void installPopupWidthHandler(JComboBox<?> comboBox) {
        for (PopupMenuListener listener : comboBox.getPopupMenuListeners()) {
            if (listener instanceof PopupWidthAdjuster) {
                return;
            }
        }

        comboBox.addPopupMenuListener(new PopupWidthAdjuster(comboBox));
    }

    private static final class SmartComboRenderer extends DefaultListCellRenderer {
        private final JComboBox<?> comboBox;

        private SmartComboRenderer(JComboBox<?> comboBox) {
            this.comboBox = comboBox;
        }

        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus
            );

            String fullText = value == null ? "" : value.toString();

            if (index < 0) {
                int availableWidth = Math.max(comboBox.getWidth() - 34, 60);
                label.setText(truncateToFit(label, fullText, availableWidth));
            } else {
                label.setText(fullText);
            }

            label.setToolTipText(fullText.isEmpty() ? null : fullText);
            return label;
        }

        private String truncateToFit(JLabel label, String text, int maxWidth) {
            if (text == null || text.isEmpty()) {
                return "";
            }

            if (label.getFontMetrics(label.getFont()).stringWidth(text) <= maxWidth) {
                return text;
            }

            StringBuilder builder = new StringBuilder(text);
            while (builder.length() > 0
                    && label.getFontMetrics(label.getFont())
                            .stringWidth(builder + ELLIPSIS) > maxWidth) {
                builder.setLength(builder.length() - 1);
            }

            return builder.length() == 0 ? ELLIPSIS : builder + ELLIPSIS;
        }
    }

    private static final class PopupWidthAdjuster implements PopupMenuListener {
        private final JComboBox<?> comboBox;

        private PopupWidthAdjuster(JComboBox<?> comboBox) {
            this.comboBox = comboBox;
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            SwingUtilities.invokeLater(this::adjustPopup);
        }

        private void adjustPopup() {
            Object child = comboBox.getAccessibleContext().getAccessibleChild(0);
            if (!(child instanceof BasicComboPopup)) {
                return;
            }

            BasicComboPopup popup = (BasicComboPopup) child;
            JList<?> list = popup.getList();
            JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);

            int width = calculatePopupWidth(list);
            int height = calculatePopupHeight(list);
            Dimension size = new Dimension(width, height);

            scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            );
            scrollPane.setPreferredSize(size);
            scrollPane.setMinimumSize(size);
            scrollPane.setMaximumSize(size);

            popup.setPreferredSize(size);
            popup.setMinimumSize(size);
            popup.setMaximumSize(size);
            popup.revalidate();
        }

        private int calculatePopupWidth(JList<?> list) {
            int widest = comboBox.getWidth();
            @SuppressWarnings("rawtypes")
            ListCellRenderer renderer = comboBox.getRenderer();

            for (int i = 0; i < comboBox.getItemCount(); i++) {
                Object item = comboBox.getItemAt(i);
                @SuppressWarnings("unchecked")
                Component component = renderer.getListCellRendererComponent(
                    (JList) list, item, i, false, false
                );
                widest = Math.max(widest, component.getPreferredSize().width + EXTRA_POPUP_WIDTH);
            }

            return Math.min(widest, MAX_POPUP_WIDTH);
        }

        private int calculatePopupHeight(JList<?> list) {
            int itemCount = comboBox.getItemCount();
            if (itemCount == 0) {
                return comboBox.getHeight();
            }

            int visibleRows = Math.min(itemCount, comboBox.getMaximumRowCount());
            Rectangle bounds = list.getCellBounds(0, visibleRows - 1);
            int listHeight = bounds != null ? bounds.height : visibleRows * list.getFixedCellHeight();
            int borderHeight = 6;
            return listHeight + borderHeight;
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }
}
