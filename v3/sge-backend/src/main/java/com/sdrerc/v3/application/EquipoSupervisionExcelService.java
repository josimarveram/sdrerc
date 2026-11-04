package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.EquipoSupervisionImportItemDTO;
import com.sdrerc.v3.domain.EquipoSupervisionImportResultadoDTO;
import com.sdrerc.v3.domain.UsuarioAsignableEquipoDTO;
import com.sdrerc.v3.infrastructure.dao.UsuarioSupervisionAdminDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

/**
 * Plantilla Excel simplificada de "Personal por supervisor" (Administración &gt; Equipo Jurídico),
 * alcance reducido deliberado respecto a la plantilla legacy de V1
 * ({@code EquipoJuridicoImportService}/{@code EquipoJuridicoPlantillaService}, ~2570 líneas):
 * solo vincula abogado↔supervisor por {@code username} de usuarios YA EXISTENTES, sin crear
 * personas ni contraseñas, sin parseo ambiguo de nombres libres — ver Javadoc de
 * {@link EquipoSupervisionImportItemDTO} y la sección de Administración del plan aprobado.
 *
 * <p>Mismo patrón UX que V1 (previsualización con 3 niveles VALIDO/ADVERTENCIA/ERROR, confirmación
 * fila por fila con manejo de error individual, no todo-o-nada) pero sin la mecánica POI-vs-fallback
 * ni la generación de username/contraseña temporal de V1 — acá no se crea ningún usuario nuevo.</p>
 */
@Service
public class EquipoSupervisionExcelService {

    public static final String NOMBRE_ARCHIVO = "plantilla_personal_por_supervisor_sdrerc.xlsx";

    private static final String HOJA_DATOS = "PERSONAL_SUPERVISION";
    private static final String COL_ABOGADO = "USUARIO_ABOGADO";
    private static final String COL_SUPERVISOR = "USUARIO_SUPERVISOR";

    private final UsuarioSupervisionAdminDAO usuarioSupervisionAdminDAO;

    public EquipoSupervisionExcelService(UsuarioSupervisionAdminDAO usuarioSupervisionAdminDAO) {
        this.usuarioSupervisionAdminDAO = usuarioSupervisionAdminDAO;
    }

    public byte[] generarPlantilla() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(HOJA_DATOS);
            Font negrita = workbook.createFont();
            negrita.setBold(true);
            CellStyle estiloEncabezado = workbook.createCellStyle();
            estiloEncabezado.setFont(negrita);

            Row encabezado = sheet.createRow(0);
            Cell c0 = encabezado.createCell(0);
            c0.setCellValue(COL_ABOGADO);
            c0.setCellStyle(estiloEncabezado);
            Cell c1 = encabezado.createCell(1);
            c1.setCellValue(COL_SUPERVISOR);
            c1.setCellStyle(estiloEncabezado);
            sheet.setColumnWidth(0, 8000);
            sheet.setColumnWidth(1, 8000);

