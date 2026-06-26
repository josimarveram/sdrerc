package com.sdrerc.ui.views.administracion.roles;

import com.sdrerc.application.sdrercapp.PermisoRolService;
import com.sdrerc.application.sdrercapp.RolService;
import com.sdrerc.domain.dto.sdrercapp.PermisoDTO;
import com.sdrerc.domain.dto.sdrercapp.RolDTO;
import com.sdrerc.domain.dto.sdrercapp.RolFiltroDTO;
import com.sdrerc.domain.dto.sdrercapp.RolResultadoDTO;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2ColumnFilterSupport;
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

public class JPanelRolesV2 extends JPanel {

    private final RolService rolService;
    private final PermisoRolService permisoRolService;

    private final JTextField txtBusqueda = new JTextField(26);
    private final JComboBox<EstadoFiltroItem> cmbEstado = new JComboBox<EstadoFiltroItem>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(300, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnNuevo = new JButton("Nuevo rol");
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnActivarInactivar = new JButton("Activar / Inactivar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnGuardar = new JButton("Guardar rol");
    private final JButton btnCancelar = new JButton("Cancelar");
    private final JButton btnGuardarPermisos = new JButton("Guardar permisos");

    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar roles.");
    private final JLabel lblRolSeleccionado = new JLabel("Sin rol seleccionado");
    private final JLabel lblPermisosEstado = new JLabel("Seleccione un rol para revisar permisos.");
    private final JTextField txtCodigo = new JTextField(18);
    private final JTextField txtNombre = new JTextField(24);
    private final JTextArea txtDescripcion = new JTextArea(4, 24);
    private final JCheckBox chkActivo = new JCheckBox("Rol activo");

    private final RolesTableModel rolesModel = new RolesTableModel();
    private final JTable tblRoles = new AppV2Table(rolesModel);
    private final PermisosTableModel permisosModel = new PermisosTableModel();
    private final JTable tblPermisos = new AppV2Table(permisosModel);
    private JScrollPane scrollRoles;
    private JScrollPane scrollPermisos;

    private final MetricCardV2 cardRoles = new MetricCardV2("Roles", "0", "Resultado de búsqueda", AppV2Theme.PRIMARY);
    private final MetricCardV2 cardActivos = new MetricCardV2("Activos", "0", "Habilitados para uso", AppV2Theme.SUCCESS);
    private final MetricCardV2 cardInactivos = new MetricCardV2("Inactivos", "0", "Sin eliminación física", AppV2Theme.WARNING);

    private final List<RolDTO> roles = new ArrayList<>();
    private final List<PermisoDTO> permisos = new ArrayList<>();
    private Long idRolEditando;
    private boolean cargandoFormulario;

    public JPanelRolesV2() {
        this(new RolService(), new PermisoRolService());
    }

    public JPanelRolesV2(RolService rolService, PermisoRolService permisoRolService) {
        this.rolService = rolService;
        this.permisoRolService = permisoRolService;
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());
        add(crearMetricas(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        configurarControles();
        configurarTablas();
        configurarEventos();
        nuevoRol();
        cargarRoles();
    }

    private JPanel crearMetricas() {
        JPanel metricas = new JPanel(new GridLayout(1, 3, 12, 0));
        metricas.setOpaque(false);
        metricas.add(cardRoles);
        metricas.add(cardActivos);
        metricas.add(cardInactivos);
        return metricas;
    }

    private Component crearCentro() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, crearPanelListado(), crearPanelDetalle());
        split.setBorder(null);
        split.setResizeWeight(0.68);
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

        gbc.gridx = 4;
        filtros.add(label("Mostrar"), gbc);
        gbc.gridx = 5;
        filtros.add(spnLimite, gbc);

        JPanel accionesFiltro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accionesFiltro.setOpaque(false);
        accionesFiltro.add(btnBuscar);
        accionesFiltro.add(btnLimpiar);
        gbc.gridx = 6;
        filtros.add(accionesFiltro, gbc);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnNuevo);
        acciones.add(btnEditar);
        acciones.add(btnActivarInactivar);
        acciones.add(btnRefrescar);

        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel barra = new JPanel(new BorderLayout(8, 8));
        barra.setOpaque(false);
        barra.add(filtros, BorderLayout.NORTH);
        barra.add(acciones, BorderLayout.CENTER);
        barra.add(lblEstado, BorderLayout.SOUTH);

        panel.add(barra, BorderLayout.NORTH);
        scrollRoles = new JScrollPane(tblRoles);
        panel.add(scrollRoles, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelDetalle() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(430, 0));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.sectionBorder());

