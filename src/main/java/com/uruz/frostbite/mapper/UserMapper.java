package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.user.UserSelfQueryResponse;
import com.uruz.frostbite.enums.user.UserStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Mapper
public interface UserMapper {
    public int insertUser(User user);
    // 추후 기본 CRUD , Count들은 모두 공통 mapper로 올리는 방안도 고민

    public Optional<UserSelfQueryResponse> selectSelfUser(Integer userId);

    public int softDeleteSelfUser(@Param("userId")Integer userId, @Param("status")UserStatus status);

    public Optional<User> selectUserByUuid(UUID uuid);
    public Optional<User> selectUserByUserId(Integer userId);

    public int updateUserPassword(@Param("userId") Integer userId, @Param("newPassword")String newPassword);
    // FE로 가입 시 오류 사유를 나누어서 전달하기 위해 사유 분리
    @Deprecated
    public boolean selectUserExistsByEmailAndPhoneNumber(@Param("email") String email, @Param("phoneNumber")String phoneNumber);
    public boolean selectUserExistsByEmail(@Param("email") String email);
    public boolean selectUserExistsByPhoneNumber(@Param("phoneNumber")String phoneNumber);

    public User selectUserByEmail(String email);
    public User selectUserByUserIdForPassword(Integer userId);

    /** Assertions */
    public boolean selectUserEmailExists(String email);
    public boolean selectUserPhoneNumberExists(String phoneNumber);
}
