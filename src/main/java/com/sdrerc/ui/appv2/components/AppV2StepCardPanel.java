package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Mini-panel tipo "paso" para paneles laterales operativos (patron stepper): insignia numerica
 * opcional, titulo, chip de estado a la derecha y contenido que puede quedar "bloqueado" mostrando
 * un mensaje en su lugar en vez del contenido real. Pensado para reordenar acciones que dependen de
 * un estado previo (ej. Emision antes de Asignacion en la Bandeja Asignacion de Notificacion) sin
 * tocar la logica interna del contenido que envuelve.
 */
public class AppV2StepCardPanel extends JPanel {

    private static final String CARA_CONTENIDO = "contenido";
    private static final String CARA_BLOQUEADO = "bloqueado";

    private final PillBadgeV2 badgeNumero = new PillBadgeV2("", AppV2Theme.PRIMARY, Color.WHITE);
    private final JLabel lblTitulo = new JLabel();
    private final PillBadgeV2 chipEstado = new PillBadgeV2("", AppV2Theme.SOFT_GRAY, AppV2Theme.MUTED);
    private final JLabel lblCandado = new JLabel(AppV2IconProvider.action(AppV2IconProvider.LOCK));
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cuerpo = new JPanel(cardLayout);
    private final JPanel contenedorContenido = new JPanel(new BorderLayout());
    private final JLabel lblMensajeBloqueo = new JLabel("", SwingConstants.CENTER);

    public AppV2StepCardPanel(String titulo) {
        super(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(12, 14, 14, 14)));

        lblTitulo.setText(titulo);
        lblTitulo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        lblTitulo.setForeground(AppV2Theme.TEXT_PRIMARY);

        badgeNumero.setPreferredSize(new Dimension(24, 24));
        badgeNumero.setFont(AppV2Theme.fontBold(12));
        badgeNumero.setVisible(false);

        chipEstado.setFont(AppV2Theme.fontBold(11));
        chipEstado.setVisible(false);
        lblCandado.setVisible(false);

        JPanel encabezadoIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        encabezadoIzquierda.setOpaque(false);
        encabezadoIzquierda.add(badgeNumero);
        encabezadoIzquierda.add(lblTitulo);

        JPanel encabezadoDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        encabezadoDerecha.setOpaque(false);
        encabezadoDerecha.add(lblCandado);
        encabezadoDerecha.add(chipEstado);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(encabezadoIzquierda, BorderLayout.WEST);
        encabezado.add(encabezadoDerecha, BorderLayout.EAST);

        contenedorContenido.setOpaque(false);

        lblMensajeBloqueo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblMensajeBloqueo.setForeground(AppV2Theme.TEXT_SECONDARY);
        JPanel panelBloqueado = new JPanel(new BorderLayout());
        panelBloqueado.setOpaque(false);
        panelBloqueado.setBorder(BorderFactory.createEmptyBorder(18, 8, 18, 8));
        JLabel lblCandadoGrande = new JLabel(AppV2IconProvider.load(AppV2IconProvider.LOCK, 28));
        lblCandadoGrande.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel bloqueadoCentro = new JPanel();
        bloqueadoCentro.setOpaque(false);
        bloqueadoCentro.setLayout(new javax.swing.BoxLayout(bloqueadoCentro, javax.swing.BoxLayout.Y_AXIS));
        lblCandadoGrande.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblMensajeBloqueo.setAlignmentX(Component.CENTER_ALIGNMENT);
        bloqueadoCentro.add(lblCandadoGrande);
        bloqueadoCentro.add(javax.swing.Box.createVerticalStrut(8));
        bloqueadoCentro.add(lblMensajeBloqueo);
        panelBloqueado.add(bloqueadoCentro, BorderLayout.CENTER);

        cuerpo.setOpaque(false);
        cuerpo.add(contenedorContenido, CARA_CONTENIDO);
        cuerpo.add(panelBloqueado, CARA_BLOQUEADO);

        add(encabezado, BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);
    }

    public void setStepNumber(Integer numero) {
        if (numero == null) {
            badgeNumero.setVisible(false);
            return;
        }
        badgeNumero.setText(String.valueOf(numero));
        badgeNumero.setVisible(true);
    }

    public void setStatus(String texto, Color background, Color foreground) {
        if (texto == null || texto.trim().isEmpty()) {
            chipEstado.setVisible(false);
            return;
        }
        chipEstado.setText(texto);
        chipEstado.setBackground(background);
        chipEstado.setForeground(foreground);
        chipEstado.setVisible(true);
        // PillBadgeV2 dibuja el pill redondeado con su propio ancho (texto + relleno) en
        // paintComponent, sin pasar por el calculo estandar de JLabel.getPreferredSize(); si el
        // layout (FlowLayout aqui) solo reserva el ancho "de texto" por defecto, el pill queda
        // recortado a la derecha. Se fuerza un preferredSize generoso acorde al texto real.
        java.awt.FontMetrics metrics = chipEstado.getFontMetrics(chipEstado.getFont());
        int ancho = metrics.stringWidth(texto) + 30;
        chipEstado.setPreferredSize(new Dimension(Math.max(64, ancho), 24));
    }

    public void setContent(Component content) {
        contenedorContenido.removeAll();
        if (content != null) {
            contenedorContenido.add(content, BorderLayout.CENTER);
        }
        contenedorContenido.revalidate();
        contenedorContenido.repaint();
    }

    public void setLocked(boolean locked, String mensaje) {
        lblCandado.setVisible(locked);
        lblMensajeBloqueo.setText(locked && mensaje != null ? "<html><div style='width:220px;text-align:center;'>"
                + mensaje.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</div></html>" : "");
        cardLayout.show(cuerpo, locked ? CARA_BLOQUEADO : CARA_CONTENIDO);
    }
}
