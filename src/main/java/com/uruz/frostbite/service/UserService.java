package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.RequestProcessingException;
import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.user.UserJoinRequest;
import com.uruz.frostbite.dto.user.UserJoinResponse;
import com.uruz.frostbite.dto.user.UserPasswordModifyRequest;
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

    public boolean assertUserEmailExists(String email){
        return userMapper.selectUserEmailExists(email);
    }

    /** 사용자 추가 */
    @Transactional
    public UserJoinResponse createUser(UserJoinRequest request) throws RequestProcessingException {
        String encPassword = bcryptor.encode(request.getPassword());
        LocalDate createDate = LocalDate.now();

        if(userMapper.selectUserEmailExists(request.getEmail())) {
            throw new RequestProcessingException(ApiErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }
        if(userMapper.selectUserExistsByPhoneNumber(request.getPhoneNumber())){
            throw new RequestProcessingException(ApiErrorCode.USER_PHONE_NUMBER_ALREADY_EXISTS);
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
        folderService.createPrivateEntryFolder(user.getName()+"의 개인폴더", user.getUserId());


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
    public UserSelfQueryResponse getSelfUesr(Integer userId){
        UserSelfQueryResponse userSelfQueryResponse = userMapper.selectSelfUser(userId).orElseThrow(
                () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
        );

        return userSelfQueryResponse;
    }

    /** 사용자 탈퇴 */

    public boolean removeSelfUser(Integer userId){

        return userMapper.softDeleteSelfUser(userId, UserStatus.DELETED) == 1 ? true : false;
    }

    /** 사용자 비밀번호 변경 */
    public boolean modifySelfPassword(Integer userId, UserPasswordModifyRequest passwordRequest){

        User user = userMapper.selectUserByUserIdForPassword(userId);

        if(!bcryptor.matches(passwordRequest.getCurrentPassword(), user.getPassword())) {
            throw new RequestProcessingException(ApiErrorCode.USER_PASSWORD_NOT_MATCH);
        }
        // FE에서 별도로 암호화 하지 않고 날아옴을 전제함.
        String newEncPassword = bcryptor.encode(passwordRequest.getNewPassword());
        return userMapper.updateUserPassword(userId, newEncPassword) == 1 ? true : false;
    }

    /** 타 메서드 헬퍼 */
    public User getUserByUuid(UUID uuid) throws RequestProcessingException {
        return userMapper.selectUserByUuid(uuid).orElseThrow(() -> new RequestProcessingException(ApiErrorCode.NOT_FOUND));
    }

    public User getUserByUserId(Integer userId) throws RequestProcessingException {
        return userMapper.selectUserByUserId(userId).orElseThrow(() -> new RequestProcessingException(ApiErrorCode.NOT_FOUND));
    }

    /** UserDetailsService에서 사용할 Password 포함 리턴 메서드*/
    public User loadUserByEmail(String email){return userMapper.selectUserByEmail(email);}

}
