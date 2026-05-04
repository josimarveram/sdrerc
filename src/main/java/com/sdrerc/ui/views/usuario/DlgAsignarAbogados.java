/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.sdrerc.ui.views.usuario;

import com.sdrerc.application.SupervisionService;
import com.sdrerc.application.UserService;
import com.sdrerc.domain.model.User;
import com.sdrerc.ui.common.icon.IconUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author David
 */
public class DlgAsignarAbogados extends javax.swing.JDialog {

    /**
     * Creates new form DlgEditarRol
     */
    
    private final Long supervisorId;
    private final UserService userService;
    private final SupervisionService supervisionService;

    private DefaultListModel<User> modelDisponibles = new DefaultListModel<>();
    private DefaultListModel<User> modelAsignados = new DefaultListModel<>();
    private JLabel lblDisponiblesContador;
    private JLabel lblAsignadosContador;
    
    public DlgAsignarAbogados(
            Window parent,
            Long supervisorId,
            String supervisorNombre,
            UserService userService,
            SupervisionService supervisionService) {
        super(parent, "Equipo supervisado", ModalityType.APPLICATION_MODAL);
        initComponents();

        this.supervisorId = supervisorId;
        this.userService = userService;
        this.supervisionService = supervisionService;

        lblSupervisor.setText(supervisorNombre);

        configurarListas();
        cargarDatos();
        configurarDialogoEquipo();

    }
    
    private void configurarListas() {

        lstDisponibles.setModel(modelDisponibles);
        lstAsignados.setModel(modelAsignados);

        lstDisponibles.setSelectionMode(
            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        );

        lstAsignados.setSelectionMode(
            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        );
    }
    
    private void cargarDatos() {

        try {
            List<User> todosAbogados =
                supervisionService.listarAbogadosDisponiblesParaSupervisor(supervisorId);

            List<Long> asignadosIds =
                supervisionService.obtenerAbogados(supervisorId);

            modelDisponibles.clear();
            modelAsignados.clear();

            for (User u : todosAbogados) {
                if (asignadosIds.contains(u.getUserId())) {
                    modelAsignados.addElement(u);
                } else {
                    modelDisponibles.addElement(u);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,e.getMessage());
        }
    }
    
    private void moverSeleccionados(
            DefaultListModel<User> origen,
            DefaultListModel<User> destino,
            JList<User> listaOrigen) {

        List<User> seleccionados =
            listaOrigen.getSelectedValuesList();

        for (User u : seleccionados) {
            origen.removeElement(u);
            destino.addElement(u);
        }
        actualizarEstadoVisual();
    }
    
    private void moverTodos(
            DefaultListModel<User> origen,
            DefaultListModel<User> destino) {

        int total = origen.getSize();

        for (int i = 0; i < total; i++) {
            destino.addElement(origen.getElementAt(i));
        }

        origen.clear();
        actualizarEstadoVisual();
    }

    private void configurarDialogoEquipo() {
        setTitle("Gestionar equipo supervisado");

        configurarBotonesEquipo();
        configurarListasPremium();
        registrarEventosUx();
        registrarActualizacionModelos();

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(new Color(245, 247, 250));
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        root.add(crearEncabezadoEquipo(), BorderLayout.NORTH);
        root.add(crearCentroEquipo(), BorderLayout.CENTER);
        root.add(crearBotoneraEquipo(), BorderLayout.SOUTH);

        setContentPane(root);
        setPreferredSize(new Dimension(840, 620));
        setMinimumSize(new Dimension(820, 600));
        actualizarEstadoVisual();
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void configurarBotonesEquipo() {
        btnAgregar.setText("Agregar >");
        btnAgregar.setToolTipText("Agregar seleccionado");
        estilizarBotonMovimiento(btnAgregar);

        btnAgregarTodos.setText("Agregar todos >>");
        btnAgregarTodos.setToolTipText("Agregar todos");
        estilizarBotonMovimiento(btnAgregarTodos);

        btnQuitar.setText("< Quitar");
        btnQuitar.setToolTipText("Quitar seleccionado");
        estilizarBotonMovimiento(btnQuitar);

        btnQuitarTodos.setText("<< Quitar todos");
        btnQuitarTodos.setToolTipText("Quitar todos");
        estilizarBotonMovimiento(btnQuitarTodos);

        btnGuardar.setText("Guardar cambios");
        btnGuardar.setToolTipText("Guardar equipo supervisado.");
        btnGuardar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        IconUtils.applyIcon(btnGuardar, "active.svg");

        btnCancelar.setText("Cancelar");
        btnCancelar.setToolTipText("Cerrar sin guardar cambios.");
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        IconUtils.applyIcon(btnCancelar, "clear.svg");
    }

    private void estilizarBotonMovimiento(JButton boton) {
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setFocusPainted(false);
        boton.setPreferredSize(new Dimension(150, 34));
        boton.setMinimumSize(new Dimension(150, 34));
    }

    private void configurarListasPremium() {
        configurarLista(lstDisponibles);
        configurarLista(lstAsignados);
    }

    private void configurarLista(JList<User> lista) {
        lista.setFixedCellHeight(48);
        lista.setVisibleRowCount(10);
        lista.setCellRenderer(new AbogadoListRenderer());
        lista.setSelectionBackground(new Color(219, 234, 254));
        lista.setSelectionForeground(new Color(15, 23, 42));
        lista.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        lista.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void registrarEventosUx() {
        lstDisponibles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !lstDisponibles.isSelectionEmpty()) {
                    moverSeleccionados(modelDisponibles, modelAsignados, lstDisponibles);
                }
            }
        });

