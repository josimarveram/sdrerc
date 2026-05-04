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
import com.sdrerc.domain.model.ExpedienteAnalisAbogadoDetDoc.ExpedienteAnalisisAbogadoDetDoc;
import com.sdrerc.domain.model.ExpedienteAnalisAbogadoDetDoc.ExpedienteAnalisisAbogadoDetResponse;
import com.sdrerc.domain.model.ExpedienteAnalisisAbogado.ExpedienteAnalisisAbogado;
import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.domain.model.Provincia;
import com.sdrerc.ui.common.icon.IconUtils;
import com.sdrerc.util.TextFieldRules;
import java.awt.BorderLayout;
import java.awt.Color;
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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.JTableHeader;

/**
 *
 * @author usuario
 */
public class JPanelRegistrarExpedientePorTrabajar extends javax.swing.JPanel implements Scrollable
{
    private static final Color COLOR_FONDO = new Color(245, 247, 250);
    private static final Color COLOR_CARD = Color.WHITE;
    private static final Color COLOR_BORDE = new Color(220, 226, 235);
    private static final Color COLOR_TITULO = new Color(20, 45, 84);
    private static final Color COLOR_TEXTO_SECUNDARIO = new Color(92, 105, 123);
    private static final Color COLOR_ACCION = new Color(25, 120, 210);
    private static final Color COLOR_ACCION_HOVER = new Color(18, 101, 184);
    private static final Color COLOR_SECUNDARIO = new Color(239, 244, 250);

    private final ExpedienteService expedienteService;
    private final CatalogoItemService catalogoItemService;
    private final ExpedienteAsignacionService expedienteAsignacionService;
    private final UbigeoService ubigeoService;
    private final ExpedienteAnalisisAbogadoService expedienteAnalisisAbogadoService;
    private Integer idExpedienteOculto = 0;
    
    private ExpedienteAnalisisAbogado oExpedienteAnalisisAbogado;
    private List<ExpedienteAnalisisAbogadoDetDoc> listaDocumentos = new ArrayList<>();
    private int filaEditando = -1;
    private JLabel badgeEstadoActual;
    private JLabel badgeTramiteActual;
    private JLabel badgeTitularActual;
    
    /**
     * Creates new form JPanelRegistrarExpediente
     */
    public JPanelRegistrarExpedientePorTrabajar() {
        initComponents();
        corregirTextosVisibles();
        
        oExpedienteAnalisisAbogado = new ExpedienteAnalisisAbogado();
        
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
        
        TableColumn col = jTableDocumentosAnalisis.getColumnModel().getColumn(1);
        col.setMinWidth(0);
        col.setMaxWidth(0);
        col.setPreferredWidth(0);

        configurarFormularioTrabajoExpedientePremium();
    }

