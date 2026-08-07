package com.sdrerc.ui.views.notificacion;

import com.sdrerc.application.sdrercapp.DocumentoEjecucionService;
import com.sdrerc.application.sdrercapp.NotificacionExpedienteService;
import com.sdrerc.domain.dto.sdrercapp.CargoAcuseDTO;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.CierreNotificacionDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoEjecucionDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.PublicacionRequeridaDTO;
import com.sdrerc.shared.session.SessionContext;
import com.sdrerc.ui.appv2.components.AppV2ActionPanel;
import com.sdrerc.ui.appv2.components.AppV2ColumnFilterSupport;
import com.sdrerc.ui.appv2.components.AppV2ExpandCollapseGlyph;
import com.sdrerc.ui.appv2.components.AppV2ExpedientePanelFactory;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.components.AppV2ResponsiveGridPanel;
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2SideActionPanel;
import com.sdrerc.ui.appv2.components.AppV2SideSectionPanel;
import com.sdrerc.ui.appv2.components.AppV2StackedSideTab;
import com.sdrerc.ui.appv2.components.AppV2StepCardPanel;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.components.AppV2TablePanel;
import com.sdrerc.ui.appv2.components.AppV2TableSectionPanel;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.PremiumDateFieldV2;
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.helpers.EstadoExpedienteComboSupportV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import com.sdrerc.ui.views.expedienteconsola.DlgConsolaExpedienteV2;
import com.sdrerc.util.DateRangePickerSupport;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
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
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class JPanelNotificacionV2 extends JPanel {

    private enum FiltroKpi {
        TODOS,
        PENDIENTES,
        EN_REVISION,
        NOTIFICADOS,
        FALLIDOS,
        PUBLICACION,
        PLAZO_CRITICO
    }

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int PANEL_NOTIFICACION_ANCHO_MINIMO = 380;
    private static final int PANEL_NOTIFICACION_ANCHO_NORMAL = 430;
    private static final int PANEL_NOTIFICACION_TAB_OVERHANG = 46;
    private static final int PANEL_NOTIFICACION_TAB_TOP = 18;
    private static final int PANEL_NOTIFICACION_TAB_HEIGHT = 140;
    private static final String TAB_NOTIF_PANEL_NOTIFICACION = "NOTIFICACION";
    private static final String TAB_NOTIF_PANEL_CIERRE = "CIERRE";
    private static final int TAB_BANDEJA_NOTIF_ASIGNACION = 0;
    private static final int TAB_BANDEJA_NOTIF_VALIDACION = 1;
    private static final int TAB_BANDEJA_NOTIF_NOTIFICACION = 2;
    private static final String PERMISO_BANDEJA_NOTIFICACION_ASIGNACION = "BANDEJA_NOTIFICACION_ASIGNACION";
    private static final String PERMISO_BANDEJA_NOTIFICACION_VALIDACION = "BANDEJA_NOTIFICACION_VALIDACION";
    private static final String PERMISO_BANDEJA_NOTIFICACION_NOTIFICACION = "BANDEJA_NOTIFICACION_NOTIFICACION";

    private final NotificacionExpedienteService notificacionService;
    private final DocumentoEjecucionService documentoService;

    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite/SGD, acta, titular o documento", 28);
    private final JComboBox<SimpleItem> cmbEstadoFiltro = new JComboBox<SimpleItem>();
    private final PremiumDateFieldV2 fechaSolicitudDesde = new PremiumDateFieldV2();
    private final PremiumDateFieldV2 fechaSolicitudHasta = new PremiumDateFieldV2();
    private final JComboBox<SimpleItem> cmbTipoNotificacionFiltro = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbResultadoFiltro = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbPublicacionFiltro = new JComboBox<SimpleItem>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnRegistrarNotificacion = new JButton("Registrar notificación");
    private final JButton btnRegistrarCargo = new JButton("Registrar cargo");
    private final JButton btnMarcarNotificado = new JButton("Marcar notificado");
    private final JButton btnRequierePublicacion = new JButton("Preparar publicación");
    private final JButton btnCerrarExpediente = new JButton("Cerrar expediente");

    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar expedientes en Notificación.");
    private final JLabel lblExpediente = new JLabel("-");
    private final JLabel lblExpedienteSgd = new JLabel("-");
    private final JLabel lblTitular = new JLabel("-");
    private final JLabel lblActa = new JLabel("-");
    private final JLabel lblProcedimiento = new JLabel("-");
    private final JLabel lblEtapaEstado = new JLabel("-");
    private final JLabel lblResolucion = new JLabel("-");
    private final JLabel lblDocumentoNotificar = new JLabel("-");
    private final JLabel lblNotificacion = new JLabel("-");
    private final JLabel lblIntentos = new JLabel("-");
    private final JLabel lblCargo = new JLabel("-");
    private final JLabel lblSupervisor = new JLabel("-");
    private final JLabel lblPublicacion = new JLabel("-");
    private final JLabel lblDestino = new JLabel("-");
    private final JLabel lblAlertas = new JLabel("Sin alertas.");
    private final JLabel lblAcciones = new JLabel("-");
    private final JLabel lblAnalisis = new JLabel("-");
    private final JLabel lblVerificacion = new JLabel("-");
    private final JLabel lblEjecucion = new JLabel("-");
    private final JLabel lblCierreDestino = new JLabel("-");
    private final JLabel lblCierrePublicacion = new JLabel("-");
    private final JLabel lblCierreAlertas = new JLabel("Sin alertas.");

    private final JComboBox<SimpleItem> cmbTipoNotificacion = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbEstadoCargo = new JComboBox<SimpleItem>();
    private final JTextField txtFechaNotificacion = new JTextField(10);
    private final JTextField txtFechaCargo = new JTextField(10);
    private final JTextField txtDestinatario = new JTextField(22);
    private final JTextField txtResultado = new JTextField(22);
    private final JTextField txtRecibidoPor = new JTextField(22);
    private final JTextArea txtComentario = new JTextArea(4, 22);
    private final JTextArea txtMotivoPublicacion = new JTextArea(3, 22);
    private final JTextArea txtObservacion = new JTextArea(3, 22);
    private final JTextArea txtComentarioCierre = new JTextArea(4, 22);
    private final AppV2StackedSideTab tabNotifPanelNotificacion =
            crearTabPanelNotificacion("Notificación", new Color(230, 241, 245), new Color(57, 125, 199));
    private final AppV2StackedSideTab tabNotifPanelCierre =
            crearTabPanelNotificacion("Cierre", new Color(224, 243, 240), new Color(10, 118, 145));
    private CardLayout panelNotifCardsLayout;
    private JPanel panelNotifCards;
    private String tabNotifPanelActiva = TAB_NOTIF_PANEL_NOTIFICACION;

    private final NotificacionTableModel tableModel = new NotificacionTableModel();
    private final JTable table = new AppV2Table(tableModel);
    private final AppV2TablePanel tablePanel = new AppV2TablePanel(
            table,
            "Sin expedientes para mostrar",
            "Seleccione filtros y presione Buscar.");
    private AppV2ColumnFilterSupport.Controller columnFilterSupport;
    private final DefaultTableModel documentosModel = new DefaultTableModel(
            new Object[]{"Tipo", "Estado", "Número", "Documento", "Fecha"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable documentosTable = new JTable(documentosModel);
    private final List<NotificacionExpedienteDTO> expedientes = new ArrayList<NotificacionExpedienteDTO>();
    private final AtomicLong secuenciaBusqueda = new AtomicLong(0L);
    private volatile SwingWorker<?, ?> busquedaActiva;

    private final MetricCardV2 cardPendientes = new MetricCardV2("Pendientes", "0", "Por notificar", AppV2Theme.INFO);
    private final MetricCardV2 cardRevision = new MetricCardV2("En revisión", "0", "Cargo pendiente", AppV2Theme.WARNING);
    private final MetricCardV2 cardNotificados = new MetricCardV2("Notificados", "0", "Confirmados", AppV2Theme.SUCCESS);
    private final MetricCardV2 cardFallidos = new MetricCardV2("Fallidos", "0", "Intentos agotados", AppV2Theme.ERROR);
    private final MetricCardV2 cardPublicacion = new MetricCardV2("Requieren publicación", "0", "Publicación prevista", AppV2Theme.PRIMARY);
    private final MetricCardV2 cardVencidos = new MetricCardV2("Por vencer", "0", "Vencidos o críticos", AppV2Theme.WARNING);
    private FiltroKpi kpiActivo = FiltroKpi.TODOS;
    private final List<NotificacionExpedienteDTO> expedientesVisibles = new ArrayList<NotificacionExpedienteDTO>();
    private AppV2OperationalSplitPanel splitBandejasNotif;
    private JPanel panelLateralNotifHost;
    private JPanel panelLateralAsigNotif;
    private JPanel panelLateralValidacionNotif;
    private JPanel panelLateralNotifBandeja;
    private JPanel panelLateralPublicacionNotif;
    private AppV2SideActionPanel panelNotificacion;
    private AppV2SideActionPanel panelCierre;
    private AppV2SideActionPanel panelAsignacionOperativaNotif;
    private AppV2SideActionPanel panelValidarOperativo;
    private JTabbedPane tabsBandejasTop;

    private static final int COL_ASIG_EXPANDIR = 0;
    private static final int COL_ASIG_SELECCION = 1;
    private static final int COL_ASIG_EXPEDIENTE = 2;
    private static final int COL_ASIG_ID = 11;
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

    private final com.sdrerc.application.sdrercapp.DocumentoAnalisisService documentoAnalisisService =
            new com.sdrerc.application.sdrercapp.DocumentoAnalisisService();
    private final com.sdrerc.application.sdrercapp.UsuarioAsignacionService usuarioAsignacionServiceNotif =
            new com.sdrerc.application.sdrercapp.UsuarioAsignacionService();
    private final com.sdrerc.application.sdrercapp.ExpedienteRelacionadoService relacionadoServiceNotif =
            new com.sdrerc.application.sdrercapp.ExpedienteRelacionadoService();
    private final com.sdrerc.application.sdrercapp.AsignacionExpedienteService asignacionExpedienteServiceNotif =
            new com.sdrerc.application.sdrercapp.AsignacionExpedienteService();
    private final List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> documentosAsignacionNotif =
            new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>();
    private final List<AsignacionNotifTableRow> filasAsignacionNotif = new ArrayList<AsignacionNotifTableRow>();
    private final java.util.Map<Long, List<com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO>> asociadosCacheAsigNotif =
            new java.util.HashMap<Long, List<com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO>>();
    private final java.util.Set<Long> principalesExpandidosAsigNotif = new java.util.HashSet<Long>();
    private final java.util.Set<Long> principalesCargandoAsigNotif = new java.util.HashSet<Long>();
    private Long idExpedienteExpansionActivaAsigNotif;
    private boolean modoReasignacionAsigNotif = false;
    private com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO documentoAsigNotifFoco;
    private Long idExpedienteFirmaAsigNotifCargado;
    private final AsignacionNotifTableModel asignacionNotifModel = new AsignacionNotifTableModel();
    private final JTable tablaAsignacionNotif = new AppV2Table(asignacionNotifModel);

    private static final int PANEL_ASIG_NOTIF_ANCHO_MINIMO = 380;
    private static final int PANEL_ASIG_NOTIF_ANCHO_NORMAL = 430;
    private static final int PANEL_ASIG_NOTIF_TAB_OVERHANG = 46;
    private static final int PANEL_ASIG_NOTIF_TAB_TOP = 18;
    private static final int PANEL_ASIG_NOTIF_TAB_HEIGHT = 140;
    private static final String TAB_ASIG_NOTIF_DATOS = "DATOS";
    private static final String TAB_ASIG_NOTIF_ASIGNACION = "ASIGNACION";
    private final AppV2StackedSideTab tabAsigNotifDatos =
            crearTabAsigNotif("Datos", new Color(230, 241, 245), new Color(57, 125, 199));
    private final AppV2StackedSideTab tabAsigNotifAsignacion =
            crearTabAsigNotif("Asignación", new Color(219, 240, 237), new Color(10, 118, 145));
    private CardLayout panelAsigNotifCardsLayout;
    private JPanel panelAsigNotifCards;
    private String tabAsigNotifActiva = TAB_ASIG_NOTIF_DATOS;
    private boolean panelAsigNotifCerradoPorUsuario;
    private final DatosExpedienteNotifPanel datosAsigNotif = new DatosExpedienteNotifPanel();
    private final JCheckBox chkHabilitarReasignacionNotif = new JCheckBox("Habilitar reasignación");
    private final DocumentoFirmaNotificacionTreeGridPanelV2 documentosFirmaTreePanel = new DocumentoFirmaNotificacionTreeGridPanelV2();
    private final AppV2TablePanel tablaAsignacionNotifPanel = new AppV2TablePanel(
            tablaAsignacionNotif, "Sin documentos para asignar", "No hay documentos pendientes de asignación.");
    private final JLabel lblEstadoAsignacionNotif = new JLabel("Seleccione documentos y presione Generar asignación.");
    private final JComboBox<EquipoNotifItem> cmbEquipoNotif = new JComboBox<EquipoNotifItem>();
    private final JComboBox<UsuarioNotifItem> cmbUsuarioNotif = new JComboBox<UsuarioNotifItem>();
    private final JButton btnGenerarAsignacionNotif = new JButton("Generar asignación");
    private final JButton btnCancelarAsignacionNotif = new JButton("Cancelar");
    private boolean cargandoCombosAsignacionNotif;
    private static final DateTimeFormatter DATE_HORA_FORMAT_ASIG_NOTIF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DefaultTableModel asignacionMultipleModelNotif = new DefaultTableModel(
            new Object[]{"Nro. Expediente", "N° expediente SGD", "Hoja de envío nueva", "Hoja de envío actual", "Usuario actual"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2;
        }
    };
    private final JTable asignacionMultipleTableNotif = new AppV2Table(asignacionMultipleModelNotif);
    private JScrollPane asignacionMultipleScrollNotif;
    private final List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> documentosAsignacionMultipleNotif =
            new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>();
    private final java.util.Map<Long, String> hojasEnvioAsignacionMultipleNotif = new java.util.HashMap<Long, String>();
    private AppV2SideSectionPanel sectionAsignacionMultipleNotif;
    private AppV2StepCardPanel cardEmisionAsigNotif;
    private AppV2StepCardPanel cardAsignacionAsigNotif;
    private final JComboBox<String> cmbResultadoEmisionNotif =
            new JComboBox<String>(new String[]{"Aprobado", "Observado"});
    private final javax.swing.JTextArea txtComentarioEmisionNotif = new javax.swing.JTextArea(3, 20);
    private final JButton btnRegistrarSupervisionEmisionNotif = new JButton("Registrar Supervisión");
    private JPanel panelComentarioEmisionNotif;
    private JPanel panelAccionesAsigNotif;
    private final DefaultTableModel historialAsignacionModelNotif = new DefaultTableModel(
            new Object[]{"Tipo", "Usuario", "Equipo", "Hoja de envío", "Fecha", "Asignado por", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tablaHistorialAsignacionNotif = new AppV2Table(historialAsignacionModelNotif);
    private final AppV2TablePanel panelHistorialAsignacionNotif = new AppV2TablePanel(
            tablaHistorialAsignacionNotif, "Sin historial", "No hay asignaciones registradas para este documento.");
    private long secuenciaHistorialAsigNotif;
    private Long idDocumentoHistorialAsigNotifActual;

    private enum FiltroKpiAsigNotif {
        TODOS,
        PENDIENTES_ASIGNACION,
        ASIGNADOS,
        LISTOS_FIRMA,
        INTERMEDIOS
    }

    private FiltroKpiAsigNotif kpiActivoAsigNotif = FiltroKpiAsigNotif.TODOS;
    private final MetricCardV2 cardAsigNotifPendientes =
            new MetricCardV2("Pendientes de asignación", "0", "Sin responsable asignado", AppV2Theme.WARNING);
    private final MetricCardV2 cardAsigNotifAsignados =
            new MetricCardV2("Asignados", "0", "Con validador o abogado de notificación", AppV2Theme.INFO);
    private final MetricCardV2 cardAsigNotifListosFirma =
            new MetricCardV2("Listos para firma", "0", "FINAL validados", AppV2Theme.SUCCESS);
    private final MetricCardV2 cardAsigNotifIntermedios =
            new MetricCardV2("Intermedios", "0", "Se asignan a Notificación", AppV2Theme.PRIMARY);

    private final AppV2SearchField txtBusquedaAsigNotif =
            new AppV2SearchField("Buscar expediente, trámite/SGD, titular o documento", 28);
    private final PremiumDateFieldV2 fechaEmisionDesdeAsigNotif = new PremiumDateFieldV2();
    private final PremiumDateFieldV2 fechaEmisionHastaAsigNotif = new PremiumDateFieldV2();
    private final JComboBox<SimpleItem> cmbEstadoAsigNotif = new JComboBox<SimpleItem>();
    private final JSpinner spnLimiteAsigNotif = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscarAsigNotif = new JButton("Buscar");
    private final JButton btnLimpiarAsigNotif = new JButton("Limpiar");
    private final JButton btnRefrescarAsigNotif = new JButton("Refrescar");

    private final List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> documentosValidacion =
            new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>();
    private final DefaultTableModel validacionModel = new DefaultTableModel(
            new Object[]{
                "N° expediente", "N° expediente SGD", "Clas. Documentos", "Tipo documento",
                "N° Documento", "Fecha Emisión", "Titular", "Estado", "Estado doc."
            },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tablaValidacion = new AppV2Table(validacionModel);
    private final AppV2TablePanel tablaValidacionPanel = new AppV2TablePanel(
            tablaValidacion, "Sin documentos para validar", "No hay documentos pendientes de validación.");
    private final JLabel lblEstadoValidacion = new JLabel("Haga doble clic en un documento para abrir el Panel de Validación.");

    private enum FiltroKpiValidacion {
        TODOS,
        PENDIENTES,
        POR_VENCER,
        VENCIDOS
    }

    private FiltroKpiValidacion kpiActivoValidacion = FiltroKpiValidacion.TODOS;
    private final MetricCardV2 cardValidacionPendientes =
            new MetricCardV2("Pendientes de validar", "0", "Documentos en despacho asignados", AppV2Theme.INFO);
    private final MetricCardV2 cardValidacionPorVencer =
            new MetricCardV2("Por vencer", "0", "0 a 5 días hábiles", AppV2Theme.WARNING);
    private final MetricCardV2 cardValidacionVencidos =
            new MetricCardV2("Vencidos", "0", "Plazo excedido", AppV2Theme.ERROR);

    private final AppV2SearchField txtBusquedaValidacion =
            new AppV2SearchField("Buscar expediente, trámite/SGD, titular o documento", 28);
    private final PremiumDateFieldV2 fechaEmisionDesdeValidacion = new PremiumDateFieldV2();
    private final PremiumDateFieldV2 fechaEmisionHastaValidacion = new PremiumDateFieldV2();
    private final JComboBox<SimpleItem> cmbEstadoValidacion = new JComboBox<SimpleItem>();
    private final JSpinner spnLimiteValidacion = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscarValidacion = new JButton("Buscar");
    private final JButton btnLimpiarValidacion = new JButton("Limpiar");
    private final JButton btnRefrescarValidacion = new JButton("Refrescar");
    private final com.sdrerc.ui.views.ejecucion.DocumentoEjecucionTreeGridPanelV2 documentosValidacionTreePanel =
            new com.sdrerc.ui.views.ejecucion.DocumentoEjecucionTreeGridPanelV2();
    private final JButton btnRegistrarValidacion = new JButton("Registrar Validación");
    private final JButton btnCancelarValidacion = new JButton("Cancelar");
    private final JLabel lblPanelValidacionTitulo = new JLabel("Panel de Validación");
    private Long idDocumentoValidacionSeleccionado;
    private Long idExpedienteValidacionSeleccionado;
    private static final int PANEL_VALIDACION_TAB_OVERHANG = 46;
    private static final int PANEL_VALIDACION_TAB_TOP = 18;
    private static final int PANEL_VALIDACION_TAB_HEIGHT = 140;
    private static final int PANEL_VALIDACION_ANCHO_MINIMO = 380;
    private static final int PANEL_VALIDACION_ANCHO_NORMAL = 520;
    private static final String TAB_VALIDACION_DATOS = "DATOS";
    private static final String TAB_VALIDACION_VALIDAR = "VALIDAR";
    private final AppV2StackedSideTab tabValidacionDatos =
            crearTabAsigNotif("Datos", new Color(230, 241, 245), new Color(57, 125, 199));
    private final AppV2StackedSideTab tabValidacionValidar =
            crearTabAsigNotif("Validar", new Color(224, 243, 240), AppV2Theme.PRIMARY);
    private CardLayout panelValidacionCardsLayout;
    private JPanel panelValidacionCards;
    private String tabValidacionActiva = TAB_VALIDACION_DATOS;
    private boolean panelValidacionCerradoPorUsuario;
    private final DatosExpedienteNotifPanel datosValidacionNotif = new DatosExpedienteNotifPanel();
    private final JComboBox<SimpleItem> cmbResultadoValidacion = new JComboBox<SimpleItem>();
    private final JTextArea txtComentarioValidacion = new JTextArea(3, 20);

    private final List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> documentosNotifBandeja =
            new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>();
    private final List<NotifFilaTabla> filasNotifBandeja = new ArrayList<NotifFilaTabla>();
    private final java.util.Map<Long, List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>> intentosNotifCache =
            new java.util.HashMap<Long, List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>>();
    private final java.util.Set<Long> documentosNotifExpandidos = new java.util.HashSet<Long>();
    private final java.util.Set<Long> documentosNotifSeleccionados = new java.util.HashSet<Long>();
    private final java.util.Map<Long, List<IntentoBorrador>> borradoresNotifPorDocumento =
            new java.util.HashMap<Long, List<IntentoBorrador>>();
    private final AtomicLong secuenciaBorradorIntento = new AtomicLong(-1L);
    private static final int COL_NOTIF_SEL = 0;
    private static final int COL_NOTIF_EXPAND = 1;
    private static final int COL_NOTIF_MODALIDAD = 3;
    private static final int COL_NOTIF_FECHA_ENVIO = 4;
    private static final int COL_NOTIF_ESTADO = 5;
    private static final int COL_NOTIF_CODIGO = 6;
    private static final int COL_NOTIF_FECHA_RECEPCION = 7;
    private static final int COL_NOTIF_ESTADO_NOTIF = 8;
    private static final int COL_NOTIF_ACCION = 10;
    private final DefaultTableModel notifBandejaModel = new DefaultTableModel(
            new Object[]{"", "", "N° expediente", "Clas. Documentos", "Tipo documento", "N° Documento",
                "Fecha Emisión", "Titular", "Estado Final", "Estado doc.", ""},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            if (row < 0 || row >= filasNotifBandeja.size()) {
                return false;
            }
            NotifFilaTabla fila = filasNotifBandeja.get(row);
            if (fila.esPadre()) {
                return column == COL_NOTIF_SEL;
            }
            if (fila.esSubEncabezado()) {
                return false;
            }
            return column == COL_NOTIF_ACCION
                    || column == COL_NOTIF_MODALIDAD
                    || column == COL_NOTIF_CODIGO
                    || column == COL_NOTIF_FECHA_ENVIO
                    || column == COL_NOTIF_FECHA_RECEPCION
                    || column == COL_NOTIF_ESTADO
                    || column == COL_NOTIF_ESTADO_NOTIF;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == COL_NOTIF_SEL ? Boolean.class : Object.class;
        }
    };
    private final JTable tablaNotifBandeja = new AppV2Table(notifBandejaModel);
    private final AppV2TablePanel tablaNotifBandejaPanel = new AppV2TablePanel(
            tablaNotifBandeja, "Sin documentos para notificar", "No hay documentos pendientes de notificación.");
    private final JLabel lblEstadoNotifBandeja = new JLabel(
            "Marque uno o varios documentos y presione \"Agregar intento\" para registrar el envío al ciudadano.");
    private final JButton btnAgregarIntento = new JButton("+ Agregar intento");
    private Long idDocumentoNotifSeleccionado;

    // ===================== Bandeja Publicación (4ta pestaña de Notificación) =====================
    // Mismo diseño que la Bandeja Notificación (KPIs + buscador compacto + grilla arbol padre/hijo
    // con icono "+ Agregar X"), pero acotada a documentos POR_PUBLICAR (intento 1 y 2 ya FALLIDA/no
    // ubicado). Los intentos 1 y 2 se muestran de solo lectura; unicamente la fila de "Publicación"
    // (el 3er intento, tipo_notificacion=PUBLICACION, ya sembrado por el script 46) es editable.
    private static final int COL_PUB_SEL = 0;
    private static final int COL_PUB_EXPAND = 1;
    private static final int COL_PUB_FECHA = 4;
    private static final int COL_PUB_ESTADO = 5;
    private static final int COL_PUB_REFERENCIA = 6;
    private static final int COL_PUB_ACCION = 10;
    private final MetricCardV2 cardPubTotal = new MetricCardV2("Documentos", "0", "Con intentos agotados", AppV2Theme.INFO);
    private final MetricCardV2 cardPubPendientes =
            new MetricCardV2("Sin publicación registrar", "0", "Aún no tienen 3er intento", AppV2Theme.WARNING);
    private final MetricCardV2 cardPubRegistradas =
            new MetricCardV2("Publicación registrada", "0", "Con 3er intento en curso", AppV2Theme.SUCCESS);
    private final MetricCardV2 cardPubVencidos = new MetricCardV2("Vencidos", "0", "Plazo excedido", AppV2Theme.ERROR);

    private enum FiltroKpiPublicacion {
        TODOS,
        SIN_PUBLICACION,
        CON_PUBLICACION,
        VENCIDOS
    }

    private FiltroKpiPublicacion kpiActivoPublicacion = FiltroKpiPublicacion.TODOS;
    private final AppV2SearchField txtBusquedaPublicacion =
            new AppV2SearchField("Buscar expediente, trámite/SGD, titular o documento", 28);
    private final PremiumDateFieldV2 fechaEmisionDesdePublicacion = new PremiumDateFieldV2();
    private final PremiumDateFieldV2 fechaEmisionHastaPublicacion = new PremiumDateFieldV2();
    private final JComboBox<SimpleItem> cmbEstadoPublicacion = new JComboBox<SimpleItem>();
    private final JSpinner spnLimitePublicacion = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscarPublicacion = new JButton("Buscar");
    private final JButton btnLimpiarPublicacion = new JButton("Limpiar");
    private final JButton btnRefrescarPublicacion = new JButton("Refrescar");

    private final List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> documentosPublicacionBandeja =
            new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>();
    private final List<PublicacionFilaTabla> filasPublicacionBandeja = new ArrayList<PublicacionFilaTabla>();
    private final java.util.Map<Long, List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>> intentosPublicacionCache =
            new java.util.HashMap<Long, List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>>();
    private final java.util.Set<Long> documentosPublicacionExpandidos = new java.util.HashSet<Long>();
    private final java.util.Set<Long> documentosPublicacionSeleccionados = new java.util.HashSet<Long>();
    private final java.util.Map<Long, PublicacionBorrador> borradoresPublicacionPorDocumento =
            new java.util.HashMap<Long, PublicacionBorrador>();
    private final AtomicLong secuenciaBorradorPublicacion = new AtomicLong(-1L);
    private final DefaultTableModel publicacionBandejaModel = new DefaultTableModel(
            new Object[]{"", "", "N° expediente", "Clas. Documentos", "Tipo documento", "N° Documento",
                "Fecha Emisión", "Titular", "Estado Final", "Estado doc.", ""},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            if (row < 0 || row >= filasPublicacionBandeja.size()) {
                return false;
            }
            PublicacionFilaTabla fila = filasPublicacionBandeja.get(row);
            if (fila.esPadre()) {
                return column == COL_PUB_SEL;
            }
            if (fila.esSubEncabezado() || fila.esIntentoSoloLectura()) {
                return false;
            }
            return column == COL_PUB_ACCION || column == COL_PUB_FECHA
                    || column == COL_PUB_ESTADO || column == COL_PUB_REFERENCIA;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == COL_PUB_SEL ? Boolean.class : Object.class;
        }
    };
    private final JTable tablaPublicacionBandeja = new AppV2Table(publicacionBandejaModel);
    private final AppV2TablePanel tablaPublicacionBandejaPanel = new AppV2TablePanel(
            tablaPublicacionBandeja, "Sin documentos para publicar", "No hay documentos pendientes de publicación.");
    private final JLabel lblEstadoPublicacionBandeja = new JLabel(
            "Marque uno o varios documentos y presione \"Agregar publicación\" para registrar la publicación.");
    private final JButton btnAgregarPublicacion = new JButton("+ Agregar publicación");
    private Long idDocumentoPublicacionSeleccionado;
    private com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO documentoPublicacionFoco;

    private static final int PANEL_PUBLICACION_TAB_OVERHANG = 46;
    private static final int PANEL_PUBLICACION_TAB_TOP = 18;
    private static final int PANEL_PUBLICACION_TAB_HEIGHT = 140;
    private static final int PANEL_PUBLICACION_ANCHO_MINIMO = 380;
    private static final int PANEL_PUBLICACION_ANCHO_NORMAL = 430;
    private static final String TAB_PUBLICACION_DATOS = "DATOS";
    private static final String TAB_PUBLICACION_PUBLICACION = "PUBLICACION";
    private final AppV2StackedSideTab tabPublicacionDatos = new AppV2StackedSideTab(
            "Datos", PANEL_PUBLICACION_TAB_OVERHANG - 6, PANEL_PUBLICACION_TAB_HEIGHT,
            new Color(230, 241, 245), new Color(57, 125, 199), new Color(57, 125, 199).darker());
    private final AppV2StackedSideTab tabPublicacionPublicacion = new AppV2StackedSideTab(
            "Publicación", PANEL_PUBLICACION_TAB_OVERHANG - 6, PANEL_PUBLICACION_TAB_HEIGHT,
            new Color(224, 243, 240), new Color(10, 118, 145), new Color(10, 118, 145).darker());
    private CardLayout panelPublicacionCardsLayout;
    private JPanel panelPublicacionCards;
    private String tabPublicacionActiva = TAB_PUBLICACION_DATOS;
    private boolean panelPublicacionCerradoPorUsuario;
    private final DatosExpedienteNotifPanel datosPublicacionNotif = new DatosExpedienteNotifPanel();
    private final JLabel lblPubInfoTipoDocumento = new JLabel("-");
    private final JLabel lblPubInfoNumeroDocumento = new JLabel("-");
    private final JLabel lblPubInfoFechaEmision = new JLabel("-");
    private final JLabel lblPubInfoEstadoFinal = new JLabel("-");
    private final JLabel lblPubInfoIntento1 = new JLabel("-");
    private final JLabel lblPubInfoIntento2 = new JLabel("-");
    private final JLabel lblPubInfoPublicacion = new JLabel("-");

    private static final class PublicacionBorrador {
        private final long tempId;
        private final Long idExpediente;
        private final Long idDocumento;
        private final int numeroIntento;

        private PublicacionBorrador(long tempId, Long idExpediente, Long idDocumento, int numeroIntento) {
            this.tempId = tempId;
            this.idExpediente = idExpediente;
            this.idDocumento = idDocumento;
            this.numeroIntento = numeroIntento;
        }
    }

    private static class PublicacionFilaTabla {
        private final boolean padre;
        private final boolean subEncabezado;
        private final Long idDocumento;
        private final com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento;
        private final PublicacionBorrador borrador;

        private PublicacionFilaTabla(
                boolean padre,
                boolean subEncabezado,
                Long idDocumento,
                com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento,
                PublicacionBorrador borrador) {
            this.padre = padre;
            this.subEncabezado = subEncabezado;
            this.idDocumento = idDocumento;
            this.intento = intento;
            this.borrador = borrador;
        }

        private static PublicacionFilaTabla padre(com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item) {
            return new PublicacionFilaTabla(true, false, item.getIdDocumentoAnalizado(), null, null);
        }

        private static PublicacionFilaTabla subEncabezado(Long idDocumento) {
            return new PublicacionFilaTabla(false, true, idDocumento, null, null);
        }

        private static PublicacionFilaTabla hijo(Long idDocumento, com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento) {
            return new PublicacionFilaTabla(false, false, idDocumento, intento, null);
        }

        private static PublicacionFilaTabla hijoBorrador(Long idDocumento, PublicacionBorrador borrador) {
            return new PublicacionFilaTabla(false, false, idDocumento, null, borrador);
        }

        private boolean esPadre() {
            return padre;
        }

        private boolean esSubEncabezado() {
            return subEncabezado;
        }

        private boolean esBorrador() {
            return borrador != null;
        }

        /** Intento 1/2 ya persistido (no es la fila de Publicación ni un borrador): solo lectura. */
        private boolean esIntentoSoloLectura() {
            return intento != null && !"PUBLICACION".equalsIgnoreCase(intento.getTipoNotificacionCodigo());
        }
    }
    // =================== fin de campos propios de Bandeja Publicación ===================

    private static final class IntentoBorrador {
        private final long tempId;
        private final Long idExpediente;
        private final Long idDocumento;
        private final int numeroIntento;
        private String modalidadCodigo;

        private IntentoBorrador(long tempId, Long idExpediente, Long idDocumento, int numeroIntento, String modalidadCodigo) {
            this.tempId = tempId;
            this.idExpediente = idExpediente;
            this.idDocumento = idDocumento;
            this.numeroIntento = numeroIntento;
            this.modalidadCodigo = modalidadCodigo;
        }
    }

    private enum ModoBandejaNotificacion {
        ASIGNACION,
        VALIDACION,
        NOTIFICACION,
        PUBLICACION
    }
    private ModoBandejaNotificacion modoBandejaNotificacion = ModoBandejaNotificacion.NOTIFICACION;
    private boolean construccionCompleta;
    private boolean panelNotificacionCerradoPorUsuario;

    public JPanelNotificacionV2() {
        this(new NotificacionExpedienteService(), new DocumentoEjecucionService());
    }

    public JPanelNotificacionV2(
            NotificacionExpedienteService notificacionService,
            DocumentoEjecucionService documentoService) {
        this.notificacionService = notificacionService;
        this.documentoService = documentoService;
        setLayout(new BorderLayout(8, 8));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        add(crearCentro(), BorderLayout.CENTER);
        configurarTabla();
        configurarDocumentosTabla();
        configurarEventos();
        configurarKpisInteractivos();
        cargarFiltrosBase();
        cargarCatalogos();
        cargarCatalogosDocumentosValidacion();
        inicializarFechas();
        inicializarFechasFiltro();
        actualizarSeleccion();
        cargarBandejaAsignacionNotificacion();
        cargarEquiposAsignacionNotif();
        cargarEstadosDocumentoFirmaAsigNotif();
        cargarResultadosValidacion();
        cargarBandejaValidacion();
        cargarBandejaNotifV2();
        cargarBandejaPublicacionNotif();
        construccionCompleta = true;
    }

    private JPanel crearHeader() {
        JPanel metricas = new AppV2ResponsiveGridPanel(190, 6, 12, 0);
        metricas.add(cardPendientes);
        metricas.add(cardRevision);
        metricas.add(cardNotificados);
        metricas.add(cardFallidos);
        metricas.add(cardPublicacion);
        metricas.add(cardVencidos);
        return metricas;
    }

    private JPanel crearCentro() {
        // Split UNICO a nivel de todo el modulo: el JTabbedPane con las 3 bandejas
        // (Asignacion/Validacion/Notificacion) es el lado izquierdo, y un host compartido
        // es el lado derecho, para que el panel lateral arranque a la misma altura que el
        // encabezado de las pestanas superiores (igual patron que JPanelAsignacionV2), en vez
        // de quedar anidado dentro del contenido de cada pestana (que lo empujaba mas abajo).
        JPanel contenidoAsigNotif = crearBandejaAsignacionNotificacion();
        JPanel contenidoValidacion = crearBandejaValidacion();
        JPanel contenidoPublicacion = crearBandejaPublicacionNotif();

        JPanel contenidoPrincipal = new JPanel(new BorderLayout(4, 4));
        contenidoPrincipal.setOpaque(false);
        contenidoPrincipal.add(crearHeader(), BorderLayout.NORTH);

        JPanel contenidoOperativo = new JPanel(new BorderLayout(4, 4));
        contenidoOperativo.setOpaque(false);
        contenidoOperativo.add(crearBuscador(), BorderLayout.NORTH);
        contenidoOperativo.add(crearBandejaNotifV2(), BorderLayout.CENTER);
        contenidoPrincipal.add(contenidoOperativo, BorderLayout.CENTER);

        panelNotificacion = crearPanelNotificacion();
        panelCierre = crearPanelCierre();
        panelLateralNotifBandeja = crearPanelNotificacionConTab(panelNotificacion, panelCierre);

        tabsBandejasTop = new JTabbedPane();
        tabsBandejasTop.setOpaque(false);
        tabsBandejasTop.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        tabsBandejasTop.setBackground(AppV2Theme.BACKGROUND);
        tabsBandejasTop.setBorder(BorderFactory.createEmptyBorder());
        tabsBandejasTop.addTab("Bandeja Asignación", contenidoAsigNotif);
        tabsBandejasTop.addTab("Bandeja Validación", contenidoValidacion);
        tabsBandejasTop.addTab("Bandeja Notificación", contenidoPrincipal);
        // "Publicación" (4ta pestaña, sin permiso propio todavia): a diferencia de las otras 3
        // bandejas, no hay un codigo de permiso sembrado en BD para esta pestaña nueva
        // (60_catalogo_permisos_bandejas.sql no la contempla); gatearla con un codigo nuevo sin
        // sembrar dejaria la pestaña inaccesible incluso para ADMIN_SISTEMA hasta ejecutar un
        // script nuevo (fuera de alcance sin autorizacion explicita), asi que queda habilitada
        // para todos por ahora. Ver AGENTS.md para el detalle y el script pendiente sugerido.
        tabsBandejasTop.addTab("Publicación", contenidoPublicacion);
        tabsBandejasTop.addChangeListener(e -> actualizarTabBandejaNotificacion());
        aplicarPermisoBandeja(
                TAB_BANDEJA_NOTIF_ASIGNACION, PERMISO_BANDEJA_NOTIFICACION_ASIGNACION,
                "No tiene permiso para ver Bandeja Asignación.");
        aplicarPermisoBandeja(
                TAB_BANDEJA_NOTIF_VALIDACION, PERMISO_BANDEJA_NOTIFICACION_VALIDACION,
                "No tiene permiso para ver Bandeja Validación.");
        aplicarPermisoBandeja(
                TAB_BANDEJA_NOTIF_NOTIFICACION, PERMISO_BANDEJA_NOTIFICACION_NOTIFICACION,
                "No tiene permiso para ver Bandeja Notificación.");

        panelLateralNotifHost = new JPanel(new BorderLayout());
        panelLateralNotifHost.setOpaque(false);
        splitBandejasNotif = new AppV2OperationalSplitPanel(
                tabsBandejasTop,
                panelLateralNotifHost,
                0,
                PANEL_NOTIFICACION_ANCHO_MINIMO + PANEL_NOTIFICACION_TAB_OVERHANG,
                PANEL_VALIDACION_ANCHO_NORMAL + PANEL_NOTIFICACION_TAB_OVERHANG);
        actualizarTabBandejaNotificacion();

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(splitBandejasNotif, BorderLayout.CENTER);
        return wrapper;
    }

    private void mostrarPanelLateralNotif(JPanel panelLateral) {
        if (panelLateralNotifHost == null || panelLateral == null) {
            return;
        }
        if (panelLateral.getParent() == panelLateralNotifHost) {
            return;
        }
        panelLateralNotifHost.removeAll();
        panelLateralNotifHost.add(panelLateral, BorderLayout.CENTER);
        panelLateralNotifHost.revalidate();
        panelLateralNotifHost.repaint();
    }

    private void aplicarPermisoBandeja(int indice, String codigoPermiso, String motivo) {
        if (tabsBandejasTop == null || indice < 0 || indice >= tabsBandejasTop.getTabCount()) {
            return;
        }
        if (!SessionContext.tienePermiso(codigoPermiso)) {
            tabsBandejasTop.setEnabledAt(indice, false);
            tabsBandejasTop.setToolTipTextAt(indice, motivo);
        }
    }

    private JPanel crearBandejaAsignacionNotificacion() {
        tablaAsignacionNotif.setRowHeight(32);
        tablaAsignacionNotif.setAutoCreateRowSorter(false);
        tablaAsignacionNotif.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaAsignacionNotif.getTableHeader().setReorderingAllowed(false);
        tablaAsignacionNotif.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tablaAsignacionNotif.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        tablaAsignacionNotif.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        tablaAsignacionNotif.setGridColor(AppV2Theme.BORDER);
        tablaAsignacionNotif.setShowVerticalLines(false);
        tablaAsignacionNotif.setDefaultRenderer(Object.class, new AsignacionNotifRenderer());
        AppV2TableColumnSizer.applyFriendlyDefaults(tablaAsignacionNotif);
        tablaAsignacionNotif.getColumnModel().getColumn(COL_ASIG_EXPANDIR).setCellRenderer(new AsignacionNotifExpandirRenderer());
        tablaAsignacionNotif.getColumnModel().getColumn(COL_ASIG_EXPANDIR).setMaxWidth(28);
        tablaAsignacionNotif.getColumnModel().getColumn(COL_ASIG_EXPANDIR).setMinWidth(24);
        tablaAsignacionNotif.getColumnModel().getColumn(COL_ASIG_SELECCION).setMaxWidth(40);
        tablaAsignacionNotif.getColumnModel().getColumn(COL_ASIG_SELECCION).setMinWidth(36);
        AppV2TableColumnSizer.applyWidths(tablaAsignacionNotif,
                28, 40, 150, 130, 110, 150, 130, 110, 200, 130);
        try {
            tablaAsignacionNotif.removeColumn(tablaAsignacionNotif.getColumnModel().getColumn(COL_ASIG_ID));
        } catch (Exception ignored) {
            // columna oculta ya removida
        }
        AppV2ColumnFilterSupport.install(
                "notificacionAsignacion",
                tablaAsignacionNotif,
                tablaAsignacionNotifPanel.getScrollPane(),
                tablaAsignacionNotifPanel,
                () -> contraerTodosExceptoAsigNotif(null),
                COL_ASIG_EXPANDIR,
                COL_ASIG_SELECCION);
        tablaAsignacionNotif.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = tablaAsignacionNotif.rowAtPoint(e.getPoint());
                int viewColumn = tablaAsignacionNotif.columnAtPoint(e.getPoint());
                if (viewRow < 0 || viewColumn < 0) {
                    return;
                }
                int modelColumn = tablaAsignacionNotif.convertColumnIndexToModel(viewColumn);
                int modelRow = tablaAsignacionNotif.convertRowIndexToModel(viewRow);
                if (modelColumn == COL_ASIG_EXPANDIR) {
                    alternarExpansionFilaAsigNotif(modelRow);
                    return;
                }
                if (e.getClickCount() == 2) {
                    panelAsigNotifCerradoPorUsuario = false;
                    actualizarFocoAsignacionNotif();
                    if (documentoAsigNotifFoco != null) {
                        mostrarPanelLateralNotif(panelLateralAsigNotif);
                        splitBandejasNotif.setSideVisible(true);
                        seleccionarTabAsigNotif(TAB_ASIG_NOTIF_DATOS);
                    }
                }
            }
        });
        tablaAsignacionNotif.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            actualizarFocoAsignacionNotif();
            actualizarVisibilidadPanelAsigNotif();
        });

        configurarFiltrosAsigNotif();

        JPanel superior = new JPanel(new BorderLayout(4, 4));
        superior.setOpaque(false);
        superior.add(crearHeaderAsigNotif(), BorderLayout.NORTH);
        superior.add(crearBuscadorAsigNotif(), BorderLayout.CENTER);

        JPanel izquierda = new JPanel(new BorderLayout(6, 6));
        izquierda.setOpaque(false);
        izquierda.add(superior, BorderLayout.NORTH);
        AppV2TableSectionPanel section = new AppV2TableSectionPanel(tablaAsignacionNotifPanel);
        section.setStatus(lblEstadoAsignacionNotif);
        izquierda.add(section, BorderLayout.CENTER);

        panelLateralAsigNotif = crearPanelDetalleAsignacionNotif();
        return izquierda;
    }

    private void actualizarVisibilidadPanelAsigNotif() {
        if (modoBandejaNotificacion != ModoBandejaNotificacion.ASIGNACION
                || splitBandejasNotif == null || !splitBandejasNotif.isSideVisible()) {
            return;
        }
        splitBandejasNotif.setSideVisible(documentoAsigNotifFoco != null && !panelAsigNotifCerradoPorUsuario);
    }

    private JPanel crearHeaderAsigNotif() {
        JPanel metricas = new AppV2ResponsiveGridPanel(190, 4, 12, 10);
        metricas.add(cardAsigNotifPendientes);
        metricas.add(cardAsigNotifAsignados);
        metricas.add(cardAsigNotifListosFirma);
        metricas.add(cardAsigNotifIntermedios);
        return metricas;
    }

    private JPanel crearBuscadorAsigNotif() {
        JPanel acciones = AppV2ActionPanel.right();
        acciones.add(btnBuscarAsigNotif);
        acciones.add(btnLimpiarAsigNotif);
        acciones.add(btnRefrescarAsigNotif);
        return AppV2ExpedientePanelFactory.crearPanelBusquedaEstiloRegistro(
                "Búsqueda",
                txtBusquedaAsigNotif,
                acciones,
                fechaEmisionDesdeAsigNotif,
                fechaEmisionHastaAsigNotif,
                cmbEstadoAsigNotif,
                null,
                spnLimiteAsigNotif);
    }

    private void configurarFiltrosAsigNotif() {
        cmbEstadoAsigNotif.removeAllItems();
        cmbEstadoAsigNotif.addItem(new SimpleItem("", "Todos los estados"));
        cmbEstadoAsigNotif.addItem(new SimpleItem("EMITIDO", "Emitido"));
        cmbEstadoAsigNotif.addItem(new SimpleItem("EN_DESPACHO", "En despacho"));
        cmbEstadoAsigNotif.addItem(new SimpleItem("VALIDADO", "Validado"));
        AppV2Theme.estilizarBotonPrimario(btnBuscarAsigNotif);
        cardAsigNotifPendientes.setOnClick(() -> activarKpiAsigNotif(FiltroKpiAsigNotif.PENDIENTES_ASIGNACION));
        cardAsigNotifAsignados.setOnClick(() -> activarKpiAsigNotif(FiltroKpiAsigNotif.ASIGNADOS));
        cardAsigNotifListosFirma.setOnClick(() -> activarKpiAsigNotif(FiltroKpiAsigNotif.LISTOS_FIRMA));
        cardAsigNotifIntermedios.setOnClick(() -> activarKpiAsigNotif(FiltroKpiAsigNotif.INTERMEDIOS));
        btnBuscarAsigNotif.addActionListener(e -> aplicarFiltrosAsigNotif());
        txtBusquedaAsigNotif.addActionListener(e -> aplicarFiltrosAsigNotif());
        btnLimpiarAsigNotif.addActionListener(e -> limpiarFiltrosAsigNotif());
        btnRefrescarAsigNotif.addActionListener(e -> cargarBandejaAsignacionNotificacion());
        restaurarFechasAsigNotif();
    }

    private void restaurarFechasAsigNotif() {
        fechaEmisionDesdeAsigNotif.setDate(DateRangePickerSupport.defaultSearchFromDateCurrentMonth());
        fechaEmisionHastaAsigNotif.setDate(DateRangePickerSupport.defaultSearchToDate());
    }

    private void limpiarFiltrosAsigNotif() {
        txtBusquedaAsigNotif.setText("");
        restaurarFechasAsigNotif();
        cmbEstadoAsigNotif.setSelectedIndex(0);
        spnLimiteAsigNotif.setValue(200);
        kpiActivoAsigNotif = FiltroKpiAsigNotif.TODOS;
        marcarKpisAsigNotif();
        aplicarFiltrosAsigNotif();
    }

    private void activarKpiAsigNotif(FiltroKpiAsigNotif filtro) {
        kpiActivoAsigNotif = kpiActivoAsigNotif == filtro ? FiltroKpiAsigNotif.TODOS : filtro;
        marcarKpisAsigNotif();
        aplicarFiltrosAsigNotif();
    }

    private void marcarKpisAsigNotif() {
        cardAsigNotifPendientes.setSelected(kpiActivoAsigNotif == FiltroKpiAsigNotif.PENDIENTES_ASIGNACION);
        cardAsigNotifAsignados.setSelected(kpiActivoAsigNotif == FiltroKpiAsigNotif.ASIGNADOS);
        cardAsigNotifListosFirma.setSelected(kpiActivoAsigNotif == FiltroKpiAsigNotif.LISTOS_FIRMA);
        cardAsigNotifIntermedios.setSelected(kpiActivoAsigNotif == FiltroKpiAsigNotif.INTERMEDIOS);
    }

    private void actualizarMetricasAsigNotif() {
        int pendientes = 0;
        int asignados = 0;
        int listosFirma = 0;
        int intermedios = 0;
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : documentosAsignacionNotif) {
            boolean esFinal = "FINAL".equalsIgnoreCase(item.getClasificacion());
            boolean esIntermedio = "INTERMEDIO".equalsIgnoreCase(item.getClasificacion());
            if (!item.isAsignado()) {
                pendientes++;
            }
            if (item.isAsignado()) {
                asignados++;
            }
            if (esFinal && "VALIDADO".equalsIgnoreCase(item.getEstadoDocumentoCodigo())) {
                listosFirma++;
            }
            if (esIntermedio) {
                intermedios++;
            }
        }
        cardAsigNotifPendientes.setValue(String.valueOf(pendientes));
        cardAsigNotifAsignados.setValue(String.valueOf(asignados));
        cardAsigNotifListosFirma.setValue(String.valueOf(listosFirma));
        cardAsigNotifIntermedios.setValue(String.valueOf(intermedios));
    }

    private boolean coincideKpiAsigNotif(com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item) {
        boolean esFinal = "FINAL".equalsIgnoreCase(item.getClasificacion());
        boolean esIntermedio = "INTERMEDIO".equalsIgnoreCase(item.getClasificacion());
        switch (kpiActivoAsigNotif) {
            case PENDIENTES_ASIGNACION:
                return !item.isAsignado();
            case ASIGNADOS:
                return item.isAsignado();
            case LISTOS_FIRMA:
                return esFinal && "VALIDADO".equalsIgnoreCase(item.getEstadoDocumentoCodigo());
            case INTERMEDIOS:
                return esIntermedio;
            case TODOS:
            default:
                return true;
        }
    }

    private boolean coincideTextoAsigNotif(com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item, String texto) {
        return contieneTextoAsigNotif(item.getNumeroExpediente(), texto)
                || contieneTextoAsigNotif(item.getNumeroExpedienteSgd(), texto)
                || contieneTextoAsigNotif(item.getTitular(), texto)
                || contieneTextoAsigNotif(item.getNumeroDocumento(), texto);
    }

    private static boolean contieneTextoAsigNotif(String valor, String texto) {
        return valor != null && valor.toLowerCase().contains(texto);
    }

    private List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> filtrarDocumentosAsigNotif() {
        String texto = txtBusquedaAsigNotif.getText() == null ? "" : txtBusquedaAsigNotif.getText().trim().toLowerCase();
        LocalDate desde = fechaSeleccionadaAsigNotif(fechaEmisionDesdeAsigNotif);
        LocalDate hasta = fechaSeleccionadaAsigNotif(fechaEmisionHastaAsigNotif);
        SimpleItem estadoItem = (SimpleItem) cmbEstadoAsigNotif.getSelectedItem();
        String estadoCodigo = estadoItem == null ? "" : estadoItem.getCodigo();
        int limite = (Integer) spnLimiteAsigNotif.getValue();
        List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> resultado =
                new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>();
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : documentosAsignacionNotif) {
            if (!coincideKpiAsigNotif(item)) {
                continue;
            }
            if (!texto.isEmpty() && !coincideTextoAsigNotif(item, texto)) {
                continue;
            }
            if (item.getFechaDocumento() != null) {
                if (desde != null && item.getFechaDocumento().isBefore(desde)) {
                    continue;
                }
                if (hasta != null && item.getFechaDocumento().isAfter(hasta)) {
                    continue;
                }
            }
            if (!estadoCodigo.isEmpty() && !estadoCodigo.equalsIgnoreCase(item.getEstadoDocumentoCodigo())) {
                continue;
            }
            resultado.add(item);
            if (resultado.size() >= limite) {
                break;
            }
        }
        return resultado;
    }

    private static LocalDate fechaSeleccionadaAsigNotif(PremiumDateFieldV2 field) {
        if (field == null || field.getDate() == null) {
            return null;
        }
        return field.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void aplicarFiltrosAsigNotif() {
        documentoAsigNotifFoco = null;
        chkHabilitarReasignacionNotif.setSelected(false);
        modoReasignacionAsigNotif = false;
        hojasEnvioAsignacionMultipleNotif.clear();
        poblarGrillaAsignacionNotif(filtrarDocumentosAsigNotif());
    }

    private void poblarGrillaAsignacionNotif(List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> items) {
        filasAsignacionNotif.clear();
        asociadosCacheAsigNotif.clear();
        principalesExpandidosAsigNotif.clear();
        principalesCargandoAsigNotif.clear();
        idExpedienteExpansionActivaAsigNotif = null;
        asignacionNotifModel.setRowCount(0);
        tablaAsignacionNotif.clearSelection();
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : items) {
            agregarFilaPrincipalAsigNotif(item);
        }
        tablaAsignacionNotifPanel.setEmpty(items.isEmpty());
        lblEstadoAsignacionNotif.setText(items.isEmpty()
                ? "No hay documentos pendientes de asignación con los filtros aplicados."
                : items.size() + " documento(s) encontrados de " + documentosAsignacionNotif.size() + " en total.");
        actualizarPanelDatosAsigNotif();
        actualizarPanelFirmaAsigNotif();
        actualizarVisibilidadPanelAsigNotif();
        actualizarPanelAsignacionSeleccionNotif();
        actualizarModoPanelAsigNotif();
    }

    private void actualizarFocoAsignacionNotif() {
        int viewRow = tablaAsignacionNotif.getSelectedRow();
        if (viewRow < 0) {
            documentoAsigNotifFoco = null;
        } else {
            int modelRow = tablaAsignacionNotif.convertRowIndexToModel(viewRow);
            AsignacionNotifTableRow fila = filaAsignacionNotif(modelRow);
            documentoAsigNotifFoco = fila != null && fila.esPrincipal() ? fila.principal : null;
        }
        actualizarSubtituloPanelesAsigNotif();
        actualizarPanelDatosAsigNotif();
        actualizarPanelFirmaAsigNotif();
        actualizarPanelAsignacionSeleccionNotif();
        actualizarModoPanelAsigNotif();
    }

    /**
     * Calcula el "momento" operativo del documento enfocado y ajusta los mini-paneles tipo
     * stepper del panel "Asignación": un documento FINAL recien llegado (EN_DESPACHO) o vuelto
     * Observado, y cualquier documento INTERMEDIO, solo necesitan el mini-panel "Asignación" (sin
     * numeración, comportamiento igual al historico). Un documento FINAL que ya paso por
     * Validación (VALIDADO o EMITIDO) exige primero "① Emisión" y solo despues "② Asignación"
     * (bloqueada hasta que el documento pase de VALIDADO a EMITIDO).
     */
    private void actualizarModoPanelAsigNotif() {
        if (cardEmisionAsigNotif == null || cardAsignacionAsigNotif == null) {
            return;
        }
        boolean segundoMomento = esSegundoMomentoAsigNotif(documentoAsigNotifFoco);

        cardEmisionAsigNotif.setVisible(segundoMomento);
        if (segundoMomento) {
            boolean emisionCompletada = emisionCompletadaAsigNotif(documentoAsigNotifFoco);
            cardEmisionAsigNotif.setStepNumber(1);
            cardEmisionAsigNotif.setStatus(
                    emisionCompletada ? "Completado" : "Pendiente",
                    emisionCompletada ? AppV2Theme.SOFT_GREEN : AppV2Theme.SOFT_ORANGE,
                    emisionCompletada ? AppV2Theme.SUCCESS : AppV2Theme.WARNING);
            cmbResultadoEmisionNotif.setSelectedItem("Aprobado");
            txtComentarioEmisionNotif.setText("");
            if (panelComentarioEmisionNotif != null) {
                panelComentarioEmisionNotif.setVisible(false);
            }
        }
        cardAsignacionAsigNotif.setStepNumber(segundoMomento ? 2 : null);
        actualizarBloqueoAsignacionAsigNotif();
        cardEmisionAsigNotif.revalidate();
        cardEmisionAsigNotif.repaint();
        actualizarBotonRegistrarSupervisionVisible(segundoMomento);
    }

    private static boolean esSegundoMomentoAsigNotif(
            com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO doc) {
        boolean esFinal = doc != null && "FINAL".equalsIgnoreCase(doc.getClasificacion());
        String estado = doc == null ? "" : doc.getEstadoDocumentoCodigo();
        return esFinal && ("VALIDADO".equalsIgnoreCase(estado) || "EMITIDO".equalsIgnoreCase(estado));
    }

    private static boolean emisionCompletadaAsigNotif(
            com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO doc) {
        return esSegundoMomentoAsigNotif(doc) && "EMITIDO".equalsIgnoreCase(doc.getEstadoDocumentoCodigo());
    }

    /**
     * El mini-panel "② Asignación" queda bloqueado mientras el documento FINAL sigue Validado (no
     * emitido todavia) — salvo que el supervisor haya elegido "Observado" en "① Emisión": en ese
     * caso se desbloquea igual, porque {@link #registrarSupervisionEmisionNotif()} reutiliza el
     * mismo combo "Equipo destino"/"Usuario destino" de este mini-panel (Eq. Análisis/Eq.
     * Ejecución) en vez de duplicar un segundo combo solo para Observado (pedido explicito del
     * usuario, 05/08/2026: "no debería tener el bloque de destino operativo ya que eso ya se está
     * mostrando en el bloque inferior de asignación").
     */
    private void actualizarBloqueoAsignacionAsigNotif() {
        if (cardAsignacionAsigNotif == null) {
            return;
        }
        boolean segundoMomento = esSegundoMomentoAsigNotif(documentoAsigNotifFoco);
        boolean emisionCompletada = emisionCompletadaAsigNotif(documentoAsigNotifFoco);
        boolean observado = "Observado".equals(cmbResultadoEmisionNotif.getSelectedItem());
        boolean desbloqueado = !segundoMomento || emisionCompletada || observado;
        cardAsignacionAsigNotif.setLocked(
                !desbloqueado, "Complete la emisión del documento para habilitar la asignación.");
        if (segundoMomento) {
            cardAsignacionAsigNotif.setStatus(
                    desbloqueado ? "Pendiente" : "Bloqueado",
                    desbloqueado ? AppV2Theme.SOFT_ORANGE : AppV2Theme.SOFT_GRAY,
                    desbloqueado ? AppV2Theme.WARNING : AppV2Theme.MUTED);
        } else {
            cardAsignacionAsigNotif.setStatus(null, null, null);
        }
        cardAsignacionAsigNotif.revalidate();
        cardAsignacionAsigNotif.repaint();
    }

    /**
     * "Registrar Supervisión" vive en el footer fijo del panel (junto a "Generar asignación"/
     * "Cancelar", encima de "Generar asignación"), no dentro del contenido scrolleable de
     * "Emisión", para que quede siempre visible sin desplazarse (pedido explicito del usuario).
     * Como el footer usa GridLayout (asigna espacio fijo por fila sin importar visibilidad), se
     * agrega/quita el boton dinamicamente en vez de solo alternar setVisible, para no dejar un
     * hueco en blanco en el primer momento (donde "Emisión" ni siquiera aplica).
     */
    private void actualizarBotonRegistrarSupervisionVisible(boolean visible) {
        if (panelAccionesAsigNotif == null) {
            return;
        }
        boolean presente = false;
        for (Component componente : panelAccionesAsigNotif.getComponents()) {
            if (componente == btnRegistrarSupervisionEmisionNotif) {
                presente = true;
                break;
            }
        }
        if (visible && !presente) {
            panelAccionesAsigNotif.add(btnRegistrarSupervisionEmisionNotif, 0);
        } else if (!visible && presente) {
            panelAccionesAsigNotif.remove(btnRegistrarSupervisionEmisionNotif);
        }
        panelAccionesAsigNotif.revalidate();
        panelAccionesAsigNotif.repaint();
    }

    private void actualizarSubtituloPanelesAsigNotif() {
        String titular = documentoAsigNotifFoco == null || documentoAsigNotifFoco.getTitular() == null
                ? "" : documentoAsigNotifFoco.getTitular().trim();
        if (panelAsignacionOperativaNotif != null) {
            panelAsignacionOperativaNotif.setSubtitle(titular);
        }
    }

    private void actualizarPanelDatosAsigNotif() {
        if (documentoAsigNotifFoco == null || documentoAsigNotifFoco.getIdExpediente() == null) {
            datosAsigNotif.limpiar();
            return;
        }
        final Long idExpediente = documentoAsigNotifFoco.getIdExpediente();
        SwingWorker<com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO, Void> worker =
                new SwingWorker<com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO, Void>() {
            @Override
            protected com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO doInBackground() throws Exception {
                return asignacionExpedienteServiceNotif.obtenerExpedientePorId(idExpediente);
            }

            @Override
            protected void done() {
                if (documentoAsigNotifFoco == null || !idExpediente.equals(documentoAsigNotifFoco.getIdExpediente())) {
                    return;
                }
                try {
                    datosAsigNotif.poblar(get());
                } catch (Exception ex) {
                    datosAsigNotif.limpiar();
                }
            }
        };
        worker.execute();
    }

    private void actualizarPanelFirmaAsigNotif() {
        if (documentoAsigNotifFoco == null || documentoAsigNotifFoco.getIdExpediente() == null) {
            idExpedienteFirmaAsigNotifCargado = null;
            documentosFirmaTreePanel.setDocumentos(null, new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>());
            return;
        }
        cargarDocumentosFirmaAsigNotif(documentoAsigNotifFoco.getIdExpediente());
    }

    private void cargarDocumentosFirmaAsigNotif(final Long idExpediente) {
        idExpedienteFirmaAsigNotifCargado = idExpediente;
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> doInBackground() throws Exception {
                return documentoAnalisisService.listarDocumentosPorExpediente(idExpediente);
            }

            @Override
            protected void done() {
                if (idExpedienteFirmaAsigNotifCargado == null || !idExpedienteFirmaAsigNotifCargado.equals(idExpediente)) {
                    return;
                }
                try {
                    documentosFirmaTreePanel.setDocumentos(idExpediente, get());
                } catch (Exception ex) {
                    documentosFirmaTreePanel.setDocumentos(idExpediente, new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>());
                    mostrarError("No se pudieron cargar los documentos del expediente.", ex);
                }
            }
        };
        worker.execute();
    }

    private JPanel crearPanelDetalleAsignacionNotif() {
        AppV2SideActionPanel panelDatos = datosAsigNotif.crearPanel(
                "Panel de datos", new Color(57, 125, 199), this::cerrarPanelAsignacionNotif);
        AppV2SideActionPanel panelAsignacion = crearPanelAsignacionOperativaNotif();
        panelAsignacionOperativaNotif = panelAsignacion;
        return crearPanelAsignacionConTabNotif(panelDatos, panelAsignacion);
    }

    private void cerrarPanelAsignacionNotif() {
        panelAsigNotifCerradoPorUsuario = true;
        if (splitBandejasNotif != null) {
            splitBandejasNotif.setSideVisible(false);
        }
    }

    private AppV2SideActionPanel crearPanelAsignacionOperativaNotif() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de Asignación y Firma", this::cerrarPanelAsignacionNotif);
        panel.setAccentColor(new Color(10, 118, 145));

        sectionAsignacionMultipleNotif = crearAsignacionMultipleSeccionNotif();
        JPanel contenidoAsignacion = new JPanel();
        contenidoAsignacion.setOpaque(false);
        contenidoAsignacion.setLayout(new BoxLayout(contenidoAsignacion, BoxLayout.Y_AXIS));
        sectionAsignacionMultipleNotif.setAlignmentX(Component.LEFT_ALIGNMENT);
        AppV2SideSectionPanel destinoSeccion = crearDestinoAsignacionSeccionNotif();
        destinoSeccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenidoAsignacion.add(sectionAsignacionMultipleNotif);
        contenidoAsignacion.add(Box.createVerticalStrut(12));
        contenidoAsignacion.add(destinoSeccion);

        cardEmisionAsigNotif = new AppV2StepCardPanel("Emisión");
        cardEmisionAsigNotif.setContent(crearDocumentosFirmaSeccionNotif());
        cardAsignacionAsigNotif = new AppV2StepCardPanel("Asignación");
        cardAsignacionAsigNotif.setContent(contenidoAsignacion);

        panel.addSection(cardEmisionAsigNotif);
        panel.addSection(cardAsignacionAsigNotif);
        panel.addSection(crearHistorialAsignacionSeccionNotif());

        JPanel acciones = new JPanel(new GridLayout(0, 1, 0, 8));
        acciones.setOpaque(false);
        AppV2Theme.estilizarBotonPrimario(btnRegistrarSupervisionEmisionNotif);
        AppV2Theme.estilizarBotonPrimario(btnGenerarAsignacionNotif);
        acciones.add(btnGenerarAsignacionNotif);
        acciones.add(btnCancelarAsignacionNotif);
        panel.setFooter(acciones);
        panelAccionesAsigNotif = acciones;

        cmbEquipoNotif.addActionListener(e -> {
            if (!cargandoCombosAsignacionNotif) {
                cargarUsuariosAsignacionNotif();
            }
        });
        chkHabilitarReasignacionNotif.addActionListener(e -> {
            modoReasignacionAsigNotif = chkHabilitarReasignacionNotif.isSelected();
            tablaAsignacionNotif.repaint();
            actualizarPanelAsignacionSeleccionNotif();
        });
        btnGenerarAsignacionNotif.addActionListener(e -> generarAsignacionNotificacion());
        btnCancelarAsignacionNotif.addActionListener(e -> {
            hojasEnvioAsignacionMultipleNotif.clear();
            for (int i = 0; i < asignacionNotifModel.getRowCount(); i++) {
                if (asignacionNotifModel.isCellEditable(i, COL_ASIG_SELECCION)) {
                    asignacionNotifModel.setValueAt(Boolean.FALSE, i, COL_ASIG_SELECCION);
                }
            }
            chkHabilitarReasignacionNotif.setSelected(false);
            modoReasignacionAsigNotif = false;
            tablaAsignacionNotif.repaint();
            actualizarPanelAsignacionSeleccionNotif();
            cerrarPanelAsignacionNotif();
        });
        return panel;
    }

    private AppV2SideSectionPanel crearAsignacionMultipleSeccionNotif() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Documentos seleccionados");
        JLabel ayuda = new JLabel("Revise los documentos antes de generar la asignación.");
        ayuda.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        ayuda.setForeground(AppV2Theme.TEXT_SECONDARY);

        chkHabilitarReasignacionNotif.setOpaque(false);
        chkHabilitarReasignacionNotif.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        chkHabilitarReasignacionNotif.setToolTipText(
                "Permite marcar en el listado documentos ya asignados, para reasignarlos con una nueva hoja de envío.");

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        ayuda.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkHabilitarReasignacionNotif.setAlignmentX(Component.LEFT_ALIGNMENT);
        encabezado.add(ayuda);
        encabezado.add(chkHabilitarReasignacionNotif);

        asignacionMultipleTableNotif.setRowHeight(28);
        asignacionMultipleTableNotif.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        AppV2TableColumnSizer.applyWidths(asignacionMultipleTableNotif, 175, 150, 160, 160, 175);

        JPanel content = new JPanel(new BorderLayout(6, 6));
        content.setOpaque(false);
        content.add(encabezado, BorderLayout.NORTH);
        asignacionMultipleScrollNotif = new JScrollPane(asignacionMultipleTableNotif);
        asignacionMultipleScrollNotif.setPreferredSize(new Dimension(320, 170));
        asignacionMultipleScrollNotif.setMinimumSize(new Dimension(280, 120));
        asignacionMultipleScrollNotif.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        asignacionMultipleScrollNotif.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        asignacionMultipleScrollNotif.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        content.add(asignacionMultipleScrollNotif, BorderLayout.CENTER);
        section.addContent(content);
        ajustarTamanoAsignacionMultipleNotif();
        return section;
    }

    private AppV2SideSectionPanel crearDestinoAsignacionSeccionNotif() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Destino operativo");
        cmbEquipoNotif.setPreferredSize(new Dimension(180, 32));
        cmbUsuarioNotif.setPreferredSize(new Dimension(180, 32));
        section.addRow("Equipo destino", cmbEquipoNotif);
        section.addRow("Usuario destino", cmbUsuarioNotif);
        return section;
    }

    /**
     * A diferencia de las demas secciones de este panel, NO usa {@link AppV2SideSectionPanel}
     * (titulo + "form" GridBagLayout via addContent): con un unico componente en
     * gridwidth=2/weightx=1 y ningun otro componente que defina el peso de cada columna
     * individualmente, GridBagLayout puede repartir mal el deficit de ancho entre las 2 columnas
     * "virtuales" que crea el span (limitacion conocida de GridBagLayout, no un error de esta
     * clase), dejando el contenido angosto y desplazado a la derecha (visto en captura del
     * usuario: 05/08/2026, el ancho no llegaba al 100% pese a fijar preferredSize y alignmentX).
     * Se arma la seccion a mano con BorderLayout puro (mismo patron ya usado y confirmado
     * funcional en {@link AppV2StepCardPanel}: titulo arriba, contenido al centro, sin
     * GridBagLayout de por medio), que siempre estira el contenido al 100% sin ambiguedad.
     */
    /**
     * Causa real confirmada con diagnostico en vivo (05/08/2026, ver historial de commits): el
     * "sections" de {@link AppV2SideActionPanel} usa BoxLayout, que en el eje transversal NO
     * estira cada hijo al ancho del contenedor (a diferencia de GridBagLayout con weightx o
     * BorderLayout.CENTER) — cada hijo se renderiza a su propio ancho preferido. Los mini-paneles
     * "Emision"/"Asignacion" solo "parecen" ocupar el 100% porque su contenido interno (tabla
     * "Documentos seleccionados" a ~820px + relleno) ya es naturalmente asi de ancho. El bloque de
     * Historial, con solo texto simple, nunca llega a ese ancho por si solo (medido: 901px de
     * seccion propia vs 1314px del contenedor real). Fix: un listener de redimension del padre
     * mantiene el ancho preferido de "content" sincronizado con el ancho real disponible.
     */
    private JPanel crearHistorialAsignacionSeccionNotif() {
        tablaHistorialAsignacionNotif.setRowHeight(28);
        tablaHistorialAsignacionNotif.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        AppV2TableColumnSizer.applyWidths(tablaHistorialAsignacionNotif, 110, 140, 130, 110, 130, 140, 90);

        JPanel section = new JPanel(new BorderLayout(0, 10));
        section.setOpaque(false);
        section.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppV2Theme.BORDER));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitulo = new JLabel("Historial de asignación / reasignación");
        lblTitulo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblTitulo.setForeground(AppV2Theme.TEXT_PRIMARY);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        content.add(panelHistorialAsignacionNotif, BorderLayout.CENTER);
        content.setPreferredSize(new Dimension(1200, 180));

        section.add(lblTitulo, BorderLayout.NORTH);
        section.add(content, BorderLayout.CENTER);

        section.addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsAdapter() {
            @Override
            public void ancestorResized(java.awt.event.HierarchyEvent e) {
                java.awt.Container padre = section.getParent();
                if (padre == null || padre.getWidth() <= 0) {
                    return;
                }
                Dimension actual = content.getPreferredSize();
                if (actual.width == padre.getWidth()) {
                    return;
                }
                content.setPreferredSize(new Dimension(padre.getWidth(), actual.height));
                section.revalidate();
            }
        });
        return section;
    }

    private void ajustarTamanoAsignacionMultipleNotif() {
        if (asignacionMultipleTableNotif == null || asignacionMultipleScrollNotif == null) {
            return;
        }
        int ancho = 0;
        for (int i = 0; i < asignacionMultipleTableNotif.getColumnCount(); i++) {
            ancho += asignacionMultipleTableNotif.getColumnModel().getColumn(i).getPreferredWidth();
        }
        ancho += asignacionMultipleTableNotif.getIntercellSpacing().width;
        int altoEncabezado = asignacionMultipleTableNotif.getTableHeader() != null
                ? asignacionMultipleTableNotif.getTableHeader().getPreferredSize().height
                : 28;
        int altoFilas = Math.max(1, asignacionMultipleTableNotif.getRowCount()) * asignacionMultipleTableNotif.getRowHeight();
        int alto = altoEncabezado + altoFilas + 8;
        Dimension size = new Dimension(Math.max(ancho, 240), Math.max(alto, 64));
        asignacionMultipleTableNotif.setPreferredScrollableViewportSize(size);
        asignacionMultipleScrollNotif.setPreferredSize(size);
        asignacionMultipleScrollNotif.setMinimumSize(size);
        if (asignacionMultipleTableNotif.getParent() != null) {
            asignacionMultipleTableNotif.revalidate();
            asignacionMultipleTableNotif.repaint();
        }
        asignacionMultipleScrollNotif.revalidate();
        asignacionMultipleScrollNotif.repaint();
    }

    private void guardarHojasEnvioAsignacionMultipleNotif() {
        for (int row = 0; row < asignacionMultipleModelNotif.getRowCount() && row < documentosAsignacionMultipleNotif.size(); row++) {
            Object value = asignacionMultipleModelNotif.getValueAt(row, 2);
            Long idDocumento = documentosAsignacionMultipleNotif.get(row).getIdDocumentoAnalizado();
            if (idDocumento != null) {
                hojasEnvioAsignacionMultipleNotif.put(idDocumento, value == null ? "" : value.toString());
            }
        }
    }

    private void cargarPanelAsignacionMultipleNotif(List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> items) {
        documentosAsignacionMultipleNotif.clear();
        asignacionMultipleModelNotif.setRowCount(0);
        java.util.Set<Long> idsVigentes = new java.util.HashSet<Long>();
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : items) {
            if (item == null || item.getIdDocumentoAnalizado() == null) {
                continue;
            }
            documentosAsignacionMultipleNotif.add(item);
            idsVigentes.add(item.getIdDocumentoAnalizado());
            String hojaNuevaPorDefecto = item.isAsignado() ? "" : valorNotif(item.getNumeroHojaEnvioNotificacion());
            asignacionMultipleModelNotif.addRow(new Object[]{
                valorNotif(item.getNumeroExpediente()),
                valorNotif(item.getNumeroExpedienteSgd()),
                hojasEnvioAsignacionMultipleNotif.containsKey(item.getIdDocumentoAnalizado())
                        ? hojasEnvioAsignacionMultipleNotif.get(item.getIdDocumentoAnalizado())
                        : hojaNuevaPorDefecto,
                valorNotif(item.getNumeroHojaEnvioNotificacion()),
                item.isAsignado() ? valorNotif(item.getUsuarioNotificacionActual()) : "Nuevo"
            });
        }
        hojasEnvioAsignacionMultipleNotif.keySet().retainAll(idsVigentes);
        ajustarTamanoAsignacionMultipleNotif();
    }

    private void cargarHistorialAsignacionNotif(Long idDocumentoAnalizado) {
        idDocumentoHistorialAsigNotifActual = idDocumentoAnalizado;
        historialAsignacionModelNotif.setRowCount(0);
        panelHistorialAsignacionNotif.setEmpty(true);
        if (idDocumentoAnalizado == null) {
            return;
        }
        final long solicitud = ++secuenciaHistorialAsigNotif;
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.AsignacionHistorialDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.AsignacionHistorialDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.AsignacionHistorialDTO> doInBackground() throws Exception {
                return documentoAnalisisService.listarHistorialAsignacionesNotificacion(idDocumentoAnalizado);
            }

            @Override
            protected void done() {
                if (solicitud != secuenciaHistorialAsigNotif || !idDocumentoAnalizado.equals(idDocumentoHistorialAsigNotifActual)) {
                    return;
                }
                try {
                    List<com.sdrerc.domain.dto.sdrercapp.AsignacionHistorialDTO> items = get();
                    for (com.sdrerc.domain.dto.sdrercapp.AsignacionHistorialDTO item : items) {
                        historialAsignacionModelNotif.addRow(new Object[]{
                            item.isReasignacionExcepcional() ? "Reasignación" : "Asignación inicial",
                            item.getAbogado().isEmpty() ? "-" : item.getAbogado(),
                            item.getEquipo().isEmpty() ? "-" : item.getEquipo(),
                            item.getNumeroHojaEnvio().isEmpty() ? "-" : item.getNumeroHojaEnvio(),
                            item.getFechaAsignacion() == null ? "-" : item.getFechaAsignacion().format(DATE_HORA_FORMAT_ASIG_NOTIF),
                            item.getAsignadoPor().isEmpty() ? "-" : item.getAsignadoPor(),
                            item.isActiva() ? "Activa" : "Histórica"
                        });
                    }
                    panelHistorialAsignacionNotif.setEmpty(items.isEmpty());
                } catch (Exception ex) {
                    mostrarError("No se pudo cargar el historial de asignación.", ex);
                }
            }
        };
        worker.execute();
    }

    private List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> obtenerDocumentosMarcadosAsigNotif() {
        List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> resultado =
                new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>();
        for (int row = 0; row < asignacionNotifModel.getRowCount(); row++) {
            if (Boolean.TRUE.equals(asignacionNotifModel.getValueAt(row, COL_ASIG_SELECCION))) {
                AsignacionNotifTableRow fila = filaAsignacionNotif(row);
                if (fila != null && fila.esPrincipal()) {
                    resultado.add(fila.principal);
                }
            }
        }
        return resultado;
    }

    private void actualizarPanelAsignacionSeleccionNotif() {
        guardarHojasEnvioAsignacionMultipleNotif();
        List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> marcados = obtenerDocumentosMarcadosAsigNotif();
        List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> paraGrid;
        if (!marcados.isEmpty()) {
            paraGrid = marcados;
        } else if (documentoAsigNotifFoco != null && esFilaSeleccionableAsigNotif(documentoAsigNotifFoco)) {
            paraGrid = java.util.Collections.singletonList(documentoAsigNotifFoco);
        } else {
            paraGrid = new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>();
        }
        cargarPanelAsignacionMultipleNotif(paraGrid);
        btnGenerarAsignacionNotif.setEnabled(!paraGrid.isEmpty());
        if (paraGrid.size() == 1) {
            cargarHistorialAsignacionNotif(paraGrid.get(0).getIdDocumentoAnalizado());
        } else {
            cargarHistorialAsignacionNotif(null);
        }
    }

    /**
     * Contenido del mini-panel "Emisión" (segundo momento), diseñado a semejanza del panel
     * "Verificar" de Verificación: la grilla "Documentos a firmar" y el resto de bloques quedan
     * SIEMPRE visibles, sin CardLayout que oculte nada segun el resultado elegido (igual que
     * Verificación, que nunca oculta su grilla de documentos ni su bloque "Destino operativo"
     * pase lo que pase en el combo Resultado) — pedido explicito del usuario (05/08/2026): un
     * resultado Observado no debe ocultar la grilla, porque puede ser necesario ajustar ahi mismo
     * el Estado documento antes de registrar la supervisión. El unico bloque condicional es
     * "Comentario" (motivo de la observación), que se muestra solo cuando el resultado es
     * Observado. Ya no existe un combo "Destino operativo" propio de este mini-panel: reutiliza
     * el mismo {@link #cmbEquipoNotif}/{@link #cmbUsuarioNotif} del mini-panel "② Asignación"
     * (ese combo ya soporta Eq. Análisis/Eq. Ejecución, ver {@link #generarAsignacionNotificacion()}),
     * en vez de duplicar un segundo combo con las mismas opciones.
     */
    private JPanel crearDocumentosFirmaSeccionNotif() {
        documentosFirmaTreePanel.setHandlers(
                (idDocumento, numeroDocumento, fechaEmision, estadoDocumentoCodigo) ->
                        documentoAnalisisService.registrarFirmaDocumentoNotificacion(
                                idDocumento, numeroDocumento, fechaEmision, estadoDocumentoCodigo),
                () -> {
                    Long idExpedienteFoco = documentoAsigNotifFoco == null ? null : documentoAsigNotifFoco.getIdExpediente();
                    Long idDocumentoFoco = documentoAsigNotifFoco == null ? null : documentoAsigNotifFoco.getIdDocumentoAnalizado();
                    if (idExpedienteFoco != null) {
                        cargarDocumentosFirmaAsigNotif(idExpedienteFoco);
                    }
                    cargarBandejaAsignacionNotificacion(idDocumentoFoco);
                });

        JPanel resultadoSeccion = section("Resultado de Supervisión");
        JPanel gridResultado = new JPanel(new GridBagLayout());
        gridResultado.setOpaque(false);
        gridResultado.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbResultadoEmisionNotif.setPreferredSize(new Dimension(235, 34));
        addRow(gridResultado, 0, "Resultado", cmbResultadoEmisionNotif);
        resultadoSeccion.add(gridResultado, BorderLayout.CENTER);

        JPanel comentarioSeccion = section("Comentario");
        txtComentarioEmisionNotif.setLineWrap(true);
        txtComentarioEmisionNotif.setWrapStyleWord(true);
        comentarioSeccion.add(scrollText(txtComentarioEmisionNotif, 70), BorderLayout.CENTER);
        comentarioSeccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        comentarioSeccion.setVisible(false);
        panelComentarioEmisionNotif = comentarioSeccion;

        JPanel contenedor = new JPanel();
        contenedor.setOpaque(false);
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        resultadoSeccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        documentosFirmaTreePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenedor.add(resultadoSeccion);
        contenedor.add(Box.createVerticalStrut(4));
        contenedor.add(documentosFirmaTreePanel);
        contenedor.add(Box.createVerticalStrut(4));
        contenedor.add(comentarioSeccion);

        cmbResultadoEmisionNotif.addActionListener(e -> actualizarModoResultadoEmisionNotif());
        btnRegistrarSupervisionEmisionNotif.addActionListener(e -> registrarSupervisionEmisionNotif());
        return contenedor;
    }

    private void actualizarModoResultadoEmisionNotif() {
        boolean observado = "Observado".equals(cmbResultadoEmisionNotif.getSelectedItem());
        if (panelComentarioEmisionNotif != null) {
            panelComentarioEmisionNotif.setVisible(observado);
            panelComentarioEmisionNotif.revalidate();
            panelComentarioEmisionNotif.repaint();
        }
        actualizarBloqueoAsignacionAsigNotif();
    }

    private void cargarEstadosDocumentoFirmaAsigNotif() {
        SwingWorker<List<CatalogoItemDTO>, Void> worker = new SwingWorker<List<CatalogoItemDTO>, Void>() {
            @Override
            protected List<CatalogoItemDTO> doInBackground() throws Exception {
                return documentoAnalisisService.listarEstadosDocumentoNotificacion();
            }

            @Override
            protected void done() {
                try {
                    documentosFirmaTreePanel.setEstadosDocumento(get());
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los estados de documento para Emisión.", ex);
                }
            }
        };
        worker.execute();
    }

    /**
     * Unico boton "Registrar Supervisión" del mini-panel "Emisión" (equivalente a "Registrar
     * Verificación" de Verificación): lee {@link #cmbResultadoEmisionNotif} y actua segun el
     * resultado elegido. Observado registra el motivo y deriva el expediente (misma logica que
     * ya usaba "Devolver expediente"), reutilizando el combo "Equipo destino"/"Usuario destino"
     * del mini-panel "② Asignación" ({@link #cmbEquipoNotif}/{@link #cmbUsuarioNotif}, desbloqueado
     * automaticamente por {@link #actualizarBloqueoAsignacionAsigNotif()} mientras el resultado es
     * Observado) en vez de un combo propio duplicado. Aprobado ya no se queda sin registrar nada en
     * BD (07/08/2026, pedido explicito del usuario): {@link #registrarSupervisionEmisionAprobadaNotif()}
     * deja un rastro en {@code EXPEDIENTE_HISTORIAL} (via
     * {@code DocumentoAnalisisDAO.registrarSupervisionEmisionAprobada}), igual que el resto de
     * modulos dejan registrado su resultado (Analisis en EXPEDIENTE_EVALUACION; Verificacion y
     * Ejecucion en la transicion de etapa/estado + EXPEDIENTE_HISTORIAL.motivo).
     */
    private void registrarSupervisionEmisionNotif() {
        if (documentoAsigNotifFoco == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un documento para registrar la supervisión.",
                    "Emisión", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        boolean observado = "Observado".equals(cmbResultadoEmisionNotif.getSelectedItem());
        if (!observado) {
            registrarSupervisionEmisionAprobadaNotif();
            return;
        }
        EquipoNotifItem equipoItem = (EquipoNotifItem) cmbEquipoNotif.getSelectedItem();
        String codigoEquipoDestino = equipoItem == null || equipoItem.equipo == null || equipoItem.equipo.getCodigo() == null
                ? "" : equipoItem.equipo.getCodigo().toUpperCase(java.util.Locale.ROOT);
        if (!"EQ_ANALISIS".equals(codigoEquipoDestino) && !"EQ_EJECUCION".equals(codigoEquipoDestino)) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione Eq. Análisis o Eq. Ejecución en \"Destino operativo\" (bloque Asignación) para registrar la observación.",
                    "Emisión", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String comentario = txtComentarioEmisionNotif.getText();
        if (comentario == null || comentario.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el comentario del motivo de la observación.",
                    "Emisión", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        UsuarioNotifItem usuarioItem = (UsuarioNotifItem) cmbUsuarioNotif.getSelectedItem();
        derivarAsigNotifADestinoOperativo(
                codigoEquipoDestino,
                java.util.Collections.singletonList(documentoAsigNotifFoco),
                equipoItem, usuarioItem, comentario);
    }

    /**
     * Resultado Aprobado de "Registrar Supervisión": exige que el documento enfocado ya esté
     * Emitido (guardado con su icono de disco en "Documentos a firmar") antes de dejar el rastro de
     * auditoría en {@code EXPEDIENTE_HISTORIAL}; no mueve etapa/estado del expediente, eso ocurre
     * recién al generar la asignación en el mini-panel "② Asignación" (ya desbloqueado en cuanto el
     * documento pasa a Emitido, independientemente de este botón).
     */
    private void registrarSupervisionEmisionAprobadaNotif() {
        if (!emisionCompletadaAsigNotif(documentoAsigNotifFoco)) {
            JOptionPane.showMessageDialog(this,
                    "Complete Número/Fecha/Estado y guarde el documento con su icono de disco antes de registrar la supervisión.",
                    "Emisión", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        final Long idExpediente = documentoAsigNotifFoco.getIdExpediente();
        final Long idDocumento = documentoAsigNotifFoco.getIdDocumentoAnalizado();
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                documentoAnalisisService.registrarSupervisionEmisionAprobada(idExpediente, idDocumento);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(JPanelNotificacionV2.this,
                            "Supervisión registrada. Continúe con el bloque \"② Asignación\" para enviar el documento a Notificación.",
                            "Emisión", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    mostrarError("No se pudo registrar la supervisión.", ex);
                }
            }
        };
        worker.execute();
    }

    private JPanel crearPanelAsignacionConTabNotif(
            final AppV2SideActionPanel panelDatos,
            final AppV2SideActionPanel panelAsignacion) {
        JPanel wrapper = new JPanel(null) {
            @Override
            public void doLayout() {
                int width = getWidth();
                int height = getHeight();
                int panelX = PANEL_ASIG_NOTIF_TAB_OVERHANG;
                int panelWidth = Math.max(0, width - panelX);
                int[] positions = calcularPosicionesLenguetasNotif(
                        2, PANEL_ASIG_NOTIF_TAB_HEIGHT, 8, height, PANEL_ASIG_NOTIF_TAB_TOP);
                tabAsigNotifDatos.setBounds(0, positions[0], PANEL_ASIG_NOTIF_TAB_OVERHANG - 6, PANEL_ASIG_NOTIF_TAB_HEIGHT);
                tabAsigNotifAsignacion.setBounds(0, positions[1], PANEL_ASIG_NOTIF_TAB_OVERHANG - 6, PANEL_ASIG_NOTIF_TAB_HEIGHT);
                panelAsigNotifCards.setBounds(panelX, 0, panelWidth, height);
            }
        };
        wrapper.setOpaque(false);
        panelAsigNotifCardsLayout = new CardLayout();
        panelAsigNotifCards = new JPanel(panelAsigNotifCardsLayout);
        panelAsigNotifCards.setOpaque(false);
        panelAsigNotifCards.add(panelDatos, TAB_ASIG_NOTIF_DATOS);
        panelAsigNotifCards.add(panelAsignacion, TAB_ASIG_NOTIF_ASIGNACION);
        tabAsigNotifDatos.setToolTipText("Ver datos del expediente");
        tabAsigNotifAsignacion.setToolTipText("Asignar, firmar o derivar el documento");
        tabAsigNotifDatos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabAsigNotif(TAB_ASIG_NOTIF_DATOS);
            }
        });
        tabAsigNotifAsignacion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabAsigNotif(TAB_ASIG_NOTIF_ASIGNACION);
            }
        });
        wrapper.add(tabAsigNotifDatos);
        wrapper.add(tabAsigNotifAsignacion);
        wrapper.add(panelAsigNotifCards);
        wrapper.setMinimumSize(new Dimension(
                PANEL_ASIG_NOTIF_ANCHO_MINIMO + PANEL_ASIG_NOTIF_TAB_OVERHANG, 0));
        wrapper.setPreferredSize(new Dimension(
                PANEL_ASIG_NOTIF_ANCHO_NORMAL + PANEL_ASIG_NOTIF_TAB_OVERHANG, 0));
        seleccionarTabAsigNotif(TAB_ASIG_NOTIF_DATOS);
        return wrapper;
    }

    private void seleccionarTabAsigNotif(String tab) {
        if (tab == null || panelAsigNotifCardsLayout == null || panelAsigNotifCards == null) {
            return;
        }
        boolean mismaTab = tab.equals(tabAsigNotifActiva);
        tabAsigNotifActiva = tab;
        panelAsigNotifCardsLayout.show(panelAsigNotifCards, tab);
        if (splitBandejasNotif != null && splitBandejasNotif.isSideVisible() && mismaTab) {
            splitBandejasNotif.setSideExpanded(!splitBandejasNotif.isSideExpanded());
        }
        panelAsigNotifCards.revalidate();
        panelAsigNotifCards.repaint();
        actualizarLenguetasAsigNotif();
    }

    private void actualizarLenguetasAsigNotif() {
        boolean expandido = splitBandejasNotif != null && splitBandejasNotif.isSideExpanded();
        tabAsigNotifDatos.setState(TAB_ASIG_NOTIF_DATOS.equals(tabAsigNotifActiva), TAB_ASIG_NOTIF_DATOS.equals(tabAsigNotifActiva) && expandido);
        tabAsigNotifAsignacion.setState(TAB_ASIG_NOTIF_ASIGNACION.equals(tabAsigNotifActiva), TAB_ASIG_NOTIF_ASIGNACION.equals(tabAsigNotifActiva) && expandido);
    }

    private void cargarBandejaAsignacionNotificacion() {
        cargarBandejaAsignacionNotificacion(null);
    }

    /**
     * @param idDocumentoAnalizadoAReseleccionar si no es null, tras recargar busca esa fila y la
     *      reselecciona (dispara el listener de seleccion, que repuebla documentoAsigNotifFoco),
     *      en vez de dejar el panel lateral cerrado. Necesario para que el mini-panel "②
     *      Asignación" se vea desbloquearse en el mismo lugar tras guardar la firma o tras generar
     *      una asignación de un unico documento, sin que el usuario tenga que volver a buscarlo.
     *      Ademas, si el panel lateral estaba abierto antes de recargar, se fuerza a que siga
     *      abierto tras la reseleccion: {@link #tablaAsignacionNotif}.clearSelection() (parte de
     *      {@code poblarGrillaAsignacionNotif}, invocado desde {@code aplicarFiltrosAsigNotif}) ya
     *      deja {@link #documentoAsigNotifFoco} en null momentaneamente y, con el panel visible en
     *      ese instante, {@link #actualizarVisibilidadPanelAsigNotif()} lo oculta; esa misma guarda
     *      solo puede OCULTAR (nunca reabrir) mientras el split ya esta oculto, asi que sin este
     *      forzado explicito la reseleccion de mas abajo dejaba el panel cerrado pese a encontrar
     *      y reseleccionar la fila correctamente (bug reportado: guardar un documento de la grilla
     *      "Documentos a firmar" sacaba al usuario del panel).
     */
    private void cargarBandejaAsignacionNotificacion(final Long idDocumentoAnalizadoAReseleccionar) {
        final boolean panelAbiertoAntes = splitBandejasNotif != null
                && splitBandejasNotif.isSideVisible() && !panelAsigNotifCerradoPorUsuario;
        lblEstadoAsignacionNotif.setText("Cargando documentos pendientes de asignación...");
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> doInBackground() throws Exception {
                return documentoAnalisisService.listarDocumentosAsignacionNotificacion();
            }

            @Override
            protected void done() {
                try {
                    List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> items = get();
                    documentosAsignacionNotif.clear();
                    documentosAsignacionNotif.addAll(items);
                    actualizarMetricasAsigNotif();
                    aplicarFiltrosAsigNotif();
                    reseleccionarDocumentoAsigNotif(idDocumentoAnalizadoAReseleccionar, panelAbiertoAntes);
                } catch (Exception ex) {
                    documentosAsignacionNotif.clear();
                    actualizarMetricasAsigNotif();
                    poblarGrillaAsignacionNotif(new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>());
                    lblEstadoAsignacionNotif.setText("No se pudieron cargar los documentos pendientes de asignación.");
                    mostrarError("No se pudieron cargar los documentos pendientes de asignación.", ex);
                }
            }
        };
        worker.execute();
    }

    private void reseleccionarDocumentoAsigNotif(Long idDocumentoAnalizado, boolean reabrirPanelSiEstabaAbierto) {
        if (idDocumentoAnalizado == null) {
            return;
        }
        for (int modelRow = 0; modelRow < filasAsignacionNotif.size(); modelRow++) {
            AsignacionNotifTableRow fila = filasAsignacionNotif.get(modelRow);
            if (fila.esPrincipal() && idDocumentoAnalizado.equals(fila.principal.getIdDocumentoAnalizado())) {
                int viewRow = tablaAsignacionNotif.convertRowIndexToView(modelRow);
                if (viewRow >= 0) {
                    tablaAsignacionNotif.setRowSelectionInterval(viewRow, viewRow);
                    if (reabrirPanelSiEstabaAbierto && splitBandejasNotif != null
                            && modoBandejaNotificacion == ModoBandejaNotificacion.ASIGNACION) {
                        splitBandejasNotif.setSideVisible(documentoAsigNotifFoco != null && !panelAsigNotifCerradoPorUsuario);
                    }
                    return;
                }
                break;
            }
        }
        // El documento ya no aparece en la vista filtrada actual (p.ej. cambió de Estado documento
        // tras guardar la firma y el filtro "Estado" de la búsqueda ya no lo incluye), pero sigue
        // existiendo en la bandeja completa: mantener el panel abierto sobre ese mismo documento en
        // vez de cerrarlo, para no sacar al usuario a mitad del flujo Emisión -> Registrar
        // Supervisión -> Asignación.
        if (!reabrirPanelSiEstabaAbierto) {
            return;
        }
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO doc : documentosAsignacionNotif) {
            if (idDocumentoAnalizado.equals(doc.getIdDocumentoAnalizado())) {
                documentoAsigNotifFoco = doc;
                actualizarSubtituloPanelesAsigNotif();
                actualizarPanelDatosAsigNotif();
                actualizarPanelFirmaAsigNotif();
                actualizarPanelAsignacionSeleccionNotif();
                actualizarModoPanelAsigNotif();
                if (splitBandejasNotif != null && modoBandejaNotificacion == ModoBandejaNotificacion.ASIGNACION) {
                    splitBandejasNotif.setSideVisible(!panelAsigNotifCerradoPorUsuario);
                }
                return;
            }
        }
    }

    private void cargarEquiposAsignacionNotif() {
        cargandoCombosAsignacionNotif = true;
        cmbEquipoNotif.removeAllItems();
        cmbEquipoNotif.addItem(EquipoNotifItem.placeholder("Seleccione equipo"));
        cmbUsuarioNotif.removeAllItems();
        cmbUsuarioNotif.addItem(UsuarioNotifItem.placeholder("Seleccione usuario"));
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO> doInBackground() throws Exception {
                return usuarioAsignacionServiceNotif.listarEquiposActivos();
            }

            @Override
            protected void done() {
                try {
                    for (com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO equipo : get()) {
                        String codigo = equipo.getCodigo() == null ? "" : equipo.getCodigo().toUpperCase();
                        if ("EQ_NOTIFICACION".equals(codigo) || "EQ_VALIDACION".equals(codigo)
                                || "EQ_ANALISIS".equals(codigo) || "EQ_EJECUCION".equals(codigo)) {
                            cmbEquipoNotif.addItem(new EquipoNotifItem(equipo));
                        }
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los equipos destino.", ex);
                } finally {
                    cargandoCombosAsignacionNotif = false;
                }
            }
        };
        worker.execute();
    }

    private void cargarUsuariosAsignacionNotif() {
        EquipoNotifItem equipoItem = (EquipoNotifItem) cmbEquipoNotif.getSelectedItem();
        cmbUsuarioNotif.removeAllItems();
        cmbUsuarioNotif.addItem(UsuarioNotifItem.placeholder("Seleccione usuario"));
        if (equipoItem == null || equipoItem.equipo == null) {
            return;
        }
        final Long idEquipo = equipoItem.equipo.getIdEquipo();
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO> doInBackground() throws Exception {
                return usuarioAsignacionServiceNotif.listarUsuariosAsignablesPorEquipo(idEquipo);
            }

            @Override
            protected void done() {
                EquipoNotifItem equipoActual = (EquipoNotifItem) cmbEquipoNotif.getSelectedItem();
                if (equipoActual == null || equipoActual.equipo == null || !idEquipo.equals(equipoActual.equipo.getIdEquipo())) {
                    return;
                }
                try {
                    for (com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO usuario : get()) {
                        cmbUsuarioNotif.addItem(new UsuarioNotifItem(usuario));
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los usuarios del equipo destino.", ex);
                }
            }
        };
        worker.execute();
    }

    private void generarAsignacionNotificacion() {
        guardarHojasEnvioAsignacionMultipleNotif();
        final List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> documentos =
                new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>(documentosAsignacionMultipleNotif);
        if (documentos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione al menos un documento para generar la asignación.",
                    "Asignar Notif.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        EquipoNotifItem equipoItem = (EquipoNotifItem) cmbEquipoNotif.getSelectedItem();
        UsuarioNotifItem usuarioItem = (UsuarioNotifItem) cmbUsuarioNotif.getSelectedItem();
        if (equipoItem == null || equipoItem.equipo == null) {
            JOptionPane.showMessageDialog(this, "Seleccione equipo destino para generar la asignación.",
                    "Asignar Notif.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String codigoEquipoDestino = equipoItem.equipo.getCodigo() == null
                ? "" : equipoItem.equipo.getCodigo().toUpperCase(java.util.Locale.ROOT);
        if ("EQ_ANALISIS".equals(codigoEquipoDestino) || "EQ_EJECUCION".equals(codigoEquipoDestino)) {
            derivarAsigNotifADestinoOperativo(codigoEquipoDestino, documentos, equipoItem, usuarioItem, null);
            return;
        }
        if (usuarioItem == null || usuarioItem.usuario == null) {
            JOptionPane.showMessageDialog(this, "Seleccione equipo y usuario destino para generar la asignación.",
                    "Asignar Notif.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        java.util.Set<String> equiposEsperados = new java.util.HashSet<String>();
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO doc : documentos) {
            equiposEsperados.add(equipoEsperadoParaClasificacionAsigNotif(doc.getClasificacion(), doc.getEstadoDocumentoCodigo()));
        }
        if (equiposEsperados.size() > 1) {
            JOptionPane.showMessageDialog(this,
                    "No puede generar en una misma acción documentos que requieren equipos destino distintos: "
                            + "selecciónelos por separado (Intermedios y Finales ya Emitidos van a Notificación; "
                            + "Finales pendientes de validar van a Validación).",
                    "Asignar Notif.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String equipoEsperado = equiposEsperados.iterator().next();
        String codigoEquipoSeleccionado = equipoItem.equipo.getCodigo() == null
                ? "" : equipoItem.equipo.getCodigo().toUpperCase(java.util.Locale.ROOT);
        if (equipoEsperado != null && !equipoEsperado.equals(codigoEquipoSeleccionado)) {
            JOptionPane.showMessageDialog(this,
                    "EQ_NOTIFICACION".equals(equipoEsperado)
                            ? "Estos documentos deben asignarse al equipo de Notificación."
                            : "Estos documentos deben asignarse al equipo de Validación.",
                    "Asignar Notif.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        final List<Long> idsNuevos = new ArrayList<Long>();
        final List<Long> idsReasignar = new ArrayList<Long>();
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO doc : documentos) {
            if (doc.isAsignado()) {
                idsReasignar.add(doc.getIdDocumentoAnalizado());
            } else {
                idsNuevos.add(doc.getIdDocumentoAnalizado());
            }
        }
        if (!idsReasignar.isEmpty() && !modoReasignacionAsigNotif) {
            JOptionPane.showMessageDialog(this,
                    "Active \"Habilitar reasignación\" para reasignar documentos que ya tienen un responsable asignado.",
                    "Asignar Notif.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String mensajeConfirmacion = "Se generará la asignación de " + documentos.size() + " documento(s). ¿Desea continuar?";
        if (!idsReasignar.isEmpty()) {
            mensajeConfirmacion = "Va a reasignar " + idsReasignar.size()
                    + " documento(s) que ya tienen validador asignado. Se conservará el historial de la asignación anterior.\n"
                    + "¿Desea continuar?";
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                mensajeConfirmacion,
                "Generar asignación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        final Long idEquipoDestino = equipoItem.equipo.getIdEquipo();
        final Long idUsuarioDestino = usuarioItem.usuario.getIdUsuario();
        final java.util.Map<Long, String> hojasEnvio = new java.util.HashMap<Long, String>();
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO doc : documentos) {
            hojasEnvio.put(doc.getIdDocumentoAnalizado(), hojasEnvioAsignacionMultipleNotif.get(doc.getIdDocumentoAnalizado()));
        }
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (!idsNuevos.isEmpty()) {
                    documentoAnalisisService.asignarNotificacionMultiple(
                            idsNuevos, idEquipoDestino, idUsuarioDestino, hojasEnvio, false);
                }
                if (!idsReasignar.isEmpty()) {
                    documentoAnalisisService.asignarNotificacionMultiple(
                            idsReasignar, idEquipoDestino, idUsuarioDestino, hojasEnvio, true);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(
                            JPanelNotificacionV2.this,
                            "La asignación se generó correctamente.",
                            "Asignar Notif.",
                            JOptionPane.INFORMATION_MESSAGE);
                    hojasEnvioAsignacionMultipleNotif.clear();
                    chkHabilitarReasignacionNotif.setSelected(false);
                    modoReasignacionAsigNotif = false;
                    cargarBandejaAsignacionNotificacion(
                            documentos.size() == 1 ? documentos.get(0).getIdDocumentoAnalizado() : null);
                } catch (Exception ex) {
                    mostrarError("No se pudo generar la asignación.", ex);
                }
            }
        };
        worker.execute();
    }

    /**
     * Deriva el expediente de un unico documento a Analisis o a Ejecucion desde el mismo
     * "Destino operativo" del panel fusionado Asignacion+Firma (ver AGENTS.md, entrada de
     * fusion de estas 2 lenguetas). Caso real: el validador observa un documento FINAL, el
     * expediente se queda en NOTIFICACION/POR_VALIDAR (registrarResultadoValidacion no mueve
     * su estado), y el supervisor decide desde aqui a donde debe volver. A diferencia de
     * asignarNotificacionMultiple (que solo mueve documentos dentro de Notificacion sin cambiar
     * la etapa del expediente), esto SI mueve la etapa real, por lo que exige exactamente un
     * documento (no tiene sentido de "hoja de envio"/lote como la asignacion a validador).
     */
    private void derivarAsigNotifADestinoOperativo(
            String codigoEquipoDestino,
            List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> documentos,
            EquipoNotifItem equipoItem,
            UsuarioNotifItem usuarioItem,
            String comentario) {
        if (documentos.size() != 1) {
            JOptionPane.showMessageDialog(this,
                    "Para derivar a Análisis o Ejecución, seleccione un único documento.",
                    "Asignar Notif.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        boolean esAnalisis = "EQ_ANALISIS".equals(codigoEquipoDestino);
        if (esAnalisis && (usuarioItem == null || usuarioItem.usuario == null)) {
            JOptionPane.showMessageDialog(this, "Seleccione el abogado destino en Análisis.",
                    "Asignar Notif.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        final com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO documento = documentos.get(0);
        String mensajeConfirmacion = esAnalisis
                ? "Se derivará el expediente " + documento.getNumeroExpediente() + " a la Bandeja Análisis. ¿Desea continuar?"
                : "Se derivará el expediente " + documento.getNumeroExpediente() + " a la Bandeja Ejecución. ¿Desea continuar?";
        int confirm = JOptionPane.showConfirmDialog(
                this, mensajeConfirmacion, "Derivar expediente", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        final Long idExpediente = documento.getIdExpediente();
        final Long idDocumento = documento.getIdDocumentoAnalizado();
        final Long idEquipoDestino = equipoItem.equipo.getIdEquipo();
        final Long idUsuarioDestino = usuarioItem == null || usuarioItem.usuario == null
                ? null : usuarioItem.usuario.getIdUsuario();
        final com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO equipoDto = equipoItem.equipo;
        final com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO usuarioDto = usuarioItem == null ? null : usuarioItem.usuario;
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (esAnalisis) {
                    asignacionExpedienteServiceNotif.reasignarDesdeCartaRespuesta(idExpediente, equipoDto, usuarioDto, comentario);
                } else {
                    documentoAnalisisService.derivarDocumentoNotificacionAEjecucion(idDocumento, idEquipoDestino, idUsuarioDestino, comentario);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(
                            JPanelNotificacionV2.this,
                            esAnalisis ? "El expediente fue derivado a Análisis." : "El expediente fue derivado a Ejecución.",
                            "Derivar expediente",
                            JOptionPane.INFORMATION_MESSAGE);
                    documentoAsigNotifFoco = null;
                    cargarBandejaAsignacionNotificacion();
                } catch (Exception ex) {
                    mostrarError("No se pudo derivar el expediente.", ex);
                }
            }
        };
        worker.execute();
    }

    private static class EquipoNotifItem {
        private final com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO equipo;
        private final String label;

        private EquipoNotifItem(com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO equipo) {
            this.equipo = equipo;
            this.label = equipo.getDisplayName();
        }

        private EquipoNotifItem(String label) {
            this.equipo = null;
            this.label = label;
        }

        private static EquipoNotifItem placeholder(String label) {
            return new EquipoNotifItem(label);
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class UsuarioNotifItem {
        private final com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO usuario;
        private final String label;

        private UsuarioNotifItem(com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO usuario) {
            this.usuario = usuario;
            this.label = usuario.getDisplayName();
        }

        private UsuarioNotifItem(String label) {
            this.usuario = null;
            this.label = label;
        }

        private static UsuarioNotifItem placeholder(String label) {
            return new UsuarioNotifItem(label);
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private AsignacionNotifTableRow filaAsignacionNotif(int modelRow) {
        if (modelRow < 0 || modelRow >= filasAsignacionNotif.size()) {
            return null;
        }
        return filasAsignacionNotif.get(modelRow);
    }

    private boolean esFilaSeleccionableAsigNotif(com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item) {
        if (item == null || equipoEsperadoParaClasificacionAsigNotif(item.getClasificacion()) == null) {
            return false;
        }
        return !item.isAsignado() || modoReasignacionAsigNotif;
    }

    private static String equipoEsperadoParaClasificacionAsigNotif(String clasificacion) {
        if ("INTERMEDIO".equalsIgnoreCase(clasificacion)) {
            return "EQ_NOTIFICACION";
        }
        if ("FINAL".equalsIgnoreCase(clasificacion)) {
            return "EQ_VALIDACION";
        }
        return null;
    }

    /**
     * Variante consciente del estado del documento, usada al validar el equipo elegido en
     * "Generar asignación": un documento FINAL ya Emitido (segundo momento, mini-panel "②
     * Asignación" ya desbloqueado tras la firma) debe ir a Eq. Notificación, no a Eq. Validación
     * como un FINAL recién llegado (EN_DESPACHO) o vuelto Observado.
     */
    private static String equipoEsperadoParaClasificacionAsigNotif(String clasificacion, String estadoDocumentoCodigo) {
        if ("FINAL".equalsIgnoreCase(clasificacion) && "EMITIDO".equalsIgnoreCase(estadoDocumentoCodigo)) {
            return "EQ_NOTIFICACION";
        }
        return equipoEsperadoParaClasificacionAsigNotif(clasificacion);
    }

    private void agregarFilaPrincipalAsigNotif(com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item) {
        AsignacionNotifTableRow row = AsignacionNotifTableRow.principal(item);
        filasAsignacionNotif.add(row);
        asignacionNotifModel.addRow(new Object[]{
            iconoExpansionAsigNotif(item),
            Boolean.FALSE,
            item.getNumeroExpediente().isEmpty() ? "-" : item.getNumeroExpediente(),
            item.getNumeroExpedienteSgd().isEmpty() ? "-" : item.getNumeroExpedienteSgd(),
            item.getClasificacion().isEmpty() ? "-" : item.getClasificacion(),
            item.getTipoDocumento().isEmpty() ? "-" : item.getTipoDocumento(),
            item.getNumeroDocumento().isEmpty() ? "-" : item.getNumeroDocumento(),
            item.getFechaDocumento() == null ? "-" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(item.getFechaDocumento()),
            item.getTitular().isEmpty() ? "-" : item.getTitular(),
            item.getEstadoExpediente().isEmpty() ? "-" : item.getEstadoExpediente(),
            item.getEstadoDocumento().isEmpty() ? "-" : item.getEstadoDocumento(),
            item.getIdDocumentoAnalizado()
        });
    }

    private void agregarFilaAsociadaAsigNotif(
            com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO principal,
            com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO asociado,
            int index) {
        AsignacionNotifTableRow row = AsignacionNotifTableRow.asociada(principal.getIdDocumentoAnalizado(), asociado);
        filasAsignacionNotif.add(index, row);
        asignacionNotifModel.insertRow(index, new Object[]{
            "",
            null,
            valorAsigNotif(principal.getNumeroExpediente()),
            valorAsigNotif(asociado.getNumeroExpedienteSgd()),
            "-",
            "-",
            "-",
            "-",
            valorAsigNotif(asociado.getTitular()),
            estadoAsociadoAsigNotif(asociado),
            "-",
            asociado.getIdExpediente()
        });
    }

    private static String valorAsigNotif(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }

    private String estadoAsociadoAsigNotif(com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO asociado) {
        if (asociado == null || asociado.getEstadoCodigo() == null || asociado.getEstadoCodigo().isEmpty()) {
            return "Expediente asociado";
        }
        return DisplayNameMapperV2.estado(asociado.getEstadoCodigo());
    }

    private String iconoExpansionAsigNotif(com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item) {
        if (item == null || item.getIdDocumentoAnalizado() == null || item.getTotalRelacionados() <= 0) {
            return "";
        }
        if (principalesCargandoAsigNotif.contains(item.getIdDocumentoAnalizado())) {
            return "loading";
        }
        return principalesExpandidosAsigNotif.contains(item.getIdDocumentoAnalizado()) ? "expanded" : "collapsed";
    }

    private void refrescarIconoExpansionAsigNotif(int modelRow) {
        AsignacionNotifTableRow row = filaAsignacionNotif(modelRow);
        if (row == null || !row.esPrincipal()) {
            return;
        }
        asignacionNotifModel.setValueAt(iconoExpansionAsigNotif(row.principal), modelRow, COL_ASIG_EXPANDIR);
    }

    private void alternarExpansionFilaAsigNotif(int modelRow) {
        AsignacionNotifTableRow row = filaAsignacionNotif(modelRow);
        if (row == null
                || !row.esPrincipal()
                || row.principal.getIdDocumentoAnalizado() == null
                || row.principal.getTotalRelacionados() <= 0) {
            return;
        }
        final Long idGrupo = row.principal.getIdDocumentoAnalizado();
        final Long idExpediente = row.principal.getIdExpediente();
        if (principalesExpandidosAsigNotif.contains(idGrupo)
                || (idGrupo.equals(idExpedienteExpansionActivaAsigNotif) && principalesCargandoAsigNotif.contains(idGrupo))) {
            contraerAsociadosAsigNotif(idGrupo);
            principalesCargandoAsigNotif.remove(idGrupo);
            idExpedienteExpansionActivaAsigNotif = null;
            refrescarIconoExpansionAsigNotif(indiceFilaPrincipalAsigNotif(idGrupo));
            return;
        }
        contraerTodosExceptoAsigNotif(idGrupo);
        idExpedienteExpansionActivaAsigNotif = idGrupo;
        List<com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO> cache = asociadosCacheAsigNotif.get(idGrupo);
        if (cache != null) {
            insertarAsociadosAsigNotif(modelRow, row.principal, cache);
            return;
        }
        if (principalesCargandoAsigNotif.contains(idGrupo)) {
            return;
        }
        principalesCargandoAsigNotif.add(idGrupo);
        refrescarIconoExpansionAsigNotif(modelRow);
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO> doInBackground() throws Exception {
                return relacionadoServiceNotif.listarAsociadosConfirmados(idExpediente);
            }

            @Override
            protected void done() {
                principalesCargandoAsigNotif.remove(idGrupo);
                int principalRow = indiceFilaPrincipalAsigNotif(idGrupo);
                if (principalRow < 0) {
                    return;
                }
                if (!idGrupo.equals(idExpedienteExpansionActivaAsigNotif)) {
                    refrescarIconoExpansionAsigNotif(principalRow);
                    return;
                }
                try {
                    List<com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO> asociados = get();
                    asociadosCacheAsigNotif.put(idGrupo, asociados);
                    insertarAsociadosAsigNotif(principalRow, filasAsignacionNotif.get(principalRow).principal, asociados);
                } catch (Exception ex) {
                    refrescarIconoExpansionAsigNotif(principalRow);
                    mostrarError("No se pudieron cargar los expedientes asociados.", ex);
                }
            }
        };
        worker.execute();
    }

    private void insertarAsociadosAsigNotif(
            int principalRow,
            com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO principal,
            List<com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO> asociados) {
        if (principal == null || principal.getIdDocumentoAnalizado() == null
                || principalesExpandidosAsigNotif.contains(principal.getIdDocumentoAnalizado())) {
            return;
        }
        Long idGrupo = principal.getIdDocumentoAnalizado();
        if (!idGrupo.equals(idExpedienteExpansionActivaAsigNotif)) {
            return;
        }
        contraerTodosExceptoAsigNotif(idGrupo);
        principalRow = indiceFilaPrincipalAsigNotif(idGrupo);
        if (principalRow < 0) {
            return;
        }
        principalesExpandidosAsigNotif.add(idGrupo);
        int insertAt = principalRow + 1;
        if (asociados != null) {
            for (com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO asociado : asociados) {
                agregarFilaAsociadaAsigNotif(principal, asociado, insertAt);
                insertAt++;
            }
        }
        refrescarIconoExpansionAsigNotif(principalRow);
        tablaAsignacionNotif.revalidate();
        tablaAsignacionNotif.repaint();
    }

    private void contraerTodosExceptoAsigNotif(Long idPermitido) {
        List<Long> expandidos = new ArrayList<Long>(principalesExpandidosAsigNotif);
        for (Long id : expandidos) {
            if (id != null && !id.equals(idPermitido)) {
                contraerAsociadosAsigNotif(id);
            }
        }
        List<Long> cargando = new ArrayList<Long>(principalesCargandoAsigNotif);
        for (Long id : cargando) {
            if (id != null && !id.equals(idPermitido)) {
                principalesCargandoAsigNotif.remove(id);
                refrescarIconoExpansionAsigNotif(indiceFilaPrincipalAsigNotif(id));
            }
        }
    }

    private void contraerAsociadosAsigNotif(Long idGrupo) {
        if (idGrupo == null) {
            return;
        }
        int principalRow = indiceFilaPrincipalAsigNotif(idGrupo);
        if (principalRow < 0) {
            principalesExpandidosAsigNotif.remove(idGrupo);
            if (idGrupo.equals(idExpedienteExpansionActivaAsigNotif)) {
                idExpedienteExpansionActivaAsigNotif = null;
            }
            return;
        }
        for (int i = filasAsignacionNotif.size() - 1; i > principalRow; i--) {
            AsignacionNotifTableRow row = filasAsignacionNotif.get(i);
            if (row.esAsociada() && idGrupo.equals(row.getIdGrupo())) {
                filasAsignacionNotif.remove(i);
                asignacionNotifModel.removeRow(i);
            }
        }
        principalesExpandidosAsigNotif.remove(idGrupo);
        if (idGrupo.equals(idExpedienteExpansionActivaAsigNotif)) {
            idExpedienteExpansionActivaAsigNotif = null;
        }
        refrescarIconoExpansionAsigNotif(principalRow);
        tablaAsignacionNotif.revalidate();
        tablaAsignacionNotif.repaint();
    }

    private int indiceFilaPrincipalAsigNotif(Long idGrupo) {
        if (idGrupo == null) {
            return -1;
        }
        for (int i = 0; i < filasAsignacionNotif.size(); i++) {
            AsignacionNotifTableRow row = filasAsignacionNotif.get(i);
            if (row.esPrincipal() && idGrupo.equals(row.principal.getIdDocumentoAnalizado())) {
                return i;
            }
        }
        return -1;
    }

    private Color colorFondoFilaAsigNotif(int viewRow, AsignacionNotifTableRow fila, boolean selected) {
        if (selected) {
            return TABLE_SELECTION_BACKGROUND;
        }
        if (fila != null && fila.esAsociada()) {
            return ASSOCIATED_ROW_BACKGROUND;
        }
        if (fila != null && fila.esPrincipal() && principalesExpandidosAsigNotif.contains(fila.getIdGrupo())) {
            return new Color(238, 250, 252);
        }
        return viewRow % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT;
    }

    private boolean debeMostrarBarraGrupoAsigNotif(AsignacionNotifTableRow fila) {
        if (fila == null || fila.getIdGrupo() == null) {
            return false;
        }
        return fila.esAsociada() || principalesExpandidosAsigNotif.contains(fila.getIdGrupo());
    }

    private Color acentoGrupoAsigNotif(Long groupKey) {
        if (groupKey == null) {
            return GRID_ACTION_ICON_BLUE;
        }
        int index = Math.abs(groupKey.hashCode()) % GROUP_STRIPE_COLORS.length;
        return GROUP_STRIPE_COLORS[index];
    }

    private javax.swing.border.Border bordeContenidoAsociadoAsigNotif(int leftPadding, int rightPadding) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(0, leftPadding, 0, rightPadding));
    }

    private class AsignacionNotifTableModel extends DefaultTableModel {

        private AsignacionNotifTableModel() {
            super(new Object[]{
                "", "", "N° expediente", "N° expediente SGD", "Clas. Documentos",
                "Tipo documento", "N° Documento", "Fecha Emisión", "Titular", "Estado", "Estado doc.", "_ID"
            }, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column != COL_ASIG_SELECCION) {
                return false;
            }
            AsignacionNotifTableRow fila = filaAsignacionNotif(row);
            return fila != null && fila.esPrincipal() && esFilaSeleccionableAsigNotif(fila.principal);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == COL_ASIG_SELECCION ? Boolean.class : Object.class;
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            super.setValueAt(aValue, row, column);
            if (column == COL_ASIG_SELECCION) {
                actualizarPanelAsignacionSeleccionNotif();
            }
        }
    }

    private class AsignacionNotifExpandirRenderer extends JPanel implements TableCellRenderer {

        private final AppV2ExpandCollapseGlyph glyph = new AppV2ExpandCollapseGlyph();

        private AsignacionNotifExpandirRenderer() {
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
            AsignacionNotifTableRow fila = filaAsignacionNotif(modelRow);
            Color background = colorFondoFilaAsigNotif(row, fila, isSelected);
            setBorder(BorderFactory.createCompoundBorder(
                    debeMostrarBarraGrupoAsigNotif(fila)
                            ? BorderFactory.createMatteBorder(0, GROUP_STRIPE_WIDTH, 0, 0, acentoGrupoAsigNotif(fila.getIdGrupo()))
                            : BorderFactory.createEmptyBorder(0, GROUP_STRIPE_WIDTH, 0, 0),
                    BorderFactory.createEmptyBorder(0, 4, 0, 4)));
            setBackground(background);
            if (fila != null && fila.esAsociada()) {
                glyph.configure(AppV2ExpandCollapseGlyph.NONE, GRID_ACTION_ICON_BLUE, background);
                setToolTipText("Expediente asociado al expediente principal.");
                return this;
            }
            if (fila != null
                    && fila.esPrincipal()
                    && fila.principal.getIdDocumentoAnalizado() != null
                    && fila.principal.getTotalRelacionados() > 0) {
                Long idGrupo = fila.principal.getIdDocumentoAnalizado();
                int state = principalesCargandoAsigNotif.contains(idGrupo)
                        ? AppV2ExpandCollapseGlyph.LOADING
                        : (principalesExpandidosAsigNotif.contains(idGrupo)
                        ? AppV2ExpandCollapseGlyph.COLLAPSE
                        : AppV2ExpandCollapseGlyph.EXPAND);
                glyph.configure(state, GRID_ACTION_ICON_BLUE, background);
                setToolTipText(state == AppV2ExpandCollapseGlyph.COLLAPSE
                        ? "Ocultar expedientes asociados"
                        : "Ver expedientes asociados");
            } else {
                glyph.configure(AppV2ExpandCollapseGlyph.NONE, AppV2Theme.TEXT_SECONDARY, background);
                setToolTipText(null);
            }
            return this;
        }
    }

    private class AsignacionNotifRenderer extends DefaultTableCellRenderer {

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
            AsignacionNotifTableRow fila = filaAsignacionNotif(modelRow);
            boolean filaAsociada = fila != null && fila.esAsociada();
            Color cellBackground = colorFondoFilaAsigNotif(row, fila, isSelected);
            if (!isSelected && modelColumn == 9) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
            }
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(filaAsociada && modelColumn != COL_ASIG_EXPEDIENTE
                    ? AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL)
                    : AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            String text = value == null ? "" : value.toString();
            setToolTipText(text.isEmpty() ? null : text);
            if (isSelected) {
                c.setBackground(cellBackground);
                c.setForeground(TABLE_SELECTION_FOREGROUND);
                setBorder(filaAsociada
                        ? bordeContenidoAsociadoAsigNotif(8, 8)
                        : BorderFactory.createEmptyBorder(0, 8, 0, 8));
            } else if (filaAsociada) {
                setBorder(bordeContenidoAsociadoAsigNotif(8, 8));
                c.setBackground(ASSOCIATED_ROW_BACKGROUND);
                c.setForeground(modelColumn == COL_ASIG_EXPEDIENTE
                        ? AppV2Theme.TEXT_PRIMARY
                        : AppV2Theme.TEXT_SECONDARY);
            } else {
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                c.setBackground(cellBackground);
                c.setForeground(modelColumn == COL_ASIG_EXPEDIENTE ? AppV2Theme.PRIMARY : AppV2Theme.TEXT_PRIMARY);
            }
            return c;
        }
    }

    private static final class AsignacionNotifTableRow {

        private final com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO principal;
        private final com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO asociado;
        private final Long idGrupo;

        private AsignacionNotifTableRow(
                com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO principal,
                com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO asociado,
                Long idGrupo) {
            this.principal = principal;
            this.asociado = asociado;
            this.idGrupo = idGrupo;
        }

        private static AsignacionNotifTableRow principal(
                com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO principal) {
            return new AsignacionNotifTableRow(principal, null, principal == null ? null : principal.getIdDocumentoAnalizado());
        }

        private static AsignacionNotifTableRow asociada(
                Long idGrupo, com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO asociado) {
            return new AsignacionNotifTableRow(null, asociado, idGrupo);
        }

        private boolean esPrincipal() {
            return asociado == null && principal != null;
        }

        private boolean esAsociada() {
            return asociado != null;
        }

        private Long getIdGrupo() {
            return idGrupo;
        }
    }

    private static int[] calcularPosicionesLenguetasNotif(int count, int tabHeight, int gap, int containerHeight, int topMargin) {
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

    private AppV2StackedSideTab crearTabAsigNotif(String label, Color idleColor, Color accentColor) {
        return new AppV2StackedSideTab(
                label,
                PANEL_ASIG_NOTIF_TAB_OVERHANG - 6,
                PANEL_ASIG_NOTIF_TAB_HEIGHT,
                idleColor,
                accentColor,
                accentColor.darker());
    }

    private static AppV2StackedSideTab crearTabPanelNotificacion(String label, Color idleColor, Color accentColor) {
        return new AppV2StackedSideTab(
                label,
                PANEL_NOTIFICACION_TAB_OVERHANG - 6,
                PANEL_NOTIFICACION_TAB_HEIGHT,
                idleColor,
                accentColor,
                accentColor.darker());
    }

    private static String valorNotif(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private final class DatosExpedienteNotifPanel {

        private final JLabel lblDias = new JLabel("-");
        private final JLabel lblFechaVencimiento = new JLabel("-");
        private final JLabel lblExpediente = new JLabel("-");
        private final JLabel lblExpedienteSgd = new JLabel("-");
        private final JLabel lblFechaRecepcion = new JLabel("-");
        private final JLabel lblCanalIngreso = new JLabel("-");
        private final JLabel lblTramiteWeb = new JLabel("-");
        private final JLabel lblProcedimiento = new JLabel("-");
        private final JLabel lblTipoDocumento = new JLabel("-");
        private final JLabel lblNumeroDocumento = new JLabel("-");
        private final JLabel lblTipoSolicitud = new JLabel("-");
        private final JLabel lblGrupoFamiliar = new JLabel("-");
        private final JLabel lblTipoActa = new JLabel("-");
        private final JLabel lblNumeroActa = new JLabel("-");
        private final JLabel lblTitular = new JLabel("-");
        private final JLabel lblTipoDocumentoTitular = new JLabel("-");
        private final JLabel lblNumeroDocumentoTitular = new JLabel("-");
        private final JLabel lblSolicitante = new JLabel("-");
        private final JLabel lblTipoDocumentoSolicitante = new JLabel("-");
        private final JLabel lblNumeroDocumentoSolicitante = new JLabel("-");
        private final JLabel lblCorreo = new JLabel("-");
        private final JLabel lblTelefono = new JLabel("-");
        private final JLabel lblDepartamento = new JLabel("-");
        private final JLabel lblProvincia = new JLabel("-");
        private final JLabel lblDistrito = new JLabel("-");
        private final JLabel lblDireccion = new JLabel("-");
        private AppV2SideActionPanel panel;

        private AppV2SideActionPanel crearPanel(String titulo, Color accentColor, Runnable onClose) {
            panel = new AppV2SideActionPanel(titulo, onClose);
            panel.setAccentColor(accentColor);
            AppV2ResponsiveGridPanel secciones = new AppV2ResponsiveGridPanel(320, 2, 12, 12);
            secciones.add(seccionPlazo());
            secciones.add(seccionExpediente());
            secciones.add(seccionActa());
            secciones.add(seccionSolicitud());
            secciones.add(seccionTitular());
            secciones.add(seccionSolicitante());
            secciones.add(seccionNotificacionUbicacion());
            panel.addSection(secciones);
            return panel;
        }

        private AppV2SideSectionPanel seccionPlazo() {
            AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del plazo");
            section.addRow("Días", lblDias);
            section.addRow("Fecha Vencimiento", lblFechaVencimiento);
            return section;
        }

        private AppV2SideSectionPanel seccionExpediente() {
            AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del expediente");
            section.addRow("N° expediente", lblExpediente);
            section.addRow("N° expediente SGD", lblExpedienteSgd);
            return section;
        }

        private AppV2SideSectionPanel seccionActa() {
            AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del acta");
            section.addRow("Tipo de acta", lblTipoActa);
            section.addRow("Nro. acta", lblNumeroActa);
            return section;
        }

        private AppV2SideSectionPanel seccionSolicitud() {
            AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos de solicitud");
            section.addRow("Fecha recepción", lblFechaRecepcion);
            section.addRow("Canal de ingreso", lblCanalIngreso);
            section.addRow("Nro. trámite web", lblTramiteWeb);
            section.addRow("Proc.Registral", lblProcedimiento);
            section.addRow("Tipo documento", lblTipoDocumento);
            section.addRow("N° documento", lblNumeroDocumento);
            section.addRow("Tipo de solicitud", lblTipoSolicitud);
            section.addRow("Grupo familiar", lblGrupoFamiliar);
            return section;
        }

        private AppV2SideSectionPanel seccionTitular() {
            AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del titular");
            section.addRow("Titular", lblTitular);
            section.addRow("Tipo documento", lblTipoDocumentoTitular);
            section.addRow("Número documento", lblNumeroDocumentoTitular);
            return section;
        }

        private AppV2SideSectionPanel seccionSolicitante() {
            AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del solicitante");
            section.addRow("Solicitante", lblSolicitante);
            section.addRow("Tipo documento", lblTipoDocumentoSolicitante);
            section.addRow("Número documento", lblNumeroDocumentoSolicitante);
            return section;
        }

        private AppV2SideSectionPanel seccionNotificacionUbicacion() {
            AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos de notificación y ubicación");
            section.addRow("Correo", lblCorreo);
            section.addRow("Teléfono", lblTelefono);
            section.addRow("Departamento", lblDepartamento);
            section.addRow("Provincia", lblProvincia);
            section.addRow("Distrito", lblDistrito);
            section.addRow("Dirección", lblDireccion);
            return section;
        }

        private void poblar(com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO dto) {
            if (dto == null) {
                limpiar();
                return;
            }
            lblDias.setText(dto.getDiasRestantes() == null ? "-" : String.valueOf(dto.getDiasRestantes()));
            lblFechaVencimiento.setText(dto.getFechaVencimiento() == null
                    ? "-" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(dto.getFechaVencimiento()));
            lblExpediente.setText(valorNotif(dto.getNumeroExpediente()));
            lblExpedienteSgd.setText(valorNotif(dto.getNumeroExpedienteSgd()));
            lblFechaRecepcion.setText(dto.getFechaRecepcion() == null
                    ? "-" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(dto.getFechaRecepcion()));
            lblCanalIngreso.setText(valorNotif(dto.getCanalIngreso()));
            lblTramiteWeb.setText(valorNotif(dto.getNumeroTramiteDocumentario()));
            lblProcedimiento.setText(valorNotif(dto.getProcedimiento()));
            lblTipoDocumento.setText(valorNotif(dto.getTipoDocumento()));
            lblNumeroDocumento.setText(valorNotif(dto.getNumeroDocumento()));
            lblTipoSolicitud.setText(valorNotif(dto.getTipoSolicitud()));
            lblGrupoFamiliar.setText(valorNotif(dto.getGrupoFamiliarEstado()));
            lblTipoActa.setText(valorNotif(dto.getTipoActa()));
            lblNumeroActa.setText(valorNotif(dto.getNumeroActa()));
            lblTitular.setText(valorNotif(dto.getTitular()));
            lblTipoDocumentoTitular.setText(valorNotif(dto.getTipoDocumentoTitular()));
            lblNumeroDocumentoTitular.setText(valorNotif(dto.getNumeroDocumentoTitular()));
            lblSolicitante.setText(valorNotif(dto.getSolicitante()));
            lblTipoDocumentoSolicitante.setText(valorNotif(dto.getTipoDocumentoSolicitante()));
            lblNumeroDocumentoSolicitante.setText(valorNotif(dto.getNumeroDocumentoSolicitante()));
            lblCorreo.setText(valorNotif(dto.getCorreoSolicitante()));
            lblTelefono.setText(valorNotif(dto.getTelefonoSolicitante()));
            lblDepartamento.setText(valorNotif(dto.getDepartamentoSolicitante()));
            lblProvincia.setText(valorNotif(dto.getProvinciaSolicitante()));
            lblDistrito.setText(valorNotif(dto.getDistritoSolicitante()));
            lblDireccion.setText(valorNotif(dto.getDireccionSolicitante()));
            if (panel != null) {
                panel.setSubtitle(dto.getTitular() == null ? "" : dto.getTitular().trim());
            }
        }

        private void limpiar() {
            if (panel != null) {
                panel.setSubtitle("");
            }
            JLabel[] labels = {
                lblDias, lblFechaVencimiento, lblExpediente, lblExpedienteSgd, lblFechaRecepcion,
                lblCanalIngreso, lblTramiteWeb, lblProcedimiento, lblTipoDocumento, lblNumeroDocumento,
                lblTipoSolicitud, lblGrupoFamiliar, lblTipoActa, lblNumeroActa, lblTitular, lblTipoDocumentoTitular,
                lblNumeroDocumentoTitular, lblSolicitante, lblTipoDocumentoSolicitante, lblNumeroDocumentoSolicitante,
                lblCorreo, lblTelefono, lblDepartamento, lblProvincia, lblDistrito, lblDireccion
            };
            for (JLabel lbl : labels) {
                lbl.setText("-");
            }
        }
    }

    private JPanel crearBandejaValidacion() {
        tablaValidacion.setRowHeight(32);
        tablaValidacion.setAutoCreateRowSorter(false);
        tablaValidacion.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaValidacion.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablaValidacion.getTableHeader().setReorderingAllowed(false);
        tablaValidacion.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tablaValidacion.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        tablaValidacion.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        tablaValidacion.setGridColor(AppV2Theme.BORDER);
        tablaValidacion.setShowVerticalLines(false);
        AppV2TableColumnSizer.applyFriendlyDefaults(tablaValidacion);
        AppV2TableColumnSizer.applyWidths(tablaValidacion, 150, 130, 110, 150, 130, 110, 200, 130);
        AppV2ColumnFilterSupport.install(
                "notificacionValidacion",
                tablaValidacion,
                tablaValidacionPanel.getScrollPane(),
                tablaValidacionPanel,
                null);
        tablaValidacion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = tablaValidacion.getSelectedRow();
                    if (viewRow < 0) {
                        return;
                    }
                    int modelRow = tablaValidacion.convertRowIndexToModel(viewRow);
                    if (modelRow >= 0 && modelRow < documentosValidacion.size()) {
                        panelValidacionCerradoPorUsuario = false;
                        abrirPanelValidacion(documentosValidacion.get(modelRow));
                        mostrarPanelLateralNotif(panelLateralValidacionNotif);
                        splitBandejasNotif.setSideVisible(true);
                        seleccionarTabValidacion(TAB_VALIDACION_DATOS);
                    }
                }
            }
        });

        configurarFiltrosValidacion();

        JPanel superior = new JPanel(new BorderLayout(4, 4));
        superior.setOpaque(false);
        superior.add(crearHeaderValidacion(), BorderLayout.NORTH);
        superior.add(crearBuscadorValidacion(), BorderLayout.CENTER);

        JPanel izquierda = new JPanel(new BorderLayout(6, 6));
        izquierda.setOpaque(false);
        izquierda.add(superior, BorderLayout.NORTH);
        AppV2TableSectionPanel section = new AppV2TableSectionPanel(tablaValidacionPanel);
        section.setStatus(lblEstadoValidacion);
        izquierda.add(section, BorderLayout.CENTER);

        panelLateralValidacionNotif = crearPanelDetalleValidacion();
        return izquierda;
    }

    private JPanel crearHeaderValidacion() {
        JPanel metricas = new AppV2ResponsiveGridPanel(190, 4, 12, 10);
        metricas.add(cardValidacionPendientes);
        metricas.add(cardValidacionPorVencer);
        metricas.add(cardValidacionVencidos);
        return metricas;
    }

    private JPanel crearBuscadorValidacion() {
        JPanel acciones = AppV2ActionPanel.right();
        acciones.add(btnBuscarValidacion);
        acciones.add(btnLimpiarValidacion);
        acciones.add(btnRefrescarValidacion);
        return AppV2ExpedientePanelFactory.crearPanelBusquedaEstiloRegistro(
                "Búsqueda",
                txtBusquedaValidacion,
                acciones,
                fechaEmisionDesdeValidacion,
                fechaEmisionHastaValidacion,
                cmbEstadoValidacion,
                null,
                spnLimiteValidacion);
    }

    private void configurarFiltrosValidacion() {
        cmbEstadoValidacion.removeAllItems();
        cmbEstadoValidacion.addItem(new SimpleItem("", "Pendientes de validar"));
        AppV2Theme.estilizarBotonPrimario(btnBuscarValidacion);
        cardValidacionPendientes.setOnClick(() -> activarKpiValidacion(FiltroKpiValidacion.PENDIENTES));
        cardValidacionPorVencer.setOnClick(() -> activarKpiValidacion(FiltroKpiValidacion.POR_VENCER));
        cardValidacionVencidos.setOnClick(() -> activarKpiValidacion(FiltroKpiValidacion.VENCIDOS));
        btnBuscarValidacion.addActionListener(e -> aplicarFiltrosValidacion());
        txtBusquedaValidacion.addActionListener(e -> aplicarFiltrosValidacion());
        btnLimpiarValidacion.addActionListener(e -> limpiarFiltrosValidacion());
        btnRefrescarValidacion.addActionListener(e -> cargarBandejaValidacion());
        restaurarFechasValidacion();
    }

    private void restaurarFechasValidacion() {
        fechaEmisionDesdeValidacion.setDate(DateRangePickerSupport.defaultSearchFromDateCurrentMonth());
        fechaEmisionHastaValidacion.setDate(DateRangePickerSupport.defaultSearchToDate());
    }

    private void limpiarFiltrosValidacion() {
        txtBusquedaValidacion.setText("");
        restaurarFechasValidacion();
        cmbEstadoValidacion.setSelectedIndex(0);
        spnLimiteValidacion.setValue(200);
        kpiActivoValidacion = FiltroKpiValidacion.TODOS;
        marcarKpisValidacion();
        aplicarFiltrosValidacion();
    }

    private void activarKpiValidacion(FiltroKpiValidacion filtro) {
        kpiActivoValidacion = kpiActivoValidacion == filtro ? FiltroKpiValidacion.TODOS : filtro;
        marcarKpisValidacion();
        aplicarFiltrosValidacion();
    }

    private void marcarKpisValidacion() {
        cardValidacionPendientes.setSelected(kpiActivoValidacion == FiltroKpiValidacion.PENDIENTES);
        cardValidacionPorVencer.setSelected(kpiActivoValidacion == FiltroKpiValidacion.POR_VENCER);
        cardValidacionVencidos.setSelected(kpiActivoValidacion == FiltroKpiValidacion.VENCIDOS);
    }

    private void actualizarMetricasValidacion() {
        int pendientes = documentosValidacion.size();
        int porVencer = 0;
        int vencidos = 0;
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : documentosValidacion) {
            Long dias = item.getDiasRestantes();
            if (dias != null && dias < 0) {
                vencidos++;
            } else if (dias != null && dias <= 5) {
                porVencer++;
            }
        }
        cardValidacionPendientes.setValue(String.valueOf(pendientes));
        cardValidacionPorVencer.setValue(String.valueOf(porVencer));
        cardValidacionVencidos.setValue(String.valueOf(vencidos));
    }

    private boolean coincideKpiValidacion(com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item) {
        Long dias = item.getDiasRestantes();
        switch (kpiActivoValidacion) {
            case PENDIENTES:
                return true;
            case POR_VENCER:
                return dias != null && dias >= 0 && dias <= 5;
            case VENCIDOS:
                return dias != null && dias < 0;
            case TODOS:
            default:
                return true;
        }
    }

    private boolean coincideTextoValidacion(com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item, String texto) {
        return contieneTextoAsigNotif(item.getNumeroExpediente(), texto)
                || contieneTextoAsigNotif(item.getNumeroExpedienteSgd(), texto)
                || contieneTextoAsigNotif(item.getTitular(), texto)
                || contieneTextoAsigNotif(item.getNumeroDocumento(), texto);
    }

    private List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> filtrarDocumentosValidacion() {
        String texto = txtBusquedaValidacion.getText() == null ? "" : txtBusquedaValidacion.getText().trim().toLowerCase();
        LocalDate desde = fechaSeleccionadaAsigNotif(fechaEmisionDesdeValidacion);
        LocalDate hasta = fechaSeleccionadaAsigNotif(fechaEmisionHastaValidacion);
        int limite = (Integer) spnLimiteValidacion.getValue();
        List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> resultado =
                new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>();
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : documentosValidacion) {
            if (!coincideKpiValidacion(item)) {
                continue;
            }
            if (!texto.isEmpty() && !coincideTextoValidacion(item, texto)) {
                continue;
            }
            if (item.getFechaDocumento() != null) {
                if (desde != null && item.getFechaDocumento().isBefore(desde)) {
                    continue;
                }
                if (hasta != null && item.getFechaDocumento().isAfter(hasta)) {
                    continue;
                }
            }
            resultado.add(item);
            if (resultado.size() >= limite) {
                break;
            }
        }
        return resultado;
    }

    private void aplicarFiltrosValidacion() {
        poblarGrillaValidacion(filtrarDocumentosValidacion());
    }

    private void poblarGrillaValidacion(List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> items) {
        validacionModel.setRowCount(0);
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : items) {
            validacionModel.addRow(new Object[]{
                item.getNumeroExpediente(),
                item.getNumeroExpedienteSgd().isEmpty() ? "-" : item.getNumeroExpedienteSgd(),
                item.getClasificacion().isEmpty() ? "-" : item.getClasificacion(),
                item.getTipoDocumento().isEmpty() ? "-" : item.getTipoDocumento(),
                item.getNumeroDocumento().isEmpty() ? "-" : item.getNumeroDocumento(),
                item.getFechaDocumento() == null ? "-" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(item.getFechaDocumento()),
                item.getTitular().isEmpty() ? "-" : item.getTitular(),
                item.getEstadoExpediente().isEmpty() ? "-" : item.getEstadoExpediente(),
                item.getEstadoDocumento().isEmpty() ? "-" : item.getEstadoDocumento()
            });
        }
        tablaValidacionPanel.setEmpty(items.isEmpty());
        lblEstadoValidacion.setText(items.isEmpty()
                ? "No hay documentos pendientes de validación con los filtros aplicados."
                : items.size() + " documento(s) encontrados de " + documentosValidacion.size() + " en total.");
    }

    private JPanel crearPanelDetalleValidacion() {
        AppV2SideActionPanel panelDatos = datosValidacionNotif.crearPanel(
                "Panel de datos", new Color(57, 125, 199), this::cerrarPanelValidacionNotif);
        AppV2SideActionPanel panelValidar = crearPanelValidarOperativo();
        panelValidarOperativo = panelValidar;
        return crearPanelValidacionConTab(panelDatos, panelValidar);
    }

    private void cerrarPanelValidacionNotif() {
        panelValidacionCerradoPorUsuario = true;
        if (splitBandejasNotif != null) {
            splitBandejasNotif.setSideVisible(false);
        }
    }

    private AppV2SideActionPanel crearPanelValidarOperativo() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de Validación", this::cerrarPanelValidacionNotif);
        panel.setAccentColor(AppV2Theme.PRIMARY);

        panel.addSection(crearResultadoValidacion());

        panel.addSection(documentosValidacionTreePanel);
        documentosValidacionTreePanel.configurarSoloComentarioEditable();
        documentosValidacionTreePanel.setHandlers(
                documento -> {
                    documentoAnalisisService.guardarDocumentoJerarquico(idExpedienteValidacionSeleccionado, documento);
                    return new com.sdrerc.domain.dto.sdrercapp.AnalisisResultadoDTO(
                            idExpedienteValidacionSeleccionado,
                            "",
                            "GUARDAR_DOCUMENTO_VALIDACION",
                            "",
                            "",
                            "El documento fue guardado correctamente.");
                },
                null,
                null,
                () -> cargarDocumentosValidacion(idExpedienteValidacionSeleccionado));

        JPanel acciones = new JPanel(new GridLayout(0, 1, 0, 8));
        acciones.setOpaque(false);
        AppV2Theme.estilizarBotonPrimario(btnRegistrarValidacion);
        acciones.add(btnRegistrarValidacion);
        acciones.add(btnCancelarValidacion);
        panel.setFooter(acciones);

        cmbResultadoValidacion.addActionListener(e -> actualizarComentarioValidacionHabilitado());
        btnRegistrarValidacion.addActionListener(e -> registrarValidacion());
        btnCancelarValidacion.addActionListener(e -> limpiarPanelValidacion());
        limpiarPanelValidacion();
        return panel;
    }

    private JPanel crearResultadoValidacion() {
        JPanel panel = section("Resultado de validación");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbResultadoValidacion.setPreferredSize(new Dimension(200, 32));
        addRow(grid, 0, "Resultado", cmbResultadoValidacion);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private void actualizarComentarioValidacionHabilitado() {
        SimpleItem seleccionado = (SimpleItem) cmbResultadoValidacion.getSelectedItem();
        boolean esObservado = seleccionado != null && "OBSERVADO".equalsIgnoreCase(seleccionado.codigo);
        txtComentarioValidacion.setEnabled(esObservado);
        if (!esObservado) {
            txtComentarioValidacion.setText("");
        }
    }

    private void cargarResultadosValidacion() {
        cmbResultadoValidacion.removeAllItems();
        SwingWorker<List<CatalogoItemDTO>, Void> worker = new SwingWorker<List<CatalogoItemDTO>, Void>() {
            @Override
            protected List<CatalogoItemDTO> doInBackground() throws Exception {
                return documentoAnalisisService.listarResultadosValidacion();
            }

            @Override
            protected void done() {
                try {
                    List<CatalogoItemDTO> items = get();
                    if (items == null || items.isEmpty()) {
                        cmbResultadoValidacion.addItem(new SimpleItem("APROBADO", "Aprobado"));
                        cmbResultadoValidacion.addItem(new SimpleItem("OBSERVADO", "Observado"));
                    } else {
                        for (CatalogoItemDTO item : items) {
                            cmbResultadoValidacion.addItem(new SimpleItem(item.getCodigo(), item.getNombre()));
                        }
                    }
                    actualizarComentarioValidacionHabilitado();
                } catch (Exception ex) {
                    cmbResultadoValidacion.addItem(new SimpleItem("APROBADO", "Aprobado"));
                    cmbResultadoValidacion.addItem(new SimpleItem("OBSERVADO", "Observado"));
                }
            }
        };
        worker.execute();
    }

    private JPanel crearPanelValidacionConTab(
            final AppV2SideActionPanel panelDatos,
            final AppV2SideActionPanel panelValidar) {
        JPanel wrapper = new JPanel(null) {
            @Override
            public void doLayout() {
                int width = getWidth();
                int height = getHeight();
                int panelX = PANEL_VALIDACION_TAB_OVERHANG;
                int panelWidth = Math.max(0, width - panelX);
                int[] positions = calcularPosicionesLenguetasNotif(
                        2, PANEL_VALIDACION_TAB_HEIGHT, 8, height, PANEL_VALIDACION_TAB_TOP);
                tabValidacionDatos.setBounds(0, positions[0], PANEL_VALIDACION_TAB_OVERHANG - 6, PANEL_VALIDACION_TAB_HEIGHT);
                tabValidacionValidar.setBounds(0, positions[1], PANEL_VALIDACION_TAB_OVERHANG - 6, PANEL_VALIDACION_TAB_HEIGHT);
                panelValidacionCards.setBounds(panelX, 0, panelWidth, height);
            }
        };
        wrapper.setOpaque(false);
        panelValidacionCardsLayout = new CardLayout();
        panelValidacionCards = new JPanel(panelValidacionCardsLayout);
        panelValidacionCards.setOpaque(false);
        panelValidacionCards.add(panelDatos, TAB_VALIDACION_DATOS);
        panelValidacionCards.add(panelValidar, TAB_VALIDACION_VALIDAR);
        tabValidacionDatos.setToolTipText("Ver datos del expediente");
        tabValidacionValidar.setToolTipText("Validar el documento");
        tabValidacionDatos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabValidacion(TAB_VALIDACION_DATOS);
            }
        });
        tabValidacionValidar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabValidacion(TAB_VALIDACION_VALIDAR);
            }
        });
        wrapper.add(tabValidacionDatos);
        wrapper.add(tabValidacionValidar);
        wrapper.add(panelValidacionCards);
        wrapper.setMinimumSize(new Dimension(
                PANEL_VALIDACION_ANCHO_MINIMO + PANEL_VALIDACION_TAB_OVERHANG, 0));
        wrapper.setPreferredSize(new Dimension(
                PANEL_VALIDACION_ANCHO_NORMAL + PANEL_VALIDACION_TAB_OVERHANG, 0));
        seleccionarTabValidacion(TAB_VALIDACION_DATOS);
        return wrapper;
    }

    private void seleccionarTabValidacion(String tab) {
        if (tab == null || panelValidacionCardsLayout == null || panelValidacionCards == null) {
            return;
        }
        boolean mismaTab = tab.equals(tabValidacionActiva);
        tabValidacionActiva = tab;
        panelValidacionCardsLayout.show(panelValidacionCards, tab);
        if (splitBandejasNotif != null && splitBandejasNotif.isSideVisible() && mismaTab) {
            splitBandejasNotif.setSideExpanded(!splitBandejasNotif.isSideExpanded());
        }
        panelValidacionCards.revalidate();
        panelValidacionCards.repaint();
        boolean expandido = splitBandejasNotif != null && splitBandejasNotif.isSideExpanded();
        tabValidacionDatos.setState(TAB_VALIDACION_DATOS.equals(tabValidacionActiva), TAB_VALIDACION_DATOS.equals(tabValidacionActiva) && expandido);
        tabValidacionValidar.setState(TAB_VALIDACION_VALIDAR.equals(tabValidacionActiva), TAB_VALIDACION_VALIDAR.equals(tabValidacionActiva) && expandido);
    }

    private void cargarBandejaValidacion() {
        lblEstadoValidacion.setText("Cargando documentos pendientes de validación...");
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> doInBackground() throws Exception {
                return documentoAnalisisService.listarDocumentosValidacion();
            }

            @Override
            protected void done() {
                try {
                    List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> items = get();
                    documentosValidacion.clear();
                    documentosValidacion.addAll(items);
                    actualizarMetricasValidacion();
                    aplicarFiltrosValidacion();
                } catch (Exception ex) {
                    documentosValidacion.clear();
                    actualizarMetricasValidacion();
                    poblarGrillaValidacion(new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>());
                    lblEstadoValidacion.setText("No se pudieron cargar los documentos pendientes de validación.");
                    mostrarError("No se pudieron cargar los documentos pendientes de validación.", ex);
                }
            }
        };
        worker.execute();
    }

    private void cargarDocumentosValidacion(Long idExpediente) {
        if (idExpediente == null) {
            return;
        }
        SwingWorker<List<DocumentoAnalizadoDTO>, Void> worker = new SwingWorker<List<DocumentoAnalizadoDTO>, Void>() {
            @Override
            protected List<DocumentoAnalizadoDTO> doInBackground() throws Exception {
                return documentoAnalisisService.listarDocumentosAnalizados(idExpediente);
            }

            @Override
            protected void done() {
                if (!idExpediente.equals(idExpedienteValidacionSeleccionado)) {
                    return;
                }
                try {
                    documentosValidacionTreePanel.setDocumentos(idExpediente, get());
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los documentos del expediente.", ex);
                }
            }
        };
        worker.execute();
    }

    private void abrirPanelValidacion(com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item) {
        idDocumentoValidacionSeleccionado = item.getIdDocumentoAnalizado();
        idExpedienteValidacionSeleccionado = item.getIdExpediente();
        lblPanelValidacionTitulo.setText("Panel de Validación - " + item.getNumeroExpediente());
        if (panelValidarOperativo != null) {
            panelValidarOperativo.setSubtitle(item.getTitular() == null ? "" : item.getTitular().trim());
        }
        btnRegistrarValidacion.setEnabled(true);
        if (cmbResultadoValidacion.getItemCount() > 0) {
            cmbResultadoValidacion.setSelectedIndex(0);
        }
        txtComentarioValidacion.setText("");
        actualizarComentarioValidacionHabilitado();
        datosValidacionNotif.limpiar();
        final Long idExpediente = item.getIdExpediente();
        cargarDocumentosValidacion(idExpediente);
        SwingWorker<com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO, Void> workerDatos =
                new SwingWorker<com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO, Void>() {
            @Override
            protected com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO doInBackground() throws Exception {
                return asignacionExpedienteServiceNotif.obtenerExpedientePorId(idExpediente);
            }

            @Override
            protected void done() {
                if (idExpedienteValidacionSeleccionado == null || !idExpedienteValidacionSeleccionado.equals(idExpediente)) {
                    return;
                }
                try {
                    datosValidacionNotif.poblar(get());
                } catch (Exception ex) {
                    datosValidacionNotif.limpiar();
                }
            }
        };
        workerDatos.execute();
    }

    private void limpiarPanelValidacion() {
        idDocumentoValidacionSeleccionado = null;
        idExpedienteValidacionSeleccionado = null;
        lblPanelValidacionTitulo.setText("Panel de Validación");
        btnRegistrarValidacion.setEnabled(false);
        documentosValidacionTreePanel.setDocumentos(null, new ArrayList<DocumentoAnalizadoDTO>());
        datosValidacionNotif.limpiar();
        txtComentarioValidacion.setText("");
        if (cmbResultadoValidacion.getItemCount() > 0) {
            cmbResultadoValidacion.setSelectedIndex(0);
        }
        cerrarPanelValidacionNotif();
    }

    private void registrarValidacion() {
        if (idDocumentoValidacionSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Haga doble clic en un documento de la bandeja de validación.",
                    "Registrar Validación", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        SimpleItem resultadoItem = (SimpleItem) cmbResultadoValidacion.getSelectedItem();
        if (resultadoItem == null) {
            JOptionPane.showMessageDialog(this, "Seleccione el resultado de la validación.",
                    "Registrar Validación", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        final String comentario = txtComentarioValidacion.getText();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Se registrará el resultado de validación del documento seleccionado. ¿Desea continuar?",
                "Registrar Validación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        final Long idDocumento = idDocumentoValidacionSeleccionado;
        final String resultadoCodigo = resultadoItem.codigo;
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                documentoAnalisisService.registrarResultadoValidacion(idDocumento, resultadoCodigo, comentario);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(
                            JPanelNotificacionV2.this,
                            "La validación se registró correctamente.",
                            "Registrar Validación",
                            JOptionPane.INFORMATION_MESSAGE);
                    limpiarPanelValidacion();
                    cargarBandejaValidacion();
                    cargarBandejaAsignacionNotificacion();
                } catch (Exception ex) {
                    mostrarError("No se pudo registrar la validación.", ex);
                }
            }
        };
        worker.execute();
    }

    private JPanel crearBandejaNotifV2() {
        tablaNotifBandeja.setRowHeight(32);
        tablaNotifBandeja.setAutoCreateRowSorter(false);
        tablaNotifBandeja.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaNotifBandeja.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablaNotifBandeja.getTableHeader().setReorderingAllowed(false);
        tablaNotifBandeja.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tablaNotifBandeja.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        tablaNotifBandeja.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        tablaNotifBandeja.setGridColor(AppV2Theme.BORDER);
        tablaNotifBandeja.setShowVerticalLines(false);
        AppV2TableColumnSizer.applyFriendlyDefaults(tablaNotifBandeja);
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_SEL).setMaxWidth(34);
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_SEL).setMinWidth(30);
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_SEL).setCellRenderer(new NotifSeleccionRenderer());
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_SEL).setCellEditor(new NotifSeleccionEditor());
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_EXPAND).setMaxWidth(46);
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_EXPAND).setMinWidth(40);
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_EXPAND).setCellRenderer(new NotifExpandirRenderer());
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_MODALIDAD).setCellEditor(
                new NotifComboCellEditor(crearComboModalidad()));
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_FECHA_ENVIO).setCellEditor(new NotifFechaCellEditor());
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_ESTADO).setCellEditor(
                new NotifComboCellEditor(crearComboEstadoHija()));
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_FECHA_RECEPCION).setCellEditor(new NotifFechaCellEditor());
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_ESTADO_NOTIF).setCellEditor(
                new NotifComboCellEditor(crearComboEstadoNotifHija()));
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_ACCION).setMaxWidth(76);
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_ACCION).setMinWidth(64);
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_ACCION).setCellRenderer(new NotifAccionRenderer());
        tablaNotifBandeja.getColumnModel().getColumn(COL_NOTIF_ACCION).setCellEditor(new NotifAccionEditor());
        tablaNotifBandeja.setDefaultRenderer(Object.class, new NotifBandejaRenderer());
        AppV2ColumnFilterSupport.install(
                "bandejaNotificacion",
                tablaNotifBandeja,
                tablaNotifBandejaPanel.getScrollPane(),
                tablaNotifBandejaPanel,
                () -> {
                    documentosNotifExpandidos.clear();
                    reconstruirFilasNotifBandeja();
                },
                COL_NOTIF_SEL, COL_NOTIF_EXPAND, COL_NOTIF_ACCION);
        notifBandejaModel.addTableModelListener(evento -> {
            if (evento.getColumn() != COL_NOTIF_SEL || evento.getType() != javax.swing.event.TableModelEvent.UPDATE) {
                return;
            }
            int fila = evento.getFirstRow();
            if (fila < 0 || fila >= filasNotifBandeja.size()) {
                return;
            }
            NotifFilaTabla filaTabla = filasNotifBandeja.get(fila);
            if (!filaTabla.esPadre()) {
                return;
            }
            boolean marcado = Boolean.TRUE.equals(notifBandejaModel.getValueAt(fila, COL_NOTIF_SEL));
            if (marcado) {
                documentosNotifSeleccionados.add(filaTabla.idDocumento);
            } else {
                documentosNotifSeleccionados.remove(filaTabla.idDocumento);
            }
            actualizarEstadoBotonAgregarIntento();
        });
        tablaNotifBandeja.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = tablaNotifBandeja.rowAtPoint(e.getPoint());
                int viewCol = tablaNotifBandeja.columnAtPoint(e.getPoint());
                if (viewRow < 0) {
                    return;
                }
                int modelRow = tablaNotifBandeja.convertRowIndexToModel(viewRow);
                if (modelRow < 0 || modelRow >= filasNotifBandeja.size()) {
                    return;
                }
                int modelCol = tablaNotifBandeja.convertColumnIndexToModel(viewCol);
                NotifFilaTabla fila = filasNotifBandeja.get(modelRow);
                if (modelCol == COL_NOTIF_SEL) {
                    return;
                }
                if (modelCol == COL_NOTIF_EXPAND && fila.esPadre()) {
                    alternarExpansionNotif(fila.idDocumento);
                    return;
                }
                idDocumentoNotifSeleccionado = fila.idDocumento;
                actualizarEstadoBotonAgregarIntento();
                seleccionarExpedienteDesdeDocumentoNotif(fila.idDocumento);
                if (e.getClickCount() == 2 && fila.esPadre()) {
                    panelNotificacionCerradoPorUsuario = false;
                    mostrarPanelLateralNotif(panelLateralNotifBandeja);
                    splitBandejasNotif.setSideVisible(true);
                }
            }
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        AppV2Theme.estilizarBotonPrimario(btnAgregarIntento);
        btnAgregarIntento.setToolTipText(
                "Marque una o varias filas de documentos (o selecciónelas con clic simple) para registrar el siguiente intento de notificación.");
        btnAgregarIntento.setEnabled(false);
        toolbar.add(btnAgregarIntento);
        btnAgregarIntento.addActionListener(e -> agregarIntentosInline());

        JPanel izquierda = new JPanel(new BorderLayout(6, 6));
        izquierda.setOpaque(false);
        izquierda.add(toolbar, BorderLayout.NORTH);
        AppV2TableSectionPanel section = new AppV2TableSectionPanel(tablaNotifBandejaPanel);
        section.setStatus(lblEstadoNotifBandeja);
        izquierda.add(section, BorderLayout.CENTER);
        return izquierda;
    }

    private void actualizarEstadoBotonAgregarIntento() {
        boolean habilitado = !documentosNotifSeleccionados.isEmpty();
        if (!habilitado && idDocumentoNotifSeleccionado != null) {
            habilitado = buscarDocumentoNotif(idDocumentoNotifSeleccionado) != null;
        }
        btnAgregarIntento.setEnabled(habilitado);
    }

    private com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO buscarDocumentoNotif(Long idDocumento) {
        if (idDocumento == null) {
            return null;
        }
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : documentosNotifBandeja) {
            if (idDocumento.equals(item.getIdDocumentoAnalizado())) {
                return item;
            }
        }
        return null;
    }

    private JComboBox<SimpleItem> crearComboModalidad() {
        JComboBox<SimpleItem> combo = new JComboBox<SimpleItem>();
        combo.addItem(new SimpleItem("VIRTUAL", "Virtual"));
        combo.addItem(new SimpleItem("PRESENCIAL", "Presencial"));
        return combo;
    }

    private JComboBox<SimpleItem> crearComboEstadoHija() {
        JComboBox<SimpleItem> combo = new JComboBox<SimpleItem>();
        combo.addItem(new SimpleItem("PENDIENTE", "Pendiente"));
        combo.addItem(new SimpleItem("ENVIADA", "Enviado"));
        combo.addItem(new SimpleItem("EXITOSA", "Atendido"));
        return combo;
    }

    private JComboBox<SimpleItem> crearComboEstadoNotifHija() {
        JComboBox<SimpleItem> combo = new JComboBox<SimpleItem>();
        combo.addItem(new SimpleItem("", "(sin definir)"));
        combo.addItem(new SimpleItem("FALLIDA", "No ubicado"));
        combo.addItem(new SimpleItem("EXITOSA", "Ubicado"));
        return combo;
    }

    private void seleccionarExpedienteDesdeDocumentoNotif(Long idDocumento) {
        if (idDocumento == null) {
            return;
        }
        Long idExpediente = null;
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : documentosNotifBandeja) {
            if (idDocumento.equals(item.getIdDocumentoAnalizado())) {
                idExpediente = item.getIdExpediente();
                break;
            }
        }
        if (idExpediente == null) {
            return;
        }
        for (int modelRow = 0; modelRow < expedientesVisibles.size(); modelRow++) {
            if (idExpediente.equals(expedientesVisibles.get(modelRow).getIdExpediente())) {
                int viewRow = table.convertRowIndexToView(modelRow);
                if (viewRow >= 0) {
                    table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                }
                return;
            }
        }
        table.clearSelection();
    }

    private void cargarBandejaNotifV2() {
        lblEstadoNotifBandeja.setText("Cargando documentos pendientes de notificación...");
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> doInBackground() throws Exception {
                return documentoAnalisisService.listarDocumentosNotificacion();
            }

            @Override
            protected void done() {
                try {
                    List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> items = get();
                    documentosNotifBandeja.clear();
                    documentosNotifBandeja.addAll(items);
                    documentosNotifExpandidos.clear();
                    intentosNotifCache.clear();
                    reconstruirFilasNotifBandeja();
                    tablaNotifBandejaPanel.setEmpty(items.isEmpty());
                    lblEstadoNotifBandeja.setText(items.isEmpty()
                            ? "No hay documentos pendientes de notificación."
                            : items.size() + " documento(s) pendientes de notificación.");
                } catch (Exception ex) {
                    lblEstadoNotifBandeja.setText("No se pudieron cargar los documentos pendientes de notificación.");
                }
            }
        };
        worker.execute();
    }

    private void reconstruirFilasNotifBandeja() {
        filasNotifBandeja.clear();
        notifBandejaModel.setRowCount(0);
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : documentosNotifBandeja) {
            Long idDocumento = item.getIdDocumentoAnalizado();
            List<IntentoBorrador> borradores = borradoresNotifPorDocumento.get(idDocumento);
            int totalIntentosMostrar = item.getTotalIntentos() + (borradores == null ? 0 : borradores.size());
            boolean expandido = documentosNotifExpandidos.contains(idDocumento);
            filasNotifBandeja.add(NotifFilaTabla.padre(item));
            String iconoIntentos = totalIntentosMostrar <= 0
                    ? ""
                    : (expandido ? "collapse" : "expand") + ":" + totalIntentosMostrar;
            notifBandejaModel.addRow(new Object[]{
                Boolean.valueOf(documentosNotifSeleccionados.contains(idDocumento)),
                iconoIntentos,
                item.getNumeroExpediente(),
                item.getClasificacion().isEmpty() ? "-" : item.getClasificacion(),
                item.getTipoDocumento().isEmpty() ? "-" : item.getTipoDocumento(),
                item.getNumeroDocumento().isEmpty() ? "-" : item.getNumeroDocumento(),
                item.getFechaDocumento() == null ? "-" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(item.getFechaDocumento()),
                item.getTitular().isEmpty() ? "-" : item.getTitular(),
                item.getEstadoFinalNotificacion().isEmpty() ? "Por notificar" : item.getEstadoFinalNotificacion(),
                item.getEstadoDocumento().isEmpty() ? "-" : item.getEstadoDocumento(),
                ""
            });
            if (!expandido) {
                continue;
            }
            if (totalIntentosMostrar > 0) {
                filasNotifBandeja.add(NotifFilaTabla.subEncabezado(idDocumento));
                notifBandejaModel.addRow(filaSubEncabezadoIntentos());
            }
            List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> intentos = intentosNotifCache.get(idDocumento);
            if (intentos != null) {
                for (com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento : intentos) {
                    filasNotifBandeja.add(NotifFilaTabla.hijo(idDocumento, intento));
                    notifBandejaModel.addRow(filaHijoDesdeIntento(intento));
                }
            }
            if (borradores != null) {
                for (IntentoBorrador borrador : borradores) {
                    filasNotifBandeja.add(NotifFilaTabla.hijoBorrador(idDocumento, borrador));
                    notifBandejaModel.addRow(filaHijoDesdeBorrador(borrador));
                }
            }
        }
    }

    /**
     * Fila "sub-encabezado" no editable e insertada solo cuando el padre esta expandido y
     * tiene al menos un intento: rotula las columnas de la mini-grilla de intentos con sus
     * nombres reales del Excel (Modalidad/Fecha Envío/Estado/Código.../Fecha Acuse/Estado
     * Notificación), en vez de dejar que el usuario adivine el significado reutilizando los
     * encabezados del documento padre.
     */
    private Object[] filaSubEncabezadoIntentos() {
        return new Object[]{
            null,
            "",
            "Nro. Intento",
            "Modalidad",
            "Fecha Envío",
            "Estado",
            "Código/Usuario Notif.",
            "Fecha Acuse",
            "Estado Notificación",
            "",
            ""
        };
    }

    private Object[] filaHijoDesdeIntento(com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento) {
        return new Object[]{
            null,
            "",
            "Intento " + intento.getNumeroIntento(),
            codigoModalidadColumna(intento.getTipoNotificacionCodigo()),
            intento.getFechaEnvio() == null ? "-" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(intento.getFechaEnvio()),
            codigoEstadoParaColumnaEstado(intento.getEstadoNotificacionCodigo()),
            intento.getCodigoNotificacion(),
            intento.getFechaRecepcion() == null ? "-" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(intento.getFechaRecepcion()),
            codigoEstadoNotifParaColumna(intento.getEstadoNotificacionCodigo()),
            "",
            "guardar"
        };
    }

    private Object[] filaHijoDesdeBorrador(IntentoBorrador borrador) {
        return new Object[]{
            null,
            "",
            "Intento " + borrador.numeroIntento + " (nuevo)",
            borrador.modalidadCodigo,
            "-",
            "PENDIENTE",
            "",
            "-",
            "",
            "",
            "guardar-borrador"
        };
    }

    private static String codigoEstadoParaColumnaEstado(String estadoNotificacionCodigo) {
        String c = estadoNotificacionCodigo == null ? "" : estadoNotificacionCodigo.trim().toUpperCase();
        if ("EXITOSA".equals(c)) {
            return "EXITOSA";
        }
        if ("PENDIENTE".equals(c)) {
            return "PENDIENTE";
        }
        return "ENVIADA";
    }

    private static String codigoEstadoNotifParaColumna(String estadoNotificacionCodigo) {
        String c = estadoNotificacionCodigo == null ? "" : estadoNotificacionCodigo.trim().toUpperCase();
        if ("EXITOSA".equals(c)) {
            return "EXITOSA";
        }
        if ("FALLIDA".equals(c)) {
            return "FALLIDA";
        }
        return "";
    }

    /** Colapsa el codigo real del catalogo (VIRTUAL/PRESENCIAL_1/PRESENCIAL_2) al codigo que maneja el combo (VIRTUAL/PRESENCIAL). */
    private static String codigoModalidadColumna(String tipoNotificacionCodigo) {
        String c = tipoNotificacionCodigo == null ? "" : tipoNotificacionCodigo.trim().toUpperCase();
        return "VIRTUAL".equals(c) ? "VIRTUAL" : "PRESENCIAL";
    }

    /** Expande la seleccion del combo (VIRTUAL/PRESENCIAL) al codigo real de catalogo segun el numero de intento. */
    private static String codigoModalidadParaGuardar(String seleccionCombo, int numeroIntento) {
        if ("VIRTUAL".equalsIgnoreCase(seleccionCombo)) {
            return "VIRTUAL";
        }
        return numeroIntento >= 3 ? "PRESENCIAL_2" : "PRESENCIAL_1";
    }

    private static String textoModalidad(String codigo) {
        String c = codigo == null ? "" : codigo.trim().toUpperCase();
        if ("VIRTUAL".equals(c)) {
            return "Virtual";
        }
        return c.isEmpty() ? "-" : "Presencial";
    }

    private static String textoEstadoHija(String codigoColumnaEstado) {
        if ("EXITOSA".equals(codigoColumnaEstado)) {
            return "Atendido";
        }
        if ("ENVIADA".equals(codigoColumnaEstado)) {
            return "Enviado";
        }
        return "Pendiente";
    }

    private static String textoEstadoNotifHija(String codigoColumnaEstadoNotif) {
        if ("EXITOSA".equals(codigoColumnaEstadoNotif)) {
            return "Ubicado";
        }
        if ("FALLIDA".equals(codigoColumnaEstadoNotif)) {
            return "No ubicado";
        }
        return "";
    }

    private void alternarExpansionNotif(Long idDocumento) {
        if (idDocumento == null) {
            return;
        }
        if (documentosNotifExpandidos.contains(idDocumento)) {
            documentosNotifExpandidos.remove(idDocumento);
            reconstruirFilasNotifBandeja();
            return;
        }
        if (intentosNotifCache.containsKey(idDocumento)) {
            documentosNotifExpandidos.add(idDocumento);
            reconstruirFilasNotifBandeja();
            return;
        }
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> doInBackground() throws Exception {
                return documentoAnalisisService.listarIntentosNotificacion(idDocumento);
            }

            @Override
            protected void done() {
                try {
                    intentosNotifCache.put(idDocumento, get());
                    documentosNotifExpandidos.add(idDocumento);
                    reconstruirFilasNotifBandeja();
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los intentos de notificación.", ex);
                }
            }
        };
        worker.execute();
    }

    private void alternarExpansionNotifForzado(Long idDocumento) {
        documentosNotifExpandidos.remove(idDocumento);
        alternarExpansionNotif(idDocumento);
    }

    /**
     * "+ Agregar intento": ya no abre un dialogo. Por cada documento marcado (o el
     * seleccionado con clic simple si no hay ninguno marcado) inserta una fila hija
     * "borrador" editable directamente en la grilla, para elegir Modalidad y digitar el
     * Codigo/Usuario de notificacion antes de presionar Guardar en esa misma fila.
     */
    private void agregarIntentosInline() {
        final java.util.LinkedHashSet<Long> objetivos = new java.util.LinkedHashSet<Long>(documentosNotifSeleccionados);
        if (objetivos.isEmpty() && idDocumentoNotifSeleccionado != null) {
            objetivos.add(idDocumentoNotifSeleccionado);
        }
        if (objetivos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione uno o varios documentos de la bandeja de notificación.",
                    "Agregar intento", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        final List<Long> pendientesDeCarga = new ArrayList<Long>();
        for (Long idDocumento : objetivos) {
            com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO documento = buscarDocumentoNotif(idDocumento);
            if (documento != null && documento.getTotalIntentos() > 0 && !intentosNotifCache.containsKey(idDocumento)) {
                pendientesDeCarga.add(idDocumento);
            }
        }
        if (pendientesDeCarga.isEmpty()) {
            crearBorradoresIntento(objetivos);
            return;
        }
        final java.util.concurrent.atomic.AtomicInteger restantes =
                new java.util.concurrent.atomic.AtomicInteger(pendientesDeCarga.size());
        for (final Long idDocumento : pendientesDeCarga) {
            SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>, Void> worker =
                    new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>, Void>() {
                @Override
                protected List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> doInBackground() throws Exception {
                    return documentoAnalisisService.listarIntentosNotificacion(idDocumento);
                }

                @Override
                protected void done() {
                    try {
                        intentosNotifCache.put(idDocumento, get());
                    } catch (Exception ex) {
                        intentosNotifCache.put(idDocumento, new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>());
                    }
                    if (restantes.decrementAndGet() == 0) {
                        crearBorradoresIntento(objetivos);
                    }
                }
            };
            worker.execute();
        }
    }

    private void crearBorradoresIntento(java.util.Collection<Long> idsDocumento) {
        int agregados = 0;
        for (Long idDocumento : idsDocumento) {
            com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO documento = buscarDocumentoNotif(idDocumento);
            if (documento == null) {
                continue;
            }
            List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> intentosActuales = intentosNotifCache.get(idDocumento);
            List<IntentoBorrador> borradores = borradoresNotifPorDocumento.get(idDocumento);
            int totalActual = (intentosActuales != null ? intentosActuales.size() : 0) + (borradores != null ? borradores.size() : 0);
            int siguienteIntento = totalActual + 1;
            if (siguienteIntento > 3) {
                continue;
            }
            String modalidadPorDefecto = siguienteIntento == 1 ? "VIRTUAL" : "PRESENCIAL";
            if (borradores == null) {
                borradores = new ArrayList<IntentoBorrador>();
                borradoresNotifPorDocumento.put(idDocumento, borradores);
            }
            borradores.add(new IntentoBorrador(
                    secuenciaBorradorIntento.getAndDecrement(), documento.getIdExpediente(), idDocumento,
                    siguienteIntento, modalidadPorDefecto));
            documentosNotifExpandidos.add(idDocumento);
            agregados++;
        }
        if (agregados == 0) {
            JOptionPane.showMessageDialog(this,
                    "Los documentos seleccionados ya alcanzaron el máximo de 3 intentos de notificación.",
                    "Agregar intento", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        reconstruirFilasNotifBandeja();
    }

    private void cancelarBorradorIntento(int modelRow) {
        if (modelRow < 0 || modelRow >= filasNotifBandeja.size()) {
            return;
        }
        NotifFilaTabla fila = filasNotifBandeja.get(modelRow);
        if (!fila.esBorrador()) {
            return;
        }
        List<IntentoBorrador> lista = borradoresNotifPorDocumento.get(fila.idDocumento);
        if (lista != null) {
            lista.remove(fila.borrador);
            if (lista.isEmpty()) {
                borradoresNotifPorDocumento.remove(fila.idDocumento);
            }
        }
        reconstruirFilasNotifBandeja();
    }

    /** Baja logica de un intento ya guardado (no un borrador), con confirmacion previa. */
    private void eliminarFilaIntento(int modelRow) {
        if (modelRow < 0 || modelRow >= filasNotifBandeja.size()) {
            return;
        }
        NotifFilaTabla fila = filasNotifBandeja.get(modelRow);
        if (fila.esPadre() || fila.esBorrador() || fila.intento == null) {
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(
                this,
                "¿Desea eliminar el intento " + fila.intento.getNumeroIntento() + " de notificación?",
                "Eliminar intento",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }
        final Long idExpedienteNotificacion = fila.intento.getIdExpedienteNotificacion();
        final Long idDocumento = fila.idDocumento;
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                documentoAnalisisService.darBajaIntentoNotificacion(idExpedienteNotificacion);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    recargarBandejaYExpandir(idDocumento);
                } catch (Exception ex) {
                    mostrarError("No se pudo eliminar el intento de notificación.", ex);
                }
            }
        };
        worker.execute();
    }

    /**
     * Guarda la edicion inline de una fila hija (borrador nuevo o intento existente).
     * En existentes, si "Estado Notificación" quedo en UBICADO se confirma la recepcion
     * (fecha_recepcion/recibido_por en expediente_cargo_acuse) en vez de un simple update.
     */
    private void guardarFilaIntento(int modelRow) {
        if (modelRow < 0 || modelRow >= filasNotifBandeja.size()) {
            return;
        }
        NotifFilaTabla fila = filasNotifBandeja.get(modelRow);
        if (fila.esPadre()) {
            return;
        }
        Object modalidadValor = notifBandejaModel.getValueAt(modelRow, COL_NOTIF_MODALIDAD);
        Object codigoValor = notifBandejaModel.getValueAt(modelRow, COL_NOTIF_CODIGO);
        String modalidadSeleccion = modalidadValor == null ? "VIRTUAL" : modalidadValor.toString();
        final String codigoTexto = codigoValor == null ? "" : codigoValor.toString();
        final LocalDate fechaEnvio = parseFechaCeldaNotif(notifBandejaModel.getValueAt(modelRow, COL_NOTIF_FECHA_ENVIO));
        final LocalDate fechaRecepcion = parseFechaCeldaNotif(notifBandejaModel.getValueAt(modelRow, COL_NOTIF_FECHA_RECEPCION));

        if (fila.esBorrador()) {
            final IntentoBorrador borrador = fila.borrador;
            final Long idDocumento = fila.idDocumento;
            final String modalidadCodigo = codigoModalidadParaGuardar(modalidadSeleccion, borrador.numeroIntento);
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    documentoAnalisisService.registrarIntentoNotificacion(
                            borrador.idExpediente, idDocumento, modalidadCodigo, codigoTexto, fechaEnvio);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        List<IntentoBorrador> lista = borradoresNotifPorDocumento.get(idDocumento);
                        if (lista != null) {
                            lista.remove(borrador);
                            if (lista.isEmpty()) {
                                borradoresNotifPorDocumento.remove(idDocumento);
                            }
                        }
                        recargarBandejaYExpandir(idDocumento);
                    } catch (Exception ex) {
                        mostrarError("No se pudo registrar el intento de notificación.", ex);
                    }
                }
            };
            worker.execute();
            return;
        }

        Object estadoValor = notifBandejaModel.getValueAt(modelRow, COL_NOTIF_ESTADO);
        Object estadoNotifValor = notifBandejaModel.getValueAt(modelRow, COL_NOTIF_ESTADO_NOTIF);
        String estadoColumna = estadoValor == null ? "PENDIENTE" : estadoValor.toString();
        String estadoNotifColumna = estadoNotifValor == null ? "" : estadoNotifValor.toString();
        final String estadoFinalCodigo;
        if ("EXITOSA".equals(estadoColumna) || "EXITOSA".equals(estadoNotifColumna)) {
            estadoFinalCodigo = "EXITOSA";
        } else if ("FALLIDA".equals(estadoNotifColumna)) {
            estadoFinalCodigo = "FALLIDA";
        } else {
            estadoFinalCodigo = "ENVIADA".equals(estadoColumna) ? "ENVIADA" : "PENDIENTE";
        }
        final com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento = fila.intento;
        final Long idDocumento = fila.idDocumento;
        final String modalidadCodigo = codigoModalidadParaGuardar(modalidadSeleccion, intento.getNumeroIntento());
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if ("EXITOSA".equals(estadoFinalCodigo)) {
                    documentoAnalisisService.confirmarRecepcionIntentoNotificacion(
                            intento.getIdExpediente(), intento.getIdExpedienteNotificacion(),
                            intento.getIdDocumentoAnalizado(), codigoTexto, fechaEnvio, fechaRecepcion);
                } else {
                    documentoAnalisisService.actualizarIntentoNotificacion(
                            intento.getIdExpedienteNotificacion(), modalidadCodigo, estadoFinalCodigo, codigoTexto,
                            fechaEnvio, null);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    recargarBandejaYExpandir(idDocumento);
                } catch (Exception ex) {
                    mostrarError("No se pudo actualizar el intento de notificación.", ex);
                }
            }
        };
        worker.execute();
    }

    private static LocalDate parseFechaCeldaNotif(Object valor) {
        String texto = valor == null ? "" : valor.toString().trim();
        if (texto.isEmpty() || "-".equals(texto)) {
            return null;
        }
        try {
            return LocalDate.parse(texto, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /** Recarga la bandeja completa (Estado Final depende del servidor) y re-expande el documento editado. */
    private void recargarBandejaYExpandir(final Long idDocumento) {
        lblEstadoNotifBandeja.setText("Actualizando bandeja de notificación...");
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void>() {
            private List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> intentosDocumento;

            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> doInBackground() throws Exception {
                List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> items =
                        documentoAnalisisService.listarDocumentosNotificacion();
                if (idDocumento != null) {
                    intentosDocumento = documentoAnalisisService.listarIntentosNotificacion(idDocumento);
                }
                return items;
            }

            @Override
            protected void done() {
                try {
                    List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> items = get();
                    documentosNotifBandeja.clear();
                    documentosNotifBandeja.addAll(items);
                    intentosNotifCache.clear();
                    if (idDocumento != null) {
                        intentosNotifCache.put(idDocumento, intentosDocumento == null
                                ? new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>()
                                : intentosDocumento);
                        documentosNotifExpandidos.add(idDocumento);
                    }
                    reconstruirFilasNotifBandeja();
                    tablaNotifBandejaPanel.setEmpty(items.isEmpty());
                    lblEstadoNotifBandeja.setText(items.size() + " documento(s) pendientes de notificación.");
                } catch (Exception ex) {
                    lblEstadoNotifBandeja.setText("No se pudieron actualizar los documentos pendientes de notificación.");
                }
            }
        };
        worker.execute();
    }

    private static void seleccionarItemPorCodigo(JComboBox<SimpleItem> combo, String codigo) {
        String buscado = codigo == null ? "" : codigo.trim();
        for (int i = 0; i < combo.getItemCount(); i++) {
            SimpleItem item = combo.getItemAt(i);
            if (item != null && item.getCodigo().equalsIgnoreCase(buscado)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    private static class NotifFilaTabla {
        private final boolean padre;
        private final boolean subEncabezado;
        private final Long idDocumento;
        private final com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento;
        private final IntentoBorrador borrador;

        private NotifFilaTabla(
                boolean padre,
                boolean subEncabezado,
                Long idDocumento,
                com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento,
                IntentoBorrador borrador) {
            this.padre = padre;
            this.subEncabezado = subEncabezado;
            this.idDocumento = idDocumento;
            this.intento = intento;
            this.borrador = borrador;
        }

        private static NotifFilaTabla padre(com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item) {
            return new NotifFilaTabla(true, false, item.getIdDocumentoAnalizado(), null, null);
        }

        private static NotifFilaTabla subEncabezado(Long idDocumento) {
            return new NotifFilaTabla(false, true, idDocumento, null, null);
        }

        private static NotifFilaTabla hijo(Long idDocumento, com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento) {
            return new NotifFilaTabla(false, false, idDocumento, intento, null);
        }

        private static NotifFilaTabla hijoBorrador(Long idDocumento, IntentoBorrador borrador) {
            return new NotifFilaTabla(false, false, idDocumento, null, borrador);
        }

        private boolean esPadre() {
            return padre;
        }

        private boolean esSubEncabezado() {
            return subEncabezado;
        }

        private boolean esBorrador() {
            return borrador != null;
        }
    }

    /**
     * Insignia de intentos de notificacion: chevron dentro de un cuadrado redondeado
     * (en vez del circulo con +/- que usan las bandejas de expedientes asociados/
     * duplicados), con el numero de intentos registrados como sello. No dibuja nada
     * si el documento aun no tiene intentos (estado NONE).
     */
    private static final class NotifIntentoGlyph extends JPanel {
        static final int NONE = 0;
        static final int EXPAND = 1;
        static final int COLLAPSE = 2;

        private int state = NONE;
        private int totalIntentos;
        private Color accent = AppV2Theme.TEAL;

        private NotifIntentoGlyph() {
            setOpaque(true);
            setPreferredSize(new Dimension(40, 28));
        }

        private void configure(int state, int totalIntentos, Color accent, Color background) {
            this.state = state;
            this.totalIntentos = totalIntentos;
            this.accent = accent == null ? AppV2Theme.TEAL : accent;
            setBackground(background);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (state == NONE) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cy = getHeight() / 2;
                int size = 18;
                int x = 6;
                int y = cy - size / 2;
                Color fill = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30);
                g2.setColor(fill);
                g2.fillRoundRect(x, y, size, size, 7, 7);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawRoundRect(x, y, size, size, 7, 7);
                int cx = x + size / 2;
                int pad = 4;
                if (state == COLLAPSE) {
                    g2.drawPolyline(
                            new int[]{cx - pad, cx, cx + pad},
                            new int[]{cy - 3, cy + 2, cy - 3}, 3);
                } else {
                    g2.drawPolyline(
                            new int[]{cx - 3, cx + 2, cx - 3},
                            new int[]{cy - pad, cy, cy + pad}, 3);
                }
                if (totalIntentos > 0) {
                    g2.setFont(AppV2Theme.fontBold(9));
                    FontMetrics fm = g2.getFontMetrics();
                    String texto = String.valueOf(totalIntentos);
                    g2.drawString(texto, x + size + 4, cy + fm.getAscent() / 2 - 1);
                }
            } finally {
                g2.dispose();
            }
        }
    }

    private class NotifExpandirRenderer extends JPanel implements TableCellRenderer {
        private final NotifIntentoGlyph glyph = new NotifIntentoGlyph();

        private NotifExpandirRenderer() {
            setOpaque(true);
            setLayout(new BorderLayout());
            add(glyph, BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Color background = isSelected ? new Color(219, 244, 249) : (row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
            setBackground(background);
            String texto = value == null ? "" : value.toString();
            int totalIntentos = 0;
            int separador = texto.indexOf(':');
            String estado = separador >= 0 ? texto.substring(0, separador) : texto;
            if (separador >= 0) {
                try {
                    totalIntentos = Integer.parseInt(texto.substring(separador + 1));
                } catch (NumberFormatException ignored) {
                    totalIntentos = 0;
                }
            }
            if ("expand".equals(estado)) {
                glyph.configure(NotifIntentoGlyph.EXPAND, totalIntentos, AppV2Theme.TEAL, background);
            } else if ("collapse".equals(estado)) {
                glyph.configure(NotifIntentoGlyph.COLLAPSE, totalIntentos, AppV2Theme.TEAL, background);
            } else {
                glyph.configure(NotifIntentoGlyph.NONE, 0, AppV2Theme.TEAL, background);
            }
            return this;
        }
    }

    private class NotifBandejaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            int modelRow = table.convertRowIndexToModel(row);
            int modelCol = table.convertColumnIndexToModel(column);
            NotifFilaTabla fila = modelRow >= 0 && modelRow < filasNotifBandeja.size() ? filasNotifBandeja.get(modelRow) : null;
            boolean esSubEncabezado = fila != null && fila.esSubEncabezado();
            boolean esHijo = fila != null && !fila.esPadre() && !esSubEncabezado;
            Object valorMostrado = value;
            if (esHijo) {
                String texto = value == null ? "" : value.toString();
                if (modelCol == COL_NOTIF_MODALIDAD) {
                    valorMostrado = textoModalidad(texto);
                } else if (modelCol == COL_NOTIF_ESTADO) {
                    valorMostrado = textoEstadoHija(texto);
                } else if (modelCol == COL_NOTIF_ESTADO_NOTIF) {
                    valorMostrado = textoEstadoNotifHija(texto);
                } else if (modelCol == COL_NOTIF_CODIGO && texto.isEmpty()) {
                    valorMostrado = "-";
                }
            }
            Component c = super.getTableCellRendererComponent(table, valorMostrado, isSelected, hasFocus, row, column);
            setFont(esSubEncabezado
                    ? AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL - 1)
                    : (esHijo ? AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL) : AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE)));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (!isSelected) {
                if (esSubEncabezado) {
                    setBackground(new Color(224, 238, 241));
                    setForeground(AppV2Theme.TEAL.darker());
                } else {
                    setBackground(esHijo ? new Color(238, 250, 252) : (row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT));
                    setForeground(esHijo ? AppV2Theme.TEXT_SECONDARY : AppV2Theme.TEXT_PRIMARY);
                }
            }
            return c;
        }
    }

    private class NotifSeleccionRenderer extends JPanel implements TableCellRenderer {
        private final JCheckBox checkBox = new JCheckBox();

        private NotifSeleccionRenderer() {
            setOpaque(true);
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            checkBox.setOpaque(false);
            add(checkBox);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Color background = isSelected ? new Color(219, 244, 249) : (row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
            setBackground(background);
            int modelRow = table.convertRowIndexToModel(row);
            boolean esPadre = modelRow >= 0 && modelRow < filasNotifBandeja.size() && filasNotifBandeja.get(modelRow).esPadre();
            checkBox.setVisible(esPadre);
            checkBox.setSelected(Boolean.TRUE.equals(value));
            return this;
        }
    }

    private class NotifSeleccionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JCheckBox checkBox = new JCheckBox();

        private NotifSeleccionEditor() {
            checkBox.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Object getCellEditorValue() {
            return Boolean.valueOf(checkBox.isSelected());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            checkBox.setSelected(Boolean.TRUE.equals(value));
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            panel.setOpaque(true);
            panel.setBackground(table.getSelectionBackground());
            panel.add(checkBox);
            return panel;
        }
    }

    private class NotifComboCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JComboBox<SimpleItem> combo;

        private NotifComboCellEditor(JComboBox<SimpleItem> combo) {
            this.combo = combo;
        }

        @Override
        public Object getCellEditorValue() {
            SimpleItem item = (SimpleItem) combo.getSelectedItem();
            return item == null ? "" : item.getCodigo();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            seleccionarItemPorCodigo(combo, value == null ? "" : value.toString());
            return combo;
        }
    }

    /** Editor de fecha inline para Fecha Envío/Fecha Acuse, mismo patrón que documentos analizados. */
    private static class NotifFechaCellEditor extends AbstractCellEditor implements TableCellEditor {
        private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private final PremiumDateFieldV2 field = new PremiumDateFieldV2();

        private NotifFechaCellEditor() {
            field.setPreferredSize(new Dimension(130, 28));
        }

        @Override
        public Object getCellEditorValue() {
            Date fecha = field.getDate();
            if (fecha == null) {
                return "-";
            }
            LocalDate local = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return FORMATO.format(local);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            LocalDate local = parseFechaCeldaNotif(value);
            field.setDate(local == null ? null : Date.from(local.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            return field;
        }
    }

    private class NotifAccionRenderer extends JPanel implements TableCellRenderer {
        private final JButton btnGuardar = crearBotonAccionNotif(new NotifSaveIcon(), "Guardar intento");
        private final JButton btnCancelar = crearBotonAccionNotif(new NotifCancelIcon(), "Descartar intento sin guardar");
        private final JButton btnEliminar = crearBotonAccionNotif(new NotifDeleteIcon(), "Eliminar intento");

        private NotifAccionRenderer() {
            setOpaque(true);
            setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));
            add(btnGuardar);
            add(btnCancelar);
            add(btnEliminar);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? new Color(219, 244, 249) : (row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT));
            boolean esBorrador = "guardar-borrador".equals(value);
            boolean esPersistido = "guardar".equals(value);
            btnGuardar.setVisible(esBorrador || esPersistido);
            btnCancelar.setVisible(esBorrador);
            btnEliminar.setVisible(esPersistido);
            return this;
        }
    }

    private class NotifAccionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        private final JButton btnGuardar = crearBotonAccionNotif(new NotifSaveIcon(), "Guardar intento");
        private final JButton btnCancelar = crearBotonAccionNotif(new NotifCancelIcon(), "Descartar intento sin guardar");
        private final JButton btnEliminar = crearBotonAccionNotif(new NotifDeleteIcon(), "Eliminar intento");
        private int editingRow = -1;

        private NotifAccionEditor() {
            panel.setOpaque(true);
            panel.add(btnGuardar);
            panel.add(btnCancelar);
            panel.add(btnEliminar);
            btnGuardar.addActionListener(e -> {
                int fila = editingRow;
                fireEditingStopped();
                guardarFilaIntento(fila);
            });
            btnCancelar.addActionListener(e -> {
                int fila = editingRow;
                fireEditingStopped();
                cancelarBorradorIntento(fila);
            });
            btnEliminar.addActionListener(e -> {
                int fila = editingRow;
                fireEditingStopped();
                eliminarFilaIntento(fila);
            });
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = table.convertRowIndexToModel(row);
            boolean esBorrador = "guardar-borrador".equals(value);
            boolean esPersistido = "guardar".equals(value);
            btnGuardar.setVisible(esBorrador || esPersistido);
            btnCancelar.setVisible(esBorrador);
            btnEliminar.setVisible(esPersistido);
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
    }

    private static JButton crearBotonAccionNotif(Icon icon, String tooltip) {
        JButton boton = new JButton();
        boton.setText("");
        boton.setIcon(icon);
        boton.setToolTipText(tooltip);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        return boton;
    }

    private static class NotifSaveIcon implements Icon {
        private static final int SIZE = 16;

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
                g2.setColor(new Color(238, 247, 252));
                g2.fillRoundRect(x + 1, y + 1, 14, 14, 4, 4);
                g2.setColor(stroke);
                g2.drawRoundRect(x + 1, y + 1, 14, 14, 4, 4);
                g2.fillRect(x + 4, y + 2, 7, 4);
                g2.setColor(Color.WHITE);
                g2.fillRect(x + 5, y + 3, 4, 2);
                g2.setColor(stroke);
                g2.fillRoundRect(x + 4, y + 9, 8, 5, 2, 2);
                g2.setColor(Color.WHITE);
                g2.drawLine(x + 6, y + 11, x + 10, y + 11);
            } finally {
                g2.dispose();
            }
        }
    }

    private static class NotifCancelIcon implements Icon {
        private static final int SIZE = 16;

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
                g2.setColor(new Color(196, 60, 60));
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 4, y + 4, x + 12, y + 12);
                g2.drawLine(x + 12, y + 4, x + 4, y + 12);
            } finally {
                g2.dispose();
            }
        }
    }

    private static class NotifDeleteIcon implements Icon {
        private static final int SIZE = 16;

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
                Color stroke = new Color(196, 60, 60);
                g2.setColor(stroke);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 4, y + 5, x + 12, y + 5);
                g2.drawLine(x + 6, y + 5, x + 6, y + 3);
                g2.drawLine(x + 10, y + 5, x + 10, y + 3);
                g2.drawLine(x + 6, y + 3, x + 10, y + 3);
                g2.drawLine(x + 5, y + 5, x + 5, y + 13);
                g2.drawLine(x + 11, y + 5, x + 11, y + 13);
                g2.drawLine(x + 5, y + 13, x + 11, y + 13);
                g2.drawLine(x + 8, y + 7, x + 8, y + 11);
            } finally {
                g2.dispose();
            }
        }
    }

    // ===================== Bandeja Publicación: construcción y logica =====================

    private JPanel crearBandejaPublicacionNotif() {
        configurarTablaPublicacionNotif();
        configurarFiltrosPublicacionNotif();

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        AppV2Theme.estilizarBotonPrimario(btnAgregarPublicacion);
        btnAgregarPublicacion.setToolTipText(
                "Marque una o varias filas de documentos (o selecciónelas con clic simple) para registrar la publicación.");
        btnAgregarPublicacion.setEnabled(false);
        toolbar.add(btnAgregarPublicacion);
        btnAgregarPublicacion.addActionListener(e -> agregarPublicacionesInline());

        JPanel superior = new JPanel(new BorderLayout(4, 4));
        superior.setOpaque(false);
        superior.add(crearHeaderPublicacionNotif(), BorderLayout.NORTH);
        superior.add(crearBuscadorPublicacionNotif(), BorderLayout.CENTER);

        JPanel contenidoTabla = new JPanel(new BorderLayout(6, 6));
        contenidoTabla.setOpaque(false);
        contenidoTabla.add(toolbar, BorderLayout.NORTH);
        AppV2TableSectionPanel section = new AppV2TableSectionPanel(tablaPublicacionBandejaPanel);
        section.setStatus(lblEstadoPublicacionBandeja);
        contenidoTabla.add(section, BorderLayout.CENTER);

        JPanel izquierda = new JPanel(new BorderLayout(6, 6));
        izquierda.setOpaque(false);
        izquierda.add(superior, BorderLayout.NORTH);
        izquierda.add(contenidoTabla, BorderLayout.CENTER);

        panelLateralPublicacionNotif = crearPanelDetallePublicacionNotif();
        return izquierda;
    }

    private JPanel crearHeaderPublicacionNotif() {
        JPanel metricas = new AppV2ResponsiveGridPanel(190, 4, 12, 10);
        metricas.add(cardPubTotal);
        metricas.add(cardPubPendientes);
        metricas.add(cardPubRegistradas);
        metricas.add(cardPubVencidos);
        return metricas;
    }

    private JPanel crearBuscadorPublicacionNotif() {
        JPanel acciones = AppV2ActionPanel.right();
        acciones.add(btnBuscarPublicacion);
        acciones.add(btnLimpiarPublicacion);
        acciones.add(btnRefrescarPublicacion);
        return AppV2ExpedientePanelFactory.crearPanelBusquedaEstiloRegistro(
                "Búsqueda",
                txtBusquedaPublicacion,
                acciones,
                fechaEmisionDesdePublicacion,
                fechaEmisionHastaPublicacion,
                cmbEstadoPublicacion,
                null,
                spnLimitePublicacion);
    }

    private void configurarFiltrosPublicacionNotif() {
        cmbEstadoPublicacion.removeAllItems();
        cmbEstadoPublicacion.addItem(new SimpleItem("", "Todos los estados"));
        cmbEstadoPublicacion.addItem(new SimpleItem("EMITIDO", "Emitido"));
        AppV2Theme.estilizarBotonPrimario(btnBuscarPublicacion);
        cardPubPendientes.setOnClick(() -> activarKpiPublicacion(FiltroKpiPublicacion.SIN_PUBLICACION));
        cardPubRegistradas.setOnClick(() -> activarKpiPublicacion(FiltroKpiPublicacion.CON_PUBLICACION));
        cardPubVencidos.setOnClick(() -> activarKpiPublicacion(FiltroKpiPublicacion.VENCIDOS));
        btnBuscarPublicacion.addActionListener(e -> aplicarFiltrosPublicacionNotif());
        txtBusquedaPublicacion.addActionListener(e -> aplicarFiltrosPublicacionNotif());
        btnLimpiarPublicacion.addActionListener(e -> limpiarFiltrosPublicacionNotif());
        btnRefrescarPublicacion.addActionListener(e -> cargarBandejaPublicacionNotif());
        restaurarFechasPublicacionNotif();
    }

    private void restaurarFechasPublicacionNotif() {
        fechaEmisionDesdePublicacion.setDate(DateRangePickerSupport.defaultSearchFromDateCurrentMonth());
        fechaEmisionHastaPublicacion.setDate(DateRangePickerSupport.defaultSearchToDate());
    }

    private void limpiarFiltrosPublicacionNotif() {
        txtBusquedaPublicacion.setText("");
        restaurarFechasPublicacionNotif();
        cmbEstadoPublicacion.setSelectedIndex(0);
        spnLimitePublicacion.setValue(200);
        kpiActivoPublicacion = FiltroKpiPublicacion.TODOS;
        marcarKpisPublicacion();
        aplicarFiltrosPublicacionNotif();
    }

    private void activarKpiPublicacion(FiltroKpiPublicacion filtro) {
        kpiActivoPublicacion = kpiActivoPublicacion == filtro ? FiltroKpiPublicacion.TODOS : filtro;
        marcarKpisPublicacion();
        aplicarFiltrosPublicacionNotif();
    }

    private void marcarKpisPublicacion() {
        cardPubPendientes.setSelected(kpiActivoPublicacion == FiltroKpiPublicacion.SIN_PUBLICACION);
        cardPubRegistradas.setSelected(kpiActivoPublicacion == FiltroKpiPublicacion.CON_PUBLICACION);
        cardPubVencidos.setSelected(kpiActivoPublicacion == FiltroKpiPublicacion.VENCIDOS);
    }

    private void actualizarMetricasPublicacionNotif() {
        int total = documentosPublicacionBandeja.size();
        int pendientes = 0;
        int registradas = 0;
        int vencidos = 0;
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : documentosPublicacionBandeja) {
            if (item.getTotalIntentos() >= 3) {
                registradas++;
            } else {
                pendientes++;
            }
            Long dias = item.getDiasRestantes();
            if (dias != null && dias.longValue() < 0) {
                vencidos++;
            }
        }
        cardPubTotal.setValue(String.valueOf(total));
        cardPubPendientes.setValue(String.valueOf(pendientes));
        cardPubRegistradas.setValue(String.valueOf(registradas));
        cardPubVencidos.setValue(String.valueOf(vencidos));
    }

    private boolean coincideKpiPublicacion(com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item) {
        switch (kpiActivoPublicacion) {
            case SIN_PUBLICACION:
                return item.getTotalIntentos() < 3;
            case CON_PUBLICACION:
                return item.getTotalIntentos() >= 3;
            case VENCIDOS:
                return item.getDiasRestantes() != null && item.getDiasRestantes().longValue() < 0;
            case TODOS:
            default:
                return true;
        }
    }

    private boolean coincideTextoPublicacion(com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item, String texto) {
        return contieneTextoAsigNotif(item.getNumeroExpediente(), texto)
                || contieneTextoAsigNotif(item.getNumeroExpedienteSgd(), texto)
                || contieneTextoAsigNotif(item.getTitular(), texto)
                || contieneTextoAsigNotif(item.getNumeroDocumento(), texto);
    }

    private List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> filtrarDocumentosPublicacionNotif() {
        String texto = txtBusquedaPublicacion.getText() == null ? "" : txtBusquedaPublicacion.getText().trim().toLowerCase();
        LocalDate desde = fechaSeleccionadaAsigNotif(fechaEmisionDesdePublicacion);
        LocalDate hasta = fechaSeleccionadaAsigNotif(fechaEmisionHastaPublicacion);
        SimpleItem estadoItem = (SimpleItem) cmbEstadoPublicacion.getSelectedItem();
        String estadoCodigo = estadoItem == null ? "" : estadoItem.getCodigo();
        int limite = (Integer) spnLimitePublicacion.getValue();
        List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> resultado =
                new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>();
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : documentosPublicacionBandeja) {
            if (!coincideKpiPublicacion(item)) {
                continue;
            }
            if (!texto.isEmpty() && !coincideTextoPublicacion(item, texto)) {
                continue;
            }
            if (item.getFechaDocumento() != null) {
                if (desde != null && item.getFechaDocumento().isBefore(desde)) {
                    continue;
                }
                if (hasta != null && item.getFechaDocumento().isAfter(hasta)) {
                    continue;
                }
            }
            if (!estadoCodigo.isEmpty() && !estadoCodigo.equalsIgnoreCase(item.getEstadoDocumentoCodigo())) {
                continue;
            }
            resultado.add(item);
            if (resultado.size() >= limite) {
                break;
            }
        }
        return resultado;
    }

    private void aplicarFiltrosPublicacionNotif() {
        documentoPublicacionFoco = null;
        idDocumentoPublicacionSeleccionado = null;
        documentosPublicacionSeleccionados.clear();
        reconstruirFilasPublicacionBandeja();
    }

    private void cargarBandejaPublicacionNotif() {
        lblEstadoPublicacionBandeja.setText("Cargando documentos pendientes de publicación...");
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> doInBackground() throws Exception {
                return documentoAnalisisService.listarDocumentosBandejaPublicacion();
            }

            @Override
            protected void done() {
                try {
                    List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> items = get();
                    documentosPublicacionBandeja.clear();
                    documentosPublicacionBandeja.addAll(items);
                    documentosPublicacionExpandidos.clear();
                    intentosPublicacionCache.clear();
                    borradoresPublicacionPorDocumento.clear();
                    actualizarMetricasPublicacionNotif();
                    reconstruirFilasPublicacionBandeja();
                } catch (Exception ex) {
                    documentosPublicacionBandeja.clear();
                    actualizarMetricasPublicacionNotif();
                    reconstruirFilasPublicacionBandeja();
                    lblEstadoPublicacionBandeja.setText("No se pudieron cargar los documentos pendientes de publicación.");
                }
            }
        };
        worker.execute();
    }

    private void reconstruirFilasPublicacionBandeja() {
        filasPublicacionBandeja.clear();
        publicacionBandejaModel.setRowCount(0);
        List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> visibles = filtrarDocumentosPublicacionNotif();
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : visibles) {
            Long idDocumento = item.getIdDocumentoAnalizado();
            PublicacionBorrador borrador = borradoresPublicacionPorDocumento.get(idDocumento);
            List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> intentos = intentosPublicacionCache.get(idDocumento);
            int totalMostrar = (intentos != null ? intentos.size() : item.getTotalIntentos()) + (borrador != null ? 1 : 0);
            boolean expandido = documentosPublicacionExpandidos.contains(idDocumento);
            filasPublicacionBandeja.add(PublicacionFilaTabla.padre(item));
            String iconoIntentos = totalMostrar <= 0
                    ? ""
                    : (expandido ? "collapse" : "expand") + ":" + totalMostrar;
            publicacionBandejaModel.addRow(new Object[]{
                Boolean.valueOf(documentosPublicacionSeleccionados.contains(idDocumento)),
                iconoIntentos,
                item.getNumeroExpediente(),
                item.getClasificacion().isEmpty() ? "-" : item.getClasificacion(),
                item.getTipoDocumento().isEmpty() ? "-" : item.getTipoDocumento(),
                item.getNumeroDocumento().isEmpty() ? "-" : item.getNumeroDocumento(),
                item.getFechaDocumento() == null ? "-" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(item.getFechaDocumento()),
                item.getTitular().isEmpty() ? "-" : item.getTitular(),
                item.getEstadoFinalNotificacion().isEmpty() ? "Por publicar" : item.getEstadoFinalNotificacion(),
                item.getEstadoDocumento().isEmpty() ? "-" : item.getEstadoDocumento(),
                ""
            });
            if (!expandido) {
                continue;
            }
            if (totalMostrar > 0) {
                filasPublicacionBandeja.add(PublicacionFilaTabla.subEncabezado(idDocumento));
                publicacionBandejaModel.addRow(filaSubEncabezadoPublicacion());
            }
            if (intentos != null) {
                for (com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento : intentos) {
                    filasPublicacionBandeja.add(PublicacionFilaTabla.hijo(idDocumento, intento));
                    publicacionBandejaModel.addRow(filaHijoPublicacionDesdeIntento(intento));
                }
            }
            if (borrador != null) {
                filasPublicacionBandeja.add(PublicacionFilaTabla.hijoBorrador(idDocumento, borrador));
                publicacionBandejaModel.addRow(filaHijoPublicacionDesdeBorrador());
            }
        }
        tablaPublicacionBandejaPanel.setEmpty(visibles.isEmpty());
        lblEstadoPublicacionBandeja.setText(visibles.isEmpty()
                ? "No hay documentos pendientes de publicación con los filtros aplicados."
                : visibles.size() + " documento(s) de " + documentosPublicacionBandeja.size() + " en total.");
        actualizarEstadoBotonAgregarPublicacion();
    }

    private Object[] filaSubEncabezadoPublicacion() {
        return new Object[]{null, "", "Registro", "Modalidad", "Fecha", "Estado", "Referencia / Medio", "", "", "", ""};
    }

    private Object[] filaHijoPublicacionDesdeIntento(com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento) {
        boolean esPublicacion = "PUBLICACION".equalsIgnoreCase(intento.getTipoNotificacionCodigo());
        String etiqueta = esPublicacion ? "Publicación" : "Intento " + intento.getNumeroIntento();
        String modalidad = esPublicacion
                ? "Publicación"
                : (intento.getTipoNotificacion() == null || intento.getTipoNotificacion().isEmpty() ? "-" : intento.getTipoNotificacion());
        String fecha = intento.getFechaEnvio() == null ? "-" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(intento.getFechaEnvio());
        String estado = esPublicacion
                ? codigoEstadoPublicacionParaColumna(intento.getEstadoNotificacionCodigo())
                : (intento.getEstadoNotificacion() == null || intento.getEstadoNotificacion().isEmpty() ? "-" : intento.getEstadoNotificacion());
        String referencia = intento.getCodigoNotificacion() == null || intento.getCodigoNotificacion().isEmpty()
                ? "-" : intento.getCodigoNotificacion();
        return new Object[]{
            null, "", etiqueta, modalidad, fecha, estado, referencia, "-", "-", "-",
            esPublicacion ? "guardar" : ""
        };
    }

    private Object[] filaHijoPublicacionDesdeBorrador() {
        return new Object[]{null, "", "Publicación (nueva)", "Publicación", "-", "PENDIENTE", "", "-", "-", "-", "guardar-borrador"};
    }

    private static String codigoEstadoPublicacionParaColumna(String estadoNotificacionCodigo) {
        String c = estadoNotificacionCodigo == null ? "" : estadoNotificacionCodigo.trim().toUpperCase();
        return "EXITOSA".equals(c) ? "EXITOSA" : "PENDIENTE";
    }

    private static String textoEstadoPublicacion(String codigoColumnaEstado) {
        return "EXITOSA".equals(codigoColumnaEstado) ? "Publicado" : "Pendiente";
    }

    private void alternarExpansionPublicacion(Long idDocumento) {
        if (idDocumento == null) {
            return;
        }
        if (documentosPublicacionExpandidos.contains(idDocumento)) {
            documentosPublicacionExpandidos.remove(idDocumento);
            reconstruirFilasPublicacionBandeja();
            return;
        }
        if (intentosPublicacionCache.containsKey(idDocumento)) {
            documentosPublicacionExpandidos.add(idDocumento);
            reconstruirFilasPublicacionBandeja();
            return;
        }
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> doInBackground() throws Exception {
                return documentoAnalisisService.listarIntentosNotificacion(idDocumento);
            }

            @Override
            protected void done() {
                try {
                    intentosPublicacionCache.put(idDocumento, get());
                    documentosPublicacionExpandidos.add(idDocumento);
                    reconstruirFilasPublicacionBandeja();
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los intentos de notificación.", ex);
                }
            }
        };
        worker.execute();
    }

    private com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO buscarDocumentoPublicacion(Long idDocumento) {
        if (idDocumento == null) {
            return null;
        }
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : documentosPublicacionBandeja) {
            if (idDocumento.equals(item.getIdDocumentoAnalizado())) {
                return item;
            }
        }
        return null;
    }

    private void actualizarEstadoBotonAgregarPublicacion() {
        boolean habilitado = !documentosPublicacionSeleccionados.isEmpty();
        if (!habilitado && idDocumentoPublicacionSeleccionado != null) {
            habilitado = buscarDocumentoPublicacion(idDocumentoPublicacionSeleccionado) != null;
        }
        btnAgregarPublicacion.setEnabled(habilitado);
    }

    /**
     * "+ Agregar publicación": mismo patron que "+ Agregar intento" de la Bandeja Notificación
     * (inserta una fila hija "borrador" editable en la grilla, en vez de abrir un dialogo), pero
     * sin combo de modalidad (siempre tipo_notificacion=PUBLICACION, ya sembrado por el script
     * 46_tipo_notificacion_publicacion.sql) y solo aplica una vez por documento (esta bandeja ya
     * garantiza que el documento agoto intento 1 y 2; un 3er intento ya registrado o en borrador
     * no permite agregar otro).
     */
    private void agregarPublicacionesInline() {
        final java.util.LinkedHashSet<Long> objetivos = new java.util.LinkedHashSet<Long>(documentosPublicacionSeleccionados);
        if (objetivos.isEmpty() && idDocumentoPublicacionSeleccionado != null) {
            objetivos.add(idDocumentoPublicacionSeleccionado);
        }
        if (objetivos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione uno o varios documentos de la bandeja de publicación.",
                    "Agregar publicación", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        final List<Long> pendientesDeCarga = new ArrayList<Long>();
        for (Long idDocumento : objetivos) {
            com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO documento = buscarDocumentoPublicacion(idDocumento);
            if (documento != null && documento.getTotalIntentos() > 0 && !intentosPublicacionCache.containsKey(idDocumento)) {
                pendientesDeCarga.add(idDocumento);
            }
        }
        if (pendientesDeCarga.isEmpty()) {
            crearBorradoresPublicacion(objetivos);
            return;
        }
        final java.util.concurrent.atomic.AtomicInteger restantes =
                new java.util.concurrent.atomic.AtomicInteger(pendientesDeCarga.size());
        for (final Long idDocumento : pendientesDeCarga) {
            SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>, Void> worker =
                    new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>, Void>() {
                @Override
                protected List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> doInBackground() throws Exception {
                    return documentoAnalisisService.listarIntentosNotificacion(idDocumento);
                }

                @Override
                protected void done() {
                    try {
                        intentosPublicacionCache.put(idDocumento, get());
                    } catch (Exception ex) {
                        intentosPublicacionCache.put(idDocumento, new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>());
                    }
                    if (restantes.decrementAndGet() == 0) {
                        crearBorradoresPublicacion(objetivos);
                    }
                }
            };
            worker.execute();
        }
    }

    private void crearBorradoresPublicacion(java.util.Collection<Long> idsDocumento) {
        int agregados = 0;
        for (Long idDocumento : idsDocumento) {
            com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO documento = buscarDocumentoPublicacion(idDocumento);
            if (documento == null || borradoresPublicacionPorDocumento.containsKey(idDocumento)) {
                continue;
            }
            List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> intentosActuales = intentosPublicacionCache.get(idDocumento);
            int totalActual = intentosActuales != null ? intentosActuales.size() : documento.getTotalIntentos();
            boolean yaTienePublicacion = false;
            if (intentosActuales != null) {
                for (com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento : intentosActuales) {
                    if ("PUBLICACION".equalsIgnoreCase(intento.getTipoNotificacionCodigo())) {
                        yaTienePublicacion = true;
                        break;
                    }
                }
            }
            if (yaTienePublicacion || totalActual >= 3) {
                continue;
            }
            borradoresPublicacionPorDocumento.put(idDocumento, new PublicacionBorrador(
                    secuenciaBorradorPublicacion.getAndDecrement(), documento.getIdExpediente(), idDocumento, totalActual + 1));
            documentosPublicacionExpandidos.add(idDocumento);
            agregados++;
        }
        if (agregados == 0) {
            JOptionPane.showMessageDialog(this,
                    "Los documentos seleccionados ya tienen una publicación registrada.",
                    "Agregar publicación", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        reconstruirFilasPublicacionBandeja();
    }

    private void cancelarBorradorPublicacion(int modelRow) {
        if (modelRow < 0 || modelRow >= filasPublicacionBandeja.size()) {
            return;
        }
        PublicacionFilaTabla fila = filasPublicacionBandeja.get(modelRow);
        if (!fila.esBorrador()) {
            return;
        }
        borradoresPublicacionPorDocumento.remove(fila.idDocumento);
        reconstruirFilasPublicacionBandeja();
    }

    /** Baja logica de una publicación ya guardada (no un borrador), con confirmación previa. */
    private void eliminarFilaPublicacion(int modelRow) {
        if (modelRow < 0 || modelRow >= filasPublicacionBandeja.size()) {
            return;
        }
        PublicacionFilaTabla fila = filasPublicacionBandeja.get(modelRow);
        if (fila.esPadre() || fila.esBorrador() || fila.intento == null) {
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(
                this,
                "¿Desea eliminar el registro de publicación de este documento?",
                "Eliminar publicación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }
        final Long idExpedienteNotificacion = fila.intento.getIdExpedienteNotificacion();
        final Long idDocumento = fila.idDocumento;
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                documentoAnalisisService.darBajaIntentoNotificacion(idExpedienteNotificacion);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    recargarBandejaPublicacionYExpandir(idDocumento);
                } catch (Exception ex) {
                    mostrarError("No se pudo eliminar el registro de publicación.", ex);
                }
            }
        };
        worker.execute();
    }

    /** Guarda la edicion inline de la fila de Publicación (borrador nuevo o ya persistida). */
    private void guardarFilaPublicacion(int modelRow) {
        if (modelRow < 0 || modelRow >= filasPublicacionBandeja.size()) {
            return;
        }
        PublicacionFilaTabla fila = filasPublicacionBandeja.get(modelRow);
        if (fila.esPadre() || fila.esSubEncabezado() || fila.esIntentoSoloLectura()) {
            return;
        }
        Object fechaValor = publicacionBandejaModel.getValueAt(modelRow, COL_PUB_FECHA);
        Object estadoValor = publicacionBandejaModel.getValueAt(modelRow, COL_PUB_ESTADO);
        Object referenciaValor = publicacionBandejaModel.getValueAt(modelRow, COL_PUB_REFERENCIA);
        final LocalDate fecha = parseFechaCeldaNotif(fechaValor);
        final String estadoCodigo = estadoValor == null || estadoValor.toString().trim().isEmpty() ? "PENDIENTE" : estadoValor.toString();
        final String referencia = referenciaValor == null ? "" : referenciaValor.toString();

        if (fila.esBorrador()) {
            final PublicacionBorrador borrador = fila.borrador;
            final Long idDocumento = fila.idDocumento;
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    documentoAnalisisService.registrarIntentoNotificacion(
                            borrador.idExpediente, idDocumento, "PUBLICACION", referencia, fecha, estadoCodigo);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        borradoresPublicacionPorDocumento.remove(idDocumento);
                        recargarBandejaPublicacionYExpandir(idDocumento);
                    } catch (Exception ex) {
                        mostrarError("No se pudo registrar la publicación.", ex);
                    }
                }
            };
            worker.execute();
            return;
        }

        final com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento = fila.intento;
        final Long idDocumento = fila.idDocumento;
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                documentoAnalisisService.actualizarIntentoNotificacion(
                        intento.getIdExpedienteNotificacion(), "PUBLICACION", estadoCodigo, referencia, fecha, null);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    recargarBandejaPublicacionYExpandir(idDocumento);
                } catch (Exception ex) {
                    mostrarError("No se pudo actualizar la publicación.", ex);
                }
            }
        };
        worker.execute();
    }

    /** Recarga la bandeja completa (Estado Final depende del servidor) y re-expande el documento editado. */
    private void recargarBandejaPublicacionYExpandir(final Long idDocumento) {
        lblEstadoPublicacionBandeja.setText("Actualizando bandeja de publicación...");
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void>() {
            private List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> intentosDocumento;

            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> doInBackground() throws Exception {
                List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> items =
                        documentoAnalisisService.listarDocumentosBandejaPublicacion();
                if (idDocumento != null) {
                    intentosDocumento = documentoAnalisisService.listarIntentosNotificacion(idDocumento);
                }
                return items;
            }

            @Override
            protected void done() {
                try {
                    List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> items = get();
                    documentosPublicacionBandeja.clear();
                    documentosPublicacionBandeja.addAll(items);
                    intentosPublicacionCache.clear();
                    if (idDocumento != null) {
                        intentosPublicacionCache.put(idDocumento, intentosDocumento == null
                                ? new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>()
                                : intentosDocumento);
                        documentosPublicacionExpandidos.add(idDocumento);
                    }
                    actualizarMetricasPublicacionNotif();
                    reconstruirFilasPublicacionBandeja();
                } catch (Exception ex) {
                    lblEstadoPublicacionBandeja.setText("No se pudieron actualizar los documentos pendientes de publicación.");
                }
            }
        };
        worker.execute();
    }

    private void configurarTablaPublicacionNotif() {
        tablaPublicacionBandeja.setRowHeight(32);
        tablaPublicacionBandeja.setAutoCreateRowSorter(false);
        tablaPublicacionBandeja.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaPublicacionBandeja.getTableHeader().setReorderingAllowed(false);
        tablaPublicacionBandeja.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tablaPublicacionBandeja.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        tablaPublicacionBandeja.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        tablaPublicacionBandeja.setGridColor(AppV2Theme.BORDER);
        tablaPublicacionBandeja.setShowVerticalLines(false);
        AppV2TableColumnSizer.applyFriendlyDefaults(tablaPublicacionBandeja);
        tablaPublicacionBandeja.getColumnModel().getColumn(COL_PUB_SEL).setMaxWidth(34);
        tablaPublicacionBandeja.getColumnModel().getColumn(COL_PUB_SEL).setMinWidth(30);
        tablaPublicacionBandeja.getColumnModel().getColumn(COL_PUB_SEL).setCellRenderer(new PublicacionSeleccionRenderer());
        tablaPublicacionBandeja.getColumnModel().getColumn(COL_PUB_SEL).setCellEditor(new PublicacionSeleccionEditor());
        tablaPublicacionBandeja.getColumnModel().getColumn(COL_PUB_EXPAND).setMaxWidth(46);
        tablaPublicacionBandeja.getColumnModel().getColumn(COL_PUB_EXPAND).setMinWidth(40);
        tablaPublicacionBandeja.getColumnModel().getColumn(COL_PUB_EXPAND).setCellRenderer(new PublicacionExpandirRenderer());
        tablaPublicacionBandeja.getColumnModel().getColumn(COL_PUB_FECHA).setCellEditor(new NotifFechaCellEditor());
        tablaPublicacionBandeja.getColumnModel().getColumn(COL_PUB_ESTADO).setCellEditor(
                new NotifComboCellEditor(crearComboEstadoPublicacion()));
        tablaPublicacionBandeja.getColumnModel().getColumn(COL_PUB_ACCION).setMaxWidth(76);
        tablaPublicacionBandeja.getColumnModel().getColumn(COL_PUB_ACCION).setMinWidth(64);
        tablaPublicacionBandeja.getColumnModel().getColumn(COL_PUB_ACCION).setCellRenderer(new PublicacionAccionRenderer());
        tablaPublicacionBandeja.getColumnModel().getColumn(COL_PUB_ACCION).setCellEditor(new PublicacionAccionEditor());
        tablaPublicacionBandeja.setDefaultRenderer(Object.class, new PublicacionBandejaRenderer());
        AppV2ColumnFilterSupport.install(
                "bandejaPublicacion",
                tablaPublicacionBandeja,
                tablaPublicacionBandejaPanel.getScrollPane(),
                tablaPublicacionBandejaPanel,
                () -> {
                    documentosPublicacionExpandidos.clear();
                    reconstruirFilasPublicacionBandeja();
                },
                COL_PUB_SEL, COL_PUB_EXPAND, COL_PUB_ACCION);
        publicacionBandejaModel.addTableModelListener(evento -> {
            if (evento.getColumn() != COL_PUB_SEL || evento.getType() != javax.swing.event.TableModelEvent.UPDATE) {
                return;
            }
            int fila = evento.getFirstRow();
            if (fila < 0 || fila >= filasPublicacionBandeja.size()) {
                return;
            }
            PublicacionFilaTabla filaTabla = filasPublicacionBandeja.get(fila);
            if (!filaTabla.esPadre()) {
                return;
            }
            boolean marcado = Boolean.TRUE.equals(publicacionBandejaModel.getValueAt(fila, COL_PUB_SEL));
            if (marcado) {
                documentosPublicacionSeleccionados.add(filaTabla.idDocumento);
            } else {
                documentosPublicacionSeleccionados.remove(filaTabla.idDocumento);
            }
            actualizarEstadoBotonAgregarPublicacion();
        });
        tablaPublicacionBandeja.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = tablaPublicacionBandeja.rowAtPoint(e.getPoint());
                int viewCol = tablaPublicacionBandeja.columnAtPoint(e.getPoint());
                if (viewRow < 0) {
                    return;
                }
                int modelRow = tablaPublicacionBandeja.convertRowIndexToModel(viewRow);
                if (modelRow < 0 || modelRow >= filasPublicacionBandeja.size()) {
                    return;
                }
                int modelCol = tablaPublicacionBandeja.convertColumnIndexToModel(viewCol);
                PublicacionFilaTabla fila = filasPublicacionBandeja.get(modelRow);
                if (modelCol == COL_PUB_SEL) {
                    return;
                }
                if (modelCol == COL_PUB_EXPAND && fila.esPadre()) {
                    alternarExpansionPublicacion(fila.idDocumento);
                    return;
                }
                idDocumentoPublicacionSeleccionado = fila.idDocumento;
                actualizarEstadoBotonAgregarPublicacion();
                if (e.getClickCount() == 2 && fila.esPadre()) {
                    panelPublicacionCerradoPorUsuario = false;
                    actualizarFocoPublicacionNotif(fila.idDocumento);
                    if (documentoPublicacionFoco != null) {
                        mostrarPanelLateralNotif(panelLateralPublicacionNotif);
                        splitBandejasNotif.setSideVisible(true);
                        seleccionarTabPublicacion(TAB_PUBLICACION_DATOS);
                    }
                }
            }
        });
    }

    private JComboBox<SimpleItem> crearComboEstadoPublicacion() {
        JComboBox<SimpleItem> combo = new JComboBox<SimpleItem>();
        combo.addItem(new SimpleItem("PENDIENTE", "Pendiente"));
        combo.addItem(new SimpleItem("EXITOSA", "Publicado"));
        return combo;
    }

    private void actualizarFocoPublicacionNotif(Long idDocumento) {
        documentoPublicacionFoco = buscarDocumentoPublicacion(idDocumento);
        actualizarPanelDatosPublicacionNotif();
        actualizarPanelInfoPublicacionNotif();
    }

    private void actualizarPanelDatosPublicacionNotif() {
        if (documentoPublicacionFoco == null || documentoPublicacionFoco.getIdExpediente() == null) {
            datosPublicacionNotif.limpiar();
            return;
        }
        final Long idExpediente = documentoPublicacionFoco.getIdExpediente();
        SwingWorker<com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO, Void> worker =
                new SwingWorker<com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO, Void>() {
            @Override
            protected com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO doInBackground() throws Exception {
                return asignacionExpedienteServiceNotif.obtenerExpedientePorId(idExpediente);
            }

            @Override
            protected void done() {
                if (documentoPublicacionFoco == null || !idExpediente.equals(documentoPublicacionFoco.getIdExpediente())) {
                    return;
                }
                try {
                    datosPublicacionNotif.poblar(get());
                } catch (Exception ex) {
                    datosPublicacionNotif.limpiar();
                }
            }
        };
        worker.execute();
    }

    /** Panel "Publicación" del panel lateral: puramente informativo, sin campos editables. */
    private void actualizarPanelInfoPublicacionNotif() {
        if (documentoPublicacionFoco == null) {
            limpiarPanelInfoPublicacionNotif();
            return;
        }
        lblPubInfoTipoDocumento.setText(valorNotif(documentoPublicacionFoco.getTipoDocumento()));
        lblPubInfoNumeroDocumento.setText(valorNotif(documentoPublicacionFoco.getNumeroDocumento()));
        lblPubInfoFechaEmision.setText(documentoPublicacionFoco.getFechaDocumento() == null
                ? "-" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(documentoPublicacionFoco.getFechaDocumento()));
        lblPubInfoEstadoFinal.setText(valorNotif(documentoPublicacionFoco.getEstadoFinalNotificacion()));
        lblPubInfoIntento1.setText("Cargando...");
        lblPubInfoIntento2.setText("Cargando...");
        lblPubInfoPublicacion.setText("Cargando...");
        final Long idDocumento = documentoPublicacionFoco.getIdDocumentoAnalizado();
        List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> cache = intentosPublicacionCache.get(idDocumento);
        if (cache != null) {
            poblarPanelInfoIntentosPublicacion(cache);
            return;
        }
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> doInBackground() throws Exception {
                return documentoAnalisisService.listarIntentosNotificacion(idDocumento);
            }

            @Override
            protected void done() {
                if (documentoPublicacionFoco == null || !idDocumento.equals(documentoPublicacionFoco.getIdDocumentoAnalizado())) {
                    return;
                }
                try {
                    List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> items = get();
                    intentosPublicacionCache.put(idDocumento, items);
                    poblarPanelInfoIntentosPublicacion(items);
                } catch (Exception ex) {
                    lblPubInfoIntento1.setText("-");
                    lblPubInfoIntento2.setText("-");
                    lblPubInfoPublicacion.setText("-");
                }
            }
        };
        worker.execute();
    }

    private void poblarPanelInfoIntentosPublicacion(List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> intentos) {
        String intento1 = "-";
        String intento2 = "-";
        String publicacion = "Sin registrar";
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento : intentos) {
            if ("PUBLICACION".equalsIgnoreCase(intento.getTipoNotificacionCodigo())) {
                String estado = textoEstadoPublicacion(codigoEstadoPublicacionParaColumna(intento.getEstadoNotificacionCodigo()));
                String fecha = intento.getFechaEnvio() == null
                        ? "-" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(intento.getFechaEnvio());
                publicacion = estado + " · " + fecha;
            } else if (intento.getNumeroIntento() == 1) {
                intento1 = valorNotif(intento.getEstadoNotificacion());
            } else if (intento.getNumeroIntento() == 2) {
                intento2 = valorNotif(intento.getEstadoNotificacion());
            }
        }
        lblPubInfoIntento1.setText(intento1);
        lblPubInfoIntento2.setText(intento2);
        lblPubInfoPublicacion.setText(publicacion);
    }

    private void limpiarPanelInfoPublicacionNotif() {
        lblPubInfoTipoDocumento.setText("-");
        lblPubInfoNumeroDocumento.setText("-");
        lblPubInfoFechaEmision.setText("-");
        lblPubInfoEstadoFinal.setText("-");
        lblPubInfoIntento1.setText("-");
        lblPubInfoIntento2.setText("-");
        lblPubInfoPublicacion.setText("-");
    }

    private void cerrarPanelPublicacionNotif() {
        panelPublicacionCerradoPorUsuario = true;
        if (splitBandejasNotif != null) {
            splitBandejasNotif.setSideVisible(false);
        }
    }

    private JPanel crearPanelDetallePublicacionNotif() {
        AppV2SideActionPanel panelDatos = datosPublicacionNotif.crearPanel(
                "Panel de datos", new Color(57, 125, 199), this::cerrarPanelPublicacionNotif);
        AppV2SideActionPanel panelPublicacionInfo = crearPanelPublicacionInfoNotif();
        return crearPanelPublicacionConTabNotif(panelDatos, panelPublicacionInfo);
    }

    /**
     * Lengüeta "Publicación" del panel lateral: solo informativa (sin botones ni campos
     * editables). Registrar o editar la publicación se hace unicamente desde la fila
     * correspondiente de la grilla (icono Guardar/Eliminar), pedido explicito del usuario.
     */
    private AppV2SideActionPanel crearPanelPublicacionInfoNotif() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Publicación", this::cerrarPanelPublicacionNotif);
        panel.setAccentColor(new Color(10, 118, 145));
        AppV2SideSectionPanel seccionDocumento = new AppV2SideSectionPanel("Documento");
        seccionDocumento.addRow("Tipo documento", lblPubInfoTipoDocumento);
        seccionDocumento.addRow("N° Documento", lblPubInfoNumeroDocumento);
        seccionDocumento.addRow("Fecha Emisión", lblPubInfoFechaEmision);
        seccionDocumento.addRow("Estado Final", lblPubInfoEstadoFinal);
        panel.addSection(seccionDocumento);
        AppV2SideSectionPanel seccionIntentos = new AppV2SideSectionPanel("Intentos de notificación");
        seccionIntentos.addRow("Intento 1", lblPubInfoIntento1);
        seccionIntentos.addRow("Intento 2", lblPubInfoIntento2);
        panel.addSection(seccionIntentos);
        AppV2SideSectionPanel seccionPublicacion = new AppV2SideSectionPanel("Publicación registrada");
        seccionPublicacion.addRow("Publicación", lblPubInfoPublicacion);
        panel.addSection(seccionPublicacion);
        JLabel ayuda = new JLabel(
                "<html>Panel informativo. Para registrar, editar o eliminar la publicación use los "
                        + "iconos de la fila correspondiente en la grilla de la bandeja.</html>");
        ayuda.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        ayuda.setForeground(AppV2Theme.TEXT_SECONDARY);
        ayuda.setBorder(BorderFactory.createEmptyBorder(4, 4, 8, 4));
        panel.addSection(ayuda);
        return panel;
    }

    private JPanel crearPanelPublicacionConTabNotif(
            final AppV2SideActionPanel panelDatos,
            final AppV2SideActionPanel panelPublicacionInfo) {
        JPanel wrapper = new JPanel(null) {
            @Override
            public void doLayout() {
                int width = getWidth();
                int height = getHeight();
                int panelX = PANEL_PUBLICACION_TAB_OVERHANG;
                int panelWidth = Math.max(0, width - panelX);
                int[] positions = calcularPosicionesLenguetasNotif(
                        2, PANEL_PUBLICACION_TAB_HEIGHT, 8, height, PANEL_PUBLICACION_TAB_TOP);
                tabPublicacionDatos.setBounds(0, positions[0], PANEL_PUBLICACION_TAB_OVERHANG - 6, PANEL_PUBLICACION_TAB_HEIGHT);
                tabPublicacionPublicacion.setBounds(0, positions[1], PANEL_PUBLICACION_TAB_OVERHANG - 6, PANEL_PUBLICACION_TAB_HEIGHT);
                panelPublicacionCards.setBounds(panelX, 0, panelWidth, height);
            }
        };
        wrapper.setOpaque(false);
        panelPublicacionCardsLayout = new CardLayout();
        panelPublicacionCards = new JPanel(panelPublicacionCardsLayout);
        panelPublicacionCards.setOpaque(false);
        panelPublicacionCards.add(panelDatos, TAB_PUBLICACION_DATOS);
        panelPublicacionCards.add(panelPublicacionInfo, TAB_PUBLICACION_PUBLICACION);
        tabPublicacionDatos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabPublicacion(TAB_PUBLICACION_DATOS);
            }
        });
        tabPublicacionPublicacion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabPublicacion(TAB_PUBLICACION_PUBLICACION);
            }
        });
        wrapper.add(tabPublicacionDatos);
        wrapper.add(tabPublicacionPublicacion);
        wrapper.add(panelPublicacionCards);
        wrapper.setMinimumSize(new Dimension(PANEL_PUBLICACION_ANCHO_MINIMO + PANEL_PUBLICACION_TAB_OVERHANG, 0));
        wrapper.setPreferredSize(new Dimension(PANEL_PUBLICACION_ANCHO_NORMAL + PANEL_PUBLICACION_TAB_OVERHANG, 0));
        seleccionarTabPublicacion(TAB_PUBLICACION_DATOS);
        return wrapper;
    }

    private void seleccionarTabPublicacion(String tab) {
        if (tab == null || panelPublicacionCardsLayout == null || panelPublicacionCards == null) {
            return;
        }
        boolean mismaTab = tab.equals(tabPublicacionActiva);
        tabPublicacionActiva = tab;
        panelPublicacionCardsLayout.show(panelPublicacionCards, tab);
        if (splitBandejasNotif != null && splitBandejasNotif.isSideVisible() && mismaTab) {
            splitBandejasNotif.setSideExpanded(!splitBandejasNotif.isSideExpanded());
        }
        panelPublicacionCards.revalidate();
        panelPublicacionCards.repaint();
        actualizarLenguetasPublicacion();
    }

    private void actualizarLenguetasPublicacion() {
        boolean expandido = splitBandejasNotif != null && splitBandejasNotif.isSideExpanded();
        tabPublicacionDatos.setState(
                TAB_PUBLICACION_DATOS.equals(tabPublicacionActiva), TAB_PUBLICACION_DATOS.equals(tabPublicacionActiva) && expandido);
        tabPublicacionPublicacion.setState(
                TAB_PUBLICACION_PUBLICACION.equals(tabPublicacionActiva),
                TAB_PUBLICACION_PUBLICACION.equals(tabPublicacionActiva) && expandido);
    }

    private class PublicacionExpandirRenderer extends JPanel implements TableCellRenderer {
        private final NotifIntentoGlyph glyph = new NotifIntentoGlyph();

        private PublicacionExpandirRenderer() {
            setOpaque(true);
            setLayout(new BorderLayout());
            add(glyph, BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Color background = isSelected ? new Color(219, 244, 249) : (row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
            setBackground(background);
            String texto = value == null ? "" : value.toString();
            int total = 0;
            int separador = texto.indexOf(':');
            String estado = separador >= 0 ? texto.substring(0, separador) : texto;
            if (separador >= 0) {
                try {
                    total = Integer.parseInt(texto.substring(separador + 1));
                } catch (NumberFormatException ignored) {
                    total = 0;
                }
            }
            if ("expand".equals(estado)) {
                glyph.configure(NotifIntentoGlyph.EXPAND, total, AppV2Theme.TEAL, background);
            } else if ("collapse".equals(estado)) {
                glyph.configure(NotifIntentoGlyph.COLLAPSE, total, AppV2Theme.TEAL, background);
            } else {
                glyph.configure(NotifIntentoGlyph.NONE, 0, AppV2Theme.TEAL, background);
            }
            return this;
        }
    }

    private class PublicacionBandejaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            int modelRow = table.convertRowIndexToModel(row);
            int modelCol = table.convertColumnIndexToModel(column);
            PublicacionFilaTabla fila = modelRow >= 0 && modelRow < filasPublicacionBandeja.size()
                    ? filasPublicacionBandeja.get(modelRow) : null;
            boolean esSubEncabezado = fila != null && fila.esSubEncabezado();
            boolean esHijo = fila != null && !fila.esPadre() && !esSubEncabezado;
            Object valorMostrado = value;
            if (esHijo && modelCol == COL_PUB_ESTADO && !fila.esIntentoSoloLectura()) {
                valorMostrado = textoEstadoPublicacion(value == null ? "" : value.toString());
            }
            Component c = super.getTableCellRendererComponent(table, valorMostrado, isSelected, hasFocus, row, column);
            setFont(esSubEncabezado
                    ? AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL - 1)
                    : (esHijo ? AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL) : AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE)));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (!isSelected) {
                if (esSubEncabezado) {
                    setBackground(new Color(224, 238, 241));
                    setForeground(AppV2Theme.TEAL.darker());
                } else {
                    setBackground(esHijo ? new Color(238, 250, 252) : (row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT));
                    setForeground(esHijo ? AppV2Theme.TEXT_SECONDARY : AppV2Theme.TEXT_PRIMARY);
                }
            }
            return c;
        }
    }

    private class PublicacionSeleccionRenderer extends JPanel implements TableCellRenderer {
        private final JCheckBox checkBox = new JCheckBox();

        private PublicacionSeleccionRenderer() {
            setOpaque(true);
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            checkBox.setOpaque(false);
            add(checkBox);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Color background = isSelected ? new Color(219, 244, 249) : (row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
            setBackground(background);
            int modelRow = table.convertRowIndexToModel(row);
            boolean esPadre = modelRow >= 0 && modelRow < filasPublicacionBandeja.size() && filasPublicacionBandeja.get(modelRow).esPadre();
            checkBox.setVisible(esPadre);
            checkBox.setSelected(Boolean.TRUE.equals(value));
            return this;
        }
    }

    private class PublicacionSeleccionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JCheckBox checkBox = new JCheckBox();

        private PublicacionSeleccionEditor() {
            checkBox.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Object getCellEditorValue() {
            return Boolean.valueOf(checkBox.isSelected());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            checkBox.setSelected(Boolean.TRUE.equals(value));
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            panel.setOpaque(true);
            panel.setBackground(table.getSelectionBackground());
            panel.add(checkBox);
            return panel;
        }
    }

    private class PublicacionAccionRenderer extends JPanel implements TableCellRenderer {
        private final JButton btnGuardar = crearBotonAccionNotif(new NotifSaveIcon(), "Guardar publicación");
        private final JButton btnCancelar = crearBotonAccionNotif(new NotifCancelIcon(), "Descartar publicación sin guardar");
        private final JButton btnEliminar = crearBotonAccionNotif(new NotifDeleteIcon(), "Eliminar publicación");

        private PublicacionAccionRenderer() {
            setOpaque(true);
            setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));
            add(btnGuardar);
            add(btnCancelar);
            add(btnEliminar);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? new Color(219, 244, 249) : (row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT));
            boolean esBorrador = "guardar-borrador".equals(value);
            boolean esPersistido = "guardar".equals(value);
            btnGuardar.setVisible(esBorrador || esPersistido);
            btnCancelar.setVisible(esBorrador);
            btnEliminar.setVisible(esPersistido);
            return this;
        }
    }

    private class PublicacionAccionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        private final JButton btnGuardar = crearBotonAccionNotif(new NotifSaveIcon(), "Guardar publicación");
        private final JButton btnCancelar = crearBotonAccionNotif(new NotifCancelIcon(), "Descartar publicación sin guardar");
        private final JButton btnEliminar = crearBotonAccionNotif(new NotifDeleteIcon(), "Eliminar publicación");
        private int editingRow = -1;

        private PublicacionAccionEditor() {
            panel.setOpaque(true);
            panel.add(btnGuardar);
            panel.add(btnCancelar);
            panel.add(btnEliminar);
            btnGuardar.addActionListener(e -> {
                int fila = editingRow;
                fireEditingStopped();
                guardarFilaPublicacion(fila);
            });
            btnCancelar.addActionListener(e -> {
                int fila = editingRow;
                fireEditingStopped();
                cancelarBorradorPublicacion(fila);
            });
            btnEliminar.addActionListener(e -> {
                int fila = editingRow;
                fireEditingStopped();
                eliminarFilaPublicacion(fila);
            });
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = table.convertRowIndexToModel(row);
            boolean esBorrador = "guardar-borrador".equals(value);
            boolean esPersistido = "guardar".equals(value);
            btnGuardar.setVisible(esBorrador || esPersistido);
            btnCancelar.setVisible(esBorrador);
            btnEliminar.setVisible(esPersistido);
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
    }

    // =================== fin de Bandeja Publicación ===================

    private void actualizarTabBandejaNotificacion() {
        if (tabsBandejasTop != null) {
            int index = tabsBandejasTop.getSelectedIndex();
            if (index == 0) {
                modoBandejaNotificacion = ModoBandejaNotificacion.ASIGNACION;
            } else if (index == 1) {
                modoBandejaNotificacion = ModoBandejaNotificacion.VALIDACION;
            } else if (index == 2) {
                modoBandejaNotificacion = ModoBandejaNotificacion.NOTIFICACION;
            } else {
                modoBandejaNotificacion = ModoBandejaNotificacion.PUBLICACION;
            }
        }
        // El panel lateral es compartido por las 4 bandejas: al cambiar de bandeja se cierra,
        // para no dejar visible el detalle de una bandeja distinta a la seleccionada.
        if (splitBandejasNotif != null) {
            splitBandejasNotif.setSideVisible(false);
        }
        mostrarTabPanelNotificacion(modoBandejaNotificacion == ModoBandejaNotificacion.VALIDACION
                ? TAB_NOTIF_PANEL_CIERRE
                : TAB_NOTIF_PANEL_NOTIFICACION);
        if (construccionCompleta) {
            if (modoBandejaNotificacion == ModoBandejaNotificacion.ASIGNACION) {
                cargarBandejaAsignacionNotificacion();
            } else if (modoBandejaNotificacion == ModoBandejaNotificacion.VALIDACION) {
                cargarBandejaValidacion();
            } else if (modoBandejaNotificacion == ModoBandejaNotificacion.PUBLICACION) {
                cargarBandejaPublicacionNotif();
            } else {
                buscar();
                cargarBandejaNotifV2();
            }
        }
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
                spnLimite,
                new AppV2ExpedientePanelFactory.CampoFiltro("Tipo notificación", cmbTipoNotificacionFiltro),
                new AppV2ExpedientePanelFactory.CampoFiltro("Resultado", cmbResultadoFiltro),
                new AppV2ExpedientePanelFactory.CampoFiltro("Publicación prevista", cmbPublicacionFiltro));
    }

    private AppV2SideActionPanel crearPanelNotificacion() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de notificación", new Runnable() {
            @Override
            public void run() {
                cerrarPanelNotificacion();
            }
        });
        panel.setAccentColor(AppV2Theme.PRIMARY);
        panel.addSection(crearResumenSeleccion());
        panel.addSection(crearAntecedentes());
        panel.addSection(crearDocumentosPanel());
        panel.addSection(crearValidacionCartaPanel());
        panel.addSection(crearFormularioNotificacion());
        panel.addSection(crearPublicacionPanel());
        panel.setFooter(crearAccionesPanelNotificacion());
        return panel;
    }

    private AppV2SideActionPanel crearPanelCierre() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Cierre", new Runnable() {
            @Override
            public void run() {
                cerrarPanelNotificacion();
            }
        });
        panel.setAccentColor(AppV2Theme.PRIMARY);
        panel.addSection(crearCierreResumenPanel());
        panel.addSection(crearCierrePanel());
        panel.setFooter(crearAccionesCierrePanel());
        return panel;
    }

    private JPanel crearPanelNotificacionConTab(final AppV2SideActionPanel panelNotificacion, final AppV2SideActionPanel panelCierre) {
        JPanel wrapper = new JPanel(null) {
            @Override
            public void doLayout() {
                int width = getWidth();
                int height = getHeight();
                int panelX = PANEL_NOTIFICACION_TAB_OVERHANG;
                int panelWidth = Math.max(0, width - panelX);
                int[] positions = calcularPosicionesLenguetasNotif(
                        2, PANEL_NOTIFICACION_TAB_HEIGHT, 8, height, PANEL_NOTIFICACION_TAB_TOP);
                tabNotifPanelNotificacion.setBounds(0, positions[0], PANEL_NOTIFICACION_TAB_OVERHANG - 6, PANEL_NOTIFICACION_TAB_HEIGHT);
                tabNotifPanelCierre.setBounds(0, positions[1], PANEL_NOTIFICACION_TAB_OVERHANG - 6, PANEL_NOTIFICACION_TAB_HEIGHT);
                panelNotifCards.setBounds(panelX, 0, panelWidth, height);
            }
        };
        wrapper.setOpaque(false);
        panelNotifCardsLayout = new CardLayout();
        panelNotifCards = new JPanel(panelNotifCardsLayout);
        panelNotifCards.setOpaque(false);
        panelNotifCards.add(panelNotificacion, TAB_NOTIF_PANEL_NOTIFICACION);
        panelNotifCards.add(panelCierre, TAB_NOTIF_PANEL_CIERRE);
        tabNotifPanelNotificacion.setToolTipText("Registrar notificación al ciudadano");
        tabNotifPanelCierre.setToolTipText("Cerrar el expediente");
        tabNotifPanelNotificacion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabPanelNotificacion(TAB_NOTIF_PANEL_NOTIFICACION);
            }
        });
        tabNotifPanelCierre.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabPanelNotificacion(TAB_NOTIF_PANEL_CIERRE);
            }
        });
        wrapper.add(tabNotifPanelNotificacion);
        wrapper.add(tabNotifPanelCierre);
        wrapper.add(panelNotifCards);
        wrapper.setMinimumSize(new Dimension(PANEL_NOTIFICACION_ANCHO_MINIMO + PANEL_NOTIFICACION_TAB_OVERHANG, 0));
        wrapper.setPreferredSize(new Dimension(PANEL_NOTIFICACION_ANCHO_NORMAL + PANEL_NOTIFICACION_TAB_OVERHANG, 0));
        mostrarTabPanelNotificacion(TAB_NOTIF_PANEL_NOTIFICACION);
        return wrapper;
    }

    private void seleccionarTabPanelNotificacion(String tab) {
        if (tab == null || panelNotifCardsLayout == null || panelNotifCards == null) {
            return;
        }
        boolean mismaTab = tab.equals(tabNotifPanelActiva);
        mostrarTabPanelNotificacion(tab);
        if (splitBandejasNotif != null && splitBandejasNotif.isSideVisible() && mismaTab) {
            splitBandejasNotif.setSideExpanded(!splitBandejasNotif.isSideExpanded());
            actualizarLenguetasPanelNotificacion();
        }
    }

    private void mostrarTabPanelNotificacion(String tab) {
        if (tab == null || panelNotifCardsLayout == null || panelNotifCards == null) {
            return;
        }
        tabNotifPanelActiva = tab;
        panelNotifCardsLayout.show(panelNotifCards, tab);
        panelNotifCards.revalidate();
        panelNotifCards.repaint();
        actualizarLenguetasPanelNotificacion();
    }

    private void actualizarLenguetasPanelNotificacion() {
        boolean expandido = splitBandejasNotif != null && splitBandejasNotif.isSideExpanded();
        tabNotifPanelNotificacion.setState(
                TAB_NOTIF_PANEL_NOTIFICACION.equals(tabNotifPanelActiva),
                TAB_NOTIF_PANEL_NOTIFICACION.equals(tabNotifPanelActiva) && expandido);
        tabNotifPanelCierre.setState(
                TAB_NOTIF_PANEL_CIERRE.equals(tabNotifPanelActiva),
                TAB_NOTIF_PANEL_CIERRE.equals(tabNotifPanelActiva) && expandido);
    }

    private JPanel crearAccionesPanelNotificacion() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setOpaque(false);
        panel.add(btnRegistrarNotificacion);
        panel.add(btnRegistrarCargo);
        panel.add(btnMarcarNotificado);
        panel.add(btnRequierePublicacion);
        return panel;
    }

    private JPanel crearCierrePanel() {
        JPanel panel = section("Cierre terminal");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Destino siguiente", lblCierreDestino);
        addRow(grid, row++, "Publicación prevista", lblCierrePublicacion);
        addRow(grid, row++, "Alertas", lblCierreAlertas);
        addRow(grid, row, "Comentario", scrollText(txtComentarioCierre, 86));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearCierreResumenPanel() {
        JPanel panel = section("Resumen de cierre");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Expediente", lblExpediente);
        addRow(grid, row++, "N° expediente SGD", lblExpedienteSgd);
        addRow(grid, row++, "Titular", lblTitular);
        addRow(grid, row++, "Procedimiento", lblProcedimiento);
        addRow(grid, row++, "Etapa / Estado", lblEtapaEstado);
        addRow(grid, row, "Documento a notificar", lblDocumentoNotificar);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearAccionesCierrePanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setOpaque(false);
        panel.add(btnCerrarExpediente);
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
        addRow(grid, row++, "Etapa / Estado", lblEtapaEstado);
        addRow(grid, row++, "Resolución", lblResolucion);
        addRow(grid, row++, "Documento a notificar", lblDocumentoNotificar);
        addRow(grid, row++, "Acciones", lblAcciones);
        addRow(grid, row, "Alertas", lblAlertas);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearAntecedentes() {
        JPanel panel = section("Antecedentes");
        txtObservacion.setEditable(false);
        txtObservacion.setBackground(AppV2Theme.SURFACE_ALT);
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Análisis", lblAnalisis);
        addRow(grid, row++, "Verificación", lblVerificacion);
        addRow(grid, row++, "Ejecución", lblEjecucion);
        addRow(grid, row, "Observación", scrollText(txtObservacion, 72));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDocumentosPanel() {
        JPanel panel = section("Documentos y resolución");
        JScrollPane scroll = new JScrollPane(documentosTable);
        scroll.setPreferredSize(new Dimension(360, 132));
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearValidacionCartaPanel() {
        JPanel panel = section("Validación de carta");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Supervisor", lblSupervisor);
        addRow(grid, row, "Destino siguiente", lblDestino);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormularioNotificacion() {
        JPanel panel = section("Intentos de notificación y cargo");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Notificación actual", lblNotificacion);
        addRow(grid, row++, "Intentos", lblIntentos);
        addRow(grid, row++, "Tipo / modalidad", cmbTipoNotificacion);
        addRow(grid, row++, "Fecha notificación", txtFechaNotificacion);
        addRow(grid, row++, "Destinatario", txtDestinatario);
        addRow(grid, row++, "Resultado", txtResultado);
        addRow(grid, row++, "Cargo actual", lblCargo);
        addRow(grid, row++, "Estado cargo", cmbEstadoCargo);
        addRow(grid, row++, "Fecha cargo", txtFechaCargo);
        addRow(grid, row++, "Recibido por", txtRecibidoPor);
        addRow(grid, row++, "Comentario", scrollText(txtComentario, 86));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPublicacionPanel() {
        JPanel panel = section("Publicación prevista");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Estado", lblPublicacion);
        addRow(grid, row, "Motivo", scrollText(txtMotivoPublicacion, 72));
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
        fechaSolicitudDesde.setPreferredSize(new Dimension(190, 38));
        fechaSolicitudHasta.setPreferredSize(new Dimension(190, 38));
        cmbTipoNotificacionFiltro.setPreferredSize(new Dimension(180, 34));
        cmbResultadoFiltro.setPreferredSize(new Dimension(170, 34));
        cmbPublicacionFiltro.setPreferredSize(new Dimension(150, 34));
        cmbTipoNotificacion.setPreferredSize(new Dimension(250, 34));
        cmbEstadoCargo.setPreferredSize(new Dimension(250, 34));
        txtFechaNotificacion.setPreferredSize(new Dimension(250, 34));
        txtFechaCargo.setPreferredSize(new Dimension(250, 34));
        txtDestinatario.setPreferredSize(new Dimension(250, 34));
        txtResultado.setPreferredSize(new Dimension(250, 34));
        txtRecibidoPor.setPreferredSize(new Dimension(250, 34));
        spnLimite.setPreferredSize(new Dimension(86, 34));
        AppV2Theme.estilizarBotonPrimario(btnBuscar);
        AppV2Theme.estilizarBotonPrimario(btnRegistrarNotificacion);
        AppV2Theme.estilizarBotonPrimario(btnRegistrarCargo);
        AppV2Theme.estilizarBotonPrimario(btnMarcarNotificado);
        AppV2Theme.estilizarBotonPrimario(btnRequierePublicacion);
        btnRequierePublicacion.setToolTipText("Preparar metadata para publicación futura sin registrar publicación real.");
        AppV2Theme.estilizarBotonPrimario(btnCerrarExpediente);
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
        table.setDefaultRenderer(Object.class, new NotificacionRenderer());
        AppV2TableColumnSizer.applyFriendlyDefaults(table);
        table.getColumnModel().getColumn(0).setMaxWidth(84);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(190);
        table.getColumnModel().getColumn(5).setPreferredWidth(220);
        table.getColumnModel().getColumn(6).setPreferredWidth(190);
        table.getColumnModel().getColumn(7).setPreferredWidth(145);
        table.getColumnModel().getColumn(8).setMaxWidth(92);
        table.getColumnModel().getColumn(11).setMaxWidth(105);
        tablePanel.getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        columnFilterSupport = AppV2ColumnFilterSupport.install(
                "Notificacion",
                table,
                tablePanel.getScrollPane(),
                tablePanel,
                null);
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
        txtBusqueda.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnRefrescar.addActionListener(e -> buscar());
        btnRegistrarNotificacion.addActionListener(e -> registrarNotificacion());
        btnRegistrarCargo.addActionListener(e -> registrarCargo());
        btnMarcarNotificado.addActionListener(e -> marcarNotificado());
        btnRequierePublicacion.addActionListener(e -> requierePublicacion());
        btnCerrarExpediente.addActionListener(e -> cerrarExpediente());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarSeleccion();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.rowAtPoint(e.getPoint()) >= 0) {
                    panelNotificacionCerradoPorUsuario = false;
                    mostrarPanelLateralNotif(panelLateralNotifBandeja);
                    splitBandejasNotif.setSideVisible(true);
                    actualizarSeleccion();
                }
            }
        });
    }

    private void cargarFiltrosBase() {
        EstadoExpedienteComboSupportV2.cargar(
                cmbEstadoFiltro, "NOTIFICACION", new SimpleItem("TODOS", "Todos los estados"),
                (codigo, nombre) -> new SimpleItem(codigo, nombre),
                ex -> lblEstado.setText("No se pudieron cargar los estados de Notificación."));
        cmbResultadoFiltro.removeAllItems();
        cmbResultadoFiltro.addItem(new SimpleItem("TODOS", "Todos los resultados"));
        cmbResultadoFiltro.addItem(new SimpleItem("ENVIADA", "Enviada"));
        cmbResultadoFiltro.addItem(new SimpleItem("EXITOSA", "Exitosa"));
        cmbResultadoFiltro.addItem(new SimpleItem("FALLIDA", "Fallida"));
        cmbPublicacionFiltro.removeAllItems();
        cmbPublicacionFiltro.addItem(new SimpleItem("TODOS", "Todas"));
        cmbPublicacionFiltro.addItem(new SimpleItem("SI", "Requiere publicación"));
        cmbPublicacionFiltro.addItem(new SimpleItem("NO", "Sin publicación"));
    }

    private void cargarCatalogos() {
        setTrabajando(true, "Cargando catálogos de notificación...");
        SwingWorker<CatalogosCarga, Void> worker = new SwingWorker<CatalogosCarga, Void>() {
            @Override
            protected CatalogosCarga doInBackground() throws Exception {
                return new CatalogosCarga(
                        notificacionService.listarTiposNotificacion(),
                        notificacionService.listarEstadosCargoAcuse());
            }

            @Override
            protected void done() {
                try {
                    CatalogosCarga carga = get();
                    cargarCombo(cmbTipoNotificacion, carga.tiposNotificacion, false);
                    cargarCombo(cmbTipoNotificacionFiltro, carga.tiposNotificacion, true);
                    cargarCombo(cmbEstadoCargo, carga.estadosCargo, false);
                } catch (Exception ex) {
                    cargarFallbackCatalogos();
                    mostrarError("No se pudieron cargar catálogos de notificación. Se usaron opciones base.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void cargarCatalogosDocumentosValidacion() {
        SwingWorker<Object[], Void> worker = new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                return new Object[]{
                        documentoAnalisisService.listarTiposDocumentoAnalizado(),
                        documentoAnalisisService.listarEstadosDocumentoNotificacion()
                };
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] resultado = get();
                    documentosValidacionTreePanel.setCatalogos(
                            (List<CatalogoItemDTO>) resultado[0],
                            (List<CatalogoItemDTO>) resultado[1]);
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los catálogos de documentos para validación.", ex);
                }
            }
        };
        worker.execute();
    }

    private void cargarFallbackCatalogos() {
        cmbTipoNotificacion.removeAllItems();
        cmbTipoNotificacion.addItem(new SimpleItem("VIRTUAL", "Virtual"));
        cmbTipoNotificacion.addItem(new SimpleItem("PRESENCIAL_1", "Presencial 1"));
        cmbTipoNotificacion.addItem(new SimpleItem("PRESENCIAL_2", "Presencial 2"));
        cmbTipoNotificacionFiltro.removeAllItems();
        cmbTipoNotificacionFiltro.addItem(new SimpleItem("TODOS", "Todos los tipos"));
        cmbTipoNotificacionFiltro.addItem(new SimpleItem("VIRTUAL", "Virtual"));
        cmbTipoNotificacionFiltro.addItem(new SimpleItem("PRESENCIAL_1", "Presencial 1"));
        cmbTipoNotificacionFiltro.addItem(new SimpleItem("PRESENCIAL_2", "Presencial 2"));
        cmbEstadoCargo.removeAllItems();
        cmbEstadoCargo.addItem(new SimpleItem("CARGO_RECIBIDO", "Cargo recibido"));
        cmbEstadoCargo.addItem(new SimpleItem("CARGO_PENDIENTE", "Cargo pendiente"));
    }

    private void cargarCombo(JComboBox<SimpleItem> combo, List<CatalogoItemDTO> items, boolean incluirTodos) {
        combo.removeAllItems();
        if (incluirTodos) {
            combo.addItem(new SimpleItem("TODOS", "Todos"));
        }
        for (CatalogoItemDTO item : items) {
            combo.addItem(new SimpleItem(item.getCodigo(), item.getNombre()));
        }
    }

    private void inicializarFechas() {
        String hoy = DATE_FORMAT.format(LocalDate.now());
        txtFechaNotificacion.setText(hoy);
        txtFechaCargo.setText(hoy);
    }

    private void inicializarFechasFiltro() {
        fechaSolicitudDesde.setDate(DateRangePickerSupport.defaultSearchFromDateCurrentMonth());
        fechaSolicitudHasta.setDate(DateRangePickerSupport.defaultSearchToDate());
    }

    private void buscar() {
        final LocalDate desde = toLocalDate(fechaSolicitudDesde);
        final LocalDate hasta = toLocalDate(fechaSolicitudHasta);
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            JOptionPane.showMessageDialog(this, "Fecha desde no puede ser mayor que Fecha hasta.", "Notificación", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        final long secuencia = secuenciaBusqueda.incrementAndGet();
        SwingWorker<?, ?> workerAnterior = busquedaActiva;
        if (workerAnterior != null && !workerAnterior.isDone()) {
            workerAnterior.cancel(true);
        }
        setTrabajando(true, "Consultando expedientes en Notificación...");
        final String texto = txtBusqueda.getText();
        final String estado = obtenerCodigo(cmbEstadoFiltro);
        final String tipoNotificacion = obtenerCodigo(cmbTipoNotificacionFiltro);
        final String resultadoNotificacion = obtenerCodigo(cmbResultadoFiltro);
        final String requierePublicacion = obtenerCodigo(cmbPublicacionFiltro);
        final int limite = ((Number) spnLimite.getValue()).intValue();
        SwingWorker<List<NotificacionExpedienteDTO>, Void> worker = new SwingWorker<List<NotificacionExpedienteDTO>, Void>() {
            @Override
            protected List<NotificacionExpedienteDTO> doInBackground() throws Exception {
                return notificacionService.buscarExpedientes(
                        texto,
                        estado,
                        desde,
                        hasta,
                        tipoNotificacion,
                        resultadoNotificacion,
                        requierePublicacion,
                        limite);
            }

            @Override
            protected void done() {
                try {
                    if (secuencia != secuenciaBusqueda.get()) {
                        return;
                    }
                    expedientes.clear();
                    expedientes.addAll(get());
                    expedientesVisibles.clear();
                    expedientesVisibles.addAll(filtrarKpi(expedientes));
                    tableModel.fireTableDataChanged();
                    table.clearSelection();
                    tablePanel.setEmpty(expedientesVisibles.isEmpty());
                    actualizarMetricas();
                    lblEstado.setText(expedientesVisibles.size() + " expediente(s) en Notificación encontrados.");
                    if (expedientesVisibles.isEmpty()) {
                        actualizarSeleccion();
                    } else {
                        actualizarSeleccion();
                    }
                    marcarKpis();
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar la bandeja de Notificación.", ex);
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

    private void limpiar() {
        if (columnFilterSupport != null) {
            columnFilterSupport.clearFilters();
        }
        txtBusqueda.setText("");
        seleccionarPrimero(cmbEstadoFiltro);
        seleccionarPrimero(cmbTipoNotificacionFiltro);
        seleccionarPrimero(cmbResultadoFiltro);
        seleccionarPrimero(cmbPublicacionFiltro);
        inicializarFechasFiltro();
        expedientes.clear();
        expedientesVisibles.clear();
        tableModel.fireTableDataChanged();
        table.clearSelection();
        tablePanel.setEmpty(true);
        actualizarMetricas();
        actualizarSeleccion();
        lblEstado.setText("Filtros limpiados. Presione Buscar para consultar Notificación.");
        panelNotificacionCerradoPorUsuario = false;
        kpiActivo = FiltroKpi.TODOS;
        marcarKpis();
    }

    private void configurarKpisInteractivos() {
        cardPendientes.setOnClick(() -> activarKpi(FiltroKpi.PENDIENTES));
        cardRevision.setOnClick(() -> activarKpi(FiltroKpi.EN_REVISION));
        cardNotificados.setOnClick(() -> activarKpi(FiltroKpi.NOTIFICADOS));
        cardFallidos.setOnClick(() -> activarKpi(FiltroKpi.FALLIDOS));
        cardPublicacion.setOnClick(() -> activarKpi(FiltroKpi.PUBLICACION));
        cardVencidos.setOnClick(() -> activarKpi(FiltroKpi.PLAZO_CRITICO));
        marcarKpis();
    }

    private void activarKpi(FiltroKpi filtro) {
        kpiActivo = filtro;
        expedientesVisibles.clear();
        expedientesVisibles.addAll(filtrarKpi(expedientes));
        tableModel.fireTableDataChanged();
        tablePanel.setEmpty(expedientesVisibles.isEmpty());
        if (expedientesVisibles.isEmpty()) {
            limpiarResumen();
        } else {
            actualizarSeleccion();
        }
        marcarKpis();
    }

    private void marcarKpis() {
        cardPendientes.setSelected(kpiActivo == FiltroKpi.PENDIENTES);
        cardRevision.setSelected(kpiActivo == FiltroKpi.EN_REVISION);
        cardNotificados.setSelected(kpiActivo == FiltroKpi.NOTIFICADOS);
        cardFallidos.setSelected(kpiActivo == FiltroKpi.FALLIDOS);
        cardPublicacion.setSelected(kpiActivo == FiltroKpi.PUBLICACION);
        cardVencidos.setSelected(kpiActivo == FiltroKpi.PLAZO_CRITICO);
    }

    private List<NotificacionExpedienteDTO> filtrarKpi(List<NotificacionExpedienteDTO> items) {
        List<NotificacionExpedienteDTO> filtrados = new ArrayList<NotificacionExpedienteDTO>();
        if (items == null || items.isEmpty() || kpiActivo == FiltroKpi.TODOS) {
            if (items != null) {
                filtrados.addAll(items);
            }
            return filtrados;
        }
        for (NotificacionExpedienteDTO item : items) {
            if (coincideKpi(item)) {
                filtrados.add(item);
            }
        }
        return filtrados;
    }

    private boolean coincideKpi(NotificacionExpedienteDTO item) {
        switch (kpiActivo) {
            case PENDIENTES:
                return item.isEnNotificacion();
            case EN_REVISION:
                return item.isCargoPendiente();
            case NOTIFICADOS:
                return item.isNotificado();
            case FALLIDOS:
                return item.isFallida();
            case PUBLICACION:
                return item.isRequierePublicacion() || item.isRequierePublicacionEstado();
            case PLAZO_CRITICO:
                return item.getDiasEnEtapa() != null && item.getDiasEnEtapa() <= 3;
            case TODOS:
            default:
                return true;
        }
    }

    private void actualizarMetricas() {
        int pendientes = 0;
        int revision = 0;
        int notificados = 0;
        int fallidos = 0;
        int publicacion = 0;
        int vencidos = 0;
        for (NotificacionExpedienteDTO expediente : expedientes) {
            if (expediente.isEnNotificacion()) {
                pendientes++;
            }
            if (expediente.isCargoPendiente()) {
                revision++;
            }
            if (expediente.isNotificado()) {
                notificados++;
            }
            if (expediente.isFallida()) {
                fallidos++;
            }
            if (expediente.isRequierePublicacion() || expediente.isRequierePublicacionEstado()) {
                publicacion++;
            }
            if (expediente.getDiasEnEtapa() != null && expediente.getDiasEnEtapa() <= 3) {
                vencidos++;
            }
        }
        cardPendientes.setValue(String.valueOf(pendientes));
        cardRevision.setValue(String.valueOf(revision));
        cardNotificados.setValue(String.valueOf(notificados));
        cardFallidos.setValue(String.valueOf(fallidos));
        cardPublicacion.setValue(String.valueOf(publicacion));
        cardVencidos.setValue(String.valueOf(vencidos));
        marcarKpis();
    }

    private void actualizarSeleccion() {
        NotificacionExpedienteDTO expediente = seleccionado();
        actualizarVisibilidadPanelNotificacion();
        if (expediente == null) {
            limpiarResumen();
            return;
        }
        lblExpediente.setText(valor(expediente.getNumeroExpediente()));
        lblExpedienteSgd.setText(valor(expediente.getNumeroExpedienteSgd()));
        lblTitular.setText(valor(expediente.getTitular()));
        actualizarSubtituloPanelesNotificacion(expediente.getTitular());
        lblActa.setText(valor(expediente.getTipoActa()) + " · " + valor(expediente.getNumeroActa()));
        lblProcedimiento.setText(valor(expediente.getProcedimiento()));
        lblEtapaEstado.setText(DisplayNameMapperV2.etapa(expediente.getEtapaCodigo()) + " / " + DisplayNameMapperV2.estado(expediente.getEstadoCodigo()));
        lblResolucion.setText(resolucionTexto(expediente));
        lblDocumentoNotificar.setText(valor(expediente.getDocumentoNotificarResumen()));
        lblNotificacion.setText(notificacionTexto(expediente));
        lblIntentos.setText(intentosTexto(expediente));
        lblCargo.setText(cargoTexto(expediente));
        lblSupervisor.setText(supervisorTexto(expediente));
        lblPublicacion.setText(publicacionTexto(expediente));
        lblDestino.setText(destinoTexto(expediente));
        lblAcciones.setText(accionesTexto(expediente));
        lblAlertas.setText(alertasTexto(expediente));
        lblAnalisis.setText(valor(expediente.getResultadoAnalisis()));
        lblVerificacion.setText(valor(expediente.getResultadoVerificacion()));
        lblEjecucion.setText(valor(expediente.getResultadoEjecucion()));
        lblCierreDestino.setText(destinoTexto(expediente));
        lblCierrePublicacion.setText(publicacionTexto(expediente));
        lblCierreAlertas.setText(alertasTexto(expediente));
        txtObservacion.setText(valor(expediente.getUltimaObservacion()));
        if (hasText(expediente.getTitular())) {
            txtDestinatario.setText(expediente.getTitular());
        }
        seleccionarModalidadSugerida(expediente);
        cargarDocumentos(expediente.getIdExpediente());
        actualizarAcciones(expediente);
    }

    private void actualizarSubtituloPanelesNotificacion(String titular) {
        String valor = titular == null || titular.trim().isEmpty() ? "" : titular.trim();
        if (panelNotificacion != null) {
            panelNotificacion.setSubtitle(valor);
        }
        if (panelCierre != null) {
            panelCierre.setSubtitle(valor);
        }
    }

    private void limpiarResumen() {
        actualizarSubtituloPanelesNotificacion(null);
        lblExpediente.setText("-");
        lblExpedienteSgd.setText("-");
        lblTitular.setText("-");
        lblActa.setText("-");
        lblProcedimiento.setText("-");
        lblEtapaEstado.setText("-");
        lblResolucion.setText("-");
        lblDocumentoNotificar.setText("-");
        lblNotificacion.setText("-");
        lblIntentos.setText("-");
        lblCargo.setText("-");
        lblSupervisor.setText("-");
        lblPublicacion.setText("-");
        lblDestino.setText("-");
        lblAcciones.setText("-");
        lblAlertas.setText("Sin expediente seleccionado.");
        lblAnalisis.setText("-");
        lblVerificacion.setText("-");
        lblEjecucion.setText("-");
        lblCierreDestino.setText("-");
        lblCierrePublicacion.setText("-");
        lblCierreAlertas.setText("Sin alertas.");
        mostrarTabPanelNotificacion(TAB_NOTIF_PANEL_NOTIFICACION);
        txtDestinatario.setText("");
        txtResultado.setText("");
        txtRecibidoPor.setText("");
        txtComentario.setText("");
        txtMotivoPublicacion.setText("");
        txtObservacion.setText("");
        txtComentarioCierre.setText("");
        inicializarFechas();
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

    private void actualizarAcciones(NotificacionExpedienteDTO expediente) {
        boolean seleccionado = expediente != null;
        btnRegistrarNotificacion.setEnabled(seleccionado && puedeRegistrarIntento(expediente));
        btnRegistrarCargo.setEnabled(seleccionado && expediente.hasAccion(NotificacionExpedienteService.ACCION_RECEPCION_CARGO));
        btnMarcarNotificado.setEnabled(seleccionado && expediente.hasAccion(NotificacionExpedienteService.ACCION_CONFIRMACION));
        btnRequierePublicacion.setEnabled(seleccionado
                && ((expediente.isIntentosAgotados() && expediente.hasAccion(NotificacionExpedienteService.ACCION_NOTIFICACION_FALLIDA))
                || expediente.hasAccion(NotificacionExpedienteService.ACCION_GENERACION_PUBLICACION)));
        btnCerrarExpediente.setEnabled(seleccionado && expediente.hasAccion(NotificacionExpedienteService.ACCION_CIERRE));
    }

    private void registrarNotificacion() {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        final String accion = resolverAccionNotificacion(expediente);
        if (!hasText(accion)) {
            JOptionPane.showMessageDialog(this, "No hay una acción de notificación activa para el tipo seleccionado.", "Notificación", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!confirmar("Se registrará el intento " + expediente.getSiguienteIntento()
                + " de notificación del expediente " + expediente.getNumeroExpediente() + ". ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando notificación...", new Callable<NotificacionResultadoDTO>() {
            @Override
            public NotificacionResultadoDTO call() throws Exception {
                return notificacionService.registrarNotificacion(crearRegistro(accion));
            }
        });
    }

    private void registrarCargo() {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null || !confirmar("Se registrará el cargo de acuse de " + expediente.getNumeroExpediente() + ". ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando cargo de acuse...", new Callable<NotificacionResultadoDTO>() {
            @Override
            public NotificacionResultadoDTO call() throws Exception {
                return notificacionService.registrarCargo(crearCargo());
            }
        });
    }

    private void marcarNotificado() {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null || !confirmar("El expediente " + expediente.getNumeroExpediente() + " será marcado como notificado. ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Marcando expediente como notificado...", new Callable<NotificacionResultadoDTO>() {
            @Override
            public NotificacionResultadoDTO call() throws Exception {
                return notificacionService.marcarNotificado(crearRegistro(NotificacionExpedienteService.ACCION_CONFIRMACION));
            }
        });
    }

    private void requierePublicacion() {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        final String accion = expediente.hasAccion(NotificacionExpedienteService.ACCION_GENERACION_PUBLICACION)
                ? NotificacionExpedienteService.ACCION_GENERACION_PUBLICACION
                : NotificacionExpedienteService.ACCION_NOTIFICACION_FALLIDA;
        if (NotificacionExpedienteService.ACCION_NOTIFICACION_FALLIDA.equals(accion) && !expediente.isIntentosAgotados()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Para preparar publicación debe registrar primero los tres intentos de notificación.",
                    "Notificación",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String mensaje = NotificacionExpedienteService.ACCION_GENERACION_PUBLICACION.equals(accion)
                ? "Se preparará el expediente para publicación futura. ¿Desea continuar?"
                : "Se registrará notificación fallida y requerimiento de publicación futura. ¿Desea continuar?";
        if (!confirmar(mensaje)) {
            return;
        }
        ejecutarOperacion("Registrando publicación requerida...", new Callable<NotificacionResultadoDTO>() {
            @Override
            public NotificacionResultadoDTO call() throws Exception {
                return notificacionService.registrarPublicacion(crearPublicacion(accion));
            }
        });
    }

    private void cerrarExpediente() {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null || !confirmar("El expediente " + expediente.getNumeroExpediente() + " será cerrado. ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Cerrando expediente...", new Callable<NotificacionResultadoDTO>() {
            @Override
            public NotificacionResultadoDTO call() throws Exception {
                return notificacionService.cerrarExpediente(crearCierre());
            }
        });
    }

    private void ejecutarOperacion(String mensajeTrabajo, Callable<NotificacionResultadoDTO> operacion) {
        setTrabajando(true, mensajeTrabajo);
        SwingWorker<NotificacionResultadoDTO, Void> worker = new SwingWorker<NotificacionResultadoDTO, Void>() {
            @Override
            protected NotificacionResultadoDTO doInBackground() throws Exception {
                return operacion.call();
            }

            @Override
            protected void done() {
                try {
                    NotificacionResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelNotificacionV2.this,
                            resultado.getMensaje(),
                            "Notificación",
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

    private NotificacionRegistroDTO crearRegistro(String accionCodigo) {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        return new NotificacionRegistroDTO(
                expediente.getIdExpediente(),
                accionCodigo,
                tipoNotificacionParaIntento(expediente),
                parseFecha(txtFechaNotificacion, "notificación"),
                txtResultado.getText(),
                txtDestinatario.getText(),
                txtComentario.getText());
    }

    private CargoAcuseDTO crearCargo() {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        SimpleItem estadoCargo = (SimpleItem) cmbEstadoCargo.getSelectedItem();
        return new CargoAcuseDTO(
                expediente.getIdExpediente(),
                NotificacionExpedienteService.ACCION_RECEPCION_CARGO,
                estadoCargo == null ? "CARGO_RECIBIDO" : estadoCargo.getCodigo(),
                parseFecha(txtFechaCargo, "cargo"),
                txtRecibidoPor.getText(),
                txtComentario.getText());
    }

    private PublicacionRequeridaDTO crearPublicacion(String accionCodigo) {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        return new PublicacionRequeridaDTO(
                expediente.getIdExpediente(),
                accionCodigo,
                txtMotivoPublicacion.getText(),
                txtComentario.getText());
    }

    private CierreNotificacionDTO crearCierre() {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        return new CierreNotificacionDTO(
                expediente.getIdExpediente(),
                NotificacionExpedienteService.ACCION_CIERRE,
                txtComentario.getText());
    }

    private String resolverAccionNotificacion(NotificacionExpedienteDTO expediente) {
        int intento = expediente.getSiguienteIntento();
        if (intento == 1 && expediente.hasAccion(NotificacionExpedienteService.ACCION_NOTIFICACION_VIRTUAL)) {
            return NotificacionExpedienteService.ACCION_NOTIFICACION_VIRTUAL;
        }
        if ((intento == 2 || intento == 3) && expediente.hasAccion(NotificacionExpedienteService.ACCION_NOTIFICACION_PRESENCIAL_2)) {
            return NotificacionExpedienteService.ACCION_NOTIFICACION_PRESENCIAL_2;
        }
        return "";
    }

    private boolean puedeRegistrarIntento(NotificacionExpedienteDTO expediente) {
        if (expediente == null || expediente.getSiguienteIntento() > 3) {
            return false;
        }
        return hasText(resolverAccionNotificacion(expediente));
    }

    private String tipoNotificacionParaIntento(NotificacionExpedienteDTO expediente) {
        if (expediente == null) {
            return "";
        }
        return expediente.getSiguienteIntento() == 1 ? "VIRTUAL" : "PRESENCIAL_2";
    }

    private void seleccionarModalidadSugerida(NotificacionExpedienteDTO expediente) {
        seleccionarComboPorCodigo(cmbTipoNotificacion, tipoNotificacionParaIntento(expediente));
    }

    private void seleccionarComboPorCodigo(JComboBox<SimpleItem> combo, String codigo) {
        if (combo == null || !hasText(codigo)) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            SimpleItem item = combo.getItemAt(i);
            if (item != null && codigo.equalsIgnoreCase(item.getCodigo())) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarPrimero(JComboBox<SimpleItem> combo) {
        if (combo != null && combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    private LocalDate toLocalDate(PremiumDateFieldV2 field) {
        if (field == null) {
            return null;
        }
        Date date = field.getDate();
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalDate parseFecha(JTextField field, String nombreCampo) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ingrese una fecha de " + nombreCampo + " válida con formato yyyy-MM-dd.");
        }
    }

    private void abrirDetalle() {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(owner, expediente.getIdExpediente());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private NotificacionExpedienteDTO seleccionado() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= expedientesVisibles.size()) {
            return null;
        }
        return expedientesVisibles.get(modelRow);
    }

    private NotificacionExpedienteDTO requerirSeleccion() {
        NotificacionExpedienteDTO expediente = seleccionado();
        if (expediente == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un expediente.", "Notificación", JOptionPane.INFORMATION_MESSAGE);
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
            btnRegistrarNotificacion.setEnabled(false);
            btnRegistrarCargo.setEnabled(false);
            btnMarcarNotificado.setEnabled(false);
            btnRequierePublicacion.setEnabled(false);
            btnCerrarExpediente.setEnabled(false);
        }
    }

    private String obtenerCodigo(JComboBox<SimpleItem> combo) {
        SimpleItem item = (SimpleItem) combo.getSelectedItem();
        return item == null ? "" : item.getCodigo();
    }

    private void cerrarPanelNotificacion() {
        panelNotificacionCerradoPorUsuario = true;
        if (splitBandejasNotif != null) {
            splitBandejasNotif.setSideVisible(false);
        }
    }

    private void actualizarVisibilidadPanelNotificacion() {
        if (modoBandejaNotificacion != ModoBandejaNotificacion.NOTIFICACION
                || splitBandejasNotif == null || !splitBandejasNotif.isSideVisible()) {
            return;
        }
        splitBandejasNotif.setSideVisible(seleccionado() != null && !panelNotificacionCerradoPorUsuario);
        actualizarLenguetasPanelNotificacion();
    }

    private String intentosTexto(NotificacionExpedienteDTO expediente) {
        int registrados = expediente.getNumeroIntento() == null ? 0 : expediente.getNumeroIntento();
        if (registrados >= 3) {
            return "3 de 3 intentos registrados";
        }
        return registrados + " de 3 registrados · siguiente intento " + expediente.getSiguienteIntento();
    }

    private String supervisorTexto(NotificacionExpedienteDTO expediente) {
        if (expediente == null) {
            return "-";
        }
        if (expediente.hasAccion(NotificacionExpedienteService.ACCION_NOTIFICACION_VIRTUAL)
                || expediente.hasAccion(NotificacionExpedienteService.ACCION_NOTIFICACION_PRESENCIAL_2)) {
            return "Carta lista para notificar según transición activa";
        }
        return "Sin devolución a Ejecución configurada; cualquier inconsistencia debe bloquearse con diagnóstico";
    }

    private String publicacionTexto(NotificacionExpedienteDTO expediente) {
        if (expediente == null) {
            return "-";
        }
        if (!expediente.isRequierePublicacion() && !expediente.isRequierePublicacionEstado()) {
            return "No requiere publicación";
        }
        String fecha = expediente.getFechaPublicacion() == null
                ? "sin fecha registrada"
                : "fecha " + format(expediente.getFechaPublicacion());
        return "Requiere publicación · " + fecha + " · dato registrado desde Asignación o Notificación";
    }

    private String destinoTexto(NotificacionExpedienteDTO expediente) {
        if (expediente == null) {
            return "-";
        }
        if (expediente.hasAccion(NotificacionExpedienteService.ACCION_GENERACION_PUBLICACION)) {
            return "Preparar para Publicación futura";
        }
        if (expediente.hasAccion(NotificacionExpedienteService.ACCION_CIERRE)) {
            return "Cierre / Archivo";
        }
        if (expediente.isCargoPendiente()) {
            return "Registrar cargo, siguiente intento o publicación futura según corresponda";
        }
        return "Pendiente de acción real activa";
    }

    private String alertasTexto(NotificacionExpedienteDTO expediente) {
        List<String> alertas = new ArrayList<String>();
        if (expediente.getTotalRelacionados() > 0) {
            alertas.add(expediente.getTotalRelacionados() + " expediente(s) asociado(s)");
        }
        if (expediente.getTotalDocumentos() == 0) {
            alertas.add("Sin documentos registrados");
        }
        if (!hasText(expediente.getNumeroResolucion())) {
            alertas.add("Sin resolución visible");
        }
        if (expediente.isCargoPendiente() && !expediente.hasAccion(NotificacionExpedienteService.ACCION_RECEPCION_CARGO)) {
            alertas.add("No hay transición activa para cargo");
        }
        if (expediente.isRequierePublicacion()) {
            alertas.add("Publicación requerida");
        }
        if (!expediente.hasAccion(NotificacionExpedienteService.ACCION_GENERACION_PUBLICACION)
                && expediente.isRequierePublicacionEstado()) {
            alertas.add("Sin transición activa a Publicación");
        }
        if (expediente.getSiguienteIntento() > 3 && !expediente.isRequierePublicacionEstado() && !expediente.isNotificado()) {
            alertas.add("Intentos agotados");
        }
        return alertas.isEmpty() ? "Sin alertas." : String.join(" · ", alertas);
    }

    private String accionesTexto(NotificacionExpedienteDTO expediente) {
        return hasText(expediente.getAccionesPermitidas())
                ? expediente.getAccionesPermitidas().replace(",", ", ")
                : "Sin acciones activas";
    }

    private String resolucionTexto(NotificacionExpedienteDTO expediente) {
        if (hasText(expediente.getNumeroResolucion())) {
            return expediente.getNumeroResolucion() + " · " + format(expediente.getFechaResolucion());
        }
        if (expediente.getIdResolucion() != null) {
            return "Resolución sin número visible";
        }
        return "Sin resolución registrada";
    }

    private String notificacionTexto(NotificacionExpedienteDTO expediente) {
        if (expediente.getIdNotificacion() == null) {
            return "Sin notificación registrada";
        }
        String intento = expediente.getNumeroIntento() == null ? "" : " · Intento " + expediente.getNumeroIntento();
        return valor(expediente.getTipoNotificacion()) + " · " + valor(expediente.getEstadoNotificacion()) + intento;
    }

    private String cargoTexto(NotificacionExpedienteDTO expediente) {
        if (expediente.getIdCargoAcuse() == null) {
            return "Sin cargo registrado";
        }
        return valor(expediente.getEstadoCargo()) + " · " + format(expediente.getFechaCargo());
    }

    private void mostrarError(String contexto, Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String detalle = cause.getMessage() == null ? "Error no especificado." : cause.getMessage();
        JOptionPane.showMessageDialog(this, contexto + "\n" + detalle, "Notificación", JOptionPane.WARNING_MESSAGE);
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

    private class NotificacionTableModel extends AbstractTableModel {

        private final String[] columns = {
            "Días", "Expediente", "N° expediente SGD", "Trámite / documento", "Titular",
            "Documento a notificar", "Estado", "Intento", "Tipo notificación",
            "Resultado", "Acuse", "Publicación prevista", "Alertas"
        };

        @Override
        public int getRowCount() {
            return expedientesVisibles.size();
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
            NotificacionExpedienteDTO item = expedientesVisibles.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return item.getDiasEnEtapa();
                case 1:
                    return item.getNumeroExpediente();
                case 2:
                    return item.getNumeroExpedienteSgd();
                case 3:
                    return item.getNumeroTramiteDocumentario();
                case 4:
                    return item.getTitular();
                case 5:
                    return item.getDocumentoNotificarResumen();
                case 6:
                    return DisplayNameMapperV2.estado(item.getEstadoCodigo());
                case 7:
                    return item.getNumeroIntento() == null ? "Sin intento" : "Intento " + item.getNumeroIntento();
                case 8:
                    return item.getTipoNotificacion();
                case 9:
                    return item.getResultadoNotificacion();
                case 10:
                    return item.isAcuseRegistrado() ? item.getEstadoCargo() : "Sin acuse";
                case 11:
                    return item.isRequierePublicacion() ? "Sí" : "No";
                case 12:
                    return alertasTexto(item);
                default:
                    return "";
            }
        }
    }

    private class NotificacionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            if (column == 0) {
                return StatusBadgeV2.forDias(value);
            }
            if (column == 6) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
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

        private final List<CatalogoItemDTO> tiposNotificacion;
        private final List<CatalogoItemDTO> estadosCargo;

        private CatalogosCarga(List<CatalogoItemDTO> tiposNotificacion, List<CatalogoItemDTO> estadosCargo) {
            this.tiposNotificacion = tiposNotificacion;
            this.estadosCargo = estadosCargo;
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

        @Override
        public String toString() {
            return nombre;
        }
    }
}
