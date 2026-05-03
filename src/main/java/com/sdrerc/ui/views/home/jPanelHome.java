/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.sdrerc.ui.views.home;

import com.sdrerc.ui.common.icon.IconUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 *
 * @author betom
 */
public class jPanelHome extends javax.swing.JPanel {

    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER = new Color(218, 224, 231);
    private static final Color TITLE = new Color(25, 42, 62);
    private static final Color TEXT = new Color(73, 85, 99);
    private static final Color MUTED = new Color(107, 114, 128);
    private static final Color ACCENT = new Color(25, 120, 210);
    private static final Color HOVER = new Color(239, 246, 255);

    private Runnable abrirRecepcion;
    private Runnable abrirAsignacion;
    private Runnable abrirExpedientesPorTrabajar;
    private Runnable abrirExpedientesPorVerificar;
    private Runnable abrirEquipoJuridico;
    private Runnable abrirUsuarios;
    private Runnable abrirRoles;

    /**
     * Creates new form jPanelHome
     */
    public jPanelHome() {
        initComponents();
        configurarHomeModerno();
    }

    public jPanelHome(
            Runnable abrirRecepcion,
            Runnable abrirAsignacion,
            Runnable abrirExpedientesPorTrabajar,
            Runnable abrirExpedientesPorVerificar,
            Runnable abrirEquipoJuridico,
            Runnable abrirUsuarios,
            Runnable abrirRoles) {
        initComponents();
        this.abrirRecepcion = abrirRecepcion;
        this.abrirAsignacion = abrirAsignacion;
        this.abrirExpedientesPorTrabajar = abrirExpedientesPorTrabajar;
        this.abrirExpedientesPorVerificar = abrirExpedientesPorVerificar;
        this.abrirEquipoJuridico = abrirEquipoJuridico;
        this.abrirUsuarios = abrirUsuarios;
        this.abrirRoles = abrirRoles;
        configurarHomeModerno();
    }

