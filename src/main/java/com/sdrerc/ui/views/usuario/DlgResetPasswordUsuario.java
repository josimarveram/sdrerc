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
import com.sdrerc.infrastructure.security.PasswordGenerator;
import com.sdrerc.ui.common.icon.IconUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.Insets;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

/**
 *
 * @author David
 */
public class DlgResetPasswordUsuario extends javax.swing.JDialog {

    /**
     * Creates new form DlgEditarRol
     */
    
    private final Long userId;
    private final UserService userService;
    private final String username;
    private final String nombreVisible;
    private final String estado;
    private final JButton btnGenerarClave = new JButton("Generar clave");
    private final JCheckBox chkMostrarClave = new JCheckBox("Mostrar clave temporal");
    private char echoCharPassword;
    
    public DlgResetPasswordUsuario(Window parent, 
            ModalityType modalityType, 
            Long userId,
            UserService userService) {
        this(parent, modalityType, userId, "", "", "", userService);
    }

    public DlgResetPasswordUsuario(Window parent,
            ModalityType modalityType,
            Long userId,
            String username,
            String nombreVisible,
            String estado,
            UserService userService) {
        super(parent, modalityType);
        initComponents();
        
        this.userId = userId;
        this.userService = userService;
        this.username = username == null ? "" : username;
        this.nombreVisible = nombreVisible == null ? "" : nombreVisible;
        this.estado = estado == null ? "" : estado;
        configurarDialogoReset();

    }

    private void configurarDialogoReset() {
        setTitle("Resetear contraseña");
        lblPassword.setText("Nueva contraseña temporal");
        lblConfirmar.setText("Confirmar contraseña temporal");
        btnGuardar.setText("Guardar reset");
        btnCancelar.setText("Cancelar");

        pwdNueva.setToolTipText("El usuario deberá cambiar esta contraseña al iniciar sesión.");
        pwdConfirmar.setToolTipText("Repita la contraseña temporal.");
        chkMostrarClave.setToolTipText("Permite ver la contraseña temporal generada para comunicarla al usuario.");
        chkMostrarClave.setOpaque(false);
        echoCharPassword = pwdNueva.getEchoChar();

        aplicarIcono(btnGenerarClave, "key.svg");
        aplicarIcono(btnGuardar, "active.svg");
        aplicarIcono(btnCancelar, "clear.svg");

        btnGenerarClave.addActionListener(e -> generarClaveTemporal());
        chkMostrarClave.addActionListener(e -> actualizarVisibilidadClave());
        configurarCopiaClaveTemporal();
        reconstruirLayoutReset();
        pack();
        setMinimumSize(new Dimension(700, 520));
        setSize(new Dimension(700, 520));
        setLocationRelativeTo(getParent());
    }

    private void aplicarIcono(JButton button, String iconName) {
        javax.swing.Icon icon = IconUtils.load(iconName, 16);
        if (icon != null) {
            button.setIcon(icon);
            button.setIconTextGap(8);
        }
    }

    private void generarClaveTemporal() {
        String temporal = PasswordGenerator.generateTemporaryPassword(username);
        pwdNueva.setText(temporal);
        pwdConfirmar.setText(temporal);
        chkMostrarClave.setSelected(true);
        actualizarVisibilidadClave();
        pwdNueva.requestFocus();
        pwdNueva.selectAll();
    }

    private void actualizarVisibilidadClave() {
        char echo = chkMostrarClave.isSelected() ? (char) 0 : echoCharPassword;
        pwdNueva.setEchoChar(echo);
        pwdConfirmar.setEchoChar(echo);
    }

