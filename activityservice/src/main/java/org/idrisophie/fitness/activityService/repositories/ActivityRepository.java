package org.idrisophie.fitness.activityService.repositories;

import org.idrisophie.fitness.activityService.models.Activity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ActivityRepository extends MongoRepository<Activity, String>{
    
    List<Activity> findByUserId(String userId);
}
