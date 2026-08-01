package com.uruz.frostbite.enums;

import com.uruz.frostbite.utils.CipherUtils;
import lombok.Getter;

@Getter
public enum EncryptFormatVersion {

    // AES256ECB -> 같은 문자열은 같은 암호문
    // AES256CBC -> 이전 블록이 다음블록에 영향미침, 중간읽기 불가
    // AES256CTR -> 위변조검사 없고 한바이트가 바뀌어도 복호화됨. (내용이 이상해지긴 하지만)
    // AES256GCM -> CTR 방식 + GCM 태그 (위변조검사)
    AES256GCM(1, "AES/GCM/NoPadding", 32, 16);

    private final int version;
    private final String cipherTransformation;
    private final int keyLengthBytes;
    private final int ivByteLength;
    EncryptFormatVersion(int version, String cipherTransformation,
                         int keyLengthBytes, int ivByteLength){
        if (ivByteLength < 1 || ivByteLength > CipherUtils.IV_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "IV length must fit in an unsigned byte: " + ivByteLength
            );
        }
        this.version = version;
        this.cipherTransformation = cipherTransformation;
        this.keyLengthBytes = keyLengthBytes;
        this.ivByteLength = ivByteLength;
    }

    public static EncryptFormatVersion fromValue(int value){
        // switch 보단 return이 더 "동작 판단" 에 맞게될수도..
        return switch(value){
            case 1 -> AES256GCM;
            default -> null;
        };
    }
}
