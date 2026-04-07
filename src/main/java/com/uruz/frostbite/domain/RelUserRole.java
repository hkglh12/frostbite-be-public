package com.uruz.frostbite.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RelUserRole {

    /** 관계 식별자 */
    private Integer relId;

    /** 사용자 식별자 */
    private Integer userId;

    /** 역할 식별자 */
    private Integer roleId;
}
