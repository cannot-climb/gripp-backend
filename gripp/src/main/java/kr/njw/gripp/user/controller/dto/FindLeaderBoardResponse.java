package kr.njw.gripp.user.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FindLeaderBoardResponse {
    @Schema(description = "상위 리더보드 (전체 유저 상위 최대 10명)", maxLength = 10)
    private List<FindUserResponse> topBoard = new ArrayList<>();
    @Schema(description = "내 근처 리더보드 (나를 포함하여 나보다 상위 최대 10명부터 하위 최대 10명까지, 총합 최대 21명)", maxLength = 21)
    private List<FindUserResponse> defaultBoard = new ArrayList<>();
}
