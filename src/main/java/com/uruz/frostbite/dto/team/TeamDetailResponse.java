package com.uruz.frostbite.dto.team;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uruz.frostbite.enums.team.TeamStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@Builder
@Schema(
        description = "팀 상세정보"
)
public class TeamDetailResponse {

        @Schema(
                description = "팀 이름"
        )
        /** 팀 이름 */
        private String name;

        @Schema(
                description = "팀 생성시각"
        )
        @JsonFormat(pattern = "yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
        /** 생성시각 */
        private LocalDateTime createTime;

        /** 팀 생성자 이름
         * PK(serial) 노출을 최대한 안하기 위해, 사용자명으로 리턴
         * Team Projection으로부터 user_id를 뽑아내서 User 객체를 받아오는것은 접근낭비라 판단,
         * Join절로 Mybatis에서 바로 가져오는 방향으로 수행
         * */
        @Schema(
                description = "팀 생성자 이름"
        )
        private String createUserName;

        @Schema(
                description = "팀 UUID"
        )
        /** 팀 UUID */
        private UUID uuid;

        @Schema(
                description = "팀 상태"
        )
        /** 삭제여부 */
        private TeamStatus status;

}
