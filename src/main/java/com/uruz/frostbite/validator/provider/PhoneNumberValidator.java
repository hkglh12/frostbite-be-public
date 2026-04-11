package com.uruz.frostbite.validator.provider;

import com.uruz.frostbite.validator.annotation.PhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null){
            return false;
        }
        return value.matches("(01[016789])(\\d{3,4})(\\d{4})");
    }
}
