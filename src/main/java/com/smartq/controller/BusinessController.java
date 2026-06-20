package com.smartq.controller;

import com.smartq.dto.request.BusinessRequest;
import com.smartq.dto.response.BusinessResponse;
import com.smartq.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.smartq.entity.Business;
import com.smartq.repository.BusinessRepository;
import com.smartq.service.QrCodeService;
import java.util.HashMap;
import java.util.Map;
import com.smartq.dto.response.AnalyticsResponse;
import com.smartq.service.AnalyticsService;

import java.util.List;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;
    private final BusinessRepository businessRepository;
    private final QrCodeService qrCodeService;
    private final AnalyticsService analyticsService;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

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

    // Get QR code as Base64 (for showing in browser)
    @GetMapping("/qr/{businessId}")
    public ResponseEntity<Map<String, String>> getQrCode(
            @PathVariable Long businessId) {
        return businessRepository.findById(businessId)
                .map(business -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("qrCode", business.getQrCodeUrl());
                    response.put("businessName", business.getName());
                    response.put("scanUrl", baseUrl + "/join.html?businessId="
                            + businessId);
                    return ResponseEntity.ok(response);
                })
                .orElseThrow(() ->
                        new RuntimeException("Business not found"));
    }

    // Download QR code as PNG image
    @GetMapping("/qr/download/{businessId}")
    public ResponseEntity<byte[]> downloadQrCode(
            @PathVariable Long businessId) {
        Business business = businessRepository
                .findById(businessId)
                .orElseThrow(() ->
                        new RuntimeException("Business not found"));

        String qrContent = baseUrl + "/join.html?businessId="
                + businessId;
        byte[] qrBytes = qrCodeService
                .generateQrCodeBytes(qrContent);

        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .header("Content-Disposition",
                        "attachment; filename=smartq-" +
                                businessId + ".png")
                .body(qrBytes);
    }

    @GetMapping("/analytics/{businessId}")
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @PathVariable Long businessId) {
        return ResponseEntity.ok(
                analyticsService.getAnalytics(businessId));
    }

    @PutMapping("/regenerate-qr/{businessId}")
    public ResponseEntity<BusinessResponse> regenerateQr(@PathVariable Long businessId) {
        return ResponseEntity.ok(businessService.regenerateQrCode(businessId));
    }
}