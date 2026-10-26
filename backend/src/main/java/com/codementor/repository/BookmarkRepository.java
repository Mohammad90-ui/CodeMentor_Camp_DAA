package com.codementor.repository;
import com.codementor.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUser(User user);
    Optional<Bookmark> findByUserAndProblem(User user, Problem problem);
}