    private void corregirTextosVisibles() {
        btnGuardarAnalisis.setText("Registrar análisis");
        btnRegresar.setText("Regresar");
        btnRegresar2.setText("Editar datos de solicitud");
        btnGenerarDocumento.setText("Generar plantilla");
        btnAgregarTipoAnalisis.setText("Agregar documento");
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

    private void configurarFormularioTrabajoExpedientePremium() {
        setBackground(COLOR_FONDO);
        setLayout(new BorderLayout());

        corregirTextosVisibles();
        configurarEstilosTrabajoExpediente();
        configurarTablaDocumentosPremium();

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setBackground(COLOR_FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 14, 0);

        contenido.add(crearCabeceraTrabajo(), gbc);

        gbc.gridy++;
        contenido.add(crearCardResumenSolicitud(), gbc);

        gbc.gridy++;
        contenido.add(crearFilaPlantillaResultado(), gbc);

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contenido.add(crearCardDocumentosAnalizados(), gbc);

        gbc.gridy++;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 0, 0);
        contenido.add(crearBarraAccionesTrabajo(), gbc);

        JScrollPane scrollPane = new JScrollPane(contenido);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(COLOR_FONDO);

        jPanelPrincipal.removeAll();
        jPanelPrincipal.setLayout(new BorderLayout());
        jPanelPrincipal.setBackground(COLOR_FONDO);
        jPanelPrincipal.add(scrollPane, BorderLayout.CENTER);

        removeAll();
        add(jPanelPrincipal, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel crearCabeceraTrabajo() {
        JPanel header = new JPanel(new BorderLayout(12, 10));
        header.setOpaque(false);

        JPanel textos = new JPanel(new GridBagLayout());
        textos.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel("Trabajo del expediente");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(COLOR_TITULO);
        textos.add(titulo, gbc);

        gbc.gridy++;
        JLabel subtitulo = new JLabel("Revise la solicitud, registre documentos analizados y defina el resultado del análisis.");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitulo.setForeground(COLOR_TEXTO_SECUNDARIO);
        textos.add(subtitulo, gbc);

        JPanel badges = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        badges.setOpaque(false);
        badgeEstadoActual = crearBadge("Estado actual: Recibido");
        badgeTramiteActual = crearBadge("N° trámite: -");
        badgeTitularActual = crearBadge("Titular: -");
        badges.add(badgeEstadoActual);
        badges.add(badgeTramiteActual);
        badges.add(badgeTitularActual);

        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 0, 0);
        textos.add(badges, gbc);

        header.add(textos, BorderLayout.CENTER);
        return header;
    }

    private JLabel crearBadge(String texto) {
        JLabel badge = new JLabel(texto);
        badge.setOpaque(true);
        badge.setBackground(new Color(232, 241, 252));
        badge.setForeground(COLOR_TITULO);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(204, 222, 244)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return badge;
    }

    private JPanel crearCardResumenSolicitud() {
        JPanel card = crearCardTrabajo("Resumen de la solicitud");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        int y = 0;
        agregarCampoTrabajo(grid, 0, y, 1, "Fecha recepción", spFechaRecepcion);
        agregarCampoTrabajo(grid, 1, y, 1, "Fecha solicitud", spFechaSolicitud);
        agregarCampoTrabajo(grid, 2, y, 1, "N° trámite web", textNumeroTramiteDocumento);
        agregarCampoTrabajo(grid, 3, y, 1, "Tipo solicitud", cboTipoSolicitud);

        y++;
        agregarCampoTrabajo(grid, 0, y, 1, "Tipo documento", cboTipoDocumento);
        agregarCampoTrabajo(grid, 1, y, 1, "N° documento", textNumeroDocumento);
        agregarCampoTrabajo(grid, 2, y, 1, "Tipo acta", cboTipoActa);
        agregarCampoTrabajo(grid, 3, y, 1, "N° acta", textNumeroActa);

        y++;
        agregarCampoTrabajo(grid, 0, y, 1, "Grupo familiar", cboGrupoFamiliar);
        agregarCampoTrabajo(grid, 1, y, 1, "Grado parentesco", cboGradoParentesco);
        agregarCampoTrabajo(grid, 2, y, 2, "Tipo procedimiento registral", cboTipoProcedimientoRegistral);

        y++;
        agregarCampoTrabajo(grid, 0, y, 1, "DNI remitente", textDniRemitente);
        agregarCampoTrabajo(grid, 1, y, 2, "Apellidos y nombres remitente", textApellidosNombreRemitente);
        agregarCampoTrabajo(grid, 3, y, 1, "Unidad orgánica", cboUnidadOrganica);

        y++;
        agregarCampoTrabajo(grid, 0, y, 1, "DNI / N° documento titular", textNumeroDocumentoTitular);
        agregarCampoTrabajo(grid, 1, y, 3, "Apellidos y nombres titular", textApellidosNombreTitular);

        JPanel acciones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        acciones.setOpaque(false);
        acciones.add(btnRegresar2);

        card.add(grid, BorderLayout.CENTER);
        card.add(acciones, BorderLayout.SOUTH);
        return card;
    }

    private JPanel crearFilaPlantillaResultado() {
        JPanel fila = new JPanel(new GridBagLayout());
        fila.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 10);

        gbc.gridx = 0;
        gbc.weightx = 0.42;
        fila.add(crearCardPlantillas(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.58;
        gbc.insets = new Insets(0, 10, 0, 0);
        fila.add(crearCardResultadoAnalisis(), gbc);
        return fila;
    }

    private JPanel crearCardPlantillas() {
        JPanel card = crearCardTrabajo("Plantillas de documento");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        agregarCampoTrabajo(grid, 0, 0, 2, "Plantilla documento", cboPlantillaDocumento);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 0, 0);
        grid.add(btnGenerarDocumento, gbc);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearCardResultadoAnalisis() {
        JPanel card = crearCardTrabajo("Resultado del análisis");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        agregarCampoTrabajo(grid, 0, 0, 1, "¿Tiene observación?", cboTieneObservacion);
        agregarCampoTrabajo(grid, 1, 0, 1, "Tipo de observación", cboTipoObservacion);
        agregarCampoTrabajo(grid, 2, 0, 1, "Resultado", cboAnalisisAbogado);
        agregarCampoTrabajo(grid, 0, 1, 3, "Descripción de la observación", jScrollPane1);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearCardDocumentosAnalizados() {
        JPanel card = crearCardTrabajo("Documentos analizados");
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        agregarCampoTrabajo(grid, 0, 0, 1, "Tipo de documento", cboTipoDocumentoAnalizado);
        agregarCampoTrabajo(grid, 1, 0, 2, "Descripción", textDescripcionDocumentoAnalisis);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(18, 8, 8, 0);
        grid.add(btnAgregarTipoAnalisis, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(8, 0, 0, 0);
        jScrollPane2.setBorder(BorderFactory.createLineBorder(COLOR_BORDE));
        jScrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setPreferredSize(new Dimension(760, 190));
        grid.add(jScrollPane2, gbc);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearBarraAccionesTrabajo() {
        JPanel barra = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        barra.setOpaque(false);
        barra.add(btnRegresar);
        barra.add(btnGuardarAnalisis);
        return barra;
    }

    private JPanel crearCardTrabajo(String titulo) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JLabel label = new JLabel(titulo);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(COLOR_TITULO);
        card.add(label, BorderLayout.NORTH);
        return card;
    }

    private void agregarCampoTrabajo(JPanel parent, int x, int y, int width, String label, JComponent component) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 5));
        wrapper.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_TEXTO_SECUNDARIO);
        wrapper.add(lbl, BorderLayout.NORTH);

        dimensionarCampoTrabajo(component, width);
        wrapper.add(component, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.weightx = width;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 10);
        parent.add(wrapper, gbc);
    }

