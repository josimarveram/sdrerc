/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.asignacion;

import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.application.CatalogoService;
import com.sdrerc.application.ExpedienteService;
import com.sdrerc.application.PlazoAtencionService;
import com.toedter.calendar.JDateChooser;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.Enumerado;
import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.ui.common.icon.IconUtils;
import com.sdrerc.ui.common.swing.PlazoAtencionCellRenderer;
import com.sdrerc.ui.common.swing.TablePaginationHelper;
import com.sdrerc.ui.menu.MenuPrincipal;
import com.sdrerc.util.DateRangePickerSupport;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author David
 */
public class JPanelFiltroBusqueda extends javax.swing.JPanel {

    /**
     * Creates new form JPanelFiltroBusqueda
     */
    
    //public CatalogoItemService catalogoItemService = new CatalogoItemService();
    
    private final ExpedienteService expedienteService;
    private final CatalogoService catalogoService;
    private final CatalogoItemService catalogoItemService;
    private final PlazoAtencionService plazoAtencionService;
    private final Map<Integer, String> estadosPorId;
    private final Map<Integer, String> tiposSolicitudPorId;
    private final Map<Integer, String> tiposDocumentoPorId;
    private final Map<Integer, String> procedimientosPorId;
    private final Map<Integer, String> tiposActaPorId;
    private final Map<Integer, String> unidadesOrganicasPorId;
    private final Map<Integer, String> direccionesDomiciliariasPorId;
    private final SimpleDateFormat formatoFecha;
    private DateRangePickerSupport.Range rangoFechas;
    private JLabel lblFeedbackFechas;
    private boolean tooltipOrdenamientoHeaderConfigurado;
    private boolean filtrosPorColumnaConfigurados;
    private TablePaginationHelper paginationHelper;
    private JPanel panelPaginacion;
    private final JDateChooser filtroFechaSolicitudColumna = new JDateChooser();
    private final Map<Integer, JTextField> filtrosTextoPorColumna = new HashMap<>();
    private final Map<String, List<Expediente>> documentosAsociadosPorNumero = new HashMap<>();
    private final Set<Integer> expedientesExpandidos = new HashSet<>();
    private static final int COL_SELECCION = 0;
    private static final int COL_EXPANDIR = 1;
    private static final int COL_ID = 2;
    private static final int COL_FECHA_SOLICITUD = 3;
    private static final int COL_CANAL = 4;
    private static final int COL_REFERENCIA = 5;
    private static final int COL_TIPO_SOLICITUD = 6;
    private static final int COL_PROCEDIMIENTO_REGISTRAL = 7;
    private static final int COL_ACTA = 8;
    private static final int COL_TITULAR = 9;
    private static final int COL_ESTADO = 10;
    private static final int COL_DIAS_RESTANTES = 11;
    private static final int COL_ESTADO_ID = 12;
    private static final int COL_TIPO_DOCUMENTO = 13;
    private static final int COL_NUMERO_DOCUMENTO = 14;
    private static final int COL_TIPO_ACTA = 15;
    private static final int COL_NUMERO_ACTA = 16;
    private static final int COL_DNI_TITULAR_1 = 17;
    private static final int COL_TITULAR_1 = 18;
    private static final int COL_DNI_TITULAR_2 = 19;
    private static final int COL_TITULAR_2 = 20;
    private static final int COL_UNIDAD_ORGANICA = 21;
    private static final int COL_CORREO_ELECTRONICO = 22;
    private static final int COL_CELULAR = 23;
    private static final int COL_DIRECCION_DOMICILIARIA = 24;
    private static final int COL_DOMICILIO = 25;
    private static final int COL_DEPARTAMENTO = 26;
    private static final int COL_PROVINCIA = 27;
    private static final int COL_DISTRITO = 28;
    private static final int COL_ABOGADO_DESIGNADO = 29;
    private static final int COL_SUPERVISOR_RESPONSABLE = 30;
    private static final int COL_TIPO_FILA = 31;
    private static final int COL_ID_PADRE = 32;
    private static final String TIPO_FILA_PRINCIPAL = "PRINCIPAL";
    private static final String TIPO_FILA_DETALLE = "DETALLE";
    private static final int[] COLUMNAS_EXPORTACION_EXCEL = {
        COL_ID, COL_FECHA_SOLICITUD, COL_CANAL, COL_REFERENCIA, COL_TIPO_SOLICITUD,
        COL_PROCEDIMIENTO_REGISTRAL, COL_ESTADO,
        COL_TIPO_DOCUMENTO, COL_NUMERO_DOCUMENTO, COL_TIPO_ACTA, COL_NUMERO_ACTA,
        COL_DNI_TITULAR_1, COL_TITULAR_1, COL_DNI_TITULAR_2, COL_TITULAR_2,
        COL_UNIDAD_ORGANICA, COL_CORREO_ELECTRONICO, COL_CELULAR,
        COL_DIRECCION_DOMICILIARIA, COL_DOMICILIO, COL_DEPARTAMENTO, COL_PROVINCIA, COL_DISTRITO,
        COL_ABOGADO_DESIGNADO, COL_SUPERVISOR_RESPONSABLE
    };
    
    
    public JPanelFiltroBusqueda(){
        initComponents();
        initDatePickers();
        this.expedienteService = new ExpedienteService();
        this.catalogoService = new CatalogoService();
        this.catalogoItemService = new CatalogoItemService();
        this.plazoAtencionService = new PlazoAtencionService();
        this.estadosPorId = new HashMap<>();
        this.tiposSolicitudPorId = new HashMap<>();
        this.tiposDocumentoPorId = new HashMap<>();
        this.procedimientosPorId = new HashMap<>();
        this.tiposActaPorId = new HashMap<>();
        this.unidadesOrganicasPorId = new HashMap<>();
        this.direccionesDomiciliariasPorId = new HashMap<>();
        this.formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        this.formatoFecha.setLenient(false);
        cargarTiposBusqueda();
        cargarCatalogosListado();
        cargarComboEstados();    
        configurarComponentesAsignacion();
    }
          
    public void cargarTabla(JTable tabla) throws Exception {

        DefaultTableModel modelo = crearModeloTablaAsignacion();

        List<Expediente> lista = expedienteService.listarExpedientes();

        for (Expediente e : lista) {
            modelo.addRow(crearFilaTablaAsignacion(e, TIPO_FILA_PRINCIPAL, 0));
        }

        tabla.setModel(modelo);
    }
    
    private void cargarComboEstados() {
        cmbEstado.removeAllItems();
        //cmbEstado.addItem("TODOS");
        
        cmbEstado.addItem(new CatalogoItem(0, 0, "TODOS", 1));

        List<CatalogoItem> lista = catalogoItemService.obtenerEstadosTramite();

        for (CatalogoItem estado : lista) {
            estadosPorId.put(estado.getIdCatalogoItem(), estado.getDescripcion());
            cmbEstado.addItem(estado);
        }
    }
    
    private void cargarTiposBusqueda() {
        cmbTipoBusqueda.removeAllItems();

        cmbTipoBusqueda.addItem("NUMERO_TRAMITE_DOCUMENTO");
        cmbTipoBusqueda.addItem("NUMERO_DOCUMENTO");
        cmbTipoBusqueda.addItem("NUMERO_ACTA");
        cmbTipoBusqueda.addItem("TIPO_SOLICITUD");
        cmbTipoBusqueda.addItem("DNI_REMITENTE");
        cmbTipoBusqueda.addItem("APELLIDO_NOMBRE_REMITENTE");
        cmbTipoBusqueda.addItem("TIPO_PROCEDIMIENTO_REGISTRAL");
    }

    private void cargarCatalogosListado()
    {
        cargarMapaCatalogo(1, tiposSolicitudPorId);
        cargarMapaCatalogo(2, tiposDocumentoPorId);
        cargarMapaCatalogo(3, procedimientosPorId);
        cargarMapaCatalogo(4, tiposActaPorId);
        cargarMapaCatalogo(8, direccionesDomiciliariasPorId);
        cargarMapaCatalogo(9, unidadesOrganicasPorId);
    }

    private void cargarMapaCatalogo(int idCatalogo, Map<Integer, String> destino)
    {
        destino.clear();
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(idCatalogo);
        for (CatalogoItem item : lista) {
            destino.put(item.getIdCatalogoItem(), item.getDescripcion());
        }
    }
    
    private void buscarExpedientes() {
        try {
            if (rangoFechas != null && !rangoFechas.isValidRange()) {
                return;
            }

            String campo = cmbTipoBusqueda.getSelectedItem().toString();
            String valor = txtValorBusqueda.getText().trim();
            
            CatalogoItem estado = (CatalogoItem) cmbEstado.getSelectedItem();
            int idestado = estado.getIdCatalogoItem();           
            Date fechaDesde = rangoFechas == null ? null : DateRangePickerSupport.startOfDay(rangoFechas.getFromDate());
            Date fechaHasta = rangoFechas == null ? null : DateRangePickerSupport.endOfDay(rangoFechas.getToDate());
            
            List<Expediente> lista = expedienteService.buscar(campo, valor, idestado);

            cargarTablaNueva(filtrarPorRangoFechas(lista, fechaDesde, fechaHasta));

        } 
        catch (Exception e) {
        }
    }
    
