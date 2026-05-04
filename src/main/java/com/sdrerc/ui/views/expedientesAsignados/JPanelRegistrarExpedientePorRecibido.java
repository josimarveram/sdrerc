/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.expedientesAsignados;

import com.sdrerc.ui.views.expedientes.*;
import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.application.ExpedienteAsignacionService;
import com.sdrerc.application.ExpedienteService;
import com.sdrerc.application.UbigeoService;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.Departamento;
import com.sdrerc.domain.model.Distrito;
import com.sdrerc.domain.model.Enumerado;
import com.sdrerc.domain.model.Enumerado.TipoSolicitud;
import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.ui.menu.MenuPrincipal;
import com.sdrerc.domain.model.Expediente.ExpedienteResponse;
import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.domain.model.Provincia;
import com.sdrerc.shared.session.SessionContext;
import com.sdrerc.ui.common.icon.IconUtils;
import com.sdrerc.util.ComboBoxUtils;
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
import java.awt.Rectangle;
import java.text.SimpleDateFormat;
import java.util.List;
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
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
/**
 *
 * @author usuario
 */
public class JPanelRegistrarExpedientePorRecibido extends javax.swing.JPanel implements Scrollable
{
    private final ExpedienteService expedienteService;
    private final CatalogoItemService catalogoItemService;
    private final UbigeoService ubigeoService;
    private Integer idExpedienteOculto = 0;
    private final ExpedienteAsignacionService expedienteAsignacionService;
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
    private Expediente expedienteActual;
    private ExpedienteAsignacion asignacionActual;
    private JLabel lblEstadoRecepcion;
    private JLabel lblTramiteBadge;
    private JLabel lblFechaBadge;
    private JLabel lblResumenRecepcion;
    private JLabel lblResumenTramite;
    private JLabel lblResumenProcedimiento;
    private JLabel lblResumenSolicitud;
    private JLabel lblResumenHojaEnvio;
    private JLabel lblResumenRemitente;
    private JLabel lblResumenTitular;
    
    /**
     * Creates new form JPanelRegistrarExpediente
     */
    public JPanelRegistrarExpedientePorRecibido() {
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
        
        cargarDepartamentos();

        //cboProvincia.setEnabled(false);
        //cboDistrito.setEnabled(false);
        
        registrarEventos();
        
        //textDniRemitente.setEnabled(false);
        //textApellidosNombreRemitente.setEnabled(false);
        //cboUnidadOrganica.setEnabled(false);
        
        ButtonGroup grupoCorrespondeSdrerc = new ButtonGroup();
        grupoCorrespondeSdrerc.add(jRadiButonSiCorresponde);
        grupoCorrespondeSdrerc.add(jRadiButonNoCorresponde);

        ComboBoxUtils.applySmartRenderer(this);
        configurarFormularioRecepcionAsignadoPremium();
    }
    
