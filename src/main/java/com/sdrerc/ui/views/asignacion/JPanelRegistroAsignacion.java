/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.asignacion;

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
import com.sdrerc.ui.common.icon.IconUtils;
import com.sdrerc.ui.menu.MenuPrincipal;
import com.sdrerc.domain.model.Provincia;
import com.sdrerc.util.DateRangePickerSupport;
import com.sdrerc.util.TextFieldRules;
import com.toedter.calendar.JDateChooser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Date;
import com.sdrerc.domain.model.Expediente.ExpedienteResponse;
import com.sdrerc.domain.model.ExpedienteAsignacion;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import com.sdrerc.util.ComboBoxUtils;
/**
 *
 * @author usuario
 */
public class JPanelRegistroAsignacion extends javax.swing.JPanel implements Scrollable
{
    
    private final ExpedienteService expedienteService;
    private final ExpedienteAsignacionService expedienteAsignacionService;
    private final CatalogoItemService catalogoItemService;
    private final UbigeoService ubigeoService;
    private Integer idExpedienteOculto = 0;
    private static final String[] CANALES_RECEPCION = {"INTERNO", "MP PRESENCIAL", "MPV", "OR PRESENCIAL"};
    
    /**
     * Creates new form JPanelRegistrarExpediente
     */
    public JPanelRegistroAsignacion() {
        initComponents();
        initFechaSolicitudPicker();
        initFechaAsignacionPicker();
        
        this.expedienteService = new ExpedienteService();
        this.expedienteAsignacionService = new ExpedienteAsignacionService();
        this.catalogoItemService = new CatalogoItemService();
        this.ubigeoService = new UbigeoService();
        
        TextFieldRules.apply(textDniRemitente).onlyNumbers().max(8);
        TextFieldRules.apply(textApellidosNombreRemitente).onlyLetters().max(300);
        
        TextFieldRules.apply(textNumeroDocumentoTitular).onlyNumbers().max(8);
        TextFieldRules.apply(textApellidosNombreTitular).onlyLetters().max(300);
        
        TextFieldRules.apply(textCelular).onlyNumbers().max(9);
        
                
        cargarComboTipoSolicitud(); 
        cargarComboCanalRecepcion();
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

        ComboBoxUtils.applySmartRenderer(this);
        configurarFormularioAsignacionPremium();
    }

    private void initFechaSolicitudPicker() {
        DateRangePickerSupport.configurePicker(spFechaRecepcion);
        spFechaRecepcion.setDate(new Date());
    }

    private void initFechaAsignacionPicker() {
        DateRangePickerSupport.configurePicker(spFechaAsignacion);
        spFechaAsignacion.setDate(new Date());
    }

    private Date getFechaSolicitudSeleccionada() {
        return spFechaRecepcion.getDate();
    }

    private void setFechaSolicitudSeleccionada(Date fecha) {
        spFechaRecepcion.setDate(fecha != null ? fecha : new Date());
    }
    
    
    private void registrarEventos() {

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
        */
      
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
    
    /*
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
    */
    
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

        //fechaSolicitud
        setFechaSolicitudSeleccionada(lista.getFechaSolicitud());

        //canalRecepcion
        seleccionarCanalRecepcion(lista.getCanalRecepcion());

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

        //provincia

        //distrito

        //direccionDomiciliaria
        seleccionarEstadoEnCombo(cboDireccionDomiciliaria, lista.getDireccionDomiciliaria());

        //domicilio
        textDomicilio.setText(lista.getDomicilio());

        //correoElectronico
        textCorreoElectronico.setText(lista.getCorreoElectronico());

        //celular
        textCelular.setText(lista.getCelular());
        actualizarTooltipsDatosExpediente();
        
    }

