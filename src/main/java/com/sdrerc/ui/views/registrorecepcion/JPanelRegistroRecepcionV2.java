package com.sdrerc.ui.views.registrorecepcion;

import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.ui.views.expedienteconsola.JPanelBandejaExpedientesNueva;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class JPanelRegistroRecepcionV2 extends JPanel {

    private JPanelBandejaExpedientesNueva bandejaRegistro;

    public JPanelRegistroRecepcionV2() {
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());
        add(crearTabs(), BorderLayout.CENTER);
    }

    private JTabbedPane crearTabs() {
        JTabbedPane tabs = new JTabbedPane();
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
        tabs.addTab("Registro manual", new JPanelRegistroManualPlaceholderV2());
        return tabs;
    }

    private JPanel crearBandejaRegistro() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(AppV2Theme.BACKGROUND);

        JPanel metricas = new JPanel(new GridLayout(1, 4, 12, 0));
        metricas.setOpaque(false);
        metricas.add(new MetricCardV2("En registro", "-", "Pendiente de métrica", AppV2Theme.INFO));
        metricas.add(new MetricCardV2("Recepcionados", "-", "Preparado para SDRERC_APP", AppV2Theme.TEAL));
        metricas.add(new MetricCardV2("Observados", "-", "Validación futura", AppV2Theme.WARNING));
        metricas.add(new MetricCardV2("Duplicados", "-", "Conservados para revisión", AppV2Theme.INDIGO));

        bandejaRegistro = new JPanelBandejaExpedientesNueva(
                "REGISTRO",
                "Registro / Recepción",
                "Expedientes registrados o recibidos pendientes de gestión",
                true,
                false);

        panel.add(metricas, BorderLayout.NORTH);
        panel.add(bandejaRegistro, BorderLayout.CENTER);
        return panel;
    }
}
