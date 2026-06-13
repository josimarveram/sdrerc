package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MetricCardV2 extends JPanel {

    private final JLabel lblValue;

    public MetricCardV2(String title, String value, String caption, Color accent) {
        super(new BorderLayout(6, 3));
        setBackground(AppV2Theme.SURFACE);
        setPreferredSize(new Dimension(0, 76));
        setMinimumSize(new Dimension(0, 68));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AppV2Theme.BORDER),
                        BorderFactory.createEmptyBorder(7, 10, 7, 10))));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblTitle.setForeground(AppV2Theme.TEXT_SECONDARY);

        lblValue = new JLabel(value);
        lblValue.setFont(AppV2Theme.fontBold(22));
        lblValue.setForeground(AppV2Theme.TEXT_PRIMARY);

        JLabel lblCaption = new JLabel(caption);
        lblCaption.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblCaption.setForeground(AppV2Theme.MUTED);

        add(lblTitle, BorderLayout.NORTH);
        add(lblValue, BorderLayout.CENTER);
        add(lblCaption, BorderLayout.SOUTH);
    }

    public void setValue(String value) {
        lblValue.setText(value == null ? "" : value);
    }
}
