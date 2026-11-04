package com.sdrerc.v3;

import com.sdrerc.v3.application.CargaInicialImportService;
import com.sdrerc.v3.domain.CargaInicialResultadoDTO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Trigger de un solo uso para corregir el numero_expediente de filas marcadas como duplicado por la
 * columna "N EXPEDIENTE DIGITAL" (ver Javadoc de
 * {@link CargaInicialImportService#corregirNumeroExpedienteDigital}) — NUNCA activo en el arranque
 * normal. Se ejecuta explícitamente con:
 *
 * <pre>
 * mvn -f v3/sdrerc-v3-backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=carga-inicial-corregir-numero-digital
 * </pre>
 */
@Component
@Profile("carga-inicial-corregir-numero-digital")
public class CargaInicialCorregirNumeroDigitalRunner implements CommandLineRunner {

    private static final String RUTA_POR_DEFECTO = "docs/carga_inicial/data-unificada-2026.xlsx";

    private final CargaInicialImportService importService;

    public CargaInicialCorregirNumeroDigitalRunner(CargaInicialImportService importService) {
        this.importService = importService;
    }

    @Override
    public void run(String... args) throws IOException {
        String rutaConfigurada = System.getenv("SDRERC_CARGA_INICIAL_ARCHIVO");
        Path ruta = Paths.get(rutaConfigurada != null ? rutaConfigurada : RUTA_POR_DEFECTO);
        if (!Files.exists(ruta)) {
            System.out.println("[CargaInicialCorregirNumeroDigital] No se encontró el archivo: " + ruta.toAbsolutePath());
            System.exit(1);
            return;
        }

        Long idUsuarioActual = null;
        String idUsuarioEnv = System.getenv("SDRERC_CARGA_INICIAL_USUARIO_ID");
        if (idUsuarioEnv != null && !idUsuarioEnv.isBlank()) {
            idUsuarioActual = Long.valueOf(idUsuarioEnv.trim());
        }

        System.out.println("[CargaInicialCorregirNumeroDigital] Iniciando corrección de numero_expediente desde " + ruta.toAbsolutePath());
        try (InputStream in = Files.newInputStream(ruta)) {
            CargaInicialResultadoDTO resultado = importService.corregirNumeroExpedienteDigital(in, idUsuarioActual);
            System.out.println("[CargaInicialCorregirNumeroDigital] Resultado: " + resultado);
        }
        System.exit(0);
    }
}
