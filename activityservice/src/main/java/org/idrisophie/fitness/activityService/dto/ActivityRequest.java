package org.idrisophie.fitness.activityService.dto;

import java.time.LocalDateTime;
import java.util.Map;

import org.idrisophie.fitness.activityService.models.ActivityType;

import lombok.Data;

@Data
public class ActivityRequest {
    private String userId;
    private ActivityType type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> additionalMetrics;
}
