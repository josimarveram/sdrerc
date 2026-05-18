package com.sdrerc.ui.common.swing;

import com.sdrerc.domain.model.PlazoAtencionResultado;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class PlazoAtencionCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        PlazoAtencionResultado plazo = value instanceof PlazoAtencionResultado
                ? (PlazoAtencionResultado) value
                : PlazoAtencionResultado.sinConfig();

        label.setText(plazo.getTexto());
        label.setToolTipText(plazo.getTooltip());
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        label.setOpaque(true);

        if (!isSelected) {
            aplicarColor(label, plazo.getNivel());
        }
        return label;
    }

    private void aplicarColor(JLabel label, PlazoAtencionResultado.Nivel nivel)
    {
        switch (nivel) {
            case VERDE:
                label.setBackground(new Color(220, 252, 231));
                label.setForeground(new Color(22, 101, 52));
                break;
            case AMARILLO:
                label.setBackground(new Color(254, 249, 195));
                label.setForeground(new Color(133, 77, 14));
                break;
            case ROJO:
                label.setBackground(new Color(254, 226, 226));
                label.setForeground(new Color(153, 27, 27));
                break;
            case VENCIDO:
                label.setBackground(new Color(248, 113, 113));
                label.setForeground(Color.WHITE);
                break;
            case SIN_CONFIG:
            default:
                label.setBackground(new Color(241, 245, 249));
                label.setForeground(new Color(100, 116, 139));
                break;
        }
    }
}
