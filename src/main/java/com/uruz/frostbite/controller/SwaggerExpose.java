package com.uruz.frostbite.controller;

import com.uruz.frostbite.configuration.SwaggerExamples;
import com.uruz.frostbite.dto.wrapper.ApiErrorResponse;
import com.uruz.frostbite.dto.wrapper.ApiSucceedResponse;
import com.uruz.frostbite.security.SecurityContextUserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1")
@Tag(name="Swagger-Expose", description = "Swagger-UI 노출 전용 URL 명시")
public class SwaggerExpose {

    @PostMapping("/login")
    @Operation(
            summary = "로그인",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "로그인 예시",
                                    value = SwaggerExamples.USER_LOGIN_PARAMETER_EXAMPLE
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiSucceedResponse.class),
                            examples = @ExampleObject(
                                    name = "정상응답",
                                    summary = "로그인 완료, Session Base이므로 JsessoionID 참고",
                                    value = SwaggerExamples.USER_LOGIN_SUCCESS_EXAMPLE
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
                                    value = SwaggerExamples.USER_LOGIN_FAILURE_EXAMPLE
                            )
                    )

            )
    })
    public ResponseEntity<SecurityContextUserContext> login(
            @RequestBody Map<String, String> body
    ){

        return null;
    }
}
