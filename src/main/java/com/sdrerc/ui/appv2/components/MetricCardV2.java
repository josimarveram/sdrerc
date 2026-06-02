package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MetricCardV2 extends JPanel {

    public MetricCardV2(String title, String value, String caption, Color accent) {
        super(new BorderLayout(8, 6));
        setBackground(AppV2Theme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                AppV2Theme.cardBorder()));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblTitle.setForeground(AppV2Theme.TEXT_SECONDARY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(AppV2Theme.fontBold(28));
        lblValue.setForeground(AppV2Theme.TEXT_PRIMARY);

        JLabel lblCaption = new JLabel(caption);
        lblCaption.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblCaption.setForeground(AppV2Theme.MUTED);

        add(lblTitle, BorderLayout.NORTH);
        add(lblValue, BorderLayout.CENTER);
        add(lblCaption, BorderLayout.SOUTH);
    }
}
