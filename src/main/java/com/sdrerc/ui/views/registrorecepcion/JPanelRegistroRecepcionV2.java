package com.sdrerc.ui.views.registrorecepcion;

import com.sdrerc.application.sdrercapp.GrupoFamiliarRegistroService;
import com.sdrerc.ui.appv2.components.AppV2SideActionPanel;
import com.sdrerc.ui.appv2.components.AppV2SideSectionPanel;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.AppV2ResponsiveGridPanel;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.views.expedienteconsola.JPanelBandejaExpedientesNueva;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

public class JPanelRegistroRecepcionV2 extends JPanel {

    private static final int TAB_REGISTRO_MANUAL = 2;

    private final MetricCardV2 cardPotencialDuplicado = new MetricCardV2("Potencial duplicado", "0", "Acta + titular", AppV2Theme.WARNING);
    private final MetricCardV2 cardPosibleGrupoFamiliar = new MetricCardV2("Posible Grupo Familiar", "0", "Apellidos coincidentes", AppV2Theme.TEAL);
    private final GrupoFamiliarRegistroService grupoFamiliarRegistroService = new GrupoFamiliarRegistroService();
    private JPanelBandejaExpedientesNueva bandejaRegistro;
    private JTabbedPane tabs;
    private JPanel metricasRegistro;
    private AppV2OperationalSplitPanel splitBandejaRegistro;
    private JPanelRegistrarGrupoFamiliarV2 panelRegistrarGrupoFamiliar;

    public JPanelRegistroRecepcionV2() {
        setLayout(new BorderLayout(8, 8));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        tabs = crearTabs();
        if (bandejaRegistro != null && bandejaRegistro.getPanelRecepcionWrapper() != null) {
            splitBandejaRegistro = new AppV2OperationalSplitPanel(
                    tabs,
                    bandejaRegistro.getPanelRecepcionWrapper(),
                    0,
                    380 + 46,
                    430 + 46);
            bandejaRegistro.vincularSplitRecepcion(splitBandejaRegistro);
            add(splitBandejaRegistro, BorderLayout.CENTER);
        } else {
            add(tabs, BorderLayout.CENTER);
        }
    }

    private JTabbedPane crearTabs() {
        tabs = new JTabbedPane();
        tabs.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        tabs.addTab("Bandeja Registro", crearBandejaRegistro());
        tabs.addTab("Carga diaria", new JPanelCargaDiariaRecepcionV2(new Runnable() {
            @Override
            public void run() {
                if (bandejaRegistro != null) {
                    bandejaRegistro.refrescar();
                }
            }
        }));
        tabs.addTab("Registro manual", crearPanelRegistroManual());
        tabs.addChangeListener(e -> {
            actualizarVisibilidadPanelRecepcion();
        });
        return tabs;
    }

    private JPanel crearBandejaRegistro() {
        JPanel panel = new JPanel(new BorderLayout(12, 14));
        panel.setBackground(AppV2Theme.BACKGROUND);

        metricasRegistro = new AppV2ResponsiveGridPanel(190, 2, 12, 0);
        metricasRegistro.add(cardPotencialDuplicado);
        metricasRegistro.add(cardPosibleGrupoFamiliar);

        bandejaRegistro = new JPanelBandejaExpedientesNueva(
                "REGISTRO",
                "Registro / Recepción",
                "Expedientes registrados o recibidos pendientes de gestión",
                true,
                false,
                metricasRegistro,
                new Consumer<Long>() {
                    @Override
                    public void accept(Long idExpediente) {
                        mostrarEdicionManual(idExpediente);
                    }
                },
                true);
        bandejaRegistro.vincularMetricasAlertasRegistro(cardPotencialDuplicado, cardPosibleGrupoFamiliar);
        cardPotencialDuplicado.setOnClick(() -> bandejaRegistro.alternarFiltroAlertaRegistro("POTENCIAL_DUPLICADO"));
        cardPosibleGrupoFamiliar.setOnClick(() -> bandejaRegistro.alternarFiltroAlertaRegistro("POSIBLE_GRUPO_FAMILIAR"));
        bandejaRegistro.setBorder(BorderFactory.createEmptyBorder());
        panel.add(bandejaRegistro, BorderLayout.CENTER);
        return panel;
    }

    private void actualizarVisibilidadPanelRecepcion() {
        if (splitBandejaRegistro == null) {
            return;
        }
        if (tabs != null && tabs.getSelectedIndex() != 0) {
            splitBandejaRegistro.setSideVisible(false);
        }
    }

    private JPanelRegistroManualRecepcionV2 crearPanelRegistroManual() {
        return new JPanelRegistroManualRecepcionV2(new Runnable() {
            @Override
            public void run() {
                refrescarBandeja();
            }
        });
    }

    private JPanel crearPanelRegistrarGrupoFamiliar() {
        panelRegistrarGrupoFamiliar = new JPanelRegistrarGrupoFamiliarV2();
        if (bandejaRegistro != null) {
            bandejaRegistro.setOnGrupoFamiliarSelectionChanged(() -> panelRegistrarGrupoFamiliar.actualizarEstado());
        }
        return panelRegistrarGrupoFamiliar;
    }

