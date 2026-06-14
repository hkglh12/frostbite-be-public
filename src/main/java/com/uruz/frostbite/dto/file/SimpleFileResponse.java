package com.uruz.frostbite.dto.file;

import com.uruz.frostbite.enums.FileStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class SimpleFileResponse {
    private UUID uuid;
    private String fileName;
    private UUID parentFolderUuid;
    private FileStatus fileStatus;

}
