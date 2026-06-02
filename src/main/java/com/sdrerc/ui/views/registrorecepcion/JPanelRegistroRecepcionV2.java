package com.sdrerc.ui.views.registrorecepcion;

import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.EmptyStatePanelV2;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class JPanelRegistroRecepcionV2 extends JPanel {

    public JPanelRegistroRecepcionV2() {
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());
        add(crearHeader(), BorderLayout.NORTH);
        add(crearTabs(), BorderLayout.CENTER);
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 6));
        header.setOpaque(false);

        JLabel title = new JLabel("Registro / Recepción");
        title.setFont(AppV2Theme.fontBold(24));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Bandeja, carga diaria y registro manual preparados para SDRERC V2 sin escritura en BD.");
        subtitle.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        subtitle.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel text = new JPanel(new BorderLayout(0, 4));
        text.setOpaque(false);
        text.add(title, BorderLayout.NORTH);
        text.add(subtitle, BorderLayout.CENTER);

        header.add(text, BorderLayout.CENTER);
        header.add(new BadgeV2("Lectura / preparación", AppV2Theme.SOFT_GREEN, AppV2Theme.SUCCESS), BorderLayout.EAST);
        return header;
    }

    private JTabbedPane crearTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        tabs.addTab("Bandeja Registro", crearBandejaRegistro());
        tabs.addTab("Carga diaria", new JPanelCargaDiariaRecepcionV2());
        tabs.addTab("Registro manual", new JPanelRegistroManualPlaceholderV2());
        return tabs;
    }

    private JPanel crearBandejaRegistro() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(AppV2Theme.BACKGROUND);

        JPanel metricas = new JPanel(new GridLayout(1, 4, 12, 0));
        metricas.setOpaque(false);
        metricas.add(new MetricCardV2("En registro", "0", "Pendiente de consulta específica", AppV2Theme.INFO));
        metricas.add(new MetricCardV2("Recepcionados", "0", "Preparado para SDRERC_APP", AppV2Theme.TEAL));
        metricas.add(new MetricCardV2("Observados", "0", "Validación futura", AppV2Theme.WARNING));
        metricas.add(new MetricCardV2("Duplicados", "0", "Conservados para revisión", AppV2Theme.INDIGO));

        JPanel tablaPanel = new JPanel(new BorderLayout(8, 8));
        tablaPanel.setBackground(AppV2Theme.SURFACE);
        tablaPanel.setBorder(AppV2Theme.sectionBorder());

        JLabel title = new JLabel("Bandeja de recepción preparada");
        title.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);

        JTable table = new JTable(new DefaultTableModel(
                new Object[]{
                    "Número expediente",
                    "Número trámite",
                    "Tipo procedimiento",
                    "Titular",
                    "Fecha recepción",
                    "Responsable",
                    "Estado"
                },
                0));
        table.setRowHeight(32);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        table.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        table.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        table.setShowVerticalLines(false);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));

        tablaPanel.add(title, BorderLayout.NORTH);
        tablaPanel.add(scroll, BorderLayout.CENTER);
        tablaPanel.add(new EmptyStatePanelV2(
                "Sin consulta ejecutada",
                "La bandeja específica de Registro / Recepción queda preparada para conectarse a vistas de lectura en un incremento posterior."),
                BorderLayout.SOUTH);

        panel.add(metricas, BorderLayout.NORTH);
        panel.add(tablaPanel, BorderLayout.CENTER);
        return panel;
    }
}
