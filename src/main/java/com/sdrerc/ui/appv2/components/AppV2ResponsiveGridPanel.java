package com.sdrerc.ui.appv2.components;

import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
        setLayout(new GridLayout(0, currentColumns, this.horizontalGap, this.verticalGap));
        setOpaque(false);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updateColumns();
            }
        });
    }

    @Override
    public void doLayout() {
        updateColumns();
        super.doLayout();
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
        if (columns == currentColumns) {
            return;
        }
        currentColumns = columns;
        setLayout(new GridLayout(0, currentColumns, horizontalGap, verticalGap));
        revalidate();
        repaint();
    }
}
