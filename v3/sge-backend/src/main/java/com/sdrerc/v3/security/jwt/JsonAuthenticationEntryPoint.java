package com.sdrerc.v3.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Reemplaza el 403 de cuerpo vacío que Spring Security devuelve por defecto cuando una ruta
 * protegida se pide sin token, con token vencido o con token inválido — {@link JwtAuthenticationFilter}
 * limpia el contexto en esos 3 casos y deja que el filtro de autorización rechace la request antes
 * de llegar a ningún controller/{@code GlobalExceptionHandler}. Sin este entry point, el cuerpo
 * vacío hacía que el frontend (`http.ts#handleResponse`, que espera siempre `{message: string}`)
 * mostrara "Ocurrió un error inesperado." en vez de un mensaje real de sesión vencida.
 *
 * <p>Devuelve 401 (no 403): semántica REST correcta para "falta autenticarse", reservando 403
 * para una futura autorización real por rol a nivel de filtro (hoy los permisos por rol se
 * resuelven en los Services/Controllers, nunca en este filtro).</p>
 */
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(),
                Map.of("message", "Su sesión expiró o no es válida. Inicie sesión nuevamente."));
    }
}
