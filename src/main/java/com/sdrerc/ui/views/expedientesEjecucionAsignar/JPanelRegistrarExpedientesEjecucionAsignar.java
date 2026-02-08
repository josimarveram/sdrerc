/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.expedientesEjecucionAsignar;

import com.sdrerc.ui.views.expedientesPorVerificar.*;
import com.sdrerc.ui.views.expedientesPorTrabajar.*;
import com.sdrerc.ui.views.expedientes.*;
import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.application.ExpedienteAnalisisAbogadoService;
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
import com.sdrerc.domain.model.ExpedienteAnalisAbogadoDetDoc.ExpedienteAnalisisAbogadoDetDoc;
import com.sdrerc.domain.model.ExpedienteAnalisisAbogado.ExpedienteAnalisisAbogadoResponse;
import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.domain.model.Provincia;
import com.sdrerc.ui.views.asignacion.JDialogTecnico;
import com.sdrerc.util.TextFieldRules;
import java.awt.Component;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
/**
 *
 * @author usuario
 */
public class JPanelRegistrarExpedientesEjecucionAsignar extends javax.swing.JPanel 
{
    private final ExpedienteService expedienteService;
    private final ExpedienteAnalisisAbogadoService expedienteAnalisisAbogadoService;
    private final CatalogoItemService catalogoItemService;
    private final ExpedienteAsignacionService expedienteAsignacionService;
    private final UbigeoService ubigeoService;
    private Integer idExpedienteOculto = 0;
    
    /**
     * Creates new form JPanelRegistrarExpediente
     */
    public JPanelRegistrarExpedientesEjecucionAsignar() {
        initComponents();        
        this.expedienteService = new ExpedienteService();
        this.catalogoItemService = new CatalogoItemService();
        this.ubigeoService = new UbigeoService();
        this.expedienteAsignacionService = new ExpedienteAsignacionService();     
        this.expedienteAnalisisAbogadoService = new ExpedienteAnalisisAbogadoService();
        cargarComboTipoProcedimientoRegistral(); 
        cargarComboTipoActa();
    }
    
    public void cargarExpediente(String idExpediente) throws Exception 
    {
        
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
        jLabel8 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        textNumeroTramiteDocumento2 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        txtIdTecnico = new javax.swing.JTextField();
        txtNombreTecnico = new javax.swing.JTextField();
        cboTipoProcedimientoRegistral = new javax.swing.JComboBox();
        cboTipoActa = new javax.swing.JComboBox();
        btnRegresar = new javax.swing.JButton();
        btnGuardarAsignacionEjecucion = new javax.swing.JButton();
        jPanelDatosSolicitud2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        spFechaSolicitud2 = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cboTipoSolicitud = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        cboTipoDocumento = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        spFechaRecepcion = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        textNumeroDocumento = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        cboTipoActa1 = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        textNumeroActa = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        cboGrupoFamiliar = new javax.swing.JComboBox();
        jLabel18 = new javax.swing.JLabel();
        cboGradoParentesco = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        textNumeroDocumentoTitular = new javax.swing.JTextField();
        textApellidosNombreTitular = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        cboUnidadOrganica = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        textDniRemitente = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        textApellidosNombreRemitente = new javax.swing.JTextField();
        textNumeroTramiteDocumento = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        cboTipoProcedimientoRegistral1 = new javax.swing.JComboBox();
        jPanelParaNotificacion = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        textCorreoElectronico = new javax.swing.JTextField();
        textCelular = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        textDomicilio = new javax.swing.JTextField();
        cboDireccionDomiciliaria = new javax.swing.JComboBox();
        jLabel28 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(1060, 728));

        jPanelPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        jPanelPrincipal.setPreferredSize(new java.awt.Dimension(908, 558));
        jPanelPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanelDatosSolicitud.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosSolicitud.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos de la asignación de la ejecución"));
        jPanelDatosSolicitud.setPreferredSize(new java.awt.Dimension(1034, 329));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText("Fecha Asignación");

        spFechaSolicitud.setModel(new javax.swing.SpinnerDateModel());

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setText("Tipo acta");

        jLabel26.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel26.setText("Tipo Procedimiento");

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setText("Numero de Resolución");

