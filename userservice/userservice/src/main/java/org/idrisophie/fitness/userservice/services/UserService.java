package org.idrisophie.fitness.userservice.services;

import org.idrisophie.fitness.userservice.dto.RegistreRequest;
import org.idrisophie.fitness.userservice.dto.UserResponse;

public interface UserService {
   public UserResponse registre(RegistreRequest request);
   public UserResponse getUserProfile(String userId);

   public Boolean existeByUserId(String userId);
}
