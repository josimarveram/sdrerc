/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.expedientesPorVerificar;

import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.application.CatalogoService;
import com.sdrerc.application.ExpedienteAsignacionService;
import com.sdrerc.application.ExpedienteService;
import com.sdrerc.application.PlazoAtencionService;
import com.sdrerc.application.SupervisionService;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.Enumerado;
import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.domain.model.User;
import com.sdrerc.shared.session.SessionContext;
import com.sdrerc.ui.common.icon.IconUtils;
import com.sdrerc.ui.common.swing.PlazoAtencionCellRenderer;
import com.sdrerc.ui.common.swing.TablePaginationHelper;
import com.sdrerc.ui.menu.MenuPrincipal;
import com.sdrerc.ui.views.asignacion.JPanelFiltroBusqueda;
import com.sdrerc.util.DateRangePickerSupport;
import com.toedter.calendar.JDateChooser;
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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.Scrollable;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
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
 * @author betom
 */
public class JPanelListadoExpedientesPorVerificar extends javax.swing.JPanel implements Scrollable {

    private final ExpedienteService expedienteService;
    private final CatalogoService catalogoService;
    private final CatalogoItemService catalogoItemService;
    private final ExpedienteAsignacionService expedienteAsignacionService;
    private final PlazoAtencionService plazoAtencionService;
    private final SupervisionService supervisionService;
    private final Map<Integer, String> estadosPorId;
    private final Map<Integer, String> tiposSolicitudPorId;
    private final Map<Integer, String> tiposDocumentoPorId;
    private final Map<Integer, String> procedimientosPorId;
    private final Map<Integer, String> tiposActaPorId;
    private final Map<Integer, String> unidadesOrganicasPorId;
    private final Map<Integer, String> direccionesDomiciliariasPorId;
    private final SimpleDateFormat formatoFecha;
    private JLabel lblMensajeListado;
    private JLabel lblFeedbackFechas;
    private JDateChooser fechaDesdePicker;
    private JDateChooser fechaHastaPicker;
    private boolean tooltipOrdenamientoHeaderConfigurado;
    private boolean filtrosPorColumnaConfigurados;
    private TablePaginationHelper paginationHelper;
    private JPanel panelPaginacion;
    private final JDateChooser filtroFechaSolicitudColumna = new JDateChooser();
    private final Map<Integer, JTextField> filtrosTextoPorColumna = new HashMap<>();
    private static final int COL_ID = 0;
    private static final int COL_FECHA_SOLICITUD = 1;
    private static final int COL_CANAL = 2;
    private static final int COL_REFERENCIA = 3;
    private static final int COL_TIPO_SOLICITUD = 4;
    private static final int COL_PROCEDIMIENTO_REGISTRAL = 5;
    private static final int COL_ACTA = 6;
    private static final int COL_TITULAR = 7;
    private static final int COL_ESTADO = 8;
    private static final int COL_DIAS_RESTANTES = 9;
    private static final int COL_ESTADO_ID = 10;
    private static final int COL_TIPO_DOCUMENTO = 11;
    private static final int COL_NUMERO_DOCUMENTO = 12;
    private static final int COL_TIPO_ACTA = 13;
    private static final int COL_NUMERO_ACTA = 14;
    private static final int COL_DNI_TITULAR_1 = 15;
    private static final int COL_TITULAR_1 = 16;
    private static final int COL_DNI_TITULAR_2 = 17;
    private static final int COL_TITULAR_2 = 18;
    private static final int COL_UNIDAD_ORGANICA = 19;
    private static final int COL_CORREO_ELECTRONICO = 20;
    private static final int COL_CELULAR = 21;
    private static final int COL_DIRECCION_DOMICILIARIA = 22;
    private static final int COL_DOMICILIO = 23;
    private static final int COL_DEPARTAMENTO = 24;
    private static final int COL_PROVINCIA = 25;
    private static final int COL_DISTRITO = 26;
    private static final int COL_ABOGADO_DESIGNADO = 27;
    private static final int COL_SUPERVISOR_RESPONSABLE = 28;
    private static final int[] COLUMNAS_EXPORTACION_EXCEL = {
        COL_ID, COL_FECHA_SOLICITUD, COL_CANAL, COL_REFERENCIA, COL_TIPO_SOLICITUD,
        COL_PROCEDIMIENTO_REGISTRAL, COL_ESTADO,
        COL_TIPO_DOCUMENTO, COL_NUMERO_DOCUMENTO, COL_TIPO_ACTA, COL_NUMERO_ACTA,
        COL_DNI_TITULAR_1, COL_TITULAR_1, COL_DNI_TITULAR_2, COL_TITULAR_2,
        COL_UNIDAD_ORGANICA, COL_CORREO_ELECTRONICO, COL_CELULAR,
        COL_DIRECCION_DOMICILIARIA, COL_DOMICILIO, COL_DEPARTAMENTO, COL_PROVINCIA, COL_DISTRITO,
        COL_ABOGADO_DESIGNADO, COL_SUPERVISOR_RESPONSABLE
    };
    
