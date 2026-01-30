/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.expedienteNotificacionPorTrabajar;

import com.sdrerc.ui.views.expedienteEjecucionPorTrabajar.*;
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
import com.sdrerc.domain.model.ExpedienteAnalisisAbogado.ExpedienteAnalisisAbogado;
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
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
/**
 *
 * @author usuario
 */
public class JPanelRegistrarExpedientesNotificacionPorTrabajar extends javax.swing.JPanel 
{
    private final ExpedienteService expedienteService;
    private final ExpedienteAnalisisAbogadoService expedienteAnalisisAbogadoService;
    private final CatalogoItemService catalogoItemService;
    private final ExpedienteAsignacionService expedienteAsignacionService;
    private final UbigeoService ubigeoService;
    private Integer idExpedienteOculto = 0;
    private ExpedienteAnalisisAbogado oExpedienteAnalisisAbogado;
    
    /**
     * Creates new form JPanelRegistrarExpediente
     */
    public JPanelRegistrarExpedientesNotificacionPorTrabajar() {
        initComponents();        
        oExpedienteAnalisisAbogado = new ExpedienteAnalisisAbogado();
        this.expedienteService = new ExpedienteService();
        this.catalogoItemService = new CatalogoItemService();
        this.ubigeoService = new UbigeoService();
        this.expedienteAsignacionService = new ExpedienteAsignacionService();     
        this.expedienteAnalisisAbogadoService = new ExpedienteAnalisisAbogadoService();
        cargarTipoMedioNotificacion();
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
    
    private void cargarTipoMedioNotificacion() 
    {
        cboTipoMedioNotificacion.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(14);
        for (CatalogoItem catalogoitem : lista) 
        {
            cboTipoMedioNotificacion.addItem(catalogoitem);
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
    
    
        DefaultTableModel modelo;    
    private void configurarModelo() 
    {
        modelo = new DefaultTableModel
        (
            //new Object[]{"N°", "Campo 1", "Campo 2", "Editar", "Eliminar"}, 0
              new Object[]{"N°", "Documento Analizado", "Desc Documento", "Eliminar"}, 0
        ) 
        {
            /*
            @Override
            public boolean isCellEditable(int row, int column) 
            {
                return column == 3 || column == 4;
            }
            */
            @Override
            public boolean isCellEditable(int row, int column) 
            {
                return column == 3;
            }
        };
        jTableDocumentosAnalisis.setModel(modelo);
    }
    
	
    private void configurarColumnaNumero() 
    {
        if(jTableDocumentosAnalisis.getColumnCount() == 0) return;
        
        jTableDocumentosAnalisis.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() 
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
            {

                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                label.setHorizontalAlignment(JLabel.CENTER);
                label.setText(String.valueOf(row + 1));
                return label;
            }
        });
    }	
	
    private void configurarColumnas() 
    {
        jTableDocumentosAnalisis.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        jTableDocumentosAnalisis.getColumnModel().getColumn(0).setPreferredWidth(40);
        jTableDocumentosAnalisis.getColumnModel().getColumn(1).setPreferredWidth(180);
        jTableDocumentosAnalisis.getColumnModel().getColumn(2).setPreferredWidth(180);
        jTableDocumentosAnalisis.getColumnModel().getColumn(3).setPreferredWidth(70);
    }
    
    /*
    class EditarRenderer extends JButton implements TableCellRenderer 
    {
        public EditarRenderer() { setText("✏"); }
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        return this;
        }
    }
    */

    class EliminarRenderer extends JButton implements TableCellRenderer {
        public EliminarRenderer() { setText("🗑"); }
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            return this;
        }
    }

    /*
    class EditarEditor extends DefaultCellEditor 
    {

        private JButton button;
        private JTable table;

        public EditarEditor(JTable table) 
        {
            super(new JCheckBox());
            this.table = table;
            button = new JButton("✏");
            button.addActionListener(e -> fireEditingStopped());
        }
        @Override
        public Object getCellEditorValue() 
        {
            int fila = table.getSelectedRow();
            textDescripcionDocumentoAnalisis.setText(table.getValueAt(fila, 1).toString());
            textDescripcionDocumentoAnalisis.setText(table.getValueAt(fila, 2).toString());
            filaEditando = fila;
            return "Editar";
        }
    }
    */

