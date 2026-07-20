package com.sdrerc.ui.views.asignacion;

import com.sdrerc.application.sdrercapp.AsignacionExpedienteService;
import com.sdrerc.application.sdrercapp.DocumentoAnalisisService;
import com.sdrerc.application.sdrercapp.ExpedienteRelacionadoDeteccionService;
import com.sdrerc.application.sdrercapp.ExpedienteRelacionadoService;
import com.sdrerc.application.sdrercapp.UsuarioAsignacionService;
import com.sdrerc.domain.dto.sdrercapp.AsignacionCartaRespuestaDTO;
import com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.AsignacionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CargaLaboralAbogadoDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.GrupoFamiliarCandidatoDTO;
import com.sdrerc.domain.dto.sdrercapp.GrupoFamiliarIntegranteDTO;
import com.sdrerc.domain.dto.sdrercapp.GrupoFamiliarResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO;
import com.sdrerc.domain.rules.AsignacionRegistroEditRules;
import com.sdrerc.ui.appv2.components.AppV2ActionPanel;
import com.sdrerc.ui.appv2.components.AppV2ColumnFilterSupport;
import com.sdrerc.ui.appv2.components.AppV2ExpedientePanelFactory;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.components.AppV2RemoveActionButton;
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
import com.sdrerc.ui.appv2.components.PlazoVisualSupportV2;
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.helpers.EstadoExpedienteComboSupportV2;
import com.sdrerc.ui.appv2.helpers.FiltroCatalogoItemV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import com.sdrerc.ui.views.expedienteconsola.DlgConsolaExpedienteV2;
import com.sdrerc.shared.session.SessionContext;
import com.sdrerc.util.DateRangePickerSupport;
import java.awt.BasicStroke;
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
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class JPanelAsignacionV2 extends JPanel {

    private enum FiltroKpiBandeja {
        TODOS,
        PENDIENTES,
        POTENCIAL_DUPLICADO,
        POSIBLE_GRUPO_FAMILIAR,
        GRUPO_FAMILIAR_CONFIRMADO,
        POR_VENCER,
        VENCIDOS
    }

    private enum FiltroKpiCartas {
        TODOS,
        CON_ACUSE,
        PENDIENTES,
        PUBLICACION
    }

    private enum FiltroKpiCarga {
        TODOS,
        CON_CARGA,
        SIN_CARGA
    }

    private static final int COL_EXPANDIR = 0;
    private static final int COL_SELECCION = 1;
    private static final int COL_DIAS = 2;
    private static final int COL_EXPEDIENTE = 3;
    private static final int COL_FECHA_VENCIMIENTO = 6;
    private static final int COL_ESTADO = 12;
    private static final int COL_RELACIONADOS = 13;
    private static final int COL_ID = 14;
    private static final int PANEL_ASIGNACION_ANCHO_MINIMO = 380;
    private static final int PANEL_ASIGNACION_ANCHO_NORMAL = 430;
    private static final int PANEL_ASIGNACION_TAB_OVERHANG = 46;
    private static final int PANEL_ASIGNACION_TAB_TOP = 18;
    private static final String TAB_DATOS_EXPEDIENTE = "datosExpediente";
    private static final String TAB_PANEL_ASIGNACION = "panelAsignacion";
    private static final String TAB_PANEL_ASOCIAR = "panelAsociar";
    private static final String TAB_PANEL_GRUPO_FAMILIAR = "panelGrupoFamiliar";
    private static final String TAB_PANEL_RESPUESTA = "panelRespuesta";
    private static final String MODO_PANEL_ASIGNACION = "asignacion";
    private static final String MODO_PANEL_RESPUESTA = "respuesta";
    private static final String MODO_PANEL_OCULTO = "oculto";
    private static final int TAB_BANDEJA_ASIGNACION = 0;
    private static final int TAB_BANDEJA_CARTAS_RESPUESTA = 1;
    private static final int TAB_BANDEJA_CARGA_ABOGADOS = 2;
    private static final String PERMISO_BANDEJA_ASIGNACION_LISTADO = "BANDEJA_ASIGNACION_LISTADO";
    private static final String PERMISO_BANDEJA_ASIGNACION_CARTAS_RESPUESTA = "BANDEJA_ASIGNACION_CARTAS_RESPUESTA";
    private static final String PERMISO_BANDEJA_ASIGNACION_CARGA_ABOGADOS = "BANDEJA_ASIGNACION_CARGA_ABOGADOS";
    private static final int ASIGNACION_TAB_HEIGHT = 92;
    private static final int GROUP_STRIPE_WIDTH = 5;
    private static final int ASSOCIATED_EXPEDIENTE_INDENT = 8;
    private static final Color TABLE_SELECTION_BACKGROUND = new Color(219, 244, 249);
    private static final Color EXPANDED_PARENT_BACKGROUND = new Color(205, 236, 244);
    private static final Color EXPANDED_ASSOCIATED_BACKGROUND = new Color(238, 250, 252);
    private static final Color TABLE_SELECTION_FOREGROUND = AppV2Theme.TEXT_PRIMARY;
    private static final Color ASSOCIATED_ROW_BACKGROUND = EXPANDED_ASSOCIATED_BACKGROUND;
    private static final Color ASSOCIATED_BLOCK_BORDER = new Color(224, 233, 240);
    private static final Color PANEL_ASSIGNMENT_ACCENT = new Color(10, 118, 145);
    private static final Color GRID_ACTION_ICON_BLUE = AppV2Theme.PRIMARY;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_HORA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color[] GROUP_STRIPE_COLORS = new Color[]{
        new Color(30, 59, 97),
        new Color(56, 88, 128),
        new Color(77, 132, 164),
        new Color(94, 154, 183),
        new Color(147, 186, 210),
        new Color(10, 118, 145),
        new Color(11, 142, 151),
        new Color(65, 164, 181),
        new Color(63, 95, 135),
        new Color(84, 110, 154),
        new Color(110, 160, 222),
        new Color(83, 101, 169),
        new Color(116, 95, 180),
        new Color(146, 120, 221),
        new Color(128, 104, 166),
        new Color(141, 116, 150),
        new Color(111, 107, 120),
        new Color(100, 117, 126),
        new Color(13, 28, 56),
        new Color(36, 53, 68)
    };

    private final AsignacionExpedienteService asignacionService;
    private final UsuarioAsignacionService usuarioService;
    private final DocumentoAnalisisService documentoAnalisisService = new DocumentoAnalisisService();
    private final ExpedienteRelacionadoDeteccionService relacionadoDeteccionService = new ExpedienteRelacionadoDeteccionService();
    private final ExpedienteRelacionadoService relacionadoService = new ExpedienteRelacionadoService();
    private final com.sdrerc.application.sdrercapp.GrupoFamiliarService grupoFamiliarService =
            new com.sdrerc.application.sdrercapp.GrupoFamiliarService();
    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite/SGD, acta, titular o documento", 28);
    private final AppV2SearchField txtBusquedaCartasRespuesta = new AppV2SearchField(
            "Buscar expediente, SGD, titular, tipo o documento", 28);
    private final PremiumDateFieldV2 fechaSolicitudDesde = new PremiumDateFieldV2();
    private final PremiumDateFieldV2 fechaSolicitudHasta = new PremiumDateFieldV2();
    private final JComboBox<FiltroCatalogoItemV2> cmbEstado = new JComboBox<FiltroCatalogoItemV2>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnEditarRegistro = new JButton("Editar");
    private final JButton btnEliminarRegistro = new JButton("Eliminar");
    private final com.sdrerc.application.sdrercapp.ExpedienteEdicionManualService edicionManualService =
            new com.sdrerc.application.sdrercapp.ExpedienteEdicionManualService();
    private final JButton btnLimpiarCartasRespuesta = new JButton("Limpiar");
    private final JButton btnAsociarRelacionados = new JButton("Asociar relacionados");
    private final JButton btnGenerarNumeroExpediente = new JButton("Generar número");
    private final JButton btnAsignarSeleccionado = new JButton("Generar asignación");
    private final JButton btnAsignarSeleccionados = new JButton("Cancelar");
    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar expedientes.");
    private final JLabel lblEstadoCartas = new JLabel("Seleccione una carta para revisar o registrar su respuesta.");
    private final JLabel lblEstadoCarga = new JLabel("Carga informativa de abogados activos.");
    private final JLabel lblSeleccionados = new JLabel("0 expedientes seleccionados");
    private final JLabel lblSeleccionadosPanel = new JLabel("0 expedientes seleccionados");
    private final JLabel lblExpedienteSeleccionado = new JLabel("-");
    private final JLabel lblTitularSeleccionado = new JLabel("-");
    private final JLabel lblTramiteWebSeleccionado = new JLabel("-");
    private final JLabel lblNumeroDocumentoSeleccionado = new JLabel("-");
    private final JLabel lblExpedienteSgdSeleccionado = new JLabel("-");
    private final JLabel lblTipoDocumentoSeleccionado = new JLabel("-");
    private final JLabel lblProcedimientoSeleccionado = new JLabel("-");
    private final JLabel lblResultadoInicialSeleccionado = new JLabel("-");
    private final JLabel lblTipoSolicitudSeleccionada = new JLabel("-");
    private final JLabel lblCanalIngresoSeleccionado = new JLabel("-");
    private final JLabel lblPrioridadSeleccionada = new JLabel("-");
    private final JLabel lblTipoActaSeleccionada = new JLabel("-");
    private final JLabel lblNumeroActaSeleccionada = new JLabel("-");
    private final JLabel lblTipoDocumentoTitularSeleccionado = new JLabel("-");
    private final JLabel lblTipoDocumentoSolicitanteSeleccionado = new JLabel("-");
    private final JLabel lblNumeroDocumentoSolicitanteSeleccionado = new JLabel("-");
    private final JLabel lblCorreoNotificacionSeleccionado = new JLabel("-");
    private final JLabel lblTelefonoNotificacionSeleccionado = new JLabel("-");
    private final JLabel lblDepartamentoSeleccionado = new JLabel("-");
    private final JLabel lblProvinciaSeleccionada = new JLabel("-");
    private final JLabel lblDistritoSeleccionado = new JLabel("-");
    private final JLabel lblDireccionNotificacionSeleccionada = new JLabel("-");
    private final JLabel lblActaSeleccionada = new JLabel("-");
    private final JLabel lblSolicitanteSeleccionado = new JLabel("-");
    private final JLabel lblDocumentoTitularSeleccionado = new JLabel("-");
    private final JLabel lblFechaSolicitudSeleccionada = new JLabel("-");
    private final BadgeV2 lblDiasSeleccionados = new BadgeV2("-", AppV2Theme.SOFT_GRAY, AppV2Theme.MUTED);
    private final JLabel lblFechaVencimientoSeleccionada = new JLabel("-");
    private final JLabel lblEstadoSeleccionado = new JLabel("-");
    private final JLabel lblHojaEnvioSeleccionada = new JLabel("-");
    private final JLabel lblObservacionSeleccionada = new JLabel("-");
    private final JLabel lblRecepcionAbogado = new JLabel("-");
    private final JLabel lblGrupoFamiliar = new JLabel("No");
    private final JLabel lblMarcaOperativaSeleccionada = new JLabel("No");
    private final JLabel lblOrigen = new JLabel("Registro / Registrado");
    private final JLabel lblDestino = new JLabel("Asignación / Asignado");
    private final JLabel lblIngreso = new JLabel("Normal");
    private final JLabel lblSupervisor = new JLabel("-");
    private final JLabel lblAbogadoAnalisisAsignacionEtiqueta = AppV2SideSectionPanel.buildLabel("Abogado");
    private final JLabel lblAbogadoAnalisisAsignacionValor = new JLabel("-");
    private final JLabel lblRelacionados = new JLabel("Sin alerta de relacionados.");
    private final JLabel lblExpedientePrincipalAsociacion = new JLabel("-");
    private final DefaultTableModel documentosRelacionadosModel = new DefaultTableModel(
            new Object[]{"", "N° expediente", "N° expediente SGD", "Estado", "Fecha Asociación", ""}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == 0) {
                return puedeAsociarDocumentoRelacionado(row);
            }
            return column == 5 && puedeEliminarDocumentoRelacionado(row);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : Object.class;
        }
    };
    private final JTable documentosRelacionadosTable = new AppV2Table(documentosRelacionadosModel);
    private JScrollPane documentosRelacionadosScroll;
    private JPanel documentosRelacionadosWrapper;
    private JPanel panelSolicitudesAsociadas;
    private final JLabel lblEstadoDeteccionGrupoFamiliar = new JLabel("Seleccione un expediente en la bandeja.");
    private final JLabel lblEstadoGrupoFamiliarActual = new JLabel("Sin grupo familiar.");
    private final JButton btnAsociarGrupoFamiliar = new JButton("Asociar al grupo familiar");
    private final DefaultTableModel integrantesGrupoFamiliarModel = new DefaultTableModel(
            new Object[]{"", "N° expediente", "Titular", "Etapa/Estado", "Abogado asignado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : Object.class;
        }
    };
    private final JTable integrantesGrupoFamiliarTable = new AppV2Table(integrantesGrupoFamiliarModel);
    private final DefaultTableModel grupoFamiliarActualModel = new DefaultTableModel(
            new Object[]{"Integrante", "N° expediente", "Etapa/Estado", "Abogado asignado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable grupoFamiliarActualTable = new AppV2Table(grupoFamiliarActualModel);
    private List<GrupoFamiliarCandidatoDTO> candidatosGrupoFamiliarActuales = new ArrayList<>();
    private AsignacionExpedienteDTO expedienteFocoGrupoFamiliar;
    private long secuenciaCargaGrupoFamiliar;
    private final DefaultTableModel asignacionMultipleModel = new DefaultTableModel(
            new Object[]{"Nro. Expediente", "N° expediente SGD", "Hoja de envío nueva", "Hoja de envío actual", "Abogado actual"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2;
        }
    };
    private final JTable asignacionMultipleTable = new AppV2Table(asignacionMultipleModel);
    private final JComboBox<EquipoItem> cmbEquipo = new JComboBox<EquipoItem>();
    private final JComboBox<UsuarioItem> cmbAbogado = new JComboBox<UsuarioItem>();
    private final JCheckBox chkModoReasignacion = new JCheckBox("Habilitar reasignación");
    private boolean modoReasignacion;
    private final DefaultTableModel historialAsignacionModel = new DefaultTableModel(
            new Object[]{"Tipo", "Abogado", "Equipo", "Hoja de envío", "Fecha", "Asignado por", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tablaHistorialAsignacion = new AppV2Table(historialAsignacionModel);
    private final AppV2TablePanel panelHistorialAsignacion = new AppV2TablePanel(
            tablaHistorialAsignacion, "Sin historial de asignación", "Aún no hay asignaciones registradas para este expediente.");
    private long secuenciaHistorialAsignacion;
    private CartaRespuestaTreeGridPanelV2 cartasRespuestaTreePanel;
    private final DefaultTableModel bandejaCartasRespuestaModel = new DefaultTableModel(
            new Object[]{
                "N° expediente",
                "N° expediente SGD",
                "Titular",
                "Tipo documento",
                "N° Documento",
                "Fecha Recepción",
                "Fecha Publicación",
                "Estado"
            }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bandejaCartasRespuestaTable = new AppV2Table(bandejaCartasRespuestaModel);
    private final AppV2TablePanel bandejaCartasRespuestaTablePanel = new AppV2TablePanel(
            bandejaCartasRespuestaTable,
            "Sin cartas de respuesta",
            "Aún no existen documentos analizados con respuesta pendiente.");
    private AppV2ColumnFilterSupport.Controller cartasRespuestaColumnFilterSupport;
    private final DefaultTableModel cargaLaboralModel = new DefaultTableModel(
            new Object[]{"Abogado", "Supervisor", "Equipo", "Asignadas", "Por vencer", "Vencidos", "En análisis"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable cargaLaboralTable = new AppV2Table(cargaLaboralModel);
    private final AppV2TablePanel cargaLaboralTablePanel = new AppV2TablePanel(
            cargaLaboralTable,
            "Sin abogados para mostrar",
            "No hay carga laboral disponible con los criterios actuales.");
    private final JLabel lblCargaLaboralEquipo = new JLabel("Carga laboral general de abogados activos.");
    private final JLabel lblCargaLaboralAyuda = new JLabel("Indicadores informativos para apoyar la asignación; no bloquean la decisión.");
    private final JTextField txtHojaEnvioAsignacion = new JTextField();
    private final JTextArea txtComentario = new JTextArea(3, 18);
    private final AppV2StackedSideTab tabDatosExpediente = crearTabAsignacion("Datos", new Color(230, 241, 245), new Color(57, 125, 199));
    private final AppV2StackedSideTab tabPanelAsignacionOperativa = crearTabAsignacion("Asignación", new Color(224, 243, 240), new Color(10, 118, 145));
    private final AppV2StackedSideTab tabPanelAsociar = crearTabAsignacion("Asociar", new Color(249, 239, 224), new Color(198, 121, 31));
    private final AppV2StackedSideTab tabPanelGrupoFamiliar = crearTabAsignacion("Grupo Familiar", new Color(224, 245, 232), new Color(35, 138, 94));
    private final AppV2StackedSideTab tabPanelRespuesta = crearTabAsignacion("Respuesta", new Color(240, 233, 249), new Color(110, 78, 164));
    private final AsignacionTableModel tableModel = new AsignacionTableModel();
    private final JTable table = new AppV2Table(tableModel);
    private final AppV2TablePanel tablePanel = new AppV2TablePanel(
            table,
            "Sin expedientes para mostrar",
            "Seleccione filtros y presione Buscar.");
    private AppV2ColumnFilterSupport.Controller columnFilterSupport;
    private final JPanel panelOperativo = new JPanel(new BorderLayout(8, 8));
    private final List<AsignacionExpedienteDTO> expedientes = new ArrayList<>();
    private final List<AsignacionTableRow> filasTabla = new ArrayList<>();
    private final List<AsignacionExpedienteDTO> expedientesAsignacionMultiple = new ArrayList<>();
    private final Map<Long, List<ExpedienteRelacionadoDTO>> asociadosCache = new HashMap<>();
    private final Map<Long, String> hojasEnvioAsignacionMultiple = new HashMap<>();
    private final Set<Long> principalesExpandidos = new HashSet<>();
    private final Set<Long> principalesCargando = new HashSet<>();
    private final List<DocumentoRelacionadoFila> documentosRelacionadosPanel = new ArrayList<>();
    private final List<CargaLaboralAbogadoDTO> cargasLaborales = new ArrayList<>();
    private final AtomicLong secuenciaBusqueda = new AtomicLong(0L);
    private volatile SwingWorker<?, ?> busquedaActiva;
    private final MetricCardV2 cardPendientes = new MetricCardV2("Pendientes", "0", "Para asignación", AppV2Theme.INFO);
    private final MetricCardV2 cardPotencialDuplicado = new MetricCardV2("Potencial duplicado", "0", "Acta + titular", AppV2Theme.WARNING);
    private final MetricCardV2 cardPosibleGrupoFamiliar = new MetricCardV2("Posible Grupo Familiar", "0", "Apellidos coincidentes", AppV2Theme.PRIMARY);
    private final MetricCardV2 cardGrupoFamiliarConfirmado = new MetricCardV2("Grupo Familiar Confirmado", "0", "Registrado", AppV2Theme.TEAL);
    private final MetricCardV2 cardPorVencer = new MetricCardV2("Por vencer", "0", "0 a 5 días hábiles", AppV2Theme.WARNING);
    private final MetricCardV2 cardVencidos = new MetricCardV2("Vencidos", "0", "Plazo excedido", AppV2Theme.ERROR);
    private final MetricCardV2 cardCartasTotal = new MetricCardV2("Cartas", "0", "Con respuesta requerida", AppV2Theme.INFO);
    private final MetricCardV2 cardCartasNotificadas = new MetricCardV2("Con acuse", "0", "Listas para respuesta", AppV2Theme.TEAL);
    private final MetricCardV2 cardCartasPendientes = new MetricCardV2("Pendientes", "0", "Sin confirmación final", AppV2Theme.WARNING);
    private final MetricCardV2 cardCartasPublicacion = new MetricCardV2("Publicación", "0", "Preparadas para publicar", AppV2Theme.INDIGO);
    private final MetricCardV2 cardCargaAbogados = new MetricCardV2("Abogados", "0", "Activos", AppV2Theme.INFO);
    private final MetricCardV2 cardCargaConAsignacion = new MetricCardV2("Con carga", "0", "Con solicitudes asignadas", AppV2Theme.TEAL);
    private final MetricCardV2 cardCargaSinAsignacion = new MetricCardV2("Sin carga", "0", "Disponibles", AppV2Theme.WARNING);
    private final MetricCardV2 cardCargaSolicitudes = new MetricCardV2("Solicitudes", "0", "Carga total asignada", AppV2Theme.INDIGO);
    private AppV2SideActionPanel panelAsignacion;
    private AppV2SideActionPanel panelDatosExpediente;
    private AppV2SideActionPanel panelAsociar;
    private AppV2SideActionPanel panelGrupoFamiliar;
    private AppV2SideActionPanel panelCartasRespuesta;
    private CardLayout panelAsignacionCardsLayout;
    private JPanel panelAsignacionCards;
    private String tabAsignacionActiva = TAB_DATOS_EXPEDIENTE;
    private String modoPanelLateral = MODO_PANEL_ASIGNACION;
    private JTabbedPane tabsBandejas;
    private AppV2SideSectionPanel sectionDatosExpediente;
    private AppV2SideSectionPanel sectionDatosSolicitud;
    private AppV2SideSectionPanel sectionDatosActa;
    private AppV2SideSectionPanel sectionDatosTitular;
    private AppV2SideSectionPanel sectionDatosSolicitante;
    private AppV2SideSectionPanel sectionDatosNotificacionUbicacion;
    private AppV2SideSectionPanel sectionResumenAsignacion;
    private AppV2SideSectionPanel sectionAsignacionMultiple;
    private AppV2SideSectionPanel sectionCartasRespuesta;
    private AppV2SideSectionPanel sectionAccionesRelacionados;
    private AppV2SideSectionPanel sectionDecisionNumero;
    private AppV2SideSectionPanel sectionFlujoAsignacion;
    private AppV2SideSectionPanel sectionDestinoAsignacion;
    private JScrollPane asignacionMultipleScroll;
    private AppV2SideSectionPanel sectionHojaEnvioAsignacion;
    private AppV2SideSectionPanel sectionComentarioAsignacion;
    private AppV2OperationalSplitPanel splitOperativo;
    private Color acentoSeleccion = GROUP_STRIPE_COLORS[0];
    private Color fondoSeleccion = TABLE_SELECTION_BACKGROUND;
    private Long idExpedienteExpansionActiva;
    private String contextoChip = "Panel de asignación";
    private boolean panelAsignacionVisible;
    private boolean panelAsignacionCerradoPorUsuario;
    private boolean todasVisiblesSeleccionadas;
    private boolean hayVisiblesAsignables;
    private boolean busquedaInicialEjecutada;
    private Long idExpedienteDocumentosRelacionados;
    private Long idExpedienteHojaEnvioSimple;
    private Long idExpedienteReasignacionActual;
    private Long idExpedienteCartasRespuesta;
    private final List<AsignacionCartaRespuestaDTO> cartasRespuestaPendientes = new ArrayList<AsignacionCartaRespuestaDTO>();
    private final List<AsignacionCartaRespuestaDTO> cartasRespuestaVisibles = new ArrayList<AsignacionCartaRespuestaDTO>();
    private AsignacionCartaRespuestaDTO cartaRespuestaSeleccionada;
    private FiltroKpiBandeja kpiBandejaActiva = FiltroKpiBandeja.TODOS;
    private FiltroKpiCartas kpiCartasActiva = FiltroKpiCartas.TODOS;
    private FiltroKpiCarga kpiCargaActiva = FiltroKpiCarga.TODOS;

    private boolean cargandoCombos;
    private boolean actualizandoSeleccion;
    private boolean usuarioActualResuelto;
    private Long idUsuarioActualSdrercApp;
    private Long idEquipoPendienteSeleccion;
    private Long idAbogadoPendienteSeleccion;
    private long secuenciaCargaAbogados;
    private long secuenciaCargaCartasRespuesta;

    public JPanelAsignacionV2() {
        this(new AsignacionExpedienteService(), new UsuarioAsignacionService());
    }

    public JPanelAsignacionV2(AsignacionExpedienteService asignacionService, UsuarioAsignacionService usuarioService) {
        this.asignacionService = asignacionService;
        this.usuarioService = usuarioService;
        setLayout(new BorderLayout(8, 8));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        add(crearCentro(), BorderLayout.CENTER);
        configurarTabla();
        configurarTablaDocumentosRelacionados();
        configurarTablaAsignacionMultiple();
        configurarTablaBandejaCartasRespuesta();
        configurarTablaCargaLaboral();
        configurarEventos();
        configurarKpisInteractivos();
        restaurarFechasBusqueda();
        cargarEstados();
        cargarEquipos();
        actualizarPanelSeleccion();
        actualizarModoBandejaActiva();
    }

    private JPanel crearHeader() {
        JPanel metricas = new AppV2ResponsiveGridPanel(190, 6, 12, 0);
        metricas.add(cardPendientes);
        metricas.add(cardPotencialDuplicado);
        metricas.add(cardPosibleGrupoFamiliar);
        metricas.add(cardGrupoFamiliarConfirmado);
        metricas.add(cardPorVencer);
        metricas.add(cardVencidos);
        return metricas;
    }

    private JPanel crearHeaderCartasRespuesta() {
        JPanel metricas = new AppV2ResponsiveGridPanel(190, 4, 12, 0);
        metricas.add(cardCartasTotal);
        metricas.add(cardCartasNotificadas);
        metricas.add(cardCartasPendientes);
        metricas.add(cardCartasPublicacion);
        return metricas;
    }

    private JPanel crearHeaderCargaAbogados() {
        JPanel metricas = new AppV2ResponsiveGridPanel(190, 4, 12, 0);
        metricas.add(cardCargaAbogados);
        metricas.add(cardCargaConAsignacion);
        metricas.add(cardCargaSinAsignacion);
        metricas.add(cardCargaSolicitudes);
        return metricas;
    }

    private JPanel crearCentro() {
        tabsBandejas = new JTabbedPane();
        tabsBandejas.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        tabsBandejas.addTab("Bandeja Asignación", crearContenidoBandejaAsignacion());
        tabsBandejas.addTab("Cartas de respuesta", crearContenidoBandejaCartasRespuesta());
        tabsBandejas.addTab("Carga Abogados", crearContenidoBandejaCargaAbogados());
        aplicarPermisoBandeja(
                TAB_BANDEJA_ASIGNACION, PERMISO_BANDEJA_ASIGNACION_LISTADO, "No tiene permiso para ver Bandeja Asignación.");
        aplicarPermisoBandeja(
                TAB_BANDEJA_CARTAS_RESPUESTA, PERMISO_BANDEJA_ASIGNACION_CARTAS_RESPUESTA,
                "No tiene permiso para ver Cartas de respuesta.");
        aplicarPermisoBandeja(
                TAB_BANDEJA_CARGA_ABOGADOS, PERMISO_BANDEJA_ASIGNACION_CARGA_ABOGADOS,
                "No tiene permiso para ver Carga Abogados.");

        panelDatosExpediente = crearPanelDatosExpediente();
        panelAsignacion = crearPanelAsignacionOperativa();
        panelAsociar = crearPanelAsociar();
        panelGrupoFamiliar = crearPanelGrupoFamiliar();
        panelCartasRespuesta = crearPanelCartasRespuesta();
        JPanel panelAsignacionConTab = crearPanelAsignacionConTab(
                panelDatosExpediente,
                panelAsignacion,
                panelAsociar,
                panelGrupoFamiliar,
                panelCartasRespuesta);
        splitOperativo = new AppV2OperationalSplitPanel(
                tabsBandejas,
                panelAsignacionConTab,
                0,
                PANEL_ASIGNACION_ANCHO_MINIMO + PANEL_ASIGNACION_TAB_OVERHANG,
                PANEL_ASIGNACION_ANCHO_NORMAL + PANEL_ASIGNACION_TAB_OVERHANG);
        splitOperativo.setOnExpandChanged(this::aplicarCompactacionKpisBandeja);

        panelOperativo.setOpaque(false);
        panelOperativo.add(splitOperativo, BorderLayout.CENTER);
        return panelOperativo;
    }

    private void aplicarPermisoBandeja(int indice, String codigoPermiso, String motivo) {
        if (tabsBandejas == null || indice < 0 || indice >= tabsBandejas.getTabCount()) {
            return;
        }
        if (!SessionContext.tienePermiso(codigoPermiso)) {
            tabsBandejas.setEnabledAt(indice, false);
            tabsBandejas.setToolTipTextAt(indice, motivo);
        }
    }

    private JPanel crearContenidoBandejaAsignacion() {
        JPanel contenidoPrincipal = new JPanel(new BorderLayout(4, 4));
        contenidoPrincipal.setOpaque(false);
        contenidoPrincipal.add(crearHeader(), BorderLayout.NORTH);

        JPanel contenidoOperativo = new JPanel(new BorderLayout(4, 4));
        contenidoOperativo.setOpaque(false);
        contenidoOperativo.add(crearBuscador(), BorderLayout.NORTH);
        contenidoOperativo.add(crearBandeja(), BorderLayout.CENTER);
        contenidoPrincipal.add(contenidoOperativo, BorderLayout.CENTER);
        return contenidoPrincipal;
    }

    private JPanel crearContenidoBandejaCartasRespuesta() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.add(crearHeaderCartasRespuesta(), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(crearBuscadorCartasRespuesta(), BorderLayout.NORTH);
        centro.add(crearBandejaCartasRespuesta(), BorderLayout.CENTER);
        panel.add(centro, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearBuscadorCartasRespuesta() {
        AppV2SearchToolbar toolbar = new AppV2SearchToolbar();
        JPanel acciones = AppV2ActionPanel.right();
        btnLimpiarCartasRespuesta.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        btnLimpiarCartasRespuesta.addActionListener(e -> limpiarBusquedaCartasRespuesta());
        acciones.add(btnLimpiarCartasRespuesta);
        toolbar.addSearchRow("Búsqueda", txtBusquedaCartasRespuesta, acciones);
        return toolbar;
    }

    private JPanel crearContenidoBandejaCargaAbogados() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.add(crearHeaderCargaAbogados(), BorderLayout.NORTH);
        panel.add(crearBandejaCargaAbogados(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearBuscador() {
        configurarControles();
        JPanel acciones = AppV2ActionPanel.right();
        acciones.add(btnBuscar);
        acciones.add(btnLimpiar);
        acciones.add(btnEditarRegistro);
        acciones.add(btnEliminarRegistro);
        return AppV2ExpedientePanelFactory.crearPanelBusquedaEstiloRegistro(
                "Búsqueda",
                txtBusqueda,
                acciones,
                fechaSolicitudDesde,
                fechaSolicitudHasta,
                cmbEstado,
                null,
                spnLimite);
    }

    private JPanel crearBandeja() {
        JPanel seleccion = AppV2ActionPanel.left();
        seleccion.add(lblSeleccionados);

        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);
        lblSeleccionados.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblSeleccionados.setForeground(AppV2Theme.PRIMARY);
        lblSeleccionadosPanel.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblSeleccionadosPanel.setForeground(AppV2Theme.PRIMARY);

        AppV2TableSectionPanel section = new AppV2TableSectionPanel(tablePanel);
        section.setActions(seleccion);
        section.setStatus(lblEstado);
        return section;
    }

    private JPanel crearBandejaCartasRespuesta() {
        JPanel acciones = AppV2ActionPanel.left();
        JLabel ayuda = new JLabel("Seleccione una carta para registrar su respuesta.");
        ayuda.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        ayuda.setForeground(AppV2Theme.PRIMARY);
        acciones.add(ayuda);

        AppV2TableSectionPanel section = new AppV2TableSectionPanel(bandejaCartasRespuestaTablePanel);
        section.setActions(acciones);
        section.setStatus(lblEstadoCartas);
        return section;
    }

    private JPanel crearBandejaCargaAbogados() {
        JPanel acciones = AppV2ActionPanel.left();
        lblCargaLaboralEquipo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblCargaLaboralEquipo.setForeground(AppV2Theme.PRIMARY);
        acciones.add(lblCargaLaboralEquipo);

        AppV2TableSectionPanel section = new AppV2TableSectionPanel(cargaLaboralTablePanel);
        section.setActions(acciones);
        section.setStatus(lblEstadoCarga);
        return section;
    }

    private AppV2SideActionPanel crearPanelDatosExpediente() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de datos", new Runnable() {
            @Override
            public void run() {
                cerrarPanelAsignacion();
            }
        });
        panel.setAccentColor(new Color(57, 125, 199));
        AppV2ResponsiveGridPanel secciones = new AppV2ResponsiveGridPanel(320, 2, 12, 12);
        secciones.add(crearDatosPlazoAsignacion());
        sectionDatosExpediente = crearDatosExpedienteAsignacion();
        sectionDatosActa = crearDatosActaAsignacion();
        sectionDatosSolicitud = crearDatosSolicitudAsignacion();
        sectionDatosTitular = crearDatosTitularAsignacion();
        sectionDatosSolicitante = crearDatosSolicitanteAsignacion();
        sectionDatosNotificacionUbicacion = crearDatosNotificacionUbicacionAsignacion();
        secciones.add(sectionDatosExpediente);
        secciones.add(sectionDatosActa);
        secciones.add(sectionDatosSolicitud);
        secciones.add(sectionDatosTitular);
        secciones.add(sectionDatosSolicitante);
        secciones.add(sectionDatosNotificacionUbicacion);
        panel.addSection(secciones);
        return panel;
    }

    private AppV2SideSectionPanel crearDatosPlazoAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del plazo");
        section.addRow("Días", lblDiasSeleccionados);
        section.addRow("Fecha Vencimiento", lblFechaVencimientoSeleccionada);
        return section;
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

    private AppV2SideSectionPanel crearDatosExpedienteAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del expediente");
        section.addRow("N° expediente", lblExpedienteSeleccionado);
        section.addRow("N° expediente SGD", lblExpedienteSgdSeleccionado);
        return section;
    }

    private AppV2SideActionPanel crearPanelAsignacionOperativa() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de asignación", new Runnable() {
            @Override
            public void run() {
                cerrarPanelAsignacion();
            }
        });
        panel.setAccentColor(new Color(10, 118, 145));
        sectionAsignacionMultiple = crearAsignacionMultiple();
        sectionDestinoAsignacion = crearDestinoAsignacion();
        panel.addSection(sectionAsignacionMultiple);
        panel.addSection(sectionDestinoAsignacion);
        panel.addSection(crearHistorialAsignacion());
        sectionAsignacionMultiple.setVisible(false);
        panel.setFooter(crearAccionesAsignacion());
        return panel;
    }

    private AppV2SideActionPanel crearPanelAsociar() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de Asociación", new Runnable() {
            @Override
            public void run() {
                cerrarPanelAsignacion();
            }
        });
        panel.setAccentColor(new Color(198, 121, 31));
        sectionResumenAsignacion = crearResumenAsignacion();
        sectionAccionesRelacionados = crearAccionesRelacionados();
        sectionDecisionNumero = crearDecisionNumero();
        panel.addSection(sectionResumenAsignacion);
        panel.addSection(sectionAccionesRelacionados);
        panel.addSection(sectionDecisionNumero);
        sectionDecisionNumero.setVisible(false);
        return panel;
    }

    private AppV2SideActionPanel crearPanelGrupoFamiliar() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Grupo Familiar", new Runnable() {
            @Override
            public void run() {
                cerrarPanelAsignacion();
            }
        });
        panel.setAccentColor(new Color(35, 138, 94));

        AppV2SideSectionPanel seccionDeteccion = new AppV2SideSectionPanel("Posibles integrantes");
        seccionDeteccion.addRow("Estado", lblEstadoDeteccionGrupoFamiliar);
        JPanel contentDeteccion = new JPanel(new BorderLayout(6, 6));
        contentDeteccion.setOpaque(false);
        JPanel encabezadoDeteccion = new JPanel();
        encabezadoDeteccion.setOpaque(false);
        encabezadoDeteccion.setLayout(new BoxLayout(encabezadoDeteccion, BoxLayout.Y_AXIS));
        JLabel tituloDeteccion = new JLabel("Coincidencias por apellidos del titular");
        tituloDeteccion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tituloDeteccion.setForeground(AppV2Theme.TEXT_PRIMARY);
        tituloDeteccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel ayudaDeteccion = new JLabel("<html>Marque las personas que realmente pertenecen a la misma familia. "
                + "Solo se asigna \"Sí\" a Grupo Familiar; no se asocian expedientes entre sí ni se hereda "
                + "número, equipo o abogado.</html>");
        ayudaDeteccion.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        ayudaDeteccion.setForeground(AppV2Theme.TEXT_SECONDARY);
        ayudaDeteccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        encabezadoDeteccion.add(tituloDeteccion);
        encabezadoDeteccion.add(ayudaDeteccion);
        contentDeteccion.add(encabezadoDeteccion, BorderLayout.NORTH);
        JScrollPane scrollIntegrantes = new JScrollPane(integrantesGrupoFamiliarTable);
        scrollIntegrantes.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scrollIntegrantes.setPreferredSize(new Dimension(300, 170));
        contentDeteccion.add(scrollIntegrantes, BorderLayout.CENTER);
        seccionDeteccion.addContent(contentDeteccion);
        panel.addSection(seccionDeteccion);

        AppV2SideSectionPanel seccionAccion = new AppV2SideSectionPanel("Asociación");
        AppV2Theme.estilizarBotonPrimario(btnAsociarGrupoFamiliar);
        btnAsociarGrupoFamiliar.setEnabled(false);
        btnAsociarGrupoFamiliar.setToolTipText("Asocia las personas marcadas al mismo grupo familiar.");
        btnAsociarGrupoFamiliar.addActionListener(e -> asociarGrupoFamiliarSeleccion());
        seccionAccion.addRow("Acción", btnAsociarGrupoFamiliar);
        panel.addSection(seccionAccion);

        AppV2SideSectionPanel seccionGrupoActual = new AppV2SideSectionPanel("Grupo familiar actual");
        seccionGrupoActual.addRow("Estado", lblEstadoGrupoFamiliarActual);
        JScrollPane scrollGrupoActual = new JScrollPane(grupoFamiliarActualTable);
        scrollGrupoActual.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scrollGrupoActual.setPreferredSize(new Dimension(300, 120));
        seccionGrupoActual.addContent(scrollGrupoActual);
        panel.addSection(seccionGrupoActual);

        integrantesGrupoFamiliarTable.setRowHeight(28);
        integrantesGrupoFamiliarTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        AppV2TableColumnSizer.applyWidths(integrantesGrupoFamiliarTable, 34, 130, 150, 130, 150);
        integrantesGrupoFamiliarModel.addTableModelListener(evento -> {
            if (evento.getColumn() == 0) {
                actualizarBotonAsociarGrupoFamiliar();
            }
        });

        grupoFamiliarActualTable.setRowHeight(26);
        grupoFamiliarActualTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        AppV2TableColumnSizer.applyWidths(grupoFamiliarActualTable, 150, 130, 130, 150);

        return panel;
    }

    private AppV2SideActionPanel crearPanelCartasRespuesta() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Cartas de respuesta", new Runnable() {
            @Override
            public void run() {
                cerrarPanelAsignacion();
            }
        });
        panel.setAccentColor(new Color(110, 78, 164));
        sectionCartasRespuesta = crearCartasRespuesta();
        panel.addSection(sectionCartasRespuesta);
        sectionCartasRespuesta.setVisible(false);
        return panel;
    }

    private void configurarTablaCargaLaboral() {
        lblCargaLaboralEquipo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        lblCargaLaboralEquipo.setForeground(AppV2Theme.TEXT_PRIMARY);
        lblCargaLaboralAyuda.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblCargaLaboralAyuda.setForeground(AppV2Theme.TEXT_SECONDARY);

        cargaLaboralTable.setRowHeight(30);
        cargaLaboralTable.setAutoCreateRowSorter(false);
        cargaLaboralTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        cargaLaboralTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cargaLaboralTable.getTableHeader().setReorderingAllowed(false);
        cargaLaboralTable.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        cargaLaboralTable.setDefaultRenderer(Object.class, new CargaLaboralRenderer());
        cargaLaboralTable.setGridColor(AppV2Theme.BORDER);
        cargaLaboralTable.setShowVerticalLines(false);
        cargaLaboralTable.setIntercellSpacing(new Dimension(0, 1));
        AppV2TableColumnSizer.applyWidths(cargaLaboralTable, 220, 180, 150, 92, 92, 92, 92);
        AppV2ColumnFilterSupport.install(
                "Asignacion.CargaLaboral",
                cargaLaboralTable,
                cargaLaboralTablePanel.getScrollPane(),
                cargaLaboralTablePanel.getScrollPane(),
                null);
    }

    private static int[] calcularPosicionesLenguetas(int count, int tabHeight, int gap, int containerHeight, int topMargin) {
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

    private AppV2StackedSideTab crearTabAsignacion(String label, Color idleColor, Color accentColor) {
        return new AppV2StackedSideTab(
                label,
                PANEL_ASIGNACION_TAB_OVERHANG - 6,
                ASIGNACION_TAB_HEIGHT,
                idleColor,
                accentColor,
                accentColor.darker());
    }

    private List<AppV2StackedSideTab> obtenerLenguetasVisibles() {
        List<AppV2StackedSideTab> tabs = new ArrayList<AppV2StackedSideTab>();
        if (MODO_PANEL_RESPUESTA.equals(modoPanelLateral)) {
            tabs.add(tabDatosExpediente);
            tabs.add(tabPanelRespuesta);
            return tabs;
        }
        if (MODO_PANEL_ASIGNACION.equals(modoPanelLateral)) {
            tabs.add(tabDatosExpediente);
            tabs.add(tabPanelAsignacionOperativa);
            tabs.add(tabPanelAsociar);
            tabs.add(tabPanelGrupoFamiliar);
        }
        return tabs;
    }

    private JPanel crearPanelAsignacionConTab(
            final AppV2SideActionPanel panelDatos,
            final AppV2SideActionPanel panelOperativo,
            final AppV2SideActionPanel panelAsociarActual,
            final AppV2SideActionPanel panelGrupoFamiliarActual,
            final AppV2SideActionPanel panelCartasActual) {
        JPanel wrapper = new JPanel(null) {
            @Override
            public void doLayout() {
                int width = getWidth();
                int height = getHeight();
                int railWidth = PANEL_ASIGNACION_TAB_OVERHANG;
                int availableWidth = Math.max(0, width - railWidth);
                int tabHeight = ASIGNACION_TAB_HEIGHT;
                int gap = 8;
                List<AppV2StackedSideTab> tabsVisibles = obtenerLenguetasVisibles();
                int[] positions = calcularPosicionesLenguetas(tabsVisibles.size(), tabHeight, gap, height, PANEL_ASIGNACION_TAB_TOP);
                for (int i = 0; i < tabsVisibles.size(); i++) {
                    tabsVisibles.get(i).setBounds(0, positions[i], PANEL_ASIGNACION_TAB_OVERHANG - 6, tabHeight);
                }
                panelAsignacionCards.setBounds(railWidth, 0, availableWidth, Math.max(0, height));
            }
        };
        wrapper.setOpaque(false);
        panelAsignacionCardsLayout = new CardLayout();
        panelAsignacionCards = new JPanel(panelAsignacionCardsLayout);
        panelAsignacionCards.setOpaque(false);
        panelAsignacionCards.add(panelDatos, TAB_DATOS_EXPEDIENTE);
        panelAsignacionCards.add(panelOperativo, TAB_PANEL_ASIGNACION);
        panelAsignacionCards.add(panelAsociarActual, TAB_PANEL_ASOCIAR);
        panelAsignacionCards.add(panelGrupoFamiliarActual, TAB_PANEL_GRUPO_FAMILIAR);
        panelAsignacionCards.add(panelCartasActual, TAB_PANEL_RESPUESTA);
        tabDatosExpediente.setToolTipText("Ver datos del expediente");
        tabPanelAsignacionOperativa.setToolTipText("Ver panel de asignación");
        tabPanelAsociar.setToolTipText("Ver panel para asociar expedientes");
        tabPanelGrupoFamiliar.setToolTipText("Ver panel de grupo familiar");
        tabPanelRespuesta.setToolTipText("Ver panel de respuesta");
        tabDatosExpediente.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabAsignacion(TAB_DATOS_EXPEDIENTE);
            }
        });
        tabPanelAsignacionOperativa.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabAsignacion(TAB_PANEL_ASIGNACION);
            }
        });
        tabPanelAsociar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabAsignacion(TAB_PANEL_ASOCIAR);
            }
        });
        tabPanelGrupoFamiliar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabAsignacion(TAB_PANEL_GRUPO_FAMILIAR);
            }
        });
        tabPanelRespuesta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarTabAsignacion(TAB_PANEL_RESPUESTA);
            }
        });
        wrapper.add(tabDatosExpediente);
        wrapper.add(tabPanelAsignacionOperativa);
        wrapper.add(tabPanelAsociar);
        wrapper.add(tabPanelGrupoFamiliar);
        wrapper.add(tabPanelRespuesta);
        wrapper.add(panelAsignacionCards);
        wrapper.setMinimumSize(new Dimension(
                PANEL_ASIGNACION_ANCHO_MINIMO + PANEL_ASIGNACION_TAB_OVERHANG,
                0));
        wrapper.setPreferredSize(new Dimension(
                PANEL_ASIGNACION_ANCHO_NORMAL + PANEL_ASIGNACION_TAB_OVERHANG,
                0));
        seleccionarTabAsignacion(TAB_DATOS_EXPEDIENTE);
        return wrapper;
    }

    private AppV2SideSectionPanel crearResumenAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Selección y alertas");
        section.addRow("Seleccionados", lblSeleccionadosPanel);
        section.addRow("Recepción", lblRecepcionAbogado);
        section.addRow("Grupo familiar", lblGrupoFamiliar);
        section.addRow("Alertas", lblRelacionados);
        panelSolicitudesAsociadas = crearPanelDocumentosRelacionados();
        panelSolicitudesAsociadas.setVisible(false);
        section.addContent(panelSolicitudesAsociadas);
        return section;
    }

    private AppV2SideSectionPanel crearDatosSolicitudAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos de solicitud");
        section.addRow("Fecha recepción", lblFechaSolicitudSeleccionada);
        section.addRow("Canal de ingreso", lblCanalIngresoSeleccionado);
        section.addRow("Nro. trámite web", lblTramiteWebSeleccionado);
        section.addRow("Proc.Registral", lblProcedimientoSeleccionado);
        section.addRow("Tipo documento", lblTipoDocumentoSeleccionado);
        section.addRow("N° documento", lblNumeroDocumentoSeleccionado);
        section.addRow("Tipo de solicitud", lblTipoSolicitudSeleccionada);
        section.addRow("Grupo familiar", lblMarcaOperativaSeleccionada);
        return section;
    }

    private AppV2SideSectionPanel crearDatosActaAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del acta");
        section.addRow("Tipo de acta", lblTipoActaSeleccionada);
        section.addRow("Nro. acta", lblNumeroActaSeleccionada);
        return section;
    }

    private AppV2SideSectionPanel crearDatosTitularAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del titular");
        section.addRow("Titular", lblTitularSeleccionado);
        section.addRow("Tipo documento", lblTipoDocumentoTitularSeleccionado);
        section.addRow("Número documento", lblDocumentoTitularSeleccionado);
        return section;
    }

    private AppV2SideSectionPanel crearDatosSolicitanteAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos del solicitante");
        section.addRow("Solicitante", lblSolicitanteSeleccionado);
        section.addRow("Tipo documento", lblTipoDocumentoSolicitanteSeleccionado);
        section.addRow("Número documento", lblNumeroDocumentoSolicitanteSeleccionado);
        return section;
    }

    private AppV2SideSectionPanel crearDatosNotificacionUbicacionAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Datos de notificación y ubicación");
        section.addRow("Correo", lblCorreoNotificacionSeleccionado);
        section.addRow("Teléfono", lblTelefonoNotificacionSeleccionado);
        section.addRow("Departamento", lblDepartamentoSeleccionado);
        section.addRow("Provincia", lblProvinciaSeleccionada);
        section.addRow("Distrito", lblDistritoSeleccionado);
        section.addRow("Dirección", lblDireccionNotificacionSeleccionada);
        return section;
    }

    private AppV2SideSectionPanel crearAsignacionMultiple() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Asignación de abogado");
        JLabel ayuda = new JLabel("Revise los expedientes antes de generar la asignación.");
        ayuda.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        ayuda.setForeground(AppV2Theme.TEXT_SECONDARY);

        chkModoReasignacion.setOpaque(false);
        chkModoReasignacion.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        chkModoReasignacion.setToolTipText(
                "Permite marcar en el listado expedientes ya asignados, para reasignarlos con una nueva hoja de envío.");
        chkModoReasignacion.addActionListener(e -> alternarModoReasignacion());

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        ayuda.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkModoReasignacion.setAlignmentX(Component.LEFT_ALIGNMENT);
        encabezado.add(ayuda);
        encabezado.add(chkModoReasignacion);

        JPanel content = new JPanel(new BorderLayout(6, 6));
        content.setOpaque(false);
        content.add(encabezado, BorderLayout.NORTH);
        asignacionMultipleScroll = new JScrollPane(asignacionMultipleTable);
        asignacionMultipleScroll.setPreferredSize(new Dimension(320, 170));
        asignacionMultipleScroll.setMinimumSize(new Dimension(280, 120));
        asignacionMultipleScroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        asignacionMultipleScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        asignacionMultipleScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        content.add(asignacionMultipleScroll, BorderLayout.CENTER);
        section.addContent(content);
        ajustarTamanoAsignacionMultiple();
        return section;
    }

    private AppV2SideSectionPanel crearCartasRespuesta() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Cartas de Rpta");
        cartasRespuestaTreePanel = new CartaRespuestaTreeGridPanelV2();
        cartasRespuestaTreePanel.setHandlers(
                carta -> documentoAnalisisService.guardarCartaRespuesta(idExpedienteCartasRespuesta, carta),
                () -> cargarCartasRespuestaPorDocumento(idExpedienteCartasRespuesta, null));
        section.addContent(cartasRespuestaTreePanel);
        return section;
    }

    private JPanel crearPanelDocumentosRelacionados() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setOpaque(false);

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Solicitudes asociadas");
        titulo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        titulo.setForeground(AppV2Theme.TEXT_PRIMARY);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel ayuda = new JLabel("<html>Marque el/los expedientes <b>sin número</b> (potenciales duplicados) "
                + "para asociarlos al expediente principal, que es el que ya tiene número.</html>");
        ayuda.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        ayuda.setForeground(AppV2Theme.TEXT_SECONDARY);
        ayuda.setAlignmentX(Component.LEFT_ALIGNMENT);
        encabezado.add(titulo);
        encabezado.add(ayuda);
        panel.add(encabezado, BorderLayout.NORTH);

        documentosRelacionadosScroll = new JScrollPane(documentosRelacionadosTable);
        documentosRelacionadosScroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        documentosRelacionadosScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        documentosRelacionadosScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        documentosRelacionadosWrapper = new JPanel(new BorderLayout(0, 0));
        documentosRelacionadosWrapper.setOpaque(false);
        documentosRelacionadosWrapper.add(documentosRelacionadosScroll, BorderLayout.WEST);

        panel.add(documentosRelacionadosWrapper, BorderLayout.CENTER);
        ajustarTamanoDocumentosRelacionados();
        return panel;
    }

    private AppV2SideSectionPanel crearAccionesRelacionados() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Asociación rápida");
        section.addRow("Expediente principal", lblExpedientePrincipalAsociacion);
        AppV2Theme.estilizarBotonPrimario(btnAsociarRelacionados);
        btnAsociarRelacionados.setEnabled(false);
        btnAsociarRelacionados.setToolTipText("Asociar expedientes con el mismo número de acta y titular.");
        section.addRow("Acción", btnAsociarRelacionados);
        return section;
    }

    private AppV2SideSectionPanel crearDecisionNumero() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Decisión de número");
        AppV2Theme.estilizarBotonPrimario(btnGenerarNumeroExpediente);
        btnGenerarNumeroExpediente.setEnabled(false);
        btnGenerarNumeroExpediente.setToolTipText("Generar número de expediente para Reconsideración/Apelación sin número.");
        section.addRow("Acción", btnGenerarNumeroExpediente);
        return section;
    }

    private AppV2SideSectionPanel crearHistorialAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Historial de asignación / reasignación");
        tablaHistorialAsignacion.setRowHeight(28);
        tablaHistorialAsignacion.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        AppV2TableColumnSizer.applyWidths(tablaHistorialAsignacion, 130, 160, 160, 130, 140, 160, 110);
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setPreferredSize(new Dimension(320, 180));
        content.add(panelHistorialAsignacion, BorderLayout.CENTER);
        section.addContent(content);
        return section;
    }

    private void cargarHistorialAsignacion(Long idExpediente) {
        idExpedienteReasignacionActual = idExpediente;
        historialAsignacionModel.setRowCount(0);
        panelHistorialAsignacion.setEmpty(true);
        if (idExpediente == null) {
            return;
        }
        final long solicitud = ++secuenciaHistorialAsignacion;
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.AsignacionHistorialDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.AsignacionHistorialDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.AsignacionHistorialDTO> doInBackground() throws Exception {
                return asignacionService.listarHistorialAsignaciones(idExpediente);
            }

            @Override
            protected void done() {
                if (solicitud != secuenciaHistorialAsignacion || !idExpediente.equals(idExpedienteReasignacionActual)) {
                    return;
                }
                try {
                    List<com.sdrerc.domain.dto.sdrercapp.AsignacionHistorialDTO> items = get();
                    for (com.sdrerc.domain.dto.sdrercapp.AsignacionHistorialDTO item : items) {
                        historialAsignacionModel.addRow(new Object[]{
                                item.isReasignacionExcepcional() ? "Reasignación" : "Asignación inicial",
                                item.getAbogado().isEmpty() ? "-" : item.getAbogado(),
                                item.getEquipo().isEmpty() ? "-" : item.getEquipo(),
                                item.getNumeroHojaEnvio().isEmpty() ? "-" : item.getNumeroHojaEnvio(),
                                item.getFechaAsignacion() == null ? "-" : item.getFechaAsignacion().format(DATE_HORA_FORMAT),
                                item.getAsignadoPor().isEmpty() ? "-" : item.getAsignadoPor(),
                                item.isActiva() ? "Activa" : "Histórica"
                        });
                    }
                    panelHistorialAsignacion.setEmpty(items.isEmpty());
                } catch (Exception ex) {
                    mostrarError("No se pudo cargar el historial de asignación.", ex);
                }
            }
        };
        worker.execute();
    }

    private AppV2SideSectionPanel crearFlujoAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Flujo operativo");
        section.addRow("Origen", lblOrigen);
        section.addRow("Destino", lblDestino);
        section.addRow("Ingreso", lblIngreso);
        return section;
    }

    private AppV2SideSectionPanel crearDestinoAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Destino operativo");
        section.addRow(lblAbogadoAnalisisAsignacionEtiqueta, lblAbogadoAnalisisAsignacionValor);
        section.addRow("Equipo destino", cmbEquipo);
        section.addRow("Abogado responsable", cmbAbogado);
        section.addRow("Supervisor", lblSupervisor);
        return section;
    }

    private AppV2SideSectionPanel crearHojaEnvioAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Hoja de envío");
        section.addRow("N° hoja de envío", txtHojaEnvioAsignacion);
        return section;
    }

    private AppV2SideSectionPanel crearComentarioAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Comentario");
        section.addContent(scrollComentario());
        return section;
    }

    private JPanel crearAccionesAsignacion() {
        JPanel acciones = new JPanel(new GridBagLayout());
        acciones.setOpaque(false);
        acciones.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 0, 0, 8);
        acciones.add(btnAsignarSeleccionado, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 0);
        acciones.add(btnAsignarSeleccionados, gbc);
        return acciones;
    }

    private JScrollPane scrollComentario() {
        txtComentario.setLineWrap(true);
        txtComentario.setWrapStyleWord(true);
        txtComentario.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        txtComentario.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane scroll = new JScrollPane(txtComentario);
        scroll.setPreferredSize(new Dimension(260, 84));
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        return scroll;
    }

    private void configurarControles() {
        txtBusqueda.setPreferredSize(new Dimension(720, 36));
        txtBusqueda.setMinimumSize(new Dimension(360, 36));
        txtBusquedaCartasRespuesta.setPreferredSize(new Dimension(720, 36));
        txtBusquedaCartasRespuesta.setMinimumSize(new Dimension(360, 36));
        Dimension fechaSize = new Dimension(250, 42);
        fechaSolicitudDesde.setPreferredSize(fechaSize);
        fechaSolicitudDesde.setMinimumSize(new Dimension(210, 42));
        fechaSolicitudHasta.setPreferredSize(fechaSize);
        fechaSolicitudHasta.setMinimumSize(new Dimension(210, 42));
        cmbEstado.setPreferredSize(new Dimension(190, 34));
        cmbEstado.setMinimumSize(new Dimension(180, 34));
        spnLimite.setPreferredSize(new Dimension(88, 34));
        cmbEquipo.setPreferredSize(new Dimension(230, 34));
        cmbAbogado.setPreferredSize(new Dimension(230, 34));
        txtBusqueda.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        txtBusquedaCartasRespuesta.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        cmbEstado.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        cmbEquipo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        cmbAbogado.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        txtHojaEnvioAsignacion.setPreferredSize(new Dimension(230, 34));
        txtHojaEnvioAsignacion.setMinimumSize(new Dimension(180, 34));
        txtHojaEnvioAsignacion.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        txtHojaEnvioAsignacion.setToolTipText("Número de hoja de envío de la asignación.");
        configurarLabelsDatosExpediente();
        lblRecepcionAbogado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblGrupoFamiliar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblAbogadoAnalisisAsignacionValor.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        AppV2Theme.estilizarBotonPrimario(btnBuscar);
        AppV2Theme.estilizarBotonPrimario(btnAsignarSeleccionado);
        AppV2Theme.estilizarBotonSecundario(btnAsignarSeleccionados);
        AppV2Theme.estilizarBotonPrimario(btnGenerarNumeroExpediente);
        btnGenerarNumeroExpediente.setEnabled(false);
        btnGenerarNumeroExpediente.setToolTipText("Disponible solo para Reconsideración/Apelación registrada sin número.");
        btnEditarRegistro.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnEditarRegistro.setToolTipText("Disponible solo para expedientes Registrados sin asignación a abogado.");
        btnEliminarRegistro.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnEliminarRegistro.setToolTipText("Disponible solo para expedientes Registrados sin asignación a abogado.");
    }

    private void configurarLabelsDatosExpediente() {
        JLabel[] labels = new JLabel[]{
            lblExpedienteSeleccionado,
            lblExpedienteSgdSeleccionado,
            lblTitularSeleccionado,
            lblDocumentoTitularSeleccionado,
            lblSolicitanteSeleccionado,
            lblTramiteWebSeleccionado,
            lblNumeroDocumentoSeleccionado,
            lblTipoDocumentoSeleccionado,
            lblProcedimientoSeleccionado,
            lblActaSeleccionada,
            lblFechaSolicitudSeleccionada,
            lblEstadoSeleccionado,
            lblHojaEnvioSeleccionada,
            lblObservacionSeleccionada,
            lblCorreoNotificacionSeleccionado,
            lblTelefonoNotificacionSeleccionado,
            lblDepartamentoSeleccionado,
            lblProvinciaSeleccionada,
            lblDistritoSeleccionado,
            lblDireccionNotificacionSeleccionada,
            lblMarcaOperativaSeleccionada
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
        table.getTableHeader().setResizingAllowed(true);
        table.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        table.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        table.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        table.setGridColor(AppV2Theme.BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setDefaultRenderer(Object.class, new AsignacionRenderer());
        tablePanel.getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        AppV2TableColumnSizer.applyFriendlyDefaults(table);
        AppV2TableColumnSizer.applyWidths(table, 42, 34, 84, 185, 155, 135, 135, 230, 135, 130, 260, 240, 155, 170, 0);
        configurarColumna(table.getColumnModel().getColumn(COL_EXPANDIR), 42, 40, 46);
        configurarColumna(table.getColumnModel().getColumn(COL_SELECCION), 34, 32, 36);
        configurarColumna(table.getColumnModel().getColumn(COL_DIAS), 84, 78, 92);
        configurarColumna(table.getColumnModel().getColumn(3), 185, 165, 260);
        configurarColumna(table.getColumnModel().getColumn(4), 155, 140, 220);
        configurarColumna(table.getColumnModel().getColumn(5), 135, 128, 170);
        configurarColumna(table.getColumnModel().getColumn(6), 135, 128, 170);
        configurarColumna(table.getColumnModel().getColumn(7), 230, 210, 320);
        configurarColumna(table.getColumnModel().getColumn(8), 135, 128, 190);
        configurarColumna(table.getColumnModel().getColumn(9), 130, 120, 180);
        configurarColumna(table.getColumnModel().getColumn(10), 260, 230, 380);
        configurarColumna(table.getColumnModel().getColumn(11), 240, 210, 360);
        configurarColumna(table.getColumnModel().getColumn(COL_ESTADO), 155, 145, 220);
        configurarColumna(table.getColumnModel().getColumn(COL_RELACIONADOS), 170, 150, 240);
        configurarColumna(table.getColumnModel().getColumn(COL_ID), 0, 0, 0);
        table.getColumnModel().getColumn(COL_EXPANDIR).setCellRenderer(new ExpandirRenderer());
        table.getColumnModel().getColumn(COL_SELECCION).setCellRenderer(new SeleccionRenderer());
        columnFilterSupport = AppV2ColumnFilterSupport.install(
                "Asignacion",
                table,
                tablePanel.getScrollPane(),
                tablePanel,
                () -> contraerTodosExcepto(null),
                COL_EXPANDIR,
                COL_SELECCION,
                COL_ID);
        table.getColumnModel().getColumn(COL_SELECCION).setHeaderRenderer(new SelectAllHeaderRenderer());
        SwingUtilities.invokeLater(this::instalarListenerCabeceraSeleccionAsignacion);
        table.getRowSorter().addRowSorterListener(e -> SwingUtilities.invokeLater(
                this::actualizarEstadoHeaderSeleccion));
    }

    private void instalarListenerCabeceraSeleccionAsignacion() {
        if (tablePanel == null || tablePanel.getScrollPane() == null
                || tablePanel.getScrollPane().getColumnHeader() == null
                || tablePanel.getScrollPane().getColumnHeader().getView() == null) {
            return;
        }
        Component headerView = tablePanel.getScrollPane().getColumnHeader().getView();
        if (!(headerView instanceof javax.swing.JComponent)) {
            return;
        }
        javax.swing.JComponent component = (javax.swing.JComponent) headerView;
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getY() >= 30) {
                    return;
                }
                int offset = 0;
                for (int viewColumn = 0; viewColumn < table.getColumnModel().getColumnCount(); viewColumn++) {
                    int width = table.getColumnModel().getColumn(viewColumn).getWidth();
                    if (e.getX() >= offset && e.getX() < offset + width) {
                        if (table.convertColumnIndexToModel(viewColumn) == COL_SELECCION) {
                            alternarSeleccionVisibleDesdeHeader();
                        }
                        return;
                    }
                    offset += width;
                }
            }
        });
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                if (column >= 0 && table.convertColumnIndexToModel(column) == COL_SELECCION) {
                    alternarSeleccionVisibleDesdeHeader();
                }
            }
        });
    }

    private void configurarTablaDocumentosRelacionados() {
        documentosRelacionadosTable.setRowHeight(30);
        documentosRelacionadosTable.setAutoCreateRowSorter(false);
        documentosRelacionadosTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        documentosRelacionadosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        documentosRelacionadosTable.getTableHeader().setReorderingAllowed(false);
        documentosRelacionadosTable.getTableHeader().setResizingAllowed(false);
        documentosRelacionadosTable.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        documentosRelacionadosTable.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        documentosRelacionadosTable.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        documentosRelacionadosTable.setGridColor(AppV2Theme.BORDER);
        documentosRelacionadosTable.setShowVerticalLines(false);
        documentosRelacionadosTable.setIntercellSpacing(new Dimension(0, 1));
        documentosRelacionadosTable.setDefaultRenderer(Object.class, new DocumentoRelacionadoRenderer());
        documentosRelacionadosTable.getColumnModel().getColumn(0).setPreferredWidth(34);
        documentosRelacionadosTable.getColumnModel().getColumn(0).setMinWidth(30);
        documentosRelacionadosTable.getColumnModel().getColumn(0).setMaxWidth(38);
        documentosRelacionadosTable.getColumnModel().getColumn(0).setCellRenderer(new SeleccionDocumentoRelacionadoRenderer());
        documentosRelacionadosTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        documentosRelacionadosTable.getColumnModel().getColumn(1).setMinWidth(100);
        documentosRelacionadosTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        documentosRelacionadosTable.getColumnModel().getColumn(2).setMinWidth(120);
        documentosRelacionadosTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        documentosRelacionadosTable.getColumnModel().getColumn(3).setMinWidth(100);
        documentosRelacionadosTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        documentosRelacionadosTable.getColumnModel().getColumn(4).setMinWidth(100);
        documentosRelacionadosTable.getColumnModel().getColumn(5).setPreferredWidth(58);
        documentosRelacionadosTable.getColumnModel().getColumn(5).setMinWidth(54);
        documentosRelacionadosTable.getColumnModel().getColumn(5).setMaxWidth(64);
        documentosRelacionadosTable.getColumnModel().getColumn(5).setCellRenderer(new EliminarDocumentoRenderer());
        documentosRelacionadosTable.getColumnModel().getColumn(5).setCellEditor(new EliminarDocumentoEditor());
        documentosRelacionadosModel.addTableModelListener(evento -> {
            if (evento.getColumn() == 0) {
                actualizarBotonAsociarPorSeleccionTabla();
            }
        });
        ajustarTamanoDocumentosRelacionados();
    }

    private void configurarTablaAsignacionMultiple() {
        asignacionMultipleTable.setRowHeight(32);
        asignacionMultipleTable.setAutoCreateRowSorter(false);
        asignacionMultipleTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        asignacionMultipleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        asignacionMultipleTable.getTableHeader().setReorderingAllowed(false);
        asignacionMultipleTable.getTableHeader().setResizingAllowed(true);
        asignacionMultipleTable.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        asignacionMultipleTable.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        asignacionMultipleTable.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        asignacionMultipleTable.setGridColor(AppV2Theme.BORDER);
        asignacionMultipleTable.setShowVerticalLines(false);
        asignacionMultipleTable.setIntercellSpacing(new Dimension(0, 1));
        asignacionMultipleTable.setDefaultRenderer(Object.class, new AsignacionMultipleRenderer());
        asignacionMultipleTable.getColumnModel().getColumn(0).setPreferredWidth(175);
        asignacionMultipleTable.getColumnModel().getColumn(0).setMinWidth(150);
        asignacionMultipleTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        asignacionMultipleTable.getColumnModel().getColumn(1).setMinWidth(130);
        asignacionMultipleTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        asignacionMultipleTable.getColumnModel().getColumn(2).setMinWidth(140);
        asignacionMultipleTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        asignacionMultipleTable.getColumnModel().getColumn(3).setMinWidth(140);
        asignacionMultipleTable.getColumnModel().getColumn(4).setPreferredWidth(175);
        asignacionMultipleTable.getColumnModel().getColumn(4).setMinWidth(150);
    }

    private void configurarTablaBandejaCartasRespuesta() {
        bandejaCartasRespuestaTable.setRowHeight(34);
        bandejaCartasRespuestaTable.setAutoCreateRowSorter(false);
        bandejaCartasRespuestaTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        bandejaCartasRespuestaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bandejaCartasRespuestaTable.getTableHeader().setReorderingAllowed(false);
        bandejaCartasRespuestaTable.getTableHeader().setResizingAllowed(true);
        bandejaCartasRespuestaTable.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        bandejaCartasRespuestaTable.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        bandejaCartasRespuestaTable.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        bandejaCartasRespuestaTable.setGridColor(AppV2Theme.BORDER);
        bandejaCartasRespuestaTable.setShowVerticalLines(false);
        bandejaCartasRespuestaTable.setIntercellSpacing(new Dimension(0, 1));
        bandejaCartasRespuestaTable.setDefaultRenderer(Object.class, new CartaRespuestaPendienteRenderer());
        AppV2TableColumnSizer.applyWidths(
                bandejaCartasRespuestaTable,
                165, 150, 220, 170, 140, 96, 130, 260);
        cartasRespuestaColumnFilterSupport = AppV2ColumnFilterSupport.install(
                    "Asignacion.BandejaCartasRespuesta",
                    bandejaCartasRespuestaTable,
                    bandejaCartasRespuestaTablePanel.getScrollPane(),
                    bandejaCartasRespuestaTablePanel.getScrollPane(),
                    null);
    }

    private void configurarColumna(TableColumn column, int preferred, int min, int max) {
        column.setPreferredWidth(preferred);
        column.setMinWidth(min);
        column.setMaxWidth(max);
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

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnEditarRegistro.addActionListener(e -> editarRegistroSeleccionado());
        btnEliminarRegistro.addActionListener(e -> eliminarRegistroSeleccionado());
        txtBusquedaCartasRespuesta.getDocument().addDocumentListener(simpleDocumentListener(this::aplicarBusquedaCartasRespuesta));
        btnLimpiarCartasRespuesta.addActionListener(e -> limpiarBusquedaCartasRespuesta());
        btnAsociarRelacionados.addActionListener(e -> asociarRelacionadosRapido());
        btnGenerarNumeroExpediente.addActionListener(e -> generarNumeroExpedienteSeleccionado());
        btnAsignarSeleccionado.addActionListener(e -> generarAsignacionDesdePanel());
        btnAsignarSeleccionados.addActionListener(e -> cancelarSeleccionAsignacion());
        cmbEquipo.addActionListener(e -> {
            if (!cargandoCombos) {
                idEquipoPendienteSeleccion = null;
                idAbogadoPendienteSeleccion = null;
                cargarAbogados();
            }
        });
        cmbAbogado.addActionListener(e -> actualizarSupervisor());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarPanelSeleccion();
            }
        });
        bandejaCartasRespuestaTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarPanelCartasRespuestaSeleccion();
            }
        });
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == COL_SELECCION && !actualizandoSeleccion) {
                actualizarPanelSeleccion();
            }
        });
        if (tabsBandejas != null) {
            tabsBandejas.addChangeListener(e -> actualizarModoBandejaActiva());
        }
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewColumn = table.columnAtPoint(e.getPoint());
                int viewRow = table.rowAtPoint(e.getPoint());
                if (viewRow >= 0
                        && viewColumn >= 0
                        && table.convertColumnIndexToModel(viewColumn) == COL_EXPANDIR) {
                    alternarExpansionFila(table.convertRowIndexToModel(viewRow));
                    return;
                }
                if (viewRow >= 0
                        && viewColumn >= 0
                        && table.convertColumnIndexToModel(viewColumn) == COL_SELECCION
                        && !modoReasignacion) {
                    AsignacionTableRow row = filaTabla(table.convertRowIndexToModel(viewRow));
                    if (row != null && row.esPrincipal() && !row.esAsignable() && row.principal.isAsignacionActiva()) {
                        mostrarInfo("Este expediente ya está asignado. Active \"Habilitar reasignación\" para poder marcarlo.");
                        return;
                    }
                }
                if (e.getClickCount() == 2 && viewRow >= 0) {
                    abrirPanelAsignacion();
                }
            }
        });
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                if (column >= 0 && table.convertColumnIndexToModel(column) == COL_SELECCION) {
                    alternarSeleccionVisibleDesdeHeader();
                }
            }
        });
    }

    private void cargarEstados() {
        EstadoExpedienteComboSupportV2.cargarPorCodigos(
                cmbEstado,
                new FiltroCatalogoItemV2(null, "Todos los estados"),
                (codigo, nombre) -> new FiltroCatalogoItemV2(codigo, nombre),
                ex -> lblEstado.setText("No se pudieron cargar los estados de Asignación."),
                "REGISTRADO",
                "ASIGNADO");
    }

    /**
     * El combo "Equipo destino" del bloque "Destino operativo" (Asignacion de abogado) solo
     * debe ofrecer el equipo de Analisis, unico destino valido para esta accion.
     */
    private boolean esEquipoDestinoAsignacionValido(String codigo) {
        return "EQ_ANALISIS".equalsIgnoreCase(codigo);
    }

    private void cargarEquipos() {
        cargandoCombos = true;
        cmbEquipo.removeAllItems();
        cmbEquipo.addItem(EquipoItem.placeholder("Seleccione equipo"));
        cmbAbogado.removeAllItems();
        cmbAbogado.addItem(UsuarioItem.placeholder("Seleccione abogado"));
        setTrabajando(true, "Cargando equipos y abogados...");
        SwingWorker<List<EquipoAsignacionDTO>, Void> worker = new SwingWorker<List<EquipoAsignacionDTO>, Void>() {
            @Override
            protected List<EquipoAsignacionDTO> doInBackground() throws Exception {
                return usuarioService.listarEquiposActivos();
            }

            @Override
            protected void done() {
                try {
                    for (EquipoAsignacionDTO equipo : get()) {
                        if (esEquipoDestinoAsignacionValido(equipo.getCodigo())) {
                            cmbEquipo.addItem(new EquipoItem(equipo));
                        }
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los equipos de asignación.", ex);
                } finally {
                    cargandoCombos = false;
                    aplicarAsignacionPendienteEnCombos();
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

    private void cargarAbogados() {
        EquipoAsignacionDTO equipo = obtenerEquipoSeleccionado();
        final long solicitud = ++secuenciaCargaAbogados;
        final Long idAbogadoObjetivo = idAbogadoPendienteSeleccion;
        idAbogadoPendienteSeleccion = null;
        cmbAbogado.removeAllItems();
        cmbAbogado.addItem(UsuarioItem.placeholder("Seleccione abogado"));
        lblSupervisor.setText("-");
        if (equipo == null) {
            setTrabajando(false, null);
            return;
        }
        final Long idEquipoSolicitado = equipo.getIdEquipo();
        setTrabajando(true, "Cargando abogados del equipo destino...");
        SwingWorker<List<UsuarioAsignableDTO>, Void> worker = new SwingWorker<List<UsuarioAsignableDTO>, Void>() {
            @Override
            protected List<UsuarioAsignableDTO> doInBackground() throws Exception {
                return usuarioService.listarAbogadosAsignables(idEquipoSolicitado);
            }

            @Override
            protected void done() {
                EquipoAsignacionDTO equipoActual = obtenerEquipoSeleccionado();
                if (solicitud != secuenciaCargaAbogados
                        || equipoActual == null
                        || !idEquipoSolicitado.equals(equipoActual.getIdEquipo())) {
                    return;
                }
                try {
                    List<UsuarioAsignableDTO> abogados = get();
                    cargandoCombos = true;
                    for (UsuarioAsignableDTO abogado : abogados) {
                        cmbAbogado.addItem(new UsuarioItem(abogado));
                    }
                    seleccionarAbogadoPorId(idAbogadoObjetivo);
                    if (abogados.isEmpty()) {
                        lblEstado.setText("No hay abogados activos asociados al equipo seleccionado.");
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los abogados responsables.", ex);
                } finally {
                    cargandoCombos = false;
                    actualizarSupervisor();
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void mostrarAsignacionActual(
            String equipoNombre,
            Long idEquipo,
            String abogadoNombre,
            Long idAbogado) {
        boolean tieneEquipo = equipoNombre != null && !equipoNombre.trim().isEmpty();
        lblAbogadoAnalisisAsignacionEtiqueta.setText(tieneEquipo ? "Abogado " + equipoNombre.trim() : "Abogado");
        lblAbogadoAnalisisAsignacionValor.setText(textoAsignacion(abogadoNombre));
        lblAbogadoAnalisisAsignacionValor.setToolTipText(textoAsignacion(abogadoNombre));
        idEquipoPendienteSeleccion = idEquipo;
        idAbogadoPendienteSeleccion = idAbogado;
        aplicarAsignacionPendienteEnCombos();
    }

    private void aplicarAsignacionPendienteEnCombos() {
        if (cargandoCombos) {
            return;
        }
        Long idEquipo = idEquipoPendienteSeleccion;
        Long idAbogado = idAbogadoPendienteSeleccion;
        idEquipoPendienteSeleccion = null;
        if (idEquipo == null) {
            cargandoCombos = true;
            try {
                if (cmbEquipo.getItemCount() > 0) {
                    cmbEquipo.setSelectedIndex(0);
                }
                cmbAbogado.removeAllItems();
                cmbAbogado.addItem(UsuarioItem.placeholder("Seleccione abogado"));
                lblSupervisor.setText("-");
            } finally {
                cargandoCombos = false;
            }
            idAbogadoPendienteSeleccion = null;
            return;
        }
        EquipoAsignacionDTO equipoActual = obtenerEquipoSeleccionado();
        if (equipoActual != null && idEquipo.equals(equipoActual.getIdEquipo())) {
            idAbogadoPendienteSeleccion = null;
            if (idAbogado == null || seleccionarAbogadoPorId(idAbogado)) {
                actualizarSupervisor();
                return;
            }
        }
        if (!seleccionarEquipoPorId(idEquipo)) {
            idAbogadoPendienteSeleccion = null;
            return;
        }
        idAbogadoPendienteSeleccion = idAbogado;
        cargarAbogados();
    }

    private boolean seleccionarEquipoPorId(Long idEquipo) {
        for (int i = 0; i < cmbEquipo.getItemCount(); i++) {
            EquipoItem item = cmbEquipo.getItemAt(i);
            if (item != null
                    && item.equipo != null
                    && idEquipo.equals(item.equipo.getIdEquipo())) {
                cargandoCombos = true;
                try {
                    cmbEquipo.setSelectedIndex(i);
                } finally {
                    cargandoCombos = false;
                }
                return true;
            }
        }
        return false;
    }

    private boolean seleccionarAbogadoPorId(Long idAbogado) {
        if (idAbogado == null) {
            return false;
        }
        for (int i = 0; i < cmbAbogado.getItemCount(); i++) {
            UsuarioItem item = cmbAbogado.getItemAt(i);
            if (item != null
                    && item.usuario != null
                    && idAbogado.equals(item.usuario.getIdUsuario())) {
                cmbAbogado.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

    private String textoAsignacion(String value) {
        return value == null || value.trim().isEmpty() ? "Sin asignación" : value.trim();
    }

    private void buscar() {
        busquedaInicialEjecutada = true;
        limpiarSeleccion();
        long secuencia = secuenciaBusqueda.incrementAndGet();
        LocalDate desde = fechaSeleccionada(fechaSolicitudDesde);
        LocalDate hasta = fechaSeleccionada(fechaSolicitudHasta);
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            mostrarInfo("La fecha hasta no puede ser menor que la fecha desde.");
            return;
        }
        SwingWorker<?, ?> workerAnterior = busquedaActiva;
        if (workerAnterior != null && !workerAnterior.isDone()) {
            workerAnterior.cancel(true);
        }
        setTrabajando(true, "Consultando expedientes según filtros...");
        String texto = txtBusqueda.getText();
        String estado = codigoSeleccionado(cmbEstado);
        int limite = ((Number) spnLimite.getValue()).intValue();
        SwingWorker<List<AsignacionExpedienteDTO>, Void> worker = new SwingWorker<List<AsignacionExpedienteDTO>, Void>() {
            @Override
            protected List<AsignacionExpedienteDTO> doInBackground() throws Exception {
                return asignacionService.buscarExpedientes(texto, estado, desde, hasta, limite, false);
            }

            @Override
            protected void done() {
                try {
                    if (secuencia != secuenciaBusqueda.get()) {
                        return;
                    }
                    cargarTabla(get());
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar la bandeja de asignación.", ex);
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

    private void cargarTabla(List<AsignacionExpedienteDTO> items) {
        expedientes.clear();
        expedientes.addAll(items);
        List<AsignacionExpedienteDTO> visibles = filtrarBandejaKpi(items);
        filasTabla.clear();
        asociadosCache.clear();
        hojasEnvioAsignacionMultiple.clear();
        principalesExpandidos.clear();
        principalesCargando.clear();
        idExpedienteExpansionActiva = null;
        tableModel.setRowCount(0);
        table.clearSelection();
        actualizarMetricasBandeja(expedientes);
        cargarTablaVisible(visibles);
        lblEstado.setText(items.isEmpty()
                ? "No se encontraron expedientes con los filtros ingresados."
                : visibles.size() + " expediente(s) encontrado(s).");
        tablePanel.setEmpty(visibles.isEmpty());
        actualizarPanelSeleccion();
    }

    private void cargarTablaVisible(List<AsignacionExpedienteDTO> items) {
        filasTabla.clear();
        asociadosCache.clear();
        principalesExpandidos.clear();
        principalesCargando.clear();
        idExpedienteExpansionActiva = null;
        tableModel.setRowCount(0);
        if (items == null) {
            return;
        }
        for (AsignacionExpedienteDTO item : items) {
            agregarFilaPrincipal(item);
        }
        table.clearSelection();
    }

    private List<AsignacionExpedienteDTO> filtrarBandejaKpi(List<AsignacionExpedienteDTO> items) {
        List<AsignacionExpedienteDTO> filtrados = new ArrayList<AsignacionExpedienteDTO>();
        if (items == null || items.isEmpty() || kpiBandejaActiva == FiltroKpiBandeja.TODOS) {
            if (items != null) {
                filtrados.addAll(items);
            }
            return filtrados;
        }
        for (AsignacionExpedienteDTO item : items) {
            if (coincideBandeja(item)) {
                filtrados.add(item);
            }
        }
        return filtrados;
    }

    private boolean coincideBandeja(AsignacionExpedienteDTO item) {
        switch (kpiBandejaActiva) {
            case PENDIENTES:
                return item.isAsignable();
            case POTENCIAL_DUPLICADO:
                return item.isPotencialDuplicado();
            case POSIBLE_GRUPO_FAMILIAR:
                return item.isPosibleGrupoFamiliar();
            case GRUPO_FAMILIAR_CONFIRMADO:
                return item.isGrupoFamiliar();
            case POR_VENCER:
                Long dias = item.getDiasRestantes();
                return dias != null && dias >= 0 && dias <= 5;
            case VENCIDOS:
                return item.getDiasRestantes() != null && item.getDiasRestantes() < 0;
            case TODOS:
            default:
                return true;
        }
    }

    private void configurarKpisInteractivos() {
        cardPendientes.setOnClick(() -> activarKpiBandeja(FiltroKpiBandeja.PENDIENTES));
        cardPotencialDuplicado.setOnClick(() -> activarKpiBandeja(FiltroKpiBandeja.POTENCIAL_DUPLICADO));
        cardPosibleGrupoFamiliar.setOnClick(() -> activarKpiBandeja(FiltroKpiBandeja.POSIBLE_GRUPO_FAMILIAR));
        cardGrupoFamiliarConfirmado.setOnClick(() -> activarKpiBandeja(FiltroKpiBandeja.GRUPO_FAMILIAR_CONFIRMADO));
        cardPorVencer.setOnClick(() -> activarKpiBandeja(FiltroKpiBandeja.POR_VENCER));
        cardVencidos.setOnClick(() -> activarKpiBandeja(FiltroKpiBandeja.VENCIDOS));

        cardCartasTotal.setOnClick(() -> activarKpiCartas(FiltroKpiCartas.TODOS));
        cardCartasNotificadas.setOnClick(() -> activarKpiCartas(FiltroKpiCartas.CON_ACUSE));
        cardCartasPendientes.setOnClick(() -> activarKpiCartas(FiltroKpiCartas.PENDIENTES));
        cardCartasPublicacion.setOnClick(() -> activarKpiCartas(FiltroKpiCartas.PUBLICACION));

        cardCargaAbogados.setOnClick(() -> activarKpiCarga(FiltroKpiCarga.TODOS));
        cardCargaConAsignacion.setOnClick(() -> activarKpiCarga(FiltroKpiCarga.CON_CARGA));
        cardCargaSinAsignacion.setOnClick(() -> activarKpiCarga(FiltroKpiCarga.SIN_CARGA));
        cardCargaSolicitudes.setOnClick(() -> activarKpiCarga(FiltroKpiCarga.TODOS));

        marcarKpisBandeja();
        marcarKpisCartas();
        marcarKpisCarga();
    }

    private void aplicarCompactacionKpisBandeja() {
        boolean compacto = splitOperativo != null && splitOperativo.isSideExpanded();
        cardPendientes.setCompact(compacto);
        cardPotencialDuplicado.setCompact(compacto);
        cardPosibleGrupoFamiliar.setCompact(compacto);
        cardGrupoFamiliarConfirmado.setCompact(compacto);
        cardPorVencer.setCompact(compacto);
        cardVencidos.setCompact(compacto);
    }

    private void activarKpiBandeja(FiltroKpiBandeja filtro) {
        kpiBandejaActiva = filtro;
        cargarTabla(new ArrayList<AsignacionExpedienteDTO>(expedientes));
        marcarKpisBandeja();
    }

    private void activarKpiCartas(FiltroKpiCartas filtro) {
        kpiCartasActiva = filtro;
        refrescarCartasRespuesta();
        marcarKpisCartas();
    }

    private void activarKpiCarga(FiltroKpiCarga filtro) {
        kpiCargaActiva = filtro;
        List<CargaLaboralAbogadoDTO> visibles = filtrarCargaLaboralKpi(cargasLaborales);
        cargarCargaLaboralModel(visibles);
        marcarKpisCarga();
    }

    private void marcarKpisBandeja() {
        cardPendientes.setSelected(kpiBandejaActiva == FiltroKpiBandeja.PENDIENTES);
        cardPotencialDuplicado.setSelected(kpiBandejaActiva == FiltroKpiBandeja.POTENCIAL_DUPLICADO);
        cardPosibleGrupoFamiliar.setSelected(kpiBandejaActiva == FiltroKpiBandeja.POSIBLE_GRUPO_FAMILIAR);
        cardGrupoFamiliarConfirmado.setSelected(kpiBandejaActiva == FiltroKpiBandeja.GRUPO_FAMILIAR_CONFIRMADO);
        cardPorVencer.setSelected(kpiBandejaActiva == FiltroKpiBandeja.POR_VENCER);
        cardVencidos.setSelected(kpiBandejaActiva == FiltroKpiBandeja.VENCIDOS);
    }

    private void marcarKpisCartas() {
        cardCartasTotal.setSelected(kpiCartasActiva == FiltroKpiCartas.TODOS);
        cardCartasNotificadas.setSelected(kpiCartasActiva == FiltroKpiCartas.CON_ACUSE);
        cardCartasPendientes.setSelected(kpiCartasActiva == FiltroKpiCartas.PENDIENTES);
        cardCartasPublicacion.setSelected(kpiCartasActiva == FiltroKpiCartas.PUBLICACION);
    }

    private void marcarKpisCarga() {
        cardCargaAbogados.setSelected(kpiCargaActiva == FiltroKpiCarga.TODOS);
        cardCargaConAsignacion.setSelected(kpiCargaActiva == FiltroKpiCarga.CON_CARGA);
        cardCargaSinAsignacion.setSelected(kpiCargaActiva == FiltroKpiCarga.SIN_CARGA);
        cardCargaSolicitudes.setSelected(kpiCargaActiva == FiltroKpiCarga.TODOS);
    }

    private List<AsignacionCartaRespuestaDTO> filtrarCartasKpi(List<AsignacionCartaRespuestaDTO> items) {
        List<AsignacionCartaRespuestaDTO> filtrados = new ArrayList<AsignacionCartaRespuestaDTO>();
        if (items == null || items.isEmpty() || kpiCartasActiva == FiltroKpiCartas.TODOS) {
            if (items != null) {
                filtrados.addAll(items);
            }
            return filtrados;
        }
        for (AsignacionCartaRespuestaDTO item : items) {
            if (coincideCarta(item)) {
                filtrados.add(item);
            }
        }
        return filtrados;
    }

    private List<AsignacionCartaRespuestaDTO> filtrarCartasBusqueda(List<AsignacionCartaRespuestaDTO> items) {
        List<AsignacionCartaRespuestaDTO> filtrados = new ArrayList<AsignacionCartaRespuestaDTO>();
        if (items == null || items.isEmpty()) {
            return filtrados;
        }
        String texto = normalizarFiltroTexto(txtBusquedaCartasRespuesta.getText());
        if (texto.isEmpty()) {
            filtrados.addAll(items);
            return filtrados;
        }
        for (AsignacionCartaRespuestaDTO item : items) {
            if (coincideBusquedaCartas(item, texto)) {
                filtrados.add(item);
            }
        }
        return filtrados;
    }

    private boolean coincideBusquedaCartas(AsignacionCartaRespuestaDTO item, String texto) {
        if (item == null || texto == null || texto.isEmpty()) {
            return false;
        }
        return contieneNormalizado(item.getNumeroExpediente(), texto)
                || contieneNormalizado(item.getNumeroExpedienteSgd(), texto)
                || contieneNormalizado(item.getTitular(), texto)
                || contieneNormalizado(item.getTipoDocumentoNombre(), texto)
                || contieneNormalizado(item.getEstadoDocumentoNombre(), texto)
                || contieneNormalizado(item.getNumeroDocumento(), texto)
                || contieneNormalizado(item.getDescripcion(), texto);
    }

    private void refrescarCartasRespuesta() {
        cargarBandejaCartasRespuestaModel(
                filtrarCartasBusqueda(
                        filtrarCartasKpi(cartasRespuestaPendientes)));
    }

    private void aplicarBusquedaCartasRespuesta() {
        refrescarCartasRespuesta();
    }

    private void limpiarBusquedaCartasRespuesta() {
        txtBusquedaCartasRespuesta.setText("");
        refrescarCartasRespuesta();
    }

    private boolean coincideCarta(AsignacionCartaRespuestaDTO item) {
        switch (kpiCartasActiva) {
            case CON_ACUSE:
                return item != null && item.getFechaAcuse() != null;
            case PENDIENTES:
                if (item == null) {
                    return false;
                }
                String confirmacion = item.getConfirmacionRespuesta() == null
                        ? ""
                        : item.getConfirmacionRespuesta().trim().toUpperCase(Locale.ROOT);
                return !"SI".equals(confirmacion) && !"NO".equals(confirmacion);
            case PUBLICACION:
                return item != null && item.isRequierePublicacion();
            case TODOS:
            default:
                return true;
        }
    }

    private List<CargaLaboralAbogadoDTO> filtrarCargaLaboralKpi(List<CargaLaboralAbogadoDTO> items) {
        List<CargaLaboralAbogadoDTO> filtrados = new ArrayList<CargaLaboralAbogadoDTO>();
        if (items == null || items.isEmpty() || kpiCargaActiva == FiltroKpiCarga.TODOS) {
            if (items != null) {
                filtrados.addAll(items);
            }
            return filtrados;
        }
        for (CargaLaboralAbogadoDTO item : items) {
            if (kpiCargaActiva == FiltroKpiCarga.CON_CARGA && item.getExpedientesActivos() > 0) {
                filtrados.add(item);
            } else if (kpiCargaActiva == FiltroKpiCarga.SIN_CARGA && item.getExpedientesActivos() == 0) {
                filtrados.add(item);
            }
        }
        return filtrados;
    }

    private void actualizarMetricasBandeja(List<AsignacionExpedienteDTO> items) {
        int pendientes = 0;
        int potencialDuplicado = 0;
        int posibleGrupoFamiliar = 0;
        int grupoFamiliarConfirmado = 0;
        int porVencer = 0;
        int vencidos = 0;
        if (items != null) {
            for (AsignacionExpedienteDTO item : items) {
                if (item.isAsignable()) {
                    pendientes++;
                }
                if (item.isPotencialDuplicado()) {
                    potencialDuplicado++;
                }
                if (contienePosibleGrupoFamiliar(item)) {
                    posibleGrupoFamiliar++;
                }
                if (item.isGrupoFamiliar()) {
                    grupoFamiliarConfirmado++;
                }
                Long dias = item.getDiasRestantes();
                if (dias != null && dias < 0) {
                    vencidos++;
                } else if (dias != null && dias <= 5) {
                    porVencer++;
                }
            }
        }
        cardPendientes.setValue(String.valueOf(pendientes));
        cardPotencialDuplicado.setValue(String.valueOf(potencialDuplicado));
        cardPosibleGrupoFamiliar.setValue(String.valueOf(posibleGrupoFamiliar));
        cardGrupoFamiliarConfirmado.setValue(String.valueOf(grupoFamiliarConfirmado));
        cardPorVencer.setValue(String.valueOf(porVencer));
        cardVencidos.setValue(String.valueOf(vencidos));
        marcarKpisBandeja();
    }

    private void agregarFilaPrincipal(AsignacionExpedienteDTO item) {
        AsignacionTableRow row = AsignacionTableRow.principal(item);
        filasTabla.add(row);
        tableModel.addRow(new Object[]{
            iconoExpansion(item),
            Boolean.FALSE,
            item.getDiasRestantes() == null ? "" : item.getDiasRestantes(),
            item.getNumeroExpediente(),
            item.getNumeroExpedienteSgd(),
            formatDate(item.getFechaRecepcion()),
            formatDate(item.getFechaVencimiento()),
            item.getProcedimiento(),
            item.getTipoActa(),
            item.getNumeroActa(),
            item.getTitular(),
            item.getAbogadoAsignado(),
            DisplayNameMapperV2.estado(item.getEstadoCodigo()),
            alertaAsignacion(item),
            item.getIdExpediente()
        });
    }

    private void agregarFilaAsociada(AsignacionExpedienteDTO principal, ExpedienteRelacionadoDTO asociado, int index) {
        AsignacionTableRow row = AsignacionTableRow.asociada(principal, asociado);
        filasTabla.add(index, row);
        tableModel.insertRow(index, new Object[]{
            "",
            null,
            "",
            principal == null ? "" : principal.getNumeroExpediente(),
            valorUi(asociado.getNumeroExpedienteSgd()),
            formatDate(asociado.getFechaRecepcion()),
            principal == null ? "" : formatDate(principal.getFechaVencimiento()),
            procedimientoAsociado(asociado),
            valorUi(asociado.getTipoActa()),
            valorUi(asociado.getNumeroActa()),
            valorUi(asociado.getTitular()),
            valorUi(abogadoAsociado(principal, asociado)),
            estadoAsociado(asociado),
            alertaAsociada(asociado),
            asociado.getIdExpediente()
        });
    }

    private String iconoExpansion(AsignacionExpedienteDTO item) {
        if (item == null || item.getIdExpediente() == null || item.getAsociadosConfirmados() <= 0) {
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

    private String abogadoAsociado(AsignacionExpedienteDTO principal, ExpedienteRelacionadoDTO asociado) {
        if (asociado != null && !asociado.getAbogadoAsignado().isEmpty()) {
            return asociado.getAbogadoAsignado();
        }
        return principal == null ? "" : principal.getAbogadoAsignado();
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

    private static String valorUi(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
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
        AsignacionTableRow row = filasTabla.get(modelRow);
        if (!row.esPrincipal()) {
            return;
        }
        tableModel.setValueAt(iconoExpansion(row.principal), modelRow, COL_EXPANDIR);
    }

    private String alertaAsignacion(AsignacionExpedienteDTO item) {
        if (item == null) {
            return "Sin Alerta";
        }
        if (item.isPotencialDuplicado()) {
            return "Potencial duplicado";
        }
        if (contienePosibleGrupoFamiliar(item)) {
            return "Posible Grupo Familiar";
        }
        return "Sin Alerta";
    }

    private String alertaAsociada(ExpedienteRelacionadoDTO asociado) {
        if (asociado == null) {
            return "Sin Alerta";
        }
        String alerta = normalizarTexto(asociado.getAlertaIngreso());
        if (alerta.contains("potencial duplicado")) {
            return "Potencial duplicado";
        }
        if (alerta.contains("posible grupo familiar")) {
            return "Posible Grupo Familiar";
        }
        if (alerta.contains("con observaciones")) {
            return "Con observaciones";
        }
        return "Sin Alerta";
    }

    private boolean contienePosibleGrupoFamiliar(AsignacionExpedienteDTO item) {
        return item != null && item.tieneAlertaGrupoFamiliarActiva();
    }

    private static String normalizarTexto(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static String formatDate(LocalDate value) {
        return value == null ? "" : DATE_FORMAT.format(value);
    }

    private static LocalDate fechaSeleccionada(PremiumDateFieldV2 field) {
        if (field == null || field.getDate() == null) {
            return null;
        }
        Date date = field.getDate();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static String codigoSeleccionado(JComboBox<FiltroCatalogoItemV2> combo) {
        Object selected = combo.getSelectedItem();
        if (selected instanceof FiltroCatalogoItemV2) {
            FiltroCatalogoItemV2 item = (FiltroCatalogoItemV2) selected;
            return item.hasCodigo() ? item.getCodigo() : null;
        }
        return null;
    }

    private void restaurarFechasBusqueda() {
        fechaSolicitudDesde.setDate(DateRangePickerSupport.defaultSearchFromDate());
        fechaSolicitudHasta.setDate(DateRangePickerSupport.defaultSearchToDate());
    }

    private void limpiar() {
        if (columnFilterSupport != null) {
            columnFilterSupport.clearFilters();
        }
        txtBusqueda.setText("");
        restaurarFechasBusqueda();
        cmbEstado.setSelectedIndex(0);
        spnLimite.setValue(200);
        expedientes.clear();
        filasTabla.clear();
        asociadosCache.clear();
        principalesExpandidos.clear();
        principalesCargando.clear();
        cargasLaborales.clear();
        idExpedienteExpansionActiva = null;
        kpiBandejaActiva = FiltroKpiBandeja.TODOS;
        kpiCartasActiva = FiltroKpiCartas.TODOS;
        kpiCargaActiva = FiltroKpiCarga.TODOS;
        tableModel.setRowCount(0);
        table.clearSelection();
        tablePanel.setEmpty(true);
        txtComentario.setText("");
        cardPendientes.setValue("0");
        cardPotencialDuplicado.setValue("0");
        cardPosibleGrupoFamiliar.setValue("0");
        cardGrupoFamiliarConfirmado.setValue("0");
        cardPorVencer.setValue("0");
        cardVencidos.setValue("0");
        marcarKpisBandeja();
        marcarKpisCartas();
        marcarKpisCarga();
        lblEstado.setText("Filtros limpiados. Presione Buscar para consultar expedientes.");
        actualizarPanelSeleccion();
    }

    private void alternarExpansionFila(int modelRow) {
        finalizarEdicionTabla();
        if (modelRow < 0 || modelRow >= filasTabla.size()) {
            return;
        }
        AsignacionTableRow row = filasTabla.get(modelRow);
        if (!row.esPrincipal()
                || row.principal.getIdExpediente() == null
                || row.principal.getAsociadosConfirmados() <= 0) {
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

    private void insertarAsociados(int principalRow, AsignacionExpedienteDTO principal, List<ExpedienteRelacionadoDTO> asociados) {
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
        List<Long> expandidos = new ArrayList<>(principalesExpandidos);
        for (Long id : expandidos) {
            if (id != null && !id.equals(idPermitido)) {
                contraerAsociados(id);
            }
        }
        List<Long> cargando = new ArrayList<>(principalesCargando);
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
        boolean seleccionarPrincipal = false;
        AsignacionTableRow selected = filaTabla(selectedRow);
        if (selected != null && selected.esAsociada() && idPrincipal.equals(selected.idExpedientePrincipal)) {
            seleccionarPrincipal = true;
        }
        for (int i = filasTabla.size() - 1; i > principalRow; i--) {
            AsignacionTableRow row = filasTabla.get(i);
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
        actualizarPanelSeleccion();
    }

    private int indiceFilaPrincipal(Long idPrincipal) {
        if (idPrincipal == null) {
            return -1;
        }
        for (int i = 0; i < filasTabla.size(); i++) {
            AsignacionTableRow row = filasTabla.get(i);
            if (row.esPrincipal() && idPrincipal.equals(row.principal.getIdExpediente())) {
                return i;
            }
        }
        return -1;
    }

    private void seleccionarVisibles() {
        if (tableModel.getRowCount() == 0) {
            mostrarInfo("No hay expedientes visibles para seleccionar.");
            return;
        }
        actualizandoSeleccion = true;
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            AsignacionTableRow row = filaTabla(modelRow);
            if (esFilaSeleccionableAsignacion(row)) {
                tableModel.setValueAt(Boolean.TRUE, modelRow, COL_SELECCION);
            }
        }
        actualizandoSeleccion = false;
        panelAsignacionCerradoPorUsuario = false;
        actualizarPanelSeleccion();
    }

    private void alternarSeleccionVisibleDesdeHeader() {
        actualizarEstadoHeaderSeleccion();
        if (!hayVisiblesAsignables) {
            return;
        }
        boolean seleccionar = !todasVisiblesSeleccionadas;
        actualizandoSeleccion = true;
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            AsignacionTableRow row = filaTabla(modelRow);
            if (esFilaSeleccionableAsignacion(row)) {
                tableModel.setValueAt(Boolean.valueOf(seleccionar), modelRow, COL_SELECCION);
            }
        }
        actualizandoSeleccion = false;
        if (seleccionar) {
            panelAsignacionCerradoPorUsuario = false;
        }
        actualizarPanelSeleccion();
    }

    private void limpiarSeleccion() {
        guardarHojasEnvioAsignacionMultiple();
        actualizandoSeleccion = true;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(Boolean.FALSE, i, COL_SELECCION);
        }
        actualizandoSeleccion = false;
        table.clearSelection();
        asignacionMultipleModel.setRowCount(0);
        expedientesAsignacionMultiple.clear();
        hojasEnvioAsignacionMultiple.clear();
        panelAsignacionCerradoPorUsuario = false;
        actualizarPanelSeleccion();
    }

    private void asignarFilaSeleccionada() {
        finalizarEdicionTabla();
        Long id = obtenerIdFilaSeleccionada();
        if (id == null) {
            mostrarInfo("Seleccione un expediente para asignar.");
            return;
        }
        int modelRow = obtenerModelRowSeleccionada();
        AsignacionTableRow row = filaTabla(modelRow);
        if (row != null && row.esAsociada()) {
            mostrarInfo("Este documento está asociado al expediente principal y no requiere asignación independiente.");
            return;
        }
        if (!esFilaSeleccionableAsignacion(row)) {
            mostrarInfo(modoReasignacion
                    ? "El expediente no está habilitado para asignación o reasignación."
                    : "El expediente ya está asignado. Active \"Habilitar reasignación\" para reasignarlo.");
            return;
        }
        if (!row.principal.tieneNumeroExpediente()) {
            mostrarInfo("El expediente seleccionado aún no tiene número. Asócielo a un expediente principal o genere número antes de asignarlo.");
            return;
        }
        generarAsignacionDesdePanel();
    }

    private void asignarMarcados() {
        generarAsignacionDesdePanel();
    }

    private void cancelarSeleccionAsignacion() {
        limpiarSeleccion();
        panelAsignacionCerradoPorUsuario = false;
        actualizarPanelSeleccion();
    }

    private void generarAsignacionDesdePanel() {
        finalizarEdicionTabla();
        List<AsignacionExpedienteDTO> expedientes = obtenerExpedientesSeleccionadosParaAsignacion();
        if (expedientes == null || expedientes.isEmpty()) {
            mostrarInfo("Seleccione uno o más expedientes para generar la asignación.");
            return;
        }
        List<Long> idsNuevos = new ArrayList<>();
        List<Long> idsReasignar = new ArrayList<>();
        for (AsignacionExpedienteDTO expediente : expedientes) {
            if (expediente == null || expediente.getIdExpediente() == null) {
                continue;
            }
            if (expediente.isAsignacionActiva()) {
                idsReasignar.add(expediente.getIdExpediente());
            } else {
                idsNuevos.add(expediente.getIdExpediente());
            }
        }
        if (idsNuevos.isEmpty() && idsReasignar.isEmpty()) {
            mostrarInfo("Seleccione uno o más expedientes para generar la asignación.");
            return;
        }
        List<Long> todosIds = new ArrayList<>(idsNuevos);
        todosIds.addAll(idsReasignar);
        Map<Long, String> hojasEnvioPorExpediente = obtenerHojasEnvioAsignacionMultiple(todosIds);
        if (hojasEnvioPorExpediente == null) {
            return;
        }
        String comentario = txtComentario == null ? "" : txtComentario.getText();
        ejecutarAsignacion(idsNuevos, idsReasignar, hojasEnvioPorExpediente, comentario == null ? "" : comentario.trim());
    }

    private List<AsignacionExpedienteDTO> obtenerExpedientesSeleccionadosParaAsignacion() {
        List<AsignacionExpedienteDTO> seleccionados = new ArrayList<>();
        List<Long> ids = obtenerIdsMarcados();
        if (ids == null) {
            return null;
        }
        Long idSeleccionado = obtenerIdFilaSeleccionada();
        if (ids.isEmpty() && idSeleccionado != null) {
            ids.add(idSeleccionado);
        }
        for (Long id : ids) {
            AsignacionTableRow row = buscarFilaPorId(id);
            if (row == null || row.asociado != null || row.principal == null) {
                continue;
            }
            AsignacionExpedienteDTO item = row.principal;
            if (!esFilaSeleccionableAsignacion(row)) {
                continue;
            }
            if (!item.tieneNumeroExpediente()) {
                continue;
            }
            seleccionados.add(item);
        }
        return seleccionados;
    }

    private AsignacionTableRow buscarFilaPorId(Long idExpediente) {
        if (idExpediente == null) {
            return null;
        }
        for (int i = 0; i < filasTabla.size(); i++) {
            AsignacionTableRow row = filasTabla.get(i);
            if (row == null) {
                continue;
            }
            if (row.principal != null && idExpediente.equals(row.principal.getIdExpediente())) {
                return row;
            }
            if (row.asociado != null && idExpediente.equals(row.asociado.getIdExpediente())) {
                return row;
            }
        }
        return null;
    }

    private void ejecutarAsignacion(
            List<Long> idsNuevos,
            List<Long> idsReasignar,
            Map<Long, String> hojasEnvioPorExpediente,
            String comentario) {
        EquipoAsignacionDTO equipo = obtenerEquipoSeleccionado();
        UsuarioAsignableDTO abogado = obtenerAbogadoSeleccionado();
        if (equipo == null || abogado == null) {
            mostrarInfo("Seleccione equipo destino y abogado responsable antes de asignar.");
            return;
        }
        if (!idsReasignar.isEmpty()) {
            int confirmacion = JOptionPane.showConfirmDialog(
                    this,
                    "Va a reasignar " + idsReasignar.size()
                            + " expediente(s) que ya tienen un abogado asignado. Se conservará el historial de la asignación anterior.\n"
                            + "¿Desea continuar?",
                    "Confirmar reasignación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
        }
        setTrabajando(true, "Procesando asignación...");
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                StringBuilder mensaje = new StringBuilder();
                if (!idsNuevos.isEmpty()) {
                    AsignacionResultadoDTO resultado = asignacionService.asignar(
                            idsNuevos, equipo, abogado, comentario, hojasEnvioPorExpediente);
                    mensaje.append(resultado.getMensaje());
                }
                if (!idsReasignar.isEmpty()) {
                    int exitosas = 0;
                    List<String> errores = new ArrayList<>();
                    for (Long id : idsReasignar) {
                        try {
                            asignacionService.reasignar(id, equipo, abogado, hojasEnvioPorExpediente.get(id), comentario);
                            exitosas++;
                        } catch (Exception ex) {
                            errores.add("Expediente " + id + ": " + mensajeCausaRaiz(ex));
                        }
                    }
                    if (mensaje.length() > 0) {
                        mensaje.append("\n");
                    }
                    mensaje.append(exitosas).append(" expediente(s) reasignado(s) correctamente.");
                    if (!errores.isEmpty()) {
                        mensaje.append("\n").append(errores.size()).append(" reasignación(es) no se pudieron completar:");
                        for (String error : errores) {
                            mensaje.append("\n- ").append(error);
                        }
                    }
                }
                return mensaje.toString();
            }

            @Override
            protected void done() {
                try {
                    String mensaje = get();
                    JOptionPane.showMessageDialog(
                            JPanelAsignacionV2.this,
                            mensaje,
                            "Asignación",
                            JOptionPane.INFORMATION_MESSAGE);
                    if (modoReasignacion) {
                        modoReasignacion = false;
                        chkModoReasignacion.setSelected(false);
                    }
                    buscar();
                } catch (Exception ex) {
                    mostrarError("No se pudo completar la asignación. No se dejaron expedientes parcialmente asignados.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private static String mensajeCausaRaiz(Throwable ex) {
        Throwable causa = ex;
        while (causa.getCause() != null && (causa instanceof java.util.concurrent.ExecutionException || causa.getMessage() == null)) {
            causa = causa.getCause();
        }
        return causa.getMessage() == null ? causa.toString() : causa.getMessage();
    }

    private void abrirDetalleSeleccionado() {
        Long idExpediente = obtenerIdFilaSeleccionada();
        if (idExpediente == null) {
            mostrarInfo("Seleccione un expediente para ver el detalle.");
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(owner, idExpediente);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void editarRegistroSeleccionado() {
        AsignacionExpedienteDTO item = obtenerExpedienteFoco();
        if (item == null) {
            mostrarInfo("Seleccione un expediente para editar.");
            return;
        }
        if (!item.isAsignable()) {
            mostrarInfo("Solo se permite editar expedientes en estado Registrado y sin asignación a abogado.");
            return;
        }
        mostrarEdicionManualAsignacion(item.getIdExpediente());
    }

    private void mostrarEdicionManualAsignacion(final Long idExpediente) {
        if (tabsBandejas == null || idExpediente == null) {
            return;
        }
        com.sdrerc.ui.views.registrorecepcion.JPanelRegistroManualRecepcionV2 panelEdicion =
                new com.sdrerc.ui.views.registrorecepcion.JPanelRegistroManualRecepcionV2(
                        idExpediente,
                        new Runnable() {
                            @Override
                            public void run() {
                                restaurarBandejaAsignacionTrasEdicion();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                restaurarBandejaAsignacionTrasEdicion();
                            }
                        },
                        false);
        if (tabsBandejas.getTabCount() > 3) {
            tabsBandejas.removeTabAt(3);
        }
        tabsBandejas.addTab("Edición manual", panelEdicion);
        tabsBandejas.setSelectedIndex(3);
    }

    private void restaurarBandejaAsignacionTrasEdicion() {
        if (tabsBandejas == null) {
            return;
        }
        if (tabsBandejas.getTabCount() > 3) {
            tabsBandejas.removeTabAt(3);
        }
        tabsBandejas.setSelectedIndex(TAB_BANDEJA_ASIGNACION);
        buscar();
    }

    private void eliminarRegistroSeleccionado() {
        AsignacionExpedienteDTO item = obtenerExpedienteFoco();
        if (item == null) {
            mostrarInfo("Seleccione un expediente para eliminar.");
            return;
        }
        if (!item.isAsignable()) {
            mostrarInfo("Solo se permite eliminar expedientes en estado Registrado y sin asignación a abogado.");
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Esta acción eliminará el registro seleccionado (baja lógica, quedará excluido de las bandejas). ¿Desea continuar?",
                "Eliminar registro",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        final Long idExpediente = item.getIdExpediente();
        setTrabajando(true, "Eliminando registro...");
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                edicionManualService.eliminar(idExpediente);
                return null;
            }

            @Override
            protected void done() {
                setTrabajando(false, null);
                try {
                    get();
                    limpiarSeleccion();
                    buscar();
                    JOptionPane.showMessageDialog(
                            JPanelAsignacionV2.this,
                            "El registro fue eliminado correctamente.",
                            "Eliminar registro",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    mostrarError("No se pudo eliminar el registro seleccionado.", ex);
                }
            }
        };
        worker.execute();
    }

    private void asociarRelacionadosRapido() {
        finalizarEdicionTabla();
        AsociacionRapidaSeleccion seleccion = obtenerSeleccionAsociacionRapida();
        if (seleccion == null) {
            mostrarInfo("Seleccione un expediente con posibles relacionados o marque dos expedientes con la misma acta y titular.");
            return;
        }
        if (seleccion.ambiguo) {
            mostrarInfo("Hay más de un expediente con número entre los marcados. Solo puede haber un expediente "
                    + "principal (el que ya tiene número): desmarque los que no correspondan y deje marcado únicamente "
                    + "el expediente principal junto con los potenciales duplicados sin número.");
            return;
        }
        final Long idPrincipal;
        final String numeroPrincipalMostrado;
        final String sgdPrincipalMostrado;
        final List<Long> idsAAsociar;
        if (seleccion.esSeleccionMultiple()) {
            idPrincipal = seleccion.idPrincipal;
            numeroPrincipalMostrado = seleccion.expedienteFoco.getNumeroExpediente();
            sgdPrincipalMostrado = seleccion.expedienteFoco.getNumeroExpedienteSgd();
            idsAAsociar = new ArrayList<>(seleccion.idsRelacionados);
        } else {
            List<Long> marcados = obtenerIdsDocumentosRelacionadosMarcados();
            ResolucionAsociacionFoco resolucion = resolverPrincipalParaFoco(seleccion.expedienteFoco, marcados);
            if (resolucion.ambiguo) {
                mostrarInfo("Se detectó más de un expediente con número entre las solicitudes marcadas como "
                        + "coincidencias. Seleccione manualmente cuál es el expediente principal (haga clic en esa "
                        + "fila de la bandeja) y vuelva a intentar la asociación desde ahí.");
                return;
            }
            idPrincipal = resolucion.idPrincipal;
            numeroPrincipalMostrado = resolucion.numeroPrincipal;
            sgdPrincipalMostrado = resolucion.sgdPrincipal;
            idsAAsociar = resolucion.idsRelacionados;
        }
        if (idPrincipal == null || idsAAsociar.isEmpty()) {
            mostrarInfo(seleccion.esSeleccionMultiple()
                    ? "No hay coincidencias pendientes para asociar."
                    : "Marque al menos una solicitud en \"Solicitudes asociadas\" para asociar.");
            return;
        }
        String detalle = seleccion.esSeleccionMultiple()
                ? "Se asociarán los " + seleccion.totalSeleccionados + " registros marcados como documentos duplicados del expediente principal si comparten el mismo número de acta y titular.\n"
                : "Se asociarán " + idsAAsociar.size() + " solicitud(es) sin número como documentos duplicados del expediente principal.\n";
        int confirm = JOptionPane.showConfirmDialog(
                this,
                detalle
                        + "Expediente principal destino: "
                        + valorUi(numeroPrincipalMostrado)
                        + (sgdPrincipalMostrado == null || sgdPrincipalMostrado.trim().isEmpty()
                                ? ""
                                : " / SGD " + sgdPrincipalMostrado.trim())
                        + "\n"
                        + "Si un relacionado no tiene número de expediente, tomará el número del expediente principal.\n"
                        + "No se registrará como documento analizado; esa decisión corresponde al módulo Análisis.\n"
                        + "¿Desea continuar?",
                "Confirmar asociación rápida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setTrabajando(true, "Asociando expedientes por misma acta y titular...");
        SwingWorker<ExpedienteRelacionResultadoDTO, Void> worker = new SwingWorker<ExpedienteRelacionResultadoDTO, Void>() {
            @Override
            protected ExpedienteRelacionResultadoDTO doInBackground() throws Exception {
                return relacionadoService.asociarRelacionados(
                        idPrincipal,
                        idsAAsociar,
                        "Documento duplicado asociado al expediente principal por misma acta y titular.");
            }

            @Override
            protected void done() {
                try {
                    ExpedienteRelacionResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelAsignacionV2.this,
                            resultado.getMensaje(),
                            "Asociación de expedientes",
                            JOptionPane.INFORMATION_MESSAGE);
                    buscar();
                } catch (Exception ex) {
                    mostrarError("No se pudo completar la asociación de expedientes.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void generarNumeroExpedienteSeleccionado() {
        finalizarEdicionTabla();
        AsignacionExpedienteDTO item = obtenerExpedienteFoco();
        if (item == null || item.getIdExpediente() == null) {
            mostrarInfo("Seleccione un expediente de Reconsideración o Apelación sin número.");
            return;
        }
        if (!item.requiereDecisionNumeroAsignacion()) {
            mostrarInfo("La generación manual de número solo aplica para Reconsideración o Apelación registrada sin número.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Se generará un nuevo número de expediente para esta solicitud.\n"
                        + "Use esta opción solo si no corresponde asociarla a un expediente principal.\n\n"
                        + "¿Desea continuar?",
                "Generar número de expediente",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setTrabajando(true, "Generando número de expediente...");
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return asignacionService.generarNumeroExpediente(item.getIdExpediente());
            }

            @Override
            protected void done() {
                try {
                    String numero = get();
                    JOptionPane.showMessageDialog(
                            JPanelAsignacionV2.this,
                            "Número de expediente generado: " + numero,
                            "Generación de número",
                            JOptionPane.INFORMATION_MESSAGE);
                    buscar();
                } catch (Exception ex) {
                    mostrarError("No se pudo generar el número de expediente.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private List<Long> obtenerIdsMarcados() {
        finalizarEdicionTabla();
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, COL_SELECCION))) {
                AsignacionTableRow row = filaTabla(i);
                if (!esFilaSeleccionableAsignacion(row)) {
                    mostrarInfo(modoReasignacion
                            ? "Un expediente marcado no está habilitado para asignación o reasignación."
                            : "Un expediente marcado ya está asignado. Active \"Habilitar reasignación\" para reasignarlo.");
                    return null;
                }
                AsignacionExpedienteDTO item = row.principal;
                if (!item.tieneNumeroExpediente()) {
                    mostrarInfo("El expediente seleccionado aún no tiene número. Asócielo a un expediente principal o genere número antes de asignarlo.");
                    return null;
                }
                ids.add(item.getIdExpediente());
            }
        }
        return ids;
    }

    private static boolean esHojaEnvioVacia(String valor) {
        String normalizado = valor == null ? "" : valor.trim();
        return normalizado.isEmpty() || normalizado.equals("-");
    }

    private Map<Long, String> obtenerHojaEnvioAsignacionSimple(Long idExpediente) {
        String hoja = txtHojaEnvioAsignacion.getText() == null
                ? ""
                : txtHojaEnvioAsignacion.getText().trim();
        txtHojaEnvioAsignacion.setText(hoja);
        if (esHojaEnvioVacia(hoja)) {
            mostrarInfo("Ingrese una hoja de envío.");
            txtHojaEnvioAsignacion.requestFocusInWindow();
            return null;
        }
        Map<Long, String> result = new HashMap<>();
        result.put(idExpediente, hoja);
        return result;
    }

    private Map<Long, String> obtenerHojasEnvioAsignacionMultiple(List<Long> ids) {
        guardarHojasEnvioAsignacionMultiple();
        Map<Long, String> result = new HashMap<>();
        Set<String> normalizados = new HashSet<>();
        for (Long id : ids) {
            String hoja = hojasEnvioAsignacionMultiple.get(id);
            if (esHojaEnvioVacia(hoja)) {
                mostrarInfo("Ingrese una hoja de envío.");
                enfocarHojaEnvioPendiente(id);
                return null;
            }
            String normalizada = hoja.trim().toUpperCase(Locale.ROOT);
            if (!normalizados.add(normalizada)) {
                mostrarInfo("El número de hoja de envío " + hoja.trim() + " está duplicado en la selección.");
                return null;
            }
            result.put(id, hoja.trim());
        }
        return result;
    }

    private void guardarHojasEnvioAsignacionMultiple() {
        finalizarEdicionAsignacionMultiple();
        for (int row = 0; row < asignacionMultipleModel.getRowCount(); row++) {
            Long id = obtenerIdDesdeFilaAsignacionMultiple(row);
            if (id == null) {
                continue;
            }
            Object hojaValue = asignacionMultipleModel.getValueAt(row, 2);
            String hoja = hojaValue == null ? "" : hojaValue.toString().trim();
            if (hoja.isEmpty()) {
                hojasEnvioAsignacionMultiple.remove(id);
            } else {
                hojasEnvioAsignacionMultiple.put(id, hoja);
            }
        }
    }

    private Long obtenerIdDesdeFilaAsignacionMultiple(int row) {
        return row >= 0 && row < expedientesAsignacionMultiple.size()
                ? expedientesAsignacionMultiple.get(row).getIdExpediente()
                : null;
    }

    private void enfocarHojaEnvioPendiente(Long idExpediente) {
        if (idExpediente == null) {
            return;
        }
        for (int row = 0; row < expedientesAsignacionMultiple.size(); row++) {
            AsignacionExpedienteDTO item = expedientesAsignacionMultiple.get(row);
            if (idExpediente.equals(item.getIdExpediente())) {
                asignacionMultipleTable.requestFocusInWindow();
                asignacionMultipleTable.changeSelection(row, 2, false, false);
                asignacionMultipleTable.editCellAt(row, 2);
                return;
            }
        }
    }

    private AsignacionExpedienteDTO obtenerExpedienteParaRelacionados() {
        int marcados = contarSeleccionados();
        if (marcados > 1) {
            return null;
        }
        if (marcados == 1) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (Boolean.TRUE.equals(tableModel.getValueAt(i, COL_SELECCION))) {
                    AsignacionTableRow row = filaTabla(i);
                    return row == null || !row.esPrincipal() ? null : row.principal;
                }
            }
        }
        int modelRow = obtenerModelRowSeleccionada();
        AsignacionTableRow row = filaTabla(modelRow);
        if (row == null) {
            return null;
        }
        return row.principal;
    }

    private AsociacionRapidaSeleccion obtenerSeleccionAsociacionRapida() {
        List<AsignacionExpedienteDTO> marcados = obtenerExpedientesMarcados();
        if (marcados.size() >= 2) {
            if (contarConNumeroExpediente(marcados) > 1) {
                return new AsociacionRapidaSeleccion(null, null, new ArrayList<Long>(), marcados.size(), true);
            }
            AsignacionExpedienteDTO principal = elegirPrincipalAsociacion(marcados);
            List<Long> relacionados = new ArrayList<>();
            for (AsignacionExpedienteDTO item : marcados) {
                if (!item.getIdExpediente().equals(principal.getIdExpediente())) {
                    relacionados.add(item.getIdExpediente());
                }
            }
            return new AsociacionRapidaSeleccion(principal, principal.getIdExpediente(), relacionados, marcados.size());
        }
        AsignacionExpedienteDTO foco = obtenerExpedienteParaRelacionados();
        if (foco == null) {
            return null;
        }
        return new AsociacionRapidaSeleccion(foco, foco.getIdExpediente(), new ArrayList<Long>(), 1);
    }

    private int contarConNumeroExpediente(List<AsignacionExpedienteDTO> items) {
        int total = 0;
        for (AsignacionExpedienteDTO item : items) {
            if (tieneNumeroExpediente(item)) {
                total++;
            }
        }
        return total;
    }

    /**
     * Cuando el foco de la asociacion de un solo expediente (no seleccion multiple) no tiene
     * numero de expediente propio, pero entre las solicitudes asociadas marcadas hay exactamente
     * una CON numero, esa es el principal real: se invierte la asociacion (el foco pasa a ser uno
     * de los relacionados). Si hay mas de una marcada con numero, es ambiguo y no se decide solo.
     */
    private ResolucionAsociacionFoco resolverPrincipalParaFoco(AsignacionExpedienteDTO foco, List<Long> idsMarcados) {
        if (foco == null) {
            return new ResolucionAsociacionFoco(null, null, null, Collections.<Long>emptyList(), false);
        }
        if (tieneNumeroExpediente(foco)) {
            return new ResolucionAsociacionFoco(
                    foco.getIdExpediente(), foco.getNumeroExpediente(), foco.getNumeroExpedienteSgd(), idsMarcados, false);
        }
        ExpedienteRelacionadoDTO candidatoConNumero = null;
        int totalConNumero = 0;
        for (Long idMarcado : idsMarcados) {
            for (DocumentoRelacionadoFila fila : documentosRelacionadosPanel) {
                if (fila == null || fila.esAsociado() || fila.getExpediente() == null) {
                    continue;
                }
                if (idMarcado.equals(fila.getExpediente().getIdExpediente()) && tieneNumeroExpediente(fila.getExpediente())) {
                    candidatoConNumero = fila.getExpediente();
                    totalConNumero++;
                }
            }
        }
        if (totalConNumero == 0) {
            return new ResolucionAsociacionFoco(
                    foco.getIdExpediente(), foco.getNumeroExpediente(), foco.getNumeroExpedienteSgd(), idsMarcados, false);
        }
        if (totalConNumero > 1) {
            return new ResolucionAsociacionFoco(null, null, null, Collections.<Long>emptyList(), true);
        }
        List<Long> idsFinal = new ArrayList<>();
        for (Long idMarcado : idsMarcados) {
            if (!idMarcado.equals(candidatoConNumero.getIdExpediente())) {
                idsFinal.add(idMarcado);
            }
        }
        idsFinal.add(foco.getIdExpediente());
        return new ResolucionAsociacionFoco(
                candidatoConNumero.getIdExpediente(),
                candidatoConNumero.getNumeroExpediente(),
                candidatoConNumero.getNumeroExpedienteSgd(),
                idsFinal,
                false);
    }

    private List<AsignacionExpedienteDTO> obtenerExpedientesMarcados() {
        List<AsignacionExpedienteDTO> marcados = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, COL_SELECCION))) {
                AsignacionTableRow row = filaTabla(i);
                if (row != null && row.esPrincipal()) {
                    marcados.add(row.principal);
                }
            }
        }
        return marcados;
    }

    private AsignacionExpedienteDTO elegirPrincipalAsociacion(List<AsignacionExpedienteDTO> items) {
        AsignacionExpedienteDTO principal = null;
        for (AsignacionExpedienteDTO item : items) {
            if (principal == null) {
                principal = item;
                continue;
            }
            boolean itemTieneNumero = tieneNumeroExpediente(item);
            boolean principalTieneNumero = tieneNumeroExpediente(principal);
            if (itemTieneNumero && !principalTieneNumero) {
                principal = item;
                continue;
            }
            if (itemTieneNumero == principalTieneNumero
                    && item.getFechaRegistro() != null
                    && (principal.getFechaRegistro() == null || item.getFechaRegistro().isBefore(principal.getFechaRegistro()))) {
                principal = item;
            }
        }
        return principal == null ? items.get(0) : principal;
    }

    private boolean tieneNumeroExpediente(AsignacionExpedienteDTO item) {
        return item != null
                && item.getNumeroExpediente() != null
                && !item.getNumeroExpediente().trim().isEmpty();
    }

    private AsignacionExpedienteDTO obtenerExpedienteFoco() {
        List<AsignacionExpedienteDTO> marcados = obtenerExpedientesMarcados();
        if (marcados.size() == 1) {
            return marcados.get(0);
        }
        if (marcados.size() > 1) {
            return null;
        }
        AsignacionTableRow row = filaTabla(obtenerModelRowSeleccionada());
        return row != null && row.esPrincipal() ? row.principal : null;
    }

    private Long obtenerIdFilaSeleccionada() {
        int modelRow = obtenerModelRowSeleccionada();
        AsignacionTableRow row = filaTabla(modelRow);
        if (row == null) {
            return null;
        }
        return row.getIdExpediente();
    }

    private int obtenerModelRowSeleccionada() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            return -1;
        }
        return table.convertRowIndexToModel(selected);
    }

    private AsignacionTableRow filaTabla(int modelRow) {
        return modelRow >= 0 && modelRow < filasTabla.size() ? filasTabla.get(modelRow) : null;
    }

    private boolean esFilaSeleccionableAsignacion(AsignacionTableRow row) {
        if (row == null || !row.esPrincipal() || !row.principal.tieneNumeroExpediente()) {
            return false;
        }
        if (row.esAsignable()) {
            return true;
        }
        return modoReasignacion && row.principal.isAsignacionActiva();
    }

    private void alternarModoReasignacion() {
        modoReasignacion = chkModoReasignacion.isSelected();
        actualizandoSeleccion = true;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            AsignacionTableRow row = filaTabla(i);
            if (!esFilaSeleccionableAsignacion(row) && Boolean.TRUE.equals(tableModel.getValueAt(i, COL_SELECCION))) {
                tableModel.setValueAt(Boolean.FALSE, i, COL_SELECCION);
            }
        }
        actualizandoSeleccion = false;
        table.repaint();
        actualizarPanelSeleccion();
    }

    private EquipoAsignacionDTO obtenerEquipoSeleccionado() {
        Object selected = cmbEquipo.getSelectedItem();
        return selected instanceof EquipoItem ? ((EquipoItem) selected).equipo : null;
    }

    private UsuarioAsignableDTO obtenerAbogadoSeleccionado() {
        Object selected = cmbAbogado.getSelectedItem();
        return selected instanceof UsuarioItem ? ((UsuarioItem) selected).usuario : null;
    }

    private void actualizarSupervisor() {
        UsuarioAsignableDTO abogado = obtenerAbogadoSeleccionado();
        lblSupervisor.setText(abogado == null || abogado.getSupervisorNombre().isEmpty() ? "-" : abogado.getSupervisorNombre());
    }

    private void cargarCargaLaboralModel(List<CargaLaboralAbogadoDTO> cargas) {
        cargaLaboralModel.setRowCount(0);
        if (cargas != null) {
            for (CargaLaboralAbogadoDTO carga : cargas) {
                cargaLaboralModel.addRow(new Object[]{
                    valorUi(carga.getAbogado()),
                    valorUi(carga.getSupervisor()),
                    valorUi(carga.getEquipo()),
                    carga.getExpedientesActivos(),
                    carga.getPorVencer(),
                    carga.getVencidos(),
                    carga.getEnAnalisis()
                });
            }
        }
        cargaLaboralTablePanel.setEmpty(cargaLaboralModel.getRowCount() == 0);
        if (cargaLaboralModel.getRowCount() == 0) {
            lblCargaLaboralAyuda.setText("No hay abogados activos para mostrar.");
        } else {
            lblCargaLaboralAyuda.setText("Indicadores informativos para apoyar la asignación; no bloquean la decisión.");
        }
    }

    private void limpiarCargaLaboral() {
        cargaLaboralModel.setRowCount(0);
        cargaLaboralTablePanel.setEmpty(true);
        lblCargaLaboralEquipo.setText("Carga laboral general de abogados activos.");
        lblCargaLaboralAyuda.setText("Indicadores informativos para apoyar la asignación; no bloquean la decisión.");
    }

    private void actualizarPanelSeleccion() {
        guardarHojasEnvioAsignacionMultiple();
        int marcados = contarSeleccionados();
        int modelRow = obtenerModelRowSeleccionada();
        AsignacionTableRow filaSeleccionada = filaTabla(modelRow);
        int seleccionados = marcados > 0 ? marcados : (filaSeleccionada != null ? 1 : 0);
        boolean modoMultiple = marcados > 1;
        String seleccionadosText = seleccionados == 0
                ? "0 expediente(s) seleccionados"
                : seleccionados + " expediente(s) seleccionados";
        lblSeleccionados.setText(seleccionadosText);
        lblSeleccionadosPanel.setText(seleccionados + " expediente(s) seleccionados");
        actualizarEstadoHeaderSeleccion();
        if (seleccionados == 0) {
            panelAsignacionCerradoPorUsuario = false;
        }
        if (esBandejaAsignacionActiva() && splitOperativo != null && splitOperativo.isSideVisible()) {
            actualizarVisibilidadPanelAsignacion(seleccionados > 0 && !panelAsignacionCerradoPorUsuario);
        }
        AsignacionTableRow filaPanel = filaParaPanel(modelRow, marcados);
        boolean mostrarAsignacion = filaPanel != null && (filaPanel.esPrincipal() || modoMultiple);
        actualizarModoPanelAsignacion(mostrarAsignacion);
        boolean haySeleccionOperativa = mostrarAsignacion;
        btnAsignarSeleccionado.setEnabled(haySeleccionOperativa);
        btnAsignarSeleccionados.setEnabled(haySeleccionOperativa);
        actualizarTituloPanelAsignacion(filaPanel, modoMultiple);
        if (modoMultiple) {
            prepararHojaEnvioSimple(null);
            cargarHistorialAsignacion(null);
            actualizarDecisionNumero(null);
            aplicarIdentidadVisual(null, true);
            cargarPanelAsignacionMultiple(obtenerExpedientesMarcados());
            actualizarDatosExpedientePanel(null);
            lblExpedienteSeleccionado.setText("Selección múltiple");
            lblTitularSeleccionado.setText("Múltiples titulares");
            actualizarIdentificacionDocumento(null, null, null);
            lblAbogadoAnalisisAsignacionEtiqueta.setText("Abogado");
            lblAbogadoAnalisisAsignacionValor.setText("Selección múltiple");
            lblAbogadoAnalisisAsignacionValor.setToolTipText("Los expedientes seleccionados pueden tener asignaciones diferentes.");
            aplicarEstadoRecepcion(lblRecepcionAbogado, "No aplica");
            lblGrupoFamiliar.setText("Múltiple");
            lblGrupoFamiliar.setToolTipText("Revise la columna Alertas de cada expediente seleccionado.");
            lblOrigen.setText("Registro / Registrado");
            lblDestino.setText("Asignación / Asignado");
            lblIngreso.setText("Múltiple");
            lblIngreso.setToolTipText("Selección múltiple de expedientes marcados.");
            limpiarDocumentosRelacionadosPanel("Puede asociar la selección al expediente principal elegido automáticamente.");
            cargarPosiblesIntegrantesGrupoFamiliar(null);
            AsociacionRapidaSeleccion seleccionMultiple = obtenerSeleccionAsociacionRapida();
            if (seleccionMultiple != null && seleccionMultiple.ambiguo) {
                lblExpedientePrincipalAsociacion.setText("Ambiguo");
                lblExpedientePrincipalAsociacion.setToolTipText(
                        "Hay más de un expediente con número entre los marcados; solo puede haber un principal.");
                actualizarAccionRelacionadosAmbigua();
            } else {
                actualizarExpedientePrincipalAsociacion(seleccionMultiple == null ? null : seleccionMultiple.expedienteFoco);
                actualizarAccionRelacionadosParaSeleccionMultiple();
            }
        } else if (filaPanel != null && filaPanel.esPrincipal()) {
            AsignacionExpedienteDTO item = filaPanel.principal;
            prepararHojaEnvioSimple(item);
            cargarHistorialAsignacion(item.getIdExpediente());
            cargarPanelAsignacionMultiple(Collections.singletonList(item));
            aplicarIdentidadVisual(item, false);
            lblExpedienteSeleccionado.setText(numeroExpedienteVisual(item));
            lblTitularSeleccionado.setText(titularPrincipalVisual(item));
            actualizarDatosExpedientePanel(item);
            actualizarIdentificacionDocumento(
                    item.getNumeroTramiteDocumentario(),
                    item.getNumeroDocumento(),
                    detalleDocumentoPrincipal(item));
            mostrarAsignacionActual(
                    item.getEquipoAsignado(),
                    item.getIdEquipoResponsable(),
                    item.getAbogadoAsignado(),
                    item.getIdAbogadoResponsable());
            aplicarEstadoRecepcion(lblRecepcionAbogado, estadoRecepcionPrincipal(item));
            aplicarGrupoFamiliarPanel(item);
            lblOrigen.setText("Registro / Registrado");
            lblDestino.setText("Asignación / Asignado");
            if (item.requiereDecisionNumeroAsignacion()) {
                lblIngreso.setText("Requiere decisión de número");
                lblIngreso.setToolTipText("Reconsideración/Apelación sin número: puede asociarse a un expediente principal o generar número.");
            } else {
                lblIngreso.setText(item.getAlertaIngreso());
                lblIngreso.setToolTipText(item.getObservacionSolicitud().isEmpty() ? item.getAlertaIngreso() : item.getObservacionSolicitud());
            }
            lblRelacionados.setText(item.getPosiblesRelacionados() > 0
                    ? item.getPosiblesRelacionados() + " posibles relacionados para este expediente principal."
                    : (item.getAsociadosConfirmados() > 0
                            ? item.getAsociadosConfirmados() + " documento(s) asociado(s) confirmado(s) al principal."
                            : "Sin alerta de relacionados para el expediente principal."));
            if (item.getPosiblesRelacionados() > 0) {
                lblRelacionados.setToolTipText("Se asociarán al expediente principal " + valorUi(item.getNumeroExpediente())
                        + (item.getNumeroExpedienteSgd() == null || item.getNumeroExpedienteSgd().trim().isEmpty()
                                ? ""
                                : " / SGD " + item.getNumeroExpedienteSgd().trim())
                        + ".");
            }
            actualizarExpedientePrincipalAsociacion(item);
            cargarDocumentosRelacionadosPanel(item);
            cargarPosiblesIntegrantesGrupoFamiliar(item);
            actualizarAccionRelacionadosParaPrincipal(item);
            actualizarDecisionNumero(item);
        } else if (filaPanel != null && filaPanel.esAsociada()) {
            prepararHojaEnvioSimple(null);
            cargarHistorialAsignacion(null);
            actualizarDecisionNumero(null);
            aplicarIdentidadVisual(filaPanel.principal, false);
            lblExpedienteSeleccionado.setText("Expediente principal: " + filaPanel.numeroExpedientePrincipal());
            lblTitularSeleccionado.setText(titularPrincipalVisual(filaPanel.principal));
            actualizarDatosExpedientePanel(filaPanel.principal);
            lblExpedienteSeleccionado.setText("Expediente principal: " + filaPanel.numeroExpedientePrincipal());
            actualizarIdentificacionDocumento(
                    filaPanel.asociado.getNumeroTramiteDocumentario(),
                    filaPanel.asociado.getNumeroDocumento(),
                    detalleDocumentoAsociado(filaPanel));
            mostrarAsignacionActual(
                    filaPanel.asociado.getEquipoAsignado(),
                    filaPanel.asociado.getIdEquipoResponsable(),
                    filaPanel.asociado.getAbogadoAsignado(),
                    filaPanel.asociado.getIdAbogadoResponsable());
            aplicarEstadoRecepcion(lblRecepcionAbogado, estadoRecepcionAsociado(filaPanel));
            aplicarGrupoFamiliarPanel(filaPanel.principal);
            lblOrigen.setText("Expediente principal");
            lblDestino.setText(filaPanel.numeroExpedientePrincipal());
            lblIngreso.setText("Duplicado confirmado");
            lblIngreso.setToolTipText("Este documento está asociado al expediente principal y no requiere asignación independiente.");
            limpiarDocumentosRelacionadosPanel("Asociado al expediente principal " + filaPanel.numeroExpedientePrincipal() + ". La asociación se gestiona desde el principal.");
            actualizarExpedientePrincipalAsociacion(filaPanel.principal);
            actualizarAccionRelacionadosParaAsociado(filaPanel);
            cargarPosiblesIntegrantesGrupoFamiliar(filaPanel.principal);
        } else {
            actualizarDecisionNumero(null);
            aplicarIdentidadVisual(null, false);
            cargarPanelAsignacionMultiple(Collections.<AsignacionExpedienteDTO>emptyList());
            lblTitularSeleccionado.setText("-");
            limpiarPanelAsignacion();
            cargarPosiblesIntegrantesGrupoFamiliar(null);
        }
        if (panelDatosExpediente != null) {
            panelDatosExpediente.revalidate();
            panelDatosExpediente.repaint();
        }
        if (panelOperativo != null) {
            panelOperativo.revalidate();
            panelOperativo.repaint();
        }
        if (splitOperativo != null) {
            splitOperativo.revalidate();
            splitOperativo.repaint();
        }
        if (panelDatosExpediente != null || panelAsignacion != null || panelAsociar != null || panelCartasRespuesta != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (panelDatosExpediente != null) {
                        panelDatosExpediente.refreshLayoutNow();
                    }
                    if (panelAsignacion != null) {
                        panelAsignacion.refreshLayoutNow();
                    }
                    if (panelAsociar != null) {
                        panelAsociar.refreshLayoutNow();
                    }
                    if (panelCartasRespuesta != null) {
                        panelCartasRespuesta.refreshLayoutNow();
                    }
                    if (splitOperativo != null) {
                        splitOperativo.revalidate();
                        splitOperativo.repaint();
                    }
                    if (panelOperativo != null) {
                        panelOperativo.revalidate();
                        panelOperativo.repaint();
                    }
                }
            });
        }
    }

    private boolean esBandejaAsignacionActiva() {
        return tabsBandejas == null || tabsBandejas.getSelectedIndex() == TAB_BANDEJA_ASIGNACION;
    }

    private boolean esBandejaCartasActiva() {
        return tabsBandejas != null && tabsBandejas.getSelectedIndex() == TAB_BANDEJA_CARTAS_RESPUESTA;
    }

    private boolean esBandejaCargaActiva() {
        return tabsBandejas != null && tabsBandejas.getSelectedIndex() == TAB_BANDEJA_CARGA_ABOGADOS;
    }

    private void actualizarModoBandejaActiva() {
        if (esBandejaCargaActiva()) {
            modoPanelLateral = MODO_PANEL_OCULTO;
            actualizarVisibilidadPanelAsignacion(false);
            cargarCargaLaboralBandeja();
        } else if (esBandejaCartasActiva()) {
            modoPanelLateral = MODO_PANEL_RESPUESTA;
            if (!TAB_DATOS_EXPEDIENTE.equals(tabAsignacionActiva) && !TAB_PANEL_RESPUESTA.equals(tabAsignacionActiva)) {
                tabAsignacionActiva = TAB_DATOS_EXPEDIENTE;
            }
            if (panelAsignacionCardsLayout != null && panelAsignacionCards != null) {
                panelAsignacionCardsLayout.show(panelAsignacionCards, tabAsignacionActiva);
            }
            cargarBandejaCartasRespuesta();
            actualizarPanelCartasRespuestaSeleccion();
        } else {
            modoPanelLateral = MODO_PANEL_ASIGNACION;
            if (!TAB_DATOS_EXPEDIENTE.equals(tabAsignacionActiva)
                    && !TAB_PANEL_ASIGNACION.equals(tabAsignacionActiva)
                    && !TAB_PANEL_ASOCIAR.equals(tabAsignacionActiva)) {
                tabAsignacionActiva = TAB_DATOS_EXPEDIENTE;
            }
            if (panelAsignacionCardsLayout != null && panelAsignacionCards != null) {
                panelAsignacionCardsLayout.show(panelAsignacionCards, tabAsignacionActiva);
            }
            actualizarPanelSeleccion();
        }
        actualizarLenguetasAsignacion();
        if (panelAsignacionCards != null) {
            panelAsignacionCards.revalidate();
            panelAsignacionCards.repaint();
        }
        if (splitOperativo != null) {
            splitOperativo.revalidate();
            splitOperativo.repaint();
        }
    }

    private void cargarBandejaCartasRespuesta() {
        lblEstadoCartas.setText("Cargando cartas de respuesta...");
        SwingWorker<List<AsignacionCartaRespuestaDTO>, Void> worker = new SwingWorker<List<AsignacionCartaRespuestaDTO>, Void>() {
            @Override
            protected List<AsignacionCartaRespuestaDTO> doInBackground() throws Exception {
                return documentoAnalisisService.listarCartasRespuestaPendientes();
            }

            @Override
            protected void done() {
                try {
                    List<AsignacionCartaRespuestaDTO> items = get();
                    cartasRespuestaPendientes.clear();
                    if (items != null) {
                        cartasRespuestaPendientes.addAll(items);
                    }
                    cargarBandejaCartasRespuestaModel(filtrarCartasKpi(cartasRespuestaPendientes));
                } catch (Exception ex) {
                    cartasRespuestaPendientes.clear();
                    cartasRespuestaVisibles.clear();
                    bandejaCartasRespuestaModel.setRowCount(0);
                    bandejaCartasRespuestaTablePanel.setEmpty(true);
                    lblEstadoCartas.setText("No se pudo cargar la bandeja de cartas de respuesta.");
                }
            }
        };
        worker.execute();
    }

    private void cargarBandejaCartasRespuestaModel(List<AsignacionCartaRespuestaDTO> items) {
        cartasRespuestaVisibles.clear();
        bandejaCartasRespuestaModel.setRowCount(0);
        if (items != null) {
            cartasRespuestaVisibles.addAll(items);
            for (AsignacionCartaRespuestaDTO item : cartasRespuestaVisibles) {
                bandejaCartasRespuestaModel.addRow(new Object[]{
                    valorUi(item.getNumeroExpediente()),
                    valorUi(item.getNumeroExpedienteSgd()),
                    valorUi(item.getTitular()),
                    valorUi(item.getTipoDocumentoNombre()),
                    valorUi(item.getNumeroDocumento()),
                    formatDate(item.getFechaDocumento()),
                    formatDate(item.getFechaPublicacion()),
                    valorUi(item.getEstadoDocumentoNombre())
                });
            }
        }
        bandejaCartasRespuestaTablePanel.setEmpty(bandejaCartasRespuestaModel.getRowCount() == 0);
        actualizarMetricasCartasRespuesta(cartasRespuestaPendientes);
        lblEstadoCartas.setText(bandejaCartasRespuestaModel.getRowCount() == 0
                ? "No hay cartas con respuesta pendiente."
                : bandejaCartasRespuestaModel.getRowCount() + " carta(s) disponibles para respuesta.");
    }

    private void actualizarMetricasCartasRespuesta(List<AsignacionCartaRespuestaDTO> items) {
        int total = 0;
        int conAcuse = 0;
        int pendientes = 0;
        int publicacion = 0;
        if (items != null) {
            total = items.size();
            for (AsignacionCartaRespuestaDTO item : items) {
                if (item.getFechaAcuse() != null) {
                    conAcuse++;
                }
                String confirmacion = item.getConfirmacionRespuesta() == null
                        ? ""
                        : item.getConfirmacionRespuesta().trim().toUpperCase(Locale.ROOT);
                if (!"SI".equals(confirmacion) && !"NO".equals(confirmacion)) {
                    pendientes++;
                }
                if (item.isRequierePublicacion()) {
                    publicacion++;
                }
            }
        }
        cardCartasTotal.setValue(String.valueOf(total));
        cardCartasNotificadas.setValue(String.valueOf(conAcuse));
        cardCartasPendientes.setValue(String.valueOf(pendientes));
        cardCartasPublicacion.setValue(String.valueOf(publicacion));
        marcarKpisCartas();
    }

    private void actualizarPanelCartasRespuestaSeleccion() {
        if (!esBandejaCartasActiva()) {
            return;
        }
        int viewRow = bandejaCartasRespuestaTable.getSelectedRow();
        if (viewRow < 0) {
            cartaRespuestaSeleccionada = null;
            limpiarCartasRespuestaPanel();
            limpiarPanelAsignacion();
            actualizarTituloPanelAsignacionPorItem(null, null);
            actualizarVisibilidadPanelAsignacion(false);
            return;
        }
        int modelRow = bandejaCartasRespuestaTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= cartasRespuestaVisibles.size()) {
            return;
        }
        cartaRespuestaSeleccionada = cartasRespuestaVisibles.get(modelRow);
        if (cartaRespuestaSeleccionada == null || cartaRespuestaSeleccionada.getIdExpediente() == null) {
            return;
        }
        panelAsignacionCerradoPorUsuario = false;
        actualizarVisibilidadPanelAsignacion(true);
        if (!TAB_DATOS_EXPEDIENTE.equals(tabAsignacionActiva)) {
            seleccionarTabAsignacion(TAB_DATOS_EXPEDIENTE);
        } else {
            actualizarLenguetasAsignacion();
        }
        cargarDetalleCartaRespuestaSeleccionada(cartaRespuestaSeleccionada);
    }

    private void cargarDetalleCartaRespuestaSeleccionada(final AsignacionCartaRespuestaDTO carta) {
        lblEstadoCartas.setText("Cargando detalle de la carta seleccionada...");
        SwingWorker<AsignacionExpedienteDTO, Void> worker = new SwingWorker<AsignacionExpedienteDTO, Void>() {
            @Override
            protected AsignacionExpedienteDTO doInBackground() throws Exception {
                return asignacionService.obtenerExpedientePorId(carta.getIdExpediente());
            }

            @Override
            protected void done() {
                try {
                    AsignacionExpedienteDTO expediente = get();
                    actualizarTituloPanelAsignacionPorItem(expediente, null);
                    actualizarDatosExpedientePanel(expediente);
                    aplicarGrupoFamiliarPanel(expediente);
                    cargarCartasRespuestaPorDocumento(carta.getIdExpediente(), carta.getIdDocumentoAnalizado());
                    lblEstadoCartas.setText("Carta lista para registrar respuesta.");
                } catch (Exception ex) {
                    lblEstadoCartas.setText("No se pudo cargar el detalle de la carta seleccionada.");
                }
            }
        };
        worker.execute();
    }

    private void cargarCargaLaboralBandeja() {
        lblEstadoCarga.setText("Cargando carga laboral...");
        SwingWorker<List<CargaLaboralAbogadoDTO>, Void> worker = new SwingWorker<List<CargaLaboralAbogadoDTO>, Void>() {
            @Override
            protected List<CargaLaboralAbogadoDTO> doInBackground() throws Exception {
                return usuarioService.listarCargaLaboralAbogados(null);
            }

            @Override
            protected void done() {
                try {
                    List<CargaLaboralAbogadoDTO> cargas = get();
                    cargasLaborales.clear();
                    cargasLaborales.addAll(cargas);
                    cargarCargaLaboralModel(filtrarCargaLaboralKpi(cargas));
                    actualizarMetricasCargaLaboral(cargas);
                } catch (Exception ex) {
                    cargaLaboralModel.setRowCount(0);
                    cargaLaboralTablePanel.setEmpty(true);
                    lblEstadoCarga.setText("No se pudo cargar la carga laboral.");
                }
            }
        };
        worker.execute();
    }

    private void actualizarMetricasCargaLaboral(List<CargaLaboralAbogadoDTO> cargas) {
        int activos = 0;
        int conCarga = 0;
        int sinCarga = 0;
        int totalSolicitudes = 0;
        if (cargas != null) {
            activos = cargas.size();
            for (CargaLaboralAbogadoDTO carga : cargas) {
                totalSolicitudes += carga.getExpedientesActivos();
                if (carga.getExpedientesActivos() > 0) {
                    conCarga++;
                } else {
                    sinCarga++;
                }
            }
        }
        cardCargaAbogados.setValue(String.valueOf(activos));
        cardCargaConAsignacion.setValue(String.valueOf(conCarga));
        cardCargaSinAsignacion.setValue(String.valueOf(sinCarga));
        cardCargaSolicitudes.setValue(String.valueOf(totalSolicitudes));
        marcarKpisCarga();
    }

    private void actualizarTituloPanelAsignacion(AsignacionTableRow filaPanel, boolean modoMultiple) {
        if (modoMultiple) {
            actualizarTituloPanelAsignacionPorItem(null, "Selección múltiple");
            return;
        }
        actualizarTituloPanelAsignacionPorItem(filaPanel == null ? null : filaPanel.principal, null);
    }

    private void actualizarTituloPanelAsignacionPorItem(AsignacionExpedienteDTO item, String sufijoManual) {
        if (panelAsignacion == null && panelDatosExpediente == null) {
            return;
        }
        String titulo = "Panel de datos";
        String subtitulo = "";
        if (sufijoManual != null && !sufijoManual.trim().isEmpty()) {
            subtitulo = sufijoManual.trim();
        } else if (item != null) {
            String titular = titularPrincipalVisual(item);
            if (!titular.isEmpty() && !"-".equals(titular)) {
                subtitulo = titular;
            }
        }
        if (panelDatosExpediente != null) {
            panelDatosExpediente.setTitle(titulo);
            panelDatosExpediente.setSubtitle(subtitulo);
        }
        if (panelAsociar != null) {
            panelAsociar.setTitle("Panel de Asociación");
        }
        if (panelCartasRespuesta != null) {
            panelCartasRespuesta.setTitle(titulo);
        }
    }

    private String titularPrincipalVisual(AsignacionExpedienteDTO item) {
        if (item == null) {
            return "-";
        }
        String titular = item.getTitular();
        titular = titular == null ? "" : titular.trim();
        return titular.isEmpty() ? "-" : titular;
    }

    private void cargarCartasRespuestaPorDocumento(final Long idExpediente, final Long idDocumentoAnalizado) {
        idExpedienteCartasRespuesta = idExpediente;
        final long secuencia = ++secuenciaCargaCartasRespuesta;
        if (cartasRespuestaTreePanel != null) {
            cartasRespuestaTreePanel.setDocumentos(idExpediente, new ArrayList<DocumentoAnalizadoDTO>(), new ArrayList<DocumentoAnalizadoDTO>());
        }
        SwingWorker<List<DocumentoAnalizadoDTO>, Void> worker = new SwingWorker<List<DocumentoAnalizadoDTO>, Void>() {
            @Override
            protected List<DocumentoAnalizadoDTO> doInBackground() throws Exception {
                return documentoAnalisisService.listarDocumentosAnalizados(idExpediente);
            }

            @Override
            protected void done() {
                if (secuencia != secuenciaCargaCartasRespuesta || !idExpediente.equals(idExpedienteCartasRespuesta)) {
                    return;
                }
                try {
                    List<DocumentoAnalizadoDTO> todos = get();
                    List<DocumentoAnalizadoDTO> documentosAnalisis = new ArrayList<DocumentoAnalizadoDTO>();
                    List<DocumentoAnalizadoDTO> cartasRespuesta = new ArrayList<DocumentoAnalizadoDTO>();
                    for (DocumentoAnalizadoDTO documento : todos) {
                        if (documento == null) {
                            continue;
                        }
                        if (documento.getNivel() == 1) {
                            cartasRespuesta.add(documento);
                        } else if (documento.isRequiereRespuesta()) {
                            documentosAnalisis.add(documento);
                        }
                    }
                    if (cartasRespuestaTreePanel != null) {
                        cartasRespuestaTreePanel.setDocumentos(idExpediente, documentosAnalisis, cartasRespuesta);
                    }
                } catch (Exception ex) {
                    lblEstadoCartas.setText("No se pudieron cargar cartas de respuesta.");
                }
            }
        };
        worker.execute();
    }

    private void limpiarCartasRespuestaPanel() {
        idExpedienteCartasRespuesta = null;
        ++secuenciaCargaCartasRespuesta;
        if (cartasRespuestaTreePanel != null) {
            cartasRespuestaTreePanel.setDocumentos(null, new ArrayList<DocumentoAnalizadoDTO>(), new ArrayList<DocumentoAnalizadoDTO>());
        }
        if (sectionCartasRespuesta != null) {
            sectionCartasRespuesta.setVisible(false);
        }
    }

    private static LocalDate parseFechaUi(String value) {
        if (!hasTextUi(value) || "-".equals(value.trim())) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String normalizarValorEditable(Object value) {
        String text = stringValue(value);
        return "-".equals(text) ? "" : text;
    }

    private static String normalizarFiltroTexto(String value) {
        return AsignacionRegistroEditRules.normalizar(value);
    }

    private static boolean contieneNormalizado(Object value, String criterio) {
        if (criterio == null || criterio.isEmpty()) {
            return true;
        }
        return normalizarFiltroTexto(stringValue(value)).contains(criterio);
    }

    private static String stringValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private DocumentListener simpleDocumentListener(Runnable action) {
        return new DocumentListener() {
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

    private void prepararHojaEnvioSimple(AsignacionExpedienteDTO item) {
        if (item == null || item.getIdExpediente() == null) {
            idExpedienteHojaEnvioSimple = null;
            txtHojaEnvioAsignacion.setText("");
            txtHojaEnvioAsignacion.setEnabled(false);
            txtHojaEnvioAsignacion.setEditable(false);
            txtHojaEnvioAsignacion.setForeground(AppV2Theme.TEXT_SECONDARY);
            txtHojaEnvioAsignacion.setToolTipText("Número de hoja de envío de la asignación.");
            return;
        }
        if (!item.getIdExpediente().equals(idExpedienteHojaEnvioSimple)) {
            idExpedienteHojaEnvioSimple = item.getIdExpediente();
            txtHojaEnvioAsignacion.setText(item.isAsignable() ? "" : item.getNumeroHojaEnvioAsignacion());
        }
        txtHojaEnvioAsignacion.setEnabled(true);
        txtHojaEnvioAsignacion.setEditable(item.isAsignable());
        txtHojaEnvioAsignacion.setForeground(item.isAsignable()
                ? AppV2Theme.TEXT_PRIMARY
                : AppV2Theme.TEXT_SECONDARY);
        txtHojaEnvioAsignacion.setToolTipText(item.isAsignable()
                ? "Ingrese el número de hoja de envío para confirmar la asignación."
                : (item.getNumeroHojaEnvioAsignacion().isEmpty()
                        ? "Este expediente no tiene hoja de envío registrada."
                        : "Hoja de envío registrada durante la asignación."));
    }

    private void actualizarDecisionNumero(AsignacionExpedienteDTO item) {
        boolean habilitado = item != null && item.requiereDecisionNumeroAsignacion();
        if (sectionDecisionNumero != null) {
            sectionDecisionNumero.setVisible(habilitado);
        }
        btnGenerarNumeroExpediente.setEnabled(habilitado);
        btnGenerarNumeroExpediente.setToolTipText(habilitado
                ? "Generar número de expediente para esta Reconsideración/Apelación."
                : "Disponible solo para Reconsideración/Apelación registrada sin número.");
    }

    private void actualizarModoPanelAsignacion(boolean multiple) {
        if (sectionAsignacionMultiple != null) {
            sectionAsignacionMultiple.setVisible(multiple);
        }
        if (sectionDecisionNumero != null && multiple) {
            sectionDecisionNumero.setVisible(false);
        }
        if (sectionHojaEnvioAsignacion != null) {
            sectionHojaEnvioAsignacion.setVisible(false);
        }
        if (sectionComentarioAsignacion != null) {
            sectionComentarioAsignacion.setVisible(false);
        }
        if (panelAsignacion != null) {
            panelAsignacion.revalidate();
            panelAsignacion.repaint();
        }
    }

    private void cargarPanelAsignacionMultiple(List<AsignacionExpedienteDTO> marcados) {
        expedientesAsignacionMultiple.clear();
        asignacionMultipleModel.setRowCount(0);
        Set<Long> idsVigentes = new HashSet<>();
        for (AsignacionExpedienteDTO item : marcados) {
            if (item == null || item.getIdExpediente() == null) {
                continue;
            }
            expedientesAsignacionMultiple.add(item);
            idsVigentes.add(item.getIdExpediente());
            String hojaNuevaPorDefecto = item.isAsignacionActiva() ? "" : valorUi(item.getNumeroHojaEnvioAsignacion());
            asignacionMultipleModel.addRow(new Object[]{
                numeroExpedienteVisual(item),
                valorUi(item.getNumeroExpedienteSgd()),
                hojasEnvioAsignacionMultiple.containsKey(item.getIdExpediente())
                        ? hojasEnvioAsignacionMultiple.get(item.getIdExpediente())
                        : hojaNuevaPorDefecto,
                valorUi(item.getNumeroHojaEnvioAsignacion()),
                item.isAsignacionActiva() ? valorUi(item.getAbogadoAsignado()) : "Nuevo"
            });
        }
        hojasEnvioAsignacionMultiple.keySet().retainAll(idsVigentes);
        ajustarTamanoAsignacionMultiple();
    }

    private void ajustarTamanoAsignacionMultiple() {
        if (asignacionMultipleTable == null || asignacionMultipleScroll == null) {
            return;
        }
        int ancho = 0;
        for (int i = 0; i < asignacionMultipleTable.getColumnCount(); i++) {
            ancho += asignacionMultipleTable.getColumnModel().getColumn(i).getPreferredWidth();
        }
        ancho += asignacionMultipleTable.getIntercellSpacing().width;
        int altoEncabezado = asignacionMultipleTable.getTableHeader() != null
                ? asignacionMultipleTable.getTableHeader().getPreferredSize().height
                : 28;
        int altoFilas = Math.max(1, asignacionMultipleTable.getRowCount()) * asignacionMultipleTable.getRowHeight();
        int alto = altoEncabezado + altoFilas + 8;
        Dimension size = new Dimension(Math.max(ancho, 240), Math.max(alto, 64));
        asignacionMultipleTable.setPreferredScrollableViewportSize(size);
        asignacionMultipleScroll.setPreferredSize(size);
        asignacionMultipleScroll.setMinimumSize(size);
        if (asignacionMultipleTable.getParent() != null) {
            asignacionMultipleTable.revalidate();
            asignacionMultipleTable.repaint();
        }
        asignacionMultipleScroll.revalidate();
        asignacionMultipleScroll.repaint();
    }

    private static String numeroExpedienteVisual(AsignacionExpedienteDTO item) {
        if (item == null || item.getNumeroExpediente().trim().isEmpty()) {
            return "Sin número";
        }
        return item.getNumeroExpediente();
    }

    private void cargarDocumentosRelacionadosPanel(AsignacionExpedienteDTO item) {
        if (item == null || item.getIdExpediente() == null
                || (item.getPosiblesRelacionados() <= 0 && item.getAsociadosConfirmados() <= 0)) {
            limpiarDocumentosRelacionadosPanel("Sin alerta de relacionados.");
            return;
        }
        mostrarSolicitudesAsociadas(true);
        Long idExpediente = item.getIdExpediente();
        if (idExpediente.equals(idExpedienteDocumentosRelacionados)
                && documentosRelacionadosModel.getRowCount() > 0) {
            return;
        }
        idExpedienteDocumentosRelacionados = idExpediente;
        documentosRelacionadosPanel.clear();
        documentosRelacionadosModel.setRowCount(0);
        lblRelacionados.setText("Buscando documentos relacionados...");

        SwingWorker<List<DocumentoRelacionadoFila>, Void> worker = new SwingWorker<List<DocumentoRelacionadoFila>, Void>() {
            @Override
            protected List<DocumentoRelacionadoFila> doInBackground() throws Exception {
                List<DocumentoRelacionadoFila> filas = new ArrayList<DocumentoRelacionadoFila>();
                Set<Long> vistos = new HashSet<Long>();
                for (ExpedienteRelacionadoDTO relacionado : relacionadoDeteccionService.listarPosiblesRelacionados(idExpediente)) {
                    if (relacionado != null && relacionado.getIdExpediente() != null && vistos.add(relacionado.getIdExpediente())) {
                        filas.add(new DocumentoRelacionadoFila(relacionado, false));
                    }
                }
                for (ExpedienteRelacionadoDTO relacionado : relacionadoService.listarAsociadosConfirmados(idExpediente)) {
                    if (relacionado != null && relacionado.getIdExpediente() != null && vistos.add(relacionado.getIdExpediente())) {
                        filas.add(new DocumentoRelacionadoFila(relacionado, true));
                    }
                }
                return filas;
            }

            @Override
            protected void done() {
                if (!idExpediente.equals(idExpedienteDocumentosRelacionados)) {
                    return;
                }
                try {
                    List<DocumentoRelacionadoFila> relacionados = get();
                    documentosRelacionadosPanel.clear();
                    documentosRelacionadosModel.setRowCount(0);
                    int pendientes = 0;
                    int asociados = 0;
                    for (DocumentoRelacionadoFila fila : relacionados) {
                        documentosRelacionadosPanel.add(fila);
                        if (fila.esAsociado()) {
                            asociados++;
                        } else {
                            pendientes++;
                        }
                        documentosRelacionadosModel.addRow(new Object[]{
                            fila.esAsociado() ? null : Boolean.TRUE,
                            numeroExpedienteDocumentoRelacionado(fila),
                            textoDocumentoRelacionado(fila),
                            estadoDocumentoRelacionado(fila),
                            fechaAsociacionDocumentoRelacionado(fila),
                            fila.esAsociado() ? "Eliminar" : ""
                        });
                    }
                    ajustarTamanoDocumentosRelacionados();
                    mostrarSolicitudesAsociadas(!relacionados.isEmpty());
                    actualizarBotonAsociarPorSeleccionTabla();
                    if (!relacionados.isEmpty()) {
                        if (pendientes > 0 && asociados > 0) {
                            lblRelacionados.setText(pendientes + " documento(s) pendiente(s) de asociar y "
                                    + asociados + " documento(s) ya asociado(s).");
                        } else if (pendientes > 0) {
                            lblRelacionados.setText(pendientes + " documento(s) pendiente(s) de asociar.");
                        } else {
                            lblRelacionados.setText(asociados + " documento(s) asociado(s).");
                        }
                    } else {
                        lblRelacionados.setText("Sin documentos relacionados pendientes.");
                    }
                } catch (Exception ex) {
                    limpiarDocumentosRelacionadosPanel("No se pudo consultar documentos relacionados.");
                }
            }
        };
        worker.execute();
    }

    private void limpiarDocumentosRelacionadosPanel(String mensaje) {
        idExpedienteDocumentosRelacionados = null;
        documentosRelacionadosPanel.clear();
        documentosRelacionadosModel.setRowCount(0);
        ajustarTamanoDocumentosRelacionados();
        mostrarSolicitudesAsociadas(false);
        lblRelacionados.setText(mensaje == null || mensaje.trim().isEmpty()
                ? "Sin alerta de relacionados."
                : mensaje);
    }

    private void cargarPosiblesIntegrantesGrupoFamiliar(AsignacionExpedienteDTO expedientePrincipal) {
        expedienteFocoGrupoFamiliar = expedientePrincipal;
        Long idExpediente = expedientePrincipal == null ? null : expedientePrincipal.getIdExpediente();
        if (idExpediente == null) {
            candidatosGrupoFamiliarActuales = new ArrayList<>();
            integrantesGrupoFamiliarModel.setRowCount(0);
            lblEstadoDeteccionGrupoFamiliar.setText("Seleccione un expediente en la bandeja.");
            actualizarBotonAsociarGrupoFamiliar();
            grupoFamiliarActualModel.setRowCount(0);
            lblEstadoGrupoFamiliarActual.setText("Sin grupo familiar.");
            return;
        }
        lblEstadoDeteccionGrupoFamiliar.setText("Buscando posibles integrantes...");
        cargarGrupoFamiliarActual(idExpediente);
        final long sequence = ++secuenciaCargaGrupoFamiliar;
        SwingWorker<List<GrupoFamiliarCandidatoDTO>, Void> worker =
                new SwingWorker<List<GrupoFamiliarCandidatoDTO>, Void>() {
            @Override
            protected List<GrupoFamiliarCandidatoDTO> doInBackground() throws Exception {
                return grupoFamiliarService.listarPosiblesIntegrantes(idExpediente);
            }

            @Override
            protected void done() {
                if (sequence != secuenciaCargaGrupoFamiliar) {
                    return;
                }
                try {
                    candidatosGrupoFamiliarActuales = get();
                } catch (Exception ex) {
                    candidatosGrupoFamiliarActuales = new ArrayList<>();
                    lblEstadoDeteccionGrupoFamiliar.setText("No se pudieron consultar posibles integrantes.");
                }
                integrantesGrupoFamiliarModel.setRowCount(0);
                for (GrupoFamiliarCandidatoDTO candidato : candidatosGrupoFamiliarActuales) {
                    integrantesGrupoFamiliarModel.addRow(new Object[]{
                            Boolean.TRUE,
                            valorUi(candidato.getNumeroExpediente()),
                            valorUi(candidato.getTitular()),
                            DisplayNameMapperV2.etapa(candidato.getEtapaCodigo()) + " / "
                                    + DisplayNameMapperV2.estado(candidato.getEstadoCodigo()),
                            valorUi(candidato.getAbogadoAsignado())
                    });
                }
                if (candidatosGrupoFamiliarActuales.isEmpty()) {
                    lblEstadoDeteccionGrupoFamiliar.setText("No se detectaron posibles integrantes por apellidos.");
                } else {
                    lblEstadoDeteccionGrupoFamiliar.setText(
                            candidatosGrupoFamiliarActuales.size() + " posible(s) integrante(s) detectado(s).");
                }
                actualizarBotonAsociarGrupoFamiliar();
            }
        };
        worker.execute();
    }

    private void cargarGrupoFamiliarActual(Long idExpediente) {
        lblEstadoGrupoFamiliarActual.setText("Consultando grupo familiar...");
        SwingWorker<List<GrupoFamiliarIntegranteDTO>, Void> worker =
                new SwingWorker<List<GrupoFamiliarIntegranteDTO>, Void>() {
            @Override
            protected List<GrupoFamiliarIntegranteDTO> doInBackground() throws Exception {
                return grupoFamiliarService.listarIntegrantesGrupoFamiliar(idExpediente);
            }

            @Override
            protected void done() {
                List<GrupoFamiliarIntegranteDTO> integrantes;
                try {
                    integrantes = get();
                } catch (Exception ex) {
                    integrantes = new ArrayList<>();
                }
                grupoFamiliarActualModel.setRowCount(0);
                for (GrupoFamiliarIntegranteDTO integrante : integrantes) {
                    grupoFamiliarActualModel.addRow(new Object[]{
                            valorUi(integrante.getNombreCompleto()),
                            valorUi(integrante.getNumeroExpediente()),
                            DisplayNameMapperV2.etapa(integrante.getEtapaCodigo()) + " / "
                                    + DisplayNameMapperV2.estado(integrante.getEstadoCodigo()),
                            valorUi(integrante.getAbogadoAsignado())
                    });
                }
                lblEstadoGrupoFamiliarActual.setText(integrantes.isEmpty()
                        ? "Este expediente aún no pertenece a un grupo familiar."
                        : integrantes.size() + " persona(s) en el grupo familiar.");
            }
        };
        worker.execute();
    }

    private void actualizarBotonAsociarGrupoFamiliar() {
        int marcados = 0;
        for (int i = 0; i < integrantesGrupoFamiliarModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(integrantesGrupoFamiliarModel.getValueAt(i, 0))) {
                marcados++;
            }
        }
        if (marcados <= 0) {
            btnAsociarGrupoFamiliar.setText("Sin integrantes marcados");
            btnAsociarGrupoFamiliar.setEnabled(false);
            btnAsociarGrupoFamiliar.setToolTipText("Marque al menos una coincidencia para habilitar la asociación.");
            return;
        }
        btnAsociarGrupoFamiliar.setText("Asociar al grupo familiar (" + marcados + ")");
        btnAsociarGrupoFamiliar.setEnabled(true);
        btnAsociarGrupoFamiliar.setToolTipText(
                "Asocia las personas marcadas con el expediente actualmente seleccionado en un mismo grupo familiar.");
    }

    private void asociarGrupoFamiliarSeleccion() {
        if (expedienteFocoGrupoFamiliar == null || expedienteFocoGrupoFamiliar.getIdExpediente() == null) {
            return;
        }
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < integrantesGrupoFamiliarModel.getRowCount() && i < candidatosGrupoFamiliarActuales.size(); i++) {
            if (Boolean.TRUE.equals(integrantesGrupoFamiliarModel.getValueAt(i, 0))) {
                Long idCandidato = candidatosGrupoFamiliarActuales.get(i).getIdExpediente();
                if (idCandidato != null) {
                    ids.add(idCandidato);
                }
            }
        }
        if (ids.isEmpty()) {
            mostrarInfo("Marque al menos un posible integrante para asociar.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Se asociarán " + ids.size() + " persona(s) al mismo grupo familiar del expediente seleccionado.\n"
                        + "Solo se marca \"Sí\" en Grupo Familiar; no se asocian expedientes entre sí.\n\n"
                        + "¿Desea continuar?",
                "Grupo Familiar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        final Long idPrincipal = expedienteFocoGrupoFamiliar.getIdExpediente();
        setTrabajando(true, "Asociando grupo familiar...");
        SwingWorker<GrupoFamiliarResultadoDTO, Void> worker = new SwingWorker<GrupoFamiliarResultadoDTO, Void>() {
            @Override
            protected GrupoFamiliarResultadoDTO doInBackground() throws Exception {
                return grupoFamiliarService.asociarGrupoFamiliar(idPrincipal, ids);
            }

            @Override
            protected void done() {
                try {
                    GrupoFamiliarResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelAsignacionV2.this,
                            resultado.getMensaje(),
                            "Grupo Familiar",
                            JOptionPane.INFORMATION_MESSAGE);
                    buscar();
                    cargarPosiblesIntegrantesGrupoFamiliar(expedienteFocoGrupoFamiliar);
                } catch (Exception ex) {
                    mostrarError("No se pudo asociar el grupo familiar.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void mostrarSolicitudesAsociadas(boolean visible) {
        if (panelSolicitudesAsociadas == null) {
            return;
        }
        panelSolicitudesAsociadas.setVisible(visible);
        if (sectionResumenAsignacion != null) {
            sectionResumenAsignacion.revalidate();
            sectionResumenAsignacion.repaint();
        }
    }

    private String textoDocumentoRelacionado(ExpedienteRelacionadoDTO relacionado) {
        if (relacionado == null) {
            return "-";
        }
        String sgd = valorUi(relacionado.getNumeroExpedienteSgd());
        if (!"-".equals(sgd)) {
            return sgd;
        }
        String numeroExpediente = valorUi(relacionado.getNumeroExpediente());
        if (!"-".equals(numeroExpediente)) {
            return numeroExpediente;
        }
        return valorUi(relacionado.getNumeroDocumento());
    }

    private String textoDocumentoRelacionado(DocumentoRelacionadoFila fila) {
        return fila == null ? "-" : textoDocumentoRelacionado(fila.getExpediente());
    }

    private String numeroExpedienteDocumentoRelacionado(DocumentoRelacionadoFila fila) {
        if (fila == null || fila.getExpediente() == null || !tieneNumeroExpediente(fila.getExpediente())) {
            return "Sin número (potencial duplicado)";
        }
        return fila.getExpediente().getNumeroExpediente().trim();
    }

    private static boolean tieneNumeroExpediente(ExpedienteRelacionadoDTO expediente) {
        return expediente != null
                && expediente.getNumeroExpediente() != null
                && !expediente.getNumeroExpediente().trim().isEmpty();
    }

    private String fechaAsociacionDocumentoRelacionado(DocumentoRelacionadoFila fila) {
        if (fila == null || fila.getExpediente() == null || fila.getExpediente().getFechaAsociacion() == null) {
            return "-";
        }
        return DATE_FORMAT.format(fila.getExpediente().getFechaAsociacion());
    }

    private String estadoDocumentoRelacionado(DocumentoRelacionadoFila fila) {
        if (fila == null) {
            return "-";
        }
        return fila.esAsociado() ? "Asociado" : "Pendiente de asociar";
    }

    private static boolean pareceIdentificadorTecnico(String value) {
        return value != null && value.trim().matches("\\d{1,4}");
    }

    private void actualizarIdentificacionDocumento(String tramiteWeb, String numeroDocumento, String tooltip) {
        lblTramiteWebSeleccionado.setText(valorUi(tramiteWeb));
        lblNumeroDocumentoSeleccionado.setText(valorUi(numeroDocumento));
        lblTramiteWebSeleccionado.setToolTipText(tooltip);
        lblNumeroDocumentoSeleccionado.setToolTipText(tooltip);
    }

    private void actualizarDatosExpedientePanel(AsignacionExpedienteDTO item) {
        if (item == null) {
            lblResultadoInicialSeleccionado.setText("-");
            lblExpedienteSeleccionado.setText("-");
            lblTitularSeleccionado.setText("-");
            lblExpedienteSgdSeleccionado.setText("-");
            lblTipoDocumentoSeleccionado.setText("-");
            lblProcedimientoSeleccionado.setText("-");
            lblTipoSolicitudSeleccionada.setText("-");
            lblCanalIngresoSeleccionado.setText("-");
            lblPrioridadSeleccionada.setText("-");
            lblTipoActaSeleccionada.setText("-");
            lblNumeroActaSeleccionada.setText("-");
            lblTipoDocumentoTitularSeleccionado.setText("-");
            lblTipoDocumentoSolicitanteSeleccionado.setText("-");
            lblNumeroDocumentoSolicitanteSeleccionado.setText("-");
            lblCorreoNotificacionSeleccionado.setText("-");
            lblTelefonoNotificacionSeleccionado.setText("-");
            lblDepartamentoSeleccionado.setText("-");
            lblProvinciaSeleccionada.setText("-");
            lblDistritoSeleccionado.setText("-");
            lblDireccionNotificacionSeleccionada.setText("-");
            lblDireccionNotificacionSeleccionada.setToolTipText(null);
            lblActaSeleccionada.setText("-");
            lblSolicitanteSeleccionado.setText("-");
            lblDocumentoTitularSeleccionado.setText("-");
            lblFechaSolicitudSeleccionada.setText("-");
            actualizarBadgeDias(lblDiasSeleccionados, null);
            lblFechaVencimientoSeleccionada.setText("-");
            lblEstadoSeleccionado.setText("-");
            lblHojaEnvioSeleccionada.setText("-");
            lblObservacionSeleccionada.setText("-");
            lblObservacionSeleccionada.setToolTipText(null);
            return;
        }
        lblResultadoInicialSeleccionado.setText("Corresponde a SDRERC");
        lblExpedienteSeleccionado.setText(numeroExpedienteVisual(item));
        lblTitularSeleccionado.setText(titularPrincipalVisual(item));
        lblExpedienteSgdSeleccionado.setText(valorUi(item.getNumeroExpedienteSgd()));
        lblTipoDocumentoSeleccionado.setText(valorUi(item.getTipoDocumento()));
        lblProcedimientoSeleccionado.setText(valorUi(item.getProcedimiento()));
        lblTipoSolicitudSeleccionada.setText(valorUi(item.getTipoSolicitud()));
        lblCanalIngresoSeleccionado.setText(valorUi(item.getCanalIngreso()));
        lblPrioridadSeleccionada.setText("-");
        lblTipoActaSeleccionada.setText(valorUi(item.getTipoActa()));
        lblNumeroActaSeleccionada.setText(valorUi(item.getNumeroActa()));
        lblTipoDocumentoTitularSeleccionado.setText(valorUi(item.getTipoDocumentoTitular()));
        lblTipoDocumentoSolicitanteSeleccionado.setText(valorUi(item.getTipoDocumentoSolicitante()));
        lblNumeroDocumentoSolicitanteSeleccionado.setText(valorUi(item.getNumeroDocumentoSolicitante()));
        lblActaSeleccionada.setText(valorUi(item.getTipoActa()) + " / " + valorUi(item.getNumeroActa()));
        lblSolicitanteSeleccionado.setText(valorUi(item.getSolicitante()));
        lblDocumentoTitularSeleccionado.setText(valorUi(item.getNumeroDocumentoTitular()));
        lblCorreoNotificacionSeleccionado.setText(valorUi(item.getCorreoSolicitante()));
        lblTelefonoNotificacionSeleccionado.setText(valorUi(item.getTelefonoSolicitante()));
        lblDepartamentoSeleccionado.setText(valorUi(item.getDepartamentoSolicitante()));
        lblProvinciaSeleccionada.setText(valorUi(item.getProvinciaSolicitante()));
        lblDistritoSeleccionado.setText(valorUi(item.getDistritoSolicitante()));
        lblDireccionNotificacionSeleccionada.setText(valorUi(item.getDireccionSolicitante()));
        lblDireccionNotificacionSeleccionada.setToolTipText(valorUi(item.getDireccionSolicitante()));
        lblFechaSolicitudSeleccionada.setText(formatDate(item.getFechaRecepcion()));
        actualizarBadgeDias(lblDiasSeleccionados, item.getDiasRestantes());
        lblFechaVencimientoSeleccionada.setText(formatDate(item.getFechaVencimiento()));
        lblEstadoSeleccionado.setText(DisplayNameMapperV2.estado(item.getEstadoCodigo()));
        lblHojaEnvioSeleccionada.setText(valorUi(item.getNumeroHojaEnvioAsignacion()));
        lblObservacionSeleccionada.setText(valorUi(item.getObservacionSolicitud()));
        lblObservacionSeleccionada.setToolTipText(valorUi(item.getObservacionSolicitud()));
    }

    private String detalleDocumentoPrincipal(AsignacionExpedienteDTO item) {
        if (item == null) {
            return null;
        }
        return "Trámite Web: " + valorUi(item.getNumeroTramiteDocumentario())
                + " | N° Documento: " + valorUi(item.getNumeroDocumento())
                + " | Procedimiento: " + valorUi(item.getProcedimiento())
                + " | Tipo acta: " + valorUi(item.getTipoActa())
                + " | N° acta: " + valorUi(item.getNumeroActa())
                + " | Titular: " + valorUi(item.getTitular())
                + " | Solicitante: " + valorUi(item.getSolicitante());
    }

    private String detalleDocumentoAsociado(AsignacionTableRow row) {
        if (row == null || row.asociado == null) {
            return null;
        }
        ExpedienteRelacionadoDTO asociado = row.asociado;
        return "Expediente principal: " + row.numeroExpedientePrincipal()
                + " | Trámite Web: " + valorUi(asociado.getNumeroTramiteDocumentario())
                + " | N° Documento: " + valorUi(asociado.getNumeroDocumento())
                + " | Procedimiento: " + procedimientoAsociado(asociado)
                + " | Tipo acta: " + valorUi(asociado.getTipoActa())
                + " | N° acta: " + valorUi(asociado.getNumeroActa())
                + " | Titular: " + valorUi(asociado.getTitular())
                + " | Solicitante: " + valorUi(asociado.getSolicitante())
                + " | Relación: " + textoRelacionAsociada(asociado);
    }

    private String estadoRecepcionPrincipal(AsignacionExpedienteDTO item) {
        if (item == null) {
            return "-";
        }
        if (item.getAbogadoAsignado().isEmpty()) {
            return "Sin abogado asignado";
        }
        if ("RECIBIDO_POR_ABOGADO".equalsIgnoreCase(item.getEstadoCodigo())) {
            return "Recibido por abogado";
        }
        return "Pendiente de recibir";
    }

    private String estadoRecepcionAsociado(AsignacionTableRow row) {
        if (row == null || row.asociado == null) {
            return "-";
        }
        String abogado = abogadoAsociado(row.principal, row.asociado);
        if (abogado == null || abogado.trim().isEmpty()) {
            return "Sin abogado asignado";
        }
        if (row.asociado.isRecibidoPorAbogado()) {
            return "Recibido por abogado";
        }
        return "Pendiente de recibir";
    }

    private void aplicarGrupoFamiliarPanel(AsignacionExpedienteDTO item) {
        if (item == null) {
            lblGrupoFamiliar.setText("No");
            lblGrupoFamiliar.setToolTipText(null);
            lblMarcaOperativaSeleccionada.setText("No");
            lblMarcaOperativaSeleccionada.setToolTipText(null);
            return;
        }
        String valorGrupo = item.isGrupoFamiliar() ? "Sí" : "No";
        lblGrupoFamiliar.setText(valorGrupo);
        lblMarcaOperativaSeleccionada.setText(valorGrupo);
        StringBuilder tooltip = new StringBuilder();
        if (!item.getCriterioGrupoFamiliar().isEmpty()) {
            tooltip.append("Criterio: ").append(item.getCriterioGrupoFamiliar());
        }
        if (!item.getObservacionGrupoFamiliar().isEmpty()) {
            if (tooltip.length() > 0) {
                tooltip.append(" | ");
            }
            tooltip.append(item.getObservacionGrupoFamiliar());
        }
        if (item.isGrupoFamiliar() || item.isPosibleGrupoFamiliar()) {
            if (tooltip.length() > 0) {
                tooltip.append(" | ");
            }
            tooltip.append("Considere asignar solicitudes relacionadas al mismo abogado.");
        }
        lblGrupoFamiliar.setToolTipText(tooltip.length() == 0 ? null : tooltip.toString());
        lblMarcaOperativaSeleccionada.setToolTipText(tooltip.length() == 0 ? null : tooltip.toString());
    }

    private void aplicarEstadoRecepcion(JLabel label, String estado) {
        String text = estado == null || estado.trim().isEmpty() ? "-" : estado.trim();
        label.setText(text);
        label.setToolTipText(text);
        if ("Recibido por abogado".equalsIgnoreCase(text)) {
            label.setForeground(AppV2Theme.SUCCESS);
        } else if ("Pendiente de recibir".equalsIgnoreCase(text)) {
            label.setForeground(AppV2Theme.WARNING);
        } else {
            label.setForeground(AppV2Theme.TEXT_SECONDARY);
        }
    }

    private void eliminarDocumentoRelacionado(int modelRow) {
        DocumentoRelacionadoFila fila = documentoRelacionadoFila(modelRow);
        if (fila == null || fila.getExpediente() == null || fila.getExpediente().getIdExpediente() == null || !fila.esAsociado()) {
            mostrarInfo("Seleccione un documento asociado válido para eliminar la relación.");
            return;
        }
        Long idPrincipal = idExpedienteDocumentosRelacionados;
        if (idPrincipal == null) {
            mostrarInfo("Seleccione un expediente principal válido.");
            return;
        }
        ExpedienteRelacionadoDTO relacionado = fila.getExpediente();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Se eliminará la asociación del documento " + textoDocumentoRelacionado(relacionado)
                        + ".\nEl expediente volverá a mostrarse como independiente y sin número heredado.\n"
                        + "¿Desea continuar?",
                "Eliminar asociación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setTrabajando(true, "Eliminando asociación...");
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return relacionadoService.desasociarRelacionado(
                        idPrincipal,
                        relacionado.getIdExpediente(),
                        "Asociación eliminada desde Asignación.");
            }

            @Override
            protected void done() {
                try {
                    String mensaje = get();
                    JOptionPane.showMessageDialog(
                            JPanelAsignacionV2.this,
                            mensaje,
                            "Asociación eliminada",
                            JOptionPane.INFORMATION_MESSAGE);
                    buscar();
                } catch (Exception ex) {
                    mostrarError("No se pudo eliminar la asociación del expediente relacionado.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private AsignacionExpedienteDTO buscarExpedientePrincipal(Long idPrincipal) {
        if (idPrincipal == null) {
            return null;
        }
        for (AsignacionExpedienteDTO item : expedientes) {
            if (idPrincipal.equals(item.getIdExpediente())) {
                return item;
            }
        }
        for (AsignacionTableRow row : filasTabla) {
            if (row != null && row.esPrincipal() && idPrincipal.equals(row.principal.getIdExpediente())) {
                return row.principal;
            }
        }
        return null;
    }

    private boolean usuarioActualEsAbogadoResponsable(AsignacionExpedienteDTO principal) {
        if (principal == null || principal.getIdAbogadoResponsable() == null) {
            return false;
        }
        Long idUsuarioActual = resolverIdUsuarioActualSdrercApp();
        return idUsuarioActual != null && idUsuarioActual.equals(principal.getIdAbogadoResponsable());
    }

    private Long resolverIdUsuarioActualSdrercApp() {
        if (usuarioActualResuelto) {
            return idUsuarioActualSdrercApp;
        }
        usuarioActualResuelto = true;
        try {
            idUsuarioActualSdrercApp = usuarioService.obtenerIdUsuarioActivoPorUsername(SessionContext.getUsername());
        } catch (Exception ex) {
            idUsuarioActualSdrercApp = null;
        }
        return idUsuarioActualSdrercApp;
    }

    private AsignacionTableRow filaParaPanel(int modelRow, int marcados) {
        if (marcados > 1) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (!Boolean.TRUE.equals(tableModel.getValueAt(i, COL_SELECCION))) {
                    continue;
                }
                AsignacionTableRow row = filaTabla(i);
                if (row != null && row.esPrincipal()) {
                    return row;
                }
            }
            return null;
        }
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, COL_SELECCION))) {
                return filaTabla(i);
            }
        }
        return filaTabla(modelRow);
    }

    private void aplicarIdentidadVisual(AsignacionExpedienteDTO item, boolean multiple) {
        if (item != null) {
            acentoSeleccion = PANEL_ASSIGNMENT_ACCENT;
            fondoSeleccion = TABLE_SELECTION_BACKGROUND;
            contextoChip = "Expediente en foco: " + item.getNumeroExpediente();
        } else if (multiple) {
            acentoSeleccion = PANEL_ASSIGNMENT_ACCENT;
            fondoSeleccion = TABLE_SELECTION_BACKGROUND;
            contextoChip = "Selección múltiple";
        } else {
            acentoSeleccion = PANEL_ASSIGNMENT_ACCENT;
            fondoSeleccion = TABLE_SELECTION_BACKGROUND;
            contextoChip = "Panel de asignación";
        }
        actualizarLenguetasAsignacion();
        if (panelAsignacion != null) {
            panelAsignacion.setAccentColor(acentoSeleccion);
        }
        table.setSelectionBackground(TABLE_SELECTION_BACKGROUND);
        table.setSelectionForeground(TABLE_SELECTION_FOREGROUND);
        table.repaint();
    }

    private void seleccionarTabAsignacion(String tab) {
        if (tab == null || panelAsignacionCardsLayout == null || panelAsignacionCards == null) {
            return;
        }
        if (!TAB_DATOS_EXPEDIENTE.equals(tab)
                && !TAB_PANEL_ASIGNACION.equals(tab)
                && !TAB_PANEL_ASOCIAR.equals(tab)
                && !TAB_PANEL_GRUPO_FAMILIAR.equals(tab)
                && !TAB_PANEL_RESPUESTA.equals(tab)) {
            return;
        }
        if (MODO_PANEL_RESPUESTA.equals(modoPanelLateral)
                && !TAB_DATOS_EXPEDIENTE.equals(tab)
                && !TAB_PANEL_RESPUESTA.equals(tab)) {
            return;
        }
        if (MODO_PANEL_ASIGNACION.equals(modoPanelLateral)
                && !TAB_DATOS_EXPEDIENTE.equals(tab)
                && !TAB_PANEL_ASIGNACION.equals(tab)
                && !TAB_PANEL_ASOCIAR.equals(tab)
                && !TAB_PANEL_GRUPO_FAMILIAR.equals(tab)) {
            return;
        }
        boolean mismaTab = tab.equals(tabAsignacionActiva);
        tabAsignacionActiva = tab;
        panelAsignacionCardsLayout.show(panelAsignacionCards, tab);
        if (splitOperativo != null && panelAsignacionVisible && mismaTab) {
            splitOperativo.setSideExpanded(!splitOperativo.isSideExpanded());
        }
        actualizarLenguetasAsignacion();
        if (TAB_PANEL_GRUPO_FAMILIAR.equals(tab)) {
            cargarPosiblesIntegrantesGrupoFamiliar(obtenerExpedienteFoco());
        }
        panelAsignacionCards.revalidate();
        panelAsignacionCards.repaint();
    }

    private void actualizarLenguetasAsignacion() {
        boolean expandido = splitOperativo != null && splitOperativo.isSideExpanded();
        List<AppV2StackedSideTab> visibles = obtenerLenguetasVisibles();
        tabDatosExpediente.setVisible(visibles.contains(tabDatosExpediente));
        tabPanelAsignacionOperativa.setVisible(visibles.contains(tabPanelAsignacionOperativa));
        tabPanelAsociar.setVisible(visibles.contains(tabPanelAsociar));
        tabPanelGrupoFamiliar.setVisible(visibles.contains(tabPanelGrupoFamiliar));
        tabPanelRespuesta.setVisible(visibles.contains(tabPanelRespuesta));
        tabDatosExpediente.setState(TAB_DATOS_EXPEDIENTE.equals(tabAsignacionActiva), TAB_DATOS_EXPEDIENTE.equals(tabAsignacionActiva) && expandido);
        tabPanelAsignacionOperativa.setState(TAB_PANEL_ASIGNACION.equals(tabAsignacionActiva), TAB_PANEL_ASIGNACION.equals(tabAsignacionActiva) && expandido);
        tabPanelAsociar.setState(TAB_PANEL_ASOCIAR.equals(tabAsignacionActiva), TAB_PANEL_ASOCIAR.equals(tabAsignacionActiva) && expandido);
        tabPanelGrupoFamiliar.setState(TAB_PANEL_GRUPO_FAMILIAR.equals(tabAsignacionActiva), TAB_PANEL_GRUPO_FAMILIAR.equals(tabAsignacionActiva) && expandido);
        tabPanelRespuesta.setState(TAB_PANEL_RESPUESTA.equals(tabAsignacionActiva), TAB_PANEL_RESPUESTA.equals(tabAsignacionActiva) && expandido);
        tabDatosExpediente.setToolTipText("Datos del expediente · " + contextoChip);
        tabPanelAsignacionOperativa.setToolTipText("Panel de asignación · " + contextoChip);
        tabPanelAsociar.setToolTipText("Asociar expedientes · " + contextoChip);
        tabPanelGrupoFamiliar.setToolTipText("Grupo familiar · " + contextoChip);
        tabPanelRespuesta.setToolTipText("Respuesta de carta · " + contextoChip);
    }

    private int indicePaleta(Long idExpediente) {
        long value = idExpediente == null ? 0L : idExpediente.longValue();
        return (int) Math.abs(value % GROUP_STRIPE_COLORS.length);
    }

    private Color acentoGrupo(Long idExpedientePrincipal) {
        return GROUP_STRIPE_COLORS[indicePaleta(idExpedientePrincipal)];
    }

    private Color colorFondoFila(int row, AsignacionTableRow fila, boolean isSelected) {
        if (fila != null && fila.esAsociada()) {
            return isSelected ? TABLE_SELECTION_BACKGROUND : EXPANDED_ASSOCIATED_BACKGROUND;
        }
        if (fila != null
                && fila.esPrincipal()
                && fila.principal.getIdExpediente() != null
                && principalesExpandidos.contains(fila.principal.getIdExpediente())) {
            return EXPANDED_PARENT_BACKGROUND;
        }
        if (isSelected) {
            return TABLE_SELECTION_BACKGROUND;
        }
        return row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT;
    }

    private boolean debeMostrarBarraGrupo(AsignacionTableRow fila) {
        if (fila == null || fila.getIdPrincipal() == null) {
            return false;
        }
        if (fila.esAsociada()) {
            return true;
        }
        return fila.esPrincipal()
                && fila.principal.getIdExpediente() != null
                && fila.principal.getAsociadosConfirmados() > 0
                && principalesExpandidos.contains(fila.principal.getIdExpediente());
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

    private javax.swing.border.Border bordeContenidoAsociado(int modelRow, int left, int right) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        esPrimerAsociado(modelRow) ? 1 : 0,
                        0,
                        esUltimoAsociado(modelRow) ? 1 : 0,
                        0,
                        ASSOCIATED_BLOCK_BORDER),
                BorderFactory.createEmptyBorder(0, left, 0, right));
    }

    private void limpiarPanelAsignacion() {
        actualizarDatosExpedientePanel(null);
        actualizarIdentificacionDocumento(null, null, null);
        mostrarAsignacionActual(null, null, null, null);
        aplicarEstadoRecepcion(lblRecepcionAbogado, "-");
        aplicarGrupoFamiliarPanel(null);
        lblOrigen.setText("Registro / Registrado");
        lblDestino.setText("Asignación / Asignado");
        lblIngreso.setText("Normal");
        lblIngreso.setToolTipText(null);
        limpiarDocumentosRelacionadosPanel("Sin alerta de relacionados.");
        actualizarExpedientePrincipalAsociacion(null);
        actualizarAccionRelacionadosSinSeleccion();
        actualizarDecisionNumero(null);
        idExpedienteHojaEnvioSimple = null;
        txtHojaEnvioAsignacion.setText("");
        txtHojaEnvioAsignacion.setEnabled(false);
        txtComentario.setText("");
    }

    private void cerrarPanelAsignacion() {
        if (contarSeleccionOperativa() == 0) {
            return;
        }
        panelAsignacionCerradoPorUsuario = true;
        actualizarVisibilidadPanelAsignacion(false);
    }

    private void abrirPanelAsignacion() {
        if (contarSeleccionOperativa() == 0) {
            return;
        }
        panelAsignacionCerradoPorUsuario = false;
        actualizarVisibilidadPanelAsignacion(true);
        actualizarPanelSeleccion();
    }

    private void alternarExpansionPanelAsignacion() {
        if (splitOperativo == null || !panelAsignacionVisible) {
            return;
        }
        splitOperativo.toggleSideExpanded();
        actualizarLenguetasAsignacion();
        panelOperativo.revalidate();
        panelOperativo.repaint();
    }

    private void actualizarVisibilidadPanelAsignacion(boolean mostrar) {
        if (panelAsignacion == null || splitOperativo == null || panelAsignacionVisible == mostrar) {
            return;
        }
        panelAsignacionVisible = mostrar;
        splitOperativo.setSideVisible(mostrar);
        actualizarLenguetasAsignacion();
        panelOperativo.revalidate();
        panelOperativo.repaint();
        if (mostrar) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (panelDatosExpediente != null) {
                        panelDatosExpediente.refreshLayoutNow();
                    }
                    if (panelAsignacion != null) {
                        panelAsignacion.refreshLayoutNow();
                    }
                    if (panelAsociar != null) {
                        panelAsociar.refreshLayoutNow();
                    }
                    if (panelCartasRespuesta != null) {
                        panelCartasRespuesta.refreshLayoutNow();
                    }
                    if (splitOperativo != null) {
                        splitOperativo.revalidate();
                        splitOperativo.repaint();
                    }
                }
            });
        }
    }

    private int contarSeleccionOperativa() {
        int marcados = contarSeleccionados();
        if (marcados > 0) {
            return marcados;
        }
        int modelRow = obtenerModelRowSeleccionada();
        return filaTabla(modelRow) != null ? 1 : 0;
    }

    private int contarSeleccionados() {
        int total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            AsignacionTableRow row = filaTabla(i);
            if (esFilaSeleccionableAsignacion(row)
                    && Boolean.TRUE.equals(tableModel.getValueAt(i, COL_SELECCION))) {
                total++;
            }
        }
        return total;
    }

    private void actualizarEstadoHeaderSeleccion() {
        int visiblesAsignables = 0;
        int visiblesSeleccionados = 0;
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            AsignacionTableRow row = filaTabla(modelRow);
            if (esFilaSeleccionableAsignacion(row)) {
                visiblesAsignables++;
                if (Boolean.TRUE.equals(tableModel.getValueAt(modelRow, COL_SELECCION))) {
                    visiblesSeleccionados++;
                }
            }
        }
        hayVisiblesAsignables = visiblesAsignables > 0;
        todasVisiblesSeleccionadas = visiblesAsignables > 0 && visiblesAsignables == visiblesSeleccionados;
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.repaint();
        }
    }

    private void setTrabajando(boolean trabajando, String mensaje) {
        btnBuscar.setEnabled(!trabajando);
        btnLimpiar.setEnabled(!trabajando);
        btnAsociarRelacionados.setEnabled(!trabajando && puedeAsociarRelacionados());
        boolean habilitadoAsignacion = !trabajando && (contarSeleccionOperativa() > 0 || asignacionFocoConNumero());
        btnAsignarSeleccionado.setEnabled(habilitadoAsignacion);
        btnAsignarSeleccionados.setEnabled(habilitadoAsignacion);
        btnGenerarNumeroExpediente.setEnabled(!trabajando && puedeGenerarNumeroExpediente());
        documentosRelacionadosTable.setEnabled(!trabajando);
        asignacionMultipleTable.setEnabled(!trabajando);
        if (mensaje != null) {
            lblEstado.setText(mensaje);
        }
    }

    private void mostrarInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Asignación", JOptionPane.INFORMATION_MESSAGE);
    }

    private void finalizarEdicionTabla() {
        if (table.isEditing()) {
            TableCellEditor editor = table.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
        }
        finalizarEdicionAsignacionMultiple();
    }

    private void finalizarEdicionAsignacionMultiple() {
        if (asignacionMultipleTable.isEditing()) {
            TableCellEditor editor = asignacionMultipleTable.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
        }
    }

    private boolean puedeAsociarRelacionados() {
        int marcados = contarSeleccionados();
        if (marcados >= 2) {
            return true;
        }
        AsignacionTableRow fila = filaTabla(obtenerModelRowSeleccionada());
        if (fila != null && fila.esAsociada()) {
            return false;
        }
        AsignacionExpedienteDTO expediente = obtenerExpedienteParaRelacionados();
        if (expediente == null || expediente.getPosiblesRelacionados() <= 0) {
            return false;
        }
        return contarDocumentosRelacionadosMarcados() > 0;
    }

    private boolean puedeGenerarNumeroExpediente() {
        AsignacionExpedienteDTO item = obtenerExpedienteFoco();
        return item != null && item.requiereDecisionNumeroAsignacion();
    }

    private boolean asignacionFocoConNumero() {
        AsignacionExpedienteDTO item = obtenerExpedienteFoco();
        if (item == null || !item.tieneNumeroExpediente()) {
            return false;
        }
        return item.isAsignable() || (modoReasignacion && item.isAsignacionActiva());
    }

    private void actualizarAccionRelacionadosParaPrincipal(AsignacionExpedienteDTO item) {
        if (item == null || item.getPosiblesRelacionados() <= 0) {
            actualizarAccionRelacionadosSinSeleccion();
            return;
        }
        btnAsociarRelacionados.setText("Asociar al principal (" + item.getPosiblesRelacionados() + ")");
        btnAsociarRelacionados.setEnabled(true);
        btnAsociarRelacionados.setToolTipText(
                "Asocia los expedientes detectados con el expediente principal actualmente seleccionado.");
    }

    private void actualizarAccionRelacionadosParaAsociado(AsignacionTableRow filaPanel) {
        String numeroPrincipal = filaPanel != null ? filaPanel.numeroExpedientePrincipal() : null;
        btnAsociarRelacionados.setText("Relación confirmada");
        btnAsociarRelacionados.setEnabled(false);
        btnAsociarRelacionados.setToolTipText(
                numeroPrincipal == null || numeroPrincipal.trim().isEmpty()
                        ? "Este expediente ya está asociado a un expediente principal. La asociación se gestiona desde el principal."
                        : "Este expediente ya está asociado al expediente principal " + numeroPrincipal + ". La asociación se gestiona desde el principal.");
    }

    private void actualizarAccionRelacionadosParaSeleccionMultiple() {
        btnAsociarRelacionados.setText("Asociar selección al principal");
        btnAsociarRelacionados.setEnabled(true);
        btnAsociarRelacionados.setToolTipText(
                "Asocia la selección marcada al expediente principal elegido automáticamente.");
    }

    private void actualizarAccionRelacionadosAmbigua() {
        btnAsociarRelacionados.setText("Selección ambigua");
        btnAsociarRelacionados.setEnabled(false);
        btnAsociarRelacionados.setToolTipText(
                "Hay más de un expediente con número entre los marcados. Solo puede haber un expediente principal: "
                        + "desmarque los que no correspondan y deje marcado únicamente el principal junto con los "
                        + "potenciales duplicados sin número.");
    }

    private void actualizarAccionRelacionadosSinSeleccion() {
        btnAsociarRelacionados.setText("Sin relacionados pendientes");
        btnAsociarRelacionados.setEnabled(false);
        btnAsociarRelacionados.setToolTipText("Seleccione un expediente principal para asociar los relacionados detectados.");
    }

    private void actualizarExpedientePrincipalAsociacion(AsignacionExpedienteDTO item) {
        if (item == null) {
            mostrarExpedientePrincipalAsociacion(null, null);
            lblExpedientePrincipalAsociacion.setToolTipText("Seleccione un expediente principal para ver el destino de la asociación.");
            return;
        }
        mostrarExpedientePrincipalAsociacion(item.getNumeroExpediente(), item.getNumeroExpedienteSgd());
        lblExpedientePrincipalAsociacion.setToolTipText(tieneNumeroExpediente(item)
                ? "Expediente principal destino de la asociación."
                : "Este expediente todavía no tiene número. Si al marcar una solicitud asociada esta sí tiene número, "
                        + "se detectará automáticamente como el expediente principal real.");
    }

    private void actualizarExpedientePrincipalAsociacionResuelto(String numero, String sgd) {
        mostrarExpedientePrincipalAsociacion(numero, sgd);
        lblExpedientePrincipalAsociacion.setToolTipText("Expediente principal detectado automáticamente entre las coincidencias marcadas.");
    }

    private void mostrarExpedientePrincipalAsociacion(String numero, String sgd) {
        boolean tieneNumero = numero != null && !numero.trim().isEmpty();
        StringBuilder texto = new StringBuilder("<html>");
        texto.append(tieneNumero ? "<b>" + numero.trim() + "</b>" : "-");
        if (sgd != null && !sgd.trim().isEmpty()) {
            texto.append(" / SGD ").append(sgd.trim());
        }
        texto.append("</html>");
        lblExpedientePrincipalAsociacion.setText(texto.toString());
    }

    private boolean puedeEliminarDocumentoRelacionado(int modelRow) {
        DocumentoRelacionadoFila fila = documentoRelacionadoFila(modelRow);
        return fila != null && fila.esAsociado();
    }

    private boolean puedeAsociarDocumentoRelacionado(int modelRow) {
        DocumentoRelacionadoFila fila = documentoRelacionadoFila(modelRow);
        return fila != null && !fila.esAsociado();
    }

    private int contarDocumentosRelacionadosMarcados() {
        int total = 0;
        for (int i = 0; i < documentosRelacionadosModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(documentosRelacionadosModel.getValueAt(i, 0))) {
                total++;
            }
        }
        return total;
    }

    private List<Long> obtenerIdsDocumentosRelacionadosMarcados() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < documentosRelacionadosModel.getRowCount() && i < documentosRelacionadosPanel.size(); i++) {
            if (!Boolean.TRUE.equals(documentosRelacionadosModel.getValueAt(i, 0))) {
                continue;
            }
            DocumentoRelacionadoFila fila = documentosRelacionadosPanel.get(i);
            if (fila != null && !fila.esAsociado() && fila.getExpediente() != null && fila.getExpediente().getIdExpediente() != null) {
                ids.add(fila.getExpediente().getIdExpediente());
            }
        }
        return ids;
    }

    private void actualizarBotonAsociarPorSeleccionTabla() {
        if (contarSeleccionados() >= 2) {
            return;
        }
        AsignacionTableRow fila = filaTabla(obtenerModelRowSeleccionada());
        if (fila == null || !fila.esPrincipal()) {
            return;
        }
        int marcados = contarDocumentosRelacionadosMarcados();
        if (marcados <= 0) {
            actualizarExpedientePrincipalAsociacion(fila.principal);
            btnAsociarRelacionados.setText("Sin relacionados marcados");
            btnAsociarRelacionados.setEnabled(false);
            btnAsociarRelacionados.setToolTipText(
                    "Marque al menos una solicitud en \"Solicitudes asociadas\" para habilitar la asociación.");
            return;
        }
        ResolucionAsociacionFoco resolucion = resolverPrincipalParaFoco(fila.principal, obtenerIdsDocumentosRelacionadosMarcados());
        if (resolucion.ambiguo) {
            lblExpedientePrincipalAsociacion.setText("Ambiguo");
            lblExpedientePrincipalAsociacion.setToolTipText(
                    "Hay más de un expediente con número entre las solicitudes marcadas. Marque solo los que no tienen número.");
            actualizarAccionRelacionadosAmbigua();
            return;
        }
        actualizarExpedientePrincipalAsociacionResuelto(resolucion.numeroPrincipal, resolucion.sgdPrincipal);
        btnAsociarRelacionados.setText("Asociar al principal (" + marcados + ")");
        btnAsociarRelacionados.setEnabled(true);
        btnAsociarRelacionados.setToolTipText(resolucion.idPrincipal.equals(fila.principal.getIdExpediente())
                ? "Asocia las solicitudes marcadas en \"Solicitudes asociadas\" con el expediente principal actualmente seleccionado."
                : "El expediente seleccionado no tiene número: se asociará junto con lo marcado al expediente principal "
                        + resolucion.numeroPrincipal + ", detectado automáticamente entre las coincidencias.");
    }

    private void ajustarTamanoDocumentosRelacionados() {
        if (documentosRelacionadosScroll == null || documentosRelacionadosTable == null) {
            return;
        }
        int ancho = 0;
        for (int i = 0; i < documentosRelacionadosTable.getColumnCount(); i++) {
            ancho += documentosRelacionadosTable.getColumnModel().getColumn(i).getPreferredWidth();
        }
        ancho += documentosRelacionadosTable.getIntercellSpacing().width;
        int altoEncabezado = documentosRelacionadosTable.getTableHeader() != null
                ? documentosRelacionadosTable.getTableHeader().getPreferredSize().height
                : 28;
        int altoFilas = documentosRelacionadosTable.getRowCount() * documentosRelacionadosTable.getRowHeight();
        int altoHorizontalBarra = documentosRelacionadosScroll.getHorizontalScrollBar() != null
                ? documentosRelacionadosScroll.getHorizontalScrollBar().getPreferredSize().height
                : 16;
        int alto = Math.max(altoEncabezado + altoFilas + altoHorizontalBarra + 8,
                altoEncabezado + documentosRelacionadosTable.getRowHeight() + altoHorizontalBarra + 8);
        Dimension size = new Dimension(Math.max(ancho, 240), Math.max(alto, 64));
        documentosRelacionadosTable.setPreferredScrollableViewportSize(size);
        documentosRelacionadosScroll.setPreferredSize(size);
        documentosRelacionadosScroll.setMinimumSize(size);
        if (documentosRelacionadosWrapper != null) {
            documentosRelacionadosWrapper.revalidate();
            documentosRelacionadosWrapper.repaint();
        }
    }

    private DocumentoRelacionadoFila documentoRelacionadoFila(int modelRow) {
        if (modelRow < 0 || modelRow >= documentosRelacionadosPanel.size()) {
            return null;
        }
        return documentosRelacionadosPanel.get(modelRow);
    }

    private static boolean hasTextUi(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void mostrarError(String context, Exception ex) {
        String message = ex.getMessage();
        if (message == null && ex.getCause() != null) {
            message = ex.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(
                this,
                context + (message == null ? "" : "\n" + message),
                "Error de asignación",
                JOptionPane.ERROR_MESSAGE);
        lblEstado.setText(context);
    }

    private class AsignacionTableModel extends DefaultTableModel {

        private AsignacionTableModel() {
            super(new Object[]{
                "",
                "",
                "Días",
                "Nro. Expediente",
                "N° expediente SGD",
                "Fecha Solicitud",
                "Fecha Vencimiento",
                "Proc. Registral",
                "Tipo Acta",
                "Nro. Acta",
                "Titular",
                "Abogado actual",
                "Estado",
                "Alertas",
                "_ID"
            }, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == COL_SELECCION
                    && row >= 0
                    && row < filasTabla.size()
                    && esFilaSeleccionableAsignacion(filasTabla.get(row));
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == COL_SELECCION ? Boolean.class : Object.class;
        }
    }

    private class CartaRespuestaPendienteRenderer extends DefaultTableCellRenderer {

        private CartaRespuestaPendienteRenderer() {
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
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setText(value == null || value.toString().trim().isEmpty() ? "-" : value.toString());
            setToolTipText(getText());
            setHorizontalAlignment(column == 5 || column == 6 ? SwingConstants.CENTER : SwingConstants.LEFT);
            setBackground(isSelected
                    ? TABLE_SELECTION_BACKGROUND
                    : (row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT));
            setForeground(AppV2Theme.TEXT_PRIMARY);
            return this;
        }
    }

    private class CargaLaboralRenderer extends DefaultTableCellRenderer {

        private CargaLaboralRenderer() {
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
            setText(value == null || value.toString().trim().isEmpty() ? "-" : value.toString());
            setHorizontalAlignment(column >= 3 ? SwingConstants.CENTER : SwingConstants.LEFT);
            setBackground(isSelected ? TABLE_SELECTION_BACKGROUND : (row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT));
            if (!isSelected && column == 5 && value instanceof Number && ((Number) value).intValue() > 0) {
                setForeground(AppV2Theme.ERROR);
                setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
            } else {
                setForeground(AppV2Theme.TEXT_PRIMARY);
                setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            }
            return component;
        }
    }

    private class FechaCartaCellEditor extends AbstractCellEditor implements TableCellEditor {

        private final PremiumDateFieldV2 field = new PremiumDateFieldV2();

        private FechaCartaCellEditor() {
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
            LocalDate date = parseFechaUi(stringValue(value));
            field.setDate(date == null ? null : Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            return field;
        }
    }

    private class AsignacionMultipleRenderer extends DefaultTableCellRenderer {

        private AsignacionMultipleRenderer() {
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
            setText(value == null || value.toString().trim().isEmpty() ? "-" : value.toString());
            setToolTipText(getText());
            setBackground(isSelected
                    ? TABLE_SELECTION_BACKGROUND
                    : (row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT));
            setForeground(column == 2 ? AppV2Theme.TEXT_PRIMARY : AppV2Theme.TEXT_SECONDARY);
            if (column == 2) {
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AppV2Theme.BORDER),
                        BorderFactory.createEmptyBorder(0, 8, 0, 8)));
            } else {
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            }
            return component;
        }
    }

    private class SelectAllHeaderRenderer extends JLabel implements TableCellRenderer {

        private SelectAllHeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setOpaque(true);
            setBackground(AppV2Theme.SURFACE_ALT);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, AppV2Theme.BORDER));
            setToolTipText("Seleccionar o desmarcar todos los expedientes filtrados.");
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            setEnabled(hayVisiblesAsignables);
            setText("");
            setIcon(new HeaderCheckBoxIcon(
                    todasVisiblesSeleccionadas,
                    hayVisiblesAsignables));
            setToolTipText(hayVisiblesAsignables
                    ? (todasVisiblesSeleccionadas
                            ? "Desmarcar todos los expedientes filtrados."
                            : "Seleccionar todos los expedientes filtrados.")
                    : "No hay expedientes asignables en el filtro actual.");
            return this;
        }
    }

    private static class HeaderCheckBoxIcon implements javax.swing.Icon {

        private final boolean selected;
        private final boolean enabled;

        private HeaderCheckBoxIcon(boolean selected, boolean enabled) {
            this.selected = selected;
            this.enabled = enabled;
        }

        @Override
        public int getIconWidth() {
            return 18;
        }

        @Override
        public int getIconHeight() {
            return 18;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color border = enabled ? AppV2Theme.PRIMARY : AppV2Theme.BORDER_STRONG;
                Color fill = selected && enabled ? AppV2Theme.PRIMARY : AppV2Theme.SURFACE;
                g2.setColor(fill);
                g2.fillRoundRect(x + 1, y + 1, 15, 15, 4, 4);
                g2.setColor(border);
                g2.drawRoundRect(x + 1, y + 1, 15, 15, 4, 4);
                if (selected && enabled) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 4, y + 9, x + 7, y + 12);
                    g2.drawLine(x + 7, y + 12, x + 13, y + 5);
                }
            } finally {
                g2.dispose();
            }
        }
    }

    private class ExpandirRenderer extends JPanel implements TableCellRenderer {

        private final ExpandGlyph glyph = new ExpandGlyph();

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
            AsignacionTableRow fila = filaTabla(modelRow);
            Color background = colorFondoFila(row, fila, isSelected);
            setBorder(BorderFactory.createCompoundBorder(
                    debeMostrarBarraGrupo(fila)
                            ? BorderFactory.createMatteBorder(0, GROUP_STRIPE_WIDTH, 0, 0, acentoGrupo(fila.getIdPrincipal()))
                            : BorderFactory.createEmptyBorder(0, GROUP_STRIPE_WIDTH, 0, 0),
                    BorderFactory.createEmptyBorder(0, 4, 0, 4)));
            if (fila != null && fila.esAsociada()) {
                setBackground(background);
                glyph.configure(ExpandGlyph.NONE, GRID_ACTION_ICON_BLUE, background);
                setToolTipText("Documento asociado al expediente principal.");
                return this;
            }
            setBackground(background);
            if (fila != null
                    && fila.esPrincipal()
                    && fila.principal.getIdExpediente() != null
                    && fila.principal.getAsociadosConfirmados() > 0) {
                Long idPrincipal = fila.principal.getIdExpediente();
                int state = principalesCargando.contains(idPrincipal)
                        ? ExpandGlyph.LOADING
                        : (principalesExpandidos.contains(idPrincipal) ? ExpandGlyph.COLLAPSE : ExpandGlyph.EXPAND);
                glyph.configure(state, GRID_ACTION_ICON_BLUE, background);
                setToolTipText(state == ExpandGlyph.COLLAPSE
                        ? "Ocultar documentos asociados"
                        : "Ver documentos asociados");
            } else {
                glyph.configure(ExpandGlyph.NONE, AppV2Theme.TEXT_SECONDARY, background);
                setToolTipText(null);
            }
            return this;
        }
    }

    private static final class ExpandGlyph extends JPanel {

        private static final int NONE = 0;
        private static final int EXPAND = 1;
        private static final int COLLAPSE = 2;
        private static final int LOADING = 3;
        private int state = NONE;
        private Color accent = AppV2Theme.TEAL;

        private ExpandGlyph() {
            setOpaque(false);
            setPreferredSize(new Dimension(30, 28));
        }

        private void configure(int state, Color accent, Color background) {
            this.state = state;
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
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                if (state == LOADING) {
                    pintarLoading(g2, cx, cy);
                    return;
                }
                int size = 18;
                int x = cx - size / 2;
                int y = cy - size / 2;
                Color fill = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 28);
                g2.setColor(fill);
                g2.fillOval(x, y, size, size);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, size, size);
                int pad = 5;
                g2.drawLine(x + pad, cy, x + size - pad, cy);
                if (state == EXPAND) {
                    g2.drawLine(cx, y + pad, cx, y + size - pad);
                }
            } finally {
                g2.dispose();
            }
        }

        private void pintarLoading(Graphics2D g2, int cx, int cy) {
            g2.setColor(accent);
            int start = cx - 8;
            for (int i = 0; i < 3; i++) {
                g2.fillOval(start + i * 7, cy - 2, 4, 4);
            }
        }
    }

    private class SeleccionRenderer extends JCheckBox implements TableCellRenderer {

        private final DocumentoAsociadoCell documentoAsociadoCell = new DocumentoAsociadoCell();

        private SeleccionRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder());
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
            AsignacionTableRow fila = filaTabla(modelRow);
            if (fila != null && fila.esAsociada()) {
                documentoAsociadoCell.configure(
                        GRID_ACTION_ICON_BLUE,
                        colorFondoFila(row, fila, isSelected),
                        bordeContenidoAsociado(modelRow, 8, 8));
                documentoAsociadoCell.setToolTipText("Documento asociado al expediente principal; no requiere asignación independiente.");
                return documentoAsociadoCell;
            }
            setSelected(Boolean.TRUE.equals(value));
            setEnabled(esFilaSeleccionableAsignacion(fila));
            setBackground(colorFondoFila(row, fila, isSelected));
            setToolTipText(fila != null && fila.esAsignable()
                    ? "Seleccionar expediente principal."
                    : (modoReasignacion
                            ? "Seleccionar expediente principal para reasignarlo."
                            : "Este expediente ya está asignado. Active \"Habilitar reasignación\" para poder marcarlo."));
            return this;
        }
    }

    private static final class DocumentoAsociadoCell extends JPanel {

        private Color accent = AppV2Theme.TEAL;

        private DocumentoAsociadoCell() {
            setOpaque(true);
            setPreferredSize(new Dimension(30, 28));
        }

        private void configure(Color accent, Color background, javax.swing.border.Border border) {
            this.accent = accent == null ? AppV2Theme.TEAL : accent;
            setBackground(background == null ? ASSOCIATED_ROW_BACKGROUND : background);
            setBorder(border);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = 15;
                int h = 18;
                int x = (getWidth() - w) / 2;
                int y = (getHeight() - h) / 2;
                Color fill = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 22);
                Color line = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 150);
                g2.setColor(fill);
                g2.fillRoundRect(x, y, w, h, 4, 4);
                g2.setColor(line);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(x, y, w, h, 4, 4);
                int fold = 5;
                g2.drawLine(x + w - fold, y, x + w - 1, y + fold);
                g2.drawLine(x + w - fold, y, x + w - fold, y + fold);
                g2.drawLine(x + 4, y + 8, x + w - 4, y + 8);
                g2.drawLine(x + 4, y + 12, x + w - 5, y + 12);
            } finally {
                g2.dispose();
            }
        }
    }

    private class AsignacionRenderer extends DefaultTableCellRenderer {
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
            AsignacionTableRow fila = filaTabla(modelRow);
            boolean filaAsociada = fila != null && fila.esAsociada();
            Color cellBackground = colorFondoFila(row, fila, isSelected);
            if (modelColumn == COL_DIAS) {
                if (filaAsociada) {
                    Component c = super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
                    c.setBackground(cellBackground);
                    c.setForeground(AppV2Theme.TEXT_SECONDARY);
                    setBorder(bordeContenidoAsociado(modelRow, 8, 8));
                    setToolTipText("La alerta de días aplica al expediente principal.");
                    return c;
                }
                return StatusBadgeV2.forDias(value, cellBackground);
            }
            if (!isSelected && modelColumn == COL_ESTADO) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
            }
            if (!isSelected && modelColumn == COL_RELACIONADOS) {
                String text = value == null ? "" : value.toString();
                if (!text.startsWith("Sin")) {
                    Color bg = filaAsociada ? ASSOCIATED_ROW_BACKGROUND : AppV2Theme.SOFT_ORANGE;
                    Color fg = filaAsociada ? AppV2Theme.TEXT_SECONDARY : AppV2Theme.WARNING;
                    return new BadgeV2(text, bg, fg);
                }
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
                        ? bordeContenidoAsociado(modelRow, modelColumn == COL_EXPEDIENTE ? ASSOCIATED_EXPEDIENTE_INDENT : 8, 8)
                        : BorderFactory.createEmptyBorder(0, 8, 0, 8));
            } else if (filaAsociada) {
                setBorder(bordeContenidoAsociado(modelRow, modelColumn == COL_EXPEDIENTE ? ASSOCIATED_EXPEDIENTE_INDENT : 8, 8));
                c.setBackground(ASSOCIATED_ROW_BACKGROUND);
                c.setForeground(modelColumn == COL_EXPEDIENTE ? AppV2Theme.TEXT_PRIMARY : AppV2Theme.TEXT_SECONDARY);
            } else {
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                c.setBackground(cellBackground);
                c.setForeground(AppV2Theme.TEXT_PRIMARY);
            }
            return c;
        }
    }

    private class DocumentoRelacionadoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            c.setBackground(isSelected ? TABLE_SELECTION_BACKGROUND : AppV2Theme.SURFACE);
            c.setForeground(AppV2Theme.TEXT_PRIMARY);
            String text = value == null ? "" : value.toString();
            setToolTipText(text);
            return c;
        }
    }

    private class SeleccionDocumentoRelacionadoRenderer extends JCheckBox implements TableCellRenderer {

        private SeleccionDocumentoRelacionadoRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
            setBorderPainted(false);
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
            Color background = isSelected ? TABLE_SELECTION_BACKGROUND : AppV2Theme.SURFACE;
            if (!puedeAsociarDocumentoRelacionado(modelRow)) {
                JLabel vacio = new JLabel("");
                vacio.setOpaque(true);
                vacio.setBackground(background);
                return vacio;
            }
            setBackground(background);
            setSelected(Boolean.TRUE.equals(value));
            return this;
        }
    }

    private class EliminarDocumentoRenderer extends AppV2RemoveActionButton implements TableCellRenderer {

        private EliminarDocumentoRenderer() {
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
            boolean permitido = puedeEliminarDocumentoRelacionado(modelRow);
            if (!permitido) {
                JLabel vacio = new JLabel("");
                vacio.setOpaque(true);
                vacio.setHorizontalAlignment(SwingConstants.CENTER);
                vacio.setBackground(isSelected ? TABLE_SELECTION_BACKGROUND : AppV2Theme.SURFACE);
                vacio.setForeground(AppV2Theme.TEXT_PRIMARY);
                vacio.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                vacio.setToolTipText(null);
                return vacio;
            }
            configure(false, true);
            setToolTipText("Eliminar la asociación del expediente asociado.");
            return this;
        }
    }

    private class EliminarDocumentoEditor extends AbstractCellEditor implements TableCellEditor {

        private final AppV2RemoveActionButton button = new AppV2RemoveActionButton();
        private int modelRow = -1;

        private EliminarDocumentoEditor() {
            button.addActionListener(e -> {
                int row = modelRow;
                fireEditingStopped();
                eliminarDocumentoRelacionado(row);
            });
        }

        @Override
        public Object getCellEditorValue() {
            return "Eliminar";
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
            modelRow = table.convertRowIndexToModel(row);
            boolean permitido = puedeEliminarDocumentoRelacionado(modelRow);
            if (!permitido) {
                JLabel vacio = new JLabel("");
                vacio.setOpaque(true);
                vacio.setHorizontalAlignment(SwingConstants.CENTER);
                vacio.setBackground(isSelected ? TABLE_SELECTION_BACKGROUND : AppV2Theme.SURFACE);
                vacio.setForeground(AppV2Theme.TEXT_PRIMARY);
                vacio.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return vacio;
            }
            button.configure(false, permitido);
            button.setToolTipText("Eliminar la asociación del expediente asociado.");
            return button;
        }
    }

    private static final class AsignacionTableRow {

        private final AsignacionExpedienteDTO principal;
        private final ExpedienteRelacionadoDTO asociado;
        private final Long idExpedientePrincipal;

        private AsignacionTableRow(AsignacionExpedienteDTO principal, ExpedienteRelacionadoDTO asociado) {
            this.principal = principal;
            this.asociado = asociado;
            this.idExpedientePrincipal = principal == null ? null : principal.getIdExpediente();
        }

        private static AsignacionTableRow principal(AsignacionExpedienteDTO principal) {
            return new AsignacionTableRow(principal, null);
        }

        private static AsignacionTableRow asociada(AsignacionExpedienteDTO principal, ExpedienteRelacionadoDTO asociado) {
            return new AsignacionTableRow(principal, asociado);
        }

        private boolean esPrincipal() {
            return asociado == null && principal != null;
        }

        private boolean esAsociada() {
            return asociado != null;
        }

        private boolean esAsignable() {
            return esPrincipal() && principal.isAsignable();
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

    private static final class DocumentoRelacionadoFila {

        private final ExpedienteRelacionadoDTO expediente;
        private final boolean asociado;

        private DocumentoRelacionadoFila(ExpedienteRelacionadoDTO expediente, boolean asociado) {
            this.expediente = expediente;
            this.asociado = asociado;
        }

        private ExpedienteRelacionadoDTO getExpediente() {
            return expediente;
        }

        private boolean esAsociado() {
            return asociado;
        }
    }

    private static class EquipoItem {

        private final EquipoAsignacionDTO equipo;
        private final String label;

        private EquipoItem(EquipoAsignacionDTO equipo) {
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

        private final UsuarioAsignableDTO usuario;
        private final String label;

        private UsuarioItem(UsuarioAsignableDTO usuario) {
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

    private static final class AsociacionRapidaSeleccion {
        private final AsignacionExpedienteDTO expedienteFoco;
        private final Long idPrincipal;
        private final List<Long> idsRelacionados;
        private final int totalSeleccionados;
        private final boolean ambiguo;

        private AsociacionRapidaSeleccion(
                AsignacionExpedienteDTO expedienteFoco,
                Long idPrincipal,
                List<Long> idsRelacionados,
                int totalSeleccionados) {
            this(expedienteFoco, idPrincipal, idsRelacionados, totalSeleccionados, false);
        }

        private AsociacionRapidaSeleccion(
                AsignacionExpedienteDTO expedienteFoco,
                Long idPrincipal,
                List<Long> idsRelacionados,
                int totalSeleccionados,
                boolean ambiguo) {
            this.expedienteFoco = expedienteFoco;
            this.idPrincipal = idPrincipal;
            this.idsRelacionados = idsRelacionados;
            this.totalSeleccionados = totalSeleccionados;
            this.ambiguo = ambiguo;
        }

        private boolean esSeleccionMultiple() {
            return totalSeleccionados >= 2;
        }
    }

    private static final class ResolucionAsociacionFoco {
        private final Long idPrincipal;
        private final String numeroPrincipal;
        private final String sgdPrincipal;
        private final List<Long> idsRelacionados;
        private final boolean ambiguo;

        private ResolucionAsociacionFoco(
                Long idPrincipal,
                String numeroPrincipal,
                String sgdPrincipal,
                List<Long> idsRelacionados,
                boolean ambiguo) {
            this.idPrincipal = idPrincipal;
            this.numeroPrincipal = numeroPrincipal;
            this.sgdPrincipal = sgdPrincipal;
            this.idsRelacionados = idsRelacionados;
            this.ambiguo = ambiguo;
        }
    }

}