    private void actualizarTooltipsDatosExpediente() {
        textNumeroTramiteDocumento.setToolTipText(textNumeroTramiteDocumento.getText());
        textNumeroDocumento.setToolTipText(textNumeroDocumento.getText());
        textNumeroActa.setToolTipText(textNumeroActa.getText());
        textDniRemitente.setToolTipText(textDniRemitente.getText());
        textApellidosNombreRemitente.setToolTipText(textApellidosNombreRemitente.getText());
        textNumeroDocumentoTitular.setToolTipText(textNumeroDocumentoTitular.getText());
        textApellidosNombreTitular.setToolTipText(textApellidosNombreTitular.getText());
        textCorreoElectronico.setToolTipText(textCorreoElectronico.getText());
        textCelular.setToolTipText(textCelular.getText());
        textDomicilio.setToolTipText(textDomicilio.getText());
        txtNombreTecnico.setToolTipText(txtNombreTecnico.getText());
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

    private void cargarComboCanalRecepcion() {
        cboCanalRecepcion.removeAllItems();
        for (String canal : CANALES_RECEPCION) {
            cboCanalRecepcion.addItem(canal);
        }
        cboCanalRecepcion.setSelectedIndex(0);
    }

    private void seleccionarCanalRecepcion(String canalRecepcion) {
        if (canalRecepcion == null || canalRecepcion.trim().isEmpty()) {
            cboCanalRecepcion.setSelectedIndex(0);
            return;
        }

        for (int i = 0; i < cboCanalRecepcion.getItemCount(); i++) {
            String item = String.valueOf(cboCanalRecepcion.getItemAt(i));
            if (item.equalsIgnoreCase(canalRecepcion)) {
                cboCanalRecepcion.setSelectedIndex(i);
                return;
            }
        }

        cboCanalRecepcion.setSelectedIndex(0);
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

    private void configurarFormularioAsignacionPremium() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        removeAll();

        jPanelPrincipal.removeAll();
        jPanelPrincipal.setLayout(new BorderLayout(0, 14));
        jPanelPrincipal.setBackground(new Color(245, 247, 250));
        jPanelPrincipal.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        jPanelPrincipal.setPreferredSize(null);
        jPanelPrincipal.setMinimumSize(new Dimension(0, 0));

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setOpaque(false);
        agregarBloqueVertical(contenido, crearHeaderAsignacion(), 0, 0);
        agregarBloqueVertical(contenido, crearSeccionDatosSolicitud(), 1, 12);
        agregarBloqueVertical(contenido, crearSeccionNotificacion(), 2, 12);
        agregarBloqueVertical(contenido, crearSeccionDatosAsignacion(), 3, 12);

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(245, 247, 250));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        jPanelPrincipal.add(scroll, BorderLayout.CENTER);
        jPanelPrincipal.add(crearBarraAcciones(), BorderLayout.SOUTH);
        add(jPanelPrincipal, BorderLayout.CENTER);

        configurarEstiloAsignacion();
        revalidate();
        repaint();
    }

    private JPanel crearHeaderAsignacion() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel textos = new JPanel(new BorderLayout(0, 4));
        textos.setOpaque(false);

        JLabel titulo = new JLabel("Asignación de expediente");
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        titulo.setForeground(new Color(25, 42, 62));

