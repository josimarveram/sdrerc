package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class SideSummaryCardV2 extends JPanel {

    private static final int MIN_WIDTH = 250;
    private static final int BASE_HEIGHT = 72;

    public SideSummaryCardV2(String title, String value, String detail) {
        super(new GridBagLayout());
        setBackground(AppV2Theme.SURFACE_ALT);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        int valueRows = rowsFor(value, 34, 2);
        int detailRows = isBlank(detail) ? 0 : rowsFor(detail, 42, 2);
        int preferredHeight = BASE_HEIGHT + ((valueRows - 1) * 17) + (detailRows * 17);
        preferredHeight = Math.max(86, Math.min(preferredHeight, 132));
        setMinimumSize(new Dimension(MIN_WIDTH, 86));
        setPreferredSize(new Dimension(MIN_WIDTH, preferredHeight));
        setMaximumSize(new Dimension(Short.MAX_VALUE, preferredHeight));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        JTextArea lblTitle = text(title, AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL),
                AppV2Theme.TEXT_SECONDARY, 1);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        add(lblTitle, gbc);

        JTextArea lblValue = text(valueOrDash(value), AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM),
                AppV2Theme.TEXT_PRIMARY, valueRows);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, detailRows > 0 ? 4 : 0, 0);
        add(lblValue, gbc);

        if (detailRows > 0) {
            JTextArea lblDetail = text(detail, AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL),
                    AppV2Theme.MUTED, detailRows);
            gbc.gridy = 2;
            gbc.insets = new Insets(0, 0, 0, 0);
            add(lblDetail, gbc);
        }
    }

    private static JTextArea text(String value, java.awt.Font font, java.awt.Color color, int rows) {
        JTextArea area = new JTextArea(valueOrDash(value));
        area.setRows(rows);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(font);
        area.setForeground(color);
        area.setMargin(new Insets(0, 0, 0, 0));
        return area;
    }

    private static int rowsFor(String value, int charsPerLine, int maxRows) {
        if (isBlank(value)) {
            return 1;
        }
        int rows = (value.trim().length() + charsPerLine - 1) / charsPerLine;
        return Math.max(1, Math.min(rows, maxRows));
    }

    private static String valueOrDash(String value) {
        return isBlank(value) ? "-" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
