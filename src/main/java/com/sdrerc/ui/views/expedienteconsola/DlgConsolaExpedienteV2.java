package com.sdrerc.ui.views.expedienteconsola;

import com.sdrerc.application.sdrercapp.ExpedienteDetalleService;
import com.sdrerc.domain.dto.sdrercapp.AccionPermitidaDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteConsolaDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteTimelineDTO;
import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.EmptyStatePanelV2;
import com.sdrerc.ui.appv2.components.SideInfoPanelV2;
import com.sdrerc.ui.appv2.components.StageProgressPanelV2;
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
    private final JLabel lblTitulo = new JLabel("Consola Expediente V2");
    private final JLabel lblSubtitulo = new JLabel("Cargando datos del expediente...");
    private final JPanel panelBadges = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final JPanel panelEtapas = new JPanel(new BorderLayout());
    private final JPanel headerDatos = new JPanel(new GridLayout(1, 5, 10, 0));
    private final JPanel datosGenerales = new JPanel(new BorderLayout(12, 12));
    private final JPanel documentosPanel = new JPanel(new GridLayout(0, 2, 12, 12));
    private final JPanel accionesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final JPanel sideContainer = new JPanel(new BorderLayout());
    private final DefaultTableModel timelineModel = new DefaultTableModel(
            new Object[]{"Fecha", "Acción / Movimiento", "Usuario", "Origen -> Destino", "Comentario / Motivo"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable timelineTable = new JTable(timelineModel);

    public DlgConsolaExpedienteV2(Window owner, Long idExpediente) {
        this(owner, idExpediente, new ExpedienteDetalleService());
    }

    public DlgConsolaExpedienteV2(Window owner, Long idExpediente, ExpedienteDetalleService detalleService) {
        super(owner, "Consola Expediente V2", ModalityType.APPLICATION_MODAL);
        this.idExpediente = idExpediente;
        this.detalleService = detalleService;
        configurarDialogo();
        cargarDatos();
    }

    private void configurarDialogo() {
        setSize(1220, 780);
        setMinimumSize(new java.awt.Dimension(980, 640));
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
        tabs.addTab("Timeline / Historial", crearTimelineTab());
        tabs.addTab("Acciones disponibles", crearAccionesTab());

        sideContainer.setOpaque(false);
        sideContainer.setPreferredSize(new java.awt.Dimension(340, 0));
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
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
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
                return new DetalleCarga(consola, timeline, acciones);
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
        lblSubtitulo.setText("Consultando vistas de solo lectura SDRERC_APP");
        panelBadges.removeAll();
        panelEtapas.removeAll();
        headerDatos.removeAll();
        datosGenerales.removeAll();
        documentosPanel.removeAll();
        accionesPanel.removeAll();
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
        lblSubtitulo.setText("Titular: No disponible · Procedimiento: No disponible");
        cargarHeaderDatos(expediente);
        cargarBadges(expediente);
        cargarEtapas(expediente);
        cargarDatosGenerales(expediente);
        cargarDocumentos(expediente);
        cargarAcciones(carga.acciones);
        cargarTimeline(carga.timeline);
        cargarPanelLateral(expediente, carga.timeline, carga.acciones);
    }

    private void cargarBadges(ExpedienteConsolaDTO expediente) {
        panelBadges.removeAll();
        panelBadges.add(StatusBadgeV2.forEtapa(expediente.getEtapaCodigo()));
        panelBadges.add(StatusBadgeV2.forEstado(expediente.getEstadoCodigo()));
        panelBadges.add(StatusBadgeV2.forDias(expediente.getDiasRestantes()));
        panelBadges.add(expediente.isRequierePublicacion()
                ? new BadgeV2("Requiere publicación", AppV2Theme.SOFT_ORANGE, AppV2Theme.WARNING)
                : new BadgeV2("Sin publicación pendiente", AppV2Theme.SOFT_GRAY, AppV2Theme.TEXT_SECONDARY));
        panelBadges.add(expediente.isExpedienteDigitalCompleto()
                ? new BadgeV2("Digital completo", AppV2Theme.SOFT_GREEN, AppV2Theme.SUCCESS)
                : new BadgeV2("Digital pendiente", AppV2Theme.SOFT_GRAY, AppV2Theme.TEXT_SECONDARY));
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
        headerDatos.add(crearHeaderDato("Fecha registro", formatDateTime(expediente.getFechaRegistro())));
        headerDatos.add(crearHeaderDato("Vencimiento", formatDate(expediente.getFechaVencimiento())));
        headerDatos.add(crearHeaderDato("Días restantes", value(expediente.getDiasRestantes())));
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

    private void cargarDatosGenerales(ExpedienteConsolaDTO expediente) {
        datosGenerales.removeAll();
        JPanel content = new JPanel();
        content.setBackground(AppV2Theme.SURFACE);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(crearSeccionDetalle("Datos del expediente", new String[][]{
            {"ID expediente", value(expediente.getIdExpediente())},
            {"Número expediente", expediente.getNumeroExpediente()},
            {"Etapa actual", DisplayNameMapperV2.etapa(expediente.getEtapaCodigo())},
            {"Estado actual", DisplayNameMapperV2.estado(expediente.getEstadoCodigo())}
        }));
        content.add(Box.createVerticalStrut(10));
        content.add(crearSeccionDetalle("Datos del trámite", new String[][]{
            {"Número trámite", expediente.getNumeroTramiteDocumentario()},
            {"Titular", "No disponible en vista actual"},
            {"Procedimiento", "No disponible en vista actual"},
            {"Número de resolución", "No disponible en vista actual"}
        }));
        content.add(Box.createVerticalStrut(10));
        content.add(crearSeccionDetalle("Responsables", new String[][]{
            {"Abogado inicial", expediente.getAbogadoInicial()},
            {"Responsable actual", expediente.getResponsableActual()},
            {"Equipo actual", expediente.getEquipoActual()}
        }));
        content.add(Box.createVerticalStrut(10));
        content.add(crearSeccionDetalle("Plazos", new String[][]{
            {"Fecha registro", formatDateTime(expediente.getFechaRegistro())},
            {"Último movimiento", formatDateTime(expediente.getFechaUltimoMovimiento())},
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

    private void cargarDocumentos(ExpedienteConsolaDTO expediente) {
        documentosPanel.removeAll();
        documentosPanel.add(crearInfoCard("Documentos", value(expediente.getTotalDocumentos()), "Total en expediente"));
        documentosPanel.add(crearInfoCard("Observaciones", value(expediente.getObservacionesPendientes()), "Pendientes de subsanación"));
        documentosPanel.add(crearInfoCard("Notificaciones", value(expediente.getTotalNotificaciones()), "Registros asociados"));
        documentosPanel.add(crearInfoCard("Cargos de acuse", value(expediente.getTotalCargos()), "Registros asociados"));
        documentosPanel.add(crearInfoCard("Publicación", expediente.isRequierePublicacion() ? "Pendiente" : "Sin pendiente", "Indicador de publicación"));
        documentosPanel.add(crearInfoCard("Expediente digital", expediente.isExpedienteDigitalCompleto() ? "Completo" : "Pendiente", "Completitud digital"));
        documentosPanel.revalidate();
        documentosPanel.repaint();
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

    private void cargarPanelLateral(ExpedienteConsolaDTO expediente, List<ExpedienteTimelineDTO> timeline, List<AccionPermitidaDTO> acciones) {
        sideContainer.removeAll();
        SideInfoPanelV2 side = new SideInfoPanelV2("Resumen del expediente");
        side.addItem("Responsable actual", expediente.getResponsableActual(), expediente.getEquipoActual());
        side.addItem("Última acción", timeline.isEmpty() ? "Sin historial" : DisplayNameMapperV2.accion(timeline.get(0).getMovimiento()), timeline.isEmpty() ? "" : formatDateTime(timeline.get(0).getFechaMovimiento()));
        side.addItem("Documentos", value(expediente.getTotalDocumentos()), "Total asociado al expediente");
        side.addItem("Observaciones", value(expediente.getObservacionesPendientes()), "Pendientes de subsanación");
        side.addItem("Notificación / cargo", expediente.getTotalNotificaciones() + " / " + expediente.getTotalCargos(), "Notificaciones y cargos registrados");
        side.addItem("Publicación", expediente.isRequierePublicacion() ? "Requiere publicación" : "Sin pendiente", "Indicador de publicación");
        side.addItem("Expediente digital", expediente.isExpedienteDigitalCompleto() ? "Completo" : "Pendiente", "Completitud digital");
        side.addItem("Acciones visibles", String.valueOf(acciones.size()), "Solo informativas");
        sideContainer.add(side, BorderLayout.CENTER);
        sideContainer.revalidate();
        sideContainer.repaint();
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

    private static String safe(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }

    private static class DetalleCarga {
        private final ExpedienteConsolaDTO consola;
        private final List<ExpedienteTimelineDTO> timeline;
        private final List<AccionPermitidaDTO> acciones;

        private DetalleCarga(
                ExpedienteConsolaDTO consola,
                List<ExpedienteTimelineDTO> timeline,
                List<AccionPermitidaDTO> acciones) {
            this.consola = consola;
            this.timeline = timeline == null ? new ArrayList<ExpedienteTimelineDTO>() : timeline;
            this.acciones = acciones == null ? new ArrayList<AccionPermitidaDTO>() : acciones;
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
            return c;
        }
    }
}
