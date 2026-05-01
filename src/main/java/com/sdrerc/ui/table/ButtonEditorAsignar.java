/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.ui.table;

import com.sdrerc.application.UserService;
import com.sdrerc.ui.views.usuario.JPanelListadoUsuario;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author David
 */
public class ButtonEditorAsignar extends AbstractCellEditor
        implements TableCellEditor, ActionListener {

    private final JButton button = new JButton("Equipo");
    private final JTable table;
    private final JPanelListadoUsuario parent;
    private final UserService userService;

    private int row;
    private boolean enabled;
    private Object currentValue;

    public ButtonEditorAsignar(JTable table,
                               JPanelListadoUsuario parent,
                               UserService userService) {

        this.table = table;
        this.parent = parent;
        this.userService = userService;

        button.addActionListener(this);
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected,
            int row, int column) {

        this.row = row;
        this.currentValue = value;
        enabled = !(value instanceof ButtonCellValue) || ((ButtonCellValue) value).isEnabled();
        button.setEnabled(enabled);
        button.setText(value == null ? "Equipo" : value.toString());
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return currentValue == null ? "Equipo" : currentValue;
    }

    @Override
    public boolean isCellEditable(EventObject event) {
        if (event instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent) event;
            int clickedRow = table.rowAtPoint(mouseEvent.getPoint());
            int clickedColumn = table.columnAtPoint(mouseEvent.getPoint());

            if (clickedRow >= 0 && clickedColumn >= 0) {
                Object value = table.getValueAt(clickedRow, clickedColumn);
                return !(value instanceof ButtonCellValue) || ((ButtonCellValue) value).isEnabled();
            }
        }

        return super.isCellEditable(event);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (enabled) {
            parent.abrirDlgAsignarAbogados(row);
        }
        fireEditingStopped();
    }
}
