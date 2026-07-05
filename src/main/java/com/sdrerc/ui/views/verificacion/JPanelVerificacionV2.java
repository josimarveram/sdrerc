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
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2SearchToolbar;
import com.sdrerc.ui.appv2.components.AppV2SideActionPanel;
import com.sdrerc.ui.appv2.components.AppV2StackedSideTab;
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
    private static final int COL_ESTADO = 11;
    private static final int COL_RESULTADO = 10;
    private static final int COL_EMITIDO = 12;
    private static final int COL_PUBLICACION = 13;
    private static final int COL_ALERTAS = 14;
    private static final int COL_ID = 15;
    private static final int COL_DOCUMENTO_NUM_ANALISIS = 0;
    private static final int COL_DOCUMENTO_TIPO = 1;
    private static final int COL_DOCUMENTO_ESTADO = 2;
    private static final int COL_DOCUMENTO_DETALLE_OBS = 3;
    private static final int COL_DOCUMENTO_FECHA = 4;
    private static final int COL_DOCUMENTO_NUMERO = 5;
    private static final int COL_DOCUMENTO_DESCRIPCION = 6;
    private static final int COL_DOCUMENTO_REQUIERE_RESPUESTA = 7;
    private static final int COL_DOCUMENTO_FECHA_ACUSE = 8;
    private static final int COL_DOCUMENTO_CONFIRMACION_RESPUESTA = 9;
    private static final int COL_DOCUMENTO_FECHA_RESPUESTA = 10;
    private static final int COL_DOCUMENTO_HOJA_ENVIO_RESPUESTA = 11;
    private static final int COL_DOCUMENTO_NOTIFICADO = 12;
    private static final int COL_DOCUMENTO_GUARDAR = 13;
    private static final int COL_DOCUMENTO_ESTADO_CODIGO = 14;
    private static final int COL_DOCUMENTO_ID = 15;
    private static final int DOCUMENTOS_COLUMNAS_FIJAS_ANCHO = 242;
    private static final int PANEL_VERIFICACION_ANCHO_MINIMO = 380;
    private static final int PANEL_VERIFICACION_ANCHO_NORMAL = 430;
    private static final int PANEL_VERIFICACION_TAB_OVERHANG = 46;
    private static final int PANEL_VERIFICACION_TAB_TOP = 18;
    private static final int PANEL_VERIFICACION_TAB_HEIGHT = 94;
    private static final int GROUP_STRIPE_WIDTH = 5;
    private static final String TAB_VERIFICACION_DATOS = "DATOS";
    private static final String TAB_VERIFICACION_OPERACION = "OPERACION";
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
    private final JLabel lblEtapaEstado = new JLabel("-");
    private final JLabel lblAnalisis = new JLabel("-");
    private final JLabel lblAlertas = new JLabel("Sin alertas.");
    private final JLabel lblEstadoDocumentoEmitido = new JLabel("-");
    private final JLabel lblDestinoSiguiente = new JLabel("-");
    private final JLabel lblResponsableFirma = new JLabel("-");
    private final JLabel lblRequierePublicacion = new JLabel("-");
    private final JLabel lblFechaPublicacion = new JLabel("-");
    private final JLabel lblFechaVerificacion = new JLabel(DATE_FORMAT.format(LocalDate.now()));

    private final JComboBox<ResultadoItem> cmbResultado = new JComboBox<ResultadoItem>();
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
    private final DefaultTableModel documentosModel = new DefaultTableModel(
            new Object[]{
                "N° Análisis",
                "Tipo",
                "Estado",
                "Detalle Obs.",
                "Fecha Emisión",
                "N° Documento",
                "Descripción",
                "¿Requiere respuesta?",
                "Fecha Acuse",
                "Confirmación de respuesta",
                "Fecha Respuesta",
                "Hoja de Envío",
                "Notificado",
                "",
                "_ESTADO_CODIGO",
                "_ID_DOCUMENTO"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == COL_DOCUMENTO_ESTADO
                    || column == COL_DOCUMENTO_DETALLE_OBS
                    || column == COL_DOCUMENTO_FECHA
                    || column == COL_DOCUMENTO_NUMERO
                    || column == COL_DOCUMENTO_GUARDAR;
        }
    };
    private final JTable documentosTable = new AppV2Table(documentosModel);
    private final AbstractTableModel documentosFijosModel = new AbstractTableModel() {
        @Override
        public int getRowCount() {
            return documentosTable == null ? documentosModel.getRowCount() : documentosTable.getRowCount();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            return column == 0 ? "N° Análisis" : "Tipo";
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            int modelRow = modeloDocumentoDesdeFilaFija(row);
            return modelRow >= 0 && documentosModel.isCellEditable(modelRow, column);
        }

        @Override
        public Object getValueAt(int row, int column) {
            int modelRow = modeloDocumentoDesdeFilaFija(row);
            if (modelRow < 0) {
                return "";
            }
            return column == 0
                    ? documentosModel.getValueAt(modelRow, COL_DOCUMENTO_NUM_ANALISIS)
                    : documentosModel.getValueAt(modelRow, COL_DOCUMENTO_TIPO);
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            int modelRow = modeloDocumentoDesdeFilaFija(row);
            if (modelRow >= 0) {
                documentosModel.setValueAt(value, modelRow, column == 0 ? COL_DOCUMENTO_NUM_ANALISIS : COL_DOCUMENTO_TIPO);
            }
        }
    };
    private final JTable documentosFijosTable = new AppV2Table(documentosFijosModel);
    private JScrollPane documentosScrollPane;
    private JScrollPane documentosFijosScrollPane;
    private AppV2ColumnFilterSupport.Controller documentosColumnFilterSupport;
    private JTextField filtroDocumentoAnalisis;
    private JTextField filtroDocumentoTipo;
    private final List<SimpleItem> estadosDocumento = new ArrayList<SimpleItem>();
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
        setBorder(AppV2Theme.pageBorder());
        add(crearCentro(), BorderLayout.CENTER);
        configurarTabla();
        configurarDocumentosTabla();
        configurarEventos();
        configurarKpisInteractivos();
        restaurarFechasBusqueda();
        cargarFiltrosBase();
        cargarCatalogos();
        actualizarSeleccion();
    }

    private JPanel crearHeader() {
        JPanel metricas = new AppV2ResponsiveGridPanel(190, 6, 12, 10);
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
        AppV2SearchToolbar toolbar = new AppV2SearchToolbar();
        JPanel accionesFiltro = AppV2ActionPanel.right();
        accionesFiltro.add(btnBuscar);
        accionesFiltro.add(btnLimpiar);
        accionesFiltro.add(btnRefrescar);
        toolbar.addSearchRow("Búsqueda", txtBusqueda, accionesFiltro);
        toolbar.addFilter("Fecha desde", fechaSolicitudDesde);
        toolbar.addFilter("Fecha hasta", fechaSolicitudHasta);
        toolbar.addFilter("Estado", cmbEstadoFiltro);
        toolbar.addCompactFilter(spnLimite);
        return toolbar;
    }

    private JPanel crearBandeja() {
        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);

        AppV2TableSectionPanel section = new AppV2TableSectionPanel(tablePanel);
        section.setStatus(lblEstado);
        return section;
    }

    private AppV2SideActionPanel crearPanelDatosVerificacion() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Datos de verificación", new Runnable() {
            @Override
            public void run() {
                cerrarPanelVerificacion();
            }
        });
        panel.setAccentColor(new Color(57, 125, 199));
        panel.addSection(crearResumenSeleccion());
        panel.addSection(crearAnalisisPrevio());
        panel.addSection(crearPublicacionPrevista());
        return panel;
    }

    private AppV2SideActionPanel crearPanelVerificacionOperativa() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Verificar documento", new Runnable() {
            @Override
            public void run() {
                cerrarPanelVerificacion();
            }
        });
        panel.setAccentColor(new Color(10, 118, 145));
        panel.addSection(crearDocumentosPanel());
        panel.addSection(crearFormularioVerificacion());
        panel.addSection(crearFormularioFirmaEmision());
        panel.setFooter(crearAccionesPanelVerificacion());
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
        JPanel panel = section("Documentos revisados en análisis");
        documentosScrollPane = new JScrollPane(documentosTable);
        documentosScrollPane.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        documentosScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        documentosScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        documentosModel.addTableModelListener(e -> actualizarAlturaDocumentosVerificacion(documentosScrollPane));
        actualizarAlturaDocumentosVerificacion(documentosScrollPane);
        documentosFijosScrollPane = construirScrollFijoDocumentos();

        JPanel contenedorTablas = new JPanel(new BorderLayout(0, 0));
        contenedorTablas.setOpaque(false);
        contenedorTablas.add(documentosFijosScrollPane, BorderLayout.WEST);
        contenedorTablas.add(documentosScrollPane, BorderLayout.CENTER);

        panel.add(contenedorTablas, BorderLayout.CENTER);
        return panel;
    }

    private void actualizarAlturaDocumentosVerificacion(JScrollPane scroll) {
        int headerHeight = scroll.getColumnHeader() != null && scroll.getColumnHeader().getView() != null
                ? scroll.getColumnHeader().getView().getPreferredSize().height
                : documentosTable.getTableHeader().getPreferredSize().height;
        int rowCount = Math.max(1, documentosTable.getRowCount());
        int rowsHeight = rowCount * documentosTable.getRowHeight();
        int horizontalBarHeight = scroll.getHorizontalScrollBar().getPreferredSize().height;
        int height = headerHeight + rowsHeight + horizontalBarHeight + 18;
        Dimension size = new Dimension(360, height);
        scroll.setPreferredSize(size);
        scroll.setMinimumSize(new Dimension(280, height));
        if (documentosFijosScrollPane != null) {
            documentosFijosScrollPane.setPreferredSize(new Dimension(DOCUMENTOS_COLUMNAS_FIJAS_ANCHO, height));
            documentosFijosScrollPane.setMinimumSize(new Dimension(DOCUMENTOS_COLUMNAS_FIJAS_ANCHO, height));
        }
        scroll.revalidate();
        scroll.repaint();
        actualizarDocumentosFijos();
    }

    private void actualizarDocumentosFijos() {
        if (documentosFijosModel != null) {
            documentosFijosModel.fireTableDataChanged();
        }
        if (documentosFijosScrollPane != null) {
            documentosFijosScrollPane.revalidate();
            documentosFijosScrollPane.repaint();
        }
    }

    private int modeloDocumentoDesdeFilaFija(int fixedRow) {
        if (fixedRow < 0 || documentosTable == null || fixedRow >= documentosTable.getRowCount()) {
            return -1;
        }
        return documentosTable.convertRowIndexToModel(fixedRow);
    }

    private void configurarColumnaDocumento(JTable tabla, int index, int preferred, int min, int max) {
        TableColumn column = tabla.getColumnModel().getColumn(index);
        column.setPreferredWidth(preferred);
        column.setMinWidth(min);
        column.setMaxWidth(max);
    }

    private JScrollPane construirScrollFijoDocumentos() {
        JScrollPane scroll = new JScrollPane(documentosFijosTable);
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(DOCUMENTOS_COLUMNAS_FIJAS_ANCHO, 0));
        scroll.setMinimumSize(new Dimension(DOCUMENTOS_COLUMNAS_FIJAS_ANCHO, 0));
        scroll.setMaximumSize(new Dimension(DOCUMENTOS_COLUMNAS_FIJAS_ANCHO, Integer.MAX_VALUE));
        documentosFijosTable.setShowHorizontalLines(false);
        documentosFijosTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        documentosFijosTable.setPreferredScrollableViewportSize(new Dimension(DOCUMENTOS_COLUMNAS_FIJAS_ANCHO, 0));
        documentosFijosTable.setRowHeight(30);
        TableColumn colAnalisis = documentosFijosTable.getColumnModel().getColumn(0);
        colAnalisis.setPreferredWidth(92);
        colAnalisis.setMinWidth(92);
        colAnalisis.setMaxWidth(92);
        TableColumn colTipo = documentosFijosTable.getColumnModel().getColumn(1);
        colTipo.setPreferredWidth(150);
        colTipo.setMinWidth(150);
        colTipo.setMaxWidth(150);
        JTextField filtroAnalisis = crearCampoFiltroDocumentoFijo("Filtrar");
        JTextField filtroTipo = crearCampoFiltroDocumentoFijo("Filtrar");
        filtroDocumentoAnalisis = filtroAnalisis;
        filtroDocumentoTipo = filtroTipo;
        filtroAnalisis.setPreferredSize(new Dimension(92, 24));
        filtroTipo.setPreferredSize(new Dimension(150, 24));
        FrozenDocumentHeaderPanel header = new FrozenDocumentHeaderPanel(
                documentosFijosTable,
                filtroAnalisis,
                filtroTipo);
        scroll.setColumnHeaderView(header);
        documentosFijosTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        documentosFijosTable.setSelectionModel(documentosTable.getSelectionModel());
        scroll.getVerticalScrollBar().setModel(documentosScrollPane.getVerticalScrollBar().getModel());
        filtroAnalisis.getDocument().addDocumentListener(simpleDocumentListener(() ->
                aplicarFiltroDocumentoFijo(COL_DOCUMENTO_NUM_ANALISIS, filtroAnalisis.getText())));
        filtroTipo.getDocument().addDocumentListener(simpleDocumentListener(() ->
                aplicarFiltroDocumentoFijo(COL_DOCUMENTO_TIPO, filtroTipo.getText())));
        return scroll;
    }

    private JTextField crearCampoFiltroDocumentoFijo(String prompt) {
        JTextField field = new JTextField();
        field.setFont(AppV2Theme.fontPlain(10));
        field.setForeground(AppV2Theme.TEXT_PRIMARY);
        field.setBackground(AppV2Theme.SURFACE);
        field.setCaretColor(AppV2Theme.PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(1, 6, 1, 6)));
        field.setPreferredSize(new Dimension(100, 24));
        field.setToolTipText(prompt);
        return field;
    }

    private SortOrder sortOrderDocumentoFijo(int modelColumn) {
        if (documentosColumnFilterSupport == null || documentosColumnFilterSupport.getSorter() == null) {
            return SortOrder.UNSORTED;
        }
        for (javax.swing.RowSorter.SortKey key : documentosColumnFilterSupport.getSorter().getSortKeys()) {
            if (key.getColumn() == modelColumn) {
                return key.getSortOrder();
            }
        }
        return SortOrder.UNSORTED;
    }

    private void aplicarFiltroDocumentoFijo(int modelColumn, String text) {
        if (documentosColumnFilterSupport == null) {
            return;
        }
        documentosColumnFilterSupport.setFilterText(modelColumn, text);
    }

    private DocumentListener simpleDocumentListener(Runnable action) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        };
    }

    private final class FrozenDocumentHeaderPanel extends JPanel {

        private static final int FIXED_HEADER_HEIGHT = 30;
        private static final int FIXED_FILTER_HEIGHT = 30;
        private final CellRendererPane rendererPane = new CellRendererPane();
        private final JTable tableRef;
        private final JTextField filtroAnalisisRef;
        private final JTextField filtroTipoRef;

        private FrozenDocumentHeaderPanel(
                JTable tableRef,
                JTextField filtroAnalisisRef,
                JTextField filtroTipoRef) {
            this.tableRef = tableRef;
            this.filtroAnalisisRef = filtroAnalisisRef;
            this.filtroTipoRef = filtroTipoRef;
            setLayout(null);
            setOpaque(true);
            setBackground(AppV2Theme.SURFACE_ALT);
            add(rendererPane);
            add(filtroAnalisisRef);
            add(filtroTipoRef);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    if (event.getY() >= FIXED_HEADER_HEIGHT) {
                        return;
                    }
                    int viewColumn = viewColumnAt(event.getX());
                    if (viewColumn < 0) {
                        return;
                    }
                    int modelColumn = viewColumn == 0 ? COL_DOCUMENTO_NUM_ANALISIS : COL_DOCUMENTO_TIPO;
                    if (documentosColumnFilterSupport != null && documentosColumnFilterSupport.getSorter() != null) {
                        documentosColumnFilterSupport.getSorter().toggleSortOrder(modelColumn);
                        repaint();
                    }
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent event) {
                    setCursor(event.getY() < FIXED_HEADER_HEIGHT
                            ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                            : Cursor.getDefaultCursor());
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(DOCUMENTOS_COLUMNAS_FIJAS_ANCHO, FIXED_HEADER_HEIGHT + FIXED_FILTER_HEIGHT);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public void doLayout() {
            int analisisWidth = tableRef.getColumnModel().getColumn(0).getWidth();
            int tipoWidth = tableRef.getColumnModel().getColumn(1).getWidth();
            rendererPane.setBounds(0, 0, analisisWidth + tipoWidth, FIXED_HEADER_HEIGHT);
            filtroAnalisisRef.setBounds(2, FIXED_HEADER_HEIGHT + 3, Math.max(0, analisisWidth - 4), 24);
            filtroTipoRef.setBounds(analisisWidth + 2, FIXED_HEADER_HEIGHT + 3, Math.max(0, tipoWidth - 4), 24);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            paintHeaderCell(graphics, 0, 92, COL_DOCUMENTO_NUM_ANALISIS);
            paintHeaderCell(graphics, 92, 150, COL_DOCUMENTO_TIPO);
        }

        private void paintHeaderCell(Graphics graphics, int x, int width, int modelColumn) {
            TableCellRenderer renderer = tableRef.getTableHeader().getDefaultRenderer();
            Component component = renderer.getTableCellRendererComponent(
                    tableRef,
                    tableRef.getColumnModel().getColumn(modelColumn).getHeaderValue(),
                    false,
                    false,
                    -1,
                    modelColumn);
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                label.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
                label.setForeground(AppV2Theme.TEXT_SECONDARY);
                label.setBackground(AppV2Theme.SURFACE_ALT);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setHorizontalTextPosition(JLabel.LEFT);
                label.setIconTextGap(6);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, AppV2Theme.BORDER),
                        BorderFactory.createEmptyBorder(0, 8, 0, 8)));
                label.setIcon(new SortIndicatorIcon(sortOrderDocumentoFijo(modelColumn)));
                rendererPane.paintComponent(
                        graphics,
                        label,
                        this,
                        x,
                        0,
                        width,
                        FIXED_HEADER_HEIGHT,
                        true);
                return;
            }
            rendererPane.paintComponent(graphics, component, this, x, 0, width, FIXED_HEADER_HEIGHT, true);
        }

        private int viewColumnAt(int x) {
            if (x >= 0 && x < 92) {
                return 0;
            }
            if (x >= 92 && x < 242) {
                return 1;
            }
            return -1;
        }
    }

    private static final class SortIndicatorIcon implements Icon {

        private final SortOrder order;

        private SortIndicatorIcon(SortOrder order) {
            this.order = order == null ? SortOrder.UNSORTED : order;
        }

        @Override
        public int getIconWidth() {
            return 9;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (order == SortOrder.ASCENDING) {
                    g2.setColor(AppV2Theme.PRIMARY);
                    paintUp(g2, x, y + 3);
                } else if (order == SortOrder.DESCENDING) {
                    g2.setColor(AppV2Theme.PRIMARY);
                    paintDown(g2, x, y + 6);
                } else {
                    g2.setColor(new Color(
                            AppV2Theme.TEXT_SECONDARY.getRed(),
                            AppV2Theme.TEXT_SECONDARY.getGreen(),
                            AppV2Theme.TEXT_SECONDARY.getBlue(),
                            150));
                    paintUp(g2, x, y + 2);
                    paintDown(g2, x, y + 8);
                }
            } finally {
                g2.dispose();
            }
        }

        private static void paintUp(Graphics2D g2, int x, int y) {
            g2.fillPolygon(
                    new int[]{x + 4, x + 1, x + 7},
                    new int[]{y, y + 5, y + 5},
                    3);
        }

        private static void paintDown(Graphics2D g2, int x, int y) {
            g2.fillPolygon(
                    new int[]{x + 1, x + 7, x + 4},
                    new int[]{y, y, y + 5},
                    3);
        }
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
        cmbResultado.setPreferredSize(new Dimension(235, 34));
        cmbTipoObservacion.setPreferredSize(new Dimension(235, 34));
        cmbMotivoCorreccion.setPreferredSize(new Dimension(235, 34));
        cmbTipoResolucion.setPreferredSize(new Dimension(235, 34));
        txtNumeroResolucion.setPreferredSize(new Dimension(235, 34));
        txtFechaFirma.setPreferredSize(new Dimension(235, 34));
        txtFechaEmision.setPreferredSize(new Dimension(235, 34));
        txtFechaResolucion.setPreferredSize(new Dimension(235, 34));
        spnLimite.setPreferredSize(new Dimension(86, 34));
        btnBuscar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarVerificacion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnCancelarVerificacion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnAprobar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnEnviarFirma.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnObservar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnDocumentoInconsistente.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnDevolverAnalisis.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarFirma.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarEmision.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarNumero.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnEnviarEjecucion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnEnviarNotificacion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
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
                46, 88, 185, 155, 160, 145, 210, 125, 125, 250, 175, 150, 105, 125, 190, 0);
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
                15);
    }

    private void configurarDocumentosTabla() {
        configurarDocumentoTablaBase(documentosTable, true);
        configurarDocumentoTablaBase(documentosFijosTable, false);
        documentosTable.setSelectionModel(documentosFijosTable.getSelectionModel());
        documentosFijosTable.setSelectionModel(documentosTable.getSelectionModel());
        if (documentosScrollPane != null) {
            documentosColumnFilterSupport = AppV2ColumnFilterSupport.install(
                    "Verificacion.DocumentosRevisados",
                    documentosTable,
                    documentosScrollPane,
                    documentosScrollPane,
                    null,
                    COL_DOCUMENTO_GUARDAR,
                    COL_DOCUMENTO_ESTADO_CODIGO,
                    COL_DOCUMENTO_ID);
            documentosColumnFilterSupport.getFilterField(COL_DOCUMENTO_NUM_ANALISIS).setVisible(false);
            documentosColumnFilterSupport.getFilterField(COL_DOCUMENTO_TIPO).setVisible(false);
            documentosColumnFilterSupport.getSorter().addRowSorterListener(event -> actualizarDocumentosFijos());
            actualizarAlturaDocumentosVerificacion(documentosScrollPane);
        }
    }

    private void configurarDocumentoTablaBase(JTable tabla, boolean principal) {
        tabla.setRowHeight(30);
        tabla.setAutoCreateRowSorter(false);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setResizingAllowed(true);
        tabla.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tabla.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        tabla.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        tabla.setGridColor(AppV2Theme.BORDER);
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setDefaultRenderer(Object.class, new DocumentoVerificacionRenderer());
        if (principal) {
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_ESTADO)
                    .setCellEditor(new DefaultCellEditor(comboEstadosDocumento()));
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_DETALLE_OBS)
                    .setCellEditor(new DefaultCellEditor(new JTextField(28)));
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_FECHA)
                    .setCellEditor(new FechaDocumentoCellEditor());
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_NUMERO)
                    .setCellEditor(new DefaultCellEditor(new JTextField(18)));
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_GUARDAR).setCellRenderer(new GuardarDocumentoRenderer());
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_GUARDAR).setCellEditor(new GuardarDocumentoEditor());
        }
        int[] widths = principal
                ? new int[]{0, 0, 120, 180, 110, 120, 240, 145, 105, 165, 115, 145, 95, 42, 0, 0}
                : new int[]{92, 150};
        int visibleCount = tabla.getColumnModel().getColumnCount();
        for (int i = 0; i < visibleCount && i < widths.length; i++) {
            int preferred = widths[i];
            int min = principal && i < 2 ? 0 : Math.min(preferred, 90);
            int max = principal && i < 2 ? 0 : preferred + 90;
            configurarColumnaDocumento(tabla, i, preferred, min, max);
        }
        if (principal) {
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_GUARDAR).setMinWidth(42);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_GUARDAR).setPreferredWidth(42);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_GUARDAR).setMaxWidth(48);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_ESTADO_CODIGO).setMinWidth(0);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_ESTADO_CODIGO).setMaxWidth(0);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_ID).setMinWidth(0);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_ID).setMaxWidth(0);
        } else {
            for (int i = tabla.getColumnModel().getColumnCount() - 1; i >= 2; i--) {
                tabla.getColumnModel().removeColumn(tabla.getColumnModel().getColumn(i));
            }
            tabla.setPreferredScrollableViewportSize(new Dimension(DOCUMENTOS_COLUMNAS_FIJAS_ANCHO, 0));
        }
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
        estadosDocumento.clear();
        for (CatalogoItemDTO item : carga.estadosDocumento) {
            estadosDocumento.add(new SimpleItem(item.getCodigo(), item.getNombre()));
        }
        configurarEditoresCatalogoDocumentoTabla();
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
            item.getNumeroTramiteDocumentario(),
            formatDate(item.getFechaRecepcion()),
            item.getProcedimiento(),
            item.getTipoActa(),
            item.getNumeroActa(),
            item.getTitular(),
            item.getUltimoResultadoAnalisis().isEmpty() ? "Sin resultado" : item.getUltimoResultadoAnalisis(),
            estadoVisualIntegrado(item),
            item.isDocumentoEmitido() ? "Emitido" : "Pendiente",
            item.isRequierePublicacion() ? "Requiere" : "No",
            item.isTieneObservacionPendiente() ? "Con observación" : "Sin observación",
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
            valorUi(asociado.getNumeroDocumento().isEmpty()
                    ? asociado.getNumeroTramiteDocumentario()
                    : asociado.getNumeroDocumento()),
            formatDate(asociado.getFechaRecepcion()),
            procedimientoAsociado(asociado),
            valorUi(asociado.getTipoActa()),
            valorUi(asociado.getNumeroActa()),
            valorUi(asociado.getTitular()),
            "Contexto de verificación",
            estadoAsociado(asociado),
            "-",
            principal.isRequierePublicacion() ? "Requiere" : "No",
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
        documentosModel.setRowCount(0);
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
            lblExpediente.setText("-");
            lblExpedienteSgd.setText("-");
            lblTitular.setText("-");
            lblActa.setText("-");
            lblProcedimiento.setText("-");
            lblResponsable.setText("-");
            lblResponsableAnalisis.setText("-");
            lblEtapaEstado.setText("-");
            lblDestinoSiguiente.setText("-");
            lblAnalisis.setText("-");
            lblAlertas.setText("Sin alertas.");
            limpiarContextoDocumentoEmitido();
            txtFundamentoAnalisis.setText("");
            documentosModel.setRowCount(0);
            limpiarFormularioFirma();
            return;
        }
        if (asociado) {
            ExpedienteRelacionadoDTO relacionado = fila.asociado;
            lblExpediente.setText("Documento asociado seleccionado");
            lblExpedienteSgd.setText(item == null ? "-" : valorUi(item.getNumeroExpedienteSgd()));
            lblTitular.setText(valorUi(relacionado.getTitular()));
            lblActa.setText((valorUi(relacionado.getTipoActa()) + " " + valorUi(relacionado.getNumeroActa())).trim());
            lblProcedimiento.setText(procedimientoAsociado(relacionado));
            lblResponsable.setText(valorUi(relacionado.getAbogadoAsignado()));
            lblResponsableAnalisis.setText(item == null || item.getResponsableAnalisis().isEmpty() ? "-" : item.getResponsableAnalisis());
            lblEtapaEstado.setText("Expediente principal: " + fila.numeroExpedientePrincipal());
            lblDestinoSiguiente.setText("Contexto del expediente principal");
            lblAnalisis.setText("Contexto de verificación");
            txtFundamentoAnalisis.setText("Este documento está asociado al expediente principal y se muestra como contexto del caso.");
            txtFundamentoAnalisis.setCaretPosition(0);
            lblAlertas.setText(textoRelacionAsociada(relacionado));
            txtComentario.setText("");
            txtObservacion.setText("");
            documentosModel.setRowCount(0);
            limpiarContextoDocumentoEmitido();
            limpiarFormularioFirma();
            return;
        }
        lblExpediente.setText(item.getNumeroExpediente());
        lblExpedienteSgd.setText(valorUi(item.getNumeroExpedienteSgd()));
        lblTitular.setText(item.getTitular());
        lblActa.setText((item.getTipoActa() + " " + item.getNumeroActa()).trim());
        lblProcedimiento.setText(item.getProcedimiento());
        lblResponsable.setText(item.getResponsable().isEmpty() ? "-" : item.getResponsable());
        lblResponsableAnalisis.setText(item.getResponsableAnalisis().isEmpty() ? "-" : item.getResponsableAnalisis());
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
        documentosModel.setRowCount(0);
        if (item == null || item.getIdExpediente() == null) {
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
                    int analisis = 1;
                    for (DocumentoVerificacionDTO documento : get()) {
                        documentosModel.addRow(new Object[]{
                            analisis++,
                            documento.getTipoDocumento(),
                            new SimpleItem(documento.getEstadoDocumentoCodigo(), documento.getEstadoDocumento()),
                            documento.getDetalleObservacion(),
                            formatDate(documento.getFechaDocumento()),
                            documento.getNumeroDocumento(),
                            documento.getDescripcion(),
                            documento.isRequiereRespuesta() ? "Si" : "No",
                            formatDate(documento.getFechaAcuse()),
                            confirmacionRespuestaUi(documento.getConfirmacionRespuesta(), documento.isRequiereRespuesta()),
                            formatDate(documento.getFechaRespuesta()),
                            documento.getNumeroHojaEnvioRespuesta(),
                            documento.isNotificado() ? "Si" : "No",
                            "",
                            documento.getEstadoDocumentoCodigo(),
                            documento.getIdDocumentoAnalizado()
                        });
                    }
                } catch (Exception ex) {
                    lblEstado.setText("No se pudieron cargar los documentos analizados.");
                }
            }
        };
        worker.execute();
    }

    private void guardarDocumentoRevisado(int modelRow) {
        if (modelRow < 0 || modelRow >= documentosModel.getRowCount()) {
            return;
        }
        VerificacionExpedienteDTO item = obtenerSeleccionado();
        if (item == null || item.getIdExpediente() == null) {
            mostrarInfo("Seleccione un expediente principal para editar el estado del documento.");
            return;
        }
        Long idDocumento = asLong(documentosModel.getValueAt(modelRow, COL_DOCUMENTO_ID));
        if (idDocumento == null) {
            mostrarInfo("Documento analizado no identificado.");
            return;
        }
        if (estadosDocumento.isEmpty()) {
            mostrarInfo("No hay estados de documento disponibles.");
            return;
        }
        SimpleItem seleccionado = estadoDocumentoDesdeFila(modelRow);
        if (seleccionado == null || seleccionado.codigo.isEmpty()) {
            mostrarInfo("Seleccione el estado del documento revisado.");
            return;
        }
        String detalle = value(documentosModel.getValueAt(modelRow, COL_DOCUMENTO_DETALLE_OBS));
        String numeroDocumento = value(documentosModel.getValueAt(modelRow, COL_DOCUMENTO_NUMERO));
        String fechaEmisionTexto = value(documentosModel.getValueAt(modelRow, COL_DOCUMENTO_FECHA));
        LocalDate fechaEmision = parseFechaDocumentoRevisado(fechaEmisionTexto);
        if (!fechaEmisionTexto.trim().isEmpty() && fechaEmision == null) {
            mostrarInfo("Ingrese la fecha de emisión con formato dd/MM/yyyy.");
            return;
        }
        if ("OBSERVADO".equalsIgnoreCase(seleccionado.codigo) && detalle.trim().isEmpty()) {
            mostrarInfo("Ingrese el detalle de observación del documento revisado.");
            return;
        }

        final Long idExpediente = item.getIdExpediente();
        final Long idDocumentoAnalizado = idDocumento;
        final String estadoCodigo = seleccionado.codigo;
        final String estadoNombre = seleccionado.nombre;
        final String detalleObservacion = detalle;
        final String numeroDocumentoFinal = numeroDocumento;
        final LocalDate fechaEmisionFinal = fechaEmision;
        documentosTable.setEnabled(false);
        setTrabajando(true, "Guardando documento revisado...");
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                documentoService.actualizarEstadoDocumentoAnalizado(
                        idExpediente,
                        idDocumentoAnalizado,
                        estadoCodigo,
                        detalleObservacion,
                        fechaEmisionFinal,
                        numeroDocumentoFinal);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    int row = indiceFilaDocumento(idDocumentoAnalizado);
                    if (row >= 0) {
                        documentosModel.setValueAt(new SimpleItem(estadoCodigo, estadoNombre), row, COL_DOCUMENTO_ESTADO);
                        documentosModel.setValueAt(estadoCodigo, row, COL_DOCUMENTO_ESTADO_CODIGO);
                        documentosModel.setValueAt(detalleObservacion, row, COL_DOCUMENTO_DETALLE_OBS);
                        documentosModel.setValueAt(formatDate(fechaEmisionFinal), row, COL_DOCUMENTO_FECHA);
                        documentosModel.setValueAt(numeroDocumentoFinal, row, COL_DOCUMENTO_NUMERO);
                    }
                    lblEstado.setText("Documento revisado actualizado.");
                } catch (Exception ex) {
                    mostrarError("No se pudo guardar el documento revisado.", ex);
                } finally {
                    documentosTable.setEnabled(true);
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private int indiceFilaDocumento(Long idDocumentoAnalizado) {
        if (idDocumentoAnalizado == null) {
            return -1;
        }
        for (int row = 0; row < documentosModel.getRowCount(); row++) {
            Long id = asLong(documentosModel.getValueAt(row, COL_DOCUMENTO_ID));
            if (idDocumentoAnalizado.equals(id)) {
                return row;
            }
        }
        return -1;
    }

    private JComboBox<SimpleItem> comboEstadosDocumento() {
        JComboBox<SimpleItem> combo = new JComboBox<SimpleItem>();
        for (SimpleItem estado : estadosDocumento) {
            if (estado != null && !estado.codigo.isEmpty()) {
                combo.addItem(estado);
            }
        }
        return combo;
    }

    private void configurarEditoresCatalogoDocumentoTabla() {
        if (documentosTable != null && documentosTable.getColumnModel().getColumnCount() > COL_DOCUMENTO_ESTADO) {
            documentosTable.getColumnModel().getColumn(COL_DOCUMENTO_ESTADO)
                    .setCellEditor(new DefaultCellEditor(comboEstadosDocumento()));
        }
    }

    private SimpleItem estadoDocumentoDesdeFila(int modelRow) {
        Object estadoValue = documentosModel.getValueAt(modelRow, COL_DOCUMENTO_ESTADO);
        if (estadoValue instanceof SimpleItem) {
            return (SimpleItem) estadoValue;
        }
        String codigo = value(documentosModel.getValueAt(modelRow, COL_DOCUMENTO_ESTADO_CODIGO));
        String nombre = value(estadoValue);
        if (codigo.trim().isEmpty()) {
            return null;
        }
        return new SimpleItem(codigo, nombre.trim().isEmpty() ? codigo : nombre);
    }

    private static LocalDate parseFechaDocumentoRevisado(String value) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private boolean esEstadoDocumentoObservadoPorFila(int modelRow) {
        if (modelRow < 0 || modelRow >= documentosModel.getRowCount()) {
            return false;
        }
        SimpleItem estado = estadoDocumentoDesdeFila(modelRow);
        return estado != null && "OBSERVADO".equalsIgnoreCase(estado.codigo);
    }

    private static String confirmacionRespuestaUi(String value, boolean requiereRespuesta) {
        if (!requiereRespuesta) {
            return "-";
        }
        String normalized = value == null ? "" : value.trim().toUpperCase();
        normalized = normalized.replace('Í', 'I');
        if ("SI".equals(normalized)) {
            return "Si";
        }
        if ("NO".equals(normalized)) {
            return "No";
        }
        return "Pendiente";
    }

    private void registrarVerificacion() {
        VerificacionExpedienteDTO item = requerirSeleccion("Seleccione un expediente para registrar verificación.");
        if (item == null) {
            return;
        }
        VerificacionRegistroDTO registro = construirRegistro(item, null);
        confirmarYEjecutar(
                "Registrar verificación",
                "Se registrará la verificación del expediente " + item.getNumeroExpediente() + ". ¿Desea continuar?",
                () -> verificacionService.registrarVerificacion(registro));
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
        documentosModel.setRowCount(0);
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
                "Trámite / Documento",
                "Fecha solicitud",
                "Procedimiento",
                "Tipo acta",
                "Nro. acta",
                "Titular",
                "Resultado análisis",
                "Estado",
                "Emitido",
                "Requiere publicación",
                "Alertas / Observaciones",
                "_ID"
            }, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private class DocumentoVerificacionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = value instanceof SimpleItem ? ((SimpleItem) value).toString() : value(value);
            setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setText(text == null || text.trim().isEmpty() ? "-" : text);
            setToolTipText(getText());
            if (isSelected) {
                c.setBackground(TABLE_SELECTION_BACKGROUND);
                c.setForeground(TABLE_SELECTION_FOREGROUND);
            } else {
                c.setBackground(colorDocumentoRevisado(column));
                c.setForeground(column == COL_DOCUMENTO_DETALLE_OBS
                        && !esEstadoDocumentoObservadoPorFila(table.convertRowIndexToModel(row))
                        ? AppV2Theme.TEXT_SECONDARY
                        : AppV2Theme.TEXT_PRIMARY);
            }
            return c;
        }
    }

    private Color colorDocumentoRevisado(int viewColumn) {
        int modelColumn = documentosTable.convertColumnIndexToModel(viewColumn);
        if (modelColumn >= COL_DOCUMENTO_NUM_ANALISIS && modelColumn <= COL_DOCUMENTO_REQUIERE_RESPUESTA) {
            return DOCUMENTO_ANALISIS_BACKGROUND;
        }
        if (modelColumn >= COL_DOCUMENTO_FECHA_ACUSE && modelColumn <= COL_DOCUMENTO_HOJA_ENVIO_RESPUESTA) {
            return DOCUMENTO_ASIGNACION_BACKGROUND;
        }
        if (modelColumn == COL_DOCUMENTO_NOTIFICADO) {
            return DOCUMENTO_NOTIFICACION_BACKGROUND;
        }
        return DOCUMENTO_ACTION_BACKGROUND;
    }

    private class GuardarDocumentoRenderer extends JButton implements TableCellRenderer {
        private GuardarDocumentoRenderer() {
            setText("");
            setIcon(new SaveDocumentIcon());
            setToolTipText("Guardar documento revisado");
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            setEnabled(table.isEnabled());
            return this;
        }
    }

    private class GuardarDocumentoEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton();
        private int modelRow = -1;

        private GuardarDocumentoEditor() {
            button.setText("");
            button.setIcon(new SaveDocumentIcon());
            button.setToolTipText("Guardar documento revisado");
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.addActionListener(e -> {
                int row = modelRow;
                fireEditingStopped();
                guardarDocumentoRevisado(row);
            });
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
            modelRow = table.convertRowIndexToModel(row);
                button.setEnabled(table.isEnabled());
            return button;
        }
    }

    private class FechaDocumentoCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final PremiumDateFieldV2 field = new PremiumDateFieldV2();

        private FechaDocumentoCellEditor() {
            field.setPreferredSize(new Dimension(130, 34));
        }

        @Override
        public Object getCellEditorValue() {
            LocalDate date = fechaSeleccionada(field);
            return date == null ? "" : formatDate(date);
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
            LocalDate date = parseFechaDocumentoRevisado(value == null ? "" : value.toString());
            field.setDate(date == null ? null : Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            return field;
        }
    }

    private static final class SaveDocumentIcon implements Icon {
        private static final int SIZE = 18;

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color stroke = AppV2Theme.PRIMARY;
                Color fill = new Color(238, 247, 252);
                g2.setColor(fill);
                g2.fillRoundRect(x + 2, y + 2, 14, 14, 4, 4);
                g2.setColor(stroke);
                g2.drawRoundRect(x + 2, y + 2, 14, 14, 4, 4);
                g2.fillRect(x + 5, y + 3, 7, 4);
                g2.setColor(Color.WHITE);
                g2.fillRect(x + 6, y + 4, 4, 2);
                g2.setColor(stroke);
                g2.fillRoundRect(x + 5, y + 10, 8, 5, 2, 2);
                g2.setColor(Color.WHITE);
                g2.drawLine(x + 7, y + 12, x + 11, y + 12);
            } finally {
                g2.dispose();
            }
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
            if (!isSelected && modelColumn == COL_EMITIDO) {
                boolean emitido = "Emitido".equalsIgnoreCase(value == null ? "" : value.toString());
                return new BadgeV2(
                        emitido ? "Emitido" : "Pendiente",
                        emitido ? AppV2Theme.SOFT_GREEN : AppV2Theme.SOFT_GRAY,
                        emitido ? AppV2Theme.SUCCESS : AppV2Theme.TEXT_SECONDARY);
            }
            if (!isSelected && modelColumn == COL_PUBLICACION) {
                boolean requiere = "Requiere".equalsIgnoreCase(value == null ? "" : value.toString());
                return new BadgeV2(
                        requiere ? "Requiere" : "No",
                        requiere ? AppV2Theme.SOFT_ORANGE : AppV2Theme.SOFT_GRAY,
                        requiere ? AppV2Theme.WARNING : AppV2Theme.TEXT_SECONDARY);
            }
            if (!isSelected && modelColumn == COL_ALERTAS && value != null && value.toString().startsWith("Con")) {
                return new BadgeV2(value.toString(), AppV2Theme.SOFT_ORANGE, AppV2Theme.WARNING);
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
