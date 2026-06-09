package com.smartq.dto.request;

import com.smartq.enums.CounterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CounterRequest {

    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotBlank(message = "Counter name is required")
    private String counterName;

    @NotNull(message = "Counter type is required")
    private CounterType counterType;

    private String staffName;
}