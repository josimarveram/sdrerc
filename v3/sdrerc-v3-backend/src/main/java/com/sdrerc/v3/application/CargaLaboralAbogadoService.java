package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.CargaLaboralAbogadoDTO;
import com.sdrerc.v3.domain.CargaLaboralDocumentoDTO;
import com.sdrerc.v3.infrastructure.dao.CargaLaboralAbogadoDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Capa fina sobre {@link CargaLaboralAbogadoDAO} para la sub-pestaña "Carga Abogados" de
 * Asignación (en V2 el panel llama directo a {@code UsuarioAsignacionService}; en V3 se mantiene
 * el mismo patrón Controller→Service→DAO del resto del backend).
 */
@Service
public class CargaLaboralAbogadoService {

    private final CargaLaboralAbogadoDAO cargaLaboralAbogadoDAO;

    public CargaLaboralAbogadoService(CargaLaboralAbogadoDAO cargaLaboralAbogadoDAO) {
        this.cargaLaboralAbogadoDAO = cargaLaboralAbogadoDAO;
    }

    public List<CargaLaboralAbogadoDTO> listarCargaLaboralAbogados(Long idEquipo) throws SQLException {
        return cargaLaboralAbogadoDAO.listarCargaLaboralAbogados(idEquipo);
    }

    public List<CargaLaboralDocumentoDTO> listarDocumentosPorAbogado(Long idUsuario) throws SQLException {
        return cargaLaboralAbogadoDAO.listarDocumentosPorAbogado(idUsuario);
    }

    public Set<Long> listarIdsUsuarioConVencimientoEnRango(LocalDate desde, LocalDate hasta) throws SQLException {
        return cargaLaboralAbogadoDAO.listarIdsUsuarioConVencimientoEnRango(desde, hasta);
    }
}