    private void cargarTablaNueva(List<Expediente> lista) {
        
        DefaultTableModel model = crearModeloTablaAsignacion();
        model.setRowCount(0);
        documentosAsociadosPorNumero.clear();
        expedientesExpandidos.clear();
        List<Expediente> principales = agruparExpedientesPorNumero(lista);
        for (Expediente e : principales) {
            model.addRow(crearFilaTablaAsignacion(e, TIPO_FILA_PRINCIPAL, 0));
        }

        jTable1.setModel(model);
        configurarTablaAsignacion();
    }

    private DefaultTableModel crearModeloTablaAsignacion()
    {
        String[] columnas = {
                "", "", "ID expediente", "Fecha solicitud", "Canal", "Num. Expediente", "Tipo solicitud",
                "Proc. Reg", "Acta", "Titular", "Estado", "Días restantes", "EstadoId",
                "Tipo Doc.", "N° documento", "Tipo acta", "N° acta",
                "DNI titular 1", "Titular 1", "DNI titular 2", "Titular 2",
                "Unidad orgánica", "Correo electrónico", "Celular", "Dirección domiciliaria",
                "Domicilio", "Departamento", "Provincia", "Distrito",
                "Abogado designado", "Supervisor responsable", "Tipo fila", "Id padre"
        };
        return new DefaultTableModel(columnas, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == COL_SELECCION && TIPO_FILA_PRINCIPAL.equals(String.valueOf(getValueAt(row, COL_TIPO_FILA)));
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == COL_SELECCION ? Boolean.class : Object.class;
            }
        };
    }

    private Object[] crearFilaTablaAsignacion(Expediente e, String tipoFila, int idPadre)
    {
        boolean detalle = TIPO_FILA_DETALLE.equals(tipoFila);
        return new Object[] {
                false,
                detalle ? "└" : obtenerIndicadorExpandir(e),
                e.getIdExpediente(),
                formatearFecha(e.getFechaSolicitud()),
                textoSeguro(e.getCanalRecepcion()),
                obtenerNumeroExpedienteListado(e),
                obtenerDescripcionCatalogo(tiposSolicitudPorId, e.getTipoSolicitud()),
                obtenerDescripcionCatalogo(procedimientosPorId, e.getTipoProcedimientoRegistral()),
                obtenerActa(e),
                obtenerTitularListado(e),
                obtenerDescripcionEstado(e.getEstado()),
                detalle ? "" : plazoAtencionService.calcular(e.getTipoDocumento(), e.getFechaSolicitud()),
                e.getEstado(),
                obtenerDescripcionCatalogo(tiposDocumentoPorId, e.getTipoDocumento()),
                textoSeguro(e.getNumeroDocumento()),
                obtenerDescripcionCatalogo(tiposActaPorId, e.getTipoActa()),
                textoSeguro(e.getNumeroActa()),
                textoSeguro(e.getDniTitular()),
                textoSeguro(e.getApellidoNombreTitular()),
                textoSeguro(e.getDniTitular2()),
                textoSeguro(e.getApellidoNombreTitular2()),
                obtenerDescripcionCatalogo(unidadesOrganicasPorId, e.getUnidadOrganica()),
                textoSeguro(e.getCorreoElectronico()),
                textoSeguro(e.getCelular()),
                obtenerDescripcionCatalogo(direccionesDomiciliariasPorId, e.getDireccionDomiciliaria()),
                textoSeguro(e.getDomicilio()),
                idComoTexto(e.getDepartamento()),
                idComoTexto(e.getProvincia()),
                idComoTexto(e.getDistrito()),
                textoSeguro(e.getAbogadoDesignado()),
                textoSeguro(e.getSupervisorDesignado()),
                tipoFila,
                idPadre
        };
    }

    private List<Expediente> agruparExpedientesPorNumero(List<Expediente> lista)
    {
        Map<String, List<Expediente>> grupos = new LinkedHashMap<>();
        List<Expediente> sinNumero = new ArrayList<>();
        for (Expediente expediente : lista) {
            String numero = textoSeguro(expediente.getNumExpediente()).trim();
            if (numero.isEmpty()) {
                sinNumero.add(expediente);
            } else {
                grupos.computeIfAbsent(numero, key -> new ArrayList<>()).add(expediente);
            }
        }

        List<Expediente> principales = new ArrayList<>();
        for (Map.Entry<String, List<Expediente>> entry : grupos.entrySet()) {
            List<Expediente> grupo = entry.getValue();
            grupo.sort(this::compararExpedientePrincipal);
            Expediente principal = grupo.get(0);
            principales.add(principal);
            if (grupo.size() > 1) {
                documentosAsociadosPorNumero.put(entry.getKey(), new ArrayList<>(grupo.subList(1, grupo.size())));
            }
        }
        principales.addAll(sinNumero);
        return principales;
    }

    private int compararExpedientePrincipal(Expediente left, Expediente right)
    {
        int fecha = compararFechaExpediente(left.getFechaSolicitud(), right.getFechaSolicitud());
        if (fecha != 0) {
            return fecha;
        }
        return Integer.compare(left.getIdExpediente(), right.getIdExpediente());
    }

    private int compararFechaExpediente(java.util.Date left, java.util.Date right)
    {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return left.compareTo(right);
    }

    private String obtenerIndicadorExpandir(Expediente expediente)
    {
        String numero = textoSeguro(expediente.getNumExpediente()).trim();
        List<Expediente> asociados = documentosAsociadosPorNumero.get(numero);
        if (asociados == null || asociados.isEmpty()) {
            return "";
        }
        return expedientesExpandidos.contains(expediente.getIdExpediente()) ? "-" : "+";
    }
    private String obtenerDescripcionEstado(int idEstado)
    {
        return estadosPorId.getOrDefault(idEstado, String.valueOf(idEstado));
    }

    private String formatearFecha(java.util.Date fecha)
    {
        return fecha == null ? "" : formatoFecha.format(fecha);
    }

    private String obtenerDescripcionCatalogo(Map<Integer, String> catalogo, int id)
    {
        if (id <= 0) {
            return "";
        }
        return catalogo.getOrDefault(id, String.valueOf(id));
    }

    private String idComoTexto(int id)
    {
        return id <= 0 ? "" : String.valueOf(id);
    }

    private String obtenerNumeroExpedienteListado(Expediente expediente)
    {
        if (!estaVacio(expediente.getNumExpediente())) {
            return expediente.getNumExpediente().trim();
        }
        return "Sin expediente";
    }

    private String obtenerActa(Expediente expediente)
    {
        String tipoActa = obtenerDescripcionCatalogo(tiposActaPorId, expediente.getTipoActa());
        String numeroActa = textoSeguro(expediente.getNumeroActa()).trim();
        if (!tipoActa.isEmpty() && !numeroActa.isEmpty()) {
            return tipoActa + " " + numeroActa;
        }
        if (!tipoActa.isEmpty()) {
            return tipoActa;
        }
        if (!numeroActa.isEmpty()) {
            return numeroActa;
        }
        return "";
    }

    private TitularListadoValue obtenerTitularListado(Expediente expediente)
    {
        String titular1 = textoSeguro(expediente.getApellidoNombreTitular()).trim();
        String titular2 = textoSeguro(expediente.getApellidoNombreTitular2()).trim();
        if (esActaMatrimonio(expediente)) {
            if (!titular2.isEmpty()) {
                return new TitularListadoValue(
                        unirTitulares(titular1, titular2),
                        "<html>Titular 1: " + escaparHtml(titular1) + "<br>Titular 2: " + escaparHtml(titular2) + "</html>");
            }
            return new TitularListadoValue(titular1, "Acta de matrimonio sin segundo titular registrado.");
        }
        return new TitularListadoValue(titular1, "Titular: " + titular1);
    }

    private String unirTitulares(String titular1, String titular2)
    {
        if (titular1.isEmpty()) {
            return titular2;
        }
        return titular1 + " / " + titular2;
    }

    private boolean esActaMatrimonio(Expediente expediente)
    {
        String tipoActa = obtenerDescripcionCatalogo(tiposActaPorId, expediente.getTipoActa());
        return "MATRIMONIO".equals(tipoActa.trim().toUpperCase(Locale.ROOT));
    }

    private boolean estaVacio(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private List<Expediente> filtrarPorRangoFechas(List<Expediente> lista, Date fechaDesde, Date fechaHasta)
    {
        if (fechaDesde == null && fechaHasta == null) {
            return lista;
        }

        List<Expediente> filtrada = new ArrayList<>();
        for (Expediente expediente : lista) {
            Date fechaSolicitud = expediente.getFechaSolicitud();
            if (fechaSolicitud == null) {
                continue;
            }
            if (fechaDesde != null && fechaSolicitud.before(fechaDesde)) {
                continue;
            }
            if (fechaHasta != null && fechaSolicitud.after(fechaHasta)) {
                continue;
            }
            filtrada.add(expediente);
        }
        return filtrada;
    }

    private void initDatePickers()
    {
        lblFeedbackFechas = new JLabel(" ");
        lblFeedbackFechas.setForeground(new Color(198, 40, 40));
        pnlFechas.add(lblFeedbackFechas, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 42, 340, 20));

        DateRangePickerSupport.replaceTextFieldsDeferred(
            txtFechaDesde,
            txtFechaHasta,
            pnlFechas,
            lblFeedbackFechas,
            new DateRangePickerSupport.RangeConsumer() {
                @Override
                public void accept(DateRangePickerSupport.Range range) {
                    rangoFechas = range;
                    rangoFechas.getFromPicker().setDate(DateRangePickerSupport.defaultSearchFromDate());
                    rangoFechas.getToPicker().setDate(DateRangePickerSupport.defaultSearchToDate());
                    configurarVistaAsignacion();
                    buscarExpedientes();
                }
            }
        );
    }

    private void configurarComponentesAsignacion()
    {
        setBackground(new Color(245, 247, 250));
        jPanel1.setBackground(new Color(245, 247, 250));

        btnBuscar.setText("Buscar");
        btnLimpiar.setText("Limpiar");
        IconUtils.applyIcon(btnBuscar, "search.svg");
        IconUtils.applyIcon(btnLimpiar, "clear.svg");

        btnBuscar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLimpiar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBuscar.setToolTipText("Buscar expedientes pendientes de asignación.");
        btnLimpiar.setToolTipText("Limpiar filtros.");
        txtValorBusqueda.setToolTipText("Ingrese el valor a buscar.");
        cmbTipoBusqueda.setToolTipText("Seleccione el tipo de búsqueda.");
        cmbEstado.setToolTipText("Seleccione el estado del trámite.");

        estilizarBoton(btnBuscar, true);
        estilizarBoton(btnLimpiar, false);

        cmbTipoBusqueda.setRenderer(new TipoBusquedaRenderer());
        cmbEstado.setRenderer(new ComboTooltipRenderer());
        actualizarTooltipTipoBusqueda();
        configurarTablaAsignacion();
        configurarVistaAsignacion();
    }

    private void configurarVistaAsignacion()
    {
        remove(jPanel1);
        setLayout(new BorderLayout());

        jPanel1.removeAll();
        jPanel1.setLayout(new BorderLayout(0, 16));
        jPanel1.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        jPanel1.setBackground(new Color(245, 247, 250));

        jPanel1.add(crearHeaderAsignacion(), BorderLayout.NORTH);
        jPanel1.add(crearContenidoAsignacion(), BorderLayout.CENTER);

        add(jPanel1, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel crearHeaderAsignacion()
    {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titulo = new JLabel("Asignación de expedientes");
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        titulo.setForeground(new Color(25, 42, 62));

        JLabel subtitulo = new JLabel("Consulte expedientes registrados y seleccione uno para asignarlo al equipo correspondiente.");
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(93, 105, 119));

        JPanel textos = new JPanel(new GridBagLayout());
        textos.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        textos.add(titulo, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 0, 0);
        textos.add(subtitulo, gbc);

        header.add(textos, BorderLayout.WEST);
        return header;
    }

    private JPanel crearContenidoAsignacion()
    {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.add(crearCardFiltros(), BorderLayout.NORTH);
        content.add(crearCardResultados(), BorderLayout.CENTER);
        return content;
    }

    private JPanel crearCardFiltros()
    {
        JPanel card = crearCard();
        card.setLayout(new GridBagLayout());
        dimensionarFiltros();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 6, 12);

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.22;
        gbc.insets = new Insets(0, 0, 6, 12);
        card.add(crearLabelFiltro("Tipo de búsqueda"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.32;
        card.add(crearLabelFiltro("Valor de búsqueda"), gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.18;
        card.add(crearLabelFiltro("Estado del trámite"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.28;
        card.add(new JLabel(" "), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.22;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(cmbTipoBusqueda, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.32;
        card.add(txtValorBusqueda, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.18;
        gbc.fill = GridBagConstraints.NONE;
        card.add(cmbEstado, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.22;
        gbc.insets = new Insets(12, 0, 6, 12);
        card.add(crearLabelFiltro("Fecha desde"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.22;
        card.add(crearLabelFiltro("Fecha hasta"), gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.weightx = 0.22;
        gbc.insets = new Insets(0, 0, 0, 12);
        card.add(obtenerComponenteFechaDesde(), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.22;
        card.add(obtenerComponenteFechaHasta(), gbc);

        JPanel acciones = new JPanel(new GridBagLayout());
        acciones.setOpaque(false);
        GridBagConstraints gbcAccion = new GridBagConstraints();
        gbcAccion.insets = new Insets(0, 0, 0, 8);
        acciones.add(btnBuscar, gbcAccion);
        acciones.add(crearBotonExportarExcel(), gbcAccion);
        acciones.add(crearBotonAsociarSolicitudes(), gbcAccion);
        gbcAccion.insets = new Insets(0, 0, 0, 0);
        acciones.add(btnLimpiar, gbcAccion);

        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0.56;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        card.add(acciones, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 0, 0);
        card.add(lblFeedbackFechas, gbc);

        return card;
    }

    private JButton crearBotonExportarExcel()
    {
        JButton button = IconUtils.createSecondaryButton("Exportar", "excel.svg");
        button.setToolTipText("Exportar listado a Excel");
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(118, 36));
        button.addActionListener(e -> exportarListadoAsignacionExcel());
        return button;
    }

    private JButton crearBotonAsociarSolicitudes()
    {
        JButton button = IconUtils.createSecondaryButton("Asociar", "assignment.svg");
        button.setToolTipText("Asociar solicitudes sin número de expediente");
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(112, 36));
        button.addActionListener(e -> asociarSolicitudesSeleccionadas());
        return button;
    }

    private void asociarSolicitudesSeleccionadas()
    {
        try {
            List<Integer> filas = obtenerFilasSeleccionadasParaAsociar();
            if (filas.size() < 2) {
                JOptionPane.showMessageDialog(this, "Seleccione una solicitud principal y al menos un documento sin número de expediente.");
                return;
            }

            Set<String> numerosExpediente = new HashSet<>();
            List<Integer> filasSinNumero = new ArrayList<>();
            for (Integer fila : filas) {
                String numero = numeroExpedienteReal(fila);
                if (numero.isEmpty()) {
                    filasSinNumero.add(fila);
                } else {
                    numerosExpediente.add(numero);
                }
            }

            if (numerosExpediente.size() > 1) {
                JOptionPane.showMessageDialog(this, "No se puede asociar solicitudes con diferentes números de expediente.");
                return;
            }
            if (numerosExpediente.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar una solicitud principal con número de expediente.");
                return;
            }
            if (filasSinNumero.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seleccione al menos un documento sin número de expediente para asociar.");
                return;
            }

            String numeroPrincipal = numerosExpediente.iterator().next();
            int filaPrincipal = buscarFilaPrincipalSeleccionada(filas, numeroPrincipal);
            if (filaPrincipal < 0) {
                JOptionPane.showMessageDialog(this, "No se pudo identificar la solicitud principal seleccionada.");
                return;
            }
            int idExpedientePrincipal = parseIntSeguro(jTable1.getModel().getValueAt(filaPrincipal, COL_ID));
            String abogadoPrincipal = textoSeguro(jTable1.getModel().getValueAt(filaPrincipal, COL_ABOGADO_DESIGNADO)).trim();
            if (idExpedientePrincipal <= 0 || abogadoPrincipal.isEmpty()) {
                JOptionPane.showMessageDialog(this, "La solicitud principal no tiene abogado asignado.");
                return;
            }
            if (!validarCoincidenciaActaTitular(filaPrincipal, filasSinNumero)) {
                return;
            }

            List<Integer> idsAsociar = new ArrayList<>();
            for (Integer fila : filasSinNumero) {
                idsAsociar.add(parseIntSeguro(jTable1.getModel().getValueAt(fila, COL_ID)));
            }

            int actualizados = expedienteService.asociarNumeroExpediente(idsAsociar, numeroPrincipal, idExpedientePrincipal);
            JOptionPane.showMessageDialog(this, "Asociación finalizada: " + actualizados + " documento(s) asociado(s).");
            buscarExpedientes();
        } catch (Exception ex) {
            Logger.getLogger(JPanelFiltroBusqueda.class.getName()).log(Level.SEVERE, null, ex);
            String mensaje = "La solicitud principal no tiene abogado asignado.".equals(ex.getMessage())
                    ? ex.getMessage()
                    : "No se pudo asociar las solicitudes seleccionadas.";
            JOptionPane.showMessageDialog(this, mensaje);
        }
    }

    private List<Integer> obtenerFilasSeleccionadasParaAsociar()
    {
        List<Integer> filas = new ArrayList<>();
        for (int row = 0; row < jTable1.getModel().getRowCount(); row++) {
            Object seleccionado = jTable1.getModel().getValueAt(row, COL_SELECCION);
            Object tipoFila = jTable1.getModel().getValueAt(row, COL_TIPO_FILA);
            if (Boolean.TRUE.equals(seleccionado) && TIPO_FILA_PRINCIPAL.equals(String.valueOf(tipoFila))) {
                filas.add(row);
            }
        }
        return filas;
    }

    private String numeroExpedienteReal(int modelRow)
    {
        String numero = textoSeguro(jTable1.getModel().getValueAt(modelRow, COL_REFERENCIA)).trim();
        return "Sin expediente".equalsIgnoreCase(numero) ? "" : numero;
    }

    private int buscarFilaPrincipalSeleccionada(List<Integer> filas, String numeroPrincipal)
    {
        for (Integer fila : filas) {
            if (numeroPrincipal.equals(numeroExpedienteReal(fila))) {
                return fila;
            }
        }
        return -1;
    }

    private boolean validarCoincidenciaActaTitular(int filaPrincipal, List<Integer> filasSinNumero)
    {
        String actaPrincipal = normalizarComparacion(jTable1.getModel().getValueAt(filaPrincipal, COL_NUMERO_ACTA));
        String titularPrincipal1 = normalizarComparacion(jTable1.getModel().getValueAt(filaPrincipal, COL_TITULAR_1));
        String titularPrincipal2 = normalizarComparacion(jTable1.getModel().getValueAt(filaPrincipal, COL_TITULAR_2));

        for (Integer fila : filasSinNumero) {
            String acta = normalizarComparacion(jTable1.getModel().getValueAt(fila, COL_NUMERO_ACTA));
            String titular1 = normalizarComparacion(jTable1.getModel().getValueAt(fila, COL_TITULAR_1));
            String titular2 = normalizarComparacion(jTable1.getModel().getValueAt(fila, COL_TITULAR_2));
            if (actaPrincipal.isEmpty() || !actaPrincipal.equals(acta)
                    || !coincideAlguno(titularPrincipal1, titularPrincipal2, titular1, titular2)) {
                JOptionPane.showMessageDialog(this, "Solo se pueden asociar documentos sin número de expediente con el mismo número de acta y nombre del titular.");
                return false;
            }
        }
        return true;
    }

    private boolean coincideAlguno(String principal1, String principal2, String titular1, String titular2)
    {
        return coincideTitular(principal1, titular1)
                || coincideTitular(principal1, titular2)
                || coincideTitular(principal2, titular1)
                || coincideTitular(principal2, titular2);
    }

    private boolean coincideTitular(String left, String right)
    {
        return !left.isEmpty() && left.equals(right);
    }

    private String normalizarComparacion(Object value)
    {
        String texto = Normalizer.normalize(textoSeguro(value).trim().toUpperCase(Locale.ROOT), Normalizer.Form.NFD);
        texto = texto.replaceAll("\\p{M}", "");
        return texto.replaceAll("\\s+", " ");
    }

    private void exportarListadoAsignacionExcel()
    {
        List<Integer> filasExportacion = obtenerFilasExportacion();
        if (filasExportacion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay registros para exportar.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exportar listado a Excel");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivo Excel (*.xlsx)", "xlsx"));
        fileChooser.setSelectedFile(new File(nombreArchivoExcelAsignacion()));

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = asegurarExtensionXlsx(fileChooser.getSelectedFile());
        if (archivo.exists()) {
            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "El archivo ya existe. ¿Desea reemplazarlo?",
                    "Confirmar reemplazo",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            escribirExcelAsignacion(archivo, filasExportacion);
            JOptionPane.showMessageDialog(this, "Archivo Excel generado correctamente.");
        } catch (IOException ex) {
            Logger.getLogger(JPanelFiltroBusqueda.class.getName()).log(Level.WARNING, "No se pudo exportar listado de asignacion", ex);
            JOptionPane.showMessageDialog(this, "No se pudo exportar el archivo Excel.");
        }
    }

    private String nombreArchivoExcelAsignacion()
    {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "asignacion_solicitudes_" + timestamp + ".xlsx";
    }

    private File asegurarExtensionXlsx(File archivo)
    {
        if (archivo.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            return archivo;
        }
        return new File(archivo.getParentFile(), archivo.getName() + ".xlsx");
    }

    private List<Integer> obtenerFilasExportacion()
    {
        List<Integer> filas = new ArrayList<>();
        if (paginationHelper != null) {
            for (Integer modelRow : paginationHelper.getFilteredModelRowsInSortOrder()) {
                if (!esFilaDetalleModel(modelRow)) {
                    filas.add(modelRow);
                }
            }
            return filas;
        }
        for (int viewRow = 0; viewRow < jTable1.getRowCount(); viewRow++) {
            int modelRow = jTable1.convertRowIndexToModel(viewRow);
            if (!esFilaDetalleModel(modelRow)) {
                filas.add(modelRow);
            }
        }
        return filas;
    }

    private void escribirExcelAsignacion(File archivo, List<Integer> filasExportacion) throws IOException
    {
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(archivo)) {
            Sheet sheet = workbook.createSheet("Solicitudes");
            CellStyle headerStyle = crearEstiloCabeceraExcel(workbook);
            CellStyle dateStyle = crearEstiloFechaExcel(workbook);

            Row header = sheet.createRow(0);
            for (int col = 0; col < COLUMNAS_EXPORTACION_EXCEL.length; col++) {
                int modelColumn = COLUMNAS_EXPORTACION_EXCEL[col];
                Cell cell = header.createCell(col);
                cell.setCellValue(jTable1.getModel().getColumnName(modelColumn));
                cell.setCellStyle(headerStyle);
            }

            for (int index = 0; index < filasExportacion.size(); index++) {
                int modelRow = filasExportacion.get(index);
                Row row = sheet.createRow(index + 1);
                for (int col = 0; col < COLUMNAS_EXPORTACION_EXCEL.length; col++) {
                    int modelColumn = COLUMNAS_EXPORTACION_EXCEL[col];
                    Object value = obtenerValorTablaExportacion(modelRow, modelColumn);
                    Cell cell = row.createCell(col);
                    escribirValorExcel(cell, value, modelColumn, dateStyle);
                }
            }

            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new CellRangeAddress(0, filasExportacion.size(), 0, COLUMNAS_EXPORTACION_EXCEL.length - 1));
            for (int col = 0; col < COLUMNAS_EXPORTACION_EXCEL.length; col++) {
                sheet.autoSizeColumn(col);
                int width = sheet.getColumnWidth(col);
                sheet.setColumnWidth(col, Math.min(Math.max(width + 512, 2800), 18000));
            }
            workbook.write(out);
        }
    }

    private CellStyle crearEstiloCabeceraExcel(Workbook workbook)
    {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        aplicarBordesExcel(style);
        return style;
    }

    private CellStyle crearEstiloFechaExcel(Workbook workbook)
    {
        CellStyle style = workbook.createCellStyle();
        CreationHelper helper = workbook.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("dd/MM/yyyy"));
        aplicarBordesExcel(style);
        return style;
    }

    private void aplicarBordesExcel(CellStyle style)
    {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private Object obtenerValorTablaExportacion(int modelRow, int modelColumn)
    {
        return jTable1.getModel().getValueAt(modelRow, modelColumn);
    }

    private void escribirValorExcel(Cell cell, Object value, int modelColumn, CellStyle dateStyle)
    {
        if (modelColumn == COL_FECHA_SOLICITUD) {
            Date fecha = parsearFechaTabla(value);
            if (fecha != null) {
                cell.setCellValue(fecha);
                cell.setCellStyle(dateStyle);
                return;
            }
        }
        cell.setCellValue(textoSeguro(value));
    }

    private JPanel crearCardResultados()
    {
        JPanel card = crearCard();
        card.setLayout(new BorderLayout(0, 10));

        JLabel titulo = new JLabel("Listado de expedientes para asignación");
        titulo.setFont(new Font("Arial", Font.BOLD, 15));
        titulo.setForeground(new Color(25, 42, 62));

        JPanel superior = new JPanel(new BorderLayout(0, 8));
        superior.setOpaque(false);
        superior.add(titulo, BorderLayout.NORTH);
        superior.add(crearPanelFiltrosPorColumnaAsignacion(), BorderLayout.CENTER);

        jScrollPane1.setViewportView(jTable1);
        jScrollPane1.setBorder(BorderFactory.createLineBorder(new Color(218, 224, 231)));
        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        card.add(superior, BorderLayout.NORTH);
        card.add(jScrollPane1, BorderLayout.CENTER);
        if (panelPaginacion == null) {
            panelPaginacion = new JPanel(new BorderLayout());
            panelPaginacion.setOpaque(false);
        }
        card.add(panelPaginacion, BorderLayout.SOUTH);
        actualizarPanelPaginacion();
        return card;
    }

    private void actualizarPanelPaginacion()
    {
        if (panelPaginacion == null || paginationHelper == null) {
            return;
        }
        panelPaginacion.removeAll();
        panelPaginacion.add(paginationHelper.getPanel(), BorderLayout.CENTER);
        panelPaginacion.revalidate();
        panelPaginacion.repaint();
    }


    private void alternarDocumentosAsociados(int viewRow)
    {
        int modelRow = jTable1.convertRowIndexToModel(viewRow);
        if (esFilaDetalleModel(modelRow)) {
            return;
        }
        int idExpediente = parseIntSeguro(jTable1.getModel().getValueAt(modelRow, COL_ID));
        String numeroExpediente = textoSeguro(jTable1.getModel().getValueAt(modelRow, COL_REFERENCIA)).trim();
        List<Expediente> asociados = documentosAsociadosPorNumero.get(numeroExpediente);
        if (idExpediente <= 0 || asociados == null || asociados.isEmpty()) {
            return;
        }

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        if (expedientesExpandidos.contains(idExpediente)) {
            expedientesExpandidos.remove(idExpediente);
            quitarFilasDetalle(idExpediente);
            model.setValueAt("+", modelRow, COL_EXPANDIR);
        } else {
            expedientesExpandidos.add(idExpediente);
            model.setValueAt("-", modelRow, COL_EXPANDIR);
            int insertIndex = modelRow + 1;
            for (Expediente asociado : asociados) {
                model.insertRow(insertIndex++, crearFilaTablaAsignacion(asociado, TIPO_FILA_DETALLE, idExpediente));
            }
        }
        if (paginationHelper != null) {
            paginationHelper.refresh(false);
        }
        actualizarAlturasFilasDetalle();
        jTable1.repaint();
    }

    private void quitarFilasDetalle(int idPadre)
    {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        for (int row = model.getRowCount() - 1; row >= 0; row--) {
            if (esFilaDetalleModel(row) && parseIntSeguro(model.getValueAt(row, COL_ID_PADRE)) == idPadre) {
                model.removeRow(row);
            }
        }
    }

    private void colapsarDocumentosAsociados()
    {
        if (!(jTable1.getModel() instanceof DefaultTableModel) || expedientesExpandidos.isEmpty()) {
            return;
        }
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        for (int row = model.getRowCount() - 1; row >= 0; row--) {
            if (esFilaDetalleModel(row)) {
                model.removeRow(row);
            }
        }
        for (int row = 0; row < model.getRowCount(); row++) {
            if (!esFilaDetalleModel(row) && "+".equals(obtenerIndicadorExpandirPorFila(row))) {
                model.setValueAt("+", row, COL_EXPANDIR);
            }
        }
        expedientesExpandidos.clear();
        if (paginationHelper != null) {
            paginationHelper.refresh(false);
        }
    }

    private String obtenerIndicadorExpandirPorFila(int modelRow)
    {
        if (jTable1.getModel().getColumnCount() <= COL_REFERENCIA) {
            return "";
        }
        String numeroExpediente = textoSeguro(jTable1.getModel().getValueAt(modelRow, COL_REFERENCIA)).trim();
        List<Expediente> asociados = documentosAsociadosPorNumero.get(numeroExpediente);
        return asociados == null || asociados.isEmpty() ? "" : "+";
    }

    private void actualizarAlturasFilasDetalle()
    {
        for (int viewRow = 0; viewRow < jTable1.getRowCount(); viewRow++) {
            int modelRow = jTable1.convertRowIndexToModel(viewRow);
            int height = esFilaDetalleModel(modelRow) ? 28 : 30;
            if (jTable1.getRowHeight(viewRow) != height) {
                jTable1.setRowHeight(viewRow, height);
            }
        }
    }

    private boolean esFilaDetalleModel(int modelRow)
    {
        return jTable1.getModel().getColumnCount() > COL_TIPO_FILA
                && TIPO_FILA_DETALLE.equals(jTable1.getModel().getValueAt(modelRow, COL_TIPO_FILA));
    }

    private boolean esFilaDetalleView(JTable table, int viewRow)
    {
        if (viewRow < 0 || table.getModel().getColumnCount() <= COL_TIPO_FILA) {
            return false;
        }
        return TIPO_FILA_DETALLE.equals(table.getModel().getValueAt(table.convertRowIndexToModel(viewRow), COL_TIPO_FILA));
    }

    private boolean esFilaPrincipalConAsociadosModel(int modelRow)
    {
        if (modelRow < 0 || jTable1.getModel().getColumnCount() <= COL_REFERENCIA || esFilaDetalleModel(modelRow)) {
            return false;
        }
        String numeroExpediente = textoSeguro(jTable1.getModel().getValueAt(modelRow, COL_REFERENCIA)).trim();
        List<Expediente> asociados = documentosAsociadosPorNumero.get(numeroExpediente);
        return asociados != null && !asociados.isEmpty();
    }

    private Color obtenerColorFondoAsociacion(int viewRow, int modelRow)
    {
        if (esFilaDetalleModel(modelRow)) {
            return new Color(240, 253, 250);
        }
        if (esFilaPrincipalConAsociadosModel(modelRow)) {
            return new Color(220, 252, 231);
        }
        return viewRow % 2 == 0 ? Color.WHITE : new Color(248, 250, 252);
    }

    private String obtenerTooltipAsociacion(int modelRow)
    {
        if (esFilaDetalleModel(modelRow)) {
            return "Documento asociado al expediente principal.";
        }
        if (esFilaPrincipalConAsociadosModel(modelRow)) {
            return "Solicitud principal con documentos asociados.";
        }
        return null;
    }

    private JPanel crearPanelFiltrosPorColumnaAsignacion()
    {
        configurarFiltrosPorColumnaAsignacion();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        agregarFiltroColumna(panel, "Plazo", filtrosTextoPorColumna.get(COL_DIAS_RESTANTES), 0, 0.70, 70);
        agregarFiltroColumna(panel, "Fecha", filtroFechaSolicitudColumna, 1, 0.82, 96);
        agregarFiltroColumna(panel, "Num. Expediente", filtrosTextoPorColumna.get(COL_REFERENCIA), 2, 1.35, 170);
        agregarFiltroColumna(panel, "Proc. Reg", filtrosTextoPorColumna.get(COL_PROCEDIMIENTO_REGISTRAL), 3, 1.05);
        agregarFiltroColumna(panel, "Tipo Doc.", filtrosTextoPorColumna.get(COL_TIPO_DOCUMENTO), 4, 1.05);
        agregarFiltroColumna(panel, "Acta", filtrosTextoPorColumna.get(COL_ACTA), 5, 0.95);
        agregarFiltroColumna(panel, "Titular", filtrosTextoPorColumna.get(COL_TITULAR), 6, 1.75);
        agregarFiltroColumna(panel, "Estado", filtrosTextoPorColumna.get(COL_ESTADO), 7, 0.90);

        JButton btnLimpiarFiltros = crearBotonLimpiarFiltrosPorColumna();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 6, 0, 0);
        gbc.fill = GridBagConstraints.NONE;
        panel.add(btnLimpiarFiltros, gbc);
        return panel;
    }

    private void agregarFiltroColumna(JPanel panel, String etiqueta, JComponent filtro, int columna, double peso)
    {
        agregarFiltroColumna(panel, etiqueta, filtro, columna, peso, 80);
    }

    private void agregarFiltroColumna(JPanel panel, String etiqueta, JComponent filtro, int columna, double peso, int anchoPreferido)
    {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = columna;
        gbc.gridy = 0;
        gbc.weightx = peso;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 3, 6);
        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("Arial", Font.PLAIN, 10));
        label.setForeground(new Color(100, 116, 139));
        panel.add(label, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 6);
        filtro.setPreferredSize(new Dimension(anchoPreferido, 28));
        filtro.setMinimumSize(new Dimension(anchoPreferido, 28));
        panel.add(filtro, gbc);
    }

    private JButton crearBotonLimpiarFiltrosPorColumna()
    {
        JButton button = IconUtils.createIconButton("Limpiar filtros de columna", "broom.svg");
        button.setText("");
        button.setPreferredSize(new Dimension(30, 28));
        button.setMinimumSize(new Dimension(30, 28));
        button.setMaximumSize(new Dimension(30, 28));
        button.setFocusPainted(false);
        button.setIconTextGap(0);
        button.addActionListener(e -> limpiarFiltrosPorColumna());
        return button;
    }

    private void configurarFiltrosPorColumnaAsignacion()
    {
        if (filtrosPorColumnaConfigurados) {
            return;
        }
        filtrosPorColumnaConfigurados = true;

        filtroFechaSolicitudColumna.setDateFormatString("dd/MM/yyyy");
        filtroFechaSolicitudColumna.setToolTipText("Filtrar por fecha de solicitud");
        filtroFechaSolicitudColumna.getDateEditor().getUiComponent().setToolTipText("Filtrar por fecha de solicitud");
        filtroFechaSolicitudColumna.addPropertyChangeListener("date", evt -> aplicarFiltrosPorColumna());

        crearFiltroTextoColumna(COL_CANAL, "Filtrar canal");
        crearFiltroTextoColumna(COL_REFERENCIA, "Filtrar referencia");
        crearFiltroTextoColumna(COL_TIPO_SOLICITUD, "Filtrar tipo de solicitud");
        crearFiltroTextoColumna(COL_PROCEDIMIENTO_REGISTRAL, "Filtrar procedimiento registral");
        crearFiltroTextoColumna(COL_TIPO_DOCUMENTO, "Filtrar tipo documento");
        crearFiltroTextoColumna(COL_ACTA, "Filtrar acta");
        crearFiltroTextoColumna(COL_TITULAR, "Filtrar titular");
        crearFiltroTextoColumna(COL_ESTADO, "Filtrar estado");
        crearFiltroTextoColumna(COL_DIAS_RESTANTES, "Filtrar días restantes");
    }

    private void crearFiltroTextoColumna(int columna, String tooltip)
    {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 11));
        field.setToolTipText(tooltip);
        field.putClientProperty("JTextField.placeholderText", "Buscar");
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltrosPorColumna();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltrosPorColumna();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltrosPorColumna();
            }
        });
        filtrosTextoPorColumna.put(columna, field);
    }

    private void aplicarFiltrosPorColumna()
    {
        if (!(jTable1.getRowSorter() instanceof TableRowSorter)) {
            return;
        }

        List<RowFilter<DefaultTableModel, Integer>> filtros = new ArrayList<>();
        Date fechaFiltro = filtroFechaSolicitudColumna.getDate();
        if (fechaFiltro != null) {
            filtros.add(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    return mismaFecha(fechaFiltro, parsearFechaTabla(entry.getValue(COL_FECHA_SOLICITUD)));
                }
            });
        }

        for (Map.Entry<Integer, JTextField> filtro : filtrosTextoPorColumna.entrySet()) {
            String criterio = normalizarFiltro(filtro.getValue().getText());
            if (criterio.isEmpty()) {
                continue;
            }
            int columna = filtro.getKey();
            filtros.add(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    return normalizarFiltro(textoSeguro(entry.getValue(columna))).contains(criterio);
                }
            });
        }

        RowFilter<DefaultTableModel, Integer> filtroBase = filtros.isEmpty() ? null : RowFilter.andFilter(filtros);
        if (paginationHelper != null) {
            paginationHelper.setBaseFilter(filtroBase);
        } else {
            @SuppressWarnings({"rawtypes", "unchecked"})
            TableRowSorter sorter = (TableRowSorter) jTable1.getRowSorter();
            sorter.setRowFilter(filtroBase);
        }
    }

    private void limpiarFiltrosPorColumna()
    {
        if (!filtrosPorColumnaConfigurados) {
            return;
        }
        filtroFechaSolicitudColumna.setDate(null);
        for (JTextField filtro : filtrosTextoPorColumna.values()) {
            filtro.setText("");
        }
        aplicarFiltrosPorColumna();
    }

    private String normalizarFiltro(String value)
    {
        return textoSeguro(value).trim().toUpperCase(Locale.ROOT);
    }

    private Date parsearFechaTabla(Object value)
    {
        String texto = textoSeguro(value).trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            synchronized (formatoFecha) {
                return formatoFecha.parse(texto);
            }
        } catch (ParseException ex) {
            return null;
        }
    }

    private boolean mismaFecha(Date left, Date right)
    {
        if (left == null || right == null) {
            return false;
        }
        synchronized (formatoFecha) {
            return formatoFecha.format(left).equals(formatoFecha.format(right));
        }
    }

    private JPanel crearCard()
    {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        return card;
    }

    private JLabel crearLabelFiltro(String texto)
    {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(71, 85, 105));
        return label;
    }

    private void dimensionarFiltros()
    {
        cmbTipoBusqueda.setPreferredSize(new Dimension(230, 36));
        dimensionarComponenteFijo(cmbEstado, 180, 36);
        txtValorBusqueda.setPreferredSize(new Dimension(260, 36));
        dimensionarComponenteFecha(obtenerComponenteFechaDesde());
        dimensionarComponenteFecha(obtenerComponenteFechaHasta());
        btnBuscar.setPreferredSize(new Dimension(116, 36));
        btnLimpiar.setPreferredSize(new Dimension(116, 36));
    }

    private void dimensionarComponenteFecha(JComponent component)
    {
        dimensionarComponenteFijo(component, 170, 36);
    }

    private void dimensionarComponenteFijo(JComponent component, int width, int height)
    {
        Dimension dimension = new Dimension(width, height);
        component.setPreferredSize(dimension);
        component.setMinimumSize(dimension);
        component.setMaximumSize(dimension);
    }

    private void estilizarBoton(javax.swing.JButton button, boolean primary)
    {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        if (primary) {
            button.setBackground(new Color(37, 99, 160));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(241, 245, 249));
            button.setForeground(new Color(51, 65, 85));
        }
    }

    private JComponent obtenerComponenteFechaDesde()
    {
        return rangoFechas == null ? txtFechaDesde : rangoFechas.getFromPicker();
    }

    private JComponent obtenerComponenteFechaHasta()
    {
        return rangoFechas == null ? txtFechaHasta : rangoFechas.getToPicker();
    }

    private void configurarTablaAsignacion()
    {
        jTable1.setRowHeight(30);
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable1.setFillsViewportHeight(true);
        jTable1.setShowGrid(false);
        jTable1.setIntercellSpacing(new Dimension(0, 0));
        jTable1.setSelectionBackground(new Color(219, 235, 247));
        jTable1.setSelectionForeground(new Color(25, 42, 62));
        jTable1.setDefaultRenderer(Object.class, new ExpedienteCellRenderer());
        configurarRendererPlazoAtencion();
        configurarOrdenamientoTablaAsignacion();

        JTableHeader header = jTable1.getTableHeader();
        if (header != null) {
            header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));
            header.setFont(header.getFont().deriveFont(Font.BOLD));
            header.setReorderingAllowed(false);
            header.setBackground(new Color(241, 245, 249));
            header.setForeground(new Color(30, 41, 59));
            header.setDefaultRenderer(new SortHeaderRenderer(header.getDefaultRenderer()));
            if (jTable1.getRowSorter() != null) {
                jTable1.getRowSorter().addRowSorterListener(e -> header.repaint());
            }
            configurarTooltipOrdenamientoHeader(header);
        }

        if (jTable1.getColumnModel().getColumnCount() >= 10) {
            ajustarColumna(COL_SELECCION, 34, 38, 42);
            ajustarColumna(COL_EXPANDIR, 28, 32, 36);
            ajustarColumna(COL_ID, 0, 0, 0);
            ajustarColumna(COL_FECHA_SOLICITUD, 90, 105, 120);
            ajustarColumna(COL_CANAL, 0, 0, 0);
            ajustarColumna(COL_REFERENCIA, 150, 185, 230);
            ajustarColumna(COL_TIPO_SOLICITUD, 0, 0, 0);
            ajustarColumna(COL_PROCEDIMIENTO_REGISTRAL, 95, 125, 165);
            ajustarColumna(COL_TIPO_DOCUMENTO, 95, 125, 165);
            ajustarColumna(COL_ACTA, 90, 115, 145);
            ajustarColumna(COL_TITULAR, 180, 260, Integer.MAX_VALUE);
            ajustarColumna(COL_ESTADO, 90, 110, 130);
            ajustarColumna(COL_DIAS_RESTANTES, 75, 90, 105);
            ajustarColumna(COL_ESTADO_ID, 0, 0, 0);
            for (int column = COL_NUMERO_DOCUMENTO; column < jTable1.getColumnModel().getColumnCount(); column++) {
                ajustarColumna(column, 0, 0, 0);
            }
            moverColumnaDiasRestantesAntesDeFecha();
            moverColumnaTipoDocumentoDespuesDeProcedimiento();
        }
    }

    private void moverColumnaDiasRestantesAntesDeFecha()
    {
        int viewPlazo = jTable1.convertColumnIndexToView(COL_DIAS_RESTANTES);
        int viewFecha = jTable1.convertColumnIndexToView(COL_FECHA_SOLICITUD);
        if (viewPlazo >= 0 && viewFecha >= 0 && viewPlazo != viewFecha) {
            jTable1.getColumnModel().moveColumn(viewPlazo, viewFecha);
        }
    }

    private void moverColumnaTipoDocumentoDespuesDeProcedimiento()
    {
        int viewTipoDocumento = jTable1.convertColumnIndexToView(COL_TIPO_DOCUMENTO);
        int viewProcedimiento = jTable1.convertColumnIndexToView(COL_PROCEDIMIENTO_REGISTRAL);
        if (viewTipoDocumento >= 0 && viewProcedimiento >= 0 && viewTipoDocumento != viewProcedimiento + 1) {
            int target = viewTipoDocumento < viewProcedimiento ? viewProcedimiento : viewProcedimiento + 1;
            jTable1.getColumnModel().moveColumn(viewTipoDocumento, target);
        }
    }
    private void configurarRendererPlazoAtencion()
    {
        if (jTable1.getColumnModel().getColumnCount() > COL_DIAS_RESTANTES) {
            jTable1.getColumnModel().getColumn(COL_DIAS_RESTANTES).setCellRenderer(new PlazoAtencionCellRenderer());
        }
    }

    private void configurarOrdenamientoTablaAsignacion()
    {
        if (!(jTable1.getModel() instanceof DefaultTableModel)) {
            return;
        }
        if (jTable1.getModel().getColumnCount() <= COL_ESTADO_ID) {
            return;
        }
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) jTable1.getModel());
        sorter.setComparator(COL_FECHA_SOLICITUD, this::compararFechaSolicitud);
        sorter.setComparator(COL_DIAS_RESTANTES, compararDiasRestantes());
        sorter.setComparator(COL_ID, compararEnteros());
        sorter.setComparator(COL_ESTADO_ID, compararEnteros());
        sorter.setSortable(COL_SELECCION, false);
        sorter.setSortable(COL_EXPANDIR, false);
        sorter.setSortable(COL_ID, false);
        sorter.setSortable(COL_ESTADO_ID, false);
        for (int column = COL_NUMERO_DOCUMENTO; column < jTable1.getModel().getColumnCount(); column++) {
            sorter.setSortable(column, false);
        }
        sorter.setSortsOnUpdates(true);
        jTable1.setRowSorter(sorter);
        paginationHelper = new TablePaginationHelper(jTable1, sorter);
        actualizarPanelPaginacion();
        aplicarFiltrosPorColumna();
    }

    private Comparator<Object> compararEnteros()
    {
        return (left, right) -> Integer.compare(parseIntSeguro(left), parseIntSeguro(right));
    }

    private Comparator<Object> compararDiasRestantes()
    {
        return (left, right) -> Integer.compare(valorDiasRestantes(left), valorDiasRestantes(right));
    }

    private int valorDiasRestantes(Object value)
    {
        if (value instanceof com.sdrerc.domain.model.PlazoAtencionResultado) {
            Integer dias = ((com.sdrerc.domain.model.PlazoAtencionResultado) value).getDiasRestantes();
            return dias == null ? Integer.MAX_VALUE : dias;
        }
        return Integer.MAX_VALUE;
    }

    private int parseIntSeguro(Object value)
    {
        try {
            return Integer.parseInt(textoSeguro(value).trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private int compararFechaSolicitud(Object left, Object right)
    {
        return Long.compare(valorFechaOrden(left), valorFechaOrden(right));
    }

    private long valorFechaOrden(Object value)
    {
        String texto = textoSeguro(value).trim();
        if (texto.isEmpty()) {
            return Long.MAX_VALUE;
        }
        try {
            synchronized (formatoFecha) {
                return formatoFecha.parse(texto).getTime();
            }
        } catch (ParseException ex) {
            return Long.MAX_VALUE;
        }
    }

    private void configurarTooltipOrdenamientoHeader(JTableHeader header)
    {
        if (tooltipOrdenamientoHeaderConfigurado) {
            return;
        }
        tooltipOrdenamientoHeaderConfigurado = true;
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                colapsarDocumentosAsociados();
            }
        });
        header.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int viewColumn = header.columnAtPoint(e.getPoint());
                if (viewColumn < 0) {
                    header.setToolTipText(null);
                    return;
                }
                int modelColumn = header.getTable().convertColumnIndexToModel(viewColumn);
                if (!esColumnaVisibleOrdenable(modelColumn)) {
                    header.setToolTipText(null);
                    return;
                }
                header.setToolTipText("Ordenar por " + header.getTable().getModel().getColumnName(modelColumn));
            }
        });
    }

    private boolean esColumnaVisibleOrdenable(int modelColumn)
    {
        return modelColumn > COL_ID && modelColumn < COL_ESTADO_ID;
    }

    private void ajustarColumna(int index, int min, int preferred, int max)
    {
        TableColumn column = jTable1.getColumnModel().getColumn(index);
        column.setMinWidth(min);
        column.setPreferredWidth(preferred);
        column.setMaxWidth(max);
    }

    private String etiquetaTipoBusqueda(Object value)
    {
        if (value == null) {
            return "";
        }
        String key = value.toString().trim();
        if ("NUMERO_TRAMITE_DOCUMENTO".equalsIgnoreCase(key)) {
            return "N° trámite web";
        }
        if ("NUMERO_DOCUMENTO".equalsIgnoreCase(key)) {
            return "N° documento";
        }
        if ("NUMERO_ACTA".equalsIgnoreCase(key)) {
            return "N° acta";
        }
        if ("TIPO_SOLICITUD".equalsIgnoreCase(key)) {
            return "Tipo de solicitud";
        }
        if ("DNI_REMITENTE".equalsIgnoreCase(key)) {
            return "DNI remitente";
        }
        if ("APELLIDO_NOMBRE_REMITENTE".equalsIgnoreCase(key)) {
            return "Solicitante";
        }
        if ("TIPO_PROCEDIMIENTO_REGISTRAL".equalsIgnoreCase(key)) {
            return "Procedimiento registral";
        }
        return key;
    }

    private String textoSeguro(Object value)
    {
        return value == null ? "" : value.toString();
    }

    private String escaparHtml(String value)
    {
        return textoSeguro(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void actualizarTooltipTipoBusqueda()
    {
        Object selected = cmbTipoBusqueda.getSelectedItem();
        cmbTipoBusqueda.setToolTipText(etiquetaTipoBusqueda(selected) + " - " + textoSeguro(selected));
    }

    private class TipoBusquedaRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setText(etiquetaTipoBusqueda(value));
            label.setToolTipText(textoSeguro(value));
            return label;
        }
    }

    private class ComboTooltipRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setToolTipText(textoSeguro(value));
            return label;
        }
    }

    private class SortHeaderRenderer implements TableCellRenderer {
        private final TableCellRenderer delegate;

        private SortHeaderRenderer(TableCellRenderer delegate) {
            this.delegate = delegate;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            JLabel label = (JLabel) delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int modelColumn = table.convertColumnIndexToModel(column);
            label.setText(textoSeguro(value));
            label.setIcon(indicadorOrden(modelColumn));
            label.setHorizontalTextPosition(SwingConstants.LEFT);
            label.setIconTextGap(6);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
            label.setOpaque(true);
            label.setBackground(new Color(241, 245, 249));
            label.setForeground(new Color(30, 41, 59));
            label.setToolTipText(esColumnaVisibleOrdenable(modelColumn)
                    ? "Ordenar por " + textoSeguro(value)
                    : null);
            return label;
        }

        private Icon indicadorOrden(int modelColumn)
        {
            if (!esColumnaVisibleOrdenable(modelColumn)) {
                return null;
            }
            RowSorter<?> sorter = jTable1.getRowSorter();
            if (sorter != null) {
                for (RowSorter.SortKey key : sorter.getSortKeys()) {
                    if (key.getColumn() == modelColumn) {
                        return SortIndicatorIcon.sorted(key.getSortOrder() == SortOrder.DESCENDING);
                    }
                }
            }
            return SortIndicatorIcon.unsorted();
        }
    }

    private static class SortIndicatorIcon implements Icon {
        private static final int SIZE = 10;
        private static final SortIndicatorIcon UNSORTED = new SortIndicatorIcon(false, false);
        private static final SortIndicatorIcon ASC = new SortIndicatorIcon(true, false);
        private static final SortIndicatorIcon DESC = new SortIndicatorIcon(true, true);
        private final boolean sorted;
        private final boolean descending;

        private SortIndicatorIcon(boolean sorted, boolean descending) {
            this.sorted = sorted;
            this.descending = descending;
        }

        private static SortIndicatorIcon unsorted() {
            return UNSORTED;
        }

        private static SortIndicatorIcon sorted(boolean descending) {
            return descending ? DESC : ASC;
        }

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE + 2;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color active = new Color(37, 99, 160);
            Color inactive = new Color(100, 116, 139);

            if (!sorted || !descending) {
                g2.setColor(sorted ? active : inactive);
                Polygon up = new Polygon();
                up.addPoint(x + 5, y + 1);
                up.addPoint(x + 1, y + 5);
                up.addPoint(x + 9, y + 5);
                g2.fill(up);
            }
            if (!sorted || descending) {
                g2.setColor(sorted ? active : inactive);
                Polygon down = new Polygon();
                down.addPoint(x + 1, y + 7);
                down.addPoint(x + 9, y + 7);
                down.addPoint(x + 5, y + 11);
                g2.fill(down);
            }
            g2.dispose();
        }
    }

    private class ExpedienteCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String texto = textoSeguro(value);
            int modelRow = table.convertRowIndexToModel(row);
            int modelColumn = table.convertColumnIndexToModel(column);
            if (modelColumn == COL_TITULAR && value instanceof TitularListadoValue) {
                label.setToolTipText(((TitularListadoValue) value).getTooltip());
            } else {
                label.setToolTipText((modelColumn == COL_REFERENCIA || modelColumn == COL_ACTA || modelColumn == COL_TITULAR
                        || modelColumn == COL_PROCEDIMIENTO_REGISTRAL || modelColumn == COL_ESTADO) ? texto : null);
            }
            String tooltipAsociacion = obtenerTooltipAsociacion(modelRow);
            if (tooltipAsociacion != null) {
                String tooltipActual = label.getToolTipText();
                label.setToolTipText(tooltipActual == null || tooltipActual.trim().isEmpty()
                        ? tooltipAsociacion
                        : tooltipActual + " - " + tooltipAsociacion);
            }
            label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            boolean pendienteAsignacion = esFilaPendienteAsignacion(table, row);
            boolean filaAsociada = esFilaDetalleModel(modelRow);
            boolean filaPrincipalConAsociados = esFilaPrincipalConAsociadosModel(modelRow);

            if (modelColumn == COL_FECHA_SOLICITUD || modelColumn == COL_CANAL || modelColumn == COL_ESTADO || modelColumn == COL_EXPANDIR) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
            }

            if (!isSelected) {
                label.setBackground(obtenerColorFondoAsociacion(row, modelRow));
                if (filaPrincipalConAsociados) {
                    label.setForeground(new Color(24, 78, 62));
                } else if (filaAsociada) {
                    label.setForeground(new Color(45, 100, 82));
                } else {
                    label.setForeground(pendienteAsignacion ? new Color(30, 41, 59) : new Color(115, 125, 138));
                }
            }

            if (modelColumn == COL_ESTADO) {
                label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
                if (!isSelected) {
                    if (filaPrincipalConAsociados || filaAsociada) {
                        label.setBackground(obtenerColorFondoAsociacion(row, modelRow));
                        label.setForeground(filaPrincipalConAsociados ? new Color(20, 83, 45) : new Color(21, 94, 74));
                    } else {
                        label.setForeground(pendienteAsignacion ? new Color(55, 95, 140) : new Color(100, 116, 139));
                        label.setBackground(pendienteAsignacion ? new Color(232, 241, 252) : new Color(241, 245, 249));
                    }
                    String detalleEstado = pendienteAsignacion
                            ? "Estado: " + texto + " - Pendiente de asignación."
                            : "Estado: " + texto + " - Expediente solo para consulta en esta bandeja.";
                    label.setToolTipText(tooltipAsociacion == null ? detalleEstado : detalleEstado + " - " + tooltipAsociacion);
                }
            } else {
                label.setFont(label.getFont().deriveFont(Font.PLAIN, 12f));
            }
            label.setOpaque(true);
            return label;
        }
    }

    private static class TitularListadoValue {
        private final String display;
        private final String tooltip;

        private TitularListadoValue(String display, String tooltip) {
            this.display = display;
            this.tooltip = tooltip;
        }

        private String getTooltip() {
            return tooltip;
        }

        @Override
        public String toString() {
            return display;
        }
    }

    private boolean esFilaPendienteAsignacion(JTable table, int viewRow)
    {
        if (table.getModel().getColumnCount() <= COL_ESTADO_ID) {
            return true;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object estadoId = table.getModel().getValueAt(modelRow, COL_ESTADO_ID);
        return String.valueOf(Enumerado.EstadoExpediente.RegistroExpediente.getId()).equals(String.valueOf(estadoId));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cmbEstado = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        cmbTipoBusqueda = new javax.swing.JComboBox();
        txtValorBusqueda = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnLimpiar = new javax.swing.JButton();
        pnlFechas = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        txtFechaDesde = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtFechaHasta = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(908, 563));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("FILTRO BUSQUEDA");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel5.setText("Estado del trámite");

        cmbEstado.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbEstado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbEstadoActionPerformed(evt);
            }
        });

        jLabel4.setText("Tipo de búsqueda");

        cmbTipoBusqueda.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbTipoBusqueda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTipoBusquedaActionPerformed(evt);
            }
        });

        txtValorBusqueda.setText("jTextField1");
        txtValorBusqueda.setEnabled(false);
        txtValorBusqueda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtValorBusquedaActionPerformed(evt);
            }
        });

        btnBuscar.setText("BUSCAR");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        btnLimpiar.setText("LIMPIAR");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        pnlFechas.setBackground(new java.awt.Color(255, 255, 255));
        pnlFechas.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel6.setText("Fecha desde");
        pnlFechas.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 120, -1));

        txtFechaDesde.setToolTipText("dd/MM/yyyy");
        pnlFechas.add(txtFechaDesde, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 170, 36));

        jLabel7.setText("Fecha hasta");
        pnlFechas.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 0, 120, -1));

        txtFechaHasta.setToolTipText("dd/MM/yyyy");
        pnlFechas.add(txtFechaHasta, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 20, 170, 36));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 900, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addGap(30, 30, 30)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbTipoBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtValorBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(79, 79, 79)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(cmbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(99, 99, 99)
                                    .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(pnlFechas, javax.swing.GroupLayout.PREFERRED_SIZE, 770, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 875, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(8, 8, 8))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbTipoBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtValorBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlFechas, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        cmbTipoBusqueda.getAccessibleContext().setAccessibleName("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmbEstadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbEstadoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbEstadoActionPerformed

    private void txtValorBusquedaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtValorBusquedaActionPerformed
        
        // TODO add your handling code here:
    }//GEN-LAST:event_txtValorBusquedaActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscarExpedientes();
        // TODO add your handling code here:
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void cmbTipoBusquedaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTipoBusquedaActionPerformed
        if (cmbTipoBusqueda.getSelectedItem() != null) {
            actualizarTooltipTipoBusqueda();
            txtValorBusqueda.setEnabled(true);
            txtValorBusqueda.setText("");
            txtValorBusqueda.requestFocus();
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbTipoBusquedaActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        int columna = jTable1.columnAtPoint(evt.getPoint());
        if (evt.getClickCount() == 1 && columna >= 0 && jTable1.convertColumnIndexToModel(columna) == COL_EXPANDIR) {
            int fila = jTable1.rowAtPoint(evt.getPoint());
            if (fila >= 0) {
                alternarDocumentosAsociados(fila);
            }
            return;
        }

        if (evt.getClickCount() == 2 && jTable1.getSelectedRow() != -1) {

            int fila = jTable1.rowAtPoint(evt.getPoint());
            if (fila < 0) {
                fila = jTable1.getSelectedRow();
            }
            if (fila >= 0) {
                // Obtener datos de la fila
                int filaModelo = jTable1.convertRowIndexToModel(fila);
                String idExpediente = jTable1.getModel().getValueAt(filaModelo, COL_ID).toString();
                //DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                //Expediente expedienteSeleccionado = model.get(fila);

                // Crear el panel al que quieres ir
                JPanelRegistroAsignacion panel = new JPanelRegistroAsignacion();
                
                try {
                    // Si el panel necesita recibir datos:
                    panel.cargarExpediente(idExpediente);
                } catch (Exception ex) {
                    Logger.getLogger(JPanelFiltroBusqueda.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                // Abrir formulario de edición
                MenuPrincipal.ShowJPanel(panel);
            }
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_jTable1MouseClicked

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiarCampos();
        // TODO add your handling code here:
    }//GEN-LAST:event_btnLimpiarActionPerformed
    
    private void limpiarCampos() 
    {
        // Limpiar JTextFields
        txtValorBusqueda.setText("");
        if (rangoFechas != null) {
            rangoFechas.clear();
        }
        // Resetear JComboBoxes al primer elemento
        if (cmbTipoBusqueda.getItemCount() > 0) cmbTipoBusqueda.setSelectedIndex(0);
        if (cmbEstado.getItemCount() > 0) cmbEstado.setSelectedIndex(0);
        limpiarFiltrosPorColumna();
        
        buscarExpedientes();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JComboBox cmbEstado;
    private javax.swing.JComboBox cmbTipoBusqueda;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel pnlFechas;
    private javax.swing.JTextField txtFechaDesde;
    private javax.swing.JTextField txtFechaHasta;
    private javax.swing.JTextField txtValorBusqueda;
    // End of variables declaration//GEN-END:variables
}

