/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.expedientes;

import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.application.ExpedienteService;
import com.sdrerc.application.UbigeoService;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.Departamento;
import com.sdrerc.domain.model.Enumerado.TipoSolicitud;
import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.ui.menu.MenuPrincipal;
import com.sdrerc.domain.model.Expediente.ExpedienteResponse;
import com.sdrerc.domain.model.Provincia;
import com.sdrerc.util.TextFieldRules;
import java.sql.Date;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
/**
 *
 * @author usuario
 */
public class JPanelRegistrarExpediente extends javax.swing.JPanel 
{
    private final ExpedienteService expedienteService;
    private final CatalogoItemService catalogoItemService;
    private final UbigeoService ubigeoService;
    private Integer idExpedienteOculto = 0;
    
    /**
     * Creates new form JPanelRegistrarExpediente
     */
    public JPanelRegistrarExpediente() {
        initComponents();
        
        this.expedienteService = new ExpedienteService();
        this.catalogoItemService = new CatalogoItemService();
        this.ubigeoService = new UbigeoService();
        
        TextFieldRules.apply(textDniRemitente).onlyNumbers().max(8);
        TextFieldRules.apply(textApellidosNombreRemitente).onlyLetters().max(300);
        
        TextFieldRules.apply(textNumeroDocumentoTitular).onlyNumbers().max(8);
        TextFieldRules.apply(textApellidosNombreTitular).onlyLetters().max(300);
        
        TextFieldRules.apply(textCelular).onlyNumbers().max(9);
        
                
        cargarComboTipoSolicitud(); 
        cargarComboTipoDocumento();
        cargarComboTipoProcedimientoRegistral(); 
        cargarComboTipoActa();
        cargarComboGrupoFamiliar();
        cargarComboParentesco();
        cargarComboDireccionDomiciliaria();
        cargarComboUnidadOrganica();
        
        cargarDepartamentos();

        cboProvincia.setEnabled(false);
        cboDistrito.setEnabled(false);
        
        registrarEventos();
        
        textDniRemitente.setEnabled(false);
        textApellidosNombreRemitente.setEnabled(false);
        cboUnidadOrganica.setEnabled(false);
        
        ButtonGroup grupoCorrespondeSdrerc = new ButtonGroup();
        grupoCorrespondeSdrerc.add(jRadiButonSiCorresponde);
        grupoCorrespondeSdrerc.add(jRadiButonNoCorresponde);
    }
    
    
    private void registrarEventos() {

        cboDepartamento.addActionListener(e -> {
            if (cboDepartamento.getSelectedIndex() != -1) {
                cboProvincia.setEnabled(true);
                cargarProvincias();
            }
        });

        cboProvincia.addActionListener(e -> {
            if (cboProvincia.getSelectedIndex() != -1) {
                cboDistrito.setEnabled(true);
                cargarDistritos();
            }
        });
      
        /*
        cboTipoSolicitud.addActionListener(e -> {
            if (cboTipoSolicitud.getSelectedIndex() == 1) {
                textDniRemitente.setEnabled(true);
                textApellidosNombreRemitente.setEnabled(true);
                cboUnidadOrganica.setEnabled(false);
            }else{
                textDniRemitente.setEnabled(false);
                textApellidosNombreRemitente.setEnabled(false);
                cboUnidadOrganica.setEnabled(true);
            }            
        });
        */
        
    }
    
    private void cargarDepartamentos() {
        cboDepartamento.removeAllItems();
        ubigeoService.listarDepartamentos()
                     .forEach(cboDepartamento::addItem);
    }
    
    private void cargarProvincias() {
        cboProvincia.removeAllItems();
        cboDistrito.removeAllItems();

        Departamento d = (Departamento) cboDepartamento.getSelectedItem();
        if (d == null) return;

        ubigeoService.listarProvincias(d.getIdDepartamento())
                     .forEach(cboProvincia::addItem);

        cboProvincia.setSelectedIndex(-1);
    }
    
    private void cargarDistritos() {
        cboDistrito.removeAllItems();

        Provincia p = (Provincia) cboProvincia.getSelectedItem();
        if (p == null) return;

        ubigeoService.listarDistritos(p.getIdProvincia())
                     .forEach(cboDistrito::addItem);

        cboDistrito.setSelectedIndex(-1);
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
        textDniRemitente.setText(lista.getDniRemitente());
        textApellidosNombreRemitente.setText(lista.getApellidoNombreRemitente());
        textNumeroActa.setText(lista.getNumeroActa());
        textApellidosNombreTitular.setText(lista.getDniTitular());
        textNumeroDocumentoTitular.setText(lista.getApellidoNombreTitular());
        idExpedienteOculto = lista.getIdExpediente();
    }
    
