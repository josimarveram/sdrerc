package com.sdrerc.ui.views.verificacion;

import com.sdrerc.application.sdrercapp.DocumentoVerificacionService;
import com.sdrerc.application.sdrercapp.ExpedienteRelacionadoService;
import com.sdrerc.application.sdrercapp.FirmaEmisionExpedienteService;
import com.sdrerc.application.sdrercapp.VerificacionExpedienteService;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoVerificacionDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO;
import com.sdrerc.domain.dto.sdrercapp.FirmaEmisionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.FirmaEmisionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.ObservacionVerificacionDTO;
import com.sdrerc.domain.dto.sdrercapp.VerificacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.VerificacionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.VerificacionResultadoDTO;
import com.sdrerc.ui.appv2.components.AppV2ActionPanel;
import com.sdrerc.ui.appv2.components.AppV2ColumnFilterSupport;
import com.sdrerc.ui.appv2.components.AppV2AssociatedDocumentIconCell;
import com.sdrerc.ui.appv2.components.AppV2ExpandCollapseGlyph;
import com.sdrerc.ui.appv2.components.AppV2IconProvider;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.components.AppV2ResponsiveGridPanel;
import com.sdrerc.ui.appv2.components.AppV2ExpedientePanelFactory;
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.CardLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
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
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.CellRendererPane;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultCellEditor;
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
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class JPanelVerificacionV2 extends JPanel {

    private enum FiltroKpi {
        TODOS,
        PENDIENTES,
        OBSERVADOS,
        INCONSISTENTES,
        APROBADOS,
        EMITIDOS,
        PLAZO_CRITICO
    }

    private static final int COL_EXPANDIR = 0;
    private static final int COL_DIAS = 1;
    private static final int COL_EXPEDIENTE = 2;
    private static final int COL_RESULTADO = 8;
    private static final int COL_ESTADO = 9;
    private static final int COL_ASOCIADOS = 10;
    private static final int COL_ID = 11;
    private static final int PANEL_VERIFICACION_ANCHO_MINIMO = 380;
    private static final int PANEL_VERIFICACION_ANCHO_NORMAL = 430;
    private static final int PANEL_VERIFICACION_TAB_OVERHANG = 46;
    private static final int PANEL_VERIFICACION_TAB_TOP = 18;
    private static final int PANEL_VERIFICACION_TAB_HEIGHT = 94;
    private static final int GROUP_STRIPE_WIDTH = 5;
    private static final String TAB_VERIFICACION_DATOS = "DATOS";
    private static final String TAB_VERIFICACION_OPERACION = "OPERACION";
    private static final String ETAPA_ANALISIS = "ANALISIS";
    private static final String ETAPA_VERIFICACION = "VERIFICACION";
    private static final String ETAPA_FIRMA_EMISION = "FIRMA_EMISION";
    private static final String ESTADO_PARA_FIRMA = "PARA_FIRMA";
    private static final String ESTADO_FIRMADO = "FIRMADO";
    private static final String ESTADO_EMITIDO = "EMITIDO";
    private static final String ESTADO_RESOLUCION_NUMERADA = "RESOLUCION_NUMERADA";
    private static final Color TABLE_SELECTION_BACKGROUND = new Color(219, 244, 249);
    private static final Color TABLE_SELECTION_FOREGROUND = AppV2Theme.TEXT_PRIMARY;
    private static final Color ASSOCIATED_ROW_BACKGROUND = new Color(238, 250, 252);
    private static final Color DOCUMENTO_ANALISIS_BACKGROUND = new Color(238, 247, 252);
    private static final Color DOCUMENTO_ASIGNACION_BACKGROUND = new Color(239, 249, 246);
    private static final Color DOCUMENTO_NOTIFICACION_BACKGROUND = new Color(248, 245, 253);
    private static final Color DOCUMENTO_ACTION_BACKGROUND = new Color(247, 249, 252);
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

    private final VerificacionExpedienteService verificacionService;
    private final DocumentoVerificacionService documentoService;
    private final FirmaEmisionExpedienteService firmaEmisionService;
    private final ExpedienteRelacionadoService relacionadoService = new ExpedienteRelacionadoService();

    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite/SGD, acta, titular o documento", 28);
    private final PremiumDateFieldV2 fechaSolicitudDesde = new PremiumDateFieldV2();
    private final PremiumDateFieldV2 fechaSolicitudHasta = new PremiumDateFieldV2();
    private final JComboBox<SimpleItem> cmbEstadoFiltro = new JComboBox<SimpleItem>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnRegistrarVerificacion = new JButton("Registrar Verificación");
    private final JButton btnCancelarVerificacion = new JButton("Cancelar");
    private final JButton btnAprobar = new JButton("Aprobar verificación");
    private final JButton btnEnviarFirma = new JButton("Preparar documento emitido");
    private final JButton btnObservar = new JButton("Requiere corrección");
    private final JButton btnDocumentoInconsistente = new JButton("Documento inconsistente");
    private final JButton btnDevolverAnalisis = new JButton("Devolver a análisis");
    private final JButton btnRegistrarFirma = new JButton("Registrar conformidad");
    private final JButton btnRegistrarEmision = new JButton("Marcar como emitido");
    private final JButton btnRegistrarNumero = new JButton("Registrar número de documento");
    private final JButton btnEnviarEjecucion = new JButton("Enviar a ejecución");
    private final JButton btnEnviarNotificacion = new JButton("Enviar a notificación");

    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar expedientes en Verificación.");
    private final JLabel lblExpediente = new JLabel("-");
    private final JLabel lblExpedienteSgd = new JLabel("-");
    private final JLabel lblTitular = new JLabel("-");
    private final JLabel lblActa = new JLabel("-");
    private final JLabel lblProcedimiento = new JLabel("-");
    private final JLabel lblResponsable = new JLabel("-");
    private final JLabel lblResponsableAnalisis = new JLabel("-");
    private final JLabel lblAbogadoAnalisisDestinoEtiqueta = label("Abogado");
    private final JLabel lblAbogadoAnalisisDestinoValor = new JLabel("-");
    private final JLabel lblEtapaEstado = new JLabel("-");
    private final JLabel lblAnalisis = new JLabel("-");
    private final JLabel lblAlertas = new JLabel("Sin alertas.");
    private final JLabel lblEstadoDocumentoEmitido = new JLabel("-");
    private final JLabel lblDestinoSiguiente = new JLabel("-");
    private final JLabel lblResponsableFirma = new JLabel("-");
    private final JLabel lblRequierePublicacion = new JLabel("-");
    private final JLabel lblFechaPublicacion = new JLabel("-");
    private final JLabel lblFechaVerificacion = new JLabel(DATE_FORMAT.format(LocalDate.now()));
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

    private final JComboBox<ResultadoItem> cmbResultado = new JComboBox<ResultadoItem>();
    private final JComboBox<ResultadoItem> cmbResultadoVerificacion = new JComboBox<ResultadoItem>(
            new ResultadoItem[]{
                    new ResultadoItem("APROBADO", "Aprobado"),
                    new ResultadoItem("OBSERVADO", "Observado")
            });
    private final JComboBox<SimpleItem> cmbTipoObservacion = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbMotivoCorreccion = new JComboBox<SimpleItem>();
    private final JTextArea txtComentario = new JTextArea(4, 22);
    private final JTextArea txtObservacion = new JTextArea(4, 22);
    private final JTextArea txtFundamentoAnalisis = new JTextArea(4, 22);
    private final JComboBox<SimpleItem> cmbTipoResolucion = new JComboBox<SimpleItem>();
    private final JTextField txtNumeroResolucion = new JTextField(12);
    private final JTextField txtFechaFirma = new JTextField(10);
    private final JTextField txtFechaEmision = new JTextField(10);
    private final JTextField txtFechaResolucion = new JTextField(10);
    private final JTextArea txtComentarioFirma = new JTextArea(3, 22);

    private final VerificacionTableModel tableModel = new VerificacionTableModel();
    private final JTable table = new AppV2Table(tableModel);
    private final AppV2TablePanel tablePanel = new AppV2TablePanel(
            table,
            "Sin expedientes para mostrar",
            "Seleccione filtros y presione Buscar.");
    private AppV2ColumnFilterSupport.Controller columnFilterSupport;
    private DocumentoVerificacionTreeGridPanelV2 documentosTreePanel;
    private final JComboBox<EquipoItem> cmbEquipoDestino = new JComboBox<EquipoItem>();
    private final JComboBox<UsuarioItem> cmbUsuarioDestino = new JComboBox<UsuarioItem>();
    private boolean cargandoCombosDestino;
    private final List<VerificacionExpedienteDTO> expedientes = new ArrayList<VerificacionExpedienteDTO>();
    private final List<VerificacionTableRow> filasTabla = new ArrayList<VerificacionTableRow>();
    private final Map<Long, List<ExpedienteRelacionadoDTO>> asociadosCache = new HashMap<Long, List<ExpedienteRelacionadoDTO>>();
    private final Set<Long> principalesExpandidos = new HashSet<Long>();
    private final Set<Long> principalesCargando = new HashSet<Long>();
    private final AtomicLong secuenciaBusqueda = new AtomicLong(0L);
    private volatile SwingWorker<?, ?> busquedaActiva;

    private final MetricCardV2 cardEnVerificacion = new MetricCardV2("Pendientes", "0", "En revisión documental", AppV2Theme.INFO);
    private final MetricCardV2 cardObservados = new MetricCardV2("Observados", "0", "Requieren corrección", AppV2Theme.WARNING);
    private final MetricCardV2 cardInconsistentes = new MetricCardV2("Inconsistentes", "0", "Documento inconsistente", AppV2Theme.ERROR);
    private final MetricCardV2 cardAprobados = new MetricCardV2("Aprobados", "0", "Listos para emisión", AppV2Theme.SUCCESS);
    private final MetricCardV2 cardEmitidos = new MetricCardV2("Emitidos", "0", "Documento emitido", AppV2Theme.PRIMARY);
    private final MetricCardV2 cardPlazoCritico = new MetricCardV2("Plazo crítico", "0", "Por vencer o vencidos", AppV2Theme.WARNING);
    private FiltroKpi kpiActivo = FiltroKpi.TODOS;
    private AppV2OperationalSplitPanel splitOperativo;
    private AppV2SideActionPanel panelVerificacion;
    private AppV2SideActionPanel panelDatosVerificacion;
    private JTabbedPane tabsBandejasVerificacion;
    private JPanel bandejaVerificacionTab;
    private AppV2StackedSideTab tabDatosVerificacion = crearTabVerificacion("Datos", new Color(230, 241, 245), new Color(57, 125, 199));
    private AppV2StackedSideTab tabOperacionVerificacion = crearTabVerificacion("Verificar", new Color(224, 243, 240), new Color(10, 118, 145));
    private CardLayout panelVerificacionCardsLayout;
    private JPanel panelVerificacionCards;
    private String tabVerificacionActiva = TAB_VERIFICACION_DATOS;
    private boolean panelVerificacionCerradoPorUsuario;
    private Long idExpedienteExpansionActiva;

    private boolean cargandoCatalogos;
    private boolean busquedaInicialEjecutada;

    public JPanelVerificacionV2() {
        this(new VerificacionExpedienteService(), new DocumentoVerificacionService(), new FirmaEmisionExpedienteService());
    }

    public JPanelVerificacionV2(
            VerificacionExpedienteService verificacionService,
            DocumentoVerificacionService documentoService) {
        this(verificacionService, documentoService, new FirmaEmisionExpedienteService());
    }

    public JPanelVerificacionV2(
            VerificacionExpedienteService verificacionService,
            DocumentoVerificacionService documentoService,
            FirmaEmisionExpedienteService firmaEmisionService) {
        this.verificacionService = verificacionService;
        this.documentoService = documentoService;
        this.firmaEmisionService = firmaEmisionService;
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
        cargarEquiposDestino();
        actualizarSeleccion();
    }

    private JPanel crearHeader() {
        JPanel metricas = new AppV2ResponsiveGridPanel(190, 6, 12, 0);
        metricas.add(cardEnVerificacion);
        metricas.add(cardObservados);
        metricas.add(cardInconsistentes);
        metricas.add(cardAprobados);
        metricas.add(cardEmitidos);
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

        panelDatosVerificacion = crearPanelDatosVerificacion();
        panelVerificacion = crearPanelVerificacionOperativa();
        JPanel panelVerificacionConTab = crearPanelVerificacionConTab(
                panelDatosVerificacion,
                panelVerificacion);
        tabsBandejasVerificacion = new JTabbedPane();
        tabsBandejasVerificacion.setOpaque(false);
        tabsBandejasVerificacion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        tabsBandejasVerificacion.setBackground(AppV2Theme.BACKGROUND);
        tabsBandejasVerificacion.setBorder(BorderFactory.createEmptyBorder());

        bandejaVerificacionTab = new JPanel(new BorderLayout());
        bandejaVerificacionTab.setOpaque(false);
        bandejaVerificacionTab.add(contenidoPrincipal, BorderLayout.CENTER);
        tabsBandejasVerificacion.addTab("Bandeja Verificación", bandejaVerificacionTab);

        splitOperativo = new AppV2OperationalSplitPanel(
                tabsBandejasVerificacion,
                panelVerificacionConTab,
                0,
                PANEL_VERIFICACION_ANCHO_MINIMO + PANEL_VERIFICACION_TAB_OVERHANG,
                PANEL_VERIFICACION_ANCHO_NORMAL + PANEL_VERIFICACION_TAB_OVERHANG);
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

    private AppV2SideActionPanel crearPanelDatosVerificacion() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de Verificación", new Runnable() {
            @Override
            public void run() {
                cerrarPanelVerificacion();
            }
        });
        panel.setAccentColor(new Color(57, 125, 199));
        AppV2ResponsiveGridPanel secciones = new AppV2ResponsiveGridPanel(320, 2, 12, 12);
        secciones.add(crearDatosPlazoVerificacion());
        secciones.add(crearDatosExpedienteVerificacion());
        secciones.add(crearDatosActaVerificacion());
        secciones.add(crearDatosSolicitudVerificacion());
        secciones.add(crearDatosTitularVerificacion());
        secciones.add(crearDatosSolicitanteVerificacion());
        secciones.add(crearDatosNotificacionVerificacion());
        panel.addSection(secciones);
        return panel;
    }

    private AppV2SideActionPanel crearPanelVerificacionOperativa() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de Verificación", new Runnable() {
            @Override
            public void run() {
                cerrarPanelVerificacion();
            }
        });
        panel.setAccentColor(new Color(10, 118, 145));
        panel.addSection(crearDocumentosPanel());
        panel.addSection(crearResultadoVerificacion());
        panel.addSection(crearDestinoVerificacion());
        panel.setFooter(crearAccionesPanelVerificacion());
        return panel;
    }

    private JPanel crearResultadoVerificacion() {
        JPanel panel = section("Resultado de verificación");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        int row = 0;
        addRow(grid, row++, "Resultado", cmbResultadoVerificacion);
        addRow(grid, row, "Comentario", scrollText(txtComentario, 60));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelVerificacionConTab(
            final AppV2SideActionPanel panelDatos,
            final AppV2SideActionPanel panelOperacion) {
        JPanel wrapper = new JPanel(null) {
            @Override
            public void doLayout() {
                int width = getWidth();
                int height = getHeight();
                int panelX = PANEL_VERIFICACION_TAB_OVERHANG;
                int panelWidth = Math.max(0, width - panelX);
                int[] positions = calcularPosicionesLenguetasVerificacion(2, PANEL_VERIFICACION_TAB_HEIGHT, 8, height, PANEL_VERIFICACION_TAB_TOP);
                tabDatosVerificacion.setBounds(0, positions[0], PANEL_VERIFICACION_TAB_OVERHANG - 6, PANEL_VERIFICACION_TAB_HEIGHT);
                tabOperacionVerificacion.setBounds(0, positions[1], PANEL_VERIFICACION_TAB_OVERHANG - 6, PANEL_VERIFICACION_TAB_HEIGHT);
                panelVerificacionCards.setBounds(panelX, 0, panelWidth, height);
            }
        };
        wrapper.setOpaque(false);
        panelVerificacionCardsLayout = new CardLayout();
        panelVerificacionCards = new JPanel(panelVerificacionCardsLayout);
        panelVerificacionCards.setOpaque(false);
        panelVerificacionCards.add(panelDatos, TAB_VERIFICACION_DATOS);
        panelVerificacionCards.add(panelOperacion, TAB_VERIFICACION_OPERACION);
        tabDatosVerificacion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabVerificacion(TAB_VERIFICACION_DATOS);
            }
        });
        tabOperacionVerificacion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabVerificacion(TAB_VERIFICACION_OPERACION);
            }
        });
        wrapper.add(tabDatosVerificacion);
        wrapper.add(tabOperacionVerificacion);
        wrapper.add(panelVerificacionCards);
        wrapper.setMinimumSize(new Dimension(
                PANEL_VERIFICACION_ANCHO_MINIMO + PANEL_VERIFICACION_TAB_OVERHANG,
                0));
        wrapper.setPreferredSize(new Dimension(
                PANEL_VERIFICACION_ANCHO_NORMAL + PANEL_VERIFICACION_TAB_OVERHANG,
                0));
        seleccionarTabVerificacion(TAB_VERIFICACION_DATOS);
        return wrapper;
    }

    private AppV2StackedSideTab crearTabVerificacion(String label, Color idleColor, Color accentColor) {
        return new AppV2StackedSideTab(
                label,
                PANEL_VERIFICACION_TAB_OVERHANG - 6,
                PANEL_VERIFICACION_TAB_HEIGHT,
                idleColor,
                accentColor,
                accentColor.darker());
    }

    private static int[] calcularPosicionesLenguetasVerificacion(int count, int tabHeight, int gap, int containerHeight, int topMargin) {
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

    private JPanel crearAccionesPanelVerificacion() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setOpaque(false);
        panel.add(btnRegistrarVerificacion);
        panel.add(btnCancelarVerificacion);
        return panel;
    }

    private AppV2SideSectionPanel crearDatosPlazoVerificacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del plazo");
        section.addRow("Días", lblDatosDias);
        section.addRow("Fecha Vencimiento", lblDatosVencimiento);
        return section;
    }

    private AppV2SideSectionPanel crearDatosExpedienteVerificacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del expediente");
        section.addRow("N° expediente", lblDatosExpediente);
        section.addRow("N° expediente SGD", lblDatosExpedienteSgd);
        return section;
    }

    private AppV2SideSectionPanel crearDatosActaVerificacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del acta");
        section.addRow("Tipo de acta", lblDatosTipoActa);
        section.addRow("Nro. acta", lblDatosNumeroActa);
        return section;
    }

    private AppV2SideSectionPanel crearDatosSolicitudVerificacion() {
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

    private AppV2SideSectionPanel crearDatosTitularVerificacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del titular");
        section.addRow("Titular", lblDatosTitular);
        section.addRow("Tipo documento", lblDatosTipoDocumentoTitular);
        section.addRow("N° documento", lblDatosNumeroDocumentoTitular);
        return section;
    }

    private AppV2SideSectionPanel crearDatosSolicitanteVerificacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del solicitante");
        section.addRow("Solicitante", lblDatosSolicitante);
        section.addRow("Tipo documento", lblDatosTipoDocumentoSolicitante);
        section.addRow("N° documento", lblDatosNumeroDocumentoSolicitante);
        return section;
    }

    private AppV2SideSectionPanel crearDatosNotificacionVerificacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos de notificación y ubicación");
        section.addRow("Correo", lblDatosCorreo);
        section.addRow("Teléfono", lblDatosTelefono);
        section.addRow("Departamento", lblDatosDepartamento);
        section.addRow("Provincia", lblDatosProvincia);
        section.addRow("Distrito", lblDatosDistrito);
        section.addRow("Dirección", lblDatosDireccion);
        return section;
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
        addRow(grid, row, "Alertas", lblAlertas);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearAnalisisPrevio() {
        JPanel panel = section("Análisis recibido");
        txtFundamentoAnalisis.setEditable(false);
        txtFundamentoAnalisis.setBackground(AppV2Theme.SURFACE_ALT);
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Resultado", lblAnalisis);
        addRow(grid, row++, "Responsable análisis", lblResponsableAnalisis);
        addRow(grid, row, "Sustento", scrollText(txtFundamentoAnalisis, 96));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDocumentosPanel() {
        JPanel panel = section("Documentos verificados");
        documentosTreePanel = new DocumentoVerificacionTreeGridPanelV2();
        documentosTreePanel.setHandlers(
                (idDocumento, estadoCodigo, detalleObservacion, fechaEmision, numeroDocumento) -> {
                    VerificacionExpedienteDTO item = requerirSeleccion("Seleccione un expediente para guardar el documento.");
                    if (item == null) {
                        throw new IllegalArgumentException("Seleccione un expediente para guardar el documento.");
                    }
                    documentoService.actualizarEstadoDocumentoAnalizado(
                            item.getIdExpediente(),
                            idDocumento,
                            estadoCodigo,
                            detalleObservacion,
                            fechaEmision,
                            numeroDocumento);
                },
                () -> {
                    VerificacionExpedienteDTO item = obtenerSeleccionado();
                    if (item != null) {
                        cargarDocumentos(item);
                    }
                });
        panel.add(documentosTreePanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDestinoVerificacion() {
        JPanel panel = section("Destino operativo");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        int row = 0;
        addRow(grid, row++, lblAbogadoAnalisisDestinoEtiqueta, lblAbogadoAnalisisDestinoValor);
        addRow(grid, row++, "Equipo destino", cmbEquipoDestino);
        addRow(grid, row, "Usuario destino", cmbUsuarioDestino);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private void actualizarAbogadoAnalisisDestino(VerificacionExpedienteDTO item) {
        String equipo = item == null ? "" : (item.getEquipoAnalisis().isEmpty() ? item.getEquipo() : item.getEquipoAnalisis());
        String abogado = item == null ? "" : (item.getResponsableAnalisis().isEmpty() ? item.getResponsable() : item.getResponsableAnalisis());
        lblAbogadoAnalisisDestinoEtiqueta.setText(equipo.isEmpty() ? "Abogado" : "Abogado " + equipo);
        lblAbogadoAnalisisDestinoValor.setText(abogado.isEmpty() ? "-" : abogado);
    }

    private JPanel crearFormularioVerificacion() {
        JPanel panel = section("Registro de verificación");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Resultado", cmbResultado);
        addRow(grid, row++, "Tipo observación", cmbTipoObservacion);
        addRow(grid, row++, "Motivo corrección", cmbMotivoCorreccion);
        addRow(grid, row++, "Fecha", lblFechaVerificacion);
        addRow(grid, row++, "Sustento", scrollText(txtComentario, 86));
        addRow(grid, row, "Observación", scrollText(txtObservacion, 86));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormularioFirmaEmision() {
        JPanel panel = section("Documento emitido");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Estado documental", lblEstadoDocumentoEmitido);
        addRow(grid, row++, "Validación requerida", lblResponsableFirma);
        addRow(grid, row++, "Tipo documento", cmbTipoResolucion);
        addRow(grid, row++, "N° resolución / documento", txtNumeroResolucion);
        addRow(grid, row++, "Fecha firma", txtFechaFirma);
        addRow(grid, row++, "Fecha emisión", txtFechaEmision);
        addRow(grid, row++, "Fecha resolución / documento", txtFechaResolucion);
        addRow(grid, row, "Comentario", scrollText(txtComentarioFirma, 76));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPublicacionPrevista() {
        JPanel panel = section("Publicación prevista");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Requiere publicación", lblRequierePublicacion);
        addRow(grid, row, "Fecha de publicación", lblFechaPublicacion);
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
        scroll.setPreferredSize(new Dimension(235, height));
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        return scroll;
    }

    private void addRow(JPanel target, int row, String label, Component component) {
        addRow(target, row, label(label), component);
    }

    private void addRow(JPanel target, int row, JLabel labelComponent, Component component) {
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
        target.add(labelComponent, gbcLabel);
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
        cmbResultado.setPreferredSize(new Dimension(235, 34));
        cmbTipoObservacion.setPreferredSize(new Dimension(235, 34));
        cmbMotivoCorreccion.setPreferredSize(new Dimension(235, 34));
        cmbTipoResolucion.setPreferredSize(new Dimension(235, 34));
        cmbEquipoDestino.setPreferredSize(new Dimension(235, 34));
        cmbUsuarioDestino.setPreferredSize(new Dimension(235, 34));
        txtNumeroResolucion.setPreferredSize(new Dimension(235, 34));
        txtFechaFirma.setPreferredSize(new Dimension(235, 34));
        txtFechaEmision.setPreferredSize(new Dimension(235, 34));
        txtFechaResolucion.setPreferredSize(new Dimension(235, 34));
        spnLimite.setPreferredSize(new Dimension(86, 34));
        AppV2Theme.estilizarBotonPrimario(btnBuscar);
        AppV2Theme.estilizarBotonPrimario(btnRegistrarVerificacion);
        AppV2Theme.estilizarBotonPrimario(btnAprobar);
        AppV2Theme.estilizarBotonPrimario(btnEnviarFirma);
        AppV2Theme.estilizarBotonPrimario(btnDevolverAnalisis);
        AppV2Theme.estilizarBotonPrimario(btnRegistrarFirma);
        AppV2Theme.estilizarBotonPrimario(btnRegistrarEmision);
        AppV2Theme.estilizarBotonPrimario(btnRegistrarNumero);
        AppV2Theme.estilizarBotonPrimario(btnEnviarEjecucion);
        AppV2Theme.estilizarBotonPrimario(btnEnviarNotificacion);
        configurarDatosPanelLabels();
    }

    private void configurarDatosPanelLabels() {
        JLabel[] labels = {
            lblDatosVencimiento,
            lblDatosExpediente,
            lblDatosExpedienteSgd,
            lblDatosTipoActa,
            lblDatosNumeroActa,
            lblDatosFechaRecepcion,
            lblDatosCanalIngreso,
            lblDatosTramiteWeb,
            lblDatosTipoDocumentoSolicitud,
            lblDatosNumeroDocumentoSolicitud,
            lblDatosTipoSolicitud,
            lblDatosGrupoFamiliar,
            lblDatosTitular,
            lblDatosTipoDocumentoTitular,
            lblDatosNumeroDocumentoTitular,
            lblDatosSolicitante,
            lblDatosTipoDocumentoSolicitante,
            lblDatosNumeroDocumentoSolicitante,
            lblDatosCorreo,
            lblDatosTelefono,
            lblDatosDepartamento,
            lblDatosProvincia,
            lblDatosDistrito,
            lblDatosDireccion
        };
        for (JLabel label : labels) {
            label.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            label.setForeground(AppV2Theme.TEXT_PRIMARY);
        }
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
        table.setDefaultRenderer(Object.class, new VerificacionRenderer());
        AppV2TableColumnSizer.applyFriendlyDefaults(table);
        AppV2TableColumnSizer.applyWidths(
                table,
                46, 88, 185, 150, 145, 200, 260, 210, 150, 155, 160, 0);
        table.getColumnModel().getColumn(COL_EXPANDIR).setMinWidth(42);
        table.getColumnModel().getColumn(COL_EXPANDIR).setPreferredWidth(46);
        table.getColumnModel().getColumn(COL_EXPANDIR).setMaxWidth(48);
        table.getColumnModel().getColumn(COL_EXPANDIR).setCellRenderer(new ExpandirRenderer());
        tablePanel.getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        columnFilterSupport = AppV2ColumnFilterSupport.install(
                "Verificacion",
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
        btnRegistrarVerificacion.addActionListener(e -> registrarVerificacion());
        btnCancelarVerificacion.addActionListener(e -> cerrarPanelVerificacion());
        btnAprobar.addActionListener(e -> accionRapida("APROBACION_VERIFICACION"));
        btnEnviarFirma.addActionListener(e -> enviarFirma());
        btnObservar.addActionListener(e -> accionRapida("REGISTRO_OBSERVACION_VERIFICACION"));
        btnDocumentoInconsistente.addActionListener(e -> accionRapida("REVERSION_ESTADO_DOCUMENTO"));
        btnDevolverAnalisis.addActionListener(e -> devolverAnalisis());
        btnRegistrarFirma.addActionListener(e -> registrarFirma());
        btnRegistrarEmision.addActionListener(e -> registrarEmision());
        btnRegistrarNumero.addActionListener(e -> registrarNumeroResolucion());
        btnEnviarEjecucion.addActionListener(e -> enviarEjecucion());
        btnEnviarNotificacion.addActionListener(e -> enviarNotificacion());
        cmbResultado.addActionListener(e -> actualizarResultadoSeleccionado());
        cmbEquipoDestino.addActionListener(e -> {
            if (!cargandoCombosDestino) {
                cargarUsuariosDestino();
            }
        });
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
                    panelVerificacionCerradoPorUsuario = false;
                    if (splitOperativo != null) {
                        splitOperativo.setSideVisible(true);
                    }
                    actualizarVisibilidadPanelVerificacion();
                }
            }
        });
    }

    private void cargarFiltrosBase() {
        EstadoExpedienteComboSupportV2.cargarPorEtapas(
                cmbEstadoFiltro, new SimpleItem("TODOS", "Todos los estados"),
                (codigo, nombre) -> new SimpleItem(codigo, nombre),
                ex -> lblEstado.setText("No se pudieron cargar los estados de Verificación."),
                ETAPA_ANALISIS,
                ETAPA_VERIFICACION,
                ETAPA_FIRMA_EMISION);
    }

    private void cargarCatalogos() {
        cargandoCatalogos = true;
        setTrabajando(true, "Cargando catálogos de Verificación...");
        SwingWorker<CatalogosCarga, Void> worker = new SwingWorker<CatalogosCarga, Void>() {
            @Override
            protected CatalogosCarga doInBackground() throws Exception {
                return new CatalogosCarga(
                        verificacionService.listarResultadosVerificacion(),
                        verificacionService.listarTiposObservacion(),
                        verificacionService.listarMotivosCorreccion(),
                        documentoService.listarEstadosDocumento(),
                        firmaEmisionService.listarTiposResolucion());
            }

            @Override
            protected void done() {
                try {
                    cargarCatalogosVista(get());
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los catálogos de Verificación.", ex);
                } finally {
                    cargandoCatalogos = false;
                    setTrabajando(false, null);
                    actualizarResultadoSeleccionado();
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
        cargarSimpleItems(cmbTipoObservacion, carga.tiposObservacion, "Seleccione tipo");
        cargarSimpleItems(cmbMotivoCorreccion, carga.motivosCorreccion, "Seleccione motivo");
        cargarSimpleItems(cmbTipoResolucion, carga.tiposResolucion, "Seleccione tipo");
        if (documentosTreePanel != null) {
            documentosTreePanel.setEstadosDocumento(carga.estadosDocumento);
        }
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
        long secuencia = secuenciaBusqueda.incrementAndGet();
        LocalDate desde = fechaSeleccionada(fechaSolicitudDesde);
        LocalDate hasta = fechaSeleccionada(fechaSolicitudHasta);
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            mostrarInfo("Fecha desde no puede ser mayor que Fecha hasta.");
            return;
        }
        SwingWorker<?, ?> workerAnterior = busquedaActiva;
        if (workerAnterior != null && !workerAnterior.isDone()) {
            workerAnterior.cancel(true);
        }
        setTrabajando(true, "Consultando expedientes en Verificación...");
        String texto = txtBusqueda.getText();
        String estado = obtenerCodigo(cmbEstadoFiltro);
        int limite = ((Number) spnLimite.getValue()).intValue();
        SwingWorker<List<VerificacionExpedienteDTO>, Void> worker = new SwingWorker<List<VerificacionExpedienteDTO>, Void>() {
            @Override
            protected List<VerificacionExpedienteDTO> doInBackground() throws Exception {
                return verificacionService.buscarExpedientes(texto, estado, desde, hasta, limite);
            }

            @Override
            protected void done() {
                try {
                    if (secuencia != secuenciaBusqueda.get()) {
                        return;
                    }
                    cargarTabla(get());
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar la bandeja de verificación.", ex);
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

    private void cargarTabla(List<VerificacionExpedienteDTO> items) {
        expedientes.clear();
        expedientes.addAll(items);
        List<VerificacionExpedienteDTO> visibles = filtrarKpi(items);
        filasTabla.clear();
        asociadosCache.clear();
        principalesExpandidos.clear();
        principalesCargando.clear();
        idExpedienteExpansionActiva = null;
        tableModel.setRowCount(0);
        table.clearSelection();
        int enVerificacion = 0;
        int observados = 0;
        int inconsistentes = 0;
        int aprobados = 0;
        int emitidos = 0;
        int plazoCritico = 0;
        for (VerificacionExpedienteDTO item : expedientes) {
            if (item.isRegistrableVerificacion()) {
                enVerificacion++;
            }
            if ("REQUIERE_CORRECCION".equalsIgnoreCase(item.getEstadoCodigo())) {
                observados++;
            }
            if ("DOCUMENTO_INCONSISTENTE".equalsIgnoreCase(item.getEstadoCodigo())) {
                inconsistentes++;
            }
            if (item.isEnviableFirma()) {
                aprobados++;
            }
            if (item.isDocumentoEmitido()) {
                emitidos++;
            }
            if (item.getDiasEnEtapa() != null && item.getDiasEnEtapa() <= 3L) {
                plazoCritico++;
            }
        }
        cardEnVerificacion.setValue(String.valueOf(enVerificacion));
        cardObservados.setValue(String.valueOf(observados));
        cardInconsistentes.setValue(String.valueOf(inconsistentes));
        cardAprobados.setValue(String.valueOf(aprobados));
        cardEmitidos.setValue(String.valueOf(emitidos));
        cardPlazoCritico.setValue(String.valueOf(plazoCritico));
        marcarKpis();
        cargarTablaVisible(visibles);
        lblEstado.setText(items.isEmpty()
                ? "No se encontraron expedientes en Verificación."
                : visibles.size() + " expediente(s) encontrados.");
        tablePanel.setEmpty(visibles.isEmpty());
        actualizarSeleccion();
    }

    private void cargarTablaVisible(List<VerificacionExpedienteDTO> items) {
        if (items == null) {
            return;
        }
        for (VerificacionExpedienteDTO item : items) {
            agregarFilaPrincipal(item);
        }
    }

    private List<VerificacionExpedienteDTO> filtrarKpi(List<VerificacionExpedienteDTO> items) {
        List<VerificacionExpedienteDTO> filtrados = new ArrayList<VerificacionExpedienteDTO>();
        if (items == null || items.isEmpty() || kpiActivo == FiltroKpi.TODOS) {
            if (items != null) {
                filtrados.addAll(items);
            }
            return filtrados;
        }
        for (VerificacionExpedienteDTO item : items) {
            if (coincideKpi(item)) {
                filtrados.add(item);
            }
        }
        return filtrados;
    }

    private boolean coincideKpi(VerificacionExpedienteDTO item) {
        switch (kpiActivo) {
            case PENDIENTES:
                return item.isRegistrableVerificacion();
            case OBSERVADOS:
                return "REQUIERE_CORRECCION".equalsIgnoreCase(item.getEstadoCodigo());
            case INCONSISTENTES:
                return "DOCUMENTO_INCONSISTENTE".equalsIgnoreCase(item.getEstadoCodigo());
            case APROBADOS:
                return item.isEnviableFirma();
            case EMITIDOS:
                return item.isDocumentoEmitido();
            case PLAZO_CRITICO:
                return item.getDiasEnEtapa() != null && item.getDiasEnEtapa() <= 3L;
            case TODOS:
            default:
                return true;
        }
    }

    private void configurarKpisInteractivos() {
        cardEnVerificacion.setOnClick(() -> activarKpi(FiltroKpi.PENDIENTES));
        cardObservados.setOnClick(() -> activarKpi(FiltroKpi.OBSERVADOS));
        cardInconsistentes.setOnClick(() -> activarKpi(FiltroKpi.INCONSISTENTES));
        cardAprobados.setOnClick(() -> activarKpi(FiltroKpi.APROBADOS));
        cardEmitidos.setOnClick(() -> activarKpi(FiltroKpi.EMITIDOS));
        cardPlazoCritico.setOnClick(() -> activarKpi(FiltroKpi.PLAZO_CRITICO));
        marcarKpis();
    }

    private void activarKpi(FiltroKpi filtro) {
        kpiActivo = filtro;
        cargarTabla(new java.util.ArrayList<VerificacionExpedienteDTO>(expedientes));
        marcarKpis();
    }

    private void marcarKpis() {
        cardEnVerificacion.setSelected(kpiActivo == FiltroKpi.PENDIENTES);
        cardObservados.setSelected(kpiActivo == FiltroKpi.OBSERVADOS);
        cardInconsistentes.setSelected(kpiActivo == FiltroKpi.INCONSISTENTES);
        cardAprobados.setSelected(kpiActivo == FiltroKpi.APROBADOS);
        cardEmitidos.setSelected(kpiActivo == FiltroKpi.EMITIDOS);
        cardPlazoCritico.setSelected(kpiActivo == FiltroKpi.PLAZO_CRITICO);
    }

    private void agregarFilaPrincipal(VerificacionExpedienteDTO item) {
        VerificacionTableRow row = VerificacionTableRow.principal(item);
        filasTabla.add(row);
        tableModel.addRow(new Object[]{
            iconoExpansion(item),
            item.getDiasEnEtapa() == null ? "" : item.getDiasEnEtapa(),
            item.getNumeroExpediente(),
            item.getNumeroExpedienteSgd(),
            formatDate(item.getFechaRecepcion()),
            item.getTipoDocumentoPendiente().isEmpty() ? "-" : item.getTipoDocumentoPendiente(),
            item.getTitular(),
            item.getResponsableAnalisis().isEmpty() ? item.getResponsable() : item.getResponsableAnalisis(),
            item.getUltimoResultadoAnalisis().isEmpty() ? "-" : item.getUltimoResultadoAnalisis(),
            DisplayNameMapperV2.estado(item.getEstadoCodigo()),
            item.getTotalRelacionados() > 0 ? item.getTotalRelacionados() + " asociado(s)" : "Sin asociados",
            item.getIdExpediente()
        });
    }

    private void agregarFilaAsociada(VerificacionExpedienteDTO principal, ExpedienteRelacionadoDTO asociado, int index) {
        VerificacionTableRow row = VerificacionTableRow.asociada(principal, asociado);
        filasTabla.add(index, row);
        tableModel.insertRow(index, new Object[]{
            "",
            "",
            valorUi(principal.getNumeroExpediente()),
            valorUi(principal.getNumeroExpedienteSgd()),
            formatDate(asociado.getFechaRecepcion()),
            "-",
            valorUi(asociado.getTitular()),
            valorUi(asociado.getAbogadoAsignado()),
            "-",
            estadoAsociado(asociado),
            textoRelacionAsociada(asociado),
            asociado.getIdExpediente()
        });
    }

    private String iconoExpansion(VerificacionExpedienteDTO item) {
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
        VerificacionTableRow row = filasTabla.get(modelRow);
        if (!row.esPrincipal()) {
            return;
        }
        tableModel.setValueAt(iconoExpansion(row.principal), modelRow, COL_EXPANDIR);
    }

    private void alternarExpansionFila(int modelRow) {
        if (modelRow < 0 || modelRow >= filasTabla.size()) {
            return;
        }
        VerificacionTableRow row = filasTabla.get(modelRow);
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

    private void insertarAsociados(int principalRow, VerificacionExpedienteDTO principal, List<ExpedienteRelacionadoDTO> asociados) {
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
        VerificacionTableRow selected = filaTabla(selectedRow);
        boolean seleccionarPrincipal = selected != null && selected.esAsociada() && idPrincipal.equals(selected.idExpedientePrincipal);
        for (int i = filasTabla.size() - 1; i > principalRow; i--) {
            VerificacionTableRow row = filasTabla.get(i);
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
            VerificacionTableRow row = filasTabla.get(i);
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
        cmbEstadoFiltro.setSelectedIndex(0);
        spnLimite.setValue(200);
        restaurarFechasBusqueda();
        expedientes.clear();
        filasTabla.clear();
        asociadosCache.clear();
        principalesExpandidos.clear();
        principalesCargando.clear();
        kpiActivo = FiltroKpi.TODOS;
        idExpedienteExpansionActiva = null;
        tableModel.setRowCount(0);
        table.clearSelection();
        tablePanel.setEmpty(true);
        if (documentosTreePanel != null) { documentosTreePanel.setDocumentos(null, new ArrayList<DocumentoVerificacionDTO>()); }
        limpiarFormulario();
        cardEnVerificacion.setValue("0");
        cardObservados.setValue("0");
        cardInconsistentes.setValue("0");
        cardAprobados.setValue("0");
        cardEmitidos.setValue("0");
        cardPlazoCritico.setValue("0");
        marcarKpis();
        lblEstado.setText("Filtros limpiados. Presione Buscar para consultar expedientes en Verificación.");
        panelVerificacionCerradoPorUsuario = false;
        actualizarSeleccion();
    }

    private void actualizarTituloPanelVerificacion(String titular) {
        if (panelDatosVerificacion == null) {
            return;
        }
        String titulo = "<html><div style='font-size:18px;font-weight:700;color:#1c242e;'>Panel de Verificación</div>";
        if (titular != null && !titular.trim().isEmpty() && !"-".equals(titular.trim())) {
            titulo = titulo + "<div style='font-size:12px;font-weight:600;color:rgb(21,71,117);margin-top:2px;'>"
                    + escapeHtml(titular.trim()) + "</div>";
        }
        titulo += "</html>";
        panelDatosVerificacion.setTitle(titulo);
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void limpiarDatosExpedienteVerificacion() {
        JLabel[] labels = {
            lblDatosVencimiento,
            lblDatosExpediente,
            lblDatosExpedienteSgd,
            lblDatosTipoActa,
            lblDatosNumeroActa,
            lblDatosFechaRecepcion,
            lblDatosCanalIngreso,
            lblDatosTramiteWeb,
            lblDatosTipoDocumentoSolicitud,
            lblDatosNumeroDocumentoSolicitud,
            lblDatosTipoSolicitud,
            lblDatosGrupoFamiliar,
            lblDatosTitular,
            lblDatosTipoDocumentoTitular,
            lblDatosNumeroDocumentoTitular,
            lblDatosSolicitante,
            lblDatosTipoDocumentoSolicitante,
            lblDatosNumeroDocumentoSolicitante,
            lblDatosCorreo,
            lblDatosTelefono,
            lblDatosDepartamento,
            lblDatosProvincia,
            lblDatosDistrito,
            lblDatosDireccion
        };
        for (JLabel label : labels) {
            label.setText("-");
            label.setToolTipText(null);
        }
        actualizarBadgeDias(lblDatosDias, null);
    }

    private void cargarDatosExpedienteVerificacion(VerificacionExpedienteDTO item) {
        if (item == null) {
            limpiarDatosExpedienteVerificacion();
            return;
        }
        lblDatosExpediente.setText(valorUi(item.getNumeroExpediente()));
        lblDatosExpedienteSgd.setText(valorUi(item.getNumeroExpedienteSgd()));
        lblDatosTipoActa.setText(valorUi(item.getTipoActa()));
        lblDatosNumeroActa.setText(valorUi(item.getNumeroActa()));
        lblDatosFechaRecepcion.setText(formatDate(item.getFechaRecepcion()));
        lblDatosCanalIngreso.setText(valorUi(item.getCanalIngreso()));
        lblDatosTramiteWeb.setText(valorUi(item.getNumeroTramiteDocumentario()));
        lblDatosTipoDocumentoSolicitud.setText(valorUi(item.getTipoDocumento()));
        lblDatosNumeroDocumentoSolicitud.setText(valorUi(item.getNumeroDocumento()));
        lblDatosTipoSolicitud.setText(extraerValorObservacion(item.getObservacionSolicitud(), "Tipo de solicitud"));
        lblDatosGrupoFamiliar.setText(valorUi(item.getGrupoFamiliarEstado()));
        lblDatosTitular.setText(valorUi(item.getTitular()));
        lblDatosTipoDocumentoTitular.setText(valorUi(item.getTipoDocumentoTitular()));
        lblDatosNumeroDocumentoTitular.setText(valorUi(item.getNumeroDocumentoTitular()));
        lblDatosSolicitante.setText(valorUi(item.getSolicitante()));
        lblDatosTipoDocumentoSolicitante.setText(valorUi(item.getTipoDocumentoSolicitante()));
        lblDatosNumeroDocumentoSolicitante.setText(valorUi(item.getNumeroDocumentoSolicitante()));
        lblDatosCorreo.setText(valorUi(item.getCorreoSolicitante()));
        lblDatosTelefono.setText(valorUi(item.getTelefonoSolicitante()));
        lblDatosDepartamento.setText(valorUi(item.getDepartamentoSolicitante()));
        lblDatosProvincia.setText(valorUi(item.getProvinciaSolicitante()));
        lblDatosDistrito.setText(valorUi(item.getDistritoSolicitante()));
        lblDatosDireccion.setText(valorUi(item.getDireccionSolicitante()));
        lblDatosDireccion.setToolTipText(valorUi(item.getDireccionSolicitante()));
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

    private void actualizarSeleccion() {
        VerificacionTableRow fila = obtenerFilaSeleccionada();
        VerificacionExpedienteDTO item = fila == null ? null : fila.principal;
        boolean has = fila != null;
        boolean asociado = fila != null && fila.esAsociada();
        btnRegistrarVerificacion.setEnabled(has && !asociado && item.isRegistrableVerificacion());
        btnAprobar.setEnabled(has && !asociado && item.isRegistrableVerificacion());
        btnObservar.setEnabled(has && !asociado && item.isRegistrableVerificacion());
        btnDocumentoInconsistente.setEnabled(has && !asociado && item.isRegistrableVerificacion());
        btnEnviarFirma.setEnabled(has && !asociado && item.isEnviableFirma());
        btnDevolverAnalisis.setEnabled(has && !asociado && item.isDevolvibleAnalisis());
        actualizarAccionesFirmaEmision(item, asociado, has);
        actualizarVisibilidadPanelVerificacion();
        if (!has) {
            actualizarTituloPanelVerificacion(null);
            limpiarDatosExpedienteVerificacion();
            lblExpediente.setText("-");
            lblExpedienteSgd.setText("-");
            lblTitular.setText("-");
            lblActa.setText("-");
            lblProcedimiento.setText("-");
            lblResponsable.setText("-");
            lblResponsableAnalisis.setText("-");
            actualizarAbogadoAnalisisDestino(null);
            lblEtapaEstado.setText("-");
            lblDestinoSiguiente.setText("-");
            lblAnalisis.setText("-");
            lblAlertas.setText("Sin alertas.");
            limpiarContextoDocumentoEmitido();
            txtFundamentoAnalisis.setText("");
            if (documentosTreePanel != null) { documentosTreePanel.setDocumentos(null, new ArrayList<DocumentoVerificacionDTO>()); }
            limpiarFormularioFirma();
            return;
        }
        if (asociado) {
            ExpedienteRelacionadoDTO relacionado = fila.asociado;
            actualizarTituloPanelVerificacion(relacionado.getTitular());
            limpiarDatosExpedienteVerificacion();
            lblExpediente.setText("Documento asociado seleccionado");
            lblExpedienteSgd.setText(item == null ? "-" : valorUi(item.getNumeroExpedienteSgd()));
            lblTitular.setText(valorUi(relacionado.getTitular()));
            lblActa.setText((valorUi(relacionado.getTipoActa()) + " " + valorUi(relacionado.getNumeroActa())).trim());
            lblProcedimiento.setText(procedimientoAsociado(relacionado));
            lblResponsable.setText(valorUi(relacionado.getAbogadoAsignado()));
            lblResponsableAnalisis.setText(item == null || item.getResponsableAnalisis().isEmpty() ? "-" : item.getResponsableAnalisis());
            actualizarAbogadoAnalisisDestino(item);
            lblEtapaEstado.setText("Expediente principal: " + fila.numeroExpedientePrincipal());
            lblDestinoSiguiente.setText("Contexto del expediente principal");
            lblAnalisis.setText("Contexto de verificación");
            txtFundamentoAnalisis.setText("Este documento está asociado al expediente principal y se muestra como contexto del caso.");
            txtFundamentoAnalisis.setCaretPosition(0);
            lblAlertas.setText(textoRelacionAsociada(relacionado));
            txtComentario.setText("");
            txtObservacion.setText("");
            if (documentosTreePanel != null) { documentosTreePanel.setDocumentos(null, new ArrayList<DocumentoVerificacionDTO>()); }
            limpiarContextoDocumentoEmitido();
            limpiarFormularioFirma();
            return;
        }
        actualizarTituloPanelVerificacion(item.getTitular());
        cargarDatosExpedienteVerificacion(item);
        lblExpediente.setText(item.getNumeroExpediente());
        lblExpedienteSgd.setText(valorUi(item.getNumeroExpedienteSgd()));
        lblTitular.setText(item.getTitular());
        lblActa.setText((item.getTipoActa() + " " + item.getNumeroActa()).trim());
        lblProcedimiento.setText(item.getProcedimiento());
        lblResponsable.setText(item.getResponsable().isEmpty() ? "-" : item.getResponsable());
        lblResponsableAnalisis.setText(item.getResponsableAnalisis().isEmpty() ? "-" : item.getResponsableAnalisis());
        actualizarAbogadoAnalisisDestino(item);
        lblEtapaEstado.setText(estadoVisualIntegrado(item));
        lblDestinoSiguiente.setText(item.getDestinoSiguiente());
        lblAnalisis.setText(item.getUltimoResultadoAnalisis().isEmpty() ? "Sin resultado registrado" : item.getUltimoResultadoAnalisis());
        txtFundamentoAnalisis.setText(item.getFundamentoAnalisis());
        txtFundamentoAnalisis.setCaretPosition(0);
        lblAlertas.setText(alertas(item));
        txtComentario.setText("");
        txtObservacion.setText(item.getUltimaObservacionVerificacion());
        cargarContextoDocumentoEmitido(item);
        cargarDocumentos(item);
    }

    private void cerrarPanelVerificacion() {
        panelVerificacionCerradoPorUsuario = true;
        if (splitOperativo != null) {
            splitOperativo.setSideVisible(false);
        }
    }

    private void actualizarVisibilidadPanelVerificacion() {
        if (splitOperativo == null) {
            return;
        }
        if (!splitOperativo.isSideVisible()) {
            return;
        }
        splitOperativo.setSideVisible(obtenerFilaSeleccionada() != null && !panelVerificacionCerradoPorUsuario);
        actualizarLenguetasVerificacion();
    }

    private void seleccionarTabVerificacion(String tab) {
        if (tab == null || panelVerificacionCardsLayout == null || panelVerificacionCards == null) {
            return;
        }
        boolean mismaTab = tab.equals(tabVerificacionActiva);
        tabVerificacionActiva = tab;
        panelVerificacionCardsLayout.show(panelVerificacionCards, tab);
        if (splitOperativo != null && splitOperativo.isSideVisible() && mismaTab) {
            splitOperativo.setSideExpanded(!splitOperativo.isSideExpanded());
        }
        panelVerificacionCards.revalidate();
        panelVerificacionCards.repaint();
        actualizarLenguetasVerificacion();
    }

    private void actualizarLenguetasVerificacion() {
        boolean expandido = splitOperativo != null && splitOperativo.isSideExpanded();
        tabDatosVerificacion.setState(TAB_VERIFICACION_DATOS.equals(tabVerificacionActiva), TAB_VERIFICACION_DATOS.equals(tabVerificacionActiva) && expandido);
        tabOperacionVerificacion.setState(TAB_VERIFICACION_OPERACION.equals(tabVerificacionActiva), TAB_VERIFICACION_OPERACION.equals(tabVerificacionActiva) && expandido);
        tabDatosVerificacion.setToolTipText("Datos de verificación");
        tabOperacionVerificacion.setToolTipText("Verificar documentos");
    }

    private String alertas(VerificacionExpedienteDTO item) {
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
        if (item.isRequierePublicacion()) {
            alertas.add("Publicación prevista");
        }
        if (item.isTieneCartaEdicto()) {
            alertas.add("Carta edicto: validación de Subdirector");
        }
        return alertas.isEmpty() ? "Sin alertas." : String.join(" · ", alertas);
    }

    private String estadoVisualIntegrado(VerificacionExpedienteDTO item) {
        if (item == null) {
            return "-";
        }
        if (esEnFirmaEmision(item)) {
            return "Verificación / " + DisplayNameMapperV2.estado(item.getEstadoCodigo());
        }
        return DisplayNameMapperV2.etapa(item.getEtapaCodigo())
                + " / "
                + DisplayNameMapperV2.estado(item.getEstadoCodigo());
    }

    private void cargarContextoDocumentoEmitido(VerificacionExpedienteDTO item) {
        if (item == null) {
            limpiarContextoDocumentoEmitido();
            return;
        }
        limpiarFormularioFirma();
        lblEstadoDocumentoEmitido.setText(item.isDocumentoEmitido()
                ? "Emitido"
                : DisplayNameMapperV2.estado(item.getEstadoCodigo()));
        lblResponsableFirma.setText(item.isTieneCartaEdicto()
                ? "Subdirector"
                : "Responsable de verificación");
        lblRequierePublicacion.setText(item.isRequierePublicacion() ? "Sí" : "No");
        lblFechaPublicacion.setText(item.getFechaPublicacion() == null
                ? "-"
                : formatDate(item.getFechaPublicacion()));
        seleccionarTipoDocumentoEmitido(item.getTipoDocumentoEmitido());
        txtNumeroResolucion.setText(item.getNumeroDocumentoEmitido());
        if (item.getFechaFirmaDocumento() != null) {
            txtFechaFirma.setText(formatDate(item.getFechaFirmaDocumento().toLocalDate()));
        }
        if (item.getFechaDocumentoEmitido() != null) {
            String fechaDocumento = formatDate(item.getFechaDocumentoEmitido());
            txtFechaEmision.setText(fechaDocumento);
            txtFechaResolucion.setText(fechaDocumento);
        }
    }

    private void limpiarContextoDocumentoEmitido() {
        lblEstadoDocumentoEmitido.setText("-");
        lblResponsableFirma.setText("-");
        lblRequierePublicacion.setText("-");
        lblFechaPublicacion.setText("-");
    }

    private void seleccionarTipoDocumentoEmitido(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return;
        }
        for (int i = 0; i < cmbTipoResolucion.getItemCount(); i++) {
            SimpleItem item = cmbTipoResolucion.getItemAt(i);
            if (item != null && nombre.equalsIgnoreCase(item.nombre)) {
                cmbTipoResolucion.setSelectedIndex(i);
                return;
            }
        }
    }

    private boolean esEnFirmaEmision(VerificacionExpedienteDTO item) {
        return item != null && ETAPA_FIRMA_EMISION.equalsIgnoreCase(item.getEtapaCodigo());
    }

    private boolean esFirmable(VerificacionExpedienteDTO item) {
        return esEnFirmaEmision(item) && ESTADO_PARA_FIRMA.equalsIgnoreCase(item.getEstadoCodigo());
    }

    private boolean esEmitible(VerificacionExpedienteDTO item) {
        return esEnFirmaEmision(item) && ESTADO_FIRMADO.equalsIgnoreCase(item.getEstadoCodigo());
    }

    private boolean esNumerable(VerificacionExpedienteDTO item) {
        return esEnFirmaEmision(item) && ESTADO_EMITIDO.equalsIgnoreCase(item.getEstadoCodigo());
    }

    private boolean esEnviableEjecucion(VerificacionExpedienteDTO item) {
        return esEnFirmaEmision(item)
                && item.isResultadoResolutivo()
                && ESTADO_RESOLUCION_NUMERADA.equalsIgnoreCase(item.getEstadoCodigo());
    }

    private void actualizarAccionesFirmaEmision(VerificacionExpedienteDTO item, boolean asociado, boolean disponible) {
        btnRegistrarFirma.setEnabled(disponible && !asociado && esFirmable(item));
        btnRegistrarEmision.setEnabled(disponible && !asociado && esEmitible(item));
        btnRegistrarNumero.setEnabled(disponible && !asociado && esNumerable(item));
        btnEnviarEjecucion.setEnabled(disponible && !asociado && esEnviableEjecucion(item));
        btnEnviarNotificacion.setEnabled(disponible && !asociado && item != null && item.isEnviableNotificacion());
    }

    private void cargarDocumentos(VerificacionExpedienteDTO item) {
        if (item == null || item.getIdExpediente() == null) {
            if (documentosTreePanel != null) {
                documentosTreePanel.setDocumentos(null, new ArrayList<DocumentoVerificacionDTO>());
            }
            return;
        }
        final Long idExpediente = item.getIdExpediente();
        SwingWorker<List<DocumentoVerificacionDTO>, Void> worker = new SwingWorker<List<DocumentoVerificacionDTO>, Void>() {
            @Override
            protected List<DocumentoVerificacionDTO> doInBackground() throws Exception {
                return documentoService.listarDocumentosAnalizados(idExpediente);
            }

            @Override
            protected void done() {
                try {
                    VerificacionExpedienteDTO seleccionado = obtenerSeleccionado();
                    if (seleccionado == null || !idExpediente.equals(seleccionado.getIdExpediente())) {
                        return;
                    }
                    if (documentosTreePanel != null) {
                        documentosTreePanel.setDocumentos(idExpediente, get());
                    }
                } catch (Exception ex) {
                    lblEstado.setText("No se pudieron cargar los documentos analizados.");
                }
            }
        };
        worker.execute();
    }

    private void cargarEquiposDestino() {
        cargandoCombosDestino = true;
        cmbEquipoDestino.removeAllItems();
        cmbEquipoDestino.addItem(EquipoItem.placeholder("Seleccione equipo"));
        cmbUsuarioDestino.removeAllItems();
        cmbUsuarioDestino.addItem(UsuarioItem.placeholder("Seleccione abogado"));
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO> doInBackground() throws Exception {
                return verificacionService.listarEquiposActivos();
            }

            @Override
            protected void done() {
                try {
                    for (com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO equipo : get()) {
                        cmbEquipoDestino.addItem(new EquipoItem(equipo));
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los equipos destino.", ex);
                } finally {
                    cargandoCombosDestino = false;
                }
            }
        };
        worker.execute();
    }

    private void cargarUsuariosDestino() {
        EquipoItem equipoItem = (EquipoItem) cmbEquipoDestino.getSelectedItem();
        cmbUsuarioDestino.removeAllItems();
        cmbUsuarioDestino.addItem(UsuarioItem.placeholder("Seleccione abogado"));
        if (equipoItem == null || equipoItem.equipo == null) {
            return;
        }
        final Long idEquipo = equipoItem.equipo.getIdEquipo();
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO> doInBackground() throws Exception {
                return verificacionService.listarAbogadosAsignables(idEquipo);
            }

            @Override
            protected void done() {
                EquipoItem equipoActual = (EquipoItem) cmbEquipoDestino.getSelectedItem();
                if (equipoActual == null || equipoActual.equipo == null || !idEquipo.equals(equipoActual.equipo.getIdEquipo())) {
                    return;
                }
                try {
                    for (com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO usuario : get()) {
                        cmbUsuarioDestino.addItem(new UsuarioItem(usuario));
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los abogados del equipo destino.", ex);
                }
            }
        };
        worker.execute();
    }

    private void registrarVerificacion() {
        VerificacionExpedienteDTO item = requerirSeleccion("Seleccione un expediente para registrar verificación.");
        if (item == null) {
            return;
        }
        ResultadoItem resultado = (ResultadoItem) cmbResultadoVerificacion.getSelectedItem();
        String resultadoCodigo = resultado == null ? "" : resultado.codigo;
        final String comentario = txtComentario.getText();
        if ("OBSERVADO".equals(resultadoCodigo)) {
            ObservacionVerificacionDTO observacion = new ObservacionVerificacionDTO("", "", "", "", comentario);
            VerificacionRegistroDTO registro = new VerificacionRegistroDTO(
                    item.getIdExpediente(), "REGISTRO_OBSERVACION_VERIFICACION", "Observado", comentario, observacion);
            confirmarYEjecutar(
                    "Registrar verificación",
                    "El expediente " + item.getNumeroExpediente() + " quedará Observado y volverá a Análisis. ¿Desea continuar?",
                    () -> verificacionService.registrarObservacionYDevolverAnalisis(registro));
            return;
        }
        if (item.isResultadoResolutivo()) {
            confirmarYEjecutar(
                    "Registrar verificación",
                    "El expediente " + item.getNumeroExpediente() + " será aprobado y enviado a Ejecución. ¿Desea continuar?",
                    () -> verificacionService.aprobarVerificacionDirecta(item.getIdExpediente(), comentario));
            return;
        }
        EquipoItem equipoItem = (EquipoItem) cmbEquipoDestino.getSelectedItem();
        UsuarioItem usuarioItem = (UsuarioItem) cmbUsuarioDestino.getSelectedItem();
        if (equipoItem == null || equipoItem.equipo == null || usuarioItem == null || usuarioItem.usuario == null) {
            mostrarInfo("Seleccione equipo destino y usuario destino para registrar la verificación.");
            return;
        }
        final Long idEquipoDestino = equipoItem.equipo.getIdEquipo();
        final Long idUsuarioDestino = usuarioItem.usuario.getIdUsuario();
        confirmarYEjecutar(
                "Registrar verificación",
                "Se registrará la verificación del expediente " + item.getNumeroExpediente()
                        + " y se enviará al equipo destino seleccionado. ¿Desea continuar?",
                () -> verificacionService.aprobarVerificacionConDestino(
                        item.getIdExpediente(), comentario, idEquipoDestino, idUsuarioDestino));
    }

    private void accionRapida(String accionCodigo) {
        VerificacionExpedienteDTO item = requerirSeleccion("Seleccione un expediente para ejecutar la acción.");
        if (item == null) {
            return;
        }
        seleccionarResultado(accionCodigo);
        String titulo = "APROBACION_VERIFICACION".equals(accionCodigo)
                ? "Aprobar verificación"
                : "REVERSION_ESTADO_DOCUMENTO".equals(accionCodigo)
                ? "Documento inconsistente"
                : "Requiere corrección";
        VerificacionRegistroDTO registro = construirRegistro(item, accionCodigo);
        confirmarYEjecutar(
                titulo,
                "Se aplicará la acción al expediente " + item.getNumeroExpediente() + ". ¿Desea continuar?",
                () -> verificacionService.registrarVerificacion(registro));
    }

    private void enviarFirma() {
        VerificacionExpedienteDTO item = requerirSeleccion("Seleccione un expediente verificado para preparar el documento emitido.");
        if (item == null) {
            return;
        }
        confirmarYEjecutar(
                "Preparar documento emitido",
                "El expediente " + item.getNumeroExpediente()
                        + " continuará con la preparación del documento emitido. ¿Desea continuar?",
                () -> verificacionService.enviarFirma(item.getIdExpediente(), txtComentario.getText()));
    }

    private void devolverAnalisis() {
        VerificacionExpedienteDTO item = requerirSeleccion("Seleccione un expediente observado o inconsistente para devolver a análisis.");
        if (item == null) {
            return;
        }
        VerificacionRegistroDTO registro = construirRegistro(item, "DEVOLUCION_A_ANALISIS");
        confirmarYEjecutar(
                "Devolver a análisis",
                "Se devolverá el expediente " + item.getNumeroExpediente() + " a Análisis para corrección. ¿Desea continuar?",
                () -> verificacionService.devolverAnalisis(registro));
    }

    private void registrarFirma() {
        VerificacionExpedienteDTO item = requerirSeleccionFirma("Seleccione un expediente pendiente de conformidad documental.");
        if (item == null) {
            return;
        }
        confirmarYEjecutarFirma(
                "Registrar conformidad",
                "Se registrará la conformidad del documento del expediente "
                        + item.getNumeroExpediente() + ". ¿Desea continuar?",
                "Registrando conformidad documental...",
                () -> firmaEmisionService.registrarFirma(crearRegistroFirma(FirmaEmisionExpedienteService.ACCION_FIRMA_DOCUMENTO)));
    }

    private void registrarEmision() {
        VerificacionExpedienteDTO item = requerirSeleccionFirma("Seleccione un expediente firmado para registrar emisión.");
        if (item == null) {
            return;
        }
        confirmarYEjecutarFirma(
                "Marcar como emitido",
                "El documento del expediente " + item.getNumeroExpediente() + " quedará como Emitido. ¿Desea continuar?",
                "Registrando documento emitido...",
                () -> firmaEmisionService.registrarEmision(crearRegistroFirma(FirmaEmisionExpedienteService.ACCION_FIRMA_DOCUMENTO)));
    }

    private void registrarNumeroResolucion() {
        VerificacionExpedienteDTO item = requerirSeleccionFirma("Seleccione un expediente emitido para registrar número.");
        if (item == null) {
            return;
        }
        confirmarYEjecutarFirma(
                "Registrar número de documento",
                "Se registrará el número de resolución o documento del expediente "
                        + item.getNumeroExpediente() + ". ¿Desea continuar?",
                "Registrando número de documento...",
                () -> firmaEmisionService.registrarNumeroResolucion(crearRegistroFirma(FirmaEmisionExpedienteService.ACCION_REGISTRO_NUMERO)));
    }

    private void enviarEjecucion() {
        VerificacionExpedienteDTO item = requerirSeleccionFirma("Seleccione un expediente con resolución numerada para enviar a Ejecución.");
        if (item == null) {
            return;
        }
        confirmarYEjecutarFirma(
                "Enviar a ejecución",
                "El expediente " + item.getNumeroExpediente() + " será enviado a Ejecución. ¿Desea continuar?",
                "Enviando expediente a Ejecución...",
                () -> firmaEmisionService.enviarEjecucion(crearRegistroFirma(FirmaEmisionExpedienteService.ACCION_REGISTRO_NUMERO)));
    }

    private void enviarNotificacion() {
        VerificacionExpedienteDTO item = requerirSeleccionFirma(
                "Seleccione un documento emitido con una transición activa hacia Notificación.");
        if (item == null) {
            return;
        }
        if (item.isResultadoResolutivo()) {
            mostrarInfo("Las resoluciones deben continuar a Ejecución.");
            return;
        }
        if (!item.isPuedeDerivarNotificacion()) {
            mostrarInfo("No existe una transición activa hacia Notificación para el estado actual del documento.");
            return;
        }
        confirmarYEjecutarFirma(
                "Enviar a notificación",
                "El documento emitido del expediente " + item.getNumeroExpediente()
                        + " será derivado a Notificación. ¿Desea continuar?",
                "Enviando documento a Notificación...",
                () -> firmaEmisionService.enviarNotificacion(
                        crearRegistroFirma(FirmaEmisionExpedienteService.ACCION_DERIVACION_NOTIFICACION)));
    }

    private VerificacionRegistroDTO construirRegistro(VerificacionExpedienteDTO item, String accionOverride) {
        ResultadoItem resultado = (ResultadoItem) cmbResultado.getSelectedItem();
        SimpleItem tipoObservacion = (SimpleItem) cmbTipoObservacion.getSelectedItem();
        SimpleItem motivoCorreccion = (SimpleItem) cmbMotivoCorreccion.getSelectedItem();
        String accion = accionOverride == null ? (resultado == null ? "" : resultado.codigo) : accionOverride;
        String nombre = resultado == null ? "" : resultado.nombre;
        ObservacionVerificacionDTO observacion = new ObservacionVerificacionDTO(
                tipoObservacion == null ? "" : tipoObservacion.codigo,
                tipoObservacion == null ? "" : tipoObservacion.nombre,
                motivoCorreccion == null ? "" : motivoCorreccion.codigo,
                motivoCorreccion == null ? "" : motivoCorreccion.nombre,
                txtObservacion.getText());
        return new VerificacionRegistroDTO(item.getIdExpediente(), accion, nombre, txtComentario.getText(), observacion);
    }

    private FirmaEmisionRegistroDTO crearRegistroFirma(String accionCodigo) {
        VerificacionExpedienteDTO item = requerirSeleccionFirma("Seleccione un expediente con gestión de documento emitido.");
        if (item == null) {
            throw new IllegalArgumentException("Seleccione un expediente con gestión de documento emitido.");
        }
        SimpleItem tipo = (SimpleItem) cmbTipoResolucion.getSelectedItem();
        return new FirmaEmisionRegistroDTO(
                item.getIdExpediente(),
                accionCodigo,
                tipo == null ? "" : tipo.codigo,
                tipo == null ? "" : tipo.nombre,
                txtNumeroResolucion.getText(),
                parseFechaFirma(txtFechaFirma, "fecha de firma"),
                parseFechaFirma(txtFechaEmision, "fecha de emisión"),
                parseFechaFirma(txtFechaResolucion, "fecha de resolución"),
                txtComentarioFirma.getText());
    }

    private LocalDate parseFechaFirma(JTextField field, String label) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ingrese una " + label + " válida con formato dd/MM/yyyy.");
        }
    }

    private void confirmarYEjecutar(String titulo, String mensaje, OperacionVerificacion operacion) {
        int confirm = JOptionPane.showConfirmDialog(this, mensaje, titulo, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        setTrabajando(true, "Ejecutando operación de verificación...");
        SwingWorker<VerificacionResultadoDTO, Void> worker = new SwingWorker<VerificacionResultadoDTO, Void>() {
            @Override
            protected VerificacionResultadoDTO doInBackground() throws Exception {
                return operacion.ejecutar();
            }

            @Override
            protected void done() {
                try {
                    VerificacionResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelVerificacionV2.this,
                            resultado.getMensaje(),
                            "Verificación",
                            JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormulario();
                    buscar();
                } catch (Exception ex) {
                    mostrarError("No se pudo completar la operación de verificación.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void confirmarYEjecutarFirma(
            String titulo,
            String mensaje,
            String mensajeTrabajo,
            OperacionFirmaEmision operacion) {
        int confirm = JOptionPane.showConfirmDialog(this, mensaje, titulo, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        setTrabajando(true, mensajeTrabajo);
        SwingWorker<FirmaEmisionResultadoDTO, Void> worker = new SwingWorker<FirmaEmisionResultadoDTO, Void>() {
            @Override
            protected FirmaEmisionResultadoDTO doInBackground() throws Exception {
                return operacion.ejecutar();
            }

            @Override
            protected void done() {
                try {
                    FirmaEmisionResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelVerificacionV2.this,
                            resultado.getMensaje(),
                            "Documento emitido",
                            JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormularioFirma();
                    buscar();
                } catch (Exception ex) {
                    mostrarError("No se pudo completar la operación del documento emitido.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void abrirDetalle() {
        VerificacionTableRow fila = obtenerFilaSeleccionada();
        if (fila == null || fila.getIdExpediente() == null) {
            mostrarInfo("Seleccione un expediente para ver el detalle.");
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(owner, fila.getIdExpediente());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private VerificacionExpedienteDTO requerirSeleccion(String mensaje) {
        VerificacionTableRow fila = obtenerFilaSeleccionada();
        if (fila != null && fila.esAsociada()) {
            mostrarInfo("Este documento está asociado al expediente principal y se muestra como contexto del caso.");
            return null;
        }
        VerificacionExpedienteDTO item = fila == null ? null : fila.principal;
        if (item == null) {
            mostrarInfo(mensaje);
        }
        return item;
    }

    private VerificacionExpedienteDTO requerirSeleccionFirma(String mensaje) {
        VerificacionExpedienteDTO item = requerirSeleccion(mensaje);
        if (item == null) {
            return null;
        }
        if (!esEnFirmaEmision(item)) {
            mostrarInfo("Seleccione un expediente que ya se encuentre en gestión de documento emitido.");
            return null;
        }
        return item;
    }

    private VerificacionExpedienteDTO obtenerSeleccionado() {
        VerificacionTableRow fila = obtenerFilaSeleccionada();
        return fila == null || !fila.esPrincipal() ? null : fila.principal;
    }

    private VerificacionTableRow obtenerFilaSeleccionada() {
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

    private VerificacionTableRow filaTabla(int modelRow) {
        if (modelRow < 0 || modelRow >= filasTabla.size()) {
            return null;
        }
        return filasTabla.get(modelRow);
    }

    private void actualizarResultadoSeleccionado() {
        if (cargandoCatalogos) {
            return;
        }
        ResultadoItem resultado = (ResultadoItem) cmbResultado.getSelectedItem();
        boolean requiereCorreccion = resultado != null
                && ("REGISTRO_OBSERVACION_VERIFICACION".equalsIgnoreCase(resultado.codigo)
                || "REVERSION_ESTADO_DOCUMENTO".equalsIgnoreCase(resultado.codigo));
        cmbTipoObservacion.setEnabled(requiereCorreccion);
        cmbMotivoCorreccion.setEnabled(requiereCorreccion);
        txtObservacion.setEnabled(requiereCorreccion);
        if (!requiereCorreccion) {
            if (cmbTipoObservacion.getItemCount() > 0) {
                cmbTipoObservacion.setSelectedIndex(0);
            }
            if (cmbMotivoCorreccion.getItemCount() > 0) {
                cmbMotivoCorreccion.setSelectedIndex(0);
            }
        }
    }

    private void seleccionarResultado(String accionCodigo) {
        for (int i = 0; i < cmbResultado.getItemCount(); i++) {
            ResultadoItem item = cmbResultado.getItemAt(i);
            if (accionCodigo.equalsIgnoreCase(item.codigo)) {
                cmbResultado.setSelectedIndex(i);
                return;
            }
        }
    }

    private void limpiarFormulario() {
        if (cmbResultado.getItemCount() > 0) {
            cmbResultado.setSelectedIndex(0);
        }
        if (cmbTipoObservacion.getItemCount() > 0) {
            cmbTipoObservacion.setSelectedIndex(0);
        }
        if (cmbMotivoCorreccion.getItemCount() > 0) {
            cmbMotivoCorreccion.setSelectedIndex(0);
        }
        txtComentario.setText("");
        txtObservacion.setText("");
        if (documentosTreePanel != null) { documentosTreePanel.setDocumentos(null, new ArrayList<DocumentoVerificacionDTO>()); }
        limpiarFormularioFirma();
        actualizarResultadoSeleccionado();
    }

    private void setTrabajando(boolean trabajando, String mensaje) {
        btnBuscar.setEnabled(!trabajando);
        btnLimpiar.setEnabled(!trabajando);
        btnRefrescar.setEnabled(!trabajando);
        VerificacionTableRow fila = obtenerFilaSeleccionada();
        VerificacionExpedienteDTO item = fila == null || !fila.esPrincipal() ? null : fila.principal;
        btnRegistrarVerificacion.setEnabled(!trabajando && item != null && item.isRegistrableVerificacion());
        btnAprobar.setEnabled(!trabajando && item != null && item.isRegistrableVerificacion());
        btnObservar.setEnabled(!trabajando && item != null && item.isRegistrableVerificacion());
        btnDocumentoInconsistente.setEnabled(!trabajando && item != null && item.isRegistrableVerificacion());
        btnEnviarFirma.setEnabled(!trabajando && item != null && item.isEnviableFirma());
        btnDevolverAnalisis.setEnabled(!trabajando && item != null && item.isDevolvibleAnalisis());
        actualizarAccionesFirmaEmision(item, false, !trabajando && item != null);
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
        inicializarFechasFirma();
    }

    private void limpiarFormularioFirma() {
        if (cmbTipoResolucion.getItemCount() > 0) {
            cmbTipoResolucion.setSelectedIndex(0);
        }
        txtNumeroResolucion.setText("");
        txtComentarioFirma.setText("");
        inicializarFechasFirma();
    }

    private void inicializarFechasFirma() {
        String hoy = DATE_FORMAT.format(LocalDate.now());
        txtFechaFirma.setText(hoy);
        txtFechaEmision.setText(hoy);
        txtFechaResolucion.setText(hoy);
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

    private static String formatDateTime(java.time.LocalDateTime value) {
        return value == null ? "" : DATE_TIME_FORMAT.format(value);
    }

    private void mostrarInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Verificación", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String context, Exception ex) {
        String message = ex.getMessage();
        if (message == null && ex.getCause() != null) {
            message = ex.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(
                this,
                context + (message == null ? "" : "\n" + message),
                "Error de verificación",
                JOptionPane.ERROR_MESSAGE);
        lblEstado.setText(context);
    }

    private interface OperacionVerificacion {
        VerificacionResultadoDTO ejecutar() throws Exception;
    }

    private interface OperacionFirmaEmision {
        FirmaEmisionResultadoDTO ejecutar() throws Exception;
    }

    private class VerificacionTableModel extends DefaultTableModel {
        private VerificacionTableModel() {
            super(new Object[]{
                "",
                "Días",
                "Expediente",
                "N° expediente SGD",
                "Fecha solicitud",
                "Tipo documento",
                "Titular",
                "Abogado designado",
                "Resultado",
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
            VerificacionTableRow fila = filaTabla(modelRow);
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

    private class VerificacionRenderer extends DefaultTableCellRenderer {

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
            VerificacionTableRow fila = filaTabla(modelRow);
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

    private Color colorFondoFila(int viewRow, VerificacionTableRow fila, boolean selected) {
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

    private boolean debeMostrarBarraGrupo(VerificacionTableRow fila) {
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

    private static String value(Object value) {
        return value == null ? "" : value.toString();
    }

    private static Long asLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null || value.toString().trim().isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(value.toString().trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static boolean pareceIdentificadorTecnico(String value) {
        return value != null && value.trim().matches("\\d+");
    }

    private static final class VerificacionTableRow {

        private final VerificacionExpedienteDTO principal;
        private final ExpedienteRelacionadoDTO asociado;
        private final Long idExpedientePrincipal;

        private VerificacionTableRow(VerificacionExpedienteDTO principal, ExpedienteRelacionadoDTO asociado) {
            this.principal = principal;
            this.asociado = asociado;
            this.idExpedientePrincipal = principal == null ? null : principal.getIdExpediente();
        }

        private static VerificacionTableRow principal(VerificacionExpedienteDTO principal) {
            return new VerificacionTableRow(principal, null);
        }

        private static VerificacionTableRow asociada(VerificacionExpedienteDTO principal, ExpedienteRelacionadoDTO asociado) {
            return new VerificacionTableRow(principal, asociado);
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

    private static class EquipoItem {
        private final com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO equipo;
        private final String label;

        private EquipoItem(com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO equipo) {
            this.equipo = equipo;
            this.label = equipo.getDisplayName();
        }

        private EquipoItem(String label) {
            this.equipo = null;
            this.label = label;
        }

        private static EquipoItem placeholder(String label) {
            return new EquipoItem(label);
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class UsuarioItem {
        private final com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO usuario;
        private final String label;

        private UsuarioItem(com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO usuario) {
            this.usuario = usuario;
            this.label = usuario.getDisplayName();
        }

        private UsuarioItem(String label) {
            this.usuario = null;
            this.label = label;
        }

        private static UsuarioItem placeholder(String label) {
            return new UsuarioItem(label);
        }

        @Override
        public String toString() {
            return label;
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
        private final List<CatalogoItemDTO> tiposObservacion;
        private final List<CatalogoItemDTO> motivosCorreccion;
        private final List<CatalogoItemDTO> estadosDocumento;
        private final List<CatalogoItemDTO> tiposResolucion;

        private CatalogosCarga(
                List<CatalogoItemDTO> resultados,
                List<CatalogoItemDTO> tiposObservacion,
                List<CatalogoItemDTO> motivosCorreccion,
                List<CatalogoItemDTO> estadosDocumento,
                List<CatalogoItemDTO> tiposResolucion) {
            this.resultados = resultados == null ? new ArrayList<CatalogoItemDTO>() : resultados;
            this.tiposObservacion = tiposObservacion == null ? new ArrayList<CatalogoItemDTO>() : tiposObservacion;
            this.motivosCorreccion = motivosCorreccion == null ? new ArrayList<CatalogoItemDTO>() : motivosCorreccion;
            this.estadosDocumento = estadosDocumento == null ? new ArrayList<CatalogoItemDTO>() : estadosDocumento;
            this.tiposResolucion = tiposResolucion == null ? new ArrayList<CatalogoItemDTO>() : tiposResolucion;
        }
    }
}
