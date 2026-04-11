package com.uruz.frostbite.validator.annotation;

import com.uruz.frostbite.validator.provider.PhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = PhoneNumberValidator.class)
public @interface PhoneNumber {
    String message() default "Invalid phone number format";
    // 규약, 경우에 따른 검증 여부 제공 가능. Interface Class 추가 후 @Validate(Interface.Class)로 마킹
    Class<?>[] groups() default {};
    // 규약, 검증실패시의 추가정보
    Class<? extends Payload>[] payload() default{};


}
