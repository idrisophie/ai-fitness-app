package org.idrisophie.fitness.userservice.factories;

import org.idrisophie.fitness.userservice.dto.RegistreRequest;
import org.idrisophie.fitness.userservice.models.User;
import org.idrisophie.fitness.userservice.models.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {
    
    public User createUser(RegistreRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(UserRole.USER);
        return user;
    }
    
    public User createUser(String email, String password, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(UserRole.USER);
        return user;
    }
}
