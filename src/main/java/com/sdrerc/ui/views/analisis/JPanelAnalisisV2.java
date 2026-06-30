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
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO;
import com.sdrerc.domain.dto.sdrercapp.ObservacionAnalisisDTO;
import com.sdrerc.ui.appv2.components.AppV2ActionPanel;
import com.sdrerc.ui.appv2.components.AppV2ColumnFilterSupport;
import com.sdrerc.ui.appv2.components.AppV2AssociatedDocumentIconCell;
import com.sdrerc.ui.appv2.components.AppV2ExpandCollapseGlyph;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.components.AppV2ReceiveActionButton;
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2SearchToolbar;
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
import java.awt.event.ItemEvent;
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
import javax.swing.BorderFactory;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.CellRendererPane;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JFileChooser;
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
import javax.swing.SwingConstants;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.filechooser.FileNameExtensionFilter;

public class JPanelAnalisisV2 extends JPanel {

    private static final int COL_EXPANDIR = 0;
    private static final int COL_DIAS = 1;
    private static final int COL_EXPEDIENTE = 2;
    private static final int COL_EXPEDIENTE_SGD = 3;
    private static final int COL_ESTADO = 10;
    private static final int COL_ASOCIADOS = 11;
    private static final int COL_ID = 12;
    private static final int COL_DOCUMENTO_NUMERO_ANALISIS = 0;
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
    private static final int COL_DOCUMENTO_PLANTILLA = 13;
    private static final int COL_DOCUMENTO_GUARDAR = 14;
    private static final int COL_DOCUMENTO_ACCION = 15;
    private static final int COL_DOCUMENTO_TIPO_CODIGO = 16;
    private static final int COL_DOCUMENTO_ESTADO_CODIGO = 17;
    private static final int COL_DOCUMENTO_ID = 18;
    private static final int DOCUMENTOS_COLUMNAS_FIJAS_ANCHO = 242;
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

    private final AnalisisExpedienteService analisisService;
    private final DocumentoAnalisisService documentoService;
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
    private final JButton btnRegistrarAnalisis = new JButton("Registrar resultado final");
    private final JButton btnEnviarVerificacion = new JButton("Enviar a verificación");
    private final JButton btnArchivarNoCorresponde = new JButton("Archivar no corresponde");
    private final JButton btnAgregarDocumento = new JButton("+");
    private final JButton btnQuitarDocumento = new JButton("Quitar documento");
    private final JButton btnNuevoBloqueAnalisis = new JButton("+ Nuevo análisis");

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
    private final JLabel lblDatosTramiteWeb = new JLabel("-");
    private final JLabel lblDatosNumeroDocumentoTitular = new JLabel("-");
    private final JLabel lblDatosExpedienteSgd = new JLabel("-");
    private final JLabel lblDatosFechaRecepcion = new JLabel("-");
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
    private final JLabel lblDatosDias = new JLabel("-");
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
    private final JComboBox<AnalisisItemDTO> cmbBloquesAnalisis = new JComboBox<AnalisisItemDTO>();
    private final AppV2StackedSideTab tabDatosAnalisis = crearTabAnalisis("Datos", new Color(230, 241, 245), new Color(57, 125, 199));
    private final AppV2StackedSideTab tabDocumentosAnalisis = crearTabAnalisis("Análisis", new Color(224, 243, 240), new Color(10, 118, 145));