    private boolean modoEdicion = false;
    
    
    private void registrarEventos() {

        cboDepartamento.addActionListener(e -> {
            if (cboDepartamento.getSelectedIndex() != -1) {
                //cboProvincia.setEnabled(true);
                cargarProvincias();
            }
        });

        cboProvincia.addActionListener(e -> {
            if (cboProvincia.getSelectedIndex() != -1) {
                //cboDistrito.setEnabled(true);
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
    
    private void cargarUbigeo(Expediente exp) 
    {
            if (exp == null) return;
            modoEdicion = true;
            seleccionarDepartamento(exp.getDepartamento());
            seleccionarProvincia(exp.getProvincia());
            seleccionarDistrito(exp.getDistrito());
            modoEdicion = false;
    }
    
    public void cargarExpediente(String idExpediente) throws Exception 
    {        
        Expediente lista = expedienteService.buscarporid(Integer.parseInt(idExpediente));           
        expedienteActual = lista;
        idExpedienteOculto = lista.getIdExpediente();
        asignacionActual = expedienteAsignacionService.buscarAsignacionInicialActivaPorExpediente(idExpedienteOculto);
        
        //esRegistroSdrerc 
        jRadiButonNoCorresponde.setSelected(lista.getEsRegistroSdrerc() == 1? true : false);

        //hojaEnvioExpediente
        textHojaEnvioExpediente.setText(lista.getHojaEnvioExpediente());                          

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
        SwingUtilities.invokeLater(() -> cargarUbigeo(lista));

        //direccionDomiciliaria
        seleccionarEstadoEnCombo(cboDireccionDomiciliaria, lista.getDireccionDomiciliaria());

        //domicilio
        textDomicilio.setText(lista.getDomicilio());

        //correoElectronico
        textCorreoElectronico.setText(lista.getCorreoElectronico());

        //celular
        textCelular.setText(lista.getCelular());

        actualizarContextoRecepcion();
        
    }   
    
    private void seleccionarDepartamento(int idDepartamento) {
    for (int i = 0; i < cboDepartamento.getItemCount(); i++) {
        Departamento d = (Departamento) cboDepartamento.getItemAt(i);
        if (d.getIdDepartamento() == idDepartamento) {
            cboDepartamento.setSelectedIndex(i);
            return;
        }
    }
	}

	private void seleccionarProvincia(int idProvincia) {
    for (int i = 0; i < cboProvincia.getItemCount(); i++) {
        Provincia p = (Provincia) cboProvincia.getItemAt(i);
        if (p.getIdProvincia() == idProvincia) {
            cboProvincia.setSelectedIndex(i);
            return;
        }
    }
}

private void seleccionarDistrito(int idDistrito) {
    for (int i = 0; i < cboDistrito.getItemCount(); i++) {
        Distrito d = (Distrito) cboDistrito.getItemAt(i);
        if (d.getIdDistrito() == idDistrito) {
            cboDistrito.setSelectedIndex(i);
            return;
        }
    }
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

    private void configurarFormularioRecepcionAsignadoPremium() {
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
        agregarBloqueVertical(contenido, crearHeaderRecepcion(), 0, 0);
        agregarBloqueVertical(contenido, crearSeccionResumen(), 1, 12);
        agregarBloqueVertical(contenido, crearSeccionDatosSolicitud(), 2, 12);
        agregarBloqueVertical(contenido, crearSeccionNotificacion(), 3, 12);
        agregarBloqueVertical(contenido, crearSeccionConfirmacion(), 4, 12);

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(245, 247, 250));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        jPanelPrincipal.add(scroll, BorderLayout.CENTER);
        jPanelPrincipal.add(crearBarraAcciones(), BorderLayout.SOUTH);
        add(jPanelPrincipal, BorderLayout.CENTER);

        configurarEstiloRecepcionAsignado();
        revalidate();
        repaint();
    }

    private JPanel crearHeaderRecepcion() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel textos = new JPanel(new BorderLayout(0, 4));
        textos.setOpaque(false);

        JLabel titulo = new JLabel("Recepción de expediente asignado");
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        titulo.setForeground(new Color(25, 42, 62));

        JLabel subtitulo = new JLabel("Revise los datos del expediente y confirme la recepción para iniciar la atención.");
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(93, 105, 119));

        textos.add(titulo, BorderLayout.NORTH);
        textos.add(subtitulo, BorderLayout.CENTER);

        JPanel badges = new JPanel(new GridBagLayout());
        badges.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 6, 0, 0);
        lblEstadoRecepcion = crearBadge("Estado: Asignado", new Color(219, 234, 254), new Color(37, 99, 235));
        lblTramiteBadge = crearBadge("N° trámite: -", new Color(241, 245, 249), new Color(71, 85, 105));
        lblFechaBadge = crearBadge("Fecha: -", new Color(241, 245, 249), new Color(71, 85, 105));
        badges.add(lblEstadoRecepcion, gbc);
        badges.add(lblTramiteBadge, gbc);
        badges.add(lblFechaBadge, gbc);

        header.add(textos, BorderLayout.CENTER);
        header.add(badges, BorderLayout.EAST);
        return header;
    }

    private JLabel crearBadge(String texto, Color background, Color foreground) {
        JLabel badge = new JLabel(texto);
        badge.setFont(new Font("Arial", Font.BOLD, 12));
        badge.setForeground(foreground);
        badge.setOpaque(true);
        badge.setBackground(background);
        badge.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return badge;
    }

    private JPanel crearSeccionResumen() {
        JPanel card = crearCardSeccion("Resumen del expediente");
        JPanel body = obtenerBodyCard(card);
        body.setLayout(new GridBagLayout());

        lblResumenTramite = crearValorResumen("-");
        lblResumenProcedimiento = crearValorResumen("-");
        lblResumenSolicitud = crearValorResumen("-");
        lblResumenHojaEnvio = crearValorResumen("-");
        lblResumenRemitente = crearValorResumen("-");
        lblResumenTitular = crearValorResumen("-");

        int row = 0;
        agregarCampo(body, "Nro. trámite web", lblResumenTramite, 0, row, 1, 0.20);
        agregarCampo(body, "Tipo procedimiento registral", lblResumenProcedimiento, 1, row, 1, 0.22);
        agregarCampo(body, "Tipo solicitud", lblResumenSolicitud, 2, row, 1, 0.18);
        agregarCampo(body, "Hoja de envío", lblResumenHojaEnvio, 3, row++, 1, 0.20);

        agregarCampo(body, "Remitente", lblResumenRemitente, 0, row, 2, 0.45);
        agregarCampo(body, "Titular", lblResumenTitular, 2, row, 2, 0.45);
        return card;
    }

    private JPanel crearSeccionDatosSolicitud() {
        JPanel card = crearCardSeccion("Datos de la solicitud");
        JPanel body = obtenerBodyCard(card);
        body.setLayout(new GridBagLayout());

        int row = 0;
        agregarCampo(body, "Fecha solicitud", spFechaSolicitud, 0, row, 1, 0.17);
        agregarCampo(body, "Nro. trámite web", textNumeroTramiteDocumento, 1, row, 1, 0.22);
        agregarCampo(body, "Tipo documento", cboTipoDocumento, 2, row, 1, 0.22);
        agregarCampo(body, "Nro. documento", textNumeroDocumento, 3, row++, 1, 0.18);

        agregarCampo(body, "Tipo acta", cboTipoActa, 0, row, 1, 0.19);
        agregarCampo(body, "Nro. acta", textNumeroActa, 1, row, 1, 0.16);
        agregarCampo(body, "Grupo familiar", cboGrupoFamiliar, 2, row, 1, 0.21);
        agregarCampo(body, "Grado de parentesco", cboGradoParentesco, 3, row++, 1, 0.21);

        agregarCampo(body, "Tipo solicitud", cboTipoSolicitud, 0, row, 1, 0.16);
        agregarCampo(body, "DNI remitente", textDniRemitente, 1, row, 1, 0.14);
        agregarCampo(body, "Apellidos y nombres remitente", textApellidosNombreRemitente, 2, row, 1, 0.35);
        agregarCampo(body, "Unidad orgánica", cboUnidadOrganica, 3, row++, 1, 0.28);

        agregarCampo(body, "DNI / nro. documento titular", textNumeroDocumentoTitular, 0, row, 1, 0.20);
        agregarCampo(body, "Apellidos y nombres titular", textApellidosNombreTitular, 1, row, 3, 0.80);
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
        agregarCampo(body, "Domicilio", textDomicilio, 3, row, 1, 0.38);
        return card;
    }

    private JPanel crearSeccionUbicacion() {
        JPanel card = crearCardSeccion("Ubicación");
        JPanel body = obtenerBodyCard(card);
        body.setLayout(new GridBagLayout());

        int row = 0;
        agregarCampo(body, "Departamento", cboDepartamento, 0, row, 1, 0.33);
        agregarCampo(body, "Provincia", cboProvincia, 1, row, 1, 0.33);
        agregarCampo(body, "Distrito", cboDistrito, 2, row, 1, 0.33);
        return card;
    }

    private JPanel crearSeccionConfirmacion() {
        JPanel card = crearCardSeccion("Confirmación de recepción");
        JPanel body = obtenerBodyCard(card);
        body.setLayout(new BorderLayout(0, 12));

        lblResumenRecepcion = new JLabel("Al aceptar el expediente, quedará registrado que usted recibió la asignación para iniciar la atención.");
        lblResumenRecepcion.setFont(new Font("Arial", Font.PLAIN, 13));
        lblResumenRecepcion.setForeground(new Color(71, 85, 105));
        body.add(lblResumenRecepcion, BorderLayout.CENTER);
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
        derecha.add(btnAceptarExpediente);

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
        gbc.insets = new Insets(y == 0 ? 0 : 8, 0, 0, x + gridwidth >= 4 ? 0 : 12);
        parent.add(crearCampo(label, component), gbc);
    }

    private JLabel crearLabelCampo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(55, 65, 81));
        return label;
    }

    private JLabel crearValorResumen(String texto) {
        JLabel label = new JLabel(texto);
        label.setOpaque(true);
        label.setBackground(new Color(248, 250, 252));
        label.setForeground(new Color(30, 41, 59));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        label.setPreferredSize(new Dimension(120, 34));
        return label;
    }

    private void configurarEstiloRecepcionAsignado() {
        for (JTextField field : new JTextField[] {
                textHojaEnvioExpediente, textNumeroTramiteDocumento, textNumeroDocumento,
                textNumeroActa, textDniRemitente, textApellidosNombreRemitente,
                textNumeroDocumentoTitular, textApellidosNombreTitular, textCorreoElectronico,
                textCelular, textDomicilio
        }) {
            configurarCampoTexto(field);
        }

        Dimension fieldSize = new Dimension(120, 34);
        for (JComponent component : new JComponent[] {
                cboTipoDocumento, cboTipoActa, cboGrupoFamiliar, cboGradoParentesco,
                cboTipoProcedimientoRegistral, cboTipoSolicitud, cboUnidadOrganica,
                cboDireccionDomiciliaria, cboDepartamento, cboProvincia, cboDistrito,
                spFechaSolicitud, spFechaRecepcion
        }) {
            component.setPreferredSize(fieldSize);
            component.setMinimumSize(new Dimension(80, 34));
        }

        bloquearControlesConsulta();

        btnAceptarExpediente.setText("Aceptar expediente");
        btnRegresar.setText("Regresar");
        aplicarEstiloBotonPrimario(btnAceptarExpediente);
        aplicarEstiloBotonSecundario(btnRegresar);
        IconUtils.applyIcon(btnAceptarExpediente, "check.svg");
        IconUtils.applyIcon(btnRegresar, "clear.svg");

        textNumeroTramiteDocumento.setToolTipText("Número de trámite web registrado.");
        textApellidosNombreRemitente.setToolTipText("Apellidos y nombres completos del remitente.");
        textApellidosNombreTitular.setToolTipText("Apellidos y nombres completos del titular.");
        cboUnidadOrganica.setToolTipText("Unidad orgánica registrada en el expediente.");
        textDomicilio.setToolTipText("Domicilio registrado para notificación.");
    }

    private void configurarCampoTexto(JTextField field) {
        field.setPreferredSize(new Dimension(120, 34));
        field.setMinimumSize(new Dimension(80, 34));
        field.setDisabledTextColor(new Color(75, 85, 99));
        field.setToolTipText(field.getText());
    }

    private void bloquearControlesConsulta() {
        for (JComponent component : new JComponent[] {
                spFechaSolicitud, spFechaRecepcion, textHojaEnvioExpediente,
                textNumeroTramiteDocumento, cboTipoDocumento, textNumeroDocumento,
                cboTipoActa, textNumeroActa, cboGrupoFamiliar, cboGradoParentesco,
                cboTipoProcedimientoRegistral, cboTipoSolicitud, textDniRemitente,
                textApellidosNombreRemitente, cboUnidadOrganica, textNumeroDocumentoTitular,
                textApellidosNombreTitular, textCorreoElectronico, textCelular,
                cboDireccionDomiciliaria, textDomicilio, cboDepartamento, cboProvincia, cboDistrito,
                jRadiButonSiCorresponde, jRadiButonNoCorresponde
        }) {
            component.setEnabled(false);
        }
    }

    private void aplicarEstiloBotonPrimario(JButton boton) {
        boton.setBackground(new Color(25, 120, 210));
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(180, 38));
        boton.setFocusPainted(false);
        boton.setIconTextGap(8);
    }

    private void aplicarEstiloBotonSecundario(JButton boton) {
        boton.setBackground(Color.WHITE);
        boton.setForeground(new Color(25, 42, 62));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(140, 38));
        boton.setFocusPainted(false);
        boton.setIconTextGap(8);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
    }

    private void actualizarContextoRecepcion() {
        if (lblEstadoRecepcion == null || expedienteActual == null) {
            return;
        }

        String estado = descripcionEstadoActual();
        lblEstadoRecepcion.setText("Estado: " + estado);
        lblTramiteBadge.setText("N° trámite: " + textoSeguro(expedienteActual.getNumeroTramiteDocumento()));
        lblFechaBadge.setText("Fecha: " + formatearFecha(expedienteActual.getFechaSolicitud()));
        actualizarResumenVisual();

        boolean recibido = asignacionActual != null && asignacionActual.getAceptaRecepcion() == 1;
        if (recibido) {
            lblEstadoRecepcion.setForeground(new Color(22, 101, 52));
            lblEstadoRecepcion.setBackground(new Color(220, 252, 231));
            lblResumenRecepcion.setText("Este expediente ya fue recibido anteriormente. Fecha de recepción: "
                    + formatearFecha(asignacionActual.getFechaRecepcion()) + ".");
            btnAceptarExpediente.setEnabled(false);
            btnAceptarExpediente.setText("Expediente recibido");
            btnAceptarExpediente.setToolTipText("Este expediente ya fue recibido anteriormente.");
        } else {
            lblEstadoRecepcion.setForeground(new Color(37, 99, 235));
            lblEstadoRecepcion.setBackground(new Color(219, 234, 254));
            lblResumenRecepcion.setText("Al aceptar el expediente, quedará registrado que usted recibió la asignación para iniciar la atención."
                    + textoAbogadoAsignado());
            boolean puedeAceptar = expedienteActual.getEstado() == Enumerado.EstadoExpediente.ExpedienteAsignado.getId()
                    && esAsignacionDelUsuarioActual();
            btnAceptarExpediente.setEnabled(puedeAceptar);
            btnAceptarExpediente.setToolTipText(puedeAceptar
                    ? "Registrar recepción del expediente asignado."
                    : "Solo el abogado designado puede aceptar la recepción.");
        }
    }

    private boolean esAsignacionDelUsuarioActual() {
        if (asignacionActual == null) {
            return false;
        }
        try {
            return asignacionActual.getIdTecnico() == SessionContext.getIdTecnicoActual();
        } catch (Exception ex) {
            return false;
        }
    }

    private void actualizarResumenVisual() {
        lblResumenTramite.setText(textoSeguro(expedienteActual.getNumeroTramiteDocumento()));
        lblResumenProcedimiento.setText(textoSeleccionado(cboTipoProcedimientoRegistral));
        lblResumenSolicitud.setText(textoSeleccionado(cboTipoSolicitud));
        lblResumenHojaEnvio.setText(textoSeguro(expedienteActual.getHojaEnvioExpediente()));
        lblResumenRemitente.setText(textoSeguro(expedienteActual.getApellidoNombreRemitente()));
        lblResumenTitular.setText(textoSeguro(expedienteActual.getApellidoNombreTitular()));

        for (JLabel label : new JLabel[] {
                lblResumenTramite, lblResumenProcedimiento, lblResumenSolicitud,
                lblResumenHojaEnvio, lblResumenRemitente, lblResumenTitular
        }) {
            label.setToolTipText(label.getText());
        }
    }

    private String textoSeleccionado(JComboBox combo) {
        Object item = combo.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    private String textoAbogadoAsignado() {
        if (asignacionActual == null || textoSeguro(asignacionActual.getNombreTecnico()).isEmpty()) {
            return "";
        }
        return " Abogado asignado: " + textoSeguro(asignacionActual.getNombreTecnico()) + ".";
    }

    private String descripcionEstadoActual() {
        if (expedienteActual == null) {
            return "Asignado";
        }
        try {
            return Enumerado.EstadoExpediente.fromId(expedienteActual.getEstado()).name()
                    .replaceAll("([a-z])([A-Z])", "$1 $2");
        } catch (IllegalArgumentException ex) {
            return String.valueOf(expedienteActual.getEstado());
        }
    }

    private String formatearFecha(java.util.Date fecha) {
        return fecha == null ? "-" : formatoFecha.format(fecha);
    }

    private String textoSeguro(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean validarAntesDeAceptarExpediente() {
        if (idExpedienteOculto == null || idExpedienteOculto == 0) {
            JOptionPane.showMessageDialog(this, "No se encontró el expediente a recibir.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (expedienteActual != null && expedienteActual.getEstado() != Enumerado.EstadoExpediente.ExpedienteAsignado.getId()) {
            JOptionPane.showMessageDialog(this, "El expediente no se encuentra en estado asignado.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (asignacionActual == null) {
            JOptionPane.showMessageDialog(this, "No se encontró una asignación activa para recibir este expediente.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!esAsignacionDelUsuarioActual()) {
            JOptionPane.showMessageDialog(this, "Solo el abogado designado puede aceptar la recepción del expediente.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (asignacionActual != null && asignacionActual.getAceptaRecepcion() == 1) {
            JOptionPane.showMessageDialog(this, "Este expediente ya fue recibido anteriormente.", "Validación", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Se registrará la recepción del expediente asignado. ¿Desea continuar?",
                "Confirmar recepción",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return confirmacion == JOptionPane.YES_OPTION;
    }

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
        btnAceptarExpediente = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jPanelPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        jPanelPrincipal.setPreferredSize(new java.awt.Dimension(908, 558));

        jPanelDatosSolicitud.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosSolicitud.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos de la solicitud"));

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

        cboUnidadOrganica.setEditable(true);
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

        textCorreoElectronico.setEnabled(false);

        textCelular.setEnabled(false);

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText("Celular");

        jLabel23.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel23.setText("Domicilio");

        textDomicilio.setEnabled(false);

        cboDireccionDomiciliaria.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        cboDireccionDomiciliaria.setEnabled(false);

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

        jPanelDatosUbicacion.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosUbicacion.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos Ubicación"));

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setText("Departamento");

        cboDepartamento.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        cboDepartamento.setEnabled(false);

        jLabel11.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel11.setText("Provincia");

        cboProvincia.setEditable(true);
        cboProvincia.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        cboProvincia.setEnabled(false);

        jLabel24.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel24.setText("Distrito");

        cboDistrito.setEditable(true);
        cboDistrito.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        cboDistrito.setEnabled(false);

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
        jRadiButonSiCorresponde.setEnabled(false);
        jRadiButonSiCorresponde.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadiButonSiCorrespondeActionPerformed(evt);
            }
        });

        jRadiButonNoCorresponde.setText("No corresponde a la SDRERC");
        jRadiButonNoCorresponde.setEnabled(false);
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
                .addContainerGap(578, Short.MAX_VALUE))
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

        btnAceptarExpediente.setBackground(new java.awt.Color(25, 120, 210));
        btnAceptarExpediente.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAceptarExpediente.setForeground(new java.awt.Color(255, 255, 255));
        btnAceptarExpediente.setText("ACEPTAR EXPEDIENTE");
        btnAceptarExpediente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarExpedienteActionPerformed(evt);
            }
        });

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
                        .addGap(347, 347, 347)
                        .addComponent(btnAceptarExpediente)
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
                .addGroup(jPanelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAceptarExpediente, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
        // TODO add your handling code here:        
        MenuPrincipal.ShowJPanel(new JPanelListadoExpedientesAsignados());
    }//GEN-LAST:event_btnRegresarActionPerformed

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

    private void btnAceptarExpedienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarExpedienteActionPerformed

        try
        {
            if (!validarAntesDeAceptarExpediente()) {
                return;
            }

            ExpedienteAsignacion oExpedienteAsignacion = new ExpedienteAsignacion();

            Enumerado.EstadoExpediente estadoExpedienteRecibido = Enumerado.EstadoExpediente.ExpedienteRecibido;
            oExpedienteAsignacion.setEtapaFlujo(estadoExpedienteRecibido.getId());
            oExpedienteAsignacion.setIdExpediente(idExpedienteOculto);
            oExpedienteAsignacion.setAceptaRecepcion(1);
            oExpedienteAsignacion.setIdUsuarioModifica(SessionContext.getIdUsuarioActual());
            oExpedienteAsignacion.setIdTecnico(SessionContext.getIdTecnicoActual());
            // Llamar al servicio
            if(idExpedienteOculto == 0)
            {
                JOptionPane.showMessageDialog(this,"No se encontró el expediente a recibir." ,"Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            expedienteAsignacionService.actualizarRecepcionExpediente(oExpedienteAsignacion);
            JOptionPane.showMessageDialog(this, "Expediente recibido correctamente.","Éxito", JOptionPane.INFORMATION_MESSAGE);

            //limpiarCampos();
            MenuPrincipal.ShowJPanel(new JPanelListadoExpedientesAsignados());
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "No se pudo recibir el expediente. Verifique que siga asignado.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAceptarExpedienteActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptarExpediente;
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
