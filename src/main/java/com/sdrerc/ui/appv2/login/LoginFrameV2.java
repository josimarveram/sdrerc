package com.sdrerc.ui.appv2.login;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sdrerc.application.sdrercapp.AutenticacionService;
import com.sdrerc.domain.model.User;
import com.sdrerc.shared.session.SessionContext;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Login premium con doble autenticación (contraseña + TOTP) para SDRERC V2, contra la tabla
 * {@code usuario} de SDRERC_APP. Orquesta los pasos: credenciales -&gt; cambio de contraseña
 * obligatorio (si aplica) -&gt; enrolamiento o verificación TOTP -&gt; sesión.
 *
 * <p>Al autenticar con éxito, fija {@code SessionContext.setUsuarioActual(...)} y luego invoca el
 * callback {@code onLoginExitoso}, que típicamente abre {@code MenuPrincipalV2}.</p>
 *
 * <p>Cada paso reemplaza el contenido de {@code panelPasos} (en vez de usar {@code CardLayout},
 * que reserva el alto del paso más grande de todos incluso al mostrar uno chico) y la ventana se
 * reajusta con {@code pack()} tras cada cambio, para que el tamaño se adapte al contenido real de
 * cada paso y a la resolución de pantalla, sin scrollbar ni huecos vacíos.</p>
 */
public class LoginFrameV2 extends JFrame {

    private static final int QR_SIZE_PX = 200;
    private static final int ANCHO_CONTENIDO = 340;

    private final AutenticacionService autenticacionService = new AutenticacionService();
    private final Runnable onLoginExitoso;

    private final JPanel panelPasos = crearContenedorPasos();
    private final AppV2LoadingOverlay overlay = new AppV2LoadingOverlay();

    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JCheckBox chkMostrarPassword = new JCheckBox("Mostrar contraseña");
    private final JLabel lblError = new JLabel(" ");
    private final JButton btnIngresar = new JButton("Ingresar");
    private char echoCharPassword;

    private JPanel panelCredenciales;
    private PasoCambioPasswordPanel panelCambioPassword;
    private PasoTotpEnrolarPanel panelTotpEnrolar;
    private PasoTotpVerificarPanel panelTotpVerificar;

    private AutenticacionService.ResultadoLogin resultadoLoginActual;

    public LoginFrameV2(Runnable onLoginExitoso) {
        super("SDRERC - Acceso seguro");
        this.onLoginExitoso = onLoginExitoso;
        configurarVentana();
    }

