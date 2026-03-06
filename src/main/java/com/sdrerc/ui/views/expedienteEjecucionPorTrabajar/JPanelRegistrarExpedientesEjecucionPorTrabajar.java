/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.expedienteEjecucionPorTrabajar;

import com.sdrerc.ui.views.expedientesPorVerificar.*;
import com.sdrerc.ui.views.expedientesPorTrabajar.*;
import com.sdrerc.ui.views.expedientes.*;
import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.application.ExpedienteAnalisisAbogadoService;
import com.sdrerc.application.ExpedienteAsignacionService;
import com.sdrerc.application.ExpedienteObservacionEjecucionService;
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
import com.sdrerc.domain.model.ExpedienteObservacionEjecucion.ExpedienteObservacionEjecucion;
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
public class JPanelRegistrarExpedientesEjecucionPorTrabajar extends javax.swing.JPanel 
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
    public JPanelRegistrarExpedientesEjecucionPorTrabajar() {
        initComponents();        
        this.expedienteService = new ExpedienteService();
        this.catalogoItemService = new CatalogoItemService();
        this.ubigeoService = new UbigeoService();
        this.expedienteAsignacionService = new ExpedienteAsignacionService();     
        this.expedienteAnalisisAbogadoService = new ExpedienteAnalisisAbogadoService();
        cargarTipoMedioNotificacion();
        cargarTieneObservacion();
    }
    
    public void cargarExpediente(String idExpediente) throws Exception 
    {
        Expediente lista = expedienteService.buscarporid(Integer.parseInt(idExpediente));           
        idExpedienteOculto = lista.getIdExpediente();
    }
    
    private void cargarTieneObservacion() 
    {
        cboTieneObservacion.removeAllItems();   
        
        // 👉 Item por defecto
        CatalogoItem itemSeleccione = new CatalogoItem();
        itemSeleccione.setId(0); // o 0 si tu lógica lo prefiere
        itemSeleccione.setDescripcion("--Seleccione--");

        cboTieneObservacion.addItem(itemSeleccione);
        
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(12);
        for (CatalogoItem catalogoitem : lista) 
        {
            cboTieneObservacion.addItem(catalogoitem);
        }
        cboTieneObservacion.setSelectedIndex(0);
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
    
    private void cargarTipoMedioNotificacion() 
    {
        cboEstadoEjecucion.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(15);
        for (CatalogoItem catalogoitem : lista) 
        {
            cboEstadoEjecucion.addItem(catalogoitem);
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
        cboEstadoEjecucion = new javax.swing.JComboBox();
        jLabel21 = new javax.swing.JLabel();
        cboTieneObservacion = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextDescripcionObservacion = new javax.swing.JTextArea();
        jLabel28 = new javax.swing.JLabel();
        jPanelDatosSolicitud1 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        spFechaSolicitud1 = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        spFechaRecepcion1 = new javax.swing.JSpinner();
        textNumeroTramiteDocumento1 = new javax.swing.JTextField();
        btnRegresar = new javax.swing.JButton();
        btnGuardarNotificacion = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(1060, 728));

        jPanelPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        jPanelPrincipal.setPreferredSize(new java.awt.Dimension(908, 558));
        jPanelPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanelDatosSolicitud.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosSolicitud.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos de la Ejecución"));
        jPanelDatosSolicitud.setPreferredSize(new java.awt.Dimension(1034, 329));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText("Fecha Ejecución");

        spFechaSolicitud.setModel(new javax.swing.SpinnerDateModel());

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setText("Estado");

        jLabel21.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel21.setText("¿Tiene observación?");

        cboTieneObservacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTieneObservacionActionPerformed(evt);
            }
        });

        jTextDescripcionObservacion.setColumns(20);
        jTextDescripcionObservacion.setRows(5);
        jScrollPane1.setViewportView(jTextDescripcionObservacion);

        jLabel28.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel28.setText("Descripción de la observación");

        javax.swing.GroupLayout jPanelDatosSolicitudLayout = new javax.swing.GroupLayout(jPanelDatosSolicitud);
        jPanelDatosSolicitud.setLayout(jPanelDatosSolicitudLayout);
        jPanelDatosSolicitudLayout.setHorizontalGroup(
            jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cboEstadoEjecucion, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 135, Short.MAX_VALUE)
                        .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spFechaSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cboTieneObservacion, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(46, 46, 46)
                        .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                                .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                                .addComponent(jScrollPane1)
                                .addGap(145, 145, 145)))))
                .addGap(79, 79, 79))
        );
        jPanelDatosSolicitudLayout.setVerticalGroup(
            jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLabel8))
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cboEstadoEjecucion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spFechaSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGap(70, 70, 70)
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboTieneObservacion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(71, Short.MAX_VALUE))
        );

        jPanelPrincipal.add(jPanelDatosSolicitud, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, 1030, 380));

        jPanelDatosSolicitud1.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosSolicitud1.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos de la solicitud"));
        jPanelDatosSolicitud1.setPreferredSize(new java.awt.Dimension(1034, 329));

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setText("Fecha Solicitud ");

        spFechaSolicitud1.setModel(new javax.swing.SpinnerDateModel());
        spFechaSolicitud1.setEnabled(false);

        jLabel11.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel11.setText("Nro. Tramite Web");

        jLabel19.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel19.setText("Fecha Recepción ");

        spFechaRecepcion1.setModel(new javax.swing.SpinnerDateModel());
        spFechaRecepcion1.setEnabled(false);

        textNumeroTramiteDocumento1.setEnabled(false);

        javax.swing.GroupLayout jPanelDatosSolicitud1Layout = new javax.swing.GroupLayout(jPanelDatosSolicitud1);
        jPanelDatosSolicitud1.setLayout(jPanelDatosSolicitud1Layout);
        jPanelDatosSolicitud1Layout.setHorizontalGroup(
            jPanelDatosSolicitud1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosSolicitud1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDatosSolicitud1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosSolicitud1Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosSolicitud1Layout.createSequentialGroup()
                        .addComponent(spFechaRecepcion1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(spFechaSolicitud1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(textNumeroTramiteDocumento1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(428, Short.MAX_VALUE))
        );
        jPanelDatosSolicitud1Layout.setVerticalGroup(
            jPanelDatosSolicitud1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosSolicitud1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDatosSolicitud1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosSolicitud1Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jLabel19))
                    .addComponent(jLabel10)
                    .addComponent(jLabel11))
                .addGap(1, 1, 1)
                .addGroup(jPanelDatosSolicitud1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spFechaRecepcion1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spFechaSolicitud1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textNumeroTramiteDocumento1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        jPanelPrincipal.add(jPanelDatosSolicitud1, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 6, 1034, 100));

        btnRegresar.setBackground(new java.awt.Color(25, 120, 210));
        btnRegresar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegresar.setForeground(new java.awt.Color(255, 255, 255));
        btnRegresar.setText("REGRESAR");
        btnRegresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresarActionPerformed(evt);
            }
        });
        jPanelPrincipal.add(btnRegresar, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 550, 146, 40));

        btnGuardarNotificacion.setBackground(new java.awt.Color(25, 120, 210));
        btnGuardarNotificacion.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardarNotificacion.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarNotificacion.setText("GUARDAR EJECUCIÖN");
        btnGuardarNotificacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarNotificacionActionPerformed(evt);
            }
        });
        jPanelPrincipal.add(btnGuardarNotificacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 550, 184, 40));

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

    private void btnGuardarNotificacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarNotificacionActionPerformed
     
    try {
            ExpedienteObservacionEjecucion o = new ExpedienteObservacionEjecucion();    
            CatalogoItem seleccionado = (CatalogoItem) cboTieneObservacion.getSelectedItem();

            if (seleccionado == null || seleccionado.getIdCatalogoItem() == 0) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Debe seleccionar una opción válida del Tiene Observación",
                        "Validación",
                        JOptionPane.WARNING_MESSAGE
                    );
                    cboTieneObservacion.requestFocus();
                    return;
            }
            
            CatalogoItem catalogoEstadoEjecucion = (CatalogoItem) cboEstadoEjecucion.getSelectedItem();
            int idEstadoEjecucion = catalogoEstadoEjecucion.getIdCatalogoItem();            
            o.setIdEstadoEjecucion(idEstadoEjecucion);

            String valorCombo = cboTieneObservacion.getSelectedItem().toString();
            boolean tieneObservacion = valorCombo.equalsIgnoreCase("SI");
            o.setTieneObservacion(tieneObservacion);        
            o.setIdExpediente(idExpedienteOculto);
            o.setDescripcionObservacion(jTextDescripcionObservacion.getText());        
            //o.setFechaEjecucion((Date) spFechaSolicitud.getValue());
            
            o.setUsuarioRegistro(1); 
            o.setUsuarioModificacion(1);
            Enumerado.EstadoExpediente estadoExpedienteVerificado = Enumerado.EstadoExpediente.ExpedienteVerificado;
            Enumerado.EstadoExpediente estadoExpedienteEjecucionTrabajada = Enumerado.EstadoExpediente.ExpedienteEjecucionTrabajada;
            o.setIdEstadoExpediente(tieneObservacion    ?   estadoExpedienteVerificado.getId()
                                                        :   estadoExpedienteEjecucionTrabajada.getId());

            ExpedienteObservacionEjecucionService service =
                new ExpedienteObservacionEjecucionService();

            service.registrarObservacion(o);

            JOptionPane.showMessageDialog(
                this,
                "Observación registrada correctamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE
            );
            MenuPrincipal.ShowJPanel(new JPanelListadoExpedientesEjecucionPorTrabajar());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Error al guardar",
                JOptionPane.ERROR_MESSAGE
            );
        }        
    }//GEN-LAST:event_btnGuardarNotificacionActionPerformed

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRegresarActionPerformed

    private void cboTieneObservacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTieneObservacionActionPerformed
        CatalogoItem seleccionado = (CatalogoItem) cboTieneObservacion.getSelectedItem();

       

        // TODO add your handling code here:
    }//GEN-LAST:event_cboTieneObservacionActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGuardarNotificacion;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JComboBox cboEstadoEjecucion;
    private javax.swing.JComboBox cboTieneObservacion;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanelDatosSolicitud;
    private javax.swing.JPanel jPanelDatosSolicitud1;
    private javax.swing.JPanel jPanelPrincipal;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextDescripcionObservacion;
    private javax.swing.JSpinner spFechaRecepcion1;
    private javax.swing.JSpinner spFechaSolicitud;
    private javax.swing.JSpinner spFechaSolicitud1;
    private javax.swing.JTextField textNumeroTramiteDocumento1;
    // End of variables declaration//GEN-END:variables
}
