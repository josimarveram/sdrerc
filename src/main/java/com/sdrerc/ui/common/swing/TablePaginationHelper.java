package com.sdrerc.ui.common.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class TablePaginationHelper {

    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int[] PAGE_SIZES = {10, 25, 50, 100};
    private static final Color BORDER = new Color(220, 226, 235);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);

    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
    private final JButton btnFirst = createButton("|<", "Primera pagina");
    private final JButton btnPrevious = createButton("<", "Pagina anterior");
    private final JButton btnNext = createButton(">", "Pagina siguiente");
    private final JButton btnLast = createButton(">|", "Ultima pagina");
    private final JLabel lblPage = new JLabel("Pagina 1 de 1");
    private final JLabel lblRange = new JLabel("Mostrando 0 de 0");
    private final JComboBox<Integer> cboPageSize = new JComboBox<>();

    private RowFilter<DefaultTableModel, Integer> baseFilter;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private int currentPage;
    private int totalRows;
    private int totalPages = 1;
    private boolean adjusting;

    public TablePaginationHelper(JTable table, TableRowSorter<DefaultTableModel> sorter) {
        this.table = table;
        this.sorter = sorter;
        configurePanel();
        registerEvents();
        refresh(true);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setBaseFilter(RowFilter<DefaultTableModel, Integer> filter) {
        this.baseFilter = filter;
        currentPage = 0;
        refresh(true);
    }

    public void clearFilter() {
        setBaseFilter(null);
    }

    public void refresh(boolean resetPage) {
        if (adjusting) {
            return;
        }
        adjusting = true;
        try {
            if (resetPage) {
                currentPage = 0;
            }

            sorter.setRowFilter(baseFilter);
            totalRows = table.getRowCount();
            totalPages = Math.max(1, (int) Math.ceil(totalRows / (double) pageSize));
            if (currentPage >= totalPages) {
                currentPage = totalPages - 1;
            }

            Set<Integer> pageRows = collectPageRows();
            RowFilter<DefaultTableModel, Integer> pageFilter = createPageFilter(pageRows);
            sorter.setRowFilter(combineFilters(baseFilter, pageFilter));
            updateControls();
        } finally {
            adjusting = false;
        }
    }

    public List<Integer> getFilteredModelRowsInSortOrder() {
        List<Integer> rows = new ArrayList<>();
        if (adjusting) {
            return rows;
        }
        adjusting = true;
        RowFilter<? super DefaultTableModel, ? super Integer> currentFilter = sorter.getRowFilter();
        try {
            sorter.setRowFilter(baseFilter);
            for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
                rows.add(table.convertRowIndexToModel(viewRow));
            }
        } finally {
            sorter.setRowFilter(currentFilter);
            updateControls();
            adjusting = false;
        }
        return rows;
    }

    private void configurePanel() {
        panel.setOpaque(false);

        lblPage.setForeground(TEXT);
        lblPage.setFont(lblPage.getFont().deriveFont(Font.BOLD, 12f));
        lblRange.setForeground(MUTED);

        JLabel lblRows = new JLabel("Filas:");
        lblRows.setForeground(MUTED);

        for (int size : PAGE_SIZES) {
            cboPageSize.addItem(size);
        }
        cboPageSize.setSelectedItem(DEFAULT_PAGE_SIZE);
        cboPageSize.setPreferredSize(new Dimension(72, 30));
        cboPageSize.setToolTipText("Filas por pagina");

        panel.add(lblRange);
        panel.add(btnFirst);
        panel.add(btnPrevious);
        panel.add(lblPage);
        panel.add(btnNext);
        panel.add(btnLast);
        panel.add(lblRows);
        panel.add(cboPageSize);
    }

    private void registerEvents() {
        btnFirst.addActionListener(e -> goToPage(0));
        btnPrevious.addActionListener(e -> goToPage(currentPage - 1));
        btnNext.addActionListener(e -> goToPage(currentPage + 1));
        btnLast.addActionListener(e -> goToPage(totalPages - 1));
        cboPageSize.addActionListener(e -> {
            Object selected = cboPageSize.getSelectedItem();
            if (selected instanceof Integer) {
                pageSize = (Integer) selected;
                currentPage = 0;
                refresh(false);
            }
        });
        sorter.addRowSorterListener(e -> {
            if (!adjusting && e.getType() == RowSorterEvent.Type.SORTED) {
                SwingUtilities.invokeLater(() -> refresh(false));
            }
        });
    }

    private void goToPage(int page) {
        int bounded = Math.max(0, Math.min(page, totalPages - 1));
        if (bounded != currentPage) {
            currentPage = bounded;
            refresh(false);
        }
    }

    private Set<Integer> collectPageRows() {
        Set<Integer> rows = new LinkedHashSet<>();
        if (totalRows == 0) {
            return rows;
        }
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, totalRows);
        for (int viewRow = start; viewRow < end; viewRow++) {
            rows.add(table.convertRowIndexToModel(viewRow));
        }
        return rows;
    }

    private RowFilter<DefaultTableModel, Integer> createPageFilter(Set<Integer> pageRows) {
        return new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                return pageRows.contains(entry.getIdentifier());
            }
        };
    }

    private RowFilter<DefaultTableModel, Integer> combineFilters(
            RowFilter<DefaultTableModel, Integer> first,
            RowFilter<DefaultTableModel, Integer> second) {
        if (first == null) {
            return second;
        }
        return new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                return first.include(entry) && second.include(entry);
            }
        };
    }

    private void updateControls() {
        int from = totalRows == 0 ? 0 : currentPage * pageSize + 1;
        int to = totalRows == 0 ? 0 : Math.min((currentPage + 1) * pageSize, totalRows);
        lblPage.setText("Pagina " + (currentPage + 1) + " de " + totalPages);
        lblRange.setText(totalRows == 0
                ? "Mostrando 0 de 0"
                : "Mostrando " + from + "-" + to + " de " + totalRows);

        boolean hasRows = totalRows > 0;
        btnFirst.setEnabled(hasRows && currentPage > 0);
        btnPrevious.setEnabled(hasRows && currentPage > 0);
        btnNext.setEnabled(hasRows && currentPage < totalPages - 1);
        btnLast.setEnabled(hasRows && currentPage < totalPages - 1);
    }

    private static JButton createButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(38, 30));
        button.setMinimumSize(new Dimension(38, 30));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        button.setBackground(Color.WHITE);
        button.setForeground(TEXT);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        return button;
    }
}
