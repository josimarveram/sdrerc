package com.sdrerc.ui.appv2;

import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.CardPanelV2;
import com.sdrerc.ui.appv2.components.AppV2ResponsiveGridPanel;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.SectionPanelV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;

public class HomeV2 extends JPanel {

    private static final String[] ETAPAS_FLUJO = {
        "Registro",
        "Asignación",
        "Análisis",
        "Verificación",
        "Firma / Emisión",
        "Ejecución",
        "Notificación",
        "Publicación",
        "Expediente digital",
        "Cierre / Archivo"
    };

    private static final String[][] MODULOS = {
        {"Registro / Recepción", "Ingreso, carga diaria y registro manual de expedientes."},
        {"Asignación", "Distribución controlada de expedientes a equipos y responsables."},
        {"Análisis", "Evaluación jurídica y documental de expedientes asignados."},
        {"Verificación", "Revisión de resultados, documentos y observaciones del análisis."},
        {"Firma / Emisión", "Firma, emisión y numeración del documento resolutivo."},
        {"Ejecución", "Seguimiento del cumplimiento de resoluciones emitidas."},
        {"Notificación", "Gestión de modalidades, cargos y resultados de notificación."},
        {"Publicación", "Control de publicaciones requeridas por notificación no concretada."},
        {"Expediente digital", "Carpeta, enlace y completitud documental del expediente."},
        {"Cierre / Archivo", "Consulta y gestión final de expedientes cerrados o archivados."},
        {"Administración", "Usuarios, roles y organización del equipo jurídico."}
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

        HomeScrollContent page = new HomeScrollContent();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(AppV2Theme.BACKGROUND);
        page.setBorder(AppV2Theme.pageBorder());

        page.add(fullWidth(crearHeader()));
        page.add(Box.createVerticalStrut(AppV2Theme.SPACE_XL));
        page.add(fullWidth(crearMetricas()));
        page.add(Box.createVerticalStrut(AppV2Theme.SPACE_LARGE));
        page.add(fullWidth(crearAccesosRapidos()));
        page.add(Box.createVerticalStrut(AppV2Theme.SPACE_LARGE));
        page.add(fullWidth(crearFlujoOperativo()));
        page.add(Box.createVerticalStrut(AppV2Theme.SPACE_LARGE));
        page.add(fullWidth(crearModulos()));
        page.add(Box.createVerticalStrut(AppV2Theme.SPACE_XL));

        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(AppV2Theme.BACKGROUND);
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

        JTextArea subtitulo = textArea(
                "Sistema de Rectificación de Actas - Nueva arquitectura SDRERC_APP",
                AppV2Theme.fontPlain(15), AppV2Theme.TEXT_SECONDARY);
        subtitulo.setFont(AppV2Theme.fontPlain(15));
        subtitulo.setForeground(AppV2Theme.TEXT_SECONDARY);
        subtitulo.setAlignmentX(LEFT_ALIGNMENT);

        header.add(titulo);
        header.add(Box.createVerticalStrut(AppV2Theme.SPACE_SMALL));
        header.add(subtitulo);
        return header;
    }

    private JPanel crearMetricas() {
        AppV2ResponsiveGridPanel metrics = new AppV2ResponsiveGridPanel(
                190, 6, AppV2Theme.SPACE, AppV2Theme.SPACE);
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
        AppV2ResponsiveGridPanel cards = new AppV2ResponsiveGridPanel(
                280, 3, AppV2Theme.SPACE, AppV2Theme.SPACE);
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

        JTextArea detail = textArea(
                "Consulta expedientes desde vistas SDRERC_APP sin modificar datos.",
                AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE), AppV2Theme.TEXT_SECONDARY);

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
        AppV2ResponsiveGridPanel flow = new AppV2ResponsiveGridPanel(
                150, 5, AppV2Theme.SPACE_SMALL, AppV2Theme.SPACE_SMALL);
        for (String etapa : ETAPAS_FLUJO) {
            BadgeV2 badge = new BadgeV2(etapa, AppV2Theme.SOFT_BLUE, AppV2Theme.PRIMARY);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));
            flow.add(badge);
        }
        section.setContent(flow);
        return section;
    }

    private JPanel crearModulos() {
        SectionPanelV2 section = new SectionPanelV2("Módulos principales");
        AppV2ResponsiveGridPanel grid = new AppV2ResponsiveGridPanel(
                300, 3, AppV2Theme.SPACE, AppV2Theme.SPACE);
        for (String[] modulo : MODULOS) {
            grid.add(new CardPanelV2(modulo[0], modulo[1]));
        }
        section.setContent(grid);
        return section;
    }

    private static JTextArea textArea(String text, java.awt.Font font, Color foreground) {
        JTextArea area = new JTextArea(text == null ? "" : text);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new Insets(0, 0, 0, 0));
        area.setFont(font);
        area.setForeground(foreground);
        return area;
    }

    private static <T extends Component> T fullWidth(T component) {
        if (component instanceof javax.swing.JComponent) {
            javax.swing.JComponent swingComponent = (javax.swing.JComponent) component;
            swingComponent.setAlignmentX(LEFT_ALIGNMENT);
            swingComponent.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                    swingComponent.getMaximumSize().height));
        }
        return component;
    }

    private static final class HomeScrollContent extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(visibleRect.height - 36, 36);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
