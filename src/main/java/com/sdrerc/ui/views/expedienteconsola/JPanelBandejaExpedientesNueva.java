package com.sdrerc.ui.views.expedienteconsola;

import com.sdrerc.application.sdrercapp.ExpedienteConsultaService;
import com.sdrerc.application.sdrercapp.ExpedienteDetalleService;
import com.sdrerc.application.sdrercapp.ExpedienteEdicionManualService;
import com.sdrerc.application.sdrercapp.ExpedienteRelacionadoDeteccionService;
import com.sdrerc.application.sdrercapp.ExpedienteRelacionadoService;
import com.sdrerc.application.sdrercapp.GrupoFamiliarRegistroService;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteBandejaDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteConsolaDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO;
import com.sdrerc.domain.rules.ProcedimientoRegistralRules;
import com.sdrerc.ui.appv2.components.AppV2ActionPanel;
import com.sdrerc.ui.appv2.components.AppV2ColumnFilterSupport;
import com.sdrerc.ui.appv2.components.AppV2FilterPanel;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.components.AppV2ResponsiveGridPanel;
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2SideActionPanel;
import com.sdrerc.ui.appv2.components.AppV2SideSectionPanel;
import com.sdrerc.ui.appv2.components.AppV2StackedSideTab;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.components.AppV2TablePanel;
import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.PremiumDateFieldV2;
import com.sdrerc.ui.appv2.components.PlazoVisualSupportV2;
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.helpers.FiltroCatalogoItemV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import com.sdrerc.util.DateRangePickerSupport;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;
import javax.swing.table.JTableHeader;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.util.Set;

public class JPanelBandejaExpedientesNueva extends JPanel {

