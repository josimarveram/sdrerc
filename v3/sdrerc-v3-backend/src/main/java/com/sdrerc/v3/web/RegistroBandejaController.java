package com.sdrerc.v3.web;

import com.sdrerc.v3.application.ExpedienteConsultaService;
import com.sdrerc.v3.application.ExpedienteEdicionManualService;
import com.sdrerc.v3.domain.ExpedienteBandejaDTO;
import com.sdrerc.v3.domain.ExpedienteEdicionManualDTO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Equivalente web de la pestaña "Bandeja Registro" (JPanelRegistroRecepcionV2 + la instancia de
 * JPanelBandejaExpedientesNueva configurada con etapa "REGISTRO" en V2). Etapa fija a REGISTRO
 * (no expuesta como filtro, igual que V2: "no mostrar filtro de etapa" en esta bandeja). Rango de
 * fechas por defecto: día 1 del mes de hace 5 meses -> hoy (regla general de CLAUDE.md para
 * bandejas operativas, salvo la Bandeja General de Expedientes).
 *
 * <p>Alcance de este incremento: solo el listado + filtros + datos base. Los KPIs
 * ("Potencial duplicado"/"Posible Grupo Familiar"/"Grupo Familiar Confirmado") se calculan en el
 * frontend sobre esta misma lista, igual que V2 (ver JPanelBandejaExpedientesNueva.
 * actualizarMetricasAlertasRegistro/filtrarPorAlertaRegistro — filtro en memoria, no una consulta
 * aparte). La expansión de expedientes asociados/duplicados y el panel "Asociar" quedan para un
 * incremento posterior.</p>
 */
@RestController
@RequestMapping("/api/registro/bandeja")
public class RegistroBandejaController {

    private static final String ETAPA_REGISTRO = "REGISTRO";

    private final ExpedienteConsultaService expedienteConsultaService;
    private final ExpedienteEdicionManualService expedienteEdicionManualService;

    public RegistroBandejaController(
            ExpedienteConsultaService expedienteConsultaService,
            ExpedienteEdicionManualService expedienteEdicionManualService) {
        this.expedienteConsultaService = expedienteConsultaService;
        this.expedienteEdicionManualService = expedienteEdicionManualService;
    }

    @GetMapping
    public List<ExpedienteBandejaDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false, defaultValue = "200") int limite) throws SQLException {
        LocalDate hastaEfectiva = hasta != null ? hasta : LocalDate.now();
        LocalDate desdeEfectiva = desde != null ? desde : hastaEfectiva.minusMonths(5).withDayOfMonth(1);
        return expedienteConsultaService.buscarBandeja(texto, ETAPA_REGISTRO, estado, desdeEfectiva, hastaEfectiva, limite);
    }

    /**
     * Equivalente web del "Panel de datos" de V2 (doble clic en una fila): solo lectura, sin
     * exigir que el expediente sea editable, informativo, sin botones (ver `CLAUDE.md` raíz,
     * "Panel derecho Registro"). Se abre como pestaña en el drawer de pestañas del frontend.
     */
    @GetMapping("/{idExpediente}/datos")
    public ExpedienteEdicionManualDTO obtenerDatos(@PathVariable Long idExpediente) throws SQLException {
        return expedienteEdicionManualService.obtenerDatos(idExpediente);
    }
}
