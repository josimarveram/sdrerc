package com.sdrerc.ui.views.verificacion;

import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoVerificacionDTO;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class DocumentoVerificacionTreeGridPanelV2 extends JPanel {

    public interface SaveRowHandler {
        void guardarFila(
                Long idDocumentoAnalizado,
                String estadoCodigo,
                String comentario,
                LocalDate fechaEmision,
                String numeroDocumento) throws Exception;
    }

    private static final int PADRE_COL_TIPO = 0;
    private static final int PADRE_COL_NUMERO = 1;
    private static final int PADRE_COL_ESTADO_DOCUMENTO = 2;
    private static final int PADRE_COL_FECHA = 3;
    private static final int PADRE_COL_COMENTARIO = 4;
    private static final int PADRE_COL_REQUIERE_RESPUESTA = 5;
    private static final int PADRE_COL_GUARDAR = 6;

    private static final int HIJO_COL_TIPO = 0;
    private static final int HIJO_COL_CONFIRMACION_RESPUESTA = 1;
    private static final int HIJO_COL_FECHA_RESPUESTA = 2;
    private static final int HIJO_COL_FECHA_PUBLICACION = 3;
    private static final int HIJO_COL_EXISTE_OPOSICION = 4;
    private static final int HIJO_COL_HOJA_ENVIO = 5;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> ESTADOS_VISIBLES_PADRE = java.util.Arrays.asList("EN_DESPACHO", "EMITIDO", "OBSERVADO");

    private final List<DocumentoRow> allRows = new ArrayList<DocumentoRow>();
    private final PadreTableModel padreModel = new PadreTableModel();
    private final HijoTableModel hijoModel = new HijoTableModel();
    private final JTable tablaPadre = new AppV2Table(padreModel);
    private final JTable tablaHijo = new AppV2Table(hijoModel);
    private final JScrollPane scrollPadre = new JScrollPane(tablaPadre);
    private final JScrollPane scrollHijo = new JScrollPane(tablaHijo);
    private final JLabel lblBannerPadre = crearBanner("Documentos de verificación");
    private final JLabel lblBannerHijo = crearBanner("Documentos de cartas de respuesta");
    private final JLabel lblEstado = new JLabel("Seleccione un expediente para revisar sus documentos.");

    private List<CatalogoItemDTO> estados = new ArrayList<CatalogoItemDTO>();
    private Long idExpediente;
    private SaveRowHandler saveRowHandler;
    private Runnable refreshHandler;

    public DocumentoVerificacionTreeGridPanelV2() {
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        add(crearEncabezado(), BorderLayout.NORTH);
        configurarTablas();
        add(crearGrillas(), BorderLayout.CENTER);
    }

    public void setHandlers(SaveRowHandler saveRowHandler, Runnable refreshHandler) {
        this.saveRowHandler = saveRowHandler;
        this.refreshHandler = refreshHandler;
    }

    public void setEstadosDocumento(List<CatalogoItemDTO> estados) {
        this.estados = estados == null ? new ArrayList<CatalogoItemDTO>() : new ArrayList<CatalogoItemDTO>(estados);
        tablaPadre.getColumnModel().getColumn(PADRE_COL_ESTADO_DOCUMENTO)
                .setCellEditor(new DefaultCellEditor(comboCatalogo(this.estados)));
    }

    public void setDocumentos(Long idExpediente, List<DocumentoVerificacionDTO> documentos) {
        this.idExpediente = idExpediente;
        allRows.clear();
        if (documentos != null) {
            for (DocumentoVerificacionDTO documento : documentos) {
                if (documento != null) {
                    allRows.add(DocumentoRow.from(documento));
                }
            }
        }
        rebuildPadre();
        rebuildHijo(null);
        actualizarEstado();
    }

    private JPanel crearEncabezado() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        lblEstado.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);
        wrapper.add(lblEstado, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel crearGrillas() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(crearBloqueGrilla(lblBannerPadre, scrollPadre));
        panel.add(Box.createVerticalStrut(10));
        panel.add(crearBloqueGrilla(lblBannerHijo, scrollHijo));
        return panel;
    }

    private JPanel crearBloqueGrilla(JLabel banner, JScrollPane scroll) {
        JPanel bloque = new JPanel(new BorderLayout(0, 4));
        bloque.setOpaque(false);
        bloque.setAlignmentX(Component.LEFT_ALIGNMENT);
        bloque.add(banner, BorderLayout.NORTH);
        bloque.add(scroll, BorderLayout.CENTER);
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

    private void configurarTablas() {
        configurarTablaBase(tablaPadre);
        configurarTablaBase(tablaHijo);
        DocumentoCellRenderer textoRenderer = new DocumentoCellRenderer();
        tablaPadre.setDefaultRenderer(Object.class, textoRenderer);
        tablaPadre.setDefaultRenderer(Boolean.class, new RequiereRespuestaRenderer());
        tablaHijo.setDefaultRenderer(Object.class, textoRenderer);

        tablaPadre.getColumnModel().getColumn(PADRE_COL_FECHA).setCellEditor(new FechaCellEditor());
        tablaPadre.getColumnModel().getColumn(PADRE_COL_COMENTARIO).setCellEditor(new DefaultCellEditor(new JTextField()));
        tablaPadre.getColumnModel().getColumn(PADRE_COL_NUMERO).setCellEditor(new DefaultCellEditor(new JTextField()));

        tablaPadre.getColumnModel().getColumn(PADRE_COL_GUARDAR).setCellRenderer(
                new RowActionRenderer(new SaveDocumentIcon(), "Guardar documento revisado"));
        tablaPadre.getColumnModel().getColumn(PADRE_COL_GUARDAR).setCellEditor(
                new RowActionEditor(new SaveDocumentIcon(), "Guardar documento revisado",
                        row -> guardarFila(padreModel.getRow(row))));

        ajustarAnchos(tablaPadre, new int[]{200, 130, 150, 110, 240, 140});
        configurarColumnasAccion(tablaPadre, new int[]{PADRE_COL_GUARDAR});
        ajustarAnchos(tablaHijo, new int[]{170, 210, 150, 110, 120, 120});

        scrollPadre.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scrollPadre.setPreferredSize(new Dimension(820, 150));
        scrollHijo.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scrollHijo.setPreferredSize(new Dimension(820, 130));
        configurarScrollAnidado(scrollPadre);
        configurarScrollAnidado(scrollHijo);
        AppV2TableScrollDiagnostics.log("VerificacionDocumentosPadre", tablaPadre, scrollPadre);
        AppV2TableScrollDiagnostics.log("VerificacionDocumentosHijo", tablaHijo, scrollHijo);

        tablaPadre.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int row = tablaPadre.getSelectedRow();
            DocumentoRow seleccionado = row < 0 ? null : padreModel.getRow(row);
            rebuildHijo(seleccionado == null ? null : seleccionado.id);
        });
    }

    private void configurarTablaBase(JTable table) {
        table.setRowHeight(28);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        table.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        table.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        table.setGridColor(AppV2Theme.BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
    }

    private void configurarScrollAnidado(JScrollPane scroll) {
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        scroll.setWheelScrollingEnabled(true);
    }

    private void ajustarAnchos(JTable table, int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(widths[i]);
            column.setMinWidth(Math.min(widths[i], 95));
        }
    }

    private void configurarColumnasAccion(JTable table, int[] columnas) {
        for (int index : columnas) {
            TableColumn column = table.getColumnModel().getColumn(index);
            column.setPreferredWidth(44);
            column.setMinWidth(40);
            column.setMaxWidth(48);
        }
    }

    private void guardarFila(DocumentoRow row) {
        if (row == null || row.id == null) {
            return;
        }
        if (saveRowHandler == null) {
            mostrarInfo("No se configuró el servicio de guardado de documentos verificados.");
            return;
        }
        if ("OBSERVADO".equalsIgnoreCase(row.estadoDocumento == null ? "" : row.estadoDocumento.getCodigo())
                && (row.descripcion == null || row.descripcion.trim().isEmpty())) {
            mostrarInfo("Ingrese el comentario del documento revisado.");
            return;
        }
        final Long idDocumento = row.id;
        final String estadoCodigo = row.estadoDocumento == null ? "" : row.estadoDocumento.getCodigo();
        final String comentario = row.descripcion;
        final LocalDate fechaEmision = row.fechaDocumento;
        final String numeroDocumento = row.numeroDocumento;
        lblEstado.setText("Guardando documento revisado...");
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                saveRowHandler.guardarFila(idDocumento, estadoCodigo, comentario, fechaEmision, numeroDocumento);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    lblEstado.setText("Documento revisado actualizado.");
                    JOptionPane.showMessageDialog(
                            DocumentoVerificacionTreeGridPanelV2.this,
                            "El documento revisado se guardó correctamente.",
                            "Documentos verificados",
                            JOptionPane.INFORMATION_MESSAGE);
                    refrescar();
                } catch (Exception ex) {
                    mostrarError("No se pudo guardar el documento revisado.", ex);
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

    private void rebuildPadre() {
        List<DocumentoRow> visibles = new ArrayList<DocumentoRow>();
        for (DocumentoRow row : allRows) {
            if (row.nivel == 0 && row.estadoDocumento != null
                    && ESTADOS_VISIBLES_PADRE.contains(row.estadoDocumento.getCodigo().toUpperCase())) {
                visibles.add(row);
            }
        }
        visibles.sort(Comparator.comparingInt(r -> r.orden));
        padreModel.setRows(visibles);
    }

    private void rebuildHijo(Long idPadre) {
        List<DocumentoRow> visibles = new ArrayList<DocumentoRow>();
        if (idPadre != null) {
            for (DocumentoRow row : allRows) {
                if (row.nivel == 1 && idPadre.equals(row.parentId)) {
                    visibles.add(row);
                }
            }
            visibles.sort(Comparator.comparingInt(r -> r.orden));
        }
        hijoModel.setRows(visibles);
    }

    private void actualizarEstado() {
        lblEstado.setText(padreModel.getRowCount() + " documento(s) principal(es) para revisar.");
    }

    private JComboBox<CatalogoItemDTO> comboCatalogo(List<CatalogoItemDTO> items) {
        JComboBox<CatalogoItemDTO> combo = new JComboBox<CatalogoItemDTO>();
        combo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        if (items != null) {
            for (CatalogoItemDTO item : items) {
                if (item != null && item.hasCodigo()) {
                    combo.addItem(item);
                }
            }
        }
        combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value == null || value.getNombre() == null ? "" : value.getNombre());
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            label.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
            label.setBackground(isSelected ? new Color(220, 237, 255) : Color.WHITE);
            label.setForeground(AppV2Theme.TEXT_PRIMARY);
            return label;
        });
        return combo;
    }

    private void mostrarInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Documentos verificados", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String message, Exception ex) {
        String detail = ex == null || ex.getMessage() == null ? "" : "\n\n" + ex.getMessage();
        JOptionPane.showMessageDialog(this, message + detail, "Documentos verificados", JOptionPane.ERROR_MESSAGE);
    }

    private static class PadreTableModel extends AbstractTableModel {
        private final List<DocumentoRow> rows = new ArrayList<DocumentoRow>();
        private final String[] columns = new String[]{
            "Tipo documento", "Número Documento", "Estado documento", "Fecha Emisión",
            "Comentario", "¿Requiere respuesta?", ""
        };

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
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == PADRE_COL_REQUIERE_RESPUESTA ? Boolean.class : Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return getRow(rowIndex) != null
                    && (columnIndex == PADRE_COL_ESTADO_DOCUMENTO
                    || columnIndex == PADRE_COL_FECHA
                    || columnIndex == PADRE_COL_COMENTARIO
                    || columnIndex == PADRE_COL_NUMERO
                    || columnIndex == PADRE_COL_GUARDAR);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DocumentoRow row = getRow(rowIndex);
            if (row == null) {
                return "";
            }
            switch (columnIndex) {
                case PADRE_COL_TIPO:
                    return row.tipoDocumento;
                case PADRE_COL_NUMERO:
                    return row.numeroDocumento;
                case PADRE_COL_ESTADO_DOCUMENTO:
                    return row.estadoDocumento;
                case PADRE_COL_FECHA:
                    return row.fechaDocumento != null ? DATE_FORMAT.format(row.fechaDocumento) : "";
                case PADRE_COL_COMENTARIO:
                    return row.descripcion;
                case PADRE_COL_REQUIERE_RESPUESTA:
                    return row.requiereRespuesta;
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
                case PADRE_COL_NUMERO:
                    row.numeroDocumento = text(value);
                    break;
                case PADRE_COL_ESTADO_DOCUMENTO:
                    if (value instanceof CatalogoItemDTO) {
                        row.estadoDocumento = (CatalogoItemDTO) value;
                    }
                    break;
                case PADRE_COL_FECHA:
                    row.fechaDocumento = parseDate(value);
                    break;
                case PADRE_COL_COMENTARIO:
                    row.descripcion = text(value);
                    break;
                default:
                    break;
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    private static class HijoTableModel extends AbstractTableModel {
        private final List<DocumentoRow> rows = new ArrayList<DocumentoRow>();
        private final String[] columns = new String[]{
            "Tipo documento", "Confirmación de respuesta", "Fecha Respuesta",
            "Fecha Publicación", "Existe Oposición", "Hoja de Envío"
        };

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
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DocumentoRow row = getRow(rowIndex);
            if (row == null) {
                return "";
            }
            switch (columnIndex) {
                case HIJO_COL_TIPO:
                    return row.tipoDocumento;
                case HIJO_COL_CONFIRMACION_RESPUESTA:
                    return row.confirmacionRespuesta;
                case HIJO_COL_FECHA_RESPUESTA:
                    return row.fechaRespuesta != null ? DATE_FORMAT.format(row.fechaRespuesta) : "";
                case HIJO_COL_FECHA_PUBLICACION:
                    return row.fechaPublicacion != null ? DATE_FORMAT.format(row.fechaPublicacion) : "";
                case HIJO_COL_EXISTE_OPOSICION:
                    return row.existeOposicion == null ? "-" : (row.existeOposicion ? "Si" : "No");
                case HIJO_COL_HOJA_ENVIO:
                    return row.hojaEnvio;
                default:
                    return "";
            }
        }
    }

    private static class DocumentoRow {
        private Long id;
        private Long parentId;
        private int nivel;
        private int orden;
        private String tipoDocumento;
        private CatalogoItemDTO estadoDocumento;
        private LocalDate fechaDocumento;
        private String numeroDocumento;
        private String descripcion;
        private boolean requiereRespuesta;
        private String confirmacionRespuesta;
        private LocalDate fechaRespuesta;
        private LocalDate fechaPublicacion;
        private Boolean existeOposicion;
        private String hojaEnvio;

        static DocumentoRow from(DocumentoVerificacionDTO dto) {
            DocumentoRow row = new DocumentoRow();
            row.id = dto.getIdDocumentoAnalizado();
            row.parentId = dto.getIdDocumentoPadre();
            row.nivel = dto.getNivel();
            row.orden = dto.getOrden();
            row.tipoDocumento = dto.getTipoDocumento();
            row.estadoDocumento = new CatalogoItemDTO(dto.getEstadoDocumentoCodigo(), dto.getEstadoDocumento());
            row.fechaDocumento = dto.getFechaDocumento();
            row.numeroDocumento = dto.getNumeroDocumento();
            row.descripcion = dto.getDescripcion();
            row.requiereRespuesta = dto.isRequiereRespuesta();
            row.confirmacionRespuesta = dto.getConfirmacionRespuesta();
            row.fechaRespuesta = dto.getFechaRespuesta();
            row.fechaPublicacion = dto.getFechaPublicacion();
            row.existeOposicion = dto.getExisteOposicion();
            row.hojaEnvio = dto.getNumeroHojaEnvioRespuesta();
            return row;
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
            label.setForeground(AppV2Theme.TEXT_PRIMARY);
            label.setHorizontalAlignment(LEFT);
            if (!isSelected) {
                label.setBackground(Color.WHITE);
            }
            return label;
        }
    }

    private static class RequiereRespuestaRenderer extends JCheckBox implements TableCellRenderer {
        RequiereRespuestaRenderer() {
            setHorizontalAlignment(CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            setSelected(Boolean.TRUE.equals(value));
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    private static class RowActionRenderer extends JButton implements TableCellRenderer {
        RowActionRenderer(Icon icon, String tooltip) {
            setText("");
            setIcon(icon);
            setToolTipText(tooltip);
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
            setEnabled(table.isEnabled());
            return this;
        }
    }

    private static class RowActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton();
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
            button.setEnabled(table.isEnabled());
            return button;
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
