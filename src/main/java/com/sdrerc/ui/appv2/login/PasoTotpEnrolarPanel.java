package com.sdrerc.ui.appv2.login;

import com.sdrerc.ui.appv2.components.AppV2IconProvider;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Paso de enrolamiento TOTP: primero muestra el QR (+ secreto manual) para vincular la app
 * autenticadora y pide el código de 6 dígitos para confirmar; al confirmar, muestra los códigos
 * de respaldo de un solo uso (se muestran una única vez, nunca se vuelven a exponer).
 *
 * <p>Las dos vistas (QR y respaldo) tienen alturas muy distintas, así que se alternan
 * reemplazando el contenido de {@code panelCentral} (no con {@code CardLayout}, que reservaría
 * siempre el alto de la vista más alta de las dos, dejando un hueco vacío en la más chica).</p>
 */
public class PasoTotpEnrolarPanel extends JPanel {

    public interface Listener {
        void onConfirmarCodigo(String codigo);

        void onFinalizar();

        void onCancelar();

        /** Notifica que el contenido cambió de tamaño, para que el contenedor padre reajuste la ventana. */
        void onTamanoCambiado();
    }

    private final Listener listener;
    private final JPanel panelCentral = new JPanel(new BorderLayout());
    private final JPanel panelVistaQr;
    private final JPanel panelVistaBackup;

    private final JLabel lblQr = new JLabel();
    private final JTextField txtSecreto = new JTextField();
    private final JTextField txtCodigo = new JTextField();
    private final JPanel panelBackupCodes = new JPanel(new GridLayout(0, 2, 12, 6));

    public PasoTotpEnrolarPanel(Listener listener) {
        this.listener = listener;
        setOpaque(false);
        setLayout(new BorderLayout());
        panelCentral.setOpaque(false);
        panelVistaQr = crearPanelQr();
        panelVistaBackup = crearPanelBackup();
        add(panelCentral, BorderLayout.CENTER);
    }

    private JPanel crearPanelQr() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JLabel titulo = new JLabel("Active la verificación en dos pasos");
        titulo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        titulo.setForeground(AppV2Theme.TEXT_PRIMARY);
        JLabel subtitulo = new JLabel(
                "<html><body style='width:280px'>Escanee el código con Google Authenticator, Microsoft "
                        + "Authenticator u otra app compatible, o ingrese la clave manualmente.</body></html>");
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

        lblQr.setHorizontalAlignment(SwingConstants.CENTER);
        lblQr.setPreferredSize(new Dimension(200, 200));
        lblQr.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        JPanel qrWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        qrWrapper.setOpaque(false);
        qrWrapper.add(lblQr);

        txtSecreto.setEditable(false);
        txtSecreto.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        txtSecreto.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblCodigo = new JLabel("Código de verificación");
        lblCodigo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblCodigo.setForeground(AppV2Theme.TEXT_SECONDARY);
        txtCodigo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_MEDIUM));
        txtCodigo.setHorizontalAlignment(SwingConstants.CENTER);
        txtCodigo.setPreferredSize(new Dimension(160, 36));
        txtCodigo.addActionListener(e -> confirmarCodigo());

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 8, 0);
        formulario.add(qrWrapper, gbc);
        gbc.gridy = 1;
        formulario.add(txtSecreto, gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(14, 0, 4, 0);
        formulario.add(lblCodigo, gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        formulario.add(txtCodigo, gbc);

        JButton btnConfirmar = new JButton("Confirmar y activar");
        JButton btnCancelar = new JButton("Cancelar");
        btnConfirmar.setIcon(AppV2IconProvider.action(AppV2IconProvider.LOCK));
        btnConfirmar.setIconTextGap(8);
        AppV2Theme.estilizarBotonPrimario(btnConfirmar);
        AppV2Theme.estilizarBotonSecundario(btnCancelar);
        btnConfirmar.setPreferredSize(new Dimension(200, 38));
        btnCancelar.setPreferredSize(new Dimension(110, 38));
        btnConfirmar.addActionListener(e -> confirmarCodigo());
        btnCancelar.addActionListener(e -> {
            if (listener != null) {
                listener.onCancelar();
            }
        });
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        botones.add(btnCancelar);
        botones.add(btnConfirmar);

        panel.add(header, BorderLayout.NORTH);
        panel.add(formulario, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelBackup() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JLabel titulo = new JLabel("Guarde sus códigos de respaldo");
        titulo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        titulo.setForeground(AppV2Theme.TEXT_PRIMARY);
        JLabel subtitulo = new JLabel(
                "<html><body style='width:280px'>Úselos si pierde acceso a su app autenticadora. Cada código "
                        + "sirve una sola vez. No se volverán a mostrar.</body></html>");
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

        panelBackupCodes.setOpaque(true);
        panelBackupCodes.setBackground(AppV2Theme.SURFACE_ALT);
        panelBackupCodes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JButton btnFinalizar = new JButton("Ya los guardé, continuar");
        AppV2Theme.estilizarBotonPrimario(btnFinalizar);
        btnFinalizar.setPreferredSize(new Dimension(220, 38));
        btnFinalizar.addActionListener(e -> {
            if (listener != null) {
                listener.onFinalizar();
            }
        });
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        botones.add(btnFinalizar);

        panel.add(header, BorderLayout.NORTH);
        panel.add(panelBackupCodes, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private void confirmarCodigo() {
        if (listener != null) {
            listener.onConfirmarCodigo(txtCodigo.getText().trim());
        }
    }

    public void mostrarQr(BufferedImage imagenQr, String secretoBase32) {
        txtCodigo.setText("");
        txtSecreto.setText(secretoBase32);
        if (imagenQr != null) {
            lblQr.setIcon(new ImageIcon(imagenQr));
            lblQr.setText(null);
        } else {
            lblQr.setIcon(null);
            lblQr.setText("QR no disponible");
        }
        mostrarVista(panelVistaQr);
    }

    public void mostrarBackupCodes(List<String> codigos) {
        panelBackupCodes.removeAll();
        for (String codigo : codigos) {
            JLabel lbl = new JLabel(codigo);
            lbl.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
            lbl.setForeground(AppV2Theme.TEXT_PRIMARY);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            panelBackupCodes.add(lbl);
        }
        mostrarVista(panelVistaBackup);
    }

    public void reset() {
        txtCodigo.setText("");
        mostrarVista(panelVistaQr);
    }

    private void mostrarVista(JPanel vista) {
        panelCentral.removeAll();
        panelCentral.add(vista, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
        if (listener != null) {
            listener.onTamanoCambiado();
        }
    }
}
