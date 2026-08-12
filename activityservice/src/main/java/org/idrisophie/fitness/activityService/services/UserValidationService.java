package org.idrisophie.fitness.activityService.services;

import lombok.RequiredArgsConstructor;
import org.idrisophie.fitness.activityService.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class UserValidationService {
    private final WebClient userServiceWebClient;

    public UserValidationService(WebClient userServiceWebClient) {
        this.userServiceWebClient = userServiceWebClient;
    }

    public boolean validateUser(String userId) {
        try {
            return userServiceWebClient
                    .get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND)
                throw new ResourceNotFoundException("User Not Found: " + userId);
            else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new ResourceNotFoundException("Invalid Request: " + userId);
            }
        }
        return false;
    }
}
