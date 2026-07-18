package com.uruz.frostbite.dto.encrypt;

import com.uruz.frostbite.enums.EncryptFormatVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class EncryptedFileMagicHeader {

    EncryptFormatVersion formatVersion;
    int ivLength;
    byte[] ivBytes;
}
