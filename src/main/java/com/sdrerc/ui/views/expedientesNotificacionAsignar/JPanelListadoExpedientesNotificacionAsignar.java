/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.expedientesNotificacionAsignar;

import com.sdrerc.ui.views.expedientesEjecucionAsignar.*;
import com.sdrerc.ui.views.expedientesPorVerificar.*;
import com.sdrerc.ui.views.expedientesPorTrabajar.*;
import com.sdrerc.ui.views.expedientesAsignados.*;
import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.application.CatalogoService;
import com.sdrerc.application.ExpedienteAsignacionService;
import com.sdrerc.application.ExpedienteNotificacionAsignacionService;
import com.sdrerc.application.ExpedientePorNotificarService;
import com.sdrerc.application.ExpedienteService;
import com.sdrerc.application.PlazoAtencionService;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.Enumerado;
import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.ui.common.swing.PlazoAtencionCellRenderer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.sdrerc.ui.views.asignacion.JPanelFiltroBusqueda;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sdrerc.ui.menu.MenuPrincipal;

/**
 *
 * @author betom
 */
public class JPanelListadoExpedientesNotificacionAsignar extends javax.swing.JPanel {

    private final ExpedienteService expedienteService;
    private final CatalogoService catalogoService;
    private final CatalogoItemService catalogoItemService;
    private final ExpedienteNotificacionAsignacionService expedienteNotificacionAsignacionService;
    private final Map<Integer, String> estadosPorId;
    private final Map<Integer, String> procedimientosPorId;
    private final Map<Integer, String> tiposDocumentoPorId;
    private final Map<Integer, String> tiposActaPorId;
    private final SimpleDateFormat formatoFecha;
    private final PlazoAtencionService plazoAtencionService;
    
    /**
     * Creates new form JPanelListadoExpedientesAsignados
     */
    public JPanelListadoExpedientesNotificacionAsignar() {
        initComponents();
        this.expedienteService = new ExpedienteService();
        this.catalogoService = new CatalogoService();
        this.catalogoItemService = new CatalogoItemService();
        this.expedienteNotificacionAsignacionService = new ExpedienteNotificacionAsignacionService();
        this.estadosPorId = new HashMap<>();
        this.procedimientosPorId = new HashMap<>();
        this.tiposDocumentoPorId = new HashMap<>();
        this.tiposActaPorId = new HashMap<>();
        this.formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        this.formatoFecha.setLenient(false);
        this.plazoAtencionService = new PlazoAtencionService();
        
        cargarTiposBusqueda();
        cargarCatalogosListado();
        cargarComboEstados();    
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
        cmbTipoBusqueda.addItem("NUM_EXPEDIENTE");
        cmbTipoBusqueda.addItem("TIPO_SOLICITUD");
        cmbTipoBusqueda.addItem("DNI_REMITENTE");
        cmbTipoBusqueda.addItem("APELLIDO_NOMBRE_REMITENTE");
        cmbTipoBusqueda.addItem("TIPO_PROCEDIMIENTO_REGISTRAL");
    }
    
     
      private void buscarExpedientes() 
      {
        try 
        {
            String valor = txtValorBusqueda.getText();            
            CatalogoItem estado = (CatalogoItem) cmbEstado.getSelectedItem();
                        
            Enumerado.EstadoExpediente estadoExpedienteEjecucionTrabajada = Enumerado.EstadoExpediente.ExpedienteEjecucionTrabajada;
            
            List<Expediente> lista = expedienteNotificacionAsignacionService.ListarExpedientesNotificacion(estadoExpedienteEjecucionTrabajada.getId());
            cargarTablaNueva(lista);
        } 
        catch (Exception e) 
        {
        }
      }
      
    private void limpiarCampos() 
    {
        // Limpiar JTextFields
        txtValorBusqueda.setText("");
        // Resetear JComboBoxes al primer elemento
        if (cmbTipoBusqueda.getItemCount() > 0) cmbTipoBusqueda.setSelectedIndex(0);
        if (cmbEstado.getItemCount() > 0) cmbEstado.setSelectedIndex(0);
        
        buscarExpedientes();
    }
      
