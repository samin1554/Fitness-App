package com.fitness.Aiservice.service;

import com.fitness.Aiservice.model.Recommendation;
import com.fitness.Aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RecommendationRepository recommendationRepository;

    public List<Recommendation> getUserRecommendations(String userId) {
        return recommendationRepository.findByUserId(userId);
    }

    public Recommendation getActivityRecommendations(String activityId) {
       return recommendationRepository.findByActivityId(activityId)
               .orElseThrow(()-> new RuntimeException("No Recommendation found"));
    }
}
