package com.smartq.controller;

import com.smartq.dto.request.TokenRequest;
import com.smartq.dto.response.CounterResponse;
import com.smartq.dto.response.TokenStatusResponse;
import com.smartq.service.CounterService;
import com.smartq.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;
    private final CounterService counterService;

    // Customer sees available counters before getting token
    @GetMapping("/counters/{businessId}")
    public ResponseEntity<List<CounterResponse>> getCounters(
            @PathVariable Long businessId) {
        return ResponseEntity.ok(
                counterService.getActiveCounters(businessId));
    }

    // Customer generates token
    @PostMapping("/generate")
    public ResponseEntity<TokenStatusResponse> generateToken(
            @Valid @RequestBody TokenRequest request) {
        return ResponseEntity.ok(
                tokenService.generateToken(request));
    }

    // Customer checks live status
    @GetMapping("/status/{tokenId}")
    public ResponseEntity<TokenStatusResponse> getStatus(
            @PathVariable Long tokenId) {
        return ResponseEntity.ok(
                tokenService.getTokenStatus(tokenId));
    }

    // Customer leaves queue
    @PutMapping("/leave/{tokenId}")
    public ResponseEntity<TokenStatusResponse> leaveQueue(
            @PathVariable Long tokenId) {
        return ResponseEntity.ok(
                tokenService.leaveQueue(tokenId));
    }

    // Owner calls next token on a counter
    @PutMapping("/next/{counterId}")
    public ResponseEntity<TokenStatusResponse> callNext(
            @PathVariable Long counterId) {
        return ResponseEntity.ok(
                tokenService.callNextToken(counterId));
    }
}