    private void cargarTablaNueva(List<Expediente> lista) 
    {        
        String[] columnas = {
          "ID expediente", "Días restantes", "Fecha solicitud", "Num. Expediente",
          "Proc. Reg", "Tipo Doc.", "Acta", "Titular", "Estado"
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
                    plazoAtencionService.calcular(e.getTipoDocumento(), e.getFechaSolicitud()),
                    formatearFecha(e.getFechaSolicitud()),
                    obtenerNumeroExpedienteListado(e),
                    obtenerDescripcionCatalogo(procedimientosPorId, e.getTipoProcedimientoRegistral()),
                    obtenerDescripcionCatalogo(tiposDocumentoPorId, e.getTipoDocumento()),
                    obtenerActa(e),
                    obtenerTitularListado(e),
                    obtenerDescripcionEstado(e.getEstado())
            };
            model.addRow(fila);
        }
        jTable1.setModel(model);
        configurarTablaNotificacion();
    }

    private String obtenerDescripcionEstado(int idEstado) {
        return estadosPorId.getOrDefault(idEstado, String.valueOf(idEstado));
    }

    private String formatearFecha(java.util.Date fecha) {
        return fecha == null ? "" : formatoFecha.format(fecha);
    }

    private void cargarCatalogosListado() {
        cargarMapaCatalogo(2, tiposDocumentoPorId);
        cargarMapaCatalogo(3, procedimientosPorId);
        cargarMapaCatalogo(4, tiposActaPorId);
    }

    private void cargarMapaCatalogo(int idCatalogo, Map<Integer, String> destino) {
        destino.clear();
        List<CatalogoItem> items = catalogoItemService.listarCatalogoItem(idCatalogo);
        for (CatalogoItem item : items) {
            destino.put(item.getIdCatalogoItem(), item.getDescripcion());
        }
    }

    private String obtenerDescripcionCatalogo(Map<Integer, String> catalogo, int id) {
        if (id <= 0) {
            return "";
        }
        return catalogo.getOrDefault(id, String.valueOf(id));
    }

    private String obtenerNumeroExpedienteListado(Expediente expediente) {
        String numExpediente = textoSeguro(expediente.getNumExpediente());
        return numExpediente.isEmpty() ? "Sin expediente" : numExpediente;
    }

    private String obtenerActa(Expediente expediente) {
        String tipoActa = obtenerDescripcionCatalogo(tiposActaPorId, expediente.getTipoActa());
        String numeroActa = textoSeguro(expediente.getNumeroActa());
        if (!tipoActa.isEmpty() && !numeroActa.isEmpty()) {
            return tipoActa + " " + numeroActa;
        }
        return !tipoActa.isEmpty() ? tipoActa : numeroActa;
    }

    private String obtenerTitularListado(Expediente expediente) {
        String titular1 = textoSeguro(expediente.getApellidoNombreTitular());
        String titular2 = textoSeguro(expediente.getApellidoNombreTitular2());
        if (!titular1.isEmpty() && !titular2.isEmpty()) {
            return titular1 + " / " + titular2;
        }
        return !titular1.isEmpty() ? titular1 : titular2;
    }

    private String textoSeguro(String value) {
        return value == null ? "" : value.trim();
    }

    private void configurarTablaNotificacion() {
        jTable1.setRowHeight(30);
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        ocultarColumna(0);
        ajustarColumna(1, 80, 95, 110);
        ajustarColumna(2, 90, 105, 120);
        ajustarColumna(3, 150, 185, 230);
        ajustarColumna(4, 95, 125, 165);
        ajustarColumna(5, 95, 125, 165);
        ajustarColumna(6, 90, 115, 145);
        ajustarColumna(7, 180, 260, Integer.MAX_VALUE);
        ajustarColumna(8, 90, 110, 130);
        jTable1.getColumnModel().getColumn(1).setCellRenderer(new PlazoAtencionCellRenderer());
    }

    private void ocultarColumna(int index) {
        ajustarColumna(index, 0, 0, 0);
    }

    private void ajustarColumna(int index, int min, int preferred, int max) {
        if (jTable1.getColumnModel().getColumnCount() <= index) {
            return;
        }
        TableColumn column = jTable1.getColumnModel().getColumn(index);
        column.setMinWidth(min);
        column.setPreferredWidth(preferred);
        column.setMaxWidth(max);
        if (max == 0) {
            column.setResizable(false);
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
                JPanelRegistrarExpedientesNotificacionAsignar panel = new JPanelRegistrarExpedientesNotificacionAsignar();
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
