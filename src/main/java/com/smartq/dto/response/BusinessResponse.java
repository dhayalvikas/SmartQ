package com.smartq.dto.response;

import com.smartq.enums.BusinessType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusinessResponse {
    private Long id;
    private String name;
    private BusinessType businessType;
    private String address;
    private String city;
    private Boolean isQueueOpen;
    private Integer maxQueueSize;
    private Integer avgServiceMins;
    private Double avgRating;
    private Integer totalReviews;
    private String qrCodeUrl;
    private String calledMessage;
    private String ownerName;
}