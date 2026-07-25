package org.idrisophie.fitness.activityService.controllers;

import org.idrisophie.fitness.activityService.dto.ActivityRequest;
import org.idrisophie.fitness.activityService.dto.ActivityResponse;
import org.idrisophie.fitness.activityService.services.ActivityServiceDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/activies")
@RequiredArgsConstructor
public class ActivityController {
    
    private final ActivityServiceDefault activityService;

    @PostMapping
    public ResponseEntity<ActivityResponse> trackActivity(@RequestBody ActivityRequest request){
        return ResponseEntity.ok(activityService.trackActivity(request));
    }
}
