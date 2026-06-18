package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.FeriadoNacionalDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import com.sdrerc.infrastructure.sdrercapp.dao.FeriadoNacionalDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class FeriadoNacionalService {

    private final FeriadoNacionalDAO feriadoNacionalDAO;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public FeriadoNacionalService() {
        this(new FeriadoNacionalDAO(), new UsuarioAsignacionService());
    }

    public FeriadoNacionalService(FeriadoNacionalDAO feriadoNacionalDAO, UsuarioAsignacionService usuarioAsignacionService) {
        this.feriadoNacionalDAO = feriadoNacionalDAO;
        this.usuarioAsignacionService = usuarioAsignacionService;
    }

    public List<FeriadoNacionalDTO> buscar(Integer anio, Boolean activo, int limite) throws SQLException {
        return feriadoNacionalDAO.buscar(anio, activo, limite);
    }

    public FeriadoNacionalDTO guardar(FeriadoNacionalDTO feriado) throws SQLException {
        validarFeriado(feriado);
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idUsuario = resolverUsuarioActualSdrercApp();
                if (feriadoNacionalDAO.existeFecha(conn, feriado.getFecha(), feriado.getTipo(), feriado.getIdFeriado())) {
                    throw new IllegalArgumentException("Ya existe un feriado registrado para la fecha y tipo indicados.");
                }
                FeriadoNacionalDTO resultado = feriado.getIdFeriado() == null
                        ? feriadoNacionalDAO.insertar(conn, feriado, idUsuario)
                        : feriadoNacionalDAO.actualizar(conn, feriado, idUsuario);
                conn.commit();
                return resultado;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public FeriadoNacionalDTO cambiarActivo(Long idFeriado, boolean activo) throws SQLException {
        if (idFeriado == null) {
            throw new IllegalArgumentException("Seleccione un feriado.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                FeriadoNacionalDTO resultado = feriadoNacionalDAO.cambiarActivo(
                        conn,
                        idFeriado,
                        activo,
                        resolverUsuarioActualSdrercApp());
                conn.commit();
                return resultado;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private void validarFeriado(FeriadoNacionalDTO feriado) {
        if (feriado == null) {
            throw new IllegalArgumentException("Complete los datos del feriado.");
        }
        if (feriado.getFecha() == null) {
            throw new IllegalArgumentException("Ingrese la fecha del feriado.");
        }
        if (feriado.getFecha().isBefore(LocalDate.of(2000, 1, 1))) {
            throw new IllegalArgumentException("Ingrese una fecha válida para el feriado.");
        }
        if (feriado.getNombre() == null || feriado.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese el nombre del feriado.");
        }
        if (feriado.getNombre().trim().length() > 150) {
            throw new IllegalArgumentException("El nombre del feriado no debe exceder 150 caracteres.");
        }
        if (feriado.getObservacion() != null && feriado.getObservacion().trim().length() > 300) {
            throw new IllegalArgumentException("La observación no debe exceder 300 caracteres.");
        }
        feriado.setNombre(feriado.getNombre());
        feriado.setTipo(feriado.getTipo());
        feriado.setObservacion(feriado.getObservacion());
    }

    private Long resolverUsuarioActualSdrercApp() {
        try {
            String username = SessionContext.getUsername();
            return usuarioAsignacionService.obtenerIdUsuarioActivoPorUsername(username);
        } catch (Exception ex) {
            return null;
        }
    }
}
