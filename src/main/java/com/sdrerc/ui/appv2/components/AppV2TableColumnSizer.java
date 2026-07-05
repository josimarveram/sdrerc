package com.sdrerc.ui.appv2.components;

import java.awt.FontMetrics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class AppV2TableColumnSizer {

    private static final String AUTO_WIDTH_INSTALLED_KEY = AppV2TableColumnSizer.class.getName() + ".autoWidthInstalled";
    private AppV2TableColumnSizer() {
    }

    public static void applyWidths(JTable table, int... widths) {
        if (table == null || widths == null) {
            return;
        }
        TableColumnModel model = table.getColumnModel();
        int count = Math.min(model.getColumnCount(), widths.length);
        for (int i = 0; i < count; i++) {
            int width = widths[i];
            if (width <= 0) {
                continue;
            }
            TableColumn column = model.getColumn(i);
            column.setPreferredWidth(width);
            column.setMinWidth(Math.min(width, 80));
        }
        ensureDynamicExpedienteWidth(table);
        updateDynamicExpedienteWidth(table);
    }

    public static void applyFriendlyDefaults(JTable table) {
        if (table == null || table.getColumnModel() == null || table.getTableHeader() == null) {
            return;
        }
        TableColumnModel model = table.getColumnModel();
        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn column = model.getColumn(i);
            String header = String.valueOf(column.getHeaderValue()).toLowerCase(Locale.ROOT);
            if (isTechnical(header)) {
                hide(column);
                continue;
            }
            int width = preferredWidth(header);
            if (width > 0 && column.getPreferredWidth() < width) {
                column.setPreferredWidth(width);
            }
            if (isCompact(header)) {
                column.setMaxWidth(Math.max(width, 70));
            } else if (width > 0 && column.getMinWidth() < Math.min(width, 90)) {
                column.setMinWidth(Math.min(width, 90));
            }
        }
        ensureDynamicExpedienteWidth(table);
        updateDynamicExpedienteWidth(table);
    }

    private static boolean isTechnical(String header) {
        return header.startsWith("_");
    }

    private static void hide(TableColumn column) {
        column.setMinWidth(0);
        column.setPreferredWidth(0);
        column.setMaxWidth(0);
        column.setResizable(false);
    }

    private static boolean isCompact(String header) {
        return header.startsWith("sel")
                || header.contains("dias")
                || header.contains("días")
                || header.contains("fila");
    }

    private static int preferredWidth(String header) {
        if (header.startsWith("sel") || header.contains("fila")) {
            return 64;
        }
        if (header.contains("dias") || header.contains("días")) {
            return 88;
        }
        if (header.contains("expediente")) {
            return 165;
        }
        if (header.contains("trámite") || header.contains("tramite")) {
            return 145;
        }
        if (header.contains("procedimiento")) {
            return 220;
        }
        if (header.contains("titular") || header.contains("remitente") || header.contains("responsable")
                || header.contains("abogado") || header.contains("usuario")) {
            return 220;
        }
        if (header.contains("documento") || header.contains("resolución") || header.contains("resolucion")) {
            return 180;
        }
        if (header.contains("acta")) {
            return 130;
        }
        if (header.contains("etapa") || header.contains("estado")) {
            return 150;
        }
        if (header.contains("fecha") || header.contains("recepción") || header.contains("recepcion")
                || header.contains("registro") || header.contains("mov")) {
            return 145;
        }
        if (header.contains("observación") || header.contains("observacion") || header.contains("comentario")
                || header.contains("motivo") || header.contains("descripción") || header.contains("descripcion")) {
            return 240;
        }
        if (header.contains("alerta") || header.contains("relacion") || header.contains("asociado")) {
            return 160;
        }
        if (header.contains("publicación") || header.contains("publicacion") || header.contains("digital")) {
            return 120;
        }
        return 0;
    }

    private static void ensureDynamicExpedienteWidth(final JTable table) {
        if (table == null || Boolean.TRUE.equals(table.getClientProperty(AUTO_WIDTH_INSTALLED_KEY))) {
            return;
        }
        final TableModelListener listener = new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                scheduleDynamicExpedienteWidth(table);
            }
        };
        final PropertyChangeListener propertyListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!"model".equals(evt.getPropertyName())) {
                    return;
                }
                TableModel previous = evt.getOldValue() instanceof TableModel ? (TableModel) evt.getOldValue() : null;
                TableModel current = evt.getNewValue() instanceof TableModel ? (TableModel) evt.getNewValue() : null;
                if (previous != null) {
                    previous.removeTableModelListener(listener);
                }
                if (current != null) {
                    current.addTableModelListener(listener);
                }
                scheduleDynamicExpedienteWidth(table);
            }
        };
        TableModel model = table.getModel();
        if (model != null) {
            model.addTableModelListener(listener);
        }
        table.addPropertyChangeListener(propertyListener);
        table.putClientProperty(AUTO_WIDTH_INSTALLED_KEY, Boolean.TRUE);
    }

    private static void scheduleDynamicExpedienteWidth(final JTable table) {
        if (table == null) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            updateDynamicExpedienteWidth(table);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateDynamicExpedienteWidth(table);
                }
            });
        }
    }

    private static void updateDynamicExpedienteWidth(JTable table) {
        if (table == null || table.getColumnModel() == null || table.getTableHeader() == null) {
            return;
        }
        TableColumnModel model = table.getColumnModel();
        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn column = model.getColumn(i);
            String header = String.valueOf(column.getHeaderValue()).toLowerCase(Locale.ROOT);
            if (!header.contains("expediente") || isTechnical(header)) {
                continue;
            }
            int width = calculateDynamicWidth(table, i, column);
            if (width > 0) {
                column.setPreferredWidth(width);
                int minimum = Math.min(width, 90);
                if (column.getMinWidth() < minimum) {
                    column.setMinWidth(minimum);
                }
            }
        }
    }

    private static int calculateDynamicWidth(JTable table, int viewColumn, TableColumn column) {
        int maxWidth = 0;
        Object headerValue = column.getHeaderValue();
        if (headerValue != null) {
            maxWidth = Math.max(maxWidth, textWidth(table, headerValue.toString(), true));
        }
        int rowCount = table.getRowCount();
        for (int row = 0; row < rowCount; row++) {
            Object value = table.getValueAt(row, viewColumn);
            if (value == null) {
                continue;
            }
            maxWidth = Math.max(maxWidth, textWidth(table, value.toString(), false));
        }
        maxWidth += 28;
        if (maxWidth < 110) {
            maxWidth = 110;
        } else if (maxWidth > 320) {
            maxWidth = 320;
        }
        return maxWidth;
    }

    private static int textWidth(JTable table, String text, boolean header) {
        FontMetrics metrics;
        if (header && table.getTableHeader() != null && table.getTableHeader().getFont() != null) {
            metrics = table.getTableHeader().getFontMetrics(table.getTableHeader().getFont());
        } else {
            metrics = table.getFontMetrics(table.getFont());
        }
        if (metrics == null || text == null) {
            return 0;
        }
        return metrics.stringWidth(text);
    }
}
