package org.idrisophie.fitness.userservice.repositories;

import org.idrisophie.fitness.userservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String>{
    public boolean existsByEmail(String email);
}
