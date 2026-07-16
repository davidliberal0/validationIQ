package com.example.validationiq.repository;

import com.example.validationiq.entity.Failure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailureRepository extends JpaRepository<Failure, Long> {
}
