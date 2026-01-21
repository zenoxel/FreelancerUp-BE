package com.FreelancerUp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for MongoDB ObjectId.
 *
 * Ensures that a string is a valid 24-character hexadecimal string.
 */
@Documented
@Constraint(validatedBy = ObjectIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectId {

    String message() default "Invalid MongoDB ObjectId format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
