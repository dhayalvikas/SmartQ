package com.smartq.dto.response;

import com.smartq.enums.CounterType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CounterResponse {
    private Long id;
    private String counterName;
    private CounterType counterType;
    private Boolean isActive;
    private Integer currentToken;
    private String staffName;
    private Integer tokensServedToday;
    private Long businessId;
    private String businessName;
}