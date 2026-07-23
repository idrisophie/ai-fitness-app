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
        
    @PostMapping("/registre")
    public ResponseEntity<UserResponse> registre(@Valid @RequestBody RegistreRequest request){
        return ResponseEntity.ok(userService.registre(request));
    }
        

}
