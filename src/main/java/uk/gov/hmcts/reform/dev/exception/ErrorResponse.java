package uk.gov.hmcts.reform.dev.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(int status, String message, List<String> errors, Instant timestamp) {
}
