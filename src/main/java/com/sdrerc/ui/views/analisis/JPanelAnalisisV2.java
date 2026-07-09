package com.sdrerc.ui.views.analisis;

import com.sdrerc.application.sdrercapp.AnalisisExpedienteService;
import com.sdrerc.application.sdrercapp.AnalisisPlantillaDocumentoService;
import com.sdrerc.application.sdrercapp.DocumentoAnalisisService;
import com.sdrerc.application.sdrercapp.ExpedienteRelacionadoService;
import com.sdrerc.domain.dto.sdrercapp.AnalisisDetalleDTO;
import com.sdrerc.domain.dto.sdrercapp.AnalisisExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.AnalisisItemDTO;
import com.sdrerc.domain.dto.sdrercapp.AnalisisRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.AnalisisResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosActaDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosPersonaRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosSolicitudDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO;
import com.sdrerc.domain.dto.sdrercapp.ObservacionAnalisisDTO;
import com.sdrerc.ui.common.icon.IconUtils;
import com.sdrerc.ui.appv2.components.AppV2ActionPanel;
import com.sdrerc.ui.appv2.components.AppV2ColumnFilterSupport;
import com.sdrerc.ui.appv2.components.AppV2AssociatedDocumentIconCell;
import com.sdrerc.ui.appv2.components.AppV2ExpandCollapseGlyph;
import com.sdrerc.ui.appv2.components.AppV2ExpedientePanelFactory;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.components.AppV2ReceiveActionButton;
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2SideActionPanel;
import com.sdrerc.ui.appv2.components.AppV2SideSectionPanel;
import com.sdrerc.ui.appv2.components.AppV2StackedSideTab;
import com.sdrerc.ui.appv2.components.AppV2ResponsiveGridPanel;
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
import com.sdrerc.ui.views.registrorecepcion.JPanelRegistroManualRecepcionV2;
import com.sdrerc.util.DateRangePickerSupport;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.BorderFactory;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.filechooser.FileNameExtensionFilter;

public class JPanelAnalisisV2 extends JPanel {

    private enum FiltroKpi {
        TODOS,
        POR_RECIBIR,
        EN_ANALISIS,
        CARTA_INTERMEDIA,
        OBSERVADOS,
        VENCIMIENTO_CRITICO
    }

    private static final int COL_EXPANDIR = 0;
    private static final int COL_DIAS = 1;
    private static final int COL_EXPEDIENTE = 2;
    private static final int COL_EXPEDIENTE_SGD = 3;
    private static final int COL_ESTADO = 10;
    private static final int COL_ASOCIADOS = 11;
    private static final int COL_ID = 12;
    private static final int PANEL_ANALISIS_ANCHO_MINIMO = 380;
    private static final int PANEL_ANALISIS_ANCHO_NORMAL = 430;
    private static final int PANEL_ANALISIS_TAB_OVERHANG = 46;
    private static final int PANEL_ANALISIS_TAB_TOP = 18;
    private static final int PANEL_ANALISIS_TAB_HEIGHT = 94;
    private static final String TAB_ANALISIS_DATOS = "datos";
    private static final String TAB_ANALISIS_DOCUMENTOS = "documentos";
    private static final int GROUP_STRIPE_WIDTH = 5;
    private static final int ASSOCIATED_EXPEDIENTE_INDENT = 8;
    private static final Color TABLE_SELECTION_BACKGROUND = new Color(219, 244, 249);
    private static final Color EXPANDED_PARENT_BACKGROUND = new Color(205, 236, 244);
    private static final Color TABLE_SELECTION_FOREGROUND = AppV2Theme.TEXT_PRIMARY;
    private static final Color ASSOCIATED_ROW_BACKGROUND = new Color(238, 250, 252);
    private static final Color ASSOCIATED_BLOCK_BORDER = new Color(224, 233, 240);
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

    private final AnalisisExpedienteService analisisService;
    private final DocumentoAnalisisService documentoService;
    private DocumentoAnalisisTreeGridPanelV2 documentosTreePanel;
    private final AnalisisPlantillaDocumentoService plantillaDocumentoService = new AnalisisPlantillaDocumentoService();
    private final ExpedienteRelacionadoService relacionadoService = new ExpedienteRelacionadoService();
    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite/SGD, acta, titular o documento", 28);
    private final PremiumDateFieldV2 fechaSolicitudDesde = new PremiumDateFieldV2();
    private final PremiumDateFieldV2 fechaSolicitudHasta = new PremiumDateFieldV2();
    private final JComboBox<SimpleItem> cmbEstadoFiltro = new JComboBox<SimpleItem>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnRecibir = new JButton("Recibir expediente");
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnRegistrarAnalisis = new JButton("Registrar Análisis");
    private final JButton btnCancelarAnalisis = new JButton("Cancelar");
    private final JButton btnEnviarVerificacion = new JButton("Enviar a verificación");
    private final JButton btnArchivarNoCorresponde = new JButton("Archivar no corresponde");

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
    private final JLabel lblRequierePublicacion = new JLabel("-");
    private final JLabel lblFechaPublicacion = new JLabel("-");
    private final JLabel lblFuentePublicacion = new JLabel("Dato de solo lectura registrado desde Asignación.");
    private final JLabel lblExpedienteDigital = new JLabel("Pendiente para módulo Expediente digital.");
    private final JLabel lblDatosResultadoInicial = new JLabel("-");
    private final JLabel lblDatosHojaEnvio = new JLabel("-");
    private final JLabel lblDatosTramiteWeb = new JLabel("-");
    private final JLabel lblDatosNumeroDocumentoTitular = new JLabel("-");
    private final JLabel lblDatosExpedienteSgd = new JLabel("-");
    private final JLabel lblDatosTipoSolicitud = new JLabel("-");
    private final JLabel lblDatosFechaRecepcion = new JLabel("-");
    private final JLabel lblDatosCanalIngreso = new JLabel("-");
    private final JLabel lblDatosPrioridad = new JLabel("-");
    private final JLabel lblDatosMarcaOperativa = new JLabel("-");
    private final JLabel lblDatosExpediente = new JLabel("-");
    private final JLabel lblDatosEstado = new JLabel("-");
    private final JLabel lblDatosObservacion = new JLabel("-");
    private final JLabel lblDatosTipoActa = new JLabel("-");
    private final JLabel lblDatosNumeroActa = new JLabel("-");
    private final JLabel lblDatosTipoDocumentoTitular = new JLabel("-");
    private final JLabel lblDatosTipoDocumentoSolicitante = new JLabel("-");
    private final JLabel lblDatosNumeroDocumentoSolicitante = new JLabel("-");
    private final JLabel lblDatosSolicitante = new JLabel("-");
    private final JLabel lblDatosCorreo = new JLabel("-");
    private final JLabel lblDatosTelefono = new JLabel("-");
    private final JLabel lblDatosDepartamento = new JLabel("-");
    private final JLabel lblDatosProvincia = new JLabel("-");
    private final JLabel lblDatosDistrito = new JLabel("-");
    private final JLabel lblDatosDireccion = new JLabel("-");
    private final BadgeV2 lblDatosDias = new BadgeV2("-", AppV2Theme.SOFT_GRAY, AppV2Theme.MUTED);
    private final JLabel lblDatosVencimiento = new JLabel("-");
    private final JLabel lblDatosTipoDocumentoSolicitud = new JLabel("-");
    private final JLabel lblDatosNumeroDocumentoSolicitud = new JLabel("-");
    private final JLabel lblDatosEquipo = new JLabel("-");
    private final JLabel lblPlantillaSeleccionada = new JLabel("Seleccione un documento analizado para descargar su plantilla.");
    private final JLabel lblPlantillaAyuda = new JLabel("Las plantillas reutilizan la configuración documental del expediente en análisis.");

    private final JComboBox<ResultadoItem> cmbResultado = new JComboBox<ResultadoItem>();
    private final JComboBox<SimpleItem> cmbIncorporado = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbTipoDocumento = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbEstadoDocumento = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbTipoObservacion = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbMotivoNoCorresponde = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbTipoDocumentoNoCorresponde = new JComboBox<SimpleItem>();
    private final JTextField txtNumeroDocumentoProveido = new JTextField();
    private final JCheckBox chkReconstitucion = new JCheckBox("Reconstitución");
    private final JCheckBox chkLegitimidad = new JCheckBox("Legitimidad");
    private final JCheckBox chkMediosProbatorios = new JCheckBox("Medios probatorios");
    private final JCheckBox chkRegistrarObservacion = new JCheckBox("Registrar observación");
    private final JCheckBox chkDocumentoRequiereRespuesta = new JCheckBox("¿Requiere respuesta?");
    private final JTextArea txtFundamento = new JTextArea(4, 22);
    private final PremiumDateFieldV2 fechaDocumentoAnalizado = new PremiumDateFieldV2();
    private final JTextArea txtDescripcionDocumento = new JTextArea(2, 20);
    private final JTextArea txtObservacion = new JTextArea(3, 22);
    private final JTextArea txtComentarioMovimiento = new JTextArea(3, 22);
    private final JButton btnDescargarPlantillaSeleccionada = new JButton("Descargar plantilla seleccionada");
    private final AppV2StackedSideTab tabDatosAnalisis = crearTabAnalisis("Datos", new Color(230, 241, 245), new Color(57, 125, 199));
    private final AppV2StackedSideTab tabDocumentosAnalisis = crearTabAnalisis("Análisis", new Color(224, 243, 240), new Color(10, 118, 145));
    private JTabbedPane tabsAnalisis;

