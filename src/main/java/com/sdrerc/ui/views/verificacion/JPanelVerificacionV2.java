package com.sdrerc.ui.views.verificacion;

import com.sdrerc.application.sdrercapp.DocumentoVerificacionService;
import com.sdrerc.application.sdrercapp.VerificacionExpedienteService;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoVerificacionDTO;
import com.sdrerc.domain.dto.sdrercapp.ObservacionVerificacionDTO;
import com.sdrerc.domain.dto.sdrercapp.VerificacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.VerificacionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.VerificacionResultadoDTO;
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.BadgeV2;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class JPanelVerificacionV2 extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final VerificacionExpedienteService verificacionService;
    private final DocumentoVerificacionService documentoService;

    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite, titular, acta o resultado", 28);
    private final JComboBox<SimpleItem> cmbEstadoFiltro = new JComboBox<SimpleItem>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnVerDetalle = new JButton("Ver detalle");
    private final JButton btnRegistrarVerificacion = new JButton("Registrar verificación");
    private final JButton btnAprobar = new JButton("Aprobar verificación");
    private final JButton btnEnviarFirma = new JButton("Enviar a firma");
    private final JButton btnObservar = new JButton("Requiere corrección");
    private final JButton btnDocumentoInconsistente = new JButton("Documento inconsistente");
    private final JButton btnDevolverAnalisis = new JButton("Devolver a análisis");

    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar expedientes en verificación.");
    private final JLabel lblExpediente = new JLabel("-");
    private final JLabel lblTitular = new JLabel("-");
    private final JLabel lblActa = new JLabel("-");
    private final JLabel lblProcedimiento = new JLabel("-");
    private final JLabel lblResponsable = new JLabel("-");
    private final JLabel lblResponsableAnalisis = new JLabel("-");
    private final JLabel lblEtapaEstado = new JLabel("-");
    private final JLabel lblAnalisis = new JLabel("-");
    private final JLabel lblAlertas = new JLabel("Sin alertas.");
    private final JLabel lblFechaVerificacion = new JLabel(DATE_FORMAT.format(LocalDate.now()));

    private final JComboBox<ResultadoItem> cmbResultado = new JComboBox<ResultadoItem>();
    private final JComboBox<SimpleItem> cmbTipoObservacion = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbMotivoCorreccion = new JComboBox<SimpleItem>();
    private final JTextArea txtComentario = new JTextArea(4, 22);
    private final JTextArea txtObservacion = new JTextArea(4, 22);
    private final JTextArea txtFundamentoAnalisis = new JTextArea(4, 22);

    private final VerificacionTableModel tableModel = new VerificacionTableModel();
    private final JTable table = new AppV2Table(tableModel);
    private final DefaultTableModel documentosModel = new DefaultTableModel(
            new Object[]{"Tipo", "Estado", "Fecha", "Descripción"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable documentosTable = new JTable(documentosModel);
    private final List<VerificacionExpedienteDTO> expedientes = new ArrayList<VerificacionExpedienteDTO>();

    private final MetricCardV2 cardEnVerificacion = new MetricCardV2("En verificación", "0", "Pendientes de revisión", AppV2Theme.INFO);
    private final MetricCardV2 cardCorreccion = new MetricCardV2("Con corrección", "0", "Observados o inconsistentes", AppV2Theme.WARNING);
    private final MetricCardV2 cardVerificados = new MetricCardV2("Verificados", "0", "Listos para firma", AppV2Theme.SUCCESS);

    private boolean cargandoCatalogos;

    public JPanelVerificacionV2() {
        this(new VerificacionExpedienteService(), new DocumentoVerificacionService());
    }

    public JPanelVerificacionV2(
            VerificacionExpedienteService verificacionService,
            DocumentoVerificacionService documentoService) {
        this.verificacionService = verificacionService;
        this.documentoService = documentoService;
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());
        add(crearHeader(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        configurarTabla();
        configurarDocumentosTabla();
        configurarEventos();
        cargarFiltrosBase();
        cargarCatalogos();
        actualizarSeleccion();
    }

    private JPanel crearHeader() {
        JPanel metricas = new JPanel(new GridLayout(1, 3, 12, 0));
        metricas.setOpaque(false);
        metricas.add(cardEnVerificacion);
        metricas.add(cardCorreccion);
        metricas.add(cardVerificados);
        return metricas;
    }

    private JPanel crearCentro() {
        JPanel centro = new JPanel(new BorderLayout(14, 14));
        centro.setOpaque(false);
        centro.add(crearBandeja(), BorderLayout.CENTER);
        centro.add(crearPanelVerificacion(), BorderLayout.EAST);
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
        acciones.add(btnRegistrarVerificacion);
        acciones.add(btnAprobar);
        acciones.add(btnEnviarFirma);
        acciones.add(btnObservar);
        acciones.add(btnDocumentoInconsistente);
        acciones.add(btnDevolverAnalisis);

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

    private JPanel crearPanelVerificacion() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(405, 0));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.sectionBorder());

        JLabel title = new JLabel("Panel de verificación");
        title.setFont(AppV2Theme.fontBold(18));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.add(crearResumenSeleccion());
        content.add(crearAnalisisPrevio());
        content.add(crearDocumentosPanel());
        content.add(crearFormularioVerificacion());

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
        addRow(grid, row++, "Responsable", lblResponsable);
        addRow(grid, row++, "Etapa / Estado", lblEtapaEstado);
        addRow(grid, row, "Alertas", lblAlertas);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearAnalisisPrevio() {
        JPanel panel = section("Análisis recibido");
        txtFundamentoAnalisis.setEditable(false);
        txtFundamentoAnalisis.setBackground(AppV2Theme.SURFACE_ALT);
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Resultado", lblAnalisis);
        addRow(grid, row++, "Responsable análisis", lblResponsableAnalisis);
        addRow(grid, row, "Sustento", scrollText(txtFundamentoAnalisis, 96));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDocumentosPanel() {
        JPanel panel = section("Documentos revisados en análisis");
        JScrollPane scroll = new JScrollPane(documentosTable);
        scroll.setPreferredSize(new Dimension(340, 145));
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormularioVerificacion() {
        JPanel panel = section("Registro de verificación");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Resultado", cmbResultado);
        addRow(grid, row++, "Tipo observación", cmbTipoObservacion);
        addRow(grid, row++, "Motivo corrección", cmbMotivoCorreccion);
        addRow(grid, row++, "Fecha", lblFechaVerificacion);
        addRow(grid, row++, "Sustento", scrollText(txtComentario, 86));
        addRow(grid, row, "Observación", scrollText(txtObservacion, 86));
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
        scroll.setPreferredSize(new Dimension(235, height));
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
        cmbResultado.setPreferredSize(new Dimension(235, 34));
        cmbTipoObservacion.setPreferredSize(new Dimension(235, 34));
        cmbMotivoCorreccion.setPreferredSize(new Dimension(235, 34));
        spnLimite.setPreferredSize(new Dimension(86, 34));
        btnBuscar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarVerificacion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnAprobar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnEnviarFirma.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnObservar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnDocumentoInconsistente.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnDevolverAnalisis.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
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
        table.setDefaultRenderer(Object.class, new VerificacionRenderer());
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(10).setPreferredWidth(150);
        table.getColumnModel().getColumn(11).setPreferredWidth(160);
        table.getColumnModel().getColumn(12).setMaxWidth(92);
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
        btnRegistrarVerificacion.addActionListener(e -> registrarVerificacion());
        btnAprobar.addActionListener(e -> accionRapida("APROBACION_VERIFICACION"));
        btnEnviarFirma.addActionListener(e -> enviarFirma());
        btnObservar.addActionListener(e -> accionRapida("REGISTRO_OBSERVACION_VERIFICACION"));
        btnDocumentoInconsistente.addActionListener(e -> accionRapida("REVERSION_ESTADO_DOCUMENTO"));
        btnDevolverAnalisis.addActionListener(e -> devolverAnalisis());
        cmbResultado.addActionListener(e -> actualizarResultadoSeleccionado());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarSeleccion();
            }
        });
    }

    private void cargarFiltrosBase() {
        cmbEstadoFiltro.removeAllItems();
        cmbEstadoFiltro.addItem(new SimpleItem("TODOS", "Todos"));
        cmbEstadoFiltro.addItem(new SimpleItem("EN_VERIFICACION", "En verificación"));
        cmbEstadoFiltro.addItem(new SimpleItem("REQUIERE_CORRECCION", "Requiere corrección"));
        cmbEstadoFiltro.addItem(new SimpleItem("DOCUMENTO_INCONSISTENTE", "Documento inconsistente"));
        cmbEstadoFiltro.addItem(new SimpleItem("VERIFICADO", "Verificado"));
    }

    private void cargarCatalogos() {
        cargandoCatalogos = true;
        setTrabajando(true, "Cargando catálogos de verificación...");
        SwingWorker<CatalogosCarga, Void> worker = new SwingWorker<CatalogosCarga, Void>() {
            @Override
            protected CatalogosCarga doInBackground() throws Exception {
                return new CatalogosCarga(
                        verificacionService.listarResultadosVerificacion(),
                        verificacionService.listarTiposObservacion(),
                        verificacionService.listarMotivosCorreccion());
            }

            @Override
            protected void done() {
                try {
                    cargarCatalogosVista(get());
                } catch (Exception ex) {
                    mostrarError("No se pudieron cargar los catálogos de verificación.", ex);
                } finally {
                    cargandoCatalogos = false;
                    setTrabajando(false, null);
                    actualizarResultadoSeleccionado();
                }
            }
        };
        worker.execute();
    }

    private void cargarCatalogosVista(CatalogosCarga carga) {
        cmbResultado.removeAllItems();
        cmbResultado.addItem(ResultadoItem.placeholder("Seleccione resultado"));
        for (CatalogoItemDTO item : carga.resultados) {
            cmbResultado.addItem(new ResultadoItem(item));
        }
        cargarSimpleItems(cmbTipoObservacion, carga.tiposObservacion, "Seleccione tipo");
        cargarSimpleItems(cmbMotivoCorreccion, carga.motivosCorreccion, "Seleccione motivo");
    }

    private void cargarSimpleItems(JComboBox<SimpleItem> combo, List<CatalogoItemDTO> items, String placeholder) {
        combo.removeAllItems();
        combo.addItem(new SimpleItem("", placeholder));
        for (CatalogoItemDTO item : items) {
            combo.addItem(new SimpleItem(item.getCodigo(), item.getNombre()));
        }
    }

    private void buscar() {
        setTrabajando(true, "Consultando expedientes en verificación...");
        String texto = txtBusqueda.getText();
        String estado = obtenerCodigo(cmbEstadoFiltro);
        int limite = ((Number) spnLimite.getValue()).intValue();
        SwingWorker<List<VerificacionExpedienteDTO>, Void> worker = new SwingWorker<List<VerificacionExpedienteDTO>, Void>() {
            @Override
            protected List<VerificacionExpedienteDTO> doInBackground() throws Exception {
                return verificacionService.buscarExpedientes(texto, estado, limite);
            }

            @Override
            protected void done() {
                try {
                    cargarTabla(get());
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar la bandeja de verificación.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void cargarTabla(List<VerificacionExpedienteDTO> items) {
        expedientes.clear();
        expedientes.addAll(items);
        tableModel.setRowCount(0);
        int enVerificacion = 0;
        int correccion = 0;
        int verificados = 0;
        for (VerificacionExpedienteDTO item : expedientes) {
            if (item.isRegistrableVerificacion()) {
                enVerificacion++;
            }
            if (item.isDevolvibleAnalisis()) {
                correccion++;
            }
            if (item.isEnviableFirma()) {
                verificados++;
            }
            tableModel.addRow(new Object[]{
                item.getIdExpediente(),
                item.getNumeroExpediente(),
                item.getNumeroTramiteDocumentario(),
                item.getProcedimiento(),
                item.getTipoDocumento(),
                item.getTipoActa(),
                item.getNumeroActa(),
                item.getTitular(),
                formatDate(item.getFechaRecepcion()),
                formatDateTime(item.getFechaEnvioVerificacion()),
                item.getResponsableAnalisis().isEmpty() ? item.getResponsable() : item.getResponsableAnalisis(),
                DisplayNameMapperV2.etapa(item.getEtapaCodigo()),
                DisplayNameMapperV2.estado(item.getEstadoCodigo()),
                item.getDiasEnEtapa() == null ? "" : item.getDiasEnEtapa(),
                item.isTieneObservacionPendiente() ? "Con observación" : "Sin observación",
                item.getTotalRelacionados() > 0 ? item.getTotalRelacionados() + " asociados" : "Sin asociados"
            });
        }
        cardEnVerificacion.setValue(String.valueOf(enVerificacion));
        cardCorreccion.setValue(String.valueOf(correccion));
        cardVerificados.setValue(String.valueOf(verificados));
        lblEstado.setText(items.isEmpty()
                ? "No se encontraron expedientes en verificación."
                : items.size() + " expediente(s) encontrados.");
        actualizarSeleccion();
    }

    private void limpiar() {
        txtBusqueda.setText("");
        cmbEstadoFiltro.setSelectedIndex(0);
        spnLimite.setValue(200);
        expedientes.clear();
        tableModel.setRowCount(0);
        documentosModel.setRowCount(0);
        limpiarFormulario();
        cardEnVerificacion.setValue("0");
        cardCorreccion.setValue("0");
        cardVerificados.setValue("0");
        lblEstado.setText("Filtros limpiados. Presione Buscar para consultar expedientes en verificación.");
        actualizarSeleccion();
    }

    private void actualizarSeleccion() {
        VerificacionExpedienteDTO item = obtenerSeleccionado();
        boolean has = item != null;
        btnVerDetalle.setEnabled(has);
        btnRegistrarVerificacion.setEnabled(has && item.isRegistrableVerificacion());
        btnAprobar.setEnabled(has && item.isRegistrableVerificacion());
        btnObservar.setEnabled(has && item.isRegistrableVerificacion());
        btnDocumentoInconsistente.setEnabled(has && item.isRegistrableVerificacion());
        btnEnviarFirma.setEnabled(has && item.isEnviableFirma());
        btnDevolverAnalisis.setEnabled(has && item.isDevolvibleAnalisis());
        if (!has) {
            lblExpediente.setText("-");
            lblTitular.setText("-");
            lblActa.setText("-");
            lblProcedimiento.setText("-");
            lblResponsable.setText("-");
            lblResponsableAnalisis.setText("-");
            lblEtapaEstado.setText("-");
            lblAnalisis.setText("-");
            lblAlertas.setText("Sin alertas.");
            txtFundamentoAnalisis.setText("");
            documentosModel.setRowCount(0);
            return;
        }
        lblExpediente.setText(item.getNumeroExpediente());
        lblTitular.setText(item.getTitular());
        lblActa.setText((item.getTipoActa() + " " + item.getNumeroActa()).trim());
        lblProcedimiento.setText(item.getProcedimiento());
        lblResponsable.setText(item.getResponsable().isEmpty() ? "-" : item.getResponsable());
        lblResponsableAnalisis.setText(item.getResponsableAnalisis().isEmpty() ? "-" : item.getResponsableAnalisis());
        lblEtapaEstado.setText(DisplayNameMapperV2.etapa(item.getEtapaCodigo()) + " / " + DisplayNameMapperV2.estado(item.getEstadoCodigo()));
        lblAnalisis.setText(item.getUltimoResultadoAnalisis().isEmpty() ? "Sin resultado registrado" : item.getUltimoResultadoAnalisis());
        txtFundamentoAnalisis.setText(item.getFundamentoAnalisis());
        txtFundamentoAnalisis.setCaretPosition(0);
        lblAlertas.setText(alertas(item));
        txtComentario.setText("");
        txtObservacion.setText(item.getUltimaObservacionVerificacion());
        cargarDocumentos(item);
    }

    private String alertas(VerificacionExpedienteDTO item) {
        List<String> alertas = new ArrayList<String>();
        if (item.isTieneObservacionPendiente()) {
            alertas.add("Observación pendiente");
        }
        if (item.getTotalRelacionados() > 0) {
            alertas.add(item.getTotalRelacionados() + " expediente(s) asociado(s)");
        }
        if (item.getTotalDocumentosAnalizados() > 0) {
            alertas.add(item.getTotalDocumentosAnalizados() + " documento(s) analizado(s)");
        }
        return alertas.isEmpty() ? "Sin alertas." : String.join(" · ", alertas);
    }

    private void cargarDocumentos(VerificacionExpedienteDTO item) {
        documentosModel.setRowCount(0);
        SwingWorker<List<DocumentoVerificacionDTO>, Void> worker = new SwingWorker<List<DocumentoVerificacionDTO>, Void>() {
            @Override
            protected List<DocumentoVerificacionDTO> doInBackground() throws Exception {
                return documentoService.listarDocumentosAnalizados(item.getIdExpediente());
            }

            @Override
            protected void done() {
                try {
                    for (DocumentoVerificacionDTO documento : get()) {
                        documentosModel.addRow(new Object[]{
                            documento.getTipoDocumento(),
                            documento.getEstadoDocumento(),
                            formatDate(documento.getFechaDocumento()),
                            documento.getDescripcion()
                        });
                    }
                } catch (Exception ex) {
                    lblEstado.setText("No se pudieron cargar los documentos analizados.");
                }
            }
        };
        worker.execute();
    }

    private void registrarVerificacion() {
        VerificacionExpedienteDTO item = requerirSeleccion("Seleccione un expediente para registrar verificación.");
        if (item == null) {
            return;
        }
        VerificacionRegistroDTO registro = construirRegistro(item, null);
        confirmarYEjecutar(
                "Registrar verificación",
                "Se registrará la verificación del expediente " + item.getNumeroExpediente() + ". ¿Desea continuar?",
                () -> verificacionService.registrarVerificacion(registro));
    }

    private void accionRapida(String accionCodigo) {
        VerificacionExpedienteDTO item = requerirSeleccion("Seleccione un expediente para ejecutar la acción.");
        if (item == null) {
            return;
        }
        seleccionarResultado(accionCodigo);
        String titulo = "APROBACION_VERIFICACION".equals(accionCodigo)
                ? "Aprobar verificación"
                : "REVERSION_ESTADO_DOCUMENTO".equals(accionCodigo)
                ? "Documento inconsistente"
                : "Requiere corrección";
        VerificacionRegistroDTO registro = construirRegistro(item, accionCodigo);
        confirmarYEjecutar(
                titulo,
                "Se aplicará la acción al expediente " + item.getNumeroExpediente() + ". ¿Desea continuar?",
                () -> verificacionService.registrarVerificacion(registro));
    }

    private void enviarFirma() {
        VerificacionExpedienteDTO item = requerirSeleccion("Seleccione un expediente verificado para enviar a firma.");
        if (item == null) {
            return;
        }
        confirmarYEjecutar(
                "Enviar a firma",
                "Se enviará el expediente " + item.getNumeroExpediente() + " a Firma / Emisión. ¿Desea continuar?",
                () -> verificacionService.enviarFirma(item.getIdExpediente(), txtComentario.getText()));
    }

    private void devolverAnalisis() {
        VerificacionExpedienteDTO item = requerirSeleccion("Seleccione un expediente observado o inconsistente para devolver a análisis.");
        if (item == null) {
            return;
        }
        VerificacionRegistroDTO registro = construirRegistro(item, "DEVOLUCION_A_ANALISIS");
        confirmarYEjecutar(
                "Devolver a análisis",
                "Se devolverá el expediente " + item.getNumeroExpediente() + " a Análisis para corrección. ¿Desea continuar?",
                () -> verificacionService.devolverAnalisis(registro));
    }

    private VerificacionRegistroDTO construirRegistro(VerificacionExpedienteDTO item, String accionOverride) {
        ResultadoItem resultado = (ResultadoItem) cmbResultado.getSelectedItem();
        SimpleItem tipoObservacion = (SimpleItem) cmbTipoObservacion.getSelectedItem();
        SimpleItem motivoCorreccion = (SimpleItem) cmbMotivoCorreccion.getSelectedItem();
        String accion = accionOverride == null ? (resultado == null ? "" : resultado.codigo) : accionOverride;
        String nombre = resultado == null ? "" : resultado.nombre;
        ObservacionVerificacionDTO observacion = new ObservacionVerificacionDTO(
                tipoObservacion == null ? "" : tipoObservacion.codigo,
                tipoObservacion == null ? "" : tipoObservacion.nombre,
                motivoCorreccion == null ? "" : motivoCorreccion.codigo,
                motivoCorreccion == null ? "" : motivoCorreccion.nombre,
                txtObservacion.getText());
        return new VerificacionRegistroDTO(item.getIdExpediente(), accion, nombre, txtComentario.getText(), observacion);
    }

    private void confirmarYEjecutar(String titulo, String mensaje, OperacionVerificacion operacion) {
        int confirm = JOptionPane.showConfirmDialog(this, mensaje, titulo, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        setTrabajando(true, "Ejecutando operación de verificación...");
        SwingWorker<VerificacionResultadoDTO, Void> worker = new SwingWorker<VerificacionResultadoDTO, Void>() {
            @Override
            protected VerificacionResultadoDTO doInBackground() throws Exception {
                return operacion.ejecutar();
            }

            @Override
            protected void done() {
                try {
                    VerificacionResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelVerificacionV2.this,
                            resultado.getMensaje(),
                            "Verificación",
                            JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormulario();
                    buscar();
                } catch (Exception ex) {
                    mostrarError("No se pudo completar la operación de verificación.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void abrirDetalle() {
        VerificacionExpedienteDTO item = requerirSeleccion("Seleccione un expediente para ver el detalle.");
        if (item == null) {
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(owner, item.getIdExpediente());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private VerificacionExpedienteDTO requerirSeleccion(String mensaje) {
        VerificacionExpedienteDTO item = obtenerSeleccionado();
        if (item == null) {
            mostrarInfo(mensaje);
        }
        return item;
    }

    private VerificacionExpedienteDTO obtenerSeleccionado() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(selected);
        if (modelRow < 0 || modelRow >= expedientes.size()) {
            return null;
        }
        return expedientes.get(modelRow);
    }

    private void actualizarResultadoSeleccionado() {
        if (cargandoCatalogos) {
            return;
        }
        ResultadoItem resultado = (ResultadoItem) cmbResultado.getSelectedItem();
        boolean requiereCorreccion = resultado != null
                && ("REGISTRO_OBSERVACION_VERIFICACION".equalsIgnoreCase(resultado.codigo)
                || "REVERSION_ESTADO_DOCUMENTO".equalsIgnoreCase(resultado.codigo));
        cmbTipoObservacion.setEnabled(requiereCorreccion);
        cmbMotivoCorreccion.setEnabled(requiereCorreccion);
        txtObservacion.setEnabled(requiereCorreccion);
        if (!requiereCorreccion) {
            if (cmbTipoObservacion.getItemCount() > 0) {
                cmbTipoObservacion.setSelectedIndex(0);
            }
            if (cmbMotivoCorreccion.getItemCount() > 0) {
                cmbMotivoCorreccion.setSelectedIndex(0);
            }
        }
    }

    private void seleccionarResultado(String accionCodigo) {
        for (int i = 0; i < cmbResultado.getItemCount(); i++) {
            ResultadoItem item = cmbResultado.getItemAt(i);
            if (accionCodigo.equalsIgnoreCase(item.codigo)) {
                cmbResultado.setSelectedIndex(i);
                return;
            }
        }
    }

    private void limpiarFormulario() {
        if (cmbResultado.getItemCount() > 0) {
            cmbResultado.setSelectedIndex(0);
        }
        if (cmbTipoObservacion.getItemCount() > 0) {
            cmbTipoObservacion.setSelectedIndex(0);
        }
        if (cmbMotivoCorreccion.getItemCount() > 0) {
            cmbMotivoCorreccion.setSelectedIndex(0);
        }
        txtComentario.setText("");
        txtObservacion.setText("");
        actualizarResultadoSeleccionado();
    }

    private void setTrabajando(boolean trabajando, String mensaje) {
        btnBuscar.setEnabled(!trabajando);
        btnLimpiar.setEnabled(!trabajando);
        btnRefrescar.setEnabled(!trabajando);
        VerificacionExpedienteDTO item = obtenerSeleccionado();
        btnVerDetalle.setEnabled(!trabajando && item != null);
        btnRegistrarVerificacion.setEnabled(!trabajando && item != null && item.isRegistrableVerificacion());
        btnAprobar.setEnabled(!trabajando && item != null && item.isRegistrableVerificacion());
        btnObservar.setEnabled(!trabajando && item != null && item.isRegistrableVerificacion());
        btnDocumentoInconsistente.setEnabled(!trabajando && item != null && item.isRegistrableVerificacion());
        btnEnviarFirma.setEnabled(!trabajando && item != null && item.isEnviableFirma());
        btnDevolverAnalisis.setEnabled(!trabajando && item != null && item.isDevolvibleAnalisis());
        if (mensaje != null) {
            lblEstado.setText(mensaje);
        }
    }

    private String obtenerCodigo(JComboBox<SimpleItem> combo) {
        Object selected = combo.getSelectedItem();
        return selected instanceof SimpleItem ? ((SimpleItem) selected).codigo : "";
    }

    private static String formatDate(LocalDate value) {
        return value == null ? "" : DATE_FORMAT.format(value);
    }

    private static String formatDateTime(java.time.LocalDateTime value) {
        return value == null ? "" : DATE_TIME_FORMAT.format(value);
    }

    private void mostrarInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Verificación", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String context, Exception ex) {
        String message = ex.getMessage();
        if (message == null && ex.getCause() != null) {
            message = ex.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(
                this,
                context + (message == null ? "" : "\n" + message),
                "Error de verificación",
                JOptionPane.ERROR_MESSAGE);
        lblEstado.setText(context);
    }

    private interface OperacionVerificacion {
        VerificacionResultadoDTO ejecutar() throws Exception;
    }

    private class VerificacionTableModel extends DefaultTableModel {
        private VerificacionTableModel() {
            super(new Object[]{
                "ID",
                "Expediente",
                "Trámite",
                "Procedimiento",
                "Tipo doc.",
                "Tipo acta",
                "Nro. acta",
                "Titular",
                "Recepción",
                "Envío verificación",
                "Responsable análisis",
                "Etapa",
                "Estado",
                "Días",
                "Observación",
                "Asociados"
            }, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private static class VerificacionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            int modelColumn = table.convertColumnIndexToModel(column);
            if (!isSelected && modelColumn == 11) {
                return StatusBadgeV2.forEtapa(value == null ? "" : value.toString());
            }
            if (!isSelected && modelColumn == 12) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
            }
            if (!isSelected && modelColumn == 14 && value != null && value.toString().startsWith("Con")) {
                return new BadgeV2(value.toString(), AppV2Theme.SOFT_ORANGE, AppV2Theme.WARNING);
            }
            if (!isSelected && modelColumn == 15 && value != null && !value.toString().startsWith("Sin")) {
                return new BadgeV2(value.toString(), AppV2Theme.SOFT_BLUE, AppV2Theme.INFO);
            }
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
                c.setForeground(modelColumn == 1 ? AppV2Theme.PRIMARY : AppV2Theme.TEXT_PRIMARY);
            }
            return c;
        }
    }

    private static class SimpleItem {
        private final String codigo;
        private final String nombre;

        private SimpleItem(String codigo, String nombre) {
            this.codigo = codigo == null ? "" : codigo;
            this.nombre = nombre == null ? "" : nombre;
        }

        @Override
        public String toString() {
            return nombre.isEmpty() ? codigo : nombre;
        }
    }

    private static class ResultadoItem {
        private final String codigo;
        private final String nombre;

        private ResultadoItem(CatalogoItemDTO item) {
            this.codigo = item.getCodigo() == null ? "" : item.getCodigo();
            this.nombre = item.getNombre() == null ? this.codigo : item.getNombre();
        }

        private ResultadoItem(String codigo, String nombre) {
            this.codigo = codigo == null ? "" : codigo;
            this.nombre = nombre == null ? "" : nombre;
        }

        private static ResultadoItem placeholder(String nombre) {
            return new ResultadoItem("", nombre);
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    private static class CatalogosCarga {
        private final List<CatalogoItemDTO> resultados;
        private final List<CatalogoItemDTO> tiposObservacion;
        private final List<CatalogoItemDTO> motivosCorreccion;

        private CatalogosCarga(
                List<CatalogoItemDTO> resultados,
                List<CatalogoItemDTO> tiposObservacion,
                List<CatalogoItemDTO> motivosCorreccion) {
            this.resultados = resultados == null ? new ArrayList<CatalogoItemDTO>() : resultados;
            this.tiposObservacion = tiposObservacion == null ? new ArrayList<CatalogoItemDTO>() : tiposObservacion;
            this.motivosCorreccion = motivosCorreccion == null ? new ArrayList<CatalogoItemDTO>() : motivosCorreccion;
        }
    }
}
