package com.codementor.recommendation;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Rule-based recommender (pure Java, no framework dependencies).
 *
 * DAA concepts used
 *  - Scoring       : every unsolved problem gets a score from topic weakness, staleness, level fit, retry bonus.
 *  - Priority queue: candidates go into a max-heap (java.util.PriorityQueue) keyed by score.
 *  - Greedy        : repeatedly poll the best remaining candidate (subject to a per-topic cap for variety).
 *  - Sorting       : the heap yields candidates in descending score order (heap-sort style).
 *
 * Running time with P problems, A attempts, T topics per problem (T is a small constant), k results:
 *   topic statistics  O(A log A)   (sorting attempts per topic)
 *   scoring           O(P * T)
 *   heap insertion    O(P log P)
 *   k greedy polls    O(k log P)   ->  total O(A log A + P log P), dominated by the heap work as P grows.
 */
public class RecommendationEngine {

    public static final int STALE_CAP_DAYS = 14;   // staleness stops growing after 14 idle days
    public static final int RECENT_WINDOW = 10;    // only the latest 10 attempts in a topic count
    public static final int MAX_PER_TOPIC = 3;     // keeps the queue varied
    private static final List<String> LEVELS = List.of("EASY", "MEDIUM", "HARD");

    public record ProblemInfo(long id, String title, String difficulty, List<String> topics) { }
    public record AttemptInfo(long problemId, List<String> topics, String result, LocalDate date) { }
    public record Recommendation(long problemId, String title, String difficulty, List<String> topics,
                                 double score, String reason) { }

    private record TopicState(int attempts, double success, long daysSince) { }
    private record Scored(ProblemInfo problem, double score, String driver, String reason) { }

    public List<Recommendation> recommend(List<ProblemInfo> problems, List<AttemptInfo> attempts,
                                          LocalDate today, int limit) {
        Map<String, TopicState> topicStates = topicStates(attempts, today);

        Set<Long> solved = new HashSet<>();
        Set<Long> tried = new HashSet<>();
        for (AttemptInfo a : attempts) {
            tried.add(a.problemId());
            if ("SOLVED".equals(a.result())) solved.add(a.problemId());
        }
        int level = studentLevel(problems, solved);

        // Max-heap: highest score first, ties broken by lower id so results are deterministic.
        PriorityQueue<Scored> queue = new PriorityQueue<>(
                Comparator.comparingDouble(Scored::score).reversed().thenComparingLong(s -> s.problem().id()));

        for (ProblemInfo p : problems) {
            if (solved.contains(p.id())) continue;                // eligibility rule
            String driver = null;
            double best = -1;
            for (String t : p.topics()) {                         // the weakest topic drives the score
                double s = topicScore(topicStates.get(t));
                if (s > best) { best = s; driver = t; }
            }
            if (driver == null) continue;

            int distance = Math.abs(LEVELS.indexOf(p.difficulty()) - level);
            double fit = distance == 0 ? 15 : distance == 1 ? 5 : 0;
            boolean retry = tried.contains(p.id());
            double score = round(best + fit + (retry ? 10 : 0));
            queue.add(new Scored(p, score, driver,
                    explain(driver, topicStates.get(driver), distance == 0, p.difficulty(), retry)));
        }

        // Greedy selection: always take the best remaining candidate, at most MAX_PER_TOPIC per driver topic.
        List<Recommendation> result = new ArrayList<>();
        Map<String, Integer> used = new HashMap<>();
        while (!queue.isEmpty() && result.size() < limit) {
            Scored s = queue.poll();                               // O(log P)
            if (used.merge(s.driver(), 1, Integer::sum) > MAX_PER_TOPIC) continue;
            ProblemInfo p = s.problem();
            result.add(new Recommendation(p.id(), p.title(), p.difficulty(), p.topics(), s.score(), s.reason()));
        }
        return result;
    }

    /** 0..100. Weak topics (60%) and topics left idle (40%) score highest; unseen topics are neutral-high. */
    static double topicScore(TopicState st) {
        double weakness = st == null ? 0.5 : 1 - st.success();
        double staleness = st == null ? 1.0 : Math.min(st.daysSince(), STALE_CAP_DAYS) / (double) STALE_CAP_DAYS;
        return 60 * weakness + 40 * staleness;
    }

    private Map<String, TopicState> topicStates(List<AttemptInfo> attempts, LocalDate today) {
        Map<String, List<AttemptInfo>> byTopic = new HashMap<>();
        for (AttemptInfo a : attempts)
            for (String t : a.topics()) byTopic.computeIfAbsent(t, k -> new ArrayList<>()).add(a);

        Map<String, TopicState> states = new HashMap<>();
        byTopic.forEach((topic, list) -> {
            list.sort(Comparator.comparing(AttemptInfo::date).reversed());
            List<AttemptInfo> recent = list.subList(0, Math.min(RECENT_WINDOW, list.size()));
            double sum = 0;
            for (AttemptInfo a : recent) sum += weight(a.result());
            long days = Math.max(0, ChronoUnit.DAYS.between(list.get(0).date(), today));
            states.put(topic, new TopicState(list.size(), sum / recent.size(), days));
        });
        return states;
    }

    private static double weight(String result) {
        return switch (result) { case "SOLVED" -> 1.0; case "PARTIAL" -> 0.5; default -> 0.0; };
    }

    /** 0 = Easy, 1 = Medium, 2 = Hard: moves up once the student has solved 3 problems of the current level. */
    private int studentLevel(List<ProblemInfo> problems, Set<Long> solved) {
        int easy = 0, medium = 0;
        for (ProblemInfo p : problems) {
            if (!solved.contains(p.id())) continue;
            if ("EASY".equals(p.difficulty())) easy++;
            else if ("MEDIUM".equals(p.difficulty())) medium++;
        }
        return easy < 3 ? 0 : medium < 3 ? 1 : 2;
    }

    private String explain(String topic, TopicState st, boolean levelFits, String difficulty, boolean retry) {
        StringBuilder sb = new StringBuilder();
        if (st == null) {
            sb.append("You have not tried any ").append(topic).append(" problems yet, so this is a good chance to explore it.");
        } else {
            int pct = (int) Math.round(st.success() * 100);
            sb.append("Practise ").append(topic).append(" next: your recent results in this topic ");
            sb.append(pct < 50 ? "were low (" : pct < 80 ? "were mixed (" : "were strong (").append(pct).append("% success)");
            if (st.daysSince() >= 3) sb.append(", and you have not reviewed it for ").append(st.daysSince()).append(" days.");
            else sb.append(", and you practised it recently.");
        }
        if (retry) sb.append(" You tried this problem before without solving it, so it is worth another go.");
        if (levelFits) sb.append(" It is ").append(difficulty.charAt(0)).append(difficulty.substring(1).toLowerCase())
                .append(", which fits your current level.");
        return sb.toString();
    }

    private static double round(double v) { return Math.round(v * 10) / 10.0; }
}
