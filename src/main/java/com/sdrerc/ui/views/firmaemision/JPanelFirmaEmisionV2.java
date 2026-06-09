package com.sdrerc.ui.views.firmaemision;

import com.sdrerc.application.sdrercapp.DocumentoFirmaService;
import com.sdrerc.application.sdrercapp.FirmaEmisionExpedienteService;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoFirmaDTO;
import com.sdrerc.domain.dto.sdrercapp.FirmaEmisionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.FirmaEmisionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.FirmaEmisionResultadoDTO;
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import com.sdrerc.ui.views.expedienteconsola.DlgConsolaExpedienteV2;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.AbstractTableModel;

public class JPanelFirmaEmisionV2 extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final FirmaEmisionExpedienteService firmaEmisionService;
    private final DocumentoFirmaService documentoFirmaService;

    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite, titular o resolución", 28);
    private final JComboBox<SimpleItem> cmbEstadoFiltro = new JComboBox<SimpleItem>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnVerDetalle = new JButton("Ver detalle");
    private final JButton btnRegistrarFirma = new JButton("Registrar firma");
    private final JButton btnRegistrarEmision = new JButton("Registrar emisión");
    private final JButton btnRegistrarNumero = new JButton("Registrar número resolución");
    private final JButton btnEnviarEjecucion = new JButton("Enviar a Ejecución");

    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar expedientes en Firma / Emisión.");
    private final JLabel lblExpediente = new JLabel("-");
    private final JLabel lblTitular = new JLabel("-");
    private final JLabel lblActa = new JLabel("-");
    private final JLabel lblProcedimiento = new JLabel("-");
    private final JLabel lblEtapaEstado = new JLabel("-");
    private final JLabel lblAnalisis = new JLabel("-");
    private final JLabel lblVerificacion = new JLabel("-");
    private final JLabel lblResolucion = new JLabel("-");
    private final JLabel lblAlertas = new JLabel("Sin alertas.");

    private final JComboBox<SimpleItem> cmbTipoResolucion = new JComboBox<SimpleItem>();
    private final JTextField txtNumeroResolucion = new JTextField(18);
    private final JTextField txtFechaFirma = new JTextField(10);
    private final JTextField txtFechaEmision = new JTextField(10);
    private final JTextField txtFechaResolucion = new JTextField(10);
    private final JTextArea txtComentario = new JTextArea(4, 22);
    private final JTextArea txtFundamentoAnalisis = new JTextArea(4, 22);
    private final JTextArea txtObservacion = new JTextArea(3, 22);

    private final FirmaEmisionTableModel tableModel = new FirmaEmisionTableModel();
    private final JTable table = new AppV2Table(tableModel);
    private final DefaultTableModel documentosModel = new DefaultTableModel(
            new Object[]{"Tipo", "Estado", "Número", "Documento", "Fecha"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable documentosTable = new JTable(documentosModel);
    private final List<FirmaEmisionExpedienteDTO> expedientes = new ArrayList<FirmaEmisionExpedienteDTO>();

    private final MetricCardV2 cardParaFirma = new MetricCardV2("Para firma", "0", "Pendientes de firma", AppV2Theme.INFO);
    private final MetricCardV2 cardEnEmision = new MetricCardV2("En emisión", "0", "Firmados o emitidos", AppV2Theme.WARNING);
    private final MetricCardV2 cardNumerados = new MetricCardV2("Numerados", "0", "Listos para Ejecución", AppV2Theme.SUCCESS);

    public JPanelFirmaEmisionV2() {
        this(new FirmaEmisionExpedienteService(), new DocumentoFirmaService());
    }

    public JPanelFirmaEmisionV2(
            FirmaEmisionExpedienteService firmaEmisionService,
            DocumentoFirmaService documentoFirmaService) {
        this.firmaEmisionService = firmaEmisionService;
        this.documentoFirmaService = documentoFirmaService;
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());
        add(crearHeader(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        configurarTabla();
        configurarDocumentosTabla();
        configurarEventos();
        cargarFiltrosBase();
        cargarTiposResolucion();
        inicializarFechas();
        actualizarSeleccion();
    }

    private JPanel crearHeader() {
        JPanel metricas = new JPanel(new GridLayout(1, 3, 12, 0));
        metricas.setOpaque(false);
        metricas.add(cardParaFirma);
        metricas.add(cardEnEmision);
        metricas.add(cardNumerados);
        return metricas;
    }

    private JPanel crearCentro() {
        JPanel centro = new JPanel(new BorderLayout(14, 14));
        centro.setOpaque(false);
        centro.add(crearBandeja(), BorderLayout.CENTER);
        centro.add(crearPanelFirmaEmision(), BorderLayout.EAST);
        return centro;
    }

    private JPanel crearBandeja() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JPanel filtros = new JPanel(new GridBagLayout());
        filtros.setBackground(AppV2Theme.SURFACE);
        filtros.setBorder(AppV2Theme.toolbarBorder());
        configurarControles();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;
        gbc.gridx = 0;
        filtros.add(label("Búsqueda"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtros.add(txtBusqueda, gbc);
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 2;
        filtros.add(label("Estado"), gbc);
        gbc.gridx = 3;
        filtros.add(cmbEstadoFiltro, gbc);
        gbc.gridx = 4;
        filtros.add(label("Mostrar"), gbc);
        gbc.gridx = 5;
        filtros.add(spnLimite, gbc);

        JPanel accionesFiltro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accionesFiltro.setOpaque(false);
        accionesFiltro.add(btnBuscar);
        accionesFiltro.add(btnLimpiar);
        accionesFiltro.add(btnRefrescar);
        accionesFiltro.add(btnVerDetalle);
        gbc.gridx = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtros.add(accionesFiltro, gbc);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnRegistrarFirma);
        acciones.add(btnRegistrarEmision);
        acciones.add(btnRegistrarNumero);
        acciones.add(btnEnviarEjecucion);

        JPanel superior = new JPanel(new BorderLayout(8, 8));
        superior.setOpaque(false);
        superior.add(filtros, BorderLayout.NORTH);
        superior.add(acciones, BorderLayout.CENTER);
        superior.add(lblEstado, BorderLayout.SOUTH);
        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);

        panel.add(superior, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelFirmaEmision() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(420, 0));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.sectionBorder());

        JLabel title = new JLabel("Panel de firma y emisión");
        title.setFont(AppV2Theme.fontBold(18));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.add(crearResumenSeleccion());
        content.add(crearRevisionPrevia());
        content.add(crearDocumentosPanel());
        content.add(crearFormularioFirmaEmision());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearResumenSeleccion() {
        JPanel panel = section("Expediente seleccionado");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Expediente", lblExpediente);
        addRow(grid, row++, "Titular", lblTitular);
        addRow(grid, row++, "Acta", lblActa);
        addRow(grid, row++, "Procedimiento", lblProcedimiento);
        addRow(grid, row++, "Etapa / Estado", lblEtapaEstado);
        addRow(grid, row++, "Resolución", lblResolucion);
        addRow(grid, row, "Alertas", lblAlertas);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearRevisionPrevia() {
        JPanel panel = section("Revisión recibida");
        txtFundamentoAnalisis.setEditable(false);
        txtFundamentoAnalisis.setBackground(AppV2Theme.SURFACE_ALT);
        txtObservacion.setEditable(false);
        txtObservacion.setBackground(AppV2Theme.SURFACE_ALT);
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Análisis", lblAnalisis);
        addRow(grid, row++, "Verificación", lblVerificacion);
        addRow(grid, row++, "Sustento", scrollText(txtFundamentoAnalisis, 84));
        addRow(grid, row, "Observación", scrollText(txtObservacion, 74));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDocumentosPanel() {
        JPanel panel = section("Documentos del expediente");
        JScrollPane scroll = new JScrollPane(documentosTable);
        scroll.setPreferredSize(new Dimension(350, 135));
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormularioFirmaEmision() {
        JPanel panel = section("Registro de firma / emisión");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Tipo documento", cmbTipoResolucion);
        addRow(grid, row++, "Número", txtNumeroResolucion);
        addRow(grid, row++, "Fecha firma", txtFechaFirma);
        addRow(grid, row++, "Fecha emisión", txtFechaEmision);
        addRow(grid, row++, "Fecha resolución", txtFechaResolucion);
        addRow(grid, row, "Comentario", scrollText(txtComentario, 92));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel section(String title) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(12, 0, 12, 0)));
        JLabel label = new JLabel(title);
        label.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        label.setForeground(AppV2Theme.TEXT_PRIMARY);
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private JScrollPane scrollText(JTextArea area, int height) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        area.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(245, height));
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        return scroll;
    }

    private void addRow(JPanel target, int row, String label, Component component) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.NORTHWEST;
        gbcLabel.insets = new Insets(5, 0, 5, 10);

        GridBagConstraints gbcValue = new GridBagConstraints();
        gbcValue.gridx = 1;
        gbcValue.gridy = row;
        gbcValue.weightx = 1;
        gbcValue.fill = GridBagConstraints.HORIZONTAL;
        gbcValue.insets = new Insets(5, 0, 5, 0);
        target.add(label(label), gbcLabel);
        target.add(component, gbcValue);
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        label.setForeground(AppV2Theme.TEXT_SECONDARY);
        return label;
    }

    private void configurarControles() {
        txtBusqueda.setPreferredSize(new Dimension(340, 34));
        cmbEstadoFiltro.setPreferredSize(new Dimension(220, 34));
        cmbTipoResolucion.setPreferredSize(new Dimension(245, 34));
        txtNumeroResolucion.setPreferredSize(new Dimension(245, 34));
        txtFechaFirma.setPreferredSize(new Dimension(245, 34));
        txtFechaEmision.setPreferredSize(new Dimension(245, 34));
        txtFechaResolucion.setPreferredSize(new Dimension(245, 34));
        spnLimite.setPreferredSize(new Dimension(86, 34));
        btnBuscar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarFirma.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarEmision.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarNumero.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnEnviarEjecucion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
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
        table.setDefaultRenderer(Object.class, new FirmaEmisionRenderer());
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(10).setPreferredWidth(145);
        table.getColumnModel().getColumn(11).setPreferredWidth(160);
        table.getColumnModel().getColumn(12).setMaxWidth(92);
        table.getColumnModel().getColumn(14).setMaxWidth(92);
        table.getColumnModel().getColumn(15).setMaxWidth(92);
        AppV2TableColumnSizer.applyFriendlyDefaults(table);
    }

    private void configurarDocumentosTabla() {
        documentosTable.setRowHeight(30);
        documentosTable.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        documentosTable.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        documentosTable.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        documentosTable.setGridColor(AppV2Theme.BORDER);
        documentosTable.setShowVerticalLines(false);
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnRefrescar.addActionListener(e -> buscar());
        btnVerDetalle.addActionListener(e -> abrirDetalle());
        btnRegistrarFirma.addActionListener(e -> registrarFirma());
        btnRegistrarEmision.addActionListener(e -> registrarEmision());
        btnRegistrarNumero.addActionListener(e -> registrarNumeroResolucion());
        btnEnviarEjecucion.addActionListener(e -> enviarEjecucion());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarSeleccion();
            }
        });
    }

    private void cargarFiltrosBase() {
        cmbEstadoFiltro.removeAllItems();
        cmbEstadoFiltro.addItem(new SimpleItem("TODOS", "Todos"));
        cmbEstadoFiltro.addItem(new SimpleItem("PARA_FIRMA", "Para firma"));
        cmbEstadoFiltro.addItem(new SimpleItem("FIRMADO", "Firmado"));
        cmbEstadoFiltro.addItem(new SimpleItem("EMITIDO", "Emitido"));
        cmbEstadoFiltro.addItem(new SimpleItem("RESOLUCION_NUMERADA", "Resolución numerada"));
    }

    private void cargarTiposResolucion() {
        setTrabajando(true, "Cargando tipos de documento resolutivo...");
        SwingWorker<List<CatalogoItemDTO>, Void> worker = new SwingWorker<List<CatalogoItemDTO>, Void>() {
            @Override
            protected List<CatalogoItemDTO> doInBackground() throws Exception {
                return firmaEmisionService.listarTiposResolucion();
            }

            @Override
            protected void done() {
                try {
                    cargarTiposResolucionVista(get());
                } catch (Exception ex) {
                    cmbTipoResolucion.removeAllItems();
                    cmbTipoResolucion.addItem(new SimpleItem("", "Tipo no catalogado"));
                    mostrarError("No se pudieron cargar los tipos de resolución.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void cargarTiposResolucionVista(List<CatalogoItemDTO> items) {
        cmbTipoResolucion.removeAllItems();
        cmbTipoResolucion.addItem(new SimpleItem("", "Sin tipo seleccionado"));
        for (CatalogoItemDTO item : items) {
            cmbTipoResolucion.addItem(new SimpleItem(item.getCodigo(), item.getNombre()));
        }
        if (items.isEmpty()) {
            lblEstado.setText("No hay tipos de resolución activos; el tipo queda opcional según el modelo actual.");
        }
    }

    private void inicializarFechas() {
        String hoy = DATE_FORMAT.format(LocalDate.now());
        txtFechaFirma.setText(hoy);
        txtFechaEmision.setText(hoy);
        txtFechaResolucion.setText(hoy);
    }

    private void buscar() {
        setTrabajando(true, "Consultando expedientes en Firma / Emisión...");
        String texto = txtBusqueda.getText();
        String estado = obtenerCodigo(cmbEstadoFiltro);
        int limite = ((Number) spnLimite.getValue()).intValue();
        SwingWorker<List<FirmaEmisionExpedienteDTO>, Void> worker = new SwingWorker<List<FirmaEmisionExpedienteDTO>, Void>() {
            @Override
            protected List<FirmaEmisionExpedienteDTO> doInBackground() throws Exception {
                return firmaEmisionService.buscarExpedientes(texto, estado, limite);
            }

            @Override
            protected void done() {
                try {
                    cargarTabla(get());
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar la bandeja de Firma / Emisión.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void cargarTabla(List<FirmaEmisionExpedienteDTO> items) {
        expedientes.clear();
        expedientes.addAll(items);
        tableModel.fireTableDataChanged();
        actualizarMetricas();
        if (!items.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
        } else {
            limpiarDetalle();
        }
        lblEstado.setText(items.size() + " expediente(s) en Firma / Emisión.");
    }

    private void actualizarMetricas() {
        int paraFirma = 0;
        int emision = 0;
        int numerados = 0;
        for (FirmaEmisionExpedienteDTO expediente : expedientes) {
            if ("PARA_FIRMA".equalsIgnoreCase(expediente.getEstadoCodigo())) {
                paraFirma++;
            } else if ("FIRMADO".equalsIgnoreCase(expediente.getEstadoCodigo())
                    || "EMITIDO".equalsIgnoreCase(expediente.getEstadoCodigo())) {
                emision++;
            } else if ("RESOLUCION_NUMERADA".equalsIgnoreCase(expediente.getEstadoCodigo())) {
                numerados++;
            }
        }
        cardParaFirma.setValue(String.valueOf(paraFirma));
        cardEnEmision.setValue(String.valueOf(emision));
        cardNumerados.setValue(String.valueOf(numerados));
    }

    private void limpiar() {
        txtBusqueda.setText("");
        cmbEstadoFiltro.setSelectedIndex(0);
        spnLimite.setValue(200);
        expedientes.clear();
        tableModel.fireTableDataChanged();
        actualizarMetricas();
        limpiarDetalle();
        lblEstado.setText("Filtros limpiados. Presione Buscar para consultar expedientes.");
    }

    private void actualizarSeleccion() {
        FirmaEmisionExpedienteDTO expediente = seleccionado();
        if (expediente == null) {
            limpiarDetalle();
            return;
        }
        lblExpediente.setText(valor(expediente.getNumeroExpediente()));
        lblTitular.setText(valor(expediente.getTitular()));
        lblActa.setText(valor(expediente.getTipoActa()) + " " + valor(expediente.getNumeroActa()));
        lblProcedimiento.setText(valor(expediente.getProcedimiento()));
        lblEtapaEstado.setText(DisplayNameMapperV2.etapa(expediente.getEtapaCodigo())
                + " / " + DisplayNameMapperV2.estado(expediente.getEstadoCodigo()));
        lblAnalisis.setText(valor(expediente.getResultadoAnalisis()));
        lblVerificacion.setText(valor(expediente.getResultadoVerificacion()));
        lblResolucion.setText(resolucionTexto(expediente));
        txtNumeroResolucion.setText(expediente.getNumeroResolucion());
        inicializarFechas();
        if (expediente.getFechaFirma() != null) {
            txtFechaFirma.setText(DATE_FORMAT.format(expediente.getFechaFirma().toLocalDate()));
        }
        if (expediente.getFechaResolucion() != null) {
            txtFechaResolucion.setText(DATE_FORMAT.format(expediente.getFechaResolucion()));
        }
        txtFundamentoAnalisis.setText(expediente.getFundamentoAnalisis());
        txtObservacion.setText(expediente.getUltimaObservacion());
        lblAlertas.setText(alertasTexto(expediente));
        cargarDocumentos(expediente.getIdExpediente());
        actualizarAcciones(expediente);
    }

    private void limpiarDetalle() {
        lblExpediente.setText("-");
        lblTitular.setText("-");
        lblActa.setText("-");
        lblProcedimiento.setText("-");
        lblEtapaEstado.setText("-");
        lblAnalisis.setText("-");
        lblVerificacion.setText("-");
        lblResolucion.setText("-");
        lblAlertas.setText("Sin expediente seleccionado.");
        txtNumeroResolucion.setText("");
        txtComentario.setText("");
        txtFundamentoAnalisis.setText("");
        txtObservacion.setText("");
        inicializarFechas();
        documentosModel.setRowCount(0);
        actualizarAcciones(null);
    }

    private void cargarDocumentos(Long idExpediente) {
        documentosModel.setRowCount(0);
        SwingWorker<List<DocumentoFirmaDTO>, Void> worker = new SwingWorker<List<DocumentoFirmaDTO>, Void>() {
            @Override
            protected List<DocumentoFirmaDTO> doInBackground() throws Exception {
                return documentoFirmaService.listarPorExpediente(idExpediente);
            }

            @Override
            protected void done() {
                try {
                    cargarDocumentosVista(get());
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los documentos del expediente.", ex);
                }
            }
        };
        worker.execute();
    }

    private void cargarDocumentosVista(List<DocumentoFirmaDTO> documentos) {
        documentosModel.setRowCount(0);
        for (DocumentoFirmaDTO documento : documentos) {
            documentosModel.addRow(new Object[]{
                valor(documento.getTipoDocumento()),
                valor(documento.getEstadoDocumento()),
                valor(documento.getNumeroDocumento()),
                valor(documento.getNombreDocumento()),
                format(documento.getFechaDocumento())
            });
        }
    }

    private void actualizarAcciones(FirmaEmisionExpedienteDTO expediente) {
        boolean seleccionado = expediente != null;
        btnVerDetalle.setEnabled(seleccionado);
        btnRegistrarFirma.setEnabled(seleccionado && expediente.isFirmable());
        btnRegistrarEmision.setEnabled(seleccionado && expediente.isEmitible());
        btnRegistrarNumero.setEnabled(seleccionado && expediente.isNumerable());
        btnEnviarEjecucion.setEnabled(seleccionado && expediente.isEnviableEjecucion());
    }

    private void registrarFirma() {
        FirmaEmisionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        if (!confirmar("Se registrará la firma del expediente " + expediente.getNumeroExpediente() + ". ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando firma...", new Callable<FirmaEmisionResultadoDTO>() {
            @Override
            public FirmaEmisionResultadoDTO call() throws Exception {
                return firmaEmisionService.registrarFirma(crearRegistro(FirmaEmisionExpedienteService.ACCION_FIRMA_DOCUMENTO));
            }
        });
    }

    private void registrarEmision() {
        FirmaEmisionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        if (!confirmar("Se registrará la emisión del documento del expediente " + expediente.getNumeroExpediente() + ". ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando emisión...", new Callable<FirmaEmisionResultadoDTO>() {
            @Override
            public FirmaEmisionResultadoDTO call() throws Exception {
                return firmaEmisionService.registrarEmision(crearRegistro(FirmaEmisionExpedienteService.ACCION_FIRMA_DOCUMENTO));
            }
        });
    }

    private void registrarNumeroResolucion() {
        FirmaEmisionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        if (!confirmar("Se registrará el número de resolución del expediente " + expediente.getNumeroExpediente() + ". ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando número de resolución...", new Callable<FirmaEmisionResultadoDTO>() {
            @Override
            public FirmaEmisionResultadoDTO call() throws Exception {
                return firmaEmisionService.registrarNumeroResolucion(crearRegistro(FirmaEmisionExpedienteService.ACCION_REGISTRO_NUMERO));
            }
        });
    }

    private void enviarEjecucion() {
        FirmaEmisionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        if (!confirmar("El expediente " + expediente.getNumeroExpediente() + " será enviado a Ejecución. ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Enviando expediente a Ejecución...", new Callable<FirmaEmisionResultadoDTO>() {
            @Override
            public FirmaEmisionResultadoDTO call() throws Exception {
                return firmaEmisionService.enviarEjecucion(crearRegistro(FirmaEmisionExpedienteService.ACCION_REGISTRO_NUMERO));
            }
        });
    }

    private void ejecutarOperacion(String mensajeTrabajo, Callable<FirmaEmisionResultadoDTO> operacion) {
        setTrabajando(true, mensajeTrabajo);
        SwingWorker<FirmaEmisionResultadoDTO, Void> worker = new SwingWorker<FirmaEmisionResultadoDTO, Void>() {
            @Override
            protected FirmaEmisionResultadoDTO doInBackground() throws Exception {
                return operacion.call();
            }

            @Override
            protected void done() {
                try {
                    FirmaEmisionResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelFirmaEmisionV2.this,
                            resultado.getMensaje(),
                            "Firma / Emisión",
                            JOptionPane.INFORMATION_MESSAGE);
                    buscar();
                } catch (Exception ex) {
                    mostrarError("No se pudo completar la acción.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private FirmaEmisionRegistroDTO crearRegistro(String accionCodigo) {
        FirmaEmisionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        SimpleItem tipo = (SimpleItem) cmbTipoResolucion.getSelectedItem();
        return new FirmaEmisionRegistroDTO(
                expediente.getIdExpediente(),
                accionCodigo,
                tipo == null ? "" : tipo.getCodigo(),
                tipo == null ? "" : tipo.getNombre(),
                txtNumeroResolucion.getText(),
                parseFecha(txtFechaFirma, "fecha de firma"),
                parseFecha(txtFechaEmision, "fecha de emisión"),
                parseFecha(txtFechaResolucion, "fecha de resolución"),
                txtComentario.getText());
    }

    private LocalDate parseFecha(JTextField field, String label) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ingrese una " + label + " válida con formato yyyy-MM-dd.");
        }
    }

    private void abrirDetalle() {
        FirmaEmisionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(owner, expediente.getIdExpediente());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private FirmaEmisionExpedienteDTO seleccionado() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= expedientes.size()) {
            return null;
        }
        return expedientes.get(modelRow);
    }

    private FirmaEmisionExpedienteDTO requerirSeleccion() {
        FirmaEmisionExpedienteDTO expediente = seleccionado();
        if (expediente == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un expediente.", "Firma / Emisión", JOptionPane.INFORMATION_MESSAGE);
        }
        return expediente;
    }

    private boolean confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(
                this,
                mensaje,
                "Confirmar acción",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void setTrabajando(boolean trabajando, String mensaje) {
        btnBuscar.setEnabled(!trabajando);
        btnLimpiar.setEnabled(!trabajando);
        btnRefrescar.setEnabled(!trabajando);
        table.setEnabled(!trabajando);
        if (mensaje != null) {
            lblEstado.setText(mensaje);
        }
        if (!trabajando) {
            actualizarAcciones(seleccionado());
        } else {
            btnVerDetalle.setEnabled(false);
            btnRegistrarFirma.setEnabled(false);
            btnRegistrarEmision.setEnabled(false);
            btnRegistrarNumero.setEnabled(false);
            btnEnviarEjecucion.setEnabled(false);
        }
    }

    private String obtenerCodigo(JComboBox<SimpleItem> combo) {
        SimpleItem item = (SimpleItem) combo.getSelectedItem();
        return item == null ? "" : item.getCodigo();
    }

    private String alertasTexto(FirmaEmisionExpedienteDTO expediente) {
        List<String> alertas = new ArrayList<String>();
        if (expediente.getTotalRelacionados() > 0) {
            alertas.add(expediente.getTotalRelacionados() + " expediente(s) asociado(s)");
        }
        if (expediente.getTotalDocumentos() == 0) {
            alertas.add("Sin documentos registrados");
        }
        if (!hasText(expediente.getResultadoVerificacion())) {
            alertas.add("Sin verificación aprobada visible");
        }
        return alertas.isEmpty() ? "Sin alertas." : String.join(" · ", alertas);
    }

    private String resolucionTexto(FirmaEmisionExpedienteDTO expediente) {
        if (hasText(expediente.getNumeroResolucion())) {
            return expediente.getNumeroResolucion();
        }
        if (expediente.getIdResolucion() != null) {
            return "Documento en preparación";
        }
        return "Sin resolución registrada";
    }

    private void mostrarError(String contexto, Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String detalle = cause.getMessage() == null ? "Error no especificado." : cause.getMessage();
        JOptionPane.showMessageDialog(this, contexto + "\n" + detalle, "Firma / Emisión", JOptionPane.WARNING_MESSAGE);
        lblEstado.setText(contexto);
    }

    private String format(LocalDate value) {
        return value == null ? "-" : DATE_FORMAT.format(value);
    }

    private String format(LocalDateTime value) {
        return value == null ? "-" : DATE_TIME_FORMAT.format(value);
    }

    private String valor(String value) {
        return hasText(value) ? value : "-";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private class FirmaEmisionTableModel extends AbstractTableModel {

        private final String[] columns = {
            "ID", "Expediente", "Trámite", "Procedimiento", "Tipo doc.", "Tipo acta", "Nro. acta",
            "Titular", "Recepción", "Envío firma", "Etapa", "Estado", "Días", "Nro. resolución",
            "Docs", "Asociados"
        };

        @Override
        public int getRowCount() {
            return expedientes.size();
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
            FirmaEmisionExpedienteDTO item = expedientes.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return item.getIdExpediente();
                case 1:
                    return item.getNumeroExpediente();
                case 2:
                    return item.getNumeroTramiteDocumentario();
                case 3:
                    return item.getProcedimiento();
                case 4:
                    return item.getTipoDocumento();
                case 5:
                    return item.getTipoActa();
                case 6:
                    return item.getNumeroActa();
                case 7:
                    return item.getTitular();
                case 8:
                    return format(item.getFechaRecepcion());
                case 9:
                    return format(item.getFechaEnvioFirma());
                case 10:
                    return item.getEtapaCodigo();
                case 11:
                    return item.getEstadoCodigo();
                case 12:
                    return item.getDiasEnEtapa();
                case 13:
                    return item.getNumeroResolucion();
                case 14:
                    return item.getTotalDocumentos();
                case 15:
                    return item.getTotalRelacionados();
                default:
                    return "";
            }
        }
    }

    private class FirmaEmisionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            if (column == 10) {
                return StatusBadgeV2.forEtapa(value == null ? "" : value.toString());
            }
            if (column == 11) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
            }
            if (column == 12) {
                return StatusBadgeV2.forDias(value);
            }
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (!isSelected) {
                label.setForeground(AppV2Theme.TEXT_PRIMARY);
                label.setBackground(row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
            }
            if (value == null || value.toString().trim().isEmpty()) {
                label.setText("-");
                label.setForeground(AppV2Theme.MUTED);
            }
            return label;
        }
    }

    private static class SimpleItem {

        private final String codigo;
        private final String nombre;

        private SimpleItem(String codigo, String nombre) {
            this.codigo = codigo == null ? "" : codigo;
            this.nombre = nombre == null || nombre.trim().isEmpty() ? this.codigo : nombre;
        }

        private String getCodigo() {
            return codigo;
        }

        private String getNombre() {
            return nombre;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }
}
