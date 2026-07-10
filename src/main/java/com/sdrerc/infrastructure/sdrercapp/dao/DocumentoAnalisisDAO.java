package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.AsignacionCartaRespuestaDTO;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentoAnalisisDAO {

    private final CatalogoLookupDAO catalogoLookupDAO;

    public DocumentoAnalisisDAO() {
        this(new CatalogoLookupDAO());
    }

    public DocumentoAnalisisDAO(CatalogoLookupDAO catalogoLookupDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    public List<CatalogoItemDTO> listarTiposDocumentoAnalizado() throws SQLException {
        return catalogoLookupDAO.listarTiposDocumentoAdjuntoAnalisis();
    }

    public List<CatalogoItemDTO> listarEstadosDocumento() throws SQLException {
        return filtrarEstadosDocumentoAnalizado(catalogoLookupDAO.listarEstadosDocumento());
    }

    public List<DocumentoAnalizadoDTO> listarPorExpediente(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return new ArrayList<DocumentoAnalizadoDTO>();
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return listarPorExpediente(conn, idExpediente);
        }
    }

    public List<AsignacionCartaRespuestaDTO> listarCartasRespuestaPendientes() throws SQLException {
        List<AsignacionCartaRespuestaDTO> items = new ArrayList<AsignacionCartaRespuestaDTO>();
        try (Connection conn = SdrercAppConnection.getConnection()) {
            if (!soportaRespuestaDocumentoAnalizado(conn)) {
                return items;
            }
            boolean soportaPublicacion = soportaPublicacionPreparada(conn);
            boolean soportaNumeroDocumento = soportaNumeroDocumentoAnalizado(conn);
            boolean soportaClasificacion = soportaClasificacionTipoDocumento(conn);
            String sql = "SELECT da.id_documento_analizado, da.id_expediente, e.numero_expediente, "
                    + "esol.numero_expediente_sgd, "
                    + nombrePersona("p") + " AS titular, "
                    + "tda.nombre AS tipo_documento_nombre, ed.nombre AS estado_documento_nombre, "
                    + "da.fecha_documento, "
                    + (soportaNumeroDocumento
                            ? "da.numero_documento, "
                            : "CAST(NULL AS VARCHAR2(120)) AS numero_documento, ")
                    + "da.descripcion, NVL(da.requiere_respuesta, 0) AS requiere_respuesta, "
                    + "NVL(da.notificado, 0) AS notificado, da.fecha_acuse, da.confirmacion_respuesta, "
                    + "da.fecha_respuesta, da.numero_hoja_envio_respuesta "
                    + (soportaPublicacion
                            ? ", NVL(e.requiere_publicacion, 0) AS requiere_publicacion, "
                            + "(SELECT fecha_publicacion FROM ("
                            + " SELECT p2.fecha_publicacion FROM expediente_publicacion p2 "
                            + " WHERE p2.id_expediente = da.id_expediente AND p2.activo = 1 "
                            + " ORDER BY p2.creado_en DESC, p2.id_expediente_publicacion DESC"
                            + ") WHERE ROWNUM = 1) AS fecha_publicacion "
                            : ", 0 AS requiere_publicacion, CAST(NULL AS DATE) AS fecha_publicacion ")
                    + "FROM expediente_documento_analizado da "
                    + "JOIN expediente e ON e.id_expediente = da.id_expediente AND e.activo = 1 "
                    + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                    + "LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente "
                    + " AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                    + "LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                    + "LEFT JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                    + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                    + "WHERE da.activo = 1 "
                    + "AND UPPER(NVL(da.confirmacion_respuesta, '')) = 'SI' "
                    + "AND UPPER(NVL(ed.codigo, '')) IN ('ATENDIDO', 'FINALIZADO') "
                    + (soportaClasificacion ? "AND UPPER(NVL(tda.clasificacion, '')) = 'INTERMEDIO' " : "")
                    + "ORDER BY da.fecha_documento DESC NULLS LAST, da.id_documento_analizado DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new AsignacionCartaRespuestaDTO(
                            getLongOrNull(rs, "id_documento_analizado"),
                            getLongOrNull(rs, "id_expediente"),
                            rs.getString("numero_expediente"),
                            rs.getString("numero_expediente_sgd"),
                            rs.getString("titular"),
                            rs.getString("tipo_documento_nombre"),
                            rs.getString("estado_documento_nombre"),
                            toLocalDate(rs.getDate("fecha_documento")),
                            rs.getString("numero_documento"),
                            rs.getString("descripcion"),
                            rs.getInt("requiere_respuesta") == 1,
                            rs.getInt("notificado") == 1,
                            toLocalDate(rs.getDate("fecha_acuse")),
                            rs.getString("confirmacion_respuesta"),
                            toLocalDate(rs.getDate("fecha_respuesta")),
                            rs.getString("numero_hoja_envio_respuesta"),
                            rs.getInt("requiere_publicacion") == 1,
                            toLocalDate(rs.getDate("fecha_publicacion"))));
                }
            }
        }
        return items;
    }

    public void guardarCartaRespuesta(
            Long idExpediente,
            DocumentoAnalizadoDTO carta,
            Long idUsuario) throws SQLException {
        if (idExpediente == null || carta == null || !hasText(carta.getTipoDocumentoCodigo())) {
            throw new IllegalArgumentException("Seleccione el tipo de documento de la carta de respuesta.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            Long idTipoDocumento = catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, carta.getTipoDocumentoCodigo());
            if (idTipoDocumento == null) {
                throw new SQLException("No se encontró el tipo de documento: " + carta.getTipoDocumentoCodigo() + ".");
            }
            boolean soportaAnalisisMultiple = soportaAnalisisMultiple(conn);
            boolean soportaOposicion = soportaExisteOposicion(conn);
            Long idDocumento = carta.getIdDocumentoAnalizado();
            if (idDocumento == null || idDocumento.longValue() < 0L) {
                insertarCartaRespuesta(conn, idExpediente, carta, idTipoDocumento, soportaAnalisisMultiple, soportaOposicion, idUsuario);
            } else {
                actualizarCartaRespuesta(conn, idExpediente, carta, soportaOposicion, idUsuario);
            }
        }
    }

    private void insertarCartaRespuesta(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO carta,
            Long idTipoDocumento,
            boolean soportaAnalisisMultiple,
            boolean soportaOposicion,
            Long idUsuario) throws SQLException {
        String sql = "INSERT INTO expediente_documento_analizado ("
                + "id_expediente, "
                + (soportaAnalisisMultiple ? "id_expediente_analisis, " : "")
                + "id_documento_padre, nivel, orden, id_tipo_documento_adjunto, "
                + "fecha_documento, confirmacion_respuesta, fecha_respuesta, numero_hoja_envio_respuesta, "
                + (soportaOposicion ? "existe_oposicion, " : "")
                + "activo, creado_por, creado_en"
                + ") VALUES (?, " + (soportaAnalisisMultiple ? "?, " : "")
                + "?, ?, 0, ?, ?, ?, ?, ?, " + (soportaOposicion ? "?, " : "")
                + "1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            ps.setLong(index++, idExpediente);
            if (soportaAnalisisMultiple) {
                setLongOrNull(ps, index++, carta.getIdExpedienteAnalisis());
            }
            setLongOrNull(ps, index++, carta.getIdDocumentoPadre());
            ps.setInt(index++, 1);
            ps.setLong(index++, idTipoDocumento);
            setDateOrNull(ps, index++, carta.getFechaDocumento());
            setStringOrNull(ps, index++, emptyToNull(carta.getConfirmacionRespuesta()));
            setDateOrNull(ps, index++, carta.getFechaRespuesta());
            setStringOrNull(ps, index++, limitar(emptyToNull(carta.getNumeroHojaEnvioRespuesta()), 120));
            if (soportaOposicion) {
                setBooleanOrNull(ps, index++, carta.getExisteOposicion());
            }
            if (idUsuario == null) {
                ps.setNull(index, Types.NUMERIC);
            } else {
                ps.setLong(index, idUsuario);
            }
            ps.executeUpdate();
        }
    }

    private void actualizarCartaRespuesta(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO carta,
            boolean soportaOposicion,
            Long idUsuario) throws SQLException {
        String sql = "UPDATE expediente_documento_analizado SET "
                + "confirmacion_respuesta = ?, fecha_respuesta = ?, numero_hoja_envio_respuesta = ?, "
                + (soportaOposicion ? "existe_oposicion = ?, " : "")
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            setStringOrNull(ps, index++, emptyToNull(carta.getConfirmacionRespuesta()));
            setDateOrNull(ps, index++, carta.getFechaRespuesta());
            setStringOrNull(ps, index++, limitar(emptyToNull(carta.getNumeroHojaEnvioRespuesta()), 120));
            if (soportaOposicion) {
                setBooleanOrNull(ps, index++, carta.getExisteOposicion());
            }
            if (idUsuario == null) {
                ps.setNull(index++, Types.NUMERIC);
            } else {
                ps.setLong(index++, idUsuario);
            }
            ps.setLong(index++, carta.getIdDocumentoAnalizado());
            ps.setLong(index, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar la carta de respuesta.");
            }
        }
    }

    public List<DocumentoAnalizadoDTO> listarPorExpediente(Connection conn, Long idExpediente) throws SQLException {
        return listarPorExpediente(conn, idExpediente, null);
    }

    public List<DocumentoAnalizadoDTO> listarPorExpediente(
            Connection conn,
            Long idExpediente,
            Long idExpedienteAnalisis) throws SQLException {
        List<DocumentoAnalizadoDTO> documentos = new ArrayList<>();
        if (conn == null || idExpediente == null) {
            return documentos;
        }
        boolean soportaRespuesta = soportaRespuestaDocumentoAnalizado(conn);
        boolean soportaPublicacion = soportaPublicacionPreparada(conn);
        boolean soportaNumeroDocumento = soportaNumeroDocumentoAnalizado(conn);
        boolean soportaDetalleObservacion = soportaDetalleObservacionDocumentoAnalizado(conn);
        boolean soportaAnalisisMultiple = soportaAnalisisMultiple(conn);
        boolean soportaJerarquia = soportaJerarquiaDocumentoAnalizado(conn);
        boolean soportaOposicion = soportaExisteOposicion(conn);
        String sql = "SELECT da.id_documento_analizado, da.id_expediente, "
                + (soportaAnalisisMultiple
                        ? "da.id_expediente_analisis, "
                        : "CAST(NULL AS NUMBER) AS id_expediente_analisis, ")
                + (soportaJerarquia
                        ? "da.id_documento_padre, NVL(da.nivel, 0) AS nivel, NVL(da.orden, 0) AS orden, "
                        + "da.estado_respuesta, da.activo, "
                        : "CAST(NULL AS NUMBER) AS id_documento_padre, 0 AS nivel, 0 AS orden, "
                        + "CAST(NULL AS VARCHAR2(40)) AS estado_respuesta, da.activo, ")
                + "da.creado_en, da.modificado_en, "
                + "td.codigo AS tipo_documento_codigo, td.nombre AS tipo_documento_nombre, "
                + "ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento_nombre, "
                + "da.fecha_documento, "
                + (soportaNumeroDocumento
                        ? "da.numero_documento, "
                        : "CAST(NULL AS VARCHAR2(120)) AS numero_documento, ")
                + (soportaDetalleObservacion
                        ? "da.detalle_observacion, "
                        : "CAST(NULL AS VARCHAR2(1000)) AS detalle_observacion, ")
                + "da.descripcion, "
                + (soportaRespuesta
                        ? "NVL(da.notificado, 0) AS notificado, da.fecha_acuse, "
                        + "NVL(da.requiere_respuesta, 0) AS requiere_respuesta, "
                        + "da.confirmacion_respuesta, da.fecha_respuesta, da.numero_hoja_envio_respuesta "
                        : "0 AS notificado, CAST(NULL AS DATE) AS fecha_acuse, "
                        + "0 AS requiere_respuesta, CAST(NULL AS VARCHAR2(20)) AS confirmacion_respuesta, "
                        + "CAST(NULL AS DATE) AS fecha_respuesta, "
                        + "CAST(NULL AS VARCHAR2(120)) AS numero_hoja_envio_respuesta ")
                + (soportaPublicacion
                        ? ", NVL(e.requiere_publicacion, 0) AS requiere_publicacion, "
                        + "(SELECT fecha_publicacion FROM ("
                        + " SELECT p.fecha_publicacion FROM expediente_publicacion p "
                        + " WHERE p.id_expediente = da.id_expediente AND p.activo = 1 "
                        + " ORDER BY p.creado_en DESC, p.id_expediente_publicacion DESC"
                        + ") WHERE ROWNUM = 1) AS fecha_publicacion "
                        : ", 0 AS requiere_publicacion, CAST(NULL AS DATE) AS fecha_publicacion ")
                + (soportaOposicion
                        ? ", da.existe_oposicion "
                        : ", CAST(NULL AS NUMBER(1)) AS existe_oposicion ")
                + "FROM expediente_documento_analizado da "
                + "JOIN expediente e ON e.id_expediente = da.id_expediente "
                + "LEFT JOIN tipo_documento_adjunto td ON td.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                + "WHERE da.id_expediente = ? AND da.activo = 1 "
                + (soportaAnalisisMultiple && idExpedienteAnalisis != null
                        ? "AND da.id_expediente_analisis = ? "
                        : "")
                + (soportaJerarquia
                        ? "ORDER BY NVL(da.id_documento_padre, da.id_documento_analizado), NVL(da.nivel, 0), "
                        + "NVL(da.orden, da.id_documento_analizado), da.id_documento_analizado"
                        : "ORDER BY da.fecha_documento DESC NULLS LAST, da.id_documento_analizado DESC");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            if (soportaAnalisisMultiple && idExpedienteAnalisis != null) {
                ps.setLong(2, idExpedienteAnalisis);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    documentos.add(new DocumentoAnalizadoDTO(
                            getLongOrNull(rs, "id_documento_analizado"),
                            getLongOrNull(rs, "id_expediente"),
                            getLongOrNull(rs, "id_expediente_analisis"),
                            rs.getString("tipo_documento_codigo"),
                            rs.getString("tipo_documento_nombre"),
                            rs.getString("estado_documento_codigo"),
                            rs.getString("estado_documento_nombre"),
                            toLocalDate(rs.getDate("fecha_documento")),
                            rs.getString("numero_documento"),
                            rs.getString("descripcion"),
                            rs.getInt("notificado") == 1,
                            toLocalDate(rs.getDate("fecha_acuse")),
                            rs.getInt("requiere_respuesta") == 1,
                            rs.getString("confirmacion_respuesta"),
                            toLocalDate(rs.getDate("fecha_respuesta")),
                            rs.getString("numero_hoja_envio_respuesta"),
                            rs.getInt("requiere_publicacion") == 1,
                            toLocalDate(rs.getDate("fecha_publicacion")),
                            rs.getString("detalle_observacion"),
                            getLongOrNull(rs, "id_documento_padre"),
                            rs.getInt("nivel"),
                            rs.getInt("orden"),
                            rs.getString("estado_respuesta"),
                            rs.getInt("activo") == 1,
                            "",
                            toLocalDateTime(rs.getTimestamp("creado_en")),
                            "",
                            toLocalDateTime(rs.getTimestamp("modificado_en")),
                            getBooleanOrNull(rs, "existe_oposicion")));
                }
            }
        }
        return documentos;
    }

    public int contarPorExpediente(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT COUNT(*) FROM expediente_documento_analizado WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void guardarDocumentosJerarquicos(
            Connection conn,
            Long idExpediente,
            List<DocumentoAnalizadoDTO> documentos,
            Long idUsuario) throws SQLException {
        if (conn == null || idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente para guardar documentos de análisis.");
        }
        if (!soportaJerarquiaDocumentoAnalizado(conn)) {
            throw new SQLException("Faltan columnas jerárquicas en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 39_patch_documento_analizado_jerarquia.sql.");
        }
        Map<Long, Long> idsTemporales = new HashMap<Long, Long>();
        if (documentos == null) {
            return;
        }
        for (DocumentoAnalizadoDTO documento : documentos) {
            if (documento == null) {
                continue;
            }
            if (!documento.isActivo()) {
                darBajaDocumentoAnalizado(conn, idExpediente, documento.getIdDocumentoAnalizado(), idUsuario);
                continue;
            }
            validarJerarquia(documento, idsTemporales);
            Long idPadre = resolverIdPadreJerarquico(documento, idsTemporales);
            Long idDocumento = documento.getIdDocumentoAnalizado();
            if (idDocumento == null || idDocumento.longValue() < 0L) {
                Long nuevoId = insertarDocumentoAnalizadoJerarquico(conn, idExpediente, documento, idPadre, idUsuario);
                if (idDocumento != null) {
                    idsTemporales.put(idDocumento, nuevoId);
                }
            } else {
                actualizarDocumentoAnalizadoJerarquico(conn, idExpediente, documento, idPadre, idUsuario);
            }
        }
    }

    public void darBajaDocumentoAnalizado(
            Connection conn,
            Long idExpediente,
            Long idDocumentoAnalizado,
            Long idUsuarioModificador) throws SQLException {
        if (conn == null || idExpediente == null || idDocumentoAnalizado == null || idDocumentoAnalizado.longValue() < 0L) {
            return;
        }
        String sql = "UPDATE expediente_documento_analizado SET activo = 0, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (idUsuarioModificador == null) {
                ps.setNull(1, Types.NUMERIC);
            } else {
                ps.setLong(1, idUsuarioModificador);
            }
            ps.setLong(2, idDocumentoAnalizado);
            ps.setLong(3, idExpediente);
            ps.executeUpdate();
        }
    }

    public void actualizarRespuestaDocumentoAnalizado(
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioModificador) throws SQLException {
        if (idExpediente == null || documento == null || documento.getIdDocumentoAnalizado() == null) {
            throw new IllegalArgumentException("Seleccione un documento analizado para actualizar la respuesta.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                actualizarRespuestaDocumentoAnalizado(conn, idExpediente, documento, idUsuarioModificador);
                actualizarPublicacionPreparada(conn, idExpediente, documento, idUsuarioModificador);
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
    }

    private void actualizarPublicacionPreparada(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioModificador) throws SQLException {
        if (!soportaPublicacionPreparada(conn)) {
            if (documento.isRequierePublicacion() || documento.getFechaPublicacion() != null) {
                throw new SQLException("La base de datos no tiene soporte completo de publicación preparada en EXPEDIENTE/EXPEDIENTE_PUBLICACION.");
            }
            return;
        }
        String sqlExpediente = "UPDATE expediente SET requiere_publicacion = ?, "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sqlExpediente)) {
            ps.setInt(1, documento.isRequierePublicacion() ? 1 : 0);
            if (idUsuarioModificador == null) {
                ps.setNull(2, Types.NUMERIC);
            } else {
                ps.setLong(2, idUsuarioModificador);
            }
            ps.setLong(3, idExpediente);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo actualizar el indicador de publicación del expediente.");
            }
        }
        if (!documento.isRequierePublicacion() && documento.getFechaPublicacion() == null) {
            return;
        }
        Long idPublicacion = obtenerPublicacionActiva(conn, idExpediente);
        if (idPublicacion == null) {
            insertarPublicacionPreparada(conn, idExpediente, documento.getFechaPublicacion(), idUsuarioModificador);
        } else {
            actualizarPublicacionPreparada(conn, idPublicacion, documento.getFechaPublicacion(), idUsuarioModificador);
        }
    }

    public void actualizarRespuestaDocumentoAnalizado(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioModificador) throws SQLException {
        if (!soportaRespuestaDocumentoAnalizado(conn)) {
            throw new SQLException("Faltan columnas de respuesta en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 29_patch_documento_analizado_respuesta.sql.");
        }
        String confirmacion = normalizarConfirmacionRespuesta(documento.getConfirmacionRespuesta());
        LocalDate fechaRespuesta = documento.getFechaRespuesta();
        String hojaRespuesta = limitar(emptyToNull(documento.getNumeroHojaEnvioRespuesta()), 120);
        if (!documento.isRequiereRespuesta()) {
            confirmacion = null;
            fechaRespuesta = null;
            hojaRespuesta = null;
        } else if (!documento.isNotificado() || documento.getFechaAcuse() == null) {
            confirmacion = "PENDIENTE";
            fechaRespuesta = null;
            hojaRespuesta = null;
        }
        String sql = "UPDATE expediente_documento_analizado SET "
                + "notificado = ?, fecha_acuse = ?, confirmacion_respuesta = ?, fecha_respuesta = ?, "
                + "numero_hoja_envio_respuesta = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, documento.isNotificado() ? 1 : 0);
            setDateOrNull(ps, 2, documento.getFechaAcuse());
            if (confirmacion == null) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, confirmacion);
            }
            setDateOrNull(ps, 4, fechaRespuesta);
            if (hojaRespuesta == null) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, hojaRespuesta);
            }
            if (idUsuarioModificador == null) {
                ps.setNull(6, Types.NUMERIC);
            } else {
                ps.setLong(6, idUsuarioModificador);
            }
            ps.setLong(7, documento.getIdDocumentoAnalizado());
            ps.setLong(8, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar la respuesta del documento analizado.");
            }
        }
    }

    public void insertarDocumentoAnalizado(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioCreador) throws SQLException {
        Long idTipoDocumento = catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, documento.getTipoDocumentoCodigo());
        Long idEstadoDocumento = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, documento.getEstadoDocumentoCodigo());
        if (idTipoDocumento == null) {
            throw new SQLException("No se encontró el tipo de documento analizado: " + documento.getTipoDocumentoCodigo() + ".");
        }
        if (idEstadoDocumento == null) {
            throw new SQLException("No se encontró el estado de documento: " + documento.getEstadoDocumentoCodigo() + ".");
        }
        boolean soportaRespuesta = soportaRespuestaDocumentoAnalizado(conn);
        boolean soportaNumeroDocumento = soportaNumeroDocumentoAnalizado(conn);
        boolean soportaDetalleObservacion = soportaDetalleObservacionDocumentoAnalizado(conn);
        boolean soportaAnalisisMultiple = soportaAnalisisMultiple(conn);
        if (!soportaNumeroDocumento && hasText(documento.getNumeroDocumento())) {
            throw new SQLException("Falta la columna NUMERO_DOCUMENTO en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 32_patch_documento_analizado_numero_documento.sql.");
        }
        if (!soportaDetalleObservacion && hasText(documento.getDetalleObservacion())) {
            throw new SQLException("Falta la columna DETALLE_OBSERVACION en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 33_patch_documento_analizado_detalle_observacion.sql.");
        }
        if (!soportaRespuesta && tieneDatosRespuesta(documento)) {
            throw new SQLException("Faltan columnas de respuesta en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 29_patch_documento_analizado_respuesta.sql.");
        }
        String sql = soportaRespuesta
                ? "INSERT INTO expediente_documento_analizado ("
                + "id_expediente, "
                + (soportaAnalisisMultiple ? "id_expediente_analisis, " : "")
                + "id_tipo_documento_adjunto, id_estado_documento, fecha_documento, "
                + (soportaNumeroDocumento ? "numero_documento, " : "")
                + (soportaDetalleObservacion ? "detalle_observacion, " : "")
                + "descripcion, notificado, fecha_acuse, requiere_respuesta, confirmacion_respuesta, "
                + "fecha_respuesta, numero_hoja_envio_respuesta, activo, creado_por, creado_en"
                + ") VALUES (?, " + (soportaAnalisisMultiple ? "?, " : "") + "?, ?, ?, " + (soportaNumeroDocumento ? "?, " : "")
                + (soportaDetalleObservacion ? "?, " : "") + "?, ?, ?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)"
                : "INSERT INTO expediente_documento_analizado ("
                + "id_expediente, "
                + (soportaAnalisisMultiple ? "id_expediente_analisis, " : "")
                + "id_tipo_documento_adjunto, id_estado_documento, fecha_documento, "
                + (soportaNumeroDocumento ? "numero_documento, " : "")
                + (soportaDetalleObservacion ? "detalle_observacion, " : "")
                + "descripcion, activo, creado_por, creado_en"
                + ") VALUES (?, " + (soportaAnalisisMultiple ? "?, " : "") + "?, ?, ?, " + (soportaNumeroDocumento ? "?, " : "")
                + (soportaDetalleObservacion ? "?, " : "") + "?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            ps.setLong(index++, idExpediente);
            if (soportaAnalisisMultiple) {
                setLongOrNull(ps, index++, documento.getIdExpedienteAnalisis());
            }
            ps.setLong(index++, idTipoDocumento);
            ps.setLong(index++, idEstadoDocumento);
            setDateOrNull(ps, index++, documento.getFechaDocumento());
            if (soportaNumeroDocumento) {
                ps.setString(index++, limitar(documento.getNumeroDocumento(), 120));
            }
            if (soportaDetalleObservacion) {
                setStringOrNull(ps, index++, detalleObservacionPersistencia(documento));
            }
            ps.setString(index++, limitar(documento.getDescripcion(), 1000));
            int usuarioIndex;
            if (soportaRespuesta) {
                RespuestaPersistencia respuesta = respuestaPersistencia(documento, true);
                ps.setInt(index++, documento.isNotificado() ? 1 : 0);
                setDateOrNull(ps, index++, documento.getFechaAcuse());
                ps.setInt(index++, documento.isRequiereRespuesta() ? 1 : 0);
                setStringOrNull(ps, index++, respuesta.confirmacion);
                setDateOrNull(ps, index++, respuesta.fechaRespuesta);
                setStringOrNull(ps, index++, respuesta.hojaRespuesta);
                usuarioIndex = index;
            } else {
                usuarioIndex = index;
            }
            if (idUsuarioCreador == null) {
                ps.setNull(usuarioIndex, java.sql.Types.NUMERIC);
            } else {
                ps.setLong(usuarioIndex, idUsuarioCreador);
            }
            ps.executeUpdate();
        }
    }

    public void actualizarDocumentoAnalizado(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioModificador) throws SQLException {
        Long idTipoDocumento = catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, documento.getTipoDocumentoCodigo());
        Long idEstadoDocumento = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, documento.getEstadoDocumentoCodigo());
        if (idTipoDocumento == null) {
            throw new SQLException("No se encontró el tipo de documento analizado: " + documento.getTipoDocumentoCodigo() + ".");
        }
        if (idEstadoDocumento == null) {
            throw new SQLException("No se encontró el estado de documento: " + documento.getEstadoDocumentoCodigo() + ".");
        }
        boolean soportaRespuesta = soportaRespuestaDocumentoAnalizado(conn);
        boolean soportaNumeroDocumento = soportaNumeroDocumentoAnalizado(conn);
        boolean soportaDetalleObservacion = soportaDetalleObservacionDocumentoAnalizado(conn);
        boolean soportaAnalisisMultiple = soportaAnalisisMultiple(conn);
        if (!soportaNumeroDocumento && hasText(documento.getNumeroDocumento())) {
            throw new SQLException("Falta la columna NUMERO_DOCUMENTO en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 32_patch_documento_analizado_numero_documento.sql.");
        }
        if (!soportaDetalleObservacion && hasText(documento.getDetalleObservacion())) {
            throw new SQLException("Falta la columna DETALLE_OBSERVACION en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 33_patch_documento_analizado_detalle_observacion.sql.");
        }
        if (!soportaRespuesta && tieneDatosRespuesta(documento)) {
            throw new SQLException("Faltan columnas de respuesta en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 29_patch_documento_analizado_respuesta.sql.");
        }
        String sql = soportaRespuesta
                ? "UPDATE expediente_documento_analizado SET "
                + (soportaAnalisisMultiple ? "id_expediente_analisis = ?, " : "")
                + "id_tipo_documento_adjunto = ?, id_estado_documento = ?, fecha_documento = ?, "
                + (soportaNumeroDocumento ? "numero_documento = ?, " : "")
                + (soportaDetalleObservacion ? "detalle_observacion = ?, " : "")
                + "descripcion = ?, "
                + "notificado = ?, fecha_acuse = ?, requiere_respuesta = ?, confirmacion_respuesta = ?, "
                + "fecha_respuesta = ?, numero_hoja_envio_respuesta = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1"
                : "UPDATE expediente_documento_analizado SET "
                + (soportaAnalisisMultiple ? "id_expediente_analisis = ?, " : "")
                + "id_tipo_documento_adjunto = ?, id_estado_documento = ?, fecha_documento = ?, "
                + (soportaNumeroDocumento ? "numero_documento = ?, " : "")
                + (soportaDetalleObservacion ? "detalle_observacion = ?, " : "")
                + "descripcion = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            if (soportaAnalisisMultiple) {
                setLongOrNull(ps, index++, documento.getIdExpedienteAnalisis());
            }
            ps.setLong(index++, idTipoDocumento);
            ps.setLong(index++, idEstadoDocumento);
            setDateOrNull(ps, index++, documento.getFechaDocumento());
            if (soportaNumeroDocumento) {
                ps.setString(index++, limitar(documento.getNumeroDocumento(), 120));
            }
            if (soportaDetalleObservacion) {
                setStringOrNull(ps, index++, detalleObservacionPersistencia(documento));
            }
            ps.setString(index++, limitar(documento.getDescripcion(), 1000));
            int userIndex;
            int idIndex;
            if (soportaRespuesta) {
                RespuestaPersistencia respuesta = respuestaPersistencia(documento, true);
                ps.setInt(index++, documento.isNotificado() ? 1 : 0);
                setDateOrNull(ps, index++, documento.getFechaAcuse());
                ps.setInt(index++, documento.isRequiereRespuesta() ? 1 : 0);
                setStringOrNull(ps, index++, respuesta.confirmacion);
                setDateOrNull(ps, index++, respuesta.fechaRespuesta);
                setStringOrNull(ps, index++, respuesta.hojaRespuesta);
                userIndex = index++;
                idIndex = index;
            } else {
                userIndex = index++;
                idIndex = index;
            }
            if (idUsuarioModificador == null) {
                ps.setNull(userIndex, Types.NUMERIC);
            } else {
                ps.setLong(userIndex, idUsuarioModificador);
            }
            ps.setLong(idIndex, documento.getIdDocumentoAnalizado());
            ps.setLong(idIndex + 1, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar el documento analizado.");
            }
        }
    }

    private Long insertarDocumentoAnalizadoJerarquico(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idDocumentoPadre,
            Long idUsuarioCreador) throws SQLException {
        Long idTipoDocumento = catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, documento.getTipoDocumentoCodigo());
        Long idEstadoDocumento = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, documento.getEstadoDocumentoCodigo());
        if (idTipoDocumento == null) {
            throw new SQLException("No se encontró el tipo de documento analizado: " + documento.getTipoDocumentoCodigo() + ".");
        }
        if (idEstadoDocumento == null) {
            throw new SQLException("No se encontró el estado de documento: " + documento.getEstadoDocumentoCodigo() + ".");
        }
        boolean soportaAnalisisMultiple = soportaAnalisisMultiple(conn);
        boolean soportaDetalleObservacion = soportaDetalleObservacionDocumentoAnalizado(conn);
        String sql = "INSERT INTO expediente_documento_analizado ("
                + "id_expediente, "
                + (soportaAnalisisMultiple ? "id_expediente_analisis, " : "")
                + "id_documento_padre, nivel, orden, id_tipo_documento_adjunto, id_estado_documento, "
                + "fecha_documento, numero_documento, "
                + (soportaDetalleObservacion ? "detalle_observacion, " : "")
                + "descripcion, "
                + "notificado, fecha_acuse, requiere_respuesta, confirmacion_respuesta, "
                + "fecha_respuesta, numero_hoja_envio_respuesta, estado_respuesta, "
                + "activo, creado_por, creado_en"
                + ") VALUES (?, " + (soportaAnalisisMultiple ? "?, " : "")
                + "?, ?, ?, ?, ?, ?, ?, " + (soportaDetalleObservacion ? "?, " : "")
                + "?, ?, ?, ?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_DOCUMENTO_ANALIZADO"})) {
            int index = 1;
            ps.setLong(index++, idExpediente);
            if (soportaAnalisisMultiple) {
                setLongOrNull(ps, index++, documento.getIdExpedienteAnalisis());
            }
            setLongOrNull(ps, index++, idDocumentoPadre);
            ps.setInt(index++, documento.getNivel());
            ps.setInt(index++, documento.getOrden());
            ps.setLong(index++, idTipoDocumento);
            ps.setLong(index++, idEstadoDocumento);
            setDateOrNull(ps, index++, documento.getFechaDocumento());
            ps.setString(index++, limitar(documento.getNumeroDocumento(), 120));
            if (soportaDetalleObservacion) {
                setStringOrNull(ps, index++, detalleObservacionPersistencia(documento));
            }
            ps.setString(index++, limitar(documento.getDescripcion(), 1000));
            RespuestaPersistencia respuesta = respuestaPersistencia(documento, true);
            ps.setInt(index++, documento.isNotificado() ? 1 : 0);
            setDateOrNull(ps, index++, documento.getFechaAcuse());
            ps.setInt(index++, documento.isRequiereRespuesta() ? 1 : 0);
            setStringOrNull(ps, index++, respuesta.confirmacion);
            setDateOrNull(ps, index++, respuesta.fechaRespuesta);
            setStringOrNull(ps, index++, respuesta.hojaRespuesta);
            setStringOrNull(ps, index++, estadoRespuestaPersistencia(documento));
            if (idUsuarioCreador == null) {
                ps.setNull(index, Types.NUMERIC);
            } else {
                ps.setLong(index, idUsuarioCreador);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el identificador generado del documento analizado.");
    }

    private void actualizarDocumentoAnalizadoJerarquico(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idDocumentoPadre,
            Long idUsuarioModificador) throws SQLException {
        Long idTipoDocumento = catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, documento.getTipoDocumentoCodigo());
        Long idEstadoDocumento = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, documento.getEstadoDocumentoCodigo());
        if (idTipoDocumento == null) {
            throw new SQLException("No se encontró el tipo de documento analizado: " + documento.getTipoDocumentoCodigo() + ".");
        }
        if (idEstadoDocumento == null) {
            throw new SQLException("No se encontró el estado de documento: " + documento.getEstadoDocumentoCodigo() + ".");
        }
        boolean soportaAnalisisMultiple = soportaAnalisisMultiple(conn);
        boolean soportaDetalleObservacion = soportaDetalleObservacionDocumentoAnalizado(conn);
        String sql = "UPDATE expediente_documento_analizado SET "
                + (soportaAnalisisMultiple ? "id_expediente_analisis = ?, " : "")
                + "id_documento_padre = ?, nivel = ?, orden = ?, "
                + "id_tipo_documento_adjunto = ?, id_estado_documento = ?, fecha_documento = ?, "
                + "numero_documento = ?, "
                + (soportaDetalleObservacion ? "detalle_observacion = ?, " : "")
                + "descripcion = ?, "
                + "notificado = ?, fecha_acuse = ?, requiere_respuesta = ?, confirmacion_respuesta = ?, "
                + "fecha_respuesta = ?, numero_hoja_envio_respuesta = ?, estado_respuesta = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            if (soportaAnalisisMultiple) {
                setLongOrNull(ps, index++, documento.getIdExpedienteAnalisis());
            }
            setLongOrNull(ps, index++, idDocumentoPadre);
            ps.setInt(index++, documento.getNivel());
            ps.setInt(index++, documento.getOrden());
            ps.setLong(index++, idTipoDocumento);
            ps.setLong(index++, idEstadoDocumento);
            setDateOrNull(ps, index++, documento.getFechaDocumento());
            ps.setString(index++, limitar(documento.getNumeroDocumento(), 120));
            if (soportaDetalleObservacion) {
                setStringOrNull(ps, index++, detalleObservacionPersistencia(documento));
            }
            ps.setString(index++, limitar(documento.getDescripcion(), 1000));
            RespuestaPersistencia respuesta = respuestaPersistencia(documento, true);
            ps.setInt(index++, documento.isNotificado() ? 1 : 0);
            setDateOrNull(ps, index++, documento.getFechaAcuse());
            ps.setInt(index++, documento.isRequiereRespuesta() ? 1 : 0);
            setStringOrNull(ps, index++, respuesta.confirmacion);
            setDateOrNull(ps, index++, respuesta.fechaRespuesta);
            setStringOrNull(ps, index++, respuesta.hojaRespuesta);
            setStringOrNull(ps, index++, estadoRespuestaPersistencia(documento));
            if (idUsuarioModificador == null) {
                ps.setNull(index++, Types.NUMERIC);
            } else {
                ps.setLong(index++, idUsuarioModificador);
            }
            ps.setLong(index++, documento.getIdDocumentoAnalizado());
            ps.setLong(index, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar el documento analizado.");
            }
        }
    }

    public void actualizarEstadoDocumentoAnalizado(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioModificador) throws SQLException {
        Long idEstadoDocumento = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, documento.getEstadoDocumentoCodigo());
        if (idEstadoDocumento == null) {
            throw new SQLException("No se encontró el estado de documento: " + documento.getEstadoDocumentoCodigo() + ".");
        }
        String sql = "UPDATE expediente_documento_analizado SET "
                + "id_estado_documento = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEstadoDocumento);
            if (idUsuarioModificador == null) {
                ps.setNull(2, java.sql.Types.NUMERIC);
            } else {
                ps.setLong(2, idUsuarioModificador);
            }
            ps.setLong(3, documento.getIdDocumentoAnalizado());
            ps.setLong(4, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar el estado del documento analizado.");
            }
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static Boolean getBooleanOrNull(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value == 1;
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static void setDateOrNull(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(date));
        }
    }

    private static void setStringOrNull(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

    private static void setBooleanOrNull(PreparedStatement ps, int index, Boolean value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setInt(index, value.booleanValue() ? 1 : 0);
        }
    }

    private static void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private static boolean soportaRespuestaDocumentoAnalizado(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT column_name) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                + "AND column_name IN ("
                + "'NOTIFICADO', 'FECHA_ACUSE', 'REQUIERE_RESPUESTA', "
                + "'CONFIRMACION_RESPUESTA', 'FECHA_RESPUESTA', 'NUMERO_HOJA_ENVIO_RESPUESTA'"
                + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 6;
        }
    }

    private static boolean soportaNumeroDocumentoAnalizado(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                + "AND column_name = 'NUMERO_DOCUMENTO'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static boolean soportaDetalleObservacionDocumentoAnalizado(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                + "AND column_name = 'DETALLE_OBSERVACION'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static boolean soportaExisteOposicion(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                + "AND column_name = 'EXISTE_OPOSICION'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static boolean soportaClasificacionTipoDocumento(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'TIPO_DOCUMENTO_ADJUNTO' "
                + "AND column_name = 'CLASIFICACION'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static boolean soportaJerarquiaDocumentoAnalizado(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT column_name) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                + "AND column_name IN ('ID_DOCUMENTO_PADRE', 'NIVEL', 'ORDEN', 'ESTADO_RESPUESTA')";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 4;
        }
    }

    private static boolean soportaAnalisisMultiple(Connection conn) throws SQLException {
        String sql = "SELECT "
                + "(SELECT COUNT(1) FROM user_tables WHERE table_name = 'EXPEDIENTE_ANALISIS') + "
                + "(SELECT COUNT(1) FROM user_tab_columns WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' AND column_name = 'ID_EXPEDIENTE_ANALISIS') "
                + "AS total FROM dual";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt("total") == 2;
        }
    }

    private static boolean soportaPublicacionPreparada(Connection conn) throws SQLException {
        String sql = "SELECT "
                + "(SELECT COUNT(1) FROM user_tab_columns WHERE table_name = 'EXPEDIENTE' AND column_name = 'REQUIERE_PUBLICACION') + "
                + "(SELECT COUNT(1) FROM user_tables WHERE table_name = 'EXPEDIENTE_PUBLICACION') + "
                + "(SELECT COUNT(1) FROM user_tab_columns WHERE table_name = 'EXPEDIENTE_PUBLICACION' AND column_name = 'FECHA_PUBLICACION') "
                + "AS total FROM dual";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt("total") == 3;
        }
    }

    private static Long obtenerPublicacionActiva(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT id_expediente_publicacion FROM ("
                + "SELECT id_expediente_publicacion FROM expediente_publicacion "
                + "WHERE id_expediente = ? AND activo = 1 "
                + "ORDER BY creado_en DESC, id_expediente_publicacion DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return getLongOrNull(rs, "id_expediente_publicacion");
            }
        }
    }

    private static void insertarPublicacionPreparada(
            Connection conn,
            Long idExpediente,
            LocalDate fechaPublicacion,
            Long idUsuario) throws SQLException {
        String sql = "INSERT INTO expediente_publicacion ("
                + "id_expediente, tipo_publicacion, estado_publicacion, fecha_generacion, fecha_publicacion, "
                + "observacion, activo, creado_por, creado_en"
                + ") VALUES (?, 'CARTA_RESPUESTA', 'PENDIENTE_PUBLICACION', SYSDATE, ?, "
                + "'Publicación preparada desde Asignación - Cartas de respuesta.', 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            setDateOrNull(ps, 2, fechaPublicacion);
            if (idUsuario == null) {
                ps.setNull(3, Types.NUMERIC);
            } else {
                ps.setLong(3, idUsuario);
            }
            ps.executeUpdate();
        }
    }

    private static void actualizarPublicacionPreparada(
            Connection conn,
            Long idPublicacion,
            LocalDate fechaPublicacion,
            Long idUsuario) throws SQLException {
        String sql = "UPDATE expediente_publicacion SET "
                + "tipo_publicacion = NVL(tipo_publicacion, 'CARTA_RESPUESTA'), "
                + "estado_publicacion = CASE WHEN estado_publicacion IS NULL THEN 'PENDIENTE_PUBLICACION' ELSE estado_publicacion END, "
                + "fecha_publicacion = ?, "
                + "observacion = NVL(observacion, 'Publicación preparada desde Asignación - Cartas de respuesta.'), "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente_publicacion = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setDateOrNull(ps, 1, fechaPublicacion);
            if (idUsuario == null) {
                ps.setNull(2, Types.NUMERIC);
            } else {
                ps.setLong(2, idUsuario);
            }
            ps.setLong(3, idPublicacion);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo actualizar la publicación preparada.");
            }
        }
    }

    private static String normalizarConfirmacionRespuesta(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
        normalized = normalized.replace('Í', 'I');
        if ("SI".equals(normalized) || "NO".equals(normalized) || "PENDIENTE".equals(normalized)) {
            return normalized;
        }
        return "PENDIENTE";
    }

    private static RespuestaPersistencia respuestaPersistencia(
            DocumentoAnalizadoDTO documento,
            boolean incluirRequiereRespuesta) {
        boolean requiereRespuesta = incluirRequiereRespuesta && documento.isRequiereRespuesta();
        String confirmacion = requiereRespuesta
                ? normalizarConfirmacionRespuesta(documento.getConfirmacionRespuesta())
                : null;
        LocalDate fechaRespuesta = requiereRespuesta ? documento.getFechaRespuesta() : null;
        String hojaRespuesta = requiereRespuesta ? limitar(emptyToNull(documento.getNumeroHojaEnvioRespuesta()), 120) : null;
        return new RespuestaPersistencia(confirmacion, fechaRespuesta, hojaRespuesta);
    }

    private static boolean tieneDatosRespuesta(DocumentoAnalizadoDTO documento) {
        return documento.isNotificado()
                || documento.getFechaAcuse() != null
                || documento.isRequiereRespuesta()
                || !emptyString(documento.getConfirmacionRespuesta()).isEmpty()
                || documento.getFechaRespuesta() != null
                || !emptyString(documento.getNumeroHojaEnvioRespuesta()).isEmpty();
    }

    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static String emptyString(String value) {
        return value == null ? "" : value.trim();
    }

    private static String detalleObservacionPersistencia(DocumentoAnalizadoDTO documento) {
        return esEstadoObservado(documento.getEstadoDocumentoCodigo())
                ? limitar(emptyToNull(documento.getDetalleObservacion()), 1000)
                : null;
    }

    private static List<CatalogoItemDTO> filtrarEstadosDocumentoAnalizado(List<CatalogoItemDTO> estados) {
        List<CatalogoItemDTO> filtrados = new ArrayList<CatalogoItemDTO>();
        if (estados == null) {
            return filtrados;
        }
        for (CatalogoItemDTO estado : estados) {
            if (estado != null && esEstadoDocumentoAnalizadoPermitido(estado.getCodigo())) {
                filtrados.add(estado);
            }
        }
        return filtrados;
    }

    private static boolean esEstadoDocumentoAnalizadoPermitido(String codigo) {
        String value = codigo == null ? "" : codigo.trim().toUpperCase();
        return "EN_PROYECTO".equals(value)
                || "EN_DESPACHO".equals(value)
                || "EMITIDO".equals(value)
                || "OBSERVADO".equals(value);
    }

    private static boolean esEstadoObservado(String codigo) {
        return "OBSERVADO".equalsIgnoreCase(codigo == null ? "" : codigo.trim());
    }

    private static void validarJerarquia(
            DocumentoAnalizadoDTO documento,
            Map<Long, Long> idsTemporales) throws SQLException {
        if (documento.getNivel() < 0 || documento.getNivel() > 1) {
            throw new SQLException("Solo se permiten documentos principales e hijos directos.");
        }
        if (documento.getNivel() == 0 && documento.getIdDocumentoPadre() != null) {
            throw new SQLException("Un documento principal no debe tener documento padre.");
        }
        if (documento.getNivel() == 1) {
            Long idPadre = documento.getIdDocumentoPadre();
            if (idPadre == null) {
                throw new SQLException("Toda respuesta debe estar asociada a un documento analizado que requiere respuesta.");
            }
            if (idPadre.longValue() < 0L && (idsTemporales == null || !idsTemporales.containsKey(idPadre))) {
                throw new SQLException("El documento padre debe guardarse antes de registrar el documento hijo.");
            }
        }
    }

    private static Long resolverIdPadreJerarquico(
            DocumentoAnalizadoDTO documento,
            Map<Long, Long> idsTemporales) {
        Long idPadre = documento.getIdDocumentoPadre();
        if (idPadre == null) {
            return null;
        }
        if (idPadre.longValue() < 0L && idsTemporales != null) {
            return idsTemporales.get(idPadre);
        }
        return idPadre;
    }

    private static String estadoRespuestaPersistencia(DocumentoAnalizadoDTO documento) {
        String estado = documento.getEstadoRespuesta();
        if (estado != null && !estado.trim().isEmpty()) {
            return limitar(estado.trim().toUpperCase(java.util.Locale.ROOT), 40);
        }
        return documento.isRequiereRespuesta() ? "PENDIENTE" : null;
    }

    private static String nombrePersona(String alias) {
        return "TRIM(REGEXP_REPLACE(NVL(" + alias + ".nombres, '') || ' ' || "
                + "NVL(" + alias + ".apellido_paterno, '') || ' ' || NVL(" + alias + ".apellido_materno, ''), '\\s+', ' '))";
    }

    private static void rollbackSilencioso(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // rollback de contingencia
        }
    }

    private static String limitar(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static final class RespuestaPersistencia {
        private final String confirmacion;
        private final LocalDate fechaRespuesta;
        private final String hojaRespuesta;

        private RespuestaPersistencia(String confirmacion, LocalDate fechaRespuesta, String hojaRespuesta) {
            this.confirmacion = confirmacion;
            this.fechaRespuesta = fechaRespuesta;
            this.hojaRespuesta = hojaRespuesta;
        }
    }
}
