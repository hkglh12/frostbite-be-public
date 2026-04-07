package com.uruz.frostbite.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileStatus {

    /** 파일 식별자 */
    private Long fileId;

    /** 업로드 상태 */
    private Integer uploadStatus;
}
