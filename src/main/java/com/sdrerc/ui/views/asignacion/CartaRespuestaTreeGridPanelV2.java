package com.sdrerc.ui.views.asignacion;

import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableScrollDiagnostics;
import com.sdrerc.ui.appv2.components.PremiumDateFieldV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.util.concurrent.atomic.AtomicLong;
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

public class CartaRespuestaTreeGridPanelV2 extends JPanel {

    public interface SaveRowHandler {
        void guardarFila(DocumentoAnalizadoDTO carta) throws Exception;
    }

    private static final String TIPO_CARTA_RESPUESTA_CODIGO = "ANALISIS_DOC_20_CARTA_RESPUESTA";
    private static final String TIPO_CARTA_RESPUESTA_NOMBRE = "Carta de Respuesta";
    private static final String TIPO_PEDIDO_CODIGO = "ANALISIS_DOC_21_PEDIDO";
    private static final String TIPO_PEDIDO_NOMBRE = "Pedido";

    private static final int PADRE_COL_TIPO = 0;
    private static final int PADRE_COL_NUMERO = 1;
    private static final int PADRE_COL_ESTADO = 2;
    private static final int PADRE_COL_FECHA = 3;
    private static final int PADRE_COL_COMENTARIO = 4;
    private static final int PADRE_COL_REQUIERE_RESPUESTA = 5;

    private static final int HIJO_COL_TIPO = 0;
    private static final int HIJO_COL_CONFIRMACION_RESPUESTA = 1;
    private static final int HIJO_COL_FECHA_RESPUESTA = 2;
    private static final int HIJO_COL_FECHA_PUBLICACION = 3;
    private static final int HIJO_COL_EXISTE_OPOSICION = 4;
    private static final int HIJO_COL_HOJA_ENVIO = 5;
    private static final int HIJO_COL_GUARDAR = 6;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final List<DocumentoRow> padreRows = new ArrayList<DocumentoRow>();
    private final List<DocumentoRow> hijoRows = new ArrayList<DocumentoRow>();
    private final PadreTableModel padreModel = new PadreTableModel();
    private final HijoTableModel hijoModel = new HijoTableModel();
    private final JTable tablaPadre = new AppV2Table(padreModel);
    private final JTable tablaHijo = new AppV2Table(hijoModel);
    private final JScrollPane scrollPadre = new JScrollPane(tablaPadre);
    private final JScrollPane scrollHijo = new JScrollPane(tablaHijo);
    private final JLabel lblBannerPadre = crearBanner("Documentos de análisis");
    private final JLabel lblBannerHijo = crearBanner("Documentos de cartas de respuesta");
    private final JButton btnAgregarRelacionado = new JButton("+ Relacionados");
    private final JButton btnAgregarDocumento = new JButton("+ Documentos");
    private final JLabel lblEstado = new JLabel("Seleccione un documento de análisis para vincular una carta de respuesta.");
    private final AtomicLong tempIds = new AtomicLong(-1L);

    private Long idExpediente;
    private DocumentoRow padreSeleccionado;
    private SaveRowHandler saveRowHandler;
    private Runnable refreshHandler;

    public CartaRespuestaTreeGridPanelV2() {
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        configurarTablas();
        add(crearGrillas(), BorderLayout.CENTER);
    }

    public void setHandlers(SaveRowHandler saveRowHandler, Runnable refreshHandler) {
        this.saveRowHandler = saveRowHandler;
        this.refreshHandler = refreshHandler;
    }

    public void setDocumentos(Long idExpediente, List<DocumentoAnalizadoDTO> documentosAnalisis, List<DocumentoAnalizadoDTO> cartasRespuesta) {
        this.idExpediente = idExpediente;
        padreRows.clear();
        hijoRows.clear();
        if (documentosAnalisis != null) {
            for (DocumentoAnalizadoDTO documento : documentosAnalisis) {
                if (documento != null && documento.isRequiereRespuesta()) {
                    padreRows.add(DocumentoRow.fromPadre(documento));
                }
            }
        }
        if (cartasRespuesta != null) {
            for (DocumentoAnalizadoDTO documento : cartasRespuesta) {
                if (documento != null) {
                    hijoRows.add(DocumentoRow.fromHijo(documento));
                }
            }
        }
        padreSeleccionado = null;
        rebuildPadre();
        rebuildHijo();
        actualizarEstado();
    }

