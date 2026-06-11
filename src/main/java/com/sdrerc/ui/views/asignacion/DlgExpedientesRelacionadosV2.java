package com.sdrerc.ui.views.asignacion;

import com.sdrerc.application.sdrercapp.ExpedienteRelacionadoDeteccionService;
import com.sdrerc.application.sdrercapp.ExpedienteRelacionadoService;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionResultadoDTO;
import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.EmptyStatePanelV2;
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import com.sdrerc.ui.views.expedienteconsola.DlgConsolaExpedienteV2;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DlgExpedientesRelacionadosV2 extends JDialog {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Long idExpediente;
    private final ExpedienteRelacionadoDeteccionService deteccionService;
    private final ExpedienteRelacionadoService relacionadoService;
    private final JLabel lblEstado = new JLabel("Cargando posibles relacionados...");
    private final JButton btnAbrirConsola = new JButton("Abrir consola");
    private final JButton btnAsociar = new JButton("Asociar seleccionados");
    private final JButton btnCerrar = new JButton("Cerrar");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{
                "Sel.",
                "ID",
                "Número expediente",
                "Tipo acta",
                "Nro. acta",
                "Titular",
                "Procedimiento",
                "Etapa",
                "Estado",
                "Fecha recepción",
                "Motivo de coincidencia"
            },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : Object.class;
        }
    };
    private final JTable table = new JTable(tableModel);

    public DlgExpedientesRelacionadosV2(Window owner, Long idExpediente) {
        this(owner, idExpediente, new ExpedienteRelacionadoDeteccionService(), new ExpedienteRelacionadoService());
    }

    public DlgExpedientesRelacionadosV2(
            Window owner,
            Long idExpediente,
            ExpedienteRelacionadoDeteccionService deteccionService,
            ExpedienteRelacionadoService relacionadoService) {
        super(owner, "Expedientes relacionados", ModalityType.APPLICATION_MODAL);
        this.idExpediente = idExpediente;
        this.deteccionService = deteccionService;
        this.relacionadoService = relacionadoService;
        configurarDialogo();
        cargarRelacionados();
    }

    private void configurarDialogo() {
        setSize(1060, 640);
        setMinimumSize(new java.awt.Dimension(900, 540));
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(AppV2Theme.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setContentPane(root);

        root.add(crearHeader(), BorderLayout.NORTH);
        root.add(crearCentro(), BorderLayout.CENTER);
        root.add(crearFooter(), BorderLayout.SOUTH);

        btnAbrirConsola.addActionListener(e -> abrirConsolaSeleccionada());
        btnAsociar.addActionListener(e -> asociarSeleccionados());
        btnCerrar.addActionListener(e -> dispose());
        actualizarBotones();
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 8));
        header.setBackground(AppV2Theme.SURFACE);
        header.setBorder(AppV2Theme.cardBorder());

        JLabel title = new JLabel("Expedientes relacionados");
        title.setFont(AppV2Theme.fontBold(22));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);

        JTextArea subtitle = new JTextArea("Revise coincidencias por misma acta y titular. La asociación solo se registra cuando confirma la selección.");
        subtitle.setEditable(false);
        subtitle.setFocusable(false);
        subtitle.setOpaque(false);
        subtitle.setLineWrap(true);
        subtitle.setWrapStyleWord(true);
        subtitle.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        subtitle.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel text = new JPanel(new BorderLayout(0, 4));
        text.setOpaque(false);
        text.add(title, BorderLayout.NORTH);
        text.add(subtitle, BorderLayout.CENTER);

        header.add(text, BorderLayout.CENTER);
        header.add(new BadgeV2("Relación confirmada", AppV2Theme.SOFT_ORANGE, AppV2Theme.WARNING), BorderLayout.EAST);
        return header;
    }

    private JScrollPane crearCentro() {
        table.setRowHeight(34);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        table.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        table.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        table.setGridColor(AppV2Theme.BORDER);
        table.setShowVerticalLines(false);
        table.setDefaultRenderer(Object.class, new RelacionadosRenderer());
        table.getColumnModel().getColumn(0).setMaxWidth(52);
        table.getColumnModel().getColumn(1).setMaxWidth(70);
        table.getColumnModel().getColumn(7).setPreferredWidth(115);
        table.getColumnModel().getColumn(8).setPreferredWidth(125);
        return new JScrollPane(table);
    }

    private JPanel crearFooter() {
        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.setOpaque(false);
        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnAbrirConsola);
        acciones.add(btnAsociar);
        acciones.add(btnCerrar);

        footer.add(lblEstado, BorderLayout.CENTER);
        footer.add(acciones, BorderLayout.EAST);
        return footer;
    }

    private void cargarRelacionados() {
        setTrabajando(true, "Buscando posibles relacionados...");
        SwingWorker<List<ExpedienteRelacionadoDTO>, Void> worker = new SwingWorker<List<ExpedienteRelacionadoDTO>, Void>() {
            @Override
            protected List<ExpedienteRelacionadoDTO> doInBackground() throws Exception {
                return deteccionService.listarPosiblesRelacionados(idExpediente);
            }

            @Override
            protected void done() {
                try {
                    cargarTabla(get());
                } catch (Exception ex) {
                    mostrarError("No se pudieron consultar los expedientes relacionados.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void cargarTabla(List<ExpedienteRelacionadoDTO> items) {
        tableModel.setRowCount(0);
        for (ExpedienteRelacionadoDTO item : items) {
            tableModel.addRow(new Object[]{
                Boolean.FALSE,
                item.getIdExpediente(),
                item.getNumeroExpediente(),
                item.getTipoActa(),
                item.getNumeroActa(),
                item.getTitular(),
                item.getProcedimiento(),
                DisplayNameMapperV2.etapa(item.getEtapaCodigo()),
                DisplayNameMapperV2.estado(item.getEstadoCodigo()),
                item.getFechaRecepcion() == null ? "" : DATE_FORMAT.format(item.getFechaRecepcion()),
                item.getMotivoCoincidencia()
            });
        }
        if (items.isEmpty()) {
            lblEstado.setText("No hay nuevas coincidencias pendientes. Revise las relaciones confirmadas en la consola.");
            table.setFillsViewportHeight(true);
        } else {
            lblEstado.setText(items.size() + " posible(s) relacionado(s). Revise antes de asociar.");
        }
        actualizarBotones();
    }

    private void abrirConsolaSeleccionada() {
        Long id = obtenerIdFilaSeleccionada();
        if (id == null) {
            mostrarInfo("Seleccione un expediente para abrir la consola.");
            return;
        }
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(getOwner(), id);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void asociarSeleccionados() {
        List<Long> seleccionados = obtenerIdsMarcados();
        if (seleccionados.isEmpty()) {
            mostrarInfo("Seleccione uno o más expedientes relacionados para asociar.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Se asociarán " + seleccionados.size() + " expediente(s) como relación confirmada por misma acta y titular. ¿Desea continuar?",
                "Confirmar asociación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setTrabajando(true, "Registrando relación confirmada...");
        SwingWorker<ExpedienteRelacionResultadoDTO, Void> worker = new SwingWorker<ExpedienteRelacionResultadoDTO, Void>() {
            @Override
            protected ExpedienteRelacionResultadoDTO doInBackground() throws Exception {
                return relacionadoService.asociarRelacionados(
                        idExpediente,
                        seleccionados,
                        "Relación confirmada por misma acta y titular.");
            }

            @Override
            protected void done() {
                try {
                    ExpedienteRelacionResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            DlgExpedientesRelacionadosV2.this,
                            resultado.getMensaje(),
                            "Expedientes relacionados",
                            JOptionPane.INFORMATION_MESSAGE);
                    cargarRelacionados();
                } catch (Exception ex) {
                    mostrarError("No se pudo registrar la asociación confirmada.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private List<Long> obtenerIdsMarcados() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object checked = tableModel.getValueAt(i, 0);
            if (Boolean.TRUE.equals(checked)) {
                Object value = tableModel.getValueAt(i, 1);
                if (value instanceof Number) {
                    ids.add(((Number) value).longValue());
                }
            }
        }
        return ids;
    }

    private Long obtenerIdFilaSeleccionada() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(selected);
        Object value = tableModel.getValueAt(modelRow, 1);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private void setTrabajando(boolean trabajando, String mensaje) {
        btnAbrirConsola.setEnabled(!trabajando);
        btnAsociar.setEnabled(!trabajando);
        if (mensaje != null) {
            lblEstado.setText(mensaje);
        }
        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean tieneFilas = tableModel.getRowCount() > 0;
        btnAbrirConsola.setEnabled(tieneFilas);
        btnAsociar.setEnabled(tieneFilas);
    }

    private void mostrarInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Expedientes relacionados", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String context, Exception ex) {
        String message = ex.getMessage();
        if (message == null && ex.getCause() != null) {
            message = ex.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(
                this,
                context + (message == null ? "" : "\n" + message),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        lblEstado.setText(context);
    }

    private static class RelacionadosRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            int modelColumn = table.convertColumnIndexToModel(column);
            if (!isSelected && modelColumn == 7) {
                return StatusBadgeV2.forEtapa(value == null ? "" : value.toString());
            }
            if (!isSelected && modelColumn == 8) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
            }
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
                c.setForeground(AppV2Theme.TEXT_PRIMARY);
            }
            return c;
        }
    }
}
