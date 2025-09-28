package com.fitness.gateway.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final WebClient userSeriveWebClient;

    public Mono<Boolean> validateUser(String userId){
        log.info("Checking if user exists by userId: {}", userId);
        if (userId == null || userId.isEmpty()) {
            return Mono.just(false);
        }
        
        return userSeriveWebClient.get()
                .uri("/api/users/{userId}/validate", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(WebClientResponseException.class, error -> {
                    log.error("User validation error for userId {}: status={}, body={}", 
                             userId, error.getStatusCode(), error.getResponseBodyAsString());
                    if (error.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new RuntimeException("User Not Found: " + userId));
                    } else if (error.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.error(new RuntimeException("Invalid User Id: " + userId));
                    }
                    return Mono.just(false);
                })
                .onErrorResume(Exception.class, error -> {
                    log.error("Unexpected error during user validation for userId: " + userId, error);
                    return Mono.just(false);
                });
    }

    public Mono<UserResponse> registerUser(RegisterRequest requestRegister) {
        log.info("Registering user with email: {}", requestRegister.getEmail());
        return userSeriveWebClient.post()
                .uri("/api/users/register")
                .bodyValue(requestRegister)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(WebClientResponseException.class, error -> {
                    log.error("Registration error: status={}, body={}", 
                             error.getStatusCode(), error.getResponseBodyAsString());
                    if (error.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.error(new RuntimeException("Invalid registration data"));
                    } else if (error.getStatusCode() == HttpStatus.CONFLICT) {
                        return Mono.error(new RuntimeException("User already exists"));
                    }
                    return Mono.error(new RuntimeException("Registration failed"));
                })
                .onErrorResume(Exception.class, error -> {
                    log.error("Unexpected error during registration", error);
                    return Mono.error(new RuntimeException("Registration failed: " + error.getMessage()));
                });
    }
}