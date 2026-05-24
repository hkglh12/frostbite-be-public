package com.uruz.frostbite.service;

import com.uruz.frostbite.domain.FolderAcl;
import com.uruz.frostbite.enums.PermissionBitMask;
import com.uruz.frostbite.mapper.AclMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AclService {
    private AclMapper aclMapper;

    @Autowired
    public AclService(AclMapper aclMapper){
        this.aclMapper = aclMapper;
    }


    // Folder<-> ACL 결합을 낮추기 위해 DTO가 아닌 파라미터 인자값을 직접 받아서 작업
    public int createPrivateFolderAcl(Integer userId, Long folderId){
        FolderAcl folderAcl = FolderAcl.createUserAllow(folderId, userId, userId,
            PermissionBitMask.allMasks()
        );

        return aclMapper.insertFolderAcl(folderAcl);

    }

}
