package com.uruz.frostbite.enums.workflow;

import lombok.Getter;

@Getter
public enum UnitTask {

    // V1 파일 1Kb 암호화
    UT_FILE_ENCRYPT_TO_STORAGE_V1( "V1 암호화 (AES256GCM)"),
    // V2 파일 전문 암호화
    // Application 임시파일을 영구저장소 Storage로 copy
    UT_FILE_COPY_STORAGE("Storage로 copy"),
    // 미리보기 생성 등
    UT_FILE_MAKE_PREVIEW("활용본 생성"),
    // ELS 동기화
    UT_FILE_SYNC_SEARCH("통합검색 동기화"),
    // 임시파일 제거
    UT_FILE_REMOVE_TEMP("임시파일 제거");
    private final String caption;

    UnitTask(String caption) {
        this.caption = caption;
    }
}
