package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.dto.entry.FolderContents;
import com.uruz.frostbite.dto.folder.SimpleFolder;
import com.uruz.frostbite.dto.folder.SimpleFolderTreeNode;
import com.uruz.frostbite.dto.folder.internal.MovedSubFolderPathCommand;
import com.uruz.frostbite.dto.folder.internal.*;
import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.dto.folder.internal.FolderTargetAccessControlProjection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
@Repository
@Mapper
public interface FolderMapper {

    // 진입 폴더 (Entry 폴더) 생성
    void insertEntryFolder(Folder folder);
    // 1계층 폴더 생성
    void insertRootFolder(Folder newFolder);
    // 차차계층 폴더 생성
    void insertSubFolder(Folder newFolder);




    /** 개인폴더 */

    // 개인 entry 폴더 조회 -> 외부에 노출될일 없음. 시스템이 조회함
    Optional<Folder> selectPrivateEntryFolderByUserId(int userId);

    // 개선안 채택
    @Deprecated(forRemoval = true)
    Optional<Folder> _selectTeamEntryFolderByTeamId(int teamId);

    // 내부에 folder_type = 2 하드코딩되어있음 (유스케이스별 메서드분리목적)
    // 내부에 folder_status = 1 하드코딩되어있음 (살아있는 폴더만 조회) -> 이것도 외부로 꺼낼 수 있는지 조사할것
    Optional<Folder> selectAccessibleTeamEntryFolderByTeamUuid(@Param("teamUuid")UUID teamUuid, @Param("userId") Integer userId);
    // 개인폴더 조회 (시스템관점)
    Optional<Folder> selectFolderByFolderId(Long folderId);
    // 개인폴더 조회 (시스템관점) -> WF / UnitTask측에서 Request Parameter를 통해 UUID로 접근.
    // TODO 현재 오류를 잡기위해 강제로 셋팅, 추후 "UUID는 시스템-사용자 접점, PK는 사용자내부통신" 이라는 컨벤션을 지키기 위해서 이 메서드를 지우고 등록시 PK로 전환해야함
    @Deprecated(forRemoval = true)
    Optional<Folder> selectFolderByUuid(UUID folderUuid);
    // 개인폴더 조회 (FE입력)
    Optional<Folder> selectAccessiblePrivateFolder(PrivateFolderCondition condition);
    // 팀 폴더 조회
    // 개인폴더와 합치기 애매함. target Column 자체가 달라서 Condition도 합쳐져야 하는데, 이경우 privateFolder Condition에 항상 resource_scope_team_id가 따라다니게됨.
    Optional<Folder> selectAccessibleTeamFolder(TeamFolderCondition condition);
    // 개인 폴더 조회 by parent
    //Optional<Folder> selectAccessiblePrivateFolderByParent(PrivateFolderCondition condition);

    /** 부모를 기준으로 자식을 리스팅하는것임 */
    // Cursor 베이스 폴더 조회 (좌측 폴더트리용)
    List<SimpleFolderTreeNode> selectPrivateFolderCursorBaseByUuid(PrivateFolderCursorBaseConditionByUuid condition);
    // 다음 UUID 조회 (Cursor 베이스)
    Optional<UUID> selectPrivateFolderCursorBaseNext(PrivateFolderCursorBaseConditionByUuid condition);

    // 개인폴더 폴더 컨텐츠 전체 개수 조회
    int selectPrivateFolderContentsTotalCount(@Param("userId") Integer userId, @Param("parentFolderId") Long parentFolderId);
    // 개인폴더 컨텐츠 조회
    List<FolderContents> selectPrivateFolderContentsList(@Param("userId") Integer userId, @Param("parentFolderId") Long parentFolderId
            , @Param("offset") Integer offset, @Param("limit") Integer limit);


    // 팀 폴더 컨텐츠 전체 개수 조회
    int selectTeamFolderContentsTotalCount(SimpleTeamFolderOffsetConditionByFolderId condition);
    // 팀 컨텐츠 조회
    List<FolderContents> selectTeamFolderContentsList(SimpleTeamFolderOffsetConditionByFolderId condition);


    // Cursor 베이스 폴더 조회 (좌측 폴더트리용)
    List<SimpleFolderTreeNode> selectSimpleTeamFolderCursorBaseByUuid(TeamFolderCursorBaseConditionByUuid condition);
    // 다음 UUID 조회 (Cursor 베이스)
    Optional<UUID> selectSimpleTeamFolderCursorBaseNext(TeamFolderCursorBaseConditionByUuid condition);



    // 팀 엔트리 폴더 조회
    @Deprecated
    Optional<Folder> selectTeamEntryFolder(TeamEntryFolderCondition condition);

    // offsetById 방식에 PK조회가 필요할까? FE는 PK를 모를텐데
    @Deprecated(forRemoval = true)
    List<SimpleFolder> selectSimpleFolderOffsetById(SimplePrivateFolderOffsetConditionByFolderId condition);
    // UUID 방식으로 폴더조회 <- 필요할까? Main ContentsView는 결국 FolderContents로 조회해갈텐데
    @Deprecated(forRemoval = true)
    List<SimpleFolder> selectSimpleFolderOffsetByUuid(SimplePrivateFolderOffsetConditionByUuid condition);

    @Deprecated(forRemoval = true)
    int selectSimpleFolderTotalCountById(SimplePrivateFolderOffsetConditionByFolderId condition);
    @Deprecated(forRemoval = true)
    int selectSimpleFolderTotalCountByUuid(SimplePrivateFolderOffsetConditionByUuid condition);

    // 무한로딩 방식에 PK 방식이 필요할까? FE는 PK를 모를텐데
    @Deprecated(forRemoval = true)
    List<SimpleFolder> selectSimpleFolderKeyBaseById(SimpleFolderKeyBaseConditionByFolderId condition);


    @Deprecated(forRemoval = true)
    boolean selectSimpleFolderKeyBaseHasNext(SimpleFolderKeyBaseConditionByFolderId condition);
    @Deprecated(forRemoval = true)
    boolean selectSimpleFolderKeyBaseHasNextByUuid(PrivateFolderCursorBaseConditionByUuid condition);



    // 헬퍼메서드 레벨
    Optional<SimpleFolder> selectSimpleFolderByFolderId(Long folderId);
    Optional<SimpleFolder> selectSimpleFolderByFolderUuid(UUID folderUuid);
    Optional<FolderDetailsDraft> selectFolderDetails(UUID folderUuid);
    // 폴더 메타데이터 수정
    int updateFolder(FolderModifyCommand command);
    // 폴더이동
    int updateFolderParent(SubFolderMoveCommand command);
    // 폴더이동 (?) 이거 뭐였지
    int updateSubFolderPath(MovedSubFolderPathCommand pathCommand);
    // 폴더 softDelete
    int updateFolderStatusAsDelete(@Param("folderUuid") UUID folderUuid, @Param("status")FolderStatus status);


    FolderTargetAccessControlProjection selectFolderWithAccessControlResources(@Param("folderId") Long folderId);
    /*
    Folder selectFolderDetailByFolderId(Long folderId);

    Folder selectFoldersByParentUuid(UUID folderUuid);

     */
}
