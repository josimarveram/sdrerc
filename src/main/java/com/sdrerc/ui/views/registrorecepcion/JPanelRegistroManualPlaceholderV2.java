package com.sdrerc.ui.views.registrorecepcion;

import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class JPanelRegistroManualPlaceholderV2 extends JPanel {

    public JPanelRegistroManualPlaceholderV2() {
        setLayout(new BorderLayout(12, 12));
        setBackground(AppV2Theme.BACKGROUND);
        add(crearHeader(), BorderLayout.NORTH);
        add(crearFormulario(), BorderLayout.CENTER);
        add(crearFooter(), BorderLayout.SOUTH);
    }

    private JPanel crearHeader() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.cardBorder());

        JLabel title = new JLabel("Registro manual individual");
        title.setFont(AppV2Theme.fontBold(18));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Formulario preparado para un futuro guardado controlado; este incremento no registra expedientes.");
        subtitle.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        subtitle.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel text = new JPanel(new BorderLayout(0, 4));
        text.setOpaque(false);
        text.add(title, BorderLayout.NORTH);
        text.add(subtitle, BorderLayout.CENTER);
        panel.add(text, BorderLayout.CENTER);
        panel.add(new BadgeV2("Solo UI", AppV2Theme.SOFT_ORANGE, AppV2Theme.WARNING), BorderLayout.EAST);
        return panel;
    }

    private JScrollPane crearFormulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppV2Theme.SURFACE);
        form.setBorder(AppV2Theme.sectionBorder());

        int row = 0;
        agregarCampo(form, row++, "Número de trámite", crearTextoPreparado("Pendiente de captura"));
        agregarCampo(form, row++, "Tipo de procedimiento", crearComboPreparado("Seleccionar tipo"));
        agregarCampo(form, row++, "Datos del titular", crearTextoPreparado("Nombres, documento y contacto"));
        agregarCampo(form, row++, "Datos del acta", crearTextoPreparado("Tipo de acta, número, fecha y oficina"));
        agregarCampo(form, row++, "Remitente", crearTextoPreparado("Entidad o canal de recepción"));
        agregarCampo(form, row++, "Fecha de recepción", crearTextoPreparado("Pendiente de captura"));
        agregarCampo(form, row++, "Documentos", crearTextoPreparado("Adjuntos y sustento documental"));
        agregarCampo(form, row++, "Observaciones iniciales", crearAreaPreparada("Motivo, comentario o advertencia inicial"));
        agregarCampo(form, row++, "Número expediente generado", crearTextoPreparado("Se generará automáticamente al guardar"));
        agregarCampo(form, row, "Validación antes de guardar", crearTextoPreparado("Pendiente de reglas y verificación"));

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel crearFooter() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        JTextArea note = new JTextArea("El guardado, la generación real del número de expediente, el historial inicial y la validación contra SDRERC_APP se implementarán en otro incremento autorizado.");
        note.setEditable(false);
        note.setFocusable(false);
        note.setOpaque(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        note.setForeground(AppV2Theme.TEXT_SECONDARY);

        JButton btnValidar = new JButton("Validar");
        btnValidar.setEnabled(false);
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setEnabled(false);
        JPanel acciones = new JPanel();
        acciones.setOpaque(false);
        acciones.add(btnValidar);
        acciones.add(btnGuardar);

        panel.add(note, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.EAST);
        return panel;
    }

    private void agregarCampo(JPanel form, int row, String label, java.awt.Component value) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.NORTHWEST;
        gbcLabel.insets = new Insets(7, 0, 7, 18);

        GridBagConstraints gbcValue = new GridBagConstraints();
        gbcValue.gridx = 1;
        gbcValue.gridy = row;
        gbcValue.weightx = 1;
        gbcValue.fill = GridBagConstraints.HORIZONTAL;
        gbcValue.insets = new Insets(7, 0, 7, 0);

        JLabel lbl = new JLabel(label);
        lbl.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_BASE));
        lbl.setForeground(AppV2Theme.TEXT_SECONDARY);
        form.add(lbl, gbcLabel);
        form.add(value, gbcValue);
    }

    private JTextField crearTextoPreparado(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setEditable(false);
        field.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        field.setForeground(AppV2Theme.TEXT_SECONDARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        return field;
    }

    private JComboBox<String> crearComboPreparado(String text) {
        JComboBox<String> combo = new JComboBox<String>(new String[]{text});
        combo.setEnabled(false);
        combo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        return combo;
    }

    private JTextArea crearAreaPreparada(String text) {
        JTextArea area = new JTextArea(text, 3, 20);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        area.setForeground(AppV2Theme.TEXT_SECONDARY);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        return area;
    }
}
