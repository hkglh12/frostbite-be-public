package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.folder.FolderCreateRequest;
import com.uruz.frostbite.dto.folder.TeamRootFolderCreateRequest;
import com.uruz.frostbite.dto.folder.internal.PrivateEntryFolderCondition;
import com.uruz.frostbite.dto.folder.internal.TeamEntryFolderCondition;
import com.uruz.frostbite.enums.FolderType;
import com.uruz.frostbite.enums.common.ApiErrorCode;
import com.uruz.frostbite.mapper.FolderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FolderService {

    private final FolderMapper folderMapper;

    private final UserResolverService userResolverService;

    @Autowired
    public FolderService(FolderMapper folderMapper, UserResolverService userResolverService){
        this.folderMapper = folderMapper;
        this.userResolverService = userResolverService;
    }

    /** 사용자 UUID로 부터 개인 엔트리폴더 아래 루트폴더 생성 */
    public Folder createPrivateRootFolder(UUID userUuid, String folderName){

        PrivateEntryFolderCondition condition = PrivateEntryFolderCondition.fromUserUuid(userUuid);

        // 사용자 UUID를 통한 개인 엔트리폴더 조회
        Folder privateEntryFolder = folderMapper.selectPrivateEntryFolder(condition).orElseThrow(() ->
                new GeneralCustomException(ApiErrorCode.NOT_FOUND, "FOLDER NOT FOUND"));

        Folder newFolder = Folder.createPrivateRootFolder(
                folderName, privateEntryFolder.getFolderId(), privateEntryFolder.getCreateUserId()
        );

        folderMapper.insertRootFolder(newFolder);

        return newFolder;
    }

    /** 팀 UUID로부터 팀 엔트리폴더 아래 루트폴더 생성*/
    public Folder createTeamRootFolder(UUID userUuid, TeamRootFolderCreateRequest request){

        TeamEntryFolderCondition condition = TeamEntryFolderCondition.of(userUuid, request.getTeamUuid());

        Folder teamEntryFolder = folderMapper.selectTeamEntryFolder(condition).orElseThrow(() ->
            new GeneralCustomException(ApiErrorCode.NOT_FOUND, "FOLDER NOT FOUND"));
        User user = userResolverService.getUser(userUuid);
        Folder newFolder = Folder.createTeamRootFolder(request.getName(), teamEntryFolder.getFolderId(),
                teamEntryFolder.getOwnerId(), user.getUserId());

        folderMapper.insertRootFolder(newFolder);
        return null;
    }

    /** 부모 폴더 UUID로부터 서브폴더 생성 */
    public Folder createSubFolder(UUID userUuid, FolderCreateRequest request) {
        Folder parentFolder = getSimpleFolder(request.getParentFolderUuid());
        User user = userResolverService.getUser(userUuid);

        Folder newFolder = null;
        if(request.getFolderType().equals(FolderType.PRIVATE)){
            newFolder = Folder.createPrivateSubFolder(request.getName(), parentFolder.getFolderId(), user.getUserId());
        } else {
            newFolder = Folder.createTeamSubFolder(request.getName(), parentFolder.getFolderId(), parentFolder.getOwnerId(), user.getUserId());
        }

        folderMapper.insertSubFolder(newFolder);
        return newFolder;
    }

    /** 폴더 UUID로 폴더 조회*/
    public Folder getSimpleFolder(UUID folderUuid){
        return folderMapper.selectFolderSimpleByUuid(folderUuid).orElseThrow(
                () -> new GeneralCustomException(ApiErrorCode.NOT_FOUND, "FOLDER NOT FOUND")
        );
    }

    /** 최초 사용자 가입 시 개인폴더 할당 */
    public void createEntryPrivateFolder(String initialFolderName, Integer userId){
        // 최초가입시 자동 개인폴더 root 할당. 이경우 일반 폴더생성과 관심사 흐름이 다름 (폴더생성 =/= 자동루트폴더생성)
        // 따라서 별도 메서드로 처리

        // transactional 걸려있고 컨트롤러 공개가 안되는 메서드, userId는 정상으로 처리되었을것이라 신뢰하고 앱 레벨 정합성검사 생략
        Folder initialPrivateRootFolder = Folder.createEntryFolder(initialFolderName, userId, FolderType.PRIVATE);
        folderMapper.insertEntryFolder(initialPrivateRootFolder);
        // TODO : 폴더 생성 후 ACL 제어 추가 필요

    }

}
