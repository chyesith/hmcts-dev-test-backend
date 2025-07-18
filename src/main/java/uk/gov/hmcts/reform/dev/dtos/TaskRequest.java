package uk.gov.hmcts.reform.dev.dtos;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;


import uk.gov.hmcts.reform.dev.models.Status;
import uk.gov.hmcts.reform.dev.validators.ValidateRequest;

@ValidateRequest
public record TaskRequest(
    @NotBlank(message = "Title is a mandatory field")
    String title,
    String description,
    Status status,
    LocalDateTime dueDate
) {
}
