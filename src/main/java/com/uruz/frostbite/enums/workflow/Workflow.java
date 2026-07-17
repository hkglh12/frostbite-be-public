package com.uruz.frostbite.enums.workflow;

import lombok.Getter;

import java.util.List;

@Getter
public enum Workflow {

    WF_FILE_PROVISION_MOVEONLY(
            "파일등록",
            "파일 이동 전용",
            List.of(
                    UnitTask.UT_FILE_COPY_STORAGE,
                    UnitTask.UT_FILE_REMOVE_TEMP
            )
    ),

    WF_FILE_PROVISION_NORMAL(
            "파일등록",
            "일반 파일 등록",
            List.of(
                    UnitTask.UT_FILE_ENCRYPT,
                    UnitTask.UT_FILE_COPY_STORAGE,
                    UnitTask.UT_FILE_MAKE_PREVIEW,
                    UnitTask.UT_FILE_SYNC_SEARCH,
                    UnitTask.UT_FILE_REMOVE_TEMP
            )
    ),

    WF_FILE_PROVISION_INTERNAL(
            "파일등록",
            "내부 전용 파일 등록",
            List.of(
                    UnitTask.UT_FILE_ENCRYPT,
                    UnitTask.UT_FILE_COPY_STORAGE,
                    UnitTask.UT_FILE_MAKE_PREVIEW,
                    UnitTask.UT_FILE_REMOVE_TEMP
            )
    ),

    WF_FILE_PROVISION_RESTRICTED(
            "파일등록",
            "제한 파일 등록",
            List.of(
                    UnitTask.UT_FILE_ENCRYPT,
                    UnitTask.UT_FILE_COPY_STORAGE,
                    UnitTask.UT_FILE_REMOVE_TEMP
            )
    );

    private final String workflowType;

    private final String caption;

    private final List<UnitTask> unitTasks;

    Workflow(String workflowType, String caption, List<UnitTask> unitTasks) {
        validateRemoveTempIsLast(workflowType, unitTasks);
        this.workflowType = workflowType;
        this.caption = caption;
        this.unitTasks = unitTasks;
    }
    private static void validateRemoveTempIsLast(String workflowType, List<UnitTask> unitTasks) {
        if(workflowType.equals("파일등록")) {
            int index = unitTasks.indexOf(UnitTask.UT_FILE_REMOVE_TEMP);
            if (index >= 0 && index != unitTasks.size() - 1) {
                throw new IllegalStateException("UT_FILE_REMOVE_TEMP must be the last unit task");
            }
        }
    }

    public int getUnitTaskOrder(UnitTask unitTask){
        return unitTasks.indexOf(unitTask);
    }
}
