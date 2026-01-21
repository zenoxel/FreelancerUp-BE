package com.FreelancerUp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator for password strength annotation.
 */
public class PasswordValidator implements ConstraintValidator<Password, String> {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");

    private int minLength;

    @Override
    public void initialize(Password constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        if (value.length() < minLength) {
            return false;
        }

        if (!UPPERCASE.matcher(value).find()) {
            return false;
        }

        if (!LOWERCASE.matcher(value).find()) {
            return false;
        }

        if (!DIGIT.matcher(value).find()) {
            return false;
        }

        if (!SPECIAL.matcher(value).find()) {
            return false;
        }

        return true;
    }
}
