package com.uruz.frostbite.domain;

import com.uruz.frostbite.enums.user.AccountType;
import com.uruz.frostbite.enums.user.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
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

    /** 계정 종류 */
    private AccountType accountType;

    /** 삭제여부 */
    private UserStatus status;

    /*
    public User(String email, String password, AccountType accountType, UserStatus status, UUID uuid){

    }

     */

    private User(String name, String email, String password, String phoneNumber, LocalDate birth, LocalDate createDate, UUID uuid, AccountType accountType, UserStatus status){
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.birth = birth;
        this.createDate = createDate;
        this.uuid = uuid;
        this.status = status;
        this.accountType = accountType;
    }
    public static User create(String name, String email, String password, String phoneNumber, LocalDate birth, LocalDate createDate, UUID uuid, AccountType accountType){
        if(name.length() < 2 || name.length() > 20){
            throw new IllegalArgumentException("Name constraint violated");
        }
        if(!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Email constraint violdated");
        }
        if(password == null){
            throw new IllegalArgumentException("Password not passed");
        }
        if(!phoneNumber.matches("(01[016789])(\\d{3,4})(\\d{4})")) {
            throw new IllegalArgumentException("Phone number malformed");
        }

        return new User(
            name, email, password, phoneNumber, birth,createDate, uuid, accountType, UserStatus.ACTIVE
        );
    }
}
