package com.sdrerc.ui.views.administracion.usuarios;

import com.sdrerc.application.sdrercapp.UsuarioService;
import com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO;
import com.sdrerc.domain.dto.sdrercapp.RolDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioFiltroDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioResultadoDTO;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2ColumnFilterSupport;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class JPanelUsuariosV2 extends JPanel {

    private final UsuarioService usuarioService;

    private final JTextField txtBusqueda = new JTextField(24);
    private final JComboBox<EstadoFiltroItem> cmbEstado = new JComboBox<EstadoFiltroItem>();
    private final JComboBox<RolItem> cmbRolFiltro = new JComboBox<RolItem>();
    private final JComboBox<EquipoItem> cmbEquipoFiltro = new JComboBox<EquipoItem>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(300, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnNuevo = new JButton("Nuevo usuario");
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnActivarInactivar = new JButton("Activar / Inactivar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnGuardar = new JButton("Guardar usuario");
    private final JButton btnCancelar = new JButton("Cancelar");
    private final JButton btnRestablecerClave = new JButton("Restablecer clave");

    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar usuarios.");
    private final JLabel lblUsuarioSeleccionado = new JLabel("Sin usuario seleccionado");
    private final JLabel lblArea = new JLabel("-");
    private final JLabel lblClave = new JLabel("Gestión de clave pendiente de mecanismo de autenticación.");
    private final JTextField txtUsername = new JTextField(20);
    private final JTextField txtNombres = new JTextField(22);
    private final JTextField txtApellidos = new JTextField(22);
    private final JTextField txtCorreo = new JTextField(24);
    private final JComboBox<TipoDocumentoIdentidadItem> cmbTipoDocumento = new JComboBox<TipoDocumentoIdentidadItem>();
    private final JTextField txtNumeroDocumento = new JTextField(16);
    private final JComboBox<EquipoItem> cmbEquipo = new JComboBox<EquipoItem>();
    private final JCheckBox chkActivo = new JCheckBox("Usuario activo");

    private final UsuariosTableModel usuariosModel = new UsuariosTableModel();
    private final JTable tblUsuarios = new AppV2Table(usuariosModel);
    private final RolesUsuarioTableModel rolesUsuarioModel = new RolesUsuarioTableModel();
    private final JTable tblRoles = new AppV2Table(rolesUsuarioModel);
    private JScrollPane scrollUsuarios;
    private JScrollPane scrollRoles;
    private AppV2OperationalSplitPanel splitDetalle;

    private final MetricCardV2 cardUsuarios = new MetricCardV2("Usuarios", "0", "Resultado de búsqueda", AppV2Theme.PRIMARY);
    private final MetricCardV2 cardActivos = new MetricCardV2("Activos", "0", "Acceso habilitado", AppV2Theme.SUCCESS);
    private final MetricCardV2 cardInactivos = new MetricCardV2("Inactivos", "0", "Sin eliminación física", AppV2Theme.WARNING);

    private final List<UsuarioDTO> usuarios = new ArrayList<>();
    private final List<RolDTO> rolesDisponibles = new ArrayList<>();
    private final List<EquipoAsignacionDTO> equiposDisponibles = new ArrayList<>();
    private final List<RoleSelection> rolesSeleccionables = new ArrayList<>();
    private Long idUsuarioEditando;
    private boolean cargandoFormulario;

    public JPanelUsuariosV2() {
        this(new UsuarioService());
    }

    public JPanelUsuariosV2(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());
        add(crearMetricas(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        configurarControles();
        configurarTablas();
        configurarEventos();
        cargarCatalogosYUsuarios();
    }

    private JPanel crearMetricas() {
        JPanel metricas = new JPanel(new GridLayout(1, 3, 12, 0));
        metricas.setOpaque(false);
        metricas.add(cardUsuarios);
        metricas.add(cardActivos);
        metricas.add(cardInactivos);
        return metricas;
    }

    private Component crearCentro() {
        splitDetalle = new AppV2OperationalSplitPanel(crearPanelListado(), crearPanelDetalle(), 540, 380, 440);
        return splitDetalle;
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
        filtros.add(label("Rol"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtros.add(cmbRolFiltro, gbc);
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 2;
        filtros.add(label("Equipo"), gbc);
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtros.add(cmbEquipoFiltro, gbc);
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
        acciones.add(btnRefrescar);
        acciones.add(btnRestablecerClave);

        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel barra = new JPanel(new BorderLayout(8, 8));
        barra.setOpaque(false);
        barra.add(filtros, BorderLayout.NORTH);
        barra.add(acciones, BorderLayout.CENTER);
        barra.add(lblEstado, BorderLayout.SOUTH);

        panel.add(barra, BorderLayout.NORTH);
        scrollUsuarios = new JScrollPane(tblUsuarios);
        panel.add(scrollUsuarios, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelDetalle() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(440, 0));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.sectionBorder());

        JLabel title = new JLabel("Detalle del usuario");
        title.setFont(AppV2Theme.fontBold(18));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);
        lblUsuarioSeleccionado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblUsuarioSeleccionado.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(lblUsuarioSeleccionado, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tabs.addTab("Datos", crearFormularioUsuario());
        tabs.addTab("Roles asignados", crearPanelRoles());

        panel.add(header, BorderLayout.NORTH);
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormularioUsuario() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        int row = 0;
        agregarFila(form, row++, "Usuario / login", txtUsername);
        agregarFila(form, row++, "Nombres", txtNombres);
        agregarFila(form, row++, "Apellidos", txtApellidos);
        agregarFila(form, row++, "Correo institucional", txtCorreo);
        agregarFila(form, row++, "Tipo documento", cmbTipoDocumento);
        agregarFila(form, row++, "Nro. documento", txtNumeroDocumento);
        agregarFila(form, row++, "Equipo", cmbEquipo);
        agregarFila(form, row++, "Área", lblArea);
        agregarFila(form, row, "Estado", chkActivo);

        lblClave.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblClave.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel clavePanel = new JPanel(new BorderLayout(8, 4));
        clavePanel.setOpaque(false);
        clavePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 0, 0, 0)));
        JLabel claveTitle = new JLabel("Clave");
        claveTitle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        claveTitle.setForeground(AppV2Theme.TEXT_PRIMARY);
        clavePanel.add(claveTitle, BorderLayout.NORTH);
        clavePanel.add(lblClave, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new GridLayout(1, 2, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnGuardar);
        acciones.add(btnCancelar);

        panel.add(form, BorderLayout.NORTH);
        panel.add(clavePanel, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelRoles() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        JLabel title = new JLabel("Roles disponibles");
        title.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Seleccione al menos un rol activo para el usuario.");
        subtitle.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        subtitle.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);
        scrollRoles = new JScrollPane(tblRoles);
        panel.add(scrollRoles, BorderLayout.CENTER);
        return panel;
    }

    private void agregarFila(JPanel form, int row, String label, Component component) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.NORTHWEST;
        gbcLabel.insets = new Insets(7, 0, 7, 12);

        GridBagConstraints gbcValue = new GridBagConstraints();
        gbcValue.gridx = 1;
        gbcValue.gridy = row;
        gbcValue.weightx = 1;
        gbcValue.fill = GridBagConstraints.HORIZONTAL;
        gbcValue.insets = new Insets(7, 0, 7, 0);

        form.add(label(label), gbcLabel);
        form.add(component, gbcValue);
    }

    private void configurarControles() {
        cmbEstado.addItem(new EstadoFiltroItem("Todos", null));
        cmbEstado.addItem(new EstadoFiltroItem("Activos", Boolean.TRUE));
        cmbEstado.addItem(new EstadoFiltroItem("Inactivos", Boolean.FALSE));
        cmbTipoDocumento.addItem(new TipoDocumentoIdentidadItem("", "Seleccione"));
        cmbTipoDocumento.addItem(new TipoDocumentoIdentidadItem("SIN DNI", "SIN DNI"));
        cmbTipoDocumento.addItem(new TipoDocumentoIdentidadItem("DNI", "DNI"));
        cmbTipoDocumento.addItem(new TipoDocumentoIdentidadItem("CE", "CE"));
        cmbTipoDocumento.addItem(new TipoDocumentoIdentidadItem("PASAPORTE", "Pasaporte"));
        chkActivo.setOpaque(false);
        chkActivo.setSelected(true);
        btnRestablecerClave.setEnabled(false);
        btnRestablecerClave.setToolTipText("Gestión de clave pendiente de definir para SDRERC_APP.");

        estilizarBotonPrimario(btnBuscar);
        estilizarBotonSecundario(btnLimpiar);
        estilizarBotonPrimario(btnNuevo);
        estilizarBotonSecundario(btnEditar);
        estilizarBotonSecundario(btnActivarInactivar);
        estilizarBotonSecundario(btnRefrescar);
        estilizarBotonSecundario(btnRestablecerClave);
        estilizarBotonPrimario(btnGuardar);
        estilizarBotonSecundario(btnCancelar);
    }

    private void configurarTablas() {
        tblUsuarios.setRowHeight(30);
        tblUsuarios.setFillsViewportHeight(true);
        tblUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblUsuarios.setAutoCreateRowSorter(false);
        tblUsuarios.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollUsuarios.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tblUsuarios.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tblUsuarios.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        tblUsuarios.getColumnModel().getColumn(0).setMaxWidth(70);
        tblUsuarios.getColumnModel().getColumn(6).setCellRenderer(new EstadoUsuarioRenderer());
        AppV2TableColumnSizer.applyFriendlyDefaults(tblUsuarios);
        AppV2ColumnFilterSupport.install("Administracion.Usuarios", tblUsuarios, scrollUsuarios, null, null);

        tblRoles.setRowHeight(30);
        tblRoles.setFillsViewportHeight(true);
        tblRoles.setAutoCreateRowSorter(false);
        tblRoles.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollRoles.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tblRoles.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tblRoles.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        tblRoles.getColumnModel().getColumn(0).setMaxWidth(72);
        tblRoles.getColumnModel().getColumn(0).setCellRenderer(tblRoles.getDefaultRenderer(Boolean.class));
        AppV2TableColumnSizer.applyFriendlyDefaults(tblRoles);
        AppV2ColumnFilterSupport.install("Administracion.Usuarios.Roles", tblRoles, scrollRoles, null, null);
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> cargarUsuarios());
        btnLimpiar.addActionListener(e -> limpiarFiltros());
        btnRefrescar.addActionListener(e -> cargarUsuarios());
        btnNuevo.addActionListener(e -> {
            nuevoUsuario();
            mostrarPanelDetalle();
        });
        btnEditar.addActionListener(e -> editarSeleccionado());
        btnCancelar.addActionListener(e -> nuevoUsuario());
        btnGuardar.addActionListener(e -> guardarUsuario());
        btnActivarInactivar.addActionListener(e -> cambiarEstadoSeleccionado());
        txtBusqueda.addActionListener(e -> cargarUsuarios());
        cmbEquipo.addActionListener(e -> actualizarAreaSeleccionada());

        tblUsuarios.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && !cargandoFormulario) {
                    UsuarioDTO usuario = obtenerUsuarioSeleccionado();
                    if (usuario != null) {
                        cargarFormulario(usuario);
                        mostrarPanelDetalle();
                    }
                    actualizarBotones();
                }
            }
        });
    }

    private void cargarCatalogosYUsuarios() {
        setBusy(true, "Consultando catálogos de usuarios...");
        new SwingWorker<CatalogosUsuarios, Void>() {
            @Override
            protected CatalogosUsuarios doInBackground() throws Exception {
                return new CatalogosUsuarios(usuarioService.listarRolesActivos(), usuarioService.listarEquiposActivos());
            }

            @Override
            protected void done() {
                try {
                    CatalogosUsuarios catalogos = get();
                    rolesDisponibles.clear();
                    rolesDisponibles.addAll(catalogos.roles);
                    equiposDisponibles.clear();
                    equiposDisponibles.addAll(catalogos.equipos);
                    poblarCombosCatalogos();
                    nuevoUsuario();
                    cargarUsuarios();
                } catch (Exception ex) {
                    mostrarError("No se pudo cargar catálogos de usuarios.", ex);
                    setBusy(false, null);
                }
            }
        }.execute();
    }

    private void poblarCombosCatalogos() {
        cmbRolFiltro.removeAllItems();
        cmbRolFiltro.addItem(new RolItem(null, "Todos los roles"));
        for (RolDTO rol : rolesDisponibles) {
            cmbRolFiltro.addItem(new RolItem(rol, rol.getNombre()));
        }

        cmbEquipoFiltro.removeAllItems();
        cmbEquipoFiltro.addItem(new EquipoItem(null, "Todos los equipos"));
        cmbEquipo.removeAllItems();
        cmbEquipo.addItem(new EquipoItem(null, "Sin equipo"));
        for (EquipoAsignacionDTO equipo : equiposDisponibles) {
            EquipoItem item = new EquipoItem(equipo, equipo.getDisplayName());
            cmbEquipoFiltro.addItem(item);
            cmbEquipo.addItem(item);
        }
        reconstruirRolesSeleccionables(new HashSet<Long>());
    }

    private void cargarUsuarios() {
        setBusy(true, "Consultando usuarios...");
        UsuarioFiltroDTO filtro = new UsuarioFiltroDTO(
                txtBusqueda.getText(),
                ((EstadoFiltroItem) cmbEstado.getSelectedItem()).activo,
                ((RolItem) cmbRolFiltro.getSelectedItem()).getIdRol(),
                ((EquipoItem) cmbEquipoFiltro.getSelectedItem()).getIdEquipo(),
                ((Number) spnLimite.getValue()).intValue());
        new SwingWorker<List<UsuarioDTO>, Void>() {
            @Override
            protected List<UsuarioDTO> doInBackground() throws Exception {
                return usuarioService.buscar(filtro);
            }

            @Override
            protected void done() {
                try {
                    usuarios.clear();
                    usuarios.addAll(get());
                    usuariosModel.fireTableDataChanged();
                    actualizarMetricas();
                    nuevoUsuario();
                    lblEstado.setText(usuarios.size() + " usuario(s) encontrado(s).");
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar usuarios.", ex);
                    lblEstado.setText("No se pudo consultar usuarios.");
                } finally {
                    setBusy(false, null);
                }
            }
        }.execute();
    }

    private void limpiarFiltros() {
        txtBusqueda.setText("");
        cmbEstado.setSelectedIndex(0);
        if (cmbRolFiltro.getItemCount() > 0) {
            cmbRolFiltro.setSelectedIndex(0);
        }
        if (cmbEquipoFiltro.getItemCount() > 0) {
            cmbEquipoFiltro.setSelectedIndex(0);
        }
        spnLimite.setValue(300);
        cargarUsuarios();
    }

    private void nuevoUsuario() {
        cargandoFormulario = true;
        idUsuarioEditando = null;
        tblUsuarios.clearSelection();
        txtUsername.setText("");
        txtNombres.setText("");
        txtApellidos.setText("");
        txtCorreo.setText("");
        seleccionarTipoDocumento(null);
        txtNumeroDocumento.setText("");
        if (cmbEquipo.getItemCount() > 0) {
            cmbEquipo.setSelectedIndex(0);
        }
        chkActivo.setSelected(true);
        lblUsuarioSeleccionado.setText("Nuevo usuario");
        reconstruirRolesSeleccionables(new HashSet<Long>());
        lblClave.setText("El usuario se registrará sin clave. Gestión pendiente de mecanismo de autenticación V2.");
        cargandoFormulario = false;
        actualizarAreaSeleccionada();
        actualizarBotones();
    }

    private void editarSeleccionado() {
        UsuarioDTO usuario = obtenerUsuarioSeleccionado();
        if (usuario == null) {
            mostrarInfo("Seleccione un usuario para editar.");
            return;
        }
        cargarFormulario(usuario);
        mostrarPanelDetalle();
        txtNombres.requestFocusInWindow();
    }

    private void cargarFormulario(UsuarioDTO usuario) {
        if (usuario == null) {
            return;
        }
        cargandoFormulario = true;
        idUsuarioEditando = usuario.getIdUsuario();
        txtUsername.setText(nullToEmpty(usuario.getUsername()));
        txtNombres.setText(nullToEmpty(usuario.getNombres()));
        txtApellidos.setText(nullToEmpty(usuario.getApellidos()));
        txtCorreo.setText(nullToEmpty(usuario.getCorreo()));
        seleccionarTipoDocumento(usuario.getTipoDocumento());
        txtNumeroDocumento.setText(nullToEmpty(usuario.getNumeroDocumento()));
        chkActivo.setSelected(usuario.isActivo());
        seleccionarEquipo(usuario.getIdEquipo());
        reconstruirRolesSeleccionables(new HashSet<Long>(usuario.getIdsRoles()));
        lblUsuarioSeleccionado.setText("Usuario seleccionado: " + nullToEmpty(usuario.getUsername()) + " · " + nullToEmpty(usuario.getNombreCompleto()));
        lblClave.setText("No se muestra la clave actual. Restablecimiento pendiente de mecanismo de autenticación V2.");
        cargandoFormulario = false;
        actualizarAreaSeleccionada();
    }

    private void guardarUsuario() {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setIdUsuario(idUsuarioEditando);
        usuario.setUsername(txtUsername.getText());
        usuario.setNombres(txtNombres.getText());
        usuario.setApellidos(txtApellidos.getText());
        usuario.setCorreo(txtCorreo.getText());
        usuario.setTipoDocumento(obtenerTipoDocumentoSeleccionado());
        usuario.setNumeroDocumento(txtNumeroDocumento.getText());
        usuario.setActivo(chkActivo.isSelected());

        List<Long> rolesSeleccionados = obtenerIdsRolesSeleccionados();
        Long idEquipo = ((EquipoItem) cmbEquipo.getSelectedItem()).getIdEquipo();

        setBusy(true, "Guardando usuario...");
        new SwingWorker<UsuarioResultadoDTO, Void>() {
            @Override
            protected UsuarioResultadoDTO doInBackground() throws Exception {
                return usuarioService.guardar(usuario, rolesSeleccionados, idEquipo);
            }

            @Override
            protected void done() {
                try {
                    UsuarioResultadoDTO resultado = get();
                    mostrarInfo(resultado.getMensaje());
                    Long idGuardado = resultado.getUsuario() == null ? null : resultado.getUsuario().getIdUsuario();
                    recargarYSeleccionar(idGuardado);
                } catch (Exception ex) {
                    mostrarError("No se pudo guardar el usuario.", ex);
                } finally {
                    setBusy(false, null);
                }
            }
        }.execute();
    }

    private void cambiarEstadoSeleccionado() {
        UsuarioDTO usuario = obtenerUsuarioSeleccionado();
        if (usuario == null) {
            mostrarInfo("Seleccione un usuario.");
            return;
        }
        boolean activar = !usuario.isActivo();
        String accion = activar ? "activar" : "inactivar";
        int option = JOptionPane.showConfirmDialog(
                this,
                "¿Desea " + accion + " el usuario " + usuario.getUsername() + "?",
                "Confirmar cambio de estado",
                JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        setBusy(true, "Actualizando estado del usuario...");
        new SwingWorker<UsuarioResultadoDTO, Void>() {
            @Override
            protected UsuarioResultadoDTO doInBackground() throws Exception {
                return usuarioService.cambiarActivo(usuario.getIdUsuario(), activar);
            }

            @Override
            protected void done() {
                try {
                    UsuarioResultadoDTO resultado = get();
                    mostrarInfo(resultado.getMensaje());
                    recargarYSeleccionar(usuario.getIdUsuario());
                } catch (Exception ex) {
                    mostrarError("No se pudo cambiar el estado del usuario.", ex);
                } finally {
                    setBusy(false, null);
                }
            }
        }.execute();
    }

    private void recargarYSeleccionar(Long idUsuario) {
        UsuarioFiltroDTO filtro = new UsuarioFiltroDTO(
                txtBusqueda.getText(),
                ((EstadoFiltroItem) cmbEstado.getSelectedItem()).activo,
                ((RolItem) cmbRolFiltro.getSelectedItem()).getIdRol(),
                ((EquipoItem) cmbEquipoFiltro.getSelectedItem()).getIdEquipo(),
                ((Number) spnLimite.getValue()).intValue());
        new SwingWorker<List<UsuarioDTO>, Void>() {
            @Override
            protected List<UsuarioDTO> doInBackground() throws Exception {
                return usuarioService.buscar(filtro);
            }

            @Override
            protected void done() {
                try {
                    usuarios.clear();
                    usuarios.addAll(get());
                    usuariosModel.fireTableDataChanged();
                    actualizarMetricas();
                    seleccionarUsuario(idUsuario);
                    lblEstado.setText(usuarios.size() + " usuario(s) encontrado(s).");
                } catch (Exception ex) {
                    mostrarError("No se pudo refrescar usuarios.", ex);
                }
            }
        }.execute();
    }

    private void seleccionarUsuario(Long idUsuario) {
        if (idUsuario == null) {
            nuevoUsuario();
            return;
        }
        for (int i = 0; i < usuarios.size(); i++) {
            UsuarioDTO usuario = usuarios.get(i);
            if (idUsuario.equals(usuario.getIdUsuario())) {
                int viewRow = tblUsuarios.convertRowIndexToView(i);
                tblUsuarios.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                tblUsuarios.scrollRectToVisible(tblUsuarios.getCellRect(viewRow, 0, true));
                cargarFormulario(usuario);
                return;
            }
        }
        nuevoUsuario();
    }

    private void seleccionarEquipo(Long idEquipo) {
        if (cmbEquipo.getItemCount() == 0) {
            return;
        }
        for (int i = 0; i < cmbEquipo.getItemCount(); i++) {
            EquipoItem item = cmbEquipo.getItemAt(i);
            if (idEquipo == null && item.getIdEquipo() == null) {
                cmbEquipo.setSelectedIndex(i);
                return;
            }
            if (idEquipo != null && idEquipo.equals(item.getIdEquipo())) {
                cmbEquipo.setSelectedIndex(i);
                return;
            }
        }
        cmbEquipo.setSelectedIndex(0);
    }

    private void seleccionarTipoDocumento(String codigo) {
        String normalized = codigo == null ? "" : codigo.trim();
        for (int i = 0; i < cmbTipoDocumento.getItemCount(); i++) {
            TipoDocumentoIdentidadItem item = cmbTipoDocumento.getItemAt(i);
            if (item.codigo.equalsIgnoreCase(normalized)) {
                cmbTipoDocumento.setSelectedIndex(i);
                return;
            }
        }
        cmbTipoDocumento.setSelectedIndex(0);
    }

    private String obtenerTipoDocumentoSeleccionado() {
        TipoDocumentoIdentidadItem item = (TipoDocumentoIdentidadItem) cmbTipoDocumento.getSelectedItem();
        return item == null || item.codigo.trim().isEmpty() ? null : item.codigo;
    }

    private void mostrarPanelDetalle() {
        if (splitDetalle != null) {
            splitDetalle.setSideVisible(true);
        }
    }

    private void actualizarAreaSeleccionada() {
        EquipoItem item = (EquipoItem) cmbEquipo.getSelectedItem();
        if (item == null || item.equipo == null || item.equipo.getArea().isEmpty()) {
            lblArea.setText("-");
        } else {
            lblArea.setText(item.equipo.getArea());
        }
    }

    private UsuarioDTO obtenerUsuarioSeleccionado() {
        int viewRow = tblUsuarios.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = tblUsuarios.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= usuarios.size()) {
            return null;
        }
        return usuarios.get(modelRow);
    }

    private void reconstruirRolesSeleccionables(Set<Long> idsSeleccionados) {
        rolesSeleccionables.clear();
        for (RolDTO rol : rolesDisponibles) {
            rolesSeleccionables.add(new RoleSelection(rol, idsSeleccionados.contains(rol.getIdRol())));
        }
        rolesUsuarioModel.fireTableDataChanged();
    }

    private List<Long> obtenerIdsRolesSeleccionados() {
        List<Long> ids = new ArrayList<>();
        for (RoleSelection roleSelection : rolesSeleccionables) {
            if (roleSelection.selected) {
                ids.add(roleSelection.rol.getIdRol());
            }
        }
        return ids;
    }

    private void actualizarMetricas() {
        int activos = 0;
        int inactivos = 0;
        for (UsuarioDTO usuario : usuarios) {
            if (usuario.isActivo()) {
                activos++;
            } else {
                inactivos++;
            }
        }
        cardUsuarios.setValue(String.valueOf(usuarios.size()));
        cardActivos.setValue(String.valueOf(activos));
        cardInactivos.setValue(String.valueOf(inactivos));
    }

    private void actualizarBotones() {
        UsuarioDTO usuario = obtenerUsuarioSeleccionado();
        btnEditar.setEnabled(usuario != null);
        btnActivarInactivar.setEnabled(usuario != null);
        if (usuario == null) {
            btnActivarInactivar.setText("Activar / Inactivar");
        } else {
            btnActivarInactivar.setText(usuario.isActivo() ? "Inactivar" : "Activar");
        }
    }

    private void setBusy(boolean busy, String message) {
        btnBuscar.setEnabled(!busy);
        btnLimpiar.setEnabled(!busy);
        btnNuevo.setEnabled(!busy);
        btnEditar.setEnabled(!busy && obtenerUsuarioSeleccionado() != null);
        btnActivarInactivar.setEnabled(!busy && obtenerUsuarioSeleccionado() != null);
        btnRefrescar.setEnabled(!busy);
        btnGuardar.setEnabled(!busy);
        btnCancelar.setEnabled(!busy);
        if (message != null) {
            lblEstado.setText(message);
        }
    }

    private void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Usuarios", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String titulo, Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String detalle = cause.getMessage() == null ? "Ocurrió un error inesperado." : cause.getMessage();
        JOptionPane.showMessageDialog(this, detalle, titulo, JOptionPane.ERROR_MESSAGE);
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        label.setForeground(AppV2Theme.TEXT_SECONDARY);
        return label;
    }

    private void estilizarBotonPrimario(JButton button) {
        button.setFocusPainted(false);
        button.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        button.setBackground(AppV2Theme.PRIMARY);
        button.setForeground(java.awt.Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    private void estilizarBotonSecundario(JButton button) {
        button.setFocusPainted(false);
        button.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        button.setBackground(AppV2Theme.SURFACE_ALT);
        button.setForeground(AppV2Theme.TEXT_PRIMARY);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private class UsuariosTableModel extends AbstractTableModel {

        private final String[] columns = {
            "ID", "Usuario", "Nombres", "Apellidos", "Documento", "Correo", "Estado", "Roles", "Equipo / área"
        };

        @Override
        public int getRowCount() {
            return usuarios.size();
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
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Long.class;
            }
            return String.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            UsuarioDTO usuario = usuarios.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return usuario.getIdUsuario();
                case 1:
                    return usuario.getUsername();
                case 2:
                    return usuario.getNombres();
                case 3:
                    return usuario.getApellidos();
                case 4:
                    return documento(usuario);
                case 5:
                    return usuario.getCorreo();
                case 6:
                    return usuario.isActivo() ? "Activo" : "Inactivo";
                case 7:
                    return nullToEmpty(usuario.getRolesResumen());
                case 8:
                    return equipoArea(usuario);
                default:
                    return "";
            }
        }

        private String documento(UsuarioDTO usuario) {
            String tipo = nullToEmpty(usuario.getTipoDocumento());
            String numero = nullToEmpty(usuario.getNumeroDocumento());
            if (tipo.isEmpty()) {
                return numero;
            }
            if (numero.isEmpty()) {
                return tipo;
            }
            return tipo + " " + numero;
        }

        private String equipoArea(UsuarioDTO usuario) {
            String equipo = nullToEmpty(usuario.getEquipoNombre());
            String area = nullToEmpty(usuario.getAreaNombre());
            if (equipo.isEmpty()) {
                return area;
            }
            if (area.isEmpty()) {
                return equipo;
            }
            return equipo + " - " + area;
        }
    }

    private class RolesUsuarioTableModel extends AbstractTableModel {

        private final String[] columns = {"Asignar", "Código", "Rol"};

        @Override
        public int getRowCount() {
            return rolesSeleccionables.size();
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
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RoleSelection roleSelection = rolesSeleccionables.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return roleSelection.selected;
                case 1:
                    return roleSelection.rol.getCodigo();
                case 2:
                    return roleSelection.rol.getNombre();
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0 && rowIndex >= 0 && rowIndex < rolesSeleccionables.size()) {
                rolesSeleccionables.get(rowIndex).selected = Boolean.TRUE.equals(aValue);
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }

    private static class EstadoFiltroItem {

        private final String label;
        private final Boolean activo;

        EstadoFiltroItem(String label, Boolean activo) {
            this.label = label;
            this.activo = activo;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class RolItem {

        private final RolDTO rol;
        private final String label;

        RolItem(RolDTO rol, String label) {
            this.rol = rol;
            this.label = label;
        }

        Long getIdRol() {
            return rol == null ? null : rol.getIdRol();
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class EquipoItem {

        private final EquipoAsignacionDTO equipo;
        private final String label;

        EquipoItem(EquipoAsignacionDTO equipo, String label) {
            this.equipo = equipo;
            this.label = label;
        }

        Long getIdEquipo() {
            return equipo == null ? null : equipo.getIdEquipo();
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class TipoDocumentoIdentidadItem {
        private final String codigo;
        private final String label;

        private TipoDocumentoIdentidadItem(String codigo, String label) {
            this.codigo = codigo == null ? "" : codigo;
            this.label = label == null ? this.codigo : label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class RoleSelection {

        private final RolDTO rol;
        private boolean selected;

        RoleSelection(RolDTO rol, boolean selected) {
            this.rol = rol;
            this.selected = selected;
        }
    }

    private static class CatalogosUsuarios {

        private final List<RolDTO> roles;
        private final List<EquipoAsignacionDTO> equipos;

        CatalogosUsuarios(List<RolDTO> roles, List<EquipoAsignacionDTO> equipos) {
            this.roles = roles == null ? new ArrayList<RolDTO>() : roles;
            this.equipos = equipos == null ? new ArrayList<EquipoAsignacionDTO>() : equipos;
        }
    }

    private static class EstadoUsuarioRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = value == null ? "-" : value.toString();
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
            label.setText(text);
            if (!isSelected) {
                if ("Activo".equalsIgnoreCase(text)) {
                    label.setBackground(AppV2Theme.SOFT_GREEN);
                    label.setForeground(AppV2Theme.SUCCESS);
                } else {
                    label.setBackground(AppV2Theme.SOFT_ORANGE);
                    label.setForeground(AppV2Theme.WARNING);
                }
            }
            return label;
        }
    }
}
