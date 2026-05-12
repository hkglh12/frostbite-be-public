package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.dto.folder.SimpleFolderResponse;
import com.uruz.frostbite.dto.folder.MovedSubFolderPathCommand;
import com.uruz.frostbite.dto.folder.internal.*;
import org.apache.ibatis.annotations.Mapper;
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
    //Folder selectFolderDetailByUuid(UUID folderUuid);
    Optional<Folder> selectFolderSimpleByUuid(UUID folderUuid);

    int updateSubFolderParent(SubFolderMoveCommand command);

    int updateSubFolderPath(MovedSubFolderPathCommand pathCommand);




    /*
    Folder selectFolderDetailByFolderId(Long folderId);

    Folder selectFoldersByParentUuid(UUID folderUuid);

     */
}
