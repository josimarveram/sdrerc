package com.sdrerc.v3.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Smoke test minimo del esqueleto de Fase 0: confirma que el backend arranca
 * y sirve JSON. La verificacion de conectividad a Oracle la cubre
 * /actuator/health (DataSourceHealthIndicator autoconfigurado por Spring Boot).
 */
@RestController
public class PingController {

    @GetMapping("/api/ping")
    public Map<String, String> ping() {
        return Map.of("status", "ok", "service", "sdrerc-v3-backend");
    }
}
