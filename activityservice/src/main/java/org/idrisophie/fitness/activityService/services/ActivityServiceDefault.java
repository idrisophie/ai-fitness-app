package org.idrisophie.fitness.activityService.services;

import org.idrisophie.fitness.activityService.dto.ActivityRequest;
import org.idrisophie.fitness.activityService.dto.ActivityResponse;
import org.idrisophie.fitness.activityService.mappers.ActivityMapper;
import org.idrisophie.fitness.activityService.models.Activity;
import org.idrisophie.fitness.activityService.repositories.ActivityRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityServiceDefault implements ActivityService {

    private final ActivityRepository repository;
    private final ActivityMapper activityMapper;
    
    public ActivityResponse trackActivity(ActivityRequest request){
        Activity activity = activityMapper.toEntity(request);
        Activity savedActivity = repository.save(activity);
        return activityMapper.toResponse(savedActivity);
    }
}
