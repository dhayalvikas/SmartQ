package com.smartq.repository;

import com.smartq.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository
        extends JpaRepository<Feedback, Long> {

    List<Feedback> findByBusinessIdOrderByCreatedAtDesc(
            Long businessId);

    boolean existsByTokenId(Long tokenId);

    @Query("SELECT AVG(f.overallRating) FROM Feedback f " +
            "WHERE f.business.id = :businessId")
    Double findAvgRatingByBusiness(Long businessId);
}