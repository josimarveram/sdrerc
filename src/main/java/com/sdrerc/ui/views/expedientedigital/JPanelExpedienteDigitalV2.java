package com.sdrerc.ui.views.expedientedigital;

import com.sdrerc.application.sdrercapp.DocumentoEjecucionService;
import com.sdrerc.application.sdrercapp.ExpedienteDigitalService;
import com.sdrerc.domain.dto.sdrercapp.DocumentoEjecucionDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteDigitalDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteDigitalRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteDigitalResultadoDTO;
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.helpers.EstadoExpedienteComboSupportV2;
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

public class JPanelExpedienteDigitalV2 extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ExpedienteDigitalService expedienteDigitalService;
    private final DocumentoEjecucionService documentoService;

    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite/SGD, titular o enlace digital", 28);
    private final JComboBox<SimpleItem> cmbEstadoFiltro = new JComboBox<SimpleItem>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnVerDetalle = new JButton("Ver detalle");
    private final JButton btnRegistrarCarpeta = new JButton("Registrar carpeta");
    private final JButton btnRegistrarEnlace = new JButton("Registrar enlace");
    private final JButton btnMarcarCompleto = new JButton("Marcar completo");

    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar expedientes digitales.");
    private final JLabel lblExpediente = new JLabel("-");
    private final JLabel lblTitular = new JLabel("-");
    private final JLabel lblActa = new JLabel("-");
    private final JLabel lblProcedimiento = new JLabel("-");
    private final JLabel lblEtapaEstado = new JLabel("-");
    private final JLabel lblResolucion = new JLabel("-");
    private final JLabel lblNotificacion = new JLabel("-");
    private final JLabel lblPublicacion = new JLabel("-");
    private final JLabel lblDigital = new JLabel("-");
    private final JLabel lblResponsableDigital = new JLabel("-");
    private final JLabel lblAcciones = new JLabel("-");
    private final JLabel lblAlertas = new JLabel("Sin alertas.");

    private final JTextField txtCodigoDigital = new JTextField(22);
    private final JTextField txtRutaCarpeta = new JTextField(22);
    private final JTextField txtEnlaceCarpeta = new JTextField(22);
    private final JTextArea txtComentario = new JTextArea(4, 22);
    private final JTextArea txtObservacion = new JTextArea(3, 22);

    private final ExpedienteDigitalTableModel tableModel = new ExpedienteDigitalTableModel();
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
    private final List<ExpedienteDigitalDTO> expedientes = new ArrayList<ExpedienteDigitalDTO>();

    private final MetricCardV2 cardCarpetas = new MetricCardV2("Carpetas", "0", "Carpeta creada", AppV2Theme.INFO);
    private final MetricCardV2 cardLinks = new MetricCardV2("Enlaces", "0", "Link registrado", AppV2Theme.WARNING);
    private final MetricCardV2 cardCompletos = new MetricCardV2("Completos", "0", "Expediente digital completo", AppV2Theme.SUCCESS);

    public JPanelExpedienteDigitalV2() {
        this(new ExpedienteDigitalService(), new DocumentoEjecucionService());
    }

    public JPanelExpedienteDigitalV2(
            ExpedienteDigitalService expedienteDigitalService,
            DocumentoEjecucionService documentoService) {
        this.expedienteDigitalService = expedienteDigitalService;
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
        metricas.add(cardCarpetas);
        metricas.add(cardLinks);
        metricas.add(cardCompletos);
        return metricas;
    }

    private JPanel crearCentro() {
        JPanel centro = new JPanel(new BorderLayout(14, 14));
        centro.setOpaque(false);
        centro.add(crearBandeja(), BorderLayout.CENTER);
        centro.add(crearPanelDigital(), BorderLayout.EAST);
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
        acciones.add(btnRegistrarCarpeta);
        acciones.add(btnRegistrarEnlace);
        acciones.add(btnMarcarCompleto);

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

    private JPanel crearPanelDigital() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(448, 0));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.sectionBorder());

        JLabel title = new JLabel("Panel de expediente digital");
        title.setFont(AppV2Theme.fontBold(18));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.add(crearResumenSeleccion());
        content.add(crearAntecedentes());
        content.add(crearDocumentosPanel());
        content.add(crearFormularioDigital());

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
        addRow(grid, row++, "Responsable", lblResponsableDigital);
        addRow(grid, row++, "Acciones", lblAcciones);
        addRow(grid, row, "Alertas", lblAlertas);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearAntecedentes() {
        JPanel panel = section("Antecedentes");
        txtObservacion.setEditable(false);
        txtObservacion.setBackground(AppV2Theme.SURFACE_ALT);
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        addRow(grid, 0, "Observación", scrollText(txtObservacion, 76));
        panel.add(grid, BorderLayout.CENTER);
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

    private JPanel crearFormularioDigital() {
        JPanel panel = section("Metadata digital");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Código", txtCodigoDigital);
        addRow(grid, row++, "Ruta / carpeta", txtRutaCarpeta);
        addRow(grid, row++, "Enlace", txtEnlaceCarpeta);
        addRow(grid, row, "Comentario", scrollText(txtComentario, 88));
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
        cmbEstadoFiltro.setPreferredSize(new Dimension(230, 34));
        spnLimite.setPreferredSize(new Dimension(86, 34));
        txtCodigoDigital.setPreferredSize(new Dimension(250, 34));
        txtRutaCarpeta.setPreferredSize(new Dimension(250, 34));
        txtEnlaceCarpeta.setPreferredSize(new Dimension(250, 34));
        btnBuscar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarCarpeta.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarEnlace.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnMarcarCompleto.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
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
        table.setDefaultRenderer(Object.class, new ExpedienteDigitalRenderer());
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);
        table.getColumnModel().getColumn(8).setPreferredWidth(160);
        table.getColumnModel().getColumn(9).setPreferredWidth(170);
        table.getColumnModel().getColumn(10).setPreferredWidth(170);
        table.getColumnModel().getColumn(11).setMaxWidth(90);
        table.getColumnModel().getColumn(12).setMaxWidth(90);
        table.getColumnModel().getColumn(13).setMaxWidth(90);
        table.getColumnModel().getColumn(14).setMaxWidth(90);
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
        btnRegistrarCarpeta.addActionListener(e -> registrarCarpeta());
        btnRegistrarEnlace.addActionListener(e -> registrarEnlace());
        btnMarcarCompleto.addActionListener(e -> marcarCompleto());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarSeleccion();
            }
        });
    }

    private void cargarFiltrosBase() {
        EstadoExpedienteComboSupportV2.cargar(
                cmbEstadoFiltro, "EXPEDIENTE_DIGITAL", new SimpleItem("TODOS", "Todos los estados"),
                (codigo, nombre) -> new SimpleItem(codigo, nombre),
                ex -> lblEstado.setText("No se pudieron cargar los estados de Expediente digital."));
    }

    private void buscar() {
        setTrabajando(true, "Consultando expedientes digitales...");
        final String texto = txtBusqueda.getText();
        final String estado = obtenerCodigo(cmbEstadoFiltro);
        final int limite = ((Number) spnLimite.getValue()).intValue();
        SwingWorker<List<ExpedienteDigitalDTO>, Void> worker = new SwingWorker<List<ExpedienteDigitalDTO>, Void>() {
            @Override
            protected List<ExpedienteDigitalDTO> doInBackground() throws Exception {
                return expedienteDigitalService.buscarExpedientes(texto, estado, limite);
            }

            @Override
            protected void done() {
                try {
                    expedientes.clear();
                    expedientes.addAll(get());
                    tableModel.fireTableDataChanged();
                    actualizarMetricas();
                    lblEstado.setText(expedientes.size() + " expediente(s) digitales encontrados.");
                    if (!expedientes.isEmpty()) {
                        table.setRowSelectionInterval(0, 0);
                    } else {
                        actualizarSeleccion();
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudieron consultar los expedientes digitales.", ex);
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
        limpiarResumen();
        lblEstado.setText("Filtros limpiados.");
    }

    private void actualizarMetricas() {
        int carpetas = 0;
        int links = 0;
        int completos = 0;
        for (ExpedienteDigitalDTO expediente : expedientes) {
            if (expediente.isCarpetaCreada()) {
                carpetas++;
            } else if (expediente.isLinkRegistrado()) {
                links++;
            } else if (expediente.isExpedienteDigitalCompleto()) {
                completos++;
            }
        }
        cardCarpetas.setValue(String.valueOf(carpetas));
        cardLinks.setValue(String.valueOf(links));
        cardCompletos.setValue(String.valueOf(completos));
    }

    private void actualizarSeleccion() {
        ExpedienteDigitalDTO expediente = seleccionado();
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
        lblDigital.setText(digitalTexto(expediente));
        lblResponsableDigital.setText(responsableTexto(expediente));
        lblAcciones.setText(accionesTexto(expediente));
        lblAlertas.setText(alertasTexto(expediente));
        txtObservacion.setText(valor(expediente.getUltimaObservacion()));
        txtCodigoDigital.setText(expediente.getCodigoExpedienteDigital());
        txtRutaCarpeta.setText(expediente.getRutaCarpeta());
        txtEnlaceCarpeta.setText(expediente.getEnlaceCarpeta());
        txtComentario.setText("");
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
        lblResponsableDigital.setText("-");
        lblAcciones.setText("-");
        lblAlertas.setText("Sin expediente seleccionado.");
        txtCodigoDigital.setText("");
        txtRutaCarpeta.setText("");
        txtEnlaceCarpeta.setText("");
        txtComentario.setText("");
        txtObservacion.setText("");
        documentosModel.setRowCount(0);
        actualizarAcciones(null);
    }

    private void cargarDocumentos(final Long idExpediente) {
        documentosModel.setRowCount(0);
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

    private void actualizarAcciones(ExpedienteDigitalDTO expediente) {
        boolean seleccionado = expediente != null;
        btnVerDetalle.setEnabled(seleccionado);
        btnRegistrarCarpeta.setEnabled(seleccionado
                && expediente.isCarpetaCreada()
                && expediente.hasAccion(ExpedienteDigitalService.ACCION_CREACION_CARPETA));
        btnRegistrarEnlace.setEnabled(seleccionado
                && expediente.isCarpetaCreada()
                && expediente.hasAccion(ExpedienteDigitalService.ACCION_CREACION_CARPETA));
        btnMarcarCompleto.setEnabled(seleccionado
                && expediente.isLinkRegistrado()
                && expediente.hasAccion(ExpedienteDigitalService.ACCION_CARGA_DOCUMENTOS));
    }

    private void registrarCarpeta() {
        ExpedienteDigitalDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        if (!expediente.hasAccion(ExpedienteDigitalService.ACCION_CREACION_CARPETA)) {
            JOptionPane.showMessageDialog(this, "No hay una transición activa para registrar la carpeta digital.", "Expediente digital", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!confirmar("Se registrará la carpeta digital del expediente " + expediente.getNumeroExpediente() + ". ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando carpeta digital...", new Callable<ExpedienteDigitalResultadoDTO>() {
            @Override
            public ExpedienteDigitalResultadoDTO call() throws Exception {
                return expedienteDigitalService.registrarCarpeta(crearRegistro(ExpedienteDigitalService.ACCION_CREACION_CARPETA));
            }
        });
    }

    private void registrarEnlace() {
        ExpedienteDigitalDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        if (!expediente.hasAccion(ExpedienteDigitalService.ACCION_CREACION_CARPETA)) {
            JOptionPane.showMessageDialog(this, "No hay una transición activa para registrar el enlace digital.", "Expediente digital", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!confirmar("Se registrará el enlace digital del expediente " + expediente.getNumeroExpediente() + ". ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando enlace digital...", new Callable<ExpedienteDigitalResultadoDTO>() {
            @Override
            public ExpedienteDigitalResultadoDTO call() throws Exception {
                return expedienteDigitalService.registrarEnlace(crearRegistro(ExpedienteDigitalService.ACCION_CREACION_CARPETA));
            }
        });
    }

    private void marcarCompleto() {
        ExpedienteDigitalDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        if (!expediente.hasAccion(ExpedienteDigitalService.ACCION_CARGA_DOCUMENTOS)) {
            JOptionPane.showMessageDialog(this, "No hay una transición activa para marcar el expediente digital como completo.", "Expediente digital", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!confirmar("El expediente " + expediente.getNumeroExpediente() + " será marcado como digitalmente completo. ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Marcando expediente digital completo...", new Callable<ExpedienteDigitalResultadoDTO>() {
            @Override
            public ExpedienteDigitalResultadoDTO call() throws Exception {
                return expedienteDigitalService.marcarCompleto(crearRegistro(ExpedienteDigitalService.ACCION_CARGA_DOCUMENTOS));
            }
        });
    }

    private void ejecutarOperacion(String mensajeTrabajo, Callable<ExpedienteDigitalResultadoDTO> operacion) {
        setTrabajando(true, mensajeTrabajo);
        SwingWorker<ExpedienteDigitalResultadoDTO, Void> worker = new SwingWorker<ExpedienteDigitalResultadoDTO, Void>() {
            @Override
            protected ExpedienteDigitalResultadoDTO doInBackground() throws Exception {
                return operacion.call();
            }

            @Override
            protected void done() {
                try {
                    ExpedienteDigitalResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelExpedienteDigitalV2.this,
                            resultado.getMensaje(),
                            "Expediente digital",
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

    private ExpedienteDigitalRegistroDTO crearRegistro(String accionCodigo) {
        ExpedienteDigitalDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        return new ExpedienteDigitalRegistroDTO(
                expediente.getIdExpediente(),
                accionCodigo,
                txtCodigoDigital.getText(),
                txtRutaCarpeta.getText(),
                txtEnlaceCarpeta.getText(),
                txtComentario.getText());
    }

    private void abrirDetalle() {
        ExpedienteDigitalDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(owner, expediente.getIdExpediente());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private ExpedienteDigitalDTO seleccionado() {
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

    private ExpedienteDigitalDTO requerirSeleccion() {
        ExpedienteDigitalDTO expediente = seleccionado();
        if (expediente == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un expediente.", "Expediente digital", JOptionPane.INFORMATION_MESSAGE);
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
            btnRegistrarCarpeta.setEnabled(false);
            btnRegistrarEnlace.setEnabled(false);
            btnMarcarCompleto.setEnabled(false);
        }
    }

    private String obtenerCodigo(JComboBox<SimpleItem> combo) {
        SimpleItem item = (SimpleItem) combo.getSelectedItem();
        return item == null ? "" : item.getCodigo();
    }

    private String alertasTexto(ExpedienteDigitalDTO expediente) {
        List<String> alertas = new ArrayList<String>();
        if (expediente.getTotalRelacionados() > 0) {
            alertas.add(expediente.getTotalRelacionados() + " expediente(s) asociado(s)");
        }
        if (expediente.getTotalDocumentosMetadata() == 0) {
            alertas.add("Sin documentos registrados");
        }
        if (!hasText(expediente.getRutaCarpeta()) && !hasText(expediente.getEnlaceCarpeta())) {
            alertas.add("Sin ruta o enlace digital");
        }
        if (expediente.isCarpetaCreada() && !expediente.hasAccion(ExpedienteDigitalService.ACCION_CREACION_CARPETA)) {
            alertas.add("No hay transición activa para registrar metadata digital");
        }
        if (expediente.isLinkRegistrado() && !expediente.hasAccion(ExpedienteDigitalService.ACCION_CARGA_DOCUMENTOS)) {
            alertas.add("No hay transición activa para marcar completo");
        }
        return alertas.isEmpty() ? "Sin alertas." : String.join(" · ", alertas);
    }

    private String accionesTexto(ExpedienteDigitalDTO expediente) {
        return hasText(expediente.getAccionesPermitidas())
                ? expediente.getAccionesPermitidas().replace(",", ", ")
                : "Sin acciones activas";
    }

    private String resolucionTexto(ExpedienteDigitalDTO expediente) {
        if (hasText(expediente.getNumeroResolucion())) {
            return expediente.getNumeroResolucion() + " · " + format(expediente.getFechaResolucion());
        }
        if (expediente.getIdResolucion() != null) {
            return "Resolución sin número visible";
        }
        return "Sin resolución registrada";
    }

    private String notificacionTexto(ExpedienteDigitalDTO expediente) {
        if (expediente.getIdNotificacion() == null) {
            return "Sin notificación registrada";
        }
        return valor(expediente.getResultadoNotificacion());
    }

    private String publicacionTexto(ExpedienteDigitalDTO expediente) {
        if (expediente.getIdPublicacion() == null) {
            return "Sin publicación registrada";
        }
        return valor(expediente.getEstadoPublicacion());
    }

    private String digitalTexto(ExpedienteDigitalDTO expediente) {
        if (expediente.getIdExpedienteDigital() == null) {
            return "Sin metadata digital activa";
        }
        List<String> partes = new ArrayList<String>();
        if (hasText(expediente.getCodigoExpedienteDigital())) {
            partes.add(expediente.getCodigoExpedienteDigital());
        }
        if (hasText(expediente.getRutaCarpeta())) {
            partes.add("Ruta registrada");
        }
        if (hasText(expediente.getEnlaceCarpeta())) {
            partes.add("Enlace registrado");
        }
        partes.add(expediente.isCompleto() ? "Completo" : "Pendiente");
        return String.join(" · ", partes);
    }

    private String responsableTexto(ExpedienteDigitalDTO expediente) {
        if (hasText(expediente.getResponsableDigital())) {
            return expediente.getResponsableDigital();
        }
        if (hasText(expediente.getCustodioDigital())) {
            return expediente.getCustodioDigital();
        }
        return valor(expediente.getResponsable());
    }

    private void mostrarError(String contexto, Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String detalle = cause.getMessage() == null ? "Error no especificado." : cause.getMessage();
        JOptionPane.showMessageDialog(this, contexto + "\n" + detalle, "Expediente digital", JOptionPane.WARNING_MESSAGE);
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

    private class ExpedienteDigitalTableModel extends AbstractTableModel {

        private final String[] columns = {
            "ID", "Expediente", "Trámite", "Procedimiento", "Tipo doc.", "Tipo acta", "Nro. acta",
            "Titular", "Etapa", "Estado", "Ruta/carpeta", "Enlace", "Docs", "Asociados", "Días"
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
            ExpedienteDigitalDTO item = expedientes.get(rowIndex);
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
                    return item.getEtapaCodigo();
                case 9:
                    return item.getEstadoCodigo();
                case 10:
                    return item.getRutaCarpeta();
                case 11:
                    return item.getEnlaceCarpeta();
                case 12:
                    return item.getTotalDocumentosMetadata();
                case 13:
                    return item.getTotalRelacionados();
                case 14:
                    return item.getDiasEnEtapa();
                default:
                    return "";
            }
        }
    }

    private class ExpedienteDigitalRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            if (column == 8) {
                return StatusBadgeV2.forEtapa(value == null ? "" : value.toString());
            }
            if (column == 9) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
            }
            if (column == 14) {
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
