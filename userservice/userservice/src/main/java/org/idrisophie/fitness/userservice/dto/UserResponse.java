package org.idrisophie.fitness.userservice.dto;

import java.time.LocalDateTime;

import org.idrisophie.fitness.userservice.models.UserRole;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UserRole role = UserRole.USER;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