        JLabel subtitulo = new JLabel("Revise los datos del expediente y asigne el abogado responsable para iniciar la atención.");
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(93, 105, 119));

        textos.add(titulo, BorderLayout.NORTH);
        textos.add(subtitulo, BorderLayout.CENTER);

        JLabel estado = new JLabel("Estado: Registrado");
        estado.setFont(new Font("Arial", Font.BOLD, 12));
        estado.setForeground(new Color(37, 99, 235));
        estado.setOpaque(true);
        estado.setBackground(new Color(219, 234, 254));
        estado.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        header.add(textos, BorderLayout.CENTER);
        header.add(estado, BorderLayout.EAST);
        return header;
    }

    private JPanel crearSeccionDatosSolicitud() {
        JPanel card = crearCardSeccion("Datos de la solicitud");
        JPanel body = obtenerBodyCard(card);
        body.setLayout(new GridBagLayout());

        int row = 0;
        agregarCampo(body, "Fecha solicitud", spFechaRecepcion, 0, row, 1, 0.16);
        agregarCampo(body, "Canal de recepción", cboCanalRecepcion, 1, row, 1, 0.18);
        agregarCampo(body, "Nro. trámite web", textNumeroTramiteDocumento, 2, row, 1, 0.22);
        agregarCampo(body, "Tipo documento", cboTipoDocumento, 3, row, 1, 0.22);
        agregarCampo(body, "Nro. documento", textNumeroDocumento, 4, row++, 1, 0.18);

        agregarCampo(body, "Tipo acta", cboTipoActa, 0, row, 1, 0.19);
        agregarCampo(body, "Nro. acta", textNumeroActa, 1, row, 1, 0.16);
        agregarCampo(body, "Grupo familiar", cboGrupoFamiliar, 2, row, 1, 0.21);
        agregarCampo(body, "Grado de parentesco", cboGradoParentesco, 3, row, 1, 0.21);
        agregarCampo(body, "Tipo procedimiento registral", cboTipoProcedimientoRegistral, 4, row++, 1, 0.23);

        agregarCampo(body, "Tipo solicitud", cboTipoSolicitud, 0, row, 1, 0.16);
        agregarCampo(body, "DNI remitente", textDniRemitente, 1, row, 1, 0.14);
        agregarCampo(body, "Apellidos y nombres remitente", textApellidosNombreRemitente, 2, row, 2, 0.38);
        agregarCampo(body, "Unidad orgánica", cboUnidadOrganica, 4, row++, 1, 0.32);

        agregarCampo(body, "DNI / nro. documento titular", textNumeroDocumentoTitular, 0, row, 1, 0.20);
        agregarCampo(body, "Apellidos y nombres titular", textApellidosNombreTitular, 1, row, 4, 0.80);

        return card;
    }

    private JPanel crearSeccionNotificacion() {
        JPanel card = crearCardSeccion("Datos para notificación");
        JPanel body = obtenerBodyCard(card);
        body.setLayout(new GridBagLayout());

        int row = 0;
        agregarCampo(body, "Correo electrónico", textCorreoElectronico, 0, row, 1, 0.24);
        agregarCampo(body, "Celular", textCelular, 1, row, 1, 0.16);
        agregarCampo(body, "Dirección domiciliaria", cboDireccionDomiciliaria, 2, row, 1, 0.22);
        agregarCampo(body, "Domicilio", textDomicilio, 3, row, 2, 0.38);

        return card;
    }

    private JPanel crearSeccionDatosAsignacion() {
        JPanel card = crearCardSeccion("Datos de asignación");
        JPanel body = obtenerBodyCard(card);
        body.setLayout(new GridBagLayout());

        txtIdTecnico.setVisible(false);

        JLabel ayuda = new JLabel("Seleccione el abogado que tendrá a cargo la atención del expediente.");
        ayuda.setForeground(new Color(93, 105, 119));
        ayuda.setFont(new Font("Arial", Font.PLAIN, 12));
        GridBagConstraints gbcAyuda = new GridBagConstraints();
        gbcAyuda.gridx = 0;
        gbcAyuda.gridy = 0;
        gbcAyuda.gridwidth = 4;
        gbcAyuda.weightx = 1;
        gbcAyuda.fill = GridBagConstraints.HORIZONTAL;
        gbcAyuda.insets = new Insets(0, 0, 10, 0);
        body.add(ayuda, gbcAyuda);

        JPanel abogado = new JPanel(new BorderLayout(8, 0));
        abogado.setOpaque(false);
        abogado.add(txtNombreTecnico, BorderLayout.CENTER);
        abogado.add(jButton2, BorderLayout.EAST);

        agregarCampo(body, "Abogado responsable", abogado, 0, 1, 2, 0.54);
        agregarCampo(body, "Fecha de asignación", spFechaAsignacion, 2, 1, 1, 0.22);
        agregarCampo(body, "Hoja de envío", textHojaEnvioAsignacion, 3, 1, 1, 0.24);

        return card;
    }

    private JPanel crearBarraAcciones() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);

        JPanel izquierda = new JPanel(new GridBagLayout());
        izquierda.setOpaque(false);
        izquierda.add(btnRegresar);

        JPanel derecha = new JPanel(new GridBagLayout());
        derecha.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 10);
        derecha.add(btnLimpiar, gbc);
        gbc.insets = new Insets(0, 0, 0, 0);
        derecha.add(btnGuardar, gbc);

        barra.add(izquierda, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    private JPanel crearCardSeccion(String titulo) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(14, 16, 16, 16)
        ));
        card.setMinimumSize(new Dimension(0, 0));

        JLabel labelTitulo = new JLabel(titulo);
        labelTitulo.setFont(new Font("Arial", Font.BOLD, 15));
        labelTitulo.setForeground(new Color(25, 42, 62));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setMinimumSize(new Dimension(0, 0));

        card.add(labelTitulo, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel obtenerBodyCard(JPanel card) {
        return (JPanel) card.getComponent(1);
    }

    private void agregarBloqueVertical(JPanel parent, Component component, int row, int topInset) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(topInset, 0, 0, 0);
        parent.add(component, gbc);
    }

    private JPanel crearCampo(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.setMinimumSize(new Dimension(0, 0));
        panel.add(crearLabelCampo(label), BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private void agregarCampo(JPanel parent, String label, JComponent component, int x, int y, int gridwidth, double weightx) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = gridwidth;
        gbc.weightx = weightx;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(y == 0 ? 0 : 8, 0, 0, x + gridwidth >= 5 ? 0 : 12);
        parent.add(crearCampo(label, component), gbc);
    }

    private JLabel crearLabelCampo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(55, 65, 81));
        return label;
    }

    private void configurarEstiloAsignacion() {
        configurarCampoTexto(textNumeroTramiteDocumento);
        configurarCampoTexto(textNumeroDocumento);
        configurarCampoTexto(textNumeroActa);
        configurarCampoTexto(textDniRemitente);
        configurarCampoTexto(textApellidosNombreRemitente);
        configurarCampoTexto(textNumeroDocumentoTitular);
        configurarCampoTexto(textApellidosNombreTitular);
        configurarCampoTexto(textCorreoElectronico);
        configurarCampoTexto(textCelular);
        configurarCampoTexto(textDomicilio);
        configurarCampoTexto(textHojaEnvioAsignacion);
        configurarCampoTexto(txtNombreTecnico);

        txtNombreTecnico.setEditable(false);
        txtNombreTecnico.setToolTipText("Abogado responsable seleccionado.");
        txtIdTecnico.setToolTipText("Identificador interno del abogado seleccionado.");
        textApellidosNombreRemitente.setToolTipText("Apellidos y nombres completos del remitente.");
        textApellidosNombreTitular.setToolTipText("Apellidos y nombres completos del titular.");
        cboUnidadOrganica.setToolTipText("Unidad orgánica registrada en el expediente.");
        textDomicilio.setToolTipText("Domicilio registrado para notificación.");
        textHojaEnvioAsignacion.setToolTipText("Número de hoja de envío de asignación, si corresponde.");

        Dimension fieldSize = new Dimension(120, 34);
        for (JComponent component : new JComponent[] {
                cboCanalRecepcion, cboTipoDocumento, cboTipoActa, cboGrupoFamiliar,
                cboGradoParentesco, cboTipoProcedimientoRegistral, cboTipoSolicitud,
                cboUnidadOrganica, cboDireccionDomiciliaria, spFechaRecepcion, spFechaAsignacion
        }) {
            component.setPreferredSize(fieldSize);
            component.setMinimumSize(new Dimension(80, 34));
        }

        jButton2.setText("Seleccionar abogado");
        btnRegresar.setText("Regresar");
        btnLimpiar.setText("Limpiar");
        btnGuardar.setText("Generar asignación");

        aplicarEstiloBotonSecundario(btnRegresar);
        aplicarEstiloBotonSecundario(btnLimpiar);
        aplicarEstiloBotonPrimario(btnGuardar);
        aplicarEstiloBotonSecundario(jButton2);

        IconUtils.applyIcon(btnGuardar, "assignment.svg");
        IconUtils.applyIcon(btnLimpiar, "clear.svg");
        IconUtils.applyIcon(btnRegresar, "clear.svg");
        IconUtils.applyIcon(jButton2, "users.svg");
    }

    private void configurarCampoTexto(JTextField field) {
        field.setPreferredSize(new Dimension(120, 34));
        field.setMinimumSize(new Dimension(80, 34));
        field.setDisabledTextColor(new Color(75, 85, 99));
        field.setToolTipText(field.getText());
    }

    private void aplicarEstiloBotonPrimario(JButton boton) {
        boton.setBackground(new Color(25, 120, 210));
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(168, 38));
        boton.setFocusPainted(false);
        boton.setIconTextGap(8);
    }

    private void aplicarEstiloBotonSecundario(JButton boton) {
        boton.setBackground(Color.WHITE);
        boton.setForeground(new Color(25, 42, 62));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(150, 38));
        boton.setFocusPainted(false);
        boton.setIconTextGap(8);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
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
        setFechaSolicitudSeleccionada(new Date());
        

        // Resetear JComboBoxes al primer elemento
        if (cboCanalRecepcion.getItemCount() > 0) cboCanalRecepcion.setSelectedIndex(0);
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
        jPanelDatosSolicitud = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        cboCanalRecepcion = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cboTipoSolicitud = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        cboTipoDocumento = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        spFechaRecepcion = new com.toedter.calendar.JDateChooser();
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
        jButton2 = new javax.swing.JButton();
        txtIdTecnico = new javax.swing.JTextField();
        txtNombreTecnico = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        spFechaAsignacion = new com.toedter.calendar.JDateChooser();
        jLabel25 = new javax.swing.JLabel();
        textHojaEnvioAsignacion = new javax.swing.JTextField();

        setBackground(new java.awt.Color(255, 255, 255));

        jPanelPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        jPanelPrincipal.setPreferredSize(new java.awt.Dimension(908, 558));

        jPanelDatosSolicitud.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosSolicitud.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos de la solicitud"));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText("Canal de recepción");

        cboCanalRecepcion.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N

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
        jLabel8.setText("Fecha Solicitud");

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
                        .addComponent(cboCanalRecepcion, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(cboCanalRecepcion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        btnGuardar.setText("GENERAR");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        jPanelDatosUbicacion.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosUbicacion.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos Asignación"));

        jButton2.setText("Seleccionar Abogado");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel10.setText("Fecha de Asignación:");

        jLabel25.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel25.setText("Hoja de Envio");

        javax.swing.GroupLayout jPanelDatosUbicacionLayout = new javax.swing.GroupLayout(jPanelDatosUbicacion);
        jPanelDatosUbicacion.setLayout(jPanelDatosUbicacionLayout);
        jPanelDatosUbicacionLayout.setHorizontalGroup(
            jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosUbicacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDatosUbicacionLayout.createSequentialGroup()
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtIdTecnico, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNombreTecnico, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spFechaAsignacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDatosUbicacionLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(textHojaEnvioAsignacion, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelDatosUbicacionLayout.setVerticalGroup(
            jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatosUbicacionLayout.createSequentialGroup()
                .addGroup(jPanelDatosUbicacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtIdTecnico, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNombreTecnico, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(spFechaAsignacion, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel25)
                .addGap(3, 3, 3)
                .addComponent(textHojaEnvioAsignacion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 49, Short.MAX_VALUE))
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
                .addComponent(jPanelDatosSolicitud, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelParaNotificacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelDatosUbicacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
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
        MenuPrincipal.ShowJPanel(new JPanelFiltroBusqueda());
    }//GEN-LAST:event_btnRegresarActionPerformed

    
    private boolean validarFormulario() {

        if (getFechaSolicitudSeleccionada() == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar la Fecha de Solicitud.");
            spFechaRecepcion.requestFocus();
            return false;
        }

        if (cboCanalRecepcion.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar el Canal de recepción.");
            cboCanalRecepcion.requestFocus();
            return false;
        }

        if (textNumeroTramiteDocumento.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el Número de Trámite.");
            textNumeroTramiteDocumento.requestFocus();
            return false;
        }

        if (cboTipoSolicitud.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un Tipo de Solicitud.");
            cboTipoSolicitud.requestFocus();
            return false;
        }

        if (cboTipoDocumento.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un Tipo de Documento.");
            cboTipoDocumento.requestFocus();
            return false;
        }
/*
        if (textNumeroDocumento.getText().trim().length() != 8) {
            JOptionPane.showMessageDialog(this, "El DNI del Remitente debe tener 8 dígitos.");
            textNumeroDocumento.requestFocus();
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
        
        */

        if (cboTipoActa.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar Tipo de Acta.");
            cboTipoActa.requestFocus();
            return false;
        }

        if (textNumeroActa.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar Número de Acta.");
            textNumeroActa.requestFocus();
            return false;
        }

        if (cboGrupoFamiliar.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar Grupo Familiar.");
            cboGrupoFamiliar.requestFocus();
            return false;
        }
        /*
        if (textNumeroGrupoFamiliar.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar Número de Grupo Familiar.");
            textNumeroGrupoFamiliar.requestFocus();
            return false;
        }
        */

        if (textNumeroDocumentoTitular.getText().trim().length() != 8) {
            JOptionPane.showMessageDialog(this, "El DNI del Titular debe tener 8 dígitos.");
            textNumeroDocumentoTitular.requestFocus();
            return false;
        }
/*
        if (textApellidosNombresTitular.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar Apellidos y Nombres del Titular.");
            textApellidosNombresTitular.requestFocus();
            return false;
        }
        
        */

        return validarAntesDeGenerarAsignacion();
    }

    private boolean validarAntesDeGenerarAsignacion() {
        return validarAbogadoAsignado()
                && validarFechaAsignacion()
                && validarHojaEnvioAsignacion(true);
    }

    private boolean validarAbogadoAsignado() {
        String idTecnico = txtIdTecnico.getText() == null ? "" : txtIdTecnico.getText().trim();
        String nombreTecnico = txtNombreTecnico.getText() == null ? "" : txtNombreTecnico.getText().trim();

        if (idTecnico.isEmpty() || nombreTecnico.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione el abogado responsable del expediente.");
            jButton2.requestFocus();
            return false;
        }

        try {
            Integer.parseInt(idTecnico);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Seleccione el abogado responsable del expediente.");
            jButton2.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validarFechaAsignacion() {
        Date fechaAsignacion = spFechaAsignacion.getDate();
        if (fechaAsignacion == null) {
            JOptionPane.showMessageDialog(this, "Ingrese una fecha de asignación válida.");
            spFechaAsignacion.requestFocus();
            return false;
        }

        Date hoy = new Date();
        if (fechaAsignacion.after(hoy)) {
            JOptionPane.showMessageDialog(this, "Ingrese una fecha de asignación válida.");
            spFechaAsignacion.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validarHojaEnvioAsignacion(boolean obligatoria) {
        String hojaEnvio = textHojaEnvioAsignacion.getText() == null
                ? ""
                : textHojaEnvioAsignacion.getText().trim();
        textHojaEnvioAsignacion.setText(hojaEnvio);

        if (obligatoria && hojaEnvio.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el número de hoja de envío de asignación.");
            textHojaEnvioAsignacion.requestFocus();
            return false;
        }

        return true;
    }

    private boolean confirmarGeneracionAsignacion() {
        String nombreAbogado = txtNombreTecnico.getText() == null ? "" : txtNombreTecnico.getText().trim();
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "Se asignará el expediente al abogado " + nombreAbogado + ". ¿Desea continuar?",
                "Confirmar asignación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return respuesta == JOptionPane.YES_OPTION;
    }

    private String mensajeErrorAsignacion(Exception ex) {
        String mensaje = ex.getMessage() == null ? "" : ex.getMessage();
        if (mensaje.toLowerCase().contains("asignación activa")
                || mensaje.toLowerCase().contains("asignacion activa")) {
            return "El expediente ya cuenta con una asignación activa.";
        }
        return "Error al guardar: " + mensaje;
    }
    
    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        if (!validarFormulario()) {
            return; // Detiene el proceso si hay errores
        }
        if (!confirmarGeneracionAsignacion()) {
            return;
        }
        try 
        {
            Expediente expediente = new Expediente();  

            //esRegistroSdrerc 
            //int noPertenece = jRadiButonNoCorresponde.isSelected()? 1 : 0;
            //expediente.setEsRegistroSdrerc(noPertenece);            
            
            //hojaEnvioExpediente
            //expediente.setHojaEnvioExpediente(textHojaEnvioExpediente.getText());            
            
            //numeroTramiteDocumento 
            expediente.setNumeroTramiteDocumento(textNumeroTramiteDocumento.getText());
            
            //fechaSolicitud
            expediente.setFechaSolicitud(getFechaSolicitudSeleccionada());
            expediente.setCanalRecepcion(String.valueOf(cboCanalRecepcion.getSelectedItem()));
            
            
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
            
            expediente.setIdExpediente(idExpedienteOculto);
            
            Enumerado.EstadoExpediente estadoExpedienteRecibido = Enumerado.EstadoExpediente.ExpedienteAsignado;
            expediente.setEstado(estadoExpedienteRecibido.getId());
                                             
            ExpedienteResponse response;              
            ExpedienteAsignacion asignacion = new ExpedienteAsignacion();
            asignacion.setIdExpediente(idExpedienteOculto);
            asignacion.setIdTecnico(Integer.parseInt(txtIdTecnico.getText()));            
            Date fecha = spFechaAsignacion.getDate();
            asignacion.setFechaAsignacion(fecha);
            
            asignacion.setHojaEnvioAsignacion(textHojaEnvioAsignacion.getText());
            
            expedienteAsignacionService.agregarExpediente(asignacion,expediente);

            JOptionPane.showMessageDialog(this, "Asignación registrada correctamente");

            MenuPrincipal.ShowJPanel(new JPanelFiltroBusqueda());
            /*
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
            */
        } 
        catch (Exception ex) 
        {
            JOptionPane.showMessageDialog(this,
                    mensajeErrorAsignacion(ex),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }        
    }//GEN-LAST:event_btnGuardarActionPerformed

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
            txtNombreTecnico.setToolTipText(nomTec);
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(16, visibleRect.height - 32);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JComboBox<String> cboCanalRecepcion;
    private javax.swing.JComboBox cboDireccionDomiciliaria;
    private javax.swing.JComboBox cboGradoParentesco;
    private javax.swing.JComboBox cboGrupoFamiliar;
    private javax.swing.JComboBox cboTipoActa;
    private javax.swing.JComboBox cboTipoDocumento;
    private javax.swing.JComboBox cboTipoProcedimientoRegistral;
    private javax.swing.JComboBox cboTipoSolicitud;
    private javax.swing.JComboBox cboUnidadOrganica;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel10;
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
    private javax.swing.JLabel jLabel25;
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
    private com.toedter.calendar.JDateChooser spFechaAsignacion;
    private com.toedter.calendar.JDateChooser spFechaRecepcion;
    private javax.swing.JTextField textApellidosNombreRemitente;
    private javax.swing.JTextField textApellidosNombreTitular;
    private javax.swing.JTextField textCelular;
    private javax.swing.JTextField textCorreoElectronico;
    private javax.swing.JTextField textDniRemitente;
    private javax.swing.JTextField textDomicilio;
    private javax.swing.JTextField textHojaEnvioAsignacion;
    private javax.swing.JTextField textNumeroActa;
    private javax.swing.JTextField textNumeroDocumento;
    private javax.swing.JTextField textNumeroDocumentoTitular;
    private javax.swing.JTextField textNumeroTramiteDocumento;
    private javax.swing.JTextField txtIdTecnico;
    private javax.swing.JTextField txtNombreTecnico;
    // End of variables declaration//GEN-END:variables
}
