package com.smartq.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlatformStatsResponse {
    private Long totalBusinesses;
    private Long totalUsers;
    private Long totalCustomers;
    private Long totalOwners;
    private Long totalTokensIssued;
    private Long totalTokensServedAllTime;
    private Long restaurantCount;
    private Long hospitalCount;
    private Long bankCount;
    private Long salonCount;
    private Long governmentCount;
    private Long otherCount;
}