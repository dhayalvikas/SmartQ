package com.smartq.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackResponse {
    private Long id;
    private Integer tokenNumber;
    private Integer waitRating;
    private Integer serviceRating;
    private Integer overallRating;
    private String comment;
    private Boolean wouldReturn;
    private String customerName;
    private LocalDateTime createdAt;
}