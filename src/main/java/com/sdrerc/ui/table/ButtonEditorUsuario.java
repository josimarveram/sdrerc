/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.ui.table;

import com.sdrerc.ui.views.role.JPanelListadoRole;
import com.sdrerc.ui.views.usuario.JPanelListadoUsuario;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author David
 */
public class ButtonEditorUsuario extends AbstractCellEditor
        implements TableCellEditor, ActionListener {

    private JButton button;
    private JTable table;
    private JPanelListadoUsuario form;
    private int column;

    public ButtonEditorUsuario(JTable table, JPanelListadoUsuario form, int column) {
        this.table = table;
        this.form = form;
        this.column = column;

        button = new JButton();
        button.addActionListener(e -> {
            fireEditingStopped(); // 🔥 PRIMERO
        });
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column) {

        button.setText(value.toString());
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return button.getText();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int row = table.getSelectedRow();

        if (column == 4) { // EDITAR
            form.editarDesdeTabla(row);
        } else if (column == 5) { // ACTIVAR / INACTIVAR
            form.cambiarEstadoDesdeTabla(row);
        }

        fireEditingStopped();
    }
}