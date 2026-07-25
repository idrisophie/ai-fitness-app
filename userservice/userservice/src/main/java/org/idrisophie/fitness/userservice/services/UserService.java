package org.idrisophie.fitness.userservice.services;

import org.idrisophie.fitness.userservice.dto.RegistreRequest;
import org.idrisophie.fitness.userservice.dto.UserResponse;

public interface UserService {
    UserResponse registre(RegistreRequest request);
    UserResponse getUserProfile(String userId);
}
