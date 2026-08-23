package com.uruz.frostbite.service.resolver;

import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.dto.folder.internal.FolderTargetAccessControlProjection;
import com.uruz.frostbite.dto.folder.internal.PrivateFolderCondition;
import com.uruz.frostbite.mapper.FolderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class FolderResolverService {
    /** folder 도메인 외 도메인에서 폴더 조회를 요청할 때 객체정보를 전달하는 서비스 */

    private final FolderMapper folderMapper;

    @Autowired
    public FolderResolverService(FolderMapper folderMapper){
        this.folderMapper = folderMapper;
    }
    public FolderTargetAccessControlProjection getFolderForAclEval(Long folderId){

        return folderMapper.selectFolderWithAccessControlResources(folderId);

    }
    public Optional<Folder> resolveAccessibleFolderByFolderUuid(UUID folderUuid, Integer userId){
        return folderMapper.selectAccessibleFolderByFolderUuid(folderUuid, userId);
    }


    public PrivateFolderCondition getPrivateFolderCondition(Integer userId,UUID folderUuid) {
        return PrivateFolderCondition.fromUserId(userId).withFolderUuid(folderUuid);
    }
}
