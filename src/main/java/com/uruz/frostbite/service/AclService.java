package com.uruz.frostbite.service;

import com.uruz.frostbite.domain.FolderAcl;
import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.PermissionBitMask;
import com.uruz.frostbite.mapper.AclMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AclService {
    /** 동작 실행 요청에 대한 "동작 허용", "접근 허용" 등의 데이터를 가진 ACL을 조작 및 CRUD 하기 위한 서비스  */

    private AclMapper aclMapper;

    @Autowired
    public AclService(AclMapper aclMapper){
        this.aclMapper = aclMapper;
    }

    // Folder <-> ACL 결합을 최대한 낮추기 위해, 직접 DTO를 만들어서 넘기는게 아니라 파라미터 인자값을 받아서 실행

    // 개인폴더는 ACL의 형태가 sub, root, entry 모두 동일해야 정상.
    // 그러나, 컨벤션 자체는 서브폴더는 부모폴더의 ACL을 따라간다 로 설계되었으므로, ENTRY, ROOT 용도로 호출 요망
    // 개인 ALLOW, 개인 자체에 대해 Permission 31
    public int createPrivateDefaultFolderAcl(Long folderId, Integer userId){
        FolderAcl folderAcl = FolderAcl.createUserAllow(folderId, userId, userId,
            PermissionBitMask.allMasks()
        );

        return aclMapper.insertFolderAcl(folderAcl);

    }

    // TEAM Folder Default ACL 생성 (ROOT, ENTRY)
    // 팀 전체 ALLOW, 팀 전체에 대해 Permission 31
    public int createTeamDefaultFolderAcl(Long folderId, Integer teamId, Integer createUserId){
        FolderAcl folderAcl = FolderAcl.createTeamAllow(folderId, teamId, createUserId,
                PermissionBitMask.allMasks());

        return aclMapper.insertFolderAcl(folderAcl);
    }

    // 서브폴더는 default로

    @Transactional(propagation = Propagation.REQUIRED)
    // ACL 클론 자체는 단일 쿼리 SELECT_INSERT 로 실행하지만, 폴더 생성 후 ACL 할당을 생각해보면,
    // ACL 생성 실패나면 폴더 생성도 되돌려져야 한다. 아니면 ACL 없는 폴더가 탄생함
    public void createSubFolderAcl(Long parentFolderId, Long targetFolderId, Integer createUserId){
        aclMapper.cloneParentFolderAclIntoSubFolder(parentFolderId, targetFolderId, createUserId, LocalDateTime.now(), FolderStatus.ACTIVE);
    }
}