    private void dimensionarCampoTrabajo(JComponent component, int width) {
        int ancho = width > 1 ? 360 : 190;
        if (component instanceof JScrollPane) {
            component.setPreferredSize(new Dimension(ancho, 92));
        } else {
            component.setPreferredSize(new Dimension(ancho, 36));
            component.setMinimumSize(new Dimension(140, 34));
        }
    }

    private void configurarEstilosTrabajoExpediente() {
        estilizarBotonPrimario(btnGuardarAnalisis);
        estilizarBotonPrimario(btnGenerarDocumento);
        estilizarBotonSecundario(btnRegresar);
        estilizarBotonSecundario(btnRegresar2);
        estilizarBotonSecundario(btnAgregarTipoAnalisis);

        btnGenerarDocumento.setToolTipText("Genera el documento base seleccionado para el expediente.");
        btnAgregarTipoAnalisis.setToolTipText("Agregar documento analizado");
        btnGuardarAnalisis.setToolTipText("Registra el análisis y actualiza el estado del expediente.");
        btnRegresar.setToolTipText("Volver al listado de expedientes por trabajar.");

        btnGuardarAnalisis.setIcon(IconUtils.load("active.svg", 16));
        btnGenerarDocumento.setIcon(IconUtils.load("file.svg", 16));
        btnRegresar.setIcon(IconUtils.load("clear.svg", 16));
        btnAgregarTipoAnalisis.setIcon(IconUtils.load("add.svg", 16));

        configurarCamposConsultaSolicitud();
        configurarTooltipsSolicitud();
        estilizarAreaTexto(jTextArea1);

        if (cboTieneObservacion.isEnabled()) {
            cboTieneObservacion.addActionListener(e -> aplicarEstadoObservacion());
        }
        aplicarEstadoObservacion();
    }

