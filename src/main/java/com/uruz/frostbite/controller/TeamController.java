package com.uruz.frostbite.controller;

import com.uruz.frostbite.configuration.SwaggerExamples;
import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.dto.team.TeamCreateRequest;
import com.uruz.frostbite.dto.team.TeamDetailResponse;
import com.uruz.frostbite.dto.team.TeamModifyRequest;
import com.uruz.frostbite.dto.wrapper.ApiErrorResponse;
import com.uruz.frostbite.dto.wrapper.ApiSucceedResponse;
import com.uruz.frostbite.security.CustomUserDetails;
import com.uruz.frostbite.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/team")
@Tag(name = "TEAM API")
public class TeamController {

    private final TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService){
        this.teamService = teamService;
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "정상 생성 완료",
                    useReturnTypeSchema = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiSucceedResponse.class),
                            examples = @ExampleObject(
                                    name = "정상응답",
                                    summary = "팀 생성 성공 시 응답",
                                    value = SwaggerExamples.TEAM_CREATE_SUCCESS_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "실패 : 필수 값 validated 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "실패응답 : Request body validated 실패",
                                    summary = "팀 생성 실패 시 응답",
                                    value = SwaggerExamples.TEAM_CREATE_BAD_REQUEST_EXAMPLE
                            )
                    )

            )
    })
    @PostMapping("/")
    @Operation(summary = "팀 생성", description = "팀 생성 + 자동 팀 폴더 생성, 생성자는 팀 리더")
    public ResponseEntity<?> createTeam(
            @AuthenticationPrincipal(expression = "userId") Integer createUserId,
            @Validated @RequestBody TeamCreateRequest request
            ) throws GeneralCustomException {

        TeamDetailResponse team = teamService.createTeam(createUserId, request);
        return ResponseEntity.ok(new ApiSucceedResponse<TeamDetailResponse>(team));
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "정상 조회 완료",
                    useReturnTypeSchema = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiSucceedResponse.class),
                            examples = @ExampleObject(
                                    name = "정상응답",
                                    summary = "팀 리스트 응답",
                                    value = SwaggerExamples.TEAM_LIST_SUCCESS_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "가입한 팀 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "정상응답",
                                    summary = "가입한 팀 없음",
                                    value = SwaggerExamples.TEAM_LIST_SUCCESS_EMPTY_EXAMPLE
                            )
                    )

            )
    })
    @GetMapping("/list")
    @Operation(summary = "가입한 팀 리스트 조회", description = "사용자 본인 가입 팀 리스트 조회")
    public ResponseEntity<?> getTeams(
            @AuthenticationPrincipal (expression = "userId") Integer userId
    ){
        List<TeamDetailResponse> teams = teamService.getTeamsByUserId(userId);
        return ResponseEntity.ok(new ApiSucceedResponse<>(teams));
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "정상 조회 완료",
                    useReturnTypeSchema = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiSucceedResponse.class),
                            examples = @ExampleObject(
                                    name = "정상응답",
                                    summary = "사용자 가입 성공 시 응답",
                                    value = SwaggerExamples.TEAM_DETAIL_SUCCESS_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "실패 : 제시한 파라미터로 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "실패응답 : not found",
                                    summary = "사용자 가입 실패 시 응답",
                                    value = SwaggerExamples.TEAM_DETAIL_NOT_FOUND_EXAMPLE
                            )
                    )

            )
    })
    @GetMapping("/")
    @Operation(summary = "팀 상세정보 조회", description = "본인이 가입한 팀 중 특정 팀 상세정보 조회")
    public ResponseEntity<?> getTeam(
            @RequestParam(name="uuid") UUID teamUuid,
            @AuthenticationPrincipal (expression = "userId") Integer userId
    ){
        TeamDetailResponse team = teamService.getTeamDetail(teamUuid);
        return ResponseEntity.ok(new ApiSucceedResponse<TeamDetailResponse>(team));
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "정상 수정 완료",
                    useReturnTypeSchema = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiSucceedResponse.class),
                            examples = @ExampleObject(
                                    name = "정상응답",
                                    summary = "팀 정보 수정 완료",
                                    value = SwaggerExamples.TEAM_MODIFY_SUCCESS_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "실패 : 필수 값 validated 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "실패응답 : Request body validated 실패",
                                    summary = "팀 수정 실패 시 응답 응답",
                                    value = SwaggerExamples.TEAM_MODIFY_BAD_REQUEST_EXAMPLE
                            )
                    )

            )
    })
    @PutMapping("/")
    @Operation(summary = "팀 상세정보 수정", description = "본인이 가입한 팀 중 특정 팀 상세정보 수정")
    public ResponseEntity<?> updateTeam(
            @Valid @RequestBody TeamModifyRequest modifyRequest,
            @AuthenticationPrincipal (expression = "userId") Integer modifyUserId
            ){

        TeamDetailResponse team = teamService.modifyTeam(modifyRequest, modifyUserId);
        return ResponseEntity.ok(new ApiSucceedResponse<TeamDetailResponse>(team));
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "정상 삭제 완료",
                    useReturnTypeSchema = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiSucceedResponse.class),
                            examples = @ExampleObject(
                                    name = "정상응답",
                                    summary = "팀 soft delete 성공",
                                    value = SwaggerExamples.TEAM_REMOVE_SUCCESS_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "실패 : 제시한 조건으로 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "실패응답 : Request body validated 실패",
                                    summary = "팀 soft 삭제 실패 시 응답",
                                    value = SwaggerExamples.TEAM_REMOVE_NOT_FOUND_EXAMPLE
                            )
                    )

            )
    })
    @DeleteMapping("/")
    @Operation(summary = "팀 폐쇄", description = "팀 폐쇄 (soft delete)")
    public ResponseEntity<?> removeTeam(
            @RequestParam(name = "uuid") UUID teamUuid,
            @AuthenticationPrincipal (expression = "userId") Integer userId
    ){
        teamService.softDeleteTeam(teamUuid, userId);
        return ResponseEntity.ok(new ApiSucceedResponse<>(null));
    }

    @PostMapping("/invitation")
    @Operation(summary = "팀 초대 코드 생성", description = "UUID 제공, ")
    public ResponseEntity<?> createInvitation(){
        return null;
    }

    @PostMapping("/join")
    @Operation(summary = "User 팀 가입 by invitation code", description = "UUID 코드 입력을 통한 사용자 팀 가입, 권한은 자동으로 멤버")
    public ResponseEntity<?> joinTeamByCode(){

        return null;
    }
}
