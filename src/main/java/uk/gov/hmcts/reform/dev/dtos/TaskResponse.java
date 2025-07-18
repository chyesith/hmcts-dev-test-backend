package uk.gov.hmcts.reform.dev.dtos;

import java.time.LocalDateTime;


import uk.gov.hmcts.reform.dev.models.Status;


public record TaskResponse(
    Long id,
    String title,
    String description,
    Status status,
    LocalDateTime dueDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
