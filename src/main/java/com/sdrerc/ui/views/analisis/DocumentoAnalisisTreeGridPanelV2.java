package com.sdrerc.ui.views.analisis;

import com.sdrerc.domain.dto.sdrercapp.AnalisisResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
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
import java.time.LocalDateTime;
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
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class DocumentoAnalisisTreeGridPanelV2 extends JPanel {

    public interface SaveRowHandler {
        AnalisisResultadoDTO guardarFila(DocumentoAnalizadoDTO documento) throws Exception;
    }

    public interface DeleteRowHandler {
        AnalisisResultadoDTO eliminarFila(Long idExpediente, List<Long> idsDocumentoAnalizado) throws Exception;
    }

    public interface DownloadPlantillaHandler {
        void descargar(DocumentoAnalizadoDTO documento);
    }

    private static final int PADRE_COL_TIPO = 0;
    private static final int PADRE_COL_NUMERO = 1;
    private static final int PADRE_COL_ESTADO_DOCUMENTO = 2;
    private static final int PADRE_COL_FECHA = 3;
    private static final int PADRE_COL_DESCRIPCION = 4;
    private static final int PADRE_COL_REQUIERE_RESPUESTA = 5;
    private static final int PADRE_COL_WORD = 6;
    private static final int PADRE_COL_GUARDAR = 7;
    private static final int PADRE_COL_ELIMINAR = 8;

    private static final int HIJO_COL_TIPO = 0;
    private static final int HIJO_COL_COMENTARIO = 1;
    private static final int HIJO_COL_CONFIRMACION_RESPUESTA = 2;
    private static final int HIJO_COL_FECHA_RESPUESTA = 3;
    private static final int HIJO_COL_FECHA_PUBLICACION = 4;
    private static final int HIJO_COL_HOJA_ENVIO = 5;
    private static final int HIJO_COL_GUARDAR = 6;
    private static final int HIJO_COL_ELIMINAR = 7;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final List<DocumentoRow> allRows = new ArrayList<DocumentoRow>();
    private final PadreTableModel padreModel = new PadreTableModel();
    private final HijoTableModel hijoModel = new HijoTableModel();
    private final JTable tablaPadre = new AppV2Table(padreModel);
    private final JTable tablaHijo = new AppV2Table(hijoModel);
    private final JScrollPane scrollPadre = new JScrollPane(tablaPadre);
    private final JScrollPane scrollHijo = new JScrollPane(tablaHijo);
    private final JLabel lblBannerPadre = crearBanner("Documentos de análisis");
    private final JLabel lblBannerHijo = crearBanner("Documentos relacionados / respuesta");
    private final JButton btnAgregarPadre = new JButton("+ Documento");
    private final JButton btnAgregarHijo = new JButton("+ Relacionado");
    private final JLabel lblEstado = new JLabel("Registre documentos sin cerrar el resultado final del análisis.");
    private final AtomicLong tempIds = new AtomicLong(-1L);

    private List<CatalogoItemDTO> tipos = new ArrayList<CatalogoItemDTO>();
    private List<CatalogoItemDTO> estados = new ArrayList<CatalogoItemDTO>();
    private java.util.Set<String> tiposIntermedios = new java.util.HashSet<String>();
    private Long idExpediente;
    private Long idExpedienteAnalisis;
    private DocumentoRow padreSeleccionado;
    private SaveRowHandler saveRowHandler;
    private DeleteRowHandler deleteRowHandler;
    private DownloadPlantillaHandler downloadHandler;
    private Runnable refreshHandler;

    public DocumentoAnalisisTreeGridPanelV2() {
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        add(crearToolbar(), BorderLayout.NORTH);
        configurarTablas();
        add(crearGrillas(), BorderLayout.CENTER);
    }

    public void setHandlers(
            SaveRowHandler saveRowHandler,
            DeleteRowHandler deleteRowHandler,
            DownloadPlantillaHandler downloadHandler,
            Runnable refreshHandler) {
        this.saveRowHandler = saveRowHandler;
        this.deleteRowHandler = deleteRowHandler;
        this.downloadHandler = downloadHandler;
        this.refreshHandler = refreshHandler;
    }

    public void setCatalogos(List<CatalogoItemDTO> tipos, List<CatalogoItemDTO> estados) {
        this.tipos = tipos == null ? new ArrayList<CatalogoItemDTO>() : new ArrayList<CatalogoItemDTO>(tipos);
        this.estados = estados == null ? new ArrayList<CatalogoItemDTO>() : new ArrayList<CatalogoItemDTO>(estados);
        tablaPadre.getColumnModel().getColumn(PADRE_COL_TIPO).setCellEditor(new DefaultCellEditor(comboCatalogo(this.tipos)));
        tablaPadre.getColumnModel().getColumn(PADRE_COL_ESTADO_DOCUMENTO).setCellEditor(new DefaultCellEditor(comboCatalogo(this.estados)));
        tablaHijo.getColumnModel().getColumn(HIJO_COL_TIPO).setCellEditor(new DefaultCellEditor(comboCatalogo(this.tipos)));
    }

    public void setTiposIntermedios(java.util.Set<String> tiposIntermedios) {
        this.tiposIntermedios = tiposIntermedios == null ? new java.util.HashSet<String>() : tiposIntermedios;
        padreModel.tiposIntermedios = this.tiposIntermedios;
    }

    public void setDocumentos(Long idExpediente, Long idExpedienteAnalisis, List<DocumentoAnalizadoDTO> documentos) {
        this.idExpediente = idExpediente;
        this.idExpedienteAnalisis = idExpedienteAnalisis;
        allRows.clear();
        if (documentos != null) {
            for (DocumentoAnalizadoDTO documento : documentos) {
                if (documento != null && documento.isActivo()) {
                    allRows.add(DocumentoRow.from(documento));
                }
            }
        }
        padreSeleccionado = null;
        rebuildPadre();
        rebuildHijo(null);
        actualizarEstado();
    }

    public List<DocumentoAnalizadoDTO> getDocumentosActivos() {
        List<DocumentoAnalizadoDTO> documentos = new ArrayList<DocumentoAnalizadoDTO>();
        for (DocumentoRow row : allRows) {
            documentos.add(row.toDocumento(idExpediente, idExpedienteAnalisis));
        }
        return documentos;
    }

    private JPanel crearToolbar() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 6));
        wrapper.setOpaque(false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        estilizarPrimario(btnAgregarPadre);
        estilizarSecundario(btnAgregarHijo);
        actions.add(btnAgregarPadre);
        actions.add(btnAgregarHijo);
        lblEstado.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);
        wrapper.add(actions, BorderLayout.NORTH);
        wrapper.add(lblEstado, BorderLayout.SOUTH);
        btnAgregarPadre.addActionListener(e -> agregarPadre());
        btnAgregarHijo.addActionListener(e -> agregarHijo());
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

        tablaPadre.getColumnModel().getColumn(PADRE_COL_DESCRIPCION).setCellEditor(new TextAreaCellEditor());
        tablaPadre.getColumnModel().getColumn(PADRE_COL_FECHA).setCellEditor(new FechaCellEditor());
        tablaHijo.getColumnModel().getColumn(HIJO_COL_COMENTARIO).setCellEditor(new TextAreaCellEditor());
        tablaHijo.getColumnModel().getColumn(HIJO_COL_CONFIRMACION_RESPUESTA)
                .setCellEditor(new DefaultCellEditor(comboConfirmacionRespuesta()));
        tablaHijo.getColumnModel().getColumn(HIJO_COL_FECHA_RESPUESTA).setCellEditor(new FechaCellEditor());

        tablaPadre.getColumnModel().getColumn(PADRE_COL_WORD).setCellRenderer(
                new RowActionRenderer(new WordDocumentIcon(), "Descargar plantilla Word"));
        tablaPadre.getColumnModel().getColumn(PADRE_COL_WORD).setCellEditor(
                new RowActionEditor(new WordDocumentIcon(), "Descargar plantilla Word",
                        row -> descargarPlantilla(padreModel.getRow(row))));
        tablaPadre.getColumnModel().getColumn(PADRE_COL_GUARDAR).setCellRenderer(
                new RowActionRenderer(new SaveDocumentIcon(), "Guardar documento"));
        tablaPadre.getColumnModel().getColumn(PADRE_COL_GUARDAR).setCellEditor(
                new RowActionEditor(new SaveDocumentIcon(), "Guardar documento",
                        row -> guardarFila(padreModel.getRow(row))));
        tablaPadre.getColumnModel().getColumn(PADRE_COL_ELIMINAR).setCellRenderer(
                new RowActionRenderer(new DeleteDocumentIcon(), "Eliminar documento"));
        tablaPadre.getColumnModel().getColumn(PADRE_COL_ELIMINAR).setCellEditor(
                new RowActionEditor(new DeleteDocumentIcon(), "Eliminar documento",
                        row -> eliminarFila(padreModel.getRow(row))));

        tablaHijo.getColumnModel().getColumn(HIJO_COL_GUARDAR).setCellRenderer(
                new RowActionRenderer(new SaveDocumentIcon(), "Guardar documento"));
        tablaHijo.getColumnModel().getColumn(HIJO_COL_GUARDAR).setCellEditor(
                new RowActionEditor(new SaveDocumentIcon(), "Guardar documento",
                        row -> guardarFila(hijoModel.getRow(row))));
        tablaHijo.getColumnModel().getColumn(HIJO_COL_ELIMINAR).setCellRenderer(
                new RowActionRenderer(new DeleteDocumentIcon(), "Eliminar documento"));
        tablaHijo.getColumnModel().getColumn(HIJO_COL_ELIMINAR).setCellEditor(
                new RowActionEditor(new DeleteDocumentIcon(), "Eliminar documento",
                        row -> eliminarFila(hijoModel.getRow(row))));

        ajustarAnchos(tablaPadre, new int[]{200, 130, 150, 110, 240, 140});
        configurarColumnasAccion(tablaPadre, new int[]{PADRE_COL_WORD, PADRE_COL_GUARDAR, PADRE_COL_ELIMINAR});
        ajustarAnchos(tablaHijo, new int[]{170, 210, 150, 110, 120, 120});
        configurarColumnasAccion(tablaHijo, new int[]{HIJO_COL_GUARDAR, HIJO_COL_ELIMINAR});

        scrollPadre.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scrollPadre.setPreferredSize(new Dimension(820, 150));
        scrollHijo.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        scrollHijo.setPreferredSize(new Dimension(820, 130));
        configurarScrollAnidado(scrollPadre);
        configurarScrollAnidado(scrollHijo);
        AppV2TableScrollDiagnostics.log("AnalisisDocumentosPadre", tablaPadre, scrollPadre);
        AppV2TableScrollDiagnostics.log("AnalisisDocumentosHijo", tablaHijo, scrollHijo);

        tablaPadre.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int row = tablaPadre.getSelectedRow();
            padreSeleccionado = row < 0 ? null : padreModel.getRow(row);
            rebuildHijo(padreSeleccionado == null ? null : padreSeleccionado.id);
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

    private void agregarPadre() {
        DocumentoRow row = DocumentoRow.nuevoPadre(tempIds.getAndDecrement(), primerTipo(), primerEstado(), siguienteOrden(null));
        allRows.add(row);
        rebuildPadre();
        seleccionarFilaPadre(row);
        actualizarEstado();
    }

    private void agregarHijo() {
        DocumentoRow seleccionado = padreSeleccionado;
        if (seleccionado == null) {
            mostrarInfo("Seleccione un documento principal para agregar un documento relacionado.");
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
        DocumentoRow hijo = DocumentoRow.nuevoHijo(
                tempIds.getAndDecrement(),
                seleccionado.id,
                primerTipo(),
                primerEstado(),
                siguienteOrden(seleccionado.id));
        allRows.add(hijo);
        rebuildHijo(seleccionado.id);
        seleccionarFilaHijo(hijo);
        actualizarEstado();
    }

    private void guardarFila(DocumentoRow row) {
        if (row == null) {
            return;
        }
        if (saveRowHandler == null) {
            mostrarInfo("No se configuró el servicio de guardado de documentos.");
            return;
        }
        DocumentoAnalizadoDTO documento = row.toDocumento(idExpediente, idExpedienteAnalisis);
        lblEstado.setText("Guardando documento...");
        SwingWorker<AnalisisResultadoDTO, Void> worker = new SwingWorker<AnalisisResultadoDTO, Void>() {
            @Override
            protected AnalisisResultadoDTO doInBackground() throws Exception {
                return saveRowHandler.guardarFila(documento);
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
                    mostrarError("No se pudo guardar el documento.", ex);
                } finally {
                    actualizarEstado();
                }
            }
        };
        worker.execute();
    }

    private void eliminarFila(DocumentoRow row) {
        if (row == null) {
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "El documento será dado de baja lógicamente. No se eliminará físicamente. ¿Desea continuar?",
                "Eliminar documento",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        if (row.id == null || row.id.longValue() < 0L) {
            allRows.remove(row);
            if (row.nivel == 0) {
                allRows.removeIf(candidato -> row.id != null && row.id.equals(candidato.parentId));
                if (row.equals(padreSeleccionado)) {
                    padreSeleccionado = null;
                }
            }
            rebuildPadre();
            rebuildHijo(padreSeleccionado == null ? null : padreSeleccionado.id);
            actualizarEstado();
            return;
        }
        if (deleteRowHandler == null) {
            mostrarInfo("No se configuró el servicio de baja de documentos.");
            return;
        }
        final List<Long> ids = new ArrayList<Long>();
        ids.add(row.id);
        if (row.nivel == 0) {
            for (DocumentoRow child : allRows) {
                if (row.id.equals(child.parentId) && child.activo
                        && child.id != null && child.id.longValue() >= 0L) {
                    ids.add(child.id);
                }
            }
        }
        lblEstado.setText("Eliminando documento...");
        SwingWorker<AnalisisResultadoDTO, Void> worker = new SwingWorker<AnalisisResultadoDTO, Void>() {
            @Override
            protected AnalisisResultadoDTO doInBackground() throws Exception {
                return deleteRowHandler.eliminarFila(idExpediente, ids);
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
                    mostrarError("No se pudo eliminar el documento.", ex);
                } finally {
                    actualizarEstado();
                }
            }
        };
        worker.execute();
    }

    private void descargarPlantilla(DocumentoRow row) {
        if (row == null) {
            return;
        }
        if (downloadHandler == null) {
            mostrarInfo("No se configuró el servicio de descarga de plantillas.");
            return;
        }
        downloadHandler.descargar(row.toDocumento(idExpediente, idExpedienteAnalisis));
    }

    private void refrescar() {
        if (refreshHandler != null) {
            refreshHandler.run();
        }
    }

    private void rebuildPadre() {
        List<DocumentoRow> visibles = new ArrayList<DocumentoRow>();
        for (DocumentoRow row : allRows) {
            if (row.activo && row.nivel == 0) {
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
                if (row.activo && row.nivel == 1 && idPadre.equals(row.parentId)) {
                    visibles.add(row);
                }
            }
            visibles.sort(Comparator.comparingInt(r -> r.orden));
        }
        hijoModel.setRows(visibles);
    }

    private void seleccionarFilaPadre(DocumentoRow row) {
        int index = padreModel.indexOf(row);
        if (index >= 0) {
            tablaPadre.getSelectionModel().setSelectionInterval(index, index);
            tablaPadre.scrollRectToVisible(tablaPadre.getCellRect(index, 0, true));
        }
    }

    private void seleccionarFilaHijo(DocumentoRow row) {
        int index = hijoModel.indexOf(row);
        if (index >= 0) {
            tablaHijo.getSelectionModel().setSelectionInterval(index, index);
            tablaHijo.scrollRectToVisible(tablaHijo.getCellRect(index, 0, true));
        }
    }

    private CatalogoItemDTO primerTipo() {
        return tipos.isEmpty() ? new CatalogoItemDTO("", "") : tipos.get(0);
    }

    private CatalogoItemDTO primerEstado() {
        for (CatalogoItemDTO estado : estados) {
            if (estado != null && "EN_PROYECTO".equalsIgnoreCase(estado.getCodigo())) {
                return estado;
            }
        }
        return estados.isEmpty() ? new CatalogoItemDTO("", "") : estados.get(0);
    }

    private int siguienteOrden(Long parentId) {
        int max = 0;
        for (DocumentoRow row : allRows) {
            if (same(parentId, row.parentId) && row.activo) {
                max = Math.max(max, row.orden);
            }
        }
        return max + 1;
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

    private JComboBox<String> comboConfirmacionRespuesta() {
        JComboBox<String> combo = new JComboBox<String>(new String[]{"Pendiente", "Si", "No"});
        combo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        return combo;
    }

    private void actualizarEstado() {
        int count = 0;
        for (DocumentoRow row : allRows) {
            if (row.activo) {
                count++;
            }
        }
        lblEstado.setText(count + " documento(s) activos. Guardar documentos no mueve el expediente de etapa.");
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

    private static class PadreTableModel extends AbstractTableModel {
        private final List<DocumentoRow> rows = new ArrayList<DocumentoRow>();
        private final String[] columns = new String[]{
            "Tipo documento", "Número Documento", "Estado documento", "Fecha Emisión",
            "Comentario", "¿Requiere respuesta?", "", "", ""
        };
        private java.util.Set<String> tiposIntermedios = new java.util.HashSet<String>();

        private boolean esTipoIntermedio(CatalogoItemDTO tipo) {
            return tipo != null && tipo.getCodigo() != null && tiposIntermedios.contains(tipo.getCodigo());
        }

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
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == PADRE_COL_REQUIERE_RESPUESTA ? Boolean.class : Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return getRow(rowIndex) != null;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DocumentoRow row = getRow(rowIndex);
            if (row == null) {
                return "";
            }
            switch (columnIndex) {
                case PADRE_COL_TIPO:
                    return row.tipo;
                case PADRE_COL_NUMERO:
                    return row.numeroDocumento;
                case PADRE_COL_ESTADO_DOCUMENTO:
                    return row.estadoDocumento;
                case PADRE_COL_FECHA:
                    return row.fechaDocumento != null ? DATE_FORMAT.format(row.fechaDocumento) : "";
                case PADRE_COL_DESCRIPCION:
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
                case PADRE_COL_TIPO:
                    if (value instanceof CatalogoItemDTO) {
                        row.tipo = (CatalogoItemDTO) value;
                        if (esTipoIntermedio(row.tipo)) {
                            row.requiereRespuesta = true;
                            row.estadoRespuesta = "PENDIENTE";
                        }
                    }
                    break;
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
                case PADRE_COL_DESCRIPCION:
                    row.descripcion = text(value);
                    break;
                case PADRE_COL_REQUIERE_RESPUESTA:
                    row.requiereRespuesta = Boolean.TRUE.equals(value);
                    row.estadoRespuesta = row.requiereRespuesta ? "PENDIENTE" : "";
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
            "Tipo documento", "Comentario", "Confirmación de respuesta", "Fecha Respuesta",
            "Fecha Publicación", "Hoja de Envío", "", ""
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
            return getRow(rowIndex) != null && columnIndex != HIJO_COL_FECHA_PUBLICACION;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DocumentoRow row = getRow(rowIndex);
            if (row == null) {
                return "";
            }
            switch (columnIndex) {
                case HIJO_COL_TIPO:
                    return row.tipo;
                case HIJO_COL_COMENTARIO:
                    return row.descripcion;
                case HIJO_COL_CONFIRMACION_RESPUESTA:
                    return row.confirmacionRespuesta;
                case HIJO_COL_FECHA_RESPUESTA:
                    return row.fechaRespuesta != null ? DATE_FORMAT.format(row.fechaRespuesta) : "";
                case HIJO_COL_FECHA_PUBLICACION:
                    return row.fechaPublicacion != null ? DATE_FORMAT.format(row.fechaPublicacion) : "";
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
                case HIJO_COL_TIPO:
                    if (value instanceof CatalogoItemDTO) {
                        row.tipo = (CatalogoItemDTO) value;
                    }
                    break;
                case HIJO_COL_COMENTARIO:
                    row.descripcion = text(value);
                    break;
                case HIJO_COL_CONFIRMACION_RESPUESTA:
                    row.confirmacionRespuesta = text(value);
                    break;
                case HIJO_COL_FECHA_RESPUESTA:
                    row.fechaRespuesta = parseDate(value);
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
        private String confirmacionRespuesta;
        private LocalDate fechaRespuesta;
        private LocalDate fechaPublicacion;
        private String hojaEnvio;
        private boolean activo = true;
        private String usuarioRegistro;
        private LocalDateTime fechaRegistro;
        private String usuarioModificacion;
        private LocalDateTime fechaModificacion;
        private Boolean existeOposicion;

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
            row.confirmacionRespuesta = dto.getConfirmacionRespuesta();
            row.fechaRespuesta = dto.getFechaRespuesta();
            row.fechaPublicacion = dto.getFechaPublicacion();
            row.hojaEnvio = dto.getNumeroHojaEnvioRespuesta();
            row.activo = dto.isActivo();
            row.usuarioRegistro = dto.getUsuarioRegistro();
            row.fechaRegistro = dto.getFechaRegistro();
            row.usuarioModificacion = dto.getUsuarioModificacion();
            row.fechaModificacion = dto.getFechaModificacion();
            row.existeOposicion = dto.getExisteOposicion();
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
                    confirmacionRespuesta,
                    fechaRespuesta,
                    hojaEnvio,
                    false,
                    fechaPublicacion,
                    observacion,
                    parentId,
                    nivel,
                    orden,
                    estadoRespuesta,
                    activo,
                    usuarioRegistro,
                    fechaRegistro,
                    usuarioModificacion,
                    fechaModificacion,
                    existeOposicion);
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
            row.confirmacionRespuesta = "";
            row.hojaEnvio = "";
            row.usuarioRegistro = "";
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

    private static class TextAreaCellEditor extends AbstractCellEditor implements TableCellEditor {
        private static final int ALTURA_EXPANDIDA = 90;
        private final JTextArea area = new JTextArea();
        private final JScrollPane scroll;
        private JTable tablaActual;
        private int filaActual = -1;
        private int alturaOriginal;

        TextAreaCellEditor() {
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            area.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            scroll = new JScrollPane(area);
            scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.PRIMARY));
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
            tablaActual = table;
            filaActual = row;
            alturaOriginal = table.getRowHeight(row);
            if (alturaOriginal < ALTURA_EXPANDIDA) {
                table.setRowHeight(row, ALTURA_EXPANDIDA);
            }
            return scroll;
        }

        @Override
        public boolean stopCellEditing() {
            restaurarAltura();
            return super.stopCellEditing();
        }

        @Override
        public void cancelCellEditing() {
            restaurarAltura();
            super.cancelCellEditing();
        }

        private void restaurarAltura() {
            if (tablaActual != null && filaActual >= 0) {
                tablaActual.setRowHeight(filaActual, alturaOriginal);
            }
            tablaActual = null;
            filaActual = -1;
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

    private static class WordDocumentIcon implements Icon {
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
                Color fill = new Color(232, 244, 252);
                g2.setColor(fill);
                g2.fillRoundRect(x + 3, y + 1, 12, 16, 3, 3);
                g2.setColor(stroke);
                g2.drawRoundRect(x + 3, y + 1, 12, 16, 3, 3);
                g2.drawLine(x + 6, y + 6, x + 12, y + 6);
                g2.drawLine(x + 6, y + 9, x + 12, y + 9);
                g2.drawLine(x + 6, y + 12, x + 10, y + 12);
                g2.setColor(new Color(29, 92, 151));
                g2.fillRoundRect(x + 1, y + 5, 8, 8, 2, 2);
                g2.setColor(Color.WHITE);
                g2.drawLine(x + 3, y + 7, x + 4, y + 11);
                g2.drawLine(x + 4, y + 11, x + 5, y + 8);
                g2.drawLine(x + 5, y + 8, x + 6, y + 11);
                g2.drawLine(x + 6, y + 11, x + 7, y + 7);
            } finally {
                g2.dispose();
            }
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

    private static class DeleteDocumentIcon implements Icon {
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
                Color stroke = new Color(196, 53, 53);
                Color fill = new Color(253, 237, 237);
                g2.setColor(fill);
                g2.fillRoundRect(x + 2, y + 2, 14, 14, 7, 7);
                g2.setColor(stroke);
                g2.drawRoundRect(x + 2, y + 2, 14, 14, 7, 7);
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawLine(x + 6, y + 6, x + 12, y + 12);
                g2.drawLine(x + 12, y + 6, x + 6, y + 12);
            } finally {
                g2.dispose();
            }
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

    private static LocalDate fechaSeleccionada(PremiumDateFieldV2 field) {
        if (field == null || field.getDate() == null) {
            return null;
        }
        return field.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
