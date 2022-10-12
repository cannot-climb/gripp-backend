package kr.njw.gripp.video.application.dto;

import kr.njw.gripp.video.entity.vo.VideoStatus;
import lombok.Data;

@Data
public class FindVideoAppResponse {
    private boolean success;
    private String uuid = "";
    private String streamingUrl = "";
    private int streamingLength;
    private double streamingAspectRatio;
    private String thumbnailUrl = "";
    private VideoStatus status = VideoStatus.PREPROCESSING;
}
