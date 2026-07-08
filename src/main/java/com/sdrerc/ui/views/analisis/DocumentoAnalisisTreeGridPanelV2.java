package com.sdrerc.ui.views.analisis;

import com.sdrerc.domain.dto.sdrercapp.AnalisisResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

public class DocumentoAnalisisTreeGridPanelV2 extends JPanel {

    public interface SaveHandler {
        AnalisisResultadoDTO guardar(List<DocumentoAnalizadoDTO> documentos) throws Exception;
    }

    private static final int COL_EXPANDIR = 0;
    private static final int COL_TIPO = 1;
    private static final int COL_NUMERO = 2;
    private static final int COL_FECHA = 3;
    private static final int COL_DESCRIPCION = 4;
    private static final int COL_REQUIERE_RESPUESTA = 5;
    private static final int COL_ESTADO_RESPUESTA = 6;
    private static final int COL_ESTADO_DOCUMENTO = 7;
    private static final int COL_OBSERVACION = 8;
    private static final int COL_USUARIO_REGISTRO = 9;
    private static final int COL_FECHA_REGISTRO = 10;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DocumentoTreeTableModel model = new DocumentoTreeTableModel();
    private final JTable table = new AppV2Table(model);
    private final JScrollPane scrollPane = new JScrollPane(table);
    private final JButton btnAgregarPadre = new JButton("+ Documento");
    private final JButton btnAgregarHijo = new JButton("+ Relacionado");
    private final JButton btnGuardar = new JButton("Guardar cambios");
    private final JButton btnCancelar = new JButton("Cancelar cambios");
    private final JButton btnBaja = new JButton("Dar de baja");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JLabel lblEstado = new JLabel("Registre documentos sin cerrar el resultado final del análisis.");
    private final AtomicLong tempIds = new AtomicLong(-1L);

    private List<CatalogoItemDTO> tipos = new ArrayList<CatalogoItemDTO>();
    private List<CatalogoItemDTO> estados = new ArrayList<CatalogoItemDTO>();
    private Long idExpediente;
    private Long idExpedienteAnalisis;
    private SaveHandler saveHandler;
    private Runnable refreshHandler;

