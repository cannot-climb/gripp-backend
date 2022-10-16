package kr.njw.gripp.article.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.njw.gripp.user.controller.dto.FindUserResponse;
import kr.njw.gripp.video.controller.dto.FindVideoResponse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FindArticleResponse {
    @Schema(description = "게시물 아이디", example = "42")
    private String articleId;
    private FindUserResponse user;
    private FindVideoResponse video;
    @Schema(description = "제목", example = "Bell of the Wall")
    private String title;
    @Schema(description = "설명", example = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
    private String description;
    @Schema(description = "난이도 (0 이상 19 이하)", example = "3", minimum = "0", maximum = "19")
    private int level;
    @Schema(description = "벽각도 (0 이상 90 이하)", example = "45", minimum = "0", maximum = "90")
    private int angle;
    @Schema(description = "조회수", example = "103")
    private long viewCount;
    @Schema(description = "좋아요 수", example = "18")
    private long favoriteCount;
    @Schema(description = "내가 좋아요를 눌렀는지 여부", example = "true")
    private boolean favorite;
    @Schema(description = "게시일시", type = "string", example = "2022-10-17 13:01:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerDateTime;
}
