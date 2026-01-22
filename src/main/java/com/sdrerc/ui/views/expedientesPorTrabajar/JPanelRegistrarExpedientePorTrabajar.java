/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.expedientesPorTrabajar;

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
import com.sdrerc.domain.model.ExpedienteAnalisisAbogado.ExpedienteAnalisisAbogado;
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
import javax.swing.table.DefaultTableModel;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

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
    private final ExpedienteAnalisisAbogadoService expedienteAnalisisAbogadoService;
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
        this.expedienteAnalisisAbogadoService = new ExpedienteAnalisisAbogadoService();
        
        TextFieldRules.apply(textDniRemitente).onlyNumbers().max(8);
        TextFieldRules.apply(textApellidosNombreRemitente).onlyLetters().max(300);
        
        TextFieldRules.apply(textNumeroDocumentoTitular).onlyNumbers().max(8);
        TextFieldRules.apply(textApellidosNombreTitular).onlyLetters().max(300);
        
        
                
        cargarComboTipoSolicitud(); 
        cargarComboTipoDocumento();
        cargarComboTipoProcedimientoRegistral(); 
        cargarComboTipoActa();
        cargarComboGrupoFamiliar();
        cargarComboParentesco();
        //cargarComboDireccionDomiciliaria();
        cargarComboUnidadOrganica();
        
        cargarTipoDocumentoAnalizado();
        cargarPlantillaDocumento();
        cargarTieneObservacion();
        cargarTipoObservacion();
        cargarAnalisis();  
        
                
        registrarEventos();
        
        textDniRemitente.setEnabled(false);
        textApellidosNombreRemitente.setEnabled(false);
        cboUnidadOrganica.setEnabled(false);       
        
        configurarModelo();
        configurarColumnaNumero();
	configurarColumnas();        

        //jTableDocumentosAnalisis.getColumn("Editar").setCellRenderer(new EditarRenderer());
        //jTableDocumentosAnalisis.getColumn("Editar").setCellEditor(new EditarEditor(jTableDocumentosAnalisis));
        jTableDocumentosAnalisis.getColumn("Eliminar").setCellRenderer(new EliminarRenderer());
        jTableDocumentosAnalisis.getColumn("Eliminar").setCellEditor(new EliminarEditor(jTableDocumentosAnalisis));
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
        //seleccionarEstadoEnCombo(cboDireccionDomiciliaria, lista.getDireccionDomiciliaria());

        //domicilio
        //textDomicilio.setText(lista.getDomicilio());

        //correoElectronico
        //textCorreoElectronico.setText(lista.getCorreoElectronico());

        //celular
        //textCelular.setText(lista.getCelular());
        
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
    
    private void cargarPlantillaDocumento() 
    {
        cboPlantillaDocumento.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(10);
        for (CatalogoItem catalogoitem : lista) 
        {
            cboPlantillaDocumento.addItem(catalogoitem);
        }
    }
    
    private void cargarTieneObservacion() 
    {
        cboTieneObservacion.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(12);
        for (CatalogoItem catalogoitem : lista) 
        {
            cboTieneObservacion.addItem(catalogoitem);
        }
    }
    
    private void cargarTipoObservacion() 
    {
        cboTipoObservacion.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(13);
        for (CatalogoItem catalogoitem : lista) 
        {
            cboTipoObservacion.addItem(catalogoitem);
        }
    }
    
    private void cargarTipoDocumentoAnalizado() 
    {
        cboTipoDocumentoAnalizado.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(10);
        for (CatalogoItem catalogoitem : lista) 
        {
            cboTipoDocumentoAnalizado.addItem(catalogoitem);
        }
    }
    
    private void cargarAnalisis() 
    {
        cboAnalisisAbogado.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(11);
        for (CatalogoItem catalogoitem : lista) 
        {
            cboAnalisisAbogado.addItem(catalogoitem);
        }
    }
    
    
    /*
    private void cargarComboDireccionDomiciliaria() 
    {
        cboDireccionDomiciliaria.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(8);

        for (CatalogoItem catalogoitem : lista) {
            cboDireccionDomiciliaria.addItem(catalogoitem);
        }
    }
    */
    
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
    
    DefaultTableModel modelo;    
    private void configurarModelo() 
    {
        modelo = new DefaultTableModel
        (
            //new Object[]{"N°", "Campo 1", "Campo 2", "Editar", "Eliminar"}, 0
              new Object[]{"N°", "Campo 1", "Campo 2", "Eliminar"}, 0
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
	
    
	
    
    // private void configurarColumnaNumero() 
    // {

      // jTableDocumentosAnalisis.getColumnModel().getColumn(0)
        // .setCellRenderer(new DefaultTableCellRenderer() {
            // @Override
            // public Component getTableCellRendererComponent(
                    // JTable table, Object value, boolean isSelected,
                    // boolean hasFocus, int row, int column) {

                // JLabel label = (JLabel) super.getTableCellRendererComponent(
                        // table, value, isSelected, hasFocus, row, column);

                // label.setHorizontalAlignment(JLabel.CENTER);
                // label.setText(String.valueOf(row + 1));
                // return label;
            // }
        // });
    // }
	
	private void configurarColumnaNumero() 
	{
		if(jTableDocumentosAnalisis.getColumnCount() == 0) return;

		jTableDocumentosAnalisis.getColumnModel()
        .getColumn(0)
        .setCellRenderer(new DefaultTableCellRenderer() 
		{
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

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

            if (r == JOptionPane.YES_OPTION) {
                ((DefaultTableModel) table.getModel()).removeRow(fila);
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
        btnRegresar2 = new javax.swing.JButton();
        jPanelDatosUbicacion = new javax.swing.JPanel();
        cboPlantillaDocumento = new javax.swing.JComboBox();
        btnGenerarDocumento = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        jPanelDatosUbicacion1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableDocumentosAnalisis = new javax.swing.JTable();
        cboTipoDocumentoAnalizado = new javax.swing.JComboBox();
        btnAgregarTipoAnalisis = new javax.swing.JButton();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        textDescripcionDocumentoAnalisis = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        cboAnalisisAbogado = new javax.swing.JComboBox();
        btnGuardarAnalisis = new javax.swing.JButton();
        btnRegresar = new javax.swing.JButton();
        jPanelDatosUbicacion2 = new javax.swing.JPanel();
        cboTieneObservacion = new javax.swing.JComboBox();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        cboTipoObservacion = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel28 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(1060, 728));

        jPanelPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        jPanelPrincipal.setPreferredSize(new java.awt.Dimension(908, 558));
        jPanelPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanelDatosSolicitud.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosSolicitud.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos de la solicitud"));
        jPanelDatosSolicitud.setPreferredSize(new java.awt.Dimension(1034, 329));

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

        btnRegresar2.setBackground(new java.awt.Color(25, 120, 210));
        btnRegresar2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegresar2.setForeground(new java.awt.Color(255, 255, 255));
        btnRegresar2.setText("MODIFICAR");
        btnRegresar2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresar2ActionPerformed(evt);
            }
        });

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
                        .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(51, 51, 51)
                                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                                    .addComponent(textNumeroDocumentoTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(textApellidosNombreTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 588, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnRegresar2, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                                    .addComponent(cboTipoSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(textDniRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(textApellidosNombreRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(cboUnidadOrganica, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE))))))
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
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textNumeroDocumentoTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textApellidosNombreTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRegresar2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanelPrincipal.add(jPanelDatosSolicitud, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 6, 1034, 329));

        jPanelDatosUbicacion.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosUbicacion.setBorder(javax.swing.BorderFactory.createTitledBorder("Generar Plantilla"));

        btnGenerarDocumento.setBackground(new java.awt.Color(25, 120, 210));
        btnGenerarDocumento.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGenerarDocumento.setForeground(new java.awt.Color(255, 255, 255));
        btnGenerarDocumento.setText("GENERAR PLANTILLA");
        btnGenerarDocumento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarDocumentoActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel19.setText("Pantilla Documento");

        javax.swing.GroupLayout jPanelDatosUbicacionLayout = new javax.swing.GroupLayout(jPanelDatosUbicacion);
        jPanelDatosUbicacion.setLayout(jPanelDatosUbicacionLayout);
        jPanelDatosUbicacionLayout.setHorizontalGroup(
            jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosUbicacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                    .addComponent(cboPlantillaDocumento, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(28, 28, 28)
                .addComponent(btnGenerarDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelDatosUbicacionLayout.setVerticalGroup(
            jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosUbicacionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboPlantillaDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGenerarDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jPanelPrincipal.add(jPanelDatosUbicacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 500, 110));

        jPanelDatosUbicacion1.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosUbicacion1.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos Analisis"));

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

        btnAgregarTipoAnalisis.setBackground(new java.awt.Color(25, 120, 210));
        btnAgregarTipoAnalisis.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAgregarTipoAnalisis.setForeground(new java.awt.Color(255, 255, 255));
        btnAgregarTipoAnalisis.setText("+");
        btnAgregarTipoAnalisis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarTipoAnalisisActionPerformed(evt);
            }
        });

        jLabel24.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel24.setText("Tipo Documento Analizado");

        jLabel25.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel25.setText("DESCRIPCIÓN");

        jLabel27.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel27.setText("Análisis");

        btnGuardarAnalisis.setBackground(new java.awt.Color(25, 120, 210));
        btnGuardarAnalisis.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardarAnalisis.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarAnalisis.setText("GUARDAR ANALISIS");
        btnGuardarAnalisis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarAnalisisActionPerformed(evt);
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

        javax.swing.GroupLayout jPanelDatosUbicacion1Layout = new javax.swing.GroupLayout(jPanelDatosUbicacion1);
        jPanelDatosUbicacion1.setLayout(jPanelDatosUbicacion1Layout);
        jPanelDatosUbicacion1Layout.setHorizontalGroup(
            jPanelDatosUbicacion1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosUbicacion1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDatosUbicacion1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
                    .addGroup(jPanelDatosUbicacion1Layout.createSequentialGroup()
                        .addGroup(jPanelDatosUbicacion1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel24, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                            .addComponent(cboTipoDocumentoAnalizado, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(26, 26, 26)
                        .addGroup(jPanelDatosUbicacion1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanelDatosUbicacion1Layout.createSequentialGroup()
                                .addComponent(textDescripcionDocumentoAnalisis, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnAgregarTipoAnalisis, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanelDatosUbicacion1Layout.createSequentialGroup()
                        .addGroup(jPanelDatosUbicacion1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cboAnalisisAbogado, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanelDatosUbicacion1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnGuardarAnalisis, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRegresar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanelDatosUbicacion1Layout.setVerticalGroup(
            jPanelDatosUbicacion1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosUbicacion1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDatosUbicacion1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosUbicacion1Layout.createSequentialGroup()
                        .addComponent(jLabel25)
                        .addGap(3, 3, 3)
                        .addGroup(jPanelDatosUbicacion1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(textDescripcionDocumentoAnalisis, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAgregarTipoAnalisis, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelDatosUbicacion1Layout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboTipoDocumentoAnalizado, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelDatosUbicacion1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanelDatosUbicacion1Layout.createSequentialGroup()
                        .addComponent(jLabel27)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboAnalisisAbogado, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14))
                    .addGroup(jPanelDatosUbicacion1Layout.createSequentialGroup()
                        .addComponent(btnGuardarAnalisis, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        jPanelPrincipal.add(jPanelDatosUbicacion1, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 340, 520, 360));

        jPanelDatosUbicacion2.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosUbicacion2.setBorder(javax.swing.BorderFactory.createTitledBorder("Resultado de la verificación"));

        cboTieneObservacion.setEnabled(false);

        jLabel21.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel21.setText("Tiene Observacion?");

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText("Tipo Observación");

        cboTipoObservacion.setEnabled(false);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setEnabled(false);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel28.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel28.setText("Descripción de la observación");

        javax.swing.GroupLayout jPanelDatosUbicacion2Layout = new javax.swing.GroupLayout(jPanelDatosUbicacion2);
        jPanelDatosUbicacion2.setLayout(jPanelDatosUbicacion2Layout);
        jPanelDatosUbicacion2Layout.setHorizontalGroup(
            jPanelDatosUbicacion2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosUbicacion2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDatosUbicacion2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosUbicacion2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addContainerGap())
                    .addGroup(jPanelDatosUbicacion2Layout.createSequentialGroup()
                        .addGroup(jPanelDatosUbicacion2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cboTieneObservacion, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanelDatosUbicacion2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cboTipoObservacion, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27))
                    .addGroup(jPanelDatosUbicacion2Layout.createSequentialGroup()
                        .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanelDatosUbicacion2Layout.setVerticalGroup(
            jPanelDatosUbicacion2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosUbicacion2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDatosUbicacion2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosUbicacion2Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboTieneObservacion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosUbicacion2Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboTipoObservacion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanelPrincipal.add(jPanelDatosUbicacion2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 460, 500, 240));

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

    private void jTableDocumentosAnalisisMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableDocumentosAnalisisMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jTableDocumentosAnalisisMouseClicked
    
    int filaEditando = -1;
    
    private void btnAgregarTipoAnalisisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarTipoAnalisisActionPerformed
        String v1 = cboTipoDocumentoAnalizado.getSelectedItem().toString();
        String v2 = textDescripcionDocumentoAnalisis.getText();

        if(v2 == null || v2.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Debe ingresar la informacion necesaria", "WARNING", JOptionPane.ERROR_MESSAGE);
            return;
        }           
        
        if (filaEditando == -1) 
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
        //textDescripcionDocumentoAnalisis.setText("");
    }//GEN-LAST:event_btnAgregarTipoAnalisisActionPerformed

    private void btnGuardarAnalisisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarAnalisisActionPerformed
        // TODO add your handling code here:
        
        try
        {
            ExpedienteAnalisisAbogado oExpedienteAnalisisAbogado = new ExpedienteAnalisisAbogado();

            Enumerado.EstadoExpediente estadoExpedienteRecibido = Enumerado.EstadoExpediente.ExpedienteAtendido;
            //oExpedienteAnalisisAbogado.setIdEstadoExpediente(estadoExpedienteRecibido.getId());
            oExpedienteAnalisisAbogado.setIdExpediente(idExpedienteOculto);
            oExpedienteAnalisisAbogado.setIdAbogado(1);
            oExpedienteAnalisisAbogado.setIdAnalisis(1);
            oExpedienteAnalisisAbogado.setUsuarioRegistro(1);
            
            // Llamar al servicio
            if(idExpedienteOculto == 0)
            JOptionPane.showMessageDialog(this,"Registro no puede ser actualizado" ,"Error", JOptionPane.ERROR_MESSAGE);

            expedienteAnalisisAbogadoService.agregarAnalisisAbogado(oExpedienteAnalisisAbogado);
            JOptionPane.showMessageDialog(this, "Se realizo la recepción del expediente","Éxito", JOptionPane.INFORMATION_MESSAGE);

            limpiarCampos();
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnGuardarAnalisisActionPerformed

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRegresarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarTipoAnalisis;
    private javax.swing.JButton btnGenerarDocumento;
    private javax.swing.JButton btnGuardarAnalisis;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JButton btnRegresar2;
    private javax.swing.JComboBox cboAnalisisAbogado;
    private javax.swing.JComboBox cboGradoParentesco;
    private javax.swing.JComboBox cboGrupoFamiliar;
    private javax.swing.JComboBox cboPlantillaDocumento;
    private javax.swing.JComboBox cboTieneObservacion;
    private javax.swing.JComboBox cboTipoActa;
    private javax.swing.JComboBox cboTipoDocumento;
    private javax.swing.JComboBox cboTipoDocumentoAnalizado;
    private javax.swing.JComboBox cboTipoObservacion;
    private javax.swing.JComboBox cboTipoProcedimientoRegistral;
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
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
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
    private javax.swing.JPanel jPanelDatosUbicacion;
    private javax.swing.JPanel jPanelDatosUbicacion1;
    private javax.swing.JPanel jPanelDatosUbicacion2;
    private javax.swing.JPanel jPanelPrincipal;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTableDocumentosAnalisis;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JSpinner spFechaRecepcion;
    private javax.swing.JSpinner spFechaSolicitud;
    private javax.swing.JTextField textApellidosNombreRemitente;
    private javax.swing.JTextField textApellidosNombreTitular;
    private javax.swing.JTextField textDescripcionDocumentoAnalisis;
    private javax.swing.JTextField textDniRemitente;
    private javax.swing.JTextField textNumeroActa;
    private javax.swing.JTextField textNumeroDocumento;
    private javax.swing.JTextField textNumeroDocumentoTitular;
    private javax.swing.JTextField textNumeroTramiteDocumento;
    // End of variables declaration//GEN-END:variables
}
