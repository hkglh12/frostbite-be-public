package com.uruz.frostbite.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class User {

    /** 사용자 식별자 */
    private Integer userId;

    /** 사용자이름 */
    private String name;

    /** 이메일 */
    private String email;

    /** 비밀번호 */
    private String password;

    /** 휴대전화번호 */
    private String phoneNumber;

    /** 생년월일 */
    private LocalDate birth;

    /** 가입날짜 */
    private LocalDate createDate;

    /** 사용자 UUID */
    private UUID uuid;

    /** 삭제여부 */
    private Short isDelete;
}
