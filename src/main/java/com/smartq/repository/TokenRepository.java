package com.smartq.repository;

import com.smartq.entity.Token;
import com.smartq.enums.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    // Count waiting tokens in a counter
    int countByCounterIdAndStatus(Long counterId, TokenStatus status);

    // Get all waiting tokens for a counter ordered by token number
    List<Token> findByCounterIdAndStatusOrderByTokenNumber(
            Long counterId, TokenStatus status);

    // Get last token number for a counter today
    @Query("SELECT MAX(t.tokenNumber) FROM Token t " +
            "WHERE t.counter.id = :counterId " +
            "AND DATE(t.issuedAt) = CURRENT_DATE")
    Optional<Integer> findMaxTokenNumberForToday(Long counterId);

    // Get customer's active token
    Optional<Token> findByCustomerIdAndStatusIn(
            Long customerId, List<TokenStatus> statuses);

    // Get all tokens for a business today
    @Query("SELECT t FROM Token t " +
            "WHERE t.business.id = :businessId " +
            "AND DATE(t.issuedAt) = CURRENT_DATE")
    List<Token> findTodaysTokensByBusiness(Long businessId);

    // Count tokens by status for a business today
    @Query("SELECT COUNT(t) FROM Token t " +
            "WHERE t.business.id = :businessId " +
            "AND t.status = :status " +
            "AND DATE(t.issuedAt) = CURRENT_DATE")
    int countTodaysTokensByStatus(Long businessId, TokenStatus status);
}