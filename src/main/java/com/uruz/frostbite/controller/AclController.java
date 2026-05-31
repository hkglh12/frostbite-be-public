package com.uruz.frostbite.controller;

import com.uruz.frostbite.dto.wrapper.ApiSucceedResponse;
import com.uruz.frostbite.enums.common.ApiSucceedCode;
import com.uruz.frostbite.service.AclService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/acl")
public class AclController {

    private final AclService aclService;

    @Autowired
    public AclController(AclService aclService) { this.aclService = aclService;}


    /** Folder ACL */
    /*
    @PostMapping("/folder")
    public ResponseEntity<?> createAcl(
    ){
        return new ResponseEntity<>(new ApiSucceedResponse<>());
    }
    */

    /** ACL을 'POST' 할 일은 현 시점 없음.
     * 폴더 ACL은 폴더 생성 시점에서 자동생성되며,
     * 1. 개인폴더 -> 개인에게 ACL 생성
     * 2. 팀 전체 공개 폴더 -> 특정인원에게 공개로 전환
     * 3. 특정 인원 공개 폴더 -> 팀 전체 공개로 전환
     *
     * 따라서 '수정' 역할만 열려있다고 보는게 맞음
     * */
    @GetMapping("/folder")
    public ResponseEntity<?> getAcl(){
        return ResponseEntity.ok(new ApiSucceedResponse<>(null));
    }

    @PutMapping("/folder")
    public ResponseEntity<?> putAcl(){
        return ResponseEntity.ok(new ApiSucceedResponse<>(null));
    }

    @DeleteMapping("/folder")
    public ResponseEntity<?> removeAcl(){
        return  ResponseEntity.ok(new ApiSucceedResponse<>(null));
    }

}
