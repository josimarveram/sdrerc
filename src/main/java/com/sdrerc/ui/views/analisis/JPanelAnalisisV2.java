package com.sdrerc.ui.views.analisis;

import com.sdrerc.application.sdrercapp.AnalisisExpedienteService;
import com.sdrerc.application.sdrercapp.DocumentoAnalisisService;
import com.sdrerc.application.sdrercapp.ExpedienteRelacionadoService;
import com.sdrerc.domain.dto.sdrercapp.AnalisisExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.AnalisisRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.AnalisisResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO;
import com.sdrerc.domain.dto.sdrercapp.ObservacionAnalisisDTO;
import com.sdrerc.ui.appv2.components.AppV2ActionPanel;
import com.sdrerc.ui.appv2.components.AppV2AssociatedDocumentIconCell;
import com.sdrerc.ui.appv2.components.AppV2ExpandCollapseGlyph;
import com.sdrerc.ui.appv2.components.AppV2IconProvider;
import com.sdrerc.ui.appv2.components.AppV2NotebookToggleTab;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2SearchToolbar;
import com.sdrerc.ui.appv2.components.AppV2SideActionPanel;
import com.sdrerc.ui.appv2.components.AppV2SideSectionPanel;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.components.AppV2TablePanel;
import com.sdrerc.ui.appv2.components.AppV2TableSectionPanel;
import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.PremiumDateFieldV2;
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.helpers.EstadoExpedienteComboSupportV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import com.sdrerc.ui.views.expedienteconsola.DlgConsolaExpedienteV2;
import com.sdrerc.util.DateRangePickerSupport;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.AbstractCellEditor;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class JPanelAnalisisV2 extends JPanel {

    private static final int COL_EXPANDIR = 0;
    private static final int COL_DIAS = 1;
    private static final int COL_EXPEDIENTE = 2;
    private static final int COL_ESTADO = 9;
    private static final int COL_ASOCIADOS = 10;
    private static final int COL_ID = 11;
    private static final int PANEL_ANALISIS_ANCHO_MINIMO = 380;
    private static final int PANEL_ANALISIS_ANCHO_NORMAL = 430;
    private static final int PANEL_ANALISIS_TAB_OVERHANG = 18;
    private static final int PANEL_ANALISIS_TAB_TOP = 18;
    private static final int GROUP_STRIPE_WIDTH = 5;
    private static final int ASSOCIATED_EXPEDIENTE_INDENT = 8;
    private static final Color TABLE_SELECTION_BACKGROUND = new Color(219, 244, 249);
    private static final Color TABLE_SELECTION_FOREGROUND = AppV2Theme.TEXT_PRIMARY;
    private static final Color ASSOCIATED_ROW_BACKGROUND = new Color(238, 250, 252);
    private static final Color GRID_ACTION_ICON_BLUE = AppV2Theme.PRIMARY;
    private static final Color[] GROUP_STRIPE_COLORS = new Color[]{
        new Color(30, 59, 97),
        new Color(56, 88, 128),
        new Color(77, 132, 164),
        new Color(94, 154, 183),
        new Color(10, 118, 145),
        new Color(65, 164, 181),
        new Color(83, 101, 169),
        new Color(116, 95, 180),
        new Color(100, 117, 126),
        new Color(36, 53, 68)
    };
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AnalisisExpedienteService analisisService;
    private final DocumentoAnalisisService documentoService;
    private final ExpedienteRelacionadoService relacionadoService = new ExpedienteRelacionadoService();

    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite, titular, acta o responsable", 28);
    private final PremiumDateFieldV2 fechaSolicitudDesde = new PremiumDateFieldV2();
    private final PremiumDateFieldV2 fechaSolicitudHasta = new PremiumDateFieldV2();
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
    private final JLabel lblDocumentosAsociados = new JLabel("Sin documentos asociados.");
    private final JLabel lblFechaAnalisis = new JLabel(DATE_FORMAT.format(LocalDate.now()));

    private final JComboBox<ResultadoItem> cmbResultado = new JComboBox<ResultadoItem>();
    private final JComboBox<SimpleItem> cmbIncorporado = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbTipoDocumento = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbEstadoDocumento = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbTipoObservacion = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbMotivoNoCorresponde = new JComboBox<SimpleItem>();
    private final JTextField txtNumeroDocumentoProveido = new JTextField();
    private final JCheckBox chkReconstitucion = new JCheckBox("Reconstitución");
    private final JCheckBox chkLegitimidad = new JCheckBox("Legitimidad");
    private final JCheckBox chkMediosProbatorios = new JCheckBox("Medios probatorios");
    private final JCheckBox chkRegistrarObservacion = new JCheckBox("Registrar observación");
    private final JTextArea txtFundamento = new JTextArea(4, 22);
    private final JTextArea txtDescripcionDocumento = new JTextArea(2, 20);
    private final JTextArea txtObservacion = new JTextArea(3, 22);
    private final JTextArea txtComentarioMovimiento = new JTextArea(3, 22);
    private final AppV2NotebookToggleTab tabPanelAnalisis = new AppV2NotebookToggleTab();

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
    private final DefaultTableModel documentosAsociadosModel = new DefaultTableModel(
            new Object[]{"N° documento", "Estado", ""}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2 && puedeRecibirDocumentoAsociado(row);
        }
    };
    private final JTable documentosAsociadosTable = new AppV2Table(documentosAsociadosModel);
    private final List<AnalisisExpedienteDTO> expedientes = new ArrayList<AnalisisExpedienteDTO>();
    private final List<AnalisisTableRow> filasTabla = new ArrayList<AnalisisTableRow>();
    private final Map<Long, List<ExpedienteRelacionadoDTO>> asociadosCache = new HashMap<Long, List<ExpedienteRelacionadoDTO>>();
    private final Set<Long> principalesExpandidos = new HashSet<Long>();
    private final Set<Long> principalesCargando = new HashSet<Long>();
    private final List<ExpedienteRelacionadoDTO> documentosAsociadosPanel = new ArrayList<ExpedienteRelacionadoDTO>();
    private final MetricCardV2 cardPorRecibir = new MetricCardV2("Por recibir", "0", "Asignación / Asignado", AppV2Theme.INFO);
    private final MetricCardV2 cardEnAnalisis = new MetricCardV2("En análisis", "0", "Recibidos y observados", AppV2Theme.TEAL);
    private final MetricCardV2 cardEspeciales = new MetricCardV2("Rutas especiales", "0", "No corresponde / notificación", AppV2Theme.WARNING);
    private AppV2OperationalSplitPanel splitOperativo;
    private AppV2SideActionPanel panelAnalisis;
    private boolean panelAnalisisCerradoPorUsuario;
    private Long idExpedienteExpansionActiva;
    private Long idExpedienteDocumentosAsociados;

    private boolean cargandoCatalogos;
    private boolean busquedaInicialEjecutada;

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
        configurarDocumentosAsociadosTabla();
        configurarEventos();
        restaurarFechasBusqueda();
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
        centro.add(crearBuscador(), BorderLayout.NORTH);
        panelAnalisis = crearPanelAnalisis();
        JPanel panelAnalisisConTab = crearPanelAnalisisConTab(panelAnalisis);
        splitOperativo = new AppV2OperationalSplitPanel(
                crearBandeja(),
                panelAnalisisConTab,
                0,
                PANEL_ANALISIS_ANCHO_MINIMO + PANEL_ANALISIS_TAB_OVERHANG,
                PANEL_ANALISIS_ANCHO_NORMAL + PANEL_ANALISIS_TAB_OVERHANG);
        centro.add(splitOperativo, BorderLayout.CENTER);
        return centro;
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
        toolbar.addFilter("Fecha desde", fechaSolicitudDesde);
        toolbar.addFilter("Fecha hasta", fechaSolicitudHasta);
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

    private AppV2SideActionPanel crearPanelAnalisis() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de análisis", new Runnable() {
            @Override
            public void run() {
                cerrarPanelAnalisis();
            }
        });
        panel.setAccentColor(AppV2Theme.PRIMARY);
        tabPanelAnalisis.setAccent(AppV2Theme.PRIMARY, AppV2Theme.SOFT_BLUE);
        tabPanelAnalisis.setExpanded(false);
        tabPanelAnalisis.setToolTipText("Ampliar panel de análisis");
        tabPanelAnalisis.addActionListener(e -> alternarExpansionPanelAnalisis());
        panel.addSection(crearResumenSeleccion());
        panel.addSection(crearDocumentosAsociadosPanel());
        panel.addSection(crearFormularioAnalisis());
        panel.addSection(crearDocumentosPanel());
        panel.addSection(crearObservacionPanel());
        panel.addSection(crearComentarioMovimientoPanel());
        panel.setFooter(crearAccionesPanelAnalisis());
        return panel;
    }

    private JPanel crearPanelAnalisisConTab(final AppV2SideActionPanel panel) {
        JPanel wrapper = new JPanel(null) {
            @Override
            public void doLayout() {
                int width = getWidth();
                int height = getHeight();
                int panelX = PANEL_ANALISIS_TAB_OVERHANG;
                panel.setBounds(panelX, 0, Math.max(0, width - panelX), height);
                int tabY = Math.min(PANEL_ANALISIS_TAB_TOP, Math.max(0, height - AppV2NotebookToggleTab.DEFAULT_HEIGHT));
                tabPanelAnalisis.setBounds(
                        0,
                        tabY,
                        AppV2NotebookToggleTab.DEFAULT_WIDTH,
                        AppV2NotebookToggleTab.DEFAULT_HEIGHT);
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(panel);
        wrapper.add(tabPanelAnalisis);
        wrapper.setMinimumSize(new Dimension(
                PANEL_ANALISIS_ANCHO_MINIMO + PANEL_ANALISIS_TAB_OVERHANG,
                0));
        wrapper.setPreferredSize(new Dimension(
                PANEL_ANALISIS_ANCHO_NORMAL + PANEL_ANALISIS_TAB_OVERHANG,
                0));
        return wrapper;
    }

    private JPanel crearAccionesPanelAnalisis() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setOpaque(false);
        panel.add(btnRecibir);
        panel.add(btnRegistrarAnalisis);
        panel.add(btnEnviarVerificacion);
        panel.add(btnDerivarNotificacion);
        panel.add(btnArchivarNoCorresponde);
        panel.add(btnDerivarExterna);
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
        addRow(grid, row++, "N° Documento (Proveído)", txtNumeroDocumentoProveido);

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
        JTextArea ayuda = new JTextArea(
                "Los documentos duplicados asociados quedan disponibles en la consola del expediente. "
                        + "Agréguelos aquí solo cuando el abogado confirme que corresponde analizarlos.");
        ayuda.setEditable(false);
        ayuda.setFocusable(false);
        ayuda.setOpaque(false);
        ayuda.setLineWrap(true);
        ayuda.setWrapStyleWord(true);
        ayuda.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        ayuda.setForeground(AppV2Theme.TEXT_SECONDARY);
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

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(ayuda, BorderLayout.NORTH);
        top.add(form, BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDocumentosAsociadosPanel() {
        JPanel panel = section("Documentos asociados");
        lblDocumentosAsociados.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblDocumentosAsociados.setForeground(AppV2Theme.TEXT_SECONDARY);

        JScrollPane scroll = new JScrollPane(documentosAsociadosTable);
        scroll.setPreferredSize(new Dimension(300, 112));
        scroll.setMinimumSize(new Dimension(250, 88));
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.setOpaque(false);
        content.add(lblDocumentosAsociados, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);
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
        cmbMotivoNoCorresponde.setEnabled(false);
        txtNumeroDocumentoProveido.setPreferredSize(new Dimension(260, 34));
        txtNumeroDocumentoProveido.setToolTipText("Ingrese el número del proveído.");
        txtNumeroDocumentoProveido.setEnabled(false);
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
        table.setAutoCreateRowSorter(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        table.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        table.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        table.setGridColor(AppV2Theme.BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setDefaultRenderer(Object.class, new AnalisisRenderer());
        AppV2TableColumnSizer.applyFriendlyDefaults(table);
        AppV2TableColumnSizer.applyWidths(table, 46, 88, 185, 145, 230, 130, 130, 260, 210, 155, 160, 0);
        table.getColumnModel().getColumn(COL_EXPANDIR).setMinWidth(42);
        table.getColumnModel().getColumn(COL_EXPANDIR).setPreferredWidth(46);
        table.getColumnModel().getColumn(COL_EXPANDIR).setMaxWidth(48);
        table.getColumnModel().getColumn(COL_EXPANDIR).setCellRenderer(new ExpandirRenderer());
        tablePanel.getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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

    private void configurarDocumentosAsociadosTabla() {
        documentosAsociadosTable.setRowHeight(30);
        documentosAsociadosTable.setAutoCreateRowSorter(false);
        documentosAsociadosTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        documentosAsociadosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        documentosAsociadosTable.getTableHeader().setReorderingAllowed(false);
        documentosAsociadosTable.getTableHeader().setResizingAllowed(false);
        documentosAsociadosTable.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        documentosAsociadosTable.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        documentosAsociadosTable.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        documentosAsociadosTable.setGridColor(AppV2Theme.BORDER);
        documentosAsociadosTable.setShowVerticalLines(false);
        documentosAsociadosTable.setIntercellSpacing(new Dimension(0, 1));
        documentosAsociadosTable.setDefaultRenderer(Object.class, new DocumentoAsociadoPanelRenderer());
        documentosAsociadosTable.getColumnModel().getColumn(0).setPreferredWidth(145);
        documentosAsociadosTable.getColumnModel().getColumn(0).setMinWidth(110);
        documentosAsociadosTable.getColumnModel().getColumn(1).setPreferredWidth(125);
        documentosAsociadosTable.getColumnModel().getColumn(1).setMinWidth(105);
        documentosAsociadosTable.getColumnModel().getColumn(2).setPreferredWidth(42);
        documentosAsociadosTable.getColumnModel().getColumn(2).setMinWidth(42);
        documentosAsociadosTable.getColumnModel().getColumn(2).setMaxWidth(48);
        documentosAsociadosTable.getColumnModel().getColumn(2).setCellRenderer(new RecibirAsociadoRenderer());
        documentosAsociadosTable.getColumnModel().getColumn(2).setCellEditor(new RecibirAsociadoEditor());
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
                panelAnalisisCerradoPorUsuario = false;
                actualizarSeleccion();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                int viewColumn = table.columnAtPoint(e.getPoint());
                if (viewRow >= 0
                        && viewColumn >= 0
                        && table.convertColumnIndexToModel(viewColumn) == COL_EXPANDIR) {
                    alternarExpansionFila(table.convertRowIndexToModel(viewRow));
                    return;
                }
                if (viewRow >= 0) {
                    panelAnalisisCerradoPorUsuario = false;
                    actualizarVisibilidadPanelAnalisis();
                }
            }
        });
    }

    private void cargarFiltrosBase() {
        EstadoExpedienteComboSupportV2.cargar(
                cmbEstadoFiltro, "ANALISIS", new SimpleItem("TODOS", "Todos los estados"),
                (codigo, nombre) -> new SimpleItem(codigo, nombre),
                ex -> lblEstado.setText("No se pudieron cargar los estados de Análisis."));
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
                    if (!busquedaInicialEjecutada) {
                        busquedaInicialEjecutada = true;
                        buscar();
                    }
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
        busquedaInicialEjecutada = true;
        LocalDate desde = fechaSeleccionada(fechaSolicitudDesde);
        LocalDate hasta = fechaSeleccionada(fechaSolicitudHasta);
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            mostrarInfo("Fecha desde no puede ser mayor que Fecha hasta.");
            return;
        }
        setTrabajando(true, "Consultando expedientes asignados para análisis...");
        String texto = txtBusqueda.getText();
        String estado = obtenerCodigo(cmbEstadoFiltro);
        int limite = ((Number) spnLimite.getValue()).intValue();
        SwingWorker<List<AnalisisExpedienteDTO>, Void> worker = new SwingWorker<List<AnalisisExpedienteDTO>, Void>() {
            @Override
            protected List<AnalisisExpedienteDTO> doInBackground() throws Exception {
                return analisisService.buscarExpedientes(texto, estado, desde, hasta, limite);
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
        filasTabla.clear();
        asociadosCache.clear();
        principalesExpandidos.clear();
        principalesCargando.clear();
        idExpedienteExpansionActiva = null;
        tableModel.setRowCount(0);
        table.clearSelection();
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
            agregarFilaPrincipal(item);
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
        restaurarFechasBusqueda();
        expedientes.clear();
        filasTabla.clear();
        asociadosCache.clear();
        principalesExpandidos.clear();
        principalesCargando.clear();
        idExpedienteExpansionActiva = null;
        tableModel.setRowCount(0);
        table.clearSelection();
        tablePanel.setEmpty(true);
        limpiarFormulario();
        cardPorRecibir.setValue("0");
        cardEnAnalisis.setValue("0");
        cardEspeciales.setValue("0");
        lblEstado.setText("Filtros limpiados. Presione Buscar para consultar expedientes de análisis.");
        panelAnalisisCerradoPorUsuario = false;
        actualizarSeleccion();
    }

    private void agregarFilaPrincipal(AnalisisExpedienteDTO item) {
        AnalisisTableRow row = AnalisisTableRow.principal(item);
        filasTabla.add(row);
        tableModel.addRow(new Object[]{
            iconoExpansion(item),
            item.getDiasEnEtapa() == null ? "" : item.getDiasEnEtapa(),
            item.getNumeroExpediente(),
            formatDate(item.getFechaRecepcion()),
            item.getProcedimiento(),
            item.getTipoActa(),
            item.getNumeroActa(),
            item.getTitular(),
            valorUi(item.getResponsable()),
            DisplayNameMapperV2.estado(item.getEstadoCodigo()),
            item.getTotalRelacionados() > 0 ? item.getTotalRelacionados() + " asociados" : "Sin asociados",
            item.getIdExpediente()
        });
    }

    private void agregarFilaAsociada(AnalisisExpedienteDTO principal, ExpedienteRelacionadoDTO asociado, int index) {
        AnalisisTableRow row = AnalisisTableRow.asociada(principal, asociado);
        filasTabla.add(index, row);
        tableModel.insertRow(index, new Object[]{
            "",
            "",
            valorUi(principal.getNumeroExpediente()),
            formatDate(asociado.getFechaRecepcion()),
            procedimientoAsociado(asociado),
            valorUi(asociado.getTipoActa()),
            valorUi(asociado.getNumeroActa()),
            valorUi(asociado.getTitular()),
            valorUi(asociado.getAbogadoAsignado().isEmpty()
                    ? principal.getResponsable()
                    : asociado.getAbogadoAsignado()),
            estadoAsociado(asociado),
            textoRelacionAsociada(asociado),
            asociado.getIdExpediente()
        });
    }

    private String iconoExpansion(AnalisisExpedienteDTO item) {
        if (item == null || item.getIdExpediente() == null || item.getTotalRelacionados() <= 0) {
            return "";
        }
        if (principalesCargando.contains(item.getIdExpediente())) {
            return "loading";
        }
        return principalesExpandidos.contains(item.getIdExpediente()) ? "expanded" : "collapsed";
    }

    private String procedimientoAsociado(ExpedienteRelacionadoDTO asociado) {
        if (asociado == null) {
            return "-";
        }
        String procedimiento = asociado.getProcedimiento();
        if (procedimiento == null || procedimiento.trim().isEmpty() || pareceIdentificadorTecnico(procedimiento)) {
            return "-";
        }
        return procedimiento.trim();
    }

    private String estadoAsociado(ExpedienteRelacionadoDTO asociado) {
        if (asociado == null || asociado.getEstadoCodigo().isEmpty()) {
            return "Documento asociado";
        }
        if (asociado.isRecibidoPorAbogado()) {
            return "Recibido por abogado";
        }
        return DisplayNameMapperV2.estado(asociado.getEstadoCodigo());
    }

    private String textoRelacionAsociada(ExpedienteRelacionadoDTO asociado) {
        if (asociado == null) {
            return "Documento asociado";
        }
        if (!asociado.getTipoRelacion().isEmpty()) {
            return "Duplicado confirmado";
        }
        if (!asociado.getDescripcionRelacion().isEmpty()) {
            return "Relación confirmada";
        }
        return "Documento asociado";
    }

    private void refrescarIconoExpansion(int modelRow) {
        if (modelRow < 0 || modelRow >= filasTabla.size()) {
            return;
        }
        AnalisisTableRow row = filasTabla.get(modelRow);
        if (!row.esPrincipal()) {
            return;
        }
        tableModel.setValueAt(iconoExpansion(row.principal), modelRow, COL_EXPANDIR);
    }

    private void alternarExpansionFila(int modelRow) {
        if (modelRow < 0 || modelRow >= filasTabla.size()) {
            return;
        }
        AnalisisTableRow row = filasTabla.get(modelRow);
        if (!row.esPrincipal()
                || row.principal.getIdExpediente() == null
                || row.principal.getTotalRelacionados() <= 0) {
            return;
        }
        Long idPrincipal = row.principal.getIdExpediente();
        if (principalesExpandidos.contains(idPrincipal)
                || (idPrincipal.equals(idExpedienteExpansionActiva) && principalesCargando.contains(idPrincipal))) {
            contraerAsociados(idPrincipal);
            principalesCargando.remove(idPrincipal);
            idExpedienteExpansionActiva = null;
            refrescarIconoExpansion(indiceFilaPrincipal(idPrincipal));
            return;
        }
        contraerTodosExcepto(idPrincipal);
        idExpedienteExpansionActiva = idPrincipal;
        List<ExpedienteRelacionadoDTO> cache = asociadosCache.get(idPrincipal);
        if (cache != null) {
            insertarAsociados(modelRow, row.principal, cache);
            return;
        }
        if (principalesCargando.contains(idPrincipal)) {
            return;
        }
        principalesCargando.add(idPrincipal);
        refrescarIconoExpansion(modelRow);
        SwingWorker<List<ExpedienteRelacionadoDTO>, Void> worker = new SwingWorker<List<ExpedienteRelacionadoDTO>, Void>() {
            @Override
            protected List<ExpedienteRelacionadoDTO> doInBackground() throws Exception {
                return relacionadoService.listarAsociadosConfirmados(idPrincipal);
            }

            @Override
            protected void done() {
                principalesCargando.remove(idPrincipal);
                int principalRow = indiceFilaPrincipal(idPrincipal);
                if (principalRow < 0) {
                    return;
                }
                if (!idPrincipal.equals(idExpedienteExpansionActiva)) {
                    refrescarIconoExpansion(principalRow);
                    return;
                }
                try {
                    List<ExpedienteRelacionadoDTO> asociados = get();
                    asociadosCache.put(idPrincipal, asociados);
                    insertarAsociados(principalRow, filasTabla.get(principalRow).principal, asociados);
                } catch (Exception ex) {
                    refrescarIconoExpansion(principalRow);
                    mostrarError("No se pudieron cargar los documentos asociados.", ex);
                }
            }
        };
        worker.execute();
    }

    private void insertarAsociados(int principalRow, AnalisisExpedienteDTO principal, List<ExpedienteRelacionadoDTO> asociados) {
        if (principal == null || principal.getIdExpediente() == null || principalesExpandidos.contains(principal.getIdExpediente())) {
            return;
        }
        Long idPrincipal = principal.getIdExpediente();
        if (!idPrincipal.equals(idExpedienteExpansionActiva)) {
            return;
        }
        contraerTodosExcepto(idPrincipal);
        principalRow = indiceFilaPrincipal(idPrincipal);
        if (principalRow < 0) {
            return;
        }
        principalesExpandidos.add(idPrincipal);
        int insertAt = principalRow + 1;
        if (asociados != null) {
            for (ExpedienteRelacionadoDTO asociado : asociados) {
                agregarFilaAsociada(principal, asociado, insertAt);
                insertAt++;
            }
        }
        refrescarIconoExpansion(principalRow);
        table.revalidate();
        table.repaint();
    }

    private void contraerTodosExcepto(Long idPermitido) {
        List<Long> expandidos = new ArrayList<Long>(principalesExpandidos);
        for (Long id : expandidos) {
            if (id != null && !id.equals(idPermitido)) {
                contraerAsociados(id);
            }
        }
        List<Long> cargando = new ArrayList<Long>(principalesCargando);
        for (Long id : cargando) {
            if (id != null && !id.equals(idPermitido)) {
                principalesCargando.remove(id);
                refrescarIconoExpansion(indiceFilaPrincipal(id));
            }
        }
    }

    private void contraerAsociados(Long idPrincipal) {
        if (idPrincipal == null) {
            return;
        }
        int principalRow = indiceFilaPrincipal(idPrincipal);
        if (principalRow < 0) {
            principalesExpandidos.remove(idPrincipal);
            if (idPrincipal.equals(idExpedienteExpansionActiva)) {
                idExpedienteExpansionActiva = null;
            }
            return;
        }
        int selectedRow = obtenerModelRowSeleccionada();
        AnalisisTableRow selected = filaTabla(selectedRow);
        boolean seleccionarPrincipal = selected != null && selected.esAsociada() && idPrincipal.equals(selected.idExpedientePrincipal);
        for (int i = filasTabla.size() - 1; i > principalRow; i--) {
            AnalisisTableRow row = filasTabla.get(i);
            if (row.esAsociada() && idPrincipal.equals(row.idExpedientePrincipal)) {
                filasTabla.remove(i);
                tableModel.removeRow(i);
            }
        }
        principalesExpandidos.remove(idPrincipal);
        if (idPrincipal.equals(idExpedienteExpansionActiva)) {
            idExpedienteExpansionActiva = null;
        }
        refrescarIconoExpansion(principalRow);
        if (seleccionarPrincipal && principalRow >= 0 && principalRow < tableModel.getRowCount()) {
            int viewRow = table.convertRowIndexToView(principalRow);
            if (viewRow >= 0 && viewRow < table.getRowCount()) {
                table.setRowSelectionInterval(viewRow, viewRow);
            }
        }
        table.revalidate();
        table.repaint();
        actualizarSeleccion();
    }

    private int indiceFilaPrincipal(Long idPrincipal) {
        if (idPrincipal == null) {
            return -1;
        }
        for (int i = 0; i < filasTabla.size(); i++) {
            AnalisisTableRow row = filasTabla.get(i);
            if (row.esPrincipal() && idPrincipal.equals(row.principal.getIdExpediente())) {
                return i;
            }
        }
        return -1;
    }

    private void actualizarSeleccion() {
        AnalisisTableRow fila = obtenerFilaSeleccionada();
        AnalisisExpedienteDTO item = fila == null ? null : fila.principal;
        boolean has = fila != null;
        boolean asociado = fila != null && fila.esAsociada();
        btnVerDetalle.setEnabled(has);
        btnRecibir.setEnabled(has && !asociado && item.isRecibible());
        btnRegistrarAnalisis.setEnabled(has && !asociado && item.isRegistrable());
        btnEnviarVerificacion.setEnabled(has && !asociado && item.isEnviableVerificacion());
        btnDerivarNotificacion.setEnabled(has && !asociado && item.isDerivableNotificacionEspecial());
        btnArchivarNoCorresponde.setEnabled(has && !asociado && item.isArchivableNoCorresponde());
        btnDerivarExterna.setEnabled(has && !asociado && item.isArchivableNoCorresponde());
        actualizarVisibilidadPanelAnalisis();
        if (!has) {
            lblExpediente.setText("-");
            lblTitular.setText("-");
            lblActa.setText("-");
            lblProcedimiento.setText("-");
            lblResponsable.setText("-");
            lblEtapaEstado.setText("-");
            lblAlertas.setText("Sin alertas.");
            limpiarDocumentosAsociadosPanel("Sin documentos asociados.");
            return;
        }
        if (asociado) {
            ExpedienteRelacionadoDTO relacionado = fila.asociado;
            lblExpediente.setText("Documento asociado seleccionado");
            lblTitular.setText(valorUi(relacionado.getTitular()));
            lblActa.setText((valorUi(relacionado.getTipoActa()) + " " + valorUi(relacionado.getNumeroActa())).trim());
            lblProcedimiento.setText(procedimientoAsociado(relacionado));
            lblResponsable.setText(valorUi(relacionado.getAbogadoAsignado()));
            lblEtapaEstado.setText("Expediente principal: " + fila.numeroExpedientePrincipal());
            lblAlertas.setText(textoRelacionAsociada(relacionado) + " · Disponible para análisis");
            txtComentarioMovimiento.setText("Este documento está asociado al expediente principal y se muestra como contexto del caso.");
            cargarDocumentosAsociadosPanel(fila.principal);
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
        cargarDocumentosAsociadosPanel(item);
        if (item.isRegistrable() && documentoModel.getRowCount() == 0 && cmbEstadoDocumento.getItemCount() > 1) {
            cmbEstadoDocumento.setSelectedIndex(1);
        }
    }

    private void cerrarPanelAnalisis() {
        panelAnalisisCerradoPorUsuario = true;
        if (splitOperativo != null) {
            splitOperativo.setSideVisible(false);
        }
        tabPanelAnalisis.setExpanded(false);
        actualizarTooltipTabPanelAnalisis();
    }

    private void actualizarVisibilidadPanelAnalisis() {
        if (splitOperativo == null) {
            return;
        }
        splitOperativo.setSideVisible(obtenerFilaSeleccionada() != null && !panelAnalisisCerradoPorUsuario);
        tabPanelAnalisis.setExpanded(splitOperativo.isSideExpanded());
        actualizarTooltipTabPanelAnalisis();
    }

    private void alternarExpansionPanelAnalisis() {
        if (splitOperativo == null || !splitOperativo.isSideVisible()) {
            return;
        }
        boolean expandido = splitOperativo.toggleSideExpanded();
        tabPanelAnalisis.setExpanded(expandido);
        actualizarTooltipTabPanelAnalisis();
        revalidate();
        repaint();
    }

    private void actualizarTooltipTabPanelAnalisis() {
        boolean expandido = splitOperativo != null && splitOperativo.isSideExpanded();
        tabPanelAnalisis.setToolTipText(expandido ? "Restaurar panel de análisis" : "Ampliar panel de análisis");
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

    private void cargarDocumentosAsociadosPanel(AnalisisExpedienteDTO principal) {
        if (principal == null || principal.getIdExpediente() == null || principal.getTotalRelacionados() <= 0) {
            limpiarDocumentosAsociadosPanel("Sin documentos asociados.");
            return;
        }
        Long idPrincipal = principal.getIdExpediente();
        List<ExpedienteRelacionadoDTO> cache = asociadosCache.get(idPrincipal);
        if (cache != null) {
            actualizarDocumentosAsociadosPanel(idPrincipal, cache);
            return;
        }
        if (idPrincipal.equals(idExpedienteDocumentosAsociados)) {
            return;
        }
        idExpedienteDocumentosAsociados = idPrincipal;
        documentosAsociadosPanel.clear();
        documentosAsociadosModel.setRowCount(0);
        lblDocumentosAsociados.setForeground(AppV2Theme.TEXT_SECONDARY);
        lblDocumentosAsociados.setText("Consultando documentos asociados...");

        SwingWorker<List<ExpedienteRelacionadoDTO>, Void> worker =
                new SwingWorker<List<ExpedienteRelacionadoDTO>, Void>() {
            @Override
            protected List<ExpedienteRelacionadoDTO> doInBackground() throws Exception {
                return relacionadoService.listarAsociadosConfirmados(idPrincipal);
            }

            @Override
            protected void done() {
                if (!idPrincipal.equals(idExpedienteDocumentosAsociados)) {
                    return;
                }
                try {
                    List<ExpedienteRelacionadoDTO> asociados = get();
                    asociadosCache.put(idPrincipal, asociados);
                    actualizarDocumentosAsociadosPanel(idPrincipal, asociados);
                } catch (Exception ex) {
                    limpiarDocumentosAsociadosPanel("No se pudieron consultar los documentos asociados.");
                }
            }
        };
        worker.execute();
    }

    private void actualizarDocumentosAsociadosPanel(
            Long idPrincipal,
            List<ExpedienteRelacionadoDTO> asociados) {
        idExpedienteDocumentosAsociados = idPrincipal;
        documentosAsociadosPanel.clear();
        documentosAsociadosModel.setRowCount(0);
        int pendientes = 0;
        if (asociados != null) {
            for (ExpedienteRelacionadoDTO asociado : asociados) {
                documentosAsociadosPanel.add(asociado);
                boolean recibido = asociado.isRecibidoPorAbogado();
                if (!recibido) {
                    pendientes++;
                }
                documentosAsociadosModel.addRow(new Object[]{
                    valorUi(asociado.getNumeroDocumento()),
                    recibido ? "Recibido" : "Pendiente de recibir",
                    recibido ? "Recibido" : "Recibir"
                });
            }
        }
        if (documentosAsociadosPanel.isEmpty()) {
            lblDocumentosAsociados.setForeground(AppV2Theme.TEXT_SECONDARY);
            lblDocumentosAsociados.setText("Sin documentos asociados.");
        } else if (pendientes > 0) {
            lblDocumentosAsociados.setForeground(AppV2Theme.WARNING);
            lblDocumentosAsociados.setText(
                    pendientes + " documento(s) asociado(s) pendiente(s) de recibir.");
        } else {
            lblDocumentosAsociados.setForeground(AppV2Theme.SUCCESS);
            lblDocumentosAsociados.setText("Todos los documentos asociados fueron recibidos.");
        }
        actualizarAlertaRecepcionAsociados(idPrincipal, pendientes);
        documentosAsociadosTable.revalidate();
        documentosAsociadosTable.repaint();
    }

    private void actualizarAlertaRecepcionAsociados(Long idPrincipal, int pendientes) {
        AnalisisTableRow fila = obtenerFilaSeleccionada();
        if (fila == null || fila.getIdPrincipal() == null || !fila.getIdPrincipal().equals(idPrincipal)) {
            return;
        }
        String base = fila.esAsociada()
                ? textoRelacionAsociada(fila.asociado) + " · Disponible para análisis"
                : alertas(fila.principal);
        if (pendientes > 0) {
            base += " · " + pendientes + " documento(s) asociado(s) pendiente(s) de recibir";
        }
        lblAlertas.setText(base);
    }

    private void limpiarDocumentosAsociadosPanel(String mensaje) {
        idExpedienteDocumentosAsociados = null;
        documentosAsociadosPanel.clear();
        documentosAsociadosModel.setRowCount(0);
        lblDocumentosAsociados.setForeground(AppV2Theme.TEXT_SECONDARY);
        lblDocumentosAsociados.setText(mensaje == null || mensaje.trim().isEmpty()
                ? "Sin documentos asociados."
                : mensaje);
    }

    private boolean puedeRecibirDocumentoAsociado(int modelRow) {
        if (modelRow < 0 || modelRow >= documentosAsociadosPanel.size()) {
            return false;
        }
        ExpedienteRelacionadoDTO asociado = documentosAsociadosPanel.get(modelRow);
        return !asociado.isRecibidoPorAbogado()
                && "ASIGNACION".equalsIgnoreCase(asociado.getEtapaCodigo())
                && "ASIGNADO".equalsIgnoreCase(asociado.getEstadoCodigo())
                && analisisService.usuarioActualEsResponsable(asociado.getIdAbogadoResponsable());
    }

    private void recibirDocumentoAsociado(int modelRow) {
        if (modelRow < 0 || modelRow >= documentosAsociadosPanel.size()) {
            return;
        }
        Long idPrincipal = idExpedienteDocumentosAsociados;
        ExpedienteRelacionadoDTO asociado = documentosAsociadosPanel.get(modelRow);
        if (idPrincipal == null || asociado == null || asociado.getIdExpediente() == null) {
            mostrarInfo("Seleccione un documento asociado válido para recibir.");
            return;
        }
        if (!puedeRecibirDocumentoAsociado(modelRow)) {
            mostrarInfo("Solo el abogado responsable puede registrar la recepción.");
            return;
        }
        confirmarYEjecutar(
                "Recibir documento asociado",
                "Se recibirá el documento " + valorUi(asociado.getNumeroDocumento())
                        + " para análisis. ¿Desea continuar?",
                () -> analisisService.recibirDocumentoAsociado(
                        idPrincipal,
                        asociado.getIdExpediente(),
                        txtComentarioMovimiento.getText()));
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
        boolean noCorresponde = resultado != null && "NO_CORRESPONDE".equalsIgnoreCase(resultado.codigo);
        return new AnalisisRegistroDTO(
                item.getIdExpediente(),
                resultado == null ? "" : resultado.codigo,
                resultado == null ? "" : resultado.nombre,
                resultado == null || resultado.codigo.isEmpty() ? null : !noCorresponde,
                noCorresponde || incorporado == null || incorporado.codigo.isEmpty()
                        ? null
                        : "SI".equalsIgnoreCase(incorporado.codigo),
                !noCorresponde && chkReconstitucion.isSelected(),
                !noCorresponde && chkLegitimidad.isSelected(),
                !noCorresponde && chkMediosProbatorios.isSelected(),
                txtFundamento.getText(),
                motivo == null ? "" : motivo.codigo,
                txtNumeroDocumentoProveido.getText(),
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
        AnalisisTableRow fila = obtenerFilaSeleccionada();
        if (fila == null || fila.getIdExpediente() == null) {
            mostrarInfo("Seleccione un expediente para ver el detalle.");
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(owner, fila.getIdExpediente());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private AnalisisExpedienteDTO requerirSeleccion(String mensaje) {
        AnalisisTableRow fila = obtenerFilaSeleccionada();
        if (fila != null && fila.esAsociada()) {
            mostrarInfo("Este documento está asociado al expediente principal y se muestra como contexto del caso.");
            return null;
        }
        AnalisisExpedienteDTO item = fila == null ? null : fila.principal;
        if (item == null) {
            mostrarInfo(mensaje);
        }
        return item;
    }

    private AnalisisExpedienteDTO obtenerSeleccionado() {
        AnalisisTableRow fila = obtenerFilaSeleccionada();
        return fila == null || !fila.esPrincipal() ? null : fila.principal;
    }

    private AnalisisTableRow obtenerFilaSeleccionada() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(selected);
        return filaTabla(modelRow);
    }

    private int obtenerModelRowSeleccionada() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            return -1;
        }
        return table.convertRowIndexToModel(selected);
    }

    private AnalisisTableRow filaTabla(int modelRow) {
        if (modelRow < 0 || modelRow >= filasTabla.size()) {
            return null;
        }
        return filasTabla.get(modelRow);
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
        txtNumeroDocumentoProveido.setEnabled(noCorresponde);
        cmbIncorporado.setEnabled(!noCorresponde);
        if (noCorresponde) {
            if (cmbIncorporado.getItemCount() > 0) {
                cmbIncorporado.setSelectedIndex(0);
            }
            chkReconstitucion.setSelected(false);
            chkLegitimidad.setSelected(false);
            chkMediosProbatorios.setSelected(false);
            chkReconstitucion.setEnabled(false);
            chkLegitimidad.setEnabled(false);
            chkMediosProbatorios.setEnabled(false);
        } else {
            txtNumeroDocumentoProveido.setText("");
            actualizarChecksIncorporado();
        }
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
        txtNumeroDocumentoProveido.setText("");
        txtComentarioMovimiento.setText("");
        txtDescripcionDocumento.setText("");
        txtObservacion.setText("");
        chkRegistrarObservacion.setSelected(false);
        documentoModel.setRowCount(0);
        actualizarResultadoSeleccionado();
        actualizarObservacionHabilitada();
    }

    private void setTrabajando(boolean trabajando, String mensaje) {
        btnBuscar.setEnabled(!trabajando);
        btnLimpiar.setEnabled(!trabajando);
        btnRefrescar.setEnabled(!trabajando);
        AnalisisTableRow fila = obtenerFilaSeleccionada();
        AnalisisExpedienteDTO item = fila == null || !fila.esPrincipal() ? null : fila.principal;
        btnVerDetalle.setEnabled(!trabajando && fila != null);
        btnRecibir.setEnabled(!trabajando && item != null && item.isRecibible());
        btnRegistrarAnalisis.setEnabled(!trabajando && item != null && item.isRegistrable());
        btnEnviarVerificacion.setEnabled(!trabajando && item != null && item.isEnviableVerificacion());
        btnDerivarNotificacion.setEnabled(!trabajando && item != null && item.isDerivableNotificacionEspecial());
        btnArchivarNoCorresponde.setEnabled(!trabajando && item != null && item.isArchivableNoCorresponde());
        btnDerivarExterna.setEnabled(!trabajando && item != null && item.isArchivableNoCorresponde());
        documentosAsociadosTable.setEnabled(!trabajando);
        if (mensaje != null) {
            lblEstado.setText(mensaje);
        }
    }

    private String obtenerCodigo(JComboBox<SimpleItem> combo) {
        Object selected = combo.getSelectedItem();
        return selected instanceof SimpleItem ? ((SimpleItem) selected).codigo : "";
    }

    private void restaurarFechasBusqueda() {
        fechaSolicitudDesde.setDate(DateRangePickerSupport.defaultSearchFromDate());
        fechaSolicitudHasta.setDate(DateRangePickerSupport.defaultSearchToDate());
    }

    private static LocalDate fechaSeleccionada(PremiumDateFieldV2 field) {
        if (field == null || field.getDate() == null) {
            return null;
        }
        Date date = field.getDate();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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
                "",
                "Días",
                "Expediente",
                "Fecha solicitud",
                "Procedimiento",
                "Tipo acta",
                "Nro. acta",
                "Titular",
                "Abogado designado",
                "Estado",
                "Asociados",
                "_ID"
            }, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private class ExpandirRenderer extends JPanel implements TableCellRenderer {

        private final AppV2ExpandCollapseGlyph glyph = new AppV2ExpandCollapseGlyph();

        private ExpandirRenderer() {
            setOpaque(true);
            setLayout(new BorderLayout());
            add(glyph, BorderLayout.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            int modelRow = table.convertRowIndexToModel(row);
            AnalisisTableRow fila = filaTabla(modelRow);
            Color background = colorFondoFila(row, fila, isSelected);
            setBorder(BorderFactory.createCompoundBorder(
                    debeMostrarBarraGrupo(fila)
                            ? BorderFactory.createMatteBorder(0, GROUP_STRIPE_WIDTH, 0, 0, acentoGrupo(fila.getIdPrincipal()))
                            : BorderFactory.createEmptyBorder(0, GROUP_STRIPE_WIDTH, 0, 0),
                    BorderFactory.createEmptyBorder(0, 4, 0, 4)));
            setBackground(background);
            if (fila != null && fila.esAsociada()) {
                glyph.configure(AppV2ExpandCollapseGlyph.NONE, GRID_ACTION_ICON_BLUE, background);
                setToolTipText("Documento asociado al expediente principal.");
                return this;
            }
            if (fila != null
                    && fila.esPrincipal()
                    && fila.principal.getIdExpediente() != null
                    && fila.principal.getTotalRelacionados() > 0) {
                Long idPrincipal = fila.principal.getIdExpediente();
                int state = principalesCargando.contains(idPrincipal)
                        ? AppV2ExpandCollapseGlyph.LOADING
                        : (principalesExpandidos.contains(idPrincipal)
                        ? AppV2ExpandCollapseGlyph.COLLAPSE
                        : AppV2ExpandCollapseGlyph.EXPAND);
                glyph.configure(state, GRID_ACTION_ICON_BLUE, background);
                setToolTipText(state == AppV2ExpandCollapseGlyph.COLLAPSE
                        ? "Ocultar documentos asociados"
                        : "Ver documentos asociados");
            } else {
                glyph.configure(AppV2ExpandCollapseGlyph.NONE, AppV2Theme.TEXT_SECONDARY, background);
                setToolTipText(null);
            }
            return this;
        }
    }

    private class AnalisisRenderer extends DefaultTableCellRenderer {

        private final AppV2AssociatedDocumentIconCell documentoAsociadoCell = new AppV2AssociatedDocumentIconCell();

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            int modelColumn = table.convertColumnIndexToModel(column);
            int modelRow = table.convertRowIndexToModel(row);
            AnalisisTableRow fila = filaTabla(modelRow);
            boolean filaAsociada = fila != null && fila.esAsociada();
            Color cellBackground = colorFondoFila(row, fila, isSelected);
            if (modelColumn == COL_DIAS) {
                if (filaAsociada) {
                    documentoAsociadoCell.configure(
                            GRID_ACTION_ICON_BLUE,
                            cellBackground,
                            bordeContenidoAsociado(modelRow, 8, 8));
                    documentoAsociadoCell.setToolTipText("Documento asociado al expediente principal.");
                    return documentoAsociadoCell;
                }
                return StatusBadgeV2.forDias(value, cellBackground);
            }
            if (!isSelected && modelColumn == COL_ESTADO) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
            }
            if (!isSelected && modelColumn == COL_ASOCIADOS && value != null && !value.toString().startsWith("Sin")) {
                Color bg = filaAsociada ? ASSOCIATED_ROW_BACKGROUND : AppV2Theme.SOFT_BLUE;
                Color fg = filaAsociada ? AppV2Theme.TEXT_SECONDARY : AppV2Theme.INFO;
                return new BadgeV2(value.toString(), bg, fg);
            }
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(filaAsociada && modelColumn != COL_EXPEDIENTE
                    ? AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL)
                    : AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            String text = value == null ? "" : value.toString();
            setToolTipText(text.isEmpty() ? null : text);
            if (isSelected) {
                c.setBackground(cellBackground);
                c.setForeground(TABLE_SELECTION_FOREGROUND);
                setBorder(filaAsociada
                        ? bordeContenidoAsociado(modelRow, 8, 8)
                        : BorderFactory.createEmptyBorder(0, 8, 0, 8));
            } else if (filaAsociada) {
                setBorder(bordeContenidoAsociado(modelRow, 8, 8));
                c.setBackground(ASSOCIATED_ROW_BACKGROUND);
                c.setForeground(modelColumn == COL_EXPEDIENTE
                        ? AppV2Theme.TEXT_PRIMARY
                        : AppV2Theme.TEXT_SECONDARY);
            } else {
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                c.setBackground(cellBackground);
                c.setForeground(modelColumn == COL_EXPEDIENTE ? AppV2Theme.PRIMARY : AppV2Theme.TEXT_PRIMARY);
            }
            return c;
        }
    }

    private class DocumentoAsociadoPanelRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            int modelColumn = table.convertColumnIndexToModel(column);
            String text = value == null ? "" : value.toString();
            if (modelColumn == 1 && !isSelected) {
                boolean recibido = "Recibido".equalsIgnoreCase(text);
                return new BadgeV2(
                        text,
                        recibido ? AppV2Theme.SOFT_GREEN : AppV2Theme.SOFT_ORANGE,
                        recibido ? AppV2Theme.SUCCESS : AppV2Theme.WARNING);
            }
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            c.setBackground(isSelected ? TABLE_SELECTION_BACKGROUND : AppV2Theme.SURFACE);
            c.setForeground(AppV2Theme.TEXT_PRIMARY);
            setToolTipText(text.isEmpty() ? null : text);
            return c;
        }
    }

    private class RecibirAsociadoRenderer extends JButton implements TableCellRenderer {
        private RecibirAsociadoRenderer() {
            setText("");
            setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
            setFocusPainted(false);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            int modelRow = table.convertRowIndexToModel(row);
            ExpedienteRelacionadoDTO asociado = modelRow >= 0 && modelRow < documentosAsociadosPanel.size()
                    ? documentosAsociadosPanel.get(modelRow)
                    : null;
            boolean recibido = asociado != null && asociado.isRecibidoPorAbogado();
            boolean permitido = puedeRecibirDocumentoAsociado(modelRow);
            setIcon(AppV2IconProvider.action(
                    recibido ? AppV2IconProvider.VERIFICACION : AppV2IconProvider.BANDEJA));
            setEnabled(permitido);
            setBackground(recibido
                    ? AppV2Theme.SOFT_GREEN
                    : permitido ? AppV2Theme.SOFT_BLUE : AppV2Theme.SURFACE_ALT);
            setToolTipText(recibido
                    ? "Documento recibido por el abogado."
                    : permitido
                            ? "Recibir documento asociado."
                            : "Solo el abogado responsable puede registrar la recepción.");
            return this;
        }
    }

    private class RecibirAsociadoEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton();
        private int modelRow = -1;

        private RecibirAsociadoEditor() {
            button.setText("");
            button.setIcon(AppV2IconProvider.action(AppV2IconProvider.BANDEJA));
            button.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
            button.setFocusPainted(false);
            button.addActionListener(e -> {
                int row = modelRow;
                fireEditingStopped();
                recibirDocumentoAsociado(row);
            });
        }

        @Override
        public Object getCellEditorValue() {
            return "Recibir";
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
            modelRow = table.convertRowIndexToModel(row);
            boolean permitido = puedeRecibirDocumentoAsociado(modelRow);
            button.setEnabled(permitido);
            button.setBackground(permitido ? AppV2Theme.SOFT_BLUE : AppV2Theme.SURFACE_ALT);
            button.setToolTipText(permitido
                    ? "Recibir documento asociado."
                    : "Solo el abogado responsable puede registrar la recepción.");
            return button;
        }
    }

    private Color colorFondoFila(int viewRow, AnalisisTableRow fila, boolean selected) {
        if (selected) {
            return TABLE_SELECTION_BACKGROUND;
        }
        if (fila != null && fila.esAsociada()) {
            return ASSOCIATED_ROW_BACKGROUND;
        }
        if (fila != null && fila.esPrincipal() && principalesExpandidos.contains(fila.getIdPrincipal())) {
            return new Color(238, 250, 252);
        }
        return viewRow % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT;
    }

    private boolean debeMostrarBarraGrupo(AnalisisTableRow fila) {
        if (fila == null || fila.getIdPrincipal() == null) {
            return false;
        }
        return fila.esAsociada() || principalesExpandidos.contains(fila.getIdPrincipal());
    }

    private Color acentoGrupo(Long groupKey) {
        if (groupKey == null) {
            return GRID_ACTION_ICON_BLUE;
        }
        int index = Math.abs(groupKey.hashCode()) % GROUP_STRIPE_COLORS.length;
        return GROUP_STRIPE_COLORS[index];
    }

    private javax.swing.border.Border bordeContenidoAsociado(int modelRow, int leftPadding, int rightPadding) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(0, leftPadding, 0, rightPadding));
    }

    private static String valorUi(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private static boolean pareceIdentificadorTecnico(String value) {
        return value != null && value.trim().matches("\\d+");
    }

    private static final class AnalisisTableRow {

        private final AnalisisExpedienteDTO principal;
        private final ExpedienteRelacionadoDTO asociado;
        private final Long idExpedientePrincipal;

        private AnalisisTableRow(AnalisisExpedienteDTO principal, ExpedienteRelacionadoDTO asociado) {
            this.principal = principal;
            this.asociado = asociado;
            this.idExpedientePrincipal = principal == null ? null : principal.getIdExpediente();
        }

        private static AnalisisTableRow principal(AnalisisExpedienteDTO principal) {
            return new AnalisisTableRow(principal, null);
        }

        private static AnalisisTableRow asociada(AnalisisExpedienteDTO principal, ExpedienteRelacionadoDTO asociado) {
            return new AnalisisTableRow(principal, asociado);
        }

        private boolean esPrincipal() {
            return asociado == null && principal != null;
        }

        private boolean esAsociada() {
            return asociado != null;
        }

        private Long getIdExpediente() {
            return esAsociada() ? asociado.getIdExpediente() : principal.getIdExpediente();
        }

        private Long getIdPrincipal() {
            return idExpedientePrincipal;
        }

        private String numeroExpedientePrincipal() {
            return principal == null || principal.getNumeroExpediente().isEmpty()
                    ? "-"
                    : principal.getNumeroExpediente();
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
