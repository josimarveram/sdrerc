package com.sdrerc.ui.appv2;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.views.asignacion.JPanelAsignacionV2;
import com.sdrerc.ui.views.administracion.roles.JPanelRolesV2;
import com.sdrerc.ui.views.expedienteconsola.JPanelBandejaExpedientesNueva;
import com.sdrerc.ui.views.registrorecepcion.JPanelRegistroRecepcionV2;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class MenuPrincipalV2 extends JFrame {

    private final JPanel body = new JPanel(new BorderLayout());
    private final JLabel lblTitulo = new JLabel("Inicio");
    private final JLabel lblSubtitulo = new JLabel("Panel inicial de SDRERC V2");
    private JButton btnInicio;
    private JButton btnBandeja;
    private JButton btnRegistroRecepcion;
    private JButton btnAsignacion;
    private JButton btnRoles;
    private JButton botonActivo;

    public MenuPrincipalV2() {
        configurarVentana();
        configurarLayout();
        mostrarInicio();
    }

    private void configurarVentana() {
        setTitle("SDRERC V2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setSize(1240, 800);
    }

    private void configurarLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppV2Theme.BACKGROUND);
        setContentPane(root);

        root.add(crearMenuLateral(), BorderLayout.WEST);
        root.add(crearAreaPrincipal(), BorderLayout.CENTER);
    }

    private JPanel crearMenuLateral() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(286, 0));
        sidebar.setBackground(AppV2Theme.SIDEBAR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));

        JLabel marca = new JLabel("<html><b>SDRERC V2</b><br><span style='font-size:10px'>Service Console</span></html>");
        marca.setForeground(Color.WHITE);
        marca.setFont(AppV2Theme.fontBold(22));
        marca.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(57, 91, 120)),
                BorderFactory.createEmptyBorder(0, 8, 16, 8)));

        JPanel opciones = new JPanel();
        opciones.setOpaque(false);
        opciones.setLayout(new BoxLayout(opciones, BoxLayout.Y_AXIS));

        opciones.add(crearSeccionMenu("Inicio"));
        btnInicio = crearBotonMenu("Inicio");
        btnInicio.addActionListener(e -> mostrarInicio());
        opciones.add(btnInicio);
        opciones.add(Box.createVerticalStrut(AppV2Theme.SPACE));

        opciones.add(crearSeccionMenu("Expedientes"));
        btnBandeja = crearBotonMenu("Bandeja Expedientes V2");
        btnBandeja.addActionListener(e -> mostrarBandeja(btnBandeja));
        opciones.add(btnBandeja);
        btnRegistroRecepcion = crearBotonMenu("Registro / Recepción");
        btnRegistroRecepcion.addActionListener(e -> mostrarRegistroRecepcion(btnRegistroRecepcion));
        opciones.add(btnRegistroRecepcion);
        btnAsignacion = crearBotonMenu("Asignación");
        btnAsignacion.addActionListener(e -> mostrarAsignacion(btnAsignacion));
        opciones.add(btnAsignacion);
        opciones.add(crearBotonPendiente("Análisis"));
        opciones.add(crearBotonPendiente("Verificación"));
        opciones.add(crearBotonPendiente("Firma / Emisión"));
        opciones.add(crearBotonPendiente("Ejecución"));
        opciones.add(Box.createVerticalStrut(AppV2Theme.SPACE));

        opciones.add(crearSeccionMenu("Seguimiento"));
        opciones.add(crearBotonPendiente("Notificación"));
        opciones.add(crearBotonPendiente("Publicación"));
        opciones.add(crearBotonPendiente("Expediente digital"));
        opciones.add(crearBotonPendiente("Cierre / Archivo"));
        opciones.add(Box.createVerticalStrut(AppV2Theme.SPACE));

        opciones.add(crearSeccionMenu("Administración"));
        opciones.add(crearBotonPendiente("Usuarios"));
        opciones.add(crearBotonPendiente("Equipo jurídico"));
        btnRoles = crearBotonMenu("Roles");
        btnRoles.addActionListener(e -> mostrarRoles(btnRoles));
        opciones.add(btnRoles);
        opciones.add(Box.createVerticalStrut(AppV2Theme.SPACE));

        JButton btnSalir = crearBotonMenu("Salir");
        btnSalir.addActionListener(e -> dispose());
        opciones.add(btnSalir);

        JScrollPane scrollMenu = new JScrollPane(opciones);
        scrollMenu.setOpaque(false);
        scrollMenu.getViewport().setOpaque(false);
        scrollMenu.setBorder(null);
        scrollMenu.getVerticalScrollBar().setUnitIncrement(16);

        sidebar.add(marca, BorderLayout.NORTH);
        sidebar.add(scrollMenu, BorderLayout.CENTER);
        botonActivo = btnInicio;
        aplicarEstadoActivo(btnInicio);
        return sidebar;
    }

    private JLabel crearSeccionMenu(String texto) {
        JLabel label = new JLabel(texto.toUpperCase());
        label.setForeground(new Color(190, 211, 230));
        label.setFont(AppV2Theme.fontBold(11));
        label.setBorder(BorderFactory.createEmptyBorder(14, 10, 6, 10));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JPanel crearAreaPrincipal() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(AppV2Theme.BACKGROUND);
        main.add(crearHeader(), BorderLayout.NORTH);
        body.setBackground(AppV2Theme.BACKGROUND);
        main.add(body, BorderLayout.CENTER);
        return main;
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppV2Theme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)));

        lblTitulo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_TITLE));
        lblTitulo.setForeground(AppV2Theme.TEXT_PRIMARY);

        lblSubtitulo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblSubtitulo.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel tituloPanel = new JPanel();
        tituloPanel.setOpaque(false);
        tituloPanel.setLayout(new BoxLayout(tituloPanel, BoxLayout.Y_AXIS));
        tituloPanel.add(lblTitulo);
        tituloPanel.add(Box.createVerticalStrut(2));
        tituloPanel.add(lblSubtitulo);

        JLabel lblModo = new JLabel("SDRERC_APP · lectura");
        lblModo.setHorizontalAlignment(SwingConstants.RIGHT);
        lblModo.setOpaque(true);
        lblModo.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        lblModo.setBackground(AppV2Theme.SOFT_GREEN);
        lblModo.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblModo.setForeground(AppV2Theme.SUCCESS);

        header.add(tituloPanel, BorderLayout.WEST);
        header.add(lblModo, BorderLayout.EAST);
        return header;
    }

    private JButton crearBotonPendiente(String texto) {
        JButton boton = crearBotonMenu(texto);
        boton.addActionListener(e -> {
            aplicarEstadoActivo(boton);
            JOptionPane.showMessageDialog(
                    this,
                    "Módulo pendiente de implementación en SDRERC V2.",
                    "SDRERC V2",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        return boton;
    }

    private JButton crearBotonMenu(String texto) {
        JButton boton = new JButton(texto);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppV2Theme.SIDEBAR),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        boton.setForeground(Color.WHITE);
        boton.setBackground(AppV2Theme.SIDEBAR);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(true);
        boton.setFont(new Font(AppV2Theme.FONT_FAMILY, Font.BOLD, 13));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        boton.setAlignmentX(LEFT_ALIGNMENT);
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (boton != botonActivo) {
                    boton.setBackground(AppV2Theme.SIDEBAR_HOVER);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (boton != botonActivo) {
                    aplicarEstadoNormal(boton);
                }
            }
        });
        return boton;
    }

    private void mostrarInicio() {
        lblTitulo.setText("Inicio");
        lblSubtitulo.setText("Dashboard inicial y accesos rápidos de SDRERC V2");
        cambiarContenido(new HomeV2(new Runnable() {
            @Override
            public void run() {
                mostrarBandeja(btnBandeja);
            }
        }));
        if (btnInicio != null) {
            aplicarEstadoActivo(btnInicio);
        }
    }

    private void mostrarBandeja(JButton boton) {
        lblTitulo.setText("Bandeja Expedientes V2");
        lblSubtitulo.setText("Listado general de expedientes consultado desde SDRERC_APP");
        cambiarContenido(new JPanelBandejaExpedientesNueva(false));
        aplicarEstadoActivo(boton);
    }

    private void mostrarRegistroRecepcion(JButton boton) {
        lblTitulo.setText("Registro / Recepción");
        lblSubtitulo.setText("Bandeja, carga diaria y registro manual preparados para la nueva arquitectura SDRERC_APP");
        cambiarContenido(new JPanelRegistroRecepcionV2());
        aplicarEstadoActivo(boton);
    }

    private void mostrarAsignacion(JButton boton) {
        lblTitulo.setText("Asignación");
        lblSubtitulo.setText("Asignación controlada de expedientes REGISTRO / REGISTRADO");
        cambiarContenido(new JPanelAsignacionV2());
        aplicarEstadoActivo(boton);
    }

    private void mostrarRoles(JButton boton) {
        lblTitulo.setText("Roles");
        lblSubtitulo.setText("Administración de perfiles de acceso y permisos del aplicativo");
        cambiarContenido(new JPanelRolesV2());
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
        boton.setBackground(AppV2Theme.SIDEBAR_ACTIVE);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppV2Theme.TEAL),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        boton.setForeground(Color.WHITE);
    }

    private void aplicarEstadoNormal(JButton boton) {
        boton.setBackground(AppV2Theme.SIDEBAR);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppV2Theme.SIDEBAR),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        boton.setForeground(Color.WHITE);
    }
}
