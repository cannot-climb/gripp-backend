package kr.njw.gripp.video.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.njw.gripp.video.entity.vo.VideoStatus;
import lombok.Data;

@Data
public class FindVideoResponse {
    @Schema(description = "영상 아이디", example = "7dc53df5-703e-49b3-8670-b1c468f47f1f")
    private String videoId;
    @Schema(description = "영상 스트리밍 URL (인코딩 중이면 공백 문자열)",
            example = "https://objectstorage.ap-seoul-1.oraclecloud.com/n/cngzlmggdnp2/b/gripp/o/sample/master.m3u8")
    private String streamingUrl = "";
    @Schema(description = "영상 길이 (초 단위, 오차 범위 약 1초, 인코딩 중이면 0)", example = "45")
    private int streamingLength;
    @Schema(description = "영상 화면 가로 대비 세로의 비율 (인코딩 중이면 0)", example = "1.7777777777777777")
    private double streamingAspectRatio;
    @Schema(description = "영상 썸네일 URL (인코딩 중이면 공백 문자열)",
            example = "https://objectstorage.ap-seoul-1.oraclecloud.com/n/cngzlmggdnp2/b/gripp/o/sample/thumbnail.png")
    private String thumbnailUrl = "";
    @Schema(description = "영상 판정 (PREPROCESSING: 인코딩 중, NO_CERTIFIED: 등반 실패, CERTIFIED: 등반 성공)", example = "CERTIFIED")
    private VideoStatus status = VideoStatus.PREPROCESSING;
}
