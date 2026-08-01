package com.uruz.frostbite.mapper;

import com.uruz.frostbite.domain.UnitTaskRequest;
import com.uruz.frostbite.enums.workflow.UnitTaskRequestStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Mapper
@Repository
public interface UnitTaskMapper {
    void insertUnitTaskRequest(UnitTaskRequest unitTaskRequest);
    UnitTaskRequest selectLatestUnitTaskRequest(@Param("workflowRequestId") long workflowRequestId);

    void updateUnitTaskRequestStatus(@Param("unitTaskRequestId") long unitTaskRequestId,
                                     @Param("status") UnitTaskRequestStatus status,
                                     @Param("currentTime") LocalDateTime currentTime);

    void updateUnitTaskRequestEndStatus(@Param("unitTaskRequestId") long unitTaskRequestId,
                                        @Param("status") UnitTaskRequestStatus status,
                                        @Param("currentTime") LocalDateTime currentTime);
}
