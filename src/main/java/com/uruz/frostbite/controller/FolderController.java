package com.uruz.frostbite.controller;

import com.uruz.frostbite.dto.folder.TeamRootFolderCreateRequest;
import com.uruz.frostbite.security.CustomUserDetails;
import com.uruz.frostbite.service.FolderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/folder")
@Tag(name="폴더 관련 API", description = "팀폴더/개인폴더 해당 API에서 제어 제공")
public class FolderController {
    private final FolderService folderService;

    @Autowired
    public FolderController(FolderService folderService){
        this.folderService = folderService;
    }

    /** 사용자 UUID를 기반으로 개인폴더 ROOT를 만든다.*/
    @PostMapping("/private/root")
    public ResponseEntity<?> createPrivateRootFolder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            // 내지는 코드ㅔ서 SecurityContextHolder 가져와도됌
            @RequestBody String folderName
    ){
        folderService.createPrivateRootFolder(userDetails.getUuid(), folderName);
        return null;
    }

    /** 팀 UUID를 기반으로 팀 ROOT를 만든다.*/
    @PostMapping("/team/root")
    public ResponseEntity<?> createTeamRootFolder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            // 내지는 코드에서 SecurityContextHolder 가져와도됌
            @RequestBody TeamRootFolderCreateRequest request
    ){
        folderService.createTeamRootFolder(userDetails.getUuid(), request);
        return null;
    }

    /** 부모 폴더 UUID를 기준으로 폴더 생성 */
    /*
    @PostMapping
    public ResponseEntity<?> createFolder(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody
    ){
        return null;
    }

     */

    /** 개인폴더 ROOT를 사용자 UUID를 기준으로 조회한다.*/
    @GetMapping("/priavte/roots")
    public ResponseEntity<?> getPrivateRootFoldersByUserUuid(UUID userUuid){
        return null;
    }

    @GetMapping("/team/roots")
    /** 팀 폴더 ROOT를 기본할당 팀 폴더를 기준으로 조회한다. */
    public ResponseEntity<?> getTeamRootFoldersByTeamUuid(UUID teamUuid){
        return null;
    }
    @GetMapping("/folders")
    /** 사용자가 클릭한 폴더의 UUID를 기준으로 서브폴더를 조회한다 */
    public ResponseEntity<?> getSubFolders(){

        return null;
    }

    /** 지정한 폴더를 수정한다 */
    @PutMapping
    public ResponseEntity<?> updateFolder(){
        return null;
    }


    /** 지정한 폴더를 softDelete한다 */
    public ResponseEntity<?> removeFolder(){

        return null;
    }


}
