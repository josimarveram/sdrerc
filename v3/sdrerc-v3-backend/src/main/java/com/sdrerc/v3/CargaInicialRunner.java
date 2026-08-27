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
 * Trigger de un solo uso para el batch de carga inicial (migración histórica, ver v3/CLAUDE.md) —
 * NUNCA activo en el arranque normal de la aplicación (sin este perfil, Spring ni siquiera crea este
 * bean). Se ejecuta explícitamente con:
 *
 * <pre>
 * mvn -f v3/sdrerc-v3-backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=carga-inicial
 * </pre>
 *
 * o lanzando el jar empaquetado con {@code -Dspring.profiles.active=carga-inicial}. Sin endpoint
 * HTTP, sin botón de UI — a propósito, ver decisión de arquitectura documentada en v3/CLAUDE.md
 * ("batch de backend de un solo uso, no un módulo").
 *
 * <p>Lee el Excel de una ruta fija en disco (no recibe upload) y termina el proceso con
 * {@link System#exit(int)} al finalizar — no tiene sentido dejar el resto de la aplicación (todos
 * los controllers/seguridad HTTP) sirviendo tráfico después de correr un batch de un solo uso.</p>
 */
@Component
@Profile("carga-inicial")
public class CargaInicialRunner implements CommandLineRunner {

    private static final String RUTA_POR_DEFECTO = "docs/carga_inicial/data-unificada-2026.xlsx";

    private final CargaInicialImportService importService;

    public CargaInicialRunner(CargaInicialImportService importService) {
        this.importService = importService;
    }

    @Override
    public void run(String... args) throws IOException {
        String rutaConfigurada = System.getenv("SDRERC_CARGA_INICIAL_ARCHIVO");
        Path ruta = Paths.get(rutaConfigurada != null ? rutaConfigurada : RUTA_POR_DEFECTO);
        if (!Files.exists(ruta)) {
            System.out.println("[CargaInicial] No se encontró el archivo: " + ruta.toAbsolutePath());
            System.exit(1);
            return;
        }

        Long idUsuarioActual = null;
        String idUsuarioEnv = System.getenv("SDRERC_CARGA_INICIAL_USUARIO_ID");
        if (idUsuarioEnv != null && !idUsuarioEnv.isBlank()) {
            idUsuarioActual = Long.valueOf(idUsuarioEnv.trim());
        }

        System.out.println("[CargaInicial] Iniciando batch desde " + ruta.toAbsolutePath());
        try (InputStream in = Files.newInputStream(ruta)) {
            CargaInicialResultadoDTO resultado = importService.ejecutar(in, idUsuarioActual);
            System.out.println("[CargaInicial] Resultado: " + resultado);
        }
        System.exit(0);
    }
}
