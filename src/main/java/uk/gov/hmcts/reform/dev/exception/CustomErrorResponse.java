package uk.gov.hmcts.reform.dev.exception;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class CustomErrorResponse {
    private HttpStatus status;
    private String message;
    private List<String> errors;

    public CustomErrorResponse(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
