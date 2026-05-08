/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.expedientesPorVerificar;

import com.sdrerc.ui.views.expedientesPorTrabajar.*;
import com.sdrerc.ui.views.expedientes.*;
import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.application.ExpedienteAnalisisAbogadoService;
import com.sdrerc.application.ExpedienteAsignacionService;
import com.sdrerc.application.ExpedienteObservacionVerificacionService;
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
import com.sdrerc.domain.model.ExpedienteAnalisAbogadoDetDoc.ExpedienteAnalisisAbogadoDetResponse;
import com.sdrerc.domain.model.ExpedienteAnalisisAbogado.ExpedienteAnalisisAbogado;
import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.domain.model.ExpedienteObservacionVerificacion.ExpedienteObservacionVerificacion;
import com.sdrerc.domain.model.Provincia;
import com.sdrerc.ui.common.icon.IconUtils;
import com.sdrerc.util.TextFieldRules;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
/**
 *
 * @author usuario
 */
public class JPanelRegistrarExpedientePorVerificar extends javax.swing.JPanel 
{
    private static final Color COLOR_FONDO = new Color(245, 247, 250);
    private static final Color COLOR_CARD = Color.WHITE;
    private static final Color COLOR_BORDE = new Color(220, 226, 235);
    private static final Color COLOR_TITULO = new Color(20, 45, 84);
    private static final Color COLOR_TEXTO = new Color(43, 52, 66);
    private static final Color COLOR_TEXTO_SECUNDARIO = new Color(92, 105, 123);
    private static final Color COLOR_ACCION = new Color(25, 120, 210);
    private static final Color COLOR_ACCION_HOVER = new Color(18, 101, 184);
    private static final Color COLOR_SECUNDARIO = new Color(239, 244, 250);

    private final ExpedienteService expedienteService;
    private final CatalogoItemService catalogoItemService;
    private final ExpedienteAsignacionService expedienteAsignacionService;
    private final ExpedienteObservacionVerificacionService expedienteObservacionVerificacionService;
    
    private final ExpedienteAnalisisAbogadoService expedienteAnalisisAbogadoService;
    private final UbigeoService ubigeoService;
    private Integer idExpedienteOculto = 0;
    private ExpedienteAnalisisAbogado oExpedienteAnalisisAbogado;
    private List<ExpedienteAnalisisAbogadoDetDoc> listaDocumentos = new ArrayList<>();
    private int filaEditando = -1;
    
    /**
     * Creates new form JPanelRegistrarExpediente
     */
    public JPanelRegistrarExpedientePorVerificar() {
        initComponents();
        corregirTextosVisibles();
        
        oExpedienteAnalisisAbogado = new ExpedienteAnalisisAbogado();
        this.expedienteService = new ExpedienteService();
        this.expedienteObservacionVerificacionService = new ExpedienteObservacionVerificacionService();
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
        
        cargarTipoObservacion();
        cargarTieneObservacion();
        cargarAnalisis();  
                
        registrarEventos();
        
        configurarModelo();
        configurarColumnaNumero();
	configurarColumnas();
        
        TableColumn col = jTableDocumentosAnalisis.getColumnModel().getColumn(1);
        col.setMinWidth(0);
        col.setMaxWidth(0);
        col.setPreferredWidth(0);
        configurarFormularioVerificacionPremium();
        
        //textDniRemitente.setEnabled(false);
        //textApellidosNombreRemitente.setEnabled(false);
        //cboUnidadOrganica.setEnabled(false);        
    }

    private void corregirTextosVisibles() {
        btnGuardarAnalisis.setText("Guardar verificación");
        btnRegresar5.setText("Regresar");
        jLabel23.setText("Hoja de Envío");
    }
    
    
    DefaultTableModel modelo;    
    private void configurarModelo() 
    {
        modelo = new DefaultTableModel
        (
              new Object[]{
                  "N°", 
                  "ID_TIPO_DOCUMENTO", // OCULTO
                  "Documento Analizado", 
                  "Desc Documento"}, 0
        );
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
        jTableDocumentosAnalisis.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        
        jTableDocumentosAnalisis.getColumnModel().getColumn(0).setPreferredWidth(40);
        jTableDocumentosAnalisis.getColumnModel().getColumn(1).setPreferredWidth(180);
        jTableDocumentosAnalisis.getColumnModel().getColumn(2).setPreferredWidth(210);
        jTableDocumentosAnalisis.getColumnModel().getColumn(3).setPreferredWidth(360);
    }

