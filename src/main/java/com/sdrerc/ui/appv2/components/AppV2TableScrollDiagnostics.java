package com.sdrerc.ui.appv2.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

/**
 * Optional diagnostics for table/header scroll issues in client laptops.
 * Enable with: -Dsdrerc.debug.tableScroll=true
 */
public final class AppV2TableScrollDiagnostics {

    private static final String PROPERTY = "sdrerc.debug.tableScroll";

    private AppV2TableScrollDiagnostics() {
    }

    public static void log(String module, JTable table, JScrollPane expectedScrollPane) {
        if (!Boolean.getBoolean(PROPERTY) || table == null) {
            return;
        }
        JScrollPane enclosing = enclosingScrollPane(table);
        JScrollPane extraParent = extraParentScrollPane(expectedScrollPane != null ? expectedScrollPane : enclosing);
        JScrollPane scroll = expectedScrollPane != null ? expectedScrollPane : enclosing;
        StringBuilder out = new StringBuilder(512);
        out.append("[SDRERC table-scroll] module=").append(safe(module));
        out.append(" tableName=").append(safe(table.getName()));
        out.append(" autoResizeMode=").append(table.getAutoResizeMode());
        out.append(" tableParent=").append(className(table.getParent()));
        out.append(" enclosingScroll=").append(className(enclosing));
        out.append(" expectedScroll=").append(className(expectedScrollPane));
        out.append(" scrollViewport=").append(scroll == null ? "-" : className(scroll.getViewport()));
        out.append(" viewportView=").append(scroll == null ? "-" : className(scroll.getViewport().getView()));
        out.append(" hasColumnHeader=").append(scroll != null && scroll.getColumnHeader() != null);
        out.append(" columnHeaderView=").append(scroll == null || scroll.getColumnHeader() == null
                ? "-" : className(scroll.getColumnHeader().getView()));
        out.append(" horizontalPolicy=").append(scroll == null ? "-" : scroll.getHorizontalScrollBarPolicy());
        out.append(" verticalPolicy=").append(scroll == null ? "-" : scroll.getVerticalScrollBarPolicy());
        out.append(" tablePreferred=").append(size(table.getPreferredSize()));
        out.append(" scrollPreferred=").append(scroll == null ? "-" : size(scroll.getPreferredSize()));
        out.append(" scrollSize=").append(scroll == null ? "-" : size(scroll.getSize()));
        out.append(" viewportExtent=").append(scroll == null ? "-" : size(scroll.getViewport().getExtentSize()));
        out.append(" extraParentScroll=").append(className(extraParent));
        out.append(" extraParentHorizontalPolicy=").append(extraParent == null ? "-" : extraParent.getHorizontalScrollBarPolicy());
        System.out.println(out.toString());
    }

    private static JScrollPane enclosingScrollPane(JTable table) {
        Container parent = table.getParent();
        if (parent instanceof JViewport) {
            Container grandParent = parent.getParent();
            if (grandParent instanceof JScrollPane) {
                return (JScrollPane) grandParent;
            }
        }
        return (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, table);
    }

    private static JScrollPane extraParentScrollPane(Component start) {
        if (start == null) {
            return null;
        }
        Container parent = start.getParent();
        while (parent != null) {
            if (parent instanceof JScrollPane) {
                return (JScrollPane) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private static String className(Object value) {
        return value == null ? "-" : value.getClass().getName();
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private static String size(Dimension dimension) {
        return dimension == null ? "-" : dimension.width + "x" + dimension.height;
    }
}
