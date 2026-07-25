package org.idrisophie.fitness.activityService.mappers;

import org.idrisophie.fitness.activityService.dto.ActivityRequest;
import org.idrisophie.fitness.activityService.dto.ActivityResponse;
import org.idrisophie.fitness.activityService.models.Activity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ActivityMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Activity toEntity(ActivityRequest request);
    
    @Mapping(source = "addtionalMetrics", target = "addtionalMetrics")
    ActivityResponse toResponse(Activity activity);
}
