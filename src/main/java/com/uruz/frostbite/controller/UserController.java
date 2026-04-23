package com.uruz.frostbite.controller;

import com.uruz.frostbite.configuration.SwaggerExamples;
import com.uruz.frostbite.dto.user.UserJoinRequest;
import com.uruz.frostbite.dto.user.UserJoinResponse;
import com.uruz.frostbite.dto.user.UserSelfQueryResponse;
import com.uruz.frostbite.dto.wrapper.ApiErrorResponse;
import com.uruz.frostbite.dto.wrapper.ApiSucceedResponse;
import com.uruz.frostbite.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/v1/api/user")
@Tag(name="일반 사용자 사용자 관련 API", description = "일반 사용자의 본인 정보 조회/수정/삭제 및 가입")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "정상 가입 완료",
                    useReturnTypeSchema = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiSucceedResponse.class),
                            examples = @ExampleObject(
                                    name = "정상응답",
                                    summary = "사용자 가입 성공 시 응답",
                                    value = SwaggerExamples.USER_JOIN_SUCCESS
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
                                    summary = "사용자 가입 실패 시 응답",
                                    value = SwaggerExamples.USER_JOIN_BAD_REQUEST
                            )
                    )

            )
    })
    @PostMapping(value="/")
    @Operation(summary="사용자 회원가입", description="사용자 회원가입")
    public ResponseEntity<ApiSucceedResponse<UserJoinResponse>> createUser(@Validated @RequestBody UserJoinRequest createRequest){

        UserJoinResponse response = userService.createUser(createRequest);
        return ResponseEntity.ok(new ApiSucceedResponse<>(response));
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
                                    summary = "사용자 본인 정보 조회 성공",
                                    value = SwaggerExamples.USER_SELF_GET_SUCCEESS
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "실패 : 해당 사용자 조회 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "실패응답 :사용자 조회 실패",
                                    summary = "사용자 조회 실패 시 응답",
                                    value = SwaggerExamples.USER_SELF_GET_FAILED
                            )
                    )

            )
    })
    @GetMapping(value="/")
    @Operation(summary="사용자 본인 정보조회", description="사용자 본인 정보조회")
    public ResponseEntity<?> getSelfUser(
            // 추후 JWT 기반으로 전환 예정, 이 시점부터 JWT로부터 사용자 정보 취득(RedisMapping) -> SecurityCTX에서 값을 뽑아서 조회하는 방식으로 변경 예정
            @AuthenticationPrincipal(expression = "uuid") UUID uuid
    ){
        UserSelfQueryResponse response = userService.getSelfUesr(uuid);

        return ResponseEntity.ok(new ApiSucceedResponse<>(response));
    }

    @DeleteMapping(value="/")
    public ResponseEntity<?> removeUser(
            // 추후 JWT 기반으로 전환 예정, 이 시점부터 JWT로부터 사용자 정보 취득(RedisMapping) -> SecurityCTX에서 값을 뽑아서 조회하는 방식으로 변경 예정
            @RequestParam(name="uuid", required = true) UUID uuid
    ){
        boolean result = userService.removeUser(uuid);
        return null;
    }

    @PutMapping(value="/")
    public ResponseEntity<?> modifyUser(){
        // 추후 JWT 기반으로 전환 예정, 이 시점부터 JWT로부터 사용자 정보 취득(RedisMapping) -> SecurityCTX에서 값을 뽑아서 조회하는 방식으로 변경 예정

        return null;
    }

}
