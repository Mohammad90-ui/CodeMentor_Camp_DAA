package com.codementor.service;

import com.codementor.dto.Dtos.*;
import com.codementor.model.*;
import com.codementor.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class ProblemService {
    private final ProblemRepository problems;
    private final AttemptRepository attempts;
    private final BookmarkRepository bookmarks;

    public ProblemService(ProblemRepository problems, AttemptRepository attempts, BookmarkRepository bookmarks) {
        this.problems = problems; this.attempts = attempts; this.bookmarks = bookmarks;
    }

    /** Searching (title/topic match) + filtering + sorting (by title, difficulty or time). */
    public List<ProblemDto> search(User user, String q, String topic, String difficulty, String sort) {
        Map<Long, String> status = statusMap(user);
        Set<Long> marked = bookmarkedIds(user);
        String needle = q == null ? "" : q.trim().toLowerCase();

        Comparator<Problem> order = switch (sort == null ? "title" : sort) {
            case "difficulty" -> Comparator.comparing(Problem::getDifficulty).thenComparing(Problem::getTitle);
            case "time" -> Comparator.comparingInt(Problem::getEstimatedMinutes).thenComparing(Problem::getTitle);
            default -> Comparator.comparing(Problem::getTitle);
        };

        return problems.findAll().stream()
                .filter(p -> needle.isEmpty() || p.getTitle().toLowerCase().contains(needle)
                        || p.getTopics().stream().anyMatch(t -> t.getName().toLowerCase().contains(needle)))
                .filter(p -> blank(topic) || p.getTopics().stream().anyMatch(t -> t.getName().equalsIgnoreCase(topic)))
                .filter(p -> blank(difficulty) || p.getDifficulty().name().equalsIgnoreCase(difficulty))
                .sorted(order)
                .map(p -> toDto(p, status, marked))
                .toList();
    }

    public ProblemDetailDto detail(User user, Long id) {
        Problem p = find(id);
        Map<Long, String> status = statusMap(user);
        Set<Long> marked = bookmarkedIds(user);
        List<AttemptDto> history = attempts.findByUserAndProblemOrderByCreatedAtDesc(user, p)
                .stream().map(AttemptDto::from).toList();
        return new ProblemDetailDto(p.getId(), p.getTitle(), p.getDescription(), p.getDifficulty().name(),
                p.getEstimatedMinutes(), p.getExternalUrl(), RecommendationService.topicNames(p),
                marked.contains(p.getId()), status.getOrDefault(p.getId(), "NEW"), history);
    }

    public Problem find(Long id) {
        return problems.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));
    }

    public ProblemDto toDto(Problem p, Map<Long, String> status, Set<Long> marked) {
        return new ProblemDto(p.getId(), p.getTitle(), p.getDifficulty().name(), p.getEstimatedMinutes(),
                RecommendationService.topicNames(p), marked.contains(p.getId()), status.getOrDefault(p.getId(), "NEW"));
    }

    public Map<Long, String> statusMap(User user) {
        Map<Long, String> status = new HashMap<>();
        for (Attempt a : attempts.findByUserOrderByCreatedAtDesc(user)) {
            Long id = a.getProblem().getId();
            if (a.getResult() == Result.SOLVED) status.put(id, "SOLVED");
            else status.putIfAbsent(id, "ATTEMPTED");
        }
        return status;
    }

    public Set<Long> bookmarkedIds(User user) {
        Set<Long> ids = new HashSet<>();
        for (Bookmark b : bookmarks.findByUser(user)) ids.add(b.getProblem().getId());
        return ids;
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }
}