        lstAsignados.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !lstAsignados.isSelectionEmpty()) {
                    moverSeleccionados(modelAsignados, modelDisponibles, lstAsignados);
                }
            }
        });

        ListSelectionListener listener = (ListSelectionEvent e) -> actualizarEstadoVisual();
        lstDisponibles.addListSelectionListener(listener);
        lstAsignados.addListSelectionListener(listener);
    }

    private void registrarActualizacionModelos() {
        ListDataListener listener = new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                actualizarEstadoVisual();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                actualizarEstadoVisual();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                actualizarEstadoVisual();
            }
        };
        modelDisponibles.addListDataListener(listener);
        modelAsignados.addListDataListener(listener);
    }

    private JPanel crearEncabezadoEquipo() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Gestionar equipo supervisado");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        titulo.setForeground(new Color(15, 23, 42));

        JLabel supervisor = new JLabel("<html><b>Supervisor:</b> " + textoHtml(lblSupervisor.getText()) + "</html>");
        supervisor.setFont(supervisor.getFont().deriveFont(Font.PLAIN, 14f));
        supervisor.setForeground(new Color(30, 41, 59));

        JLabel ayuda = new JLabel("Asigne o retire abogados del equipo del supervisor seleccionado.");
        ayuda.setFont(ayuda.getFont().deriveFont(Font.PLAIN, 13f));
        ayuda.setForeground(new Color(71, 85, 105));

        JPanel resumen = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        resumen.setOpaque(false);
        lblDisponiblesContador = crearBadgeResumen();
        lblAsignadosContador = crearBadgeResumen();
        resumen.add(lblDisponiblesContador);
        resumen.add(lblAsignadosContador);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(crearBordeCard(new Color(226, 232, 240)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(supervisor, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(ayuda, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(resumen, gbc);

        header.add(titulo);
        header.add(Box.createVerticalStrut(10));
        header.add(card);
        return header;
    }

    private JLabel crearBadgeResumen() {
        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setBackground(new Color(232, 241, 252));
        label.setForeground(new Color(30, 64, 175));
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        return label;
    }

    private JPanel crearCentroEquipo() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 12);

        gbc.gridx = 0;
        gbc.weightx = 0.48;
        center.add(crearPanelLista("Abogados disponibles", lstDisponibles), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 0, 12);
        center.add(crearPanelMovimiento(), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.48;
        gbc.insets = new Insets(0, 0, 0, 0);
        center.add(crearPanelLista("Abogados asignados", lstAsignados), gbc);

        return center;
    }

    private JPanel crearPanelLista(String titulo, JList<User> lista) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(crearBordeCard(new Color(226, 232, 240)));

        JLabel label = new JLabel(titulo);
        label.setForeground(new Color(15, 23, 42));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));

        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        panel.add(label, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelMovimiento() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalGlue());
        panel.add(btnAgregar);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnAgregarTodos);
        panel.add(Box.createVerticalStrut(16));
        panel.add(btnQuitar);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnQuitarTodos);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel crearBotoneraEquipo() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JLabel ayuda = new JLabel("Use los controles centrales o doble clic sobre un abogado para moverlo entre listas.");
        ayuda.setForeground(new Color(100, 116, 139));
        ayuda.setFont(ayuda.getFont().deriveFont(Font.PLAIN, 12f));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnGuardar);
        botones.add(btnCancelar);

        footer.add(ayuda, BorderLayout.WEST);
        footer.add(botones, BorderLayout.EAST);
        return footer;
    }

    private void actualizarEstadoVisual() {
        if (lblDisponiblesContador != null) {
            lblDisponiblesContador.setText("Disponibles: " + modelDisponibles.getSize());
        }
        if (lblAsignadosContador != null) {
            lblAsignadosContador.setText("Asignados: " + modelAsignados.getSize());
        }

        btnAgregar.setEnabled(!lstDisponibles.isSelectionEmpty());
        btnQuitar.setEnabled(!lstAsignados.isSelectionEmpty());
        btnAgregarTodos.setEnabled(modelDisponibles.getSize() > 0);
        btnQuitarTodos.setEnabled(modelAsignados.getSize() > 0);
    }

    private String textoHtml(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private javax.swing.border.Border crearBordeCard(Color color) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        );
    }

    private final class AbogadoListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            User user = value instanceof User ? (User) value : null;
            String nombre = user == null ? "" : textoHtml(user.getFullName());
            String usuario = user == null ? "" : textoHtml(user.getUsername());
            String texto = usuario.isEmpty()
                    ? nombre
                    : "<html><b>" + nombre + "</b><br><span style='color:#64748b;'>"
                    + usuario + "</span></html>";

            label.setText(texto);
            label.setToolTipText(user == null ? null : user.getFullName());
            label.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
            label.setFont(label.getFont().deriveFont(Font.PLAIN, 13f));
            label.setHorizontalAlignment(SwingConstants.LEFT);

            if (isSelected) {
                label.setBackground(new Color(219, 234, 254));
                label.setForeground(new Color(15, 23, 42));
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(new Color(30, 41, 59));
            }
            return label;
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

        btnCancelar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        lblSupervisor = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstAsignados = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstDisponibles = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        btnAgregarTodos = new javax.swing.JButton();
        btnAgregar = new javax.swing.JButton();
        btnQuitar = new javax.swing.JButton();
        btnQuitarTodos = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        btnCancelar.setText("CANCELAR");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        btnGuardar.setText("GUARDAR");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(lstAsignados);

        jLabel1.setText("Abogados disponibles");

        jScrollPane2.setViewportView(lstDisponibles);

        jLabel2.setText("Abogados asignados");

        btnAgregarTodos.setText(">>");
        btnAgregarTodos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarTodosActionPerformed(evt);
            }
        });

        btnAgregar.setText(">");
        btnAgregar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarActionPerformed(evt);
            }
        });

        btnQuitar.setText("<");
        btnQuitar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarActionPerformed(evt);
            }
        });

        btnQuitarTodos.setText("<<");
        btnQuitarTodos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarTodosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(54, 54, 54)
                                .addComponent(lblSupervisor, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(26, 26, 26)
                                .addComponent(jLabel2)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                            .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnCancelar)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(23, 23, 23))))
            .addGroup(layout.createSequentialGroup()
                .addGap(57, 57, 57)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAgregarTodos, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnQuitarTodos, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnQuitar, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(56, 56, 56))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSupervisor, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAgregar)
                    .addComponent(btnQuitar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAgregarTodos)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnGuardar)
                            .addComponent(btnCancelar))
                        .addGap(21, 21, 21))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnQuitarTodos)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
       
        List<Long> abogados = new ArrayList<>();

        for (int i = 0; i < modelAsignados.size(); i++) {
            abogados.add(modelAsignados.get(i).getUserId());
        }

        try {
            supervisionService.asignarAbogados(supervisorId, abogados);
            JOptionPane.showMessageDialog(this, "Equipo supervisado actualizado correctamente.");
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,e.getMessage());
        }
        
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        dispose();
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnAgregarTodosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarTodosActionPerformed
        moverTodos(modelDisponibles, modelAsignados);
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAgregarTodosActionPerformed

    private void btnQuitarTodosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarTodosActionPerformed
        moverTodos(modelAsignados, modelDisponibles);
        // TODO add your handling code here:
    }//GEN-LAST:event_btnQuitarTodosActionPerformed

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        // TODO add your handling code here:
        moverSeleccionados(modelDisponibles, modelAsignados, lstDisponibles);
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnQuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarActionPerformed
        // TODO add your handling code here:
        moverSeleccionados(modelAsignados, modelDisponibles, lstAsignados);
    }//GEN-LAST:event_btnQuitarActionPerformed

    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DlgAsignarAbogados.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DlgAsignarAbogados.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DlgAsignarAbogados.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DlgAsignarAbogados.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnAgregarTodos;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JButton btnQuitarTodos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblSupervisor;
    private javax.swing.JList lstAsignados;
    private javax.swing.JList lstDisponibles;
    // End of variables declaration//GEN-END:variables
}
