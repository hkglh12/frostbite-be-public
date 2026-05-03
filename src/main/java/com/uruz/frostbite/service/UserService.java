package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.user.UserJoinRequest;
import com.uruz.frostbite.dto.user.UserJoinResponse;
import com.uruz.frostbite.dto.user.UserSelfQueryResponse;
import com.uruz.frostbite.enums.user.AccountType;
import com.uruz.frostbite.enums.common.ApiErrorCode;
import com.uruz.frostbite.enums.user.UserStatus;
import com.uruz.frostbite.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static com.uruz.frostbite.enums.common.ApiErrorCode.NOT_FOUND;

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
    public UserJoinResponse createUser(UserJoinRequest request) throws GeneralCustomException {
        String encPassword = bcryptor.encode(request.getPassword());
        LocalDate createDate = LocalDate.now();

        boolean isExists = userMapper.selectUserExistsByNameAndPhoneNumber(request.getEmail(), request.getPhoneNumber());
        if(isExists){
            throw new GeneralCustomException(ApiErrorCode.USER_EMAIL_OR_PHONE_ALREADY_EXISTS);
        }
        User user = User.create(
                request.getName(),
                request.getEmail(),
                encPassword,
                request.getPhoneNumber(),
                request.getBirth(),
                createDate,
                UUID.randomUUID(),
                AccountType.USER
        );

        userMapper.insertUser(user);

        if(user.getUserId() == null){
            log.error(String.format("User creation failed. Parameters, {}, {}"), user.getName(), user.getEmail());
            throw new IllegalStateException("User creation failed");
        }
        folderService.createEntryPrivateFolder(user.getName()+"의 개인폴더", user.getUserId());


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
    public UserSelfQueryResponse getSelfUesr(UUID uuid){
        UserSelfQueryResponse userSelfQueryResponse = userMapper.selectSelfUser(uuid).orElseThrow(
                () -> new GeneralCustomException(NOT_FOUND, "USER NOT FOUND")
        );

        return userSelfQueryResponse;
    }

    public boolean removeUser(UUID uuid){

        return userMapper.softDeleteUser(uuid, UserStatus.DELETED) == 1 ? true : false;
    }

    /** 타 메서드 헬퍼 */
    public User getUser(UUID uuid) throws GeneralCustomException{
        return userMapper.selectUser(uuid).orElseThrow(() -> new GeneralCustomException(NOT_FOUND));
    }

    /** UserDetailsService에서 사용할 Password 포함 리턴 메서드*/
    public User loadUserByEmail(String email){return userMapper.selectUserByEmail(email);}

}
