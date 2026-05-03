package com.sdrerc.ui.views.equipojuridico;

import com.sdrerc.application.EquipoJuridicoService;
import com.sdrerc.application.EquipoJuridicoConsultaService;
import com.sdrerc.domain.model.EquipoJuridicoConsultaItem;
import com.sdrerc.domain.model.EquipoJuridicoImportPreview;
import com.sdrerc.domain.model.EquipoJuridicoImportResult;
import com.sdrerc.domain.model.EquipoJuridicoResumen;
import com.sdrerc.domain.model.PaginatedResult;
import com.sdrerc.domain.model.SupervisorComboItem;
import com.sdrerc.ui.table.ButtonRenderer;
import com.sdrerc.ui.views.usuario.DlgPrevisualizarEquipoJuridicoExcel;
import com.sdrerc.ui.views.usuario.DlgRegistrarEquipoJuridico;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class JPanelEquipoJuridico extends JPanel implements EquipoJuridicoImportOwner {

    private static final int COL_ABOGADO = 0;
    private static final int COL_USERNAME = 1;
    private static final int COL_SUPERVISOR = 2;
    private static final int COL_TIPO_PERSONAL = 3;
    private static final int COL_ESTADO = 4;
    private static final int COL_EDITAR = 5;

    private final EquipoJuridicoService equipoJuridicoService = new EquipoJuridicoService();
    private final EquipoJuridicoConsultaService consultaService = new EquipoJuridicoConsultaService();
    private final JButton btnNuevoEquipoJuridico = new JButton("Nuevo abogado/supervisor");
    private final JButton btnDescargarPlantilla = new JButton("Descargar plantilla Excel");
    private final JButton btnPrevisualizarExcel = new JButton("Previsualizar Excel");
    private final JComboBox<SupervisorComboItem> cboSupervisor = new JComboBox<>();
    private final JComboBox<String> cboEstado = new JComboBox<>(new String[]{"TODOS", "ACTIVO", "INACTIVO"});
    private final JTextField txtBuscar = new JTextField();
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JLabel lblTotalAbogados = new JLabel("0");
    private final JLabel lblTotalSupervisores = new JLabel("0");
    private final JLabel lblSinSupervisor = new JLabel("0");
    private final JLabel lblActivos = new JLabel("0");
    private final JLabel lblInactivos = new JLabel("0");
    private final DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblEquipo = new JTable(model);
    private final List<EquipoJuridicoConsultaItem> abogadosPaginaActual = new ArrayList<>();
    private final JButton btnPrimeraPagina = new JButton("Primera");
    private final JButton btnPaginaAnterior = new JButton("Anterior");
    private final JButton btnPaginaSiguiente = new JButton("Siguiente");
    private final JButton btnUltimaPagina = new JButton("Última");
    private final JLabel lblPagina = new JLabel("Página 1 de 1");
    private final JLabel lblResumenPagina = new JLabel("Mostrando 0-0 de 0 abogados");
    private final JComboBox<Integer> cboFilasPorPagina = new JComboBox<>(new Integer[]{10, 25, 50, 100});
    private int currentPage = 1;
    private int pageSize = 25;
    private int totalRecords = 0;
    private int totalPages = 1;
    private boolean cargandoFiltros = false;

    public JPanelEquipoJuridico() {
        configurarLayout();
        configurarBotones();
        configurarTabla();
        configurarEventosConsulta();
        cargarDatosEquipoJuridico();
    }

    private void configurarLayout() {
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        setBackground(new Color(245, 247, 250));

        JLabel titulo = new JLabel("Equipo Jurídico");
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        titulo.setForeground(new Color(25, 42, 62));

        JLabel subtitulo = new JLabel("Gestión de abogados, supervisores e importación masiva");
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(93, 105, 119));

        JPanel headerText = new JPanel(new GridBagLayout());
        headerText.setOpaque(false);
        GridBagConstraints gbcHeader = new GridBagConstraints();
        gbcHeader.gridx = 0;
        gbcHeader.gridy = 0;
        gbcHeader.anchor = GridBagConstraints.WEST;
        headerText.add(titulo, gbcHeader);
        gbcHeader.gridy = 1;
        gbcHeader.insets = new Insets(4, 0, 0, 0);
        headerText.add(subtitulo, gbcHeader);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        acciones.add(btnNuevoEquipoJuridico);
        acciones.add(btnDescargarPlantilla);
        acciones.add(btnPrevisualizarExcel);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(headerText, BorderLayout.CENTER);
        header.add(acciones, BorderLayout.EAST);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));
        JLabel info = new JLabel("Use este módulo para registrar abogados y supervisores, descargar la plantilla oficial e importar información del equipo jurídico.");
        info.setForeground(new Color(73, 85, 99));
        info.setFont(new Font("Arial", Font.PLAIN, 14));
        infoPanel.add(info, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.add(infoPanel, BorderLayout.NORTH);
        content.add(crearPanelConsulta(), BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    private JPanel crearPanelConsulta() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);
        panel.add(crearPanelResumen(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.add(crearPanelFiltros(), BorderLayout.NORTH);
        centro.add(crearPanelTabla(), BorderLayout.CENTER);
        panel.add(centro, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelResumen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        panel.add(crearTarjetaResumen("Total abogados", lblTotalAbogados), gbc);
        gbc.gridx = 1;
        panel.add(crearTarjetaResumen("Total supervisores", lblTotalSupervisores), gbc);
        gbc.gridx = 2;
        panel.add(crearTarjetaResumen("Sin supervisor", lblSinSupervisor), gbc);
        gbc.gridx = 3;
        panel.add(crearTarjetaResumen("Activos", lblActivos), gbc);
        gbc.gridx = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(crearTarjetaResumen("Inactivos", lblInactivos), gbc);
        return panel;
    }

    private JPanel crearTarjetaResumen(String titulo, JLabel valor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        JLabel label = new JLabel(titulo);
        label.setForeground(new Color(93, 105, 119));
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        valor.setForeground(new Color(25, 42, 62));
        valor.setFont(new Font("Arial", Font.BOLD, 22));
        card.add(label, BorderLayout.NORTH);
        card.add(valor, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearPanelFiltros() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        txtBuscar.setPreferredSize(new Dimension(280, 34));
        cboSupervisor.setPreferredSize(new Dimension(280, 34));
        cboEstado.setPreferredSize(new Dimension(130, 34));
        btnBuscar.setPreferredSize(new Dimension(96, 34));
        btnLimpiar.setPreferredSize(new Dimension(96, 34));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        panel.add(new JLabel("Supervisor"), gbc);
        gbc.gridx = 1;
        panel.add(cboSupervisor, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Buscar abogado o usuario"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtBuscar, gbc);
        gbc.gridx = 4;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Estado"), gbc);
        gbc.gridx = 5;
        panel.add(cboEstado, gbc);
        gbc.gridx = 6;
        panel.add(btnBuscar, gbc);
        gbc.gridx = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(btnLimpiar, gbc);
        return panel;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(tblEquipo);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(218, 224, 231)));
        scroll.setPreferredSize(new Dimension(980, 360));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(crearPanelPaginacion(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelPaginacion() {
        JPanel paginationPanel = new JPanel(new BorderLayout(12, 0));
        paginationPanel.setBackground(Color.WHITE);
        paginationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        lblResumenPagina.setForeground(new Color(73, 85, 99));
        paginationPanel.add(lblResumenPagina, BorderLayout.WEST);

        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 8);
        gbc.gridy = 0;

        controlsPanel.add(btnPrimeraPagina, gbc);
        controlsPanel.add(btnPaginaAnterior, gbc);
        controlsPanel.add(lblPagina, gbc);
        controlsPanel.add(btnPaginaSiguiente, gbc);
        controlsPanel.add(btnUltimaPagina, gbc);
        controlsPanel.add(new JLabel("Filas por página"), gbc);
        gbc.insets = new Insets(0, 0, 0, 0);
        controlsPanel.add(cboFilasPorPagina, gbc);

        paginationPanel.add(controlsPanel, BorderLayout.EAST);
        return paginationPanel;
    }

    private JPanel crearPanelAcciones() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 12);
        panel.add(btnNuevoEquipoJuridico, gbc);
        gbc.gridx = 1;
        panel.add(btnDescargarPlantilla, gbc);
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(btnPrevisualizarExcel, gbc);
        return panel;
    }

    private void configurarBotones() {
        Dimension principal = new Dimension(210, 38);
        Dimension plantilla = new Dimension(190, 38);
        Dimension previsualizar = new Dimension(170, 38);

        btnNuevoEquipoJuridico.setPreferredSize(principal);
        btnDescargarPlantilla.setPreferredSize(plantilla);
        btnPrevisualizarExcel.setPreferredSize(previsualizar);

        btnNuevoEquipoJuridico.setToolTipText("Registrar abogado o supervisor creando técnico, usuario y roles");
        btnDescargarPlantilla.setToolTipText("Descargar plantilla oficial para carga masiva de equipo jurídico");
        btnPrevisualizarExcel.setToolTipText("Leer plantilla Excel y previsualizar validaciones sin grabar en base de datos");

        btnNuevoEquipoJuridico.addActionListener(e -> abrirRegistroEquipoJuridico());
        btnDescargarPlantilla.addActionListener(e -> descargarPlantillaEquipoJuridico());
        btnPrevisualizarExcel.addActionListener(e -> previsualizarPlantillaEquipoJuridico());
    }

    private void configurarTabla() {
        model.setColumnIdentifiers(new Object[]{"Abogado", "Username", "Supervisor", "Tipo personal", "Estado", "Editar"});
        tblEquipo.setModel(model);
        tblEquipo.setRowHeight(36);
        tblEquipo.setFillsViewportHeight(true);
        tblEquipo.setShowGrid(false);
        tblEquipo.setIntercellSpacing(new Dimension(0, 0));
        tblEquipo.setSelectionBackground(new Color(219, 235, 247));
        tblEquipo.setSelectionForeground(new Color(25, 42, 62));

        JTableHeader header = tblEquipo.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        header.setReorderingAllowed(false);

        tblEquipo.getColumnModel().getColumn(COL_ABOGADO).setPreferredWidth(300);
        tblEquipo.getColumnModel().getColumn(COL_USERNAME).setPreferredWidth(140);
        tblEquipo.getColumnModel().getColumn(COL_SUPERVISOR).setPreferredWidth(280);
        tblEquipo.getColumnModel().getColumn(COL_TIPO_PERSONAL).setPreferredWidth(190);
        tblEquipo.getColumnModel().getColumn(COL_ESTADO).setPreferredWidth(110);
        tblEquipo.getColumnModel().getColumn(COL_EDITAR).setPreferredWidth(90);
        tblEquipo.getColumnModel().getColumn(COL_ESTADO).setCellRenderer(new EstadoEquipoRenderer());
        tblEquipo.getColumnModel().getColumn(COL_EDITAR).setCellRenderer(new ButtonRenderer("Editar"));
    }

    private void configurarEventosConsulta() {
        btnBuscar.addActionListener(e -> resetearPaginacionAlBuscar());
        btnLimpiar.addActionListener(e -> limpiarFiltros());
        txtBuscar.addActionListener(e -> resetearPaginacionAlBuscar());
        cboSupervisor.addActionListener(e -> {
            if (!cargandoFiltros) {
                resetearPaginacionAlBuscar();
            }
        });
        cboEstado.addActionListener(e -> {
            if (!cargandoFiltros) {
                resetearPaginacionAlBuscar();
            }
        });

        Dimension botonPaginacion = new Dimension(86, 30);
        btnPrimeraPagina.setPreferredSize(botonPaginacion);
        btnPaginaAnterior.setPreferredSize(botonPaginacion);
        btnPaginaSiguiente.setPreferredSize(botonPaginacion);
        btnUltimaPagina.setPreferredSize(botonPaginacion);
        cboFilasPorPagina.setPreferredSize(new Dimension(76, 30));
        cboFilasPorPagina.setSelectedItem(pageSize);

        btnPrimeraPagina.addActionListener(e -> irPrimeraPagina());
        btnPaginaAnterior.addActionListener(e -> irPaginaAnterior());
        btnPaginaSiguiente.addActionListener(e -> irPaginaSiguiente());
        btnUltimaPagina.addActionListener(e -> irUltimaPagina());
        cboFilasPorPagina.addActionListener(e -> {
            Object selected = cboFilasPorPagina.getSelectedItem();
            if (selected instanceof Integer) {
                pageSize = (Integer) selected;
                currentPage = 1;
                cargarPaginaAbogados();
            }
        });

        tblEquipo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tblEquipo.rowAtPoint(evt.getPoint());
                int col = tblEquipo.columnAtPoint(evt.getPoint());
                if (row >= 0 && col == COL_EDITAR) {
                    abrirEdicionDesdeTabla(row);
                }
            }
        });
    }

    private void cargarDatosEquipoJuridico() {
        cargarResumen();
        cargarSupervisores();
        currentPage = 1;
        cargarPaginaAbogados();
    }

    private void cargarResumen() {
        try {
            EquipoJuridicoResumen resumen = consultaService.obtenerResumen();
            lblTotalAbogados.setText(String.valueOf(resumen.getTotalAbogados()));
            lblTotalSupervisores.setText(String.valueOf(resumen.getTotalSupervisores()));
            lblSinSupervisor.setText(String.valueOf(resumen.getAbogadosSinSupervisor()));
            lblActivos.setText(String.valueOf(resumen.getActivos()));
            lblInactivos.setText(String.valueOf(resumen.getInactivos()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el resumen: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarSupervisores() {
        cargandoFiltros = true;
        try {
            cboSupervisor.removeAllItems();
            cboSupervisor.addItem(SupervisorComboItem.todos());
            cboSupervisor.addItem(SupervisorComboItem.sinSupervisor());
            for (SupervisorComboItem item : consultaService.listarSupervisoresActivos()) {
                cboSupervisor.addItem(item);
            }
            cboSupervisor.setSelectedIndex(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar supervisores: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            cargandoFiltros = false;
        }
    }

    private void cargarPaginaAbogados() {
        try {
            model.setRowCount(0);
            abogadosPaginaActual.clear();
            SupervisorComboItem supervisor = (SupervisorComboItem) cboSupervisor.getSelectedItem();
            boolean soloSinSupervisor = supervisor != null && SupervisorComboItem.TIPO_SIN_SUPERVISOR.equals(supervisor.getTipo());
            Long supervisorId = supervisor != null && SupervisorComboItem.TIPO_SUPERVISOR.equals(supervisor.getTipo())
                    ? supervisor.getUserId()
                    : null;
            String estado = cboEstado.getSelectedItem() == null ? "TODOS" : cboEstado.getSelectedItem().toString();

            PaginatedResult<EquipoJuridicoConsultaItem> result = consultaService.buscarAbogados(
                    supervisorId,
                    soloSinSupervisor,
                    txtBuscar.getText(),
                    estado,
                    currentPage,
                    pageSize
            );

            currentPage = result.getPage();
            pageSize = result.getPageSize();
            totalRecords = result.getTotalRecords();
            totalPages = result.getTotalPages();

            abogadosPaginaActual.addAll(result.getData());
            for (EquipoJuridicoConsultaItem item : result.getData()) {
                model.addRow(new Object[]{
                    item.getAbogadoNombre(),
                    item.getAbogadoUsername(),
                    item.getSupervisorNombre() == null || item.getSupervisorNombre().trim().isEmpty() ? "Sin supervisor" : item.getSupervisorNombre(),
                    item.getTipoPersonal() == null || item.getTipoPersonal().trim().isEmpty() ? "" : item.getTipoPersonal(),
                    item.getEstado(),
                    "Editar"
                });
            }

            if (model.getRowCount() == 0 && totalRecords > 0 && currentPage > 1) {
                currentPage--;
                cargarPaginaAbogados();
                return;
            }

            actualizarControlesPaginacion();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar abogados: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirEdicionDesdeTabla(int row) {
        int modelRow = tblEquipo.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= abogadosPaginaActual.size()) {
            JOptionPane.showMessageDialog(this, "Seleccione un abogado valido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        EquipoJuridicoConsultaItem item = abogadosPaginaActual.get(modelRow);
        Window parent = SwingUtilities.getWindowAncestor(this);
        DlgEditarEquipoJuridico dlg = new DlgEditarEquipoJuridico(parent, consultaService, equipoJuridicoService, item.getAbogadoId());
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (dlg.isGuardado()) {
            SupervisorComboItem supervisorActual = (SupervisorComboItem) cboSupervisor.getSelectedItem();
            cargarResumen();
            cargarSupervisores();
            seleccionarSupervisorFiltro(supervisorActual);
            cargarPaginaAbogados();
        }
    }

    private void seleccionarSupervisorFiltro(SupervisorComboItem supervisorActual) {
        if (supervisorActual == null) {
            return;
        }
        cargandoFiltros = true;
        try {
            for (int i = 0; i < cboSupervisor.getItemCount(); i++) {
                SupervisorComboItem item = cboSupervisor.getItemAt(i);
                if (item == null) {
                    continue;
                }
                if (supervisorActual.getTipo().equals(item.getTipo())
                        && (supervisorActual.getUserId() == null
                        || supervisorActual.getUserId().equals(item.getUserId()))) {
                    cboSupervisor.setSelectedIndex(i);
                    return;
                }
            }
        } finally {
            cargandoFiltros = false;
        }
    }

    private void limpiarFiltros() {
        cargandoFiltros = true;
        txtBuscar.setText("");
        cboEstado.setSelectedIndex(0);
        cboSupervisor.setSelectedIndex(0);
        cargandoFiltros = false;
        resetearPaginacionAlBuscar();
    }

    private void resetearPaginacionAlBuscar() {
        currentPage = 1;
        cargarPaginaAbogados();
    }

    private void irPrimeraPagina() {
        if (currentPage != 1) {
            currentPage = 1;
            cargarPaginaAbogados();
        }
    }

    private void irPaginaAnterior() {
        if (currentPage > 1) {
            currentPage--;
            cargarPaginaAbogados();
        }
    }

    private void irPaginaSiguiente() {
        if (currentPage < totalPages) {
            currentPage++;
            cargarPaginaAbogados();
        }
    }

    private void irUltimaPagina() {
        if (currentPage != totalPages) {
            currentPage = totalPages;
            cargarPaginaAbogados();
        }
    }

    private void actualizarControlesPaginacion() {
        int from = totalRecords == 0 ? 0 : ((currentPage - 1) * pageSize) + 1;
        int to = totalRecords == 0 ? 0 : Math.min(currentPage * pageSize, totalRecords);

        lblPagina.setText("Página " + currentPage + " de " + totalPages);
        lblResumenPagina.setText("Mostrando " + from + "-" + to + " de " + totalRecords + " abogados");

        boolean hasPrevious = currentPage > 1;
        boolean hasNext = currentPage < totalPages;
        btnPrimeraPagina.setEnabled(hasPrevious);
        btnPaginaAnterior.setEnabled(hasPrevious);
        btnPaginaSiguiente.setEnabled(hasNext);
        btnUltimaPagina.setEnabled(hasNext);
    }

    private void abrirRegistroEquipoJuridico() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        DlgRegistrarEquipoJuridico dlg = new DlgRegistrarEquipoJuridico(parent, equipoJuridicoService);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        cargarDatosEquipoJuridico();
    }

    private void descargarPlantillaEquipoJuridico() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar plantilla Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));
        chooser.setSelectedFile(new File("plantilla_equipo_juridico_sdrerc.xlsx"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destino = asegurarExtensionXlsx(chooser.getSelectedFile());
        if (destino.exists()) {
            int confirmar = JOptionPane.showConfirmDialog(
                    this,
                    "El archivo ya existe. ¿Desea reemplazarlo?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirmar != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            generarPlantillaEquipoJuridico(destino);
            if (!destino.exists() || destino.length() == 0) {
                throw new IllegalStateException("El archivo no se generó en la ruta seleccionada.");
            }
            JOptionPane.showMessageDialog(
                    this,
                    "Plantilla Excel generada correctamente:\n" + destino.getAbsolutePath(),
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Throwable ex) {
            Throwable causa = obtenerCausaReal(ex);
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo generar la plantilla Excel: " + causa.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private File asegurarExtensionXlsx(File file) {
        String path = file.getAbsolutePath();
        if (path.toLowerCase().endsWith(".xlsx")) {
            return file;
        }
        return new File(path + ".xlsx");
    }

    private void generarPlantillaEquipoJuridico(File destino) throws Exception {
        try {
            Class<?> serviceClass = Class.forName("com.sdrerc.application.EquipoJuridicoPlantillaService");
            Object service = serviceClass.getDeclaredConstructor().newInstance();
            serviceClass.getMethod("generarPlantilla", File.class).invoke(service, destino);
        } catch (Throwable ex) {
            Class<?> fallbackClass = Class.forName("com.sdrerc.application.EquipoJuridicoPlantillaSimpleService");
            Object fallback = fallbackClass.getDeclaredConstructor().newInstance();
            fallbackClass.getMethod("generarPlantilla", File.class).invoke(fallback, destino);
        }
    }

    private void previsualizarPlantillaEquipoJuridico() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar plantilla Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            EquipoJuridicoImportPreview preview = previsualizarEquipoJuridico(chooser.getSelectedFile());
            Window parent = SwingUtilities.getWindowAncestor(this);
            DlgPrevisualizarEquipoJuridicoExcel dlg = new DlgPrevisualizarEquipoJuridicoExcel(parent, this, preview);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        } catch (Throwable ex) {
            Throwable causa = obtenerCausaReal(ex);
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo previsualizar la plantilla Excel: " + causa.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private EquipoJuridicoImportPreview previsualizarEquipoJuridico(File archivo) throws Exception {
        try {
            Class<?> serviceClass = Class.forName("com.sdrerc.application.EquipoJuridicoImportService");
            Object service = serviceClass.getDeclaredConstructor().newInstance();
            Object preview = serviceClass.getMethod("previsualizar", File.class).invoke(service, archivo);
            return (EquipoJuridicoImportPreview) preview;
        } catch (Throwable ex) {
            if (!esErrorDependenciaPoi(ex)) {
                throw convertirException(ex);
            }
            Class<?> fallbackClass = Class.forName("com.sdrerc.application.EquipoJuridicoImportSimpleService");
            Object fallback = fallbackClass.getDeclaredConstructor().newInstance();
            Object preview = fallbackClass.getMethod("previsualizar", File.class).invoke(fallback, archivo);
            return (EquipoJuridicoImportPreview) preview;
        }
    }

    @Override
    public EquipoJuridicoImportResult confirmarImportacionEquipoJuridico(
            EquipoJuridicoImportPreview preview,
            boolean incluirAdvertencias) throws Exception {
        try {
            Class<?> serviceClass = Class.forName("com.sdrerc.application.EquipoJuridicoImportService");
            Object service = serviceClass.getDeclaredConstructor().newInstance();
            Object result = serviceClass
                    .getMethod("confirmarImportacion", EquipoJuridicoImportPreview.class, boolean.class)
                    .invoke(service, preview, incluirAdvertencias);
            cargarDatosEquipoJuridico();
            return (EquipoJuridicoImportResult) result;
        } catch (Throwable ex) {
            if (!esErrorDependenciaPoi(ex)) {
                throw convertirException(ex);
            }
            Class<?> fallbackClass = Class.forName("com.sdrerc.application.EquipoJuridicoImportSimpleService");
            Object fallback = fallbackClass.getDeclaredConstructor().newInstance();
            Object result = fallbackClass
                    .getMethod("confirmarImportacion", EquipoJuridicoImportPreview.class, boolean.class)
                    .invoke(fallback, preview, incluirAdvertencias);
            cargarDatosEquipoJuridico();
            return (EquipoJuridicoImportResult) result;
        }
    }

    private boolean esErrorDependenciaPoi(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage() == null ? "" : current.getMessage();
            if (message.contains("org.apache.poi") || current instanceof NoClassDefFoundError) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private Exception convertirException(Throwable ex) {
        Throwable causa = obtenerCausaReal(ex);
        if (causa instanceof Exception) {
            return (Exception) causa;
        }
        return new Exception(causa.getMessage(), causa);
    }

    private Throwable obtenerCausaReal(Throwable ex) {
        if (ex instanceof InvocationTargetException && ((InvocationTargetException) ex).getTargetException() != null) {
            return ((InvocationTargetException) ex).getTargetException();
        }
        return ex.getCause() != null ? ex.getCause() : ex;
    }

    private static class EstadoEquipoRenderer extends DefaultTableCellRenderer {

        @Override
        public java.awt.Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String estado = value == null ? "" : value.toString();
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));

            if ("ACTIVE".equalsIgnoreCase(estado) || "ACTIVO".equalsIgnoreCase(estado)) {
                label.setText("ACTIVO");
                if (!isSelected) {
                    label.setForeground(new Color(24, 112, 70));
                    label.setBackground(new Color(225, 244, 235));
                }
            } else if ("INACTIVE".equalsIgnoreCase(estado) || "INACTIVO".equalsIgnoreCase(estado)) {
                label.setText("INACTIVO");
                if (!isSelected) {
                    label.setForeground(new Color(143, 48, 48));
                    label.setBackground(new Color(250, 230, 230));
                }
            } else {
                label.setText(estado);
                if (!isSelected) {
                    label.setForeground(new Color(73, 85, 99));
                    label.setBackground(Color.WHITE);
                }
            }
            label.setOpaque(true);
            return label;
        }
    }
}