        JLabel title = new JLabel("Detalle del rol");
        title.setFont(AppV2Theme.fontBold(18));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);

        lblRolSeleccionado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblRolSeleccionado.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(lblRolSeleccionado, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tabs.addTab("Rol", crearFormularioRol());
        tabs.addTab("Permisos del rol", crearPanelPermisos());

        panel.add(header, BorderLayout.NORTH);
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormularioRol() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        int row = 0;
        agregarFila(form, row++, "Código", txtCodigo);
        agregarFila(form, row++, "Nombre", txtNombre);
        agregarFila(form, row++, "Descripción", scrollDescripcion());
        agregarFila(form, row, "Estado", chkActivo);

        JLabel ayuda = new JLabel("<html>El código se normaliza en mayúsculas y usa guion bajo cuando haya espacios.</html>");
        ayuda.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        ayuda.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel acciones = new JPanel(new GridLayout(1, 2, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnGuardar);
        acciones.add(btnCancelar);

        panel.add(form, BorderLayout.NORTH);
        panel.add(ayuda, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelPermisos() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        JLabel title = new JLabel("Permisos disponibles");
        title.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);
        lblPermisosEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblPermisosEstado.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel header = new JPanel(new BorderLayout(8, 4));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(lblPermisosEstado, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnGuardarPermisos);

        panel.add(header, BorderLayout.NORTH);
        scrollPermisos = new JScrollPane(tblPermisos);
        panel.add(scrollPermisos, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane scrollDescripcion() {
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        txtDescripcion.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane scroll = new JScrollPane(txtDescripcion);
        scroll.setPreferredSize(new Dimension(250, 90));
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        return scroll;
    }

    private void agregarFila(JPanel form, int row, String label, Component component) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.NORTHWEST;
        gbcLabel.insets = new Insets(8, 0, 8, 12);

        GridBagConstraints gbcValue = new GridBagConstraints();
        gbcValue.gridx = 1;
        gbcValue.gridy = row;
        gbcValue.weightx = 1;
        gbcValue.fill = GridBagConstraints.HORIZONTAL;
        gbcValue.insets = new Insets(8, 0, 8, 0);

        form.add(label(label), gbcLabel);
        form.add(component, gbcValue);
    }

    private void configurarControles() {
        cmbEstado.addItem(new EstadoFiltroItem("Todos", null));
        cmbEstado.addItem(new EstadoFiltroItem("Activos", Boolean.TRUE));
        cmbEstado.addItem(new EstadoFiltroItem("Inactivos", Boolean.FALSE));
        txtDescripcion.setRows(4);
        chkActivo.setOpaque(false);
        chkActivo.setSelected(true);

        estilizarBotonPrimario(btnBuscar);
        estilizarBotonSecundario(btnLimpiar);
        estilizarBotonPrimario(btnNuevo);
        estilizarBotonSecundario(btnEditar);
        estilizarBotonSecundario(btnActivarInactivar);
        estilizarBotonSecundario(btnRefrescar);
        estilizarBotonPrimario(btnGuardar);
        estilizarBotonSecundario(btnCancelar);
        estilizarBotonPrimario(btnGuardarPermisos);
    }

    private void configurarTablas() {
        tblRoles.setRowHeight(30);
        tblRoles.setFillsViewportHeight(true);
        tblRoles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRoles.setAutoCreateRowSorter(false);
        tblRoles.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tblRoles.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        tblRoles.getColumnModel().getColumn(0).setMaxWidth(70);
        tblRoles.getColumnModel().getColumn(4).setCellRenderer(new EstadoRolRenderer());
        tblRoles.getColumnModel().getColumn(5).setMaxWidth(80);
        tblRoles.getColumnModel().getColumn(6).setMaxWidth(80);
        AppV2TableColumnSizer.applyFriendlyDefaults(tblRoles);
        AppV2ColumnFilterSupport.install("Administracion.Roles", tblRoles, scrollRoles, null, null);

        tblPermisos.setRowHeight(30);
        tblPermisos.setFillsViewportHeight(true);
        tblPermisos.setAutoCreateRowSorter(false);
        tblPermisos.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tblPermisos.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        tblPermisos.getColumnModel().getColumn(0).setMaxWidth(72);
        tblPermisos.getColumnModel().getColumn(0).setCellRenderer(tblPermisos.getDefaultRenderer(Boolean.class));
        tblPermisos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AppV2TableColumnSizer.applyFriendlyDefaults(tblPermisos);
        AppV2ColumnFilterSupport.install("Administracion.Roles.Permisos", tblPermisos, scrollPermisos, null, null);
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> cargarRoles());
        btnLimpiar.addActionListener(e -> limpiarFiltros());
        btnRefrescar.addActionListener(e -> cargarRoles());
        btnNuevo.addActionListener(e -> nuevoRol());
        btnEditar.addActionListener(e -> editarSeleccionado());
        btnCancelar.addActionListener(e -> nuevoRol());
        btnGuardar.addActionListener(e -> guardarRol());
        btnActivarInactivar.addActionListener(e -> cambiarEstadoSeleccionado());
        btnGuardarPermisos.addActionListener(e -> guardarPermisos());
        txtBusqueda.addActionListener(e -> cargarRoles());

        tblRoles.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && !cargandoFormulario) {
                    RolDTO rol = obtenerRolSeleccionado();
                    if (rol != null) {
                        cargarFormulario(rol);
                    }
                    actualizarBotones();
                }
            }
        });
    }

    private void cargarRoles() {
        setBusy(true, "Consultando roles...");
        RolFiltroDTO filtro = new RolFiltroDTO(
                txtBusqueda.getText(),
                ((EstadoFiltroItem) cmbEstado.getSelectedItem()).activo,
                ((Number) spnLimite.getValue()).intValue());
        new SwingWorker<List<RolDTO>, Void>() {
            @Override
            protected List<RolDTO> doInBackground() throws Exception {
                return rolService.buscar(filtro);
            }

            @Override
            protected void done() {
                try {
                    roles.clear();
                    roles.addAll(get());
                    rolesModel.fireTableDataChanged();
                    actualizarMetricas();
                    nuevoRol();
                    lblEstado.setText(roles.size() + " rol(es) encontrado(s).");
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar roles.", ex);
                    lblEstado.setText("No se pudo consultar roles.");
                } finally {
                    setBusy(false, null);
                }
            }
        }.execute();
    }

    private void limpiarFiltros() {
        txtBusqueda.setText("");
        cmbEstado.setSelectedIndex(0);
        spnLimite.setValue(300);
        cargarRoles();
    }

    private void nuevoRol() {
        cargandoFormulario = true;
        idRolEditando = null;
        tblRoles.clearSelection();
        txtCodigo.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");
        chkActivo.setSelected(true);
        lblRolSeleccionado.setText("Nuevo rol");
        permisos.clear();
        permisosModel.fireTableDataChanged();
        lblPermisosEstado.setText("Guarde el rol antes de asociar permisos.");
        btnGuardarPermisos.setEnabled(false);
        cargandoFormulario = false;
        actualizarBotones();
    }

    private void editarSeleccionado() {
        RolDTO rol = obtenerRolSeleccionado();
        if (rol == null) {
            mostrarInfo("Seleccione un rol para editar.");
            return;
        }
        cargarFormulario(rol);
        txtNombre.requestFocusInWindow();
    }

    private void cargarFormulario(RolDTO rol) {
        if (rol == null) {
            return;
        }
        cargandoFormulario = true;
        idRolEditando = rol.getIdRol();
        txtCodigo.setText(nullToEmpty(rol.getCodigo()));
        txtNombre.setText(nullToEmpty(rol.getNombre()));
        txtDescripcion.setText(nullToEmpty(rol.getDescripcion()));
        chkActivo.setSelected(rol.isActivo());
        lblRolSeleccionado.setText("Rol seleccionado: " + nullToEmpty(rol.getNombre()) + " · " + nullToEmpty(rol.getCodigo()));
        cargandoFormulario = false;
        cargarPermisos(rol.getIdRol());
    }

    private void cargarPermisos(Long idRol) {
        permisos.clear();
        permisosModel.fireTableDataChanged();
        lblPermisosEstado.setText("Consultando permisos del rol...");
        btnGuardarPermisos.setEnabled(false);
        new SwingWorker<List<PermisoDTO>, Void>() {
            @Override
            protected List<PermisoDTO> doInBackground() throws Exception {
                return permisoRolService.listarPermisosPorRol(idRol);
            }

            @Override
            protected void done() {
                try {
                    permisos.clear();
                    permisos.addAll(get());
                    permisosModel.fireTableDataChanged();
                    btnGuardarPermisos.setEnabled(idRolEditando != null && !permisos.isEmpty());
                    if (permisos.isEmpty()) {
                        lblPermisosEstado.setText("No hay permisos catalogados en SDRERC_APP.");
                    } else {
                        lblPermisosEstado.setText(permisos.size() + " permiso(s) disponible(s).");
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar permisos del rol.", ex);
                    lblPermisosEstado.setText("No se pudo consultar permisos.");
                }
            }
        }.execute();
    }

    private void guardarRol() {
        RolDTO rol = new RolDTO();
        rol.setIdRol(idRolEditando);
        rol.setCodigo(txtCodigo.getText());
        rol.setNombre(txtNombre.getText());
        rol.setDescripcion(txtDescripcion.getText());
        rol.setActivo(chkActivo.isSelected());

        setBusy(true, "Guardando rol...");
        new SwingWorker<RolResultadoDTO, Void>() {
            @Override
            protected RolResultadoDTO doInBackground() throws Exception {
                return rolService.guardar(rol);
            }

            @Override
            protected void done() {
                try {
                    RolResultadoDTO resultado = get();
                    mostrarInfo(resultado.getMensaje());
                    Long idGuardado = resultado.getRol() == null ? null : resultado.getRol().getIdRol();
                    recargarYSeleccionar(idGuardado);
                } catch (Exception ex) {
                    mostrarError("No se pudo guardar el rol.", ex);
                } finally {
                    setBusy(false, null);
                }
            }
        }.execute();
    }

    private void cambiarEstadoSeleccionado() {
        RolDTO rol = obtenerRolSeleccionado();
        if (rol == null) {
            mostrarInfo("Seleccione un rol.");
            return;
        }
        boolean activar = !rol.isActivo();
        String accion = activar ? "activar" : "inactivar";
        int option = JOptionPane.showConfirmDialog(
                this,
                "¿Desea " + accion + " el rol " + rol.getNombre() + "?",
                "Confirmar cambio de estado",
                JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        setBusy(true, "Actualizando estado del rol...");
        new SwingWorker<RolResultadoDTO, Void>() {
            @Override
            protected RolResultadoDTO doInBackground() throws Exception {
                return rolService.cambiarActivo(rol.getIdRol(), activar);
            }

            @Override
            protected void done() {
                try {
                    RolResultadoDTO resultado = get();
                    mostrarInfo(resultado.getMensaje());
                    recargarYSeleccionar(rol.getIdRol());
                } catch (Exception ex) {
                    mostrarError("No se pudo cambiar el estado del rol.", ex);
                } finally {
                    setBusy(false, null);
                }
            }
        }.execute();
    }

    private void guardarPermisos() {
        if (idRolEditando == null) {
            mostrarInfo("Seleccione o guarde un rol antes de asociar permisos.");
            return;
        }
        if (permisos.isEmpty()) {
            mostrarInfo("No hay permisos catalogados para asociar.");
            return;
        }
        int option = JOptionPane.showConfirmDialog(
                this,
                "Se actualizarán los permisos del rol seleccionado. ¿Desea continuar?",
                "Confirmar permisos",
                JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        List<Long> seleccionados = new ArrayList<>();
        for (PermisoDTO permiso : permisos) {
            if (permiso.isAsignado()) {
                seleccionados.add(permiso.getIdPermiso());
            }
        }

        setBusy(true, "Guardando permisos del rol...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                permisoRolService.guardarPermisos(idRolEditando, seleccionados);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    mostrarInfo("Los permisos del rol fueron actualizados correctamente.");
                    recargarYSeleccionar(idRolEditando);
                } catch (Exception ex) {
                    mostrarError("No se pudo guardar permisos del rol.", ex);
                } finally {
                    setBusy(false, null);
                }
            }
        }.execute();
    }

    private void recargarYSeleccionar(Long idRol) {
        RolFiltroDTO filtro = new RolFiltroDTO(
                txtBusqueda.getText(),
                ((EstadoFiltroItem) cmbEstado.getSelectedItem()).activo,
                ((Number) spnLimite.getValue()).intValue());
        new SwingWorker<List<RolDTO>, Void>() {
            @Override
            protected List<RolDTO> doInBackground() throws Exception {
                return rolService.buscar(filtro);
            }

            @Override
            protected void done() {
                try {
                    roles.clear();
                    roles.addAll(get());
                    rolesModel.fireTableDataChanged();
                    actualizarMetricas();
                    seleccionarRol(idRol);
                    lblEstado.setText(roles.size() + " rol(es) encontrado(s).");
                } catch (Exception ex) {
                    mostrarError("No se pudo refrescar roles.", ex);
                }
            }
        }.execute();
    }

    private void seleccionarRol(Long idRol) {
        if (idRol == null) {
            nuevoRol();
            return;
        }
        for (int i = 0; i < roles.size(); i++) {
            RolDTO rol = roles.get(i);
            if (idRol.equals(rol.getIdRol())) {
                int viewRow = tblRoles.convertRowIndexToView(i);
                tblRoles.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                tblRoles.scrollRectToVisible(tblRoles.getCellRect(viewRow, 0, true));
                cargarFormulario(rol);
                return;
            }
        }
        nuevoRol();
    }

    private RolDTO obtenerRolSeleccionado() {
        int viewRow = tblRoles.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = tblRoles.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= roles.size()) {
            return null;
        }
        return roles.get(modelRow);
    }

    private void actualizarMetricas() {
        int activos = 0;
        int inactivos = 0;
        for (RolDTO rol : roles) {
            if (rol.isActivo()) {
                activos++;
            } else {
                inactivos++;
            }
        }
        cardRoles.setValue(String.valueOf(roles.size()));
        cardActivos.setValue(String.valueOf(activos));
        cardInactivos.setValue(String.valueOf(inactivos));
    }

    private void actualizarBotones() {
        RolDTO rol = obtenerRolSeleccionado();
        btnEditar.setEnabled(rol != null);
        btnActivarInactivar.setEnabled(rol != null);
        if (rol == null) {
            btnActivarInactivar.setText("Activar / Inactivar");
        } else {
            btnActivarInactivar.setText(rol.isActivo() ? "Inactivar" : "Activar");
        }
    }

    private void setBusy(boolean busy, String message) {
        btnBuscar.setEnabled(!busy);
        btnLimpiar.setEnabled(!busy);
        btnNuevo.setEnabled(!busy);
        btnEditar.setEnabled(!busy && obtenerRolSeleccionado() != null);
        btnActivarInactivar.setEnabled(!busy && obtenerRolSeleccionado() != null);
        btnRefrescar.setEnabled(!busy);
        btnGuardar.setEnabled(!busy);
        btnCancelar.setEnabled(!busy);
        btnGuardarPermisos.setEnabled(!busy && idRolEditando != null && !permisos.isEmpty());
        if (message != null) {
            lblEstado.setText(message);
        }
    }

    private void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Roles", JOptionPane.INFORMATION_MESSAGE);
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

    private class RolesTableModel extends AbstractTableModel {

        private final String[] columns = {
            "ID", "Código", "Nombre", "Descripción", "Estado", "Usuarios", "Permisos"
        };

        @Override
        public int getRowCount() {
            return roles.size();
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
            if (columnIndex == 5 || columnIndex == 6) {
                return Integer.class;
            }
            return String.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RolDTO rol = roles.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return rol.getIdRol();
                case 1:
                    return rol.getCodigo();
                case 2:
                    return rol.getNombre();
                case 3:
                    return rol.getDescripcion();
                case 4:
                    return rol.isActivo() ? "Activo" : "Inactivo";
                case 5:
                    return rol.getUsuariosAsociados();
                case 6:
                    return rol.getPermisosAsociados();
                default:
                    return "";
            }
        }
    }

    private class PermisosTableModel extends AbstractTableModel {

        private final String[] columns = {"Asignar", "Módulo", "Código", "Permiso"};

        @Override
        public int getRowCount() {
            return permisos.size();
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
            return columnIndex == 0 && idRolEditando != null;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            PermisoDTO permiso = permisos.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return permiso.isAsignado();
                case 1:
                    return permiso.getModulo();
                case 2:
                    return permiso.getCodigo();
                case 3:
                    return permiso.getNombre();
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0 && rowIndex >= 0 && rowIndex < permisos.size()) {
                permisos.get(rowIndex).setAsignado(Boolean.TRUE.equals(aValue));
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

    private static class EstadoRolRenderer extends DefaultTableCellRenderer {

        private final JCheckBox checkBox = new JCheckBox();

        EstadoRolRenderer() {
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            checkBox.setOpaque(true);
            checkBox.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        }

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
