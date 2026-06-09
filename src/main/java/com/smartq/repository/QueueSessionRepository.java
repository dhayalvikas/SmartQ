package com.smartq.repository;

import com.smartq.entity.QueueSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueueSessionRepository
        extends JpaRepository<QueueSession, Long> {

    Optional<QueueSession> findByBusinessIdAndDate(
            Long businessId, LocalDate date);
    List<QueueSession> findByBusinessIdOrderByDateDesc(
            Long businessId);
}