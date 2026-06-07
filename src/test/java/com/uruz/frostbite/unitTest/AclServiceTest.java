package com.uruz.frostbite.unitTest;

import com.uruz.frostbite.domain.FolderAcl;
import com.uruz.frostbite.enums.AclEffectType;
import com.uruz.frostbite.enums.AclStatus;
import com.uruz.frostbite.enums.AclSubjectType;
import com.uruz.frostbite.enums.PermissionBitMask;
import com.uruz.frostbite.mapper.AclMapper;
import com.uruz.frostbite.service.AclService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AclService 단위 테스트")
class AclServiceTest {

    @Mock
    private AclMapper aclMapper;

    @InjectMocks
    private AclService aclService;

    @Test
    @DisplayName("개인 폴더 기본 ACL은 사용자에게 모든 권한을 허용한다")
    void createPrivateDefaultFolderAclInsertsUserAllowAllAcl() {
        // given
        long folderId = 10L;
        int userId = 7;
        given(aclMapper.insertFolderAcl(org.mockito.ArgumentMatchers.any(FolderAcl.class))).willReturn(1);

        // when
        int affectedRows = aclService.createPrivateDefaultFolderAcl(folderId, userId);

        // then
        ArgumentCaptor<FolderAcl> captor = ArgumentCaptor.forClass(FolderAcl.class);
        verify(aclMapper).insertFolderAcl(captor.capture());
        FolderAcl acl = captor.getValue();

        assertThat(affectedRows).isOne();
        assertThat(acl.getFolderId()).isEqualTo(folderId);
        assertThat(acl.getSubjectType()).isEqualTo(AclSubjectType.USER);
        assertThat(acl.getSubjectUserId()).isEqualTo(userId);
        assertThat(acl.getSubjectTeamId()).isNull();
        assertThat(acl.getEffectType()).isEqualTo(AclEffectType.ALLOW);
        assertThat(acl.getStatus()).isEqualTo(AclStatus.ACTIVE);
        assertThat(acl.hasAllPermissions(PermissionBitMask.allMasks())).isTrue();
    }
}
