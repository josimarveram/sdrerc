package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.AppV2DisplayScale;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class AppV2Table extends JTable {

    public AppV2Table(TableModel model) {
        super(model);
        configurarBase();
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new ToolTipTableHeader(getColumnModel());
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
        setRowHeight(AppV2DisplayScale.scale(38));
        setShowVerticalLines(false);
        setGridColor(AppV2Theme.BORDER);
        setIntercellSpacing(new Dimension(0, 1));
        setSelectionBackground(new java.awt.Color(207, 229, 244));
        setSelectionForeground(AppV2Theme.TEXT_PRIMARY);
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        getTableHeader().setPreferredSize(new Dimension(0, AppV2DisplayScale.scale(56)));
        getTableHeader().setDefaultRenderer(new MultilineHeaderRenderer());
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
            int leftAccent = isSelected && column == 0 ? 4 : 0;
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, leftAccent, 1, 0, isSelected ? AppV2Theme.TEAL : AppV2Theme.BORDER),
                    BorderFactory.createEmptyBorder(0, leftAccent == 0 ? 10 : 6, 0, 10)));
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

    private static class MultilineHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            String display = value == null ? "" : DisplayNameMapperV2.valor(value.toString());
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int width = 140;
            if (table != null && column >= 0 && column < table.getColumnModel().getColumnCount()) {
                width = Math.max(52, table.getColumnModel().getColumn(column).getWidth() - 18);
            }
            setText("<html><body style='width:" + width + "px'>" + escape(display) + "</body></html>");
            setOpaque(true);
            setBackground(AppV2Theme.SURFACE_ALT);
            setForeground(AppV2Theme.TEXT_SECONDARY);
            setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
            setHorizontalAlignment(SwingConstants.LEFT);
            setVerticalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppV2Theme.BORDER_STRONG),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            setToolTipText(display);
            return this;
        }
    }

    private static class ToolTipTableHeader extends JTableHeader {

        private ToolTipTableHeader(TableColumnModel model) {
            super(model);
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            int viewColumn = columnAtPoint(event.getPoint());
            if (viewColumn < 0) {
                return null;
            }
            Object value = getColumnModel().getColumn(viewColumn).getHeaderValue();
            return value == null ? null : value.toString();
        }
    }

    private static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
