package org.idrisophie.fitness.aiservice.controllers;

import lombok.RequiredArgsConstructor;
import org.idrisophie.fitness.aiservice.models.Recommendation;
import org.idrisophie.fitness.aiservice.services.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private RecommendationService recommendationService;

    @GetMapping("/user/{usersId}")
    public ResponseEntity<List<Recommendation>> getUserRecommendation(@PathVariable String userId){
        return ResponseEntity.ok(recommendationService.getUserRecommendation(userId));
    }

    @GetMapping("/user/{activityId}")
    public ResponseEntity<Recommendation> getActivityRecommendation(@PathVariable String activityId){
        return ResponseEntity.ok((Recommendation) recommendationService.getUserRecommendation(activityId));
    }


}
