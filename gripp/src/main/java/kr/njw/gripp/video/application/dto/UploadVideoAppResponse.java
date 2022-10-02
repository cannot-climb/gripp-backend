package kr.njw.gripp.video.application.dto;

import lombok.Data;

@Data
public class UploadVideoAppResponse {
    private boolean success;
    private String message = "";
    private String uuid = "";
}
