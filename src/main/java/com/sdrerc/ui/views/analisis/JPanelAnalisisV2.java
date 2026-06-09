package com.sdrerc.ui.views.analisis;

import com.sdrerc.application.sdrercapp.AnalisisExpedienteService;
import com.sdrerc.application.sdrercapp.DocumentoAnalisisService;
import com.sdrerc.domain.dto.sdrercapp.AnalisisExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.AnalisisRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.AnalisisResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.domain.dto.sdrercapp.ObservacionAnalisisDTO;
import com.sdrerc.ui.appv2.components.AppV2ActionPanel;
import com.sdrerc.ui.appv2.components.AppV2FilterPanel;
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.components.AppV2TablePanel;
import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import com.sdrerc.ui.views.expedienteconsola.DlgConsolaExpedienteV2;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class JPanelAnalisisV2 extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AnalisisExpedienteService analisisService;
    private final DocumentoAnalisisService documentoService;

    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite, titular, acta o responsable", 28);
    private final JComboBox<SimpleItem> cmbEstadoFiltro = new JComboBox<SimpleItem>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnVerDetalle = new JButton("Ver detalle");
    private final JButton btnRecibir = new JButton("Recibir expediente");
    private final JButton btnRegistrarAnalisis = new JButton("Registrar análisis");
    private final JButton btnEnviarVerificacion = new JButton("Enviar a verificación");
    private final JButton btnDerivarNotificacion = new JButton("Derivar a notificación");
    private final JButton btnArchivarNoCorresponde = new JButton("Archivar no corresponde");
    private final JButton btnDerivarExterna = new JButton("Derivación externa");
    private final JButton btnAgregarDocumento = new JButton("Agregar documento");
    private final JButton btnQuitarDocumento = new JButton("Quitar documento");

    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar expedientes de análisis.");
    private final JLabel lblExpediente = new JLabel("-");
    private final JLabel lblTitular = new JLabel("-");
    private final JLabel lblActa = new JLabel("-");
    private final JLabel lblProcedimiento = new JLabel("-");
    private final JLabel lblResponsable = new JLabel("-");
    private final JLabel lblEtapaEstado = new JLabel("-");
    private final JLabel lblAlertas = new JLabel("Sin alertas.");
    private final JLabel lblFechaAnalisis = new JLabel(DATE_FORMAT.format(LocalDate.now()));

    private final JComboBox<ResultadoItem> cmbResultado = new JComboBox<ResultadoItem>();
    private final JComboBox<SimpleItem> cmbIncorporado = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbTipoDocumento = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbEstadoDocumento = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbTipoObservacion = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbMotivoNoCorresponde = new JComboBox<SimpleItem>();
    private final JCheckBox chkReconstitucion = new JCheckBox("Reconstitución");
    private final JCheckBox chkLegitimidad = new JCheckBox("Legitimidad");
    private final JCheckBox chkMediosProbatorios = new JCheckBox("Medios probatorios");
    private final JCheckBox chkRegistrarObservacion = new JCheckBox("Registrar observación");
    private final JTextArea txtFundamento = new JTextArea(4, 22);
    private final JTextArea txtDescripcionDocumento = new JTextArea(2, 20);
    private final JTextArea txtObservacion = new JTextArea(3, 22);
    private final JTextArea txtComentarioMovimiento = new JTextArea(3, 22);

    private final AnalisisTableModel tableModel = new AnalisisTableModel();
    private final JTable table = new AppV2Table(tableModel);
    private final AppV2TablePanel tablePanel = new AppV2TablePanel(
            table,
            "Sin expedientes para mostrar",
            "Seleccione filtros y presione Buscar.");
    private final DefaultTableModel documentoModel = new DefaultTableModel(
            new Object[]{"Tipo", "Estado", "Fecha", "Descripción", "tipo_codigo", "estado_codigo"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable documentosTable = new AppV2Table(documentoModel);
    private final List<AnalisisExpedienteDTO> expedientes = new ArrayList<AnalisisExpedienteDTO>();
    private final MetricCardV2 cardPorRecibir = new MetricCardV2("Por recibir", "0", "Asignación / Asignado", AppV2Theme.INFO);
    private final MetricCardV2 cardEnAnalisis = new MetricCardV2("En análisis", "0", "Recibidos y observados", AppV2Theme.TEAL);
    private final MetricCardV2 cardEspeciales = new MetricCardV2("Rutas especiales", "0", "No corresponde / notificación", AppV2Theme.WARNING);

    private boolean cargandoCatalogos;

    public JPanelAnalisisV2() {
        this(new AnalisisExpedienteService(), new DocumentoAnalisisService());
    }

    public JPanelAnalisisV2(AnalisisExpedienteService analisisService, DocumentoAnalisisService documentoService) {
        this.analisisService = analisisService;
        this.documentoService = documentoService;
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());
        add(crearHeader(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        configurarTabla();
        configurarDocumentoTabla();
        configurarEventos();
        cargarFiltrosBase();
        cargarCatalogos();
        actualizarSeleccion();
    }

    private JPanel crearHeader() {
        JPanel metricas = new JPanel(new GridLayout(1, 3, 12, 0));
        metricas.setOpaque(false);
        metricas.add(cardPorRecibir);
        metricas.add(cardEnAnalisis);
        metricas.add(cardEspeciales);
        return metricas;
    }

    private JPanel crearCentro() {
        JPanel centro = new JPanel(new BorderLayout(14, 14));
        centro.setOpaque(false);
        centro.add(crearBandeja(), BorderLayout.CENTER);
        centro.add(crearPanelAnalisis(), BorderLayout.EAST);
        return centro;
    }

    private JPanel crearBandeja() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JPanel filtros = new AppV2FilterPanel();
        configurarControles();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;
        gbc.gridx = 0;
        filtros.add(label("Búsqueda"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtros.add(txtBusqueda, gbc);
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        JPanel accionesFiltro = AppV2ActionPanel.right();
        accionesFiltro.add(btnBuscar);
        accionesFiltro.add(btnLimpiar);
        accionesFiltro.add(btnVerDetalle);
        accionesFiltro.add(btnRefrescar);
        gbc.gridx = 4;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtros.add(accionesFiltro, gbc);
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridy = 1;
        gbc.gridx = 0;
        filtros.add(label("Estado"), gbc);
        gbc.gridx = 1;
        filtros.add(cmbEstadoFiltro, gbc);
        gbc.gridx = 2;
        filtros.add(label("Mostrar"), gbc);
        gbc.gridx = 3;
        filtros.add(spnLimite, gbc);

        JPanel acciones = AppV2ActionPanel.left();
        acciones.add(btnRecibir);
        acciones.add(btnRegistrarAnalisis);
        acciones.add(btnEnviarVerificacion);
        acciones.add(btnDerivarNotificacion);
        acciones.add(btnArchivarNoCorresponde);
        acciones.add(btnDerivarExterna);

        JPanel superior = new JPanel(new BorderLayout(8, 8));
        superior.setOpaque(false);
        superior.add(filtros, BorderLayout.NORTH);
        superior.add(acciones, BorderLayout.CENTER);
        superior.add(lblEstado, BorderLayout.SOUTH);
        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);

        panel.add(superior, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelAnalisis() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(420, 0));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.sectionBorder());

        JLabel title = new JLabel("Panel de análisis");
        title.setFont(AppV2Theme.fontBold(18));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.add(crearResumenSeleccion());
        content.add(crearFormularioAnalisis());
        content.add(crearDocumentosPanel());
        content.add(crearObservacionPanel());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(crearComentarioMovimientoPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearResumenSeleccion() {
        JPanel panel = section("Expediente seleccionado");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Expediente", lblExpediente);
        addRow(grid, row++, "Titular", lblTitular);
        addRow(grid, row++, "Acta", lblActa);
        addRow(grid, row++, "Procedimiento", lblProcedimiento);
        addRow(grid, row++, "Responsable", lblResponsable);
        addRow(grid, row++, "Etapa / Estado", lblEtapaEstado);
        addRow(grid, row, "Alertas", lblAlertas);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormularioAnalisis() {
        JPanel panel = section("Resultado del análisis");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Resultado", cmbResultado);
        addRow(grid, row++, "¿Acta incorporada?", cmbIncorporado);
        addRow(grid, row++, "Motivo no corresponde", cmbMotivoNoCorresponde);

        JPanel checks = new JPanel(new GridLayout(0, 1, 4, 4));
        checks.setOpaque(false);
        checks.add(chkReconstitucion);
        checks.add(chkLegitimidad);
        checks.add(chkMediosProbatorios);
        addRow(grid, row++, "Evaluaciones", checks);
        addRow(grid, row++, "Fecha análisis", lblFechaAnalisis);
        addRow(grid, row, "Sustento", scrollText(txtFundamento, 96));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDocumentosPanel() {
        JPanel panel = section("Documentos analizados");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        int row = 0;
        addRow(form, row++, "Tipo", cmbTipoDocumento);
        addRow(form, row++, "Estado", cmbEstadoDocumento);
        addRow(form, row++, "Descripción", scrollText(txtDescripcionDocumento, 58));
        JPanel acciones = AppV2ActionPanel.right();
        acciones.add(btnAgregarDocumento);
        acciones.add(btnQuitarDocumento);
        addRow(form, row, "", acciones);

        JScrollPane scroll = new JScrollPane(documentosTable);
        scroll.setPreferredSize(new Dimension(360, 140));
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));

        panel.add(form, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearObservacionPanel() {
        JPanel panel = section("Observación");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "", chkRegistrarObservacion);
        addRow(grid, row++, "Tipo", cmbTipoObservacion);
        addRow(grid, row, "Descripción", scrollText(txtObservacion, 78));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearComentarioMovimientoPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setOpaque(false);
        JLabel lbl = label("Comentario de movimiento");
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(scrollText(txtComentarioMovimiento, 70), BorderLayout.CENTER);
        return panel;
    }

    private JPanel section(String title) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(12, 0, 12, 0)));
        JLabel label = new JLabel(title);
        label.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        label.setForeground(AppV2Theme.TEXT_PRIMARY);
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private JScrollPane scrollText(JTextArea area, int height) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        area.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(260, height));
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        return scroll;
    }

    private void addRow(JPanel target, int row, String label, Component component) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.NORTHWEST;
        gbcLabel.insets = new Insets(5, 0, 5, 10);

        GridBagConstraints gbcValue = new GridBagConstraints();
        gbcValue.gridx = 1;
        gbcValue.gridy = row;
        gbcValue.weightx = 1;
        gbcValue.fill = GridBagConstraints.HORIZONTAL;
        gbcValue.insets = new Insets(5, 0, 5, 0);
        if (label == null || label.isEmpty()) {
            target.add(new JLabel(""), gbcLabel);
        } else {
            target.add(label(label), gbcLabel);
        }
        target.add(component, gbcValue);
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        label.setForeground(AppV2Theme.TEXT_SECONDARY);
        return label;
    }

    private void configurarControles() {
        txtBusqueda.setPreferredSize(new Dimension(420, 34));
        txtBusqueda.setMinimumSize(new Dimension(320, 34));
        cmbEstadoFiltro.setPreferredSize(new Dimension(250, 34));
        spnLimite.setPreferredSize(new Dimension(86, 34));
        cmbResultado.setPreferredSize(new Dimension(260, 34));
        cmbIncorporado.setPreferredSize(new Dimension(260, 34));
        cmbMotivoNoCorresponde.setPreferredSize(new Dimension(260, 34));
        cmbTipoDocumento.setPreferredSize(new Dimension(260, 34));
        cmbEstadoDocumento.setPreferredSize(new Dimension(260, 34));
        cmbTipoObservacion.setPreferredSize(new Dimension(260, 34));
        btnBuscar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRecibir.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarAnalisis.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnEnviarVerificacion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnDerivarNotificacion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnArchivarNoCorresponde.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnDerivarExterna.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        chkReconstitucion.setOpaque(false);
        chkLegitimidad.setOpaque(false);
        chkMediosProbatorios.setOpaque(false);
        chkRegistrarObservacion.setOpaque(false);
    }

    private void configurarTabla() {
        table.setRowHeight(34);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        table.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        table.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        table.setGridColor(AppV2Theme.BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setDefaultRenderer(Object.class, new AnalisisRenderer());
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(9).setPreferredWidth(150);
        table.getColumnModel().getColumn(10).setPreferredWidth(150);
        table.getColumnModel().getColumn(11).setMaxWidth(92);
        AppV2TableColumnSizer.applyFriendlyDefaults(table);
    }

    private void configurarDocumentoTabla() {
        documentosTable.setRowHeight(30);
        documentosTable.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        documentosTable.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        documentosTable.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        documentosTable.setGridColor(AppV2Theme.BORDER);
        documentosTable.setShowVerticalLines(false);
        documentosTable.getColumnModel().getColumn(4).setMinWidth(0);
        documentosTable.getColumnModel().getColumn(4).setMaxWidth(0);
        documentosTable.getColumnModel().getColumn(5).setMinWidth(0);
        documentosTable.getColumnModel().getColumn(5).setMaxWidth(0);
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnRefrescar.addActionListener(e -> buscar());
        btnVerDetalle.addActionListener(e -> abrirDetalle());
        btnRecibir.addActionListener(e -> recibir());
        btnRegistrarAnalisis.addActionListener(e -> registrarAnalisis());
        btnEnviarVerificacion.addActionListener(e -> enviarVerificacion());
        btnDerivarNotificacion.addActionListener(e -> derivarNotificacion());
        btnArchivarNoCorresponde.addActionListener(e -> archivarNoCorresponde());
        btnDerivarExterna.addActionListener(e -> diagnosticarDerivacionExterna());
        btnAgregarDocumento.addActionListener(e -> agregarDocumento());
        btnQuitarDocumento.addActionListener(e -> quitarDocumento());
        cmbIncorporado.addActionListener(e -> actualizarChecksIncorporado());
        cmbResultado.addActionListener(e -> actualizarResultadoSeleccionado());
        chkRegistrarObservacion.addActionListener(e -> actualizarObservacionHabilitada());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarSeleccion();
            }
        });
    }

    private void cargarFiltrosBase() {
        cmbEstadoFiltro.removeAllItems();
        cmbEstadoFiltro.addItem(new SimpleItem("TODOS", "Todos"));
        cmbEstadoFiltro.addItem(new SimpleItem("ASIGNADO", "Asignado"));
        cmbEstadoFiltro.addItem(new SimpleItem("RECIBIDO_POR_ABOGADO", "Recibido por abogado"));
        cmbEstadoFiltro.addItem(new SimpleItem("ATENDIDO", "Atendido"));
        cmbEstadoFiltro.addItem(new SimpleItem("OBSERVADO", "Observado"));
        cmbEstadoFiltro.addItem(new SimpleItem("SUBSANADO", "Subsanado"));
        cmbEstadoFiltro.addItem(new SimpleItem("NO_CORRESPONDE", "No corresponde"));
        cmbEstadoFiltro.addItem(new SimpleItem("EN_ABANDONO", "En abandono"));
        cmbEstadoFiltro.addItem(new SimpleItem("OBSERVACION_ADMINISTRATIVA", "Observación administrativa"));
        cmbIncorporado.removeAllItems();
        cmbIncorporado.addItem(new SimpleItem("", "Seleccione"));
        cmbIncorporado.addItem(new SimpleItem("SI", "Sí"));
        cmbIncorporado.addItem(new SimpleItem("NO", "No"));
    }

    private void cargarCatalogos() {
        cargandoCatalogos = true;
        setTrabajando(true, "Cargando catálogos de análisis...");
        SwingWorker<CatalogosCarga, Void> worker = new SwingWorker<CatalogosCarga, Void>() {
            @Override
            protected CatalogosCarga doInBackground() throws Exception {
                return new CatalogosCarga(
                        analisisService.listarResultadosAnalisis(),
                        documentoService.listarTiposDocumentoAnalizado(),
                        documentoService.listarEstadosDocumento(),
                        analisisService.listarTiposObservacion(),
                        analisisService.listarMotivosNoCorresponde());
            }

            @Override
            protected void done() {
                try {
                    cargarCatalogosVista(get());
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los catálogos de análisis.", ex);
                } finally {
                    cargandoCatalogos = false;
                    setTrabajando(false, null);
                    actualizarResultadoSeleccionado();
                    actualizarObservacionHabilitada();
                }
            }
        };
        worker.execute();
    }

    private void cargarCatalogosVista(CatalogosCarga carga) {
        cmbResultado.removeAllItems();
        cmbResultado.addItem(ResultadoItem.placeholder("Seleccione resultado"));
        for (CatalogoItemDTO item : carga.resultados) {
            cmbResultado.addItem(new ResultadoItem(item));
        }

        cargarSimpleItems(cmbTipoDocumento, carga.tiposDocumento, "Seleccione tipo");
        cargarSimpleItems(cmbEstadoDocumento, carga.estadosDocumento, "Seleccione estado");
        cargarSimpleItems(cmbTipoObservacion, carga.tiposObservacion, "Seleccione tipo");
        cargarSimpleItems(cmbMotivoNoCorresponde, carga.motivosNoCorresponde, "Seleccione motivo");
    }

    private void cargarSimpleItems(JComboBox<SimpleItem> combo, List<CatalogoItemDTO> items, String placeholder) {
        combo.removeAllItems();
        combo.addItem(new SimpleItem("", placeholder));
        for (CatalogoItemDTO item : items) {
            combo.addItem(new SimpleItem(item.getCodigo(), item.getNombre()));
        }
    }

    private void buscar() {
        setTrabajando(true, "Consultando expedientes asignados para análisis...");
        String texto = txtBusqueda.getText();
        String estado = obtenerCodigo(cmbEstadoFiltro);
        int limite = ((Number) spnLimite.getValue()).intValue();
        SwingWorker<List<AnalisisExpedienteDTO>, Void> worker = new SwingWorker<List<AnalisisExpedienteDTO>, Void>() {
            @Override
            protected List<AnalisisExpedienteDTO> doInBackground() throws Exception {
                return analisisService.buscarExpedientes(texto, estado, limite);
            }

            @Override
            protected void done() {
                try {
                    cargarTabla(get());
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar la bandeja de análisis.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void cargarTabla(List<AnalisisExpedienteDTO> items) {
        expedientes.clear();
        expedientes.addAll(items);
        tableModel.setRowCount(0);
        int porRecibir = 0;
        int enAnalisis = 0;
        int especiales = 0;
        for (AnalisisExpedienteDTO item : expedientes) {
            if (item.isRecibible()) {
                porRecibir++;
            }
            if (item.isRegistrable() || item.isEnviableVerificacion()) {
                enAnalisis++;
            }
            if (item.isDerivableNotificacionEspecial() || item.isArchivableNoCorresponde()) {
                especiales++;
            }
            tableModel.addRow(new Object[]{
                item.getIdExpediente(),
                item.getNumeroExpediente(),
                item.getNumeroTramiteDocumentario(),
                item.getProcedimiento(),
                item.getTipoDocumento(),
                item.getTipoActa(),
                item.getNumeroActa(),
                item.getTitular(),
                formatDate(item.getFechaRecepcion()),
                item.getResponsable(),
                DisplayNameMapperV2.etapa(item.getEtapaCodigo()),
                DisplayNameMapperV2.estado(item.getEstadoCodigo()),
                item.getDiasEnEtapa() == null ? "" : item.getDiasEnEtapa(),
                item.isTieneObservacionPendiente() ? "Con observación" : "Sin observación",
                item.getTotalRelacionados() > 0 ? item.getTotalRelacionados() + " asociados" : "Sin asociados"
            });
        }
        cardPorRecibir.setValue(String.valueOf(porRecibir));
        cardEnAnalisis.setValue(String.valueOf(enAnalisis));
        cardEspeciales.setValue(String.valueOf(especiales));
        lblEstado.setText(items.isEmpty()
                ? "No se encontraron expedientes para análisis."
                : items.size() + " expediente(s) encontrados.");
        tablePanel.setEmpty(items.isEmpty());
        actualizarSeleccion();
    }

    private void limpiar() {
        txtBusqueda.setText("");
        cmbEstadoFiltro.setSelectedIndex(0);
        spnLimite.setValue(200);
        expedientes.clear();
        tableModel.setRowCount(0);
        tablePanel.setEmpty(true);
        limpiarFormulario();
        cardPorRecibir.setValue("0");
        cardEnAnalisis.setValue("0");
        cardEspeciales.setValue("0");
        lblEstado.setText("Filtros limpiados. Presione Buscar para consultar expedientes de análisis.");
        actualizarSeleccion();
    }

    private void actualizarSeleccion() {
        AnalisisExpedienteDTO item = obtenerSeleccionado();
        boolean has = item != null;
        btnVerDetalle.setEnabled(has);
        btnRecibir.setEnabled(has && item.isRecibible());
        btnRegistrarAnalisis.setEnabled(has && item.isRegistrable());
        btnEnviarVerificacion.setEnabled(has && item.isEnviableVerificacion());
        btnDerivarNotificacion.setEnabled(has && item.isDerivableNotificacionEspecial());
        btnArchivarNoCorresponde.setEnabled(has && item.isArchivableNoCorresponde());
        btnDerivarExterna.setEnabled(has && item.isArchivableNoCorresponde());
        if (!has) {
            lblExpediente.setText("-");
            lblTitular.setText("-");
            lblActa.setText("-");
            lblProcedimiento.setText("-");
            lblResponsable.setText("-");
            lblEtapaEstado.setText("-");
            lblAlertas.setText("Sin alertas.");
            return;
        }
        lblExpediente.setText(item.getNumeroExpediente());
        lblTitular.setText(item.getTitular());
        lblActa.setText((item.getTipoActa() + " " + item.getNumeroActa()).trim());
        lblProcedimiento.setText(item.getProcedimiento());
        lblResponsable.setText(item.getResponsable().isEmpty() ? "-" : item.getResponsable());
        lblEtapaEstado.setText(DisplayNameMapperV2.etapa(item.getEtapaCodigo()) + " / " + DisplayNameMapperV2.estado(item.getEstadoCodigo()));
        lblAlertas.setText(alertas(item));
        txtComentarioMovimiento.setText("");
        if (item.isRegistrable() && documentoModel.getRowCount() == 0 && cmbEstadoDocumento.getItemCount() > 1) {
            cmbEstadoDocumento.setSelectedIndex(1);
        }
    }

    private String alertas(AnalisisExpedienteDTO item) {
        List<String> alertas = new ArrayList<String>();
        if (item.isTieneObservacionPendiente()) {
            alertas.add("Observación pendiente");
        }
        if (item.getTotalRelacionados() > 0) {
            alertas.add(item.getTotalRelacionados() + " expediente(s) asociado(s)");
        }
        if (item.getTotalDocumentosAnalizados() > 0) {
            alertas.add(item.getTotalDocumentosAnalizados() + " documento(s) analizado(s)");
        }
        return alertas.isEmpty() ? "Sin alertas." : String.join(" · ", alertas);
    }

    private void recibir() {
        AnalisisExpedienteDTO item = requerirSeleccion("Seleccione un expediente para recibir.");
        if (item == null) {
            return;
        }
        confirmarYEjecutar(
                "Recibir expediente",
                "Se recibirá el expediente " + item.getNumeroExpediente() + " para análisis. ¿Desea continuar?",
                () -> analisisService.recibirExpediente(item.getIdExpediente(), txtComentarioMovimiento.getText()));
    }

    private void registrarAnalisis() {
        AnalisisExpedienteDTO item = requerirSeleccion("Seleccione un expediente para registrar el análisis.");
        if (item == null) {
            return;
        }
        AnalisisRegistroDTO registro = construirRegistro(item);
        confirmarYEjecutar(
                "Registrar análisis",
                "Se registrará el análisis del expediente " + item.getNumeroExpediente() + ". ¿Desea continuar?",
                () -> analisisService.registrarAnalisis(registro));
    }

    private void enviarVerificacion() {
        AnalisisExpedienteDTO item = requerirSeleccion("Seleccione un expediente para enviar a verificación.");
        if (item == null) {
            return;
        }
        confirmarYEjecutar(
                "Enviar a verificación",
                "Se enviará el expediente " + item.getNumeroExpediente() + " a Verificación. ¿Desea continuar?",
                () -> analisisService.enviarVerificacion(item.getIdExpediente(), txtComentarioMovimiento.getText()));
    }

    private void derivarNotificacion() {
        AnalisisExpedienteDTO item = requerirSeleccion("Seleccione un expediente para derivar a notificación.");
        if (item == null) {
            return;
        }
        confirmarYEjecutar(
                "Derivar a notificación",
                "Se derivará el expediente " + item.getNumeroExpediente() + " a Notificación. ¿Desea continuar?",
                () -> analisisService.derivarNotificacionEspecial(item.getIdExpediente(), txtComentarioMovimiento.getText()));
    }

    private void archivarNoCorresponde() {
        AnalisisExpedienteDTO item = requerirSeleccion("Seleccione un expediente no corresponde para archivar.");
        if (item == null) {
            return;
        }
        confirmarYEjecutar(
                "Archivar no corresponde",
                "Se archivará el expediente " + item.getNumeroExpediente() + " por no corresponder a SDRERC. ¿Desea continuar?",
                () -> analisisService.archivarNoCorresponde(item.getIdExpediente(), txtComentarioMovimiento.getText()));
    }

    private void diagnosticarDerivacionExterna() {
        AnalisisExpedienteDTO item = requerirSeleccion("Seleccione un expediente no corresponde para preparar derivación externa.");
        if (item == null) {
            return;
        }
        JOptionPane.showMessageDialog(
                this,
                "La derivación externa está prevista por el flujo SDRERC_TO_BE, pero requiere registrar entidad destino, tipo de derivación y datos del documento enviado.\n"
                + "Esta escritura queda bloqueada hasta definir esos datos en el módulo correspondiente.",
                "Derivación externa",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmarYEjecutar(String titulo, String mensaje, OperacionAnalisis operacion) {
        int confirm = JOptionPane.showConfirmDialog(this, mensaje, titulo, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        setTrabajando(true, "Ejecutando operación de análisis...");
        SwingWorker<AnalisisResultadoDTO, Void> worker = new SwingWorker<AnalisisResultadoDTO, Void>() {
            @Override
            protected AnalisisResultadoDTO doInBackground() throws Exception {
                return operacion.ejecutar();
            }

            @Override
            protected void done() {
                try {
                    AnalisisResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelAnalisisV2.this,
                            resultado.getMensaje(),
                            "Análisis",
                            JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormulario();
                    buscar();
                } catch (Exception ex) {
                    mostrarError("No se pudo completar la operación de análisis.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private AnalisisRegistroDTO construirRegistro(AnalisisExpedienteDTO item) {
        ResultadoItem resultado = (ResultadoItem) cmbResultado.getSelectedItem();
        SimpleItem incorporado = (SimpleItem) cmbIncorporado.getSelectedItem();
        ObservacionAnalisisDTO observacion = null;
        if (chkRegistrarObservacion.isSelected() || resultadoRequiereObservacion(resultado)) {
            SimpleItem tipoObservacion = (SimpleItem) cmbTipoObservacion.getSelectedItem();
            observacion = new ObservacionAnalisisDTO(
                    tipoObservacion == null ? "" : tipoObservacion.codigo,
                    tipoObservacion == null ? "" : tipoObservacion.nombre,
                    txtObservacion.getText());
        }
        SimpleItem motivo = (SimpleItem) cmbMotivoNoCorresponde.getSelectedItem();
        return new AnalisisRegistroDTO(
                item.getIdExpediente(),
                resultado == null ? "" : resultado.codigo,
                resultado == null ? "" : resultado.nombre,
                resultado == null || resultado.codigo.isEmpty() ? null : !"NO_CORRESPONDE".equalsIgnoreCase(resultado.codigo),
                incorporado == null || incorporado.codigo.isEmpty() ? null : "SI".equalsIgnoreCase(incorporado.codigo),
                chkReconstitucion.isSelected(),
                chkLegitimidad.isSelected(),
                chkMediosProbatorios.isSelected(),
                txtFundamento.getText(),
                motivo == null ? "" : motivo.codigo,
                observacion,
                obtenerDocumentosFormulario());
    }

    private List<DocumentoAnalizadoDTO> obtenerDocumentosFormulario() {
        List<DocumentoAnalizadoDTO> documentos = new ArrayList<DocumentoAnalizadoDTO>();
        for (int i = 0; i < documentoModel.getRowCount(); i++) {
            documentos.add(DocumentoAnalizadoDTO.nuevo(
                    value(documentoModel.getValueAt(i, 4)),
                    value(documentoModel.getValueAt(i, 0)),
                    value(documentoModel.getValueAt(i, 5)),
                    value(documentoModel.getValueAt(i, 1)),
                    LocalDate.now(),
                    value(documentoModel.getValueAt(i, 3))));
        }
        return documentos;
    }

    private void agregarDocumento() {
        SimpleItem tipo = (SimpleItem) cmbTipoDocumento.getSelectedItem();
        SimpleItem estado = (SimpleItem) cmbEstadoDocumento.getSelectedItem();
        String descripcion = txtDescripcionDocumento.getText() == null ? "" : txtDescripcionDocumento.getText().trim();
        if (tipo == null || tipo.codigo.isEmpty()) {
            mostrarInfo("Seleccione el tipo de documento analizado.");
            return;
        }
        if (estado == null || estado.codigo.isEmpty()) {
            mostrarInfo("Seleccione el estado del documento analizado.");
            return;
        }
        if (descripcion.isEmpty()) {
            mostrarInfo("Ingrese la descripción del documento analizado.");
            return;
        }
        documentoModel.addRow(new Object[]{
            tipo.nombre,
            estado.nombre,
            DATE_FORMAT.format(LocalDate.now()),
            descripcion,
            tipo.codigo,
            estado.codigo
        });
        txtDescripcionDocumento.setText("");
    }

    private void quitarDocumento() {
        int row = documentosTable.getSelectedRow();
        if (row < 0) {
            mostrarInfo("Seleccione un documento para quitarlo del registro.");
            return;
        }
        documentoModel.removeRow(documentosTable.convertRowIndexToModel(row));
    }

    private void abrirDetalle() {
        AnalisisExpedienteDTO item = requerirSeleccion("Seleccione un expediente para ver el detalle.");
        if (item == null) {
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(owner, item.getIdExpediente());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private AnalisisExpedienteDTO requerirSeleccion(String mensaje) {
        AnalisisExpedienteDTO item = obtenerSeleccionado();
        if (item == null) {
            mostrarInfo(mensaje);
        }
        return item;
    }

    private AnalisisExpedienteDTO obtenerSeleccionado() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(selected);
        if (modelRow < 0 || modelRow >= expedientes.size()) {
            return null;
        }
        return expedientes.get(modelRow);
    }

    private void actualizarChecksIncorporado() {
        SimpleItem item = (SimpleItem) cmbIncorporado.getSelectedItem();
        boolean habilitar = item != null && "SI".equalsIgnoreCase(item.codigo);
        chkReconstitucion.setEnabled(habilitar);
        chkLegitimidad.setEnabled(habilitar);
        chkMediosProbatorios.setEnabled(habilitar);
        if (!habilitar) {
            chkReconstitucion.setSelected(false);
            chkLegitimidad.setSelected(false);
            chkMediosProbatorios.setSelected(false);
        }
    }

    private void actualizarResultadoSeleccionado() {
        if (cargandoCatalogos) {
            return;
        }
        ResultadoItem resultado = (ResultadoItem) cmbResultado.getSelectedItem();
        boolean noCorresponde = resultado != null && "NO_CORRESPONDE".equalsIgnoreCase(resultado.codigo);
        cmbMotivoNoCorresponde.setEnabled(noCorresponde);
        if (!noCorresponde && cmbMotivoNoCorresponde.getItemCount() > 0) {
            cmbMotivoNoCorresponde.setSelectedIndex(0);
        }
        if (resultadoRequiereObservacion(resultado)) {
            chkRegistrarObservacion.setSelected(true);
        }
        actualizarObservacionHabilitada();
    }

    private boolean resultadoRequiereObservacion(ResultadoItem resultado) {
        if (resultado == null) {
            return false;
        }
        return "OBSERVADO".equalsIgnoreCase(resultado.codigo)
                || "OBSERVACION_ADMINISTRATIVA".equalsIgnoreCase(resultado.codigo);
    }

    private void actualizarObservacionHabilitada() {
        boolean habilitar = chkRegistrarObservacion.isSelected();
        cmbTipoObservacion.setEnabled(habilitar);
        txtObservacion.setEnabled(habilitar);
    }

    private void limpiarFormulario() {
        if (cmbResultado.getItemCount() > 0) {
            cmbResultado.setSelectedIndex(0);
        }
        if (cmbIncorporado.getItemCount() > 0) {
            cmbIncorporado.setSelectedIndex(0);
        }
        if (cmbMotivoNoCorresponde.getItemCount() > 0) {
            cmbMotivoNoCorresponde.setSelectedIndex(0);
        }
        txtFundamento.setText("");
        txtComentarioMovimiento.setText("");
        txtDescripcionDocumento.setText("");
        txtObservacion.setText("");
        chkRegistrarObservacion.setSelected(false);
        documentoModel.setRowCount(0);
        actualizarChecksIncorporado();
        actualizarObservacionHabilitada();
    }

    private void setTrabajando(boolean trabajando, String mensaje) {
        btnBuscar.setEnabled(!trabajando);
        btnLimpiar.setEnabled(!trabajando);
        btnRefrescar.setEnabled(!trabajando);
        btnVerDetalle.setEnabled(!trabajando && obtenerSeleccionado() != null);
        btnRecibir.setEnabled(!trabajando && obtenerSeleccionado() != null && obtenerSeleccionado().isRecibible());
        btnRegistrarAnalisis.setEnabled(!trabajando && obtenerSeleccionado() != null && obtenerSeleccionado().isRegistrable());
        btnEnviarVerificacion.setEnabled(!trabajando && obtenerSeleccionado() != null && obtenerSeleccionado().isEnviableVerificacion());
        btnDerivarNotificacion.setEnabled(!trabajando && obtenerSeleccionado() != null && obtenerSeleccionado().isDerivableNotificacionEspecial());
        btnArchivarNoCorresponde.setEnabled(!trabajando && obtenerSeleccionado() != null && obtenerSeleccionado().isArchivableNoCorresponde());
        btnDerivarExterna.setEnabled(!trabajando && obtenerSeleccionado() != null && obtenerSeleccionado().isArchivableNoCorresponde());
        if (mensaje != null) {
            lblEstado.setText(mensaje);
        }
    }

    private String obtenerCodigo(JComboBox<SimpleItem> combo) {
        Object selected = combo.getSelectedItem();
        return selected instanceof SimpleItem ? ((SimpleItem) selected).codigo : "";
    }

    private static String formatDate(LocalDate value) {
        return value == null ? "" : DATE_FORMAT.format(value);
    }

    private static String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private void mostrarInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Análisis", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String context, Exception ex) {
        String message = ex.getMessage();
        if (message == null && ex.getCause() != null) {
            message = ex.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(
                this,
                context + (message == null ? "" : "\n" + message),
                "Error de análisis",
                JOptionPane.ERROR_MESSAGE);
        lblEstado.setText(context);
    }

    private interface OperacionAnalisis {
        AnalisisResultadoDTO ejecutar() throws Exception;
    }

    private class AnalisisTableModel extends DefaultTableModel {
        private AnalisisTableModel() {
            super(new Object[]{
                "ID",
                "Expediente",
                "Trámite",
                "Procedimiento",
                "Tipo doc.",
                "Tipo acta",
                "Nro. acta",
                "Titular",
                "Recepción",
                "Responsable",
                "Etapa",
                "Estado",
                "Días",
                "Observación",
                "Asociados"
            }, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private static class AnalisisRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            int modelColumn = table.convertColumnIndexToModel(column);
            if (!isSelected && modelColumn == 10) {
                return StatusBadgeV2.forEtapa(value == null ? "" : value.toString());
            }
            if (!isSelected && modelColumn == 11) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
            }
            if (!isSelected && modelColumn == 13 && value != null && value.toString().startsWith("Con")) {
                return new BadgeV2(value.toString(), AppV2Theme.SOFT_ORANGE, AppV2Theme.WARNING);
            }
            if (!isSelected && modelColumn == 14 && value != null && !value.toString().startsWith("Sin")) {
                return new BadgeV2(value.toString(), AppV2Theme.SOFT_BLUE, AppV2Theme.INFO);
            }
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
                c.setForeground(modelColumn == 1 ? AppV2Theme.PRIMARY : AppV2Theme.TEXT_PRIMARY);
            }
            return c;
        }
    }

    private static class SimpleItem {
        private final String codigo;
        private final String nombre;

        private SimpleItem(String codigo, String nombre) {
            this.codigo = codigo == null ? "" : codigo;
            this.nombre = nombre == null ? "" : nombre;
        }

        @Override
        public String toString() {
            return nombre.isEmpty() ? codigo : nombre;
        }
    }

    private static class ResultadoItem {
        private final String codigo;
        private final String nombre;

        private ResultadoItem(CatalogoItemDTO item) {
            this.codigo = item.getCodigo() == null ? "" : item.getCodigo();
            this.nombre = item.getNombre() == null ? this.codigo : item.getNombre();
        }

        private ResultadoItem(String codigo, String nombre) {
            this.codigo = codigo == null ? "" : codigo;
            this.nombre = nombre == null ? "" : nombre;
        }

        private static ResultadoItem placeholder(String nombre) {
            return new ResultadoItem("", nombre);
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    private static class CatalogosCarga {
        private final List<CatalogoItemDTO> resultados;
        private final List<CatalogoItemDTO> tiposDocumento;
        private final List<CatalogoItemDTO> estadosDocumento;
        private final List<CatalogoItemDTO> tiposObservacion;
        private final List<CatalogoItemDTO> motivosNoCorresponde;

        private CatalogosCarga(
                List<CatalogoItemDTO> resultados,
                List<CatalogoItemDTO> tiposDocumento,
                List<CatalogoItemDTO> estadosDocumento,
                List<CatalogoItemDTO> tiposObservacion,
                List<CatalogoItemDTO> motivosNoCorresponde) {
            this.resultados = resultados == null ? new ArrayList<CatalogoItemDTO>() : resultados;
            this.tiposDocumento = tiposDocumento == null ? new ArrayList<CatalogoItemDTO>() : tiposDocumento;
            this.estadosDocumento = estadosDocumento == null ? new ArrayList<CatalogoItemDTO>() : estadosDocumento;
            this.tiposObservacion = tiposObservacion == null ? new ArrayList<CatalogoItemDTO>() : tiposObservacion;
            this.motivosNoCorresponde = motivosNoCorresponde == null ? new ArrayList<CatalogoItemDTO>() : motivosNoCorresponde;
        }
    }
}
