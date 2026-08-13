package com.sdrerc.ui.views.administracion.plantillas;

import com.sdrerc.application.sdrercapp.PlantillaBloqueService;
import com.sdrerc.domain.dto.sdrercapp.PlantillaBloqueDTO;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

public class DlgBloquesPlantillaV2 extends JDialog {

    private static final String[] VARIABLES_SUGERIDAS = {
        "nomTitular", "dniTitular", "nomSolicitante", "dniSolicitante",
        "tipoActa", "nroActa", "tipoProcedimiento", "canalRecepcion",
        "fechaSolicitud", "nroTramiteWeb", "numDoc", "tipoDoc",
        "resAnalisis", "numDocInforme", "fechaDocInforme"
    };

    private final PlantillaBloqueService plantillaBloqueService;
    private final Long idTipoDocumentoAdjunto;

    private final DefaultListModel<PlantillaBloqueDTO> listModel = new DefaultListModel<PlantillaBloqueDTO>();
    private final JList<PlantillaBloqueDTO> lstBloques = new JList<PlantillaBloqueDTO>(listModel);

    private final JButton btnNuevo = new JButton("Nuevo bloque");
    private final JButton btnEliminar = new JButton("Eliminar bloque");
    private final JButton btnSubir = new JButton("Subir");
    private final JButton btnBajar = new JButton("Bajar");
    private final JButton btnGuardarBloque = new JButton("Guardar bloque");
    private final JButton btnCerrar = new JButton("Cerrar");

    private final JTextField txtTitulo = new JTextField();
    private final JTextArea txtContenido = new JTextArea(8, 30);
    private final JTextField txtSeccion = new JTextField();
    private final JCheckBox chkAplicarCondicion = new JCheckBox("Aplicar condición (mostrar solo si...)");
    private final JComboBox<String> cmbVariable = new JComboBox<String>(VARIABLES_SUGERIDAS);
    private final JComboBox<String> cmbOperador = new JComboBox<String>(new String[]{"Coincide con", "No coincide con"});
    private final JTextField txtValoresCondicion = new JTextField();
    private final JLabel lblAyuda = new JLabel(
            "<html>La plantilla base debe tener un párrafo con el texto exacto "
            + "<b>[[CONTENIDO]]</b> (o <b>[[CONTENIDO:seccion]]</b> si usa el campo Sección de abajo) "
            + "en el punto donde deben aparecer estos bloques.<br>"
            + "Deje \"Sección\" en blanco para el marcador sin nombre [[CONTENIDO]]; escriba un nombre "
            + "(ej: antecedentes) para que el bloque solo se inserte en [[CONTENIDO:antecedentes]], "
            + "útil cuando la plantilla tiene más de un punto de contenido dinámico.<br>"
            + "En \"Valores esperados\" separe varias opciones con coma (ej: OR Presencial, MP Presencial).</html>");

    private PlantillaBloqueDTO bloqueSeleccionado;

