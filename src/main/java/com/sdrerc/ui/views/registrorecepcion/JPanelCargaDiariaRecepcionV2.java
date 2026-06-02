package com.sdrerc.ui.views.registrorecepcion;

import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

public class JPanelCargaDiariaRecepcionV2 extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{
                "Número de trámite",
                "Tipo de procedimiento",
                "Titular",
                "Acta",
                "Fecha recepción",
                "Remitente",
                "Estado de validación",
                "Posible duplicado",
                "Número expediente generado",
                "Observación"
            },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public JPanelCargaDiariaRecepcionV2() {
        setLayout(new BorderLayout(12, 12));
        setBackground(AppV2Theme.BACKGROUND);
        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearTablaPreview(), BorderLayout.CENTER);
        add(crearPanelNotas(), BorderLayout.SOUTH);
        cargarPrevisualizacion(Collections.<CargaDiariaPreviewDTO>emptyList());
    }

    private JPanel crearPanelSuperior() {
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout(8, 8));
        toolbar.setBackground(AppV2Theme.SURFACE);
        toolbar.setBorder(AppV2Theme.toolbarBorder());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        JButton btnArchivo = crearBotonPendiente("Seleccionar archivo");
        JButton btnPrevisualizar = crearBotonPendiente("Previsualizar");
        JButton btnValidar = crearBotonPendiente("Validar registros");
        JButton btnConfirmar = new JButton("Confirmar carga");
        btnConfirmar.setEnabled(false);
        btnConfirmar.setToolTipText("Pendiente de implementación de escritura controlada");
        acciones.add(btnArchivo);
        acciones.add(btnPrevisualizar);
        acciones.add(btnValidar);
        acciones.add(btnConfirmar);

        JLabel modo = new JLabel("Preparación visual · sin escritura");
        modo.setOpaque(true);
        modo.setBackground(AppV2Theme.SOFT_ORANGE);
        modo.setForeground(AppV2Theme.WARNING);
        modo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        modo.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        toolbar.add(acciones, BorderLayout.WEST);
        toolbar.add(modo, BorderLayout.EAST);

        JLabel ayuda = new JLabel("<html>El número de expediente se generará durante la confirmación de la carga, una vez validados los registros. En este incremento solo se prepara la previsualización.</html>");
        ayuda.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        ayuda.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel metricas = new JPanel(new GridLayout(1, 4, 12, 0));
        metricas.setOpaque(false);
        metricas.add(new MetricCardV2("Registros leídos", "0", "Pendiente de archivo", AppV2Theme.INFO));
        metricas.add(new MetricCardV2("Válidos", "0", "Pendiente de validación", AppV2Theme.SUCCESS));
        metricas.add(new MetricCardV2("Posibles duplicados", "0", "Se conservarán en la carga", AppV2Theme.WARNING));
        metricas.add(new MetricCardV2("Expedientes a generar", "0", "Pendiente de confirmación", AppV2Theme.TEAL));

        wrapper.add(toolbar, BorderLayout.NORTH);
        wrapper.add(ayuda, BorderLayout.CENTER);
        wrapper.add(metricas, BorderLayout.SOUTH);
        return wrapper;
    }

    private JScrollPane crearTablaPreview() {
        table.setRowHeight(32);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        table.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        table.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        table.setShowVerticalLines(false);
        table.setGridColor(AppV2Theme.BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(8).setPreferredWidth(190);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        return scroll;
    }

    private JPanel crearPanelNotas() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);
        panel.add(crearNota(
                "Duplicados",
                "Los posibles duplicados se conservarán en la carga y se mostrarán antes de confirmar. La asociación de duplicados se realizará en la etapa correspondiente.",
                new BadgeV2("Regla funcional", AppV2Theme.SOFT_BLUE, AppV2Theme.INFO)));
        panel.add(crearNota(
                "Generación futura",
                "La futura generación debe considerar correlativo único, año o periodo, tipo de procedimiento si aplica, concurrencia, no duplicidad, trazabilidad, historial inicial y usuario confirmante.",
                new BadgeV2("Pendiente técnico", AppV2Theme.SOFT_GRAY, AppV2Theme.TEXT_SECONDARY)));
        return panel;
    }

    private JPanel crearNota(String title, String text, BadgeV2 badge) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.cardBorder());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);

        JTextArea detail = new JTextArea(text);
        detail.setEditable(false);
        detail.setFocusable(false);
        detail.setOpaque(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        detail.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        detail.setForeground(AppV2Theme.TEXT_SECONDARY);

        panel.add(header, BorderLayout.NORTH);
        panel.add(detail, BorderLayout.CENTER);
        return panel;
    }

    private JButton crearBotonPendiente(String text) {
        JButton button = new JButton(text);
        button.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "Pendiente de implementación. Este incremento solo prepara la interfaz.",
                "Registro / Recepción V2",
                JOptionPane.INFORMATION_MESSAGE));
        return button;
    }

    private void cargarPrevisualizacion(List<CargaDiariaPreviewDTO> items) {
        tableModel.setRowCount(0);
        for (CargaDiariaPreviewDTO item : items) {
            tableModel.addRow(new Object[]{
                safe(item.getNumeroTramite()),
                safe(item.getTipoProcedimiento()),
                safe(item.getTitular()),
                safe(item.getActa()),
                item.getFechaRecepcion() == null ? "" : DATE_FORMAT.format(item.getFechaRecepcion()),
                safe(item.getRemitente()),
                safe(item.getEstadoValidacion()),
                item.isPosibleDuplicado() ? "Sí" : "No",
                safeOrPending(item.getNumeroExpedienteGenerado()),
                safe(item.getObservacion())
            });
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String safeOrPending(String value) {
        return value == null || value.trim().isEmpty() ? "Pendiente" : value;
    }
}
