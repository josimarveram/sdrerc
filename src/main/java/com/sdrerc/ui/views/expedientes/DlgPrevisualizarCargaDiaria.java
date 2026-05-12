package com.sdrerc.ui.views.expedientes;

import com.sdrerc.ui.common.icon.IconUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DlgPrevisualizarCargaDiaria extends JDialog {

    private final CargaDiariaImportResult preview;
    private final CargaDiariaExcelImportService importService;
    private final Runnable onImportacionFinalizada;
    private final DefaultTableModel model = new DefaultTableModel();
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
        ajustarColumnas();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(218, 224, 231)));

        JButton btnImportar = IconUtils.createPrimaryButton("Importar válidos", "upload.svg");
        btnImportar.addActionListener(e -> importarValidos());
        JButton btnCerrar = IconUtils.createSecondaryButton("Cancelar", "clear.svg");
        btnCerrar.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.add(btnImportar);
        footer.add(btnCerrar);

        root.add(header, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
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
