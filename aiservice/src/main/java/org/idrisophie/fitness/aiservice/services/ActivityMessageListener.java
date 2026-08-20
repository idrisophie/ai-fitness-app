package org.idrisophie.fitness.aiservice.services;

import org.idrisophie.fitness.aiservice.models.Activity;
import org.idrisophie.fitness.aiservice.models.Recommendation;
import org.idrisophie.fitness.aiservice.repositories.RecommendationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ActivityMessageListener {
    private final ActivityAIService aiService;
    private final RecommendationRepository recommendationRepository;

    public ActivityMessageListener(ActivityAIService aiService, RecommendationRepository recommendationRepository) {
        this.aiService = aiService;
        this.recommendationRepository = recommendationRepository;
    }

    @RabbitListener(queues = "activity.queue")
    public void processActivity(Activity activity){
        log.info("Received activity for processing: {}", activity.getId());
   //     log.info("Generated Recommendation: {}", aiService.generateRecommendation(activity));
        Recommendation recommendation = aiService.generateRecommendation(activity);
        recommendationRepository.save(recommendation);
    }
}
