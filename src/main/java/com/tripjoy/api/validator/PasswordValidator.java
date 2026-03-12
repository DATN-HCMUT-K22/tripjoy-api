package com.tripjoy.api.validator;

import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.passay.*;

public class PasswordValidator implements ConstraintValidator<PasswordConstraint, String> {
    private static final int MIN_LENGTH = 6;
    private static final int MAX_LENGTH = 32;

    private boolean detailedMessage;

    @Override
    public void initialize(PasswordConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        detailedMessage = constraintAnnotation.detailedMessage();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return true;
        }

        org.passay.PasswordValidator validator = new org.passay.PasswordValidator(java.util.Arrays.asList(
                // Length rule. Min 6 max 32 characters
                new LengthRule(MIN_LENGTH, MAX_LENGTH),
                // At least one upper case letter
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                // At least one lower case letter
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                // At least one number
                new CharacterRule(EnglishCharacterData.Digit, 1),
                // At least one special characters
                new CharacterRule(EnglishCharacterData.Special, 1),
                // No whitespace
                new WhitespaceRule()));

        final RuleResult result = validator.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        }

        if (detailedMessage) {
            List<String> messages = validator.getMessages(result);
            messages.forEach(
                    msg -> context.buildConstraintViolationWithTemplate(msg).addConstraintViolation());
            context.disableDefaultConstraintViolation();
        }

        return false;
    }
}
