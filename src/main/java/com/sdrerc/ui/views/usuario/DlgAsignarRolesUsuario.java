/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.sdrerc.ui.views.usuario;

import com.sdrerc.application.RoleService;
import com.sdrerc.application.UserService;
import com.sdrerc.domain.model.Role;
import com.sdrerc.ui.common.icon.IconUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import java.util.stream.Collectors;

/**
 *
 * @author David
 */
public class DlgAsignarRolesUsuario extends javax.swing.JDialog {

    /**
     * Creates new form DlgEditarRol
     */
    
    private final Long userId;
    private final UserService userService;
    private final RoleService roleService;
    private final List<RoleCard> roleCards = new ArrayList<>();
    
    public DlgAsignarRolesUsuario(Window parent,
            Long userId,
            String username,
            UserService userService,
            RoleService roleService) {
        super(parent, "Asignar Roles", ModalityType.APPLICATION_MODAL);
        initComponents();

        this.userId = userId;
        this.userService = userService;
        this.roleService = roleService;

        lblUsuario.setText(username);

        cargarRoles();
        marcarRolesAsignados();
        configurarDialogoRoles();

    }
    
    private void marcarRolesAsignados() {

        try {
            List<Role> rolesUsuario =
                userService.obtenerRolesUsuario(userId);

            ListModel<Role> model = lstRoles.getModel();
            List<Integer> indices = new ArrayList<>();

            for (int i = 0; i < model.getSize(); i++) {
                Role r = model.getElementAt(i);
                for (Role ru : rolesUsuario) {
                    if (r.getRoleId().equals(ru.getRoleId())) {
                        indices.add(i);
                    }
                }
            }

            lstRoles.setSelectedIndices(
                indices.stream().mapToInt(Integer::intValue).toArray()
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,e.getMessage());
        }
    }
    
    private void cargarRoles() {

        DefaultListModel<Role> model = new DefaultListModel<>();

        try {
            List<Role> roles = new ArrayList<>(roleService.listar());
            roles.sort(Comparator
                    .comparingInt(this::prioridadRol)
                    .thenComparing(r -> textoSeguro(r.getRoleName())));

            for (Role r : roles) {
                model.addElement(r);
            }
            lstRoles.setModel(model);
            lstRoles.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
            );
        } catch (SQLException ex) {
            
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void configurarDialogoRoles() {
        setTitle("Asignar roles");

        btnGuardar.setText("Guardar roles");
        btnGuardar.setToolTipText("Guardar perfiles de acceso seleccionados.");
        btnGuardar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        IconUtils.applyIcon(btnGuardar, "role.svg");

        btnCancelar.setText("Cancelar");
        btnCancelar.setToolTipText("Cerrar sin guardar cambios.");
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        IconUtils.applyIcon(btnCancelar, "clear.svg");

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(new Color(245, 247, 250));
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        root.add(crearCabeceraRoles(), BorderLayout.NORTH);
        root.add(crearPanelRoles(), BorderLayout.CENTER);
        root.add(crearBotoneraRoles(), BorderLayout.SOUTH);

        setContentPane(root);
        setPreferredSize(new Dimension(620, 520));
        setMinimumSize(new Dimension(620, 520));
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel crearCabeceraRoles() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Asignar roles");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        titulo.setForeground(new Color(15, 23, 42));

        JLabel subtitulo = new JLabel("Seleccione los perfiles de acceso que tendra este usuario.");
        subtitulo.setFont(subtitulo.getFont().deriveFont(Font.PLAIN, 13f));
        subtitulo.setForeground(new Color(71, 85, 105));

        JPanel contexto = new JPanel(new GridBagLayout());
        contexto.setBackground(Color.WHITE);
        contexto.setBorder(crearBordeCard(new Color(226, 232, 240)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 4, 0);
        contexto.add(crearTextoContexto("Usuario: ", lblUsuario.getText()), gbc);

        gbc.gridy++;
        JLabel ayuda = new JLabel("Los roles ABOGADO y SUPERVISION requieren una persona operativa vinculada.");
        ayuda.setForeground(new Color(100, 116, 139));
        ayuda.setFont(ayuda.getFont().deriveFont(Font.PLAIN, 12f));
        contexto.add(ayuda, gbc);

        wrapper.add(titulo);
        wrapper.add(Box.createVerticalStrut(4));
        wrapper.add(subtitulo);
        wrapper.add(Box.createVerticalStrut(14));
        wrapper.add(contexto);

        return wrapper;
    }

    private JLabel crearTextoContexto(String etiqueta, String valor) {
        JLabel label = new JLabel("<html><b>" + etiqueta + "</b>" + textoSeguro(valor) + "</html>");
        label.setForeground(new Color(30, 41, 59));
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 13f));
        return label;
    }

    private JScrollPane crearPanelRoles() {
        JPanel panelRoles = new JPanel();
        panelRoles.setLayout(new BoxLayout(panelRoles, BoxLayout.Y_AXIS));
        panelRoles.setBackground(new Color(245, 247, 250));
        roleCards.clear();

        ListModel<Role> model = lstRoles.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Role role = model.getElementAt(i);
            RoleCard card = new RoleCard(role, i, lstRoles.isSelectedIndex(i));
            roleCards.add(card);
            panelRoles.add(card);
            panelRoles.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(panelRoles);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(245, 247, 250));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JPanel crearBotoneraRoles() {
        JPanel botonera = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botonera.setOpaque(false);
        botonera.add(btnGuardar);
        botonera.add(btnCancelar);
        return botonera;
    }

