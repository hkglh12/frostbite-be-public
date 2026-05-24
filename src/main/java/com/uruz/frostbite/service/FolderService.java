package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.domain.Team;
import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.folder.*;
import com.uruz.frostbite.dto.folder.internal.*;
import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
import com.uruz.frostbite.enums.common.ApiErrorCode;
import com.uruz.frostbite.mapper.FolderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FolderService {

    private final FolderMapper folderMapper;

    private final UserResolverService userResolverService;
    private final TeamResolverService teamResolverService;

    @Autowired
    public FolderService(FolderMapper folderMapper, UserResolverService userResolverService,
                         TeamResolverService teamResolverService){
        this.folderMapper = folderMapper;
        this.userResolverService = userResolverService;
        this.teamResolverService = teamResolverService;
    }

    /** 최초 사용자 가입 시 개인폴더 할당 */
    public void createEntryPrivateFolder(String initialFolderName, Integer userId){
        // 최초가입시 자동 개인폴더 root 할당. 이경우 일반 폴더생성과 관심사 흐름이 다름 (폴더생성 =/= 자동루트폴더생성)
        // 따라서 별도 메서드로 처리

        // transactional 걸려있고 컨트롤러 공개가 안되는 메서드, userId는 정상으로 처리되었을것이라 신뢰하고 앱 레벨 정합성검사 생략
        Folder initialPrivateRootFolder = Folder.createPrivateEntryFolder(initialFolderName, userId);
        folderMapper.insertEntryFolder(initialPrivateRootFolder);
        // TODO : 폴더 생성 후 ACL 제어 추가 필요

    }

    /** 사용자 UUID로 부터 개인 엔트리폴더 아래 루트폴더 생성 */
    public SimpleFolderResponse createPrivateRootFolder(Integer userId, String folderName){

        PrivateEntryFolderCondition condition = PrivateEntryFolderCondition.fromUserId(userId);
        // 사용자 UUID를 통한 개인 엔트리폴더 조회
        Folder privateEntryFolder = getPrivateEntryFolder(condition);

        Folder newFolder = Folder.createPrivateRootFolder(
                folderName, privateEntryFolder.getFolderId(), privateEntryFolder.getCreateUserId()
        );

        folderMapper.insertRootFolder(newFolder);
        return selectSimpleFolderByFolderId(newFolder.getFolderId());
    }

    /** 팀 UUID로부터 팀 엔트리폴더 아래 루트폴더 생성*/
    public SimpleFolderResponse createTeamRootFolder(Integer userId, TeamRootFolderCreateRequest request){

        TeamEntryFolderCondition condition = TeamEntryFolderCondition.of(userId, request.getTeamUuid());

        Folder teamEntryFolder = getTeamEntryFolder(condition);
        Folder newFolder = Folder.createTeamRootFolder(request.getName(), teamEntryFolder.getFolderId(),
                teamEntryFolder.getResourceScopeTeamId(), userId);

        folderMapper.insertRootFolder(newFolder);
        return selectSimpleFolderByFolderId(newFolder.getFolderId());
    }

    /** 부모 폴더 UUID로부터 서브폴더 생성 */
    public SimpleFolderResponse createFolder(Integer userId, FolderCreateRequest request) {
        Folder parentFolder = selectFolderByUuid(request.getParentFolderUuid());
        // 폴더타입은 이미 Context 상 부모폴더에 있음.
        FolderType folderType = parentFolder.getFolderType();
        Folder newFolder = null;
        if(folderType.equals(FolderType.PRIVATE)){
            newFolder = Folder.createPrivateSubFolder(request.getName(), parentFolder.getFolderId(), userId);
        } else {
            newFolder = Folder.createTeamSubFolder(request.getName(), parentFolder.getFolderId(), parentFolder.getResourceScopeTeamId(),userId);
        }

        folderMapper.insertSubFolder(newFolder);
        return selectSimpleFolderByFolderId(newFolder.getFolderId());
    }

    /** offsetBase로 개인 RootFolder List 조회에 사용 */
    public SimpleFolderListOffsetResponse getPrivateRootFoldersByOffsetBase(Integer userId, Integer offset, Integer limit){
        PrivateEntryFolderCondition entryCondition = PrivateEntryFolderCondition.fromUserId(userId);

        Folder privateEntryFolder = getPrivateEntryFolder(entryCondition);

        SimplePrivateFolderOffsetConditionByFolderId folderCondition = SimplePrivateFolderOffsetConditionByFolderId.of(
                userId, privateEntryFolder.getFolderId(), FolderStatus.ACTIVE, offset, limit
        );

        List<SimpleFolderResponse> list = selectSimpleFolderOffsetById(folderCondition);
        int totalCount = folderMapper.selectSimpleFolderTotalCountById(folderCondition);
        int currentMax = (offset+1) * limit + list.size();
        SimpleFolderListOffsetResponse response = new SimpleFolderListOffsetResponse(list, totalCount, currentMax);

        return response;
    }


    /** KEYSET Base로 개인 ROOT Folder List 조회*/
    public SimpleFolderListKeysetResponse getPrivateRootFoldersByKeysetBase(Integer userId, UUID lastFolderUuid, Integer limit){
        PrivateEntryFolderCondition entryCondition = PrivateEntryFolderCondition.fromUserId(userId);

        Folder privateEntryFolder = getPrivateEntryFolder(entryCondition);

        SimpleFolderKeyBaseConditionByFolderId folderCondition = SimpleFolderKeyBaseConditionByFolderId.of(
                privateEntryFolder.getFolderId(), FolderStatus.ACTIVE, lastFolderUuid, limit);

        List<SimpleFolderResponse> list = selectSimpleFolderKeyBaseById(folderCondition);
        Boolean hasNext = folderMapper.selectSimpleFolderKeyBaseHasNext(folderCondition);
        UUID newLastFolderUuid = list.get(list.size()-1).getUuid();
        SimpleFolderListKeysetResponse response = new SimpleFolderListKeysetResponse(list, hasNext, newLastFolderUuid);
        return response;
    }

    /** offsetBase로 팀 RootFolder List 조회에 사용*/
    public SimpleFolderListOffsetResponse getTeamRootFoldersByOffsetBase(Integer userId, UUID teamUuid, Integer offset, Integer limit){
        TeamEntryFolderCondition entryCondition = TeamEntryFolderCondition.of(userId, teamUuid);

        Folder teamEntryFolder = getTeamEntryFolder(entryCondition);

        SimplePrivateFolderOffsetConditionByFolderId folderCondition = SimplePrivateFolderOffsetConditionByFolderId.of(
                teamEntryFolder.getResourceScopeTeamId(), teamEntryFolder.getFolderId(), FolderStatus.ACTIVE, offset, limit
        );

        List<SimpleFolderResponse> list = selectSimpleFolderOffsetById(folderCondition);
        int totalCount = folderMapper.selectSimpleFolderTotalCountById(folderCondition);
        int currentMax = (offset+1) * limit + list.size();
        SimpleFolderListOffsetResponse response = new SimpleFolderListOffsetResponse(list, totalCount, currentMax);

        return response;
    }

    /** KeyBase 팀 RootFolder List  조회에 사용*/

    public SimpleFolderListKeysetResponse getTeamRootFoldersByKeysetBase(Integer userId, UUID teamUuid, UUID lastFolderUuid, Integer limit){
        TeamEntryFolderCondition entryCondition =  TeamEntryFolderCondition.of(userId, teamUuid);

        Folder teamEntryFolder = getTeamEntryFolder(entryCondition);

        SimpleFolderKeyBaseConditionByFolderId folderCondition = SimpleFolderKeyBaseConditionByFolderId.of(
                teamEntryFolder.getFolderId(), FolderStatus.ACTIVE, lastFolderUuid, limit
        );

        List<SimpleFolderResponse> list = selectSimpleFolderKeyBaseById(folderCondition);
        Boolean hasNext = folderMapper.selectSimpleFolderKeyBaseHasNext(folderCondition);
        UUID newLastFolderUuid = list.get(list.size()-1).getUuid();
        SimpleFolderListKeysetResponse response = new SimpleFolderListKeysetResponse(list, hasNext, newLastFolderUuid);
        return response;
    }

    /** 서브폴더 Keyset base 리스트 by uuid
     * -> keysetBase는 좌측트리로만 출력하므로, Entries(folder+file) 일 필요 없다.
     *
     */

    public SimpleFolderListKeysetResponse getSubFoldersByKeysetBaseByUuid(Integer userId, UUID parentFolderUuid, UUID lastFolderUuid, Integer limit){
        SimpleFolderKeyBaseConditionByUuid folderCondition = SimpleFolderKeyBaseConditionByUuid.of(parentFolderUuid, FolderStatus.ACTIVE, lastFolderUuid, limit);

        List<SimpleFolderResponse> list = selectSimpleFolderKeyBaseByUuid(folderCondition);
        Boolean hasNext = false;
        UUID newLastFolderUuid = null;
        if(list.size() > 0) {
            hasNext = list.size() > 0 ? folderMapper.selectSimpleFolderKeyBaseHasNextByUuid(folderCondition) : false;
            newLastFolderUuid = list.get(list.size() - 1).getUuid();
        }
        SimpleFolderListKeysetResponse response = new SimpleFolderListKeysetResponse(list, hasNext, newLastFolderUuid);
        return response;
    }

    /** 사용자가 클릭한 폴더의 UUID를 기준으로 그 폴더의 내용물 (서브폴더와 파일)을 함께 조회한다 */
    public SimpleFolderEntriesListOffsetResponse getSubFolderEntriesOffsetBaseByUuid(Integer userId, UUID parentFolderUuid, Integer offset, Integer limit){
        SimpleFolderOffsetConditionByUuid folderCondition = SimpleFolderOffsetConditionByUuid.of(parentFolderUuid, FolderStatus.ACTIVE, offset, limit);

        // 조회하려는 위치가 폴더 전체 개수보다 많은지 확인한다.
        int currentStartCount = (offset) * limit;
        int totalFolderCount = folderMapper.selectSimpleFolderTotalCountByUuid(folderCondition);

        int currentFolderMax = 0;

        List<SimpleFolderResponse> folders = null;
        if(totalFolderCount >= currentStartCount){
            folders = selectSimpleFolderOffsetByUuid(folderCondition);
            currentFolderMax = (offset) * limit + folders.size();
        } else {
            currentFolderMax = totalFolderCount;
            // 폴더 조회 후 파일
        }

        SimpleFolderEntriesListOffsetResponse response = new SimpleFolderEntriesListOffsetResponse();
        response.setFolders(folders);
        response.setFolderTotal(totalFolderCount);
        response.setCurrentFolderMax(currentFolderMax);

        return response;

    }

    /** 폴더 상세 조회 */
    public FolderDetailsResponse getFolderDetails(Integer userId, UUID folderUuid){
        FolderDetailsDraft draft = folderMapper.selectFolderDetails(folderUuid).orElseThrow(
                () -> new GeneralCustomException(ApiErrorCode.NOT_FOUND, "Target folder not found")
        );
        User createUser = userResolverService.getUserByUserId(draft.getCreateUserId()).orElseThrow(() -> new GeneralCustomException(ApiErrorCode.NOT_FOUND, "TARGET USER NOT FOUND"));
        // 일단은, 신규생성하고 아무것도 안건드렸으면 수정자 없음
        Optional<User> modifyUser = userResolverService.getUserByUserId(draft.getModifyUserId());

        String resourcScopeName = null;
        if(draft.getFolderType().equals(FolderType.PRIVATE)){
            User ownerUser = userResolverService.getUserByUserId(draft.getResourceScopeUserId()).orElse(null);
            resourcScopeName = ownerUser.getName();
        } else {
            Team team = teamResolverService.getTeambyTeamId(draft.getResourceScopeTeamId());
            resourcScopeName = team.getName();
        }

        return FolderDetailsResponse.from(draft, resourcScopeName, createUser.getName(), modifyUser.map(User::getName).orElse(null));
    }

    @Transactional
    /** 폴더 메타정보 수정 */
    public SimpleFolderResponse modifyFolder(Integer userId, UUID targetFolderUuid, FolderModifyRequest request){
        FolderModifyCommand command = FolderModifyCommand.builder()
                .folderUuid(targetFolderUuid)
                .name(request.getName())
                .build();
        int affectedRow = folderMapper.updateFolder(command);

        if(affectedRow == 0){
            throw new GeneralCustomException(ApiErrorCode.NOT_FOUND, "Target folder not found");
        } else if(affectedRow > 1){
            throw new GeneralCustomException(ApiErrorCode.INTERNAL_ERROR, "More than one folder affected");
        }

        SimpleFolderResponse resultFolder = selectSimpleFolderByFolderUuid(targetFolderUuid);

        return resultFolder;
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
    public SimpleFolderResponse moveFolder(Integer userId, UUID targetFolderUuid, FolderMoveRequest request){
        Folder targetFolder = selectFolderByUuid(targetFolderUuid);
        Folder destinationFolder = selectFolderByUuid(request.getDestinationFolderUuid());

        // 폴더 종류 변경 불가
        if(targetFolder.getFolderType() != destinationFolder.getFolderType()){
            throw new GeneralCustomException(ApiErrorCode.BAD_REQUEST, "FOLDER TYPE MISMATCH");
        }

        // Owner 변경불가
        // Team FolderType + Owner 변경 = 폴더 타 팀으로 인계
        // Private FolderType + Owner 변경 = 타 유저에게 폴더 소유권 이전
        if(targetFolder.getFolderType() != destinationFolder.getFolderType()){
            throw new GeneralCustomException(ApiErrorCode.BAD_REQUEST, "FOLDER OWNER CAN NOT BE CHANGED");
        }

        SubFolderMoveCommand command = SubFolderMoveCommand.of(targetFolder.getFolderId(), destinationFolder.getFolderId(), userId);

        int affectedRow = folderMapper.updateFolderParent(command);

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

        return selectSimpleFolderByFolderUuid(targetFolderUuid);
    }

    @Transactional
    public boolean softDeleteFolder(Integer userId, UUID folderUuid){
        int affectedRows = folderMapper.updateFolderStatusAsDelete(folderUuid, FolderStatus.DELETED);

        if(affectedRows == 0){
            throw new GeneralCustomException(ApiErrorCode.NOT_FOUND, "Target folder not found");
        } else if (affectedRows > 1){
            throw new GeneralCustomException(ApiErrorCode.INTERNAL_ERROR, "More than one row affected");
        } else {
            return true;
        }
    }

    /**
     * Offset base로 폴더 조회 헬퍼메서드. -> 이 경우 메인 컨텐츠 뷰 offset base 조회에 사용. Entries 와 함께 사용해야 할듯
     * ROOT List, Sub Folder 리스트 구분없이 사용 가능.
     *
     * */
    public List<SimpleFolderResponse> selectSimpleFolderOffsetById(SimplePrivateFolderOffsetConditionByFolderId condition){
        return folderMapper.selectSimpleFolderOffsetById(condition);
    }

    public List<SimpleFolderResponse> selectSimpleFolderOffsetByUuid(SimpleFolderOffsetConditionByUuid condition){
        return folderMapper.selectSimpleFolderOffsetByUuid(condition);
    }

    /**
     * Keyset base로 폴더 조회 메서드 -> 이경우 좌측 트리 무한스크롤/더보기 에 사용
     * ROOT List, Subfolder 리스트 구분 없이 사용 가능.
     * */

    public List<SimpleFolderResponse> selectSimpleFolderKeyBaseById(SimpleFolderKeyBaseConditionByFolderId condition){
        return folderMapper.selectSimpleFolderKeyBaseById(condition);
    }

    public List<SimpleFolderResponse> selectSimpleFolderKeyBaseByUuid(SimpleFolderKeyBaseConditionByUuid condition){
        return folderMapper.selectSimpleFolderKeyBaseByUuid(condition);
    }

    public Folder selectFolderByUuid(UUID folderUuid){
        return folderMapper.selectFolderByUuid(folderUuid).orElseThrow(
                () -> new GeneralCustomException(ApiErrorCode.NOT_FOUND, "FOLDER NOT FOUND")
        );
    }

    /** 폴더 UUID로 폴더 조회*/
    public SimpleFolderResponse selectSimpleFolderByFolderUuid(UUID folderUuid){
        return folderMapper.selectSimpleFolderByFolderUuid(folderUuid).orElseThrow(
                () -> new GeneralCustomException(ApiErrorCode.NOT_FOUND, "FOLDER NOT FOUND")
        );
    }

    public SimpleFolderResponse selectSimpleFolderByFolderId(Long folderId){
        return folderMapper.selectSimpleFolderByFolderId(folderId).orElseThrow(
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


}
