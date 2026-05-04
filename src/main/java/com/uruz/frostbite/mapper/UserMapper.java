package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.user.UserSelfQueryResponse;
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

    public int softDeleteSelfUser(Integer userId);

    public Optional<User> selectUserByUuid(UUID uuid);
    public Optional<User> selectUserByUserId(Integer userId);

    public boolean selectUserExistsByNameAndPhoneNumber(@Param("email") String email, @Param("phoneNumber")String phoneNumber);

    public User selectUserByEmail(String email);
}
