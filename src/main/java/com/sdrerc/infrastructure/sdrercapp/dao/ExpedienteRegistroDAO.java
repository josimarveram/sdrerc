package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.application.sdrercapp.CorrelativoExpedienteService;
import com.sdrerc.domain.dto.sdrercapp.CargaDiariaPreviewDTO;
import com.sdrerc.domain.dto.sdrercapp.CargaDiariaResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosActaDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosPersonaRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosSolicitudDTO;
import com.sdrerc.domain.dto.sdrercapp.RegistroManualExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.RegistroManualResultadoDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpedienteRegistroDAO {

    private static final String CODIGO_ETAPA_REGISTRO = "REGISTRO";
    private static final String CODIGO_ESTADO_REGISTRADO = "REGISTRADO";
    private static final String CODIGO_MOVIMIENTO_CARGA_DIARIA = "IMPORTACION_CARGA_DIARIA";
    private static final String CODIGO_MOVIMIENTO_REGISTRO_MANUAL = "RECEPCION_DOCUMENTO";
    private static final int DIAS_PLAZO_INICIAL = 30;

    private final CatalogoLookupDAO catalogoLookupDAO;

    public ExpedienteRegistroDAO() {
        this(new CatalogoLookupDAO());
    }

    public ExpedienteRegistroDAO(CatalogoLookupDAO catalogoLookupDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    public Map<Integer, String> detectarDuplicadosContraBase(List<CargaDiariaPreviewDTO> registros) throws SQLException {
        Map<Integer, String> duplicados = new LinkedHashMap<>();
        if (registros == null || registros.isEmpty()) {
            return duplicados;
        }

        try (Connection conn = SdrercAppConnection.getConnection()) {
            for (CargaDiariaPreviewDTO item : registros) {
                List<String> motivos = new ArrayList<>();
                String porActaTitular = buscarPorActaYTitular(conn, item.getNumeroActa(), item.getTitular());
                if (porActaTitular != null) {
                    motivos.add("Acta y titular ya existen en " + porActaTitular);
                }
                if (!motivos.isEmpty()) {
                    duplicados.put(item.getFila(), String.join("; ", motivos));
                }
            }
        }
        return duplicados;
    }

    public String detectarDuplicadoPorActaYTitular(String numeroActa, String titular) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return buscarPorActaYTitular(conn, numeroActa, titular);
        }
    }

    public int obtenerSiguienteCorrelativoExpediente(int anio) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return obtenerUltimoCorrelativoExpediente(conn, anio) + 1;
        }
    }

    public CargaDiariaResultadoDTO registrarCarga(
            List<CargaDiariaPreviewDTO> registros,
            CorrelativoExpedienteService correlativoService) throws SQLException {
        if (registros == null || registros.isEmpty()) {
            return new CargaDiariaResultadoDTO(0, 0, "No hay registros para confirmar.", registros);
        }

        List<CargaDiariaPreviewDTO> candidatos = new ArrayList<>();
        int omitidos = 0;
        for (CargaDiariaPreviewDTO item : registros) {
            if (item.isListoParaRegistrar() && !item.isRegistrado()) {
                candidatos.add(item);
            } else {
                omitidos++;
            }
        }
        if (candidatos.isEmpty()) {
            return new CargaDiariaResultadoDTO(0, omitidos, "No hay filas listas para registrar.", registros);
        }

        List<RegistroConfirmado> confirmados = new ArrayList<>();
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idEtapaRegistro = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, CODIGO_ETAPA_REGISTRO), "etapa REGISTRO");
                Long idEstadoRegistrado = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, CODIGO_ESTADO_REGISTRADO), "estado REGISTRADO");
                Long idTipoMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_CARGA_DIARIA), "movimiento IMPORTACION_CARGA_DIARIA");
                int anioCorrelativo = correlativoService.anioActual();
                int siguienteCorrelativo = obtenerUltimoCorrelativoExpediente(conn, anioCorrelativo) + 1;

                for (CargaDiariaPreviewDTO item : candidatos) {
                    DuplicadoRegistro duplicadoActaTitular = buscarRegistroPorActaYTitular(conn, item.getNumeroActa(), item.getTitular());
                    if (duplicadoActaTitular != null) {
                        item.setPosibleDuplicado(true);
                        item.setMotivoDuplicado(unirMotivoDuplicado(
                                item.getMotivoDuplicado(),
                                "Acta y titular ya existen en " + duplicadoActaTitular.descripcion()));
                    }

                    Long idTitular = insertarPersona(
                            conn,
                            item.getTitular(),
                            item.getTipoDocumentoIdentidadTitular(),
                            item.getNumeroDocumentoIdentidadTitular());
                    Long idRemitente = hasText(item.getRemitente()) && !equalsIgnoreCase(item.getRemitente(), item.getTitular())
                            ? insertarPersona(
                                    conn,
                                    item.getRemitente(),
                                    item.getTipoDocumentoIdentidadSolicitante(),
                                    item.getNumeroDocumentoIdentidadSolicitante())
                            : null;
                    Long idSolicitante = idRemitente == null ? idTitular : idRemitente;

                    Long idExpediente = insertarExpediente(conn, item, idEtapaRegistro, idEstadoRegistrado);
                    String numeroExpediente = null;
                    if (duplicadoActaTitular == null) {
                        numeroExpediente = correlativoService.generar(anioCorrelativo, siguienteCorrelativo++);
                        actualizarNumeroExpediente(conn, idExpediente, numeroExpediente);
                    }

                    insertarSolicitud(conn, item, idExpediente, idSolicitante);
                    insertarExpedientePersona(conn, idExpediente, idTitular, "TITULAR");
                    if (idRemitente != null) {
                        insertarExpedientePersona(conn, idExpediente, idRemitente, "REMITENTE");
                    }
                    insertarActa(conn, item, idExpediente);
                    insertarDocumento(conn, item, idExpediente);
                    insertarHistorial(conn, item, idExpediente, idTipoMovimiento, idEtapaRegistro, idEstadoRegistrado);

                    confirmados.add(new RegistroConfirmado(item, idExpediente, numeroExpediente, duplicadoActaTitular != null));
                }

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            }
        }

        int sinNumeroPorDuplicado = 0;
        for (RegistroConfirmado confirmado : confirmados) {
            confirmado.item.setRegistrado(true);
            confirmado.item.setListoParaRegistrar(false);
            confirmado.item.setEstadoValidacion("Registrado");
            if (confirmado.duplicadoSinNumero) {
                sinNumeroPorDuplicado++;
                confirmado.item.setMensajeValidacion("Registrado en SDRERC_APP sin número de expediente por duplicidad de acta y titular.");
                confirmado.item.setNumeroExpedienteGenerado("Sin número por duplicado");
            } else {
                confirmado.item.setMensajeValidacion("Registrado en SDRERC_APP.");
                confirmado.item.setNumeroExpedienteGenerado(confirmado.numeroExpediente);
            }
            confirmado.item.setIdExpedienteRegistrado(confirmado.idExpediente);
        }

        String mensaje = confirmados.size() + " expediente(s) registrado(s) en SDRERC_APP.";
        if (sinNumeroPorDuplicado > 0) {
            mensaje += " " + sinNumeroPorDuplicado + " duplicado(s) quedaron sin número de expediente.";
        }
        return new CargaDiariaResultadoDTO(
                confirmados.size(),
                omitidos,
                mensaje,
                registros);
    }

    public RegistroManualResultadoDTO registrarManual(
            RegistroManualExpedienteDTO registro,
            CorrelativoExpedienteService correlativoService) throws SQLException {
        if (registro == null) {
            throw new IllegalArgumentException("Complete los datos del registro manual.");
        }

        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idEtapaRegistro = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, CODIGO_ETAPA_REGISTRO), "etapa REGISTRO");
                Long idEstadoRegistrado = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, CODIGO_ESTADO_REGISTRADO), "estado REGISTRADO");
                Long idTipoMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_REGISTRO_MANUAL), "movimiento RECEPCION_DOCUMENTO");
                DuplicadoRegistro duplicadoActaTitular = buscarRegistroPorActaYTitular(
                        conn,
                        registro.getActa().getNumeroActa(),
                        registro.getTitular().getNombreCompleto());
                if (duplicadoActaTitular != null) {
                    registro.setPosibleDuplicado(true);
                    registro.setMotivoDuplicado("Acta y titular ya existen en " + duplicadoActaTitular.descripcion());
                }

                Long idTitular = insertarPersonaManual(conn, registro.getTitular());
                Long idRemitente = insertarPersonaManual(conn, registro.getRemitente());
                Long idExpediente = insertarExpedienteManual(conn, registro.getSolicitud(), idEtapaRegistro, idEstadoRegistrado);
                String numeroExpediente = null;
                if (duplicadoActaTitular == null) {
                    int anioCorrelativo = correlativoService.anioActual();
                    int siguienteCorrelativo = obtenerUltimoCorrelativoExpediente(conn, anioCorrelativo) + 1;
                    numeroExpediente = correlativoService.generar(anioCorrelativo, siguienteCorrelativo);
                    actualizarNumeroExpediente(conn, idExpediente, numeroExpediente);
                }

                insertarSolicitudManual(conn, registro, idExpediente, idRemitente);
                insertarExpedientePersona(conn, idExpediente, idTitular, "TITULAR");
                insertarExpedientePersona(conn, idExpediente, idRemitente, "REMITENTE");
                insertarActaManual(conn, registro.getActa(), idExpediente);
                insertarDocumentoManual(conn, registro.getSolicitud(), idExpediente);
                insertarHistorialManual(conn, registro, idExpediente, idTipoMovimiento, idEtapaRegistro, idEstadoRegistrado, numeroExpediente);

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                if (duplicadoActaTitular != null) {
                    return new RegistroManualResultadoDTO(
                            idExpediente,
                            "Sin número por duplicado",
                            "Registro manual guardado sin número de expediente por duplicidad de acta y titular. "
                                    + "Registro previo: " + duplicadoActaTitular.descripcion() + ".");
                }
                return new RegistroManualResultadoDTO(
                        idExpediente,
                        numeroExpediente,
                        "Expediente " + numeroExpediente + " registrado en SDRERC_APP.");
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            }
        }
    }

    private String buscarPorActaYTitular(Connection conn, String acta, String titular) throws SQLException {
        DuplicadoRegistro duplicado = buscarRegistroPorActaYTitular(conn, acta, titular);
        return duplicado == null ? null : duplicado.descripcion();
    }

    private int obtenerUltimoCorrelativoExpediente(Connection conn, int anio) throws SQLException {
        String sql = "SELECT NVL(MAX(TO_NUMBER(SUBSTR(numero_expediente, -6))), 0) AS correlativo "
                + "FROM expediente "
                + "WHERE numero_expediente IS NOT NULL "
                + "AND activo = 1 "
                + "AND (numero_expediente LIKE ? OR numero_expediente LIKE ?) "
                + "AND REGEXP_LIKE(numero_expediente, '^SDRERC[-_]EXP[-_][0-9]{4}[-_][0-9]{6}$')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "SDRERC-EXP-" + anio + "-%");
            ps.setString(2, "SDRERC_EXP_" + anio + "_%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("correlativo");
                }
            }
        }
        return 0;
    }

    private DuplicadoRegistro buscarRegistroPorActaYTitular(Connection conn, String acta, String titular) throws SQLException {
        if (!hasText(acta) || !hasText(titular)) {
            return null;
        }
        String sql = "SELECT e.id_expediente, e.numero_expediente FROM expediente e "
                + "JOIN expediente_acta a ON a.id_expediente = e.id_expediente "
                + "JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente "
                + "JOIN persona p ON p.id_persona = ep.id_persona "
                + "WHERE e.activo = 1 AND a.activo = 1 AND ep.activo = 1 "
                + "AND ep.tipo_relacion_persona = 'TITULAR' "
                + "AND UPPER(TRIM(a.numero_acta)) = ? "
                + "AND UPPER(TRIM(COALESCE(NULLIF(TRIM(p.razon_social), ''), "
                + "NULLIF(TRIM(TRIM(NVL(p.nombres, '')) || ' ' || TRIM(NVL(p.apellidos, ''))), ''), "
                + "p.numero_documento))) = ? "
                + "AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, acta.trim().toUpperCase(Locale.ROOT));
            ps.setString(2, titular.trim().toUpperCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new DuplicadoRegistro(rs.getLong("id_expediente"), rs.getString("numero_expediente"));
            }
        }
    }

    private Long insertarPersona(Connection conn, String nombre, String tipoDocumento, String numeroDocumento) throws SQLException {
        String sql = "INSERT INTO persona (tipo_documento, numero_documento, razon_social, activo) VALUES (?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_PERSONA"})) {
            ps.setString(1, normalizarTipoDocumentoIdentidadParaBd(tipoDocumento));
            ps.setString(2, normalizarDocumentoIdentidadParaBd(numeroDocumento));
            ps.setString(3, nombre);
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "persona");
        }
    }

    private Long insertarExpediente(Connection conn, CargaDiariaPreviewDTO item, Long idEtapa, Long idEstado) throws SQLException {
        String sql = "INSERT INTO expediente ("
                + "numero_expediente, numero_tramite_documentario, id_etapa_actual, id_estado_actual, "
                + "fecha_registro, fecha_ultimo_movimiento, fecha_vencimiento, prioridad, requiere_publicacion, "
                + "expediente_digital_completo, archivado, cerrado, activo"
                + ") VALUES (NULL, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP, ?, 'NORMAL', 0, 0, 0, 0, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE"})) {
            ps.setString(1, item.getNumeroTramite());
            ps.setLong(2, idEtapa);
            ps.setLong(3, idEstado);
            ps.setDate(4, calcularFechaVencimiento(item.getFechaRecepcion()));
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "expediente");
        }
    }

    private void actualizarNumeroExpediente(Connection conn, Long idExpediente, String numeroExpediente) throws SQLException {
        String sql = "UPDATE expediente SET numero_expediente = ?, modificado_en = SYSTIMESTAMP WHERE id_expediente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroExpediente);
            ps.setLong(2, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo registrar el número de expediente generado.");
            }
        }
    }

    private void insertarSolicitud(Connection conn, CargaDiariaPreviewDTO item, Long idExpediente, Long idPersonaSolicitante) throws SQLException {
        Long idCanal = null;
        if (hasText(item.getCanalRecepcion())) {
            String codigoCanal = resolverCodigoCanalRecepcion(item.getCanalRecepcion());
            idCanal = requerirId(catalogoLookupDAO.obtenerCanalRecepcionId(conn, codigoCanal), "canal " + nombreVisualCanal(codigoCanal));
        }
        String sql = "INSERT INTO expediente_solicitud ("
                + "id_expediente, id_canal_recepcion, id_persona_solicitante, numero_tramite_documentario, "
                + "numero_expediente_sgd, fecha_recepcion, asunto, observacion, es_tramite_virtual, "
                + "potencial_duplicado, activo"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            setLongOrNull(ps, 2, idCanal);
            setLongOrNull(ps, 3, idPersonaSolicitante);
            ps.setString(4, item.getNumeroTramite());
            ps.setString(5, item.getNumeroExpedienteSgd());
            ps.setDate(6, item.getFechaRecepcion() == null ? null : Date.valueOf(item.getFechaRecepcion()));
            ps.setString(7, item.getTipoProcedimiento());
            ps.setString(8, limitar(observacionSolicitud(item), 1000));
            ps.setInt(9, item.isPosibleDuplicado() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    private void insertarExpedientePersona(Connection conn, Long idExpediente, Long idPersona, String tipoRelacion) throws SQLException {
        if (idPersona == null) {
            return;
        }
        String sql = "INSERT INTO expediente_persona (id_expediente, id_persona, tipo_relacion_persona, activo) "
                + "VALUES (?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idPersona);
            ps.setString(3, tipoRelacion);
            ps.executeUpdate();
        }
    }

    private void insertarActa(Connection conn, CargaDiariaPreviewDTO item, Long idExpediente) throws SQLException {
        if (!hasText(item.getNumeroActa())) {
            return;
        }
        Long idTipoActa = null;
        if (hasText(item.getTipoActa())) {
            idTipoActa = catalogoLookupDAO.obtenerTipoActaIdPorCodigoONombre(conn, item.getTipoActa());
        }
        String sql = "INSERT INTO expediente_acta (id_expediente, id_tipo_acta, numero_acta, anio_acta, activo) "
                + "VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            setLongOrNull(ps, 2, idTipoActa);
            ps.setString(3, item.getNumeroActa());
            if (item.getFechaRecepcion() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, item.getFechaRecepcion().getYear());
            }
            ps.executeUpdate();
        }
    }

    private void insertarDocumento(Connection conn, CargaDiariaPreviewDTO item, Long idExpediente) throws SQLException {
        if (!hasText(item.getTipoDocumento())) {
            return;
        }
        String sql = "INSERT INTO expediente_documento ("
                + "id_expediente, nombre_documento, numero_documento, fecha_documento, activo"
                + ") VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, item.getTipoDocumento());
            ps.setString(3, item.getNumeroDocumento());
            ps.setDate(4, item.getFechaRecepcion() == null ? null : Date.valueOf(item.getFechaRecepcion()));
            ps.executeUpdate();
        }
    }

    private void insertarHistorial(
            Connection conn,
            CargaDiariaPreviewDTO item,
            Long idExpediente,
            Long idTipoMovimiento,
            Long idEtapaDestino,
            Long idEstadoDestino) throws SQLException {
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, id_etapa_destino, id_estado_destino, "
                + "tabla_relacionada, id_registro_relacionado, comentario, motivo, activo"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, 'EXPEDIENTE', ?, ?, 'CARGA_DIARIA', 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idTipoMovimiento);
            ps.setLong(3, idEtapaDestino);
            ps.setLong(4, idEstadoDestino);
            ps.setLong(5, idExpediente);
            ps.setString(6, "Registro inicial por carga diaria. Trámite: " + item.getNumeroTramite());
            ps.executeUpdate();
        }
    }

    private Long insertarPersonaManual(Connection conn, DatosPersonaRegistroDTO persona) throws SQLException {
        String sql = "INSERT INTO persona ("
                + "tipo_documento, numero_documento, razon_social, correo_electronico, telefono, direccion, activo"
                + ") VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_PERSONA"})) {
            ps.setString(1, persona.getTipoDocumento());
            ps.setString(2, persona.getNumeroDocumento());
            ps.setString(3, persona.getNombreCompleto());
            ps.setString(4, persona.getCorreo());
            ps.setString(5, persona.getTelefono());
            ps.setString(6, persona.getDireccion());
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "persona");
        }
    }

    private Long insertarExpedienteManual(Connection conn, DatosSolicitudDTO solicitud, Long idEtapa, Long idEstado) throws SQLException {
        String sql = "INSERT INTO expediente ("
                + "numero_expediente, numero_tramite_documentario, id_etapa_actual, id_estado_actual, "
                + "fecha_registro, fecha_ultimo_movimiento, fecha_vencimiento, prioridad, requiere_publicacion, "
                + "expediente_digital_completo, archivado, cerrado, activo"
                + ") VALUES (NULL, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP, ?, ?, 0, 0, 0, 0, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE"})) {
            ps.setString(1, solicitud.getNumeroTramite());
            ps.setLong(2, idEtapa);
            ps.setLong(3, idEstado);
            ps.setDate(4, calcularFechaVencimiento(solicitud.getFechaRecepcion()));
            ps.setString(5, hasText(solicitud.getPrioridad()) ? solicitud.getPrioridad() : "NORMAL");
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "expediente");
        }
    }

    private Date calcularFechaVencimiento(java.time.LocalDate fechaSolicitud) {
        if (fechaSolicitud == null) {
            return null;
        }
        return Date.valueOf(fechaSolicitud.plusDays(DIAS_PLAZO_INICIAL));
    }

    private void insertarSolicitudManual(
            Connection conn,
            RegistroManualExpedienteDTO registro,
            Long idExpediente,
            Long idPersonaSolicitante) throws SQLException {
        DatosSolicitudDTO solicitud = registro.getSolicitud();
        Long idCanal = null;
        if (hasText(solicitud.getCanalCodigo())) {
            idCanal = requerirId(catalogoLookupDAO.obtenerCanalRecepcionId(conn, solicitud.getCanalCodigo()), "canal " + solicitud.getCanalNombre());
        }

        String sql = "INSERT INTO expediente_solicitud ("
                + "id_expediente, id_canal_recepcion, id_persona_solicitante, numero_tramite_documentario, "
                + "numero_expediente_sgd, fecha_recepcion, asunto, observacion, es_tramite_virtual, "
                + "correo_electronico, potencial_duplicado, activo"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            setLongOrNull(ps, 2, idCanal);
            setLongOrNull(ps, 3, idPersonaSolicitante);
            ps.setString(4, solicitud.getNumeroTramite());
            ps.setString(5, solicitud.getNumeroExpedienteSgd());
            ps.setDate(6, solicitud.getFechaRecepcion() == null ? null : Date.valueOf(solicitud.getFechaRecepcion()));
            ps.setString(7, solicitud.getTipoProcedimientoNombre());
            ps.setString(8, limitar(observacionSolicitud(registro), 1000));
            ps.setNull(9, Types.VARCHAR);
            ps.setInt(10, registro.isPosibleDuplicado() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    private void insertarActaManual(Connection conn, DatosActaDTO acta, Long idExpediente) throws SQLException {
        Long idTipoActa = null;
        if (hasText(acta.getTipoActaCodigo())) {
            idTipoActa = catalogoLookupDAO.obtenerTipoActaId(conn, acta.getTipoActaCodigo());
        }

        String sql = "INSERT INTO expediente_acta ("
                + "id_expediente, id_tipo_acta, numero_acta, anio_acta, oficina_registral, libro, folio, activo"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            setLongOrNull(ps, 2, idTipoActa);
            ps.setString(3, acta.getNumeroActa());
            setIntegerOrNull(ps, 4, acta.getAnioActa());
            ps.setString(5, acta.getUbicacionRegistral());
            ps.setNull(6, Types.VARCHAR);
            ps.setNull(7, Types.VARCHAR);
            ps.executeUpdate();
        }
    }

    private void insertarDocumentoManual(Connection conn, DatosSolicitudDTO solicitud, Long idExpediente) throws SQLException {
        String sql = "INSERT INTO expediente_documento ("
                + "id_expediente, nombre_documento, numero_documento, fecha_documento, activo"
                + ") VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, solicitud.getTipoDocumentoNombre());
            ps.setString(3, solicitud.getNumeroDocumento());
            ps.setDate(4, solicitud.getFechaRecepcion() == null ? null : Date.valueOf(solicitud.getFechaRecepcion()));
            ps.executeUpdate();
        }
    }

    private void insertarHistorialManual(
            Connection conn,
            RegistroManualExpedienteDTO registro,
            Long idExpediente,
            Long idTipoMovimiento,
            Long idEtapaDestino,
            Long idEstadoDestino,
            String numeroExpediente) throws SQLException {
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, id_etapa_destino, id_estado_destino, "
                + "tabla_relacionada, id_registro_relacionado, comentario, motivo, activo"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, 'EXPEDIENTE', ?, ?, 'REGISTRO_MANUAL', 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String numeroVisual = hasText(numeroExpediente) ? numeroExpediente : "sin número por duplicado";
            ps.setLong(1, idExpediente);
            ps.setLong(2, idTipoMovimiento);
            ps.setLong(3, idEtapaDestino);
            ps.setLong(4, idEstadoDestino);
            ps.setLong(5, idExpediente);
            ps.setString(6, limitar("Registro manual inicial. Expediente: " + numeroVisual
                    + ". Trámite: " + registro.getSolicitud().getNumeroTramite()
                    + ". Titular: " + registro.getTitular().getNombreCompleto(), 2000));
            ps.executeUpdate();
        }
    }

    private String observacionSolicitud(RegistroManualExpedienteDTO registro) {
        StringBuilder sb = new StringBuilder();
        append(sb, "Validación inicial", registro.getSolicitud().getValidacionInicial());
        append(sb, "Hoja de envío", registro.getSolicitud().getHojaEnvio());
        append(sb, "Tipo de solicitud", registro.getSolicitud().getTipoSolicitudNombre());
        append(sb, "Tipo de documento", registro.getSolicitud().getTipoDocumentoNombre());
        append(sb, "N° EXPEDIENTE SGD", registro.getSolicitud().getNumeroExpedienteSgd());
        append(sb, "Tipo de acta", registro.getActa().getTipoActaNombre());
        append(sb, "Observaciones de registro", registro.getObservacionesGenerales());
        append(sb, "Motivo duplicado", registro.getMotivoDuplicado());
        return sb.toString();
    }

    private String observacionSolicitud(CargaDiariaPreviewDTO item) {
        StringBuilder sb = new StringBuilder();
        append(sb, "Tipo de solicitud", item.getTipoSolicitud());
        append(sb, "Tipo de acta informado", item.getTipoActa());
        append(sb, "Canal recepción", nombreVisualCanal(item.getCanalRecepcion()));
        append(sb, "N° EXPEDIENTE SGD", item.getNumeroExpedienteSgd());
        append(sb, "Tipo documento identidad solicitante", item.getTipoDocumentoIdentidadSolicitante());
        append(sb, "Documento identidad solicitante", normalizarDocumentoIdentidadParaBd(item.getNumeroDocumentoIdentidadSolicitante()));
        append(sb, "Tipo documento identidad titular", item.getTipoDocumentoIdentidadTitular());
        append(sb, "Documento identidad titular", normalizarDocumentoIdentidadParaBd(item.getNumeroDocumentoIdentidadTitular()));
        append(sb, "Observación inicial", item.getObservacionInicial());
        if (!"Válido".equalsIgnoreCase(item.getEstadoValidacion())) {
            append(sb, "Advertencias de validación", item.getMensajeValidacion());
        }
        append(sb, "Motivo duplicado", item.getMotivoDuplicado());
        return sb.toString();
    }

    private void append(StringBuilder sb, String etiqueta, String valor) {
        if (!hasText(valor)) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(" | ");
        }
        sb.append(etiqueta).append(": ").append(valor.trim());
    }

    private Long obtenerGeneratedKey(PreparedStatement ps, String entidad) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                long value = rs.getLong(1);
                return rs.wasNull() ? null : value;
            }
        }
        throw new SQLException("No se pudo obtener el identificador generado de " + entidad + ".");
    }

    private Long requerirId(Long value, String descripcion) throws SQLException {
        if (value == null) {
            throw new SQLException("No se encontró el catálogo requerido: " + descripcion + ".");
        }
        return value;
    }

    private void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private void setIntegerOrNull(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private String limitar(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String normalizarTipoDocumentoIdentidadParaBd(String value) {
        if (!hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if ("SIN DNI".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return trimmed;
    }

    private String normalizarDocumentoIdentidadParaBd(String value) {
        if (!hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if ("SIN DNI".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return trimmed;
    }

    private String resolverCodigoCanalRecepcion(String value) {
        if (!hasText(value)) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT)
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
                .replaceAll("\\s+", " ");
        if ("MPV".equals(normalized)
                || "MESA PARTES VIRTUAL".equals(normalized)
                || "MESA DE PARTES VIRTUAL".equals(normalized)) {
            return "MESA_PARTES_VIRTUAL";
        }
        if ("MP PRESENCIAL".equals(normalized)
                || "MESA PARTES PRESENCIAL".equals(normalized)
                || "MESA DE PARTES PRESENCIAL".equals(normalized)) {
            return "MESA_PARTES_PRESENCIAL";
        }
        if ("OR".equals(normalized) || "OR PRESENCIAL".equals(normalized)) {
            return "OR_PRESENCIAL";
        }
        return normalized.replace(' ', '_');
    }

    private String nombreVisualCanal(String codigo) {
        if (!hasText(codigo)) {
            return null;
        }
        String normalized = resolverCodigoCanalRecepcion(codigo);
        if ("MESA_PARTES_VIRTUAL".equals(normalized)) {
            return "Mesa de partes virtual";
        }
        if ("MESA_PARTES_PRESENCIAL".equals(normalized)) {
            return "Mesa de partes presencial";
        }
        if ("OR_PRESENCIAL".equals(normalized)) {
            return "OR Presencial";
        }
        if ("INTERNO".equals(normalized)) {
            return "Interno";
        }
        return codigo;
    }

    private String unirMotivoDuplicado(String actual, String nuevo) {
        if (!hasText(actual)) {
            return nuevo;
        }
        if (!hasText(nuevo) || actual.contains(nuevo)) {
            return actual;
        }
        return actual.trim() + " | " + nuevo.trim();
    }

    private void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // El error original se reporta al usuario; el rollback fallido no debe ocultarlo.
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }

    private static class RegistroConfirmado {

        private final CargaDiariaPreviewDTO item;
        private final Long idExpediente;
        private final String numeroExpediente;
        private final boolean duplicadoSinNumero;

        private RegistroConfirmado(CargaDiariaPreviewDTO item, Long idExpediente, String numeroExpediente, boolean duplicadoSinNumero) {
            this.item = item;
            this.idExpediente = idExpediente;
            this.numeroExpediente = numeroExpediente;
            this.duplicadoSinNumero = duplicadoSinNumero;
        }
    }

    private static class DuplicadoRegistro {

        private final long idExpediente;
        private final String numeroExpediente;

        private DuplicadoRegistro(long idExpediente, String numeroExpediente) {
            this.idExpediente = idExpediente;
            this.numeroExpediente = numeroExpediente;
        }

        private String descripcion() {
            if (hasText(numeroExpediente)) {
                return numeroExpediente;
            }
            return "expediente ID " + idExpediente + " sin número";
        }
    }
}
