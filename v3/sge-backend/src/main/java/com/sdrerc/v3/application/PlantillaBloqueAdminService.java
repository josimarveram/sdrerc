package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.PlantillaBloqueAdminDTO;
import com.sdrerc.v3.infrastructure.dao.PlantillaBloqueAdminDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

/** Port literal de {@code com.sdrerc.application.sdrercapp.PlantillaBloqueService} (V2). */
@Service
public class PlantillaBloqueAdminService {

    private final PlantillaBloqueAdminDAO plantillaBloqueAdminDAO;

    public PlantillaBloqueAdminService(PlantillaBloqueAdminDAO plantillaBloqueAdminDAO) {
        this.plantillaBloqueAdminDAO = plantillaBloqueAdminDAO;
    }

    public List<PlantillaBloqueAdminDTO> listarPorTipo(Long idTipoDocumentoAdjunto) throws SQLException {
        if (idTipoDocumentoAdjunto == null) {
            throw new IllegalArgumentException("Seleccione un tipo de documento.");
        }
        return plantillaBloqueAdminDAO.listarPorTipo(idTipoDocumentoAdjunto);
    }

    public Long guardar(PlantillaBloqueAdminDTO bloque) throws SQLException {
        validar(bloque);
        Long idUsuario = resolverIdUsuarioActual();
        if (bloque.getIdPlantillaBloque() == null) {
            return plantillaBloqueAdminDAO.insertar(bloque, idUsuario);
        }
        plantillaBloqueAdminDAO.actualizar(bloque, idUsuario);
        return bloque.getIdPlantillaBloque();
    }

    public void eliminar(Long idPlantillaBloque) throws SQLException {
        if (idPlantillaBloque == null) {
            throw new IllegalArgumentException("Seleccione el bloque a eliminar.");
        }
        plantillaBloqueAdminDAO.eliminar(idPlantillaBloque, resolverIdUsuarioActual());
    }

    public void guardarOrden(List<Long> idsOrdenados) throws SQLException {
        if (idsOrdenados == null || idsOrdenados.isEmpty()) {
            return;
        }
        plantillaBloqueAdminDAO.guardarOrden(idsOrdenados, resolverIdUsuarioActual());
    }

    private void validar(PlantillaBloqueAdminDTO bloque) {
        if (bloque.getIdTipoDocumentoAdjunto() == null) {
            throw new IllegalArgumentException("El bloque debe pertenecer a un tipo de documento.");
        }
        if (bloque.getContenido() == null || bloque.getContenido().trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido del bloque no puede estar vacío.");
        }
        boolean tieneVariable = bloque.getVariableCondicion() != null && !bloque.getVariableCondicion().trim().isEmpty();
        boolean tieneValores = bloque.getValoresCondicion() != null && !bloque.getValoresCondicion().trim().isEmpty();
        if (tieneVariable != tieneValores) {
            throw new IllegalArgumentException("Para condicionar el bloque, complete la variable, el operador y los valores esperados.");
        }
        if (tieneVariable
                && !PlantillaBloqueAdminDTO.OPERADOR_COINCIDE.equals(bloque.getOperadorCondicion())
                && !PlantillaBloqueAdminDTO.OPERADOR_NO_COINCIDE.equals(bloque.getOperadorCondicion())) {
            throw new IllegalArgumentException("Seleccione un operador de condición válido.");
        }
    }

    private Long resolverIdUsuarioActual() {
        return CurrentUser.get().idUsuario();
    }
}
