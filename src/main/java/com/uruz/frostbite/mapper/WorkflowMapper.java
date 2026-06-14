package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.File;
import com.uruz.frostbite.enums.WorkflowStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@Mapper
public interface WorkflowMapper {
    void updateFileRegistStartTime(@Param("startTime")LocalDateTime startTime, @Param("fileId") Long fileId, @Param("fileRegistStatus")WorkflowStatus workflowStatus);

    void updateFileRegistDone(@Param("endTime")LocalDateTime endTime, @Param("fileId") Long fileId, @Param("fileRegistStatus")WorkflowStatus workflowStatus);
}
