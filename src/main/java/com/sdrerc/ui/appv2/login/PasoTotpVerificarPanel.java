package com.sdrerc.ui.appv2.login;

import com.sdrerc.ui.appv2.components.AppV2IconProvider;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Paso de verificación TOTP en logins posteriores al enrolamiento: un único campo acepta tanto el
 * código de 6 dígitos de la app autenticadora como un código de respaldo (formato XXXX-XXXX);
 * {@code AutenticacionService.validarCodigoTotp} intenta ambos.
 */
public class PasoTotpVerificarPanel extends JPanel {

    public interface Listener {
        void onConfirmarCodigo(String codigo);

        void onCancelar();
    }

    private final JTextField txtCodigo = new JTextField();

    public PasoTotpVerificarPanel(Listener listener) {
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        JLabel titulo = new JLabel("Verificación en dos pasos");
        titulo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        titulo.setForeground(AppV2Theme.TEXT_PRIMARY);
        JLabel subtitulo = new JLabel(
                "<html><body style='width:280px'>Ingrese el código de 6 dígitos de su app autenticadora, o "
                        + "un código de respaldo si no tiene acceso a ella.</body></html>");
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

        txtCodigo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_MEDIUM));
        txtCodigo.setHorizontalAlignment(SwingConstants.CENTER);
        txtCodigo.setPreferredSize(new Dimension(200, 40));
        txtCodigo.addActionListener(e -> {
            if (listener != null) {
                listener.onConfirmarCodigo(txtCodigo.getText().trim());
            }
        });
        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        formulario.setOpaque(false);
        formulario.add(txtCodigo);

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

    public void reset() {
        txtCodigo.setText("");
    }

    public void enfocar() {
        txtCodigo.requestFocusInWindow();
    }
}
