package com.sdrerc.ui.appv2;

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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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

    private static final String[] ETAPAS_FLUJO_DETALLE = {
        "Ingreso y control inicial",
        "Responsable definido",
        "Evaluación jurídica",
        "Control documental",
        "Documento resolutivo",
        "Cumplimiento operativo",
        "Comunicación formal",
        "Ruta condicional",
        "Carpeta y trazabilidad",
        "Estado final"
    };

    private static final Color[] FLUJO_ACCENTOS = {
        AppV2Theme.INFO,
        AppV2Theme.TEAL,
        AppV2Theme.INDIGO,
        AppV2Theme.SUCCESS,
        new Color(116, 78, 145),
        AppV2Theme.WARNING,
        new Color(178, 72, 86),
        new Color(150, 91, 33),
        new Color(54, 104, 126),
        new Color(88, 98, 110)
    };

    private static final Color[] FLUJO_FONDOS = {
        new Color(232, 243, 252),
        new Color(229, 244, 244),
        new Color(237, 239, 249),
        new Color(230, 245, 236),
        new Color(243, 237, 248),
        new Color(255, 244, 226),
        new Color(252, 237, 240),
        new Color(250, 240, 229),
        new Color(232, 242, 246),
        new Color(241, 243, 246)
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

        hero.add(copy, BorderLayout.CENTER);
        hero.add(crearLogoReniecPanel(), BorderLayout.EAST);
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
        JPanel board = baseCard();
        board.setLayout(new BorderLayout(0, 0));
        board.setBackground(new Color(252, 253, 255));
        board.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 219, 230)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        AppV2WrapPanel flow = new AppV2WrapPanel(8, 12);
        for (int i = 0; i < ETAPAS_FLUJO.length; i++) {
            flow.add(crearEtapaFlujoItem(i));
        }
        board.add(flow, BorderLayout.CENTER);

        return crearSeccion(
                "Flujo operativo",
                "Ruta de atención del expediente desde el registro hasta el cierre",
                board);
    }

    private JPanel crearEtapaFlujoItem(int index) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        item.setOpaque(false);
        item.add(crearEtapaFlujoCard(index));
        if (index < ETAPAS_FLUJO.length - 1) {
            JLabel arrow = new JLabel("→");
            arrow.setFont(AppV2Theme.fontBold(18));
            arrow.setForeground(new Color(74, 129, 143));
            arrow.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));
            item.add(arrow);
        }
        return item;
    }

    private JPanel crearEtapaFlujoCard(int index) {
        Color accent = FLUJO_ACCENTOS[index % FLUJO_ACCENTOS.length];
        Color background = FLUJO_FONDOS[index % FLUJO_FONDOS.length];

        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setOpaque(true);
        card.setBackground(background);
        card.setPreferredSize(new Dimension(220, 74));
        card.setMinimumSize(new Dimension(198, 70));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(
                        Math.max(0, accent.getRed() - 8),
                        Math.max(0, accent.getGreen() - 8),
                        Math.max(0, accent.getBlue() - 8))),
                BorderFactory.createEmptyBorder(10, 10, 10, 12)));

        StageNumberBadge number = new StageNumberBadge(String.format("%02d", index + 1), accent);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(ETAPAS_FLUJO[index]);
        title.setFont(AppV2Theme.fontBold(13));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel detail = new JLabel(ETAPAS_FLUJO_DETALLE[index]);
        detail.setFont(AppV2Theme.fontPlain(11));
        detail.setForeground(AppV2Theme.TEXT_SECONDARY);
        detail.setAlignmentX(LEFT_ALIGNMENT);

        text.add(Box.createVerticalGlue());
        text.add(title);
        text.add(Box.createVerticalStrut(3));
        text.add(detail);
        text.add(Box.createVerticalGlue());

        card.add(number, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
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

    private JPanel crearLogoReniecPanel() {
        JPanel logoCard = new JPanel(new BorderLayout());
        logoCard.setOpaque(true);
        logoCard.setBackground(Color.WHITE);
        logoCard.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        logoCard.setPreferredSize(new Dimension(214, 101));
        logoCard.setMinimumSize(new Dimension(190, 90));

        JLabel logo = new JLabel(cargarLogoReniec());
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setVerticalAlignment(SwingConstants.CENTER);
        logo.setToolTipText("Registro Nacional de Identificación y Estado Civil");
        logoCard.add(logo, BorderLayout.CENTER);
        return logoCard;
    }

    private static ImageIcon cargarLogoReniec() {
        URL url = HomeV2.class.getResource("/com/sdrerc/ui/imagenes/LogoRENIEC.png");
        if (url == null) {
            url = HomeV2.class.getResource("/com/sdrerc/ui/imagenes/logo.png");
        }
        if (url == null) {
            return null;
        }
        try {
            BufferedImage source = ImageIO.read(url);
            if (source == null) {
                return new ImageIcon(url);
            }
            int maxWidth = 214;
            int maxHeight = 101;
            double scale = Math.min((double) maxWidth / source.getWidth(),
                    (double) maxHeight / source.getHeight());
            int targetWidth = Math.max(1, (int) Math.round(source.getWidth() * scale));
            int targetHeight = Math.max(1, (int) Math.round(source.getHeight() * scale));
            Image scaled = source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception ex) {
            return new ImageIcon(url);
        }
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

    private static final class StageNumberBadge extends JLabel {

        private final Color background;

        private StageNumberBadge(String text, Color background) {
            super(text);
            this.background = background;
            setOpaque(false);
            setFont(AppV2Theme.fontBold(11));
            setForeground(Color.WHITE);
            setHorizontalAlignment(SwingConstants.CENTER);
            setPreferredSize(new Dimension(36, 36));
            setMinimumSize(new Dimension(36, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(background);
                int size = Math.min(getWidth(), getHeight()) - 1;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                g2.fillOval(x, y, size, size);

                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics metrics = g2.getFontMetrics();
                String value = getText();
                int textX = (getWidth() - metrics.stringWidth(value)) / 2;
                int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                g2.drawString(value, textX, textY);
            } finally {
                g2.dispose();
            }
        }
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