    private final AnalisisTableModel tableModel = new AnalisisTableModel();
    private final JTable table = new AppV2Table(tableModel);
    private final AppV2TablePanel tablePanel = new AppV2TablePanel(
            table,
            "Sin expedientes para mostrar",
            "Seleccione filtros y presione Buscar.");
    private AppV2ColumnFilterSupport.Controller columnFilterSupport;
    private final DefaultTableModel documentosAsociadosModel = new DefaultTableModel(
            new Object[]{"N° expediente SGD", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable documentosAsociadosTable = new AppV2Table(documentosAsociadosModel);
    private JPanel panelSolicitudesAsociadas;
    private final List<AnalisisExpedienteDTO> expedientes = new ArrayList<AnalisisExpedienteDTO>();
    private final List<AnalisisTableRow> filasTabla = new ArrayList<AnalisisTableRow>();
    private final Map<Long, List<ExpedienteRelacionadoDTO>> asociadosCache = new HashMap<Long, List<ExpedienteRelacionadoDTO>>();
    private final Set<Long> principalesExpandidos = new HashSet<Long>();
    private final Set<Long> principalesCargando = new HashSet<Long>();
    private final AtomicLong secuenciaBusqueda = new AtomicLong(0L);
    private volatile SwingWorker<?, ?> busquedaActiva;
    private final List<ExpedienteRelacionadoDTO> documentosAsociadosPanel = new ArrayList<ExpedienteRelacionadoDTO>();
    private final List<AnalisisItemDTO> bloquesAnalisis = new ArrayList<AnalisisItemDTO>();
    private FiltroKpi kpiActivo = FiltroKpi.TODOS;
    private final MetricCardV2 cardPorRecibir = new MetricCardV2("Por recibir", "0", "Asignación / Asignado", AppV2Theme.INFO);
    private final MetricCardV2 cardEnAnalisis = new MetricCardV2("En análisis", "0", "Recibidos y observados", AppV2Theme.TEAL);
    private final MetricCardV2 cardCartaIntermedia = new MetricCardV2("Con carta intermedia", "0", "Documentos guardados", AppV2Theme.INDIGO);
    private final MetricCardV2 cardObservados = new MetricCardV2("Observados", "0", "Requieren subsanación", AppV2Theme.WARNING);
    private final MetricCardV2 cardVencimiento = new MetricCardV2("Por vencer / vencidos", "0", "Días hábiles críticos", AppV2Theme.ERROR);
    private AppV2OperationalSplitPanel splitOperativo;
    private AppV2SideActionPanel panelAnalisis;
    private AppV2SideActionPanel panelDatosAnalisis;
    private CardLayout panelAnalisisCardsLayout;
    private JPanel panelAnalisisCards;
    private String tabAnalisisActiva = TAB_ANALISIS_DATOS;
    private boolean panelAnalisisCerradoPorUsuario;
    private Long idExpedienteExpansionActiva;
    private Long idExpedienteDocumentosAsociados;
    private Long idExpedienteDetalleSolicitado;
    private Long idExpedienteDetalleCargado;
    private Long idExpedienteRecepcionPreguntada;
    private Long idAnalisisSeleccionado;

    private boolean cargandoCatalogos;
    private boolean cargandoDetalleAnalisis;
    private boolean cargandoBloquesAnalisis;
    private boolean busquedaInicialEjecutada;

    public JPanelAnalisisV2() {
        this(new AnalisisExpedienteService(), new DocumentoAnalisisService());
    }

    public JPanelAnalisisV2(AnalisisExpedienteService analisisService, DocumentoAnalisisService documentoService) {
        this.analisisService = analisisService;
        this.documentoService = documentoService;
        setLayout(new BorderLayout(8, 8));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        add(crearCentro(), BorderLayout.CENTER);
        configurarTabla();
        configurarDocumentosAsociadosTabla();
        configurarEventos();
        configurarKpisInteractivos();
        restaurarFechasBusqueda();
        cargarFiltrosBase();
        cargarCatalogos();
        actualizarSeleccion();
    }

    private JPanel crearHeader() {
        JPanel metricas = new AppV2ResponsiveGridPanel(190, 5, 12, 0);
        metricas.add(cardPorRecibir);
        metricas.add(cardEnAnalisis);
        metricas.add(cardCartaIntermedia);
        metricas.add(cardObservados);
        metricas.add(cardVencimiento);
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

        panelDatosAnalisis = crearPanelDatosAnalisis();
        panelAnalisis = crearPanelAnalisis();
        JPanel panelAnalisisConTab = crearPanelAnalisisConTab(
                panelDatosAnalisis,
                panelAnalisis);
        tabsAnalisis = new JTabbedPane();
        tabsAnalisis.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        tabsAnalisis.addTab("Bandeja Análisis", contenidoPrincipal);
        splitOperativo = new AppV2OperationalSplitPanel(
                tabsAnalisis,
                panelAnalisisConTab,
                0,
                PANEL_ANALISIS_ANCHO_MINIMO + PANEL_ANALISIS_TAB_OVERHANG,
                PANEL_ANALISIS_ANCHO_NORMAL + PANEL_ANALISIS_TAB_OVERHANG);
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
        accionesFiltro.add(btnEditar);
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

    private AppV2SideActionPanel crearPanelAnalisis() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de análisis", new Runnable() {
            @Override
            public void run() {
                cerrarPanelAnalisis();
            }
        });
        panel.setAccentColor(new Color(10, 118, 145));
        panel.addSection(crearResumenAnalisisUnico());
        panel.addSection(crearDocumentosPanel());
        panel.addSection(crearFormularioAnalisis());
        panel.setFooter(crearAccionesPanelAnalisis());
        return panel;
    }

