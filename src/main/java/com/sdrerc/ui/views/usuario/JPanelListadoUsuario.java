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
import com.sdrerc.domain.model.EquipoJuridicoImportResult;
import com.sdrerc.domain.model.EquipoJuridicoImportPreview;
import com.sdrerc.domain.model.PaginatedResult;
import com.sdrerc.domain.model.User;
import com.sdrerc.domain.model.UsuarioListadoItem;
import com.sdrerc.ui.table.ButtonEditor;
import com.sdrerc.ui.table.ButtonCellValue;
import com.sdrerc.ui.table.ButtonEditorAsignar;
import com.sdrerc.ui.table.ButtonEditorUsuario;
import com.sdrerc.ui.table.ButtonRenderer;
import com.sdrerc.ui.common.icon.IconUtils;
import com.sdrerc.ui.views.asignacion.JDialogTecnico;
import com.sdrerc.ui.views.equipojuridico.EquipoJuridicoImportOwner;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author David
 */
public class JPanelListadoUsuario extends javax.swing.JPanel implements EquipoJuridicoImportOwner, Scrollable {

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
    private final JButton btnVincularTecnico = new JButton("Persona operativa");
    private final JButton btnPrimeraPagina = new JButton("Primera");
    private final JButton btnPaginaAnterior = new JButton("Anterior");
    private final JButton btnPaginaSiguiente = new JButton("Siguiente");
    private final JButton btnUltimaPagina = new JButton("Última");
    private final JLabel lblPaginaUsuarios = new JLabel("Página 1 de 1");
    private final JLabel lblResumenUsuarios = new JLabel("Mostrando 0-0 de 0 usuarios");
    private final JComboBox<Integer> cboFilasPorPagina = new JComboBox<>(new Integer[]{10, 25, 50, 100});
    private int currentPage = 1;
    private int pageSize = 25;
    private int totalRecords = 0;
    private int totalPages = 1;
    
    public static final int COL_ID = 0;
    private static final int COL_NOMBRE = 1;
    private static final int COL_DESCRIPCION = 2;
    private static final int COL_PERFIL = 3;
    private static final int COL_VINCULO_OPERATIVO = 4;
    private static final int COL_ESTADO = 5;
    private static final int COL_EDITAR = 6;
    private static final int COL_ACTIVAR = 7;
    private static final int COL_RESET = 8;
    private static final int COL_ASIGNAR_ROL = 9;
    private static final int COL_ASIGNAR_ABOGADO = 10;
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
        configurarPaginacionUsuarios();
        //cargarRoles();
        cargarPaginaUsuarios();
        
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

