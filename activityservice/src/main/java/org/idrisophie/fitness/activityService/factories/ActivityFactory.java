package org.idrisophie.fitness.activityService.factories;

import org.idrisophie.fitness.activityService.dto.ActivityRequest;
import org.idrisophie.fitness.activityService.models.Activity;
import org.idrisophie.fitness.activityService.models.ActivityType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ActivityFactory {
    
    public Activity createActivity(ActivityRequest request) {
        return Activity.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .addtionalMetrics(request.getAdditionalMetrics())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Activity createActivity(String userId, ActivityType type, Integer duration, 
                                   Integer caloriesBurned, LocalDateTime startTime) {
        return Activity.builder()
                .userId(userId)
                .type(type)
                .duration(duration)
                .caloriesBurned(caloriesBurned)
                .startTime(startTime)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
