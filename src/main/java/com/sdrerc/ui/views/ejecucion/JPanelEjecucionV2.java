package com.sdrerc.ui.views.ejecucion;

import com.sdrerc.application.sdrercapp.AnalisisPlantillaDocumentoService;
import com.sdrerc.application.sdrercapp.DocumentoEjecucionService;
import com.sdrerc.application.sdrercapp.EjecucionExpedienteService;
import com.sdrerc.application.sdrercapp.ExpedienteRelacionadoService;
import com.sdrerc.domain.dto.sdrercapp.AnalisisExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoEjecucionDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionReversionDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO;
import com.sdrerc.ui.appv2.components.AppV2ActionPanel;
import com.sdrerc.ui.appv2.components.AppV2AssociatedDocumentIconCell;
import com.sdrerc.ui.appv2.components.AppV2ColumnFilterSupport;
import com.sdrerc.ui.appv2.components.AppV2ExpandCollapseGlyph;
import com.sdrerc.ui.appv2.components.AppV2ExpedientePanelFactory;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.components.AppV2ResponsiveGridPanel;
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2SideActionPanel;
import com.sdrerc.ui.appv2.components.AppV2SideSectionPanel;
import com.sdrerc.ui.appv2.components.AppV2StackedSideTab;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.components.AppV2TablePanel;
import com.sdrerc.ui.appv2.components.AppV2TableSectionPanel;
import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.PlazoVisualSupportV2;
import com.sdrerc.ui.appv2.components.PremiumDateFieldV2;
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.helpers.EstadoExpedienteComboSupportV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import com.sdrerc.ui.views.expedienteconsola.DlgConsolaExpedienteV2;
import com.sdrerc.util.DateRangePickerSupport;
import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class JPanelEjecucionV2 extends JPanel {

    private enum FiltroKpi {
        TODOS,
        PENDIENTES,
        EN_REVISION,
        ERROR_MATERIAL,
        LISTOS_NOTIFICACION,
        EJECUTADOS,
        PLAZO_CRITICO
    }

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMAT_DATOS = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int PANEL_EJECUCION_ANCHO_MINIMO = 380;
    private static final int PANEL_EJECUCION_ANCHO_NORMAL = 430;
    private static final int PANEL_EJECUCION_TAB_OVERHANG = 46;
    private static final int PANEL_EJECUCION_TAB_TOP = 18;
    private static final int PANEL_EJECUCION_TAB_HEIGHT = 94;
    private static final String TAB_EJECUCION_DATOS = "DATOS";
    private static final String TAB_EJECUCION_OPERACION = "OPERACION";
    private static final String ESTADO_RESULTADO_EJECUTADO = "EJECUTADO";
    private static final int COL_EXPANDIR = 0;
    private static final int COL_DIAS = 1;
    private static final int COL_EXPEDIENTE = 2;
    private static final int COL_ESTADO = 9;
    private static final int COL_ID = 10;
    private static final int GROUP_STRIPE_WIDTH = 5;
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

    private final EjecucionExpedienteService ejecucionService;
    private final DocumentoEjecucionService documentoService;
    private final ExpedienteRelacionadoService relacionadoService = new ExpedienteRelacionadoService();

    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite/SGD, acta, titular o documento", 28);
    private final PremiumDateFieldV2 fechaSolicitudDesde = new PremiumDateFieldV2();
    private final PremiumDateFieldV2 fechaSolicitudHasta = new PremiumDateFieldV2();
    private final JComboBox<SimpleItem> cmbEstadoFiltro = new JComboBox<SimpleItem>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnRegistrarEjecucion = new JButton("Guardar Ejecución");
    private final JButton btnCancelarEjecucion = new JButton("Cancelar");
    private final JButton btnMarcarEjecutado = new JButton("Marcar ejecutado");
    private final JButton btnObservar = new JButton("Observación ejecución");
    private final JButton btnDocumentoInconsistente = new JButton("Error material");
    private final JButton btnRevertirAnalisis = new JButton("Devolver a Análisis");
    private final JButton btnDerivarNotificacion = new JButton("Derivar a Notificación");

    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar expedientes en Ejecución.");
    private final JLabel lblExpediente = new JLabel("-");
    private final JLabel lblExpedienteSgd = new JLabel("-");
    private final JLabel lblTitular = new JLabel("-");
    private final JLabel lblActa = new JLabel("-");
    private final JLabel lblProcedimiento = new JLabel("-");
    private final JLabel lblResponsable = new JLabel("-");
    private final JLabel lblEtapaEstado = new JLabel("-");
    private final JLabel lblResolucion = new JLabel("-");
    private final JLabel lblDocumentoEmitido = new JLabel("-");
    private final JLabel lblResponsableAnalisis = new JLabel("-");
    private final JLabel lblAnalisis = new JLabel("-");
    private final JLabel lblVerificacion = new JLabel("-");
    private final JLabel lblPublicacion = new JLabel("-");
    private final JLabel lblDestinoSiguiente = new JLabel("-");
    private final JLabel lblDestinoCarta = new JLabel("-");
    private final JLabel lblCartaNotificacion = new JLabel("-");
    private final JLabel lblValidacionSupervisor = new JLabel("-");
    private final JLabel lblAlertas = new JLabel("Sin alertas.");
    private final JLabel lblAcciones = new JLabel("-");

    private final BadgeV2 lblDatosDias = new BadgeV2("-", AppV2Theme.SOFT_GRAY, AppV2Theme.MUTED);
    private final JLabel lblDatosVencimiento = new JLabel("-");
    private final JLabel lblDatosExpediente = new JLabel("-");
    private final JLabel lblDatosExpedienteSgd = new JLabel("-");
    private final JLabel lblDatosTipoActa = new JLabel("-");
    private final JLabel lblDatosNumeroActa = new JLabel("-");
    private final JLabel lblDatosFechaRecepcion = new JLabel("-");
    private final JLabel lblDatosCanalIngreso = new JLabel("-");
    private final JLabel lblDatosTramiteWeb = new JLabel("-");
    private final JLabel lblDatosTipoDocumentoSolicitud = new JLabel("-");
    private final JLabel lblDatosNumeroDocumentoSolicitud = new JLabel("-");
    private final JLabel lblDatosTipoSolicitud = new JLabel("-");
    private final JLabel lblDatosGrupoFamiliar = new JLabel("-");
    private final JLabel lblDatosTitular = new JLabel("-");
    private final JLabel lblDatosTipoDocumentoTitular = new JLabel("-");
    private final JLabel lblDatosNumeroDocumentoTitular = new JLabel("-");
    private final JLabel lblDatosSolicitante = new JLabel("-");
    private final JLabel lblDatosTipoDocumentoSolicitante = new JLabel("-");
    private final JLabel lblDatosNumeroDocumentoSolicitante = new JLabel("-");
    private final JLabel lblDatosCorreo = new JLabel("-");
    private final JLabel lblDatosTelefono = new JLabel("-");
    private final JLabel lblDatosDepartamento = new JLabel("-");
    private final JLabel lblDatosProvincia = new JLabel("-");
    private final JLabel lblDatosDistrito = new JLabel("-");
    private final JLabel lblDatosDireccion = new JLabel("-");

    private final JComboBox<SimpleItem> cmbResultado = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbTipoObservacion = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbMotivoCorreccion = new JComboBox<SimpleItem>();
    private final PremiumDateFieldV2 fechaEjecucion = new PremiumDateFieldV2();
    private final JTextArea txtComentario = new JTextArea(4, 22);
    private final JTextArea txtMotivoReversion = new JTextArea(3, 22);
    private final JTextArea txtFundamentoAnalisis = new JTextArea(4, 22);
    private final JTextArea txtObservacion = new JTextArea(3, 22);
    private final AppV2StackedSideTab tabDatosEjecucion = crearTabEjecucion("Datos", new Color(230, 241, 245), new Color(57, 125, 199));
    private final AppV2StackedSideTab tabOperacionEjecucion = crearTabEjecucion("Ejecutar", new Color(224, 243, 240), AppV2Theme.PRIMARY);
    private CardLayout panelEjecucionCardsLayout;
    private JPanel panelEjecucionCards;
    private String tabEjecucionActiva = TAB_EJECUCION_DATOS;

    private final EjecucionTableModel tableModel = new EjecucionTableModel();
    private final JTable table = new AppV2Table(tableModel);
    private final AppV2TablePanel tablePanel = new AppV2TablePanel(
            table,
            "Sin expedientes para mostrar",
            "Seleccione filtros y presione Buscar.");
    private AppV2ColumnFilterSupport.Controller columnFilterSupport;
    private DocumentoEjecucionTreeGridPanelV2 documentosTreePanel;
    private final com.sdrerc.application.sdrercapp.DocumentoAnalisisService documentoAnalisisService =
            new com.sdrerc.application.sdrercapp.DocumentoAnalisisService();
    private final AnalisisPlantillaDocumentoService plantillaDocumentoService = new AnalisisPlantillaDocumentoService();
    private final List<EjecucionExpedienteDTO> expedientes = new ArrayList<EjecucionExpedienteDTO>();
    private final List<EjecucionTableRow> filasTabla = new ArrayList<EjecucionTableRow>();
    private final Map<Long, List<ExpedienteRelacionadoDTO>> asociadosCache = new HashMap<Long, List<ExpedienteRelacionadoDTO>>();
    private final Set<Long> principalesExpandidos = new HashSet<Long>();
    private final Set<Long> principalesCargando = new HashSet<Long>();
    private Long idExpedienteExpansionActiva;
    private final AtomicLong secuenciaBusqueda = new AtomicLong(0L);
    private volatile SwingWorker<?, ?> busquedaActiva;
    private boolean busquedaInicialEjecutada;

    private final MetricCardV2 cardPendientes = new MetricCardV2("Pendientes", "0", "En ejecución", AppV2Theme.INFO);
    private final MetricCardV2 cardEnRevision = new MetricCardV2("En revisión", "0", "Documento emitido", AppV2Theme.TEAL);
    private final MetricCardV2 cardErrorMaterial = new MetricCardV2("Error material", "0", "Corrección requerida", AppV2Theme.ERROR);
    private final MetricCardV2 cardListosNotificacion = new MetricCardV2("Listos para notificación", "0", "Con transición activa", AppV2Theme.PRIMARY);
    private final MetricCardV2 cardEjecutados = new MetricCardV2("Ejecutados", "0", "Resultado registrado", AppV2Theme.SUCCESS);
    private final MetricCardV2 cardPlazoCritico = new MetricCardV2("Por vencer / vencidos", "0", "Días hábiles críticos", AppV2Theme.WARNING);
    private FiltroKpi kpiActivo = FiltroKpi.TODOS;
    private AppV2OperationalSplitPanel splitOperativo;
    private AppV2SideActionPanel panelDatosEjecucion;
    private AppV2SideActionPanel panelEjecucion;
    private JTabbedPane tabsBandejasEjecucion;
    private JPanel bandejaEjecucionTab;
    private boolean panelEjecucionCerradoPorUsuario;

    public JPanelEjecucionV2() {
        this(new EjecucionExpedienteService(), new DocumentoEjecucionService());
    }

    public JPanelEjecucionV2(
            EjecucionExpedienteService ejecucionService,
            DocumentoEjecucionService documentoService) {
        this.ejecucionService = ejecucionService;
        this.documentoService = documentoService;
        setLayout(new BorderLayout(8, 8));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        add(crearCentro(), BorderLayout.CENTER);
        configurarTabla();
        configurarEventos();
        configurarKpisInteractivos();
        restaurarFechasBusqueda();
        cargarFiltrosBase();
        cargarCatalogos();
        inicializarFecha();
        actualizarSeleccion();
    }

    private JPanel crearHeader() {
        JPanel metricas = new AppV2ResponsiveGridPanel(190, 6, 12, 10);
        metricas.add(cardPendientes);
        metricas.add(cardEnRevision);
        metricas.add(cardErrorMaterial);
        metricas.add(cardListosNotificacion);
        metricas.add(cardEjecutados);
        metricas.add(cardPlazoCritico);
        return metricas;
    }

    private JPanel crearCentro() {
        JPanel contenidoPrincipal = new JPanel(new BorderLayout(4, 4));
        contenidoPrincipal.setOpaque(false);
        contenidoPrincipal.add(crearHeader(), BorderLayout.NORTH);

        JPanel contenidoOperativo = new JPanel(new BorderLayout(4, 4));
        contenidoOperativo.setOpaque(false);
        contenidoOperativo.add(crearBuscador(), BorderLayout.NORTH);
        contenidoOperativo.add(crearBandeja(), BorderLayout.CENTER);
        contenidoPrincipal.add(contenidoOperativo, BorderLayout.CENTER);

        panelDatosEjecucion = crearPanelDatosEjecucion();
        panelEjecucion = crearPanelEjecucionOperativa();
        JPanel panelConTab = crearPanelEjecucionConTab(panelDatosEjecucion, panelEjecucion);
        tabsBandejasEjecucion = new JTabbedPane();
        tabsBandejasEjecucion.setOpaque(false);
        tabsBandejasEjecucion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        tabsBandejasEjecucion.setBackground(AppV2Theme.BACKGROUND);
        tabsBandejasEjecucion.setBorder(BorderFactory.createEmptyBorder());

        bandejaEjecucionTab = new JPanel(new BorderLayout());
        bandejaEjecucionTab.setOpaque(false);
        bandejaEjecucionTab.add(contenidoPrincipal, BorderLayout.CENTER);
        tabsBandejasEjecucion.addTab("Bandeja Ejecución", bandejaEjecucionTab);

        splitOperativo = new AppV2OperationalSplitPanel(
                tabsBandejasEjecucion,
                panelConTab,
                0,
                PANEL_EJECUCION_ANCHO_MINIMO + PANEL_EJECUCION_TAB_OVERHANG,
                PANEL_EJECUCION_ANCHO_NORMAL + PANEL_EJECUCION_TAB_OVERHANG);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(splitOperativo, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel crearBuscador() {
        configurarControles();
        JPanel accionesFiltro = AppV2ActionPanel.right();
        accionesFiltro.add(btnBuscar);
        accionesFiltro.add(btnLimpiar);
        accionesFiltro.add(btnRefrescar);
        return AppV2ExpedientePanelFactory.crearPanelBusquedaEstiloRegistro(
                "Búsqueda",
                txtBusqueda,
                accionesFiltro,
                fechaSolicitudDesde,
                fechaSolicitudHasta,
                cmbEstadoFiltro,
                null,
                spnLimite);
    }

    private JPanel crearBandeja() {
        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);
        AppV2TableSectionPanel section = new AppV2TableSectionPanel(tablePanel);
        section.setStatus(lblEstado);
        return section;
    }

    private AppV2SideActionPanel crearPanelDatosEjecucion() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de Ejecución", new Runnable() {
            @Override
            public void run() {
                cerrarPanelEjecucion();
            }
        });
        panel.setAccentColor(new Color(57, 125, 199));
        AppV2ResponsiveGridPanel secciones = new AppV2ResponsiveGridPanel(320, 2, 12, 12);
        secciones.add(crearDatosPlazoEjecucion());
        secciones.add(crearDatosExpedienteEjecucion());
        secciones.add(crearDatosActaEjecucion());
        secciones.add(crearDatosSolicitudEjecucion());
        secciones.add(crearDatosTitularEjecucion());
        secciones.add(crearDatosSolicitanteEjecucion());
        secciones.add(crearDatosNotificacionEjecucion());
        panel.addSection(secciones);
        return panel;
    }

    private AppV2SideActionPanel crearPanelEjecucionOperativa() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de Ejecución", new Runnable() {
            @Override
            public void run() {
                cerrarPanelEjecucion();
            }
        });
        panel.setAccentColor(AppV2Theme.PRIMARY);
        panel.addSection(crearResultadoEjecucion());
        panel.addSection(crearDocumentosPanel());
        panel.setFooter(crearAccionesPanelEjecucion());
        return panel;
    }

    private JPanel crearPanelEjecucionConTab(
            final AppV2SideActionPanel panelDatos,
            final AppV2SideActionPanel panelOperacion) {
        JPanel wrapper = new JPanel(null) {
            @Override
            public void doLayout() {
                int width = getWidth();
                int height = getHeight();
                int panelX = PANEL_EJECUCION_TAB_OVERHANG;
                int panelWidth = Math.max(0, width - panelX);
                int[] positions = calcularPosicionesLenguetasEjecucion(2, PANEL_EJECUCION_TAB_HEIGHT, 8, height, PANEL_EJECUCION_TAB_TOP);
                tabDatosEjecucion.setBounds(0, positions[0], PANEL_EJECUCION_TAB_OVERHANG - 6, PANEL_EJECUCION_TAB_HEIGHT);
                tabOperacionEjecucion.setBounds(0, positions[1], PANEL_EJECUCION_TAB_OVERHANG - 6, PANEL_EJECUCION_TAB_HEIGHT);
                panelEjecucionCards.setBounds(panelX, 0, panelWidth, height);
            }
        };
        wrapper.setOpaque(false);
        panelEjecucionCardsLayout = new CardLayout();
        panelEjecucionCards = new JPanel(panelEjecucionCardsLayout);
        panelEjecucionCards.setOpaque(false);
        panelEjecucionCards.add(panelDatos, TAB_EJECUCION_DATOS);
        panelEjecucionCards.add(panelOperacion, TAB_EJECUCION_OPERACION);
        tabDatosEjecucion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabEjecucion(TAB_EJECUCION_DATOS);
            }
        });
        tabOperacionEjecucion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabEjecucion(TAB_EJECUCION_OPERACION);
            }
        });
        wrapper.add(tabDatosEjecucion);
        wrapper.add(tabOperacionEjecucion);
        wrapper.add(panelEjecucionCards);
        wrapper.setMinimumSize(new Dimension(
                PANEL_EJECUCION_ANCHO_MINIMO + PANEL_EJECUCION_TAB_OVERHANG,
                0));
        wrapper.setPreferredSize(new Dimension(
                PANEL_EJECUCION_ANCHO_NORMAL + PANEL_EJECUCION_TAB_OVERHANG,
                0));
        seleccionarTabEjecucion(TAB_EJECUCION_DATOS);
        return wrapper;
    }

    private AppV2StackedSideTab crearTabEjecucion(String label, Color idleColor, Color accentColor) {
        return new AppV2StackedSideTab(
                label,
                PANEL_EJECUCION_TAB_OVERHANG - 6,
                PANEL_EJECUCION_TAB_HEIGHT,
                idleColor,
                accentColor,
                accentColor.darker());
    }

    private static int[] calcularPosicionesLenguetasEjecucion(int count, int tabHeight, int gap, int containerHeight, int topMargin) {
        int[] positions = new int[Math.max(0, count)];
        int totalHeight = count * tabHeight + Math.max(0, count - 1) * gap;
        int startY = topMargin;
        if (startY + totalHeight > containerHeight - 12) {
            startY = Math.max(0, containerHeight - totalHeight - 12);
        }
        for (int i = 0; i < count; i++) {
            positions[i] = startY + i * (tabHeight + gap);
        }
        return positions;
    }

    private void seleccionarTabEjecucion(String tab) {
        if (tab == null || panelEjecucionCardsLayout == null || panelEjecucionCards == null) {
            return;
        }
        boolean mismaTab = tab.equals(tabEjecucionActiva);
        tabEjecucionActiva = tab;
        panelEjecucionCardsLayout.show(panelEjecucionCards, tab);
        if (splitOperativo != null && splitOperativo.isSideVisible() && mismaTab) {
            splitOperativo.setSideExpanded(!splitOperativo.isSideExpanded());
        }
        panelEjecucionCards.revalidate();
        panelEjecucionCards.repaint();
        actualizarLenguetasEjecucion();
    }

    private void actualizarLenguetasEjecucion() {
        boolean expandido = splitOperativo != null && splitOperativo.isSideExpanded();
        tabDatosEjecucion.setState(TAB_EJECUCION_DATOS.equals(tabEjecucionActiva), TAB_EJECUCION_DATOS.equals(tabEjecucionActiva) && expandido);
        tabOperacionEjecucion.setState(TAB_EJECUCION_OPERACION.equals(tabEjecucionActiva), TAB_EJECUCION_OPERACION.equals(tabEjecucionActiva) && expandido);
        tabDatosEjecucion.setToolTipText("Datos del expediente");
        tabOperacionEjecucion.setToolTipText("Registrar ejecución");
    }

    private JPanel crearAccionesPanelEjecucion() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setOpaque(false);
        panel.add(btnRegistrarEjecucion);
        panel.add(btnCancelarEjecucion);
        return panel;
    }

    private JPanel crearResumenSeleccion() {
        JPanel panel = section("Expediente seleccionado");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Expediente", lblExpediente);
        addRow(grid, row++, "N° expediente SGD", lblExpedienteSgd);
        addRow(grid, row++, "Titular", lblTitular);
        addRow(grid, row++, "Acta", lblActa);
        addRow(grid, row++, "Procedimiento", lblProcedimiento);
        addRow(grid, row++, "Responsable", lblResponsable);
        addRow(grid, row++, "Etapa / Estado", lblEtapaEstado);
        addRow(grid, row++, "Destino siguiente", lblDestinoSiguiente);
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
        addRow(grid, row++, "Abogado análisis", lblResponsableAnalisis);
        addRow(grid, row++, "Verificación", lblVerificacion);
        addRow(grid, row++, "Sustento", scrollText(txtFundamentoAnalisis, 80));
        addRow(grid, row, "Observación", scrollText(txtObservacion, 70));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDocumentosPanel() {
        JPanel panel = section("Documentos del expediente");
        documentosTreePanel = new DocumentoEjecucionTreeGridPanelV2();
        documentosTreePanel.setHandlers(
                documento -> {
                    EjecucionExpedienteDTO item = requerirSeleccion();
                    if (item == null) {
                        throw new IllegalArgumentException("Seleccione un expediente para guardar el documento.");
                    }
                    documentoAnalisisService.guardarDocumentoJerarquico(item.getIdExpediente(), documento);
                    return new com.sdrerc.domain.dto.sdrercapp.AnalisisResultadoDTO(
                            item.getIdExpediente(),
                            item.getNumeroExpediente(),
                            "GUARDAR_DOCUMENTO_EJECUCION",
                            item.getEtapaCodigo(),
                            item.getEstadoCodigo(),
                            "El documento fue guardado correctamente.");
                },
                this::descargarPlantillaDocumento,
                () -> {
                    EjecucionExpedienteDTO item = seleccionado();
                    if (item != null) {
                        cargarDocumentos(item.getIdExpediente());
                    }
                });
        panel.add(documentosTreePanel, BorderLayout.CENTER);
        return panel;
    }

    private void descargarPlantillaDocumento(DocumentoAnalizadoDTO documento) {
        EjecucionExpedienteDTO item = requerirSeleccion();
        if (item == null) {
            return;
        }
        if (documento == null || documento.getTipoDocumentoNombre().trim().isEmpty()) {
            mostrarInfo("Seleccione el tipo de documento para descargar la plantilla.");
            return;
        }
        AnalisisExpedienteDTO adaptado = construirAnalisisExpedienteDTO(item);

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Descargar plantilla Word");
        chooser.setSelectedFile(Paths.get(plantillaDocumentoService.nombreSugerido(adaptado, documento)).toFile());
        chooser.setFileFilter(new FileNameExtensionFilter("Documentos Word (*.docx, *.doc)", "docx", "doc"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path destino = chooser.getSelectedFile().toPath();
        if (!tieneExtensionWord(destino)) {
            destino = Paths.get(destino.toString() + ".docx");
        }
        if (destino.toFile().exists()) {
            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "El archivo ya existe. ¿Desea reemplazarlo?",
                    "Descargar plantilla",
                    JOptionPane.YES_NO_OPTION);
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }

        final Path destinoFinal = destino;
        SwingWorker<Path, Void> worker = new SwingWorker<Path, Void>() {
            @Override
            protected Path doInBackground() throws Exception {
                List<DocumentoAnalizadoDTO> documentosExpediente =
                        documentoAnalisisService.listarDocumentosAnalizados(item.getIdExpediente());
                return plantillaDocumentoService.generarDocumento(adaptado, documento, documentosExpediente, destinoFinal);
            }

            @Override
            protected void done() {
                try {
                    Path generado = get();
                    JOptionPane.showMessageDialog(
                            JPanelEjecucionV2.this,
                            "Plantilla descargada en:\n" + generado.toAbsolutePath(),
                            "Descargar plantilla",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    mostrarError("No se pudo descargar la plantilla del documento.", ex);
                }
            }
        };
        worker.execute();
    }

    private boolean tieneExtensionWord(Path path) {
        String nombre = path == null || path.getFileName() == null
                ? ""
                : path.getFileName().toString().toLowerCase();
        return nombre.endsWith(".docx") || nombre.endsWith(".doc");
    }

    private AnalisisExpedienteDTO construirAnalisisExpedienteDTO(EjecucionExpedienteDTO item) {
        return new AnalisisExpedienteDTO(
                item.getIdExpediente(),
                item.getNumeroExpediente(),
                item.getNumeroExpedienteSgd(),
                item.getNumeroTramiteDocumentario(),
                "",
                item.getProcedimiento(),
                item.getTipoDocumento(),
                item.getNumeroDocumento(),
                item.getTipoDocumentoTitular(),
                item.getNumeroDocumentoTitular(),
                item.getTipoActa(),
                item.getNumeroActa(),
                item.getTitular(),
                item.getSolicitante(),
                item.getTipoDocumentoSolicitante(),
                item.getNumeroDocumentoSolicitante(),
                item.getCorreoSolicitante(),
                item.getTelefonoSolicitante(),
                item.getDireccionSolicitante(),
                item.getDepartamentoSolicitante(),
                item.getProvinciaSolicitante(),
                item.getDistritoSolicitante(),
                item.getCanalIngreso(),
                "",
                item.getObservacionSolicitud(),
                item.isGrupoFamiliar(),
                item.getCriterioGrupoFamiliar(),
                item.getObservacionGrupoFamiliar(),
                item.getFechaRecepcion(),
                item.getDiasEnEtapa(),
                item.getFechaVencimiento(),
                null,
                null,
                item.getFechaUltimoMovimiento(),
                item.getResponsable(),
                item.getEquipo(),
                item.getEtapaCodigo(),
                item.getEstadoCodigo(),
                false,
                item.getTotalRelacionados(),
                item.getTotalDocumentos(),
                item.getResultadoAnalisis());
    }

    private JPanel crearDocumentoEmitidoPanel() {
        JPanel panel = section("Documento emitido");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Estado documental", lblDocumentoEmitido);
        addRow(grid, row, "Resolución / documento", lblResolucion);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearErrorMaterialPanel() {
        JPanel panel = section("Validación de error material");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Validación supervisor", lblValidacionSupervisor);
        addRow(grid, row, "Motivo / sustento", scrollText(txtMotivoReversion, 78));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearCartaNotificacionPanel() {
        JPanel panel = section("Carta de notificación");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Preparación", lblCartaNotificacion);
        addRow(grid, row, "Destino siguiente", lblDestinoCarta);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPublicacionPrevistaPanel() {
        JPanel panel = section("Publicación prevista");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        addRow(grid, 0, "Contexto", lblPublicacion);
        panel.add(grid, BorderLayout.CENTER);
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
        addRow(grid, row++, "Fecha ejecución", fechaEjecucion);
        addRow(grid, row++, "Comentario", scrollText(txtComentario, 88));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearResultadoEjecucion() {
        JPanel panel = section("Resultado de ejecución");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        int row = 0;
        addRow(grid, row++, "Resultado", cmbResultado);
        addRow(grid, row++, "Fecha Ejecución", fechaEjecucion);
        addRow(grid, row, "Comentario", scrollText(txtComentario, 60));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private AppV2SideSectionPanel crearDatosPlazoEjecucion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del plazo");
        section.addRow("Días", lblDatosDias);
        section.addRow("Fecha Vencimiento", lblDatosVencimiento);
        return section;
    }

    private AppV2SideSectionPanel crearDatosExpedienteEjecucion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del expediente");
        section.addRow("N° expediente", lblDatosExpediente);
        section.addRow("N° expediente SGD", lblDatosExpedienteSgd);
        return section;
    }

    private AppV2SideSectionPanel crearDatosActaEjecucion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del acta");
        section.addRow("Tipo de acta", lblDatosTipoActa);
        section.addRow("Nro. acta", lblDatosNumeroActa);
        return section;
    }

    private AppV2SideSectionPanel crearDatosSolicitudEjecucion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos de solicitud");
        section.addRow("Fecha recepción", lblDatosFechaRecepcion);
        section.addRow("Canal de ingreso", lblDatosCanalIngreso);
        section.addRow("Nro. trámite web", lblDatosTramiteWeb);
        section.addRow("Proc.Registral", lblProcedimiento);
        section.addRow("Tipo documento", lblDatosTipoDocumentoSolicitud);
        section.addRow("N° documento", lblDatosNumeroDocumentoSolicitud);
        section.addRow("Tipo de solicitud", lblDatosTipoSolicitud);
        section.addRow("Grupo familiar", lblDatosGrupoFamiliar);
        return section;
    }

    private AppV2SideSectionPanel crearDatosTitularEjecucion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del titular");
        section.addRow("Titular", lblDatosTitular);
        section.addRow("Tipo documento", lblDatosTipoDocumentoTitular);
        section.addRow("N° documento", lblDatosNumeroDocumentoTitular);
        return section;
    }

    private AppV2SideSectionPanel crearDatosSolicitanteEjecucion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del solicitante");
        section.addRow("Solicitante", lblDatosSolicitante);
        section.addRow("Tipo documento", lblDatosTipoDocumentoSolicitante);
        section.addRow("N° documento", lblDatosNumeroDocumentoSolicitante);
        return section;
    }

    private AppV2SideSectionPanel crearDatosNotificacionEjecucion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos de notificación y ubicación");
        section.addRow("Correo", lblDatosCorreo);
        section.addRow("Teléfono", lblDatosTelefono);
        section.addRow("Departamento", lblDatosDepartamento);
        section.addRow("Provincia", lblDatosProvincia);
        section.addRow("Distrito", lblDatosDistrito);
        section.addRow("Dirección", lblDatosDireccion);
        return section;
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
        fechaSolicitudDesde.setPreferredSize(new Dimension(180, 40));
        fechaSolicitudHasta.setPreferredSize(new Dimension(180, 40));
        cmbEstadoFiltro.setPreferredSize(new Dimension(220, 34));
        cmbResultado.setPreferredSize(new Dimension(250, 34));
        cmbTipoObservacion.setPreferredSize(new Dimension(250, 34));
        cmbMotivoCorreccion.setPreferredSize(new Dimension(250, 34));
        fechaEjecucion.setPreferredSize(new Dimension(250, 34));
        spnLimite.setPreferredSize(new Dimension(86, 34));
        AppV2Theme.estilizarBotonPrimario(btnBuscar);
        AppV2Theme.estilizarBotonPrimario(btnRegistrarEjecucion);
        AppV2Theme.estilizarBotonPrimario(btnMarcarEjecutado);
        AppV2Theme.estilizarBotonPrimario(btnDerivarNotificacion);
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
        table.setDefaultRenderer(Object.class, new EjecucionRenderer());
        AppV2TableColumnSizer.applyFriendlyDefaults(table);
        AppV2TableColumnSizer.applyWidths(
                table,
                46, 88, 185, 150, 145, 200, 150, 210, 260, 155, 0);
        table.getColumnModel().getColumn(COL_EXPANDIR).setMinWidth(42);
        table.getColumnModel().getColumn(COL_EXPANDIR).setPreferredWidth(46);
        table.getColumnModel().getColumn(COL_EXPANDIR).setMaxWidth(48);
        table.getColumnModel().getColumn(COL_EXPANDIR).setCellRenderer(new ExpandirRenderer());
        tablePanel.getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        columnFilterSupport = AppV2ColumnFilterSupport.install(
                "Ejecucion",
                table,
                tablePanel.getScrollPane(),
                tablePanel,
                () -> contraerTodosExcepto(null),
                COL_EXPANDIR,
                COL_ID);
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnRefrescar.addActionListener(e -> buscar());
        btnRegistrarEjecucion.addActionListener(e -> registrarEjecucion());
        btnCancelarEjecucion.addActionListener(e -> cerrarPanelEjecucion());
        btnMarcarEjecutado.addActionListener(e -> marcarEjecutado());
        btnObservar.addActionListener(e -> registrarObservacion());
        btnDocumentoInconsistente.addActionListener(e -> registrarDocumentoInconsistente());
        btnRevertirAnalisis.addActionListener(e -> revertirAnalisis());
        btnDerivarNotificacion.addActionListener(e -> derivarNotificacion());
        cmbResultado.addActionListener(e -> actualizarVisibilidadFechaEjecucion());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarSeleccion();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                int viewColumn = table.columnAtPoint(e.getPoint());
                if (viewRow >= 0
                        && viewColumn >= 0
                        && table.convertColumnIndexToModel(viewColumn) == COL_EXPANDIR) {
                    alternarExpansionFila(table.convertRowIndexToModel(viewRow));
                    return;
                }
                if (e.getClickCount() == 2 && viewRow >= 0) {
                    panelEjecucionCerradoPorUsuario = false;
                    if (splitOperativo != null) {
                        splitOperativo.setSideVisible(true);
                    }
                    actualizarSeleccion();
                }
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
                        ejecucionService.listarMotivosCorreccion(),
                        documentoAnalisisService.listarEstadosDocumento(),
                        documentoAnalisisService.listarTiposDocumentoAnalizado());
            }

            @Override
            protected void done() {
                try {
                    cargarCatalogosVista(get());
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los catálogos de ejecución.", ex);
                } finally {
                    setTrabajando(false, null);
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
        cargarSimpleItems(cmbResultado, carga.resultados, "Seleccione resultado");
        cargarSimpleItems(cmbTipoObservacion, carga.tiposObservacion, "Seleccione tipo");
        cargarSimpleItems(cmbMotivoCorreccion, carga.motivosCorreccion, "Seleccione motivo");
        if (documentosTreePanel != null) {
            documentosTreePanel.setCatalogos(carga.tipos, carga.estadosDocumento);
        }
        actualizarVisibilidadFechaEjecucion();
    }

    private void cargarSimpleItems(JComboBox<SimpleItem> combo, List<CatalogoItemDTO> items, String placeholder) {
        combo.removeAllItems();
        combo.addItem(new SimpleItem("", placeholder));
        for (CatalogoItemDTO item : items) {
            combo.addItem(new SimpleItem(item.getCodigo(), item.getNombre()));
        }
    }

    private void inicializarFecha() {
        actualizarVisibilidadFechaEjecucion();
    }

    private void actualizarVisibilidadFechaEjecucion() {
        SimpleItem resultado = (SimpleItem) cmbResultado.getSelectedItem();
        boolean esEjecutado = resultado != null && ESTADO_RESULTADO_EJECUTADO.equalsIgnoreCase(resultado.getCodigo());
        fechaEjecucion.setEnabled(esEjecutado);
        if (esEjecutado && fechaSeleccionada(fechaEjecucion) == null) {
            fechaEjecucion.setDate(new Date());
        }
    }

    private void buscar() {
        busquedaInicialEjecutada = true;
        long secuencia = secuenciaBusqueda.incrementAndGet();
        LocalDate desde = fechaSeleccionada(fechaSolicitudDesde);
        LocalDate hasta = fechaSeleccionada(fechaSolicitudHasta);
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            JOptionPane.showMessageDialog(this, "Fecha desde no puede ser mayor que Fecha hasta.", "Ejecución", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        SwingWorker<?, ?> workerAnterior = busquedaActiva;
        if (workerAnterior != null && !workerAnterior.isDone()) {
            workerAnterior.cancel(true);
        }
        setTrabajando(true, "Consultando expedientes en Ejecución...");
        String texto = txtBusqueda.getText();
        String estado = obtenerCodigo(cmbEstadoFiltro);
        int limite = ((Number) spnLimite.getValue()).intValue();
        SwingWorker<List<EjecucionExpedienteDTO>, Void> worker = new SwingWorker<List<EjecucionExpedienteDTO>, Void>() {
            @Override
            protected List<EjecucionExpedienteDTO> doInBackground() throws Exception {
                return ejecucionService.buscarExpedientes(texto, estado, desde, hasta, limite);
            }

            @Override
            protected void done() {
                try {
                    if (secuencia != secuenciaBusqueda.get()) {
                        return;
                    }
                    cargarTabla(get());
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar la bandeja de Ejecución.", ex);
                } finally {
                    if (secuencia == secuenciaBusqueda.get()) {
                        setTrabajando(false, null);
                    }
                }
            }
        };
        busquedaActiva = worker;
        worker.execute();
    }

    private void cargarTabla(List<EjecucionExpedienteDTO> items) {
        expedientes.clear();
        expedientes.addAll(items);
        List<EjecucionExpedienteDTO> visibles = filtrarKpi(items);
        filasTabla.clear();
        asociadosCache.clear();
        principalesExpandidos.clear();
        principalesCargando.clear();
        idExpedienteExpansionActiva = null;
        tableModel.setRowCount(0);
        table.clearSelection();
        for (EjecucionExpedienteDTO item : visibles) {
            agregarFilaPrincipal(item);
        }
        tablePanel.setEmpty(visibles.isEmpty());
        actualizarMetricas();
        if (visibles.isEmpty()) {
            limpiarDetalle();
        } else {
            actualizarSeleccion();
        }
        lblEstado.setText(visibles.size() + " expediente(s) en Ejecución.");
        marcarKpis();
    }

    private List<EjecucionExpedienteDTO> filtrarKpi(List<EjecucionExpedienteDTO> items) {
        List<EjecucionExpedienteDTO> filtrados = new ArrayList<EjecucionExpedienteDTO>();
        if (items == null || items.isEmpty() || kpiActivo == FiltroKpi.TODOS) {
            if (items != null) {
                filtrados.addAll(items);
            }
            return filtrados;
        }
        for (EjecucionExpedienteDTO item : items) {
            if (coincideKpi(item)) {
                filtrados.add(item);
            }
        }
        return filtrados;
    }

    private boolean coincideKpi(EjecucionExpedienteDTO item) {
        switch (kpiActivo) {
            case PENDIENTES:
                return item.isEnEjecucion();
            case EN_REVISION:
                return item.isDocumentoEmitido();
            case ERROR_MATERIAL:
                return item.isCorregible();
            case LISTOS_NOTIFICACION:
                return item.isListoParaNotificacion();
            case EJECUTADOS:
                return item.isEjecutado();
            case PLAZO_CRITICO:
                return item.getDiasEnEtapa() != null && item.getDiasEnEtapa() <= 3L;
            case TODOS:
            default:
                return true;
        }
    }

    private void configurarKpisInteractivos() {
        cardPendientes.setOnClick(() -> activarKpi(FiltroKpi.PENDIENTES));
        cardEnRevision.setOnClick(() -> activarKpi(FiltroKpi.EN_REVISION));
        cardErrorMaterial.setOnClick(() -> activarKpi(FiltroKpi.ERROR_MATERIAL));
        cardListosNotificacion.setOnClick(() -> activarKpi(FiltroKpi.LISTOS_NOTIFICACION));
        cardEjecutados.setOnClick(() -> activarKpi(FiltroKpi.EJECUTADOS));
        cardPlazoCritico.setOnClick(() -> activarKpi(FiltroKpi.PLAZO_CRITICO));
        marcarKpis();
    }

    private void activarKpi(FiltroKpi filtro) {
        kpiActivo = filtro;
        cargarTabla(new java.util.ArrayList<EjecucionExpedienteDTO>(expedientes));
        marcarKpis();
    }

    private void marcarKpis() {
        cardPendientes.setSelected(kpiActivo == FiltroKpi.PENDIENTES);
        cardEnRevision.setSelected(kpiActivo == FiltroKpi.EN_REVISION);
        cardErrorMaterial.setSelected(kpiActivo == FiltroKpi.ERROR_MATERIAL);
        cardListosNotificacion.setSelected(kpiActivo == FiltroKpi.LISTOS_NOTIFICACION);
        cardEjecutados.setSelected(kpiActivo == FiltroKpi.EJECUTADOS);
        cardPlazoCritico.setSelected(kpiActivo == FiltroKpi.PLAZO_CRITICO);
    }

    private void actualizarMetricas() {
        int pendientes = 0;
        int enRevision = 0;
        int errorMaterial = 0;
        int listosNotificacion = 0;
        int ejecutados = 0;
        int plazoCritico = 0;
        for (EjecucionExpedienteDTO expediente : expedientes) {
            if ("EN_EJECUCION".equalsIgnoreCase(expediente.getEstadoCodigo())
                    || "INDAGATORIO".equalsIgnoreCase(expediente.getEstadoCodigo())) {
                pendientes++;
            }
            if (expediente.isDocumentoEmitido()) {
                enRevision++;
            }
            if ("DOCUMENTO_INCONSISTENTE".equalsIgnoreCase(expediente.getEstadoCodigo())
                    || "REQUIERE_CORRECCION".equalsIgnoreCase(expediente.getEstadoCodigo())) {
                errorMaterial++;
            } else if ("EJECUTADO".equalsIgnoreCase(expediente.getEstadoCodigo())) {
                ejecutados++;
            }
            if (expediente.isListoParaNotificacion()) {
                listosNotificacion++;
            }
            if (expediente.getDiasEnEtapa() != null && expediente.getDiasEnEtapa() <= 3L) {
                plazoCritico++;
            }
        }
        cardPendientes.setValue(String.valueOf(pendientes));
        cardEnRevision.setValue(String.valueOf(enRevision));
        cardErrorMaterial.setValue(String.valueOf(errorMaterial));
        cardListosNotificacion.setValue(String.valueOf(listosNotificacion));
        cardEjecutados.setValue(String.valueOf(ejecutados));
        cardPlazoCritico.setValue(String.valueOf(plazoCritico));
    }

    private void agregarFilaPrincipal(EjecucionExpedienteDTO item) {
        EjecucionTableRow row = EjecucionTableRow.principal(item);
        filasTabla.add(row);
        tableModel.addRow(new Object[]{
            iconoExpansion(item),
            item.getDiasEnEtapa(),
            item.getNumeroExpediente(),
            item.getNumeroExpedienteSgd(),
            format(item.getFechaRecepcion()),
            item.getTipoResolucion().isEmpty() ? "Resolución" : item.getTipoResolucion(),
            valor(item.getResultadoAnalisis()),
            item.getResponsableAnalisis().isEmpty() ? item.getResponsable() : item.getResponsableAnalisis(),
            item.getTitular(),
            DisplayNameMapperV2.estado(item.getEstadoCodigo()),
            item.getIdExpediente()
        });
    }

    private void agregarFilaAsociada(EjecucionExpedienteDTO principal, ExpedienteRelacionadoDTO asociado, int index) {
        EjecucionTableRow row = EjecucionTableRow.asociada(principal, asociado);
        filasTabla.add(index, row);
        tableModel.insertRow(index, new Object[]{
            "",
            "",
            valor(principal.getNumeroExpediente()),
            valor(principal.getNumeroExpedienteSgd()),
            format(asociado.getFechaRecepcion()),
            "-",
            "-",
            valor(asociado.getAbogadoAsignado()),
            valor(asociado.getTitular()),
            estadoAsociado(asociado),
            asociado.getIdExpediente()
        });
    }

    private String iconoExpansion(EjecucionExpedienteDTO item) {
        if (item == null || item.getIdExpediente() == null || item.getTotalRelacionados() <= 0) {
            return "";
        }
        if (principalesCargando.contains(item.getIdExpediente())) {
            return "loading";
        }
        return principalesExpandidos.contains(item.getIdExpediente()) ? "expanded" : "collapsed";
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

    private void refrescarIconoExpansion(int modelRow) {
        if (modelRow < 0 || modelRow >= filasTabla.size()) {
            return;
        }
        EjecucionTableRow row = filasTabla.get(modelRow);
        if (!row.esPrincipal()) {
            return;
        }
        tableModel.setValueAt(iconoExpansion(row.principal), modelRow, COL_EXPANDIR);
    }

    private void alternarExpansionFila(int modelRow) {
        if (modelRow < 0 || modelRow >= filasTabla.size()) {
            return;
        }
        EjecucionTableRow row = filasTabla.get(modelRow);
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

    private void insertarAsociados(int principalRow, EjecucionExpedienteDTO principal, List<ExpedienteRelacionadoDTO> asociados) {
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
        EjecucionTableRow selected = filaTabla(selectedRow);
        boolean seleccionarPrincipal = selected != null && selected.esAsociada() && idPrincipal.equals(selected.idExpedientePrincipal);
        for (int i = filasTabla.size() - 1; i > principalRow; i--) {
            EjecucionTableRow row = filasTabla.get(i);
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
            EjecucionTableRow row = filasTabla.get(i);
            if (row.esPrincipal() && idPrincipal.equals(row.principal.getIdExpediente())) {
                return i;
            }
        }
        return -1;
    }

    private void limpiar() {
        if (columnFilterSupport != null) {
            columnFilterSupport.clearFilters();
        }
        txtBusqueda.setText("");
        restaurarFechasBusqueda();
        cmbEstadoFiltro.setSelectedIndex(0);
        spnLimite.setValue(200);
        expedientes.clear();
        filasTabla.clear();
        asociadosCache.clear();
        principalesExpandidos.clear();
        principalesCargando.clear();
        idExpedienteExpansionActiva = null;
        tableModel.setRowCount(0);
        table.clearSelection();
        tablePanel.setEmpty(true);
        actualizarMetricas();
        limpiarDetalle();
        lblEstado.setText("Filtros limpiados. Presione Buscar para consultar expedientes.");
        panelEjecucionCerradoPorUsuario = false;
        kpiActivo = FiltroKpi.TODOS;
        marcarKpis();
    }

    private void restaurarFechasBusqueda() {
        fechaSolicitudDesde.setDate(DateRangePickerSupport.defaultSearchFromDate());
        fechaSolicitudHasta.setDate(DateRangePickerSupport.defaultSearchToDate());
    }

    private static LocalDate fechaSeleccionada(PremiumDateFieldV2 field) {
        if (field == null || field.getDate() == null) {
            return null;
        }
        return field.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void actualizarSeleccion() {
        EjecucionExpedienteDTO expediente = seleccionado();
        actualizarVisibilidadPanelEjecucion();
        if (expediente == null) {
            limpiarDetalle();
            return;
        }
        lblExpediente.setText(valor(expediente.getNumeroExpediente()));
        lblExpedienteSgd.setText(valor(expediente.getNumeroExpedienteSgd()));
        lblTitular.setText(valor(expediente.getTitular()));
        lblActa.setText(valor(expediente.getTipoActa()) + " " + valor(expediente.getNumeroActa()));
        lblProcedimiento.setText(valor(expediente.getProcedimiento()));
        lblResponsable.setText(valor(expediente.getResponsable()) + " / " + valor(expediente.getEquipo()));
        lblEtapaEstado.setText(DisplayNameMapperV2.etapa(expediente.getEtapaCodigo())
                + " / " + DisplayNameMapperV2.estado(expediente.getEstadoCodigo()));
        lblResolucion.setText(resolucionTexto(expediente));
        lblDocumentoEmitido.setText(documentoEmitidoDetalle(expediente));
        lblResponsableAnalisis.setText(valor(expediente.getResponsableAnalisis()));
        lblAnalisis.setText(valor(expediente.getResultadoAnalisis()));
        lblVerificacion.setText(valor(expediente.getResultadoVerificacion()));
        lblPublicacion.setText(publicacionTexto(expediente));
        lblDestinoSiguiente.setText(destinoSiguienteTexto(expediente));
        lblDestinoCarta.setText(destinoSiguienteTexto(expediente));
        lblCartaNotificacion.setText(cartaNotificacionTexto(expediente));
        lblValidacionSupervisor.setText(validacionSupervisorTexto(expediente));
        lblAcciones.setText(accionesTexto(expediente));
        lblAlertas.setText(alertasTexto(expediente));
        txtFundamentoAnalisis.setText(expediente.getFundamentoAnalisis());
        txtObservacion.setText(expediente.getUltimaObservacion());
        cargarDatosExpedienteEjecucion(expediente);
        inicializarFecha();
        cargarDocumentos(expediente.getIdExpediente());
        actualizarAcciones(expediente);
    }

    private void limpiarDetalle() {
        cargarDatosExpedienteEjecucion(null);
        lblExpediente.setText("-");
        lblExpedienteSgd.setText("-");
        lblTitular.setText("-");
        lblActa.setText("-");
        lblProcedimiento.setText("-");
        lblResponsable.setText("-");
        lblEtapaEstado.setText("-");
        lblResolucion.setText("-");
        lblDocumentoEmitido.setText("-");
        lblResponsableAnalisis.setText("-");
        lblAnalisis.setText("-");
        lblVerificacion.setText("-");
        lblPublicacion.setText("-");
        lblDestinoSiguiente.setText("-");
        lblDestinoCarta.setText("-");
        lblCartaNotificacion.setText("-");
        lblValidacionSupervisor.setText("-");
        lblAcciones.setText("-");
        lblAlertas.setText("Sin expediente seleccionado.");
        txtComentario.setText("");
        txtMotivoReversion.setText("");
        txtFundamentoAnalisis.setText("");
        txtObservacion.setText("");
        inicializarFecha();
        if (documentosTreePanel != null) {
            documentosTreePanel.setDocumentos(null, new ArrayList<com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO>());
        }
        actualizarAcciones(null);
    }

    private void cargarDatosExpedienteEjecucion(EjecucionExpedienteDTO item) {
        if (item == null) {
            lblDatosExpediente.setText("-");
            lblDatosExpedienteSgd.setText("-");
            lblDatosTipoActa.setText("-");
            lblDatosNumeroActa.setText("-");
            lblDatosFechaRecepcion.setText("-");
            lblDatosCanalIngreso.setText("-");
            lblDatosTramiteWeb.setText("-");
            lblDatosTipoDocumentoSolicitud.setText("-");
            lblDatosNumeroDocumentoSolicitud.setText("-");
            lblDatosTipoSolicitud.setText("-");
            lblDatosGrupoFamiliar.setText("-");
            lblDatosTitular.setText("-");
            lblDatosTipoDocumentoTitular.setText("-");
            lblDatosNumeroDocumentoTitular.setText("-");
            lblDatosSolicitante.setText("-");
            lblDatosTipoDocumentoSolicitante.setText("-");
            lblDatosNumeroDocumentoSolicitante.setText("-");
            lblDatosCorreo.setText("-");
            lblDatosTelefono.setText("-");
            lblDatosDepartamento.setText("-");
            lblDatosProvincia.setText("-");
            lblDatosDistrito.setText("-");
            lblDatosDireccion.setText("-");
            lblDatosDireccion.setToolTipText(null);
            actualizarBadgeDias(lblDatosDias, null);
            lblDatosVencimiento.setText("-");
            return;
        }
        lblDatosExpediente.setText(valorUiDatos(item.getNumeroExpediente()));
        lblDatosExpedienteSgd.setText(valorUiDatos(item.getNumeroExpedienteSgd()));
        lblDatosTipoActa.setText(valorUiDatos(item.getTipoActa()));
        lblDatosNumeroActa.setText(valorUiDatos(item.getNumeroActa()));
        lblDatosFechaRecepcion.setText(formatDate(item.getFechaRecepcion()));
        lblDatosCanalIngreso.setText(valorUiDatos(item.getCanalIngreso()));
        lblDatosTramiteWeb.setText(valorUiDatos(item.getNumeroTramiteDocumentario()));
        lblDatosTipoDocumentoSolicitud.setText(valorUiDatos(item.getTipoDocumento()));
        lblDatosNumeroDocumentoSolicitud.setText(valorUiDatos(item.getNumeroDocumento()));
        lblDatosTipoSolicitud.setText(extraerValorObservacion(item.getObservacionSolicitud(), "Tipo de solicitud"));
        lblDatosGrupoFamiliar.setText(valorUiDatos(item.getGrupoFamiliarEstado()));
        lblDatosTitular.setText(valorUiDatos(item.getTitular()));
        lblDatosTipoDocumentoTitular.setText(valorUiDatos(item.getTipoDocumentoTitular()));
        lblDatosNumeroDocumentoTitular.setText(valorUiDatos(item.getNumeroDocumentoTitular()));
        lblDatosSolicitante.setText(valorUiDatos(item.getSolicitante()));
        lblDatosTipoDocumentoSolicitante.setText(valorUiDatos(item.getTipoDocumentoSolicitante()));
        lblDatosNumeroDocumentoSolicitante.setText(valorUiDatos(item.getNumeroDocumentoSolicitante()));
        lblDatosCorreo.setText(valorUiDatos(item.getCorreoSolicitante()));
        lblDatosTelefono.setText(valorUiDatos(item.getTelefonoSolicitante()));
        lblDatosDepartamento.setText(valorUiDatos(item.getDepartamentoSolicitante()));
        lblDatosProvincia.setText(valorUiDatos(item.getProvinciaSolicitante()));
        lblDatosDistrito.setText(valorUiDatos(item.getDistritoSolicitante()));
        lblDatosDireccion.setText(valorUiDatos(item.getDireccionSolicitante()));
        lblDatosDireccion.setToolTipText(valorUiDatos(item.getDireccionSolicitante()));
        actualizarBadgeDias(lblDatosDias, item.getDiasEnEtapa());
        lblDatosVencimiento.setText(formatDate(item.getFechaVencimiento()));
    }

    private static void actualizarBadgeDias(BadgeV2 badge, Long dias) {
        if (badge == null) {
            return;
        }
        if (dias == null) {
            badge.setText("-");
            badge.setBackground(AppV2Theme.SOFT_GRAY);
            badge.setForeground(AppV2Theme.MUTED);
            badge.setToolTipText(null);
            return;
        }
        PlazoVisualSupportV2.Nivel nivel = PlazoVisualSupportV2.clasificarDias(dias);
        badge.setText(String.valueOf(dias));
        badge.setBackground(PlazoVisualSupportV2.backgroundFor(nivel));
        badge.setForeground(PlazoVisualSupportV2.foregroundFor(nivel));
        if (dias < 0) {
            badge.setToolTipText("Vencido. Días hábiles restantes: " + Math.abs(dias));
        } else if (dias == 0) {
            badge.setToolTipText("Vence hoy. Días hábiles restantes: 0");
        } else {
            badge.setToolTipText("Días hábiles restantes: " + dias);
        }
    }

    private static String extraerValorObservacion(String observacion, String etiqueta) {
        if (observacion == null || etiqueta == null) {
            return "-";
        }
        String prefijo = etiqueta + ":";
        String[] partes = observacion.split("\\|");
        for (String parte : partes) {
            String texto = parte == null ? "" : parte.trim();
            if (texto.regionMatches(true, 0, prefijo, 0, prefijo.length())) {
                String valor = texto.substring(prefijo.length()).trim();
                return valor.isEmpty() ? "-" : valor;
            }
        }
        return "-";
    }

    private static String valorUiDatos(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private static String formatDate(LocalDate value) {
        return value == null ? "-" : DATE_FORMAT_DATOS.format(value);
    }

    private void cargarDocumentos(Long idExpediente) {
        if (documentosTreePanel != null) {
            documentosTreePanel.setDocumentos(idExpediente, new ArrayList<com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO>());
        }
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO> doInBackground() throws Exception {
                return documentoAnalisisService.listarDocumentosAnalizados(idExpediente);
            }

            @Override
            protected void done() {
                try {
                    if (documentosTreePanel != null) {
                        documentosTreePanel.setDocumentos(idExpediente, get());
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los documentos del expediente.", ex);
                }
            }
        };
        worker.execute();
    }

    private void actualizarAcciones(EjecucionExpedienteDTO expediente) {
        boolean seleccionado = expediente != null;
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
        final Long idExpediente = expediente.getIdExpediente();
        ejecutarOperacion("Registrando ejecución...", new Callable<EjecucionResultadoDTO>() {
            @Override
            public EjecucionResultadoDTO call() throws Exception {
                EjecucionResultadoDTO resultado = ejecucionService.registrarEjecucion(
                        crearRegistro(EjecucionExpedienteService.ACCION_INICIO_EJECUCION));
                if (!documentoAnalisisService.tieneDocumentoFinalEnDespacho(idExpediente)) {
                    return new EjecucionResultadoDTO(
                            resultado.getIdExpediente(),
                            resultado.getNumeroExpediente(),
                            resultado.getAccionCodigo(),
                            resultado.getEtapaDestinoCodigo(),
                            resultado.getEstadoDestinoCodigo(),
                            resultado.getMensaje()
                                    + " Falta registrar la carta de notificación en despacho para derivar a Notificación.");
                }
                EjecucionResultadoDTO derivado = ejecucionService.derivarNotificacion(
                        crearRegistro(EjecucionExpedienteService.ACCION_DERIVACION_NOTIFICACION));
                return new EjecucionResultadoDTO(
                        derivado.getIdExpediente(),
                        derivado.getNumeroExpediente(),
                        derivado.getAccionCodigo(),
                        derivado.getEtapaDestinoCodigo(),
                        derivado.getEstadoDestinoCodigo(),
                        "Ejecución registrada. " + derivado.getMensaje());
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
        if (expediente == null || !confirmar("Se registrará error material para " + expediente.getNumeroExpediente() + ". ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando error material...", new Callable<EjecucionResultadoDTO>() {
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
                fechaSeleccionada(fechaEjecucion),
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
        EjecucionTableRow fila = obtenerFilaSeleccionada();
        return fila == null ? null : fila.principal;
    }

    private EjecucionTableRow obtenerFilaSeleccionada() {
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

    private EjecucionTableRow filaTabla(int modelRow) {
        if (modelRow < 0 || modelRow >= filasTabla.size()) {
            return null;
        }
        return filasTabla.get(modelRow);
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
    }

    private void actualizarVisibilidadPanelEjecucion() {
        if (splitOperativo == null) {
            return;
        }
        if (!splitOperativo.isSideVisible()) {
            return;
        }
        splitOperativo.setSideVisible(seleccionado() != null && !panelEjecucionCerradoPorUsuario);
        actualizarLenguetasEjecucion();
    }

    private String alertasTexto(EjecucionExpedienteDTO expediente) {
        List<String> alertas = new ArrayList<String>();
        if (expediente.getTotalRelacionados() > 0) {
            alertas.add(expediente.getTotalRelacionados() + " expediente(s) asociado(s)");
        }
        if (expediente.isRequierePublicacion()) {
            alertas.add("Publicación prevista");
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

    private String documentoEmitidoDetalle(EjecucionExpedienteDTO expediente) {
        if (expediente == null || !expediente.isDocumentoEmitido()) {
            return "Sin documento emitido registrado";
        }
        List<String> partes = new ArrayList<String>();
        if (hasText(expediente.getTipoResolucion())) {
            partes.add(expediente.getTipoResolucion());
        }
        if (hasText(expediente.getNumeroResolucion())) {
            partes.add(expediente.getNumeroResolucion());
        }
        if (expediente.getFechaFirmaResolucion() != null) {
            partes.add("Firma: " + format(expediente.getFechaFirmaResolucion().toLocalDate()));
        }
        return partes.isEmpty() ? "Documento emitido" : String.join(" · ", partes);
    }

    private String publicacionTexto(EjecucionExpedienteDTO expediente) {
        if (expediente == null) {
            return "-";
        }
        if (!expediente.isRequierePublicacion()) {
            return "No requiere publicación";
        }
        return "Requiere publicación"
                + (expediente.getFechaPublicacion() == null
                ? " · Fecha pendiente"
                : " · " + format(expediente.getFechaPublicacion()));
    }

    private String destinoSiguienteTexto(EjecucionExpedienteDTO expediente) {
        if (expediente == null) {
            return "-";
        }
        if (expediente.isListoParaNotificacion()) {
            return "Notificación";
        }
        if (expediente.isEjecutado()) {
            return "Notificación (transición no configurada)";
        }
        if (expediente.isCorregible()) {
            return "Análisis";
        }
        return "Registrar ejecución / validar carta";
    }

    private String cartaNotificacionTexto(EjecucionExpedienteDTO expediente) {
        if (expediente == null) {
            return "-";
        }
        if (!expediente.isDocumentoEmitido()) {
            return "Pendiente de documento emitido desde Verificación";
        }
        if (expediente.isEjecutado()) {
            return expediente.hasAccion(EjecucionExpedienteService.ACCION_DERIVACION_NOTIFICACION)
                    ? "Lista para derivar a Notificación"
                    : "Ejecutada, sin transición activa a Notificación";
        }
        return "Preparar carta de notificación y validar con supervisor";
    }

    private String validacionSupervisorTexto(EjecucionExpedienteDTO expediente) {
        if (expediente == null) {
            return "-";
        }
        if (expediente.isCorregible()) {
            return "Error material / corrección pendiente";
        }
        if (expediente.isEjecutado()) {
            return "Validación de ejecución registrada";
        }
        return "Pendiente de revisión de error material";
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

    private void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Ejecución", JOptionPane.INFORMATION_MESSAGE);
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

    private class EjecucionTableModel extends DefaultTableModel {
        private EjecucionTableModel() {
            super(new Object[]{
                "",
                "Días",
                "Expediente",
                "N° expediente SGD",
                "Fecha solicitud",
                "Tipo documento",
                "Resultado",
                "Abogado",
                "Titular",
                "Estado",
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
            EjecucionTableRow fila = filaTabla(modelRow);
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

    private class EjecucionRenderer extends DefaultTableCellRenderer {

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
            EjecucionTableRow fila = filaTabla(modelRow);
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

    private Color colorFondoFila(int viewRow, EjecucionTableRow fila, boolean selected) {
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

    private boolean debeMostrarBarraGrupo(EjecucionTableRow fila) {
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

    private static final class EjecucionTableRow {

        private final EjecucionExpedienteDTO principal;
        private final ExpedienteRelacionadoDTO asociado;
        private final Long idExpedientePrincipal;

        private EjecucionTableRow(EjecucionExpedienteDTO principal, ExpedienteRelacionadoDTO asociado) {
            this.principal = principal;
            this.asociado = asociado;
            this.idExpedientePrincipal = principal == null ? null : principal.getIdExpediente();
        }

        private static EjecucionTableRow principal(EjecucionExpedienteDTO principal) {
            return new EjecucionTableRow(principal, null);
        }

        private static EjecucionTableRow asociada(EjecucionExpedienteDTO principal, ExpedienteRelacionadoDTO asociado) {
            return new EjecucionTableRow(principal, asociado);
        }

        private boolean esPrincipal() {
            return asociado == null && principal != null;
        }

        private boolean esAsociada() {
            return asociado != null;
        }

        private Long getIdPrincipal() {
            return idExpedientePrincipal;
        }
    }

    private static class CatalogosCarga {

        private final List<CatalogoItemDTO> resultados;
        private final List<CatalogoItemDTO> tiposObservacion;
        private final List<CatalogoItemDTO> motivosCorreccion;
        private final List<CatalogoItemDTO> estadosDocumento;
        private final List<CatalogoItemDTO> tipos;

        private CatalogosCarga(
                List<CatalogoItemDTO> resultados,
                List<CatalogoItemDTO> tiposObservacion,
                List<CatalogoItemDTO> motivosCorreccion,
                List<CatalogoItemDTO> estadosDocumento,
                List<CatalogoItemDTO> tipos) {
            this.resultados = resultados;
            this.tiposObservacion = tiposObservacion;
            this.motivosCorreccion = motivosCorreccion;
            this.estadosDocumento = estadosDocumento;
            this.tipos = tipos;
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
