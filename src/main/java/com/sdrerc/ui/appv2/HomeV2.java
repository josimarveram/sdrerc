package com.sdrerc.ui.appv2;

import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.AppV2IconProvider;
import com.sdrerc.ui.appv2.components.AppV2ResponsiveGridPanel;
import com.sdrerc.ui.appv2.components.AppV2WrapPanel;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
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
        {"Registro / Recepción", "Ingreso, carga diaria y registro manual de expedientes.", AppV2IconProvider.REGISTRO},
        {"Asignación", "Distribución controlada de expedientes a equipos y responsables.", AppV2IconProvider.ASIGNACION},
        {"Análisis", "Evaluación jurídica y documental de expedientes asignados.", AppV2IconProvider.ANALISIS},
        {"Verificación", "Revisión de resultados, documentos y observaciones del análisis.", AppV2IconProvider.VERIFICACION},
        {"Firma / Emisión", "Firma, emisión y numeración del documento resolutivo.", AppV2IconProvider.FIRMA_EMISION},
        {"Ejecución", "Seguimiento del cumplimiento de resoluciones emitidas.", AppV2IconProvider.EJECUCION},
        {"Notificación", "Gestión de modalidades, cargos y resultados de comunicación.", AppV2IconProvider.NOTIFICACION},
        {"Publicación", "Control de publicaciones requeridas por notificación no concretada.", AppV2IconProvider.PUBLICACION},
        {"Expediente digital", "Carpeta, enlace y completitud documental del expediente.", AppV2IconProvider.EXPEDIENTE_DIGITAL},
        {"Cierre / Archivo", "Gestión final de expedientes cerrados o archivados.", AppV2IconProvider.CIERRE_ARCHIVO},
        {"Administración", "Usuarios, roles y organización del equipo jurídico.", AppV2IconProvider.USUARIOS}
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

        page.add(fullWidth(crearHero()));
        page.add(Box.createVerticalStrut(AppV2Theme.SPACE_XL));
        page.add(fullWidth(crearSeccion(
                "Resumen operativo",
                "Estado general de la carga de expedientes",
                crearMetricas())));
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

    private JPanel crearHero() {
        JPanel hero = new JPanel(new BorderLayout(24, 0));
        hero.setBackground(AppV2Theme.PRIMARY_DARK);
        hero.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, AppV2Theme.TEAL),
                BorderFactory.createEmptyBorder(24, 26, 24, 26)));

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("PANEL EJECUTIVO");
        eyebrow.setFont(AppV2Theme.fontBold(11));
        eyebrow.setForeground(new Color(164, 218, 221));
        eyebrow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titulo = new JLabel("Sistema de Rectificación de Actas");
        titulo.setFont(AppV2Theme.fontBold(26));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JTextArea subtitulo = textArea(
                "Gestión integral, seguimiento y trazabilidad de expedientes registrales.",
                AppV2Theme.fontPlain(15), new Color(214, 228, 239));
        subtitulo.setAlignmentX(LEFT_ALIGNMENT);

        copy.add(eyebrow);
        copy.add(Box.createVerticalStrut(7));
        copy.add(titulo);
        copy.add(Box.createVerticalStrut(AppV2Theme.SPACE_SMALL));
        copy.add(subtitulo);

        JPanel estado = new JPanel();
        estado.setOpaque(false);
        estado.setLayout(new BoxLayout(estado, BoxLayout.Y_AXIS));
        estado.add(heroBadge("Operación oficial"));
        estado.add(Box.createVerticalStrut(8));
        estado.add(heroBadge("Flujo integral"));

        hero.add(copy, BorderLayout.CENTER);
        hero.add(estado, BorderLayout.EAST);
        return hero;
    }

    private JPanel crearMetricas() {
        AppV2ResponsiveGridPanel metrics = new AppV2ResponsiveGridPanel(
                190, 6, AppV2Theme.SPACE, AppV2Theme.SPACE);
        metrics.add(new MetricCardV2("Expedientes en registro", "-", "Pendientes de atención", AppV2Theme.INFO));
        metrics.add(new MetricCardV2("En análisis", "-", "Carga jurídica", AppV2Theme.INDIGO));
        metrics.add(new MetricCardV2("En verificación", "-", "Control documental", AppV2Theme.TEAL));
        metrics.add(new MetricCardV2("En ejecución", "-", "Cumplimiento operativo", AppV2Theme.SUCCESS));
        metrics.add(new MetricCardV2("En notificación", "-", "Comunicación pendiente", AppV2Theme.WARNING));
        metrics.add(new MetricCardV2("Por vencer", "-", "Atención prioritaria", AppV2Theme.ERROR));
        return metrics;
    }

    private JPanel crearAccesosRapidos() {
        AppV2ResponsiveGridPanel cards = new AppV2ResponsiveGridPanel(
                280, 3, AppV2Theme.SPACE, AppV2Theme.SPACE);
        cards.add(crearAccesoCard(
                "Bandeja de Expedientes",
                "Consulta y prioriza expedientes por etapa, estado, responsable y plazo.",
                AppV2IconProvider.BANDEJA,
                "Abrir bandeja",
                abrirBandejaAction));
        cards.add(crearAccesoCard(
                "Consola de expediente",
                "Revisa datos, documentos, historial y acciones desde una vista unificada.",
                AppV2IconProvider.ANALISIS,
                "Disponible desde la bandeja",
                null));
        cards.add(crearAccesoCard(
                "Seguimiento integral",
                "Ubica rápidamente la etapa actual, el responsable y la siguiente acción.",
                AppV2IconProvider.VERIFICACION,
                "Trazabilidad completa",
                null));
        return crearSeccion(
                "Accesos rápidos",
                "Puntos de entrada para la gestión diaria",
                cards);
    }

    private JPanel crearFlujoOperativo() {
        AppV2WrapPanel flow = new AppV2WrapPanel(10, 10);
        for (int i = 0; i < ETAPAS_FLUJO.length; i++) {
            String etapa = ETAPAS_FLUJO[i];
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            item.setOpaque(false);
            BadgeV2 badge = new BadgeV2(etapa, AppV2Theme.SOFT_BLUE, AppV2Theme.PRIMARY);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(199, 218, 235)),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)));
            item.add(badge);
            if (i < ETAPAS_FLUJO.length - 1) {
                JLabel arrow = new JLabel("→");
                arrow.setFont(AppV2Theme.fontBold(16));
                arrow.setForeground(AppV2Theme.TEAL);
                item.add(arrow);
            }
            flow.add(item);
        }
        return crearSeccion(
                "Flujo operativo",
                "Ruta de atención del expediente desde el registro hasta el cierre",
                flow);
    }

    private JPanel crearModulos() {
        AppV2ResponsiveGridPanel grid = new AppV2ResponsiveGridPanel(
                300, 3, AppV2Theme.SPACE, AppV2Theme.SPACE);
        for (String[] modulo : MODULOS) {
            grid.add(crearModuloCard(modulo[0], modulo[1], modulo[2]));
        }
        return crearSeccion(
                "Módulos principales",
                "Áreas operativas y administrativas del sistema",
                grid);
    }

    private JPanel crearSeccion(String title, String subtitle, Component content) {
        JPanel section = new JPanel(new BorderLayout(0, 12));
        section.setOpaque(false);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppV2Theme.fontBold(18));
        lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblSubtitle.setForeground(AppV2Theme.TEXT_SECONDARY);
        lblSubtitle.setAlignmentX(LEFT_ALIGNMENT);

        header.add(lblTitle);
        header.add(Box.createVerticalStrut(3));
        header.add(lblSubtitle);

        section.add(header, BorderLayout.NORTH);
        section.add(content, BorderLayout.CENTER);
        return section;
    }

    private JPanel crearAccesoCard(String title, String detail, String iconCode,
            String footerText, Runnable action) {
        JPanel card = baseCard();
        card.setLayout(new BorderLayout(0, 14));

        JPanel header = crearCardHeader(title, iconCode);
        JTextArea description = textArea(detail,
                AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE), AppV2Theme.TEXT_SECONDARY);

        if (action != null) {
            JButton button = new JButton(footerText);
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setBackground(AppV2Theme.PRIMARY);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
            button.addActionListener(e -> action.run());
            card.add(button, BorderLayout.SOUTH);
        } else {
            JLabel footer = new JLabel(footerText);
            footer.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
            footer.setForeground(AppV2Theme.TEAL);
            card.add(footer, BorderLayout.SOUTH);
        }

        card.add(header, BorderLayout.NORTH);
        card.add(description, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearModuloCard(String title, String detail, String iconCode) {
        JPanel card = baseCard();
        card.setLayout(new BorderLayout(0, 12));
        card.add(crearCardHeader(title, iconCode), BorderLayout.NORTH);
        card.add(textArea(detail, AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE),
                AppV2Theme.TEXT_SECONDARY), BorderLayout.CENTER);

        JLabel status = new JLabel("Disponible");
        status.setFont(AppV2Theme.fontBold(11));
        status.setForeground(AppV2Theme.SUCCESS);
        card.add(status, BorderLayout.SOUTH);
        return card;
    }

    private JPanel crearCardHeader(String title, String iconCode) {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel icon = new JLabel(AppV2IconProvider.menuCollapsed(iconCode));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setOpaque(true);
        icon.setBackground(AppV2Theme.PRIMARY_DARK);
        icon.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        icon.setPreferredSize(new Dimension(38, 38));

        JLabel label = new JLabel(title);
        label.setFont(AppV2Theme.fontBold(16));
        label.setForeground(AppV2Theme.TEXT_PRIMARY);

        header.add(icon, BorderLayout.WEST);
        header.add(label, BorderLayout.CENTER);
        return header;
    }

    private JPanel baseCard() {
        JPanel card = new JPanel();
        card.setBackground(AppV2Theme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return card;
    }

    private JLabel heroBadge(String text) {
        JLabel badge = new JLabel(text);
        badge.setOpaque(true);
        badge.setBackground(Color.WHITE);
        badge.setForeground(AppV2Theme.PRIMARY_DARK);
        badge.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        badge.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        badge.setAlignmentX(RIGHT_ALIGNMENT);
        return badge;
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
