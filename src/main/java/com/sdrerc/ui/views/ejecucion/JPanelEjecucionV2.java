package com.sdrerc.ui.views.ejecucion;

import com.sdrerc.application.sdrercapp.DocumentoEjecucionService;
import com.sdrerc.application.sdrercapp.EjecucionExpedienteService;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoEjecucionDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionReversionDTO;
import com.sdrerc.ui.appv2.components.AppV2ActionPanel;
import com.sdrerc.ui.appv2.components.AppV2NotebookToggleTab;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2SearchToolbar;
import com.sdrerc.ui.appv2.components.AppV2SideActionPanel;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.components.AppV2TablePanel;
import com.sdrerc.ui.appv2.components.AppV2TableSectionPanel;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.helpers.EstadoExpedienteComboSupportV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import com.sdrerc.ui.views.expedienteconsola.DlgConsolaExpedienteV2;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class JPanelEjecucionV2 extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int PANEL_EJECUCION_ANCHO_MINIMO = 380;
    private static final int PANEL_EJECUCION_ANCHO_NORMAL = 430;
    private static final int PANEL_EJECUCION_TAB_OVERHANG = 18;
    private static final int PANEL_EJECUCION_TAB_TOP = 18;

    private final EjecucionExpedienteService ejecucionService;
    private final DocumentoEjecucionService documentoService;

    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite/SITD, titular o resolución", 28);
    private final JComboBox<SimpleItem> cmbEstadoFiltro = new JComboBox<SimpleItem>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnVerDetalle = new JButton("Ver detalle");
    private final JButton btnRegistrarEjecucion = new JButton("Registrar ejecución");
    private final JButton btnMarcarEjecutado = new JButton("Marcar ejecutado");
    private final JButton btnObservar = new JButton("Observación ejecución");
    private final JButton btnDocumentoInconsistente = new JButton("Documento inconsistente");
    private final JButton btnRevertirAnalisis = new JButton("Revertir a Análisis");
    private final JButton btnDerivarNotificacion = new JButton("Derivar a Notificación");

    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar expedientes en Ejecución.");
    private final JLabel lblExpediente = new JLabel("-");
    private final JLabel lblTitular = new JLabel("-");
    private final JLabel lblActa = new JLabel("-");
    private final JLabel lblProcedimiento = new JLabel("-");
    private final JLabel lblResponsable = new JLabel("-");
    private final JLabel lblEtapaEstado = new JLabel("-");
    private final JLabel lblResolucion = new JLabel("-");
    private final JLabel lblAnalisis = new JLabel("-");
    private final JLabel lblVerificacion = new JLabel("-");
    private final JLabel lblAlertas = new JLabel("Sin alertas.");
    private final JLabel lblAcciones = new JLabel("-");

    private final JComboBox<SimpleItem> cmbResultado = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbTipoObservacion = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbMotivoCorreccion = new JComboBox<SimpleItem>();
    private final JTextField txtFechaEjecucion = new JTextField(10);
    private final JTextArea txtComentario = new JTextArea(4, 22);
    private final JTextArea txtMotivoReversion = new JTextArea(3, 22);
    private final JTextArea txtFundamentoAnalisis = new JTextArea(4, 22);
    private final JTextArea txtObservacion = new JTextArea(3, 22);
    private final AppV2NotebookToggleTab tabPanelEjecucion = new AppV2NotebookToggleTab();

    private final EjecucionTableModel tableModel = new EjecucionTableModel();
    private final JTable table = new AppV2Table(tableModel);
    private final AppV2TablePanel tablePanel = new AppV2TablePanel(
            table,
            "Sin expedientes para mostrar",
            "Seleccione filtros y presione Buscar.");
    private final DefaultTableModel documentosModel = new DefaultTableModel(
            new Object[]{"Tipo", "Estado", "Número", "Documento", "Fecha"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable documentosTable = new JTable(documentosModel);
    private final List<EjecucionExpedienteDTO> expedientes = new ArrayList<EjecucionExpedienteDTO>();

    private final MetricCardV2 cardEnEjecucion = new MetricCardV2("En ejecución", "0", "Pendientes de atención", AppV2Theme.INFO);
    private final MetricCardV2 cardConCorreccion = new MetricCardV2("Con corrección", "0", "Observados o inconsistentes", AppV2Theme.WARNING);
    private final MetricCardV2 cardEjecutados = new MetricCardV2("Ejecutados", "0", "Listos para notificación", AppV2Theme.SUCCESS);
    private AppV2OperationalSplitPanel splitOperativo;
    private AppV2SideActionPanel panelEjecucion;
    private boolean panelEjecucionCerradoPorUsuario;

    public JPanelEjecucionV2() {
        this(new EjecucionExpedienteService(), new DocumentoEjecucionService());
    }

    public JPanelEjecucionV2(
            EjecucionExpedienteService ejecucionService,
            DocumentoEjecucionService documentoService) {
        this.ejecucionService = ejecucionService;
        this.documentoService = documentoService;
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());
        add(crearCentro(), BorderLayout.CENTER);
        configurarTabla();
        configurarDocumentosTabla();
        configurarEventos();
        cargarFiltrosBase();
        cargarCatalogos();
        inicializarFecha();
        actualizarSeleccion();
    }

    private JPanel crearHeader() {
        JPanel metricas = new JPanel(new GridLayout(1, 3, 12, 0));
        metricas.setOpaque(false);
        metricas.add(cardEnEjecucion);
        metricas.add(cardConCorreccion);
        metricas.add(cardEjecutados);
        return metricas;
    }

    private JPanel crearCentro() {
        JPanel contenidoPrincipal = new JPanel(new BorderLayout(14, 14));
        contenidoPrincipal.setOpaque(false);
        contenidoPrincipal.add(crearHeader(), BorderLayout.NORTH);

        JPanel contenidoOperativo = new JPanel(new BorderLayout(14, 14));
        contenidoOperativo.setOpaque(false);
        contenidoOperativo.add(crearBuscador(), BorderLayout.NORTH);
        contenidoOperativo.add(crearBandeja(), BorderLayout.CENTER);
        contenidoPrincipal.add(contenidoOperativo, BorderLayout.CENTER);

        panelEjecucion = crearPanelEjecucion();
        JPanel panelConTab = crearPanelEjecucionConTab(panelEjecucion);
        splitOperativo = new AppV2OperationalSplitPanel(
                contenidoPrincipal,
                panelConTab,
                0,
                PANEL_EJECUCION_ANCHO_MINIMO + PANEL_EJECUCION_TAB_OVERHANG,
                PANEL_EJECUCION_ANCHO_NORMAL + PANEL_EJECUCION_TAB_OVERHANG);
        return splitOperativo;
    }

    private JPanel crearBuscador() {
        configurarControles();
        AppV2SearchToolbar toolbar = new AppV2SearchToolbar();
        JPanel accionesFiltro = AppV2ActionPanel.right();
        accionesFiltro.add(btnBuscar);
        accionesFiltro.add(btnLimpiar);
        accionesFiltro.add(btnVerDetalle);
        accionesFiltro.add(btnRefrescar);
        toolbar.addSearchRow("Búsqueda", txtBusqueda, accionesFiltro);
        toolbar.addFilter("Estado", cmbEstadoFiltro);
        toolbar.addFilter("Mostrar", spnLimite);
        return toolbar;
    }

    private JPanel crearBandeja() {
        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);
        AppV2TableSectionPanel section = new AppV2TableSectionPanel(tablePanel);
        section.setStatus(lblEstado);
        return section;
    }

    private AppV2SideActionPanel crearPanelEjecucion() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de ejecución", new Runnable() {
            @Override
            public void run() {
                cerrarPanelEjecucion();
            }
        });
        panel.setAccentColor(AppV2Theme.PRIMARY);
        tabPanelEjecucion.setAccent(AppV2Theme.PRIMARY, AppV2Theme.SOFT_BLUE);
        tabPanelEjecucion.setExpanded(false);
        tabPanelEjecucion.setToolTipText("Ampliar panel de ejecución");
        tabPanelEjecucion.addActionListener(e -> alternarExpansionPanelEjecucion());
        panel.addSection(crearResumenSeleccion());
        panel.addSection(crearAntecedentes());
        panel.addSection(crearDocumentosPanel());
        panel.addSection(crearFormularioEjecucion());
        panel.setFooter(crearAccionesPanelEjecucion());
        return panel;
    }

    private JPanel crearPanelEjecucionConTab(final AppV2SideActionPanel panel) {
        JPanel wrapper = new JPanel(null) {
            @Override
            public void doLayout() {
                int width = getWidth();
                int height = getHeight();
                int panelX = PANEL_EJECUCION_TAB_OVERHANG;
                panel.setBounds(panelX, 0, Math.max(0, width - panelX), height);
                int tabY = Math.min(PANEL_EJECUCION_TAB_TOP, Math.max(0, height - AppV2NotebookToggleTab.DEFAULT_HEIGHT));
                tabPanelEjecucion.setBounds(
                        0,
                        tabY,
                        AppV2NotebookToggleTab.DEFAULT_WIDTH,
                        AppV2NotebookToggleTab.DEFAULT_HEIGHT);
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(panel);
        wrapper.add(tabPanelEjecucion);
        wrapper.setMinimumSize(new Dimension(PANEL_EJECUCION_ANCHO_MINIMO + PANEL_EJECUCION_TAB_OVERHANG, 0));
        wrapper.setPreferredSize(new Dimension(PANEL_EJECUCION_ANCHO_NORMAL + PANEL_EJECUCION_TAB_OVERHANG, 0));
        return wrapper;
    }

    private JPanel crearAccionesPanelEjecucion() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setOpaque(false);
        panel.add(btnRegistrarEjecucion);
        panel.add(btnMarcarEjecutado);
        panel.add(btnObservar);
        panel.add(btnDocumentoInconsistente);
        panel.add(btnRevertirAnalisis);
        panel.add(btnDerivarNotificacion);
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
        addRow(grid, row++, "Resolución", lblResolucion);
        addRow(grid, row++, "Acciones", lblAcciones);
        addRow(grid, row, "Alertas", lblAlertas);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearAntecedentes() {
        JPanel panel = section("Antecedentes");
        txtFundamentoAnalisis.setEditable(false);
        txtFundamentoAnalisis.setBackground(AppV2Theme.SURFACE_ALT);
        txtObservacion.setEditable(false);
        txtObservacion.setBackground(AppV2Theme.SURFACE_ALT);
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Análisis", lblAnalisis);
        addRow(grid, row++, "Verificación", lblVerificacion);
        addRow(grid, row++, "Sustento", scrollText(txtFundamentoAnalisis, 80));
        addRow(grid, row, "Observación", scrollText(txtObservacion, 70));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDocumentosPanel() {
        JPanel panel = section("Documentos y resolución");
        JScrollPane scroll = new JScrollPane(documentosTable);
        scroll.setPreferredSize(new Dimension(355, 132));
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormularioEjecucion() {
        JPanel panel = section("Registro operativo");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Resultado", cmbResultado);
        addRow(grid, row++, "Tipo observación", cmbTipoObservacion);
        addRow(grid, row++, "Motivo corrección", cmbMotivoCorreccion);
        addRow(grid, row++, "Fecha ejecución", txtFechaEjecucion);
        addRow(grid, row++, "Comentario", scrollText(txtComentario, 88));
        addRow(grid, row, "Motivo reversión", scrollText(txtMotivoReversion, 78));
        panel.add(grid, BorderLayout.CENTER);
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
        scroll.setPreferredSize(new Dimension(250, height));
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
        target.add(label(label), gbcLabel);
        target.add(component, gbcValue);
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        label.setForeground(AppV2Theme.TEXT_SECONDARY);
        return label;
    }

    private void configurarControles() {
        txtBusqueda.setPreferredSize(new Dimension(340, 34));
        cmbEstadoFiltro.setPreferredSize(new Dimension(220, 34));
        cmbResultado.setPreferredSize(new Dimension(250, 34));
        cmbTipoObservacion.setPreferredSize(new Dimension(250, 34));
        cmbMotivoCorreccion.setPreferredSize(new Dimension(250, 34));
        txtFechaEjecucion.setPreferredSize(new Dimension(250, 34));
        spnLimite.setPreferredSize(new Dimension(86, 34));
        btnBuscar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarEjecucion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnMarcarEjecutado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnObservar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnDocumentoInconsistente.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRevertirAnalisis.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnDerivarNotificacion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
    }

    private void configurarTabla() {
        table.setRowHeight(34);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        table.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        table.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        table.setGridColor(AppV2Theme.BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setDefaultRenderer(Object.class, new EjecucionRenderer());
        table.getColumnModel().getColumn(11).setPreferredWidth(145);
        table.getColumnModel().getColumn(12).setPreferredWidth(160);
        table.getColumnModel().getColumn(13).setMaxWidth(92);
        table.getColumnModel().getColumn(15).setMaxWidth(92);
        table.getColumnModel().getColumn(16).setMaxWidth(92);
        AppV2TableColumnSizer.applyFriendlyDefaults(table);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        tablePanel.getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private void configurarDocumentosTabla() {
        documentosTable.setRowHeight(30);
        documentosTable.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        documentosTable.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        documentosTable.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        documentosTable.setGridColor(AppV2Theme.BORDER);
        documentosTable.setShowVerticalLines(false);
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnRefrescar.addActionListener(e -> buscar());
        btnVerDetalle.addActionListener(e -> abrirDetalle());
        btnRegistrarEjecucion.addActionListener(e -> registrarEjecucion());
        btnMarcarEjecutado.addActionListener(e -> marcarEjecutado());
        btnObservar.addActionListener(e -> registrarObservacion());
        btnDocumentoInconsistente.addActionListener(e -> registrarDocumentoInconsistente());
        btnRevertirAnalisis.addActionListener(e -> revertirAnalisis());
        btnDerivarNotificacion.addActionListener(e -> derivarNotificacion());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                panelEjecucionCerradoPorUsuario = false;
                actualizarSeleccion();
            }
        });
    }

    private void cargarFiltrosBase() {
        EstadoExpedienteComboSupportV2.cargar(
                cmbEstadoFiltro, "EJECUCION", new SimpleItem("TODOS", "Todos los estados"),
                (codigo, nombre) -> new SimpleItem(codigo, nombre),
                ex -> lblEstado.setText("No se pudieron cargar los estados de Ejecución."));
    }

    private void cargarCatalogos() {
        setTrabajando(true, "Cargando catálogos de ejecución...");
        SwingWorker<CatalogosCarga, Void> worker = new SwingWorker<CatalogosCarga, Void>() {
            @Override
            protected CatalogosCarga doInBackground() throws Exception {
                return new CatalogosCarga(
                        ejecucionService.listarResultadosEjecucion(),
                        ejecucionService.listarTiposObservacion(),
                        ejecucionService.listarMotivosCorreccion());
            }

            @Override
            protected void done() {
                try {
                    cargarCatalogosVista(get());
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los catálogos de ejecución.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void cargarCatalogosVista(CatalogosCarga carga) {
        cargarSimpleItems(cmbResultado, carga.resultados, "Seleccione resultado");
        cargarSimpleItems(cmbTipoObservacion, carga.tiposObservacion, "Seleccione tipo");
        cargarSimpleItems(cmbMotivoCorreccion, carga.motivosCorreccion, "Seleccione motivo");
    }

    private void cargarSimpleItems(JComboBox<SimpleItem> combo, List<CatalogoItemDTO> items, String placeholder) {
        combo.removeAllItems();
        combo.addItem(new SimpleItem("", placeholder));
        for (CatalogoItemDTO item : items) {
            combo.addItem(new SimpleItem(item.getCodigo(), item.getNombre()));
        }
    }

    private void inicializarFecha() {
        txtFechaEjecucion.setText(DATE_FORMAT.format(LocalDate.now()));
    }

    private void buscar() {
        setTrabajando(true, "Consultando expedientes en Ejecución...");
        String texto = txtBusqueda.getText();
        String estado = obtenerCodigo(cmbEstadoFiltro);
        int limite = ((Number) spnLimite.getValue()).intValue();
        SwingWorker<List<EjecucionExpedienteDTO>, Void> worker = new SwingWorker<List<EjecucionExpedienteDTO>, Void>() {
            @Override
            protected List<EjecucionExpedienteDTO> doInBackground() throws Exception {
                return ejecucionService.buscarExpedientes(texto, estado, limite);
            }

            @Override
            protected void done() {
                try {
                    cargarTabla(get());
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar la bandeja de Ejecución.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void cargarTabla(List<EjecucionExpedienteDTO> items) {
        expedientes.clear();
        expedientes.addAll(items);
        tableModel.fireTableDataChanged();
        table.clearSelection();
        tablePanel.setEmpty(items.isEmpty());
        actualizarMetricas();
        if (items.isEmpty()) {
            limpiarDetalle();
        } else {
            actualizarSeleccion();
        }
        lblEstado.setText(items.size() + " expediente(s) en Ejecución.");
    }

    private void actualizarMetricas() {
        int enEjecucion = 0;
        int correccion = 0;
        int ejecutados = 0;
        for (EjecucionExpedienteDTO expediente : expedientes) {
            if ("EN_EJECUCION".equalsIgnoreCase(expediente.getEstadoCodigo())
                    || "INDAGATORIO".equalsIgnoreCase(expediente.getEstadoCodigo())) {
                enEjecucion++;
            } else if ("DOCUMENTO_INCONSISTENTE".equalsIgnoreCase(expediente.getEstadoCodigo())
                    || "REQUIERE_CORRECCION".equalsIgnoreCase(expediente.getEstadoCodigo())) {
                correccion++;
            } else if ("EJECUTADO".equalsIgnoreCase(expediente.getEstadoCodigo())) {
                ejecutados++;
            }
        }
        cardEnEjecucion.setValue(String.valueOf(enEjecucion));
        cardConCorreccion.setValue(String.valueOf(correccion));
        cardEjecutados.setValue(String.valueOf(ejecutados));
    }

    private void limpiar() {
        txtBusqueda.setText("");
        cmbEstadoFiltro.setSelectedIndex(0);
        spnLimite.setValue(200);
        expedientes.clear();
        tableModel.fireTableDataChanged();
        table.clearSelection();
        tablePanel.setEmpty(true);
        actualizarMetricas();
        limpiarDetalle();
        lblEstado.setText("Filtros limpiados. Presione Buscar para consultar expedientes.");
        panelEjecucionCerradoPorUsuario = false;
    }

    private void actualizarSeleccion() {
        EjecucionExpedienteDTO expediente = seleccionado();
        actualizarVisibilidadPanelEjecucion();
        if (expediente == null) {
            limpiarDetalle();
            return;
        }
        lblExpediente.setText(valor(expediente.getNumeroExpediente()));
        lblTitular.setText(valor(expediente.getTitular()));
        lblActa.setText(valor(expediente.getTipoActa()) + " " + valor(expediente.getNumeroActa()));
        lblProcedimiento.setText(valor(expediente.getProcedimiento()));
        lblResponsable.setText(valor(expediente.getResponsable()) + " / " + valor(expediente.getEquipo()));
        lblEtapaEstado.setText(DisplayNameMapperV2.etapa(expediente.getEtapaCodigo())
                + " / " + DisplayNameMapperV2.estado(expediente.getEstadoCodigo()));
        lblResolucion.setText(resolucionTexto(expediente));
        lblAnalisis.setText(valor(expediente.getResultadoAnalisis()));
        lblVerificacion.setText(valor(expediente.getResultadoVerificacion()));
        lblAcciones.setText(accionesTexto(expediente));
        lblAlertas.setText(alertasTexto(expediente));
        txtFundamentoAnalisis.setText(expediente.getFundamentoAnalisis());
        txtObservacion.setText(expediente.getUltimaObservacion());
        inicializarFecha();
        cargarDocumentos(expediente.getIdExpediente());
        actualizarAcciones(expediente);
    }

    private void limpiarDetalle() {
        lblExpediente.setText("-");
        lblTitular.setText("-");
        lblActa.setText("-");
        lblProcedimiento.setText("-");
        lblResponsable.setText("-");
        lblEtapaEstado.setText("-");
        lblResolucion.setText("-");
        lblAnalisis.setText("-");
        lblVerificacion.setText("-");
        lblAcciones.setText("-");
        lblAlertas.setText("Sin expediente seleccionado.");
        txtComentario.setText("");
        txtMotivoReversion.setText("");
        txtFundamentoAnalisis.setText("");
        txtObservacion.setText("");
        inicializarFecha();
        documentosModel.setRowCount(0);
        actualizarAcciones(null);
    }

    private void cargarDocumentos(Long idExpediente) {
        documentosModel.setRowCount(0);
        SwingWorker<List<DocumentoEjecucionDTO>, Void> worker = new SwingWorker<List<DocumentoEjecucionDTO>, Void>() {
            @Override
            protected List<DocumentoEjecucionDTO> doInBackground() throws Exception {
                return documentoService.listarPorExpediente(idExpediente);
            }

            @Override
            protected void done() {
                try {
                    cargarDocumentosVista(get());
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los documentos del expediente.", ex);
                }
            }
        };
        worker.execute();
    }

    private void cargarDocumentosVista(List<DocumentoEjecucionDTO> documentos) {
        documentosModel.setRowCount(0);
        for (DocumentoEjecucionDTO documento : documentos) {
            documentosModel.addRow(new Object[]{
                valor(documento.getTipoDocumento()),
                valor(documento.getEstadoDocumento()),
                valor(documento.getNumeroDocumento()),
                valor(documento.getNombreDocumento()),
                format(documento.getFechaDocumento())
            });
        }
    }

    private void actualizarAcciones(EjecucionExpedienteDTO expediente) {
        boolean seleccionado = expediente != null;
        btnVerDetalle.setEnabled(seleccionado);
        btnRegistrarEjecucion.setEnabled(seleccionado && expediente.hasAccion(EjecucionExpedienteService.ACCION_INICIO_EJECUCION));
        btnMarcarEjecutado.setEnabled(seleccionado && expediente.hasAccion(EjecucionExpedienteService.ACCION_INICIO_EJECUCION));
        btnObservar.setEnabled(seleccionado && expediente.hasAccion(EjecucionExpedienteService.ACCION_OBSERVACION_EJECUCION));
        btnDocumentoInconsistente.setEnabled(seleccionado && expediente.hasAccion(EjecucionExpedienteService.ACCION_DOCUMENTO_INCONSISTENTE));
        btnRevertirAnalisis.setEnabled(seleccionado && expediente.hasAccion(EjecucionExpedienteService.ACCION_DEVOLUCION_ANALISIS));
        btnDerivarNotificacion.setEnabled(seleccionado && expediente.hasAccion(EjecucionExpedienteService.ACCION_DERIVACION_NOTIFICACION));
    }

    private void registrarEjecucion() {
        EjecucionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null || !confirmar("Se registrará la ejecución del expediente " + expediente.getNumeroExpediente() + ". ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando ejecución...", new Callable<EjecucionResultadoDTO>() {
            @Override
            public EjecucionResultadoDTO call() throws Exception {
                return ejecucionService.registrarEjecucion(crearRegistro(EjecucionExpedienteService.ACCION_INICIO_EJECUCION));
            }
        });
    }

    private void marcarEjecutado() {
        EjecucionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null || !confirmar("El expediente " + expediente.getNumeroExpediente() + " será marcado como ejecutado. ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Marcando expediente como ejecutado...", new Callable<EjecucionResultadoDTO>() {
            @Override
            public EjecucionResultadoDTO call() throws Exception {
                return ejecucionService.marcarEjecutado(crearRegistro(EjecucionExpedienteService.ACCION_INICIO_EJECUCION));
            }
        });
    }

    private void registrarObservacion() {
        EjecucionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null || !confirmar("Se registrará una observación de ejecución para " + expediente.getNumeroExpediente() + ". ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando observación de ejecución...", new Callable<EjecucionResultadoDTO>() {
            @Override
            public EjecucionResultadoDTO call() throws Exception {
                return ejecucionService.registrarObservacion(crearRegistro(EjecucionExpedienteService.ACCION_OBSERVACION_EJECUCION));
            }
        });
    }

    private void registrarDocumentoInconsistente() {
        EjecucionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null || !confirmar("Se registrará documento inconsistente para " + expediente.getNumeroExpediente() + ". ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando documento inconsistente...", new Callable<EjecucionResultadoDTO>() {
            @Override
            public EjecucionResultadoDTO call() throws Exception {
                return ejecucionService.registrarDocumentoInconsistente(crearRegistro(EjecucionExpedienteService.ACCION_DOCUMENTO_INCONSISTENTE));
            }
        });
    }

    private void revertirAnalisis() {
        EjecucionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null || !confirmar("El expediente " + expediente.getNumeroExpediente() + " será devuelto a Análisis. ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Devolviendo expediente a Análisis...", new Callable<EjecucionResultadoDTO>() {
            @Override
            public EjecucionResultadoDTO call() throws Exception {
                return ejecucionService.revertirAnalisis(crearReversion());
            }
        });
    }

    private void derivarNotificacion() {
        EjecucionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null || !confirmar("El expediente " + expediente.getNumeroExpediente() + " será derivado a Notificación. ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Derivando expediente a Notificación...", new Callable<EjecucionResultadoDTO>() {
            @Override
            public EjecucionResultadoDTO call() throws Exception {
                return ejecucionService.derivarNotificacion(crearRegistro(EjecucionExpedienteService.ACCION_DERIVACION_NOTIFICACION));
            }
        });
    }

    private void ejecutarOperacion(String mensajeTrabajo, Callable<EjecucionResultadoDTO> operacion) {
        setTrabajando(true, mensajeTrabajo);
        SwingWorker<EjecucionResultadoDTO, Void> worker = new SwingWorker<EjecucionResultadoDTO, Void>() {
            @Override
            protected EjecucionResultadoDTO doInBackground() throws Exception {
                return operacion.call();
            }

            @Override
            protected void done() {
                try {
                    EjecucionResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelEjecucionV2.this,
                            resultado.getMensaje(),
                            "Ejecución",
                            JOptionPane.INFORMATION_MESSAGE);
                    buscar();
                } catch (Exception ex) {
                    mostrarError("No se pudo completar la acción.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private EjecucionRegistroDTO crearRegistro(String accionCodigo) {
        EjecucionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        SimpleItem resultado = (SimpleItem) cmbResultado.getSelectedItem();
        SimpleItem tipoObservacion = (SimpleItem) cmbTipoObservacion.getSelectedItem();
        SimpleItem motivoCorreccion = (SimpleItem) cmbMotivoCorreccion.getSelectedItem();
        return new EjecucionRegistroDTO(
                expediente.getIdExpediente(),
                accionCodigo,
                resultado == null ? "" : resultado.getCodigo(),
                resultado == null ? "" : resultado.getNombre(),
                parseFecha(),
                tipoObservacion == null ? "" : tipoObservacion.getCodigo(),
                motivoCorreccion == null ? "" : motivoCorreccion.getCodigo(),
                txtComentario.getText());
    }

    private EjecucionReversionDTO crearReversion() {
        EjecucionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        SimpleItem tipoObservacion = (SimpleItem) cmbTipoObservacion.getSelectedItem();
        SimpleItem motivoCorreccion = (SimpleItem) cmbMotivoCorreccion.getSelectedItem();
        return new EjecucionReversionDTO(
                expediente.getIdExpediente(),
                EjecucionExpedienteService.ACCION_DEVOLUCION_ANALISIS,
                tipoObservacion == null ? "" : tipoObservacion.getCodigo(),
                motivoCorreccion == null ? "" : motivoCorreccion.getCodigo(),
                txtMotivoReversion.getText(),
                txtComentario.getText());
    }

    private LocalDate parseFecha() {
        String value = txtFechaEjecucion.getText() == null ? "" : txtFechaEjecucion.getText().trim();
        if (value.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ingrese una fecha de ejecución válida con formato yyyy-MM-dd.");
        }
    }

    private void abrirDetalle() {
        EjecucionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(owner, expediente.getIdExpediente());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private EjecucionExpedienteDTO seleccionado() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= expedientes.size()) {
            return null;
        }
        return expedientes.get(modelRow);
    }

    private EjecucionExpedienteDTO requerirSeleccion() {
        EjecucionExpedienteDTO expediente = seleccionado();
        if (expediente == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un expediente.", "Ejecución", JOptionPane.INFORMATION_MESSAGE);
        }
        return expediente;
    }

    private boolean confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(
                this,
                mensaje,
                "Confirmar acción",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void setTrabajando(boolean trabajando, String mensaje) {
        btnBuscar.setEnabled(!trabajando);
        btnLimpiar.setEnabled(!trabajando);
        btnRefrescar.setEnabled(!trabajando);
        table.setEnabled(!trabajando);
        if (mensaje != null) {
            lblEstado.setText(mensaje);
        }
        if (!trabajando) {
            actualizarAcciones(seleccionado());
        } else {
            btnVerDetalle.setEnabled(false);
            btnRegistrarEjecucion.setEnabled(false);
            btnMarcarEjecutado.setEnabled(false);
            btnObservar.setEnabled(false);
            btnDocumentoInconsistente.setEnabled(false);
            btnRevertirAnalisis.setEnabled(false);
            btnDerivarNotificacion.setEnabled(false);
        }
    }

    private String obtenerCodigo(JComboBox<SimpleItem> combo) {
        SimpleItem item = (SimpleItem) combo.getSelectedItem();
        return item == null ? "" : item.getCodigo();
    }

    private void cerrarPanelEjecucion() {
        panelEjecucionCerradoPorUsuario = true;
        if (splitOperativo != null) {
            splitOperativo.setSideVisible(false);
        }
        tabPanelEjecucion.setExpanded(false);
        actualizarTooltipTabPanelEjecucion();
    }

    private void actualizarVisibilidadPanelEjecucion() {
        if (splitOperativo == null) {
            return;
        }
        splitOperativo.setSideVisible(seleccionado() != null && !panelEjecucionCerradoPorUsuario);
        tabPanelEjecucion.setExpanded(splitOperativo.isSideExpanded());
        actualizarTooltipTabPanelEjecucion();
    }

    private void alternarExpansionPanelEjecucion() {
        if (splitOperativo == null || !splitOperativo.isSideVisible()) {
            return;
        }
        boolean expandido = splitOperativo.toggleSideExpanded();
        tabPanelEjecucion.setExpanded(expandido);
        actualizarTooltipTabPanelEjecucion();
        revalidate();
        repaint();
    }

    private void actualizarTooltipTabPanelEjecucion() {
        boolean expandido = splitOperativo != null && splitOperativo.isSideExpanded();
        tabPanelEjecucion.setToolTipText(expandido ? "Restaurar panel de ejecución" : "Ampliar panel de ejecución");
    }

    private String alertasTexto(EjecucionExpedienteDTO expediente) {
        List<String> alertas = new ArrayList<String>();
        if (expediente.getTotalRelacionados() > 0) {
            alertas.add(expediente.getTotalRelacionados() + " expediente(s) asociado(s)");
        }
        if (expediente.getTotalDocumentos() == 0) {
            alertas.add("Sin documentos registrados");
        }
        if (!hasText(expediente.getNumeroResolucion())) {
            alertas.add("Sin número de resolución visible");
        }
        if (expediente.isEnEjecucion() && !expediente.hasAccion(EjecucionExpedienteService.ACCION_INICIO_EJECUCION)) {
            alertas.add("No hay transición activa para marcar ejecutado");
        }
        return alertas.isEmpty() ? "Sin alertas." : String.join(" · ", alertas);
    }

    private String accionesTexto(EjecucionExpedienteDTO expediente) {
        return hasText(expediente.getAccionesPermitidas())
                ? expediente.getAccionesPermitidas().replace(",", ", ")
                : "Sin acciones activas";
    }

    private String resolucionTexto(EjecucionExpedienteDTO expediente) {
        if (hasText(expediente.getNumeroResolucion())) {
            return expediente.getNumeroResolucion() + " · " + format(expediente.getFechaResolucion());
        }
        if (expediente.getIdResolucion() != null) {
            return "Resolución sin número visible";
        }
        return "Sin resolución registrada";
    }

    private void mostrarError(String contexto, Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String detalle = cause.getMessage() == null ? "Error no especificado." : cause.getMessage();
        JOptionPane.showMessageDialog(this, contexto + "\n" + detalle, "Ejecución", JOptionPane.WARNING_MESSAGE);
        lblEstado.setText(contexto);
    }

    private String format(LocalDate value) {
        return value == null ? "-" : DATE_FORMAT.format(value);
    }

    private String format(LocalDateTime value) {
        return value == null ? "-" : DATE_TIME_FORMAT.format(value);
    }

    private String valor(String value) {
        return hasText(value) ? value : "-";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private class EjecucionTableModel extends AbstractTableModel {

        private final String[] columns = {
            "ID", "Expediente", "Trámite", "Procedimiento", "Tipo doc.", "Tipo acta", "Nro. acta",
            "Titular", "Nro. resolución", "Fecha resolución", "Ingreso ejecución", "Etapa", "Estado",
            "Días", "Responsable", "Docs", "Asociados"
        };

        @Override
        public int getRowCount() {
            return expedientes.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            EjecucionExpedienteDTO item = expedientes.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return item.getIdExpediente();
                case 1:
                    return item.getNumeroExpediente();
                case 2:
                    return item.getNumeroTramiteDocumentario();
                case 3:
                    return item.getProcedimiento();
                case 4:
                    return item.getTipoDocumento();
                case 5:
                    return item.getTipoActa();
                case 6:
                    return item.getNumeroActa();
                case 7:
                    return item.getTitular();
                case 8:
                    return item.getNumeroResolucion();
                case 9:
                    return format(item.getFechaResolucion());
                case 10:
                    return format(item.getFechaIngresoEjecucion());
                case 11:
                    return item.getEtapaCodigo();
                case 12:
                    return item.getEstadoCodigo();
                case 13:
                    return item.getDiasEnEtapa();
                case 14:
                    return item.getResponsable();
                case 15:
                    return item.getTotalDocumentos();
                case 16:
                    return item.getTotalRelacionados();
                default:
                    return "";
            }
        }
    }

    private class EjecucionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            if (column == 11) {
                return StatusBadgeV2.forEtapa(value == null ? "" : value.toString());
            }
            if (column == 12) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
            }
            if (column == 13) {
                return StatusBadgeV2.forDias(value);
            }
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (!isSelected) {
                label.setForeground(AppV2Theme.TEXT_PRIMARY);
                label.setBackground(row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
            }
            if (value == null || value.toString().trim().isEmpty()) {
                label.setText("-");
                label.setForeground(AppV2Theme.MUTED);
            }
            return label;
        }
    }

    private static class CatalogosCarga {

        private final List<CatalogoItemDTO> resultados;
        private final List<CatalogoItemDTO> tiposObservacion;
        private final List<CatalogoItemDTO> motivosCorreccion;

        private CatalogosCarga(
                List<CatalogoItemDTO> resultados,
                List<CatalogoItemDTO> tiposObservacion,
                List<CatalogoItemDTO> motivosCorreccion) {
            this.resultados = resultados;
            this.tiposObservacion = tiposObservacion;
            this.motivosCorreccion = motivosCorreccion;
        }
    }

    private static class SimpleItem {

        private final String codigo;
        private final String nombre;

        private SimpleItem(String codigo, String nombre) {
            this.codigo = codigo == null ? "" : codigo;
            this.nombre = nombre == null || nombre.trim().isEmpty() ? this.codigo : nombre;
        }

        private String getCodigo() {
            return codigo;
        }

        private String getNombre() {
            return nombre;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }
}
