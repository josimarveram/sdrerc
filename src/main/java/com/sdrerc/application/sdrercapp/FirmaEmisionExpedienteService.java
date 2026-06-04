package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.FirmaEmisionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.FirmaEmisionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.FirmaEmisionResultadoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.CatalogoLookupDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.FirmaEmisionExpedienteDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class FirmaEmisionExpedienteService {

    public static final String ACCION_FIRMA_DOCUMENTO = "FIRMA_DOCUMENTO";
    public static final String ACCION_REGISTRO_NUMERO = "REGISTRO_NUMERO_RESOLUCION";

    private final FirmaEmisionExpedienteDAO firmaEmisionExpedienteDAO;
    private final FirmaEmisionValidacionService validacionService;
    private final UsuarioAsignacionService usuarioAsignacionService;
    private final CatalogoLookupDAO catalogoLookupDAO;

    public FirmaEmisionExpedienteService() {
        this(
                new FirmaEmisionExpedienteDAO(),
                new FirmaEmisionValidacionService(),
                new UsuarioAsignacionService(),
                new CatalogoLookupDAO());
    }

    public FirmaEmisionExpedienteService(
            FirmaEmisionExpedienteDAO firmaEmisionExpedienteDAO,
            FirmaEmisionValidacionService validacionService,
            UsuarioAsignacionService usuarioAsignacionService,
            CatalogoLookupDAO catalogoLookupDAO) {
        this.firmaEmisionExpedienteDAO = firmaEmisionExpedienteDAO;
        this.validacionService = validacionService;
        this.usuarioAsignacionService = usuarioAsignacionService;
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    public List<FirmaEmisionExpedienteDTO> buscarExpedientes(String textoLibre, String estadoCodigo, int limite) throws SQLException {
        return firmaEmisionExpedienteDAO.buscarExpedientes(textoLibre, estadoCodigo, limite);
    }

    public List<CatalogoItemDTO> listarTiposResolucion() throws SQLException {
        return catalogoLookupDAO.listarTiposResolucion();
    }

    public FirmaEmisionResultadoDTO registrarFirma(FirmaEmisionRegistroDTO registro) throws SQLException {
        validar(registro, false);
        return firmaEmisionExpedienteDAO.registrarFirma(normalizarRegistro(registro), resolverUsuarioActualSdrercApp());
    }

    public FirmaEmisionResultadoDTO registrarEmision(FirmaEmisionRegistroDTO registro) throws SQLException {
        validar(registro, false);
        return firmaEmisionExpedienteDAO.registrarEmision(normalizarRegistro(registro), resolverUsuarioActualSdrercApp());
    }

    public FirmaEmisionResultadoDTO registrarNumeroResolucion(FirmaEmisionRegistroDTO registro) throws SQLException {
        validar(registro, true);
        return firmaEmisionExpedienteDAO.registrarNumeroResolucion(normalizarRegistro(registro), resolverUsuarioActualSdrercApp());
    }

    public FirmaEmisionResultadoDTO enviarEjecucion(FirmaEmisionRegistroDTO registro) throws SQLException {
        validar(registro, false);
        return firmaEmisionExpedienteDAO.enviarEjecucion(normalizarRegistro(registro), resolverUsuarioActualSdrercApp());
    }

    private void validar(FirmaEmisionRegistroDTO registro, boolean requiereNumero) {
        List<String> errores = validacionService.validarRegistro(registro, requiereNumero);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
    }

    private FirmaEmisionRegistroDTO normalizarRegistro(FirmaEmisionRegistroDTO registro) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaFirma = registro.getFechaFirma() == null ? hoy : registro.getFechaFirma();
        LocalDate fechaEmision = registro.getFechaEmision() == null ? hoy : registro.getFechaEmision();
        LocalDate fechaResolucion = registro.getFechaResolucion() == null ? hoy : registro.getFechaResolucion();
        return new FirmaEmisionRegistroDTO(
                registro.getIdExpediente(),
                registro.getAccionCodigo(),
                registro.getTipoResolucionCodigo(),
                registro.getTipoResolucionNombre(),
                registro.getNumeroResolucion(),
                fechaFirma,
                fechaEmision,
                fechaResolucion,
                registro.getComentario());
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
