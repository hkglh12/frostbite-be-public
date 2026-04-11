package com.uruz.frostbite.controller;

import com.uruz.frostbite.dto.UserJoinRequest;
import com.uruz.frostbite.dto.UserJoinResponse;
import com.uruz.frostbite.dto.UserSelfQueryCondition;
import com.uruz.frostbite.dto.UserSelfQueryResponse;
import com.uruz.frostbite.dto.wrapper.ApiResponse;
import com.uruz.frostbite.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@Tag(name="일반 사용자 사용자 관련 API", description = "일반 사용자의 본인 정보 조회/수정/삭제 및 가입")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }
    @PostMapping(value="/")
    public ResponseEntity<ApiResponse<UserJoinResponse>> postUser(@Validated @RequestBody UserJoinRequest createRequest){

        UserJoinResponse response = userService.createUser(createRequest);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @GetMapping(value="/")
    @Operation(summary="사용자 정보조회", description="사용자 정보조회")
    public ResponseEntity<?> getSelfUser(
            // 추후 JWT 기반으로 전환 예정, 이 시점부터 JWT로부터 사용자 정보 취득(RedisMapping) -> SecurityCTX에서 값을 뽑아서 조회하는 방식으로 변경 예정
            @Validated UserSelfQueryCondition userReadCondition
    ){
        UserSelfQueryResponse response = userService.getSelfUesr(userReadCondition);

        return ResponseEntity.ok(new ApiResponse<>(response));
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
