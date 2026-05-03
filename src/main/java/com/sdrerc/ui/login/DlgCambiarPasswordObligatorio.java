package com.sdrerc.ui.login;

import com.sdrerc.application.UserService;
import com.sdrerc.domain.model.User;
import com.sdrerc.ui.common.icon.IconUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class DlgCambiarPasswordObligatorio extends JDialog {

    private final User user;
    private final UserService userService;
    private final JPasswordField pwdActual = new JPasswordField();
    private final JPasswordField pwdNueva = new JPasswordField();
    private final JPasswordField pwdConfirmar = new JPasswordField();
    private final JCheckBox chkMostrar = new JCheckBox("Mostrar contraseñas");
    private final javax.swing.JButton btnCambiar = new javax.swing.JButton("Cambiar contraseña");
    private final javax.swing.JButton btnCancelar = new javax.swing.JButton("Cancelar");
    private boolean passwordChanged;
    private char echoChar;

    public DlgCambiarPasswordObligatorio(Window parent, User user, UserService userService) {
        super(parent, "Cambiar contraseña", ModalityType.APPLICATION_MODAL);
        this.user = user;
        this.userService = userService;
        configurarDialogo();
    }

    public boolean isPasswordChanged() {
        return passwordChanged;
    }

    private void configurarDialogo() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        echoChar = pwdActual.getEchoChar();

        btnCambiar.setIcon(IconUtils.load("key.svg", 16));
        btnCancelar.setIcon(IconUtils.load("clear.svg", 16));
        btnCambiar.setIconTextGap(8);
        btnCancelar.setIconTextGap(8);

        chkMostrar.setOpaque(false);
        chkMostrar.addActionListener(e -> actualizarVisibilidad());
        btnCambiar.addActionListener(e -> cambiarPassword());
        btnCancelar.addActionListener(e -> dispose());

        setContentPane(crearContenido());
        pack();
        setMinimumSize(new Dimension(640, 520));
        setSize(new Dimension(640, 520));
        setLocationRelativeTo(getOwner());
    }

    private JPanel crearContenido() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        root.setBackground(new Color(245, 247, 250));
        root.add(crearHeader(), BorderLayout.NORTH);
        root.add(crearFormulario(), BorderLayout.CENTER);
        root.add(crearBotones(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel crearHeader() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel info = crearCard();
        info.setLayout(new BorderLayout(0, 5));
        JLabel titulo = new JLabel("Cambiar contraseña");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        titulo.setForeground(new Color(25, 42, 62));
        JLabel subtitulo = new JLabel("Debe actualizar su contraseña para continuar.");
        subtitulo.setForeground(new Color(73, 85, 99));
        JLabel reglas = new JLabel("<html><body style='width:540px'>La nueva contraseña debe tener mínimo 8 caracteres, mayúscula, minúscula, número y carácter especial.</body></html>");
        reglas.setForeground(new Color(93, 105, 119));
        info.add(titulo, BorderLayout.NORTH);
        info.add(subtitulo, BorderLayout.CENTER);
        info.add(reglas, BorderLayout.SOUTH);

        JPanel contexto = crearCard();
        contexto.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 4, 10);
        agregarDato(contexto, gbc, 0, "Usuario:", texto(user.getUsername()));
        agregarDato(contexto, gbc, 1, "Nombre:", texto(user.getFullName()));

        panel.add(info, BorderLayout.NORTH);
        panel.add(contexto, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormulario() {
        JPanel panel = crearCard();
        panel.setLayout(new GridBagLayout());

        Dimension fieldSize = new Dimension(280, 34);
        pwdActual.setPreferredSize(fieldSize);
        pwdNueva.setPreferredSize(fieldSize);
        pwdConfirmar.setPreferredSize(fieldSize);
        pwdActual.setToolTipText("Ingrese la contraseña temporal o actual.");
        pwdNueva.setToolTipText("Ingrese la nueva contraseña.");
        pwdConfirmar.setToolTipText("Repita la nueva contraseña.");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 12, 12);
        gbc.gridy = 0;
        agregarFila(panel, gbc, "Contraseña actual", pwdActual);
        agregarFila(panel, gbc, "Nueva contraseña", pwdNueva);
        agregarFila(panel, gbc, "Confirmar nueva contraseña", pwdConfirmar);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(chkMostrar, gbc);
        return panel;
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);
        btnCambiar.setPreferredSize(new Dimension(180, 36));
        btnCancelar.setPreferredSize(new Dimension(110, 36));
        panel.add(btnCambiar);
        panel.add(btnCancelar);
        return panel;
    }

    private JPanel crearCard() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        return panel;
    }

    private void agregarDato(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
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
        panel.add(new JLabel(value), gbc);
    }

    private void agregarFila(JPanel panel, GridBagConstraints gbc, String labelText, JPasswordField field) {
        JLabel label = new JLabel(labelText);
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

    private void actualizarVisibilidad() {
        char echo = chkMostrar.isSelected() ? (char) 0 : echoChar;
        pwdActual.setEchoChar(echo);
        pwdNueva.setEchoChar(echo);
        pwdConfirmar.setEchoChar(echo);
    }

    private void cambiarPassword() {
        try {
            userService.cambiarPasswordObligatorio(
                    user.getUserId(),
                    user.getUsername(),
                    new String(pwdActual.getPassword()),
                    new String(pwdNueva.getPassword()),
                    new String(pwdConfirmar.getPassword())
            );
            passwordChanged = true;
            JOptionPane.showMessageDialog(
                    this,
                    "Contraseña actualizada correctamente.",
                    "Contraseña",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar la contraseña.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String texto(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }
}
