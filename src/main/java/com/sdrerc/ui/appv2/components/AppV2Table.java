package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

public class AppV2Table extends JTable {

    public AppV2Table(TableModel model) {
        super(model);
        configurarBase();
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Point point = event.getPoint();
        int row = rowAtPoint(point);
        int column = columnAtPoint(point);
        if (row < 0 || column < 0) {
            return null;
        }
        Object value = getValueAt(row, column);
        if (value == null) {
            return null;
        }
        String text = DisplayNameMapperV2.valor(value.toString());
        if (text.trim().isEmpty()) {
            return null;
        }
        return "<html><body style='max-width:360px'>" + escape(text) + "</body></html>";
    }

    private void configurarBase() {
        setFillsViewportHeight(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        setRowHeight(34);
        setShowVerticalLines(false);
        setGridColor(AppV2Theme.BORDER);
        setIntercellSpacing(new java.awt.Dimension(0, 1));
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        setDefaultRenderer(Object.class, new FriendlyCellRenderer());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                AppV2TableColumnSizer.applyFriendlyDefaults(AppV2Table.this);
            }
        });
    }

    private static class FriendlyCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            String display = value == null ? "" : DisplayNameMapperV2.valor(value.toString());
            Component c = super.getTableCellRendererComponent(table, display, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            setHorizontalAlignment(isNumeric(display) ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setToolTipText(display == null || display.trim().isEmpty() ? null : display);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
                c.setForeground(AppV2Theme.TEXT_PRIMARY);
            }
            return c;
        }

        private static boolean isNumeric(String value) {
            return value != null && value.matches("-?\\d+(\\.\\d+)?");
        }
    }

    private static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