    public DlgBloquesPlantillaV2(
            Window parent, PlantillaBloqueService plantillaBloqueService, Long idTipoDocumentoAdjunto, String nombreTipoDocumento) {
        super(parent, "Bloques de contenido - " + nombreTipoDocumento, ModalityType.APPLICATION_MODAL);
        this.plantillaBloqueService = plantillaBloqueService;
        this.idTipoDocumentoAdjunto = idTipoDocumentoAdjunto;
        construirInterfaz();
        configurarEventos();
        cargarBloques();
        pack();
        setMinimumSize(new Dimension(820, 520));
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));

        add(lblAyuda, BorderLayout.NORTH);

        JPanel izquierda = new JPanel(new BorderLayout(6, 6));
        izquierda.setPreferredSize(new Dimension(260, 0));
        lstBloques.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstBloques.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel((index + 1) + ". " + resumenBloque(value));
            label.setOpaque(true);
            label.setBackground(isSelected ? AppV2Theme.PRIMARY : list.getBackground());
            label.setForeground(isSelected ? java.awt.Color.WHITE : AppV2Theme.TEXT_PRIMARY);
            label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            return label;
        });
        izquierda.add(new JScrollPane(lstBloques), BorderLayout.CENTER);

        JPanel accionesLista = new JPanel(new GridLayout(0, 1, 4, 4));
        accionesLista.add(btnNuevo);
        accionesLista.add(btnSubir);
        accionesLista.add(btnBajar);
        accionesLista.add(btnEliminar);
        izquierda.add(accionesLista, BorderLayout.SOUTH);

        JPanel derecha = new JPanel(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        form.add(new JLabel("Sección (opcional)"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        txtSeccion.setToolTipText("Deje en blanco para [[CONTENIDO]]; escriba un nombre para [[CONTENIDO:nombre]]");
        form.add(txtSeccion, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        form.add(new JLabel("Título del bloque"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(txtTitulo, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Contenido"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        txtContenido.setLineWrap(true);
        txtContenido.setWrapStyleWord(true);
        form.add(new JScrollPane(txtContenido), gbc);
        row++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        form.add(chkAplicarCondicion, gbc);
        row++;
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = row;
        form.add(new JLabel("Variable"), gbc);
        gbc.gridx = 1;
        cmbVariable.setEditable(true);
        form.add(cmbVariable, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        form.add(new JLabel("Operador"), gbc);
        gbc.gridx = 1;
        form.add(cmbOperador, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        form.add(new JLabel("Valores esperados"), gbc);
        gbc.gridx = 1;
        form.add(txtValoresCondicion, gbc);

        derecha.add(form, BorderLayout.CENTER);

        JPanel accionesForm = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        AppV2Theme.estilizarBotonPrimario(btnGuardarBloque);
        accionesForm.add(btnGuardarBloque);
        derecha.add(accionesForm, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.add(izquierda, BorderLayout.WEST);
        centro.add(derecha, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JPanel botonesInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botonesInferior.add(btnCerrar);
        add(botonesInferior, BorderLayout.SOUTH);

        habilitarCondicion(false);
    }

    private void configurarEventos() {
        btnCerrar.addActionListener(e -> dispose());
        btnNuevo.addActionListener(e -> nuevoBloque());
        btnGuardarBloque.addActionListener(e -> guardarBloque());
        btnEliminar.addActionListener(e -> eliminarBloque());
        btnSubir.addActionListener(e -> moverBloque(-1));
        btnBajar.addActionListener(e -> moverBloque(1));
        chkAplicarCondicion.addActionListener(e -> habilitarCondicion(chkAplicarCondicion.isSelected()));
        lstBloques.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
    }

    private void habilitarCondicion(boolean habilitado) {
        cmbVariable.setEnabled(habilitado);
        cmbOperador.setEnabled(habilitado);
        txtValoresCondicion.setEnabled(habilitado);
    }

    private void cargarBloques() {
        new SwingWorker<List<PlantillaBloqueDTO>, Void>() {
            @Override
            protected List<PlantillaBloqueDTO> doInBackground() throws Exception {
                return plantillaBloqueService.listarPorTipo(idTipoDocumentoAdjunto);
            }

            @Override
            protected void done() {
                try {
                    listModel.clear();
                    for (PlantillaBloqueDTO bloque : get()) {
                        listModel.addElement(bloque);
                    }
                    bloqueSeleccionado = null;
                    limpiarFormulario();
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar los bloques.", ex);
                }
            }
        }.execute();
    }

    private void cargarSeleccion() {
        bloqueSeleccionado = lstBloques.getSelectedValue();
        if (bloqueSeleccionado == null) {
            limpiarFormulario();
            return;
        }
        txtSeccion.setText(bloqueSeleccionado.getSeccion());
        txtTitulo.setText(bloqueSeleccionado.getTitulo());
        txtContenido.setText(bloqueSeleccionado.getContenido());
        boolean tieneCondicion = bloqueSeleccionado.tieneCondicion();
        chkAplicarCondicion.setSelected(tieneCondicion);
        habilitarCondicion(tieneCondicion);
        if (tieneCondicion) {
            cmbVariable.setSelectedItem(bloqueSeleccionado.getVariableCondicion());
            cmbOperador.setSelectedIndex(
                    PlantillaBloqueDTO.OPERADOR_NO_COINCIDE.equals(bloqueSeleccionado.getOperadorCondicion()) ? 1 : 0);
            txtValoresCondicion.setText(
                    bloqueSeleccionado.getValoresCondicion() == null
                            ? "" : bloqueSeleccionado.getValoresCondicion().replace("|", ", "));
        } else {
            txtValoresCondicion.setText("");
        }
    }

    private void limpiarFormulario() {
        txtSeccion.setText("");
        txtTitulo.setText("");
        txtContenido.setText("");
        chkAplicarCondicion.setSelected(false);
        habilitarCondicion(false);
        txtValoresCondicion.setText("");
    }

    private void nuevoBloque() {
        lstBloques.clearSelection();
        bloqueSeleccionado = null;
        limpiarFormulario();
        txtTitulo.requestFocusInWindow();
    }

    private void guardarBloque() {
        String contenido = txtContenido.getText();
        if (contenido == null || contenido.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el contenido del bloque.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PlantillaBloqueDTO dto = new PlantillaBloqueDTO();
        dto.setIdPlantillaBloque(bloqueSeleccionado == null ? null : bloqueSeleccionado.getIdPlantillaBloque());
        dto.setIdTipoDocumentoAdjunto(idTipoDocumentoAdjunto);
        dto.setOrden(bloqueSeleccionado == null ? 0 : bloqueSeleccionado.getOrden());
        dto.setSeccion(txtSeccion.getText());
        dto.setTitulo(txtTitulo.getText());
        dto.setContenido(contenido);
        if (chkAplicarCondicion.isSelected()) {
            Object variable = cmbVariable.getSelectedItem();
            String valores = txtValoresCondicion.getText();
            if (variable == null || String.valueOf(variable).trim().isEmpty() || valores == null || valores.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this, "Complete la variable y los valores esperados de la condición.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dto.setVariableCondicion(String.valueOf(variable).trim());
            dto.setOperadorCondicion(
                    cmbOperador.getSelectedIndex() == 1 ? PlantillaBloqueDTO.OPERADOR_NO_COINCIDE : PlantillaBloqueDTO.OPERADOR_COINCIDE);
            dto.setValoresCondicion(convertirValoresALista(valores));
        }
        btnGuardarBloque.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                plantillaBloqueService.guardar(dto);
                return null;
            }

            @Override
            protected void done() {
                btnGuardarBloque.setEnabled(true);
                try {
                    get();
                    cargarBloques();
                } catch (Exception ex) {
                    mostrarError("No se pudo guardar el bloque.", ex);
                }
            }
        }.execute();
    }

    private void eliminarBloque() {
        if (bloqueSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un bloque.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Se quitará este bloque de la plantilla (no se elimina el historial). ¿Desea continuar?",
                "Eliminar bloque",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        final Long id = bloqueSeleccionado.getIdPlantillaBloque();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                plantillaBloqueService.eliminar(id);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    cargarBloques();
                } catch (Exception ex) {
                    mostrarError("No se pudo eliminar el bloque.", ex);
                }
            }
        }.execute();
    }

    private void moverBloque(int desplazamiento) {
        int index = lstBloques.getSelectedIndex();
        int destino = index + desplazamiento;
        if (index < 0 || destino < 0 || destino >= listModel.getSize()) {
            return;
        }
        List<Long> idsOrdenados = new ArrayList<Long>();
        for (int i = 0; i < listModel.getSize(); i++) {
            idsOrdenados.add(listModel.getElementAt(i).getIdPlantillaBloque());
        }
        Long temp = idsOrdenados.get(index);
        idsOrdenados.set(index, idsOrdenados.get(destino));
        idsOrdenados.set(destino, temp);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                plantillaBloqueService.guardarOrden(idsOrdenados);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    cargarBloques();
                } catch (Exception ex) {
                    mostrarError("No se pudo reordenar los bloques.", ex);
                }
            }
        }.execute();
    }

    private static String convertirValoresALista(String valores) {
        String[] partes = valores.split(",");
        StringBuilder sb = new StringBuilder();
        for (String parte : partes) {
            String limpio = parte.trim();
            if (limpio.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append(limpio);
        }
        return sb.toString();
    }

    private static String resumenBloque(PlantillaBloqueDTO bloque) {
        if (bloque == null) {
            return "";
        }
        String titulo = bloque.getTitulo() == null || bloque.getTitulo().trim().isEmpty()
                ? recorte(bloque.getContenido())
                : bloque.getTitulo();
        String prefijoSeccion = bloque.tieneSeccion() ? "[" + bloque.getSeccion() + "] " : "";
        return prefijoSeccion + titulo + (bloque.tieneCondicion() ? "  [condicionado]" : "");
    }

    private static String recorte(String texto) {
        if (texto == null) {
            return "(sin contenido)";
        }
        String limpio = texto.trim().replaceAll("\\s+", " ");
        return limpio.length() > 40 ? limpio.substring(0, 40) + "..." : limpio;
    }

    private void mostrarError(String mensaje, Exception ex) {
        Throwable causa = ex;
        if (ex instanceof java.util.concurrent.ExecutionException && ex.getCause() != null) {
            causa = ex.getCause();
        }
        String detalle = causa == null ? "" : causa.getMessage();
        JOptionPane.showMessageDialog(this, mensaje + "\n" + detalle, "Bloques de plantilla", JOptionPane.ERROR_MESSAGE);
    }
}
