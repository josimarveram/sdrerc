package com.sdrerc.ui.views.asignacion;

import com.sdrerc.application.sdrercapp.AsignacionExpedienteService;
import com.sdrerc.application.sdrercapp.ExpedienteRelacionadoDeteccionService;
import com.sdrerc.application.sdrercapp.ExpedienteRelacionadoService;
import com.sdrerc.application.sdrercapp.UsuarioAsignacionService;
import com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.AsignacionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO;
import com.sdrerc.ui.appv2.components.AppV2ActionPanel;
import com.sdrerc.ui.appv2.components.AppV2ContextChip;
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
import com.sdrerc.ui.appv2.helpers.FiltroCatalogoItemV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import com.sdrerc.ui.views.expedienteconsola.DlgConsolaExpedienteV2;
import com.sdrerc.shared.session.SessionContext;
import com.sdrerc.util.DateRangePickerSupport;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
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
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
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
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class JPanelAsignacionV2 extends JPanel {

    private static final int COL_EXPANDIR = 0;
    private static final int COL_SELECCION = 1;
    private static final int COL_DIAS = 2;
    private static final int COL_EXPEDIENTE = 3;
    private static final int COL_ESTADO = 11;
    private static final int COL_RELACIONADOS = 12;
    private static final int COL_ID = 13;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color[] FOCUS_ACCENTS = new Color[]{
        AppV2Theme.TEAL,
        AppV2Theme.INFO,
        AppV2Theme.INDIGO,
        AppV2Theme.SUCCESS,
        AppV2Theme.WARNING,
        new Color(112, 96, 160)
    };
    private static final Color[] FOCUS_BACKGROUNDS = new Color[]{
        new Color(228, 246, 246),
        AppV2Theme.SOFT_BLUE,
        new Color(238, 240, 250),
        AppV2Theme.SOFT_GREEN,
        AppV2Theme.SOFT_ORANGE,
        new Color(243, 240, 249)
    };

    private final AsignacionExpedienteService asignacionService;
    private final UsuarioAsignacionService usuarioService;
    private final ExpedienteRelacionadoDeteccionService relacionadoDeteccionService = new ExpedienteRelacionadoDeteccionService();
    private final ExpedienteRelacionadoService relacionadoService = new ExpedienteRelacionadoService();
    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite, titular, acta o documento", 28);
    private final PremiumDateFieldV2 fechaSolicitudDesde = new PremiumDateFieldV2();
    private final PremiumDateFieldV2 fechaSolicitudHasta = new PremiumDateFieldV2();
    private final JComboBox<FiltroCatalogoItemV2> cmbEstado = new JComboBox<FiltroCatalogoItemV2>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnVerDetalle = new JButton("Ver detalle");
    private final JButton btnVerRelacionados = new JButton("Ver relacionados");
    private final JButton btnAsociarRelacionados = new JButton("Asociar relacionados");
    private final JButton btnSeleccionarVisibles = new JButton("Seleccionar visibles");
    private final JButton btnLimpiarSeleccion = new JButton("Limpiar selección");
    private final JButton btnAsignarSeleccionado = new JButton("Asignar expediente");
    private final JButton btnAsignarSeleccionados = new JButton("Asignar seleccionados");
    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar expedientes.");
    private final JLabel lblSeleccionados = new JLabel("0 expedientes seleccionados");
    private final JLabel lblSeleccionadosPanel = new JLabel("0 expedientes seleccionados");
    private final JLabel lblExpedienteSeleccionado = new JLabel("-");
    private final JLabel lblTramiteWebSeleccionado = new JLabel("-");
    private final JLabel lblNumeroDocumentoSeleccionado = new JLabel("-");
    private final JLabel lblRecepcionAbogado = new JLabel("-");
    private final JLabel lblOrigen = new JLabel("Registro / Registrado");
    private final JLabel lblDestino = new JLabel("Asignación / Asignado");
    private final JLabel lblIngreso = new JLabel("Normal");
    private final JLabel lblSupervisor = new JLabel("-");
    private final JLabel lblRelacionados = new JLabel("Sin alerta de relacionados.");
    private final DefaultTableModel documentosRelacionadosModel = new DefaultTableModel(
            new Object[]{"N° documento", ""}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1 && puedeRecibirDocumentoRelacionado();
        }
    };
    private final JTable documentosRelacionadosTable = new AppV2Table(documentosRelacionadosModel);
    private final JComboBox<EquipoItem> cmbEquipo = new JComboBox<EquipoItem>();
    private final JComboBox<UsuarioItem> cmbAbogado = new JComboBox<UsuarioItem>();
    private final JTextArea txtComentario = new JTextArea(3, 18);
    private final AppV2ContextChip chipPanelAsignacion = new AppV2ContextChip("Expandir");
    private final AsignacionTableModel tableModel = new AsignacionTableModel();
    private final JTable table = new AppV2Table(tableModel);
    private final AppV2TablePanel tablePanel = new AppV2TablePanel(
            table,
            "Sin expedientes para mostrar",
            "Seleccione filtros y presione Buscar.");
    private final JPanel panelOperativo = new JPanel(new BorderLayout(14, 14));
    private final List<AsignacionExpedienteDTO> expedientes = new ArrayList<>();
    private final List<AsignacionTableRow> filasTabla = new ArrayList<>();
    private final Map<Long, List<ExpedienteRelacionadoDTO>> asociadosCache = new HashMap<>();
    private final Set<Long> principalesExpandidos = new HashSet<>();
    private final Set<Long> principalesCargando = new HashSet<>();
    private final List<ExpedienteRelacionadoDTO> documentosRelacionadosPanel = new ArrayList<>();
    private final MetricCardV2 cardPendientes = new MetricCardV2("Resultados", "0", "Según filtros", AppV2Theme.INFO);
    private final MetricCardV2 cardSeleccionados = new MetricCardV2("Seleccionados", "0", "Listos para asignación", AppV2Theme.TEAL);
    private final MetricCardV2 cardRelacionados = new MetricCardV2("Alertas", "0", "Posibles relacionados", AppV2Theme.WARNING);
    private AppV2SideActionPanel panelAsignacion;
    private AppV2OperationalSplitPanel splitOperativo;
    private Color acentoSeleccion = AppV2Theme.TEAL;
    private Color fondoSeleccion = AppV2Theme.SOFT_BLUE;
    private Long idExpedienteGrupoEnFoco;
    private String contextoChip = "Panel de asignación";
    private boolean panelAsignacionVisible;
    private boolean panelAsignacionCerradoPorUsuario;
    private boolean todasVisiblesSeleccionadas;
    private boolean busquedaInicialEjecutada;
    private Long idExpedienteDocumentosRelacionados;

    private boolean cargandoCombos;
    private boolean actualizandoSeleccion;
    private boolean usuarioActualResuelto;
    private Long idUsuarioActualSdrercApp;

    public JPanelAsignacionV2() {
        this(new AsignacionExpedienteService(), new UsuarioAsignacionService());
    }

    public JPanelAsignacionV2(AsignacionExpedienteService asignacionService, UsuarioAsignacionService usuarioService) {
        this.asignacionService = asignacionService;
        this.usuarioService = usuarioService;
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());
        add(crearHeader(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        configurarTabla();
        configurarTablaDocumentosRelacionados();
        configurarEventos();
        restaurarFechasBusqueda();
        cargarEstados();
        cargarEquipos();
        actualizarPanelSeleccion();
    }

    private JPanel crearHeader() {
        JPanel metricas = new JPanel(new GridLayout(1, 3, 12, 0));
        metricas.setOpaque(false);
        metricas.add(cardPendientes);
        metricas.add(cardSeleccionados);
        metricas.add(cardRelacionados);
        return metricas;
    }

    private JPanel crearCentro() {
        JPanel centro = new JPanel(new BorderLayout(14, 14));
        centro.setOpaque(false);
        centro.add(crearBuscador(), BorderLayout.NORTH);

        panelOperativo.setOpaque(false);
        JPanel bandeja = crearBandeja();
        panelAsignacion = crearPanelAsignacion();
        splitOperativo = new AppV2OperationalSplitPanel(bandeja, panelAsignacion, 0, 380, 430);
        panelOperativo.add(splitOperativo, BorderLayout.CENTER);

        centro.add(panelOperativo, BorderLayout.CENTER);
        return centro;
    }

    private JPanel crearBuscador() {
        configurarControles();
        AppV2SearchToolbar toolbar = new AppV2SearchToolbar();
        JPanel acciones = AppV2ActionPanel.right();
        acciones.add(btnBuscar);
        acciones.add(btnLimpiar);
        acciones.add(btnVerDetalle);
        acciones.add(btnVerRelacionados);
        toolbar.addSearchRow("Búsqueda", txtBusqueda, acciones);
        toolbar.addFilter("Fecha desde", fechaSolicitudDesde);
        toolbar.addFilter("Fecha hasta", fechaSolicitudHasta);
        toolbar.addFilter("Estado", cmbEstado);
        toolbar.addFilter("Mostrar", spnLimite);
        return toolbar;
    }

    private JPanel crearBandeja() {
        JPanel seleccion = AppV2ActionPanel.left();
        seleccion.add(btnSeleccionarVisibles);
        seleccion.add(btnLimpiarSeleccion);
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

    private AppV2SideActionPanel crearPanelAsignacion() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de asignación", new Runnable() {
            @Override
            public void run() {
                cerrarPanelAsignacion();
            }
        });
        chipPanelAsignacion.setExpanded(false);
        chipPanelAsignacion.setToolTipText("Ampliar el Panel de asignación");
        chipPanelAsignacion.addActionListener(e -> alternarExpansionPanelAsignacion());
        panel.setHeaderLeadingComponent(chipPanelAsignacion);
        panel.addSection(crearResumenAsignacion());
        panel.addSection(crearAccionesRelacionados());
        panel.addSection(crearFlujoAsignacion());
        panel.addSection(crearDestinoAsignacion());
        panel.addSection(crearComentarioAsignacion());
        panel.setFooter(crearAccionesAsignacion());
        return panel;
    }

    private JPanel crearResumenAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Expediente seleccionado");
        section.addRow("Seleccionados", lblSeleccionadosPanel);
        section.addRow("Expediente", lblExpedienteSeleccionado);
        section.addRow("Trámite Web", lblTramiteWebSeleccionado);
        section.addRow("N° Documento", lblNumeroDocumentoSeleccionado);
        section.addRow("Recepción", lblRecepcionAbogado);
        section.addRow("Alertas", crearPanelDocumentosRelacionados());
        return section;
    }

    private JPanel crearPanelDocumentosRelacionados() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setOpaque(false);
        lblRelacionados.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblRelacionados.setForeground(AppV2Theme.TEXT_SECONDARY);
        panel.add(lblRelacionados, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(documentosRelacionadosTable);
        scroll.setPreferredSize(new Dimension(270, 104));
        scroll.setMinimumSize(new Dimension(240, 84));
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearAccionesRelacionados() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Asociación rápida");
        btnAsociarRelacionados.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnAsociarRelacionados.setEnabled(false);
        btnAsociarRelacionados.setToolTipText("Asociar expedientes con el mismo número de acta y titular.");
        section.addRow("Acción", btnAsociarRelacionados);
        return section;
    }

    private JPanel crearFlujoAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Flujo operativo");
        section.addRow("Origen", lblOrigen);
        section.addRow("Destino", lblDestino);
        section.addRow("Ingreso", lblIngreso);
        return section;
    }

    private JPanel crearDestinoAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Destino operativo");
        section.addRow("Equipo destino", cmbEquipo);
        section.addRow("Abogado responsable", cmbAbogado);
        section.addRow("Supervisor", lblSupervisor);
        return section;
    }

    private JPanel crearComentarioAsignacion() {
        AppV2SideSectionPanel section = new AppV2SideSectionPanel("Comentario");
        section.addRow("Comentario", scrollComentario());
        return section;
    }

    private JPanel crearAccionesAsignacion() {
        JPanel acciones = new JPanel(new GridLayout(1, 2, 8, 0));
        acciones.setOpaque(false);
        acciones.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        acciones.add(btnAsignarSeleccionado);
        acciones.add(btnAsignarSeleccionados);
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
        Dimension fechaSize = new Dimension(170, 40);
        fechaSolicitudDesde.setPreferredSize(fechaSize);
        fechaSolicitudDesde.setMinimumSize(new Dimension(150, 40));
        fechaSolicitudHasta.setPreferredSize(fechaSize);
        fechaSolicitudHasta.setMinimumSize(new Dimension(150, 40));
        cmbEstado.setPreferredSize(new Dimension(240, 34));
        cmbEstado.setMinimumSize(new Dimension(190, 34));
        spnLimite.setPreferredSize(new Dimension(88, 34));
        cmbEquipo.setPreferredSize(new Dimension(230, 34));
        cmbAbogado.setPreferredSize(new Dimension(230, 34));
        txtBusqueda.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        cmbEstado.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        cmbEquipo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        cmbAbogado.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        lblTramiteWebSeleccionado.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblNumeroDocumentoSeleccionado.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblRecepcionAbogado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        btnBuscar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnAsignarSeleccionado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnAsignarSeleccionados.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
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
        AppV2TableColumnSizer.applyWidths(table, 46, 54, 84, 185, 135, 230, 135, 130, 260, 240, 220, 155, 170, 0);
        configurarColumna(table.getColumnModel().getColumn(COL_EXPANDIR), 46, 42, 48);
        configurarColumna(table.getColumnModel().getColumn(COL_SELECCION), 54, 54, 58);
        configurarColumna(table.getColumnModel().getColumn(COL_DIAS), 84, 78, 92);
        configurarColumna(table.getColumnModel().getColumn(3), 185, 165, 260);
        configurarColumna(table.getColumnModel().getColumn(4), 135, 128, 170);
        configurarColumna(table.getColumnModel().getColumn(5), 230, 210, 320);
        configurarColumna(table.getColumnModel().getColumn(6), 135, 128, 190);
        configurarColumna(table.getColumnModel().getColumn(7), 130, 120, 180);
        configurarColumna(table.getColumnModel().getColumn(8), 260, 230, 380);
        configurarColumna(table.getColumnModel().getColumn(9), 240, 210, 360);
        configurarColumna(table.getColumnModel().getColumn(10), 220, 190, 320);
        configurarColumna(table.getColumnModel().getColumn(COL_ESTADO), 155, 145, 220);
        configurarColumna(table.getColumnModel().getColumn(COL_RELACIONADOS), 170, 150, 240);
        table.getColumnModel().getColumn(COL_EXPANDIR).setCellRenderer(new ExpandirRenderer());
        table.getColumnModel().getColumn(COL_SELECCION).setHeaderRenderer(new SelectAllHeaderRenderer());
        table.getColumnModel().getColumn(COL_SELECCION).setCellRenderer(new SeleccionRenderer());
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
        documentosRelacionadosTable.getColumnModel().getColumn(0).setPreferredWidth(190);
        documentosRelacionadosTable.getColumnModel().getColumn(0).setMinWidth(150);
        documentosRelacionadosTable.getColumnModel().getColumn(1).setPreferredWidth(58);
        documentosRelacionadosTable.getColumnModel().getColumn(1).setMinWidth(54);
        documentosRelacionadosTable.getColumnModel().getColumn(1).setMaxWidth(64);
        documentosRelacionadosTable.getColumnModel().getColumn(1).setCellRenderer(new RecibirDocumentoRenderer());
        documentosRelacionadosTable.getColumnModel().getColumn(1).setCellEditor(new RecibirDocumentoEditor());
    }

    private void configurarColumna(TableColumn column, int preferred, int min, int max) {
        column.setPreferredWidth(preferred);
        column.setMinWidth(min);
        column.setMaxWidth(max);
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnVerDetalle.addActionListener(e -> abrirDetalleSeleccionado());
        btnVerRelacionados.addActionListener(e -> abrirRelacionadosSeleccionado());
        btnAsociarRelacionados.addActionListener(e -> asociarRelacionadosRapido());
        btnSeleccionarVisibles.addActionListener(e -> seleccionarVisibles());
        btnLimpiarSeleccion.addActionListener(e -> limpiarSeleccion());
        btnAsignarSeleccionado.addActionListener(e -> asignarFilaSeleccionada());
        btnAsignarSeleccionados.addActionListener(e -> asignarMarcados());
        cmbEquipo.addActionListener(e -> {
            if (!cargandoCombos) {
                cargarAbogados();
            }
        });
        cmbAbogado.addActionListener(e -> actualizarSupervisor());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarPanelSeleccion();
            }
        });
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == COL_SELECCION && !actualizandoSeleccion) {
                actualizarPanelSeleccion();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewColumn = table.columnAtPoint(e.getPoint());
                int viewRow = table.rowAtPoint(e.getPoint());
                if (viewRow >= 0
                        && viewColumn >= 0
                        && table.convertColumnIndexToModel(viewColumn) == COL_EXPANDIR) {
                    alternarExpansionFila(table.convertRowIndexToModel(viewRow));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                reabrirPanelSiCorresponde(e);
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
                        cmbEquipo.addItem(new EquipoItem(equipo));
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los equipos de asignación.", ex);
                } finally {
                    cargandoCombos = false;
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
        cmbAbogado.removeAllItems();
        cmbAbogado.addItem(UsuarioItem.placeholder("Seleccione abogado"));
        lblSupervisor.setText("-");
        if (equipo == null) {
            return;
        }
        setTrabajando(true, "Cargando abogados del equipo destino...");
        SwingWorker<List<UsuarioAsignableDTO>, Void> worker = new SwingWorker<List<UsuarioAsignableDTO>, Void>() {
            @Override
            protected List<UsuarioAsignableDTO> doInBackground() throws Exception {
                return usuarioService.listarAbogadosAsignables(equipo.getIdEquipo());
            }

            @Override
            protected void done() {
                try {
                    List<UsuarioAsignableDTO> abogados = get();
                    for (UsuarioAsignableDTO abogado : abogados) {
                        cmbAbogado.addItem(new UsuarioItem(abogado));
                    }
                    if (abogados.isEmpty()) {
                        lblEstado.setText("No hay abogados activos asociados al equipo seleccionado.");
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los abogados responsables.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void buscar() {
        busquedaInicialEjecutada = true;
        limpiarSeleccion();
        LocalDate desde = fechaSeleccionada(fechaSolicitudDesde);
        LocalDate hasta = fechaSeleccionada(fechaSolicitudHasta);
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            mostrarInfo("La fecha hasta no puede ser menor que la fecha desde.");
            return;
        }
        setTrabajando(true, "Consultando expedientes según filtros...");
        String texto = txtBusqueda.getText();
        String estado = codigoSeleccionado(cmbEstado);
        int limite = ((Number) spnLimite.getValue()).intValue();
        SwingWorker<List<AsignacionExpedienteDTO>, Void> worker = new SwingWorker<List<AsignacionExpedienteDTO>, Void>() {
            @Override
            protected List<AsignacionExpedienteDTO> doInBackground() throws Exception {
                return asignacionService.buscarExpedientes(texto, estado, desde, hasta, limite);
            }

            @Override
            protected void done() {
                try {
                    cargarTabla(get());
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar la bandeja de asignación.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void cargarTabla(List<AsignacionExpedienteDTO> items) {
        expedientes.clear();
        expedientes.addAll(items);
        filasTabla.clear();
        asociadosCache.clear();
        principalesExpandidos.clear();
        principalesCargando.clear();
        tableModel.setRowCount(0);
        table.clearSelection();
        int alertas = 0;
        for (AsignacionExpedienteDTO item : expedientes) {
            if (item.getPosiblesRelacionados() > 0 || item.getAsociadosConfirmados() > 0) {
                alertas++;
            } else if (!"Normal".equalsIgnoreCase(item.getAlertaIngreso())) {
                alertas++;
            }
            agregarFilaPrincipal(item);
        }
        cardPendientes.setValue(String.valueOf(items.size()));
        cardRelacionados.setValue(String.valueOf(alertas));
        lblEstado.setText(items.isEmpty()
                ? "No se encontraron expedientes con los filtros ingresados."
                : items.size() + " expediente(s) encontrado(s). Solo REGISTRO / REGISTRADO queda habilitado para asignar.");
        tablePanel.setEmpty(items.isEmpty());
        actualizarPanelSeleccion();
    }

    private void agregarFilaPrincipal(AsignacionExpedienteDTO item) {
        AsignacionTableRow row = AsignacionTableRow.principal(item);
        filasTabla.add(row);
        tableModel.addRow(new Object[]{
            iconoExpansion(item),
            Boolean.FALSE,
            item.getDiasRestantes() == null ? "" : item.getDiasRestantes(),
            item.getNumeroExpediente(),
            formatDate(item.getFechaRecepcion()),
            item.getProcedimiento(),
            item.getTipoActa(),
            item.getNumeroActa(),
            item.getTitular(),
            item.getSolicitante(),
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
            "",
            formatDate(asociado.getFechaRecepcion()),
            procedimientoAsociado(asociado),
            valorUi(asociado.getTipoActa()),
            valorUi(asociado.getNumeroActa()),
            valorUi(asociado.getTitular()),
            valorUi(asociado.getSolicitante()),
            valorUi(abogadoAsociado(principal, asociado)),
            estadoAsociado(asociado),
            textoRelacionAsociada(asociado),
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
        if (item.getAsociadosConfirmados() > 0 && item.getPosiblesRelacionados() > 0) {
            return item.getAsociadosConfirmados() + " asociados / " + item.getPosiblesRelacionados() + " pendientes";
        }
        if (item.getAsociadosConfirmados() > 0) {
            return item.getAsociadosConfirmados() + " documento(s) asociado(s)";
        }
        if (item.getPosiblesRelacionados() > 0) {
            return item.getPosiblesRelacionados() + " relacionados";
        }
        String alerta = item.getAlertaIngreso();
        return "Normal".equalsIgnoreCase(alerta) ? "Sin alerta" : alerta;
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
        txtBusqueda.setText("");
        restaurarFechasBusqueda();
        cmbEstado.setSelectedIndex(0);
        spnLimite.setValue(200);
        expedientes.clear();
        filasTabla.clear();
        asociadosCache.clear();
        principalesExpandidos.clear();
        principalesCargando.clear();
        tableModel.setRowCount(0);
        table.clearSelection();
        tablePanel.setEmpty(true);
        txtComentario.setText("");
        cardPendientes.setValue("0");
        cardRelacionados.setValue("0");
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
        if (principalesExpandidos.contains(idPrincipal)) {
            contraerAsociados(idPrincipal);
            return;
        }
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
        principalesExpandidos.add(principal.getIdExpediente());
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

    private void contraerAsociados(Long idPrincipal) {
        if (idPrincipal == null) {
            return;
        }
        int principalRow = indiceFilaPrincipal(idPrincipal);
        if (principalRow < 0) {
            return;
        }
        for (int i = filasTabla.size() - 1; i > principalRow; i--) {
            AsignacionTableRow row = filasTabla.get(i);
            if (row.esAsociada() && idPrincipal.equals(row.idExpedientePrincipal)) {
                filasTabla.remove(i);
                tableModel.removeRow(i);
            }
        }
        principalesExpandidos.remove(idPrincipal);
        refrescarIconoExpansion(principalRow);
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
            if (row != null && row.esAsignable()) {
                tableModel.setValueAt(Boolean.TRUE, modelRow, COL_SELECCION);
            }
        }
        actualizandoSeleccion = false;
        panelAsignacionCerradoPorUsuario = false;
        actualizarPanelSeleccion();
    }

    private void alternarSeleccionVisibleDesdeHeader() {
        if (tableModel.getRowCount() == 0 || table.getRowCount() == 0) {
            return;
        }
        boolean seleccionar = !todasVisiblesSeleccionadas;
        actualizandoSeleccion = true;
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            AsignacionTableRow row = filaTabla(modelRow);
            if (row != null && row.esAsignable()) {
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
        actualizandoSeleccion = true;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(Boolean.FALSE, i, COL_SELECCION);
        }
        actualizandoSeleccion = false;
        table.clearSelection();
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
        if (row == null || !row.esAsignable()) {
            mostrarInfo("El expediente ya se encuentra asignado o no está habilitado para asignación.");
            return;
        }
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        ejecutarAsignacion(ids);
    }

    private void asignarMarcados() {
        finalizarEdicionTabla();
        List<Long> ids = obtenerIdsMarcados();
        if (ids.isEmpty()) {
            mostrarInfo("Seleccione uno o más expedientes para asignar.");
            return;
        }
        ejecutarAsignacion(ids);
    }

    private void ejecutarAsignacion(List<Long> ids) {
        EquipoAsignacionDTO equipo = obtenerEquipoSeleccionado();
        UsuarioAsignableDTO abogado = obtenerAbogadoSeleccionado();
        if (equipo == null || abogado == null) {
            mostrarInfo("Seleccione equipo destino y abogado responsable antes de asignar.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Se asignarán " + ids.size() + " expediente(s) al abogado "
                        + abogado.getDisplayName() + " en el equipo " + equipo.getNombre()
                        + ". ¿Desea continuar?",
                "Confirmar asignación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setTrabajando(true, "Asignando expedientes en una transacción...");
        String comentario = txtComentario.getText();
        SwingWorker<AsignacionResultadoDTO, Void> worker = new SwingWorker<AsignacionResultadoDTO, Void>() {
            @Override
            protected AsignacionResultadoDTO doInBackground() throws Exception {
                return asignacionService.asignar(ids, equipo, abogado, comentario);
            }

            @Override
            protected void done() {
                try {
                    AsignacionResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelAsignacionV2.this,
                            resultado.getMensaje(),
                            "Asignación",
                            JOptionPane.INFORMATION_MESSAGE);
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

    private void abrirRelacionadosSeleccionado() {
        finalizarEdicionTabla();
        AsignacionExpedienteDTO expediente = obtenerExpedienteParaRelacionados();
        if (expediente == null) {
            mostrarInfo("Seleccione un solo expediente para revisar relacionados.");
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgExpedientesRelacionadosV2 dialog = new DlgExpedientesRelacionadosV2(owner, expediente.getIdExpediente());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        buscar();
    }

    private void asociarRelacionadosRapido() {
        finalizarEdicionTabla();
        AsociacionRapidaSeleccion seleccion = obtenerSeleccionAsociacionRapida();
        if (seleccion == null) {
            mostrarInfo("Seleccione un expediente con posibles relacionados o marque dos expedientes con la misma acta y titular.");
            return;
        }
        if (!seleccion.esSeleccionMultiple() && seleccion.expedienteFoco.getPosiblesRelacionados() <= 0) {
            mostrarInfo("El expediente seleccionado no tiene coincidencias pendientes por misma acta y titular.");
            return;
        }
        String detalle = seleccion.esSeleccionMultiple()
                ? "Se asociarán los " + seleccion.totalSeleccionados + " registros marcados como documentos duplicados del expediente principal si comparten el mismo número de acta y titular.\n"
                : "Se asociarán las coincidencias pendientes como documentos duplicados del expediente principal.\n";
        int confirm = JOptionPane.showConfirmDialog(
                this,
                detalle
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
                List<Long> ids = new ArrayList<>(seleccion.idsRelacionados);
                if (!seleccion.esSeleccionMultiple()) {
                    ids.clear();
                    List<ExpedienteRelacionadoDTO> relacionados = relacionadoDeteccionService.listarPosiblesRelacionados(seleccion.expedienteFoco.getIdExpediente());
                    for (ExpedienteRelacionadoDTO relacionado : relacionados) {
                        if (relacionado.getIdExpediente() != null) {
                            ids.add(relacionado.getIdExpediente());
                        }
                    }
                }
                if (ids.isEmpty()) {
                    return new ExpedienteRelacionResultadoDTO(
                            0,
                            0,
                            0,
                            0,
                            "No hay coincidencias pendientes para asociar.");
                }
                return relacionadoService.asociarRelacionados(
                        seleccion.idPrincipal,
                        ids,
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

    private List<Long> obtenerIdsMarcados() {
        finalizarEdicionTabla();
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, COL_SELECCION))) {
                AsignacionTableRow row = filaTabla(i);
                if (row == null || row.esAsociada()) {
                    mostrarInfo("La selección contiene documentos asociados que no requieren asignación independiente.");
                    return new ArrayList<Long>();
                }
                AsignacionExpedienteDTO item = row.principal;
                if (!item.isAsignable()) {
                    mostrarInfo("El expediente ya se encuentra asignado o no está habilitado para asignación.");
                    return new ArrayList<Long>();
                }
                ids.add(item.getIdExpediente());
            }
        }
        return ids;
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

    private void actualizarPanelSeleccion() {
        int marcados = contarSeleccionados();
        int modelRow = obtenerModelRowSeleccionada();
        AsignacionTableRow filaSeleccionada = filaTabla(modelRow);
        int seleccionados = marcados > 0 ? marcados : (filaSeleccionada != null ? 1 : 0);
        String seleccionadosText = seleccionados == 0
                ? "Seleccione uno o más expedientes para habilitar el panel de asignación."
                : seleccionados + " expediente(s) seleccionados";
        lblSeleccionados.setText(seleccionadosText);
        lblSeleccionadosPanel.setText(seleccionados + " expediente(s) seleccionados");
        cardSeleccionados.setValue(String.valueOf(seleccionados));
        actualizarEstadoHeaderSeleccion();
        if (seleccionados == 0) {
            panelAsignacionCerradoPorUsuario = false;
        }
        actualizarVisibilidadPanelAsignacion(seleccionados > 0 && !panelAsignacionCerradoPorUsuario);

        AsignacionTableRow filaPanel = filaParaPanel(modelRow, marcados);
        if (filaPanel != null && filaPanel.esPrincipal()) {
            AsignacionExpedienteDTO item = filaPanel.principal;
            aplicarIdentidadVisual(item, false);
            lblExpedienteSeleccionado.setText(item.getNumeroExpediente());
            actualizarIdentificacionDocumento(
                    item.getNumeroTramiteDocumentario(),
                    item.getNumeroDocumento(),
                    detalleDocumentoPrincipal(item));
            aplicarEstadoRecepcion(lblRecepcionAbogado, estadoRecepcionPrincipal(item));
            lblOrigen.setText("Registro / Registrado");
            lblDestino.setText("Asignación / Asignado");
            lblIngreso.setText(item.getAlertaIngreso());
            lblIngreso.setToolTipText(item.getObservacionSolicitud().isEmpty() ? item.getAlertaIngreso() : item.getObservacionSolicitud());
            lblRelacionados.setText(item.getPosiblesRelacionados() > 0
                    ? item.getPosiblesRelacionados() + " posibles relacionados por misma acta y titular."
                    : (item.getAsociadosConfirmados() > 0
                            ? item.getAsociadosConfirmados() + " documento(s) asociado(s) confirmado(s)."
                            : "Sin alerta de relacionados."));
            cargarDocumentosRelacionadosPanel(item);
            btnAsociarRelacionados.setText(item.getPosiblesRelacionados() > 0
                    ? "Asociar " + item.getPosiblesRelacionados() + " relacionado(s)"
                    : "Sin relacionados pendientes");
            btnAsociarRelacionados.setEnabled(item.getPosiblesRelacionados() > 0);
        } else if (filaPanel != null && filaPanel.esAsociada()) {
            aplicarIdentidadVisual(filaPanel.principal, false);
            lblExpedienteSeleccionado.setText("Expediente principal: " + filaPanel.numeroExpedientePrincipal());
            actualizarIdentificacionDocumento(
                    filaPanel.asociado.getNumeroTramiteDocumentario(),
                    filaPanel.asociado.getNumeroDocumento(),
                    detalleDocumentoAsociado(filaPanel));
            aplicarEstadoRecepcion(lblRecepcionAbogado, estadoRecepcionAsociado(filaPanel));
            lblOrigen.setText("Expediente principal");
            lblDestino.setText(filaPanel.numeroExpedientePrincipal());
            lblIngreso.setText("Duplicado confirmado");
            lblIngreso.setToolTipText("Este documento está asociado al expediente principal y no requiere asignación independiente.");
            limpiarDocumentosRelacionadosPanel("Asociado a expediente principal " + filaPanel.numeroExpedientePrincipal() + ".");
            btnAsociarRelacionados.setText("Relación confirmada");
            btnAsociarRelacionados.setEnabled(false);
        } else if (marcados > 1) {
            aplicarIdentidadVisual(null, true);
            lblExpedienteSeleccionado.setText("Selección múltiple");
            actualizarIdentificacionDocumento(null, null, null);
            aplicarEstadoRecepcion(lblRecepcionAbogado, "No aplica");
            lblOrigen.setText("Registro / Registrado");
            lblDestino.setText("Asignación / Asignado");
            lblIngreso.setText("Múltiple");
            lblIngreso.setToolTipText("Selección múltiple de expedientes marcados.");
            limpiarDocumentosRelacionadosPanel("Puede asociar la selección si comparte número de acta y titular.");
            btnAsociarRelacionados.setText("Asociar selección relacionada");
            btnAsociarRelacionados.setEnabled(true);
        } else {
            aplicarIdentidadVisual(null, false);
            limpiarPanelAsignacion();
        }
    }

    private void cargarDocumentosRelacionadosPanel(AsignacionExpedienteDTO item) {
        if (item == null || item.getIdExpediente() == null || item.getPosiblesRelacionados() <= 0) {
            limpiarDocumentosRelacionadosPanel(item != null && item.getAsociadosConfirmados() > 0
                    ? item.getAsociadosConfirmados() + " documento(s) asociado(s) confirmado(s)."
                    : "Sin alerta de relacionados.");
            return;
        }
        Long idExpediente = item.getIdExpediente();
        if (idExpediente.equals(idExpedienteDocumentosRelacionados)
                && documentosRelacionadosModel.getRowCount() > 0) {
            return;
        }
        idExpedienteDocumentosRelacionados = idExpediente;
        documentosRelacionadosPanel.clear();
        documentosRelacionadosModel.setRowCount(0);
        lblRelacionados.setText("Buscando documentos relacionados...");

        SwingWorker<List<ExpedienteRelacionadoDTO>, Void> worker = new SwingWorker<List<ExpedienteRelacionadoDTO>, Void>() {
            @Override
            protected List<ExpedienteRelacionadoDTO> doInBackground() throws Exception {
                return relacionadoDeteccionService.listarPosiblesRelacionados(idExpediente);
            }

            @Override
            protected void done() {
                if (!idExpediente.equals(idExpedienteDocumentosRelacionados)) {
                    return;
                }
                try {
                    List<ExpedienteRelacionadoDTO> relacionados = get();
                    documentosRelacionadosPanel.clear();
                    documentosRelacionadosModel.setRowCount(0);
                    for (ExpedienteRelacionadoDTO relacionado : relacionados) {
                        documentosRelacionadosPanel.add(relacionado);
                        documentosRelacionadosModel.addRow(new Object[]{
                            textoDocumentoRelacionado(relacionado),
                            "Recibir"
                        });
                    }
                    lblRelacionados.setText(relacionados.isEmpty()
                            ? "Sin documentos relacionados pendientes."
                            : relacionados.size() + " documento(s) relacionado(s) pendiente(s).");
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
        lblRelacionados.setText(mensaje == null || mensaje.trim().isEmpty()
                ? "Sin alerta de relacionados."
                : mensaje);
    }

    private String textoDocumentoRelacionado(ExpedienteRelacionadoDTO relacionado) {
        if (relacionado == null) {
            return "-";
        }
        return valorUi(relacionado.getNumeroDocumento());
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

    private void recibirDocumentoRelacionado(int modelRow) {
        if (modelRow < 0 || modelRow >= documentosRelacionadosPanel.size()) {
            return;
        }
        Long idPrincipal = idExpedienteDocumentosRelacionados;
        ExpedienteRelacionadoDTO relacionado = documentosRelacionadosPanel.get(modelRow);
        if (idPrincipal == null || relacionado == null || relacionado.getIdExpediente() == null) {
            mostrarInfo("Seleccione un documento relacionado válido para recibir.");
            return;
        }
        AsignacionExpedienteDTO principal = buscarExpedientePrincipal(idPrincipal);
        if (!usuarioActualEsAbogadoResponsable(principal)) {
            mostrarInfo("Solo el abogado responsable puede registrar la recepción.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Se recibirá el documento " + textoDocumentoRelacionado(relacionado)
                        + " como documento duplicado asociado al expediente principal.\n"
                        + "La evidencia quedará registrada en la relación e historial del expediente.\n"
                        + "¿Desea continuar?",
                "Recibir documento relacionado",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setTrabajando(true, "Recibiendo documento relacionado...");
        SwingWorker<ExpedienteRelacionResultadoDTO, Void> worker = new SwingWorker<ExpedienteRelacionResultadoDTO, Void>() {
            @Override
            protected ExpedienteRelacionResultadoDTO doInBackground() throws Exception {
                return relacionadoService.asociarRelacionados(
                        idPrincipal,
                        Collections.singletonList(relacionado.getIdExpediente()),
                        "Documento recibido y asociado al expediente principal desde Asignación.");
            }

            @Override
            protected void done() {
                try {
                    ExpedienteRelacionResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelAsignacionV2.this,
                            resultado.getMensaje(),
                            "Documento recibido",
                            JOptionPane.INFORMATION_MESSAGE);
                    buscar();
                } catch (Exception ex) {
                    mostrarError("No se pudo recibir el documento relacionado.", ex);
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
            int paletteIndex = indicePaleta(item.getIdExpediente());
            acentoSeleccion = FOCUS_ACCENTS[paletteIndex];
            fondoSeleccion = FOCUS_BACKGROUNDS[paletteIndex];
            idExpedienteGrupoEnFoco = item.getIdExpediente();
            contextoChip = "Expediente en foco: " + item.getNumeroExpediente();
        } else if (multiple) {
            acentoSeleccion = AppV2Theme.INDIGO;
            fondoSeleccion = new Color(238, 240, 250);
            idExpedienteGrupoEnFoco = null;
            contextoChip = "Selección múltiple";
        } else {
            acentoSeleccion = AppV2Theme.TEAL;
            fondoSeleccion = AppV2Theme.SOFT_BLUE;
            idExpedienteGrupoEnFoco = null;
            contextoChip = "Panel de asignación";
        }
        chipPanelAsignacion.setAccent(acentoSeleccion, fondoSeleccion);
        actualizarTooltipChip();
        if (panelAsignacion != null) {
            panelAsignacion.setAccentColor(acentoSeleccion);
        }
        table.setSelectionBackground(fondoSeleccion);
        table.setSelectionForeground(AppV2Theme.TEXT_PRIMARY);
        table.repaint();
    }

    private void actualizarTooltipChip() {
        boolean expandido = splitOperativo != null && splitOperativo.isSideExpanded();
        String accion = expandido
                ? "Restaurar el ancho del Panel de asignación"
                : "Ampliar el Panel de asignación";
        chipPanelAsignacion.setToolTipText(accion + " · " + contextoChip);
    }

    private int indicePaleta(Long idExpediente) {
        long value = idExpediente == null ? 0L : idExpediente.longValue();
        return (int) Math.abs(value % FOCUS_ACCENTS.length);
    }

    private Color acentoGrupo(Long idExpedientePrincipal) {
        return FOCUS_ACCENTS[indicePaleta(idExpedientePrincipal)];
    }

    private Color fondoGrupo(Long idExpedientePrincipal) {
        return FOCUS_BACKGROUNDS[indicePaleta(idExpedientePrincipal)];
    }

    private void limpiarPanelAsignacion() {
        lblExpedienteSeleccionado.setText("-");
        actualizarIdentificacionDocumento(null, null, null);
        aplicarEstadoRecepcion(lblRecepcionAbogado, "-");
        lblOrigen.setText("Registro / Registrado");
        lblDestino.setText("Asignación / Asignado");
        lblIngreso.setText("Normal");
        lblIngreso.setToolTipText(null);
        limpiarDocumentosRelacionadosPanel("Sin alerta de relacionados.");
        btnAsociarRelacionados.setText("Sin relacionados pendientes");
        btnAsociarRelacionados.setEnabled(false);
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
        boolean expandido = splitOperativo.toggleSideExpanded();
        chipPanelAsignacion.setExpanded(expandido);
        actualizarTooltipChip();
        panelOperativo.revalidate();
        panelOperativo.repaint();
    }

    private void reabrirPanelSiCorresponde(MouseEvent event) {
        if (!panelAsignacionCerradoPorUsuario || table.rowAtPoint(event.getPoint()) < 0) {
            return;
        }
        abrirPanelAsignacion();
    }

    private void actualizarVisibilidadPanelAsignacion(boolean mostrar) {
        if (panelAsignacion == null || splitOperativo == null || panelAsignacionVisible == mostrar) {
            return;
        }
        panelAsignacionVisible = mostrar;
        splitOperativo.setSideVisible(mostrar);
        chipPanelAsignacion.setExpanded(splitOperativo.isSideExpanded());
        actualizarTooltipChip();
        panelOperativo.revalidate();
        panelOperativo.repaint();
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
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, COL_SELECCION))) {
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
            if (row != null && row.esAsignable()) {
                visiblesAsignables++;
                if (Boolean.TRUE.equals(tableModel.getValueAt(modelRow, COL_SELECCION))) {
                    visiblesSeleccionados++;
                }
            }
        }
        todasVisiblesSeleccionadas = visiblesAsignables > 0 && visiblesAsignables == visiblesSeleccionados;
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.repaint();
        }
    }

    private void setTrabajando(boolean trabajando, String mensaje) {
        btnBuscar.setEnabled(!trabajando);
        btnLimpiar.setEnabled(!trabajando);
        btnVerDetalle.setEnabled(!trabajando);
        btnVerRelacionados.setEnabled(!trabajando);
        btnAsociarRelacionados.setEnabled(!trabajando && puedeAsociarRelacionados());
        btnSeleccionarVisibles.setEnabled(!trabajando);
        btnLimpiarSeleccion.setEnabled(!trabajando);
        btnAsignarSeleccionado.setEnabled(!trabajando);
        btnAsignarSeleccionados.setEnabled(!trabajando);
        documentosRelacionadosTable.setEnabled(!trabajando);
        if (mensaje != null) {
            lblEstado.setText(mensaje);
        }
    }

    private void mostrarInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Asignación", JOptionPane.INFORMATION_MESSAGE);
    }

    private void finalizarEdicionTabla() {
        if (!table.isEditing()) {
            return;
        }
        TableCellEditor editor = table.getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
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
        return expediente != null && expediente.getPosiblesRelacionados() > 0;
    }

    private boolean puedeRecibirDocumentoRelacionado() {
        return usuarioActualEsAbogadoResponsable(buscarExpedientePrincipal(idExpedienteDocumentosRelacionados));
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
                "Fecha Solicitud",
                "Proc. Registral",
                "Tipo Acta",
                "Nro. Acta",
                "Titular",
                "Solicitante",
                "Abogado asignado",
                "Estado",
                "Relacionados",
                "_ID"
            }, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == COL_SELECCION
                    && row >= 0
                    && row < filasTabla.size()
                    && filasTabla.get(row).esAsignable();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == COL_SELECCION ? Boolean.class : Object.class;
        }
    }

    private class SelectAllHeaderRenderer extends JCheckBox implements TableCellRenderer {

        private SelectAllHeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
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
            setSelected(todasVisiblesSeleccionadas);
            return this;
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
            Color background = isSelected ? fondoSeleccion : (row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
            if (fila != null && fila.esAsociada()) {
                background = fondoGrupo(fila.idExpedientePrincipal);
                setBackground(background);
                glyph.configure(ExpandGlyph.ASSOCIATED, acentoGrupo(fila.idExpedientePrincipal), background);
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
                glyph.configure(state, acentoGrupo(idPrincipal), background);
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
        private static final int ASSOCIATED = 4;

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
                if (state == ASSOCIATED) {
                    pintarDocumento(g2, cx, cy);
                    return;
                }
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

        private void pintarDocumento(Graphics2D g2, int cx, int cy) {
            int w = 14;
            int h = 17;
            int x = cx - w / 2;
            int y = cy - h / 2;
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 24));
            g2.fillRoundRect(x, y, w, h, 4, 4);
            g2.setColor(accent);
            g2.setStroke(new BasicStroke(1.3f));
            g2.drawRoundRect(x, y, w, h, 4, 4);
            g2.drawLine(x + 4, y + 6, x + w - 4, y + 6);
            g2.drawLine(x + 4, y + 10, x + w - 4, y + 10);
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
                JLabel empty = new JLabel("");
                empty.setOpaque(true);
                empty.setBackground(fondoGrupo(fila.idExpedientePrincipal));
                empty.setBorder(BorderFactory.createEmptyBorder());
                return empty;
            }
            setSelected(Boolean.TRUE.equals(value));
            setEnabled(fila != null && fila.esAsignable());
            setBackground(isSelected ? fondoSeleccion : (row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT));
            setToolTipText("Seleccionar expediente principal.");
            return this;
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
            boolean filaEnFoco = fila != null
                    && idExpedienteGrupoEnFoco != null
                    && idExpedienteGrupoEnFoco.equals(fila.idExpedientePrincipal);
            if (!filaAsociada && !isSelected && !filaEnFoco && modelColumn == COL_DIAS) {
                return StatusBadgeV2.forDias(value);
            }
            if (!isSelected && !filaEnFoco && modelColumn == COL_ESTADO) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
            }
            if (!isSelected && !filaEnFoco && modelColumn == COL_RELACIONADOS) {
                String text = value == null ? "" : value.toString();
                if (!text.startsWith("Sin")) {
                    Color bg = filaAsociada ? fondoGrupo(fila.idExpedientePrincipal) : AppV2Theme.SOFT_ORANGE;
                    Color fg = filaAsociada ? acentoGrupo(fila.idExpedientePrincipal) : AppV2Theme.WARNING;
                    return new BadgeV2(text, bg, fg);
                }
            }
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(filaAsociada
                    ? AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL)
                    : AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            String text = value == null ? "" : value.toString();
            setToolTipText(text.isEmpty() ? null : text);
            if (isSelected || filaEnFoco) {
                c.setBackground(filaAsociada ? fondoGrupo(fila.idExpedientePrincipal) : fondoSeleccion);
                c.setForeground(AppV2Theme.TEXT_PRIMARY);
                setBorder(modelColumn == COL_EXPEDIENTE
                        ? BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 4, 0, 0, filaAsociada ? acentoGrupo(fila.idExpedientePrincipal) : acentoSeleccion),
                                BorderFactory.createEmptyBorder(0, filaAsociada ? 20 : 4, 0, 8))
                        : BorderFactory.createEmptyBorder(0, 8, 0, 8));
            } else if (filaAsociada) {
                setBorder(BorderFactory.createEmptyBorder(0, modelColumn == COL_EXPEDIENTE ? 20 : 8, 0, 8));
                c.setBackground(fondoGrupo(fila.idExpedientePrincipal));
                c.setForeground(AppV2Theme.TEXT_SECONDARY);
            } else {
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                c.setBackground(row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
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
            c.setBackground(isSelected ? fondoSeleccion : AppV2Theme.SURFACE);
            c.setForeground(AppV2Theme.TEXT_PRIMARY);
            String text = value == null ? "" : value.toString();
            setToolTipText(text);
            return c;
        }
    }

    private class RecibirDocumentoRenderer extends JButton implements TableCellRenderer {

        private RecibirDocumentoRenderer() {
            setText("Rec.");
            setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
            setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
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
            boolean permitido = puedeRecibirDocumentoRelacionado();
            setEnabled(permitido);
            setBackground(permitido ? AppV2Theme.SOFT_GREEN : AppV2Theme.SURFACE_ALT);
            setForeground(permitido ? AppV2Theme.SUCCESS : AppV2Theme.TEXT_SECONDARY);
            setToolTipText(permitido
                    ? "Recibir documento y asociarlo al expediente principal."
                    : "Solo el abogado responsable puede registrar la recepción.");
            return this;
        }
    }

    private class RecibirDocumentoEditor extends AbstractCellEditor implements TableCellEditor {

        private final JButton button = new JButton("Rec.");
        private int modelRow = -1;

        private RecibirDocumentoEditor() {
            button.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
            button.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            button.setFocusPainted(false);
            button.addActionListener(e -> {
                int row = modelRow;
                fireEditingStopped();
                recibirDocumentoRelacionado(row);
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
            boolean permitido = puedeRecibirDocumentoRelacionado();
            button.setEnabled(permitido);
            button.setBackground(permitido ? AppV2Theme.SOFT_GREEN : AppV2Theme.SURFACE_ALT);
            button.setForeground(permitido ? AppV2Theme.SUCCESS : AppV2Theme.TEXT_SECONDARY);
            button.setToolTipText(permitido
                    ? "Recibir documento y asociarlo al expediente principal."
                    : "Solo el abogado responsable puede registrar la recepción.");
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

        private AsociacionRapidaSeleccion(
                AsignacionExpedienteDTO expedienteFoco,
                Long idPrincipal,
                List<Long> idsRelacionados,
                int totalSeleccionados) {
            this.expedienteFoco = expedienteFoco;
            this.idPrincipal = idPrincipal;
            this.idsRelacionados = idsRelacionados;
            this.totalSeleccionados = totalSeleccionados;
        }

        private boolean esSeleccionMultiple() {
            return totalSeleccionados >= 2;
        }
    }
}