        jButton2.setText("Seleccionar Abogado");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelDatosSolicitudLayout = new javax.swing.GroupLayout(jPanelDatosSolicitud);
        jPanelDatosSolicitud.setLayout(jPanelDatosSolicitudLayout);
        jPanelDatosSolicitudLayout.setHorizontalGroup(
            jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtIdTecnico, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtNombreTecnico))
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spFechaSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 68, Short.MAX_VALUE)
                        .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cboTipoActa, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(78, 78, 78)
                        .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cboTipoProcedimientoRegistral, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(37, 37, 37)
                        .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textNumeroTramiteDocumento2, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(21, 21, 21))
        );
        jPanelDatosSolicitudLayout.setVerticalGroup(
            jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(1, 1, 1)
                        .addComponent(spFechaSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboTipoProcedimientoRegistral, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addGap(1, 1, 1)
                        .addComponent(textNumeroTramiteDocumento2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cboTipoActa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtIdTecnico, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNombreTecnico, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(45, Short.MAX_VALUE))
        );

        jPanelPrincipal.add(jPanelDatosSolicitud, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 450, 1030, 210));

        btnRegresar.setBackground(new java.awt.Color(25, 120, 210));
        btnRegresar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegresar.setForeground(new java.awt.Color(255, 255, 255));
        btnRegresar.setText("REGRESAR");
        btnRegresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresarActionPerformed(evt);
            }
        });
        jPanelPrincipal.add(btnRegresar, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 670, 146, 40));

        btnGuardarAsignacionEjecucion.setBackground(new java.awt.Color(25, 120, 210));
        btnGuardarAsignacionEjecucion.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardarAsignacionEjecucion.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarAsignacionEjecucion.setText("GUARDAR ASIGNACIÖN");
        btnGuardarAsignacionEjecucion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarAsignacionEjecucionActionPerformed(evt);
            }
        });
        jPanelPrincipal.add(btnGuardarAsignacionEjecucion, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 670, 184, 40));

        jPanelDatosSolicitud2.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosSolicitud2.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos de la solicitud"));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText("Fecha Solicitud ");

        spFechaSolicitud2.setModel(new javax.swing.SpinnerDateModel());
        spFechaSolicitud2.setEnabled(false);

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText("Nro. Tramite Web");

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText("Tipo Solicitud");

        cboTipoSolicitud.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        cboTipoSolicitud.setEnabled(false);
        cboTipoSolicitud.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTipoSolicitudActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText("Tipo Documento");

        cboTipoDocumento.setEnabled(false);

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setText("Fecha Recepción ");

        spFechaRecepcion.setModel(new javax.swing.SpinnerDateModel());
        spFechaRecepcion.setEnabled(false);

        jLabel13.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel13.setText("Nro. Documento");

        textNumeroDocumento.setEnabled(false);

        jLabel15.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel15.setText("Tipo Acta");

        cboTipoActa1.setEnabled(false);

        jLabel16.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel16.setText("Nro Acta");

        textNumeroActa.setEnabled(false);

        jLabel17.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel17.setText("Grupo Familiar");

        cboGrupoFamiliar.setEnabled(false);

        jLabel18.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel18.setText("Grado de Parentesco");

        cboGradoParentesco.setEnabled(false);

        jLabel14.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel14.setText("DNI / Nro Documento");

        textNumeroDocumentoTitular.setEnabled(false);

        textApellidosNombreTitular.setEnabled(false);

        jLabel20.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel20.setText("Apellidos y Nombres Titular");

        jLabel21.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel21.setText("Unidad Organica");

        cboUnidadOrganica.setEditable(true);
        cboUnidadOrganica.setEnabled(false);

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText("DNI");

        textDniRemitente.setEnabled(false);

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText("Apellidos y Nombres Remitente");

        textApellidosNombreRemitente.setEnabled(false);

        textNumeroTramiteDocumento.setEnabled(false);

        jLabel23.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel23.setText("Tipo Procedimiento Registral");

        cboTipoProcedimientoRegistral1.setEnabled(false);

        javax.swing.GroupLayout jPanelDatosSolicitud2Layout = new javax.swing.GroupLayout(jPanelDatosSolicitud2);
        jPanelDatosSolicitud2.setLayout(jPanelDatosSolicitud2Layout);
        jPanelDatosSolicitud2Layout.setHorizontalGroup(
            jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                .addGroup(jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(67, 67, 67)
                        .addComponent(jLabel13))
                    .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(spFechaRecepcion, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(spFechaSolicitud2, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(textNumeroTramiteDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(cboTipoDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(textNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(109, 109, 109)
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(117, 117, 117)
                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(cboTipoActa1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(textNumeroActa, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(cboGrupoFamiliar, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(cboGradoParentesco, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(cboTipoProcedimientoRegistral1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(52, 52, 52)
                        .addComponent(jLabel7)
                        .addGap(118, 118, 118)
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(90, 90, 90)
                        .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(51, 51, 51)
                                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                                .addComponent(cboTipoSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(textDniRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(textApellidosNombreRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(cboUnidadOrganica, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                                .addComponent(textNumeroDocumentoTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(textApellidosNombreTitular)))))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanelDatosSolicitud2Layout.setVerticalGroup(
            jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jLabel9))
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spFechaRecepcion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spFechaSolicitud2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textNumeroTramiteDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTipoDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18)
                    .addComponent(jLabel23))
                .addGroup(jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboTipoActa1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textNumeroActa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboGrupoFamiliar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboGradoParentesco, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTipoProcedimientoRegistral1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel7)
                    .addComponent(jLabel22)
                    .addComponent(jLabel21))
                .addGap(3, 3, 3)
                .addGroup(jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboTipoSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textDniRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textApellidosNombreRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboUnidadOrganica, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addComponent(jLabel20))
                .addGap(3, 3, 3)
                .addGroup(jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textNumeroDocumentoTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textApellidosNombreTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanelPrincipal.add(jPanelDatosSolicitud2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1030, -1));

        jPanelParaNotificacion.setBackground(new java.awt.Color(255, 255, 255));
        jPanelParaNotificacion.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos para Notificación"));

        jLabel24.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel24.setText("Correo Electrónico");

        textCorreoElectronico.setEnabled(false);

        textCelular.setEnabled(false);

        jLabel25.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel25.setText("Celular");

        jLabel27.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel27.setText("Domicilio");

        textDomicilio.setEnabled(false);

        cboDireccionDomiciliaria.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        cboDireccionDomiciliaria.setEnabled(false);

        jLabel28.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel28.setText("Dirección Domiciliaria");

        javax.swing.GroupLayout jPanelParaNotificacionLayout = new javax.swing.GroupLayout(jPanelParaNotificacion);
        jPanelParaNotificacion.setLayout(jPanelParaNotificacionLayout);
        jPanelParaNotificacionLayout.setHorizontalGroup(
            jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParaNotificacionLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textCorreoElectronico, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(47, 47, 47)
                .addGroup(jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textCelular, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboDireccionDomiciliaria, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(44, 44, 44)
                .addGroup(jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textDomicilio, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15))
        );
        jPanelParaNotificacionLayout.setVerticalGroup(
            jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParaNotificacionLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel24)
                    .addComponent(jLabel27))
                .addGap(3, 3, 3)
                .addGroup(jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textCorreoElectronico, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textDomicilio, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelParaNotificacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelParaNotificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelParaNotificacionLayout.createSequentialGroup()
                        .addComponent(jLabel25)
                        .addGap(3, 3, 3)
                        .addComponent(textCelular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelParaNotificacionLayout.createSequentialGroup()
                        .addComponent(jLabel28)
                        .addGap(3, 3, 3)
                        .addComponent(cboDireccionDomiciliaria, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        jPanelPrincipal.add(jPanelParaNotificacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 1030, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 1040, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jPanelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarAsignacionEjecucionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarAsignacionEjecucionActionPerformed
     try
        {
            MenuPrincipal.ShowJPanel(new JPanelListadoExpedientesPorTrabajar());
        }
    catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnGuardarAsignacionEjecucionActionPerformed

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRegresarActionPerformed

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

    private void cboTipoSolicitudActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTipoSolicitudActionPerformed

        CatalogoItem catalogoTipoSolicitud = (CatalogoItem) cboTipoSolicitud.getSelectedItem();
        int idTipoSolicitud = catalogoTipoSolicitud.getIdCatalogoItem();
        /*
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
        */
    }//GEN-LAST:event_cboTipoSolicitudActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGuardarAsignacionEjecucion;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JComboBox cboDireccionDomiciliaria;
    private javax.swing.JComboBox cboGradoParentesco;
    private javax.swing.JComboBox cboGrupoFamiliar;
    private javax.swing.JComboBox cboTipoActa;
    private javax.swing.JComboBox cboTipoActa1;
    private javax.swing.JComboBox cboTipoDocumento;
    private javax.swing.JComboBox cboTipoProcedimientoRegistral;
    private javax.swing.JComboBox cboTipoProcedimientoRegistral1;
    private javax.swing.JComboBox cboTipoSolicitud;
    private javax.swing.JComboBox cboUnidadOrganica;
    private javax.swing.JButton jButton2;
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
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanelDatosSolicitud;
    private javax.swing.JPanel jPanelDatosSolicitud2;
    private javax.swing.JPanel jPanelParaNotificacion;
    private javax.swing.JPanel jPanelPrincipal;
    private javax.swing.JSpinner spFechaRecepcion;
    private javax.swing.JSpinner spFechaSolicitud;
    private javax.swing.JSpinner spFechaSolicitud2;
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
    private javax.swing.JTextField textNumeroTramiteDocumento2;
    private javax.swing.JTextField txtIdTecnico;
    private javax.swing.JTextField txtNombreTecnico;
    // End of variables declaration//GEN-END:variables
}
