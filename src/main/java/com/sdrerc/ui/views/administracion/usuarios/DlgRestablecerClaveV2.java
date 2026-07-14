package com.sdrerc.ui.views.administracion.usuarios;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.security.SecureRandom;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 * Diálogo premium para Administración &gt; Usuarios &gt; "Restablecer clave": el administrador
 * fija una contraseña temporal (o la genera aleatoriamente) para el usuario seleccionado, con la
 * opción de reiniciar también su verificación en dos pasos (cuando perdió el dispositivo
 * autenticador). Estilo {@code AppV2Theme}, mismo patrón visual de {@code DlgCambiarPasswordObligatorio}.
 */
public class DlgRestablecerClaveV2 extends JDialog {

    private static final String ALFABETO_ALEATORIO =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private final JPasswordField txtPassword = new JPasswordField();
    private final JCheckBox chkMostrar = new JCheckBox("Mostrar");
    private final JCheckBox chkReiniciarTotp =
            new JCheckBox("También reiniciar verificación en dos pasos (el usuario deberá vincular su app autenticadora nuevamente)");
    private final JButton btnGenerar = new JButton("Generar aleatoria");
    private final JButton btnGuardar = new JButton("Restablecer clave");
    private final JButton btnCancelar = new JButton("Cancelar");
    private boolean confirmado;
    private char echoChar;

    public DlgRestablecerClaveV2(Window parent, String username) {
        super(parent, "Restablecer clave", ModalityType.APPLICATION_MODAL);
        configurarDialogo(username);
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public String getPasswordTemporal() {
        return new String(txtPassword.getPassword());
    }

    public boolean isReiniciarTotp() {
        return chkReiniciarTotp.isSelected();
    }

    private void configurarDialogo(String username) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        echoChar = txtPassword.getEchoChar();

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(new Color(245, 247, 250));
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel header = crearCard();
        header.setLayout(new BorderLayout(0, 4));
        JLabel titulo = new JLabel("Restablecer clave de " + (username == null ? "usuario" : username));
        titulo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        titulo.setForeground(AppV2Theme.TEXT_PRIMARY);
        JLabel subtitulo = new JLabel(
                "<html><body style='width:420px'>El usuario deberá cambiar esta contraseña temporal en su "
                        + "próximo ingreso.</body></html>");
        subtitulo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        subtitulo.setForeground(AppV2Theme.TEXT_SECONDARY);
        header.add(titulo, BorderLayout.NORTH);
        header.add(subtitulo, BorderLayout.CENTER);

        JPanel formulario = crearCard();
        formulario.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lbl = new JLabel("Contraseña temporal");
        lbl.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lbl.setForeground(AppV2Theme.TEXT_SECONDARY);
        formulario.add(lbl, gbc);

        gbc.gridy = 1;
        txtPassword.setPreferredSize(new Dimension(240, 34));
        txtPassword.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        formulario.add(txtPassword, gbc);

        gbc.gridx = 1;
        AppV2Theme.estilizarBotonSecundario(btnGenerar);
        btnGenerar.addActionListener(e -> generarAleatoria());
        formulario.add(btnGenerar, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        chkMostrar.setOpaque(false);
        chkMostrar.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        chkMostrar.addActionListener(e -> actualizarVisibilidad());
        formulario.add(chkMostrar, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(6, 0, 0, 10);
        chkReiniciarTotp.setOpaque(false);
        chkReiniciarTotp.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        formulario.add(chkReiniciarTotp, gbc);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        btnGuardar.setPreferredSize(new Dimension(180, 36));
        btnCancelar.setPreferredSize(new Dimension(110, 36));
        AppV2Theme.estilizarBotonPrimario(btnGuardar);
        AppV2Theme.estilizarBotonSecundario(btnCancelar);
        btnGuardar.addActionListener(e -> {
            confirmado = true;
            dispose();
        });
        btnCancelar.addActionListener(e -> dispose());
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        root.add(header, BorderLayout.NORTH);
        root.add(formulario, BorderLayout.CENTER);
        root.add(botones, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setMinimumSize(new Dimension(520, 320));
        setLocationRelativeTo(getOwner());
    }

    private JPanel crearCard() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return panel;
    }

    private void generarAleatoria() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(ALFABETO_ALEATORIO.charAt(random.nextInt(ALFABETO_ALEATORIO.length())));
        }
        txtPassword.setText(sb.toString());
        chkMostrar.setSelected(true);
        actualizarVisibilidad();
    }

    private void actualizarVisibilidad() {
        txtPassword.setEchoChar(chkMostrar.isSelected() ? (char) 0 : echoChar);
    }
}
