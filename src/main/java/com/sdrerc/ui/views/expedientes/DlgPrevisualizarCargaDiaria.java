package com.sdrerc.ui.views.expedientes;

import com.sdrerc.ui.common.icon.IconUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DlgPrevisualizarCargaDiaria extends JDialog {

    private final CargaDiariaImportResult preview;
    private final CargaDiariaExcelImportService importService;
    private final Runnable onImportacionFinalizada;
    private final DefaultTableModel model = new DefaultTableModel();
    private final List<JTextField> filtrosColumna = new ArrayList<>();
    private TableRowSorter<DefaultTableModel> sorter;
    private JScrollPane tableScroll;
    private JButton btnMaximizar;
    private boolean maximizado;
    private Dimension tamanoNormal;
    private Point ubicacionNormal;
    private final JTable table = new JTable(model) {
        @Override
        public String getToolTipText(java.awt.event.MouseEvent event) {
            int row = rowAtPoint(event.getPoint());
            int col = columnAtPoint(event.getPoint());
            if (row >= 0 && col >= 0) {
                Object value = getValueAt(row, col);
                return value == null ? null : value.toString();
            }
            return super.getToolTipText(event);
        }
    };

    public DlgPrevisualizarCargaDiaria(
            Window parent,
            CargaDiariaImportResult preview,
            CargaDiariaExcelImportService importService,
            Runnable onImportacionFinalizada) {
        super(parent, "Previsualización de Carga diaria", ModalityType.APPLICATION_MODAL);
        this.preview = preview;
        this.importService = importService;
        this.onImportacionFinalizada = onImportacionFinalizada;
        configurar();
        cargarDatos();
    }

    private void configurar() {
        setLayout(new BorderLayout(0, 12));
        setPreferredSize(new Dimension(1280, 650));
        setMinimumSize(new Dimension(980, 540));
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        root.setBackground(new Color(245, 247, 250));

        JLabel titulo = new JLabel("Previsualización de Carga diaria");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        titulo.setForeground(new Color(25, 42, 62));
        JLabel resumen = new JLabel(resumen());
        resumen.setForeground(new Color(72, 84, 96));

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(titulo, BorderLayout.NORTH);
        header.add(resumen, BorderLayout.CENTER);

        btnMaximizar = crearBotonMaximizarVentana();
        header.add(btnMaximizar, BorderLayout.EAST);

        model.setColumnIdentifiers(new Object[]{
            "Estado validación",
            "Observaciones",
            "Fecha solicitud",
            "Canal",
            "Referencia",
            "Tipo solicitud",
            "Procedimiento registral",
            "Tipo documento",
            "N° documento",
            "Tipo acta",
            "N° acta",
            "DNI titular",
            "Titular",
            "Titular 2",
            "Solicitado por",
            "DNI solicitante"
        });

        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new EstadoCargaDiariaRenderer());
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        ajustarColumnas();

        tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(218, 224, 231)));

        JPanel tablaConFiltros = new JPanel(new BorderLayout(0, 6));
        tablaConFiltros.setOpaque(false);
        tablaConFiltros.add(crearPanelFiltrosColumna(), BorderLayout.NORTH);
        tablaConFiltros.add(tableScroll, BorderLayout.CENTER);

        JButton btnExportar = IconUtils.createSecondaryButton("Exportar", "excel.svg");
        btnExportar.setToolTipText("Exportar previsualización a Excel");
        btnExportar.addActionListener(e -> exportarPrevisualizacionExcel());

        JButton btnImportar = IconUtils.createPrimaryButton("Importar válidos", "upload.svg");
        btnImportar.addActionListener(e -> importarValidos());
        JButton btnCerrar = IconUtils.createSecondaryButton("Cancelar", "clear.svg");
        btnCerrar.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.add(btnExportar);
        footer.add(btnImportar);
        footer.add(btnCerrar);

        root.add(header, BorderLayout.NORTH);
        root.add(tablaConFiltros, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
    }

    private JButton crearBotonMaximizarVentana() {
        JButton button = new JButton("□");
        button.setToolTipText("Maximizar ventana");
        button.setPreferredSize(new Dimension(36, 32));
        button.setMinimumSize(new Dimension(36, 32));
        button.setMaximumSize(new Dimension(36, 32));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        button.setBackground(new Color(248, 250, 252));
        button.setForeground(new Color(51, 65, 85));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 15f));
        button.addActionListener(e -> alternarMaximizado());
        return button;
    }

    private void ajustarColumnas() {
        int[] widths = {130, 520, 115, 110, 140, 150, 230, 140, 120, 140, 110, 120, 250, 250, 230, 130};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private JPanel crearPanelFiltrosColumna() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        String[] columnas = {
            "Estado", "Observaciones", "Fecha", "Canal", "Referencia", "Tipo solicitud",
            "Procedimiento", "Tipo doc.", "N° doc.", "Tipo acta", "N° acta",
            "DNI titular", "Titular", "Titular 2", "Solicitado por", "DNI"
        };
        int[] widths = {130, 520, 115, 110, 140, 150, 230, 140, 120, 140, 110, 120, 250, 250, 230, 130};

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 6);

        for (int i = 0; i < columnas.length; i++) {
            JTextField filtro = new JTextField();
            filtro.setPreferredSize(new Dimension(widths[i], 28));
            filtro.setMinimumSize(new Dimension(widths[i], 28));
            filtro.setToolTipText("Filtrar por " + columnas[i]);
            final int column = i;
            filtro.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    aplicarFiltrosColumna();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    aplicarFiltrosColumna();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    aplicarFiltrosColumna();
                }
            });
            filtrosColumna.add(filtro);

            gbc.gridx = column;
            panel.add(filtro, gbc);
        }

        JButton limpiar = IconUtils.createIconButton("Limpiar filtros de columna", "broom.svg");
        limpiar.setText("");
        limpiar.setPreferredSize(new Dimension(32, 28));
        limpiar.addActionListener(e -> limpiarFiltrosColumna());
        gbc.gridx = columnas.length;
        panel.add(limpiar, gbc);

        JScrollPane scrollFiltros = new JScrollPane(panel);
        scrollFiltros.setBorder(BorderFactory.createEmptyBorder());
        scrollFiltros.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollFiltros.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        if (tableScroll != null) {
            scrollFiltros.getHorizontalScrollBar().setModel(tableScroll.getHorizontalScrollBar().getModel());
        }

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.add(scrollFiltros, BorderLayout.CENTER);
        return contenedor;
    }

    private void aplicarFiltrosColumna() {
        if (sorter == null) {
            return;
        }

        List<RowFilter<DefaultTableModel, Integer>> filtros = new ArrayList<>();
        for (int i = 0; i < filtrosColumna.size(); i++) {
            String texto = filtrosColumna.get(i).getText();
            if (texto == null || texto.trim().isEmpty()) {
                continue;
            }
            final int columna = i;
            final String valor = normalizar(texto);
            filtros.add(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    Object cellValue = entry.getValue(columna);
                    return normalizar(cellValue == null ? "" : cellValue.toString()).contains(valor);
                }
            });
        }
        sorter.setRowFilter(filtros.isEmpty() ? null : RowFilter.andFilter(filtros));
    }

    private void limpiarFiltrosColumna() {
        for (JTextField filtro : filtrosColumna) {
            filtro.setText("");
        }
        if (sorter != null) {
            sorter.setRowFilter(null);
        }
    }

    private String normalizar(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private void alternarMaximizado() {
        if (!maximizado) {
            tamanoNormal = getSize();
            ubicacionNormal = getLocation();
            Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getMaximumWindowBounds();
            setBounds(bounds);
            maximizado = true;
            btnMaximizar.setText("❐");
            btnMaximizar.setToolTipText("Restaurar ventana");
        } else {
            if (tamanoNormal != null) {
                setSize(tamanoNormal);
            }
            if (ubicacionNormal != null) {
                setLocation(ubicacionNormal);
            } else {
                setLocationRelativeTo(getOwner());
            }
            maximizado = false;
            btnMaximizar.setText("□");
            btnMaximizar.setToolTipText("Maximizar ventana");
        }
    }

    private void cargarDatos() {
        for (CargaDiariaExcelRow item : preview.getFilas()) {
            model.addRow(new Object[]{
                item.getEstadoValidacion(),
                item.getObservacionesResumen(),
                item.getFechaSolicitudTexto(),
                item.getCanal(),
                item.getReferencia(),
                item.getTipoSolicitud(),
                item.getProcedimientoRegistral(),
                item.getTipoDocumento(),
                item.getNumeroDocumento(),
                item.getTipoActa(),
                item.getNumeroActa(),
                item.getDniTitularVisual(),
                item.getTitular(),
                item.getTitular2(),
                item.getSolicitadoPor(),
                item.getDniSolicitanteVisual()
            });
        }
    }

    private String resumen() {
        return "Total filas: " + preview.getTotal()
                + " | Válidas: " + preview.getValidas()
                + " | Advertencias: " + preview.getAdvertencias()
                + " | Errores: " + preview.getErrores()
                + " | Duplicadas: " + preview.getDuplicadas();
    }

    private void importarValidos() {
        if (preview.getImportables() == 0) {
            JOptionPane.showMessageDialog(this, "No existen registros válidos para importar.");
            return;
        }

        if (preview.getErrores() > 0 || preview.getDuplicadas() > 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Se encontraron observaciones bloqueantes. Solo se importarán registros válidos.",
                    "Carga diaria",
                    JOptionPane.WARNING_MESSAGE);
        }

        try {
            CargaDiariaImportResult result = importService.importarValidos(preview);
            JOptionPane.showMessageDialog(
                    this,
                    "Importación finalizada: " + result.getRegistrados()
                            + " registrados, " + result.getOmitidos() + " omitidos.");
            if (onImportacionFinalizada != null) {
                onImportacionFinalizada.run();
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo completar la importación: " + ex.getMessage(),
                    "Carga diaria",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportarPrevisualizacionExcel() {
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay registros para exportar.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportar previsualización");
        chooser.setSelectedFile(new File("carga_diaria_previsualizacion_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = asegurarExtensionXlsx(chooser.getSelectedFile());
        try {
            escribirPrevisualizacionExcel(archivo);
            JOptionPane.showMessageDialog(this, "Archivo Excel generado correctamente.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo exportar el archivo Excel.",
                    "Carga diaria",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private File asegurarExtensionXlsx(File archivo) {
        String nombre = archivo.getName().toLowerCase(Locale.ROOT);
        if (nombre.endsWith(".xlsx")) {
            return archivo;
        }
        return new File(archivo.getParentFile(), archivo.getName() + ".xlsx");
    }

    private void escribirPrevisualizacionExcel(File archivo) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(archivo)) {
            Sheet sheet = workbook.createSheet("Previsualización");
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle bodyStyle = crearEstiloCuerpo(workbook);

            Row header = sheet.createRow(0);
            for (int col = 0; col < table.getColumnCount(); col++) {
                Cell cell = header.createCell(col);
                cell.setCellValue(table.getColumnName(col));
                cell.setCellStyle(headerStyle);
            }

            for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
                Row row = sheet.createRow(viewRow + 1);
                int modelRow = table.convertRowIndexToModel(viewRow);
                for (int col = 0; col < table.getColumnCount(); col++) {
                    Cell cell = row.createCell(col);
                    Object value = model.getValueAt(modelRow, col);
                    cell.setCellValue(value == null ? "" : value.toString());
                    cell.setCellStyle(bodyStyle);
                }
            }

            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(
                    0,
                    Math.max(0, table.getRowCount()),
                    0,
                    table.getColumnCount() - 1));
            for (int col = 0; col < table.getColumnCount(); col++) {
                sheet.autoSizeColumn(col);
                int anchoActual = sheet.getColumnWidth(col);
                sheet.setColumnWidth(col, Math.min(Math.max(anchoActual + 800, 3200), 18000));
            }

            workbook.write(out);
        }
    }

    private CellStyle crearEstiloCabecera(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle crearEstiloCuerpo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private static class EstadoCargaDiariaRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setVerticalAlignment(SwingConstants.CENTER);
            if (!isSelected) {
                component.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                component.setForeground(new Color(30, 41, 59));
            }
            if (column == 0 && !isSelected) {
                String estado = value == null ? "" : value.toString();
                if ("VÁLIDO".equals(estado)) {
                    component.setForeground(new Color(22, 101, 52));
                } else if (CargaDiariaExcelRow.ESTADO_ADVERTENCIA.equals(estado)) {
                    component.setForeground(new Color(146, 64, 14));
                } else if (CargaDiariaExcelRow.ESTADO_ERROR.equals(estado)) {
                    component.setForeground(new Color(185, 28, 28));
                } else if (CargaDiariaExcelRow.ESTADO_DUPLICADO.equals(estado)) {
                    component.setForeground(new Color(109, 40, 217));
                }
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setFont(getFont().deriveFont(Font.PLAIN));
            }
            return component;
        }
    }
}
