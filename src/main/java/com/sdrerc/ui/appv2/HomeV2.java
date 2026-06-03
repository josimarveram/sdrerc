package com.sdrerc.ui.appv2;

import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.CardPanelV2;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.SectionPanelV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class HomeV2 extends JPanel {

    private static final String[] ETAPAS_FLUJO = {
        "REGISTRO",
        "ASIGNACION",
        "ANALISIS",
        "VERIFICACION",
        "FIRMA_EMISION",
        "EJECUCION",
        "NOTIFICACION",
        "PUBLICACION_CONDICIONAL",
        "EXPEDIENTE_DIGITAL",
        "CIERRE_ARCHIVO"
    };

    private static final String[] MODULOS = {
        "Registro / Recepción",
        "Asignación",
        "Análisis",
        "Verificación",
        "Firma / Emisión",
        "Ejecución",
        "Notificación",
        "Publicación",
        "Expediente digital",
        "Cierre / Archivo",
        "Administración"
    };

    private final Runnable abrirBandejaAction;

    public HomeV2() {
        this(null);
    }

    public HomeV2(Runnable abrirBandejaAction) {
        this.abrirBandejaAction = abrirBandejaAction;
        configurarLayout();
    }

    private void configurarLayout() {
        setLayout(new BorderLayout());
        setBackground(AppV2Theme.BACKGROUND);

        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(AppV2Theme.BACKGROUND);
        page.setBorder(AppV2Theme.pageBorder());

        page.add(crearHeader());
        page.add(Box.createVerticalStrut(AppV2Theme.SPACE_XL));
        page.add(crearMetricas());
        page.add(Box.createVerticalStrut(AppV2Theme.SPACE_LARGE));
        page.add(crearAccesosRapidos());
        page.add(Box.createVerticalStrut(AppV2Theme.SPACE_LARGE));
        page.add(crearFlujoOperativo());
        page.add(Box.createVerticalStrut(AppV2Theme.SPACE_LARGE));
        page.add(crearModulos());

        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("SDRERC V2");
        titulo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_HERO));
        titulo.setForeground(AppV2Theme.TEXT_PRIMARY);
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Sistema de Rectificación de Actas - Nueva arquitectura SDRERC_APP");
        subtitulo.setFont(AppV2Theme.fontPlain(15));
        subtitulo.setForeground(AppV2Theme.TEXT_SECONDARY);
        subtitulo.setAlignmentX(LEFT_ALIGNMENT);

        header.add(titulo);
        header.add(Box.createVerticalStrut(AppV2Theme.SPACE_SMALL));
        header.add(subtitulo);
        return header;
    }

    private JPanel crearMetricas() {
        JPanel metrics = new JPanel(new GridLayout(1, 6, AppV2Theme.SPACE, 0));
        metrics.setOpaque(false);
        metrics.add(new MetricCardV2("Expedientes en registro", "-", "Métrica preparada", AppV2Theme.INFO));
        metrics.add(new MetricCardV2("En análisis", "-", "Métrica preparada", AppV2Theme.INDIGO));
        metrics.add(new MetricCardV2("En verificación", "-", "Métrica preparada", AppV2Theme.TEAL));
        metrics.add(new MetricCardV2("En ejecución", "-", "Métrica preparada", AppV2Theme.SUCCESS));
        metrics.add(new MetricCardV2("En notificación", "-", "Métrica preparada", AppV2Theme.WARNING));
        metrics.add(new MetricCardV2("Por vencer", "-", "Métrica preparada", AppV2Theme.ERROR));
        return metrics;
    }

    private JPanel crearAccesosRapidos() {
        SectionPanelV2 section = new SectionPanelV2("Accesos rápidos");
        JPanel cards = new JPanel(new GridLayout(1, 3, AppV2Theme.SPACE, 0));
        cards.setOpaque(false);
        cards.add(crearAccesoBandeja());
        cards.add(new CardPanelV2("Consola de expediente", "Abra un expediente desde la bandeja para revisar detalle, timeline y acciones."));
        cards.add(new CardPanelV2("Migración progresiva", "La app legacy se mantiene intacta mientras V2 crece por módulos."));
        section.setContent(cards);
        return section;
    }

    private JPanel crearAccesoBandeja() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.cardBorder());

        JLabel title = new JLabel("Bandeja de Expedientes");
        title.setFont(AppV2Theme.fontBold(16));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);

        JLabel detail = new JLabel("<html><body style='width:210px'>Consulta expedientes desde vistas SDRERC_APP sin modificar datos.</body></html>");
        detail.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        detail.setForeground(AppV2Theme.TEXT_SECONDARY);

        JButton button = new JButton("Abrir bandeja");
        button.setEnabled(abrirBandejaAction != null);
        if (abrirBandejaAction != null) {
            button.addActionListener(e -> abrirBandejaAction.run());
        }

        panel.add(title, BorderLayout.NORTH);
        panel.add(detail, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearFlujoOperativo() {
        SectionPanelV2 section = new SectionPanelV2("Flujo operativo");
        JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        flow.setOpaque(false);
        for (int i = 0; i < ETAPAS_FLUJO.length; i++) {
            flow.add(BadgeV2.etapa(ETAPAS_FLUJO[i]));
            if (i < ETAPAS_FLUJO.length - 1) {
                JLabel arrow = new JLabel(">");
                arrow.setFont(AppV2Theme.fontBold(14));
                arrow.setForeground(AppV2Theme.TEXT_SECONDARY);
                flow.add(arrow);
            }
        }
        section.setContent(flow);
        return section;
    }

    private JPanel crearModulos() {
        SectionPanelV2 section = new SectionPanelV2("Módulos principales");
        JPanel grid = new JPanel(new GridLayout(0, 2, AppV2Theme.SPACE, AppV2Theme.SPACE));
        grid.setOpaque(false);
        for (String modulo : MODULOS) {
            grid.add(new CardPanelV2(modulo, "Módulo V2 preparado como bandeja filtrada, consola o vista especializada."));
        }
        section.setContent(grid);
        return section;
    }
}
