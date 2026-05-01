package com.sdrerc.ui.views.usuario;

import com.sdrerc.domain.model.EquipoJuridicoImportItem;
import com.sdrerc.domain.model.EquipoJuridicoImportPreview;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class DlgPrevisualizarEquipoJuridicoExcel extends JDialog {

    private final EquipoJuridicoImportPreview preview;
    private final DefaultTableModel model = new DefaultTableModel();
    private final JTable table = new JTable(model);

    public DlgPrevisualizarEquipoJuridicoExcel(Window parent, EquipoJuridicoImportPreview preview) {
        super(parent, "Previsualización Equipo Jurídico", ModalityType.APPLICATION_MODAL);
        this.preview = preview;
        configurar();
        cargarDatos();
    }

    private void configurar() {
        setLayout(new BorderLayout(0, 12));
        setPreferredSize(new Dimension(1180, 560));

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        root.setBackground(new Color(245, 247, 250));

        JLabel titulo = new JLabel("Previsualización de carga - Equipo Jurídico");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        JLabel resumen = new JLabel(resumen());
        resumen.setForeground(new Color(72, 84, 96));

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(titulo, BorderLayout.NORTH);
        header.add(resumen, BorderLayout.CENTER);

        model.setColumnIdentifiers(new Object[]{
            "ITEM",
            "Abogado",
            "Supervisor",
            "Personal",
            "Estado",
            "Username generado",
            "Validación",
            "Observaciones"
        });

        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        ajustarColumnas();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(218, 224, 231)));

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(btnCerrar);

        root.add(header, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
    }

    private void ajustarColumnas() {
        int[] widths = {70, 240, 240, 150, 90, 150, 115, 520};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void cargarDatos() {
        for (EquipoJuridicoImportItem item : preview.getItems()) {
            model.addRow(new Object[]{
                item.getItem(),
                item.getAbogado(),
                item.getSupervisor(),
                item.getPersonal(),
                item.getEstado(),
                item.getUsernameSugerido(),
                item.getEstadoValidacion(),
                item.getMensajesResumen()
            });
        }
    }

    private String resumen() {
        return "Filas: " + preview.getTotalFilasLeidas()
                + " | Válidas: " + preview.getTotalValidas()
                + " | Advertencias: " + preview.getTotalAdvertencias()
                + " | Errores: " + preview.getTotalErrores();
    }
}
