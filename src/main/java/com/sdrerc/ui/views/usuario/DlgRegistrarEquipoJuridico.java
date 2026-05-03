package com.sdrerc.ui.views.usuario;

import com.sdrerc.application.EquipoJuridicoService;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.EquipoJuridicoRegistro;
import com.sdrerc.domain.model.User;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class DlgRegistrarEquipoJuridico extends JDialog {

    private final EquipoJuridicoService equipoJuridicoService;
    private final JTextField txtNumeroDocumento = new JTextField();
    private final JTextField txtApellidoPaterno = new JTextField();
    private final JTextField txtApellidoMaterno = new JTextField();
    private final JTextField txtNombres = new JTextField();
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField pwdTemporal = new JPasswordField();
    private final JComboBox<CatalogoItem> cboTipoPersonal = new JComboBox<>();
    private final JCheckBox chkAbogado = new JCheckBox("ABOGADO");
    private final JCheckBox chkSupervision = new JCheckBox("SUPERVISION");
    private final JComboBox<SupervisorItem> cboSupervisor = new JComboBox<>();
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnCancelar = new JButton("Cancelar");

    public DlgRegistrarEquipoJuridico(Window parent, EquipoJuridicoService equipoJuridicoService) {
        super(parent, "Nuevo abogado/supervisor", ModalityType.APPLICATION_MODAL);
        this.equipoJuridicoService = equipoJuridicoService;
        construirInterfaz();
        configurarEventos();
        cargarTiposPersonal();
        cargarSupervisores();
        pack();
        setMinimumSize(new Dimension(540, 470));
    }

    private void construirInterfaz() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 18, 12, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        agregarCampo(panel, gbc, 0, "Número documento", txtNumeroDocumento);
        agregarCampo(panel, gbc, 1, "Apellido paterno", txtApellidoPaterno);
        agregarCampo(panel, gbc, 2, "Apellido materno", txtApellidoMaterno);
        agregarCampo(panel, gbc, 3, "Nombres", txtNombres);
        agregarCombo(panel, gbc, 4, "Tipo personal", cboTipoPersonal);
        agregarCampo(panel, gbc, 5, "Username", txtUsername);
        agregarCampo(panel, gbc, 6, "Contraseña temporal", pwdTemporal);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 0;
        panel.add(new JLabel("Rol operativo"), gbc);

        JPanel rolesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rolesPanel.add(chkAbogado);
        rolesPanel.add(chkSupervision);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(rolesPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weightx = 0;
        panel.add(new JLabel("Supervisor principal"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(cboSupervisor, gbc);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnGuardar);
        botones.add(btnCancelar);

        add(panel, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        field.setPreferredSize(new Dimension(260, 30));
        panel.add(field, gbc);
    }

    private void agregarCombo(JPanel panel, GridBagConstraints gbc, int row, String label, JComboBox<?> combo) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        combo.setPreferredSize(new Dimension(260, 30));
        panel.add(combo, gbc);
    }

    private void configurarEventos() {
        chkAbogado.addActionListener(e -> actualizarSupervisorHabilitado());
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
        actualizarSupervisorHabilitado();
    }

    private void cargarSupervisores() {
        cboSupervisor.removeAllItems();
        cboSupervisor.addItem(new SupervisorItem(null, "Sin supervisor"));
        try {
            List<User> supervisores = equipoJuridicoService.listarSupervisoresActivos();
            for (User supervisor : supervisores) {
                cboSupervisor.addItem(new SupervisorItem(supervisor.getUserId(), supervisor.getFullName()));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTiposPersonal() {
        cboTipoPersonal.removeAllItems();
        cboTipoPersonal.addItem(new CatalogoItem(0, 0, "Seleccione", 1));
        try {
            for (CatalogoItem tipo : equipoJuridicoService.listarTiposPersonalOficiales()) {
                cboTipoPersonal.addItem(tipo);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar tipo de personal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarSupervisorHabilitado() {
        cboSupervisor.setEnabled(chkAbogado.isSelected());
    }

    private void guardar() {
        try {
            EquipoJuridicoRegistro registro = new EquipoJuridicoRegistro();
            registro.setNumeroDocumento(txtNumeroDocumento.getText());
            registro.setApellidoPaterno(txtApellidoPaterno.getText());
            registro.setApellidoMaterno(txtApellidoMaterno.getText());
            registro.setNombres(txtNombres.getText());
            registro.setUsername(txtUsername.getText());
            registro.setPasswordTemporal(new String(pwdTemporal.getPassword()));
            registro.setAbogado(chkAbogado.isSelected());
            registro.setSupervision(chkSupervision.isSelected());
            CatalogoItem tipoPersonal = (CatalogoItem) cboTipoPersonal.getSelectedItem();
            if (tipoPersonal != null && tipoPersonal.getIdCatalogoItem() > 0) {
                registro.setIdTipoPersonal(tipoPersonal.getIdCatalogoItem());
            }

            SupervisorItem supervisor = (SupervisorItem) cboSupervisor.getSelectedItem();
            if (chkAbogado.isSelected() && supervisor != null) {
                registro.setSupervisorId(supervisor.getUserId());
            }

            equipoJuridicoService.registrar(registro);
            JOptionPane.showMessageDialog(this, "Equipo jurídico registrado correctamente.");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static class SupervisorItem {
        private final Long userId;
        private final String nombre;

        SupervisorItem(Long userId, String nombre) {
            this.userId = userId;
            this.nombre = nombre;
        }

        Long getUserId() {
            return userId;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }
}
