package com.uruz.frostbite.unitTest;

import com.uruz.frostbite.domain.FolderAcl;
import com.uruz.frostbite.enums.PermissionBitMask;
import com.uruz.frostbite.mapper.AclMapper;
import com.uruz.frostbite.service.AclService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AclServiceTest {
    @Mock
    AclMapper aclMapper;
    @InjectMocks
    AclService aclService;

    @Test
    void createPrivateFolderAclShouldInsertUserAllowAclWithMock(){

        // given : 데이터 테스트 준비과정
        int userId = 7;
        Long folderId = 10L;
        // 어떤 FolderAcl이 오더라도, 무조건 1 리턴하라.
        given(aclMapper.insertFolderAcl(any(FolderAcl.class))).willReturn(1);

        // Mapper로 넘어가는 인자값 캡쳐링 준비
        ArgumentCaptor<FolderAcl> captor = ArgumentCaptor.forClass(FolderAcl.class);
        // when : 실제 실행
        // 호출 -> @Mock은 호출되면 호출기록을 내부에 남김
        int result = aclService.createPrivateFolderAcl(userId, folderId);


        // then : 검증
        // 진짜 mock 잘 불렀는지 보는 레벨
        assertThat(result).isEqualTo(1);

        // mapper에 호출기록있는지 검사하고 (Verify) -> 검사하는 순간 기록에 있던 인자를 꺼내다가 잡아라 (captor.capture())
        verify(aclMapper).insertFolderAcl(captor.capture());

        FolderAcl acl = captor.getValue();

        assertThat(acl.getFolderId()).isEqualTo(folderId);
        assertThat(acl.getSubjectUserId()).isEqualTo(userId);
        assertThat(acl.getSubjectTeamId()).isNull();
        assertThat(acl.getPermissionBitMask()).isEqualTo(PermissionBitMask.of(PermissionBitMask.allMasks()));

        assertThat(acl.hasPermission(PermissionBitMask.BROWSE)).isTrue();
        assertThat(acl.hasPermission(PermissionBitMask.READ)).isTrue();
        assertThat(acl.hasPermission(PermissionBitMask.WRITE)).isTrue();
        assertThat(acl.hasPermission(PermissionBitMask.CONTROL)).isTrue();
        assertThat(acl.hasPermission(PermissionBitMask.MANAGE)).isTrue();

    }
}
