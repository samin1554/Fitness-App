package com.fitness.activityservice.service;

import com.fitness.activityservice.ActivityRepository;
import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routing;

    public ActivityResponse trackActivity(ActivityRequest request) {
        log.info("Tracking activity for user: {}", request.getUserId());
        boolean isValidUser = userValidationService.validateUser(request.getUserId());
        if (!isValidUser){
            throw new RuntimeException("Invalid User: " + request.getUserId());
        }
        
        // Ensure startTime is set
        LocalDateTime startTime = request.getStartTime();
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        
        Activity activity = Activity.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .duration(request.getDuration())
                .caloriesBurnt(request.getCaloriesBurnt())
                .startTime(startTime)
                .additionalMetrics(request.getAdditionalMetrics())
                .build();

        Activity savedActivity = activityRepository.save(activity);
        log.info("Activity saved with ID: {}", savedActivity.getId());

        try {
            rabbitTemplate.convertAndSend(exchange, routing, savedActivity);
            log.info("Activity sent to RabbitMQ");
        } catch(Exception e){
            log.error("Error sending message to RabbitMQ: " + e.getMessage(), e);
        }

        return mapResponse(savedActivity);
    }

    private ActivityResponse mapResponse(Activity activity){
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setType(activity.getType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurnt(activity.getCaloriesBurnt());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMetrics(activity.getAdditionalMetrics());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        return response;
    }

    public List<ActivityResponse> getUserActivity(String userId) {
        List<Activity> activities = activityRepository.findByUserId(userId);
        return activities.stream().map(this::mapResponse).collect(Collectors.toList());
    }

    public ActivityResponse getActivityById(String activityId) {
        return activityRepository.findById(activityId)
                .map(this::mapResponse)
                .orElseThrow(()-> new RuntimeException("Activity Not found with Id" + activityId));
    }
}