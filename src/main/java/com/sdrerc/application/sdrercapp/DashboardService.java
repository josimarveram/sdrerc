package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.CargaLaboralAbogadoDTO;
import com.sdrerc.domain.dto.sdrercapp.DashboardConteoDTO;
import com.sdrerc.domain.dto.sdrercapp.DashboardResumenDTO;
import com.sdrerc.domain.dto.sdrercapp.DashboardTendenciaMensualDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.DashboardDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Modulo Dashboard (reportes gerenciales): exclusivo ADMIN_SISTEMA. No aplica filtro de
 * visibilidad por asignacion (VisibilidadBandejaSql); siempre agrega sobre el universo
 * completo de expedientes, a diferencia de las bandejas operativas.
 */
public class DashboardService {

    private static final int TOP_ABOGADOS = 10;

    private final DashboardDAO dashboardDAO;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public DashboardService() {
        this(new DashboardDAO(), new UsuarioAsignacionService());
    }

    public DashboardService(DashboardDAO dashboardDAO, UsuarioAsignacionService usuarioAsignacionService) {
        this.dashboardDAO = dashboardDAO;
        this.usuarioAsignacionService = usuarioAsignacionService;
    }

    public boolean tieneAcceso() {
        return SessionContext.hasRole("ADMIN_SISTEMA");
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
        List<CargaLaboralAbogadoDTO> cargas = new ArrayList<CargaLaboralAbogadoDTO>(
                usuarioAsignacionService.listarCargaLaboralAbogados(null));
        Collections.sort(cargas, new Comparator<CargaLaboralAbogadoDTO>() {
            @Override
            public int compare(CargaLaboralAbogadoDTO a, CargaLaboralAbogadoDTO b) {
                return Integer.compare(b.getCargaTotal(), a.getCargaTotal());
            }
        });
        return cargas.size() > TOP_ABOGADOS ? cargas.subList(0, TOP_ABOGADOS) : cargas;
    }

    private void requerirAcceso() {
        if (!tieneAcceso()) {
            throw new IllegalStateException("El Dashboard es exclusivo para administradores del sistema.");
        }
    }
}
