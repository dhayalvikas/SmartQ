package com.smartq.dto.response;

import com.smartq.enums.TokenStatus;
import lombok.Builder;
import lombok.Data;

// Lightweight view of a single token in a counter's live queue,
// shown to the owner so they can see who is waiting / currently
// called, with customer name, party size, and special request.
@Data
@Builder
public class QueueTokenResponse {
    private Long tokenId;
    private Integer tokenNumber;
    private TokenStatus status;
    private String customerName;
    private String customerPhone;
    private Integer partySize;
    private String specialRequest;
    private Integer positionInQueue;
}