    private void configurarFormularioVerificacionPremium()
    {
        corregirTextosVisibles();
        setBackground(COLOR_FONDO);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        configurarComponentesConsulta();
        configurarTablaAnalisisPremium();
        configurarBotonesPremium();

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setBackground(COLOR_FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 14, 0);
        contenido.add(crearCardDatosSolicitud(), gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx = 0.46;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 14, 10);
        contenido.add(crearCardResultadoVerificacion(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.54;
        gbc.insets = new Insets(0, 10, 14, 0);
        contenido.add(crearCardDatosAnalisis(), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        contenido.add(crearBarraAcciones(), gbc);

        removeAll();
        add(contenido, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel crearCardDatosSolicitud()
    {
        JPanel card = crearCard("Datos de la solicitud");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        agregarCampo(grid, 0, 0, 1.0, "Fecha recepción", spFechaRecepcion);
        agregarCampo(grid, 1, 0, 1.0, "Fecha solicitud", spFechaSolicitud);
        agregarCampo(grid, 2, 0, 1.0, "N° trámite", textNumeroTramiteDocumento);
        agregarCampo(grid, 3, 0, 1.0, "Tipo documento", cboTipoDocumento);
        agregarCampo(grid, 4, 0, 1.0, "N° documento", textNumeroDocumento);

        agregarCampo(grid, 0, 1, 1.0, "Tipo acta", cboTipoActa);
        agregarCampo(grid, 1, 1, 1.0, "N° acta", textNumeroActa);
        agregarCampo(grid, 2, 1, 1.0, "Grupo familiar", cboGrupoFamiliar);
        agregarCampo(grid, 3, 1, 1.0, "Grado parentesco", cboGradoParentesco);
        agregarCampo(grid, 4, 1, 1.0, "Procedimiento registral", cboTipoProcedimientoRegistral);

        agregarCampo(grid, 0, 2, 0.70, "Tipo solicitud", cboTipoSolicitud);
        agregarCampo(grid, 1, 2, 0.55, "DNI remitente", textDniRemitente);
        agregarCampo(grid, 2, 2, 1.45, "Remitente", textApellidosNombreRemitente);
        agregarCampo(grid, 3, 2, 1.30, "Unidad orgánica", cboUnidadOrganica);

        agregarCampo(grid, 0, 3, 0.70, "DNI titular", textNumeroDocumentoTitular);
        agregarCampo(grid, 1, 3, 2.25, "Titular", textApellidosNombreTitular);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearCardResultadoVerificacion()
    {
        JPanel card = crearCard("Resultado de la verificación");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        agregarCampo(grid, 0, 0, 1.0, "Hoja de Envío", textHojaEnvio);
        agregarCampo(grid, 1, 0, 1.0, "N° Resolución", textResolucion);
        agregarCampo(grid, 0, 1, 1.0, "Tiene Observación", cboTieneObservacion);
        agregarCampo(grid, 1, 1, 1.0, "Tipo Observación", cboTipoObservacion);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 6, 0);
        grid.add(crearLabelCampo("Descripción de la observación"), gbc);

        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        jTextDescripcionObservacion.setRows(6);
        jScrollPane1.setPreferredSize(new Dimension(420, 132));
        grid.add(jScrollPane1, gbc);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearCardDatosAnalisis()
    {
        JPanel card = crearCard("Datos de análisis");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 12, 0);
        jScrollPane2.setPreferredSize(new Dimension(500, 250));
        grid.add(jScrollPane2, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(crearLabelCampo("Análisis"), gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(6, 0, 0, 0);
        cboAnalisisAbogado.setPreferredSize(new Dimension(320, 36));
        grid.add(cboAnalisisAbogado, gbc);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearBarraAcciones()
    {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(false);

        JPanel acciones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        acciones.add(btnRegresar5);
        acciones.add(btnGuardarAnalisis);
        card.add(acciones, BorderLayout.EAST);
        return card;
    }

    private JPanel crearCard(String titulo)
    {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE),
                BorderFactory.createEmptyBorder(16, 18, 18, 18)));

        JLabel label = new JLabel(titulo);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(COLOR_TITULO);
        card.add(label, BorderLayout.NORTH);
        return card;
    }

    private void agregarCampo(JPanel parent, int x, int y, double weightx, String label, JComponent component)
    {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setOpaque(false);
        wrapper.add(crearLabelCampo(label), BorderLayout.NORTH);
        wrapper.add(component, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = weightx;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(y == 0 ? 0 : 12, x == 0 ? 0 : 10, 0, 0);
        parent.add(wrapper, gbc);
    }

    private JLabel crearLabelCampo(String texto)
    {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(COLOR_TEXTO_SECUNDARIO);
        return label;
    }

    private void configurarComponentesConsulta()
    {
        JComponent[] componentes = {
            spFechaRecepcion, spFechaSolicitud, textNumeroTramiteDocumento, cboTipoDocumento,
            textNumeroDocumento, cboTipoActa, textNumeroActa, cboGrupoFamiliar, cboGradoParentesco,
            cboTipoProcedimientoRegistral, cboTipoSolicitud, textDniRemitente, textApellidosNombreRemitente,
            cboUnidadOrganica, textNumeroDocumentoTitular, textApellidosNombreTitular, textHojaEnvio,
            textResolucion, cboTieneObservacion, cboTipoObservacion, cboAnalisisAbogado
        };
        for (JComponent component : componentes) {
            component.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            component.setPreferredSize(new Dimension(component.getPreferredSize().width, 36));
        }

        JTextField[] textosConsulta = {
            textNumeroTramiteDocumento, textNumeroDocumento, textNumeroActa, textDniRemitente,
            textApellidosNombreRemitente, textNumeroDocumentoTitular, textApellidosNombreTitular
        };
        for (JTextField field : textosConsulta) {
            field.setDisabledTextColor(COLOR_TEXTO);
            field.setBackground(new Color(249, 251, 254));
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(214, 221, 230)),
                    BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        }

        estilizarAreaTexto(jTextDescripcionObservacion);
        actualizarTooltipsVerificacion();
    }

    private void estilizarAreaTexto(JTextArea area)
    {
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setForeground(COLOR_TEXTO);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    private void configurarTablaAnalisisPremium()
    {
        jTableDocumentosAnalisis.setRowHeight(30);
        jTableDocumentosAnalisis.setFillsViewportHeight(true);
        jTableDocumentosAnalisis.setShowGrid(false);
        jTableDocumentosAnalisis.setIntercellSpacing(new Dimension(0, 0));
        jTableDocumentosAnalisis.setSelectionBackground(new Color(223, 237, 252));
        jTableDocumentosAnalisis.setSelectionForeground(COLOR_TITULO);

        JTableHeader header = jTableDocumentosAnalisis.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(236, 241, 247));
        header.setForeground(COLOR_TITULO);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                setToolTipText(value == null ? "" : value.toString());
                if (!isSelected) {
                    component.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 253));
                    component.setForeground(COLOR_TEXTO);
                }
                return component;
            }
        };
        renderer.setVerticalAlignment(SwingConstants.CENTER);
        jTableDocumentosAnalisis.setDefaultRenderer(Object.class, renderer);
    }

    private void configurarBotonesPremium()
    {
        estilizarBoton(btnGuardarAnalisis, true);
        estilizarBoton(btnRegresar5, false);
        btnGuardarAnalisis.setIcon(IconUtils.load("save.svg", 16));
        btnRegresar5.setIcon(IconUtils.load("clear.svg", 16));
        btnGuardarAnalisis.setPreferredSize(new Dimension(176, 38));
        btnRegresar5.setPreferredSize(new Dimension(116, 38));
    }

    private void estilizarBoton(JButton button, boolean primary)
    {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBackground(primary ? COLOR_ACCION : COLOR_SECUNDARIO);
        button.setForeground(primary ? Color.WHITE : COLOR_TITULO);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(primary ? COLOR_ACCION_HOVER : new Color(228, 236, 246));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(primary ? COLOR_ACCION : COLOR_SECUNDARIO);
            }
        });
    }

    private void actualizarTooltipsVerificacion()
    {
        aplicarTooltipTexto(textNumeroTramiteDocumento);
        aplicarTooltipTexto(textNumeroDocumento);
        aplicarTooltipTexto(textNumeroActa);
        aplicarTooltipTexto(textDniRemitente);
        aplicarTooltipTexto(textApellidosNombreRemitente);
        aplicarTooltipTexto(textNumeroDocumentoTitular);
        aplicarTooltipTexto(textApellidosNombreTitular);
        aplicarTooltipTexto(textHojaEnvio);
        aplicarTooltipTexto(textResolucion);
        aplicarTooltipCombo(cboUnidadOrganica);
        aplicarTooltipCombo(cboTipoProcedimientoRegistral);
    }

    private void aplicarTooltipTexto(JTextField field)
    {
        field.setToolTipText(field.getText() == null ? "" : field.getText());
    }

    private void aplicarTooltipCombo(JComboBox combo)
    {
        Object item = combo.getSelectedItem();
        combo.setToolTipText(item == null ? "" : item.toString());
    }
    
    private void cargarTablaAnalisis(Integer idExpediente) {

        try {
            List<ExpedienteAnalisisAbogadoDetResponse> docs =
                expedienteAnalisisAbogadoService
                    .listarDocumentosPorExpediente(idExpediente);
            
            DefaultTableModel model =
                (DefaultTableModel) jTableDocumentosAnalisis.getModel();
            
            // Limpiar tabla y lista
            model.setRowCount(0);
            oExpedienteAnalisisAbogado
            .getExpedienteAnalisisAbogadoDetDoc()
            .clear();

            for (ExpedienteAnalisisAbogadoDetResponse d : docs) {
                
                // 1. Crear entidad de dominio
                ExpedienteAnalisisAbogadoDetDoc det =
                    new ExpedienteAnalisisAbogadoDetDoc();

                det.setIdTipoDocumentoAnalizado(d.getIdTipoDocumento());
                det.setDescTipoDocumentoAnalizado(d.getTipoDocumento());
                det.setDescDocumento(d.getDescripcionDocumento());
                det.setActive(1);

                // 2. Agregar a la lista (FUENTE DE VERDAD)
                oExpedienteAnalisisAbogado
                    .getExpedienteAnalisisAbogadoDetDoc()
                    .add(det);
                model.addRow(new Object[]{
                    "",
                    d.getIdTipoDocumento(),   // 🔥 ID REAL
                    d.getTipoDocumento(),
                    d.getDescripcionDocumento()
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
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
        
        Integer idAnalisis = expedienteAnalisisAbogadoService.obtenerIdAnalisisPorExpediente(Integer.parseInt(idExpediente));
        
        //tipoActa
        seleccionarEstadoEnCombo(cboAnalisisAbogado, idAnalisis); 
        
        cargarTablaAnalisis(Integer.parseInt(idExpediente));
        
        //esRegistroSdrerc 
        //jRadiButonNoCorresponde.setSelected(lista.getEsRegistroSdrerc() == 1? true : false);

        //hojaEnvioExpediente
        //textHojaEnvioExpediente.setText(lista.getHojaEnvioExpediente());                          

        //numeroTramiteDocumento 
        textNumeroTramiteDocumento.setText(lista.getNumeroTramiteDocumento());

        //fechaRecepcion
        spFechaRecepcion.setValue(lista.getFechaSolicitud()); 
        
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
        actualizarTooltipsVerificacion();
        
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
    
    /*
    private void cargarPlantillaDocumento() 
    {
        cboPlantillaDocumento.removeAllItems();    
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(10);
        for (CatalogoItem catalogoitem : lista) 
        {
            cboPlantillaDocumento.addItem(catalogoitem);
        }
    }
    */
    
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
    
    private void cargarTipoObservacion() 
    {
        cboTipoObservacion.removeAllItems();  
        
        CatalogoItem itemSeleccione = new CatalogoItem();
        itemSeleccione.setId(0); // o 0 si tu lógica lo prefiere
        itemSeleccione.setDescripcion("--Seleccione--");
        cboTipoObservacion.addItem(itemSeleccione);
        
        List<CatalogoItem> lista = catalogoItemService.listarCatalogoItem(13);
        for (CatalogoItem catalogoitem : lista) 
        {
            cboTipoObservacion.addItem(catalogoitem);
        }
        cboTipoObservacion.setSelectedIndex(0);
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
        jPanelDatosUbicacion1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableDocumentosAnalisis = new javax.swing.JTable();
        jLabel27 = new javax.swing.JLabel();
        cboAnalisisAbogado = new javax.swing.JComboBox();
        jPanelDatosUbicacion2 = new javax.swing.JPanel();
        cboTieneObservacion = new javax.swing.JComboBox();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        cboTipoObservacion = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextDescripcionObservacion = new javax.swing.JTextArea();
        jLabel28 = new javax.swing.JLabel();
        btnGuardarAnalisis = new javax.swing.JButton();
        btnRegresar5 = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        textHojaEnvio = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        textResolucion = new javax.swing.JTextField();

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
        spFechaSolicitud.setEnabled(false);

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText("Nro. Tramite Web");

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText("Tipo Solicitud");

        cboTipoSolicitud.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        cboTipoSolicitud.setEnabled(false);
        cboTipoSolicitud.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTipoSolicitudActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText("Tipo Documento");

        cboTipoDocumento.setEnabled(false);

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setText("Fecha Solicitud");

        spFechaRecepcion.setModel(new javax.swing.SpinnerDateModel());
        spFechaRecepcion.setEnabled(false);

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setText("Nro. Documento");

        textNumeroDocumento.setEnabled(false);

        jLabel15.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel15.setText("Tipo Acta");

        cboTipoActa.setEnabled(false);

        jLabel16.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel16.setText("Nro Acta");

        textNumeroActa.setEnabled(false);

        jLabel17.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel17.setText("Grupo Familiar");

        cboGrupoFamiliar.setEnabled(false);

        jLabel18.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel18.setText("Grado de Parentesco");

        cboGradoParentesco.setEnabled(false);

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setText("DNI / Nro Documento");

        textNumeroDocumentoTitular.setEnabled(false);

        textApellidosNombreTitular.setEnabled(false);

        jLabel13.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel13.setText("Apellidos y Nombres Titular");

        jLabel20.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel20.setText("Unidad Organica");

        cboUnidadOrganica.setEnabled(false);

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText("DNI");

        textDniRemitente.setEnabled(false);

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText("Apellidos y Nombres Remitente");

        textApellidosNombreRemitente.setEnabled(false);

        textNumeroTramiteDocumento.setEnabled(false);

        jLabel14.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel14.setText("Tipo Procedimiento Registral");

        cboTipoProcedimientoRegistral.setEnabled(false);

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
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelDatosSolicitudLayout.createSequentialGroup()
                                    .addComponent(textNumeroDocumentoTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(textApellidosNombreTitular))
                                .addGroup(jPanelDatosSolicitudLayout.createSequentialGroup()
                                    .addComponent(cboTipoSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(textDniRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(textApellidosNombreRemitente, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(cboUnidadOrganica, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE))))))
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
                .addGroup(jPanelDatosSolicitudLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textNumeroDocumentoTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textApellidosNombreTitular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanelPrincipal.add(jPanelDatosSolicitud, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 6, 1034, 329));

        jPanelDatosUbicacion1.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosUbicacion1.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos Analisis"));

        jTableDocumentosAnalisis.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableDocumentosAnalisis.setEnabled(false);
        jTableDocumentosAnalisis.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableDocumentosAnalisisMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTableDocumentosAnalisis);

        jLabel27.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel27.setText("Análisis");

        cboAnalisisAbogado.setEnabled(false);

        javax.swing.GroupLayout jPanelDatosUbicacion1Layout = new javax.swing.GroupLayout(jPanelDatosUbicacion1);
        jPanelDatosUbicacion1.setLayout(jPanelDatosUbicacion1Layout);
        jPanelDatosUbicacion1Layout.setHorizontalGroup(
            jPanelDatosUbicacion1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosUbicacion1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDatosUbicacion1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboAnalisisAbogado, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 426, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(78, Short.MAX_VALUE))
        );
        jPanelDatosUbicacion1Layout.setVerticalGroup(
            jPanelDatosUbicacion1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosUbicacion1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cboAnalisisAbogado, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29))
        );

        jPanelPrincipal.add(jPanelDatosUbicacion1, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 340, 520, 360));

        jPanelDatosUbicacion2.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosUbicacion2.setBorder(javax.swing.BorderFactory.createTitledBorder("Resultado de la verificación"));

        cboTieneObservacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTieneObservacionActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel21.setText("Tiene Observacion?");

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText("Tipo Observación");

        jTextDescripcionObservacion.setColumns(20);
        jTextDescripcionObservacion.setRows(5);
        jScrollPane1.setViewportView(jTextDescripcionObservacion);

        jLabel28.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel28.setText("Descripción de la observación");

        btnGuardarAnalisis.setBackground(new java.awt.Color(25, 120, 210));
        btnGuardarAnalisis.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardarAnalisis.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarAnalisis.setText("GUARDAR ANALISIS");
        btnGuardarAnalisis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarAnalisisActionPerformed(evt);
            }
        });

        btnRegresar5.setBackground(new java.awt.Color(25, 120, 210));
        btnRegresar5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegresar5.setForeground(new java.awt.Color(255, 255, 255));
        btnRegresar5.setText("REGRESAR");
        btnRegresar5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresar5ActionPerformed(evt);
            }
        });

        jLabel23.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel23.setText("Proveido");

        jLabel24.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel24.setText("N° Resolución");

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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                        .addGroup(jPanelDatosUbicacion2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cboTipoObservacion, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27))
                    .addGroup(jPanelDatosUbicacion2Layout.createSequentialGroup()
                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textHojaEnvio, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(jPanelDatosUbicacion2Layout.createSequentialGroup()
                .addGroup(jPanelDatosUbicacion2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosUbicacion2Layout.createSequentialGroup()
                        .addGap(66, 66, 66)
                        .addComponent(btnRegresar5, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(btnGuardarAnalisis, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosUbicacion2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelDatosUbicacion2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelDatosUbicacion2Layout.createSequentialGroup()
                                .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(textResolucion, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelDatosUbicacion2Layout.setVerticalGroup(
            jPanelDatosUbicacion2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosUbicacion2Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jPanelDatosUbicacion2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(textHojaEnvio, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addGroup(jPanelDatosUbicacion2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosUbicacion2Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboTieneObservacion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosUbicacion2Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboTipoObservacion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(12, 12, 12)
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelDatosUbicacion2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textResolucion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelDatosUbicacion2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRegresar5, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGuardarAnalisis, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23))
        );

        jPanelPrincipal.add(jPanelDatosUbicacion2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 500, 360));

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
    }//GEN-LAST:event_cboTipoSolicitudActionPerformed

    private void jTableDocumentosAnalisisMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableDocumentosAnalisisMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jTableDocumentosAnalisisMouseClicked

    private void btnGuardarAnalisisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarAnalisisActionPerformed
        
        try 
        {            
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
            
            ExpedienteObservacionVerificacion expedienteObservacion = new ExpedienteObservacionVerificacion();
            expedienteObservacion.setHojaEnvio(textHojaEnvio.getText());
            //Valor SI tiene Observacion
            String valorCombo = cboTieneObservacion.getSelectedItem().toString();
            boolean tieneObservacion = valorCombo.equalsIgnoreCase("SI");
            expedienteObservacion.setTieneObservacion(tieneObservacion);
            //tipoObservacion
            CatalogoItem catalogoTipoObservacion = (CatalogoItem) cboTipoObservacion.getSelectedItem();
            int idTipoObservacion = catalogoTipoObservacion.getIdCatalogoItem();            
            expedienteObservacion.setTipoObservacion(idTipoObservacion);
            expedienteObservacion.setDescripcionObservacion(jTextDescripcionObservacion.getText());
            
                   
            expedienteObservacion.setIdExpediente(idExpedienteOculto); 
            expedienteObservacion.setUsuarioRegistro(1); 
            expedienteObservacion.setUsuarioModificacion(1);
            Enumerado.EstadoExpediente estadoExpedienteRecibido = Enumerado.EstadoExpediente.ExpedienteRecibido;
            Enumerado.EstadoExpediente estadoExpedienteVerificado = Enumerado.EstadoExpediente.ExpedienteVerificado;
            expedienteObservacion.setIdEstadoExpediente(tieneObservacion    ?   estadoExpedienteRecibido.getId()
                                                                            :   estadoExpedienteVerificado.getId());
            
            expedienteObservacion.setResolucion(textResolucion.getText());
            expedienteObservacionVerificacionService.registrarObservacion(expedienteObservacion);

            JOptionPane.showMessageDialog(this, "Verificación registrada correctamente");

            MenuPrincipal.ShowJPanel(new JPanelListadoExpedientesPorVerificar());
        } 
        catch (Exception ex) 
        {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } 

        // TODO add your handling code here:
    }//GEN-LAST:event_btnGuardarAnalisisActionPerformed

    private void btnRegresar5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresar5ActionPerformed
        MenuPrincipal.ShowJPanel(new JPanelListadoExpedientesPorVerificar());
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRegresar5ActionPerformed

    private void cboTieneObservacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTieneObservacionActionPerformed
        CatalogoItem seleccionado = (CatalogoItem) cboTieneObservacion.getSelectedItem();
        
        if (seleccionado == null || seleccionado.getIdCatalogoItem() == 0) {
            // --Seleccione--
            cboTipoObservacion.setEnabled(false);
            cboTipoObservacion.setSelectedIndex(0);
            return;
        }
        
        boolean esSi =
            "SI".equalsIgnoreCase(seleccionado.getDescripcion());
        
        cboTipoObservacion.setEnabled(esSi);
        
        if (!esSi) {
            // NO → limpiar tipo
            cboTipoObservacion.setSelectedIndex(0);
            jTextDescripcionObservacion.setEnabled(false);
        }else  {
            cboTipoObservacion.setEnabled(true);
            jTextDescripcionObservacion.setEnabled(true);
        } 
        
        // TODO add your handling code here:
    }//GEN-LAST:event_cboTieneObservacionActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGuardarAnalisis;
    private javax.swing.JButton btnRegresar5;
    private javax.swing.JComboBox cboAnalisisAbogado;
    private javax.swing.JComboBox cboGradoParentesco;
    private javax.swing.JComboBox cboGrupoFamiliar;
    private javax.swing.JComboBox cboTieneObservacion;
    private javax.swing.JComboBox cboTipoActa;
    private javax.swing.JComboBox cboTipoDocumento;
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
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
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
    private javax.swing.JPanel jPanelDatosUbicacion1;
    private javax.swing.JPanel jPanelDatosUbicacion2;
    private javax.swing.JPanel jPanelPrincipal;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTableDocumentosAnalisis;
    private javax.swing.JTextArea jTextDescripcionObservacion;
    private javax.swing.JSpinner spFechaRecepcion;
    private javax.swing.JSpinner spFechaSolicitud;
    private javax.swing.JTextField textApellidosNombreRemitente;
    private javax.swing.JTextField textApellidosNombreTitular;
    private javax.swing.JTextField textDniRemitente;
    private javax.swing.JTextField textHojaEnvio;
    private javax.swing.JTextField textNumeroActa;
    private javax.swing.JTextField textNumeroDocumento;
    private javax.swing.JTextField textNumeroDocumentoTitular;
    private javax.swing.JTextField textNumeroTramiteDocumento;
    private javax.swing.JTextField textResolucion;
    // End of variables declaration//GEN-END:variables
}
