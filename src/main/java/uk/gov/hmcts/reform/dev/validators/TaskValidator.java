package uk.gov.hmcts.reform.dev.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.reform.dev.dtos.TaskRequest;
import uk.gov.hmcts.reform.dev.models.Status;

import java.time.LocalDateTime;


public class TaskValidator implements ConstraintValidator<ValidateRequest, TaskRequest> {

    @Override
    public void initialize(ValidateRequest constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(TaskRequest taskRequest, ConstraintValidatorContext context) {
        if (taskRequest == null) {
            return false;
        }

        if (taskRequest.status() == Status.COMPLETED) {
            return taskRequest.dueDate() != null;
        }

        if (taskRequest.dueDate() == null) {
            buildViolation(context, "Due date is mandatory for non-completed tasks");
            return false;
        }

        if (taskRequest.dueDate().isBefore(LocalDateTime.now())) {
            buildViolation(context, "Due date should be today or a future date");
            return false;
        }

        return true;
    }

    private void buildViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addPropertyNode("dueDate").addConstraintViolation();
    }
}
