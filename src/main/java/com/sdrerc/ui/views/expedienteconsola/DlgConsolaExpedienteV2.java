package com.sdrerc.ui.views.expedienteconsola;

import com.sdrerc.application.sdrercapp.ExpedienteDetalleService;
import com.sdrerc.application.sdrercapp.ExpedienteRelacionadoService;
import com.sdrerc.domain.dto.sdrercapp.AccionPermitidaDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteConsolaDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteTimelineDTO;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.EmptyStatePanelV2;
import com.sdrerc.ui.appv2.components.SideInfoPanelV2;
import com.sdrerc.ui.appv2.components.StageProgressPanelV2;
import com.sdrerc.ui.appv2.components.PlazoVisualSupportV2;
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DlgConsolaExpedienteV2 extends JDialog {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Long idExpediente;
    private final ExpedienteDetalleService detalleService;
    private final ExpedienteRelacionadoService relacionadoService = new ExpedienteRelacionadoService();
    private final JLabel lblTitulo = new JLabel("Consola de expediente");
    private final JLabel lblSubtitulo = new JLabel("Cargando datos del expediente...");
    private final JLabel lblAsociadosEstado = new JLabel("Cargando expedientes asociados...");
    private final JLabel lblAvisoAsociado = new JLabel();
    private final JButton btnAbrirAsociado = new JButton("Abrir expediente asociado");
    private final JButton btnAbrirPrincipal = new JButton("Abrir expediente principal");
    private final JButton btnMaximizar = new JButton("Maximizar");
    private final JPanel panelBadges = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final JPanel panelEtapas = new JPanel(new BorderLayout());
    private final JPanel headerDatos = new JPanel(new GridLayout(1, 5, 10, 0));
    private final JPanel datosGenerales = new JPanel(new BorderLayout(12, 12));
    private final JPanel documentosPanel = new JPanel(new GridLayout(0, 2, 12, 12));
    private final JPanel accionesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final JPanel sideContainer = new JPanel(new BorderLayout());
    private final JPanel avisoAsociadoPanel = new JPanel(new BorderLayout(10, 0));
    private final DefaultTableModel timelineModel = new DefaultTableModel(
            new Object[]{"Fecha", "Acción / Movimiento", "Usuario", "Origen -> Destino", "Comentario / Motivo"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable timelineTable = new AppV2Table(timelineModel);
    private final DefaultTableModel asociadosModel = new DefaultTableModel(
            new Object[]{"ID", "Número expediente", "Tipo relación", "Descripción", "Etapa", "Estado", "Fecha asociación", "Usuario"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable asociadosTable = new AppV2Table(asociadosModel);
    private Long idExpedientePrincipalAsociado;
    private boolean consolaMaximizada;
    private Rectangle boundsAntesDeMaximizar;

    public DlgConsolaExpedienteV2(Window owner, Long idExpediente) {
        this(owner, idExpediente, new ExpedienteDetalleService());
    }

    public DlgConsolaExpedienteV2(Window owner, Long idExpediente, ExpedienteDetalleService detalleService) {
        super(owner, "Consola de expediente", ModalityType.APPLICATION_MODAL);
        this.idExpediente = idExpediente;
        this.detalleService = detalleService;
        configurarDialogo();
        cargarDatos();
    }

    private void configurarDialogo() {
        setSize(1220, 780);
        setMinimumSize(new Dimension(980, 640));
        setResizable(true);
        setLocationRelativeTo(getOwner());

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(AppV2Theme.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setContentPane(root);

        root.add(crearHeader(), BorderLayout.NORTH);
        root.add(crearCentro(), BorderLayout.CENTER);
        root.add(crearFooter(), BorderLayout.SOUTH);
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 12));
        header.setBackground(AppV2Theme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JPanel titleBlock = new JPanel(new BorderLayout(0, 4));
        titleBlock.setOpaque(false);
        lblTitulo.setFont(AppV2Theme.fontBold(24));
        lblTitulo.setForeground(AppV2Theme.TEXT_PRIMARY);
        lblSubtitulo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        lblSubtitulo.setForeground(AppV2Theme.TEXT_SECONDARY);
        titleBlock.add(lblTitulo, BorderLayout.NORTH);
        titleBlock.add(lblSubtitulo, BorderLayout.CENTER);

        panelBadges.setOpaque(false);
        panelEtapas.setOpaque(false);
        headerDatos.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        top.add(titleBlock, BorderLayout.CENTER);
        top.add(panelBadges, BorderLayout.EAST);

        header.add(top, BorderLayout.NORTH);
        header.add(headerDatos, BorderLayout.CENTER);
        header.add(panelEtapas, BorderLayout.SOUTH);
        return header;
    }

    private JSplitPane crearCentro() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        tabs.addTab("Detalles", crearDetallesTab());
        tabs.addTab("Documentos", crearDocumentosTab());
        tabs.addTab("Documentos asociados", crearAsociadosTab());
        tabs.addTab("Timeline / Historial", crearTimelineTab());
        tabs.addTab("Acciones disponibles", crearAccionesTab());

        sideContainer.setOpaque(false);
        sideContainer.setPreferredSize(new Dimension(340, 0));
        sideContainer.add(new EmptyStatePanelV2("Resumen", "Cargando panel lateral..."), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabs, sideContainer);
        split.setResizeWeight(0.72d);
        split.setBorder(null);
        split.setOpaque(false);
        return split;
    }

    private JScrollPane crearDetallesTab() {
        datosGenerales.setBackground(AppV2Theme.SURFACE);
        datosGenerales.setBorder(AppV2Theme.sectionBorder());
        JScrollPane scroll = new JScrollPane(datosGenerales);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private JScrollPane crearDocumentosTab() {
        documentosPanel.setBackground(AppV2Theme.BACKGROUND);
        documentosPanel.setBorder(AppV2Theme.pageBorder());
        JScrollPane scroll = new JScrollPane(documentosPanel);
        scroll.setBorder(null);
        return scroll;
    }

    private JPanel crearAsociadosTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.sectionBorder());

        asociadosTable.setRowHeight(34);
        asociadosTable.setAutoCreateRowSorter(true);
        asociadosTable.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        asociadosTable.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        asociadosTable.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        asociadosTable.setGridColor(AppV2Theme.BORDER);
        asociadosTable.setShowVerticalLines(false);
        asociadosTable.setDefaultRenderer(Object.class, new AsociadosCellRenderer());
        asociadosTable.getColumnModel().getColumn(0).setMaxWidth(70);
        asociadosTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        asociadosTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        AppV2TableColumnSizer.applyFriendlyDefaults(asociadosTable);
        asociadosTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnAbrirAsociado.setEnabled(asociadosTable.getSelectedRow() >= 0);
            }
        });

        btnAbrirAsociado.setEnabled(false);
        btnAbrirAsociado.addActionListener(e -> abrirExpedienteAsociado());
        btnAbrirPrincipal.setEnabled(false);
        btnAbrirPrincipal.addActionListener(e -> abrirExpedientePrincipal());

        lblAsociadosEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblAsociadosEstado.setForeground(AppV2Theme.TEXT_SECONDARY);
        lblAvisoAsociado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblAvisoAsociado.setForeground(AppV2Theme.WARNING);

        avisoAsociadoPanel.setBackground(AppV2Theme.SOFT_ORANGE);
        avisoAsociadoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.WARNING),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        avisoAsociadoPanel.add(lblAvisoAsociado, BorderLayout.CENTER);
        avisoAsociadoPanel.add(btnAbrirPrincipal, BorderLayout.EAST);
        avisoAsociadoPanel.setVisible(false);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(lblAsociadosEstado, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnAbrirAsociado);
        footer.add(acciones, BorderLayout.EAST);

        panel.add(avisoAsociadoPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(asociadosTable), BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane crearTimelineTab() {
        timelineTable.setRowHeight(34);
        timelineTable.setAutoCreateRowSorter(true);
        timelineTable.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        timelineTable.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        timelineTable.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        timelineTable.setGridColor(AppV2Theme.BORDER);
        timelineTable.setShowVerticalLines(false);
        timelineTable.setDefaultRenderer(Object.class, new TimelineCellRenderer());
        timelineTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        timelineTable.getColumnModel().getColumn(1).setPreferredWidth(210);
        timelineTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        timelineTable.getColumnModel().getColumn(3).setPreferredWidth(230);
        timelineTable.getColumnModel().getColumn(4).setPreferredWidth(260);
        AppV2TableColumnSizer.applyFriendlyDefaults(timelineTable);
        JScrollPane scroll = new JScrollPane(timelineTable);
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        return scroll;
    }

    private JScrollPane crearAccionesTab() {
        accionesPanel.setBackground(AppV2Theme.SURFACE);
        accionesPanel.setBorder(AppV2Theme.sectionBorder());
        JScrollPane scroll = new JScrollPane(accionesPanel);
        scroll.setBorder(null);
        return scroll;
    }

    private JPanel crearFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        btnMaximizar.setToolTipText("Maximizar consola a toda la pantalla");
        btnMaximizar.addActionListener(e -> alternarMaximizarConsola());
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        footer.add(btnMaximizar);
        footer.add(btnCerrar);
        return footer;
    }

    private void cargarDatos() {
        setLoadingState();
        SwingWorker<DetalleCarga, Void> worker = new SwingWorker<DetalleCarga, Void>() {
            @Override
            protected DetalleCarga doInBackground() throws Exception {
                ExpedienteConsolaDTO consola = detalleService.obtenerConsolaPorExpediente(idExpediente);
                List<ExpedienteTimelineDTO> timeline = detalleService.listarTimeline(idExpediente);
                List<AccionPermitidaDTO> acciones = detalleService.listarAccionesPermitidas(idExpediente);
                List<ExpedienteRelacionadoDTO> asociados = relacionadoService.listarAsociadosConfirmados(idExpediente);
                ExpedienteRelacionadoDTO principalAsociado = relacionadoService.obtenerExpedientePrincipalAsociado(idExpediente);
                return new DetalleCarga(consola, timeline, acciones, asociados, principalAsociado);
            }

            @Override
            protected void done() {
                try {
                    cargarVista(get());
                } catch (Exception ex) {
                    mostrarError(ex);
                }
            }
        };
        worker.execute();
    }

    private void setLoadingState() {
        lblTitulo.setText("Cargando expediente...");
        lblSubtitulo.setText("Consultando detalle del expediente");
        panelBadges.removeAll();
        panelEtapas.removeAll();
        headerDatos.removeAll();
        datosGenerales.removeAll();
        documentosPanel.removeAll();
        accionesPanel.removeAll();
        asociadosModel.setRowCount(0);
        idExpedientePrincipalAsociado = null;
        avisoAsociadoPanel.setVisible(false);
        btnAbrirPrincipal.setEnabled(false);
        btnAbrirAsociado.setEnabled(false);
        timelineModel.setRowCount(0);
    }

    private void cargarVista(DetalleCarga carga) {
        if (carga.consola == null) {
            lblTitulo.setText("Expediente no encontrado");
            lblSubtitulo.setText("ID expediente: " + idExpediente);
            datosGenerales.removeAll();
            datosGenerales.add(new EmptyStatePanelV2("Expediente no encontrado", "ID expediente: " + idExpediente), BorderLayout.CENTER);
            return;
        }

        ExpedienteConsolaDTO expediente = carga.consola;
        lblTitulo.setText("Expediente " + expediente.getNumeroExpediente());
        lblSubtitulo.setText("Titular: " + safe(expediente.getTitular())
                + " · Procedimiento: " + safe(expediente.getProcedimiento()));
        cargarHeaderDatos(expediente);
        cargarBadges(expediente);
        cargarEtapas(expediente);
        cargarDatosGenerales(expediente);
        cargarDocumentos(expediente, carga.asociados, carga.principalAsociado);
        cargarAsociados(carga.asociados, carga.principalAsociado);
        cargarAcciones(carga.acciones);
        cargarTimeline(carga.timeline);
        cargarPanelLateral(expediente, carga.timeline, carga.acciones, carga.asociados, carga.principalAsociado);
    }

    private void cargarBadges(ExpedienteConsolaDTO expediente) {
        panelBadges.removeAll();
        panelBadges.add(StatusBadgeV2.forEtapa(expediente.getEtapaCodigo()));
        panelBadges.add(StatusBadgeV2.forEstado(expediente.getEstadoCodigo()));
        panelBadges.add(expediente.isRequierePublicacion()
                ? new BadgeV2("Requiere publicación", AppV2Theme.SOFT_ORANGE, AppV2Theme.WARNING)
                : new BadgeV2("Sin publicación pendiente", AppV2Theme.SOFT_GRAY, AppV2Theme.TEXT_SECONDARY));
        panelBadges.add(expediente.isExpedienteDigitalCompleto()
                ? new BadgeV2("Digital completo", AppV2Theme.SOFT_GREEN, AppV2Theme.SUCCESS)
                : new BadgeV2("Digital pendiente", AppV2Theme.SOFT_GRAY, AppV2Theme.TEXT_SECONDARY));
        if (expediente.tieneIndicadorGrupoFamiliar()) {
            panelBadges.add(expediente.isGrupoFamiliar()
                    ? new BadgeV2("Grupo familiar", AppV2Theme.SOFT_BLUE, AppV2Theme.PRIMARY)
                    : new BadgeV2("Posible grupo familiar", AppV2Theme.SOFT_ORANGE, AppV2Theme.WARNING));
        }
        panelBadges.revalidate();
        panelBadges.repaint();
    }

    private void cargarEtapas(ExpedienteConsolaDTO expediente) {
        panelEtapas.removeAll();
        panelEtapas.add(new StageProgressPanelV2(expediente.getEtapaCodigo(), expediente.getEstadoCodigo()), BorderLayout.CENTER);
        panelEtapas.revalidate();
        panelEtapas.repaint();
    }

    private void cargarHeaderDatos(ExpedienteConsolaDTO expediente) {
        headerDatos.removeAll();
        headerDatos.add(crearHeaderDato("Trámite", expediente.getNumeroTramiteDocumentario()));
        headerDatos.add(crearHeaderDato("Responsable", expediente.getResponsableActual()));
        headerDatos.add(crearHeaderDato("Recepción", formatDate(expediente.getFechaRecepcion())));
        headerDatos.add(crearHeaderDato("Vencimiento", formatDate(expediente.getFechaVencimiento())));
        headerDatos.add(crearHeaderDatoDiasRestantes(expediente));
        headerDatos.revalidate();
        headerDatos.repaint();
    }

    private JPanel crearHeaderDato(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout(2, 3));
        panel.setBackground(AppV2Theme.SURFACE_ALT);
        panel.setBorder(AppV2Theme.compactCardBorder());
        panel.setMinimumSize(new java.awt.Dimension(120, 58));
        panel.setPreferredSize(new java.awt.Dimension(140, 58));
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppV2Theme.fontBold(11));
        lbl.setForeground(AppV2Theme.TEXT_SECONDARY);
        JTextArea val = new JTextArea(value == null || value.isEmpty() ? "-" : value);
        val.setEditable(false);
        val.setFocusable(false);
        val.setOpaque(false);
        val.setLineWrap(true);
        val.setWrapStyleWord(true);
        val.setRows(1);
        val.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        val.setForeground(AppV2Theme.TEXT_PRIMARY);
        val.setMargin(new Insets(0, 0, 0, 0));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(val, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearHeaderDatoDiasRestantes(ExpedienteConsolaDTO expediente) {
        Long dias = expediente == null ? null : expediente.getDiasRestantes();
        Color accent = colorPlazo(dias);
        Color background = fondoPlazo(dias);
        JPanel panel = new JPanel(new BorderLayout(6, 2));
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(accent),
                        BorderFactory.createEmptyBorder(7, 10, 7, 10))));
        panel.setMinimumSize(new Dimension(120, 58));
        panel.setPreferredSize(new Dimension(150, 58));

        JLabel lbl = new JLabel("Plazo de vencimiento");
        lbl.setFont(AppV2Theme.fontBold(11));
        lbl.setForeground(AppV2Theme.TEXT_SECONDARY);

        JLabel val = new JLabel(dias == null ? "-" : String.valueOf(dias));
        val.setFont(AppV2Theme.fontBold(28));
        val.setForeground(accent);
        val.setToolTipText(descripcionPlazo(dias, expediente == null ? null : expediente.getFechaVencimiento()));

        JLabel caption = new JLabel(descripcionCortaPlazo(dias));
        caption.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        caption.setForeground(accent);

        JPanel center = new JPanel(new BorderLayout(8, 0));
        center.setOpaque(false);
        center.add(val, BorderLayout.WEST);
        center.add(caption, BorderLayout.CENTER);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void cargarDatosGenerales(ExpedienteConsolaDTO expediente) {
        datosGenerales.removeAll();
        JPanel content = new JPanel();
        content.setBackground(AppV2Theme.SURFACE);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(crearSeccionDetalle("Datos del expediente", new String[][]{
            {"ID expediente", value(expediente.getIdExpediente())},
            {"Número expediente", expediente.getNumeroExpediente()},
            {"Número trámite", expediente.getNumeroTramiteDocumentario()},
            {"Etapa actual", DisplayNameMapperV2.etapa(expediente.getEtapaCodigo())},
            {"Estado actual", DisplayNameMapperV2.estado(expediente.getEstadoCodigo())},
            {"Fecha registro", formatDateTime(expediente.getFechaRegistro())},
            {"Último movimiento", formatDateTime(expediente.getFechaUltimoMovimiento())}
        }));
        content.add(Box.createVerticalStrut(10));
        content.add(crearSeccionDetalle("Datos del trámite", new String[][]{
            {"Procedimiento", expediente.getProcedimiento()},
            {"Canal de recepción", expediente.getCanalRecepcion()},
            {"Fecha recepción", formatDate(expediente.getFechaRecepcion())},
            {"Tipo de solicitud", expediente.getTipoSolicitud()},
            {"N° expediente SGD", expediente.getNumeroExpedienteSgd()},
            {"Tipo documento", expediente.getTipoDocumento()},
            {"Número documento", expediente.getNumeroDocumento()}
        }));
        content.add(Box.createVerticalStrut(10));
        content.add(crearSeccionDetalle("Personas", new String[][]{
            {"Titular", expediente.getTitular()},
            {"Documento titular", expediente.getTitularDocumento()},
            {"Remitente", expediente.getRemitente()},
            {"Documento remitente", expediente.getRemitenteDocumento()}
        }));
        content.add(Box.createVerticalStrut(10));
        content.add(crearSeccionDetalle("Acta", new String[][]{
            {"Tipo acta", expediente.getTipoActa()},
            {"Número acta", expediente.getNumeroActa()}
        }));
        content.add(Box.createVerticalStrut(10));
        if (expediente.tieneIndicadorGrupoFamiliar()) {
            content.add(crearSeccionDetalle("Grupo familiar", new String[][]{
                {"Estado", expediente.getGrupoFamiliarEstado()},
                {"Criterio", criterioGrupoFamiliar(expediente.getCriterioGrupoFamiliar())},
                {"Observación", expediente.getObservacionGrupoFamiliar()}
            }));
            content.add(Box.createVerticalStrut(10));
        }
        content.add(crearSeccionDetalle("Responsables", new String[][]{
            {"Abogado inicial", expediente.getAbogadoInicial()},
            {"Responsable actual", expediente.getResponsableActual()},
            {"Equipo actual", expediente.getEquipoActual()}
        }));
        content.add(Box.createVerticalStrut(10));
        content.add(crearSeccionDetalle("Resolución / documento", new String[][]{
            {"Tipo resolución", expediente.getTipoResolucion()},
            {"Número resolución", expediente.getNumeroResolucion()},
            {"Fecha resolución", formatDate(expediente.getFechaResolucion())},
            {"Fecha firma", formatDateTime(expediente.getFechaFirma())}
        }));
        content.add(Box.createVerticalStrut(10));
        content.add(crearSeccionDetalle("Notificación y publicación", new String[][]{
            {"Tipo notificación", expediente.getTipoNotificacion()},
            {"Estado notificación", expediente.getEstadoNotificacion()},
            {"Resultado notificación", expediente.getResultadoNotificacion()},
            {"Cargo de acuse", expediente.getEstadoCargoAcuse()},
            {"Estado publicación", DisplayNameMapperV2.valor(expediente.getEstadoPublicacion())},
            {"Medio publicación", expediente.getMedioPublicacion()},
            {"Número publicación", expediente.getNumeroPublicacion()},
            {"Fecha publicación", formatDate(expediente.getFechaPublicacion())}
        }));
        content.add(Box.createVerticalStrut(10));
        content.add(crearSeccionDetalle("Expediente digital", new String[][]{
            {"Ruta / carpeta", expediente.getRutaCarpetaDigital()},
            {"Enlace digital", expediente.getEnlaceCarpetaDigital()},
            {"Completitud", expediente.isExpedienteDigitalCompleto() ? "Completo" : "Pendiente"}
        }));
        content.add(Box.createVerticalStrut(10));
        content.add(crearSeccionDetalle("Plazos", new String[][]{
            {"Fecha vencimiento", formatDate(expediente.getFechaVencimiento())},
            {"Días restantes", value(expediente.getDiasRestantes())}
        }));
        content.add(Box.createVerticalStrut(10));
        content.add(crearSeccionDetalle("Indicadores", new String[][]{
            {"Documentos", value(expediente.getTotalDocumentos())},
            {"Observaciones pendientes", value(expediente.getObservacionesPendientes())},
            {"Notificaciones", value(expediente.getTotalNotificaciones())},
            {"Cargos de acuse", value(expediente.getTotalCargos())},
            {"Publicación", expediente.isRequierePublicacion() ? "Requiere publicación" : "Sin pendiente"},
            {"Expediente digital", expediente.isExpedienteDigitalCompleto() ? "Completo" : "Pendiente"}
        }));
        datosGenerales.add(content, BorderLayout.NORTH);
        datosGenerales.revalidate();
        datosGenerales.repaint();
    }

    private JPanel crearSeccionDetalle(String title, String[][] rows) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setBackground(AppV2Theme.SURFACE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 0, 0, 0)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        for (int i = 0; i < rows.length; i++) {
            agregarDato(grid, rows[i][0], rows[i][1], i);
        }

        wrapper.add(lblTitle, BorderLayout.NORTH);
        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private void cargarDocumentos(
            ExpedienteConsolaDTO expediente,
            List<ExpedienteRelacionadoDTO> asociados,
            ExpedienteRelacionadoDTO principalAsociado) {
        documentosPanel.removeAll();
        documentosPanel.add(crearInfoCard("Documentos", value(expediente.getTotalDocumentos()), "Total en expediente"));
        documentosPanel.add(crearInfoCard(
                "Documentos duplicados",
                value(asociados == null ? 0 : asociados.size()),
                principalAsociado == null
                        ? "Asociados al expediente principal"
                        : "Este registro pertenece al expediente principal"));
        documentosPanel.add(crearInfoCard("Observaciones", value(expediente.getObservacionesPendientes()), "Pendientes de subsanación"));
        documentosPanel.add(crearInfoCard("Notificaciones", value(expediente.getTotalNotificaciones()), "Registros asociados"));
        documentosPanel.add(crearInfoCard("Cargos de acuse", value(expediente.getTotalCargos()), "Registros asociados"));
        documentosPanel.add(crearInfoCard(
                "Publicación",
                expediente.isRequierePublicacion() ? "Prevista" : "Sin pendiente",
                expediente.getFechaPublicacion() == null
                        ? "Sin fecha registrada"
                        : "Fecha: " + formatDate(expediente.getFechaPublicacion())));
        documentosPanel.add(crearInfoCard("Expediente digital", expediente.isExpedienteDigitalCompleto() ? "Completo" : "Pendiente", "Completitud digital"));
        documentosPanel.revalidate();
        documentosPanel.repaint();
    }

    private void cargarAsociados(List<ExpedienteRelacionadoDTO> asociados, ExpedienteRelacionadoDTO principalAsociado) {
        idExpedientePrincipalAsociado = principalAsociado == null ? null : principalAsociado.getIdExpediente();
        if (principalAsociado == null) {
            avisoAsociadoPanel.setVisible(false);
            btnAbrirPrincipal.setEnabled(false);
        } else {
            lblAvisoAsociado.setText("Este registro/documento está asociado al expediente principal "
                    + safe(principalAsociado.getNumeroExpediente()) + ".");
            avisoAsociadoPanel.setVisible(true);
            btnAbrirPrincipal.setEnabled(idExpedientePrincipalAsociado != null);
        }
        asociadosModel.setRowCount(0);
        for (ExpedienteRelacionadoDTO item : asociados) {
            asociadosModel.addRow(new Object[]{
                item.getIdExpediente(),
                item.getNumeroExpediente(),
                DisplayNameMapperV2.accion(item.getTipoRelacion()),
                item.getDescripcionRelacion(),
                DisplayNameMapperV2.etapa(item.getEtapaCodigo()),
                DisplayNameMapperV2.estado(item.getEstadoCodigo()),
                formatDateTime(item.getFechaRelacion()),
                item.getUsuarioRelacion()
            });
        }
        if (asociados.isEmpty()) {
            lblAsociadosEstado.setText(principalAsociado == null
                    ? "No existen documentos duplicados asociados para este expediente."
                    : "Este registro está asociado a un expediente principal. No tiene otros asociados confirmados.");
        } else {
            lblAsociadosEstado.setText(asociados.size() + " documento(s) duplicado(s) asociado(s) confirmados.");
        }
        btnAbrirAsociado.setEnabled(false);
        avisoAsociadoPanel.revalidate();
        avisoAsociadoPanel.repaint();
    }

    private void abrirExpedienteAsociado() {
        int selectedRow = asociadosTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un documento o expediente asociado para abrirlo.",
                    "Documentos asociados",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = asociadosTable.convertRowIndexToModel(selectedRow);
        Object value = asociadosModel.getValueAt(modelRow, 0);
        if (!(value instanceof Number)) {
            return;
        }
        Long idAsociado = ((Number) value).longValue();
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(getOwner(), idAsociado);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void abrirExpedientePrincipal() {
        if (idExpedientePrincipalAsociado == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se encontró expediente principal asociado.",
                    "Documento asociado",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(getOwner(), idExpedientePrincipalAsociado);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel crearInfoCard(String title, String value, String caption) {
        JPanel card = new JPanel(new BorderLayout(4, 6));
        card.setBackground(AppV2Theme.SURFACE);
        card.setBorder(AppV2Theme.cardBorder());

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblTitle.setForeground(AppV2Theme.TEXT_SECONDARY);
        JLabel lblValue = new JLabel(value == null || value.isEmpty() ? "-" : value);
        lblValue.setFont(AppV2Theme.fontBold(22));
        lblValue.setForeground(AppV2Theme.TEXT_PRIMARY);
        JLabel lblCaption = new JLabel(caption);
        lblCaption.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblCaption.setForeground(AppV2Theme.MUTED);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblCaption, BorderLayout.SOUTH);
        return card;
    }

    private void agregarDato(JPanel target, String label, String value, int row) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = row;
        gbcLabel.weightx = 0;
        gbcLabel.anchor = GridBagConstraints.NORTHWEST;
        gbcLabel.insets = new Insets(6, 0, 6, 18);

        GridBagConstraints gbcValue = new GridBagConstraints();
        gbcValue.gridx = 1;
        gbcValue.gridy = row;
        gbcValue.weightx = 1;
        gbcValue.fill = GridBagConstraints.HORIZONTAL;
        gbcValue.anchor = GridBagConstraints.NORTHWEST;
        gbcValue.insets = new Insets(6, 0, 6, 0);

        JLabel lbl = new JLabel(label);
        lbl.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        lbl.setForeground(AppV2Theme.TEXT_SECONDARY);

        JLabel val = new JLabel(value == null || value.isEmpty() ? "-" : value);
        val.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        val.setForeground(AppV2Theme.TEXT_PRIMARY);
        val.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppV2Theme.BORDER));
        val.setToolTipText(value == null || value.isEmpty() ? null : value);

        target.add(lbl, gbcLabel);
        target.add(val, gbcValue);
    }

    private void cargarAcciones(List<AccionPermitidaDTO> acciones) {
        accionesPanel.removeAll();
        if (acciones.isEmpty()) {
            accionesPanel.add(new EmptyStatePanelV2("Sin acciones permitidas", "No hay acciones visibles para la etapa y estado actual."));
        } else {
            for (AccionPermitidaDTO accion : acciones) {
                accionesPanel.add(crearAccionInfo(accion));
            }
        }
        accionesPanel.revalidate();
        accionesPanel.repaint();
    }

    private JPanel crearAccionInfo(AccionPermitidaDTO accion) {
        JPanel card = new JPanel(new BorderLayout(6, 4));
        card.setBackground(AppV2Theme.SURFACE_ALT);
        card.setBorder(AppV2Theme.compactCardBorder());

        JButton btn = new JButton(accion.getNombreAccion().isEmpty()
                ? DisplayNameMapperV2.accion(accion.getCodigoAccion())
                : accion.getNombreAccion());
        btn.setEnabled(false);
        btn.setToolTipText("Pendiente de implementación");

        String requisitos = (accion.isRequiereComentario() ? "Comentario" : "Sin comentario")
                + " · "
                + (accion.isRequiereDocumento() ? "Documento" : "Sin documento");
        JLabel lbl = new JLabel(requisitos);
        lbl.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lbl.setForeground(AppV2Theme.TEXT_SECONDARY);

        card.add(btn, BorderLayout.NORTH);
        card.add(lbl, BorderLayout.CENTER);
        return card;
    }

    private void cargarTimeline(List<ExpedienteTimelineDTO> timeline) {
        timelineModel.setRowCount(0);
        for (ExpedienteTimelineDTO item : timeline) {
            timelineModel.addRow(new Object[]{
                formatDateTime(item.getFechaMovimiento()),
                DisplayNameMapperV2.accion(item.getMovimiento()),
                usuarioTimeline(item),
                join(item.getEtapaOrigen(), item.getEstadoOrigen()) + " -> " + join(item.getEtapaDestino(), item.getEstadoDestino()),
                comentarioTimeline(item)
            });
        }
    }

    private void cargarPanelLateral(
            ExpedienteConsolaDTO expediente,
            List<ExpedienteTimelineDTO> timeline,
            List<AccionPermitidaDTO> acciones,
            List<ExpedienteRelacionadoDTO> asociados,
            ExpedienteRelacionadoDTO principalAsociado) {
        sideContainer.removeAll();
        SideInfoPanelV2 side = new SideInfoPanelV2("Resumen del expediente");
        side.addItem("Responsable actual", expediente.getResponsableActual(), expediente.getEquipoActual());
        side.addItem("Última acción", timeline.isEmpty() ? "Sin historial" : DisplayNameMapperV2.accion(timeline.get(0).getMovimiento()), timeline.isEmpty() ? "" : formatDateTime(timeline.get(0).getFechaMovimiento()));
        side.addItem("Documentos", value(expediente.getTotalDocumentos()), "Total asociado al expediente");
        side.addItem("Documentos duplicados", value(asociados == null ? 0 : asociados.size()),
                principalAsociado == null ? "Asociados confirmados" : "Asociado a principal");
        side.addItem("Observaciones", value(expediente.getObservacionesPendientes()), "Pendientes de subsanación");
        side.addItem("Notificación / cargo", expediente.getTotalNotificaciones() + " / " + expediente.getTotalCargos(), "Notificaciones y cargos registrados");
        side.addItem(
                "Publicación",
                expediente.isRequierePublicacion() ? "Requiere publicación" : "Sin pendiente",
                expediente.getFechaPublicacion() == null
                        ? "Sin fecha registrada"
                        : "Fecha: " + formatDate(expediente.getFechaPublicacion()));
        side.addItem("Expediente digital", expediente.isExpedienteDigitalCompleto() ? "Completo" : "Pendiente", "Completitud digital");
        side.addItem("Acciones visibles", String.valueOf(acciones.size()), "Solo informativas");
        sideContainer.add(side, BorderLayout.CENTER);
        sideContainer.revalidate();
        sideContainer.repaint();
    }

    private void alternarMaximizarConsola() {
        if (!consolaMaximizada) {
            boundsAntesDeMaximizar = getBounds();
            Rectangle bounds = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getMaximumWindowBounds();
            setBounds(bounds);
            consolaMaximizada = true;
            btnMaximizar.setText("Restaurar");
            btnMaximizar.setToolTipText("Restaurar tamaño de consola");
            return;
        }
        if (boundsAntesDeMaximizar != null) {
            setBounds(boundsAntesDeMaximizar);
        } else {
            setSize(1220, 780);
            setLocationRelativeTo(getOwner());
        }
        consolaMaximizada = false;
        btnMaximizar.setText("Maximizar");
        btnMaximizar.setToolTipText("Maximizar consola a toda la pantalla");
    }

    private void mostrarError(Exception ex) {
        String message = ex.getMessage();
        if (message == null && ex.getCause() != null) {
            message = ex.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(
                this,
                message == null ? "No se pudo cargar la consola del expediente." : message,
                "Error de consulta",
                JOptionPane.ERROR_MESSAGE);
        lblTitulo.setText("Error al cargar expediente");
        lblSubtitulo.setText("Revise conexión SDRERC_APP y permisos de lectura.");
    }

    private static String join(String etapa, String estado) {
        if ((etapa == null || etapa.isEmpty()) && (estado == null || estado.isEmpty())) {
            return "";
        }
        return DisplayNameMapperV2.etapa(etapa) + " / " + DisplayNameMapperV2.estado(estado);
    }

    private static String usuarioTimeline(ExpedienteTimelineDTO item) {
        if (item.getUsuarioOrigen() != null && !item.getUsuarioOrigen().isEmpty()) {
            return item.getUsuarioOrigen();
        }
        return item.getUsuarioDestino();
    }

    private static String comentarioTimeline(ExpedienteTimelineDTO item) {
        String comentario = item.getComentario();
        String motivo = item.getMotivo();
        if ((comentario == null || comentario.isEmpty()) && (motivo == null || motivo.isEmpty())) {
            return "";
        }
        if (comentario == null || comentario.isEmpty()) {
            return motivo;
        }
        if (motivo == null || motivo.isEmpty()) {
            return comentario;
        }
        return comentario + " / " + motivo;
    }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME_FORMAT.format(value);
    }

    private static String formatDate(LocalDate value) {
        return value == null ? "" : DATE_FORMAT.format(value);
    }

    private static String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String criterioGrupoFamiliar(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            return "";
        }
        String normalized = criterio.trim().toUpperCase(Locale.ROOT);
        if ("MANUAL".equals(normalized)) {
            return "Marcado manual";
        }
        if ("EXCEL".equals(normalized)) {
            return "Informado desde Excel";
        }
        if ("COINCIDENCIA_APELLIDOS_EXCEL".equals(normalized)) {
            return "Coincidencia de apellidos en carga";
        }
        if ("COINCIDENCIA_APELLIDOS_BD".equals(normalized)) {
            return "Coincidencia de apellidos con solicitud existente";
        }
        return DisplayNameMapperV2.valor(criterio);
    }

    private static String safe(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }

    private static Color colorPlazo(Long dias) {
        return PlazoVisualSupportV2.foregroundFor(PlazoVisualSupportV2.clasificarDias(dias));
    }

    private static Color fondoPlazo(Long dias) {
        return PlazoVisualSupportV2.backgroundFor(PlazoVisualSupportV2.clasificarDias(dias));
    }

    private static String descripcionCortaPlazo(Long dias) {
        if (dias == null) {
            return "sin plazo";
        }
        if (dias < 0) {
            return "vencido";
        }
        if (dias == 0) {
            return "vence hoy";
        }
        return "hábil(es)";
    }

    private static String descripcionPlazo(Long dias, LocalDate fechaVencimiento) {
        String fecha = fechaVencimiento == null ? "sin fecha configurada" : DATE_FORMAT.format(fechaVencimiento);
        if (dias == null) {
            return "Sin plazo de vencimiento (" + fecha + ")";
        }
        if (dias < 0) {
            return "Vencido hace " + Math.abs(dias) + " día(s) hábil(es). Vencimiento: " + fecha;
        }
        if (dias == 0) {
            return "Vence hoy. Vencimiento: " + fecha;
        }
        return dias + " día(s) hábil(es) restantes. Vencimiento: " + fecha;
    }

    private static class DetalleCarga {
        private final ExpedienteConsolaDTO consola;
        private final List<ExpedienteTimelineDTO> timeline;
        private final List<AccionPermitidaDTO> acciones;
        private final List<ExpedienteRelacionadoDTO> asociados;
        private final ExpedienteRelacionadoDTO principalAsociado;

        private DetalleCarga(
                ExpedienteConsolaDTO consola,
                List<ExpedienteTimelineDTO> timeline,
                List<AccionPermitidaDTO> acciones,
                List<ExpedienteRelacionadoDTO> asociados,
                ExpedienteRelacionadoDTO principalAsociado) {
            this.consola = consola;
            this.timeline = timeline == null ? new ArrayList<ExpedienteTimelineDTO>() : timeline;
            this.acciones = acciones == null ? new ArrayList<AccionPermitidaDTO>() : acciones;
            this.asociados = asociados == null ? new ArrayList<ExpedienteRelacionadoDTO>() : asociados;
            this.principalAsociado = principalAsociado;
        }
    }

    private static class AsociadosCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            int modelColumn = table.convertColumnIndexToModel(column);
            if (!isSelected && modelColumn == 4) {
                Component badge = StatusBadgeV2.forEtapa(value == null ? "" : value.toString());
                if (badge instanceof JLabel) {
                    ((JLabel) badge).setToolTipText(value == null ? null : value.toString());
                }
                return badge;
            }
            if (!isSelected && modelColumn == 5) {
                Component badge = StatusBadgeV2.forEstado(value == null ? "" : value.toString());
                if (badge instanceof JLabel) {
                    ((JLabel) badge).setToolTipText(value == null ? null : value.toString());
                }
                return badge;
            }
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
                c.setForeground(modelColumn == 1 ? AppV2Theme.PRIMARY : AppV2Theme.TEXT_PRIMARY);
            }
            setToolTipText(value == null ? null : value.toString());
            return c;
        }
    }

    private static class TimelineCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setFont(column == 1 ? AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE) : AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
                c.setForeground(column == 1 ? AppV2Theme.PRIMARY : AppV2Theme.TEXT_PRIMARY);
            }
            setToolTipText(value == null ? null : value.toString());
            return c;
        }
    }
}
