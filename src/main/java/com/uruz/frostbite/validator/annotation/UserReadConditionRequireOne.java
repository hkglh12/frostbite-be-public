package com.uruz.frostbite.validator.annotation;

import com.uruz.frostbite.validator.provider.UserReadConditionRequireOneProvider;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Deprecated(forRemoval = true)
//@Constraint(validatedBy = UserReadConditionRequireOneProvider.class)
public @interface UserReadConditionRequireOne {
    String message() default "UUID or username must be provided";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
