package com.fitness.activityservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService  {
    private final WebClient userSeriveWebClient;
    public boolean validateUser(String userId){
        log.info("Checking if user exists by userId: {}", userId);
        if (userId == null || userId.isEmpty()) {
            log.warn("User ID is null or empty");
            return false;
        }
        
        try {
            Boolean result = userSeriveWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            
            log.info("User validation result for userId {}: {}", userId, result);
            return result != null && result;
        } catch (WebClientResponseException e) {
            log.error("User validation failed with status: {} and body: {}", 
                     e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("User Not Found: " + userId);
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new RuntimeException("Invalid User Id: " + userId);
            }
        } catch (Exception e) {
            log.error("Unexpected error during user validation for userId: " + userId, e);
        }
        return false;
    }
}