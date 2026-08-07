package com.sdrerc.ui.views.dashboard;

import com.sdrerc.application.sdrercapp.DashboardService;
import com.sdrerc.domain.dto.sdrercapp.CargaLaboralAbogadoDTO;
import com.sdrerc.domain.dto.sdrercapp.DashboardConteoDTO;
import com.sdrerc.domain.dto.sdrercapp.DashboardResumenDTO;
import com.sdrerc.domain.dto.sdrercapp.DashboardTendenciaMensualDTO;
import com.sdrerc.ui.appv2.components.AppV2ResponsiveGridPanel;
import com.sdrerc.ui.appv2.components.MetricCardV2;
import com.sdrerc.ui.appv2.components.PremiumDateFieldV2;
import com.sdrerc.ui.appv2.theme.AppV2Theme;
import com.sdrerc.util.DateRangePickerSupport;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 * Dashboard gerencial (reportes de gestión), exclusivo ADMIN_SISTEMA. Solo lectura: no hay
 * acciones de edición, únicamente KPIs y gráficos agregados con un rango de fechas.
 */
public class JPanelDashboardV2 extends JPanel {

    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MMM yyyy", new Locale("es", "PE"));

    private static final Color[] PALETA = {
        AppV2Theme.PRIMARY, AppV2Theme.TEAL, AppV2Theme.SUCCESS, AppV2Theme.WARNING,
        AppV2Theme.INDIGO, AppV2Theme.ERROR, AppV2Theme.INFO, AppV2Theme.MUTED
    };

    private final DashboardService dashboardService;

    private final MetricCardV2 cardActivos = new MetricCardV2("Expedientes activos", "0", "En trámite", AppV2Theme.INFO);
    private final MetricCardV2 cardVencidos = new MetricCardV2("Vencidos", "0", "Plazo excedido", AppV2Theme.ERROR);
    private final MetricCardV2 cardPorVencer = new MetricCardV2("Por vencer", "0", "0 a 5 días", AppV2Theme.WARNING);
    private final MetricCardV2 cardIngresados = new MetricCardV2("Ingresados", "0", "En el periodo", AppV2Theme.PRIMARY);
    private final MetricCardV2 cardCerrados = new MetricCardV2("Cerrados", "0", "En el periodo", AppV2Theme.SUCCESS);

    private final PremiumDateFieldV2 fechaDesde = new PremiumDateFieldV2();
    private final PremiumDateFieldV2 fechaHasta = new PremiumDateFieldV2();
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JLabel lblEstado = new JLabel(" ");

    private final JPanel panelGraficoEtapas = crearContenedorGrafico("Expedientes por etapa");
    private final JPanel panelGraficoResultadoAnalisis = crearContenedorGrafico("Resultado de análisis (periodo)");
    private final JPanel panelGraficoCargaAbogados = crearContenedorGrafico("Carga por abogado (top 10)");
    private final JPanel panelGraficoTendencia = crearContenedorGrafico("Ingresados vs. cerrados por mes");
    private final JPanel panelGraficoNotificacion = crearContenedorGrafico("Estado final de notificación");

    public JPanelDashboardV2() {
        this(new DashboardService());
    }

    public JPanelDashboardV2(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
        setLayout(new BorderLayout(14, 14));
        setBackground(AppV2Theme.BACKGROUND);
        setBorder(AppV2Theme.pageBorder());

        if (!dashboardService.tieneAcceso()) {
            add(crearPanelAccesoRestringido(), BorderLayout.CENTER);
            return;
        }

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        inicializarFechas();
        refrescar();
    }

