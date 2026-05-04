/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.expedientesPorTrabajar;

import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.application.CatalogoService;
import com.sdrerc.application.ExpedienteAsignacionService;
import com.sdrerc.application.ExpedienteService;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.Enumerado;
import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.domain.model.User;
import com.sdrerc.shared.session.SessionContext;
import com.sdrerc.ui.common.icon.IconUtils;
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 *
 * @author betom
 */
public class JPanelListadoExpedientesPorTrabajar extends javax.swing.JPanel implements Scrollable {

    private final ExpedienteService expedienteService;
    private final CatalogoService catalogoService;
    private final CatalogoItemService catalogoItemService;
    private final ExpedienteAsignacionService expedienteAsignacionService;
    private final Map<Integer, String> estadosPorId;
    private final SimpleDateFormat formatoFecha;
    private JLabel lblMensajeListado;
    private JLabel lblFeedbackFechas;
    private JDateChooser fechaDesdePicker;
    private JDateChooser fechaHastaPicker;
    
    /**
     * Creates new form JPanelListadoExpedientesAsignados
     */
    public JPanelListadoExpedientesPorTrabajar() {
        initComponents();
        this.expedienteService = new ExpedienteService();
        this.catalogoService = new CatalogoService();
        this.catalogoItemService = new CatalogoItemService();
        this.expedienteAsignacionService = new ExpedienteAsignacionService();
        this.estadosPorId = new HashMap<>();
        this.formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        this.formatoFecha.setLenient(false);
        
        cargarTiposBusqueda();
        cargarComboEstados();
        configurarListadoPorTrabajarPremium();
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
        cmbTipoBusqueda.addItem("TIPO_SOLICITUD");
        cmbTipoBusqueda.addItem("DNI_REMITENTE");
        cmbTipoBusqueda.addItem("APELLIDO_NOMBRE_REMITENTE");
        cmbTipoBusqueda.addItem("TIPO_PROCEDIMIENTO_REGISTRAL");
        cmbTipoBusqueda.addItem("ABOGADO_DESIGNADO");
    }
    
     
      private void buscarExpedientes() 
      {
        try 
        {
            if (!esRangoFechasValido()) {
                return;
            }

            int idTecnicoFiltro = obtenerIdTecnicoFiltroPorTrabajar();
            if (idTecnicoFiltro == -1) {
                cargarTablaNueva(java.util.Collections.<Expediente>emptyList());
                mostrarMensajeListado("No tiene rol de abogado para visualizar expedientes por trabajar.");
                return;
            }
            if (idTecnicoFiltro == -2) {
                cargarTablaNueva(java.util.Collections.<Expediente>emptyList());
                mostrarMensajeListado("El usuario abogado no tiene persona operativa vinculada.");
                return;
            }

            Object tipoSeleccionado = cmbTipoBusqueda.getSelectedItem();
            String campo = tipoSeleccionado == null ? "" : tipoSeleccionado.toString();
            String valor = txtValorBusqueda.getText() == null ? "" : txtValorBusqueda.getText().trim();
            CatalogoItem estado = (CatalogoItem) cmbEstado.getSelectedItem();
            int idestado = estado == null ? 0 : estado.getIdCatalogoItem();
            if (idestado == 0) {
                idestado = Enumerado.EstadoExpediente.ExpedienteRecibido.getId();
            }

            List<Expediente> lista = expedienteAsignacionService.listarExpedientesPorTrabajar(campo, valor, idestado, idTecnicoFiltro);
            List<Expediente> filtrada = filtrarPorRangoFechas(lista, obtenerFechaDesde(), obtenerFechaHasta());
            cargarTablaNueva(filtrada);
            mostrarMensajeListado(filtrada.isEmpty() ? "No se encontraron expedientes por trabajar con los filtros seleccionados." : " ");
        } 
        catch (Exception e) {
            cargarTablaNueva(java.util.Collections.<Expediente>emptyList());
            mostrarMensajeListado("No se pudo cargar el listado de expedientes por trabajar.");
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
        
        buscarExpedientes();
    }
      
    private void cargarTablaNueva(List<Expediente> lista) 
    {        
        String[] columnas = 
        {
          "ID", "Fecha", "N° Trámite", "Solicitante", "Titular", "Abogado designado", "Estado"
        };
        
        DefaultTableModel model = new DefaultTableModel(columnas, 0)
        {        
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };      
                
        //DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        for (Expediente e : lista) {
            Object[] fila = {
                    e.getIdExpediente(),
                    formatearFecha(e.getFechaSolicitud()),
                    e.getNumeroTramiteDocumento(),
                    e.getApellidoNombreRemitente(),
                    e.getApellidoNombreTitular(),
                    textoSeguro(e.getAbogadoDesignado()),
                    obtenerDescripcionEstado(e.getEstado())
            };
            model.addRow(fila);
        }
        jTable1.setModel(model);
        configurarTablaExpedientesPorTrabajar();
    }

    private String obtenerDescripcionEstado(int idEstado) {
        return estadosPorId.getOrDefault(idEstado, String.valueOf(idEstado));
    }

    private String formatearFecha(java.util.Date fecha) {
        return fecha == null ? "" : formatoFecha.format(fecha);
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

    private void configurarListadoPorTrabajarPremium()
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

        configurarTablaExpedientesPorTrabajar();
        revalidate();
        repaint();
    }

