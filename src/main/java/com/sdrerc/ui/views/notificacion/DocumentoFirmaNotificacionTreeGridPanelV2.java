package com.sdrerc.ui.views.notificacion;

import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableScrollDiagnostics;
import com.sdrerc.ui.appv2.components.PremiumDateFieldV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Grilla de documentos del panel "Firma" de la Bandeja Asignación de Notificación. Mismo diseño
 * que {@code DocumentoVerificacionTreeGridPanelV2} (banner + grilla con icono Guardar por fila),
 * simplificada: sin jerarquía padre/hijo (Firma no maneja cartas de respuesta) y sin combo de
 * Estado editable (Firma solo corrige N° Documento/Fecha Emisión, nunca el estado directamente;
 * el estado avanza a Emitido como efecto del guardado, igual que antes).
 *
 * Una fila es editable solo si el documento está listo para firma: INTERMEDIO siempre, o FINAL
 * únicamente cuando su estado actual es VALIDADO (mismo criterio que ya usaba el panel de un
 * solo documento que este componente reemplaza).
 */
public class DocumentoFirmaNotificacionTreeGridPanelV2 extends JPanel {

    public interface SaveRowHandler {
        void guardarFila(Long idDocumentoAnalizado, String numeroDocumento, LocalDate fechaEmision) throws Exception;
    }

    private static final int COL_GUARDAR = 0;
    private static final int COL_TIPO = 1;
    private static final int COL_NUMERO = 2;
    private static final int COL_ESTADO = 3;
    private static final int COL_FECHA = 4;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final List<DocumentoRow> allRows = new ArrayList<DocumentoRow>();
    private final DocumentoTableModel model = new DocumentoTableModel();
    private final JTable tabla = new AppV2Table(model);
    private final JScrollPane scroll = new JScrollPane(tabla);
    private final JLabel lblBanner = crearBanner("Documentos a firmar");
    private final JLabel lblEstado = new JLabel("Seleccione un documento en la grilla de asignación.");

    private Long idExpediente;
    private SaveRowHandler saveRowHandler;
    private Runnable refreshHandler;

    public DocumentoFirmaNotificacionTreeGridPanelV2() {
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        configurarTabla();
        add(crearBloque(), BorderLayout.CENTER);
    }

    public void setHandlers(SaveRowHandler saveRowHandler, Runnable refreshHandler) {
        this.saveRowHandler = saveRowHandler;
        this.refreshHandler = refreshHandler;
    }

    public void setDocumentos(Long idExpediente, List<NotificacionAsignacionDocumentoDTO> documentos) {
        this.idExpediente = idExpediente;
        allRows.clear();
        if (documentos != null) {
            for (NotificacionAsignacionDocumentoDTO documento : documentos) {
                if (documento != null) {
                    allRows.add(DocumentoRow.from(documento));
                }
            }
        }
        allRows.sort(Comparator.comparing((DocumentoRow r) -> r.fechaDocumento == null ? LocalDate.MIN : r.fechaDocumento).reversed());
        model.setRows(allRows);
        actualizarEstado();
    }