    private final AnalisisTableModel tableModel = new AnalisisTableModel();
    private final JTable table = new AppV2Table(tableModel);
    private final AppV2TablePanel tablePanel = new AppV2TablePanel(
            table,
            "Sin expedientes para mostrar",
            "Seleccione filtros y presione Buscar.");
    private AppV2ColumnFilterSupport.Controller columnFilterSupport;
    private final DefaultTableModel documentoModel = new DefaultTableModel(
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
                "",
                "",
                "tipo_codigo",
                "estado_codigo",
                "_id_documento"
            },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == COL_DOCUMENTO_DETALLE_OBS) {
                return esEstadoDocumentoObservadoPorFila(row);
            }
            return (column >= COL_DOCUMENTO_TIPO && column <= COL_DOCUMENTO_NOTIFICADO)
                    || column == COL_DOCUMENTO_PLANTILLA
                    || column == COL_DOCUMENTO_GUARDAR
                    || column == COL_DOCUMENTO_ACCION;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == COL_DOCUMENTO_REQUIERE_RESPUESTA ? Boolean.class : Object.class;
        }
    };
    private final JTable documentosTable = new AppV2Table(documentoModel);
    private final AbstractTableModel documentosFijosModel = new AbstractTableModel() {
        @Override
        public int getRowCount() {
            return documentosTable == null ? documentoModel.getRowCount() : documentosTable.getRowCount();
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
            return modelRow >= 0 && documentoModel.isCellEditable(modelRow, column);
        }

        @Override
        public Object getValueAt(int row, int column) {
            int modelRow = modeloDocumentoDesdeFilaFija(row);
            return modelRow < 0 ? "" : documentoModel.getValueAt(modelRow, column);
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            int modelRow = modeloDocumentoDesdeFilaFija(row);
            if (modelRow >= 0) {
                documentoModel.setValueAt(value, modelRow, column);
            }
        }
    };
    private final JTable documentosFijosTable = new AppV2Table(documentosFijosModel);
    private JScrollPane documentosScrollPane;
    private JScrollPane documentosFijosScrollPane;
    private AppV2ColumnFilterSupport.Controller documentosColumnFilterSupport;
    private JTextField filtroDocumentoAnalisis;
    private JTextField filtroDocumentoTipo;
    private final DefaultTableModel documentosAsociadosModel = new DefaultTableModel(
            new Object[]{"N° documento", "Estado", ""}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2 && puedeRecibirDocumentoAsociado(row);
        }
    };
    private final JTable documentosAsociadosTable = new AppV2Table(documentosAsociadosModel);
    private JPanel panelSolicitudesAsociadas;
    private final List<AnalisisExpedienteDTO> expedientes = new ArrayList<AnalisisExpedienteDTO>();
    private final List<AnalisisTableRow> filasTabla = new ArrayList<AnalisisTableRow>();
    private final Map<Long, List<ExpedienteRelacionadoDTO>> asociadosCache = new HashMap<Long, List<ExpedienteRelacionadoDTO>>();
    private final Set<Long> principalesExpandidos = new HashSet<Long>();
    private final Set<Long> principalesCargando = new HashSet<Long>();
    private final List<ExpedienteRelacionadoDTO> documentosAsociadosPanel = new ArrayList<ExpedienteRelacionadoDTO>();
    private final List<AnalisisItemDTO> bloquesAnalisis = new ArrayList<AnalisisItemDTO>();
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
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());
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
        JPanel metricas = new AppV2ResponsiveGridPanel(190, 5, 12, 0);
        metricas.add(cardPorRecibir);
        metricas.add(cardEnAnalisis);
        metricas.add(cardCartaIntermedia);
        metricas.add(cardObservados);
        metricas.add(cardVencimiento);
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

        panelDatosAnalisis = crearPanelDatosAnalisis();
        panelAnalisis = crearPanelAnalisis();
        JPanel panelAnalisisConTab = crearPanelAnalisisConTab(
                panelDatosAnalisis,
                panelAnalisis);
        splitOperativo = new AppV2OperationalSplitPanel(
                contenidoPrincipal,
                panelAnalisisConTab,
                0,
                PANEL_ANALISIS_ANCHO_MINIMO + PANEL_ANALISIS_TAB_OVERHANG,
                PANEL_ANALISIS_ANCHO_NORMAL + PANEL_ANALISIS_TAB_OVERHANG);
        return splitOperativo;
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
        panel.setAccentColor(new Color(10, 118, 145));
        panel.addSection(crearSelectorAnalisisPanel());
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
        panel.addSection(crearDatosSolicitudAnalisis());
        panel.addSection(crearDatosActaAnalisis());
        panel.addSection(crearDatosTitularAnalisis());
        panel.addSection(crearDatosSolicitanteAnalisis());
        panel.addSection(crearDatosNotificacionAnalisis());
        panel.addSection(crearResumenSeleccion());
        panelSolicitudesAsociadas = crearDocumentosAsociadosPanel();
        panelSolicitudesAsociadas.setVisible(false);
        panel.addSection(panelSolicitudesAsociadas);
        return panel;
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

    private AppV2SideActionPanel crearPanelPlantillasAnalisis() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de análisis", new Runnable() {
            @Override
            public void run() {
                cerrarPanelAnalisis();
            }
        });
        panel.setAccentColor(new Color(110, 78, 164));
        panel.addSection(crearPanelPlantillas());
        panel.addSection(crearExpedienteDigitalPanel());
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
        panel.add(btnEnviarVerificacion);
        panel.add(btnArchivarNoCorresponde);
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

    private AppV2SideSectionPanel crearDatosSolicitudAnalisis() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos de solicitud");
        section.addRow("Resultado inicial", lblDatosResultadoInicial);
        section.addRow("Nro. trámite web", lblDatosTramiteWeb);
        section.addRow("N° documento", lblDatosNumeroDocumentoTitular);
        section.addRow("N° expediente SGD", lblDatosExpedienteSgd);
        section.addRow("Fecha recepción", lblDatosFechaRecepcion);
        section.addRow("Procedimiento registral", lblProcedimiento);
        section.addRow("Días hábiles", lblDatosDias);
        section.addRow("Equipo actual", lblDatosEquipo);
        return section;
    }

    private AppV2SideSectionPanel crearDatosActaAnalisis() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del acta");
        section.addRow("Tipo de acta", lblDatosTipoActa);
        section.addRow("N° de acta", lblDatosNumeroActa);
        section.addRow("Acta", lblActa);
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
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Notificación y ubicación");
        section.addRow("Correo", lblDatosCorreo);
        section.addRow("Teléfono", lblDatosTelefono);
        section.addRow("Departamento", lblDatosDepartamento);
        section.addRow("Provincia", lblDatosProvincia);
        section.addRow("Distrito", lblDatosDistrito);
        section.addRow("Dirección", lblDatosDireccion);
        return section;
    }

    private AppV2SideSectionPanel crearPanelPlantillas() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Plantillas Word");
        lblPlantillaSeleccionada.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        lblPlantillaSeleccionada.setForeground(AppV2Theme.TEXT_PRIMARY);
        lblPlantillaAyuda.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblPlantillaAyuda.setForeground(AppV2Theme.TEXT_SECONDARY);
        btnDescargarPlantillaSeleccionada.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        JPanel contenido = new JPanel(new BorderLayout(0, 8));
        contenido.setOpaque(false);
        contenido.add(lblPlantillaSeleccionada, BorderLayout.NORTH);
        contenido.add(lblPlantillaAyuda, BorderLayout.CENTER);
        contenido.add(btnDescargarPlantillaSeleccionada, BorderLayout.SOUTH);
        section.addContent(contenido);
        return section;
    }

    private JPanel crearResumenSeleccion() {
        JPanel panel = section("Expediente seleccionado");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
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

    private JPanel crearSelectorAnalisisPanel() {
        JPanel panel = section("Bloques de análisis");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbBloquesAnalisis.setPreferredSize(new Dimension(240, 34));
        btnNuevoBloqueAnalisis.setToolTipText("Crear un nuevo bloque de análisis para el expediente seleccionado.");
        addRow(grid, 0, "Análisis", cmbBloquesAnalisis);
        JPanel acciones = AppV2ActionPanel.left();
        acciones.add(btnNuevoBloqueAnalisis);
        addRow(grid, 1, "", acciones);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDocumentosPanel() {
        JPanel panel = section("Documentos de análisis y cartas intermedias");
        JLabel tituloDocumentos = new JLabel("Documentos Analizados");
        tituloDocumentos.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        tituloDocumentos.setForeground(AppV2Theme.TEXT_PRIMARY);
        btnAgregarDocumento.setPreferredSize(new Dimension(42, 34));
        btnAgregarDocumento.setToolTipText("Agregar documento analizado");
        btnAgregarDocumento.setBackground(AppV2Theme.PRIMARY);
        btnAgregarDocumento.setForeground(Color.WHITE);
        btnAgregarDocumento.setFocusPainted(false);

        documentosScrollPane = new JScrollPane(documentosTable);
        documentosScrollPane.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        documentosScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        documentosScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        documentoModel.addTableModelListener(e -> actualizarAlturaDocumentosAnalizados(documentosScrollPane));
        actualizarAlturaDocumentosAnalizados(documentosScrollPane);
        documentosFijosScrollPane = construirScrollFijoDocumentos();

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        JPanel barra = new JPanel(new BorderLayout(8, 0));
        barra.setOpaque(false);
        barra.add(btnAgregarDocumento, BorderLayout.WEST);
        barra.add(tituloDocumentos, BorderLayout.CENTER);
        top.add(barra, BorderLayout.NORTH);

        JPanel contenedorTablas = new JPanel(new BorderLayout(0, 0));
        contenedorTablas.setOpaque(false);
        contenedorTablas.add(documentosFijosScrollPane, BorderLayout.WEST);
        contenedorTablas.add(documentosScrollPane, BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(contenedorTablas, BorderLayout.CENTER);
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

    private void actualizarAlturaDocumentosAnalizados(JScrollPane scroll) {
        int headerHeight = scroll.getColumnHeader() != null && scroll.getColumnHeader().getView() != null
                ? scroll.getColumnHeader().getView().getPreferredSize().height
                : documentosTable.getTableHeader().getPreferredSize().height;
        int rowCount = Math.max(1, documentosTable.getRowCount());
        int rowsHeight = rowCount * documentosTable.getRowHeight();
        int horizontalBarHeight = scroll.getHorizontalScrollBar().getPreferredSize().height;
        int extraPadding = 18;
        int height = headerHeight + rowsHeight + horizontalBarHeight + extraPadding;
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
        documentosFijosTable.getColumnModel().getColumn(1)
                .setCellEditor(new DefaultCellEditor(comboDesdeCatalogo(cmbTipoDocumento)));
        JTextField filtroAnalisis = crearCampoFiltroDocumentoFijo("Filtrar");
        JTextField filtroTipo = crearCampoFiltroDocumentoFijo("Filtrar");
        filtroDocumentoAnalisis = filtroAnalisis;
        filtroDocumentoTipo = filtroTipo;
        filtroAnalisis.setPreferredSize(new Dimension(92, 24));
        filtroTipo.setPreferredSize(new Dimension(150, 24));
        FrozenDocumentHeaderPanel header = new FrozenDocumentHeaderPanel(
                documentosFijosTable,
                documentosColumnFilterSupport,
                filtroAnalisis,
                filtroTipo);
        scroll.setColumnHeaderView(header);
        scroll.getVerticalScrollBar().setModel(documentosScrollPane.getVerticalScrollBar().getModel());
        documentosFijosTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        documentosFijosTable.setSelectionModel(documentosTable.getSelectionModel());
        filtroAnalisis.getDocument().addDocumentListener(simpleDocumentListener(() ->
                aplicarFiltroDocumentoFijo(COL_DOCUMENTO_NUMERO_ANALISIS, filtroAnalisis.getText())));
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

    private final class FrozenDocumentHeaderPanel extends JPanel {

        private static final int FIXED_HEADER_HEIGHT = 30;
        private static final int FIXED_FILTER_HEIGHT = 30;
        private final CellRendererPane rendererPane = new CellRendererPane();
        private final JTable tableRef;
        private final AppV2ColumnFilterSupport.Controller sorterSupport;
        private final JTextField filtroAnalisisRef;
        private final JTextField filtroTipoRef;

        private FrozenDocumentHeaderPanel(
                JTable tableRef,
                AppV2ColumnFilterSupport.Controller sorterSupport,
                JTextField filtroAnalisisRef,
                JTextField filtroTipoRef) {
            this.tableRef = tableRef;
            this.sorterSupport = sorterSupport;
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
                    int modelColumn = viewColumn == 0 ? COL_DOCUMENTO_NUMERO_ANALISIS : COL_DOCUMENTO_TIPO;
                    if (sorterSupport != null && sorterSupport.getSorter() != null) {
                        sorterSupport.getSorter().toggleSortOrder(modelColumn);
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
            paintHeaderCell(graphics, 0, 92, COL_DOCUMENTO_NUMERO_ANALISIS);
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

    private javax.swing.event.DocumentListener simpleDocumentListener(Runnable action) {
        return new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                action.run();
            }
        };
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
        btnBuscar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRecibir.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarAnalisis.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnEnviarVerificacion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnArchivarNoCorresponde.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnDescargarPlantillaSeleccionada.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
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
            lblDatosDias,
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

    private void configurarDocumentoTabla() {
        configurarDocumentoTablaBase(documentosTable, true);
        configurarDocumentoTablaBase(documentosFijosTable, false);
        documentosTable.setSelectionModel(documentosFijosTable.getSelectionModel());
        documentosFijosTable.setSelectionModel(documentosTable.getSelectionModel());
        if (documentosScrollPane != null) {
            documentosColumnFilterSupport = AppV2ColumnFilterSupport.install(
                    "Analisis.DocumentosAnalizados",
                    documentosTable,
                    documentosScrollPane,
                    documentosScrollPane,
                    null,
                    COL_DOCUMENTO_PLANTILLA,
                    COL_DOCUMENTO_GUARDAR,
                    COL_DOCUMENTO_ACCION,
                    COL_DOCUMENTO_TIPO_CODIGO,
                    COL_DOCUMENTO_ESTADO_CODIGO,
                    COL_DOCUMENTO_ID);
            documentosColumnFilterSupport.getFilterField(COL_DOCUMENTO_NUMERO_ANALISIS).setVisible(false);
            documentosColumnFilterSupport.getFilterField(COL_DOCUMENTO_TIPO).setVisible(false);
            documentosColumnFilterSupport.getSorter().addRowSorterListener(event -> actualizarDocumentosFijos());
            actualizarAlturaDocumentosAnalizados(documentosScrollPane);
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
        tabla.setDefaultRenderer(Object.class, new DocumentoAnalizadoRenderer());
        tabla.setDefaultRenderer(Boolean.class, new DocumentoAnalizadoCheckRenderer());
        if (principal) {
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_NOTIFICADO).setCellEditor(new DefaultCellEditor(comboSiNo()));
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_FECHA).setCellEditor(new FechaDocumentoCellEditor());
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_FECHA_ACUSE).setCellEditor(new FechaDocumentoCellEditor());
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_CONFIRMACION_RESPUESTA).setCellEditor(new DefaultCellEditor(comboConfirmacionRespuesta()));
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_FECHA_RESPUESTA).setCellEditor(new FechaDocumentoCellEditor());
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_DESCRIPCION).setCellRenderer(new DescripcionDocumentoRenderer());
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_DESCRIPCION).setCellEditor(new DescripcionDocumentoCellEditor());
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_PLANTILLA).setCellRenderer(new PlantillaDocumentoRenderer());
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_PLANTILLA).setCellEditor(new PlantillaDocumentoEditor());
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_GUARDAR).setCellRenderer(new GuardarDocumentoRenderer());
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_GUARDAR).setCellEditor(new GuardarDocumentoEditor());
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_ACCION).setCellRenderer(new EliminarDocumentoRenderer());
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_ACCION).setCellEditor(new EliminarDocumentoEditor());
        }
        int[] widths = principal
                ? new int[]{0, 0, 120, 180, 110, 120, 240, 145, 105, 165, 115, 145, 95, 42, 42, 42, 0, 0, 0}
                : new int[]{92, 150};
        int visibleCount = tabla.getColumnModel().getColumnCount();
        for (int i = 0; i < visibleCount && i < widths.length; i++) {
            int preferred = widths[i];
            int min = principal && i < 2 ? 0 : Math.min(preferred, 90);
            int max = principal && i < 2 ? 0 : preferred + 90;
            configurarColumnaDocumento(tabla, i, preferred, min, max);
        }
        if (principal) {
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_PLANTILLA).setMinWidth(42);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_PLANTILLA).setPreferredWidth(42);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_PLANTILLA).setMaxWidth(48);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_GUARDAR).setMinWidth(42);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_GUARDAR).setPreferredWidth(42);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_GUARDAR).setMaxWidth(48);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_ACCION).setMinWidth(42);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_ACCION).setPreferredWidth(42);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_ACCION).setMaxWidth(48);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_TIPO_CODIGO).setMinWidth(0);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_TIPO_CODIGO).setMaxWidth(0);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_ESTADO_CODIGO).setMinWidth(0);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_ESTADO_CODIGO).setMaxWidth(0);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_ID).setMinWidth(0);
            tabla.getColumnModel().getColumn(COL_DOCUMENTO_ID).setMaxWidth(0);
        } else {
            for (int i = tabla.getColumnModel().getColumnCount() - 1; i >= 2; i--) {
                tabla.getColumnModel().removeColumn(tabla.getColumnModel().getColumn(i));
            }
            tabla.setPreferredScrollableViewportSize(new Dimension(250, 0));
        }
    }

    private void configurarColumnaDocumento(JTable tabla, int index, int preferred, int min, int max) {
        TableColumn column = tabla.getColumnModel().getColumn(index);
        column.setPreferredWidth(preferred);
        column.setMinWidth(min);
        column.setMaxWidth(max);
    }

    private int modeloDocumentoDesdeFilaFija(int fixedRow) {
        if (fixedRow < 0 || documentosTable == null || fixedRow >= documentosTable.getRowCount()) {
            return -1;
        }
        return documentosTable.convertRowIndexToModel(fixedRow);
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

    private void ajustarAlturaDocumentoFila(int viewRow, int height) {
        documentosTable.setRowHeight(viewRow, height);
        if (viewRow >= 0 && viewRow < documentosFijosTable.getRowCount()) {
            documentosFijosTable.setRowHeight(viewRow, height);
        }
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
        btnRecibir.addActionListener(e -> recibir());
        btnRegistrarAnalisis.addActionListener(e -> registrarAnalisis());
        btnEnviarVerificacion.addActionListener(e -> enviarVerificacion());
        btnArchivarNoCorresponde.addActionListener(e -> archivarNoCorresponde());
        btnAgregarDocumento.addActionListener(e -> mostrarDialogoAgregarDocumento());
        btnQuitarDocumento.addActionListener(e -> quitarDocumento());
        btnNuevoBloqueAnalisis.addActionListener(e -> crearNuevoBloqueAnalisis());
        btnDescargarPlantillaSeleccionada.addActionListener(e -> descargarPlantillaDocumentoSeleccionado());
        cmbBloquesAnalisis.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                SwingUtilities.invokeLater(this::seleccionarBloqueAnalisisActual);
            }
        });
        cmbIncorporado.addActionListener(e -> actualizarChecksIncorporado());
        cmbResultado.addActionListener(e -> actualizarResultadoSeleccionado());
        chkRegistrarObservacion.addActionListener(e -> actualizarObservacionHabilitada());
        documentosTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarPlantillaSeleccionada();
            }
        });
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
        configurarEditoresCatalogoDocumentoTabla();
    }

    private void cargarSimpleItems(JComboBox<SimpleItem> combo, List<CatalogoItemDTO> items, String placeholder) {
        combo.removeAllItems();
        combo.addItem(new SimpleItem("", placeholder));
        for (CatalogoItemDTO item : items) {
            combo.addItem(new SimpleItem(item.getCodigo(), item.getNombre()));
        }
    }

    private void configurarEditoresCatalogoDocumentoTabla() {
        documentosTable.getColumnModel().getColumn(COL_DOCUMENTO_TIPO)
                .setCellEditor(new DefaultCellEditor(comboDesdeCatalogo(cmbTipoDocumento)));
        documentosTable.getColumnModel().getColumn(COL_DOCUMENTO_ESTADO)
                .setCellEditor(new DefaultCellEditor(comboDesdeCatalogo(cmbEstadoDocumento)));
        documentosFijosTable.getColumnModel().getColumn(1)
                .setCellEditor(new DefaultCellEditor(comboDesdeCatalogo(cmbTipoDocumento)));
    }

    private JComboBox<SimpleItem> comboDesdeCatalogo(JComboBox<SimpleItem> origen) {
        JComboBox<SimpleItem> combo = new JComboBox<SimpleItem>();
        combo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        for (int i = 0; i < origen.getItemCount(); i++) {
            SimpleItem item = origen.getItemAt(i);
            if (item != null && item.codigo != null && !item.codigo.trim().isEmpty()) {
                combo.addItem(item);
            }
        }
        return combo;
    }

    private JComboBox<String> comboSiNo() {
        JComboBox<String> combo = new JComboBox<String>(new String[]{"No", "Si"});
        combo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        return combo;
    }

    private JComboBox<String> comboConfirmacionRespuesta() {
        JComboBox<String> combo = new JComboBox<String>(new String[]{"Pendiente", "Si", "No"});
        combo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        return combo;
    }

    private void buscar() {
        buscar(null);
    }

    private void buscar(Long idExpedienteASeleccionar) {
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
                    if (idExpedienteASeleccionar != null) {
                        seleccionarFilaPorId(idExpedienteASeleccionar);
                    }
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
            agregarFilaPrincipal(item);
        }
        cardPorRecibir.setValue(String.valueOf(porRecibir));
        cardEnAnalisis.setValue(String.valueOf(enAnalisis));
        cardCartaIntermedia.setValue(String.valueOf(cartasIntermedias));
        cardObservados.setValue(String.valueOf(observados));
        cardVencimiento.setValue(String.valueOf(vencimientoCritico));
        lblEstado.setText(items.isEmpty()
                ? "No se encontraron expedientes para análisis."
                : items.size() + " expediente(s) encontrados.");
        tablePanel.setEmpty(items.isEmpty());
        actualizarSeleccion();
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
        btnRegistrarAnalisis.setEnabled(has && !asociado && item.isRegistrable());
        btnEnviarVerificacion.setEnabled(has && !asociado && item.isEnviableVerificacion());
        btnArchivarNoCorresponde.setEnabled(has && !asociado && item.isArchivableNoCorresponde());
        actualizarVisibilidadPanelAnalisis();
        if (!has) {
            idExpedienteDetalleSolicitado = null;
            idExpedienteDetalleCargado = null;
            lblExpediente.setText("-");
            lblTitular.setText("-");
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
            lblDatosDias,
            lblDatosEquipo
        };
        for (JLabel label : labels) {
            label.setText("-");
            label.setToolTipText(null);
        }
    }

    private void cargarDatosExpedienteAnalisis(AnalisisExpedienteDTO item) {
        if (item == null) {
            limpiarDatosExpedienteAnalisis();
            return;
        }
        lblDatosResultadoInicial.setText("Corresponde a SDRERC");
        lblDatosTramiteWeb.setText(valorUi(item.getNumeroTramiteDocumentario()));
        lblDatosNumeroDocumentoTitular.setText(valorUi(item.getNumeroDocumentoTitular()));
        lblDatosExpedienteSgd.setText(valorUi(item.getNumeroExpedienteSgd()));
        lblDatosFechaRecepcion.setText(formatDate(item.getFechaRecepcion()));
        lblDatosTipoActa.setText(valorUi(item.getTipoActa()));
        lblDatosNumeroActa.setText(valorUi(item.getNumeroActa()));
        lblDatosTipoDocumentoTitular.setText(valorUi(item.getTipoDocumento()));
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
        lblDatosDias.setText(item.getDiasEnEtapa() == null ? "-" : item.getDiasEnEtapa() + " día(s)");
        lblDatosEquipo.setText(valorUi(item.getEquipo()));
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
                    aplicarBloquesAnalisis(get());
                    idExpedienteDetalleCargado = idExpediente;
                } catch (Exception ex) {
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
                && documentoModel.getRowCount() == 0
                && cmbEstadoDocumento.getItemCount() > 1) {
            cmbEstadoDocumento.setSelectedIndex(1);
        }
        actualizarObservacionHabilitada();
    }

    private void cargarDocumentosAnalizados(List<DocumentoAnalizadoDTO> documentos) {
        documentoModel.setRowCount(0);
        if (documentos == null) {
            return;
        }
        for (DocumentoAnalizadoDTO documento : documentos) {
            documentoModel.addRow(new Object[]{
                etiquetaAnalisisDocumento(documento),
                new SimpleItem(documento.getTipoDocumentoCodigo(), documento.getTipoDocumentoNombre()),
                new SimpleItem(documento.getEstadoDocumentoCodigo(), documento.getEstadoDocumentoNombre()),
                documento.getDetalleObservacion(),
                documento.getFechaDocumento() == null ? "-" : DATE_FORMAT.format(documento.getFechaDocumento()),
                valueOrEmpty(documento.getNumeroDocumento()),
                documento.getDescripcion(),
                documento.isRequiereRespuesta(),
                documento.getFechaAcuse() == null ? "" : DATE_FORMAT.format(documento.getFechaAcuse()),
                confirmacionRespuestaUi(documento.getConfirmacionRespuesta(), documento.isRequiereRespuesta()),
                documento.getFechaRespuesta() == null ? "" : DATE_FORMAT.format(documento.getFechaRespuesta()),
                valueOrEmpty(documento.getNumeroHojaEnvioRespuesta()),
                documento.isNotificado() ? "Si" : "No",
                "",
                "",
                "",
                documento.getTipoDocumentoCodigo(),
                documento.getEstadoDocumentoCodigo(),
                documento.getIdDocumentoAnalizado()
            });
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
                    valorUi(asociado.getNumeroDocumento()),
                    recibido ? "Recibido" : "Pendiente de recibir",
                    recibido ? "Recibido" : "Recibir"
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
        List<DocumentoAnalizadoDTO> documentos = new ArrayList<DocumentoAnalizadoDTO>();
        for (int i = 0; i < documentoModel.getRowCount(); i++) {
            documentos.add(documentoDesdeFila(i));
        }
        return documentos;
    }

    private DocumentoAnalizadoDTO documentoDesdeFila(int i) {
        return new DocumentoAnalizadoDTO(
                longValue(documentoModel.getValueAt(i, COL_DOCUMENTO_ID)),
                null,
                idAnalisisSeleccionado,
                codigoDocumentoFila(i, COL_DOCUMENTO_TIPO, COL_DOCUMENTO_TIPO_CODIGO, cmbTipoDocumento),
                nombreDocumentoFila(i, COL_DOCUMENTO_TIPO),
                codigoDocumentoFila(i, COL_DOCUMENTO_ESTADO, COL_DOCUMENTO_ESTADO_CODIGO, cmbEstadoDocumento),
                nombreDocumentoFila(i, COL_DOCUMENTO_ESTADO),
                localDateValue(documentoModel.getValueAt(i, COL_DOCUMENTO_FECHA)),
                valueOrEmpty(documentoModel.getValueAt(i, COL_DOCUMENTO_NUMERO)),
                value(documentoModel.getValueAt(i, COL_DOCUMENTO_DESCRIPCION)),
                esSi(documentoModel.getValueAt(i, COL_DOCUMENTO_NOTIFICADO)),
                localDateOptional(documentoModel.getValueAt(i, COL_DOCUMENTO_FECHA_ACUSE)),
                Boolean.TRUE.equals(documentoModel.getValueAt(i, COL_DOCUMENTO_REQUIERE_RESPUESTA)),
                confirmacionRespuestaDb(documentoModel.getValueAt(i, COL_DOCUMENTO_CONFIRMACION_RESPUESTA)),
                localDateOptional(documentoModel.getValueAt(i, COL_DOCUMENTO_FECHA_RESPUESTA)),
                valueOrEmpty(documentoModel.getValueAt(i, COL_DOCUMENTO_HOJA_ENVIO_RESPUESTA)),
                false,
                null,
                esEstadoDocumentoObservadoPorFila(i)
                        ? valueOrEmpty(documentoModel.getValueAt(i, COL_DOCUMENTO_DETALLE_OBS))
                        : "");
    }

    private void mostrarDialogoAgregarDocumento() {
        resetearDocumentoFormulario();
        txtDescripcionDocumento.setText("");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        int row = 0;
        addRow(form, row++, "Tipo", cmbTipoDocumento);
        addRow(form, row++, "Estado", cmbEstadoDocumento);
        addRow(form, row++, "Fecha", fechaDocumentoAnalizado);
        addRow(form, row++, "Descripción", scrollText(txtDescripcionDocumento, 58));
        addRow(form, row, "", chkDocumentoRequiereRespuesta);
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                form,
                "Agregar documento analizado",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (respuesta == JOptionPane.OK_OPTION) {
            agregarDocumento();
        }
    }

    private void crearNuevoBloqueAnalisis() {
        AnalisisExpedienteDTO item = requerirSeleccion("Seleccione un expediente para crear un nuevo análisis.");
        if (item == null) {
            return;
        }
        setTrabajando(true, "Creando nuevo análisis...");
        SwingWorker<AnalisisItemDTO, Void> worker = new SwingWorker<AnalisisItemDTO, Void>() {
            @Override
            protected AnalisisItemDTO doInBackground() throws Exception {
                return analisisService.crearBloqueAnalisis(item.getIdExpediente());
            }

            @Override
            protected void done() {
                try {
                    AnalisisItemDTO nuevo = get();
                    recargarDetalleSeleccionado();
                    JOptionPane.showMessageDialog(
                            JPanelAnalisisV2.this,
                            "Se creó " + nuevo.getTitulo() + ".",
                            "Análisis",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    mostrarError("No se pudo crear el nuevo análisis. Verifique que el script 38_analisis_multiple.sql esté aplicado.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void agregarDocumento() {
        SimpleItem tipo = (SimpleItem) cmbTipoDocumento.getSelectedItem();
        SimpleItem estado = (SimpleItem) cmbEstadoDocumento.getSelectedItem();
        LocalDate fechaDocumento = fechaSeleccionada(fechaDocumentoAnalizado);
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
            etiquetaAnalisisActual(),
            tipo,
            estado,
            "",
            DATE_FORMAT.format(fechaDocumento == null ? LocalDate.now() : fechaDocumento),
            "",
            descripcion,
            chkDocumentoRequiereRespuesta.isSelected(),
            "",
            "Pendiente",
            "",
            "",
            "No",
            "",
            "",
            "",
            tipo.codigo,
            estado.codigo,
            null
        });
        int lastRow = documentoModel.getRowCount() - 1;
        if (lastRow >= 0) {
            documentosTable.getSelectionModel().setSelectionInterval(lastRow, lastRow);
        }
        txtDescripcionDocumento.setText("");
        resetearDocumentoFormulario();
        actualizarPlantillaSeleccionada();
    }

    private void resetearDocumentoFormulario() {
        if (cmbTipoDocumento.getItemCount() > 0) {
            cmbTipoDocumento.setSelectedIndex(0);
        }
        if (cmbEstadoDocumento.getItemCount() > 0) {
            cmbEstadoDocumento.setSelectedIndex(0);
        }
        fechaDocumentoAnalizado.setDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        chkDocumentoRequiereRespuesta.setSelected(false);
    }

    private void quitarDocumento() {
        int row = documentosTable.getSelectedRow();
        if (row < 0) {
            mostrarInfo("Seleccione un documento para quitarlo del registro.");
            return;
        }
        quitarDocumentoFila(documentosTable.convertRowIndexToModel(row));
    }

    private void quitarDocumentoFila(int modelRow) {
        if (modelRow < 0 || modelRow >= documentoModel.getRowCount()) {
            return;
        }
        documentoModel.removeRow(modelRow);
        actualizarPlantillaSeleccionada();
    }

    private void guardarDocumentoFila(int modelRow) {
        if (modelRow < 0 || modelRow >= documentoModel.getRowCount()) {
            return;
        }
        AnalisisExpedienteDTO item = requerirSeleccion("Seleccione un expediente para guardar el documento.");
        if (item == null) {
            return;
        }
        if (!puedeGuardarDocumentos(item)) {
            mostrarInfo("El expediente seleccionado no permite guardar documentos de análisis.");
            return;
        }
        DocumentoAnalizadoDTO documento = documentoDesdeFila(modelRow);
        setTrabajando(true, "Guardando documento de análisis...");
        SwingWorker<AnalisisResultadoDTO, Void> worker = new SwingWorker<AnalisisResultadoDTO, Void>() {
            @Override
            protected AnalisisResultadoDTO doInBackground() throws Exception {
                return analisisService.guardarDocumentoAnalisis(item.getIdExpediente(), documento);
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
                    recargarDetalleSeleccionado();
                } catch (Exception ex) {
                    mostrarError("No se pudo guardar el documento de análisis.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void descargarPlantillaDocumento(int modelRow) {
        if (modelRow < 0 || modelRow >= documentoModel.getRowCount()) {
            return;
        }
        AnalisisExpedienteDTO item = requerirSeleccion("Seleccione un expediente para descargar la plantilla.");
        if (item == null) {
            return;
        }
        DocumentoAnalizadoDTO documento = documentoDesdeFila(modelRow);
        if (documento.getTipoDocumentoNombre().trim().isEmpty()) {
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
        Long idAnterior = idAnalisisSeleccionado;
        cargandoBloquesAnalisis = true;
        bloquesAnalisis.clear();
        cmbBloquesAnalisis.removeAllItems();
        if (items != null) {
            bloquesAnalisis.addAll(items);
        }
        if (bloquesAnalisis.isEmpty()) {
            bloquesAnalisis.add(new AnalisisItemDTO(null, 1, "Análisis 1", "TEMPORAL", null));
        }
        for (AnalisisItemDTO item : bloquesAnalisis) {
            cmbBloquesAnalisis.addItem(item);
        }
        seleccionarBloquePorId(idAnterior);
        if (cmbBloquesAnalisis.getSelectedIndex() < 0 && cmbBloquesAnalisis.getItemCount() > 0) {
            cmbBloquesAnalisis.setSelectedIndex(0);
        }
        cargandoBloquesAnalisis = false;
        seleccionarBloqueAnalisisActual();
    }

    private void seleccionarBloqueAnalisisActual() {
        if (cargandoBloquesAnalisis) {
            return;
        }
        AnalisisItemDTO item = (AnalisisItemDTO) cmbBloquesAnalisis.getSelectedItem();
        idAnalisisSeleccionado = item == null ? null : item.getIdExpedienteAnalisis();
        aplicarAnalisisRegistrado(item == null ? null : item.getDetalle());
    }

    private void seleccionarBloquePorId(Long idExpedienteAnalisis) {
        if (idExpedienteAnalisis == null) {
            return;
        }
        for (int i = 0; i < cmbBloquesAnalisis.getItemCount(); i++) {
            AnalisisItemDTO item = cmbBloquesAnalisis.getItemAt(i);
            if (item != null && idExpedienteAnalisis.equals(item.getIdExpedienteAnalisis())) {
                cmbBloquesAnalisis.setSelectedIndex(i);
                return;
            }
        }
    }

    private String etiquetaAnalisisActual() {
        return etiquetaAnalisisDocumento(idAnalisisSeleccionado);
    }

    private String etiquetaAnalisisDocumento(DocumentoAnalizadoDTO documento) {
        return documento == null
                ? etiquetaAnalisisActual()
                : etiquetaAnalisisDocumento(documento.getIdExpedienteAnalisis());
    }

    private String etiquetaAnalisisDocumento(Long idExpedienteAnalisis) {
        AnalisisItemDTO item = buscarBloqueAnalisis(idExpedienteAnalisis);
        if (item != null) {
            return item.getTitulo();
        }
        if (idExpedienteAnalisis != null) {
            return "Análisis " + idExpedienteAnalisis;
        }
        AnalisisItemDTO seleccionado = (AnalisisItemDTO) cmbBloquesAnalisis.getSelectedItem();
        return seleccionado == null ? "Análisis 1" : seleccionado.getTitulo();
    }

    private AnalisisItemDTO buscarBloqueAnalisis(Long idExpedienteAnalisis) {
        if (idExpedienteAnalisis == null) {
            return null;
        }
        for (AnalisisItemDTO item : bloquesAnalisis) {
            if (item != null && idExpedienteAnalisis.equals(item.getIdExpedienteAnalisis())) {
                return item;
            }
        }
        return null;
    }

    private void descargarPlantillaDocumentoSeleccionado() {
        int row = documentosTable.getSelectedRow();
        if (row < 0) {
            mostrarInfo("Seleccione un documento analizado para descargar su plantilla.");
            return;
        }
        descargarPlantillaDocumento(documentosTable.convertRowIndexToModel(row));
    }

    private void actualizarPlantillaSeleccionada() {
        int row = documentosTable.getSelectedRow();
        if (row < 0) {
            lblPlantillaSeleccionada.setText("Seleccione un documento analizado para descargar su plantilla.");
            return;
        }
        int modelRow = documentosTable.convertRowIndexToModel(row);
        String tipo = valueOrEmpty(documentoModel.getValueAt(modelRow, COL_DOCUMENTO_TIPO));
        String descripcion = valueOrEmpty(documentoModel.getValueAt(modelRow, COL_DOCUMENTO_DESCRIPCION));
        lblPlantillaSeleccionada.setText(
                tipo.isEmpty() ? "Documento analizado seleccionado"
                        : "Plantilla: " + tipo + (descripcion.isEmpty() ? "" : " · " + descripcion));
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
        documentosTable.setEnabled(habilitar);
        btnAgregarDocumento.setEnabled(habilitar);
        btnQuitarDocumento.setEnabled(habilitar);
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
        documentoModel.setRowCount(0);
        actualizarPlantillaSeleccionada();
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

    private static String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static Long longValue(Object value) {
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

    private static LocalDate localDateValue(Object value) {
        String text = value(value).trim();
        if (text.isEmpty() || "-".equals(text)) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (Exception ex) {
            return LocalDate.now();
        }
    }

    private static LocalDate localDateOptional(Object value) {
        String text = value(value).trim();
        if (text.isEmpty() || "-".equals(text)) {
            return null;
        }
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (Exception ex) {
            return null;
        }
    }

    private static boolean esSi(Object value) {
        String text = value(value).trim().toUpperCase(java.util.Locale.ROOT)
                .replace("Í", "I");
        return "SI".equals(text) || "S".equals(text);
    }

    private static String confirmacionRespuestaUi(String value, boolean requiereRespuesta) {
        if (!requiereRespuesta) {
            return "Pendiente";
        }
        String normalized = value(value).trim().toUpperCase(java.util.Locale.ROOT)
                .replace("Í", "I");
        if ("SI".equals(normalized)) {
            return "Si";
        }
        if ("NO".equals(normalized)) {
            return "No";
        }
        return "Pendiente";
    }

    private static String confirmacionRespuestaDb(Object value) {
        String normalized = value(value).trim().toUpperCase(java.util.Locale.ROOT)
                .replace("Í", "I");
        if ("SI".equals(normalized) || "NO".equals(normalized) || "PENDIENTE".equals(normalized)) {
            return normalized;
        }
        return "PENDIENTE";
    }

    private String codigoDocumentoFila(
            int row,
            int visibleColumn,
            int hiddenColumn,
            JComboBox<SimpleItem> catalogo) {
        Object visible = documentoModel.getValueAt(row, visibleColumn);
        if (visible instanceof SimpleItem) {
            return ((SimpleItem) visible).codigo;
        }
        String hidden = value(documentoModel.getValueAt(row, hiddenColumn)).trim();
        if (!hidden.isEmpty()) {
            return hidden;
        }
        String nombre = value(visible).trim();
        for (int i = 0; i < catalogo.getItemCount(); i++) {
            SimpleItem item = catalogo.getItemAt(i);
            if (item != null && item.nombre.equalsIgnoreCase(nombre)) {
                return item.codigo;
            }
        }
        return "";
    }

    private String nombreDocumentoFila(int row, int visibleColumn) {
        Object visible = documentoModel.getValueAt(row, visibleColumn);
        if (visible instanceof SimpleItem) {
            return ((SimpleItem) visible).nombre;
        }
        return value(visible);
    }

    private static String valueOrEmpty(Object value) {
        String text = value(value).trim();
        return "-".equals(text) ? "" : text;
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

    private class DocumentoAnalizadoRenderer extends DefaultTableCellRenderer {

        private DocumentoAnalizadoRenderer() {
            setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = value instanceof SimpleItem ? ((SimpleItem) value).toString() : value(value);
            setText(text == null || text.trim().isEmpty() ? "-" : text);
            setToolTipText(getText());
            setHorizontalAlignment(column == COL_DOCUMENTO_NUMERO_ANALISIS
                    || column == COL_DOCUMENTO_FECHA
                    || column == COL_DOCUMENTO_NOTIFICADO
                    || column == COL_DOCUMENTO_FECHA_ACUSE
                    || column == COL_DOCUMENTO_CONFIRMACION_RESPUESTA
                    || column == COL_DOCUMENTO_FECHA_RESPUESTA
                            ? SwingConstants.CENTER
                            : SwingConstants.LEFT);
            if (isSelected) {
                setBackground(TABLE_SELECTION_BACKGROUND);
                setForeground(TABLE_SELECTION_FOREGROUND);
            } else {
                int modelRow = table == documentosFijosTable
                        ? modeloDocumentoDesdeFilaFija(row)
                        : table.convertRowIndexToModel(row);
                setBackground(colorDocumentoAnalizado(table, column));
                setForeground(column == COL_DOCUMENTO_DETALLE_OBS
                        && !esEstadoDocumentoObservadoPorFila(modelRow)
                        ? AppV2Theme.TEXT_SECONDARY
                        : AppV2Theme.TEXT_PRIMARY);
            }
            return component;
        }
    }

    private class DescripcionDocumentoRenderer extends JTextArea implements TableCellRenderer {

        private DescripcionDocumentoRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
            int row,
            int column) {
            String text = value == null ? "" : value.toString();
            setText(text.isEmpty() ? "-" : text);
            setSize(table.getColumnModel().getColumn(column).getWidth(), Short.MAX_VALUE);
            int height = Math.max(30, getPreferredSize().height + 8);
            if (documentosTable.getRowHeight(row) != height || documentosFijosTable.getRowHeight(row) != height) {
                ajustarAlturaDocumentoFila(row, height);
            }
            if (isSelected) {
                setBackground(TABLE_SELECTION_BACKGROUND);
                setForeground(TABLE_SELECTION_FOREGROUND);
            } else {
                setBackground(colorDocumentoAnalizado(table, column));
                setForeground(AppV2Theme.TEXT_PRIMARY);
            }
            setToolTipText(text.isEmpty() ? null : text);
            return this;
        }
    }

    private class DocumentoAnalizadoCheckRenderer extends JCheckBox implements TableCellRenderer {

        private DocumentoAnalizadoCheckRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
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
            setSelected(Boolean.TRUE.equals(value));
            setBackground(isSelected ? TABLE_SELECTION_BACKGROUND : colorDocumentoAnalizado(table, column));
            setToolTipText(Boolean.TRUE.equals(value)
                    ? "Requiere respuesta."
                    : "No requiere respuesta.");
            return this;
        }
    }

    private Color colorDocumentoAnalizado(JTable table, int viewColumn) {
        int modelColumn = table.convertColumnIndexToModel(viewColumn);
        if (modelColumn >= COL_DOCUMENTO_NUMERO_ANALISIS && modelColumn <= COL_DOCUMENTO_REQUIERE_RESPUESTA) {
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

    private boolean esEstadoDocumentoObservadoPorFila(int modelRow) {
        if (modelRow < 0 || modelRow >= documentoModel.getRowCount()) {
            return false;
        }
        Object estado = documentoModel.getValueAt(modelRow, COL_DOCUMENTO_ESTADO);
        String codigo = estado instanceof SimpleItem ? ((SimpleItem) estado).codigo : "";
        if (codigo.isEmpty()) {
            codigo = valueOrEmpty(documentoModel.getValueAt(modelRow, COL_DOCUMENTO_ESTADO_CODIGO));
        }
        return "OBSERVADO".equalsIgnoreCase(codigo);
    }

    private class FechaDocumentoCellEditor extends AbstractCellEditor implements TableCellEditor {

        private final PremiumDateFieldV2 field = new PremiumDateFieldV2();

        private FechaDocumentoCellEditor() {
            field.setPreferredSize(new Dimension(120, 34));
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
            LocalDate date = localDateOptional(value);
            field.setDate(date == null ? null : Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            return field;
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

    private class GuardarDocumentoRenderer extends JButton implements TableCellRenderer {
        private GuardarDocumentoRenderer() {
            setText("");
            setIcon(new SaveDocumentIcon());
            setToolTipText("Guardar documento");
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

    private class PlantillaDocumentoRenderer extends JButton implements TableCellRenderer {
        private PlantillaDocumentoRenderer() {
            setText("");
            setIcon(new WordDocumentIcon());
            setToolTipText("Descargar plantilla Word");
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

    private class PlantillaDocumentoEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton();
        private int modelRow = -1;

        private PlantillaDocumentoEditor() {
            button.setText("");
            button.setIcon(new WordDocumentIcon());
            button.setToolTipText("Descargar plantilla Word");
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.addActionListener(e -> {
                int row = modelRow;
                fireEditingStopped();
                descargarPlantillaDocumento(row);
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

    private class EliminarDocumentoRenderer extends JButton implements TableCellRenderer {
        private EliminarDocumentoRenderer() {
            setText("");
            setIcon(new DeleteDocumentIcon());
            setToolTipText("Quitar documento");
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
            button.setToolTipText("Guardar documento");
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.addActionListener(e -> {
                int row = modelRow;
                fireEditingStopped();
                guardarDocumentoFila(row);
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

    private static class WordDocumentIcon implements Icon {
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
                Color fill = new Color(232, 244, 252);
                g2.setColor(fill);
                g2.fillRoundRect(x + 3, y + 1, 12, 16, 3, 3);
                g2.setColor(stroke);
                g2.drawRoundRect(x + 3, y + 1, 12, 16, 3, 3);
                g2.drawLine(x + 6, y + 6, x + 12, y + 6);
                g2.drawLine(x + 6, y + 9, x + 12, y + 9);
                g2.drawLine(x + 6, y + 12, x + 10, y + 12);
                g2.setColor(new Color(29, 92, 151));
                g2.fillRoundRect(x + 1, y + 5, 8, 8, 2, 2);
                g2.setColor(Color.WHITE);
                g2.drawLine(x + 3, y + 7, x + 4, y + 11);
                g2.drawLine(x + 4, y + 11, x + 5, y + 8);
                g2.drawLine(x + 5, y + 8, x + 6, y + 11);
                g2.drawLine(x + 6, y + 11, x + 7, y + 7);
            } finally {
                g2.dispose();
            }
        }
    }

    private static class SaveDocumentIcon implements Icon {
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

    private static class DeleteDocumentIcon implements Icon {
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
                Color stroke = new Color(196, 53, 53);
                Color fill = new Color(253, 237, 237);
                g2.setColor(fill);
                g2.fillRoundRect(x + 2, y + 2, 14, 14, 7, 7);
                g2.setColor(stroke);
                g2.drawRoundRect(x + 2, y + 2, 14, 14, 7, 7);
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawLine(x + 6, y + 6, x + 12, y + 12);
                g2.drawLine(x + 12, y + 6, x + 6, y + 12);
            } finally {
                g2.dispose();
            }
        }
    }

    private class EliminarDocumentoEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton();
        private int modelRow = -1;

        private EliminarDocumentoEditor() {
            button.setText("");
            button.setIcon(new DeleteDocumentIcon());
            button.setToolTipText("Quitar documento");
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.addActionListener(e -> {
                int row = modelRow;
                fireEditingStopped();
                quitarDocumentoFila(row);
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

    private class DescripcionDocumentoCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JTextArea area = new JTextArea();
        private final JScrollPane scroll = new JScrollPane(area);
        private int viewRow = -1;

        private DescripcionDocumentoCellEditor() {
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            area.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            area.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    ajustarAltura();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    ajustarAltura();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    ajustarAltura();
                }
            });
        }

        @Override
        public Object getCellEditorValue() {
            return area.getText();
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
            viewRow = row;
            area.setText(value == null ? "" : value.toString());
            SwingUtilities.invokeLater(() -> {
                area.setCaretPosition(area.getDocument().getLength());
                ajustarAltura();
            });
            return scroll;
        }

        private void ajustarAltura() {
            if (viewRow < 0) {
                return;
            }
            int columnWidth = documentosTable.getColumnModel().getColumn(COL_DOCUMENTO_DESCRIPCION).getWidth();
            int lines = Math.max(2, Math.min(10, area.getLineCount() + 1));
            int height = Math.max(54, lines * 19 + 18);
            ajustarAlturaDocumentoFila(viewRow, height);
            scroll.setPreferredSize(new Dimension(Math.max(220, columnWidth), height));
            scroll.revalidate();
            documentosTable.revalidate();
            documentosFijosTable.revalidate();
            documentosTable.repaint();
            documentosFijosTable.repaint();
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
                            : "Solo el abogado responsable puede registrar la recepción.");
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
