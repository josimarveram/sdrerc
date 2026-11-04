package com.sdrerc.v3.web;

import com.sdrerc.v3.application.EquipoJuridicoAdminService;
import com.sdrerc.v3.application.EquipoSupervisionExcelService;
import com.sdrerc.v3.domain.AbogadoSupervisadoDTO;
import com.sdrerc.v3.domain.AreaAdminDTO;
import com.sdrerc.v3.domain.EquipoJuridicoAdminDTO;
import com.sdrerc.v3.domain.EquipoMiembroAdminDTO;
import com.sdrerc.v3.domain.EquipoSupervisionImportItemDTO;
import com.sdrerc.v3.domain.EquipoSupervisionImportResultadoDTO;
import com.sdrerc.v3.domain.UsuarioAsignableEquipoDTO;
import com.sdrerc.v3.web.dto.ConfirmarImportacionSupervisionRequest;
import com.sdrerc.v3.web.dto.GuardarEquipoJuridicoRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Equivalente web de Administración &gt; Equipo Jurídico (V2: {@code JPanelEquipoJuridicoV2}).
 * Cubre las 2 pestañas del módulo: "Equipos" (CRUD + miembros) y "Personal por supervisor"
 * (relación abogado↔supervisor + plantilla Excel simplificada, ver Javadoc de
 * {@link EquipoSupervisionExcelService}).
 */
@RestController
@RequestMapping("/api/admin/equipos")
public class EquipoJuridicoAdminController {

    private static final MediaType XLSX = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final EquipoJuridicoAdminService equipoJuridicoAdminService;
    private final EquipoSupervisionExcelService equipoSupervisionExcelService;

    public EquipoJuridicoAdminController(
            EquipoJuridicoAdminService equipoJuridicoAdminService,
            EquipoSupervisionExcelService equipoSupervisionExcelService) {
        this.equipoJuridicoAdminService = equipoJuridicoAdminService;
        this.equipoSupervisionExcelService = equipoSupervisionExcelService;
    }