            Sheet instrucciones = workbook.createSheet("INSTRUCCIONES");
            String[] textos = {
                "Plantilla de Personal por supervisor (SDRERC).",
                "Complete USUARIO_ABOGADO y USUARIO_SUPERVISOR con el LOGIN (username) de usuarios ya existentes en el sistema.",
                "No cree usuarios nuevos aquí: ambos deben existir previamente en Administración > Usuarios.",
                "Cada fila vincula a un abogado con su supervisor. Un abogado puede tener un solo supervisor activo.",
            };
            for (int i = 0; i < textos.length; i++) {
                instrucciones.createRow(i).createCell(0).setCellValue(textos[i]);
            }
            instrucciones.setColumnWidth(0, 12000);

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                workbook.write(out);
                return out.toByteArray();
            }
        }
    }

    public List<EquipoSupervisionImportItemDTO> previsualizar(InputStream inputStream) throws IOException, SQLException {
        List<EquipoSupervisionImportItemDTO> items = new ArrayList<>();
        Set<String> abogadosVistos = new HashSet<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int ultimaFila = sheet.getLastRowNum();
            for (int i = 1; i <= ultimaFila; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String usuarioAbogado = valorCelda(row.getCell(0));
                String usuarioSupervisor = valorCelda(row.getCell(1));
                if (usuarioAbogado.isEmpty() && usuarioSupervisor.isEmpty()) {
                    continue;
                }
                items.add(validarFila(i + 1, usuarioAbogado, usuarioSupervisor, abogadosVistos));
            }
        }
        return items;
    }

    private EquipoSupervisionImportItemDTO validarFila(int numeroFila, String usuarioAbogado, String usuarioSupervisor,
            Set<String> abogadosVistos) throws SQLException {
        EquipoSupervisionImportItemDTO item = new EquipoSupervisionImportItemDTO();
        item.setFila(numeroFila);
        item.setUsuarioAbogado(usuarioAbogado);
        item.setUsuarioSupervisor(usuarioSupervisor);

        if (usuarioAbogado.isEmpty()) {
            marcar(item, "ERROR", "Falta el usuario abogado.");
            return item;
        }
        if (usuarioSupervisor.isEmpty()) {
            marcar(item, "ERROR", "Falta el usuario supervisor.");
            return item;
        }
        String claveAbogado = usuarioAbogado.trim().toUpperCase(Locale.ROOT);
        if (!abogadosVistos.add(claveAbogado)) {
            marcar(item, "ERROR", "Abogado repetido en el archivo.");
            return item;
        }

        UsuarioAsignableEquipoDTO abogado = usuarioSupervisionAdminDAO.buscarUsuarioActivoPorUsername(usuarioAbogado);
        if (abogado == null) {
            marcar(item, "ERROR", "El usuario abogado no existe o no está activo.");
            return item;
        }
        item.setIdAbogado(abogado.getIdUsuario());
        item.setNombreAbogado(abogado.getNombreCompleto());

        UsuarioAsignableEquipoDTO supervisor = usuarioSupervisionAdminDAO.buscarUsuarioActivoPorUsername(usuarioSupervisor);
        if (supervisor == null) {
            marcar(item, "ERROR", "El usuario supervisor no existe o no está activo.");
            return item;
        }
        item.setIdSupervisor(supervisor.getIdUsuario());
        item.setNombreSupervisor(supervisor.getNombreCompleto());

        if (abogado.getIdUsuario().equals(supervisor.getIdUsuario())) {
            marcar(item, "ERROR", "El abogado no puede ser su propio supervisor.");
            return item;
        }
        if (!supervisor.isSupervisor()) {
            marcar(item, "ERROR", "El usuario supervisor no tiene rol de supervisión.");
            return item;
        }
        if (!abogado.isAbogado()) {
            marcar(item, "ADVERTENCIA", "El usuario abogado no tiene rol ABOGADO.");
            return item;
        }
        marcar(item, "VALIDO", null);
        return item;
    }

    private void marcar(EquipoSupervisionImportItemDTO item, String estado, String mensaje) {
        item.setEstadoValidacion(estado);
        item.setMensaje(mensaje);
    }

    /**
     * Confirma la importación fila por fila (cada una con su propia transacción implícita vía
     * {@link UsuarioSupervisionAdminDAO#agregarAbogado(Long, Long, Long)}), sin todo-o-nada — mismo
     * criterio de robustez que la importación legacy de V1.
     */
    public List<EquipoSupervisionImportResultadoDTO> confirmarImportacion(
            List<EquipoSupervisionImportItemDTO> filas, boolean incluirAdvertencias) {
        Long idUsuarioActual = CurrentUser.get().idUsuario();
        List<EquipoSupervisionImportResultadoDTO> resultados = new ArrayList<>();
        for (EquipoSupervisionImportItemDTO fila : filas) {
            if ("ERROR".equals(fila.getEstadoValidacion())) {
                resultados.add(new EquipoSupervisionImportResultadoDTO(
                        fila.getFila(), fila.getUsuarioAbogado(), fila.getUsuarioSupervisor(), "OMITIDO", fila.getMensaje()));
                continue;
            }
            if ("ADVERTENCIA".equals(fila.getEstadoValidacion()) && !incluirAdvertencias) {
                resultados.add(new EquipoSupervisionImportResultadoDTO(
                        fila.getFila(), fila.getUsuarioAbogado(), fila.getUsuarioSupervisor(), "OMITIDO", fila.getMensaje()));
                continue;
            }
            try {
                usuarioSupervisionAdminDAO.agregarAbogado(fila.getIdSupervisor(), fila.getIdAbogado(), idUsuarioActual);
                resultados.add(new EquipoSupervisionImportResultadoDTO(
                        fila.getFila(), fila.getUsuarioAbogado(), fila.getUsuarioSupervisor(), "IMPORTADO", null));
            } catch (SQLException ex) {
                resultados.add(new EquipoSupervisionImportResultadoDTO(
                        fila.getFila(), fila.getUsuarioAbogado(), fila.getUsuarioSupervisor(), "ERROR", ex.getMessage()));
            }
        }
        return resultados;
    }

    private static String valorCelda(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }
}
