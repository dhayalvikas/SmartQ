package com.smartq.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalyticsResponse {
    private String businessName;
    private Integer totalServedToday;
    private Integer totalAbandonedToday;
    private Integer totalWaitingNow;
    private Double avgWaitMinsToday;
    private Integer peakHourToday;
    private Integer maxQueueReachedToday;
    private Double avgPartySizeToday;
    private Double revenueEstimateToday;
    private Double avgRating;
    private Integer totalReviews;
    private List<DailyStats> last7Days;

    @Data
    @Builder
    public static class DailyStats {
        private String date;
        private Integer totalServed;
        private Integer totalAbandoned;
        private Integer avgWaitMins;
    }
}