    @GetMapping
    public List<EquipoJuridicoAdminDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Long idArea,
            @RequestParam(required = false) Long idSupervisor,
            @RequestParam(required = false) Integer limite) throws SQLException {
        return equipoJuridicoAdminService.buscar(texto, activo, idArea, idSupervisor, limite);
    }

    @GetMapping("/{idEquipo}")
    public EquipoJuridicoAdminDTO obtener(@PathVariable Long idEquipo) throws SQLException {
        return equipoJuridicoAdminService.obtenerPorId(idEquipo);
    }

    @GetMapping("/areas")
    public List<AreaAdminDTO> listarAreas() throws SQLException {
        return equipoJuridicoAdminService.listarAreasActivas();
    }

    @GetMapping("/usuarios-asignables")
    public List<UsuarioAsignableEquipoDTO> listarUsuariosAsignables() throws SQLException {
        return equipoJuridicoAdminService.listarUsuariosAsignables();
    }

    @GetMapping("/supervisores-asignables")
    public List<UsuarioAsignableEquipoDTO> listarSupervisoresAsignables() throws SQLException {
        return equipoJuridicoAdminService.listarSupervisoresAsignables();
    }

    @GetMapping("/{idEquipo}/miembros")
    public List<EquipoMiembroAdminDTO> listarMiembros(@PathVariable Long idEquipo) throws SQLException {
        return equipoJuridicoAdminService.listarMiembros(idEquipo);
    }

    @PostMapping
    public EquipoJuridicoAdminDTO crear(@RequestBody GuardarEquipoJuridicoRequest request) throws SQLException {
        return equipoJuridicoAdminService.guardar(aDto(request, null), request.idResponsable());
    }

    @PutMapping("/{idEquipo}")
    public EquipoJuridicoAdminDTO editar(@PathVariable Long idEquipo, @RequestBody GuardarEquipoJuridicoRequest request)
            throws SQLException {
        return equipoJuridicoAdminService.guardar(aDto(request, idEquipo), request.idResponsable());
    }

    @PutMapping("/{idEquipo}/activo")
    public EquipoJuridicoAdminDTO cambiarActivo(@PathVariable Long idEquipo, @RequestParam boolean activo) throws SQLException {
        return equipoJuridicoAdminService.cambiarActivo(idEquipo, activo);
    }

    @PostMapping("/{idEquipo}/miembros")
    public void agregarMiembro(@PathVariable Long idEquipo, @RequestParam Long idUsuario) throws SQLException {
        equipoJuridicoAdminService.agregarMiembro(idEquipo, idUsuario);
    }

    @DeleteMapping("/{idEquipo}/miembros/{idUsuario}")
    public void quitarMiembro(@PathVariable Long idEquipo, @PathVariable Long idUsuario) throws SQLException {
        equipoJuridicoAdminService.quitarMiembro(idEquipo, idUsuario);
    }

    @PutMapping("/{idEquipo}/responsable")
    public void marcarResponsable(@PathVariable Long idEquipo, @RequestParam Long idUsuario) throws SQLException {
        equipoJuridicoAdminService.marcarResponsable(idEquipo, idUsuario);
    }

    @GetMapping("/supervisores")
    public List<UsuarioAsignableEquipoDTO> listarSupervisoresConAbogados() throws SQLException {
        return equipoJuridicoAdminService.listarSupervisoresConAbogados();
    }

    @GetMapping("/supervisores/{idSupervisor}/abogados")
    public List<AbogadoSupervisadoDTO> listarAbogadosPorSupervisor(@PathVariable Long idSupervisor) throws SQLException {
        return equipoJuridicoAdminService.listarAbogadosPorSupervisor(idSupervisor);
    }

    @PostMapping("/supervisores/{idSupervisor}/abogados")
    public void agregarAbogadoASupervisor(@PathVariable Long idSupervisor, @RequestParam Long idAbogado) throws SQLException {
        equipoJuridicoAdminService.agregarAbogadoASupervisor(idSupervisor, idAbogado);
    }

    @DeleteMapping("/supervisores/{idSupervisor}/abogados/{idAbogado}")
    public void quitarAbogadoDeSupervisor(@PathVariable Long idSupervisor, @PathVariable Long idAbogado) throws SQLException {
        equipoJuridicoAdminService.quitarAbogadoDeSupervisor(idSupervisor, idAbogado);
    }

    @GetMapping("/supervisores/plantilla-excel")
    public ResponseEntity<byte[]> plantillaExcel() throws IOException {
        byte[] contenido = equipoSupervisionExcelService.generarPlantilla();
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(EquipoSupervisionExcelService.NOMBRE_ARCHIVO).build().toString())
                .body(contenido);
    }

    @PostMapping("/supervisores/excel/previsualizar")
    public List<EquipoSupervisionImportItemDTO> previsualizarExcel(@RequestParam("archivo") MultipartFile archivo)
            throws IOException, SQLException {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Seleccione un archivo .xlsx para previsualizar.");
        }
        return equipoSupervisionExcelService.previsualizar(archivo.getInputStream());
    }

    @PostMapping("/supervisores/excel/confirmar")
    public List<EquipoSupervisionImportResultadoDTO> confirmarExcel(@RequestBody ConfirmarImportacionSupervisionRequest request) {
        return equipoSupervisionExcelService.confirmarImportacion(request.filas(), request.incluirAdvertencias());
    }

    private static EquipoJuridicoAdminDTO aDto(GuardarEquipoJuridicoRequest request, Long idEquipo) {
        EquipoJuridicoAdminDTO dto = new EquipoJuridicoAdminDTO();
        dto.setIdEquipo(idEquipo != null ? idEquipo : request.idEquipo());
        dto.setIdArea(request.idArea());
        dto.setCodigo(request.codigo());
        dto.setNombre(request.nombre());
        dto.setDescripcion(request.descripcion());
        dto.setActivo(request.activo());
        return dto;
    }
}
