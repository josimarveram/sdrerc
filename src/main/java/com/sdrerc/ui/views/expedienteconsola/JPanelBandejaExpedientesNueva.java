package com.sdrerc.ui.views.expedienteconsola;

import com.sdrerc.application.sdrercapp.ExpedienteConsultaService;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteBandejaDTO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class JPanelBandejaExpedientesNueva extends JPanel {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ExpedienteConsultaService consultaService;
    private final JTextField txtBusqueda = new JTextField(18);
    private final JTextField txtEtapa = new JTextField(12);
    private final JTextField txtEstado = new JTextField(12);
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnVerDetalle = new JButton("Ver detalle");
    private final JLabel lblResultado = new JLabel("Sin busqueda ejecutada");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{
                "ID",
                "Expediente",
                "Tramite",
                "Etapa",
                "Estado",
                "Abogado inicial",
                "Responsable",
                "Equipo",
                "Registro",
                "Ultimo mov.",
                "Vencimiento",
                "Dias",
                "Publicacion",
                "Digital"
            },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public JPanelBandejaExpedientesNueva() {
        this(new ExpedienteConsultaService());
    }

    public JPanelBandejaExpedientesNueva(ExpedienteConsultaService consultaService) {
        this.consultaService = consultaService;
        configurarLayout();
        configurarTabla();
        configurarEventos();
    }

    private void configurarLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        filtros.setBorder(BorderFactory.createTitledBorder("Bandeja SDRERC_APP - solo lectura"));
        filtros.add(new JLabel("Buscar"));
        filtros.add(txtBusqueda);
        filtros.add(new JLabel("Etapa"));
        filtros.add(txtEtapa);
        filtros.add(new JLabel("Estado"));
        filtros.add(txtEstado);
        filtros.add(new JLabel("Limite"));
        filtros.add(spnLimite);
        filtros.add(btnBuscar);
        filtros.add(btnLimpiar);
        filtros.add(btnVerDetalle);

        btnVerDetalle.setEnabled(false);
        btnVerDetalle.setToolTipText("Pendiente de implementacion");

        JPanel superior = new JPanel(new BorderLayout(8, 8));
        JLabel titulo = new JLabel("Nueva bandeja de expedientes");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        superior.add(titulo, BorderLayout.NORTH);
        superior.add(filtros, BorderLayout.CENTER);
        superior.add(lblResultado, BorderLayout.SOUTH);

        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void configurarTabla() {
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new BandejaCellRenderer());
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnVerDetalle.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "Detalle pendiente de implementacion.",
                "SDRERC",
                JOptionPane.INFORMATION_MESSAGE));
    }

    private void buscar() {
        setBuscando(true);
        String texto = txtBusqueda.getText();
        String etapa = txtEtapa.getText();
        String estado = txtEstado.getText();
        int limite = ((Number) spnLimite.getValue()).intValue();

        SwingWorker<List<ExpedienteBandejaDTO>, Void> worker = new SwingWorker<List<ExpedienteBandejaDTO>, Void>() {
            @Override
            protected List<ExpedienteBandejaDTO> doInBackground() throws Exception {
                return consultaService.buscarBandeja(texto, etapa, estado, limite);
            }

            @Override
            protected void done() {
                try {
                    cargarTabla(get());
                } catch (Exception ex) {
                    mostrarError(ex);
                } finally {
                    setBuscando(false);
                }
            }
        };
        worker.execute();
    }

    private void limpiar() {
        txtBusqueda.setText("");
        txtEtapa.setText("");
        txtEstado.setText("");
        spnLimite.setValue(200);
        tableModel.setRowCount(0);
        lblResultado.setText("Filtros limpiados");
    }

    private void cargarTabla(List<ExpedienteBandejaDTO> expedientes) {
        tableModel.setRowCount(0);
        for (ExpedienteBandejaDTO item : expedientes) {
            tableModel.addRow(new Object[]{
                item.getIdExpediente(),
                item.getNumeroExpediente(),
                item.getNumeroTramiteDocumentario(),
                item.getEtapaCodigo(),
                item.getEstadoCodigo(),
                item.getAbogadoInicial(),
                item.getResponsableActual(),
                item.getEquipoActual(),
                formatDateTime(item.getFechaRegistro()),
                formatDateTime(item.getFechaUltimoMovimiento()),
                item.getFechaVencimiento() == null ? "" : DATE_FORMAT.format(item.getFechaVencimiento()),
                item.getDiasRestantes() == null ? "" : item.getDiasRestantes(),
                item.isRequierePublicacion() ? "Si" : "No",
                item.isExpedienteDigitalCompleto() ? "Completo" : "Pendiente"
            });
        }
        lblResultado.setText(expedientes.size() + " expediente(s) encontrado(s)");
    }

    private void setBuscando(boolean buscando) {
        btnBuscar.setEnabled(!buscando);
        btnLimpiar.setEnabled(!buscando);
        lblResultado.setText(buscando ? "Consultando SDRERC_APP..." : lblResultado.getText());
    }

    private void mostrarError(Exception ex) {
        String message = ex.getMessage();
        if (message == null && ex.getCause() != null) {
            message = ex.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(
                this,
                message == null ? "No se pudo consultar la bandeja." : message,
                "Error de consulta",
                JOptionPane.ERROR_MESSAGE);
        lblResultado.setText("Error al consultar SDRERC_APP");
    }

    private static String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME_FORMAT.format(dateTime);
    }

    private static class BandejaCellRenderer extends DefaultTableCellRenderer {
        private final Color etapaColor = new Color(230, 242, 255);
        private final Color estadoColor = new Color(232, 245, 233);
        private final Color alertaColor = new Color(255, 235, 238);

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                int modelColumn = table.convertColumnIndexToModel(column);
                if (modelColumn == 3) {
                    c.setBackground(etapaColor);
                } else if (modelColumn == 4) {
                    c.setBackground(estadoColor);
                } else if (modelColumn == 11 && esVencido(value)) {
                    c.setBackground(alertaColor);
                }
            }
            return c;
        }

        private boolean esVencido(Object value) {
            if (value instanceof Number) {
                return ((Number) value).longValue() < 0;
            }
            try {
                return value != null && !value.toString().isEmpty() && Long.parseLong(value.toString()) < 0;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
    }
}
