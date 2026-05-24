package com.uruz.frostbite.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public enum PermissionBitMask {
    NONE(0, "DENIED"),
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
            if (permission.value != 0 && (mask & permission.value) == permission.value) {
                result.add(permission);
            }
        }

        return result;
    }

    public static PermissionBitMask[] fromMaskToArray(Integer mask) {
        if (mask == null || mask == 0) {
            return new PermissionBitMask[0];
        }

        return fromMask(mask).toArray(new PermissionBitMask[0]);
    }

    // 비트마스크를 통해 Integer 연산
    public static int of(PermissionBitMask... permissions) {
        int bitMask = 0;

        for (PermissionBitMask permission : permissions) {
            if (permission == null) {
                continue;
            }
            bitMask |= permission.value;
        }

        return bitMask;
    }

    public static PermissionBitMask[] allMasks() {
        return Arrays.stream(values())
                .filter(permission -> permission.value != 0)
                .toArray(PermissionBitMask[]::new);
    }

    public static boolean has(Integer bitMask, PermissionBitMask permission) {
        if (bitMask == null || permission == null || permission.value == 0) {
            return false;
        }

        return (bitMask & permission.value) == permission.value;
    }

    public static boolean hasAny(Integer bitMask, PermissionBitMask... permissions) {
        if (bitMask == null || permissions == null) {
            return false;
        }

        for (PermissionBitMask permission : permissions) {
            if (has(bitMask, permission)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAll(Integer bitMask, PermissionBitMask... permissions) {
        if (bitMask == null || permissions == null) {
            return false;
        }

        for (PermissionBitMask permission : permissions) {
            if (!has(bitMask, permission)) {
                return false;
            }
        }
        return true;
    }
}
