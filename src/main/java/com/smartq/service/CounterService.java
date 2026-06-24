package com.smartq.service;

import com.smartq.dto.request.CounterRequest;
import com.smartq.dto.response.CounterResponse;
import com.smartq.entity.Business;
import com.smartq.entity.Counter;
import com.smartq.entity.User;
import com.smartq.enums.TokenStatus;
import com.smartq.repository.BusinessRepository;
import com.smartq.repository.CounterRepository;
import com.smartq.repository.TokenRepository;
import com.smartq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounterService {

    private final CounterRepository counterRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository; // FIX #5

    // Get current logged in user
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // Create counter
    public CounterResponse createCounter(CounterRequest request) {
        User owner = getCurrentUser();

        Business business = businessRepository
                .findByIdAndOwnerId(
                        request.getBusinessId(), owner.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Business not found or unauthorized"));

        // Check for duplicate counter name in this business
        boolean nameExists = counterRepository
                .findByBusinessId(request.getBusinessId())
                .stream()
                .anyMatch(c -> c.getCounterName()
                        .equalsIgnoreCase(request.getCounterName()));

        if (nameExists) {
            throw new RuntimeException(
                    "A counter named '"
                            + request.getCounterName()
                            + "' already exists");
        }

        Counter counter = Counter.builder()
                .business(business)
                .counterName(request.getCounterName())
                .counterType(request.getCounterType())
                .staffName(request.getStaffName())
                .isActive(true)
                .currentToken(0)
                .tokensServedToday(0)
                .build();

        counterRepository.save(counter);
        return mapToResponse(counter);
    }

    // Get all counters for a business
    public List<CounterResponse> getCounters(Long businessId) {
        return counterRepository.findByBusinessId(businessId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get active counters only
    public List<CounterResponse> getActiveCounters(Long businessId) {
        return counterRepository
                .findByBusinessIdAndIsActive(businessId, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Activate counter
    public CounterResponse activateCounter(Long counterId) {
        Counter counter = getCounterForOwner(counterId);
        counter.setIsActive(true);
        counterRepository.save(counter);
        return mapToResponse(counter);
    }

    // Deactivate counter
    public CounterResponse deactivateCounter(Long counterId) {
        Counter counter = getCounterForOwner(counterId);
        counter.setIsActive(false);
        counterRepository.save(counter);
        return mapToResponse(counter);
    }

    // FIX #5: Block delete if counter has active tokens
    // Prevents FK constraint crash in database
    public void deleteCounter(Long counterId) {
        User owner = getCurrentUser();
        Counter counter = counterRepository.findById(counterId)
                .orElseThrow(() ->
                        new RuntimeException("Counter not found"));

        if (!counter.getBusiness().getOwner()
                .getId().equals(owner.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Block deletion if active customers are in this counter
        int activeTokens =
                tokenRepository.countByCounterIdAndStatus(
                        counterId, TokenStatus.WAITING)
                        + tokenRepository.countByCounterIdAndStatus(
                        counterId, TokenStatus.CALLED);

        if (activeTokens > 0) {
            throw new RuntimeException(
                    "Cannot delete counter with "
                            + activeTokens
                            + " active customer(s) in queue. "
                            + "Wait for queue to clear first.");
        }

        counterRepository.delete(counter);
    }

    // Helper — get counter and verify ownership
    private Counter getCounterForOwner(Long counterId) {
        User owner = getCurrentUser();
        Counter counter = counterRepository.findById(counterId)
                .orElseThrow(() ->
                        new RuntimeException("Counter not found"));

        if (!counter.getBusiness().getOwner()
                .getId().equals(owner.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        return counter;
    }

    // Map entity to response
    public CounterResponse mapToResponse(Counter counter) {
        return CounterResponse.builder()
                .id(counter.getId())
                .counterName(counter.getCounterName())
                .counterType(counter.getCounterType())
                .isActive(counter.getIsActive())
                .currentToken(counter.getCurrentToken())
                .staffName(counter.getStaffName())
                .tokensServedToday(counter.getTokensServedToday())
                .businessId(counter.getBusiness().getId())
                .businessName(counter.getBusiness().getName())
                .build();
    }
}
