package com.uruz.frostbite.service;

import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.UserJoinRequest;
import com.uruz.frostbite.dto.UserJoinResponse;
import com.uruz.frostbite.dto.UserSelfQueryCondition;
import com.uruz.frostbite.dto.UserSelfQueryResponse;
import com.uruz.frostbite.enums.UserStatus;
import com.uruz.frostbite.internalvo.UserDetailProjection;
import com.uruz.frostbite.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class UserService {

    private final UserMapper userMapper;

    private final BCryptPasswordEncoder bcryptor;

    public UserService(UserMapper userMappser, BCryptPasswordEncoder bcryptor){
        this.userMapper = userMappser;
        this.bcryptor = bcryptor;
    }

    /** 사용자 추가 */
    public UserJoinResponse createUser(UserJoinRequest createRequest){
        String encPassword = bcryptor.encode(createRequest.getPassword());
        LocalDate createDate = LocalDate.now();

        User user = User.create(
                createRequest.getName(),
                createRequest.getEmail(),
                encPassword,
                createRequest.getPhoneNumber(),
                createRequest.getBirth(),
                createDate,
                UUID.randomUUID()
        );

        userMapper.insertUser(user);

        UserJoinResponse response = UserJoinResponse.builder()
                .uuid(user.getUuid())
                .phoneNumber(user.getPhoneNumber())
                .birth(user.getBirth())
                .email(user.getEmail())
                .name(user.getName())
                .createDate(user.getCreateDate())
                .build();

        return response;
    }

    /** 사용자 본인 정보 조회 (자기자신 정보만 조회 가능) */
    public UserSelfQueryResponse getSelfUesr(UserSelfQueryCondition getUserCondition){
        UserDetailProjection detailProjc = userMapper.selectSelfUser(getUserCondition);

        return UserSelfQueryResponse.builder().uuid(detailProjc.getUuid())
                .phoneNumber(detailProjc.getPhoneNumber())
                .name(detailProjc.getName())
                .email(detailProjc.getEmail())
                .birth(detailProjc.getBirth())
                .createDate(detailProjc.getCreateDate()).build();
    }

    public boolean removeUser(UUID uuid){

        return userMapper.softDeleteUser(uuid, UserStatus.DELETED) == 1 ? true : false;
    }

}
