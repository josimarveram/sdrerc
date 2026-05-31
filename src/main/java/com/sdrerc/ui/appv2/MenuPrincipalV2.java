package com.sdrerc.ui.appv2;

import com.sdrerc.ui.views.expedienteconsola.JPanelBandejaExpedientesNueva;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MenuPrincipalV2 extends JFrame {

    private static final Color SIDEBAR_BG = new Color(28, 74, 128);
    private static final Color SIDEBAR_ACTIVE = new Color(18, 55, 100);
    private static final Color SIDEBAR_HOVER = new Color(40, 91, 150);
    private static final Color CONTENT_BG = new Color(245, 247, 250);
    private static final Color HEADER_BG = Color.WHITE;
    private static final Color BORDER = new Color(220, 224, 230);

    private final JPanel body = new JPanel(new BorderLayout());
    private final JLabel lblTitulo = new JLabel("Inicio");
    private JButton btnInicio;
    private JButton btnBandeja;
    private JButton botonActivo;

    public MenuPrincipalV2() {
        configurarVentana();
        configurarLayout();
        mostrarInicio();
    }

    private void configurarVentana() {
        setTitle("SDRERC V2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1180, 760);
    }

    private void configurarLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CONTENT_BG);
        setContentPane(root);

        root.add(crearMenuLateral(), BorderLayout.WEST);
        root.add(crearAreaPrincipal(), BorderLayout.CENTER);
    }

    private JPanel crearMenuLateral() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 14, 18, 14));

        JLabel marca = new JLabel("<html><b>SDRERC</b><br>V2</html>");
        marca.setForeground(Color.WHITE);
        marca.setFont(new Font("Segoe UI", Font.BOLD, 24));
        marca.setBorder(BorderFactory.createEmptyBorder(0, 8, 18, 8));

        JPanel opciones = new JPanel(new GridLayout(0, 1, 0, 8));
        opciones.setOpaque(false);
        btnInicio = crearBotonMenu("Inicio");
        btnBandeja = crearBotonMenu("Bandeja Expedientes V2");
        JButton btnSalir = crearBotonMenu("Salir");

        btnInicio.addActionListener(e -> mostrarInicio());
        btnBandeja.addActionListener(e -> mostrarBandeja(btnBandeja));
        btnSalir.addActionListener(e -> dispose());

        opciones.add(btnInicio);
        opciones.add(btnBandeja);
        opciones.add(btnSalir);

        sidebar.add(marca, BorderLayout.NORTH);
        sidebar.add(opciones, BorderLayout.CENTER);
        botonActivo = btnInicio;
        aplicarEstadoActivo(btnInicio);
        return sidebar;
    }

    private JPanel crearAreaPrincipal() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(CONTENT_BG);
        main.add(crearHeader(), BorderLayout.NORTH);
        body.setBackground(CONTENT_BG);
        main.add(body, BorderLayout.CENTER);
        return main;
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)));

        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(33, 37, 41));

        JLabel lblModo = new JLabel("SDRERC_APP - solo lectura inicial");
        lblModo.setHorizontalAlignment(SwingConstants.RIGHT);
        lblModo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblModo.setForeground(new Color(96, 108, 120));

        header.add(lblTitulo, BorderLayout.WEST);
        header.add(lblModo, BorderLayout.EAST);
        return header;
    }

    private JButton crearBotonMenu(String texto) {
        JButton boton = new JButton(texto);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        boton.setForeground(Color.WHITE);
        boton.setBackground(SIDEBAR_BG);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (boton != botonActivo) {
                    boton.setBackground(SIDEBAR_HOVER);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (boton != botonActivo) {
                    boton.setBackground(SIDEBAR_BG);
                }
            }
        });
        return boton;
    }

    private void mostrarInicio() {
        lblTitulo.setText("Inicio");
        cambiarContenido(new HomeV2());
        if (btnInicio != null) {
            aplicarEstadoActivo(btnInicio);
        }
    }

    private void mostrarBandeja(JButton boton) {
        lblTitulo.setText("Bandeja Expedientes V2");
        cambiarContenido(new JPanelBandejaExpedientesNueva());
        aplicarEstadoActivo(boton);
    }

    private void cambiarContenido(JPanel panel) {
        body.removeAll();
        body.add(panel, BorderLayout.CENTER);
        body.revalidate();
        body.repaint();
    }

    private void aplicarEstadoActivo(JButton boton) {
        if (botonActivo != null && botonActivo != boton) {
            aplicarEstadoNormal(botonActivo);
        }
        botonActivo = boton;
        boton.setBackground(SIDEBAR_ACTIVE);
    }

    private void aplicarEstadoNormal(JButton boton) {
        boton.setBackground(SIDEBAR_BG);
    }
}
