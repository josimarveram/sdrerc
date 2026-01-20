/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.ui.table;

import com.sdrerc.application.UserService;
import com.sdrerc.ui.views.role.JPanelListadoRole;
import com.sdrerc.ui.views.usuario.JPanelListadoUsuario;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author David
 */
public class ButtonEditorAsignar extends AbstractCellEditor
        implements TableCellEditor, ActionListener {

    private final JButton button = new JButton("Asignar");
    private final JTable table;
    private final JPanelListadoUsuario parent;
    private final UserService userService;

    private int row;

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
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return "Asignar";
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Long userId =
            (Long) table.getValueAt(row, parent.COL_ID);

        try {
            if (!userService.tieneRol(userId, "SUPERVISOR")) {

                JOptionPane.showMessageDialog(
                    parent,
                    "El usuario no tiene el rol SUPERVISOR",
                    "Acceso denegado",
                    JOptionPane.WARNING_MESSAGE
                );
                fireEditingStopped();
                return;
            }

            parent.abrirDlgAsignarAbogados(row);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                parent,
                ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }

        fireEditingStopped();
    }
}