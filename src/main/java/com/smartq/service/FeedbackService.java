package com.smartq.service;

import com.smartq.dto.request.FeedbackRequest;
import com.smartq.dto.response.FeedbackResponse;
import com.smartq.entity.*;
import com.smartq.enums.TokenStatus;
import com.smartq.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final TokenRepository tokenRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    @Transactional
    public FeedbackResponse submitFeedback(FeedbackRequest request) {
        User customer = getCurrentUser();

        Token token = tokenRepository.findById(request.getTokenId())
                .orElseThrow(() ->
                        new RuntimeException("Token not found"));

        // Verify token belongs to customer
        if (!token.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Verify token is completed
        if (token.getStatus() != TokenStatus.DONE) {
            throw new RuntimeException(
                    "Feedback can only be submitted for completed visits");
        }

        // Prevent duplicate feedback
        if (feedbackRepository.existsByTokenId(token.getId())) {
            throw new RuntimeException(
                    "Feedback already submitted for this visit");
        }

        Business business = token.getBusiness();

        Feedback feedback = Feedback.builder()
                .token(token)
                .business(business)
                .customer(customer)
                .waitRating(request.getWaitRating())
                .serviceRating(request.getServiceRating())
                .overallRating(request.getOverallRating())
                .comment(request.getComment())
                .wouldReturn(request.getWouldReturn())
                .build();

        feedbackRepository.save(feedback);

        // Update business average rating
        Double avgRating = feedbackRepository
                .findAvgRatingByBusiness(business.getId());

        business.setAvgRating(
                Math.round(avgRating * 100) / 100.0);
        business.setTotalReviews(
                business.getTotalReviews() + 1);
        businessRepository.save(business);

        return mapToResponse(feedback);
    }

    public List<FeedbackResponse> getBusinessFeedback(Long businessId) {
        return feedbackRepository
                .findByBusinessIdOrderByCreatedAtDesc(businessId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private FeedbackResponse mapToResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .tokenNumber(feedback.getToken().getTokenNumber())
                .waitRating(feedback.getWaitRating())
                .serviceRating(feedback.getServiceRating())
                .overallRating(feedback.getOverallRating())
                .comment(feedback.getComment())
                .wouldReturn(feedback.getWouldReturn())
                .customerName(feedback.getCustomer().getName())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}