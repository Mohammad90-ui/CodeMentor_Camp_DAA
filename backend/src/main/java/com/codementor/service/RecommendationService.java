package com.codementor.service;

import com.codementor.model.*;
import com.codementor.recommendation.RecommendationEngine;
import com.codementor.recommendation.RecommendationEngine.*;
import com.codementor.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/** Adapts database entities to the framework-free RecommendationEngine. */
@Service
public class RecommendationService {
    private final ProblemRepository problems;
    private final AttemptRepository attempts;
    private final RecommendationEngine engine = new RecommendationEngine();

    public RecommendationService(ProblemRepository problems, AttemptRepository attempts) {
        this.problems = problems; this.attempts = attempts;
    }

    public List<Recommendation> forUser(User user, int limit) {
        List<ProblemInfo> infos = problems.findAll().stream()
                .map(p -> new ProblemInfo(p.getId(), p.getTitle(), p.getDifficulty().name(), topicNames(p))).toList();
        List<AttemptInfo> history = attempts.findByUserOrderByCreatedAtDesc(user).stream()
                .map(a -> new AttemptInfo(a.getProblem().getId(), topicNames(a.getProblem()),
                        a.getResult().name(), a.getCreatedAt().toLocalDate())).toList();
        return engine.recommend(infos, history, LocalDate.now(), limit);
    }

    static List<String> topicNames(Problem p) {
        return p.getTopics().stream().map(Topic::getName).sorted().toList();
    }
}
