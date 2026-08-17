package com.uruz.frostbite.service;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.customException.RequestProcessingException;
import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.domain.Team;
import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.common.paging.CursorBasePagination;
import com.uruz.frostbite.dto.common.paging.OffsetBasePagination;
import com.uruz.frostbite.dto.common.response.CursorBaseListResponse;
import com.uruz.frostbite.dto.common.response.OffsetBaseListResponse;
import com.uruz.frostbite.dto.entry.FolderContents;
import com.uruz.frostbite.dto.folder.*;
import com.uruz.frostbite.dto.folder.internal.*;
import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
import com.uruz.frostbite.enums.PermissionBitMask;
import com.uruz.frostbite.enums.common.ApiErrorCode;
import com.uruz.frostbite.mapper.FolderMapper;
import com.uruz.frostbite.service.resolver.AccessControlResolverService;
import com.uruz.frostbite.service.resolver.TeamResolverService;
import com.uruz.frostbite.service.resolver.UserResolverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FolderService {
    /** folder 도메인에서 폴더 정보를 조작하고 관리 및 조회 할 때 사용하는 서비스 */

    // TODO 조회 시, 부모폴더ID를 가져온 다음의 쿼리에도 resource_scope를 검새햐아 하는가?
    //    1. 부모폴더ID가 Resource_scope 정보를 가지고 있어서, 이미 조회해 왔다면 그 scope 내의 객체인것인데, SQL 조회마다 scope 검사를 할 필요가 있는지 고민할 필요가 있다.
    private final FolderMapper folderMapper;
    private final AclService aclService;

    private final UserResolverService userResolverService;
    private final TeamResolverService teamResolverService;
    private final AccessControlResolverService accessControlResolverService;

    @Autowired
    public FolderService(FolderMapper folderMapper, AclService aclService, UserResolverService userResolverService,
                         TeamResolverService teamResolverService,
                         AccessControlResolverService accessControlResolverService){
        this.folderMapper = folderMapper;
        this.aclService = aclService;
        this.userResolverService = userResolverService;
        this.teamResolverService = teamResolverService;
        this.accessControlResolverService = accessControlResolverService;
    }


    /** 개인폴더 */
    // 사용자 UUID로 부터 루트폴더 생성
    @Transactional
    public SimpleFolder createPrivateRootFolder(Integer userId, String folderName) throws RequestProcessingException{
        // 사용자 UUID를 통한 개인 엔트리폴더 조회
        Folder privateEntryFolder = folderMapper.selectPrivateEntryFolderByUserId(userId)
                .orElseThrow(() -> new RequestProcessingException(ApiErrorCode.INTERNAL_DATA_INCONSISTENCY_FOLDER_RESOURCE));

        Folder newFolder = Folder.createPrivateRootFolder(
                folderName, privateEntryFolder.getFolderId(), privateEntryFolder.getCreateUserId()
        );

        folderMapper.insertRootFolder(newFolder);
        aclService.createPrivateDefaultFolderAcl(newFolder.getFolderId(), userId);
        return selectSimpleFolderByFolderId(newFolder.getFolderId());
    }

    /** 팀 폴더 */
    // 팀 UUID로부터 루트 폴더 생성
    @Deprecated
    public SimpleFolder _createTeamRootFolder(Integer userId, TeamRootFolderCreateRequest request){

        // 팀에 접근권한이 있는 팀을 조회해온다.
        Team team = teamResolverService.selectAccessibleTeamByTeamUuid(userId, request.getTeamUuid()).orElseThrow(
                () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
        );

        // TEAM UUID를 통한 team EntryFolder 조회
        Folder teamEntryFolder = folderMapper._selectTeamEntryFolderByTeamId(team.getTeamId()).orElseThrow(
                () -> new RequestProcessingException(ApiErrorCode.INTERNAL_DATA_INCONSISTENCY_FOLDER_RESOURCE)
        );

        Folder newFolder = Folder.createTeamRootFolder(request.getName(), teamEntryFolder.getFolderId(),
                teamEntryFolder.getResourceScopeTeamId(), userId);

        folderMapper.insertRootFolder(newFolder);
        aclService.createTeamDefaultFolderAcl(newFolder.getFolderId(), teamEntryFolder.getResourceScopeTeamId(), userId);
        return selectSimpleFolderByFolderId(newFolder.getFolderId());
    }
    // 직전 메서드 개선안
    // 팀 접근권한 조회 -> EntryFolder 조회 라는 기존의 흐름이 "권한감사 + 자원조회 동시에" 라는 목적을 이루지 못하고있음을 개선한다.
    // 기존의 팀 관계 확인 -> 팀 엔트리폴더 조회 를 하나의 절차로 합쳐 -> "접근할 수 있는 팀 엔트리폴더가 있는가" 로 전환한다.
    // 대신 이경우엔 팀을 못찾은건지, 유효한 Entry folder를 못찾은건지 알수 없게되므로 -> 더욱 캡슐화된 NotFound 에러를 사용한다.
    public SimpleFolder createTeamRootFolder(Integer userId, TeamRootFolderCreateRequest request){
        // TEAM UUID를 통한 team EntryFolder 조회
        Folder teamEntryFolder = folderMapper.selectAccessibleTeamEntryFolderByTeamUuid(request.getTeamUuid(), userId).orElseThrow(
                () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
        );

        Folder newFolder = Folder.createTeamRootFolder(request.getName(), teamEntryFolder.getFolderId(),
                teamEntryFolder.getResourceScopeTeamId(), userId);

        folderMapper.insertRootFolder(newFolder);
        aclService.createTeamDefaultFolderAcl(newFolder.getFolderId(), teamEntryFolder.getResourceScopeTeamId(), userId);
        return selectSimpleFolderByFolderId(newFolder.getFolderId());
    }

    /** 개인폴더 팀폴더 공용 */
    // 부모폴더 UUID로 부터 서브폴더 생성
    @Transactional
    public SimpleFolder createSubFolder(Integer userId, FolderCreateRequest request) {
        Folder parentFolder = folderMapper.selectFolderByUuid(request.getParentFolderUuid()).orElseThrow(
                () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
        );
        // 폴더타입은 이미 Context 상 부모폴더에 있음.
        FolderType folderType = parentFolder.getFolderType();
        Folder newFolder = null;
        if(folderType.equals(FolderType.PRIVATE)){
            newFolder = Folder.createPrivateSubFolder(request.getFolderName(), parentFolder.getFolderId(), userId);
        } else {
            newFolder = Folder.createTeamSubFolder(request.getFolderName(), parentFolder.getFolderId(), parentFolder.getResourceScopeTeamId(),userId);
        }

        folderMapper.insertSubFolder(newFolder);
        aclService.createSubFolderAcl(parentFolder.getFolderId(), newFolder.getFolderId(), userId);
        return selectSimpleFolderByFolderId(newFolder.getFolderId());
    }

    // parentFolderUuid로 부터 부모폴더 조회용 헬퍼 메서드
    public Folder selectPrivateParentFolder(Integer userId, UUID parentFolderUuid) throws RequestProcessingException {
        Folder parentFolder = null;

        if(parentFolderUuid == null) {
            // 쿼리스트링에 부모 UUID 없음 = Entry 폴더
            parentFolder = folderMapper.selectPrivateEntryFolderByUserId(userId).orElseThrow(
                    () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
            );
        } else {
            // 쿼리스트링에 부모 UUID 있음 -> 특정 폴더 하위 조회
            PrivateFolderCondition condition = PrivateFolderCondition.fromUserId(userId)
                    .withFolderUuid(parentFolderUuid);
            parentFolder = folderMapper.selectAccessiblePrivateFolder(condition).orElseThrow(
                    () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
            );
        }

        return parentFolder;
    }

    // KeySetBase 개인 folder List 조회 (좌측 트리용)
    public CursorBaseListResponse<SimpleFolderTreeNode> getPrivateFoldersByCursorBase(Integer userId, UUID parentFolderUuid, UUID startFolderUuid, Integer limit){

        Folder parentFolder = selectPrivateParentFolder(userId, parentFolderUuid);

        PrivateFolderCursorBaseConditionByUuid folderCondition = PrivateFolderCursorBaseConditionByUuid.of(
                userId, parentFolder.getFolderId(), startFolderUuid, FolderStatus.ACTIVE, limit);

        List<SimpleFolderTreeNode> list = folderMapper.selectPrivateFolderCursorBaseByUuid(folderCondition);

        UUID lastUuid = null;
        if(!list.isEmpty()) lastUuid = list.getLast().getUuid();
        PrivateFolderCursorBaseConditionByUuid nextUuidCondition = folderCondition.nextFolderUuidCondition(lastUuid);
        UUID nextUuid = folderMapper.selectPrivateFolderCursorBaseNext(nextUuidCondition).orElse(null);
        boolean hasNext = nextUuid != null;

        CursorBasePagination cursorBasePagination = new CursorBasePagination(lastUuid, nextUuid, hasNext, limit);
        return new CursorBaseListResponse<SimpleFolderTreeNode>(list, cursorBasePagination);
    }


    // 개인폴더 컨텐츠 조회
    public OffsetBaseListResponse<FolderContents> getPrivateFolderContentsByOffsetBase(Integer userId, UUID parentFolderUuid, int offset, int limit){

        Folder parentFolder = selectPrivateParentFolder(userId, parentFolderUuid);

        int totalCount = folderMapper.selectPrivateFolderContentsTotalCount(userId,parentFolder.getFolderId());
        List<FolderContents> folderContentsList = folderMapper.selectPrivateFolderContentsList(userId, parentFolder.getFolderId(), offset, limit);
        OffsetBasePagination offsetBasePagination = new OffsetBasePagination(totalCount, offset, limit);

        return new OffsetBaseListResponse<>(folderContentsList, offsetBasePagination);
    }

    // parentFolderUuid로 부터 부모폴더 조회용 헬퍼 메서드
    public Folder selectTeamParentFolder(Integer userId, UUID parentFolderUuid, UUID teamUuid) throws RequestProcessingException {

        Folder parentFolder = null;
        if(parentFolderUuid == null){
            // parentFolderUuid가 없을경우 Entry Folder가 부모
            parentFolder = folderMapper.selectAccessibleTeamEntryFolderByTeamUuid(teamUuid, userId).orElseThrow(
                    () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND));
        } else {
            // parentFolderUuid가 있을경우 해당 팀 폴더에 접근이 가능한지 조회 필요
            // 팀에 접근권한이 있는 팀을 조회해온다.
            Team team = teamResolverService.selectAccessibleTeamByTeamUuid(userId, teamUuid).orElseThrow(
                    () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
            );
            // Read 이상의 권한을 가지고 있어야 한다. -> PermissionBit와도 엮임
            // TODO privateFolder도 PermissionBit 검사해야함
            // ACL 권한감사는 조회 SQL로 압축하지 않는다.
                /* 단순 관계/소속 여부는 SQL JOIN/Exists로 처리한다. -> 해당 자원이 특정 사용자/팀 Scope 내에 있는가
                   그러나 "허용/거부" 등의 접근정책과 행위허가 등 도메인 정책을 판정해야 하는 순간 SQL 외부로 꺼내 ResolverService를 활용한다.
                   도메인 정책 판정, 허용판정 등이 SQL 내부로 숨어서 캡슐화되면 오히려 유지보수가 힘들어진다.
                */
            TeamFolderCondition teamFolderCondition = TeamFolderCondition.fromTeamIdWithUserId(team.getTeamId(), userId)
                    .withFolderUuid(parentFolderUuid);

            parentFolder = folderMapper.selectAccessibleTeamFolder(teamFolderCondition).orElseThrow(
                    () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
            );
        }

        return parentFolder;
    }

    // 팀 폴더 컨텐츠 조회
    public OffsetBaseListResponse<FolderContents> getTeamFolderContentsByOffsetBase(Integer userId, UUID parentFolderUuid, UUID teamUuid, Integer offset, Integer limit){
        Team team = teamResolverService.selectAccessibleTeamByTeamUuid(userId, teamUuid).orElseThrow(
                () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
        );

        Folder parentFolder = selectTeamParentFolder(userId, parentFolderUuid, teamUuid);

        SimpleTeamFolderOffsetConditionByFolderId folderCondition = SimpleTeamFolderOffsetConditionByFolderId.of(
                team.getTeamId(), userId, parentFolder.getFolderId(), FolderStatus.ACTIVE, offset, limit
        );

        int totalCount = folderMapper.selectTeamFolderContentsTotalCount(folderCondition);

        List<FolderContents> folderContentsList = folderMapper.selectTeamFolderContentsList(folderCondition);
        OffsetBasePagination offsetBasePagination = new OffsetBasePagination(totalCount, offset, limit);

        return new OffsetBaseListResponse<>(folderContentsList, offsetBasePagination);
    }

    // Team 좌측 트리용 커서베이스 조회
    public CursorBaseListResponse<SimpleFolderTreeNode> getTeamFolderByCursorBase(Integer userId, UUID teamUuid, UUID parentFolderUuid, UUID nextFolderUuid, Integer limit){
        Team team = teamResolverService.selectAccessibleTeamByTeamUuid(userId, teamUuid).orElseThrow(
                () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
        );

        Folder parentFolder = selectTeamParentFolder(userId, parentFolderUuid, teamUuid);

        TeamFolderCursorBaseConditionByUuid folderCondition = TeamFolderCursorBaseConditionByUuid.of(
                team.getTeamId(), userId, parentFolder.getFolderId(), nextFolderUuid, FolderStatus.ACTIVE, limit
        );

        List<SimpleFolderTreeNode> list = folderMapper.selectSimpleTeamFolderCursorBaseByUuid(folderCondition);

        UUID lastUuid = null;
        if(!list.isEmpty()) list.getLast().getUuid();
        TeamFolderCursorBaseConditionByUuid nextUuidCondition = folderCondition.nextFolderUuidCondition(lastUuid);
        UUID nextUuid = folderMapper.selectSimpleTeamFolderCursorBaseNext(nextUuidCondition).orElse(null);
        boolean hasNext = nextUuid != null;

        CursorBasePagination cursorBasePagination = new CursorBasePagination(lastUuid, nextUuid, hasNext, limit);
        return new CursorBaseListResponse<SimpleFolderTreeNode>(list, cursorBasePagination);
    }

    /** 서브폴더 Keyset base 리스트 by uuid
     * -> keysetBase는 좌측트리로만 출력하므로, Entries(folder+file) 일 필요 없다.
     *
     */

    @Deprecated
    public SimpleFolderListKeysetResponse getSubFoldersByKeysetBaseByUuid(Integer userId, UUID parentFolderUuid, UUID nextFolderUuid, Integer limit){
        PrivateFolderCursorBaseConditionByUuid folderCondition = null;// PrivateFolderCursorBaseConditionByUuid.of(userId, parentFolderUuid, nextFolderUuid, FolderStatus.ACTIVE, limit);

        List<SimpleFolder> list = null; //selectSimpleFolderKeyBaseByUuid(folderCondition);
        Boolean hasNext = false;
        UUID newLastFolderUuid = null;
        if(list.size() > 0) {
            hasNext = list.size() > 0 ? folderMapper.selectSimpleFolderKeyBaseHasNextByUuid(folderCondition) : false;
            newLastFolderUuid = list.get(list.size() - 1).getUuid();
        }
        SimpleFolderListKeysetResponse response = new SimpleFolderListKeysetResponse(list, hasNext, newLastFolderUuid);
        return response;
    }

    @Deprecated
    /** 사용자가 클릭한 폴더의 UUID를 기준으로 그 폴더의 내용물 (서브폴더와 파일)을 함께 조회한다 */
    public SimpleFolderEntriesListOffsetResponse getSubFolderEntriesOffsetBaseByUuid(Integer userId, UUID parentFolderUuid, Integer offset, Integer limit){
        SimplePrivateFolderOffsetConditionByUuid folderCondition = SimplePrivateFolderOffsetConditionByUuid.of(parentFolderUuid, FolderStatus.ACTIVE, userId, offset, limit);

        // 조회하려는 위치가 폴더 전체 개수보다 많은지 확인한다.
        int currentStartCount = (offset) * limit;
        int totalFolderCount = folderMapper.selectSimpleFolderTotalCountByUuid(folderCondition);

        int currentFolderMax = 0;

        List<SimpleFolder> folders = null;
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

    /** 폴더 메타정보 수정 */
    public SimpleFolder modifyFolder(Integer userId, UUID targetFolderUuid, FolderModifyRequest request){
        FolderModifyCommand command = FolderModifyCommand.builder()
                .folderUuid(targetFolderUuid)
                .folderName(request.getFolderName())
                .build();
        int affectedRow = folderMapper.updateFolder(command);

        SimpleFolder resultFolder = selectSimpleFolderByFolderUuid(targetFolderUuid);

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
    public SimpleFolder moveFolder(Integer userId, UUID targetFolderUuid, FolderMoveRequest request){
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

        // TODO 현재는 Mybatis 레벨에서 concat(||) 로 폴더경로를 만들고있다.
        // 이걸 JAVA 레벨로 끌어올려야 한다. 비즈니스 로직 (폴더 이동에 따른 경로계산) 이 SQL 레이어에 숨는다는 유지보수 어려움과
        // XML이 복잡해지는것을 방지하기 위함
        String oldFolderIdpath = targetFolder.getFolderIdPath();
        String oldFolderNamePath = targetFolder.getFolderNamePath();
        String newFolderIdPath = destinationFolder.getFolderIdPath() + targetFolder.getFolderId() + "/";
        String newFolderNamePath = destinationFolder.getFolderNamePath() + targetFolder.getFolderName() + "/";
        MovedSubFolderPathCommand pathCommand = new MovedSubFolderPathCommand(
                oldFolderIdpath, oldFolderNamePath, newFolderIdPath, newFolderNamePath
        );

        int pathAffectedRow = folderMapper.updateSubFolderPath(pathCommand);

        return selectSimpleFolderByFolderUuid(targetFolderUuid);
    }

    public boolean softDeleteFolder(Integer userId, UUID folderUuid){
        return folderMapper.updateFolderStatusAsDelete(folderUuid, FolderStatus.DELETED) == 1 ? true : false;
    }

    /** 폴더 조회 시 ACL 조회 */
    // 폴더 진입시 (폴더 조회시) -> 폴더 조회 이상의 권한이 있는지 확인해야함
    // 이건 keyset folder list 도, folder entries도 마찬가지임

    // 1. 폴더 조회 진입 -> 폴더 정보 조회
    // 2. 사용자가 진입하려는 폴더의 scope에 정말 사용자가 속해있는지 확인 필요
    // 3-1. 개인폴더일 경우, USER에게 deny가 떨어질 수 없음. ACL도 ALLOW ALL, PermissinoBitmask 도 all 권한임. 검증 종료 -> 조회 허용
    // 3-2. 팀 폴더일 경우 (4부터 시작)
    // 4. 사용자가 해당 팀의 ^Manager Role인지 검증, 맞다면 ACL 무시하고 동작하기로 했으므로 통과 -> 조회 허용
    // 5. 사용자가 해당 폴더에 Deny ACL이 있는지 검증. 있다면 검증 종료, -> 조회 거절
    // 6. 사용자가 해당 폴더에 ALLOW ACL이 있다면, permissionBitMask MAX()로 가져올것. (더 큰 권한을 판정에 사용하기로 했음)
    // 7. 현재 수행하려는 동작이 ACL의 permissionBit에 포함된 상태인지 검증 -> 포함되었다면 조회 허용
    // 8. 포함되어있지 않거나, ACL이 조회되지 않는다면 -> 조회 거절
    public boolean validateUserActionPermissionForFolder(Integer userId, Long folderId, PermissionBitMask bitMask) throws GeneralCustomException{

        // 허용일경우 true 리턴, 허용되지 않을경우 throw
        return accessControlResolverService.validateUserActionPermissionForFolder(userId, folderId, bitMask);

    }

    /**
     * External Call
     * */
    // 부모메서드 transactional에 참여
    @Transactional(propagation = Propagation.REQUIRED)
    /** 최초 사용자 가입 시 개인 엔트리 폴더 할당 */
    public void createPrivateEntryFolder(String initialFolderName, Integer userId){
        // 최초가입시 자동 개인폴더 root 할당. 이경우 일반 폴더생성과 관심사 흐름이 다름 (폴더생성 =/= 자동루트폴더생성)
        // 따라서 별도 메서드로 처리

        // transactional 걸려있고 컨트롤러 공개가 안되는 메서드, userId는 정상으로 처리되었을것이라 신뢰하고 앱 레벨 정합성검사 생략
        Folder initialPrivateEntryFolder = Folder.createPrivateEntryFolder(initialFolderName, userId);
        folderMapper.insertEntryFolder(initialPrivateEntryFolder);

        // TODO : 폴더 생성 후 ACL 제어 추가 필요
        aclService.createPrivateDefaultFolderAcl(initialPrivateEntryFolder.getFolderId(), userId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    /** 최초 사용자 가입 시 팀 엔트리 폴더 할당 */
    public void createTeamEntryFolder(String initialFolderName, Integer teamId, Integer createUserId){
        // 최초가입시 자동 개인폴더 root 할당. 이경우 일반 폴더생성과 관심사 흐름이 다름 (폴더생성 =/= 자동루트폴더생성)
        // 따라서 별도 메서드로 처리

        // transactional 걸려있고 컨트롤러 공개가 안되는 메서드, userId는 정상으로 처리되었을것이라 신뢰하고 앱 레벨 정합성검사 생략
        Folder initialTeamEntryFolder = Folder.createTeamEntryFolder(initialFolderName, teamId, createUserId);
        folderMapper.insertEntryFolder(initialTeamEntryFolder);

        // TODO : 폴더 생성 후 ACL 제어 추가 필요
        aclService.createTeamDefaultFolderAcl(initialTeamEntryFolder.getFolderId(), teamId, createUserId);
    }

    /**
     * Offset base로 폴더 조회 헬퍼메서드. -> 이 경우 메인 컨텐츠 뷰 offset base 조회에 사용. Entries 와 함께 사용해야 할듯
     * ROOT List, Sub Folder 리스트 구분없이 사용 가능.
     *
     * */
    @Deprecated(forRemoval = true)
    public List<SimpleFolder> selectSimpleFolderOffsetByUuid(SimplePrivateFolderOffsetConditionByUuid condition){
        return folderMapper.selectSimpleFolderOffsetByUuid(condition);
    }

    /**
     * Keyset base로 폴더 조회 메서드 -> 이경우 좌측 트리 무한스크롤/더보기 에 사용
     * ROOT List, Subfolder 리스트 구분 없이 사용 가능.
     * */

    @Deprecated(forRemoval = true)
    public List<SimpleFolder> selectSimpleFolderKeyBaseById(SimpleFolderKeyBaseConditionByFolderId condition){
        return folderMapper.selectSimpleFolderKeyBaseById(condition);
    }

    public Folder selectFolderByUuid(UUID folderUuid){
        return folderMapper.selectFolderByUuid(folderUuid).orElseThrow(
                () -> new GeneralCustomException(ApiErrorCode.NOT_FOUND, "FOLDER NOT FOUND")
        );
    }

    /** 폴더 UUID로 폴더 조회*/
    public SimpleFolder selectSimpleFolderByFolderUuid(UUID folderUuid){
        return folderMapper.selectSimpleFolderByFolderUuid(folderUuid).orElseThrow(
                () -> new GeneralCustomException(ApiErrorCode.NOT_FOUND, "FOLDER NOT FOUND")
        );
    }

    public SimpleFolder selectSimpleFolderByFolderId(Long folderId){
        return folderMapper.selectSimpleFolderByFolderId(folderId).orElseThrow(
                () -> new GeneralCustomException(ApiErrorCode.NOT_FOUND, "FOLDER NOT FOUND")
        );
    }




    /** 팀 ENTRY FOLDER 조회*/
    @Deprecated
    public Folder getTeamEntryFolder(TeamEntryFolderCondition condition){
        return folderMapper.selectTeamEntryFolder(condition).orElseThrow(() ->
                new GeneralCustomException(ApiErrorCode.NOT_FOUND, "FOLDER NOT FOUND"));

    }


}