    private AppV2SideActionPanel crearPanelDatosAnalisis() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de análisis", new Runnable() {
            @Override
            public void run() {
                cerrarPanelAnalisis();
            }
        });
        panel.setAccentColor(new Color(57, 125, 199));
        AppV2ResponsiveGridPanel secciones = new AppV2ResponsiveGridPanel(320, 2, 12, 12);
        secciones.add(crearDatosPlazoAnalisis());
        secciones.add(crearDatosExpedienteAnalisis());
        secciones.add(crearDatosActaAnalisis());
        secciones.add(crearDatosSolicitudAnalisis());
        secciones.add(crearDatosTitularAnalisis());
        secciones.add(crearDatosSolicitanteAnalisis());
        secciones.add(crearDatosNotificacionAnalisis());
        panel.addSection(secciones);
        panelSolicitudesAsociadas = crearDocumentosAsociadosPanel();
        panelSolicitudesAsociadas.setVisible(false);
        panel.addSection(panelSolicitudesAsociadas);
        return panel;
    }

    private void actualizarTituloPanelAnalisis(String titular) {
        if (panelDatosAnalisis == null) {
            return;
        }
        String titulo = "<html><div style='font-size:18px;font-weight:700;color:#1c242e;'>Panel de Análisis</div>";
        if (titular != null && !titular.trim().isEmpty() && !"-".equals(titular.trim())) {
            titulo = titulo + "<div style='font-size:12px;font-weight:600;color:rgb(21,71,117);margin-top:2px;'>"
                    + escapeHtml(titular.trim()) + "</div>";
        }
        titulo += "</html>";
        panelDatosAnalisis.setTitle(titulo);
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        String result = value;
        result = result.replace("&", "&amp;");
        result = result.replace("<", "&lt;");
        result = result.replace(">", "&gt;");
        result = result.replace("\"", "&quot;");
        result = result.replace("'", "&#39;");
        return result;
    }

    private AppV2SideActionPanel crearPanelResultadoAnalisis() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de análisis", new Runnable() {
            @Override
            public void run() {
                cerrarPanelAnalisis();
            }
        });
        panel.setAccentColor(new Color(198, 121, 31));
        panel.addSection(crearFormularioAnalisis());
        panel.addSection(crearPublicacionLecturaPanel());
        panel.addSection(crearObservacionPanel());
        panel.addSection(crearComentarioMovimientoPanel());
        panel.setFooter(crearAccionesPanelAnalisis());
        return panel;
    }

    private JPanel crearPanelAnalisisConTab(
            final AppV2SideActionPanel panelDatos,
            final AppV2SideActionPanel panelDocumentos) {
        JPanel wrapper = new JPanel(null) {
            @Override
            public void doLayout() {
                int width = getWidth();
                int height = getHeight();
                int panelX = PANEL_ANALISIS_TAB_OVERHANG;
                int panelWidth = Math.max(0, width - panelX);
                int[] positions = calcularPosicionesLenguetasAnalisis(2, PANEL_ANALISIS_TAB_HEIGHT, 8, height, PANEL_ANALISIS_TAB_TOP);
                tabDatosAnalisis.setBounds(0, positions[0], PANEL_ANALISIS_TAB_OVERHANG - 6, PANEL_ANALISIS_TAB_HEIGHT);
                tabDocumentosAnalisis.setBounds(0, positions[1], PANEL_ANALISIS_TAB_OVERHANG - 6, PANEL_ANALISIS_TAB_HEIGHT);
                panelAnalisisCards.setBounds(panelX, 0, panelWidth, height);
            }
        };
        wrapper.setOpaque(false);
        panelAnalisisCardsLayout = new CardLayout();
        panelAnalisisCards = new JPanel(panelAnalisisCardsLayout);
        panelAnalisisCards.setOpaque(false);
        panelAnalisisCards.add(panelDatos, TAB_ANALISIS_DATOS);
        panelAnalisisCards.add(panelDocumentos, TAB_ANALISIS_DOCUMENTOS);

        tabDatosAnalisis.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabAnalisis(TAB_ANALISIS_DATOS);
            }
        });
        tabDocumentosAnalisis.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabAnalisis(TAB_ANALISIS_DOCUMENTOS);
            }
        });
        wrapper.add(tabDatosAnalisis);
        wrapper.add(tabDocumentosAnalisis);
        wrapper.add(panelAnalisisCards);
        wrapper.setMinimumSize(new Dimension(
                PANEL_ANALISIS_ANCHO_MINIMO + PANEL_ANALISIS_TAB_OVERHANG,
                0));
        wrapper.setPreferredSize(new Dimension(
                PANEL_ANALISIS_ANCHO_NORMAL + PANEL_ANALISIS_TAB_OVERHANG,
                0));
        seleccionarTabAnalisis(TAB_ANALISIS_DATOS);
        return wrapper;
    }

    private JPanel crearAccionesPanelAnalisis() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setOpaque(false);
        panel.add(btnRegistrarAnalisis);
        panel.add(btnCancelarAnalisis);
        return panel;
    }

    private AppV2StackedSideTab crearTabAnalisis(String label, Color idleColor, Color accentColor) {
        return new AppV2StackedSideTab(
                label,
                PANEL_ANALISIS_TAB_OVERHANG - 6,
                PANEL_ANALISIS_TAB_HEIGHT,
                idleColor,
                accentColor,
                accentColor.darker());
    }

    private static int[] calcularPosicionesLenguetasAnalisis(int count, int tabHeight, int gap, int containerHeight, int topMargin) {
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

    private AppV2SideSectionPanel crearDatosPlazoAnalisis() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del plazo");
        section.addRow("Días", lblDatosDias);
        section.addRow("Fecha Vencimiento", lblDatosVencimiento);
        return section;
    }

    private AppV2SideSectionPanel crearDatosExpedienteAnalisis() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del expediente");
        section.addRow("N° expediente", lblDatosExpediente);
        section.addRow("N° expediente SGD", lblDatosExpedienteSgd);
        return section;
    }

    private AppV2SideSectionPanel crearDatosActaAnalisis() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del acta");
        section.addRow("Tipo de acta", lblDatosTipoActa);
        section.addRow("Nro. acta", lblDatosNumeroActa);
        return section;
    }

    private AppV2SideSectionPanel crearDatosSolicitudAnalisis() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos de solicitud");
        section.addRow("Fecha recepción", lblDatosFechaRecepcion);
        section.addRow("Canal de ingreso", lblDatosCanalIngreso);
        section.addRow("Nro. trámite web", lblDatosTramiteWeb);
        section.addRow("Proc.Registral", lblProcedimiento);
        section.addRow("Tipo documento", lblDatosTipoDocumentoSolicitud);
        section.addRow("N° documento", lblDatosNumeroDocumentoSolicitud);
        section.addRow("Tipo de solicitud", lblDatosTipoSolicitud);
        section.addRow("Grupo familiar", lblDatosMarcaOperativa);
        return section;
    }

    private AppV2SideSectionPanel crearDatosTitularAnalisis() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del titular");
        section.addRow("Titular", lblTitular);
        section.addRow("Tipo documento", lblDatosTipoDocumentoTitular);
        section.addRow("N° documento", lblDatosNumeroDocumentoTitular);
        return section;
    }

    private AppV2SideSectionPanel crearDatosSolicitanteAnalisis() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del solicitante");
        section.addRow("Solicitante", lblDatosSolicitante);
        section.addRow("Tipo documento", lblDatosTipoDocumentoSolicitante);
        section.addRow("N° documento", lblDatosNumeroDocumentoSolicitante);
        return section;
    }

    private AppV2SideSectionPanel crearDatosNotificacionAnalisis() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos de notificación y ubicación");
        section.addRow("Correo", lblDatosCorreo);
        section.addRow("Teléfono", lblDatosTelefono);
        section.addRow("Departamento", lblDatosDepartamento);
        section.addRow("Provincia", lblDatosProvincia);
        section.addRow("Distrito", lblDatosDistrito);
        section.addRow("Dirección", lblDatosDireccion);
        return section;
    }

    private void editarSeleccionActual() {
        AnalisisTableRow fila = obtenerFilaSeleccionada();
        if (fila == null || !fila.esPrincipal() || fila.principal == null || fila.principal.getIdExpediente() == null) {
            mostrarInfo("Seleccione un expediente principal para editar sus datos.");
            return;
        }
        mostrarEdicionManual(fila.principal.getIdExpediente());
    }

    private void mostrarEdicionManual(final Long idExpediente) {
        if (tabsAnalisis == null || idExpediente == null) {
            return;
        }
        JPanelRegistroManualRecepcionV2 panelEdicion = new JPanelRegistroManualRecepcionV2(
                idExpediente,
                new Runnable() {
                    @Override
                    public void run() {
                        restaurarBandejaAnalisis(idExpediente);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        restaurarBandejaAnalisis(null);
                    }
                },
                true);
        if (tabsAnalisis.getTabCount() > 1) {
            tabsAnalisis.removeTabAt(1);
        }
        tabsAnalisis.addTab("Edición manual", panelEdicion);
        tabsAnalisis.setSelectedIndex(1);
    }

    private void restaurarBandejaAnalisis(Long idExpedienteASeleccionar) {
        if (tabsAnalisis == null) {
            return;
        }
        if (tabsAnalisis.getTabCount() > 1) {
            tabsAnalisis.removeTabAt(1);
        }
        tabsAnalisis.setSelectedIndex(0);
        if (idExpedienteASeleccionar != null) {
            buscar(idExpedienteASeleccionar);
        } else {
            buscar();
        }
    }

    private JPanel crearFormularioAnalisis() {
        JPanel panel = section("Resultado del análisis");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        int row = 0;
        addRow(grid, row++, "Resultado", cmbResultado);
        addRow(grid, row++, "¿Acta incorporada?", cmbIncorporado);
        addRow(grid, row++, "Motivo no corresponde", cmbMotivoNoCorresponde);
        addRow(grid, row++, "Tipo Documento", cmbTipoDocumentoNoCorresponde);
        addRow(grid, row++, "N° Documento", txtNumeroDocumentoProveido);

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

    private JPanel crearResumenAnalisisUnico() {
        JPanel panel = section("Análisis");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblUnico = new JLabel("El expediente se gestiona con un único análisis.");
        lblUnico.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblUnico.setForeground(AppV2Theme.TEXT_SECONDARY);
        addRow(grid, 0, "Análisis actual", lblUnico);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDocumentosPanel() {
        JPanel panel = section("Documentos Analizados");
        documentosTreePanel = new DocumentoAnalisisTreeGridPanelV2();
        documentosTreePanel.setHandlers(
                documento -> {
                    AnalisisExpedienteDTO item = requerirSeleccion("Seleccione un expediente para guardar el documento.");
                    if (item == null) {
                        throw new IllegalArgumentException("Seleccione un expediente para guardar el documento.");
                    }
                    if (!puedeGuardarDocumentos(item)) {
                        throw new IllegalArgumentException("El expediente seleccionado no permite guardar documentos de análisis.");
                    }
                    return analisisService.guardarDocumentoAnalisisJerarquico(item.getIdExpediente(), documento);
                },
                (idExp, ids) -> {
                    if (idExp == null) {
                        throw new IllegalArgumentException("Seleccione un expediente para eliminar el documento.");
                    }
                    return analisisService.darBajaDocumentosAnalisis(idExp, ids);
                },
                this::descargarPlantillaDocumento,
                this::recargarDetalleSeleccionado);
        panel.add(documentosTreePanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPublicacionLecturaPanel() {
        JPanel panel = section("Publicación prevista");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        int row = 0;
        addRow(grid, row++, "Requiere publicación", lblRequierePublicacion);
        addRow(grid, row++, "Fecha de publicación", lblFechaPublicacion);
        addRow(grid, row, "Origen", lblFuentePublicacion);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearExpedienteDigitalPanel() {
        JPanel panel = section("Expediente digital");
        lblExpedienteDigital.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblExpedienteDigital.setForeground(AppV2Theme.TEXT_SECONDARY);
        panel.add(lblExpedienteDigital, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDocumentosAsociadosPanel() {
        JPanel panel = section("Solicitudes asociadas");
        lblDocumentosAsociados.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblDocumentosAsociados.setForeground(AppV2Theme.TEXT_SECONDARY);

        JScrollPane scroll = new JScrollPane(documentosAsociadosTable);
        scroll.setPreferredSize(new Dimension(320, 112));
        scroll.setMinimumSize(new Dimension(270, 88));
        scroll.setMaximumSize(new Dimension(330, 150));
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.setOpaque(false);
        content.add(lblDocumentosAsociados, BorderLayout.NORTH);
        JPanel tablaCompacta = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tablaCompacta.setOpaque(false);
        tablaCompacta.add(scroll);
        content.add(tablaCompacta, BorderLayout.CENTER);
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
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
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
        cmbTipoDocumentoNoCorresponde.setPreferredSize(new Dimension(260, 34));
        cmbTipoDocumentoNoCorresponde.setEnabled(false);
        txtNumeroDocumentoProveido.setPreferredSize(new Dimension(260, 34));
        txtNumeroDocumentoProveido.setToolTipText("Ingrese el número del documento para No corresponde.");
        txtNumeroDocumentoProveido.setEnabled(false);
        cmbTipoDocumento.setPreferredSize(new Dimension(260, 34));
        cmbEstadoDocumento.setPreferredSize(new Dimension(260, 34));
        fechaDocumentoAnalizado.setPreferredSize(new Dimension(260, 40));
        fechaDocumentoAnalizado.setMinimumSize(new Dimension(220, 40));
        fechaDocumentoAnalizado.setDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        cmbTipoObservacion.setPreferredSize(new Dimension(260, 34));
        AppV2Theme.estilizarBotonPrimario(btnBuscar);
        AppV2Theme.estilizarBotonPrimario(btnRecibir);
        AppV2Theme.estilizarBotonPrimario(btnEditar);
        AppV2Theme.estilizarBotonPrimario(btnRegistrarAnalisis);
        AppV2Theme.estilizarBotonPrimario(btnEnviarVerificacion);
        AppV2Theme.estilizarBotonPrimario(btnArchivarNoCorresponde);
        AppV2Theme.estilizarBotonPrimario(btnDescargarPlantillaSeleccionada);
        chkReconstitucion.setOpaque(false);
        chkLegitimidad.setOpaque(false);
        chkMediosProbatorios.setOpaque(false);
        chkRegistrarObservacion.setOpaque(false);
        chkDocumentoRequiereRespuesta.setOpaque(false);
        configurarLabelsDatosAnalisis();
    }

    private void configurarLabelsDatosAnalisis() {
        JLabel[] labels = new JLabel[]{
            lblExpediente,
            lblTitular,
            lblActa,
            lblProcedimiento,
            lblResponsable,
            lblEtapaEstado,
            lblAlertas,
            lblDatosResultadoInicial,
            lblDatosTramiteWeb,
            lblDatosNumeroDocumentoTitular,
            lblDatosExpedienteSgd,
            lblDatosFechaRecepcion,
            lblDatosTipoActa,
            lblDatosNumeroActa,
            lblDatosTipoDocumentoTitular,
            lblDatosTipoDocumentoSolicitante,
            lblDatosNumeroDocumentoSolicitante,
            lblDatosSolicitante,
            lblDatosCorreo,
            lblDatosTelefono,
            lblDatosDepartamento,
            lblDatosProvincia,
            lblDatosDistrito,
            lblDatosDireccion,
            lblDatosVencimiento,
            lblDatosTipoDocumentoSolicitud,
            lblDatosNumeroDocumentoSolicitud,
            lblDatosEquipo,
            lblRequierePublicacion,
            lblFechaPublicacion,
            lblFuentePublicacion,
            lblPlantillaSeleccionada,
            lblPlantillaAyuda
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
        table.setDefaultRenderer(Object.class, new AnalisisRenderer());
        AppV2TableColumnSizer.applyFriendlyDefaults(table);
        AppV2TableColumnSizer.applyWidths(table, 46, 88, 185, 150, 145, 220, 130, 130, 260, 210, 155, 160, 0);
        table.getColumnModel().getColumn(COL_EXPANDIR).setMinWidth(42);
        table.getColumnModel().getColumn(COL_EXPANDIR).setPreferredWidth(46);
        table.getColumnModel().getColumn(COL_EXPANDIR).setMaxWidth(48);
        table.getColumnModel().getColumn(COL_EXPANDIR).setCellRenderer(new ExpandirRenderer());
        tablePanel.getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        columnFilterSupport = AppV2ColumnFilterSupport.install(
                "Analisis",
                table,
                tablePanel.getScrollPane(),
                tablePanel,
                () -> contraerTodosExcepto(null),
                COL_EXPANDIR,
                12);
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
        documentosAsociadosTable.getColumnModel().getColumn(0).setPreferredWidth(170);
        documentosAsociadosTable.getColumnModel().getColumn(0).setMinWidth(140);
        documentosAsociadosTable.getColumnModel().getColumn(1).setPreferredWidth(125);
        documentosAsociadosTable.getColumnModel().getColumn(1).setMinWidth(105);
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnRefrescar.addActionListener(e -> buscar());
        btnRecibir.addActionListener(e -> recibir());
        btnEditar.addActionListener(e -> editarSeleccionActual());
        btnRegistrarAnalisis.addActionListener(e -> registrarAnalisis());
        btnCancelarAnalisis.addActionListener(e -> cerrarPanelAnalisis());
        btnEnviarVerificacion.addActionListener(e -> enviarVerificacion());
        btnArchivarNoCorresponde.addActionListener(e -> archivarNoCorresponde());
        cmbIncorporado.addActionListener(e -> actualizarChecksIncorporado());
        cmbResultado.addActionListener(e -> actualizarResultadoSeleccionado());
        chkRegistrarObservacion.addActionListener(e -> actualizarObservacionHabilitada());
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
                    panelAnalisisCerradoPorUsuario = false;
                    if (splitOperativo != null) {
                        splitOperativo.setSideVisible(true);
                    }
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
        cmbTipoDocumentoNoCorresponde.removeAllItems();
        cmbTipoDocumentoNoCorresponde.addItem(new SimpleItem("", "Seleccione tipo"));
        cmbTipoDocumentoNoCorresponde.addItem(new SimpleItem("PROVEIDO", "Proveido"));
        cmbTipoDocumentoNoCorresponde.addItem(new SimpleItem("HOJA_ENVIO", "Hoja de Envío"));
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
        if (documentosTreePanel != null) {
            documentosTreePanel.setCatalogos(carga.tiposDocumento, carga.estadosDocumento);
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
        buscar(null);
    }

    private void buscar(Long idExpedienteASeleccionar) {
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
                    if (secuencia != secuenciaBusqueda.get()) {
                        return;
                    }
                    cargarTabla(get());
                    if (idExpedienteASeleccionar != null) {
                        seleccionarFilaPorId(idExpedienteASeleccionar);
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar la bandeja de análisis.", ex);
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

    private void cargarTabla(List<AnalisisExpedienteDTO> items) {
        expedientes.clear();
        expedientes.addAll(items);
        List<AnalisisExpedienteDTO> visibles = filtrarKpi(items);
        filasTabla.clear();
        asociadosCache.clear();
        principalesExpandidos.clear();
        principalesCargando.clear();
        idExpedienteExpansionActiva = null;
        tableModel.setRowCount(0);
        table.clearSelection();
        int porRecibir = 0;
        int enAnalisis = 0;
        int cartasIntermedias = 0;
        int observados = 0;
        int vencimientoCritico = 0;
        for (AnalisisExpedienteDTO item : expedientes) {
            if (item.isRecibible()) {
                porRecibir++;
            }
            if (item.isRegistrable() || item.isEnviableVerificacion()) {
                enAnalisis++;
            }
            if (item.getTotalDocumentosAnalizados() > 0 && !item.isEnviableVerificacion()) {
                cartasIntermedias++;
            }
            if ("OBSERVADO".equalsIgnoreCase(item.getEstadoCodigo())) {
                observados++;
            }
            if (item.getDiasEnEtapa() != null && item.getDiasEnEtapa() <= 3) {
                vencimientoCritico++;
            }
        }
        cardPorRecibir.setValue(String.valueOf(porRecibir));
        cardEnAnalisis.setValue(String.valueOf(enAnalisis));
        cardCartaIntermedia.setValue(String.valueOf(cartasIntermedias));
        cardObservados.setValue(String.valueOf(observados));
        cardVencimiento.setValue(String.valueOf(vencimientoCritico));
        marcarKpis();
        cargarTablaVisible(visibles);
        lblEstado.setText(items.isEmpty()
                ? "No se encontraron expedientes para análisis."
                : visibles.size() + " expediente(s) encontrados.");
        tablePanel.setEmpty(visibles.isEmpty());
        actualizarSeleccion();
    }

    private void cargarTablaVisible(List<AnalisisExpedienteDTO> items) {
        if (items == null) {
            return;
        }
        for (AnalisisExpedienteDTO item : items) {
            agregarFilaPrincipal(item);
        }
    }

    private List<AnalisisExpedienteDTO> filtrarKpi(List<AnalisisExpedienteDTO> items) {
        List<AnalisisExpedienteDTO> filtrados = new ArrayList<AnalisisExpedienteDTO>();
        if (items == null || items.isEmpty() || kpiActivo == FiltroKpi.TODOS) {
            if (items != null) {
                filtrados.addAll(items);
            }
            return filtrados;
        }
        for (AnalisisExpedienteDTO item : items) {
            if (coincideKpi(item)) {
                filtrados.add(item);
            }
        }
        return filtrados;
    }

    private boolean coincideKpi(AnalisisExpedienteDTO item) {
        switch (kpiActivo) {
            case POR_RECIBIR:
                return item.isRecibible();
            case EN_ANALISIS:
                return item.isRegistrable() || item.isEnviableVerificacion();
            case CARTA_INTERMEDIA:
                return item.getTotalDocumentosAnalizados() > 0 && !item.isEnviableVerificacion();
            case OBSERVADOS:
                return "OBSERVADO".equalsIgnoreCase(item.getEstadoCodigo());
            case VENCIMIENTO_CRITICO:
                return item.getDiasEnEtapa() != null && item.getDiasEnEtapa() <= 3;
            case TODOS:
            default:
                return true;
        }
    }

    private void configurarKpisInteractivos() {
        cardPorRecibir.setOnClick(() -> activarKpi(FiltroKpi.POR_RECIBIR));
        cardEnAnalisis.setOnClick(() -> activarKpi(FiltroKpi.EN_ANALISIS));
        cardCartaIntermedia.setOnClick(() -> activarKpi(FiltroKpi.CARTA_INTERMEDIA));
        cardObservados.setOnClick(() -> activarKpi(FiltroKpi.OBSERVADOS));
        cardVencimiento.setOnClick(() -> activarKpi(FiltroKpi.VENCIMIENTO_CRITICO));
        marcarKpis();
    }

    private void activarKpi(FiltroKpi filtro) {
        kpiActivo = filtro;
        cargarTabla(new java.util.ArrayList<AnalisisExpedienteDTO>(expedientes));
        marcarKpis();
    }

    private void marcarKpis() {
        cardPorRecibir.setSelected(kpiActivo == FiltroKpi.POR_RECIBIR);
        cardEnAnalisis.setSelected(kpiActivo == FiltroKpi.EN_ANALISIS);
        cardCartaIntermedia.setSelected(kpiActivo == FiltroKpi.CARTA_INTERMEDIA);
        cardObservados.setSelected(kpiActivo == FiltroKpi.OBSERVADOS);
        cardVencimiento.setSelected(kpiActivo == FiltroKpi.VENCIMIENTO_CRITICO);
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
        limpiarFormulario();
        cardPorRecibir.setValue("0");
        cardEnAnalisis.setValue("0");
        cardCartaIntermedia.setValue("0");
        cardObservados.setValue("0");
        cardVencimiento.setValue("0");
        marcarKpis();
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
            valorUi(item.getNumeroExpedienteSgd()),
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
            valorUi(asociado.getNumeroExpedienteSgd()),
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

    private boolean seleccionarFilaPorId(Long idExpediente) {
        if (idExpediente == null) {
            return false;
        }
        for (int i = 0; i < filasTabla.size(); i++) {
            AnalisisTableRow row = filasTabla.get(i);
            if (row != null && idExpediente.equals(row.getIdExpediente())) {
                int viewRow = table.convertRowIndexToView(i);
                if (viewRow >= 0 && viewRow < table.getRowCount()) {
                    table.setRowSelectionInterval(viewRow, viewRow);
                    table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                    return true;
                }
            }
        }
        return false;
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
        btnRecibir.setEnabled(has && !asociado && item.isRecibible());
        btnEditar.setEnabled(has && !asociado);
        btnRegistrarAnalisis.setEnabled(has && !asociado && item.isRegistrable());
        btnEnviarVerificacion.setEnabled(has && !asociado && item.isEnviableVerificacion());
        btnArchivarNoCorresponde.setEnabled(has && !asociado && item.isArchivableNoCorresponde());
        actualizarVisibilidadPanelAnalisis();
        if (!has) {
            idExpedienteDetalleSolicitado = null;
            idExpedienteDetalleCargado = null;
            lblExpediente.setText("-");
            lblTitular.setText("-");
            actualizarTituloPanelAnalisis(null);
            lblActa.setText("-");
            lblProcedimiento.setText("-");
            lblResponsable.setText("-");
            lblEtapaEstado.setText("-");
            lblAlertas.setText("Sin alertas.");
            limpiarDatosExpedienteAnalisis();
            limpiarDocumentosAsociadosPanel("Sin documentos asociados.");
            limpiarPublicacionLectura();
            lblExpedienteDigital.setText("Pendiente para módulo Expediente digital.");
            limpiarFormulario();
            idExpedienteRecepcionPreguntada = null;
            return;
        }
        if (asociado) {
            idExpedienteRecepcionPreguntada = null;
            idExpedienteDetalleSolicitado = null;
            idExpedienteDetalleCargado = null;
            limpiarFormulario();
            ExpedienteRelacionadoDTO relacionado = fila.asociado;
            lblExpediente.setText("Documento asociado seleccionado");
            lblTitular.setText(valorUi(relacionado.getTitular()));
            actualizarTituloPanelAnalisis(valorUi(relacionado.getTitular()));
            lblActa.setText((valorUi(relacionado.getTipoActa()) + " " + valorUi(relacionado.getNumeroActa())).trim());
            lblProcedimiento.setText(procedimientoAsociado(relacionado));
            lblResponsable.setText(valorUi(relacionado.getAbogadoAsignado()));
            lblEtapaEstado.setText("Expediente principal: " + fila.numeroExpedientePrincipal());
            lblAlertas.setText(textoRelacionAsociada(relacionado) + " · Disponible para análisis");
            cargarDatosExpedienteAnalisis(null);
            txtComentarioMovimiento.setText("Este documento está asociado al expediente principal y se muestra como contexto del caso.");
            limpiarPublicacionLectura();
            lblExpedienteDigital.setText("El documento asociado se muestra como contexto del expediente principal.");
            cargarDocumentosAsociadosPanel(fila.principal);
            return;
        }
        lblExpediente.setText(item.getNumeroExpediente());
        lblTitular.setText(item.getTitular());
        actualizarTituloPanelAnalisis(item.getTitular());
        lblActa.setText((item.getTipoActa() + " " + item.getNumeroActa()).trim());
        lblProcedimiento.setText(item.getProcedimiento());
        lblResponsable.setText(item.getResponsable().isEmpty() ? "-" : item.getResponsable());
        lblEtapaEstado.setText(DisplayNameMapperV2.etapa(item.getEtapaCodigo()) + " / " + DisplayNameMapperV2.estado(item.getEstadoCodigo()));
        lblAlertas.setText(alertas(item));
        cargarDatosExpedienteAnalisis(item);
        txtComentarioMovimiento.setText("");
        lblExpedienteDigital.setText("Registro de enlace/carpeta se gestiona desde el módulo Expediente digital.");
        cargarDocumentosAsociadosPanel(item);
        cargarAnalisisRegistrado(item);
        preguntarRecepcionSiCorresponde(item);
    }

    private void limpiarDatosExpedienteAnalisis() {
        JLabel[] labels = new JLabel[]{
            lblDatosResultadoInicial,
            lblDatosHojaEnvio,
            lblDatosTramiteWeb,
            lblDatosNumeroDocumentoTitular,
            lblDatosExpedienteSgd,
            lblDatosTipoSolicitud,
            lblDatosFechaRecepcion,
            lblDatosCanalIngreso,
            lblDatosPrioridad,
            lblDatosMarcaOperativa,
            lblDatosExpediente,
            lblDatosEstado,
            lblDatosObservacion,
            lblDatosTipoActa,
            lblDatosNumeroActa,
            lblDatosTipoDocumentoTitular,
            lblDatosTipoDocumentoSolicitante,
            lblDatosNumeroDocumentoSolicitante,
            lblDatosSolicitante,
            lblDatosCorreo,
            lblDatosTelefono,
            lblDatosDepartamento,
            lblDatosProvincia,
            lblDatosDistrito,
            lblDatosDireccion,
            lblDatosVencimiento,
            lblDatosTipoDocumentoSolicitud,
            lblDatosNumeroDocumentoSolicitud,
            lblDatosEquipo
        };
        for (JLabel label : labels) {
            label.setText("-");
            label.setToolTipText(null);
        }
        actualizarBadgeDias(lblDatosDias, null);
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

    private void cargarDatosExpedienteAnalisis(AnalisisExpedienteDTO item) {
        if (item == null) {
            limpiarDatosExpedienteAnalisis();
            return;
        }
        lblDatosResultadoInicial.setText("Corresponde a SDRERC");
        lblDatosHojaEnvio.setText(valorUi(item.getNumeroHojaEnvioAsignacion()));
        lblDatosTramiteWeb.setText(valorUi(item.getNumeroTramiteDocumentario()));
        lblDatosNumeroDocumentoTitular.setText(valorUi(item.getNumeroDocumentoTitular()));
        lblDatosExpedienteSgd.setText(valorUi(item.getNumeroExpedienteSgd()));
        lblDatosTipoSolicitud.setText(extraerValorObservacion(item.getObservacionSolicitud(), "Tipo de solicitud"));
        lblDatosFechaRecepcion.setText(formatDate(item.getFechaRecepcion()));
        lblDatosCanalIngreso.setText(valorUi(item.getCanalIngreso()));
        lblDatosPrioridad.setText(valorUi(item.getPrioridad()));
        lblDatosMarcaOperativa.setText(valorUi(item.getGrupoFamiliarEstado()));
        lblDatosExpediente.setText(valorUi(item.getNumeroExpediente()));
        lblDatosEstado.setText(DisplayNameMapperV2.estado(item.getEstadoCodigo()));
        lblDatosObservacion.setText(valorUi(item.getObservacionSolicitud()));
        lblDatosTipoActa.setText(valorUi(item.getTipoActa()));
        lblDatosNumeroActa.setText(valorUi(item.getNumeroActa()));
        lblDatosTipoDocumentoTitular.setText(valorUi(item.getTipoDocumentoTitular()));
        lblDatosTipoDocumentoSolicitud.setText(valorUi(item.getTipoDocumento()));
        lblDatosNumeroDocumentoSolicitud.setText(valorUi(item.getNumeroDocumento()));
        lblDatosTipoDocumentoSolicitante.setText(valorUi(item.getTipoDocumentoSolicitante()));
        lblDatosNumeroDocumentoSolicitante.setText(valorUi(item.getNumeroDocumentoSolicitante()));
        lblDatosSolicitante.setText(valorUi(item.getSolicitante()));
        lblDatosCorreo.setText(valorUi(item.getCorreoSolicitante()));
        lblDatosTelefono.setText(valorUi(item.getTelefonoSolicitante()));
        lblDatosDepartamento.setText(valorUi(item.getDepartamentoSolicitante()));
        lblDatosProvincia.setText(valorUi(item.getProvinciaSolicitante()));
        lblDatosDistrito.setText(valorUi(item.getDistritoSolicitante()));
        lblDatosDireccion.setText(valorUi(item.getDireccionSolicitante()));
        lblDatosDireccion.setToolTipText(valorUi(item.getDireccionSolicitante()));
        actualizarBadgeDias(lblDatosDias, item.getDiasEnEtapa());
        lblDatosVencimiento.setText(formatDate(item.getFechaVencimiento()));
        lblDatosEquipo.setText(valorUi(item.getEquipo()));
    }

    private static String extraerValorObservacion(String observacion, String etiqueta) {
        if (observacion == null || etiqueta == null) {
            return "-";
        }
        String prefix = etiqueta.trim().toLowerCase() + ":";
        String[] partes = observacion.split("\\|");
        for (String parte : partes) {
            String texto = parte == null ? "" : parte.trim();
            if (texto.toLowerCase().startsWith(prefix)) {
                String valor = texto.substring(prefix.length()).trim();
                return valor.isEmpty() ? "-" : valor;
            }
        }
        return "-";
    }

    private boolean puedeGuardarDocumentos(AnalisisExpedienteDTO item) {
        return item != null
                && "ANALISIS".equalsIgnoreCase(item.getEtapaCodigo())
                && ("RECIBIDO_POR_ABOGADO".equalsIgnoreCase(item.getEstadoCodigo())
                || "OBSERVADO".equalsIgnoreCase(item.getEstadoCodigo())
                || "SUBSANADO".equalsIgnoreCase(item.getEstadoCodigo())
                || "ATENDIDO".equalsIgnoreCase(item.getEstadoCodigo()));
    }

    private void cargarAnalisisRegistrado(AnalisisExpedienteDTO item) {
        if (item == null || item.getIdExpediente() == null) {
            limpiarFormulario();
            return;
        }
        Long idExpediente = item.getIdExpediente();
        if (idExpediente.equals(idExpedienteDetalleCargado)
                || idExpediente.equals(idExpedienteDetalleSolicitado)) {
            return;
        }
        idExpedienteDetalleSolicitado = idExpediente;
        limpiarFormulario();
        lblFechaAnalisis.setText("Cargando...");
        SwingWorker<List<AnalisisItemDTO>, Void> worker = new SwingWorker<List<AnalisisItemDTO>, Void>() {
            @Override
            protected List<AnalisisItemDTO> doInBackground() throws Exception {
                return analisisService.listarAnalisisPorExpediente(idExpediente);
            }

            @Override
            protected void done() {
                if (!idExpediente.equals(idExpedienteDetalleSolicitado)
                        || !esExpedientePrincipalSeleccionado(idExpediente)) {
                    return;
                }
                try {
                    idExpedienteDetalleCargado = idExpediente;
                    aplicarBloquesAnalisis(get());
                } catch (Exception ex) {
                    idExpedienteDetalleCargado = null;
                    limpiarFormulario();
                    mostrarError("No se pudo cargar el análisis registrado.", ex);
                } finally {
                    if (idExpediente.equals(idExpedienteDetalleSolicitado)) {
                        idExpedienteDetalleSolicitado = null;
                    }
                }
            }
        };
        worker.execute();
    }

    private boolean esExpedientePrincipalSeleccionado(Long idExpediente) {
        AnalisisTableRow fila = obtenerFilaSeleccionada();
        return fila != null
                && fila.esPrincipal()
                && fila.principal != null
                && idExpediente.equals(fila.principal.getIdExpediente());
    }

    private void aplicarAnalisisRegistrado(AnalisisDetalleDTO detalle) {
        limpiarFormulario();
        if (detalle == null) {
            return;
        }
        cargandoDetalleAnalisis = true;
        try {
            seleccionarResultado(detalle.getResultadoCodigo());
            seleccionarSimpleItem(
                    cmbIncorporado,
                    detalle.getIncorporado() == null
                            ? ""
                            : Boolean.TRUE.equals(detalle.getIncorporado()) ? "SI" : "NO");
            seleccionarSimpleItem(cmbMotivoNoCorresponde, detalle.getMotivoNoCorrespondeCodigo());
            seleccionarSimpleItem(cmbTipoDocumentoNoCorresponde, detalle.getTipoDocumentoNoCorrespondeCodigo());
            txtFundamento.setText(detalle.getFundamento());
            txtNumeroDocumentoProveido.setText(detalle.getNumeroDocumentoProveido());
            chkReconstitucion.setSelected(detalle.isRequiereReconstitucion());
            chkLegitimidad.setSelected(detalle.isTieneLegitimidad());
            chkMediosProbatorios.setSelected(detalle.isCumpleMediosProbatorios());
            lblFechaAnalisis.setText(detalle.getFechaEvaluacion() == null
                    ? DATE_FORMAT.format(LocalDate.now())
                    : DATE_FORMAT.format(detalle.getFechaEvaluacion()));
            cargarDocumentosAnalizados(detalle.getDocumentosAnalizados());
        } finally {
            cargandoDetalleAnalisis = false;
        }
        actualizarResultadoSeleccionado();
        ObservacionAnalisisDTO observacion = detalle.getObservacion();
        boolean tieneObservacion = observacion != null && observacion.hasDescripcion();
        chkRegistrarObservacion.setSelected(tieneObservacion || resultadoRequiereObservacion(
                (ResultadoItem) cmbResultado.getSelectedItem()));
        if (observacion != null) {
            seleccionarSimpleItem(cmbTipoObservacion, observacion.getTipoObservacionCodigo());
            txtObservacion.setText(observacion.getDescripcion());
        }
        if (!detalle.isRegistrado()
                && (detalle.getDocumentosAnalizados() == null || detalle.getDocumentosAnalizados().isEmpty())
                && cmbEstadoDocumento.getItemCount() > 1) {
            cmbEstadoDocumento.setSelectedIndex(1);
        }
        actualizarObservacionHabilitada();
    }

    private void cargarDocumentosAnalizados(List<DocumentoAnalizadoDTO> documentos) {
        if (documentosTreePanel != null) {
            documentosTreePanel.setDocumentos(idExpedienteDetalleCargado, idAnalisisSeleccionado, documentos);
        }
        actualizarPublicacionLectura(documentos);
    }

    private void actualizarPublicacionLectura(List<DocumentoAnalizadoDTO> documentos) {
        limpiarPublicacionLectura();
        if (documentos == null || documentos.isEmpty()) {
            return;
        }
        DocumentoAnalizadoDTO seleccionado = null;
        for (DocumentoAnalizadoDTO documento : documentos) {
            if (documento != null && (documento.isRequierePublicacion() || documento.getFechaPublicacion() != null)) {
                seleccionado = documento;
                break;
            }
        }
        if (seleccionado == null) {
            return;
        }
        lblRequierePublicacion.setText(seleccionado.isRequierePublicacion() ? "Sí" : "No");
        lblFechaPublicacion.setText(seleccionado.getFechaPublicacion() == null
                ? "-"
                : DATE_FORMAT.format(seleccionado.getFechaPublicacion()));
        lblFuentePublicacion.setText("Dato registrado desde Asignación. Solo lectura en Análisis.");
    }

    private void limpiarPublicacionLectura() {
        lblRequierePublicacion.setText("No");
        lblFechaPublicacion.setText("-");
        lblFuentePublicacion.setText("Dato registrado desde Asignación. Solo lectura en Análisis.");
    }

    private void seleccionarResultado(String codigo) {
        String value = codigo == null ? "" : codigo.trim();
        for (int i = 0; i < cmbResultado.getItemCount(); i++) {
            ResultadoItem item = cmbResultado.getItemAt(i);
            if (item != null && item.codigo.equalsIgnoreCase(value)) {
                cmbResultado.setSelectedIndex(i);
                return;
            }
        }
        if (cmbResultado.getItemCount() > 0) {
            cmbResultado.setSelectedIndex(0);
        }
    }

    private void seleccionarSimpleItem(JComboBox<SimpleItem> combo, String codigo) {
        String value = codigo == null ? "" : codigo.trim();
        for (int i = 0; i < combo.getItemCount(); i++) {
            SimpleItem item = combo.getItemAt(i);
            if (item != null && item.codigo.equalsIgnoreCase(value)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    private void cerrarPanelAnalisis() {
        panelAnalisisCerradoPorUsuario = true;
        if (splitOperativo != null) {
            splitOperativo.setSideVisible(false);
        }
        actualizarLenguetasAnalisis();
    }

    private void actualizarVisibilidadPanelAnalisis() {
        if (splitOperativo == null) {
            return;
        }
        if (!splitOperativo.isSideVisible()) {
            return;
        }
        splitOperativo.setSideVisible(obtenerFilaSeleccionada() != null && !panelAnalisisCerradoPorUsuario);
        actualizarLenguetasAnalisis();
    }

    private void alternarExpansionPanelAnalisis() {
        if (splitOperativo == null || !splitOperativo.isSideVisible()) {
            return;
        }
        splitOperativo.toggleSideExpanded();
        actualizarLenguetasAnalisis();
        revalidate();
        repaint();
    }

    private void seleccionarTabAnalisis(String tab) {
        if (tab == null || panelAnalisisCardsLayout == null || panelAnalisisCards == null) {
            return;
        }
        if (!TAB_ANALISIS_DATOS.equals(tab)
                && !TAB_ANALISIS_DOCUMENTOS.equals(tab)) {
            return;
        }
        boolean mismaTab = tab.equals(tabAnalisisActiva);
        tabAnalisisActiva = tab;
        panelAnalisisCardsLayout.show(panelAnalisisCards, tab);
        if (splitOperativo != null && splitOperativo.isSideVisible() && mismaTab) {
            splitOperativo.setSideExpanded(!splitOperativo.isSideExpanded());
        }
        actualizarLenguetasAnalisis();
        panelAnalisisCards.revalidate();
        panelAnalisisCards.repaint();
    }

    private void actualizarLenguetasAnalisis() {
        boolean expandido = splitOperativo != null && splitOperativo.isSideExpanded();
        tabDatosAnalisis.setState(TAB_ANALISIS_DATOS.equals(tabAnalisisActiva), TAB_ANALISIS_DATOS.equals(tabAnalisisActiva) && expandido);
        tabDocumentosAnalisis.setState(TAB_ANALISIS_DOCUMENTOS.equals(tabAnalisisActiva), TAB_ANALISIS_DOCUMENTOS.equals(tabAnalisisActiva) && expandido);
        tabDatosAnalisis.setToolTipText("Datos del expediente");
        tabDocumentosAnalisis.setToolTipText("Documentos y resultado del análisis");
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
        mostrarSolicitudesAsociadas(true);
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
                    valorUi(asociado.getNumeroExpedienteSgd()),
                    recibido ? "Recibido" : "Pendiente de recibir"
                });
            }
        }
        if (documentosAsociadosPanel.isEmpty()) {
            mostrarSolicitudesAsociadas(false);
            lblDocumentosAsociados.setForeground(AppV2Theme.TEXT_SECONDARY);
            lblDocumentosAsociados.setText("Sin documentos asociados.");
        } else if (pendientes > 0) {
            mostrarSolicitudesAsociadas(true);
            lblDocumentosAsociados.setForeground(AppV2Theme.WARNING);
            lblDocumentosAsociados.setText(
                    pendientes + " documento(s) asociado(s) pendiente(s) de recibir.");
        } else {
            mostrarSolicitudesAsociadas(true);
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
        mostrarSolicitudesAsociadas(false);
        lblDocumentosAsociados.setForeground(AppV2Theme.TEXT_SECONDARY);
        lblDocumentosAsociados.setText(mensaje == null || mensaje.trim().isEmpty()
                ? "Sin documentos asociados."
                : mensaje);
    }

    private void mostrarSolicitudesAsociadas(boolean visible) {
        if (panelSolicitudesAsociadas == null) {
            return;
        }
        panelSolicitudesAsociadas.setVisible(visible);
        if (panelAnalisis != null) {
            panelAnalisis.revalidate();
            panelAnalisis.repaint();
        }
    }

    private boolean puedeRecibirDocumentoAsociado(int modelRow) {
        if (modelRow < 0 || modelRow >= documentosAsociadosPanel.size()) {
            return false;
        }
        ExpedienteRelacionadoDTO asociado = documentosAsociadosPanel.get(modelRow);
        return !asociado.isRecibidoPorAbogado()
                && "ASIGNACION".equalsIgnoreCase(asociado.getEtapaCodigo())
                && "ASIGNADO".equalsIgnoreCase(asociado.getEstadoCodigo());
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
            mostrarInfo("El documento asociado no se encuentra disponible para recibir.");
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

    private void preguntarRecepcionSiCorresponde(AnalisisExpedienteDTO item) {
        if (item == null || item.getIdExpediente() == null || !item.isRecibible()) {
            return;
        }
        if (item.getIdExpediente().equals(idExpedienteRecepcionPreguntada)) {
            return;
        }
        idExpedienteRecepcionPreguntada = item.getIdExpediente();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de recibir el expediente asignado?",
                "Recibir expediente",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) {
            idExpedienteRecepcionPreguntada = null;
            return;
        }
        setTrabajando(true, "Recibiendo expediente asignado...");
        SwingWorker<AnalisisResultadoDTO, Void> worker = new SwingWorker<AnalisisResultadoDTO, Void>() {
            @Override
            protected AnalisisResultadoDTO doInBackground() throws Exception {
                return analisisService.recibirExpediente(item.getIdExpediente(), txtComentarioMovimiento.getText());
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
                    buscar(item.getIdExpediente());
                } catch (Exception ex) {
                    idExpedienteRecepcionPreguntada = null;
                    mostrarError("No se pudo recibir el expediente asignado.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void registrarAnalisis() {
        AnalisisExpedienteDTO item = requerirSeleccion("Seleccione un expediente para registrar el resultado final.");
        if (item == null) {
            return;
        }
        AnalisisRegistroDTO registro = construirRegistro(item);
        confirmarYEjecutar(
                "Registrar resultado final",
                "Se registrará el resultado final del expediente " + item.getNumeroExpediente() + ". ¿Desea continuar?",
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

    private void confirmarYEjecutar(String titulo, String mensaje, OperacionAnalisis operacion) {
        confirmarYEjecutar(titulo, mensaje, operacion, true);
    }

    private void confirmarYEjecutar(
            String titulo,
            String mensaje,
            OperacionAnalisis operacion,
            boolean limpiarYBuscarAlFinal) {
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
                    if (limpiarYBuscarAlFinal) {
                        limpiarFormulario();
                        buscar();
                    } else {
                        recargarDetalleSeleccionado();
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudo completar la operación de análisis.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void recargarDetalleSeleccionado() {
        AnalisisExpedienteDTO item = obtenerSeleccionado();
        if (item == null || item.getIdExpediente() == null) {
            return;
        }
        idExpedienteDetalleCargado = null;
        idExpedienteDetalleSolicitado = null;
        cargarAnalisisRegistrado(item);
    }

    private AnalisisRegistroDTO construirRegistro(AnalisisExpedienteDTO item) {
        ResultadoItem resultado = (ResultadoItem) cmbResultado.getSelectedItem();
        boolean noCorresponde = esResultadoNoCorresponde(resultado);
        SimpleItem incorporado = (SimpleItem) cmbIncorporado.getSelectedItem();
        ObservacionAnalisisDTO observacion = null;
        if (!noCorresponde
                && (chkRegistrarObservacion.isSelected() || resultadoRequiereObservacion(resultado))) {
            SimpleItem tipoObservacion = (SimpleItem) cmbTipoObservacion.getSelectedItem();
            observacion = new ObservacionAnalisisDTO(
                    tipoObservacion == null ? "" : tipoObservacion.codigo,
                    tipoObservacion == null ? "" : tipoObservacion.nombre,
                    txtObservacion.getText());
        }
        SimpleItem motivo = (SimpleItem) cmbMotivoNoCorresponde.getSelectedItem();
        SimpleItem tipoDocumentoNoCorresponde = (SimpleItem) cmbTipoDocumentoNoCorresponde.getSelectedItem();
        return new AnalisisRegistroDTO(
                item.getIdExpediente(),
                idAnalisisSeleccionado,
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
                noCorresponde && tipoDocumentoNoCorresponde != null ? tipoDocumentoNoCorresponde.codigo : "",
                noCorresponde && tipoDocumentoNoCorresponde != null ? tipoDocumentoNoCorresponde.nombre : "",
                noCorresponde ? txtNumeroDocumentoProveido.getText() : "",
                observacion,
                noCorresponde
                        ? new ArrayList<DocumentoAnalizadoDTO>()
                        : obtenerDocumentosFormulario());
    }

    private List<DocumentoAnalizadoDTO> obtenerDocumentosFormulario() {
        return documentosTreePanel != null
                ? documentosTreePanel.getDocumentosActivos()
                : new ArrayList<DocumentoAnalizadoDTO>();
    }

    private void crearNuevoBloqueAnalisis() {
        mostrarInfo("El módulo de Análisis quedó configurado con un único análisis por expediente.");
    }

    private void descargarPlantillaDocumento(DocumentoAnalizadoDTO documento) {
        AnalisisExpedienteDTO item = requerirSeleccion("Seleccione un expediente para descargar la plantilla.");
        if (item == null) {
            return;
        }
        if (documento == null || documento.getTipoDocumentoNombre().trim().isEmpty()) {
            mostrarInfo("Seleccione el tipo de documento para descargar la plantilla.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Descargar plantilla Word");
        chooser.setSelectedFile(Paths.get(plantillaDocumentoService.nombreSugerido(item, documento)).toFile());
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
        setTrabajando(true, "Generando plantilla Word...");
        SwingWorker<Path, Void> worker = new SwingWorker<Path, Void>() {
            @Override
            protected Path doInBackground() throws Exception {
                return plantillaDocumentoService.generarDocumento(item, documento, destinoFinal);
            }

            @Override
            protected void done() {
                try {
                    Path generado = get();
                    JOptionPane.showMessageDialog(
                            JPanelAnalisisV2.this,
                            "Plantilla descargada en:\n" + generado.toAbsolutePath(),
                            "Descargar plantilla",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    mostrarError("No se pudo descargar la plantilla del documento.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void aplicarBloquesAnalisis(List<AnalisisItemDTO> items) {
        cargandoBloquesAnalisis = true;
        bloquesAnalisis.clear();
        AnalisisItemDTO unico = null;
        if (items != null && !items.isEmpty()) {
            unico = items.get(0);
        }
        if (unico == null) {
            unico = new AnalisisItemDTO(null, 1, "Análisis 1", "TEMPORAL", null);
        }
        bloquesAnalisis.add(unico);
        cargandoBloquesAnalisis = false;
        idAnalisisSeleccionado = unico.getIdExpedienteAnalisis();
        seleccionarBloqueAnalisisActual();
    }

    private void seleccionarBloqueAnalisisActual() {
        if (cargandoBloquesAnalisis) {
            return;
        }
        AnalisisItemDTO item = bloquesAnalisis.isEmpty() ? null : bloquesAnalisis.get(0);
        idAnalisisSeleccionado = item == null ? null : item.getIdExpedienteAnalisis();
        aplicarAnalisisRegistrado(item == null ? null : item.getDetalle());
    }

    private String etiquetaAnalisisActual() {
        if (!bloquesAnalisis.isEmpty() && bloquesAnalisis.get(0) != null) {
            return bloquesAnalisis.get(0).getTitulo();
        }
        return "Análisis 1";
    }

    private String etiquetaAnalisisDocumento(DocumentoAnalizadoDTO documento) {
        return documento == null
                ? etiquetaAnalisisActual()
                : etiquetaAnalisisDocumento(documento.getIdExpedienteAnalisis());
    }

    private String etiquetaAnalisisDocumento(Long idExpedienteAnalisis) {
        if (!bloquesAnalisis.isEmpty() && bloquesAnalisis.get(0) != null) {
            return bloquesAnalisis.get(0).getTitulo();
        }
        return "Análisis 1";
    }

    private boolean tieneExtensionWord(Path path) {
        String nombre = path == null || path.getFileName() == null
                ? ""
                : path.getFileName().toString().toLowerCase();
        return nombre.endsWith(".docx") || nombre.endsWith(".doc");
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
        if (cargandoCatalogos || cargandoDetalleAnalisis) {
            return;
        }
        ResultadoItem resultado = (ResultadoItem) cmbResultado.getSelectedItem();
        boolean noCorresponde = esResultadoNoCorresponde(resultado);
        cmbMotivoNoCorresponde.setEnabled(noCorresponde);
        cmbTipoDocumentoNoCorresponde.setEnabled(noCorresponde);
        txtNumeroDocumentoProveido.setEnabled(noCorresponde);
        cmbIncorporado.setEnabled(!noCorresponde);
        if (noCorresponde) {
            chkReconstitucion.setEnabled(false);
            chkLegitimidad.setEnabled(false);
            chkMediosProbatorios.setEnabled(false);
            if (cmbTipoDocumentoNoCorresponde.getSelectedIndex() <= 0
                    && cmbTipoDocumentoNoCorresponde.getItemCount() > 1) {
                cmbTipoDocumentoNoCorresponde.setSelectedIndex(1);
            }
        } else {
            actualizarChecksIncorporado();
        }
        actualizarBloquesComplementarios(noCorresponde);
        if (!noCorresponde && cmbMotivoNoCorresponde.getItemCount() > 0) {
            cmbMotivoNoCorresponde.setSelectedIndex(0);
        }
        if (!noCorresponde && cmbTipoDocumentoNoCorresponde.getItemCount() > 0) {
            cmbTipoDocumentoNoCorresponde.setSelectedIndex(0);
            txtNumeroDocumentoProveido.setText("");
        }
        if (resultadoRequiereObservacion(resultado)) {
            chkRegistrarObservacion.setSelected(true);
        }
        actualizarObservacionHabilitada();
    }

    private boolean esResultadoNoCorresponde(ResultadoItem resultado) {
        return resultado != null && "NO_CORRESPONDE".equalsIgnoreCase(resultado.codigo);
    }

    private boolean esResultadoNoCorrespondeSeleccionado() {
        return esResultadoNoCorresponde((ResultadoItem) cmbResultado.getSelectedItem());
    }

    private void actualizarBloquesComplementarios(boolean noCorresponde) {
        boolean habilitar = !noCorresponde;
        cmbTipoDocumento.setEnabled(habilitar);
        cmbEstadoDocumento.setEnabled(habilitar);
        fechaDocumentoAnalizado.setEnabled(habilitar);
        txtDescripcionDocumento.setEnabled(habilitar);
        chkDocumentoRequiereRespuesta.setEnabled(habilitar);
        chkRegistrarObservacion.setEnabled(habilitar);
        txtComentarioMovimiento.setEnabled(habilitar);
    }

    private boolean resultadoRequiereObservacion(ResultadoItem resultado) {
        if (resultado == null) {
            return false;
        }
        return "OBSERVADO".equalsIgnoreCase(resultado.codigo)
                || "OBSERVACION_ADMINISTRATIVA".equalsIgnoreCase(resultado.codigo);
    }

    private void actualizarObservacionHabilitada() {
        boolean habilitar = !esResultadoNoCorrespondeSeleccionado()
                && chkRegistrarObservacion.isSelected();
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
        if (cmbTipoDocumentoNoCorresponde.getItemCount() > 0) {
            cmbTipoDocumentoNoCorresponde.setSelectedIndex(0);
        }
        if (cmbTipoDocumento.getItemCount() > 0) {
            cmbTipoDocumento.setSelectedIndex(0);
        }
        if (cmbEstadoDocumento.getItemCount() > 0) {
            cmbEstadoDocumento.setSelectedIndex(0);
        }
        if (cmbTipoObservacion.getItemCount() > 0) {
            cmbTipoObservacion.setSelectedIndex(0);
        }
        txtFundamento.setText("");
        txtNumeroDocumentoProveido.setText("");
        txtComentarioMovimiento.setText("");
        txtDescripcionDocumento.setText("");
        txtObservacion.setText("");
        fechaDocumentoAnalizado.setDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        lblFechaAnalisis.setText(DATE_FORMAT.format(LocalDate.now()));
        chkReconstitucion.setSelected(false);
        chkLegitimidad.setSelected(false);
        chkMediosProbatorios.setSelected(false);
        chkRegistrarObservacion.setSelected(false);
        chkDocumentoRequiereRespuesta.setSelected(false);
        if (documentosTreePanel != null) {
            documentosTreePanel.setDocumentos(idExpedienteDetalleCargado, idAnalisisSeleccionado, new ArrayList<DocumentoAnalizadoDTO>());
        }
        limpiarPublicacionLectura();
        actualizarResultadoSeleccionado();
        actualizarObservacionHabilitada();
    }

    private void setTrabajando(boolean trabajando, String mensaje) {
        btnBuscar.setEnabled(!trabajando);
        btnLimpiar.setEnabled(!trabajando);
        btnRefrescar.setEnabled(!trabajando);
        AnalisisTableRow fila = obtenerFilaSeleccionada();
        AnalisisExpedienteDTO item = fila == null || !fila.esPrincipal() ? null : fila.principal;
        btnRecibir.setEnabled(!trabajando && item != null && item.isRecibible());
        btnEditar.setEnabled(!trabajando && item != null);
        btnRegistrarAnalisis.setEnabled(!trabajando && item != null && item.isRegistrable());
        btnEnviarVerificacion.setEnabled(!trabajando && item != null && item.isEnviableVerificacion());
        btnArchivarNoCorresponde.setEnabled(!trabajando && item != null && item.isArchivableNoCorresponde());
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
                "N° expediente SGD",
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

    private class RecibirAsociadoRenderer extends AppV2ReceiveActionButton implements TableCellRenderer {
        private RecibirAsociadoRenderer() {
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
            configure(recibido, permitido);
            setToolTipText(recibido
                    ? "Documento recibido por el abogado."
                    : permitido
                            ? "Recibir documento asociado."
                            : "Documento asociado no disponible para recibir.");
            return this;
        }
    }

    private class RecibirAsociadoEditor extends AbstractCellEditor implements TableCellEditor {
        private final AppV2ReceiveActionButton button = new AppV2ReceiveActionButton();
        private int modelRow = -1;

        private RecibirAsociadoEditor() {
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
            button.configure(false, permitido);
            button.setToolTipText(permitido
                    ? "Recibir documento asociado."
                    : "Documento asociado no disponible para recibir.");
            return button;
        }
    }

    private Color colorFondoFila(int viewRow, AnalisisTableRow fila, boolean selected) {
        if (fila != null && fila.esAsociada()) {
            return selected ? TABLE_SELECTION_BACKGROUND : ASSOCIATED_ROW_BACKGROUND;
        }
        if (fila != null && fila.esPrincipal() && principalesExpandidos.contains(fila.getIdPrincipal())) {
            return EXPANDED_PARENT_BACKGROUND;
        }
        if (selected) {
            return TABLE_SELECTION_BACKGROUND;
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

    private boolean esPrimerAsociado(int modelRow) {
        if (modelRow < 0 || modelRow >= filasTabla.size() || !filasTabla.get(modelRow).esAsociada()) {
            return false;
        }
        return modelRow == 0 || !filasTabla.get(modelRow - 1).esAsociada();
    }

    private boolean esUltimoAsociado(int modelRow) {
        if (modelRow < 0 || modelRow >= filasTabla.size() || !filasTabla.get(modelRow).esAsociada()) {
            return false;
        }
        return modelRow == filasTabla.size() - 1 || !filasTabla.get(modelRow + 1).esAsociada();
    }

    private javax.swing.border.Border bordeContenidoAsociado(int modelRow, int leftPadding, int rightPadding) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        esPrimerAsociado(modelRow) ? 1 : 0,
                        0,
                        esUltimoAsociado(modelRow) ? 1 : 0,
                        0,
                        ASSOCIATED_BLOCK_BORDER),
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
