package com.smartq.controller;

import com.smartq.dto.request.FeedbackRequest;
import com.smartq.dto.response.FeedbackResponse;
import com.smartq.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/submit")
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @Valid @RequestBody FeedbackRequest request) {
        return ResponseEntity.ok(
                feedbackService.submitFeedback(request));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<FeedbackResponse>> getBusinessFeedback(
            @PathVariable Long businessId) {
        return ResponseEntity.ok(
                feedbackService.getBusinessFeedback(businessId));
    }
}