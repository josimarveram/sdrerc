/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.expedientesAsignados;

import com.sdrerc.ui.views.expedientes.*;
import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.application.ExpedienteService;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.Enumerado.TipoSolicitud;
import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.ui.menu.MenuPrincipal;
import com.sdrerc.domain.model.Expediente.ExpedienteResponse;
import com.sdrerc.util.TextFieldRules;
import java.sql.Date;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
/**
 *
 * @author usuario
 */
public class JPanelRegistrarExpedientePorRecibido extends javax.swing.JPanel 
{
    private final ExpedienteService expedienteService;
    private final CatalogoItemService catalogoItemService;
    private Integer idExpedienteOculto = 0;
    
    /**
     * Creates new form JPanelRegistrarExpediente
     */
    public JPanelRegistrarExpedientePorRecibido() {
        initComponents();
        
        this.expedienteService = new ExpedienteService();
        this.catalogoItemService = new CatalogoItemService();
        
        TextFieldRules.apply(textNumeroDocumentoRemitente).onlyNumbers().max(8);
        TextFieldRules.apply(textApellidosNombreRemitente).onlyLetters().max(300);
        
        TextFieldRules.apply(textNumeroDocumentoSolicitante).onlyNumbers().max(8);
        TextFieldRules.apply(textApellidosNombresSolicitante).onlyLetters().max(300);
        
        TextFieldRules.apply(textNumeroDocumentoTitular).onlyNumbers().max(8);
        TextFieldRules.apply(textApellidosNombresTitular).onlyLetters().max(300);
                
        cargarComboTipoSolicitud(); 
        cargarComboTipoDocumento();
        cargarComboTipoProcedimientoRegistral(); 
        cargarComboTipoActa();
        cargarComboGrupoFamiliar();        
    }
    
    
    public void cargarExpediente(String idExpediente) throws Exception 
    {        
        Expediente lista = expedienteService.buscarporid(Integer.parseInt(idExpediente));           
        textNumeroTramiteDocumento.setText(lista.getNumeroTramiteDocumento());
        seleccionarEstadoEnCombo(cboTipoSolicitud, lista.getTipoSolicitud()); 
        seleccionarEstadoEnCombo(cboTipoDocumento, lista.getTipoDocumento()); 
        seleccionarEstadoEnCombo(cboTipoProcedimientoRegistral, lista.getTipoProcedimientoRegistral()); 
        seleccionarEstadoEnCombo(cboTipoActa, lista.getTipoActa()); 
        seleccionarEstadoEnCombo(cboGrupoFamiliar, lista.getTipoGrupoFamiliar());      
        spFechaSolicitud.setValue(lista.getFechaSolicitud());
        textNumeroDocumentoRemitente.setText(lista.getDniRemitente());
        textApellidosNombreRemitente.setText(lista.getApellidoNombreRemitente());
        textNumeroDocumentoSolicitante.setText(lista.getDniSolicitante());
        textApellidosNombresSolicitante.setText(lista.getApellidoNombreSolicitante());
        textNumeroActa.setText(lista.getNumeroActa());
        textNumeroGrupoFamiliar.setText(lista.getNumeroGrupoFamiliar());
        textNumeroDocumentoTitular.setText(lista.getDniTitular());
        textApellidosNombresTitular.setText(lista.getApellidoNombreTitular());
        idExpedienteOculto = lista.getIdExpediente();
    }
    
