package org.idrisophie.fitness.userservice.services;


import org.idrisophie.fitness.userservice.dto.RegistreRequest;
import org.idrisophie.fitness.userservice.dto.UserResponse;
import org.idrisophie.fitness.userservice.exceptions.DuplicateResourceException;
import org.idrisophie.fitness.userservice.exceptions.ResourceNotFoundException;
import org.idrisophie.fitness.userservice.mappers.UserMapper;
import org.idrisophie.fitness.userservice.models.User;
import org.idrisophie.fitness.userservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceDefault implements UserService {

    @Autowired
    private UserRepository repository;
    
    @Autowired
    private UserMapper userMapper;
    
    public UserResponse registre(RegistreRequest request){

        if(repository.existsByEmail(request.getEmail())){
            throw new DuplicateResourceException("Email already exist!");
        }
        
        // Vérifier si keycloakId est fourni et s'assurer de son unicité
        if(request.getKeycloakId() != null && !request.getKeycloakId().isEmpty()) {
            if(repository.existsByKeycloakId(request.getKeycloakId())){
                throw new DuplicateResourceException("Keycloak ID already exist!");
            }
        }
        
        User user = userMapper.toEntity(request);
        User savedUser = repository.save(user);
        return userMapper.toResponse(savedUser);
        
    }

    public UserResponse getUserProfile(String userId){
        User user = repository.findById(userId)
                    .orElseThrow( () -> new ResourceNotFoundException("User Not Found"));
        return userMapper.toResponse(user);
    }

    @Override
    public Boolean existeByUserId(String userId) {
        return repository.existsById(userId);
    }
    
    public User findByKeycloakId(String keycloakId) {
        return repository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Keycloak ID: " + keycloakId));
    }
}
