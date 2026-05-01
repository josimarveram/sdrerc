/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.usuario;

import com.sdrerc.ui.views.role.*;
import com.sdrerc.application.EquipoJuridicoService;
import com.sdrerc.application.RoleService;
import com.sdrerc.application.SupervisionService;
import com.sdrerc.application.UserService;
import com.sdrerc.domain.model.EquipoJuridicoImportPreview;
import com.sdrerc.domain.model.User;
import com.sdrerc.ui.table.ButtonEditor;
import com.sdrerc.ui.table.ButtonCellValue;
import com.sdrerc.ui.table.ButtonEditorAsignar;
import com.sdrerc.ui.table.ButtonEditorUsuario;
import com.sdrerc.ui.table.ButtonRenderer;
import com.sdrerc.ui.views.asignacion.JDialogTecnico;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 *
 * @author David
 */
public class JPanelListadoUsuario extends javax.swing.JPanel {

    private DefaultTableModel model;
    private UserService userService; // 👈 AQUÍ
    private RoleService roleService; // 👈 AQUÍ
    private SupervisionService supervisionService; // 👈 AQUÍ
    private EquipoJuridicoService equipoJuridicoService;
    private User usuario;
    private Long roleIdSeleccionado;
    private String roleNameSeleccionado;
    private String roleDescriptionSeleccionado;
    private String statusSeleccionado;
    private final JButton btnNuevoEquipoJuridico = new JButton("Nuevo abogado/supervisor");
    private final JButton btnDescargarPlantillaEquipo = new JButton("Descargar plantilla Excel");
    private final JButton btnPrevisualizarPlantillaEquipo = new JButton("Previsualizar Excel");
    private final JButton btnVincularTecnico = new JButton("Vincular técnico");
    
    public static final int COL_ID = 0;
    private static final int COL_NOMBRE = 1;
    private static final int COL_DESCRIPCION = 2;
    private static final int COL_ESTADO = 3;
    private static final int COL_EDITAR = 4;
    private static final int COL_ACTIVAR = 5;
    private static final int COL_RESET = 6;
    private static final int COL_ASIGNAR_ROL = 7;
    private static final int COL_ASIGNAR_ABOGADO = 8;
    /**
     * Creates new form JPanelListadoRole
     */
    public JPanelListadoUsuario() {
        initComponents();
        usuario = new User();
        userService = new UserService(); // 👈 SE INICIALIZA AQUÍ   
        roleService = new RoleService(); // 👈 SE INICIALIZA AQUÍ     
        supervisionService = new SupervisionService(); // 👈 SE INICIALIZA AQUÍ     
        equipoJuridicoService = new EquipoJuridicoService();
        initTable();
        initFiltros(); 
        initEventos(); 
        configurarEventosTabla(); // 👈 AQUÍ
        corregirTextosVisibles();
        configurarLayoutUsuarios();
        configurarFiltrosUsuarios();
        configurarBotonesUsuarios();
        configurarTablaUsuarios();
        configurarRenderersUsuarios();
        //cargarRoles();
        buscarUsuarios();
        
    }

    private void corregirTextosVisibles() {
        jLabel2.setText("Mantenimiento de Usuarios");
        jLabel7.setText("Buscar usuario");
        jLabel8.setText("Estado");
        btnNuevo1.setText("Nuevo usuario");
        btnBusqueda.setText("Buscar");
        btnLimpiar1.setText("Limpiar");
    }

