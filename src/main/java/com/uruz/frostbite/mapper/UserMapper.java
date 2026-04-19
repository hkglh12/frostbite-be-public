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

    public Optional<UserSelfQueryResponse> selectSelfUser(String uuid);

    public int softDeleteUser(UUID uuid, UserStatus status);

    public Optional<User> selectUser(UUID uuid);

    public boolean selectUserExistsByNameAndPhoneNumber(@Param("email") String email, @Param("phoneNumber")String phoneNumber);

    public User selectUserByEmail(String email);
}
