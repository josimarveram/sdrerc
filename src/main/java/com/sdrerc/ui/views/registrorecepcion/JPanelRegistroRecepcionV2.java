package com.sdrerc.ui.views.registrorecepcion;

import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.AppV2ResponsiveGridPanel;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.views.expedienteconsola.JPanelBandejaExpedientesNueva;
import java.awt.BorderLayout;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class JPanelRegistroRecepcionV2 extends JPanel {

    private static final int TAB_REGISTRO_MANUAL = 2;

    private JPanelBandejaExpedientesNueva bandejaRegistro;
    private JTabbedPane tabs;
    private JPanel metricasRegistro;
    private AppV2OperationalSplitPanel splitBandejaRegistro;

    public JPanelRegistroRecepcionV2() {
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());
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
        tabs.addChangeListener(e -> actualizarVisibilidadPanelRecepcion());
        return tabs;
    }

    private JPanel crearBandejaRegistro() {
        JPanel panel = new JPanel(new BorderLayout(12, 14));
        panel.setBackground(AppV2Theme.BACKGROUND);

        metricasRegistro = new AppV2ResponsiveGridPanel(190, 4, 12, 0);
        metricasRegistro.add(new MetricCardV2("En registro", "-", "Pendiente de métrica", AppV2Theme.INFO));
        metricasRegistro.add(new MetricCardV2("Recepcionados", "-", "Preparado para SDRERC_APP", AppV2Theme.TEAL));
        metricasRegistro.add(new MetricCardV2("Observados", "-", "Validación futura", AppV2Theme.WARNING));
        metricasRegistro.add(new MetricCardV2("Duplicados", "-", "Conservados para revisión", AppV2Theme.INDIGO));

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
                        restaurarRegistroManual();
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

    private void refrescarBandeja() {
        if (bandejaRegistro != null) {
            bandejaRegistro.refrescar();
        }
    }
}