    private void seleccionarEstadoEnCombo(JComboBox<CatalogoItem> combo, int idEstado) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            CatalogoItem item = combo.getItemAt(i);
            if (item.getIdCatalogoItem() == idEstado) {
                combo.setSelectedIndex(i);
                break;
            }
        }
    }
    
    private void cargarComboTipoSolicitud() {
        cboTipoSolicitud.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(1);

        for (CatalogoItem catalogoitem : lista) {
            cboTipoSolicitud.addItem(catalogoitem);
        }
    }
    
    private void cargarComboTipoDocumento() {
        cboTipoDocumento.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(2);

        for (CatalogoItem catalogoitem : lista) {
            cboTipoDocumento.addItem(catalogoitem);
        }
    }
    
    private void cargarComboTipoProcedimientoRegistral() {
        cboTipoProcedimientoRegistral.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(3);

        for (CatalogoItem catalogoitem : lista) {
            cboTipoProcedimientoRegistral.addItem(catalogoitem);
        }
    }
    
    private void cargarComboTipoActa() {
        cboTipoActa.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(4);

        for (CatalogoItem catalogoitem : lista) {
            cboTipoActa.addItem(catalogoitem);
        }
    }
    
    private void cargarComboGrupoFamiliar() {
        cboGrupoFamiliar.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(6);

        for (CatalogoItem catalogoitem : lista) {
            cboGrupoFamiliar.addItem(catalogoitem);
        }
    }
    
      
        private void limpiarCampos() 
    {
        // Limpiar JTextFields
        textApellidosNombreRemitente.setText("");
        textApellidosNombresSolicitante.setText("");
        textApellidosNombresTitular.setText("");
        textNumeroActa.setText("");
        textNumeroDocumentoRemitente.setText("");
        textNumeroDocumentoSolicitante.setText("");
        textNumeroDocumentoTitular.setText("");
        textNumeroGrupoFamiliar.setText("");
        textNumeroTramiteDocumento.setText("");
        //spFechaSolicitud.setText("");

        // Resetear JComboBoxes al primer elemento
        if (cboGrupoFamiliar.getItemCount() > 0) cboGrupoFamiliar.setSelectedIndex(0);
        if (cboTipoActa.getItemCount() > 0) cboTipoActa.setSelectedIndex(0);
        if (cboTipoDocumento.getItemCount() > 0) cboTipoDocumento.setSelectedIndex(0);
        if (cboTipoProcedimientoRegistral.getItemCount() > 0) cboTipoProcedimientoRegistral.setSelectedIndex(0);
        if (cboTipoSolicitud.getItemCount() > 0) cboTipoSolicitud.setSelectedIndex(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelPrincipal = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        textNumeroTramiteDocumento = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        textNumeroDocumentoRemitente = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        textApellidosNombreRemitente = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        textNumeroDocumentoSolicitante = new javax.swing.JTextField();
        textApellidosNombresSolicitante = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        textNumeroActa = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        textNumeroGrupoFamiliar = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        textNumeroDocumentoTitular = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        textApellidosNombresTitular = new javax.swing.JTextField();
        btnGuardar = new javax.swing.JButton();
        btnRegresar = new javax.swing.JButton();
        spFechaSolicitud = new javax.swing.JSpinner();
        cboTipoSolicitud = new javax.swing.JComboBox();
        cboTipoDocumento = new javax.swing.JComboBox();
        cboTipoProcedimientoRegistral = new javax.swing.JComboBox();
        cboTipoActa = new javax.swing.JComboBox();
        cboGrupoFamiliar = new javax.swing.JComboBox();

        setPreferredSize(new java.awt.Dimension(900, 570));

        jPanelPrincipal.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText("Fecha Solicitud");

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText("Nro Tramite Documento");

        textNumeroTramiteDocumento.setEnabled(false);

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText("Tipo Solicitud");

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText("Tipo Documento");

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText("DNI / Nro Documento");

        textNumeroDocumentoRemitente.setEnabled(false);

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText("Apellidos y Nombres ciudadano / Remitente");

        textApellidosNombreRemitente.setEnabled(false);

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setText("Apellidos y Nombres Solicitante");

        textNumeroDocumentoSolicitante.setEnabled(false);

        textApellidosNombresSolicitante.setEnabled(false);

        jLabel14.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel14.setText("Tipo Procedimiento Registral");

        jLabel15.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel15.setText("Tipo Acta");

        jLabel16.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel16.setText("Nro Acta");

        textNumeroActa.setEnabled(false);

        jLabel17.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel17.setText("Grupo Familiar");

        jLabel11.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel11.setText("DNI / Nro Documento");

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText("Nro Grupo F");

        textNumeroGrupoFamiliar.setEnabled(false);

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setText("DNI / Nro Documento");

        textNumeroDocumentoTitular.setEnabled(false);

        jLabel13.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel13.setText("Apellidos y Nombres Titular");

        textApellidosNombresTitular.setEnabled(false);

        btnGuardar.setBackground(new java.awt.Color(25, 120, 210));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("ACEPTAR EXPEDIENTE");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnRegresar.setBackground(new java.awt.Color(25, 120, 210));
        btnRegresar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegresar.setForeground(new java.awt.Color(255, 255, 255));
        btnRegresar.setText("REGRESAR");
        btnRegresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresarActionPerformed(evt);
            }
        });

        spFechaSolicitud.setModel(new javax.swing.SpinnerDateModel());
        spFechaSolicitud.setEnabled(false);

        cboTipoSolicitud.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        cboTipoSolicitud.setEnabled(false);

        cboTipoDocumento.setEnabled(false);

        cboTipoProcedimientoRegistral.setEnabled(false);

        cboTipoActa.setEnabled(false);

        cboGrupoFamiliar.setEnabled(false);

        javax.swing.GroupLayout jPanelPrincipalLayout = new javax.swing.GroupLayout(jPanelPrincipal);
        jPanelPrincipal.setLayout(jPanelPrincipalLayout);
        jPanelPrincipalLayout.setHorizontalGroup(
            jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(22, 22, 22)
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(textApellidosNombresTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 640, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(textNumeroDocumentoTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 17, Short.MAX_VALUE))
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                                    .addComponent(spFechaSolicitud))
                                .addGap(37, 37, 37)
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(textNumeroTramiteDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(40, 40, 40)
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                                    .addComponent(cboTipoSolicitud, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(textNumeroDocumentoSolicitante, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .addComponent(cboTipoDocumento, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(17, 17, 17))
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(textNumeroDocumentoRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(37, 37, 37)
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(textApellidosNombreRemitente)))
                            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(298, 298, 298)
                                .addComponent(btnGuardar))
                            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cboTipoProcedimientoRegistral, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(22, 22, 22)
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(cboTipoActa, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(textNumeroActa, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                                            .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                                            .addComponent(cboGrupoFamiliar, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(textNumeroGrupoFamiliar)
                                            .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(textApellidosNombresSolicitante))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanelPrincipalLayout.setVerticalGroup(
            jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                            .addComponent(jLabel4)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cboTipoSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cboTipoDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                            .addComponent(jLabel3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(textNumeroTramiteDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(spFechaSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel5))
                .addGap(30, 30, 30)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textApellidosNombreRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textNumeroDocumentoRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(textApellidosNombresSolicitante, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textNumeroDocumentoSolicitante, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cboTipoActa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cboTipoProcedimientoRegistral, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(textNumeroActa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel14)
                                    .addComponent(jLabel17))
                                .addGap(46, 46, 46))))
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(textNumeroGrupoFamiliar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cboGrupoFamiliar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(14, 14, 14)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(textApellidosNombresTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textNumeroDocumentoTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(38, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jPanelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
        // TODO add your handling code here:        
        MenuPrincipal.ShowJPanel(new JPanelRegistroExpediente());
    }//GEN-LAST:event_btnRegresarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
                                                  
        try 
        {
            Expediente expediente = new Expediente();

            // FECHA
            //expediente.setFechaSolicitud(java.sql.Date.valueOf(textfechaSolicitud.getText())); 
            expediente.setNumeroTramiteDocumento(textNumeroTramiteDocumento.getText());

            // COMBO: tipoSolicitud
            CatalogoItem catalogoTipoSolicitud = (CatalogoItem) cboTipoSolicitud.getSelectedItem();
            int idTipoSolicitud = catalogoTipoSolicitud.getIdCatalogoItem();            
            expediente.setTipoSolicitud(idTipoSolicitud);

            // COMBO: tipoDocumento
            CatalogoItem catalogoTipoDocumento = (CatalogoItem) cboTipoDocumento.getSelectedItem();
            int idTipoDocumento = catalogoTipoDocumento.getIdCatalogoItem();            
            expediente.setTipoDocumento(idTipoDocumento);

            expediente.setDniRemitente(textNumeroDocumentoRemitente.getText());
            expediente.setApellidoNombreRemitente(textApellidosNombreRemitente.getText());

            expediente.setDniSolicitante(textNumeroDocumentoSolicitante.getText());
            expediente.setApellidoNombreSolicitante(textApellidosNombresSolicitante.getText());

            // COMBO: tipo Procedimiento Registral       
            CatalogoItem catalogoTipoProcedimientoRegistral = (CatalogoItem) cboTipoProcedimientoRegistral.getSelectedItem();
            int idTipoProcedimientoRegistral = catalogoTipoProcedimientoRegistral.getIdCatalogoItem();
            expediente.setTipoProcedimientoRegistral(idTipoProcedimientoRegistral);
              

            // COMBO: tipo acta
            CatalogoItem catalogoTipoActa = (CatalogoItem) cboTipoActa.getSelectedItem();
            int idTipoActa = catalogoTipoActa.getIdCatalogoItem();
            expediente.setTipoActa(idTipoActa);

            expediente.setNumeroActa(textNumeroActa.getText());

            // COMBO: tipo grupo familiar
            CatalogoItem catalogoGrupoFamiliar = (CatalogoItem) cboGrupoFamiliar.getSelectedItem();
            int idGrupoFamiliar = catalogoGrupoFamiliar.getIdCatalogoItem();
            expediente.setTipoGrupoFamiliar(idGrupoFamiliar);

            expediente.setNumeroGrupoFamiliar(textNumeroGrupoFamiliar.getText());

            expediente.setDniTitular(textNumeroDocumentoTitular.getText());
            expediente.setApellidoNombreTitular(textApellidosNombresTitular.getText());

            // ESTADO
            expediente.setEstado(8);

            // Auditoría
            expediente.setIdUsuarioCrea(1);
            //expediente.setFechaRegistra(new Date());
                                             
            ExpedienteResponse response;            
            // Llamar al servicio
            if(idExpedienteOculto == 0)
            {                
                response = expedienteService.agregarExpediente(expediente);
                JOptionPane.showMessageDialog(this,
                    "Expediente registrado correctamente.\nID generado: " + response.getIdExpediente(),
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            }                
            else
            {
                expediente.setIdExpediente(idExpedienteOculto);
                response = expedienteService.actualizarExpediente(expediente);
                JOptionPane.showMessageDialog(this,
                    "Expediente actualizo correctamente.\nID generado: " + response.getIdExpediente(),
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                
                idExpedienteOculto = 0;
            }                     
            limpiarCampos();
        } 
        catch (Exception ex) 
        {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_btnGuardarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JComboBox cboGrupoFamiliar;
    private javax.swing.JComboBox cboTipoActa;
    private javax.swing.JComboBox cboTipoDocumento;
    private javax.swing.JComboBox cboTipoProcedimientoRegistral;
    private javax.swing.JComboBox cboTipoSolicitud;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanelPrincipal;
    private javax.swing.JSpinner spFechaSolicitud;
    private javax.swing.JTextField textApellidosNombreRemitente;
    private javax.swing.JTextField textApellidosNombresSolicitante;
    private javax.swing.JTextField textApellidosNombresTitular;
    private javax.swing.JTextField textNumeroActa;
    private javax.swing.JTextField textNumeroDocumentoRemitente;
    private javax.swing.JTextField textNumeroDocumentoSolicitante;
    private javax.swing.JTextField textNumeroDocumentoTitular;
    private javax.swing.JTextField textNumeroGrupoFamiliar;
    private javax.swing.JTextField textNumeroTramiteDocumento;
    // End of variables declaration//GEN-END:variables
}
