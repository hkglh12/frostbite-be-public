package com.uruz.frostbite.enums.team;

public enum TeamRole {
    /** 팀 내 역할을 표현하기 위한 도메인 역할
     *
     *
     * */
    MEMBER(1),
    MANAGER(2),
    HEAD(3);

    private final int value;

    TeamRole(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TeamRole fromValue(int value) {
        for (TeamRole status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown TeamRole value: " + value);
    }
}
