package com.sdrerc.ui.views.administracion.equipojuridico;

import com.sdrerc.application.sdrercapp.EquipoJuridicoService;
import com.sdrerc.domain.dto.sdrercapp.AreaDTO;
import com.sdrerc.domain.dto.sdrercapp.EquipoJuridicoDTO;
import com.sdrerc.domain.dto.sdrercapp.EquipoJuridicoFiltroDTO;
import com.sdrerc.domain.dto.sdrercapp.EquipoJuridicoResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.EquipoMiembroDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableEquipoDTO;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class JPanelEquipoJuridicoV2 extends JPanel {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final EquipoJuridicoService equipoService;

    private final JTextField txtBusqueda = new JTextField(24);
    private final JComboBox<EstadoFiltroItem> cmbEstado = new JComboBox<EstadoFiltroItem>();
    private final JComboBox<AreaItem> cmbAreaFiltro = new JComboBox<AreaItem>();
    private final JComboBox<UsuarioItem> cmbSupervisorFiltro = new JComboBox<UsuarioItem>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(300, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnNuevo = new JButton("Nuevo equipo");
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnActivarInactivar = new JButton("Activar / Inactivar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnVerMiembros = new JButton("Ver miembros");
    private final JButton btnGuardar = new JButton("Guardar equipo");
    private final JButton btnCancelar = new JButton("Cancelar");
    private final JButton btnAgregarMiembro = new JButton("Agregar miembro");
    private final JButton btnQuitarMiembro = new JButton("Quitar miembro");
    private final JButton btnMarcarResponsable = new JButton("Marcar responsable");

    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar equipos.");
    private final JLabel lblEquipoSeleccionado = new JLabel("Sin equipo seleccionado");
    private final JTextField txtCodigo = new JTextField(20);
    private final JTextField txtNombre = new JTextField(22);
    private final JTextArea txtDescripcion = new JTextArea(4, 22);
    private final JComboBox<AreaItem> cmbArea = new JComboBox<AreaItem>();
    private final JComboBox<UsuarioItem> cmbResponsable = new JComboBox<UsuarioItem>();
    private final JComboBox<UsuarioItem> cmbUsuarioMiembro = new JComboBox<UsuarioItem>();
    private final JCheckBox chkActivo = new JCheckBox("Equipo activo");

    private final EquiposTableModel equiposModel = new EquiposTableModel();
    private final JTable tblEquipos = new JTable(equiposModel);
    private final MiembrosTableModel miembrosModel = new MiembrosTableModel();
    private final JTable tblMiembros = new JTable(miembrosModel);
    private final JTabbedPane tabsDetalle = new JTabbedPane();

    private final MetricCardV2 cardEquipos = new MetricCardV2("Equipos", "0", "Resultado de búsqueda", AppV2Theme.PRIMARY);
    private final MetricCardV2 cardActivos = new MetricCardV2("Activos", "0", "Estructura habilitada", AppV2Theme.SUCCESS);
    private final MetricCardV2 cardMiembros = new MetricCardV2("Miembros", "0", "Miembros activos visibles", AppV2Theme.TEAL);

    private final List<EquipoJuridicoDTO> equipos = new ArrayList<>();
    private final List<EquipoMiembroDTO> miembros = new ArrayList<>();
    private final List<AreaDTO> areas = new ArrayList<>();
    private final List<UsuarioAsignableEquipoDTO> usuariosAsignables = new ArrayList<>();
    private final List<UsuarioAsignableEquipoDTO> supervisoresAsignables = new ArrayList<>();
    private Long idEquipoEditando;
    private boolean cargandoFormulario;

    public JPanelEquipoJuridicoV2() {
        this(new EquipoJuridicoService());
    }

    public JPanelEquipoJuridicoV2(EquipoJuridicoService equipoService) {
        this.equipoService = equipoService;
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());
        add(crearMetricas(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        configurarControles();
        configurarTablas();
        configurarEventos();
        cargarCatalogosYEquipos();
    }

    private JPanel crearMetricas() {
        JPanel metricas = new JPanel(new GridLayout(1, 3, 12, 0));
        metricas.setOpaque(false);
        metricas.add(cardEquipos);
        metricas.add(cardActivos);
        metricas.add(cardMiembros);
        return metricas;
    }

    private Component crearCentro() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, crearPanelListado(), crearPanelDetalle());
        split.setBorder(null);
        split.setResizeWeight(0.66);
        split.setDividerSize(8);
        split.setOpaque(false);
        return split;
    }

    private JPanel crearPanelListado() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JPanel filtros = new JPanel(new GridBagLayout());
        filtros.setBackground(AppV2Theme.SURFACE);
        filtros.setBorder(AppV2Theme.toolbarBorder());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        filtros.add(label("Buscar"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtros.add(txtBusqueda, gbc);
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 2;
        filtros.add(label("Estado"), gbc);
        gbc.gridx = 3;
        filtros.add(cmbEstado, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        filtros.add(label("Área"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtros.add(cmbAreaFiltro, gbc);
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 2;
        filtros.add(label("Responsable"), gbc);
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtros.add(cmbSupervisorFiltro, gbc);
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 4;
        gbc.gridy = 0;
        filtros.add(label("Mostrar"), gbc);
        gbc.gridx = 5;
        filtros.add(spnLimite, gbc);

        JPanel accionesFiltro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accionesFiltro.setOpaque(false);
        accionesFiltro.add(btnBuscar);
        accionesFiltro.add(btnLimpiar);
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        filtros.add(accionesFiltro, gbc);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnNuevo);
        acciones.add(btnEditar);
        acciones.add(btnActivarInactivar);
        acciones.add(btnVerMiembros);
        acciones.add(btnRefrescar);

        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel barra = new JPanel(new BorderLayout(8, 8));
        barra.setOpaque(false);
        barra.add(filtros, BorderLayout.NORTH);
        barra.add(acciones, BorderLayout.CENTER);
        barra.add(lblEstado, BorderLayout.SOUTH);

        panel.add(barra, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblEquipos), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelDetalle() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(460, 0));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.sectionBorder());

        JLabel title = new JLabel("Detalle del equipo");
        title.setFont(AppV2Theme.fontBold(18));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);
        lblEquipoSeleccionado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEquipoSeleccionado.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(lblEquipoSeleccionado, BorderLayout.CENTER);

        tabsDetalle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tabsDetalle.addTab("Datos", crearFormularioEquipo());
        tabsDetalle.addTab("Miembros", crearPanelMiembros());

        panel.add(header, BorderLayout.NORTH);
        panel.add(tabsDetalle, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormularioEquipo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        int row = 0;
        agregarFila(form, row++, "Código", txtCodigo);
        agregarFila(form, row++, "Nombre", txtNombre);
        agregarFila(form, row++, "Descripción", new JScrollPane(txtDescripcion));
        agregarFila(form, row++, "Área", cmbArea);
        agregarFila(form, row++, "Responsable", cmbResponsable);
        agregarFila(form, row, "Estado", chkActivo);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnCancelar);
        acciones.add(btnGuardar);

        JLabel nota = new JLabel("El responsable se guarda como miembro responsable del equipo.");
        nota.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        nota.setForeground(AppV2Theme.TEXT_SECONDARY);

        panel.add(form, BorderLayout.NORTH);
        panel.add(nota, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelMiembros() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JPanel alta = new JPanel(new GridBagLayout());
        alta.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        alta.add(label("Usuario"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        alta.add(cmbUsuarioMiembro, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        alta.add(btnAgregarMiembro, gbc);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnQuitarMiembro);
        acciones.add(btnMarcarResponsable);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setOpaque(false);
        top.add(alta, BorderLayout.NORTH);
        top.add(acciones, BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblMiembros), BorderLayout.CENTER);
        return panel;
    }

    private void configurarControles() {
        cmbEstado.addItem(new EstadoFiltroItem("Todos", null));
        cmbEstado.addItem(new EstadoFiltroItem("Activos", Boolean.TRUE));
        cmbEstado.addItem(new EstadoFiltroItem("Inactivos", Boolean.FALSE));
        chkActivo.setOpaque(false);
        chkActivo.setSelected(true);
        txtCodigo.setToolTipText("Mayúsculas, números y guion bajo. Ejemplo: EQUIPO_ANALISIS");
        btnEditar.setEnabled(false);
        btnActivarInactivar.setEnabled(false);
        btnVerMiembros.setEnabled(false);
        btnAgregarMiembro.setEnabled(false);
        btnQuitarMiembro.setEnabled(false);
        btnMarcarResponsable.setEnabled(false);
    }

    private void configurarTablas() {
        tblEquipos.setModel(equiposModel);
        tblEquipos.setRowHeight(30);
        tblEquipos.setFillsViewportHeight(true);
        tblEquipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblEquipos.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tblEquipos.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        tblEquipos.getColumnModel().getColumn(0).setPreferredWidth(54);
        tblEquipos.getColumnModel().getColumn(1).setPreferredWidth(128);
        tblEquipos.getColumnModel().getColumn(2).setPreferredWidth(170);
        tblEquipos.getColumnModel().getColumn(3).setPreferredWidth(120);
        tblEquipos.getColumnModel().getColumn(4).setPreferredWidth(160);
        tblEquipos.getColumnModel().getColumn(5).setPreferredWidth(82);
        tblEquipos.getColumnModel().getColumn(6).setPreferredWidth(76);
        tblEquipos.getColumnModel().getColumn(7).setPreferredWidth(80);
        tblEquipos.getColumnModel().getColumn(5).setCellRenderer(new EstadoRenderer());

        tblMiembros.setModel(miembrosModel);
        tblMiembros.setRowHeight(28);
        tblMiembros.setFillsViewportHeight(true);
        tblMiembros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblMiembros.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tblMiembros.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        tblMiembros.getColumnModel().getColumn(0).setPreferredWidth(110);
        tblMiembros.getColumnModel().getColumn(1).setPreferredWidth(170);
        tblMiembros.getColumnModel().getColumn(2).setPreferredWidth(130);
        tblMiembros.getColumnModel().getColumn(3).setPreferredWidth(85);
        tblMiembros.getColumnModel().getColumn(4).setPreferredWidth(92);
        tblMiembros.getColumnModel().getColumn(5).setPreferredWidth(76);
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> buscarEquipos(null));
        btnLimpiar.addActionListener(e -> limpiarFiltros());
        btnRefrescar.addActionListener(e -> buscarEquipos(idEquipoEditando));
        btnNuevo.addActionListener(e -> nuevoEquipo());
        btnEditar.addActionListener(e -> editarSeleccionado());
        btnActivarInactivar.addActionListener(e -> cambiarEstadoEquipo());
        btnVerMiembros.addActionListener(e -> tabsDetalle.setSelectedIndex(1));
        btnGuardar.addActionListener(e -> guardarEquipo());
        btnCancelar.addActionListener(e -> llenarFormulario(obtenerEquipoSeleccionado()));
        btnAgregarMiembro.addActionListener(e -> agregarMiembro());
        btnQuitarMiembro.addActionListener(e -> quitarMiembro());
        btnMarcarResponsable.addActionListener(e -> marcarResponsable());

        tblEquipos.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    EquipoJuridicoDTO equipo = obtenerEquipoSeleccionado();
                    llenarFormulario(equipo);
                    cargarMiembros(equipo == null ? null : equipo.getIdEquipo());
                    actualizarBotonesSeleccion();
                }
            }
        });

        tblMiembros.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    actualizarBotonesMiembros();
                }
            }
        });
    }

    private void cargarCatalogosYEquipos() {
        setBusy(true);
        new SwingWorker<DatosIniciales, Void>() {
            @Override
            protected DatosIniciales doInBackground() throws Exception {
                DatosIniciales datos = new DatosIniciales();
                datos.areas = equipoService.listarAreasActivas();
                datos.usuarios = equipoService.listarUsuariosAsignables();
                datos.supervisores = equipoService.listarSupervisoresAsignables();
                datos.equipos = equipoService.buscar(crearFiltro());
                return datos;
            }

            @Override
            protected void done() {
                try {
                    DatosIniciales datos = get();
                    areas.clear();
                    areas.addAll(datos.areas);
                    usuariosAsignables.clear();
                    usuariosAsignables.addAll(datos.usuarios);
                    supervisoresAsignables.clear();
                    supervisoresAsignables.addAll(datos.supervisores);
                    cargarCombosCatalogo();
                    actualizarEquipos(datos.equipos, null);
                    lblEstado.setText("Catálogos cargados. " + equipos.size() + " equipos encontrados.");
                } catch (Exception ex) {
                    mostrarError("No se pudo cargar Equipo Jurídico.", ex);
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void buscarEquipos(final Long idSeleccionar) {
        setBusy(true);
        new SwingWorker<List<EquipoJuridicoDTO>, Void>() {
            @Override
            protected List<EquipoJuridicoDTO> doInBackground() throws Exception {
                return equipoService.buscar(crearFiltro());
            }

            @Override
            protected void done() {
                try {
                    actualizarEquipos(get(), idSeleccionar);
                    lblEstado.setText(equipos.size() + " equipos encontrados.");
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar equipos.", ex);
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private EquipoJuridicoFiltroDTO crearFiltro() {
        EstadoFiltroItem estado = (EstadoFiltroItem) cmbEstado.getSelectedItem();
        AreaItem area = (AreaItem) cmbAreaFiltro.getSelectedItem();
        UsuarioItem supervisor = (UsuarioItem) cmbSupervisorFiltro.getSelectedItem();
        return new EquipoJuridicoFiltroDTO(
                txtBusqueda.getText(),
                estado == null ? null : estado.getActivo(),
                area == null ? null : area.getIdArea(),
                supervisor == null ? null : supervisor.getIdUsuario(),
                ((Number) spnLimite.getValue()).intValue());
    }

    private void cargarCombosCatalogo() {
        cmbAreaFiltro.removeAllItems();
        cmbAreaFiltro.addItem(AreaItem.todos());
        cmbArea.removeAllItems();
        cmbArea.addItem(AreaItem.sinArea());
        for (AreaDTO area : areas) {
            AreaItem item = new AreaItem(area);
            cmbAreaFiltro.addItem(item);
            cmbArea.addItem(item);
        }

        cmbSupervisorFiltro.removeAllItems();
        cmbSupervisorFiltro.addItem(UsuarioItem.todos());
        cmbResponsable.removeAllItems();
        cmbResponsable.addItem(UsuarioItem.sinResponsable());
        for (UsuarioAsignableEquipoDTO supervisor : supervisoresAsignables) {
            UsuarioItem item = new UsuarioItem(supervisor);
            cmbSupervisorFiltro.addItem(item);
            cmbResponsable.addItem(item);
        }

        cmbUsuarioMiembro.removeAllItems();
        cmbUsuarioMiembro.addItem(UsuarioItem.seleccione());
        for (UsuarioAsignableEquipoDTO usuario : usuariosAsignables) {
            cmbUsuarioMiembro.addItem(new UsuarioItem(usuario));
        }
    }

    private void actualizarEquipos(List<EquipoJuridicoDTO> nuevosEquipos, Long idSeleccionar) {
        equipos.clear();
        if (nuevosEquipos != null) {
            equipos.addAll(nuevosEquipos);
        }
        equiposModel.fireTableDataChanged();
        actualizarMetricas();
        if (idSeleccionar != null && seleccionarEquipoPorId(idSeleccionar)) {
            return;
        }
        if (!equipos.isEmpty()) {
            tblEquipos.setRowSelectionInterval(0, 0);
        } else {
            limpiarFormulario(null);
            miembros.clear();
            miembrosModel.fireTableDataChanged();
        }
        actualizarBotonesSeleccion();
    }

    private void actualizarMetricas() {
        int activos = 0;
        int totalMiembros = 0;
        for (EquipoJuridicoDTO equipo : equipos) {
            if (equipo.isActivo()) {
                activos++;
            }
            totalMiembros += equipo.getMiembrosActivos();
        }
        cardEquipos.setValue(String.valueOf(equipos.size()));
        cardActivos.setValue(String.valueOf(activos));
        cardMiembros.setValue(String.valueOf(totalMiembros));
    }

    private boolean seleccionarEquipoPorId(Long idEquipo) {
        for (int i = 0; i < equipos.size(); i++) {
            if (idEquipo.equals(equipos.get(i).getIdEquipo())) {
                tblEquipos.setRowSelectionInterval(i, i);
                return true;
            }
        }
        return false;
    }

    private void cargarMiembros(final Long idEquipo) {
        miembros.clear();
        miembrosModel.fireTableDataChanged();
        if (idEquipo == null) {
            actualizarBotonesMiembros();
            return;
        }
        new SwingWorker<List<EquipoMiembroDTO>, Void>() {
            @Override
            protected List<EquipoMiembroDTO> doInBackground() throws Exception {
                return equipoService.listarMiembros(idEquipo);
            }

            @Override
            protected void done() {
                try {
                    miembros.clear();
                    miembros.addAll(get());
                    miembrosModel.fireTableDataChanged();
                } catch (Exception ex) {
                    mostrarError("No se pudo cargar miembros del equipo.", ex);
                } finally {
                    actualizarBotonesMiembros();
                }
            }
        }.execute();
    }

    private void nuevoEquipo() {
        tblEquipos.clearSelection();
        limpiarFormulario(new EquipoJuridicoDTO());
        idEquipoEditando = null;
        miembros.clear();
        miembrosModel.fireTableDataChanged();
        tabsDetalle.setSelectedIndex(0);
        txtCodigo.requestFocusInWindow();
        actualizarBotonesSeleccion();
    }

    private void editarSeleccionado() {
        EquipoJuridicoDTO equipo = obtenerEquipoSeleccionado();
        if (equipo == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un equipo.", "Equipo Jurídico", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        tabsDetalle.setSelectedIndex(0);
        txtNombre.requestFocusInWindow();
    }

    private void llenarFormulario(EquipoJuridicoDTO equipo) {
        if (equipo == null) {
            limpiarFormulario(null);
            return;
        }
        limpiarFormulario(equipo);
    }

    private void limpiarFormulario(EquipoJuridicoDTO equipo) {
        cargandoFormulario = true;
        try {
            EquipoJuridicoDTO data = equipo == null ? new EquipoJuridicoDTO() : equipo;
            idEquipoEditando = data.getIdEquipo();
            txtCodigo.setText(nullToEmpty(data.getCodigo()));
            txtNombre.setText(nullToEmpty(data.getNombre()));
            txtDescripcion.setText(nullToEmpty(data.getDescripcion()));
            chkActivo.setSelected(data.isActivo());
            seleccionarArea(data.getIdArea());
            seleccionarResponsable(data);
            if (data.getIdEquipo() == null) {
                lblEquipoSeleccionado.setText("Nuevo equipo");
            } else {
                lblEquipoSeleccionado.setText(data.getCodigo() + " · " + data.getNombre());
            }
        } finally {
            cargandoFormulario = false;
        }
    }

    private void seleccionarArea(Long idArea) {
        for (int i = 0; i < cmbArea.getItemCount(); i++) {
            AreaItem item = cmbArea.getItemAt(i);
            if ((idArea == null && item.getIdArea() == null) || (idArea != null && idArea.equals(item.getIdArea()))) {
                cmbArea.setSelectedIndex(i);
                return;
            }
        }
        cmbArea.setSelectedIndex(0);
    }

    private void seleccionarResponsable(EquipoJuridicoDTO equipo) {
        Long idResponsable = equipo == null ? null : equipo.getIdResponsable();
        for (int i = 0; i < cmbResponsable.getItemCount(); i++) {
            UsuarioItem item = cmbResponsable.getItemAt(i);
            if ((idResponsable == null && item.getIdUsuario() == null)
                    || (idResponsable != null && idResponsable.equals(item.getIdUsuario()))) {
                cmbResponsable.setSelectedIndex(i);
                return;
            }
        }
        if (idResponsable != null) {
            UsuarioItem item = UsuarioItem.actual(idResponsable, equipo.getResponsableNombre());
            cmbResponsable.addItem(item);
            cmbResponsable.setSelectedItem(item);
        } else {
            cmbResponsable.setSelectedIndex(0);
        }
    }

    private void guardarEquipo() {
        if (cargandoFormulario) {
            return;
        }
        EquipoJuridicoDTO equipo = new EquipoJuridicoDTO();
        equipo.setIdEquipo(idEquipoEditando);
        equipo.setCodigo(txtCodigo.getText());
        equipo.setNombre(txtNombre.getText());
        equipo.setDescripcion(txtDescripcion.getText());
        equipo.setActivo(chkActivo.isSelected());
        AreaItem area = (AreaItem) cmbArea.getSelectedItem();
        equipo.setIdArea(area == null ? null : area.getIdArea());
        UsuarioItem responsable = (UsuarioItem) cmbResponsable.getSelectedItem();
        Long idResponsable = responsable == null ? null : responsable.getIdUsuario();

        setBusy(true);
        new SwingWorker<EquipoJuridicoResultadoDTO, Void>() {
            @Override
            protected EquipoJuridicoResultadoDTO doInBackground() throws Exception {
                return equipoService.guardar(equipo, idResponsable);
            }

            @Override
            protected void done() {
                try {
                    EquipoJuridicoResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelEquipoJuridicoV2.this,
                            resultado.getMensaje(),
                            "Equipo Jurídico",
                            JOptionPane.INFORMATION_MESSAGE);
                    Long id = resultado.getEquipo() == null ? null : resultado.getEquipo().getIdEquipo();
                    buscarEquipos(id);
                } catch (Exception ex) {
                    mostrarError("No se pudo guardar el equipo.", ex);
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void cambiarEstadoEquipo() {
        final EquipoJuridicoDTO equipo = obtenerEquipoSeleccionado();
        if (equipo == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un equipo.", "Equipo Jurídico", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        final boolean nuevoActivo = !equipo.isActivo();
        String accion = nuevoActivo ? "activar" : "inactivar";
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Se va a " + accion + " el equipo " + equipo.getNombre() + ". ¿Desea continuar?",
                "Confirmar cambio",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        setBusy(true);
        new SwingWorker<EquipoJuridicoResultadoDTO, Void>() {
            @Override
            protected EquipoJuridicoResultadoDTO doInBackground() throws Exception {
                return equipoService.cambiarActivo(equipo.getIdEquipo(), nuevoActivo);
            }

            @Override
            protected void done() {
                try {
                    EquipoJuridicoResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelEquipoJuridicoV2.this,
                            resultado.getMensaje(),
                            "Equipo Jurídico",
                            JOptionPane.INFORMATION_MESSAGE);
                    buscarEquipos(equipo.getIdEquipo());
                } catch (Exception ex) {
                    mostrarError("No se pudo cambiar el estado del equipo.", ex);
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void agregarMiembro() {
        EquipoJuridicoDTO equipo = obtenerEquipoActualParaMiembros();
        UsuarioItem usuario = (UsuarioItem) cmbUsuarioMiembro.getSelectedItem();
        if (equipo == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un equipo.", "Equipo Jurídico", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (usuario == null || usuario.getIdUsuario() == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario para agregar.", "Equipo Jurídico", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        setBusy(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                equipoService.agregarMiembro(equipo.getIdEquipo(), usuario.getIdUsuario());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    lblEstado.setText("Miembro agregado correctamente.");
                    buscarEquipos(equipo.getIdEquipo());
                } catch (Exception ex) {
                    mostrarError("No se pudo agregar el miembro.", ex);
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void quitarMiembro() {
        EquipoJuridicoDTO equipo = obtenerEquipoActualParaMiembros();
        EquipoMiembroDTO miembro = obtenerMiembroSeleccionado();
        if (equipo == null || miembro == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un miembro del equipo.", "Equipo Jurídico", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Se retirará a " + miembro.getNombreCompleto() + " del equipo. ¿Desea continuar?",
                "Confirmar retiro",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        setBusy(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                equipoService.quitarMiembro(equipo.getIdEquipo(), miembro.getIdUsuario());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    lblEstado.setText("Miembro retirado correctamente.");
                    buscarEquipos(equipo.getIdEquipo());
                } catch (Exception ex) {
                    mostrarError("No se pudo retirar el miembro.", ex);
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void marcarResponsable() {
        EquipoJuridicoDTO equipo = obtenerEquipoActualParaMiembros();
        EquipoMiembroDTO miembro = obtenerMiembroSeleccionado();
        if (equipo == null || miembro == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un miembro del equipo.", "Equipo Jurídico", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        setBusy(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                equipoService.marcarResponsable(equipo.getIdEquipo(), miembro.getIdUsuario());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    lblEstado.setText("Responsable actualizado correctamente.");
                    buscarEquipos(equipo.getIdEquipo());
                } catch (Exception ex) {
                    mostrarError("No se pudo marcar responsable.", ex);
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private EquipoJuridicoDTO obtenerEquipoActualParaMiembros() {
        EquipoJuridicoDTO equipo = obtenerEquipoSeleccionado();
        if (equipo != null) {
            return equipo;
        }
        if (idEquipoEditando == null) {
            return null;
        }
        for (EquipoJuridicoDTO item : equipos) {
            if (idEquipoEditando.equals(item.getIdEquipo())) {
                return item;
            }
        }
        return null;
    }

    private EquipoJuridicoDTO obtenerEquipoSeleccionado() {
        int row = tblEquipos.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int modelRow = tblEquipos.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= equipos.size()) {
            return null;
        }
        return equipos.get(modelRow);
    }

    private EquipoMiembroDTO obtenerMiembroSeleccionado() {
        int row = tblMiembros.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int modelRow = tblMiembros.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= miembros.size()) {
            return null;
        }
        return miembros.get(modelRow);
    }

    private void actualizarBotonesSeleccion() {
        EquipoJuridicoDTO equipo = obtenerEquipoSeleccionado();
        boolean seleccionado = equipo != null;
        btnEditar.setEnabled(seleccionado);
        btnActivarInactivar.setEnabled(seleccionado);
        btnVerMiembros.setEnabled(seleccionado);
        btnAgregarMiembro.setEnabled(seleccionado);
    }

    private void actualizarBotonesMiembros() {
        boolean hayEquipo = obtenerEquipoActualParaMiembros() != null;
        boolean hayMiembro = obtenerMiembroSeleccionado() != null;
        btnAgregarMiembro.setEnabled(hayEquipo);
        btnQuitarMiembro.setEnabled(hayEquipo && hayMiembro);
        btnMarcarResponsable.setEnabled(hayEquipo && hayMiembro);
    }

    private void limpiarFiltros() {
        txtBusqueda.setText("");
        cmbEstado.setSelectedIndex(0);
        if (cmbAreaFiltro.getItemCount() > 0) {
            cmbAreaFiltro.setSelectedIndex(0);
        }
        if (cmbSupervisorFiltro.getItemCount() > 0) {
            cmbSupervisorFiltro.setSelectedIndex(0);
        }
        spnLimite.setValue(300);
        buscarEquipos(null);
    }

    private void setBusy(boolean busy) {
        btnBuscar.setEnabled(!busy);
        btnLimpiar.setEnabled(!busy);
        btnNuevo.setEnabled(!busy);
        btnGuardar.setEnabled(!busy);
        btnCancelar.setEnabled(!busy);
        btnRefrescar.setEnabled(!busy);
        btnActivarInactivar.setEnabled(!busy && obtenerEquipoSeleccionado() != null);
        btnEditar.setEnabled(!busy && obtenerEquipoSeleccionado() != null);
        btnVerMiembros.setEnabled(!busy && obtenerEquipoSeleccionado() != null);
        btnAgregarMiembro.setEnabled(!busy && obtenerEquipoActualParaMiembros() != null);
        btnQuitarMiembro.setEnabled(!busy && obtenerMiembroSeleccionado() != null);
        btnMarcarResponsable.setEnabled(!busy && obtenerMiembroSeleccionado() != null);
        lblEstado.setText(busy ? "Procesando..." : lblEstado.getText());
    }

    private void mostrarError(String contexto, Exception ex) {
        Throwable causa = ex;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        String mensaje = causa.getMessage() == null || causa.getMessage().trim().isEmpty()
                ? contexto
                : causa.getMessage();
        lblEstado.setText(mensaje);
        JOptionPane.showMessageDialog(this, mensaje, "Equipo Jurídico", JOptionPane.WARNING_MESSAGE);
    }

    private void agregarFila(JPanel form, int row, String texto, Component campo) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = row;
        form.add(label(texto), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(campo, gbc);
    }

    private JLabel label(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        label.setForeground(AppV2Theme.TEXT_SECONDARY);
        return label;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String formatDate(LocalDateTime value) {
        return value == null ? "-" : DATE_TIME_FORMAT.format(value);
    }

    private static class DatosIniciales {
        private List<AreaDTO> areas;
        private List<UsuarioAsignableEquipoDTO> usuarios;
        private List<UsuarioAsignableEquipoDTO> supervisores;
        private List<EquipoJuridicoDTO> equipos;
    }

    private static class EstadoFiltroItem {
        private final String label;
        private final Boolean activo;

        private EstadoFiltroItem(String label, Boolean activo) {
            this.label = label;
            this.activo = activo;
        }

        private Boolean getActivo() {
            return activo;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class AreaItem {
        private final Long idArea;
        private final String label;

        private AreaItem(Long idArea, String label) {
            this.idArea = idArea;
            this.label = label;
        }

        private AreaItem(AreaDTO area) {
            this(area.getIdArea(), area.toString());
        }

        private static AreaItem todos() {
            return new AreaItem(null, "Todas");
        }

        private static AreaItem sinArea() {
            return new AreaItem(null, "Sin área asignada");
        }

        private Long getIdArea() {
            return idArea;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class UsuarioItem {
        private final Long idUsuario;
        private final String label;

        private UsuarioItem(Long idUsuario, String label) {
            this.idUsuario = idUsuario;
            this.label = label;
        }

        private UsuarioItem(UsuarioAsignableEquipoDTO usuario) {
            this(usuario.getIdUsuario(), usuario.toString());
        }

        private static UsuarioItem todos() {
            return new UsuarioItem(null, "Todos");
        }

        private static UsuarioItem seleccione() {
            return new UsuarioItem(null, "Seleccione usuario");
        }

        private static UsuarioItem sinResponsable() {
            return new UsuarioItem(null, "Sin responsable definido");
        }

        private static UsuarioItem actual(Long idUsuario, String nombre) {
            return new UsuarioItem(idUsuario, (nombre == null || nombre.trim().isEmpty() ? "Responsable actual" : nombre) + " (actual)");
        }

        private Long getIdUsuario() {
            return idUsuario;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private class EquiposTableModel extends AbstractTableModel {

        private final String[] columns = {
            "ID", "Código", "Nombre", "Área", "Responsable", "Estado", "Miembros", "Abogados", "Creado", "Modificado"
        };

        @Override
        public int getRowCount() {
            return equipos.size();
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
            EquipoJuridicoDTO equipo = equipos.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return equipo.getIdEquipo();
                case 1:
                    return equipo.getCodigo();
                case 2:
                    return equipo.getNombre();
                case 3:
                    return nullToEmpty(equipo.getAreaNombre());
                case 4:
                    return nullToEmpty(equipo.getResponsableNombre());
                case 5:
                    return equipo.isActivo() ? "Activo" : "Inactivo";
                case 6:
                    return equipo.getMiembrosActivos();
                case 7:
                    return equipo.getAbogadosActivos();
                case 8:
                    return formatDate(equipo.getCreadoEn());
                case 9:
                    return formatDate(equipo.getModificadoEn());
                default:
                    return "";
            }
        }
    }

    private class MiembrosTableModel extends AbstractTableModel {

        private final String[] columns = {
            "Usuario", "Nombres", "Rol", "Área", "Responsable", "Estado", "Desde"
        };

        @Override
        public int getRowCount() {
            return miembros.size();
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
            EquipoMiembroDTO miembro = miembros.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return miembro.getUsername();
                case 1:
                    return miembro.getNombreCompleto();
                case 2:
                    return nullToEmpty(miembro.getRolesResumen());
                case 3:
                    return nullToEmpty(miembro.getAreaNombre());
                case 4:
                    return miembro.isResponsable() ? "Sí" : "No";
                case 5:
                    return miembro.isUsuarioActivo() ? "Activo" : "Inactivo";
                case 6:
                    return formatDate(miembro.getCreadoEn());
                default:
                    return "";
            }
        }
    }

    private static class EstadoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (!isSelected) {
                String text = value == null ? "" : value.toString();
                if ("Activo".equalsIgnoreCase(text)) {
                    component.setBackground(AppV2Theme.SOFT_GREEN);
                    component.setForeground(AppV2Theme.SUCCESS);
                } else {
                    component.setBackground(AppV2Theme.SOFT_ORANGE);
                    component.setForeground(AppV2Theme.WARNING);
                }
            }
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            return component;
        }
    }
}
