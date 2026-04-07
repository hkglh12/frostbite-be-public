package com.uruz.frostbite.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Role {

    /** 역할 식별자 */
    private Integer roleId;

    /** 역할명 */
    private String roleName;
}
