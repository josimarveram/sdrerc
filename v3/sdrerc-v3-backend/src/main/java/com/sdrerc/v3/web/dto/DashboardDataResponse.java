package com.sdrerc.v3.web.dto;

import com.sdrerc.v3.domain.CargaLaboralAbogadoDTO;
import com.sdrerc.v3.domain.DashboardConteoDTO;
import com.sdrerc.v3.domain.DashboardResumenDTO;
import com.sdrerc.v3.domain.DashboardTendenciaMensualDTO;
import java.util.List;

/**
 * Combina en una sola respuesta lo que JPanelDashboardV2 (V2) carga en un unico
 * SwingWorker al abrir/refrescar el panel — un solo request en vez de 6.
 */
public record DashboardDataResponse(
        DashboardResumenDTO resumen,
        List<DashboardConteoDTO> porEtapa,
        List<DashboardConteoDTO> resultadosAnalisis,
        List<CargaLaboralAbogadoDTO> cargaAbogados,
        List<DashboardTendenciaMensualDTO> tendenciaMensual,
        List<DashboardConteoDTO> estadoNotificacion) {
}
