package uk.gov.hmcts.reform.dev.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TaskValidator.class)
@Documented
public @interface ValidateRequest {
    String message() default "Invalid task request: due date must be today or future unless status is COMPLETED";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default {};
}
