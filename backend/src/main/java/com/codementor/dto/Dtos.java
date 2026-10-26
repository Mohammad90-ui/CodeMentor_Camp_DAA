package com.codementor.dto;

import com.codementor.model.*;
import com.codementor.recommendation.RecommendationEngine.Recommendation;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/** All request/response shapes of the REST API in one place. */
public final class Dtos {
    private Dtos() { }

    // ----- auth & profile -----
    public record RegisterRequest(@NotBlank String name, @NotBlank @Email String email,
                                  @NotBlank @Size(min = 6, message = "must be at least 6 characters") String password) { }
    public record LoginRequest(@NotBlank String email, @NotBlank String password) { }
    public record ProfileRequest(@NotBlank String name, String preferredLanguage,
                                 @Min(0) @Max(100) Integer weeklyGoal) { }
    public record UserDto(Long id, String name, String email, String preferredLanguage, Integer weeklyGoal) {
        public static UserDto from(User u) {
            return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getPreferredLanguage(), u.getWeeklyGoal());
        }
    }
    public record AuthResponse(String token, UserDto user) { }

    // ----- problems -----
    /** status: NEW, ATTEMPTED or SOLVED (for the logged-in student). */
    public record ProblemDto(Long id, String title, String difficulty, int estimatedMinutes,
                             List<String> topics, boolean bookmarked, String status) { }
    public record ProblemDetailDto(Long id, String title, String description, String difficulty, int estimatedMinutes,
                                   String externalUrl, List<String> topics, boolean bookmarked, String status,
                                   List<AttemptDto> attempts) { }

    // ----- attempts -----
    public record AttemptRequest(@NotNull Long problemId, @NotNull Result result,
                                 @Min(0) @Max(1440) Integer timeSpentMinutes, String mistakeType,
                                 @Size(max = 1000) String notes) { }
    public record AttemptDto(Long id, Long problemId, String problemTitle, String result, Integer timeSpentMinutes,
                             String mistakeType, String notes, LocalDateTime createdAt) {
        public static AttemptDto from(Attempt a) {
            return new AttemptDto(a.getId(), a.getProblem().getId(), a.getProblem().getTitle(), a.getResult().name(),
                    a.getTimeSpentMinutes(), a.getMistakeType(), a.getNotes(), a.getCreatedAt());
        }
    }

    // ----- dashboard -----
    public record TopicProgressDto(String topic, int attempts, int solvedAttempts, int successRate,
                                   int solvedProblems, int totalProblems) { }
    public record DayActivity(String date, int attempts) { }
    public record DashboardDto(int totalAttempts, int solvedProblems, int totalProblems, int weeklyAttempts,
                               Integer weeklyGoal, List<DayActivity> weeklyActivity,
                               List<TopicProgressDto> weakTopics, List<AttemptDto> recent,
                               List<Recommendation> recommendations) { }
}
