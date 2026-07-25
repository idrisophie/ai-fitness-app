package org.idrisophie.fitness.activityService.services;

import org.idrisophie.fitness.activityService.dto.ActivityRequest;
import org.idrisophie.fitness.activityService.dto.ActivityResponse;

public interface ActivityService {
    ActivityResponse trackActivity(ActivityRequest request);
}
