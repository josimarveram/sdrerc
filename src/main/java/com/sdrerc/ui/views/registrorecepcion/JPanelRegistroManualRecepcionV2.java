package com.sdrerc.ui.views.registrorecepcion;

import com.sdrerc.application.sdrercapp.CatalogoLookupService;
import com.sdrerc.application.sdrercapp.RegistroManualExpedienteService;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosActaDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosPersonaRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosSolicitudDTO;
import com.sdrerc.domain.dto.sdrercapp.RegistroManualExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.RegistroManualResultadoDTO;
import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.PremiumDateFieldV2;
import com.sdrerc.ui.appv2.helpers.FiltroCatalogoItemV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class JPanelRegistroManualRecepcionV2 extends JPanel {

    private final CatalogoLookupService catalogoService = new CatalogoLookupService();
    private final RegistroManualExpedienteService registroService = new RegistroManualExpedienteService();
    private final Runnable onRegistroConfirmado;

    private final JTextField txtNumeroTramite = new JTextField();
    private final PremiumDateFieldV2 fechaRecepcionField = new PremiumDateFieldV2();
    private final JRadioButton rdoCorrespondeSdrerc = new JRadioButton("Sí corresponde a la SDRERC", true);
    private final JRadioButton rdoNoCorrespondeSdrerc = new JRadioButton("No corresponde a la SDRERC");
    private final ButtonGroup grupoValidacionInicial = new ButtonGroup();
    private final JTextField txtHojaEnvio = new JTextField();
    private final JComboBox<FiltroCatalogoItemV2> cmbProcedimiento = comboBase("Seleccione procedimiento");
    private final JComboBox<FiltroCatalogoItemV2> cmbTipoDocumento = comboBase("Seleccione tipo documento");
    private final JComboBox<FiltroCatalogoItemV2> cmbCanal = comboBase("Seleccione canal");
    private final JComboBox<FiltroCatalogoItemV2> cmbPrioridad = new JComboBox<FiltroCatalogoItemV2>(new FiltroCatalogoItemV2[]{
        new FiltroCatalogoItemV2("NORMAL", "Normal"),
        new FiltroCatalogoItemV2("ALTA", "Alta"),
        new FiltroCatalogoItemV2("URGENTE", "Urgente")
    });

    private final JComboBox<FiltroCatalogoItemV2> cmbTipoActa = comboBase("Seleccione tipo acta");
    private final JTextField txtNumeroActa = new JTextField();

    private final JTextField txtTitularNombre = new JTextField();
    private final JComboBox<FiltroCatalogoItemV2> cmbTitularTipoDoc = new JComboBox<FiltroCatalogoItemV2>(crearTiposDocumentoTitular());
    private final JTextField txtTitularDocumento = new JTextField();

    private final JTextField txtRemitenteNombre = new JTextField();
    private final JComboBox<FiltroCatalogoItemV2> cmbRemitenteTipoDoc = new JComboBox<FiltroCatalogoItemV2>(crearTiposDocumentoRemitente());
    private final JTextField txtRemitenteDocumento = new JTextField();

    private final JTextArea txtErrores = area(5);
    private final JTextArea txtResumen = area(8);
    private final JLabel lblEstado = new JLabel("Complete los datos y presione Validar.");
    private final JLabel lblNumeroExpediente = new JLabel("Pendiente de generación al guardar");
    private final JButton btnValidar = new JButton("Validar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRegistrar = new JButton("Registrar expediente");

    private boolean trabajando;
    private boolean validado;
    private RegistroManualExpedienteDTO registroValidado;

    public JPanelRegistroManualRecepcionV2(Runnable onRegistroConfirmado) {
        this.onRegistroConfirmado = onRegistroConfirmado;
        setLayout(new BorderLayout(12, 12));
        setBackground(AppV2Theme.BACKGROUND);
        add(crearHeader(), BorderLayout.NORTH);
        add(crearFormulario(), BorderLayout.CENTER);
        add(crearFooter(), BorderLayout.SOUTH);
        configurarEstadoInicial();
        configurarEventos();
        cargarCatalogos();
    }

    private JPanel crearHeader() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.cardBorder());

        JLabel title = new JLabel("Registro manual de expediente");
        title.setFont(AppV2Theme.fontBold(18));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Complete los datos de solicitud, acta, titular y remitente antes de registrar el expediente.");
        subtitle.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        subtitle.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel text = new JPanel(new BorderLayout(0, 4));
        text.setOpaque(false);
        text.add(title, BorderLayout.NORTH);
        text.add(subtitle, BorderLayout.CENTER);
        panel.add(text, BorderLayout.CENTER);
        panel.add(new BadgeV2("Escritura controlada", AppV2Theme.SOFT_GREEN, AppV2Theme.SUCCESS), BorderLayout.EAST);
        return panel;
    }

    private JScrollPane crearFormulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 12, 12);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 12, 0);
        form.add(crearValidacionInicial(), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 12, 12);
        form.add(crearDatosSolicitud(), gbc);
        gbc.gridx = 1;
        form.add(crearDatosActa(), gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        form.add(crearTitular(), gbc);
        gbc.gridx = 1;
        form.add(crearRemitente(), gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 12, 0);
        form.add(crearResumenConfirmacion(), gbc);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel crearValidacionInicial() {
        JPanel panel = seccion("Validación inicial");
        JPanel opciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        opciones.setOpaque(false);
        configurarRadio(rdoCorrespondeSdrerc);
        configurarRadio(rdoNoCorrespondeSdrerc);
        grupoValidacionInicial.add(rdoCorrespondeSdrerc);
        grupoValidacionInicial.add(rdoNoCorrespondeSdrerc);
        opciones.add(rdoCorrespondeSdrerc);
        opciones.add(rdoNoCorrespondeSdrerc);
        agregarFila(panel, 0, "Resultado inicial *", opciones);
        agregarFila(panel, 1, "Hoja de envío", txtHojaEnvio);
        return panel;
    }

    private JPanel crearDatosSolicitud() {
        JPanel panel = seccion("Datos de solicitud");
        agregarFila(panel, 0, "Nro. trámite web *", txtNumeroTramite);
        agregarFila(panel, 1, "Fecha recepción *", fechaRecepcionField);
        agregarFila(panel, 2, "Procedimiento registral *", cmbProcedimiento);
        agregarFila(panel, 3, "Tipo documento *", cmbTipoDocumento);
        agregarFila(panel, 4, "Canal de ingreso", cmbCanal);
        agregarFila(panel, 5, "Prioridad", cmbPrioridad);
        return panel;
    }

    private JPanel crearDatosActa() {
        JPanel panel = seccion("Datos del acta");
        agregarFila(panel, 0, "Tipo de acta", cmbTipoActa);
        agregarFila(panel, 1, "Nro. acta *", txtNumeroActa);
        return panel;
    }

    private JPanel crearTitular() {
        JPanel panel = seccion("Titular");
        agregarFila(panel, 0, "Nombres *", txtTitularNombre);
        agregarFila(panel, 1, "Tipo documento", cmbTitularTipoDoc);
        agregarFila(panel, 2, "Número documento", txtTitularDocumento);
        return panel;
    }

    private JPanel crearRemitente() {
        JPanel panel = seccion("Remitente");
        agregarFila(panel, 0, "Nombres / Razón Social *", txtRemitenteNombre);
        agregarFila(panel, 1, "Tipo documento", cmbRemitenteTipoDoc);
        agregarFila(panel, 2, "Número documento", txtRemitenteDocumento);
        return panel;
    }

    private JPanel crearResumenConfirmacion() {
        JPanel resumen = seccion("Resumen y confirmación");
        lblNumeroExpediente.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        lblNumeroExpediente.setForeground(AppV2Theme.INFO);
        txtErrores.setEditable(false);
        txtResumen.setEditable(false);
        txtErrores.setBackground(AppV2Theme.SOFT_RED);
        txtResumen.setBackground(AppV2Theme.SURFACE_ALT);
        agregarFila(resumen, 0, "Número expediente", lblNumeroExpediente);
        agregarFila(resumen, 1, "Errores / advertencias", scrollArea(txtErrores));
        agregarFila(resumen, 2, "Resumen", scrollArea(txtResumen));
        return resumen;
    }

    private JPanel crearFooter() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnValidar);
        acciones.add(btnLimpiar);
        acciones.add(btnRegistrar);

        panel.add(lblEstado, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.EAST);
        return panel;
    }

    private JPanel seccion(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.sectionBorder());
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppV2Theme.fontBold(17));
        lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);
        panel.add(lblTitle, gbc);
        return panel;
    }

    private void agregarFila(JPanel panel, int row, String label, Component component) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = row + 1;
        gbcLabel.anchor = GridBagConstraints.NORTHWEST;
        gbcLabel.insets = new Insets(5, 0, 5, 12);

        GridBagConstraints gbcValue = new GridBagConstraints();
        gbcValue.gridx = 1;
        gbcValue.gridy = row + 1;
        gbcValue.weightx = 1;
        gbcValue.fill = GridBagConstraints.HORIZONTAL;
        gbcValue.insets = new Insets(5, 0, 5, 0);

        JLabel lbl = new JLabel(label);
        lbl.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lbl.setForeground(AppV2Theme.TEXT_SECONDARY);
        panel.add(lbl, gbcLabel);
        panel.add(component, gbcValue);
    }

    private JScrollPane scrollArea(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scroll.setPreferredSize(new Dimension(240, Math.max(70, area.getRows() * 22)));
        return scroll;
    }

    private void configurarEstadoInicial() {
        configurarCampo(txtNumeroTramite);
        fechaRecepcionField.setDate(toDate(LocalDate.now()));
        configurarCampo(txtHojaEnvio);
        configurarCampo(txtNumeroActa);
        configurarCampo(txtTitularNombre);
        configurarCampo(txtTitularDocumento);
        configurarCampo(txtRemitenteNombre);
        configurarCampo(txtRemitenteDocumento);
        configurarCombo(cmbProcedimiento);
        configurarCombo(cmbTipoDocumento);
        configurarCombo(cmbCanal);
        configurarCombo(cmbPrioridad);
        configurarCombo(cmbTipoActa);
        configurarCombo(cmbTitularTipoDoc);
        configurarCombo(cmbRemitenteTipoDoc);
        actualizarEstadoHojaEnvio();
        txtResumen.setText("Validación pendiente.");
        txtErrores.setText("");
        btnRegistrar.setEnabled(false);
    }

    private void configurarCampo(JTextField field) {
        field.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
    }

    private void configurarCombo(JComboBox<FiltroCatalogoItemV2> combo) {
        combo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        combo.setPreferredSize(new Dimension(260, 34));
    }

    private void configurarRadio(JRadioButton radio) {
        radio.setOpaque(false);
        radio.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        radio.setForeground(AppV2Theme.TEXT_PRIMARY);
    }

    private void configurarEventos() {
        btnValidar.addActionListener(e -> validarFormulario());
        btnLimpiar.addActionListener(e -> limpiar());
        btnRegistrar.addActionListener(e -> registrarExpediente());
        registrarInvalidacion();
    }

    private void registrarInvalidacion() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                invalidarValidacion();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                invalidarValidacion();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                invalidarValidacion();
            }
        };
        for (JTextField field : camposTexto()) {
            field.getDocument().addDocumentListener(listener);
        }
        for (JTextArea area : areasTexto()) {
            area.getDocument().addDocumentListener(listener);
        }
        for (JComboBox<FiltroCatalogoItemV2> combo : combos()) {
            combo.addActionListener(e -> invalidarValidacion());
        }
        fechaRecepcionField.addDateChangeListener(e -> invalidarValidacion());
        rdoCorrespondeSdrerc.addActionListener(e -> invalidarValidacion());
        rdoNoCorrespondeSdrerc.addActionListener(e -> invalidarValidacion());
        rdoCorrespondeSdrerc.addActionListener(e -> actualizarEstadoHojaEnvio());
        rdoNoCorrespondeSdrerc.addActionListener(e -> actualizarEstadoHojaEnvio());
    }

    private List<JTextField> camposTexto() {
        List<JTextField> fields = new ArrayList<JTextField>();
        fields.add(txtNumeroTramite);
        fields.add(txtHojaEnvio);
        fields.add(txtNumeroActa);
        fields.add(txtTitularNombre);
        fields.add(txtTitularDocumento);
        fields.add(txtRemitenteNombre);
        fields.add(txtRemitenteDocumento);
        return fields;
    }

    private List<JTextArea> areasTexto() {
        return new ArrayList<JTextArea>();
    }

    private List<JComboBox<FiltroCatalogoItemV2>> combos() {
        List<JComboBox<FiltroCatalogoItemV2>> comboList = new ArrayList<JComboBox<FiltroCatalogoItemV2>>();
        comboList.add(cmbProcedimiento);
        comboList.add(cmbTipoDocumento);
        comboList.add(cmbCanal);
        comboList.add(cmbPrioridad);
        comboList.add(cmbTipoActa);
        comboList.add(cmbTitularTipoDoc);
        comboList.add(cmbRemitenteTipoDoc);
        return comboList;
    }

    private void cargarCatalogos() {
        lblEstado.setText("Cargando catálogos de SDRERC_APP...");
        SwingWorker<CatalogosFormulario, Void> worker = new SwingWorker<CatalogosFormulario, Void>() {
            @Override
            protected CatalogosFormulario doInBackground() throws Exception {
                return new CatalogosFormulario(
                        catalogoService.listarCanalesRecepcion(),
                        catalogoService.listarProcedimientosRegistrales(),
                        catalogoService.listarTiposDocumento(),
                        catalogoService.listarTiposActa());
            }

            @Override
            protected void done() {
                try {
                    CatalogosFormulario catalogos = get();
                    aplicarCatalogos(catalogos);
                    lblEstado.setText("Catálogos cargados. Complete el formulario y presione Validar.");
                } catch (Exception ex) {
                    lblEstado.setText("No se pudieron cargar catálogos. Puede usar texto libre donde corresponda.");
                }
            }
        };
        worker.execute();
    }

    private void aplicarCatalogos(CatalogosFormulario catalogos) {
        agregarItems(cmbCanal, catalogos.canales);
        agregarItems(cmbProcedimiento, catalogos.procedimientos);
        agregarItems(cmbTipoDocumento, catalogos.tiposDocumento);
        agregarItems(cmbTipoActa, catalogos.tiposActa);
    }

    private void agregarItems(JComboBox<FiltroCatalogoItemV2> combo, List<CatalogoItemDTO> items) {
        if (items == null) {
            return;
        }
        for (CatalogoItemDTO item : items) {
            combo.addItem(new FiltroCatalogoItemV2(item.getCodigo(), item.getNombre()));
        }
    }

    private void validarFormulario() {
        RegistroManualExpedienteDTO dto = construirRegistro();
        List<String> errores = registroService.validar(dto);
        if (errores.isEmpty()) {
            dto.setNumeroExpedienteVistaPrevia("Pendiente de generación al guardar");
            validado = true;
            registroValidado = dto;
            txtErrores.setBackground(AppV2Theme.SOFT_GREEN);
            txtErrores.setText("Sin errores críticos. Listo para registrar.");
            txtResumen.setText(resumen(dto));
            lblNumeroExpediente.setText(dto.getNumeroExpedienteVistaPrevia());
            lblEstado.setText("Formulario validado. Revise el resumen y registre el expediente.");
            btnRegistrar.setEnabled(!trabajando);
        } else {
            validado = false;
            registroValidado = null;
            txtErrores.setBackground(AppV2Theme.SOFT_RED);
            txtErrores.setText(String.join("\n", errores));
            txtResumen.setText("Corrija los errores antes de registrar.");
            lblNumeroExpediente.setText("Pendiente de generación al guardar");
            lblEstado.setText("Se encontraron errores de validación.");
            btnRegistrar.setEnabled(false);
        }
    }

    private void registrarExpediente() {
        if (!validado || registroValidado == null) {
            validarFormulario();
            if (!validado || registroValidado == null) {
                return;
            }
        }
        int option = JOptionPane.showConfirmDialog(
                this,
                "Se registrará un expediente en SDRERC_APP.\n\n" + resumen(registroValidado) + "\n¿Desea continuar?",
                "Confirmar registro manual",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        setTrabajando(true, "Registrando expediente en SDRERC_APP...");
        SwingWorker<RegistroManualResultadoDTO, Void> worker = new SwingWorker<RegistroManualResultadoDTO, Void>() {
            @Override
            protected RegistroManualResultadoDTO doInBackground() throws Exception {
                return registroService.registrar(registroValidado);
            }

            @Override
            protected void done() {
                try {
                    RegistroManualResultadoDTO resultado = get();
                    lblNumeroExpediente.setText(resultado.getNumeroExpediente());
                    lblEstado.setText(resultado.getMensaje());
                    txtResumen.setText(resultado.getMensaje() + "\n\n" + txtResumen.getText());
                    JOptionPane.showMessageDialog(
                            JPanelRegistroManualRecepcionV2.this,
                            resultado.getMensaje(),
                            "Registro manual confirmado",
                            JOptionPane.INFORMATION_MESSAGE);
                    validado = false;
                    btnRegistrar.setEnabled(false);
                    if (onRegistroConfirmado != null) {
                        onRegistroConfirmado.run();
                    }
                    preguntarLimpiar();
                } catch (Exception ex) {
                    mostrarError("No se pudo registrar el expediente. La transacción fue revertida.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private RegistroManualExpedienteDTO construirRegistro() {
        RegistroManualExpedienteDTO dto = new RegistroManualExpedienteDTO();
        DatosSolicitudDTO solicitud = new DatosSolicitudDTO();
        solicitud.setNumeroTramite(txtNumeroTramite.getText());
        solicitud.setFechaRecepcion(localDate(fechaRecepcionField.getDate()));
        solicitud.setValidacionInicial(valorValidacionInicial());
        solicitud.setHojaEnvio(txtHojaEnvio.getText());
        solicitud.setTipoProcedimientoCodigo(codigo(cmbProcedimiento));
        solicitud.setTipoProcedimientoNombre(nombreSeleccionadoConCodigo(cmbProcedimiento));
        solicitud.setTipoDocumentoCodigo(codigo(cmbTipoDocumento));
        solicitud.setTipoDocumentoNombre(nombreSeleccionadoConCodigo(cmbTipoDocumento));
        solicitud.setCanalCodigo(codigo(cmbCanal));
        solicitud.setCanalNombre(nombre(cmbCanal));
        solicitud.setPrioridad(codigo(cmbPrioridad));
        dto.setSolicitud(solicitud);

        DatosActaDTO acta = new DatosActaDTO();
        acta.setTipoActaCodigo(codigo(cmbTipoActa));
        acta.setTipoActaNombre(nombreSeleccionadoConCodigo(cmbTipoActa));
        acta.setNumeroActa(txtNumeroActa.getText());
        dto.setActa(acta);

        DatosPersonaRegistroDTO titular = new DatosPersonaRegistroDTO();
        titular.setNombreCompleto(txtTitularNombre.getText());
        titular.setTipoDocumento(codigo(cmbTitularTipoDoc));
        titular.setNumeroDocumento(txtTitularDocumento.getText());
        dto.setTitular(titular);

        DatosPersonaRegistroDTO remitente = new DatosPersonaRegistroDTO();
        remitente.setNombreCompleto(txtRemitenteNombre.getText());
        remitente.setTipoDocumento(codigo(cmbRemitenteTipoDoc));
        remitente.setNumeroDocumento(txtRemitenteDocumento.getText());
        dto.setRemitente(remitente);
        return dto;
    }

    private String resumen(RegistroManualExpedienteDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("Trámite: ").append(safe(dto.getSolicitud().getNumeroTramite())).append("\n");
        sb.append("Titular: ").append(safe(dto.getTitular().getNombreCompleto())).append("\n");
        sb.append("Procedimiento: ").append(safe(dto.getSolicitud().getTipoProcedimientoNombre())).append("\n");
        sb.append("Acta: ").append(safe(dto.getActa().getNumeroActa())).append("\n");
        sb.append("Remitente: ").append(safe(dto.getRemitente().getNombreCompleto())).append("\n");
        sb.append("Validación inicial: ").append(safe(dto.getSolicitud().getValidacionInicial())).append("\n");
        if (requiereHojaEnvio(dto.getSolicitud().getValidacionInicial())) {
            sb.append("Hoja de envío: ").append(safe(dto.getSolicitud().getHojaEnvio())).append("\n");
        }
        sb.append("Número expediente: ").append(safe(dto.getNumeroExpedienteVistaPrevia()));
        return sb.toString();
    }

    private void preguntarLimpiar() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "¿Desea limpiar el formulario para registrar otro expediente?",
                "Registro / Recepción V2",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
            limpiar();
        }
    }

    private void limpiar() {
        for (JTextField field : camposTexto()) {
            field.setText("");
        }
        for (JTextArea area : areasTexto()) {
            area.setText("");
        }
        fechaRecepcionField.setDate(toDate(LocalDate.now()));
        rdoCorrespondeSdrerc.setSelected(true);
        actualizarEstadoHojaEnvio();
        seleccionarPrimero(cmbProcedimiento);
        seleccionarPrimero(cmbTipoDocumento);
        seleccionarPrimero(cmbCanal);
        seleccionarPrimero(cmbTipoActa);
        seleccionarPrimero(cmbTitularTipoDoc);
        cmbPrioridad.setSelectedIndex(0);
        cmbRemitenteTipoDoc.setSelectedIndex(0);
        txtErrores.setText("");
        txtResumen.setText("Validación pendiente.");
        lblNumeroExpediente.setText("Pendiente de generación al guardar");
        lblEstado.setText("Complete los datos y presione Validar.");
        validado = false;
        registroValidado = null;
        btnRegistrar.setEnabled(false);
    }

    private void seleccionarPrimero(JComboBox<FiltroCatalogoItemV2> combo) {
        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    private void invalidarValidacion() {
        if (trabajando) {
            return;
        }
        validado = false;
        registroValidado = null;
        btnRegistrar.setEnabled(false);
        lblNumeroExpediente.setText("Pendiente de generación al guardar");
        lblEstado.setText("Cambios pendientes de validación.");
    }

    private void actualizarEstadoHojaEnvio() {
        boolean habilitado = rdoNoCorrespondeSdrerc.isSelected();
        txtHojaEnvio.setEnabled(habilitado);
        txtHojaEnvio.setEditable(habilitado);
        if (!habilitado) {
            txtHojaEnvio.setText("");
        }
    }

    private void setTrabajando(boolean trabajando, String mensaje) {
        this.trabajando = trabajando;
        btnValidar.setEnabled(!trabajando);
        btnLimpiar.setEnabled(!trabajando);
        btnRegistrar.setEnabled(!trabajando && validado);
        if (mensaje != null) {
            lblEstado.setText(mensaje);
        }
    }

    private void mostrarError(String titulo, Exception ex) {
        String message = extraerMensajeUsuario(ex);
        if (message == null || message.trim().isEmpty()) {
            message = "Revise los datos ingresados o la conexión de SDRERC_APP.";
        }
        txtErrores.setBackground(AppV2Theme.SOFT_RED);
        txtErrores.setText(message);
        lblEstado.setText(titulo);
        JOptionPane.showMessageDialog(this, titulo + "\n" + message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String extraerMensajeUsuario(Throwable throwable) {
        Throwable actual = throwable;
        while (actual != null && actual.getCause() != null) {
            actual = actual.getCause();
        }
        if (actual == null || actual.getMessage() == null) {
            return null;
        }
        return actual.getMessage().replaceFirst("^java\\.[a-zA-Z0-9_.]+:\\s*", "").trim();
    }

    private static JTextArea area(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        area.setBorder(BorderFactory.createEmptyBorder(7, 9, 7, 9));
        return area;
    }

    private static JComboBox<FiltroCatalogoItemV2> comboBase(String label) {
        return new JComboBox<FiltroCatalogoItemV2>(new FiltroCatalogoItemV2[]{
            new FiltroCatalogoItemV2(null, label)
        });
    }

    private static FiltroCatalogoItemV2[] crearTiposDocumentoTitular() {
        return new FiltroCatalogoItemV2[]{
            new FiltroCatalogoItemV2(null, "No definido"),
            new FiltroCatalogoItemV2("DNI", "DNI"),
            new FiltroCatalogoItemV2("CE", "Carné de extranjería"),
            new FiltroCatalogoItemV2("PASAPORTE", "Pasaporte")
        };
    }

    private static FiltroCatalogoItemV2[] crearTiposDocumentoRemitente() {
        return new FiltroCatalogoItemV2[]{
            new FiltroCatalogoItemV2(null, "No definido"),
            new FiltroCatalogoItemV2("DNI", "DNI"),
            new FiltroCatalogoItemV2("CE", "Carné de extranjería"),
            new FiltroCatalogoItemV2("RUC", "RUC"),
            new FiltroCatalogoItemV2("PASAPORTE", "Pasaporte")
        };
    }

    private static String codigo(JComboBox<FiltroCatalogoItemV2> combo) {
        Object selected = combo.getSelectedItem();
        return selected instanceof FiltroCatalogoItemV2 ? ((FiltroCatalogoItemV2) selected).getCodigo() : null;
    }

    private static String nombre(JComboBox<FiltroCatalogoItemV2> combo) {
        Object selected = combo.getSelectedItem();
        return selected instanceof FiltroCatalogoItemV2 ? ((FiltroCatalogoItemV2) selected).getNombreVisible() : null;
    }

    private static String nombreSeleccionadoConCodigo(JComboBox<FiltroCatalogoItemV2> combo) {
        FiltroCatalogoItemV2 item = combo.getSelectedItem() instanceof FiltroCatalogoItemV2
                ? (FiltroCatalogoItemV2) combo.getSelectedItem()
                : null;
        return item != null && item.hasCodigo() ? item.getNombreVisible() : null;
    }

    private String valorValidacionInicial() {
        if (rdoCorrespondeSdrerc.isSelected()) {
            return "Sí corresponde a la SDRERC";
        }
        if (rdoNoCorrespondeSdrerc.isSelected()) {
            return "No corresponde a la SDRERC";
        }
        return null;
    }

    private static boolean requiereHojaEnvio(String validacionInicial) {
        return "No corresponde a la SDRERC".equalsIgnoreCase(trimToNull(validacionInicial));
    }

    private static LocalDate localDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String safe(String value) {
        return value == null ? "-" : value;
    }

    private static class CatalogosFormulario {

        private final List<CatalogoItemDTO> canales;
        private final List<CatalogoItemDTO> procedimientos;
        private final List<CatalogoItemDTO> tiposDocumento;
        private final List<CatalogoItemDTO> tiposActa;

        private CatalogosFormulario(
                List<CatalogoItemDTO> canales,
                List<CatalogoItemDTO> procedimientos,
                List<CatalogoItemDTO> tiposDocumento,
                List<CatalogoItemDTO> tiposActa) {
            this.canales = canales;
            this.procedimientos = procedimientos;
            this.tiposDocumento = tiposDocumento;
            this.tiposActa = tiposActa;
        }
    }
}
