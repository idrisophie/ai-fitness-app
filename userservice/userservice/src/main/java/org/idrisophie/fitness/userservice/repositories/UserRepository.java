package org.idrisophie.fitness.userservice.repositories;

import org.idrisophie.fitness.userservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>{
    public boolean existsByEmail(String email);
    Boolean existsByKeycloakId(String keycloakId);
    User findByEmail(String email);
    Optional<User> findByKeycloakId(String keycloakId);
}
