package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.FolderAcl;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface AclMapper {

    public int insertFolderAcl(FolderAcl folderAcl);

}
