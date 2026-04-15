package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.Folder;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface FolderMapper {

    void insertRootFolder(Folder folder);
}