    private JPanel crearPanelAccesoRestringido() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        JLabel lbl = new JLabel("El Dashboard es exclusivo para administradores del sistema.");
        lbl.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        lbl.setForeground(AppV2Theme.TEXT_SECONDARY);
        panel.add(lbl);
        return panel;
    }

    private JPanel crearEncabezado() {
        JPanel contenedor = new JPanel(new BorderLayout(0, 10));
        contenedor.setOpaque(false);

        JLabel titulo = new JLabel("Dashboard gerencial");
        titulo.setFont(AppV2Theme.fontBold(20));
        titulo.setForeground(AppV2Theme.TEXT_PRIMARY);
        JLabel subtitulo = new JLabel("Vista consolidada de expedientes por etapa, resultados y carga de trabajo.");
        subtitulo.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
        subtitulo.setForeground(AppV2Theme.TEXT_SECONDARY);
        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new javax.swing.BoxLayout(titulos, javax.swing.BoxLayout.Y_AXIS));
        titulos.add(titulo);
        titulos.add(subtitulo);

        JPanel filtros = new JPanel(new GridBagLayout());
        filtros.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 6, 0, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;
        gbc.gridx = 0;
        filtros.add(etiqueta("Desde"), gbc);
        gbc.gridx = 1;
        fechaDesde.setPreferredSize(new Dimension(150, 36));
        filtros.add(fechaDesde, gbc);
        gbc.gridx = 2;
        filtros.add(etiqueta("Hasta"), gbc);
        gbc.gridx = 3;
        fechaHasta.setPreferredSize(new Dimension(150, 36));
        filtros.add(fechaHasta, gbc);
        gbc.gridx = 4;
        AppV2Theme.estilizarBotonPrimario(btnRefrescar);
        btnRefrescar.addActionListener(e -> refrescar());
        filtros.add(btnRefrescar, gbc);

        JPanel superior = new JPanel(new BorderLayout(10, 10));
        superior.setOpaque(false);
        superior.add(titulos, BorderLayout.WEST);
        superior.add(filtros, BorderLayout.EAST);

        lblEstado.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL));
        lblEstado.setForeground(AppV2Theme.TEXT_SECONDARY);

        contenedor.add(superior, BorderLayout.NORTH);
        contenedor.add(crearKpis(), BorderLayout.CENTER);
        contenedor.add(lblEstado, BorderLayout.SOUTH);
        return contenedor;
    }

    private JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_SMALL));
        lbl.setForeground(AppV2Theme.TEXT_SECONDARY);
        return lbl;
    }

    private JPanel crearKpis() {
        JPanel metricas = new AppV2ResponsiveGridPanel(190, 5, 12, 10);
        metricas.add(cardActivos);
        metricas.add(cardVencidos);
        metricas.add(cardPorVencer);
        metricas.add(cardIngresados);
        metricas.add(cardCerrados);
        return metricas;
    }

    private Component crearCentro() {
        AppV2ResponsiveGridPanel grid = new AppV2ResponsiveGridPanel(440, 2, 16, 16);
        grid.add(panelGraficoEtapas);
        grid.add(panelGraficoResultadoAnalisis);
        grid.add(panelGraficoCargaAbogados);
        grid.add(panelGraficoTendencia);
        grid.add(panelGraficoNotificacion);
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private void inicializarFechas() {
        fechaDesde.setDate(DateRangePickerSupport.defaultSearchFromDateCurrentMonth());
        fechaHasta.setDate(DateRangePickerSupport.defaultSearchToDate());
    }

    private LocalDate fechaSeleccionada(PremiumDateFieldV2 campo, LocalDate porDefecto) {
        if (campo.getDate() == null) {
            return porDefecto;
        }
        return campo.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void refrescar() {
        final LocalDate desde = fechaSeleccionada(fechaDesde, LocalDate.now().withDayOfMonth(1));
        final LocalDate hasta = fechaSeleccionada(fechaHasta, LocalDate.now());
        btnRefrescar.setEnabled(false);
        lblEstado.setText("Cargando indicadores...");
        SwingWorker<DashboardDatos, Void> worker = new SwingWorker<DashboardDatos, Void>() {
            @Override
            protected DashboardDatos doInBackground() throws Exception {
                DashboardDatos datos = new DashboardDatos();
                datos.resumen = dashboardService.obtenerResumen(desde, hasta);
                datos.porEtapa = dashboardService.listarExpedientesPorEtapa();
                datos.resultadosAnalisis = dashboardService.listarResultadosAnalisis(desde, hasta);
                datos.cargaAbogados = dashboardService.listarCargaTopAbogados();
                datos.tendenciaMensual = dashboardService.listarTendenciaMensual(desde, hasta);
                datos.estadoNotificacion = dashboardService.listarEstadoFinalNotificacion();
                return datos;
            }

            @Override
            protected void done() {
                btnRefrescar.setEnabled(true);
                try {
                    DashboardDatos datos = get();
                    actualizarKpis(datos.resumen);
                    mostrarGrafico(panelGraficoEtapas, crearGraficoBarras(datos.porEtapa, "Documentos", false));
                    mostrarGrafico(panelGraficoResultadoAnalisis, crearGraficoTorta(datos.resultadosAnalisis));
                    mostrarGrafico(panelGraficoCargaAbogados, crearGraficoCargaAbogados(datos.cargaAbogados));
                    mostrarGrafico(panelGraficoTendencia, crearGraficoTendencia(datos.tendenciaMensual));
                    mostrarGrafico(panelGraficoNotificacion, crearGraficoTorta(datos.estadoNotificacion));
                    lblEstado.setText("Actualizado con el rango " + formatoFecha(desde) + " - " + formatoFecha(hasta) + ".");
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    lblEstado.setText("No se pudieron cargar los indicadores del Dashboard.");
                    JOptionPane.showMessageDialog(
                            JPanelDashboardV2.this,
                            "No se pudieron cargar los indicadores.\n\n" + causa.getMessage(),
                            "Dashboard", JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private String formatoFecha(LocalDate fecha) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy").format(fecha);
    }

    private void actualizarKpis(DashboardResumenDTO resumen) {
        if (resumen == null) {
            return;
        }
        cardActivos.setValue(String.valueOf(resumen.getActivos()));
        cardVencidos.setValue(String.valueOf(resumen.getVencidos()));
        cardPorVencer.setValue(String.valueOf(resumen.getPorVencer()));
        cardIngresados.setValue(String.valueOf(resumen.getIngresadosPeriodo()));
        cardCerrados.setValue(String.valueOf(resumen.getCerradosPeriodo()));
    }

    private static JPanel crearContenedorGrafico(String titulo) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(AppV2Theme.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppV2Theme.BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        panel.setPreferredSize(new Dimension(440, 300));
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(AppV2Theme.fontBold(AppV2Theme.FONT_SIZE_MEDIUM));
        lbl.setForeground(AppV2Theme.TEXT_PRIMARY);
        panel.add(lbl, BorderLayout.NORTH);
        return panel;
    }

    private void mostrarGrafico(JPanel contenedor, JFreeChart chart) {
        for (Component c : contenedor.getComponents()) {
            if (c instanceof ChartPanel) {
                contenedor.remove(c);
            }
        }
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 240));
        chartPanel.setOpaque(false);
        chartPanel.setBackground(AppV2Theme.SURFACE);
        contenedor.add(chartPanel, BorderLayout.CENTER);
        contenedor.revalidate();
        contenedor.repaint();
    }

    /** Gráfico de barras genérico (categoría/total), usado por "Expedientes por etapa". */
    private JFreeChart crearGraficoBarras(List<DashboardConteoDTO> datos, String etiquetaSerie, boolean horizontal) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (DashboardConteoDTO item : datos) {
            dataset.addValue(item.getTotal(), etiquetaSerie, item.getEtiqueta());
        }
        JFreeChart chart = ChartFactory.createBarChart(
                null, null, null, dataset,
                horizontal ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL,
                false, true, false);
        estilizarChartBase(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, AppV2Theme.PRIMARY);
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        return chart;
    }

    /** Gráfico tipo dona/torta genérico, usado por "Resultado de análisis" y "Estado de notificación". */
    private JFreeChart crearGraficoTorta(List<DashboardConteoDTO> datos) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<String>();
        for (DashboardConteoDTO item : datos) {
            if (item.getTotal() > 0) {
                dataset.setValue(item.getEtiqueta() + " (" + item.getTotal() + ")", item.getTotal());
            }
        }
        JFreeChart chart = ChartFactory.createPieChart(null, dataset, true, true, false);
        chart.setBackgroundPaint(AppV2Theme.SURFACE);
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(AppV2Theme.SURFACE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setLabelGenerator(null);
        plot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator("{0}"));
        int i = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((String) key, PALETA[i % PALETA.length]);
            i++;
        }
        return chart;
    }

    private JFreeChart crearGraficoCargaAbogados(List<CargaLaboralAbogadoDTO> cargas) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (CargaLaboralAbogadoDTO carga : cargas) {
            dataset.addValue(carga.getCargaTotal(), "Carga total", carga.getAbogado());
        }
        JFreeChart chart = ChartFactory.createBarChart(
                null, null, null, dataset, PlotOrientation.HORIZONTAL, false, true, false);
        estilizarChartBase(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, AppV2Theme.TEAL);
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        return chart;
    }

    private JFreeChart crearGraficoTendencia(List<DashboardTendenciaMensualDTO> tendencia) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (DashboardTendenciaMensualDTO item : tendencia) {
            String mes = capitalizar(FORMATO_MES.format(item.getMes()));
            dataset.addValue(item.getIngresados(), "Ingresados", mes);
            dataset.addValue(item.getCerrados(), "Cerrados", mes);
        }
        JFreeChart chart = ChartFactory.createLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, true, true, false);
        estilizarChartBase(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, AppV2Theme.PRIMARY);
        renderer.setSeriesPaint(1, AppV2Theme.SUCCESS);
        renderer.setSeriesStroke(0, new BasicStroke(2.4f));
        renderer.setSeriesStroke(1, new BasicStroke(2.4f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesVisible(1, true);
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        return chart;
    }

    private static String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    /** Look flat/premium consistente en todos los gráficos: sin sombras, colores del tema. */
    private void estilizarChartBase(JFreeChart chart) {
        chart.setBackgroundPaint(AppV2Theme.SURFACE);
        chart.removeLegend();
        Font fontEjes = AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_SMALL);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(AppV2Theme.SURFACE_ALT);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(AppV2Theme.BORDER);
        plot.getDomainAxis().setTickLabelFont(fontEjes);
        plot.getDomainAxis().setLabelFont(fontEjes);
        plot.getRangeAxis().setTickLabelFont(fontEjes);
        plot.getRangeAxis().setLabelFont(fontEjes);
    }

    private static final class DashboardDatos {
        private DashboardResumenDTO resumen;
        private List<DashboardConteoDTO> porEtapa;
        private List<DashboardConteoDTO> resultadosAnalisis;
        private List<CargaLaboralAbogadoDTO> cargaAbogados;
        private List<DashboardTendenciaMensualDTO> tendenciaMensual;
        private List<DashboardConteoDTO> estadoNotificacion;
    }
}
