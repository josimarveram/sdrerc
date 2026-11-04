package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.PlazoConfiguracionDTO;
import com.sdrerc.v3.infrastructure.dao.PlazoAdminDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Port de {@code com.sdrerc.application.sdrercapp.PlazoConfiguracionService} (V2), Administración
 * &gt; Plazos. Reglas de negocio portadas literales: código único entre configuraciones activas,
 * días de plazo &gt; 0, unidad válida (HABILES/CALENDARIO), vigencia hasta no anterior a vigencia
 * desde. Mejora explícita de V3: {@link #validar} ahora también acepta (sin exigir) los 3 umbrales
 * de color — ver Javadoc de {@code AdminPlazosPage.tsx} para el porqué (V2 nunca los exponía en su
 * formulario, quedaban siempre en 51/21/0 por defecto sin forma de ajustarlos).
 */
@Service
public class PlazoAdminService {

    private final PlazoAdminDAO plazoAdminDAO;

    public PlazoAdminService(PlazoAdminDAO plazoAdminDAO) {
        this.plazoAdminDAO = plazoAdminDAO;
    }

    public List<PlazoConfiguracionDTO> buscar(String textoLibre, Boolean activo, int limite) throws SQLException {
        return plazoAdminDAO.buscar(textoLibre, activo, limite);
    }

    public PlazoConfiguracionDTO obtenerPlazoSolicitud() throws SQLException {
        return plazoAdminDAO.obtenerPlazoSolicitud();
    }

    public PlazoConfiguracionDTO guardar(PlazoConfiguracionDTO plazo) throws SQLException {
        validar(plazo);
        try (Connection conn = plazoAdminDAO.abrirConexion()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                if (plazo.isActivo() && plazoAdminDAO.existeCodigoActivo(conn, plazo.getCodigo(), plazo.getIdPlazoConfiguracion())) {
                    throw new IllegalArgumentException("Ya existe una configuración activa para el código indicado.");
                }
                Long idUsuario = resolverIdUsuarioActual();
                PlazoConfiguracionDTO resultado = plazo.getIdPlazoConfiguracion() == null
                        ? plazoAdminDAO.insertar(conn, plazo, idUsuario)
                        : plazoAdminDAO.actualizar(conn, plazo, idUsuario);
                conn.commit();
                return resultado;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException sqlEx) {
                    throw sqlEx;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public PlazoConfiguracionDTO cambiarActivo(Long idPlazoConfiguracion, boolean activo) throws SQLException {
        if (idPlazoConfiguracion == null) {
            throw new IllegalArgumentException("Seleccione una configuración de plazo.");
        }
        try (Connection conn = plazoAdminDAO.abrirConexion()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                PlazoConfiguracionDTO actual = plazoAdminDAO.obtenerPorId(conn, idPlazoConfiguracion);
                if (actual == null) {
                    throw new IllegalArgumentException("No se encontró la configuración seleccionada.");
                }
                if (activo && plazoAdminDAO.existeCodigoActivo(conn, actual.getCodigo(), idPlazoConfiguracion)) {
                    throw new IllegalArgumentException("Ya existe una configuración activa para el código indicado.");
                }
                PlazoConfiguracionDTO resultado = plazoAdminDAO.cambiarActivo(conn, idPlazoConfiguracion, activo, resolverIdUsuarioActual());
                conn.commit();
                return resultado;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException sqlEx) {
                    throw sqlEx;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private void validar(PlazoConfiguracionDTO plazo) {
        if (plazo == null) {
            throw new IllegalArgumentException("Complete los datos de la configuración.");
        }
        if (!hasText(plazo.getCodigo())) {
            throw new IllegalArgumentException("Ingrese el código del plazo.");
        }
        if (plazo.getCodigo().length() > 80) {
            throw new IllegalArgumentException("El código no debe exceder 80 caracteres.");
        }
        if (!hasText(plazo.getNombre())) {
            throw new IllegalArgumentException("Ingrese el nombre de la configuración.");
        }
        if (plazo.getNombre().length() > 180) {
            throw new IllegalArgumentException("El nombre no debe exceder 180 caracteres.");
        }
        if (!hasText(plazo.getAmbito())) {
            plazo.setAmbito(plazo.getCodigo());
        }
        if (plazo.getDiasPlazo() == null || plazo.getDiasPlazo() <= 0) {
            throw new IllegalArgumentException("Los días del plazo deben ser mayores a cero.");
        }
        if (!PlazoConfiguracionDTO.UNIDAD_HABILES.equals(plazo.getUnidadPlazo())
                && !PlazoConfiguracionDTO.UNIDAD_CALENDARIO.equals(plazo.getUnidadPlazo())) {
            throw new IllegalArgumentException("Seleccione una unidad de plazo válida.");
        }
        LocalDate desde = plazo.getFechaVigenciaDesde();
        LocalDate hasta = plazo.getFechaVigenciaHasta();
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha de vigencia hasta no puede ser anterior a la fecha desde.");
        }
        if (plazo.getObservacion() != null && plazo.getObservacion().trim().length() > 300) {
            throw new IllegalArgumentException("La observación no debe exceder 300 caracteres.");
        }
        validarUmbrales(plazo);
    }

    /**
     * Mejora explícita de V3: valida coherencia de los 3 umbrales de color (0-100, verde &gt;
     * amarillo &gt; rojo) cuando el usuario los ajusta desde la UI — V2 nunca los exponía, así que
     * nunca tuvo esta validación tampoco.
     */
    private void validarUmbrales(PlazoConfiguracionDTO plazo) {
        Integer verde = plazo.getPorcentajeVerdeDesde();
        Integer amarillo = plazo.getPorcentajeAmarilloDesde();
        Integer rojo = plazo.getPorcentajeRojoDesde();
        if (verde == null || amarillo == null || rojo == null) {
            return;
        }
        if (verde < 0 || verde > 100 || amarillo < 0 || amarillo > 100 || rojo < 0 || rojo > 100) {
            throw new IllegalArgumentException("Los umbrales de color deben estar entre 0 y 100.");
        }
        if (!(verde > amarillo && amarillo > rojo)) {
            throw new IllegalArgumentException("Los umbrales deben cumplir: verde > amarillo > rojo.");
        }
    }

    private Long resolverIdUsuarioActual() {
        return CurrentUser.get().idUsuario();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
