package com.fitness.gateway;

import com.fitness.gateway.user.RegisterRequest;
import com.fitness.gateway.user.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeyCloackUserSyncFilter implements WebFilter {
    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        RegisterRequest requestRegister = getUserDetails(token);
        
        log.info("Incoming request - X-User-ID: {}, Has Token: {}", userId, token != null);
        
        if (userId == null && requestRegister != null){
            userId = requestRegister.getKeycloackId();
            log.info("Using Keycloak ID as user ID: {}", userId);
        }

        if (userId != null && token != null) {
            String finalUserId = userId;
            return userService.validateUser(userId)
                    .flatMap(exist -> {
                        if (!exist) {
                            log.info("User does not exist, registering...");
                            if (requestRegister != null) {
                                return userService.registerUser(requestRegister)
                                        .doOnNext(user -> log.info("User registered with ID: {}", user.getId()))
                                        .then(Mono.empty());
                            } else {
                                return Mono.empty();
                            }
                        } else {
                            log.info("User already exists, skipping sync");
                            return Mono.empty();
                        }
                    })
                    .then(Mono.defer(() -> {
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-User-ID", finalUserId)
                                .build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }));
        }
        return chain.filter(exchange);
    }

    private RegisterRequest getUserDetails(String token) {
        try {
            if (token == null) {
                return null;
            }
            
            String tokenWithoutBearer = token.replace("Bearer", "").trim();
            SignedJWT signedJwt = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();

            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setEmail(claims.getStringClaim("email"));
            registerRequest.setKeycloackId(claims.getStringClaim("sub"));
            registerRequest.setPassword("@dummy");
            registerRequest.setFirstName(claims.getStringClaim("given_name"));
            registerRequest.setLastName(claims.getStringClaim("family_name"));

            log.info("Extracted user details from token - Email: {}, Keycloak ID: {}", 
                    registerRequest.getEmail(), registerRequest.getKeycloackId());
            
            return registerRequest;
        } catch (Exception e) {
            log.error("Error parsing JWT token", e);
            return null;
        }
    }
}