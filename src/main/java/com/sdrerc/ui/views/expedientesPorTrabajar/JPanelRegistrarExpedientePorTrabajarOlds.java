/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.expedientesPorTrabajar;

import com.sdrerc.ui.views.expedientesAsignados.*;
import com.sdrerc.ui.views.expedientes.*;
import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.application.ExpedienteAsignacionService;
import com.sdrerc.application.ExpedienteService;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.Enumerado;
import com.sdrerc.domain.model.Enumerado.TipoSolicitud;
import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.ui.menu.MenuPrincipal;
import com.sdrerc.domain.model.Expediente.ExpedienteResponse;
import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.util.TextFieldRules;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.util.List;
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
public class JPanelRegistrarExpedientePorTrabajarOlds extends javax.swing.JPanel 
{
    private final ExpedienteService expedienteService;
    private final ExpedienteAsignacionService expedienteAsignacionService;
    private final CatalogoItemService catalogoItemService;
    private Integer idExpedienteOculto = 0;
    
    /**
     * Creates new form JPanelRegistrarExpediente
     */
    public JPanelRegistrarExpedientePorTrabajarOlds() {
        initComponents();
        
        this.expedienteService = new ExpedienteService();
        this.catalogoItemService = new CatalogoItemService();
        this.expedienteAsignacionService = new ExpedienteAsignacionService();
        
        TextFieldRules.apply(textNumeroDocumentoRemitente).onlyNumbers().max(8);
        TextFieldRules.apply(textApellidosNombreRemitente).onlyLetters().max(300);
        
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
    
    
      
    private void limpiarCampos() 
    {
        // Limpiar JTextFields
        textApellidosNombreRemitente.setText("");
        textApellidosNombresTitular.setText("");
        textNumeroActa.setText("");
        textNumeroDocumentoRemitente.setText("");
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
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        textNumeroTramiteDocumento = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        textNumeroDocumentoRemitente = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        textApellidosNombreRemitente = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        textNumeroActa = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        textNumeroGrupoFamiliar = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        textNumeroDocumentoTitular = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        textApellidosNombresTitular = new javax.swing.JTextField();
        btnAceptarExpediente = new javax.swing.JButton();
        btnGenerarDocumento = new javax.swing.JButton();
        spFechaSolicitud = new javax.swing.JSpinner();
        cboTipoSolicitud = new javax.swing.JComboBox();
        cboTipoDocumento = new javax.swing.JComboBox();
        cboTipoProcedimientoRegistral = new javax.swing.JComboBox();
        cboTipoActa = new javax.swing.JComboBox();
        cboGrupoFamiliar = new javax.swing.JComboBox();
        btnRegresar1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel18 = new javax.swing.JLabel();
        cboTipoProcedimientoRegistral1 = new javax.swing.JComboBox();
        btnRegresar2 = new javax.swing.JButton();
        btnRegresar3 = new javax.swing.JButton();

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

        jLabel14.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel14.setText("Tipo Procedimiento Registral");

        jLabel15.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel15.setText("Tipo Acta");

        jLabel16.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel16.setText("Nro Acta");

        textNumeroActa.setEnabled(false);

        jLabel17.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel17.setText("Grupo Familiar");

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText("Nro Grupo F");

        textNumeroGrupoFamiliar.setEnabled(false);

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setText("DNI / Nro Documento");

        textNumeroDocumentoTitular.setEnabled(false);

        jLabel13.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel13.setText("Apellidos y Nombres Titular");

        textApellidosNombresTitular.setEnabled(false);

        btnAceptarExpediente.setBackground(new java.awt.Color(25, 120, 210));
        btnAceptarExpediente.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAceptarExpediente.setForeground(new java.awt.Color(255, 255, 255));
        btnAceptarExpediente.setText("MODIFICAR");
        btnAceptarExpediente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarExpedienteActionPerformed(evt);
            }
        });

        btnGenerarDocumento.setBackground(new java.awt.Color(25, 120, 210));
        btnGenerarDocumento.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGenerarDocumento.setForeground(new java.awt.Color(255, 255, 255));
        btnGenerarDocumento.setText("GENERARPLANTILLA");
        btnGenerarDocumento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarDocumentoActionPerformed(evt);
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

        btnRegresar1.setBackground(new java.awt.Color(25, 120, 210));
        btnRegresar1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegresar1.setForeground(new java.awt.Color(255, 255, 255));
        btnRegresar1.setText("REGRESAR");
        btnRegresar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresar1ActionPerformed(evt);
            }
        });

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

        jLabel18.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel18.setText("Generar Tipo Documento");

        cboTipoProcedimientoRegistral1.setEnabled(false);

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

        javax.swing.GroupLayout jPanelPrincipalLayout = new javax.swing.GroupLayout(jPanelPrincipal);
        jPanelPrincipal.setLayout(jPanelPrincipalLayout);
        jPanelPrincipalLayout.setHorizontalGroup(
            jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(spFechaSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(textNumeroTramiteDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(cboTipoSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(cboTipoDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(jLabel7))
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(textNumeroDocumentoRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(textApellidosNombreRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 651, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(cboTipoProcedimientoRegistral, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelPrincipalLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboTipoActa, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)))
                .addComponent(textNumeroActa, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cboGrupoFamiliar, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(textNumeroGrupoFamiliar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(textNumeroDocumentoTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(textApellidosNombresTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 540, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(btnAceptarExpediente))
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(cboTipoProcedimientoRegistral1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addComponent(btnGenerarDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(52, 52, 52)
                .addComponent(btnRegresar2, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 660, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnRegresar3, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(btnRegresar1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jPanelPrincipalLayout.setVerticalGroup(
            jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addGap(6, 6, 6)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spFechaSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textNumeroTramiteDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTipoSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTipoDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addGap(6, 6, 6)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textNumeroDocumentoRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textApellidosNombreRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel14))
                    .addComponent(jLabel15)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel16))
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel17))
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel22)))
                .addGap(2, 2, 2)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(textNumeroActa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cboTipoActa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cboGrupoFamiliar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textNumeroGrupoFamiliar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cboTipoProcedimientoRegistral, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                                .addGap(39, 39, 39)
                                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel13))))))
                .addGap(6, 6, 6)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textApellidosNombresTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textNumeroDocumentoTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAceptarExpediente, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addComponent(jLabel18)
                .addGap(2, 2, 2)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(cboTipoProcedimientoRegistral1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnGenerarDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRegresar2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelPrincipalLayout.createSequentialGroup()
                        .addComponent(btnRegresar3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(btnRegresar1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
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
                .addContainerGap()
                .addComponent(jPanelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnGenerarDocumentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarDocumentoActionPerformed
        // TODO add your handling code here:        
        //MenuPrincipal.ShowJPanel(new JPanelListadoExpedientesPorTrabajar());
        
        String rutaBase = "C:\\file_server_reniec";
        String plantilla = "Carta_Edicto.docx";
        String rutaPlantilla = rutaBase + File.separator + plantilla;
        
        String tipoActa = "MATRIMONIO";
        String nroActa = textNumeroActa.getText();        
        String nombreTitular = textApellidosNombresTitular.getText();
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

    private void btnRegresar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresar1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRegresar1ActionPerformed

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

    private void btnRegresar2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresar2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRegresar2ActionPerformed

    private void btnRegresar3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresar3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRegresar3ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptarExpediente;
    private javax.swing.JButton btnGenerarDocumento;
    private javax.swing.JButton btnRegresar1;
    private javax.swing.JButton btnRegresar2;
    private javax.swing.JButton btnRegresar3;
    private javax.swing.JComboBox cboGrupoFamiliar;
    private javax.swing.JComboBox cboTipoActa;
    private javax.swing.JComboBox cboTipoDocumento;
    private javax.swing.JComboBox cboTipoProcedimientoRegistral;
    private javax.swing.JComboBox cboTipoProcedimientoRegistral1;
    private javax.swing.JComboBox cboTipoSolicitud;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanelPrincipal;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JSpinner spFechaSolicitud;
    private javax.swing.JTextField textApellidosNombreRemitente;
    private javax.swing.JTextField textApellidosNombresTitular;
    private javax.swing.JTextField textNumeroActa;
    private javax.swing.JTextField textNumeroDocumentoRemitente;
    private javax.swing.JTextField textNumeroDocumentoTitular;
    private javax.swing.JTextField textNumeroGrupoFamiliar;
    private javax.swing.JTextField textNumeroTramiteDocumento;
    // End of variables declaration//GEN-END:variables
}
