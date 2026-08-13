package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.PlantillaDocumentoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.PlantillaDocumentoDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class PlantillaDocumentoService {

    private static final long TAMANO_MAXIMO_BYTES = 15L * 1024 * 1024;

    private final PlantillaDocumentoDAO plantillaDocumentoDAO;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public PlantillaDocumentoService() {
        this(new PlantillaDocumentoDAO(), new UsuarioAsignacionService());
    }

    public PlantillaDocumentoService(PlantillaDocumentoDAO plantillaDocumentoDAO, UsuarioAsignacionService usuarioAsignacionService) {
        this.plantillaDocumentoDAO = plantillaDocumentoDAO;
        this.usuarioAsignacionService = usuarioAsignacionService;
    }

    public List<PlantillaDocumentoDTO> listarTiposConPlantilla() throws SQLException {
        return plantillaDocumentoDAO.listarTiposConPlantillaAnalisis();
    }

    public List<PlantillaDocumentoDTO> listarHistorial(Long idTipoDocumentoAdjunto) throws SQLException {
        if (idTipoDocumentoAdjunto == null) {
            throw new IllegalArgumentException("Seleccione un tipo de documento.");
        }
        return plantillaDocumentoDAO.listarHistorial(idTipoDocumentoAdjunto);
    }

    public PlantillaDocumentoDAO.ContenidoPlantilla descargarContenido(Long idPlantillaDocumento) throws SQLException {
        if (idPlantillaDocumento == null) {
            throw new IllegalArgumentException("Seleccione una versión de plantilla.");
        }
        return plantillaDocumentoDAO.obtenerContenido(idPlantillaDocumento);
    }

    public Long cargarNuevaVersion(
            Long idTipoDocumentoAdjunto, String nombreArchivo, byte[] contenido, String comentario) throws SQLException {
        if (idTipoDocumentoAdjunto == null) {
            throw new IllegalArgumentException("Seleccione el tipo de documento para el que se carga la plantilla.");
        }
        validarArchivo(nombreArchivo, contenido);
        return plantillaDocumentoDAO.insertarVersion(
                idTipoDocumentoAdjunto, nombreArchivo, contenido, comentario, resolverUsuarioActualSdrercApp());
    }

    public void activarVersion(Long idPlantillaDocumento) throws SQLException {
        if (idPlantillaDocumento == null) {
            throw new IllegalArgumentException("Seleccione la versión de plantilla a activar.");
        }
        plantillaDocumentoDAO.activarVersion(idPlantillaDocumento, resolverUsuarioActualSdrercApp());
    }

    private void validarArchivo(String nombreArchivo, byte[] contenido) {
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El archivo de plantilla debe tener un nombre.");
        }
        if (!nombreArchivo.toLowerCase(Locale.ROOT).endsWith(".docx")) {
            throw new IllegalArgumentException("Solo se admiten plantillas en formato .docx.");
        }
        if (contenido == null || contenido.length == 0) {
            throw new IllegalArgumentException("El archivo de plantilla está vacío.");
        }
        if (contenido.length > TAMANO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("El archivo de plantilla supera el tamaño máximo permitido (15 MB).");
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
