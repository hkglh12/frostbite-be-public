package com.uruz.frostbite.controller;

import com.uruz.frostbite.configuration.SwaggerExamples;
import com.uruz.frostbite.dto.common.response.CursorBaseListResponse;
import com.uruz.frostbite.dto.common.response.OffsetBaseListResponse;
import com.uruz.frostbite.dto.entry.FolderContents;
import com.uruz.frostbite.dto.folder.*;
import com.uruz.frostbite.dto.wrapper.ApiSucceedResponse;
import com.uruz.frostbite.security.CustomUserDetails;
import com.uruz.frostbite.service.FolderService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
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



    /**
     * 1. 개인/부서의 Entry Folder는 가입/생성과 동시에 생성되어야 한다. 이에따라 URL로 제공되지 않음
     * 2. ROOT 폴더 생성 시, 부모 폴더 UUID를 받는게 아니라 사용자 정보를 받게됨. 별도 API로 제공
     * 2. 폴더컨텐츠 조회 시, 개인폴더와 팀폴더의 파라미터가 다름. 아예 분리해서 접근하는 방향으로 수행
     * */

    /** 개인 폴더 */
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

    //사용자 UUID를 기반으로 개인폴더 ROOT 생성
    @PostMapping("/private/root")
    public ResponseEntity<ApiSucceedResponse<SimpleFolder>> createPrivateRootFolder(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            // 내지는 코드에서 SecurityContextHolder 가져와도됌
            @RequestBody String folderName
    ){
        SimpleFolder folder = folderService.createPrivateRootFolder(userId, folderName);
        return ResponseEntity.ok(new ApiSucceedResponse<>(folder));
    }

    // 개인폴더 폴더리스트 KeyBase 조회 (좌측 트리 랜더용)
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
    @GetMapping("/private/folder/trees")
    public ResponseEntity<ApiSucceedResponse<CursorBaseListResponse<SimpleFolderTreeNode>>> getPrivateFoldersByCursorBase(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @RequestParam(required = false, name="parentFolderUuid") UUID parentFolderUuid,
            @RequestParam(required = false, name= "nextFolderUuid") UUID nextFolderUuid,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit
    ){
        CursorBaseListResponse<SimpleFolderTreeNode> folders = folderService.getPrivateFoldersByCursorBase(userId, parentFolderUuid, nextFolderUuid, limit);
        return ResponseEntity.ok(new ApiSucceedResponse<>(folders));
    }


    @ApiResponse(
            responseCode = "200",
            description = "폴더 컨텐츠 목록 조회 성공",
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
    //개인폴더 컨텐츠 조회 (Root, nonRoot 병합됨)
    @GetMapping("/private/folder/contents")
    public ResponseEntity<ApiSucceedResponse<OffsetBaseListResponse<FolderContents>>> getPrivateFolderContentsByOffsetBase(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @Nullable @RequestParam(required = false, name="parentFolderUuid") UUID parentFolderUuid,
            @RequestParam(required = false, name="offset", defaultValue = "0") Integer offset,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit

    ) {
        OffsetBaseListResponse<FolderContents> folderContents = folderService.getPrivateFolderContentsByOffsetBase(userId, parentFolderUuid, offset, limit);
        return ResponseEntity.ok(new ApiSucceedResponse<>(folderContents));
    }

    /** 팀 폴더 전용*/
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
    // 팀 UUID를 기반으로 팀 ROOT를 만든다.
    @PostMapping("/teams/{teamUuid}/roots")
    public ResponseEntity<ApiSucceedResponse<SimpleFolder>> createTeamRootFolder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            // TODO Body에서 teamUUid 꺼낼것
            // RestfulAPI URL 상, "자원" 은 URL에 표현되어야 함
            // 위 URL은 "여러개 team 중" / "특정 UUID 팀에" / root 를 @POST 가 되어야함.
            @RequestBody TeamRootFolderCreateRequest request
    ){
        SimpleFolder folder = folderService.createTeamRootFolder(userId, request);

        return ResponseEntity.ok(new ApiSucceedResponse<>(folder));
    }

    // 팀 폴더 컨텐츠 조회 (Root, nonRoot 병합됨)
    @GetMapping("/teams/{teamUuid}/folder/contents")
    public ResponseEntity<ApiSucceedResponse<OffsetBaseListResponse<FolderContents>>> getTeamRootFoldersByOffsetBase(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @PathVariable(required = true, name="teamUuid") UUID teamUuid,
            @RequestParam(required = false, name="parentFolderUuid") UUID parentFolderUuid,
            @RequestParam(required = false, name="offset", defaultValue = "0") Integer offset,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit
    ) {
        OffsetBaseListResponse<FolderContents> response = folderService.getTeamFolderContentsByOffsetBase(userId, teamUuid, parentFolderUuid, offset, limit);
        return ResponseEntity.ok(new ApiSucceedResponse<>(response));
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
    public ResponseEntity<ApiSucceedResponse<CursorBaseListResponse<SimpleFolderTreeNode>>> getTeamRootFoldersByKeysetBase(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @PathVariable(name = "teamUuid") UUID teamUuid,
            @RequestParam(required = false, name="parentFolderUuid") UUID parentFolderUuid,
            @RequestParam(required = false, name="nextFolderUuid") UUID nextFolderUuid,
            @RequestParam(required = false, name="limit", defaultValue = "50") Integer limit
    ){
        CursorBaseListResponse<SimpleFolderTreeNode> response = folderService.getTeamFolderByCursorBase(userId, teamUuid, parentFolderUuid, nextFolderUuid, limit);
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
    // 부모폴더 UUID를 기준으로 서브폴더 생성
    @PostMapping("/")
    public ResponseEntity<ApiSucceedResponse<SimpleFolder>> createSubFolder(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @Validated  @RequestBody FolderCreateRequest request
    ){
        SimpleFolder folder = folderService.createSubFolder(userId, request);

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
                            summary = "폴더 offset 목록",
                            value = SwaggerExamples.SIMPLE_FOLDER_LIST_OFFSET_RESPONSE_SUCCESS
                    )
            )
    )

    @ApiResponse(
            responseCode = "200",
            description = "폴더 조회 성공",
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

    @GetMapping("/{folderUuid}/detail")
    /** 사용자가 클릭한 폴더의 UUID의 상세내역을 조회한다.*/
    public ResponseEntity<ApiSucceedResponse<FolderDetailsResponse>> getSubFolderDetail(
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
    public ResponseEntity<ApiSucceedResponse<SimpleFolder>> updateFolder(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @PathVariable(name = "folderUuid") UUID folderUuid,
            @RequestBody @Validated FolderModifyRequest request
    ){
        SimpleFolder folder = folderService.modifyFolder(userId, folderUuid, request);
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
    public ResponseEntity<ApiSucceedResponse<SimpleFolder>> moveFolder(
        @AuthenticationPrincipal(expression = "userId") Integer userId,
        @PathVariable(name="folderUuid") UUID folderUuid,
        @RequestBody @Validated FolderMoveRequest folderMoveRequest
    ) {

        SimpleFolder folder = folderService.moveFolder(userId, folderUuid, folderMoveRequest);
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
    public ResponseEntity<ApiSucceedResponse<Boolean>> removeFolder(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @PathVariable(name="folderUuid") UUID folderUuid
    ){

        boolean result = folderService.softDeleteFolder(userId, folderUuid);
        return ResponseEntity.ok(new ApiSucceedResponse<>(result));
    }

}
