/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.sdrerc.ui.views.asignacion;

import com.sdrerc.application.TecnicoService;
import com.sdrerc.domain.model.Tecnico;
import com.sdrerc.ui.common.icon.IconUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
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
    private JPanel pnlContexto;
    private JLabel lblUsuarioValor;
    private JLabel lblNombreVisibleValor;
    private JLabel lblVinculoActualValor;
    private JLabel lblAyuda;
    private JButton btnCancelarUx;

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
        setTitle("Seleccionar persona operativa");
        setMinimumSize(new java.awt.Dimension(860, 560));
        setSize(new java.awt.Dimension(860, 560));

        tblTecnicos.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tblTecnicos.setRowHeight(34);
        tblTecnicos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTecnicos.setFillsViewportHeight(true);
        tblTecnicos.getTableHeader().setReorderingAllowed(false);
        tblTecnicos.getTableHeader().setResizingAllowed(false);
        tblTecnicos.getTableHeader().setFont(tblTecnicos.getTableHeader().getFont().deriveFont(Font.BOLD));
        tblTecnicos.setShowGrid(false);
        tblTecnicos.setIntercellSpacing(new Dimension(0, 0));

        tblTecnicos.getColumnModel().getColumn(0).setMinWidth(0);
        tblTecnicos.getColumnModel().getColumn(0).setPreferredWidth(0);
        tblTecnicos.getColumnModel().getColumn(0).setMaxWidth(0);
        tblTecnicos.getColumnModel().getColumn(1).setPreferredWidth(150);
        tblTecnicos.getColumnModel().getColumn(1).setMinWidth(120);
        tblTecnicos.getColumnModel().getColumn(1).setMaxWidth(180);
        tblTecnicos.getColumnModel().getColumn(2).setHeaderValue("PERSONA OPERATIVA");
        tblTecnicos.getColumnModel().getColumn(2).setPreferredWidth(560);
        tblTecnicos.getColumnModel().getColumn(2).setCellRenderer(new TextoTooltipRenderer());

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        txtBuscar.setToolTipText("Buscar por nombre o documento");
        tblTecnicos.setToolTipText("Doble clic para seleccionar");
        btnSeleccionar.setText("Seleccionar persona");
        btnSeleccionar.setIcon(IconUtils.load("users.svg", 16));
        btnSeleccionar.setIconTextGap(8);
        tblTecnicos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && tblTecnicos.getSelectedRow() >= 0) {
                    seleccionarTecnico();
                }
            }
        });
        configurarLayoutPersonaOperativa();
    }

    private void configurarLayoutPersonaOperativa() {
        getContentPane().removeAll();
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        root.setBackground(new Color(245, 247, 250));

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);

        JLabel titulo = new JLabel("Gestionar persona operativa");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        titulo.setForeground(new Color(25, 42, 62));

        lblAyuda = new JLabel("Busque y seleccione la persona operativa.");
        lblAyuda.setForeground(new Color(73, 85, 99));

        JPanel tituloPanel = new JPanel(new BorderLayout(0, 4));
        tituloPanel.setOpaque(false);
        tituloPanel.add(titulo, BorderLayout.NORTH);
        tituloPanel.add(lblAyuda, BorderLayout.CENTER);

        pnlContexto = new JPanel(new GridBagLayout());
        pnlContexto.setBackground(Color.WHITE);
        pnlContexto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        lblUsuarioValor = new JLabel("-");
        lblNombreVisibleValor = new JLabel("-");
        lblVinculoActualValor = new JLabel("Sin persona operativa vinculada");
        agregarFilaContexto("Usuario:", lblUsuarioValor, 0);
        agregarFilaContexto("Nombre visible:", lblNombreVisibleValor, 1);
        agregarFilaContexto("Vínculo actual:", lblVinculoActualValor, 2);
        pnlContexto.setVisible(false);

        top.add(tituloPanel, BorderLayout.NORTH);
        top.add(pnlContexto, BorderLayout.CENTER);

        JPanel busquedaPanel = new JPanel(new GridBagLayout());
        busquedaPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.gridy = 0;
        gbc.gridx = 0;
        busquedaPanel.add(new JLabel("Buscar persona operativa"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtBuscar.setPreferredSize(new Dimension(360, 34));
        busquedaPanel.add(txtBuscar, gbc);

        JPanel botones = new JPanel(new GridBagLayout());
        botones.setOpaque(false);
        btnCancelarUx = new JButton("Cancelar");
        btnCancelarUx.setIcon(IconUtils.load("clear.svg", 16));
        btnCancelarUx.setIconTextGap(8);
        btnCancelarUx.addActionListener(e -> dispose());

        GridBagConstraints gbcBotones = new GridBagConstraints();
        gbcBotones.insets = new Insets(0, 0, 0, 8);
        botones.add(btnCancelarUx, gbcBotones);
        gbcBotones.insets = new Insets(0, 0, 0, 0);
        botones.add(btnSeleccionar, gbcBotones);

        JPanel bottom = new JPanel(new BorderLayout(12, 0));
        bottom.setOpaque(false);
        bottom.add(busquedaPanel, BorderLayout.CENTER);
        bottom.add(botones, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);
        root.add(jScrollPane1, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        revalidate();
        repaint();
    }

    private void agregarFilaContexto(String etiqueta, JLabel valor, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 4, 10);
        JLabel label = new JLabel(etiqueta);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        pnlContexto.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        valor.setForeground(new Color(55, 65, 81));
        pnlContexto.add(valor, gbc);
    }
    
    private void cargarTecnicos() {
        DefaultTableModel model = (DefaultTableModel) tblTecnicos.getModel(); 
        TecnicoService service = new TecnicoService();        
        List<Tecnico> tecnicos = service.listarTecnicos();
        
        model.setRowCount(0);
        for (Tecnico e : tecnicos) {
            Object[] fila = {
                    e.getIdTecnico(),
                    formatearDocumento(e.getNumeroDocumento()),
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

    public void setContextoPersonaOperativa(
            String username,
            String nombreVisible,
            String vinculoActual,
            Integer idTecnicoActual) {
        setTitle("Gestionar persona operativa");
        pnlContexto.setVisible(true);
        lblAyuda.setText("Busque y seleccione la persona operativa que desea vincular a esta cuenta.");
        btnSeleccionar.setText("Vincular persona");
        lblUsuarioValor.setText(textoSeguro(username));
        lblNombreVisibleValor.setText(textoSeguro(nombreVisible));
        lblVinculoActualValor.setText(
                vinculoActual == null || vinculoActual.trim().isEmpty()
                        ? "Sin persona operativa vinculada"
                        : vinculoActual
        );
        seleccionarTecnicoActual(idTecnicoActual);
    }

    private void seleccionarTecnicoActual(Integer idTecnicoActual) {
        if (idTecnicoActual == null) {
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tblTecnicos.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object value = model.getValueAt(i, 0);
            if (value != null && String.valueOf(value).equals(String.valueOf(idTecnicoActual))) {
                int viewRow = tblTecnicos.convertRowIndexToView(i);
                if (viewRow >= 0) {
                    tblTecnicos.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                    Rectangle rect = tblTecnicos.getCellRect(viewRow, 0, true);
                    tblTecnicos.scrollRectToVisible(rect);
                }
                return;
            }
        }
    }

    private String formatearDocumento(int numeroDocumento) {
        return numeroDocumento <= 0 ? "Sin documento" : String.valueOf(numeroDocumento);
    }

    private String textoSeguro(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
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

    private static class TextoTooltipRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = value == null ? "" : value.toString();
            label.setToolTipText(text);
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (column == 1) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
            }
            return label;
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
