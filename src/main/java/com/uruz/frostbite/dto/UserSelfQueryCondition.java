package com.uruz.frostbite.dto;

import com.uruz.frostbite.validator.annotation.UserReadConditionRequireOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@UserReadConditionRequireOne
public class UserSelfQueryCondition {
    private String uuid;
    private String email;
    private String name;
}
