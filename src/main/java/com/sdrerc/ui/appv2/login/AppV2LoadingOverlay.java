package com.sdrerc.ui.appv2.login;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Overlay translucido con un mensaje animado ("Verificando..."), usado como glass pane del login
 * mientras se ejecutan llamadas a BD en segundo plano (SwingWorker), para que la espera se sienta
 * premium sin bloquear visualmente el EDT.
 */
public class AppV2LoadingOverlay extends JPanel {

    private final JLabel lblMensaje = new JLabel();
    private final Timer timer;
    private String mensajeBase = "Verificando";
    private int puntos;

    public AppV2LoadingOverlay() {
        setOpaque(false);
        setLayout(new GridBagLayout());
        setVisible(false);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)));
        lblMensaje.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        lblMensaje.setForeground(AppV2Theme.TEXT_PRIMARY);
        card.add(lblMensaje, BorderLayout.CENTER);
        add(card);

        timer = new Timer(400, e -> {
            puntos = (puntos + 1) % 4;
            StringBuilder sb = new StringBuilder(mensajeBase);
            for (int i = 0; i < puntos; i++) {
                sb.append('.');
            }
            lblMensaje.setText(sb.toString());
        });
    }

    public void mostrar(String mensaje) {
        mensajeBase = (mensaje == null || mensaje.trim().isEmpty()) ? "Verificando" : mensaje;
        puntos = 0;
        lblMensaje.setText(mensajeBase);
        setVisible(true);
        timer.start();
    }

    public void ocultar() {
        timer.stop();
        setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(244, 246, 249, 210));
            g2.fillRect(0, 0, getWidth(), getHeight());
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
