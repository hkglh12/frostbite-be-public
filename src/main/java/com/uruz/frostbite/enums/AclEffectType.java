package com.uruz.frostbite.enums;

public enum AclEffectType {
    ALLOW(1),
    DENY(2);

    private final int value;

    AclEffectType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AclEffectType fromValue(int value) {
        for (AclEffectType status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AclEffectType value: " + value);
    }
}
