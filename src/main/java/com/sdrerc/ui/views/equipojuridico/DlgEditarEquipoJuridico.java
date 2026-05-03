package com.sdrerc.ui.views.equipojuridico;

import com.sdrerc.application.EquipoJuridicoConsultaService;
import com.sdrerc.application.EquipoJuridicoService;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.EquipoJuridicoDetalle;
import com.sdrerc.domain.model.EquipoJuridicoUpdateRequest;
import com.sdrerc.domain.model.SupervisorComboItem;
import com.sdrerc.shared.session.SessionContext;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DlgEditarEquipoJuridico extends JDialog {

    private final EquipoJuridicoConsultaService consultaService;
    private final EquipoJuridicoService equipoJuridicoService;
    private final Long abogadoId;
    private EquipoJuridicoDetalle detalle;
    private boolean guardado;

    private final JTextField txtUsername = new JTextField();
    private final JTextField txtNumeroDocumento = new JTextField();
    private final JTextField txtApellidoPaterno = new JTextField();
    private final JTextField txtApellidoMaterno = new JTextField();
    private final JTextField txtNombres = new JTextField();
    private final JComboBox<CatalogoItem> cboTipoPersonal = new JComboBox<>();
    private final JComboBox<SupervisorComboItem> cboSupervisor = new JComboBox<>();
    private final JComboBox<String> cboEstado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnCancelar = new JButton("Cancelar");

    public DlgEditarEquipoJuridico(
            Window parent,
            EquipoJuridicoConsultaService consultaService,
            EquipoJuridicoService equipoJuridicoService,
            Long abogadoId) {
        super(parent, "Editar abogado", ModalityType.APPLICATION_MODAL);
        this.consultaService = consultaService;
        this.equipoJuridicoService = equipoJuridicoService;
        this.abogadoId = abogadoId;
        construirInterfaz();
        configurarEventos();
        cargarDatos();
        pack();
        setMinimumSize(new Dimension(540, 430));
    }

    public boolean isGuardado() {
        return guardado;
    }

    private void construirInterfaz() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 18, 12, 18));

        txtUsername.setEditable(false);
        txtUsername.setFocusable(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        agregarCampo(panel, gbc, 0, "Username", txtUsername);
        agregarCampo(panel, gbc, 1, "Numero documento", txtNumeroDocumento);
        agregarCampo(panel, gbc, 2, "Apellido paterno", txtApellidoPaterno);
        agregarCampo(panel, gbc, 3, "Apellido materno", txtApellidoMaterno);
        agregarCampo(panel, gbc, 4, "Nombres", txtNombres);
        agregarCombo(panel, gbc, 5, "Tipo personal", cboTipoPersonal);
        agregarCombo(panel, gbc, 6, "Supervisor", cboSupervisor);
        agregarCombo(panel, gbc, 7, "Estado", cboEstado);

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
        field.setPreferredSize(new Dimension(300, 30));
        panel.add(field, gbc);
    }

    private void agregarCombo(JPanel panel, GridBagConstraints gbc, int row, String label, JComboBox<?> combo) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        combo.setPreferredSize(new Dimension(300, 30));
        panel.add(combo, gbc);
    }

    private void configurarEventos() {
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
    }

    private void cargarDatos() {
        try {
            detalle = consultaService.obtenerDetalleAbogado(abogadoId);
            cargarTiposPersonal();
            cargarSupervisores();
            pintarDetalle();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void cargarTiposPersonal() throws Exception {
        cboTipoPersonal.removeAllItems();
        cboTipoPersonal.addItem(new CatalogoItem(0, 0, "Seleccione", 1));
        for (CatalogoItem tipo : equipoJuridicoService.listarTiposPersonalOficiales()) {
            cboTipoPersonal.addItem(tipo);
        }
    }

    private void cargarSupervisores() throws Exception {
        cboSupervisor.removeAllItems();
        cboSupervisor.addItem(SupervisorComboItem.sinSupervisor());
        List<SupervisorComboItem> supervisores = consultaService.listarSupervisoresActivos();
        for (SupervisorComboItem supervisor : supervisores) {
            cboSupervisor.addItem(supervisor);
        }
    }

    private void pintarDetalle() {
        txtUsername.setText(valor(detalle.getUsername()));
        txtNumeroDocumento.setText(detalle.getNumeroDocumento() == null ? "" : String.valueOf(detalle.getNumeroDocumento()));
        txtApellidoPaterno.setText(valor(detalle.getApellidoPaterno()));
        txtApellidoMaterno.setText(valor(detalle.getApellidoMaterno()));
        txtNombres.setText(valor(detalle.getNombres()));
        seleccionarTipoPersonal(detalle.getIdTipoPersonal());
        seleccionarSupervisor(detalle.getSupervisorId());
        cboEstado.setSelectedItem(esEstadoActivo(detalle.getEstado(), detalle.getTecnicoActive()) ? "ACTIVO" : "INACTIVO");
    }

    private void seleccionarTipoPersonal(Integer idTipoPersonal) {
        if (idTipoPersonal == null) {
            cboTipoPersonal.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < cboTipoPersonal.getItemCount(); i++) {
            CatalogoItem item = cboTipoPersonal.getItemAt(i);
            if (item != null && item.getIdCatalogoItem() == idTipoPersonal) {
                cboTipoPersonal.setSelectedIndex(i);
                return;
            }
        }
        cboTipoPersonal.setSelectedIndex(0);
    }

    private void seleccionarSupervisor(Long supervisorId) {
        if (supervisorId == null) {
            cboSupervisor.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < cboSupervisor.getItemCount(); i++) {
            SupervisorComboItem item = cboSupervisor.getItemAt(i);
            if (item != null && supervisorId.equals(item.getUserId())) {
                cboSupervisor.setSelectedIndex(i);
                return;
            }
        }
        cboSupervisor.setSelectedIndex(0);
    }

    private void guardar() {
        try {
            EquipoJuridicoUpdateRequest request = construirRequest();
            consultaService.actualizarEquipoJuridico(request);
            guardado = true;
            JOptionPane.showMessageDialog(this, "Abogado actualizado correctamente.");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private EquipoJuridicoUpdateRequest construirRequest() {
        validarCampos();

        EquipoJuridicoUpdateRequest request = new EquipoJuridicoUpdateRequest();
        request.setAbogadoId(detalle.getAbogadoId());
        request.setIdTecnico(detalle.getIdTecnico());
        request.setApellidoPaterno(txtApellidoPaterno.getText().trim());
        request.setApellidoMaterno(trimToNull(txtApellidoMaterno.getText()));
        request.setNombres(txtNombres.getText().trim());
        request.setNumeroDocumento(parseNumeroDocumento());

        CatalogoItem tipoPersonal = (CatalogoItem) cboTipoPersonal.getSelectedItem();
        if (tipoPersonal != null && tipoPersonal.getIdCatalogoItem() > 0) {
            request.setIdTipoPersonal(tipoPersonal.getIdCatalogoItem());
        }

        SupervisorComboItem supervisor = (SupervisorComboItem) cboSupervisor.getSelectedItem();
        if (supervisor != null && SupervisorComboItem.TIPO_SUPERVISOR.equals(supervisor.getTipo())) {
            if (supervisor.getUserId().equals(detalle.getAbogadoId())) {
                throw new IllegalArgumentException("El abogado no puede ser su propio supervisor.");
            }
            request.setSupervisorId(supervisor.getUserId());
        }

        request.setEstado("ACTIVO".equals(cboEstado.getSelectedItem()) ? "ACTIVE" : "INACTIVE");
        request.setUsuarioModificacion(obtenerUsuarioModificacion());
        return request;
    }

    private void validarCampos() {
        if (isBlank(txtApellidoPaterno.getText())) {
            throw new IllegalArgumentException("Ingrese apellido paterno.");
        }
        if (isBlank(txtNombres.getText())) {
            throw new IllegalArgumentException("Ingrese nombres.");
        }
        if (!isBlank(txtNumeroDocumento.getText()) && !txtNumeroDocumento.getText().trim().matches("\\d+")) {
            throw new IllegalArgumentException("El numero de documento debe ser numerico.");
        }
        if (cboEstado.getSelectedItem() == null) {
            throw new IllegalArgumentException("Seleccione estado.");
        }
    }

    private Long parseNumeroDocumento() {
        if (isBlank(txtNumeroDocumento.getText())) {
            return null;
        }
        return Long.valueOf(txtNumeroDocumento.getText().trim());
    }

    private String obtenerUsuarioModificacion() {
        try {
            return SessionContext.getUsername();
        } catch (Exception ex) {
            return "SDRERC";
        }
    }

    private boolean esEstadoActivo(String estado, Integer tecnicoActive) {
        if ("ACTIVE".equalsIgnoreCase(estado) || "ACTIVO".equalsIgnoreCase(estado)) {
            return true;
        }
        if ("INACTIVE".equalsIgnoreCase(estado) || "INACTIVO".equalsIgnoreCase(estado)) {
            return false;
        }
        return tecnicoActive == null || tecnicoActive == 1;
    }

    private String valor(String value) {
        return value == null ? "" : value;
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
