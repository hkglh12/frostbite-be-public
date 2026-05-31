package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.FolderAcl;
import com.uruz.frostbite.enums.FolderStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@Mapper
public interface AclMapper {

    public int insertFolderAcl(FolderAcl folderAcl);

    public void cloneParentFolderAclIntoSubFolder(@Param("parentFolderId") Long parentFolderId,
                                                  @Param("targetFolderId") Long targetFolderId,
                                                  @Param("createUserId") Integer createUserId,
                                                  @Param("createTime")LocalDateTime createTime,
                                                  @Param("status")FolderStatus status
                                                  );
}
