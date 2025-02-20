package com.karam.teamup.authentication.security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class UserSecurity implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext ctx) {
        // Extract {username} from the request path
        String username = ctx.getVariables().get("username");

        // Get the authenticated user's username from JWT
        Authentication authentication = authenticationSupplier.get();
        String authenticatedUsername = getUsernameFromAuthentication(authentication);

        // Check if the authenticated user is authorized to access the resource
        boolean isAuthorized = authenticatedUsername != null && authenticatedUsername.equals(username);
        return new AuthorizationDecision(isAuthorized);
    }

    private String getUsernameFromAuthentication(Authentication authentication) {
        // Assuming PlayerPrincipal has a getUsername() method
        if (authentication != null && authentication.getPrincipal() instanceof PlayerPrincipal playerPrincipal) {
            authentication.getPrincipal();
            return playerPrincipal.getUsername();
        }
        return null;
    }
}
