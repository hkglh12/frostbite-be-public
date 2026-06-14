package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.File;
import com.uruz.frostbite.domain.FileTemp;
import com.uruz.frostbite.domain.FileWorkflowStatus;
import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.dto.folder.SimpleFolderResponse;
import com.uruz.frostbite.dto.folder.internal.*;
import com.uruz.frostbite.enums.FileStatus;
import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.WorkflowStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Mapper
public interface FileMapper {

    void insertInitial(File file);

    void insertFileWorkflowStatus(FileWorkflowStatus fileWorkflowStatus);
    /** INITIAL */
    void insertFileTemp(FileTemp fileTemp);
    FileTemp selectTempFileByUuid(@Param("uuid") UUID uuid);
    void updateFileMetaInfoByRegistDone(@Param("fileId") Long fileId,
                                        @Param("storageFilePath") String storageFilePath,
                                        @Param("storageFileName") String storageFileName,
                                        @Param("status")FileStatus status);
}
