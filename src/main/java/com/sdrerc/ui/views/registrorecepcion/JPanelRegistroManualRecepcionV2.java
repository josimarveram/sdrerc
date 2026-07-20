package com.sdrerc.ui.views.registrorecepcion;

import com.sdrerc.application.sdrercapp.CatalogoLookupService;
import com.sdrerc.application.sdrercapp.ExpedienteEdicionManualService;
import com.sdrerc.domain.rules.ProcedimientoRegistralRules;
import com.sdrerc.application.sdrercapp.RegistroManualExpedienteService;
import com.sdrerc.application.sdrercapp.UbigeoAppService;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosActaDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosPersonaRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosSolicitudDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteEdicionManualDTO;
import com.sdrerc.domain.dto.sdrercapp.RegistroManualExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.RegistroManualResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.UbigeoItemDTO;
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
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
    private final ExpedienteEdicionManualService edicionService = new ExpedienteEdicionManualService();
    private final UbigeoAppService ubigeoService = new UbigeoAppService();
    private final Runnable onRegistroConfirmado;
    private final Runnable onCancelarEdicion;
    private final Long idExpedienteEdicion;
    private final boolean edicionDesdeAnalisis;

    private final JTextField txtNumeroTramite = new JTextField();
    private final JTextField txtNumeroDocumento = new JTextField();
    private final JTextField txtNumeroExpediente = new JTextField();
    private final JTextField txtNumeroExpedienteSgd = new JTextField();
    private final PremiumDateFieldV2 fechaRecepcionField = new PremiumDateFieldV2();
    private final JRadioButton rdoCorrespondeSdrerc = new JRadioButton("Sí corresponde a la SDRERC", true);
    private final JRadioButton rdoNoCorrespondeSdrerc = new JRadioButton("No corresponde a la SDRERC");
    private final ButtonGroup grupoValidacionInicial = new ButtonGroup();
    private final JTextField txtHojaEnvio = new JTextField();
    private final JComboBox<FiltroCatalogoItemV2> cmbTipoSolicitud = new JComboBox<FiltroCatalogoItemV2>(crearTiposSolicitud());
    private final JComboBox<FiltroCatalogoItemV2> cmbProcedimiento = comboBase("Seleccione procedimiento");
    private final JComboBox<FiltroCatalogoItemV2> cmbTipoDocumento = comboBase("Seleccione tipo documento");
    private final JComboBox<FiltroCatalogoItemV2> cmbCanal = comboBase("Seleccione canal");
    private final JComboBox<FiltroCatalogoItemV2> cmbPrioridad = new JComboBox<FiltroCatalogoItemV2>(new FiltroCatalogoItemV2[]{
        new FiltroCatalogoItemV2("NORMAL", "Normal"),
        new FiltroCatalogoItemV2("ALTA", "Alta"),
        new FiltroCatalogoItemV2("URGENTE", "Urgente")
    });
    private final JCheckBox chkGrupoFamiliar = new JCheckBox("Grupo familiar");

    private final JComboBox<FiltroCatalogoItemV2> cmbTipoActa = comboBase("Seleccione tipo acta");
    private final JTextField txtNumeroActa = new JTextField();

    private final JTextField txtTitularNombre = new JTextField();
    private final JComboBox<FiltroCatalogoItemV2> cmbTitularTipoDoc = new JComboBox<FiltroCatalogoItemV2>(crearTiposDocumentoTitular());
    private final JTextField txtTitularDocumento = new JTextField();

    private final JTextField txtRemitenteNombre = new JTextField();
    private final JComboBox<FiltroCatalogoItemV2> cmbRemitenteTipoDoc = new JComboBox<FiltroCatalogoItemV2>(crearTiposDocumentoRemitente());
    private final JTextField txtRemitenteDocumento = new JTextField();
    private final JTextField txtCorreoNotificacion = new JTextField();
    private final JTextField txtTelefonoNotificacion = new JTextField();
    private final JComboBox<FiltroCatalogoItemV2> cmbDepartamento = comboBase("Seleccione departamento");
    private final JComboBox<FiltroCatalogoItemV2> cmbProvincia = comboBase("Seleccione provincia");
    private final JComboBox<FiltroCatalogoItemV2> cmbDistrito = comboBase("Seleccione distrito");
    private final JTextField txtDireccionNotificacion = new JTextField();

    private final JTextArea txtErrores = area(5);
    private final JTextArea txtResumen = area(8);
    private final JLabel lblEstado = new JLabel("Complete los datos y presione Validar.");
    private final JLabel lblNumeroExpediente = new JLabel("Pendiente de generación al guardar");
    private final JButton btnValidar = new JButton("Validar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRegistrar = new JButton("Registrar expediente");

    private boolean trabajando;
    private boolean validado;
    private boolean cargandoEdicion;
    private boolean cargandoUbigeo;
    private RegistroManualExpedienteDTO registroValidado;
    private ExpedienteEdicionManualDTO edicionValidada;

    public JPanelRegistroManualRecepcionV2(Runnable onRegistroConfirmado) {
        this(null, onRegistroConfirmado, null, false);
    }

    public JPanelRegistroManualRecepcionV2(Long idExpedienteEdicion, Runnable onRegistroConfirmado, Runnable onCancelarEdicion) {
        this(idExpedienteEdicion, onRegistroConfirmado, onCancelarEdicion, false);
    }

    public JPanelRegistroManualRecepcionV2(
            Long idExpedienteEdicion,
            Runnable onRegistroConfirmado,
            Runnable onCancelarEdicion,
            boolean edicionDesdeAnalisis) {
        this.onRegistroConfirmado = onRegistroConfirmado;
        this.onCancelarEdicion = onCancelarEdicion;
        this.idExpedienteEdicion = idExpedienteEdicion;
        this.edicionDesdeAnalisis = edicionDesdeAnalisis;
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

        JLabel title = new JLabel(modoEdicion()
                ? "Edición manual de expediente"
                : "Registro manual de expediente");
        title.setFont(AppV2Theme.fontBold(18));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel(modoEdicion()
                ? "Actualice los datos recibidos sin modificar el número de expediente generado."
                : "Complete los datos de solicitud, acta, titular y remitente antes de registrar el expediente.");
        subtitle.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        subtitle.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel text = new JPanel(new BorderLayout(0, 4));
        text.setOpaque(false);
        text.add(title, BorderLayout.NORTH);
        text.add(subtitle, BorderLayout.CENTER);
        panel.add(text, BorderLayout.CENTER);
        panel.add(new BadgeV2(
                modoEdicion() ? "Edición manual" : "Registro manual",
                AppV2Theme.SOFT_GREEN,
                AppV2Theme.SUCCESS), BorderLayout.EAST);
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
        form.add(crearDatosExpedienteYActa(), gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        form.add(crearTitular(), gbc);
        gbc.gridx = 1;
        form.add(crearRemitente(), gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 12, 0);
        form.add(crearNotificacionUbicacion(), gbc);

        gbc.gridy = 4;
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
        agregarFila(panel, 0, "Fecha recepción *", fechaRecepcionField);
        agregarFila(panel, 1, "Canal de ingreso", cmbCanal);
        agregarFila(panel, 2, "Nro. trámite web", txtNumeroTramite);
        agregarFila(panel, 3, "Procedimiento registral *", cmbProcedimiento);
        agregarFila(panel, 4, "Tipo documento *", cmbTipoDocumento);
        agregarFila(panel, 5, "N° documento", txtNumeroDocumento);
        agregarFila(panel, 6, "Tipo de solicitud *", cmbTipoSolicitud);
        agregarFila(panel, 7, "Prioridad", cmbPrioridad);
        agregarFila(panel, 8, "Marca operativa", chkGrupoFamiliar);
        return panel;
    }

    private JPanel crearDatosExpedienteYActa() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 12));
        panel.setOpaque(false);
        panel.add(crearDatosExpediente());
        panel.add(crearDatosActa());
        return panel;
    }

    private JPanel crearDatosExpediente() {
        JPanel panel = seccion("Datos del expediente");
        agregarFila(panel, 0, "N° expediente", txtNumeroExpediente);
        agregarFila(panel, 1, "N° expediente SGD", txtNumeroExpedienteSgd);
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
        JPanel panel = seccion("Solicitante");
        agregarFila(panel, 0, "Nombres / Razón Social *", txtRemitenteNombre);
        agregarFila(panel, 1, "Tipo documento", cmbRemitenteTipoDoc);
        agregarFila(panel, 2, "Número documento", txtRemitenteDocumento);
        return panel;
    }

    private JPanel crearNotificacionUbicacion() {
        JPanel panel = seccion("Datos de notificación y ubicación");
        agregarFila(panel, 0, "Correo", txtCorreoNotificacion);
        agregarFila(panel, 1, "Teléfono", txtTelefonoNotificacion);
        agregarFila(panel, 2, "Departamento", cmbDepartamento);
        agregarFila(panel, 3, "Provincia", cmbProvincia);
        agregarFila(panel, 4, "Distrito", cmbDistrito);
        agregarFila(panel, 5, "Dirección", txtDireccionNotificacion);
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
        configurarCampo(txtNumeroDocumento);
        configurarCampoBloqueado(txtNumeroExpediente);
        configurarCampo(txtNumeroExpedienteSgd);
        fechaRecepcionField.setDate(toDate(LocalDate.now()));
        configurarCampo(txtHojaEnvio);
        configurarCampo(txtNumeroActa);
        configurarCampo(txtTitularNombre);
        configurarCampo(txtTitularDocumento);
        configurarCampo(txtRemitenteNombre);
        configurarCampo(txtRemitenteDocumento);
        configurarCampo(txtCorreoNotificacion);
        configurarCampo(txtTelefonoNotificacion);
        configurarCampo(txtDireccionNotificacion);
        configurarCombo(cmbProcedimiento);
        configurarCombo(cmbTipoSolicitud);
        configurarCombo(cmbTipoDocumento);
        configurarCombo(cmbCanal);
        configurarCombo(cmbPrioridad);
        configurarCombo(cmbTipoActa);
        configurarCombo(cmbTitularTipoDoc);
        configurarCombo(cmbRemitenteTipoDoc);
        configurarCombo(cmbDepartamento);
        configurarCombo(cmbProvincia);
        configurarCombo(cmbDistrito);
        cmbProvincia.setEnabled(false);
        cmbDistrito.setEnabled(false);
        configurarCheck(chkGrupoFamiliar);
        actualizarEstadoHojaEnvio();
        actualizarEstadoNumeroTramite();
        txtResumen.setText(modoEdicion() ? "Cargando datos del expediente..." : "Validación pendiente.");
        txtErrores.setText("");
        btnValidar.setText(modoEdicion() ? "Validar cambios" : "Validar");
        btnLimpiar.setText(modoEdicion() ? "Cancelar edición" : "Limpiar");
        btnRegistrar.setText(modoEdicion() ? "Guardar cambios" : "Registrar expediente");
        AppV2Theme.estilizarBotonPrimario(btnValidar);
        AppV2Theme.estilizarBotonPrimario(btnRegistrar);
        btnRegistrar.setEnabled(false);
        establecerNumeroExpedienteVisible(modoEdicion() ? "Cargando..." : "Pendiente de generación al guardar");
    }

    private void configurarCampo(JTextField field) {
        field.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
    }

    private void configurarCampoBloqueado(JTextField field) {
        configurarCampo(field);
        field.setEditable(false);
        field.setEnabled(true);
        field.setFocusable(false);
        field.setBackground(AppV2Theme.SURFACE_ALT);
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

    private void configurarCheck(JCheckBox check) {
        check.setOpaque(false);
        check.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        check.setForeground(AppV2Theme.TEXT_PRIMARY);
        check.setToolTipText("Marque esta opción si la solicitud pertenece o podría pertenecer a un grupo familiar. No restringe el registro.");
    }

    private void configurarEventos() {
        btnValidar.addActionListener(e -> validarFormulario());
        btnLimpiar.addActionListener(e -> {
            if (modoEdicion()) {
                cancelarEdicion();
            } else {
                limpiar();
            }
        });
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
            combo.addActionListener(e -> {
                if (combo == cmbCanal) {
                    actualizarEstadoNumeroTramite();
                }
                invalidarValidacion();
            });
        }
        fechaRecepcionField.addDateChangeListener(e -> invalidarValidacion());
        rdoCorrespondeSdrerc.addActionListener(e -> invalidarValidacion());
        rdoNoCorrespondeSdrerc.addActionListener(e -> invalidarValidacion());
        rdoCorrespondeSdrerc.addActionListener(e -> actualizarEstadoHojaEnvio());
        rdoNoCorrespondeSdrerc.addActionListener(e -> actualizarEstadoHojaEnvio());
        chkGrupoFamiliar.addActionListener(e -> invalidarValidacion());
        cmbDepartamento.addActionListener(e -> cargarProvinciasSeleccionadas());
        cmbProvincia.addActionListener(e -> cargarDistritosSeleccionados());
    }

    private List<JTextField> camposTexto() {
        List<JTextField> fields = new ArrayList<JTextField>();
        fields.add(txtNumeroTramite);
        fields.add(txtNumeroDocumento);
        fields.add(txtNumeroExpedienteSgd);
        fields.add(txtHojaEnvio);
        fields.add(txtNumeroActa);
        fields.add(txtTitularNombre);
        fields.add(txtTitularDocumento);
        fields.add(txtRemitenteNombre);
        fields.add(txtRemitenteDocumento);
        fields.add(txtCorreoNotificacion);
        fields.add(txtTelefonoNotificacion);
        fields.add(txtDireccionNotificacion);
        return fields;
    }

    private List<JTextArea> areasTexto() {
        return new ArrayList<JTextArea>();
    }

    private List<JComboBox<FiltroCatalogoItemV2>> combos() {
        List<JComboBox<FiltroCatalogoItemV2>> comboList = new ArrayList<JComboBox<FiltroCatalogoItemV2>>();
        comboList.add(cmbProcedimiento);
        comboList.add(cmbTipoSolicitud);
        comboList.add(cmbTipoDocumento);
        comboList.add(cmbCanal);
        comboList.add(cmbPrioridad);
        comboList.add(cmbTipoActa);
        comboList.add(cmbTitularTipoDoc);
        comboList.add(cmbRemitenteTipoDoc);
        comboList.add(cmbDepartamento);
        comboList.add(cmbProvincia);
        comboList.add(cmbDistrito);
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
                    int vacios = aplicarCatalogos(catalogos);
                    cargarDepartamentos();
                    if (modoEdicion()) {
                        cargarExpedienteEdicion();
                    } else {
                        lblEstado.setText(vacios == 0
                                ? "Catálogos cargados. Complete el formulario y presione Validar."
                                : "Catálogos cargados con " + vacios + " combo(s) sin opciones activas. Revise datos maestros.");
                    }
                } catch (Exception ex) {
                    String message = ex.getMessage();
                    if (message == null && ex.getCause() != null) {
                        message = ex.getCause().getMessage();
                    }
                    lblEstado.setText("No se pudieron cargar catálogos de SDRERC_APP. " + (message == null ? "" : message));
                }
            }
        };
        worker.execute();
    }

    private int aplicarCatalogos(CatalogosFormulario catalogos) {
        int vacios = 0;
        vacios += agregarItems(cmbCanal, catalogos.canales, "canales de recepción");
        vacios += agregarItems(cmbProcedimiento, catalogos.procedimientos, "procedimientos registrales");
        vacios += agregarItems(cmbTipoDocumento, catalogos.tiposDocumento, "tipos de documento");
        vacios += agregarItems(cmbTipoActa, catalogos.tiposActa, "tipos de acta");
        return vacios;
    }

    private int agregarItems(JComboBox<FiltroCatalogoItemV2> combo, List<CatalogoItemDTO> items, String nombreCatalogo) {
        if (items == null || items.isEmpty()) {
            combo.setToolTipText("No hay opciones activas para " + nombreCatalogo + ".");
            return 1;
        }
        for (CatalogoItemDTO item : items) {
            combo.addItem(new FiltroCatalogoItemV2(item.getCodigo(), item.getNombre()));
        }
        combo.setToolTipText(null);
        return 0;
    }

    private void cargarDepartamentos() {
        cargandoUbigeo = true;
        try {
            resetCombo(cmbDepartamento, "Seleccione departamento");
            resetCombo(cmbProvincia, "Seleccione provincia");
            resetCombo(cmbDistrito, "Seleccione distrito");
            for (UbigeoItemDTO item : ubigeoService.listarDepartamentos()) {
                cmbDepartamento.addItem(new FiltroCatalogoItemV2(String.valueOf(item.getId()), item.getNombre()));
            }
            cmbDepartamento.setEnabled(cmbDepartamento.getItemCount() > 1);
            cmbProvincia.setEnabled(false);
            cmbDistrito.setEnabled(false);
            cmbDepartamento.setToolTipText(cmbDepartamento.getItemCount() > 1
                    ? null
                    : "No hay departamentos activos cargados en SDRERC_APP.");
        } catch (Exception ex) {
            cmbDepartamento.setEnabled(false);
            cmbProvincia.setEnabled(false);
            cmbDistrito.setEnabled(false);
            cmbDepartamento.setToolTipText("Ejecute el script de ubigeo para habilitar departamentos/provincias/distritos.");
            lblEstado.setText("Catálogos cargados. Ubigeo no disponible hasta ejecutar el script de datos maestros.");
        } finally {
            cargandoUbigeo = false;
        }
    }

    private void cargarProvinciasSeleccionadas() {
        if (cargandoUbigeo) {
            return;
        }
        Long idDepartamento = idSeleccionado(cmbDepartamento);
        cargandoUbigeo = true;
        try {
            resetCombo(cmbProvincia, "Seleccione provincia");
            resetCombo(cmbDistrito, "Seleccione distrito");
            if (idDepartamento != null) {
                for (UbigeoItemDTO item : ubigeoService.listarProvincias(idDepartamento)) {
                    cmbProvincia.addItem(new FiltroCatalogoItemV2(String.valueOf(item.getId()), item.getNombre()));
                }
            }
            cmbProvincia.setEnabled(cmbProvincia.getItemCount() > 1);
            cmbDistrito.setEnabled(false);
        } catch (Exception ex) {
            cmbProvincia.setEnabled(false);
            cmbDistrito.setEnabled(false);
            cmbProvincia.setToolTipText("No se pudieron cargar provincias para el departamento seleccionado.");
        } finally {
            cargandoUbigeo = false;
            invalidarValidacion();
        }
    }

    private void cargarDistritosSeleccionados() {
        if (cargandoUbigeo) {
            return;
        }
        Long idProvincia = idSeleccionado(cmbProvincia);
        cargandoUbigeo = true;
        try {
            resetCombo(cmbDistrito, "Seleccione distrito");
            if (idProvincia != null) {
                for (UbigeoItemDTO item : ubigeoService.listarDistritos(idProvincia)) {
                    cmbDistrito.addItem(new FiltroCatalogoItemV2(String.valueOf(item.getId()), item.getNombre()));
                }
            }
            cmbDistrito.setEnabled(cmbDistrito.getItemCount() > 1);
        } catch (Exception ex) {
            cmbDistrito.setEnabled(false);
            cmbDistrito.setToolTipText("No se pudieron cargar distritos para la provincia seleccionada.");
        } finally {
            cargandoUbigeo = false;
            invalidarValidacion();
        }
    }

    private void resetCombo(JComboBox<FiltroCatalogoItemV2> combo, String label) {
        combo.removeAllItems();
        combo.addItem(new FiltroCatalogoItemV2(null, label));
        combo.setSelectedIndex(0);
        combo.setToolTipText(null);
    }

    private void validarFormulario() {
        RegistroManualExpedienteDTO dto = modoEdicion() ? construirEdicion() : construirRegistro();
        setTrabajando(true, modoEdicion()
                ? "Validando cambios de edición manual..."
                : "Validando formulario y duplicidad por acta y titular en SDRERC_APP...");
        SwingWorker<ValidacionManualResultado, Void> worker = new SwingWorker<ValidacionManualResultado, Void>() {
            @Override
            protected ValidacionManualResultado doInBackground() throws Exception {
                if (dto instanceof ExpedienteEdicionManualDTO) {
                    return new ValidacionManualResultado(dto, edicionService.validar((ExpedienteEdicionManualDTO) dto));
                }
                return new ValidacionManualResultado(dto, registroService.validarConDuplicados(dto));
            }

            @Override
            protected void done() {
                try {
                    ValidacionManualResultado resultado = get();
                    aplicarValidacion(resultado.dto, resultado.mensajes);
                } catch (Exception ex) {
                    mostrarError("No se pudo validar el registro manual. No se registró ningún dato.", ex);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void aplicarValidacion(RegistroManualExpedienteDTO dto, List<String> errores) {
        String numeroPreview = modoEdicion()
                ? safe(dto.getNumeroExpedienteVistaPrevia())
                : dto.isPosibleDuplicado()
                ? "Sin número por duplicado al guardar"
                : ProcedimientoRegistralRules.requiereDecisionAsignacionParaNumero(dto.getSolicitud().getTipoProcedimientoNombre())
                ? "Sin número por procedimiento al guardar"
                : "Pendiente de generación al guardar";
        if (errores.isEmpty()) {
            dto.setNumeroExpedienteVistaPrevia(numeroPreview);
            validado = true;
            registroValidado = dto;
            edicionValidada = dto instanceof ExpedienteEdicionManualDTO ? (ExpedienteEdicionManualDTO) dto : null;
            txtErrores.setBackground(AppV2Theme.SOFT_GREEN);
            txtErrores.setText(modoEdicion()
                    ? "Sin errores críticos. Listo para guardar cambios."
                    : "Sin errores críticos. Listo para registrar.");
            txtResumen.setText(resumen(dto));
            establecerNumeroExpedienteVisible(numeroPreview);
            lblNumeroExpediente.setText(dto.getNumeroExpedienteVistaPrevia());
            lblEstado.setText(modoEdicion()
                    ? "Cambios validados. Revise el resumen y guarde."
                    : "Formulario validado. Revise el resumen y registre el expediente.");
            btnRegistrar.setEnabled(!trabajando);
        } else {
            dto.setNumeroExpedienteVistaPrevia(numeroPreview);
            dto.setObservacionesGenerales("Advertencias de validación: " + String.join(" | ", errores));
            validado = true;
            registroValidado = dto;
            edicionValidada = dto instanceof ExpedienteEdicionManualDTO ? (ExpedienteEdicionManualDTO) dto : null;
            txtErrores.setBackground(AppV2Theme.SOFT_ORANGE);
            txtErrores.setText(String.join("\n", errores));
            txtResumen.setText(resumen(dto));
            establecerNumeroExpedienteVisible(numeroPreview);
            lblNumeroExpediente.setText(dto.getNumeroExpedienteVistaPrevia());
            lblEstado.setText(dto.isNumeroExpedienteSgdDuplicado()
                    ? dto.getMotivoNumeroExpedienteSgdDuplicado() + " Corrija el N° expediente SGD antes de continuar."
                    : modoEdicion()
                    ? "Se encontraron observaciones. Corrija antes de guardar."
                    : dto.isPosibleDuplicado()
                    ? "Documento duplicado detectado. Puede registrarlo y quedará marcado para Asignación."
                    : "Se encontraron observaciones. Puede registrar el expediente y quedará marcado para revisión.");
            btnRegistrar.setEnabled(!trabajando && !modoEdicion() && !dto.isNumeroExpedienteSgdDuplicado());
        }
    }

    private void cargarExpedienteEdicion() {
        if (idExpedienteEdicion == null) {
            lblEstado.setText("Seleccione un expediente para editar.");
            return;
        }
        setTrabajando(true, "Cargando datos actuales del expediente...");
        SwingWorker<ExpedienteEdicionManualDTO, Void> worker = new SwingWorker<ExpedienteEdicionManualDTO, Void>() {
            @Override
            protected ExpedienteEdicionManualDTO doInBackground() throws Exception {
                return edicionDesdeAnalisis
                        ? edicionService.obtenerParaEdicionDesdeAnalisis(idExpedienteEdicion)
                        : edicionService.obtenerParaEdicion(idExpedienteEdicion);
            }

            @Override
            protected void done() {
                try {
                    aplicarExpedienteEdicion(get());
                    lblEstado.setText("Datos cargados. Modifique los campos permitidos y presione Validar cambios.");
                } catch (Exception ex) {
                    mostrarError("No se pudo cargar el expediente para edición.", ex);
                    btnValidar.setEnabled(false);
                    btnRegistrar.setEnabled(false);
                } finally {
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void aplicarExpedienteEdicion(ExpedienteEdicionManualDTO dto) {
        cargandoEdicion = true;
        try {
            edicionValidada = null;
            registroValidado = null;
            txtNumeroTramite.setText(safeText(dto.getSolicitud().getNumeroTramite()));
            txtNumeroDocumento.setText(safeText(dto.getSolicitud().getNumeroDocumento()));
            txtNumeroExpedienteSgd.setText(safeText(dto.getSolicitud().getNumeroExpedienteSgd()));
            fechaRecepcionField.setDate(dto.getSolicitud().getFechaRecepcion() == null
                    ? null
                    : toDate(dto.getSolicitud().getFechaRecepcion()));
            seleccionarCombo(cmbTipoSolicitud, dto.getSolicitud().getTipoSolicitudCodigo(), dto.getSolicitud().getTipoSolicitudNombre());
            seleccionarCombo(cmbProcedimiento, dto.getSolicitud().getTipoProcedimientoCodigo(), dto.getSolicitud().getTipoProcedimientoNombre());
            seleccionarTipoDocumento(cmbTipoDocumento, dto.getSolicitud().getTipoDocumentoCodigo(), dto.getSolicitud().getTipoDocumentoNombre());
            seleccionarCombo(cmbCanal, dto.getSolicitud().getCanalCodigo(), dto.getSolicitud().getCanalNombre());
            seleccionarCombo(cmbPrioridad, dto.getSolicitud().getPrioridad(), dto.getSolicitud().getPrioridad());
            seleccionarCombo(cmbTipoActa, dto.getActa().getTipoActaCodigo(), dto.getActa().getTipoActaNombre());
            txtNumeroActa.setText(safeText(dto.getActa().getNumeroActa()));
            txtTitularNombre.setText(safeText(dto.getTitular().getNombreCompleto()));
            seleccionarCombo(cmbTitularTipoDoc, dto.getTitular().getTipoDocumento(), dto.getTitular().getTipoDocumento());
            txtTitularDocumento.setText(safeText(dto.getTitular().getNumeroDocumento()));
            txtRemitenteNombre.setText(safeText(dto.getRemitente().getNombreCompleto()));
            seleccionarCombo(cmbRemitenteTipoDoc, dto.getRemitente().getTipoDocumento(), dto.getRemitente().getTipoDocumento());
            txtRemitenteDocumento.setText(safeText(dto.getRemitente().getNumeroDocumento()));
            txtCorreoNotificacion.setText(safeText(dto.getRemitente().getCorreo()));
            txtTelefonoNotificacion.setText(safeText(dto.getRemitente().getTelefono()));
            txtDireccionNotificacion.setText(safeText(dto.getRemitente().getDireccion()));
            seleccionarUbigeoEdicion(dto.getRemitente());
            if ("No corresponde a la SDRERC".equalsIgnoreCase(dto.getSolicitud().getValidacionInicial())) {
                rdoNoCorrespondeSdrerc.setSelected(true);
            } else {
                rdoCorrespondeSdrerc.setSelected(true);
            }
            actualizarEstadoHojaEnvio();
            actualizarEstadoNumeroTramite();
            txtHojaEnvio.setText(safeText(dto.getSolicitud().getHojaEnvio()));
            chkGrupoFamiliar.setSelected(dto.getSolicitud().isGrupoFamiliar());
            establecerNumeroExpedienteVisible(safe(dto.getNumeroExpediente()));
            lblNumeroExpediente.setText(safe(dto.getNumeroExpedienteVistaPrevia()));
            txtErrores.setBackground(AppV2Theme.SOFT_GREEN);
            txtErrores.setText("Expediente editable. Estado actual: Registro / Registrado.");
            txtResumen.setText(resumen(dto));
            validado = false;
            btnRegistrar.setEnabled(false);
        } finally {
            cargandoEdicion = false;
        }
    }

    private void registrarExpediente() {
        if (modoEdicion()) {
            guardarEdicionManual();
            return;
        }
        if (!validado || registroValidado == null) {
            validarFormulario();
            return;
        }
        boolean conObservaciones = registroValidado.getObservacionesGenerales() != null
                && !registroValidado.getObservacionesGenerales().trim().isEmpty();
        boolean duplicado = registroValidado.isPosibleDuplicado();
        int option = JOptionPane.showConfirmDialog(
                this,
                "Se registrará un expediente en SDRERC_APP"
                        + (conObservaciones ? " con observaciones" : "")
                        + (duplicado ? " y alerta de documento duplicado" : "")
                        + ".\n\n" + resumen(registroValidado) + "\n¿Desea continuar?",
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
                    establecerNumeroExpedienteVisible(resultado.getNumeroExpediente());
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

    private void guardarEdicionManual() {
        if (!validado || edicionValidada == null) {
            validarFormulario();
            return;
        }
        int option = JOptionPane.showConfirmDialog(
                this,
                "Se actualizarán los datos de recepción del expediente.\n"
                        + "No se modificará el número de expediente generado.\n\n"
                        + resumen(edicionValidada) + "\n¿Desea continuar?",
                "Confirmar edición manual",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        setTrabajando(true, "Guardando cambios de edición manual...");
        SwingWorker<RegistroManualResultadoDTO, Void> worker = new SwingWorker<RegistroManualResultadoDTO, Void>() {
            @Override
            protected RegistroManualResultadoDTO doInBackground() throws Exception {
                return edicionDesdeAnalisis
                        ? edicionService.guardarDesdeAnalisis(edicionValidada)
                        : edicionService.guardar(edicionValidada);
            }

            @Override
            protected void done() {
                try {
                    RegistroManualResultadoDTO resultado = get();
                    establecerNumeroExpedienteVisible(resultado.getNumeroExpediente());
                    lblNumeroExpediente.setText(resultado.getNumeroExpediente());
                    lblEstado.setText(resultado.getMensaje());
                    txtResumen.setText(resultado.getMensaje() + "\n\n" + txtResumen.getText());
                    JOptionPane.showMessageDialog(
                            JPanelRegistroManualRecepcionV2.this,
                            resultado.getMensaje(),
                            "Edición manual confirmada",
                            JOptionPane.INFORMATION_MESSAGE);
                    validado = false;
                    btnRegistrar.setEnabled(false);
                    if (onRegistroConfirmado != null) {
                        onRegistroConfirmado.run();
                    }
                    if (onCancelarEdicion != null) {
                        onCancelarEdicion.run();
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudo guardar la edición manual. La transacción fue revertida.", ex);
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
        solicitud.setNumeroDocumento(txtNumeroDocumento.getText());
        solicitud.setNumeroExpedienteSgd(txtNumeroExpedienteSgd.getText());
        solicitud.setFechaRecepcion(localDate(fechaRecepcionField.getDate()));
        solicitud.setValidacionInicial(valorValidacionInicial());
        solicitud.setHojaEnvio(txtHojaEnvio.getText());
        solicitud.setTipoSolicitudCodigo(codigo(cmbTipoSolicitud));
        solicitud.setTipoSolicitudNombre(nombreSeleccionadoConCodigo(cmbTipoSolicitud));
        solicitud.setTipoProcedimientoCodigo(codigo(cmbProcedimiento));
        solicitud.setTipoProcedimientoNombre(nombreSeleccionadoConCodigo(cmbProcedimiento));
        solicitud.setTipoDocumentoCodigo(codigo(cmbTipoDocumento));
        solicitud.setTipoDocumentoNombre(nombreSeleccionadoConCodigo(cmbTipoDocumento));
        solicitud.setCanalCodigo(codigo(cmbCanal));
        solicitud.setCanalNombre(nombre(cmbCanal));
        solicitud.setPrioridad(codigo(cmbPrioridad));
        solicitud.setGrupoFamiliar(chkGrupoFamiliar.isSelected());
        solicitud.setCriterioGrupoFamiliar(chkGrupoFamiliar.isSelected() ? "MANUAL" : null);
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
        remitente.setCorreo(txtCorreoNotificacion.getText());
        remitente.setTelefono(txtTelefonoNotificacion.getText());
        remitente.setDireccion(txtDireccionNotificacion.getText());
        remitente.setIdDepartamento(idSeleccionado(cmbDepartamento));
        remitente.setIdProvincia(idSeleccionado(cmbProvincia));
        remitente.setIdDistrito(idSeleccionado(cmbDistrito));
        remitente.setDepartamento(nombreSeleccionadoConCodigo(cmbDepartamento));
        remitente.setProvincia(nombreSeleccionadoConCodigo(cmbProvincia));
        remitente.setDistrito(nombreSeleccionadoConCodigo(cmbDistrito));
        dto.setRemitente(remitente);
        return dto;
    }

    private ExpedienteEdicionManualDTO construirEdicion() {
        RegistroManualExpedienteDTO base = construirRegistro();
        ExpedienteEdicionManualDTO dto = new ExpedienteEdicionManualDTO();
        dto.setIdExpediente(idExpedienteEdicion);
        dto.setSolicitud(base.getSolicitud());
        dto.setActa(base.getActa());
        dto.setTitular(base.getTitular());
        dto.setRemitente(base.getRemitente());
        dto.setObservacionesGenerales(base.getObservacionesGenerales());
        String numero = edicionValidada == null ? lblNumeroExpediente.getText() : edicionValidada.getNumeroExpediente();
        dto.setNumeroExpediente(numero);
        dto.setNumeroExpedienteVistaPrevia(safe(numero));
        dto.setEtapaCodigo("REGISTRO");
        dto.setEstadoCodigo("REGISTRADO");
        return dto;
    }

    private String resumen(RegistroManualExpedienteDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("Trámite: ").append(safe(dto.getSolicitud().getNumeroTramite())).append("\n");
        sb.append("N° documento: ").append(safe(dto.getSolicitud().getNumeroDocumento())).append("\n");
        sb.append("N° expediente SGD: ").append(safe(dto.getSolicitud().getNumeroExpedienteSgd())).append("\n");
        sb.append("Tipo de solicitud: ").append(safe(dto.getSolicitud().getTipoSolicitudNombre())).append("\n");
        sb.append("Titular: ").append(safe(dto.getTitular().getNombreCompleto())).append("\n");
        sb.append("Procedimiento: ").append(safe(dto.getSolicitud().getTipoProcedimientoNombre())).append("\n");
        sb.append("Grupo familiar: ").append(dto.getSolicitud().getGrupoFamiliarTexto()).append("\n");
        if (dto.getSolicitud().getObservacionGrupoFamiliar() != null
                && !dto.getSolicitud().getObservacionGrupoFamiliar().trim().isEmpty()) {
            sb.append("Observación grupo familiar: ")
                    .append(dto.getSolicitud().getObservacionGrupoFamiliar())
                    .append("\n");
        }
        sb.append("Acta: ").append(safe(dto.getActa().getNumeroActa())).append("\n");
        sb.append("Remitente: ").append(safe(dto.getRemitente().getNombreCompleto())).append("\n");
        sb.append("Correo: ").append(safe(dto.getRemitente().getCorreo())).append("\n");
        sb.append("Teléfono: ").append(safe(dto.getRemitente().getTelefono())).append("\n");
        sb.append("Ubicación: ")
                .append(safe(dto.getRemitente().getDepartamento()))
                .append(" / ")
                .append(safe(dto.getRemitente().getProvincia()))
                .append(" / ")
                .append(safe(dto.getRemitente().getDistrito()))
                .append("\n");
        sb.append("Dirección: ").append(safe(dto.getRemitente().getDireccion())).append("\n");
        sb.append("Validación inicial: ").append(safe(dto.getSolicitud().getValidacionInicial())).append("\n");
        if (requiereHojaEnvio(dto.getSolicitud().getValidacionInicial())) {
            sb.append("Hoja de envío: ").append(safe(dto.getSolicitud().getHojaEnvio())).append("\n");
        }
        if (dto.getObservacionesGenerales() != null && !dto.getObservacionesGenerales().trim().isEmpty()) {
            sb.append("Observaciones: ").append(dto.getObservacionesGenerales()).append("\n");
        }
        if (dto.isPosibleDuplicado()) {
            sb.append("Alerta duplicado: ").append(safe(dto.getMotivoDuplicado())).append("\n");
        }
        sb.append("Número expediente: ").append(safe(dto.getNumeroExpedienteVistaPrevia()));
        return sb.toString();
    }

    private void preguntarLimpiar() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "¿Desea limpiar el formulario para registrar otro expediente?",
                "Registro / Recepción",
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
        establecerNumeroExpedienteVisible("Pendiente de generación al guardar");
        for (JTextArea area : areasTexto()) {
            area.setText("");
        }
        fechaRecepcionField.setDate(toDate(LocalDate.now()));
        rdoCorrespondeSdrerc.setSelected(true);
        chkGrupoFamiliar.setSelected(false);
        actualizarEstadoHojaEnvio();
        actualizarEstadoNumeroTramite();
        seleccionarPrimero(cmbProcedimiento);
        seleccionarPrimero(cmbTipoSolicitud);
        seleccionarPrimero(cmbTipoDocumento);
        seleccionarPrimero(cmbCanal);
        seleccionarPrimero(cmbTipoActa);
        seleccionarPrimero(cmbTitularTipoDoc);
        cmbPrioridad.setSelectedIndex(0);
        cmbRemitenteTipoDoc.setSelectedIndex(0);
        seleccionarPrimero(cmbDepartamento);
        resetCombo(cmbProvincia, "Seleccione provincia");
        resetCombo(cmbDistrito, "Seleccione distrito");
        cmbProvincia.setEnabled(false);
        cmbDistrito.setEnabled(false);
        txtErrores.setText("");
        txtResumen.setText("Validación pendiente.");
        lblNumeroExpediente.setText("Pendiente de generación al guardar");
        lblEstado.setText("Complete los datos y presione Validar.");
        validado = false;
        registroValidado = null;
        btnRegistrar.setEnabled(false);
    }

    private void establecerNumeroExpedienteVisible(String valor) {
        txtNumeroExpediente.setText(safe(valor));
    }

    private void seleccionarPrimero(JComboBox<FiltroCatalogoItemV2> combo) {
        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    private void invalidarValidacion() {
        if (trabajando || cargandoEdicion) {
            return;
        }
        validado = false;
        registroValidado = null;
        edicionValidada = null;
        btnRegistrar.setEnabled(false);
        if (!modoEdicion()) {
            establecerNumeroExpedienteVisible("Pendiente de generación al guardar");
        }
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

    private void actualizarEstadoNumeroTramite() {
        boolean mesaVirtual = esMesaPartesVirtual(cmbCanal);
        txtNumeroTramite.setEnabled(mesaVirtual);
        txtNumeroTramite.setEditable(mesaVirtual);
        if (mesaVirtual) {
            if ("SIN TRAMITE".equalsIgnoreCase(trimToNull(txtNumeroTramite.getText()))) {
                txtNumeroTramite.setText("");
            }
        } else {
            txtNumeroTramite.setText("SIN TRAMITE");
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

    private void cancelarEdicion() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Se cerrará la edición y se volverá a mostrar Bandeja Registro.\n"
                        + "Los cambios no guardados se perderán.",
                "Cancelar edición manual",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (option == JOptionPane.YES_OPTION && onCancelarEdicion != null) {
            onCancelarEdicion.run();
        }
    }

    private boolean modoEdicion() {
        return idExpedienteEdicion != null;
    }

    private boolean esMesaPartesVirtual(JComboBox<FiltroCatalogoItemV2> combo) {
        Object selected = combo.getSelectedItem();
        if (!(selected instanceof FiltroCatalogoItemV2)) {
            return false;
        }
        FiltroCatalogoItemV2 item = (FiltroCatalogoItemV2) selected;
        String codigo = normalizarSinAcentos(item.getCodigo());
        String nombre = normalizarSinAcentos(item.getNombreVisible());
        return "MPV".equals(codigo)
                || "MESA DE PARTES VIRTUAL".equals(nombre)
                || (nombre != null && nombre.contains("MESA DE PARTES VIRTUAL"));
    }

    private void seleccionarCombo(JComboBox<FiltroCatalogoItemV2> combo, String codigo, String nombre) {
        String normalizedCodigo = normalizarComparacion(codigo);
        String normalizedNombre = normalizarComparacion(nombre);
        for (int i = 0; i < combo.getItemCount(); i++) {
            FiltroCatalogoItemV2 item = combo.getItemAt(i);
            if (normalizedCodigo != null && normalizedCodigo.equals(normalizarComparacion(item.getCodigo()))) {
                combo.setSelectedIndex(i);
                return;
            }
            if (normalizedNombre != null && normalizedNombre.equals(normalizarComparacion(item.getNombreVisible()))) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarTipoDocumento(JComboBox<FiltroCatalogoItemV2> combo, String codigo, String nombre) {
        if (seleccionarPorCodigo(combo, codigo)) {
            return;
        }
        String normalizedNombre = normalizarSinAcentos(nombre);
        for (int i = 0; i < combo.getItemCount(); i++) {
            FiltroCatalogoItemV2 item = combo.getItemAt(i);
            if (normalizedNombre != null && normalizedNombre.equals(normalizarSinAcentos(item.getNombreVisible()))) {
                combo.setSelectedIndex(i);
                return;
            }
            if (normalizedNombre != null && normalizedNombre.equals(normalizarSinAcentos(item.getCodigo()))) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private boolean seleccionarPorCodigo(JComboBox<FiltroCatalogoItemV2> combo, String codigo) {
        String normalizedCodigo = normalizarComparacion(codigo);
        if (normalizedCodigo == null) {
            return false;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            FiltroCatalogoItemV2 item = combo.getItemAt(i);
            if (normalizedCodigo.equals(normalizarComparacion(item.getCodigo()))) {
                combo.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

    private void seleccionarUbigeoEdicion(DatosPersonaRegistroDTO persona) {
        if (persona == null) {
            return;
        }
        seleccionarCombo(cmbDepartamento,
                persona.getIdDepartamento() == null ? null : String.valueOf(persona.getIdDepartamento()),
                persona.getDepartamento());
        cargarProvinciasSeleccionadas();
        seleccionarCombo(cmbProvincia,
                persona.getIdProvincia() == null ? null : String.valueOf(persona.getIdProvincia()),
                persona.getProvincia());
        cargarDistritosSeleccionados();
        seleccionarCombo(cmbDistrito,
                persona.getIdDistrito() == null ? null : String.valueOf(persona.getIdDistrito()),
                persona.getDistrito());
    }

    private String normalizarComparacion(String value) {
        if (value == null || value.trim().isEmpty() || "-".equals(value.trim())) {
            return null;
        }
        return normalizarSinAcentos(value);
    }

    private String normalizarSinAcentos(String value) {
        if (value == null || value.trim().isEmpty() || "-".equals(value.trim())) {
            return null;
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.toUpperCase(Locale.ROOT);
    }

    private Long idSeleccionado(JComboBox<FiltroCatalogoItemV2> combo) {
        String value = codigo(combo);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
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

    private static FiltroCatalogoItemV2[] crearTiposSolicitud() {
        return new FiltroCatalogoItemV2[]{
            new FiltroCatalogoItemV2(null, "Seleccione tipo de solicitud"),
            new FiltroCatalogoItemV2("PARTE", "Parte"),
            new FiltroCatalogoItemV2("OFICIO", "Oficio")
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

    private static String safeText(String value) {
        return value == null ? "" : value;
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

    private static class ValidacionManualResultado {

        private final RegistroManualExpedienteDTO dto;
        private final List<String> mensajes;

        private ValidacionManualResultado(RegistroManualExpedienteDTO dto, List<String> mensajes) {
            this.dto = dto;
            this.mensajes = mensajes == null ? new ArrayList<String>() : mensajes;
        }
    }
}
