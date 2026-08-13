package com.sdrerc.ui.views.administracion.plantillas;

import com.sdrerc.application.sdrercapp.PlantillaDocumentoService;
import com.sdrerc.domain.dto.sdrercapp.PlantillaDocumentoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.PlantillaDocumentoDAO;
import com.sdrerc.ui.appv2.components.AppV2ColumnFilterSupport;
import com.sdrerc.ui.appv2.components.AppV2OperationalSplitPanel;
import com.sdrerc.ui.appv2.components.AppV2Table;
import com.sdrerc.ui.appv2.components.AppV2TableColumnSizer;
import com.sdrerc.ui.appv2.components.BadgeV2;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class JPanelPlantillasDocumentoV2 extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String VARIABLES_DISPONIBLES =
            "Variables de texto (formato #variable# dentro del Word):\n\n"
            + "#nomTitular# #dniTitular# #nomSolicitante# #dniSolicitante#\n"
            + "#tipoActa# #nroActa# #tipoProcedimiento# #direccion# #correo#\n"
            + "#fechaSolicitud# #nroTramiteWeb# #canalRecepcion# #fechaActual#\n"
            + "#numDoc# #tipoDoc# #numDocInforme# #fechaDocInforme#\n"
            + "#NUMERO_EXPEDIENTE# #NUMERO_EXPEDIENTE_SGD# #TRAMITE_WEB#\n"
            + "#TITULAR# #SOLICITANTE# #TIPO_ACTA# #NUMERO_ACTA#\n"
            + "#PROCEDIMIENTO_REGISTRAL# #ABOGADO_RESPONSABLE# #EQUIPO#\n"
            + "#TIPO_DOCUMENTO# #ESTADO_DOCUMENTO# #FECHA_DOCUMENTO# #NUMERO_DOCUMENTO#\n"
            + "#DESCRIPCION_DOCUMENTO# #HOJA_ENVIO# #FECHA_RESPUESTA# #FECHA_ACUSE#\n"
            + "resAnalisis (resultado del análisis: Procedente / Improcedente / ...)\n\n"
            + "Bloques condicionales (van dentro del propio Word, en párrafos aparte):\n\n"
            + "[[SI_canalRecepcion:MPV]]\n"
            + "  ...texto que solo aparece si el canal de recepción es MPV...\n"
            + "[[FIN_SI]]\n\n"
            + "[[SI_resAnalisis:PROCEDENTE|PROCEDENTE_EN_PARTE]]\n"
            + "  ...texto que solo aparece si el resultado es Procedente o Procedente en parte...\n"
            + "[[FIN_SI]]\n\n"
            + "El nombre de la variable no distingue mayúsculas ni guiones bajos "
            + "(canalRecepcion y CANAL_RECEPCION son el mismo criterio). Los valores esperados "
            + "se separan con \"|\" y tampoco distinguen tildes/mayúsculas. Los párrafos "
            + "marcadores [[SI_...]] y [[FIN_SI]] siempre se eliminan al generar el documento; "
            + "solo queda el texto intermedio si el valor actual del expediente coincide.\n\n"
            + "Bloques de contenido (alternativa recomendada, sin editar el Word a mano):\n\n"
            + "Use el botón \"Administrar bloques de contenido\" para armar el texto dinámico "
            + "desde un formulario (título + contenido + condición opcional con combos), sin "
            + "escribir marcadores. Para que los bloques aparezcan, la plantilla base debe tener "
            + "un párrafo con el texto exacto [[CONTENIDO]] en el punto donde deben insertarse.\n\n"
            + "Si la plantilla necesita más de un punto de contenido dinámico (por ejemplo "
            + "\"Antecedentes\" y \"Recomendaciones\" en puntos distintos del documento), use "
            + "marcadores nombrados: [[CONTENIDO:antecedentes]] en un punto y "
            + "[[CONTENIDO:recomendaciones]] en otro, y asigne la misma \"Sección\" (antecedentes / "
            + "recomendaciones) al crear cada bloque en el formulario. Cada marcador solo recibe "
            + "los bloques de su propia sección; los bloques sin sección van al marcador sin "
            + "nombre [[CONTENIDO]].";

    private final PlantillaDocumentoService plantillaDocumentoService;

    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnCargarVersion = new JButton("Cargar nueva versión");
    private final JButton btnDescargar = new JButton("Descargar plantilla actual");
    private final JButton btnActivarVersion = new JButton("Activar versión seleccionada");
    private final JButton btnVerVariables = new JButton("Ver variables disponibles");
    private final JButton btnBloques = new JButton("Administrar bloques de contenido");
    private final JLabel lblEstado = new JLabel(
            "Seleccione un tipo de documento para administrar su plantilla Word.");

    private final TiposTableModel tiposModel = new TiposTableModel();
    private final JTable tblTipos = new AppV2Table(tiposModel);
    private JScrollPane scrollTipos;
    private AppV2OperationalSplitPanel splitDetalle;

    private final HistorialTableModel historialModel = new HistorialTableModel();
    private final JTable tblHistorial = new AppV2Table(historialModel);

    private final JLabel lblTipoSeleccionado = new JLabel("-");
    private final JLabel lblVigente = new JLabel("-");

    private final MetricCardV2 cardTotal = new MetricCardV2("Tipos de documento", "0", "Con plantilla en Análisis", AppV2Theme.PRIMARY);
    private final MetricCardV2 cardConPlantilla = new MetricCardV2("Con plantilla propia", "0", "Administradas aquí", AppV2Theme.SUCCESS);
    private final MetricCardV2 cardSinPlantilla = new MetricCardV2("Sin plantilla propia", "0", "Usan docs/plantillas", AppV2Theme.WARNING);

    private final com.sdrerc.application.sdrercapp.PlantillaBloqueService plantillaBloqueService =
            new com.sdrerc.application.sdrercapp.PlantillaBloqueService();

    private final List<PlantillaDocumentoDTO> tipos = new ArrayList<PlantillaDocumentoDTO>();
    private final List<PlantillaDocumentoDTO> historial = new ArrayList<PlantillaDocumentoDTO>();
    private PlantillaDocumentoDTO tipoSeleccionado;

    public JPanelPlantillasDocumentoV2() {
        this(new PlantillaDocumentoService());
    }

    public JPanelPlantillasDocumentoV2(PlantillaDocumentoService plantillaDocumentoService) {
        this.plantillaDocumentoService = plantillaDocumentoService;
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());
        add(crearMetricas(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        configurarTablas();
        configurarEventos();
        cargarTipos();
    }

    private JPanel crearMetricas() {
        JPanel metricas = new JPanel(new GridLayout(1, 3, 12, 0));
        metricas.setOpaque(false);
        metricas.add(cardTotal);
        metricas.add(cardConPlantilla);
        metricas.add(cardSinPlantilla);
        return metricas;
    }

    private Component crearCentro() {
        splitDetalle = new AppV2OperationalSplitPanel(crearPanelListado(), crearPanelDetalle(), 560, 380, 460);
        return splitDetalle;
    }

    private JPanel crearPanelListado() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnRefrescar);

        lblEstado.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);

        JPanel barra = new JPanel(new BorderLayout(8, 8));
        barra.setOpaque(false);
        barra.add(acciones, BorderLayout.NORTH);
        barra.add(lblEstado, BorderLayout.SOUTH);

        panel.add(barra, BorderLayout.NORTH);
        scrollTipos = new JScrollPane(tblTipos);
        panel.add(scrollTipos, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelDetalle() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(460, 0));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(AppV2Theme.sectionBorder());

        JLabel title = new JLabel("Plantilla del tipo de documento");
        title.setFont(AppV2Theme.fontBold(18));
        title.setForeground(AppV2Theme.TEXT_PRIMARY);

        JPanel resumen = new JPanel(new GridLayout(0, 1, 0, 4));
        resumen.setOpaque(false);
        resumen.add(fila("Tipo de documento", lblTipoSeleccionado));
        resumen.add(fila("Versión vigente", lblVigente));

        JPanel accionesPrincipales = new JPanel(new GridLayout(0, 1, 0, 8));
        accionesPrincipales.setOpaque(false);
        accionesPrincipales.add(btnCargarVersion);
        accionesPrincipales.add(btnDescargar);
        accionesPrincipales.add(btnBloques);
        accionesPrincipales.add(btnVerVariables);

        JLabel lblHistorial = new JLabel("Historial de versiones");
        lblHistorial.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lblHistorial.setForeground(AppV2Theme.TEXT_SECONDARY);
        JScrollPane scrollHistorial = new JScrollPane(tblHistorial);
        scrollHistorial.setPreferredSize(new Dimension(420, 180));

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setOpaque(false);
        centro.add(resumen, BorderLayout.NORTH);
        centro.add(accionesPrincipales, BorderLayout.CENTER);
        JPanel historialWrap = new JPanel(new BorderLayout(0, 4));
        historialWrap.setOpaque(false);
        historialWrap.add(lblHistorial, BorderLayout.NORTH);
        historialWrap.add(scrollHistorial, BorderLayout.CENTER);
        historialWrap.add(btnActivarVersion, BorderLayout.SOUTH);
        centro.add(historialWrap, BorderLayout.SOUTH);

        panel.add(title, BorderLayout.NORTH);
        panel.add(centro, BorderLayout.CENTER);
        return panel;
    }

    private JPanel fila(String etiqueta, JLabel valor) {
        JPanel fila = new JPanel(new BorderLayout(8, 0));
        fila.setOpaque(false);
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lbl.setForeground(AppV2Theme.TEXT_SECONDARY);
        valor.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        valor.setForeground(AppV2Theme.TEXT_PRIMARY);
        fila.add(lbl, BorderLayout.NORTH);
        fila.add(valor, BorderLayout.CENTER);
        return fila;
    }

    private void configurarTablas() {
        tblTipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTipos.setAutoCreateRowSorter(false);
        tblTipos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollTipos.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tblTipos.setDefaultRenderer(Object.class, new TipoCellRenderer());
        AppV2TableColumnSizer.applyWidths(tblTipos, 260, 150, 100, 160, 170);
        AppV2ColumnFilterSupport.install("Administracion.PlantillasDocumento", tblTipos, scrollTipos, null, null);

        tblHistorial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHistorial.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        AppV2TableColumnSizer.applyWidths(tblHistorial, 60, 150, 70, 150, 160);
    }

    private void configurarEventos() {
        btnRefrescar.addActionListener(e -> cargarTipos());
        btnCargarVersion.addActionListener(e -> cargarNuevaVersion());
        btnDescargar.addActionListener(e -> descargarVigente());
        btnActivarVersion.addActionListener(e -> activarVersionSeleccionada());
        btnVerVariables.addActionListener(e -> mostrarVariablesDisponibles());
        btnBloques.addActionListener(e -> administrarBloques());

        tblTipos.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    seleccionarTipo();
                }
            }
        });
        tblTipos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && tblTipos.getSelectedRow() >= 0) {
                    mostrarPanelDetalle();
                }
            }
        });
    }

    private void cargarTipos() {
        lblEstado.setText("Consultando tipos de documento...");
        btnRefrescar.setEnabled(false);
        new SwingWorker<List<PlantillaDocumentoDTO>, Void>() {
            @Override
            protected List<PlantillaDocumentoDTO> doInBackground() throws Exception {
                return plantillaDocumentoService.listarTiposConPlantilla();
            }

            @Override
            protected void done() {
                btnRefrescar.setEnabled(true);
                try {
                    tipos.clear();
                    tipos.addAll(get());
                    tiposModel.fireTableDataChanged();
                    actualizarMetricas();
                    lblEstado.setText(tipos.size() + " tipo(s) de documento encontrados.");
                    tipoSeleccionado = null;
                    historial.clear();
                    historialModel.fireTableDataChanged();
                    actualizarResumenDetalle();
                } catch (Exception ex) {
                    lblEstado.setText("No se pudo consultar el catálogo de plantillas.");
                    mostrarError("No se pudo consultar el catálogo de plantillas.", ex);
                }
            }
        }.execute();
    }

    private void seleccionarTipo() {
        int viewRow = tblTipos.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = tblTipos.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= tipos.size()) {
            return;
        }
        tipoSeleccionado = tipos.get(modelRow);
        actualizarResumenDetalle();
        cargarHistorial();
    }

    private void actualizarResumenDetalle() {
        if (tipoSeleccionado == null) {
            lblTipoSeleccionado.setText("-");
            lblVigente.setText("-");
            return;
        }
        lblTipoSeleccionado.setText(tipoSeleccionado.getTipoDocumentoNombre());
        lblVigente.setText(tipoSeleccionado.isActivo()
                ? tipoSeleccionado.getNombreArchivo() + " (v" + tipoSeleccionado.getVersion() + ")"
                : "Sin plantilla propia (usa docs/plantillas)");
    }

    private void cargarHistorial() {
        if (tipoSeleccionado == null) {
            return;
        }
        final Long idTipo = tipoSeleccionado.getIdTipoDocumentoAdjunto();
        new SwingWorker<List<PlantillaDocumentoDTO>, Void>() {
            @Override
            protected List<PlantillaDocumentoDTO> doInBackground() throws Exception {
                return plantillaDocumentoService.listarHistorial(idTipo);
            }

            @Override
            protected void done() {
                try {
                    historial.clear();
                    historial.addAll(get());
                    historialModel.fireTableDataChanged();
                } catch (Exception ex) {
                    mostrarError("No se pudo consultar el historial de versiones.", ex);
                }
            }
        }.execute();
    }

    private void mostrarPanelDetalle() {
        if (splitDetalle != null) {
            splitDetalle.setSideVisible(true);
        }
    }

    private void cargarNuevaVersion() {
        if (tipoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un tipo de documento.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Cargar plantilla Word - " + tipoSeleccionado.getTipoDocumentoNombre());
        chooser.setFileFilter(new FileNameExtensionFilter("Documento Word (*.docx)", "docx"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        final File archivo = chooser.getSelectedFile();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Se registrará \"" + archivo.getName() + "\" como la nueva versión vigente de la plantilla "
                        + "para \"" + tipoSeleccionado.getTipoDocumentoNombre() + "\".\n"
                        + "La versión anterior quedará en el historial, no se elimina. ¿Desea continuar?",
                "Cargar nueva versión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        final Long idTipo = tipoSeleccionado.getIdTipoDocumentoAdjunto();
        lblEstado.setText("Cargando nueva versión...");
        btnCargarVersion.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                byte[] contenido = Files.readAllBytes(archivo.toPath());
                plantillaDocumentoService.cargarNuevaVersion(idTipo, archivo.getName(), contenido, null);
                return null;
            }

            @Override
            protected void done() {
                btnCargarVersion.setEnabled(true);
                try {
                    get();
                    lblEstado.setText("Nueva versión de plantilla cargada correctamente.");
                    cargarTipos();
                } catch (Exception ex) {
                    lblEstado.setText("No se pudo cargar la nueva versión.");
                    mostrarError("No se pudo cargar la nueva versión de la plantilla.", ex);
                }
            }
        }.execute();
    }

    private void descargarVigente() {
        if (tipoSeleccionado == null || !tipoSeleccionado.isActivo() || tipoSeleccionado.getIdPlantillaDocumento() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "El tipo de documento seleccionado no tiene una plantilla propia cargada.",
                    "Descargar plantilla",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        descargarVersion(tipoSeleccionado.getIdPlantillaDocumento(), tipoSeleccionado.getNombreArchivo());
    }

    private void activarVersionSeleccionada() {
        int viewRow = tblHistorial.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una versión del historial.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tblHistorial.convertRowIndexToModel(viewRow);
        final PlantillaDocumentoDTO version = historial.get(modelRow);
        if (version.isActivo()) {
            JOptionPane.showMessageDialog(this, "Esa versión ya está activa.", "Activar versión", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Se activará la versión " + version.getVersion() + " (" + version.getNombreArchivo() + ") "
                        + "como plantilla vigente. ¿Desea continuar?",
                "Activar versión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        btnActivarVersion.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                plantillaDocumentoService.activarVersion(version.getIdPlantillaDocumento());
                return null;
            }

            @Override
            protected void done() {
                btnActivarVersion.setEnabled(true);
                try {
                    get();
                    lblEstado.setText("Versión " + version.getVersion() + " activada correctamente.");
                    cargarTipos();
                } catch (Exception ex) {
                    mostrarError("No se pudo activar la versión seleccionada.", ex);
                }
            }
        }.execute();
    }

    private void descargarVersion(final Long idPlantillaDocumento, String nombreSugerido) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar plantilla Word");
        chooser.setSelectedFile(new File(nombreSugerido == null ? "plantilla.docx" : nombreSugerido));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        final File destino = chooser.getSelectedFile();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                PlantillaDocumentoDAO.ContenidoPlantilla contenido = plantillaDocumentoService.descargarContenido(idPlantillaDocumento);
                Files.write(destino.toPath(), contenido.getContenido());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    lblEstado.setText("Plantilla descargada en " + destino.getAbsolutePath());
                } catch (Exception ex) {
                    mostrarError("No se pudo descargar la plantilla.", ex);
                }
            }
        }.execute();
    }

    private void administrarBloques() {
        if (tipoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un tipo de documento.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DlgBloquesPlantillaV2 dialogo = new DlgBloquesPlantillaV2(
                javax.swing.SwingUtilities.getWindowAncestor(this),
                plantillaBloqueService,
                tipoSeleccionado.getIdTipoDocumentoAdjunto(),
                tipoSeleccionado.getTipoDocumentoNombre());
        dialogo.setVisible(true);
    }

    private void mostrarVariablesDisponibles() {
        JTextArea texto = new JTextArea(VARIABLES_DISPONIBLES, 22, 60);
        texto.setEditable(false);
        texto.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(texto),
                "Variables disponibles en las plantillas",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void actualizarMetricas() {
        int conPlantilla = 0;
        for (PlantillaDocumentoDTO item : tipos) {
            if (item.isActivo()) {
                conPlantilla++;
            }
        }
        cardTotal.setValue(String.valueOf(tipos.size()));
        cardConPlantilla.setValue(String.valueOf(conPlantilla));
        cardSinPlantilla.setValue(String.valueOf(tipos.size() - conPlantilla));
    }

    private void mostrarError(String mensaje, Exception ex) {
        Throwable causa = ex;
        if (ex instanceof java.util.concurrent.ExecutionException && ex.getCause() != null) {
            causa = ex.getCause();
        }
        String detalle = causa == null ? "" : causa.getMessage();
        if (causa instanceof SQLException && detalle != null && detalle.contains("ORA-00942")) {
            detalle = "Falta aplicar el script db/sdrerc_app/scripts/75_plantilla_documento.sql.";
        }
        if (causa instanceof IOException) {
            detalle = detalle == null ? "Error de lectura/escritura de archivo." : detalle;
        }
        JOptionPane.showMessageDialog(this, mensaje + "\n" + detalle, "Plantillas de documento", JOptionPane.ERROR_MESSAGE);
    }

    private final class TiposTableModel extends AbstractTableModel {
        private final String[] columns = {"Tipo de documento", "Plantilla vigente", "Versión", "Cargado por", "Fecha de carga"};

        @Override
        public int getRowCount() {
            return tipos.size();
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
        public Object getValueAt(int rowIndex, int columnIndex) {
            PlantillaDocumentoDTO dto = tipos.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return dto.getTipoDocumentoNombre();
                case 1:
                    return dto.isActivo() ? dto.getNombreArchivo() : "Sin plantilla propia";
                case 2:
                    return dto.isActivo() ? "v" + dto.getVersion() : "-";
                case 3:
                    return dto.isActivo() ? dto.getCargadoPor() : "-";
                case 4:
                    return dto.isActivo() && dto.getCreadoEn() != null ? DATE_FORMAT.format(dto.getCreadoEn()) : "-";
                default:
                    return "";
            }
        }
    }

    private final class HistorialTableModel extends AbstractTableModel {
        private final String[] columns = {"Versión", "Archivo", "Estado", "Cargado por", "Fecha"};

        @Override
        public int getRowCount() {
            return historial.size();
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
        public Object getValueAt(int rowIndex, int columnIndex) {
            PlantillaDocumentoDTO dto = historial.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return "v" + dto.getVersion();
                case 1:
                    return dto.getNombreArchivo();
                case 2:
                    return dto.isActivo() ? "Vigente" : "Histórica";
                case 3:
                    return dto.getCargadoPor();
                case 4:
                    return dto.getCreadoEn() != null ? DATE_FORMAT.format(dto.getCreadoEn()) : "-";
                default:
                    return "";
            }
        }
    }

    private final class TipoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            int modelColumn = table.convertColumnIndexToModel(column);
            if (!isSelected && modelColumn == 1) {
                boolean tienePlantilla = row < tipos.size() && tipos.get(row).isActivo();
                BadgeV2 badge = new BadgeV2(
                        String.valueOf(value),
                        tienePlantilla ? AppV2Theme.SOFT_GREEN : AppV2Theme.SOFT_GRAY,
                        tienePlantilla ? AppV2Theme.SUCCESS : AppV2Theme.TEXT_SECONDARY);
                return badge;
            }
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? AppV2Theme.SURFACE : AppV2Theme.SURFACE_ALT);
                c.setForeground(AppV2Theme.TEXT_PRIMARY);
            }
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setToolTipText(value == null ? "" : String.valueOf(value));
            return c;
        }
    }
}
