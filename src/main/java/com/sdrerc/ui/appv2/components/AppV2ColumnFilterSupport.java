package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Adds compact per-column filters and stable painted sort indicators to an
 * operational table without coupling the behavior to a specific module.
 */
public final class AppV2ColumnFilterSupport {

    private AppV2ColumnFilterSupport() {
    }

    public static Controller install(
            JTable table,
            JScrollPane scrollPane,
            JComponent filterHost,
            Runnable beforeViewChange,
            int... excludedModelColumns) {
        return install(
                "AppV2ColumnFilterSupport",
                table,
                scrollPane,
                filterHost,
                beforeViewChange,
                excludedModelColumns);
    }

    public static Controller install(
            String moduleName,
            JTable table,
            JScrollPane scrollPane,
            JComponent filterHost,
            Runnable beforeViewChange,
            int... excludedModelColumns) {
        Controller controller = new Controller(
                moduleName,
                table,
                scrollPane,
                beforeViewChange,
                excludedModelColumns);
        if (filterHost != null) {
            filterHost.revalidate();
            filterHost.repaint();
        }
        return controller;
    }

    public static final class Controller {

        private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        };

        private final JTable table;
        private final JScrollPane scrollPane;
        private final String moduleName;
        private final Runnable beforeViewChange;
        private final Set<Integer> excludedColumns;
        private final TableRowSorter<TableModel> sorter;
        private final JTextField[] fields;
        private final FilterPanel filterPanel;
        private final HeaderPanel headerPanel;
        private boolean adjusting;

        private Controller(
                String moduleName,
                JTable table,
                JScrollPane scrollPane,
                Runnable beforeViewChange,
                int... excludedModelColumns) {
            this.moduleName = moduleName == null || moduleName.trim().isEmpty()
                    ? "AppV2ColumnFilterSupport" : moduleName.trim();
            this.table = table;
            this.scrollPane = scrollPane;
            this.beforeViewChange = beforeViewChange;
            this.excludedColumns = new HashSet<Integer>();
            if (excludedModelColumns != null) {
                for (int column : excludedModelColumns) {
                    this.excludedColumns.add(column);
                }
            }
            this.sorter = new TableRowSorter<TableModel>(table.getModel());
            this.fields = new JTextField[table.getModel().getColumnCount()];
            configureSorter();
            configureHeader();
            configureFilters();
            this.filterPanel = new FilterPanel();
            this.headerPanel = new HeaderPanel();
            configureAlignment();
        }

        public JPanel getFilterPanel() {
            return filterPanel;
        }

        public void clearFilters() {
            adjusting = true;
            try {
                for (JTextField field : fields) {
                    if (field != null) {
                        field.setText("");
                    }
                }
                sorter.setRowFilter(null);
            } finally {
                adjusting = false;
            }
        }

        private void configureSorter() {
            table.setAutoCreateRowSorter(false);
            table.setRowSorter(sorter);
            sorter.setSortsOnUpdates(true);
            Comparator<Object> comparator = new OperationalValueComparator();
            for (int column = 0; column < table.getModel().getColumnCount(); column++) {
                boolean enabled = !isExcluded(column) && !isTechnical(column);
                sorter.setSortable(column, enabled);
                if (enabled) {
                    sorter.setComparator(column, comparator);
                }
            }
            sorter.addRowSorterListener(event -> table.getTableHeader().repaint());
        }

