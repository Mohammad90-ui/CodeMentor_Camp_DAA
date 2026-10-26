package com.codementor.repository;
import com.codementor.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByUserOrderByCreatedAtDesc(User user);
    List<Attempt> findByUserAndProblemOrderByCreatedAtDesc(User user, Problem problem);
}
