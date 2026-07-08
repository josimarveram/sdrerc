package com.sdrerc.ui.views.notificacion;

import com.sdrerc.application.sdrercapp.DocumentoEjecucionService;
import com.sdrerc.application.sdrercapp.NotificacionExpedienteService;
import com.sdrerc.domain.dto.sdrercapp.CargoAcuseDTO;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.CierreNotificacionDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoEjecucionDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.PublicacionRequeridaDTO;
import com.sdrerc.ui.appv2.components.AppV2ActionPanel;
import com.sdrerc.ui.appv2.components.AppV2ColumnFilterSupport;
import com.sdrerc.ui.appv2.components.AppV2NotebookToggleTab;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.components.AppV2SearchField;
import com.sdrerc.ui.appv2.components.AppV2SearchToolbar;
import com.sdrerc.ui.appv2.components.AppV2SideActionPanel;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.components.AppV2TablePanel;
import com.sdrerc.ui.appv2.components.AppV2TableSectionPanel;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.PremiumDateFieldV2;
import com.sdrerc.ui.appv2.components.StatusBadgeV2;
import com.sdrerc.ui.appv2.helpers.EstadoExpedienteComboSupportV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.appv2.util.DisplayNameMapperV2;
import com.sdrerc.ui.views.expedienteconsola.DlgConsolaExpedienteV2;
import com.sdrerc.util.DateRangePickerSupport;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
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

public class JPanelNotificacionV2 extends JPanel {

    private enum FiltroKpi {
        TODOS,
        PENDIENTES,
        EN_REVISION,
        NOTIFICADOS,
        FALLIDOS,
        PUBLICACION,
        PLAZO_CRITICO
    }

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int PANEL_NOTIFICACION_ANCHO_MINIMO = 380;
    private static final int PANEL_NOTIFICACION_ANCHO_NORMAL = 430;
    private static final int PANEL_NOTIFICACION_TAB_OVERHANG = 18;
    private static final int PANEL_NOTIFICACION_TAB_TOP = 18;

    private final NotificacionExpedienteService notificacionService;
    private final DocumentoEjecucionService documentoService;