        private void configureHeader() {
            TableCellRenderer delegate = table.getTableHeader().getDefaultRenderer();
            table.getTableHeader().setPreferredSize(new Dimension(0, 30));
            table.getTableHeader().setDefaultRenderer(new HeaderRenderer(delegate));
            table.getTableHeader().addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    int viewColumn = table.getTableHeader().columnAtPoint(event.getPoint());
                    if (viewColumn < 0) {
                        return;
                    }
                    int modelColumn = table.convertColumnIndexToModel(viewColumn);
                    if (!isExcluded(modelColumn) && !isTechnical(modelColumn)) {
                        runBeforeViewChange();
                    }
                }
            });
        }

        private void configureFilters() {
            for (int column = 0; column < fields.length; column++) {
                final int modelColumn = column;
                FilterField field = new FilterField("Filtrar");
                boolean enabled = !isExcluded(modelColumn) && !isTechnical(modelColumn);
                field.setVisible(enabled);
                field.setEnabled(enabled);
                field.setFont(AppV2Theme.fontPlain(10));
                field.setForeground(AppV2Theme.TEXT_PRIMARY);
                field.setBackground(AppV2Theme.SURFACE);
                field.setCaretColor(AppV2Theme.PRIMARY);
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AppV2Theme.BORDER),
                        BorderFactory.createEmptyBorder(1, 6, 1, 6)));
                field.setToolTipText("Filtrar "
                        + DisplayNameMapperV2.valor(table.getModel().getColumnName(modelColumn)));
                field.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent event) {
                        applyFilters();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent event) {
                        applyFilters();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent event) {
                        applyFilters();
                    }
                });
                fields[column] = field;
            }
        }

        private void configureAlignment() {
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            installColumnHeaderPanel();
            AppV2TableScrollDiagnostics.log(this.moduleName, table, scrollPane, headerPanel, filterPanel);
            SwingUtilities.invokeLater(() -> {
                installColumnHeaderPanel();
                AppV2TableScrollDiagnostics.log(
                        this.moduleName + ".afterLayout", table, scrollPane, headerPanel, filterPanel);
            });
            table.addHierarchyListener(event -> {
                long flags = event.getChangeFlags();
                if ((flags & (HierarchyEvent.DISPLAYABILITY_CHANGED | HierarchyEvent.SHOWING_CHANGED)) != 0) {
                    SwingUtilities.invokeLater(() -> {
                        installColumnHeaderPanel();
                        AppV2TableScrollDiagnostics.log(
                                this.moduleName + ".hierarchy", table, scrollPane, headerPanel, filterPanel);
                    });
                }
            });
            scrollPane.getHorizontalScrollBar().addAdjustmentListener(event -> refreshFilterPanel());
            table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
                @Override
                public void columnAdded(TableColumnModelEvent event) {
                    refreshFilterPanel();
                }

                @Override
                public void columnRemoved(TableColumnModelEvent event) {
                    refreshFilterPanel();
                }

                @Override
                public void columnMoved(TableColumnModelEvent event) {
                    refreshFilterPanel();
                }

                @Override
                public void columnMarginChanged(javax.swing.event.ChangeEvent event) {
                    refreshFilterPanel();
                }

                @Override
                public void columnSelectionChanged(javax.swing.event.ListSelectionEvent event) {
                    // The filter row only mirrors column geometry.
                }
            });
        }

        private void installColumnHeaderPanel() {
            scrollPane.setColumnHeaderView(headerPanel);
            scrollPane.revalidate();
            headerPanel.revalidate();
            headerPanel.doLayout();
            headerPanel.repaint();
        }

        private void applyFilters() {
            if (adjusting) {
                return;
            }
            runBeforeViewChange();
            List<RowFilter<TableModel, Integer>> filters = new ArrayList<RowFilter<TableModel, Integer>>();
            for (int column = 0; column < fields.length; column++) {
                JTextField field = fields[column];
                if (field == null || !field.isEnabled() || field.getText().trim().isEmpty()) {
                    continue;
                }
                final int modelColumn = column;
                final String criterion = normalize(field.getText());
                filters.add(new RowFilter<TableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                        return normalize(filterValue(entry.getValue(modelColumn))).contains(criterion);
                    }
                });
            }
            sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        }

        private void runBeforeViewChange() {
            if (beforeViewChange != null) {
                beforeViewChange.run();
            }
        }

        private void refreshFilterPanel() {
            headerPanel.revalidate();
            headerPanel.doLayout();
            headerPanel.repaint();
            filterPanel.revalidate();
            filterPanel.repaint();
        }

        private boolean isExcluded(int modelColumn) {
            return excludedColumns.contains(modelColumn);
        }

        private boolean isTechnical(int modelColumn) {
            String name = table.getModel().getColumnName(modelColumn);
            return name != null && name.startsWith("_");
        }

        private SortOrder sortOrderFor(int modelColumn) {
            for (RowSorter.SortKey key : sorter.getSortKeys()) {
                if (key.getColumn() == modelColumn) {
                    return key.getSortOrder();
                }
            }
            return SortOrder.UNSORTED;
        }

        private static String filterValue(Object value) {
            if (value == null) {
                return "";
            }
            if (value instanceof LocalDate) {
                return ((LocalDate) value).format(DATE_FORMATS[0]);
            }
            if (value instanceof LocalDateTime) {
                return ((LocalDateTime) value).format(DATE_FORMATS[2]);
            }
            return DisplayNameMapperV2.valor(String.valueOf(value));
        }

        private static String normalize(String value) {
            String text = value == null ? "" : value.trim().toUpperCase();
            text = Normalizer.normalize(text, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}+", "");
            return text.replaceAll("\\s+", " ");
        }

        private final class HeaderPanel extends JPanel {

            private HeaderPanel() {
                setLayout(null);
                setOpaque(true);
                setBackground(AppV2Theme.SURFACE_ALT);
                add(table.getTableHeader());
                add(filterPanel);
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension header = table.getTableHeader().getPreferredSize();
                Dimension filter = filterPanel.getPreferredSize();
                int width = Math.max(table.getPreferredSize().width, table.getColumnModel().getTotalColumnWidth());
                return new Dimension(width, header.height + filter.height);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public void doLayout() {
                Dimension header = table.getTableHeader().getPreferredSize();
                Dimension filter = filterPanel.getPreferredSize();
                int width = Math.max(table.getColumnModel().getTotalColumnWidth(), table.getPreferredSize().width);
                table.getTableHeader().setBounds(0, 0, width, header.height);
                filterPanel.setBounds(0, header.height, width, filter.height);
            }
        }

        private final class FilterPanel extends JPanel {

            private FilterPanel() {
                setLayout(null);
                setOpaque(true);
                setBackground(AppV2Theme.SURFACE_ALT);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppV2Theme.BORDER));
                setPreferredSize(new Dimension(0, 30));
                for (JTextField field : fields) {
                    add(field);
                }
            }

            @Override
            public void doLayout() {
                int offset = 0;
                for (int viewColumn = 0; viewColumn < table.getColumnModel().getColumnCount(); viewColumn++) {
                    TableColumn column = table.getColumnModel().getColumn(viewColumn);
                    int modelColumn = column.getModelIndex();
                    JTextField field = modelColumn >= 0 && modelColumn < fields.length
                            ? fields[modelColumn] : null;
                    int width = column.getWidth();
                    if (field != null) {
                        field.setBounds(offset + 2, 3, Math.max(0, width - 4), 24);
                    }
                    offset += width;
                }
            }
        }

        private final class HeaderRenderer implements TableCellRenderer {

            private final TableCellRenderer delegate;

            private HeaderRenderer(TableCellRenderer delegate) {
                this.delegate = delegate;
            }

            @Override
            public Component getTableCellRendererComponent(
                    JTable source,
                    Object value,
                    boolean selected,
                    boolean focus,
                    int row,
                    int viewColumn) {
                Component component = delegate.getTableCellRendererComponent(
                        source, value, selected, focus, row, viewColumn);
                if (!(component instanceof JLabel)) {
                    return component;
                }
                JLabel label = (JLabel) component;
                int modelColumn = source.convertColumnIndexToModel(viewColumn);
                label.setText(value == null ? "" : String.valueOf(value));
                label.setIcon(isExcluded(modelColumn) || isTechnical(modelColumn)
                        ? null : new SortIndicatorIcon(sortOrderFor(modelColumn)));
                label.setHorizontalTextPosition(JLabel.LEFT);
                label.setIconTextGap(6);
                label.setOpaque(true);
                label.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
                label.setForeground(AppV2Theme.TEXT_SECONDARY);
                label.setBackground(AppV2Theme.SURFACE_ALT);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, AppV2Theme.BORDER),
                        BorderFactory.createEmptyBorder(0, 8, 0, 8)));
                return label;
            }
        }

        private static final class OperationalValueComparator implements Comparator<Object> {

            @Override
            public int compare(Object left, Object right) {
                if (left == right) {
                    return 0;
                }
                if (left == null) {
                    return 1;
                }
                if (right == null) {
                    return -1;
                }
                BigDecimal leftNumber = number(left);
                BigDecimal rightNumber = number(right);
                if (leftNumber != null && rightNumber != null) {
                    return leftNumber.compareTo(rightNumber);
                }
                LocalDateTime leftDate = date(left);
                LocalDateTime rightDate = date(right);
                if (leftDate != null && rightDate != null) {
                    return leftDate.compareTo(rightDate);
                }
                return normalize(filterValue(left)).compareTo(normalize(filterValue(right)));
            }

            private static BigDecimal number(Object value) {
                if (value instanceof Number) {
                    return new BigDecimal(value.toString());
                }
                String text = String.valueOf(value).trim();
                if (!text.matches("-?\\d+(\\.\\d+)?")) {
                    return null;
                }
                try {
                    return new BigDecimal(text);
                } catch (NumberFormatException ex) {
                    return null;
                }
            }

            private static LocalDateTime date(Object value) {
                if (value instanceof LocalDateTime) {
                    return (LocalDateTime) value;
                }
                if (value instanceof LocalDate) {
                    return ((LocalDate) value).atStartOfDay();
                }
                String text = String.valueOf(value).trim();
                for (DateTimeFormatter format : DATE_FORMATS) {
                    try {
                        if (format == DATE_FORMATS[0] || format == DATE_FORMATS[1]) {
                            return LocalDate.parse(text, format).atStartOfDay();
                        }
                        return LocalDateTime.parse(text, format);
                    } catch (DateTimeParseException ex) {
                        // Try the next supported operational format.
                    }
                }
                return null;
            }
        }
    }

    private static final class FilterField extends JTextField {

        private final String prompt;

        private FilterField(String prompt) {
            this.prompt = prompt;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getText() != null && !getText().isEmpty()) {
                return;
            }
            graphics.setColor(AppV2Theme.TEXT_SECONDARY);
            graphics.setFont(getFont());
            FontMetrics metrics = graphics.getFontMetrics();
            int x = getInsets().left;
            int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            graphics.drawString(prompt, x, y);
        }
    }

    private static final class SortIndicatorIcon implements Icon {

        private final SortOrder order;

        private SortIndicatorIcon(SortOrder order) {
            this.order = order == null ? SortOrder.UNSORTED : order;
        }

        @Override
        public int getIconWidth() {
            return 9;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (order == SortOrder.ASCENDING) {
                    g2.setColor(AppV2Theme.PRIMARY);
                    paintUp(g2, x, y + 3);
                } else if (order == SortOrder.DESCENDING) {
                    g2.setColor(AppV2Theme.PRIMARY);
                    paintDown(g2, x, y + 6);
                } else {
                    g2.setColor(new Color(
                            AppV2Theme.TEXT_SECONDARY.getRed(),
                            AppV2Theme.TEXT_SECONDARY.getGreen(),
                            AppV2Theme.TEXT_SECONDARY.getBlue(),
                            150));
                    paintUp(g2, x, y + 2);
                    paintDown(g2, x, y + 8);
                }
            } finally {
                g2.dispose();
            }
        }

        private static void paintUp(Graphics2D g2, int x, int y) {
            g2.fillPolygon(
                    new int[]{x + 4, x + 1, x + 7},
                    new int[]{y, y + 5, y + 5},
                    3);
        }

        private static void paintDown(Graphics2D g2, int x, int y) {
            g2.fillPolygon(
                    new int[]{x + 1, x + 7, x + 4},
                    new int[]{y, y, y + 5},
                    3);
        }
    }
}
