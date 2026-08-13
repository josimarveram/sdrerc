package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.PlantillaBloqueDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.PlantillaBloqueDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.List;

public class PlantillaBloqueService {

    private final PlantillaBloqueDAO plantillaBloqueDAO;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public PlantillaBloqueService() {
        this(new PlantillaBloqueDAO(), new UsuarioAsignacionService());
    }

    public PlantillaBloqueService(PlantillaBloqueDAO plantillaBloqueDAO, UsuarioAsignacionService usuarioAsignacionService) {
        this.plantillaBloqueDAO = plantillaBloqueDAO;
        this.usuarioAsignacionService = usuarioAsignacionService;
    }

    public List<PlantillaBloqueDTO> listarPorTipo(Long idTipoDocumentoAdjunto) throws SQLException {
        if (idTipoDocumentoAdjunto == null) {
            throw new IllegalArgumentException("Seleccione un tipo de documento.");
        }
        return plantillaBloqueDAO.listarPorTipo(idTipoDocumentoAdjunto);
    }

    public Long guardar(PlantillaBloqueDTO bloque) throws SQLException {
        validar(bloque);
        Long idUsuario = resolverUsuarioActualSdrercApp();
        if (bloque.getIdPlantillaBloque() == null) {
            return plantillaBloqueDAO.insertar(bloque, idUsuario);
        }
        plantillaBloqueDAO.actualizar(bloque, idUsuario);
        return bloque.getIdPlantillaBloque();
    }

    public void eliminar(Long idPlantillaBloque) throws SQLException {
        if (idPlantillaBloque == null) {
            throw new IllegalArgumentException("Seleccione el bloque a eliminar.");
        }
        plantillaBloqueDAO.eliminar(idPlantillaBloque, resolverUsuarioActualSdrercApp());
    }

    public void guardarOrden(List<Long> idsOrdenados) throws SQLException {
        if (idsOrdenados == null || idsOrdenados.isEmpty()) {
            return;
        }
        plantillaBloqueDAO.guardarOrden(idsOrdenados, resolverUsuarioActualSdrercApp());
    }

    private void validar(PlantillaBloqueDTO bloque) {
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
                && !PlantillaBloqueDTO.OPERADOR_COINCIDE.equals(bloque.getOperadorCondicion())
                && !PlantillaBloqueDTO.OPERADOR_NO_COINCIDE.equals(bloque.getOperadorCondicion())) {
            throw new IllegalArgumentException("Seleccione un operador de condición válido.");
        }
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
