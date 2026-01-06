/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.role;

import com.sdrerc.application.RoleService;
import com.sdrerc.domain.model.Role;
import com.sdrerc.ui.table.ButtonEditor;
import com.sdrerc.ui.table.ButtonRenderer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class JPanelListadoRole extends javax.swing.JPanel {

    private DefaultTableModel model;
    private RoleService service = new RoleService();
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
    /**
     * Creates new form JPanelListadoRole
     */
    public JPanelListadoRole() {
        initComponents();        
        initTable();
        initFiltros(); 
        initEventos(); 
        configurarEventosTabla(); // 👈 AQUÍ
        //cargarRoles();
        buscarRoles();
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
                    buscarRoles();
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
                service.cambiarEstado(id, nuevoEstado);
                
                // 🔥 IMPORTANTE: cerrar edición si existe
                if (tblRoles.isEditing()) {
                    tblRoles.getCellEditor().stopCellEditing();
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
    
    private void buscarRoles() {
        try {
            model.setRowCount(0);

            String nombre = txtBuscarRol.getText().trim();
            String estado = cboFiltroEstado.getSelectedItem().toString();

            for (Role r : service.buscar(nombre, estado)) {
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
    
    private void resetFiltros() {
        txtBuscarRol.setText("");
        cboFiltroEstado.setSelectedIndex(0);
        buscarRoles();
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
        jLabel2.setText("MANTENIMIENTO DE ROLES");
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 621, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 632, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                            .addComponent(btnLimpiar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(429, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel2)
                .addGap(57, 57, 57)
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
        // TODO add your handling code here:
        //MenuPrincipal.ShowJPanel(new JPanelRegistrarExpediente());
    }//GEN-LAST:event_btnNuevo1ActionPerformed

    private void btnLimpiar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiar1ActionPerformed
        // TODO add your handling code here:
        //limpiarCampos();
        resetFiltros();
    }//GEN-LAST:event_btnLimpiar1ActionPerformed

    private void btnBusquedaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBusquedaActionPerformed
        buscarRoles();        
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
        roleIdSeleccionado = Long.parseLong(model.getValueAt(row, 0).toString());
        roleNameSeleccionado = model.getValueAt(row, 1).toString();
        roleDescriptionSeleccionado = model.getValueAt(row, 2).toString();
        statusSeleccionado = model.getValueAt(row, 3).toString();         
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
