package com.codementor.service;

import com.codementor.dto.Dtos.*;
import com.codementor.model.*;
import com.codementor.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class DashboardService {
    private final AttemptRepository attempts;
    private final ProblemRepository problems;
    private final TopicRepository topics;
    private final RecommendationService recommendations;

    public DashboardService(AttemptRepository attempts, ProblemRepository problems, TopicRepository topics,
                            RecommendationService recommendations) {
        this.attempts = attempts; this.problems = problems; this.topics = topics; this.recommendations = recommendations;
    }

    public DashboardDto dashboard(User user) {
        List<Attempt> mine = attempts.findByUserOrderByCreatedAtDesc(user);
        Set<Long> solved = new HashSet<>();
        for (Attempt a : mine) if (a.getResult() == Result.SOLVED) solved.add(a.getProblem().getId());

        LocalDate today = LocalDate.now();
        Map<LocalDate, Integer> perDay = new HashMap<>();
        for (Attempt a : mine) perDay.merge(a.getCreatedAt().toLocalDate(), 1, Integer::sum);
        List<DayActivity> week = new ArrayList<>();
        int weekly = 0;
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            int n = perDay.getOrDefault(d, 0);
            weekly += n;
            week.add(new DayActivity(d.toString(), n));
        }

        List<TopicProgressDto> weak = topicProgress(user).stream()
                .filter(t -> t.attempts() > 0)
                .sorted(Comparator.comparingInt(TopicProgressDto::successRate).thenComparing(TopicProgressDto::topic))
                .limit(3).toList();

        return new DashboardDto(mine.size(), solved.size(), (int) problems.count(), weekly, user.getWeeklyGoal(), week, weak,
                mine.stream().limit(5).map(AttemptDto::from).toList(),
                recommendations.forUser(user, 3));
    }

    public List<TopicProgressDto> topicProgress(User user) {
        List<Attempt> mine = attempts.findByUserOrderByCreatedAtDesc(user);
        List<Problem> all = problems.findAll();
        List<TopicProgressDto> out = new ArrayList<>();
        for (Topic t : topics.findAll()) {
            int total = 0, tried = 0, solvedAttempts = 0;
            Set<Long> solvedProblems = new HashSet<>();
            for (Problem p : all) if (hasTopic(p, t)) total++;
            for (Attempt a : mine) {
                if (!hasTopic(a.getProblem(), t)) continue;
                tried++;
                if (a.getResult() == Result.SOLVED) { solvedAttempts++; solvedProblems.add(a.getProblem().getId()); }
            }
            int rate = tried == 0 ? 0 : (int) Math.round(100.0 * solvedAttempts / tried);
            out.add(new TopicProgressDto(t.getName(), tried, solvedAttempts, rate, solvedProblems.size(), total));
        }
        out.sort(Comparator.comparing(TopicProgressDto::topic));
        return out;
    }

    private static boolean hasTopic(Problem p, Topic t) {
        return p.getTopics().stream().anyMatch(x -> x.getName().equals(t.getName()));
    }
}
