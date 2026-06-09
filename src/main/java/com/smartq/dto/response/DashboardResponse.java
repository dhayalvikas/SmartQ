package com.smartq.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private String businessName;
    private Boolean isQueueOpen;
    private Integer totalWaiting;
    private Integer totalServedToday;
    private Integer totalAbandonedToday;
    private Double avgWaitMins;
    private List<CounterResponse> counters;
}