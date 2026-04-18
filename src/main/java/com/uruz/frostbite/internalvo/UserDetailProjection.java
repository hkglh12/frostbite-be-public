package com.uruz.frostbite.internalvo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Deprecated(forRemoval = true)
public class UserDetailProjection {

    /** 사용자 UUID */
    private UUID uuid;

    /** 사용자이름 */
    private String name;

    /** 이메일 */
    private String email;

    /** 휴대전화번호 */
    private String phoneNumber;

    /** 생년월일 */
    private LocalDate birth;

    /** 가입날짜 */
    private LocalDate createDate;

}
