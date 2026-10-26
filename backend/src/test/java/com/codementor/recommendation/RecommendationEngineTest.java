package com.codementor.recommendation;

import com.codementor.recommendation.RecommendationEngine.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationEngineTest {
    private final RecommendationEngine engine = new RecommendationEngine();
    private final LocalDate today = LocalDate.of(2026, 10, 22);

    private ProblemInfo p(long id, String diff, String... topics) {
        return new ProblemInfo(id, "P" + id, diff, List.of(topics));
    }
    private AttemptInfo a(long pid, String result, int daysAgo, String... topics) {
        return new AttemptInfo(pid, List.of(topics), result, today.minusDays(daysAgo));
    }

    @Test
    void solvedProblemsAreNeverRecommended() {
        var problems = List.of(p(1, "EASY", "Arrays"), p(2, "EASY", "Arrays"));
        var attempts = List.of(a(1, "SOLVED", 1, "Arrays"));
        var recs = engine.recommend(problems, attempts, today, 5);
        assertEquals(1, recs.size());
        assertEquals(2, recs.get(0).problemId());
    }

    @Test
    void weakAndStaleTopicBeatsStrongRecentTopic() {
        var problems = List.of(p(1, "EASY", "Arrays"), p(2, "EASY", "Strings"),
                p(3, "EASY", "Arrays"), p(4, "EASY", "Strings"));
        var attempts = List.of(
                a(1, "UNSOLVED", 12, "Arrays"),
                a(2, "SOLVED", 1, "Strings"));
        var recs = engine.recommend(problems, attempts, today, 4);
        assertEquals("Arrays", recs.get(0).topics().get(0));
        assertTrue(recs.get(0).reason().contains("12 days"));
        assertTrue(recs.get(0).reason().contains("Arrays"));
    }

    @Test
    void recommendationsChangeAfterANewSuccessfulAttempt() {
        var problems = List.of(p(1, "EASY", "Arrays"), p(2, "EASY", "Arrays"), p(3, "EASY", "Strings"));
        var before = new ArrayList<>(List.of(a(1, "UNSOLVED", 12, "Arrays"), a(9, "SOLVED", 1, "Strings")));
        var first = engine.recommend(problems, before, today, 3);
        assertEquals("Arrays", first.get(0).topics().get(0));

        before.add(a(2, "SOLVED", 0, "Arrays"));       // student now solves an Arrays problem today
        var second = engine.recommend(problems, before, today, 3);
        assertTrue(second.get(0).score() <= first.get(0).score());
        assertTrue(second.stream().noneMatch(r -> r.problemId() == 2));
    }

    @Test
    void untouchedTopicsGetAnExplorationReason() {
        var recs = engine.recommend(List.of(p(1, "EASY", "Recursion")), List.of(), today, 3);
        assertTrue(recs.get(0).reason().contains("not tried any Recursion"));
    }

    @Test
    void perTopicCapAndLimitAreRespected() {
        List<ProblemInfo> problems = new ArrayList<>();
        for (int i = 1; i <= 10; i++) problems.add(p(i, "EASY", "Arrays"));
        problems.add(p(100, "EASY", "Strings"));
        var recs = engine.recommend(problems, List.of(), today, 10);
        long arrays = recs.stream().filter(r -> r.topics().contains("Arrays")).count();
        assertEquals(RecommendationEngine.MAX_PER_TOPIC, arrays);
        assertEquals(RecommendationEngine.MAX_PER_TOPIC + 1, recs.size());
        assertEquals(2, engine.recommend(problems, List.of(), today, 2).size());
    }

    @Test
    void resultsAreSortedByScoreDescending() {
        var problems = List.of(p(1, "EASY", "Arrays"), p(2, "HARD", "Arrays"), p(3, "EASY", "Strings"));
        var recs = engine.recommend(problems, List.of(a(3, "PARTIAL", 5, "Strings")), today, 3);
        for (int i = 1; i < recs.size(); i++)
            assertTrue(recs.get(i - 1).score() >= recs.get(i).score());
    }
}
