package com.uruz.frostbite.validator.provider;


import com.uruz.frostbite.dto.UserSelfQueryCondition;
import com.uruz.frostbite.validator.annotation.UserReadConditionRequireOne;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserReadConditionRequireOneProvider implements ConstraintValidator<UserReadConditionRequireOne, UserSelfQueryCondition> {

    @Override
    public boolean isValid(UserSelfQueryCondition value, ConstraintValidatorContext context) {
        if(value == null) return false;

        return !StringUtils.isEmpty(value.getName()) || !StringUtils.isEmpty(value.getEmail()) || !StringUtils.isEmpty(value.getUuid());

    }
}
