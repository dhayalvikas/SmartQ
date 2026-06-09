package com.smartq.controller;

import com.smartq.dto.request.BusinessRequest;
import com.smartq.dto.response.BusinessResponse;
import com.smartq.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping("/create")
    public ResponseEntity<BusinessResponse> createBusiness(
            @Valid @RequestBody BusinessRequest request) {
        return ResponseEntity.ok(
                businessService.createBusiness(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BusinessResponse>> getMyBusinesses() {
        return ResponseEntity.ok(
                businessService.getMyBusinesses());
    }

    @PutMapping("/queue/open/{businessId}")
    public ResponseEntity<BusinessResponse> openQueue(
            @PathVariable Long businessId) {
        return ResponseEntity.ok(
                businessService.openQueue(businessId));
    }

    @PutMapping("/queue/close/{businessId}")
    public ResponseEntity<BusinessResponse> closeQueue(
            @PathVariable Long businessId) {
        return ResponseEntity.ok(
                businessService.closeQueue(businessId));
    }
}