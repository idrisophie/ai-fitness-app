package org.idrisophie.fitness.userservice.controllers;

import org.idrisophie.fitness.userservice.dto.*;
import org.idrisophie.fitness.userservice.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable String userId){
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }
        
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registre(@Valid @RequestBody RegistreRequest request){
        return ResponseEntity.ok(userService.registre(request));
    }

    @GetMapping("/{userId}/validate")
    public ResponseEntity<Boolean> validateUser(@PathVariable String userId){
        return ResponseEntity.ok(userService.existeByUserId(userId));
    }

    @PostMapping("/test-user")
    public ResponseEntity<String> createTestUser() {
        RegistreRequest request = new RegistreRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setKeycloakId("test-keycloak-id-123");
        
        try {
            UserResponse user = userService.registre(request);
            return ResponseEntity.ok("Test user created: " + user.getEmail());
        } catch (Exception e) {
            return ResponseEntity.ok("Error creating test user: " + e.getMessage());
        }
    }

}
