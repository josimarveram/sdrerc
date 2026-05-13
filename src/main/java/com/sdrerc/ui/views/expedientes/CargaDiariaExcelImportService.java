package com.sdrerc.ui.views.expedientes;

import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.application.ExpedienteService;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.infrastructure.repository.ExpedienteRepository;
import com.sdrerc.shared.session.SessionContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CargaDiariaExcelImportService {

    private static final String HOJA_CARGA_DIARIA = "trabajado final";
    private static final int ESTADO_REGISTRO_EXPEDIENTE = 56;

    private final CatalogoItemService catalogoItemService = new CatalogoItemService();
    private final ExpedienteService expedienteService = new ExpedienteService();
    private final ExpedienteRepository expedienteRepository = new ExpedienteRepository();
    private final DataFormatter formatter = new DataFormatter(new Locale("es", "PE"));
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
    private final Map<String, CatalogoItem> tiposSolicitud = new HashMap<>();
    private final Map<String, CatalogoItem> tiposDocumento = new HashMap<>();
    private final Map<String, CatalogoItem> procedimientos = new HashMap<>();
    private final Map<String, CatalogoItem> tiposActa = new HashMap<>();

    public CargaDiariaExcelImportService() {
        formatoFecha.setLenient(false);
        cargarCatalogos();
    }

    public CargaDiariaImportResult previsualizar(File archivo) throws IOException, SQLException {
        try (FileInputStream in = new FileInputStream(archivo); Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = obtenerHojaCargaDiaria(workbook);
            if (sheet == null) {
                throw new IllegalArgumentException("El archivo seleccionado no contiene la hoja 'trabajado final'.");
            }

            int headerRowIndex = encontrarFilaCabecera(sheet);
            if (headerRowIndex < 0) {
                throw new IllegalArgumentException("No se encontraron los encabezados esperados en la hoja 'trabajado final'.");
            }

            Map<String, Integer> columnas = mapearCabeceras(sheet.getRow(headerRowIndex));
            validarCabecerasMinimas(columnas);

            CargaDiariaImportResult result = new CargaDiariaImportResult();
            Set<String> clavesLeidas = new HashSet<>();
            for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (filaVacia(row)) {
                    continue;
                }
                CargaDiariaExcelRow item = leerFila(row, columnas);
                validarDuplicidad(item, clavesLeidas);
                if (CargaDiariaExcelRow.ESTADO_VALIDO.equals(item.getEstadoValidacion())) {
                    item.addInfo("Registro válido para importar.");
                }
                result.addFila(item);
            }
            return result;
        }
    }

    public CargaDiariaImportResult importarValidos(CargaDiariaImportResult preview) throws Exception {
        int registrados = 0;
        int omitidos = 0;
        for (CargaDiariaExcelRow fila : preview.getFilas()) {
            if (!fila.esImportable()) {
                fila.setResultadoCarga(
                        CargaDiariaExcelRow.CARGA_OMITIDO,
                        "No importado por estado de validación " + fila.getEstadoValidacion() + ".");
                omitidos++;
                continue;
            }
            try {
                expedienteService.agregarExpediente(crearExpediente(fila));
                fila.setResultadoCarga(
                        CargaDiariaExcelRow.CARGA_REGISTRADO,
                        "Expediente registrado correctamente.");
                registrados++;
            } catch (Exception ex) {
                fila.setResultadoCarga(
                        CargaDiariaExcelRow.CARGA_ERROR,
                        "No se pudo registrar: " + mensajeError(ex));
                omitidos++;
            }
        }
        preview.setRegistrados(registrados);
        preview.setOmitidos(omitidos);
        return preview;
    }

    private String mensajeError(Exception ex) {
        if (ex == null || ex.getMessage() == null || ex.getMessage().trim().isEmpty()) {
            return "Error no especificado.";
        }
        return ex.getMessage().trim();
    }

    private void cargarCatalogos() {
        cargarMapaCatalogo(1, tiposSolicitud);
        cargarMapaCatalogo(2, tiposDocumento);
        cargarMapaCatalogo(3, procedimientos);
        cargarMapaCatalogo(4, tiposActa);
    }

    private void cargarMapaCatalogo(int idCatalogo, Map<String, CatalogoItem> destino) {
        destino.clear();
        List<CatalogoItem> items = catalogoItemService.listarCatalogoItem(idCatalogo);
        for (CatalogoItem item : items) {
            destino.put(normalizarTexto(item.getDescripcion()), item);
        }
    }

    private Sheet obtenerHojaCargaDiaria(Workbook workbook) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (HOJA_CARGA_DIARIA.equalsIgnoreCase(sheet.getSheetName().trim())) {
                return sheet;
            }
        }
        return null;
    }

    private int encontrarFilaCabecera(Sheet sheet) {
        int max = Math.min(sheet.getLastRowNum(), 12);
        for (int i = sheet.getFirstRowNum(); i <= max; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            Map<String, Integer> columnas = mapearCabeceras(row);
            if (columnas.containsKey("TIPO DE SOLICITUD")
                    && columnas.containsKey("FECHA DE SOLICITUD")
                    && columnas.containsKey("TITULAR")) {
                return i;
            }
        }
        return -1;
    }

    private Map<String, Integer> mapearCabeceras(Row row) {
        Map<String, Integer> columnas = new HashMap<>();
        if (row == null) {
            return columnas;
        }
        for (Cell cell : row) {
            String header = normalizarEncabezado(valorTexto(cell));
            if (!header.isEmpty()) {
                columnas.put(header, cell.getColumnIndex());
            }
        }
        return columnas;
    }

    private void validarCabecerasMinimas(Map<String, Integer> columnas) {
        String[] requeridas = {
            "TIPO DE SOLICITUD", "FECHA DE SOLICITUD", "SOLICITADO POR", "DNI SOLICITANTE",
            "N TRAMITE WEB", "TIPO DOCUMENTO", "N DOCUMENTO",
            "PROCEDIMIENTO REGISTRAL", "TIPO DE ACTA", "N ACTA", "TITULAR"
        };
        for (String requerida : requeridas) {
            if (!columnas.containsKey(requerida)) {
                throw new IllegalArgumentException("Falta la columna requerida: " + requerida);
            }
        }
    }

    private CargaDiariaExcelRow leerFila(Row row, Map<String, Integer> columnas) {
        CargaDiariaExcelRow item = new CargaDiariaExcelRow(row.getRowNum() + 1);

        item.setTipoSolicitud("");
        item.setFechaSolicitud(parseFecha(row, columnas, "FECHA DE SOLICITUD", item));
        item.setFechaSolicitudTexto(item.getFechaSolicitud() == null ? "" : formatoFecha.format(item.getFechaSolicitud()));
        item.setSolicitadoPor(texto(row, columnas, "SOLICITADO POR"));
        aplicarDniSolicitante(item, texto(row, columnas, "DNI SOLICITANTE"));
        aplicarTramiteYCanal(item, texto(row, columnas, "N TRAMITE WEB"));
        item.setTipoDocumento(texto(row, columnas, "TIPO DOCUMENTO"));
        aplicarNumeroDocumento(item, texto(row, columnas, "N DOCUMENTO"));
        item.setProcedimientoRegistral(textoProcedimientoRegistral(row, columnas));
        item.setTipoActa(texto(row, columnas, "TIPO DE ACTA"));
        item.setNumeroActa(texto(row, columnas, "N ACTA"));
        aplicarTitulares(item, texto(row, columnas, "TITULAR"));
        aplicarDniTitularSiSolicitanteEsTitular(item);

        validarCatalogos(item);
        validarObligatorios(item);
        return item;
    }

    private void aplicarTramiteYCanal(CargaDiariaExcelRow item, String tramiteWeb) {
        if (esTramiteWebReal(tramiteWeb)) {
            item.setCanal("MPV");
            item.setReferencia(tramiteWeb.trim());
            item.addInfo("Se detectó N° trámite web; canal asignado como MPV.");
        } else {
            item.setCanal("POR DEFINIR");
            item.setReferencia("SIN TRAMITE");
            item.addInfo("No existe N° trámite web real; canal asignado como POR DEFINIR y referencia SIN TRAMITE.");
        }
    }

    private boolean esTramiteWebReal(String tramiteWeb) {
        String value = textoSeguro(tramiteWeb);
        if (value.isEmpty()) {
            return false;
        }

        String normalizado = normalizarTexto(value);
        if ("S/N".equals(normalizado)
                || "S N".equals(normalizado)
                || "SN".equals(normalizado)
                || "N/A".equals(normalizado)
                || "NA".equals(normalizado)
                || "NO APLICA".equals(normalizado)
                || "SIN TRAMITE".equals(normalizado)
                || "SIN TRAMITE WEB".equals(normalizado)
                || "NO MPV".equals(normalizado)
                || "NO ES MPV".equals(normalizado)
                || "PRESENCIAL".equals(normalizado)
                || "POR DEFINIR".equals(normalizado)) {
            return false;
        }

        return value.matches(".*\\d.*");
    }

    private void aplicarDniSolicitante(CargaDiariaExcelRow item, String dni) {
        DocumentoNormalizado normalizado = normalizarDni(dni);
        item.setDniSolicitanteVisual(normalizado.visual);
        item.setDniSolicitantePersistente(normalizado.persistente);
        if (normalizado.sinDocumento) {
            item.addAdvertencia("DNI no informado; se mostrará como SIN DNI.");
        } else if (normalizado.completado) {
            item.addAdvertencia("DNI completado con ceros a la izquierda.");
        } else if (!normalizado.valido) {
            item.addAdvertencia("DNI solicitante inválido; no se guardará como DNI.");
        }
    }

    private void aplicarNumeroDocumento(CargaDiariaExcelRow item, String numeroDocumento) {
        if (esSinNumeroDocumento(numeroDocumento)) {
            item.setNumeroDocumento("S/N");
            item.setNumeroDocumentoPersistente(null);
            return;
        }

        item.setNumeroDocumento(textoSeguro(numeroDocumento));
        item.setNumeroDocumentoPersistente(textoSeguro(numeroDocumento));
    }

    private void aplicarTitulares(CargaDiariaExcelRow item, String valorTitular) {
        String titular = textoSeguro(valorTitular);
        String titular2 = "";
        if (titular.contains("/")) {
            String[] partes = titular.split("/", 2);
            titular = partes[0].trim();
            titular2 = partes.length > 1 ? partes[1].trim() : "";
        }
        item.setTitular(titular);
        item.setTitular2(titular2);
    }

    private void aplicarDniTitularSiSolicitanteEsTitular(CargaDiariaExcelRow item) {
        String solicitadoPor = normalizarTexto(item.getSolicitadoPor());
        String titular = normalizarTexto(item.getTitular());
        if (solicitadoPor.isEmpty() || titular.isEmpty() || !solicitadoPor.equals(titular)) {
            return;
        }
        item.setDniTitularVisual(item.getDniSolicitanteVisual());
        item.setDniTitularPersistente(item.getDniSolicitantePersistente());
        if (!estaVacio(item.getDniTitularPersistente())) {
            item.addInfo("Solicitado por coincide con titular; DNI titular asignado desde DNI solicitante.");
        }
    }

    private void validarCatalogos(CargaDiariaExcelRow item) {
        CatalogoItem tipoSolicitud = resolverTipoSolicitudPorDefecto();
        if (tipoSolicitud == null) {
            item.addError("No se encontró un tipo de solicitud por defecto en catálogo.");
        } else {
            item.setIdTipoSolicitud(tipoSolicitud.getIdCatalogoItem());
            item.setTipoSolicitud(tipoSolicitud.getDescripcion());
            item.addInfo("Tipo de solicitud asignado como " + tipoSolicitud.getDescripcion() + ".");
        }

        CatalogoItem tipoDocumento = resolverCatalogo(tiposDocumento, item.getTipoDocumento());
        if (tipoDocumento == null) {
            item.addError("No se encontró el tipo de documento en catálogo.");
        } else {
            item.setIdTipoDocumento(tipoDocumento.getIdCatalogoItem());
            item.setTipoDocumento(tipoDocumento.getDescripcion());
        }

        CatalogoItem procedimiento = resolverCatalogo(procedimientos, item.getProcedimientoRegistral());
        if (procedimiento == null) {
            item.addError("No se encontró el procedimiento registral en catálogo.");
        } else {
            item.setIdProcedimientoRegistral(procedimiento.getIdCatalogoItem());
            item.setProcedimientoRegistral(procedimiento.getDescripcion());
        }

        CatalogoItem tipoActa = resolverCatalogo(tiposActa, item.getTipoActa());
        if (tipoActa == null) {
            item.addError("No se encontró el tipo de acta en catálogo.");
        } else {
            item.setIdTipoActa(tipoActa.getIdCatalogoItem());
            item.setTipoActa(tipoActa.getDescripcion());
        }
    }

    private void validarObligatorios(CargaDiariaExcelRow item) {
        if (item.getFechaSolicitud() == null) {
            item.addError("Falta fecha de solicitud.");
        }
        if (estaVacio(item.getTipoSolicitud())) {
            item.addError("Falta tipo de solicitud.");
        }
        if (estaVacio(item.getProcedimientoRegistral())) {
            item.addError("Falta procedimiento registral.");
        }
        if (estaVacio(item.getTipoActa())) {
            item.addError("Falta tipo de acta.");
        }
        if (estaVacio(item.getNumeroActa())) {
            item.addError("Falta número de acta.");
        }
        if (estaVacio(item.getTitular())) {
            item.addError("Falta titular.");
        }
    }

    private void validarDuplicidad(CargaDiariaExcelRow item, Set<String> clavesLeidas) throws SQLException {
        if (CargaDiariaExcelRow.ESTADO_ERROR.equals(item.getEstadoValidacion())) {
            return;
        }
        String clave = normalizarTexto(item.getNumeroActa()) + "|" + normalizarTexto(item.getTitular());
        if (item.esMatrimonio()) {
            clave += "|" + normalizarTexto(item.getTitular2());
        }
        if (!clavesLeidas.add(clave)) {
            item.marcarDuplicado("Posible duplicado: existe otra fila del archivo con el mismo número de acta y titular.", null);
            return;
        }

        Expediente duplicado = expedienteRepository.buscarDuplicadoPorActaYNombreTitular(
                item.getNumeroActa(),
                item.getTitular(),
                item.esMatrimonio() ? item.getTitular2() : null);
        if (duplicado == null || duplicado.getIdExpediente() <= 0) {
            return;
        }
        if (item.esMatrimonio()) {
            item.marcarDuplicado(
                    "Posible duplicado: ya existe un expediente de matrimonio con el mismo número de acta y uno de los titulares.",
                    duplicado);
        } else {
            item.marcarDuplicado(
                    "Posible duplicado: ya existe expediente con el mismo número de acta y titular.",
                    duplicado);
        }
    }

    private Expediente crearExpediente(CargaDiariaExcelRow fila) {
        Expediente expediente = new Expediente();
        expediente.setEsRegistroSdrerc(0);
        expediente.setHojaEnvioExpediente("");
        expediente.setNumeroTramiteDocumento(fila.getReferencia());
        expediente.setCanalRecepcion(fila.getCanal());
        expediente.setFechaSolicitud(fila.getFechaSolicitud());
        expediente.setTipoDocumento(fila.getIdTipoDocumento());
        expediente.setNumeroDocumento(fila.getNumeroDocumentoPersistente());
        expediente.setTipoActa(fila.getIdTipoActa());
        expediente.setNumeroActa(fila.getNumeroActa());
        expediente.setTipoGrupoFamiliar(0);
        expediente.setGradoParentesco(0);
        expediente.setTipoProcedimientoRegistral(fila.getIdProcedimientoRegistral());
        expediente.setTipoSolicitud(fila.getIdTipoSolicitud());
        expediente.setDniRemitente(fila.getDniSolicitantePersistente());
        expediente.setApellidoNombreRemitente(fila.getSolicitadoPor());
        expediente.setUnidadOrganica(0);
        expediente.setDniTitular(fila.getDniTitularPersistente());
        expediente.setApellidoNombreTitular(fila.getTitular());
        expediente.setDniTitular2(null);
        expediente.setApellidoNombreTitular2(fila.esMatrimonio() ? fila.getTitular2() : null);
        expediente.setDepartamento(0);
        expediente.setProvincia(0);
        expediente.setDistrito(0);
        expediente.setDireccionDomiciliaria(0);
        expediente.setDomicilio("");
        expediente.setCorreoElectronico("");
        expediente.setCelular("");
        expediente.setEstado(ESTADO_REGISTRO_EXPEDIENTE);
        expediente.setIdUsuarioCrea(SessionContext.getIdUsuarioActual());
        return expediente;
    }

    private Date parseFecha(Row row, Map<String, Integer> columnas, String key, CargaDiariaExcelRow item) {
        Cell cell = celda(row, columnas, key);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue();
        }
        String value = valorTexto(cell);
        if (estaVacio(value)) {
            return null;
        }
        String[] patrones = {"dd/MM/yyyy", "d/M/yyyy", "yyyy-MM-dd"};
        for (String patron : patrones) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(patron);
                sdf.setLenient(false);
                return sdf.parse(value.trim());
            } catch (ParseException ignored) {
            }
        }
        item.addError("Fecha de solicitud inválida.");
        return null;
    }

    private CatalogoItem resolverCatalogo(Map<String, CatalogoItem> catalogo, String descripcion) {
        String key = normalizarTexto(descripcion);
        if (key.isEmpty()) {
            return null;
        }
        CatalogoItem exacto = catalogo.get(key);
        if (exacto != null) {
            return exacto;
        }
        for (Map.Entry<String, CatalogoItem> entry : catalogo.entrySet()) {
            if (entry.getKey().contains(key) || key.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private CatalogoItem resolverTipoSolicitudPorDefecto() {
        CatalogoItem parte = resolverCatalogo(tiposSolicitud, "PARTE");
        if (parte != null) {
            return parte;
        }
        CatalogoItem oficio = resolverCatalogo(tiposSolicitud, "OFICIO");
        if (oficio != null) {
            return oficio;
        }
        for (CatalogoItem item : tiposSolicitud.values()) {
            return item;
        }
        return null;
    }

    private String texto(Row row, Map<String, Integer> columnas, String key) {
        return textoSeguro(valorTexto(celda(row, columnas, key)));
    }

    private String textoProcedimientoRegistral(Row row, Map<String, Integer> columnas) {
        String procedimientoNuevo = texto(row, columnas, "PROCEDIMIENTO REGISTRAL");
        if (!estaVacio(procedimientoNuevo)) {
            return procedimientoNuevo;
        }
        String procedimiento = texto(row, columnas, "TIPO DE SOLICITUD PROCEDIMIENTO");
        if (!estaVacio(procedimiento)) {
            return procedimiento;
        }
        return texto(row, columnas, "TIPO DE SOLICITUD");
    }

    private Cell celda(Row row, Map<String, Integer> columnas, String key) {
        Integer column = columnas.get(key);
        return column == null || row == null ? null : row.getCell(column);
    }

    private String valorTexto(Cell cell) {
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private boolean filaVacia(Row row) {
        if (row == null) {
            return true;
        }
        for (Cell cell : row) {
            if (!estaVacio(valorTexto(cell))) {
                return false;
            }
        }
        return true;
    }

    private DocumentoNormalizado normalizarDni(String dni) {
        DocumentoNormalizado result = new DocumentoNormalizado();
        String value = textoSeguro(dni).trim();
        if (esSinDni(value)) {
            result.visual = "SIN DNI";
            result.persistente = null;
            result.sinDocumento = true;
            result.valido = true;
            return result;
        }
        String digits = value.replaceAll("\\D", "");
        if (!digits.equals(value)) {
            result.visual = value;
            result.persistente = null;
            result.valido = false;
            return result;
        }
        if (digits.length() < 8) {
            digits = completarIzquierda(digits, 8);
            result.completado = true;
        }
        if (digits.length() > 8) {
            result.visual = value;
            result.persistente = null;
            result.valido = false;
            return result;
        }
        result.visual = digits;
        result.persistente = digits;
        result.valido = true;
        return result;
    }

    private String completarIzquierda(String value, int length) {
        StringBuilder sb = new StringBuilder(value == null ? "" : value);
        while (sb.length() < length) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }

    private boolean esSinDni(String value) {
        String normalizado = normalizarTexto(value);
        return normalizado.isEmpty()
                || "SIN DNI".equals(normalizado)
                || "NO TIENE".equals(normalizado)
                || "NO REGISTRA".equals(normalizado)
                || "SINDNI".equals(normalizado);
    }

    private boolean esSinNumeroDocumento(String value) {
        String normalizado = normalizarTexto(value);
        return normalizado.isEmpty()
                || "S/N".equals(normalizado)
                || "S N".equals(normalizado)
                || "SN".equals(normalizado)
                || "N/A".equals(normalizado)
                || "SIN NUMERO".equals(normalizado)
                || "SIN DOCUMENTO".equals(normalizado);
    }

    private String normalizarEncabezado(String value) {
        String normalizado = normalizarTexto(value)
                .replace("°", "")
                .replace("º", "")
                .replace("/", " ");
        normalizado = normalizado.replaceAll("[^A-Z0-9 ]", " ");
        normalizado = normalizado.replaceAll("\\s+", " ").trim();
        normalizado = normalizado.replace("NRO ", "N ");
        normalizado = normalizado.replace("NUMERO ", "N ");
        normalizado = normalizado.replace("NUM ", "N ");
        if ("FECHA SOLICITUD".equals(normalizado)) {
            return "FECHA DE SOLICITUD";
        }
        if ("TIPO SOLICITUD".equals(normalizado)) {
            return "TIPO DE SOLICITUD";
        }
        if ("TIPO SOLICITUD PROCEDIMIENTO".equals(normalizado)) {
            return "TIPO DE SOLICITUD PROCEDIMIENTO";
        }
        if ("TIPO ACTA".equals(normalizado)) {
            return "TIPO DE ACTA";
        }
        return normalizado;
    }

    private String normalizarTexto(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
        return normalized;
    }

    private String textoSeguro(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean estaVacio(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class DocumentoNormalizado {
        private String visual;
        private String persistente;
        private boolean completado;
        private boolean sinDocumento;
        private boolean valido;
    }
}
