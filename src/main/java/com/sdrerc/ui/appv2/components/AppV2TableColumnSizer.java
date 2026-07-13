package com.sdrerc.ui.appv2.components;

import java.awt.Component;
import java.util.Locale;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public final class AppV2TableColumnSizer {

    private static final int DEFAULT_MAX_COLUMN_WIDTH = 420;
    private static final int CELL_PADDING = 24;

    private AppV2TableColumnSizer() {
    }

    /**
     * Ajusta cada columna al ancho real de su contenido (encabezado + celdas visibles),
     * respetando un máximo por columna para no generar columnas excesivamente anchas.
     */
    public static void sizeToContent(JTable table) {
        sizeToContent(table, DEFAULT_MAX_COLUMN_WIDTH);
    }

    public static void sizeToContent(JTable table, int maxColumnWidth) {
        if (table == null || table.getColumnModel() == null) {
            return;
        }
        TableColumnModel model = table.getColumnModel();
        for (int col = 0; col < model.getColumnCount(); col++) {
            TableColumn column = model.getColumn(col);
            if (column.getMaxWidth() == 0) {
                continue;
            }
            int width = anchoEncabezado(table, col);
            int filas = table.getRowCount();
            for (int row = 0; row < filas; row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, col);
                Component comp = table.prepareRenderer(renderer, row, col);
                width = Math.max(width, comp.getPreferredSize().width + CELL_PADDING);
            }
            width = Math.min(width, maxColumnWidth);
            column.setPreferredWidth(Math.max(width, column.getMinWidth()));
        }
    }

    private static int anchoEncabezado(JTable table, int col) {
        if (table.getTableHeader() == null) {
            return 0;
        }
        TableColumn column = table.getColumnModel().getColumn(col);
        TableCellRenderer headerRenderer = column.getHeaderRenderer();
        if (headerRenderer == null) {
            headerRenderer = table.getTableHeader().getDefaultRenderer();
        }
        Component comp = headerRenderer.getTableCellRendererComponent(
                table, column.getHeaderValue(), false, false, -1, col);
        return comp.getPreferredSize().width + CELL_PADDING;
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
            return 185;
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
}
