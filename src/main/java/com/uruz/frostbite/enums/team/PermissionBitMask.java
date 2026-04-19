package com.uruz.frostbite.enums.team;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.EnumSet;
import java.util.Set;

public enum PermissionBitMask {
    // 브라우징 (폴더/파일 리스팅 가능)
    BROWSE(1, "BROWSE"),
    // 다운로드, 읽기
    READ(2, "READ"),
    // 생성, 수정(+덮어쓰기) + 삭제 / 폴더, 파일명 수정 등 쓰기전반
    WRITE(4, "WRITE"),
    // 메타데이터 수정 (공유, ACL제어)
    CONTROL(8, "CONTROL"),
    // 폴더 타 팀 소유권 이전
    MANAGE(16, "MANAGE");

    private final int value;
    private final String name;

    PermissionBitMask(int value, String name)
    {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    @JsonValue
    public String getName(){return name;}

    public static PermissionBitMask fromValue(int value) {
        for (PermissionBitMask bitMask : values()) {
            if (bitMask.value == value) {
                return bitMask;
            }
        }
        throw new IllegalArgumentException("Unknown PermissionBitMask value: " + value);
    }

    public static Set<PermissionBitMask> fromMask(int mask) {
        EnumSet<PermissionBitMask> result = EnumSet.noneOf(PermissionBitMask.class);

        for (PermissionBitMask permission : values()) {
            if ((mask & permission.value) == permission.value) {
                result.add(permission);
            }
        }

        return result;
    }

    public static boolean hasPermission(int mask, PermissionBitMask permission) {
        return (mask & permission.getValue()) == permission.getValue();
    }

}
