package com.karam.teamup.gateway.filter;

import com.karam.teamup.gateway.route.RouteValidator;
import com.karam.teamup.gateway.jwt.JwtUtil;
import com.karam.teamup.gateway.exception.UnauthorizedAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RouteValidator routeValidator;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestPath = request.getURI().getPath();

            if (routeValidator.isSecured.test(request)) {
                log.info("Checking authentication for request: {}", requestPath);

                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    log.warn("Missing authorization header for request: {}", requestPath);
                    throw new UnauthorizedAccessException("Missing authorization header");
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    log.warn("Invalid authorization header format for request: {}", requestPath);
                    throw new UnauthorizedAccessException("Invalid authorization header");
                }

                String token = authHeader.substring(7);
                try {
                    jwtUtil.validateToken(token);
                    String username = jwtUtil.extractUsername(token);
                    log.info("Token validated successfully for user: {}", username);

                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-Username", username)
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());

                } catch (Exception e) {
                    log.error("Unauthorized access attempt on {}: {}", requestPath, e.getMessage());
                    throw new UnauthorizedAccessException("Unauthorized access: Invalid or expired token");
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {}
}
