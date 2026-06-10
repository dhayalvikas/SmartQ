package com.smartq.dto.request;

import com.smartq.enums.CounterType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TokenRequest {

    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotNull(message = "Counter ID is required")
    private Long counterId;

    @Min(value = 1, message = "Party size must be at least 1")
    @Max(value = 20, message = "Party size cannot exceed 20")
    private Integer partySize = 1;

    private String specialRequest;
}