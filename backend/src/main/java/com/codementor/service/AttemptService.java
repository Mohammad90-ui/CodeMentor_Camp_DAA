package com.codementor.service;

import com.codementor.dto.Dtos.*;
import com.codementor.model.*;
import com.codementor.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttemptService {
    private final AttemptRepository attempts;
    private final ProblemService problemService;

    public AttemptService(AttemptRepository attempts, ProblemService problemService) {
        this.attempts = attempts; this.problemService = problemService;
    }

    public AttemptDto create(User user, AttemptRequest req) {
        Attempt a = new Attempt();
        a.setUser(user);
        a.setProblem(problemService.find(req.problemId()));
        a.setResult(req.result());
        a.setTimeSpentMinutes(req.timeSpentMinutes());
        a.setMistakeType(req.mistakeType() == null || req.mistakeType().isBlank() ? null : req.mistakeType());
        a.setNotes(req.notes());
        return AttemptDto.from(attempts.save(a));
    }

    public List<AttemptDto> history(User user) {
        return attempts.findByUserOrderByCreatedAtDesc(user).stream().map(AttemptDto::from).toList();
    }
}
