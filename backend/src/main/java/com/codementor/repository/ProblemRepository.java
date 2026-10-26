package com.codementor.repository;
import com.codementor.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProblemRepository extends JpaRepository<Problem, Long> { }