    private void configurarLayoutUsuarios() {
        removeAll();
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        setBackground(new Color(245, 247, 250));

        JPanel headerPanel = new JPanel(new BorderLayout(12, 4));
        headerPanel.setOpaque(false);
        JLabel subtitulo = new JLabel("Gestión de accesos, estado, roles y asociación de técnicos/abogados");
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitulo.setForeground(new Color(93, 105, 119));
        jLabel2.setFont(new Font("Arial", Font.BOLD, 22));
        jLabel2.setForeground(new Color(25, 42, 62));
        jLabel2.setHorizontalAlignment(JLabel.LEFT);
        JPanel accionesHeader = new JPanel(new GridBagLayout());
        accionesHeader.setOpaque(false);
        GridBagConstraints gbcAcciones = new GridBagConstraints();
        gbcAcciones.insets = new Insets(0, 0, 0, 8);
        accionesHeader.add(btnNuevoEquipoJuridico, gbcAcciones);
        accionesHeader.add(btnDescargarPlantillaEquipo, gbcAcciones);
        accionesHeader.add(btnPrevisualizarPlantillaEquipo, gbcAcciones);
        accionesHeader.add(btnVincularTecnico, gbcAcciones);
        gbcAcciones.insets = new Insets(0, 0, 0, 0);
        accionesHeader.add(btnNuevo1, gbcAcciones);

        headerPanel.add(jLabel2, BorderLayout.NORTH);
        headerPanel.add(subtitulo, BorderLayout.CENTER);
        headerPanel.add(accionesHeader, BorderLayout.EAST);

        JPanel filtrosPanel = new JPanel(new GridBagLayout());
        filtrosPanel.setBackground(Color.WHITE);
        filtrosPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        filtrosPanel.add(jLabel7, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtrosPanel.add(txtBuscarUsuario, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        filtrosPanel.add(jLabel8, gbc);

        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtrosPanel.add(cboFiltroEstado, gbc);

        gbc.gridx = 4;
        filtrosPanel.add(btnBusqueda, gbc);

        gbc.gridx = 5;
        gbc.insets = new Insets(0, 0, 0, 0);
        filtrosPanel.add(btnLimpiar1, gbc);

        JPanel topPanel = new JPanel(new BorderLayout(0, 14));
        topPanel.setOpaque(false);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(filtrosPanel, BorderLayout.CENTER);

        jScrollPane2.setBorder(BorderFactory.createLineBorder(new Color(218, 224, 231)));
        jScrollPane2.setViewportBorder(BorderFactory.createEmptyBorder());
        jScrollPane2.setPreferredSize(new Dimension(980, 430));
        jScrollPane2.setMinimumSize(new Dimension(760, 280));

        add(topPanel, BorderLayout.NORTH);
        add(jScrollPane2, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void configurarFiltrosUsuarios() {
        txtBuscarUsuario.setPreferredSize(new Dimension(360, 34));
        txtBuscarUsuario.setMinimumSize(new Dimension(260, 34));
        cboFiltroEstado.setPreferredSize(new Dimension(145, 34));
        cboFiltroEstado.setMinimumSize(new Dimension(130, 34));
        txtBuscarUsuario.setToolTipText("Buscar por nombre de usuario");
        cboFiltroEstado.setToolTipText("Filtrar usuarios por estado");
    }

    private void configurarBotonesUsuarios() {
        Dimension botonPrincipal = new Dimension(132, 36);
        Dimension botonEquipo = new Dimension(198, 36);
        Dimension botonPlantilla = new Dimension(186, 36);
        Dimension botonPrevisualizar = new Dimension(154, 36);
        Dimension botonVincular = new Dimension(142, 36);
        Dimension botonFiltro = new Dimension(96, 34);
        btnNuevo1.setPreferredSize(botonPrincipal);
        btnNuevo1.setMinimumSize(botonPrincipal);
        btnNuevoEquipoJuridico.setPreferredSize(botonEquipo);
        btnNuevoEquipoJuridico.setMinimumSize(botonEquipo);
        btnDescargarPlantillaEquipo.setPreferredSize(botonPlantilla);
        btnDescargarPlantillaEquipo.setMinimumSize(botonPlantilla);
        btnPrevisualizarPlantillaEquipo.setPreferredSize(botonPrevisualizar);
        btnPrevisualizarPlantillaEquipo.setMinimumSize(botonPrevisualizar);
        btnVincularTecnico.setPreferredSize(botonVincular);
        btnVincularTecnico.setMinimumSize(botonVincular);
        btnBusqueda.setPreferredSize(botonFiltro);
        btnBusqueda.setMinimumSize(botonFiltro);
        btnLimpiar1.setPreferredSize(botonFiltro);
        btnLimpiar1.setMinimumSize(botonFiltro);

        btnNuevo1.setToolTipText("Registrar un nuevo usuario");
        btnNuevoEquipoJuridico.setToolTipText("Registrar abogado o supervisor creando técnico, usuario y roles");
        btnDescargarPlantillaEquipo.setToolTipText("Descargar plantilla oficial para carga masiva de equipo jurídico");
        btnPrevisualizarPlantillaEquipo.setToolTipText("Leer plantilla Excel y previsualizar validaciones sin grabar en base de datos");
        btnVincularTecnico.setToolTipText("Vincular usuario con técnico/abogado funcional");
        btnBusqueda.setToolTipText("Buscar usuarios con los filtros actuales");
        btnLimpiar1.setToolTipText("Limpiar filtros y recargar usuarios");
    }

    private void configurarTablaUsuarios() {
        tblUsuarios.setRowHeight(36);
        tblUsuarios.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblUsuarios.setShowGrid(false);
        tblUsuarios.setIntercellSpacing(new Dimension(0, 0));
        tblUsuarios.setFillsViewportHeight(true);
        tblUsuarios.setSelectionBackground(new Color(219, 235, 247));
        tblUsuarios.setSelectionForeground(new Color(25, 42, 62));

        JTableHeader header = tblUsuarios.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        header.setReorderingAllowed(false);

        renombrarEncabezado(COL_ID, "ID");
        renombrarEncabezado(COL_NOMBRE, "Usuario");
        renombrarEncabezado(COL_DESCRIPCION, "Nombre completo");
        renombrarEncabezado(COL_ESTADO, "Estado");
        renombrarEncabezado(COL_EDITAR, "Editar");
        renombrarEncabezado(COL_ACTIVAR, "Activar/Inactivar");
        renombrarEncabezado(COL_RESET, "Resetear clave");
        renombrarEncabezado(COL_ASIGNAR_ROL, "Roles");
        renombrarEncabezado(COL_ASIGNAR_ABOGADO, "Equipo supervisado");
        tblUsuarios.getTableHeader().repaint();

        ajustarColumna(COL_ID, 0, 0, 0);
        ajustarColumna(COL_NOMBRE, 120, 150, 210);
        ajustarColumna(COL_DESCRIPCION, 220, 320, 520);
        ajustarColumna(COL_ESTADO, 92, 110, 130);
        ajustarColumna(COL_EDITAR, 78, 86, 100);
        ajustarColumna(COL_ACTIVAR, 120, 138, 160);
        ajustarColumna(COL_RESET, 110, 126, 150);
        ajustarColumna(COL_ASIGNAR_ROL, 90, 104, 125);
        ajustarColumna(COL_ASIGNAR_ABOGADO, 128, 150, 180);
    }

    private void configurarRenderersUsuarios() {
        tblUsuarios.getColumnModel().getColumn(COL_ESTADO).setCellRenderer(new EstadoUsuarioRenderer());
        tblUsuarios.getColumnModel().getColumn(COL_ASIGNAR_ABOGADO)
                .setCellRenderer(new ButtonRenderer("Equipo"));
    }

    private void ajustarColumna(int indice, int min, int preferido, int max) {
        TableColumn columna = tblUsuarios.getColumnModel().getColumn(indice);
        columna.setMinWidth(min);
        columna.setPreferredWidth(preferido);
        columna.setMaxWidth(max);
        if (max == 0) {
            columna.setResizable(false);
        }
    }

    private void renombrarEncabezado(int indice, String texto) {
        tblUsuarios.getColumnModel().getColumn(indice).setHeaderValue(texto);
    }
    
    private void initFiltros() {
        cboFiltroEstado.removeAllItems(); // evita duplicados
        cboFiltroEstado.addItem("TODOS");
        cboFiltroEstado.addItem("ACTIVE");
        cboFiltroEstado.addItem("INACTIVE");
    }
    
    private void initEventos() {
        
        
        tblUsuarios.getColumn("EDITAR")
        .setCellRenderer(new ButtonRenderer("Editar"));
        tblUsuarios.getColumn("EDITAR")
                .setCellEditor(new ButtonEditorUsuario(tblUsuarios, this, 4));

        tblUsuarios.getColumn("ACTIVAR")
                .setCellRenderer(new ButtonRenderer("Activar / Inactivar"));
        tblUsuarios.getColumn("ACTIVAR")
                .setCellEditor(new ButtonEditorUsuario(tblUsuarios, this, 5));
        
        tblUsuarios.getColumn("CAMBIAR_CLAVE")
        .setCellRenderer(new ButtonRenderer("Resetear"));
        tblUsuarios.getColumn("CAMBIAR_CLAVE")
                .setCellEditor(new ButtonEditorUsuario(tblUsuarios, this, 6));
        
        tblUsuarios.getColumn("ASIGNAR_ROL")
        .setCellRenderer(new ButtonRenderer("Asignar Rol"));
        tblUsuarios.getColumn("ASIGNAR_ROL")
                .setCellEditor(new ButtonEditorUsuario(tblUsuarios, this, 7));
        /*
        tblUsuarios.getColumn("ASIGNAR_ABOGADO")
        .setCellRenderer(new ButtonRenderer("Equipo"));
        tblUsuarios.getColumn("ASIGNAR_ABOGADO")
                .setCellEditor(new ButtonEditorAsignar(tblUsuarios, this, 8));
        */
        tblUsuarios.getColumnModel()
        .getColumn(COL_ASIGNAR_ABOGADO)
        .setCellEditor(
            new ButtonEditorAsignar(
                tblUsuarios,
                this,
                userService
            )
        );
        
        txtBuscarUsuario.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    buscarUsuarios();
                }
            }
        });

        btnVincularTecnico.addActionListener(e -> vincularTecnicoDesdeSeleccion());
        btnNuevoEquipoJuridico.addActionListener(e -> abrirRegistroEquipoJuridico());
        btnDescargarPlantillaEquipo.addActionListener(e -> descargarPlantillaEquipoJuridico());
        btnPrevisualizarPlantillaEquipo.addActionListener(e -> previsualizarPlantillaEquipoJuridico());
        
        tblUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && tblUsuarios.getSelectedRow() != -1) {
                    cargarRolDesdeTabla();
                }
            }
        });
        
        
    }
    
    
    private void configurarEventosTabla() {

        tblUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                int row = tblUsuarios.rowAtPoint(e.getPoint());
                int col = tblUsuarios.columnAtPoint(e.getPoint());

                if (row < 0 || col < 0) return;

                // Columna ACTIVAR / INACTIVAR
                if (col == COL_ACTIVAR) {
                    cambiarEstadoDesdeTabla(row);
                }

                // Columna EDITAR
                if (col == COL_EDITAR) {
                    editarDesdeTabla(row);
                }
                
                // Columna RESET
                if (col == COL_RESET) {
                    resetearClaveDesdeTabla(row);
                }
                
                // Columna COL_ASIGNAR_ROL
                if (col == COL_ASIGNAR_ROL) {
                    asignarRolesDesdeTabla(row);
                }
                
                // Columna COL_ASIGNAR_ROL
                if (col == COL_ASIGNAR_ABOGADO) {
                    if (equipoSupervisadoHabilitado(row)) {
                        abrirDlgAsignarAbogados(row);
                    }
                }
                
                
                
            }
        });
    }
    
    private void cargarRolDesdeTabla() {

        int row = tblUsuarios.getSelectedRow();
        
        roleIdSeleccionado = Long.parseLong(model.getValueAt(row, 0).toString());
        roleNameSeleccionado = model.getValueAt(row, 1).toString();
        roleDescriptionSeleccionado = model.getValueAt(row, 2).toString();
        statusSeleccionado = model.getValueAt(row, 3).toString();         
    }
    
    public void cambiarEstadoDesdeTabla(int row) {
        Long id = Long.parseLong(model.getValueAt(row, 0).toString());
        String estadoActual = model.getValueAt(row, 3).toString();
        String nuevoEstado = estadoActual.equals("ACTIVE") ? "INACTIVE" : "ACTIVE";

        int r = JOptionPane.showConfirmDialog(
            this,
            "¿Cambiar estado a " + nuevoEstado + "?",
            "Confirmar",
            JOptionPane.YES_NO_OPTION
        );

        if (r == JOptionPane.YES_OPTION) {
            try {
                userService.cambiarEstado(id, nuevoEstado);
                
                // 🔥 IMPORTANTE: cerrar edición si existe
                if (tblUsuarios.isEditing()) {
                    tblUsuarios.getCellEditor().stopCellEditing();
                }
                
                model.setValueAt(nuevoEstado, row, 3);
                model.setValueAt(
                    nuevoEstado.equals("ACTIVE") ? "Inactivar" : "Activar",
                    row,
                    COL_ACTIVAR
                );

                model.fireTableRowsUpdated(row, row);
                //buscarRoles(); // refresca manteniendo filtros
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }
    
    private void buscarUsuarios() {
        try {
            model.setRowCount(0);

            String nombre = txtBuscarUsuario.getText().trim();
            String estado = cboFiltroEstado.getSelectedItem().toString();

            for (User r : userService.buscar(nombre, estado)) {
                boolean esSupervisor = userService.tieneRol(r.getUserId(), "SUPERVISION");
                model.addRow(new Object[]{
                    r.getUserId(),
                    r.getUsername(),
                    r.getFullName(),
                    r.getStatus(),
                    "Editar",
                    r.getStatus().equals("ACTIVE") ? "Inactivar" : "Activar",
                    "Resetear",
                    "Asignar Rol",
                    new ButtonCellValue("Equipo", esSupervisor),
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
    
    private void resetFiltros() {
        txtBuscarUsuario.setText("");
        cboFiltroEstado.setSelectedIndex(0);
        buscarUsuarios();
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cmbEstado = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnNuevo = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        cmbTipoBusqueda = new javax.swing.JComboBox();
        txtValorBusqueda = new javax.swing.JTextField();
        cboFiltroEstado = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        btnNuevo1 = new javax.swing.JButton();
        btnLimpiar1 = new javax.swing.JButton();
        btnBusqueda = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblUsuarios = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        txtBuscarUsuario = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();

        cmbEstado.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("FILTRO BUSQUEDA");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText("Estado del trámite");

        btnNuevo.setText("NUEVO");
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });

        btnLimpiar.setText("LIMPIAR");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        btnBuscar.setText("BUSCAR");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Nª", "Nro Tramite Documento", "Fecha Solicitud", "Tipo Solicitud", "Nombre Ciudadano / Entidad", "Estado Registro"
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jLabel5.setText("Tipo de búsqueda");

        cmbTipoBusqueda.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbTipoBusqueda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTipoBusquedaActionPerformed(evt);
            }
        });

        txtValorBusqueda.setText("jTextField1");
        txtValorBusqueda.setEnabled(false);

        cboFiltroEstado.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel2.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("MANTENIMIENTO DE USUARIOS");
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        btnNuevo1.setText("NUEVO");
        btnNuevo1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevo1ActionPerformed(evt);
            }
        });

        btnLimpiar1.setText("LIMPIAR");
        btnLimpiar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiar1ActionPerformed(evt);
            }
        });

        btnBusqueda.setText("BUSCAR");
        btnBusqueda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBusquedaActionPerformed(evt);
            }
        });

        tblUsuarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tblUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblUsuariosMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblUsuarios);

        jLabel7.setText("Buscar usuario:");

        txtBuscarUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarUsuarioActionPerformed(evt);
            }
        });

        jLabel8.setText("Estado:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnNuevo1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(txtBuscarUsuario)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(cboFiltroEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 632, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnLimpiar1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 725, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(336, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(63, 63, 63)
                .addComponent(btnNuevo1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpiar1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBuscarUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboFiltroEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        
        // TODO add your handling code here:
        //MenuPrincipal.ShowJPanel(new JPanelRegistrarExpediente());
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
        //limpiarCampos();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        //buscarExpedientes();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        
    }//GEN-LAST:event_jTable1MouseClicked

    private void cmbTipoBusquedaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTipoBusquedaActionPerformed
        if (cmbTipoBusqueda.getSelectedItem() != null) {
            txtValorBusqueda.setEnabled(true);
            txtValorBusqueda.setText("");
            txtValorBusqueda.requestFocus();
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbTipoBusquedaActionPerformed

    private void btnNuevo1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevo1ActionPerformed
        
        
        Window parent = SwingUtilities.getWindowAncestor(this);
        DlgEditarUsuario dlg = new DlgEditarUsuario(parent, Dialog.ModalityType.APPLICATION_MODAL, usuario, userService,false);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        buscarUsuarios();
        
        
        
    }//GEN-LAST:event_btnNuevo1ActionPerformed

    private void btnLimpiar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiar1ActionPerformed
        // TODO add your handling code here:
        //limpiarCampos();
        resetFiltros();
    }//GEN-LAST:event_btnLimpiar1ActionPerformed

    private void btnBusquedaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBusquedaActionPerformed
        buscarUsuarios();        
    }//GEN-LAST:event_btnBusquedaActionPerformed

    private void tblUsuariosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblUsuariosMouseClicked
        
    }//GEN-LAST:event_tblUsuariosMouseClicked

    private void txtBuscarUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarUsuarioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBuscarUsuarioActionPerformed
    
    
    private void initTable() {
        model = new DefaultTableModel(
            new Object[]{"ID", "USUARIO", "NOMBRE", "ESTADO", "EDITAR", "ACTIVAR","CAMBIAR_CLAVE","ASIGNAR_ROL","ASIGNAR_ABOGADO"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo las columnas de botones
                return column == 4 || column == 5 || column == 6 || column == 7 || column == 8;
            }
        };
        tblUsuarios.setModel(model);
    }
    
    public void editarDesdeTabla(int row) { 
        DefaultTableModel model = (DefaultTableModel) tblUsuarios.getModel();

        User usuario = new User();
        usuario.setUserId(Long.parseLong(model.getValueAt(row, COL_ID).toString()));
        usuario.setUsername(model.getValueAt(row, COL_NOMBRE).toString());
        usuario.setFullName(model.getValueAt(row, COL_DESCRIPCION).toString());
        usuario.setStatus(model.getValueAt(row, COL_ESTADO).toString());

        Window parent = SwingUtilities.getWindowAncestor(this);
        DlgEditarUsuario dlg = new DlgEditarUsuario(parent, Dialog.ModalityType.APPLICATION_MODAL, usuario, userService,true);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        buscarUsuarios();
        
    }
    
    public void resetearClaveDesdeTabla(int row) { 
        DefaultTableModel model = (DefaultTableModel) tblUsuarios.getModel();
        Long userId = (Long) model.getValueAt(row, COL_ID);

        Window parent = SwingUtilities.getWindowAncestor(this);

        DlgResetPasswordUsuario dlg = new DlgResetPasswordUsuario(
                parent,
                Dialog.ModalityType.APPLICATION_MODAL,
                userId,
                userService
        );

        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
        
        buscarUsuarios();
    }
    
    public void asignarRolesDesdeTabla(int row) { 
        DefaultTableModel model = (DefaultTableModel) tblUsuarios.getModel();
        Long userId = (Long) model.getValueAt(row, COL_ID);

        String username = model.getValueAt(row, 1).toString();
        
        Window parent = SwingUtilities.getWindowAncestor(this);

        DlgAsignarRolesUsuario  dlg = new DlgAsignarRolesUsuario (
                parent,
                userId,
                username,
                userService,
                roleService
        );

        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
        
        buscarUsuarios();
    }

    private boolean equipoSupervisadoHabilitado(int row) {
        int modelRow = tblUsuarios.convertRowIndexToModel(row);
        Object value = model.getValueAt(modelRow, COL_ASIGNAR_ABOGADO);
        return !(value instanceof ButtonCellValue) || ((ButtonCellValue) value).isEnabled();
    }

    private void abrirRegistroEquipoJuridico() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        DlgRegistrarEquipoJuridico dlg = new DlgRegistrarEquipoJuridico(parent, equipoJuridicoService);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        buscarUsuarios();
    }

    private void descargarPlantillaEquipoJuridico() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar plantilla Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));
        chooser.setSelectedFile(new File("plantilla_equipo_juridico_sdrerc.xlsx"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destino = asegurarExtensionXlsx(chooser.getSelectedFile());
        if (destino.exists()) {
            int confirmar = JOptionPane.showConfirmDialog(
                    this,
                    "El archivo ya existe. ¿Desea reemplazarlo?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirmar != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            generarPlantillaEquipoJuridico(destino);
            if (!destino.exists() || destino.length() == 0) {
                throw new IllegalStateException("El archivo no se generó en la ruta seleccionada.");
            }
            JOptionPane.showMessageDialog(
                    this,
                    "Plantilla Excel generada correctamente:\n" + destino.getAbsolutePath(),
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Throwable ex) {
            Throwable causa = obtenerCausaReal(ex);
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo generar la plantilla Excel: " + causa.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private File asegurarExtensionXlsx(File file) {
        String path = file.getAbsolutePath();
        if (path.toLowerCase().endsWith(".xlsx")) {
            return file;
        }
        return new File(path + ".xlsx");
    }

    private void generarPlantillaEquipoJuridico(File destino) throws Exception {
        try {
            Class<?> serviceClass = Class.forName("com.sdrerc.application.EquipoJuridicoPlantillaService");
            Object service = serviceClass.getDeclaredConstructor().newInstance();
            serviceClass.getMethod("generarPlantilla", File.class).invoke(service, destino);
        } catch (Throwable ex) {
            Class<?> fallbackClass = Class.forName("com.sdrerc.application.EquipoJuridicoPlantillaSimpleService");
            Object fallback = fallbackClass.getDeclaredConstructor().newInstance();
            fallbackClass.getMethod("generarPlantilla", File.class).invoke(fallback, destino);
        }
    }

    private void previsualizarPlantillaEquipoJuridico() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar plantilla Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            EquipoJuridicoImportPreview preview = previsualizarEquipoJuridico(chooser.getSelectedFile());
            Window parent = SwingUtilities.getWindowAncestor(this);
            DlgPrevisualizarEquipoJuridicoExcel dlg = new DlgPrevisualizarEquipoJuridicoExcel(parent, preview);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        } catch (Throwable ex) {
            Throwable causa = obtenerCausaReal(ex);
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo previsualizar la plantilla Excel: " + causa.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private EquipoJuridicoImportPreview previsualizarEquipoJuridico(File archivo) throws Exception {
        try {
            Class<?> serviceClass = Class.forName("com.sdrerc.application.EquipoJuridicoImportService");
            Object service = serviceClass.getDeclaredConstructor().newInstance();
            Object preview = serviceClass.getMethod("previsualizar", File.class).invoke(service, archivo);
            return (EquipoJuridicoImportPreview) preview;
        } catch (Throwable ex) {
            Class<?> fallbackClass = Class.forName("com.sdrerc.application.EquipoJuridicoImportSimpleService");
            Object fallback = fallbackClass.getDeclaredConstructor().newInstance();
            Object preview = fallbackClass.getMethod("previsualizar", File.class).invoke(fallback, archivo);
            return (EquipoJuridicoImportPreview) preview;
        }
    }

    private Throwable obtenerCausaReal(Throwable ex) {
        if (ex instanceof InvocationTargetException && ((InvocationTargetException) ex).getTargetException() != null) {
            return ((InvocationTargetException) ex).getTargetException();
        }
        return ex.getCause() != null ? ex.getCause() : ex;
    }

    private void vincularTecnicoDesdeSeleccion() {
        int row = tblUsuarios.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (tblUsuarios.isEditing()) {
            tblUsuarios.getCellEditor().stopCellEditing();
        }

        int modelRow = tblUsuarios.convertRowIndexToModel(row);
        Long userId = (Long) model.getValueAt(modelRow, COL_ID);

        Window parent = SwingUtilities.getWindowAncestor(this);
        Frame frame = parent instanceof Frame ? (Frame) parent : null;
        JDialogTecnico dialog = new JDialogTecnico(frame, true);
        dialog.setTitle("Vincular técnico/abogado funcional");
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        String idTecnicoSeleccionado = dialog.getIdTecnicoSeleccionado();
        if (idTecnicoSeleccionado == null || idTecnicoSeleccionado.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un técnico válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Integer idTecnico = Integer.valueOf(idTecnicoSeleccionado);
            userService.vincularTecnico(userId, idTecnico);
            JOptionPane.showMessageDialog(this, "Técnico vinculado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            buscarUsuarios();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Seleccione un técnico válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void abrirDlgAsignarAbogados(int row) {

        Long supervisorId =
            (Long) tblUsuarios.getValueAt(row, COL_ID);

        String nombreSupervisor =
            tblUsuarios.getValueAt(row, COL_NOMBRE).toString();

        try {
            if (!userService.tieneRol(supervisorId, "SUPERVISION")) {
                JOptionPane.showMessageDialog(
                        this,
                        "Esta opción solo aplica a usuarios con rol SUPERVISION.",
                        "Acceso denegado",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DlgAsignarAbogados dlg =
            new DlgAsignarAbogados(
                SwingUtilities.getWindowAncestor(this),
                supervisorId,
                nombreSupervisor,
                userService,
                supervisionService
            );

        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        buscarUsuarios();
    }

    private static class EstadoUsuarioRenderer extends DefaultTableCellRenderer {

        @Override
        public java.awt.Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            String estado = value == null ? "" : value.toString();
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));

            if ("ACTIVE".equalsIgnoreCase(estado)) {
                label.setText("ACTIVO");
                if (!isSelected) {
                    label.setForeground(new Color(24, 112, 70));
                    label.setBackground(new Color(225, 244, 235));
                }
            } else if ("INACTIVE".equalsIgnoreCase(estado)) {
                label.setText("INACTIVO");
                if (!isSelected) {
                    label.setForeground(new Color(143, 48, 48));
                    label.setBackground(new Color(250, 230, 230));
                }
            } else {
                label.setText(estado);
                if (!isSelected) {
                    label.setForeground(new Color(73, 85, 99));
                    label.setBackground(Color.WHITE);
                }
            }
            label.setOpaque(true);
            return label;
        }
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnBusqueda;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnLimpiar1;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnNuevo1;
    private javax.swing.JComboBox cboFiltroEstado;
    private javax.swing.JComboBox cmbEstado;
    private javax.swing.JComboBox cmbTipoBusqueda;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable tblUsuarios;
    private javax.swing.JTextField txtBuscarUsuario;
    private javax.swing.JTextField txtValorBusqueda;
    // End of variables declaration//GEN-END:variables
}
