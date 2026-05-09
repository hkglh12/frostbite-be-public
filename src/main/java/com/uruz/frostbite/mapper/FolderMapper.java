package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.dto.folder.FolderSimpleResponse;
import com.uruz.frostbite.dto.folder.MovedSubFolderPathCommand;
import com.uruz.frostbite.dto.folder.internal.PrivateEntryFolderCondition;
import com.uruz.frostbite.dto.folder.internal.SimpleFolderOffsetConditionById;
import com.uruz.frostbite.dto.folder.internal.SubFolderMoveCommand;
import com.uruz.frostbite.dto.folder.internal.TeamEntryFolderCondition;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Mapper
public interface FolderMapper {

    void insertEntryFolder(Folder folder);

    void insertRootFolder(Folder newFolder);
    void insertSubFolder(Folder newFolder);

    Optional<Folder> selectPrivateEntryFolder(PrivateEntryFolderCondition condition);
    Optional<Folder> selectTeamEntryFolder(TeamEntryFolderCondition condition);

    //Folder selectFolderDetailByUuid(UUID folderUuid);
    Optional<Folder> selectFolderSimpleByUuid(UUID folderUuid);

    int updateSubFolderParent(SubFolderMoveCommand command);

    int updateSubFolderPath(MovedSubFolderPathCommand pathCommand);

    List<FolderSimpleResponse> selectSimpleFolderOffsetById(SimpleFolderOffsetConditionById condition);
    Folder selectFolderDetailByFolderId(Long folderId);

    Folder selectFoldersByParentUuid(UUID folderUuid);
}
