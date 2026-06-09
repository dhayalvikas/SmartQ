package com.smartq.controller;

import com.smartq.dto.request.CounterRequest;
import com.smartq.dto.response.CounterResponse;
import com.smartq.service.CounterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/counter")
@RequiredArgsConstructor
public class CounterController {

    private final CounterService counterService;

    @PostMapping("/create")
    public ResponseEntity<CounterResponse> createCounter(
            @Valid @RequestBody CounterRequest request) {
        return ResponseEntity.ok(
                counterService.createCounter(request));
    }

    @GetMapping("/list/{businessId}")
    public ResponseEntity<List<CounterResponse>> getCounters(
            @PathVariable Long businessId) {
        return ResponseEntity.ok(
                counterService.getCounters(businessId));
    }

    @GetMapping("/active/{businessId}")
    public ResponseEntity<List<CounterResponse>> getActiveCounters(
            @PathVariable Long businessId) {
        return ResponseEntity.ok(
                counterService.getActiveCounters(businessId));
    }

    @PutMapping("/activate/{counterId}")
    public ResponseEntity<CounterResponse> activateCounter(
            @PathVariable Long counterId) {
        return ResponseEntity.ok(
                counterService.activateCounter(counterId));
    }

    @PutMapping("/deactivate/{counterId}")
    public ResponseEntity<CounterResponse> deactivateCounter(
            @PathVariable Long counterId) {
        return ResponseEntity.ok(
                counterService.deactivateCounter(counterId));
    }
}