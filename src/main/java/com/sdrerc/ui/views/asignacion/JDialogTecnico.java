/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.sdrerc.ui.views.asignacion;

import com.sdrerc.application.TecnicoService;
import com.sdrerc.domain.model.Tecnico;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author David
 */
public class JDialogTecnico extends javax.swing.JDialog {

    private String tecnicoSeleccionado = null;
    private TableRowSorter<DefaultTableModel> sorter;
    
    private String idTecnicoSeleccionado = null;
    private String nombreTecnicoSeleccionado = null;

    public String getIdTecnicoSeleccionado() {
        return idTecnicoSeleccionado;
    }

    public String getNombreTecnicoSeleccionado() {
        return nombreTecnicoSeleccionado;
    }
    /**
     * Creates new form JDialogTecnico
     */
    public JDialogTecnico(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        configurarDialogo();
        cargarTecnicos();
        configurarFiltro();
    }

    private void configurarDialogo() {
        setTitle("Seleccionar abogado");
        setMinimumSize(new java.awt.Dimension(780, 420));
        setSize(new java.awt.Dimension(780, 420));

        tblTecnicos.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tblTecnicos.setRowHeight(28);
        tblTecnicos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTecnicos.setFillsViewportHeight(true);
        tblTecnicos.getTableHeader().setReorderingAllowed(false);
        tblTecnicos.getTableHeader().setResizingAllowed(true);

        tblTecnicos.getColumnModel().getColumn(0).setPreferredWidth(90);
        tblTecnicos.getColumnModel().getColumn(1).setPreferredWidth(150);
        tblTecnicos.getColumnModel().getColumn(2).setPreferredWidth(500);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        txtBuscar.setToolTipText("Buscar por ID, documento o nombre completo");
        tblTecnicos.setToolTipText("Doble clic para seleccionar");
        tblTecnicos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && tblTecnicos.getSelectedRow() >= 0) {
                    seleccionarTecnico();
                }
            }
        });
    }
    
    private void cargarTecnicos() {
        DefaultTableModel model = (DefaultTableModel) tblTecnicos.getModel(); 
        TecnicoService service = new TecnicoService();        
        List<Tecnico> tecnicos = service.listarTecnicos();
        
        model.setRowCount(0);
        for (Tecnico e : tecnicos) {
            Object[] fila = {
                    e.getIdTecnico(),
                    e.getNumeroDocumento(),
                    e.getNombreCompleto()
            };

            model.addRow(fila);
        }
        

        // Ejemplo — aquí colocas tus datos reales desde BD
        /*
        model.addRow(new Object[]{"001", "Juan Pérez", "987654321"});
        model.addRow(new Object[]{"002", "Luis Ramírez", "912345678"});
        model.addRow(new Object[]{"003", "Carlos Santos", "956478213"});
        model.addRow(new Object[]{"004", "Ana Torres", "945612378"});
        */
    }
    
    private void configurarFiltro() {
        
        DefaultTableModel model = (DefaultTableModel) tblTecnicos.getModel();
        sorter = new TableRowSorter<>(model);
        tblTecnicos.setRowSorter(sorter);

        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filtrar();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filtrar();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filtrar();
            }
        });
    }
    
    private void filtrar() {
        String texto = txtBuscar.getText();

        if (texto.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(
                RowFilter.regexFilter("(?i)" + Pattern.quote(texto), 0, 1, 2)
                // 0 = columna ID
                // 1 = columna Documento
                // 2 = columna Nombre
                // (?i) = No distingue mayúsculas/minúsculas
            );
        }
    }

    public String getTecnicoSeleccionado() {
        return tecnicoSeleccionado;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblTecnicos = new javax.swing.JTable();
        txtBuscar = new javax.swing.JTextField();
        btnSeleccionar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tblTecnicos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "N° DOCUMENTO", "NOMBRE COMPLETO DEL ABOGADO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblTecnicos);

        txtBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarActionPerformed(evt);
            }
        });

        btnSeleccionar.setText("SELECCIONAR");
        btnSeleccionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSeleccionarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSeleccionar))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 740, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSeleccionar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSeleccionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSeleccionarActionPerformed
        seleccionarTecnico();
    }//GEN-LAST:event_btnSeleccionarActionPerformed

    private void seleccionarTecnico() {
        int fila = tblTecnicos.getSelectedRow();
        if (fila >= 0) {
            int filaReal = tblTecnicos.convertRowIndexToModel(fila);
            tecnicoSeleccionado = tblTecnicos.getModel().getValueAt(filaReal, 1).toString();
            idTecnicoSeleccionado = tblTecnicos.getModel().getValueAt(filaReal, 0).toString();
            nombreTecnicoSeleccionado = tblTecnicos.getModel().getValueAt(filaReal, 2).toString();
            dispose();
        }
    }

    private void txtBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBuscarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(JDialogTecnico.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JDialogTecnico.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JDialogTecnico.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JDialogTecnico.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JDialogTecnico dialog = new JDialogTecnico(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSeleccionar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblTecnicos;
    private javax.swing.JTextField txtBuscar;
    // End of variables declaration//GEN-END:variables
}