    private JPanel crearHeader()
    {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Expedientes por trabajar");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(25, 52, 84));

        JLabel subtitle = new JLabel("Consulte los expedientes recibidos pendientes de atención.");
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
        gbc.weightx = 0.26;
        card.add(crearLabelFiltro("Tipo de búsqueda"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.34;
        card.add(crearLabelFiltro("Valor de búsqueda"), gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.20;
        card.add(crearLabelFiltro("Estado del trámite"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.20;
        card.add(new JLabel(" "), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.26;
        gbc.insets = new Insets(0, 0, 0, 12);
        card.add(cmbTipoBusqueda, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.34;
        card.add(txtValorBusqueda, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.20;
        card.add(cmbEstado, gbc);

        JPanel botones = new JPanel(new GridBagLayout());
        botones.setOpaque(false);
        GridBagConstraints b = new GridBagConstraints();
        b.gridx = 0;
        b.insets = new Insets(0, 0, 0, 8);
        botones.add(btnBuscar, b);
        b.gridx = 1;
        b.insets = new Insets(0, 0, 0, 0);
        botones.add(btnLimpiar, b);

        gbc.gridx = 3;
        gbc.weightx = 0.20;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        card.add(new JLabel(" "), gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0.25;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(12, 0, 6, 12);
        card.add(crearLabelFiltro("Fecha desde"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.25;
        card.add(crearLabelFiltro("Fecha hasta"), gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.weightx = 0.25;
        gbc.insets = new Insets(0, 0, 0, 12);
        card.add(obtenerComponenteFechaDesde(), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.25;
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

    private JPanel crearCardResultados()
    {
        JPanel card = crearCard();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Listado de expedientes por trabajar");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(25, 52, 84));
        lblMensajeListado = new JLabel(" ");
        lblMensajeListado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMensajeListado.setForeground(new Color(100, 116, 139));
        header.add(title, BorderLayout.WEST);
        header.add(lblMensajeListado, BorderLayout.EAST);

        jScrollPane1.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        card.add(header, BorderLayout.NORTH);
        card.add(jScrollPane1, BorderLayout.CENTER);
        return card;
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
        cmbEstado.setPreferredSize(new Dimension(180, 36));
        txtValorBusqueda.setPreferredSize(new Dimension(300, 36));
        fechaDesdePicker.setPreferredSize(new Dimension(170, 36));
        fechaHastaPicker.setPreferredSize(new Dimension(170, 36));
        btnBuscar.setPreferredSize(new Dimension(116, 36));
        btnLimpiar.setPreferredSize(new Dimension(116, 36));
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

    private void configurarTablaExpedientesPorTrabajar()
    {
        jTable1.setRowHeight(30);
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        jTable1.setFillsViewportHeight(true);
        jTable1.setShowGrid(false);
        jTable1.setIntercellSpacing(new Dimension(0, 0));
        jTable1.setSelectionBackground(new Color(219, 234, 254));
        jTable1.setSelectionForeground(new Color(15, 23, 42));
        jTable1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jTable1.setDefaultRenderer(Object.class, new ExpedientePorTrabajarCellRenderer());

        JTableHeader header = jTable1.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setForeground(new Color(51, 65, 85));
        header.setBackground(new Color(241, 245, 249));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
        header.setReorderingAllowed(false);

        if (jTable1.getColumnModel().getColumnCount() < 7) {
            return;
        }
        ocultarColumna(0);
        configurarAnchoColumna(1, 100, 110, 125);
        configurarAnchoColumna(2, 125, 145, 170);
        configurarAnchoColumna(3, 170, 260, 520);
        configurarAnchoColumna(4, 170, 260, 520);
        configurarAnchoColumna(5, 170, 240, 420);
        configurarAnchoColumna(6, 95, 115, 135);
    }

    private void ocultarColumna(int index)
    {
        TableColumn column = jTable1.getColumnModel().getColumn(index);
        column.setMinWidth(0);
        column.setPreferredWidth(0);
        column.setMaxWidth(0);
        column.setResizable(false);
    }

    private void configurarAnchoColumna(int index, int min, int preferred, int max)
    {
        TableColumn column = jTable1.getColumnModel().getColumn(index);
        column.setMinWidth(min);
        column.setPreferredWidth(preferred);
        column.setMaxWidth(max);
    }

    private int obtenerIdTecnicoFiltroPorTrabajar()
    {
        User usuario = SessionContext.getUsuarioActual();
        boolean esAbogado = usuario.hasRole("ABOGADO");
        boolean esAdmin = usuario.hasRole("ADMIN_SISTEMA");
        if (!esAbogado) {
            return -1;
        }
        if (esAdmin) {
            return 0;
        }
        Long idTecnico = usuario.getIdTecnico();
        if (idTecnico == null || idTecnico <= 0) {
            return -2;
        }
        return Math.toIntExact(idTecnico);
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
                return "N° trámite";
            case "TIPO_SOLICITUD":
                return "Tipo de solicitud";
            case "DNI_REMITENTE":
                return "DNI remitente";
            case "APELLIDO_NOMBRE_REMITENTE":
                return "Solicitante";
            case "TIPO_PROCEDIMIENTO_REGISTRAL":
                return "Tipo procedimiento";
            case "ABOGADO_DESIGNADO":
                return "Abogado designado";
            default:
                return texto;
        }
    }

    private String textoSeguro(Object value)
    {
        return value == null ? "" : value.toString().trim();
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

    private class ExpedientePorTrabajarCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String texto = textoSeguro(value);
            label.setText(texto);
            label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            label.setToolTipText((column == 2 || column == 3 || column == 4 || column == 5) ? texto : null);

            if (!isSelected) {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                label.setForeground(new Color(30, 41, 59));
            }

            if (column == 6) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                if (!isSelected) {
                    label.setForeground(new Color(55, 95, 140));
                    label.setBackground(new Color(232, 241, 252));
                }
            } else {
                label.setHorizontalAlignment(column == 1 ? SwingConstants.CENTER : SwingConstants.LEFT);
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
            }
            return label;
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
                JPanelRegistrarExpedientePorTrabajar panel = new JPanelRegistrarExpedientePorTrabajar();
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
