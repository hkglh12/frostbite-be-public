package com.uruz.frostbite.service.resolver;

import com.uruz.frostbite.customException.ResourceAccessDeniedException;
import com.uruz.frostbite.customException.ResourceActionForbiddenException;
import com.uruz.frostbite.domain.FolderAcl;
import com.uruz.frostbite.dto.team.internal.TeamUserRelationProjection;
import com.uruz.frostbite.enums.AclEffectType;
import com.uruz.frostbite.enums.AclStatus;
import com.uruz.frostbite.enums.FolderType;
import com.uruz.frostbite.enums.PermissionBitMask;
import com.uruz.frostbite.dto.folder.internal.FolderTargetAccessControlProjection;
import com.uruz.frostbite.enums.team.TeamRole;
import com.uruz.frostbite.mapper.AclMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccessControlResolverService {
    /** 동작 실행 요청에 대한 "동작 허용", "접근 허용" 등의 행위 허가 판정을 위한 R 서비스 */

    private final AclMapper aclMapper;
    private final FolderResolverService folderResolverService;
    private final TeamResolverService teamResolverService;

    @Autowired
    public AccessControlResolverService(AclMapper aclMapper, FolderResolverService folderResolverService, TeamResolverService teamResolverService) {
        this.aclMapper = aclMapper;
        this.folderResolverService = folderResolverService;
        this.teamResolverService = teamResolverService;

    }

    // TODO testcase 풍부하게 작성
    /*
    * TEAM ALLOW + USER DENY
    TEAM ALLOW + USER ALLOW(더 높은 권한)
    USER ALLOW만 존재
    ALLOW 없음
    관리자 우회
    * */
    public boolean validateUserActionPermissionForFolder(Integer userId, Long folderId, PermissionBitMask claimedAction) {
        // 1. 폴더 정보 1차 조회 (폴더 종류 조회, scope 조회용)
        FolderTargetAccessControlProjection folderProjection = folderResolverService.getFolderForAclEval(folderId);
        // 2-1. private -> 사용자 폴더 resource scope가 지금 접근 시도하는 사용자인지 확인

        // 팀 / 유저 max perm bit select시 통합으로 처리하기 위한 공통변수처리
        Integer teamId = null;
        if(folderProjection.getFolderType() == FolderType.PRIVATE){
            if(folderProjection.getResourceScopeUserId().equals(userId)) {
                // 개인 폴더면 사용자는 무조건 max 권한을 가지고 있어야 하므로 행위 권한(permissionBitMask) 검사 불필요
                // 추후 개발 예정인 공유 기능은 공유테이블, 공유쪽 acl로 처리할 예정. parameter로 페이지 접근 시 isShare=true 로 들어오면 됨
                return true;
            } else {
                throw new ResourceAccessDeniedException("User information is not match with resource scope");
            }
        } else {
            teamId = folderProjection.getResourceScopeTeamId();
            // 2-2. TEAM -> 사용자가 team 내 relation이 있는지 조회 + MEMBER 위쪽의 역할(초과)을 가지고 있는지 확인 (true -> 허용으로 종료)
            TeamUserRelationProjection relation = teamResolverService.selectValidTeamUserRelation(userId, teamId);
            if(relation.getRole().getValue() > TeamRole.MEMBER.getValue()){
                return true;
            }
            // 3. 사용자가 해당 폴더에 대한 DENY가 있는지 조회 ( true -> 거부로 종료)
            // 팀 - 폴더 는 현재 1:1 매칭. subject_type = team && effect_type = deny 인 명시적 팀 거부 acl은 있을 수 없음 (비즈니스 설계에 근거함)
                // 팀 ALLOW ACL이 존재하지 않는 상태에서도
                // 특정 사용자 USER ALLOW ACL은 존재할 수 있음. 유념해야함
            // 단, subject_type = team && effect_type = allow + subject_type = user && effect_type = deny 는 있을 수 있으므로,사용자 deny 검사 수행
            //TODO DB에는 effectType을 effect_type으로 관리하는데 java에서는 AclEffectType이다. DB 및 관련 문서 모두 AclEffectType으로 올라가야 한다.
            if(aclMapper.isUserDeniedByAcl(userId, folderId, AclEffectType.DENY, AclStatus.ACTIVE)){
                throw new ResourceAccessDeniedException("User is explicitly denied from resource");
            }

        }
        // 4. 사용자가 해당 폴더에 대한 ALLOW 권한이 있는지, PermissionBitMask MAX() 값이 몇인지 조회
        // 사용자가 직접 폴더에 행위권한을 받았을 경우 || 팀이 행위권한을 들고있을 경우 || 폴더가 특정 인원에게만 허용된 타입 일 경우 셋다 생각해야한다.

        // 팀 폴더지만 특정 사용자에게만 권한이 부여된 경우 -> Folder.accessPolicy = user
        // 이경우 ACL이 직접적으로 있어야 한다.
        /*
        @Deprecated
        if(folderProjection.getAccessPolicy() == AccessPolicy.USER){

        } else if(folderProjection.getAccessPolicy() == AccessPolicy.TEAM){
            // 팀 폴더이며 팀 접근 허용인 경우 -> 사용자 권한이 더 높거나 팀 권한이 더 높다.
        }

         */

        // 팀 폴더이지만, 특정 사용자에게만 권한이 부여된 경우 -> 팀 ALLOW ACL이 제거된다. (컨벤션에 의한 비즈니스 설계)
        // 따라서 folderType 별로 if 분기를 할 필요 없이 max 값만 가져오면 일단 "허용 된 케이스에 대한 MAX Perm ACL" 을 가져올 수 있다.
        // 이때, "팀에는 거부되었으나 특정 사용자에게는 허용" 의 경우가 있을 수 있으므로, (team ACL 없음, 몇몇 User ACL ALLOW 있음) -> effectTYpe = allow를 명시적으로 걸어야함.
        FolderAcl maxPermissionAFolderAcl = aclMapper.selectActiveFolderMaxPermAclForEval(userId, teamId, folderId, AclEffectType.ALLOW, AclStatus.ACTIVE).orElseThrow(
                () -> new ResourceAccessDeniedException("User is not allowed explicitly")
        );

        if(maxPermissionAFolderAcl.hasPermission(claimedAction)){
           return true;
        } else {
            throw new ResourceActionForbiddenException("Claimed action forbidden");
        }

    }
}
