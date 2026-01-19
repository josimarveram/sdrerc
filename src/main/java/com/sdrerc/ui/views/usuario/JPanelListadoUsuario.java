/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.usuario;

import com.sdrerc.ui.views.role.*;
import com.sdrerc.application.RoleService;
import com.sdrerc.application.UserService;
import com.sdrerc.domain.model.User;
import com.sdrerc.ui.table.ButtonEditor;
import com.sdrerc.ui.table.ButtonEditorUsuario;
import com.sdrerc.ui.table.ButtonRenderer;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class JPanelListadoUsuario extends javax.swing.JPanel {

    private DefaultTableModel model;
    private UserService userService; // 👈 AQUÍ
    private RoleService roleService; // 👈 AQUÍ
    private User usuario;
    private Long roleIdSeleccionado;
    private String roleNameSeleccionado;
    private String roleDescriptionSeleccionado;
    private String statusSeleccionado;
    
    private static final int COL_ID = 0;
    private static final int COL_NOMBRE = 1;
    private static final int COL_DESCRIPCION = 2;
    private static final int COL_ESTADO = 3;
    private static final int COL_EDITAR = 4;
    private static final int COL_ACTIVAR = 5;
    private static final int COL_RESET = 6;
    private static final int COL_ASIGNAR_ROL = 7;
    /**
     * Creates new form JPanelListadoRole
     */
    public JPanelListadoUsuario() {
        initComponents();
        usuario = new User();
        userService = new UserService(); // 👈 SE INICIALIZA AQUÍ   
        roleService = new RoleService(); // 👈 SE INICIALIZA AQUÍ      
        initTable();
        initFiltros(); 
        initEventos(); 
        configurarEventosTabla(); // 👈 AQUÍ
        //cargarRoles();
        buscarUsuarios();
        
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
        
        
        txtBuscarUsuario.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    buscarUsuarios();
                }
            }
        });
        
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
                model.addRow(new Object[]{
                    r.getUserId(),
                    r.getUsername(),
                    r.getFullName(),
                    r.getStatus(),
                    "Editar",
                    r.getStatus().equals("ACTIVE") ? "Inactivar" : "Activar",
                    "Resetear",
                    "Asignar Rol",
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

        btnLimpiar.setText("Limpiar");
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

        btnLimpiar1.setText("Limpiar");
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtBuscarUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            new Object[]{"ID", "ROL", "DESCRIPCIÓN", "ESTADO", "EDITAR", "ACTIVAR","CAMBIAR_CLAVE","ASIGNAR_ROL"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo las columnas de botones
                return column == 4 || column == 5 || column == 6;
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
