package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;

public final class AppV2ExpedientePanelFactory {

    private AppV2ExpedientePanelFactory() {
    }

    public static JPanel crearPanelBusquedaEstiloRegistro(
            String etiquetaBusqueda,
            Component campoBusqueda,
            Component acciones,
            JComponent fechaDesde,
            JComponent fechaHasta,
            Component estado,
            JCheckBox grupoFamiliar,
            JSpinner limite) {
        AppV2FilterPanel filtros = new AppV2FilterPanel();

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        JPanel filaBusqueda = new JPanel(new GridBagLayout());
        filaBusqueda.setOpaque(false);
        GridBagConstraints gbcBusqueda = constraintsRow();
        filaBusqueda.add(crearLabel(etiquetaBusqueda), gbcBusqueda);

        gbcBusqueda.gridx = 1;
        gbcBusqueda.weightx = 1.0;
        gbcBusqueda.fill = GridBagConstraints.HORIZONTAL;
        gbcBusqueda.insets = new Insets(0, 0, 0, 14);
        filaBusqueda.add(campoBusqueda, gbcBusqueda);

        gbcBusqueda.gridx = 2;
        gbcBusqueda.weightx = 0;
        gbcBusqueda.fill = GridBagConstraints.NONE;
        gbcBusqueda.insets = new Insets(0, 0, 0, 0);
        filaBusqueda.add(acciones, gbcBusqueda);

        JPanel filaFechas = new JPanel(new GridBagLayout());
        filaFechas.setOpaque(false);
        GridBagConstraints gbcFechas = constraintsRow();
        filaFechas.add(crearCampoInline("Fecha desde", fechaDesde, 250), gbcFechas);
        gbcFechas.gridx = 1;
        filaFechas.add(crearCampoInline("Fecha hasta", fechaHasta, 250), gbcFechas);
        gbcFechas.gridx = 2;
        gbcFechas.weightx = 1.0;
        gbcFechas.fill = GridBagConstraints.HORIZONTAL;
        filaFechas.add(Box.createHorizontalGlue(), gbcFechas);

        JPanel filaEstado = new JPanel(new GridBagLayout());
        filaEstado.setOpaque(false);
        GridBagConstraints gbcEstado = constraintsRow();
        filaEstado.add(crearCampoInline("Estado", estado, 260), gbcEstado);

        gbcEstado.gridx = 1;
        filaEstado.add(crearFiltroGrupoFamiliarInline(grupoFamiliar), gbcEstado);

        gbcEstado.gridx = 2;
        filaEstado.add(crearCampoInline("Mostrar", limite, 86), gbcEstado);

        gbcEstado.gridx = 3;
        gbcEstado.weightx = 1.0;
        gbcEstado.fill = GridBagConstraints.HORIZONTAL;
        filaEstado.add(Box.createHorizontalGlue(), gbcEstado);

        contenido.add(filaBusqueda);
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(filaFechas);
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(filaEstado);
        filtros.add(contenido);
        return filtros;
    }

    public static JPanel crearPanelSeccionesResponsivo(AppV2SideSectionPanel... secciones) {
        AppV2ResponsiveGridPanel grid = new AppV2ResponsiveGridPanel(320, 2, 12, 12);
        if (secciones != null) {
            for (AppV2SideSectionPanel seccion : secciones) {
                if (seccion != null) {
                    grid.add(seccion);
                }
            }
        }
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(grid, BorderLayout.NORTH);
        return wrapper;
    }

    private static GridBagConstraints constraintsRow() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 12);
        return gbc;
    }

    private static JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        label.setForeground(AppV2Theme.TEXT_SECONDARY);
        return label;
    }

    private static JPanel crearCampoInline(String texto, Component control, int anchoPreferido) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        JLabel label = crearLabel(texto);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setPreferredSize(new Dimension(Math.max(78, label.getPreferredSize().width), control.getPreferredSize().height));
        panel.add(label, BorderLayout.WEST);
        panel.add(control, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(anchoPreferido, control.getPreferredSize().height));
        panel.setMinimumSize(new Dimension(Math.max(120, anchoPreferido), control.getPreferredSize().height));
        return panel;
    }

    private static JPanel crearFiltroGrupoFamiliarInline(JCheckBox chkFiltroGrupoFamiliar) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        JLabel label = crearLabel("Grupo familiar");
        panel.add(label, BorderLayout.WEST);
        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        checkPanel.setOpaque(false);
        chkFiltroGrupoFamiliar.setText("");
        checkPanel.add(chkFiltroGrupoFamiliar);
        panel.add(checkPanel, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(170, 34));
        panel.setMinimumSize(new Dimension(170, 34));
        return panel;
    }
}
