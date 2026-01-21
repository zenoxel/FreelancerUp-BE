package com.FreelancerUp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator for MongoDB ObjectId annotation.
 */
public class ObjectIdValidator implements ConstraintValidator<ObjectId, String> {

    // MongoDB ObjectId is a 24-character hexadecimal string
    private static final Pattern OBJECT_ID_PATTERN = Pattern.compile("^[a-fA-F0-9]{24}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // Use @NotNull for null checks
        }

        return OBJECT_ID_PATTERN.matcher(value).matches();
    }
}
