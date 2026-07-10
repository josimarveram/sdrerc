package com.sdrerc.ui.views.publicacion;

import com.sdrerc.application.sdrercapp.DocumentoEjecucionService;
import com.sdrerc.application.sdrercapp.PublicacionExpedienteService;
import com.sdrerc.domain.dto.sdrercapp.CierrePublicacionDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoEjecucionDTO;
import com.sdrerc.domain.dto.sdrercapp.PublicacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.PublicacionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.PublicacionResultadoDTO;
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2ColumnFilterSupport;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.helpers.EstadoExpedienteComboSupportV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import com.sdrerc.ui.views.expedienteconsola.DlgConsolaExpedienteV2;
import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import com.sdrerc.ui.appv2.components.AppV2ExpandCollapseGlyph;
import com.sdrerc.ui.appv2.components.AppV2TablePanel;
import com.sdrerc.ui.appv2.components.AppV2TableSectionPanel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JPanelPublicacionV2 extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PublicacionExpedienteService publicacionService;
    private final DocumentoEjecucionService documentoService;

    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite/SGD, acta, titular o documento", 28);
    private final JComboBox<SimpleItem> cmbEstadoFiltro = new JComboBox<SimpleItem>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnRegistrarPublicacion = new JButton("Registrar publicación");
    private final JButton btnMarcarRegistrada = new JButton("Marcar publicación registrada");
    private final JButton btnCerrarExpediente = new JButton("Cerrar expediente");

    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar expedientes en Publicación.");
    private final JLabel lblExpediente = new JLabel("-");
    private final JLabel lblTitular = new JLabel("-");
    private final JLabel lblActa = new JLabel("-");
    private final JLabel lblProcedimiento = new JLabel("-");
    private final JLabel lblEtapaEstado = new JLabel("-");
    private final JLabel lblResolucion = new JLabel("-");
    private final JLabel lblNotificacion = new JLabel("-");
    private final JLabel lblCargo = new JLabel("-");
    private final JLabel lblPublicacion = new JLabel("-");
    private final JLabel lblAcciones = new JLabel("-");
    private final JLabel lblAlertas = new JLabel("Sin alertas.");
    private final JLabel lblAnalisis = new JLabel("-");
    private final JLabel lblVerificacion = new JLabel("-");
    private final JLabel lblEjecucion = new JLabel("-");

    private final JTextField txtTipoPublicacion = new JTextField(22);
    private final JTextField txtFechaPublicacion = new JTextField(10);
    private final JTextField txtMedioPublicacion = new JTextField(22);
    private final JTextField txtNumeroPublicacion = new JTextField(22);
    private final JTextField txtResultadoPublicacion = new JTextField(22);
    private final JTextArea txtComentario = new JTextArea(4, 22);
    private final JTextArea txtObservacion = new JTextArea(3, 22);

    private final PublicacionTableModel tableModel = new PublicacionTableModel();
    private final JTable table = new AppV2Table(tableModel);
    private final JScrollPane tableScrollPane = new JScrollPane(table);
    private final JPanel tableHost = new JPanel(new BorderLayout());
    private AppV2ColumnFilterSupport.Controller columnFilterSupport;
    private final DefaultTableModel documentosModel = new DefaultTableModel(
            new Object[]{"Tipo", "Estado", "Número", "Documento", "Fecha"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable documentosTable = new JTable(documentosModel);
    private final List<PublicacionExpedienteDTO> expedientes = new ArrayList<PublicacionExpedienteDTO>();

    private final MetricCardV2 cardPendientes = new MetricCardV2("Pendientes", "0", "Por registrar publicación", AppV2Theme.WARNING);
    private final MetricCardV2 cardRegistradas = new MetricCardV2("Registradas", "0", "Publicación efectuada", AppV2Theme.SUCCESS);
    private final MetricCardV2 cardParaCierre = new MetricCardV2("Para cierre", "0", "Listas para cerrar", AppV2Theme.INFO);

    private final com.sdrerc.application.sdrercapp.DocumentoAnalisisService documentoAnalisisServicePub =
            new com.sdrerc.application.sdrercapp.DocumentoAnalisisService();
    private final List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> documentosPublicacionBandeja =
            new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>();
    private final List<PubFilaTabla> filasPublicacionBandeja = new ArrayList<PubFilaTabla>();
    private final java.util.Map<Long, List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>> intentosPublicacionCache =
            new java.util.HashMap<Long, List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>>();
    private final java.util.Set<Long> documentosPublicacionExpandidos = new java.util.HashSet<Long>();
    private final DefaultTableModel publicacionBandejaModel = new DefaultTableModel(
            new Object[]{"", "N° expediente", "Clas. Documentos", "Tipo documento", "N° Documento", "Fecha Emisión", "Titular", "Estado"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tablaPublicacionBandeja = new AppV2Table(publicacionBandejaModel);
    private final AppV2TablePanel tablaPublicacionBandejaPanel = new AppV2TablePanel(
            tablaPublicacionBandeja, "Sin documentos para publicar", "No hay documentos finalizados pendientes de publicación.");
    private final JLabel lblEstadoPublicacionBandeja = new JLabel("Haga clic en \"+\" para desplegar los intentos de notificación.");
    private final JButton btnAgregarIntentoPublicacion = new JButton("+ Agregar intento");
    private Long idDocumentoPublicacionSeleccionado;

    public JPanelPublicacionV2() {
        this(new PublicacionExpedienteService(), new DocumentoEjecucionService());
    }

    public JPanelPublicacionV2(
            PublicacionExpedienteService publicacionService,
            DocumentoEjecucionService documentoService) {
        this.publicacionService = publicacionService;
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
        inicializarFormulario();
        actualizarSeleccion();
        cargarBandejaPublicacionV2();
    }

    private JPanel crearHeader() {
        JPanel metricas = new JPanel(new GridLayout(1, 3, 12, 0));
        metricas.setOpaque(false);
        metricas.add(cardPendientes);
        metricas.add(cardRegistradas);
        metricas.add(cardParaCierre);
        return metricas;
    }

    private JPanel crearCentro() {
        JPanel centro = new JPanel(new BorderLayout(14, 14));
        centro.setOpaque(false);
        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setOpaque(false);
        JPanel bandejaPublicacionV2Wrapper = new JPanel(new BorderLayout());
        bandejaPublicacionV2Wrapper.setOpaque(false);
        bandejaPublicacionV2Wrapper.setPreferredSize(new Dimension(10, 320));
        bandejaPublicacionV2Wrapper.add(crearBandejaPublicacionV2(), BorderLayout.CENTER);
        principal.add(bandejaPublicacionV2Wrapper, BorderLayout.NORTH);
        principal.add(crearBandeja(), BorderLayout.CENTER);
        centro.add(principal, BorderLayout.CENTER);
        centro.add(crearPanelPublicacion(), BorderLayout.EAST);
        return centro;
    }

    private JPanel crearBandejaPublicacionV2() {
        tablaPublicacionBandeja.setRowHeight(32);
        tablaPublicacionBandeja.setAutoCreateRowSorter(false);
        tablaPublicacionBandeja.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaPublicacionBandeja.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablaPublicacionBandeja.getTableHeader().setReorderingAllowed(false);
        tablaPublicacionBandeja.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tablaPublicacionBandeja.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        tablaPublicacionBandeja.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        tablaPublicacionBandeja.setGridColor(AppV2Theme.BORDER);
        tablaPublicacionBandeja.setShowVerticalLines(false);
        AppV2TableColumnSizer.applyFriendlyDefaults(tablaPublicacionBandeja);
        tablaPublicacionBandeja.getColumnModel().getColumn(0).setMaxWidth(40);
        tablaPublicacionBandeja.getColumnModel().getColumn(0).setMinWidth(36);
        tablaPublicacionBandeja.getColumnModel().getColumn(0).setCellRenderer(new PubExpandirRenderer());
        tablaPublicacionBandeja.setDefaultRenderer(Object.class, new PubBandejaRenderer());
        tablaPublicacionBandeja.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = tablaPublicacionBandeja.rowAtPoint(e.getPoint());
                int viewCol = tablaPublicacionBandeja.columnAtPoint(e.getPoint());
                if (viewRow < 0) {
                    return;
                }
                int modelRow = tablaPublicacionBandeja.convertRowIndexToModel(viewRow);
                if (modelRow < 0 || modelRow >= filasPublicacionBandeja.size()) {
                    return;
                }
                PubFilaTabla fila = filasPublicacionBandeja.get(modelRow);
                if (viewCol == 0 && fila.esPadre()) {
                    alternarExpansionPublicacion(fila.idDocumento);
                    return;
                }
                idDocumentoPublicacionSeleccionado = fila.idDocumento;
            }
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        AppV2Theme.estilizarBotonPrimario(btnAgregarIntentoPublicacion);
        toolbar.add(btnAgregarIntentoPublicacion);
        btnAgregarIntentoPublicacion.addActionListener(e -> mostrarDialogoAgregarIntentoPublicacion());

        JPanel izquierda = new JPanel(new BorderLayout(6, 6));
        izquierda.setOpaque(false);
        izquierda.add(toolbar, BorderLayout.NORTH);
        AppV2TableSectionPanel section = new AppV2TableSectionPanel(tablaPublicacionBandejaPanel);
        section.setStatus(lblEstadoPublicacionBandeja);
        izquierda.add(section, BorderLayout.CENTER);
        return izquierda;
    }

    private void cargarBandejaPublicacionV2() {
        lblEstadoPublicacionBandeja.setText("Cargando documentos finalizados pendientes de publicación...");
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> doInBackground() throws Exception {
                return documentoAnalisisServicePub.listarDocumentosPublicacion();
            }

            @Override
            protected void done() {
                try {
                    List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> items = get();
                    documentosPublicacionBandeja.clear();
                    documentosPublicacionBandeja.addAll(items);
                    documentosPublicacionExpandidos.clear();
                    intentosPublicacionCache.clear();
                    reconstruirFilasPublicacionBandeja();
                    tablaPublicacionBandejaPanel.setEmpty(items.isEmpty());
                    lblEstadoPublicacionBandeja.setText(items.isEmpty()
                            ? "No hay documentos finalizados pendientes de publicación."
                            : items.size() + " documento(s) finalizados pendientes de publicación.");
                } catch (Exception ex) {
                    lblEstadoPublicacionBandeja.setText("No se pudieron cargar los documentos pendientes de publicación.");
                }
            }
        };
        worker.execute();
    }

    private void reconstruirFilasPublicacionBandeja() {
        filasPublicacionBandeja.clear();
        publicacionBandejaModel.setRowCount(0);
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : documentosPublicacionBandeja) {
            filasPublicacionBandeja.add(PubFilaTabla.padre(item));
            List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> intentos = intentosPublicacionCache.get(item.getIdDocumentoAnalizado());
            publicacionBandejaModel.addRow(new Object[]{
                documentosPublicacionExpandidos.contains(item.getIdDocumentoAnalizado()) ? "collapse" : "expand",
                item.getNumeroExpediente(),
                item.getClasificacion().isEmpty() ? "-" : item.getClasificacion(),
                item.getTipoDocumento().isEmpty() ? "-" : item.getTipoDocumento(),
                item.getNumeroDocumento().isEmpty() ? "-" : item.getNumeroDocumento(),
                item.getFechaDocumento() == null ? "-" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(item.getFechaDocumento()),
                item.getTitular().isEmpty() ? "-" : item.getTitular(),
                estadoPublicacionCalculado(intentos, item.getEstadoDocumento())
            });
            if (documentosPublicacionExpandidos.contains(item.getIdDocumentoAnalizado()) && intentos != null) {
                for (com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento : intentos) {
                    filasPublicacionBandeja.add(PubFilaTabla.hijo(item.getIdDocumentoAnalizado()));
                    publicacionBandejaModel.addRow(new Object[]{
                        "",
                        "↳ Intento " + intento.getNumeroIntento(),
                        intento.getTipoNotificacion().isEmpty() ? "-" : intento.getTipoNotificacion(),
                        intento.getEstadoNotificacion().isEmpty() ? "-" : intento.getEstadoNotificacion(),
                        intento.getCodigoNotificacion().isEmpty() ? "-" : intento.getCodigoNotificacion(),
                        intento.getFechaEnvio() == null ? "-" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(intento.getFechaEnvio()),
                        intento.getFechaRecepcion() == null ? "-" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(intento.getFechaRecepcion()),
                        intento.getFechaPublicacion() != null ? "Publicado" : (intento.isUbicado() ? "Ubicado" : "No ubicado")
                    });
                }
            }
        }
    }

    private String estadoPublicacionCalculado(List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> intentos, String estadoDocumentoFallback) {
        if (intentos == null || intentos.isEmpty()) {
            return estadoDocumentoFallback == null || estadoDocumentoFallback.isEmpty() ? "-" : estadoDocumentoFallback;
        }
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento : intentos) {
            if (intento.getFechaPublicacion() != null) {
                return "Publicado";
            }
        }
        boolean algunoUbicado = false;
        int intentosNoUbicados = 0;
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO intento : intentos) {
            if (intento.isUbicado()) {
                algunoUbicado = true;
            } else {
                intentosNoUbicados++;
            }
        }
        if (algunoUbicado) {
            return "Atendido";
        }
        if (intentosNoUbicados >= 2) {
            return "Pendiente de publicación";
        }
        return "Pendiente";
    }

    private void alternarExpansionPublicacion(Long idDocumento) {
        if (idDocumento == null) {
            return;
        }
        if (documentosPublicacionExpandidos.contains(idDocumento)) {
            documentosPublicacionExpandidos.remove(idDocumento);
            reconstruirFilasPublicacionBandeja();
            return;
        }
        if (intentosPublicacionCache.containsKey(idDocumento)) {
            documentosPublicacionExpandidos.add(idDocumento);
            reconstruirFilasPublicacionBandeja();
            return;
        }
        SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>, Void> worker =
                new SwingWorker<List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO>, Void>() {
            @Override
            protected List<com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO> doInBackground() throws Exception {
                return documentoAnalisisServicePub.listarIntentosNotificacion(idDocumento);
            }

            @Override
            protected void done() {
                try {
                    intentosPublicacionCache.put(idDocumento, get());
                    documentosPublicacionExpandidos.add(idDocumento);
                    reconstruirFilasPublicacionBandeja();
                } catch (Exception ex) {
                    mostrarErrorPublicacionV2("No se pudieron cargar los intentos de notificación.", ex);
                }
            }
        };
        worker.execute();
    }

    private void mostrarDialogoAgregarIntentoPublicacion() {
        if (idDocumentoPublicacionSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un documento de la bandeja de publicación.",
                    "Agregar intento", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO documento = null;
        for (com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item : documentosPublicacionBandeja) {
            if (idDocumentoPublicacionSeleccionado.equals(item.getIdDocumentoAnalizado())) {
                documento = item;
                break;
            }
        }
        if (documento == null) {
            return;
        }
        JComboBox<SimpleItem> cmbModalidad = new JComboBox<SimpleItem>();
        cmbModalidad.addItem(new SimpleItem("VIRTUAL", "Virtual"));
        cmbModalidad.addItem(new SimpleItem("PRESENCIAL_1", "Presencial 1"));
        cmbModalidad.addItem(new SimpleItem("PRESENCIAL_2", "Presencial 2"));
        cmbModalidad.addItem(new SimpleItem("PUBLICACION", "Publicación"));
        JTextField txtCodigo = new JTextField(16);
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(4, 0, 4, 8);
        GridBagConstraints gbcValue = new GridBagConstraints();
        gbcValue.gridx = 1;
        gbcValue.fill = GridBagConstraints.HORIZONTAL;
        gbcValue.insets = new Insets(4, 0, 4, 0);
        gbcLabel.gridy = 0;
        gbcValue.gridy = 0;
        form.add(new JLabel("Modalidad"), gbcLabel);
        form.add(cmbModalidad, gbcValue);
        gbcLabel.gridy = 1;
        gbcValue.gridy = 1;
        form.add(new JLabel("Código notificación"), gbcLabel);
        form.add(txtCodigo, gbcValue);

        int confirm = JOptionPane.showConfirmDialog(
                this, form, "Agregar intento de notificación", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }
        SimpleItem modalidad = (SimpleItem) cmbModalidad.getSelectedItem();
        final Long idExpediente = documento.getIdExpediente();
        final Long idDocumento = documento.getIdDocumentoAnalizado();
        final String tipoNotificacionCodigo = modalidad == null ? "PUBLICACION" : modalidad.codigo;
        final String codigoNotificacion = txtCodigo.getText();
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                documentoAnalisisServicePub.registrarIntentoNotificacion(idExpediente, idDocumento, tipoNotificacionCodigo, codigoNotificacion);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    intentosPublicacionCache.remove(idDocumento);
                    documentosPublicacionExpandidos.remove(idDocumento);
                    alternarExpansionPublicacion(idDocumento);
                } catch (Exception ex) {
                    mostrarErrorPublicacionV2("No se pudo registrar el intento de notificación.", ex);
                }
            }
        };
        worker.execute();
    }

    private void mostrarErrorPublicacionV2(String message, Exception ex) {
        String detail = ex == null || ex.getMessage() == null ? "" : "\n\n" + ex.getMessage();
        JOptionPane.showMessageDialog(this, message + detail, "Publicación", JOptionPane.ERROR_MESSAGE);
    }

    private static class PubFilaTabla {
        private final boolean padre;
        private final Long idDocumento;

        private PubFilaTabla(boolean padre, Long idDocumento) {
            this.padre = padre;
            this.idDocumento = idDocumento;
        }

        private static PubFilaTabla padre(com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO item) {
            return new PubFilaTabla(true, item.getIdDocumentoAnalizado());
        }

        private static PubFilaTabla hijo(Long idDocumento) {
            return new PubFilaTabla(false, idDocumento);
        }

        private boolean esPadre() {
            return padre;
        }
    }

    private class PubExpandirRenderer extends JPanel implements TableCellRenderer {
        private final AppV2ExpandCollapseGlyph glyph = new AppV2ExpandCollapseGlyph();

        private PubExpandirRenderer() {
            setOpaque(true);
            setLayout(new BorderLayout());
            add(glyph, BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Color background = isSelected ? new Color(219, 244, 249) : (row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
            setBackground(background);
            if ("expand".equals(value)) {
                glyph.configure(AppV2ExpandCollapseGlyph.EXPAND, AppV2Theme.PRIMARY, background);
            } else if ("collapse".equals(value)) {
                glyph.configure(AppV2ExpandCollapseGlyph.COLLAPSE, AppV2Theme.PRIMARY, background);
            } else {
                glyph.configure(AppV2ExpandCollapseGlyph.NONE, AppV2Theme.PRIMARY, background);
            }
            return this;
        }
    }

    private class PubBandejaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int modelRow = table.convertRowIndexToModel(row);
            boolean esHijo = modelRow >= 0 && modelRow < filasPublicacionBandeja.size() && !filasPublicacionBandeja.get(modelRow).esPadre();
            setFont(esHijo ? AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL) : AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (!isSelected) {
                setBackground(esHijo ? new Color(238, 250, 252) : (row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT));
                setForeground(esHijo ? AppV2Theme.TEXT_SECONDARY : AppV2Theme.TEXT_PRIMARY);
            }
            return c;
        }
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
        gbc.gridx = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtros.add(accionesFiltro, gbc);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnRegistrarPublicacion);
        acciones.add(btnMarcarRegistrada);
        acciones.add(btnCerrarExpediente);

        JPanel superior = new JPanel(new BorderLayout(8, 8));
        superior.setOpaque(false);
        superior.add(filtros, BorderLayout.NORTH);
        superior.add(acciones, BorderLayout.CENTER);
        superior.add(lblEstado, BorderLayout.SOUTH);
        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);

        panel.add(superior, BorderLayout.NORTH);
        tableHost.setOpaque(false);
        tableHost.add(tableScrollPane, BorderLayout.CENTER);
        panel.add(tableHost, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelPublicacion() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(440, 0));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.sectionBorder());

        JLabel title = new JLabel("Panel de publicación");
        title.setFont(AppV2Theme.fontBold(18));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.add(crearResumenSeleccion());
        content.add(crearAntecedentes());
        content.add(crearDocumentosPanel());
        content.add(crearFormularioPublicacion());

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
        addRow(grid, row++, "Cargo", lblCargo);
        addRow(grid, row++, "Publicación", lblPublicacion);
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
        int row = 0;
        addRow(grid, row++, "Análisis", lblAnalisis);
        addRow(grid, row++, "Verificación", lblVerificacion);
        addRow(grid, row++, "Ejecución", lblEjecucion);
        addRow(grid, row, "Observación", scrollText(txtObservacion, 72));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDocumentosPanel() {
        JPanel panel = section("Documentos y resolución");
        JScrollPane scroll = new JScrollPane(documentosTable);
        scroll.setPreferredSize(new Dimension(360, 132));
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormularioPublicacion() {
        JPanel panel = section("Registro de publicación");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Tipo", txtTipoPublicacion);
        addRow(grid, row++, "Fecha publicación", txtFechaPublicacion);
        addRow(grid, row++, "Medio", txtMedioPublicacion);
        addRow(grid, row++, "Referencia", txtNumeroPublicacion);
        addRow(grid, row++, "Resultado", txtResultadoPublicacion);
        addRow(grid, row, "Comentario", scrollText(txtComentario, 86));
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
        txtTipoPublicacion.setPreferredSize(new Dimension(250, 34));
        txtFechaPublicacion.setPreferredSize(new Dimension(250, 34));
        txtMedioPublicacion.setPreferredSize(new Dimension(250, 34));
        txtNumeroPublicacion.setPreferredSize(new Dimension(250, 34));
        txtResultadoPublicacion.setPreferredSize(new Dimension(250, 34));
        btnBuscar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnRegistrarPublicacion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnMarcarRegistrada.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        btnCerrarExpediente.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
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
        table.setDefaultRenderer(Object.class, new PublicacionRenderer());
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(10).setPreferredWidth(145);
        table.getColumnModel().getColumn(11).setPreferredWidth(170);
        table.getColumnModel().getColumn(12).setMaxWidth(92);
        table.getColumnModel().getColumn(16).setMaxWidth(92);
        table.getColumnModel().getColumn(17).setMaxWidth(92);
        AppV2TableColumnSizer.applyFriendlyDefaults(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        columnFilterSupport = AppV2ColumnFilterSupport.install(
                "Publicacion",
                table,
                tableScrollPane,
                tableHost,
                null,
                0);
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
        btnRegistrarPublicacion.addActionListener(e -> registrarPublicacion());
        btnMarcarRegistrada.addActionListener(e -> registrarPublicacion());
        btnCerrarExpediente.addActionListener(e -> cerrarExpediente());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarSeleccion();
            }
        });
    }

    private void cargarFiltrosBase() {
        EstadoExpedienteComboSupportV2.cargar(
                cmbEstadoFiltro, "PUBLICACION_CONDICIONAL", new SimpleItem("TODOS", "Todos los estados"),
                (codigo, nombre) -> new SimpleItem(codigo, nombre),
                ex -> lblEstado.setText("No se pudieron cargar los estados de Publicación."));
    }

    private void inicializarFormulario() {
        txtTipoPublicacion.setText("NOTIFICACION_FALLIDA");
        txtFechaPublicacion.setText(DATE_FORMAT.format(LocalDate.now()));
    }

    private void buscar() {
        setTrabajando(true, "Consultando expedientes en Publicación...");
        final String texto = txtBusqueda.getText();
        final String estado = obtenerCodigo(cmbEstadoFiltro);
        final int limite = ((Number) spnLimite.getValue()).intValue();
        SwingWorker<List<PublicacionExpedienteDTO>, Void> worker = new SwingWorker<List<PublicacionExpedienteDTO>, Void>() {
            @Override
            protected List<PublicacionExpedienteDTO> doInBackground() throws Exception {
                return publicacionService.buscarExpedientes(texto, estado, limite);
            }

            @Override
            protected void done() {
                try {
                    expedientes.clear();
                    expedientes.addAll(get());
                    tableModel.fireTableDataChanged();
                    actualizarMetricas();
                    lblEstado.setText(expedientes.size() + " expediente(s) en Publicación encontrados.");
                    if (!expedientes.isEmpty()) {
                        table.setRowSelectionInterval(0, 0);
                    } else {
                        actualizarSeleccion();
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar la bandeja de Publicación.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void limpiar() {
        if (columnFilterSupport != null) {
            columnFilterSupport.clearFilters();
        }
        txtBusqueda.setText("");
        cmbEstadoFiltro.setSelectedIndex(0);
        expedientes.clear();
        tableModel.fireTableDataChanged();
        actualizarMetricas();
        actualizarSeleccion();
        lblEstado.setText("Filtros limpiados. Presione Buscar para consultar Publicación.");
    }

    private void actualizarMetricas() {
        int pendientes = 0;
        int registradas = 0;
        int paraCierre = 0;
        for (PublicacionExpedienteDTO expediente : expedientes) {
            if (expediente.isPendientePublicacion()) {
                pendientes++;
            }
            if (expediente.isPublicacionRegistrada()) {
                registradas++;
                paraCierre++;
            }
        }
        cardPendientes.setValue(String.valueOf(pendientes));
        cardRegistradas.setValue(String.valueOf(registradas));
        cardParaCierre.setValue(String.valueOf(paraCierre));
    }

    private void actualizarSeleccion() {
        PublicacionExpedienteDTO expediente = seleccionado();
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
        lblCargo.setText(cargoTexto(expediente));
        lblPublicacion.setText(publicacionTexto(expediente));
        lblAcciones.setText(accionesTexto(expediente));
        lblAlertas.setText(alertasTexto(expediente));
        lblAnalisis.setText(valor(expediente.getResultadoAnalisis()));
        lblVerificacion.setText(valor(expediente.getResultadoVerificacion()));
        lblEjecucion.setText(valor(expediente.getResultadoEjecucion()));
        txtObservacion.setText(valor(expediente.getUltimaObservacion()));
        txtTipoPublicacion.setText(hasText(expediente.getTipoPublicacion()) ? expediente.getTipoPublicacion() : "NOTIFICACION_FALLIDA");
        txtFechaPublicacion.setText(expediente.getFechaPublicacion() == null ? DATE_FORMAT.format(LocalDate.now()) : format(expediente.getFechaPublicacion()));
        txtMedioPublicacion.setText(expediente.getMedioPublicacion());
        txtNumeroPublicacion.setText(expediente.getNumeroPublicacion());
        txtResultadoPublicacion.setText(hasText(expediente.getEstadoPublicacion()) ? expediente.getEstadoPublicacion() : "Publicación registrada");
        txtComentario.setText(expediente.getObservacionPublicacion());
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
        lblCargo.setText("-");
        lblPublicacion.setText("-");
        lblAcciones.setText("-");
        lblAlertas.setText("Sin expediente seleccionado.");
        lblAnalisis.setText("-");
        lblVerificacion.setText("-");
        lblEjecucion.setText("-");
        txtTipoPublicacion.setText("NOTIFICACION_FALLIDA");
        txtFechaPublicacion.setText(DATE_FORMAT.format(LocalDate.now()));
        txtMedioPublicacion.setText("");
        txtNumeroPublicacion.setText("");
        txtResultadoPublicacion.setText("");
        txtComentario.setText("");
        txtObservacion.setText("");
        documentosModel.setRowCount(0);
        actualizarAcciones(null);
    }

    private void cargarDocumentos(Long idExpediente) {
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

    private void actualizarAcciones(PublicacionExpedienteDTO expediente) {
        boolean seleccionado = expediente != null;
        btnRegistrarPublicacion.setEnabled(seleccionado && expediente.hasAccion(PublicacionExpedienteService.ACCION_REGISTRO_PUBLICACION));
        btnMarcarRegistrada.setEnabled(seleccionado && expediente.hasAccion(PublicacionExpedienteService.ACCION_REGISTRO_PUBLICACION));
        btnCerrarExpediente.setEnabled(seleccionado && expediente.hasAccion(PublicacionExpedienteService.ACCION_CIERRE));
    }

    private void registrarPublicacion() {
        PublicacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        if (!expediente.hasAccion(PublicacionExpedienteService.ACCION_REGISTRO_PUBLICACION)) {
            JOptionPane.showMessageDialog(this, "No hay una transición activa para registrar la publicación.", "Publicación", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!confirmar("Se registrará la publicación del expediente " + expediente.getNumeroExpediente() + ". ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando publicación...", new Callable<PublicacionResultadoDTO>() {
            @Override
            public PublicacionResultadoDTO call() throws Exception {
                return publicacionService.registrarPublicacion(crearRegistro());
            }
        });
    }

    private void cerrarExpediente() {
        PublicacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        if (!expediente.hasAccion(PublicacionExpedienteService.ACCION_CIERRE)) {
            JOptionPane.showMessageDialog(this, "No hay una transición activa para cerrar el expediente publicado.", "Publicación", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!confirmar("El expediente " + expediente.getNumeroExpediente() + " será cerrado. ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Cerrando expediente publicado...", new Callable<PublicacionResultadoDTO>() {
            @Override
            public PublicacionResultadoDTO call() throws Exception {
                return publicacionService.cerrarExpediente(crearCierre());
            }
        });
    }

    private void ejecutarOperacion(String mensajeTrabajo, Callable<PublicacionResultadoDTO> operacion) {
        setTrabajando(true, mensajeTrabajo);
        SwingWorker<PublicacionResultadoDTO, Void> worker = new SwingWorker<PublicacionResultadoDTO, Void>() {
            @Override
            protected PublicacionResultadoDTO doInBackground() throws Exception {
                return operacion.call();
            }

            @Override
            protected void done() {
                try {
                    PublicacionResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelPublicacionV2.this,
                            resultado.getMensaje(),
                            "Publicación",
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

    private PublicacionRegistroDTO crearRegistro() {
        PublicacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        return new PublicacionRegistroDTO(
                expediente.getIdExpediente(),
                PublicacionExpedienteService.ACCION_REGISTRO_PUBLICACION,
                txtTipoPublicacion.getText(),
                parseFecha(txtFechaPublicacion, "publicación"),
                txtMedioPublicacion.getText(),
                txtNumeroPublicacion.getText(),
                txtResultadoPublicacion.getText(),
                txtComentario.getText());
    }

    private CierrePublicacionDTO crearCierre() {
        PublicacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        return new CierrePublicacionDTO(
                expediente.getIdExpediente(),
                PublicacionExpedienteService.ACCION_CIERRE,
                txtComentario.getText());
    }

    private LocalDate parseFecha(JTextField field, String nombreCampo) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ingrese una fecha de " + nombreCampo + " válida con formato yyyy-MM-dd.");
        }
    }

    private void abrirDetalle() {
        PublicacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(owner, expediente.getIdExpediente());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private PublicacionExpedienteDTO seleccionado() {
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

    private PublicacionExpedienteDTO requerirSeleccion() {
        PublicacionExpedienteDTO expediente = seleccionado();
        if (expediente == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un expediente.", "Publicación", JOptionPane.INFORMATION_MESSAGE);
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
            btnRegistrarPublicacion.setEnabled(false);
            btnMarcarRegistrada.setEnabled(false);
            btnCerrarExpediente.setEnabled(false);
        }
    }

    private String obtenerCodigo(JComboBox<SimpleItem> combo) {
        SimpleItem item = (SimpleItem) combo.getSelectedItem();
        return item == null ? "" : item.getCodigo();
    }

    private String alertasTexto(PublicacionExpedienteDTO expediente) {
        List<String> alertas = new ArrayList<String>();
        if (expediente.getTotalRelacionados() > 0) {
            alertas.add(expediente.getTotalRelacionados() + " expediente(s) asociado(s)");
        }
        if (expediente.getTotalDocumentos() == 0) {
            alertas.add("Sin documentos registrados");
        }
        if (!hasText(expediente.getNumeroResolucion())) {
            alertas.add("Sin resolución visible");
        }
        if (expediente.getIdNotificacion() == null) {
            alertas.add("Sin notificación previa visible");
        }
        if (expediente.isPendientePublicacion() && !expediente.hasAccion(PublicacionExpedienteService.ACCION_REGISTRO_PUBLICACION)) {
            alertas.add("No hay transición activa para registrar publicación");
        }
        return alertas.isEmpty() ? "Sin alertas." : String.join(" · ", alertas);
    }

    private String accionesTexto(PublicacionExpedienteDTO expediente) {
        return hasText(expediente.getAccionesPermitidas())
                ? expediente.getAccionesPermitidas().replace(",", ", ")
                : "Sin acciones activas";
    }

    private String resolucionTexto(PublicacionExpedienteDTO expediente) {
        if (hasText(expediente.getNumeroResolucion())) {
            return expediente.getNumeroResolucion() + " · " + format(expediente.getFechaResolucion());
        }
        if (expediente.getIdResolucion() != null) {
            return "Resolución sin número visible";
        }
        return "Sin resolución registrada";
    }

    private String notificacionTexto(PublicacionExpedienteDTO expediente) {
        if (expediente.getIdNotificacion() == null) {
            return "Sin notificación registrada";
        }
        return valor(expediente.getTipoNotificacion()) + " · "
                + valor(expediente.getEstadoNotificacion()) + " · "
                + valor(expediente.getResultadoNotificacion());
    }

    private String cargoTexto(PublicacionExpedienteDTO expediente) {
        if (expediente.getIdCargoAcuse() == null) {
            return "Sin cargo registrado";
        }
        return valor(expediente.getEstadoCargo()) + " · " + format(expediente.getFechaCargo());
    }

    private String publicacionTexto(PublicacionExpedienteDTO expediente) {
        if (expediente.getIdPublicacion() == null) {
            return "Sin registro de publicación";
        }
        return valor(expediente.getEstadoPublicacion()) + " · "
                + valor(expediente.getMedioPublicacion()) + " · "
                + format(expediente.getFechaPublicacion());
    }

    private void mostrarError(String contexto, Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String detalle = cause.getMessage() == null ? "Error no especificado." : cause.getMessage();
        JOptionPane.showMessageDialog(this, contexto + "\n" + detalle, "Publicación", JOptionPane.WARNING_MESSAGE);
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

    private class PublicacionTableModel extends AbstractTableModel {

        private final String[] columns = {
            "ID", "Expediente", "Trámite", "Procedimiento", "Tipo doc.", "Tipo acta", "Nro. acta",
            "Titular", "Nro. resolución", "Ingreso publicación", "Etapa", "Estado",
            "Días", "Medio", "Referencia", "Fecha publicación", "Docs", "Asociados"
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
            PublicacionExpedienteDTO item = expedientes.get(rowIndex);
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
                    return format(item.getFechaIngresoPublicacion());
                case 10:
                    return item.getEtapaCodigo();
                case 11:
                    return item.getEstadoCodigo();
                case 12:
                    return item.getDiasEnEtapa();
                case 13:
                    return item.getMedioPublicacion();
                case 14:
                    return item.getNumeroPublicacion();
                case 15:
                    return format(item.getFechaPublicacion());
                case 16:
                    return item.getTotalDocumentos();
                case 17:
                    return item.getTotalRelacionados();
                default:
                    return "";
            }
        }
    }

    private class PublicacionRenderer extends DefaultTableCellRenderer {
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

        @Override
        public String toString() {
            return nombre;
        }
    }
}
