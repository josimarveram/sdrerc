package com.sdrerc.ui.views.registrorecepcion;

import com.sdrerc.application.sdrercapp.CargaDiariaArchivoParserService;
import com.sdrerc.application.sdrercapp.CargaDiariaPlantillaService;
import com.sdrerc.application.sdrercapp.CargaDiariaRegistroService;
import com.sdrerc.application.sdrercapp.CargaDiariaValidacionService;
import com.sdrerc.domain.rules.ProcedimientoRegistralRules;
import com.sdrerc.domain.dto.sdrercapp.CargaDiariaPreviewDTO;
import com.sdrerc.domain.dto.sdrercapp.CargaDiariaResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CargaDiariaResumenDTO;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

public class JPanelCargaDiariaRecepcionV2 extends JPanel {

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
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JLabel lblArchivo = new JLabel("Sin archivo seleccionado");
    private final JLabel lblEstado = new JLabel("Seleccione un archivo .xlsx o .csv para iniciar.");

    private final ResumenCard cardTotal = new ResumenCard("Total leídos", "0", "Pendiente de archivo", AppV2Theme.INFO);
    private final ResumenCard cardValidos = new ResumenCard("Válidos", "0", "Pendiente de validación", AppV2Theme.SUCCESS);
    private final ResumenCard cardErrores = new ResumenCard("Con alertas", "0", "No bloqueantes", AppV2Theme.WARNING);
    private final ResumenCard cardDuplicados = new ResumenCard("Posibles duplicados", "0", "Acta + titular", AppV2Theme.WARNING);
    private final ResumenCard cardGrupoFamiliar = new ResumenCard("Grupo familiar", "0", "Marca o alerta", AppV2Theme.TEAL);
    private final ResumenCard cardPendientesNumero = new ResumenCard("Pendientes de número", "0", "Asignación decide", AppV2Theme.WARNING);
    private final ResumenCard cardListos = new ResumenCard("Listos para registrar", "0", "Confirmación requerida", AppV2Theme.TEAL);
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
                "OBSERVACIONES DE VALIDACIÓN"
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

    public JPanelCargaDiariaRecepcionV2() {
        this(null);
    }

    public JPanelCargaDiariaRecepcionV2(Runnable onCargaConfirmada) {
        this.onCargaConfirmada = onCargaConfirmada;
        setLayout(new BorderLayout(12, 12));
        setBackground(AppV2Theme.BACKGROUND);
        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearTablaPreview(), BorderLayout.CENTER);
        add(crearPanelNotas(), BorderLayout.SOUTH);
        configurarEventos();
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

        JPanel metricas = new JPanel(new GridLayout(2, 4, 10, 8));
        metricas.setOpaque(false);
        metricas.add(cardTotal);
        metricas.add(cardValidos);
        metricas.add(cardErrores);
        metricas.add(cardDuplicados);
        metricas.add(cardGrupoFamiliar);
        metricas.add(cardPendientesNumero);
        metricas.add(cardListos);
        metricas.add(cardRegistrados);

        wrapper.add(toolbar, BorderLayout.NORTH);
        wrapper.add(mensajes, BorderLayout.CENTER);
        wrapper.add(metricas, BorderLayout.SOUTH);
        return wrapper;
    }

    private JScrollPane crearTablaPreview() {
        table.setRowHeight(32);
        table.setAutoCreateRowSorter(true);
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
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
        return scroll;
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
        btnValidar.addActionListener(e -> validar());
        btnConfirmar.addActionListener(e -> confirmarCarga());
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

    private void limpiar() {
        archivoSeleccionado = null;
        registros = new ArrayList<>();
        edicionPendienteValidacion = false;
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
            for (CargaDiariaPreviewDTO item : items) {
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
        item.reiniciarValidacion();
        actualizarFilaTabla(modelRow, item);
        edicionPendienteValidacion = true;
        actualizarBotones();
        lblEstado.setText("Celda actualizada. Presione Validar para recalcular observaciones, duplicidad y número de expediente.");
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
            observacionesValidacionTabla(item)
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
            text = observacionesValidacionTabla(item);
        } else if (modelColumn == COL_NUMERO_EXPEDIENTE_GENERADO && item.isRegistrado()) {
            text = "Expediente registrado correctamente.";
        }
        return hasText(text) ? htmlTooltip(text) : null;
    }

    private void actualizarResumen() {
        CargaDiariaResumenDTO resumen = CargaDiariaResumenDTO.desde(registros);
        cardTotal.actualizar(String.valueOf(resumen.getTotalLeidos()), resumen.getTotalLeidos() == 0 ? "Pendiente de archivo" : "Archivo leído");
        cardValidos.actualizar(String.valueOf(resumen.getValidos()), "Sin errores críticos");
        cardErrores.actualizar(String.valueOf(resumen.getConErrores()), "No bloqueantes");
        cardDuplicados.actualizar(String.valueOf(resumen.getPosiblesDuplicados()), "Acta + titular");
        cardGrupoFamiliar.actualizar(String.valueOf(resumen.getConGrupoFamiliar()), "Marca o alerta");
        cardPendientesNumero.actualizar(String.valueOf(resumen.getPendientesNumero()), "Asignación decide");
        cardListos.actualizar(String.valueOf(resumen.getListosParaRegistrar()), "Confirmación requerida");
        cardRegistrados.actualizar(String.valueOf(resumen.getRegistrados()), "En SDRERC_APP");
        actualizarBotones();
    }

    private void actualizarBotones() {
        CargaDiariaResumenDTO resumen = CargaDiariaResumenDTO.desde(registros);
        btnPrevisualizar.setEnabled(!trabajando && archivoSeleccionado != null);
        btnValidar.setEnabled(!trabajando && !registros.isEmpty());
        btnConfirmar.setEnabled(!trabajando && !edicionPendienteValidacion && resumen.getListosParaRegistrar() > 0);
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

    private static String observacionesValidacionTabla(CargaDiariaPreviewDTO item) {
        String observacion = appendObservacion("", item.getObservacionGrupoFamiliar());
        observacion = appendObservacion(observacion, item.getMensajeValidacion());
        return observacion;
    }

    private static String appendObservacion(String actual, String nueva) {
        if (!hasText(nueva)) {
            return safe(actual);
        }
        if (!hasText(actual)) {
            return nueva.trim();
        }
        if (actual.contains(nueva.trim())) {
            return actual;
        }
        return actual + " | " + nueva.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
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

    private static File asegurarExtensionXlsx(File file) {
        String path = file.getAbsolutePath();
        if (path.toLowerCase().endsWith(".xlsx")) {
            return file;
        }
        return new File(path + ".xlsx");
    }

    private static class ResumenCard extends JPanel {

        private final JLabel lblValue = new JLabel();
        private final JLabel lblCaption = new JLabel();

        private ResumenCard(String title, String value, String caption, Color accent) {
            super(new BorderLayout(6, 4));
            setBackground(AppV2Theme.SURFACE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                    AppV2Theme.cardBorder()));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
            lblTitle.setForeground(AppV2Theme.TEXT_SECONDARY);

            lblValue.setFont(AppV2Theme.fontBold(22));
            lblValue.setForeground(AppV2Theme.TEXT_PRIMARY);

            lblCaption.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
            lblCaption.setForeground(AppV2Theme.MUTED);

            add(lblTitle, BorderLayout.NORTH);
            add(lblValue, BorderLayout.CENTER);
            add(lblCaption, BorderLayout.SOUTH);
            actualizar(value, caption);
        }

        private void actualizar(String value, String caption) {
            lblValue.setText(value);
            lblCaption.setText(caption);
        }
    }
}
