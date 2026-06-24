package com.smartq.service;

import com.smartq.dto.request.BusinessRequest;
import com.smartq.dto.response.BusinessResponse;
import com.smartq.entity.Business;
import com.smartq.entity.QueueSession;
import com.smartq.entity.User;
import com.smartq.repository.BusinessRepository;
import com.smartq.repository.CounterRepository;
import com.smartq.repository.QueueSessionRepository;
import com.smartq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final QueueSessionRepository queueSessionRepository;
    private final QrCodeService qrCodeService;
    private final CounterRepository counterRepository;  // FIX #2

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    // Get current logged in user
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // Create business
    public BusinessResponse createBusiness(BusinessRequest request) {
        User owner = getCurrentUser();

        if (businessRepository.existsByNameAndOwnerId(
                request.getName(), owner.getId())) {
            throw new RuntimeException(
                    "Business with this name already exists");
        }

        Business business = Business.builder()
                .owner(owner)
                .name(request.getName())
                .businessType(request.getBusinessType())
                .address(request.getAddress())
                .city(request.getCity())
                .maxQueueSize(request.getMaxQueueSize())
                .avgServiceMins(request.getAvgServiceMins())
                .isQueueOpen(false)
                .avgRating(0.0)
                .totalReviews(0)
                .calledMessage(request.getCalledMessage() != null
                        ? request.getCalledMessage()
                        : "Your turn has arrived. Please proceed.")
                .build();

        businessRepository.save(business);

        // Generate QR code using production base URL
        String qrContent = baseUrl + "/join.html?businessId="
                + business.getId();
        String qrBase64 = qrCodeService
                .generateQrCodeBase64(qrContent);
        business.setQrCodeUrl(qrBase64);
        businessRepository.save(business);

        return mapToResponse(business);
    }

    // Get my businesses
    public List<BusinessResponse> getMyBusinesses() {
        User owner = getCurrentUser();
        return businessRepository.findByOwnerId(owner.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Open queue for today
    public BusinessResponse openQueue(Long businessId) {
        User owner = getCurrentUser();
        Business business = businessRepository
                .findByIdAndOwnerId(businessId, owner.getId())
                .orElseThrow(() ->
                        new RuntimeException("Business not found"));

        if (business.getIsQueueOpen()) {
            throw new RuntimeException("Queue is already open");
        }

        // FIX #2: Reset counter daily stats when opening queue
        // so "Served Today" and "Current Token" start fresh each day
        counterRepository.findByBusinessId(businessId).forEach(c -> {
            c.setTokensServedToday(0);
            c.setCurrentToken(0);
            counterRepository.save(c);
        });

        business.setIsQueueOpen(true);
        businessRepository.save(business);

        // Only create session if one doesn't exist for today
        boolean sessionExists = queueSessionRepository
                .findByBusinessIdAndDate(businessId, LocalDate.now())
                .isPresent();

        if (!sessionExists) {
            QueueSession session = QueueSession.builder()
                    .business(business)
                    .date(LocalDate.now())
                    .openedAt(LocalDateTime.now())
                    .totalServed(0)
                    .totalAbandoned(0)
                    .avgWaitMins(0)
                    .maxQueueReached(0)
                    .avgPartySize(0.0)
                    .revenueEstimate(0.0)
                    .build();

            queueSessionRepository.save(session);
        }

        return mapToResponse(business);
    }

    // Close queue
    public BusinessResponse closeQueue(Long businessId) {
        User owner = getCurrentUser();
        Business business = businessRepository
                .findByIdAndOwnerId(businessId, owner.getId())
                .orElseThrow(() ->
                        new RuntimeException("Business not found"));

        if (!business.getIsQueueOpen()) {
            throw new RuntimeException("Queue is already closed");
        }

        business.setIsQueueOpen(false);
        businessRepository.save(business);

        // Update session closed time
        queueSessionRepository
                .findByBusinessIdAndDate(
                        businessId, LocalDate.now())
                .ifPresent(session -> {
                    session.setClosedAt(LocalDateTime.now());
                    queueSessionRepository.save(session);
                });

        return mapToResponse(business);
    }

    // Regenerate QR code
    public BusinessResponse regenerateQrCode(Long businessId) {
        User owner = getCurrentUser();
        Business business = businessRepository
                .findByIdAndOwnerId(businessId, owner.getId())
                .orElseThrow(() ->
                        new RuntimeException("Business not found"));

        String qrContent = baseUrl + "/join.html?businessId="
                + business.getId();
        String qrBase64 = qrCodeService
                .generateQrCodeBase64(qrContent);
        business.setQrCodeUrl(qrBase64);
        businessRepository.save(business);

        return mapToResponse(business);
    }

    // Map entity to response
    private BusinessResponse mapToResponse(Business business) {
        return BusinessResponse.builder()
                .id(business.getId())
                .name(business.getName())
                .businessType(business.getBusinessType())
                .address(business.getAddress())
                .city(business.getCity())
                .isQueueOpen(business.getIsQueueOpen())
                .maxQueueSize(business.getMaxQueueSize())
                .avgServiceMins(business.getAvgServiceMins())
                .avgRating(business.getAvgRating())
                .totalReviews(business.getTotalReviews())
                .qrCodeUrl(business.getQrCodeUrl())
                .calledMessage(business.getCalledMessage())
                .ownerName(business.getOwner().getName())
                .build();
    }
}