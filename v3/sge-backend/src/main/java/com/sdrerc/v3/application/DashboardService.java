package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.CargaLaboralAbogadoDTO;
import com.sdrerc.v3.domain.DashboardConteoDTO;
import com.sdrerc.v3.domain.DashboardResumenDTO;
import com.sdrerc.v3.domain.DashboardTendenciaMensualDTO;
import com.sdrerc.v3.infrastructure.dao.CargaLaboralAbogadoDAO;
import com.sdrerc.v3.infrastructure.dao.DashboardDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Port casi literal de com.sdrerc.application.sdrercapp.DashboardService (V2): exclusivo
 * ADMIN_SISTEMA, no aplica visibilidad por asignacion (agrega sobre el universo completo).
 * Unico cambio real: {@code tieneAcceso()} lee el rol del {@link CurrentUser} resuelto por
 * request (JWT) en vez de {@code SessionContext.hasRole(...)} estatico.
 */
@Service
public class DashboardService {

    private static final int TOP_ABOGADOS = 10;
    private static final String ROL_ADMIN_SISTEMA = "ADMIN_SISTEMA";

    private final DashboardDAO dashboardDAO;
    private final CargaLaboralAbogadoDAO cargaLaboralAbogadoDAO;

    public DashboardService(DashboardDAO dashboardDAO, CargaLaboralAbogadoDAO cargaLaboralAbogadoDAO) {
        this.dashboardDAO = dashboardDAO;
        this.cargaLaboralAbogadoDAO = cargaLaboralAbogadoDAO;
    }

    public boolean tieneAcceso() {
        return CurrentUser.get().hasRole(ROL_ADMIN_SISTEMA);
    }

    public DashboardResumenDTO obtenerResumen(LocalDate desde, LocalDate hasta) throws SQLException {
        requerirAcceso();
        return dashboardDAO.obtenerResumen(desde, hasta);
    }

    public List<DashboardConteoDTO> listarExpedientesPorEtapa() throws SQLException {
        requerirAcceso();
        return dashboardDAO.listarExpedientesPorEtapa();
    }

    public List<DashboardConteoDTO> listarResultadosAnalisis(LocalDate desde, LocalDate hasta) throws SQLException {
        requerirAcceso();
        return dashboardDAO.listarResultadosAnalisis(desde, hasta);
    }

    public List<DashboardConteoDTO> listarEstadoFinalNotificacion() throws SQLException {
        requerirAcceso();
        return dashboardDAO.listarEstadoFinalNotificacion();
    }

    public List<DashboardTendenciaMensualDTO> listarTendenciaMensual(LocalDate desde, LocalDate hasta) throws SQLException {
        requerirAcceso();
        return dashboardDAO.listarTendenciaMensual(desde, hasta);
    }

    /** Top N abogados por carga total, reutilizando la misma consulta ya usada en Carga Abogados. */
    public List<CargaLaboralAbogadoDTO> listarCargaTopAbogados() throws SQLException {
        requerirAcceso();
        List<CargaLaboralAbogadoDTO> cargas = new ArrayList<>(cargaLaboralAbogadoDAO.listarCargaLaboralAbogados(null));
        cargas.sort(Comparator.comparingInt(CargaLaboralAbogadoDTO::cargaTotal).reversed());
        return cargas.size() > TOP_ABOGADOS ? cargas.subList(0, TOP_ABOGADOS) : cargas;
    }

    private void requerirAcceso() {
        if (!tieneAcceso()) {
            throw new IllegalStateException("El Dashboard es exclusivo para administradores del sistema.");
        }
    }
}
