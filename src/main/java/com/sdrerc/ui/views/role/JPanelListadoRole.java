/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.role;

import com.sdrerc.application.RoleService;
import com.sdrerc.domain.model.PaginatedResult;
import com.sdrerc.domain.model.Role;
import com.sdrerc.ui.table.ButtonEditor;
import com.sdrerc.ui.table.ButtonRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 *
 * @author David
 */
public class JPanelListadoRole extends javax.swing.JPanel {

    private DefaultTableModel model;
    private Role role;
    private RoleService roleService; // 👈 AQUÍ
    private Long roleIdSeleccionado;
    private String roleNameSeleccionado;
    private String roleDescriptionSeleccionado;
    private String statusSeleccionado;
    private final JButton btnPrimeraPagina = new JButton("Primera");
    private final JButton btnPaginaAnterior = new JButton("Anterior");
    private final JButton btnPaginaSiguiente = new JButton("Siguiente");
    private final JButton btnUltimaPagina = new JButton("Última");
    private final JLabel lblPaginaRoles = new JLabel("Página 1 de 1");
    private final JLabel lblResumenRoles = new JLabel("Mostrando 0-0 de 0 roles");
    private final JComboBox<Integer> cboFilasPorPagina = new JComboBox<>(new Integer[]{10, 25, 50, 100});
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalRecords = 0;
    private int totalPages = 1;
    
    private static final int COL_ID = 0;
    private static final int COL_NOMBRE = 1;
    private static final int COL_DESCRIPCION = 2;
    private static final int COL_ESTADO = 3;
    private static final int COL_EDITAR = 4;
    private static final int COL_ACTIVAR = 5;
    /**
     * Creates new form JPanelListadoRole
     */
    public JPanelListadoRole() {
        initComponents();
        role = new Role();
        roleService = new RoleService(); // 👈 SE INICIALIZA AQUÍ        
        initTable();
        initFiltros(); 
        initEventos(); 
        configurarEventosTabla(); // 👈 AQUÍ
        corregirTextosVisibles();
        configurarLayoutRoles();
        configurarFiltrosRoles();
        configurarBotonesRoles();
        configurarTablaRoles();
        configurarRenderersRoles();
        configurarPaginacionRoles();
        //cargarRoles();
        cargarPaginaRoles();
        
    }

    private void corregirTextosVisibles() {
        jLabel2.setText("Mantenimiento de Roles");
        jLabel7.setText("Buscar rol");
        jLabel8.setText("Estado");
        btnNuevo1.setText("Nuevo rol");
        btnBusqueda.setText("Buscar");
        btnLimpiar1.setText("Limpiar");
    }

