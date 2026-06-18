package com.sdrerc.ui.appv2;

import com.sdrerc.ui.appv2.components.AppV2CopyTextSupport;
import com.sdrerc.ui.appv2.components.AppV2IconProvider;
import com.sdrerc.ui.appv2.components.AppV2SidebarCollapseButton;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.views.administracion.equipojuridico.JPanelEquipoJuridicoV2;
import com.sdrerc.ui.views.administracion.feriados.JPanelFeriadosV2;
import com.sdrerc.ui.views.administracion.plazos.JPanelPlazosV2;
import com.sdrerc.ui.views.administracion.usuarios.JPanelUsuariosV2;
import com.sdrerc.ui.views.analisis.JPanelAnalisisV2;
import com.sdrerc.ui.views.asignacion.JPanelAsignacionV2;
import com.sdrerc.ui.views.administracion.roles.JPanelRolesV2;
import com.sdrerc.ui.views.cierrearchivo.JPanelCierreArchivoV2;
import com.sdrerc.ui.views.ejecucion.JPanelEjecucionV2;
import com.sdrerc.ui.views.expedientedigital.JPanelExpedienteDigitalV2;
import com.sdrerc.ui.views.expedienteconsola.JPanelBandejaExpedientesNueva;
import com.sdrerc.ui.views.notificacion.JPanelNotificacionV2;
import com.sdrerc.ui.views.publicacion.JPanelPublicacionV2;
import com.sdrerc.ui.views.registrorecepcion.JPanelRegistroRecepcionV2;
import com.sdrerc.ui.views.verificacion.JPanelVerificacionV2;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MenuPrincipalV2 extends JFrame {

    private static final int SIDEBAR_EXPANDED_WIDTH = 304;
    private static final int SIDEBAR_COLLAPSED_WIDTH = 72;

    private final JPanel body = new JPanel(new BorderLayout());
    private final JLabel lblTitulo = new JLabel("Inicio");
    private final JLabel lblSubtitulo = new JLabel("Panel ejecutivo de expedientes registrales");
    private final List<JButton> botonesMenu = new ArrayList<JButton>();
    private final List<JLabel> seccionesMenu = new ArrayList<JLabel>();
    private final Map<JButton, String> textosBotonesMenu = new LinkedHashMap<JButton, String>();
    private final Map<JButton, String> iconosBotonesMenu = new LinkedHashMap<JButton, String>();
    private final Map<JLabel, String> textosSeccionesMenu = new LinkedHashMap<JLabel, String>();
    private JPanel sidebar;
    private JLabel marca;
    private AppV2SidebarCollapseButton btnToggleSidebar;
    private boolean sidebarCollapsed;
    private JButton btnInicio;
    private JButton btnBandeja;
    private JButton btnRegistroRecepcion;
    private JButton btnAsignacion;
    private JButton btnAnalisis;
    private JButton btnVerificacion;
    private JButton btnEjecucion;
    private JButton btnNotificacion;
    private JButton btnPublicacion;
    private JButton btnExpedienteDigital;
    private JButton btnCierreArchivo;
    private JButton btnUsuarios;
    private JButton btnEquipoJuridico;
    private JButton btnRoles;
    private JButton btnFeriados;
    private JButton btnPlazos;
    private JButton botonActivo;

    public MenuPrincipalV2() {
        configurarVentana();
        AppV2CopyTextSupport.installForWindow(this);
        configurarLayout();
        mostrarInicio();
    }

    private void configurarVentana() {
        setTitle("SDRERC");
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
        sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(SIDEBAR_EXPANDED_WIDTH, 0));
        sidebar.setBackground(AppV2Theme.SIDEBAR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));

        marca = new JLabel("<html><b>SDRERC</b><br><span style='font-size:10px'>Gestión de expedientes</span></html>");
        marca.setForeground(Color.WHITE);
        marca.setFont(AppV2Theme.fontBold(22));
        marca.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(57, 91, 120)),
                BorderFactory.createEmptyBorder(0, 8, 16, 8)));
        btnToggleSidebar = new AppV2SidebarCollapseButton();
        btnToggleSidebar.addActionListener(e -> toggleSidebar());

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);
        top.add(marca, BorderLayout.CENTER);
        top.add(btnToggleSidebar, BorderLayout.EAST);

        JPanel opciones = new JPanel();
        opciones.setOpaque(false);
        opciones.setLayout(new BoxLayout(opciones, BoxLayout.Y_AXIS));

        opciones.add(crearSeccionMenu("Inicio"));
        btnInicio = crearBotonMenu("Inicio", AppV2IconProvider.HOME);
        btnInicio.addActionListener(e -> mostrarInicio());
        opciones.add(btnInicio);
        opciones.add(Box.createVerticalStrut(AppV2Theme.SPACE));

        opciones.add(crearSeccionMenu("Operación registral"));
        btnBandeja = crearBotonMenu("Bandeja de Expedientes", AppV2IconProvider.BANDEJA);
        btnBandeja.addActionListener(e -> mostrarBandeja(btnBandeja));
        opciones.add(btnBandeja);
        btnRegistroRecepcion = crearBotonMenu("Registro / Recepción", AppV2IconProvider.REGISTRO);
        btnRegistroRecepcion.addActionListener(e -> mostrarRegistroRecepcion(btnRegistroRecepcion));
        opciones.add(btnRegistroRecepcion);
        btnAsignacion = crearBotonMenu("Asignación", AppV2IconProvider.ASIGNACION);
        btnAsignacion.addActionListener(e -> mostrarAsignacion(btnAsignacion));
        opciones.add(btnAsignacion);
        btnAnalisis = crearBotonMenu("Análisis", AppV2IconProvider.ANALISIS);
        btnAnalisis.addActionListener(e -> mostrarAnalisis(btnAnalisis));
        opciones.add(btnAnalisis);
        btnVerificacion = crearBotonMenu("Verificación", AppV2IconProvider.VERIFICACION);
        btnVerificacion.addActionListener(e -> mostrarVerificacion(btnVerificacion));
        opciones.add(btnVerificacion);
        btnEjecucion = crearBotonMenu("Ejecución", AppV2IconProvider.EJECUCION);
        btnEjecucion.addActionListener(e -> mostrarEjecucion(btnEjecucion));
        opciones.add(btnEjecucion);
        opciones.add(Box.createVerticalStrut(AppV2Theme.SPACE));

        opciones.add(crearSeccionMenu("Seguimiento y comunicación"));
        btnNotificacion = crearBotonMenu("Notificación", AppV2IconProvider.NOTIFICACION);
        btnNotificacion.addActionListener(e -> mostrarNotificacion(btnNotificacion));
        opciones.add(btnNotificacion);
        btnPublicacion = crearBotonMenu("Publicación", AppV2IconProvider.PUBLICACION);
        btnPublicacion.addActionListener(e -> mostrarPublicacion(btnPublicacion));
        opciones.add(btnPublicacion);
        btnExpedienteDigital = crearBotonMenu("Expediente digital", AppV2IconProvider.EXPEDIENTE_DIGITAL);
        btnExpedienteDigital.addActionListener(e -> mostrarExpedienteDigital(btnExpedienteDigital));
        opciones.add(btnExpedienteDigital);
        btnCierreArchivo = crearBotonMenu("Cierre / Archivo", AppV2IconProvider.CIERRE_ARCHIVO);
        btnCierreArchivo.addActionListener(e -> mostrarCierreArchivo(btnCierreArchivo));
        opciones.add(btnCierreArchivo);
        opciones.add(Box.createVerticalStrut(AppV2Theme.SPACE));

        opciones.add(crearSeccionMenu("Administración"));
        btnUsuarios = crearBotonMenu("Usuarios", AppV2IconProvider.USUARIOS);
        btnUsuarios.addActionListener(e -> mostrarUsuarios(btnUsuarios));
        opciones.add(btnUsuarios);
        btnEquipoJuridico = crearBotonMenu("Equipo Jurídico", AppV2IconProvider.EQUIPO_JURIDICO);
        btnEquipoJuridico.addActionListener(e -> mostrarEquipoJuridico(btnEquipoJuridico));
        opciones.add(btnEquipoJuridico);
        btnRoles = crearBotonMenu("Roles", AppV2IconProvider.ROLES);
        btnRoles.addActionListener(e -> mostrarRoles(btnRoles));
        opciones.add(btnRoles);
        btnFeriados = crearBotonMenu("Feriados", AppV2IconProvider.FERIADOS);
        btnFeriados.addActionListener(e -> mostrarFeriados(btnFeriados));
        opciones.add(btnFeriados);
        btnPlazos = crearBotonMenu("Plazos", AppV2IconProvider.PLAZOS);
        btnPlazos.addActionListener(e -> mostrarPlazos(btnPlazos));
        opciones.add(btnPlazos);
        opciones.add(Box.createVerticalStrut(AppV2Theme.SPACE));

        opciones.add(crearSeccionMenu("Sistema"));
        JButton btnSalir = crearBotonMenu("Salir", AppV2IconProvider.SALIR);
        btnSalir.addActionListener(e -> dispose());
        opciones.add(btnSalir);

        JScrollPane scrollMenu = new JScrollPane(opciones);
        scrollMenu.setOpaque(false);
        scrollMenu.getViewport().setOpaque(false);
        scrollMenu.setBorder(null);
        scrollMenu.getVerticalScrollBar().setUnitIncrement(16);

        sidebar.add(top, BorderLayout.NORTH);
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
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        label.setAlignmentX(LEFT_ALIGNMENT);
        textosSeccionesMenu.put(label, texto.toUpperCase());
        seccionesMenu.add(label);
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

    private JButton crearBotonMenu(String texto, String iconCode) {
        JButton boton = new JButton(texto);
        boton.setIcon(AppV2IconProvider.menu(iconCode));
        boton.setIconTextGap(12);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setHorizontalTextPosition(SwingConstants.RIGHT);
        boton.setVerticalTextPosition(SwingConstants.CENTER);
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
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        boton.setAlignmentX(LEFT_ALIGNMENT);
        boton.setToolTipText(texto);
        textosBotonesMenu.put(boton, texto);
        iconosBotonesMenu.put(boton, iconCode);
        botonesMenu.add(boton);
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

    private void toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        actualizarSidebar();
    }

    private void actualizarSidebar() {
        sidebar.setPreferredSize(new Dimension(sidebarCollapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_EXPANDED_WIDTH, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, sidebarCollapsed ? 8 : 12, 18, sidebarCollapsed ? 8 : 12));
        marca.setText(sidebarCollapsed
                ? ""
                : "<html><b>SDRERC</b><br><span style='font-size:10px'>Gestión de expedientes</span></html>");
        marca.setVisible(!sidebarCollapsed);
        marca.setFont(AppV2Theme.fontBold(sidebarCollapsed ? 18 : 22));
        marca.setHorizontalAlignment(sidebarCollapsed ? SwingConstants.CENTER : SwingConstants.LEFT);
        btnToggleSidebar.setCollapsed(sidebarCollapsed);
        for (JLabel seccion : seccionesMenu) {
            String textoSeccion = textosSeccionesMenu.get(seccion);
            seccion.setVisible(true);
            seccion.setText(sidebarCollapsed ? "" : textoSeccion);
            seccion.setBorder(sidebarCollapsed
                    ? BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(52, 83, 109)),
                            BorderFactory.createEmptyBorder(7, 0, 5, 0))
                    : BorderFactory.createEmptyBorder(14, 10, 6, 10));
            seccion.setMaximumSize(new Dimension(Integer.MAX_VALUE, sidebarCollapsed ? 14 : 34));
        }
        for (JButton boton : botonesMenu) {
            String texto = textosBotonesMenu.get(boton);
            String iconCode = iconosBotonesMenu.get(boton);
            boton.setText(sidebarCollapsed ? "" : texto);
            boton.setIcon(sidebarCollapsed ? AppV2IconProvider.menuCollapsed(iconCode) : AppV2IconProvider.menu(iconCode));
            boton.setIconTextGap(sidebarCollapsed ? 0 : 12);
            boton.setHorizontalAlignment(sidebarCollapsed ? SwingConstants.CENTER : SwingConstants.LEFT);
            boton.setToolTipText(texto);
            if (boton == botonActivo) {
                aplicarEstadoActivo(boton);
            } else {
                aplicarEstadoNormal(boton);
            }
        }
        sidebar.revalidate();
        sidebar.repaint();
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void mostrarInicio() {
        lblTitulo.setText("Inicio");
        lblSubtitulo.setText("Panel ejecutivo y seguimiento integral de expedientes");
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
        lblTitulo.setText("Bandeja de Expedientes");
        lblSubtitulo.setText("Consulta, seguimiento y priorización de expedientes por etapa, estado, responsable y plazos de atención");
        cambiarContenido(new JPanelBandejaExpedientesNueva(false));
        aplicarEstadoActivo(boton);
    }

    private void mostrarRegistroRecepcion(JButton boton) {
        lblTitulo.setText("Registro / Recepción");
        lblSubtitulo.setText("Recepción, carga diaria y registro manual de expedientes para iniciar la gestión registral");
        cambiarContenido(new JPanelRegistroRecepcionV2());
        aplicarEstadoActivo(boton);
    }

    private void mostrarAsignacion(JButton boton) {
        lblTitulo.setText("Asignación");
        lblSubtitulo.setText("Gestión de expedientes pendientes, responsables y alertas de posibles relacionados");
        cambiarContenido(new JPanelAsignacionV2());
        aplicarEstadoActivo(boton);
    }

    private void mostrarAnalisis(JButton boton) {
        lblTitulo.setText("Análisis");
        lblSubtitulo.setText("Evaluación jurídica y documental de expedientes asignados");
        cambiarContenido(new JPanelAnalisisV2());
        aplicarEstadoActivo(boton);
    }

    private void mostrarVerificacion(JButton boton) {
        lblTitulo.setText("Verificación");
        lblSubtitulo.setText("Revisión, firma y emisión de expedientes aprobados por análisis");
        cambiarContenido(new JPanelVerificacionV2());
        aplicarEstadoActivo(boton);
    }

    private void mostrarEjecucion(JButton boton) {
        lblTitulo.setText("Ejecución");
        lblSubtitulo.setText("Gestión de expedientes con resolución o documento listo para ejecución");
        cambiarContenido(new JPanelEjecucionV2());
        aplicarEstadoActivo(boton);
    }

    private void mostrarNotificacion(JButton boton) {
        lblTitulo.setText("Notificación");
        lblSubtitulo.setText("Gestión de notificaciones, cargos de acuse y resultado de comunicación al administrado");
        cambiarContenido(new JPanelNotificacionV2());
        aplicarEstadoActivo(boton);
    }

    private void mostrarPublicacion(JButton boton) {
        lblTitulo.setText("Publicación");
        lblSubtitulo.setText("Gestión de expedientes con publicación pendiente por notificación no concretada");
        cambiarContenido(new JPanelPublicacionV2());
        aplicarEstadoActivo(boton);
    }

    private void mostrarExpedienteDigital(JButton boton) {
        lblTitulo.setText("Expediente digital");
        lblSubtitulo.setText("Gestión de carpeta, enlace y completitud digital del expediente");
        cambiarContenido(new JPanelExpedienteDigitalV2());
        aplicarEstadoActivo(boton);
    }

    private void mostrarCierreArchivo(JButton boton) {
        lblTitulo.setText("Cierre / Archivo");
        lblSubtitulo.setText("Consulta y gestión final de expedientes cerrados, archivados o derivados");
        cambiarContenido(new JPanelCierreArchivoV2());
        aplicarEstadoActivo(boton);
    }

    private void mostrarUsuarios(JButton boton) {
        lblTitulo.setText("Usuarios");
        lblSubtitulo.setText("Administración de usuarios, roles y acceso al aplicativo");
        cambiarContenido(new JPanelUsuariosV2());
        aplicarEstadoActivo(boton);
    }

    private void mostrarEquipoJuridico(JButton boton) {
        lblTitulo.setText("Equipo Jurídico");
        lblSubtitulo.setText("Administración de equipos, abogados y supervisores para la gestión de expedientes");
        cambiarContenido(new JPanelEquipoJuridicoV2());
        aplicarEstadoActivo(boton);
    }

    private void mostrarRoles(JButton boton) {
        lblTitulo.setText("Roles");
        lblSubtitulo.setText("Administración de perfiles de acceso y permisos del aplicativo");
        cambiarContenido(new JPanelRolesV2());
        aplicarEstadoActivo(boton);
    }

    private void mostrarFeriados(JButton boton) {
        lblTitulo.setText("Feriados");
        lblSubtitulo.setText("Configuración de feriados nacionales para el cálculo de plazos hábiles");
        cambiarContenido(new JPanelFeriadosV2());
        aplicarEstadoActivo(boton);
    }

    private void mostrarPlazos(JButton boton) {
        lblTitulo.setText("Plazos");
        lblSubtitulo.setText("Configuración de plazos de atención y unidad de cálculo");
        cambiarContenido(new JPanelPlazosV2());
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
                BorderFactory.createEmptyBorder(10, sidebarCollapsed ? 6 : 12, 10, sidebarCollapsed ? 6 : 12)));
        boton.setHorizontalAlignment(sidebarCollapsed ? SwingConstants.CENTER : SwingConstants.LEFT);
        boton.setIconTextGap(sidebarCollapsed ? 0 : 12);
        boton.setForeground(Color.WHITE);
    }

    private void aplicarEstadoNormal(JButton boton) {
        boton.setBackground(AppV2Theme.SIDEBAR);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppV2Theme.SIDEBAR),
                BorderFactory.createEmptyBorder(10, sidebarCollapsed ? 6 : 12, 10, sidebarCollapsed ? 6 : 12)));
        boton.setHorizontalAlignment(sidebarCollapsed ? SwingConstants.CENTER : SwingConstants.LEFT);
        boton.setIconTextGap(sidebarCollapsed ? 0 : 12);
        boton.setForeground(Color.WHITE);
    }
}
