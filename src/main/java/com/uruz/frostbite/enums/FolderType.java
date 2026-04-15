package com.uruz.frostbite.enums;

public enum FolderType {
    PRIVATE(1),
    TEAM(2);

    private final int value;

    FolderType(int value) {this.value = value;}

    public int getValue() { return value;}

    public static FolderType fromValue(int value){
        for(FolderType type : values()){
            if(type.value == value){
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown FolderType value : " + value);
    }
}
