package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.*;
import com.uruz.frostbite.dto.file.FileMetaModifyCommand;
import com.uruz.frostbite.dto.file.FileMetaResponse;
import com.uruz.frostbite.dto.file.FileMoveCommand;
import com.uruz.frostbite.dto.file.internal.InternalFileMoveCommand;
import com.uruz.frostbite.enums.FileStatus;
import com.uruz.frostbite.enums.FileTempStatus;
import com.uruz.frostbite.enums.WorkflowStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
@Mapper
public interface FileMapper {

    void insertInitial(File file);

    void insertFileWorkflowStatus(FileWorkflowStatus fileWorkflowStatus);
    /** INITIAL */
    void insertFileTemp(FileTemp fileTemp);
    FileTemp selectTempFileByUuid(@Param("uuid") UUID uuid);
    FileTemp selectReadyTempFileByUuid(@Param("uuid") UUID uuid, @Param("fileTempStatus")FileTempStatus status);
    void updateFileMetaInfoByRegistDone(@Param("fileId") Long fileId,
                                        @Param("storageFilePath") String storageFilePath,
                                        @Param("storageFileName") String storageFileName,
                                        @Param("status")FileStatus status);

    int updateFileMeta(FileMetaModifyCommand command);
    int moveFile(InternalFileMoveCommand command);
    FileMetaResponse selectFileMeta(@Param("uuid") UUID uuid);
    void updateForSoftDeletion(@Param("uuid") UUID uuid, @Param("status")FileStatus status);
    @Deprecated(forRemoval = true)
    void updateFileWorkflowStatusStarted(@Param("fileId")Long fileId,
                                         @Param("status") WorkflowStatus status,
                                         @Param("currentTime")LocalDateTime currentTime);



    void updateFileWorkflowRegisterStatusProcessing(@Param("fileId")Long fileId,
                                                    @Param("status") WorkflowStatus status,
                                                    @Param("currentTime")LocalDateTime currentTime);
    void updateFileWorkflowRegisterStatusFailed(@Param("fileId")Long fileId,
                                                @Param("status") WorkflowStatus status,
                                                @Param("currentTime")LocalDateTime currentTime,
                                                @Param("failedMessage") String failedMessage);

    void updateFileWorkflowRegisterStatusSuccess(@Param("fileId")Long fileId,
                                                 @Param("status") WorkflowStatus status,
                                                 @Param("currentTime")LocalDateTime currentTime);



    void markFileAsActive(@Param("fileId") Long fileId, @Param("status") FileStatus status);

}
