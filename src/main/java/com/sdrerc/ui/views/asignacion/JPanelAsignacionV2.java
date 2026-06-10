package com.sdrerc.ui.views.asignacion;

import com.sdrerc.application.sdrercapp.AsignacionExpedienteService;
import com.sdrerc.application.sdrercapp.UsuarioAsignacionService;
import com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.AsignacionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO;
import com.sdrerc.ui.appv2.components.AppV2ActionPanel;
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
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import com.sdrerc.ui.views.expedienteconsola.DlgConsolaExpedienteV2;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class JPanelAsignacionV2 extends JPanel {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AsignacionExpedienteService asignacionService;
    private final UsuarioAsignacionService usuarioService;
    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite, titular, acta o documento", 28);
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnVerDetalle = new JButton("Ver detalle");
    private final JButton btnVerRelacionados = new JButton("Ver relacionados");
    private final JButton btnSeleccionarVisibles = new JButton("Seleccionar visibles");
    private final JButton btnLimpiarSeleccion = new JButton("Limpiar selección");
    private final JButton btnAsignarSeleccionado = new JButton("Asignar expediente");
    private final JButton btnAsignarSeleccionados = new JButton("Asignar seleccionados");
    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar expedientes pendientes.");
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
    private final AsignacionTableModel tableModel = new AsignacionTableModel();
    private final JTable table = new AppV2Table(tableModel);
    private final AppV2TablePanel tablePanel = new AppV2TablePanel(
            table,
            "Sin expedientes para mostrar",
            "Seleccione filtros y presione Buscar.");
    private final JPanel panelOperativo = new JPanel(new BorderLayout(14, 14));
    private final List<AsignacionExpedienteDTO> expedientes = new ArrayList<>();
    private final MetricCardV2 cardPendientes = new MetricCardV2("Pendientes", "0", "REGISTRO / REGISTRADO", AppV2Theme.INFO);
    private final MetricCardV2 cardSeleccionados = new MetricCardV2("Seleccionados", "0", "Listos para asignación", AppV2Theme.TEAL);
    private final MetricCardV2 cardRelacionados = new MetricCardV2("Alertas", "0", "Posibles relacionados", AppV2Theme.WARNING);
    private JPanel panelAsignacion;
    private AppV2OperationalSplitPanel splitOperativo;
    private boolean panelAsignacionVisible;
    private boolean panelAsignacionCerradoPorUsuario;

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

    private JPanel crearPanelAsignacion() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de asignación", new Runnable() {
            @Override
            public void run() {
                cerrarPanelAsignacion();
            }
        });
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
        spnLimite.setPreferredSize(new Dimension(88, 34));
        cmbEquipo.setPreferredSize(new Dimension(230, 34));
        cmbAbogado.setPreferredSize(new Dimension(230, 34));
        txtBusqueda.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        cmbEquipo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        cmbAbogado.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        btnBuscar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnAsignarSeleccionado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnAsignarSeleccionados.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
    }

    private void configurarTabla() {
        table.setRowHeight(34);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        table.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        table.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        table.setGridColor(AppV2Theme.BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setDefaultRenderer(Object.class, new AsignacionRenderer());
        AppV2TableColumnSizer.applyFriendlyDefaults(table);
        table.getColumnModel().getColumn(0).setMaxWidth(52);
        table.getColumnModel().getColumn(1).setMinWidth(160);
        table.getColumnModel().getColumn(2).setMinWidth(140);
        table.getColumnModel().getColumn(3).setMinWidth(190);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);
        table.getColumnModel().getColumn(6).setMinWidth(220);
        table.getColumnModel().getColumn(7).setPreferredWidth(130);
        table.getColumnModel().getColumn(8).setMaxWidth(88);
        table.getColumnModel().getColumn(9).setPreferredWidth(130);
        table.getColumnModel().getColumn(10).setPreferredWidth(150);
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
                    setTrabajando(false, "Seleccione filtros y presione Buscar.");
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
        limpiarSeleccion();
        setTrabajando(true, "Consultando expedientes REGISTRO / REGISTRADO...");
        String texto = txtBusqueda.getText();
        int limite = ((Number) spnLimite.getValue()).intValue();
        SwingWorker<List<AsignacionExpedienteDTO>, Void> worker = new SwingWorker<List<AsignacionExpedienteDTO>, Void>() {
            @Override
            protected List<AsignacionExpedienteDTO> doInBackground() throws Exception {
                return asignacionService.buscarPendientes(texto, limite);
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
                item.getNumeroExpediente(),
                documentoTramite(item),
                item.getProcedimiento(),
                item.getTipoActa(),
                item.getNumeroActa(),
                item.getTitular(),
                formatDateTime(item.getFechaRegistro()),
                item.getDiasDesdeRegistro() == null ? "" : item.getDiasDesdeRegistro(),
                DisplayNameMapperV2.estado(item.getEstadoCodigo()),
                alertaAsignacion(item),
                item.getIdExpediente()
            });
        }
        cardPendientes.setValue(String.valueOf(items.size()));
        cardRelacionados.setValue(String.valueOf(alertas));
        lblEstado.setText(items.isEmpty()
                ? "No se encontraron expedientes pendientes de asignación."
                : items.size() + " expediente(s) pendiente(s) encontrados.");
        tablePanel.setEmpty(items.isEmpty());
        actualizarPanelSeleccion();
    }

    private String documentoTramite(AsignacionExpedienteDTO item) {
        if (item.getNumeroDocumentoTitular() != null && !item.getNumeroDocumentoTitular().isEmpty()) {
            return item.getNumeroDocumentoTitular();
        }
        return item.getNumeroTramiteDocumentario();
    }

    private String alertaAsignacion(AsignacionExpedienteDTO item) {
        if (item.getPosiblesRelacionados() > 0) {
            return item.getPosiblesRelacionados() + " relacionados";
        }
        String alerta = item.getAlertaIngreso();
        return "Normal".equalsIgnoreCase(alerta) ? "Sin alerta" : alerta;
    }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME_FORMAT.format(value);
    }

    private void limpiar() {
        txtBusqueda.setText("");
        spnLimite.setValue(200);
        expedientes.clear();
        tableModel.setRowCount(0);
        table.clearSelection();
        tablePanel.setEmpty(true);
        txtComentario.setText("");
        cardPendientes.setValue("0");
        cardRelacionados.setValue("0");
        lblEstado.setText("Filtros limpiados. Presione Buscar para consultar expedientes pendientes.");
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
        if (seleccionados == 0) {
            panelAsignacionCerradoPorUsuario = false;
        }
        actualizarVisibilidadPanelAsignacion(seleccionados > 0 && !panelAsignacionCerradoPorUsuario);

        AsignacionExpedienteDTO item = itemParaPanel(modelRow, marcados);
        if (item != null) {
            lblExpedienteSeleccionado.setText(item.getNumeroExpediente());
            lblIngreso.setText(item.getAlertaIngreso());
            lblIngreso.setToolTipText(item.getObservacionSolicitud().isEmpty() ? item.getAlertaIngreso() : item.getObservacionSolicitud());
            lblRelacionados.setText(item.getPosiblesRelacionados() > 0
                    ? item.getPosiblesRelacionados() + " posibles relacionados por misma acta y titular."
                    : "Sin alerta de relacionados.");
        } else if (marcados > 1) {
            lblExpedienteSeleccionado.setText("Selección múltiple");
            lblIngreso.setText("Múltiple");
            lblIngreso.setToolTipText("Asignación múltiple de expedientes marcados.");
            lblRelacionados.setText("Revise relacionados antes de asignar selección múltiple.");
        } else {
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
                "Sel.",
                "Número expediente",
                "Documento / trámite",
                "Procedimiento",
                "Tipo acta",
                "Nro. acta",
                "Titular",
                "Fecha registro",
                "Días",
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

    private static class AsignacionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            int modelColumn = table.convertColumnIndexToModel(column);
            if (!isSelected && modelColumn == 8) {
                return StatusBadgeV2.forDias(value);
            }
            if (!isSelected && modelColumn == 9) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
            }
            if (!isSelected && modelColumn == 10) {
                String text = value == null ? "" : value.toString();
                if (!text.startsWith("Sin")) {
                    return new BadgeV2(text, AppV2Theme.SOFT_ORANGE, AppV2Theme.WARNING);
                }
            }
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (!isSelected) {
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
