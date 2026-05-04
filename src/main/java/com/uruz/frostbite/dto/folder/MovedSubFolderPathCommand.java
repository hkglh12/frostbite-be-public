package com.uruz.frostbite.dto.folder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MovedSubFolderPathCommand {
    private String oldFolderIdPath;
    private String oldFolderNamePath;
    private String newFolderIdPath;
    private String newFolderNamePath;
}
