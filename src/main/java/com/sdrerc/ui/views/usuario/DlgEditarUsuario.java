/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.sdrerc.ui.views.usuario;

import com.sdrerc.ui.views.role.*;
import com.sdrerc.application.RoleService;
import com.sdrerc.application.UserService;
import com.sdrerc.domain.model.Role;
import com.sdrerc.domain.model.User;
import com.sdrerc.ui.common.icon.IconUtils;
import com.sdrerc.util.ComboBoxUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author David
 */
public class DlgEditarUsuario extends javax.swing.JDialog {

    /**
     * Creates new form DlgEditarRol
     */
    
    private User usuario;
    private UserService userService;
    private boolean modoEdicion = false;
    
    public DlgEditarUsuario(Window parent, ModalityType modalityType, User usuario, UserService userService, boolean Edicion) {
        super(parent, modalityType);
        initComponents();
        
        modoEdicion = Edicion;
        inicializarCombo();
        this.usuario = usuario;
        this.userService = userService;
        cargarDatos();
        configurarBotones();
        configurarDialogoUsuario();
        ComboBoxUtils.applySmartRenderer(getContentPane());
    }

    private void configurarDialogoUsuario() {
        corregirTextosVisibles();
        configurarAyudasUsuario();
        configurarBotonesUsuario();
        configurarDimensionesUsuario();
        reconstruirLayoutUsuario();
        pack();
        ajustarTamanoInicialUsuario();
        setLocationRelativeTo(getParent());
    }

    private void corregirTextosVisibles() {
        setTitle(modoEdicion ? "Editar usuario" : "Nuevo usuario");
        jLabel1.setText("Usuario");
        jLabel2.setText("Nombre de cuenta");
        lblStatus.setText("Estado de la cuenta");
        lblPassword.setText("Contraseña temporal");
        lblConfirmar.setText("Confirmar contraseña");
        btnGuardar.setText("Guardar usuario");
        btnActualizar.setText("Guardar cambios");
        btnCancelar.setText("Cancelar");
    }

    private void configurarAyudasUsuario() {
        txtUsuario.setToolTipText("Login de ingreso al sistema.");
        txtFullName.setToolTipText("Nombre visible para usuarios administrativos o sin persona operativa.");
        cboStatus.setToolTipText("Controla si la cuenta puede acceder al sistema.");
        pwdPassword.setToolTipText("Contraseña temporal para el nuevo usuario.");
        pwdConfirmar.setToolTipText("Repita la contraseña temporal.");
    }

    private void configurarBotonesUsuario() {
        aplicarIcono(btnGuardar, "add.svg");
        aplicarIcono(btnActualizar, "active.svg");
        aplicarIcono(btnCancelar, "clear.svg");

        Dimension buttonSize = new Dimension(180, 36);
        btnGuardar.setPreferredSize(buttonSize);
        btnGuardar.setMinimumSize(buttonSize);
        btnActualizar.setPreferredSize(buttonSize);
        btnActualizar.setMinimumSize(buttonSize);
        btnCancelar.setPreferredSize(new Dimension(124, 36));
        btnCancelar.setMinimumSize(new Dimension(124, 36));
    }

    private void configurarDimensionesUsuario() {
        Dimension inputSize = new Dimension(260, 34);
        txtUsuario.setPreferredSize(inputSize);
        txtUsuario.setMinimumSize(inputSize);
        txtFullName.setPreferredSize(inputSize);
        txtFullName.setMinimumSize(inputSize);
        cboStatus.setPreferredSize(inputSize);
        cboStatus.setMinimumSize(inputSize);
        pwdPassword.setPreferredSize(inputSize);
        pwdPassword.setMinimumSize(inputSize);
        pwdConfirmar.setPreferredSize(inputSize);
        pwdConfirmar.setMinimumSize(inputSize);

        Dimension labelSize = new Dimension(190, 28);
        jLabel1.setPreferredSize(labelSize);
        jLabel2.setPreferredSize(labelSize);
        lblStatus.setPreferredSize(labelSize);
        lblPassword.setPreferredSize(labelSize);
        lblConfirmar.setPreferredSize(labelSize);

        Font labelFont = jLabel1.getFont().deriveFont(Font.PLAIN, 13f);
        jLabel1.setFont(labelFont);
        jLabel2.setFont(labelFont);
        lblStatus.setFont(labelFont);
        lblPassword.setFont(labelFont);
        lblConfirmar.setFont(labelFont);
    }

    private void aplicarIcono(JButton button, String iconName) {
        Icon icon = IconUtils.load(iconName, 16);
        if (icon != null) {
            button.setIcon(icon);
            button.setIconTextGap(8);
        }
    }

