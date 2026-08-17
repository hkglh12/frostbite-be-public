package com.uruz.frostbite.service.resolver;

import com.uruz.frostbite.customException.RequestProcessingException;
import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.dto.folder.internal.FolderTargetAccessControlProjection;
import com.uruz.frostbite.enums.common.ApiErrorCode;
import com.uruz.frostbite.mapper.FolderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Folder resolveFolderByUuid(UUID folderUuid){
        return folderMapper.selectFolderByUuid(folderUuid).orElseThrow(() ->new RequestProcessingException(ApiErrorCode.NOT_FOUND));
    }
}
