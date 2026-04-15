package com.uruz.frostbite.service;

import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.enums.FolderType;
import com.uruz.frostbite.mapper.FolderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FolderService {

    private final FolderMapper folderMapper;

    @Autowired
    public FolderService(FolderMapper folderMapper){
        this.folderMapper = folderMapper;
    }
    /** 최초 사용자 가입 시 개인폴더 할당 */
    public void createInitialRootPrivateFolder(String initialFolderName, Integer userId){
        // 최초가입시 자동 개인폴더 root 할당. 이경우 일반 폴더생성과 관심사 흐름이 다름 (폴더생성 =/= 자동루트폴더생성)
        // 따라서 별도 메서드로 처리

        // transactional 걸려있고 컨트롤러 공개가 안되는 메서드, userId는 정상으로 처리되었을것이라 신뢰하고 앱 레벨 정합성검사 생략
        Folder initialPrivateRootFolder = Folder.createRoot(initialFolderName, userId, FolderType.PRIVATE);
        folderMapper.insertRootFolder(initialPrivateRootFolder);
    }

    /** 폴더 생성 */
    public Folder createPrivateFolder(Integer userId, String folderName, UUID parentFolderUUID){
        return null;
    }
}
