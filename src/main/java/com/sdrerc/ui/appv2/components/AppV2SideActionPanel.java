package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

public class AppV2SideActionPanel extends JPanel {

    private final JPanel sections = new JPanel();
    private final JPanel footer = new JPanel(new BorderLayout());

    public AppV2SideActionPanel(String title) {
        super(new BorderLayout(0, 14));
        setPreferredSize(new Dimension(420, 0));
        setMinimumSize(new Dimension(390, 0));
        setMaximumSize(new Dimension(460, Integer.MAX_VALUE));
        setBackground(AppV2Theme.SURFACE);
        setBorder(AppV2Theme.sectionBorder());

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppV2Theme.fontBold(18));
        lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);
        header.add(lblTitle, BorderLayout.NORTH);
        header.add(new JSeparator(), BorderLayout.SOUTH);

        sections.setOpaque(false);
        sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(sections);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        footer.setOpaque(false);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    public void addSection(Component section) {
        sections.add(section);
        sections.add(Box.createVerticalStrut(12));
    }

    public void setFooter(Component component) {
        footer.removeAll();
        footer.add(component, BorderLayout.CENTER);
    }
}
