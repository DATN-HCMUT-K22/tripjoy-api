package com.tripjoy.api.validator;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {PasswordValidator.class})
public @interface PasswordConstraint {
    String message() default "Invalid password.";

    boolean detailedMessage() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
