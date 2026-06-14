package com.uruz.frostbite.dto.file;


import com.uruz.frostbite.domain.FileTemp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.service.annotation.GetExchange;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class TempFileMetaInfoDraft {
    private String originalFileName;
    private String extension;
    private Long fileSize;
    private UUID physicalFileUuid;
}