    private void configurarHomeModerno() {
        removeAll();
        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel contenido = new HomeContentPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(BG);
        contenido.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        contenido.add(crearHeader());
        contenido.add(Box.createVerticalStrut(18));
        contenido.add(crearCardPresentacion());
        contenido.add(Box.createVerticalStrut(22));
        contenido.add(crearSeccionAccesosRapidos());
        contenido.add(Box.createVerticalStrut(22));
        contenido.add(crearSeccionFlujoOperativo());
        contenido.add(Box.createVerticalStrut(22));
        contenido.add(crearSeccionAdministracion());
        contenido.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(
                contenido,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel crearHeader() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

        JLabel titulo = new JLabel("Bienvenido a SDRERC");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titulo.setForeground(TITLE);

        JLabel subtitulo = new JLabel("Sistema de gestión de expedientes registrales y seguimiento operativo.");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitulo.setForeground(TEXT);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(subtitulo, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearCardPresentacion() {
        JPanel card = crearCardBase();
        card.setLayout(new BorderLayout(14, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 104));

        JLabel icon = new JLabel(IconUtils.load("file.svg", 28));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setPreferredSize(new Dimension(42, 42));

        JLabel texto = new JLabel("<html><body style='width:780px'>Desde este panel puede acceder a los módulos de recepción, asignación, trabajo de expedientes, verificación, ejecución, notificación y administración del sistema.</body></html>");
        texto.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        texto.setForeground(TEXT);

        card.add(icon, BorderLayout.WEST);
        card.add(texto, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearSeccionAccesosRapidos() {
        JPanel seccion = crearSeccion("Accesos rápidos");
        JPanel grid = crearGridCards();

        agregarCardGrid(grid, 0, 0, crearCardAcceso("Recepción", "Registrar y consultar expedientes recibidos.", "inbox.svg", abrirRecepcion));
        agregarCardGrid(grid, 1, 0, crearCardAcceso("Asignación", "Asignar expedientes para atención operativa.", "assignment.svg", abrirAsignacion));
        agregarCardGrid(grid, 2, 0, crearCardAcceso("Expedientes por trabajar", "Revisar la carga pendiente de trabajo.", "file.svg", abrirExpedientesPorTrabajar));
        agregarCardGrid(grid, 0, 1, crearCardAcceso("Expedientes por verificar", "Validar expedientes listos para revisión.", "check.svg", abrirExpedientesPorVerificar));
        agregarCardGrid(grid, 1, 1, crearCardAcceso("Equipo Jurídico", "Gestionar abogados, supervisores e importación.", "supervisor.svg", abrirEquipoJuridico));
        agregarCardGrid(grid, 2, 1, crearCardAcceso("Usuarios", "Administrar cuentas y accesos del sistema.", "users.svg", abrirUsuarios));

        seccion.add(grid);
        return seccion;
    }

    private JPanel crearCardAcceso(String titulo, String descripcion, String icono, Runnable accion) {
        JPanel card = crearCardBase();
        card.setLayout(new BorderLayout(12, 0));
        card.setPreferredSize(new Dimension(250, 112));
        card.setMinimumSize(new Dimension(210, 104));
        card.setCursor(accion == null ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setEnabled(accion != null);
        aplicarHoverCard(card);
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                ejecutarAccion(accion);
            }
        });

        JLabel icon = new JLabel(cargarIcono(icono, 26));
        icon.setPreferredSize(new Dimension(34, 34));
        icon.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(TITLE);
        JLabel lblDesc = new JLabel("<html><body style='width:170px'>" + descripcion + "</body></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(MUTED);

        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(6));
        textos.add(lblDesc);

        card.add(icon, BorderLayout.WEST);
        card.add(textos, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearSeccionFlujoOperativo() {
        JPanel seccion = crearSeccion("Flujo operativo");
        JPanel flujo = crearCardBase();
        flujo.setLayout(new GridBagLayout());
        flujo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        String[] pasos = {"Recepción", "Asignación", "Trabajo", "Verificación", "Ejecución", "Notificación"};
        String[] iconos = {"inbox.svg", "assignment.svg", "file.svg", "check.svg", "play.svg", "bell.svg"};
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < pasos.length; i++) {
            gbc.gridx = i * 2;
            gbc.weightx = 1;
            flujo.add(crearPasoFlujo(pasos[i], iconos[i]), gbc);
            if (i < pasos.length - 1) {
                gbc.gridx = i * 2 + 1;
                gbc.weightx = 0;
                JLabel flecha = new JLabel("→");
                flecha.setFont(new Font("Segoe UI", Font.BOLD, 18));
                flecha.setForeground(MUTED);
                flujo.add(flecha, gbc);
            }
        }

        seccion.add(flujo);
        return seccion;
    }

    private JPanel crearPasoFlujo(String texto, String icono) {
        JPanel paso = new JPanel(new BorderLayout(6, 0));
        paso.setOpaque(false);
        JLabel lblIcon = new JLabel(cargarIcono(icono, 18));
        JLabel lblTexto = new JLabel(texto);
        lblTexto.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTexto.setForeground(TITLE);
        paso.add(lblIcon, BorderLayout.WEST);
        paso.add(lblTexto, BorderLayout.CENTER);
        return paso;
    }

    private JPanel crearSeccionAdministracion() {
        JPanel seccion = crearSeccion("Administración");
        JPanel grid = crearGridCards();

        agregarCardGrid(grid, 0, 0, crearCardAcceso("Usuarios", "Gestionar cuentas, estados y roles.", "users.svg", abrirUsuarios));
        agregarCardGrid(grid, 1, 0, crearCardAcceso("Roles", "Administrar perfiles y permisos.", "role.svg", abrirRoles));
        agregarCardGrid(grid, 2, 0, crearCardAcceso("Equipo Jurídico", "Consultar y auditar equipo operativo.", "supervisor.svg", abrirEquipoJuridico));

        seccion.add(grid);
        return seccion;
    }

    private JPanel crearSeccion(String titulo) {
        JPanel seccion = new JPanel();
        seccion.setOpaque(false);
        seccion.setLayout(new BoxLayout(seccion, BoxLayout.Y_AXIS));
        seccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        seccion.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel label = new JLabel(titulo);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(TITLE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        seccion.add(label);
        seccion.add(Box.createVerticalStrut(10));
        return seccion;
    }

    private JPanel crearGridCards() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return grid;
    }

    private void agregarCardGrid(JPanel grid, int x, int y, JPanel card) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 14, x < 2 ? 14 : 0);
        grid.add(card, gbc);
    }

    private JPanel crearCardBase() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private void aplicarHoverCard(JPanel card) {
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (card.isEnabled()) {
                    card.setBackground(HOVER);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(CARD_BG);
            }
        });
    }

    private void ejecutarAccion(Runnable accion) {
        if (accion != null) {
            accion.run();
        }
    }

    private Icon cargarIcono(String icono, int size) {
        Icon icon = IconUtils.load(icono, size);
        return icon;
    }

    private static class HomeContentPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return Math.max(80, visibleRect.height - 80);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("BIENVENIDOS AL SISTEMA");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setText("SDRERC");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(135, 135, 135)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(227, 227, 227)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(106, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(193, 193, 193)
                .addComponent(jLabel1)
                .addGap(28, 28, 28)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(324, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
