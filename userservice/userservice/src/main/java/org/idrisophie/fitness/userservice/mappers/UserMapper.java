package org.idrisophie.fitness.userservice.mappers;

import org.idrisophie.fitness.userservice.dto.RegistreRequest;
import org.idrisophie.fitness.userservice.dto.UserResponse;
import org.idrisophie.fitness.userservice.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegistreRequest request);
    
    @Mapping(source = "password", target = "password")
    UserResponse toResponse(User user);
}
