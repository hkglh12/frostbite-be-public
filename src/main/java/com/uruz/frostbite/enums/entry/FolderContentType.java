package com.uruz.frostbite.enums.entry;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum FolderContentType {
    FOLDER(1),
    FILE(2);

    private final int value;

    FolderContentType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @JsonCreator
    public static FolderContentType fromValue(int value) {
        return Arrays.stream(values())
                .filter(v -> v.getValue() == value)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unknown FolderType: " + value)
                );
    }
}
