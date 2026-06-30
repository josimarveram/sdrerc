package com.sdrerc.ui.views.administracion.feriados;

import com.sdrerc.application.sdrercapp.FeriadoNacionalService;
import com.sdrerc.domain.dto.sdrercapp.FeriadoNacionalDTO;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2ColumnFilterSupport;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.PremiumDateFieldV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class JPanelFeriadosV2 extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FeriadoNacionalService feriadoService;
    private final JSpinner spnAnio = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1));
    private final JComboBox<EstadoFiltroItem> cmbEstado = new JComboBox<EstadoFiltroItem>();
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnNuevo = new JButton("Nuevo feriado");
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnGuardar = new JButton("Guardar feriado");
    private final JButton btnActivarInactivar = new JButton("Activar / Inactivar");
    private final JButton btnImportarXml = new JButton("Importar XML");
    private final JButton btnRefrescar = new JButton("Refrescar");

    private final JLabel lblEstado = new JLabel("Los sábados y domingos se excluyen automáticamente. Aquí solo se registran feriados o días no laborables excepcionales.");
    private final PremiumDateFieldV2 fechaFeriadoField = new PremiumDateFieldV2();
    private final JTextField txtNombre = new JTextField(24);
    private final JTextField txtTipo = new JTextField(16);
    private final JTextArea txtObservacion = new JTextArea(5, 24);
    private final JCheckBox chkActivo = new JCheckBox("Feriado activo");

    private final FeriadosTableModel tableModel = new FeriadosTableModel();
    private final JTable tblFeriados = new AppV2Table(tableModel);
    private JScrollPane scrollFeriados;
    private AppV2OperationalSplitPanel splitDetalle;
    private final MetricCardV2 cardTotal = new MetricCardV2("Feriados", "0", "Resultado del año", AppV2Theme.PRIMARY);
    private final MetricCardV2 cardActivos = new MetricCardV2("Activos", "0", "Excluidos del plazo", AppV2Theme.SUCCESS);
    private final MetricCardV2 cardInactivos = new MetricCardV2("Inactivos", "0", "Sin efecto en el cálculo", AppV2Theme.WARNING);

    private final List<FeriadoNacionalDTO> feriados = new ArrayList<FeriadoNacionalDTO>();
    private Long idFeriadoEditando;
    private boolean cargandoFormulario;

    public JPanelFeriadosV2() {
        this(new FeriadoNacionalService());
    }

    public JPanelFeriadosV2(FeriadoNacionalService feriadoService) {
        this.feriadoService = feriadoService;
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());
        add(crearMetricas(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        configurarControles();
        configurarTabla();
        configurarEventos();
        nuevoFeriado();
        cargarFeriados();
    }

    private JPanel crearMetricas() {
        JPanel metricas = new JPanel(new GridLayout(1, 3, 12, 0));
        metricas.setOpaque(false);
        metricas.add(cardTotal);
        metricas.add(cardActivos);
        metricas.add(cardInactivos);
        return metricas;
    }

    private Component crearCentro() {
        splitDetalle = new AppV2OperationalSplitPanel(crearPanelListado(), crearPanelDetalle(), 540, 360, 430);
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
        filtros.add(label("Año"), gbc);
        gbc.gridx = 1;
        filtros.add(spnAnio, gbc);

        gbc.gridx = 2;
        filtros.add(label("Estado"), gbc);
        gbc.gridx = 3;
        filtros.add(cmbEstado, gbc);

        JPanel accionesFiltro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accionesFiltro.setOpaque(false);
        accionesFiltro.add(btnBuscar);
        accionesFiltro.add(btnLimpiar);
        gbc.gridx = 4;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        filtros.add(accionesFiltro, gbc);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnNuevo);
        acciones.add(btnEditar);
        acciones.add(btnActivarInactivar);
        acciones.add(btnImportarXml);
        acciones.add(btnRefrescar);

        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel barra = new JPanel(new BorderLayout(8, 8));
        barra.setOpaque(false);
        barra.add(filtros, BorderLayout.NORTH);
        barra.add(acciones, BorderLayout.CENTER);
        barra.add(lblEstado, BorderLayout.SOUTH);

        panel.add(barra, BorderLayout.NORTH);
        scrollFeriados = new JScrollPane(tblFeriados);
        panel.add(scrollFeriados, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelDetalle() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(430, 0));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.sectionBorder());

        JLabel title = new JLabel("Detalle del feriado");
        title.setFont(AppV2Theme.fontBold(18));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        int row = 0;
        agregarFila(form, row++, "Fecha", fechaFeriadoField);
        agregarFila(form, row++, "Nombre", txtNombre);
        agregarFila(form, row++, "Tipo", txtTipo);
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
        fechaFeriadoField.setDateFormatString("dd/MM/yyyy");
        fechaFeriadoField.setToolTipText("Seleccione la fecha del feriado");
        fechaFeriadoField.getDateChooser().setToolTipText("Seleccione la fecha del feriado");
        txtTipo.setText("NACIONAL");
        chkActivo.setSelected(true);
        txtObservacion.setLineWrap(true);
        txtObservacion.setWrapStyleWord(true);
        estilizarBotonPrimario(btnBuscar);
        estilizarBotonSecundario(btnLimpiar);
        estilizarBotonPrimario(btnNuevo);
        estilizarBotonSecundario(btnEditar);
        estilizarBotonSecundario(btnActivarInactivar);
        estilizarBotonPrimario(btnImportarXml);
        estilizarBotonSecundario(btnRefrescar);
        estilizarBotonPrimario(btnGuardar);
    }

    private void configurarTabla() {
        tblFeriados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblFeriados.setAutoCreateRowSorter(false);
        tblFeriados.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollFeriados.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tblFeriados.setDefaultRenderer(Object.class, new FeriadoCellRenderer());
        AppV2TableColumnSizer.applyWidths(tblFeriados, 120, 240, 120, 110, 260);
        AppV2ColumnFilterSupport.install("Administracion.Feriados", tblFeriados, scrollFeriados, null, null);
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> cargarFeriados());
        btnLimpiar.addActionListener(e -> limpiarFiltros());
        btnNuevo.addActionListener(e -> {
            nuevoFeriado();
            mostrarPanelDetalle();
        });
        btnEditar.addActionListener(e -> cargarSeleccion());
        btnGuardar.addActionListener(e -> guardarFeriado());
        btnActivarInactivar.addActionListener(e -> cambiarActivoSeleccionado());
        btnImportarXml.addActionListener(e -> importarXmlFeriados());
        btnRefrescar.addActionListener(e -> cargarFeriados());
        tblFeriados.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    cargarSeleccion();
                }
            }
        });
    }

    private void cargarFeriados() {
        final Integer anio = Integer.valueOf(((Number) spnAnio.getValue()).intValue());
        final Boolean activo = ((EstadoFiltroItem) cmbEstado.getSelectedItem()).activo;
        lblEstado.setText("Consultando feriados...");
        btnBuscar.setEnabled(false);
        new SwingWorker<List<FeriadoNacionalDTO>, Void>() {
            @Override
            protected List<FeriadoNacionalDTO> doInBackground() throws Exception {
                return feriadoService.buscar(anio, activo, 1000);
            }

            @Override
            protected void done() {
                btnBuscar.setEnabled(true);
                try {
                    feriados.clear();
                    feriados.addAll(get());
                    tableModel.fireTableDataChanged();
                    actualizarMetricas();
                    lblEstado.setText(feriados.size() + " feriado(s) encontrado(s).");
                } catch (Exception ex) {
                    lblEstado.setText("No se pudo consultar feriados. Verifique el script de configuración.");
                    mostrarError("No se pudo consultar feriados.", ex);
                }
            }
        }.execute();
    }

    private void importarXmlFeriados() {
        final Integer anio = Integer.valueOf(((Number) spnAnio.getValue()).intValue());
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Importar feriados XML - " + anio);
        chooser.setFileFilter(new FileNameExtensionFilter("Archivo XML de feriados (*.xml)", "xml"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        final File archivo = chooser.getSelectedFile();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Se importarán los feriados del año " + anio + ".\n"
                        + "Si el XML contiene fechas ya registradas, no se importará ningún feriado.\n\n"
                        + "¿Desea continuar?",
                "Importar XML",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        lblEstado.setText("Importando feriados desde XML...");
        btnImportarXml.setEnabled(false);
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return feriadoService.importarXmlPorAnio(archivo, anio);
            }

            @Override
            protected void done() {
                btnImportarXml.setEnabled(true);
                try {
                    int importados = get();
                    lblEstado.setText(importados + " feriado(s) importado(s) desde XML.");
                    JOptionPane.showMessageDialog(
                            JPanelFeriadosV2.this,
                            importados + " feriado(s) importado(s) correctamente.",
                            "Importar XML",
                            JOptionPane.INFORMATION_MESSAGE);
                    cargarFeriados();
                } catch (Exception ex) {
                    lblEstado.setText("No se importó el XML. Revise las validaciones.");
                    mostrarError("No se pudo importar el XML de feriados.", ex);
                }
            }
        }.execute();
    }

    private void guardarFeriado() {
        FeriadoNacionalDTO dto;
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
                return feriadoService.guardar(dto).getIdFeriado();
            }

            @Override
            protected void done() {
                btnGuardar.setEnabled(true);
                try {
                    idFeriadoEditando = get();
                    lblEstado.setText("Feriado guardado correctamente.");
                    cargarFeriados();
                } catch (Exception ex) {
                    mostrarError("No se pudo guardar el feriado.", ex);
                }
            }
        }.execute();
    }

    private void cambiarActivoSeleccionado() {
        int viewRow = tblFeriados.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un feriado.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tblFeriados.convertRowIndexToModel(viewRow);
        FeriadoNacionalDTO seleccionado = feriados.get(modelRow);
        final boolean nuevoEstado = !seleccionado.isActivo();
        btnActivarInactivar.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                feriadoService.cambiarActivo(seleccionado.getIdFeriado(), nuevoEstado);
                return null;
            }

            @Override
            protected void done() {
                btnActivarInactivar.setEnabled(true);
                try {
                    get();
                    lblEstado.setText(nuevoEstado ? "Feriado activado." : "Feriado inactivado.");
                    cargarFeriados();
                } catch (Exception ex) {
                    mostrarError("No se pudo cambiar el estado del feriado.", ex);
                }
            }
        }.execute();
    }

    private FeriadoNacionalDTO leerFormulario() {
        FeriadoNacionalDTO dto = new FeriadoNacionalDTO();
        dto.setIdFeriado(idFeriadoEditando);
        dto.setFecha(leerFechaFeriado());
        dto.setNombre(txtNombre.getText());
        dto.setTipo(txtTipo.getText());
        dto.setObservacion(txtObservacion.getText());
        dto.setActivo(chkActivo.isSelected());
        return dto;
    }

    private LocalDate leerFechaFeriado() {
        Date fecha = fechaFeriadoField.getDate();
        if (fecha == null) {
            throw new IllegalArgumentException("Seleccione la fecha del feriado.");
        }
        if (fecha instanceof java.sql.Date) {
            return ((java.sql.Date) fecha).toLocalDate();
        }
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date toDate(LocalDate fecha) {
        return fecha == null ? null : java.sql.Date.valueOf(fecha);
    }

    private void cargarSeleccion() {
        if (cargandoFormulario) {
            return;
        }
        int viewRow = tblFeriados.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = tblFeriados.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= feriados.size()) {
            return;
        }
        FeriadoNacionalDTO dto = feriados.get(modelRow);
        cargandoFormulario = true;
        try {
            idFeriadoEditando = dto.getIdFeriado();
            fechaFeriadoField.setDate(toDate(dto.getFecha()));
            txtNombre.setText(dto.getNombre());
            txtTipo.setText(dto.getTipo());
            txtObservacion.setText(dto.getObservacion());
            chkActivo.setSelected(dto.isActivo());
            mostrarPanelDetalle();
        } finally {
            cargandoFormulario = false;
        }
    }

    private void nuevoFeriado() {
        idFeriadoEditando = null;
        tblFeriados.clearSelection();
        fechaFeriadoField.setDate(null);
        txtNombre.setText("");
        txtTipo.setText("NACIONAL");
        txtObservacion.setText("");
        chkActivo.setSelected(true);
        fechaFeriadoField.requestFocusInWindow();
    }

    private void mostrarPanelDetalle() {
        if (splitDetalle != null) {
            splitDetalle.setSideVisible(true);
        }
    }

    private void limpiarFiltros() {
        spnAnio.setValue(LocalDate.now().getYear());
        cmbEstado.setSelectedIndex(0);
        cargarFeriados();
    }

    private void actualizarMetricas() {
        int activos = 0;
        int inactivos = 0;
        for (FeriadoNacionalDTO feriado : feriados) {
            if (feriado.isActivo()) {
                activos++;
            } else {
                inactivos++;
            }
        }
        cardTotal.setValue(String.valueOf(feriados.size()));
        cardActivos.setValue(String.valueOf(activos));
        cardInactivos.setValue(String.valueOf(inactivos));
    }

    private void agregarFila(JPanel panel, int row, String label, Component component) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lbl = label(label);
        panel.add(lbl, gbc);

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
        if (causa instanceof SQLException && detalle != null && detalle.contains("ORA-00942")) {
            detalle = "Falta aplicar el script db/sdrerc_app/scripts/24_feriados_y_plazos_habiles.sql.";
        }
        JOptionPane.showMessageDialog(this, mensaje + "\n" + detalle, "Feriados", JOptionPane.ERROR_MESSAGE);
    }

    private final class FeriadosTableModel extends AbstractTableModel {
        private final String[] columns = {"Fecha", "Nombre", "Tipo", "Estado", "Observación"};

        @Override
        public int getRowCount() {
            return feriados.size();
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
            FeriadoNacionalDTO dto = feriados.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return dto.getFecha() == null ? "" : DATE_FORMAT.format(dto.getFecha());
                case 1:
                    return dto.getNombre();
                case 2:
                    return dto.getTipo();
                case 3:
                    return dto.isActivo() ? "Activo" : "Inactivo";
                case 4:
                    return dto.getObservacion();
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

    private final class FeriadoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            int modelColumn = table.convertColumnIndexToModel(column);
            if (!isSelected && modelColumn == 3) {
                boolean activo = "Activo".equalsIgnoreCase(value == null ? "" : value.toString());
                BadgeV2 badge = new BadgeV2(
                        activo ? "Activo" : "Inactivo",
                        activo ? AppV2Theme.SOFT_GREEN : AppV2Theme.SOFT_GRAY,
                        activo ? AppV2Theme.SUCCESS : AppV2Theme.TEXT_SECONDARY);
                badge.setToolTipText(activo ? "Feriado considerado en el cálculo" : "Feriado sin efecto en el cálculo");
                return badge;
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