    private void seleccionarEstadoEnCombo(JComboBox<CatalogoItem> combo, int idEstado) 
    {
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
    
    private void cargarComboParentesco() {
        cboGradoParentesco.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(7);

        for (CatalogoItem catalogoitem : lista) {
            cboGradoParentesco.addItem(catalogoitem);
        }
    }
    
    private void cargarComboDireccionDomiciliaria() {
        cboDireccionDomiciliaria.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(8);

        for (CatalogoItem catalogoitem : lista) {
            cboDireccionDomiciliaria.addItem(catalogoitem);
        }
    }
    
    private void cargarComboUnidadOrganica() {
        cboUnidadOrganica.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(9);

        for (CatalogoItem catalogoitem : lista) {
            cboUnidadOrganica.addItem(catalogoitem);
        }
    }
    
      
    private void limpiarCampos() 
    {
        // Limpiar JTextFields
        textApellidosNombreRemitente.setText("");
        textNumeroDocumentoTitular.setText("");
        textNumeroActa.setText("");
        textDniRemitente.setText("");
        textApellidosNombreTitular.setText("");
        textNumeroTramiteDocumento.setText("");
        //spFechaSolicitud.setText("");

        // Resetear JComboBoxes al primer elemento
        if (cboGrupoFamiliar.getItemCount() > 0) cboGrupoFamiliar.setSelectedIndex(0);
        if (cboTipoActa.getItemCount() > 0) cboTipoActa.setSelectedIndex(0);
        if (cboTipoDocumento.getItemCount() > 0) cboTipoDocumento.setSelectedIndex(0);
        if (cboTipoProcedimientoRegistral.getItemCount() > 0) cboTipoProcedimientoRegistral.setSelectedIndex(0);
        if (cboTipoSolicitud.getItemCount() > 0) cboTipoSolicitud.setSelectedIndex(0);
    }

    
    private void validarGuardar()
    {
        //esRegistroSdrerc 
        if (jRadiButonNoCorresponde.isSelected() && textHojaEnvioExpediente.getText().trim().isEmpty()) 
        {
            JOptionPane.showMessageDialog(this,"Debe ingresar la Hoja de Envío");textHojaEnvioExpediente.requestFocus();
            return;
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

        jPanelPrincipal = new javax.swing.JPanel();
        jPanelDatosSolicitud = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        spFechaSolicitud = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cboTipoSolicitud = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        cboTipoDocumento = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        spFechaRecepcion = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        textNumeroDocumento = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        cboTipoActa = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        textNumeroActa = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        cboGrupoFamiliar = new javax.swing.JComboBox();
        jLabel18 = new javax.swing.JLabel();
        cboGradoParentesco = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        textNumeroDocumentoTitular = new javax.swing.JTextField();
        textApellidosNombreTitular = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        cboUnidadOrganica = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        textDniRemitente = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        textApellidosNombreRemitente = new javax.swing.JTextField();
        textNumeroTramiteDocumento = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        cboTipoProcedimientoRegistral = new javax.swing.JComboBox();
        jPanelParaNotificacion = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        textCorreoElectronico = new javax.swing.JTextField();
        textCelular = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        textDomicilio = new javax.swing.JTextField();
        cboDireccionDomiciliaria = new javax.swing.JComboBox();
        jLabel26 = new javax.swing.JLabel();
        btnRegresar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        jPanelDatosUbicacion = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        cboDepartamento = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        cboProvincia = new javax.swing.JComboBox();
        jLabel24 = new javax.swing.JLabel();
        cboDistrito = new javax.swing.JComboBox();
        jPanelCorrespondeSdrerc = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        textHojaEnvioExpediente = new javax.swing.JTextField();
        jRadiButonSiCorresponde = new javax.swing.JRadioButton();
        jRadiButonNoCorresponde = new javax.swing.JRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jPanelPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        jPanelPrincipal.setPreferredSize(new java.awt.Dimension(908, 558));

        jPanelDatosSolicitud.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosSolicitud.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos de la solicitud"));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText("Fecha Solicitud ");

        spFechaSolicitud.setModel(new javax.swing.SpinnerDateModel());

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText("Nro. Tramite Web");

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText("Tipo Solicitud");

        cboTipoSolicitud.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        cboTipoSolicitud.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTipoSolicitudActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText("Tipo Documento");

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setText("Fecha Recepción ");

        spFechaRecepcion.setModel(new javax.swing.SpinnerDateModel());

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setText("Nro. Documento");

        jLabel15.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel15.setText("Tipo Acta");

        jLabel16.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel16.setText("Nro Acta");

        jLabel17.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel17.setText("Grupo Familiar");

        jLabel18.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel18.setText("Grado de Parentesco");

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setText("DNI / Nro Documento");

        jLabel13.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel13.setText("Apellidos y Nombres Titular");

        jLabel20.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel20.setText("Unidad Organica");

        cboUnidadOrganica.setEnabled(false);

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText("DNI");

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText("Apellidos y Nombres Remitente");

        textApellidosNombreRemitente.setEnabled(false);

        jLabel14.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel14.setText("Tipo Procedimiento Registral");

        javax.swing.GroupLayout jPanelDatosSolicitudLayout = new javax.swing.GroupLayout(jPanelDatosSolicitud);
        jPanelDatosSolicitud.setLayout(jPanelDatosSolicitudLayout);
        jPanelDatosSolicitudLayout.setHorizontalGroup(
            jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(67, 67, 67)
                        .addComponent(jLabel9))
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(spFechaRecepcion, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(spFechaSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(textNumeroTramiteDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(cboTipoDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(textNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(109, 109, 109)
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(117, 117, 117)
                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(cboTipoActa, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(textNumeroActa, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(cboGrupoFamiliar, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(cboGradoParentesco, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(cboTipoProcedimientoRegistral, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(52, 52, 52)
                        .addComponent(jLabel6)
                        .addGap(118, 118, 118)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(90, 90, 90)
                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(51, 51, 51)
                                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                                .addComponent(cboTipoSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(textDniRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(textApellidosNombreRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(cboUnidadOrganica, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                                .addComponent(textNumeroDocumentoTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(textApellidosNombreTitular)))))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        jPanelDatosSolicitudLayout.setVerticalGroup(
            jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jLabel8))
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spFechaRecepcion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spFechaSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textNumeroTramiteDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTipoDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18)
                    .addComponent(jLabel14))
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboTipoActa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textNumeroActa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboGrupoFamiliar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboGradoParentesco, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTipoProcedimientoRegistral, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel20))
                .addGap(3, 3, 3)
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboTipoSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textDniRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textApellidosNombreRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboUnidadOrganica, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(jLabel13))
                .addGap(3, 3, 3)
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textNumeroDocumentoTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textApellidosNombreTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanelParaNotificacion.setBackground(new java.awt.Color(255, 255, 255));
        jPanelParaNotificacion.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos para Notificación"));

        jLabel21.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel21.setText("Correo Electrónico");

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText("Celular");

        jLabel23.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel23.setText("Domicilio");

        cboDireccionDomiciliaria.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        jLabel26.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel26.setText("Dirección Domiciliaria");

        javax.swing.GroupLayout jPanelParaNotificacionLayout = new javax.swing.GroupLayout(jPanelParaNotificacion);
        jPanelParaNotificacion.setLayout(jPanelParaNotificacionLayout);
        jPanelParaNotificacionLayout.setHorizontalGroup(
            jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParaNotificacionLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textCorreoElectronico, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(47, 47, 47)
                .addGroup(jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textCelular, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboDireccionDomiciliaria, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(44, 44, 44)
                .addGroup(jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textDomicilio, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15))
        );
        jPanelParaNotificacionLayout.setVerticalGroup(
            jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParaNotificacionLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21)
                    .addComponent(jLabel23))
                .addGap(3, 3, 3)
                .addGroup(jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textCorreoElectronico, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textDomicilio, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelParaNotificacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelParaNotificacionLayout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addGap(3, 3, 3)
                        .addComponent(textCelular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelParaNotificacionLayout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addGap(3, 3, 3)
                        .addComponent(cboDireccionDomiciliaria, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        btnRegresar.setBackground(new java.awt.Color(25, 120, 210));
        btnRegresar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegresar.setForeground(new java.awt.Color(255, 255, 255));
        btnRegresar.setText("REGRESAR");
        btnRegresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresarActionPerformed(evt);
            }
        });

        btnLimpiar.setBackground(new java.awt.Color(25, 120, 210));
        btnLimpiar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLimpiar.setForeground(new java.awt.Color(255, 255, 255));
        btnLimpiar.setText("LIMPIAR");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        btnGuardar.setBackground(new java.awt.Color(25, 120, 210));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("GUARDAR");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        jPanelDatosUbicacion.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosUbicacion.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos Ubicación"));

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setText("Departamento");

        cboDepartamento.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel11.setText("Provincia");

        cboProvincia.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        jLabel24.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel24.setText("Distrito");

        cboDistrito.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        javax.swing.GroupLayout jPanelDatosUbicacionLayout = new javax.swing.GroupLayout(jPanelDatosUbicacion);
        jPanelDatosUbicacion.setLayout(jPanelDatosUbicacionLayout);
        jPanelDatosUbicacionLayout.setHorizontalGroup(
            jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosUbicacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboDepartamento, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(160, 160, 160)
                .addGroup(jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboProvincia, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboDistrito, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(128, 128, 128))
        );
        jPanelDatosUbicacionLayout.setVerticalGroup(
            jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelDatosUbicacionLayout.createSequentialGroup()
                .addContainerGap(11, Short.MAX_VALUE)
                .addGroup(jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelDatosUbicacionLayout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addGap(3, 3, 3)
                        .addComponent(cboDistrito, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosUbicacionLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(3, 3, 3)
                        .addComponent(cboProvincia, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosUbicacionLayout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(3, 3, 3)
                        .addComponent(cboDepartamento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanelCorrespondeSdrerc.setBackground(new java.awt.Color(255, 255, 255));
        jPanelCorrespondeSdrerc.setBorder(javax.swing.BorderFactory.createTitledBorder("¿Corresponde a la SDRERC?"));
        jPanelCorrespondeSdrerc.setPreferredSize(new java.awt.Dimension(880, 100));

        jLabel25.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel25.setText("Hoja de Envio");

        textHojaEnvioExpediente.setEnabled(false);

        jRadiButonSiCorresponde.setSelected(true);
        jRadiButonSiCorresponde.setText("Si corresponde a la SDRERC");
        jRadiButonSiCorresponde.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadiButonSiCorrespondeActionPerformed(evt);
            }
        });

        jRadiButonNoCorresponde.setText("No corresponde a la SDRERC");
        jRadiButonNoCorresponde.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadiButonNoCorrespondeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelCorrespondeSdrercLayout = new javax.swing.GroupLayout(jPanelCorrespondeSdrerc);
        jPanelCorrespondeSdrerc.setLayout(jPanelCorrespondeSdrercLayout);
        jPanelCorrespondeSdrercLayout.setHorizontalGroup(
            jPanelCorrespondeSdrercLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCorrespondeSdrercLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelCorrespondeSdrercLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jRadiButonSiCorresponde, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRadiButonNoCorresponde, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE))
                .addGap(34, 34, 34)
                .addGroup(jPanelCorrespondeSdrercLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelCorrespondeSdrercLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(textHojaEnvioExpediente, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelCorrespondeSdrercLayout.setVerticalGroup(
            jPanelCorrespondeSdrercLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelCorrespondeSdrercLayout.createSequentialGroup()
                .addComponent(jLabel25)
                .addGap(3, 3, 3)
                .addComponent(textHojaEnvioExpediente, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanelCorrespondeSdrercLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jRadiButonSiCorresponde)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadiButonNoCorresponde)
                .addContainerGap(11, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelPrincipalLayout = new javax.swing.GroupLayout(jPanelPrincipal);
        jPanelPrincipal.setLayout(jPanelPrincipalLayout);
        jPanelPrincipalLayout.setHorizontalGroup(
            jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelDatosUbicacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelParaNotificacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelDatosSolicitud, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelCorrespondeSdrerc, javax.swing.GroupLayout.DEFAULT_SIZE, 1034, Short.MAX_VALUE)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(270, 270, 270)
                        .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanelPrincipalLayout.setVerticalGroup(
            jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelCorrespondeSdrerc, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelDatosSolicitud, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelParaNotificacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelDatosUbicacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 1040, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
        limpiarCampos();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
        // TODO add your handling code here:        
        MenuPrincipal.ShowJPanel(new JPanelListadoRegistroExpediente());
    }//GEN-LAST:event_btnRegresarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
                                                  
        try 
        {
            Expediente expediente = new Expediente();  
            this.validarGuardar();

            //esRegistroSdrerc 
            int noPertenece = jRadiButonNoCorresponde.isSelected()? 1 : 0;
            expediente.setEsRegistroSdrerc(noPertenece);            
            
            //hojaEnvioExpediente
            expediente.setHojaEnvioExpediente(textHojaEnvioExpediente.getText());            
            
            //numeroTramiteDocumento 
            expediente.setNumeroTramiteDocumento(textNumeroTramiteDocumento.getText());
            
            //fechaRecepcion
            //fechaSolicitud
            
            //tipoDocumento
            CatalogoItem catalogoTipoDocumento = (CatalogoItem) cboTipoDocumento.getSelectedItem();
            int idTipoDocumento = catalogoTipoDocumento.getIdCatalogoItem();            
            expediente.setTipoDocumento(idTipoDocumento);
           
            //numeroDocumento
            expediente.setNumeroDocumento(textNumeroDocumento.getText());
            
            //tipoActa
            CatalogoItem catalogoTipoActa = (CatalogoItem) cboTipoActa.getSelectedItem();
            int idTipoActa = catalogoTipoActa.getIdCatalogoItem();
            expediente.setTipoActa(idTipoActa);
            
            //numeroActa
            expediente.setNumeroActa(textNumeroActa.getText());
            
            //tipoGrupoFamiliar
            CatalogoItem catalogoGrupoFamiliar = (CatalogoItem) cboGrupoFamiliar.getSelectedItem();
            int idGrupoFamiliar = catalogoGrupoFamiliar.getIdCatalogoItem();
            expediente.setTipoGrupoFamiliar(idGrupoFamiliar);
            
            //gradoParentesco
            CatalogoItem catalogoGradoParentesco = (CatalogoItem) cboGradoParentesco.getSelectedItem();
            int idgradoParentesco = catalogoGradoParentesco.getIdCatalogoItem();
            expediente.setGradoParentesco(idgradoParentesco);  //// MODIFICARRRRRRRRRRRRRRRRRRR
            
            //tipoProcedimientoRegistral
            CatalogoItem catalogoTipoProcedimientoRegistral = (CatalogoItem) cboTipoProcedimientoRegistral.getSelectedItem();
            int idTipoProcedimientoRegistral = catalogoTipoProcedimientoRegistral.getIdCatalogoItem();
            expediente.setTipoProcedimientoRegistral(idTipoProcedimientoRegistral);
            
            //tipoSolicitud
            CatalogoItem catalogoTipoSolicitud = (CatalogoItem) cboTipoSolicitud.getSelectedItem();
            int idTipoSolicitud = catalogoTipoSolicitud.getIdCatalogoItem();            
            expediente.setTipoSolicitud(idTipoSolicitud);
            
            //dniRemitente
            expediente.setDniRemitente(textDniRemitente.getText());
            
            //apellidoNombreRemitente
            expediente.setApellidoNombreRemitente(textApellidosNombreRemitente.getText());
            
            //unidadOrganica
            CatalogoItem catalogoUnidadOrganica = (CatalogoItem) cboUnidadOrganica.getSelectedItem();
            int idUnidadOrganica = catalogoUnidadOrganica.getIdCatalogoItem();            
            expediente.setUnidadOrganica(idUnidadOrganica);  //// MODIFICARRRRRRRRRRRRRRRRRRR
            
            //dniTitular
            expediente.setDniTitular(textNumeroDocumentoTitular.getText());
            
            //apellidoNombreTitular
            expediente.setApellidoNombreTitular(textApellidosNombreTitular.getText());
            
            //departamento
            CatalogoItem catalogoDepartamento = (CatalogoItem) cboDepartamento.getSelectedItem();
            int idDepartamento = catalogoDepartamento.getIdCatalogoItem();            
            expediente.setDepartamento(idDepartamento);  //// MODIFICARRRRRRRRRRRRRRRRRRR
            
            //provincia
            CatalogoItem catalogoProvincia = (CatalogoItem) cboProvincia.getSelectedItem();
            int idProvincia = catalogoProvincia.getIdCatalogoItem();            
            expediente.setProvincia(idProvincia);
            
            //distrito
            CatalogoItem catalogoDistrito = (CatalogoItem) cboDistrito.getSelectedItem();
            int idDistrito = catalogoDistrito.getIdCatalogoItem();            
            expediente.setDistrito(idDistrito);             //// MODIFICARRRRRRRRRRRRRRRRRRR
            
            //direccionDomiciliaria
            CatalogoItem catalogoDireccionDomiciliaria = (CatalogoItem) cboDireccionDomiciliaria.getSelectedItem();
            int idDireccionDomiciliaria = catalogoDireccionDomiciliaria.getIdCatalogoItem();            
            expediente.setDireccionDomiciliaria(idDireccionDomiciliaria);  //// MODIFICARRRRRRRRRRRRRRRRRRR
            
            //domicilio
            expediente.setDomicilio(textDomicilio.getText());
             
            //correoElectronico
            expediente.setCorreoElectronico(textCorreoElectronico.getText());
            
            //celular
            expediente.setCelular(textCelular.getText());
            
            
                                             
            ExpedienteResponse response;              
            
            if(idExpedienteOculto == 0)
            {
                //idUsuarioCrea
                //fechaRegistra      
                
                //estado
                expediente.setEstado(56);
                
                response = expedienteService.agregarExpediente(expediente);
                JOptionPane.showMessageDialog(this,
                    "Expediente registrado correctamente.\nID generado: " + response.getIdExpediente(),
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            }                
            else
            {
                //idExpediente
                expediente.setIdExpediente(idExpedienteOculto);                
                //idUsuarioModifica
                //fechaModifica
                
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

    private void jRadiButonSiCorrespondeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadiButonSiCorrespondeActionPerformed
        // TODO add your handling code here:
        textHojaEnvioExpediente.setText("");      // limpiar
        textHojaEnvioExpediente.setEnabled(false);
    }//GEN-LAST:event_jRadiButonSiCorrespondeActionPerformed

    private void jRadiButonNoCorrespondeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadiButonNoCorrespondeActionPerformed
        // TODO add your handling code here:
        textHojaEnvioExpediente.setEnabled(true);
        textHojaEnvioExpediente.requestFocus();
    }//GEN-LAST:event_jRadiButonNoCorrespondeActionPerformed

    private void cboTipoSolicitudActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTipoSolicitudActionPerformed
        
        CatalogoItem catalogoTipoSolicitud = (CatalogoItem) cboTipoSolicitud.getSelectedItem();
        int idTipoSolicitud = catalogoTipoSolicitud.getIdCatalogoItem(); 
        
        if(idTipoSolicitud == 10)
        {
          textDniRemitente.setEnabled(true);
          textApellidosNombreRemitente.setEnabled(true);
          cboUnidadOrganica.setEnabled(false);
        }
        else
        {
           textDniRemitente.setEnabled(false);
           textApellidosNombreRemitente.setEnabled(false);
           cboUnidadOrganica.setEnabled(true); 
        }
    }//GEN-LAST:event_cboTipoSolicitudActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JComboBox cboDepartamento;
    private javax.swing.JComboBox cboDireccionDomiciliaria;
    private javax.swing.JComboBox cboDistrito;
    private javax.swing.JComboBox cboGradoParentesco;
    private javax.swing.JComboBox cboGrupoFamiliar;
    private javax.swing.JComboBox cboProvincia;
    private javax.swing.JComboBox cboTipoActa;
    private javax.swing.JComboBox cboTipoDocumento;
    private javax.swing.JComboBox cboTipoProcedimientoRegistral;
    private javax.swing.JComboBox cboTipoSolicitud;
    private javax.swing.JComboBox cboUnidadOrganica;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanelCorrespondeSdrerc;
    private javax.swing.JPanel jPanelDatosSolicitud;
    private javax.swing.JPanel jPanelDatosUbicacion;
    private javax.swing.JPanel jPanelParaNotificacion;
    private javax.swing.JPanel jPanelPrincipal;
    private javax.swing.JRadioButton jRadiButonNoCorresponde;
    private javax.swing.JRadioButton jRadiButonSiCorresponde;
    private javax.swing.JSpinner spFechaRecepcion;
    private javax.swing.JSpinner spFechaSolicitud;
    private javax.swing.JTextField textApellidosNombreRemitente;
    private javax.swing.JTextField textApellidosNombreTitular;
    private javax.swing.JTextField textCelular;
    private javax.swing.JTextField textCorreoElectronico;
    private javax.swing.JTextField textDniRemitente;
    private javax.swing.JTextField textDomicilio;
    private javax.swing.JTextField textHojaEnvioExpediente;
    private javax.swing.JTextField textNumeroActa;
    private javax.swing.JTextField textNumeroDocumento;
    private javax.swing.JTextField textNumeroDocumentoTitular;
    private javax.swing.JTextField textNumeroTramiteDocumento;
    // End of variables declaration//GEN-END:variables
}
