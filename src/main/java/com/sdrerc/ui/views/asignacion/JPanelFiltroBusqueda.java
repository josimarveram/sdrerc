/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.asignacion;

import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.application.CatalogoService;
import com.sdrerc.application.ExpedienteService;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.Enumerado;
import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.ui.common.icon.IconUtils;
import com.sdrerc.ui.menu.MenuPrincipal;
import com.sdrerc.util.DateRangePickerSupport;
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
import javax.swing.SwingConstants;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

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
    private final Map<Integer, String> estadosPorId;
    private final SimpleDateFormat formatoFecha;
    private DateRangePickerSupport.Range rangoFechas;
    private JLabel lblFeedbackFechas;
    
    
    public JPanelFiltroBusqueda(){
        initComponents();
        initDatePickers();
        this.expedienteService = new ExpedienteService();
        this.catalogoService = new CatalogoService();
        this.catalogoItemService = new CatalogoItemService();
        this.estadosPorId = new HashMap<>();
        this.formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        this.formatoFecha.setLenient(false);
        cargarTiposBusqueda();
        cargarComboEstados();    
        configurarComponentesAsignacion();
        buscarExpedientes();
    }
          
    public void cargarTabla(JTable tabla) throws Exception {

        String[] columnas = {
                "ID", "Fecha", "N° Trámite", "Solicitante", "Titular", "Estado"
        };

        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);

        List<Expediente> lista = expedienteService.listarExpedientes();

        for (Expediente e : lista) {
            Object[] fila = {
                    e.getIdExpediente(),
                    formatearFecha(e.getFechaSolicitud()),
                    e.getNumeroTramiteDocumento(),
                    e.getApellidoNombreRemitente(),
                    e.getApellidoNombreTitular(),
                    obtenerDescripcionEstado(e.getEstado())
            };

            modelo.addRow(fila);
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
        cmbTipoBusqueda.addItem("TIPO_SOLICITUD");
        cmbTipoBusqueda.addItem("DNI_REMITENTE");
        cmbTipoBusqueda.addItem("APELLIDO_NOMBRE_REMITENTE");
        cmbTipoBusqueda.addItem("TIPO_PROCEDIMIENTO_REGISTRAL");
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
            
            Enumerado.EstadoExpediente estadoExpediente = Enumerado.EstadoExpediente.RegistroExpediente;
            //String estado = cmbEstado.getSelectedItem();

            List<Expediente> lista = expedienteService.buscar(campo, valor, estadoExpediente.getId());

            cargarTablaNueva(filtrarPorRangoFechas(lista, fechaDesde, fechaHasta));

        } 
        catch (Exception e) {
        }
    }
    
    private void cargarTablaNueva(List<Expediente> lista) {
        
        String[] columnas = {
                "ID", "Fecha", "N° Trámite", "Solicitante", "Titular", "Estado"
        };
        
        DefaultTableModel model = new DefaultTableModel(columnas, 0){        
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
                    obtenerDescripcionEstado(e.getEstado())
            };

            model.addRow(fila);
        }

        jTable1.setModel(model);
        configurarTablaAsignacion();
    }

    private String obtenerDescripcionEstado(int idEstado)
    {
        return estadosPorId.getOrDefault(idEstado, String.valueOf(idEstado));
    }

    private String formatearFecha(java.util.Date fecha)
    {
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
                    Date hoy = new Date();
                    rangoFechas.getFromPicker().setDate(hoy);
                    rangoFechas.getToPicker().setDate(hoy);
                    configurarVistaAsignacion();
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
        gbc.insets = new Insets(0, 0, 6, 12);
        card.add(crearLabelFiltro("Tipo de búsqueda"), gbc);
        gbc.gridx = 1;
        card.add(crearLabelFiltro("Valor de búsqueda"), gbc);
        gbc.gridx = 2;
        card.add(crearLabelFiltro("Estado del trámite"), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(cmbTipoBusqueda, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        card.add(txtValorBusqueda, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        card.add(cmbEstado, gbc);

        JPanel acciones = new JPanel(new GridBagLayout());
        acciones.setOpaque(false);
        GridBagConstraints gbcAccion = new GridBagConstraints();
        gbcAccion.insets = new Insets(0, 0, 0, 8);
        acciones.add(btnBuscar, gbcAccion);
        gbcAccion.insets = new Insets(0, 0, 0, 0);
        acciones.add(btnLimpiar, gbcAccion);

        gbc.gridx = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        card.add(acciones, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(12, 0, 6, 12);
        card.add(crearLabelFiltro("Fecha desde"), gbc);
        gbc.gridx = 1;
        card.add(crearLabelFiltro("Fecha hasta"), gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 12);
        card.add(obtenerComponenteFechaDesde(), gbc);
        gbc.gridx = 1;
        card.add(obtenerComponenteFechaHasta(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(8, 0, 0, 0);
        card.add(lblFeedbackFechas, gbc);

        return card;
    }

    private JPanel crearCardResultados()
    {
        JPanel card = crearCard();
        card.setLayout(new BorderLayout(0, 10));

        JLabel titulo = new JLabel("Resultados");
        titulo.setFont(new Font("Arial", Font.BOLD, 15));
        titulo.setForeground(new Color(25, 42, 62));

        jScrollPane1.setViewportView(jTable1);
        jScrollPane1.setBorder(BorderFactory.createLineBorder(new Color(218, 224, 231)));
        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        card.add(titulo, BorderLayout.NORTH);
        card.add(jScrollPane1, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearCard()
    {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        return card;
    }

    private JLabel crearLabelFiltro(String texto)
    {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(new Color(73, 85, 99));
        return label;
    }

    private void dimensionarFiltros()
    {
        cmbTipoBusqueda.setPreferredSize(new Dimension(230, 34));
        cmbEstado.setPreferredSize(new Dimension(180, 34));
        txtValorBusqueda.setPreferredSize(new Dimension(280, 34));
        obtenerComponenteFechaDesde().setPreferredSize(new Dimension(140, 34));
        obtenerComponenteFechaHasta().setPreferredSize(new Dimension(140, 34));
        btnBuscar.setPreferredSize(new Dimension(112, 34));
        btnLimpiar.setPreferredSize(new Dimension(112, 34));
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
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        jTable1.setFillsViewportHeight(true);
        jTable1.setShowGrid(false);
        jTable1.setIntercellSpacing(new Dimension(0, 0));
        jTable1.setSelectionBackground(new Color(219, 235, 247));
        jTable1.setSelectionForeground(new Color(25, 42, 62));
        jTable1.setDefaultRenderer(Object.class, new ExpedienteCellRenderer());

        JTableHeader header = jTable1.getTableHeader();
        if (header != null) {
            header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));
            header.setFont(header.getFont().deriveFont(Font.BOLD));
            header.setReorderingAllowed(false);
            header.setBackground(new Color(241, 245, 249));
            header.setForeground(new Color(30, 41, 59));
        }

        if (jTable1.getColumnModel().getColumnCount() >= 6) {
            ajustarColumna(0, 50, 60, 80);
            ajustarColumna(1, 95, 110, 130);
            ajustarColumna(2, 130, 150, 180);
            ajustarColumna(3, 170, 280, Integer.MAX_VALUE);
            ajustarColumna(4, 170, 280, Integer.MAX_VALUE);
            ajustarColumna(5, 110, 130, 150);
        }
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
            return "N° trámite / documento";
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
            label.setToolTipText((column == 2 || column == 3 || column == 4) ? texto : null);
            label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            if (column == 0 || column == 1 || column == 5) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
            }

            if (!isSelected) {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                label.setForeground(new Color(30, 41, 59));
            }

            if (column == 5) {
                label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
                if (!isSelected) {
                    label.setForeground(new Color(55, 95, 140));
                    label.setBackground(new Color(232, 241, 252));
                }
            } else {
                label.setFont(label.getFont().deriveFont(Font.PLAIN, 12f));
            }
            label.setOpaque(true);
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
        if (evt.getClickCount() == 2 && jTable1.getSelectedRow() != -1) {

            int fila = jTable1.getSelectedRow();
            if (fila >= 0) {
                // Obtener datos de la fila
                String idExpediente = jTable1.getValueAt(fila, 0).toString();
                String descripcion = jTable1.getValueAt(fila, 1).toString();
                String fecha = jTable1.getValueAt(fila, 2).toString();
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