    class EliminarEditor extends DefaultCellEditor {

        private JButton button;
        private JTable table;

        public EliminarEditor(JTable table) {
            super(new JCheckBox());
            this.table = table;

            button = new JButton("🗑");
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Object getCellEditorValue() {

            int fila = table.getSelectedRow();

            int r = JOptionPane.showConfirmDialog(
                table,
                "¿Eliminar esta fila?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
            );

            if (r == JOptionPane.YES_OPTION) 
            {
              ((DefaultTableModel) table.getModel()).removeRow(fila);
	       oExpedienteAnalisisAbogado.getExpedienteAnalisisAbogadoDetDoc().remove(fila);
            }
            return "Eliminar";
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
        jLabel8 = new javax.swing.JLabel();
        cboTipoMedioNotificacion = new javax.swing.JComboBox();
        jLabel24 = new javax.swing.JLabel();
        cboTipoDocumentoAnalizado = new javax.swing.JComboBox();
        jLabel25 = new javax.swing.JLabel();
        textDescripcionDocumentoAnalisis = new javax.swing.JTextField();
        btnAgregarTipoAnalisis = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableDocumentosAnalisis = new javax.swing.JTable();
        jLabel20 = new javax.swing.JLabel();
        spFechaRecepcion2 = new javax.swing.JSpinner();
        jPanelDatosSolicitud1 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        spFechaSolicitud1 = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        spFechaRecepcion1 = new javax.swing.JSpinner();
        textNumeroTramiteDocumento1 = new javax.swing.JTextField();
        btnRegresar = new javax.swing.JButton();
        btnGuardarNotificacion = new javax.swing.JButton();
        jPanelDatosSolicitud2 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        cboTieneObservacion = new javax.swing.JComboBox();
        jLabel28 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(1060, 728));

        jPanelPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        jPanelPrincipal.setPreferredSize(new java.awt.Dimension(908, 558));
        jPanelPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanelDatosSolicitud.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosSolicitud.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos de la Ejecución"));
        jPanelDatosSolicitud.setPreferredSize(new java.awt.Dimension(1034, 329));

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setText("Medio Notificación");

        jLabel24.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel24.setText("Tipo Documento Analizado");

        jLabel25.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel25.setText("DESCRIPCIÓN");

        btnAgregarTipoAnalisis.setBackground(new java.awt.Color(25, 120, 210));
        btnAgregarTipoAnalisis.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAgregarTipoAnalisis.setForeground(new java.awt.Color(255, 255, 255));
        btnAgregarTipoAnalisis.setText("+");
        btnAgregarTipoAnalisis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarTipoAnalisisActionPerformed(evt);
            }
        });

        jTableDocumentosAnalisis.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jTableDocumentosAnalisis.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableDocumentosAnalisis.setShowHorizontalLines(true);
        jTableDocumentosAnalisis.setShowVerticalLines(true);
        jTableDocumentosAnalisis.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableDocumentosAnalisisMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTableDocumentosAnalisis);

        jLabel20.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel20.setText("Fecha notificación documento");

        spFechaRecepcion2.setModel(new javax.swing.SpinnerDateModel());
        spFechaRecepcion2.setEnabled(false);

        javax.swing.GroupLayout jPanelDatosSolicitudLayout = new javax.swing.GroupLayout(jPanelDatosSolicitud);
        jPanelDatosSolicitud.setLayout(jPanelDatosSolicitudLayout);
        jPanelDatosSolicitudLayout.setHorizontalGroup(
            jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(106, 106, 106)
                        .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(163, 163, 163)
                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelDatosSolicitudLayout.createSequentialGroup()
                                .addComponent(cboTipoDocumentoAnalizado, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(textDescripcionDocumentoAnalisis, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(spFechaRecepcion2, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(btnAgregarTipoAnalisis, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTipoMedioNotificacion, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        jPanelDatosSolicitudLayout.setVerticalGroup(
            jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(jLabel25)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboTipoDocumentoAnalizado, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textDescripcionDocumentoAnalisis, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spFechaRecepcion2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregarTipoAnalisis, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(cboTipoMedioNotificacion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jPanelPrincipal.add(jPanelDatosSolicitud, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 1030, 340));

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
        jPanelPrincipal.add(btnRegresar, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 670, 146, 40));

        btnGuardarNotificacion.setBackground(new java.awt.Color(25, 120, 210));
        btnGuardarNotificacion.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardarNotificacion.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarNotificacion.setText("GUARDAR NOTIFICACIÖN");
        btnGuardarNotificacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarNotificacionActionPerformed(evt);
            }
        });
        jPanelPrincipal.add(btnGuardarNotificacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 670, 184, 40));

        jPanelDatosSolicitud2.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosSolicitud2.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos de la Observación(Solo para las improcedentes)"));
        jPanelDatosSolicitud2.setPreferredSize(new java.awt.Dimension(1034, 329));

        jLabel21.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel21.setText("Tiene Observacion?");

        cboTieneObservacion.setEnabled(false);

        jLabel28.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel28.setText("Descripción de la observación");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setEnabled(false);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanelDatosSolicitud2Layout = new javax.swing.GroupLayout(jPanelDatosSolicitud2);
        jPanelDatosSolicitud2.setLayout(jPanelDatosSolicitud2Layout);
        jPanelDatosSolicitud2Layout.setHorizontalGroup(
            jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTieneObservacion, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(64, 64, 64)
                .addGroup(jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 630, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanelDatosSolicitud2Layout.setVerticalGroup(
            jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                .addGroup(jPanelDatosSolicitud2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboTieneObservacion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosSolicitud2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanelPrincipal.add(jPanelDatosSolicitud2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 460, 1030, 190));

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
     try
        {
            MenuPrincipal.ShowJPanel(new JPanelListadoExpedientesPorTrabajar());
        }
    catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnGuardarNotificacionActionPerformed

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRegresarActionPerformed

    int filaEditando = -1;
    private void btnAgregarTipoAnalisisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarTipoAnalisisActionPerformed
        String v1 = cboTipoDocumentoAnalizado.getSelectedItem().toString();
        String v2 = textDescripcionDocumentoAnalisis.getText();

        if(v2 == null || v2.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Debe ingresar la informacion necesaria", "WARNING", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Agregando a la tablaDetalle
        ExpedienteAnalisisAbogadoDetDoc det = new ExpedienteAnalisisAbogadoDetDoc();
        //det.setIdTipoDocumentoAnalizado(cboTipoDocumentoAnalizado.getSelectedIndex());
        det.setDescDocumento(v2);
        det.setActive(1);
        det.setUsuarioRegistro(1);

        CatalogoItem catalogoTipoDocumentoAnalizado = (CatalogoItem) cboTipoDocumentoAnalizado.getSelectedItem();
        det.setIdTipoDocumentoAnalizado (catalogoTipoDocumentoAnalizado.getIdCatalogoItem());

        oExpedienteAnalisisAbogado.agregarExpedienteAnalisisAbogadoDetDoc(det);

        if(filaEditando == -1)
        {
            modelo.addRow(new Object[]{"", v1, v2, "Eliminar"});
        }
        else
        {
            modelo.setValueAt(v1, filaEditando, 1);
            modelo.setValueAt(v2, filaEditando, 2);
            filaEditando = -1;
        }
        textDescripcionDocumentoAnalisis.setText("");
    }//GEN-LAST:event_btnAgregarTipoAnalisisActionPerformed

    private void jTableDocumentosAnalisisMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableDocumentosAnalisisMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jTableDocumentosAnalisisMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarTipoAnalisis;
    private javax.swing.JButton btnGuardarNotificacion;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JComboBox cboTieneObservacion;
    private javax.swing.JComboBox cboTipoDocumentoAnalizado;
    private javax.swing.JComboBox cboTipoMedioNotificacion;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanelDatosSolicitud;
    private javax.swing.JPanel jPanelDatosSolicitud1;
    private javax.swing.JPanel jPanelDatosSolicitud2;
    private javax.swing.JPanel jPanelPrincipal;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTableDocumentosAnalisis;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JSpinner spFechaRecepcion1;
    private javax.swing.JSpinner spFechaRecepcion2;
    private javax.swing.JSpinner spFechaSolicitud1;
    private javax.swing.JTextField textDescripcionDocumentoAnalisis;
    private javax.swing.JTextField textNumeroTramiteDocumento1;
    // End of variables declaration//GEN-END:variables
}
