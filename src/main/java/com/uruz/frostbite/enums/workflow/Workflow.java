package com.uruz.frostbite.enums.workflow;

import lombok.Getter;

import java.util.List;

@Getter
public enum Workflow {

    WF_FILE_PROVISION_MOVEONLY(
            "파일 이동 전용",
            List.of(
                    UnitTask.UT_FILE_COPY_STORAGE,
                    UnitTask.UT_FILE_REMOVE_TEMP
            )
    ),

    WF_FILE_PROVISION_NORMAL(
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
            "내부 전용 파일 등록",
            List.of(
                    UnitTask.UT_FILE_ENCRYPT,
                    UnitTask.UT_FILE_COPY_STORAGE,
                    UnitTask.UT_FILE_MAKE_PREVIEW,
                    UnitTask.UT_FILE_REMOVE_TEMP
            )
    ),

    WF_FILE_PROVISION_RESTRICTED(
            "제한 파일 등록",
            List.of(
                    UnitTask.UT_FILE_ENCRYPT,
                    UnitTask.UT_FILE_COPY_STORAGE,
                    UnitTask.UT_FILE_REMOVE_TEMP
            )
    );

    private final String caption;

    private final List<UnitTask> unitTasks;

    Workflow(String caption, List<UnitTask> unitTasks) {
        this.caption = caption;
        this.unitTasks = unitTasks;
    }

    public int getUnitTaskOrder(UnitTask unitTask){
        return unitTasks.indexOf(unitTask);
    }
}
