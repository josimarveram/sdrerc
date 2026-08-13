package com.sdrerc.v3.web;

import com.sdrerc.v3.application.DashboardService;
import com.sdrerc.v3.web.dto.DashboardDataResponse;
import java.sql.SQLException;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Equivalente web de JPanelDashboardV2 (V2): mismo rango de fechas por defecto (dia 1 del mes
 * actual -> hoy, ver DateRangePickerSupport.defaultSearchFromDateCurrentMonth/defaultSearchToDate
 * en V2) y misma composicion de datos (resumen + 5 series para los 5 graficos). Protegido por
 * DashboardService.tieneAcceso() (ADMIN_SISTEMA); no requiere anotacion de seguridad adicional
 * aqui porque /api/dashboard/** ya exige JWT valido via SecurityConfig (anyRequest().authenticated()).
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardDataResponse obtenerDatos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta)
            throws SQLException {
        LocalDate hastaEfectiva = hasta != null ? hasta : LocalDate.now();
        LocalDate desdeEfectiva = desde != null ? desde : hastaEfectiva.withDayOfMonth(1);

        return new DashboardDataResponse(
                dashboardService.obtenerResumen(desdeEfectiva, hastaEfectiva),
                dashboardService.listarExpedientesPorEtapa(),
                dashboardService.listarResultadosAnalisis(desdeEfectiva, hastaEfectiva),
                dashboardService.listarCargaTopAbogados(),
                dashboardService.listarTendenciaMensual(desdeEfectiva, hastaEfectiva),
                dashboardService.listarEstadoFinalNotificacion());
    }
}