    private void mostrarEdicionManual(final Long idExpediente) {
        if (tabs == null || idExpediente == null) {
            return;
        }
        tabs.setTitleAt(TAB_REGISTRO_MANUAL, "Edición Manual");
        tabs.setComponentAt(TAB_REGISTRO_MANUAL, new JPanelRegistroManualRecepcionV2(
                idExpediente,
                new Runnable() {
                    @Override
                    public void run() {
                        refrescarBandeja();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        restaurarBandejaRegistro();
                    }
                }));
        tabs.setSelectedIndex(TAB_REGISTRO_MANUAL);
    }

    private void restaurarRegistroManual() {
        if (tabs == null) {
            return;
        }
        tabs.setTitleAt(TAB_REGISTRO_MANUAL, "Registro manual");
        tabs.setComponentAt(TAB_REGISTRO_MANUAL, crearPanelRegistroManual());
        tabs.setSelectedIndex(TAB_REGISTRO_MANUAL);
    }

    private void restaurarBandejaRegistro() {
        if (tabs == null) {
            return;
        }
        refrescarBandeja();
        tabs.setSelectedIndex(0);
    }

    private void refrescarBandeja() {
        if (bandejaRegistro != null) {
            bandejaRegistro.refrescar();
        }
        if (panelRegistrarGrupoFamiliar != null) {
            panelRegistrarGrupoFamiliar.actualizarEstado();
        }
    }

    private final class JPanelRegistrarGrupoFamiliarV2 extends AppV2SideActionPanel {

        private final JLabel lblSeleccionados = new JLabel("0");
        private final JLabel lblEstado = new JLabel("Sin expedientes marcados");
        private final JButton btnRegistrar = new JButton("Registrar G.F.");
        private final JButton btnLimpiar = new JButton("Limpiar selección");

        private JPanelRegistrarGrupoFamiliarV2() {
            super("Registrar G.F");
            setAccentColor(AppV2Theme.TEAL);

            AppV2SideSectionPanel resumen = new AppV2SideSectionPanel("Confirmación");
            resumen.addRow("Seleccionados", lblSeleccionados);
            resumen.addRow("Estado", lblEstado);
            addSection(resumen);

            JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            acciones.setOpaque(false);
            btnRegistrar.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
            btnLimpiar.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            btnRegistrar.addActionListener(e -> registrarGrupoFamiliar());
            btnLimpiar.addActionListener(e -> limpiarSeleccion());
            acciones.add(btnRegistrar);
            acciones.add(btnLimpiar);
            setFooter(acciones);
            actualizarEstado();
        }

        private void actualizarEstado() {
            int seleccionados = bandejaRegistro == null ? 0 : bandejaRegistro.contarIdsGrupoFamiliarSeleccionados();
            lblSeleccionados.setText(String.valueOf(seleccionados));
            lblEstado.setText(seleccionados > 0 ? "Listo para confirmar" : "Sin expedientes marcados");
            btnRegistrar.setEnabled(seleccionados > 0);
            btnLimpiar.setEnabled(seleccionados > 0);
        }

        private void registrarGrupoFamiliar() {
            if (bandejaRegistro == null || bandejaRegistro.contarIdsGrupoFamiliarSeleccionados() == 0) {
                JOptionPane.showMessageDialog(
                        JPanelRegistroRecepcionV2.this,
                        "Seleccione al menos un expediente marcado como posible grupo familiar.",
                        "Registrar G.F.",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int confirmacion = JOptionPane.showConfirmDialog(
                    JPanelRegistroRecepcionV2.this,
                    "¿Registrar grupo familiar en los expedientes seleccionados?",
                    "Registrar G.F.",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirmacion != JOptionPane.OK_OPTION) {
                return;
            }
            setEnabled(false);
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    bandejaRegistro.registrarGrupoFamiliarSeleccionados();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        refrescarBandeja();
                        JOptionPane.showMessageDialog(
                                JPanelRegistroRecepcionV2.this,
                                "Grupo familiar registrado y alerta desactivada.",
                                "Registrar G.F.",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                JPanelRegistroRecepcionV2.this,
                                ex.getMessage() == null ? "No se pudo registrar el grupo familiar." : ex.getMessage(),
                                "Registrar G.F.",
                                JOptionPane.ERROR_MESSAGE);
                    } finally {
                        setEnabled(true);
                        actualizarEstado();
                    }
                }
            };
            worker.execute();
        }

        private void limpiarSeleccion() {
            if (bandejaRegistro != null) {
                bandejaRegistro.limpiarSeleccionGrupoFamiliar();
            }
            actualizarEstado();
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            btnRegistrar.setEnabled(enabled && bandejaRegistro != null && bandejaRegistro.contarIdsGrupoFamiliarSeleccionados() > 0);
            btnLimpiar.setEnabled(enabled && bandejaRegistro != null && bandejaRegistro.contarIdsGrupoFamiliarSeleccionados() > 0);
        }
    }
}
