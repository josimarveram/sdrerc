package com.sdrerc.ui.appv2.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.awt.Insets;
import javax.swing.JPanel;

public class AppV2ResponsiveGridPanel extends JPanel {

    private final int minimumCellWidth;
    private final int maximumColumns;
    private final int horizontalGap;
    private final int verticalGap;
    private int currentColumns;

    public AppV2ResponsiveGridPanel(int minimumCellWidth, int maximumColumns, int horizontalGap, int verticalGap) {
        this.minimumCellWidth = Math.max(1, minimumCellWidth);
        this.maximumColumns = Math.max(1, maximumColumns);
        this.horizontalGap = Math.max(0, horizontalGap);
        this.verticalGap = Math.max(0, verticalGap);
        currentColumns = this.maximumColumns;
        setLayout(null);
        setOpaque(false);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updateColumns();
            }
        });
        addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
            @Override
            public void ancestorResized(HierarchyEvent e) {
                updateColumns();
            }
        });
    }

    @Override
    public void doLayout() {
        updateColumns();
        layoutChildren();
    }

    private void updateColumns() {
        int availableWidth = getWidth();
        if (availableWidth <= 0 && getParent() != null) {
            availableWidth = getParent().getWidth();
        }
        if (availableWidth <= 0) {
            return;
        }
        int columns = Math.max(1, Math.min(maximumColumns,
                (availableWidth + horizontalGap) / (minimumCellWidth + horizontalGap)));
        columns = Math.min(columns, Math.max(1, getComponentCount()));
        currentColumns = columns;
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Insets insets = getInsets();
        int width = getWidth();
        if (width <= 0 && getParent() != null) {
            width = getParent().getWidth();
        }
        if (width <= 0) {
            width = currentColumns * minimumCellWidth + Math.max(0, currentColumns - 1) * horizontalGap;
        }
        width = Math.max(width, minimumCellWidth);
        int height = preferredLayoutHeight(width, insets);
        return new Dimension(width, height);
    }

    private void layoutChildren() {
        Insets insets = getInsets();
        int width = Math.max(0, getWidth() - insets.left - insets.right);
        int x0 = insets.left;
        int y = insets.top;
        int count = getComponentCount();
        if (count == 0) {
            return;
        }
        int columns = Math.max(1, currentColumns);
        int cellWidth = Math.max(minimumCellWidth,
                (width - Math.max(0, columns - 1) * horizontalGap) / columns);
        int index = 0;
        while (index < count) {
            int rowStart = index;
            int rowEnd = Math.min(count, rowStart + columns);
            int rowHeight = 0;
            for (int i = rowStart; i < rowEnd; i++) {
                Component component = getComponent(i);
                Dimension pref = component.getPreferredSize();
                rowHeight = Math.max(rowHeight, pref == null ? 0 : pref.height);
            }
            int x = x0;
            for (int i = rowStart; i < rowEnd; i++) {
                Component component = getComponent(i);
                component.setBounds(x, y, cellWidth, rowHeight);
                x += cellWidth + horizontalGap;
            }
            y += rowHeight + verticalGap;
            index = rowEnd;
        }
    }

    private int preferredLayoutHeight(int width, Insets insets) {
        int count = getComponentCount();
        if (count == 0) {
            return insets.top + insets.bottom;
        }
        int columns = Math.max(1, currentColumns);
        int cellWidth = Math.max(minimumCellWidth,
                (Math.max(0, width - insets.left - insets.right) - Math.max(0, columns - 1) * horizontalGap) / columns);
        int totalHeight = insets.top + insets.bottom;
        for (int index = 0; index < count; index += columns) {
            int rowEnd = Math.min(count, index + columns);
            int rowHeight = 0;
            for (int i = index; i < rowEnd; i++) {
                Component component = getComponent(i);
                Dimension pref = component.getPreferredSize();
                if (pref != null) {
                    rowHeight = Math.max(rowHeight, pref.height);
                }
            }
            totalHeight += rowHeight;
            if (rowEnd < count) {
                totalHeight += verticalGap;
            }
        }
        return totalHeight;
    }
}
