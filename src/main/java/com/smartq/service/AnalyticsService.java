package com.smartq.service;

import com.smartq.dto.response.AnalyticsResponse;
import com.smartq.entity.Business;
import com.smartq.entity.QueueSession;
import com.smartq.entity.User;
import com.smartq.enums.TokenStatus;
import com.smartq.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final QueueSessionRepository queueSessionRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    public AnalyticsResponse getAnalytics(Long businessId) {
        User owner = getCurrentUser();

        Business business = businessRepository
                .findByIdAndOwnerId(businessId, owner.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Business not found or unauthorized"));

        // Today's session
        QueueSession todaySession = queueSessionRepository
                .findByBusinessIdAndDate(businessId, LocalDate.now())
                .orElse(null);

        int totalServedToday = todaySession != null
                ? todaySession.getTotalServed() : 0;
        int totalAbandonedToday = todaySession != null
                ? todaySession.getTotalAbandoned() : 0;
        double avgWaitToday = todaySession != null
                ? todaySession.getAvgWaitMins() : 0;
        int peakHourToday = todaySession != null
                && todaySession.getPeakHour() != null
                ? todaySession.getPeakHour() : 0;
        int maxQueueToday = todaySession != null
                ? todaySession.getMaxQueueReached() : 0;
        double avgPartySizeToday = todaySession != null
                ? todaySession.getAvgPartySize() : 0;
        double revenueToday = todaySession != null
                ? todaySession.getRevenueEstimate() : 0;

        // Currently waiting count
        int totalWaitingNow = tokenRepository
                .countTodaysTokensByStatus(
                        businessId, TokenStatus.WAITING);

        // Last 7 days
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<AnalyticsResponse.DailyStats> last7Days =
                queueSessionRepository.findRecentSessions(businessId)
                        .stream()
                        .limit(7)
                        .map(session -> AnalyticsResponse.DailyStats.builder()
                                .date(session.getDate().format(formatter))
                                .totalServed(session.getTotalServed())
                                .totalAbandoned(session.getTotalAbandoned())
                                .avgWaitMins(session.getAvgWaitMins())
                                .build())
                        .collect(Collectors.toList());

        return AnalyticsResponse.builder()
                .businessName(business.getName())
                .totalServedToday(totalServedToday)
                .totalAbandonedToday(totalAbandonedToday)
                .totalWaitingNow(totalWaitingNow)
                .avgWaitMinsToday(avgWaitToday)
                .peakHourToday(peakHourToday)
                .maxQueueReachedToday(maxQueueToday)
                .avgPartySizeToday(avgPartySizeToday)
                .revenueEstimateToday(revenueToday)
                .avgRating(business.getAvgRating())
                .totalReviews(business.getTotalReviews())
                .last7Days(last7Days)
                .build();
    }
}