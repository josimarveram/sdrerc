/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.asignacion;

import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.application.ExpedienteAsignacionService;
import com.sdrerc.application.ExpedienteService;
import com.sdrerc.application.TecnicoService;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.Enumerado;
import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.domain.model.Tecnico;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author David
 */
public class JPanelRegistroAsignacionOlds extends javax.swing.JPanel {

    /**
     * Creates new form JPanelRegistroAsignacionOlds
     */
    private final ExpedienteService expedienteService;    
    private final CatalogoItemService catalogoItemService;
    private final TecnicoService tecnicoService;
    private final ExpedienteAsignacionService expedienteAsignacionService;
    private Integer idExpedienteOculto;
    
    public JPanelRegistroAsignacionOlds() {
        initComponents();
        this.expedienteService = new ExpedienteService();
        this.catalogoItemService = new CatalogoItemService();
        this.tecnicoService = new TecnicoService();
        this.expedienteAsignacionService = new ExpedienteAsignacionService();
        cargarComboGrupoFamiliar();
        cargarComboTipoActa();
        cargarComboTipoDocumento();
        cargarComboTipoProcedimientoRegistral();
        cargarComboTipoSolicitud();
        bloquearCamposTecnico();
    }
    
    private void bloquearCamposTecnico() {
        txtIdTecnico.setEditable(false);
        txtNombreTecnico.setEditable(false);
        
        txtIdTecnico.setEnabled(false);
        txtNombreTecnico.setEnabled(false);
    }
    
