package com.sdrerc.ui.appv2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class HomeV2 extends JPanel {

    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(86, 96, 108);

    public HomeV2() {
        configurarLayout();
    }

    private void configurarLayout() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 32, 32));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("SDRERC V2");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titulo.setForeground(TEXT_PRIMARY);
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Sistema de Rectificación de Actas - Nueva arquitectura SDRERC_APP");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitulo.setForeground(TEXT_SECONDARY);
        subtitulo.setAlignmentX(LEFT_ALIGNMENT);

        header.add(titulo);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitulo);

        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(28, 0, 0, 0));
        cards.add(crearTarjeta("Arquitectura paralela", "V2 corre sobre SDRERC_APP sin reemplazar la aplicacion legacy."));
        cards.add(crearTarjeta("Solo lectura inicial", "La primera bandeja usa vistas y no implementa movimientos ni escrituras."));
        cards.add(crearTarjeta("Evolucion progresiva", "Nuevas pantallas se agregaran en V2 sin tocar flujos legacy."));

        add(header, BorderLayout.NORTH);
        add(cards, BorderLayout.CENTER);
    }

    private JPanel crearTarjeta(String titulo, String descripcion) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        card.setPreferredSize(new Dimension(220, 120));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(TEXT_PRIMARY);

        JLabel lblDescripcion = new JLabel("<html><body style='width:180px'>" + descripcion + "</body></html>");
        lblDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDescripcion.setForeground(TEXT_SECONDARY);
        lblDescripcion.setVerticalAlignment(SwingConstants.TOP);

        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        content.setOpaque(false);
        content.add(lblDescripcion);

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }
}
