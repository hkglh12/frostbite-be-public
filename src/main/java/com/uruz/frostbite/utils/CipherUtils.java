package com.uruz.frostbite.utils;

import com.uruz.frostbite.dto.encrypt.EncryptedFileMagicHeader;
import com.uruz.frostbite.enums.EncryptFormatVersion;
import lombok.Getter;

import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

@Getter
public class CipherUtils {
    public static final String FROSBITE_MAGIC_CHARSET = "FRST";
    public static final int MAGIC_CHARSE_BYTE_SIZE = 4;
    public static final char FROSBITE_ENC_VERSION_MARKER = 'V';
    public static final int VERSION_MARKER_BYTE_SIZE = Byte.BYTES;
    public static final int VERSION_NUMBER_BYTE_SIZE = Byte.BYTES;
    public static final int HEADER_LENGTH_BYTE_SIZE = Short.BYTES;
    public static final int HEADER_IV_LENGTH_BYTE_SIZE = Byte.BYTES;
    public static final int IV_MAX_LENGTH = 0xFF; // 255 (Unsiged Byte MAX)

    private static final SecureRandom secureRandom = new SecureRandom();
    public static AlgorithmParameterSpec createParameterSpec(EncryptFormatVersion algo, byte[] ivByte){

        switch(algo){
            case AES256GCM -> {
                return new GCMParameterSpec(128, ivByte);
            }
            default -> {
                return null;
            }
        }

    }

    public static byte[] createNewIv(int length){
        byte[] iv = new byte[length];
        // math random은 사실상 수학 공식으로부터 도출되는 "관측가능한 랜덤" 이라고..

        secureRandom.nextBytes(iv);
        return iv;
    }

    public static void writeEncHeader(DataOutputStream outputStream, EncryptFormatVersion algorithm, byte[] ivByte) throws IOException {
        byte[] variableHeaderIvSection = null;
        try(
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                DataOutputStream headerOutputStream = new DataOutputStream(buffer)){

            headerOutputStream.writeByte(ivByte.length);
            headerOutputStream.write(ivByte);
            headerOutputStream.flush();
            variableHeaderIvSection = buffer.toByteArray();
        }catch (Exception e){
            throw e;
        }

        int fixedHeaderByteSize =
                CipherUtils.MAGIC_CHARSE_BYTE_SIZE    // Magic VARCHAR : 4
                        + CipherUtils.VERSION_MARKER_BYTE_SIZE    // VERSION MARKER : 1
                        + CipherUtils.VERSION_NUMBER_BYTE_SIZE       // VERSION Number : 1
                        + CipherUtils.HEADER_LENGTH_BYTE_SIZE;  // Magic Byte + Version Marker + Version Number
        // + IV Length Byte + IV Bytes 총 합을 65535 내에서 끊음 (2 bytes)

        if(fixedHeaderByteSize + variableHeaderIvSection.length > 65535) {
            throw new IllegalStateException("Header Size over 65535");
        }
        int headerSize = fixedHeaderByteSize + variableHeaderIvSection.length;

        outputStream.write(CipherUtils.FROSBITE_MAGIC_CHARSET.getBytes(StandardCharsets.US_ASCII));
        outputStream.write(CipherUtils.FROSBITE_ENC_VERSION_MARKER);
        outputStream.writeByte(algorithm.getVersion());
        outputStream.writeShort(headerSize);
        outputStream.write(variableHeaderIvSection);
        outputStream.flush();

    }

    public static EncryptedFileMagicHeader readEncryptedFileMagicHeader(DataInputStream dataInputStream) throws Exception{
        EncryptedFileMagicHeader encryptedFileMagicHeader = null;
        // Header parsing을 위한 별도 Stream
        // magic 4byte
        byte[] magic = new byte[CipherUtils.MAGIC_CHARSE_BYTE_SIZE];
        dataInputStream.readFully(magic);
        String magicString = new String(magic, StandardCharsets.US_ASCII);

        if(!CipherUtils.FROSBITE_MAGIC_CHARSET.equals(magicString)){
            throw new IllegalArgumentException("INVALID FROSBITE ENCRYPTED FILE");
        }

        // version marker 'V'
        char versionMarker = (char) dataInputStream.readUnsignedByte();
        if(CipherUtils.FROSBITE_ENC_VERSION_MARKER != versionMarker){
            throw new IllegalArgumentException("INVALID VERSION MARKER FOUND");
        }

        // version number
        int version = dataInputStream.readUnsignedByte();
        EncryptFormatVersion format = EncryptFormatVersion.fromValue(version);
        if(format == null){
            throw new IllegalArgumentException("INVALID VERSION NUMBER");
        }
        // total header length
        int headerLength = dataInputStream.readUnsignedShort();
        // IV Length 1~255 허용임을 도메인 설계 규칙으로 잡았음. 고정값을 읽으면 됨.
        int ivLength = dataInputStream.readUnsignedByte();

        int expectedHeaderLength =
                CipherUtils.MAGIC_CHARSE_BYTE_SIZE
                        + CipherUtils.VERSION_MARKER_BYTE_SIZE
                        + CipherUtils.VERSION_NUMBER_BYTE_SIZE
                        + CipherUtils.HEADER_LENGTH_BYTE_SIZE
                        + CipherUtils.HEADER_IV_LENGTH_BYTE_SIZE
                        + ivLength;

        if (headerLength != expectedHeaderLength) {
            throw new IllegalArgumentException("INVALID HEADER LENGTH. expected=" + expectedHeaderLength + ", actual=" + headerLength);
        }

        byte[] ivBytes = new byte[ivLength];
        dataInputStream.readFully(ivBytes);
        encryptedFileMagicHeader = new EncryptedFileMagicHeader(format, ivLength, ivBytes);

        return encryptedFileMagicHeader;
    }
}
