package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.AsignacionCartaRespuestaDTO;
import com.sdrerc.v3.infrastructure.dao.DocumentoAnalisisDAO;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Port acotado de la porción de com.sdrerc.application.sdrercapp (V2) que resuelve la bandeja
 * "Cartas de respuesta" de Asignación (en V2 no hay un Service dedicado: el panel llama
 * directamente a {@code DocumentoAnalisisDAO}; en V3 se agrega esta capa fina para mantener el
 * mismo patrón Controller→Service→DAO del resto del backend).
 */
@Service
public class CartaRespuestaService {

    private final DocumentoAnalisisDAO documentoAnalisisDAO;

    public CartaRespuestaService(DocumentoAnalisisDAO documentoAnalisisDAO) {
        this.documentoAnalisisDAO = documentoAnalisisDAO;
    }

    public List<AsignacionCartaRespuestaDTO> listarCartasRespuestaPendientes() throws SQLException {
        return documentoAnalisisDAO.listarCartasRespuestaPendientes();
    }
}
