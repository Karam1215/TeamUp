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

import java.util.List;

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

                // Check if the authorization header is present
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    log.warn("Missing authorization header for request: {}", requestPath);
                    throw new UnauthorizedAccessException("Missing authorization header");
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    log.warn("Invalid authorization header format for request: {}", requestPath);
                    throw new UnauthorizedAccessException("Invalid authorization header");
                }

                String token = authHeader.substring(7); // Extract the token from the header
                try {
                    // Validate token and extract username
                    jwtUtil.validateToken(token);
                    String username = jwtUtil.extractUsername(token);
                    String role = jwtUtil.extractRole(token);  // Extract the role directly

                    // Get the required role(s) for the route
                    List<String> requiredRoles = getRequiredRolesForRoute(requestPath);

                    // Check if the user has at least one of the required roles
                    if (requiredRoles.contains(role)) {
                        log.info("Token validated successfully for user: {}", username);

                        // Add the username to the request header to be forwarded to the next service
                        ServerHttpRequest modifiedRequest = request.mutate()
                                .header("X-Username", username)
                                .build();

                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    } else {
                        log.warn("User does not have required role for request: {}", requestPath);
                        throw new UnauthorizedAccessException("User does not have required role");
                    }

                } catch (Exception e) {
                    log.error("Unauthorized access attempt on {}: {}", requestPath, e.getMessage());
                    throw new UnauthorizedAccessException("Unauthorized access: Invalid or expired token");
                }
            }
            return chain.filter(exchange);  // If route is not secured, continue processing
        });
    }

    // Dynamic role retrieval (you can customize this based on your routes or services)
    private List<String> getRequiredRolesForRoute(String requestPath) {
        if (requestPath.startsWith("/api/v1/player")) {
            return List.of("ROLE_USER", "ROLE_ADMIN"); // Example: "ROLE_USER" and "ROLE_ADMIN" roles allowed for player service
        }
        return List.of("ROLE_USER"); // Default to "ROLE_USER" if no specific route
    }

    public static class Config {}
}
