package com.sdrerc.ui.views.cierrearchivo;

import com.sdrerc.application.sdrercapp.CierreArchivoService;
import com.sdrerc.application.sdrercapp.DocumentoEjecucionService;
import com.sdrerc.domain.dto.sdrercapp.ArchivoExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.CierreArchivoExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.CierreArchivoResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CierreExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoEjecucionDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteTimelineDTO;
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2Table;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class JPanelCierreArchivoV2 extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CierreArchivoService cierreArchivoService;
    private final DocumentoEjecucionService documentoService;

    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite, titular o motivo", 28);
    private final JComboBox<SimpleItem> cmbEstadoFiltro = new JComboBox<SimpleItem>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnVerDetalle = new JButton("Ver detalle");
    private final JButton btnRegistrarCierre = new JButton("Registrar cierre");
    private final JButton btnRegistrarArchivo = new JButton("Registrar archivo");
    private final JButton btnVerHistorial = new JButton("Ver historial");

    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar expedientes en Cierre / Archivo.");
    private final JLabel lblExpediente = new JLabel("-");
    private final JLabel lblTitular = new JLabel("-");
    private final JLabel lblActa = new JLabel("-");
    private final JLabel lblProcedimiento = new JLabel("-");
    private final JLabel lblEtapaEstado = new JLabel("-");
    private final JLabel lblResolucion = new JLabel("-");
    private final JLabel lblNotificacion = new JLabel("-");
    private final JLabel lblPublicacion = new JLabel("-");
    private final JLabel lblDigital = new JLabel("-");
    private final JLabel lblDerivacion = new JLabel("-");
    private final JLabel lblMotivoFinal = new JLabel("-");
    private final JLabel lblAcciones = new JLabel("-");
    private final JLabel lblAlertas = new JLabel("Sin alertas.");

    private final JTextField txtMotivo = new JTextField(22);
    private final JTextArea txtComentario = new JTextArea(4, 22);
    private final JTextArea txtObservacion = new JTextArea(3, 22);

    private final CierreArchivoTableModel tableModel = new CierreArchivoTableModel();
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
    private final List<CierreArchivoExpedienteDTO> expedientes = new ArrayList<CierreArchivoExpedienteDTO>();

    private final MetricCardV2 cardCandidatos = new MetricCardV2("Pendientes", "0", "Con acción de cierre o archivo", AppV2Theme.WARNING);
    private final MetricCardV2 cardCerrados = new MetricCardV2("Cerrados", "0", "Finalizados por cierre", AppV2Theme.SUCCESS);
    private final MetricCardV2 cardArchivados = new MetricCardV2("Archivados", "0", "Finalizados por archivo", AppV2Theme.INFO);

    public JPanelCierreArchivoV2() {
        this(new CierreArchivoService(), new DocumentoEjecucionService());
    }

    public JPanelCierreArchivoV2(
            CierreArchivoService cierreArchivoService,
            DocumentoEjecucionService documentoService) {
        this.cierreArchivoService = cierreArchivoService;
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
        actualizarSeleccion();
    }

    private JPanel crearHeader() {
        JPanel metricas = new JPanel(new GridLayout(1, 3, 12, 0));
        metricas.setOpaque(false);
        metricas.add(cardCandidatos);
        metricas.add(cardCerrados);
        metricas.add(cardArchivados);
        return metricas;
    }

    private JPanel crearCentro() {
        JPanel centro = new JPanel(new BorderLayout(14, 14));
        centro.setOpaque(false);
        centro.add(crearBandeja(), BorderLayout.CENTER);
        centro.add(crearPanelCierreArchivo(), BorderLayout.EAST);
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
        accionesFiltro.add(btnVerHistorial);
        gbc.gridx = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtros.add(accionesFiltro, gbc);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnRegistrarCierre);
        acciones.add(btnRegistrarArchivo);

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

    private JPanel crearPanelCierreArchivo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(440, 0));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.sectionBorder());

        JLabel title = new JLabel("Panel de cierre y archivo");
        title.setFont(AppV2Theme.fontBold(18));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.add(crearResumenSeleccion());
        content.add(crearAntecedentes());
        content.add(crearDocumentosPanel());
        content.add(crearFormularioCierreArchivo());

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
        addRow(grid, row++, "Notificación", lblNotificacion);
        addRow(grid, row++, "Publicación", lblPublicacion);
        addRow(grid, row++, "Expediente digital", lblDigital);
        addRow(grid, row++, "Derivación externa", lblDerivacion);
        addRow(grid, row++, "Motivo final", lblMotivoFinal);
        addRow(grid, row++, "Acciones", lblAcciones);
        addRow(grid, row, "Alertas", lblAlertas);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearAntecedentes() {
        JPanel panel = section("Observaciones");
        txtObservacion.setEditable(false);
        txtObservacion.setBackground(AppV2Theme.SURFACE_ALT);
        panel.add(scrollText(txtObservacion, 72), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDocumentosPanel() {
        JPanel panel = section("Documentos y antecedentes");
        JScrollPane scroll = new JScrollPane(documentosTable);
        scroll.setPreferredSize(new Dimension(360, 132));
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormularioCierreArchivo() {
        JPanel panel = section("Registro final");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Motivo", txtMotivo);
        addRow(grid, row, "Comentario", scrollText(txtComentario, 90));
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
        scroll.setPreferredSize(new Dimension(250, height));
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
        cmbEstadoFiltro.setPreferredSize(new Dimension(250, 34));
        spnLimite.setPreferredSize(new Dimension(86, 34));
        txtMotivo.setPreferredSize(new Dimension(250, 34));
        btnBuscar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarCierre.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarArchivo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
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
        table.setDefaultRenderer(Object.class, new CierreArchivoRenderer());
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(9).setPreferredWidth(145);
        table.getColumnModel().getColumn(10).setPreferredWidth(170);
        table.getColumnModel().getColumn(11).setMaxWidth(92);
        table.getColumnModel().getColumn(15).setMaxWidth(90);
        table.getColumnModel().getColumn(16).setMaxWidth(90);
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
        btnRegistrarCierre.addActionListener(e -> registrarCierre());
        btnRegistrarArchivo.addActionListener(e -> registrarArchivo());
        btnVerHistorial.addActionListener(e -> verHistorial());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarSeleccion();
            }
        });
    }

    private void cargarFiltrosBase() {
        cmbEstadoFiltro.removeAllItems();
        cmbEstadoFiltro.addItem(new SimpleItem("TODOS", "Todos"));
        cmbEstadoFiltro.addItem(new SimpleItem("CANDIDATOS_CIERRE", "Pendientes de cierre"));
        cmbEstadoFiltro.addItem(new SimpleItem("CANDIDATOS_ARCHIVO", "Pendientes de archivo"));
        cmbEstadoFiltro.addItem(new SimpleItem("CERRADO", "Cerrados"));
        cmbEstadoFiltro.addItem(new SimpleItem("ARCHIVADO", "Archivados"));
        cmbEstadoFiltro.addItem(new SimpleItem("DERIVACION_EXTERNA_PENDIENTE", "Derivación externa pendiente"));
    }

    private void buscar() {
        setTrabajando(true, "Consultando expedientes en Cierre / Archivo...");
        final String texto = txtBusqueda.getText();
        final String estado = obtenerCodigo(cmbEstadoFiltro);
        final int limite = ((Number) spnLimite.getValue()).intValue();
        SwingWorker<List<CierreArchivoExpedienteDTO>, Void> worker = new SwingWorker<List<CierreArchivoExpedienteDTO>, Void>() {
            @Override
            protected List<CierreArchivoExpedienteDTO> doInBackground() throws Exception {
                return cierreArchivoService.buscarExpedientes(texto, estado, limite);
            }

            @Override
            protected void done() {
                try {
                    expedientes.clear();
                    expedientes.addAll(get());
                    tableModel.fireTableDataChanged();
                    actualizarMetricas();
                    lblEstado.setText(expedientes.size() + " expediente(s) encontrados en Cierre / Archivo o pendientes de cierre/archivo.");
                    if (!expedientes.isEmpty()) {
                        table.setRowSelectionInterval(0, 0);
                    } else {
                        actualizarSeleccion();
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar Cierre / Archivo.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void limpiar() {
        txtBusqueda.setText("");
        cmbEstadoFiltro.setSelectedIndex(0);
        expedientes.clear();
        tableModel.fireTableDataChanged();
        actualizarMetricas();
        lblEstado.setText("Filtros limpiados. Presione Buscar para consultar.");
        actualizarSeleccion();
    }

    private void actualizarMetricas() {
        int candidatos = 0;
        int cerrados = 0;
        int archivados = 0;
        for (CierreArchivoExpedienteDTO expediente : expedientes) {
            if (expediente.hasAccion(CierreArchivoService.ACCION_CIERRE)
                    || expediente.hasAccion(CierreArchivoService.ACCION_ARCHIVO)) {
                candidatos++;
            }
            if ("CERRADO".equalsIgnoreCase(expediente.getEstadoCodigo())) {
                cerrados++;
            }
            if ("ARCHIVADO".equalsIgnoreCase(expediente.getEstadoCodigo())) {
                archivados++;
            }
        }
        cardCandidatos.setValue(String.valueOf(candidatos));
        cardCerrados.setValue(String.valueOf(cerrados));
        cardArchivados.setValue(String.valueOf(archivados));
    }

    private void actualizarSeleccion() {
        CierreArchivoExpedienteDTO expediente = seleccionado();
        if (expediente == null) {
            limpiarResumen();
            return;
        }
        lblExpediente.setText(valor(expediente.getNumeroExpediente()));
        lblTitular.setText(valor(expediente.getTitular()));
        lblActa.setText(valor(expediente.getTipoActa()) + " · " + valor(expediente.getNumeroActa()));
        lblProcedimiento.setText(valor(expediente.getProcedimiento()));
        lblEtapaEstado.setText(DisplayNameMapperV2.etapa(expediente.getEtapaCodigo()) + " / " + DisplayNameMapperV2.estado(expediente.getEstadoCodigo()));
        lblResolucion.setText(resolucionTexto(expediente));
        lblNotificacion.setText(notificacionTexto(expediente));
        lblPublicacion.setText(publicacionTexto(expediente));
        lblDigital.setText(expedienteDigitalTexto(expediente));
        lblDerivacion.setText(derivacionTexto(expediente));
        lblMotivoFinal.setText(valor(expediente.getMotivoFinal()));
        lblAcciones.setText(accionesTexto(expediente));
        lblAlertas.setText(alertasTexto(expediente));
        txtObservacion.setText(valor(expediente.getUltimaObservacion()));
        if (!hasText(txtMotivo.getText()) || expediente.isFinalizado()) {
            txtMotivo.setText(motivoSugerido(expediente));
        }
        cargarDocumentos(expediente.getIdExpediente());
        actualizarAcciones(expediente);
    }

    private void limpiarResumen() {
        lblExpediente.setText("-");
        lblTitular.setText("-");
        lblActa.setText("-");
        lblProcedimiento.setText("-");
        lblEtapaEstado.setText("-");
        lblResolucion.setText("-");
        lblNotificacion.setText("-");
        lblPublicacion.setText("-");
        lblDigital.setText("-");
        lblDerivacion.setText("-");
        lblMotivoFinal.setText("-");
        lblAcciones.setText("-");
        lblAlertas.setText("Sin expediente seleccionado.");
        txtObservacion.setText("");
        txtMotivo.setText("");
        txtComentario.setText("");
        documentosModel.setRowCount(0);
        actualizarAcciones(null);
    }

    private void cargarDocumentos(Long idExpediente) {
        documentosModel.setRowCount(0);
        if (idExpediente == null) {
            return;
        }
        SwingWorker<List<DocumentoEjecucionDTO>, Void> worker = new SwingWorker<List<DocumentoEjecucionDTO>, Void>() {
            @Override
            protected List<DocumentoEjecucionDTO> doInBackground() throws Exception {
                return documentoService.listarPorExpediente(idExpediente);
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

    private void cargarDocumentosVista(List<DocumentoEjecucionDTO> documentos) {
        documentosModel.setRowCount(0);
        for (DocumentoEjecucionDTO documento : documentos) {
            documentosModel.addRow(new Object[]{
                valor(documento.getTipoDocumento()),
                valor(documento.getEstadoDocumento()),
                valor(documento.getNumeroDocumento()),
                valor(documento.getNombreDocumento()),
                format(documento.getFechaDocumento())
            });
        }
    }

    private void actualizarAcciones(CierreArchivoExpedienteDTO expediente) {
        boolean seleccionado = expediente != null;
        btnVerDetalle.setEnabled(seleccionado);
        btnVerHistorial.setEnabled(seleccionado);
        btnRegistrarCierre.setEnabled(seleccionado
                && !expediente.isFinalizado()
                && expediente.hasAccion(CierreArchivoService.ACCION_CIERRE));
        btnRegistrarArchivo.setEnabled(seleccionado
                && !expediente.isFinalizado()
                && expediente.hasAccion(CierreArchivoService.ACCION_ARCHIVO));
    }

    private void registrarCierre() {
        CierreArchivoExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        if (!expediente.hasAccion(CierreArchivoService.ACCION_CIERRE)) {
            JOptionPane.showMessageDialog(this, "No hay una transición activa para cerrar el expediente.", "Cierre / Archivo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!confirmar("El expediente " + expediente.getNumeroExpediente() + " será cerrado. ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando cierre...", new Callable<CierreArchivoResultadoDTO>() {
            @Override
            public CierreArchivoResultadoDTO call() throws Exception {
                return cierreArchivoService.registrarCierre(crearCierre());
            }
        });
    }

    private void registrarArchivo() {
        CierreArchivoExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        if (!expediente.hasAccion(CierreArchivoService.ACCION_ARCHIVO)) {
            JOptionPane.showMessageDialog(this, "No hay una transición activa para archivar el expediente.", "Cierre / Archivo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!confirmar("El expediente " + expediente.getNumeroExpediente() + " será archivado. ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando archivo...", new Callable<CierreArchivoResultadoDTO>() {
            @Override
            public CierreArchivoResultadoDTO call() throws Exception {
                return cierreArchivoService.registrarArchivo(crearArchivo());
            }
        });
    }

    private void ejecutarOperacion(String mensajeTrabajo, Callable<CierreArchivoResultadoDTO> operacion) {
        setTrabajando(true, mensajeTrabajo);
        SwingWorker<CierreArchivoResultadoDTO, Void> worker = new SwingWorker<CierreArchivoResultadoDTO, Void>() {
            @Override
            protected CierreArchivoResultadoDTO doInBackground() throws Exception {
                return operacion.call();
            }

            @Override
            protected void done() {
                try {
                    CierreArchivoResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelCierreArchivoV2.this,
                            resultado.getMensaje(),
                            "Cierre / Archivo",
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

    private CierreExpedienteDTO crearCierre() {
        CierreArchivoExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        return new CierreExpedienteDTO(
                expediente.getIdExpediente(),
                CierreArchivoService.ACCION_CIERRE,
                txtMotivo.getText(),
                txtComentario.getText());
    }

    private ArchivoExpedienteDTO crearArchivo() {
        CierreArchivoExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        return new ArchivoExpedienteDTO(
                expediente.getIdExpediente(),
                CierreArchivoService.ACCION_ARCHIVO,
                txtMotivo.getText(),
                txtComentario.getText());
    }

    private void abrirDetalle() {
        CierreArchivoExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(owner, expediente.getIdExpediente());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void verHistorial() {
        CierreArchivoExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        setTrabajando(true, "Cargando historial del expediente...");
        SwingWorker<List<ExpedienteTimelineDTO>, Void> worker = new SwingWorker<List<ExpedienteTimelineDTO>, Void>() {
            @Override
            protected List<ExpedienteTimelineDTO> doInBackground() throws Exception {
                return cierreArchivoService.listarHistorial(expediente.getIdExpediente());
            }

            @Override
            protected void done() {
                try {
                    mostrarHistorial(expediente, get());
                } catch (Exception ex) {
                    mostrarError("No se pudo cargar el historial.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void mostrarHistorial(CierreArchivoExpedienteDTO expediente, List<ExpedienteTimelineDTO> historial) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Fecha", "Movimiento", "Origen", "Destino", "Motivo", "Comentario"},
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (ExpedienteTimelineDTO item : historial) {
            model.addRow(new Object[]{
                format(item.getFechaMovimiento()),
                valor(item.getMovimiento()),
                etapaEstado(item.getEtapaOrigen(), item.getEstadoOrigen()),
                etapaEstado(item.getEtapaDestino(), item.getEstadoDestino()),
                valor(item.getMotivo()),
                valor(item.getComentario())
            });
        }
        JTable historialTable = new JTable(model);
        historialTable.setRowHeight(30);
        historialTable.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        JScrollPane scroll = new JScrollPane(historialTable);
        scroll.setPreferredSize(new Dimension(940, 420));

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Historial del expediente " + valor(expediente.getNumeroExpediente()));
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private CierreArchivoExpedienteDTO seleccionado() {
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

    private CierreArchivoExpedienteDTO requerirSeleccion() {
        CierreArchivoExpedienteDTO expediente = seleccionado();
        if (expediente == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un expediente.", "Cierre / Archivo", JOptionPane.INFORMATION_MESSAGE);
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
            btnVerHistorial.setEnabled(false);
            btnRegistrarCierre.setEnabled(false);
            btnRegistrarArchivo.setEnabled(false);
        }
    }

    private String obtenerCodigo(JComboBox<SimpleItem> combo) {
        SimpleItem item = (SimpleItem) combo.getSelectedItem();
        return item == null ? "" : item.getCodigo();
    }

    private String resolucionTexto(CierreArchivoExpedienteDTO expediente) {
        if (hasText(expediente.getNumeroResolucion())) {
            return expediente.getNumeroResolucion() + " · " + format(expediente.getFechaResolucion());
        }
        if (expediente.getIdResolucion() != null) {
            return "Resolución sin número visible";
        }
        return "Sin resolución registrada";
    }

    private String notificacionTexto(CierreArchivoExpedienteDTO expediente) {
        if (expediente.getIdNotificacion() == null) {
            return "Sin notificación registrada";
        }
        return valor(expediente.getResultadoNotificacion());
    }

    private String publicacionTexto(CierreArchivoExpedienteDTO expediente) {
        if (expediente.getIdPublicacion() == null) {
            return "Sin publicación registrada";
        }
        return valor(expediente.getEstadoPublicacion());
    }

    private String expedienteDigitalTexto(CierreArchivoExpedienteDTO expediente) {
        if (expediente.getIdExpedienteDigital() == null && !expediente.isExpedienteDigitalCompleto()) {
            return "Sin metadata digital visible";
        }
        if (expediente.isExpedienteDigitalCompleto()) {
            return "Completo";
        }
        if (hasText(expediente.getEnlaceDigital())) {
            return "Enlace registrado";
        }
        if (hasText(expediente.getRutaDigital())) {
            return "Carpeta registrada";
        }
        return "Metadata digital registrada";
    }

    private String derivacionTexto(CierreArchivoExpedienteDTO expediente) {
        if (expediente.getIdDerivacionExterna() == null) {
            return "Sin derivación externa";
        }
        List<String> partes = new ArrayList<String>();
        if (hasText(expediente.getEntidadDestino())) {
            partes.add(expediente.getEntidadDestino());
        }
        if (hasText(expediente.getTipoDerivacion())) {
            partes.add(expediente.getTipoDerivacion());
        }
        if (hasText(expediente.getNumeroOficio())) {
            partes.add(expediente.getNumeroOficio());
        }
        if (hasText(expediente.getEstadoDerivacion())) {
            partes.add(expediente.getEstadoDerivacion());
        }
        return partes.isEmpty() ? "Derivación registrada" : String.join(" · ", partes);
    }

    private String alertasTexto(CierreArchivoExpedienteDTO expediente) {
        List<String> alertas = new ArrayList<String>();
        if (expediente.getTotalRelacionados() > 0) {
            alertas.add(expediente.getTotalRelacionados() + " expediente(s) asociado(s)");
        }
        if (expediente.getTotalDocumentos() == 0) {
            alertas.add("Sin documentos registrados");
        }
        if (expediente.isDerivacionExternaPendiente()) {
            alertas.add("Derivación externa pendiente");
        }
        if (!expediente.isFinalizado()
                && !expediente.hasAccion(CierreArchivoService.ACCION_CIERRE)
                && !expediente.hasAccion(CierreArchivoService.ACCION_ARCHIVO)) {
            alertas.add("Sin acción final activa");
        }
        return alertas.isEmpty() ? "Sin alertas." : String.join(" · ", alertas);
    }

    private String accionesTexto(CierreArchivoExpedienteDTO expediente) {
        return hasText(expediente.getAccionesPermitidas())
                ? expediente.getAccionesPermitidas().replace(",", ", ")
                : "Sin acciones activas";
    }

    private String motivoSugerido(CierreArchivoExpedienteDTO expediente) {
        if (expediente == null) {
            return "";
        }
        if (expediente.hasAccion(CierreArchivoService.ACCION_ARCHIVO)) {
            return "No corresponde / archivo administrativo";
        }
        if (expediente.hasAccion(CierreArchivoService.ACCION_CIERRE)) {
            return "Cierre de expediente";
        }
        return expediente.getMotivoFinal();
    }

    private String etapaEstado(String etapa, String estado) {
        return DisplayNameMapperV2.etapa(etapa) + " / " + DisplayNameMapperV2.estado(estado);
    }

    private void mostrarError(String contexto, Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String detalle = cause.getMessage() == null ? "Error no especificado." : cause.getMessage();
        JOptionPane.showMessageDialog(this, contexto + "\n" + detalle, "Cierre / Archivo", JOptionPane.WARNING_MESSAGE);
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

    private class CierreArchivoTableModel extends AbstractTableModel {

        private final String[] columns = {
            "ID", "Expediente", "Trámite", "Procedimiento", "Tipo doc.", "Tipo acta", "Nro. acta",
            "Titular", "Resolución", "Fecha cierre", "Fecha archivo", "Etapa", "Estado",
            "Motivo", "Digital", "Docs", "Asociados", "Días"
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
            CierreArchivoExpedienteDTO item = expedientes.get(rowIndex);
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
                    return item.getNumeroResolucion();
                case 9:
                    return format(item.getFechaCierre());
                case 10:
                    return format(item.getFechaArchivo());
                case 11:
                    return item.getEtapaCodigo();
                case 12:
                    return item.getEstadoCodigo();
                case 13:
                    return item.getMotivoFinal();
                case 14:
                    return item.isExpedienteDigitalCompleto() ? "Completo" : expedienteDigitalTexto(item);
                case 15:
                    return item.getTotalDocumentos();
                case 16:
                    return item.getTotalRelacionados();
                case 17:
                    return item.getDiasEnEtapa();
                default:
                    return "";
            }
        }
    }

    private class CierreArchivoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            if (column == 11) {
                return StatusBadgeV2.forEtapa(value == null ? "" : value.toString());
            }
            if (column == 12) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
            }
            if (column == 17) {
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

        @Override
        public String toString() {
            return nombre;
        }
    }
}
