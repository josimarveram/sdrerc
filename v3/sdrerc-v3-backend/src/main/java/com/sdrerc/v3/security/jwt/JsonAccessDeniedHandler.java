package com.sdrerc.v3.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Contraparte de {@link JsonAuthenticationEntryPoint} para el caso "autenticado pero sin permiso"
 * (403 real, distinto de "sin autenticar" — hoy no hay ninguna regla de rol a nivel de filtro de
 * Spring Security, los permisos por rol se resuellven en Service/Controller y ya devuelven JSON
 * vía {@code GlobalExceptionHandler}; este handler solo cubre un futuro uso de
 * {@code @PreAuthorize} o reglas de rol en {@code SecurityConfig}, para que ese caso tampoco
 * devuelva nunca un cuerpo vacío).
 */
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JsonAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(),
                Map.of("message", "No tiene permisos para esta acción."));
    }
}
