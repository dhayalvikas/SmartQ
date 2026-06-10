package com.smartq.dto.response;

import com.smartq.enums.TokenStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenStatusResponse {
    private Long tokenId;
    private Integer tokenNumber;
    private TokenStatus status;
    private Integer positionInQueue;
    private Integer estimatedWaitMins;
    private String businessName;
    private String counterName;
    private String calledMessage;
    private Integer currentlyServing;
    private Integer totalWaiting;
}