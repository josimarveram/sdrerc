/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.expedientesPorTrabajar;

import com.sdrerc.ui.views.expedientes.*;
import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.application.ExpedienteAsignacionService;
import com.sdrerc.application.ExpedienteService;
import com.sdrerc.application.UbigeoService;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.Departamento;
import com.sdrerc.domain.model.Enumerado;
import com.sdrerc.domain.model.Enumerado.TipoSolicitud;
import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.ui.menu.MenuPrincipal;
import com.sdrerc.domain.model.Expediente.ExpedienteResponse;
import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.domain.model.Provincia;
import com.sdrerc.util.TextFieldRules;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
/**
 *
 * @author usuario
 */
public class JPanelRegistrarExpedientePorTrabajar extends javax.swing.JPanel 
{
    private final ExpedienteService expedienteService;
    private final CatalogoItemService catalogoItemService;
    private final ExpedienteAsignacionService expedienteAsignacionService;
    private final UbigeoService ubigeoService;
    private Integer idExpedienteOculto = 0;
    
    /**
     * Creates new form JPanelRegistrarExpediente
     */
    public JPanelRegistrarExpedientePorTrabajar() {
        initComponents();
        
        this.expedienteService = new ExpedienteService();
        this.catalogoItemService = new CatalogoItemService();
        this.ubigeoService = new UbigeoService();
        this.expedienteAsignacionService = new ExpedienteAsignacionService();
        
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
                
        registrarEventos();
        
        textDniRemitente.setEnabled(false);
        textApellidosNombreRemitente.setEnabled(false);
        cboUnidadOrganica.setEnabled(false);        
    }
    
    
    private void registrarEventos() 
    {
        /*

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
        +/
      
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
    
    public void cargarExpediente(String idExpediente) throws Exception 
    {        
        Expediente lista = expedienteService.buscarporid(Integer.parseInt(idExpediente));           
        idExpedienteOculto = lista.getIdExpediente();
        
        //esRegistroSdrerc 
        //jRadiButonNoCorresponde.setSelected(lista.getEsRegistroSdrerc() == 1? true : false);

        //hojaEnvioExpediente
        //textHojaEnvioExpediente.setText(lista.getHojaEnvioExpediente());                          

        //numeroTramiteDocumento 
        textNumeroTramiteDocumento.setText(lista.getNumeroTramiteDocumento());

        //fechaRecepcion
        spFechaRecepcion.setValue(lista.getFechaRecepcion()); 
        
        //fechaSolicitud
        spFechaSolicitud.setValue(lista.getFechaSolicitud()); 

        //tipoDocumento
        seleccionarEstadoEnCombo(cboTipoDocumento, lista.getTipoDocumento()); 

        //numeroDocumento
        textNumeroDocumento.setText(lista.getNumeroDocumento());   

        //tipoActa
        seleccionarEstadoEnCombo(cboTipoActa, lista.getTipoActa()); 

        //numeroActa
        textNumeroActa.setText(lista.getNumeroActa());

        //tipoGrupoFamiliar
        seleccionarEstadoEnCombo(cboGrupoFamiliar, lista.getTipoGrupoFamiliar()); 

        //gradoParentesco
        seleccionarEstadoEnCombo(cboGradoParentesco, lista.getGradoParentesco()); 
        
        //tipoProcedimientoRegistral
        seleccionarEstadoEnCombo(cboTipoProcedimientoRegistral, lista.getTipoProcedimientoRegistral()); 

        //tipoSolicitud
        seleccionarEstadoEnCombo(cboTipoSolicitud, lista.getTipoSolicitud()); 

        //dniRemitente
        textDniRemitente.setText(lista.getDniRemitente());

        //apellidoNombreRemitente
        textApellidosNombreRemitente.setText(lista.getApellidoNombreRemitente());

        //unidadOrganica
        seleccionarEstadoEnCombo(cboUnidadOrganica, lista.getUnidadOrganica());

        //dniTitular
        textNumeroDocumentoTitular.setText(lista.getDniTitular());

        //apellidoNombreTitular
        textApellidosNombreTitular.setText(lista.getApellidoNombreTitular());

        //departamento
        //seleccionarEstadoEnCombo(cboDepartamento, lista.getDepartamento());
        
        //provincia
        //seleccionarEstadoEnCombo(cboProvincia, lista.getProvincia());
        
        //distrito
        //seleccionarEstadoEnCombo(cboDistrito, lista.getDistrito());

        //direccionDomiciliaria
        seleccionarEstadoEnCombo(cboDireccionDomiciliaria, lista.getDireccionDomiciliaria());

        //domicilio
        textDomicilio.setText(lista.getDomicilio());

        //correoElectronico
        textCorreoElectronico.setText(lista.getCorreoElectronico());

        //celular
        textCelular.setText(lista.getCelular());
        
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
    
    private Path generarDocxLibreOffice(String plantilla, String tipoActa, String nroActa, String nombreTitular, String dniTitular) throws Exception 
    {              
        Path tempDir = Files.createTempDirectory("docgen");
        Path copia = tempDir.resolve("documento.docx");
        Files.copy(Paths.get(plantilla), copia, StandardCopyOption.REPLACE_EXISTING);
        // Reemplazo simple (Apache POI)
        try (XWPFDocument doc = new XWPFDocument(Files.newInputStream(copia))) 
        {

            for (XWPFParagraph p : doc.getParagraphs()) {
                for (XWPFRun r : p.getRuns()) 
                {
                    String text = r.getText(0);
                    if (text != null) {
                        text = text.replace("#nroActa#", nroActa)
                                   .replace("#tipoActa#", tipoActa)
                                   .replace("#nomTitular#", nombreTitular)
                                   .replace("#dniTitular#", dniTitular);
                        r.setText(text, 0);
                    }
                }
            }
            try (OutputStream out = Files.newOutputStream(copia)) {
                doc.write(out);
            }
        }
        // Abrir documento
        //Desktop.getDesktop().open(copia.toFile());
        return copia;
    }
    
    private void guardarDocumento( Path archivoGenerado, String nombreSugerido) throws IOException 
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar documento");
        chooser.setSelectedFile(new File(nombreSugerido));

        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
        File destino = chooser.getSelectedFile();

        // asegurar extensión .docx
        if (!destino.getName().toLowerCase().endsWith(".docx")) {
            destino = new File(destino.getAbsolutePath() + ".docx");
        }

        Files.copy(
                archivoGenerado,
                destino.toPath(),
                StandardCopyOption.REPLACE_EXISTING
        );

        JOptionPane.showMessageDialog(this,
                "Documento guardado correctamente");
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
        jPanelDatosUbicacion = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        cboTipoProcedimientoRegistral1 = new javax.swing.JComboBox();
        btnGenerarDocumento = new javax.swing.JButton();
        btnRegresar2 = new javax.swing.JButton();
        btnRegresar3 = new javax.swing.JButton();
        btnRegresar1 = new javax.swing.JButton();
        btnAceptarExpediente = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();

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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        jPanelDatosUbicacion.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosUbicacion.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos Analisis"));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nª", "Tipo Documento Generado", "Documento"
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        cboTipoProcedimientoRegistral1.setEnabled(false);

        btnGenerarDocumento.setBackground(new java.awt.Color(25, 120, 210));
        btnGenerarDocumento.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGenerarDocumento.setForeground(new java.awt.Color(255, 255, 255));
        btnGenerarDocumento.setText("GENERARPLANTILLA");
        btnGenerarDocumento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarDocumentoActionPerformed(evt);
            }
        });

        btnRegresar2.setBackground(new java.awt.Color(25, 120, 210));
        btnRegresar2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegresar2.setForeground(new java.awt.Color(255, 255, 255));
        btnRegresar2.setText("CARGAR DOCUMENTO");
        btnRegresar2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresar2ActionPerformed(evt);
            }
        });

        btnRegresar3.setBackground(new java.awt.Color(25, 120, 210));
        btnRegresar3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegresar3.setForeground(new java.awt.Color(255, 255, 255));
        btnRegresar3.setText("REGISTRAR ANALISIS");
        btnRegresar3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresar3ActionPerformed(evt);
            }
        });

        btnRegresar1.setBackground(new java.awt.Color(25, 120, 210));
        btnRegresar1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegresar1.setForeground(new java.awt.Color(255, 255, 255));
        btnRegresar1.setText("REGRESAR");
        btnRegresar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresar1ActionPerformed(evt);
            }
        });

        btnAceptarExpediente.setBackground(new java.awt.Color(25, 120, 210));
        btnAceptarExpediente.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAceptarExpediente.setForeground(new java.awt.Color(255, 255, 255));
        btnAceptarExpediente.setText("MODIFICAR");
        btnAceptarExpediente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarExpedienteActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel19.setText("Generar Tipo Documento");

        javax.swing.GroupLayout jPanelDatosUbicacionLayout = new javax.swing.GroupLayout(jPanelDatosUbicacion);
        jPanelDatosUbicacion.setLayout(jPanelDatosUbicacionLayout);
        jPanelDatosUbicacionLayout.setHorizontalGroup(
            jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosUbicacionLayout.createSequentialGroup()
                .addGroup(jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosUbicacionLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelDatosUbicacionLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cboTipoProcedimientoRegistral1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btnRegresar2, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnGenerarDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(41, 41, 41)))
                .addGroup(jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosUbicacionLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 523, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnRegresar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRegresar3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelDatosUbicacionLayout.createSequentialGroup()
                        .addGap(691, 691, 691)
                        .addComponent(btnAceptarExpediente)))
                .addContainerGap())
        );
        jPanelDatosUbicacionLayout.setVerticalGroup(
            jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosUbicacionLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelDatosUbicacionLayout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addGroup(jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelDatosUbicacionLayout.createSequentialGroup()
                        .addComponent(btnAceptarExpediente, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(btnRegresar3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(btnRegresar1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelDatosUbicacionLayout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addGap(18, 18, 18)
                        .addComponent(cboTipoProcedimientoRegistral1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(31, 31, 31)
                        .addComponent(btnGenerarDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnRegresar2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15))))
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
                    .addComponent(jPanelDatosSolicitud, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanelPrincipalLayout.setVerticalGroup(
            jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelDatosSolicitud, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelParaNotificacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelDatosUbicacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                JPanelRegistrarExpedientePorTrabajarOlds panel = new JPanelRegistrarExpedientePorTrabajarOlds();
                try
                {
                    // Si el panel necesita recibir datos:
                    panel.cargarExpediente(idExpediente);
                }
                catch (Exception ex)
                {
                    //Logger.getLogger(JPanelFiltroBusqueda.class.getName()).log(Level.SEVERE, null, ex);
                }
                // Abrir formulario de edición
                MenuPrincipal.ShowJPanel(panel);
            }
        }
    }//GEN-LAST:event_jTable1MouseClicked

    private void btnGenerarDocumentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarDocumentoActionPerformed
        // TODO add your handling code here:
        //MenuPrincipal.ShowJPanel(new JPanelListadoExpedientesPorTrabajar());

        String rutaBase = "C:\\file_server_reniec";
        String plantilla = "Carta_Edicto.docx";
        String rutaPlantilla = rutaBase + File.separator + plantilla;

        String tipoActa = "MATRIMONIO";
        String nroActa = textNumeroActa.getText();
        String nombreTitular = textApellidosNombreTitular.getText();
        String dniTitular = textNumeroDocumentoTitular.getText();

        try
        {
            Path archivoGenerado =  this.generarDocxLibreOffice(rutaPlantilla,
                tipoActa,
                nroActa,
                nombreTitular,
                dniTitular
            );
            this.guardarDocumento(archivoGenerado,"documentoDescargado.docx");
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }//GEN-LAST:event_btnGenerarDocumentoActionPerformed

    private void btnRegresar2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresar2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRegresar2ActionPerformed

    private void btnRegresar3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresar3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRegresar3ActionPerformed

    private void btnRegresar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresar1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRegresar1ActionPerformed

    private void btnAceptarExpedienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarExpedienteActionPerformed

        try
        {
            ExpedienteAsignacion oExpedienteAsignacion = new ExpedienteAsignacion();

            Enumerado.EstadoExpediente estadoExpedienteRecibido = Enumerado.EstadoExpediente.ExpedienteRecibido;
            oExpedienteAsignacion.setIdEstadoExpediente(estadoExpedienteRecibido.getId());
            oExpedienteAsignacion.setIdExpediente(idExpedienteOculto);
            oExpedienteAsignacion.setAceptaRecepcion(1);
            oExpedienteAsignacion.setIdUsuarioModifica(1);
            // Llamar al servicio
            if(idExpedienteOculto == 0)
            JOptionPane.showMessageDialog(this,"Registro no puede ser actualizado" ,"Error", JOptionPane.ERROR_MESSAGE);

            expedienteAsignacionService.actualizarRecepcionExpediente(oExpedienteAsignacion);
            JOptionPane.showMessageDialog(this, "Se realizo la recepción del expediente","Éxito", JOptionPane.INFORMATION_MESSAGE);

            limpiarCampos();
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAceptarExpedienteActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptarExpediente;
    private javax.swing.JButton btnGenerarDocumento;
    private javax.swing.JButton btnRegresar1;
    private javax.swing.JButton btnRegresar2;
    private javax.swing.JButton btnRegresar3;
    private javax.swing.JComboBox cboDireccionDomiciliaria;
    private javax.swing.JComboBox cboGradoParentesco;
    private javax.swing.JComboBox cboGrupoFamiliar;
    private javax.swing.JComboBox cboTipoActa;
    private javax.swing.JComboBox cboTipoDocumento;
    private javax.swing.JComboBox cboTipoProcedimientoRegistral;
    private javax.swing.JComboBox cboTipoProcedimientoRegistral1;
    private javax.swing.JComboBox cboTipoSolicitud;
    private javax.swing.JComboBox cboUnidadOrganica;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanelDatosSolicitud;
    private javax.swing.JPanel jPanelDatosUbicacion;
    private javax.swing.JPanel jPanelParaNotificacion;
    private javax.swing.JPanel jPanelPrincipal;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JSpinner spFechaRecepcion;
    private javax.swing.JSpinner spFechaSolicitud;
    private javax.swing.JTextField textApellidosNombreRemitente;
    private javax.swing.JTextField textApellidosNombreTitular;
    private javax.swing.JTextField textCelular;
    private javax.swing.JTextField textCorreoElectronico;
    private javax.swing.JTextField textDniRemitente;
    private javax.swing.JTextField textDomicilio;
    private javax.swing.JTextField textNumeroActa;
    private javax.swing.JTextField textNumeroDocumento;
    private javax.swing.JTextField textNumeroDocumentoTitular;
    private javax.swing.JTextField textNumeroTramiteDocumento;
    // End of variables declaration//GEN-END:variables
}