    /**
     * Creates new form JPanelListadoExpedientesAsignados
     */
    public JPanelListadoExpedientesPorVerificar() {
        initComponents();
        this.expedienteService = new ExpedienteService();
        this.catalogoService = new CatalogoService();
        this.catalogoItemService = new CatalogoItemService();
        this.expedienteAsignacionService = new ExpedienteAsignacionService();
        this.plazoAtencionService = new PlazoAtencionService();
        this.supervisionService = new SupervisionService();
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
        configurarListadoPorVerificarPremium();
        buscarExpedientes();
    }
    private void cargarComboEstados() 
    {
        cmbEstado.removeAllItems();
        //cmbEstado.addItem("TODOS");
        
        cmbEstado.addItem(new CatalogoItem(0, 0, "TODOS", 1));

        List<CatalogoItem> lista = catalogoItemService.obtenerEstadosTramite();

        for (CatalogoItem estado : lista) {
            estadosPorId.put(estado.getIdCatalogoItem(), estado.getDescripcion());
            cmbEstado.addItem(estado);
        }
    }
    
    private void cargarTiposBusqueda() 
    {
        cmbTipoBusqueda.removeAllItems();
        cmbTipoBusqueda.addItem("NUMERO_TRAMITE_DOCUMENTO");
        cmbTipoBusqueda.addItem("NUMERO_DOCUMENTO");
        cmbTipoBusqueda.addItem("NUMERO_ACTA");
        cmbTipoBusqueda.addItem("TIPO_SOLICITUD");
        cmbTipoBusqueda.addItem("DNI_REMITENTE");
        cmbTipoBusqueda.addItem("APELLIDO_NOMBRE_REMITENTE");
        cmbTipoBusqueda.addItem("TIPO_PROCEDIMIENTO_REGISTRAL");
        cmbTipoBusqueda.addItem("ABOGADO_DESIGNADO");
        cmbTipoBusqueda.addItem("SUPERVISOR_DESIGNADO");
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
    
     
      private void buscarExpedientes() 
      {
        try 
        {
            if (!esRangoFechasValido()) {
                return;
            }

            AlcanceVerificacion alcance = determinarAlcanceConsultaVerificacionPorUsuarioActual();
            if (!alcance.tienePermiso) {
                cargarTablaNueva(java.util.Collections.<Expediente>emptyList());
                mostrarMensajeListado("No tiene rol de supervisión para visualizar expedientes por verificar.");
                return;
            }
            if (!alcance.verTodo && alcance.supervisorUserId == null) {
                cargarTablaNueva(java.util.Collections.<Expediente>emptyList());
                mostrarMensajeListado("El usuario supervisor no tiene identificador válido.");
                return;
            }
            if (!alcance.verTodo && supervisionService.obtenerAbogados(alcance.supervisorUserId).isEmpty()) {
                cargarTablaNueva(java.util.Collections.<Expediente>emptyList());
                mostrarMensajeListado("El supervisor no tiene abogados asignados.");
                return;
            }

            Object tipoSeleccionado = cmbTipoBusqueda.getSelectedItem();
            String campo = tipoSeleccionado == null ? "" : tipoSeleccionado.toString();
            String valor = txtValorBusqueda.getText() == null ? "" : txtValorBusqueda.getText().trim();
            CatalogoItem estado = (CatalogoItem) cmbEstado.getSelectedItem();
            int idestado = estado == null ? 0 : estado.getIdCatalogoItem();

            Enumerado.EstadoExpediente estadoExpedienteAtendido = Enumerado.EstadoExpediente.ExpedienteAtendido;
            if (idestado == 0) {
                idestado = estadoExpedienteAtendido.getId();
            }

            List<Expediente> lista = expedienteAsignacionService.listarExpedientesPorVerificar(
                    campo, valor, idestado, alcance.supervisorUserId, alcance.verTodo);
            List<Expediente> filtrada = filtrarPorRangoFechas(lista, obtenerFechaDesde(), obtenerFechaHasta());
            cargarTablaNueva(filtrada);
            mostrarMensajeListado(resolverMensajeListaVacia(filtrada.isEmpty(), alcance));
        } 
        catch (Exception e) {
            cargarTablaNueva(java.util.Collections.<Expediente>emptyList());
            mostrarMensajeListado("No se pudo cargar el listado de expedientes por verificar.");
        }
      }
      
    private void limpiarCampos() 
    {
        // Limpiar JTextFields
        txtValorBusqueda.setText("");
        limpiarRangoFechas();
        // Resetear JComboBoxes al primer elemento
        if (cmbTipoBusqueda.getItemCount() > 0) cmbTipoBusqueda.setSelectedIndex(0);
        if (cmbEstado.getItemCount() > 0) cmbEstado.setSelectedIndex(0);
        limpiarFiltrosPorColumna();
        
        buscarExpedientes();
    }
      
    private void cargarTablaNueva(List<Expediente> lista) 
    {
        DefaultTableModel model = crearModeloTablaExpedientesPorVerificar();
        model.setRowCount(0);
        for (Expediente e : lista) {
            model.addRow(crearFilaTablaExpediente(e));
        }
        jTable1.setModel(model);
        configurarTablaResultados();
    }

    private DefaultTableModel crearModeloTablaExpedientesPorVerificar()
    {
        String[] columnas = {
                "ID expediente", "Fecha solicitud", "Canal", "Referencia", "Tipo solicitud",
                "Procedimiento registral", "Acta", "Titular", "Estado", "Días restantes", "EstadoId",
                "Tipo documento", "N° documento", "Tipo acta", "N° acta",
                "DNI titular 1", "Titular 1", "DNI titular 2", "Titular 2",
                "Unidad orgánica", "Correo electrónico", "Celular", "Dirección domiciliaria",
                "Domicilio", "Departamento", "Provincia", "Distrito",
                "Abogado designado", "Supervisor responsable"
        };
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private Object[] crearFilaTablaExpediente(Expediente e)
    {
        return new Object[] {
                e.getIdExpediente(),
                formatearFecha(e.getFechaSolicitud()),
                textoSeguro(e.getCanalRecepcion()),
                obtenerReferencia(e),
                obtenerDescripcionCatalogo(tiposSolicitudPorId, e.getTipoSolicitud()),
                obtenerDescripcionCatalogo(procedimientosPorId, e.getTipoProcedimientoRegistral()),
                obtenerActa(e),
                obtenerTitularListado(e),
                obtenerDescripcionEstado(e.getEstado()),
                plazoAtencionService.calcular(e.getTipoDocumento(), e.getFechaSolicitud()),
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
                textoSeguro(e.getSupervisorDesignado())
        };
    }

    private String obtenerDescripcionEstado(int idEstado) {
        return estadosPorId.getOrDefault(idEstado, String.valueOf(idEstado));
    }

    private String formatearFecha(java.util.Date fecha) {
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

    private String obtenerReferencia(Expediente expediente)
    {
        if (!estaVacio(expediente.getNumeroTramiteDocumento())) {
            return expediente.getNumeroTramiteDocumento().trim();
        }
        if (!estaVacio(expediente.getNumeroDocumento())) {
            return expediente.getNumeroDocumento().trim();
        }
        if (!estaVacio(expediente.getNumeroActa())) {
            return expediente.getNumeroActa().trim();
        }
        return "Sin referencia";
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

    private void configurarListadoPorVerificarPremium()
    {
        setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout());
        inicializarRangoFechas();

        btnBuscar.setText("Buscar");
        btnLimpiar.setText("Limpiar");
        IconUtils.applyIcon(btnBuscar, "search.svg");
        IconUtils.applyIcon(btnLimpiar, "clear.svg");
        estilizarBoton(btnBuscar, true);
        estilizarBoton(btnLimpiar, false);

        txtValorBusqueda.setText("");
        txtValorBusqueda.setEnabled(true);
        txtValorBusqueda.setToolTipText("Ingrese el valor de búsqueda.");
        cmbTipoBusqueda.setToolTipText("Seleccione el tipo de búsqueda.");
        cmbEstado.setToolTipText("Seleccione el estado del trámite.");
        cmbTipoBusqueda.setRenderer(new TipoBusquedaRenderer());
        cmbEstado.setRenderer(new ComboTooltipRenderer());
        actualizarTooltipTipoBusqueda();

        remove(jPanel1);
        jPanel1.removeAll();
        jPanel1.setLayout(new BorderLayout(0, 14));
        jPanel1.setBackground(new Color(245, 247, 250));
        jPanel1.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        jPanel1.add(crearHeader(), BorderLayout.NORTH);
        jPanel1.add(crearContenido(), BorderLayout.CENTER);
        add(jPanel1, BorderLayout.CENTER);

        configurarTablaResultados();
        revalidate();
        repaint();
    }

    private JPanel crearHeader()
    {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Búsqueda de expedientes por verificar");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(25, 52, 84));

        JLabel subtitle = new JLabel("Consulte los expedientes trabajados por abogados y pendientes de revisión.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(100, 116, 139));
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JPanel texts = new JPanel(new BorderLayout());
        texts.setOpaque(false);
        texts.add(title, BorderLayout.NORTH);
        texts.add(subtitle, BorderLayout.CENTER);
        header.add(texts, BorderLayout.CENTER);
        return header;
    }

    private JPanel crearContenido()
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
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 6, 12);

        gbc.gridx = 0;
        gbc.weightx = 0.25;
        card.add(crearLabelFiltro("Tipo de búsqueda"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.32;
        card.add(crearLabelFiltro("Valor de búsqueda"), gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.18;
        card.add(crearLabelFiltro("Estado del trámite"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.20;
        card.add(new JLabel(" "), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.25;
        gbc.insets = new Insets(0, 0, 0, 12);
        card.add(cmbTipoBusqueda, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.32;
        card.add(txtValorBusqueda, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.18;
        gbc.fill = GridBagConstraints.NONE;
        card.add(cmbEstado, gbc);

        JPanel botones = new JPanel(new GridBagLayout());
        botones.setOpaque(false);
        GridBagConstraints b = new GridBagConstraints();
        b.gridx = 0;
        b.insets = new Insets(0, 0, 0, 8);
        botones.add(btnBuscar, b);
        b.gridx = 1;
        b.insets = new Insets(0, 0, 0, 8);
        botones.add(crearBotonExportarExcel(), b);
        b.gridx = 2;
        b.insets = new Insets(0, 0, 0, 0);
        botones.add(btnLimpiar, b);

        gbc.gridx = 3;
        gbc.weightx = 0.20;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        card.add(new JLabel(" "), gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0.22;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
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

        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0.50;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        card.add(botones, gbc);
        gbc.gridwidth = 1;

        lblFeedbackFechas.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(lblFeedbackFechas, gbc);
        return card;
    }

    private JButton crearBotonExportarExcel()
    {
        JButton button = IconUtils.createSecondaryButton("Exportar", "excel.svg");
        button.setToolTipText("Exportar listado a Excel");
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(118, 36));
        button.addActionListener(e -> exportarListadoExpedientesPorVerificarExcel());
        return button;
    }

    private void exportarListadoExpedientesPorVerificarExcel()
    {
        List<Integer> filasExportacion = obtenerFilasExportacion();
        if (filasExportacion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay registros para exportar.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exportar listado a Excel");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivo Excel (*.xlsx)", "xlsx"));
        fileChooser.setSelectedFile(new File(nombreArchivoExcelExpedientesPorVerificar()));

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
            escribirExcelExpedientesPorVerificar(archivo, filasExportacion);
            JOptionPane.showMessageDialog(this, "Archivo Excel generado correctamente.");
        } catch (IOException ex) {
            Logger.getLogger(JPanelListadoExpedientesPorVerificar.class.getName()).log(Level.WARNING, "No se pudo exportar listado de expedientes por verificar", ex);
            JOptionPane.showMessageDialog(this, "No se pudo exportar el archivo Excel.");
        }
    }

    private String nombreArchivoExcelExpedientesPorVerificar()
    {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "expedientes_por_verificar_" + timestamp + ".xlsx";
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
        if (paginationHelper != null) {
            return paginationHelper.getFilteredModelRowsInSortOrder();
        }
        List<Integer> filas = new ArrayList<>();
        for (int viewRow = 0; viewRow < jTable1.getRowCount(); viewRow++) {
            filas.add(jTable1.convertRowIndexToModel(viewRow));
        }
        return filas;
    }

    private void escribirExcelExpedientesPorVerificar(File archivo, List<Integer> filasExportacion) throws IOException
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

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Listado de expedientes por verificar");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(25, 52, 84));
        lblMensajeListado = new JLabel(" ");
        lblMensajeListado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMensajeListado.setForeground(new Color(100, 116, 139));
        header.add(title, BorderLayout.WEST);
        header.add(lblMensajeListado, BorderLayout.EAST);

        JPanel superior = new JPanel(new BorderLayout(0, 8));
        superior.setOpaque(false);
        superior.add(header, BorderLayout.NORTH);
        superior.add(crearPanelFiltrosPorColumnaExpedientesPorVerificar(), BorderLayout.CENTER);

        jScrollPane1.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
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

    private JPanel crearPanelFiltrosPorColumnaExpedientesPorVerificar()
    {
        configurarFiltrosPorColumnaExpedientesPorVerificar();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        agregarFiltroColumna(panel, "Plazo", filtrosTextoPorColumna.get(COL_DIAS_RESTANTES), 0, 0.70, 70);
        agregarFiltroColumna(panel, "Fecha", filtroFechaSolicitudColumna, 1, 0.82, 96);
        agregarFiltroColumna(panel, "Canal", filtrosTextoPorColumna.get(COL_CANAL), 2, 0.65);
        agregarFiltroColumna(panel, "Referencia", filtrosTextoPorColumna.get(COL_REFERENCIA), 3, 1.05);
        agregarFiltroColumna(panel, "Tipo solicitud", filtrosTextoPorColumna.get(COL_TIPO_SOLICITUD), 4, 1.05);
        agregarFiltroColumna(panel, "Procedimiento", filtrosTextoPorColumna.get(COL_PROCEDIMIENTO_REGISTRAL), 5, 1.25);
        agregarFiltroColumna(panel, "Acta", filtrosTextoPorColumna.get(COL_ACTA), 6, 0.95);
        agregarFiltroColumna(panel, "Titular", filtrosTextoPorColumna.get(COL_TITULAR), 7, 1.75);
        agregarFiltroColumna(panel, "Estado", filtrosTextoPorColumna.get(COL_ESTADO), 8, 0.90);

        JButton btnLimpiarFiltros = crearBotonLimpiarFiltrosPorColumna();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 9;
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
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
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

    private void configurarFiltrosPorColumnaExpedientesPorVerificar()
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
        crearFiltroTextoColumna(COL_ACTA, "Filtrar acta");
        crearFiltroTextoColumna(COL_TITULAR, "Filtrar titular");
        crearFiltroTextoColumna(COL_ESTADO, "Filtrar estado");
        crearFiltroTextoColumna(COL_DIAS_RESTANTES, "Filtrar días restantes");
    }

    private void crearFiltroTextoColumna(int columna, String tooltip)
    {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 11));
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
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
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
        dimensionarComponenteFecha(fechaDesdePicker);
        dimensionarComponenteFecha(fechaHastaPicker);
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

    private void inicializarRangoFechas()
    {
        lblFeedbackFechas = new JLabel(" ");
        lblFeedbackFechas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFeedbackFechas.setForeground(new Color(198, 40, 40));

        fechaDesdePicker = new JDateChooser();
        fechaHastaPicker = new JDateChooser();
        DateRangePickerSupport.configurePicker(fechaDesdePicker);
        DateRangePickerSupport.configurePicker(fechaHastaPicker);

        Date hoy = new Date();
        fechaDesdePicker.setDate(hoy);
        fechaHastaPicker.setDate(hoy);

        fechaDesdePicker.setToolTipText("Fecha inicial de solicitud.");
        fechaHastaPicker.setToolTipText("Fecha final de solicitud.");
        fechaDesdePicker.addPropertyChangeListener("date", evt -> esRangoFechasValido());
        fechaHastaPicker.addPropertyChangeListener("date", evt -> esRangoFechasValido());
    }

    private JComponent obtenerComponenteFechaDesde()
    {
        return fechaDesdePicker;
    }

    private JComponent obtenerComponenteFechaHasta()
    {
        return fechaHastaPicker;
    }

    private Date obtenerFechaDesde()
    {
        return fechaDesdePicker == null ? null : DateRangePickerSupport.startOfDay(fechaDesdePicker.getDate());
    }

    private Date obtenerFechaHasta()
    {
        return fechaHastaPicker == null ? null : DateRangePickerSupport.endOfDay(fechaHastaPicker.getDate());
    }

    private boolean esRangoFechasValido()
    {
        if (fechaDesdePicker == null || fechaHastaPicker == null) {
            return true;
        }

        Date desde = DateRangePickerSupport.startOfDay(fechaDesdePicker.getDate());
        Date hasta = DateRangePickerSupport.startOfDay(fechaHastaPicker.getDate());
        boolean invalido = desde != null && hasta != null && desde.after(hasta);
        fechaDesdePicker.putClientProperty("JComponent.outline", invalido ? "error" : null);
        fechaHastaPicker.putClientProperty("JComponent.outline", invalido ? "error" : null);
        if (lblFeedbackFechas != null) {
            lblFeedbackFechas.setText(invalido ? "Fecha Desde no puede ser mayor que Fecha Hasta." : " ");
        }
        return !invalido;
    }

    private void limpiarRangoFechas()
    {
        if (fechaDesdePicker != null) {
            fechaDesdePicker.setDate(null);
            fechaDesdePicker.putClientProperty("JComponent.outline", null);
        }
        if (fechaHastaPicker != null) {
            fechaHastaPicker.setDate(null);
            fechaHastaPicker.putClientProperty("JComponent.outline", null);
        }
        if (lblFeedbackFechas != null) {
            lblFeedbackFechas.setText(" ");
        }
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

    private void configurarTablaResultados()
    {
        jTable1.setRowHeight(30);
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable1.setFillsViewportHeight(true);
        jTable1.setShowGrid(false);
        jTable1.setIntercellSpacing(new Dimension(0, 0));
        jTable1.setSelectionBackground(new Color(219, 234, 254));
        jTable1.setSelectionForeground(new Color(15, 23, 42));
        jTable1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jTable1.setDefaultRenderer(Object.class, new ExpedientePorVerificarCellRenderer());
        configurarRendererPlazoAtencion();
        configurarOrdenamientoTablaResultados();

        JTableHeader header = jTable1.getTableHeader();
        if (header != null) {
            header.setFont(new Font("Segoe UI", Font.BOLD, 12));
            header.setForeground(new Color(51, 65, 85));
            header.setBackground(new Color(241, 245, 249));
            header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));
            header.setReorderingAllowed(false);
            header.setDefaultRenderer(new SortHeaderRenderer(header.getDefaultRenderer()));
            if (jTable1.getRowSorter() != null) {
                jTable1.getRowSorter().addRowSorterListener(e -> header.repaint());
            }
            configurarTooltipOrdenamientoHeader(header);
        }

        if (jTable1.getColumnModel().getColumnCount() >= 10) {
            configurarAnchoColumna(COL_ID, 0, 0, 0);
            configurarAnchoColumna(COL_FECHA_SOLICITUD, 90, 105, 120);
            configurarAnchoColumna(COL_CANAL, 65, 75, 90);
            configurarAnchoColumna(COL_REFERENCIA, 100, 125, 155);
            configurarAnchoColumna(COL_TIPO_SOLICITUD, 110, 125, 150);
            configurarAnchoColumna(COL_PROCEDIMIENTO_REGISTRAL, 135, 170, 220);
            configurarAnchoColumna(COL_ACTA, 95, 120, 155);
            configurarAnchoColumna(COL_TITULAR, 160, 260, Integer.MAX_VALUE);
            configurarAnchoColumna(COL_ESTADO, 95, 110, 130);
            configurarAnchoColumna(COL_DIAS_RESTANTES, 85, 95, 110);
            configurarAnchoColumna(COL_ESTADO_ID, 0, 0, 0);
            for (int column = COL_TIPO_DOCUMENTO; column < jTable1.getColumnModel().getColumnCount(); column++) {
                configurarAnchoColumna(column, 0, 0, 0);
            }
            moverColumnaDiasRestantesAntesDeFecha();
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

    private void configurarRendererPlazoAtencion()
    {
        if (jTable1.getColumnModel().getColumnCount() > COL_DIAS_RESTANTES) {
            jTable1.getColumnModel().getColumn(COL_DIAS_RESTANTES).setCellRenderer(new PlazoAtencionCellRenderer());
        }
    }

    private void configurarOrdenamientoTablaResultados()
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
        sorter.setSortable(COL_ID, false);
        sorter.setSortable(COL_ESTADO_ID, false);
        for (int column = COL_TIPO_DOCUMENTO; column < jTable1.getModel().getColumnCount(); column++) {
            sorter.setSortable(column, false);
        }
        sorter.setSortsOnUpdates(true);
        jTable1.setRowSorter(sorter);
        paginationHelper = new TablePaginationHelper(jTable1, sorter);
        actualizarPanelPaginacion();
        aplicarFiltrosPorColumna();
    }

    private void configurarAnchoColumna(int index, int min, int preferred, int max)
    {
        TableColumn column = jTable1.getColumnModel().getColumn(index);
        column.setMinWidth(min);
        column.setPreferredWidth(preferred);
        column.setMaxWidth(max);
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

    private AlcanceVerificacion determinarAlcanceConsultaVerificacionPorUsuarioActual()
    {
        User usuario = SessionContext.getUsuarioActual();
        boolean esSupervision = usuarioTieneRol(usuario, "SUPERVISION");
        boolean esAdmin = usuarioTieneRol(usuario, "ADMIN_SISTEMA");
        if (!esSupervision) {
            return new AlcanceVerificacion(false, false, null);
        }
        if (esAdmin) {
            return new AlcanceVerificacion(true, true, usuario.getUserId());
        }
        return new AlcanceVerificacion(true, false, usuario.getUserId());
    }

    private boolean usuarioTieneRol(User usuario, String roleName)
    {
        if (usuario == null || roleName == null) {
            return false;
        }
        String esperado = roleName.trim();
        for (String role : usuario.getRoles()) {
            if (role != null && esperado.equalsIgnoreCase(role.trim())) {
                return true;
            }
        }
        return false;
    }

    private String resolverMensajeListaVacia(boolean listaVacia, AlcanceVerificacion alcance)
    {
        if (!listaVacia) {
            return " ";
        }
        if (alcance != null && alcance.verTodo) {
            return "No hay expedientes pendientes de verificación.";
        }
        return "No hay expedientes pendientes de verificación para su equipo.";
    }

    private void mostrarMensajeListado(String mensaje)
    {
        if (lblMensajeListado != null) {
            lblMensajeListado.setText(mensaje == null ? " " : mensaje);
        }
    }

    private void actualizarTooltipTipoBusqueda()
    {
        Object selected = cmbTipoBusqueda.getSelectedItem();
        cmbTipoBusqueda.setToolTipText(etiquetaTipoBusqueda(selected) + " - " + textoSeguro(selected));
    }

    private String etiquetaTipoBusqueda(Object value)
    {
        String texto = textoSeguro(value);
        switch (texto) {
            case "NUMERO_TRAMITE_DOCUMENTO":
                return "N° trámite web";
            case "NUMERO_DOCUMENTO":
                return "N° documento";
            case "NUMERO_ACTA":
                return "N° acta";
            case "TIPO_SOLICITUD":
                return "Tipo de solicitud";
            case "DNI_REMITENTE":
                return "DNI remitente";
            case "APELLIDO_NOMBRE_REMITENTE":
                return "Solicitante";
            case "TIPO_PROCEDIMIENTO_REGISTRAL":
                return "Tipo procedimiento";
            case "ABOGADO_DESIGNADO":
                return "Abogado responsable";
            case "SUPERVISOR_DESIGNADO":
                return "Supervisor";
            default:
                return texto;
        }
    }

    private String textoSeguro(Object value)
    {
        return value == null ? "" : value.toString().trim();
    }

    private String escaparHtml(String value)
    {
        return textoSeguro(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
        return Math.max(visibleRect.height - 32, 16);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    private static class AlcanceVerificacion {
        private final boolean tienePermiso;
        private final boolean verTodo;
        private final Long supervisorUserId;

        private AlcanceVerificacion(boolean tienePermiso, boolean verTodo, Long supervisorUserId) {
            this.tienePermiso = tienePermiso;
            this.verTodo = verTodo;
            this.supervisorUserId = supervisorUserId;
        }
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

    private class ExpedientePorVerificarCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String texto = textoSeguro(value);
            int modelColumn = table.convertColumnIndexToModel(column);
            label.setText(texto);
            label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (modelColumn == COL_TITULAR && value instanceof TitularListadoValue) {
                label.setToolTipText(((TitularListadoValue) value).getTooltip());
            } else {
                label.setToolTipText((modelColumn == COL_REFERENCIA || modelColumn == COL_ACTA || modelColumn == COL_TITULAR
                        || modelColumn == COL_PROCEDIMIENTO_REGISTRAL || modelColumn == COL_ESTADO) ? texto : null);
            }

            if (!isSelected) {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                label.setForeground(new Color(30, 41, 59));
            }

            if (modelColumn == COL_ESTADO) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
                if (!isSelected) {
                    label.setForeground(new Color(55, 95, 140));
                    label.setBackground(new Color(232, 241, 252));
                }
            } else {
                label.setHorizontalAlignment((modelColumn == COL_FECHA_SOLICITUD || modelColumn == COL_CANAL)
                        ? SwingConstants.CENTER
                        : SwingConstants.LEFT);
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
        cmbTipoBusqueda = new javax.swing.JComboBox();
        txtValorBusqueda = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cmbEstado = new javax.swing.JComboBox();
        btnLimpiar = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("FILTRO BUSQUEDA");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel5.setText("Tipo de búsqueda");

        cmbTipoBusqueda.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbTipoBusqueda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTipoBusquedaActionPerformed(evt);
            }
        });

        txtValorBusqueda.setText("jTextField1");
        txtValorBusqueda.setEnabled(false);

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText("Estado del trámite");

        cmbEstado.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnLimpiar.setText("LIMPIAR");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
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
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Nª", "Nro Tramite Documento", "Fecha Solicitud", "Tipo Solicitud", "Nombre Ciudadano / Entidad", "Estado Registro"
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(cmbTipoBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtValorBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(cmbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(363, 363, 363)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 876, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addComponent(cmbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel4))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(3, 3, 3)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbTipoBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtValorBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(70, 70, 70)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 17, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmbTipoBusquedaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTipoBusquedaActionPerformed
        if (cmbTipoBusqueda.getSelectedItem() != null) {
            actualizarTooltipTipoBusqueda();
            txtValorBusqueda.setEnabled(true);
            txtValorBusqueda.setText("");
            txtValorBusqueda.requestFocus();
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbTipoBusquedaActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
        limpiarCampos();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscarExpedientes();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        if (evt.getClickCount() == 2 && jTable1.getSelectedRow() != -1)
        {
            int fila = jTable1.getSelectedRow();
            if (fila >= 0)
            {
                // Obtener datos de la fila
                String idExpediente = jTable1.getValueAt(fila, 0).toString();
                String descripcion = jTable1.getValueAt(fila, 1).toString();
                String fecha = jTable1.getValueAt(fila, 2).toString();
                //DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                //Expediente expedienteSeleccionado = model.get(fila);

                // Crear el panel al que quieres ir
                JPanelRegistrarExpedientePorVerificar panel = new JPanelRegistrarExpedientePorVerificar();
                try
                {
                    // Si el panel necesita recibir datos:
                    panel.cargarExpediente(idExpediente);
                }
                catch (Exception ex)
                {
                    Logger.getLogger(JPanelFiltroBusqueda.class.getName()).log(Level.SEVERE, null, ex);
                }
                // Abrir formulario de edición
                MenuPrincipal.ShowJPanel(panel);
            }
        }
    }//GEN-LAST:event_jTable1MouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JComboBox cmbEstado;
    private javax.swing.JComboBox cmbTipoBusqueda;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField txtValorBusqueda;
    // End of variables declaration//GEN-END:variables
}
