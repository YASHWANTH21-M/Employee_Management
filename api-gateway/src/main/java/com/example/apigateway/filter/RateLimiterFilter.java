package com.example.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiterFilter extends AbstractGatewayFilterFactory<RateLimiterFilter.Config> {

    // Maximum allowed requests per window
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    public RateLimiterFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientIp = getClientKey(exchange);
            long currentTime = Instant.now().getEpochSecond();

            requestCounts.entrySet().removeIf(entry -> currentTime - entry.getValue().lastResetTime > 60);

            RequestCounter counter = requestCounts.computeIfAbsent(clientIp, k -> new RequestCounter(currentTime));

            if (currentTime - counter.lastResetTime > 60) {
                counter.count.set(0);
                counter.lastResetTime = currentTime;
            }

            if (counter.count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                response.getHeaders().add("Retry-After", "60");
                return response.setComplete();
            }

            return chain.filter(exchange);
        };
    }

    private String getClientKey(org.springframework.web.server.ServerWebExchange exchange) {
        if (exchange.getRequest().getRemoteAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        return "default-client";
    }

    public static class Config {
        // Rate limiter configuration properties
    }

    private static class RequestCounter {
        final AtomicInteger count = new AtomicInteger(0);
        long lastResetTime;

        RequestCounter(long startTime) {
            this.lastResetTime = startTime;
        }
    }
}
