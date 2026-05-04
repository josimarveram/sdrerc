/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.sdrerc.ui.login;

import com.sdrerc.application.LoginService;
import com.sdrerc.application.UserService;
import com.sdrerc.domain.model.User;
import com.sdrerc.infrastructure.security.PasswordEncoder;
import com.sdrerc.shared.session.SessionContext;
import com.sdrerc.ui.common.icon.IconUtils;
import com.sdrerc.ui.common.FrmPrincipal;
import com.sdrerc.ui.menu.MenuPrincipal;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 * @author David
 */
public class FrmLogin extends javax.swing.JFrame 
{
    int xMouse, yMouse;
    private JButton btnTogglePassword;
    private char passwordEchoCharOriginal;
    private boolean passwordVisible;
    
    public FrmLogin() {
        initComponents();
        configurarLoginPremium();
        configurarCampoPasswordConOjito();
        configurarFocoCamposLogin();
        setLocationRelativeTo(null);
        
    }

    private void configurarLoginPremium() {
        bg.setBackground(Color.WHITE);
        text_usuario.setToolTipText("Ingrese su usuario.");
        txt_contraseña.setToolTipText("Ingrese su contraseña.");

        javax.swing.JPanel loginCard = new javax.swing.JPanel();
        loginCard.setOpaque(false);
        loginCard.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        loginCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        bg.add(loginCard, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 95, 590, 390));
        bg.setComponentZOrder(loginCard, bg.getComponentCount() - 1);

        lbl_sdrerc.setFont(new Font("Roboto Black", Font.BOLD, 20));
        lbl_inicioSesion.setFont(new Font("Roboto Black", Font.BOLD, 22));
        lbl_inicioSesion.setText("Iniciar sesión");
        lbl_usuario1.setText("USUARIO");
        lbl_usuario.setText("CONTRASEÑA");

        JLabel subtitulo = new JLabel("Acceso seguro al sistema SDRERC");
        subtitulo.setFont(new Font("Roboto", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(100, 116, 139));
        bg.add(subtitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 215, 360, 24));

        text_usuario.setFont(new Font("Roboto", Font.PLAIN, 15));
        txt_contraseña.setFont(new Font("Roboto", Font.PLAIN, 15));
        text_usuario.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        txt_contraseña.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        aplicarCursoresLogin();

        setAbsoluteBounds(lbl_usuario1, 40, 248, 120, 22);
        setAbsoluteBounds(text_usuario, 40, 276, 520, 28);
        setAbsoluteBounds(separador_usuario, 40, 308, 520, 10);
        setAbsoluteBounds(lbl_usuario, 40, 326, 130, 22);
        setAbsoluteBounds(txt_contraseña, 40, 354, 485, 28);
        setAbsoluteBounds(separador_contraseña, 40, 386, 540, 10);

        lpn_btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lpn_btnLogin.putClientProperty("JComponent.arc", 14);
        lbl_btnEntrar.setText("Ingresar");
        lbl_btnEntrar.setHorizontalAlignment(SwingConstants.CENTER);
        setAbsoluteBounds(lpn_btnLogin, 40, 420, 250, 54);
        setAbsoluteBounds(lbl_btnEntrar, 0, 0, 250, 54);