    private JPanel crearBloque() {
        JPanel bloque = new JPanel(new BorderLayout(0, 4));
        bloque.setOpaque(false);
        bloque.add(lblBanner, BorderLayout.NORTH);
        bloque.add(scroll, BorderLayout.CENTER);
        lblEstado.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);
        bloque.add(lblEstado, BorderLayout.SOUTH);
        return bloque;
    }

    private static JLabel crearBanner(String texto) {
        JLabel label = new JLabel(texto);
        label.setOpaque(true);
        label.setBackground(AppV2Theme.SURFACE_ALT);
        label.setForeground(AppV2Theme.TEXT_SECONDARY);
        label.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return label;
    }

    private void configurarTabla() {
        tabla.setRowHeight(28);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        tabla.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        tabla.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        tabla.setGridColor(AppV2Theme.BORDER);
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setDefaultRenderer(Object.class, new DocumentoCellRenderer());

        tabla.getColumnModel().getColumn(COL_FECHA).setCellEditor(new FechaCellEditor());
        tabla.getColumnModel().getColumn(COL_NUMERO).setCellEditor(new DefaultCellEditor2());

        tabla.getColumnModel().getColumn(COL_GUARDAR).setCellRenderer(new GuardarRenderer());
        tabla.getColumnModel().getColumn(COL_GUARDAR).setCellEditor(
                new RowActionEditor(new SaveDocumentIcon(), "Guardar firma", row -> guardarFila(model.getRow(row))));

        TableColumn colGuardar = tabla.getColumnModel().getColumn(COL_GUARDAR);
        colGuardar.setPreferredWidth(44);
        colGuardar.setMinWidth(40);
        colGuardar.setMaxWidth(48);

        int[] widths = new int[]{240, 150, 150, 140};
        for (int i = 0; i < widths.length; i++) {
            TableColumn column = tabla.getColumnModel().getColumn(COL_TIPO + i);
            column.setPreferredWidth(widths[i]);
            column.setMinWidth(Math.min(widths[i], 95));
        }

        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scroll.setPreferredSize(new Dimension(820, 150));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        scroll.setWheelScrollingEnabled(true);
        AppV2TableScrollDiagnostics.log("NotificacionFirmaDocumentos", tabla, scroll);
    }

    private void guardarFila(DocumentoRow row) {
        if (row == null || row.id == null) {
            return;
        }
        if (!row.editable) {
            mostrarInfo(row.mensajeNoEditable());
            return;
        }
        if (saveRowHandler == null) {
            mostrarInfo("No se configuró el servicio de registro de firma.");
            return;
        }
        final Long idDocumento = row.id;
        final String numeroDocumento = row.numeroDocumento;
        final LocalDate fechaEmision = row.fechaDocumento;
        lblEstado.setText("Guardando firma...");
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                saveRowHandler.guardarFila(idDocumento, numeroDocumento, fechaEmision);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    lblEstado.setText("Firma registrada.");
                    JOptionPane.showMessageDialog(
                            DocumentoFirmaNotificacionTreeGridPanelV2.this,
                            "La firma se registró correctamente.",
                            "Documentos a firmar",
                            JOptionPane.INFORMATION_MESSAGE);
                    refrescar();
                } catch (Exception ex) {
                    mostrarError("No se pudo registrar la firma.", ex);
                    actualizarEstado();
                }
            }
        };
        worker.execute();
    }

    private void refrescar() {
        if (refreshHandler != null) {
            refreshHandler.run();
        }
    }

    private void actualizarEstado() {
        lblEstado.setText(model.getRowCount() == 0
                ? "Este expediente no tiene documentos."
                : model.getRowCount() + " documento(s) del expediente.");
    }

    private void mostrarInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Documentos a firmar", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String message, Exception ex) {
        String detail = ex == null || ex.getMessage() == null ? "" : "\n\n" + ex.getMessage();
        JOptionPane.showMessageDialog(this, message + detail, "Documentos a firmar", JOptionPane.ERROR_MESSAGE);
    }

    private static class DocumentoTableModel extends AbstractTableModel {
        private final List<DocumentoRow> rows = new ArrayList<DocumentoRow>();
        private final String[] columns = new String[]{"", "Tipo documento", "Número Documento", "Estado documento", "Fecha Emisión"};

        void setRows(List<DocumentoRow> nuevas) {
            rows.clear();
            rows.addAll(nuevas);
            fireTableDataChanged();
        }

        DocumentoRow getRow(int row) {
            return row < 0 || row >= rows.size() ? null : rows.get(row);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            DocumentoRow row = getRow(rowIndex);
            return row != null && row.editable
                    && (columnIndex == COL_NUMERO || columnIndex == COL_FECHA || columnIndex == COL_GUARDAR);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DocumentoRow row = getRow(rowIndex);
            if (row == null) {
                return "";
            }
            switch (columnIndex) {
                case COL_TIPO:
                    return row.tipoDocumento;
                case COL_NUMERO:
                    return row.numeroDocumento;
                case COL_ESTADO:
                    return row.estadoDocumento;
                case COL_FECHA:
                    return row.fechaDocumento != null ? DATE_FORMAT.format(row.fechaDocumento) : "";
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            DocumentoRow row = getRow(rowIndex);
            if (row == null) {
                return;
            }
            switch (columnIndex) {
                case COL_NUMERO:
                    row.numeroDocumento = text(value);
                    break;
                case COL_FECHA:
                    row.fechaDocumento = parseDate(value);
                    break;
                default:
                    break;
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    private static class DocumentoRow {
        private Long id;
        private String clasificacion;
        private String tipoDocumento;
        private CatalogoItemDTO estadoDocumento;
        private LocalDate fechaDocumento;
        private String numeroDocumento;
        private boolean editable;

        static DocumentoRow from(NotificacionAsignacionDocumentoDTO dto) {
            DocumentoRow row = new DocumentoRow();
            row.id = dto.getIdDocumentoAnalizado();
            row.clasificacion = dto.getClasificacion();
            row.tipoDocumento = dto.getTipoDocumento();
            row.estadoDocumento = new CatalogoItemDTO(dto.getEstadoDocumentoCodigo(), dto.getEstadoDocumento());
            row.fechaDocumento = dto.getFechaDocumento();
            row.numeroDocumento = dto.getNumeroDocumento();
            row.editable = esEditable(row.clasificacion, row.estadoDocumento);
            return row;
        }

        static boolean esEditable(String clasificacion, CatalogoItemDTO estadoDocumento) {
            String clasificacionUpper = clasificacion == null ? "" : clasificacion.toUpperCase();
            if ("INTERMEDIO".equals(clasificacionUpper)) {
                return true;
            }
            if ("FINAL".equals(clasificacionUpper)) {
                return estadoDocumento != null && "VALIDADO".equalsIgnoreCase(estadoDocumento.getCodigo());
            }
            return false;
        }

        String mensajeNoEditable() {
            String clasificacionUpper = clasificacion == null ? "" : clasificacion.toUpperCase();
            if ("FINAL".equals(clasificacionUpper)) {
                return "Este documento FINAL debe estar Validado para registrar la firma.";
            }
            return "Este documento no está listo para registrar la firma.";
        }
    }

    private static class DocumentoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            Object displayValue = value instanceof CatalogoItemDTO ? ((CatalogoItemDTO) value).getNombre() : value;
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, displayValue, isSelected, hasFocus, row, column);
            label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            label.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            label.setHorizontalAlignment(LEFT);
            int modelRow = table.convertRowIndexToModel(row);
            DocumentoRow documentoRow = ((DocumentoTableModel) table.getModel()).getRow(modelRow);
            boolean editable = documentoRow != null && documentoRow.editable;
            label.setForeground(editable ? AppV2Theme.TEXT_PRIMARY : AppV2Theme.TEXT_SECONDARY);
            if (!isSelected) {
                label.setBackground(Color.WHITE);
            }
            return label;
        }
    }

    private static class GuardarRenderer extends javax.swing.JButton implements TableCellRenderer {
        GuardarRenderer() {
            setText("");
            setIcon(new SaveDocumentIcon());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            int modelRow = table.convertRowIndexToModel(row);
            DocumentoTableModel model = (DocumentoTableModel) table.getModel();
            DocumentoRow documentoRow = model.getRow(modelRow);
            boolean editable = documentoRow != null && documentoRow.editable;
            setEnabled(editable);
            setToolTipText(editable ? "Guardar firma" : (documentoRow == null ? "" : documentoRow.mensajeNoEditable()));
            return this;
        }
    }

    private static class RowActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final javax.swing.JButton button = new javax.swing.JButton();
        private int editingRow = -1;

        RowActionEditor(Icon icon, String tooltip, IntConsumer action) {
            button.setText("");
            button.setIcon(icon);
            button.setToolTipText(tooltip);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.addActionListener(e -> {
                int row = editingRow;
                fireEditingStopped();
                action.accept(row);
            });
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
            editingRow = row;
            return button;
        }
    }

    private static class DefaultCellEditor2 extends javax.swing.DefaultCellEditor {
        DefaultCellEditor2() {
            super(new javax.swing.JTextField());
        }
    }

    private static class FechaCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final PremiumDateFieldV2 field = new PremiumDateFieldV2();

        FechaCellEditor() {
            field.setPreferredSize(new Dimension(130, 28));
        }

        @Override
        public Object getCellEditorValue() {
            LocalDate date = fechaSeleccionada(field);
            return date == null ? "" : DATE_FORMAT.format(date);
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
            LocalDate date = parseDate(value);
            field.setDate(date == null ? null : Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            return field;
        }
    }

    private static class SaveDocumentIcon implements Icon {
        private static final int SIZE = 18;

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color stroke = AppV2Theme.PRIMARY;
                Color fill = new Color(238, 247, 252);
                g2.setColor(fill);
                g2.fillRoundRect(x + 2, y + 2, 14, 14, 4, 4);
                g2.setColor(stroke);
                g2.drawRoundRect(x + 2, y + 2, 14, 14, 4, 4);
                g2.fillRect(x + 5, y + 3, 7, 4);
                g2.setColor(Color.WHITE);
                g2.fillRect(x + 6, y + 4, 4, 2);
                g2.setColor(stroke);
                g2.fillRoundRect(x + 5, y + 10, 8, 5, 2, 2);
                g2.setColor(Color.WHITE);
                g2.drawLine(x + 7, y + 12, x + 11, y + 12);
            } finally {
                g2.dispose();
            }
        }
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static LocalDate parseDate(Object value) {
        String text = text(value);
        if (text.isEmpty() || "-".equals(text)) {
            return null;
        }
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            return LocalDate.now();
        }
    }

    private static LocalDate fechaSeleccionada(PremiumDateFieldV2 field) {
        if (field == null || field.getDate() == null) {
            return null;
        }
        return field.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