    private void configurarLayoutRoles() {
        removeAll();
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        setBackground(new Color(245, 247, 250));

        JPanel headerPanel = new JPanel(new BorderLayout(12, 4));
        headerPanel.setOpaque(false);

        JLabel subtitulo = new JLabel("Gestión de perfiles, permisos y accesos del sistema");
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitulo.setForeground(new Color(93, 105, 119));

        jLabel2.setFont(new Font("Arial", Font.BOLD, 22));
        jLabel2.setForeground(new Color(25, 42, 62));
        jLabel2.setHorizontalAlignment(JLabel.LEFT);

        headerPanel.add(jLabel2, BorderLayout.NORTH);
        headerPanel.add(subtitulo, BorderLayout.CENTER);
        headerPanel.add(btnNuevo1, BorderLayout.EAST);

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
        filtrosPanel.add(txtBuscarRol, gbc);

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

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(jScrollPane2, BorderLayout.CENTER);
        centerPanel.add(crearPanelPaginacionRoles(), BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel crearPanelPaginacionRoles() {
        JPanel paginationPanel = new JPanel(new BorderLayout(12, 0));
        paginationPanel.setBackground(Color.WHITE);
        paginationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        lblResumenRoles.setForeground(new Color(73, 85, 99));
        paginationPanel.add(lblResumenRoles, BorderLayout.WEST);

        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 8);
        gbc.gridy = 0;

        controlsPanel.add(btnPrimeraPagina, gbc);
        controlsPanel.add(btnPaginaAnterior, gbc);
        controlsPanel.add(lblPaginaRoles, gbc);
        controlsPanel.add(btnPaginaSiguiente, gbc);
        controlsPanel.add(btnUltimaPagina, gbc);
        controlsPanel.add(new JLabel("Filas por página"), gbc);
        gbc.insets = new Insets(0, 0, 0, 0);
        controlsPanel.add(cboFilasPorPagina, gbc);

        paginationPanel.add(controlsPanel, BorderLayout.EAST);
        return paginationPanel;
    }

    private void configurarFiltrosRoles() {
        txtBuscarRol.setPreferredSize(new Dimension(380, 34));
        txtBuscarRol.setMinimumSize(new Dimension(260, 34));
        cboFiltroEstado.setPreferredSize(new Dimension(145, 34));
        cboFiltroEstado.setMinimumSize(new Dimension(130, 34));
        txtBuscarRol.setToolTipText("Buscar por nombre de rol");
        cboFiltroEstado.setToolTipText("Filtrar roles por estado");
    }

    private void configurarBotonesRoles() {
        Dimension botonPrincipal = new Dimension(118, 36);
        Dimension botonFiltro = new Dimension(96, 34);

        btnNuevo1.setPreferredSize(botonPrincipal);
        btnNuevo1.setMinimumSize(botonPrincipal);
        btnBusqueda.setPreferredSize(botonFiltro);
        btnBusqueda.setMinimumSize(botonFiltro);
        btnLimpiar1.setPreferredSize(botonFiltro);
        btnLimpiar1.setMinimumSize(botonFiltro);

        btnNuevo1.setToolTipText("Registrar nuevo rol");
        btnBusqueda.setToolTipText("Buscar roles según filtros");
        btnLimpiar1.setToolTipText("Limpiar filtros");
    }

    private void configurarPaginacionRoles() {
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
        cboFilasPorPagina.setToolTipText("Cantidad de roles por página");

        btnPrimeraPagina.addActionListener(e -> irPrimeraPagina());
        btnPaginaAnterior.addActionListener(e -> irPaginaAnterior());
        btnPaginaSiguiente.addActionListener(e -> irPaginaSiguiente());
        btnUltimaPagina.addActionListener(e -> irUltimaPagina());
        cboFilasPorPagina.addActionListener(e -> {
            Object selected = cboFilasPorPagina.getSelectedItem();
            if (selected instanceof Integer) {
                pageSize = (Integer) selected;
                currentPage = 1;
                cargarPaginaRoles();
            }
        });

        actualizarControlesPaginacion();
    }

    private void configurarTablaRoles() {
        tblRoles.setRowHeight(36);
        tblRoles.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblRoles.setShowGrid(false);
        tblRoles.setIntercellSpacing(new Dimension(0, 0));
        tblRoles.setFillsViewportHeight(true);
        tblRoles.setSelectionBackground(new Color(219, 235, 247));
        tblRoles.setSelectionForeground(new Color(25, 42, 62));

        JTableHeader header = tblRoles.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        header.setReorderingAllowed(false);

        renombrarEncabezado(COL_ID, "ID");
        renombrarEncabezado(COL_NOMBRE, "Rol");
        renombrarEncabezado(COL_DESCRIPCION, "Descripción");
        renombrarEncabezado(COL_ESTADO, "Estado");
        renombrarEncabezado(COL_EDITAR, "Editar");
        renombrarEncabezado(COL_ACTIVAR, "Activar/Inactivar");
        tblRoles.getTableHeader().repaint();

        ajustarColumna(COL_ID, 0, 0, 0);
        ajustarColumna(COL_NOMBRE, 170, 220, 320);
        ajustarColumna(COL_DESCRIPCION, 420, 680, 1200);
        ajustarColumna(COL_ESTADO, 95, 112, 135);
        ajustarColumna(COL_EDITAR, 78, 90, 110);
        ajustarColumna(COL_ACTIVAR, 130, 150, 180);
    }

    private void configurarRenderersRoles() {
        tblRoles.getColumnModel().getColumn(COL_ESTADO).setCellRenderer(new EstadoRolRenderer());
    }

    private void ajustarColumna(int indice, int min, int preferido, int max) {
        TableColumn columna = tblRoles.getColumnModel().getColumn(indice);
        columna.setMinWidth(min);
        columna.setPreferredWidth(preferido);
        columna.setMaxWidth(max);
        if (max == 0) {
            columna.setResizable(false);
        }
    }

    private void renombrarEncabezado(int indice, String texto) {
        tblRoles.getColumnModel().getColumn(indice).setHeaderValue(texto);
    }
    
    private void initFiltros() {
        cboFiltroEstado.removeAllItems(); // evita duplicados
        cboFiltroEstado.addItem("TODOS");
        cboFiltroEstado.addItem("ACTIVE");
        cboFiltroEstado.addItem("INACTIVE");
    }
    
    private void initEventos() {
        
        
        tblRoles.getColumn("EDITAR")
        .setCellRenderer(new ButtonRenderer("Editar"));
        tblRoles.getColumn("EDITAR")
                .setCellEditor(new ButtonEditor(tblRoles, this, 4));

        tblRoles.getColumn("ACTIVAR")
                .setCellRenderer(new ButtonRenderer("Activar / Inactivar"));
        tblRoles.getColumn("ACTIVAR")
                .setCellEditor(new ButtonEditor(tblRoles, this, 5));
        
        
        txtBuscarRol.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    resetearPaginacionAlBuscar();
                }
            }
        });
        
        tblRoles.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && tblRoles.getSelectedRow() != -1) {
                    cargarRolDesdeTabla();
                }
            }
        });
    }
    
    
    private void configurarEventosTabla() {

        tblRoles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                int row = tblRoles.rowAtPoint(e.getPoint());
                int col = tblRoles.columnAtPoint(e.getPoint());

                if (row < 0 || col < 0) return;

                // Columna ACTIVAR / INACTIVAR
                if (col == COL_ACTIVAR) {
                    cambiarEstadoDesdeTabla(row);
                }

                // Columna EDITAR
                if (col == COL_EDITAR) {
                    editarDesdeTabla(row);
                }
            }
        });
    }
    
    private void cargarRolDesdeTabla() {

        int row = tblRoles.getSelectedRow();
        
        roleIdSeleccionado = Long.parseLong(model.getValueAt(row, 0).toString());
        roleNameSeleccionado = model.getValueAt(row, 1).toString();
        roleDescriptionSeleccionado = model.getValueAt(row, 2).toString();
        statusSeleccionado = model.getValueAt(row, 3).toString();         
    }
    
    public void cambiarEstadoDesdeTabla(int row) {
        int modelRow = tblRoles.convertRowIndexToModel(row);
        Long id = Long.parseLong(model.getValueAt(modelRow, 0).toString());
        String estadoActual = model.getValueAt(modelRow, 3).toString();
        String nuevoEstado = estadoActual.equals("ACTIVE") ? "INACTIVE" : "ACTIVE";

        int r = JOptionPane.showConfirmDialog(
            this,
            "¿Cambiar estado a " + nuevoEstado + "?",
            "Confirmar",
            JOptionPane.YES_NO_OPTION
        );

        if (r == JOptionPane.YES_OPTION) {
            try {
                roleService.cambiarEstado(id, nuevoEstado);
                
                // 🔥 IMPORTANTE: cerrar edición si existe
                if (tblRoles.isEditing()) {
                    tblRoles.getCellEditor().stopCellEditing();
                }
                
                model.setValueAt(nuevoEstado, modelRow, 3);
                model.setValueAt(
                    nuevoEstado.equals("ACTIVE") ? "Inactivar" : "Activar",
                    modelRow,
                    COL_ACTIVAR
                );

                cargarPaginaRoles();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }
    
    private void buscarRoles() {
        resetearPaginacionAlBuscar();
    }

    private void cargarPaginaRoles() {
        try {
            model.setRowCount(0);

            String nombre = txtBuscarRol.getText().trim();
            String estado = cboFiltroEstado.getSelectedItem().toString();

            PaginatedResult<Role> result =
                    roleService.buscarPaginado(nombre, estado, currentPage, pageSize);

            currentPage = result.getPage();
            pageSize = result.getPageSize();
            totalRecords = result.getTotalRecords();
            totalPages = result.getTotalPages();

            for (Role r : result.getData()) {
                model.addRow(new Object[]{
                    r.getRoleId(),
                    r.getRoleName(),
                    r.getDescription(),
                    r.getStatus(),
                    "Editar",
                    r.getStatus().equals("ACTIVE") ? "Inactivar" : "Activar"
                });
            }

            if (model.getRowCount() == 0 && totalRecords > 0 && currentPage > 1) {
                currentPage--;
                cargarPaginaRoles();
                return;
            }

            actualizarControlesPaginacion();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
    
    private void resetFiltros() {
        txtBuscarRol.setText("");
        cboFiltroEstado.setSelectedIndex(0);
        resetearPaginacionAlBuscar();
    }

    private void resetearPaginacionAlBuscar() {
        currentPage = 1;
        cargarPaginaRoles();
    }

    private void irPrimeraPagina() {
        if (currentPage != 1) {
            currentPage = 1;
            cargarPaginaRoles();
        }
    }

    private void irPaginaAnterior() {
        if (currentPage > 1) {
            currentPage--;
            cargarPaginaRoles();
        }
    }

    private void irPaginaSiguiente() {
        if (currentPage < totalPages) {
            currentPage++;
            cargarPaginaRoles();
        }
    }

    private void irUltimaPagina() {
        if (currentPage != totalPages) {
            currentPage = totalPages;
            cargarPaginaRoles();
        }
    }

    private void actualizarControlesPaginacion() {
        int from = totalRecords == 0 ? 0 : ((currentPage - 1) * pageSize) + 1;
        int to = totalRecords == 0 ? 0 : Math.min(currentPage * pageSize, totalRecords);

        lblPaginaRoles.setText("Página " + currentPage + " de " + totalPages);
        lblResumenRoles.setText("Mostrando " + from + "-" + to + " de " + totalRecords + " roles");

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
        tblRoles = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        txtBuscarRol = new javax.swing.JTextField();
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
        jLabel2.setText("MANTENIMIENTO DE ROLES");
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

        tblRoles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tblRoles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblRolesMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblRoles);

        jLabel7.setText("Buscar rol:");

        txtBuscarRol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarRolActionPerformed(evt);
            }
        });

        jLabel8.setText("Estado:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBuscarRol, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboFiltroEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnNuevo1, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                            .addComponent(btnLimpiar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(429, Short.MAX_VALUE))
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
                    .addComponent(txtBuscarRol, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        /*
        Window parent = SwingUtilities.getWindowAncestor(this);
        DlgEditarRol dlg = new DlgEditarRol(parent, Dialog.ModalityType.APPLICATION_MODAL, null, roleService);
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
        buscarRoles();
        */
        
        /*
        DefaultTableModel model = (DefaultTableModel) tblRoles.getModel();

        Role role = new Role();
        role.setRoleId(Long.parseLong(model.getValueAt(row, COL_ID).toString()));
        role.setRoleName(model.getValueAt(row, COL_NOMBRE).toString());
        role.setDescription(model.getValueAt(row, COL_DESCRIPCION).toString());
        role.setStatus(model.getValueAt(row, COL_ESTADO).toString());
        */

        Window parent = SwingUtilities.getWindowAncestor(this);
        DlgEditarRol dlg = new DlgEditarRol(parent, Dialog.ModalityType.APPLICATION_MODAL, role, roleService,false);
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

    private void tblRolesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblRolesMouseClicked
        
    }//GEN-LAST:event_tblRolesMouseClicked

    private void txtBuscarRolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarRolActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBuscarRolActionPerformed
    
    
    private void initTable() {
        model = new DefaultTableModel(
            new Object[]{"ID", "ROL", "DESCRIPCIÓN", "ESTADO", "EDITAR", "ACTIVAR"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo las columnas de botones
                return column == 4 || column == 5;
            }
        };
        tblRoles.setModel(model);
    }
    
    public void editarDesdeTabla(int row) { 
        
        DefaultTableModel model = (DefaultTableModel) tblRoles.getModel();
        int modelRow = tblRoles.convertRowIndexToModel(row);

        Role role = new Role();
        role.setRoleId(Long.parseLong(model.getValueAt(modelRow, COL_ID).toString()));
        role.setRoleName(model.getValueAt(modelRow, COL_NOMBRE).toString());
        role.setDescription(model.getValueAt(modelRow, COL_DESCRIPCION).toString());
        role.setStatus(model.getValueAt(modelRow, COL_ESTADO).toString());

        Window parent = SwingUtilities.getWindowAncestor(this);
        DlgEditarRol dlg = new DlgEditarRol(parent, Dialog.ModalityType.APPLICATION_MODAL, role, roleService,true);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        cargarPaginaRoles();
        
        /*
        roleIdSeleccionado = Long.parseLong(model.getValueAt(row, 0).toString());
        roleNameSeleccionado = model.getValueAt(row, 1).toString();
        roleDescriptionSeleccionado = model.getValueAt(row, 2).toString();
        statusSeleccionado = model.getValueAt(row, 3).toString();     
        
        DefaultTableModel model = (DefaultTableModel) tblRoles.getModel();

        txtRoleId.setText(model.getValueAt(row, COL_ID).toString());
        txtRoleName.setText(model.getValueAt(row, COL_NOMBRE).toString());
        txtDescription.setText(model.getValueAt(row, COL_DESCRIPCION).toString());
        cboStatus.setSelectedItem(model.getValueAt(row, COL_ESTADO).toString());

        modoEdicion = true;

        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);

        txtRoleName.requestFocus();
        */
        
    }

    private static class EstadoRolRenderer extends DefaultTableCellRenderer {

        @Override
        public java.awt.Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            String estado = value == null ? "" : value.toString();
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));

            if ("ACTIVE".equalsIgnoreCase(estado) || "ACTIVO".equalsIgnoreCase(estado)) {
                label.setText("ACTIVO");
                if (!isSelected) {
                    label.setForeground(new Color(24, 112, 70));
                    label.setBackground(new Color(225, 244, 235));
                }
            } else if ("INACTIVE".equalsIgnoreCase(estado) || "INACTIVO".equalsIgnoreCase(estado)) {
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
    /*
    private void cargarRoles() {
        try {
            model.setRowCount(0);
            for (Role r : service.listar()) {
                model.addRow(new Object[]{
                    r.getRoleId(),
                    r.getRoleName(),
                    r.getDescription(),
                    r.getStatus(),
                    "Editar",
                    r.getStatus().equals("ACTIVE") ? "Inactivar" : "Activar"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
    */

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
    private javax.swing.JTable tblRoles;
    private javax.swing.JTextField txtBuscarRol;
    private javax.swing.JTextField txtValorBusqueda;
    // End of variables declaration//GEN-END:variables
}
