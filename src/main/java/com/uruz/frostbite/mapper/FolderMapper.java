package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.dto.folder.SimpleFolderResponse;
import com.uruz.frostbite.dto.folder.internal.MovedSubFolderPathCommand;
import com.uruz.frostbite.dto.folder.internal.*;
import com.uruz.frostbite.enums.FolderStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
@Repository
@Mapper
public interface FolderMapper {

    void insertEntryFolder(Folder folder);

    void insertRootFolder(Folder newFolder);
    void insertSubFolder(Folder newFolder);

    Optional<Folder> selectPrivateEntryFolder(PrivateEntryFolderCondition condition);
    Optional<Folder> selectTeamEntryFolder(TeamEntryFolderCondition condition);

    List<SimpleFolderResponse> selectSimpleFolderOffsetById(SimpleFolderOffsetConditionByFolderId condition);
    List<SimpleFolderResponse> selectSimpleFolderOffsetByUuid(SimpleFolderOffsetConditionByUuid condition);
    int selectSimpleFolderTotalCountById(SimpleFolderOffsetConditionByFolderId condition);
    int selectSimpleFolderTotalCountByUuid(SimpleFolderOffsetConditionByUuid condition);
    List<SimpleFolderResponse> selectSimpleFolderKeyBaseById(SimpleFolderKeyBaseConditionByFolderId condition);
    List<SimpleFolderResponse> selectSimpleFolderKeyBaseByUuid(SimpleFolderKeyBaseConditionByUuid condition);

    boolean selectSimpleFolderKeyBaseHasNext(SimpleFolderKeyBaseConditionByFolderId condition);
    boolean selectSimpleFolderKeyBaseHasNextByUuid(SimpleFolderKeyBaseConditionByUuid condition);

    Optional<SimpleFolderResponse> selectSimpleFolderByFolderId(Long folderId);
    Optional<SimpleFolderResponse> selectSimpleFolderByFolderUuid(UUID folderUuid);
    Optional<FolderDetailsDraft> selectFolderDetails(UUID folderUuid);
    //Folder selectFolderDetailByUuid(UUID folderUuid);
    Optional<Folder> selectFolderByUuid(UUID folderUuid);

    int updateFolder(FolderModifyCommand command);

    int updateFolderParent(SubFolderMoveCommand command);

    int updateSubFolderPath(MovedSubFolderPathCommand pathCommand);

    int updateFolderStatusAsDelete(@Param("folderUuid") UUID folderUuid, @Param("status")FolderStatus status);


    /*
    Folder selectFolderDetailByFolderId(Long folderId);

    Folder selectFoldersByParentUuid(UUID folderUuid);

     */
}
