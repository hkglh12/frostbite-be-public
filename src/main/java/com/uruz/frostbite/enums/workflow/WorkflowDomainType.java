package com.uruz.frostbite.enums.workflow;

import lombok.Getter;

@Getter
public enum WorkflowDomainType {
    WF_TYPE_FILE("파일"),
    WF_TYPE_FOLDER("폴더"),
    WF_TYPE_NOTE("노트");
    private final String caption;

    WorkflowDomainType(String caption) {
        this.caption = caption;
    }
}
