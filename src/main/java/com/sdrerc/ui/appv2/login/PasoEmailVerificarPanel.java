package com.sdrerc.ui.appv2.login;

import com.sdrerc.ui.appv2.components.AppV2IconProvider;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Paso de verificación por correo: primera opción de 2FA en el login. Muestra el correo
 * enmascarado al que se envió el código de 6 dígitos, con opción de reenviar (con cooldown) o de
 * cambiar a la app autenticadora (TOTP) como método alterno.
 */
public class PasoEmailVerificarPanel extends JPanel {

    public interface Listener {
        void onConfirmarCodigo(String codigo);

        void onReenviarCodigo();

        void onUsarAutenticador();

        void onCancelar();
    }

    private static final int COOLDOWN_SEGUNDOS = 45;

    private final JTextField txtCodigo = new JTextField();
    private final JLabel lblCorreo = new JLabel(" ");
    private final JLabel lblReenviar = new JLabel("Reenviar código");
    private Timer timerCooldown;
    private int segundosRestantes;

    public PasoEmailVerificarPanel(Listener listener) {
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        JLabel titulo = new JLabel("Verificación en dos pasos");
        titulo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        titulo.setForeground(AppV2Theme.TEXT_PRIMARY);
        lblCorreo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblCorreo.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        GridBagConstraints gbcH = new GridBagConstraints();
        gbcH.gridx = 0;
        gbcH.gridy = 0;
        gbcH.anchor = GridBagConstraints.WEST;
        gbcH.insets = new Insets(0, 0, 4, 0);
        header.add(titulo, gbcH);
        gbcH.gridy = 1;
        header.add(lblCorreo, gbcH);

        txtCodigo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_MEDIUM));
        txtCodigo.setHorizontalAlignment(SwingConstants.CENTER);
        txtCodigo.setPreferredSize(new Dimension(200, 40));
        txtCodigo.addActionListener(e -> {
            if (listener != null) {
                listener.onConfirmarCodigo(txtCodigo.getText().trim());
            }
        });

        lblReenviar.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL).deriveFont(Font.BOLD));
        lblReenviar.setForeground(AppV2Theme.PRIMARY);
        lblReenviar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblReenviar.setHorizontalAlignment(SwingConstants.CENTER);
        lblReenviar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (lblReenviar.isEnabled() && listener != null) {
                    listener.onReenviarCodigo();
                    iniciarCooldown();
                }
            }
        });

        JLabel lblUsarAutenticador = new JLabel("Prefiero usar una app autenticadora");
        lblUsarAutenticador.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblUsarAutenticador.setForeground(AppV2Theme.TEXT_SECONDARY);
        lblUsarAutenticador.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblUsarAutenticador.setHorizontalAlignment(SwingConstants.CENTER);
        lblUsarAutenticador.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listener != null) {
                    listener.onUsarAutenticador();
                }
            }
        });

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 10, 0);
        formulario.add(txtCodigo, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        formulario.add(lblReenviar, gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(6, 0, 0, 0);
        formulario.add(lblUsarAutenticador, gbc);

        JButton btnConfirmar = new JButton("Verificar e ingresar");
        JButton btnCancelar = new JButton("Cancelar");
        btnConfirmar.setIcon(AppV2IconProvider.action(AppV2IconProvider.LOCK));
        btnConfirmar.setIconTextGap(8);
        AppV2Theme.estilizarBotonPrimario(btnConfirmar);
        AppV2Theme.estilizarBotonSecundario(btnCancelar);
        btnConfirmar.setPreferredSize(new Dimension(190, 38));
        btnCancelar.setPreferredSize(new Dimension(110, 38));
        btnConfirmar.addActionListener(e -> {
            if (listener != null) {
                listener.onConfirmarCodigo(txtCodigo.getText().trim());
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

    /** Se llama cada vez que se envía o reenvía un código nuevo, para reflejar el correo destino. */
    public void mostrarCorreoEnviado(String correoEnmascarado) {
        lblCorreo.setText("Enviamos un código de 6 dígitos a " + correoEnmascarado);
        iniciarCooldown();
    }

    public void reset() {
        txtCodigo.setText("");
    }

    public void enfocar() {
        txtCodigo.requestFocusInWindow();
    }

    private void iniciarCooldown() {
        segundosRestantes = COOLDOWN_SEGUNDOS;
        lblReenviar.setEnabled(false);
        actualizarTextoReenviar();
        if (timerCooldown != null) {
            timerCooldown.stop();
        }
        timerCooldown = new Timer(1000, e -> {
            segundosRestantes--;
            if (segundosRestantes <= 0) {
                timerCooldown.stop();
                lblReenviar.setEnabled(true);
                lblReenviar.setText("Reenviar código");
            } else {
                actualizarTextoReenviar();
            }
        });
        timerCooldown.setRepeats(true);
        timerCooldown.start();
    }

    private void actualizarTextoReenviar() {
        lblReenviar.setText("Reenviar código (" + segundosRestantes + "s)");
    }
}
