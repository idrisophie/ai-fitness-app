package org.idrisophie.fitness.activityService.services;

import org.idrisophie.fitness.activityService.dto.ActivityRequest;
import org.idrisophie.fitness.activityService.dto.ActivityResponse;
import org.idrisophie.fitness.activityService.mappers.ActivityMapper;
import org.idrisophie.fitness.activityService.models.Activity;
import org.idrisophie.fitness.activityService.repositories.ActivityRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityServiceDefault implements ActivityService {

    private final ActivityRepository repository;
    private final ActivityMapper activityMapper;
    private final UserValidationService userValidationService;
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    @Value("${rabbitmq.exchange.key}")
    private String routingkey;

    public ActivityResponse trackActivity(ActivityRequest request){
        boolean isValidUser = userValidationService.validateUser(request.getUserId());
        if(!isValidUser){
            throw new RuntimeException("Invalid User: "+request.getUserId());
        }

        Activity activity = activityMapper.toEntity(request);
        Activity savedActivity = repository.save(activity);

        //Publish to RabbitMQ for AI Processing
        try {
            rabbitTemplate.convertAndSend(exchange, routingkey, savedActivity);
        } catch (Exception e) {
            log.error("Failed to publish activity to rabbitMQ : ", e);
        }
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
