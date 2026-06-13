package com.smartq.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackRequest {

    @NotNull(message = "Token ID is required")
    private Long tokenId;

    @Min(1) @Max(5)
    @NotNull(message = "Wait rating is required")
    private Integer waitRating;

    @Min(1) @Max(5)
    @NotNull(message = "Service rating is required")
    private Integer serviceRating;

    @Min(1) @Max(5)
    @NotNull(message = "Overall rating is required")
    private Integer overallRating;

    private String comment;

    @NotNull(message = "Would return is required")
    private Boolean wouldReturn;
}