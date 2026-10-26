package com.codementor.service;

import com.codementor.model.*;
import com.codementor.recommendation.RecommendationEngine.Recommendation;
import com.codementor.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {
    @Mock ProblemRepository problems;
    @Mock AttemptRepository attempts;

    private Topic topic(String name) { Topic t = new Topic(name); t.setId((long) name.hashCode()); return t; }

    private Problem problem(long id, String title, Topic t) {
        Problem p = new Problem();
        p.setId(id); p.setTitle(title); p.setDifficulty(Difficulty.EASY); p.setTopics(Set.of(t));
        return p;
    }

    @Test
    void recommendsWeakStaleTopicAndSkipsSolvedProblems() {
        Topic arrays = topic("Arrays"), strings = topic("Strings");
        Problem a1 = problem(1, "A1", arrays), a2 = problem(2, "A2", arrays);
        Problem s1 = problem(3, "S1", strings), s2 = problem(4, "S2", strings);
        when(problems.findAll()).thenReturn(List.of(a1, a2, s1, s2));

        Attempt failedArrays = new Attempt();
        failedArrays.setProblem(a1); failedArrays.setResult(Result.UNSOLVED);
        failedArrays.setCreatedAt(LocalDateTime.now().minusDays(12));
        Attempt solvedStrings = new Attempt();
        solvedStrings.setProblem(s1); solvedStrings.setResult(Result.SOLVED);
        solvedStrings.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(attempts.findByUserOrderByCreatedAtDesc(any())).thenReturn(List.of(solvedStrings, failedArrays));

        List<Recommendation> recs = new RecommendationService(problems, attempts).forUser(new User(), 5);

        assertEquals("Arrays", recs.get(0).topics().get(0));
        assertTrue(recs.get(0).reason().contains("12 days"));
        assertTrue(recs.stream().noneMatch(r -> r.problemId() == 3), "solved problem must not be recommended");
    }
}
