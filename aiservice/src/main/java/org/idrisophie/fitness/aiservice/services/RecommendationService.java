package org.idrisophie.fitness.aiservice.services;

import lombok.RequiredArgsConstructor;
import org.idrisophie.fitness.aiservice.models.Recommendation;
import org.idrisophie.fitness.aiservice.repositories.RecommendationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private RecommendationRepository recommendationRepository;

    public List<Recommendation> getUserRecommendation(String userId){
        return recommendationRepository.findByUserId(userId);
    }
    public Recommendation getActivityRecommendation(String activityId){
        return recommendationRepository.findByActivityId(activityId)
                .orElseThrow(() -> new RuntimeException("No Recommendation Found for this activity" + activityId));
    }


}
