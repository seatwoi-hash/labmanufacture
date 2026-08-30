package ru.polymetal.labManufacture.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Компонент инфраструктуры логирования RequestLoggingFilter.
 *
 * @author Tatarinov Anton
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_MDC_KEY = "requestId";

    @Value("${logging.slow-request-threshold-ms:1000}")
    private long slowRequestThresholdMs;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = resolveRequestId(request.getHeader(REQUEST_ID_HEADER));
        long startedAt = System.nanoTime();

        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            log.info("HTTP request started: method={}, path={}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            logCompletion(request, response, durationMs);
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    private void logCompletion(HttpServletRequest request, HttpServletResponse response, long durationMs) {
        int status = response.getStatus();
        Object[] arguments = {request.getMethod(), request.getRequestURI(), status, durationMs};
        if (status >= 500) {
            log.error("HTTP request failed: method={}, path={}, status={}, durationMs={}", arguments);
        } else if (status >= 400 || durationMs >= slowRequestThresholdMs) {
            log.warn("HTTP request completed with warning: method={}, path={}, status={}, durationMs={}", arguments);
        } else {
            log.info("HTTP request completed: method={}, path={}, status={}, durationMs={}", arguments);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/css/") || path.startsWith("/js/")
                || path.startsWith("/images/") || path.startsWith("/webfonts/")
                || path.equals("/styles.css") || path.startsWith("/favicon") || path.equals("/actuator/health")
                || path.equals("/actuator/prometheus");
    }

    private String resolveRequestId(String candidate) {
        if (candidate != null && candidate.matches("[A-Za-z0-9._-]{1,64}")) {
            return candidate;
        }
        return UUID.randomUUID().toString();
    }
}
