package com.uruz.frostbite.controller;

import com.uruz.frostbite.dto.TeamCreateRequest;
import com.uruz.frostbite.dto.TeamDetailResponse;
import com.uruz.frostbite.dto.wrapper.ApiSucceedResponse;
import com.uruz.frostbite.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/team")
@Tag(name = "TEAM API")
public class TeamController {

    private final TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService){
        this.teamService = teamService;
    }


    @PostMapping("/")
    @Operation(summary = "팀 생성", description = "팀 생성 + 자동 팀 폴더 생성, 생성자는 팀 리더")
    public ResponseEntity<?> createTeam(
            @Validated @RequestBody TeamCreateRequest request
            ){

        TeamDetailResponse team = teamService.createTeam(request);
        return ResponseEntity.ok(new ApiSucceedResponse<TeamDetailResponse>(team));
    }

    @GetMapping("/list")
    @Operation(summary = "가입한 팀 리스트 조회", description = "사용자 본인 가입 팀 리스트 조회")
    public ResponseEntity<?> getTeams(){
        return null;
    }

    @GetMapping("/")
    @Operation(summary = "팀 상세정보 조회", description = "본인이 가입한 팀 중 특정 팀 상세정보 조회")
    public ResponseEntity<?> getTeam(
            @RequestParam(name="uuid") String uuid
    ){
        TeamDetailResponse team = teamService.getTeam(uuid);
        return ResponseEntity.ok(new ApiSucceedResponse<TeamDetailResponse>(team));
    }

    @PutMapping("/")
    @Operation(summary = "팀 상세정보 수정", description = "본인이 가입한 팀 중 특정 팀 상세정보 수정")
    public ResponseEntity<?> updateTeam(){

        return null;
    }

    @DeleteMapping("/")
    @Operation(summary = "팀 폐쇄", description = "팀 폐쇄 (soft delete)")
    public ResponseEntity<?> removeTeam(){
        return null;
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
