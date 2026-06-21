package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.CargaDiariaPreviewDTO;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class CargaDiariaArchivoParserService {

    private static final int MAX_HEADER_SCAN_ROWS = 15;
    private static final List<String> REQUIRED_COLUMNS = Arrays.asList(
            "numeroTramite",
            "numeroDocumento",
            "tipoProcedimiento",
            "tipoSolicitud",
            "tipoDocumento",
            "numeroActa",
            "titular",
            "remitente",
            "fechaRecepcion"
    );
    private static final Map<String, String> REQUIRED_COLUMN_NAMES = crearNombresColumnasObligatorias();
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    );

    private static final Map<String, List<String>> ALIASES = crearAliases();
    private String ultimoDiagnostico;

    public String getUltimoDiagnostico() {
        return ultimoDiagnostico == null ? "" : ultimoDiagnostico;
    }

    public List<CargaDiariaPreviewDTO> leerArchivo(File archivo) throws IOException {
        if (archivo == null) {
            throw new IllegalArgumentException("Seleccione un archivo para previsualizar.");
        }
        String nombre = archivo.getName().toLowerCase(Locale.ROOT);
        if (nombre.endsWith(".xlsx")) {
            return leerExcel(archivo);
        }
        if (nombre.endsWith(".csv")) {
            return leerCsv(archivo);
        }
        throw new IllegalArgumentException("Formato no soportado. Use un archivo .xlsx o .csv.");
    }

    private List<CargaDiariaPreviewDTO> leerExcel(File archivo) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(archivo)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("El archivo no contiene filas para previsualizar.");
            }

            DataFormatter formatter = new DataFormatter(new Locale("es", "PE"));
            HeaderInfo header = detectarEncabezadoExcel(workbook, formatter);
            Map<String, Integer> columnas = header.columnas;
            validarColumnasObligatorias(columnas, header);
            Sheet sheet = workbook.getSheetAt(header.sheetIndex);
            ultimoDiagnostico = "Hoja \"" + sheet.getSheetName() + "\", encabezados en fila " + (header.rowIndex + 1) + ".";

            List<CargaDiariaPreviewDTO> registros = new ArrayList<>();
            for (int rowIndex = header.rowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || filaVacia(row, formatter)) {
                    continue;
                }
                CargaDiariaPreviewDTO dto = new CargaDiariaPreviewDTO();
                dto.setFila(rowIndex + 1);
                dto.setNumeroTramite(valorExcel(row, columnas.get("numeroTramite"), formatter));
                dto.setCanalRecepcion(valorExcel(row, columnas.get("canalRecepcion"), formatter));
                dto.setNumeroExpedienteSgd(valorExcel(row, columnas.get("numeroExpedienteSgd"), formatter));
                dto.setNumeroDocumento(valorExcel(row, columnas.get("numeroDocumento"), formatter));
                dto.setTipoProcedimiento(valorExcel(row, columnas.get("tipoProcedimiento"), formatter));
                dto.setTipoSolicitud(valorExcel(row, columnas.get("tipoSolicitud"), formatter));
                dto.setTipoDocumento(valorExcel(row, columnas.get("tipoDocumento"), formatter));
                dto.setTipoDocumentoIdentidadSolicitante(valorExcel(row, columnas.get("tipoDocumentoIdentidadSolicitante"), formatter));
                dto.setNumeroDocumentoIdentidadSolicitante(valorExcel(row, columnas.get("numeroDocumentoIdentidadSolicitante"), formatter));
                dto.setTipoDocumentoIdentidadTitular(valorExcel(row, columnas.get("tipoDocumentoIdentidadTitular"), formatter));
                dto.setNumeroDocumentoIdentidadTitular(valorExcel(row, columnas.get("numeroDocumentoIdentidadTitular"), formatter));
                aplicarGrupoFamiliar(dto, valorExcel(row, columnas.get("grupoFamiliar"), formatter));
                dto.setTipoActa(valorExcel(row, columnas.get("tipoActa"), formatter));
                dto.setNumeroActa(valorExcel(row, columnas.get("numeroActa"), formatter));
                dto.setTitular(valorExcel(row, columnas.get("titular"), formatter));
                dto.setRemitente(valorExcel(row, columnas.get("remitente"), formatter));
                asignarFechaExcel(dto, row, columnas.get("fechaRecepcion"), formatter);
                dto.setObservacionInicial(valorExcel(row, columnas.get("observacionInicial"), formatter));
                aplicarReglasDerivadas(dto);
                dto.reiniciarValidacion();
                registros.add(dto);
            }
            if (registros.isEmpty()) {
                throw new IllegalArgumentException("No se encontraron registros debajo de la fila de encabezados detectada. " + ultimoDiagnostico);
            }
            return registros;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException("No se pudo leer el archivo Excel. Verifique que no esté dañado o abierto por otra aplicación.", ex);
        }
    }

    private List<CargaDiariaPreviewDTO> leerCsv(File archivo) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(archivo.toPath(), StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                throw new IllegalArgumentException("El archivo CSV no contiene encabezados.");
            }
            char separador = detectarSeparador(header);
            HeaderInfo headerInfo = new HeaderInfo(0, 0, parseCsvLine(header, separador));
            Map<String, Integer> columnas = mapearColumnasCsv(headerInfo.headers);
            headerInfo.columnas.putAll(columnas);
            validarColumnasObligatorias(columnas, headerInfo);
            ultimoDiagnostico = "CSV, encabezados en fila 1.";

            List<CargaDiariaPreviewDTO> registros = new ArrayList<>();
            String line;
            int fila = 1;
            while ((line = reader.readLine()) != null) {
                fila++;
                List<String> valores = parseCsvLine(line, separador);
                if (filaCsvVacia(valores)) {
                    continue;
                }
                CargaDiariaPreviewDTO dto = new CargaDiariaPreviewDTO();
                dto.setFila(fila);
                dto.setNumeroTramite(valorCsv(valores, columnas.get("numeroTramite")));
                dto.setCanalRecepcion(valorCsv(valores, columnas.get("canalRecepcion")));
                dto.setNumeroExpedienteSgd(valorCsv(valores, columnas.get("numeroExpedienteSgd")));
                dto.setNumeroDocumento(valorCsv(valores, columnas.get("numeroDocumento")));
                dto.setTipoProcedimiento(valorCsv(valores, columnas.get("tipoProcedimiento")));
                dto.setTipoSolicitud(valorCsv(valores, columnas.get("tipoSolicitud")));
                dto.setTipoDocumento(valorCsv(valores, columnas.get("tipoDocumento")));
                dto.setTipoDocumentoIdentidadSolicitante(valorCsv(valores, columnas.get("tipoDocumentoIdentidadSolicitante")));
                dto.setNumeroDocumentoIdentidadSolicitante(valorCsv(valores, columnas.get("numeroDocumentoIdentidadSolicitante")));
                dto.setTipoDocumentoIdentidadTitular(valorCsv(valores, columnas.get("tipoDocumentoIdentidadTitular")));
                dto.setNumeroDocumentoIdentidadTitular(valorCsv(valores, columnas.get("numeroDocumentoIdentidadTitular")));
                aplicarGrupoFamiliar(dto, valorCsv(valores, columnas.get("grupoFamiliar")));
                dto.setTipoActa(valorCsv(valores, columnas.get("tipoActa")));
                dto.setNumeroActa(valorCsv(valores, columnas.get("numeroActa")));
                dto.setTitular(valorCsv(valores, columnas.get("titular")));
                dto.setRemitente(valorCsv(valores, columnas.get("remitente")));
                asignarFechaTexto(dto, valorCsv(valores, columnas.get("fechaRecepcion")));
                dto.setObservacionInicial(valorCsv(valores, columnas.get("observacionInicial")));
                aplicarReglasDerivadas(dto);
                dto.reiniciarValidacion();
                registros.add(dto);
            }
            if (registros.isEmpty()) {
                throw new IllegalArgumentException("No se encontraron registros en el archivo seleccionado.");
            }
            return registros;
        }
    }

    private HeaderInfo detectarEncabezadoExcel(Workbook workbook, DataFormatter formatter) {
        HeaderInfo mejor = null;
        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet currentSheet = workbook.getSheetAt(sheetIndex);
            int first = currentSheet.getFirstRowNum();
            int last = Math.min(currentSheet.getLastRowNum(), first + MAX_HEADER_SCAN_ROWS - 1);
            for (int rowIndex = first; rowIndex <= last; rowIndex++) {
                Row row = currentSheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                HeaderInfo candidato = new HeaderInfo(sheetIndex, rowIndex, leerHeaders(row, formatter));
                candidato.columnas.putAll(mapearColumnasCsv(candidato.headers));
                if (mejor == null || candidato.getScore() > mejor.getScore()) {
                    mejor = candidato;
                }
                if (candidato.tieneTodasObligatorias()) {
                    return candidato;
                }
            }
        }
        if (mejor == null || mejor.headers.isEmpty()) {
            throw new IllegalArgumentException("No se pudo detectar una fila de encabezados en las primeras "
                    + MAX_HEADER_SCAN_ROWS + " filas del archivo.");
        }
        return mejor;
    }

    private List<String> leerHeaders(Row row, DataFormatter formatter) {
        List<String> headers = new ArrayList<>();
        int last = Math.max(row.getLastCellNum(), 0);
        for (int i = 0; i < last; i++) {
            headers.add(formatter.formatCellValue(row.getCell(i)));
        }
        return headers;
    }

    private Map<String, Integer> mapearColumnasCsv(List<String> headers) {
        Map<String, Integer> columnas = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String normalizedHeader = normalizar(headers.get(i));
            for (Map.Entry<String, List<String>> entry : ALIASES.entrySet()) {
                if (!columnas.containsKey(entry.getKey()) && entry.getValue().contains(normalizedHeader)) {
                    columnas.put(entry.getKey(), i);
                }
            }
        }
        return columnas;
    }

    private void validarColumnasObligatorias(Map<String, Integer> columnas, HeaderInfo header) {
        List<String> faltantes = new ArrayList<>();
        for (String key : REQUIRED_COLUMNS) {
            if (!columnas.containsKey(key)) {
                faltantes.add(REQUIRED_COLUMN_NAMES.get(key));
            }
        }
        if (!faltantes.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se pudo previsualizar el archivo porque la plantilla no contiene las columnas obligatorias esperadas.\n"
                    + "Columnas faltantes: " + String.join(", ", faltantes) + ".\n"
                    + "Columnas encontradas: " + describirHeaders(header.headers) + ".\n"
                    + "Ubicación revisada: " + header.descripcion() + ".\n"
                    + "Verifique que esté usando la plantilla correcta.");
        }
    }

    private boolean filaVacia(Row row, DataFormatter formatter) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            String value = formatter.formatCellValue(row.getCell(i));
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean filaCsvVacia(List<String> valores) {
        for (String valor : valores) {
            if (valor != null && !valor.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String valorExcel(Row row, Integer index, DataFormatter formatter) {
        if (index == null) {
            return null;
        }
        Cell cell = row.getCell(index);
        return cell == null ? null : formatter.formatCellValue(cell);
    }

    private String valorCsv(List<String> valores, Integer index) {
        if (index == null || index < 0 || index >= valores.size()) {
            return null;
        }
        return limpiarBom(valores.get(index));
    }

    private void asignarFechaExcel(CargaDiariaPreviewDTO dto, Row row, Integer index, DataFormatter formatter) {
        if (index == null) {
            return;
        }
        Cell cell = row.getCell(index);
        if (cell == null) {
            return;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            LocalDate fecha = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            dto.setFechaRecepcion(fecha);
            dto.setFechaRecepcionTexto(fecha.toString());
            return;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            LocalDate fecha = DateUtil.getJavaDate(cell.getNumericCellValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            dto.setFechaRecepcion(fecha);
            dto.setFechaRecepcionTexto(fecha.toString());
            return;
        }
        asignarFechaTexto(dto, formatter.formatCellValue(cell));
    }

    private void asignarFechaTexto(CargaDiariaPreviewDTO dto, String value) {
        dto.setFechaRecepcionTexto(value);
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        String cleaned = value.trim();
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                dto.setFechaRecepcion(LocalDate.parse(cleaned, formatter));
                return;
            } catch (DateTimeParseException ignored) {
                // Intenta el siguiente formato soportado.
            }
        }
        try {
            BigDecimal excelSerial = new BigDecimal(cleaned);
            LocalDate fecha = DateUtil.getJavaDate(excelSerial.doubleValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            dto.setFechaRecepcion(fecha);
        } catch (NumberFormatException ex) {
            dto.setFechaRecepcion(null);
        }
    }

    private void aplicarGrupoFamiliar(CargaDiariaPreviewDTO dto, String value) {
        if (dto == null) {
            return;
        }
        dto.setGrupoFamiliar(false);
        dto.setCriterioGrupoFamiliar(null);
        dto.setObservacionGrupoFamiliar(null);
        if (!hasText(value)) {
            return;
        }
        String normalized = normalizarTextoLibre(value);
        if ("SI".equals(normalized) || "S".equals(normalized)) {
            dto.setGrupoFamiliar(true);
            dto.setCriterioGrupoFamiliar("EXCEL");
            return;
        }
        if ("NO".equals(normalized) || "N".equals(normalized)) {
            return;
        }
        dto.agregarObservacionGrupoFamiliar(null,
                "Valor de Grupo familiar no reconocido: " + value.trim() + ". Se tomó No.");
    }

    private void aplicarReglasDerivadas(CargaDiariaPreviewDTO dto) {
        if (dto == null) {
            return;
        }
        if (equalsNormalizado(dto.getTitular(), dto.getRemitente())) {
            if (!hasText(dto.getNumeroDocumentoIdentidadTitular())) {
                dto.setNumeroDocumentoIdentidadTitular(dto.getNumeroDocumentoIdentidadSolicitante());
            }
            if (!hasText(dto.getTipoDocumentoIdentidadTitular())) {
                String tipoSolicitante = dto.getTipoDocumentoIdentidadSolicitante();
                dto.setTipoDocumentoIdentidadTitular("RUC".equalsIgnoreCase(tipoSolicitante) ? null : tipoSolicitante);
            }
        }
        if (hasText(dto.getCanalRecepcion())) {
            dto.setCanalRecepcion(resolverCodigoCanalRecepcion(dto.getCanalRecepcion()));
        } else {
            dto.setCanalRecepcion(resolverCanalRecepcion(dto));
        }
    }

    private String resolverCanalRecepcion(CargaDiariaPreviewDTO dto) {
        String tramite = dto.getNumeroTramite();
        if (!esSinTramite(tramite) && contieneNumero(tramite)) {
            return "MESA_PARTES_VIRTUAL";
        }
        if (esSinTramite(tramite)) {
            String documentoSolicitante = dto.getNumeroDocumentoIdentidadSolicitante();
            if (contieneNumero(documentoSolicitante)) {
                return "MESA_PARTES_PRESENCIAL";
            }
            String solicitadoPor = normalizarTextoLibre(dto.getRemitente());
            if (solicitadoPor.startsWith("OR") || solicitadoPor.contains("OFICINA REGISTRAL")) {
                return "OR_PRESENCIAL";
            }
            if (esOrigenInternoReniec(solicitadoPor)) {
                return "INTERNO";
            }
            return "MESA_PARTES_PRESENCIAL";
        }
        return null;
    }

    private String resolverCodigoCanalRecepcion(String value) {
        String normalized = normalizarTextoLibre(value);
        if (!hasText(normalized)) {
            return null;
        }
        if ("INTERNO".equals(normalized)) {
            return "INTERNO";
        }
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
        if ("OR".equals(normalized)
                || "OR PRESENCIAL".equals(normalized)
                || "OFICINA REGISTRAL".equals(normalized)) {
            return "OR_PRESENCIAL";
        }
        return normalized.replace(' ', '_');
    }

    private boolean esOrigenInternoReniec(String solicitadoPor) {
        if (!hasText(solicitadoPor)) {
            return false;
        }
        return solicitadoPor.startsWith("SUB DIRECCION")
                || solicitadoPor.startsWith("SUBDIRECCION")
                || solicitadoPor.startsWith("OFICINA")
                || solicitadoPor.startsWith("DIRECCION")
                || solicitadoPor.startsWith("GERENCIA")
                || solicitadoPor.startsWith("JEFATURA")
                || solicitadoPor.startsWith("UNIDAD")
                || solicitadoPor.startsWith("AREA")
                || solicitadoPor.contains("RENIEC")
                || solicitadoPor.contains("REGISTRO NACIONAL DE IDENTIFICACION");
    }

    private boolean esSinTramite(String value) {
        String normalized = normalizarTextoLibre(value);
        return !hasText(normalized) || "SIN TRAMITE".equals(normalized);
    }

    private boolean contieneNumero(String value) {
        if (!hasText(value)) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean equalsNormalizado(String a, String b) {
        String left = normalizar(a);
        String right = normalizar(b);
        return !left.isEmpty() && left.equals(right);
    }

    private String normalizarTextoLibre(String value) {
        String cleaned = limpiarBom(value);
        if (cleaned == null) {
            return "";
        }
        String normalized = Normalizer.normalize(cleaned.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replace('_', ' ')
                .replaceAll("\\s+", " ");
        return normalized.trim();
    }

    private char detectarSeparador(String header) {
        int semicolon = contar(header, ';');
        int comma = contar(header, ',');
        return semicolon >= comma ? ';' : ',';
    }

    private int contar(String value, char needle) {
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == needle) {
                count++;
            }
        }
        return count;
    }

    private List<String> parseCsvLine(String line, char separator) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == separator && !inQuotes) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString().trim());
        return values;
    }

    private static Map<String, List<String>> crearAliases() {
        Map<String, List<String>> aliases = new LinkedHashMap<>();
        aliases.put("numeroTramite", normalizarLista(
                "NUMERO_TRAMITE",
                "NRO_TRAMITE",
                "NRO. TRAMITE",
                "N° TRAMITE",
                "N TRAMITE",
                "NUMERO DE TRAMITE",
                "NÚMERO DE TRÁMITE",
                "TRAMITE",
                "TRÁMITE",
                "N° TRAMITE WEB",
                "N TRAMITE WEB",
                "NRO TRAMITE WEB",
                "NUMERO TRAMITE WEB"));
        aliases.put("canalRecepcion", normalizarLista(
                "CANAL RECEPCION",
                "CANAL RECEPCIÓN",
                "CANAL_RECEPCION",
                "CANAL DE RECEPCION",
                "CANAL DE RECEPCIÓN",
                "CANAL INGRESO",
                "CANAL DE INGRESO"));
        aliases.put("numeroExpedienteSgd", normalizarLista(
                "N° EXPEDIENTE SGD",
                "Nº EXPEDIENTE SGD",
                "N EXPEDIENTE SGD",
                "NUMERO EXPEDIENTE SGD",
                "NUMERO DE EXPEDIENTE SGD",
                "EXPEDIENTE SGD"));
        aliases.put("numeroDocumento", normalizarLista(
                "NUMERO_DOCUMENTO",
                "NUMERO DOCUMENTO",
                "NRO_DOCUMENTO",
                "NRO DOCUMENTO",
                "NRO. DOCUMENTO",
                "NRO DE DOCUMENTO",
                "N° DOCUMENTO",
                "Nº DOCUMENTO",
                "N DOCUMENTO",
                "N DE DOCUMENTO",
                "N° DOCUMENTO RECIBIDO",
                "Nº DOCUMENTO RECIBIDO",
                "N DOCUMENTO RECIBIDO",
                "NUMERO DOCUMENTO RECIBIDO",
                "NUMERO_DOCUMENTO_RECIBIDO",
                "NRO DOCUMENTO RECIBIDO",
                "NRO. DOCUMENTO RECIBIDO",
                "NUMERO DE DOCUMENTO",
                "NÚMERO DE DOCUMENTO",
                "DOCUMENTO NUMERO",
                "DOCUMENTO NRO"));
        aliases.put("tipoDocumentoIdentidadSolicitante", normalizarLista(
                "TIPO DOCUMENTO IDENTIDAD SOLICITANTE",
                "TIPO_DOC_IDENTIDAD_SOLICITANTE",
                "TIPO DOCUMENTO SOLICITANTE",
                "TIPO DOC SOLICITANTE",
                "TIPO DNI SOLICITANTE"));
        aliases.put("numeroDocumentoIdentidadSolicitante", normalizarLista(
                "N° DOCUMENTO IDENTIDAD SOLICITANTE",
                "N DOCUMENTO IDENTIDAD SOLICITANTE",
                "NUMERO DOCUMENTO IDENTIDAD SOLICITANTE",
                "NUMERO DE DOCUMENTO IDENTIDAD SOLICITANTE",
                "DOCUMENTO IDENTIDAD SOLICITANTE",
                "DNI SOLICITANTE",
                "DNI_SOLICITANTE"));
        aliases.put("tipoProcedimiento", normalizarLista(
                "TIPO_PROCEDIMIENTO",
                "TIPO DE PROCEDIMIENTO",
                "PROCEDIMIENTO",
                "PROC. REG",
                "PROCEDIMIENTO REGISTRAL",
                "PROCESO"));
        aliases.put("tipoSolicitud", normalizarLista(
                "TIPO_SOLICITUD",
                "TIPO SOLICITUD",
                "TIPO DE SOLICITUD",
                "TIPO DE SOLICITUD (PARTE / OFICIO)",
                "TIPO DE SOLICITUD PARTE OFICIO",
                "PARTE OFICIO",
                "PARTE/OFICIO"));
        aliases.put("tipoDocumento", normalizarLista(
                "TIPO_DOCUMENTO",
                "TIPO DE DOCUMENTO",
                "DOCUMENTO",
                "TIPO DOC",
                "TIPO DOC."));
        aliases.put("tipoActa", normalizarLista(
                "TIPO_ACTA",
                "TIPO ACTA",
                "TIPO DE ACTA",
                "CLASE_ACTA",
                "CLASE DE ACTA"));
        aliases.put("numeroActa", normalizarLista(
                "ACTA",
                "NUMERO_ACTA",
                "NRO_ACTA",
                "N° ACTA",
                "N ACTA",
                "NUMERO DE ACTA",
                "NÚMERO DE ACTA",
                "ACTA REGISTRAL"));
        aliases.put("titular", normalizarLista(
                "TITULAR",
                "NOMBRE_TITULAR",
                "NOMBRE DEL TITULAR",
                "APELLIDOS Y NOMBRES",
                "NOMBRES",
                "PERSONA"));
        aliases.put("tipoDocumentoIdentidadTitular", normalizarLista(
                "TIPO DOCUMENTO IDENTIDAD TITULAR",
                "TIPO_DOC_IDENTIDAD_TITULAR",
                "TIPO DOCUMENTO TITULAR",
                "TIPO DOC TITULAR",
                "TIPO DNI TITULAR"));
        aliases.put("numeroDocumentoIdentidadTitular", normalizarLista(
                "N° DOCUMENTO IDENTIDAD TITULAR",
                "N DOCUMENTO IDENTIDAD TITULAR",
                "NUMERO DOCUMENTO IDENTIDAD TITULAR",
                "NUMERO DE DOCUMENTO IDENTIDAD TITULAR",
                "DOCUMENTO IDENTIDAD TITULAR",
                "DNI TITULAR",
                "DNI_TITULAR"));
        aliases.put("grupoFamiliar", normalizarLista(
                "GRUPO FAMILIAR",
                "GRUPO_FAMILIAR",
                "FAMILIA",
                "MARCA GRUPO FAMILIAR"));
        aliases.put("remitente", normalizarLista(
                "REMITENTE",
                "ENTIDAD REMITENTE",
                "NOMBRE REMITENTE",
                "SOLICITANTE",
                "ORIGEN",
                "SOLICITADO POR"));
        aliases.put("fechaRecepcion", normalizarLista(
                "FECHA_RECEPCION",
                "FECHA RECEPCION",
                "FECHA DE RECEPCION",
                "FECHA DE RECEPCIÓN",
                "FECHA",
                "FECHA SOLICITUD",
                "FECHA DE SOLICITUD"));
        aliases.put("observacionInicial", normalizarLista(
                "OBSERVACION",
                "OBSERVACIÓN",
                "OBSERVACIONES",
                "COMENTARIO",
                "COMENTARIOS",
                "OBSERVACION_INICIAL",
                "OBSERVACION INICIAL"));
        return aliases;
    }

    private static Map<String, String> crearNombresColumnasObligatorias() {
        Map<String, String> nombres = new LinkedHashMap<>();
        nombres.put("numeroTramite", "Número de trámite");
        nombres.put("numeroDocumento", "N° documento");
        nombres.put("tipoProcedimiento", "Tipo de procedimiento");
        nombres.put("tipoSolicitud", "Tipo de solicitud");
        nombres.put("tipoDocumento", "Tipo de documento");
        nombres.put("numeroActa", "Número de acta");
        nombres.put("titular", "Titular");
        nombres.put("remitente", "Remitente");
        nombres.put("fechaRecepcion", "Fecha recepción");
        return nombres;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String describirHeaders(List<String> headers) {
        List<String> nonEmpty = new ArrayList<>();
        for (String header : headers) {
            if (header != null && !header.trim().isEmpty()) {
                nonEmpty.add(header.trim().replaceAll("\\s+", " "));
            }
        }
        if (nonEmpty.isEmpty()) {
            return "sin encabezados visibles";
        }
        return nonEmpty.toString();
    }

    private static List<String> normalizarLista(String... values) {
        List<String> normalizados = new ArrayList<>();
        for (String value : values) {
            normalizados.add(normalizar(value));
        }
        return normalizados;
    }

    private static String normalizar(String value) {
        String cleaned = limpiarBom(value);
        if (cleaned == null) {
            return "";
        }
        String normalized = Normalizer.normalize(cleaned.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        return normalized.replaceAll("[^A-Z0-9]", "");
    }

    private static String limpiarBom(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\uFEFF", "").trim();
    }

    private static class HeaderInfo {

        private final int sheetIndex;
        private final int rowIndex;
        private final List<String> headers;
        private final Map<String, Integer> columnas = new HashMap<>();

        private HeaderInfo(int sheetIndex, int rowIndex, List<String> headers) {
            this.sheetIndex = sheetIndex;
            this.rowIndex = rowIndex;
            this.headers = headers == null ? new ArrayList<String>() : headers;
        }

        private int getScore() {
            int score = 0;
            for (String key : REQUIRED_COLUMNS) {
                if (columnas.containsKey(key)) {
                    score += 10;
                }
            }
            if (columnas.containsKey("observacionInicial")) {
                score += 1;
            }
            if (columnas.containsKey("canalRecepcion")) {
                score += 1;
            }
            if (columnas.containsKey("numeroExpedienteSgd")) {
                score += 1;
            }
            if (columnas.containsKey("grupoFamiliar")) {
                score += 1;
            }
            return score;
        }

        private boolean tieneTodasObligatorias() {
            for (String key : REQUIRED_COLUMNS) {
                if (!columnas.containsKey(key)) {
                    return false;
                }
            }
            return true;
        }

        private String descripcion() {
            return "hoja " + (sheetIndex + 1) + ", fila " + (rowIndex + 1);
        }
    }
}