    public DocumentoAnalisisTreeGridPanelV2() {
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        add(crearToolbar(), BorderLayout.NORTH);
        configurarTabla();
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setHandlers(SaveHandler saveHandler, Runnable refreshHandler) {
        this.saveHandler = saveHandler;
        this.refreshHandler = refreshHandler;
    }

    public void setCatalogos(List<CatalogoItemDTO> tipos, List<CatalogoItemDTO> estados) {
        this.tipos = tipos == null ? new ArrayList<CatalogoItemDTO>() : new ArrayList<CatalogoItemDTO>(tipos);
        this.estados = estados == null ? new ArrayList<CatalogoItemDTO>() : new ArrayList<CatalogoItemDTO>(estados);
        table.getColumnModel().getColumn(COL_TIPO).setCellEditor(new DefaultCellEditor(comboCatalogo(this.tipos)));
        table.getColumnModel().getColumn(COL_ESTADO_DOCUMENTO).setCellEditor(new DefaultCellEditor(comboCatalogo(this.estados)));
    }

    public void setDocumentos(Long idExpediente, Long idExpedienteAnalisis, List<DocumentoAnalizadoDTO> documentos) {
        this.idExpediente = idExpediente;
        this.idExpedienteAnalisis = idExpedienteAnalisis;
        model.setDocumentos(documentos);
        actualizarEstado();
    }

    public List<DocumentoAnalizadoDTO> getDocumentosActivos() {
        return model.toDocumentos(idExpediente, idExpedienteAnalisis);
    }

    private JPanel crearToolbar() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 6));
        wrapper.setOpaque(false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        estilizarPrimario(btnAgregarPadre);
        estilizarSecundario(btnAgregarHijo);
        estilizarPrimario(btnGuardar);
        estilizarSecundario(btnCancelar);
        estilizarSecundario(btnBaja);
        estilizarSecundario(btnRefrescar);
        actions.add(btnAgregarPadre);
        actions.add(btnAgregarHijo);
        actions.add(btnGuardar);
        actions.add(btnCancelar);
        actions.add(btnBaja);
        actions.add(btnRefrescar);
        lblEstado.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);
        wrapper.add(actions, BorderLayout.NORTH);
        wrapper.add(lblEstado, BorderLayout.SOUTH);
        btnAgregarPadre.addActionListener(e -> agregarPadre());
        btnAgregarHijo.addActionListener(e -> agregarHijo());
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> refrescar());
        btnBaja.addActionListener(e -> darBaja());
        btnRefrescar.addActionListener(e -> refrescar());
        return wrapper;
    }

    private void configurarTabla() {
        table.setRowHeight(34);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        table.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        table.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        table.setGridColor(AppV2Theme.BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setDefaultRenderer(Object.class, new DocumentoTreeRenderer());
        table.setDefaultRenderer(Boolean.class, new DocumentoTreeBooleanRenderer());
        table.getColumnModel().getColumn(COL_DESCRIPCION).setCellEditor(new TextAreaCellEditor());
        table.getColumnModel().getColumn(COL_OBSERVACION).setCellEditor(new TextAreaCellEditor());
        int[] widths = new int[]{54, 210, 130, 110, 260, 145, 135, 150, 220, 130, 140};
        for (int i = 0; i < widths.length; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(widths[i]);
            column.setMinWidth(i == COL_EXPANDIR ? 50 : Math.min(widths[i], 95));
        }
        scrollPane.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && table.convertColumnIndexToModel(col) == COL_EXPANDIR) {
                    model.toggleExpanded(table.convertRowIndexToModel(row));
                }
            }
        });
    }

    private void agregarPadre() {
        DocumentoRow row = DocumentoRow.nuevoPadre(tempIds.getAndDecrement(), primerTipo(), primerEstado(), siguienteOrdenPadre());
        model.addRow(row);
        seleccionarUltimaFila();
    }

    private void agregarHijo() {
        DocumentoRow seleccionado = filaSeleccionada();
        if (seleccionado == null) {
            mostrarInfo("Seleccione un documento principal para agregar un documento relacionado.");
            return;
        }
        if (seleccionado.nivel > 0) {
            mostrarInfo("Solo se permite agregar documentos hijos a un documento principal.");
            return;
        }
        if (!seleccionado.requiereRespuesta) {
            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "El documento seleccionado no está marcado como requiere respuesta. ¿Desea continuar como documento relacionado?",
                    "Documento relacionado",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }
        seleccionado.expanded = true;
        DocumentoRow hijo = DocumentoRow.nuevoHijo(
                tempIds.getAndDecrement(),
                seleccionado.id,
                primerTipo(),
                primerEstado(),
                siguienteOrdenHijo(seleccionado.id));
        model.addRow(hijo);
        seleccionarUltimaFila();
    }

    private void guardar() {
        if (saveHandler == null) {
            mostrarInfo("No se configuró el servicio de guardado de documentos.");
            return;
        }
        List<DocumentoAnalizadoDTO> documentos = getDocumentosActivos();
        if (documentos.isEmpty()) {
            mostrarInfo("Agregue al menos un documento de análisis.");
            return;
        }
        btnGuardar.setEnabled(false);
        lblEstado.setText("Guardando documentos de análisis...");
        SwingWorker<AnalisisResultadoDTO, Void> worker = new SwingWorker<AnalisisResultadoDTO, Void>() {
            @Override
            protected AnalisisResultadoDTO doInBackground() throws Exception {
                return saveHandler.guardar(documentos);
            }

            @Override
            protected void done() {
                try {
                    AnalisisResultadoDTO resultado = get();
                    JOptionPane.showMessageDialog(
                            DocumentoAnalisisTreeGridPanelV2.this,
                            resultado.getMensaje(),
                            "Documentos de análisis",
                            JOptionPane.INFORMATION_MESSAGE);
                    refrescar();
                } catch (Exception ex) {
                    mostrarError("No se pudieron guardar los documentos de análisis.", ex);
                } finally {
                    btnGuardar.setEnabled(true);
                    actualizarEstado();
                }
            }
        };
        worker.execute();
    }

    private void darBaja() {
        DocumentoRow seleccionado = filaSeleccionada();
        if (seleccionado == null) {
            mostrarInfo("Seleccione un documento para darlo de baja.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "El documento será dado de baja lógicamente. No se eliminará físicamente. ¿Desea continuar?",
                "Dar de baja documento",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        model.darBaja(seleccionado);
        actualizarEstado();
    }

    private void refrescar() {
        if (refreshHandler != null) {
            refreshHandler.run();
        }
    }

    private void seleccionarUltimaFila() {
        int last = table.getRowCount() - 1;
        if (last >= 0) {
            table.getSelectionModel().setSelectionInterval(last, last);
            table.scrollRectToVisible(table.getCellRect(last, 0, true));
        }
        actualizarEstado();
    }

    private DocumentoRow filaSeleccionada() {
        int row = table.getSelectedRow();
        return row < 0 ? null : model.getVisibleRow(table.convertRowIndexToModel(row));
    }

    private CatalogoItemDTO primerTipo() {
        return tipos.isEmpty() ? new CatalogoItemDTO("", "") : tipos.get(0);
    }

    private CatalogoItemDTO primerEstado() {
        return estados.isEmpty() ? new CatalogoItemDTO("", "") : estados.get(0);
    }

    private int siguienteOrdenPadre() {
        return model.siguienteOrden(null);
    }

    private int siguienteOrdenHijo(Long idPadre) {
        return model.siguienteOrden(idPadre);
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

    private void actualizarEstado() {
        lblEstado.setText(model.getActivosCount() + " documento(s) activos. Guardar documentos no mueve el expediente de etapa.");
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
        JOptionPane.showMessageDialog(this, message, "Documentos de análisis", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String message, Exception ex) {
        String detail = ex == null || ex.getMessage() == null ? "" : "\n\n" + ex.getMessage();
        JOptionPane.showMessageDialog(this, message + detail, "Documentos de análisis", JOptionPane.ERROR_MESSAGE);
    }

    private static class DocumentoTreeTableModel extends AbstractTableModel {
        private final List<DocumentoRow> allRows = new ArrayList<DocumentoRow>();
        private final List<DocumentoRow> visibleRows = new ArrayList<DocumentoRow>();
        private final String[] columns = new String[]{
            "", "Tipo documento", "Número documento", "Fecha documento", "Descripción",
            "¿Requiere respuesta?", "Estado respuesta", "Estado documento", "Observación",
            "Usuario registro", "Fecha registro"
        };

        void setDocumentos(List<DocumentoAnalizadoDTO> documentos) {
            allRows.clear();
            if (documentos != null) {
                for (DocumentoAnalizadoDTO documento : documentos) {
                    if (documento != null && documento.isActivo()) {
                        allRows.add(DocumentoRow.from(documento));
                    }
                }
            }
            cerrarGruposSinHijos();
            rebuildVisible();
        }

        void addRow(DocumentoRow row) {
            allRows.add(row);
            rebuildVisible();
        }

        DocumentoRow getVisibleRow(int row) {
            return row < 0 || row >= visibleRows.size() ? null : visibleRows.get(row);
        }

        void toggleExpanded(int row) {
            DocumentoRow item = getVisibleRow(row);
            if (item == null || item.nivel != 0 || !tieneHijos(item.id)) {
                return;
            }
            item.expanded = !item.expanded;
            rebuildVisible();
        }

        void darBaja(DocumentoRow row) {
            if (row == null) {
                return;
            }
            row.activo = false;
            if (row.nivel == 0) {
                for (DocumentoRow child : allRows) {
                    if (row.id.equals(child.parentId)) {
                        child.activo = false;
                    }
                }
            }
            rebuildVisible();
        }

        int siguienteOrden(Long parentId) {
            int max = 0;
            for (DocumentoRow row : allRows) {
                if (same(parentId, row.parentId) && row.activo) {
                    max = Math.max(max, row.orden);
                }
            }
            return max + 1;
        }

        int getActivosCount() {
            int count = 0;
            for (DocumentoRow row : allRows) {
                if (row.activo) {
                    count++;
                }
            }
            return count;
        }

        List<DocumentoAnalizadoDTO> toDocumentos(Long idExpediente, Long idExpedienteAnalisis) {
            List<DocumentoAnalizadoDTO> documentos = new ArrayList<DocumentoAnalizadoDTO>();
            for (DocumentoRow row : allRows) {
                documentos.add(row.toDocumento(idExpediente, idExpedienteAnalisis));
            }
            return documentos;
        }

        private void rebuildVisible() {
            visibleRows.clear();
            Set<Long> parents = new HashSet<Long>();
            for (DocumentoRow row : allRows) {
                if (row.activo && row.nivel == 0) {
                    parents.add(row.id);
                    visibleRows.add(row);
                    if (row.expanded) {
                        for (DocumentoRow child : allRows) {
                            if (child.activo && child.nivel == 1 && row.id.equals(child.parentId)) {
                                visibleRows.add(child);
                            }
                        }
                    }
                }
            }
            for (DocumentoRow row : allRows) {
                if (row.activo && row.nivel == 1 && !parents.contains(row.parentId)) {
                    visibleRows.add(row);
                }
            }
            fireTableDataChanged();
        }

        private void cerrarGruposSinHijos() {
            for (DocumentoRow row : allRows) {
                row.expanded = row.nivel == 0 && tieneHijos(row.id);
            }
        }

        private boolean tieneHijos(Long idPadre) {
            for (DocumentoRow row : allRows) {
                if (row.activo && row.nivel == 1 && idPadre != null && idPadre.equals(row.parentId)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getRowCount() {
            return visibleRows.size();
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
            return columnIndex == COL_REQUIERE_RESPUESTA ? Boolean.class : Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == COL_TIPO
                    || columnIndex == COL_NUMERO
                    || columnIndex == COL_FECHA
                    || columnIndex == COL_DESCRIPCION
                    || columnIndex == COL_REQUIERE_RESPUESTA
                    || columnIndex == COL_OBSERVACION;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DocumentoRow row = getVisibleRow(rowIndex);
            if (row == null) {
                return "";
            }
            switch (columnIndex) {
                case COL_EXPANDIR:
                    return row.nivel == 0 ? (tieneHijos(row.id) ? (row.expanded ? "-" : "+") : "") : "";
                case COL_TIPO:
                    return row.tipo;
                case COL_NUMERO:
                    return row.numeroDocumento;
                case COL_FECHA:
                    return row.fechaDocumento == null ? "" : DATE_FORMAT.format(row.fechaDocumento);
                case COL_DESCRIPCION:
                    return row.descripcion;
                case COL_REQUIERE_RESPUESTA:
                    return row.requiereRespuesta;
                case COL_ESTADO_RESPUESTA:
                    return row.estadoRespuesta;
                case COL_ESTADO_DOCUMENTO:
                    return row.estadoDocumento;
                case COL_OBSERVACION:
                    return row.observacion;
                case COL_USUARIO_REGISTRO:
                    return row.usuarioRegistro;
                case COL_FECHA_REGISTRO:
                    return row.fechaRegistro == null ? "" : DATE_TIME_FORMAT.format(row.fechaRegistro);
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            DocumentoRow row = getVisibleRow(rowIndex);
            if (row == null) {
                return;
            }
            switch (columnIndex) {
                case COL_TIPO:
                    if (value instanceof CatalogoItemDTO) {
                        row.tipo = (CatalogoItemDTO) value;
                    }
                    break;
                case COL_NUMERO:
                    row.numeroDocumento = text(value);
                    break;
                case COL_FECHA:
                    row.fechaDocumento = parseDate(value);
                    break;
                case COL_DESCRIPCION:
                    row.descripcion = text(value);
                    break;
                case COL_REQUIERE_RESPUESTA:
                    row.requiereRespuesta = Boolean.TRUE.equals(value);
                    row.estadoRespuesta = row.requiereRespuesta ? "PENDIENTE" : "";
                    break;
                case COL_OBSERVACION:
                    row.observacion = text(value);
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
        private int nivel;
        private int orden;
        private CatalogoItemDTO tipo;
        private CatalogoItemDTO estadoDocumento;
        private LocalDate fechaDocumento;
        private String numeroDocumento;
        private String descripcion;
        private boolean requiereRespuesta;
        private String estadoRespuesta;
        private String observacion;
        private boolean activo = true;
        private boolean expanded = true;
        private String usuarioRegistro;
        private LocalDateTime fechaRegistro;
        private String usuarioModificacion;
        private LocalDateTime fechaModificacion;

        static DocumentoRow nuevoPadre(Long id, CatalogoItemDTO tipo, CatalogoItemDTO estado, int orden) {
            DocumentoRow row = base(id, tipo, estado, orden);
            row.nivel = 0;
            row.parentId = null;
            return row;
        }

        static DocumentoRow nuevoHijo(Long id, Long parentId, CatalogoItemDTO tipo, CatalogoItemDTO estado, int orden) {
            DocumentoRow row = base(id, tipo, estado, orden);
            row.nivel = 1;
            row.parentId = parentId;
            row.descripcion = "Respuesta asociada";
            return row;
        }

        static DocumentoRow from(DocumentoAnalizadoDTO dto) {
            DocumentoRow row = new DocumentoRow();
            row.id = dto.getIdDocumentoAnalizado();
            row.parentId = dto.getIdDocumentoPadre();
            row.nivel = dto.getNivel();
            row.orden = dto.getOrden();
            row.tipo = new CatalogoItemDTO(dto.getTipoDocumentoCodigo(), dto.getTipoDocumentoNombre());
            row.estadoDocumento = new CatalogoItemDTO(dto.getEstadoDocumentoCodigo(), dto.getEstadoDocumentoNombre());
            row.fechaDocumento = dto.getFechaDocumento();
            row.numeroDocumento = dto.getNumeroDocumento();
            row.descripcion = dto.getDescripcion();
            row.requiereRespuesta = dto.isRequiereRespuesta();
            row.estadoRespuesta = dto.getEstadoRespuesta().isEmpty()
                    ? (dto.isRequiereRespuesta() ? "PENDIENTE" : "")
                    : dto.getEstadoRespuesta();
            row.observacion = dto.getDetalleObservacion();
            row.activo = dto.isActivo();
            row.usuarioRegistro = dto.getUsuarioRegistro();
            row.fechaRegistro = dto.getFechaRegistro();
            row.usuarioModificacion = dto.getUsuarioModificacion();
            row.fechaModificacion = dto.getFechaModificacion();
            return row;
        }

        DocumentoAnalizadoDTO toDocumento(Long idExpediente, Long idExpedienteAnalisis) {
            return new DocumentoAnalizadoDTO(
                    id,
                    idExpediente,
                    idExpedienteAnalisis,
                    tipo == null ? "" : tipo.getCodigo(),
                    tipo == null ? "" : tipo.getNombre(),
                    estadoDocumento == null ? "" : estadoDocumento.getCodigo(),
                    estadoDocumento == null ? "" : estadoDocumento.getNombre(),
                    fechaDocumento,
                    numeroDocumento,
                    descripcion,
                    false,
                    null,
                    requiereRespuesta,
                    requiereRespuesta ? "PENDIENTE" : "",
                    null,
                    "",
                    false,
                    null,
                    observacion,
                    parentId,
                    nivel,
                    orden,
                    estadoRespuesta,
                    activo,
                    usuarioRegistro,
                    fechaRegistro,
                    usuarioModificacion,
                    fechaModificacion);
        }

        private static DocumentoRow base(Long id, CatalogoItemDTO tipo, CatalogoItemDTO estado, int orden) {
            DocumentoRow row = new DocumentoRow();
            row.id = id;
            row.orden = orden;
            row.tipo = tipo;
            row.estadoDocumento = estado;
            row.fechaDocumento = LocalDate.now();
            row.numeroDocumento = "";
            row.descripcion = "";
            row.estadoRespuesta = "";
            row.observacion = "";
            row.usuarioRegistro = "";
            return row;
        }
    }

    private class DocumentoTreeRenderer extends DefaultTableCellRenderer {
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
            DocumentoRow item = DocumentoAnalisisTreeGridPanelV2.this.model.getVisibleRow(table.convertRowIndexToModel(row));
            label.setBorder(BorderFactory.createEmptyBorder(0, item != null && item.nivel == 1 && column == COL_TIPO ? 22 : 8, 0, 8));
            label.setFont(item != null && item.nivel == 0
                    ? AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL)
                    : AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            label.setForeground(AppV2Theme.TEXT_PRIMARY);
            if (column == COL_EXPANDIR) {
                label.setHorizontalAlignment(CENTER);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            } else {
                label.setHorizontalAlignment(LEFT);
            }
            if (!isSelected && item != null) {
                label.setBackground(item.nivel == 0 ? new Color(236, 247, 252) : new Color(248, 252, 253));
            }
            return label;
        }
    }

    private class DocumentoTreeBooleanRenderer extends JCheckBox implements javax.swing.table.TableCellRenderer {
        DocumentoTreeBooleanRenderer() {
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
            DocumentoRow item = DocumentoAnalisisTreeGridPanelV2.this.model.getVisibleRow(table.convertRowIndexToModel(row));
            setBackground(isSelected ? table.getSelectionBackground()
                    : item != null && item.nivel == 0 ? new Color(236, 247, 252) : new Color(248, 252, 253));
            return this;
        }
    }

    private static class TextAreaCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JTextArea area = new JTextArea();

        TextAreaCellEditor() {
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            area.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        }

        @Override
        public Object getCellEditorValue() {
            return area.getText();
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
            area.setText(value == null ? "" : String.valueOf(value));
            return area;
        }
    }

    private static boolean same(Long a, Long b) {
        return a == null ? b == null : a.equals(b);
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
}
