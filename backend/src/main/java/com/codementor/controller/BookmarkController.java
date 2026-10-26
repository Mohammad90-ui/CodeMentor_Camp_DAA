package com.codementor.controller;

import com.codementor.dto.Dtos.*;
import com.codementor.model.*;
import com.codementor.repository.BookmarkRepository;
import com.codementor.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {
    private final BookmarkRepository bookmarks;
    private final ProblemService problems;
    private final UserService users;

    public BookmarkController(BookmarkRepository bookmarks, ProblemService problems, UserService users) {
        this.bookmarks = bookmarks; this.problems = problems; this.users = users;
    }

    @GetMapping
    public List<ProblemDto> list(Authentication auth) {
        User u = users.current(auth);
        Map<Long, String> status = problems.statusMap(u);
        Set<Long> marked = problems.bookmarkedIds(u);
        return bookmarks.findByUser(u).stream()
                .map(b -> problems.toDto(b.getProblem(), status, marked))
                .sorted(Comparator.comparing(ProblemDto::title)).toList();
    }

    @PostMapping("/{problemId}") @ResponseStatus(HttpStatus.CREATED)
    public void add(Authentication auth, @PathVariable Long problemId) {
        User u = users.current(auth);
        Problem p = problems.find(problemId);
        if (bookmarks.findByUserAndProblem(u, p).isEmpty()) {
            Bookmark b = new Bookmark();
            b.setUser(u); b.setProblem(p);
            bookmarks.save(b);
        }
    }

    @DeleteMapping("/{problemId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(Authentication auth, @PathVariable Long problemId) {
        User u = users.current(auth);
        bookmarks.findByUserAndProblem(u, problems.find(problemId)).ifPresent(bookmarks::delete);
    }
}
