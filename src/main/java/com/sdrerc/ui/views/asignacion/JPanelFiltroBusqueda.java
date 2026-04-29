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
import com.sdrerc.ui.menu.MenuPrincipal;
import com.sdrerc.util.DateRangePickerSupport;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

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

        //List<CatalogoItem> lista = catalogoItemService.obtenerEstados();
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(5);

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
                }
            }
        );
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
