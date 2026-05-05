package com.uruz.frostbite.controller;

import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.dto.folder.FolderCreateRequest;
import com.uruz.frostbite.dto.folder.TeamRootFolderCreateRequest;
import com.uruz.frostbite.dto.wrapper.ApiSucceedResponse;
import com.uruz.frostbite.enums.FolderType;
import com.uruz.frostbite.security.CustomUserDetails;
import com.uruz.frostbite.service.FolderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    /** ENTRY FOLDER는 사용자가입 / 팀 생성 시 즉각 함꼐 만들어져야 한다.
     * 따라서 Controller 레벨 URL 제공 안됨 */

    /** 사용자 UUID를 기반으로 개인폴더 ROOT 폴더를 만든다.*/
    /** ROOT 폴더의 경우, 부모 UUID 폴더를 알수 없다. (사용자 UUID나, TEAM UUID만 제공되기 때문) */
    /** 따라서 별도의 URL로 분리해서 서빙한다.*/
    @PostMapping("/private/root")
    public ResponseEntity<?> createPrivateRootFolder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            // 내지는 코드ㅔ서 SecurityContextHolder 가져와도됌
            @RequestBody String folderName
    ){
        Folder folder = folderService.createPrivateRootFolder(userDetails.getUuid(), folderName);
        return ResponseEntity.ok(new ApiSucceedResponse<>(folder));
    }

    // 반드시 기억할것 -> 사용자가 Team UUID를 보내오면 -> 내부에서 entryFolder를 조회한뒤 -> Root 리스트를 제공함
    // 비즈니스 로직상 루트폴더연산 등등 작업 시, 엔트리폴더 존재를 까먹어서는 안됨.
    /** 팀 UUID를 기반으로 팀 ROOT를 만든다.*/
    @PostMapping("/teams/{teamUuid}/roots")
    public ResponseEntity<?> createTeamRootFolder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            // TODO Body에서 teamUUid 꺼낼것
            // RestfulAPI URL 상, "자원" 은 URL에 표현되어야 함
            // 위 URL은 "여러개 team 중" / "특정 UUID 팀에" / root 를 @POST 가 되어야함.
            @RequestBody TeamRootFolderCreateRequest request
    ){
        Folder folder = folderService.createTeamRootFolder(userDetails.getUuid(), request);

        return ResponseEntity.ok(new ApiSucceedResponse<>(folder));
    }

    /** 부모 폴더 UUID를 기준으로 서브폴더 생성 */
    @PostMapping("/")
    public ResponseEntity<?> createFolder(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @Validated  @RequestBody FolderCreateRequest request
            ){
        Folder folder = folderService.createFolder(userId, request);

        return ResponseEntity.ok(new ApiSucceedResponse<>(folder));
    }

    // 사용자에게 직접적으로 entry folder를 제공하거나 보이지 않는다.
    /** 개인폴더 ROOT 폴더들을 사용자 ID를 기준 offset Pagination base로 조회한다. (메인컨텐츠뷰 용)*/
    @GetMapping("/private/roots")
    public ResponseEntity<?> getPrivateRootFoldersByOffsetBase(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @RequestParam(required = false, name="offset", defaultValue = "0") Integer offset,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit
    ){
        List<Folder> folders = folderService.getPrivateRootFoldersByOffsetBase(userId, offset, limit);
        return null;
    }

    /** 개인폴더 ROOT 폴더들을 사용자 ID를 기준 Keyset Pagination로 조회한다. (좌측트리 용)*/
    @GetMapping("/private/roots/tree")
    public ResponseEntity<?> getPrivateRootFoldersByKeysetBase(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @RequestParam(required = false, name="lastFolderUuid") UUID lastFolderUuid,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit
    ){
        List<Folder> folders = folderService.getPrivateRootFoldersByKeysetBase(userId, lastFolderUuid, limit);
        return null;
    }


    // 반드시 기억할것 -> 사용자가 Team UUID를 보내오면 -> 내부에서 entryFolder를 조회한뒤 -> Root 리스트를 제공함
    // 비즈니스 로직상 루트폴더연산 등등 작업 시, 엔트리폴더 존재를 까먹어서는 안됨.
    /** 팀 폴더 ROOT 폴더들을 팀 UUID를 기준으로 offset Pagination base로 조회한다. (메인컨텐츠뷰용) */
    @GetMapping("/teams/{teamUuid}/roots")
    public ResponseEntity<?> getTeamRootFoldersByOffsetBase(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @PathVariable(required = true, name="teamUuid") UUID teamUuid,
            @RequestParam(required = false, name="offset", defaultValue = "0") Integer offset,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit
    ){
        List<Folder> folders = folderService.getTeamRootFolderByOffsetBase(userId, teamUuid, offset, limit);
        return null;
    }


    /** 팀 폴더 ROOT 폴더들을 팀 UUID를 기준으로 keyset pagination base로 조회 (좌측트리뷰용)*/
    @GetMapping("/teams/{teamUuid}/roots/tree")
    public ResponseEntity<?> getTeamRootFoldersByKeysetBase(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @PathVariable(name = "teamUuid") UUID teamUuid,
            @RequestParam(required = false, name="lastFolderUuid") UUID lastFolderUuid,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit
    ){
        List<Folder> folders = folderService.getTeamRootFoldersByKeysetBase(userId, teamUuid, lastFolderUuid, limit);
        return null;
    }

    @GetMapping("/{folderUuid}/entries")
    /** 사용자가 클릭한 폴더의 UUID를 기준으로 그 폴더의 내용물 (서브폴더와 파일)을 함께 조회한다 */
    public ResponseEntity<?> getSubFolderEntriesByOffsetBase(
            @AuthenticationPrincipal(expression =  "userId" ) Integer userId,
            @PathVariable(name="folderUuid") UUID folderUuid,
            @RequestParam(required = false, name="offset", defaultValue = "0") Integer offset,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit
    ){

        return null;
    }
    @GetMapping("/{folderUuid}/subfolders/tree")
    /** 사용자가 클릭한 폴더의 UUID를 기준으로 서브폴더를 조회한다.
     * 좌측 트리에서는 파일을 출력해주지 않을것이므로 폴더만 조회하면 된다.
     * */
    public ResponseEntity<?> getSubFoldersByKeysetBase(
            @AuthenticationPrincipal(expression =  "userId" ) Integer userId,
            @PathVariable(name="folderUuid") UUID folderUuid,
            @RequestParam(required = false, name="lastFolderUuid") UUID lastFolderUuid,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit
    ){
        return null;
    }

    @GetMapping("/detail")
    /** 사용자가 클릭한 폴더의 UUID의 상세내역을 조회한다.*/
    public ResponseEntity<?> getSubFolderDetail(){

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
