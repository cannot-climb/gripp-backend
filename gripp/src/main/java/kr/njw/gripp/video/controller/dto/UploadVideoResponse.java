package kr.njw.gripp.video.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UploadVideoResponse {
    @Schema(description = "영상 아이디", example = "7dc53df5-703e-49b3-8670-b1c468f47f1f")
    private String videoId;
}
