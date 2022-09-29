package kr.njw.gripp.user.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FindUserResponse {
    @Schema(description = "유저 아이디", example = "njw1204")
    private String username;
    @Schema(description = "티어", example = "12", minimum = "0", maximum = "19")
    private int tier;
    @Schema(description = "점수", example = "12.35", minimum = "0.00", maximum = "19.00")
    private float score;
    @Schema(description = "순위", example = "42")
    private long rank;
    @Schema(description = "백분위 (0~100), 백분위 N = 상위 (100-N)%", example = "99", minimum = "0", maximum = "100")
    private int percentile;
    @Schema(description = "게시물 개수", example = "137")
    private int articleCount;
    @Schema(description = "등반 성공 게시물 개수", example = "63")
    private int articleCertifiedCount;
    @Schema(description = "가입일시", type = "string", example = "2022-09-28 16:46:59")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerDateTime;
}
