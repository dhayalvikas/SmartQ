package com.smartq.dto.request;

import com.smartq.enums.BusinessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BusinessRequest {

    @NotBlank(message = "Business name is required")
    private String name;

    @NotNull(message = "Business type is required")
    private BusinessType businessType;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private Integer maxQueueSize = 50;
    private Integer avgServiceMins = 10;
    private String calledMessage;
}