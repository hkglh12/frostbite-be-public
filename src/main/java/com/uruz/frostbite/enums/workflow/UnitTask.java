package com.uruz.frostbite.enums.workflow;

import lombok.Getter;

@Getter
public enum UnitTask {
    UT_FILE_ENCRYPT( "암호화"),
    UT_FILE_COPY_STORAGE("임시파일 저장위치로 COPY"),
    UT_FILE_MAKE_PREVIEW("활용본 생성"),
    UT_FILE_SYNC_SEARCH("통합검색 동기화"),
    UT_FILE_REMOVE_TEMP("임시파일 제거");
    private final String caption;

    UnitTask(String caption) {
        this.caption = caption;
    }
}
