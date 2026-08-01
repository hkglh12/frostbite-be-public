package com.uruz.frostbite.dto.file;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class TempFileMetaInfoDraft {
    /** 임시파일 등록 간 산출된 정보 */

    /** 원본파일명 */
    private String originalFileName;
    /** 파일크기 */
    private Long fileSize;
    /** 임시파일 UUID */
    private UUID tempFileUuid;
}
