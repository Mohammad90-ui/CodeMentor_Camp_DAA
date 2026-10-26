package com.codementor.controller;

import com.codementor.dto.Dtos.*;
import com.codementor.recommendation.RecommendationEngine.Recommendation;
import com.codementor.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final DashboardService dashboard;
    private final RecommendationService recommendations;
    private final UserService users;

    public DashboardController(DashboardService dashboard, RecommendationService recommendations, UserService users) {
        this.dashboard = dashboard; this.recommendations = recommendations; this.users = users;
    }

    @GetMapping("/dashboard")
    public DashboardDto dashboard(Authentication auth) { return dashboard.dashboard(users.current(auth)); }

    @GetMapping("/topics/progress")
    public List<TopicProgressDto> topicProgress(Authentication auth) { return dashboard.topicProgress(users.current(auth)); }

    @GetMapping("/recommendations")
    public List<Recommendation> recommendations(Authentication auth, @RequestParam(defaultValue = "10") int limit) {
        return recommendations.forUser(users.current(auth), Math.max(1, Math.min(limit, 30)));
    }
}