    public void cargarExpediente(String idExpediente) throws Exception {        
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
        textNumeroActa.setText(lista.getNumeroActa());
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
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanelPrincipal = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        textNumeroTramiteDocumento = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        textNumeroDocumentoRemitente = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        textApellidosNombreRemitente = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        textNumeroDocumentoSolicitante = new javax.swing.JTextField();
        textApellidosNombresSolicitante = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        textNumeroActa = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        textNumeroGrupoFamiliar = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        textNumeroDocumentoTitular = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        textApellidosNombresTitular = new javax.swing.JTextField();
        cboTipoSolicitud = new javax.swing.JComboBox();
        cboTipoActa = new javax.swing.JComboBox();
        cboTipoDocumento = new javax.swing.JComboBox();
        cboGrupoFamiliar = new javax.swing.JComboBox();
        cboTipoProcedimientoRegistral = new javax.swing.JComboBox();
        jButton2 = new javax.swing.JButton();
        txtIdTecnico = new javax.swing.JTextField();
        txtNombreTecnico = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        spFechaAsignacion = new javax.swing.JSpinner();
        jLabel24 = new javax.swing.JLabel();
        spFechaSolicitud = new javax.swing.JSpinner();

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("NUEVO REGISTRO ASIGNACION");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jPanelPrincipal.setBackground(new java.awt.Color(255, 255, 255));

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setText("Nro Tramite Documento");

        jLabel11.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel11.setText("Tipo Solicitud");

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setText("Tipo Documento");

        jLabel13.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel13.setText("DNI / Nro Documento");

        jLabel14.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel14.setText("Apellidos y Nombres ciudadano / Remitente");

        jLabel15.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel15.setText("Apellidos y Nombres Solicitante");

        jLabel16.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel16.setText("Tipo Procedimiento Registral");

        jLabel17.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel17.setText("Tipo Acta");

        jLabel18.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel18.setText("Nro Acta");

        jLabel19.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel19.setText("Grupo Familiar");

        jLabel20.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel20.setText("DNI / Nro Documento");

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText("Nro Grupo F");

        jLabel21.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel21.setText("DNI / Nro Documento");

        jLabel23.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel23.setText("Apellidos y Nombres Titular");

        cboTipoSolicitud.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        jButton2.setText("Seleccionar Abogado");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton1.setText("GENERAR ASIGNACIÓN");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel8.setText("Fecha de Asignación:");

        spFechaAsignacion.setModel(new javax.swing.SpinnerDateModel());

        jLabel24.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel24.setText("Fecha Solicitud");

        spFechaSolicitud.setModel(new javax.swing.SpinnerDateModel());

        javax.swing.GroupLayout jPanelPrincipalLayout = new javax.swing.GroupLayout(jPanelPrincipal);
        jPanelPrincipal.setLayout(jPanelPrincipalLayout);
        jPanelPrincipalLayout.setHorizontalGroup(
            jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(textNumeroDocumentoRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel24, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(37, 37, 37)
                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                .addComponent(textApellidosNombreRemitente)
                                .addGap(17, 17, 17))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelPrincipalLayout.createSequentialGroup()
                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(textNumeroDocumentoTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(22, 22, 22)
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(textApellidosNombresTitular)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelPrincipalLayout.createSequentialGroup()
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                        .addComponent(spFechaSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(45, 45, 45)
                                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(textNumeroTramiteDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(40, 40, 40)
                                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                                            .addComponent(cboTipoSolicitud, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addComponent(textNumeroDocumentoSolicitante, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                                    .addComponent(cboTipoDocumento, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelPrincipalLayout.createSequentialGroup()
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtIdTecnico, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtNombreTecnico, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spFechaAsignacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton1))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelPrincipalLayout.createSequentialGroup()
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cboTipoProcedimientoRegistral, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(22, 22, 22)
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(cboTipoActa, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(textNumeroActa, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                                            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                                            .addComponent(cboGrupoFamiliar, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(textNumeroGrupoFamiliar)))
                                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(textApellidosNombresSolicitante))))
                        .addGap(17, 17, 17))))
        );
        jPanelPrincipalLayout.setVerticalGroup(
            jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                            .addComponent(jLabel11)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cboTipoSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cboTipoDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                            .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel10)
                                .addComponent(jLabel24))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(textNumeroTramiteDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(spFechaSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel12))
                .addGap(30, 30, 30)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textApellidosNombreRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textNumeroDocumentoRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(textApellidosNombresSolicitante, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textNumeroDocumentoSolicitante, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(textNumeroActa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cboTipoActa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cboTipoProcedimientoRegistral, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel17)
                                    .addComponent(jLabel16)
                                    .addComponent(jLabel19))
                                .addGap(46, 46, 46))))
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(textNumeroGrupoFamiliar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cboGrupoFamiliar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(14, 14, 14)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textNumeroDocumentoTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textApellidosNombresTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtIdTecnico, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNombreTecnico, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(spFechaAsignacion, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(121, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 876, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (!validarFormulario()) {
            return; // Detiene el proceso si hay errores
        }

        try {
            
            //objeto EXPEDIENTE
            Expediente exp = new Expediente();
            
            CatalogoItem itemTipoProcedimientoRegistral = (CatalogoItem) cboTipoProcedimientoRegistral.getSelectedItem();
            CatalogoItem itemTipoActa = (CatalogoItem) cboTipoActa.getSelectedItem();
            CatalogoItem itemGrupoFamiliar = (CatalogoItem) cboGrupoFamiliar.getSelectedItem();
            CatalogoItem itemTipoSolicitud = (CatalogoItem) cboTipoSolicitud.getSelectedItem();
            CatalogoItem itemTipoDocumento = (CatalogoItem) cboTipoDocumento.getSelectedItem();
            
            exp.setIdExpediente(idExpedienteOculto);
            exp.setFechaSolicitud((Date) spFechaSolicitud.getValue());
            exp.setNumeroTramiteDocumento(textNumeroTramiteDocumento.getText());
            exp.setTipoSolicitud(itemTipoSolicitud.getIdCatalogoItem());
            exp.setTipoDocumento(itemTipoDocumento.getIdCatalogoItem());
            exp.setTipoProcedimientoRegistral(itemTipoProcedimientoRegistral.getIdCatalogoItem());
            exp.setTipoActa(itemTipoActa.getIdCatalogoItem());
            exp.setNumeroActa(textNumeroActa.getText());
            exp.setTipoGrupoFamiliar(itemGrupoFamiliar.getIdCatalogoItem());
            exp.setDniRemitente(textNumeroDocumentoRemitente.getText());
            exp.setApellidoNombreRemitente(textApellidosNombreRemitente.getText());
            exp.setDniTitular(textNumeroDocumentoTitular.getText());
            exp.setApellidoNombreTitular(textApellidosNombresTitular.getText());
            
            Enumerado.EstadoExpediente estadoExpedienteAsignado = Enumerado.EstadoExpediente.ExpedienteAsignado;
            exp.setEstado(estadoExpedienteAsignado.getId());
            
            exp.setIdUsuarioModifica(11);
            exp.setFechaModifica(new Date());

            ExpedienteAsignacion asignacion = new ExpedienteAsignacion();
            asignacion.setIdExpediente(idExpedienteOculto);
            asignacion.setIdTecnico(Integer.parseInt(txtIdTecnico.getText()));            
            Date fecha = (Date) spFechaAsignacion.getValue();            
            asignacion.setFechaAsignacion(fecha);
            
            expedienteAsignacionService.agregarExpediente(asignacion,exp);

            JOptionPane.showMessageDialog(this, "Asignación registrada correctamente");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private boolean validarFormulario() {

        if (spFechaSolicitud.getValue() == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar la Fecha de Solicitud.");
            spFechaSolicitud.requestFocus();
            return false;
        }

        if (textNumeroTramiteDocumento.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el Número de Trámite.");
            textNumeroTramiteDocumento.requestFocus();
            return false;
        }

        if (cboTipoSolicitud.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un Tipo de Solicitud.");
            cboTipoSolicitud.requestFocus();
            return false;
        }

        if (cboTipoDocumento.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un Tipo de Documento.");
            cboTipoDocumento.requestFocus();
            return false;
        }

        if (textNumeroDocumentoRemitente.getText().trim().length() != 8) {
            JOptionPane.showMessageDialog(this, "El DNI del Remitente debe tener 8 dígitos.");
            textNumeroDocumentoRemitente.requestFocus();
            return false;
        }

        if (textApellidosNombreRemitente.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el nombre del Remitente.");
            textApellidosNombreRemitente.requestFocus();
            return false;
        }

        if (textNumeroDocumentoSolicitante.getText().trim().length() != 8) {
            JOptionPane.showMessageDialog(this, "El DNI del Solicitante debe tener 8 dígitos.");
            textNumeroDocumentoSolicitante.requestFocus();
            return false;
        }

        if (textApellidosNombresSolicitante.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el nombre del Solicitante.");
            textApellidosNombresSolicitante.requestFocus();
            return false;
        }

        if (cboTipoActa.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar Tipo de Acta.");
            cboTipoActa.requestFocus();
            return false;
        }

        if (textNumeroActa.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar Número de Acta.");
            textNumeroActa.requestFocus();
            return false;
        }

        if (cboGrupoFamiliar.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar Grupo Familiar.");
            cboGrupoFamiliar.requestFocus();
            return false;
        }

        if (textNumeroGrupoFamiliar.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar Número de Grupo Familiar.");
            textNumeroGrupoFamiliar.requestFocus();
            return false;
        }

        if (textNumeroDocumentoTitular.getText().trim().length() != 8) {
            JOptionPane.showMessageDialog(this, "El DNI del Titular debe tener 8 dígitos.");
            textNumeroDocumentoTitular.requestFocus();
            return false;
        }

        if (textApellidosNombresTitular.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar Apellidos y Nombres del Titular.");
            textApellidosNombresTitular.requestFocus();
            return false;
        }

        if (txtNombreTecnico.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un Técnico.");
            txtNombreTecnico.requestFocus();
            return false;
        }

        return true; // Todo OK
    }
    
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // Obtener el JFrame que contiene este JPanel
        java.awt.Window parent = SwingUtilities.getWindowAncestor(this);
        
        // Crear el JDialog
        JDialogTecnico dialog = new JDialogTecnico((java.awt.Frame) parent, true);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // Recuperar los valores del técnico seleccionado
        String idTec = dialog.getIdTecnicoSeleccionado();
        String nomTec = dialog.getNombreTecnicoSeleccionado();

        if (idTec != null) {
            txtIdTecnico.setText(idTec);
            txtNombreTecnico.setText(nomTec);
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboGrupoFamiliar;
    private javax.swing.JComboBox cboTipoActa;
    private javax.swing.JComboBox cboTipoDocumento;
    private javax.swing.JComboBox cboTipoProcedimientoRegistral;
    private javax.swing.JComboBox cboTipoSolicitud;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanelPrincipal;
    private javax.swing.JSpinner spFechaAsignacion;
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
    private javax.swing.JTextField txtIdTecnico;
    private javax.swing.JTextField txtNombreTecnico;
    // End of variables declaration//GEN-END:variables
}
