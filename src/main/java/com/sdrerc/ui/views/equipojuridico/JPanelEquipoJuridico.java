package com.sdrerc.ui.views.equipojuridico;

import com.sdrerc.application.EquipoJuridicoService;
import com.sdrerc.domain.model.EquipoJuridicoImportPreview;
import com.sdrerc.domain.model.EquipoJuridicoImportResult;
import com.sdrerc.ui.views.usuario.DlgPrevisualizarEquipoJuridicoExcel;
import com.sdrerc.ui.views.usuario.DlgRegistrarEquipoJuridico;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class JPanelEquipoJuridico extends JPanel implements EquipoJuridicoImportOwner {

    private final EquipoJuridicoService equipoJuridicoService = new EquipoJuridicoService();
    private final JButton btnNuevoEquipoJuridico = new JButton("Nuevo abogado/supervisor");
    private final JButton btnDescargarPlantilla = new JButton("Descargar plantilla Excel");
    private final JButton btnPrevisualizarExcel = new JButton("Previsualizar Excel");

    public JPanelEquipoJuridico() {
        configurarLayout();
        configurarBotones();
    }

    private void configurarLayout() {
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        setBackground(new Color(245, 247, 250));

        JLabel titulo = new JLabel("Equipo Jurídico");
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        titulo.setForeground(new Color(25, 42, 62));

        JLabel subtitulo = new JLabel("Gestión de abogados, supervisores e importación masiva");
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(93, 105, 119));

        JPanel headerText = new JPanel(new GridBagLayout());
        headerText.setOpaque(false);
        GridBagConstraints gbcHeader = new GridBagConstraints();
        gbcHeader.gridx = 0;
        gbcHeader.gridy = 0;
        gbcHeader.anchor = GridBagConstraints.WEST;
        headerText.add(titulo, gbcHeader);
        gbcHeader.gridy = 1;
        gbcHeader.insets = new Insets(4, 0, 0, 0);
        headerText.add(subtitulo, gbcHeader);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        acciones.add(btnNuevoEquipoJuridico);
        acciones.add(btnDescargarPlantilla);
        acciones.add(btnPrevisualizarExcel);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(headerText, BorderLayout.CENTER);
        header.add(acciones, BorderLayout.EAST);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));
        JLabel info = new JLabel("Use este módulo para registrar abogados y supervisores, descargar la plantilla oficial e importar información del equipo jurídico.");
        info.setForeground(new Color(73, 85, 99));
        info.setFont(new Font("Arial", Font.PLAIN, 14));
        infoPanel.add(info, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.add(infoPanel, BorderLayout.NORTH);

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    private JPanel crearPanelAcciones() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 231)),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 12);
        panel.add(btnNuevoEquipoJuridico, gbc);
        gbc.gridx = 1;
        panel.add(btnDescargarPlantilla, gbc);
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(btnPrevisualizarExcel, gbc);
        return panel;
    }

    private void configurarBotones() {
        Dimension principal = new Dimension(210, 38);
        Dimension plantilla = new Dimension(190, 38);
        Dimension previsualizar = new Dimension(170, 38);

        btnNuevoEquipoJuridico.setPreferredSize(principal);
        btnDescargarPlantilla.setPreferredSize(plantilla);
        btnPrevisualizarExcel.setPreferredSize(previsualizar);

        btnNuevoEquipoJuridico.setToolTipText("Registrar abogado o supervisor creando técnico, usuario y roles");
        btnDescargarPlantilla.setToolTipText("Descargar plantilla oficial para carga masiva de equipo jurídico");
        btnPrevisualizarExcel.setToolTipText("Leer plantilla Excel y previsualizar validaciones sin grabar en base de datos");

        btnNuevoEquipoJuridico.addActionListener(e -> abrirRegistroEquipoJuridico());
        btnDescargarPlantilla.addActionListener(e -> descargarPlantillaEquipoJuridico());
        btnPrevisualizarExcel.addActionListener(e -> previsualizarPlantillaEquipoJuridico());
    }

    private void abrirRegistroEquipoJuridico() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        DlgRegistrarEquipoJuridico dlg = new DlgRegistrarEquipoJuridico(parent, equipoJuridicoService);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void descargarPlantillaEquipoJuridico() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar plantilla Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));
        chooser.setSelectedFile(new File("plantilla_equipo_juridico_sdrerc.xlsx"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destino = asegurarExtensionXlsx(chooser.getSelectedFile());
        if (destino.exists()) {
            int confirmar = JOptionPane.showConfirmDialog(
                    this,
                    "El archivo ya existe. ¿Desea reemplazarlo?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirmar != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            generarPlantillaEquipoJuridico(destino);
            if (!destino.exists() || destino.length() == 0) {
                throw new IllegalStateException("El archivo no se generó en la ruta seleccionada.");
            }
            JOptionPane.showMessageDialog(
                    this,
                    "Plantilla Excel generada correctamente:\n" + destino.getAbsolutePath(),
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Throwable ex) {
            Throwable causa = obtenerCausaReal(ex);
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo generar la plantilla Excel: " + causa.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private File asegurarExtensionXlsx(File file) {
        String path = file.getAbsolutePath();
        if (path.toLowerCase().endsWith(".xlsx")) {
            return file;
        }
        return new File(path + ".xlsx");
    }

    private void generarPlantillaEquipoJuridico(File destino) throws Exception {
        try {
            Class<?> serviceClass = Class.forName("com.sdrerc.application.EquipoJuridicoPlantillaService");
            Object service = serviceClass.getDeclaredConstructor().newInstance();
            serviceClass.getMethod("generarPlantilla", File.class).invoke(service, destino);
        } catch (Throwable ex) {
            Class<?> fallbackClass = Class.forName("com.sdrerc.application.EquipoJuridicoPlantillaSimpleService");
            Object fallback = fallbackClass.getDeclaredConstructor().newInstance();
            fallbackClass.getMethod("generarPlantilla", File.class).invoke(fallback, destino);
        }
    }

    private void previsualizarPlantillaEquipoJuridico() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar plantilla Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            EquipoJuridicoImportPreview preview = previsualizarEquipoJuridico(chooser.getSelectedFile());
            Window parent = SwingUtilities.getWindowAncestor(this);
            DlgPrevisualizarEquipoJuridicoExcel dlg = new DlgPrevisualizarEquipoJuridicoExcel(parent, this, preview);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        } catch (Throwable ex) {
            Throwable causa = obtenerCausaReal(ex);
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo previsualizar la plantilla Excel: " + causa.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private EquipoJuridicoImportPreview previsualizarEquipoJuridico(File archivo) throws Exception {
        try {
            Class<?> serviceClass = Class.forName("com.sdrerc.application.EquipoJuridicoImportService");
            Object service = serviceClass.getDeclaredConstructor().newInstance();
            Object preview = serviceClass.getMethod("previsualizar", File.class).invoke(service, archivo);
            return (EquipoJuridicoImportPreview) preview;
        } catch (Throwable ex) {
            if (!esErrorDependenciaPoi(ex)) {
                throw convertirException(ex);
            }
            Class<?> fallbackClass = Class.forName("com.sdrerc.application.EquipoJuridicoImportSimpleService");
            Object fallback = fallbackClass.getDeclaredConstructor().newInstance();
            Object preview = fallbackClass.getMethod("previsualizar", File.class).invoke(fallback, archivo);
            return (EquipoJuridicoImportPreview) preview;
        }
    }

    @Override
    public EquipoJuridicoImportResult confirmarImportacionEquipoJuridico(
            EquipoJuridicoImportPreview preview,
            boolean incluirAdvertencias) throws Exception {
        try {
            Class<?> serviceClass = Class.forName("com.sdrerc.application.EquipoJuridicoImportService");
            Object service = serviceClass.getDeclaredConstructor().newInstance();
            Object result = serviceClass
                    .getMethod("confirmarImportacion", EquipoJuridicoImportPreview.class, boolean.class)
                    .invoke(service, preview, incluirAdvertencias);
            return (EquipoJuridicoImportResult) result;
        } catch (Throwable ex) {
            if (!esErrorDependenciaPoi(ex)) {
                throw convertirException(ex);
            }
            Class<?> fallbackClass = Class.forName("com.sdrerc.application.EquipoJuridicoImportSimpleService");
            Object fallback = fallbackClass.getDeclaredConstructor().newInstance();
            Object result = fallbackClass
                    .getMethod("confirmarImportacion", EquipoJuridicoImportPreview.class, boolean.class)
                    .invoke(fallback, preview, incluirAdvertencias);
            return (EquipoJuridicoImportResult) result;
        }
    }

    private boolean esErrorDependenciaPoi(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage() == null ? "" : current.getMessage();
            if (message.contains("org.apache.poi") || current instanceof NoClassDefFoundError) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private Exception convertirException(Throwable ex) {
        Throwable causa = obtenerCausaReal(ex);
        if (causa instanceof Exception) {
            return (Exception) causa;
        }
        return new Exception(causa.getMessage(), causa);
    }

    private Throwable obtenerCausaReal(Throwable ex) {
        if (ex instanceof InvocationTargetException && ((InvocationTargetException) ex).getTargetException() != null) {
            return ((InvocationTargetException) ex).getTargetException();
        }
        return ex.getCause() != null ? ex.getCause() : ex;
    }
}
