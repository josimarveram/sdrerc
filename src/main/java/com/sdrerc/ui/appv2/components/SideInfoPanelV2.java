package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class SideInfoPanelV2 extends JPanel {

    private final JPanel cards = new JPanel();

    public SideInfoPanelV2(String title) {
        super(new BorderLayout(0, 10));
        setBackground(AppV2Theme.SURFACE);
        setBorder(AppV2Theme.sectionBorder());

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppV2Theme.fontBold(16));
        lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);

        cards.setOpaque(false);
        cards.setLayout(new BoxLayout(cards, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(cards);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(lblTitle, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void addItem(String label, String value) {
        addItem(label, value, "");
    }

    public void addItem(String label, String value, String detail) {
        SideSummaryCardV2 card = new SideSummaryCardV2(label, value, detail);
        card.setAlignmentX(LEFT_ALIGNMENT);
        cards.add(card);
        cards.add(Box.createVerticalStrut(10));
        cards.revalidate();
        cards.repaint();
    }
}
