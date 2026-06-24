package com.sdrerc.ui.views.expedienteconsola;

import com.sdrerc.application.sdrercapp.ExpedienteConsultaService;
import com.sdrerc.application.sdrercapp.ExpedienteDetalleService;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteBandejaDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteConsolaDTO;
import com.sdrerc.ui.appv2.components.AppV2ActionPanel;
import com.sdrerc.ui.appv2.components.AppV2FilterPanel;
import com.sdrerc.ui.appv2.components.AppV2NotebookToggleTab;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2SideActionPanel;
import com.sdrerc.ui.appv2.components.AppV2SideSectionPanel;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.components.AppV2TablePanel;
import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.PremiumDateFieldV2;
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.helpers.EstadoExpedienteComboSupportV2;
import com.sdrerc.ui.appv2.helpers.FiltroCatalogoItemV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import com.sdrerc.util.DateRangePickerSupport;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RowFilter;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public class JPanelBandejaExpedientesNueva extends JPanel {

    private static final int PANEL_RECEPCION_ANCHO_MINIMO = 380;
    private static final int PANEL_RECEPCION_ANCHO_NORMAL = 430;
    private static final int PANEL_RECEPCION_TAB_OVERHANG = 18;
    private static final int PANEL_RECEPCION_TAB_TOP = 18;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ExpedienteConsultaService consultaService;
    private final ExpedienteDetalleService detalleService = new ExpedienteDetalleService();
    private final String etapaInicial;
    private final String tituloBandeja;
    private final String subtituloBandeja;
    private final boolean etapaBloqueada;
    private final boolean mostrarEncabezado;
    private final Component encabezadoOperativo;
    private final Consumer<Long> editarExpedienteHandler;
    private final boolean perfilRegistroRecepcion;
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
    private final JLabel lblResultado = new JLabel("Seleccione un expediente y presione Ver detalle para abrir la consola.");
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final AppV2TablePanel tablePanel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField[] columnFilterFields;
    private JPanel tableHeaderWithFilters;
    private ColumnFilterPanel columnFilterPanel;
    private final AppV2NotebookToggleTab tabPanelRecepcion = new AppV2NotebookToggleTab();
    private AppV2OperationalSplitPanel splitBandeja;
    private AppV2SideActionPanel panelRecepcion;
    private final JLabel lblRecepcionExpediente = valueLabel();
    private final JLabel lblRecepcionFecha = valueLabel();
    private final JLabel lblRecepcionCanal = valueLabel();
    private final JLabel lblRecepcionTipoSolicitud = valueLabel();
    private final JLabel lblRecepcionTramite = valueLabel();
    private final JLabel lblRecepcionNumeroExpedienteSgd = valueLabel();
    private final JLabel lblRecepcionTipoDocumento = valueLabel();
    private final JLabel lblRecepcionNumeroDocumento = valueLabel();
    private final JLabel lblRecepcionGrupoFamiliar = valueLabel();
    private final JLabel lblRecepcionProcedimiento = valueLabel();
    private final JLabel lblRecepcionTitular = valueLabel();
    private final JLabel lblRecepcionRemitente = valueLabel();
    private final JLabel lblRecepcionTipoActa = valueLabel();
    private final JLabel lblRecepcionNumeroActa = valueLabel();
    private final JLabel lblRecepcionVencimiento = valueLabel();
    private final JLabel lblRecepcionDias = valueLabel();
    private final JLabel lblRecepcionResponsable = valueLabel();
    private final JLabel lblRecepcionEquipo = valueLabel();
    private final JLabel lblRecepcionEtapaEstado = valueLabel();
    private Long idPanelRecepcionActual;
    private int panelRecepcionLoadSequence;
    private boolean panelRecepcionCargado;

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
        this.consultaService = consultaService;
        this.etapaInicial = normalizar(etapaInicial);
        this.tituloBandeja = textoConDefault(tituloBandeja, "Bandeja de Expedientes");
        this.subtituloBandeja = textoConDefault(subtituloBandeja, "Consulta, seguimiento y priorización de expedientes por etapa, estado, responsable y plazos de atención");
        this.etapaBloqueada = etapaBloqueada && this.etapaInicial != null;
        this.mostrarEncabezado = mostrarEncabezado;
        this.encabezadoOperativo = encabezadoOperativo;
        this.editarExpedienteHandler = editarExpedienteHandler;
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
        if (perfilRegistroRecepcion) {
            SwingUtilities.invokeLater(this::buscar);
        }
    }

    private static DefaultTableModel crearTableModel(boolean perfilRegistroRecepcion) {
        Object[] columnas = perfilRegistroRecepcion
                ? new Object[]{
                    "Dias",
                    "Nro. Expediente",
                    "Canal",
                    "Fecha Solicitud",
                    "Proc. Registral",
                    "Tipo Acta",
                    "Nro Acta",
                    "Titular",
                    "Alertas",
                    "_ESTADO",
                    "_ID"
                }
                : new Object[]{
                    "Días",
                    "Expediente",
                    "Trámite",
                    "Etapa",
                    "Estado",
                    "Registro",
                    "Vencimiento",
                    "Publicación",
                    "Digital",
                    "_ID"
                };
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (perfilRegistroRecepcion) {
                    if (columnIndex == 0 || columnIndex == 10) {
                        return Long.class;
                    }
                    if (columnIndex == 3) {
                        return LocalDate.class;
                    }
                    return String.class;
                }
                if (columnIndex == 0 || columnIndex == 9) {
                    return Long.class;
                }
                if (columnIndex == 5 || columnIndex == 6) {
                    return LocalDate.class;
                }
                return String.class;
            }
        };
    }

    private void configurarLayout() {
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());

        configurarBotones();

        JPanel filtros = new AppV2FilterPanel();
        configurarControlesFiltro();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = 0;
        gbc.gridx = 0;
        filtros.add(crearLabelFiltro("Búsqueda"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtros.add(txtBusqueda, gbc);
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        JPanel acciones = AppV2ActionPanel.right();
        acciones.add(btnBuscar);
        acciones.add(btnLimpiar);
        acciones.add(btnVerDetalle);
        if (editarExpedienteHandler != null) {
            acciones.add(btnEditar);
        }
        gbc.gridx = 4;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtros.add(acciones, gbc);
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridy = 1;
        gbc.gridx = 0;
        if (perfilRegistroRecepcion) {
            filtros.add(crearLabelFiltro("Fecha solicitud desde"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            filtros.add(fechaSolicitudDesde, gbc);
            gbc.fill = GridBagConstraints.NONE;

            gbc.gridx = 2;
            filtros.add(crearLabelFiltro("Fecha solicitud hasta"), gbc);
            gbc.gridx = 3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            filtros.add(fechaSolicitudHasta, gbc);
            gbc.fill = GridBagConstraints.NONE;

            gbc.gridx = 4;
            filtros.add(crearLabelFiltro("Estado"), gbc);
            gbc.gridx = 5;
            filtros.add(cmbEstado, gbc);

            gbc.gridx = 6;
            filtros.add(crearLabelFiltro("Mostrar"), gbc);
            gbc.gridx = 7;
            filtros.add(spnLimite, gbc);
        } else {
            filtros.add(crearLabelFiltro("Etapa"), gbc);
            gbc.gridx = 1;
            filtros.add(cmbEtapa, gbc);

            gbc.gridx = 2;
            filtros.add(crearLabelFiltro("Estado"), gbc);
            gbc.gridx = 3;
            filtros.add(cmbEstado, gbc);

            gbc.gridx = 4;
            filtros.add(crearLabelFiltro("Mostrar"), gbc);
            gbc.gridx = 5;
            filtros.add(spnLimite, gbc);
        }

        btnVerDetalle.setEnabled(false);
        btnVerDetalle.setToolTipText("Seleccione un expediente para abrir la consola de expediente");
        btnEditar.setEnabled(false);
        btnEditar.setToolTipText("Disponible solo para expedientes Registrados sin asignación a abogado");

        JPanel superior = new JPanel(new BorderLayout(8, 8));
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

        lblResultado.setText(etapaBloqueada
                ? "Seleccione un expediente y presione Ver detalle para abrir la consola."
                : "Seleccione filtros y presione Buscar. Doble clic o Ver detalle abre la consola.");
        lblResultado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblResultado.setForeground(AppV2Theme.TEXT_SECONDARY);

        superior.add(filtros, BorderLayout.CENTER);
        superior.add(lblResultado, BorderLayout.SOUTH);

        JPanel contenidoOperativo = new JPanel(new BorderLayout(14, 14));
        contenidoOperativo.setOpaque(false);
        contenidoOperativo.add(superior, BorderLayout.NORTH);
        contenidoOperativo.add(tablePanel, BorderLayout.CENTER);

        JPanel contenidoPrincipal = new JPanel(new BorderLayout(14, 14));
        contenidoPrincipal.setOpaque(false);
        if (encabezadoOperativo != null) {
            contenidoPrincipal.add(encabezadoOperativo, BorderLayout.NORTH);
        }
        contenidoPrincipal.add(contenidoOperativo, BorderLayout.CENTER);

        panelRecepcion = crearPanelRecepcion();
        splitBandeja = new AppV2OperationalSplitPanel(
                contenidoPrincipal,
                crearPanelRecepcionConTab(panelRecepcion),
                0,
                PANEL_RECEPCION_ANCHO_MINIMO + PANEL_RECEPCION_TAB_OVERHANG,
                PANEL_RECEPCION_ANCHO_NORMAL + PANEL_RECEPCION_TAB_OVERHANG);

        add(splitBandeja, BorderLayout.CENTER);
    }

    private AppV2SideActionPanel crearPanelRecepcion() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Recepción", this::ocultarPanelRecepcion);
        panel.setAccentColor(AppV2Theme.PRIMARY);
        tabPanelRecepcion.setAccent(AppV2Theme.PRIMARY, AppV2Theme.SOFT_BLUE);
        tabPanelRecepcion.setExpanded(false);
        tabPanelRecepcion.setToolTipText("Expandir panel de recepción");
        tabPanelRecepcion.addActionListener(e -> alternarExpansionPanelRecepcion());

        AppV2SideSectionPanel expediente = new AppV2SideSectionPanel("Expediente seleccionado");
        expediente.addRow("Expediente", lblRecepcionExpediente);
        expediente.addRow("Etapa / estado", lblRecepcionEtapaEstado);

        AppV2SideSectionPanel solicitud = new AppV2SideSectionPanel("Solicitud recibida");
        solicitud.addRow("Fecha", lblRecepcionFecha);
        solicitud.addRow("Canal", lblRecepcionCanal);
        solicitud.addRow("Tipo solicitud", lblRecepcionTipoSolicitud);
        solicitud.addRow("Trámite Web", lblRecepcionTramite);
        solicitud.addRow("N° expediente SGD", lblRecepcionNumeroExpedienteSgd);
        solicitud.addRow("Tipo documento", lblRecepcionTipoDocumento);
        solicitud.addRow("N° Documento", lblRecepcionNumeroDocumento);
        solicitud.addRow("Grupo familiar", lblRecepcionGrupoFamiliar);

        AppV2SideSectionPanel datos = new AppV2SideSectionPanel("Datos registrales");
        datos.addRow("Procedimiento", lblRecepcionProcedimiento);
        datos.addRow("Titular", lblRecepcionTitular);
        datos.addRow("Remitente", lblRecepcionRemitente);
        datos.addRow("Tipo acta", lblRecepcionTipoActa);
        datos.addRow("N° Acta", lblRecepcionNumeroActa);

        AppV2SideSectionPanel plazo = new AppV2SideSectionPanel("Plazo");
        plazo.addRow("Vencimiento", lblRecepcionVencimiento);
        plazo.addRow("Días", lblRecepcionDias);

        AppV2SideSectionPanel gestion = new AppV2SideSectionPanel("Gestión actual");
        gestion.addRow("Responsable", lblRecepcionResponsable);
        gestion.addRow("Equipo", lblRecepcionEquipo);

        panel.addSection(expediente);
        panel.addSection(solicitud);
        panel.addSection(datos);
        panel.addSection(plazo);
        panel.addSection(gestion);
        panel.setFooter(crearFooterRecepcion());
        return panel;
    }

    private JPanel crearPanelRecepcionConTab(final AppV2SideActionPanel panel) {
        JPanel wrapper = new JPanel(null) {
            @Override
            public void doLayout() {
                int width = getWidth();
                int height = getHeight();
                int panelX = PANEL_RECEPCION_TAB_OVERHANG;
                panel.setBounds(panelX, 0, Math.max(0, width - panelX), height);
                int tabY = Math.min(PANEL_RECEPCION_TAB_TOP, Math.max(0, height - AppV2NotebookToggleTab.DEFAULT_HEIGHT));
                tabPanelRecepcion.setBounds(
                        0,
                        tabY,
                        AppV2NotebookToggleTab.DEFAULT_WIDTH,
                        AppV2NotebookToggleTab.DEFAULT_HEIGHT);
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(panel);
        wrapper.add(tabPanelRecepcion);
        wrapper.setMinimumSize(new Dimension(PANEL_RECEPCION_ANCHO_MINIMO + PANEL_RECEPCION_TAB_OVERHANG, 0));
        wrapper.setPreferredSize(new Dimension(PANEL_RECEPCION_ANCHO_NORMAL + PANEL_RECEPCION_TAB_OVERHANG, 0));
        return wrapper;
    }

    private JPanel crearFooterRecepcion() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel lbl = new JLabel("Panel informativo de solo lectura");
        lbl.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lbl.setForeground(AppV2Theme.TEXT_SECONDARY);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppV2Theme.BORDER));
        footer.add(lbl, BorderLayout.CENTER);
        return footer;
    }

    private void aplicarConfiguracionInicial() {
        seleccionarEtapaInicial();
        if (perfilRegistroRecepcion) {
            restaurarFechasRegistro();
            EstadoExpedienteComboSupportV2.cargar(
                    cmbEstado,
                    "REGISTRO",
                    new FiltroCatalogoItemV2(null, "Todos los estados"),
                    (codigo, nombre) -> new FiltroCatalogoItemV2(codigo, nombre),
                    ex -> lblResultado.setText("No se pudieron cargar los estados de Registro / Recepción."));
        }
        if (etapaBloqueada) {
            cmbEtapa.setEnabled(false);
            cmbEtapa.setToolTipText("Bandeja filtrada por etapa " + DisplayNameMapperV2.etapa(etapaInicial));
        }
    }

    private void configurarTabla() {
        table.setRowHeight(34);
        rowSorter = new TableRowSorter<DefaultTableModel>(tableModel);
        rowSorter.setSortsOnUpdates(true);
        table.setRowSorter(rowSorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(perfilRegistroRecepcion ? JTable.AUTO_RESIZE_OFF : JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        table.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        table.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        table.setGridColor(AppV2Theme.BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setDefaultRenderer(Object.class, new BandejaCellRenderer());
        AppV2TableColumnSizer.applyFriendlyDefaults(table);
        if (perfilRegistroRecepcion) {
            AppV2TableColumnSizer.applyWidths(table, 88, 165, 150, 145, 220, 130, 130, 260, 190, 0, 0);
            table.getColumnModel().getColumn(0).setMaxWidth(90);
            table.getColumnModel().getColumn(7).setMinWidth(220);
            table.getColumnModel().getColumn(8).setMinWidth(170);
            table.getColumnModel().getColumn(9).setMinWidth(0);
            table.getColumnModel().getColumn(9).setMaxWidth(0);
            table.getColumnModel().getColumn(10).setMinWidth(0);
            table.getColumnModel().getColumn(10).setMaxWidth(0);
            tablePanel.getScrollPane().setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        } else {
            table.getColumnModel().getColumn(0).setMaxWidth(90);
            table.getColumnModel().getColumn(1).setMinWidth(150);
            table.getColumnModel().getColumn(2).setMinWidth(130);
            table.getColumnModel().getColumn(3).setMinWidth(130);
            table.getColumnModel().getColumn(4).setMinWidth(150);
            table.getColumnModel().getColumn(7).setMaxWidth(125);
            table.getColumnModel().getColumn(8).setMaxWidth(120);
        }
        instalarFiltrosPorColumna();
    }

    private void instalarFiltrosPorColumna() {
        columnFilterFields = new JTextField[tableModel.getColumnCount()];
        for (int i = 0; i < columnFilterFields.length; i++) {
            columnFilterFields[i] = crearCampoFiltroColumna(i);
        }
        columnFilterPanel = new ColumnFilterPanel();
        tableHeaderWithFilters = new JPanel(new BorderLayout());
        tableHeaderWithFilters.setOpaque(true);
        tableHeaderWithFilters.setBackground(AppV2Theme.SURFACE_ALT);
        tableHeaderWithFilters.add(table.getTableHeader(), BorderLayout.NORTH);
        tableHeaderWithFilters.add(columnFilterPanel, BorderLayout.CENTER);
        tablePanel.getScrollPane().setColumnHeaderView(tableHeaderWithFilters);
        table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
                refrescarFiltrosPorColumna();
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
                refrescarFiltrosPorColumna();
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                refrescarFiltrosPorColumna();
            }

            @Override
            public void columnMarginChanged(javax.swing.event.ChangeEvent e) {
                refrescarFiltrosPorColumna();
            }

            @Override
            public void columnSelectionChanged(javax.swing.event.ListSelectionEvent e) {
                // No aplica: la fila de filtros solo replica anchos de columnas.
            }
        });
    }

    private JTextField crearCampoFiltroColumna(final int modelColumn) {
        final JTextField field = new JTextField();
        String header = String.valueOf(tableModel.getColumnName(modelColumn));
        boolean visible = !esColumnaTecnica(modelColumn);
        field.setVisible(visible);
        field.setEnabled(visible);
        field.setFont(AppV2Theme.fontPlain(11));
        field.setForeground(AppV2Theme.TEXT_PRIMARY);
        field.setBackground(AppV2Theme.SURFACE);
        field.setCaretColor(AppV2Theme.PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(2, 7, 2, 7)));
        field.setToolTipText("Filtrar " + DisplayNameMapperV2.valor(header));
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltrosPorColumna();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltrosPorColumna();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltrosPorColumna();
            }
        });
        return field;
    }

    private void refrescarFiltrosPorColumna() {
        if (columnFilterPanel != null) {
            columnFilterPanel.revalidate();
            columnFilterPanel.repaint();
        }
        if (tableHeaderWithFilters != null) {
            tableHeaderWithFilters.revalidate();
            tableHeaderWithFilters.repaint();
        }
    }

    private void aplicarFiltrosPorColumna() {
        if (rowSorter == null || columnFilterFields == null) {
            return;
        }
        List<RowFilter<DefaultTableModel, Integer>> filtros = new ArrayList<RowFilter<DefaultTableModel, Integer>>();
        for (int i = 0; i < columnFilterFields.length; i++) {
            final int modelColumn = i;
            JTextField field = columnFilterFields[i];
            if (field == null || !field.isEnabled()) {
                continue;
            }
            String text = field.getText();
            if (text == null || text.trim().isEmpty()) {
                continue;
            }
            final String criterio = normalizarFiltro(text);
            filtros.add(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    Object value = entry.getValue(modelColumn);
                    return normalizarFiltro(valorFiltro(value)).contains(criterio);
                }
            });
        }
        rowSorter.setRowFilter(filtros.isEmpty() ? null : RowFilter.andFilter(filtros));
        btnVerDetalle.setEnabled(table.getSelectedRow() >= 0);
        btnEditar.setEnabled(table.getSelectedRow() >= 0 && esRegistroSeleccionadoEditable());
    }

    private void limpiarFiltrosPorColumna() {
        if (columnFilterFields == null) {
            return;
        }
        for (JTextField field : columnFilterFields) {
            if (field != null && field.getText().length() > 0) {
                field.setText("");
            }
        }
        if (rowSorter != null) {
            rowSorter.setRowFilter(null);
        }
    }

    private boolean esColumnaTecnica(int modelColumn) {
        String name = tableModel.getColumnName(modelColumn);
        return name != null && name.startsWith("_");
    }

    private JLabel crearLabelFiltro(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        label.setForeground(AppV2Theme.TEXT_SECONDARY);
        return label;
    }

    private void configurarControlesFiltro() {
        if (perfilRegistroRecepcion) {
            txtBusqueda.setPlaceholder("Buscar expediente, trámite/SGD, acta, titular o documento");
        }
        txtBusqueda.setColumns(34);
        txtBusqueda.setPreferredSize(new Dimension(perfilRegistroRecepcion ? 460 : 360, 34));
        txtBusqueda.setMinimumSize(new Dimension(perfilRegistroRecepcion ? 340 : 280, 34));

        cmbEtapa.setPreferredSize(new Dimension(190, 34));
        cmbEtapa.setMinimumSize(new Dimension(180, 34));
        cmbEstado.setPreferredSize(new Dimension(240, 34));
        cmbEstado.setMinimumSize(new Dimension(220, 34));

        Dimension fechaSize = new Dimension(180, 40);
        fechaSolicitudDesde.setPreferredSize(fechaSize);
        fechaSolicitudDesde.setMinimumSize(new Dimension(165, 40));
        fechaSolicitudHasta.setPreferredSize(fechaSize);
        fechaSolicitudHasta.setMinimumSize(new Dimension(165, 40));

        Dimension limiteSize = new Dimension(86, 34);
        spnLimite.setPreferredSize(limiteSize);
        spnLimite.setMinimumSize(limiteSize);

        cmbEtapa.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        cmbEstado.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        txtBusqueda.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
    }

    private void configurarBotones() {
        btnBuscar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnVerDetalle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnEditar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnLimpiar.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnVerDetalle.addActionListener(e -> abrirDetalleSeleccionado());
        btnEditar.addActionListener(e -> editarSeleccionado());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean haySeleccion = table.getSelectedRow() >= 0;
                btnVerDetalle.setEnabled(haySeleccion);
                btnEditar.setEnabled(haySeleccion && esRegistroSeleccionadoEditable());
                if (haySeleccion) {
                    mostrarPanelRecepcionSeleccionado();
                } else {
                    ocultarPanelRecepcion();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    abrirDetalleSeleccionado();
                } else if (table.getSelectedRow() >= 0 && splitBandeja != null && !splitBandeja.isSideVisible()) {
                    mostrarPanelRecepcionSeleccionado();
                }
            }
        });
    }

    public void refrescar() {
        buscar();
    }

    private void buscar() {
        String texto = txtBusqueda.getText();
        String etapa = perfilRegistroRecepcion ? "REGISTRO" : codigoSeleccionado(cmbEtapa);
        String estado = codigoSeleccionado(cmbEstado);
        LocalDate fechaDesde = perfilRegistroRecepcion ? fechaSeleccionada(fechaSolicitudDesde) : null;
        LocalDate fechaHasta = perfilRegistroRecepcion ? fechaSeleccionada(fechaSolicitudHasta) : null;
        int limite = ((Number) spnLimite.getValue()).intValue();

        if (fechaDesde != null && fechaHasta != null && fechaHasta.isBefore(fechaDesde)) {
            JOptionPane.showMessageDialog(
                    this,
                    "La fecha solicitud hasta no puede ser menor que la fecha solicitud desde.",
                    "Filtros de Registro / Recepción",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
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
                    cargarTabla(get());
                } catch (Exception ex) {
                    mostrarError(ex);
                } finally {
                    setBuscando(false);
                }
            }
        };
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
        if (perfilRegistroRecepcion) {
            restaurarFechasRegistro();
        } else {
            fechaSolicitudDesde.setDate(null);
            fechaSolicitudHasta.setDate(null);
        }
        spnLimite.setValue(200);
        limpiarFiltrosPorColumna();
        tableModel.setRowCount(0);
        btnVerDetalle.setEnabled(false);
        btnEditar.setEnabled(false);
        ocultarPanelRecepcion();
        tablePanel.setEmpty(true);
        lblResultado.setText(etapaBloqueada
                ? "Filtros limpiados. La bandeja permanece filtrada por la etapa seleccionada."
                : "Filtros limpiados. Presione Buscar para cargar expedientes.");
    }

    private void cargarTabla(List<ExpedienteBandejaDTO> expedientes) {
        ocultarPanelRecepcion();
        tableModel.setRowCount(0);
        btnVerDetalle.setEnabled(false);
        btnEditar.setEnabled(false);
        for (ExpedienteBandejaDTO item : expedientes) {
            if (perfilRegistroRecepcion) {
                tableModel.addRow(new Object[]{
                    item.getDiasRestantes(),
                    item.getNumeroExpediente(),
                    item.getCanal(),
                    fechaSolicitud(item),
                    item.getProcedimiento(),
                    item.getTipoActa(),
                    item.getNumeroActa(),
                    item.getTitular(),
                    item.getGrupoFamiliar(),
                    item.getEstadoCodigo(),
                    item.getIdExpediente()
                });
            } else {
                tableModel.addRow(new Object[]{
                    item.getDiasRestantes(),
                    item.getNumeroExpediente(),
                    item.getNumeroTramiteDocumentario(),
                    DisplayNameMapperV2.etapa(item.getEtapaCodigo()),
                    DisplayNameMapperV2.estado(item.getEstadoCodigo()),
                    fechaRegistro(item),
                    item.getFechaVencimiento(),
                    item.isRequierePublicacion() ? "Requiere" : "No",
                    item.isExpedienteDigitalCompleto() ? "Completo" : "Pendiente",
                    item.getIdExpediente()
                });
            }
        }
        tablePanel.setEmpty(expedientes.isEmpty());
        if (expedientes.isEmpty()) {
            lblResultado.setText("No se encontraron expedientes con los filtros ingresados.");
        } else {
            lblResultado.setText(expedientes.size() + " expediente(s) encontrado(s). Seleccione uno y presione Ver detalle.");
        }
    }

    private void mostrarPanelRecepcionSeleccionado() {
        final Long idExpediente = obtenerIdExpedienteSeleccionado();
        if (idExpediente == null) {
            ocultarPanelRecepcion();
            return;
        }
        if (splitBandeja != null) {
            splitBandeja.setSideVisible(true);
        }
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
    }

    private void cargarPanelRecepcion(ExpedienteConsolaDTO expediente) {
        setValue(lblRecepcionExpediente, expediente.getNumeroExpediente());
        setValue(lblRecepcionFecha, formatDate(expediente.getFechaRecepcion()));
        setValue(lblRecepcionCanal, expediente.getCanalRecepcion());
        setValue(lblRecepcionTipoSolicitud, expediente.getTipoSolicitud());
        setValue(lblRecepcionTramite, expediente.getNumeroTramiteDocumentario());
        setValue(lblRecepcionNumeroExpedienteSgd, expediente.getNumeroExpedienteSgd());
        setValue(lblRecepcionTipoDocumento, expediente.getTipoDocumento());
        setValue(lblRecepcionNumeroDocumento, expediente.getNumeroDocumento());
        setValue(lblRecepcionGrupoFamiliar, expediente.getGrupoFamiliarEstado());
        lblRecepcionGrupoFamiliar.setToolTipText(toolTipGrupoFamiliar(expediente));
        setValue(lblRecepcionProcedimiento, expediente.getProcedimiento());
        setValue(lblRecepcionTitular, expediente.getTitular());
        setValue(lblRecepcionRemitente, expediente.getRemitente());
        setValue(lblRecepcionTipoActa, expediente.getTipoActa());
        setValue(lblRecepcionNumeroActa, expediente.getNumeroActa());
        setValue(lblRecepcionVencimiento, formatDate(expediente.getFechaVencimiento()));
        setValue(lblRecepcionDias, descripcionPlazo(expediente.getDiasRestantes(), expediente.getFechaVencimiento()));
        lblRecepcionDias.setForeground(colorPlazo(expediente.getDiasRestantes()));
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
        setValue(lblRecepcionProcedimiento, "");
        setValue(lblRecepcionTitular, "");
        setValue(lblRecepcionRemitente, "");
        setValue(lblRecepcionTipoActa, "");
        setValue(lblRecepcionNumeroActa, "");
        setValue(lblRecepcionVencimiento, "");
        setValue(lblRecepcionDias, "");
        setValue(lblRecepcionResponsable, "");
        setValue(lblRecepcionEquipo, "");
    }

    private void ocultarPanelRecepcion() {
        panelRecepcionCargado = false;
        idPanelRecepcionActual = null;
        panelRecepcionLoadSequence++;
        tabPanelRecepcion.setExpanded(false);
        tabPanelRecepcion.setToolTipText("Expandir panel de recepción");
        if (splitBandeja != null) {
            splitBandeja.setSideVisible(false);
        }
    }

    private void alternarExpansionPanelRecepcion() {
        if (splitBandeja == null) {
            return;
        }
        boolean expandido = splitBandeja.toggleSideExpanded();
        tabPanelRecepcion.setExpanded(expandido);
        tabPanelRecepcion.setToolTipText(expandido
                ? "Restaurar panel de recepción"
                : "Expandir panel de recepción");
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

    private boolean esRegistroSeleccionadoEditable() {
        if (!perfilRegistroRecepcion || table.getSelectedRow() < 0 || tableModel.getColumnCount() < 10) {
            return false;
        }
        int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
        Object estado = tableModel.getValueAt(modelRow, tableModel.getColumnCount() - 2);
        return estado != null && "REGISTRADO".equalsIgnoreCase(estado.toString().trim());
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

    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : formatDate(dateTime.toLocalDate());
    }

    private static String formatDate(LocalDate value) {
        return value == null ? "" : DATE_FORMAT.format(value);
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

    private static LocalDate fechaRegistro(ExpedienteBandejaDTO item) {
        return item.getFechaRegistro() == null ? null : item.getFechaRegistro().toLocalDate();
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

    private static JLabel valueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
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
        if (dias == null) {
            return AppV2Theme.TEXT_SECONDARY;
        }
        if (dias < 0) {
            return AppV2Theme.ERROR;
        }
        if (dias <= 3) {
            return AppV2Theme.WARNING;
        }
        return AppV2Theme.SUCCESS;
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

    private class ColumnFilterPanel extends JPanel {

        private static final int HEIGHT = 38;

        private ColumnFilterPanel() {
            setLayout(null);
            setOpaque(true);
            setBackground(AppV2Theme.SURFACE_ALT);
            if (columnFilterFields != null) {
                for (JTextField field : columnFilterFields) {
                    add(field);
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            int width = 0;
            for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
                width += table.getColumnModel().getColumn(i).getWidth();
            }
            return new Dimension(Math.max(width, table.getWidth()), HEIGHT);
        }

        @Override
        public void doLayout() {
            int x = 0;
            for (int viewColumn = 0; viewColumn < table.getColumnModel().getColumnCount(); viewColumn++) {
                TableColumn column = table.getColumnModel().getColumn(viewColumn);
                int width = column.getWidth();
                int modelColumn = table.convertColumnIndexToModel(viewColumn);
                JTextField field = modelColumn >= 0 && modelColumn < columnFilterFields.length
                        ? columnFilterFields[modelColumn]
                        : null;
                if (field != null) {
                    boolean visible = !esColumnaTecnica(modelColumn) && width > 18;
                    field.setVisible(visible);
                    if (visible) {
                        field.setBounds(x + 5, 5, Math.max(0, width - 10), 27);
                    } else {
                        field.setBounds(x, 0, 0, 0);
                    }
                }
                x += width;
            }
        }
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
            if (!isSelected && modelColumn == 0) {
                return StatusBadgeV2.forDias(value);
            }
            if (perfilRegistroRecepcion) {
                return defaultComponent(table, value, isSelected, hasFocus, row, column);
            }
            if (!isSelected && modelColumn == 3) {
                return StatusBadgeV2.forEtapa(value == null ? "" : value.toString());
            }
            if (!isSelected && modelColumn == 4) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
            }
            if (!isSelected && modelColumn == 7) {
                String text = value == null ? "" : value.toString();
                if ("Requiere".equalsIgnoreCase(text)) {
                    return new BadgeV2("Requiere", AppV2Theme.SOFT_ORANGE, AppV2Theme.WARNING);
                }
                return new BadgeV2("No", AppV2Theme.SOFT_GRAY, AppV2Theme.TEXT_SECONDARY);
            }
            if (!isSelected && modelColumn == 8) {
                String text = value == null ? "" : value.toString();
                if ("Completo".equalsIgnoreCase(text)) {
                    return new BadgeV2("Completo", AppV2Theme.SOFT_GREEN, AppV2Theme.SUCCESS);
                }
                return new BadgeV2("Pendiente", AppV2Theme.SOFT_GRAY, AppV2Theme.TEXT_SECONDARY);
            }

            return defaultComponent(table, value, isSelected, hasFocus, row, column);
        }

        private Component defaultComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            String display = valorFiltro(value);
            Component c = super.getTableCellRendererComponent(table, display, isSelected, hasFocus, row, column);
            setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setToolTipText(display == null || display.trim().isEmpty() ? null : display);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
                c.setForeground(AppV2Theme.TEXT_PRIMARY);
            }
            return c;
        }
    }
}
