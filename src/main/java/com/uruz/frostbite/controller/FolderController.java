package com.uruz.frostbite.controller;

import com.uruz.frostbite.configuration.SwaggerExamples;
import com.uruz.frostbite.dto.folder.*;
import com.uruz.frostbite.dto.wrapper.ApiErrorResponse;
import com.uruz.frostbite.dto.wrapper.ApiSucceedResponse;
import com.uruz.frostbite.security.CustomUserDetails;
import com.uruz.frostbite.service.FolderService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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


    /** 개인 폴더 용 */

    @ApiResponse(
            responseCode = "200",
            description = "폴더 생성 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiSucceedResponse.class),
                    examples = @ExampleObject(
                            name = "성공 응답",
                            summary = "폴더 요약 정보",
                            value = SwaggerExamples.SIMPLE_FOLDER_RESPONSE_SUCCESS
                    )
            )
    )

    /** ENTRY FOLDER는 사용자가입 / 팀 생성 시 즉각 함꼐 만들어져야 한다.
     * 따라서 Controller 레벨 URL 제공 안됨 */
    /** 사용자 UUID를 기반으로 개인폴더 ROOT 폴더를 만든다.*/
    /** ROOT 폴더의 경우, 부모 UUID 폴더를 알수 없다. (사용자 UUID나, TEAM UUID만 제공되기 때문) */
    /** 따라서 별도의 URL로 분리해서 서빙한다.*/
    @PostMapping("/private/root")
    public ResponseEntity<?> createPrivateRootFolder(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            // 내지는 코드ㅔ서 SecurityContextHolder 가져와도됌
            @RequestBody String folderName
    ){
        SimpleFolderResponse folder = folderService.createPrivateRootFolder(userId, folderName);
        return ResponseEntity.ok(new ApiSucceedResponse<>(folder));
    }

    // 사용자에게 직접적으로 entry folder를 제공하거나 보이지 않는다.
    /** 개인폴더 ROOT 폴더들을 사용자 ID를 기준 offset Pagination base로 조회한다. (메인컨텐츠뷰 용)*/
    @ApiResponse(
            responseCode = "200",
            description = "폴더 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiSucceedResponse.class),
                    examples = @ExampleObject(
                            name = "성공 응답",
                            summary = "폴더 offset 목록",
                            value = SwaggerExamples.SIMPLE_FOLDER_LIST_OFFSET_RESPONSE_SUCCESS
                    )
            )
    )
    @GetMapping("/private/roots")
    public ResponseEntity<?> getPrivateRootFoldersByOffsetBase(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @RequestParam(required = false, name="offset", defaultValue = "0") Integer offset,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit
    ){
        SimpleFolderListOffsetResponse folders = folderService.getPrivateRootFoldersByOffsetBase(userId, offset, limit);
        return ResponseEntity.ok(new ApiSucceedResponse<>(folders));
    }

    /** 개인폴더 ROOT 폴더들을 사용자 ID를 기준 Keyset Pagination로 조회한다. (좌측트리 용)*/
    // 사용자 PK IN -> entryFolder select -> pk로 조회
    @ApiResponse(
            responseCode = "200",
            description = "폴더 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiSucceedResponse.class),
                    examples = @ExampleObject(
                            name = "성공 응답",
                            summary = "폴더 keyset 목록",
                            value = SwaggerExamples.SIMPLE_FOLDER_LIST_KEYSET_RESPONSE_SUCCESS
                    )
            )
    )
    @GetMapping("/private/roots/tree")
    public ResponseEntity<?> getPrivateRootFoldersByKeysetBase(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @RequestParam(required = false, name="lastFolderUuid") UUID lastFolderUuid,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit
    ){
        SimpleFolderListKeysetResponse folders = folderService.getPrivateRootFoldersByKeysetBase(userId, lastFolderUuid, limit);
        return ResponseEntity.ok(new ApiSucceedResponse<>(folders));
    }

    /** 팀 폴더 전용*/
    // 반드시 기억할것 -> 사용자가 Team UUID를 보내오면 -> 내부에서 entryFolder를 조회한뒤 -> Root 리스트를 제공함
    // 비즈니스 로직상 루트폴더연산 등등 작업 시, 엔트리폴더 존재를 까먹어서는 안됨.
    /** 팀 UUID를 기반으로 팀 ROOT를 만든다.*/
    @ApiResponse(
            responseCode = "200",
            description = "폴더 생성 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiSucceedResponse.class),
                    examples = @ExampleObject(
                            name = "성공 응답",
                            summary = "폴더 요약 정보",
                            value = SwaggerExamples.SIMPLE_FOLDER_RESPONSE_SUCCESS
                    )
            )
    )
    @PostMapping("/teams/{teamUuid}/roots")
    public ResponseEntity<?> createTeamRootFolder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            // TODO Body에서 teamUUid 꺼낼것
            // RestfulAPI URL 상, "자원" 은 URL에 표현되어야 함
            // 위 URL은 "여러개 team 중" / "특정 UUID 팀에" / root 를 @POST 가 되어야함.
            @RequestBody TeamRootFolderCreateRequest request
    ){
        SimpleFolderResponse folder = folderService.createTeamRootFolder(userId, request);

        return ResponseEntity.ok(new ApiSucceedResponse<>(folder));
    }



    // 반드시 기억할것 -> 사용자가 Team UUID를 보내오면 -> 내부에서 entryFolder를 조회한뒤 -> Root 리스트를 제공함
    // 비즈니스 로직상 루트폴더연산 등등 작업 시, 엔트리폴더 존재를 까먹어서는 안됨.
    /** 팀 폴더 ROOT 폴더들을 팀 UUID를 기준으로 offset Pagination base로 조회한다. (메인컨텐츠뷰용) */

    @ApiResponse(
            responseCode = "200",
            description = "폴더 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiSucceedResponse.class),
                    examples = @ExampleObject(
                            name = "성공 응답",
                            summary = "폴더 offset 목록",
                            value = SwaggerExamples.SIMPLE_FOLDER_LIST_OFFSET_RESPONSE_SUCCESS
                    )
            )
    )
    @GetMapping("/teams/{teamUuid}/roots")
    public ResponseEntity<?> getTeamRootFoldersByOffsetBase(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @PathVariable(required = true, name="teamUuid") UUID teamUuid,
            @RequestParam(required = false, name="offset", defaultValue = "0") Integer offset,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit
    ){
        SimpleFolderListOffsetResponse folders = folderService.getTeamRootFoldersByOffsetBase(userId, teamUuid, offset, limit);
        return ResponseEntity.ok(new ApiSucceedResponse<>(folders));
    }


    /** 팀 폴더 ROOT 폴더들을 팀 UUID를 기준으로 keyset pagination base로 조회 (좌측트리뷰용)*/
    @ApiResponse(
            responseCode = "200",
            description = "폴더 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiSucceedResponse.class),
                    examples = @ExampleObject(
                            name = "성공 응답",
                            summary = "폴더 keyset 목록",
                            value = SwaggerExamples.SIMPLE_FOLDER_LIST_KEYSET_RESPONSE_SUCCESS
                    )
            )
    )
    @GetMapping("/teams/{teamUuid}/roots/tree")
    public ResponseEntity<?> getTeamRootFoldersByKeysetBase(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @PathVariable(name = "teamUuid") UUID teamUuid,
            @RequestParam(required = false, name="lastFolderUuid") UUID lastFolderUuid,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit
    ){
        SimpleFolderListKeysetResponse response = folderService.getTeamRootFoldersByKeysetBase(userId, teamUuid, lastFolderUuid, limit);
        return ResponseEntity.ok(new ApiSucceedResponse<>(response));
    }
    /** 팀 / 개인 공통*/

    @ApiResponse(
            responseCode = "200",
            description = "폴더 생성 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiSucceedResponse.class),
                    examples = @ExampleObject(
                            name = "성공 응답",
                            summary = "폴더 요약 정보",
                            value = SwaggerExamples.SIMPLE_FOLDER_RESPONSE_SUCCESS
                    )
            )
    )
    /** 부모 폴더 UUID를 기준으로 서브폴더 생성 */
    @PostMapping("/")
    public ResponseEntity<?> createFolder(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @Validated  @RequestBody FolderCreateRequest request
    ){
        SimpleFolderResponse folder = folderService.createSubFolder(userId, request);

        return ResponseEntity.ok(new ApiSucceedResponse<>(folder));
    }


    @ApiResponse(
            responseCode = "200",
            description = "폴더 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiSucceedResponse.class),
                    examples = @ExampleObject(
                            name = "성공 응답",
                            summary = "폴더 keyset 목록",
                            value = SwaggerExamples.SIMPLE_FOLDER_LIST_KEYSET_RESPONSE_SUCCESS
                    )
            )
    )
    @GetMapping("/{folderUuid}/subfolders/tree")
    /** 사용자가 클릭한 폴더의 UUID를 기준으로 서브폴더를 조회한다.
     * 좌측 트리에서는 파일을 출력해주지 않을것이므로 폴더만 조회하면 된다.
     * */
    public ResponseEntity<?> getSubFoldersByKeysetBase(
            @AuthenticationPrincipal(expression =  "userId" ) Integer userId,
            @PathVariable(name="folderUuid") UUID parentFolderUuid,
            @RequestParam(required = false, name="lastFolderUuid") UUID lastFolderUuid,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit
    ){
        SimpleFolderListKeysetResponse response = folderService.getSubFoldersByKeysetBaseByUuid(userId, parentFolderUuid, lastFolderUuid, limit);
        return ResponseEntity.ok(new ApiSucceedResponse<>(response));
    }
    @ApiResponse(
            responseCode = "200",
            description = "폴더 및 파일 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiSucceedResponse.class),
                    examples = @ExampleObject(
                            name = "성공 응답",
                            summary = "폴더 및 파일 offset 목록",
                            value = SwaggerExamples.SIMPLE_FOLDER_ENTRIES_LIST_OFFSET_RESPONSE_SUCCESS
                    )
            )
    )
    @GetMapping("/{folderUuid}/entries")
    /** 사용자가 클릭한 폴더의 UUID를 기준으로 그 폴더의 내용물 (서브폴더와 파일)을 함께 조회한다 */
    public ResponseEntity<?> getSubFolderEntriesByOffsetBase(
            @AuthenticationPrincipal(expression =  "userId" ) Integer userId,
            @PathVariable(name="folderUuid") UUID parentFolderUuid,
            @RequestParam(required = false, name="offset", defaultValue = "0") Integer offset,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit
    ){
        SimpleFolderEntriesListOffsetResponse entries = folderService.getSubFolderEntriesOffsetBaseByUuid(userId, parentFolderUuid, offset, limit);
        return ResponseEntity.ok(new ApiSucceedResponse<>(entries));
    }

    @ApiResponse(
            responseCode = "200",
            description = "폴더 상세 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiSucceedResponse.class),
                    examples = @ExampleObject(
                            name = "성공 응답",
                            summary = "폴더 상세 정보",
                            value = SwaggerExamples.FOLDER_DETAIL_RESPONSE_SUCCESS
                    )
            )
    )
    @GetMapping("/{folderUuid}/detail")
    /** 사용자가 클릭한 폴더의 UUID의 상세내역을 조회한다.*/
    public ResponseEntity<?> getSubFolderDetail(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @PathVariable(name = "folderUuid") UUID folderUuid
    ){
        FolderDetailsResponse folder = folderService.getFolderDetails(userId, folderUuid);
        return ResponseEntity.ok(new ApiSucceedResponse<>(folder));
    }

    /** 지정한 폴더를 수정한다 */
    @ApiResponse(
            responseCode = "200",
            description = "폴더 수정 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiSucceedResponse.class),
                    examples = @ExampleObject(
                            name = "성공 응답",
                            summary = "폴더 수정 결과",
                            value = SwaggerExamples.FOLDER_MODIFY_RESPONSE_SUCCESS
                    )
            )
    )
    @PutMapping("/{folderUuid}")
    public ResponseEntity<?> updateFolder(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @PathVariable(name = "folderUuid") UUID folderUuid,
            @RequestBody @Validated FolderModifyRequest request
    ){
        SimpleFolderResponse folder = folderService.modifyFolder(userId, folderUuid, request);
        return ResponseEntity.ok(new ApiSucceedResponse<>(folder));
    }

    /** 지정한 폴더 이동 (부모 폴더 변경) */
    @ApiResponse(
            responseCode = "200",
            description = "폴더 이동 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiSucceedResponse.class),
                    examples = @ExampleObject(
                            name = "성공 응답",
                            summary = "폴더 이동 결과",
                            value = SwaggerExamples.FOLDER_MOVE_RESPONSE_SUCCESS
                    )
            )
    )
    @PutMapping("/{folderUuid}/location")
    public ResponseEntity<?> moveFolder(
        @AuthenticationPrincipal(expression = "userId") Integer userId,
        @PathVariable(name="folderUuid") UUID folderUuid,
        @RequestBody @Validated FolderMoveRequest folderMoveRequest
    ) {

        SimpleFolderResponse folder = folderService.moveFolder(userId, folderUuid, folderMoveRequest);
        return ResponseEntity.ok(new ApiSucceedResponse<>(folder));
    }

    /** 지정한 폴더를 softDelete한다 */
    @ApiResponse(
            responseCode = "200",
            description = "폴더 삭제 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiSucceedResponse.class),
                    examples = @ExampleObject(
                            name = "성공 응답",
                            summary = "폴더 삭제 결과",
                            value = SwaggerExamples.FOLDER_REMOVE_RESPONSE_SUCCESS
                    )
            )
    )
    @DeleteMapping("/{folderUuid}")
    public ResponseEntity<?> removeFolder(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @PathVariable(name="folderUuid") UUID folderUuid
    ){

        boolean result = folderService.softDeleteFolder(userId, folderUuid);
        return ResponseEntity.ok(new ApiSucceedResponse<>(result));
    }

}
