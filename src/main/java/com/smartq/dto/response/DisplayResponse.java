package com.smartq.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DisplayResponse {
    private Long businessId;
    private String businessName;
    private String businessType;
    private String city;
    private Boolean isQueueOpen;
    private List<CounterDisplayInfo> counters;

    @Data
    @Builder
    public static class CounterDisplayInfo {
        private Long counterId;
        private String counterName;
        private String counterType;
        private Integer currentToken;
        private Integer totalWaiting;
        private Boolean isActive;
    }
}