    private void configurarCopiaClaveTemporal() {
        pwdNueva.setToolTipText("El usuario deberá cambiar esta contraseña al iniciar sesión. Use Ctrl+C o clic derecho para copiar.");
        pwdConfirmar.setToolTipText("Repita la contraseña temporal. Use Ctrl+C o clic derecho para copiar.");

        JPopupMenu menuCopiar = new JPopupMenu();
        JMenuItem itemCopiar = new JMenuItem("Copiar contraseña temporal");
        javax.swing.Icon icon = IconUtils.load("key.svg", 16);
        if (icon != null) {
            itemCopiar.setIcon(icon);
        }
        itemCopiar.addActionListener(e -> copiarClaveTemporal());
        menuCopiar.add(itemCopiar);

        pwdNueva.setComponentPopupMenu(menuCopiar);
        pwdConfirmar.setComponentPopupMenu(menuCopiar);

        KeyStroke ctrlC = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
        Action copiarAction = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                copiarClaveTemporal();
            }
        };
        pwdNueva.getInputMap().put(ctrlC, "copiarClaveTemporal");
        pwdNueva.getActionMap().put("copiarClaveTemporal", copiarAction);
        pwdConfirmar.getInputMap().put(ctrlC, "copiarClaveTemporal");
        pwdConfirmar.getActionMap().put("copiarClaveTemporal", copiarAction);
    }

    private void copiarClaveTemporal() {
        String temporal = new String(pwdNueva.getPassword());
        if (temporal.isEmpty()) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(temporal), null);
    }

    private void reconstruirLayoutReset() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        root.setBackground(new Color(245, 247, 250));

        root.add(crearHeader(), BorderLayout.NORTH);
        root.add(crearFormulario(), BorderLayout.CENTER);
        root.add(crearBotones(), BorderLayout.SOUTH);

        setContentPane(root);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private JPanel crearHeader() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel info = new JPanel(new BorderLayout(0, 5));
        info.setBackground(Color.WHITE);
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel titulo = new JLabel("Resetear contraseña");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        titulo.setForeground(new Color(25, 42, 62));
        JLabel subtitulo = new JLabel("Define una contraseña temporal para recuperar el acceso del usuario.");
        subtitulo.setForeground(new Color(73, 85, 99));
        JLabel ayuda = new JLabel("<html><body style='width:560px'>El usuario deberá cambiar esta contraseña al iniciar sesión.</body></html>");
        ayuda.setForeground(new Color(93, 105, 119));

        info.add(titulo, BorderLayout.NORTH);
        info.add(subtitulo, BorderLayout.CENTER);
        info.add(ayuda, BorderLayout.SOUTH);

        JPanel contexto = new JPanel(new GridBagLayout());
        contexto.setBackground(Color.WHITE);
        contexto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 4, 10);
        agregarDatoContexto(contexto, gbc, 0, "Usuario:", textoSeguro(username));
        agregarDatoContexto(contexto, gbc, 1, "Nombre:", textoSeguro(nombreVisible));
        agregarDatoContexto(contexto, gbc, 2, "Estado:", textoSeguro(estado));

        panel.add(info, BorderLayout.NORTH);
        panel.add(contexto, BorderLayout.CENTER);
        return panel;
    }

    private void agregarDatoContexto(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel val = new JLabel(value);
        val.setForeground(new Color(55, 65, 81));
        panel.add(val, gbc);
    }

    private JPanel crearFormulario() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        Dimension fieldSize = new Dimension(280, 34);
        pwdNueva.setPreferredSize(fieldSize);
        pwdConfirmar.setPreferredSize(fieldSize);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 12, 12);
        gbc.gridy = 0;
        agregarFila(card, gbc, lblPassword, pwdNueva);
        agregarFila(card, gbc, lblConfirmar, pwdConfirmar);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        card.add(chkMostrarClave, gbc);
        return card;
    }

    private void agregarFila(JPanel panel, GridBagConstraints gbc, JLabel label, java.awt.Component field) {
        label.setPreferredSize(new Dimension(210, 28));
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
        gbc.gridy++;
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);
        btnGenerarClave.setPreferredSize(new Dimension(150, 36));
        btnGuardar.setPreferredSize(new Dimension(140, 36));
        btnCancelar.setPreferredSize(new Dimension(110, 36));
        panel.add(btnGenerarClave);
        panel.add(btnGuardar);
        panel.add(btnCancelar);
        return panel;
    }

    private String textoSeguro(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }
    
    private boolean validar() {

        String password = new String(pwdNueva.getPassword());

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese la contraseña temporal");
            return false;
        }

        if (!Arrays.equals(pwdNueva.getPassword(), pwdConfirmar.getPassword())) {
            JOptionPane.showMessageDialog(this,"Las contraseñas no coinciden");
            return false;
        }

        if (password.length() < 8) {
            JOptionPane.showMessageDialog(this,"La contraseña temporal debe tener al menos 8 caracteres");
            return false;
        }

        return true;
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
        lblPassword = new javax.swing.JLabel();
        lblConfirmar = new javax.swing.JLabel();
        pwdConfirmar = new javax.swing.JPasswordField();
        pwdNueva = new javax.swing.JPasswordField();

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

        lblPassword.setText("Contraseña:");

        lblConfirmar.setText("Confirmar contraseña:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblConfirmar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pwdConfirmar)
                    .addComponent(pwdNueva, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnCancelar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassword)
                    .addComponent(pwdNueva, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblConfirmar, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pwdConfirmar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar)
                    .addComponent(btnCancelar))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        
        
        if (!validar()) return;
        
        try {        
            String rawPassword = new String(pwdNueva.getPassword());
            userService.resetPasswordTemporal(userId, username, rawPassword);
            JOptionPane.showMessageDialog(
                    this,
                    "La contraseña temporal fue registrada correctamente. El usuario deberá cambiarla al iniciar sesión.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Política de contraseña",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo resetear la contraseña.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
        
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
            java.util.logging.Logger.getLogger(DlgResetPasswordUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DlgResetPasswordUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DlgResetPasswordUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DlgResetPasswordUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JLabel lblConfirmar;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JPasswordField pwdConfirmar;
    private javax.swing.JPasswordField pwdNueva;
    // End of variables declaration//GEN-END:variables
}
