package com.uruz.frostbite.service;

import com.uruz.frostbite.Tested;
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
import com.uruz.frostbite.enums.FileStatus;
import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
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
    @Tested("20260824")
    @Transactional
    public SimpleFolder createPrivateRootFolder(Integer userId, String folderName) throws RequestProcessingException{
        // TODO 개인 폴더 내 폴더 생성 권한 판정 책임을 PermissionResolverService로 통일할지 검토 필요.
        // 현 시점, 암묵적 보장
        Folder privateEntryFolder = folderMapper.selectPrivateEntryFolderByUserId(userId)
                .orElseThrow(() -> new RequestProcessingException(ApiErrorCode.INTERNAL_DATA_INCONSISTENCY_FOLDER_RESOURCE));

        Folder newFolder = Folder.createPrivateRootFolder(
                folderName, privateEntryFolder.getFolderId(), privateEntryFolder.getCreateUserId()
        );

        folderMapper.insertRootFolder(newFolder);
        aclService.createPrivateDefaultFolderAcl(newFolder.getFolderId(), userId);
        return selectSimpleFolderByFolderId(newFolder.getFolderId());
    }

    @Tested("20260825")
    // KeySetBase 개인 folder List 조회 (좌측 트리용)
    public CursorBaseListResponse<SimpleFolderTreeNode> getPrivateFoldersByCursorBase(Integer userId, UUID parentFolderUuid, UUID startFolderUuid, Integer limit){

        Folder parentFolder = selectPrivateParentFolder(userId, parentFolderUuid);
        // 접근 가능 확인 포함 메서드로 전환

        PrivateFolderCursorBaseConditionByUuid folderCondition = PrivateFolderCursorBaseConditionByUuid.of(
                userId, parentFolder.getFolderId(), startFolderUuid, FolderStatus.ACTIVE, limit);

        List<SimpleFolderTreeNode> list = folderMapper.selectPrivateFolderCursorBaseByUuid(folderCondition);

        UUID lastUuid = null;
        UUID nextUuid = null;
        boolean hasNext = false;

        if(!list.isEmpty()) {
            lastUuid = list.getLast().getUuid();
            PrivateFolderCursorBaseConditionByUuid nextUuidCondition = folderCondition.nextFolderUuidCondition(lastUuid);
            nextUuid = folderMapper.selectPrivateFolderCursorBaseNext(nextUuidCondition).orElse(null);
            hasNext = nextUuid != null;
        }


        CursorBasePagination cursorBasePagination = new CursorBasePagination(lastUuid, nextUuid, hasNext, limit);
        return new CursorBaseListResponse<>(list, cursorBasePagination);
    }


    // 개인폴더 컨텐츠 조회
    @Tested("20260824")
    public OffsetBaseListResponse<FolderContents> getPrivateFolderContentsByOffsetBase(Integer userId, UUID parentFolderUuid, int offset, int limit){

        Folder parentFolder = selectPrivateParentFolder(userId, parentFolderUuid);

        int totalCount = folderMapper.selectPrivateFolderContentsTotalCount(userId,parentFolder.getFolderId(), FolderStatus.ACTIVE, FileStatus.ACTIVE);
        List<FolderContents> folderContentsList = folderMapper.selectPrivateFolderContentsList(userId, parentFolder.getFolderId(), offset, limit, FolderStatus.ACTIVE, FileStatus.ACTIVE);
        OffsetBasePagination offsetBasePagination = new OffsetBasePagination(totalCount, offset, limit);

        return new OffsetBaseListResponse<>(folderContentsList, offsetBasePagination);
    }

    @Tested("20260824")
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

    /** 팀 폴더 */
    // 팀 UUID로부터 루트 폴더 생성
    // 직전 메서드 개선안
    // 팀 접근권한 조회 -> EntryFolder 조회 라는 기존의 흐름이 "권한감사 + 자원조회 동시에" 라는 목적을 이루지 못하고있음을 개선한다.
    // 기존의 팀 관계 확인 -> 팀 엔트리폴더 조회 를 하나의 절차로 합쳐 -> "접근할 수 있는 팀 엔트리폴더가 있는가" 로 전환한다.
    // 대신 이경우엔 팀을 못찾은건지, 유효한 Entry folder를 못찾은건지 알수 없게되므로 -> 더욱 캡슐화된 NotFound 에러를 사용한다.
    @Transactional
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

    public Folder selectTeamParentFolder(UUID parentFolderUuid, UUID teamUuid, Integer userId) throws RequestProcessingException {
        Folder parentFolder = null;

        if(parentFolderUuid == null) {
            // 쿼리스트링에 부모 UUID 없음 = Entry 폴더
            parentFolder = folderMapper.selectAccessibleTeamEntryFolderByTeamUuid(teamUuid, userId).orElseThrow(
                    () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
            );
        } else {
            // 쿼리스트링에 부모 UUID 있음 -> 특정 폴더 하위 조회
            TeamFolderCondition condition = TeamFolderCondition.fromTeamUuidWithUserId(teamUuid, userId)
                    .withFolderUuid(parentFolderUuid);
            parentFolder = folderMapper.selectAccessibleTeamFolder(condition).orElseThrow(
                    () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
            );
        }

        return parentFolder;
    }

    // 팀 폴더 컨텐츠 조회
    public OffsetBaseListResponse<FolderContents> getTeamFolderContentsByOffsetBase(Integer userId, UUID parentFolderUuid, UUID teamUuid, Integer offset, Integer limit){
        Folder parentFolder = selectTeamParentFolder(parentFolderUuid, teamUuid, userId);

        int totalCount = folderMapper.selectTeamFolderContentsTotalCount(teamUuid, parentFolder.getFolderId(), userId, FolderStatus.ACTIVE, FileStatus.ACTIVE);

        List<FolderContents> folderContentsList = folderMapper.selectTeamFolderContentsList(teamUuid, parentFolder.getFolderId(), userId, offset, limit, FolderStatus.ACTIVE, FileStatus.ACTIVE);
        OffsetBasePagination offsetBasePagination = new OffsetBasePagination(totalCount, offset, limit);

        return new OffsetBaseListResponse<>(folderContentsList, offsetBasePagination);
    }

    // Team 좌측 트리용 커서베이스 조회
    public CursorBaseListResponse<SimpleFolderTreeNode> getTeamFolderByCursorBase(Integer userId, UUID teamUuid, UUID parentFolderUuid, UUID nextFolderUuid, Integer limit){
        Folder parentFolder = selectTeamParentFolder(userId, parentFolderUuid, teamUuid);

        TeamFolderCursorBaseConditionByFolderUuid folderCondition = TeamFolderCursorBaseConditionByFolderUuid.ofTeamUuid(
                teamUuid, userId, parentFolder.getFolderId(), nextFolderUuid, FolderStatus.ACTIVE, limit
        );

        List<SimpleFolderTreeNode> list = folderMapper.selectTeamFolderCursorBaseByUuid(folderCondition);

        UUID lastUuid = null;
        if(!list.isEmpty()) list.getLast().getUuid();
        TeamFolderCursorBaseConditionByFolderUuid nextUuidCondition = folderCondition.nextFolderUuidCondition(lastUuid);
        UUID nextUuid = folderMapper.selectTeamFolderCursorBaseNext(nextUuidCondition).orElse(null);
        boolean hasNext = nextUuid != null;

        CursorBasePagination cursorBasePagination = new CursorBasePagination(lastUuid, nextUuid, hasNext, limit);
        return new CursorBaseListResponse<SimpleFolderTreeNode>(list, cursorBasePagination);
    }


    /** 개인폴더 팀폴더 공용 */
    // 부모폴더 UUID로 부터 서브폴더 생성 (root 계층 요청은 여기서 지원 안함.)
    @Transactional
    public SimpleFolder createSubFolder(Integer userId, FolderCreateRequest request) {
        Folder parentFolder = folderMapper.selectAccessibleFolderByParentFolderUuid(request.getParentFolderUuid(), userId).orElseThrow(
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
        Folder targetFolder = folderMapper.selectAccessibleFolderByFolderUuid(targetFolderUuid, userId).orElseThrow(
                () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
        );
        Folder destinationFolder = folderMapper.selectAccessibleFolderByFolderUuid(request.getDestinationFolderUuid(), userId).orElseThrow(
                () -> new RequestProcessingException(ApiErrorCode.NOT_FOUND)
        );

        // 타겟폴더와
        // 폴더 종류 변경 불가
        if(targetFolder.getFolderType() != destinationFolder.getFolderType()){
            throw new GeneralCustomException(ApiErrorCode.BAD_REQUEST, "FOLDER TYPE MISMATCH");
        }
        SubFolderMoveCommand command = SubFolderMoveCommand.of(targetFolder.getFolderId(), destinationFolder.getFolderId(), userId);

        int affectedRow = folderMapper.updateFolderParent(command);
        return selectSimpleFolderByFolderUuid(targetFolderUuid);
    }

    // 폴더 삭제
    public boolean softDeleteFolder(Integer userId, UUID folderUuid){
        return folderMapper.updateFolderStatusAsDelete(folderUuid, FolderStatus.DELETED) == 1 ? true : false;
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



}
