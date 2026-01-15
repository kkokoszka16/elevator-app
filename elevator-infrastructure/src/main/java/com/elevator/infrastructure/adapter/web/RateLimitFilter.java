package com.elevator.infrastructure.adapter.web;

import com.elevator.infrastructure.config.ElevatorProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ElevatorProperties properties;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        var httpRequest = (HttpServletRequest) request;
        var httpResponse = (HttpServletResponse) response;

        var path = httpRequest.getRequestURI();
        if (isExcludedPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        var clientIp = getClientIp(httpRequest);
        var bucket = buckets.computeIfAbsent(clientIp, this::createBucket);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many requests. Please try again later.\"}");
        }
    }

    private boolean isExcludedPath(String path) {
        return path.startsWith("/actuator") ||
               path.startsWith("/swagger") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/ws");
    }

    private String getClientIp(HttpServletRequest request) {
        var xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Bucket createBucket(String key) {
        var bandwidth = Bandwidth.builder()
                .capacity(properties.getRequestsPerMinute())
                .refillGreedy(properties.getRequestsPerMinute(), Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(bandwidth).build();
    }
}