    private static final int PANEL_RECEPCION_ANCHO_MINIMO = 380;
    private static final int PANEL_RECEPCION_ANCHO_NORMAL = 430;
    private static final int PANEL_RECEPCION_TAB_OVERHANG = 46;
    private static final int PANEL_RECEPCION_TAB_TOP = 18;
    private static final int PANEL_RECEPCION_TAB_HEIGHT = 94;
    private static final String PANEL_RECEPCION_CARD_DATOS = "DATOS";
    private static final String PANEL_RECEPCION_CARD_GF = "REGISTRAR_GF";
    private static final String PANEL_RECEPCION_CARD_ASOCIAR = "ASOCIAR_DUPLICADOS";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color GRID_ACTION_ICON_BLUE = AppV2Theme.PRIMARY;
    private static final Color TABLE_SELECTION_BACKGROUND = new Color(219, 244, 249);
    private static final Color EXPANDED_PARENT_BACKGROUND = new Color(205, 236, 244);
    private static final Color EXPANDED_ASSOCIATED_BACKGROUND = new Color(238, 250, 252);
    private static final Color ASSOCIATED_ROW_BACKGROUND = EXPANDED_ASSOCIATED_BACKGROUND;
    private static final Color ASSOCIATED_BLOCK_BORDER = new Color(224, 233, 240);
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
        new Color(84, 110, 154)
    };
    private static final int ASSOCIATED_EXPEDIENTE_INDENT = 8;
    private static final int COL_EXPANDIR_REGISTRO = 0;
    private static final int COL_SELECCION_REGISTRO = 1;
    private static final int COL_DIAS_REGISTRO = 2;
    private static final int COL_NUMERO_EXPEDIENTE_REGISTRO = 3;
    private static final int COL_NUMERO_EXPEDIENTE_SGD_REGISTRO = 4;
    private static final int COL_CANAL_REGISTRO = 5;
    private static final int COL_FECHA_SOLICITUD_REGISTRO = 6;
    private static final int COL_FECHA_VENCIMIENTO_REGISTRO = 7;
    private static final int COL_PROC_REGISTRAL_REGISTRO = 8;
    private static final int COL_TIPO_ACTA_REGISTRO = 9;
    private static final int COL_NUMERO_ACTA_REGISTRO = 10;
    private static final int COL_TITULAR_REGISTRO = 11;
    private static final int COL_ESTADO_REGISTRO = 12;
    private static final int COL_ALERTAS_REGISTRO = 13;
    private static final int COL_ID_REGISTRO = 14;
    private static final int COL_EXPANDIR_BANDEJA = 0;
    private static final int COL_DIAS_BANDEJA = 1;
    private static final int COL_EXPEDIENTE_BANDEJA = 2;
    private static final int COL_TRAMITE_BANDEJA = 3;
    private static final int COL_ETAPA_BANDEJA = 4;
    private static final int COL_ESTADO_BANDEJA = 5;
    private static final int COL_FECHA_SOLICITUD_BANDEJA = 6;
    private static final int COL_VENCIMIENTO_BANDEJA = 7;
    private static final int COL_PUBLICACION_BANDEJA = 8;
    private static final int COL_DIGITAL_BANDEJA = 9;
    private static final int COL_ID_BANDEJA = 10;

    private final ExpedienteConsultaService consultaService;
    private final ExpedienteDetalleService detalleService = new ExpedienteDetalleService();
    private final com.sdrerc.application.sdrercapp.AsignacionExpedienteService asignacionExpedienteServiceRecepcion =
            new com.sdrerc.application.sdrercapp.AsignacionExpedienteService();
    private final ExpedienteRelacionadoService relacionadoService = new ExpedienteRelacionadoService();
    private final ExpedienteRelacionadoDeteccionService relacionadoDeteccionService = new ExpedienteRelacionadoDeteccionService();
    private final String etapaInicial;
    private final String tituloBandeja;
    private final String subtituloBandeja;
    private final boolean etapaBloqueada;
    private final boolean mostrarEncabezado;
    private final Component encabezadoOperativo;
    private final Consumer<Long> editarExpedienteHandler;
    private final boolean usarSplitExterno;
    private final boolean perfilRegistroRecepcion;
    private volatile List<ExpedienteBandejaDTO> ultimoResultadoBuscado = Collections.emptyList();
    private final AtomicLong secuenciaBusqueda = new AtomicLong(0L);
    private volatile SwingWorker<List<ExpedienteBandejaDTO>, Void> busquedaActiva;
    private final Set<Long> grupoFamiliarSeleccionados = new LinkedHashSet<Long>();
    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite/SGD, acta, titular o documento", 28);
    private final JComboBox<FiltroCatalogoItemV2> cmbEtapa = new JComboBox<FiltroCatalogoItemV2>(crearItemsEtapa());
    private final JComboBox<FiltroCatalogoItemV2> cmbEstado = new JComboBox<FiltroCatalogoItemV2>(crearItemsEstado());
    private final PremiumDateFieldV2 fechaSolicitudDesde = new PremiumDateFieldV2();
    private final PremiumDateFieldV2 fechaSolicitudHasta = new PremiumDateFieldV2();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnVerDetalle = new JButton("Ver detalle");
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final ExpedienteEdicionManualService edicionManualService = new ExpedienteEdicionManualService();
    private final JLabel lblSeleccionados = new JLabel("0 expediente(s) seleccionado(s)");
    private final JLabel lblResultado = new JLabel("Seleccione un expediente y presione Ver detalle para abrir la consola.");
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final AppV2TablePanel tablePanel;
    private AppV2ColumnFilterSupport.Controller columnFilterSupport;
    private final List<RegistroTableRow> filasTabla = new ArrayList<>();
    private final Set<Long> principalesExpandidos = new LinkedHashSet<Long>();
    private final Set<Long> principalesCargando = new LinkedHashSet<Long>();
    private final Map<Long, List<ExpedienteRelacionadoDTO>> asociadosCacheRegistro = new HashMap<Long, List<ExpedienteRelacionadoDTO>>();
    private Long idExpedienteExpansionActiva;
    private final AppV2StackedSideTab tabPanelRecepcion = crearTabRecepcion();
    private final AppV2StackedSideTab tabPanelRegistrarGF = crearTabRegistrarGF();
    private final AppV2StackedSideTab tabPanelAsociarDuplicados = crearTabAsociarDuplicados();
    private AppV2OperationalSplitPanel splitBandeja;
    private AppV2SideActionPanel panelRecepcion;
    private JPanelRegistrarGrupoFamiliarV2 panelRegistrarGrupoFamiliar;
    private JPanelAsociarDuplicadosRecepcionV2 panelAsociarDuplicados;
    private JPanel panelRecepcionWrapper;
    private JPanel panelRecepcionCards;
    private CardLayout panelRecepcionCardsLayout;
    private String panelRecepcionActivo = PANEL_RECEPCION_CARD_DATOS;
    private final JLabel lblRecepcionExpediente = valueLabel();
    private final JLabel lblRecepcionFecha = valueLabel();
    private final JLabel lblRecepcionCanal = valueLabel();
    private final JLabel lblRecepcionTipoSolicitud = valueLabel();
    private final JLabel lblRecepcionTramite = valueLabel();
    private final JLabel lblRecepcionNumeroExpedienteSgd = valueLabel();
    private final JLabel lblRecepcionTipoDocumento = valueLabel();
    private final JLabel lblRecepcionNumeroDocumento = valueLabel();
    private final JLabel lblRecepcionGrupoFamiliar = valueLabel();
    private final JLabel lblRecepcionResultadoInicial = valueLabel();
    private final JLabel lblRecepcionHojaEnvio = valueLabel();
    private final JLabel lblRecepcionCorreo = valueLabel();
    private final JLabel lblRecepcionTelefono = valueLabel();
    private final JLabel lblRecepcionDepartamento = valueLabel();
    private final JLabel lblRecepcionProvincia = valueLabel();
    private final JLabel lblRecepcionDistrito = valueLabel();
    private final JLabel lblRecepcionDireccion = valueLabel();
    private final JLabel lblRecepcionProcedimiento = valueLabel();
    private final JLabel lblRecepcionTitular = valueLabel();
    private final JLabel lblRecepcionRemitente = valueLabel();
    private final JLabel lblRecepcionTipoDocumentoTitular = valueLabel();
    private final JLabel lblRecepcionNumeroDocumentoTitular = valueLabel();
    private final JLabel lblRecepcionTipoDocumentoSolicitante = valueLabel();
    private final JLabel lblRecepcionNumeroDocumentoSolicitante = valueLabel();
    private final JLabel lblRecepcionTipoActa = valueLabel();
    private final JLabel lblRecepcionNumeroActa = valueLabel();
    private final JLabel lblRecepcionPrioridad = valueLabel();
    private final JLabel lblRecepcionMarcaOperativa = valueLabel();
    private final JLabel lblRecepcionObservacion = valueLabel();
    private final JLabel lblRecepcionVencimiento = valueLabel();
    private final BadgeV2 lblRecepcionDias = new BadgeV2("-", AppV2Theme.SOFT_GRAY, AppV2Theme.MUTED);
    private final JLabel lblRecepcionResponsable = valueLabel();
    private final JLabel lblRecepcionEquipo = valueLabel();
    private final JLabel lblRecepcionEtapaEstado = valueLabel();
    private Long idPanelRecepcionActual;
    private int panelRecepcionLoadSequence;
    private boolean panelRecepcionCargado;
    private MetricCardV2 cardPotencialDuplicadoRegistro;
    private MetricCardV2 cardPosibleGrupoFamiliarRegistro;
    private MetricCardV2 cardGrupoFamiliarConfirmadoRegistro;
    private String filtroAlertaRegistro;
    private Runnable onGrupoFamiliarSelectionChanged;
    private boolean hayVisiblesGrupoFamiliar;
    private boolean todasVisiblesGrupoFamiliarSeleccionadas;
    private boolean actualizandoGrupoFamiliarMasivo;

    public JPanelBandejaExpedientesNueva() {
        this(new ExpedienteConsultaService());
    }

    public JPanelBandejaExpedientesNueva(boolean mostrarEncabezado) {
        this(new ExpedienteConsultaService(), null, "Bandeja de Expedientes", "Consulta, seguimiento y priorización de expedientes por etapa, estado, responsable y plazos de atención", false, mostrarEncabezado, null, null);
    }

    public JPanelBandejaExpedientesNueva(String etapaInicial, String tituloBandeja, String subtituloBandeja, boolean etapaBloqueada) {
        this(new ExpedienteConsultaService(), etapaInicial, tituloBandeja, subtituloBandeja, etapaBloqueada, true, null, null);
    }

    public JPanelBandejaExpedientesNueva(
            String etapaInicial,
            String tituloBandeja,
            String subtituloBandeja,
            boolean etapaBloqueada,
            boolean mostrarEncabezado) {
        this(new ExpedienteConsultaService(), etapaInicial, tituloBandeja, subtituloBandeja, etapaBloqueada, mostrarEncabezado, null, null);
    }

    public JPanelBandejaExpedientesNueva(
            String etapaInicial,
            String tituloBandeja,
            String subtituloBandeja,
            boolean etapaBloqueada,
            boolean mostrarEncabezado,
            Component encabezadoOperativo) {
        this(new ExpedienteConsultaService(), etapaInicial, tituloBandeja, subtituloBandeja, etapaBloqueada, mostrarEncabezado, encabezadoOperativo, null);
    }

    public JPanelBandejaExpedientesNueva(
            String etapaInicial,
            String tituloBandeja,
            String subtituloBandeja,
            boolean etapaBloqueada,
            boolean mostrarEncabezado,
            Component encabezadoOperativo,
            Consumer<Long> editarExpedienteHandler) {
        this(new ExpedienteConsultaService(), etapaInicial, tituloBandeja, subtituloBandeja, etapaBloqueada, mostrarEncabezado, encabezadoOperativo, editarExpedienteHandler);
    }

    public JPanelBandejaExpedientesNueva(
            String etapaInicial,
            String tituloBandeja,
            String subtituloBandeja,
            boolean etapaBloqueada,
            boolean mostrarEncabezado,
            Component encabezadoOperativo,
            Consumer<Long> editarExpedienteHandler,
            boolean usarSplitExterno) {
        this(new ExpedienteConsultaService(), etapaInicial, tituloBandeja, subtituloBandeja, etapaBloqueada, mostrarEncabezado, encabezadoOperativo, editarExpedienteHandler, usarSplitExterno);
    }

    public JPanelBandejaExpedientesNueva(ExpedienteConsultaService consultaService) {
        this(consultaService, null, "Bandeja de Expedientes", "Consulta, seguimiento y priorización de expedientes por etapa, estado, responsable y plazos de atención", false, true, null, null);
    }

    private JPanelBandejaExpedientesNueva(
            ExpedienteConsultaService consultaService,
            String etapaInicial,
            String tituloBandeja,
            String subtituloBandeja,
            boolean etapaBloqueada,
            boolean mostrarEncabezado,
            Component encabezadoOperativo,
            Consumer<Long> editarExpedienteHandler) {
        this(consultaService, etapaInicial, tituloBandeja, subtituloBandeja, etapaBloqueada, mostrarEncabezado, encabezadoOperativo, editarExpedienteHandler, false);
    }

    private JPanelBandejaExpedientesNueva(
            ExpedienteConsultaService consultaService,
            String etapaInicial,
            String tituloBandeja,
            String subtituloBandeja,
            boolean etapaBloqueada,
            boolean mostrarEncabezado,
            Component encabezadoOperativo,
            Consumer<Long> editarExpedienteHandler,
            boolean usarSplitExterno) {
        this.consultaService = consultaService;
        this.etapaInicial = normalizar(etapaInicial);
        this.tituloBandeja = textoConDefault(tituloBandeja, "Bandeja de Expedientes");
        this.subtituloBandeja = textoConDefault(subtituloBandeja, "Consulta, seguimiento y priorización de expedientes por etapa, estado, responsable y plazos de atención");
        this.etapaBloqueada = etapaBloqueada && this.etapaInicial != null;
        this.mostrarEncabezado = mostrarEncabezado;
        this.encabezadoOperativo = encabezadoOperativo;
        this.editarExpedienteHandler = editarExpedienteHandler;
        this.usarSplitExterno = usarSplitExterno;
        this.perfilRegistroRecepcion = this.etapaBloqueada && "REGISTRO".equals(this.etapaInicial);
        this.tableModel = crearTableModel(this.perfilRegistroRecepcion);
        this.table = new AppV2Table(tableModel);
        this.tablePanel = new AppV2TablePanel(
                table,
                "Sin expedientes para mostrar",
                "Seleccione filtros y presione Buscar.");
        configurarLayout();
        configurarTabla();
        aplicarConfiguracionInicial();
        configurarEventos();
        SwingUtilities.invokeLater(this::buscar);
    }

    private DefaultTableModel crearTableModel(boolean perfilRegistroRecepcion) {
        Object[] columnas = perfilRegistroRecepcion
                ? new Object[]{
                    " ",
                    " ",
                    "Dias",
                    "Nro. Expediente",
                    "N° expediente SGD",
                    "Canal",
                    "Fecha Solicitud",
                    "Fecha Vencimiento",
                    "Proc. Registral",
                    "Tipo Acta",
                    "Nro Acta",
                    "Titular",
                    "Estado",
                    "Alertas",
                    "_ID"
                }
                : new Object[]{
                    " ",
                    "Días",
                    "Expediente",
                    "Trámite",
                    "Etapa",
                    "Estado",
                    "Fecha Solicitud",
                    "Vencimiento",
                    "Publicación",
                    "Digital",
                    "_ID"
                };
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return perfilRegistroRecepcion
                        && column == COL_SELECCION_REGISTRO
                        && esFilaPrincipal(row);
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (perfilRegistroRecepcion) {
                    if (columnIndex == COL_EXPANDIR_REGISTRO) {
                        return String.class;
                    }
                    if (columnIndex == COL_SELECCION_REGISTRO) {
                        return Boolean.class;
                    }
                    if (columnIndex == COL_DIAS_REGISTRO) {
                        return Long.class;
                    }
                    if (columnIndex == COL_ESTADO_REGISTRO) {
                        return String.class;
                    }
                    if (columnIndex == COL_FECHA_SOLICITUD_REGISTRO || columnIndex == COL_FECHA_VENCIMIENTO_REGISTRO) {
                        return LocalDate.class;
                    }
                    return String.class;
                }
                if (columnIndex == COL_EXPANDIR_BANDEJA) {
                    return String.class;
                }
                if (columnIndex == COL_DIAS_BANDEJA || columnIndex == COL_ID_BANDEJA) {
                    return Long.class;
                }
                if (columnIndex == COL_FECHA_SOLICITUD_BANDEJA || columnIndex == COL_VENCIMIENTO_BANDEJA) {
                    return LocalDate.class;
                }
                return String.class;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column);
                if (perfilRegistroRecepcion && column == COL_SELECCION_REGISTRO && esFilaPrincipal(row)) {
                    Object idValue = getValueAt(row, getColumnCount() - 1);
                    Long id = toLong(idValue);
                    if (id != null) {
                        if (Boolean.TRUE.equals(aValue)) {
                            grupoFamiliarSeleccionados.add(id);
                        } else {
                            grupoFamiliarSeleccionados.remove(id);
                        }
                        if (!actualizandoGrupoFamiliarMasivo) {
                            notificarCambioGrupoFamiliar();
                        }
                    }
                }
            }
        };
    }

    private void configurarLayout() {
        setLayout(new BorderLayout(8, 8));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());

        configurarBotones();

        JPanel filtros = new AppV2FilterPanel();
        configurarControlesFiltro();

        JPanel filtrosContenido = new JPanel();
        filtrosContenido.setOpaque(false);
        filtrosContenido.setLayout(new BoxLayout(filtrosContenido, BoxLayout.Y_AXIS));

        JPanel filaBusqueda = new JPanel(new GridBagLayout());
        filaBusqueda.setOpaque(false);
        GridBagConstraints gbcBusqueda = new GridBagConstraints();
        gbcBusqueda.gridy = 0;
        gbcBusqueda.insets = new Insets(0, 0, 0, 12);
        gbcBusqueda.anchor = GridBagConstraints.WEST;

        gbcBusqueda.gridx = 0;
        gbcBusqueda.weightx = 0;
        filaBusqueda.add(crearLabelFiltro("Búsqueda"), gbcBusqueda);

        gbcBusqueda.gridx = 1;
        gbcBusqueda.weightx = 1.0;
        gbcBusqueda.fill = GridBagConstraints.HORIZONTAL;
        gbcBusqueda.insets = new Insets(0, 0, 0, 14);
        filaBusqueda.add(txtBusqueda, gbcBusqueda);

        JPanel acciones = AppV2ActionPanel.right();
        acciones.add(btnBuscar);
        acciones.add(btnLimpiar);
        if (!perfilRegistroRecepcion) {
            acciones.add(btnVerDetalle);
        }
        if (editarExpedienteHandler != null) {
            acciones.add(btnEditar);
            acciones.add(btnEliminar);
        }
        gbcBusqueda.gridx = 2;
        gbcBusqueda.weightx = 0;
        gbcBusqueda.fill = GridBagConstraints.NONE;
        gbcBusqueda.insets = new Insets(0, 0, 0, 0);
        filaBusqueda.add(acciones, gbcBusqueda);

        JPanel filaFechas = new JPanel(new GridBagLayout());
        filaFechas.setOpaque(false);
        GridBagConstraints gbcFechas = new GridBagConstraints();
        gbcFechas.gridy = 0;
        gbcFechas.anchor = GridBagConstraints.WEST;
        gbcFechas.insets = new Insets(0, 0, 0, 12);
        gbcFechas.gridx = 0;
        filaFechas.add(crearCampoFiltroInline("Fecha desde", fechaSolicitudDesde, 250), gbcFechas);
        gbcFechas.gridx = 1;
        filaFechas.add(crearCampoFiltroInline("Fecha hasta", fechaSolicitudHasta, 250), gbcFechas);
        gbcFechas.gridx = 2;
        gbcFechas.weightx = 1.0;
        gbcFechas.fill = GridBagConstraints.HORIZONTAL;
        filaFechas.add(Box.createHorizontalGlue(), gbcFechas);

        JPanel filaEstado = new JPanel(new GridBagLayout());
        filaEstado.setOpaque(false);
        GridBagConstraints gbcEstado = new GridBagConstraints();
        gbcEstado.gridy = 0;
        gbcEstado.anchor = GridBagConstraints.WEST;
        gbcEstado.insets = new Insets(0, 0, 0, 12);
        gbcEstado.gridx = 0;
        filaEstado.add(crearCampoFiltroInline("Estado", cmbEstado, 260), gbcEstado);
        gbcEstado.gridx = 1;
        filaEstado.add(spnLimite, gbcEstado);
        gbcEstado.gridx = 2;
        gbcEstado.weightx = 1.0;
        gbcEstado.fill = GridBagConstraints.HORIZONTAL;
        filaEstado.add(Box.createHorizontalGlue(), gbcEstado);

        filtrosContenido.add(filaBusqueda);
        filtrosContenido.add(Box.createVerticalStrut(6));
        filtrosContenido.add(filaFechas);
        filtrosContenido.add(Box.createVerticalStrut(6));
        filtrosContenido.add(filaEstado);
        filtros.add(filtrosContenido);

        btnVerDetalle.setEnabled(false);
        btnVerDetalle.setToolTipText("Disponible en bandeja general");
        btnEditar.setEnabled(false);
        btnEditar.setToolTipText("Disponible solo para expedientes Registrados sin asignación a abogado");
        btnEliminar.setEnabled(false);
        btnEliminar.setToolTipText("Disponible solo para expedientes Registrados sin asignación a abogado");

        JPanel superior = new JPanel(new BorderLayout(6, 6));
        superior.setOpaque(false);
        if (mostrarEncabezado) {
            JLabel titulo = new JLabel(tituloBandeja);
            titulo.setFont(AppV2Theme.fontBold(22));
            titulo.setForeground(AppV2Theme.TEXT_PRIMARY);
            JLabel subtitulo = new JLabel(subtituloBandeja);
            subtitulo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            subtitulo.setForeground(AppV2Theme.TEXT_SECONDARY);
            JPanel titleBlock = new JPanel(new BorderLayout(0, 4));
            titleBlock.setOpaque(false);
            titleBlock.add(titulo, BorderLayout.NORTH);
            titleBlock.add(subtitulo, BorderLayout.CENTER);
            superior.add(titleBlock, BorderLayout.NORTH);
        }

        lblSeleccionados.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblSeleccionados.setForeground(AppV2Theme.PRIMARY);
        lblResultado.setText(etapaBloqueada
                ? "Seleccione un expediente para abrir la consola."
                : (perfilRegistroRecepcion
                        ? "0 expediente(s) encontrado(s)"
                        : "Seleccione filtros y presione Buscar. Doble clic o Ver detalle abre la consola."));
        lblResultado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblResultado.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel mensajePanel = new JPanel();
        mensajePanel.setOpaque(false);
        mensajePanel.setLayout(new javax.swing.BoxLayout(mensajePanel, javax.swing.BoxLayout.Y_AXIS));
        if (perfilRegistroRecepcion) {
            mensajePanel.add(lblSeleccionados);
            mensajePanel.add(Box.createVerticalStrut(4));
        }
        mensajePanel.add(lblResultado);

        superior.add(filtros, BorderLayout.CENTER);
        superior.add(mensajePanel, BorderLayout.SOUTH);

        JPanel contenidoOperativo = new JPanel(new BorderLayout(8, 8));
        contenidoOperativo.setOpaque(false);
        contenidoOperativo.add(superior, BorderLayout.NORTH);
        contenidoOperativo.add(tablePanel, BorderLayout.CENTER);

        JPanel contenidoPrincipal = new JPanel(new BorderLayout(8, 8));
        contenidoPrincipal.setOpaque(false);
        if (encabezadoOperativo != null) {
            contenidoPrincipal.add(encabezadoOperativo, BorderLayout.NORTH);
        }
        contenidoPrincipal.add(contenidoOperativo, BorderLayout.CENTER);

        if (perfilRegistroRecepcion) {
            panelRecepcion = crearPanelRecepcion();
            panelRecepcionWrapper = crearPanelRecepcionConTab(panelRecepcion);
            if (usarSplitExterno) {
                add(contenidoPrincipal, BorderLayout.CENTER);
            } else {
                splitBandeja = new AppV2OperationalSplitPanel(
                        contenidoPrincipal,
                        panelRecepcionWrapper,
                        0,
                        PANEL_RECEPCION_ANCHO_MINIMO + PANEL_RECEPCION_TAB_OVERHANG,
                        PANEL_RECEPCION_ANCHO_NORMAL + PANEL_RECEPCION_TAB_OVERHANG);
                add(splitBandeja, BorderLayout.CENTER);
            }
        } else {
            add(contenidoPrincipal, BorderLayout.CENTER);
        }
    }

    private AppV2SideActionPanel crearPanelRecepcion() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de datos", this::ocultarPanelRecepcion);
        panel.setAccentColor(new Color(57, 125, 199));

        AppV2ResponsiveGridPanel secciones = new AppV2ResponsiveGridPanel(320, 2, 12, 12);
        secciones.add(crearSeccionDatosPlazo());
        secciones.add(crearSeccionDatosExpediente());
        secciones.add(crearSeccionDatosActa());
        secciones.add(crearSeccionDatosSolicitud());
        secciones.add(crearSeccionTitular());
        secciones.add(crearSeccionSolicitante());
        secciones.add(crearSeccionNotificacionUbicacion());

        panel.addSection(secciones);

        panelRegistrarGrupoFamiliar = new JPanelRegistrarGrupoFamiliarV2();
        panelAsociarDuplicados = new JPanelAsociarDuplicadosRecepcionV2();
        panelRecepcionCardsLayout = new CardLayout();
        panelRecepcionCards = new JPanel(panelRecepcionCardsLayout);
        panelRecepcionCards.setOpaque(false);
        panelRecepcionCards.add(panel, PANEL_RECEPCION_CARD_DATOS);
        panelRecepcionCards.add(panelRegistrarGrupoFamiliar, PANEL_RECEPCION_CARD_GF);
        panelRecepcionCards.add(panelAsociarDuplicados, PANEL_RECEPCION_CARD_ASOCIAR);
        panelRecepcionCardsLayout.show(panelRecepcionCards, PANEL_RECEPCION_CARD_DATOS);

        actualizarTabRecepcion(false, false);
        actualizarTabRegistrarGF(false, false);
        actualizarTabAsociarDuplicados(false, false);
        tabPanelRecepcion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                alternarPanelRecepcion(PANEL_RECEPCION_CARD_DATOS);
            }
        });
        tabPanelRegistrarGF.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                alternarPanelRecepcion(PANEL_RECEPCION_CARD_GF);
            }
        });
        tabPanelAsociarDuplicados.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                alternarPanelRecepcion(PANEL_RECEPCION_CARD_ASOCIAR);
            }
        });
        return panel;
    }

    private AppV2SideSectionPanel crearSeccionDatosPlazo() {
        AppV2SideSectionPanel seccion = new AppV2SideSectionPanel("Datos del plazo");
        seccion.addRow("Días", lblRecepcionDias);
        seccion.addRow("Fecha Vencimiento", lblRecepcionVencimiento);
        return seccion;
    }

    private AppV2SideSectionPanel crearSeccionDatosExpediente() {
        AppV2SideSectionPanel seccion = new AppV2SideSectionPanel("Datos del expediente");
        seccion.addRow("N° expediente", lblRecepcionExpediente);
        seccion.addRow("N° expediente SGD", lblRecepcionNumeroExpedienteSgd);
        return seccion;
    }

    private AppV2SideSectionPanel crearSeccionDatosActa() {
        AppV2SideSectionPanel seccion = new AppV2SideSectionPanel("Datos del acta");
        seccion.addRow("Tipo de acta", lblRecepcionTipoActa);
        seccion.addRow("Nro. acta", lblRecepcionNumeroActa);
        return seccion;
    }

    private AppV2SideSectionPanel crearSeccionDatosSolicitud() {
        AppV2SideSectionPanel seccion = new AppV2SideSectionPanel("Datos de solicitud");
        seccion.addRow("Fecha recepción", lblRecepcionFecha);
        seccion.addRow("Canal de ingreso", lblRecepcionCanal);
        seccion.addRow("Nro. trámite web", lblRecepcionTramite);
        seccion.addRow("Proc.Registral", lblRecepcionProcedimiento);
        seccion.addRow("Tipo documento", lblRecepcionTipoDocumento);
        seccion.addRow("N° documento", lblRecepcionNumeroDocumento);
        seccion.addRow("Tipo de solicitud", lblRecepcionTipoSolicitud);
        seccion.addRow("Grupo familiar", lblRecepcionMarcaOperativa);
        return seccion;
    }

    private AppV2SideSectionPanel crearSeccionTitular() {
        AppV2SideSectionPanel seccion = new AppV2SideSectionPanel("Datos del titular");
        seccion.addRow("Titular", lblRecepcionTitular);
        seccion.addRow("Tipo documento", lblRecepcionTipoDocumentoTitular);
        seccion.addRow("Número documento", lblRecepcionNumeroDocumentoTitular);
        return seccion;
    }

    private AppV2SideSectionPanel crearSeccionSolicitante() {
        AppV2SideSectionPanel seccion = new AppV2SideSectionPanel("Datos del solicitante");
        seccion.addRow("Solicitante", lblRecepcionRemitente);
        seccion.addRow("Tipo documento", lblRecepcionTipoDocumentoSolicitante);
        seccion.addRow("Número documento", lblRecepcionNumeroDocumentoSolicitante);
        return seccion;
    }

    private AppV2SideSectionPanel crearSeccionNotificacionUbicacion() {
        AppV2SideSectionPanel seccion = new AppV2SideSectionPanel("Datos de notificación y ubicación");
        seccion.addRow("Correo", lblRecepcionCorreo);
        seccion.addRow("Teléfono", lblRecepcionTelefono);
        seccion.addRow("Departamento", lblRecepcionDepartamento);
        seccion.addRow("Provincia", lblRecepcionProvincia);
        seccion.addRow("Distrito", lblRecepcionDistrito);
        seccion.addRow("Dirección", lblRecepcionDireccion);
        return seccion;
    }

    private JPanel crearPanelRecepcionConTab(final AppV2SideActionPanel panel) {
        JPanel wrapper = new JPanel(null) {
            @Override
            public void doLayout() {
                int width = getWidth();
                int height = getHeight();
                int panelX = PANEL_RECEPCION_TAB_OVERHANG;
                panelRecepcionCards.setBounds(panelX, 0, Math.max(0, width - panelX), height);
                int tabY = Math.min(PANEL_RECEPCION_TAB_TOP, Math.max(0, height - (PANEL_RECEPCION_TAB_HEIGHT * 3) - 28));
                tabPanelRecepcion.setBounds(
                        0,
                        tabY,
                        PANEL_RECEPCION_TAB_OVERHANG - 6,
                        PANEL_RECEPCION_TAB_HEIGHT);
                tabPanelRegistrarGF.setBounds(
                        0,
                        Math.min(height - (PANEL_RECEPCION_TAB_HEIGHT * 2) - 18, tabY + PANEL_RECEPCION_TAB_HEIGHT + 10),
                        PANEL_RECEPCION_TAB_OVERHANG - 6,
                        PANEL_RECEPCION_TAB_HEIGHT);
                tabPanelAsociarDuplicados.setBounds(
                        0,
                        Math.min(height - PANEL_RECEPCION_TAB_HEIGHT - 8, tabY + (PANEL_RECEPCION_TAB_HEIGHT + 10) * 2),
                        PANEL_RECEPCION_TAB_OVERHANG - 6,
                        PANEL_RECEPCION_TAB_HEIGHT);
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(panelRecepcionCards);
        wrapper.add(tabPanelRecepcion);
        wrapper.add(tabPanelRegistrarGF);
        wrapper.add(tabPanelAsociarDuplicados);
        wrapper.setMinimumSize(new Dimension(PANEL_RECEPCION_ANCHO_MINIMO + PANEL_RECEPCION_TAB_OVERHANG, 0));
        wrapper.setPreferredSize(new Dimension(PANEL_RECEPCION_ANCHO_NORMAL + PANEL_RECEPCION_TAB_OVERHANG, 0));
        return wrapper;
    }

    public JPanel getPanelRecepcionWrapper() {
        return panelRecepcionWrapper;
    }

    public void vincularSplitRecepcion(AppV2OperationalSplitPanel split) {
        this.splitBandeja = split;
    }

    private void aplicarConfiguracionInicial() {
        seleccionarEtapaInicial();
        if (perfilRegistroRecepcion) {
            restaurarFechasRegistro();
            cargarEstadosRegistroRecepcion();
        } else {
            restaurarFechasBandejaGeneral();
        }
        if (etapaBloqueada) {
            cmbEtapa.setEnabled(false);
            cmbEtapa.setToolTipText("Bandeja filtrada por etapa " + DisplayNameMapperV2.etapa(etapaInicial));
        }
    }

    private void configurarTabla() {
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        table.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        table.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        table.getTableHeader().setPreferredSize(new Dimension(0, 30));
        table.setGridColor(AppV2Theme.BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        BandejaCellRenderer renderer = new BandejaCellRenderer();
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Number.class, renderer);
        table.setDefaultRenderer(Long.class, renderer);
        AppV2TableColumnSizer.applyFriendlyDefaults(table);
        if (perfilRegistroRecepcion) {
            AppV2TableColumnSizer.applyWidths(table, 34, 38, 88, 165, 155, 150, 145, 145, 220, 130, 130, 260, 130, 190, 0);
            table.getColumnModel().getColumn(COL_EXPANDIR_REGISTRO).setMinWidth(34);
            table.getColumnModel().getColumn(COL_EXPANDIR_REGISTRO).setMaxWidth(38);
            table.getColumnModel().getColumn(COL_SELECCION_REGISTRO).setMinWidth(38);
            table.getColumnModel().getColumn(COL_SELECCION_REGISTRO).setMaxWidth(42);
            table.getColumnModel().getColumn(COL_PROC_REGISTRAL_REGISTRO).setMinWidth(220);
            table.getColumnModel().getColumn(COL_TIPO_ACTA_REGISTRO).setMinWidth(120);
            table.getColumnModel().getColumn(COL_ID_REGISTRO).setMinWidth(0);
            table.getColumnModel().getColumn(COL_ID_REGISTRO).setMaxWidth(0);
            tablePanel.getScrollPane().setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        } else {
            AppV2TableColumnSizer.applyWidths(table, 34, 88, 175, 155, 155, 175, 145, 145, 125, 125, 0);
            table.getColumnModel().getColumn(COL_EXPANDIR_BANDEJA).setMinWidth(34);
            table.getColumnModel().getColumn(COL_EXPANDIR_BANDEJA).setMaxWidth(38);
            table.getColumnModel().getColumn(COL_DIAS_BANDEJA).setMaxWidth(90);
            table.getColumnModel().getColumn(COL_EXPEDIENTE_BANDEJA).setMinWidth(150);
            table.getColumnModel().getColumn(COL_TRAMITE_BANDEJA).setMinWidth(130);
            table.getColumnModel().getColumn(COL_ETAPA_BANDEJA).setMinWidth(130);
            table.getColumnModel().getColumn(COL_ESTADO_BANDEJA).setMinWidth(150);
            table.getColumnModel().getColumn(COL_PUBLICACION_BANDEJA).setMaxWidth(125);
            table.getColumnModel().getColumn(COL_DIGITAL_BANDEJA).setMaxWidth(120);
            table.getColumnModel().getColumn(COL_ID_BANDEJA).setMinWidth(0);
            table.getColumnModel().getColumn(COL_ID_BANDEJA).setMaxWidth(0);
            tablePanel.getScrollPane().setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }
        if (perfilRegistroRecepcion) {
            table.getColumnModel().getColumn(COL_EXPANDIR_REGISTRO).setCellRenderer(new ExpandirRendererRegistro());
            table.getColumnModel().getColumn(COL_SELECCION_REGISTRO).setCellRenderer(new SeleccionRendererRegistro());
            table.getColumnModel().getColumn(COL_SELECCION_REGISTRO).setHeaderRenderer(new SelectAllHeaderRendererRegistro());
        } else {
            table.getColumnModel().getColumn(COL_EXPANDIR_BANDEJA).setCellRenderer(new ExpandirRendererRegistro());
        }
        instalarFiltrosPorColumna();
        configurarCabeceraSeleccionGrupoFamiliar();
    }

    private void instalarFiltrosPorColumna() {
        columnFilterSupport = AppV2ColumnFilterSupport.install(
                "BandejaExpedientes",
                table,
                tablePanel.getScrollPane(),
                null,
                () -> {
                    btnVerDetalle.setEnabled(false);
                    btnEditar.setEnabled(false);
                    btnEliminar.setEnabled(false);
                },
                perfilRegistroRecepcion
                        ? new int[]{COL_EXPANDIR_REGISTRO, COL_SELECCION_REGISTRO}
                        : new int[]{COL_EXPANDIR_BANDEJA});
        if (perfilRegistroRecepcion && columnFilterSupport != null) {
            columnFilterSupport.getSorter().addRowSorterListener(event -> actualizarEstadoHeaderSeleccionRegistro());
        }
    }

    private void limpiarFiltrosPorColumna() {
        if (columnFilterSupport != null) {
            columnFilterSupport.clearFilters();
        }
    }

    private JLabel crearLabelFiltro(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        label.setForeground(AppV2Theme.TEXT_SECONDARY);
        return label;
    }

    private JPanel crearCampoFiltroInline(String texto, Component control, int anchoPreferido) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        JLabel label = crearLabelFiltro(texto);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setPreferredSize(new Dimension(Math.max(78, label.getPreferredSize().width), control.getPreferredSize().height));
        panel.add(label, BorderLayout.WEST);
        panel.add(control, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(anchoPreferido, control.getPreferredSize().height));
        panel.setMinimumSize(new Dimension(Math.max(120, anchoPreferido), control.getPreferredSize().height));
        return panel;
    }

    private JPanel crearCampoFiltroInline(String texto, JSpinner control, int anchoPreferido) {
        return crearCampoFiltroInline(texto, (Component) control, anchoPreferido);
    }

    private void configurarControlesFiltro() {
        if (perfilRegistroRecepcion) {
            txtBusqueda.setPlaceholder("Buscar expediente, trámite/SGD, acta, titular o documento");
        }
        txtBusqueda.setColumns(34);
        txtBusqueda.setPreferredSize(new Dimension(perfilRegistroRecepcion ? 520 : 360, 34));
        txtBusqueda.setMinimumSize(new Dimension(perfilRegistroRecepcion ? 320 : 280, 34));

        cmbEtapa.setPreferredSize(new Dimension(190, 34));
        cmbEtapa.setMinimumSize(new Dimension(180, 34));
        cmbEstado.setPreferredSize(new Dimension(240, 34));
        cmbEstado.setMinimumSize(new Dimension(220, 34));

        Dimension fechaSize = new Dimension(250, 42);
        fechaSolicitudDesde.setPreferredSize(fechaSize);
        fechaSolicitudDesde.setMinimumSize(new Dimension(210, 42));
        fechaSolicitudHasta.setPreferredSize(fechaSize);
        fechaSolicitudHasta.setMinimumSize(new Dimension(210, 42));

        Dimension limiteSize = new Dimension(86, 34);
        spnLimite.setPreferredSize(limiteSize);
        spnLimite.setMinimumSize(limiteSize);

        cmbEtapa.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        cmbEstado.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        txtBusqueda.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
    }

    private void configurarBotones() {
        AppV2Theme.estilizarBotonPrimario(btnBuscar);
        btnVerDetalle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnEditar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnEliminar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnLimpiar.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnVerDetalle.addActionListener(e -> abrirDetalleSeleccionado());
        btnEditar.addActionListener(e -> editarSeleccionado());
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean haySeleccion = table.getSelectedRow() >= 0;
                btnVerDetalle.setEnabled(haySeleccion);
                btnEditar.setEnabled(haySeleccion && esRegistroSeleccionadoEditable());
                btnEliminar.setEnabled(haySeleccion && esRegistroSeleccionadoEditable());
                if (perfilRegistroRecepcion) {
                    if (haySeleccion && splitBandeja != null && splitBandeja.isSideVisible()) {
                        mostrarPanelRecepcionSeleccionado();
                    } else if (!haySeleccion) {
                        ocultarPanelRecepcion();
                    }
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                int viewColumn = table.columnAtPoint(e.getPoint());
                if (viewRow >= 0
                        && viewColumn >= 0
                        && table.convertColumnIndexToModel(viewColumn) == COL_EXPANDIR_REGISTRO) {
                    alternarExpansionFilaRegistro(table.convertRowIndexToModel(viewRow));
                    return;
                }
                if (e.getClickCount() == 2
                        && perfilRegistroRecepcion
                        && table.getSelectedRow() >= 0) {
                    mostrarPanelRecepcionSeleccionado();
                }
            }
        });
    }

    public void refrescar() {
        buscar();
    }

    public List<Long> obtenerIdsGrupoFamiliarSeleccionados() {
        List<Long> ids = new ArrayList<Long>();
        if (!perfilRegistroRecepcion) {
            return ids;
        }
        ids.addAll(grupoFamiliarSeleccionados);
        return ids;
    }

    public int contarIdsGrupoFamiliarSeleccionados() {
        return grupoFamiliarSeleccionados.size();
    }

    public void limpiarSeleccionGrupoFamiliar() {
        if (!perfilRegistroRecepcion) {
            return;
        }
        grupoFamiliarSeleccionados.clear();
        if (tableModel.getRowCount() > 0) {
            actualizandoGrupoFamiliarMasivo = true;
            try {
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    if (esFilaPrincipal(row)) {
                        tableModel.setValueAt(Boolean.FALSE, row, COL_SELECCION_REGISTRO);
                    }
                }
            } finally {
                actualizandoGrupoFamiliarMasivo = false;
            }
        }
        actualizarEstadoHeaderSeleccionRegistro();
        notificarCambioGrupoFamiliar();
    }

    public void registrarGrupoFamiliarSeleccionados() throws SQLException {
        if (!perfilRegistroRecepcion) {
            return;
        }
        if (grupoFamiliarSeleccionados.isEmpty()) {
            throw new IllegalStateException("Seleccione al menos un expediente para registrar grupo familiar.");
        }
        GrupoFamiliarRegistroService service = new GrupoFamiliarRegistroService();
        service.registrarGrupoFamiliar(new ArrayList<Long>(grupoFamiliarSeleccionados));
        limpiarSeleccionGrupoFamiliar();
    }

    public void setOnGrupoFamiliarSelectionChanged(Runnable onGrupoFamiliarSelectionChanged) {
        this.onGrupoFamiliarSelectionChanged = onGrupoFamiliarSelectionChanged;
    }

    public void vincularMetricasAlertasRegistro(MetricCardV2 potencialDuplicado, MetricCardV2 posibleGrupoFamiliar) {
        vincularMetricasAlertasRegistro(potencialDuplicado, posibleGrupoFamiliar, null);
    }

    public void vincularMetricasAlertasRegistro(
            MetricCardV2 potencialDuplicado, MetricCardV2 posibleGrupoFamiliar, MetricCardV2 grupoFamiliarConfirmado) {
        this.cardPotencialDuplicadoRegistro = potencialDuplicado;
        this.cardPosibleGrupoFamiliarRegistro = posibleGrupoFamiliar;
        this.cardGrupoFamiliarConfirmadoRegistro = grupoFamiliarConfirmado;
        actualizarMetricasAlertasRegistro(ultimoResultadoBuscado);
        marcarFiltroAlertaRegistro();
    }

    public void alternarFiltroAlertaRegistro(String codigoAlerta) {
        if (!perfilRegistroRecepcion) {
            return;
        }
        String filtro = normalizar(codigoAlerta);
        filtroAlertaRegistro = "POTENCIAL_DUPLICADO".equals(filtro)
                || "POSIBLE_GRUPO_FAMILIAR".equals(filtro)
                || "GRUPO_FAMILIAR_CONFIRMADO".equals(filtro)
                ? filtro : null;
        marcarFiltroAlertaRegistro();
        buscar(true);
    }

    private void buscar() {
        buscar(false);
    }

    private void buscar(boolean conservarFiltroAlerta) {
        long secuencia = secuenciaBusqueda.incrementAndGet();
        if (perfilRegistroRecepcion) {
            grupoFamiliarSeleccionados.clear();
            notificarCambioGrupoFamiliar();
        }
        if (perfilRegistroRecepcion && !conservarFiltroAlerta) {
            filtroAlertaRegistro = null;
            marcarFiltroAlertaRegistro();
        }
        String texto = txtBusqueda.getText();
        String etapa = perfilRegistroRecepcion
                ? "REGISTRO"
                : (etapaBloqueada ? etapaInicial : null);
        String estado = codigoSeleccionado(cmbEstado);
        LocalDate fechaDesde = fechaSeleccionada(fechaSolicitudDesde);
        LocalDate fechaHasta = fechaSeleccionada(fechaSolicitudHasta);
        int limite = ((Number) spnLimite.getValue()).intValue();

        if (fechaDesde != null && fechaHasta != null && fechaHasta.isBefore(fechaDesde)) {
            JOptionPane.showMessageDialog(
                    this,
                    "La Fecha hasta no puede ser menor que la Fecha desde.",
                    perfilRegistroRecepcion ? "Filtros de Registro / Recepción" : "Filtros de Bandeja de Expedientes",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        SwingWorker<List<ExpedienteBandejaDTO>, Void> workerAnterior = busquedaActiva;
        if (workerAnterior != null && !workerAnterior.isDone()) {
            workerAnterior.cancel(true);
        }
        setBuscando(true);
        SwingWorker<List<ExpedienteBandejaDTO>, Void> worker = new SwingWorker<List<ExpedienteBandejaDTO>, Void>() {
            @Override
            protected List<ExpedienteBandejaDTO> doInBackground() throws Exception {
                return consultaService.buscarBandeja(texto, etapa, estado, fechaDesde, fechaHasta, limite);
            }

            @Override
                protected void done() {
                    try {
                        if (secuencia != secuenciaBusqueda.get()) {
                            return;
                        }
                        List<ExpedienteBandejaDTO> expedientes = get();
                        if (perfilRegistroRecepcion) {
                            expedientes = filtrarPorAlertaRegistro(expedientes);
                            expedientes = filtrarExpedientesPrincipalesRegistro(expedientes);
                        }
                        ultimoResultadoBuscado = expedientes == null ? Collections.emptyList() : new ArrayList<>(expedientes);
                        actualizarMetricasAlertasRegistro(expedientes);
                        cargarTabla(expedientes);
                    } catch (Exception ex) {
                        mostrarError(ex);
                    } finally {
                        if (secuencia == secuenciaBusqueda.get()) {
                            setBuscando(false);
                        }
                }
            }
        };
        busquedaActiva = worker;
        worker.execute();
    }

    private void limpiar() {
        txtBusqueda.setText("");
        if (etapaBloqueada) {
            seleccionarEtapaInicial();
        } else {
            cmbEtapa.setSelectedIndex(0);
        }
        cmbEstado.setSelectedIndex(0);
        filtroAlertaRegistro = null;
        marcarFiltroAlertaRegistro();
        if (perfilRegistroRecepcion) {
            restaurarFechasRegistro();
        } else {
            restaurarFechasBandejaGeneral();
        }
        spnLimite.setValue(200);
        limpiarFiltrosPorColumna();
        grupoFamiliarSeleccionados.clear();
        filasTabla.clear();
        principalesExpandidos.clear();
        principalesCargando.clear();
        asociadosCacheRegistro.clear();
        idExpedienteExpansionActiva = null;
        notificarCambioGrupoFamiliar();
        tableModel.setRowCount(0);
        btnVerDetalle.setEnabled(false);
        btnEditar.setEnabled(false);
        btnEliminar.setEnabled(false);
        ocultarPanelRecepcion();
        tablePanel.setEmpty(true);
        lblResultado.setText(etapaBloqueada
                ? "Filtros limpiados. La bandeja permanece filtrada por la etapa seleccionada."
                : "Filtros limpiados. Presione Buscar para cargar expedientes.");
    }

    private void cargarTabla(List<ExpedienteBandejaDTO> expedientes) {
        ocultarPanelRecepcion();
        principalesExpandidos.clear();
        principalesCargando.clear();
        asociadosCacheRegistro.clear();
        idExpedienteExpansionActiva = null;
        filasTabla.clear();
        tableModel.setRowCount(0);
        btnVerDetalle.setEnabled(false);
        btnEditar.setEnabled(false);
        btnEliminar.setEnabled(false);
        for (ExpedienteBandejaDTO item : expedientes) {
            if (perfilRegistroRecepcion) {
                agregarFilaRegistro(item);
            } else {
                agregarFilaBandeja(item);
            }
        }
        actualizarEstadoHeaderSeleccionRegistro();
        tablePanel.setEmpty(expedientes.isEmpty());
        notificarCambioGrupoFamiliar();
        if (perfilRegistroRecepcion) {
            lblResultado.setText(expedientes.size() + " expediente(s) encontrado(s)");
        } else {
            lblResultado.setText(expedientes.isEmpty()
                    ? "No se encontraron expedientes con los filtros ingresados."
                    : expedientes.size() + " expediente(s) encontrado(s). Seleccione uno y presione Ver detalle.");
        }
        actualizarTextoSeleccionadosRegistro();
    }

    private List<ExpedienteBandejaDTO> filtrarPorAlertaRegistro(List<ExpedienteBandejaDTO> expedientes) {
        List<ExpedienteBandejaDTO> filtrados = new ArrayList<>();
        if (expedientes == null || filtroAlertaRegistro == null) {
            return expedientes == null ? filtrados : new ArrayList<>(expedientes);
        }
        for (ExpedienteBandejaDTO item : expedientes) {
            if (coincideAlertaRegistro(item)) {
                filtrados.add(item);
            }
        }
        return filtrados;
    }

    private List<ExpedienteBandejaDTO> filtrarExpedientesPrincipalesRegistro(List<ExpedienteBandejaDTO> expedientes) {
        List<ExpedienteBandejaDTO> filtrados = new ArrayList<>();
        if (expedientes == null) {
            return filtrados;
        }
        for (ExpedienteBandejaDTO item : expedientes) {
            if (item != null && !item.isRelacionadoComoHijo()) {
                filtrados.add(item);
            }
        }
        return filtrados;
    }

    private void agregarFilaRegistro(ExpedienteBandejaDTO item) {
        if (item == null) {
            return;
        }
        filasTabla.add(RegistroTableRow.principal(item));
        tableModel.addRow(new Object[]{
            iconoExpansionRegistro(item),
            Boolean.FALSE,
            item.getDiasRestantes() == null ? "" : item.getDiasRestantes(),
            item.getNumeroExpediente(),
            item.getNumeroExpedienteSgd(),
            valorFiltro(item.getCanal()),
            fechaSolicitud(item),
            item.getFechaVencimiento(),
            item.getProcedimiento(),
            item.getTipoActa(),
            item.getNumeroActa(),
            item.getTitular(),
            DisplayNameMapperV2.estado(item.getEstadoCodigo()),
            alertaRegistro(item),
            item.getIdExpediente()
        });
    }

    private void agregarFilaAsociadaRegistro(ExpedienteBandejaDTO principal, ExpedienteRelacionadoDTO asociado, int index) {
        if (principal == null || asociado == null) {
            return;
        }
        RegistroTableRow row = RegistroTableRow.asociada(principal, asociado);
        if (index < 0 || index > filasTabla.size()) {
            index = filasTabla.size();
        }
        filasTabla.add(index, row);
        tableModel.insertRow(index, new Object[]{
            "",
            null,
            asociado.getDiasRestantes() == null ? "" : asociado.getDiasRestantes(),
            principal.getNumeroExpediente(),
            valorFiltro(asociado.getNumeroExpedienteSgd()),
            valorFiltro(principal.getCanal()),
            asociado.getFechaRecepcion(),
            asociado.getFechaVencimiento(),
            procedimientoAsociadoRegistro(asociado),
            valorUi(asociado.getTipoActa()),
            valorUi(asociado.getNumeroActa()),
            valorUi(asociado.getTitular()),
            estadoAsociadoRegistro(asociado),
            alertaAsociadaRegistro(asociado),
            asociado.getIdExpediente()
        });
    }

    private void agregarFilaBandeja(ExpedienteBandejaDTO item) {
        if (item == null) {
            return;
        }
        filasTabla.add(RegistroTableRow.principal(item));
        tableModel.addRow(new Object[]{
            iconoExpansionRegistro(item),
            item.getDiasRestantes(),
            item.getNumeroExpediente(),
            item.getNumeroTramiteDocumentario(),
            DisplayNameMapperV2.etapa(item.getEtapaCodigo()),
            DisplayNameMapperV2.estado(item.getEstadoCodigo()),
            fechaSolicitud(item),
            item.getFechaVencimiento(),
            item.isRequierePublicacion() ? "Requiere" : "No",
            item.isExpedienteDigitalCompleto() ? "Completo" : "Pendiente",
            item.getIdExpediente()
        });
    }

    private void agregarFilaAsociadaBandeja(ExpedienteBandejaDTO principal, ExpedienteRelacionadoDTO asociado, int index) {
        if (principal == null || asociado == null) {
            return;
        }
        RegistroTableRow row = RegistroTableRow.asociada(principal, asociado);
        if (index < 0 || index > filasTabla.size()) {
            index = filasTabla.size();
        }
        filasTabla.add(index, row);
        tableModel.insertRow(index, new Object[]{
            "",
            asociado.getDiasRestantes() == null ? "" : asociado.getDiasRestantes(),
            valorUi(principal.getNumeroExpediente()),
            valorUi(asociado.getNumeroTramiteDocumentario()),
            DisplayNameMapperV2.etapa(asociado.getEtapaCodigo()),
            estadoAsociadoRegistro(asociado),
            asociado.getFechaRecepcion(),
            asociado.getFechaVencimiento(),
            "-",
            "-",
            asociado.getIdExpediente()
        });
    }

    private String iconoExpansionRegistro(ExpedienteBandejaDTO item) {
        if (item == null || item.getIdExpediente() == null || item.getRelacionesConfirmadasComoPrincipal() <= 0) {
            return "";
        }
        if (principalesCargando.contains(item.getIdExpediente())) {
            return "loading";
        }
        return principalesExpandidos.contains(item.getIdExpediente()) ? "expanded" : "collapsed";
    }

    private void refrescarIconoExpansionRegistro(int modelRow) {
        if (modelRow < 0 || modelRow >= filasTabla.size()) {
            return;
        }
        RegistroTableRow row = filasTabla.get(modelRow);
        if (row == null || !row.esPrincipal()) {
            return;
        }
        tableModel.setValueAt(iconoExpansionRegistro(row.principal), modelRow, COL_EXPANDIR_REGISTRO);
    }

    private void alternarExpansionFilaRegistro(int modelRow) {
        finalizarEdicionTabla();
        if (modelRow < 0 || modelRow >= filasTabla.size()) {
            return;
        }
        RegistroTableRow row = filasTabla.get(modelRow);
        if (row == null || !row.esPrincipal() || row.principal.getIdExpediente() == null
                || row.principal.getRelacionesConfirmadasComoPrincipal() <= 0) {
            return;
        }
        Long idPrincipal = row.principal.getIdExpediente();
        if (principalesExpandidos.contains(idPrincipal)
                || (idPrincipal.equals(idExpedienteExpansionActiva) && principalesCargando.contains(idPrincipal))) {
            contraerAsociadosRegistro(idPrincipal);
            principalesCargando.remove(idPrincipal);
            idExpedienteExpansionActiva = null;
            refrescarIconoExpansionRegistro(indiceFilaPrincipalRegistro(idPrincipal));
            return;
        }
        contraerTodosExceptoRegistro(idPrincipal);
        idExpedienteExpansionActiva = idPrincipal;
        List<ExpedienteRelacionadoDTO> cache = asociadosCacheRegistro.get(idPrincipal);
        if (cache != null) {
            insertarAsociadosRegistro(modelRow, row.principal, cache);
            return;
        }
        if (principalesCargando.contains(idPrincipal)) {
            return;
        }
        principalesCargando.add(idPrincipal);
        refrescarIconoExpansionRegistro(modelRow);
        SwingWorker<List<ExpedienteRelacionadoDTO>, Void> worker = new SwingWorker<List<ExpedienteRelacionadoDTO>, Void>() {
            @Override
            protected List<ExpedienteRelacionadoDTO> doInBackground() throws Exception {
                return relacionadoService.listarAsociadosConfirmados(idPrincipal);
            }

            @Override
            protected void done() {
                principalesCargando.remove(idPrincipal);
                int principalRow = indiceFilaPrincipalRegistro(idPrincipal);
                if (principalRow < 0) {
                    return;
                }
                if (!idPrincipal.equals(idExpedienteExpansionActiva)) {
                    refrescarIconoExpansionRegistro(principalRow);
                    return;
                }
                try {
                    List<ExpedienteRelacionadoDTO> asociados = get();
                    asociadosCacheRegistro.put(idPrincipal, asociados);
                    insertarAsociadosRegistro(principalRow, filasTabla.get(principalRow).principal, asociados);
                } catch (Exception ex) {
                    refrescarIconoExpansionRegistro(principalRow);
                    mostrarError("No se pudieron cargar los expedientes asociados.", ex);
                }
            }
        };
        worker.execute();
    }

    private void insertarAsociadosRegistro(int principalRow, ExpedienteBandejaDTO principal, List<ExpedienteRelacionadoDTO> asociados) {
        if (principal == null || principal.getIdExpediente() == null || principalesExpandidos.contains(principal.getIdExpediente())) {
            return;
        }
        Long idPrincipal = principal.getIdExpediente();
        if (!idPrincipal.equals(idExpedienteExpansionActiva)) {
            return;
        }
        contraerTodosExceptoRegistro(idPrincipal);
        principalRow = indiceFilaPrincipalRegistro(idPrincipal);
        if (principalRow < 0) {
            return;
        }
        principalesExpandidos.add(idPrincipal);
        int insertAt = principalRow + 1;
        if (asociados != null) {
            for (ExpedienteRelacionadoDTO asociado : asociados) {
                if (perfilRegistroRecepcion) {
                    agregarFilaAsociadaRegistro(principal, asociado, insertAt);
                } else {
                    agregarFilaAsociadaBandeja(principal, asociado, insertAt);
                }
                insertAt++;
            }
        }
        refrescarIconoExpansionRegistro(principalRow);
        table.revalidate();
        table.repaint();
    }

    private void contraerTodosExceptoRegistro(Long idPermitido) {
        List<Long> expandidos = new ArrayList<>(principalesExpandidos);
        for (Long id : expandidos) {
            if (id != null && !id.equals(idPermitido)) {
                contraerAsociadosRegistro(id);
            }
        }
        List<Long> cargando = new ArrayList<>(principalesCargando);
        for (Long id : cargando) {
            if (id != null && !id.equals(idPermitido)) {
                principalesCargando.remove(id);
                refrescarIconoExpansionRegistro(indiceFilaPrincipalRegistro(id));
            }
        }
    }

    private void contraerAsociadosRegistro(Long idPrincipal) {
        if (idPrincipal == null) {
            return;
        }
        int principalRow = indiceFilaPrincipalRegistro(idPrincipal);
        if (principalRow < 0) {
            principalesExpandidos.remove(idPrincipal);
            if (idPrincipal.equals(idExpedienteExpansionActiva)) {
                idExpedienteExpansionActiva = null;
            }
            return;
        }
        int selectedRow = obtenerModelRowSeleccionada();
        boolean seleccionarPrincipal = false;
        RegistroTableRow selected = filaRegistro(selectedRow);
        if (selected != null && selected.esAsociada() && idPrincipal.equals(selected.idExpedientePrincipal)) {
            seleccionarPrincipal = true;
        }
        for (int i = filasTabla.size() - 1; i > principalRow; i--) {
            RegistroTableRow row = filasTabla.get(i);
            if (row != null && row.esAsociada() && idPrincipal.equals(row.idExpedientePrincipal)) {
                filasTabla.remove(i);
                tableModel.removeRow(i);
            }
        }
        principalesExpandidos.remove(idPrincipal);
        if (idPrincipal.equals(idExpedienteExpansionActiva)) {
            idExpedienteExpansionActiva = null;
        }
        refrescarIconoExpansionRegistro(principalRow);
        if (seleccionarPrincipal && principalRow >= 0 && principalRow < tableModel.getRowCount()) {
            int viewRow = table.convertRowIndexToView(principalRow);
            if (viewRow >= 0 && viewRow < table.getRowCount()) {
                table.setRowSelectionInterval(viewRow, viewRow);
            }
        }
    }

    private int indiceFilaPrincipalRegistro(Long idPrincipal) {
        if (idPrincipal == null) {
            return -1;
        }
        for (int i = 0; i < filasTabla.size(); i++) {
            RegistroTableRow row = filasTabla.get(i);
            if (row != null && row.esPrincipal() && idPrincipal.equals(row.getIdPrincipal())) {
                return i;
            }
        }
        return -1;
    }

    private RegistroTableRow filaRegistro(int modelRow) {
        if (modelRow < 0 || modelRow >= filasTabla.size()) {
            return null;
        }
        return filasTabla.get(modelRow);
    }

    private boolean esFilaPrincipal(int modelRow) {
        RegistroTableRow fila = filaRegistro(modelRow);
        return fila != null && fila.esPrincipal();
    }

    private String procedimientoAsociadoRegistro(ExpedienteRelacionadoDTO asociado) {
        if (asociado == null) {
            return "-";
        }
        String procedimiento = asociado.getProcedimiento();
        if (procedimiento == null || procedimiento.trim().isEmpty() || pareceIdentificadorTecnico(procedimiento)) {
            return "-";
        }
        return procedimiento.trim();
    }

    private String estadoAsociadoRegistro(ExpedienteRelacionadoDTO asociado) {
        if (asociado == null || asociado.getEstadoCodigo().isEmpty()) {
            return "Documento asociado";
        }
        if (asociado.isRecibidoPorAbogado()) {
            return "Recibido por abogado";
        }
        return DisplayNameMapperV2.estado(asociado.getEstadoCodigo());
    }

    private String alertaRegistro(ExpedienteBandejaDTO item) {
        if (item == null) {
            return "Sin Alerta";
        }
        if (contienePotencialDuplicado(item)) {
            return "Potencial duplicado";
        }
        if (contienePosibleGrupoFamiliar(item)) {
            return "Posible Grupo Familiar";
        }
        return "Sin Alerta";
    }

    private String alertaAsociadaRegistro(ExpedienteRelacionadoDTO asociado) {
        if (asociado == null) {
            return "Sin Alerta";
        }
        String alerta = normalizarFiltro(asociado.getAlertaIngreso());
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

    private boolean coincideAlertaRegistro(ExpedienteBandejaDTO item) {
        if (item == null) {
            return false;
        }
        if ("POTENCIAL_DUPLICADO".equals(filtroAlertaRegistro)) {
            return contienePotencialDuplicado(item);
        }
        if ("POSIBLE_GRUPO_FAMILIAR".equals(filtroAlertaRegistro)) {
            return contienePosibleGrupoFamiliar(item);
        }
        if ("GRUPO_FAMILIAR_CONFIRMADO".equals(filtroAlertaRegistro)) {
            return esGrupoFamiliar(item);
        }
        return true;
    }

    private void actualizarMetricasAlertasRegistro(List<ExpedienteBandejaDTO> expedientes) {
        if (!perfilRegistroRecepcion) {
            return;
        }
        int total = 0;
        int duplicados = 0;
        int grupoFamiliar = 0;
        int grupoFamiliarConfirmado = 0;
        if (expedientes != null) {
            total = expedientes.size();
            for (ExpedienteBandejaDTO item : expedientes) {
                if (item == null) {
                    continue;
                }
                if (contienePotencialDuplicado(item)) {
                    duplicados++;
                }
                if (contienePosibleGrupoFamiliar(item)) {
                    grupoFamiliar++;
                }
                if (esGrupoFamiliar(item)) {
                    grupoFamiliarConfirmado++;
                }
            }
        }
        if (cardPotencialDuplicadoRegistro != null) {
            cardPotencialDuplicadoRegistro.setValue(String.valueOf(duplicados));
        }
        if (cardPosibleGrupoFamiliarRegistro != null) {
            cardPosibleGrupoFamiliarRegistro.setValue(String.valueOf(grupoFamiliar));
        }
        if (cardGrupoFamiliarConfirmadoRegistro != null) {
            cardGrupoFamiliarConfirmadoRegistro.setValue(String.valueOf(grupoFamiliarConfirmado));
        }
        marcarFiltroAlertaRegistro();
    }

    private boolean contienePotencialDuplicado(ExpedienteBandejaDTO item) {
        String alertas = normalizarFiltro(item == null ? null : item.getAlertas());
        return alertas.contains("potencial duplicado");
    }

    private boolean contienePosibleGrupoFamiliar(ExpedienteBandejaDTO item) {
        String alertas = normalizarFiltro(item == null ? null : item.getAlertas());
        if (alertas.contains("posible grupo familiar")) {
            return true;
        }
        String grupo = normalizarFiltro(item == null ? null : item.getGrupoFamiliar());
        return grupo.contains("posible grupo familiar");
    }

    private void marcarFiltroAlertaRegistro() {
        if (cardPotencialDuplicadoRegistro != null) {
            cardPotencialDuplicadoRegistro.setSelected("POTENCIAL_DUPLICADO".equals(filtroAlertaRegistro));
        }
        if (cardPosibleGrupoFamiliarRegistro != null) {
            cardPosibleGrupoFamiliarRegistro.setSelected("POSIBLE_GRUPO_FAMILIAR".equals(filtroAlertaRegistro));
        }
        if (cardGrupoFamiliarConfirmadoRegistro != null) {
            cardGrupoFamiliarConfirmadoRegistro.setSelected("GRUPO_FAMILIAR_CONFIRMADO".equals(filtroAlertaRegistro));
        }
    }

    private boolean esGrupoFamiliar(ExpedienteBandejaDTO item) {
        if (item == null) {
            return false;
        }
        String valor = normalizarFiltro(item.getGrupoFamiliar());
        if (valor.isEmpty()) {
            return false;
        }
        return valor.contains("GRUPO FAMILIAR")
                || valor.contains("IDENTIFICADO")
                || valor.contains("POSIBLE")
                || "SI".equals(valor)
                || "S".equals(valor)
                || "1".equals(valor)
                || "TRUE".equals(valor);
    }

    private void mostrarPanelRecepcionSeleccionado() {
        if (!perfilRegistroRecepcion || panelRecepcion == null) {
            return;
        }
        final Long idExpediente = obtenerIdExpedienteSeleccionado();
        if (idExpediente == null) {
            ocultarPanelRecepcion();
            return;
        }
        if (splitBandeja != null) {
            splitBandeja.setSideVisible(true);
        }
        mostrarPanelRecepcion(PANEL_RECEPCION_CARD_DATOS, false);
        if (idExpediente.equals(idPanelRecepcionActual) && panelRecepcionCargado) {
            return;
        }
        idPanelRecepcionActual = idExpediente;
        panelRecepcionCargado = false;
        final int sequence = ++panelRecepcionLoadSequence;
        setPanelRecepcionLoading();

        SwingWorker<ExpedienteConsolaDTO, Void> worker = new SwingWorker<ExpedienteConsolaDTO, Void>() {
            @Override
            protected ExpedienteConsolaDTO doInBackground() throws Exception {
                return detalleService.obtenerConsolaPorExpediente(idExpediente);
            }

            @Override
            protected void done() {
                if (sequence != panelRecepcionLoadSequence || !idExpediente.equals(obtenerIdExpedienteSeleccionado())) {
                    return;
                }
                try {
                    cargarPanelRecepcion(get());
                    panelRecepcionCargado = true;
                } catch (Exception ex) {
                    setValue(lblRecepcionExpediente, "No se pudo cargar");
                    setValue(lblRecepcionEtapaEstado, mensajeError(ex));
                }
            }
        };
        worker.execute();

        SwingWorker<com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO, Void> workerUbicacion =
                new SwingWorker<com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO, Void>() {
            @Override
            protected com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO doInBackground() throws Exception {
                return asignacionExpedienteServiceRecepcion.obtenerExpedientePorId(idExpediente);
            }

            @Override
            protected void done() {
                if (sequence != panelRecepcionLoadSequence || !idExpediente.equals(obtenerIdExpedienteSeleccionado())) {
                    return;
                }
                try {
                    cargarNotificacionUbicacionRecepcion(get());
                } catch (Exception ex) {
                    // El bloque de notificación y ubicación queda con su valor por defecto ("-").
                }
            }
        };
        workerUbicacion.execute();
    }

    private void cargarNotificacionUbicacionRecepcion(com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO expediente) {
        if (expediente == null) {
            return;
        }
        setValue(lblRecepcionCorreo, expediente.getCorreoSolicitante());
        setValue(lblRecepcionTelefono, expediente.getTelefonoSolicitante());
        setValue(lblRecepcionDepartamento, expediente.getDepartamentoSolicitante());
        setValue(lblRecepcionProvincia, expediente.getProvinciaSolicitante());
        setValue(lblRecepcionDistrito, expediente.getDistritoSolicitante());
        setValue(lblRecepcionDireccion, expediente.getDireccionSolicitante());
    }

    private void cargarPanelRecepcion(ExpedienteConsolaDTO expediente) {
        if (panelRecepcion == null) {
            return;
        }
        actualizarTituloPanelRecepcion(expediente);
        setValue(lblRecepcionExpediente, expediente.getNumeroExpediente());
        setValue(lblRecepcionFecha, formatDate(expediente.getFechaRecepcion()));
        setValue(lblRecepcionCanal, expediente.getCanalRecepcion());
        setValue(lblRecepcionTipoSolicitud, expediente.getTipoSolicitud());
        setValue(lblRecepcionTramite, expediente.getNumeroTramiteDocumentario());
        setValue(lblRecepcionNumeroExpedienteSgd, expediente.getNumeroExpedienteSgd());
        setValue(lblRecepcionTipoDocumento, expediente.getTipoDocumento());
        setValue(lblRecepcionNumeroDocumento, expediente.getNumeroDocumento());
        setValue(lblRecepcionGrupoFamiliar, expediente.isGrupoFamiliar() ? "Sí" : "No");
        lblRecepcionGrupoFamiliar.setToolTipText(toolTipGrupoFamiliar(expediente));
        setValue(lblRecepcionResultadoInicial,
                DisplayNameMapperV2.etapa(expediente.getEtapaCodigo())
                + " / "
                + DisplayNameMapperV2.estado(expediente.getEstadoCodigo()));
        setValue(lblRecepcionHojaEnvio, safe(expediente.getNumeroTramiteDocumentario()));
        setValue(lblRecepcionCorreo, "-");
        setValue(lblRecepcionTelefono, "-");
        setValue(lblRecepcionDepartamento, "-");
        setValue(lblRecepcionProvincia, "-");
        setValue(lblRecepcionDistrito, "-");
        setValue(lblRecepcionDireccion, "-");
        setValue(lblRecepcionTipoDocumentoTitular, extraerTipoDocumentoPersona(expediente.getTitularDocumento()));
        setValue(lblRecepcionNumeroDocumentoTitular, extraerNumeroDocumentoPersona(expediente.getTitularDocumento()));
        setValue(lblRecepcionTipoDocumentoSolicitante, extraerTipoDocumentoPersona(expediente.getRemitenteDocumento()));
        setValue(lblRecepcionNumeroDocumentoSolicitante, extraerNumeroDocumentoPersona(expediente.getRemitenteDocumento()));
        setValue(lblRecepcionProcedimiento, expediente.getProcedimiento());
        setValue(lblRecepcionTitular, expediente.getTitular());
        setValue(lblRecepcionRemitente, expediente.getRemitente());
        setValue(lblRecepcionTipoActa, expediente.getTipoActa());
        setValue(lblRecepcionNumeroActa, expediente.getNumeroActa());
        setValue(lblRecepcionPrioridad, "-");
        setValue(lblRecepcionMarcaOperativa, expediente.isGrupoFamiliar() ? "Sí" : "No");
        setValue(lblRecepcionObservacion, "-");
        actualizarBadgeDias(lblRecepcionDias, expediente.getDiasRestantes());
        setValue(lblRecepcionVencimiento, formatDate(expediente.getFechaVencimiento()));
        setValue(lblRecepcionResponsable, expediente.getResponsableActual());
        setValue(lblRecepcionEquipo, expediente.getEquipoActual());
        setValue(lblRecepcionEtapaEstado,
                DisplayNameMapperV2.etapa(expediente.getEtapaCodigo())
                + " / "
                + DisplayNameMapperV2.estado(expediente.getEstadoCodigo()));
        panelRecepcion.revalidate();
        panelRecepcion.repaint();
    }

    private void setPanelRecepcionLoading() {
        actualizarTituloPanelRecepcion(null);
        setValue(lblRecepcionExpediente, "Cargando...");
        setValue(lblRecepcionEtapaEstado, "");
        setValue(lblRecepcionFecha, "");
        setValue(lblRecepcionCanal, "");
        setValue(lblRecepcionTipoSolicitud, "");
        setValue(lblRecepcionTramite, "");
        setValue(lblRecepcionNumeroExpedienteSgd, "");
        setValue(lblRecepcionTipoDocumento, "");
        setValue(lblRecepcionNumeroDocumento, "");
        setValue(lblRecepcionGrupoFamiliar, "");
        setValue(lblRecepcionResultadoInicial, "");
        setValue(lblRecepcionHojaEnvio, "");
        setValue(lblRecepcionCorreo, "");
        setValue(lblRecepcionTelefono, "");
        setValue(lblRecepcionDepartamento, "");
        setValue(lblRecepcionProvincia, "");
        setValue(lblRecepcionDistrito, "");
        setValue(lblRecepcionDireccion, "");
        setValue(lblRecepcionTipoDocumentoTitular, "");
        setValue(lblRecepcionNumeroDocumentoTitular, "");
        setValue(lblRecepcionTipoDocumentoSolicitante, "");
        setValue(lblRecepcionNumeroDocumentoSolicitante, "");
        setValue(lblRecepcionPrioridad, "");
        setValue(lblRecepcionMarcaOperativa, "");
        setValue(lblRecepcionObservacion, "");
        setValue(lblRecepcionProcedimiento, "");
        setValue(lblRecepcionTitular, "");
        setValue(lblRecepcionRemitente, "");
        setValue(lblRecepcionTipoActa, "");
        setValue(lblRecepcionNumeroActa, "");
        actualizarBadgeDias(lblRecepcionDias, null);
        setValue(lblRecepcionVencimiento, "");
        setValue(lblRecepcionResponsable, "");
        setValue(lblRecepcionEquipo, "");
    }

    private void ocultarPanelRecepcion() {
        if (!perfilRegistroRecepcion) {
            return;
        }
        panelRecepcionCargado = false;
        idPanelRecepcionActual = null;
        panelRecepcionLoadSequence++;
        panelRecepcionActivo = PANEL_RECEPCION_CARD_DATOS;
        if (splitBandeja != null) {
            splitBandeja.setSideVisible(false);
        }
        actualizarTabRecepcion(false, false);
        actualizarTabRegistrarGF(false, false);
        actualizarTabAsociarDuplicados(false, false);
    }

    private void actualizarTituloPanelRecepcion(ExpedienteConsolaDTO expediente) {
        if (panelRecepcion == null) {
            return;
        }
        String titulo = "<html><div style='font-size:18px;font-weight:700;color:#1c242e;'>Panel de datos</div>";
        if (expediente != null) {
            String titular = expediente.getTitular();
            if (titular == null || titular.trim().isEmpty()) {
                titular = expediente.getNumeroExpediente();
            }
            if (titular != null && !titular.trim().isEmpty()) {
                titulo = titulo + "<div style='font-size:12px;font-weight:600;color:rgb(21,71,117);margin-top:2px;'>" + escapeHtml(titular.trim()) + "</div>";
            }
        }
        titulo += "</html>";
        panelRecepcion.setTitle(titulo);
    }

    private void alternarExpansionPanelRecepcion() {
        if (splitBandeja == null) {
            return;
        }
        if (!splitBandeja.isSideVisible()) {
            splitBandeja.setSideVisible(true);
        }
        boolean expandido = splitBandeja.toggleSideExpanded();
        actualizarTabRecepcion(true, expandido);
        if (PANEL_RECEPCION_CARD_GF.equals(panelRecepcionActivo)) {
            actualizarTabRegistrarGF(true, expandido);
        }
        if (PANEL_RECEPCION_CARD_ASOCIAR.equals(panelRecepcionActivo)) {
            actualizarTabAsociarDuplicados(true, expandido);
        }
    }

    private AppV2StackedSideTab crearTabRecepcion() {
        return new AppV2StackedSideTab(
                "Datos",
                PANEL_RECEPCION_TAB_OVERHANG - 6,
                PANEL_RECEPCION_TAB_HEIGHT,
                new Color(230, 241, 245),
                new Color(57, 125, 199),
                new Color(33, 99, 174));
    }

    private AppV2StackedSideTab crearTabRegistrarGF() {
        return new AppV2StackedSideTab(
                "Registrar G.F",
                PANEL_RECEPCION_TAB_OVERHANG - 6,
                PANEL_RECEPCION_TAB_HEIGHT,
                new Color(248, 240, 225),
                new Color(201, 129, 42),
                new Color(156, 96, 22));
    }

    private AppV2StackedSideTab crearTabAsociarDuplicados() {
        return new AppV2StackedSideTab(
                "Asociar",
                PANEL_RECEPCION_TAB_OVERHANG - 6,
                PANEL_RECEPCION_TAB_HEIGHT,
                new Color(249, 239, 224),
                new Color(198, 121, 31),
                new Color(156, 96, 22));
    }

    private void actualizarTabRecepcion(boolean seleccionado, boolean expandido) {
        tabPanelRecepcion.setState(seleccionado, expandido);
        tabPanelRecepcion.setToolTipText(expandido
                ? "Restaurar datos de recepción"
                : "Expandir datos de recepción");
    }

    private void actualizarTabRegistrarGF(boolean seleccionado, boolean expandido) {
        tabPanelRegistrarGF.setState(seleccionado, expandido);
        tabPanelRegistrarGF.setToolTipText(expandido
                ? "Restaurar registrar grupo familiar"
                : "Expandir registrar grupo familiar");
    }

    private void actualizarTabAsociarDuplicados(boolean seleccionado, boolean expandido) {
        tabPanelAsociarDuplicados.setState(seleccionado, expandido);
        tabPanelAsociarDuplicados.setToolTipText(expandido
                ? "Restaurar asociar duplicados"
                : "Expandir asociar duplicados");
    }

    private void alternarPanelRecepcion(String panelId) {
        if (panelId == null || splitBandeja == null || panelRecepcionCardsLayout == null) {
            return;
        }
        boolean mismoPanel = panelId.equals(panelRecepcionActivo);
        if (!splitBandeja.isSideVisible()) {
            splitBandeja.setSideVisible(true);
            mismoPanel = false;
        }
        panelRecepcionCardsLayout.show(panelRecepcionCards, panelId);
        panelRecepcionActivo = panelId;
        if (mismoPanel) {
            splitBandeja.toggleSideExpanded();
        }
        boolean expandido = splitBandeja.isSideExpanded();
        actualizarTabRecepcion(PANEL_RECEPCION_CARD_DATOS.equals(panelId), expandido && PANEL_RECEPCION_CARD_DATOS.equals(panelId));
        actualizarTabRegistrarGF(PANEL_RECEPCION_CARD_GF.equals(panelId), expandido && PANEL_RECEPCION_CARD_GF.equals(panelId));
        actualizarTabAsociarDuplicados(PANEL_RECEPCION_CARD_ASOCIAR.equals(panelId), expandido && PANEL_RECEPCION_CARD_ASOCIAR.equals(panelId));
        if (PANEL_RECEPCION_CARD_GF.equals(panelId) && panelRegistrarGrupoFamiliar != null) {
            panelRegistrarGrupoFamiliar.actualizarEstado();
        }
        if (PANEL_RECEPCION_CARD_ASOCIAR.equals(panelId) && panelAsociarDuplicados != null) {
            panelAsociarDuplicados.cargarDuplicados(obtenerExpedientePrincipalSeleccionado());
        }
    }

    private void mostrarPanelRecepcion(String panelId, boolean forzarExpandido) {
        if (splitBandeja == null || panelRecepcionCardsLayout == null) {
            return;
        }
        if (!splitBandeja.isSideVisible()) {
            splitBandeja.setSideVisible(true);
        }
        panelRecepcionActivo = panelId;
        panelRecepcionCardsLayout.show(panelRecepcionCards, panelId);
        if (forzarExpandido && !splitBandeja.isSideExpanded()) {
            splitBandeja.setSideExpanded(true);
        }
        boolean expandido = splitBandeja.isSideExpanded();
        actualizarTabRecepcion(PANEL_RECEPCION_CARD_DATOS.equals(panelId), expandido && PANEL_RECEPCION_CARD_DATOS.equals(panelId));
        actualizarTabRegistrarGF(PANEL_RECEPCION_CARD_GF.equals(panelId), expandido && PANEL_RECEPCION_CARD_GF.equals(panelId));
        actualizarTabAsociarDuplicados(PANEL_RECEPCION_CARD_ASOCIAR.equals(panelId), expandido && PANEL_RECEPCION_CARD_ASOCIAR.equals(panelId));
        if (PANEL_RECEPCION_CARD_GF.equals(panelId) && panelRegistrarGrupoFamiliar != null) {
            panelRegistrarGrupoFamiliar.actualizarEstado();
        }
        if (PANEL_RECEPCION_CARD_ASOCIAR.equals(panelId) && panelAsociarDuplicados != null) {
            panelAsociarDuplicados.cargarDuplicados(obtenerExpedientePrincipalSeleccionado());
        }
    }

    private void abrirDetalleSeleccionado() {
        Long idExpediente = obtenerIdExpedienteSeleccionado();
        if (idExpediente == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un expediente para ver el detalle.",
                    "SDRERC",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(owner, idExpediente);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void editarSeleccionado() {
        if (editarExpedienteHandler == null) {
            return;
        }
        Long idExpediente = obtenerIdExpedienteSeleccionado();
        if (idExpediente == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un expediente para editar.",
                    "Registro / Recepción",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!esRegistroSeleccionadoEditable()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Solo se permite editar expedientes en estado Registrado y sin asignación a abogado.",
                    "Edición manual no disponible",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        editarExpedienteHandler.accept(idExpediente);
    }

    private void eliminarSeleccionado() {
        Long idExpediente = obtenerIdExpedienteSeleccionado();
        if (idExpediente == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un expediente para eliminar.",
                    "Registro / Recepción",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!esRegistroSeleccionadoEditable()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Solo se permite eliminar expedientes en estado Registrado y sin asignación a abogado.",
                    "Eliminar no disponible",
                    JOptionPane.INFORMATION_MESSAGE);
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
        setBuscando(true);
        final Long idExpedienteFinal = idExpediente;
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                edicionManualService.eliminar(idExpedienteFinal);
                return null;
            }

            @Override
            protected void done() {
                setBuscando(false);
                try {
                    get();
                    ocultarPanelRecepcion();
                    buscar();
                    JOptionPane.showMessageDialog(
                            JPanelBandejaExpedientesNueva.this,
                            "El registro fue eliminado correctamente.",
                            "Eliminar registro",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    mostrarError(ex);
                }
            }
        };
        worker.execute();
    }

    private boolean esRegistroSeleccionadoEditable() {
        if (!perfilRegistroRecepcion || table.getSelectedRow() < 0 || tableModel.getColumnCount() == 0) {
            return false;
        }
        int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
        int indiceEstado = tableModel.findColumn("Estado");
        if (indiceEstado < 0) {
            return false;
        }
        Object estado = tableModel.getValueAt(modelRow, indiceEstado);
        String valor = estado == null ? "" : estado.toString().trim();
        return "Registrado".equalsIgnoreCase(valor) || "REGISTRADO".equalsIgnoreCase(valor);
    }

    private Long obtenerIdExpedienteSeleccionado() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        Object value = tableModel.getValueAt(modelRow, tableModel.getColumnCount() - 1);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void notificarCambioGrupoFamiliar() {
        actualizarEstadoHeaderSeleccionRegistro();
        if (panelRegistrarGrupoFamiliar != null) {
            panelRegistrarGrupoFamiliar.actualizarEstado();
        }
        if (onGrupoFamiliarSelectionChanged != null) {
            SwingUtilities.invokeLater(onGrupoFamiliarSelectionChanged);
        }
    }

    private void finalizarEdicionTabla() {
        if (table != null && table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }

    private int obtenerModelRowSeleccionada() {
        int selectedRow = table.getSelectedRow();
        return selectedRow < 0 ? -1 : table.convertRowIndexToModel(selectedRow);
    }

    private ExpedienteBandejaDTO obtenerExpedientePrincipalSeleccionado() {
        RegistroTableRow fila = filaRegistro(obtenerModelRowSeleccionada());
        return fila == null ? null : fila.principal;
    }

    private boolean requiereDecisionNumero(ExpedienteBandejaDTO item) {
        if (item == null) {
            return false;
        }
        String numero = item.getNumeroExpediente();
        return "REGISTRO".equalsIgnoreCase(item.getEtapaCodigo())
                && "REGISTRADO".equalsIgnoreCase(item.getEstadoCodigo())
                && (numero == null || numero.trim().isEmpty())
                && ProcedimientoRegistralRules.requiereDecisionAsignacionParaNumero(item.getProcedimiento());
    }

    private static boolean pareceIdentificadorTecnico(String value) {
        if (value == null) {
            return false;
        }
        String texto = value.trim();
        if (texto.isEmpty()) {
            return false;
        }
        String upper = texto.toUpperCase(Locale.ROOT);
        return upper.matches(".*[A-Z]{2,}_[A-Z0-9_]+.*")
                || upper.matches(".*\\b[A-Z]{3,}[0-9]{2,}.*")
                || upper.contains("ANALISIS_")
                || upper.contains("PROYECTO_");
    }

    private final class JPanelRegistrarGrupoFamiliarV2 extends AppV2SideActionPanel {

        private final JLabel lblSeleccionados = new JLabel("0");
        private final JLabel lblEstado = new JLabel("Sin expedientes marcados");
        private final JButton btnRegistrar = new JButton("Registrar G.F.");
        private final JButton btnLimpiar = new JButton("Limpiar selección");

        private JPanelRegistrarGrupoFamiliarV2() {
            super("Registrar G.F.");
            setAccentColor(AppV2Theme.TEAL);

            AppV2SideSectionPanel resumen = new AppV2SideSectionPanel("Confirmación");
            resumen.addRow("Seleccionados", lblSeleccionados);
            resumen.addRow("Estado", lblEstado);
            addSection(resumen);

            JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            acciones.setOpaque(false);
            btnRegistrar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
            btnLimpiar.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            btnRegistrar.addActionListener(e -> registrarGrupoFamiliar());
            btnLimpiar.addActionListener(e -> limpiarSeleccion());
            acciones.add(btnRegistrar);
            acciones.add(btnLimpiar);
            setFooter(acciones);
            actualizarEstado();
        }

        private void actualizarEstado() {
            int seleccionados = contarIdsGrupoFamiliarSeleccionados();
            lblSeleccionados.setText(String.valueOf(seleccionados));
            lblEstado.setText(seleccionados > 0 ? "Listo para confirmar" : "Sin expedientes marcados");
            btnRegistrar.setEnabled(seleccionados > 0);
            btnLimpiar.setEnabled(seleccionados > 0);
        }

        private void registrarGrupoFamiliar() {
            if (contarIdsGrupoFamiliarSeleccionados() == 0) {
                JOptionPane.showMessageDialog(
                        JPanelBandejaExpedientesNueva.this,
                        "Seleccione al menos un expediente marcado como posible grupo familiar.",
                        "Registrar G.F.",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int confirmacion = JOptionPane.showConfirmDialog(
                    JPanelBandejaExpedientesNueva.this,
                    "¿Registrar grupo familiar en los expedientes seleccionados?",
                    "Registrar G.F.",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirmacion != JOptionPane.OK_OPTION) {
                return;
            }
            setEnabled(false);
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    registrarGrupoFamiliarSeleccionados();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        refrescar();
                        JOptionPane.showMessageDialog(
                                JPanelBandejaExpedientesNueva.this,
                                "Grupo familiar registrado y alerta desactivada.",
                                "Registrar G.F.",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                JPanelBandejaExpedientesNueva.this,
                                ex.getMessage() == null ? "No se pudo registrar el grupo familiar." : ex.getMessage(),
                                "Registrar G.F.",
                                JOptionPane.ERROR_MESSAGE);
                    } finally {
                        setEnabled(true);
                        actualizarEstado();
                    }
                }
            };
            worker.execute();
        }

        private void limpiarSeleccion() {
            limpiarSeleccionGrupoFamiliar();
            actualizarEstado();
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            btnRegistrar.setEnabled(enabled && contarIdsGrupoFamiliarSeleccionados() > 0);
            btnLimpiar.setEnabled(enabled && contarIdsGrupoFamiliarSeleccionados() > 0);
        }
    }

    private final class JPanelAsociarDuplicadosRecepcionV2 extends AppV2SideActionPanel {

        private final DefaultTableModel modeloDuplicados = new DefaultTableModel(
                new Object[]{"", "N° expediente", "Titular", "Tipo/N° acta", "Motivo coincidencia"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : Object.class;
            }
        };
        private final JTable tablaDuplicados = new AppV2Table(modeloDuplicados);
        private final AppV2TablePanel panelTablaDuplicados = new AppV2TablePanel(
                tablaDuplicados, "Sin duplicados detectados", "No hay posibles duplicados para el expediente seleccionado.");
        private final JLabel lblEstadoAsociar = new JLabel("Seleccione un expediente en la bandeja.");
        private final JLabel lblExpedientePrincipalAsociacion = new JLabel("-");
        private final JButton btnAsociarDuplicados = new JButton("Asociar seleccionados");
        private final JButton btnAsociarRapido = new JButton("Asociar todo");
        private final JButton btnGenerarNumeroExpediente = new JButton("Generar número de expediente");
        private final AppV2SideSectionPanel seccionDecisionNumero = new AppV2SideSectionPanel("Decisión de número");
        private List<ExpedienteRelacionadoDTO> duplicadosActuales = new ArrayList<ExpedienteRelacionadoDTO>();
        private ExpedienteBandejaDTO expedientePrincipalDuplicados;
        private Long idExpedientePrincipalDuplicados;
        private long secuenciaCargaDuplicados;

        private JPanelAsociarDuplicadosRecepcionV2() {
            super("Asociar duplicados");
            setAccentColor(new Color(198, 121, 31));

            AppV2SideSectionPanel seccionSeleccion = new AppV2SideSectionPanel("Selección y alertas");
            seccionSeleccion.addRow("Estado", lblEstadoAsociar);
            JPanel content = new JPanel(new BorderLayout(6, 6));
            content.setOpaque(false);
            content.setPreferredSize(new Dimension(320, 220));
            JPanel encabezadoTabla = new JPanel();
            encabezadoTabla.setOpaque(false);
            encabezadoTabla.setLayout(new BoxLayout(encabezadoTabla, BoxLayout.Y_AXIS));
            JLabel tituloTabla = new JLabel("Coincidencias por N° de acta y titular");
            tituloTabla.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
            tituloTabla.setForeground(AppV2Theme.TEXT_PRIMARY);
            tituloTabla.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel ayudaTabla = new JLabel("<html>El expediente <b>principal</b> es el que ya tiene número. "
                    + "Marque el/los <b>potenciales duplicados</b> (sin número) para asociarlos a él.</html>");
            ayudaTabla.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            ayudaTabla.setForeground(AppV2Theme.TEXT_SECONDARY);
            ayudaTabla.setAlignmentX(Component.LEFT_ALIGNMENT);
            encabezadoTabla.add(tituloTabla);
            encabezadoTabla.add(ayudaTabla);
            content.add(encabezadoTabla, BorderLayout.NORTH);
            content.add(panelTablaDuplicados, BorderLayout.CENTER);
            seccionSeleccion.addContent(content);
            addSection(seccionSeleccion);

            AppV2SideSectionPanel seccionRapida = new AppV2SideSectionPanel("Asociación rápida");
            seccionRapida.addRow("Expediente principal", lblExpedientePrincipalAsociacion);
            AppV2Theme.estilizarBotonPrimario(btnAsociarRapido);
            btnAsociarRapido.setEnabled(false);
            btnAsociarRapido.setToolTipText(
                    "Asociar todas las coincidencias detectadas por misma acta y titular, sin marcarlas una por una.");
            seccionRapida.addRow("Acción", btnAsociarRapido);
            addSection(seccionRapida);

            AppV2Theme.estilizarBotonPrimario(btnGenerarNumeroExpediente);
            btnGenerarNumeroExpediente.setEnabled(false);
            btnGenerarNumeroExpediente.setToolTipText("Disponible solo para Reconsideración/Apelación registrada sin número.");
            seccionDecisionNumero.addRow("Acción", btnGenerarNumeroExpediente);
            addSection(seccionDecisionNumero);
            seccionDecisionNumero.setVisible(false);

            tablaDuplicados.setRowHeight(28);
            tablaDuplicados.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            AppV2TableColumnSizer.applyWidths(tablaDuplicados, 34, 140, 160, 120, 200);

            AppV2Theme.estilizarBotonPrimario(btnAsociarDuplicados);
            btnAsociarDuplicados.addActionListener(e -> asociarSeleccionados());
            btnAsociarRapido.addActionListener(e -> asociarRapido());
            btnGenerarNumeroExpediente.addActionListener(e -> generarNumeroExpediente());
            JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            acciones.setOpaque(false);
            acciones.add(btnAsociarDuplicados);
            setFooter(acciones);
            modeloDuplicados.addTableModelListener(evento -> {
                if (evento.getColumn() == 0) {
                    actualizarEtiquetaPrincipalPorSeleccion();
                }
            });
            actualizarEstadoBoton();
        }

        private void cargarDuplicados(ExpedienteBandejaDTO expedientePrincipal) {
            expedientePrincipalDuplicados = expedientePrincipal;
            Long idExpediente = expedientePrincipal == null ? null : expedientePrincipal.getIdExpediente();
            idExpedientePrincipalDuplicados = idExpediente;
            actualizarDecisionNumero();
            if (idExpediente == null) {
                duplicadosActuales = new ArrayList<ExpedienteRelacionadoDTO>();
                modeloDuplicados.setRowCount(0);
                lblEstadoAsociar.setText("Seleccione un expediente en la bandeja.");
                lblExpedientePrincipalAsociacion.setText("-");
                actualizarEstadoBoton();
                return;
            }
            lblExpedientePrincipalAsociacion.setText(textoExpedientePrincipal(expedientePrincipal));
            lblEstadoAsociar.setText("Buscando posibles duplicados...");
            final long sequence = ++secuenciaCargaDuplicados;
            SwingWorker<List<ExpedienteRelacionadoDTO>, Void> worker = new SwingWorker<List<ExpedienteRelacionadoDTO>, Void>() {
                @Override
                protected List<ExpedienteRelacionadoDTO> doInBackground() throws Exception {
                    return relacionadoDeteccionService.listarPosiblesRelacionados(idExpediente);
                }

                @Override
                protected void done() {
                    if (sequence != secuenciaCargaDuplicados) {
                        return;
                    }
                    try {
                        duplicadosActuales = get();
                    } catch (Exception ex) {
                        duplicadosActuales = new ArrayList<ExpedienteRelacionadoDTO>();
                        lblEstadoAsociar.setText(mensajeError(ex));
                    }
                    modeloDuplicados.setRowCount(0);
                    for (ExpedienteRelacionadoDTO relacionado : duplicadosActuales) {
                        modeloDuplicados.addRow(new Object[]{
                                Boolean.TRUE,
                                tieneNumeroExpediente(relacionado) ? relacionado.getNumeroExpediente().trim() : "Sin número (potencial duplicado)",
                                relacionado.getTitular(),
                                (relacionado.getTipoActa() + " " + relacionado.getNumeroActa()).trim(),
                                relacionado.getMotivoCoincidencia()
                        });
                    }
                    if (duplicadosActuales.isEmpty()) {
                        lblEstadoAsociar.setText("No se detectaron posibles duplicados.");
                    } else {
                        lblEstadoAsociar.setText(duplicadosActuales.size() + " posible(s) duplicado(s) detectado(s).");
                    }
                    actualizarEstadoBoton();
                    actualizarEtiquetaPrincipalPorSeleccion();
                }
            };
            worker.execute();
        }

        private String textoExpedientePrincipal(ExpedienteBandejaDTO expedientePrincipal) {
            String numero = expedientePrincipal.getNumeroExpediente();
            String texto = (numero == null || numero.trim().isEmpty()) ? "(sin número)" : numero.trim();
            String sgd = expedientePrincipal.getNumeroExpedienteSgd();
            if (sgd != null && !sgd.trim().isEmpty()) {
                texto += " / SGD " + sgd.trim();
            }
            return texto;
        }

        private boolean tieneNumeroExpediente(ExpedienteBandejaDTO expediente) {
            return expediente != null
                    && expediente.getNumeroExpediente() != null
                    && !expediente.getNumeroExpediente().trim().isEmpty();
        }

        private boolean tieneNumeroExpediente(ExpedienteRelacionadoDTO expediente) {
            return expediente != null
                    && expediente.getNumeroExpediente() != null
                    && !expediente.getNumeroExpediente().trim().isEmpty();
        }

        private List<Long> obtenerIdsMarcados() {
            List<Long> ids = new ArrayList<Long>();
            for (int i = 0; i < modeloDuplicados.getRowCount() && i < duplicadosActuales.size(); i++) {
                Boolean marcado = (Boolean) modeloDuplicados.getValueAt(i, 0);
                if (Boolean.TRUE.equals(marcado)) {
                    Long idRelacionado = duplicadosActuales.get(i).getIdExpediente();
                    if (idRelacionado != null) {
                        ids.add(idRelacionado);
                    }
                }
            }
            return ids;
        }

        /**
         * Si el expediente seleccionado en la bandeja (potencial principal) no tiene numero, pero
         * entre los marcados hay exactamente uno CON numero, ese es el principal real: se invierte
         * la asociacion. Si hay mas de uno marcado con numero, es ambiguo y no se decide solo.
         */
        private ResolucionPrincipalDuplicados resolverPrincipal(List<Long> idsSeleccionados) {
            if (expedientePrincipalDuplicados == null || idExpedientePrincipalDuplicados == null) {
                return new ResolucionPrincipalDuplicados(null, null, new ArrayList<Long>(), false);
            }
            if (tieneNumeroExpediente(expedientePrincipalDuplicados)) {
                return new ResolucionPrincipalDuplicados(
                        idExpedientePrincipalDuplicados,
                        textoExpedientePrincipal(expedientePrincipalDuplicados),
                        idsSeleccionados,
                        false);
            }
            ExpedienteRelacionadoDTO candidatoConNumero = null;
            int totalConNumero = 0;
            for (Long idSeleccionado : idsSeleccionados) {
                for (ExpedienteRelacionadoDTO candidato : duplicadosActuales) {
                    if (candidato != null && idSeleccionado.equals(candidato.getIdExpediente()) && tieneNumeroExpediente(candidato)) {
                        candidatoConNumero = candidato;
                        totalConNumero++;
                    }
                }
            }
            if (totalConNumero == 0) {
                return new ResolucionPrincipalDuplicados(
                        idExpedientePrincipalDuplicados,
                        textoExpedientePrincipal(expedientePrincipalDuplicados),
                        idsSeleccionados,
                        false);
            }
            if (totalConNumero > 1) {
                return new ResolucionPrincipalDuplicados(null, null, new ArrayList<Long>(), true);
            }
            List<Long> idsFinal = new ArrayList<Long>();
            for (Long idSeleccionado : idsSeleccionados) {
                if (!idSeleccionado.equals(candidatoConNumero.getIdExpediente())) {
                    idsFinal.add(idSeleccionado);
                }
            }
            idsFinal.add(idExpedientePrincipalDuplicados);
            String texto = candidatoConNumero.getNumeroExpediente().trim();
            String sgd = candidatoConNumero.getNumeroExpedienteSgd();
            if (sgd != null && !sgd.trim().isEmpty()) {
                texto += " / SGD " + sgd.trim();
            }
            return new ResolucionPrincipalDuplicados(candidatoConNumero.getIdExpediente(), texto, idsFinal, false);
        }

        private void actualizarEtiquetaPrincipalPorSeleccion() {
            if (idExpedientePrincipalDuplicados == null) {
                return;
            }
            ResolucionPrincipalDuplicados resolucion = resolverPrincipal(obtenerIdsMarcados());
            if (resolucion.ambiguo) {
                lblExpedientePrincipalAsociacion.setText("Ambiguo");
                lblExpedientePrincipalAsociacion.setToolTipText(
                        "Hay más de un expediente con número entre los marcados. Solo puede haber un principal: "
                                + "marque únicamente los que no tienen número.");
                btnAsociarDuplicados.setEnabled(false);
                btnAsociarRapido.setEnabled(false);
                return;
            }
            lblExpedientePrincipalAsociacion.setText(
                    resolucion.textoPrincipal != null ? resolucion.textoPrincipal : textoExpedientePrincipal(expedientePrincipalDuplicados));
            lblExpedientePrincipalAsociacion.setToolTipText(
                    resolucion.idPrincipal != null && resolucion.idPrincipal.equals(idExpedientePrincipalDuplicados)
                            ? "Expediente principal destino de la asociación."
                            : "El expediente seleccionado no tiene número: se detectó automáticamente como principal "
                                    + "al que sí tiene número entre las coincidencias.");
            actualizarEstadoBoton();
        }

        private void actualizarEstadoBoton() {
            boolean hayCandidatos = idExpedientePrincipalDuplicados != null && !duplicadosActuales.isEmpty();
            btnAsociarDuplicados.setEnabled(hayCandidatos);
            btnAsociarRapido.setEnabled(hayCandidatos);
        }

        private void actualizarDecisionNumero() {
            boolean habilitado = requiereDecisionNumero(expedientePrincipalDuplicados);
            seccionDecisionNumero.setVisible(habilitado);
            btnGenerarNumeroExpediente.setEnabled(habilitado);
        }

        private void asociarSeleccionados() {
            if (idExpedientePrincipalDuplicados == null || duplicadosActuales.isEmpty()) {
                return;
            }
            List<Long> marcados = obtenerIdsMarcados();
            if (marcados.isEmpty()) {
                JOptionPane.showMessageDialog(
                        JPanelBandejaExpedientesNueva.this,
                        "Seleccione al menos un duplicado para asociar.",
                        "Asociar duplicados",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            ResolucionPrincipalDuplicados resolucion = resolverPrincipal(marcados);
            if (resolucion.ambiguo) {
                mostrarAmbiguedadPrincipal();
                return;
            }
            int confirmacion = JOptionPane.showConfirmDialog(
                    JPanelBandejaExpedientesNueva.this,
                    "Se asociarán " + resolucion.idsRelacionados.size() + " expediente(s) sin número como duplicados del "
                            + "expediente principal " + resolucion.textoPrincipal + ".\n"
                            + "¿Desea continuar?",
                    "Asociar duplicados",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
            ejecutarAsociacion(resolucion.idPrincipal, resolucion.idsRelacionados,
                    "Documento duplicado asociado desde Recepción por misma acta y titular.");
        }

        private void asociarRapido() {
            if (idExpedientePrincipalDuplicados == null || duplicadosActuales.isEmpty()) {
                return;
            }
            final List<Long> todos = new ArrayList<Long>();
            for (ExpedienteRelacionadoDTO relacionado : duplicadosActuales) {
                if (relacionado.getIdExpediente() != null) {
                    todos.add(relacionado.getIdExpediente());
                }
            }
            if (todos.isEmpty()) {
                return;
            }
            ResolucionPrincipalDuplicados resolucion = resolverPrincipal(todos);
            if (resolucion.ambiguo) {
                mostrarAmbiguedadPrincipal();
                return;
            }
            int confirmacion = JOptionPane.showConfirmDialog(
                    JPanelBandejaExpedientesNueva.this,
                    "Se asociarán los " + resolucion.idsRelacionados.size() + " registro(s) detectados como documentos "
                            + "duplicados del expediente principal si comparten el mismo número de acta y titular.\n"
                            + "Expediente principal destino: " + resolucion.textoPrincipal + "\n"
                            + "¿Desea continuar?",
                    "Confirmar asociación rápida",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
            ejecutarAsociacion(resolucion.idPrincipal, resolucion.idsRelacionados,
                    "Documento duplicado asociado desde Recepción por misma acta y titular (asociación rápida).");
        }

        private void mostrarAmbiguedadPrincipal() {
            JOptionPane.showMessageDialog(
                    JPanelBandejaExpedientesNueva.this,
                    "Hay más de un expediente con número entre los duplicados detectados/marcados. Solo puede haber un "
                            + "expediente principal: desmarque los que no correspondan y deje marcado únicamente el "
                            + "principal junto con los potenciales duplicados sin número.",
                    "Asociar duplicados",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        private void ejecutarAsociacion(Long idPrincipalResuelto, List<Long> ids, String motivo) {
            final Long idPrincipal = idPrincipalResuelto;
            final ExpedienteBandejaDTO principal = expedientePrincipalDuplicados;
            btnAsociarDuplicados.setEnabled(false);
            btnAsociarRapido.setEnabled(false);
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    relacionadoService.asociarRelacionados(idPrincipal, ids, motivo);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        refrescar();
                        cargarDuplicados(principal);
                        JOptionPane.showMessageDialog(
                                JPanelBandejaExpedientesNueva.this,
                                "Duplicados asociados correctamente.",
                                "Asociar duplicados",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                JPanelBandejaExpedientesNueva.this,
                                ex.getMessage() == null ? "No se pudo asociar los duplicados." : ex.getMessage(),
                                "Asociar duplicados",
                                JOptionPane.ERROR_MESSAGE);
                    } finally {
                        actualizarEstadoBoton();
                    }
                }
            };
            worker.execute();
        }

        private void generarNumeroExpediente() {
            if (expedientePrincipalDuplicados == null || !requiereDecisionNumero(expedientePrincipalDuplicados)) {
                return;
            }
            int confirmacion = JOptionPane.showConfirmDialog(
                    JPanelBandejaExpedientesNueva.this,
                    "Se generará un nuevo número de expediente para esta solicitud.\n"
                            + "Use esta opción solo si no corresponde asociarla a un expediente principal.\n\n"
                            + "¿Desea continuar?",
                    "Generar número de expediente",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
            final Long idExpediente = expedientePrincipalDuplicados.getIdExpediente();
            btnGenerarNumeroExpediente.setEnabled(false);
            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return asignacionExpedienteServiceRecepcion.generarNumeroExpediente(idExpediente);
                }

                @Override
                protected void done() {
                    try {
                        String numero = get();
                        refrescar();
                        JOptionPane.showMessageDialog(
                                JPanelBandejaExpedientesNueva.this,
                                "Número de expediente generado: " + numero,
                                "Generación de número",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                JPanelBandejaExpedientesNueva.this,
                                ex.getMessage() == null ? "No se pudo generar el número de expediente." : ex.getMessage(),
                                "Generación de número",
                                JOptionPane.ERROR_MESSAGE);
                    } finally {
                        actualizarDecisionNumero();
                    }
                }
            };
            worker.execute();
        }
    }

    private static final class ResolucionPrincipalDuplicados {
        private final Long idPrincipal;
        private final String textoPrincipal;
        private final List<Long> idsRelacionados;
        private final boolean ambiguo;

        private ResolucionPrincipalDuplicados(
                Long idPrincipal,
                String textoPrincipal,
                List<Long> idsRelacionados,
                boolean ambiguo) {
            this.idPrincipal = idPrincipal;
            this.textoPrincipal = textoPrincipal;
            this.idsRelacionados = idsRelacionados;
            this.ambiguo = ambiguo;
        }
    }

    private void configurarCabeceraSeleccionGrupoFamiliar() {
        if (!perfilRegistroRecepcion) {
            return;
        }
        table.getColumnModel().getColumn(COL_SELECCION_REGISTRO).setHeaderRenderer(new SelectAllHeaderRendererRegistro());
        SwingUtilities.invokeLater(() -> instalarListenerCabeceraSeleccionRegistro());
        actualizarEstadoHeaderSeleccionRegistro();
    }

    private void instalarListenerCabeceraSeleccionRegistro() {
        if (!perfilRegistroRecepcion) {
            return;
        }
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
                        if (table.convertColumnIndexToModel(viewColumn) == COL_SELECCION_REGISTRO) {
                            alternarSeleccionVisibleDesdeHeaderRegistro();
                        }
                        return;
                    }
                    offset += width;
                }
            }
        });
    }

    private void alternarSeleccionVisibleDesdeHeaderRegistro() {
        actualizarEstadoHeaderSeleccionRegistro();
        if (!hayVisiblesGrupoFamiliar) {
            return;
        }
        boolean seleccionar = !todasVisiblesGrupoFamiliarSeleccionadas;
        actualizandoGrupoFamiliarMasivo = true;
        try {
            for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                if (esFilaPrincipal(modelRow)) {
                    tableModel.setValueAt(Boolean.valueOf(seleccionar), modelRow, COL_SELECCION_REGISTRO);
                }
            }
        } finally {
            actualizandoGrupoFamiliarMasivo = false;
        }
        actualizarEstadoHeaderSeleccionRegistro();
        notificarCambioGrupoFamiliar();
    }

    private void actualizarEstadoHeaderSeleccionRegistro() {
        if (!perfilRegistroRecepcion) {
            return;
        }
        int visiblesSeleccionables = 0;
        int visiblesSeleccionadas = 0;
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            if (modelRow < 0 || modelRow >= tableModel.getRowCount() || !esFilaPrincipal(modelRow)) {
                continue;
            }
            visiblesSeleccionables++;
            if (Boolean.TRUE.equals(tableModel.getValueAt(modelRow, COL_SELECCION_REGISTRO))) {
                visiblesSeleccionadas++;
            }
        }
        hayVisiblesGrupoFamiliar = visiblesSeleccionables > 0;
        todasVisiblesGrupoFamiliarSeleccionadas = hayVisiblesGrupoFamiliar
                && visiblesSeleccionables == visiblesSeleccionadas;
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.repaint();
        }
        actualizarTextoSeleccionadosRegistro();
    }

    private void actualizarTextoSeleccionadosRegistro() {
        if (!perfilRegistroRecepcion) {
            return;
        }
        lblSeleccionados.setText(contarIdsGrupoFamiliarSeleccionados() + " expediente(s) seleccionado(s)");
    }

    private class ExpandirRendererRegistro extends JPanel implements TableCellRenderer {

        private final ExpandGlyphRegistro glyph = new ExpandGlyphRegistro();

        private ExpandirRendererRegistro() {
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
            RegistroTableRow fila = filaRegistro(modelRow);
            Color background = colorFondoFilaRegistro(row, fila, isSelected);
            setBackground(background);
            if (fila != null && fila.esAsociada()) {
                Color acento = acentoGrupoRegistro(fila.getIdPrincipal());
                setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, acento));
                glyph.configure(ExpandGlyphRegistro.NONE, acento, background);
                setToolTipText("Expediente asociado al principal.");
                return this;
            }
            setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            if (fila != null
                    && fila.esPrincipal()
                    && fila.principal.getIdExpediente() != null
                    && fila.principal.getRelacionesConfirmadasComoPrincipal() > 0) {
                Long idPrincipal = fila.principal.getIdExpediente();
                int state = principalesCargando.contains(idPrincipal)
                        ? ExpandGlyphRegistro.LOADING
                        : (principalesExpandidos.contains(idPrincipal) ? ExpandGlyphRegistro.COLLAPSE : ExpandGlyphRegistro.EXPAND);
                glyph.configure(state, GRID_ACTION_ICON_BLUE, background);
                setToolTipText(state == ExpandGlyphRegistro.COLLAPSE
                        ? "Ocultar expedientes asociados"
                        : "Ver expedientes asociados");
            } else {
                glyph.configure(ExpandGlyphRegistro.NONE, AppV2Theme.TEXT_SECONDARY, background);
                setToolTipText(null);
            }
            return this;
        }
    }

    private static final class ExpandGlyphRegistro extends JPanel {

        private static final int NONE = 0;
        private static final int EXPAND = 1;
        private static final int COLLAPSE = 2;
        private static final int LOADING = 3;
        private int state = NONE;
        private Color accent = AppV2Theme.TEAL;

        private ExpandGlyphRegistro() {
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
                    g2.setColor(accent);
                    int start = cx - 8;
                    for (int i = 0; i < 3; i++) {
                        g2.fillOval(start + i * 7, cy - 2, 4, 4);
                    }
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
    }

    private class SeleccionRendererRegistro extends JCheckBox implements TableCellRenderer {

        private final DocumentoAsociadoCellRegistro documentoAsociadoCell = new DocumentoAsociadoCellRegistro();

        private SeleccionRendererRegistro() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
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
            RegistroTableRow fila = filaRegistro(modelRow);
            Color background = colorFondoFilaRegistro(row, fila, isSelected);
            setBackground(background);
            if (fila != null && fila.esAsociada()) {
                documentoAsociadoCell.configure(
                        GRID_ACTION_ICON_BLUE,
                        background,
                        bordeContenidoAsociadoRegistro(modelRow, 8, 8));
                documentoAsociadoCell.setToolTipText("Expediente asociado al principal; no requiere selección independiente.");
                return documentoAsociadoCell;
            }
            if (fila == null || !fila.esPrincipal()) {
                setSelected(false);
                setEnabled(false);
                setText("");
                setForeground(AppV2Theme.TEXT_SECONDARY);
                return this;
            }
            setEnabled(true);
            setSelected(Boolean.TRUE.equals(tableModel.getValueAt(modelRow, COL_SELECCION_REGISTRO)));
            setText("");
            setForeground(AppV2Theme.TEXT_PRIMARY);
            return this;
        }
    }

    private static final class DocumentoAsociadoCellRegistro extends JPanel {

        private Color accent = AppV2Theme.TEAL;

        private DocumentoAsociadoCellRegistro() {
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

    private Color colorFondoFilaRegistro(int viewRow, RegistroTableRow fila, boolean isSelected) {
        if (fila != null && fila.esAsociada()) {
            return isSelected ? TABLE_SELECTION_BACKGROUND : ASSOCIATED_ROW_BACKGROUND;
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
        return viewRow % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT;
    }

    private Color acentoGrupoRegistro(Long idExpedientePrincipal) {
        long value = idExpedientePrincipal == null ? 0L : idExpedientePrincipal.longValue();
        int indice = (int) Math.abs(value % GROUP_STRIPE_COLORS.length);
        return GROUP_STRIPE_COLORS[indice];
    }

    private boolean esPrimerAsociadoRegistro(int modelRow) {
        if (modelRow < 0 || modelRow >= filasTabla.size() || !filasTabla.get(modelRow).esAsociada()) {
            return false;
        }
        return modelRow == 0 || !filasTabla.get(modelRow - 1).esAsociada();
    }

    private boolean esUltimoAsociadoRegistro(int modelRow) {
        if (modelRow < 0 || modelRow >= filasTabla.size() || !filasTabla.get(modelRow).esAsociada()) {
            return false;
        }
        return modelRow == filasTabla.size() - 1 || !filasTabla.get(modelRow + 1).esAsociada();
    }

    private javax.swing.border.Border bordeContenidoAsociadoRegistro(int modelRow, int left, int right) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        esPrimerAsociadoRegistro(modelRow) ? 1 : 0,
                        0,
                        esUltimoAsociadoRegistro(modelRow) ? 1 : 0,
                        0,
                        ASSOCIATED_BLOCK_BORDER),
                BorderFactory.createEmptyBorder(0, left, 0, right));
    }

    private static final class RegistroTableRow {

        private final ExpedienteBandejaDTO principal;
        private final ExpedienteRelacionadoDTO asociado;
        private final Long idExpedientePrincipal;

        private RegistroTableRow(ExpedienteBandejaDTO principal, ExpedienteRelacionadoDTO asociado) {
            this.principal = principal;
            this.asociado = asociado;
            this.idExpedientePrincipal = principal == null ? null : principal.getIdExpediente();
        }

        private static RegistroTableRow principal(ExpedienteBandejaDTO principal) {
            return new RegistroTableRow(principal, null);
        }

        private static RegistroTableRow asociada(ExpedienteBandejaDTO principal, ExpedienteRelacionadoDTO asociado) {
            return new RegistroTableRow(principal, asociado);
        }

        private boolean esPrincipal() {
            return principal != null && asociado == null;
        }

        private boolean esAsociada() {
            return asociado != null;
        }

        private Long getIdPrincipal() {
            return idExpedientePrincipal;
        }
    }

    private class SelectAllHeaderRendererRegistro extends JLabel implements TableCellRenderer {

        private SelectAllHeaderRendererRegistro() {
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
            setEnabled(hayVisiblesGrupoFamiliar);
            setText("");
            setIcon(new HeaderCheckBoxIconRegistro(
                    todasVisiblesGrupoFamiliarSeleccionadas,
                    hayVisiblesGrupoFamiliar));
            setToolTipText(hayVisiblesGrupoFamiliar
                    ? (todasVisiblesGrupoFamiliarSeleccionadas
                            ? "Desmarcar todos los expedientes filtrados."
                            : "Seleccionar todos los expedientes filtrados.")
                    : "No hay expedientes visibles para seleccionar.");
            return this;
        }
    }

    private static class HeaderCheckBoxIconRegistro implements javax.swing.Icon {

        private final boolean selected;
        private final boolean enabled;

        private HeaderCheckBoxIconRegistro(boolean selected, boolean enabled) {
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

    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void setBuscando(boolean buscando) {
        btnBuscar.setEnabled(!buscando);
        btnLimpiar.setEnabled(!buscando);
        lblResultado.setText(buscando ? "Consultando bandeja de SDRERC_APP..." : lblResultado.getText());
    }

    private void mostrarError(Exception ex) {
        String message = ex.getMessage();
        if (message == null && ex.getCause() != null) {
            message = ex.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(
                this,
                message == null ? "No se pudo consultar la bandeja. Revise la conexión de solo lectura a SDRERC_APP." : message,
                "Error de consulta",
                JOptionPane.ERROR_MESSAGE);
        lblResultado.setText("No se pudo consultar SDRERC_APP. La bandeja no realizó cambios en BD.");
    }

    private void mostrarError(String titulo, Exception ex) {
        String message = ex.getMessage();
        if (message == null && ex.getCause() != null) {
            message = ex.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(
                this,
                message == null ? titulo : message,
                titulo,
                JOptionPane.ERROR_MESSAGE);
    }

    private static String codigoSeleccionado(JComboBox<FiltroCatalogoItemV2> combo) {
        Object selected = combo.getSelectedItem();
        if (selected instanceof FiltroCatalogoItemV2) {
            FiltroCatalogoItemV2 item = (FiltroCatalogoItemV2) selected;
            return item.hasCodigo() ? item.getCodigo() : null;
        }
        return null;
    }

    private static LocalDate fechaSeleccionada(PremiumDateFieldV2 field) {
        if (field == null) {
            return null;
        }
        Date date = field.getDate();
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void restaurarFechasRegistro() {
        fechaSolicitudDesde.setDate(DateRangePickerSupport.defaultSearchFromDate());
        fechaSolicitudHasta.setDate(DateRangePickerSupport.defaultSearchToDate());
    }

    private void restaurarFechasBandejaGeneral() {
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        Date desde = Date.from(inicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date hasta = new Date();
        fechaSolicitudDesde.setDate(desde);
        fechaSolicitudHasta.setDate(hasta);
    }

    private void seleccionarEtapaInicial() {
        if (etapaInicial == null) {
            return;
        }
        for (int i = 0; i < cmbEtapa.getItemCount(); i++) {
            FiltroCatalogoItemV2 item = cmbEtapa.getItemAt(i);
            if (etapaInicial.equals(item.getCodigo())) {
                cmbEtapa.setSelectedIndex(i);
                return;
            }
        }
    }

    private static String normalizar(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private static String textoConDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    private static FiltroCatalogoItemV2[] crearItemsEtapa() {
        return new FiltroCatalogoItemV2[]{
            new FiltroCatalogoItemV2(null, "Todas"),
            new FiltroCatalogoItemV2("REGISTRO", "Registro"),
            new FiltroCatalogoItemV2("ASIGNACION", "Asignación"),
            new FiltroCatalogoItemV2("ANALISIS", "Análisis"),
            new FiltroCatalogoItemV2("VERIFICACION", "Verificación"),
            new FiltroCatalogoItemV2("FIRMA_EMISION", "Verificación / documento emitido"),
            new FiltroCatalogoItemV2("EJECUCION", "Ejecución"),
            new FiltroCatalogoItemV2("NOTIFICACION", "Notificación"),
            new FiltroCatalogoItemV2("PUBLICACION_CONDICIONAL", "Publicación"),
            new FiltroCatalogoItemV2("EXPEDIENTE_DIGITAL", "Expediente digital"),
            new FiltroCatalogoItemV2("CIERRE_ARCHIVO", "Cierre / Archivo")
        };
    }

    private static FiltroCatalogoItemV2[] crearItemsEstado() {
        return new FiltroCatalogoItemV2[]{
            new FiltroCatalogoItemV2(null, "Todos"),
            new FiltroCatalogoItemV2("REGISTRADO", "Registrado"),
            new FiltroCatalogoItemV2("ASIGNADO", "Asignado"),
            new FiltroCatalogoItemV2("RECIBIDO_POR_ABOGADO", "Recibido por abogado"),
            new FiltroCatalogoItemV2("ATENDIDO", "Atendido"),
            new FiltroCatalogoItemV2("OBSERVADO", "Observado"),
            new FiltroCatalogoItemV2("SUBSANADO", "Subsanado"),
            new FiltroCatalogoItemV2("EN_VERIFICACION", "En verificación"),
            new FiltroCatalogoItemV2("REQUIERE_CORRECCION", "Requiere corrección"),
            new FiltroCatalogoItemV2("DOCUMENTO_INCONSISTENTE", "Documento inconsistente"),
            new FiltroCatalogoItemV2("VERIFICADO", "Verificado"),
            new FiltroCatalogoItemV2("PARA_FIRMA", "Pendiente de emisión"),
            new FiltroCatalogoItemV2("FIRMADO", "Documento validado"),
            new FiltroCatalogoItemV2("EMITIDO", "Emitido"),
            new FiltroCatalogoItemV2("RESOLUCION_NUMERADA", "Resolución numerada"),
            new FiltroCatalogoItemV2("EN_EJECUCION", "En ejecución"),
            new FiltroCatalogoItemV2("EJECUTADO", "Ejecutado"),
            new FiltroCatalogoItemV2("EN_NOTIFICACION", "En notificación"),
            new FiltroCatalogoItemV2("CARGO_PENDIENTE", "Cargo pendiente"),
            new FiltroCatalogoItemV2("CARGO_RECIBIDO", "Cargo recibido"),
            new FiltroCatalogoItemV2("NOTIFICADO", "Notificado"),
            new FiltroCatalogoItemV2("REQUIERE_PUBLICACION", "Requiere publicación"),
            new FiltroCatalogoItemV2("PENDIENTE_PUBLICACION", "Pendiente publicación"),
            new FiltroCatalogoItemV2("PUBLICACION_REGISTRADA", "Publicación registrada"),
            new FiltroCatalogoItemV2("CERRADO", "Cerrado"),
            new FiltroCatalogoItemV2("ARCHIVADO", "Archivado")
        };
    }

    private void cargarEstadosRegistroRecepcion() {
        cmbEstado.removeAllItems();
        cmbEstado.addItem(new FiltroCatalogoItemV2(null, "Todos los estados"));
        cmbEstado.addItem(new FiltroCatalogoItemV2("REGISTRADO", "Registrado"));
        cmbEstado.setSelectedIndex(0);
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : formatDate(dateTime.toLocalDate());
    }

    private static String formatDate(LocalDate value) {
        return value == null ? "" : DATE_FORMAT.format(value);
    }

    private static void actualizarBadgeDias(BadgeV2 badge, Long dias) {
        if (badge == null) {
            return;
        }
        if (dias == null) {
            badge.setText("-");
            badge.setBackground(AppV2Theme.SOFT_GRAY);
            badge.setForeground(AppV2Theme.MUTED);
            badge.setToolTipText("Sin días calculados");
            return;
        }
        PlazoVisualSupportV2.Nivel nivel = PlazoVisualSupportV2.clasificarDias(dias);
        badge.setText(String.valueOf(dias));
        badge.setBackground(PlazoVisualSupportV2.backgroundFor(nivel));
        badge.setForeground(PlazoVisualSupportV2.foregroundFor(nivel));
        String tooltip;
        switch (nivel) {
            case VENCIDO:
                tooltip = "Vencido. Días hábiles restantes: " + dias;
                break;
            case ROJO:
                tooltip = dias.longValue() == 0L
                        ? "Vence hoy. Días hábiles restantes: 0"
                        : "Días hábiles restantes: " + dias;
                break;
            default:
                tooltip = "Días hábiles restantes: " + dias;
                break;
        }
        badge.setToolTipText(tooltip);
    }

    private static LocalDate fechaSolicitud(ExpedienteBandejaDTO item) {
        if (item.getFechaRecepcion() != null) {
            return item.getFechaRecepcion();
        }
        if (item.getFechaRegistro() != null) {
            return item.getFechaRegistro().toLocalDate();
        }
        return null;
    }

    private static String valorFiltro(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDate) {
            return formatDate((LocalDate) value);
        }
        if (value instanceof LocalDateTime) {
            return formatDateTime((LocalDateTime) value);
        }
        return DisplayNameMapperV2.valor(value.toString());
    }

    private static String valorUi(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return DisplayNameMapperV2.valor(value);
    }

    private static String normalizarFiltro(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        return normalized.replaceAll("\\s+", " ");
    }

    private static String toolTipGrupoFamiliar(ExpedienteConsolaDTO expediente) {
        if (expediente == null || !expediente.tieneIndicadorGrupoFamiliar()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(expediente.isGrupoFamiliar()
                ? "Grupo familiar identificado"
                : "Posible grupo familiar");
        String criterio = criterioGrupoFamiliar(expediente.getCriterioGrupoFamiliar());
        if (!criterio.isEmpty()) {
            sb.append(" · ").append(criterio);
        }
        if (expediente.getObservacionGrupoFamiliar() != null
                && !expediente.getObservacionGrupoFamiliar().trim().isEmpty()) {
            sb.append(" · ").append(expediente.getObservacionGrupoFamiliar().trim());
        }
        return sb.toString();
    }

    private static String extraerTipoDocumentoPersona(String valor) {
        if (valor == null) {
            return "";
        }
        String normalizado = valor.trim().replaceAll("[\\./-]+", " ").replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        normalizado = normalizado.replace("C E", "CE");
        if (normalizado.isEmpty()) {
            return "";
        }
        String[] partes = normalizado.split(" ");
        if (partes.length == 0) {
            return "";
        }
        String primerToken = partes[0];
        String tipoCanonico = tipoDocumentoCanonico(primerToken);
        if (tipoCanonico != null) {
            return tipoCanonico;
        }
        if (esNumeroDocumentoProbable(primerToken)) {
            return "";
        }
        return valor.trim();
    }

    private static String extraerNumeroDocumentoPersona(String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = valor.trim().replaceAll("[\\./-]+", " ").replaceAll("\\s+", " ");
        limpio = limpio.replace("C E", "CE");
        if (limpio.isEmpty()) {
            return "";
        }
        if (limpio.toUpperCase(Locale.ROOT).startsWith("SIN DNI")) {
            return "";
        }
        String[] partes = limpio.split(" ");
        if (partes.length <= 1) {
            return esTipoDocumentoPersona(partes[0]) ? "" : partes[0];
        }
        String primerToken = partes[0].toUpperCase(Locale.ROOT);
        if (esTipoDocumentoPersona(primerToken)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < partes.length; i++) {
                if (partes[i] == null || partes[i].trim().isEmpty()) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(partes[i].trim());
            }
            return sb.toString();
        }
        return valor.trim();
    }

    private static String tipoDocumentoCanonico(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim().replaceAll("[\\./-]+", " ").replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        normalizado = normalizado.replace("C E", "CE");
        if (normalizado.isEmpty()) {
            return null;
        }
        if ("DNI".equals(normalizado)) {
            return "DNI";
        }
        if ("CE".equals(normalizado)) {
            return "CE";
        }
        if ("PASAPORTE".equals(normalizado) || "PASSAPORTE".equals(normalizado)) {
            return "Pasaporte";
        }
        if ("SIN DNI".equals(normalizado)) {
            return "SIN DNI";
        }
        return null;
    }

    private static boolean esTipoDocumentoPersona(String valor) {
        return tipoDocumentoCanonico(valor) != null;
    }

    private static boolean esNumeroDocumentoProbable(String valor) {
        if (valor == null) {
            return false;
        }
        String normalizado = valor.trim();
        return normalizado.matches("[0-9A-Z]{6,15}");
    }

    private static String criterioGrupoFamiliar(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            return "";
        }
        String normalized = criterio.trim().toUpperCase(Locale.ROOT);
        if ("MANUAL".equals(normalized)) {
            return "Marcado manual";
        }
        if ("EXCEL".equals(normalized)) {
            return "Informado desde Excel";
        }
        if ("COINCIDENCIA_APELLIDOS_EXCEL".equals(normalized)) {
            return "Coincidencia de apellidos en carga";
        }
        if ("COINCIDENCIA_APELLIDOS_BD".equals(normalized)) {
            return "Coincidencia de apellidos con solicitud existente";
        }
        return DisplayNameMapperV2.valor(criterio);
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

    private static JLabel valueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        label.setForeground(AppV2Theme.TEXT_PRIMARY);
        label.setToolTipText(null);
        return label;
    }

    private static void setValue(JLabel label, String value) {
        String safeValue = safe(value);
        label.setText(safeValue);
        label.setToolTipText("-".equals(safeValue) ? null : safeValue);
        label.setForeground(AppV2Theme.TEXT_PRIMARY);
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private static String mensajeError(Exception ex) {
        String message = ex.getMessage();
        if (message == null && ex.getCause() != null) {
            message = ex.getCause().getMessage();
        }
        return message == null ? "No se pudo cargar el panel de recepción." : message;
    }

    private static Color colorPlazo(Long dias) {
        return PlazoVisualSupportV2.foregroundFor(PlazoVisualSupportV2.clasificarDias(dias));
    }

    private static String descripcionPlazo(Long dias, LocalDate fechaVencimiento) {
        String fecha = fechaVencimiento == null ? "sin fecha configurada" : DATE_FORMAT.format(fechaVencimiento);
        if (dias == null) {
            return "Sin plazo de vencimiento (" + fecha + ")";
        }
        if (dias < 0) {
            return "Vencido hace " + Math.abs(dias) + " día(s) hábil(es). Vencimiento: " + fecha;
        }
        if (dias == 0) {
            return "Vence hoy. Vencimiento: " + fecha;
        }
        return dias + " día(s) hábil(es) restantes. Vencimiento: " + fecha;
    }

    private class BandejaCellRenderer extends DefaultTableCellRenderer {
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
            RegistroTableRow fila = filaRegistro(modelRow);
            boolean filaAsociada = fila != null && fila.esAsociada();
            if (perfilRegistroRecepcion) {
                if (modelColumn == COL_DIAS_REGISTRO) {
                    if (filaAsociada) {
                        Component c = super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
                        c.setBackground(colorFondoFilaRegistro(row, fila, isSelected));
                        c.setForeground(AppV2Theme.TEXT_SECONDARY);
                        setBorder(bordeContenidoAsociadoRegistro(modelRow, 8, 8));
                        setToolTipText("La alerta de días aplica al expediente principal.");
                        return c;
                    }
                    return StatusBadgeV2.forDias(value, colorFondoCelda(row, isSelected));
                }
                if (!isSelected && modelColumn == indiceColumnaRegistroRecepcionEstado()) {
                    return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
                }
                if (!isSelected && modelColumn == indiceColumnaRegistroRecepcionAlertas()) {
                    String text = value == null ? "" : value.toString();
                    if (!text.startsWith("Sin")) {
                        Color bg = filaAsociada ? ASSOCIATED_ROW_BACKGROUND : AppV2Theme.SOFT_ORANGE;
                        Color fg = filaAsociada ? AppV2Theme.TEXT_SECONDARY : AppV2Theme.WARNING;
                        return new BadgeV2(text, bg, fg);
                    }
                }
                return defaultComponent(table, value, isSelected, hasFocus, row, column, fila, modelColumn, modelRow);
            }
            if (modelColumn == COL_DIAS_BANDEJA) {
                if (filaAsociada) {
                    Component c = super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
                    c.setBackground(colorFondoFilaRegistro(row, fila, isSelected));
                    c.setForeground(AppV2Theme.TEXT_SECONDARY);
                    setBorder(bordeContenidoAsociadoRegistro(modelRow, 8, 8));
                    setToolTipText("La alerta de días aplica al expediente principal.");
                    return c;
                }
                return StatusBadgeV2.forDias(value, colorFondoCelda(row, isSelected));
            }
            if (!isSelected && modelColumn == COL_ETAPA_BANDEJA) {
                return StatusBadgeV2.forEtapa(value == null ? "" : value.toString());
            }
            if (!isSelected && modelColumn == COL_ESTADO_BANDEJA) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
            }
            if (!isSelected && !filaAsociada && modelColumn == COL_PUBLICACION_BANDEJA) {
                String text = value == null ? "" : value.toString();
                if ("Requiere".equalsIgnoreCase(text)) {
                    return new BadgeV2("Requiere", AppV2Theme.SOFT_ORANGE, AppV2Theme.WARNING);
                }
                return new BadgeV2("No", AppV2Theme.SOFT_GRAY, AppV2Theme.TEXT_SECONDARY);
            }
            if (!isSelected && !filaAsociada && modelColumn == COL_DIGITAL_BANDEJA) {
                String text = value == null ? "" : value.toString();
                if ("Completo".equalsIgnoreCase(text)) {
                    return new BadgeV2("Completo", AppV2Theme.SOFT_GREEN, AppV2Theme.SUCCESS);
                }
                return new BadgeV2("Pendiente", AppV2Theme.SOFT_GRAY, AppV2Theme.TEXT_SECONDARY);
            }

            return defaultComponent(table, value, isSelected, hasFocus, row, column, fila, modelColumn, modelRow);
        }

        private Color colorFondoCelda(int row, boolean isSelected) {
            if (isSelected) {
                return table.getSelectionBackground();
            }
            return row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT;
        }

        private Component defaultComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column,
                RegistroTableRow fila,
                int modelColumn,
                int modelRow) {
            String display = valorFiltro(value);
            Component c = super.getTableCellRendererComponent(table, display, isSelected, hasFocus, row, column);
            boolean filaAsociada = fila != null && fila.esAsociada();
            boolean esColumnaExpediente = modelColumn == COL_NUMERO_EXPEDIENTE_REGISTRO || modelColumn == COL_EXPEDIENTE_BANDEJA;
            setFont(filaAsociada && !esColumnaExpediente
                    ? AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL)
                    : AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            setBorder(filaAsociada
                    ? bordeContenidoAsociadoRegistro(
                            modelRow,
                            esColumnaExpediente ? ASSOCIATED_EXPEDIENTE_INDENT : 8,
                            8)
                    : BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setToolTipText(display == null || display.trim().isEmpty() ? null : display);
            if (filaAsociada) {
                c.setBackground(colorFondoFilaRegistro(row, fila, isSelected));
                c.setForeground(esColumnaExpediente
                        ? AppV2Theme.TEXT_PRIMARY
                        : AppV2Theme.TEXT_SECONDARY);
            } else if (!isSelected) {
                c.setBackground(row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
                c.setForeground(AppV2Theme.TEXT_PRIMARY);
            } else {
                c.setBackground(colorFondoFilaRegistro(row, fila, true));
                c.setForeground(AppV2Theme.TEXT_PRIMARY);
            }
            return c;
        }
    }

    private int indiceColumnaRegistroRecepcionEstado() {
        if (!(tableModel instanceof DefaultTableModel)) {
            return -1;
        }
        return ((DefaultTableModel) tableModel).findColumn("Estado");
    }

    private int indiceColumnaRegistroRecepcionAlertas() {
        if (!(tableModel instanceof DefaultTableModel)) {
            return -1;
        }
        return ((DefaultTableModel) tableModel).findColumn("Alertas");
    }
}
