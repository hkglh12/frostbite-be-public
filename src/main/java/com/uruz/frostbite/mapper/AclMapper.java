package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.FolderAcl;
import com.uruz.frostbite.enums.AclEffectType;
import com.uruz.frostbite.enums.AclStatus;
import com.uruz.frostbite.enums.FolderStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

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

    public boolean isUserDeniedByAcl(@Param("userId") Integer userId,
                                     @Param("folderId") Long folderId,
                                     @Param("effectType")AclEffectType aclEffectType,
                                     @Param("status") AclStatus status);
    public Optional<FolderAcl> selectActiveFolderMaxPermAclForEval(@Param("userId") Integer userId,
                                                                   @Param("teamId") Integer teamId,
                                                                   @Param("folderId") Long folderId,
                                                                   @Param("effectType")AclEffectType aclEffectType,
                                                                   @Param("status")AclStatus status
                                                                   );
}