        bg.revalidate();
        bg.repaint();
    }

    private void aplicarCursoresLogin() {
        Cursor cursorDefault = new Cursor(Cursor.DEFAULT_CURSOR);
        Cursor cursorTexto = new Cursor(Cursor.TEXT_CURSOR);
        lbl_sdrerc.setCursor(cursorDefault);
        lbl_inicioSesion.setCursor(cursorDefault);
        lbl_usuario1.setCursor(cursorDefault);
        lbl_usuario.setCursor(cursorDefault);
        separador_usuario.setCursor(cursorDefault);
        separador_contraseña.setCursor(cursorDefault);
        text_usuario.setCursor(cursorTexto);
        txt_contraseña.setCursor(cursorTexto);
    }

    private void configurarCampoPasswordConOjito() {
        passwordEchoCharOriginal = txt_contraseña.getEchoChar();
        btnTogglePassword = new JButton();
        btnTogglePassword.setToolTipText("Mostrar contraseña");
        btnTogglePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTogglePassword.setFocusable(false);
        btnTogglePassword.setBorder(BorderFactory.createEmptyBorder());
        btnTogglePassword.setContentAreaFilled(false);
        btnTogglePassword.setOpaque(false);
        btnTogglePassword.setMargin(new Insets(0, 0, 0, 0));
        btnTogglePassword.setIcon(IconUtils.load("eye.svg", 18));
        btnTogglePassword.addActionListener(e -> alternarVisibilidadPassword());
        bg.add(btnTogglePassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(535, 348, 38, 38));
        bg.setComponentZOrder(btnTogglePassword, 0);
        bg.revalidate();
        bg.repaint();
    }

    private void alternarVisibilidadPassword() {
        passwordVisible = !passwordVisible;
        txt_contraseña.setEchoChar(passwordVisible ? (char) 0 : passwordEchoCharOriginal);
        btnTogglePassword.setToolTipText(passwordVisible ? "Ocultar contraseña" : "Mostrar contraseña");
        Icon icon = IconUtils.load(passwordVisible ? "eye-off.svg" : "eye.svg", 18);
        if (icon != null) {
            btnTogglePassword.setIcon(icon);
        }
    }

    private void configurarFocoCamposLogin() {
        text_usuario.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if ("Ingrese su nombre de usuario".equals(text_usuario.getText())) {
                    text_usuario.setText("");
                    text_usuario.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (text_usuario.getText().trim().isEmpty()) {
                    text_usuario.setText("Ingrese su nombre de usuario");
                    text_usuario.setForeground(Color.GRAY);
                }
            }
        });

        txt_contraseña.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (esPlaceholderPassword()) {
                    txt_contraseña.setText("");
                    txt_contraseña.setForeground(Color.BLACK);
                    txt_contraseña.setCaretPosition(0);
                } else {
                    txt_contraseña.selectAll();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txt_contraseña.getPassword().length == 0) {
                    passwordVisible = false;
                    txt_contraseña.setEchoChar(passwordEchoCharOriginal);
                    txt_contraseña.setText("************");
                    txt_contraseña.setForeground(Color.GRAY);
                    if (btnTogglePassword != null) {
                        btnTogglePassword.setToolTipText("Mostrar contraseña");
                        Icon icon = IconUtils.load("eye.svg", 18);
                        if (icon != null) {
                            btnTogglePassword.setIcon(icon);
                        }
                    }
                }
            }
        });
    }

    private boolean esPlaceholderPassword() {
        return "************".equals(new String(txt_contraseña.getPassword()));
    }

    private void setAbsoluteBounds(java.awt.Component component, int x, int y, int width, int height) {
        java.awt.Container parent = component.getParent();
        if (parent != null && parent.getLayout() instanceof org.netbeans.lib.awtextra.AbsoluteLayout) {
            parent.remove(component);
            parent.add(component, new org.netbeans.lib.awtextra.AbsoluteConstraints(x, y, width, height));
        }
        component.setBounds(x, y, width, height);
        component.setPreferredSize(new Dimension(width, height));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bg = new javax.swing.JPanel();
        lbl_imagen_logo = new javax.swing.JLabel();
        lbl_reniec = new javax.swing.JLabel();
        lbl_imagenFondo = new javax.swing.JLabel();
        lbl_sdrerc = new javax.swing.JLabel();
        lbl_inicioSesion = new javax.swing.JLabel();
        lbl_usuario = new javax.swing.JLabel();
        text_usuario = new javax.swing.JTextField();
        separador_contraseña = new javax.swing.JSeparator();
        lbl_usuario1 = new javax.swing.JLabel();
        txt_contraseña = new javax.swing.JPasswordField();
        separador_usuario = new javax.swing.JSeparator();
        lpn_btnLogin = new javax.swing.JPanel();
        lbl_btnEntrar = new javax.swing.JLabel();
        header = new javax.swing.JPanel();
        pnl_Exit = new javax.swing.JPanel();
        lblCerrar = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        setUndecorated(true);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        bg.setBackground(new java.awt.Color(255, 255, 255));
        bg.setCursor(new java.awt.Cursor(java.awt.Cursor.MOVE_CURSOR));
        bg.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lbl_imagen_logo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_imagen_logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sdrerc/ui/imagenes/logo.png"))); // NOI18N
        bg.add(lbl_imagen_logo, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 200, 120, 90));

        lbl_reniec.setBackground(new java.awt.Color(255, 255, 255));
        lbl_reniec.setFont(new java.awt.Font("Roboto Black", 0, 18)); // NOI18N
        lbl_reniec.setForeground(new java.awt.Color(255, 255, 255));
        lbl_reniec.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_reniec.setText("RENIEC");
        bg.add(lbl_reniec, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 330, 190, -1));

        lbl_imagenFondo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sdrerc/ui/imagenes/city.png"))); // NOI18N
        lbl_imagenFondo.setText("jLabel1");
        bg.add(lbl_imagenFondo, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 0, 210, 500));

        lbl_sdrerc.setBackground(new java.awt.Color(255, 255, 255));
        lbl_sdrerc.setFont(new java.awt.Font("Roboto Black", 0, 18)); // NOI18N
        lbl_sdrerc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sdrerc/ui/imagenes/favicon.png"))); // NOI18N
        lbl_sdrerc.setText("SDRERC");
        bg.add(lbl_sdrerc, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 110, 160, -1));

        lbl_inicioSesion.setBackground(new java.awt.Color(255, 255, 255));
        lbl_inicioSesion.setFont(new java.awt.Font("Roboto Black", 0, 18)); // NOI18N
        lbl_inicioSesion.setText("INICIAR SESIÓN");
        bg.add(lbl_inicioSesion, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 180, 280, -1));

        lbl_usuario.setBackground(new java.awt.Color(255, 255, 255));
        lbl_usuario.setFont(new java.awt.Font("Roboto Black", 0, 12)); // NOI18N
        lbl_usuario.setText("Contraseña:");
        bg.add(lbl_usuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 330, 70, 20));

        text_usuario.setFont(new java.awt.Font("Roboto", 0, 14)); // NOI18N
        text_usuario.setForeground(new java.awt.Color(204, 204, 204));
        text_usuario.setText("Ingrese su nombre de usuario");
        text_usuario.setBorder(null);
        text_usuario.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                text_usuarioMousePressed(evt);
            }
        });
        bg.add(text_usuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 280, 520, -1));
        bg.add(separador_contraseña, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 380, 540, 10));

        lbl_usuario1.setBackground(new java.awt.Color(255, 255, 255));
        lbl_usuario1.setFont(new java.awt.Font("Roboto Black", 0, 12)); // NOI18N
        lbl_usuario1.setText("USUARIO:");
        bg.add(lbl_usuario1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 250, 70, 20));

        txt_contraseña.setFont(new java.awt.Font("Roboto", 0, 14)); // NOI18N
        txt_contraseña.setForeground(new java.awt.Color(204, 204, 204));
        txt_contraseña.setText("************");
        txt_contraseña.setBorder(null);
        txt_contraseña.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                txt_contraseñaMousePressed(evt);
            }
        });
        bg.add(txt_contraseña, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 367, 520, 10));
        bg.add(separador_usuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 300, 520, 10));

        lpn_btnLogin.setBackground(new java.awt.Color(0, 135, 190));
        lpn_btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lpn_btnLoginMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lpn_btnLoginMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lpn_btnLoginMouseExited(evt);
            }
        });
        lpn_btnLogin.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lbl_btnEntrar.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        lbl_btnEntrar.setForeground(new java.awt.Color(255, 255, 255));
        lbl_btnEntrar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_btnEntrar.setText("ENTRAR");
        lpn_btnLogin.add(lbl_btnEntrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 20, -1, -1));

        bg.add(lpn_btnLogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 410, 250, 60));

        header.setBackground(new java.awt.Color(255, 255, 255));
        header.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                headerMouseDragged(evt);
            }
        });
        header.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                headerMousePressed(evt);
            }
        });
        header.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnl_Exit.setBackground(new java.awt.Color(255, 255, 255));
        pnl_Exit.setPreferredSize(new java.awt.Dimension(500, 500));
        pnl_Exit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnl_ExitMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnl_ExitMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnl_ExitMouseExited(evt);
            }
        });
        pnl_Exit.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblCerrar.setFont(new java.awt.Font("Roboto Black", 0, 14)); // NOI18N
        lblCerrar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCerrar.setText("X");
        pnl_Exit.add(lblCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 20, 20));

        header.add(pnl_Exit, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 40, 40));

        bg.add(header, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 850, 40));

        getContentPane().add(bg, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 850, 500));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void headerMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_headerMousePressed
        xMouse = evt.getX();
        yMouse = evt.getY();
    }//GEN-LAST:event_headerMousePressed

    private void headerMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_headerMouseDragged
        
        int x = evt.getXOnScreen();
        int y = evt.getYOnScreen();        
        this.setLocation(x - xMouse, y - yMouse);
    }//GEN-LAST:event_headerMouseDragged

    private void pnl_ExitMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnl_ExitMouseClicked
        System.exit(0);
    }//GEN-LAST:event_pnl_ExitMouseClicked

    private void pnl_ExitMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnl_ExitMouseEntered
       
       pnl_Exit.setBackground(Color.RED);
       pnl_Exit.setForeground(Color.white);
        
    }//GEN-LAST:event_pnl_ExitMouseEntered

    private void pnl_ExitMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnl_ExitMouseExited
        pnl_Exit.setBackground(Color.white);
        pnl_Exit.setForeground(Color.red);
    }//GEN-LAST:event_pnl_ExitMouseExited

    private void lpn_btnLoginMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lpn_btnLoginMouseEntered
        lpn_btnLogin.setBackground(new Color(0,155,225));
    }//GEN-LAST:event_lpn_btnLoginMouseEntered

    private void lpn_btnLoginMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lpn_btnLoginMouseExited
        lpn_btnLogin.setBackground(new Color(0,135,190));
    }//GEN-LAST:event_lpn_btnLoginMouseExited

    private void text_usuarioMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_text_usuarioMousePressed
       
        if(text_usuario.getText().equals("Ingrese su nombre de usuario"))
        {
            text_usuario.setText("");
            text_usuario.setForeground(Color.black);
        }
        if(String.valueOf(txt_contraseña.getPassword()).isEmpty())
        {
            txt_contraseña.setText("************");
            text_usuario.setForeground(Color.gray);
        }               
    }//GEN-LAST:event_text_usuarioMousePressed

    private void txt_contraseñaMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_contraseñaMousePressed
        
        if(String.valueOf(txt_contraseña.getPassword()).equals("************"))
        {
            txt_contraseña.setText("");
            text_usuario.setForeground(Color.black);
        } 
        if(text_usuario.getText().isEmpty())
        {
            text_usuario.setText("Ingrese su nombre de usuario");
            text_usuario.setForeground(Color.gray);
        }
    }//GEN-LAST:event_txt_contraseñaMousePressed

    private void lpn_btnLoginMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lpn_btnLoginMouseClicked
        // javax.swing.JOptionPane.showMessageDialog(this,"Intento logears");
        
        String username = text_usuario.getText();
        String password = new String(txt_contraseña.getPassword());

        try {
             User u = new LoginService().login(username, password);

            if (u.isMustChangePassword()) {
                DlgCambiarPasswordObligatorio dlg = new DlgCambiarPasswordObligatorio(
                        this,
                        u,
                        new UserService()
                );
                dlg.setVisible(true);
                if (!dlg.isPasswordChanged()) {
                    SessionContext.limpiar();
                    txt_contraseña.setText("");
                    JOptionPane.showMessageDialog(
                            this,
                            "Debe cambiar su contraseña para ingresar al sistema.",
                            "Cambio de contraseña requerido",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                u.setMustChangePassword(false);
            }

             SessionContext.setUsuarioActual(u);
            
            /*
            JOptionPane.showMessageDialog(this,
                "Bienvenido " + u.getFullname() + "\nRol: " + u.getRole());
            */
            
            MenuPrincipal menu = new MenuPrincipal();
            menu.setLocationRelativeTo(null); // <-- Centra el JFrame en la pantalla
            menu.setVisible(true);

            // Cerrar ventana de login
            this.dispose();

        } catch (Exception ex) {
            // 4️⃣ Mostrar error si usuario o contraseña no coinciden
            JOptionPane.showMessageDialog(this,
                ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_lpn_btnLoginMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
        System.out.println(
            PasswordEncoder.hash("seg123")
        );
        */
        
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
            java.util.logging.Logger.getLogger(FrmLogin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmLogin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmLogin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmLogin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrmLogin().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bg;
    private javax.swing.JPanel header;
    private javax.swing.JLabel lblCerrar;
    private javax.swing.JLabel lbl_btnEntrar;
    private javax.swing.JLabel lbl_imagenFondo;
    private javax.swing.JLabel lbl_imagen_logo;
    private javax.swing.JLabel lbl_inicioSesion;
    private javax.swing.JLabel lbl_reniec;
    private javax.swing.JLabel lbl_sdrerc;
    private javax.swing.JLabel lbl_usuario;
    private javax.swing.JLabel lbl_usuario1;
    private javax.swing.JPanel lpn_btnLogin;
    private javax.swing.JPanel pnl_Exit;
    private javax.swing.JSeparator separador_contraseña;
    private javax.swing.JSeparator separador_usuario;
    private javax.swing.JTextField text_usuario;
    private javax.swing.JPasswordField txt_contraseña;
    // End of variables declaration//GEN-END:variables
}