    private void reconstruirLayoutUsuario() {
        String mensaje = modoEdicion
                ? "Los datos personales de usuarios operativos se administran desde Equipo Jurídico o desde la acción Persona en el listado."
                : "Para abogados y supervisores use el módulo Equipo Jurídico.";

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        root.setBackground(new Color(245, 247, 250));

        JPanel infoPanel = new JPanel(new BorderLayout(0, 5));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel titulo = new JLabel(modoEdicion ? "Editar usuario" : "Nuevo usuario");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        titulo.setForeground(new Color(25, 42, 62));

        JLabel subtitulo = new JLabel("Administra la cuenta de acceso al sistema.");
        subtitulo.setForeground(new Color(73, 85, 99));

        JLabel contexto = new JLabel("<html><body style='width:420px'>" + mensaje + "</body></html>");
        contexto.setForeground(new Color(93, 105, 119));

        infoPanel.add(titulo, BorderLayout.NORTH);
        infoPanel.add(subtitulo, BorderLayout.CENTER);
        infoPanel.add(contexto, BorderLayout.SOUTH);

        JPanel formPanel = crearFormularioUsuario();
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        card.add(formPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = crearPanelBotonesUsuario();

        root.add(infoPanel, BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);
        root.add(buttonsPanel, BorderLayout.SOUTH);

        setContentPane(root);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private JPanel crearFormularioUsuario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;

        agregarFilaFormulario(panel, gbc, jLabel1, txtUsuario);
        agregarFilaFormulario(panel, gbc, jLabel2, txtFullName);
        if (!modoEdicion) {
            agregarFilaFormulario(panel, gbc, lblPassword, pwdPassword);
            agregarFilaFormulario(panel, gbc, lblConfirmar, pwdConfirmar);
        }
        agregarFilaFormulario(panel, gbc, lblStatus, cboStatus);
        return panel;
    }

    private JPanel crearPanelBotonesUsuario() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);
        if (!modoEdicion) {
            panel.add(btnGuardar);
        } else {
            panel.add(btnActualizar);
        }
        panel.add(btnCancelar);
        return panel;
    }

    private void agregarFilaFormulario(JPanel panel, GridBagConstraints gbc, JLabel label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
        gbc.gridy++;
    }

    private void ajustarTamanoInicialUsuario() {
        Dimension min = new Dimension(720, modoEdicion ? 480 : 560);
        setMinimumSize(min);
        int width = Math.max(getWidth(), min.width);
        int height = Math.max(getHeight(), min.height);
        setSize(width, height);
        getContentPane().revalidate();
        getContentPane().repaint();
    }
    
    private void configurarBotones() {
        btnGuardar.setVisible(!modoEdicion);
        btnActualizar.setVisible(modoEdicion);
        
        pwdPassword.setVisible(!modoEdicion);
        pwdConfirmar.setVisible(!modoEdicion);
        lblPassword.setVisible(!modoEdicion);
        lblConfirmar.setVisible(!modoEdicion);        
    }    
    
    private void inicializarCombo() {
        cboStatus.removeAllItems();
        cboStatus.addItem("ACTIVE");
        cboStatus.addItem("INACTIVE");
    }
    
    private void cargarDatos() {
        txtUsuario.setText(usuario.getUsername());
        txtFullName.setText(usuario.getFullName());
        cboStatus.setSelectedItem(usuario.getStatus());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtUsuario = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtFullName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        cboStatus = new javax.swing.JComboBox<>();
        lblStatus = new javax.swing.JLabel();
        btnActualizar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        lblPassword = new javax.swing.JLabel();
        lblConfirmar = new javax.swing.JLabel();
        pwdConfirmar = new javax.swing.JPasswordField();
        pwdPassword = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Usuario:");

        txtFullName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFullNameActionPerformed(evt);
            }
        });

        jLabel2.setText("Fullname:");

        lblStatus.setText("Estado:");

        btnActualizar.setText("ACTUALIZAR");
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarActionPerformed(evt);
            }
        });

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

        lblPassword.setText("Contraseña:");

        lblConfirmar.setText("Confirmar contraseña:");

        pwdConfirmar.setText("jPasswordField1");

        pwdPassword.setText("jPasswordField1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelar))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblConfirmar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(31, 31, 31)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cboStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtUsuario, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtFullName, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pwdConfirmar, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                            .addComponent(pwdPassword))))
                .addGap(114, 114, 114))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFullName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassword)
                    .addComponent(pwdPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblConfirmar, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pwdConfirmar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblStatus)
                    .addComponent(cboStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar)
                    .addComponent(btnActualizar)
                    .addComponent(btnCancelar))
                .addGap(31, 31, 31))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtFullNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFullNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFullNameActionPerformed

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
        // TODO add your handling code here:
        usuario.setUsername(txtUsuario.getText().trim());
        usuario.setFullName(txtFullName.getText().trim());
        usuario.setStatus(cboStatus.getSelectedItem().toString());

        try {
            userService.actualizar(usuario);
        } catch (SQLException ex) {
            Logger.getLogger(DlgEditarUsuario.class.getName()).log(Level.SEVERE, null, ex);
        }

        JOptionPane.showMessageDialog(this, "Usuario actualizado correctamente");
        dispose(); // cerrar popup
    }//GEN-LAST:event_btnActualizarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        
        
        this.validar();
        
        usuario.setUsername(txtUsuario.getText().trim());
        usuario.setFullName(txtFullName.getText().trim());
        usuario.setStatus(cboStatus.getSelectedItem().toString());
        
        String rawPassword = new String(pwdPassword.getPassword());
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        usuario.setPasswordHash(hash);
        
        try {
            userService.registrar(usuario);
        } catch (SQLException ex) {
            Logger.getLogger(DlgEditarUsuario.class.getName()).log(Level.SEVERE, null, ex);
        }

        JOptionPane.showMessageDialog(this, "Usuario registrado correctamente");
        dispose();
        // TODO add your handling code here:
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        dispose();
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCancelarActionPerformed

    private boolean validar() {

        if (txtUsuario.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese usuario");
            return false;
        }

        if (pwdPassword.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this,"La contraseña es obligatoria");
            return false;
        }

        if (!Arrays.equals(pwdPassword.getPassword(), pwdConfirmar.getPassword())) {
            JOptionPane.showMessageDialog(this,"Las contraseñas no coinciden");
            return false;
        }

        return true;
    }
    
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
            java.util.logging.Logger.getLogger(DlgEditarUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DlgEditarUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DlgEditarUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DlgEditarUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JComboBox<String> cboStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lblConfirmar;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JPasswordField pwdConfirmar;
    private javax.swing.JPasswordField pwdPassword;
    private javax.swing.JTextField txtFullName;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
