package com.fitness.activityservice.controller;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.service.ActivityService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@AllArgsConstructor
@Slf4j
public class ActivityController {

    private ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityResponse> trackActivity(@RequestBody ActivityRequest request, @RequestHeader("X-User-ID") String userId){
        log.info("Received trackActivity request for user: {}", userId);
        log.info("Activity request: {}", request);
        
        if (userId != null){
            request.setUserId(userId);
        }
        
        ActivityResponse response = activityService.trackActivity(request);
        log.info("Activity tracked successfully with ID: {}", response.getId());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getUserActivity(@RequestHeader("X-User-ID")String userId){
        log.info("Received getUserActivity request for user: {}", userId);
        List<ActivityResponse> activities = activityService.getUserActivity(userId);
        log.info("Found {} activities for user: {}", activities.size(), userId);
        return ResponseEntity.ok(activities);
    }
    
    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable String activityId) {
        log.info("Received getActivity request for activity ID: {}", activityId);
        ActivityResponse activity = activityService.getActivityById(activityId);
        log.info("Found activity: {}", activityId);
        return ResponseEntity.ok(activity);
    }
}