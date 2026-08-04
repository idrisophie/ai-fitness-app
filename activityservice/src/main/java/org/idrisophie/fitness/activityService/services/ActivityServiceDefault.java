package org.idrisophie.fitness.activityService.services;

import org.idrisophie.fitness.activityService.dto.ActivityRequest;
import org.idrisophie.fitness.activityService.dto.ActivityResponse;
import org.idrisophie.fitness.activityService.mappers.ActivityMapper;
import org.idrisophie.fitness.activityService.models.Activity;
import org.idrisophie.fitness.activityService.repositories.ActivityRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityServiceDefault implements ActivityService {

    private final ActivityRepository repository;
    private final ActivityMapper activityMapper;
    private final UserValidationService userValidationService;

    public ActivityServiceDefault(ActivityRepository repository, ActivityMapper activityMapper, UserValidationService userValidationService) {
        this.repository = repository;
        this.activityMapper = activityMapper;
        this.userValidationService = userValidationService;
    }

    public ActivityResponse trackActivity(ActivityRequest request){
        boolean isValidUser = userValidationService.validateUser(request.getUserId());
        if(!isValidUser){
            throw new RuntimeException("Invalid User: "+request.getUserId());
        }

        Activity activity = activityMapper.toEntity(request);
        Activity savedActivity = repository.save(activity);
        return activityMapper.toResponse(savedActivity);
    }

    public List<ActivityResponse> getUserActivities(String userId){
        List<Activity> activities = repository.findByUserId(userId);
        return activities.stream()
                .map(activityMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ActivityResponse getActivityById(String activityId){
        Activity activity = repository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        return activityMapper.toResponse(activity);
    }
}