    private final AppV2SearchField txtBusqueda = new AppV2SearchField("Buscar expediente, trámite/SGD, acta, titular o documento", 28);
    private final JComboBox<SimpleItem> cmbEstadoFiltro = new JComboBox<SimpleItem>();
    private final PremiumDateFieldV2 fechaSolicitudDesde = new PremiumDateFieldV2();
    private final PremiumDateFieldV2 fechaSolicitudHasta = new PremiumDateFieldV2();
    private final JComboBox<SimpleItem> cmbTipoNotificacionFiltro = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbResultadoFiltro = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbPublicacionFiltro = new JComboBox<SimpleItem>();
    private final JSpinner spnLimite = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 50));
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnRegistrarNotificacion = new JButton("Registrar notificación");
    private final JButton btnRegistrarCargo = new JButton("Registrar cargo");
    private final JButton btnMarcarNotificado = new JButton("Marcar notificado");
    private final JButton btnRequierePublicacion = new JButton("Preparar publicación");
    private final JButton btnCerrarExpediente = new JButton("Cerrar expediente");

    private final JLabel lblEstado = new JLabel("Ingrese filtros y presione Buscar para consultar expedientes en Notificación.");
    private final JLabel lblExpediente = new JLabel("-");
    private final JLabel lblExpedienteSgd = new JLabel("-");
    private final JLabel lblTitular = new JLabel("-");
    private final JLabel lblActa = new JLabel("-");
    private final JLabel lblProcedimiento = new JLabel("-");
    private final JLabel lblEtapaEstado = new JLabel("-");
    private final JLabel lblResolucion = new JLabel("-");
    private final JLabel lblDocumentoNotificar = new JLabel("-");
    private final JLabel lblNotificacion = new JLabel("-");
    private final JLabel lblIntentos = new JLabel("-");
    private final JLabel lblCargo = new JLabel("-");
    private final JLabel lblSupervisor = new JLabel("-");
    private final JLabel lblPublicacion = new JLabel("-");
    private final JLabel lblDestino = new JLabel("-");
    private final JLabel lblAlertas = new JLabel("Sin alertas.");
    private final JLabel lblAcciones = new JLabel("-");
    private final JLabel lblAnalisis = new JLabel("-");
    private final JLabel lblVerificacion = new JLabel("-");
    private final JLabel lblEjecucion = new JLabel("-");
    private final JLabel lblCierreDestino = new JLabel("-");
    private final JLabel lblCierrePublicacion = new JLabel("-");
    private final JLabel lblCierreAlertas = new JLabel("Sin alertas.");

    private final JComboBox<SimpleItem> cmbTipoNotificacion = new JComboBox<SimpleItem>();
    private final JComboBox<SimpleItem> cmbEstadoCargo = new JComboBox<SimpleItem>();
    private final JTextField txtFechaNotificacion = new JTextField(10);
    private final JTextField txtFechaCargo = new JTextField(10);
    private final JTextField txtDestinatario = new JTextField(22);
    private final JTextField txtResultado = new JTextField(22);
    private final JTextField txtRecibidoPor = new JTextField(22);
    private final JTextArea txtComentario = new JTextArea(4, 22);
    private final JTextArea txtMotivoPublicacion = new JTextArea(3, 22);
    private final JTextArea txtObservacion = new JTextArea(3, 22);
    private final JTextArea txtComentarioCierre = new JTextArea(4, 22);
    private final AppV2NotebookToggleTab tabPanelNotificacion = new AppV2NotebookToggleTab();
    private final JTabbedPane tabsNotificacion = new JTabbedPane();

    private final NotificacionTableModel tableModel = new NotificacionTableModel();
    private final JTable table = new AppV2Table(tableModel);
    private final AppV2TablePanel tablePanel = new AppV2TablePanel(
            table,
            "Sin expedientes para mostrar",
            "Seleccione filtros y presione Buscar.");
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
    private final List<NotificacionExpedienteDTO> expedientes = new ArrayList<NotificacionExpedienteDTO>();
    private final AtomicLong secuenciaBusqueda = new AtomicLong(0L);
    private volatile SwingWorker<?, ?> busquedaActiva;

    private final MetricCardV2 cardPendientes = new MetricCardV2("Pendientes", "0", "Por notificar", AppV2Theme.INFO);
    private final MetricCardV2 cardRevision = new MetricCardV2("En revisión", "0", "Cargo pendiente", AppV2Theme.WARNING);
    private final MetricCardV2 cardNotificados = new MetricCardV2("Notificados", "0", "Confirmados", AppV2Theme.SUCCESS);
    private final MetricCardV2 cardFallidos = new MetricCardV2("Fallidos", "0", "Intentos agotados", AppV2Theme.ERROR);
    private final MetricCardV2 cardPublicacion = new MetricCardV2("Requieren publicación", "0", "Publicación prevista", AppV2Theme.PRIMARY);
    private final MetricCardV2 cardVencidos = new MetricCardV2("Por vencer", "0", "Vencidos o críticos", AppV2Theme.WARNING);
    private FiltroKpi kpiActivo = FiltroKpi.TODOS;
    private final List<NotificacionExpedienteDTO> expedientesVisibles = new ArrayList<NotificacionExpedienteDTO>();
    private AppV2OperationalSplitPanel splitOperativo;
    private AppV2SideActionPanel panelNotificacion;
    private AppV2SideActionPanel panelCierre;
    private JTabbedPane tabsBandejasTop;
    private JPanel bandejaAsignacionTab;
    private JPanel bandejaValidacionTab;
    private JPanel bandejaNotificacionTab;
    private enum ModoBandejaNotificacion {
        ASIGNACION,
        VALIDACION,
        NOTIFICACION
    }
    private ModoBandejaNotificacion modoBandejaNotificacion = ModoBandejaNotificacion.NOTIFICACION;
    private boolean panelNotificacionCerradoPorUsuario;

    public JPanelNotificacionV2() {
        this(new NotificacionExpedienteService(), new DocumentoEjecucionService());
    }

    public JPanelNotificacionV2(
            NotificacionExpedienteService notificacionService,
            DocumentoEjecucionService documentoService) {
        this.notificacionService = notificacionService;
        this.documentoService = documentoService;
        setLayout(new BorderLayout(8, 8));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        add(crearCentro(), BorderLayout.CENTER);
        configurarTabla();
        configurarDocumentosTabla();
        configurarEventos();
        configurarKpisInteractivos();
        cargarFiltrosBase();
        cargarCatalogos();
        inicializarFechas();
        inicializarFechasFiltro();
        actualizarSeleccion();
    }

    private JPanel crearHeader() {
        JPanel metricas = new JPanel(new GridLayout(2, 3, 12, 12));
        metricas.setOpaque(false);
        metricas.add(cardPendientes);
        metricas.add(cardRevision);
        metricas.add(cardNotificados);
        metricas.add(cardFallidos);
        metricas.add(cardPublicacion);
        metricas.add(cardVencidos);
        return metricas;
    }

    private JPanel crearCentro() {
        JPanel contenidoPrincipal = new JPanel(new BorderLayout(4, 4));
        contenidoPrincipal.setOpaque(false);
        contenidoPrincipal.add(crearHeader(), BorderLayout.NORTH);

        JPanel contenidoOperativo = new JPanel(new BorderLayout(4, 4));
        contenidoOperativo.setOpaque(false);
        contenidoOperativo.add(crearBuscador(), BorderLayout.NORTH);
        contenidoOperativo.add(crearBandeja(), BorderLayout.CENTER);
        contenidoPrincipal.add(contenidoOperativo, BorderLayout.CENTER);

        panelNotificacion = crearPanelNotificacion();
        panelCierre = crearPanelCierre();
        JPanel panelConTab = crearPanelNotificacionConTab(panelNotificacion, panelCierre);
        splitOperativo = new AppV2OperationalSplitPanel(
                contenidoPrincipal,
                panelConTab,
                0,
                PANEL_NOTIFICACION_ANCHO_MINIMO + PANEL_NOTIFICACION_TAB_OVERHANG,
                PANEL_NOTIFICACION_ANCHO_NORMAL + PANEL_NOTIFICACION_TAB_OVERHANG);
        return crearContenedorBandejasTop(splitOperativo);
    }

    private JPanel crearContenedorBandejasTop(final JPanel contenido) {
        tabsBandejasTop = new JTabbedPane();
        tabsBandejasTop.setOpaque(false);
        tabsBandejasTop.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        tabsBandejasTop.setBackground(AppV2Theme.BACKGROUND);
        tabsBandejasTop.setBorder(BorderFactory.createEmptyBorder());

        bandejaAsignacionTab = new JPanel(new BorderLayout());
        bandejaAsignacionTab.setOpaque(false);
        bandejaValidacionTab = new JPanel(new BorderLayout());
        bandejaValidacionTab.setOpaque(false);
        bandejaNotificacionTab = new JPanel(new BorderLayout());
        bandejaNotificacionTab.setOpaque(false);

        tabsBandejasTop.addTab("Bandeja Asignación", bandejaAsignacionTab);
        tabsBandejasTop.addTab("Bandeja Validación", bandejaValidacionTab);
        tabsBandejasTop.addTab("Bandeja Notificación", bandejaNotificacionTab);
        tabsBandejasTop.addChangeListener(e -> actualizarTabBandejaNotificacion());

        moverContenidoATabSeleccionada(contenido);
        actualizarTabBandejaNotificacion();
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(tabsBandejasTop, BorderLayout.CENTER);
        return wrapper;
    }

    private void moverContenidoATabSeleccionada(JPanel contenido) {
        JPanel destino = panelParaModoBandejaNotificacion();
        if (destino == null) {
            return;
        }
        if (contenido.getParent() == destino) {
            return;
        }
        if (contenido.getParent() != null) {
            contenido.getParent().remove(contenido);
        }
        destino.removeAll();
        destino.add(contenido, BorderLayout.CENTER);
        destino.revalidate();
        destino.repaint();
    }

    private JPanel panelParaModoBandejaNotificacion() {
        if (tabsBandejasTop == null) {
            return bandejaNotificacionTab;
        }
        int index = tabsBandejasTop.getSelectedIndex();
        if (index == 0) {
            modoBandejaNotificacion = ModoBandejaNotificacion.ASIGNACION;
            return bandejaAsignacionTab;
        }
        if (index == 1) {
            modoBandejaNotificacion = ModoBandejaNotificacion.VALIDACION;
            return bandejaValidacionTab;
        }
        modoBandejaNotificacion = ModoBandejaNotificacion.NOTIFICACION;
        return bandejaNotificacionTab;
    }

    private void actualizarTabBandejaNotificacion() {
        if (tabsNotificacion != null && tabsNotificacion.getTabCount() > 0) {
            if (modoBandejaNotificacion == ModoBandejaNotificacion.VALIDACION) {
                tabsNotificacion.setSelectedIndex(Math.min(1, tabsNotificacion.getTabCount() - 1));
            } else {
                tabsNotificacion.setSelectedIndex(0);
            }
        }
        if (splitOperativo != null) {
            moverContenidoATabSeleccionada(splitOperativo);
        }
    }

    private JPanel crearBuscador() {
        configurarControles();
        AppV2SearchToolbar toolbar = new AppV2SearchToolbar();
        JPanel accionesFiltro = AppV2ActionPanel.right();
        accionesFiltro.add(btnBuscar);
        accionesFiltro.add(btnLimpiar);
        accionesFiltro.add(btnRefrescar);
        toolbar.addSearchRow("Búsqueda", txtBusqueda, accionesFiltro);
        toolbar.addFilter("Fecha desde", fechaSolicitudDesde);
        toolbar.addFilter("Fecha hasta", fechaSolicitudHasta);
        toolbar.addFilter("Estado", cmbEstadoFiltro);
        toolbar.addFilter("Tipo notificación", cmbTipoNotificacionFiltro);
        toolbar.addFilter("Resultado", cmbResultadoFiltro);
        toolbar.addFilter("Publicación prevista", cmbPublicacionFiltro);
        toolbar.addCompactFilter(spnLimite);
        return toolbar;
    }

    private JPanel crearBandeja() {
        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);
        AppV2TableSectionPanel section = new AppV2TableSectionPanel(tablePanel);
        section.setStatus(lblEstado);
        return section;
    }

    private AppV2SideActionPanel crearPanelNotificacion() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Panel de notificación", new Runnable() {
            @Override
            public void run() {
                cerrarPanelNotificacion();
            }
        });
        panel.setAccentColor(AppV2Theme.PRIMARY);
        tabPanelNotificacion.setAccent(AppV2Theme.PRIMARY, AppV2Theme.SOFT_BLUE);
        tabPanelNotificacion.setExpanded(false);
        tabPanelNotificacion.setToolTipText("Ampliar panel de notificación");
        tabPanelNotificacion.addActionListener(e -> alternarExpansionPanelNotificacion());
        panel.addSection(crearResumenSeleccion());
        panel.addSection(crearAntecedentes());
        panel.addSection(crearDocumentosPanel());
        panel.addSection(crearValidacionCartaPanel());
        panel.addSection(crearFormularioNotificacion());
        panel.addSection(crearPublicacionPanel());
        panel.setFooter(crearAccionesPanelNotificacion());
        return panel;
    }

    private AppV2SideActionPanel crearPanelCierre() {
        AppV2SideActionPanel panel = new AppV2SideActionPanel("Cierre", new Runnable() {
            @Override
            public void run() {
                cerrarPanelNotificacion();
            }
        });
        panel.setAccentColor(AppV2Theme.PRIMARY);
        tabPanelNotificacion.setAccent(AppV2Theme.PRIMARY, AppV2Theme.SOFT_BLUE);
        panel.addSection(crearCierreResumenPanel());
        panel.addSection(crearCierrePanel());
        panel.setFooter(crearAccionesCierrePanel());
        return panel;
    }

    private JPanel crearPanelNotificacionConTab(final AppV2SideActionPanel panelNotificacion, final AppV2SideActionPanel panelCierre) {
        tabsNotificacion.setOpaque(false);
        tabsNotificacion.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        tabsNotificacion.addTab("Notificación", panelNotificacion);
        tabsNotificacion.addTab("Cierre", panelCierre);

        JPanel wrapper = new JPanel(null) {
            @Override
            public void doLayout() {
                int width = getWidth();
                int height = getHeight();
                int panelX = PANEL_NOTIFICACION_TAB_OVERHANG;
                tabsNotificacion.setBounds(panelX, 0, Math.max(0, width - panelX), height);
                int tabY = Math.min(PANEL_NOTIFICACION_TAB_TOP, Math.max(0, height - AppV2NotebookToggleTab.DEFAULT_HEIGHT));
                tabPanelNotificacion.setBounds(
                        0,
                        tabY,
                        AppV2NotebookToggleTab.DEFAULT_WIDTH,
                        AppV2NotebookToggleTab.DEFAULT_HEIGHT);
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(tabsNotificacion);
        wrapper.add(tabPanelNotificacion);
        wrapper.setMinimumSize(new Dimension(PANEL_NOTIFICACION_ANCHO_MINIMO + PANEL_NOTIFICACION_TAB_OVERHANG, 0));
        wrapper.setPreferredSize(new Dimension(PANEL_NOTIFICACION_ANCHO_NORMAL + PANEL_NOTIFICACION_TAB_OVERHANG, 0));
        return wrapper;
    }

    private JPanel crearAccionesPanelNotificacion() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setOpaque(false);
        panel.add(btnRegistrarNotificacion);
        panel.add(btnRegistrarCargo);
        panel.add(btnMarcarNotificado);
        panel.add(btnRequierePublicacion);
        return panel;
    }

    private JPanel crearCierrePanel() {
        JPanel panel = section("Cierre terminal");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Destino siguiente", lblCierreDestino);
        addRow(grid, row++, "Publicación prevista", lblCierrePublicacion);
        addRow(grid, row++, "Alertas", lblCierreAlertas);
        addRow(grid, row, "Comentario", scrollText(txtComentarioCierre, 86));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearCierreResumenPanel() {
        JPanel panel = section("Resumen de cierre");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Expediente", lblExpediente);
        addRow(grid, row++, "N° expediente SGD", lblExpedienteSgd);
        addRow(grid, row++, "Titular", lblTitular);
        addRow(grid, row++, "Procedimiento", lblProcedimiento);
        addRow(grid, row++, "Etapa / Estado", lblEtapaEstado);
        addRow(grid, row, "Documento a notificar", lblDocumentoNotificar);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearAccionesCierrePanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setOpaque(false);
        panel.add(btnCerrarExpediente);
        return panel;
    }

    private JPanel crearResumenSeleccion() {
        JPanel panel = section("Expediente seleccionado");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Expediente", lblExpediente);
        addRow(grid, row++, "N° expediente SGD", lblExpedienteSgd);
        addRow(grid, row++, "Titular", lblTitular);
        addRow(grid, row++, "Acta", lblActa);
        addRow(grid, row++, "Procedimiento", lblProcedimiento);
        addRow(grid, row++, "Etapa / Estado", lblEtapaEstado);
        addRow(grid, row++, "Resolución", lblResolucion);
        addRow(grid, row++, "Documento a notificar", lblDocumentoNotificar);
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

    private JPanel crearValidacionCartaPanel() {
        JPanel panel = section("Validación de carta");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Supervisor", lblSupervisor);
        addRow(grid, row, "Destino siguiente", lblDestino);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormularioNotificacion() {
        JPanel panel = section("Intentos de notificación y cargo");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Notificación actual", lblNotificacion);
        addRow(grid, row++, "Intentos", lblIntentos);
        addRow(grid, row++, "Tipo / modalidad", cmbTipoNotificacion);
        addRow(grid, row++, "Fecha notificación", txtFechaNotificacion);
        addRow(grid, row++, "Destinatario", txtDestinatario);
        addRow(grid, row++, "Resultado", txtResultado);
        addRow(grid, row++, "Cargo actual", lblCargo);
        addRow(grid, row++, "Estado cargo", cmbEstadoCargo);
        addRow(grid, row++, "Fecha cargo", txtFechaCargo);
        addRow(grid, row++, "Recibido por", txtRecibidoPor);
        addRow(grid, row++, "Comentario", scrollText(txtComentario, 86));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPublicacionPanel() {
        JPanel panel = section("Publicación prevista");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addRow(grid, row++, "Estado", lblPublicacion);
        addRow(grid, row, "Motivo", scrollText(txtMotivoPublicacion, 72));
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
        cmbEstadoFiltro.setPreferredSize(new Dimension(220, 34));
        fechaSolicitudDesde.setPreferredSize(new Dimension(190, 38));
        fechaSolicitudHasta.setPreferredSize(new Dimension(190, 38));
        cmbTipoNotificacionFiltro.setPreferredSize(new Dimension(180, 34));
        cmbResultadoFiltro.setPreferredSize(new Dimension(170, 34));
        cmbPublicacionFiltro.setPreferredSize(new Dimension(150, 34));
        cmbTipoNotificacion.setPreferredSize(new Dimension(250, 34));
        cmbEstadoCargo.setPreferredSize(new Dimension(250, 34));
        txtFechaNotificacion.setPreferredSize(new Dimension(250, 34));
        txtFechaCargo.setPreferredSize(new Dimension(250, 34));
        txtDestinatario.setPreferredSize(new Dimension(250, 34));
        txtResultado.setPreferredSize(new Dimension(250, 34));
        txtRecibidoPor.setPreferredSize(new Dimension(250, 34));
        spnLimite.setPreferredSize(new Dimension(86, 34));
        AppV2Theme.estilizarBotonPrimario(btnBuscar);
        AppV2Theme.estilizarBotonPrimario(btnRegistrarNotificacion);
        AppV2Theme.estilizarBotonPrimario(btnRegistrarCargo);
        AppV2Theme.estilizarBotonPrimario(btnMarcarNotificado);
        AppV2Theme.estilizarBotonPrimario(btnRequierePublicacion);
        btnRequierePublicacion.setToolTipText("Preparar metadata para publicación futura sin registrar publicación real.");
        AppV2Theme.estilizarBotonPrimario(btnCerrarExpediente);
    }

    private void configurarTabla() {
        table.setRowHeight(34);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        table.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        table.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        table.setGridColor(AppV2Theme.BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setDefaultRenderer(Object.class, new NotificacionRenderer());
        AppV2TableColumnSizer.applyFriendlyDefaults(table);
        table.getColumnModel().getColumn(0).setMaxWidth(84);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(190);
        table.getColumnModel().getColumn(5).setPreferredWidth(220);
        table.getColumnModel().getColumn(6).setPreferredWidth(190);
        table.getColumnModel().getColumn(7).setPreferredWidth(145);
        table.getColumnModel().getColumn(8).setMaxWidth(92);
        table.getColumnModel().getColumn(11).setMaxWidth(105);
        tablePanel.getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        columnFilterSupport = AppV2ColumnFilterSupport.install(
                "Notificacion",
                table,
                tablePanel.getScrollPane(),
                tablePanel,
                null);
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
        btnRegistrarNotificacion.addActionListener(e -> registrarNotificacion());
        btnRegistrarCargo.addActionListener(e -> registrarCargo());
        btnMarcarNotificado.addActionListener(e -> marcarNotificado());
        btnRequierePublicacion.addActionListener(e -> requierePublicacion());
        btnCerrarExpediente.addActionListener(e -> cerrarExpediente());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarSeleccion();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.rowAtPoint(e.getPoint()) >= 0) {
                    panelNotificacionCerradoPorUsuario = false;
                    if (splitOperativo != null) {
                        splitOperativo.setSideVisible(true);
                    }
                    actualizarSeleccion();
                }
            }
        });
    }

    private void cargarFiltrosBase() {
        EstadoExpedienteComboSupportV2.cargar(
                cmbEstadoFiltro, "NOTIFICACION", new SimpleItem("TODOS", "Todos los estados"),
                (codigo, nombre) -> new SimpleItem(codigo, nombre),
                ex -> lblEstado.setText("No se pudieron cargar los estados de Notificación."));
        cmbResultadoFiltro.removeAllItems();
        cmbResultadoFiltro.addItem(new SimpleItem("TODOS", "Todos los resultados"));
        cmbResultadoFiltro.addItem(new SimpleItem("ENVIADA", "Enviada"));
        cmbResultadoFiltro.addItem(new SimpleItem("EXITOSA", "Exitosa"));
        cmbResultadoFiltro.addItem(new SimpleItem("FALLIDA", "Fallida"));
        cmbPublicacionFiltro.removeAllItems();
        cmbPublicacionFiltro.addItem(new SimpleItem("TODOS", "Todas"));
        cmbPublicacionFiltro.addItem(new SimpleItem("SI", "Requiere publicación"));
        cmbPublicacionFiltro.addItem(new SimpleItem("NO", "Sin publicación"));
    }

    private void cargarCatalogos() {
        setTrabajando(true, "Cargando catálogos de notificación...");
        SwingWorker<CatalogosCarga, Void> worker = new SwingWorker<CatalogosCarga, Void>() {
            @Override
            protected CatalogosCarga doInBackground() throws Exception {
                return new CatalogosCarga(
                        notificacionService.listarTiposNotificacion(),
                        notificacionService.listarEstadosCargoAcuse());
            }

            @Override
            protected void done() {
                try {
                    CatalogosCarga carga = get();
                    cargarCombo(cmbTipoNotificacion, carga.tiposNotificacion, false);
                    cargarCombo(cmbTipoNotificacionFiltro, carga.tiposNotificacion, true);
                    cargarCombo(cmbEstadoCargo, carga.estadosCargo, false);
                } catch (Exception ex) {
                    cargarFallbackCatalogos();
                    mostrarError("No se pudieron cargar catálogos de notificación. Se usaron opciones base.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void cargarFallbackCatalogos() {
        cmbTipoNotificacion.removeAllItems();
        cmbTipoNotificacion.addItem(new SimpleItem("VIRTUAL", "Virtual"));
        cmbTipoNotificacion.addItem(new SimpleItem("PRESENCIAL_1", "Presencial 1"));
        cmbTipoNotificacion.addItem(new SimpleItem("PRESENCIAL_2", "Presencial 2"));
        cmbTipoNotificacionFiltro.removeAllItems();
        cmbTipoNotificacionFiltro.addItem(new SimpleItem("TODOS", "Todos los tipos"));
        cmbTipoNotificacionFiltro.addItem(new SimpleItem("VIRTUAL", "Virtual"));
        cmbTipoNotificacionFiltro.addItem(new SimpleItem("PRESENCIAL_1", "Presencial 1"));
        cmbTipoNotificacionFiltro.addItem(new SimpleItem("PRESENCIAL_2", "Presencial 2"));
        cmbEstadoCargo.removeAllItems();
        cmbEstadoCargo.addItem(new SimpleItem("CARGO_RECIBIDO", "Cargo recibido"));
        cmbEstadoCargo.addItem(new SimpleItem("CARGO_PENDIENTE", "Cargo pendiente"));
    }

    private void cargarCombo(JComboBox<SimpleItem> combo, List<CatalogoItemDTO> items, boolean incluirTodos) {
        combo.removeAllItems();
        if (incluirTodos) {
            combo.addItem(new SimpleItem("TODOS", "Todos"));
        }
        for (CatalogoItemDTO item : items) {
            combo.addItem(new SimpleItem(item.getCodigo(), item.getNombre()));
        }
    }

    private void inicializarFechas() {
        String hoy = DATE_FORMAT.format(LocalDate.now());
        txtFechaNotificacion.setText(hoy);
        txtFechaCargo.setText(hoy);
    }

    private void inicializarFechasFiltro() {
        fechaSolicitudDesde.setDate(DateRangePickerSupport.defaultSearchFromDate());
        fechaSolicitudHasta.setDate(DateRangePickerSupport.defaultSearchToDate());
    }

    private void buscar() {
        final LocalDate desde = toLocalDate(fechaSolicitudDesde);
        final LocalDate hasta = toLocalDate(fechaSolicitudHasta);
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            JOptionPane.showMessageDialog(this, "Fecha desde no puede ser mayor que Fecha hasta.", "Notificación", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        final long secuencia = secuenciaBusqueda.incrementAndGet();
        SwingWorker<?, ?> workerAnterior = busquedaActiva;
        if (workerAnterior != null && !workerAnterior.isDone()) {
            workerAnterior.cancel(true);
        }
        setTrabajando(true, "Consultando expedientes en Notificación...");
        final String texto = txtBusqueda.getText();
        final String estado = obtenerCodigo(cmbEstadoFiltro);
        final String tipoNotificacion = obtenerCodigo(cmbTipoNotificacionFiltro);
        final String resultadoNotificacion = obtenerCodigo(cmbResultadoFiltro);
        final String requierePublicacion = obtenerCodigo(cmbPublicacionFiltro);
        final int limite = ((Number) spnLimite.getValue()).intValue();
        SwingWorker<List<NotificacionExpedienteDTO>, Void> worker = new SwingWorker<List<NotificacionExpedienteDTO>, Void>() {
            @Override
            protected List<NotificacionExpedienteDTO> doInBackground() throws Exception {
                return notificacionService.buscarExpedientes(
                        texto,
                        estado,
                        desde,
                        hasta,
                        tipoNotificacion,
                        resultadoNotificacion,
                        requierePublicacion,
                        limite);
            }

            @Override
            protected void done() {
                try {
                    if (secuencia != secuenciaBusqueda.get()) {
                        return;
                    }
                    expedientes.clear();
                    expedientes.addAll(get());
                    expedientesVisibles.clear();
                    expedientesVisibles.addAll(filtrarKpi(expedientes));
                    tableModel.fireTableDataChanged();
                    table.clearSelection();
                    tablePanel.setEmpty(expedientesVisibles.isEmpty());
                    actualizarMetricas();
                    lblEstado.setText(expedientesVisibles.size() + " expediente(s) en Notificación encontrados.");
                    if (expedientesVisibles.isEmpty()) {
                        actualizarSeleccion();
                    } else {
                        actualizarSeleccion();
                    }
                    marcarKpis();
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar la bandeja de Notificación.", ex);
                } finally {
                    if (secuencia == secuenciaBusqueda.get()) {
                        setTrabajando(false, null);
                    }
                }
            }
        };
        busquedaActiva = worker;
        worker.execute();
    }

    private void limpiar() {
        if (columnFilterSupport != null) {
            columnFilterSupport.clearFilters();
        }
        txtBusqueda.setText("");
        seleccionarPrimero(cmbEstadoFiltro);
        seleccionarPrimero(cmbTipoNotificacionFiltro);
        seleccionarPrimero(cmbResultadoFiltro);
        seleccionarPrimero(cmbPublicacionFiltro);
        inicializarFechasFiltro();
        expedientes.clear();
        expedientesVisibles.clear();
        tableModel.fireTableDataChanged();
        table.clearSelection();
        tablePanel.setEmpty(true);
        actualizarMetricas();
        actualizarSeleccion();
        lblEstado.setText("Filtros limpiados. Presione Buscar para consultar Notificación.");
        panelNotificacionCerradoPorUsuario = false;
        kpiActivo = FiltroKpi.TODOS;
        marcarKpis();
    }

    private void configurarKpisInteractivos() {
        cardPendientes.setOnClick(() -> activarKpi(FiltroKpi.PENDIENTES));
        cardRevision.setOnClick(() -> activarKpi(FiltroKpi.EN_REVISION));
        cardNotificados.setOnClick(() -> activarKpi(FiltroKpi.NOTIFICADOS));
        cardFallidos.setOnClick(() -> activarKpi(FiltroKpi.FALLIDOS));
        cardPublicacion.setOnClick(() -> activarKpi(FiltroKpi.PUBLICACION));
        cardVencidos.setOnClick(() -> activarKpi(FiltroKpi.PLAZO_CRITICO));
        marcarKpis();
    }

    private void activarKpi(FiltroKpi filtro) {
        kpiActivo = filtro;
        expedientesVisibles.clear();
        expedientesVisibles.addAll(filtrarKpi(expedientes));
        tableModel.fireTableDataChanged();
        tablePanel.setEmpty(expedientesVisibles.isEmpty());
        if (expedientesVisibles.isEmpty()) {
            limpiarResumen();
        } else {
            actualizarSeleccion();
        }
        marcarKpis();
    }

    private void marcarKpis() {
        cardPendientes.setSelected(kpiActivo == FiltroKpi.PENDIENTES);
        cardRevision.setSelected(kpiActivo == FiltroKpi.EN_REVISION);
        cardNotificados.setSelected(kpiActivo == FiltroKpi.NOTIFICADOS);
        cardFallidos.setSelected(kpiActivo == FiltroKpi.FALLIDOS);
        cardPublicacion.setSelected(kpiActivo == FiltroKpi.PUBLICACION);
        cardVencidos.setSelected(kpiActivo == FiltroKpi.PLAZO_CRITICO);
    }

    private List<NotificacionExpedienteDTO> filtrarKpi(List<NotificacionExpedienteDTO> items) {
        List<NotificacionExpedienteDTO> filtrados = new ArrayList<NotificacionExpedienteDTO>();
        if (items == null || items.isEmpty() || kpiActivo == FiltroKpi.TODOS) {
            if (items != null) {
                filtrados.addAll(items);
            }
            return filtrados;
        }
        for (NotificacionExpedienteDTO item : items) {
            if (coincideKpi(item)) {
                filtrados.add(item);
            }
        }
        return filtrados;
    }

    private boolean coincideKpi(NotificacionExpedienteDTO item) {
        switch (kpiActivo) {
            case PENDIENTES:
                return item.isEnNotificacion();
            case EN_REVISION:
                return item.isCargoPendiente();
            case NOTIFICADOS:
                return item.isNotificado();
            case FALLIDOS:
                return item.isFallida();
            case PUBLICACION:
                return item.isRequierePublicacion() || item.isRequierePublicacionEstado();
            case PLAZO_CRITICO:
                return item.getDiasEnEtapa() != null && item.getDiasEnEtapa() <= 3;
            case TODOS:
            default:
                return true;
        }
    }

    private void actualizarMetricas() {
        int pendientes = 0;
        int revision = 0;
        int notificados = 0;
        int fallidos = 0;
        int publicacion = 0;
        int vencidos = 0;
        for (NotificacionExpedienteDTO expediente : expedientes) {
            if (expediente.isEnNotificacion()) {
                pendientes++;
            }
            if (expediente.isCargoPendiente()) {
                revision++;
            }
            if (expediente.isNotificado()) {
                notificados++;
            }
            if (expediente.isFallida()) {
                fallidos++;
            }
            if (expediente.isRequierePublicacion() || expediente.isRequierePublicacionEstado()) {
                publicacion++;
            }
            if (expediente.getDiasEnEtapa() != null && expediente.getDiasEnEtapa() <= 3) {
                vencidos++;
            }
        }
        cardPendientes.setValue(String.valueOf(pendientes));
        cardRevision.setValue(String.valueOf(revision));
        cardNotificados.setValue(String.valueOf(notificados));
        cardFallidos.setValue(String.valueOf(fallidos));
        cardPublicacion.setValue(String.valueOf(publicacion));
        cardVencidos.setValue(String.valueOf(vencidos));
        marcarKpis();
    }

    private void actualizarSeleccion() {
        NotificacionExpedienteDTO expediente = seleccionado();
        actualizarVisibilidadPanelNotificacion();
        if (expediente == null) {
            limpiarResumen();
            return;
        }
        lblExpediente.setText(valor(expediente.getNumeroExpediente()));
        lblExpedienteSgd.setText(valor(expediente.getNumeroExpedienteSgd()));
        lblTitular.setText(valor(expediente.getTitular()));
        lblActa.setText(valor(expediente.getTipoActa()) + " · " + valor(expediente.getNumeroActa()));
        lblProcedimiento.setText(valor(expediente.getProcedimiento()));
        lblEtapaEstado.setText(DisplayNameMapperV2.etapa(expediente.getEtapaCodigo()) + " / " + DisplayNameMapperV2.estado(expediente.getEstadoCodigo()));
        lblResolucion.setText(resolucionTexto(expediente));
        lblDocumentoNotificar.setText(valor(expediente.getDocumentoNotificarResumen()));
        lblNotificacion.setText(notificacionTexto(expediente));
        lblIntentos.setText(intentosTexto(expediente));
        lblCargo.setText(cargoTexto(expediente));
        lblSupervisor.setText(supervisorTexto(expediente));
        lblPublicacion.setText(publicacionTexto(expediente));
        lblDestino.setText(destinoTexto(expediente));
        lblAcciones.setText(accionesTexto(expediente));
        lblAlertas.setText(alertasTexto(expediente));
        lblAnalisis.setText(valor(expediente.getResultadoAnalisis()));
        lblVerificacion.setText(valor(expediente.getResultadoVerificacion()));
        lblEjecucion.setText(valor(expediente.getResultadoEjecucion()));
        lblCierreDestino.setText(destinoTexto(expediente));
        lblCierrePublicacion.setText(publicacionTexto(expediente));
        lblCierreAlertas.setText(alertasTexto(expediente));
        txtObservacion.setText(valor(expediente.getUltimaObservacion()));
        if (hasText(expediente.getTitular())) {
            txtDestinatario.setText(expediente.getTitular());
        }
        seleccionarModalidadSugerida(expediente);
        cargarDocumentos(expediente.getIdExpediente());
        actualizarAcciones(expediente);
    }

    private void limpiarResumen() {
        lblExpediente.setText("-");
        lblExpedienteSgd.setText("-");
        lblTitular.setText("-");
        lblActa.setText("-");
        lblProcedimiento.setText("-");
        lblEtapaEstado.setText("-");
        lblResolucion.setText("-");
        lblDocumentoNotificar.setText("-");
        lblNotificacion.setText("-");
        lblIntentos.setText("-");
        lblCargo.setText("-");
        lblSupervisor.setText("-");
        lblPublicacion.setText("-");
        lblDestino.setText("-");
        lblAcciones.setText("-");
        lblAlertas.setText("Sin expediente seleccionado.");
        lblAnalisis.setText("-");
        lblVerificacion.setText("-");
        lblEjecucion.setText("-");
        lblCierreDestino.setText("-");
        lblCierrePublicacion.setText("-");
        lblCierreAlertas.setText("Sin alertas.");
        if (tabsNotificacion != null && tabsNotificacion.getSelectedIndex() < 0 && tabsNotificacion.getTabCount() > 0) {
            tabsNotificacion.setSelectedIndex(0);
        }
        txtDestinatario.setText("");
        txtResultado.setText("");
        txtRecibidoPor.setText("");
        txtComentario.setText("");
        txtMotivoPublicacion.setText("");
        txtObservacion.setText("");
        txtComentarioCierre.setText("");
        inicializarFechas();
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

    private void actualizarAcciones(NotificacionExpedienteDTO expediente) {
        boolean seleccionado = expediente != null;
        btnRegistrarNotificacion.setEnabled(seleccionado && puedeRegistrarIntento(expediente));
        btnRegistrarCargo.setEnabled(seleccionado && expediente.hasAccion(NotificacionExpedienteService.ACCION_RECEPCION_CARGO));
        btnMarcarNotificado.setEnabled(seleccionado && expediente.hasAccion(NotificacionExpedienteService.ACCION_CONFIRMACION));
        btnRequierePublicacion.setEnabled(seleccionado
                && ((expediente.isIntentosAgotados() && expediente.hasAccion(NotificacionExpedienteService.ACCION_NOTIFICACION_FALLIDA))
                || expediente.hasAccion(NotificacionExpedienteService.ACCION_GENERACION_PUBLICACION)));
        btnCerrarExpediente.setEnabled(seleccionado && expediente.hasAccion(NotificacionExpedienteService.ACCION_CIERRE));
    }

    private void registrarNotificacion() {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        final String accion = resolverAccionNotificacion(expediente);
        if (!hasText(accion)) {
            JOptionPane.showMessageDialog(this, "No hay una acción de notificación activa para el tipo seleccionado.", "Notificación", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!confirmar("Se registrará el intento " + expediente.getSiguienteIntento()
                + " de notificación del expediente " + expediente.getNumeroExpediente() + ". ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando notificación...", new Callable<NotificacionResultadoDTO>() {
            @Override
            public NotificacionResultadoDTO call() throws Exception {
                return notificacionService.registrarNotificacion(crearRegistro(accion));
            }
        });
    }

    private void registrarCargo() {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null || !confirmar("Se registrará el cargo de acuse de " + expediente.getNumeroExpediente() + ". ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Registrando cargo de acuse...", new Callable<NotificacionResultadoDTO>() {
            @Override
            public NotificacionResultadoDTO call() throws Exception {
                return notificacionService.registrarCargo(crearCargo());
            }
        });
    }

    private void marcarNotificado() {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null || !confirmar("El expediente " + expediente.getNumeroExpediente() + " será marcado como notificado. ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Marcando expediente como notificado...", new Callable<NotificacionResultadoDTO>() {
            @Override
            public NotificacionResultadoDTO call() throws Exception {
                return notificacionService.marcarNotificado(crearRegistro(NotificacionExpedienteService.ACCION_CONFIRMACION));
            }
        });
    }

    private void requierePublicacion() {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        final String accion = expediente.hasAccion(NotificacionExpedienteService.ACCION_GENERACION_PUBLICACION)
                ? NotificacionExpedienteService.ACCION_GENERACION_PUBLICACION
                : NotificacionExpedienteService.ACCION_NOTIFICACION_FALLIDA;
        if (NotificacionExpedienteService.ACCION_NOTIFICACION_FALLIDA.equals(accion) && !expediente.isIntentosAgotados()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Para preparar publicación debe registrar primero los tres intentos de notificación.",
                    "Notificación",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String mensaje = NotificacionExpedienteService.ACCION_GENERACION_PUBLICACION.equals(accion)
                ? "Se preparará el expediente para publicación futura. ¿Desea continuar?"
                : "Se registrará notificación fallida y requerimiento de publicación futura. ¿Desea continuar?";
        if (!confirmar(mensaje)) {
            return;
        }
        ejecutarOperacion("Registrando publicación requerida...", new Callable<NotificacionResultadoDTO>() {
            @Override
            public NotificacionResultadoDTO call() throws Exception {
                return notificacionService.registrarPublicacion(crearPublicacion(accion));
            }
        });
    }

    private void cerrarExpediente() {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null || !confirmar("El expediente " + expediente.getNumeroExpediente() + " será cerrado. ¿Desea continuar?")) {
            return;
        }
        ejecutarOperacion("Cerrando expediente...", new Callable<NotificacionResultadoDTO>() {
            @Override
            public NotificacionResultadoDTO call() throws Exception {
                return notificacionService.cerrarExpediente(crearCierre());
            }
        });
    }

    private void ejecutarOperacion(String mensajeTrabajo, Callable<NotificacionResultadoDTO> operacion) {
        setTrabajando(true, mensajeTrabajo);
        SwingWorker<NotificacionResultadoDTO, Void> worker = new SwingWorker<NotificacionResultadoDTO, Void>() {
            @Override
            protected NotificacionResultadoDTO doInBackground() throws Exception {
                return operacion.call();
            }

            @Override
            protected void done() {
                try {
                    NotificacionResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            JPanelNotificacionV2.this,
                            resultado.getMensaje(),
                            "Notificación",
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

    private NotificacionRegistroDTO crearRegistro(String accionCodigo) {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        return new NotificacionRegistroDTO(
                expediente.getIdExpediente(),
                accionCodigo,
                tipoNotificacionParaIntento(expediente),
                parseFecha(txtFechaNotificacion, "notificación"),
                txtResultado.getText(),
                txtDestinatario.getText(),
                txtComentario.getText());
    }

    private CargoAcuseDTO crearCargo() {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        SimpleItem estadoCargo = (SimpleItem) cmbEstadoCargo.getSelectedItem();
        return new CargoAcuseDTO(
                expediente.getIdExpediente(),
                NotificacionExpedienteService.ACCION_RECEPCION_CARGO,
                estadoCargo == null ? "CARGO_RECIBIDO" : estadoCargo.getCodigo(),
                parseFecha(txtFechaCargo, "cargo"),
                txtRecibidoPor.getText(),
                txtComentario.getText());
    }

    private PublicacionRequeridaDTO crearPublicacion(String accionCodigo) {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        return new PublicacionRequeridaDTO(
                expediente.getIdExpediente(),
                accionCodigo,
                txtMotivoPublicacion.getText(),
                txtComentario.getText());
    }

    private CierreNotificacionDTO crearCierre() {
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        return new CierreNotificacionDTO(
                expediente.getIdExpediente(),
                NotificacionExpedienteService.ACCION_CIERRE,
                txtComentario.getText());
    }

    private String resolverAccionNotificacion(NotificacionExpedienteDTO expediente) {
        int intento = expediente.getSiguienteIntento();
        if (intento == 1 && expediente.hasAccion(NotificacionExpedienteService.ACCION_NOTIFICACION_VIRTUAL)) {
            return NotificacionExpedienteService.ACCION_NOTIFICACION_VIRTUAL;
        }
        if ((intento == 2 || intento == 3) && expediente.hasAccion(NotificacionExpedienteService.ACCION_NOTIFICACION_PRESENCIAL_2)) {
            return NotificacionExpedienteService.ACCION_NOTIFICACION_PRESENCIAL_2;
        }
        return "";
    }

    private boolean puedeRegistrarIntento(NotificacionExpedienteDTO expediente) {
        if (expediente == null || expediente.getSiguienteIntento() > 3) {
            return false;
        }
        return hasText(resolverAccionNotificacion(expediente));
    }

    private String tipoNotificacionParaIntento(NotificacionExpedienteDTO expediente) {
        if (expediente == null) {
            return "";
        }
        return expediente.getSiguienteIntento() == 1 ? "VIRTUAL" : "PRESENCIAL_2";
    }

    private void seleccionarModalidadSugerida(NotificacionExpedienteDTO expediente) {
        seleccionarComboPorCodigo(cmbTipoNotificacion, tipoNotificacionParaIntento(expediente));
    }

    private void seleccionarComboPorCodigo(JComboBox<SimpleItem> combo, String codigo) {
        if (combo == null || !hasText(codigo)) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            SimpleItem item = combo.getItemAt(i);
            if (item != null && codigo.equalsIgnoreCase(item.getCodigo())) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarPrimero(JComboBox<SimpleItem> combo) {
        if (combo != null && combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    private LocalDate toLocalDate(PremiumDateFieldV2 field) {
        if (field == null) {
            return null;
        }
        Date date = field.getDate();
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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
        NotificacionExpedienteDTO expediente = requerirSeleccion();
        if (expediente == null) {
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgConsolaExpedienteV2 dialog = new DlgConsolaExpedienteV2(owner, expediente.getIdExpediente());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private NotificacionExpedienteDTO seleccionado() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= expedientesVisibles.size()) {
            return null;
        }
        return expedientesVisibles.get(modelRow);
    }

    private NotificacionExpedienteDTO requerirSeleccion() {
        NotificacionExpedienteDTO expediente = seleccionado();
        if (expediente == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un expediente.", "Notificación", JOptionPane.INFORMATION_MESSAGE);
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
            btnRegistrarNotificacion.setEnabled(false);
            btnRegistrarCargo.setEnabled(false);
            btnMarcarNotificado.setEnabled(false);
            btnRequierePublicacion.setEnabled(false);
            btnCerrarExpediente.setEnabled(false);
        }
    }

    private String obtenerCodigo(JComboBox<SimpleItem> combo) {
        SimpleItem item = (SimpleItem) combo.getSelectedItem();
        return item == null ? "" : item.getCodigo();
    }

    private void cerrarPanelNotificacion() {
        panelNotificacionCerradoPorUsuario = true;
        if (splitOperativo != null) {
            splitOperativo.setSideVisible(false);
        }
        tabPanelNotificacion.setExpanded(false);
        actualizarTooltipTabPanelNotificacion();
    }

    private void actualizarVisibilidadPanelNotificacion() {
        if (splitOperativo == null) {
            return;
        }
        if (!splitOperativo.isSideVisible()) {
            return;
        }
        splitOperativo.setSideVisible(seleccionado() != null && !panelNotificacionCerradoPorUsuario);
        tabPanelNotificacion.setExpanded(splitOperativo.isSideExpanded());
        actualizarTooltipTabPanelNotificacion();
    }

    private void alternarExpansionPanelNotificacion() {
        if (splitOperativo == null || !splitOperativo.isSideVisible()) {
            return;
        }
        boolean expandido = splitOperativo.toggleSideExpanded();
        tabPanelNotificacion.setExpanded(expandido);
        actualizarTooltipTabPanelNotificacion();
        revalidate();
        repaint();
    }

    private void actualizarTooltipTabPanelNotificacion() {
        boolean expandido = splitOperativo != null && splitOperativo.isSideExpanded();
        tabPanelNotificacion.setToolTipText(expandido
                ? "Restaurar panel de notificación"
                : "Ampliar panel de notificación");
    }

    private String intentosTexto(NotificacionExpedienteDTO expediente) {
        int registrados = expediente.getNumeroIntento() == null ? 0 : expediente.getNumeroIntento();
        if (registrados >= 3) {
            return "3 de 3 intentos registrados";
        }
        return registrados + " de 3 registrados · siguiente intento " + expediente.getSiguienteIntento();
    }

    private String supervisorTexto(NotificacionExpedienteDTO expediente) {
        if (expediente == null) {
            return "-";
        }
        if (expediente.hasAccion(NotificacionExpedienteService.ACCION_NOTIFICACION_VIRTUAL)
                || expediente.hasAccion(NotificacionExpedienteService.ACCION_NOTIFICACION_PRESENCIAL_2)) {
            return "Carta lista para notificar según transición activa";
        }
        return "Sin devolución a Ejecución configurada; cualquier inconsistencia debe bloquearse con diagnóstico";
    }

    private String publicacionTexto(NotificacionExpedienteDTO expediente) {
        if (expediente == null) {
            return "-";
        }
        if (!expediente.isRequierePublicacion() && !expediente.isRequierePublicacionEstado()) {
            return "No requiere publicación";
        }
        String fecha = expediente.getFechaPublicacion() == null
                ? "sin fecha registrada"
                : "fecha " + format(expediente.getFechaPublicacion());
        return "Requiere publicación · " + fecha + " · dato registrado desde Asignación o Notificación";
    }

    private String destinoTexto(NotificacionExpedienteDTO expediente) {
        if (expediente == null) {
            return "-";
        }
        if (expediente.hasAccion(NotificacionExpedienteService.ACCION_GENERACION_PUBLICACION)) {
            return "Preparar para Publicación futura";
        }
        if (expediente.hasAccion(NotificacionExpedienteService.ACCION_CIERRE)) {
            return "Cierre / Archivo";
        }
        if (expediente.isCargoPendiente()) {
            return "Registrar cargo, siguiente intento o publicación futura según corresponda";
        }
        return "Pendiente de acción real activa";
    }

    private String alertasTexto(NotificacionExpedienteDTO expediente) {
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
        if (expediente.isCargoPendiente() && !expediente.hasAccion(NotificacionExpedienteService.ACCION_RECEPCION_CARGO)) {
            alertas.add("No hay transición activa para cargo");
        }
        if (expediente.isRequierePublicacion()) {
            alertas.add("Publicación requerida");
        }
        if (!expediente.hasAccion(NotificacionExpedienteService.ACCION_GENERACION_PUBLICACION)
                && expediente.isRequierePublicacionEstado()) {
            alertas.add("Sin transición activa a Publicación");
        }
        if (expediente.getSiguienteIntento() > 3 && !expediente.isRequierePublicacionEstado() && !expediente.isNotificado()) {
            alertas.add("Intentos agotados");
        }
        return alertas.isEmpty() ? "Sin alertas." : String.join(" · ", alertas);
    }

    private String accionesTexto(NotificacionExpedienteDTO expediente) {
        return hasText(expediente.getAccionesPermitidas())
                ? expediente.getAccionesPermitidas().replace(",", ", ")
                : "Sin acciones activas";
    }

    private String resolucionTexto(NotificacionExpedienteDTO expediente) {
        if (hasText(expediente.getNumeroResolucion())) {
            return expediente.getNumeroResolucion() + " · " + format(expediente.getFechaResolucion());
        }
        if (expediente.getIdResolucion() != null) {
            return "Resolución sin número visible";
        }
        return "Sin resolución registrada";
    }

    private String notificacionTexto(NotificacionExpedienteDTO expediente) {
        if (expediente.getIdNotificacion() == null) {
            return "Sin notificación registrada";
        }
        String intento = expediente.getNumeroIntento() == null ? "" : " · Intento " + expediente.getNumeroIntento();
        return valor(expediente.getTipoNotificacion()) + " · " + valor(expediente.getEstadoNotificacion()) + intento;
    }

    private String cargoTexto(NotificacionExpedienteDTO expediente) {
        if (expediente.getIdCargoAcuse() == null) {
            return "Sin cargo registrado";
        }
        return valor(expediente.getEstadoCargo()) + " · " + format(expediente.getFechaCargo());
    }

    private void mostrarError(String contexto, Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String detalle = cause.getMessage() == null ? "Error no especificado." : cause.getMessage();
        JOptionPane.showMessageDialog(this, contexto + "\n" + detalle, "Notificación", JOptionPane.WARNING_MESSAGE);
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

    private class NotificacionTableModel extends AbstractTableModel {

        private final String[] columns = {
            "Días", "Expediente", "N° expediente SGD", "Trámite / documento", "Titular",
            "Documento a notificar", "Estado", "Intento", "Tipo notificación",
            "Resultado", "Acuse", "Publicación prevista", "Alertas"
        };

        @Override
        public int getRowCount() {
            return expedientesVisibles.size();
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
            NotificacionExpedienteDTO item = expedientesVisibles.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return item.getDiasEnEtapa();
                case 1:
                    return item.getNumeroExpediente();
                case 2:
                    return item.getNumeroExpedienteSgd();
                case 3:
                    return item.getNumeroTramiteDocumentario();
                case 4:
                    return item.getTitular();
                case 5:
                    return item.getDocumentoNotificarResumen();
                case 6:
                    return DisplayNameMapperV2.estado(item.getEstadoCodigo());
                case 7:
                    return item.getNumeroIntento() == null ? "Sin intento" : "Intento " + item.getNumeroIntento();
                case 8:
                    return item.getTipoNotificacion();
                case 9:
                    return item.getResultadoNotificacion();
                case 10:
                    return item.isAcuseRegistrado() ? item.getEstadoCargo() : "Sin acuse";
                case 11:
                    return item.isRequierePublicacion() ? "Sí" : "No";
                case 12:
                    return alertasTexto(item);
                default:
                    return "";
            }
        }
    }

    private class NotificacionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            if (column == 0) {
                return StatusBadgeV2.forDias(value);
            }
            if (column == 6) {
                return StatusBadgeV2.forEstado(value == null ? "" : value.toString());
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

    private static class CatalogosCarga {

        private final List<CatalogoItemDTO> tiposNotificacion;
        private final List<CatalogoItemDTO> estadosCargo;

        private CatalogosCarga(List<CatalogoItemDTO> tiposNotificacion, List<CatalogoItemDTO> estadosCargo) {
            this.tiposNotificacion = tiposNotificacion;
            this.estadosCargo = estadosCargo;
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
