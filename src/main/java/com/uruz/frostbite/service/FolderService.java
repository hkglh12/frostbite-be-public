package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.folder.FolderCreateRequest;
import com.uruz.frostbite.dto.folder.MovedSubFolderPathCommand;
import com.uruz.frostbite.dto.folder.SubFolderMoveRequest;
import com.uruz.frostbite.dto.folder.TeamRootFolderCreateRequest;
import com.uruz.frostbite.dto.folder.internal.PrivateEntryFolderCondition;
import com.uruz.frostbite.dto.folder.internal.SubFolderMoveCommand;
import com.uruz.frostbite.dto.folder.internal.TeamEntryFolderCondition;
import com.uruz.frostbite.enums.FolderType;
import com.uruz.frostbite.enums.common.ApiErrorCode;
import com.uruz.frostbite.mapper.FolderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Folder privateEntryFolder = getPrivateEntryFolder(condition);

        Folder newFolder = Folder.createPrivateRootFolder(
                folderName, privateEntryFolder.getFolderId(), privateEntryFolder.getCreateUserId()
        );

        folderMapper.insertRootFolder(newFolder);

        return newFolder;
    }

    /** 팀 UUID로부터 팀 엔트리폴더 아래 루트폴더 생성*/
    public Folder createTeamRootFolder(UUID userUuid, TeamRootFolderCreateRequest request){

        TeamEntryFolderCondition condition = TeamEntryFolderCondition.of(userUuid, request.getTeamUuid());

        Folder teamEntryFolder = getTeamEntryFolder(condition);
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

    /** Sub폴더 이동
     * 1. Sub폴더만 이동 가능
     * 2. FolderType 변경 불가
     * */

    // EdgeCase : 재귀이동 (4 아래에 있는 11 폴더에 4 폴더를 이동 시도) 막아야 함
    // TODO FOLDER DEPTH column 추가 고려해볼것. 이동가능한 폴더를 SubFolder로 제약하려면 지금 폴더가 ROOT인지 엔트리인지 봐야함
    // 이때, 굳이 EntryFolder 정보 찾아오고, 그 EntryFolder가 직접부모인지 검증할게 아니라, depth 정보 하나면
    // 0계층, 1계층에 대해서 동작을 막아버리면 되는 이점이 있음. 단, update시 @Transactional이 걸려야겠지
    @Transactional
    public Folder moveSubFolder(Integer userId, SubFolderMoveRequest request){
        Folder targetFolder = getSimpleFolder(request.getTargetFolderUuid());
        Folder destinationFolder = getSimpleFolder(request.getDestinationFolderUuid());

        // 폴더 종류 변경 불가
        if(targetFolder.getFolderType() != destinationFolder.getFolderType()){
            throw new GeneralCustomException(ApiErrorCode.BAD_REQUEST, "FOLDER TYPE MISMATCH");
        }

        // Owner 변경불가
        // Team FolderType + Owner 변경 = 폴더 타 팀으로 인계
        // Private FolderType + Owner 변경 = 타 유저에게 폴더 소유권 이전
        if(targetFolder.getOwnerId() != destinationFolder.getOwnerId()){
            throw new GeneralCustomException(ApiErrorCode.BAD_REQUEST, "FOLDER OWNER CAN NOT BE CHANGED");
        }

        SubFolderMoveCommand command = SubFolderMoveCommand.of(targetFolder.getFolderId(), destinationFolder.getFolderId(), userId);

        int affectedRow = folderMapper.updateSubFolderParent(command);

        if(affectedRow < 0) {
            throw new GeneralCustomException(ApiErrorCode.INTERNAL_ERROR, "FOLDER MOVE FAILED");
        }

        // TODO 현재는 Mybatis 레벨에서 concat(||) 로 폴더경로를 만들고있다.
        // 이걸 JAVA 레벨로 끌어올려야 한다. 비즈니스 로직 (폴더 이동에 따른 경로계산) 이 SQL 레이어에 숨는다는 유지보수 어려움과
        // XML이 복잡해지는것을 방지하기 위함
        String oldFolderIdpath = targetFolder.getFolderIdPath();
        String oldFolderNamePath = targetFolder.getFolderNamePath();
        String newFolderIdPath = destinationFolder.getFolderIdPath() + targetFolder.getFolderId() + "/";
        String newFolderNamePath = destinationFolder.getFolderNamePath() + targetFolder.getName() + "/";
        MovedSubFolderPathCommand pathCommand = new MovedSubFolderPathCommand(
                oldFolderIdpath, oldFolderNamePath, newFolderIdPath, newFolderNamePath
        );

        int pathAffectedRow = folderMapper.updateSubFolderPath(pathCommand);

        Folder movedFolder = getSimpleFolder(request.getTargetFolderUuid());

        return movedFolder;
    }

    /** 폴더 UUID로 폴더 조회*/
    public Folder getSimpleFolder(UUID folderUuid){
        return folderMapper.selectFolderSimpleByUuid(folderUuid).orElseThrow(
                () -> new GeneralCustomException(ApiErrorCode.NOT_FOUND, "FOLDER NOT FOUND")
        );
    }


    /** 개인 ENTRY FOLDER 조회 */
    public Folder getPrivateEntryFolder(PrivateEntryFolderCondition condition){
        return folderMapper.selectPrivateEntryFolder(condition).orElseThrow(() ->
                new GeneralCustomException(ApiErrorCode.NOT_FOUND, "FOLDER NOT FOUND"));
    }

    /** 팀 ENTRY FOLDER 조회*/
    public Folder getTeamEntryFolder(TeamEntryFolderCondition condition){
        return folderMapper.selectTeamEntryFolder(condition).orElseThrow(() ->
                new GeneralCustomException(ApiErrorCode.NOT_FOUND, "FOLDER NOT FOUND"));

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
