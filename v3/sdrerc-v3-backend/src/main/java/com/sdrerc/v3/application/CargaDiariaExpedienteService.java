package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.CargaDiariaPreviewDTO;
import com.sdrerc.v3.domain.CargaDiariaResultadoDTO;
import com.sdrerc.v3.infrastructure.dao.ExpedienteRegistroDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Orquesta el pipeline básico de Carga diaria (Registro): parsear el Excel/CSV subido, aplicar las
 * mismas reglas de validación que Registro manual (duplicados, grupo familiar, número preliminar)
 * y, en un segundo paso separado, confirmar el registro en SDRERC_APP. Sin edición de celdas en la
 * previsualización, sin exportar ni descargar plantilla (alcance acordado para este incremento;
 * ver v3/CLAUDE.md).
 */
@Service
public class CargaDiariaExpedienteService {

    private final CargaDiariaArchivoParserService parserService;
    private final CargaDiariaReglasService reglasService;
    private final CargaDiariaExportService exportService;
    private final CargaDiariaPlantillaService plantillaService;
    private final ExpedienteRegistroDAO expedienteRegistroDAO;

    public CargaDiariaExpedienteService(
            CargaDiariaArchivoParserService parserService,
            CargaDiariaReglasService reglasService,
            CargaDiariaExportService exportService,
            CargaDiariaPlantillaService plantillaService,
            ExpedienteRegistroDAO expedienteRegistroDAO) {
        this.parserService = parserService;
        this.reglasService = reglasService;
        this.exportService = exportService;
        this.plantillaService = plantillaService;
        this.expedienteRegistroDAO = expedienteRegistroDAO;
    }

    public List<CargaDiariaPreviewDTO> previsualizar(InputStream contenido, String nombreArchivo) throws IOException, SQLException {
        List<CargaDiariaPreviewDTO> registros = parserService.leerArchivo(contenido, nombreArchivo);
        return reglasService.validar(registros);
    }

    /**
     * Re-ejecuta las reglas de validación (duplicados, grupo familiar, número preliminar) sobre una
     * lista ya editada por el usuario en la previsualización — equivalente al botón "Validar" de V2
     * después de editar celdas (`JPanelCargaDiariaRecepcionV2.validar()`).
     */
    public List<CargaDiariaPreviewDTO> validar(List<CargaDiariaPreviewDTO> registros) throws SQLException {
        if (registros == null || registros.isEmpty()) {
            throw new IllegalArgumentException("No hay registros para validar. Seleccione un archivo nuevamente.");
        }
        return reglasService.validar(registros);
    }

    public CargaDiariaResultadoDTO confirmar(List<CargaDiariaPreviewDTO> registros) throws SQLException {
        if (registros == null || registros.isEmpty()) {
            throw new IllegalArgumentException("No hay registros para confirmar. Previsualice el archivo nuevamente.");
        }
        return expedienteRegistroDAO.registrarCarga(registros, CurrentUser.get().idUsuario());
    }

    public byte[] exportarExcel(List<CargaDiariaPreviewDTO> registros) throws IOException {
        if (registros == null || registros.isEmpty()) {
            throw new IllegalArgumentException("No hay registros para exportar.");
        }
        return exportService.exportarExcel(registros);
    }

    public byte[] generarPlantilla() throws IOException {
        return plantillaService.generarPlantilla();
    }
}
