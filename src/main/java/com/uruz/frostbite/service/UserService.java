package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.UserJoinRequest;
import com.uruz.frostbite.dto.UserJoinResponse;
import com.uruz.frostbite.dto.UserSelfQueryCondition;
import com.uruz.frostbite.dto.UserSelfQueryResponse;
import com.uruz.frostbite.enums.AccountType;
import com.uruz.frostbite.enums.ErrorApiResponseCode;
import com.uruz.frostbite.enums.UserStatus;
import com.uruz.frostbite.internalvo.UserDetailProjection;
import com.uruz.frostbite.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static com.uruz.frostbite.enums.ErrorApiResponseCode.NOT_FOUND;

@Service
@Slf4j
public class UserService {

    private final UserMapper userMapper;

    private final BCryptPasswordEncoder bcryptor;

    private final FolderService folderService;

    @Autowired
    public UserService(UserMapper userMappser, BCryptPasswordEncoder bcryptor, FolderService folderService){
        this.userMapper = userMappser;
        this.bcryptor = bcryptor;
        this.folderService = folderService;
    }

    /** 사용자 추가 */
    @Transactional
    public UserJoinResponse createUser(UserJoinRequest createRequest) throws GeneralCustomException {
        String encPassword = bcryptor.encode(createRequest.getPassword());
        LocalDate createDate = LocalDate.now();

        User user = User.create(
                createRequest.getName(),
                createRequest.getEmail(),
                encPassword,
                createRequest.getPhoneNumber(),
                createRequest.getBirth(),
                createDate,
                UUID.randomUUID(),
                AccountType.USER
        );

        userMapper.insertUser(user);

        if(user.getUserId() == null){
            log.error(String.format("User creation failed. Parameters, {}, {}"), user.getName(), user.getEmail());
            throw new IllegalStateException("User creation failed");
        }
        folderService.createInitialRootPrivateFolder(user.getName()+"의 개인폴더", user.getUserId());


        return UserJoinResponse.builder()
                .uuid(user.getUuid())
                .phoneNumber(user.getPhoneNumber())
                .birth(user.getBirth())
                .email(user.getEmail())
                .name(user.getName())
                .createDate(user.getCreateDate())
                .status(user.getStatus())
                .build();
    }

    /** 사용자 본인 정보 조회 (자기자신 정보만 조회 가능) */
    public UserSelfQueryResponse getSelfUesr(String uuid){
        UserDetailProjection detailProjc = userMapper.selectSelfUser(uuid).orElseThrow(
                () -> new GeneralCustomException(NOT_FOUND, "USER NOT FOUND")
        );

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
