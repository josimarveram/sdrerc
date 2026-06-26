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
        log(module, table, expectedScrollPane, null, null);
    }

    public static void log(
            String module,
            JTable table,
            JScrollPane expectedScrollPane,
            Component headerView,
            Component filterView) {
        if (!Boolean.getBoolean(PROPERTY) || table == null) {
            return;
        }
        JScrollPane enclosing = enclosingScrollPane(table);
        JScrollPane extraParent = extraParentScrollPane(expectedScrollPane != null ? expectedScrollPane : enclosing);
        JScrollPane scroll = expectedScrollPane != null ? expectedScrollPane : enclosing;
        Component columnHeaderView = scroll == null || scroll.getColumnHeader() == null
                ? null : scroll.getColumnHeader().getView();
        boolean tableScroll = scroll != null && scroll == enclosing;
        boolean externalHorizontalScroll = hasExternalHorizontalScroll(extraParent);
        boolean filtersInsideColumnHeader = filterView != null
                && columnHeaderView != null
                && SwingUtilities.isDescendingFrom(filterView, columnHeaderView);
        StringBuilder out = new StringBuilder(512);
        out.append("[SDRERC table-scroll] module=").append(safe(module));
        out.append(" tableScroll=").append(tableScroll);
        out.append(" externalHorizontalScroll=").append(externalHorizontalScroll);
        out.append(" filtersInsideColumnHeader=").append(filtersInsideColumnHeader);
        out.append(" tableName=").append(safe(table.getName()));
        out.append(" autoResizeMode=").append(table.getAutoResizeMode());
        out.append(" tableParent=").append(className(table.getParent()));
        out.append(" enclosingScroll=").append(className(enclosing));
        out.append(" expectedScroll=").append(className(expectedScrollPane));
        out.append(" scrollViewport=").append(scroll == null ? "-" : className(scroll.getViewport()));
        out.append(" viewportView=").append(scroll == null ? "-" : className(scroll.getViewport().getView()));
        out.append(" hasColumnHeader=").append(scroll != null && scroll.getColumnHeader() != null);
        out.append(" columnHeaderView=").append(className(columnHeaderView));
        out.append(" headerView=").append(className(headerView));
        out.append(" filterView=").append(className(filterView));
        out.append(" horizontalPolicy=").append(scroll == null ? "-" : scroll.getHorizontalScrollBarPolicy());
        out.append(" verticalPolicy=").append(scroll == null ? "-" : scroll.getVerticalScrollBarPolicy());
        out.append(" tablePreferred=").append(size(table.getPreferredSize()));
        out.append(" tableMinimum=").append(size(table.getMinimumSize()));
        out.append(" tableActual=").append(size(table.getSize()));
        out.append(" scrollPreferred=").append(scroll == null ? "-" : size(scroll.getPreferredSize()));
        out.append(" scrollMinimum=").append(scroll == null ? "-" : size(scroll.getMinimumSize()));
        out.append(" scrollSize=").append(scroll == null ? "-" : size(scroll.getSize()));
        out.append(" viewportExtent=").append(scroll == null ? "-" : size(scroll.getViewport().getExtentSize()));
        out.append(" columnHeaderPreferred=").append(columnHeaderView == null ? "-" : size(columnHeaderView.getPreferredSize()));
        out.append(" columnHeaderActual=").append(columnHeaderView == null ? "-" : size(columnHeaderView.getSize()));
        out.append(" extraParentScroll=").append(className(extraParent));
        out.append(" extraParentHorizontalPolicy=").append(extraParent == null ? "-" : extraParent.getHorizontalScrollBarPolicy());
        out.append(" parentChain=").append(parentChain(table, 10));
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

    private static boolean hasExternalHorizontalScroll(JScrollPane scrollPane) {
        if (scrollPane == null) {
            return false;
        }
        return scrollPane.getHorizontalScrollBarPolicy() != JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                && scrollPane.getHorizontalScrollBar() != null
                && scrollPane.getHorizontalScrollBar().isVisible();
    }

    private static String parentChain(Component component, int maxDepth) {
        StringBuilder out = new StringBuilder();
        Component current = component;
        int depth = 0;
        while (current != null && depth < maxDepth) {
            if (depth > 0) {
                out.append(">");
            }
            out.append(current.getClass().getSimpleName());
            current = current.getParent();
            depth++;
        }
        return out.toString();
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
