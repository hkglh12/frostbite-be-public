package com.uruz.frostbite.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "팀 생성 요청")
public class TeamCreateRequest {

    @NotEmpty
    @Size(max=256)
    @Schema(description = "팀 이름")
    private String name;

}
