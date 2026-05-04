package com.uruz.frostbite.dto.folder.internal;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SubFolderMoveCommand {

    /** Destination Parent Folder UUID */
    private Long destinationFolderId;

    /** 이동 대상 폴더 UUID */
    private Long targetFolderId;

    /** 수정자 PK */
    private Integer modifyUserId;

    private SubFolderMoveCommand(Long targetFolderId, Long destinationFolderId, Integer modifyUserId){
        this.targetFolderId = targetFolderId;
        this.destinationFolderId = destinationFolderId;
        this.modifyUserId = modifyUserId;
    }
    public static SubFolderMoveCommand of(Long targetFolderId, Long destinationFolderId, Integer modifyUserId){
        return new SubFolderMoveCommand(targetFolderId, destinationFolderId, modifyUserId);
    }
}