    private static JPanel crearContenedorPasos() {
        return new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension natural = super.getPreferredSize();
                return new Dimension(ANCHO_CONTENIDO, natural.height);
            }
        };
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        panelCredenciales = crearPasoCredenciales();
        panelCambioPassword = new PasoCambioPasswordPanel(new PasoCambioPasswordPanel.Listener() {
            @Override
            public void onConfirmar(String nuevaPassword, String confirmarPassword) {
                confirmarCambioPassword(nuevaPassword, confirmarPassword);
            }

            @Override
            public void onCancelar() {
                volverACredenciales();
            }
        });
        panelTotpEnrolar = new PasoTotpEnrolarPanel(new PasoTotpEnrolarPanel.Listener() {
            @Override
            public void onConfirmarCodigo(String codigo) {
                confirmarEnrolamientoTotp(codigo);
            }

            @Override
            public void onFinalizar() {
                completarLogin();
            }

            @Override
            public void onCancelar() {
                volverACredenciales();
            }

            @Override
            public void onTamanoCambiado() {
                ajustarTamano();
            }
        });
        panelTotpVerificar = new PasoTotpVerificarPanel(new PasoTotpVerificarPanel.Listener() {
            @Override
            public void onConfirmarCodigo(String codigo) {
                verificarTotp(codigo);
            }

            @Override
            public void onCancelar() {
                volverACredenciales();
            }
        });

        panelPasos.setOpaque(false);

        JPanel card = new JPanel(new BorderLayout(0, 18));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER_STRONG),
                BorderFactory.createEmptyBorder(28, 32, 26, 32)));
        card.add(crearHeader(), BorderLayout.NORTH);
        card.add(panelPasos, BorderLayout.CENTER);
        card.add(crearFooter(), BorderLayout.SOUTH);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(AppV2Theme.BACKGROUND);
        root.add(card, new GridBagConstraints());

        JPanel layered = new JPanel(new BorderLayout());
        layered.add(root, BorderLayout.CENTER);

        setContentPane(layered);
        setGlassPane(overlay);
        mostrarPaso(panelCredenciales);
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel lblLogo = new JLabel();
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        URL logoUrl = getClass().getResource("/com/sdrerc/ui/imagenes/LogoRENIEC.png");
        if (logoUrl != null) {
            ImageIcon icono = new ImageIcon(logoUrl);
            Image escalada = icono.getImage().getScaledInstance(180, -1, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(escalada));
        }

        JLabel lblTitulo = new JLabel("SDRERC");
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setFont(new Font(AppV2Theme.FONT_FAMILY, Font.BOLD, 22));
        lblTitulo.setForeground(AppV2Theme.PRIMARY_DARK);

        JLabel lblSubtitulo = new JLabel("Acceso seguro institucional");
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSubtitulo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblSubtitulo.setForeground(AppV2Theme.TEXT_SECONDARY);

        header.add(lblLogo);
        header.add(Box.createVerticalStrut(12));
        header.add(lblTitulo);
        header.add(Box.createVerticalStrut(2));
        header.add(lblSubtitulo);
        header.add(Box.createVerticalStrut(16));
        return header;
    }

    private JPanel crearPasoCredenciales() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        Dimension fieldSize = new Dimension(ANCHO_CONTENIDO, 36);
        txtUsuario.setPreferredSize(fieldSize);
        txtUsuario.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        txtPassword.setPreferredSize(fieldSize);
        txtPassword.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        echoCharPassword = txtPassword.getEchoChar();

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        gbc.gridy = 0;
        formulario.add(etiqueta("Usuario"), gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 14, 0);
        formulario.add(txtUsuario, gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        formulario.add(etiqueta("Contraseña"), gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 6, 0);
        formulario.add(txtPassword, gbc);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 4, 0);
        chkMostrarPassword.setOpaque(false);
        chkMostrarPassword.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        chkMostrarPassword.addActionListener(e -> actualizarVisibilidadPassword());
        formulario.add(chkMostrarPassword, gbc);

        lblError.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblError.setForeground(AppV2Theme.ERROR);
        gbc.gridy = 5;
        gbc.insets = new Insets(6, 0, 0, 0);
        formulario.add(lblError, gbc);

        btnIngresar.setPreferredSize(new Dimension(ANCHO_CONTENIDO, 40));
        AppV2Theme.estilizarBotonPrimario(btnIngresar);
        btnIngresar.addActionListener(e -> iniciarSesion());

        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    iniciarSesion();
                }
            }
        };
        txtUsuario.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        botones.setOpaque(false);
        botones.add(btnIngresar);

        panel.add(formulario, BorderLayout.NORTH);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        footer.setOpaque(false);
        JLabel lbl = new JLabel("Registro Nacional de Identificación y Estado Civil");
        lbl.setFont(AppV2Theme.fontPlain(11));
        lbl.setForeground(AppV2Theme.MUTED);
        footer.add(lbl);
        return footer;
    }

    private JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lbl.setForeground(AppV2Theme.TEXT_SECONDARY);
        return lbl;
    }

    private void actualizarVisibilidadPassword() {
        txtPassword.setEchoChar(chkMostrarPassword.isSelected() ? (char) 0 : echoCharPassword);
    }

    // ---------------------------------------------------------------
    // Paso 1: credenciales
    // ---------------------------------------------------------------

    private void iniciarSesion() {
        String username = txtUsuario.getText();
        String password = new String(txtPassword.getPassword());
        limpiarError();
        setEnabledFormulario(false);
        overlay.mostrar("Verificando credenciales");

        new SwingWorker<AutenticacionService.ResultadoLogin, Void>() {
            private Exception error;

            @Override
            protected AutenticacionService.ResultadoLogin doInBackground() {
                try {
                    return autenticacionService.iniciarLogin(username, password);
                } catch (Exception ex) {
                    error = ex;
                    return null;
                }
            }

            @Override
            protected void done() {
                overlay.ocultar();
                setEnabledFormulario(true);
                if (error != null) {
                    mostrarError(mensajeAmigable(error));
                    return;
                }
                try {
                    resultadoLoginActual = get();
                } catch (Exception ex) {
                    mostrarError(mensajeAmigable(ex));
                    return;
                }
                txtPassword.setText("");
                continuarLuegoDeCredenciales();
            }
        }.execute();
    }

    private void continuarLuegoDeCredenciales() {
        if (resultadoLoginActual.isDebeCambiarPassword()) {
            panelCambioPassword.limpiar();
            mostrarPaso(panelCambioPassword);
        } else if (!resultadoLoginActual.isTotpHabilitado()) {
            iniciarEnrolamientoTotp();
        } else {
            panelTotpVerificar.reset();
            mostrarPaso(panelTotpVerificar);
            panelTotpVerificar.enfocar();
        }
    }

    // ---------------------------------------------------------------
    // Paso 2: cambio de contraseña obligatorio
    // ---------------------------------------------------------------

    private void confirmarCambioPassword(String nuevaPassword, String confirmarPassword) {
        if (nuevaPassword == null || !nuevaPassword.equals(confirmarPassword)) {
            mostrarErrorEnPaso("Las contraseñas no coinciden.");
            return;
        }
        overlay.mostrar("Actualizando contraseña");
        new SwingWorker<Void, Void>() {
            private Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    autenticacionService.cambiarPasswordObligatorio(resultadoLoginActual.getIdUsuario(), nuevaPassword);
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                overlay.ocultar();
                if (error != null) {
                    mostrarErrorEnPaso(mensajeAmigable(error));
                    return;
                }
                if (!resultadoLoginActual.isTotpHabilitado()) {
                    iniciarEnrolamientoTotp();
                } else {
                    panelTotpVerificar.reset();
                    mostrarPaso(panelTotpVerificar);
                    panelTotpVerificar.enfocar();
                }
            }
        }.execute();
    }

    // ---------------------------------------------------------------
    // Paso 3a: enrolamiento TOTP (primera vez)
    // ---------------------------------------------------------------

    private void iniciarEnrolamientoTotp() {
        overlay.mostrar("Preparando verificación en dos pasos");
        new SwingWorker<AutenticacionService.ResultadoEnrolamientoTotp, Void>() {
            private Exception error;

            @Override
            protected AutenticacionService.ResultadoEnrolamientoTotp doInBackground() {
                try {
                    return autenticacionService.iniciarEnrolamientoTotp(
                            resultadoLoginActual.getIdUsuario(), resultadoLoginActual.getUsername());
                } catch (Exception ex) {
                    error = ex;
                    return null;
                }
            }

            @Override
            protected void done() {
                overlay.ocultar();
                if (error != null) {
                    mostrarErrorEnPaso(mensajeAmigable(error));
                    mostrarPaso(panelCredenciales);
                    return;
                }
                AutenticacionService.ResultadoEnrolamientoTotp resultado;
                try {
                    resultado = get();
                } catch (Exception ex) {
                    mostrarErrorEnPaso(mensajeAmigable(ex));
                    mostrarPaso(panelCredenciales);
                    return;
                }
                mostrarPaso(panelTotpEnrolar);
                BufferedImage qr = generarImagenQr(resultado.getUriEnrolamiento());
                panelTotpEnrolar.mostrarQr(qr, resultado.getSecretoBase32());
            }
        }.execute();
    }

    private BufferedImage generarImagenQr(String uri) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matriz = writer.encode(uri, BarcodeFormat.QR_CODE, QR_SIZE_PX, QR_SIZE_PX);
            return MatrixToImageWriter.toBufferedImage(matriz);
        } catch (Exception ex) {
            return null;
        }
    }

    private void confirmarEnrolamientoTotp(String codigo) {
        overlay.mostrar("Confirmando código");
        new SwingWorker<List<String>, Void>() {
            private Exception error;

            @Override
            protected List<String> doInBackground() {
                try {
                    return autenticacionService.confirmarEnrolamientoTotp(resultadoLoginActual.getIdUsuario(), codigo);
                } catch (Exception ex) {
                    error = ex;
                    return null;
                }
            }

            @Override
            protected void done() {
                overlay.ocultar();
                if (error != null) {
                    mostrarErrorEnPaso(mensajeAmigable(error));
                    return;
                }
                try {
                    panelTotpEnrolar.mostrarBackupCodes(get());
                } catch (Exception ex) {
                    mostrarErrorEnPaso(mensajeAmigable(ex));
                }
            }
        }.execute();
    }

    // ---------------------------------------------------------------
    // Paso 3b: verificación TOTP (logins posteriores)
    // ---------------------------------------------------------------

    private void verificarTotp(String codigo) {
        overlay.mostrar("Verificando código");
        new SwingWorker<Void, Void>() {
            private Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    autenticacionService.validarCodigoTotp(resultadoLoginActual.getIdUsuario(), codigo);
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                overlay.ocultar();
                if (error != null) {
                    mostrarErrorEnPaso(mensajeAmigable(error));
                    return;
                }
                completarLogin();
            }
        }.execute();
    }

    // ---------------------------------------------------------------
    // Paso final: abrir sesión
    // ---------------------------------------------------------------

    private void completarLogin() {
        overlay.mostrar("Ingresando");
        new SwingWorker<User, Void>() {
            private Exception error;

            @Override
            protected User doInBackground() {
                try {
                    return autenticacionService.completarLogin(resultadoLoginActual.getIdUsuario());
                } catch (Exception ex) {
                    error = ex;
                    return null;
                }
            }

            @Override
            protected void done() {
                overlay.ocultar();
                if (error != null) {
                    mostrarErrorEnPaso(mensajeAmigable(error));
                    return;
                }
                User usuario;
                try {
                    usuario = get();
                } catch (Exception ex) {
                    mostrarErrorEnPaso(mensajeAmigable(ex));
                    return;
                }
                SessionContext.setUsuarioActual(usuario);
                dispose();
                if (onLoginExitoso != null) {
                    SwingUtilities.invokeLater(onLoginExitoso);
                }
            }
        }.execute();
    }

    // ---------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------

    private void volverACredenciales() {
        resultadoLoginActual = null;
        txtPassword.setText("");
        limpiarError();
        mostrarPaso(panelCredenciales);
    }

    /**
     * Reemplaza el contenido visible por {@code contenido} y reajusta el tamaño de la ventana a su
     * alto natural (a diferencia de CardLayout, que reserva siempre el alto del paso más grande de
     * todos). Así cada paso ocupa solo el espacio que necesita, sin huecos vacíos ni scrollbar.
     */
    private void mostrarPaso(JComponent contenido) {
        panelPasos.removeAll();
        panelPasos.add(contenido, BorderLayout.CENTER);
        panelPasos.revalidate();
        ajustarTamano();
    }

    private void ajustarTamano() {
        pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        int altoMaximo = (int) (pantalla.height * 0.92);
        if (getHeight() > altoMaximo) {
            setSize(getWidth(), altoMaximo);
        }
        setLocationRelativeTo(null);
    }

    private void setEnabledFormulario(boolean habilitado) {
        txtUsuario.setEnabled(habilitado);
        txtPassword.setEnabled(habilitado);
        btnIngresar.setEnabled(habilitado);
    }

    private void limpiarError() {
        lblError.setText(" ");
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        ajustarTamano();
    }

    private void mostrarErrorEnPaso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Validación", JOptionPane.WARNING_MESSAGE);
    }

    private String mensajeAmigable(Exception ex) {
        String mensaje = ex.getMessage();
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return "No se pudo completar la operación. Intente nuevamente.";
        }
        return mensaje;
    }
}
