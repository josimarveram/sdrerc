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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
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

        JButton btnImportar = IconUtils.createPrimaryButton("Importar válidos", "upload.svg");
        btnImportar.addActionListener(e -> importarValidos());
        btnMaximizar = IconUtils.createSecondaryButton("Maximizar", "eye.svg");
        btnMaximizar.addActionListener(e -> alternarMaximizado());
        JButton btnCerrar = IconUtils.createSecondaryButton("Cancelar", "clear.svg");
        btnCerrar.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.add(btnImportar);
        footer.add(btnMaximizar);
        footer.add(btnCerrar);

        root.add(header, BorderLayout.NORTH);
        root.add(tablaConFiltros, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
    }

    private void ajustarColumnas() {
        int[] widths = {130, 520, 115, 110, 140, 150, 230, 140, 120, 140, 110, 250, 250, 230, 130};
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
            "Titular", "Titular 2", "Solicitado por", "DNI"
        };
        int[] widths = {130, 520, 115, 110, 140, 150, 230, 140, 120, 140, 110, 250, 250, 230, 130};

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
            btnMaximizar.setText("Restaurar");
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
            btnMaximizar.setText("Maximizar");
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
