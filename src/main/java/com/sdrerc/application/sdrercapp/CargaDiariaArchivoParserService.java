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

    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    );

    private static final Map<String, List<String>> ALIASES = crearAliases();

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
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException("El archivo no contiene filas para previsualizar.");
            }

            DataFormatter formatter = new DataFormatter(new Locale("es", "PE"));
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> columnas = mapearColumnasExcel(headerRow, formatter);
            validarColumnasObligatorias(columnas);

            List<CargaDiariaPreviewDTO> registros = new ArrayList<>();
            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || filaVacia(row, formatter)) {
                    continue;
                }
                CargaDiariaPreviewDTO dto = new CargaDiariaPreviewDTO();
                dto.setFila(rowIndex + 1);
                dto.setNumeroTramite(valorExcel(row, columnas.get("numeroTramite"), formatter));
                dto.setTipoProcedimiento(valorExcel(row, columnas.get("tipoProcedimiento"), formatter));
                dto.setTipoDocumento(valorExcel(row, columnas.get("tipoDocumento"), formatter));
                dto.setActa(valorExcel(row, columnas.get("acta"), formatter));
                dto.setTitular(valorExcel(row, columnas.get("titular"), formatter));
                dto.setRemitente(valorExcel(row, columnas.get("remitente"), formatter));
                asignarFechaExcel(dto, row, columnas.get("fechaRecepcion"), formatter);
                dto.setObservacionInicial(valorExcel(row, columnas.get("observacionInicial"), formatter));
                dto.reiniciarValidacion();
                registros.add(dto);
            }
            if (registros.isEmpty()) {
                throw new IllegalArgumentException("No se encontraron registros en el archivo seleccionado.");
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
            Map<String, Integer> columnas = mapearColumnasCsv(parseCsvLine(header, separador));
            validarColumnasObligatorias(columnas);

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
                dto.setTipoProcedimiento(valorCsv(valores, columnas.get("tipoProcedimiento")));
                dto.setTipoDocumento(valorCsv(valores, columnas.get("tipoDocumento")));
                dto.setActa(valorCsv(valores, columnas.get("acta")));
                dto.setTitular(valorCsv(valores, columnas.get("titular")));
                dto.setRemitente(valorCsv(valores, columnas.get("remitente")));
                asignarFechaTexto(dto, valorCsv(valores, columnas.get("fechaRecepcion")));
                dto.setObservacionInicial(valorCsv(valores, columnas.get("observacionInicial")));
                dto.reiniciarValidacion();
                registros.add(dto);
            }
            if (registros.isEmpty()) {
                throw new IllegalArgumentException("No se encontraron registros en el archivo seleccionado.");
            }
            return registros;
        }
    }

    private Map<String, Integer> mapearColumnasExcel(Row headerRow, DataFormatter formatter) {
        if (headerRow == null) {
            throw new IllegalArgumentException("El archivo no contiene fila de encabezados.");
        }
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            headers.add(formatter.formatCellValue(headerRow.getCell(i)));
        }
        return mapearColumnasCsv(headers);
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

    private void validarColumnasObligatorias(Map<String, Integer> columnas) {
        Map<String, String> nombres = new LinkedHashMap<>();
        nombres.put("numeroTramite", "número de trámite");
        nombres.put("tipoProcedimiento", "tipo de procedimiento");
        nombres.put("tipoDocumento", "tipo de documento");
        nombres.put("acta", "acta");
        nombres.put("titular", "titular");
        nombres.put("remitente", "remitente");
        nombres.put("fechaRecepcion", "fecha recepción");

        List<String> faltantes = new ArrayList<>();
        for (Map.Entry<String, String> entry : nombres.entrySet()) {
            if (!columnas.containsKey(entry.getKey())) {
                faltantes.add(entry.getValue());
            }
        }
        if (!faltantes.isEmpty()) {
            throw new IllegalArgumentException("Faltan columnas obligatorias: " + String.join(", ", faltantes) + ".");
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
        aliases.put("numeroTramite", normalizarLista("NUMERO_TRAMITE", "NRO_TRAMITE", "TRAMITE", "NUMERO TRAMITE", "NRO TRAMITE"));
        aliases.put("tipoProcedimiento", normalizarLista("TIPO_PROCEDIMIENTO", "PROCEDIMIENTO", "TIPO PROCEDIMIENTO"));
        aliases.put("tipoDocumento", normalizarLista("TIPO_DOCUMENTO", "DOCUMENTO", "TIPO DOCUMENTO"));
        aliases.put("acta", normalizarLista("ACTA", "NUMERO_ACTA", "NRO_ACTA", "NUMERO ACTA"));
        aliases.put("titular", normalizarLista("TITULAR", "NOMBRE_TITULAR", "NOMBRE TITULAR"));
        aliases.put("remitente", normalizarLista("REMITENTE"));
        aliases.put("fechaRecepcion", normalizarLista("FECHA_RECEPCION", "FECHA", "FECHA RECEPCION"));
        aliases.put("observacionInicial", normalizarLista("OBSERVACION", "OBSERVACIONES", "OBSERVACION_INICIAL", "OBSERVACION INICIAL"));
        return aliases;
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
}
