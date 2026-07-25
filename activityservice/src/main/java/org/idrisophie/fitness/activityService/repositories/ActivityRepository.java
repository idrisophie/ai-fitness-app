package org.idrisophie.fitness.activityService.repositories;

import org.idrisophie.fitness.activityService.models.Activity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ActivityRepository extends MongoRepository<Activity, String>{
    
}
