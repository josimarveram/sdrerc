package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class AppV2FlowContextPanel extends JPanel {

    private final JPanel items = new JPanel();

    public AppV2FlowContextPanel(String title, String detail) {
        super(new BorderLayout(0, 10));
        setBackground(AppV2Theme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AppV2Theme.TEAL),
                AppV2Theme.cardBorder()));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel(title == null ? "Contexto de flujo" : title);
        lblTitle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);

        JTextArea lblDetail = text(detail);
        lblDetail.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblDetail.setForeground(AppV2Theme.TEXT_SECONDARY);

        header.add(lblTitle, BorderLayout.NORTH);
        header.add(lblDetail, BorderLayout.CENTER);

        items.setOpaque(false);
        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));

        add(header, BorderLayout.NORTH);
        add(items, BorderLayout.CENTER);
    }

    public AppV2FlowContextPanel addItem(String title, String detail, Color accent) {
        ContextItem item = new ContextItem(title, detail, accent);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        items.add(item);
        items.add(Box.createVerticalStrut(8));
        return this;
    }

    private static JTextArea text(String value) {
        JTextArea area = new JTextArea(value == null ? "" : value);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new Insets(0, 0, 0, 0));
        return area;
    }

    private static final class ContextItem extends JPanel {

        private ContextItem(String title, String detail, Color accent) {
            super(new GridBagLayout());
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

            JLabel marker = new JLabel();
            marker.setOpaque(true);
            marker.setBackground(accent == null ? AppV2Theme.INFO : accent);
            marker.setPreferredSize(new Dimension(8, 38));

            JLabel lblTitle = new JLabel(title == null ? "-" : title);
            lblTitle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
            lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);

            JTextArea lblDetail = text(detail);
            lblDetail.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            lblDetail.setForeground(AppV2Theme.TEXT_SECONDARY);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridheight = 2;
            gbc.insets = new Insets(0, 0, 0, 10);
            gbc.anchor = GridBagConstraints.NORTHWEST;
            add(marker, gbc);

            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            add(lblTitle, gbc);

            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            add(lblDetail, gbc);
        }
    }
}
