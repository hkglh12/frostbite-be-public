package com.uruz.frostbite.integration;

import com.uruz.frostbite.dto.user.UserJoinRequest;
import com.uruz.frostbite.dto.user.UserJoinResponse;
import com.uruz.frostbite.enums.PermissionBitMask;
import com.uruz.frostbite.enums.user.UserStatus;
import com.uruz.frostbite.service.UserService;
import com.uruz.frostbite.support.IntegrationTestSupport;
import com.uruz.frostbite.support.TestDataQuerySupport.AclRow;
import com.uruz.frostbite.support.TestDataQuerySupport.FolderRow;
import com.uruz.frostbite.support.TestDataQuerySupport.UserRow;
import com.uruz.frostbite.support.UserDomainEntitySupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserService 통합 테스트")
class UserServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("회원가입은 사용자와 개인 엔트리 폴더 및 기본 ACL을 원자적으로 생성한다")
    void createUserCreatesUserFolderAndAcl() {
        // given
        UserJoinRequest request = UserDomainEntitySupport.joinRequest();

        // when
        UserJoinResponse response = userService.createUser(request);

        // then
        UserRow user = query.findUserByEmail(request.getEmail());
        assertThat(user.name()).isEqualTo(request.getName());
        assertThat(user.phoneNumber()).isEqualTo(request.getPhoneNumber());
        assertThat(user.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.getUuid()).isEqualTo(user.uuid());

        FolderRow entryFolder = only(query.findEntryFoldersByUser(user.id()));
        AclRow acl = only(query.findAclsByFolder(entryFolder.id()));
        assertThat(acl.subjectUserId()).isEqualTo(user.id());
        assertThat(acl.permissionBitMask()).isEqualTo(PermissionBitMask.of(PermissionBitMask.allMasks()));
    }


    private static <T> T only(List<T> rows) {
        assertThat(rows).hasSize(1);
        return rows.getFirst();
    }
}
