package com.sdrerc.ui.views.usuario;

import com.sdrerc.domain.model.EquipoJuridicoImportResult;
import com.sdrerc.domain.model.EquipoJuridicoImportRowResult;
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

public class DlgResultadoImportacionEquipoJuridico extends JDialog {

    private final EquipoJuridicoImportResult result;
    private final DefaultTableModel model = new DefaultTableModel();
    private final JTable table = new JTable(model);

    public DlgResultadoImportacionEquipoJuridico(Window parent, EquipoJuridicoImportResult result) {
        super(parent, "Resultado de importación", ModalityType.APPLICATION_MODAL);
        this.result = result;
        configurar();
        cargarDatos();
    }

    private void configurar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1040, 520));

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        root.setBackground(new Color(245, 247, 250));

        JLabel titulo = new JLabel("Resultado de importación - Equipo Jurídico");
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
            "Estado",
            "Resultado",
            "Mensaje"
        });

        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        int[] widths = {70, 230, 230, 105, 150, 420};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

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

    private void cargarDatos() {
        for (EquipoJuridicoImportRowResult row : result.getResultadosPorFila()) {
            model.addRow(new Object[]{
                row.getItem(),
                row.getAbogado(),
                row.getSupervisor(),
                row.getEstado(),
                row.getAccionesResumen(),
                row.getMensaje()
            });
        }
    }

    private String resumen() {
        return "Procesadas: " + result.getTotalFilas()
                + " | Importadas: " + result.getImportadas()
                + " | Omitidas: " + result.getOmitidas()
                + " | Fallidas: " + result.getFallidas()
                + " | Con advertencias: " + result.getConAdvertencias();
    }
}
