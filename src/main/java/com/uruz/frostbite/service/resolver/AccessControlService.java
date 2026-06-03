package com.uruz.frostbite.service.resolver;

import com.uruz.frostbite.enums.PermissionBitMask;
import com.uruz.frostbite.mapper.AclMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {
    /** 동작 실행 요청에 대한 "동작 허용", "접근 허용" 등의 행위 허가 판정을 위한 R 서비스 */

    private final AclMapper aclMapper;
    private final FolderResolverService folderResolverService;
    private final TeamResolverService teamResolverService;

    @Autowired
    public AccessControlService(AclMapper aclMapper, FolderResolverService folderResolverService, TeamResolverService teamResolverService) {
        this.aclMapper = aclMapper;
        this.folderResolverService = folderResolverService;
        this.teamResolverService = teamResolverService;

    }

    public boolean validateAccessingUserForFolder(Integer userId, Long folderId, PermissionBitMask permissionBitMask) {


        return false;
    }
}
