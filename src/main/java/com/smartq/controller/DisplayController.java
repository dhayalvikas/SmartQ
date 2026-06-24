package com.smartq.controller;

import com.smartq.dto.response.DisplayResponse;
import com.smartq.entity.Business;
import com.smartq.entity.Counter;
import com.smartq.enums.TokenStatus;
import com.smartq.repository.BusinessRepository;
import com.smartq.repository.CounterRepository;
import com.smartq.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class DisplayController {

    private final BusinessRepository businessRepository;
    private final CounterRepository counterRepository;
    private final TokenRepository tokenRepository;

    // TV Display API — public, no login needed
    @GetMapping("/api/display/{businessId}")
    public ResponseEntity<DisplayResponse> getDisplay(
            @PathVariable Long businessId) {

        Business business = businessRepository
                .findById(businessId)
                .orElseThrow(() ->
                        new RuntimeException("Business not found"));

        // FIX #11: Only show active counters on TV display
        // Deactivated counters should not appear on the screen
        List<Counter> counters = counterRepository
                .findByBusinessIdAndIsActive(businessId, true);

        List<DisplayResponse.CounterDisplayInfo> counterInfos =
                counters.stream().map(counter -> {
                    int waiting = tokenRepository
                            .countByCounterIdAndStatus(
                                    counter.getId(),
                                    TokenStatus.WAITING);

                    return DisplayResponse.CounterDisplayInfo.builder()
                            .counterId(counter.getId())
                            .counterName(counter.getCounterName())
                            .counterType(counter.getCounterType().name())
                            .currentToken(counter.getCurrentToken())
                            .totalWaiting(waiting)
                            .isActive(counter.getIsActive())
                            .build();
                }).collect(Collectors.toList());

        DisplayResponse response = DisplayResponse.builder()
                .businessId(business.getId())
                .businessName(business.getName())
                .businessType(business.getBusinessType().name())
                .city(business.getCity())
                .isQueueOpen(business.getIsQueueOpen())
                .counters(counterInfos)
                .build();

        return ResponseEntity.ok(response);
    }

    // Customer join page — public, no login needed
    @GetMapping("/join/{businessId}")
    public ResponseEntity<DisplayResponse> joinPage(
            @PathVariable Long businessId) {
        return getDisplay(businessId);
    }
}