    private JPanel crearGrillas() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(crearBloqueGrilla(lblBannerPadre, scrollPadre, null));
        panel.add(Box.createVerticalStrut(10));
        panel.add(crearBloqueGrilla(lblBannerHijo, scrollHijo, crearToolbarHijo()));
        return panel;
    }

    private JPanel crearToolbarHijo() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 4));
        wrapper.setOpaque(false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        estilizarPrimario(btnAgregarRelacionado);
        estilizarSecundario(btnAgregarDocumento);
        actions.add(btnAgregarRelacionado);
        actions.add(btnAgregarDocumento);
        lblEstado.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);
        wrapper.add(actions, BorderLayout.NORTH);
        wrapper.add(lblEstado, BorderLayout.SOUTH);
        btnAgregarRelacionado.addActionListener(e -> agregarRelacionado());
        btnAgregarDocumento.addActionListener(e -> agregarDocumento());
        return wrapper;
    }

    private JPanel crearBloqueGrilla(JLabel banner, JScrollPane scroll, JPanel toolbar) {
        JPanel bloque = new JPanel(new BorderLayout(0, 4));
        bloque.setOpaque(false);
        bloque.setAlignmentX(Component.LEFT_ALIGNMENT);
        bloque.add(banner, BorderLayout.NORTH);
        if (toolbar != null) {
            JPanel centro = new JPanel(new BorderLayout(0, 4));
            centro.setOpaque(false);
            centro.add(toolbar, BorderLayout.NORTH);
            centro.add(scroll, BorderLayout.CENTER);
            bloque.add(centro, BorderLayout.CENTER);
        } else {
            bloque.add(scroll, BorderLayout.CENTER);
        }
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

        tablaHijo.getColumnModel().getColumn(HIJO_COL_CONFIRMACION_RESPUESTA)
                .setCellEditor(new DefaultCellEditor(comboConfirmacionRespuesta()));
        tablaHijo.getColumnModel().getColumn(HIJO_COL_FECHA_RESPUESTA).setCellEditor(new FechaCellEditor());
        tablaHijo.getColumnModel().getColumn(HIJO_COL_EXISTE_OPOSICION)
                .setCellEditor(new DefaultCellEditor(comboSiNo()));
        tablaHijo.getColumnModel().getColumn(HIJO_COL_HOJA_ENVIO).setCellEditor(new DefaultCellEditor(new JTextField()));

        tablaHijo.getColumnModel().getColumn(HIJO_COL_GUARDAR).setCellRenderer(
                new RowActionRenderer(new SaveDocumentIcon(), "Guardar carta de respuesta"));
        tablaHijo.getColumnModel().getColumn(HIJO_COL_GUARDAR).setCellEditor(
                new RowActionEditor(new SaveDocumentIcon(), "Guardar carta de respuesta",
                        row -> guardarFila(hijoModel.getRow(row))));

        ajustarAnchos(tablaPadre, new int[]{200, 130, 150, 110, 240, 140});
        ajustarAnchos(tablaHijo, new int[]{170, 170, 130, 130, 130, 140});
        configurarColumnasAccion(tablaHijo, new int[]{HIJO_COL_GUARDAR});

        scrollPadre.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scrollPadre.setPreferredSize(new Dimension(820, 150));
        scrollHijo.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scrollHijo.setPreferredSize(new Dimension(820, 150));
        configurarScrollAnidado(scrollPadre);
        configurarScrollAnidado(scrollHijo);
        AppV2TableScrollDiagnostics.log("CartasRespuestaPadre", tablaPadre, scrollPadre);
        AppV2TableScrollDiagnostics.log("CartasRespuestaHijo", tablaHijo, scrollHijo);

        tablaPadre.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int row = tablaPadre.getSelectedRow();
            padreSeleccionado = row < 0 ? null : padreModel.getRow(row);
            actualizarEstado();
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

    private void agregarRelacionado() {
        if (padreSeleccionado == null) {
            mostrarInfo("Seleccione un documento de análisis para agregar la carta de respuesta.");
            return;
        }
        DocumentoRow hijo = DocumentoRow.nuevoHijo(
                tempIds.getAndDecrement(), padreSeleccionado.id, TIPO_CARTA_RESPUESTA_CODIGO, TIPO_CARTA_RESPUESTA_NOMBRE);
        hijoRows.add(hijo);
        rebuildHijo();
        seleccionarFilaHijo(hijo);
        actualizarEstado();
    }

    private void agregarDocumento() {
        DocumentoRow hijo = DocumentoRow.nuevoHijo(tempIds.getAndDecrement(), null, TIPO_PEDIDO_CODIGO, TIPO_PEDIDO_NOMBRE);
        hijoRows.add(hijo);
        rebuildHijo();
        seleccionarFilaHijo(hijo);
        actualizarEstado();
    }

    private void guardarFila(DocumentoRow row) {
        if (row == null) {
            return;
        }
        if (saveRowHandler == null) {
            mostrarInfo("No se configuró el servicio de guardado de cartas de respuesta.");
            return;
        }
        DocumentoAnalizadoDTO documento = row.toDocumento(idExpediente);
        lblEstado.setText("Guardando carta de respuesta...");
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                saveRowHandler.guardarFila(documento);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(
                            CartaRespuestaTreeGridPanelV2.this,
                            "La carta de respuesta se guardó correctamente.",
                            "Cartas de respuesta",
                            JOptionPane.INFORMATION_MESSAGE);
                    refrescar();
                } catch (Exception ex) {
                    mostrarError("No se pudo guardar la carta de respuesta.", ex);
                } finally {
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

    private void rebuildPadre() {
        List<DocumentoRow> visibles = new ArrayList<DocumentoRow>(padreRows);
        visibles.sort(Comparator.comparing((DocumentoRow r) -> r.fechaDocumento == null ? LocalDate.MIN : r.fechaDocumento).reversed());
        padreModel.setRows(visibles);
    }

    private void rebuildHijo() {
        List<DocumentoRow> visibles = new ArrayList<DocumentoRow>(hijoRows);
        visibles.sort(Comparator.comparing((DocumentoRow r) -> r.fechaDocumento == null ? LocalDate.MIN : r.fechaDocumento).reversed());
        hijoModel.setRows(visibles);
    }

    private void seleccionarFilaHijo(DocumentoRow row) {
        int index = hijoModel.indexOf(row);
        if (index >= 0) {
            tablaHijo.getSelectionModel().setSelectionInterval(index, index);
            tablaHijo.scrollRectToVisible(tablaHijo.getCellRect(index, 0, true));
        }
    }

    private JComboBox<String> comboConfirmacionRespuesta() {
        JComboBox<String> combo = new JComboBox<String>(new String[]{"Pendiente", "Si", "No"});
        combo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        return combo;
    }

    private JComboBox<String> comboSiNo() {
        JComboBox<String> combo = new JComboBox<String>(new String[]{"-", "Si", "No"});
        combo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        return combo;
    }

    private void actualizarEstado() {
        if (padreSeleccionado != null) {
            lblEstado.setText("Documento seleccionado: " + padreSeleccionado.tipoNombre
                    + ". " + hijoModel.getRowCount() + " carta(s) de respuesta registradas.");
        } else {
            lblEstado.setText(hijoModel.getRowCount() + " carta(s) de respuesta registradas.");
        }
    }

    private void estilizarPrimario(JButton button) {
        button.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        button.setBackground(AppV2Theme.PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
    }

    private void estilizarSecundario(JButton button) {
        button.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        button.setBackground(AppV2Theme.SURFACE);
        button.setForeground(AppV2Theme.TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));
    }

    private void mostrarInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Cartas de respuesta", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String message, Exception ex) {
        String detail = ex == null || ex.getMessage() == null ? "" : "\n\n" + ex.getMessage();
        JOptionPane.showMessageDialog(this, message + detail, "Cartas de respuesta", JOptionPane.ERROR_MESSAGE);
    }

    private static class PadreTableModel extends AbstractTableModel {
        private final List<DocumentoRow> rows = new ArrayList<DocumentoRow>();
        private final String[] columns = new String[]{
            "Tipo documento", "Número Documento", "Estado documento", "Fecha Emisión",
            "Comentario", "¿Requiere respuesta?"
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
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DocumentoRow row = getRow(rowIndex);
            if (row == null) {
                return "";
            }
            switch (columnIndex) {
                case PADRE_COL_TIPO:
                    return row.tipoNombre;
                case PADRE_COL_NUMERO:
                    return row.numeroDocumento;
                case PADRE_COL_ESTADO:
                    return row.estadoNombre;
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
    }

    private static class HijoTableModel extends AbstractTableModel {
        private final List<DocumentoRow> rows = new ArrayList<DocumentoRow>();
        private final String[] columns = new String[]{
            "Tipo documento", "Confirmación de respuesta", "Fecha Respuesta",
            "Fecha Publicación", "Existe Oposición", "Hoja de Envío", ""
        };

        void setRows(List<DocumentoRow> nuevas) {
            rows.clear();
            rows.addAll(nuevas);
            fireTableDataChanged();
        }

        DocumentoRow getRow(int row) {
            return row < 0 || row >= rows.size() ? null : rows.get(row);
        }

        int indexOf(DocumentoRow target) {
            return rows.indexOf(target);
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
            return getRow(rowIndex) != null
                    && columnIndex != HIJO_COL_TIPO
                    && columnIndex != HIJO_COL_FECHA_PUBLICACION;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DocumentoRow row = getRow(rowIndex);
            if (row == null) {
                return "";
            }
            switch (columnIndex) {
                case HIJO_COL_TIPO:
                    return row.tipoNombre;
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

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            DocumentoRow row = getRow(rowIndex);
            if (row == null) {
                return;
            }
            switch (columnIndex) {
                case HIJO_COL_CONFIRMACION_RESPUESTA:
                    row.confirmacionRespuesta = text(value);
                    break;
                case HIJO_COL_FECHA_RESPUESTA:
                    row.fechaRespuesta = parseDate(value);
                    break;
                case HIJO_COL_EXISTE_OPOSICION:
                    row.existeOposicion = "Si".equalsIgnoreCase(text(value)) ? Boolean.TRUE
                            : "No".equalsIgnoreCase(text(value)) ? Boolean.FALSE : null;
                    break;
                case HIJO_COL_HOJA_ENVIO:
                    row.hojaEnvio = text(value);
                    break;
                default:
                    break;
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    private static class DocumentoRow {
        private Long id;
        private Long parentId;
        private String tipoCodigo;
        private String tipoNombre;
        private String estadoCodigo;
        private String estadoNombre;
        private LocalDate fechaDocumento;
        private String numeroDocumento;
        private String descripcion;
        private boolean requiereRespuesta;
        private String confirmacionRespuesta = "";
        private LocalDate fechaRespuesta;
        private LocalDate fechaPublicacion;
        private Boolean existeOposicion;
        private String hojaEnvio = "";

        static DocumentoRow fromPadre(DocumentoAnalizadoDTO dto) {
            DocumentoRow row = new DocumentoRow();
            row.id = dto.getIdDocumentoAnalizado();
            row.tipoCodigo = dto.getTipoDocumentoCodigo();
            row.tipoNombre = dto.getTipoDocumentoNombre();
            row.estadoCodigo = dto.getEstadoDocumentoCodigo();
            row.estadoNombre = dto.getEstadoDocumentoNombre();
            row.fechaDocumento = dto.getFechaDocumento();
            row.numeroDocumento = dto.getNumeroDocumento();
            row.descripcion = dto.getDescripcion();
            row.requiereRespuesta = dto.isRequiereRespuesta();
            return row;
        }

        static DocumentoRow fromHijo(DocumentoAnalizadoDTO dto) {
            DocumentoRow row = new DocumentoRow();
            row.id = dto.getIdDocumentoAnalizado();
            row.parentId = dto.getIdDocumentoPadre();
            row.tipoCodigo = dto.getTipoDocumentoCodigo();
            row.tipoNombre = dto.getTipoDocumentoNombre();
            row.fechaDocumento = dto.getFechaDocumento();
            row.confirmacionRespuesta = dto.getConfirmacionRespuesta();
            row.fechaRespuesta = dto.getFechaRespuesta();
            row.fechaPublicacion = dto.getFechaPublicacion();
            row.existeOposicion = dto.getExisteOposicion();
            row.hojaEnvio = dto.getNumeroHojaEnvioRespuesta();
            return row;
        }

        static DocumentoRow nuevoHijo(Long id, Long parentId, String tipoCodigo, String tipoNombre) {
            DocumentoRow row = new DocumentoRow();
            row.id = id;
            row.parentId = parentId;
            row.tipoCodigo = tipoCodigo;
            row.tipoNombre = tipoNombre;
            row.fechaDocumento = LocalDate.now();
            return row;
        }

        DocumentoAnalizadoDTO toDocumento(Long idExpediente) {
            return new DocumentoAnalizadoDTO(
                    id,
                    idExpediente,
                    null,
                    tipoCodigo,
                    tipoNombre,
                    estadoCodigo,
                    estadoNombre,
                    fechaDocumento,
                    numeroDocumento == null ? "" : numeroDocumento,
                    descripcion == null ? "" : descripcion,
                    false,
                    null,
                    requiereRespuesta,
                    confirmacionRespuesta,
                    fechaRespuesta,
                    hojaEnvio,
                    false,
                    fechaPublicacion,
                    "",
                    parentId,
                    1,
                    0,
                    "",
                    true,
                    "",
                    null,
                    "",
                    null,
                    existeOposicion);
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
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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