    private void estilizarBotonPrimario(JButton button) {
        button.setBackground(COLOR_ACCION);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(COLOR_ACCION_HOVER);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(COLOR_ACCION);
            }
        });
    }

    private void estilizarBotonSecundario(JButton button) {
        button.setBackground(COLOR_SECUNDARIO);
        button.setForeground(COLOR_TITULO);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE),
                BorderFactory.createEmptyBorder(8, 13, 8, 13)));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void configurarCamposConsultaSolicitud() {
        JComponent[] componentes = {
            spFechaRecepcion, spFechaSolicitud, textNumeroTramiteDocumento, cboTipoDocumento,
            textNumeroDocumento, cboTipoActa, textNumeroActa, cboGrupoFamiliar,
            cboGradoParentesco, cboTipoProcedimientoRegistral, cboTipoSolicitud,
            textDniRemitente, textApellidosNombreRemitente, cboUnidadOrganica,
            textNumeroDocumentoTitular, textApellidosNombreTitular
        };
        for (JComponent componente : componentes) {
            componente.setEnabled(false);
        }
    }

    private void configurarTooltipsSolicitud() {
        aplicarTooltipTexto(textNumeroTramiteDocumento);
        aplicarTooltipTexto(textNumeroDocumento);
        aplicarTooltipTexto(textNumeroActa);
        aplicarTooltipTexto(textDniRemitente);
        aplicarTooltipTexto(textApellidosNombreRemitente);
        aplicarTooltipTexto(textNumeroDocumentoTitular);
        aplicarTooltipTexto(textApellidosNombreTitular);
        aplicarTooltipCombo(cboUnidadOrganica);
        aplicarTooltipCombo(cboTipoProcedimientoRegistral);
        aplicarTooltipCombo(cboTipoSolicitud);
        actualizarBadgesResumen();
    }

    private void aplicarTooltipTexto(JTextField field) {
        field.setToolTipText(field.getText());
    }

    private void aplicarTooltipCombo(JComboBox combo) {
        Object selected = combo.getSelectedItem();
        combo.setToolTipText(selected == null ? "" : selected.toString());
    }

    private void estilizarAreaTexto(JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jScrollPane1.setBorder(BorderFactory.createLineBorder(COLOR_BORDE));
    }

    private void aplicarEstadoObservacion() {
        if (!cboTieneObservacion.isEnabled()) {
            cboTipoObservacion.setEnabled(false);
            jTextArea1.setEnabled(false);
            return;
        }
        boolean tieneObservacion = esSeleccionSi(cboTieneObservacion.getSelectedItem());
        cboTipoObservacion.setEnabled(tieneObservacion);
        jTextArea1.setEnabled(tieneObservacion);
        if (!tieneObservacion) {
            jTextArea1.setText("");
        }
    }

    private boolean esSeleccionSi(Object item) {
        if (item == null) {
            return false;
        }
        String texto = normalizarTexto(item.toString());
        return "SI".equals(texto) || "S".equals(texto);
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim().toUpperCase()
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U");
    }

    private void actualizarBadgesResumen() {
        if (badgeTramiteActual != null) {
            badgeTramiteActual.setText("N° trámite: " + valorOTexto(textNumeroTramiteDocumento.getText(), "-"));
        }
        if (badgeTitularActual != null) {
            badgeTitularActual.setText("Titular: " + valorOTexto(textApellidosNombreTitular.getText(), "-"));
            badgeTitularActual.setToolTipText(textApellidosNombreTitular.getText());
        }
    }

    private String valorOTexto(String valor, String defecto) {
        return valor == null || valor.trim().isEmpty() ? defecto : valor.trim();
    }
    
    public void cargarExpediente(String idExpediente) throws Exception 
    {        
        Expediente lista = expedienteService.buscarporid(Integer.parseInt(idExpediente));           
        idExpedienteOculto = lista.getIdExpediente();
        
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

        configurarCamposConsultaSolicitud();
        configurarTooltipsSolicitud();
        
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
        textDescripcionDocumentoAnalisis.setText("");

        // Resetear JComboBoxes al primer elemento
        if(cboTipoDocumentoAnalizado.getItemCount() > 0) cboTipoDocumentoAnalizado.setSelectedIndex(0);
        if(cboAnalisisAbogado.getItemCount() > 0)        cboAnalisisAbogado.setSelectedIndex(0);
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
              new Object[]{
                  "N°", 
                  "ID_TIPO_DOCUMENTO", // OCULTO
                  "Documento Analizado", 
                  "Desc Documento", 
                  "Eliminar"}, 0
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
                return column == 4;
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
        jTableDocumentosAnalisis.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        ajustarColumnaTabla(0, 45, 55, 65);
        ajustarColumnaTabla(1, 0, 0, 0);
        ajustarColumnaTabla(2, 170, 240, 420);
        ajustarColumnaTabla(3, 220, 420, 700);
        ajustarColumnaTabla(4, 70, 80, 95);
    }

    private void ajustarColumnaTabla(int index, int min, int pref, int max) {
        TableColumn column = jTableDocumentosAnalisis.getColumnModel().getColumn(index);
        column.setMinWidth(min);
        column.setPreferredWidth(pref);
        column.setMaxWidth(max);
    }

    private void configurarTablaDocumentosPremium() {
        jTableDocumentosAnalisis.setRowHeight(30);
        jTableDocumentosAnalisis.setFillsViewportHeight(true);
        jTableDocumentosAnalisis.setShowGrid(false);
        jTableDocumentosAnalisis.setIntercellSpacing(new Dimension(0, 0));
        jTableDocumentosAnalisis.setSelectionBackground(new Color(223, 237, 252));
        jTableDocumentosAnalisis.setSelectionForeground(COLOR_TITULO);
        jTableDocumentosAnalisis.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

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
                    component.setForeground(new Color(43, 52, 66));
                }
                return component;
            }
        };
        renderer.setVerticalAlignment(SwingConstants.CENTER);
        jTableDocumentosAnalisis.setDefaultRenderer(Object.class, renderer);
        configurarColumnaNumero();
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
                    d.getDescripcionDocumento(),
                    "Eliminar"
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
        private int fila;

        public EliminarEditor(JTable table) {
            super(new JCheckBox());
            this.table = table;

            button = new JButton("🗑");
            button.setFocusPainted(false);
            button.addActionListener(e -> eliminarFila());
        }
        
        private void eliminarFila() {

            int r = JOptionPane.showConfirmDialog(
                    table,
                    "¿Eliminar este documento?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION
            );

            if (r == JOptionPane.YES_OPTION) {

                // Fila real del modelo (importante si hay sort/filter)
                int filaModelo = table.convertRowIndexToModel(fila);

                DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.removeRow(filaModelo);

                // Elimina también del modelo lógico
                oExpedienteAnalisisAbogado
                    .getExpedienteAnalisisAbogadoDetDoc()
                    .remove(filaModelo);
            }

            fireEditingStopped();
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {

            this.fila = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Eliminar";
        }
        
        /*
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
        */
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
        jPanelGenerarPlantilla = new javax.swing.JPanel();
        cboPlantillaDocumento = new javax.swing.JComboBox();
        btnGenerarDocumento = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        jPanelDatosAnalisis = new javax.swing.JPanel();
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
        jPanelResultadoVerificacion = new javax.swing.JPanel();
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
        jLabel8.setText("Fecha Solicitud");

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

        jPanelGenerarPlantilla.setBackground(new java.awt.Color(255, 255, 255));
        jPanelGenerarPlantilla.setBorder(javax.swing.BorderFactory.createTitledBorder("Generar Plantilla"));

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

        javax.swing.GroupLayout jPanelGenerarPlantillaLayout = new javax.swing.GroupLayout(jPanelGenerarPlantilla);
        jPanelGenerarPlantilla.setLayout(jPanelGenerarPlantillaLayout);
        jPanelGenerarPlantillaLayout.setHorizontalGroup(
            jPanelGenerarPlantillaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelGenerarPlantillaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelGenerarPlantillaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                    .addComponent(cboPlantillaDocumento, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(28, 28, 28)
                .addComponent(btnGenerarDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelGenerarPlantillaLayout.setVerticalGroup(
            jPanelGenerarPlantillaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelGenerarPlantillaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelGenerarPlantillaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboPlantillaDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGenerarDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jPanelPrincipal.add(jPanelGenerarPlantilla, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 500, 110));

        jPanelDatosAnalisis.setBackground(new java.awt.Color(255, 255, 255));
        jPanelDatosAnalisis.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos Analisis"));
        jPanelDatosAnalisis.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

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

        jPanelDatosAnalisis.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 105, 498, 146));
        jPanelDatosAnalisis.add(cboTipoDocumentoAnalizado, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 205, 40));

        btnAgregarTipoAnalisis.setBackground(new java.awt.Color(25, 120, 210));
        btnAgregarTipoAnalisis.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAgregarTipoAnalisis.setForeground(new java.awt.Color(255, 255, 255));
        btnAgregarTipoAnalisis.setText("+");
        btnAgregarTipoAnalisis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarTipoAnalisisActionPerformed(evt);
            }
        });
        jPanelDatosAnalisis.add(btnAgregarTipoAnalisis, new org.netbeans.lib.awtextra.AbsoluteConstraints(444, 45, 40, 40));

        jLabel24.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel24.setText("Tipo Documento Analizado");
        jPanelDatosAnalisis.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 24, 205, -1));

        jLabel25.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel25.setText("DESCRIPCIÓN");
        jPanelDatosAnalisis.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(242, 24, 149, -1));
        jPanelDatosAnalisis.add(textDescripcionDocumentoAnalisis, new org.netbeans.lib.awtextra.AbsoluteConstraints(242, 44, 190, 40));

        jLabel27.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel27.setText("Análisis");
        jPanelDatosAnalisis.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 263, 183, -1));
        jPanelDatosAnalisis.add(cboAnalisisAbogado, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 286, 290, 40));

        btnGuardarAnalisis.setBackground(new java.awt.Color(25, 120, 210));
        btnGuardarAnalisis.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardarAnalisis.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarAnalisis.setText("GUARDAR ANALISIS");
        btnGuardarAnalisis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarAnalisisActionPerformed(evt);
            }
        });
        jPanelDatosAnalisis.add(btnGuardarAnalisis, new org.netbeans.lib.awtextra.AbsoluteConstraints(363, 263, 146, 40));

        btnRegresar.setBackground(new java.awt.Color(25, 120, 210));
        btnRegresar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegresar.setForeground(new java.awt.Color(255, 255, 255));
        btnRegresar.setText("REGRESAR");
        btnRegresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresarActionPerformed(evt);
            }
        });
        jPanelDatosAnalisis.add(btnRegresar, new org.netbeans.lib.awtextra.AbsoluteConstraints(363, 309, 146, 40));

        jPanelPrincipal.add(jPanelDatosAnalisis, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 340, 520, 360));

        jPanelResultadoVerificacion.setBackground(new java.awt.Color(255, 255, 255));
        jPanelResultadoVerificacion.setBorder(javax.swing.BorderFactory.createTitledBorder("Resultado de la verificación"));

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

        javax.swing.GroupLayout jPanelResultadoVerificacionLayout = new javax.swing.GroupLayout(jPanelResultadoVerificacion);
        jPanelResultadoVerificacion.setLayout(jPanelResultadoVerificacionLayout);
        jPanelResultadoVerificacionLayout.setHorizontalGroup(
            jPanelResultadoVerificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelResultadoVerificacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelResultadoVerificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelResultadoVerificacionLayout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addContainerGap())
                    .addGroup(jPanelResultadoVerificacionLayout.createSequentialGroup()
                        .addGroup(jPanelResultadoVerificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cboTieneObservacion, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanelResultadoVerificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cboTipoObservacion, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27))
                    .addGroup(jPanelResultadoVerificacionLayout.createSequentialGroup()
                        .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanelResultadoVerificacionLayout.setVerticalGroup(
            jPanelResultadoVerificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelResultadoVerificacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelResultadoVerificacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelResultadoVerificacionLayout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboTieneObservacion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelResultadoVerificacionLayout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboTipoObservacion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanelPrincipal.add(jPanelResultadoVerificacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 460, 500, 240));

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

    
    private void btnAgregarTipoAnalisisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarTipoAnalisisActionPerformed
        if (cboTipoDocumentoAnalizado.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Seleccione el tipo de documento analizado.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String v1 = cboTipoDocumentoAnalizado.getSelectedItem().toString();
        String v2 = textDescripcionDocumentoAnalisis.getText();

        if(v2 == null || v2.trim().isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Ingrese la descripción del documento analizado.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        } 
        // Agregando a la tablaDetalle
        //ExpedienteAnalisisAbogadoDetDoc det = new ExpedienteAnalisisAbogadoDetDoc();
        //det.setIdTipoDocumentoAnalizado(cboTipoDocumentoAnalizado.getSelectedIndex());
        //det.setDescDocumento(v2);
        //det.setActive(1);
        //det.setUsuarioRegistro(1);        
        
        CatalogoItem catalogoTipoDocumentoAnalizado = (CatalogoItem) cboTipoDocumentoAnalizado.getSelectedItem();   
        String v3 = catalogoTipoDocumentoAnalizado.getDescripcion();

        //det.setIdTipoDocumentoAnalizado (catalogoTipoDocumentoAnalizado.getIdCatalogoItem());
        
        //oExpedienteAnalisisAbogado.agregarExpedienteAnalisisAbogadoDetDoc(det);
        
        Integer idDocumentoAnalisis = catalogoTipoDocumentoAnalizado.getIdCatalogoItem();
        if(filaEditando == -1) 
        {
            
            ExpedienteAnalisisAbogadoDetDoc det = new ExpedienteAnalisisAbogadoDetDoc();
            det.setIdTipoDocumentoAnalizado(idDocumentoAnalisis);
            det.setDescTipoDocumentoAnalizado(v3);
            det.setDescDocumento(v2.trim());
            det.setActive(1);
            det.setUsuarioRegistro(1);

            listaDocumentos.add(det);
            modelo.addRow(new Object[]{"", idDocumentoAnalisis, v3, v2.trim(), "Eliminar"});
        } 
        else 
        {
            ExpedienteAnalisisAbogadoDetDoc det = new ExpedienteAnalisisAbogadoDetDoc();
            det.setIdTipoDocumentoAnalizado(catalogoTipoDocumentoAnalizado.getIdCatalogoItem());
            det.setDescDocumento(v2.trim());
            
            modelo.setValueAt(v1, filaEditando, 1);
            modelo.setValueAt(v2.trim(), filaEditando, 2);
            filaEditando = -1;
        }
        textDescripcionDocumentoAnalisis.setText("");
    }//GEN-LAST:event_btnAgregarTipoAnalisisActionPerformed

    
    private void sincronizarDetalleDesdeTabla() {

        DefaultTableModel model =
            (DefaultTableModel) jTableDocumentosAnalisis.getModel();
        List<ExpedienteAnalisisAbogadoDetDoc> lista = new ArrayList<>();


        for (int i = 0; i < model.getRowCount(); i++) {

            ExpedienteAnalisisAbogadoDetDoc det =
                new ExpedienteAnalisisAbogadoDetDoc();
            
            int idTipoDocumento =
            Integer.parseInt(model.getValueAt(i, 1).toString()); // ID oculto
            
            String tipoDocumento =
                model.getValueAt(i, 1).toString();

            String descripcion =
                model.getValueAt(i, 3).toString();


            // ⚠️ aquí debes mapear correctamente el ID real
            det.setIdTipoDocumentoAnalizado(idTipoDocumento);
            det.setDescDocumento(descripcion);
            det.setActive(1);
            det.setUsuarioRegistro(1);

            lista.add(det);
        }

        oExpedienteAnalisisAbogado
            .getExpedienteAnalisisAbogadoDetDoc()
            .clear();

        oExpedienteAnalisisAbogado
            .getExpedienteAnalisisAbogadoDetDoc()
            .addAll(lista);
    }

    private boolean validarAntesDeRegistrarAnalisis() {
        return validarExpedienteCargado()
                && validarResultadoAnalisis()
                && validarDocumentosAnalizados()
                && validarObservacionSiAplica();
    }

    private boolean validarExpedienteCargado() {
        if (idExpedienteOculto == null || idExpedienteOculto == 0) {
            JOptionPane.showMessageDialog(this, "No hay expediente cargado para registrar el análisis.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean validarResultadoAnalisis() {
        if (cboAnalisisAbogado.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Seleccione el resultado del análisis.", "Validación", JOptionPane.WARNING_MESSAGE);
            cboAnalisisAbogado.requestFocusInWindow();
            return false;
        }
        return true;
    }

    private boolean validarDocumentosAnalizados() {
        if (jTableDocumentosAnalisis.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Agregue al menos un documento analizado.", "Validación", JOptionPane.WARNING_MESSAGE);
            cboTipoDocumentoAnalizado.requestFocusInWindow();
            return false;
        }
        return true;
    }

    private boolean validarObservacionSiAplica() {
        if (!cboTieneObservacion.isEnabled() || !esSeleccionSi(cboTieneObservacion.getSelectedItem())) {
            return true;
        }

        if (cboTipoObservacion.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Seleccione el tipo de observación.", "Validación", JOptionPane.WARNING_MESSAGE);
            cboTipoObservacion.requestFocusInWindow();
            return false;
        }

        if (jTextArea1.getText() == null || jTextArea1.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese la descripción de la observación.", "Validación", JOptionPane.WARNING_MESSAGE);
            jTextArea1.requestFocusInWindow();
            return false;
        }

        return true;
    }

    private void btnGuardarAnalisisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarAnalisisActionPerformed
        // TODO add your handling code here:
        
        try
        {
            if (!validarAntesDeRegistrarAnalisis()) {
                return;
            }

            int confirmacion = JOptionPane.showConfirmDialog(
                    this,
                    "Se registrará el análisis del expediente y se actualizará su estado. ¿Desea continuar?",
                    "Confirmar registro de análisis",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }

            // 🔥 PASO 1: reconstruir desde JTable
            sincronizarDetalleDesdeTabla();
        
            List<ExpedienteAnalisisAbogadoDetDoc> dataTo = oExpedienteAnalisisAbogado.getExpedienteAnalisisAbogadoDetDoc();
            
            if(dataTo == null || dataTo.isEmpty())
            {
                JOptionPane.showMessageDialog(this,"Agregue al menos un documento analizado." ,"Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Enumerado.EstadoExpediente estadoExpedienteRecibido = Enumerado.EstadoExpediente.ExpedienteAtendido;
            oExpedienteAnalisisAbogado.setIdEstadoExpediente(estadoExpedienteRecibido.getId());            
            CatalogoItem catalogoAnalisisAbogado = (CatalogoItem) cboAnalisisAbogado.getSelectedItem();
            int idAnalisisAbogado = catalogoAnalisisAbogado.getIdCatalogoItem();
            
            
            //tipoProcedimientoRegistral
            //CatalogoItem catalogoTipoProcedimientoRegistral = (CatalogoItem) cboTipoProcedimientoRegistral.getSelectedItem();
            //int idTipoProcedimientoRegistral = catalogoTipoProcedimientoRegistral.getIdCatalogoItem();
            
            oExpedienteAnalisisAbogado.setIdAnalisis(idAnalisisAbogado);                        
            oExpedienteAnalisisAbogado.setIdExpediente(idExpedienteOculto);                                    
            oExpedienteAnalisisAbogado.setIdAbogado(1);            
            oExpedienteAnalisisAbogado.setUsuarioRegistro(1);
            
            // Llamar al servicio
            if(idExpedienteOculto == 0)
            {
                JOptionPane.showMessageDialog(this,"No hay expediente cargado para registrar el análisis." ,"Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            expedienteAnalisisAbogadoService.agregarAnalisisAbogado(oExpedienteAnalisisAbogado);
            JOptionPane.showMessageDialog(this, "Análisis registrado correctamente","Éxito", JOptionPane.INFORMATION_MESSAGE);

            //limpiarCampos();
            MenuPrincipal.ShowJPanel(new JPanelListadoExpedientesPorTrabajar());
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnGuardarAnalisisActionPerformed

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
                                             
        MenuPrincipal.ShowJPanel(new JPanelListadoExpedientesPorTrabajar());
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRegresarActionPerformed

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 24;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(visibleRect.height - 48, 120);
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
    private javax.swing.JPanel jPanelDatosAnalisis;
    private javax.swing.JPanel jPanelDatosSolicitud;
    private javax.swing.JPanel jPanelGenerarPlantilla;
    private javax.swing.JPanel jPanelPrincipal;
    private javax.swing.JPanel jPanelResultadoVerificacion;
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
