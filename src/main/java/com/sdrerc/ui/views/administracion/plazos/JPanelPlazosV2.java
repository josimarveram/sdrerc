package com.sdrerc.ui.views.administracion.plazos;

import com.sdrerc.application.sdrercapp.PlazoConfiguracionService;
import com.sdrerc.domain.dto.sdrercapp.PlazoConfiguracionDTO;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2ColumnFilterSupport;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.components.BadgeV2;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class JPanelPlazosV2 extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PlazoConfiguracionService plazoService;
    private final JTextField txtBusqueda = new JTextField(26);
    private final JComboBox<EstadoFiltroItem> cmbEstado = new JComboBox<EstadoFiltroItem>();
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnNuevo = new JButton("Nuevo plazo");
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnActivarInactivar = new JButton("Activar / Inactivar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnGuardar = new JButton("Guardar plazo");

    private final JLabel lblEstado = new JLabel("Configure el plazo oficial de solicitudes SDRERC para evitar depender del fallback.");
    private final JTextField txtCodigo = new JTextField(18);
    private final JTextField txtNombre = new JTextField(24);
    private final JTextField txtAmbito = new JTextField(18);
    private final JSpinner spnDias = new JSpinner(new SpinnerNumberModel(30, 1, 9999, 1));
    private final JComboBox<UnidadItem> cmbUnidad = new JComboBox<UnidadItem>();
    private final JTextField txtVigenciaDesde = new JTextField(12);
    private final JTextField txtVigenciaHasta = new JTextField(12);
    private final JTextArea txtObservacion = new JTextArea(5, 24);
    private final JCheckBox chkActivo = new JCheckBox("Configuración activa");

    private final PlazosTableModel tableModel = new PlazosTableModel();
    private final JTable tblPlazos = new AppV2Table(tableModel);
    private JScrollPane scrollPlazos;
    private AppV2OperationalSplitPanel splitDetalle;
    private final MetricCardV2 cardTotal = new MetricCardV2("Plazos", "0", "Configuraciones", AppV2Theme.PRIMARY);
    private final MetricCardV2 cardActivos = new MetricCardV2("Activos", "0", "Disponibles para cálculo", AppV2Theme.SUCCESS);
    private final MetricCardV2 cardSolicitud = new MetricCardV2("Solicitud SDRERC", "No configurado", "Configuración oficial", AppV2Theme.WARNING);

    private final List<PlazoConfiguracionDTO> plazos = new ArrayList<PlazoConfiguracionDTO>();
    private Long idPlazoEditando;
    private boolean cargandoFormulario;

    public JPanelPlazosV2() {
        this(new PlazoConfiguracionService());
    }

    public JPanelPlazosV2(PlazoConfiguracionService plazoService) {
        this.plazoService = plazoService;
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());
        add(crearMetricas(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        configurarControles();
        configurarTabla();
        configurarEventos();
        nuevoPlazo();
        cargarPlazos();
    }

    private JPanel crearMetricas() {
        JPanel metricas = new JPanel(new GridLayout(1, 3, 12, 0));
        metricas.setOpaque(false);
        metricas.add(cardTotal);
        metricas.add(cardActivos);
        metricas.add(cardSolicitud);
        return metricas;
    }

    private Component crearCentro() {
        splitDetalle = new AppV2OperationalSplitPanel(crearPanelListado(), crearPanelDetalle(), 540, 380, 460);
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

        JPanel accionesFiltro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accionesFiltro.setOpaque(false);
        accionesFiltro.add(btnBuscar);
        accionesFiltro.add(btnLimpiar);
        gbc.gridx = 4;
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
        scrollPlazos = new JScrollPane(tblPlazos);
        panel.add(scrollPlazos, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelDetalle() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(460, 0));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.sectionBorder());

        JLabel title = new JLabel("Configuración del plazo");
        title.setFont(AppV2Theme.fontBold(18));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        int row = 0;
        agregarFila(form, row++, "Código", txtCodigo);
        agregarFila(form, row++, "Nombre", txtNombre);
        agregarFila(form, row++, "Ámbito", txtAmbito);
        agregarFila(form, row++, "Días", spnDias);
        agregarFila(form, row++, "Unidad", cmbUnidad);
        agregarFila(form, row++, "Vigencia desde", txtVigenciaDesde);
        agregarFila(form, row++, "Vigencia hasta", txtVigenciaHasta);
        agregarFila(form, row++, "Observación", new JScrollPane(txtObservacion));
        agregarFila(form, row++, "", chkActivo);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnGuardar);

        panel.add(title, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private void configurarControles() {
        cmbEstado.addItem(new EstadoFiltroItem("Todos", null));
        cmbEstado.addItem(new EstadoFiltroItem("Activos", Boolean.TRUE));
        cmbEstado.addItem(new EstadoFiltroItem("Inactivos", Boolean.FALSE));
        cmbUnidad.addItem(new UnidadItem("Días hábiles", PlazoConfiguracionDTO.UNIDAD_HABILES));
        cmbUnidad.addItem(new UnidadItem("Días calendario", PlazoConfiguracionDTO.UNIDAD_CALENDARIO));
        txtVigenciaDesde.setToolTipText("Formato dd/MM/yyyy");
        txtVigenciaHasta.setToolTipText("Formato dd/MM/yyyy; opcional");
        txtObservacion.setLineWrap(true);
        txtObservacion.setWrapStyleWord(true);
        estilizarBotonPrimario(btnBuscar);
        estilizarBotonSecundario(btnLimpiar);
        estilizarBotonPrimario(btnNuevo);
        estilizarBotonSecundario(btnEditar);
        estilizarBotonSecundario(btnActivarInactivar);
        estilizarBotonSecundario(btnRefrescar);
        estilizarBotonPrimario(btnGuardar);
    }

    private void configurarTabla() {
        tblPlazos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPlazos.setAutoCreateRowSorter(false);
        tblPlazos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollPlazos.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tblPlazos.setDefaultRenderer(Object.class, new PlazoCellRenderer());
        AppV2TableColumnSizer.applyWidths(tblPlazos, 160, 260, 130, 90, 130, 110, 120, 120);
        AppV2ColumnFilterSupport.install("Administracion.Plazos", tblPlazos, scrollPlazos, null, null);
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> cargarPlazos());
        btnLimpiar.addActionListener(e -> limpiarFiltros());
        btnRefrescar.addActionListener(e -> cargarPlazos());
        btnNuevo.addActionListener(e -> {
            nuevoPlazo();
            mostrarPanelDetalle();
        });
        btnEditar.addActionListener(e -> cargarSeleccion());
        btnGuardar.addActionListener(e -> guardarPlazo());
        btnActivarInactivar.addActionListener(e -> cambiarActivoSeleccionado());
        tblPlazos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        tblPlazos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && tblPlazos.getSelectedRow() >= 0) {
                    mostrarPanelDetalle();
                }
            }
        });
    }

    private void cargarPlazos() {
        final String texto = txtBusqueda.getText();
        final Boolean activo = ((EstadoFiltroItem) cmbEstado.getSelectedItem()).activo;
        lblEstado.setText("Consultando configuraciones de plazo...");
        btnBuscar.setEnabled(false);
        new SwingWorker<List<PlazoConfiguracionDTO>, Void>() {
            @Override
            protected List<PlazoConfiguracionDTO> doInBackground() throws Exception {
                return plazoService.buscar(texto, activo, 1000);
            }

            @Override
            protected void done() {
                btnBuscar.setEnabled(true);
                try {
                    plazos.clear();
                    plazos.addAll(get());
                    tableModel.fireTableDataChanged();
                    actualizarMetricas();
                    lblEstado.setText(plazos.size() + " configuración(es) encontrada(s).");
                } catch (Exception ex) {
                    lblEstado.setText("No se pudo consultar plazos. Verifique el script de configuración.");
                    mostrarError("No se pudo consultar plazos.", ex);
                }
            }
        }.execute();
    }

    private void guardarPlazo() {
        PlazoConfiguracionDTO dto;
        try {
            dto = leerFormulario();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        btnGuardar.setEnabled(false);
        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws Exception {
                return plazoService.guardar(dto).getIdPlazoConfiguracion();
            }

            @Override
            protected void done() {
                btnGuardar.setEnabled(true);
                try {
                    idPlazoEditando = get();
                    lblEstado.setText("Configuración de plazo guardada correctamente.");
                    cargarPlazos();
                } catch (Exception ex) {
                    mostrarError("No se pudo guardar la configuración.", ex);
                }
            }
        }.execute();
    }

    private void cambiarActivoSeleccionado() {
        int viewRow = tblPlazos.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una configuración.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tblPlazos.convertRowIndexToModel(viewRow);
        PlazoConfiguracionDTO seleccionado = plazos.get(modelRow);
        final boolean nuevoEstado = !seleccionado.isActivo();
        btnActivarInactivar.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                plazoService.cambiarActivo(seleccionado.getIdPlazoConfiguracion(), nuevoEstado);
                return null;
            }

            @Override
            protected void done() {
                btnActivarInactivar.setEnabled(true);
                try {
                    get();
                    lblEstado.setText(nuevoEstado ? "Configuración activada." : "Configuración inactivada.");
                    cargarPlazos();
                } catch (Exception ex) {
                    mostrarError("No se pudo cambiar el estado de la configuración.", ex);
                }
            }
        }.execute();
    }

    private PlazoConfiguracionDTO leerFormulario() {
        PlazoConfiguracionDTO dto = new PlazoConfiguracionDTO();
        dto.setIdPlazoConfiguracion(idPlazoEditando);
        dto.setCodigo(txtCodigo.getText());
        dto.setNombre(txtNombre.getText());
        dto.setAmbito(txtAmbito.getText());
        dto.setDiasPlazo(Integer.valueOf(((Number) spnDias.getValue()).intValue()));
        dto.setUnidadPlazo(((UnidadItem) cmbUnidad.getSelectedItem()).codigo);
        dto.setFechaVigenciaDesde(parseFechaOpcional(txtVigenciaDesde.getText(), "vigencia desde"));
        dto.setFechaVigenciaHasta(parseFechaOpcional(txtVigenciaHasta.getText(), "vigencia hasta"));
        dto.setObservacion(txtObservacion.getText());
        dto.setActivo(chkActivo.isSelected());
        return dto;
    }

    private LocalDate parseFechaOpcional(String value, String campo) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ingrese " + campo + " con formato dd/MM/yyyy.");
        }
    }

    private void cargarSeleccion() {
        if (cargandoFormulario) {
            return;
        }
        int viewRow = tblPlazos.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = tblPlazos.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= plazos.size()) {
            return;
        }
        PlazoConfiguracionDTO dto = plazos.get(modelRow);
        cargandoFormulario = true;
        try {
            idPlazoEditando = dto.getIdPlazoConfiguracion();
            txtCodigo.setText(dto.getCodigo());
            txtNombre.setText(dto.getNombre());
            txtAmbito.setText(dto.getAmbito());
            spnDias.setValue(dto.getDiasPlazo() == null ? Integer.valueOf(30) : dto.getDiasPlazo());
            seleccionarUnidad(dto.getUnidadPlazo());
            txtVigenciaDesde.setText(dto.getFechaVigenciaDesde() == null ? "" : DATE_FORMAT.format(dto.getFechaVigenciaDesde()));
            txtVigenciaHasta.setText(dto.getFechaVigenciaHasta() == null ? "" : DATE_FORMAT.format(dto.getFechaVigenciaHasta()));
            txtObservacion.setText(dto.getObservacion());
            chkActivo.setSelected(dto.isActivo());
            mostrarPanelDetalle();
        } finally {
            cargandoFormulario = false;
        }
    }

    private void seleccionarUnidad(String unidad) {
        for (int i = 0; i < cmbUnidad.getItemCount(); i++) {
            UnidadItem item = cmbUnidad.getItemAt(i);
            if (item.codigo.equalsIgnoreCase(unidad)) {
                cmbUnidad.setSelectedIndex(i);
                return;
            }
        }
        cmbUnidad.setSelectedIndex(0);
    }

    private void nuevoPlazo() {
        idPlazoEditando = null;
        tblPlazos.clearSelection();
        txtCodigo.setText(PlazoConfiguracionDTO.CODIGO_SOLICITUD_SDRERC);
        txtNombre.setText("Plazo de atención de solicitudes SDRERC");
        txtAmbito.setText(PlazoConfiguracionDTO.CODIGO_SOLICITUD_SDRERC);
        spnDias.setValue(Integer.valueOf(30));
        cmbUnidad.setSelectedIndex(0);
        txtVigenciaDesde.setText("");
        txtVigenciaHasta.setText("");
        txtObservacion.setText("");
        chkActivo.setSelected(true);
        txtCodigo.requestFocusInWindow();
    }

    private void mostrarPanelDetalle() {
        if (splitDetalle != null) {
            splitDetalle.setSideVisible(true);
        }
    }

    private void limpiarFiltros() {
        txtBusqueda.setText("");
        cmbEstado.setSelectedIndex(0);
        cargarPlazos();
    }

    private void actualizarMetricas() {
        int activos = 0;
        String solicitud = "No configurado";
        for (PlazoConfiguracionDTO plazo : plazos) {
            if (plazo.isActivo()) {
                activos++;
            }
            if (PlazoConfiguracionDTO.CODIGO_SOLICITUD_SDRERC.equalsIgnoreCase(plazo.getCodigo()) && plazo.isActivo()) {
                solicitud = plazo.getDiasPlazo() + " " + (plazo.isHabiles() ? "hábiles" : "calendario");
            }
        }
        cardTotal.setValue(String.valueOf(plazos.size()));
        cardActivos.setValue(String.valueOf(activos));
        cardSolicitud.setValue(solicitud);
    }

    private void agregarFila(JPanel panel, int row, String label, Component component) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(label(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
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

    private void mostrarError(String mensaje, Exception ex) {
        Throwable causa = ex;
        if (ex instanceof java.util.concurrent.ExecutionException && ex.getCause() != null) {
            causa = ex.getCause();
        }
        String detalle = causa == null ? "" : causa.getMessage();
        if (detalle != null && (detalle.contains("ORA-00904") || detalle.contains("ORA-00942"))) {
            detalle = "Falta aplicar los scripts db/sdrerc_app/scripts/24_feriados_y_plazos_habiles.sql y 25_datos_maestros_plazos_feriados.sql.";
        }
        JOptionPane.showMessageDialog(this, mensaje + "\n" + detalle, "Plazos", JOptionPane.ERROR_MESSAGE);
    }

    private final class PlazosTableModel extends AbstractTableModel {
        private final String[] columns = {"Código", "Nombre", "Ámbito", "Días", "Unidad", "Estado", "Desde", "Hasta"};

        @Override
        public int getRowCount() {
            return plazos.size();
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
            PlazoConfiguracionDTO dto = plazos.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return dto.getCodigo();
                case 1:
                    return dto.getNombre();
                case 2:
                    return dto.getAmbito();
                case 3:
                    return dto.getDiasPlazo();
                case 4:
                    return dto.isHabiles() ? "Días hábiles" : "Días calendario";
                case 5:
                    return dto.isActivo() ? "Activo" : "Inactivo";
                case 6:
                    return dto.getFechaVigenciaDesde() == null ? "" : DATE_FORMAT.format(dto.getFechaVigenciaDesde());
                case 7:
                    return dto.getFechaVigenciaHasta() == null ? "" : DATE_FORMAT.format(dto.getFechaVigenciaHasta());
                default:
                    return "";
            }
        }
    }

    private static final class EstadoFiltroItem {
        private final String label;
        private final Boolean activo;

        private EstadoFiltroItem(String label, Boolean activo) {
            this.label = label;
            this.activo = activo;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class UnidadItem {
        private final String label;
        private final String codigo;

        private UnidadItem(String label, String codigo) {
            this.label = label;
            this.codigo = codigo;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final class PlazoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            int modelColumn = table.convertColumnIndexToModel(column);
            if (!isSelected && modelColumn == 4) {
                String text = value == null ? "" : value.toString();
                return new BadgeV2(
                        text,
                        text.toLowerCase().contains("hábil") ? AppV2Theme.SOFT_BLUE : AppV2Theme.SOFT_GRAY,
                        text.toLowerCase().contains("hábil") ? AppV2Theme.PRIMARY : AppV2Theme.TEXT_SECONDARY);
            }
            if (!isSelected && modelColumn == 5) {
                boolean activo = "Activo".equalsIgnoreCase(value == null ? "" : value.toString());
                return new BadgeV2(
                        activo ? "Activo" : "Inactivo",
                        activo ? AppV2Theme.SOFT_GREEN : AppV2Theme.SOFT_GRAY,
                        activo ? AppV2Theme.SUCCESS : AppV2Theme.TEXT_SECONDARY);
            }
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
                c.setForeground(AppV2Theme.TEXT_PRIMARY);
            }
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setToolTipText(value == null ? "" : String.valueOf(value));
            return c;
        }
    }
}
