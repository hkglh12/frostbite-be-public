package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.dto.entry.FolderContents;
import com.uruz.frostbite.dto.folder.SimpleFolder;
import com.uruz.frostbite.dto.folder.SimpleFolderTreeNode;
import com.uruz.frostbite.dto.folder.internal.*;
import com.uruz.frostbite.enums.FileStatus;
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

    // 개인폴더 조회 (시스템관점)
    Optional<Folder> selectFolderByFolderId(Long folderId);
    // 개인폴더 조회 (FE입력)
    Optional<Folder> selectAccessiblePrivateFolder(PrivateFolderCondition condition);

    // Cursor 베이스 폴더 조회 (좌측 폴더트리용)
    List<SimpleFolderTreeNode> selectPrivateFolderCursorBaseByUuid(PrivateFolderCursorBaseConditionByUuid condition);
    // 다음 UUID 조회 (Cursor 베이스)
    Optional<UUID> selectPrivateFolderCursorBaseNext(PrivateFolderCursorBaseConditionByUuid condition);

    // 개인폴더 폴더 컨텐츠 전체 개수 조회
    int selectPrivateFolderContentsTotalCount(@Param("userId") Integer userId, @Param("parentFolderId") Long parentFolderId,
                                              @Param("folderStatus") FolderStatus folderStatus,
                                              @Param("fileStatus")FileStatus fileStatus);
    // 개인폴더 컨텐츠 조회
    List<FolderContents> selectPrivateFolderContentsList(@Param("userId") Integer userId, @Param("parentFolderId") Long parentFolderId
            , @Param("offset") Integer offset, @Param("limit") Integer limit,
                                                         @Param("folderStatus") FolderStatus folderStatus,
                                                         @Param("fileStatus")FileStatus fileStatus);


    /** 팀 폴더*/
    // 내부에 folder_type = 2 하드코딩되어있음 (유스케이스별 메서드분리목적)
    // 내부에 folder_status = 1 하드코딩되어있음 (살아있는 폴더만 조회) -> 이것도 외부로 꺼낼 수 있는지 조사할것
    // 접근가능 팀 엔트리폴더 조회
    Optional<Folder> selectAccessibleTeamEntryFolderByTeamUuid(@Param("teamUuid")UUID teamUuid, @Param("userId") Integer userId);
    // 팀 폴더 조회
    // 개인폴더와 합치기 애매함. target Column 자체가 달라서 Condition도 합쳐져야 하는데, 이경우 privateFolder Condition에 항상 resource_scope_team_id가 따라다니게됨.
    Optional<Folder> selectAccessibleTeamFolder(TeamFolderCondition condition);

    // Cursor 베이스 폴더 조회 (좌측 폴더트리용)
    List<SimpleFolderTreeNode> selectTeamFolderCursorBaseByUuid(TeamFolderCursorBaseConditionByFolderUuid condition);
    // 다음 UUID 조회 (Cursor 베이스)
    Optional<UUID> selectTeamFolderCursorBaseNext(TeamFolderCursorBaseConditionByFolderUuid condition);


    // 팀 폴더 컨텐츠 전체 개수 조회
    int selectTeamFolderContentsTotalCount(@Param("teamUuid") UUID teamUuid, @Param("parentFolderId") Long parentFolderId, @Param("userId") Integer userId,
                                           @Param("folderStatus") FolderStatus folderStatus,
                                           @Param("fileStatus")FileStatus fileStatus);
    // 팀 폴더 컨텐츠 조회
    List<FolderContents> selectTeamFolderContentsList(@Param("teamUuid") UUID teamUuid, @Param("parentFolderId") Long parentFolderId, @Param("userId") Integer userId,
                                                      @Param("offset") Integer offset, @Param("limit") Integer limit,
                                                      @Param("folderStatus") FolderStatus folderStatus,
                                                      @Param("fileStatus")FileStatus fileStatus);



    /** 개인/팀 공용 */
    // 접근가능한 폴더 조회
    Optional<Folder> selectAccessibleFolderByParentFolderUuid(@Param("parentFolderUuid")UUID parentFolderUuid, @Param("userId") Integer userId);
    Optional<Folder> selectAccessibleFolderByFolderUuid(@Param("folderUuid")UUID folderUuid, @Param("userId") Integer userId);

    // 헬퍼메서드 레벨
    Optional<SimpleFolder> selectSimpleFolderByFolderId(Long folderId);
    Optional<SimpleFolder> selectSimpleFolderByFolderUuid(UUID folderUuid);
    Optional<FolderDetailsDraft> selectFolderDetails(UUID folderUuid);
    // 폴더 메타데이터 수정
    int updateFolder(FolderModifyCommand command);
    // 폴더이동
    int updateFolderParent(SubFolderMoveCommand command);

    FolderPathProjection selectFolderPath(FolderPathCondition condition);
    // 폴더 softDelete
    int updateFolderStatusAsDelete(@Param("folderUuid") UUID folderUuid, @Param("status")FolderStatus status);


    FolderTargetAccessControlProjection selectFolderWithAccessControlResources(@Param("folderId") Long folderId);
    /*
    Folder selectFolderDetailByFolderId(Long folderId);

    Folder selectFoldersByParentUuid(UUID folderUuid);

     */
}
