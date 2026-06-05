package com.sdrerc.ui.appv2.components;

import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.table.TableModel;

public class AppV2Table extends JTable {

    public AppV2Table(TableModel model) {
        super(model);
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
        String text = value.toString();
        if (text.trim().isEmpty()) {
            return null;
        }
        return "<html><body style='max-width:360px'>" + escape(text) + "</body></html>";
    }

    private static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
