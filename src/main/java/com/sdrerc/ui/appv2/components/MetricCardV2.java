package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MetricCardV2 extends JPanel {

    private final JLabel lblValue;
    private final Color baseBackground = AppV2Theme.SURFACE;
    private final Color selectedBackground = AppV2Theme.SURFACE_ALT;
    private Runnable clickAction;
    private boolean selected;

    public MetricCardV2(String title, String value, String caption, Color accent) {
        super(new BorderLayout(6, 3));
        setBackground(baseBackground);
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
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (clickAction != null) {
                    clickAction.run();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        };
        instalarMouseRecursivo(this, mouseAdapter);
    }

    public void setValue(String value) {
        lblValue.setText(value == null ? "" : value);
    }

    public void setOnClick(Runnable clickAction) {
        this.clickAction = clickAction;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setBackground(selected ? selectedBackground : baseBackground);
        setOpaque(true);
        repaint();
    }

    public boolean isSelected() {
        return selected;
    }

    private void instalarMouseRecursivo(Component component, MouseAdapter mouseAdapter) {
        component.addMouseListener(mouseAdapter);
        if (component instanceof JPanel) {
            for (Component child : ((JPanel) component).getComponents()) {
                instalarMouseRecursivo(child, mouseAdapter);
            }
        }
    }
}
