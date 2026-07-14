package com.sdrerc.ui.appv2.login;

import com.sdrerc.ui.appv2.components.AppV2IconProvider;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;

/**
 * Paso "cambio de contraseña obligatorio" del login premium, mostrado cuando el administrador
 * asignó una contraseña temporal (Administración &gt; Usuarios &gt; "Restablecer clave"). Estilo
 * analogo a {@code DlgCambiarPasswordObligatorio} legacy, pero con {@code AppV2Theme}.
 */
public class PasoCambioPasswordPanel extends JPanel {

    public interface Listener {
        void onConfirmar(String nuevaPassword, String confirmarPassword);

        void onCancelar();
    }

    private final JPasswordField txtNueva = new JPasswordField();
    private final JPasswordField txtConfirmar = new JPasswordField();
    private final JCheckBox chkMostrar = new JCheckBox("Mostrar contraseña");
    private final JButton btnConfirmar = new JButton("Continuar");
    private final JButton btnCancelar = new JButton("Cancelar");
    private char echoChar;

    public PasoCambioPasswordPanel(Listener listener) {
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        JLabel titulo = new JLabel("Debe cambiar su contraseña", SwingConstants.LEFT);
        titulo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        titulo.setForeground(AppV2Theme.TEXT_PRIMARY);

        JLabel subtitulo = new JLabel(
                "<html><body style='width:280px'>Por seguridad, active una contraseña propia antes de continuar. "
                        + "Mínimo 8 caracteres, combinando letras y números.</body></html>");
        subtitulo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        subtitulo.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        GridBagConstraints gbcH = new GridBagConstraints();
        gbcH.gridx = 0;
        gbcH.gridy = 0;
        gbcH.anchor = GridBagConstraints.WEST;
        gbcH.insets = new Insets(0, 0, 4, 0);
        header.add(titulo, gbcH);
        gbcH.gridy = 1;
        header.add(subtitulo, gbcH);

        echoChar = txtNueva.getEchoChar();
        Dimension fieldSize = new Dimension(340, 34);
        txtNueva.setPreferredSize(fieldSize);
        txtConfirmar.setPreferredSize(fieldSize);
        txtNueva.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        txtConfirmar.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formulario.add(campoConLabel("Nueva contraseña", txtNueva), gbc);
        gbc.gridy = 1;
        formulario.add(campoConLabel("Confirmar nueva contraseña", txtConfirmar), gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        chkMostrar.setOpaque(false);
        chkMostrar.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        chkMostrar.addActionListener(e -> actualizarVisibilidad());
        formulario.add(chkMostrar, gbc);

        btnConfirmar.setIcon(AppV2IconProvider.action(AppV2IconProvider.LOCK));
        btnConfirmar.setIconTextGap(8);
        AppV2Theme.estilizarBotonPrimario(btnConfirmar);
        AppV2Theme.estilizarBotonSecundario(btnCancelar);
        btnConfirmar.setPreferredSize(new Dimension(180, 38));
        btnCancelar.setPreferredSize(new Dimension(110, 38));
        btnConfirmar.addActionListener(e -> {
            if (listener != null) {
                listener.onConfirmar(new String(txtNueva.getPassword()), new String(txtConfirmar.getPassword()));
            }
        });
        btnCancelar.addActionListener(e -> {
            if (listener != null) {
                listener.onCancelar();
            }
        });

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        botones.add(btnCancelar);
        botones.add(btnConfirmar);

        add(header, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
    }

    public void limpiar() {
        txtNueva.setText("");
        txtConfirmar.setText("");
        chkMostrar.setSelected(false);
        actualizarVisibilidad();
    }

    private JPanel campoConLabel(String texto, JPasswordField campo) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel label = new JLabel(texto);
        label.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        label.setForeground(AppV2Theme.TEXT_SECONDARY);
        panel.add(label, BorderLayout.NORTH);
        panel.add(campo, BorderLayout.CENTER);
        return panel;
    }

    private void actualizarVisibilidad() {
        char echo = chkMostrar.isSelected() ? (char) 0 : echoChar;
        txtNueva.setEchoChar(echo);
        txtConfirmar.setEchoChar(echo);
    }
}
