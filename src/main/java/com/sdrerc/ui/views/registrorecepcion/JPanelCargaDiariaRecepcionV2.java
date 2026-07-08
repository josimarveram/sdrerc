package com.sdrerc.ui.views.registrorecepcion;

import com.sdrerc.application.sdrercapp.CargaDiariaArchivoParserService;
import com.sdrerc.application.sdrercapp.CargaDiariaPlantillaService;
import com.sdrerc.application.sdrercapp.CargaDiariaRegistroService;
import com.sdrerc.application.sdrercapp.CargaDiariaValidacionService;
import com.sdrerc.domain.rules.ProcedimientoRegistralRules;
import com.sdrerc.domain.dto.sdrercapp.CargaDiariaPreviewDTO;
import com.sdrerc.domain.dto.sdrercapp.CargaDiariaResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CargaDiariaResumenDTO;
import com.sdrerc.ui.appv2.components.AppV2ColumnFilterSupport;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class JPanelCargaDiariaRecepcionV2 extends JPanel {

    private enum FiltroKpi {
        TODOS,
        VALIDOS,
        ALERTAS,
        LISTOS,
        DUPLICADOS,
        GRUPO_FAMILIAR,
        PENDIENTES_NUMERO,
        REGISTRADOS
    }

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_INPUT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int COL_GRUPO_FAMILIAR = 16;
    private static final int COL_ESTADO_VALIDACION = 17;
    private static final int COL_POSIBLE_DUPLICADO = 18;
    private static final int COL_NUMERO_EXPEDIENTE_GENERADO = 19;
    private static final int COL_OBSERVACION_ARCHIVO = 20;
    private static final int COL_OBSERVACIONES_VALIDACION = 21;

    private final CargaDiariaArchivoParserService parserService = new CargaDiariaArchivoParserService();
    private final CargaDiariaPlantillaService plantillaService = new CargaDiariaPlantillaService();
    private final CargaDiariaValidacionService validacionService = new CargaDiariaValidacionService();
    private final CargaDiariaRegistroService registroService = new CargaDiariaRegistroService();
    private final Runnable onCargaConfirmada;

    private final JButton btnDescargarPlantilla = new JButton("Descargar plantilla");
    private final JButton btnArchivo = new JButton("Seleccionar archivo");
    private final JButton btnPrevisualizar = new JButton("Previsualizar");
    private final JButton btnValidar = new JButton("Validar");
    private final JButton btnConfirmar = new JButton("Confirmar carga");
    private final JButton btnExportar = new JButton("Exportar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JLabel lblArchivo = new JLabel("Sin archivo seleccionado");
    private final JLabel lblEstado = new JLabel("Seleccione un archivo .xlsx o .csv para iniciar.");
    private AppV2ColumnFilterSupport.Controller columnFilterSupport;

    private final ResumenCard cardValidos = new ResumenCard("Válidos", "0", "Pendiente de validación", AppV2Theme.SUCCESS);
    private final ResumenCard cardErrores = new ResumenCard("Con alertas", "0", "No bloqueantes", AppV2Theme.WARNING);
    private final ResumenCard cardDuplicados = new ResumenCard("Posibles duplicados", "0", "Acta + titular", AppV2Theme.WARNING);
    private final ResumenCard cardGrupoFamiliar = new ResumenCard("Grupo familiar", "0", "Marca o alerta", AppV2Theme.TEAL);
    private final ResumenCard cardPendientesNumero = new ResumenCard("Pendientes de número", "0", "Asignación decide", AppV2Theme.WARNING);
    private final ResumenCard cardRegistrados = new ResumenCard("Registrados", "0", "Pendiente", AppV2Theme.INDIGO);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{
                "TIPO DE SOLICITUD",
                "FECHA DE SOLICITUD",
                "SOLICITADO POR",
                "TIPO DOCUMENTO IDENTIDAD SOLICITANTE",
                "N° DOCUMENTO IDENTIDAD SOLICITANTE",
                "N° TRÁMITE WEB",
                "CANAL RECEPCIÓN",
                "N° EXPEDIENTE SGD",
                "TIPO DOCUMENTO",
                "N° DOCUMENTO",
                "PROCEDIMIENTO REGISTRAL",
                "TIPO DE ACTA",
                "N° ACTA",
                "TITULAR",
                "TIPO DOCUMENTO IDENTIDAD TITULAR",
                "N° DOCUMENTO IDENTIDAD TITULAR",
                "GRUPO FAMILIAR",
                "RESULTADO DEL SISTEMA",
                "DUPLICIDAD",
                "NÚMERO EXPEDIENTE",
                "OBSERVACIÓN DEL ARCHIVO",
                "OBSERVACIÓN"
            },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return esCeldaEditable(row, column);
        }
    };
    private final JTable table = new AppV2Table(tableModel) {
        @Override
        public String getToolTipText(MouseEvent event) {
            String tooltip = tooltipPrevisualizacion(event);
            return tooltip != null ? tooltip : super.getToolTipText(event);
        }
    };

    private File archivoSeleccionado;
    private List<CargaDiariaPreviewDTO> registros = new ArrayList<>();
    private boolean trabajando;
    private boolean cargandoTabla;
    private boolean edicionPendienteValidacion;
    private FiltroKpi kpiActivo = FiltroKpi.TODOS;

    public JPanelCargaDiariaRecepcionV2() {
        this(null);
    }

    public JPanelCargaDiariaRecepcionV2(Runnable onCargaConfirmada) {
        this.onCargaConfirmada = onCargaConfirmada;
        setLayout(new BorderLayout(12, 12));
        setBackground(AppV2Theme.BACKGROUND);
        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearTablaPreview(), BorderLayout.CENTER);
        configurarEventos();
        configurarKpisInteractivos();
        cargarPrevisualizacion(Collections.<CargaDiariaPreviewDTO>emptyList());
        actualizarResumen();
        actualizarBotones();
    }

    private JPanel crearPanelSuperior() {
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout(8, 8));
        toolbar.setBackground(AppV2Theme.SURFACE);
        toolbar.setBorder(AppV2Theme.toolbarBorder());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnDescargarPlantilla);
        acciones.add(btnArchivo);
        acciones.add(btnPrevisualizar);
        acciones.add(btnValidar);
        acciones.add(btnConfirmar);
        acciones.add(btnExportar);
        acciones.add(btnLimpiar);

        lblArchivo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblArchivo.setForeground(AppV2Theme.TEXT_SECONDARY);

        toolbar.add(acciones, BorderLayout.WEST);
        toolbar.add(lblArchivo, BorderLayout.EAST);

        JLabel ayuda = new JLabel("<html>Descargue la plantilla oficial, complete la hoja CARGA_DIARIA y luego use Seleccionar archivo. En la previsualización puede editar solo datos importados; las observaciones de validación son calculadas por el sistema.</html>");
        ayuda.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        ayuda.setForeground(AppV2Theme.TEXT_SECONDARY);

        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel mensajes = new JPanel(new BorderLayout(0, 4));
        mensajes.setOpaque(false);
        mensajes.add(ayuda, BorderLayout.NORTH);
        mensajes.add(lblEstado, BorderLayout.SOUTH);

        JPanel metricas = new JPanel(new GridLayout(1, 2, 10, 8));
        metricas.setOpaque(false);
        metricas.add(cardValidos);
        metricas.add(cardErrores);

        wrapper.add(toolbar, BorderLayout.NORTH);
        wrapper.add(mensajes, BorderLayout.CENTER);
        wrapper.add(metricas, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel crearTablaPreview() {
        JPanel container = new JPanel(new BorderLayout(0, 0));
        container.setOpaque(false);

        table.setRowHeight(32);
        table.setAutoCreateRowSorter(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setSurrendersFocusOnKeystroke(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        table.getTableHeader().setBackground(AppV2Theme.SURFACE_ALT);
        table.getTableHeader().setForeground(AppV2Theme.TEXT_SECONDARY);
        table.setShowVerticalLines(false);
        table.setGridColor(AppV2Theme.BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        AppV2TableColumnSizer.applyFriendlyDefaults(table);
        AppV2TableColumnSizer.applyWidths(
                table,
                180,
                180,
                280,
                360,
                340,
                180,
                240,
                260,
                190,
                180,
                260,
                160,
                150,
                280,
                340,
                320,
                190,
                190,
                170,
                280,
                320,
                420);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));

        columnFilterSupport = AppV2ColumnFilterSupport.install(
                "CargaDiariaRecepcion",
                table,
                scroll,
                null,
                null);

        container.add(scroll, BorderLayout.CENTER);
        return container;
    }

    private JPanel crearPanelNotas() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 12, 0));
        panel.setOpaque(false);
        panel.add(crearNota(
                "Duplicados",
                "Los duplicados se detectan únicamente por la combinación acta y titular. Se registran y quedan marcados para identificarlos en Asignación."));
        panel.add(crearNota(
                "Observaciones",
                "La observación del archivo es editable. Las observaciones de validación y alertas del sistema se recalculan al presionar Validar."));
        panel.add(crearNota(
                "Confirmación",
                "La confirmación registra expediente, solicitud, acta, personas, documento inicial e historial en una transacción sobre SDRERC_APP."));
        return panel;
    }

    private JPanel crearNota(String title, String text) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.cardBorder());

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        lblTitle.setForeground(AppV2Theme.TEXT_PRIMARY);

        JTextArea detail = new JTextArea(text);
        detail.setEditable(false);
        detail.setFocusable(false);
        detail.setOpaque(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        detail.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        detail.setForeground(AppV2Theme.TEXT_SECONDARY);

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(detail, BorderLayout.CENTER);
        return panel;
    }

    private void configurarEventos() {
        btnDescargarPlantilla.addActionListener(e -> descargarPlantilla());
        btnArchivo.addActionListener(e -> seleccionarArchivo());
        btnPrevisualizar.addActionListener(e -> previsualizar());
        AppV2Theme.estilizarBotonPrimario(btnValidar);
        AppV2Theme.estilizarBotonPrimario(btnConfirmar);
        btnValidar.addActionListener(e -> validar());
        btnConfirmar.addActionListener(e -> confirmarCarga());
        btnExportar.addActionListener(e -> exportarPrevisualizacion());
        btnLimpiar.addActionListener(e -> limpiar());
        tableModel.addTableModelListener(e -> {
            if (cargandoTabla || e.getType() != TableModelEvent.UPDATE || e.getFirstRow() < 0) {
                return;
            }
            int column = e.getColumn();
            if (column == TableModelEvent.ALL_COLUMNS || !esColumnaEditable(column)) {
                return;
            }
            sincronizarEdicionTabla(e.getFirstRow(), column);
        });
    }

    private void seleccionarArchivo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar plantilla o archivo de carga diaria");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos de carga diaria (.xlsx, .csv)", "xlsx", "csv"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            archivoSeleccionado = chooser.getSelectedFile();
            lblArchivo.setText(archivoSeleccionado.getName());
            lblEstado.setText("Archivo seleccionado correctamente. Presione Previsualizar para revisar los registros de la plantilla.");
            registros = new ArrayList<>();
            edicionPendienteValidacion = false;
            cargarPrevisualizacion(registros);
            actualizarResumen();
            actualizarBotones();
        }
    }

    private void descargarPlantilla() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar plantilla de carga diaria");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));
        chooser.setSelectedFile(new File(CargaDiariaPlantillaService.NOMBRE_ARCHIVO));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destino = asegurarExtensionXlsx(chooser.getSelectedFile());
        if (destino.exists()) {
            int confirmar = JOptionPane.showConfirmDialog(
                    this,
                    "El archivo ya existe. ¿Desea reemplazarlo?",
                    "Confirmar reemplazo",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirmar != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            plantillaService.generarPlantilla(destino);
            if (!destino.exists() || destino.length() == 0) {
                throw new IllegalStateException("El archivo no se generó en la ruta seleccionada.");
            }
            lblEstado.setText("Plantilla generada correctamente. Complete la hoja CARGA_DIARIA y luego selecciónela para previsualizar.");
            JOptionPane.showMessageDialog(
                    this,
                    "Plantilla generada correctamente:\n" + destino.getAbsolutePath(),
                    "Plantilla de carga diaria",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            mostrarError("No se pudo generar la plantilla de carga diaria.", ex);
        }
    }

    private void previsualizar() {
        if (archivoSeleccionado == null) {
            mostrarInfo("Seleccione un archivo antes de previsualizar.");
            return;
        }
        setTrabajando(true, "Leyendo archivo de carga diaria...");
        SwingWorker<List<CargaDiariaPreviewDTO>, Void> worker = new SwingWorker<List<CargaDiariaPreviewDTO>, Void>() {
            @Override
            protected List<CargaDiariaPreviewDTO> doInBackground() throws Exception {
                return parserService.leerArchivo(archivoSeleccionado);
            }

            @Override
            protected void done() {
                try {
                    registros = get();
                    edicionPendienteValidacion = false;
                    cargarPrevisualizacion(registros);
                    String diagnostico = parserService.getUltimoDiagnostico();
                    lblEstado.setText(registros.size() + " registro(s) leídos. "
                            + (diagnostico.isEmpty() ? "" : diagnostico + " ")
                            + "Presione Validar para revisar reglas y duplicados.");
                } catch (Exception ex) {
                    registros = new ArrayList<>();
                    edicionPendienteValidacion = false;
                    cargarPrevisualizacion(registros);
                    mostrarError("No se pudo previsualizar el archivo.", ex);
                } finally {
                    actualizarResumen();
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void validar() {
        detenerEdicionTabla();
        if (registros.isEmpty()) {
            mostrarInfo("Previsualice registros antes de validar.");
            return;
        }
        setTrabajando(true, "Validando registros y duplicidad por acta y titular en SDRERC_APP...");
        SwingWorker<List<CargaDiariaPreviewDTO>, Void> worker = new SwingWorker<List<CargaDiariaPreviewDTO>, Void>() {
            @Override
            protected List<CargaDiariaPreviewDTO> doInBackground() throws Exception {
                return validacionService.validar(registros);
            }

            @Override
            protected void done() {
                try {
                    registros = get();
                    edicionPendienteValidacion = false;
                    cargarPrevisualizacion(registros);
                    CargaDiariaResumenDTO resumen = CargaDiariaResumenDTO.desde(registros);
                    lblEstado.setText(resumen.getListosParaRegistrar() + " registro(s) listo(s) para confirmar.");
                } catch (Exception ex) {
                    mostrarError("No se pudo validar la carga diaria. No se registró ningún dato.", ex);
                } finally {
                    actualizarResumen();
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void confirmarCarga() {
        detenerEdicionTabla();
        CargaDiariaResumenDTO resumen = CargaDiariaResumenDTO.desde(registros);
        if (resumen.getListosParaRegistrar() == 0) {
            mostrarInfo("No hay registros listos para registrar.");
            return;
        }
        int option = JOptionPane.showConfirmDialog(
                this,
                "Se registrarán " + resumen.getListosParaRegistrar() + " expediente(s) en SDRERC_APP.\n"
                        + "Los registros con observaciones o duplicidad también se registrarán y quedarán marcados.\n\n"
                        + "¿Desea confirmar la carga?",
                "Confirmar carga diaria",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        setTrabajando(true, "Confirmando carga diaria en SDRERC_APP...");
        SwingWorker<CargaDiariaResultadoDTO, Void> worker = new SwingWorker<CargaDiariaResultadoDTO, Void>() {
            @Override
            protected CargaDiariaResultadoDTO doInBackground() throws Exception {
                return registroService.confirmarCarga(registros);
            }

            @Override
            protected void done() {
                try {
                    CargaDiariaResultadoDTO resultado = get();
                    registros = new ArrayList<CargaDiariaPreviewDTO>(resultado.getRegistros());
                    edicionPendienteValidacion = false;
                    cargarPrevisualizacion(registros);
                    lblEstado.setText(resultado.getMensaje());
                    JOptionPane.showMessageDialog(
                            JPanelCargaDiariaRecepcionV2.this,
                            resultado.getMensaje(),
                            "Carga diaria confirmada",
                            JOptionPane.INFORMATION_MESSAGE);
                    if (resultado.getRegistrados() > 0 && onCargaConfirmada != null) {
                        onCargaConfirmada.run();
                    }
                } catch (Exception ex) {
                    mostrarError("No se pudo confirmar la carga diaria. La transacción fue revertida.", ex);
                } finally {
                    actualizarResumen();
                    setTrabajando(false, null);
                }
            }
        };
        worker.execute();
    }

    private void exportarPrevisualizacion() {
        List<Integer> filasExportacion = obtenerFilasExportacion();
        if (filasExportacion.isEmpty()) {
            mostrarInfo("No hay registros para exportar.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportar previsualización a Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));
        chooser.setSelectedFile(new File(nombreArchivoExcelPrevisualizacion()));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destino = asegurarExtensionXlsx(chooser.getSelectedFile());
        if (destino.exists()) {
            int confirmar = JOptionPane.showConfirmDialog(
                    this,
                    "El archivo ya existe. ¿Desea reemplazarlo?",
                    "Confirmar reemplazo",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirmar != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            escribirExcelPrevisualizacion(destino, filasExportacion);
            JOptionPane.showMessageDialog(
                    this,
                    "Archivo Excel generado correctamente:\n" + destino.getAbsolutePath(),
                    "Exportar previsualización",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            mostrarError("No se pudo exportar la previsualización.", ex);
        }
    }

    private void limpiar() {
        archivoSeleccionado = null;
        registros = new ArrayList<>();
        edicionPendienteValidacion = false;
        kpiActivo = FiltroKpi.TODOS;
        lblArchivo.setText("Sin archivo seleccionado");
        lblEstado.setText("Seleccione un archivo .xlsx o .csv para iniciar.");
        cargarPrevisualizacion(registros);
        actualizarResumen();
        actualizarBotones();
    }

    private void cargarPrevisualizacion(List<CargaDiariaPreviewDTO> items) {
        cargandoTabla = true;
        try {
            tableModel.setRowCount(0);
            for (CargaDiariaPreviewDTO item : filtrarPorKpi(items)) {
                tableModel.addRow(crearFilaTabla(item));
            }
        } finally {
            cargandoTabla = false;
        }
    }

    private void sincronizarEdicionTabla(int modelRow, int column) {
        if (modelRow < 0 || modelRow >= registros.size() || !esColumnaEditable(column)) {
            return;
        }
        CargaDiariaPreviewDTO item = registros.get(modelRow);
        if (item.isRegistrado()) {
            actualizarFilaTabla(modelRow, item);
            return;
        }
        aplicarValorEditado(item, column, valorTabla(modelRow, column));
        actualizarValidacionLocal(item);
        actualizarFilaTabla(modelRow, item);
        edicionPendienteValidacion = true;
        actualizarBotones();
        lblEstado.setText("Celda actualizada. Presione Validar para recalcular observaciones, duplicidad y número de expediente.");
    }

    private void actualizarValidacionLocal(CargaDiariaPreviewDTO item) {
        if (item == null) {
            return;
        }
        List<String> observaciones = new ArrayList<>();
        if (!hasText(item.getNumeroTramite())) {
            observaciones.add("Dato incompleto: Número de trámite");
        }
        if (!hasText(item.getNumeroDocumento())) {
            observaciones.add("Dato incompleto: N° Documento");
        }
        if (!hasText(item.getTipoProcedimiento())) {
            observaciones.add("Dato incompleto: Procedimiento registral");
        }
        if (!hasText(item.getTipoSolicitud())) {
            observaciones.add("Dato incompleto: Tipo de solicitud");
        }
        if (!hasText(item.getTipoDocumento())) {
            observaciones.add("Dato incompleto: Tipo documento");
        }
        if (!hasText(item.getTipoActa())) {
            observaciones.add("Dato incompleto: Tipo de acta");
        }
        if (!hasText(item.getNumeroActa())) {
            observaciones.add("Dato incompleto: N° Acta");
        }
        if (!hasText(item.getTitular())) {
            observaciones.add("Dato incompleto: Titular");
        }
        if (item.getFechaRecepcion() == null && !hasText(item.getFechaRecepcionTexto())) {
            observaciones.add("Dato incompleto: Fecha de solicitud");
        }
        if (!hasText(item.getCanalRecepcion())) {
            observaciones.add("Dato incompleto: Canal recepción");
        }
        if (!hasText(item.getTipoDocumentoIdentidadSolicitante()) && hasText(item.getNumeroDocumentoIdentidadSolicitante())) {
            observaciones.add("Dato incompleto: Tipo documento identidad solicitante");
        }
        if (!hasText(item.getNumeroDocumentoIdentidadSolicitante())
                && hasText(item.getTipoDocumentoIdentidadSolicitante())
                && !"SIN DNI".equalsIgnoreCase(item.getTipoDocumentoIdentidadSolicitante())) {
            observaciones.add("Dato incompleto: N° documento identidad solicitante");
        }
        if (!hasText(item.getTipoDocumentoIdentidadTitular()) && hasText(item.getNumeroDocumentoIdentidadTitular())) {
            observaciones.add("Dato incompleto: Tipo documento identidad titular");
        }
        if (!hasText(item.getNumeroDocumentoIdentidadTitular())
                && hasText(item.getTipoDocumentoIdentidadTitular())
                && !"SIN DNI".equalsIgnoreCase(item.getTipoDocumentoIdentidadTitular())) {
            observaciones.add("Dato incompleto: N° documento identidad titular");
        }
        item.setMensajeValidacion(observaciones.isEmpty() ? null : String.join(" | ", observaciones));
        if (item.isPosibleDuplicado()) {
            item.setEstadoValidacion("Duplicado");
        } else if (!observaciones.isEmpty() || item.isGrupoFamiliar() || item.isPosibleGrupoFamiliar()) {
            item.setEstadoValidacion("Con observaciones");
        } else {
            item.setEstadoValidacion("Válido");
        }
    }

    private void aplicarValorEditado(CargaDiariaPreviewDTO item, int column, String value) {
        switch (column) {
            case 0:
                item.setTipoSolicitud(value);
                break;
            case 1:
                item.setFechaRecepcionTexto(value);
                item.setFechaRecepcion(parseFechaTabla(value));
                break;
            case 2:
                item.setRemitente(value);
                break;
            case 3:
                item.setTipoDocumentoIdentidadSolicitante(value);
                break;
            case 4:
                item.setNumeroDocumentoIdentidadSolicitante(value);
                break;
            case 5:
                item.setNumeroTramite(value);
                break;
            case 6:
                item.setCanalRecepcion(value);
                break;
            case 7:
                item.setNumeroExpedienteSgd(value);
                break;
            case 8:
                item.setTipoDocumento(value);
                break;
            case 9:
                item.setNumeroDocumento(value);
                break;
            case 10:
                item.setTipoProcedimiento(value);
                break;
            case 11:
                item.setTipoActa(value);
                break;
            case 12:
                item.setNumeroActa(value);
                break;
            case 13:
                item.setTitular(value);
                break;
            case 14:
                item.setTipoDocumentoIdentidadTitular(value);
                break;
            case 15:
                item.setNumeroDocumentoIdentidadTitular(value);
                break;
            case COL_GRUPO_FAMILIAR:
                aplicarGrupoFamiliarEditado(item, value);
                break;
            case COL_OBSERVACION_ARCHIVO:
                item.setObservacionInicial(value);
                break;
            default:
                break;
        }
    }

    private void aplicarGrupoFamiliarEditado(CargaDiariaPreviewDTO item, String value) {
        item.setGrupoFamiliar(false);
        item.setCriterioGrupoFamiliar(null);
        item.setObservacionGrupoFamiliar(null);
        if (!hasText(value)) {
            return;
        }
        String normalizado = value.trim()
                .toUpperCase()
                .replace('Á', 'A')
                .replace('É', 'E')
                .replace('Í', 'I')
                .replace('Ó', 'O')
                .replace('Ú', 'U');
        if ("SI".equals(normalizado) || "S".equals(normalizado)) {
            item.setGrupoFamiliar(true);
            item.setCriterioGrupoFamiliar("EXCEL");
        } else if (!"NO".equals(normalizado) && !"N".equals(normalizado)) {
            item.agregarObservacionGrupoFamiliar(null,
                    "Valor de Grupo familiar no reconocido: " + value.trim() + ". Se tomó No.");
        }
    }

    private void actualizarFilaTabla(int modelRow, CargaDiariaPreviewDTO item) {
        cargandoTabla = true;
        try {
            Object[] values = crearFilaTabla(item);
            for (int column = 0; column < values.length; column++) {
                tableModel.setValueAt(values[column], modelRow, column);
            }
        } finally {
            cargandoTabla = false;
        }
    }

    private Object[] crearFilaTabla(CargaDiariaPreviewDTO item) {
        return new Object[]{
            safe(item.getTipoSolicitud()),
            item.getFechaRecepcion() == null ? safe(item.getFechaRecepcionTexto()) : DATE_FORMAT.format(item.getFechaRecepcion()),
            safe(item.getRemitente()),
            safe(item.getTipoDocumentoIdentidadSolicitante()),
            documentoVisual(item.getNumeroDocumentoIdentidadSolicitante()),
            safe(item.getNumeroTramite()),
            canalVisual(item.getCanalRecepcion()),
            safe(item.getNumeroExpedienteSgd()),
            safe(item.getTipoDocumento()),
            safe(item.getNumeroDocumento()),
            safe(item.getTipoProcedimiento()),
            safe(item.getTipoActa()),
            safe(item.getNumeroActa()),
            safe(item.getTitular()),
            safe(item.getTipoDocumentoIdentidadTitular()),
            documentoVisual(item.getNumeroDocumentoIdentidadTitular()),
            item.getGrupoFamiliarTexto(),
            safe(item.getEstadoValidacion()),
            item.isPosibleDuplicado() ? "Sí" : "No",
            numeroExpedientePreview(item),
            observacionArchivoTabla(item),
            observacionValidacionTabla(item)
        };
    }

    private boolean esCeldaEditable(int row, int column) {
        return !trabajando
                && esColumnaEditable(column)
                && registros != null
                && row >= 0
                && row < registros.size()
                && !registros.get(row).isRegistrado();
    }

    private static boolean esColumnaEditable(int column) {
        return (column >= 0 && column < COL_ESTADO_VALIDACION) || column == COL_OBSERVACION_ARCHIVO;
    }

    private String valorTabla(int modelRow, int column) {
        Object value = tableModel.getValueAt(modelRow, column);
        return value == null ? null : value.toString().trim();
    }

    private void detenerEdicionTabla() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }

    private String tooltipPrevisualizacion(MouseEvent event) {
        Point point = event.getPoint();
        int viewRow = table.rowAtPoint(point);
        int viewColumn = table.columnAtPoint(point);
        if (viewRow < 0 || viewColumn < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int modelColumn = table.convertColumnIndexToModel(viewColumn);
        if (modelRow < 0 || modelRow >= registros.size()) {
            return null;
        }
        CargaDiariaPreviewDTO item = registros.get(modelRow);
        String text = null;
        if (modelColumn == COL_ESTADO_VALIDACION) {
            text = item.getMensajeValidacion();
        } else if (modelColumn == COL_POSIBLE_DUPLICADO) {
            text = item.getMotivoDuplicado();
        } else if (modelColumn == COL_GRUPO_FAMILIAR) {
            text = item.getObservacionGrupoFamiliar();
        } else if (modelColumn == COL_OBSERVACION_ARCHIVO) {
            text = item.getObservacionInicial();
        } else if (modelColumn == COL_OBSERVACIONES_VALIDACION) {
            text = observacionValidacionTabla(item);
        } else if (modelColumn == COL_NUMERO_EXPEDIENTE_GENERADO && item.isRegistrado()) {
            text = "Expediente registrado correctamente.";
        }
        return hasText(text) ? htmlTooltip(text) : null;
    }

    private void actualizarResumen() {
        CargaDiariaResumenDTO resumen = CargaDiariaResumenDTO.desde(registros);
        cardValidos.actualizar(String.valueOf(resumen.getValidos()), "Sin errores críticos");
        cardErrores.actualizar(
                String.valueOf(resumen.getConErrores()),
                "Duplicados: " + resumen.getPosiblesDuplicados()
                        + " | Grupo familiar: " + resumen.getConGrupoFamiliar()
                        + " | Pendientes de número: " + resumen.getPendientesNumero());
        actualizarBotones();
        marcarKpisSeleccionados();
    }

    private void configurarKpisInteractivos() {
        cardValidos.setOnClick(() -> activarFiltroKpi(FiltroKpi.VALIDOS));
        cardErrores.setOnClick(() -> activarFiltroKpi(FiltroKpi.ALERTAS));
        cardDuplicados.setOnClick(() -> activarFiltroKpi(FiltroKpi.DUPLICADOS));
        cardGrupoFamiliar.setOnClick(() -> activarFiltroKpi(FiltroKpi.GRUPO_FAMILIAR));
        cardPendientesNumero.setOnClick(() -> activarFiltroKpi(FiltroKpi.PENDIENTES_NUMERO));
        cardRegistrados.setOnClick(() -> activarFiltroKpi(FiltroKpi.REGISTRADOS));
        marcarKpisSeleccionados();
    }

    private void activarFiltroKpi(FiltroKpi filtro) {
        if (filtro == null) {
            filtro = FiltroKpi.TODOS;
        }
        kpiActivo = kpiActivo == filtro ? FiltroKpi.TODOS : filtro;
        cargarPrevisualizacion(registros);
        marcarKpisSeleccionados();
    }

    private void marcarKpisSeleccionados() {
        cardValidos.setSelected(kpiActivo == FiltroKpi.VALIDOS);
        cardErrores.setSelected(kpiActivo == FiltroKpi.ALERTAS);
        cardDuplicados.setSelected(kpiActivo == FiltroKpi.DUPLICADOS);
        cardGrupoFamiliar.setSelected(kpiActivo == FiltroKpi.GRUPO_FAMILIAR);
        cardPendientesNumero.setSelected(kpiActivo == FiltroKpi.PENDIENTES_NUMERO);
        cardRegistrados.setSelected(kpiActivo == FiltroKpi.REGISTRADOS);
    }

    private List<CargaDiariaPreviewDTO> filtrarPorKpi(List<CargaDiariaPreviewDTO> items) {
        List<CargaDiariaPreviewDTO> filtrados = new ArrayList<CargaDiariaPreviewDTO>();
        if (items == null || items.isEmpty() || kpiActivo == FiltroKpi.TODOS) {
            if (items != null) {
                filtrados.addAll(items);
            }
            return filtrados;
        }
        for (CargaDiariaPreviewDTO item : items) {
            if (coincideKpi(item)) {
                filtrados.add(item);
            }
        }
        return filtrados;
    }

    private boolean coincideKpi(CargaDiariaPreviewDTO item) {
        switch (kpiActivo) {
            case VALIDOS:
                return "VALIDO".equalsIgnoreCase(safe(item.getEstadoValidacion()))
                        || "VÁLIDO".equalsIgnoreCase(safe(item.getEstadoValidacion()))
                        || item.isListoParaRegistrar();
            case ALERTAS:
                return hasText(item.getMensajeValidacion())
                        || hasText(item.getObservacionInicial())
                        || item.isPosibleDuplicado()
                        || item.isGrupoFamiliar()
                        || item.isPosibleGrupoFamiliar();
            case LISTOS:
                return item.isListoParaRegistrar();
            case DUPLICADOS:
                return item.isPosibleDuplicado();
            case GRUPO_FAMILIAR:
                return item.isGrupoFamiliar() || item.isPosibleGrupoFamiliar();
            case PENDIENTES_NUMERO:
                return !item.isRegistrado() && !hasText(item.getNumeroExpedienteGenerado());
            case REGISTRADOS:
                return item.isRegistrado();
            case TODOS:
            default:
                return true;
        }
    }

    private void actualizarBotones() {
        CargaDiariaResumenDTO resumen = CargaDiariaResumenDTO.desde(registros);
        btnPrevisualizar.setEnabled(!trabajando && archivoSeleccionado != null);
        btnValidar.setEnabled(!trabajando && archivoSeleccionado != null);
        btnConfirmar.setEnabled(!trabajando && archivoSeleccionado != null && !registros.isEmpty());
        btnExportar.setEnabled(!trabajando && !registros.isEmpty());
        btnDescargarPlantilla.setEnabled(!trabajando);
        btnArchivo.setEnabled(!trabajando);
        btnLimpiar.setEnabled(!trabajando && (archivoSeleccionado != null || !registros.isEmpty()));
    }

    private void setTrabajando(boolean trabajando, String mensaje) {
        this.trabajando = trabajando;
        if (mensaje != null) {
            lblEstado.setText(mensaje);
        }
        table.repaint();
        actualizarBotones();
    }

    private void mostrarInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Registro / Recepción", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String titulo, Exception ex) {
        String message = extraerMensajeUsuario(ex);
        if (message == null || message.trim().isEmpty()) {
            message = "Revise el archivo o la conexión de SDRERC_APP.";
        }
        lblEstado.setText(titulo + " " + message);
        JOptionPane.showMessageDialog(this, titulo + "\n" + message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String extraerMensajeUsuario(Throwable throwable) {
        Throwable actual = throwable;
        while (actual != null && actual.getCause() != null) {
            actual = actual.getCause();
        }
        if (actual == null) {
            return null;
        }
        String message = actual.getMessage();
        if (message == null) {
            return null;
        }
        return message.replaceFirst("^java\\.[a-zA-Z0-9_.]+:\\s*", "").trim();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String observacionArchivoTabla(CargaDiariaPreviewDTO item) {
        return safe(item.getObservacionInicial());
    }

    private static String observacionValidacionTabla(CargaDiariaPreviewDTO item) {
        List<String> observaciones = new ArrayList<>();
        if (item == null) {
            return "Sin observación";
        }
        if (item.isPosibleDuplicado()) {
            observaciones.add("Potencial duplicado");
        }
        if (item.isGrupoFamiliar() || item.isPosibleGrupoFamiliar()) {
            observaciones.add("Posible Grupo Familiar");
        }
        observaciones.addAll(observacionesIncompletas(item.getMensajeValidacion()));
        if (observaciones.isEmpty()) {
            return "Sin observación";
        }
        return String.join(" | ", observaciones);
    }

    private static List<String> observacionesIncompletas(String mensajeValidacion) {
        List<String> observaciones = new ArrayList<>();
        if (!hasText(mensajeValidacion)) {
            return observaciones;
        }
        String[] partes = mensajeValidacion.split("\\s*\\|\\s*");
        for (String parte : partes) {
            String observacion = convertirADatoIncompleto(parte);
            if (hasText(observacion) && !observaciones.contains(observacion)) {
                observaciones.add(observacion);
            }
        }
        return observaciones;
    }

    private static String convertirADatoIncompleto(String mensaje) {
        if (!hasText(mensaje)) {
            return null;
        }
        String texto = mensaje.trim();
        String lower = texto.toLowerCase(Locale.ROOT);
        if (!lower.contains("obligatorio") && !lower.contains("inválida") && !lower.contains("invalida") && !lower.contains("determinar")) {
            return null;
        }
        if (lower.contains("número de trámite")) {
            return "Dato incompleto: Número de trámite";
        }
        if (lower.contains("número de documento")) {
            return "Dato incompleto: N° Documento";
        }
        if (lower.contains("tipo de procedimiento")) {
            return "Dato incompleto: Procedimiento registral";
        }
        if (lower.contains("tipo de solicitud")) {
            return "Dato incompleto: Tipo de solicitud";
        }
        if (lower.contains("tipo de documento de identidad del solicitante")) {
            return "Dato incompleto: Tipo documento identidad solicitante";
        }
        if (lower.contains("número de documento de identidad del solicitante")) {
            return "Dato incompleto: N° documento identidad solicitante";
        }
        if (lower.contains("tipo de documento de identidad del titular")) {
            return "Dato incompleto: Tipo documento identidad titular";
        }
        if (lower.contains("número de documento de identidad del titular")) {
            return "Dato incompleto: N° documento identidad titular";
        }
        if (lower.contains("tipo de documento")) {
            return "Dato incompleto: Tipo documento";
        }
        if (lower.contains("tipo de acta")) {
            return "Dato incompleto: Tipo de acta";
        }
        if (lower.contains("número de acta")) {
            return "Dato incompleto: N° Acta";
        }
        if (lower.contains("titular")) {
            return "Dato incompleto: Titular";
        }
        if (lower.contains("fecha de solicitud")) {
            return "Dato incompleto: Fecha de solicitud";
        }
        if (lower.contains("canal de recepción") || lower.contains("canal de recepcion")) {
            return "Dato incompleto: Canal recepción";
        }
        return null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private List<Integer> obtenerFilasExportacion() {
        List<Integer> filas = new ArrayList<>();
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            filas.add(table.convertRowIndexToModel(viewRow));
        }
        return filas;
    }

    private String nombreArchivoExcelPrevisualizacion() {
        return "previsualizacion_carga_diaria_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
    }

    private File asegurarExtensionXlsx(File archivo) {
        if (archivo.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            return archivo;
        }
        return new File(archivo.getParentFile(), archivo.getName() + ".xlsx");
    }

    private void escribirExcelPrevisualizacion(File archivo, List<Integer> filasExportacion) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(archivo)) {
            Sheet sheet = workbook.createSheet("Previsualización");
            CellStyle headerStyle = crearEstiloCabeceraExcel(workbook);
            CellStyle textStyle = crearEstiloTextoExcel(workbook);
            CellStyle dateStyle = crearEstiloFechaExcel(workbook);

            Row header = sheet.createRow(0);
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                Cell cell = header.createCell(col);
                cell.setCellValue(tableModel.getColumnName(col));
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < filasExportacion.size(); i++) {
                int modelRow = filasExportacion.get(i);
                Row row = sheet.createRow(i + 1);
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Object value = tableModel.getValueAt(modelRow, col);
                    Cell cell = row.createCell(col);
                    escribirValorExcel(cell, value, col, dateStyle, textStyle);
                }
            }

            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, filasExportacion.size(), 0, tableModel.getColumnCount() - 1));
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                sheet.autoSizeColumn(col);
                int width = sheet.getColumnWidth(col);
                sheet.setColumnWidth(col, Math.min(Math.max(width + 512, 2800), 18000));
            }
            workbook.write(out);
        }
    }

    private CellStyle crearEstiloCabeceraExcel(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        aplicarBordesExcel(style);
        return style;
    }

    private CellStyle crearEstiloTextoExcel(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        aplicarBordesExcel(style);
        return style;
    }

    private CellStyle crearEstiloFechaExcel(Workbook workbook) {
        CellStyle style = crearEstiloTextoExcel(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/MM/yyyy"));
        return style;
    }

    private void aplicarBordesExcel(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void escribirValorExcel(Cell cell, Object value, int modelColumn, CellStyle dateStyle, CellStyle textStyle) {
        if (modelColumn == 1) {
            LocalDate fecha = parseFechaTabla(value == null ? null : value.toString());
            if (fecha != null) {
                cell.setCellValue(java.sql.Date.valueOf(fecha));
                cell.setCellStyle(dateStyle);
                return;
            }
        }
        cell.setCellValue(value == null ? "" : value.toString());
        cell.setCellStyle(textStyle);
    }

    private static String numeroExpedientePreview(CargaDiariaPreviewDTO item) {
        if (item == null) {
            return "Pendiente";
        }
        if (hasText(item.getNumeroExpedienteGenerado())) {
            return item.getNumeroExpedienteGenerado();
        }
        if (item.isPosibleDuplicado()) {
            return "Sin número por duplicado";
        }
        if (ProcedimientoRegistralRules.requiereDecisionAsignacionParaNumero(item.getTipoProcedimiento())) {
            return "Sin número por procedimiento";
        }
        return "Pendiente";
    }

    private static LocalDate parseFechaTabla(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return LocalDate.parse(trimmed, DATE_FORMAT);
        } catch (DateTimeParseException ignored) {
            // Se intenta el formato de ingreso usado por la plantilla/usuarios.
        }
        try {
            return LocalDate.parse(trimmed, DATE_INPUT_FORMAT);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static String htmlTooltip(String value) {
        return "<html><body style='max-width:360px'>" + escapeHtml(value.trim()) + "</body></html>";
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String documentoVisual(String value) {
        return value == null || value.trim().isEmpty() || "SIN DNI".equalsIgnoreCase(value.trim()) ? "" : value.trim();
    }

    private static String canalVisual(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        String normalized = value.trim().toUpperCase()
                .replace('Á', 'A')
                .replace('É', 'E')
                .replace('Í', 'I')
                .replace('Ó', 'O')
                .replace('Ú', 'U')
                .replaceAll("\\s+", "_");
        if ("INTERNO".equals(normalized)) {
            return "Interno";
        }
        if ("MESA_PARTES_PRESENCIAL".equals(normalized) || "MESA_DE_PARTES_PRESENCIAL".equals(normalized)) {
            return "Mesa de partes presencial";
        }
        if ("MESA_PARTES_VIRTUAL".equals(normalized) || "MESA_DE_PARTES_VIRTUAL".equals(normalized) || "MPV".equals(normalized)) {
            return "Mesa de partes virtual";
        }
        if ("OR".equals(normalized) || "OR_PRESENCIAL".equals(normalized)) {
            return "OR Presencial";
        }
        if ("OR_PASIVO".equals(normalized) || "PASIVO_OR".equals(normalized)) {
            return "OR Pasivo";
        }
        return value.trim();
    }

    private static class ResumenCard extends JPanel {

        private final JLabel lblValue = new JLabel();
        private final JLabel lblCaption = new JLabel();
        private final Color baseBackground = AppV2Theme.SURFACE;
        private final Color selectedBackground = AppV2Theme.SURFACE_ALT;
        private Runnable clickAction;
        private boolean selected;

        private ResumenCard(String title, String value, String caption, Color accent) {
            super(new BorderLayout(6, 4));
            setBackground(baseBackground);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                    AppV2Theme.cardBorder()));
            setPreferredSize(new Dimension(0, 64));
            setMinimumSize(new Dimension(0, 60));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
            lblTitle.setForeground(AppV2Theme.TEXT_SECONDARY);

            lblValue.setFont(AppV2Theme.fontBold(18));
            lblValue.setForeground(AppV2Theme.TEXT_PRIMARY);

            lblCaption.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            lblCaption.setForeground(AppV2Theme.MUTED);

            add(lblTitle, BorderLayout.NORTH);
            add(lblValue, BorderLayout.CENTER);
            add(lblCaption, BorderLayout.SOUTH);
            actualizar(value, caption);
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (clickAction != null) {
                        clickAction.run();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setCursor(Cursor.getDefaultCursor());
                }
            };
            instalarMouseRecursivo(this, mouseAdapter);
        }

        private void actualizar(String value, String caption) {
            lblValue.setText(value);
            lblCaption.setText(caption);
        }

        private void setOnClick(Runnable clickAction) {
            this.clickAction = clickAction;
        }

        private void setSelected(boolean selected) {
            this.selected = selected;
            setBackground(selected ? selectedBackground : baseBackground);
            setOpaque(true);
            repaint();
        }

        private boolean isSelected() {
            return selected;
        }

        private void instalarMouseRecursivo(Component component, MouseAdapter mouseAdapter) {
            component.addMouseListener(mouseAdapter);
            if (component instanceof JPanel) {
                for (Component child : ((JPanel) component).getComponents()) {
                    instalarMouseRecursivo(child, mouseAdapter);
                }
            }
        }
    }
}