    private void alternarRol(RoleCard card) {
        card.setSelected(!card.isSelected());
        sincronizarSeleccionLista();
    }

    private void sincronizarSeleccionLista() {
        List<Integer> indices = new ArrayList<>();
        for (RoleCard card : roleCards) {
            if (card.isSelected()) {
                indices.add(card.getModelIndex());
            }
        }

        lstRoles.setSelectedIndices(
                indices.stream().mapToInt(Integer::intValue).toArray()
        );
    }

    private int prioridadRol(Role role) {
        String nombre = normalizarRol(role);
        if ("ADMIN_SISTEMA".equals(nombre)) {
            return 0;
        }
        if ("SUPERVISION".equals(nombre)) {
            return 1;
        }
        if ("ABOGADO".equals(nombre)) {
            return 2;
        }
        if ("EJECUCION".equals(nombre)) {
            return 3;
        }
        if ("NOTIFICACION".equals(nombre)) {
            return 4;
        }
        return 100;
    }

    private String descripcionRol(Role role) {
        if (role != null && role.getDescription() != null && !role.getDescription().trim().isEmpty()) {
            return role.getDescription().trim();
        }

        String nombre = normalizarRol(role);
        if ("ADMIN_SISTEMA".equals(nombre)) {
            return "Acceso administrativo al sistema.";
        }
        if ("SUPERVISION".equals(nombre)) {
            return "Permite gestionar equipo supervisado.";
        }
        if ("ABOGADO".equals(nombre)) {
            return "Permite trabajar expedientes asignados.";
        }
        if ("EJECUCION".equals(nombre)) {
            return "Acceso a actividades de ejecucion.";
        }
        if ("NOTIFICACION".equals(nombre)) {
            return "Acceso a actividades de notificacion.";
        }
        return "Perfil de acceso configurado en el sistema.";
    }

    private String normalizarRol(Role role) {
        return role == null || role.getRoleName() == null
                ? ""
                : role.getRoleName().trim().toUpperCase();
    }

    private String textoSeguro(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private Border crearBordeCard(Color color) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        );
    }

    private final class RoleCard extends JPanel {

        private final Role role;
        private final int modelIndex;
        private final JCheckBox check;
        private boolean hover;

        RoleCard(Role role, int modelIndex, boolean selected) {
            super(new GridBagLayout());
            this.role = role;
            this.modelIndex = modelIndex;
            this.check = new JCheckBox();
            this.check.setSelected(selected);
            this.check.setOpaque(false);
            construir();
            actualizarEstilo();
        }

        private void construir() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
            setAlignmentX(LEFT_ALIGNMENT);

            JLabel nombre = new JLabel(textoSeguro(role.getRoleName()));
            nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 13f));
            nombre.setForeground(new Color(15, 23, 42));

            JLabel descripcion = new JLabel(descripcionRol(role));
            descripcion.setFont(descripcion.getFont().deriveFont(Font.PLAIN, 12f));
            descripcion.setForeground(new Color(100, 116, 139));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridheight = 2;
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.insets = new Insets(2, 0, 0, 12);
            add(check, gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridheight = 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 2, 0);
            add(nombre, gbc);

            gbc.gridy = 1;
            gbc.insets = new Insets(0, 0, 0, 0);
            add(descripcion, gbc);

            check.addActionListener(e -> {
                actualizarEstilo();
                sincronizarSeleccionLista();
            });

            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    alternarRol(RoleCard.this);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    actualizarEstilo();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    actualizarEstilo();
                }
            };
            addMouseListener(mouse);
            nombre.addMouseListener(mouse);
            descripcion.addMouseListener(mouse);
        }

        int getModelIndex() {
            return modelIndex;
        }

        boolean isSelected() {
            return check.isSelected();
        }

        void setSelected(boolean selected) {
            check.setSelected(selected);
            actualizarEstilo();
        }

        private void actualizarEstilo() {
            if (check.isSelected()) {
                setBackground(new Color(232, 241, 252));
                setBorder(crearBordeCard(new Color(96, 165, 250)));
            } else if (hover) {
                setBackground(new Color(248, 250, 252));
                setBorder(crearBordeCard(new Color(203, 213, 225)));
            } else {
                setBackground(Color.WHITE);
                setBorder(crearBordeCard(new Color(226, 232, 240)));
            }
            repaint();
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

        btnCancelar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        lblUsuario = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstRoles = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        btnCancelar.setText("CANCELAR");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        btnGuardar.setText("GUARDAR");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(lstRoles);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(75, 75, 75)
                        .addComponent(lblUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(25, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancelar)
                .addGap(78, 78, 78))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar)
                    .addComponent(btnCancelar))
                .addGap(21, 21, 21))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
       
        List<Role> seleccionados = lstRoles.getSelectedValuesList();

        if (seleccionados.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Debe asignar al menos un rol");
            return;
        }

        try {
            userService.asignarRoles(
                userId,
                seleccionados.stream()
                    .map(Role::getRoleId)
                    .collect(Collectors.toList())
            );
            dispose();
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Asignación de roles",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,e.getMessage());
        }
        /*
        if (!validar()) return;
        
        try {        
            String rawPassword = new String(pwdNueva.getPassword());
            String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
            userService.cambiarPassword(userId, hash);   
            JOptionPane.showMessageDialog(
                    this,
                    "La contraseña fue actualizada correctamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar contraseña");
        }
        
        */
        
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        dispose();
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCancelarActionPerformed

    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DlgAsignarRolesUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DlgAsignarRolesUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DlgAsignarRolesUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DlgAsignarRolesUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JList lstRoles;
    // End of variables declaration//GEN-END:variables
}