    private void corregirTextosVisibles() {
        jLabel2.setText("Mantenimiento de Usuarios");
        jLabel7.setText("Buscar usuario");
        jLabel8.setText("Estado");
        btnNuevo1.setText("Nuevo usuario");
        btnBusqueda.setText("Buscar");
        btnLimpiar1.setText("Limpiar");
        btnVincularTecnico.setText("Persona operativa");
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
        jScrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(jScrollPane2, BorderLayout.CENTER);
        centerPanel.add(crearPanelPaginacionUsuarios(), BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel crearPanelPaginacionUsuarios() {
        JPanel paginationPanel = new JPanel(new BorderLayout(12, 0));
        paginationPanel.setBackground(Color.WHITE);
        paginationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        lblResumenUsuarios.setForeground(new Color(73, 85, 99));
        paginationPanel.add(lblResumenUsuarios, BorderLayout.WEST);

        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 8);
        gbc.gridy = 0;

        controlsPanel.add(btnPrimeraPagina, gbc);
        controlsPanel.add(btnPaginaAnterior, gbc);
        controlsPanel.add(lblPaginaUsuarios, gbc);
        controlsPanel.add(btnPaginaSiguiente, gbc);
        controlsPanel.add(btnUltimaPagina, gbc);
        controlsPanel.add(new JLabel("Filas por página"), gbc);
        gbc.insets = new Insets(0, 0, 0, 0);
        controlsPanel.add(cboFilasPorPagina, gbc);

        paginationPanel.add(controlsPanel, BorderLayout.EAST);
        return paginationPanel;
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
        Dimension botonVincular = new Dimension(168, 36);
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

        aplicarIconoBoton(btnNuevo1, "add.svg");
        aplicarIconoBoton(btnVincularTecnico, "users.svg");
        aplicarIconoBoton(btnBusqueda, "search.svg");
        aplicarIconoBoton(btnLimpiar1, "clear.svg");

        btnNuevo1.setToolTipText("Registrar un nuevo usuario");
        btnNuevoEquipoJuridico.setToolTipText("Registrar abogado o supervisor creando técnico, usuario y roles");
        btnDescargarPlantillaEquipo.setToolTipText("Descargar plantilla oficial para carga masiva de equipo jurídico");
        btnPrevisualizarPlantillaEquipo.setToolTipText("Leer plantilla Excel y previsualizar validaciones sin grabar en base de datos");
        btnVincularTecnico.setToolTipText("Acción avanzada para vincular la cuenta con una persona operativa.");
        btnBusqueda.setToolTipText("Buscar usuarios con los filtros actuales");
        btnLimpiar1.setToolTipText("Limpiar filtros y recargar usuarios");
    }

    private void aplicarIconoBoton(JButton button, String iconName) {
        Icon icon = IconUtils.load(iconName, 16);
        if (icon != null) {
            button.setIcon(icon);
            button.setIconTextGap(8);
        }
    }

    private void configurarPaginacionUsuarios() {
        Dimension botonPaginacion = new Dimension(86, 30);
        btnPrimeraPagina.setPreferredSize(botonPaginacion);
        btnPaginaAnterior.setPreferredSize(botonPaginacion);
        btnPaginaSiguiente.setPreferredSize(botonPaginacion);
        btnUltimaPagina.setPreferredSize(botonPaginacion);
        cboFilasPorPagina.setPreferredSize(new Dimension(76, 30));
        cboFilasPorPagina.setSelectedItem(pageSize);

        btnPrimeraPagina.setToolTipText("Ir a la primera página");
        btnPaginaAnterior.setToolTipText("Ir a la página anterior");
        btnPaginaSiguiente.setToolTipText("Ir a la página siguiente");
        btnUltimaPagina.setToolTipText("Ir a la última página");
        cboFilasPorPagina.setToolTipText("Cantidad de usuarios por página");

        btnPrimeraPagina.addActionListener(e -> irPrimeraPagina());
        btnPaginaAnterior.addActionListener(e -> irPaginaAnterior());
        btnPaginaSiguiente.addActionListener(e -> irPaginaSiguiente());
        btnUltimaPagina.addActionListener(e -> irUltimaPagina());
        cboFilasPorPagina.addActionListener(e -> {
            Object selected = cboFilasPorPagina.getSelectedItem();
            if (selected instanceof Integer) {
                pageSize = (Integer) selected;
                currentPage = 1;
                cargarPaginaUsuarios();
            }
        });

        actualizarControlesPaginacion();
    }

    private void configurarTablaUsuarios() {
        tblUsuarios.setRowHeight(36);
        tblUsuarios.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
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
        renombrarEncabezado(COL_DESCRIPCION, "Nombre visible");
        renombrarEncabezado(COL_PERFIL, "Perfil");
        renombrarEncabezado(COL_VINCULO_OPERATIVO, "Vínculo operativo");
        renombrarEncabezado(COL_ESTADO, "Estado");
        renombrarEncabezado(COL_EDITAR, "");
        renombrarEncabezado(COL_ACTIVAR, "");
        renombrarEncabezado(COL_RESET, "");
        renombrarEncabezado(COL_ASIGNAR_ROL, "Roles");
        renombrarEncabezado(COL_ASIGNAR_ABOGADO, "");
        tblUsuarios.getTableHeader().repaint();

        ajustarColumna(COL_ID, 0, 0, 0);
        ajustarColumna(COL_NOMBRE, 100, 130, 170);
        ajustarColumna(COL_DESCRIPCION, 220, 320, Integer.MAX_VALUE);
        ajustarColumna(COL_PERFIL, 140, 180, 240);
        ajustarColumna(COL_VINCULO_OPERATIVO, 110, 130, 150);
        ajustarColumna(COL_ESTADO, 90, 100, 120);
        ajustarColumna(COL_EDITAR, 45, 55, 65);
        ajustarColumna(COL_ACTIVAR, 45, 55, 65);
        ajustarColumna(COL_RESET, 45, 55, 65);
        ajustarColumna(COL_ASIGNAR_ROL, 45, 55, 65);
        ajustarColumna(COL_ASIGNAR_ABOGADO, 0, 0, 0);
    }

    private void configurarRenderersUsuarios() {
        tblUsuarios.getColumnModel().getColumn(COL_NOMBRE).setCellRenderer(new TextoTooltipRenderer());
        tblUsuarios.getColumnModel().getColumn(COL_DESCRIPCION).setCellRenderer(new TextoTooltipRenderer());
        tblUsuarios.getColumnModel().getColumn(COL_PERFIL).setCellRenderer(new TextoTooltipRenderer());
        tblUsuarios.getColumnModel().getColumn(COL_VINCULO_OPERATIVO).setCellRenderer(new VinculoOperativoRenderer());
        tblUsuarios.getColumnModel().getColumn(COL_ESTADO).setCellRenderer(new EstadoUsuarioRenderer());
        tblUsuarios.getColumnModel().getColumn(COL_EDITAR).setCellRenderer(new UsuarioActionRenderer(COL_EDITAR));
        tblUsuarios.getColumnModel().getColumn(COL_EDITAR).setCellEditor(new UsuarioActionEditor(COL_EDITAR));
        tblUsuarios.getColumnModel().getColumn(COL_ACTIVAR).setCellRenderer(new UsuarioActionRenderer(COL_ACTIVAR));
        tblUsuarios.getColumnModel().getColumn(COL_ACTIVAR).setCellEditor(new UsuarioActionEditor(COL_ACTIVAR));
        tblUsuarios.getColumnModel().getColumn(COL_RESET).setCellRenderer(new UsuarioActionRenderer(COL_RESET));
        tblUsuarios.getColumnModel().getColumn(COL_RESET).setCellEditor(new UsuarioActionEditor(COL_RESET));
        tblUsuarios.getColumnModel().getColumn(COL_ASIGNAR_ROL).setCellRenderer(new UsuarioActionRenderer(COL_ASIGNAR_ROL));
        tblUsuarios.getColumnModel().getColumn(COL_ASIGNAR_ROL).setCellEditor(new UsuarioActionEditor(COL_ASIGNAR_ROL));
        tblUsuarios.getColumnModel().getColumn(COL_ASIGNAR_ABOGADO).setCellRenderer(new UsuarioActionRenderer(COL_ASIGNAR_ABOGADO));
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
                .setCellEditor(new ButtonEditorUsuario(tblUsuarios, this, COL_EDITAR));

        tblUsuarios.getColumn("ACTIVAR")
                .setCellRenderer(new ButtonRenderer("Activar / Inactivar"));
        tblUsuarios.getColumn("ACTIVAR")
                .setCellEditor(new ButtonEditorUsuario(tblUsuarios, this, COL_ACTIVAR));
        
        tblUsuarios.getColumn("CAMBIAR_CLAVE")
        .setCellRenderer(new ButtonRenderer("Resetear"));
        tblUsuarios.getColumn("CAMBIAR_CLAVE")
                .setCellEditor(new ButtonEditorUsuario(tblUsuarios, this, COL_RESET));
        
        tblUsuarios.getColumn("ASIGNAR_ROL")
        .setCellRenderer(new ButtonRenderer("Asignar Rol"));
        tblUsuarios.getColumn("ASIGNAR_ROL")
                .setCellEditor(new ButtonEditorUsuario(tblUsuarios, this, COL_ASIGNAR_ROL));
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
                    resetearPaginacionAlBuscar();
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
                int row = tblUsuarios.rowAtPoint(evt.getPoint());
                int col = tblUsuarios.columnAtPoint(evt.getPoint());
                if (evt.getClickCount() == 2 && row >= 0 && !esColumnaAccion(col)) {
                    editarDesdeTabla(row);
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
        tblUsuarios.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = tblUsuarios.columnAtPoint(e.getPoint());
                tblUsuarios.setCursor(esColumnaAccion(col)
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                tblUsuarios.repaint();
            }
        });
        tblUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                tblUsuarios.setCursor(Cursor.getDefaultCursor());
                tblUsuarios.repaint();
            }
        });
    }

    private boolean esColumnaAccion(int col) {
        return col == COL_EDITAR
                || col == COL_ACTIVAR
                || col == COL_RESET
                || col == COL_ASIGNAR_ROL
                || col == COL_ASIGNAR_ABOGADO;
    }
    
    private void cargarRolDesdeTabla() {

        int row = tblUsuarios.getSelectedRow();
        
        roleIdSeleccionado = Long.parseLong(model.getValueAt(row, 0).toString());
        roleNameSeleccionado = model.getValueAt(row, 1).toString();
        roleDescriptionSeleccionado = model.getValueAt(row, 2).toString();
        statusSeleccionado = model.getValueAt(row, COL_ESTADO).toString();
    }
    
    public void cambiarEstadoDesdeTabla(int row) {
        int modelRow = tblUsuarios.convertRowIndexToModel(row);
        Long id = Long.parseLong(model.getValueAt(modelRow, 0).toString());
        String estadoActual = model.getValueAt(modelRow, COL_ESTADO).toString();
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
                
                model.setValueAt(nuevoEstado, modelRow, COL_ESTADO);
                model.setValueAt(
                    nuevoEstado.equals("ACTIVE") ? "Inactivar" : "Activar",
                    modelRow,
                    COL_ACTIVAR
                );

                cargarPaginaUsuarios();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }
    
    private void buscarUsuarios() {
        resetearPaginacionAlBuscar();
    }

    private void cargarPaginaUsuarios() {
        try {
            model.setRowCount(0);

            String nombre = txtBuscarUsuario.getText().trim();
            String estado = cboFiltroEstado.getSelectedItem().toString();

            PaginatedResult<UsuarioListadoItem> result =
                    userService.buscarPaginado(nombre, estado, currentPage, pageSize);

            currentPage = result.getPage();
            pageSize = result.getPageSize();
            totalRecords = result.getTotalRecords();
            totalPages = result.getTotalPages();

            for (UsuarioListadoItem r : result.getData()) {
                model.addRow(new Object[]{
                    r.getUserId(),
                    r.getUsername(),
                    r.getNombreVisible(),
                    r.getRolesPerfil(),
                    crearValorVinculoOperativo(r),
                    r.getStatus(),
                    "Editar",
                    r.getStatus().equals("ACTIVE") ? "Inactivar" : "Activar",
                    "Resetear",
                    "Asignar Rol",
                    new ButtonCellValue("Equipo", r.isEsSupervision()),
                });
            }

            if (model.getRowCount() == 0 && totalRecords > 0 && currentPage > 1) {
                currentPage--;
                cargarPaginaUsuarios();
                return;
            }

            actualizarControlesPaginacion();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private VinculoOperativoValue crearValorVinculoOperativo(UsuarioListadoItem usuario) {
        if (usuario.getIdTecnico() != null) {
            String nombre = usuario.getNombreVisible() == null ? "" : usuario.getNombreVisible();
            return new VinculoOperativoValue("Vinculado", "Vinculado a: " + nombre, VinculoOperativoValue.Tipo.VINCULADO);
        }
        if (usuario.isEsOperativoJuridico()) {
            return new VinculoOperativoValue(
                    "Sin vínculo",
                    "Usuario operativo sin persona operativa vinculada",
                    VinculoOperativoValue.Tipo.SIN_VINCULO
            );
        }
        return new VinculoOperativoValue(
                "No aplica",
                "No aplica para usuario administrativo/no operativo",
                VinculoOperativoValue.Tipo.NO_APLICA
        );
    }
    
    private void resetFiltros() {
        txtBuscarUsuario.setText("");
        cboFiltroEstado.setSelectedIndex(0);
        resetearPaginacionAlBuscar();
    }

    private void resetearPaginacionAlBuscar() {
        currentPage = 1;
        cargarPaginaUsuarios();
    }

    private void irPrimeraPagina() {
        if (currentPage != 1) {
            currentPage = 1;
            cargarPaginaUsuarios();
        }
    }

    private void irPaginaAnterior() {
        if (currentPage > 1) {
            currentPage--;
            cargarPaginaUsuarios();
        }
    }

    private void irPaginaSiguiente() {
        if (currentPage < totalPages) {
            currentPage++;
            cargarPaginaUsuarios();
        }
    }

    private void irUltimaPagina() {
        if (currentPage != totalPages) {
            currentPage = totalPages;
            cargarPaginaUsuarios();
        }
    }

    private void actualizarControlesPaginacion() {
        int from = totalRecords == 0 ? 0 : ((currentPage - 1) * pageSize) + 1;
        int to = totalRecords == 0 ? 0 : Math.min(currentPage * pageSize, totalRecords);

        lblPaginaUsuarios.setText("Página " + currentPage + " de " + totalPages);
        lblResumenUsuarios.setText("Mostrando " + from + "-" + to + " de " + totalRecords + " usuarios");

        boolean hasPrevious = currentPage > 1;
        boolean hasNext = currentPage < totalPages;
        btnPrimeraPagina.setEnabled(hasPrevious);
        btnPaginaAnterior.setEnabled(hasPrevious);
        btnPaginaSiguiente.setEnabled(hasNext);
        btnUltimaPagina.setEnabled(hasNext);
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

        resetearPaginacionAlBuscar();
        
        
        
    }//GEN-LAST:event_btnNuevo1ActionPerformed

    private void btnLimpiar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiar1ActionPerformed
        // TODO add your handling code here:
        //limpiarCampos();
        resetFiltros();
    }//GEN-LAST:event_btnLimpiar1ActionPerformed

    private void btnBusquedaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBusquedaActionPerformed
        resetearPaginacionAlBuscar();
    }//GEN-LAST:event_btnBusquedaActionPerformed

    private void tblUsuariosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblUsuariosMouseClicked
        
    }//GEN-LAST:event_tblUsuariosMouseClicked

    private void txtBuscarUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarUsuarioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBuscarUsuarioActionPerformed
    
    
    private void initTable() {
        model = new DefaultTableModel(
            new Object[]{"ID", "USUARIO", "NOMBRE", "PERFIL", "VINCULO_OPERATIVO", "ESTADO", "EDITAR", "ACTIVAR","CAMBIAR_CLAVE","ASIGNAR_ROL","ASIGNAR_ABOGADO"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo las columnas de botones
                return column == COL_EDITAR || column == COL_ACTIVAR || column == COL_RESET
                        || column == COL_ASIGNAR_ROL || column == COL_ASIGNAR_ABOGADO;
            }
        };
        tblUsuarios.setModel(model);
    }
    
    public void editarDesdeTabla(int row) { 
        DefaultTableModel model = (DefaultTableModel) tblUsuarios.getModel();
        int modelRow = tblUsuarios.convertRowIndexToModel(row);

        User usuario = new User();
        usuario.setUserId(Long.parseLong(model.getValueAt(modelRow, COL_ID).toString()));
        usuario.setUsername(model.getValueAt(modelRow, COL_NOMBRE).toString());
        usuario.setFullName(model.getValueAt(modelRow, COL_DESCRIPCION).toString());
        usuario.setStatus(model.getValueAt(modelRow, COL_ESTADO).toString());

        Window parent = SwingUtilities.getWindowAncestor(this);
        DlgEditarUsuario dlg = new DlgEditarUsuario(parent, Dialog.ModalityType.APPLICATION_MODAL, usuario, userService,true);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        cargarPaginaUsuarios();
        
    }
    
    public void resetearClaveDesdeTabla(int row) { 
        DefaultTableModel model = (DefaultTableModel) tblUsuarios.getModel();
        int modelRow = tblUsuarios.convertRowIndexToModel(row);
        Long userId = (Long) model.getValueAt(modelRow, COL_ID);

        Window parent = SwingUtilities.getWindowAncestor(this);

        DlgResetPasswordUsuario dlg = new DlgResetPasswordUsuario(
                parent,
                Dialog.ModalityType.APPLICATION_MODAL,
                userId,
                userService
        );

        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
        
        cargarPaginaUsuarios();
    }
    
    public void asignarRolesDesdeTabla(int row) { 
        DefaultTableModel model = (DefaultTableModel) tblUsuarios.getModel();
        int modelRow = tblUsuarios.convertRowIndexToModel(row);
        Long userId = (Long) model.getValueAt(modelRow, COL_ID);

        String username = model.getValueAt(modelRow, 1).toString();
        
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
        
        cargarPaginaUsuarios();
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
        cargarPaginaUsuarios();
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
            DlgPrevisualizarEquipoJuridicoExcel dlg = new DlgPrevisualizarEquipoJuridicoExcel(parent, this, preview);
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
            if (!esErrorDependenciaPoi(ex)) {
                throw convertirException(ex);
            }
            Class<?> fallbackClass = Class.forName("com.sdrerc.application.EquipoJuridicoImportSimpleService");
            Object fallback = fallbackClass.getDeclaredConstructor().newInstance();
            Object preview = fallbackClass.getMethod("previsualizar", File.class).invoke(fallback, archivo);
            return (EquipoJuridicoImportPreview) preview;
        }
    }

    public EquipoJuridicoImportResult confirmarImportacionEquipoJuridico(
            EquipoJuridicoImportPreview preview,
            boolean incluirAdvertencias) throws Exception {
        try {
            Class<?> serviceClass = Class.forName("com.sdrerc.application.EquipoJuridicoImportService");
            Object service = serviceClass.getDeclaredConstructor().newInstance();
            Object result = serviceClass
                    .getMethod("confirmarImportacion", EquipoJuridicoImportPreview.class, boolean.class)
                    .invoke(service, preview, incluirAdvertencias);
            return (EquipoJuridicoImportResult) result;
        } catch (Throwable ex) {
            if (!esErrorDependenciaPoi(ex)) {
                throw convertirException(ex);
            }
            Class<?> fallbackClass = Class.forName("com.sdrerc.application.EquipoJuridicoImportSimpleService");
            Object fallback = fallbackClass.getDeclaredConstructor().newInstance();
            Object result = fallbackClass
                    .getMethod("confirmarImportacion", EquipoJuridicoImportPreview.class, boolean.class)
                    .invoke(fallback, preview, incluirAdvertencias);
            return (EquipoJuridicoImportResult) result;
        }
    }

    private boolean esErrorDependenciaPoi(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage() == null ? "" : current.getMessage();
            if (message.contains("org.apache.poi") || current instanceof NoClassDefFoundError) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private Exception convertirException(Throwable ex) {
        Throwable causa = obtenerCausaReal(ex);
        if (causa instanceof Exception) {
            return (Exception) causa;
        }
        return new Exception(causa.getMessage(), causa);
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
        dialog.setTitle("Vincular persona operativa");
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
            JOptionPane.showMessageDialog(this, "Persona operativa vinculada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarPaginaUsuarios();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Seleccione un técnico válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void abrirDlgAsignarAbogados(int row) {

        int modelRow = tblUsuarios.convertRowIndexToModel(row);
        Long supervisorId =
            (Long) model.getValueAt(modelRow, COL_ID);

        String nombreSupervisor =
            model.getValueAt(modelRow, COL_NOMBRE).toString();

        if (!equipoSupervisadoHabilitado(row)) {
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
        cargarPaginaUsuarios();
    }

    private class TextoTooltipRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = value == null ? "" : value.toString();
            label.setToolTipText(text);
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return label;
        }
    }

    private static class VinculoOperativoValue {
        enum Tipo {
            VINCULADO,
            SIN_VINCULO,
            NO_APLICA
        }

        private final String texto;
        private final String tooltip;
        private final Tipo tipo;

        VinculoOperativoValue(String texto, String tooltip, Tipo tipo) {
            this.texto = texto;
            this.tooltip = tooltip;
            this.tipo = tipo;
        }

        String getTooltip() {
            return tooltip;
        }

        Tipo getTipo() {
            return tipo;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private static class VinculoOperativoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            VinculoOperativoValue vinculo = value instanceof VinculoOperativoValue
                    ? (VinculoOperativoValue) value
                    : new VinculoOperativoValue(value == null ? "" : value.toString(), null, VinculoOperativoValue.Tipo.NO_APLICA);

            label.setText(vinculo.toString());
            label.setToolTipText(vinculo.getTooltip());
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
            label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            if (!isSelected) {
                label.setOpaque(true);
                if (vinculo.getTipo() == VinculoOperativoValue.Tipo.VINCULADO) {
                    label.setForeground(new Color(24, 112, 70));
                    label.setBackground(new Color(225, 244, 235));
                } else if (vinculo.getTipo() == VinculoOperativoValue.Tipo.SIN_VINCULO) {
                    label.setForeground(new Color(153, 91, 24));
                    label.setBackground(new Color(255, 243, 217));
                } else {
                    label.setForeground(new Color(107, 114, 128));
                    label.setBackground(Color.WHITE);
                }
            }
            return label;
        }
    }

    private class UsuarioActionRenderer implements TableCellRenderer {

        private final int actionColumn;
        private final GhostActionButton button = crearBotonAccion();

        UsuarioActionRenderer(int actionColumn) {
            this.actionColumn = actionColumn;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            int modelRow = table.convertRowIndexToModel(row);
            String estado = valorModelo(modelRow, COL_ESTADO);
            boolean enabled = !(value instanceof ButtonCellValue) || ((ButtonCellValue) value).isEnabled();

            configurarBotonAccion(button, actionColumn, estado, enabled);
            button.setSelectedRow(isSelected);

            Point mouse = table.getMousePosition();
            boolean hover = enabled
                    && mouse != null
                    && table.rowAtPoint(mouse) == row
                    && table.columnAtPoint(mouse) == column;
            button.setHover(hover);
            return button;
        }
    }

    private class UsuarioActionEditor extends AbstractCellEditor implements TableCellEditor {

        private final int actionColumn;
        private final GhostActionButton button = crearBotonAccion();
        private Object editorValue;

        UsuarioActionEditor(int actionColumn) {
            this.actionColumn = actionColumn;
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column) {

            editorValue = value;
            int modelRow = table.convertRowIndexToModel(row);
            String estado = valorModelo(modelRow, COL_ESTADO);
            boolean enabled = !(value instanceof ButtonCellValue) || ((ButtonCellValue) value).isEnabled();
            configurarBotonAccion(button, actionColumn, estado, enabled);
            button.setSelectedRow(isSelected);
            button.setHover(true);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return editorValue;
        }
    }

    private GhostActionButton crearBotonAccion() {
        GhostActionButton button = new GhostActionButton();
        button.setText("");
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        button.setPreferredSize(new Dimension(32, 28));
        button.setMinimumSize(new Dimension(32, 28));
        button.setMaximumSize(new Dimension(32, 28));
        return button;
    }

    private void configurarBotonAccion(GhostActionButton button, int actionColumn, String estado, boolean enabled) {
        String iconName = iconoAccion(actionColumn, estado);
        Icon icon = IconUtils.load(iconName, 16);
        button.setIcon(icon);
        button.setToolTipText(tooltipAccion(actionColumn, estado));
        button.setEnabled(enabled);
    }

    private String iconoAccion(int actionColumn, String estado) {
        if (actionColumn == COL_EDITAR) {
            return "edit.svg";
        }
        if (actionColumn == COL_ACTIVAR) {
            return esActivo(estado) ? "inactive.svg" : "active.svg";
        }
        if (actionColumn == COL_RESET) {
            return "key.svg";
        }
        if (actionColumn == COL_ASIGNAR_ROL) {
            return "role.svg";
        }
        return "users.svg";
    }

    private String tooltipAccion(int actionColumn, String estado) {
        if (actionColumn == COL_EDITAR) {
            return "Editar usuario";
        }
        if (actionColumn == COL_ACTIVAR) {
            return esActivo(estado) ? "Inactivar usuario" : "Activar usuario";
        }
        if (actionColumn == COL_RESET) {
            return "Resetear clave";
        }
        if (actionColumn == COL_ASIGNAR_ROL) {
            return "Asignar roles";
        }
        return "Equipo supervisado";
    }

    private boolean esActivo(String estado) {
        return "ACTIVE".equalsIgnoreCase(estado) || "ACTIVO".equalsIgnoreCase(estado);
    }

    private String valorModelo(int modelRow, int col) {
        Object value = model.getValueAt(modelRow, col);
        return value == null ? "" : value.toString();
    }

    private static class GhostActionButton extends JButton {

        private boolean hover;
        private boolean selectedRow;

        GhostActionButton() {
            setHorizontalAlignment(JButton.CENTER);
            setVerticalAlignment(JButton.CENTER);
        }

        void setHover(boolean hover) {
            this.hover = hover;
        }

        void setSelectedRow(boolean selectedRow) {
            this.selectedRow = selectedRow;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hover || getModel().isRollover() || getModel().isPressed()) {
                    g2.setColor(new Color(226, 237, 248));
                    g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 10, 10);
                } else if (selectedRow) {
                    g2.setColor(new Color(219, 235, 247, 120));
                    g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 10, 10);
                }
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
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
