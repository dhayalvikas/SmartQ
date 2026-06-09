package com.smartq.repository;

import com.smartq.entity.Counter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CounterRepository
        extends JpaRepository<Counter, Long> {

    List<Counter> findByBusinessId(Long businessId);
    List<Counter> findByBusinessIdAndIsActive(
            Long businessId, Boolean isActive);
    int countByBusinessId(Long businessId);
}