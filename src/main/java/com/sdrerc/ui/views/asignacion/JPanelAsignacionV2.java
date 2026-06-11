package com.sdrerc.ui.views.asignacion;

import com.sdrerc.application.sdrercapp.AsignacionExpedienteService;
import com.sdrerc.application.sdrercapp.UsuarioAsignacionService;
import com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.AsignacionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO;
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
import com.sdrerc.ui.appv2.helpers.FiltroCatalogoItemV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import com.sdrerc.ui.views.expedienteconsola.DlgConsolaExpedienteV2;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class JPanelAsignacionV2 extends JPanel {

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
    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite, titular, acta o documento", 28);
    private final PremiumDateFieldV2 fechaSolicitudDesde = new PremiumDateFieldV2();
    private final PremiumDateFieldV2 fechaSolicitudHasta = new PremiumDateFieldV2();
    private final JComboBox<FiltroCatalogoItemV2> cmbEstado = new JComboBox<FiltroCatalogoItemV2>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnVerDetalle = new JButton("Ver detalle");
    private final JButton btnVerRelacionados = new JButton("Ver relacionados");
    private final JButton btnSeleccionarVisibles = new JButton("Seleccionar visibles");
    private final JButton btnLimpiarSeleccion = new JButton("Limpiar selección");
    private final JButton btnAsignarSeleccionado = new JButton("Asignar expediente");
    private final JButton btnAsignarSeleccionados = new JButton("Asignar seleccionados");
    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar expedientes.");
    private final JLabel lblSeleccionados = new JLabel("0 expedientes seleccionados");
    private final JLabel lblSeleccionadosPanel = new JLabel("0 expedientes seleccionados");
    private final JLabel lblExpedienteSeleccionado = new JLabel("-");
    private final JLabel lblOrigen = new JLabel("Registro / Registrado");
    private final JLabel lblDestino = new JLabel("Asignación / Asignado");
    private final JLabel lblIngreso = new JLabel("Normal");
    private final JLabel lblSupervisor = new JLabel("-");
    private final JLabel lblRelacionados = new JLabel("Sin alerta de relacionados.");
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
    private final MetricCardV2 cardPendientes = new MetricCardV2("Resultados", "0", "Según filtros", AppV2Theme.INFO);
    private final MetricCardV2 cardSeleccionados = new MetricCardV2("Seleccionados", "0", "Listos para asignación", AppV2Theme.TEAL);
    private final MetricCardV2 cardRelacionados = new MetricCardV2("Alertas", "0", "Posibles relacionados", AppV2Theme.WARNING);
    private AppV2SideActionPanel panelAsignacion;
    private AppV2OperationalSplitPanel splitOperativo;
    private Color acentoSeleccion = AppV2Theme.TEAL;
    private Color fondoSeleccion = AppV2Theme.SOFT_BLUE;
    private int filaModeloEnFoco = -1;
    private String contextoChip = "Panel de asignación";
    private boolean panelAsignacionVisible;
    private boolean panelAsignacionCerradoPorUsuario;
    private boolean todasVisiblesSeleccionadas;
    private boolean busquedaInicialEjecutada;

    private boolean cargandoCombos;
    private boolean actualizandoSeleccion;

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
        section.addRow("Relacionados", lblRelacionados);
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
        btnBuscar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnAsignarSeleccionado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnAsignarSeleccionados.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
    }

    private void configurarTabla() {
        table.setRowHeight(34);
        table.setAutoCreateRowSorter(true);
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
        AppV2TableColumnSizer.applyWidths(table, 54, 84, 185, 135, 230, 135, 130, 260, 240, 220, 155, 170, 0);
        configurarColumna(table.getColumnModel().getColumn(0), 54, 54, 58);
        configurarColumna(table.getColumnModel().getColumn(1), 84, 78, 92);
        configurarColumna(table.getColumnModel().getColumn(2), 185, 165, 260);
        configurarColumna(table.getColumnModel().getColumn(3), 135, 128, 170);
        configurarColumna(table.getColumnModel().getColumn(4), 230, 210, 320);
        configurarColumna(table.getColumnModel().getColumn(5), 135, 128, 190);
        configurarColumna(table.getColumnModel().getColumn(6), 130, 120, 180);
        configurarColumna(table.getColumnModel().getColumn(7), 260, 230, 380);
        configurarColumna(table.getColumnModel().getColumn(8), 240, 210, 360);
        configurarColumna(table.getColumnModel().getColumn(9), 220, 190, 320);
        configurarColumna(table.getColumnModel().getColumn(10), 155, 145, 220);
        configurarColumna(table.getColumnModel().getColumn(11), 170, 150, 240);
        table.getColumnModel().getColumn(0).setHeaderRenderer(new SelectAllHeaderRenderer());
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
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 0 && !actualizandoSeleccion) {
                actualizarPanelSeleccion();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                reabrirPanelSiCorresponde(e);
            }
        });
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                if (column >= 0 && table.convertColumnIndexToModel(column) == 0) {
                    alternarSeleccionVisibleDesdeHeader();
                }
            }
        });
    }

    private void cargarEstados() {
        cmbEstado.removeAllItems();
        for (FiltroCatalogoItemV2 item : crearItemsEstado()) {
            cmbEstado.addItem(item);
        }
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
        tableModel.setRowCount(0);
        table.clearSelection();
        int alertas = 0;
        for (AsignacionExpedienteDTO item : expedientes) {
            if (item.getPosiblesRelacionados() > 0) {
                alertas++;
            } else if (!"Normal".equalsIgnoreCase(item.getAlertaIngreso())) {
                alertas++;
            }
            tableModel.addRow(new Object[]{
                Boolean.FALSE,
                item.getDiasDesdeRegistro() == null ? "" : item.getDiasDesdeRegistro(),
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
        cardPendientes.setValue(String.valueOf(items.size()));
        cardRelacionados.setValue(String.valueOf(alertas));
        lblEstado.setText(items.isEmpty()
                ? "No se encontraron expedientes con los filtros ingresados."
                : items.size() + " expediente(s) encontrado(s). Solo REGISTRO / REGISTRADO queda habilitado para asignar.");
        tablePanel.setEmpty(items.isEmpty());
        actualizarPanelSeleccion();
    }

    private String alertaAsignacion(AsignacionExpedienteDTO item) {
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
        Date hoy = fechaComoDate(LocalDate.now());
        fechaSolicitudDesde.setDate(hoy);
        fechaSolicitudHasta.setDate(hoy);
    }

    private static Date fechaComoDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
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
            new FiltroCatalogoItemV2("PARA_FIRMA", "Para firma"),
            new FiltroCatalogoItemV2("FIRMADO", "Firmado"),
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

    private void limpiar() {
        txtBusqueda.setText("");
        restaurarFechasBusqueda();
        cmbEstado.setSelectedIndex(0);
        spnLimite.setValue(200);
        expedientes.clear();
        tableModel.setRowCount(0);
        table.clearSelection();
        tablePanel.setEmpty(true);
        txtComentario.setText("");
        cardPendientes.setValue("0");
        cardRelacionados.setValue("0");
        lblEstado.setText("Filtros limpiados. Presione Buscar para consultar expedientes.");
        actualizarPanelSeleccion();
    }

    private void seleccionarVisibles() {
        if (tableModel.getRowCount() == 0) {
            mostrarInfo("No hay expedientes visibles para seleccionar.");
            return;
        }
        actualizandoSeleccion = true;
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            if (modelRow >= 0 && modelRow < expedientes.size() && expedientes.get(modelRow).isAsignable()) {
                tableModel.setValueAt(Boolean.TRUE, modelRow, 0);
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
            if (modelRow >= 0 && modelRow < expedientes.size() && expedientes.get(modelRow).isAsignable()) {
                tableModel.setValueAt(Boolean.valueOf(seleccionar), modelRow, 0);
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
            tableModel.setValueAt(Boolean.FALSE, i, 0);
        }
        actualizandoSeleccion = false;
        table.clearSelection();
        panelAsignacionCerradoPorUsuario = false;
        actualizarPanelSeleccion();
    }

    private void asignarFilaSeleccionada() {
        Long id = obtenerIdFilaSeleccionada();
        if (id == null) {
            mostrarInfo("Seleccione un expediente para asignar.");
            return;
        }
        int modelRow = obtenerModelRowSeleccionada();
        if (modelRow < 0 || !expedientes.get(modelRow).isAsignable()) {
            mostrarInfo("El expediente ya se encuentra asignado.");
            return;
        }
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        ejecutarAsignacion(ids);
    }

    private void asignarMarcados() {
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
        Long idExpediente = obtenerIdFilaSeleccionada();
        if (idExpediente == null) {
            mostrarInfo("Seleccione un expediente para revisar relacionados.");
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgExpedientesRelacionadosV2 dialog = new DlgExpedientesRelacionadosV2(owner, idExpediente);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        buscar();
    }

    private List<Long> obtenerIdsMarcados() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, 0))) {
                AsignacionExpedienteDTO item = expedientes.get(i);
                if (!item.isAsignable()) {
                    mostrarInfo("El expediente ya se encuentra asignado.");
                    return new ArrayList<Long>();
                }
                ids.add(item.getIdExpediente());
            }
        }
        return ids;
    }

    private Long obtenerIdFilaSeleccionada() {
        int modelRow = obtenerModelRowSeleccionada();
        if (modelRow < 0 || modelRow >= expedientes.size()) {
            return null;
        }
        return expedientes.get(modelRow).getIdExpediente();
    }

    private int obtenerModelRowSeleccionada() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            return -1;
        }
        return table.convertRowIndexToModel(selected);
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
        boolean filaSeleccionada = modelRow >= 0 && modelRow < expedientes.size();
        int seleccionados = marcados > 0 ? marcados : (filaSeleccionada ? 1 : 0);
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

        AsignacionExpedienteDTO item = itemParaPanel(modelRow, marcados);
        if (item != null) {
            aplicarIdentidadVisual(item, false);
            lblExpedienteSeleccionado.setText(item.getNumeroExpediente());
            lblIngreso.setText(item.getAlertaIngreso());
            lblIngreso.setToolTipText(item.getObservacionSolicitud().isEmpty() ? item.getAlertaIngreso() : item.getObservacionSolicitud());
            lblRelacionados.setText(item.getPosiblesRelacionados() > 0
                    ? item.getPosiblesRelacionados() + " posibles relacionados por misma acta y titular."
                    : "Sin alerta de relacionados.");
        } else if (marcados > 1) {
            aplicarIdentidadVisual(null, true);
            lblExpedienteSeleccionado.setText("Selección múltiple");
            lblIngreso.setText("Múltiple");
            lblIngreso.setToolTipText("Asignación múltiple de expedientes marcados.");
            lblRelacionados.setText("Revise relacionados antes de asignar selección múltiple.");
        } else {
            aplicarIdentidadVisual(null, false);
            limpiarPanelAsignacion();
        }
    }

    private AsignacionExpedienteDTO itemParaPanel(int modelRow, int marcados) {
        if (marcados > 1) {
            return null;
        }
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, 0))) {
                return expedientes.get(i);
            }
        }
        return modelRow >= 0 && modelRow < expedientes.size() ? expedientes.get(modelRow) : null;
    }

    private void aplicarIdentidadVisual(AsignacionExpedienteDTO item, boolean multiple) {
        if (item != null) {
            int paletteIndex = indicePaleta(item.getIdExpediente());
            acentoSeleccion = FOCUS_ACCENTS[paletteIndex];
            fondoSeleccion = FOCUS_BACKGROUNDS[paletteIndex];
            filaModeloEnFoco = indiceExpediente(item.getIdExpediente());
            contextoChip = "Expediente en foco: " + item.getNumeroExpediente();
        } else if (multiple) {
            acentoSeleccion = AppV2Theme.INDIGO;
            fondoSeleccion = new Color(238, 240, 250);
            filaModeloEnFoco = -1;
            contextoChip = "Selección múltiple";
        } else {
            acentoSeleccion = AppV2Theme.TEAL;
            fondoSeleccion = AppV2Theme.SOFT_BLUE;
            filaModeloEnFoco = -1;
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

    private int indiceExpediente(Long idExpediente) {
        if (idExpediente == null) {
            return -1;
        }
        for (int i = 0; i < expedientes.size(); i++) {
            if (idExpediente.equals(expedientes.get(i).getIdExpediente())) {
                return i;
            }
        }
        return -1;
    }

    private void limpiarPanelAsignacion() {
        lblExpedienteSeleccionado.setText("-");
        lblIngreso.setText("Normal");
        lblIngreso.setToolTipText(null);
        lblRelacionados.setText("Sin alerta de relacionados.");
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
        return modelRow >= 0 && modelRow < expedientes.size() ? 1 : 0;
    }

    private int contarSeleccionados() {
        int total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, 0))) {
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
            if (modelRow >= 0 && modelRow < expedientes.size() && expedientes.get(modelRow).isAsignable()) {
                visiblesAsignables++;
                if (Boolean.TRUE.equals(tableModel.getValueAt(modelRow, 0))) {
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
        btnSeleccionarVisibles.setEnabled(!trabajando);
        btnLimpiarSeleccion.setEnabled(!trabajando);
        btnAsignarSeleccionado.setEnabled(!trabajando);
        btnAsignarSeleccionados.setEnabled(!trabajando);
        if (mensaje != null) {
            lblEstado.setText(mensaje);
        }
    }

    private void mostrarInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Asignación", JOptionPane.INFORMATION_MESSAGE);
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
            return column == 0
                    && row >= 0
                    && row < expedientes.size()
                    && expedientes.get(row).isAsignable();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : Object.class;
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
            boolean filaEnFoco = modelRow == filaModeloEnFoco;
            if (!isSelected && !filaEnFoco && modelColumn == 1) {
                return StatusBadgeV2.forDias(value);
            }
            if (!isSelected && !filaEnFoco && modelColumn == 10) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
            }
            if (!isSelected && !filaEnFoco && modelColumn == 11) {
                String text = value == null ? "" : value.toString();
                if (!text.startsWith("Sin")) {
                    return new BadgeV2(text, AppV2Theme.SOFT_ORANGE, AppV2Theme.WARNING);
                }
            }
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            if (isSelected || filaEnFoco) {
                c.setBackground(fondoSeleccion);
                c.setForeground(AppV2Theme.TEXT_PRIMARY);
                setBorder(modelColumn == 2
                        ? BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 4, 0, 0, acentoSeleccion),
                                BorderFactory.createEmptyBorder(0, 4, 0, 8))
                        : BorderFactory.createEmptyBorder(0, 8, 0, 8));
            } else {
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                c.setBackground(row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
                c.setForeground(AppV2Theme.TEXT_PRIMARY);
            }
            return c;
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
}
