package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

public class AppV2TablePanel extends JPanel {

    private static final String CARD_TABLE = "table";
    private static final String CARD_EMPTY = "empty";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final JScrollPane scrollPane;

    public AppV2TablePanel(JTable table, String emptyTitle, String emptyDetail) {
        super(new BorderLayout());
        setOpaque(false);

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(24);
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);

        JPanel emptyWrapper = new JPanel(new BorderLayout());
        emptyWrapper.setBackground(AppV2Theme.SURFACE);
        emptyWrapper.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        emptyWrapper.add(new EmptyStatePanelV2(emptyTitle, emptyDetail), BorderLayout.CENTER);

        cards.add(scrollPane, CARD_TABLE);
        cards.add(emptyWrapper, CARD_EMPTY);
        add(cards, BorderLayout.CENTER);
        AppV2TableScrollDiagnostics.log("AppV2TablePanel", table, scrollPane);
        SwingUtilities.invokeLater(() -> AppV2TableScrollDiagnostics.log(
                "AppV2TablePanel.afterLayout", table, scrollPane));
        setEmpty(true);
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public void setEmpty(boolean empty) {
        cardLayout.show(cards, empty ? CARD_EMPTY : CARD_TABLE);
    }
}
